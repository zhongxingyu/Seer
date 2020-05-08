 package lawscraper.server.components;
 
 import lawscraper.server.entities.law.Law;
 import lawscraper.server.entities.law.LawDocumentPart;
 import org.springframework.stereotype.Component;
 
 import java.util.List;
 
 /**
  * TODO Document this.
  */
 
 @Component
 public class LawRendererImpl implements LawRenderer {
     @Override
     public String renderToHtml(Law law) {
         return element("div", renderMeta(law)
                 + element("div", renderParts(law.getSortedParts()), "class", "lawBody"),
                        "class", "law");
     }
 
     private String renderParts(List<LawDocumentPart> children) {
         StringBuilder sb = new StringBuilder();
         for (LawDocumentPart child : children) {
             sb.append(renderPart(child));
         }
         return sb.toString();
     }
 
     private String renderPart(LawDocumentPart part) {
         return renderPart(part, "part_" + part.getLawPartType().name().toLowerCase());
     }
 
     private String renderPart(LawDocumentPart part, String cssClass) {
         return element("div", part.getTextElement().getText() + renderParts(part.getSortedParts()), "class", cssClass);
     }
 
     private String renderMeta(Law law) {
         return element("div", element("div", getLawTitle(law), "class", "lawTitle") +
                 getMetaPart("Departement", law.getCreator())
                + getMetaPart("Utf&auml;rdad", law.getReleaseDate())
                + getMetaPart("K&auml;lla", law.getPublisher())
                + getMetaPart("Senast h&auml;mtad", law.getLatestFetchFromGov()),
                        "class", "lawMeta"
                       );
     }
 
     private String getMetaPart(String title, String content) {
         return element("div",
                        element("div", title, "class", "metaTitle") +
                                element("div", content, "class", "metaText"),
                        "class", "metaHeading");
     }
 
     private String getLawTitle(Law law) {
         return law.getTitle() + " (" + law.getFsNumber() + ")";
     }
 
     private String element(String elementName, String content, String... attrKeysAndValues) {
         StringBuilder attributes = new StringBuilder();
         if (attrKeysAndValues.length % 2 == 0) {
             for (int i = 0; i < attrKeysAndValues.length; i++) {
                 String keyOrValue = attrKeysAndValues[i];
                 if (i % 2 == 0) {
                     attributes.append(" ").append(keyOrValue).append("=\"");
                 } else {
                     attributes.append(keyOrValue).append("\"");
                 }
             }
         }
         return "<" + elementName + attributes.toString() + ">" + content + "</" + elementName + ">";
     }
 }
