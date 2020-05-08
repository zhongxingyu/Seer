 package fr.istic.synthlab.command.menu;
 
 import java.awt.Desktop;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import fr.istic.synthlab.Synthlab;
 import fr.istic.synthlab.command.ICommand;
 
 public class DocumentationCommand implements ICommand {
 
 	@Override
 	public void execute() {
 		if (Desktop.isDesktopSupported()) {
 			try {
 				File pdfDocument = new File(Synthlab.DOCUMENTATION_PDF_FILE);
 				
 				InputStream inputStream = ClassLoader
 						.getSystemResourceAsStream(Synthlab.DOCUMENTATION_PDF_FILE);
 
 				OutputStream out = new FileOutputStream(pdfDocument);
 
 				int read = 0;
 				byte[] bytes = new byte[1024];
 
 				while ((read = inputStream.read(bytes)) != -1) {
 					out.write(bytes, 0, read);
 				}
 
 				inputStream.close();
 				out.flush();
 				out.close();
 				
 				// Open PDF
 				Desktop.getDesktop().open(pdfDocument);
 				
 				// Delete file on application exit
 				pdfDocument.deleteOnExit();
 			} catch (IOException ex) {
 				System.err.println("Can't locate documentation file !");
 			}
 		}
 	}
 
 }
