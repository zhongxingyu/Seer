 package edu.hawaii.systemh.frontend.page.lighting;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import org.apache.wicket.Component;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
 import org.apache.wicket.behavior.AbstractBehavior;
 import org.apache.wicket.markup.ComponentTag;
 //import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 //import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.HiddenField;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.PageableListView;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.util.time.Duration;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleCommandType;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleRoom;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleSystem;
 import edu.hawaii.ihale.api.command.IHaleCommand;
 import edu.hawaii.ihale.api.repository.SystemStatusMessage;
 import edu.hawaii.systemh.frontend.SolarDecathlonApplication;
 import edu.hawaii.systemh.frontend.SolarDecathlonSession;
 import edu.hawaii.systemh.frontend.page.Header;
 import edu.hawaii.systemh.frontend.page.help.Help;
 
 /**
  * The lighting page.
  * 
  * @author Noah Woodden
  * @author Kevin Leong
  * @author Anthony Kinsey
  * @author Kylan Hughes
  * @author Chuan Lun Hung
  */
 public class Lighting extends Header {
 
   /** Support serialization. */
   private static final long serialVersionUID = 1L;
 
   private boolean DEBUG = false;
 
   // String literals for pmd
   private static final String brightID = "amountBright";
   private static final String white = "#FFFFFF";
   private static final String yellowTag = "<font color=\"#FF9900\">Same: (";
   private static final String yellowTagEnd = "%)</font>";
   private static final String greenTagEnd = "%)</font>";
   private static final String greenTag = "<font color=\"green\">Success: (";
   private static final String CLASS = "class";
   private static final String LIVING_ROOM = "Living Room";
   private static final String DINING_ROOM = "Dining Room";
   private static final String KITCHEN = "Kitchen";
   private static final String BATHROOM = "Bathroom";
  private static final String ONCHANGE = "onchange";
 
   private boolean livingState;
   private boolean diningState;
   private boolean kitchenState;
   private boolean bathroomState;
 
   private String buttonOn = "green-button";
   private String buttonOff = "gray-button";
 
   private HiddenField<String> colorChange;
   private HiddenField<String> colorChangeAll;
 
   private TextField<String> intensity;
   private int setLivingIntensity;
   private int setDiningIntensity;
   private int setKitchenIntensity;
   private int setBathroomIntensity;
   private int desiredLivingIntensity = 0;
   private int desiredDiningIntensity = 0;
   private int desiredKitchenIntensity = 0;
   private int desiredBathroomIntensity = 0;
 
   // private Label colorFeedback;
   private Label intensityFeedback;
 
   private String setColorAll = white;
   private String setColor = white;
   private String desiredLivingColor = white;
   private String desiredDiningColor = white;
   private String desiredKitchenColor = white;
   private String desiredBathroomColor = white;
 
   private DropDownChoice<String> roomChoices;
   private static final List<String> rooms = Arrays.asList(new String[] { LIVING_ROOM, DINING_ROOM,
       KITCHEN, BATHROOM });
   private String currentRoom = ((SolarDecathlonSession) getSession()).getLightingSession()
       .getRoom();
 
   private Link<String> onButton;
   private Link<String> offButton;
   private Link<String> allOnButton;
   private Link<String> allOffButton;
 
   /**
    * Layout of page.
    */
   public Lighting() {
     ((SolarDecathlonSession) getSession()).getHeaderSession().setActiveTab(3);
 
     livingState = SolarDecathlonApplication.getLights().getLivingRoom().getLightingEnabled();
     diningState = SolarDecathlonApplication.getLights().getDiningRoom().getLightingEnabled();
     kitchenState = SolarDecathlonApplication.getLights().getKitchenRoom().getLightingEnabled();
     bathroomState = SolarDecathlonApplication.getLights().getBathroom().getLightingEnabled();
     Form<String> form = new Form<String>("form");
 
     // Add the control for the intensity slider
     setLivingIntensity = SolarDecathlonApplication.getLights().getLivingRoom().getLightingLevel();
     setDiningIntensity = SolarDecathlonApplication.getLights().getDiningRoom().getLightingLevel();
     setKitchenIntensity = SolarDecathlonApplication.getLights().getKitchenRoom().getLightingLevel();
     setBathroomIntensity = SolarDecathlonApplication.getLights().getBathroom().getLightingLevel();
 
     desiredLivingColor = SolarDecathlonApplication.getLights().getLivingRoom().getLightingColor();
     desiredDiningColor = SolarDecathlonApplication.getLights().getDiningRoom().getLightingColor();
     desiredKitchenColor = SolarDecathlonApplication.getLights().getKitchenRoom().getLightingColor();
     desiredBathroomColor = SolarDecathlonApplication.getLights().getBathroom().getLightingColor();
 
     // Messages
     // Add messages as a list view to each page
 
     // Get all messages applicable to this page
     List<SystemStatusMessage> msgs =
         SolarDecathlonApplication.getMessages().getMessages(IHaleSystem.LIGHTING);
 
     // Create wrapper container for pageable list view
     WebMarkupContainer systemLog = new WebMarkupContainer("LightingSystemLogContainer");
     systemLog.setOutputMarkupId(true);
 
     // Help button link
     Link<String> helpLink = new Link<String>("helpLink") {
       private static final long serialVersionUID = 1L;
 
       public void onClick() {
         ((SolarDecathlonSession) getSession()).getHelpSession().setCurrentTab(4);
         setResponsePage(Help.class);
       }
     };
 
     // Help Image
     helpLink.add(new Image("helpLighting", new ResourceReference(Header.class,
         "images/icons/help.png")));
 
     add(helpLink);
 
     // Create Listview
     PageableListView<SystemStatusMessage> listView =
         new PageableListView<SystemStatusMessage>("LightingStatusMessages", msgs, 10) {
 
           private static final long serialVersionUID = 1L;
 
           @Override
           protected void populateItem(ListItem<SystemStatusMessage> item) {
 
             SystemStatusMessage msg = item.getModelObject();
 
             // If only the empty message is in the list, then
             // display "No Messages"
             if (msg.getType() == null) {
               item.add(new Label("LightingMessageType", "-"));
               item.add(new Label("LightingTimestamp", "-"));
               item.add(new Label("LightingMessageContent", "No Messages"));
             }
             // Populate data
             else {
               item.add(new Label("LightingTimestamp", new Date(msg.getTimestamp()).toString()));
               item.add(new Label("LightingMessageType", msg.getType().toString()));
               item.add(new Label("LightingMessageContent", msg.getMessage()));
             }
           }
         };
 
     systemLog.add(listView);
     systemLog.add(new AjaxPagingNavigator("paginatorLighting", listView));
     // Update log every 5 seconds.
     systemLog.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(5)) {
       private static final long serialVersionUID = 1L;
     });
     systemLog.setVersioned(false);
     add(systemLog);
 
     // End messages section
 
     if (LIVING_ROOM.equals(currentRoom)) {
       setColor = desiredLivingColor;
       intensity = new TextField<String>(brightID, new Model<String>(setLivingIntensity + "%"));
     }
     else if (DINING_ROOM.equals(currentRoom)) {
       setColor = desiredDiningColor;
       intensity = new TextField<String>(brightID, new Model<String>(setDiningIntensity + "%"));
     }
     else if (KITCHEN.equals(currentRoom)) {
       setColor = desiredKitchenColor;
       intensity = new TextField<String>(brightID, new Model<String>(setKitchenIntensity + "%"));
     }
     else {
       setColor = desiredBathroomColor;
       intensity = new TextField<String>(brightID, new Model<String>(setBathroomIntensity + "%"));
     }
 
     // Added for jquery control.
     intensity.setMarkupId(intensity.getId());
    intensity.add(new AjaxFormComponentUpdatingBehavior(ONCHANGE) {
 
       /**
        * Serial ID.
        */
       private static final long serialVersionUID = 1L;
 
       /**
        * Updates the model when the value is changed on screen.
        */
       @Override
       protected void onUpdate(AjaxRequestTarget target) {
         if (LIVING_ROOM.equals(currentRoom)) {
           setLivingIntensity =
               Integer.valueOf(intensity.getValue().substring(0, intensity.getValue().length() - 1));
           if (DEBUG) {
             System.out.println("onUpdate setLivingIntensity: " + setLivingIntensity);
           }
         }
         else if (DINING_ROOM.equals(currentRoom)) {
           setDiningIntensity =
               Integer.valueOf(intensity.getValue().substring(0, intensity.getValue().length() - 1));
           if (DEBUG) {
             System.out.println("onUpdate setDiningIntensity: " + setDiningIntensity);
           }
         }
         else if (KITCHEN.equals(currentRoom)) {
           setKitchenIntensity =
               Integer.valueOf(intensity.getValue().substring(0, intensity.getValue().length() - 1));
           if (DEBUG) {
             System.out.println("onUpdate setKitchenIntensity: " + setKitchenIntensity);
           }
         }
         else if (BATHROOM.equals(currentRoom)) {
           setBathroomIntensity =
               Integer.valueOf(intensity.getValue().substring(0, intensity.getValue().length() - 1));
           if (DEBUG) {
             System.out.println("onUpdate setBathroomIntensity: " + setBathroomIntensity);
           }
         }
 
       }
     });
 
     // airTemp.setOutputMarkupId(true);
     form.add(intensity);
     form.add(new AjaxButton("SubmitIntensity") {
 
       // support serializable
       private static final long serialVersionUID = 1L;
 
       /** Provide user feeback after they set a new desired temperature */
       @Override
       protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
         IHaleSystem system = IHaleSystem.LIGHTING;
         IHaleCommandType command = IHaleCommandType.SET_LIGHTING_LEVEL;
         IHaleRoom room;
 
         if (LIVING_ROOM.equals(currentRoom)) {
           if (setLivingIntensity == desiredLivingIntensity) {
             intensityFeedback.setDefaultModelObject(yellowTag + desiredLivingIntensity
                 + yellowTagEnd);
             target.addComponent(intensityFeedback);
             return;
           }
           else {
             desiredLivingIntensity = setLivingIntensity;
             room = IHaleRoom.LIVING;
             SolarDecathlonApplication.getBackend().doCommand(system, room, command,
                 desiredLivingIntensity);
             if (DEBUG) {
               System.out.println("do command sent for living "
                   + "room lights intensity with level: " + desiredLivingIntensity + "%");
             }
             intensityFeedback
                 .setDefaultModelObject(greenTag + desiredLivingIntensity + greenTagEnd);
             target.addComponent(intensityFeedback);
             return;
           }
         }
         else if (DINING_ROOM.equals(currentRoom)) {
           if (setDiningIntensity == desiredDiningIntensity) {
             intensityFeedback.setDefaultModelObject(yellowTag + desiredDiningIntensity
                 + yellowTagEnd);
             target.addComponent(intensityFeedback);
             return;
           }
           else {
             desiredDiningIntensity = setDiningIntensity;
             room = IHaleRoom.DINING;
             SolarDecathlonApplication.getBackend().doCommand(system, room, command,
                 desiredDiningIntensity);
             if (DEBUG) {
               System.out.println("do command sent for dining "
                   + "room lights intensity with level: " + desiredDiningIntensity + "%");
             }
             intensityFeedback
                 .setDefaultModelObject(greenTag + desiredDiningIntensity + greenTagEnd);
             target.addComponent(intensityFeedback);
             return;
           }
         }
         else if (KITCHEN.equals(currentRoom)) {
           if (setKitchenIntensity == desiredKitchenIntensity) {
             intensityFeedback.setDefaultModelObject(yellowTag + desiredKitchenIntensity
                 + yellowTagEnd);
             target.addComponent(intensityFeedback);
             return;
           }
           else {
             desiredKitchenIntensity = setKitchenIntensity;
             room = IHaleRoom.KITCHEN;
             SolarDecathlonApplication.getBackend().doCommand(system, room, command,
                 desiredKitchenIntensity);
             if (DEBUG) {
               System.out.println("do command sent for kitchen room lights intensity with level: "
                   + desiredKitchenIntensity + "%");
             }
             intensityFeedback.setDefaultModelObject(greenTag + desiredKitchenIntensity
                 + greenTagEnd);
             target.addComponent(intensityFeedback);
             return;
           }
         }
         else if (BATHROOM.equals(currentRoom)) {
           if (setBathroomIntensity == desiredBathroomIntensity) {
             intensityFeedback.setDefaultModelObject(yellowTag + desiredBathroomIntensity
                 + yellowTagEnd);
             target.addComponent(intensityFeedback);
             return;
           }
           else {
             desiredBathroomIntensity = setBathroomIntensity;
             room = IHaleRoom.BATHROOM;
             SolarDecathlonApplication.getBackend().doCommand(system, room, command,
                 desiredBathroomIntensity);
             if (DEBUG) {
               System.out.println("do command sent for bathroom "
                   + "room lights intensity with level: " + desiredBathroomIntensity + "%");
             }
             intensityFeedback.setDefaultModelObject(greenTag + desiredBathroomIntensity
                 + greenTagEnd);
             target.addComponent(intensityFeedback);
             return;
           }
         }
       }
     });
 
     form.setOutputMarkupId(true);
 
     // the on button
     onButton = new Link<String>("OnButton") {
       private static final long serialVersionUID = 1L;
 
       @Override
       /**
        * Turn on the light in this room.
        */
       public void onClick() {
         handleRoomState(currentRoom, true);
       }
     };
     // set markup id to true for ajax update
     onButton.setOutputMarkupId(true);
 
     // the off button
     offButton = new Link<String>("OffButton") {
       private static final long serialVersionUID = 1L;
 
       @Override
       /**
        * Turn off the light in this room.
        */
       public void onClick() {
         handleRoomState(currentRoom, false);
       }
     };
     // set markup id to true for ajax update
     offButton.setOutputMarkupId(true);
 
     // the on button
     allOnButton = new Link<String>("AllOnButton") {
       private static final long serialVersionUID = 1L;
 
       @Override
       /**
        * Turn on the light in this room.
        */
       public void onClick() {
         handleRoomState(LIVING_ROOM, true);
         handleRoomState(DINING_ROOM, true);
         handleRoomState(KITCHEN, true);
         handleRoomState(BATHROOM, true);
       }
     };
 
     // the off button
     allOffButton = new Link<String>("AllOffButton") {
       private static final long serialVersionUID = 1L;
 
       @Override
       /**
        * Turn off the light in this room.
        */
       public void onClick() {
         handleRoomState(LIVING_ROOM, false);
         handleRoomState(DINING_ROOM, false);
         handleRoomState(KITCHEN, false);
         handleRoomState(BATHROOM, false);
       }
     };
 
     // set the buttons according to user's current dropdownchoice
     if (LIVING_ROOM.equals(currentRoom)) {
       setButtons(livingState);
     }
     if (DINING_ROOM.equals(currentRoom)) {
       setButtons(diningState);
     }
     if (KITCHEN.equals(currentRoom)) {
       setButtons(kitchenState);
     }
     if (BATHROOM.equals(currentRoom)) {
       setButtons(bathroomState);
     }
 
     // create feedback for intensity
     intensityFeedback = new Label("intensityfeedback", "");
     intensityFeedback.setEscapeModelStrings(false);
     intensityFeedback.setOutputMarkupId(true);
 
     colorChangeAll = new HiddenField<String>("colorchangeall", new Model<String>(setColorAll));
     // Added for jquery control.
     colorChangeAll.setMarkupId(colorChangeAll.getId());
    colorChangeAll.add(new AjaxFormComponentUpdatingBehavior(ONCHANGE) {
 
       /**
        * Serial ID.
        */
       private static final long serialVersionUID = 1L;
 
       /**
        * Updates the model when the value is changed on screen.
        */
       @Override
       protected void onUpdate(AjaxRequestTarget target) {
         setColorAll = colorChangeAll.getDefaultModelObjectAsString();
 
         IHaleSystem system = IHaleSystem.LIGHTING;
         IHaleCommandType command = IHaleCommandType.SET_LIGHTING_COLOR;
 
           desiredLivingColor = setColorAll;
           desiredDiningColor = setColorAll;
           desiredKitchenColor = setColorAll;
           desiredBathroomColor = setColorAll;
           SolarDecathlonApplication.getBackend().doCommand(system, IHaleRoom.LIVING, command,
               setColorAll);
           SolarDecathlonApplication.getBackend().doCommand(system, IHaleRoom.DINING, command,
               setColorAll);
           SolarDecathlonApplication.getBackend().doCommand(system, IHaleRoom.KITCHEN, command,
               setColorAll);
           SolarDecathlonApplication.getBackend().doCommand(system, IHaleRoom.BATHROOM, command,
               setColorAll);
         }
 
     });
     colorChange = new HiddenField<String>("colorchange", new Model<String>(setColor));
     // Added for jquery control.
     colorChange.setMarkupId(colorChange.getId());
    colorChange.add(new AjaxFormComponentUpdatingBehavior(ONCHANGE) {
 
       /**
        * Serial ID.
        */
       private static final long serialVersionUID = 1L;
 
       /**
        * Updates the model when the value is changed on screen.
        */
       @Override
       protected void onUpdate(AjaxRequestTarget target) {
         setColor = colorChange.getDefaultModelObjectAsString();
 
         IHaleSystem system = IHaleSystem.LIGHTING;
         IHaleCommandType command = IHaleCommandType.SET_LIGHTING_COLOR;
         IHaleRoom room;
 
         if (LIVING_ROOM.equals(currentRoom)) {
           desiredLivingColor = setColor;
           room = IHaleRoom.LIVING;
           SolarDecathlonApplication.getBackend().doCommand(system, room, command,
               desiredLivingColor);
           if (DEBUG) {
             System.out.println("do command sent for living room lights color with color: "
                 + desiredLivingColor);
           }
         }
         else if (DINING_ROOM.equals(currentRoom)) {
           desiredDiningColor = setColor;
           room = IHaleRoom.DINING;
           SolarDecathlonApplication.getBackend().doCommand(system, room, command,
               desiredDiningColor);
           if (DEBUG) {
             System.out.println("do command sent for dining room lights color with color: "
                 + desiredDiningColor);
           }
         }
         else if (KITCHEN.equals(currentRoom)) {
           desiredKitchenColor = setColor;
           room = IHaleRoom.KITCHEN;
           SolarDecathlonApplication.getBackend().doCommand(system, room, command,
               desiredKitchenColor);
           if (DEBUG) {
             System.out.println("do command sent for kitchen room lights color with color: "
                 + desiredKitchenColor);
           }
         }
         else if (BATHROOM.equals(currentRoom)) {
           desiredBathroomColor = setColor;
           room = IHaleRoom.BATHROOM;
           SolarDecathlonApplication.getBackend().doCommand(system, room, command,
               desiredBathroomColor);
           if (DEBUG) {
             System.out.println("do command sent for bathroom room lights color with color: "
                 + desiredBathroomColor);
           }
         }
       }
     });
 
     add(colorChange);
     add(colorChangeAll);
     form.add(intensityFeedback);
     add(form);
 
     Form<String> roomForm = new Form<String>("roomForm");
     roomChoices =
         new DropDownChoice<String>("room", new PropertyModel<String>(this, "currentRoom"), rooms);
 
    roomChoices.add(new AjaxFormComponentUpdatingBehavior(ONCHANGE) {
 
       private static final long serialVersionUID = 1L;
 
       /**
        * For when user chooses new room.
        */
       @Override
       protected void onUpdate(AjaxRequestTarget target) {
         String newSelection = roomChoices.getDefaultModelObjectAsString();
         currentRoom = newSelection;
         if (DEBUG) {
           System.out.println("new room selection: " + newSelection);
         }
         if (LIVING_ROOM.equals(newSelection)) {
           ((SolarDecathlonSession) getSession()).getLightingSession().setRoom(LIVING_ROOM);
         }
         else if (DINING_ROOM.equals(newSelection)) {
           ((SolarDecathlonSession) getSession()).getLightingSession().setRoom(DINING_ROOM);
         }
         else if (KITCHEN.equals(newSelection)) {
           ((SolarDecathlonSession) getSession()).getLightingSession().setRoom(KITCHEN);
         }
         else if (BATHROOM.equals(newSelection)) {
           ((SolarDecathlonSession) getSession()).getLightingSession().setRoom(BATHROOM);
         }
 
         // reset feedback
         intensityFeedback.setDefaultModelObject("");
         // add components in the page we want to update to the target.
         target.addComponent(intensityFeedback);
         setResponsePage(Lighting.class);
       }
     });
     roomForm.add(roomChoices.setRequired(true));
     add(roomForm);
     add(onButton);
     add(offButton);
     add(allOnButton);
     add(allOffButton);
     // add(form);
   }
 
   /**
    * Set the light switch according to the state of the selected room.
    * 
    * @param enabled Whether the lights are on.
    */
   private void setButtons(final boolean enabled) {
 
     onButton.add(new AbstractBehavior() {
       // support serialization
       private static final long serialVersionUID = 1L;
 
       public void onComponentTag(Component component, ComponentTag tag) {
         if (DEBUG) {
           System.out.println("state: " + enabled);
         }
 
         if (enabled) {
           tag.put(CLASS, buttonOn);
         }
         else {
           tag.put(CLASS, buttonOff);
         }
       }
     });
 
     offButton.add(new AbstractBehavior() {
 
       // support serialization
       private static final long serialVersionUID = 1L;
 
       public void onComponentTag(Component component, ComponentTag tag) {
         if (enabled) {
           tag.put(CLASS, buttonOff);
         }
         else {
           tag.put(CLASS, buttonOn);
         }
       }
     });
 
     allOnButton.add(new AbstractBehavior() {
       // support serialization
       private static final long serialVersionUID = 1L;
 
       public void onComponentTag(Component component, ComponentTag tag) {
         if (DEBUG) {
           System.out.println("state: " + enabled);
         }
 
         if (enabled) {
           tag.put(CLASS, buttonOn);
         }
         else {
           tag.put(CLASS, buttonOff);
         }
       }
     });
 
     allOffButton.add(new AbstractBehavior() {
 
       // support serialization
       private static final long serialVersionUID = 1L;
 
       public void onComponentTag(Component component, ComponentTag tag) {
         if (enabled) {
           tag.put(CLASS, buttonOff);
         }
         else {
           tag.put(CLASS, buttonOn);
         }
       }
     });
 
   }
 
   /**
    * Set the light switch and send a command to the house system.
    * 
    * @param roomName The room name.
    * @param enabled Whether the light is enabled.
    */
   private void handleRoomState(String roomName, boolean enabled) {
 
     IHaleCommand backend;
     backend = SolarDecathlonApplication.getBackend();
 
     if (LIVING_ROOM.equals(roomName)) {
       livingState = enabled;
       backend.doCommand(IHaleSystem.LIGHTING, IHaleRoom.LIVING,
           IHaleCommandType.SET_LIGHTING_ENABLED, enabled);
     }
     if (DINING_ROOM.equals(roomName)) {
       diningState = enabled;
       backend.doCommand(IHaleSystem.LIGHTING, IHaleRoom.DINING,
           IHaleCommandType.SET_LIGHTING_ENABLED, enabled);
     }
     if (KITCHEN.equals(roomName)) {
       kitchenState = enabled;
       backend.doCommand(IHaleSystem.LIGHTING, IHaleRoom.KITCHEN,
           IHaleCommandType.SET_LIGHTING_ENABLED, enabled);
     }
     if (BATHROOM.equals(roomName)) {
       bathroomState = enabled;
       backend.doCommand(IHaleSystem.LIGHTING, IHaleRoom.BATHROOM,
           IHaleCommandType.SET_LIGHTING_ENABLED, enabled);
     }
 
     if (DEBUG) {
       if (enabled) {
         System.out.println("Command { ON } sent to " + roomName);
       }
       else {
         System.out.println("Command {OFF} sent to " + roomName);
       }
     }
     intensityFeedback.setDefaultModelObject("");
     setButtons(enabled);
   }
 
 }
