 package hu.sch.kurzuscsere.panel;
 
 import hu.sch.kurzuscsere.domain.CCRequest;
 import hu.sch.kurzuscsere.domain.Lesson;
 import hu.sch.kurzuscsere.logic.CourseManager;
 import hu.sch.kurzuscsere.logic.LessonManager;
 import hu.sch.kurzuscsere.logic.UserManager;
 import hu.sch.kurzuscsere.session.AppSession;
 import java.util.List;
 import org.apache.wicket.MarkupContainer;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.form.*;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author Kresshy
  */
 public final class CourseChangePanel extends Panel {
 
     private CCRequest changeRequest;
     private static final Logger log = LoggerFactory.getLogger(CourseChangePanel.class);
 
     public CourseChangePanel(String id) {
         super(id);
     }
 
     @Override
     public void onInitialize() {
         super.onInitialize();
 
         changeRequest = new CCRequest();
         changeRequest.getToCourses().add("");
 
         final Form changeForm = new Form("changePanel");
         add(changeForm);
 
         final MarkupContainer rowPanel = new WebMarkupContainer("rowsPanel");
         rowPanel.setOutputMarkupId(true);
         changeForm.add(rowPanel);
 
         final ListView<String> lv = new ListView<String>("rows",
                new PropertyModel<List<String>>(changeRequest, "toCourses")) {
 
             @Override
             protected void populateItem(ListItem<String> item) {
                 TextField text = new TextField("text", item.getModel());
                 item.add(text);
             }
         };
 
         rowPanel.add(lv.setReuseItems(true));
 
         AjaxButton addButton = new AjaxButton("addRow", changeForm) {
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form form) {
                 lv.getModelObject().add("");
                 if (target != null) {
                     target.addComponent(rowPanel);
                 }
             }
         };
         addButton.setDefaultFormProcessing(false);
         rowPanel.add(addButton);
 
         final ChoiceRenderer cr = new ChoiceRenderer("name", "id");
         final DropDownChoice<Lesson> ddc = new DropDownChoice<Lesson>("lessons",
                 new PropertyModel<Lesson>(changeRequest, "lesson"),
                 LessonManager.getInstance().getLessons(), cr);
         changeForm.add(ddc.setRequired(true));
         changeForm.add(new RequiredTextField("from", new PropertyModel(changeRequest, "fromCourse")));
         Button send = new Button("btn1", Model.of("Elküldés")) {
 
             @Override
             public void onSubmit() {
                 super.onSubmit();
 
                 changeRequest.setUser(UserManager.getInstance().getUserById(getUserId()));
                 changeRequest.setStatus(CCRequest.Status.New);
 
                 CourseManager.getInstance().insertRequest(changeRequest);
                 log.warn(changeRequest.toString());
 
             }
         };
 
         changeForm.add(send);
     }
 
     @Override
     public boolean isVisible() {
         Long userId = getUserId();
         return userId != null && userId > 0;
     }
 
     private Long getUserId() {
         return ((AppSession) getSession()).getUserId();
     }
 }
