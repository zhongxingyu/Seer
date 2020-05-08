 package webApplication.grafica;
 
 import java.awt.Window;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.JOptionPane;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.MarshalException;
 import org.xml.sax.SAXParseException;
 
 import webApplication.xml.XMLMarshaller;
 
 /**
  * Convert the tree into the corresponding xml file
  * @author Andrea
  *
  */
 public class XMLGenerator {
 	
 	private static final String FILENAME = "fileCA.xml";
 	private static final String JOPTIONPANETITLE = "File existing";
 	private static final String FILEEXISTINGMESSAGE = "XML file already exists. Continuing will overwrite the old file.\n Do you want to continue?";
 	
 	private Window owner;
 
 	protected XMLGenerator(Window owner) {
 		this.owner = owner;
 	}
 	
 	/**
 	 * Write the xml file according to the schema
 	 * @return		The state of the execution.
 	 * 				0 = execution ended normally
 	 *				-1= xml file already exists
 	 * 				-2= context creation exception
 	 * 				-3= file related exception 
 	 * 				-4= validator related exception
 	 * 				-5= close outputstream related exception
 	 */
 	protected int generateXML() {
 		File file = new File(FILENAME);
 		if (file.exists()) {
 			int choice = JOptionPane.showConfirmDialog(owner, FILEEXISTINGMESSAGE, JOPTIONPANETITLE, JOptionPane.YES_NO_OPTION);
 			if (choice == JOptionPane.NO_OPTION) {
 				return -1;
 			}
 		}
 		XMLMarshaller marshaller = new XMLMarshaller(MainWindow.albero.getTree(), file);
 		marshaller.execute();
 		try {
 			marshaller.get();
 		} catch (InterruptedException e) {
 			System.out.println("Classe causa interrupt: "+e.getMessage());
 		} catch (ExecutionException e) {
 			System.out.println("Classe: "+e.getCause().getClass());
 			if (e.getCause().getClass().equals(SAXParseException.class)) {
 				System.out.println("Errore del parser");
 			} else if (e.getCause().getClass().equals(MarshalException.class)) {
 				System.out.println("Errore di validazione");
 				return -4;
 			} else if (e.getCause().getClass().equals(JAXBException.class)) {
 				System.out.println("Errore inaspettato durante il marshalling");
 				return -2;
 			} else if (e.getCause().getClass().equals(FileNotFoundException.class)) {
 				System.out.println("Errore outputstream");
 				return -3;
 			} else if (e.getCause().getClass().equals(IOException.class)) {
 				System.out.println("Non posso chiudere lo stream");
 				return -5;
 			}
 		}
 		return 0;
 	}
 
 }
