 //
 //  ReportsController.java
 //  MacPAFTest
 //
 //  Created by Logan Allred on Sun Mar 30 2003.
 //  Copyright (c) 2002-2004 RedBugz Software. All rights reserved.
 //
 
 import java.io.File;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.NDC;
 
 import com.apple.cocoa.application.*;
 import com.apple.cocoa.foundation.NSArray;
 import com.apple.cocoa.foundation.NSSelector;
 import com.redbugz.macpaf.Individual;
 import com.redbugz.macpaf.jdom.GedcomLoaderJDOM;
 import com.redbugz.macpaf.util.CocoaUtils;
 
 public class ImportController {
   private static final Logger log = Logger.getLogger(ImportController.class);
 
   public NSWindow importWindow; /* IBOutlet */
   public NSMatrix importRadio; /* IBOutlet */
   public NSTextField filePathField; /* IBOutlet */
   public NSProgressIndicator progress; /* IBOutlet */
 
 private NSOpenPanel openPanel;
 
   
   public void cancel(Object sender) { /* IBAction */
 //	NSApplication.sharedApplication().stopModal();
 	NSApplication.sharedApplication().endSheet(importWindow);
 	importWindow.orderOut(this);
   }
 
   public void browse(Object sender) { /* IBAction */
 	  NDC.push(((MyDocument) NSDocumentController.sharedDocumentController().currentDocument()).displayName()+" import browse");
 	  try {
 		  openPanel = NSOpenPanel.openPanel();
 		//panther only?        panel.setMessage("Please select a GEDCOM file to import into this MacPAF file.");
 //		  NSWindow mainWindow = ((MyDocument) NSDocumentController.sharedDocumentController().currentDocument()).mainWindow;
 //		  log.debug("mainwindow:"+mainWindow.title());
 		openPanel.beginSheetForDirectory(null, null, new NSArray(new Object[] {"GED"}), null,
 				  this,
 				  new NSSelector("openPanelDidEnd", new Class[] {NSOpenPanel.class, int.class, Object.class}), null);
 	  } catch (Exception e) {
 		  // TODO Auto-generated catch block
 		  e.printStackTrace();
 	  }
 	  NDC.pop();
   }
   public void openPanelDidEnd(NSOpenPanel sheet, int returnCode, Object contextInfo) {
 	  NDC.push(((MyDocument) NSDocumentController.sharedDocumentController().currentDocument()).displayName()+" import");
 	  if (returnCode == NSPanel.OKButton) {
 		  log.debug("import filename:" + sheet.filename());
 		  filePathField.setStringValue(sheet.filename());
 	  }
 	  NDC.pop();
   }
   
   public void importFile(Object sender) { /* IBAction */
 	  NDC.push(((MyDocument) NSDocumentController.sharedDocumentController().currentDocument()).displayName()+" import");
 	log.debug("save sender=" + sender + " selectedTag=" + importRadio.selectedTag());
 	try {
 		MyDocument doc = (MyDocument) NSDocumentController.sharedDocumentController().currentDocument();
 		if  (importRadio.selectedTag() == 0) { // import into existing file
 				new GedcomLoaderJDOM(doc.doc, progress).loadXMLFile(new File(filePathField.stringValue()));
 		}
 		NSApplication.sharedApplication().endSheet(importWindow);
 	} catch (RuntimeException e) {
 		e.printStackTrace();
 		MyDocument.showUserErrorMessage("There was an unexpected error during import.", "An unexpected error occurred while attempting to import the file. The data may not have been imported. Please try again or report this to the MacPAF developers");
 	} finally {
 		NDC.pop();
 	}
	
 	importWindow.orderOut(this);
   }
 
 //  public void setDocument(NSDocument nsDocument) {
 //	super.setDocument(nsDocument);
 //	log.debug("setdocument:" + nsDocument);
 ////      log.debug("surname:"+surname);
 //	Individual individual = ( (MyDocument) nsDocument).getPrimaryIndividual();
 ////      setIndividual(individual);
 //	doc = (MyDocument) nsDocument;
 //  }
 
 //  public void openImportSheet(Object sender) { /* IBAction */
 //	NSApplication nsapp = NSApplication.sharedApplication();
 //	nsapp.beginSheet(importWindow, ((MyDocument)this.document()).mainWindow, null, null, null);
 ////	nsapp.runModalForWindow(this.window());
 ////	nsapp.endSheet(this.window());
 ////	this.window().orderOut(this);
 //  }
 }
