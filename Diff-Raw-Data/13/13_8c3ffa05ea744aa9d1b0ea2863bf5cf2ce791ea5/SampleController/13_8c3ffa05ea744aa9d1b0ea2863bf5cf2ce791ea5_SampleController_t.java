 
 package timer;
 
 import java.net.URL;
 import java.util.ResourceBundle;
 import javafx.animation.KeyFrame;
 import javafx.animation.Timeline;
 import javafx.event.ActionEvent;
 import javafx.event.EventHandler;
 import javafx.fxml.FXML;
 import javafx.fxml.Initializable;
 import javafx.scene.control.Button;
 import javafx.scene.control.Label;
 import javafx.scene.control.MenuItem;
 import javafx.scene.control.RadioButton;
 import javafx.scene.control.Tab;
 import javafx.scene.control.TextField;
 import javafx.scene.control.ToggleGroup;
 import javafx.scene.media.Media;
 import javafx.scene.media.MediaPlayer;
 import javafx.util.Duration;
 
 
 
 public class SampleController implements Initializable {
     @FXML
     private TextField maxTimeField;
     @FXML
     private Button startButton;
     @FXML
     private Button pauseButton;
     @FXML
     private Button resetButton;
     @FXML
     public Label timerLabel;
     @FXML
     private MenuItem closeButton;
     @FXML
     private TextField fromField;
     @FXML
     private TextField toField;
     @FXML
     private RadioButton oneSoundRB;
     @FXML
     private ToggleGroup soundRButtons;
     @FXML
     private RadioButton twoSoundRB;
     @FXML
     private RadioButton threeSoundRB;
     @FXML
     private RadioButton fourSoundRB;
     @FXML
     private RadioButton fiveSoundRB;    
     @FXML
     private TextField iterationField;
     @FXML
     private TextField onIntervalField;
     @FXML
     private TextField offIntervalField;
     @FXML
     private Button iStartButton;
     @FXML
     private Button iPauseButton;
     @FXML
     private Button iResetButton;
     @FXML
     private Label iTimeLabel;
     @FXML
     private Tab timerTab;
     @FXML
     private Tab iTimerTab;
     
     /********** global variables **********/
     private int hour = 0;
     private int minute = 0;
     private int second = 0;
     
     private boolean keep_going = true;   
     Timeline timeline;
     Timeline itimeline;
     private int fromInterval;
     private int toInterval;
     private int[] interval;
     int nextSeconds;
     int inextSeconds;
     int nowSeconds;
     int inowSeconds;
     
     String[] soundFiles;
     boolean on = true;
     
    
     /**************************************/
         
     /************ user methods *************/
     
     /**
      * reduces displayed hour count by one
      * @return 
      */
     private boolean reduceHour(){
         if(hour > 0){
             hour = hour - 1;
             return true;
         } else {
             return false;
         }
     }
     
     /**
      * reduces displayed minute count by one
      * @return 
      */
     private boolean reduceMinute(){
         if (minute == 0){
             if(reduceHour()){
                 minute = 59;
                 return true;
             } else {
                 return false;
             }
         } else {
             minute = minute - 1;
             return true;
         }
     }
     
     /**
      * reduces global second count by one
      */
     public boolean reduceSecond(Label label){
         if(hour == 0 & minute == 0 & second == 0){
             keep_going = false;
             return false;
         }
         if(second == 0){
             if(reduceMinute()){
                 second = 59;                 
             } 
         } else {
             second = second - 1;            
         }    
         displayTime(label);
         return true;
     }
     
     /**
      * displays the wanted time on the display
      * @param hours    
      * @param minutes
      * @param seconds 
      */
     public void displayTime(Label label){
         String h;
         String m;
         String s;
         
         if(hour < 10){
             h = "0" + hour;
         } else {
             h = String.valueOf(hour);
         }
         
         if(minute < 10){
             m = "0" + minute;
         } else {
             m = String.valueOf(minute);
         }
         
         if(second < 10){
             s = "0" + second;
         } else {
             s = String.valueOf(second);
         }
         
         String time = h + ":" + m + ":" + s;
         
         label.setText(time);
     }
     
     /**
      * takes the Media and creates MediaPlayer
      * plays the Mediaplayer
      * @param m the Media
      */
     public void play(Media m) {
         if( m != null ){
             MediaPlayer mp = new MediaPlayer(m);
             mp.play();
         }
     }
     
     /**
      * creates Media for the specified file and plays it
      * @param file the name of the playable file in the directory
      */
     public void playSound(String file){
         try{            
             Media sound = new Media(getClass().getResource(file).toString());
             play(sound);            
         } catch(Exception ex){
             ex.printStackTrace();
         }
     }
     
     /**
      * writes array depending of selected count for soundfile names
      * used later to determine the random sound that is played
      * @return sounds[]
      */
     public String[] getSoundFiles() {
         String[] sounds = null;
         
         if(oneSoundRB.isSelected()) {
             sounds = new String[1];
            sounds[0] = "click.mp3";
         }
         
         if(twoSoundRB.isSelected()) {
             sounds = new String[2];
             sounds[0] = "sound1.mp3";
             sounds[1] = "sound2.mp3";
         }
         
         if(threeSoundRB.isSelected()) {
             sounds = new String[3];
             sounds[0] = "sound1.mp3";
             sounds[1] = "sound2.mp3";
             sounds[2] = "sound3.mp3";
         }
         
         if(fourSoundRB.isSelected()) {
             sounds = new String[4];
             sounds[0] = "sound1.mp3";
             sounds[1] = "sound2.mp3";
             sounds[2] = "sound3.mp3";
             sounds[3] = "sound4.mp3";
         }
         
         if(fiveSoundRB.isSelected()) {
             sounds = new String[5];
             sounds[0] = "sound1.mp3";
             sounds[1] = "sound2.mp3";
             sounds[2] = "sound3.mp3";
             sounds[3] = "sound4.mp3";
             sounds[4] = "sound5.mp3";
         } 
         
         return sounds;
     }
     
     /***************************************/
     
     @Override
     public void initialize(URL url, ResourceBundle rb) {
         displayTime(this.timerLabel);        
     }    
 
     @FXML
     private void startButtonFired(ActionEvent event) {
         
         // displaying max time
         hour = Integer.parseInt(this.maxTimeField.getText()) / 60;
         minute = Integer.parseInt(this.maxTimeField.getText()) % 60;
         second = 0;        
         displayTime(this.timerLabel);
         
         // preparing the sound file array
         // contains amount of names depending on selected radio button
         soundFiles = getSoundFiles();         
         
         // total amount of seconds 
         int count = second + (60 * minute) + (3600 * hour);
         nextSeconds = count; // copy for later use
         nowSeconds = count;
         
         // creating the array of possible values
         fromInterval = Integer.parseInt(this.fromField.getText());
         toInterval = Integer.parseInt(this.toField.getText());
         int amount = toInterval - fromInterval + 1;
         interval = new int[amount];
         int j = 0;
         for(int i = fromInterval; i <= toInterval; i++) {
             interval[j] = i;            
             j++;
         }
                
         // countdown as timeline
         timeline = new Timeline( new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent t) {
                 // if equal --> get next random number and substract it
                 if(nextSeconds == nowSeconds) {
                     int nextTone = getNextTimeValue();
                     nextSeconds = nextSeconds - nextTone; 
                 }  
                 
                 // reduce one second
                 reduceSecond(timerLabel);                 
                 nowSeconds = nowSeconds - 1; // nowSeconds is the actual second count left
                 
                 // if after substracting they are equal
                 // -> play a sound
                 if(nextSeconds == nowSeconds) {
                     // select random index of soundFiles array
                     int random = (int) (Math.random() * 10);
                     int randomIndex = random % soundFiles.length;
                     playSound(soundFiles[randomIndex]);
                 }
                 
                 if(nowSeconds == 0) {
                     playSound("endSound.mp3");
                 }
             }
         }));
         
         // starting the timeline        
         timeline.setCycleCount(count);
         timeline.play();    
         
         
     }
 
     /**
      * @return  the random value in the array of possible numbers
      */
     private int getNextTimeValue() {
         int random = (int) (Math.random() * ((toInterval + 1) - fromInterval) + fromInterval);
         int indexRandom = random % interval.length; // get index for array                
         int nextTone = interval[indexRandom]; // get random value from interval
         
         return nextTone;
     }
     
     @FXML
     private void pauseButtonFired(ActionEvent event) {
         if( pauseButton.getText().equals("Pause") ) {
             if(timeline != null){
                 timeline.pause();
                 pauseButton.setText("Resume");
             }            
         } else if ( pauseButton.getText().equals("Resume") ) {
             if(timeline != null){
                 timeline.play();
                 pauseButton.setText("Pause");
             }            
         }
         
     }
 
     @FXML
     private void resetButtonFired(ActionEvent event) {
         hour = 0;
         minute = 0;
         second = 0;
         displayTime(this.timerLabel);
         
         if( timeline != null){
             timeline.stop();
         }        
         pauseButton.setText("Pause");
     }
 
     @FXML
     private void closeButtonFired(ActionEvent event) {
         System.exit(0);
     }
 
     @FXML
     private void iStartButtonFired(ActionEvent event) {
         
         // display iteration * ( on + off )
         int iterations = Integer.parseInt(this.iterationField.getText());
         final int onTime = Integer.parseInt(this.onIntervalField.getText());
         final int offTime = Integer.parseInt(this.offIntervalField.getText());        
         
         final int maxTime = iterations * (onTime + offTime);
         hour = maxTime / 3600;
         minute = maxTime / 60;
         second = maxTime % 60;
         displayTime(this.iTimeLabel);
         
         inowSeconds = maxTime;
         inextSeconds = maxTime;
         
        playSound("onSound.mp3");
        
         itimeline = new Timeline( new KeyFrame(Duration.seconds(1), new EventHandler<ActionEvent>() {
             @Override
             public void handle(ActionEvent t) {
                
                 // if equal --> get next random number and substract it
                 if(inextSeconds == inowSeconds) {
                     int next = 0;
                     if(on){
                         next = onTime;
                         on = false;
                     } else {
                         next = offTime;
                         on = true;
                     }
                     inextSeconds = inextSeconds - next; 
                 }  
                 
                 // reduce one second
                 reduceSecond(iTimeLabel);                 
                 inowSeconds = inowSeconds - 1; // nowSeconds is the actual second count left
                 
                 // if after substracting they are equal
                 // -> play a sound
                 if(inextSeconds == inowSeconds && inowSeconds != 0) {
                     // select random index of soundFiles array
                    if(on){ // means on was triggered
                         playSound("onSound.mp3");
                     } else {
                         playSound("offSound.mp3");
                     }                    
                 }
                 
                 if(inowSeconds == 0) {
                     playSound("endSound.mp3");
                 }
             }
         }));
         
         // starting the timeline        
         itimeline.setCycleCount(maxTime);
         itimeline.play();    
     }
 
     @FXML
     private void iPauseButtonFired(ActionEvent event) {
         if( iPauseButton.getText().equals("Pause") ) {
             if(itimeline != null){
                 itimeline.pause();
                 iPauseButton.setText("Resume");
             }            
         } else if ( iPauseButton.getText().equals("Resume") ) {
             if(itimeline != null){
                 itimeline.play();
                 iPauseButton.setText("Pause");
             }            
         }
     }
 
     @FXML
     private void iResetButtonFired(ActionEvent event) {
         hour = 0;
         minute = 0;
         second = 0;
         displayTime(this.iTimeLabel);
         
         if( itimeline != null){
             itimeline.stop();
         }        
         iPauseButton.setText("Pause");
     }
     
     
 }
