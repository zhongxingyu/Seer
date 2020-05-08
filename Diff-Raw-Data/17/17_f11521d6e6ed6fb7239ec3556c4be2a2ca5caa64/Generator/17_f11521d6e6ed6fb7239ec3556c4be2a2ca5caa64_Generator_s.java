 package generator; /**
  * Author: Mateusz Buśkiewicz
  * Date: 27.12.2012
  * Time: 17:20
  */
 
import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.util.Config;

import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Properties;
 
 public class Generator {
 
     public static void main(String[] args) {
         for (int digit=0; digit<=9; digit++) {
             Producer captchaProducer = getProducer(digit);
             for(int sampleId = 0; sampleId<10; sampleId++) {
                 String text = captchaProducer.createText();
                 BufferedImage image = captchaProducer.createImage(text);
                 OutputStream fileOutputStream = null;
                 try {
                    fileOutputStream = new FileOutputStream("/tmp/kaptcha/digit"+digit+"/"+sampleId+".jpeg");
                     ImageIO.write(image,"jpg",fileOutputStream);
                 } catch (IOException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 } finally {
                     if (fileOutputStream!=null) {
                         try {
                             fileOutputStream.close();
                         } catch (IOException e) {
                             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                         }
                     }
                 }
             }
         }
 
 
     }
 
     public static Producer getProducer(int digit) {
         Properties properties = new Properties();
 
         properties.put("kaptcha.background.clear.from","white");
         properties.put("kaptcha.background.clear.to","white");
         properties.put("kaptcha.word.impl","generator.ModifiedWordRenderer");
         properties.put("kaptcha.textproducer.char.string",String.valueOf(digit));
         properties.put("kaptcha.textproducer.char.length","5");
         properties.put("kaptcha.obscurificator.impl","generator.ModifiedWaterRipple");
 
         Config config = new Config(properties);
         return config.getProducerImpl();
     }
 }
