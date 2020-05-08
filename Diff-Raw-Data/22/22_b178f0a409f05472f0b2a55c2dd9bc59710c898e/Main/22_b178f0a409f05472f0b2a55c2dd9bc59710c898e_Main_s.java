 
 package cz.cvut.fel.kinclma1;
 
 import cz.cvut.fel.kinclma1.io.FileExporter;
 import cz.cvut.fel.kinclma1.player.MusicStringPlayer;
 import org.herac.tuxguitar.song.models.TGSong;
 import org.jfugue.Player;
 
 import java.io.File;
 
 /**
  *
  * @author void
  */
 public class Main {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
        MusicStringSong ms = new MusicStringSong(new File("/home/void/project/rem1.gp4"));
        ms.getPossibleNotes();
 //        TestListener1 listener = new TestListener1();
 //        MusicStringPlayer player = new MusicStringPlayer(ms.toString(), listener);
 //        player.play();
        TGSong tgs = ms.toTGSong();
        new FileExporter().exportSong(tgs, "/home/void/project/newrem1.tg");
 
 //        for (int i = 128; i > 0; i -= 2) {
 //            try {
 //                System.out.println(new MusicStringDuration(i).toString());
 //            } catch (ImpossibleDurationException e) {
 //                System.out.println(e.getMessage());
 //            }
 //        }
     }
 }
