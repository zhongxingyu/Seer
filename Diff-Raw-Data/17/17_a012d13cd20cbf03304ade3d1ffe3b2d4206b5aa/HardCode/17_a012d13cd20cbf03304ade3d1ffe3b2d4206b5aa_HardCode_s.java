 package no.ebakke.studycaster.applications;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 import java.util.Arrays;
 import no.ebakke.studycaster.screencasting.RecordingConverter;
 import no.ebakke.studycaster.api.ServerContext;
 import no.ebakke.studycaster.screencasting.ScreenCensor;
 import no.ebakke.studycaster.screencasting.ScreenRecorder;
 import no.ebakke.studycaster.util.Util;
 
 public class HardCode {
   public static void main(String args[]) throws Exception {
     int speedupFactor = 1;
    String confCode = "b194c8fcd1e9";
     boolean download = false;
 
     if (download) {
       String fileName = "z:/rectest/downloaded.ebc";
       ServerContext sc = new ServerContext();
       OutputStream fos = new FileOutputStream(fileName);
       Util.hookupStreams(sc.downloadFile("uploads/" + confCode + "/screencast.ebc"), fos);
       fos.close();
       RecordingConverter.convert(new FileInputStream(fileName), "z:/rectest/downconv.mkv", speedupFactor);
     } else {
      RecordingConverter.convert(new FileInputStream("z:/recruiting/uploads/" + confCode + "/screencast.ebc"),
            "z:/recruiting/uploads/" + confCode + "/screencast_hardcode.mkv", speedupFactor);
     }
   }
 
   public static void mainScreenCastExperiments(String args[]) throws Exception {
     System.out.println(System.getProperty("java.io.tmpdir"));
 
     //Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
 
     // Note: slowest broadband connection sold in Norway has 100kbps upload speed.
     OutputStream os = new FileOutputStream("z:/rectest/localout.ebc");
 
     //final NonBlockingOutputStream os = new NonBlockingOutputStream(4 * 1024 * 1024);
     //ServerContext sc = new ServerContext();
     //os.connect(sc.uploadFile("screencast.ebc"));
 
     ScreenRecorder sr = new ScreenRecorder(os, 0);
     sr.setCensor(new ScreenCensor(Arrays.asList(new String[] {"Excel"}), Arrays.asList(new String[] {}), true));
     sr.start();
     Thread.sleep(20000);
     /*
     for (int i = 0; i < 1000; i++) {
       //System.err.println("frame time: " + sr.frameRecorder.avgDuration.get());
       Thread.sleep(100);
     }
     */
     sr.close();
   }
 }
