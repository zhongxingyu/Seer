 /**
 *
  */
 package kwitches.text.hyperlink.sbm;
 
 /**
  * @author voidy21
 *
  */
 public abstract class SBMLinkAbstract {

     public abstract String getLinkUrl();
     public abstract String getImageLinkUrl();
     public String getDomString(String url) {
        return String.format("<a href='%s' target='_blank'><img src='%s' /></a>",
             this.getLinkUrl() + url, this.getImageLinkUrl() + url);
     }
 }
