 package foo;
 
 import com.vaadin.addon.jpacontainer.JPAContainer;
 import com.vaadin.data.util.filter.Compare.Equal;
 import com.vaadin.server.VaadinService;
 import com.vaadin.shared.ui.label.ContentMode;
 import com.vaadin.ui.Button;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 import com.vaadin.ui.HorizontalLayout;
 import com.vaadin.ui.Label;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.Table;
 import com.vaadin.ui.VerticalLayout;
 import com.vaadin.ui.Window;
 
 import foo.domain.Activity;
 import foo.domain.Activity.Thumb;
 import foo.domain.MyEvent;
 import foo.domain.User;
 
 @SuppressWarnings("serial")
 public class EventWindow extends Window {
 
     private JPAContainer<MyEvent> events;
     private MyEvent myEvent;
     private JPAContainer<Activity> activities;
     private Table tblActivities;
     private Label lblUserList;
 
     public EventWindow(MyEvent myEvent, JPAContainer<Activity> activities,
             JPAContainer<MyEvent> events) {
         super("Event: " + myEvent.getName());
         this.myEvent = myEvent;
         this.activities = activities;
         this.events = events;
         setHeight("500px");
         setWidth("300px");
         setResizable(true);
 
         setContent(initContents());
     }
 
     /**
      * Build the window contents here
      */
     private Panel initContents() {
         Panel p = new Panel();
         VerticalLayout v = new VerticalLayout();
 
         Label lblEventDescription = new Label("<b>Event Description</b>");
         lblEventDescription.setContentMode(ContentMode.HTML);
         v.addComponent(lblEventDescription);
 
         Label hrLabel = new Label("<hr>");
         Label hrLabel2 = new Label("<hr>");
         hrLabel.setContentMode(ContentMode.HTML);
         hrLabel2.setContentMode(ContentMode.HTML);
 
         v.addComponent(hrLabel);
 
         // format the string here...
         String creatorDescribes = "The event creator, " + "<b>"
                 + myEvent.getCreator().getName() + "</b>"
                + " describes the event as:";
         String eventDescription = myEvent.getDescription();
 
         // blockquote? this might be horribly wrong
         String finalString = creatorDescribes + "<blockquote>"
                 + eventDescription + "</blockquote>";
 
         Label lblEventDescContent = new Label(finalString);
         lblEventDescContent.setContentMode(ContentMode.HTML);
         v.addComponent(lblEventDescContent);
 
         lblUserList = createUserListLabel();
         v.addComponent(lblUserList);
 
         v.addComponent(hrLabel2);
 
         // table for showing the activities related to the event
         tblActivities = new Table("Activities", activities);
         updateTables();
         tblActivities.setSelectable(true);
         tblActivities.setWidth("100%");
 
         // shrunk table to contents
         tblActivities.setPageLength(0);
 
         v.addComponent(tblActivities);
 
         // buttons for activity voting
         v.addComponent(initActivityButtonPanel());
 
         // buttons for joining/leaving
         v.addComponent(initEventButtonPanel());
 
         p.setContent(v);
 
         return p;
     }
 
     public Label createUserListLabel() {
         // build userlist from the set and format the string
         String userlist = "Current participants: ";
         for (User s : myEvent.getPartisipants()) {
             userlist += "<b>" + s.getName() + "</b>" + ", ";
         }
         // drop the last comma and whitespace
         userlist = userlist.substring(0, userlist.length() - 2) + ".";
 
         Label lbl = new Label(userlist, ContentMode.HTML);
         return lbl;
     }
 
     public void updateUserListLabel() {
         // build userlist from the set and format the string
         String userlist = "Current participants: ";
         for (User s : myEvent.getPartisipants()) {
             userlist += "<b>" + s.getName() + "</b>" + ", ";
         }
         // drop the last comma and whitespace
         userlist = userlist.substring(0, userlist.length() - 2) + ".";
         lblUserList.setValue(userlist);
     }
 
     private void updateTables() {
         events.refresh();
         activities.refresh();
         activities.removeAllContainerFilters();
         activities.addContainerFilter(new Equal("event", myEvent));
         activities.applyFilters();
         tblActivities.setContainerDataSource(tblActivities
                 .getContainerDataSource());
         tblActivities.setVisibleColumns(new String[] { "name", "votes",
                 "creator" });
     }
 
     // buttons for joining/leaving
     private Panel initEventButtonPanel() {
         Panel p = new Panel();
         HorizontalLayout h = new HorizontalLayout();
 
         Button btnJoinEvent = new Button("Join this event!");
         btnJoinEvent.addClickListener(new ClickListener() {
             User currentUser;
 
             @Override
             public void buttonClick(ClickEvent event) {
                 // get current user
                 try {
                     currentUser = (User) getCurrentUser();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 if (currentUser != null) {
                     myEvent.addPartisipant(currentUser);
                     updateUserListLabel();
                     events.addEntity(myEvent);
                     events.commit();
                     updateTables();
                 }
             }
         });
         h.addComponent(btnJoinEvent);
 
         Button btnLeaveEvent = new Button("Leave this event");
         btnLeaveEvent.addClickListener(new ClickListener() {
             User currentUser;
 
             @Override
             public void buttonClick(ClickEvent event) {
                 // get current user
                 try {
                     currentUser = (User) getCurrentUser();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 if (currentUser != null) {
                     myEvent.getPartisipants().remove(currentUser);
                     updateUserListLabel();
                     events.addEntity(myEvent);
                     events.commit();
                     updateTables();
                 }
             }
         });
         h.addComponent(btnLeaveEvent);
 
         p.setContent(h);
         return p;
     }
 
     // up/down vote buttons for selected activity
     private Panel initActivityButtonPanel() {
         Panel p = new Panel();
         HorizontalLayout h = new HorizontalLayout();
 
         Button btnUpVote = new Button("Good idea");
         btnUpVote.addClickListener(new ClickListener() {
             Object tblIndex;
             Activity selectedActivity;
             User currentUser;
 
             @Override
             public void buttonClick(ClickEvent event) {
                 try {
                     tblIndex = tblActivities.getValue();
                     selectedActivity = activities.getItem(tblIndex).getEntity();
                     // get current user
                     currentUser = (User) getCurrentUser();
 
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 // check that user exists, activity is selected and user is part
                 // of the event
                 if (currentUser != null && selectedActivity != null
                         && myEvent.getPartisipants().contains(currentUser)) {
                     selectedActivity.vote(Thumb.UP, currentUser);
                     // add user to list of people who have voted for this
                     // activity
                     selectedActivity.getVoters().add(currentUser);
                     activities.addEntity(selectedActivity);
                     activities.commit();
                     events.commit();
                     updateTables();
                 }
             }
 
         });
         h.addComponent(btnUpVote);
 
         Button btnDownVote = new Button("Bad idea");
         btnDownVote.addClickListener(new ClickListener() {
             Object tblIndex;
             Activity selectedActivity;
             User currentUser;
 
             @Override
             public void buttonClick(ClickEvent event) {
                 try {
                     tblIndex = tblActivities.getValue();
                     selectedActivity = activities.getItem(tblIndex).getEntity();
                     // get current user
                     currentUser = (User) getCurrentUser();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                 // check that user exists, activity is selected and user is part
                 // of the event
                 if (currentUser != null && selectedActivity != null
                         && myEvent.getPartisipants().contains(currentUser)) {
                     selectedActivity.vote(Thumb.DOWN, currentUser);
                     // add user to list of people who have voted for this
                     // activity
                     selectedActivity.getVoters().add(currentUser);
                     activities.addEntity(selectedActivity);
                     activities.commit();
                     events.commit();
                     updateTables();
                 }
             }
         });
 
         h.addComponent(btnDownVote);
 
         p.setContent(h);
         return p;
     }
 
     private Object getCurrentUser() {
         return VaadinService.getCurrentRequest().getWrappedSession()
                 .getAttribute("user");
     }
 
 }
