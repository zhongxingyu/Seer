 package vahdin.view;
 
 import java.sql.SQLException;
 import java.util.Date;
 
 import vahdin.VahdinUI;
 import vahdin.VahdinUI.LoginEvent;
 import vahdin.VahdinUI.LoginListener;
 import vahdin.component.GoogleMap;
 import vahdin.component.ImageUpload;
 import vahdin.data.Bust;
 import vahdin.data.User;
 
 import com.vaadin.navigator.View;
 import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.CustomLayout;
 import com.vaadin.ui.DateField;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.TextArea;
 import com.vaadin.ui.TextField;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.Upload;
 
 /**
  * "New Bust" view.
  * 
  * URL format: #!newbust/{mark_id}/
  */
 public class NewBustView extends CustomLayout implements View {
 
     private int markId;
     private GoogleMap.Marker marker = null;
     private double latitude;
     private double longitude;
 
     private final VahdinUI ui = (VahdinUI) UI.getCurrent();
 
     private final TextField title = new TextField();
     private final TextArea description = new TextArea();
     private final Upload up = new ImageUpload().createImageUpload("tissit");
    private final DateField date = new DateField();
     private final Label lat = new Label();
     private final Label lon = new Label();
 
     private final Button cancel = new Button("Cancel");
     private final Button submit = new Button("Submit");
 
     private GoogleMap.ClickListener mapListener;
 
     private final LoginListener loginListener;
 
     public NewBustView() {
         super("new-bust-sidebar");
 
         loginListener = new LoginListener() {
             @Override
             public void login(LoginEvent event) {
                 User user = ui.getCurrentUser();
                 if (!user.isLoggedIn()) {
                     ui.getNavigator().navigateTo("");
                 }
             }
         };
     }
 
     @Override
     public void enter(ViewChangeEvent event) {
 
         String[] s = event.getParameters().split("/");
         markId = Integer.parseInt(s[0]);
 
         User user = ui.getCurrentUser();
         final String userId = user.getUserId();
 
         lat.setReadOnly(true);
         lon.setReadOnly(true);
 
         mapListener = new GoogleMap.ClickListener() {
             @Override
             public void click(GoogleMap.ClickEvent event) {
                 System.out.println("moving marker");
                 if (marker != null) {
                     ui.removeMarker(marker);
                 }
                 marker = ui.addMarker(event.latitude, event.longitude);
                 ui.centerMapOn(event.latitude, event.longitude);
                 lat.setValue("Lat: " + event.latitude);
                 lon.setValue("Lon: " + event.longitude);
                 latitude = event.latitude;
                 longitude = event.longitude;
             }
         };
 
         ui.addMapClickListener(mapListener);
 
         cancel.setStyleName("cancel-button");
         cancel.addClickListener(new Button.ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 UI.getCurrent().getNavigator()
                         .navigateTo("busts/" + markId + "/");
             }
         });
 
         submit.setStyleName("submit-button");
         submit.addClickListener(new Button.ClickListener() {
             @Override
             public void buttonClick(ClickEvent event) {
                 String name = title.getValue();
                 String desc = description.getValue();
                 Date time = date.getValue();
                 Bust bust = new Bust(name, desc, time, latitude, longitude,
                         markId, userId);
                 try {
                     bust.save();
                     bust.commit();
                 } catch (SQLException e) {
                     // TODO Auto-generated catch block
                     e.printStackTrace();
                 }
             }
         });
 
         addComponent(title, "new-bust-title-input");
         addComponent(description, "new-bust-desc-texarea");
         addComponent(up, "new-bust-image-input");
         addComponent(date, "new-bust-datetime-input");
         addComponent(lat, "latitude");
         addComponent(lon, "longtitude");
         addComponent(cancel, "new-bust-cancel-button");
         addComponent(submit, "new-bust-submit-button");
 
         loginListener.login(null); // force login actions
     }
 
     @Override
     public void attach() {
         super.attach();
         ui.addLoginListener(loginListener);
     }
 
     @Override
     public void detach() {
         super.detach();
         ui.removeMapClickListener(mapListener);
         if (marker != null) {
             ui.removeMarker(marker);
         }
         ui.removeLoginListener(loginListener);
     }
 }
