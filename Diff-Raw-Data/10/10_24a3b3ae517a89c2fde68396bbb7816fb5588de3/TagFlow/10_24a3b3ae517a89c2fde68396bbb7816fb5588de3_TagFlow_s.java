 package com.teamdev.projects.test.web.businessflow;
 
 import com.teamdev.projects.test.web.page.AppPage;
 import com.teamdev.projects.test.web.component.modaldialog.TagItem;
 
 /**
  * @author Alexander Orlov
  */
 public class TagFlow {
 
     private AppPage appPage;
     private String tagName;
     private TagItem tagItem;
 
     public TagFlow(String tagName, AppPage appPage){
         this.tagName = tagName;
         this.appPage = appPage;
         tagItem = appPage.modalDialog().tag(tagName);
     }
 
     public TagFlow(String parentTagName, String tagName, AppPage appPage){
         this.tagName = tagName;
         this.appPage = appPage;
         tagItem = appPage.modalDialog().tag(parentTagName).childTag(tagName);
     }
 
     public boolean isPresented(){
         return tagItem.wrapper().isPresent();
     }
 
     public String getTagTitleName(){
//        System.out.println(tagItem.title().getText());
         return tagItem.title().getText();
     }
 
     public void remove(){
         tagItem.title()
                 .hoverOverAndClickButton(tagItem.removeTagButton());
     }
 
     public TagFlow createChildTag(String childTagName){
         tagItem.title()
                 .hoverOverAndClickButton(tagItem.createTagButton());
         appPage.modalDialog().tagNameInputBox().fillIn(childTagName);
         appPage.modalDialog().tagNameInputBox().pressTabKey();
         return new TagFlow(tagName, childTagName, appPage);
     }
 
     public void rename(String newTagName){
         tagItem.title()
                 .hoverOverAndClickButton(tagItem.renameTagButton());
         appPage.modalDialog().tagNameInputBox().clearAndFill(newTagName);
         appPage.modalDialog().tagNameInputBox().pressTabKey();
         this.tagName=newTagName;
     }
 
     public void updateTagLocator(String newTagName){
         this.tagItem = appPage.modalDialog().tag(newTagName);
     }
 
 }
