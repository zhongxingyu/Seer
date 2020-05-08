 package be.jeffreyvanmulem.brandstof.pages;
 
 import be.jeffreyvanmulem.brandstof.model.User;
 import be.jeffreyvanmulem.brandstof.service.UserService;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Jeffrey
  * Date: 6/05/12
  * Time: 23:01
  * To change this template use File | Settings | File Templates.
  */
 public class UserPage extends WebPage {
 
    @SpringBean(name = "userService")
     private UserService userService;
 
     public UserPage(final PageParameters pp) {
         Form<User> eventForm = new Form<User>("eventForm", new CompoundPropertyModel<User>(new User()));
         eventForm.add(new TextField<String>("username").setRequired(true));
 
         final WebMarkupContainer wmc = new WebMarkupContainer("listContainer");
 
         wmc.add(new ListView<User>("list", new PropertyModel<List<User>>(this, "userService.findAll")) {
 
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void populateItem(ListItem<User> item) {
                 User user = item.getModelObject();
                 item.add(new Label("username", user.getUsername()));
             }
 
         });
         wmc.setOutputMarkupId(true);
         add(wmc);
 
         eventForm.add(new AjaxSubmitLink("submit") {
             private static final long serialVersionUID = 1L;
 
             @Override
             protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                 User user = (User) form.getModelObject();
                 User newUser = new User();
                 newUser.setUsername(user.getUsername());
                 userService.save(newUser);
                 target.add(wmc);
             }
 
             @Override
             protected void onError(AjaxRequestTarget target, Form<?> form) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         });
 
 
         add(eventForm);
 
     }
 }
