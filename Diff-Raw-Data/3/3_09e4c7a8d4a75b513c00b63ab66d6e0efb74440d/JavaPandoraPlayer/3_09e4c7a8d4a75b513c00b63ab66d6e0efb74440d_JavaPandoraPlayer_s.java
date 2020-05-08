 package com.sleazyweasel.applescriptifier;
 
 import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences;
 import com.sleazyweasel.pandora.JsonPandoraRadio;
 import com.sleazyweasel.pandora.PandoraRadio;
 import com.sleazyweasel.pandora.Song;
 import com.sleazyweasel.pandora.Station;
 import javazoom.jlgui.basicplayer.*;
 
 import javax.sound.sampled.AudioFileFormat;
 import javax.sound.sampled.AudioSystem;
 import java.io.*;
 import java.net.URL;
 import java.util.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class JavaPandoraPlayer implements MusicPlayer, BasicPlayerListener {
 
     public static class MusePlayer extends BasicPlayer {
     }
 
 
     private static final Logger logger = Logger.getLogger(JavaPandoraPlayer.class.getName());
     private PandoraRadio pandoraRadio;
     private List<Station> stations;
     private Station station;
     private Song song;
     private Song[] playlist;
     private int currentSongPointer = -1;
     private MusePlayer player;
     private List<MusicPlayerStateChangeListener> listeners = new ArrayList<MusicPlayerStateChangeListener>();
     private int currentTime;
     private long totalTime;
     private double volume = 0.5d;
     private MusicPlayerInputType currentInputType = MusicPlayerInputType.CHOOSE_STATION;
     private final MuseControllerPreferences preferences;
 
     public JavaPandoraPlayer(MuseControllerPreferences preferences) {
         this.preferences = preferences;
     }
 
     @Override
     public void volumeUp() {
         volume = volume + 0.1d;
         if (volume > 1.0d) {
             volume = 1.0d;
         }
         applyGain();
     }
 
     @Override
     public void volumeDown() {
         volume = volume - 0.1d;
         if (volume < 0.0d) {
             volume = 0.0d;
         }
         applyGain();
     }
 
     @Override
     public void setVolume(double volume) {
         this.volume = volume;
         applyGain();
     }
 
     private void applyGain() {
         try {
             if (player != null && player.hasGainControl()) {
                 player.setGain(volume);
             }
         } catch (BasicPlayerException e) {
             logger.log(Level.WARNING, "Exception caught: " + e.getMessage(), e.getCause());
         }
         preferences.setPandoraVolume(volume);
         notifyListeners();
     }
 
     @Override
     public void close() {
         try {
             if (!isStopped()) {
                 player.stop();
             }
             pandoraRadio.disconnect();
             pandoraRadio = null;
             player = null;
         } catch (BasicPlayerException e) {
             logger.log(Level.WARNING, "Exception caught.", e);
         }
     }
 
     @Override
     public void bounce() {
         boolean playing = isPlaying();
         try {
             if (!isStopped()) {
                 player.stop();
             }
             pandoraRadio.disconnect();
         } catch (BasicPlayerException e) {
             logger.log(Level.WARNING, "Exception caught.", e);
         }
         player = null;
         pandoraRadio = null;
         activate();
         station = pandoraRadio.getStationById(station.getId());
         refreshPlaylist();
         if (playing) {
             playPause();
         }
         notifyListeners();
     }
 
     @Override
     public void activate() {
         if (pandoraRadio != null && player != null) {
             return;
         }
         player = new MusePlayer();
         player.addBasicPlayerListener(this);
         pandoraRadio = new JsonPandoraRadio();
         logger.info("player.getStatus() = " + player.getStatus());
         try {
             LoginInfo loginInfo = getLogin();
             pandoraRadio.sync();
             pandoraRadio.connect(loginInfo.userName, loginInfo.password);
             stations = sort(pandoraRadio.getStations());
             notifyListeners();
         } catch (BadPandoraPasswordException b) {
             pandoraRadio = null;
             throw b;
         } catch (IOException e) {
             pandoraRadio = null;
             logger.log(Level.WARNING, "Exception caught.", e);
             throw new RuntimeException("Failed to log in to Pandora.", e);
         }
     }
 
     private List<Station> sort(List<Station> stations) {
         List<Station> toBeSorted = new ArrayList<Station>(stations);
         Collections.sort(toBeSorted, new Comparator<Station>() {
             @Override
             public int compare(Station o1, Station o2) {
                 return o1.getName().compareTo(o2.getName());
             }
         });
 
         return toBeSorted;
     }
 
 
     private void notifyListeners() {
         MusicPlayerState state = getState();
         for (MusicPlayerStateChangeListener listener : listeners) {
             try {
                 listener.stateChanged(this, state);
             } catch (Exception e) {
                 logger.log(Level.WARNING, "Exception caught.", e);
             }
         }
     }
 
     private void validateRadioState() {
         logger.info("JavaPandoraPlayer.validateRadioState");
         PandoraRadio pandoraRadio = this.pandoraRadio;
         if (pandoraRadio != null && pandoraRadio.isAlive()) {
             try {
                 pandoraRadio.getStations();
             } catch (Exception e) {
                 //error means we've lost the connection.
                 pandoraRadio.disconnect();
                 try {
                     if (!isStopped()) {
                         player.stop();
                     }
                 } catch (BasicPlayerException e1) {
                     logger.log(Level.WARNING, "Exception caught:", e1);
                 }
                 player = null;
                 this.pandoraRadio = null;
                 this.song = null;
                 activate();
                 pandoraRadio = getRadio();
                 station = pandoraRadio.getStationById(station.getId());
                 refreshPlaylist();
             }
         }
     }
 
 
     private PandoraRadio getRadio() {
         if (pandoraRadio == null || !pandoraRadio.isAlive()) {
             activate();
         }
         return pandoraRadio;
     }
 
     @Override
     public MusicPlayerState getState() {
         PandoraRadio radio = getRadio();
         if (stations == null) {
             stations = sort(radio.getStations());
         }
 
         Map<Integer, String> stationData = new LinkedHashMap<Integer, String>(stations.size());
         int i = 0;
         for (Station station : stations) {
 //            logger.info("station.getName() = " + station.getName());
             stationData.put(i++, station.getName());
         }
 
         String stationName = "";
         if (station != null) {
             stationName = station.getName();
         }
         boolean currentSongIsLoved = false;
         String title = "";
         String artist = "";
         String album = "";
         String albumArtUrl = "";
         String detailUrl = "";
         String currentTimeInTrack = "";
         if (song != null) {
             currentSongIsLoved = song.isLoved();
             title = song.getTitle();
             artist = song.getArtist();
             album = song.getAlbum();
             albumArtUrl = song.getAlbumCoverUrl();
             detailUrl = song.getAlbumDetailURL();
             //todo figure out how to get total track time.
             currentTimeInTrack = formatTime(currentTime);
             if (totalTime > 0) {
                 String totalTimeAsString = formatTime((int) (totalTime / 1000));
                 currentTimeInTrack += "/" + totalTimeAsString;
             }
         }
 
         boolean isPlaying = isPlaying();
         return new MusicPlayerState(currentSongIsLoved, title, artist, stationName, album, currentInputType, stationData, albumArtUrl, currentTimeInTrack, isPlaying, detailUrl, volume);
     }
 
     private String formatTime(int currentTime) {
         int minutes = currentTime / 60;
         int seconds = currentTime % 60;
         String secondsString = String.valueOf(seconds);
         if (secondsString.length() == 1) {
             secondsString = "0" + secondsString;
         }
         return minutes + ":" + secondsString;
     }
 
     @Override
     public void selectStation(Integer stationNumber) {
         validateRadioState();
         station = stations.get(stationNumber);
         refreshPlaylist();
         next();
         currentInputType = MusicPlayerInputType.NONE;
         if (station != null) {
             preferences.setPandoraStationId(station.getId());
         } else {
             preferences.setPandoraStationId(-1);
         }
         notifyListeners();
     }
 
     private void refreshPlaylist() {
         validateRadioState();
         try {
             playlist = pandoraRadio.getPlaylist(station, "mp3-hifi");
         } catch (Exception e) {
             logger.log(Level.WARNING, "Exception caught.", e);
             try {
                 playlist = pandoraRadio.getPlaylist(station, "mp3");
             } catch (Exception e1) {
                 logger.log(Level.WARNING, "Exception caught:", e1);
                 station = null;
                 notifyListeners();
                 throw new RuntimeException("Unable to retrieve station information from Pandora. Please contact musecontrol@gmail.com.", e1);
             }
         }
     }
 
     private void play(Song song) {
         validateRadioState();
         this.song = song;
         try {
             final URL url = new URL(song.getAudioUrl());
             final InputStream inputStream = url.openStream();
             final File tempFile = File.createTempFile("pandora", ".mp3");
             tempFile.deleteOnExit();
             final OutputStream bigBuffer = new FileOutputStream(tempFile);
             final Object monitor = new Object();
             new Thread(new Runnable() {
                 long totalBytes = 0;
 
                 public void run() {
                     try {
                         byte[] buf = new byte[8192];
                         while (true) {
                             int length = inputStream.read(buf);
                             if (length < 0) {
                                 break;
                             }
                             totalBytes += length;
                             bigBuffer.write(buf, 0, length);
                             bigBuffer.flush();
                             if (totalBytes > 64000) {
                                 synchronized (monitor) {
                                     monitor.notify();
                                 }
                             }
                         }
 
                     } catch (IOException e) {
                         logger.log(Level.WARNING, "Exception caught.", e);
                     } finally {
                         if (inputStream != null) {
                             try {
                                 inputStream.close();
                             } catch (IOException e) {
                                 logger.log(Level.WARNING, "Exception caught.", e);
                             }
                         }
                         try {
                             AudioFileFormat format = AudioSystem.getAudioFileFormat(tempFile);
                             totalTime = getTimeLengthEstimation(format.properties());
                         } catch (Exception e) {
                             logger.log(Level.INFO, "skipping audio file properties due to error.", e);
                         }
                     }
                 }
             }).start();
             synchronized (monitor) {
                 monitor.wait();
             }
             player.open(tempFile);
             player.play();
             applyGain();
         } catch (IOException ioe) {
             logger.log(Level.WARNING, "Exception caught.", ioe);
             //this seems to happen when the pandora servers are rejecting our URLs.  maybe just try again?
             next();
         } catch (Exception e) {
             //not sure what I can do here!?
             logger.log(Level.WARNING, "Exception caught.", e);
             throw new RuntimeException("Failed to play music.", e);
         }
 
     }
 
     @Override
     public void askToChooseStation() {
         currentInputType = MusicPlayerInputType.CHOOSE_STATION;
         getRadio().getStations();
         notifyListeners();
     }
 
     //todo extract getLogin, saveConfig, getConfigFile, getConfigDirectory to a helper class and inject.
     private LoginInfo getLogin() throws IOException {
         BufferedReader reader = new BufferedReader(new FileReader(getConfigFile()));
         String userLine = reader.readLine();
         String passwordLine = reader.readLine();
         String userName = userLine.substring(5);
         String password = passwordLine.substring(9);
         return new LoginInfo(userName, password);
     }
 
     public void saveConfig(String username, char[] password) throws IOException {
         File configDirectory = getConfigDirectory();
         if (!configDirectory.exists()) {
             configDirectory.mkdirs();
             Runtime.getRuntime().exec(new String[]{"chmod", "700", configDirectory.getAbsolutePath()});
         }
         File configFile = getConfigFile();
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(configFile))));
         writer.write("user=" + username);
         writer.newLine();
         writer.write("password=" + new String(password));
         writer.newLine();
         writer.close();
     }
 
     private File getConfigFile() {
         return new File(getConfigDirectory(), "config");
     }
 
     private static File getConfigDirectory() {
         String userHome = System.getProperty("user.home");
         return new File(userHome + "/.config/pandora");
     }
 
     @Override
     public boolean isConfigured() {
         return getConfigFile().exists();
     }
 
     @Override
     public boolean isAuthorized() {
         return isConfigured();
     }
 
     @Override
     public boolean isPlaying() {
         return player.getStatus() == BasicPlayer.PLAYING;
     }
 
     private boolean isStopped() {
         return player.getStatus() == BasicPlayer.STOPPED || player.getStatus() == BasicPlayer.UNKNOWN;
     }
 
     @Override
     public void addListener(MusicPlayerStateChangeListener listener) {
         listeners.add(listener);
     }
 
     @Override
     public void removeListener(MusicPlayerStateChangeListener listener) {
         listeners.remove(listener);
     }
 
     @Override
     public void cancelStationSelection() {
         currentInputType = MusicPlayerInputType.NONE;
     }
 
     @Override
     public void initializeFromSavedUserState(MuseControllerPreferences preferences) {
         Long stationId = preferences.getPreviousPandoraStationId();
         Double volume = preferences.getPreviousPandoraVolume();
         if (volume != -1) {
             this.volume = volume;
         }
         if (stationId != -1) {
             station = pandoraRadio.getStationById(stationId);
             if (station != null) {
                 currentInputType = MusicPlayerInputType.NONE;
                 refreshPlaylist();
                 notifyListeners();
             } else {
                 preferences.setPandoraStationId(-1);
             }
         }
     }
 
     @Override
     public void playPause() {
         try {
             if (isStopped()) {
                 next();
             } else if (isPlaying()) {
                 player.pause();
             } else {
                 player.resume();
             }
         } catch (BasicPlayerException e) {
             logger.log(Level.WARNING, "Exception caught.", e);
             throw new RuntimeException("failed to play/pause", e);
         }
         notifyListeners();
     }
 
     @Override
     public void next() {
         currentTime = 0;
         totalTime = 0;
         currentFrame = null;
         frameDupeCount = 0;
         if (station != null && playlist != null && playlist.length > 1) {
             try {
                 player.stop();
             } catch (BasicPlayerException e) {
                 logger.log(Level.WARNING, "Exception caught.", e);
             }
             play(nextSongToPlay());
         }
         logger.info("playlist = " + Arrays.toString(playlist));
     }
 
 
     private Song nextSongToPlay() {
         ++currentSongPointer;
         Song songToPlay = playlist[currentSongPointer];
         if (currentSongPointer == playlist.length - 1) {
             refreshPlaylist();
             currentSongPointer = -1;
         }
 
         return songToPlay;
     }
 
     @Override
     public void previous() {
         throw new UnsupportedOperationException();
     }
 
     @Override
     public void thumbsUp() {
         validateRadioState();
         if (song != null) {
             pandoraRadio.rate(song, true);
             song = new Song(song, 1);
             notifyListeners();
         }
     }
 
     @Override
     public void thumbsDown() {
         validateRadioState();
         if (song != null) {
             pandoraRadio.rate(song, false);
         }
         next();
     }
 
     @Override
     public void sleep() {
         validateRadioState();
         if (song != null) {
             pandoraRadio.tired(song);
         }
         next();
     }
 
     @Override
     public void opened(Object stream, Map properties) {
         logger.info("JavaPandoraPlayer.opened");
     }
 
     private Long currentFrame = null;
     private int frameDupeCount = 0;
 
     @Override
     public void progress(int bytesread, long microseconds, byte[] pcmdata, Map properties) {
         Long frame = (Long) properties.get("mp3.frame");
         int seconds = (int) (microseconds / 1000000);
         if (currentFrame != null && currentFrame.equals(frame)) {
             frameDupeCount++;
             if (frameDupeCount > 50) {
                 logger.info("frame = " + frame);
                 logger.info("replacing player, nexting due to frame check.");
                 try {
                     player.stop();
                 } catch (BasicPlayerException e) {
                     logger.log(Level.WARNING, "Exception caught.", e);
                 }
                 player = new MusePlayer();
                 player.addBasicPlayerListener(this);
                 next();
             }
         } else {
             frameDupeCount = 0;
         }
         boolean shouldNotify = false;
         if (seconds - currentTime >= 1) {
             shouldNotify = true;
         }
         currentFrame = frame;
         currentTime = seconds;
         if (shouldNotify) {
             notifyListeners();
         }
     }
 
     public long getTimeLengthEstimation(Map properties) {
         long milliseconds = -1;
         int byteslength = -1;
         if (properties != null) {
             if (properties.containsKey("audio.length.bytes")) {
                 byteslength = (Integer) properties.get("audio.length.bytes");
             }
             if (properties.containsKey("duration")) {
                 milliseconds = (int) (((Long) properties.get("duration")).longValue()) / 1000;
             } else {
                 // Try to compute duration
                 int bitspersample = -1;
                 int channels = -1;
                 float samplerate = -1.0f;
                 int framesize = -1;
                 if (properties.containsKey("audio.samplesize.bits")) {
                     bitspersample = (Integer) properties.get("audio.samplesize.bits");
                 }
                 if (properties.containsKey("audio.channels")) {
                     channels = (Integer) properties.get("audio.channels");
                 }
                 if (properties.containsKey("audio.samplerate.hz")) {
                     samplerate = (Float) properties.get("audio.samplerate.hz");
                 }
                 if (properties.containsKey("audio.framesize.bytes")) {
                     framesize = (Integer) properties.get("audio.framesize.bytes");
                 }
                 if (bitspersample > 0) {
                     milliseconds = (int) (1000.0f * byteslength / (samplerate * channels * (bitspersample / 8)));
                 } else {
                     milliseconds = (int) (1000.0f * byteslength / (samplerate * framesize));
                 }
             }
         }
         return milliseconds;
     }
 
 
     @Override
     public void stateUpdated(BasicPlayerEvent event) {
 //        logger.info("JavaPandoraPlayer.stateUpdated");
 //        logger.info("event = " + event);
         if (BasicPlayerEvent.EOM == event.getCode()) {
             next();
         }
     }
 
     @Override
     public void setController(BasicController controller) {
         logger.info("JavaPandoraPlayer.setController");
     }
 
     private class LoginInfo {
         private final String userName;
         private final String password;
 
         public LoginInfo(String userName, String password) {
             this.userName = userName;
             this.password = password;
         }
     }
 }
