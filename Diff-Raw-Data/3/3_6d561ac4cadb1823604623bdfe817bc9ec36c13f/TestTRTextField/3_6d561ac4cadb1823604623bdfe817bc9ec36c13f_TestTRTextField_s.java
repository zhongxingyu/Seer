 import org.romppu.swing.TRTextField;
import org.romppu.translit.document.XmlTranslitDictionary;
 
 import javax.swing.*;
 import javax.xml.bind.JAXBException;
 import java.awt.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: roman
  * Date: 6.1.2013
  * Time: 12:56
  * To change this template use File | Settings | File Templates.
  */
 public class TestTRTextField {
     public static void main(String... param) {
         final TRTextField field = new TRTextField();
         try {
             field.setDictionary(new XmlTranslitDictionary("cyrillic_default.xml"));
             JFrame frame = new JFrame("Test TRTextField");
             frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             frame.getContentPane().add(field, BorderLayout.NORTH);
             frame.setSize(200, 150);
             frame.setVisible(true);
         } catch (JAXBException e) {
             e.printStackTrace();
         }
     }
 }
