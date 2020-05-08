 
 
 public class Task
 {
     private String description;
     private boolean req_photo;
     private boolean req_audio;
     
     private String result_description = "No description";
     private String result_photofile = "none";
     private String result_audiofile = "none";
     
    public Task (String description = "No Description", boolean req_photo = 0, boolean req_audio = 0){
         this.description = description;
         this.req_photo = req_photo;
         this.req_audio = req_audio;
     }
     
     public String getDescription(){
         return description;
     }
     public boolean getReqPhoto(){
         return req_photo;
     }
     public boolean getReqAudio(){
         return req_audio;
     }
     public String getResDescription(){
         return result_description;
     }
     public String getResPhotoName(){
         return result_photofile;
     }
     public String getResAudioName(){
         return result_audiofile;
     }
    public void setResult(String desc, String photo = "none", String audio = "none"){
         result_description = desc;
         if (photo != "none"){
            req_photo = photo
         }
         if (audio != "none")
            req_audio = audio;
     }
 }
