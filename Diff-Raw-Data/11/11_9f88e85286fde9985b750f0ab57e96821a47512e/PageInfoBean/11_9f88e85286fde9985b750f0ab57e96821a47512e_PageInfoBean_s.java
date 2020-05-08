 package com.mobitle.kolonsports;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 public class PageInfoBean implements java.io.Serializable {
     private int page;
     private String name;
     private String contentFileName;
     private String menuFileName;
     private String thumbnailFileName;
 
     private String menuUrl = "";
     private String contentUrl = "";
     private String mediaUrl = "";
     private String templateUrl = "";
         
     public PageInfoBean() { }
 
     public void init(HttpServletRequest request) {
         mediaUrl = request.getContextPath();
         templateUrl = "/templates";
         menuUrl = templateUrl;
         contentUrl = templateUrl;
     }
     
     public void setPage(final int page) {
         switch(page) {
         case 1:
             setValues(page, "collection", "collection", "collection_menu", "thumb");
             break;
         case 2:
             setValues(page, "backstage_1", "backstage_1", "backstage_menu", "thumb");
             break;
         case 12:
             setValues(page, "styleon", "styleon", "styleon_menu", "thumb");
             break;
         case 13:
             setValues(page, "lookbook", "lookbook", "lookbook_menu", "thumb");
             break;
         case 15:
            setValues(page, "lookbook_2", "lookbook_2", "lookbook_menu", "thumb");
             break;
         case 38:
             setValues(page, "stylepick", "stylepick", "stylepick_menu", "thumb");
             break;
         case 39:
             setValues(page, "stylepick_1", "stylepick_1", "stylepick_menu", "thumb");
             break;
         case 76:
             setValues(page, "movei_test", "collection_movie", "collection_menu", "thumb");
         default:
             setPage(1);
         }
     }
 
     private void setValues(int page, String name , String contentFileName, String menuFileName, String thumbnailFileName) {
         this.page = page;
         setName(name);
         setContentFileName(contentFileName);
         setMenuFileName(menuFileName);
         setThumbnailFileName(thumbnailFileName);
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public void setContentFileName(String filename) {
         if (filename.endsWith(".jsp"))
             this.contentFileName = filename;
         else
             this.contentFileName = filename + ".jsp";
     }
 
     public void setMenuFileName(String filename) {
         if (filename.endsWith(".jsp"))
             this.menuFileName = filename;
         else
             this.menuFileName = filename + ".jsp";
     }
 
     public void setThumbnailFileName(String filename) {
         this.thumbnailFileName = filename;
     }
 
     public int getPage() {
         return page;
     }
 
     public String getContentFileName() {
         return contentFileName;
     }
 
     public String getMenuFileName() {
         return menuFileName;
     }
 
     public String getThumbnailFileName() {
         return thumbnailFileName;
     }
 
     public String getContentUrl() {
         return contentUrl + "/" + contentFileName;
     }
 
     public String getThumbnailUrl() {
         return mediaUrl + "/images/" + thumbnailFileName;
     }
 
     public String getMenuUrl() {
         return menuUrl + "/" + menuFileName;
     }
 }
