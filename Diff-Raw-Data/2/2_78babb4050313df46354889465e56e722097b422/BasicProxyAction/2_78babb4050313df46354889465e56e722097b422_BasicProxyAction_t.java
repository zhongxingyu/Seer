 package org.jetbrains.idea.plugin.gitbar.action;
 
 import com.intellij.openapi.actionSystem.*;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.ui.Messages;
 import org.jetbrains.idea.plugin.gitbar.GitBar;
 import org.jetbrains.idea.plugin.gitbar.GitUtils;
 
 import javax.swing.*;
 
 /**
  * basic proxy action
  *
  * @author linux_china@hotmail.com
  */
 public abstract class BasicProxyAction extends AnAction {
     /**
      * constructor
      */
     public BasicProxyAction() {
         super();
     }
 
     /**
      * constructor
      *
      * @param text action's text
      */
     public BasicProxyAction(String text) {
         super(text);
     }
 
     /**
      * constructor
      *
      * @param text        action text
      * @param description action description
      * @param icon        action icon
      */
     public BasicProxyAction(String text, String description, Icon icon) {
         super(text, description, icon);
     }
 
     /**
      * target action id
      *
      * @return target action id
      */
     protected abstract String getActionId();
 
     /**
      * action perform
      *
      * @param actionEvent action event
      */
     public void actionPerformed(AnActionEvent actionEvent) {
         AnAction anaction = ActionManager.getInstance().getAction(getActionId());
         if (anaction != null)
             anaction.actionPerformed(actionEvent);
     }
 
     /**
      * presentation update
      *
      * @param actionEvent action event
      */
     public void update(AnActionEvent actionEvent) {
         try {
             AnAction anaction = ActionManager.getInstance().getAction(getActionId());
             if (anaction != null)
                 anaction.beforeActionPerformedUpdate(actionEvent);
             else {
                 Project project = actionEvent.getData(DataKeys.PROJECT);
                Messages.showMessageDialog(project, "Action not found: " + getActionId(), "Git Bar error", Messages.getErrorIcon());
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         Project project = GitUtils.getProject(actionEvent);
         GitBar bar = GitUtils.getGitBar();
         Presentation presentation = actionEvent.getPresentation();
         presentation.setVisible(bar.isVisible(this, project) && (presentation.isEnabled()));
     }
 }
