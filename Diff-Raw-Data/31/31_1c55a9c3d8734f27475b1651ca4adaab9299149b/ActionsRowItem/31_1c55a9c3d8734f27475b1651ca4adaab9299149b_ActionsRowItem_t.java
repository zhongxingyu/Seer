 package ar.com.nivel7.kernelgesturesbuilder;
 
 public class ActionsRowItem {
 	private int imageId;
     private String title;
  
    public ActionsRowItem(int imageId, String title) {
         this.imageId = imageId;
         this.title = title;
     }
     public int getImageId() {
         return imageId;
     }
     public void setImageId(int imageId) {
         this.imageId = imageId;
     }
     public String getTitle() {
         return title;
     }
     public void setTitle(String title) {
         this.title = title;
     }
     @Override
     public String toString() {
        return title;
     }
 }
 
