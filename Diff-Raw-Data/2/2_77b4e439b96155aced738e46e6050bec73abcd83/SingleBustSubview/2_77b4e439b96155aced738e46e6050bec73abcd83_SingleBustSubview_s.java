 package vahdin.view;
 
 import java.util.Date;
 
 import vahdin.data.Bust;
 import vahdin.data.Mark;
 
 import com.vaadin.server.ExternalResource;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.CustomLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Notification;
 import com.vaadin.ui.UI;
 
 public class SingleBustSubview extends Subview {
 
     public SingleBustSubview() {
 
     }
 
     @Override
     public void show(String[] params) {
         if (params.length < 3) {
             UI.getCurrent().getNavigator().navigateTo("/");
         }
 
         final String markId = params[2];
 
         CustomLayout SingleBust = new CustomLayout("single-bust-sidebar");
 
         Mark m1 = new Mark("Markin nimi", new Date(), "Markin kuvaus", 1, 1);
         Bust b1 = new Bust("Bustin nimi", 1, "Bustin kuvaus", 1, "1.3.2013",
                 123.123, 456.465);
         m1.addBust(b1);
 
         Label title = new Label("<h2>" + b1.getTitle() + "</h2>",
                 Label.CONTENT_XHTML);
         Label date = new Label("<h4>" + b1.getTime() + "</h4>",
                 Label.CONTENT_XHTML);
         Label user = new Label("<h4>Riku Riski</h4>", Label.CONTENT_XHTML);
         user.setStyleName("username");
 
         Button voteup = new Button();
         voteup.setStyleName("upvote");
         voteup.setIcon(new ExternalResource(
                 "VAADIN/themes/vahdintheme/img/up-arrow.png"));
         voteup.addClickListener(new Button.ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 // TODO Auto-generated method stub
                 Notification.show("Voteup clicked");
             }
         });
 
         Label votes = new Label("2");
         votes.setStyleName("vote-count");
 
         Button votedown = new Button();
         votedown.setStyleName("downvote");
         votedown.setIcon(new ExternalResource(
                 "VAADIN/themes/vahdintheme/img/down-arrow.png"));
         votedown.addClickListener(new Button.ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 // TODO Auto-generated method stub
                 Notification.show("Votedown clicked");
             }
         });
 
         Button delete = new Button();
         delete.setStyleName("new-mark-button");
         delete.setIcon(new ExternalResource(
                 "VAADIN/themes/vahdintheme/img/delete-button.png"));
         delete.addClickListener(new Button.ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 // TODO Auto-generated method stub
                 Notification.show("Delete clicked");
             }
         });
 
         Button back = new Button();
         back.setStyleName("go-back-button");
         back.setIcon(new ExternalResource(
                 "VAADIN/themes/vahdintheme/img/back-button.png"));
         back.addClickListener(new Button.ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 UI.getCurrent().getNavigator().navigateTo("/" + markId + "/");
             }
         });
 
         Label desc = new Label("<p>" + b1.getDescription() + "</p>",
                 Label.CONTENT_XHTML);
         desc.setStyleName("mark-description");
 
         Button viewImage = new Button("View image");
         viewImage.setStyleName("view-image-button");
         viewImage.addClickListener(new Button.ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 // TODO Auto-generated method stub
                 Notification.show("View image");
             }
         });
 
         SingleBust.addComponent(title, "bust-title");
         SingleBust.addComponent(date, "bust-datetime");
         SingleBust.addComponent(user, "bust-submitter-nickname");
         SingleBust.addComponent(voteup, "bust-upvote-arrow");
        SingleBust.addComponent(votes, "vote-count");
         SingleBust.addComponent(votedown, "bust-downvote-arrow");
         SingleBust.addComponent(delete, "bust-delete-button");
         SingleBust.addComponent(back, "bust-back-button");
         SingleBust.addComponent(desc, "bust-description");
 
         setCompositionRoot(SingleBust);
         addStyleName("open");
         super.show(params);
     }
 }
