 package vahdin.view;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.List;
 
 import vahdin.VahdinUI;
 import vahdin.VahdinUI.LoginEvent;
 import vahdin.VahdinUI.LoginListener;
 import vahdin.data.Bust;
 import vahdin.data.Mark;
 import vahdin.data.User;
 import vahdin.data.Vote;
 
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
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
 
 public class BustsView extends CustomLayout implements View {
 
     private int markId;
     private final LoginListener loginListener;
     private final VahdinUI ui = (VahdinUI) UI.getCurrent();
     private Button newBustButton;
 
     public BustsView() {
         super("single-mark-sidebar");
 
         loginListener = new LoginListener() {
             @Override
             public void login(LoginEvent event) {
                 User user = ui.getCurrentUser();
                 newBustButton.setVisible(user.isLoggedIn());
             }
         };
     }
 
     @Override
     public void enter(ViewChangeEvent event) {
 
         String[] s = event.getParameters().split("/");
         markId = Integer.parseInt(s[0]);
 
         final User user = ui.getCurrentUser();
 
         final Mark mark = Mark.getMarkById(markId);
         final int id = mark.getId();
         List<Bust> busts = Bust.getBustByMarkId(markId);
 
         VerticalLayout bustsList = new VerticalLayout();
 
        Label markTitle = new Label("<h2>"
                + SafeHtmlUtils.htmlEscape(mark.getTitle()) + "</h2>",
                 ContentMode.HTML);
 
         newBustButton = new Button();
         newBustButton.setStyleName("new-bust-button");
         newBustButton.setIcon(new ExternalResource(
                 "VAADIN/themes/vahdintheme/img/add-button.png"));
         newBustButton.addClickListener(new Button.ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 UI.getCurrent().getNavigator()
                         .navigateTo("newbust/" + markId + "/");
             }
         });
 
         Button back = new Button();
         back.setStyleName("go-back-button");
         back.setIcon(new ExternalResource(
                 "VAADIN/themes/vahdintheme/img/back-button.png"));
         back.addClickListener(new Button.ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 UI.getCurrent().getNavigator().navigateTo("");
             }
         });
 
         Label creationDate = new Label("<h4>" + mark.getTime() + "</h4>",
                 ContentMode.HTML);
 
         Label ownerNick = new Label(User.getUserById(mark.getUserID())
                 .getName());
         ownerNick.setStyleName("nickname");
 
         final Label markVotes = new Label((int) mark.getVoteCount() + "");
         markVotes.setStyleName("vote-count");
 
         final Button markUpvote = new Button();
         markUpvote.setStyleName("upvote");
 
         if (Vote.hasVoted(id, "Mark", user.getUserId())
                 && user.getVote(mark).getPower() > 0) {
             markUpvote.setIcon(new ExternalResource(
                     "VAADIN/themes/vahdintheme/img/up-arrow-active.png"));
         } else {
             markUpvote.setIcon(new ExternalResource(
                     "VAADIN/themes/vahdintheme/img/up-arrow.png"));
         }
 
         markUpvote.addClickListener(new Button.ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 if (!user.isGuest()) {
                     if (!Vote.hasVoted(id, "Mark", user.getUserId())) {
                         Vote vote = new Vote(user.getUserId(), id, "Mark", user
                                 .getPrestigePower());
                         try {
                             vote.save();
                             Vote.commit();
                             User.commit();
                             user.reload();
                         } catch (SQLException e) {
                             e.printStackTrace();
                         }
                         markVotes.setValue((int) mark.getVoteCount() + "");
                         markUpvote
                                 .setIcon(new ExternalResource(
                                         "VAADIN/themes/vahdintheme/img/up-arrow-active.png"));
                     }
                 } else {
                     ui.openLoginWindow();
                 }
             }
         });
 
         final Button markDownvote = new Button();
         markDownvote.setStyleName("downvote");
 
         if (Vote.hasVoted(id, "Mark", user.getUserId())
                 && user.getVote(mark).getPower() < 0) {
             markDownvote.setIcon(new ExternalResource(
                     "VAADIN/themes/vahdintheme/img/down-arrow-active.png"));
         } else {
             markDownvote.setIcon(new ExternalResource(
                     "VAADIN/themes/vahdintheme/img/down-arrow.png"));
         }
 
         markDownvote.addClickListener(new Button.ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 if (!user.isGuest()) {
                     if (!Vote.hasVoted(id, "Mark", user.getUserId())) {
                         Vote vote = new Vote(user.getUserId(), id, "Mark",
                                 -user.getPrestigePower());
                         try {
                             vote.save();
                             Vote.commit();
                             User.commit();
                             user.reload();
                         } catch (SQLException e) {
                             e.printStackTrace();
                         }
                         markVotes.setValue((int) mark.getVoteCount() + "");
                         markDownvote
                                 .setIcon(new ExternalResource(
                                         "VAADIN/themes/vahdintheme/img/down-arrow-active.png"));
                     }
                 } else {
                     ui.openLoginWindow();
                 }
             }
         });
 
         String markDescription = mark.getDescription();
         if (markDescription.length() > 310) {
             markDescription.substring(0, 309);
         }
        Label markDesc = new Label("<p>"
                + SafeHtmlUtils.htmlEscape(markDescription) + "</p>",
                 ContentMode.HTML);
         markDesc.setStyleName("mark-description");
 
         Button viewImage = new Button("View image");
         viewImage.setStyleName("view-image-button");
         viewImage.addClickListener(new Button.ClickListener() {
 
             @Override
             public void buttonClick(ClickEvent event) {
                 showImage(mark);
             }
         });
 
         for (int i = busts.size() - 1; i >= 0; i--) {
             CustomLayout layout = new CustomLayout("bust-row");
             final int bustId = busts.get(i).getId();
             final Bust bust = Bust.getBustById(bustId);
 
             // Title of the Bust
             Button title = new Button(busts.get(i).getTitle());
             title.setStyleName("mark-title");
             title.addClickListener(new Button.ClickListener() {
 
                 @Override
                 public void buttonClick(ClickEvent event) {
                     UI.getCurrent().getNavigator()
                             .navigateTo("bust/" + markId + "/" + bustId + "/");
                 }
             });
 
             final Label votes = new Label((int) bust.getVoteCount() + "");
             votes.setStyleName("vote-count");
 
             final Button upvote = new Button();
             upvote.setStyleName("upvote");
 
             if (Vote.hasVoted(bustId, "Bust", user.getUserId())
                     && user.getVote(bust).getPower() > 0) {
                 upvote.setIcon(new ExternalResource(
                         "VAADIN/themes/vahdintheme/img/up-arrow-active.png"));
             } else {
                 upvote.setIcon(new ExternalResource(
                         "VAADIN/themes/vahdintheme/img/up-arrow.png"));
             }
 
             upvote.addClickListener(new Button.ClickListener() {
 
                 @Override
                 public void buttonClick(ClickEvent event) {
                     if (!user.isGuest()) {
                         if (!Vote.hasVoted(bustId, "Bust", user.getUserId())) {
                             Vote vote = new Vote(user.getUserId(), bustId,
                                     "Bust", user.getPrestigePower());
                             try {
                                 vote.save();
                                 Vote.commit();
                                 User.commit();
                                 user.reload();
                             } catch (SQLException e) {
                                 e.printStackTrace();
                             }
                             votes.setValue(bust.getVoteCount() + "");
                             upvote.setIcon(new ExternalResource(
                                     "VAADIN/themes/vahdintheme/img/up-arrow-active.png"));
                         }
                     } else {
                         ui.openLoginWindow();
                     }
                 }
             });
 
             final Button downvote = new Button();
             downvote.setStyleName("downvote");
 
             if (Vote.hasVoted(bustId, "Bust", user.getUserId())
                     && user.getVote(bust).getPower() < 0) {
                 downvote.setIcon(new ExternalResource(
                         "VAADIN/themes/vahdintheme/img/down-arrow-active.png"));
             } else {
                 downvote.setIcon(new ExternalResource(
                         "VAADIN/themes/vahdintheme/img/down-arrow.png"));
             }
 
             downvote.addClickListener(new Button.ClickListener() {
 
                 @Override
                 public void buttonClick(ClickEvent event) {
                     if (!user.isGuest()) {
                         if (!Vote.hasVoted(bustId, "Bust", user.getUserId())) {
                             Vote vote = new Vote(user.getUserId(), bustId,
                                     "Bust", -user.getPrestigePower());
                             try {
                                 vote.save();
                                 Vote.commit();
                                 User.commit();
                                 user.reload();
                             } catch (SQLException e) {
                                 e.printStackTrace();
                             }
                             votes.setValue(bust.getVoteCount() + "");
                             downvote.setIcon(new ExternalResource(
                                     "VAADIN/themes/vahdintheme/img/down-arrow-active.png"));
                         }
                     } else {
                         ui.openLoginWindow();
                     }
                 }
             });
 
             layout.addComponent(upvote, "bust-row-upvote-arrow");
             layout.addComponent(votes, "bust-row-vote-count");
             layout.addComponent(downvote, "bust-row-downvote-arrow");
             layout.addComponent(title, "bust-row-title");
             bustsList.addComponent(layout);
         }
 
         addComponent(markVotes, "vote-count");
         addComponent(viewImage, "view-image-button");
         addComponent(markDesc, "mark-description");
         addComponent(markUpvote, "upvote-arrow");
         addComponent(markDownvote, "downvote-arrow");
         addComponent(ownerNick, "mark-submitter-nickname");
         addComponent(creationDate, "mark-creation-date");
         addComponent(markTitle, "mark-title");
         addComponent(newBustButton, "new-bust-button");
         addComponent(back, "back-button");
         addComponent(bustsList, "busts-list");
 
         loginListener.login(null); // force login actions
 
         ui.showBusts(mark);
     }
 
     @Override
     public void attach() {
         super.attach();
         ui.addLoginListener(loginListener);
     }
 
     @Override
     public void detach() {
         super.detach();
         ui.removeLoginListener(loginListener);
         ui.clearMap();
     }
 
     /*
      * Method that shows an image of a mark or bust in a new window on top of
      * the current interface.
      */
     public void showImage(Mark mark) {
         final VahdinUI ui = (VahdinUI) UI.getCurrent(); // Get main window
         final Window imagewin = new Window(); // Create the window
         imagewin.setStyleName("single-image-window"); // Set style name
         imagewin.setModal(true); // Make it modal
         VerticalLayout layout = new VerticalLayout(); // Create layout for the
                                                       // image
         Button close = new Button("Click this bar to close the image",
                 new Button.ClickListener() { // Add a close button for the image
                     public void buttonClick(ClickEvent event) { // inline
                                                                 // click-listener
                         ((UI) imagewin.getParent()).removeWindow(imagewin); // close
                                                                             // the
                                                                             // window
                                                                             // by
                                                                             // removing
                                                                             // it
                                                                             // from
                                                                             // the
                                                                             // parent
                                                                             // window
                     }
                 });
         layout.addComponent(close);
 
         String basepath = System.getProperty("user.home");
         File imgDirectory = new File(basepath + "/contentimgs");
         String lookingForFilename = "m" + mark.getId();
         String tempFilename = null;
         String finalFilename = null;
 
         if (imgDirectory.isDirectory()) { // check to make sure it is a
                                           // directory
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
             Image img = new Image(mark.getTitle(), resource);
             layout.addComponent(img);
         } else {
             Embedded img = new Embedded(mark.getTitle(), new ThemeResource(
                     "../vahdintheme/img/notfound.jpg"));
             layout.addComponent(img);
         }
 
         imagewin.setContent(layout);
         ui.addWindow(imagewin); // add modal window to main window
     }
 
 }
