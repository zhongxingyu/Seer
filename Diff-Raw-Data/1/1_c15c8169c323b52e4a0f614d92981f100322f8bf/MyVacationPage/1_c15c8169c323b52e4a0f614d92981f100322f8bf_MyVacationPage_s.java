 package de.urlaubr.webapp.page.myvacation;
 
 import de.urlaubr.webapp.Client;
 import de.urlaubr.webapp.components.ByteArrayImage;
 import de.urlaubr.webapp.page.SecuredPage;
 import de.urlaubr.ws.domain.Booking;
 import de.urlaubr.ws.domain.Vacation;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.markup.html.AjaxLink;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.model.AbstractReadOnlyModel;
 import org.apache.wicket.model.CompoundPropertyModel;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.request.resource.PackageResourceReference;
 import webresources.ImportResourceLocator;
 
 import java.util.List;
 
 /**
  * @author Patrick Groß-Holtwick
  *         Date: 28.08.13
  */
 public class MyVacationPage extends SecuredPage {
 
     public MyVacationPage() {
         super();
         add(new Image("logo", new PackageResourceReference(ImportResourceLocator.class, "images/urlaubr3.png")));
         add(new AjaxLink("logout") {
             @Override
             public void onClick(AjaxRequestTarget target) {
                 Client.logout(getSessionKey());
                 setResponsePage(getApplication().getHomePage());
             }
         });
         IModel<List<Booking>> model = new AbstractReadOnlyModel<List<Booking>>() {
             @Override
             public List<Booking> getObject() {
                 return Client.getMyVacation(getSessionKey());
             }
         };
         add(new ListView<Booking>("topsellerList", model) {
 
             @Override
             protected void populateItem(ListItem<Booking> item) {
                 final CompoundPropertyModel<Booking> model = new CompoundPropertyModel<Booking>(item.getModel());
                 item.add(new Label("title", model.<String>bind("vacation.title")));
                 item.add(new Label("persons", new AbstractReadOnlyModel<Integer>() {
                     @Override
                     public Integer getObject() {
                         if (model.getObject().getTraveler() != null) {
                             return model.getObject().getTraveler().length;
                         }
                         return 0;
                     }
                 }));
                 item.add(new Label("state", model.<String>bind("state")));
                 item.add(new ByteArrayImage("image", model.<byte[]>bind("vacation.image")));
             }
         });
     }
 }
