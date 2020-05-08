 package com.gris.ege.other;
 
 public class Task
 {
     private int     mId;
     private String  mCategory;
     private String  mAnswer;
     private boolean mFinished;
 
 
 
     public Task()
     {
         mId=0;
         mCategory="";
         mAnswer="";
         mFinished=false;
     }
 
     public Task(int aId, String aCategory, String aAnswer)
     {
         mId=aId;
         mCategory=aCategory;
         mAnswer=aAnswer;
         mFinished=false;
     }
 
     public Task(int aId, String aCategory, String aAnswer, boolean aFinished)
     {
         mId=aId;
         mCategory=aCategory;
         mAnswer=aAnswer;
         mFinished=aFinished;
     }
 
     public int getId()
     {
         return mId;
     }
 
     public String getCategory()
     {
         return mCategory;
     }
 
     public String getAnswer()
     {
         return mAnswer;
     }
 
     public void setAnswer(String aAnswer)
     {
         mAnswer=aAnswer;
     }
 
     public boolean isFinished()
     {
         return mFinished;
     }
 
     public void setFinished(boolean aFinished)
     {
         mFinished=aFinished;
     }
 }
