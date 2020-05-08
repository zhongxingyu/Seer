 package edu.tuhh.tagcloud.cloud;
 
 import org.mcavallo.opencloud.Cloud;
 
 /**
  * @author avelinsk
  */
 public class TagCloud {
   public static Cloud cloud;
   public static String text = "";
 
   public TagCloud(){
     cloud = new Cloud();
     text = "";
   }
 
  public Cloud getTagCloud(String content, String command){
     new TagCloud();
     // Sets the number of tag to display in the tagCloud
     cloud.setMaxTagsToDisplay(30);
 
     // We want four different levels so set the maximum weight value to 4.0.
     cloud.setMaxWeight(4.0);
 
     // Sets the default url to assign to tag.
     // The format specifier %s will be substituted by the tag name
     cloud.setDefaultLink("https://documentation.coremedia.com/servlet/content/247402?language=en&include=false&version=5.2&book=%s");
 
     if (content != null) {
             text = content;
     }
     if (command != null) {
             if (command.equalsIgnoreCase("CLEAR")) {
                     cloud.clear();
             } else if (command.equalsIgnoreCase("SEARCH")) {
                     cloud.addText(text);
                     text = "";
             }
     }
 
     return cloud;
   }
 }
