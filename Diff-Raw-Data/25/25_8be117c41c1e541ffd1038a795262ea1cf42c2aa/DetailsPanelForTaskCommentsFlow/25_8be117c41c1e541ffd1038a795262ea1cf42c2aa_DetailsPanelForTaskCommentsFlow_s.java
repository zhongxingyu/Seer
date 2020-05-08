 package com.teamdev.projects.test.web.businessflow.detailspanel;
 
 import com.teamdev.projects.test.web.page.AppPage;
 
 /**
  * @author Sergii Moroz
  */
 public class DetailsPanelForTaskCommentsFlow extends DetailsPanelForTaskFlow {
 
     public DetailsPanelForTaskCommentsFlow(AppPage appPage) {
         super(appPage);
     }
 
     public String getCommentsTabTitle(){
         return getAppPage().getDetailsPanelComments().commentsTabTitle().getText();
     }
 
     public String getNoCommentsNote(){
         return getAppPage().getDetailsPanelComments().noCommentsNote().getText();
     }
 
     public String getTextFromInputField(){
         return getAppPage().getDetailsPanelComments().inputCommentDefaultText().getText();
     }
 
     public void inputComment(String comment){
         getAppPage().getDetailsPanelComments().inputComment().clearAndFill(comment);
     }
 
    public String getAllComments(){
        return getAppPage().getDetailsPanelComments().commentsList().getText();
    }
 
 //    public void activateCommentsField() {
 //        appPage.getDetailsPanelComments().inputComment().click();
 //    }
 
     public void postComment(){
         getAppPage().getDetailsPanelComments().postComment().click();
     }
 
     public String getPostButtonLabel(){
         return getAppPage().getDetailsPanelComments().postCommentButtonLabel().getText();
     }
 
 
 
 
 }
