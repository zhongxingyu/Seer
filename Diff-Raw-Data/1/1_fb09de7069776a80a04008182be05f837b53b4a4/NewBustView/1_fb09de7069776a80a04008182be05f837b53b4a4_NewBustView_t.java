 package vahdin.view;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.UUID;
 
 import vahdin.VahdinUI;
 import vahdin.VahdinUI.LoginEvent;
 import vahdin.VahdinUI.LoginListener;
 import vahdin.component.GoogleMap;
 import vahdin.component.ImageUpload;
 import vahdin.data.Bust;
 import vahdin.data.User;
 
 import com.vaadin.data.util.sqlcontainer.query.QueryDelegate;
 import com.vaadin.data.util.sqlcontainer.query.QueryDelegate.RowIdChangeEvent;
 import com.vaadin.navigator.View;
 import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.CustomLayout;
 import com.vaadin.ui.DateField;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Notification;
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
     private Double latitude = null;
     private Double longitude = null;
 
     private final VahdinUI ui = (VahdinUI) UI.getCurrent();
 
     private final TextField title = new TextField();
     private final TextArea description = new TextArea();
     final String tempImgId = "b" + UUID.randomUUID().toString();
     private final Upload up = new ImageUpload().createImageUpload(tempImgId);
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
 
         final User user = ui.getCurrentUser();
         final String userId = user.getUserId();
 
         lat.setReadOnly(true);
         lon.setReadOnly(true);
 
         date.setDateFormat("dd.MM.yyyy");
         date.setValue(new Date());
 
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
                 if (name.isEmpty() || desc.isEmpty() || time == null
                         || latitude == null || longitude == null) {
                     Notification
                             .show("A sign of wisdom and maturity is when you come to terms with the realization that your decisions cause your rewards and consequences. You are responsible for your life, and your ultimate success depends on the choices you make.");
                     return;
                 }
 
                 Bust bust = new Bust(name, desc, time, latitude, longitude,
                         markId, userId);
                user.reload();
                 user.addExperience(1);
                 try {
                     bust.save();
                 } catch (SQLException e) {
                     e.printStackTrace();
                 }
 
                 Bust.addIdChangeListener(new QueryDelegate.RowIdChangeListener() {
                     @Override
                     public void rowIdChange(RowIdChangeEvent event) {
                         System.out.println("HERE");
                         try {
                             String basePath = System.getProperty("user.home")
                                     + "/contentimgs/";
                             File imgDirectory = new File(basePath);
                             String tempFilename = null;
                             if (imgDirectory.isDirectory()) {
                                 String filenames[] = imgDirectory.list();
                                 for (int i = 0; i < filenames.length; i++) {
                                     if (filenames[i].contains(tempImgId)) {
                                         tempFilename = filenames[i];
                                         break;
                                     }
                                 }
                             }
 
                             String tempImgPath = basePath + tempFilename;
                             System.out.println("TempImgPath: " + tempImgPath);
 
                             if (new File(tempImgPath).exists()) {
                                 String[] fileType = tempImgPath.split("\\.");
                                 String finalImgPath = basePath + "b"
                                         + event.getNewRowId() + "."
                                         + fileType[fileType.length - 1];
                                 File image = new File(tempImgPath);
 
                                 if (image.renameTo(new File(finalImgPath))) {
                                     System.out
                                             .println("File renamed successfully!");
                                 } else {
                                     System.out
                                             .println("Failed to rename image!");
                                 }
                             }
 
                         } catch (Exception e) {
                             e.printStackTrace();
                         }
                         Bust.removeIdChangeListener(this);
                     }
                 });
 
                 try {
                     Bust.commit();
                     User.commit();
                     user.reload();
                 } catch (SQLException e) {
                     e.printStackTrace();
                 }
 
                 Notification.show("Created new Bust with title: "
                         + title.getValue());
                 UI.getCurrent().getNavigator()
                         .navigateTo("busts/" + markId + "/");
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
