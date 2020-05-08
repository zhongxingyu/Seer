 package com.home.giraffe.objects.Jive;
 
 public class JiveObject {
     private String id;
     private String summary;
     private String displayName;
     private String objectType;
     private JiveImage image;
     private String updated;
 
     public String getDisplayName() {
         return displayName;
     }
 
     public String getId() {
         return id;
     }
 
     public String getSummary() {
         return summary;
     }
 
     public JiveImage getImage() {
         return image;
     }
 
     public String getUpdated() {
         return updated;
     }
 
     public JiveTypes getType() {
        if(objectType == null){
            return JiveTypes.Unknown;
        }

         if (objectType.equals("jive:message")){
             return JiveTypes.JiveMessage;
         }
 
         if (objectType.equals("jive:comment")){
             return JiveTypes.JiveComment;
         }
 
         if (objectType.equals("jive:instance")){
             return JiveTypes.JiveInstance;
         }
 
         if (objectType.equals("jive:group")){
             return JiveTypes.JiveGroup;
         }
 
         if (objectType.equals("jive:person")){
             return JiveTypes.JivePerson;
         }
 
         if (objectType.equals("jive:discussion")){
             return JiveTypes.JiveDiscussion;
         }
 
         if (objectType.equals("jive:file")){
             return JiveTypes.JiveFile;
         }
 
         if (objectType.equals("jive:document")){
             return JiveTypes.JiveDocument;
         }
 
         if (objectType.equals("jive:space")){
             return JiveTypes.JiveSpace;
         }
 
         if (objectType.equals("jive:project")){
             return JiveTypes.JiveProject;
         }
 
         if (objectType.equals("jive:level")){
             return JiveTypes.JiveLevel;
         }
 
         if (objectType.equals("jive:task")){
             return JiveTypes.JiveTask;
         }
 
         if (objectType.equals("jive:poll")){
             return JiveTypes.JivePoll;
         }
 
         if (objectType.equals("jive:post")){
             return JiveTypes.JivePost;
         }
 
         return JiveTypes.Unsupported;
     }
 }
 
