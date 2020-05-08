 package edu.hawaii.systemh.frontend.components.panel;
 
 import java.util.List;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.ajax.AbstractAjaxTimerBehavior;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.behavior.SimpleAttributeModifier;
 import org.apache.wicket.markup.html.CSSPackageResource;
 import org.apache.wicket.markup.html.IHeaderContributor;
 import org.apache.wicket.markup.html.IHeaderResponse;
 import org.apache.wicket.markup.html.JavascriptPackageResource;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.markup.html.list.ListItem;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.markup.html.panel.Panel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.util.time.Duration;
 import edu.hawaii.systemh.frontend.SolarDecathlonApplication;
 import edu.hawaii.systemh.frontend.components.image.DynamicImage;
 import edu.hawaii.systemh.frontend.page.Header;
 
 /**
  * Defines the Systems' Status panel on the bottom-right side of the webpage.
  * 
  * @author Bret K. Ikehara
  */
 public class SystemPanel extends Panel implements IHeaderContributor {
 
   /**
    * System Status values.
    */
   public enum SystemHStatus {
     /** System OK. */
     OK,
 
     /** System getting close to bad. */
     CAUTION,
 
     /** Something wrong with the system. */
     WARNING
   };
 
   private static final String ORB_OK;
   private static final String ORB_CAUTION;
   private static final String ORB_WARNING;
 
   private static final ResourceReference REF_ORB_OK;
   private static final ResourceReference REF_ORB_CAUTION;
   private static final ResourceReference REF_ORB_WARNING;
 
   /**
    * Initialize static values.
    */
   static {
     ORB_OK = "images/icons/ball_green.png";
     ORB_CAUTION = "images/icons/ball_yellow.png";
     ORB_WARNING = "images/icons/ball_red.png";
 
     REF_ORB_OK = new ResourceReference(Header.class, ORB_OK);
     REF_ORB_CAUTION = new ResourceReference(Header.class, ORB_CAUTION);
     REF_ORB_WARNING = new ResourceReference(Header.class, ORB_WARNING);
   }
 
   private int i;
 
   /**
    * Serial ID.
    */
   private static final long serialVersionUID = -4395753710453042233L;
 
   /**
    * Default Constructor defining status model.
    * 
    * @param id String
    * @param list List<SystemStatusObj>
    */
   public SystemPanel(String id, List<SystemPanelListObj> list) {
     super(id);
     super.add(CSSPackageResource
         .getHeaderContribution(edu.hawaii.systemh.frontend.components.panel.SystemPanel.class,
             "SystemPanel.css", "screen"));
     super.add(JavascriptPackageResource.getHeaderContribution(
         edu.hawaii.systemh.frontend.components.panel.SystemPanel.class, "SystemPanel.js"));
     super.add(CSSPackageResource
         .getHeaderContribution(edu.hawaii.systemh.frontend.components.panel.SystemPanel.class,
             "tooltip/stylesheets/tipsy.css", "screen"));
     super.add(JavascriptPackageResource.getHeaderContribution(
        edu.hawaii.systemh.frontend.components.panel.SystemPanel.class, "tooltip/javascripts/jquery.tipsy.js"));
     
     final WebMarkupContainer panel;
     final WebMarkupContainer content;
     final DynamicImage img;
 
     // Creates the panel object. This component's HTML ID is used by jQuery to create the panel
     // object.
     panel = new WebMarkupContainer("system-panel");
     panel.setOutputMarkupId(true);
     panel.setMarkupId(id);
     add(panel);
 
     // This container is needed for the Ajax update. ListView does not allow Ajax updates.
     content = new WebMarkupContainer("system-content");
     content.setOutputMarkupId(true);
     panel.add(content);
 
     // Populate the list
     content.add(new ListView<SystemPanelListObj>("system-list", list) {
 
       /**
        * Serial ID.
        */
       private static final long serialVersionUID = -5119857624396832441L;
 
       /**
        * Populates the system status panel.
        * 
        * @param item ListItem<SystemStatusObj>
        */
       @Override
       protected void populateItem(ListItem<SystemPanelListObj> item) {
         final SystemPanelListObj obj = item.getModelObject();
 
         // Create the link
         Link<SystemPanelListObj> link = new Link<SystemPanelListObj>("system-link") {
 
           /**
            * Serial ID.
            */
           private static final long serialVersionUID = 920138749897234234L;
 
           /**
            * Goes to this page.
            */
           @Override
           public void onClick() {
             setResponsePage(obj.getLinkClass());
           }
         };
         item.add(link);
 
         // Create the link's label
         link.add(new Label("system-link-label", obj.getLinkLabel()));
 
         // Add the image.
         DynamicImage img = new DynamicImage("system-image", obj.getModel());
         item.add(img);
       }
     });
 
     img = new DynamicImage("img", new Model<ResourceReference>() {
 
       /**
        * Serial ID.
        */
       private static final long serialVersionUID = -4576972639648586901L;
 
       /**
        * Gets this model.
        */
       @Override
       public ResourceReference getObject() {
 
         // put logic here
         if (SolarDecathlonApplication.getAquaponics().getAquaponicsStatus()
             .equals(SystemHStatus.WARNING)
             || SolarDecathlonApplication.getElectrical().getEnergyStatus()
                 .equals(SystemHStatus.WARNING)
             || SolarDecathlonApplication.getHvac().getHvacStatus().equals(SystemHStatus.WARNING)) {
           return getStatusImage(SystemHStatus.WARNING);
         }
         else if (SolarDecathlonApplication.getAquaponics().getAquaponicsStatus()
             .equals(SystemHStatus.CAUTION)
             || SolarDecathlonApplication.getElectrical().getEnergyStatus()
                 .equals(SystemHStatus.CAUTION)
             || SolarDecathlonApplication.getHvac().getHvacStatus().equals(SystemHStatus.CAUTION)) {
           return getStatusImage(SystemHStatus.CAUTION);
         }
         else {
           return getStatusImage(SystemHStatus.OK);
         }
       }
     });
     panel.add(img);
     img.add(new SimpleAttributeModifier("original-title","Testing tooltip."));
 
     // Adds the update event to the panel
     panel.add(new AbstractAjaxTimerBehavior(Duration.seconds(5)) {
 
       /**
        * Serial ID.
        */
       private static final long serialVersionUID = 8435594041503623184L;
 
       /**
        * Updates the panel component. All components need to have a model set in order for the item
        * to be dynamically updated.
        */
       @Override
       protected void onTimer(AjaxRequestTarget target) {
         i++;
         target.addComponent(img);
         target.addComponent(content);
       }
       
     });
   }
 
   /**
    * Gets this status image.
    * 
    * @param status SystemStatus
    * @return String
    */
   public static ResourceReference getStatusImage(SystemHStatus status) {
 
     return (SystemHStatus.OK == status) ? REF_ORB_OK
         : (SystemHStatus.CAUTION == status) ? REF_ORB_CAUTION
             : (SystemHStatus.WARNING == status) ? REF_ORB_WARNING : null;
   }
 
   /**
    * Renders the javascript on the page.
    * 
    * @param response IHeaderResponse
    */
   @Override
   public void renderHead(IHeaderResponse response) {
 
     String id = this.get("system-panel").getMarkupId();
 
     response.renderOnDomReadyJavascript("$('#" + id + "').panel();");
   }
 }
