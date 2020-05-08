 package edu.hawaii.ihale.frontend.page;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.Map;
 import java.util.TimeZone;
 import org.apache.wicket.ResourceReference;
 import org.apache.wicket.ajax.AjaxRequestTarget;
 import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
 import org.apache.wicket.extensions.ajax.markup.html.modal.ModalWindow;
 import org.apache.wicket.markup.html.CSSPackageResource;
 import org.apache.wicket.markup.html.JavascriptPackageResource;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.WebPage;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.link.Link;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.util.time.Duration;
 import edu.hawaii.ihale.frontend.page.aquaponics.AquaPonics;
 import edu.hawaii.ihale.frontend.page.dashboard.Dashboard;
 import edu.hawaii.ihale.frontend.page.energy.Energy;
 import edu.hawaii.ihale.frontend.page.help.Help;
 import edu.hawaii.ihale.frontend.page.hvac.Hvac;
 import edu.hawaii.ihale.frontend.page.lighting.Lighting;
 import edu.hawaii.ihale.frontend.weatherparser.CurrentWeather;
 import edu.hawaii.ihale.frontend.weatherparser.WeatherParser;
 import edu.hawaii.ihale.frontend.SolarDecathlonSession;
 import edu.hawaii.ihale.frontend.SolarDecathlonApplication;
 
 /**
  * The header page. This is a parent class to all pages.
  * 
  * @author Noah Woodden
  * @author Kevin Leong
  * @author Anthony Kinsey
  * @author Kylan Hughes
  * @author Chuan Lun Hung
  */
 public class Header extends WebPage {
 
   // Date format for the time displayed at the top right corner.
   private static final String DATE_FORMAT = "MMMM d, yyyy  hh:mm a";
     
   /** Key value for session map to be shared among pages. **/
   //public static final String PAGE_DISPLAY = "ActivePage";
 
   // Variables to allow the active tab to change.
   //protected int activePage = -1;
   
   // Variables to find the weather forecast.
   // made public to share with dashboard.
   /** A parser for retrieving info from Google Weather API. */
   public WeatherParser weatherParser;  
   /** The current weather info to be shared with the dashboard. */
   public CurrentWeather currentWeather;
   // the parser takes the city as a parameter
   private static String cityName = "Honolulu";
   // the timer should also change for different city selection.
   private static String timeZone = "US/Hawaii";
   
   private int activeTab;
 
   
   /**
    * labels for the header, aka basepage. after the simulator and backend add outsideTemp can remove
    * this line and put it in the constructor along with insideTemperatureHeader
    */
   //private static Label outsideTemperatureHeader = new Label("OutsideTemperatureHeader", "0");
 
   /** Support serialization. */
   private static final long serialVersionUID = 1L;
 
   /**
    * The header page. This is a parent class to all pages.
    */
   public Header() {   
     
     activeTab = ((SolarDecathlonSession)getSession()).getHeaderSession().getActiveTab();
     
     //System.out.println("\n\nUSER IS VIEWING PAGE" + activeTab + "\n\n");
     
     WebMarkupContainer dashboardItem;
     WebMarkupContainer energyItem;
     WebMarkupContainer aquaponicsItem;
     WebMarkupContainer lightingItem;
     WebMarkupContainer hvacItem;
     
     // WebMarkupContainer securityItem;
     // WebMarkupContainer settingsItem;
     // WebMarkupContainer reportsItem;
     // WebMarkupContainer administratorItem;
     WebMarkupContainer helpItem;
 
     // find out the info about the current weather. (right now only supports honolulu)
     // later may have to make DropDownBox for region selection.
     weatherParser = new WeatherParser(cityName);
     currentWeather = weatherParser.getCurrentWeather();
     //outsideTemperatureHeader.setDefaultModelObject(String.valueOf(currentWeather.getTempF()));
     
     // model for current weather label
     Model<String> currentWeatherModel = new Model<String>() {
       
       private static final long serialVersionUID = 1L;
       
       /**
        * Override the getObject for dynamic programming and retrieve the current weather upon 
        * refresh.
        */
       @Override
       public String getObject() {      
         return currentWeather.getCondition() + ", " + currentWeather.getWindCondition() + " ";
       }
     };
     
     Label currentWeatherHeader = new Label("CurrentWeatherHeader", currentWeatherModel);
     
     // model for inside temperature labels
     Model<String> insideTempModel = new Model<String>() {
 
       private static final long serialVersionUID = 1L;
 
       /**
        * Override the getObject for dynamic programming and change the text color according to the
        * temperature value.
        */
       @Override
       public String getObject() {
         return String.valueOf(SolarDecathlonApplication.getHvac().getTemp());
       }
     };
 
     Label insideTemperatureHeader = new Label("InsideTemperatureHeader", insideTempModel);
 
     Model<String> outsideTempModel = new Model<String>() {
       private static final long serialVersionUID = 1L;
       
       /**
        * Override the getObject for dynamic programming and change the text color according to the
        * temperature value.
        */
       @Override
       public String getObject() {
         return String.valueOf(currentWeather.getTempF());
       }
     };
     
     Label outsideTemperatureHeader = new Label("OutsideTemperatureHeader", outsideTempModel);
 
     /*******************************************************************************************
      * for testing purpose, may remove after the integration with backend system. or just simply
      * uncomment this section to test with BlackMagic Note: after uncommenting, you have to comment
      * out the thread in the Application class and use the matching dbClassName for BlackMagic
      *******************************************************************************************/
     // try {
     // new BlackMagic(SolarDecathlonApplication.db);
     // }
     // catch (Exception e1) {
     // e1.printStackTrace();
     // }
 
     insideTemperatureHeader.setDefaultModelObject(String.valueOf(SolarDecathlonApplication
         .getHvac().getTemp()));
 
     insideTemperatureHeader.setEscapeModelStrings(false);
     outsideTemperatureHeader.setEscapeModelStrings(false);
 
     String screenContainer = "screen";
 
     // Add CSS definitions for use in all pages
     add(CSSPackageResource.getHeaderContribution(edu.hawaii.ihale.frontend.page.Header.class,
         "style.css", screenContainer));
 
     /**
      * Javascripts were done by Noah but couldn't get in contact with him.
      */
     // Add Javascript for use in all pages
     add(JavascriptPackageResource.getHeaderContribution(
         edu.hawaii.ihale.frontend.page.Header.class, "javascripts/jquery.min.js"));
     add(JavascriptPackageResource.getHeaderContribution(
         edu.hawaii.ihale.frontend.page.Header.class, "javascripts/jquery-ui.min.js"));
     /*
      * add(JavascriptPackageResource.getHeaderContribution(edu.hawaii.solardecathlon
      * .frontend.Header.* class, "javascripts/jquery-ui-1.8.7.custom.js"));
      */
     add(JavascriptPackageResource.getHeaderContribution(
         edu.hawaii.ihale.frontend.page.Header.class, "javascripts/jquery.effects.core.js"));
     add(JavascriptPackageResource.getHeaderContribution(
         edu.hawaii.ihale.frontend.page.Header.class, "javascripts/jquery.ui.core.js"));
 
     add(JavascriptPackageResource.getHeaderContribution(
         edu.hawaii.ihale.frontend.page.Header.class, "javascripts/jquery.ui.widget.js"));
     add(CSSPackageResource.getHeaderContribution(edu.hawaii.ihale.frontend.page.Header.class,
         "jqueryUI.css", screenContainer));
 
     add(JavascriptPackageResource.getHeaderContribution(
         edu.hawaii.ihale.frontend.page.Header.class, "javascripts/main.js"));
 
     // For Time Picker and Color chooser
     add(JavascriptPackageResource.getHeaderContribution(
         edu.hawaii.ihale.frontend.page.Header.class, "javascripts/jquery.icolor.js"));
     add(CSSPackageResource.getHeaderContribution(edu.hawaii.ihale.frontend.page.Header.class,
         "timePicker.css", screenContainer));
     add(JavascriptPackageResource.getHeaderContribution(
         edu.hawaii.ihale.frontend.page.Header.class, "javascripts/jquery.timePicker.js"));
 
     // For Date Picker
     add(CSSPackageResource.getHeaderContribution(edu.hawaii.ihale.frontend.page.Header.class,
         "datePicker.css", screenContainer));
     add(JavascriptPackageResource.getHeaderContribution(
         edu.hawaii.ihale.frontend.page.Header.class, "javascripts/jquery.ui.datepicker.js"));
 
     // Logo Image
     add(new Image("logo", new ResourceReference(Header.class, "images/logo.png")));
 
     // Print Image
     add(new Image("printer", new ResourceReference(Header.class, "images/icons/printer.png")));
 
     // Help Image
     add(new Image("help", new ResourceReference(Header.class, "images/icons/help.png")));
 
     // Refresh Image
     add(new Image("refresh", new ResourceReference(Header.class,
         "images/icons/arrow_rotate_clockwise.png")));
 
     // Other images used throughout system
     add(new Image("TableViewImage", new ResourceReference(Header.class,
         "images/icons/magnifier.png")));
     add(new Image("TableEditImage", 
         new ResourceReference(Header.class, "images/icons/pencil.png")));
     add(new Image("TableDeleteImage",
         new ResourceReference(Header.class, "images/icons/cancel.png")));
     add(new Label("title", "Home Management System"));
 
     // Add Dashboard Link to page (tabs)
     dashboardItem = new WebMarkupContainer("DashboardItem");
     add(dashboardItem);
     dashboardItem.add(new Link<String>("DashboardPageLinkTab") {
       private static final long serialVersionUID = 1L;
 
       /** Upon clicking this link, go to dashboard page. */
       @Override
       public void onClick() {
         setResponsePage(Dashboard.class);
       }
     });
     ((SolarDecathlonSession)getSession()).getHeaderSession().setDashboardItem(dashboardItem);
 
     // Add Energy Link to page (tabs)
     energyItem = new WebMarkupContainer("EnergyItem");
     add(energyItem);
     energyItem.add(new Link<String>("EnergyPageLinkTab") {
       private static final long serialVersionUID = 1L;
 
       /** Upon clicking this link, go to energy page. */
       @Override
       public void onClick() {
         setResponsePage(Energy.class);
       }
     });
     ((SolarDecathlonSession)getSession()).getHeaderSession().setEnergyItem(energyItem);
 
     // Add Aquaponics Link to page (tabs)
     aquaponicsItem = new WebMarkupContainer("AquaponicsItem");
     add(aquaponicsItem);
     aquaponicsItem.add(new Link<String>("AquaponicsPageLinkTab") {
       private static final long serialVersionUID = 1L;
 
       /** Upon clicking this link, go to aquaponics page. */
       @Override
       public void onClick() {
         setResponsePage(AquaPonics.class);
       }
     });
     ((SolarDecathlonSession)getSession()).getHeaderSession().setAquaponicsItem(aquaponicsItem);
 
     // Add Lighting Link to page (tabs)
     lightingItem = new WebMarkupContainer("LightingItem");
     add(lightingItem);
     lightingItem.add(new Link<String>("LightingPageLinkTab") {
       private static final long serialVersionUID = 1L;
 
       /** Upon clicking this link, go to lighting page. */
       @Override
       public void onClick() {
         setResponsePage(Lighting.class);
       }
     });
     ((SolarDecathlonSession)getSession()).getHeaderSession().setLightingItem(lightingItem);
 
     // Add Hvac Link to page (tabs)
     hvacItem = new WebMarkupContainer("HvacItem");
     add(hvacItem);
     hvacItem.add(new Link<String>("HvacPageLinkTab") {
       private static final long serialVersionUID = 1L;
 
       /** Upon clicking this link, go to energy page. */
       @Override
       public void onClick() {
         setResponsePage(Hvac.class);
       }
     });
     ((SolarDecathlonSession)getSession()).getHeaderSession().setHvacItem(hvacItem);
     
     // Add help Link to page (tabs)
     helpItem = new WebMarkupContainer("HelpItem");
     add(helpItem);
     helpItem.add(new Link<String>("HelpPageLinkTab") {
       private static final long serialVersionUID = 1L;
 
       /** Upon clicking this link, go to help page. */
       @Override
       public void onClick() {
         setResponsePage(Help.class);
       }
     });
     ((SolarDecathlonSession)getSession()).getHeaderSession().setHelpItem(helpItem);
 
     // // Add Security Link to page (tabs)
     // securityItem = new WebMarkupContainer("SecurityItem");
     // add(securityItem);
     // securityItem.add(new Link<String>("SecurityPageLinkTab") {
     // private static final long serialVersionUID = 1L;
     //
     // /** Upon clicking this link, go to the security page. */
     // @Override
     // public void onClick() {
     // properties.put(PAGE_DISPLAY, 5);
     // setResponsePage(new Security());
     //
     // }
     // });
     //
     // // Add Reports Link to page (tabs)
     // reportsItem = new WebMarkupContainer("ReportsItem");
     // add(reportsItem);
     // reportsItem.add(new Link<String>("ReportsPageLinkTab") {
     // private static final long serialVersionUID = 1L;
     //
     // /** Upon clicking this link, go to reports page. */
     // @Override
     // public void onClick() {
     // properties.put(PAGE_DISPLAY, 6);
     // setResponsePage(new Reports());
     //
     // }
     // });
     //
     // // Add Settings Link to page (tabs)
     // settingsItem = new WebMarkupContainer("SettingsItem");
     // add(settingsItem);
     // settingsItem.add(new Link<String>("SettingsPageLinkTab") {
     // private static final long serialVersionUID = 1L;
     //
     // /** Upon clicking this link, go to settings page. */
     // @Override
     // public void onClick() {
     // properties.put(PAGE_DISPLAY, 7);
     // setResponsePage(new Settings());
     //
     // }
     // });
     //
     // // Add Administrator Link to page (tabs)
     // administratorItem = new WebMarkupContainer("AdministratorItem");
     // add(administratorItem);
     // administratorItem.add(new Link<String>("AdministratorPageLinkTab") {
     // private static final long serialVersionUID = 1L;
     //
     // /** Upon clicking this link, go to admin page. */
     // @Override
     // public void onClick() {
     // properties.put(PAGE_DISPLAY, 8);
     // setResponsePage(new Administrator());
     //
     // }
     // });
     //
     // // Add Help Link to page (tabs)
     // helpItem = new WebMarkupContainer("HelpItem");
     // add(helpItem);
     // helpItem.add(new Link<String>("HelpPageLinkTab") {
     // private static final long serialVersionUID = 1L;
     //
     // /** Upon clicking this link, go to help page. */
     // @Override
     // public void onClick() {
     // properties.put(PAGE_DISPLAY, 9);
     // setResponsePage(new Help());
     //
     // }
     // });
 
     // Footer Links
     add(new Link<String>("DashboardPageLink") {
       private static final long serialVersionUID = 1L;
 
       /** Upon clicking this link, go to the dashboard. */
       @Override
       public void onClick() {
         ((SolarDecathlonSession)getSession()).getHeaderSession().setActiveTab(0);
         try {
           setResponsePage(Dashboard.class);
         }
         catch (Exception e) {
           e.printStackTrace();
         }
       }
     });
 
     add(new Link<String>("EnergyPageLink") {
       private static final long serialVersionUID = 1L;
 
       /** Upon clicking this link, go to energy page. */
       @Override
       public void onClick() {
         ((SolarDecathlonSession)getSession()).getHeaderSession().setActiveTab(1);
         try {
           setResponsePage(Energy.class);
         }
         catch (Exception e) {
           e.printStackTrace();
         }
       }
     });
 
     add(new Link<String>("AquaponicsPageLink") {
       private static final long serialVersionUID = 1L;
 
       /** Upon clicking this link, go to aquaponics page. */
       @Override
       public void onClick() {
         ((SolarDecathlonSession)getSession()).getHeaderSession().setActiveTab(2);
         try {
           setResponsePage(AquaPonics.class);
         }
         catch (Exception e) {
           e.printStackTrace();
         }
       }
     });
     
     add(new Link<String>("LightingPageLink") {
       private static final long serialVersionUID = 1L;
 
       /** Upon clicking this link, go to lighting page. */
       @Override
       public void onClick() {
         ((SolarDecathlonSession)getSession()).getHeaderSession().setActiveTab(3);
         setResponsePage(Lighting.class);
       }
     });
     
     add(new Link<String>("HvacPageLink") {
       private static final long serialVersionUID = 1L;
 
       /** Upon clicking this link, go to hvac page. */
       @Override
       public void onClick() {
         ((SolarDecathlonSession)getSession()).getHeaderSession().setActiveTab(4);
         try {
           setResponsePage(Hvac.class);
         }
         catch (Exception e) {
           e.printStackTrace();
         }
       }
     });
     
     add(new Link<String>("HelpPageLink") {
       private static final long serialVersionUID = 1L;
 
       /** Upon clicking this link, go to help page. */
       @Override
       public void onClick() {
         ((SolarDecathlonSession)getSession()).getHeaderSession().setActiveTab(5);
         try {
           setResponsePage(Help.class);
         }
         catch (Exception e) {
           e.printStackTrace();
         }
       }
     });
     // add(new Link<String>("SecurityPageLink") {
     // private static final long serialVersionUID = 1L;
     //
     // /** Upon clicking this link, go to security page. */
     // @Override
     // public void onClick() {
     // properties.put(PAGE_DISPLAY, 5);
     // setResponsePage(new Security());
     // }
     // });
     // add(new Link<String>("SecurityCamPageLink") {
     // private static final long serialVersionUID = 1L;
     //
     // /** Upon clicking this link, go to security page. */
     // @Override
     // public void onClick() {
     // properties.put(PAGE_DISPLAY, 5);
     // setResponsePage(new SecurityCam());
     // }
     // });
     // add(new Link<String>("SecurityRecPageLink") {
     // private static final long serialVersionUID = 1L;
     //
     // /** Upon clicking this link, go to security page. */
     // @Override
     // public void onClick() {
     // properties.put(PAGE_DISPLAY, 5);
     // setResponsePage(new SecurityRec());
     // }
     // });
     // add(new Link<String>("ReportsPageLink") {
     // private static final long serialVersionUID = 1L;
     //
     // /** Upon clicking this link, go to reports page. */
     // @Override
     // public void onClick() {
     // properties.put(PAGE_DISPLAY, 6);
     // setResponsePage(new Reports());
     // }
     // });
     // add(new Link<String>("SettingsPageLink") {
     // private static final long serialVersionUID = 1L;
     //
     // /** Upon clicking this link, go to settings page. */
     // @Override
     // public void onClick() {
     // properties.put(PAGE_DISPLAY, 7);
     // setResponsePage(new Settings());
     // }
     // });
     // add(new Link<String>("AdministratorPageLink") {
     // private static final long serialVersionUID = 1L;
     //
     // /** Upon clicking this link, go to admin page. */
     // @Override
     // public void onClick() {
     // properties.put(PAGE_DISPLAY, 8);
     // setResponsePage(new Administrator());
     // }
     // });
     // add(new Link<String>("HelpPageLink") {
     // private static final long serialVersionUID = 1L;
     //
     // /** Upon clicking this link, go to help page. */
     // @Override
     // public void onClick() {
     // properties.put(PAGE_DISPLAY, 9);
     // setResponsePage(new Help());
     // }
     // });
     //
     // add(new Link<String>("LoggedInAsLink") {
     // private static final long serialVersionUID = 1L;
     //
     // /** Upon clicking this link, go to settings page. */
     // @Override
     // public void onClick() {
     // properties.put(PAGE_DISPLAY, 7);
     // setResponsePage(new Settings());
     // }
     // });
     //
     // add(new Link<String>("LoginPageLink") {
     // private static final long serialVersionUID = 1L;
     //
     // /** Upon clicking this link, go to login page. */
     // @Override
     // public void onClick() {
     // setResponsePage(new Login());
     // }
     // });
 
     // figure out the active page from session properties.
 //    activePage = properties.get(PAGE_DISPLAY);
 //    // Make the current tab active
 //    makeTabActive(activePage);
 
     // the info on top right of the page
     Calendar cal = Calendar.getInstance();      
     final SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
     dateFormat.setTimeZone(TimeZone.getTimeZone(timeZone));
     String currentTime = dateFormat.format(cal.getTime());
 
     final Label time = new Label("Calendar", currentTime);
     // update the time every second
     time.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)) {
 
       private static final long serialVersionUID = 1L;
 
       @Override
       protected void onPostProcessTarget(AjaxRequestTarget target) {
         Calendar newCal = Calendar.getInstance();
         // set new time to the label
         time.setDefaultModelObject(String.valueOf(dateFormat.format(newCal.getTime())));
 
       }
     });
 
     // The ModalWindow, showing some choices for the user to select.
     final SystemChecker systemChecker = new SystemChecker();  
     final ModalWindow selectModalWindow = new SelectModalWindow("modalwindow",
         systemChecker) {
         /** */
       private static final long serialVersionUID = 1L;
         /*
         void onSelect(AjaxRequestTarget target, String selection) {
             // Handle Select action
             //resultLabel.setModelObject(selection);
             //target.addComponent(resultLabel);
             close(target);
         }
 
         void onCancel(AjaxRequestTarget target) {
             // Handle Cancel action
             //resultLabel.setModelObject("ModalWindow cancelled.");
             //target.addComponent(resultLabel);
             close(target);
         }
         */
     };
   
   selectModalWindow.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(10)) {
    /** */
   private static final long serialVersionUID = 1L;
 
       @Override
       protected void onPostProcessTarget(AjaxRequestTarget target) {
         if (systemChecker.foundError()) {
           int activeTabNumber = 
              ((SolarDecathlonSession)getSession()).getHeaderSession().getActiveTab();
           String erroneousSystem = systemChecker.getErroroneousSystem();
           selectModalWindow.setTitle("System Malfunction: " + erroneousSystem);
           
           switch (activeTabNumber) {
           case 0:
             if (!erroneousSystem.equalsIgnoreCase("Dashboard")) {
               selectModalWindow.show(target);
             }
             break;
           case 1:
             if (!erroneousSystem.equalsIgnoreCase("Energy")) {
               selectModalWindow.show(target);
             }
             break;
           case 2:
             if (!erroneousSystem.equalsIgnoreCase("Aquaponics")) {
               selectModalWindow.show(target);
             }
             break;
           case 3:
             if (!erroneousSystem.equalsIgnoreCase("Lighting")) {
               selectModalWindow.show(target);
             }
             break;
           case 4:
             if (!erroneousSystem.equalsIgnoreCase("Hvac")) {
               selectModalWindow.show(target);
             }
             break;
           default:
             selectModalWindow.show(target);
           }
         }
       }
     });
     
     add(selectModalWindow);
     
     add(time);
 
     add(currentWeatherHeader);
     add(outsideTemperatureHeader);
     add(insideTemperatureHeader);
   }
 
   /**
    * Highlights the active tab.
    * 
    * @param i - this is a flag to tell the application which tab should be active
    */
 //  private void makeTabActive(int i) {
 //    String classContainer = "class";
 //    String activeContainer = "active";
 //
 //    switch (i) {
 //
 //    case 1:
 //      energyItem
 //          .add(new AttributeModifier(classContainer, true, new Model<String>(activeContainer)));
 //      break;
 //    case 2:
 //      aquaponicsItem.add(new AttributeModifier(classContainer, true, new Model<String>(
 //          activeContainer)));
 //      break;
 //    case 3:
 //      lightingItem.add(new AttributeModifier(classContainer, true, new Model<String>(
 //          activeContainer)));
 //      break;
 //    case 4:
 //      hvacItem.add(new AttributeModifier(classContainer, true, new Model<String>(
 //          activeContainer)));
 //      break;
 //    // case 5:
 //    // securityItem.add(new AttributeModifier(classContainer, true, new Model<String>(
 //    // activeContainer)));
 //    // break;
 //    // case 6:
 //    // reportsItem.add(new AttributeModifier(classContainer, true,
 //    // new Model<String>(activeContainer)));
 //    // break;
 //    // case 7:
 //    // settingsItem.add(new AttributeModifier(classContainer, true, new Model<String>(
 //    // activeContainer)));
 //    // break;
 //    // case 8:
 //    // administratorItem.add(new AttributeModifier(classContainer, true, new Model<String>(
 //    // activeContainer)));
 //    // break;
 //    // case 9:
 //    // helpItem.add(new AttributeModifier(classContainer, true,
 //    // new Model<String>(activeContainer)));
 //    // break;
 //    case 0: // pass-through
 //    default:
 //      dashboardItem.add(new AttributeModifier(classContainer, true, new Model<String>(
 //          activeContainer)));
 //      break;
 //    }
 //  }
 
   /**
    * Returns session properties for graphs.
    * 
    * @return the graph to display.
    */
   public Map<String, Integer> getSessionGraphProperties() {
     return ((SolarDecathlonSession) getSession()).getProperties();
 
   }
 
   /**
    * The outside temperature label. May delete this method after outsideTemp is added to the API
    * dictionary and do it the same way as inside temp.
    * 
    * @return The outside temp label.
    */
 //  public Label getOutsideTempLabel() {
 //    return outsideTemperatureHeader;
 //  }
 
   /**
    * Set the city name.
    * @param newCity The city name.
    */
   public static void setCityName(String newCity) {
     cityName = newCity;
   }
   
   /**
    * Returns the city name.
    * @return The city name.
    */
   public String getCityName() {
     return cityName;
   }
   
   /**
    * Set the time zone (e.g. "US/Hawaii")
    * @param newTimeZone The time zone.
    */
   public static void setTimeZone(String newTimeZone) {
     timeZone = newTimeZone;
   }
   
   /**
    * Returns the time zone.
    * @return The time zone.
    */
   public String getTimeZone() {
     return timeZone;
   }
   
   /**
    * Returns an integer associate to the page the user is currently viewing.
    * @return activeTab;
    */
   public int getActiveTab() {
     return activeTab;
   }
 }
