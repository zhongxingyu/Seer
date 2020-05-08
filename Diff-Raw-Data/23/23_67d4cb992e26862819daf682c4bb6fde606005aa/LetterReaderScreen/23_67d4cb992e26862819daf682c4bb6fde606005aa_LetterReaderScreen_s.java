 package org.edc.ht.screens;
 
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.lcdui.Font;
 import javax.microedition.midlet.MIDlet;
 
 import org.edc.j2me.audio.AudioClipType;
 import org.edc.j2me.audio.AudioPlayer;
 import org.edc.j2me.audio.ClasspathResourceAudioPlayer;
 
 public class LetterReaderScreen extends HighlightEnabledTextAreaTestScreen {
 
     private final AudioPlayer audioPlayer;
 
     public LetterReaderScreen(MIDlet midlet, Displayable mainMenu, String text, Font font) {
         super(midlet, mainMenu, text, font);
         setTitle("Letter Reader");
         audioPlayer = new ClasspathResourceAudioPlayer(100);
     }
 
    private void playAudio() {
         // TODO: figure out where to code case stuff (should be in audio player)
         // Also, audio player should maybe called TokenAudioReader or something like that.
        String text = textArea.getTokenText(highlightedTokenIdx).toLowerCase();
         audioPlayer.playAudio(AudioClipType.LETTER, text);
     }
 
     // override... this could be less copy/paste...
     protected void initTokenHighlighting() {
         final int tokenCount = textArea.tokenCount();
         timer = new Timer();
         TimerTask task = new TimerTask() {
             public void run() {
                 if (highlightedTokenIdx >= tokenCount) {
                     cancel();
                 } else {
                     repaint();
                    playAudio();
                     highlightedTokenIdx++;
                 }
             }
         };
         timer.schedule(task, 500l, 1000l);
     }
 
     protected void cancelTokenHighlighting() {
         super.cancelTokenHighlighting();
         if (audioPlayer != null) {
             audioPlayer.cleanup();
         }
     }
 
 }
