 package controller;
 
 import com.uoy.sb.Utils;
 import java.io.File;
 import java.util.LinkedList;
 import java.util.List;
 import model.*;
 import org.jdom2.Element;
 import org.w3c.dom.Document;
 
 public class DataController {
 
     /**
      * List of all the adults in the system
      */
     public LinkedList<Adult> Adults;
    
     /**
      * List of all the children in the system
      */
     public LinkedList<Child> Childrens;
    
     /**
      * List of all the stories in the system
      */
     public LinkedList<Story> Stories;
 
     public DataController() {
         this.init();
     }
 
     /**
      * Load all data from XML database into appropriate list
      * @author Y0239881
      */
     private void init() {
         Adults = new LinkedList<>();
         Childrens = new LinkedList<>();
         Stories = new LinkedList<>();
 
         Element root = new XMLReader().readXMLFile(Utils.CommonVariables.DATABASE_NAME);
         Element node = null;
 
         List secondLvlNodes = root.getChildren();
 
         for (int i = 0; i < secondLvlNodes.size(); i++) {
             node = (Element) secondLvlNodes.get(i);
 
             List tmpList = null;
             Element tmpNode = null;
 
             switch (node.getName()) {
                 case Utils.CommonVariables.CHILDREN_NODE:
                     tmpList = node.getChildren(Utils.CommonVariables.CHILD_SINGLE_NODE);
 
                     for (int j = 0; j < tmpList.size(); j++) {
                         tmpNode = (Element) tmpList.get(j);
 
                         Childrens.add(new Child(tmpNode.getChildText(Utils.CommonVariables.USER_NAME), tmpNode.getChildText(Utils.CommonVariables.USER_PASSWORD),
                                 tmpNode.getChildText(Utils.CommonVariables.CHILD_IMAGE), Integer.parseInt(tmpNode.getChildText(Utils.CommonVariables.CHILD_AGE))));
                     }
 
                     break;
                 case Utils.CommonVariables.ADULTS_NODE:
                     tmpList = node.getChildren(Utils.CommonVariables.ADULT_SINGLE_NODE);
 
                     for (int j = 0; j < tmpList.size(); j++) {
                         tmpNode = (Element) tmpList.get(j);
 
                         Adults.add(new Adult(tmpNode.getChildText(Utils.CommonVariables.USER_NAME), tmpNode.getChildText(Utils.CommonVariables.USER_PASSWORD)));
                     }
                     break;
                 case Utils.CommonVariables.STORIES_NODE:
                     tmpList = node.getChildren(Utils.CommonVariables.STORY_SINGLE_NODE);
 
                     for (int j = 0; j < tmpList.size(); j++) {
                         tmpNode = (Element) tmpList.get(j);
 
                         Story story = new Story();
                         story.setBackgroundColor(tmpNode.getChildText(Utils.CommonVariables.STORY_BG_COLOR));
                         story.setFont(tmpNode.getChildText(Utils.CommonVariables.STORY_FONT));
                         story.setFontSize(Integer.parseInt(tmpNode.getChildText(Utils.CommonVariables.STORY_FONT_SIZE)));
                         story.setTextColor(tmpNode.getChildText(Utils.CommonVariables.STORY_TEXT_COLOR));
                         story.setTitle(tmpNode.getChildText(Utils.CommonVariables.STORY_TITLE));
 
                         Stories.add(story);
                     }
 
                     break;
             }
         }
 
 
 //        Element secondLvlNodes = root.getChild("children");
 
 //        
 //        
 //        secondLvlNodes = root.getChild("stories");
 //        list = secondLvlNodes.getChildren("story");
 //
 //        for (int i = 0; i < list.size(); i++) {
 //            node = (Element) list.get(i);
 //
 //            Childrens.add(new Child(node.getChildText(Utils.CommonVariables.CHILD_NAME),
 //                    node.getChildText(Utils.CommonVariables.CHILD_PASSWORD), node.getChildText(Utils.CommonVariables.CHILD_IMAGE)));
 //        }
     }
 
     /**
      *
      */
     public static void saveData() {
     }
 
     /**
      *
      * @param doc
      * @param file
      */
     public static void saveXml(Document doc, File file) {
     }
 }
