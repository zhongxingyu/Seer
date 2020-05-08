 package com.octopus.music.play;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 public interface AudioPlayer
 {
     void playList( List<File> audioFiles );
 
     public class VLCPlayer implements AudioPlayer
     {
         private static final String vlcOption = "--play-and-exit";
         private final CommandExecutor commandExecutor;
         private final String vlcExeLoc;
 
         VLCPlayer( CommandExecutor commandExecutor, String vlcExeLoc ) {
             this.commandExecutor = commandExecutor;
             this.vlcExeLoc = vlcExeLoc;
         }
 
         public void playList( List<File> audioFiles ) {
             play( audioFiles.toArray( new File[audioFiles.size()] ) );
         }
 
         private void play( File... audioFiles ) {
             String[] command = new String[2 + audioFiles.length];
             command[0] = vlcExeLoc;
             command[1] = vlcOption;
             int i = 0;
             for( File each : audioFiles ) {
                 command[i++ + 2] = each.getAbsolutePath();
             }
             commandExecutor.executeCommand( command );
         }
     }
     
     interface CommandExecutor
     {
         void executeCommand(String[] command);
 
         public static CommandExecutor WINDOWS= new WindowsCommandExecutor();
         
         class WindowsCommandExecutor implements CommandExecutor
         {
             private final Runtime runtime = Runtime.getRuntime();
             
             public void executeCommand(String[] command) {
                 try {
                     Process p = runtime.exec( command );
                     int success = p.waitFor();
                     if( success != 0 ) {
                        System.out.println( "process execution failed " );
                     }
                 } catch( IOException e ) {
                     e.printStackTrace();
                 } catch( InterruptedException e ) {
                     e.printStackTrace();
                 }
             }
         }
     }
 }
