 package com.amee.platform.service.v3.category;
 
 import com.amee.base.resource.RequestWrapper;
 import com.amee.domain.data.DataCategory;
 import com.amee.domain.data.ItemDefinition;
 import com.amee.domain.path.PathItem;
 import com.amee.domain.tag.Tag;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Service;
 
 @Service
 @Scope("prototype")
 public class DataCategoryDOMBuilder extends DataCategoryBuilder<Document> {
 
     private final static DateTimeFormatter FMT = ISODateTimeFormat.dateTimeNoMillis();
 
     public Document handle(RequestWrapper requestWrapper) {
         DataCategoryDOMRenderer renderer = new DataCategoryDOMRenderer();
         super.handle(requestWrapper, renderer);
         return renderer.getDocument();
     }
 
     public String getMediaType() {
         return "application/xml";
     }
 
     public static class DataCategoryDOMRenderer implements DataCategoryBuilder.DataCategoryRenderer {
 
         private DataCategory dataCategory;
         private Element rootElem;
         private Element dataCategoryElem;
         private Element tagsElem;
 
         public DataCategoryDOMRenderer() {
             super();
         }
 
         public void start() {
             rootElem = new Element("Representation");
         }
 
         public void ok() {
             rootElem.addContent(new Element("Status").setText("OK"));
         }
 
         public void notFound() {
             rootElem.addContent(new Element("Status").setText("NOT_FOUND"));
         }
 
         public void notAuthenticated() {
             rootElem.addContent(new Element("Status").setText("NOT_AUTHENTICATED"));
         }
 
         public void categoryIdentifierMissing() {
             rootElem.addContent(new Element("Status").setText("ERROR"));
             rootElem.addContent(new Element("Error").setText("The categoryIdentifier was missing."));
         }
 
         public void newDataCategory(DataCategory dataCategory) {
             this.dataCategory = dataCategory;
             dataCategoryElem = new Element("Category");
             if (rootElem != null) {
                 rootElem.addContent(dataCategoryElem);
             }
         }
 
         public void addBasic() {
             dataCategoryElem.setAttribute("uid", dataCategory.getUid());
             dataCategoryElem.addContent(new Element("Name").setText(dataCategory.getName()));
             dataCategoryElem.addContent(new Element("WikiName").setText(dataCategory.getWikiName()));
         }
 
         public void addPath(PathItem pathItem) {
             dataCategoryElem.addContent(new Element("Path").setText(dataCategory.getPath()));
             if (pathItem != null) {
                 dataCategoryElem.addContent(new Element("FullPath").setText(pathItem.getFullPath()));
             }
         }
 
         public void addParent() {
             if (dataCategory.getDataCategory() != null) {
                 dataCategoryElem.addContent(new Element("ParentUid").setText(dataCategory.getDataCategory().getUid()));
                 dataCategoryElem.addContent(new Element("ParentWikiName").setText(dataCategory.getDataCategory().getWikiName()));
             }
         }
 
         public void addAudit() {
             dataCategoryElem.setAttribute("status", dataCategory.getStatus().getName());
             dataCategoryElem.setAttribute("created", FMT.print(dataCategory.getCreated().getTime()));
             dataCategoryElem.setAttribute("modified", FMT.print(dataCategory.getModified().getTime()));
         }
 
         public void addAuthority() {
             dataCategoryElem.addContent(new Element("Authority").setText(dataCategory.getAuthority()));
         }
 
         public void addWikiDoc() {
             dataCategoryElem.addContent(new Element("WikiDoc").setText(dataCategory.getWikiDoc()));
         }
 
         public void addProvenance() {
             dataCategoryElem.addContent(new Element("Provenance").setText(dataCategory.getProvenance()));
         }
 
         public void addItemDefinition(ItemDefinition itemDefinition) {
             Element e = new Element("ItemDefinition");
             dataCategoryElem.addContent(e);
             e.setAttribute("uid", itemDefinition.getUid());
             e.addContent(new Element("Name").setText(itemDefinition.getName()));
         }
 
         public void startTags() {
             tagsElem = new Element("Tags");
            rootElem.addContent(tagsElem);
         }
 
         public void newTag(Tag tag) {
             Element tagElem = new Element("Tag");
             tagsElem.addContent(tagElem);
             tagElem.addContent(new Element("Tag").setText(tag.getTag()));
         }
 
         public Element getDataCategoryElement() {
             return dataCategoryElem;
         }
 
         public Document getDocument() {
             return new Document(rootElem);
         }
     }
 }
