 package com.oryzone.mvdetector;
 
 import com.oryzone.mvdetector.differenceStrategy.RgbDifferenceStrategy;
 import java.awt.Dimension;
 import java.io.Serializable;
 import java.util.prefs.Preferences;
 
 /**
  * Class used to serialize and deserialize the Detector options
  * 
  * @author Luciano Mammino, Andrea Mangano
  * @version 1.0
  */
 public class DetectorOptions implements Serializable
 {
     /**
      * Preferences keys
      */
     protected final static String
             OPT_WARNINGSENSIBILITY = "warningSensibility",
             OPT_WARNINGDURATION = "warningDuration",
             OPT_FRAMEDIMENSIONWIDTH = "frameDimensionWidth",
             OPT_FRAMEDIMENSIONHEIGHT = "frameDimensionHeight",
             OPT_USECOLOREDDIFFERENCE = "useColoredDifference",
             OPT_ACTIONPLAYSYSTEMBEEP_ENABLED = "action_playSystemBeepEnabled",
             OPT_ACTIONSAVEFRAMES_ENABLED = "action_saveFramesEnabled",
             OPT_ACTIONSAVEFRAMES_FACEDETECTION_ENABLED = "action_saveFramesFaceDetectionEnabled",
             OPT_ACTIONREGISTERLOG_ENABLED = "action_registerLogEnabled",
             OPT_ACTIONEXECUTECOMMAND_ENABLED = "action_executeCommandEnabled",
             OPT_ACTIONEXECUTECOMMAND_TEXT = "action_executeCommandText";
 
     /**
      * Serial version UID used to serialize the class (automatically generated
      * by Eclipse)
      */
     private static final long serialVersionUID = 879312245269330066L;
     
     
     /**
      * The sensibility of the warning level. It is the frames difference
      * threshold limit after wich the warning mode starts.
      */
     protected float warningSensibility;
     
     /**
      * The duration of the warning mode in seconds
      */
     protected int warningDuration;
 
     /**
      * The size of the frame
      */
     protected Dimension frameDimension;
 
     /**
      * If <code>true</code> uses the {@link RgbDifferenceStrategy} as frame
      * differencing method.
      */
     protected boolean useColoredDifference;
     
     /**
      * Flag used to determinate wheter the action play beep sound is enabled
      */
     protected boolean actionPlayBeepSoundEnabled;
     
     /**
      * Flag used to determinate wheter the action save frames is enabled
      */
     protected boolean actionSaveFramesEnabled;
     
     /**
      * Flag used to determinate wheter the action save frames with face detection
      * is enabled
      */
     protected boolean actionSaveFramesUseFaceDetection;
     
     /**
      * Flag used to determinate wheter the action register log is enabled
      */
     protected boolean actionRegisterLogEnabled;
     
     /**
      * Flag used to determinate id the action execute command is enabled
      */
     protected boolean actionExecuteCommandEnabled;
     
     /**
      * The command string to be executed
      */
     protected String actionExecuteCommandText;
     
 
     /**
      * Constuctor. Creates an instance of the DetectorOptions with the default
      * options
      */
     public DetectorOptions()
     {
         this.loadDefaults();
     }
 
 
     /**
      * Loads the default options
      */
     public final void loadDefaults()
     {
         this.warningSensibility = .05f;
         this.warningDuration = 5;
         this.frameDimension = new Dimension(640, 480);
         this.useColoredDifference = false;
         this.actionPlayBeepSoundEnabled = true;
         this.actionSaveFramesEnabled = false;
         this.actionSaveFramesUseFaceDetection = false;
         this.actionRegisterLogEnabled = true;
         this.actionExecuteCommandEnabled = false;
         this.actionExecuteCommandText = "";
     }
 
     /**
      * Gets the preference object associated to the program
      * @return
      */
     protected Preferences getPreferences()
     {
         return Preferences.userNodeForPackage(Main.class);
     }
 
 
     /**
      * Loads the options frome the stored options
      */
     public void load()
     {
         Preferences pref = this.getPreferences();
         this.warningSensibility = pref.getFloat(DetectorOptions.OPT_WARNINGSENSIBILITY, this.warningSensibility);
         this.warningDuration = pref.getInt(DetectorOptions.OPT_WARNINGDURATION, this.warningDuration);
         this.setFrameDimension(new Dimension(pref.getInt(DetectorOptions.OPT_FRAMEDIMENSIONWIDTH, this.getFrameDimension().width), pref.getInt(DetectorOptions.OPT_FRAMEDIMENSIONHEIGHT, this.getFrameDimension().height)));
         this.setUseColoredDifference(pref.getBoolean(DetectorOptions.OPT_USECOLOREDDIFFERENCE, this.usingColoredDifference()));
         this.setActionPlayBeepSoundEnabled(pref.getBoolean(DetectorOptions.OPT_ACTIONPLAYSYSTEMBEEP_ENABLED, this.isActionPlayBeepSoundEnabled())); 
         this.setActionSaveFramesEnabled(pref.getBoolean(DetectorOptions.OPT_ACTIONSAVEFRAMES_ENABLED, this.isActionSaveFramesEnabled()));
         this.setActionSaveFramesUseFaceDetection(pref.getBoolean(DetectorOptions.OPT_ACTIONSAVEFRAMES_FACEDETECTION_ENABLED, this.isActionSaveFramesUseFaceDetection()));
         this.setActionRegisterLogEnabled(pref.getBoolean(DetectorOptions.OPT_ACTIONREGISTERLOG_ENABLED, this.isActionRegisterLogEnabled()));
         this.setActionExecuteCommandEnabled(pref.getBoolean(DetectorOptions.OPT_ACTIONEXECUTECOMMAND_ENABLED, this.isActionExecuteCommandEnabled()));
         this.setActionExecuteCommandText(pref.get(DetectorOptions.OPT_ACTIONEXECUTECOMMAND_TEXT, this.getActionExecuteCommandText()));
     }
 
 
     /**
      * Saves the options
      */
     public void save()
     {
         Preferences pref = this.getPreferences();
         pref.putFloat(DetectorOptions.OPT_WARNINGSENSIBILITY, this.warningSensibility);
         pref.putInt(DetectorOptions.OPT_WARNINGDURATION, this.warningDuration);
         pref.putInt(DetectorOptions.OPT_FRAMEDIMENSIONWIDTH, this.getFrameDimension().width);
         pref.putInt(DetectorOptions.OPT_FRAMEDIMENSIONHEIGHT, this.getFrameDimension().height);
         pref.putBoolean(DetectorOptions.OPT_USECOLOREDDIFFERENCE, this.usingColoredDifference());
        pref.putBoolean(DetectorOptions.OPT_ACTIONPLAYSYSTEMBEEP_ENABLED, this.isActionPlayBeepSoundEnabled());
         pref.putBoolean(DetectorOptions.OPT_ACTIONSAVEFRAMES_ENABLED, this.isActionSaveFramesEnabled());
         pref.putBoolean(DetectorOptions.OPT_ACTIONSAVEFRAMES_FACEDETECTION_ENABLED, this.isActionSaveFramesUseFaceDetection());
         pref.putBoolean(DetectorOptions.OPT_ACTIONREGISTERLOG_ENABLED, this.isActionRegisterLogEnabled());
         pref.putBoolean(DetectorOptions.OPT_ACTIONEXECUTECOMMAND_ENABLED, this.isActionExecuteCommandEnabled());
         pref.put(DetectorOptions.OPT_ACTIONEXECUTECOMMAND_TEXT, this.getActionExecuteCommandText());
     }
 
 
     /**
      * @return the warningSensibility
      */
     public float getWarningSensibility()
     {
         return warningSensibility;
     }
 
 
     /**
      * @param warningSensibility the warningSensibility to set
      */
     public DetectorOptions setWarningSensibility(float warningSensibility)
     {
         this.warningSensibility = warningSensibility;
         return this;
     }
 
 
     /**
      * @return the warningDuration
      */
     public int getWarningDuration()
     {
         return warningDuration;
     }
 
 
     /**
      * @param warningDuration the warningDuration to set
      */
     public DetectorOptions setWarningDuration(int warningDuration)
     {
         this.warningDuration = warningDuration;
         return this;
     }
 
 
     /**
      * The size of the frame
      * @return the frameDimension
      */
     public Dimension getFrameDimension()
     {
         return frameDimension;
     }
 
 
     /**
      * The size of the frame
      * @param frameDimension the frameDimension to set
      */
     public DetectorOptions setFrameDimension(Dimension frameDimension)
     {
         this.frameDimension = frameDimension;
         return this;
     }
 
 
     /**
      * If <code>true</code> uses the {@link RgbDifferenceStrategy} as frame
      * differencing method.
      * @return the useColoredDifference
      */
     public boolean usingColoredDifference()
     {
         return useColoredDifference;
     }
 
 
     /**
      * If <code>true</code> uses the {@link RgbDifferenceStrategy} as frame
      * differencing method.
      * @param useColoredDifference the useColoredDifference to set
      */
     public DetectorOptions setUseColoredDifference(boolean useColoredDifference)
     {
         this.useColoredDifference = useColoredDifference;
         return this;
     }
 
 
     /**
      * Checks if the action execute command is enabled
      * @return a boolean value
      */
     public boolean isActionExecuteCommandEnabled()
     {
         return actionExecuteCommandEnabled;
     }
 
 
     /**
      * Enable or disable the action execute command
      * @param actionExecuteCommandEnabled <code>true</code> to enable, 
      * <code>false</code> to disable
      * @return 
      */
     public DetectorOptions setActionExecuteCommandEnabled(boolean actionExecuteCommandEnabled)
     {
         this.actionExecuteCommandEnabled = actionExecuteCommandEnabled;
         return this;
     }
 
 
     /**
      * Gets the command to be executed when a warning occurs
      * @return String
      */
     public String getActionExecuteCommandText()
     {
         return actionExecuteCommandText;
     }
 
 
     /**
      * Sets the command to be executed when a warning occurs 
      * @param actionExecuteCommandText the string of the command
      * @return  
      */
     public DetectorOptions setActionExecuteCommandText(String actionExecuteCommandText)
     {
         this.actionExecuteCommandText = actionExecuteCommandText;
         return this;
     }
 
     /**
      * checks if the action play beep sound is enabled
      * @return 
      */
     public boolean isActionPlayBeepSoundEnabled()
     {
         return actionPlayBeepSoundEnabled;
     }
 
 
     /**
      * Enables or disables the action play beep sound
      * @param actionPlayBeepSoundEnabled
      * @return 
      */
     public DetectorOptions setActionPlayBeepSoundEnabled(boolean actionPlayBeepSoundEnabled)
     {
         this.actionPlayBeepSoundEnabled = actionPlayBeepSoundEnabled;
         return this;
     }
 
     
     /**
      * Checks if the register log action is enabled
      * @return 
      */
     public boolean isActionRegisterLogEnabled()
     {
         return actionRegisterLogEnabled;
     }
 
 
     /**
      * Enables or disables the action register log
      * @param actionRegisterLogEnabled
      * @return 
      */
     public DetectorOptions setActionRegisterLogEnabled(boolean actionRegisterLogEnabled)
     {
         this.actionRegisterLogEnabled = actionRegisterLogEnabled;
         return this;
     }
 
 
     /**
      * Checks if the save frames action is enabled
      * @return 
      */
     public boolean isActionSaveFramesEnabled()
     {
         return actionSaveFramesEnabled;
     }
 
 
     /**
      * Enables or disables the action save frames
      * @param actionSaveFramesEnabled
      * @return 
      */
     public DetectorOptions setActionSaveFramesEnabled(boolean actionSaveFramesEnabled)
     {
         this.actionSaveFramesEnabled = actionSaveFramesEnabled;
         return this;
     }
 
 
     /**
      * Checks if face detection is enabled
      * @return 
      */
     public boolean isActionSaveFramesUseFaceDetection()
     {
         return actionSaveFramesUseFaceDetection;
     }
 
     
     /**
      * Enables or disables face detection
      * @param actionSaveFramesUseFaceDetection
      * @return 
      */
     public DetectorOptions setActionSaveFramesUseFaceDetection(boolean actionSaveFramesUseFaceDetection)
     {
         this.actionSaveFramesUseFaceDetection = actionSaveFramesUseFaceDetection;
         return this;
     }
        
 
 }
