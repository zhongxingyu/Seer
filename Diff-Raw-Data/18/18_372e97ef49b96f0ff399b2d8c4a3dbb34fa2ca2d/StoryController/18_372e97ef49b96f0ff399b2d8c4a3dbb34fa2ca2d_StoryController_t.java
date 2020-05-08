 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package controller;
 
 import com.uoy.sb.Common;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import model.Page;
 import model.Story;
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.output.Format;
 import org.jdom2.output.XMLOutputter;
 
 /**
  *
  * @author Y0239881
  */
 public class StoryController {
 
     private XMLParser parser = null;
 
     public StoryController() {
         parser = new XMLParser();
     }
 // get all the stories from xml file
 
     public LinkedList<Story> getAllStories() {
         String query = "//story";
 
         List<Element> elements = (List<Element>) parser.getElements(query);
 
         return buildList(elements);
     }
 // get all the stories for the logged in user 
 
     public LinkedList<Story> getStoriesByUser(String username) {
         String query = XpathBuilder.GetElementsByAttrNameAndValue(Common.Variables.STORY_SINGLE_NODE, Common.Variables.STORY_CREATED_BY, username);
 
         List<Element> elements = (List<Element>) parser.getElements(query);
 
         return buildList(elements);
     }
 
     private LinkedList<Story> buildList(List<Element> elements) {
         LinkedList<Story> stories = new LinkedList<>();
 
         if (elements != null && elements.size() > 0) {
             for (Element e : elements) {
                 if (e != null) {
                     stories.add(getStory(e));
                 }
             }
         }
 
         return stories;
     }
 
 //    public LinkedList<Page> getAllPagesByStory(int storyID) {
 //        LinkedList<Page> allPages = new LinkedList<>();
 //        String query = XpathBuilder.GetElementsByAttrNameAndValue(Common.Variables.STORY_SINGLE_NODE, Common.Variables.STORY_ID, String.valueOf(storyID));
 //
 //        System.out.println(query);
 //
 //        Element elm = parser.getElement(query);
 //
 //        if (elm != null) {
 //            Element pagesContainer = elm.getChild(Common.Variables.PAGES_NODE);
 //
 //            List<Element> pages = pagesContainer.getChildren(Common.Variables.PAGE_SINGLE_NODE);
 //
 //            if (pages != null && pages.size() > 0) {
 //                for (Element p : pages) {
 //                    allPages.add(new Page(p.getChildText(Common.Variables.PAGE_CONTENT)));
 //                }
 //            }
 //        }
 //
 //        return allPages;
 //    }
     /*
      * Create a new story and save to the database
      */
     public void createNewStory(Story s) {
         Document objDoc = parser.getDocument();
         Element story = new Element(Common.Variables.STORY_SINGLE_NODE);
 
        story.addContent(new Element(Common.Variables.STORY_TITLE).setText(s.getTitle()));
        story.addContent(new Element(Common.Variables.STORY_BG_COLOR).setText(s.getBackgroundColor()));
        story.addContent(new Element(Common.Variables.STORY_FONT_SIZE).setText(String.valueOf(s.getFontSize())));
        story.addContent(new Element(Common.Variables.STORY_FONT).setText(s.getFont()));
        story.addContent(new Element(Common.Variables.STORY_TEXT_COLOR).setText(s.getTextColor()));
 
         Element pages = new Element(Common.Variables.PAGES_NODE);
         Element page = null;
 
         if (s.getPages().size() > 0) {
             for (Page p : s.getPages()) {
                 page = new Element(Common.Variables.PAGE_SINGLE_NODE);
 
                page.addContent(new Element(Common.Variables.PAGE_CONTENT).setText(p.getText()));
                page.addContent(new Element(Common.Variables.PAGE_SOUND).setText(p.getSound()));
 
                 Element images = new Element(Common.Variables.PAGE_IMAGES);
 
                 if (p.getImg().size() > 0) {
                     for (int i = 0; i < p.getImg().size(); i++) {
                        images.addContent(new Element(Common.Variables.PAGE_IMAGE).setText(p.getImg().get(i)));
                     }
                 }
 
                 page.addContent(images);
             }
 
             story.addContent(pages);
         }
         
         Element parent = parser.getElement("//stories");
         parent.addContent(story);
 
         // create new output writer to update back to file
         XMLOutputter xmlOutput = new XMLOutputter();
 
         // display nice nice
         xmlOutput.setFormat(Format.getPrettyFormat());
         try {
             xmlOutput.output(objDoc, new FileWriter(Common.Variables.DATABASE_NAME));
 
             // print out
 //                xmlOutput.output(objDoc, System.out);
         } catch (IOException ex) {
             Logger.getLogger(StoryController.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         System.out.println("Story created!");
     }
 
     /*
      * update the xml file with the updated content
      * 
      */
     public void updateStory(Story storyToUpdate) {
         Document objDoc = parser.getDocument();
 
         Element story = getStoryElement(storyToUpdate.getId());
 
         if (story != null) {
             // update the story
             story.getChild(Common.Variables.STORY_TITLE).setText(storyToUpdate.getTitle());
 //            story.getChild(Common.Variables.STORY_TITLE).setText("test");
             story.getChild(Common.Variables.STORY_BG_COLOR).setText(storyToUpdate.getBackgroundColor());
             story.getChild(Common.Variables.STORY_FONT).setText(storyToUpdate.getFont());
             story.getChild(Common.Variables.STORY_FONT_SIZE).setText(String.valueOf(storyToUpdate.getFontSize()));
             story.getChild(Common.Variables.STORY_TEXT_COLOR).setText(storyToUpdate.getTextColor());
 
             // update pages
             Element pagesContainer = story.getChild(Common.Variables.PAGES_NODE);
 
             List<Element> pageElements = pagesContainer.getChildren(Common.Variables.PAGE_SINGLE_NODE);
             LinkedList<Page> pages = storyToUpdate.getPages();
 
             if (pageElements.size() == pages.size()) {
                 Element e = null;
                 Page p = null;
 
                 for (int i = 0; i < pageElements.size(); i++) {
                     e = pageElements.get(i);
                     p = pages.get(i);
 
                     e.getChild(Common.Variables.PAGE_CONTENT).setText(p.getText());
 //                    e.getChild(Common.Variables.PAGE_CONTENT).setText("123244");
                     e.getChild(Common.Variables.PAGE_IMAGE).setText(p.getBackgroundImage());
                     e.getChild(Common.Variables.PAGE_SOUND).setText(p.getSound());
 
 //                    pageElements.set(i, e);
                 }
             }
 
             // create new output writer to update back to file
             XMLOutputter xmlOutput = new XMLOutputter();
 
             // display nice nice
             xmlOutput.setFormat(Format.getPrettyFormat());
             try {
                 xmlOutput.output(objDoc, new FileWriter(Common.Variables.DATABASE_NAME));
 
                 // print out
 //                xmlOutput.output(objDoc, System.out);
             } catch (IOException ex) {
                 Logger.getLogger(StoryController.class.getName()).log(Level.SEVERE, null, ex);
             }
 
             System.out.println("Story updated!");
         }
     }
 
     /*
      * get element contents from the xml file by the story ID 
      * 
      */
     private Element getStoryElement(int StoryID) {
         String query = XpathBuilder.GetElementsByAttrNameAndValue(Common.Variables.STORY_SINGLE_NODE, Common.Variables.STORY_ID, String.valueOf(StoryID));
 
         System.out.println(query);
 
         return parser.getElement(query);
     }
 
     /*
      * Initialize new Story object form a xml story element
      */
     private Story getStory(Element storyElm) {
         Story story = null;
         LinkedList<Page> pages = new LinkedList<>();
         Page page = null;
 
         if (storyElm != null) {
             story = new Story();
 
             story.setTitle(storyElm.getChildText(Common.Variables.STORY_TITLE));
             story.setBackgroundColor(storyElm.getChildText(Common.Variables.STORY_BG_COLOR));
             story.setFont(storyElm.getChildText(Common.Variables.STORY_FONT));
             story.setFontSize(Integer.parseInt(storyElm.getChildText(Common.Variables.STORY_FONT_SIZE)));
             story.setTextColor(storyElm.getChildText(Common.Variables.STORY_TEXT_COLOR));
             story.setCreatedBy(storyElm.getAttributeValue(Common.Variables.STORY_CREATED_BY));
             story.setId(Integer.parseInt(storyElm.getAttributeValue(Common.Variables.STORY_ID)));
 
             Element pagesContainer = storyElm.getChild(Common.Variables.PAGES_NODE);
 
             /*
              * Get all the pages that belong to the story
              */
             List<Element> pageElements = pagesContainer.getChildren(Common.Variables.PAGE_SINGLE_NODE);
             if (pageElements != null && pageElements.size() > 0) {
                 for (Element e1 : pageElements) {
                     if (e1 != null) {
                         page = new Page();
 
                         page.setText(e1.getChildText(Common.Variables.PAGE_CONTENT));
 
                         pages.add(page);
                     }
                 }
             }
 
             // Then add to the list
             story.setPages(pages);
         } else {
             throw new NullPointerException("Story element is null");
         }
 
         return story;
     }
 
     /*
      * Get a Story from the database by the @param storyID
      * @param storyID ID of the story to get
      * @return Story object
      */
     public Story getStory(int storyID) {
         Element e = getStoryElement(storyID);
 
         return getStory(e);
     }
 }
