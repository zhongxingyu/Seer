 package uk.org.catnip.eddie;
 
 import uk.org.catnip.eddie.Detail;
 
 public class Link extends Detail {
     private String href;
     private String title;
     private String rel;
     private String hreflang;
    private String length;
    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

     public String getHref() {
         return href;
     }
 
     public void setHref(String href) {
         this.href = href;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public String getRel() {
         return rel;
     }
 
     public void setRel(String rel) {
         this.rel = rel;
     }
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("{");    
         if (this.href != null){
         sb.append("href: '" + this.href + "', ");
         }
         if (this.title != null){
         sb.append("title: '" + this.title + "', ");
         }
         if (this.rel != null){
         sb.append("rel: '" + this.rel + "', ");
         }
         if (this.getType() != null){
         sb.append("type: '" + this.getType()+ "', ");
         }
         if (this.getLanguage() != null){
         sb.append("language: '" + this.getLanguage() + "', ");
         }
         if (this.getValue() != null){
         sb.append("value: '" + this.getValue() +"'");
         }
         sb.append("}");
         return sb.toString();
     }
 
     public String getHreflang() {
         return hreflang;
     }
 
     public void setHreflang(String hreflang) {
         this.hreflang = hreflang;
     }
 }
