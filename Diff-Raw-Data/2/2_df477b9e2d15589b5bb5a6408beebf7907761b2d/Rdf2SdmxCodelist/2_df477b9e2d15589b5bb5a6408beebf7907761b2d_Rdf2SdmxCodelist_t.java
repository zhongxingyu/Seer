 package org.virtual.sr.transforms.codelist;
 
 import org.sdmxsource.sdmx.api.model.beans.codelist.CodelistBean;
 import org.sdmxsource.sdmx.api.model.mutable.codelist.CodeMutableBean;
 import org.sdmxsource.sdmx.api.model.mutable.codelist.CodelistMutableBean;
 import org.sdmxsource.sdmx.sdmxbeans.model.mutable.codelist.CodeMutableBeanImpl;
 import org.sdmxsource.sdmx.sdmxbeans.model.mutable.codelist.CodelistMutableBeanImpl;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.virtual.sr.transforms.Xml2Rdf;
 import org.virtual.sr.transforms.XmlTransform;
 import org.virtual.sr.utils.Constants;
 import org.virtualrepository.Asset;
 import org.virtualrepository.Properties;
 import org.virtualrepository.RepositoryService;
 import org.virtualrepository.sdmx.SdmxCodelist;
 import org.virtualrepository.spi.Transform;
 
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.vocabulary.RDF;
 import com.hp.hpl.jena.vocabulary.RDFS;
 
 /**
  * A {@link Transform}s from arbitrary {@link Asset}s in given APIs to RDF. <p>
  * Bridges {@link XmlTransform}s with the {@link Xml2Rdf} transform to adapt the {@link RepositoryService}
  * to given asset types.
  *
  * @author Fabio Simeoni
  *
  */
 public class Rdf2SdmxCodelist implements Transform<SdmxCodelist, Model, CodelistBean> {
     
     private static Logger log = LoggerFactory.getLogger(Rdf2SdmxCodelist.class);
     
     @Override
     public CodelistBean apply(SdmxCodelist asset, Model m) throws Exception {
         
         log.info("transforming codelist " + asset.id() + " to sdmx");
         CodelistMutableBean codelist = new CodelistMutableBeanImpl();
         
         Properties props = asset.properties();
         
         if (props.contains(Constants.ownerName)) {
             codelist.setAgencyId(props.lookup(Constants.ownerName).value(String.class));
         } else {
             codelist.setAgencyId("SDMX");
         }
         
         codelist.setId(asset.name());
         codelist.setUri(asset.id());
         codelist.addName("en", asset.name());
        m.write(System.out);
         ResIterator codes = m.listSubjectsWithProperty(RDF.value);
         
         while (codes.hasNext()) {
             
             Resource code_resource = codes.next();
             
             CodeMutableBean code = new CodeMutableBeanImpl();
             
             code.setId(adaptId(code_resource.getRequiredProperty(RDF.value).getLiteral().getLexicalForm()));
             code.setUri(code_resource.getURI());
             StmtIterator names = code_resource.listProperties(RDFS.label);
             while (names.hasNext()) {
                 Statement name_lit = names.next();
                 //TODO: replace with for loop over known bindings
                 if (!name_lit.getLiteral().getLexicalForm().isEmpty()) {
                     code.addName(name_lit.getLanguage(), name_lit.getLiteral().getLexicalForm());
                 }
             }
             
             StmtIterator descriptions = code_resource.listProperties(RDFS.comment);
             while (descriptions.hasNext()) {
                 Statement desc_lit = descriptions.next();
                 //TODO: replace with for loop over known bindings
                 if (!desc_lit.getLiteral().getLexicalForm().isEmpty()) {
                     code.addDescription("en", desc_lit.getLiteral().getLexicalForm());
                 }
             }
             log.info("adding code : " + code.getId());
             codelist.addItem(code);
         }
         
         return codelist.getImmutableInstance();
     }
     
     @Override
     public Class<Model> inputAPI() {
         return Model.class;
     }
     
     @Override
     public Class<CodelistBean> outputAPI() {
         return CodelistBean.class;
     }
 
     //helpers
     private String adaptId(String id) {
 
         //TODO add to this simple adaptation
         return id.replace(".", "_");
     }
 }
