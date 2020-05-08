 
 package cz.cvut.fel.kinclma1;
 
 import cz.cvut.fel.kinclma1.exceptions.ImpossibleDurationException;
 import cz.cvut.fel.kinclma1.io.FileExporter;
 import cz.cvut.fel.kinclma1.player.MusicStringPlayer;
 import cz.cvut.fel.kinclma1.player.PlayerListener;
 import cz.cvut.fel.kinclma1.tonefilters.*;
 import org.herac.tuxguitar.io.base.TGFileFormatException;
 import org.herac.tuxguitar.song.models.TGSong;
 import org.jfugue.Player;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 
 /**
  *
  * @author void
  */
 public class Main {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         MusicStringSong ms = null;
         try {
            ms = MusicStringSong.create("/home/void/project/newtest.tg");
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (TGFileFormatException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
//        ms.addTrack(randomTones(ms, new TestBassTones(), 0.2), Instrument.ELECTRIC_BASS_FINGER);
 //        MusicStringPlayer player = new MusicStringPlayer(ms.toString(), new TestListener1());

         try {
            ms.export("/home/void/project/newtest1.ly");
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } catch (TGFileFormatException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 
     private static String randomTones(MusicStringSong ms, InstrumentTones toneFilter, double restProbability) {
         FlatTrack ft = ms.getPossibleNotes(toneFilter);
         System.out.println("Possible: " + ms.getPossibleNotes(toneFilter));
         System.out.println("Played: " + ms.getPlayedTones(toneFilter));
         Iterator<HashSet<String>> it = ft.getIterator();
         while (it.hasNext()) {
             HashSet<String> beat = it.next();
             Iterator<String> bi = beat.iterator();
             String s = "";
             if (Math.random() > restProbability || beat.size() == 1) {
                 for (int i = 0; i < Math.random() * beat.size(); i ++) {
                     s = bi.next();
                 }
             } else {
                 s = "R";
             }
 
             beat.clear();
             beat.add(s);
         }
         return ft.toString();
     }
 }
