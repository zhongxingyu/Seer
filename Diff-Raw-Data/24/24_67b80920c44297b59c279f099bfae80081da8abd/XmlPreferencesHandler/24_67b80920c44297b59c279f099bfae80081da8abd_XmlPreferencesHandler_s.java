 /*
  * XmlPreferencesHandler.java
  * 
  * Copyright (C) 2009 Nicola Roberto Viganò
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gestionecassa;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.reflect.Field;
 import org.apache.log4j.Logger;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.dom4j.io.OutputFormat;
 import org.dom4j.io.SAXReader;
 import org.dom4j.io.XMLWriter;
 
 /**
 *
  * @author ben
  */
 public class XmlPreferencesHandler<DataType extends Preferences> {
 
     /**
      * reference to the logger used for error reporting
      */
     Logger logger;
 
     /**
      * Constructor
      * @param logger reference to the used logger
      */
     public XmlPreferencesHandler(Logger logger) {
         this.logger = logger;
     }
 
     /**
      * Saves options to a file given by the option type
      * @param preferences 
      * @throws IOException
      */
     public void savePrefs(DataType preferences) throws IOException {
 
         Document document = DocumentHelper.createDocument();
         Element root = document.addElement( "config" );
         root.addAttribute("version", preferences.getVersion());
         root.addAttribute("application", preferences.getApplication());
 
         for (Field field : preferences.getClass().getFields()) {
             try {
                 root.addElement(field.getName()).addText(field.get(preferences) + "");
             } catch (IllegalArgumentException ex) {
                 logger.warn("campo sbagliato", ex);
             } catch (IllegalAccessException ex) {
                 logger.warn("campo sbagliato", ex);
             }
         }
 
         // lets write to a file
         OutputFormat format = OutputFormat.createPrettyPrint();
         XMLWriter writer = new XMLWriter(new FileWriter(preferences.getFileName()), format);
         writer.write( document );
         writer.close();
     }
 
     /**
      * Loads options from a file
      * @param preferences
      * @throws IOException
      * @throws DocumentException
      */
     public void loadPrefs(DataType preferences) throws IOException, DocumentException {
 
         SAXReader reader = new SAXReader();
         Document document = reader.read(preferences.getFileName());
 
         Element nodoRoot = document.getRootElement();
         if (!nodoRoot.attributeValue("version").equals(preferences.getVersion())) {
             throw new IOException("Wrong file version!");
         } else if (!nodoRoot.attributeValue("application").equals(preferences.getApplication())) {
             throw new IOException("This config file is for another client");
         }
 
         for (Field field : preferences.getClass().getFields()) {
             Element node = nodoRoot.element(field.getName());
             try {
                 if (node != null) {
                         assignField(preferences, field, node.getTextTrim());
                 } else {
                     System.out.println("errore campo: " + field.getName() +
                             "\nThis config file does not contain right info");
                     assignField(preferences, field, "");
                 }
             } catch (IllegalArgumentException ex) {
                 logger.warn("campo sbagliato", ex);
             } catch (IllegalAccessException ex) {
                 logger.warn("campo sbagliato", ex);
             }
         }
     }
 
     private void assignField(DataType preferences, Field field, String nodeText)
             throws IllegalArgumentException, IllegalAccessException {
 
         if (field.getType().equals(Boolean.TYPE)) {
            if (nodeText.toLowerCase().equals("yes")) {
                 nodeText = "true";
             }
            field.setBoolean(preferences, new Boolean(nodeText));
         } else if (field.getType().equals(Integer.TYPE)) {
             if (nodeText.equals("")) {
                 nodeText = "0";
             }
            field.setInt(preferences, new Integer(nodeText));
        } else if (field.getClass().isInstance(new Boolean("true"))) {
            if (nodeText.toLowerCase().equals("yes")) {
                 nodeText = "true";
             }
            field.set(preferences, new Boolean(nodeText));
        } else if (field.getClass().isInstance(new Integer("0"))) {
             if (nodeText.equals("")) {
                 nodeText = "0";
             }
            field.set(preferences, new Integer(nodeText));
         } else {
             field.set(preferences, nodeText);
         }
     }
 
 }
