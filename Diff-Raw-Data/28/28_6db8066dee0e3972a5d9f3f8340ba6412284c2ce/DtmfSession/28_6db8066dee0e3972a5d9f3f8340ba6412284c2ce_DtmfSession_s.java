 package org.cipango.example;
 
 import java.io.File;
 import java.net.DatagramSocket;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.cipango.media.DtmfHandler;
 import org.cipango.media.DtmfListener;
 import org.cipango.media.Player;
 import org.cipango.media.PlayerListener;
 import org.cipango.media.UdpEndPoint;
 import org.cipango.media.codecs.TelephoneEventCodec;
 import org.mortbay.log.Log;
 
 public class DtmfSession implements DtmfListener, PlayerListener
 {
 
     public static final String[] SHORT_NAMES =
     {
         "Oh", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight",
         "Nine", "Star", "Hash", "A", "B", "C", "D"
     };
 
     public static final int PERIOD = 1000; // ms
     public static final int DELAY = 10; // ms
 
     private static String getCodec(int payloadType)
     {
         switch (payloadType) {
         case 0:
             return "pcmu";
         case 8:
             return "pcma";
         default:
             return null;
         }
     }
 
     private int _dtmfPayloadType;
     private int _audioPayloadType;
     private String _host;
     private int _destPort;
     private int _localPort;
     private Player _player;
     private DtmfHandler _dtmfHandler;
     private List<String> _filenames;
     private Timer _timer;
     private boolean _playing;
     private Object _playingMutex;
     private Thread _dtmfHandlerThread;
 
     public DtmfSession(int dtmfPayloadType, String host, int port,
             int audioPayloadType)
     {
         _dtmfPayloadType = dtmfPayloadType;
         _host = host;
         _destPort = port;
         _audioPayloadType = audioPayloadType;
     }
 
     private String getFileName(String shortName, int payloadType)
     {
         StringBuffer buf = new StringBuffer();
         buf.append(shortName);
         buf.append(".");
         buf.append(getCodec(payloadType));
         buf.append(".wav");
         URL url = getClass().getClassLoader().getResource(buf.toString());
         File file;
         try {
             file = new File(url.toURI());
         } catch (URISyntaxException e) {
             Log.warn("cannot find file " + url);
             return null;
         }
         return file.getAbsolutePath();
     }
 
     public void init() throws Exception
     {
         String fileName = getFileName("hello", _audioPayloadType);
         DatagramSocket datagramSocket = new DatagramSocket();
         UdpEndPoint udpEndPoint = new UdpEndPoint(datagramSocket);
         _player = new Player(fileName, _host, _destPort, _audioPayloadType,
                 udpEndPoint);
         _dtmfHandler = new DtmfHandler(_dtmfPayloadType, udpEndPoint);
         _player.init();
         _dtmfHandler.init();
         _dtmfHandlerThread = new Thread(_dtmfHandler);
         _player.addEventListener(this);
         _dtmfHandler.addDtmfListener(this);
         _filenames = Collections.synchronizedList(new ArrayList<String>());
         _localPort = datagramSocket.getLocalPort();
         _timer = new Timer();
         _playingMutex = new Object();
         synchronized (_playingMutex)
         {
             _playing = false;
         }
     }
 
     public void start()
     {
         _dtmfHandlerThread.start();
         _player.play();
         synchronized (_playingMutex)
         {
             _playing = true;
         }
         _timer.scheduleAtFixedRate(new SpeechTimerTask(), DELAY, PERIOD);
     }
 
     public void telephoneEvent(char event) {
         char[] events = TelephoneEventCodec.EVENTS;
         String shortName = null;
         for (int i = 0; i < events.length; ++i)
             if (events[i] == event)
             {
                 shortName = SHORT_NAMES[i];
                 break;
             }
         if (shortName == null)
             return;
         _filenames.add(getFileName(shortName, _audioPayloadType));
     }
 
     public void endOfFile(Player player) {
         synchronized (_playingMutex)
         {
             _playing = false;
         }
     }
 
     public void stop()
     {
         _player.removeEventListener(this);
         _dtmfHandler.removeDtmfListener(this);
         synchronized (_playingMutex)
         {
             _player.stop();
             _playing = false;
         }
         _dtmfHandler.stop();
         _timer.cancel();
         _player = null;
         _dtmfHandler = null;
         _filenames = null;
         _timer = null;
         _dtmfHandlerThread = null;
     }
 
     public int getLocalPort()
     {
         return _localPort;
     }
 
     class SpeechTimerTask extends TimerTask
     {
         @Override
         public void run()
         {
             if (_filenames.isEmpty())
                 return;
             String filename = _filenames.get(0);
             try
             {
                 _player.setFilename(filename);
             }
             catch (Exception e)
             {
                 Log.warn("Cannot set filename to " + filename, e);
             }
             synchronized (_playingMutex)
             {
                 if (!_playing)
                 {
                     _player.play();
                     _playing = true;
                 }
             }
             _filenames.remove(0);
         }
     }
 
 }
