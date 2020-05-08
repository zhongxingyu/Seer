 package com.thoughtworks.twu.domain;
 
 public class IdeaForm
 {
     private String sectionId;
    private String ideaText;
 
     public IdeaForm(String sectionId, String ideaText)
     {
         this.sectionId = sectionId;
         this.ideaText = ideaText;
     }
     public IdeaForm()
     {
 
     }
 
 
 
     public String getSectionId()
     {
         return sectionId;
     }
 
     public String getIdeaText()
     {
         return ideaText;
     }
 
     public void setSectionId(String sectionId)
     {
 
         this.sectionId = sectionId;
     }
 
    public void setNewIdeaText(String message)
     {
 
         ideaText = message;
     }
 
     public boolean isValid()
     {
         return true;
     }
 }
