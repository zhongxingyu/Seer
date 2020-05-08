 package instruments;
 
 import javax.sound.sampled.AudioInputStream;
 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * User: brian_anderson
  * Date: 4/13/13
  * <p/>
  * Add some readme here about how this operates
  */
 public abstract class InstrumentBase implements Instrument{
     public LinkedList<Note> autoNotes;
 
 
 
     protected String getPathToSoundKits(){
         String fullPath = getClass().getClassLoader().getResource(".").getPath();
         fullPath += "../../../Resources/";
         return fullPath;
     }
 
     protected void playSoundFileFromPath(String filePath){
         // TODO: do the work of playing the sound file...
         try {
             File soundFile = new File(getPathToSoundKits() + filePath);
 //            System.out.println("sound file " + getPathToSoundKits()  + filePath);  // check the URL!
             AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
             Clip clip = AudioSystem.getClip();
             clip.open(audioInputStream);
             clip.start();
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
     public void addNotesFromWaveFilesInDirectory(String directory){
 
         autoNotes = new LinkedList<Note>();
 
         File dir = new File(getPathToSoundKits() + "/" + directory);
         File[] files = dir.listFiles(new FilenameFilter() {
             public boolean accept(File dir, String filename)
             { return filename.endsWith(".wav"); }
         });
 
         for(File file : files){
             String fileName = file.getName();
             final String name = fileName.substring(0, fileName.indexOf("."));
             final String path = directory + "/" + file.getName();
             Note auto = new Note()
             {
 
                 @Override
                 public String id()
                 {
                     return getFileName();
                 }
 
                 @Override
                 public String name()
                 {
                     return name;
                 }
 
                 @Override
                 public String getFileName()
                 {
                     return path;
                 }
 
 
                 public String toString(){
                     return name;
                 }
             };
             autoNotes.add(auto);
         }
     }
 
 
     @Override
     public void play(Note noteToPlay){
 // Uncomment if you want it only to play it's own note
 //        if(autoNotes.contains(noteToPlay)){
             playSoundFileFromPath(noteToPlay.getFileName());
 //        }
     }
 
     @Override
     public List<Note> getSupportedNotes()
     {
         return autoNotes;
     }
 
     @Override
     public Note getNoteByName(String name)
     {
         for (Note note : getSupportedNotes())
         {
            if (note.name().equalsIgnoreCase(name))
             {
                 return note;
             }
         }
         return null;
     }
 
     @Override
     public void play(List<Note> notesToPlay)
     {
         for(Note note : notesToPlay){
             play(note);
         }
     }
 
 
 }
