 package org.wattdepot.visualization.page.home;
 
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.form.CheckBox;
 import org.apache.wicket.markup.html.form.ChoiceRenderer;
 import org.apache.wicket.markup.html.form.DropDownChoice;
 import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.apache.wicket.request.resource.PackageResourceReference;
 import org.wattdepot.visualization.VisualizationSession;
 import org.wattdepot.visualization.data.sensor.SensorModel;
 import org.wattdepot.visualization.data.server.ServerModel;
 import org.wattdepot.visualization.page.BasePage;
 
 /**
  * Creates the home page.
  * 
  * @author Bret K. Ikehara
  */
 public final class HomePage extends BasePage {
 
   /**
    * Serial ID.
    */
   private static final long serialVersionUID = -5869069458989539920L;
 
   private static final PackageResourceReference JS_HOME = new PackageResourceReference(
       HomePage.class, "home.js");
 
   private static final PackageResourceReference JS_TRANSMISSION = new PackageResourceReference(
       HomePage.class, "transmission.js");
 
   private Label hd, bd;
   private Form<HomePageModel> form;
 
   // defines our page variables.
   private CheckBox sensor1, sensor2;
   private DropDownChoice<HomePageChoice> select;
 
   /**
    * Define the change behavior for the update.
    */
   class ChangeBehavior extends AjaxFormComponentUpdatingBehavior {
 
     /**
      * Default Constructor.
      */
     public ChangeBehavior() {
       super("onchange");
     }
 
     /**
      * Serial ID.
      */
     private static final long serialVersionUID = -7240939017685146857L;
 
     @Override
     protected void onUpdate(AjaxRequestTarget target) {
       target.add(form);
 
       // updates the server model.
       VisualizationSession session = (VisualizationSession) form.getSession();
       ServerModel server = session.getServerModel();
       Map<String, SensorModel> map = server.getSensors();
 
       SensorModel sensor = map.get("1");
       sensor.setOnline(sensor1Switch.booleanValue());
 
       sensor = map.get("2");
       sensor.setPulse(sensor2Switch.booleanValue());
 
       sensor = map.get("3");
       int[] color = sensor.getColor();
       color[0] = choice.getValue().intValue();
     }
   };
 
   protected Boolean sensor1Switch, sensor2Switch;
   protected HomePageChoice choice;
 
   /**
    * Default Constructor.
    */
   public HomePage() {
 
     final List<HomePageChoice> list =
         Arrays.asList(new HomePageChoice[] { new HomePageChoice("Little Usage", 90),
             new HomePageChoice("Medium Usage", 50), new HomePageChoice("Extreme Usage", 0) });
    
     sensor1Switch = true;
     sensor2Switch = false;
     choice = list.get(0);
    
     hd = new Label("hd", "");
 
     bd = new Label("ft", "");
 
     form = new Form<HomePageModel>("form");
     form.setOutputMarkupId(true);
     sensor1 = new CheckBox("sensor1Switch", new PropertyModel<Boolean>(this, "sensor1Switch"));
     sensor2 = new CheckBox("sensor2Switch", new PropertyModel<Boolean>(this, "sensor2Switch"));
 
     ChoiceRenderer<HomePageChoice> choiceRenderer =
         new ChoiceRenderer<HomePageChoice>("key", "value");
     select = new DropDownChoice<HomePageChoice>("sensor3Color", list, choiceRenderer);
     select.setDefaultModel(new PropertyModel<HomePageChoice>(this, "choice"));
   }
 
   @Override
   protected void onBeforeRender() {
     super.onBeforeRender();
 
     add(hd);
 
     add(bd);
 
     add(form);
 
     // add checkbox.
     form.add(sensor1);
     sensor1.add(new ChangeBehavior());
 
     form.add(sensor2);
     sensor2.add(new ChangeBehavior());
 
     // add select button.
     form.add(select);
     select.add(new ChangeBehavior());
   }
 
   @Override
   public void renderHead(IHeaderResponse response) {
     super.renderHead(response);

     response.renderJavaScriptReference(JS_TRANSMISSION);
     response.renderJavaScriptReference(JS_HOME);
   }
 }
