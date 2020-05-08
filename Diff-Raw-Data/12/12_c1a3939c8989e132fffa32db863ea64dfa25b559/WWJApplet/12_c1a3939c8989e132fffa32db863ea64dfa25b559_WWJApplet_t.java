 /*
 Copyright (C) 2001, 2011 United States Government
 as represented by the Administrator of the
 National Aeronautics and Space Administration.
 All Rights Reserved.
 */
 package Main;
 
 import gov.nasa.worldwind.*;
 import gov.nasa.worldwind.avlist.AVKey;
 import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
 import gov.nasa.worldwind.examples.ClickAndGoSelectListener;
 import gov.nasa.worldwind.geom.*;
 import gov.nasa.worldwind.layers.*;
 import gov.nasa.worldwind.render.GlobeAnnotation;
 import gov.nasa.worldwind.util.StatusBar;
 import gov.nasa.worldwind.view.orbit.*;
 import netscape.javascript.JSObject;
 
 import javax.swing.*;
 import java.awt.*;
 
 import Layers.*;
 import Utilities.Time;
 import Satellite.AbstractSatellite;
 import java.util.Hashtable;
 import Satellite.StkEphemerisReader;
 import Utilities.OnlineInput;
 import Satellite.CustomSatellite;
 import java.util.Vector;
 import Utilities.StateVector;
 import java.util.GregorianCalendar;
 import Satellite.JSatTrakTimeDependent;
 import Bodies.*;
 import gov.nasa.worldwind.examples.sunlight.*;
 import java.text.SimpleDateFormat;
 import gov.nasa.worldwind.layers.placename.*;
 import gov.nasa.worldwind.layers.Earth.*;
 import gov.nasa.worldwind.layers.StarsLayer;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import Utilities.AstroConst;
 import javax.swing.Timer;
 import TwoDImage.J2DEarthPanel;
 import View.*;
 import gov.nasa.worldwind.awt.AWTInputHandler;
 import java.util.Random;
 
 
 /**
  * Illustrates the how to display a World Wind <code>{@link WorldWindow}</code> in a Java Applet and interact with the
  * WorldWindow through JavaScript code running in the browser. This class extends <code>{@link JApplet}</code> and
  * embeds a WorldWindowGLCanvas and a StatusBar in the Applet's content pane.
  *
  * @author Patrick Murris
  * @version $Id: WWJApplet.java 15663 2011-06-17 21:15:40Z dcollins $
  */
 
 /**
  * This is code for a satellite visualization tool using NASA World Wind and a open source
  * satellite tracking program called JSatTrak (by Sean Gano).  The following code is a combination
  * of modified code from both programs to create a web-embeddable applet that can visualize satellites
  * in their orbits.  This program was developed by a.i. Solutions (www.ai-solutions.com).  
  * 
  * @author MMascaro
  */
 public class WWJApplet extends JApplet
 {
     protected WorldWindowGLCanvas wwd; //from original applet code- this is World Wind
     protected RenderableLayer labelsLayer; //Right now unimportant- does nothing
     private ViewControlsLayer viewControlsLayer; //Layer for the little buttons in the bottom right (controls view)
     private Model m; //This is the globe itself
     
     private Sun sun; //Self explanatory
     private SunPositionProvider spp; //Changes the position of the sun based on time
     private boolean sunShadingOn = false; // controls if sun shading is used
     private StarsLayer starsLayer; //need this to change size of star layer
     private LensFlareLayer lensFlareLayer; //creates the lens flare (needed to make sun visible)
     
     //Making ECI/ECEF Layer
     private ECIRenderableLayer eciLayer; //Earth Centered Inertial layer- satellites are added to this
     private OrbitModelRenderable orbitModel; //Model for satellites and their orbits
     private ECIRadialGrid eciRadialGrid = new ECIRadialGrid(); //Radial grid for ECI layer, good for testing to see if layer renders properly
     private ECEFRenderableLayer ecefLayer; //Earth Centered Earth Fixed layer
     private ECEFModelRenderable ecefModel; //Model for satellites and their orbits
     private boolean viewModeECI = true; //Boolean controls if ECI or ECEF view (used in WWsetMJD)
     private EcefTimeDepRenderableLayer timeDepLayer; //layer of time dependent objects in ECEF?
     
     //Satellites
     private Hashtable<String,AbstractSatellite> satHash = new Hashtable<String,AbstractSatellite>(); //this table stores each satellite added to the program (Satellites MUST be added)
     private StkEphemerisReader reader = new StkEphemerisReader(); //Reader for STK ephemeris files (only STK format)
     private OnlineInput input; //Custom-made class to aquire user inputs
     Vector<JSatTrakTimeDependent> timeDependentObjects = new Vector<JSatTrakTimeDependent>(); //Time dependent objects
     private boolean orbitShown = true; //Boolean to control whether orbit traces are shown
     private boolean update = false; //Boolean to control whether user input should automatically update or load only once (default is once)
     Vector<StateVector> vector; //vector to read in satellite information from the ephemeris reader (located in inputSatellites)
     boolean timerOn = false; //Boolean to control whether real-time mode is on or off
     Timer eTimer; //Timer for automatic updating of user inputs
    private boolean ignoreOverride = false; //Ignore override of time if original user input time was incorrect: change time when corrected
     
     private Time currentJulianDate = new Time(); // current sim or real time (Julian Date)
     private Time scenarioEpochDate = new Time(); //Time displayed in scenario
     double time = 100000000000000.0; //Far too big- used to determine earliest ephemeris time
     private SimpleDateFormat dateformat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS z"); //date format for scenario time strings
     double oldTime; //time used when no longer in real-time mode (takes scenario back to last non-real time)
     private boolean overrideTime = false; //prevents scenario from reverting to the user-input time when using automatically updating inputs
     
     //Animation
     private int currentPlayDirection = 0; //-1 backward, 0 stop, 1 forwards
     private double animationSimStepSeconds = 60.0; //step size (default is one minute)
     private int animationRefreshRateMs = 50; //Refresh rate for step size (time in between steps)
     private boolean play = true; //Boolean to control whether scenario is playing or not (default true): true means scenario is ready to play, false means not ready (playing already)
     private boolean inputSat = true; //Boolean for whether satellites have been input or not
     private boolean end = false; //Boolean for end of ephemeris time (prevents scenario from running past the end of ephemeris)
     double[] steps = new double[] {1, 10, 30, 60, 120, 300, 1800, 3600, 7200, 86400}; //step sizes
     int stepNumber = 3; //Index for the current step size in the array (starts at index of zero, so 3 corresponds to a step size of 60)
     private Timer playTimer; //Timer for animation of scenario
     private boolean twoDon = false; //Boolean to control 2D and 3D modes
     private boolean nonRealTime = true; //Boolean for whether scenario is in real time or non real time mode
     double tempStep = 60; //Temporary variable for step size- used in custom step size code
     private boolean reset = true; //Boolean to control whether scenario has been reset or not (default is true)
     private boolean updating = false; //Boolean to prevent scenario from trying to change satellite properties while user input is being updated
     private boolean displayed = false; //Boolean to prevent text from being constantly displayed in status display during a loop
     
     //Buttons
     JButton playScenario; //play scenario button
     JButton pauseScenario; //pause scenario button
     JButton resetScenario; //reset scenario button
     JButton stepSizeUp; //Increase step size
     JButton stepSizeDown; //Decrease step size
     JRadioButton ECIon; //ECI mode
     JRadioButton ECEFon; //ECEF mode
     JToolBar toolbar; //Toolbar contains each button and display 
     JTextField dateDisplay; //Display for scenario time
     JTextField stepDisplay; //Display for step size value/input
     JTextField statusDisplay; //Display for scenario status/errors
     JLabel stepSizeLabel; //Label for step size
     JRadioButton twoDbutton; //2D mode
     JRadioButton threeDbutton; //3D mode
     JCheckBox realTime; //Real time mode
     JCheckBox orbitTrace; //display orbit trace
     JCheckBox eUpdate; //Automatic update mode
     
     Container Content = this.getContentPane(); //Container for applet - toolbar, world wind, and status bar added to this
     J2DEarthPanel twoDpanel; //2D panel
     
     // view mode options
     private boolean modelViewMode = false; // model view mode (not supported currently)
     private String modelViewString = ""; // to hold name of satellite to view when modelViewMode=true
     private double modelViewNearClip = 10000; // clipping plane for when in Model View mode
     private double modelViewFarClip = 5.0E7; // max clipping plan for model view
     private boolean smoothViewChanges = true; // for 3D view smoothing (only is set after model/earth view has been changed -needs to be fixed)
     // near/far clipping plane distances for 3d windows (can effect render speed and if full orbit is shown)
     private double farClippingPlaneDistOrbit = -1;//200000000d; // good out to geo, but slow for LEO, using AutoClipping plane view I made works better
     private double nearClippingPlaneDistOrbit = -1; // -1 value Means auto adjusting // value adjusted in World Wind source code
     
     public WWJApplet()
     {
     }
 
     @Override
     public void init()
     {
         try
         {
             // Check for initial configuration values
             String value = getParameter("InitialLatitude");
             if (value != null)
                 Configuration.setValue(AVKey.INITIAL_LATITUDE, Double.parseDouble(value));
             value = getParameter("InitialLongitude");
             if (value != null)
                 Configuration.setValue(AVKey.INITIAL_LONGITUDE, Double.parseDouble(value));
             value = getParameter("InitialAltitude");
             if (value != null)
                 Configuration.setValue(AVKey.INITIAL_ALTITUDE, Double.parseDouble(value));
             value = getParameter("InitialHeading");
             if (value != null)
                 Configuration.setValue(AVKey.INITIAL_HEADING, Double.parseDouble(value));
             value = getParameter("InitialPitch");
             if (value != null)
                 Configuration.setValue(AVKey.INITIAL_PITCH, Double.parseDouble(value));
 
             // Create World Window GL Canvas
             this.wwd = new WorldWindowGLCanvas();
             this.getContentPane().add(this.wwd, BorderLayout.CENTER);
 
             // Create the default model as described in the current worldwind properties.
             m = (Model) WorldWind.createConfigurationComponent(AVKey.MODEL_CLASS_NAME);
             this.wwd.setModel(m);
             
             //Add sun and sun position provider
             sun = new Sun(currentJulianDate.getMJD());
             spp = new CustomSunPositionProvider(sun);
             // Add lens flare layer
             this.lensFlareLayer = LensFlareLayer.getPresetInstance(LensFlareLayer.PRESET_BOLD);
             m.getLayers().add(this.lensFlareLayer);
             setSunShadingOn(true);       
             
             //Call to function to set up the layers for the model
             setUpLayers();
             
             // first call to update time to current time:
             currentJulianDate.update2CurrentTime(); //update();// = getCurrentJulianDate(); // ini time
 
             // just a little touch up -- remove the milliseconds from the time
             int mil = currentJulianDate.get(Time.MILLISECOND);
             currentJulianDate.add(Time.MILLISECOND,1000-mil); // remove the milliseconds (so it shows an even second)
 
             // set time string format
             currentJulianDate.setDateFormat(dateformat);
             scenarioEpochDate.setDateFormat(dateformat);
             
             //Create 2D panel 
             this.twoDpanel = createNew2dWindow();
             
             //SET UP GUI
             //Add a tool bar
             toolbar = new JToolBar();
             Content.add(toolbar, BorderLayout.PAGE_START);
             
             //Add the play button
             playScenario = new JButton("Play");
             playScenario.addActionListener((new java.awt.event.ActionListener() {
                 @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 playButtonActionPerformed(evt);
             }}));
             toolbar.add(playScenario);
             
             //Add pause button
             pauseScenario = new JButton("Pause");
             pauseScenario.addActionListener((new java.awt.event.ActionListener() {
                 @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 pauseButtonActionPerformed(evt);
             }}));
             toolbar.add(pauseScenario); 
             
             //Reset button
             resetScenario = new JButton("Reset");
             resetScenario.addActionListener((new java.awt.event.ActionListener() {
                 @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 resetButtonActionPerformed(evt);
             }}));
             toolbar.add(resetScenario);
                         
             //Step size label
             stepSizeLabel = new JLabel();
             stepSizeLabel.setText("Step Size: ");
             toolbar.add(stepSizeLabel);
             
             //Increase step size
             stepSizeUp = new JButton("+");
             stepSizeUp.addActionListener((new java.awt.event.ActionListener() {
                 @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 stepUpButtonActionPerformed(evt);
             }}));
             toolbar.add(stepSizeUp);
 
             //Step size display
             stepDisplay = new JTextField();
             stepDisplay.setSize(50, 10);
             stepDisplay.setText("" + animationSimStepSeconds);
             stepDisplay.addActionListener((new java.awt.event.ActionListener() {
                 @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 stepDisplayActionPerformed(evt);
             }}));
             toolbar.add(stepDisplay); 
             
             //Decrease step size
             stepSizeDown = new JButton("-");
             stepSizeDown.addActionListener((new java.awt.event.ActionListener() {
                 @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 stepDownButtonActionPerformed(evt);
             }}));
             toolbar.add(stepSizeDown);
             
             //ECI Button
             ECIon = new JRadioButton("ECI");
             ECIon.setSelected(true);
             ECIon.addActionListener((new java.awt.event.ActionListener() {
                 @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 eciButtonActionPerformed(evt);
             }}));
             toolbar.add(ECIon);
             
             //ECEF Button
             ECEFon = new JRadioButton("ECEF");
             ECEFon.setSelected(true);
             ECEFon.addActionListener((new java.awt.event.ActionListener() {
                 @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 ecefButtonActionPerformed(evt);
             }}));
             toolbar.add(ECEFon);
             //Connect the radio buttons (ECI and ECEF)
             ButtonGroup bg = new ButtonGroup();
             bg.add(ECIon);
             bg.add(ECEFon);    
                         
             //two D view Button
             twoDbutton = new JRadioButton("2D View");
             twoDbutton.setSelected(false);
             twoDbutton.addActionListener((new java.awt.event.ActionListener() {
                 @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 twoDButtonActionPerformed(evt);
             }}));
             toolbar.add(twoDbutton);
             
             //three D view Button
             threeDbutton = new JRadioButton("3D View");
             threeDbutton.setSelected(true);
             threeDbutton.addActionListener((new java.awt.event.ActionListener() {
                 @Override
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 threeDButtonActionPerformed(evt);
             }}));
             toolbar.add(threeDbutton);
             //Connect 2D and 3D view buttons
             ButtonGroup bgView = new ButtonGroup();
             bgView.add(twoDbutton);
             bgView.add(threeDbutton); 
             
             //Add a date text field
             dateDisplay = new JTextField("Date/Time");
             dateDisplay.setText( currentJulianDate.getDateTimeStr() );
             toolbar.add(dateDisplay);
             
             //Add error/status display
             statusDisplay = new JTextField("Status: ");
             statusDisplay.setText("Running");
             toolbar.add(statusDisplay);
             
             //Add real time checkbox
             realTime = new JCheckBox("Real Time");
             realTime.setSelected(false);
             realTime.addActionListener((new java.awt.event.ActionListener(){
                 @Override
                         public void actionPerformed(java.awt.event.ActionEvent evt){
                     realTimeActionPerformed(evt);   
                 }}));
             toolbar.add(realTime); 
             
             //Add orbit trace checkbox
             orbitTrace = new JCheckBox("Orbit Trace");
             orbitTrace.setSelected(true);
             orbitTrace.addActionListener((new java.awt.event.ActionListener(){
                 @Override
                         public void actionPerformed(java.awt.event.ActionEvent evt){
                     orbitTraceActionPerformed(evt);   
                 }}));
             toolbar.add(orbitTrace);
             
             //Add checkbox for user input updating
             eUpdate = new JCheckBox("Auto Update");
             eUpdate.setSelected(false);
             eUpdate.addActionListener((new java.awt.event.ActionListener(){
                 @Override
                         public void actionPerformed(java.awt.event.ActionEvent evt){
                     eUpdateActionPerformed(evt);   
                 }}));
             toolbar.add(eUpdate);
             
             inputSatellites(); //function that reads user input file and adds satellites
             
             //WORLD WIND APPLET CODE (UNMODIFIED)
             // Call javascript appletInit()
             try
             {
                 JSObject win = JSObject.getWindow(this);
                 win.call("appletInit", null);
             }
             catch (Exception ignore)
             {
             }
         }
         catch (Throwable e)
         {
         }
     }
 
     @Override
     public void start()
     {
         // Call javascript appletStart()
         try
         {
             JSObject win = JSObject.getWindow(this);
             win.call("appletStart", null);
         }
         catch (Exception ignore)
         {
         }
     }
 
     @Override
     public void stop()
     {
         // Call javascript appletSop()
         try
         {
             JSObject win = JSObject.getWindow(this);
             win.call("appletStop", null);
         }
         catch (Exception ignore)
         {
         }
 
         // Shut down World Wind when the browser stops this Applet.
         WorldWind.shutDown();
     }
 
     /**
      * Adds a layer to WW current layerlist, before a named layer. Target name can be a part of the layer name
      *
      * @param wwd        the <code>WorldWindow</code> reference.
      * @param layer      the layer to be added.
      * @param targetName the partial layer name to be matched - case sensitive.
      */
     public static void insertBeforeLayerName(WorldWindow wwd, Layer layer, String targetName)
     {
         // Insert the layer into the layer list just before the target layer.
         LayerList layers = wwd.getModel().getLayers();
         int targetPosition = layers.size() - 1;
         for (Layer l : layers)
         {
             if (l.getName().indexOf(targetName) != -1)
             {
                 targetPosition = layers.indexOf(l);
                 break;
             }
         }
         layers.add(targetPosition, layer);
     }
 
     // ============== Public API - Javascript ======================= //
 
     /**
      * Move the current view position
      *
      * @param lat the target latitude in decimal degrees
      * @param lon the target longitude in decimal degrees
      */
     public void gotoLatLon(double lat, double lon)
     {
         this.gotoLatLon(lat, lon, Double.NaN, 0, 0);
     }
 
     /**
      * Move the current view position, zoom, heading and pitch
      *
      * @param lat     the target latitude in decimal degrees
      * @param lon     the target longitude in decimal degrees
      * @param zoom    the target eye distance in meters
      * @param heading the target heading in decimal degrees
      * @param pitch   the target pitch in decimal degrees
      */
     public void gotoLatLon(double lat, double lon, double zoom, double heading, double pitch)
     {
         BasicOrbitView view = (BasicOrbitView) this.wwd.getView();
         if (!Double.isNaN(lat) || !Double.isNaN(lon) || !Double.isNaN(zoom))
         {
             lat = Double.isNaN(lat) ? view.getCenterPosition().getLatitude().degrees : lat;
             lon = Double.isNaN(lon) ? view.getCenterPosition().getLongitude().degrees : lon;
             zoom = Double.isNaN(zoom) ? view.getZoom() : zoom;
             heading = Double.isNaN(heading) ? view.getHeading().degrees : heading;
             pitch = Double.isNaN(pitch) ? view.getPitch().degrees : pitch;
             view.addPanToAnimator(Position.fromDegrees(lat, lon, 0),
                 Angle.fromDegrees(heading), Angle.fromDegrees(pitch), zoom, true);
         }
     }
 
     /**
      * Set the current view heading and pitch
      *
      * @param heading the traget heading in decimal degrees
      * @param pitch   the target pitch in decimal degrees
      */
     public void setHeadingAndPitch(double heading, double pitch)
     {
         BasicOrbitView view = (BasicOrbitView) this.wwd.getView();
         if (!Double.isNaN(heading) || !Double.isNaN(pitch))
         {
             heading = Double.isNaN(heading) ? view.getHeading().degrees : heading;
             pitch = Double.isNaN(pitch) ? view.getPitch().degrees : pitch;
 
             view.addHeadingPitchAnimator(
                 view.getHeading(), Angle.fromDegrees(heading), view.getPitch(), Angle.fromDegrees(pitch));
         }
     }
 
     /**
      * Set the current view zoom
      *
      * @param zoom the target eye distance in meters
      */
     public void setZoom(double zoom)
     {
         BasicOrbitView view = (BasicOrbitView) this.wwd.getView();
         if (!Double.isNaN(zoom))
         {
             view.addZoomAnimator(view.getZoom(), zoom);
         }
     }
 
     /**
      * Get the WorldWindowGLCanvas
      *
      * @return the current WorldWindowGLCanvas
      */
     public WorldWindowGLCanvas getWW()
     {
         return this.wwd;
     }
 
     /**
      * Get the current OrbitView
      *
      * @return the current OrbitView
      */
     public OrbitView getOrbitView()
     {
         if (this.wwd.getView() instanceof OrbitView)
             return (OrbitView) this.wwd.getView();
         return null;
     }
 
     /**
      * Get a reference to a layer with part of its name
      *
      * @param layerName part of the layer name to match.
      *
      * @return the corresponding layer or null if not found.
      */
     public Layer getLayerByName(String layerName)
     {
         for (Layer layer : wwd.getModel().getLayers())
         {
             if (layer.getName().indexOf(layerName) != -1)
                 return layer;
         }
         return null;
     }
 
     /**
      * Add a text label at a position on the globe.
      *
      * @param text  the text to be displayed.
      * @param lat   the latitude in decimal degrees.
      * @param lon   the longitude in decimal degrees.
      * @param font  a string describing the font to be used.
      * @param color the color to be used as an hexadecimal coded string.
      */
     public void addLabel(String text, double lat, double lon, String font, String color)
     {
         GlobeAnnotation ga = new GlobeAnnotation(text, Position.fromDegrees(lat, lon, 0),
             Font.decode(font), Color.decode(color));
         ga.getAttributes().setBackgroundColor(Color.BLACK);
         ga.getAttributes().setDrawOffset(new Point(0, 0));
         ga.getAttributes().setFrameShape(AVKey.SHAPE_NONE);
         ga.getAttributes().setEffect(AVKey.TEXT_EFFECT_OUTLINE);
         ga.getAttributes().setTextAlign(AVKey.CENTER);
         this.labelsLayer.addRenderable(ga);
     }
     //END OF WORLDWIND APPLET CODE (UNMODIFIED)
     //JSatTrak added code
     
     /*
      * Input name of satellite, adds that satellite to the satHash
      * Called in inputSatellites function
      */
     public void addCustomSat(String name)
     {
         // if nothing given:
         if(name == null || name.equalsIgnoreCase(""))
         {
             System.out.println("returned");
             return;
         }
         
         CustomSatellite prop = new CustomSatellite(name,this.getScenarioEpochDate());
         
         satHash.put(name, prop);
 
         // set satellite time to current date
         prop.propogate2JulDate(this.getCurrentJulTime());
     }
     
     /*
      * Returns scenarioEpochDate
      * Called in addCustomSat
      */
     public Time getScenarioEpochDate()
     {
         return scenarioEpochDate;
     }
     
     /*
      * returns Julian date (double)
      * Used in addCustomSat and animateApplet
      */
     public double getCurrentJulTime()
     {
         return currentJulianDate.getJulianDate();
     }
     
     /*
      * Given an input of milliseconds, adds that time to scenario
      * used in setTime() and inputSatellites
      */
     public void setTime(long millisecs)
     {
     currentJulianDate.set(millisecs);
         
         // update maps ----------------
         // set animation direction = 0
         currentPlayDirection = 0;
         // update graphics
         updateTime();
     }
     
     /**
      * Set the current time of the app.
      * @param julianDate Julian Date
      */
     public void setTime(double julianDate)
     {
         GregorianCalendar gc = Time.convertJD2Calendar(julianDate);
         setTime(gc.getTimeInMillis());        
     }
 
     /*
      * Updates time in scenario and repaints World Wind.  
      * Updates sun postion, ground track, date display, satellites, and ECI/ECEF layers
      */
     public void updateTime()
     {
         // save old time
         double prevJulDate = currentJulianDate.getJulianDate();            
 
         //Adds seconds (play direction should be 1 or 0)
         currentJulianDate.addSeconds( currentPlayDirection*animationSimStepSeconds );
         // update sun position
         sun.setCurrentMJD(currentJulianDate.getMJD());
                 
         // if time jumps by more than 91 minutes check period of sat to see if
         // ground tracks need to be updated
         double timeDiffDays = Math.abs(currentJulianDate.getJulianDate()-prevJulDate); // in days
         checkTimeDiffResetGroundTracks(timeDiffDays);        
                 
         // update date box:
         dateDisplay.setText( currentJulianDate.getDateTimeStr() );//String.format("%tc",cal) );
         
         // now propogate all satellites to the current time  
         for (AbstractSatellite sat : satHash.values() )
         {
             sat.propogate2JulDate( currentJulianDate.getJulianDate() );
         } // propgate each sat 
         
         // update any other time dependant objects
         for(JSatTrakTimeDependent tdo : timeDependentObjects)
         {
             if(tdo != null)
             {
                 tdo.updateTime(currentJulianDate, satHash);
             }
         }
         //Update ECI/ECEF layers
         WWsetMJD(currentJulianDate.getMJD());     
         if(nonRealTime)
         {
             oldTime = currentJulianDate.getJulianDate();
         }
         forceRepainting(); // repaint 2d/3d earth
     } // update time
     
     /*
      * Checks to see if the ground track needs to be reset
      */
     public void checkTimeDiffResetGroundTracks(double timeDiffDays)
     {
         if( timeDiffDays > 91.0/1440.0)
         {
             // big time jump
             for (AbstractSatellite sat : satHash.values() )
             {
                 if(sat.getShowGroundTrack() && (sat.getPeriod() <= (timeDiffDays*24.0*60.0) ) )
                 {
                     sat.setGroundTrackIni2False();
                     //System.out.println(sat.getName() +" - Groundtrack Initiated");
                 }
             }
         }
     } // checkTimeDiffResetGroundTracks
     
     /*
      * Repaints the view, 2D and 3D
      */
     public void forceRepainting()
     {
         this.update(false);
         wwd.redraw();
         try
         {
         twoDpanel.repaint();
         }
         catch(Exception e)
         {}
     }// forceRepainting
     
     // Update worldwind sun shading
     private void update(boolean redraw)
     {
         if(sunShadingOn) //this.enableCheckBox.isSelected())
         {
             // Compute Sun position according to current date and time
             LatLon sunPos = spp.getPosition();
             Vec4 sunvar = wwd.getModel().getGlobe().computePointFromPosition(new Position(sunPos, 0)).normalize3();         
             this.lensFlareLayer.setSunDirection(sunvar);
 
             // Redraw if needed
             if(redraw)
             {
                 wwd.redraw();
             }
         } // if sun Shading
         
     } // update - for sun shading
     
     //Turns on sun shading- really only for lens-flare layer
     public void setSunShadingOn(boolean useSunShading)
     {
         if(useSunShading == sunShadingOn)
         {
             return; // nothing to do
         }
 
         sunShadingOn = useSunShading;
 
         this.update(true); // redraw
     } // setSunShadingOn
 
 //Play scenario
 public void playButtonActionPerformed(ActionEvent e)
 {
     //If scenario isn't already playing and has satellites
     if(play && inputSat)
     {
         //Update every 50 miliseconds
         animationRefreshRateMs = 50;
         //Set step size to non-real time step size used previously (60 second default)
         animationSimStepSeconds = tempStep;
         //Animate
         animateApplet(true);
         //Already playing!
         play = false;
         //Update display
         stepDisplay.setText("" + animationSimStepSeconds);
         //Hasn't been reset
         reset = false;
     }
     else if(end)
     {/* Already playing and/or no satellites */}
     
 }
 
 //Pause scenario
 public void pauseButtonActionPerformed(ActionEvent e)
 {
     //If play: means scenario can be played (Isn't already playing)
     if(play)
     {}
     //Scenario is playing
     else
     {
         //Stop animation
         animateApplet(false);
         //Turn off real time if applicable
         realTime.setSelected(false);
         //Update display
         statusDisplay.setText("Scenario Paused");
         //Isn't already playing
         play = true; 
         //Non real time
         nonRealTime = true;
     }
 }
 
 //Reset scenario
 public void resetButtonActionPerformed(ActionEvent e)
 {
     //If scenario has been reset
     if(reset)
     {}
     //Scenario needs to be reset
     else{
     //Stop playing
     animateApplet(false);
     //Scenario isn't playing (can be played)
     play = true;
     //Update display
     statusDisplay.setText("Scenario Reset");
     //Not in real time mode
     realTime.setSelected(false);
     //Non real time
     nonRealTime = true;
     //Reset step size
     //If there are satellites, set time to user requested time
     if(inputSat)
     {setTime(time);}
     else //set time to current time for no satellites
     {currentJulianDate.update2CurrentTime();} 
     //Has been reset
     reset = true;
     }
 }
 
 //Increase step size
 public void stepUpButtonActionPerformed(ActionEvent e)
 {
     //If at maximum step size, do nothing
     if(stepNumber == steps.length-1)
     {
             statusDisplay.setText("Maximum Step Size Reached");
     }
     //Can be increased
     else
     {
         //Only increase step size in non-real time mode
         if(nonRealTime)
         {//Get next step size in array, increase corresponding index
              animationSimStepSeconds = steps[stepNumber+1];
              statusDisplay.setText("Step Size Increased");
              stepNumber = stepNumber+1;
         }
         else
         {statusDisplay.setText("Real Time Mode");}
     //Save step size for switch between real-time and non-real time
     tempStep = animationSimStepSeconds;
     stepDisplay.setText("" +animationSimStepSeconds);
     }
 }
 
 //Decrease step size
 public void stepDownButtonActionPerformed(ActionEvent e)
 {
     if(stepNumber>0)
     {
         if(nonRealTime)
         {//Decrease step size in array by one, descrease corresponding index
             animationSimStepSeconds = steps[stepNumber-1];
             statusDisplay.setText("Step Size Decreased");
             stepNumber = stepNumber-1;
         }
         else
         {statusDisplay.setText("Real Time Mode");}
     //Save step size for switch between real and non-real time
     tempStep = animationSimStepSeconds;
     stepDisplay.setText("" + animationSimStepSeconds);
     }
     else //Already at minimum step size
     {
         statusDisplay.setText("Minimum Step Size Reached");
     }
 }
 
 //ECI mode
 public void eciButtonActionPerformed(ActionEvent e)
 {
     viewModeECI = true;
     statusDisplay.setText("Earth Centered Inertial View");
 }
 
 //ECEF mode
 public void ecefButtonActionPerformed(ActionEvent e)
 {
     viewModeECI = false;
     statusDisplay.setText("Earth Centered Earth Fixed View");
 }
 
 //Switch to 2D window
 public void twoDButtonActionPerformed(ActionEvent e)
 {
     if(twoDon)
     {/* Already got a 2D window! */}
     else
     {
     try
     {
         //Remove wwd so 2D window can take its place
         Content.remove(wwd);
     }
     catch(Exception nopanel)
     {}
     //Add 2D window
     Content.add(twoDpanel, BorderLayout.CENTER);
     //Window only appears after resizing - so automatically resize and resize back to original
     this.setSize(this.getWidth()-1, this.getHeight());
     this.setSize(this.getWidth()+1, this.getHeight());
     //Window on
     twoDon = true;
     statusDisplay.setText("2D View");
     }
 }
 public void threeDButtonActionPerformed(ActionEvent e)
 {
     if(twoDon)
     {
     try
     {
         //Remove 2D window
         Content.remove(twoDpanel);
         //Remove satellites!  Or else you end up adding multiple copies of the satellites
         timeDepLayer.removeAllRenderables();
         eciLayer.removeAllRenderables();
         ecefLayer.removeAllRenderables();
         m.getLayers().remove(eciLayer);
         m.getLayers().remove(ecefLayer);
         m.getLayers().remove(timeDepLayer);
 
     }
     catch(Exception nopanel)
     {}
     Content.add(wwd, BorderLayout.CENTER); //Add worldwind
     setUpLayers(); //Set up worldwind layers again
     twoDon = false; //3D view
     statusDisplay.setText("3D View");
     }
 }
 
 //If user attempts to add a custom step size by typing in the box!
 public void stepDisplayActionPerformed(ActionEvent e)
 {
     //Assume it worked
     boolean successStep = true;
     try
     {
         String text = stepDisplay.getText(); //Get their input
         tempStep = Double.parseDouble(text); //Attempt to make it a double
     }
     catch(Exception oops)
     { //Didn't work
         successStep = false; 
         statusDisplay.setText("Improper step size");
     }
     if(successStep)
     { //Worked
         if(nonRealTime)
         { //Change step size
             animationSimStepSeconds = tempStep;
             statusDisplay.setText("Step Size Changed");
         }
         else //real-time mode, don't change step size
         {statusDisplay.setText("Real Time Mode");}
     }
 }
 private void realTimeActionPerformed(ActionEvent evt)
 {
     if(nonRealTime)
     {//Switch to real time mode
         animateApplet(false); //Stop applet
         nonRealTime = false; //Real time
         currentJulianDate.update2CurrentTime(); //Set time to current system time
         setTime(currentJulianDate.getJulianDate()); //Set scenario time
         animationSimStepSeconds = 1; //Step size of 1 second
         animationRefreshRateMs = 1000; //Update every 1 second
         stepDisplay.setText("" + animationSimStepSeconds); //Change step size display
         statusDisplay.setText("Real Time Mode"); //Change status
         reset = false; //No longer reset
         if(play)
         {//Play scenario
         animateApplet(true);
         play = false;
         }
     }
     else
     {//Switch to non-real time mode
         setTime(oldTime); //Set time back to time before real time mode was activated
         nonRealTime = true; 
         animationRefreshRateMs = 50; //Update every 50 miliseconds
         animationSimStepSeconds = tempStep; //Change step size back to previous step size before real time mode
         stepDisplay.setText("" + animationSimStepSeconds);
         statusDisplay.setText("Non-real Time Mode");
         animateApplet(false); // Stop playing
         play = true; //ready to play (not playing)
         reset = false; //not reset
     }
 }
 private void orbitTraceActionPerformed(ActionEvent evt)
 {
     if(!updating && inputSat)
     {//Satellites are already added
     if(orbitShown)
     {//Orbit is already displayed, remove it
     for(int i = 0; i<input.getSize(); i++)
     {//For each satellite in scenario
         if(satHash.get(input.getSatelliteName(i)).isDisplayed())
         {//If satellite is currently being displayed, turn off orbits
         satHash.get(input.getSatelliteName(i)).setShow3DOrbitTrace(false);
         satHash.get(input.getSatelliteName(i)).setShow3DOrbitTraceECI(false);
         satHash.get(input.getSatelliteName(i)).setShowGroundTrack(false);
         }
     }
     forceRepainting(); //repaint
     orbitShown = false; //no orbits shown
     }
     else
     {//No orbits showing
     for(int i = 0; i<input.getSize(); i++)
     {//For each satellite in scenario
         if(satHash.get(input.getSatelliteName(i)).isDisplayed())
         {//If satellite is currently being displayed, turn on orbits
         satHash.get(input.getSatelliteName(i)).setShow3DOrbitTrace(true);
         satHash.get(input.getSatelliteName(i)).setShow3DOrbitTraceECI(true);
         satHash.get(input.getSatelliteName(i)).setShowGroundTrack(true);
         }
     }
     forceRepainting(); //repaint
     orbitShown = true; //orbits shown
     }}
 }
 private void eUpdateActionPerformed(ActionEvent e)
 {
     if(update) //If automatic update is on, set off when clicked
     {update = false;}
     else //If automatic update is off, set on when clicked
     {update = true;}
     eTimer = new Timer(6000, new ActionListener() //create update timer
             {
             @Override
                 public void actionPerformed(ActionEvent event)
                 {
                     overrideTime = true; //Do not change time when re-reading input file
                     if(update)
                     { //Update scenario
                     updating = true; //Do not repaint during this time 
                     satHash.clear(); //Remove satellites
                     inputSatellites(); //Add satellites, but ignore changing time
                     updating = false; //Can repaint
                     statusDisplay.setText("Ephemeris Updated");
                     displayed = false; //Variable for satus display: prevents update from constantly overriding other status messages
                     }
                     if(!update) //Stop updating!
                     {
                         eTimer.stop(); //Stop timer
                         if(!displayed) //If it hasn't already been displayed
                         {statusDisplay.setText("Ephemeris Update Stopped");
                         displayed = true; //has been displayed
                         }
                     }
                     try{
                     if(orbitTrace.isSelected()) //if orbit traces were displayed, need to redraw
                     {
                         for(int i = 0; i<input.getSize(); i++)
                         {
                             if(satHash.get(input.getSatelliteName(i)).isDisplayed())
                             {
                             satHash.get(input.getSatelliteName(i)).setShow3DOrbitTrace(true);
                             satHash.get(input.getSatelliteName(i)).setShow3DOrbitTraceECI(true);
                             satHash.get(input.getSatelliteName(i)).setShowGroundTrack(true);
                             }
                         }
                         forceRepainting();
                         orbitShown = true;
                     }
                     else //Need to prevent orbit traces from showing if they aren't supposed to be
                     {
                         for(int i = 0; i<input.getSize(); i++)
                         {
                             if(satHash.get(input.getSatelliteName(i)).isDisplayed())
                             {
                             satHash.get(input.getSatelliteName(i)).setShow3DOrbitTrace(false);
                             satHash.get(input.getSatelliteName(i)).setShow3DOrbitTraceECI(false);
                             satHash.get(input.getSatelliteName(i)).setShowGroundTrack(false);
                             }
                         }
                         orbitShown = false;
                         forceRepainting(); 
                     }}
                     catch(Exception e)
                     {}
                 }//Action performed
             }); //Timer
     if(update) //Start timer
     {eTimer.start();}
     
 }
 
 //Animates the applet
 private void animateApplet(boolean b) {
         if (b && inputSat) //If animate and there are satellites
         {
         statusDisplay.setText("Scenario Running");
         //Hard Coded play scenario
         double date = this.getCurrentJulTime();
         double currentMJDtime = date - AstroConst.JDminusMJD;
         double deltaTT2UTC = Time.deltaT(currentMJDtime); // = TT - UTC
         double maxTempTime = 0;
         //Find the very last time in the very last ephemeris
         for (int i = 1; i<=input.getSize(); i++ )
         {
             Vector<StateVector> ephemeris = satHash.get(input.getSatelliteName(i-1)).getEphemeris();
             double tempTime = ephemeris.get(ephemeris.size()-1).state[0] - deltaTT2UTC;
             if(tempTime > maxTempTime) //If this ephemeris time is greater than others
             {
                 maxTempTime = tempTime; //Set as max ephemeris time
             }
         }
         
         final double maxTime =  maxTempTime;      
         playTimer = new Timer(animationRefreshRateMs, new ActionListener()
                 {//Timer for playing scenario
                 @Override
                     public void actionPerformed(ActionEvent event)
                     {
                     //Ensure we're still within ephemeris time range
                     //include step size so orbit is still shown
                     //without this, the very first instant the orbit is gone will be the end point
                     double stepJulian = animationSimStepSeconds/86400;
                     if(getCurrentJulTime() > (maxTime-stepJulian))
                         {
                             playTimer.stop(); //Stop playing!
                             play = true;
                             statusDisplay.setText("End of Scenario");
                         }
                     // take one time step in the animation
                     currentPlayDirection = 1;
                     updateTime(); // animate
                     }
                 });
         playTimer.start();
     }
         else
         { //Should not play!
             if(play)
             {}
             else
             {if(playTimer != null) //If there is a timer created, stop it
             {playTimer.stop();}
             play = true;
             }
         }
     }
 
 //Adjusts ECI and ECEF views based on time
 public void WWsetMJD(double mjd)
     {
                
         if(viewModeECI)
         {
 //            // Hmm need to do something to keep the ECI view moving even after user interaction
 //            // seems to work after you click off globe after messing with it
 //            // this fixes the problem:
             wwd.getView().stopMovement(); //seems to fix prop in v0.5 
 //            // update rotation of view and Stars
             double theta0 = eciLayer.getRotateECIdeg();
 //
             // UPDATE TIME
             eciLayer.setCurrentMJD(mjd);
 //
             double thetaf = eciLayer.getRotateECIdeg(); // degrees
 //
 //            // move view
 //
 //            //Quaternion q0 = ((BasicOrbitView) wwd.getView()).getRotation();
 //            //Vec4 vec = ((BasicOrbitView) wwd.getView()).getEyePoint();
 //            
 //            //Position pos = ((BasicOrbitView) wwd.getView()).getCurrentEyePosition();
             Position pos = ((BasicOrbitView) wwd.getView()).getCenterPosition(); // WORKS
 //            
 //            // amount to rotate the globe (degrees) around poles axis
             double rotateEarthDelta = thetaf - theta0; // deg
 //
 //            //Quaternion q = Quaternion.fromRotationYPR(Angle.fromDegrees(0), Angle.fromDegrees(rotateEarthDelta), Angle.fromDegrees(0.0));
 //            // rotate the earth around z axis by rotateEarthDelta
 //            //double[][] rz = MathUtils.R_z(rotateEarthDelta*Math.PI/180);
 //            //double[] newEyePos = MathUtils.mult(rz, new double[] {vec.x,vec.y,vec.z});
 ////            Angle newLon = pos.getLongitude().addDegrees(-rotateEarthDelta);
 ////            Position newPos = new Position(pos.getLatitude(),newLon,pos.getElevation());
 //            
 //            //Position newPos = pos.add(new Position(Angle.fromDegrees(0),Angle.fromDegrees(-rotateEarthDelta),0.0));
             Position newPos = pos.add(new Position(Angle.fromDegrees(0),Angle.fromDegrees(-rotateEarthDelta),0.0)); // WORKS
 //            
 //            // rotation in 3D space is "added" to the quaternion by quaternion multiplication
 ////            try // try around it to prevent problems when running the simulation and then opening a new 3D window (this is called before the wwj is initalized)
 ////            {
 //                //((BasicOrbitView) wwd.getView()).setRotation(q0.multiply(q));
 //            // BUG -- ALWATS REORIENTS VIEW TO NORTH UP AND NO TILT!  -- fixed 15  Jul 2008 SEG
 //                //((BasicOrbitView) wwd.getView()).setEyePosition(newPos);
                ((BasicOrbitView) wwd.getView()).setCenterPosition(newPos); // WORKS  -- fixed 15  Jul 2008 SEG
 ////            }
 ////            catch(Exception e)
 ////            {
 ////                // do nothing, it will catch up next update
 ////            }
 //
             // star layer
             starsLayer.setLongitudeOffset(Angle.fromDegrees(-eciLayer.getRotateECIdeg()));
 //            
         } // if ECI
         else
         {
             // EFEC - just update time
             eciLayer.setCurrentMJD(mjd);
             
             // star layer
             starsLayer.setLongitudeOffset(Angle.fromDegrees(-eciLayer.getRotateECIdeg()));
         }
         
         // debug - reset view to follow sat
         //setViewCenter(15000000); // set this only if user has picked a satellite to follow!
 
         // update layer that needs time updates
         timeDepLayer.setCurrentMJD(mjd);
         
     } // set MJD
 
     //Create 2D window
     public J2DEarthPanel createNew2dWindow()
     {
         
         // create 2D Earth Panel:
         J2DEarthPanel newPanel = new J2DEarthPanel(satHash, currentJulianDate, sun);
 
         String windowName = "2D Earth Window";
         newPanel.setName(windowName);
         return newPanel;
     }
     
     //Set up view preferences (used for model view mode which is currently unavailable)
     private void setupView()
     {
         if(modelViewMode == false)
         { // Earth View mode
             AutoClipBasicOrbitView bov = new AutoClipBasicOrbitView();
             wwd.setView(bov);
             
             // remove the rest of the old input handler  (does this need a remove of hover listener? - maybe it is now completely removed?)
             wwd.getInputHandler().setEventSource(null);
             
             AWTInputHandler awth = new AWTInputHandler();
             awth.setEventSource(wwd);
             wwd.setInputHandler(awth);
             awth.setSmoothViewChanges(smoothViewChanges); // FALSE MAKES THE VIEW FAST!! -- MIGHT WANT TO MAKE IT GUI Chooseable
                         
             // IF EARTH VIEW -- RESET CLIPPING PLANES BACK TO NORMAL SETTINGS!!!
             wwd.getView().setNearClipDistance(this.nearClippingPlaneDistOrbit);
             wwd.getView().setFarClipDistance(this.farClippingPlaneDistOrbit);
             
             // change class for inputHandler
             Configuration.setValue(AVKey.INPUT_HANDLER_CLASS_NAME, 
                         AWTInputHandler.class.getName());
 
             // re-setup control layer handler
             wwd.addSelectListener(new ViewControlsSelectListener(wwd, viewControlsLayer));
             
         } // Earth View mode
         else
         { // Model View mode
             
             // TEST NEW VIEW -- TO MAKE WORK MUST TURN OFF ECI!
             this.setViewModeECI(false);
 
             if(!satHash.containsKey(modelViewString))
             {
                 statusDisplay.setText("No Current Satellite Selected, can't switch to Model Mode: " + modelViewString);
                 return;
             }
 
             AbstractSatellite sat = satHash.get(modelViewString);
 
             BasicModelView3 bmv;
             if(wwd.getView() instanceof BasicOrbitView)
             {
                 bmv = new BasicModelView3(((BasicOrbitView)wwd.getView()).getOrbitViewModel(), sat);
                 //bmv = new BasicModelView3(sat);
             }
             else
             {
                 bmv = new BasicModelView3(((BasicModelView3)wwd.getView()).getOrbitViewModel(), sat);
             }
             
             // remove the old hover listener -- depending on this instance of the input handler class type
             if( wwd.getInputHandler() instanceof AWTInputHandler)
             {
                 ((AWTInputHandler) wwd.getInputHandler()).removeHoverSelectListener();
             }
             else if( wwd.getInputHandler() instanceof BasicModelViewInputHandler3)
             {
                 ((BasicModelViewInputHandler3) wwd.getInputHandler()).removeHoverSelectListener();
             }
             
             // set view
             wwd.setView(bmv);
 
             // remove the rest of the old input handler
             wwd.getInputHandler().setEventSource(null);
              
             // add new input handler
             BasicModelViewInputHandler3 mih = new BasicModelViewInputHandler3();
             mih.setEventSource(wwd);
             wwd.setInputHandler(mih);
             
             // view smooth?
             mih.setSmoothViewChanges(smoothViewChanges); // FALSE MAKES THE VIEW FAST!!
 
             // settings for great closeups!
             wwd.getView().setNearClipDistance(modelViewNearClip);
             wwd.getView().setFarClipDistance(modelViewFarClip);
             bmv.setZoom(900000);
             bmv.setPitch(Angle.fromDegrees(45));
             
             // change class for inputHandler
             Configuration.setValue(AVKey.INPUT_HANDLER_CLASS_NAME, 
                         BasicModelViewInputHandler3.class.getName());
 
             // re-setup control layer handler
             wwd.addSelectListener(new ViewControlsSelectListener(wwd, viewControlsLayer));
             
         } // model view mode
         
     } // setupView
     
     //Set view to either ECI or ECEF
     public void setViewModeECI(boolean viewModeECI)
     {
         this.viewModeECI = viewModeECI;
         
         // take care of which view mode to use
         if(viewModeECI)
         {
             // update stars
             starsLayer.setLongitudeOffset(Angle.fromDegrees(-eciLayer.getRotateECIdeg()));
         }
         else
         {
             starsLayer.setLongitudeOffset(Angle.fromDegrees(0.0)); // reset to normal
         }
         
     }
     
     //Add the layers to the World Wind applet
     public void setUpLayers()
     {
             // Add a renderable layer for application labels -- probably not necessary
             this.labelsLayer = new RenderableLayer();
             this.labelsLayer.setName("Labels");
             insertBeforeLayerName(this.wwd, this.labelsLayer, "Compass");
 
             //Add the view controls layer
             viewControlsLayer = new ViewControlsLayer();
             viewControlsLayer.setLayout(AVKey.VERTICAL); // VOTD change from LAYOUT_VERTICAL (9/june/09)
             viewControlsLayer.setScale(6/10d);
             viewControlsLayer.setPosition(AVKey.SOUTHEAST); // put it on the right side
             viewControlsLayer.setLocationOffset( new Vec4(15,35,0,0));
             viewControlsLayer.setEnabled(true); // turn off by default
             viewControlsLayer.setShowVeControls(false); //Turn off useless buttons
             m.getLayers().add(1,viewControlsLayer);
             wwd.addSelectListener(new ViewControlsSelectListener(wwd, viewControlsLayer));
             
             // add ECI Layer -- FOR SOME REASON IF BEFORE EFEF and turned off ECEF Orbits don't show up!! Coverage effecting this too, strange
             eciLayer = new ECIRenderableLayer(currentJulianDate.getMJD()); // create ECI layer
             orbitModel = new OrbitModelRenderable(satHash, wwd.getModel().getGlobe());
             eciLayer.addRenderable(orbitModel); // add renderable object
             eciLayer.setCurrentMJD(currentJulianDate.getMJD()); // update time again after adding renderable
             eciRadialGrid.setShowGrid(false); //turn off grid
             eciLayer.addRenderable(eciRadialGrid); // add grid (optional if it is on or not)
             m.getLayers().add(0,eciLayer); // add ECI Layer
             
             // add ECEF Layer
             ecefLayer = new ECEFRenderableLayer(); // create ECEF layer
             ecefModel = new ECEFModelRenderable(satHash, wwd.getModel().getGlobe());
             ecefLayer.addRenderable(ecefModel); // add renderable object
             ecefLayer.setEnabled(false); //Default ECI not ECEF
             m.getLayers().add(ecefLayer); // add ECEF Layer
             
             // add EcefTimeDepRenderableLayer layer
             timeDepLayer = new EcefTimeDepRenderableLayer(currentJulianDate.getMJD(),sun);
             m.getLayers().add(timeDepLayer);
             
             // Add the status bar
             StatusBar statusBar = new StatusBar();
             this.getContentPane().add(statusBar, BorderLayout.PAGE_END);
 
             // Forward events to the status bar to provide the cursor position info.
             statusBar.setEventSource(this.wwd);
 
             // Setup a select listener for the worldmap click-and-go feature
             this.wwd.addSelectListener(new ClickAndGoSelectListener(this.wwd, WorldMapLayer.class));
             
              for (Layer layer : m.getLayers())
             {
             if (layer instanceof LandsatI3)
             {
                 ((TiledImageLayer) layer).setDrawBoundingVolumes(false);
                 ((TiledImageLayer) layer).setEnabled(false);
             }
             if (layer instanceof CompassLayer)
             {
                 ((CompassLayer) layer).setShowTilt(true);
                 ((CompassLayer) layer).setEnabled(true);
             }
             if (layer instanceof PlaceNameLayer)
             {
                 ((PlaceNameLayer) layer).setEnabled(false); // off
             }
             if (layer instanceof WorldMapLayer)
             {
                 ((WorldMapLayer) layer).setEnabled(false); // off
             }
             if (layer instanceof USGSUrbanAreaOrtho)
             {
                 ((USGSUrbanAreaOrtho) layer).setEnabled(false); // off
             }
             // save star layer
             if (layer instanceof StarsLayer)
             {
                 starsLayer = (StarsLayer) layer;
                 
                 // for now just enlarge radius by a factor of 10
                 starsLayer.setRadius(starsLayer.getRadius()*10.0);
             }
             if(layer instanceof CountryBoundariesLayer)
             {
                 ((CountryBoundariesLayer) layer).setEnabled(false); // off by default
             }
             } // for layers
     }
     
     //Adds user inputs to scenario, including satellites as well as scenario time if needed
     public void inputSatellites()
    {
             //Read satellites
             try{
             input = new OnlineInput("http://localhost:8080/parameters_test.html"); //Reads user input
             int n = input.getSize(); //number of satellites in input
             for (int i = 0; i <n; i++)
             {
                 addCustomSat(input.getSatelliteName(i)); //Add each satellite
             }
             reader = new StkEphemerisReader(); //initialize ephemeris reader
             double tempTime;
             double maxTempTime = 0;
             double date = this.getCurrentJulTime();
             double currentMJDtime = date - AstroConst.JDminusMJD;
             double deltaTT2UTC = Time.deltaT(currentMJDtime); // = TT - UTC
             for (int i = 0; i <n; i++)
             {	//For each satellite
                     AbstractSatellite S = satHash.get(input.getSatelliteName(i));
                     S.setGroundTrackIni2False(); 
                     S.setPlot2DFootPrint(false); //No footprints (ugly)
                     S.setShow3DFootprint(false);
                     //Set color
                     if (input.getColor(i).startsWith("b"))
                     {
                             S.setSatColor(Color.BLUE);
                     }
                     else if (input.getColor(i).startsWith("g"))
                     {
                             S.setSatColor(Color.GREEN);
                     }
                     else if (input.getColor(i).startsWith("r"))
                     {
                             S.setSatColor(Color.RED);
                     }
                     else if (input.getColor(i).startsWith("y"))
                     {
                             S.setSatColor(Color.YELLOW);
                     }
                     else if (input.getColor(i).startsWith("w"))
                     {
                             S.setSatColor(Color.WHITE);
                     }
                     else if (input.getColor(i).startsWith("p"))
                     {
                             S.setSatColor(Color.PINK);
                     }
                     else if (input.getColor(i).startsWith("o"))
                     {
                             S.setSatColor(Color.ORANGE);
                     }
                     else
                     {
                             S.setSatColor(Color.MAGENTA);
                     }
                     vector = reader.readStkEphemeris(input.getEphemerisLocation(i)); //Read ephemeris into vector
                     tempTime = StkEphemerisReader.convertScenarioTimeString2JulianDate(reader.getScenarioEpoch() + " UTC"); //Save ephemeris time
                     if(!overrideTime || ignoreOverride) //If not override time, then time needs to be updated
                     {
                         if(tempTime < time) //If earlier ephemeris time
                         {
                             time = tempTime; //Set time to earlier time
                         }
                     }
                     tempTime = vector.get(vector.size()-1).state[0] - deltaTT2UTC;
                     if(tempTime > maxTempTime) //If this ephemeris time is greater than others
                     {
                         maxTempTime = tempTime; //Set as max ephemeris time
                     }
                     S.setEphemeris(vector); //set ephemeris for each satellite
                     // set default 3d model and turn on the use of 3d models: CURRENTLY UNAVAILABLE
 //                    S.setThreeDModelPath("globalstar/Globalstar.3ds");
 //                    S.setUse3dModel(true);
                     if (input.getModelCentered(i)) //BAD
                     {
                            //statusDisplay.setText("Can't do that yet!");
                     }
                     else
                     {
                             //dont do anything!
                     }
             }
             double scenarioTime = input.getTime(); //Get user input time
             if(scenarioTime>=time && scenarioTime < maxTempTime|| overrideTime) //If user input time is greater than time in ephemeris
             {
            time = scenarioTime; //set time to user input time
                 if(!overrideTime || ignoreOverride) //If time needs to be updated
                 {
                 setTime(time);
                play = true;
                 }
             //step one second forward than go back to original time...fixes dissapearance? 
             double temp = currentJulianDate.getJulianDate();
             currentJulianDate.addSeconds(1.0);
             updateTime();
             GregorianCalendar gc = Time.convertJD2Calendar(temp);
             setTime(gc.getTimeInMillis());
             statusDisplay.setText("Satellites Added");
             inputSat = true;
             ignoreOverride = false;
             }
             else if(scenarioTime == 0.0) //Means user input read a bad time string
             {
                 statusDisplay.setText("Incorrect time requested");
                 inputSat = false; //No satellites added
                 if(!overrideTime || ignoreOverride) //If time needs to be changed
                 {
                 currentJulianDate.update2CurrentTime(); //Set to current time
                 setTime(currentJulianDate.getJulianDate());
                 }
                 play = false;
                 ignoreOverride = true;
                 try //remove satellites that were added: don't display satellites if time is bad!
                 {satHash.clear();}
                 catch(Exception e)
                 {}
             }
             else
             {//Outside ephemeris time
                 statusDisplay.setText("Requested time not within ephemeris range");
                 if(!overrideTime || ignoreOverride) //If time needs to be updated
                 {
                 time = scenarioTime; //Set time to requested time
                 setTime(time);
                 }
                 inputSat = false; //No satellites
                 play = false;
                 ignoreOverride = true;
             }
             }
             catch(Exception e) //URL given in inputs does not connect to STK ephemeris file
             {statusDisplay.setText("No satellites found");
             inputSat = false; //No satellites
             if(!overrideTime || ignoreOverride) //If time needs to be updated
             {
             currentJulianDate.update2CurrentTime(); //Set to current time
             setTime(currentJulianDate.getJulianDate());
             }
             play = false;
             ignoreOverride = true;}
     }
 } //End of program
 
