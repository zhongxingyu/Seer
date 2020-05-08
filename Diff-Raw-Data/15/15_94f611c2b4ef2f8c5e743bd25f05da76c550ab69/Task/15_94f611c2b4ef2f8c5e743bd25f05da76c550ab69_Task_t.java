package ca.ualberta.cmput301project;

 
 
 public class Task
 {
     private String description;
     private boolean req_photo;
     private boolean req_audio;
     
     private String result_description = "No description";
     private String result_photofile = "none";
     private String result_audiofile = "none";
     
    public Task (String description, boolean req_photo, boolean req_audio){
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
    
    public void setResult(String desc, String photo, String audio){
         result_description = desc;
         if (photo != "none"){
            result_photofile = photo;
         }
         if (audio != "none")
            result_audiofile = audio;
     }
 }
