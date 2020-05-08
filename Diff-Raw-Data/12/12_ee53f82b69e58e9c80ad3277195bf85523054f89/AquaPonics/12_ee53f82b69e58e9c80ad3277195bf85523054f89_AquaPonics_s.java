 package edu.hawaii.ihale.frontend;
 
 import java.text.DecimalFormat;
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.behavior.AbstractBehavior;
 import org.apache.wicket.markup.ComponentTag;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.WebMarkupContainerWithAssociatedMarkup;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.model.Model;
 
 //import edu.hawaii.ihale.api.SystemStateEntryDB;
 
 /**
  * The aquaponics page.
  * 
  * @author Noah Woodden
  * @author Kevin Leong
  * @author Anthony Kinsey
  * @author Kylan Hughes
  * @author Chuan Lun Hung
  */
 public class AquaPonics extends Header {
 
   private static final String boxTagName = "box";
   private static final String classTagName = "class";
   private static final String styleTagName = "style";
 
   private static final String ALERT_MESSAGE = "<font color=\"red\">(ALERT)</font>";
   private static final String WARNG_MESSAGE = "<font color=\"#FF9900\">(WARNG)</font>";
 
   // recommended values for aquaponics
   private static final long TEMPERATRUE_RANGE_START = 55;
   private static final long TEMPERATRUE_RANGE_END = 65;
 
   private static final double PH_RANGE_START = 6.5;
   private static final double PH_RANGE_END = 7.5;
 
  private static final double OXYGEN_RANGE_START = 1.50;
  private static final double OXYGEN_RANGE_END = 2.50;
 
   private static final Label recommendedTempLabel = new Label("RecommendedTempLabel",
       TEMPERATRUE_RANGE_START + "&deg;F - " + TEMPERATRUE_RANGE_END + "&deg;F");
 
   private static final Label recommendedPHLabel = new Label("RecommendedPHLabel", PH_RANGE_START
       + " - " + PH_RANGE_END);
 
   private static final Label recommendedOxygenLabel = new Label("RecommendedOxygenLabel",
      TEMPERATRUE_RANGE_START + " - " + TEMPERATRUE_RANGE_END);
 
   /**
    * MarkupContainer for all graphs.
    */
   WebMarkupContainer graph = new WebMarkupContainer("graphImage");
 
   static Label tempStatus = new Label("tempStatus", "");
   static Label phStatus = new Label("PhStatus", "");
   static Label oxygenStatus = new Label("OxygenStatus", "");
 
   /** Support serialization. */
   private static final long serialVersionUID = 1L;
 
   /**
    * Layout of page.
    * 
    * @throws Exception the exception.
    */
   public AquaPonics() throws Exception {
 
     DecimalFormat df = new DecimalFormat("#.##");
     Label temp =
         new Label("Temp", String.valueOf(SolarDecathlonApplication.getAquaponics().getTemp()));
     Label ph =
         new Label("PH",
             String.valueOf(df.format(SolarDecathlonApplication.getAquaponics().getPH())));
     Label oxygen =
         new Label("Oxygen", String.valueOf(df.format(SolarDecathlonApplication.getAquaponics()
             .getOxygen())));
 
     setTempStatusColor(SolarDecathlonApplication.getAquaponics().getTemp());
     setPHStatusColor(SolarDecathlonApplication.getAquaponics().getPH());
     setOxygenStatusColor(SolarDecathlonApplication.getAquaponics().getOxygen());
 
     tempStatus.setEscapeModelStrings(false);
     phStatus.setEscapeModelStrings(false);
     oxygenStatus.setEscapeModelStrings(false);
 
     // SystemStateEntryDB db = ((SolarDecathlonApplication)
     // SolarDecathlonApplication.get()).getDB();
     //
     // // Add listeners to the system. Listeners are the way the UI learns that new state has
     // // been received from some system in the house.
     // db.addSystemStateListener(((SolarDecathlonApplication) SolarDecathlonApplication.get())
     // .getAquaponicsListener());
     //
     // new BlackMagic(db);
 
     Link<String> statsButton = new Link<String>("statsButton") {
       private static final long serialVersionUID = 1L;
 
       /** Upon clicking this link, go to AquaponicsStatsPage. */
       @Override
       public void onClick() {
         try {
           setResponsePage(new AquaponicsStats());
         }
         catch (Exception e) {
           e.printStackTrace();
         }
       }
     };
 
     // Add button to switch to statistics view
     add(statsButton);
 
     WebMarkupContainerWithAssociatedMarkup tempDiv =
         new WebMarkupContainerWithAssociatedMarkup("TempDiv");
 
     WebMarkupContainerWithAssociatedMarkup tempDiv2 =
         new WebMarkupContainerWithAssociatedMarkup("TempDiv2");
 
     WebMarkupContainerWithAssociatedMarkup tempDiv3 =
         new WebMarkupContainerWithAssociatedMarkup("TempDiv3");
 
     WebMarkupContainerWithAssociatedMarkup tempDiv4 =
         new WebMarkupContainerWithAssociatedMarkup("TempDiv4");
 
     if (Long.parseLong((String) temp.getDefaultModelObject()) > TEMPERATRUE_RANGE_END
         || Long.parseLong((String) temp.getDefaultModelObject()) < TEMPERATRUE_RANGE_START) {
 
       tempDiv.add(new AbstractBehavior() {
 
         /**
          * Add attribute tags to the div.
          */
         private static final long serialVersionUID = 1L;
 
         public void onComponentTag(Component component, ComponentTag tag) {
           tag.put(classTagName, boxTagName);
           tag.put(styleTagName, "background-color:red");
         }
       });
     }
     else if (Long.parseLong((String) temp.getDefaultModelObject()) == TEMPERATRUE_RANGE_END
         || Long.parseLong((String) temp.getDefaultModelObject()) == TEMPERATRUE_RANGE_START) {
       tempDiv.add(new AbstractBehavior() {
 
         /**
          * Add attribute tags to the div.
          */
         private static final long serialVersionUID = 1L;
 
         public void onComponentTag(Component component, ComponentTag tag) {
           tag.put(classTagName, boxTagName);
           tag.put(styleTagName, "background-color:#FF9900;");
         }
       });
     }
     else {
       tempDiv.add(new AbstractBehavior() {
 
         /**
          * Add attribute tags to the div.
          */
         private static final long serialVersionUID = 1L;
 
         public void onComponentTag(Component component, ComponentTag tag) {
           tag.put(classTagName, boxTagName);
           tag.put(styleTagName, "background-color:green");
         }
       });
     }
 
     tempDiv2.add(new AbstractBehavior() {
 
       /**
        * Add attribute tags to the div.
        */
       private static final long serialVersionUID = 1L;
 
       public void onComponentTag(Component component, ComponentTag tag) {
         tag.put(classTagName, boxTagName);
         tag.put(styleTagName, "padding-bottom:0;");
       }
     });
 
     tempDiv3.add(new AbstractBehavior() {
 
       /**
        * Add attribute tags to the div.
        */
       private static final long serialVersionUID = 1L;
 
       public void onComponentTag(Component component, ComponentTag tag) {
         tag.put(classTagName, "column-three");
       }
     });
 
     tempDiv4.add(new AbstractBehavior() {
 
       /**
        * Add attribute tags to the div.
        */
       private static final long serialVersionUID = 1L;
 
       public void onComponentTag(Component component, ComponentTag tag) {
         tag.put(classTagName, "column-three-two");
       }
     });
 
     tempDiv2.add(tempDiv3);
 
     tempDiv4.add(temp);
     tempDiv4.add(tempStatus);
 
     tempDiv2.add(tempDiv4);
     tempDiv.add(tempDiv2);
     add(tempDiv);
 
     // add recommended temp range label to page
     recommendedTempLabel.setEscapeModelStrings(false);
     add(recommendedTempLabel);
 
     WebMarkupContainerWithAssociatedMarkup phInnerDiv =
         new WebMarkupContainerWithAssociatedMarkup("PhInnerDiv");
 
     WebMarkupContainerWithAssociatedMarkup phOuterDiv =
         new WebMarkupContainerWithAssociatedMarkup("PhOuterDiv");
 
     phInnerDiv.add(new AbstractBehavior() {
 
       /**
        * Add attribute tags to the div.
        */
       private static final long serialVersionUID = 1L;
 
       public void onComponentTag(Component component, ComponentTag tag) {
         tag.put(classTagName, boxTagName);
       }
     });
 
     if (Double.parseDouble((String) ph.getDefaultModelObject()) < PH_RANGE_END
         && Double.parseDouble((String) ph.getDefaultModelObject()) > PH_RANGE_START) {
       phOuterDiv.add(new AbstractBehavior() {
 
         /**
          * testing.
          */
         private static final long serialVersionUID = 1L;
 
         public void onComponentTag(Component component, ComponentTag tag) {
           tag.put(classTagName, boxTagName);
           tag.put(styleTagName, "background-color:green");
         }
       });
     }
     else if (Double.parseDouble((String) ph.getDefaultModelObject()) == PH_RANGE_END
         || Double.parseDouble((String) ph.getDefaultModelObject()) == PH_RANGE_START) {
       phOuterDiv.add(new AbstractBehavior() {
 
         /**
          * Add attribute tags to the div.
          */
         private static final long serialVersionUID = 1L;
 
         public void onComponentTag(Component component, ComponentTag tag) {
           tag.put(classTagName, boxTagName);
           tag.put(styleTagName, "background-color:#FF9900;");
         }
       });
     }
     else {
       phOuterDiv.add(new AbstractBehavior() {
 
         /**
          * Add attribute tags to the div.
          */
         private static final long serialVersionUID = 1L;
 
         public void onComponentTag(Component component, ComponentTag tag) {
           tag.put(classTagName, boxTagName);
           tag.put(styleTagName, "background-color:red");
         }
       });
     }
 
     phInnerDiv.add(ph);
     phInnerDiv.add(phStatus);
     phOuterDiv.add(phInnerDiv);
     add(phOuterDiv);
 
     // add recommended pH range label to page
     recommendedPHLabel.setEscapeModelStrings(false);
     add(recommendedPHLabel);
 
     WebMarkupContainerWithAssociatedMarkup oxygenInnerDiv =
         new WebMarkupContainerWithAssociatedMarkup("OxygenInnerDiv");
 
     WebMarkupContainerWithAssociatedMarkup oxygenOuterDiv =
         new WebMarkupContainerWithAssociatedMarkup("OxygenOuterDiv");
 
     oxygenInnerDiv.add(new AbstractBehavior() {
 
       /**
        * Add attribute tags to the div.
        */
       private static final long serialVersionUID = 1L;
 
       public void onComponentTag(Component component, ComponentTag tag) {
         tag.put(classTagName, boxTagName);
       }
     });
 
    if (Double.parseDouble((String) oxygen.getDefaultModelObject()) < OXYGEN_RANGE_START
        && Double.parseDouble((String) oxygen.getDefaultModelObject()) > OXYGEN_RANGE_END) {
       oxygenOuterDiv.add(new AbstractBehavior() {
 
         /**
          * Add attribute tags to the div.
          */
         private static final long serialVersionUID = 1L;
 
         public void onComponentTag(Component component, ComponentTag tag) {
           tag.put(classTagName, boxTagName);
           tag.put(styleTagName, "background-color:green");
         }
       });
     }
     else if (Double.parseDouble((String) oxygen.getDefaultModelObject()) == OXYGEN_RANGE_START
         || Double.parseDouble((String) oxygen.getDefaultModelObject()) == OXYGEN_RANGE_END) {
       oxygenOuterDiv.add(new AbstractBehavior() {
 
         /**
          * Add attribute tags to the div.
          */
         private static final long serialVersionUID = 1L;
 
         public void onComponentTag(Component component, ComponentTag tag) {
           tag.put(classTagName, boxTagName);
           tag.put(styleTagName, "background-color:#FF9900;");
         }
       });
     }
     else {
       oxygenOuterDiv.add(new AbstractBehavior() {
 
         /**
          * Add attribute tags to the div.
          */
         private static final long serialVersionUID = 1L;
 
         public void onComponentTag(Component component, ComponentTag tag) {
           tag.put(classTagName, boxTagName);
           tag.put(styleTagName, "background-color:red");
         }
       });
     }
 
     oxygenInnerDiv.add(oxygen);
     oxygenInnerDiv.add(oxygenStatus);
     oxygenOuterDiv.add(oxygenInnerDiv);
     add(oxygenOuterDiv);
 
     // add recommended oxygen range label to page
     recommendedOxygenLabel.setEscapeModelStrings(false);
     add(recommendedOxygenLabel);
 
     // Add Graph of Water Quality to page
     add(graph);
     displayDayGraph(graph);
 
   }
 
   /**
    * Determines which graph to display.
    * 
    * @param wmc web component to display graph in
    **/
   private void displayDayGraph(WebMarkupContainer wmc) {
     String graphURL;
     DecimalFormat df = new DecimalFormat("#");
     String tempValue = String.valueOf(SolarDecathlonApplication.getAquaponics().getTemp());
     String phValue = df.format(SolarDecathlonApplication.getAquaponics().getPH());
     String oxygenValue = df.format(SolarDecathlonApplication.getAquaponics().getOxygen());
     graphURL =
         "http://chart.apis.google.com/chart" + "?chxl=0:|Temp|pH|Oxygen" + "&chxt=x,y"
             + "&chbh=a,5,15" + "&chs=300x200" + "&cht=bvg" + "&chco=008000,FF9900"
             + "&chd=t:73,6.8,2|" + tempValue + "," + phValue + "," + oxygenValue
             + "&chdl=Recommended|Actual" + "&chdlp=t";
     wmc.add(new AttributeModifier("src", true, new Model<String>(graphURL)));
 
   }
 
   /**
    * Set the temperature label on this page.
    * 
    * @param value The temperature.
    */
   public static void setTempStatusColor(Long value) {
 
     if (value == TEMPERATRUE_RANGE_START || value == TEMPERATRUE_RANGE_END) {
       tempStatus.setDefaultModelObject(WARNG_MESSAGE);
     }
     else if (value < TEMPERATRUE_RANGE_START || value > TEMPERATRUE_RANGE_END) {
       tempStatus.setDefaultModelObject(ALERT_MESSAGE);
     }
     else {
       tempStatus.setDefaultModelObject("");
     }
   }
 
   /**
    * Set the ph value label on this page.
    * 
    * @param value The ph level.
    */
   public static void setPHStatusColor(Double value) {
 
     if (value == PH_RANGE_START || value == PH_RANGE_END) {
       phStatus.setDefaultModelObject(WARNG_MESSAGE);
     }
     else if (value < PH_RANGE_START || value > PH_RANGE_END) {
       phStatus.setDefaultModelObject(ALERT_MESSAGE);
     }
     else {
       phStatus.setDefaultModelObject("");
     }
   }
 
   /**
    * Set the electrical conductivity label on this page.
    * 
    * @param value The electrical conductivity.
    */
   public static void setOxygenStatusColor(Double value) {
     DecimalFormat df = new DecimalFormat("#.##");
 
     if ("5.50".equals(df.format(value)) || "6.00".equals(df.format(value))) {
       oxygenStatus.setDefaultModelObject(WARNG_MESSAGE);
     }
     else if (value < OXYGEN_RANGE_START || value > OXYGEN_RANGE_END) {
       oxygenStatus.setDefaultModelObject(ALERT_MESSAGE);
     }
     else {
       oxygenStatus.setDefaultModelObject("");
     }
   }
 
 }
