 package cz.cvut.fel.kinclma1;
 
 import cz.cvut.fel.kinclma1.exceptions.ImpossibleDurationException;
 import org.herac.tuxguitar.song.factory.TGFactory;
 import org.herac.tuxguitar.song.models.TGChannel;
 import org.herac.tuxguitar.song.models.TGMeasure;
 import org.herac.tuxguitar.song.models.TGString;
 import org.herac.tuxguitar.song.models.TGTrack;
 
 import java.util.*;
 
 /**
  * A track - a part of the song played by a single instrument
  */
 public class MusicStringTrack {
 
     private String id;
     private String metaInfo;
 
     private int channel;
     private Instrument instrument;
     private boolean drumTrack;
     private TempoTracker tempoTracker;
     private List<MusicStringMeasure> measures;
 
     /**
      * Creates a track from the given TGTrack
      * @param tgTrack The source track
      */
     public MusicStringTrack(TGTrack tgTrack) {
         tempoTracker = new TempoTracker();
         RepetitionTracker repetitionTracker = new RepetitionTracker(this);
         channel = tgTrack.getChannel().getChannel();
         drumTrack = channel == 9;
         instrument = Instrument.fromInt(tgTrack.getChannel().getInstrument());
         id = buildId();
         metaInfo = buildMeta();
         measures = new ArrayList<MusicStringMeasure>(tgTrack.countMeasures());
         Iterator<TGMeasure> tgMeasureIterator = tgTrack.getMeasures();
         TGMeasure tgMeasure;
         while (tgMeasureIterator.hasNext()) {
             tgMeasure = tgMeasureIterator.next();
             repetitionTracker.processMeasure(tgMeasure);
         }
     }
 
     /**
      * Add a measure to the end of the track
      * @param measure measure to be added
      */
     void addMeasure(TGMeasure measure) {
         measures.add(new MusicStringMeasure(measure,tempoTracker,drumTrack));
     }
 
     /**
      * Add all measures from a list to the song
      * @param measureList the list of measures to be added
      */
     void addMeasures(List<TGMeasure> measureList) {
         for (TGMeasure measure : measureList) {
             addMeasure(measure);
         }
     }
 
     /**
      * Parses a MusicStringTrack from the given music string
      * @param msTrack Source music string for a track
      */
     public MusicStringTrack(String msTrack) {
         tempoTracker = new TempoTracker();
         StringTokenizer tokenizer = new StringTokenizer(msTrack);
         String token;
         measures = new ArrayList<MusicStringMeasure>();
         ArrayList<String> measure = new ArrayList<String>();
         while(tokenizer.hasMoreElements()) {
             token = tokenizer.nextToken();
             if(token.charAt(0) == 'V') {
                 channel = Integer.parseInt(token.substring(1));
                 drumTrack = channel == 9;
             } else if(token.charAt(0) == 'I') {
                 instrument = Instrument.valueOf(token.substring(2,token.length() - 1));
             } else if (token.charAt(0) != '|') {
                 measure.add(token);
             } else {
                 measures.add(new MusicStringMeasure(measure, tempoTracker, drumTrack));
                 measure = new ArrayList<String>();
             }
         }
         if (!measure.isEmpty()) {
             measures.add(new MusicStringMeasure(measure, tempoTracker, drumTrack));
         }
         if (instrument == null) {
             instrument = Instrument.PIANO;
         }
         metaInfo = buildMeta();
         id = buildId();
     }
 
     /**
      * Creates a track with the given parameters - used only when adding a new track to the song
      * @param channel generated free channel number
      * @param instrument instrument to be set to the track
      * @param music the content of the track
      * @param refTrack a reference track for measure lengths
      */
     MusicStringTrack(int channel, Instrument instrument, String music, MusicStringTrack refTrack) {
         this.channel = channel;
         this.instrument = instrument;
         tempoTracker = new TempoTracker();
         drumTrack = false;
         measures = new ArrayList<MusicStringMeasure>(refTrack.measures.size());
         metaInfo = buildMeta();
         id = buildId();
 
         checkAndAddMeasures(music, refTrack);
     }
 
     private void checkAndAddMeasures(String music, MusicStringTrack refTrack) {
         StringTokenizer tokenizer = new StringTokenizer(music);
         Iterator<MusicStringMeasure> iterator = refTrack.measures.iterator();
 
         measures = new ArrayList<MusicStringMeasure>(refTrack.measures.size());
         ArrayList<String> measure = new ArrayList<String>();
 
         MusicStringMeasure refMeasure = null;
         while(iterator.hasNext()) {
             refMeasure = iterator.next();
             measure = checkAndAddMeasure(tokenizer, refMeasure, measure);
         }
         if (!measure.isEmpty() && refMeasure != null) {
             addMeasureWithRef(measure, refMeasure);
         }
     }
 
     private ArrayList<String> checkAndAddMeasure(StringTokenizer tokenizer,
                                                  MusicStringMeasure refMeasure, ArrayList<String> measure) {
         if (tokenizer.hasMoreElements()) {
             measure = checkAndAddMeasureFromSource(tokenizer, refMeasure, measure);
         } else {
             addMeasureWithRef(measure, refMeasure);
             measure = createRestMeasure(refMeasure);
         }
         return measure;
     }
 
     private ArrayList<String> checkAndAddMeasureFromSource(StringTokenizer tokenizer,
                                                            MusicStringMeasure refMeasure, ArrayList<String> measure) {
         String token = tokenizer.nextToken();
         while (token != null && token.charAt(0) != '|') {
             if (!Arrays.asList('V', 'I').contains(token.charAt(0))) {
                 measure.add(token);
             }
             token = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : null;
         }
         if (token != null && token.charAt(0) == '|') {
             addMeasureWithRef(measure, refMeasure);
             measure = new ArrayList<String>();
         }
         return measure;
     }
 
     private void addMeasureWithRef(ArrayList<String> measure, MusicStringMeasure refMeasure) {
         measures.add(new MusicStringMeasure(measure, tempoTracker, refMeasure));
     }
 
     private ArrayList<String> createRestMeasure(MusicStringMeasure refMeasure) {
         ArrayList<String> measure = new ArrayList<String>();
         int dur = refMeasure.getDuration();
         while (dur > 128) {
             measure.add(new MusicStringRest(new MusicStringDuration(Duration.WHOLE)).toString());
             dur -= 128;
         }
         try {
             measure.add(new MusicStringRest(new MusicStringDuration(dur)).toString());
         } catch (ImpossibleDurationException e) {
             List<Duration> durations = e.getDurations();
             for (Duration d : durations) {
                 measure.add(new MusicStringRest(new MusicStringDuration(d)).toString());
             }
         }
         return measure;
     }
 
     /**
      * Returns the track channel number
      * @return track channel number
      */
     public int getChannel() {
         return channel;
     }
 
     /**
      * Returns the music string for the channel number
      * @return music string for the channel number
      */
     public String getChannelMusicString() {
         return String.format("V%d ", channel);
     }
 
     /**
      * Returns the instrument name if not a drum track
      * @return instrument name / empty string for drum track
      */
     public String getInstrument() {
         return drumTrack ? "" : instrument.toString();
     }
 
     /**
      * Returns the meta info which is in the beginning of the music string track - channel and instrument
      * @return meta info which is in the beginning of the music string track - channel and instrument
      */
     public String getMetaInfo() {
         return metaInfo;
     }
 
     /**
      * Returns the track id - Channel and instrument (DRUMS) for a drum track
      * @return track id - Channel and instrument (DRUMS) for a drum track
      */
     public String getId() {
         return id;
     }
 
     /**
      * Returns the list of measures
      * @return list of measures
      */
     List<MusicStringMeasure> getMeasures() {
         return new ArrayList<MusicStringMeasure>(measures);
     }
 
     /**
      * Returns the shortest resolution duration
      * @return shortest resolution duration
      */
     MusicStringDuration getShortestNote() {
         MusicStringDuration shortest = new MusicStringDuration(Duration.WHOLE);
         for (MusicStringMeasure measure : measures) {
             MusicStringDuration measureShortest = measure.getShortestNote();
             if (measureShortest.toInteger() > shortest.toInteger()) {
                 shortest = measureShortest;
             }
         }
         return shortest;
     }
 
     @Override
     public String toString() {
        resetTempo();
         StringBuilder sb = new StringBuilder(metaInfo);
         for(MusicStringMeasure measure : measures) {
             sb.append(measure.toString());
             sb.append("| ");
         }
         sb.delete(sb.length() - 2, sb.length());
         return sb.toString();
     }
 
     private void resetTempo() {
         tempoTracker.changed(0);
     }
 
     /**
      * Returns a TGTrack created from this track
      * @param factory Universal TuxGuitar factory
      * @return TGTrack created from this track
      */
     public TGTrack toTGTrack(TGFactory factory) {
         TGTrack track = factory.newTrack();
         TGChannel tgChannel = track.getChannel();
         tgChannel.setChannel((short) channel);
         tgChannel.setEffectChannel((short) channel);
         if (channel != 9) {
             tgChannel.setInstrument((short) instrument.toInteger());
             track.setStrings(initStrings(factory));
         } else {
             ArrayList<TGString> stringList = new ArrayList<TGString>(6);
             for (int i = 0; i < 6; i ++) {
                 TGString string = factory.newString();
                 string.setNumber(i + 1);
                 stringList.add(string);
             }
             track.setStrings(stringList);
         }
         int measureNumber = 1;
         boolean bass = track.stringCount() < 6;
         for (MusicStringMeasure measure : measures) {
             TGMeasure tgMeasure = measure.toTGMeasure(factory, track);
             tgMeasure.getHeader().setNumber(measureNumber ++);
             if (bass) {
                 tgMeasure.setClef(2);
             }
             track.addMeasure(tgMeasure);
         }
         track.getColor().setR(255);
         track.setName(drumTrack ? "DRUMS" : instrument.toString());
         return track;
     }
 
     private int[] getLowestToneAndMaxTones() {
         int[] ltms = new int[2];
         ltms[0] = Integer.MAX_VALUE;
         for (MusicStringMeasure measure : measures) {
             ltms = measure.getLowestToneAndMaxTones(ltms);
         }
         return ltms;
     }
 
     private ArrayList<TGString> initStrings(TGFactory factory) {
         int[] ltms = getLowestToneAndMaxTones();
         if (ltms[0] >= new MusicStringTone("E3").toInt() && ltms[1] <= 6) {
             return createStrings(factory, new String[]{"E5", "B4", "G4", "D4", "A3", "E3"});
         } else if (ltms[0] >= new MusicStringTone("E2").toInt() && ltms[1] <= 4) {
             return createStrings(factory, new String[]{"G3", "D3", "A2", "E2"});
         } else if (ltms[0] >= new MusicStringTone("B2").toInt() && ltms[1] <= 7) {
             return createStrings(factory, new String[]{"E5", "B4", "G4", "D4", "A3", "E3", "B2"});
         } else if (ltms[0] >= new MusicStringTone("B1").toInt() && ltms[1] <= 5) {
             return createStrings(factory, new String[]{"G3", "D3", "A2", "E2", "B1"});
         } else if (ltms[1] <= 7){
             return createStrings(factory, new String[]{"C0", "C0", "C0", "C0", "C0", "C0", "C0"});
         } else {
             throw new UnsupportedOperationException("Cannot export track with more than 7 tones played simultaneously.");
         }
     }
 
     private ArrayList<TGString> createStrings(TGFactory factory, String[] tones) {
         ArrayList<TGString> stringList = new ArrayList<TGString>(tones.length);
         TGString string;
         for (int i = 0; i < tones.length; i ++) {
             string = factory.newString();
             string.setNumber(i + 1);
             string.setValue(new MusicStringTone(tones[i]).toInt());
             stringList.add(string);
         }
         return stringList;
     }
 
     private String buildId() {
         return getChannelMusicString() + (drumTrack ? "DRUMS" : instrument.toString());
     }
 
     private String buildMeta() {
         if(!drumTrack) {
             return getChannelMusicString() + instrument.toMusicString();
         }
         return getChannelMusicString();
     }
 }
