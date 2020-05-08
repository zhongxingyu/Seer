 package manifest.application;
 
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 import manifest.application.intent.Intent;
 import manifest.application.metaData.MetaData;
 
 public class Activity extends ApplicationComponent {
 
     private String action;
     private String category;
     private String data;
     private String metaData;
     private int priority;
     private boolean allowTaskReparenting = false;
     private boolean alwaysRetainTaskState = false;
     private boolean clearTaskOnLaunch = false;
 
     /**
      * @return the intents
      */
     public ArrayList<Intent> getIntents() {
         return intents;
     }
 
     /**
      * @param intents the intents to set
      */
     public void setIntents(ArrayList<Intent> intents) {
         this.intents = intents;
     }
 
     /**
      * @return the metaDatas
      */
     public ArrayList<MetaData> getMetaDatas() {
         return metaDatas;
     }
 
     /**
      * @param metaDatas the metaDatas to set
      */
     public void setMetaDatas(ArrayList<MetaData> metaDatas) {
         this.metaDatas = metaDatas;
     }
     public enum ConfigChanges {
         mcc, mnc, locale, touchscreen, keyboard, keyboardHidden, navigation, screenLayout, fontScale, uiMode, orientation, screenSize, smallestScreenSize;
     }
     private ConfigChanges[] configChanges;
     private boolean excludeFromRecents = false;
     private boolean finishOnTaskLaunch = false;
     private boolean hardwareAccerlerated = false;
 
     public enum Launchmode {
         standard, singleTop, singleTask, singleInstance;
     }
     private Launchmode launchmode = Launchmode.standard;
     private boolean multiprocess = false;
     private boolean noHistory = false;
     private String parentActivityName = null;
     private String process = null;
 
     public enum ScreenOrientation {
         unspecified, behind, landscape, portrait, reverseLandscape, reversePortrait,
         sensorLandscape, sensorPortrait, userLandscape, userPortrait, sensor, fullSensor, nosensor, user, fullUser, locked;
     }
     private ScreenOrientation screenOrientation;
     private boolean stateNotNeeded = false;
     private String taskAffinity = null;
 
     public enum Theme {
         resource, theme;
     }
     private String theme;
 
     public enum UiOptions {
         none, splitActionBarWhenNarrow;
     }
     private UiOptions uiOptions;
 
     public enum WindowSoftInputMode {
         stateUnspecified, stateUnchanged, stateHidden, stateAlwaysHidden,
         stateVisible, stateAlwaysVisible, adjustUnspecified, adjustResize, adjustPan;
     }
     private WindowSoftInputMode windowSoftInputMode;
     
     private ArrayList<Intent> intents = new ArrayList();
     private ArrayList<MetaData> metaDatas = new ArrayList();
     
     /**
      * @return the launchmode
      */
     public Launchmode getLaunchmode() {
         return launchmode;
     }
 
     /**
      * @param launchmode the launchmode to set
      */
     public void setLaunchmode(Launchmode launchmode) {
         this.launchmode = launchmode;
     }
 
     /**
      * @return the allowTaskReparenting
      */
     public boolean isAllowTaskReparenting() {
         return allowTaskReparenting;
     }
 
     /**
      * @param allowTaskReparenting the allowTaskReparenting to set
      */
     public void setAllowTaskReparenting(boolean allowTaskReparenting) {
         this.allowTaskReparenting = allowTaskReparenting;
     }
 
     /**
      * @return the alwaysRetainTaskState
      */
     public boolean isAlwaysRetainTaskState() {
         return alwaysRetainTaskState;
     }
 
     /**
      * @param alwaysRetainTaskState the alwaysRetainTaskState to set
      */
     public void setAlwaysRetainTaskState(boolean alwaysRetainTaskState) {
         this.alwaysRetainTaskState = alwaysRetainTaskState;
     }
 
     /**
      * @return the clearTaskOnLaunch
      */
     public boolean isClearTaskOnLaunch() {
         return clearTaskOnLaunch;
     }
 
     /**
      * @param clearTaskOnLaunch the clearTaskOnLaunch to set
      */
     public void setClearTaskOnLaunch(boolean clearTaskOnLaunch) {
         this.clearTaskOnLaunch = clearTaskOnLaunch;
     }
 
     /**
      * @return the excludeFromRecents
      */
     public boolean isExcludeFromRecents() {
         return excludeFromRecents;
     }
 
     /**
      * @param excludeFromRecents the excludeFromRecents to set
      */
     public void setExcludeFromRecents(boolean excludeFromRecents) {
         this.excludeFromRecents = excludeFromRecents;
     }
 
     /**
      * @return the finishOnTaskLaunch
      */
     public boolean isFinishOnTaskLaunch() {
         return finishOnTaskLaunch;
     }
 
     /**
      * @param finishOnTaskLaunch the finishOnTaskLaunch to set
      */
     public void setFinishOnTaskLaunch(boolean finishOnTaskLaunch) {
         this.finishOnTaskLaunch = finishOnTaskLaunch;
     }
 
     /**
      * @return the hardwareAccerlerated
      */
     public boolean isHardwareAccerlerated() {
         return hardwareAccerlerated;
     }
 
     /**
      * @param hardwareAccerlerated the hardwareAccerlerated to set
      */
     public void setHardwareAccerlerated(boolean hardwareAccerlerated) {
         this.hardwareAccerlerated = hardwareAccerlerated;
     }
 
     /**
      * @return the multiprocess
      */
     public boolean isMultiprocess() {
         return multiprocess;
     }
 
     /**
      * @param multiprocess the multiprocess to set
      */
     public void setMultiprocess(boolean multiprocess) {
         this.multiprocess = multiprocess;
     }
 
     /**
      * @return the noHistory
      */
     public boolean isNoHistory() {
         return noHistory;
     }
 
     /**
      * @param noHistory the noHistory to set
      */
     public void setNoHistory(boolean noHistory) {
         this.noHistory = noHistory;
     }
 
     /**
      * @return the parentActivityName
      */
     public String getParentActivityName() {
         return parentActivityName;
     }
 
     /**
      * @param parentActivityName the parentActivityName to set
      */
     public void setParentActivityName(String parentActivityName) {
         this.parentActivityName = parentActivityName;
     }
 
     /**
      * @return the process
      */
     public String getProcess() {
         return process;
     }
 
     /**
      * @param process the process to set
      */
     public void setProcess(String process) {
         this.process = process;
     }
 
     /**
      * @return the stateNotNeeded
      */
     public boolean isStateNotNeeded() {
         return stateNotNeeded;
     }
 
     /**
      * @param stateNotNeeded the stateNotNeeded to set
      */
     public void setStateNotNeeded(boolean stateNotNeeded) {
         this.stateNotNeeded = stateNotNeeded;
     }
 
     /**
      * @return the taskAffinity
      */
     public String getTaskAffinity() {
         return taskAffinity;
     }
 
     /**
      * @param taskAffinity the taskAffinity to set
      */
     public void setTaskAffinity(String taskAffinity) {
         this.taskAffinity = taskAffinity;
     }
 
     /**
      * @return the screenOrientation
      */
     public ScreenOrientation getScreenOrientation() {
         return screenOrientation;
     }
 
     /**
      * @param screenOrientation the screenOrientation to set
      */
     public void setScreenOrientation(String screenOrientation) {
         this.screenOrientation = ScreenOrientation.valueOf(screenOrientation);
     }
 
     /**
      * @return the theme
      */
     public String getTheme() {
         return theme;
     }
 
     /**
      * @param theme the theme to set
      */
     public void setTheme(String theme) {
         this.theme = theme;
     }
 
     /**
      * @return the uiOptions
      */
     public UiOptions getUiOptions() {
         return uiOptions;
     }
 
     /**
      * @param uiOptions the uiOptions to set
      */
     public void setUiOptions(String uiOptions) {
         this.uiOptions = UiOptions.valueOf(uiOptions);
     }
 
     /**
      * @return the windowSoftInputMode
      */
     public WindowSoftInputMode getWindowSoftInputMode() {
         return windowSoftInputMode;
     }
 
     /**
      * @param windowSoftInputMode the windowSoftInputMode to set
      */
     public void setWindowSoftInputMode(String windowSoftInputMode) {
         this.windowSoftInputMode =WindowSoftInputMode.valueOf(windowSoftInputMode);
     }
 
     /**
      * @return the configChanges
      */
     public ConfigChanges[] getConfigChanges() {
         return configChanges;
     }
 
     /**
      * @param configChanges the configChanges to set
      */
     public void setConfigChanges(String changes) {
         try {
             int i = 0;
             StringTokenizer st = new StringTokenizer(changes, "|");
             while (st.hasMoreTokens()) {
                 i++;
                 st.nextToken().trim();
             }
             this.configChanges = new ConfigChanges[i];
             i = 0;
             st = new StringTokenizer(changes, "|");
             while (st.hasMoreTokens()) {
                 String captured = st.nextToken().trim();
                 this.getConfigChanges()[i] = ConfigChanges.valueOf(captured);
                 i++;
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
     /**
      * @param launchmode the launchmode to set
      */
     public void setLaunchmode(String launchmode) {
        this.setLaunchmode(Launchmode.valueOf(launchmode));
     }
 
     /**
      * @return the action
      */
     public String getAction() {
         return action;
     }
 
     /**
      * @param action the action to set
      */
     public void setAction(String action) {
         this.action = action;
     }
 
     /**
      * @return the category
      */
     public String getCategory() {
         return category;
     }
 
     /**
      * @param category the category to set
      */
     public void setCategory(String category) {
         this.category = category;
     }
 
     /**
      * @return the data
      */
     public String getData() {
         return data;
     }
 
     /**
      * @param data the data to set
      */
     public void setData(String data) {
         this.data = data;
     }
 
     /**
      * @return the metaData
      */
     public String getMetaData() {
         return metaData;
     }
 
     /**
      * @param metaData the metaData to set
      */
     public void setMetaData(String metaData) {
         this.metaData = metaData;
     }
 
     /**
      * @return the priority
      */
     public int getPriority() {
         return priority;
     }
 
     /**
      * @param priority the priority to set
      */
     public void setPriority(int priority) {
         this.priority = priority;
     }
 }
