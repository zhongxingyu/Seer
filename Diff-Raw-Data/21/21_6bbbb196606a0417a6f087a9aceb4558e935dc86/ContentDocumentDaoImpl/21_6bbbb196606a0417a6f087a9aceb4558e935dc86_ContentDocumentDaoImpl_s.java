 package com.indoqa.daisy.dao.impl;
 
 import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.cocoon.pipeline.NonCachingPipeline;
 import org.apache.cocoon.pipeline.Pipeline;
 import org.apache.cocoon.sax.AbstractSAXSerializer;
 import org.apache.cocoon.sax.SAXPipelineComponent;
 import org.apache.cocoon.sax.component.CleaningTransformer;
 import org.apache.cocoon.sax.component.XMLGenerator;
 import org.apache.cocoon.sax.component.XMLSerializer;
 import org.apache.cocoon.sax.component.XSLTTransformer;
 import org.apache.cocoon.xml.sax.SAXBuffer;
 import org.apache.commons.io.output.NullOutputStream;
 import org.outerj.daisy.repository.RepositoryException;
 import org.outerj.daisy.repository.VariantKey;
 import org.outerj.daisy.repository.query.QueryManager;
 import org.springframework.stereotype.Repository;
 
 import com.indoqa.daisy.dao.ContentDocumentDao;
 import com.indoqa.daisy.dao.DaisyException;
 import com.indoqa.daisy.entity.ContentDocument;
 import com.indoqa.daisy.entity.ContentField;
 import com.indoqa.daisy.entity.ContentPart;
 import com.indoqa.daisy.entity.Part;
 import com.indoqa.daisy.pipeline.DaisyGenerator;
 import com.indoqa.daisy.pipeline.DaisyLinkRewriteTransformer;
 import com.indoqa.daisy.pipeline.MetaInfoExtractorTransformer;
 
 @Repository
 public class ContentDocumentDaoImpl extends AbstractDaisyDao implements ContentDocumentDao {
 
     @Override
     public List<String> find(String query, Locale locale) {
         try {
             List<String> result = new ArrayList<String>();
 
             QueryManager queryManager = this.getDaisyRepositoryAccessFacade().getQueryManager();
             VariantKey[] docKeys = queryManager.performQueryReturnKeys(query, locale);
             for (VariantKey key : docKeys) {
                 result.add(key.getDocumentId());
             }
 
             return result;
         } catch (RepositoryException e) {
             throw new DaisyException(e);
         }
     }
 
     @Override
     public ContentDocument get(String id, Locale locale, String pathRelativizer, Map<String, String> linkRewriteTranslationTable) {
         return this.getContentDocument(id, locale, pathRelativizer, linkRewriteTranslationTable);
     }
 
     protected ContentDocument getContentDocument(String id, Locale locale, String pathRelativizer,
             Map<String, String> linkRewriteTranslationTable) {
 
         // prepare components
         DaisyGenerator daisyGenerator = new DaisyGenerator();
         daisyGenerator.setDocumentId(id);
         daisyGenerator.setAnnotationNavDocId(this.getNavDocId());
         daisyGenerator.setIsNavDoc(false);
         daisyGenerator.setDaisyAccessFacade(this.getDaisyRepositoryAccessFacade());
         daisyGenerator.setCocoonSettings(this.getSettings());
 
         MetaInfoExtractorTransformer metaInfoExtractorTransformer = new MetaInfoExtractorTransformer();
 
         final SAXBuffer document = new SAXBuffer();
 
         // setup the pipeline
         Pipeline<SAXPipelineComponent> daisyPipeline = new NonCachingPipeline<SAXPipelineComponent>();
         daisyPipeline.addComponent(daisyGenerator);
         daisyPipeline.addComponent(new DaisyLinkRewriteTransformer(pathRelativizer, linkRewriteTranslationTable) {
 
             @Override
             protected String rewriteDocumentLink(LinkInfo linkInfo) {
                 return super.rewriteDocumentLink(linkInfo) + ".html";
             }
         });
         daisyPipeline.addComponent(metaInfoExtractorTransformer);
         daisyPipeline.addComponent(new AbstractSAXSerializer() {
 
             @Override
             public void setup(Map<String, Object> inputParameters) {
                 super.setup(inputParameters);
 
                 this.contentHandler = document;
                 this.lexicalHandler = document;
             }
         });
 
         // run the pipeline (throw away the output)
         daisyPipeline.setup(new NullOutputStream());
         try {
             daisyPipeline.execute();
         } catch (Exception e) {
             throw new DaisyException("Can't execute a Cocoon pipeline that accesses a Daisy document.", e);
         }
 
         // set name, type summary, etc.
         ContentDocument contentDocument = new ContentDocument();
         contentDocument.setId(id);
         contentDocument.setName(metaInfoExtractorTransformer.getName());
         contentDocument.setType(metaInfoExtractorTransformer.getType());
         contentDocument.setSummary(metaInfoExtractorTransformer.getSummary());
         contentDocument.setAuthor(metaInfoExtractorTransformer.getAuthor());
         contentDocument.setCreated(metaInfoExtractorTransformer.getCreated());
         contentDocument.setLastModified(metaInfoExtractorTransformer.getLastModified());
 
         // set parts
         for (Part part : metaInfoExtractorTransformer.getParts()) {
             ContentPart contentPart = new ContentPart();
             // get the text from an extra pipeline
             contentPart.setText(this.getPart(document, part.getType()));
             contentPart.setType(part.getType());
             contentDocument.addContentPart(contentPart);
         }
 
         // set fields
         Map<String, ContentField> fields = metaInfoExtractorTransformer.getFields();
         for (ContentField field : fields.values()) {
             for (int i = 0; i < field.getDocumentIds().size(); i++) {
                 String documentId = field.getDocumentIds().get(i);
                 ContentDocument fieldDoc;
                 if (documentId.equals(id)) {
                     fieldDoc = contentDocument;
                 } else {
                     fieldDoc = this.getContentDocument(documentId, locale, pathRelativizer, linkRewriteTranslationTable);
                 }
                 field.addDocument(fieldDoc);
             }
         }
         contentDocument.setFields(fields);
 
         return contentDocument;
     }
 
     protected String getPart(SAXBuffer document, String partType) {
         XSLTTransformer xsltTransformer = new XSLTTransformer(this.getClass().getResource("extractPart.xsl"));
         Map<String, String> parameters = new HashMap<String, String>();
         parameters.put("partName", partType);
         xsltTransformer.setParameters(parameters);
 
         Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
         pipeline.addComponent(new XMLGenerator(document));
         pipeline.addComponent(xsltTransformer);
         pipeline.addComponent(new XMLSerializer());
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         pipeline.setup(baos);
         try {
             pipeline.execute();
         } catch (Exception e) {
             throw new DaisyException("Can't execute a Cocoon pipeline that accesses a Daisy document.", e);
         }
 
         return this.cleanupXML(baos);
     }
 
     private String cleanupXML(ByteArrayOutputStream baos) {
         Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
         pipeline.addComponent(new XMLGenerator(baos.toByteArray()));
         pipeline.addComponent(new CleaningTransformer());
         pipeline.addComponent(new XMLSerializer().setOmitXmlDeclaration(true).setMethod("html"));
 
         ByteArrayOutputStream newBaos = new ByteArrayOutputStream();
         pipeline.setup(newBaos);
         try {
             pipeline.execute();
             return new String(newBaos.toByteArray(), "utf-8");
         } catch (Exception e) {
             throw new DaisyException("Can't execute a Cocoon pipeline that accesses a Daisy document.", e);
         }
     }

    @SuppressWarnings("unused")
    public static void debug(String id, DaisyGenerator daisyGenerator) {

        Pipeline<SAXPipelineComponent> pipeline = new NonCachingPipeline<SAXPipelineComponent>();
        pipeline.addComponent(daisyGenerator);
        pipeline.addComponent(XMLSerializer.createXMLSerializer());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pipeline.setup(baos);
        try {
            pipeline.execute();
            System.out.println("result=\n" + new String(baos.toByteArray(), "UTF-8"));
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
 }
