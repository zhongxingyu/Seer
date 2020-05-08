 package edu.hawaii.ihale.frontend.page.hvac;
 
 //import java.util.ArrayList;
 //import java.util.List;
 //import java.util.ArrayList;
 //import java.util.List;
 import org.apache.wicket.Component;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.ajax.markup.html.form.AjaxButton;
 import org.apache.wicket.behavior.AbstractBehavior;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.TextField;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleCommandType;
 import edu.hawaii.ihale.api.ApiDictionary.IHaleSystem;
 //import edu.hawaii.ihale.backend.IHaleBackend;
 import edu.hawaii.ihale.frontend.SolarDecathlonApplication;
 import edu.hawaii.ihale.frontend.SolarDecathlonSession;
 import edu.hawaii.ihale.frontend.page.Header;
 
 /**
  * The temperature(Hvac) page.
  * 
  * @author Noah Woodden
  * @author Kevin Leong
  * @author Anthony Kinsey
  * @author Kylan Hughes
  * @author Chuan Lun Hung
  */
 public class Hvac extends Header {
 
   /** Support serialization. */
   private static final long serialVersionUID = 1L;
 
   // desired room temperature range
   private static final long TEMPERATURE_RANGE_START = 60L;
   private static final long TEMPERATURE_RANGE_END = 80L;
 
   // for validating user's input for setTemp
   // don't want them perform duplicate doCommand with the same temperature.
   private int desiredTemp = SolarDecathlonApplication.getHvac().getTemp();
   private int setTemp = SolarDecathlonApplication.getHvac().getTemp();
 
   // feedback to user after they setTemp, failed or successful
   private Label feedback;
   // textfield for setTemp
   private TextField<String> airTemp;
 
   // values (attributes) for the on off hvac button
   private String buttonLabel = "Activate HVAC";
   private String buttonClass = "green-button right";
   private String buttonColor = "background-color:green";
 
   // the on off message to the right of the button.
   private Label hvacState = new Label("hvacState", "<font color=\"red\">OFF</font>");
   // to keep track of the state of hvac button
   private boolean hvacOn = false;
 
   /**
    * The temperature(Hvac) page.
    * 
    * @throws Exception the Exception
    */
   public Hvac() throws Exception {
 
     ((SolarDecathlonSession) getSession()).getHeaderSession().setActiveTab(4);
 
     // model for inside temperature label
     Model<String> insideTempModel = new Model<String>() {
 
       private static final long serialVersionUID = 1L;
 
       /**
        * Override the getObject for dynamic programming and change the text color according to the
        * temperature value.
        */
       @Override
       public String getObject() {
         long value = SolarDecathlonApplication.getHvac().getTemp();
         String original = value + "&deg;F";
         String closeTag = "</font>";
         if (value > TEMPERATURE_RANGE_START && value < TEMPERATURE_RANGE_END) {
           original = "<font color=\"green\">" + original + closeTag;
         }
         else if (value == TEMPERATURE_RANGE_START || value == TEMPERATURE_RANGE_END) {
           original = "<font color=\"#FF9900\">" + original + closeTag;
         }
         else {
           original = "<font color=\"red\">" + original + closeTag;
         }
 
         return original;
       }
     };
     Label insideTemperature = new Label("InsideTemperature", insideTempModel);
 
     // model for outside temperature label
     Model<String> outsideModel = new Model<String>() {
       private static final long serialVersionUID = 1L;
 
       @Override
       public String getObject() {
         return String.valueOf(currentWeather.getTempF() + "&deg;F");
       }
     };
     Label outsideTemperature = new Label("OutsideTemperature", outsideModel);
 
     // clear feedback each time the page is refreshed.
     feedback = new Label("Feedback", "");
 
     // the on off button for hvac
     Link<String> onOffButton = new Link<String>("button") {
       private static final long serialVersionUID = 1L;
 
       @Override
       /**
        * Turn the Hvac on or off, change button color and label.
        */
       public void onClick() {
 
         // change hvac state and button attributes.
         hvacOn = !hvacOn;
         if (hvacOn) {
           setButtonLabel("Deactivate HVAC");
           setButtonClass("red-button right");
           setButtonColor("background-color:red");
           hvacState.setDefaultModelObject("<font color=\"green\">ON</font>");
 
         }
         else {
           setButtonLabel("Activate HVAC");
           setButtonClass("green-button right");
           setButtonColor("background-color:green");
           hvacState.setDefaultModelObject("<font color=\"red\">OFF</font>");
         }
       }
 
     };
     // add the button value. e.g. Activate HVAC / Deactivate HVAC
     onOffButton.add(new Label("buttonLabel", new PropertyModel<String>(this, "buttonLabel"))
         .setEscapeModelStrings(false));
 
     // add some coponent tags to the button.
     onOffButton.add(new AbstractBehavior() {
 
       // support serialization
       private static final long serialVersionUID = 1L;
 
       public void onComponentTag(Component component, ComponentTag tag) {
         tag.put("class", buttonClass);
         tag.put("style", buttonColor);
       }
     });
 
     // add button to the page.
     add(onOffButton);
 
     hvacState.setEscapeModelStrings(false);
     // add hvac state label to the page.
     add(hvacState);
 
     // set label for inside temp
     // insideTemperature = new Label("InsideTemperature", insideTempLabel);
     insideTemperature.setEscapeModelStrings(false);
     // set label for outside temp
     outsideTemperature.setEscapeModelStrings(false);
     // add labels to the page
     add(insideTemperature);
     add(outsideTemperature);
 
     Form<String> form = new Form<String>("form");
 
     // Add the control for the air temp slider
     airTemp =
         new TextField<String>("airTemperature", new Model<String>(setTemp + "&deg;F"));
 
     // Added for jquery control.
     airTemp.setMarkupId(airTemp.getId());
     airTemp.add(new AjaxFormComponentUpdatingBehavior("onchange") {
 
       /**
        * Serial ID.
        */
       private static final long serialVersionUID = 1L;
 
       /**
        * Updates the model when the value is changed on screen.
        */
       @Override
       protected void onUpdate(AjaxRequestTarget target) {
         setTemp = Integer.valueOf(airTemp.getValue().substring(0, airTemp.getValue().length() - 2));
         System.out.println("onUpdate setTemp: " + setTemp);
       }
     });
 
     // airTemp.setOutputMarkupId(true);
     form.add(airTemp);
     form.add(new AjaxButton("SubmitTemp") {
 
       // support serializable
       private static final long serialVersionUID = 1L;
 
       /** Provide user feeback after they set a new desired temperature */
       @Override
       protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
         if (setTemp == desiredTemp) {
           feedback.setDefaultModelObject("<font color=\"#FF9900\">Unnecessary Change:<br />"
               + "Same as the original desired temperature (" + desiredTemp + "&deg;F)</font>");
           // target.addComponent(textField);
           target.addComponent(feedback);
           return;
         }
         else {
           desiredTemp = setTemp;
 
           IHaleSystem system = IHaleSystem.HVAC;
           IHaleCommandType command = IHaleCommandType.SET_TEMPERATURE;
           Integer newTemperature = setTemp;
           SolarDecathlonApplication.getBackend().doCommand(system, null, command, newTemperature);
 
           feedback.setDefaultModelObject("<font color=\"green\">"
               + "Success:<br />Desired room temperature is now " + desiredTemp + "&deg;F</font>");
         }
         // target.addComponent(textField);
         target.addComponent(feedback);
       }
     });
 
     add(form);
     form.setOutputMarkupId(true);
 
     feedback.setEscapeModelStrings(false);
     feedback.setOutputMarkupId(true);
     add(feedback);
 
     // temporary images yet to be replaced.
     add(new Image("tempY", new ResourceReference(Header.class, "images/tempY.png")));
     add(new Image("tempM", new ResourceReference(Header.class, "images/tempM.png")));
     add(new Image("tempW", new ResourceReference(Header.class, "images/tempW.png")));
     add(new Image("tempD", new ResourceReference(Header.class, "images/tempD.png")));
 
   }
 
   /**
    * Set the button label.
    * 
    * @param label The label.
    */
   public void setButtonLabel(String label) {
     this.buttonLabel = label;
   }
 
   /**
    * Get the button label. For PropertyModel to access.
    * 
    * @return The label.
    */
   public String getButtonLabel() {
     return this.buttonLabel;
   }
 
   /**
    * Get the button class attribute. For PropertyModel to access.
    * 
    * @return The button class attribute.
    */
   public String getButtonClass() {
     return this.buttonClass;
   }
 
   /**
    * Set the button class attribute.
    * 
    * @param newClass The new class attribute.
    */
   public void setButtonClass(String newClass) {
     this.buttonClass = newClass;
   }
 
   /**
    * Get the button background color. For PropertyModel to access.
    * 
    * @return The button background color.
    */
   public String getButtonColor() {
     return this.buttonColor;
   }
 
   /**
    * Set the button background color.
    * 
    * @param newColor The new color.
    */
   public void setButtonColor(String newColor) {
     this.buttonColor = newColor;
   }
 
 }
