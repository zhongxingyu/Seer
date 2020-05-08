 package com.sleazyweasel.applescriptifier;
 
 import com.sleazyweasel.applescriptifier.preferences.MuseControllerPreferences;
 import com.sleazyweasel.pandora.*;
 import javazoom.jlgui.basicplayer.*;
 
 import java.io.*;
 import java.net.URL;
 import java.util.*;
 
 public class JavaPandoraPlayer implements MusicPlayer, BasicPlayerListener {
     private PandoraRadio pandoraRadio;
     private List<Station> stations;
     private Station station;
     private Song song;
     private Song[] playlist;
     private int currentSongPointer = -1;
     private BasicPlayer player;
     private List<MusicPlayerStateChangeListener> listeners = new ArrayList<MusicPlayerStateChangeListener>();
     private int currentTime;
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
 
     private void applyGain() {
         try {
             player.setGain(volume);
         } catch (BasicPlayerException e) {
             e.printStackTrace();
         }
         preferences.setPandoraVolume(volume);
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
     public void close() {
         try {
             if (!isStopped()) {
                 player.stop();
             }
             pandoraRadio.disconnect();
             pandoraRadio = null;
             player = null;
         } catch (BasicPlayerException e) {
             e.printStackTrace();
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
             e.printStackTrace();
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
         player = new BasicPlayer();
         player.addBasicPlayerListener(this);
         pandoraRadio = new JsonPandoraRadio();
         System.out.println("player.getStatus() = " + player.getStatus());
         try {
             LoginInfo loginInfo = getLogin();
             pandoraRadio.sync();
             pandoraRadio.connect(loginInfo.userName, loginInfo.password);
             stations = pandoraRadio.getStations();
             notifyListeners();
         } catch (BadPandoraPasswordException b) {
             pandoraRadio = null;
             throw b;
         } catch (IOException e) {
             pandoraRadio = null;
             e.printStackTrace();
             throw new RuntimeException("Failed to log in to Pandora.", e);
         }
     }
 
     private void notifyListeners() {
         MusicPlayerState state = getState();
         for (MusicPlayerStateChangeListener listener : listeners) {
             try {
                 listener.stateChanged(this, state);
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
     }
 
     private void validateRadioState() {
         System.out.println("JavaPandoraPlayer.validateRadioState");
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
                     e1.printStackTrace();
                 }
                 player = null;
                 pandoraRadio = null;
                 song = null;
                 activate();
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
             stations = radio.getStations();
         }
         Map<Integer, String> stationData = new HashMap<Integer, String>(stations.size());
         int i = 0;
         for (Station station : stations) {
 //            System.out.println("station.getName() = " + station.getName());
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
             currentTimeInTrack = formatCurrentTime();
         }
 
         boolean isPlaying = isPlaying();
         return new MusicPlayerState(currentSongIsLoved, title, artist, stationName, album, currentInputType, stationData, albumArtUrl, currentTimeInTrack, isPlaying, detailUrl, volume);
     }
 
     private String formatCurrentTime() {
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
             preferences.setPandoraStationId(null);
         }
         notifyListeners();
     }
 
     private void refreshPlaylist() {
         validateRadioState();
         try {
             playlist = pandoraRadio.getPlaylist(station, "mp3-hifi");
         } catch (Exception e) {
             e.printStackTrace();
             try {
                 playlist = pandoraRadio.getPlaylist(station, "mp3");
             } catch (Exception e1) {
                 e1.printStackTrace();
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
             File tempFile = File.createTempFile("pandora", ".mp3");
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
                         e.printStackTrace();
                     } finally {
                         if (inputStream != null) {
                             try {
                                 inputStream.close();
                             } catch (IOException e) {
                                 e.printStackTrace();
                             }
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
             ioe.printStackTrace();
             //this seems to happen when the pandora servers are rejecting our URLs.  maybe just try again?
             next();
         } catch (Exception e) {
             //not sure what I can do here!?
             e.printStackTrace();
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
         if (volume != null) {
             this.volume = volume;
         }
         if (stationId != null) {
             station = pandoraRadio.getStationById(stationId);
            if (station != null) {
                currentInputType = MusicPlayerInputType.NONE;
                refreshPlaylist();
                notifyListeners();
            }
            else {
                preferences.setPandoraStationId(null);
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
             e.printStackTrace();
             throw new RuntimeException("failed to play/pause", e);
         }
         notifyListeners();
     }
 
     @Override
     public void next() {
         currentTime = 0;
         currentFrame = null;
         frameDupeCount = 0;
         if (station != null && playlist != null && playlist.length > 1) {
             try {
                 player.stop();
             } catch (BasicPlayerException e) {
                 e.printStackTrace();
             }
             play(nextSongToPlay());
         }
         System.out.println("playlist = " + Arrays.toString(playlist));
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
         pandoraRadio.rate(song, true);
         song = new Song(song, 1);
         notifyListeners();
     }
 
     @Override
     public void thumbsDown() {
         validateRadioState();
         pandoraRadio.rate(song, false);
         next();
     }
 
     @Override
     public void opened(Object stream, Map properties) {
         System.out.println("JavaPandoraPlayer.opened");
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
                 System.out.println("frame = " + frame);
                 System.out.println("replacing player, nexting due to frame check.");
                 try {
                     player.stop();
                 } catch (BasicPlayerException e) {
                     e.printStackTrace();
                 }
                 player = null;
                 player = new BasicPlayer();
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
 
     @Override
     public void stateUpdated(BasicPlayerEvent event) {
         System.out.println("JavaPandoraPlayer.stateUpdated");
         System.out.println("event = " + event);
         if (BasicPlayerEvent.EOM == event.getCode()) {
             next();
         }
     }
 
     @Override
     public void setController(BasicController controller) {
         System.out.println("JavaPandoraPlayer.setController");
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
