 package eu.uberdust.xbee.benchmark;
 
 import com.google.common.io.Files;
 import com.rapplogic.xbee.api.wpan.RxResponse16;
 import eu.mksense.MessageListener;
 
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 
 /**
  * Created with IntelliJ IDEA.
  * User: amaxilatis
  * Date: 5/15/13
  * Time: 11:32 AM
  * To change this template use File | Settings | File Templates.
  */
 public class XbeeMessageLogger implements MessageListener {
     File outputFile;
 
     public XbeeMessageLogger(String outfile) {
         outputFile = new File(outfile);
 
     }
 
     @Override
     public void receive(RxResponse16 rxResponse16) {
         int data = rxResponse16.getData()[0] * 256 + rxResponse16.getData()[1];
        String mess = rxResponse16.getRemoteAddress().toString() + "\t" + data;
 
         System.out.println(mess);
         try {
             Files.append(mess + "\n", outputFile, Charset.defaultCharset());
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
 }
