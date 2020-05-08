 package org.estudy.ui.form;
 
 import org.estudy.ui.popup.UIPopupComponent;
 import org.estudy.ui.portlet.EStudyPortlet;
 import org.exoplatform.webui.config.annotation.ComponentConfig;
 import org.exoplatform.webui.config.annotation.EventConfig;
 import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
 import org.exoplatform.webui.event.Event;
 import org.exoplatform.webui.event.EventListener;
 import org.exoplatform.webui.form.UIForm;
 import org.exoplatform.webui.form.UIFormRichtextInput;
 
 /**
  * Created with IntelliJ IDEA.
  * User: tuanp
  * Date: 10/22/13
  * Time: 6:09 PM
  * To change this template use File | Settings | File Templates.
  */
 @ComponentConfig(
         lifecycle = UIFormLifecycle.class,
         template = "system:/groovy/webui/form/UIForm.gtmpl",
         events = {
                 @EventConfig(listeners = UIQuestionForm.SaveActionListener.class),
                 @EventConfig(listeners = UIQuestionForm.OnchangeActionListener.class, phase = Event.Phase.DECODE),
                 @EventConfig(listeners = UIQuestionForm.CancelActionListener.class, phase = Event.Phase.DECODE)
         }
 )
 public class UIQuestionForm extends UIForm implements UIPopupComponent {
 
 
   public UIQuestionForm(){
 
    addChild(new UIFormRichtextInput("editor", "editor", ""));
 
   }
 
   @Override
   public void activate() throws Exception {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public void deActivate() throws Exception {
     // TODO Auto-generated method stub
 
   }
 
   @Override
   public String[] getActions() {
     return new String[]{"Search","Cancel"} ;
   }
 
   static  public class SaveActionListener extends EventListener<UIQuestionForm> {
     @Override
     public void execute(Event<UIQuestionForm> event) throws Exception {
       UIQuestionForm uiForm = event.getSource() ;
     }
   }
   static  public class OnchangeActionListener extends EventListener<UIQuestionForm> {
     @Override
     public void execute(Event<UIQuestionForm> event) throws Exception {
       UIQuestionForm uiForm = event.getSource() ;
     }
   }
   static  public class CancelActionListener extends EventListener<UIQuestionForm> {
     @Override
     public void execute(Event<UIQuestionForm> event) throws Exception {
       UIQuestionForm uiForm = event.getSource() ;
       EStudyPortlet calendarPortlet = uiForm.getAncestorOfType(EStudyPortlet.class) ;
       calendarPortlet.closePopup();
     }
   }
 }
