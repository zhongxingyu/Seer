 package instruments;
 
 /**
  * User: brian_anderson
  * Date: 4/13/13
  * <p/>
  * Add some readme here about how this operates
  */
 public abstract class InstrumentBase implements Instrument{
     protected void playSoundFileFromPath(String filePath){
         // TODO: do the work of playing the sound file...
 //        Player player = Manager.createPlayer(new MediaLocator(getClass().getClassLoader().getResource("Sounds/" + filePath)));
 
     }
     @Override
    public void play(Note noteToPlay) {
        playSoundFileFromPath(noteToPlay.getFileName());
     }


 }
