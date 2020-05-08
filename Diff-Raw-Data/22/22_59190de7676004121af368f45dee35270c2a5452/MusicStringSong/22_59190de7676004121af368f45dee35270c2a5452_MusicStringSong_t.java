 
 package cz.cvut.fel.kinclma1;
 
 import cz.cvut.fel.kinclma1.exceptions.IncompatibleTrackException;
 import cz.cvut.fel.kinclma1.exceptions.NoFreeChannelException;
 import cz.cvut.fel.kinclma1.io.FileExporter;
 import cz.cvut.fel.kinclma1.io.FileImporter;
 import org.herac.tuxguitar.io.base.*;
 import org.herac.tuxguitar.song.factory.TGFactory;
 import org.herac.tuxguitar.song.models.TGMeasure;
 import org.herac.tuxguitar.song.models.TGSong;
 import org.herac.tuxguitar.song.models.TGTrack;
 
 import java.io.*;
 import java.util.*;
 import java.util.concurrent.Callable;
 
 /**
  * A music string song - the main class and API of the library
  */
 public class MusicStringSong {
 
     private class TrackCreator implements Callable<MusicStringTrack> {
 
         private TGTrack track;
         private String strTrack;
 
         public TrackCreator(TGTrack track) {
             this.track = track;
         }
 
         public TrackCreator(String strTrack) {
             this.strTrack = strTrack;
         }
 
         @Override
         public MusicStringTrack call() throws Exception {
             return track != null ? new MusicStringTrack(track) : new MusicStringTrack(strTrack);
         }
     }
 
     private class TrackExporter implements Callable<TGTrack> {
         private MusicStringTrack track;
         private TGFactory factory;
 
         public TrackExporter(MusicStringTrack track, TGFactory factory) {
             this.track = track;
             this.factory = factory;
         }
 
         @Override
         public TGTrack call() throws Exception {
             return track.toTGTrack(factory);
         }
     }
 
     private Map<String, MusicStringTrack> tracks = new HashMap<String, MusicStringTrack>();
     private HarmonyDetector harmonyDetector;
 
     /**
      * Factory method to create a MusicStringSong from a file on the given path
      * @param filename Path to the source file
      * @return A MusicStringSong instance created from the source file
      * @throws IOException
      * @throws TGFileFormatException
      */
     public static MusicStringSong create(String filename) throws IOException, TGFileFormatException {
         if (filename.substring(filename.lastIndexOf('.')).contains("musicstring")) {
             StringBuilder sb = new StringBuilder();
             BufferedReader in = new BufferedReader(new FileReader(filename));
             try {
                 String line = in.readLine();
                 while (line != null) {
                     sb.append(line);
                     line = in.readLine();
                 }
             } finally {
                 in.close();
             }
             return new MusicStringSong(sb.toString());
         } else {
             return new MusicStringSong(new FileImporter().importFile(filename));
         }
     }
 
     /**
      * Creates MusicString from a string representation of music string
      * @param musicString a string representation of a music string
      */
     public MusicStringSong(String musicString) {
 
         //http://stackoverflow.com/questions/2206378/how-to-split-a-string-but-also-keep-the-delimiters
         String[] strTracks = musicString.split("\\s(?=V[0-9])");
 
         List<Callable<MusicStringTrack>> tcs = new ArrayList<Callable<MusicStringTrack>>(strTracks.length);
         for (String track : strTracks) {
             tcs.add(new TrackCreator(track));
         }
 
         create(tcs);
     }
 
     /**
      * Creates a MusicString from a given TGSong object
      * @param song Input TGSong object
      */
     public MusicStringSong(TGSong song) {
         Iterator<TGTrack> tgTracks = song.getTracks();
 
         List<Callable<MusicStringTrack>> tcs = new ArrayList<Callable<MusicStringTrack>>(song.countTracks());
         while (tgTracks.hasNext()) {
             tcs.add(new TrackCreator(tgTracks.next()));
         }
 
         try {
             create(tcs);
         } catch (UnsupportedOperationException e) {
             throw new UnsupportedOperationException(e.getMessage());
         }
     }
 
     /**
      * Returns a music String containing all tracks of the song
      * @return all tracks in a single String
      */
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         for (String key : getTrackIds()) {
             sb.append(tracks.get(key));
         }
         return sb.toString();
     }
 
     /**
      * Returns an array of supported input formats
      * @return Array of supported input formats
      */
     public static TGFileFormat[] getInputFormats() {
         ArrayList<TGFileFormat> fmts = new ArrayList<TGFileFormat>();
         TGFileFormatManager formatManager = TGFileFormatManager.instance();
         List<TGFileFormat> is = formatManager.getInputFormats();
         fmts.addAll(is);
         Iterator<TGLocalFileImporter> imp = formatManager.getImporters();
         while (imp.hasNext()) {
             fmts.add(imp.next().getFileFormat());
         }
         fmts.add(new TGFileFormat("JFugue 4 MusicString", "*.musicstring"));
         return fmts.toArray(new TGFileFormat[fmts.size()]);
     }
 
     /**
      * Returns an array of supported output formats
      * @return Array of supported output formats
      */
     public static TGFileFormat[] getExportFormats() {
         ArrayList<TGFileFormat> fmts = new ArrayList<TGFileFormat>();
         TGFileFormatManager formatManager = TGFileFormatManager.instance();
         List<TGFileFormat> os = formatManager.getOutputFormats();
         fmts.addAll(os);
         Iterator<TGLocalFileExporter> exp = formatManager.getExporters();
         while (exp.hasNext()) {
             fmts.add(exp.next().getFileFormat());
         }
         fmts.add(new TGFileFormat("JFugue 4 MusicString", "*.musicstring"));
         return fmts.toArray(new TGFileFormat[fmts.size()]);
     }
 
     /**
      * Exports this song to a file with a given path
      * @param filename Target file path and name
      * @throws IOException
      * @throws TGFileFormatException
      */
     public void export(String filename) throws IOException, TGFileFormatException {
         try {
             if (filename.substring(filename.lastIndexOf('.')).contains("musicstring")) {
                 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename));
                 try {
                     out.write(toString().getBytes());
                 } finally {
                     out.flush();
                     out.close();
                 }
             } else {
                 new FileExporter().exportSong(toTGSong(), filename);
             }
         } catch (FileNotFoundException e) {
             new File(filename).delete();
             throw e;
         } catch (TGFileFormatException e) {
             new File(filename).delete();
             throw e;
         } catch (IOException e) {
             new File(filename).delete();
             throw e;
         }
     }
 
     /**
      * Returns a TGSong object, a tuxGuitar internal representation of a song, created from this song
      * @return tuxGuitar internal representation of a song, created from this song
      */
     public TGSong toTGSong() {
         TGFactory factory = new TGFactory();
         TGSong song = factory.newSong();
 
         Collection<Callable<TGTrack>> tcs = new ArrayList<Callable<TGTrack>>(tracks.size());
         for (String trackId : getTrackIds()) {
             tcs.add(new TrackExporter(getTrack(trackId), factory));
         }
 
         List<TGTrack> tgTracks = Parallellization.executeSingleBatch(tcs);
 
         int i = 0;
         for (TGTrack tgTrack : tgTracks) {
             tgTrack.setNumber(i++);
             song.addTrack(tgTrack);
         }
 
         Iterator<TGMeasure> it = song.getTrack(0).getMeasures();
         while (it.hasNext()) {
             song.addMeasureHeader(it.next().getHeader());
         }
 
         return song;
     }
 
     /**
      * Returns IDs of all tracks, used to get a single track
      * @return all track IDs
      */
     public List<String> getTrackIds() {
         List<String> list = new ArrayList<String>(tracks.keySet());
         Collections.sort(list);
         return list;
     }
     
     /**
      * Returns the track with the given ID
      * @param id ID of the wanted track, list of all IDs can be obtained with
      *              getTrackIds method
      * @return music String representing a single track
      */
     public MusicStringTrack getTrack(String id) {
         return tracks.get(id);
     }
 
     /**
      * Removes the track with the given ID from the song
      * @param id ID of the track to be removed, list of all IDs can be obtained with
      *              getTrackIds method
      */
     public void removeTrack(String id) {
         tracks.remove(id);
     }
 
     /**
      * Adds a new track to the song
     * @param newTrack Track to be added without channel and instrument
      * @param instrument Instrument to be set to the track
      * @throws NoFreeChannelException when all 16 MIDI channels are occupied by tracks
      * @throws IncompatibleTrackException when measure lengths of the new track do not match the song's ones
      */
     public void addTrack(String newTrack, Instrument instrument)
             throws NoFreeChannelException, IncompatibleTrackException {
         int channel = getFreeChannel();
         if (channel < 0) {
             throw new NoFreeChannelException("No free channel available." +
                     " In order to add a new track, you have to remove one or more existing tracks");
         }
         MusicStringTrack track = new MusicStringTrack(channel,instrument,newTrack,getTrack(getTrackIds().get(0)));
         tracks.put(track.getId(),track);
     }
 
     private int getFreeChannel() {
         BitSet channels = new BitSet(16);
 
         channels.set(9);
 
         for (MusicStringTrack track : tracks.values()) {
             channels.set(track.getChannel());
         }
         int index = channels.nextClearBit(0);
         return index < 16 ? index : -1;
     }
 
     /**
      * Returns a track containing all notes that can surely be played in any additional track processed by a given tone
      * filter
      * @param toneFilter A filter defining tones playable on an instruments
      * @return track containing all notes that can surely be played in any additional track processed by a given tone
      * filter
      */
     public FlatTrack getPossibleNotes(InstrumentTones toneFilter) {
         harmonyDetector = new HarmonyDetector(this);
         return harmonyDetector.detectHarmony(toneFilter);
     }
 
     /**
      * Returns a track containing all notes played in the song processed by a given tone filter
      * @param toneFilter A filter defining tones playable on an instruments
      * @return track containing all notes played in the song processed by a given tone filter
      */
     public FlatTrack getPlayedTones(InstrumentTones toneFilter) {
         if (harmonyDetector == null) {
             harmonyDetector = new HarmonyDetector(this);
             harmonyDetector.detectHarmony(toneFilter);
         }
         return harmonyDetector.originalTones(toneFilter);
     }
 
     private void create(List<Callable<MusicStringTrack>> tcs) {
         List<MusicStringTrack> msTracks;
         msTracks = Parallellization.executeSingleBatch(tcs);
         for (MusicStringTrack msTrack : msTracks) {
             tracks.put(msTrack.getId(), msTrack);
         }
     }
 }
