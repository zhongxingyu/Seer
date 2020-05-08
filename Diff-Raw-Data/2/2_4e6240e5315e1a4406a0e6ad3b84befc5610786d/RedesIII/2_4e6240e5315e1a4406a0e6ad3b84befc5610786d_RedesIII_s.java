 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package redesiii;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import net.n3.nanoxml.IXMLElement;
 import net.n3.nanoxml.IXMLParser;
 import net.n3.nanoxml.IXMLReader;
 import net.n3.nanoxml.StdXMLReader;
 import net.n3.nanoxml.XMLException;
 import net.n3.nanoxml.XMLParserFactory;
 import org.xml.sax.XMLReader;
 
 /**
  *
  * @author cef
  */
 public class RedesIII {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         try {
             
             IXMLParser parser= XMLParserFactory.createDefaultXMLParser();
             IXMLReader reader= StdXMLReader.fileReader(System.getProperty("user.dir")+"/src/redesiii/NewFile.xml");
             parser.setReader(reader);
             IXMLElement xml = (IXMLElement) parser.parse();
             Enumeration e = xml.enumerateChildren();
             
             while(e.hasMoreElements()){
                 System.out.println(((IXMLElement)e.nextElement()).getName());
             }
             Vector<IXMLElement> v = xml.getChildrenNamed("expresion");
          //  IXMLElement c=(IXMLElement) v.get(0);
             
                 
 //                System.out.println(c.getName());
  //              System.out.println(c.getContent() );
                 
                Maquina m = new Maquina();
                 
                 m.ejecutar("ps -e");
             
             
             
         } catch (XMLException ex) {
             Logger.getLogger(RedesIII.class.getName()).log(Level.SEVERE, null, ex);
         } catch (FileNotFoundException ex) {
             Logger.getLogger(RedesIII.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IOException ex) {
             Logger.getLogger(RedesIII.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ClassNotFoundException ex) {
             Logger.getLogger(RedesIII.class.getName()).log(Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             Logger.getLogger(RedesIII.class.getName()).log(Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             Logger.getLogger(RedesIII.class.getName()).log(Level.SEVERE, null, ex);
         }
         
     }
 }
