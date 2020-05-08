 package com.gris.ege.other;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.Context;
 import android.util.Xml;
 
 public class TasksParser
 {
     private static final String TAG="TasksParser";
 
 
 
     public ArrayList<Task> parse(Context aContext)
     {
         ArrayList<Task> res=null;
 
         try
         {
             String aFileName=GlobalData.PATH_ON_SD_CARD+GlobalData.selectedLesson.getId()+".xml";
 
             if (new File(aFileName).exists())
             {
                 InputStream in=new FileInputStream(aFileName);
 
                 XmlPullParser aParser=Xml.newPullParser();
                 aParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                 aParser.setInput(in, null);
 
                 aParser.nextTag();
 
                 res=readTasks(aParser);
 
                 in.close();
             }
         }
         catch (Exception e)
         {
             Log.i(TAG, "Error during xml parsing from SD Card. Using standard list", e);
         }
 
         try
         {
             InputStream in=aContext.getAssets().open(GlobalData.selectedLesson.getId()+".xml");
 
             XmlPullParser aParser=Xml.newPullParser();
             aParser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
             aParser.setInput(in, null);
 
             aParser.nextTag();
 
             res=readTasks(aParser);
 
             in.close();
         }
         catch (Exception e)
         {
             Log.e(TAG, "Error during xml parsing", e);
         }
 
         return res;
     }
 
     private ArrayList<Task> readTasks(XmlPullParser aParser) throws XmlPullParserException, IOException
     {
         ArrayList<Task> res=new ArrayList<Task>();
 
         aParser.require(XmlPullParser.START_TAG, null, "tasks");
 
         while (aParser.next()!=XmlPullParser.END_TAG)
         {
             if (aParser.getEventType()!=XmlPullParser.START_TAG)
             {
                 continue;
             }
 
             String aTagName = aParser.getName();
 
             if (aTagName.equals("task"))
             {
                 Task aTask=readTask(aParser);
 
                 if (aTask.getId()!=res.size())
                 {
                     Log.e(TAG, "Task with id "+String.valueOf(aTask.getId())+" should have id "+String.valueOf(res.size()));
                    aTask.setId(res.size());
                 }
 
                 res.add(aTask);
             }
             else
             {
                 skip(aParser);
             }
         }
 
         return res;
     }
 
     private Task readTask(XmlPullParser aParser) throws XmlPullParserException, IOException
     {
         aParser.require(XmlPullParser.START_TAG, null, "task");
 
         String aIdStr    = aParser.getAttributeValue(null, "id");
         String aCategory = aParser.getAttributeValue(null, "category");
         String aAnswer   = aParser.getAttributeValue(null, "answer");
 
         aParser.nextTag();
         aParser.require(XmlPullParser.END_TAG, null, "task");
 
         int aId=Integer.parseInt(aIdStr);
 
         return new Task(aId, aCategory, aAnswer);
     }
 
     private void skip(XmlPullParser aParser) throws XmlPullParserException, IOException
     {
         if (aParser.getEventType()!=XmlPullParser.START_TAG)
         {
             throw new IllegalStateException();
         }
 
         int aDepth=1;
 
         while (aDepth!=0)
         {
             switch (aParser.next())
             {
                 case XmlPullParser.END_TAG:
                     aDepth--;
                 break;
                 case XmlPullParser.START_TAG:
                     aDepth++;
                 break;
             }
         }
     }
 }
