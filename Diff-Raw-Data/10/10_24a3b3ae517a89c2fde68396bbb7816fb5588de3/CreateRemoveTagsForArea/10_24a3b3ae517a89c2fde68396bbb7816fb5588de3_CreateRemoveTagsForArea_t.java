 package com.teamdev.projects.test.area;
 
 import com.teamdev.projects.test.SingleAccountTest;
 import com.teamdev.projects.test.web.businessflow.AreaFlow;
 import com.teamdev.projects.test.web.businessflow.TagFlow;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.*;
 
 /**
  * @author Alexander Orlov
  */
 public class CreateRemoveTagsForArea extends SingleAccountTest {
 
     private AreaFlow areaFlow;
 
     @BeforeMethod
     public void preCondition() {
         areaFlow = generalFlow.createArea(randomArea());
         areaFlow.editTags();
     }
 
     @Test
     public void createAndRemoveTag() {
         TagFlow tag = areaFlow.addTag(tagName);
         assertTrue(tag.isPresented(), "Tag is not created immediately!");
         generalFlow.refresh();
         areaFlow.editTags();
         assertTrue(tag.isPresented(), "Tag is not displayed after a refresh!");
         tag.remove();
         assertFalse(tag.isPresented(), "Tag is not removed immediately!");
         generalFlow.refresh();
         areaFlow.editTags();
         assertFalse(tag.isPresented(), "Tag is not removed after a refresh!");
 
     }
 
     @Test
     public void createAndRemoveChildTag() {
         TagFlow tag = areaFlow.addTag(tagName);
         TagFlow childTag = tag.createChildTag(childTagName);
         assertTrue(childTag.isPresented(), "Child tag is not created immediately!");
         generalFlow.refresh();
         areaFlow.editTags();
         assertTrue(childTag.isPresented(), "Child tag is not displayed after a refresh!");
         childTag.remove();
         assertFalse(childTag.isPresented(), "Child tag is not removed immediately!");
         generalFlow.refresh();
         areaFlow.editTags();
         assertFalse(childTag.isPresented(), "Child tag is not removed after a refresh!");
     }
 
     @Test
     public void removeParentTagWhichHasChildTag() {
         TagFlow tag = areaFlow.addTag(tagName);
         TagFlow childTag = tag.createChildTag(childTagName);
         tag.remove();
         assertFalse(tag.isPresented(), "Parent tag is not removed immediately!");
         assertFalse(childTag.isPresented(), "Child tag is not removed immediately!");
         generalFlow.refresh();
         areaFlow.editTags();
         assertFalse(tag.isPresented(), "Parent tag is not removed after a refresh!");
         assertFalse(childTag.isPresented(), "Child tag is not removed after a refresh!");
 
     }
 
     @Test
     public void renameTag() {
         TagFlow tag = areaFlow.addTag(tagName);
         tag.rename(newTagName);
        assertEquals(tag.getTagTitleName(), tag.getTagTitleName(), "Tag was not renamed immediately");
         generalFlow.refresh();
         areaFlow.editTags();
         tag.updateTagLocator(newTagName);
         assertEquals(tag.getTagTitleName(), tag.getTagTitleName(), "Tag was not renamed after a refresh");
     }
 }
