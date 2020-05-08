 package vahdin.view;
 
 import java.io.File;
 import java.sql.SQLException;
 
 import vahdin.VahdinUI;
 import vahdin.VahdinUI.LoginEvent;
 import vahdin.VahdinUI.LoginListener;
 import vahdin.data.Bust;
 import vahdin.data.User;
 import vahdin.data.Vote;
 
 import com.vaadin.navigator.View;
 import com.vaadin.navigator.ViewChangeListener.ViewChangeEvent;
 import com.vaadin.server.ExternalResource;
 import com.vaadin.server.FileResource;
 import com.vaadin.server.ThemeResource;
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.CustomLayout;
 import com.vaadin.ui.Embedded;
 import com.vaadin.ui.Image;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.UI;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 public class SingleBustView extends CustomLayout implements View {
 
     private int markId;
     private int bustId;
     private Bust bust;
     private final VahdinUI ui = (VahdinUI) UI.getCurrent();
     private final LoginListener loginListener;
     private Button delete;
 
     public SingleBustView() {
         super("single-bust-sidebar");
 
         loginListener = new LoginListener() {
             @Override
             public void login(LoginEvent event) {
                 User user = ui.getCurrentUser();
                 delete.setVisible(user.isLoggedIn()
                         && (user.getUserId().equals(bust.getUserID()) || user
                                 .isAdmin()));
             }
         };
 
     }
 
     @Override
     public void enter(ViewChangeEvent event) {
 
         String[] s = event.getParameters().split("/");
         markId = Integer.parseInt(s[0]);
         bustId = Integer.parseInt(s[1]);
 
         final User user = ui.getCurrentUser();
 
         bust = Bust.getBustById(bustId);
         final int id = bust.getId();
 
        Label title = new Label("<h2>" + bust.getTitle() + "</h2>",
                 ContentMode.HTML);
         Label date = new Label("<h4>" + bust.getTime() + "</h4>",
                 ContentMode.HTML);
         Label nick = new Label(User.getUserById(bust.getUserID()).getName());
         nick.setStyleName("nickname");
 
         final Label votes = new Label((int) bust.getVoteCount() + "");
         votes.setStyleName("vote-count");
 
         final Button voteup = new Button();
         voteup.setStyleName("upvote");
 
         if (Vote.hasVoted(bustId, "Bust", user.getUserId())
                 && user.getVote(bust).getPower() > 0) {
             voteup.setIcon(new ExternalResource(
                     "VAADIN/themes/vahdintheme/img/up-arrow-active.png"));
         } else {
             voteup.setIcon(new ExternalResource(
                     "VAADIN/themes/vahdintheme/img/up-arrow.png"));
         }
 
         voteup.addClickListener(new Button.ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 if (!user.isGuest()) {
                     if (!Vote.hasVoted(bustId, "Bust", user.getUserId())) {
                         Vote vote = new Vote(user.getUserId(), id, "Bust", user
                                 .getPrestigePower());
                         try {
                             vote.save();
                             Vote.commit();
                             User.commit();
                             user.reload();
                         } catch (SQLException e) {
                             e.printStackTrace();
                         }
                         votes.setValue((int) bust.getVoteCount() + "");
                         voteup.setIcon(new ExternalResource(
                                 "VAADIN/themes/vahdintheme/img/up-arrow-active.png"));
                     }
                 } else {
                     ui.openLoginWindow();
                 }
             }
         });
 
         final Button votedown = new Button();
         votedown.setStyleName("downvote");
 
         if (Vote.hasVoted(id, "Bust", user.getUserId())
                 && user.getVote(bust).getPower() < 0) {
             votedown.setIcon(new ExternalResource(
                     "VAADIN/themes/vahdintheme/img/down-arrow-active.png"));
         } else {
             votedown.setIcon(new ExternalResource(
                     "VAADIN/themes/vahdintheme/img/down-arrow.png"));
         }
 
         votedown.addClickListener(new Button.ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 if (!user.isGuest()) {
                     if (!Vote.hasVoted(id, "Bust", user.getUserId())) {
                         Vote vote = new Vote(user.getUserId(), id, "Bust",
                                 -user.getPrestigePower());
                         try {
                             vote.save();
                             Vote.commit();
                             User.commit();
                             user.reload();
                         } catch (SQLException e) {
                             e.printStackTrace();
                         }
                         votes.setValue((int) bust.getVoteCount() + "");
                         votedown.setIcon(new ExternalResource(
                                 "VAADIN/themes/vahdintheme/img/down-arrow-active.png"));
                     }
                 } else {
                     ui.openLoginWindow();
                 }
             }
         });
 
         delete = new Button();
         delete.setStyleName("new-mark-button");
         delete.setIcon(new ExternalResource(
                 "VAADIN/themes/vahdintheme/img/delete-button.png"));
         delete.addClickListener(new Button.ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 bust.delete();
                 ui.getNavigator().navigateTo("busts/" + markId + "/");
             }
         });
 
         Button back = new Button();
         back.setStyleName("go-back-button");
         back.setIcon(new ExternalResource(
                 "VAADIN/themes/vahdintheme/img/back-button.png"));
         back.addClickListener(new Button.ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 UI.getCurrent().getNavigator()
                         .navigateTo("busts/" + markId + "/");
             }
         });
 
        Label desc = new Label("<p>" + bust.getDescription() + "</p>",
                 ContentMode.HTML);
         desc.setStyleName("mark-description");
 
         Button viewImage = new Button("View image");
         viewImage.setStyleName("view-image-button");
         viewImage.addClickListener(new Button.ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 showImage(bust);
             }
         });
 
         addComponent(title, "bust-title");
         addComponent(date, "bust-datetime");
         addComponent(nick, "bust-submitter-nickname");
         addComponent(voteup, "bust-upvote-arrow");
         addComponent(votes, "bust-vote-count");
         addComponent(votedown, "bust-downvote-arrow");
         addComponent(delete, "bust-delete-button");
         addComponent(back, "bust-back-button");
         addComponent(desc, "bust-description");
         addComponent(viewImage, "view-bust-image-button");
 
         ui.clearMap();
         ui.addMarker(bust.getLocationLat(), bust.getLocationLon());
         ui.centerMapOn(bust.getLocationLat(), bust.getLocationLon());
 
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
         ui.clearMap();
         ui.removeLoginListener(loginListener);
     }
 
     /*
      * Method that shows an image of a mark or bust in a new window on top of
      * the current interface.
      */
     public void showImage(Bust bust) {
         final VahdinUI ui = (VahdinUI) UI.getCurrent();
         final Window imagewin = new Window();
         imagewin.setStyleName("single-image-window");
         imagewin.setModal(true);
         VerticalLayout layout = new VerticalLayout();
         Button close = new Button("Click this bar to close the image",
                 new Button.ClickListener() {
                     public void buttonClick(ClickEvent event) {
                         ((UI) imagewin.getParent()).removeWindow(imagewin);
                     }
                 });
         layout.addComponent(close);
 
         String basepath = System.getProperty("user.home");
         File imgDirectory = new File(basepath + "/contentimgs");
         String lookingForFilename = "b" + bust.getId();
         String tempFilename = null;
         String finalFilename = null;
 
         if (imgDirectory.isDirectory()) {
             String filenames[] = imgDirectory.list();
             for (int i = 0; i < filenames.length; i++) {
                 if (filenames[i].contains(lookingForFilename)) {
                     tempFilename = filenames[i];
                     break;
                 }
             }
         }
 
         if (tempFilename != null) {
             finalFilename = basepath + "/contentimgs/" + tempFilename;
             FileResource resource = new FileResource(new File(finalFilename));
             Image img = new Image(bust.getTitle(), resource);
             layout.addComponent(img);
         } else {
             Embedded img = new Embedded(bust.getTitle(), new ThemeResource(
                     "../vahdintheme/img/notfound.jpg"));
             layout.addComponent(img);
         }
 
         imagewin.setContent(layout);
         ui.addWindow(imagewin);
     }
 }
