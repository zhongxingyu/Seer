 package org.kevoree.microsandbox.samples.network;
 
import org.kevoree.annotation.*;
 import org.kevoree.framework.AbstractComponentType;
 import org.kevoree.microsandbox.api.contract.FullContracted;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 /**
  * Created with IntelliJ IDEA.
  * User: duke
  * Date: 03/07/13
  * Time: 18:25
  */
 
 @DictionaryType({
         @DictionaryAttribute(name = "sleepTime", dataType = Integer.class, optional = false),
         @DictionaryAttribute(name = "url", dataType = String.class, optional = false),
         @DictionaryAttribute(name = "violate", defaultValue = "false", vals = {"true", "false"})
 })
 @ComponentType
 public class NetworkConsumerInputPeakSecond extends AbstractComponentType implements FullContracted {
 
    private boolean running;
     @Start
     public void startComponent() {
        running = true;
         new Thread(new Runnable() {
             public void run() {
                 int total = 0;
                 int c = 0;
                 byte[] bytes = new byte[1024];
                 String urlString = getDictionary().get("url").toString();
                 int sleepTime = Integer.parseInt(getDictionary().get("sleepTime").toString());
                 boolean violate = "true".equals(getDictionary().get("violate").toString());
                 int valueViolation = Integer.parseInt(getDictionary().get("network_input_peak_seconds").toString());
                while (((!violate && total + c <= valueViolation) || violate) && running) {
                     c = 0;
                     InputStream stream = null;
                     try {
                         URL url = new URL(urlString);
                         stream = url.openStream();
                         int n;
                         while ((n = stream.read(bytes)) != -1) {
                             c += n;
                         }
                         total += c;
 //                        System.out.println("total received: " + total);
                         Thread.sleep(sleepTime);
                     } catch (MalformedURLException ignored) {
                         ignored.printStackTrace();
                     } catch (IOException ignored) {
                         ignored.printStackTrace();
                     } catch (InterruptedException ignored) {
                         ignored.printStackTrace();
                     } finally {
                         if (stream != null) {
                             try {
                                 stream.close();
                             } catch (IOException ignored) {
                             }
                         }
                     }
 
                 }
             }
         }).start();
     }
    @Stop
    public void stop() {
        running = false;
    }
 
 }
