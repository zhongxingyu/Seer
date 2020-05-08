 package com.celements.photo.metadata;
 
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.component.annotation.Requirement;
 import org.xwiki.context.Execution;
 import org.xwiki.model.reference.DocumentReference;
 
 import com.celements.photo.container.ImageLibStrings;
 import com.drew.metadata.MetadataException;
 import com.xpn.xwiki.XWikiContext;
 import com.xpn.xwiki.XWikiException;
 import com.xpn.xwiki.doc.XWikiAttachment;
 import com.xpn.xwiki.doc.XWikiDocument;
 import com.xpn.xwiki.objects.BaseObject;
 
 @Component
 public class MetadataDocument implements IMetadataDocumentRole {
   
   private static Log LOGGER = LogFactory.getFactory().getInstance(MetadataDocument.class);
   
   @Requirement
   Execution execution;
   
   public void extractMetadataToDocument(DocumentReference source, String filename, 
       DocumentReference destination, Boolean filterImport) {
     LOGGER.debug("Extracting metadata of image [" + source + " - " + filename + "] to " +
         destination);
     try {
       XWikiDocument sourceDoc = getContext().getWiki().getDocument(source, getContext());
       XWikiAttachment file = sourceDoc.getAttachment(filename);
       MetaInfoExtractor extractor = new MetaInfoExtractor();
       Map<String, String> tags = extractor.getAllTags(file.getContentInputStream(
           getContext()));
       
       XWikiDocument destinationDoc = getContext().getWiki().getDocument(destination, 
           getContext());
       boolean needsSave = addTagsToDoc(tags, destinationDoc, filterImport);
       if(needsSave) {
         LOGGER.debug("Metatags saved!");
         getContext().getWiki().saveDocument(destinationDoc, getContext());
       }
     } catch (MetadataException metaE) {
       LOGGER.error("Exception extracting metadata for file [" + source + " - " + 
           filename + "]", metaE);
     } catch (XWikiException xwe) {
       LOGGER.error("Exception extracting metadata for file [" + source + " - " + 
           filename + "]", xwe);
     }
   }
 
   Boolean addTagsToDoc(Map<String, String> tags, XWikiDocument destinationDoc,
       Boolean filteredImport) throws XWikiException {
     Boolean needsSave = false;
     for (String tag : tags.keySet()) {
       if((filteredImport == false) || !tag.matches("Unknown .*")) {
         LOGGER.debug("Adding tag [" + tag + "]");
         needsSave = true;
         BaseObject tagObj = destinationDoc.newXObject(
             ImageLibStrings.getMetainfoClassDocRef(), getContext());
         String tagSource = tags.get(tag).replaceAll("^(\\[.*\\]) .*", "$1");
         String tagValue = tags.get(tag).replaceAll("^\\[.*\\] (.*)", "$1");
         if(tagValue.startsWith(tag + " - ")) {
           tagValue = tagValue.substring(tag.length() + 3);
         }
         tagObj.setStringValue(ImageLibStrings.METAINFO_CLASS_NAME, tag);
        tagObj.setLargeStringValue(ImageLibStrings.METAINFO_CLASS_DESCRIPTION, tagValue);
         tagObj.setStringValue("lang", "");
         tagObj.setStringValue("source", "system " + tagSource);
       }
     }
     return needsSave;
   }
   
   private XWikiContext getContext() {
     return (XWikiContext) execution.getContext().getProperty("xwikicontext");
   }
 }
