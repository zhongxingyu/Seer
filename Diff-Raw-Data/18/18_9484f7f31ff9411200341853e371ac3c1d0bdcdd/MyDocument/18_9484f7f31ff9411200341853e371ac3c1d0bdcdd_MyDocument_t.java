 //
 //  MyDocument.java
 //  MacPAFTest
 //
 //  Created by Logan Allred on Sun Dec 22 2002.
 //  Copyright (c) 2002-2004 RedBugz Software. All rights reserved.
 //
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.*;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.MultipartPostMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.multipart.FilePart;
 import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
 import org.apache.commons.httpclient.methods.multipart.Part;
 import org.apache.commons.httpclient.methods.multipart.StringPart;
 import org.apache.log4j.Logger;
 import org.apache.log4j.NDC;
 
 import com.apple.cocoa.application.*;
 import com.apple.cocoa.foundation.*;
 import com.redbugz.macpaf.Family;
 import com.redbugz.macpaf.Gender;
 import com.redbugz.macpaf.Individual;
 import com.redbugz.macpaf.LocationList;
 import com.redbugz.macpaf.SurnameList;
 import com.redbugz.macpaf.jdom.MacPAFDocumentJDOM;
 import com.redbugz.macpaf.test.Tests;
 import com.redbugz.macpaf.util.CocoaUtils;
 import com.redbugz.macpaf.util.DateUtils;
 import com.redbugz.macpaf.util.MultimediaUtils;
 import com.redbugz.macpaf.util.StringUtils;
 
 /**
 * @todo This document is getting too large. I need to subclass NSDocumentController and move
 * some of this functionality over there, as well as split up my MyDocument.nib file into
 * several smaller nib files.
 */
 
 public class MyDocument extends NSDocument implements Observer {
   private static final Logger log = Logger.getLogger(MyDocument.class);
 
   {
 //  	Tests.testBase64();
 //  	Tests.testImageFiles();
 //  	Tests.testTrimLeadingWhitespace();
 	log.debug(System.getProperty("user.dir"));
 	log.debug(System.getProperty("user.home"));
 //	System.exit(99);
 	log.debug("><><><><><><>><><><<><><><><><><><><><><><><><><> MyDocument initializer");
 	//initDoc();
   }
 
   public static final String GEDCOM = "GEDCOM File (.ged)";
   public static final String MACPAF = "MacPAF File";
   public static final String TEMPLEREADY_UPDATE = "TempleReady Update File";
 
   MacPAFDocumentJDOM doc;// = null;//new MacPAFDocumentJDOM();
 
   // Maps the buttons to the individuals they represent
   protected NSMutableDictionary individualsButtonMap = new NSMutableDictionary();
 
   /**
    * This is the main individual to whom apply all actions
    */
 //  private Individual primaryIndividual = new Individual.UnknownIndividual();
 
   // This was originally to check for the constructor getting called twice
 //  public boolean firstconstr = true;
 
   // All of the outlets in the nib
   public NSWindow mainWindow; /* IBOutlet */
   public NSWindow reportsWindow; /* IBOutlet */
   public NSWindow importWindow; /* IBOutlet */
   public NSWindow individualEditWindow; /* IBOutlet */
   public NSWindow familyEditWindow; /* IBOutlet */
   public FamilyListController familyListWindowController; /* IBOutlet */
   public IndividualListController individualListWindowController; /* IBOutlet */
   public FamilyListController tabFamilyListController; /* IBOutlet */
   public IndividualListController tabIndividualListController; /* IBOutlet */
   public FamilyList familyList; /* IBOutlet */
   public IndividualList individualList; /* IBOutlet */
   public SurnameList surnameList; /* IBOutlet */
   public LocationList locationList; /* IBOutlet */
   public HistoryController historyController; /* IBOutlet */
   public PedigreeViewController pedigreeViewController; /* IBOutlet */
   public NSButton fatherButton; /* IBOutlet */
   public NSButton individualButton; /* IBOutlet */
   public NSButton individualFamilyButton; /* IBOutlet */
   public NSButton maternalGrandfatherButton; /* IBOutlet */
   public NSButton maternalGrandmotherButton; /* IBOutlet */
   public NSButton motherButton; /* IBOutlet */
   public NSButton paternalGrandfatherButton; /* IBOutlet */
   public NSButton paternalGrandmotherButton; /* IBOutlet */
   public NSButton spouseButton; /* IBOutlet */
   public NSButton familyAsSpouseButton; /* IBOutlet */
   public NSButton familyAsChildButton; /* IBOutlet */
   public NSWindow noteWindow; /* IBOutlet */
   public NSTextView noteTextView; /* IBOutlet */
   public NSMatrix reportsRadio; /* IBOutlet */
   public NSTableView childrenTable; /* IBOutlet */
   public NSTableView spouseTable; /* IBOutlet */
   public PedigreeView pedigreeView; /* IBOutlet */
   public NSTabView mainTabView; /* IBOutlet */
   public NSWindow bugReportWindow; /* IBOutlet */
   public NSButton bugReportFileCheckbox; /* IBOutlet */
   public NSTextView bugReportText; /* IBOutlet */
 //  public IndividualDetailController individualDetailController = new IndividualDetailController(); /* IBOutlet */
 //  public FamilyDetailController familyDetailController = new FamilyDetailController(); /* IBOutlet */
   private NSView printableView;
   // used to suppress GUI updates while non-GUI updates are happening, for example during import
 private boolean suppressUpdates;
 
 //  private IndividualList startupIndividualList;
 //  private FamilyList startupFamilyList;
   /**
    * This the internal name for the gedcom file in the .macpaf file package
    */
   private static final String DEFAULT_GEDCOM_FILENAME = "data.ged";
   /**
    * This the internal name for the data xml file in the .macpaf file package
    */
   private static final String DEFAULT_XML_FILENAME = "data.xml";
 private static final String TEMPLEREADY_UPDATE_EXTENSION = "oup";
 
   /**
  * This method was originally started to avoid the bug that in Java-Cocoa applications,
  * constructors get called twice, so if you initialize anything in a constructor, it can get
  * nuked later by another constructor
  */
   private void initDoc() {
 	log.error("MyDocument.initDoc()");
 	try {
 	if (doc == null) {
 	  log.error("????????????????????????????????????????????????????? doc is null, making new doc");
 	  doc = new MacPAFDocumentJDOM();
 	}
 	}
 	catch (Exception e) {
 	  log.error("Exception: ", e); //To change body of catch statement use Options | File Templates.
 	}
   }
 
   public MyDocument() {
 	super();
 	log.error("MyDocument.MyDocument()");
 	initDoc();
   }
 
   public MyDocument(String fileName, String fileType) {
 	super(fileName, fileType);
 	log.error("MyDocument.MyDocument(" + fileName + ", " + fileType + ")");
 	initDoc();
   }
 
 //   public void closeFamilyEditSheet(Object sender) { /* IBAction */
 //      NSApplication.sharedApplication().stopModal();
 //      NSApplication.sharedApplication().endSheet(familyEditWindow);
 //      familyEditWindow.orderOut(this);
 //   }
 
 //   public void closeIndividualEditSheet(Object sender) { /* IBAction */
 //      NSApplication.sharedApplication().stopModal();
 //   }
 
   public void cancel(Object sender) { /* IBAction */
 //      NSApplication.sharedApplication().stopModal();
 	NSApplication.sharedApplication().endSheet(reportsWindow);
 	reportsWindow.orderOut(sender);
   }
 
   public void openFamilyEditSheet(Object sender) { /* IBAction */
 	( (NSWindowController) familyEditWindow.delegate()).setDocument(this);
 	if (sender instanceof NSControl) {
 	if ( (  (NSControl) sender).tag() == 1) {
 		Family familyAsChild = getPrimaryIndividual().getFamilyAsChild();
 		( (FamilyEditController) familyEditWindow.delegate()).setFamilyAsChild(familyAsChild);
 	} else {
 	  Family familyAsSpouse = getCurrentFamily();
 		( (FamilyEditController) familyEditWindow.delegate()).setFamily(familyAsSpouse);
 	}
 	} else if (sender instanceof NSValidatedUserInterfaceItem) {
 		if ( (  (NSValidatedUserInterfaceItem) sender).tag() == 1) {
 			Family familyAsChild = getPrimaryIndividual().getFamilyAsChild();
 			( (FamilyEditController) familyEditWindow.delegate()).setFamilyAsChild(familyAsChild);
 		} else {
 		  Family familyAsSpouse = getCurrentFamily();
 			( (FamilyEditController) familyEditWindow.delegate()).setFamily(familyAsSpouse);
 		}
 	
 	}
 	NSApplication nsapp = NSApplication.sharedApplication();
 	nsapp.beginSheet(familyEditWindow, mainWindow, this, new NSSelector("sheetDidEndShouldClose2", new Class[] {}), null);
 	// sheet is up here, control passes to the sheet controller
   }
   
   public void deletePrimaryIndividual(Object sender) { /* IBAction */
 	  log.debug("MyDocument.deletePrimaryIndividual()");
 	  String msg = "Are you sure you want to delete the individual named " + getPrimaryIndividual().getFullName() + "?";
 	  String details = "This will delete this person from your file and remove them from any family relationships.";
 	  boolean shouldDelete = confirmCriticalActionMessage(msg, details, "Delete", "Cancel");
 	  if (shouldDelete) {
 		  doc.removeIndividual(getPrimaryIndividual());
 	  }
 	  setPrimaryIndividual(doc.getPrimaryIndividual());
   }  
 
   public void deletePrimaryFamily(Object sender) { /* IBAction */
 	  log.debug("MyDocument.deletePrimaryFamily()");
 	  String msg = "Are you sure you want to delete the family with parents " + getCurrentFamily().getFather().getFullName() + " and "+ getCurrentFamily().getMother().getFullName()+"?";
 	  String details = "This will delete the relationship between the parents and children, but will leave the individual records.";
 	  boolean shouldDelete = confirmCriticalActionMessage(msg, details, "Delete", "Cancel");
 	  if (shouldDelete) {
 		  doc.removeFamily(getCurrentFamily());
 	  }
   }  
 
   private Individual getKnownPrimaryIndividual() {
 	Individual indi = getPrimaryIndividual();
 	if (indi instanceof Individual.UnknownIndividual) {
 		indi = createAndInsertNewIndividual();
 	}
 	return indi;
 }
 
 public void sheetDidEndShouldClose2() {
 	log.debug("sheetDidEndShouldClose2");
   }
 
   public void openIndividualEditSheet(Object sender) { /* IBAction */
 	( (NSWindowController) individualEditWindow.delegate()).setDocument(this);
 	NSApplication nsapp = NSApplication.sharedApplication();
 	nsapp.beginSheet(individualEditWindow, mainWindow, this, CocoaUtils.didEndSelector(), null);
 	// sheet is up here, control passes to the sheet controller
   }
   
   public void sheetDidEnd(NSWindow sheet, int returnCode, Object contextInfo) {
     log.debug("Called did-end selector");
     log.debug("sheetdidend contextInfo:"+contextInfo);
     try {
 		refreshData();
 		save();
 	} catch (Exception e) {
 		// TODO: handle exception
 		e.printStackTrace();
 	}
     sheet.orderOut(this);
   }  	
 
 
   public void editPrimaryIndividual(Object sender) { /* IBAction */
 	  openIndividualEditSheet(sender);
   }
   
   public void editCurrentFamily(Object sender) { /* IBAction */
 	  openFamilyEditSheet(sender);
   }
 	  
   public void printDocument(Object sender) { /* IBAction */
 	  openReportsSheet(sender);
   }
 		  
   public void openReportsSheet(Object sender) { /* IBAction */
 	( (NSWindowController) reportsWindow.delegate()).setDocument(this);
 //      if (!myCustomSheet)
 //          [NSBundle loadNibNamed: @"MyCustomSheet" owner: self];
 
 	NSApplication nsapp = NSApplication.sharedApplication();
 	log.debug("openReportsSheet mainWindow:" + mainWindow);
 //      reportsWindow.makeKeyAndOrderFront(this);
 	nsapp.beginSheet(reportsWindow, mainWindow, this, null, null);
 	//nsapp.runModalForWindow(reportsWindow);
 	//nsapp.endSheet(individualEditWindow);
 	//individualEditWindow.orderOut(this);
   }
 
   public void setPrintableView(Object sender) { /* IBAction */
 	try {
 		log.debug("setPrintableView selected=" + reportsRadio.selectedTag());
 		printInfo().setTopMargin(36);
 		printInfo().setBottomMargin(36);
 		printInfo().setLeftMargin(72);
 		printInfo().setRightMargin(36);
 		setPrintInfo(printInfo());
 		switch (reportsRadio.selectedTag()) {
 		  case 0:
 			printableView = new PedigreeView(new NSRect(0, 0,
 				printInfo().paperSize().width() - printInfo().leftMargin() - printInfo().rightMargin(),
 				printInfo().paperSize().height() - printInfo().topMargin() - printInfo().bottomMargin()), getPrimaryIndividual(),
 											 4);
 			break;
 		  case 1:
 			printableView = new FamilyGroupSheetView(new NSRect(0, 0,
 				printInfo().paperSize().width() - printInfo().leftMargin() - printInfo().rightMargin(),
 				printInfo().paperSize().height() - printInfo().topMargin() - printInfo().bottomMargin()), getCurrentFamily());
 			break;
 		  case 3:
 			printableView = new PocketPedigreeView(new NSRect(0, 0,
 				printInfo().paperSize().width() - printInfo().leftMargin() - printInfo().rightMargin(),
 				printInfo().paperSize().height() - printInfo().topMargin() - printInfo().bottomMargin()), getPrimaryIndividual(),
 											 6);
 			break;
 		  default:
 			printableView = new PedigreeView(new NSRect(0, 0,
 				printInfo().paperSize().width() - printInfo().leftMargin() - printInfo().rightMargin(),
 				printInfo().paperSize().height() - printInfo().topMargin() - printInfo().bottomMargin()), getPrimaryIndividual(),
 											 4);
 		}
 //      printableView = new PedigreeView(new NSRect(0,0,printInfo().paperSize().width()-printInfo().leftMargin()-printInfo().rightMargin(),printInfo().paperSize().height()-printInfo().topMargin()-printInfo().bottomMargin()), getPrimaryIndividual(), 4);
 //      printableView = new FamilyGroupSheetView(new NSRect(0,0,printInfo().paperSize().width()-printInfo().leftMargin()-printInfo().rightMargin(),printInfo().paperSize().height()-printInfo().topMargin()-printInfo().bottomMargin()), getPrimaryIndividual());
 //      printableView = new IndividualSummaryView(new NSRect(0,0,printInfo().paperSize().width()-printInfo().leftMargin()-printInfo().rightMargin(),printInfo().paperSize().height()-printInfo().topMargin()-printInfo().bottomMargin()), getPrimaryIndividual());
 
 	} catch (Exception e) {
 		e.printStackTrace();
 	}  
 }
 
 //   public void saveFamily(Object sender) { /* IBAction */
 //// save family info
 //      closeFamilyEditSheet(sender);
 //   }
 
 //   public void saveIndividual(Object sender) { /* IBAction */
 //// save individual info
 //      closeIndividualEditSheet(sender);
 //   }
   
   public Individual getPrimaryIndividual() {
 	return doc.getPrimaryIndividual();
   }
   
   public void setPrimaryIndividual(Individual newIndividual) {
 	  setPrimaryIndividualAndSpouse(newIndividual, newIndividual.getPreferredSpouse());
   }
   
 	  public void setPrimaryIndividualAndSpouse(Individual individual, Individual spouse) {
 	try {
 		Family familyAsSpouse = MyDocument.getFamilyForSpouse(individual, spouse);
 		
 		doc.setPrimaryIndividual(individual);
 		assignIndividualToButton(individual, individualButton);
 		// if primary individual is unknown, change button back to enabled
 		individualButton.setEnabled(true);
 		assignIndividualToButton(spouse, spouseButton);
 		assignIndividualToButton(individual.getFather(), fatherButton);
 		assignIndividualToButton(individual.getMother(), motherButton);
 		assignIndividualToButton(individual.getFather().getFather(), paternalGrandfatherButton);
 		assignIndividualToButton(individual.getFather().getMother(), paternalGrandmotherButton);
 		assignIndividualToButton(individual.getMother().getFather(), maternalGrandfatherButton);
 		assignIndividualToButton(individual.getMother().getMother(), maternalGrandmotherButton);
 		noteTextView.setString(individual.getNoteText());
 		pedigreeViewController.setPrimaryIndividual(individual);
 //		individualDetailController.setIndividual(individual);
 		log.debug("famsp id:"+familyAsSpouse.getId());
 		log.debug("famch id:"+individual.getFamilyAsChild().getId());
 		familyAsSpouseButton.setTitle("Family: "+familyAsSpouse.getId());
 		familyAsChildButton.setTitle("Family: "+individual.getFamilyAsChild().getId());
 		spouseTable.selectRow(0, false);
 		spouseTable.reloadData();
 		childrenTable.reloadData();
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
   }
 
   public static Family getFamilyForSpouse(Individual individual, Individual spouse) {
 	  Family matchingFamily = Family.UNKNOWN_FAMILY;
 	  for (Iterator iter = individual.getFamiliesAsSpouse().iterator(); iter.hasNext();) {
 		Family family = (Family) iter.next();		
 		  if (spouse.equals(family.getFather()) || spouse.equals(family.getMother())) {
 			  matchingFamily = family;
 		  }
 	  }
 	  return matchingFamily;
 	}
 
 public void setIndividual(Object sender) { /* IBAction */
 	try {
 		log.error("setIndividual to " + sender);
 		if (sender instanceof NSButton) {
 		  try {
 			log.debug("individuals=" + individualsButtonMap.objectForKey(sender.toString()));
 			Individual newIndividual = (Individual) individualsButtonMap.objectForKey(sender.toString());
 			if (newIndividual == null) {
 				newIndividual = Individual.UNKNOWN;
 			}
 			NSButton animateButton = (NSButton) sender;
 
 			NSPoint individualButtonOrigin = individualButton.frame().origin();
 			NSPoint animateButtonOrigin = animateButton.frame().origin();
 			log.debug("animating from " + animateButtonOrigin + " to " + individualButtonOrigin);
 			if (!animateButtonOrigin.equals(individualButtonOrigin)) {
 			  float stepx = (individualButtonOrigin.x() - animateButtonOrigin.x()) / 10;
 			  float stepy = (individualButtonOrigin.y() - animateButtonOrigin.y()) / 10;
 			  NSImage image = new NSImage();
 			  log.debug("animatebutton.bounds:" + animateButton.bounds());
 			  image.addRepresentation(new NSBitmapImageRep(animateButton.bounds()));
 			  NSImageView view = new NSImageView(animateButton.frame());
 			  view.setImage(image);
 			  animateButton.superview().addSubview(view);
 			  for (int steps = 0; steps < 10; steps++) {
 				animateButtonOrigin = new NSPoint(animateButtonOrigin.x() + stepx, animateButtonOrigin.y() + stepy);
 				view.setFrameOrigin(animateButtonOrigin);
 				view.display();
 			  }
 			  view.removeFromSuperview();
 			  animateButton.superview().setNeedsDisplay(true);
 			}
 			setPrimaryIndividual(newIndividual);
 		  }
 		  catch (Exception e) {
 			log.error("Exception: ", e);
 		  }
 		}
 		else if (sender instanceof NSTableView) {
 		  NSTableView tv = (NSTableView) sender;
 		  NSView superview = individualButton.superview();
 		  log.info("tableview selectedRow = "+tv.selectedRow());
 		  if (tv.selectedRow() >= 0) {
 			log.debug("individualList=" + individualsButtonMap.objectForKey("child" + tv.selectedRow()));
 			NSPoint individualButtonOrigin = individualButton.frame().origin();
 			Individual newIndividual = (Individual) individualsButtonMap.objectForKey("child" + tv.selectedRow());
 			if (tv.tag() == 2) {
 			  newIndividual = (Individual) individualsButtonMap.objectForKey("spouse" + tv.selectedRow());
 			  individualButtonOrigin = spouseButton.frame().origin();
 			}
 			NSRect rowRect = tv.convertRectToView(tv.rectOfRow(tv.selectedRow()), superview);
 			NSPoint tvOrigin = rowRect.origin();
 			log.debug("animating from " + tvOrigin + " to " + individualButtonOrigin);
 			float stepx = (individualButtonOrigin.x() - tvOrigin.x()) / 10;
 			float stepy = (individualButtonOrigin.y() - tvOrigin.y()) / 10;
 			NSImage image = new NSImage();
 			log.debug("rowrect:" + rowRect);
 			image.addRepresentation(new NSBitmapImageRep(rowRect));
 			NSImageView view = new NSImageView(rowRect);
 			view.setImage(image);
 			superview.addSubview(view);
 			for (int steps = 0; steps < 10; steps++) {
 			  tvOrigin = new NSPoint(tvOrigin.x() + stepx, tvOrigin.y() + stepy);
 			  view.setFrameOrigin(tvOrigin);
 			  view.display();
 			}
 			view.removeFromSuperview();
 			superview.setNeedsDisplay(true);
 			if (tv.tag() == 2) {
 			  setCurrentSpouse(newIndividual);
 			}
 else {
 			  setPrimaryIndividual(newIndividual);
 			}
 		  }
 		}
 		else if (sender instanceof IndividualList) {
 		  IndividualList iList = (IndividualList) sender;
 		  setPrimaryIndividual(iList.getSelectedIndividual());
 		}
 		else if (sender instanceof FamilyList) {
 		  FamilyList fList = (FamilyList) sender;
 		  setPrimaryIndividual(fList.getSelectedFamily().getFather());
 		}
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 
   }
 
   public String windowNibName() {
 	return "MyDocument";
   }
 
   public void windowControllerDidLoadNib(NSWindowController aController) {
 	try {
 	  super.windowControllerDidLoadNib(aController);
 
 	  // Add any code here that need to be executed once the windowController has loaded the document's window.
 	  mainWindow = aController.window();
 	  mainWindow.setToolbar(new MainToolbar());
 	  log.debug("mainWindow:" + mainWindow);
 	  log.debug("indivEditWindow:" + individualEditWindow);
 	  //myAction(this);
 	  NSAttributedString text = individualButton.attributedTitle();
 	  log.debug("indivButton attrTitle: " + text);
 //	  log.debug("indivButton attr at index 5:" + text.attributesAtIndex(5, null));
 	  log.debug("individualList: " + individualsButtonMap.count() + "(" + individualsButtonMap + ")");
 	  log.debug(" individualList="+individualList);
 	  log.debug("individualList: " + individualList.size()); // + "(" + indiMap + ")");
 	  individualList.document = this;
 	  familyList.document = this;
 //	  individualList.setIndividualMap(doc.getIndividualsMap());
 //	  familyList.setFamilyMap(doc.getFamiliesMap());
 	  if (individualList.size() > 0) {
 		log.debug("indimap");
 		assignIndividualToButton( (Individual) individualList.getFirstIndividual(), individualButton);
 		setIndividual(individualButton);
 	  }
 	  else {
 		log.info("!!! No individuals in file");
 		setPrimaryIndividual(Individual.UNKNOWN);
 	  }
 	  // add famMap to FamilyList
 //	  for (Iterator iter = famMap.values().iterator(); iter.hasNext(); ) {
 //		Family fam = (Family) iter.next();
 //		familyList.add(fam);
 //	  }
 //         NSImage testImage = new NSImage("/Users/logan/Pictures/iPhoto Library/Albums/Proposal/GQ.jpg", false);
 //         testImage.setSize(new NSSize(50f, 50f));
 //         testImage.setScalesWhenResized(true);
 	  log.debug("indivButton cell type: " + individualButton.cell().type());
 //         individualButton.setImage(testImage);
 //         save();
 	  // save newly opened document as preference
 	  log.debug("setting lastOpenedDocument to: " + fileName());
 	  NSUserDefaults.standardUserDefaults().setObjectForKey(fileName(), "com.redbugz.macpaf.lastOpenedDocument");
 	  tabFamilyListController.setup();
 	  tabFamilyListController.setDocument(this);
 	  tabIndividualListController.setup();
 	  tabIndividualListController.setDocument(this);
 	  // register as an observer of the MacPAFDocumentJDOM
 	  doc.addObserver(this);
 	}
 	catch (Exception e) {
 	  log.error("Exception: ", e); //To change body of catch statement use Options | File Templates.
 	}
   }
 
   public boolean readFromFile(String filename, String aType) {
 	log.debug("readfromfile type:" + aType + ":" + filename);
 	log.debug("********* readFromFile");
 	try {
 	  if (GEDCOM.equals(aType)) {
 		// import GEDCOM into this file
 		boolean success = super.readFromFile(filename, aType);
 		if (success) {
 		  importGEDCOM(new File(filename));
 		  setFileType(MACPAF);
 		  setFileName(null); //"ThisIsATest.macpaf");
 //						save();
 		}
 		return success;
 	  }
 	  if (!MACPAF.equals(aType)) {
 		try {
 //			            docFile = new File(filename);
 		  doc.loadXMLFile(new File(filename));
 		}
 		catch (Throwable e) {
 		  log.error("Exception: ", e); //To change body of catch statement use Options | File Templates.
 		  return false;
 		}
 		return true; //super.readFromFile(filename, aType);
 	  } else if (TEMPLEREADY_UPDATE.equalsIgnoreCase(aType)) {
 		  importTempleReadyUpdate(new File(filename));
 		  return false;
 	  }
 	  else {
 		return super.readFromFile(filename, aType);
 	  }
 	}
 	catch (Exception e) {
 	  // TODO Auto-generated catch block
 	  log.error("Exception: ", e);
 	  return false;
 	}
   }
 
   public NSFileWrapper fileWrapperRepresentationOfType(String s) {
 	log.debug("MyDocument.fileWrapperRepresentationOfType:" + s);
 	try {
 	  if (MACPAF.equals(s)) {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		ByteArrayOutputStream baosGed = new ByteArrayOutputStream();
 		try {
 //					out.output(
 //						doc,
 //						new FileWriter("/Projects/MacPAFTest/savetest.xml"));
 		  doc.outputToXML(baos);
 //		  doc.outputToXML(System.out);
 //					XMLTest.outputWithKay(
 //						doc,
 //						new FileOutputStream("/Projects/MacPAFTest/savetest.ged"));
 		  doc.outputToGedcom(baosGed);
 //		  doc.outputToGedcom(System.out);
 		}
 		catch (IOException e) {
 		  log.error("Exception: ", e);
 		}
 		NSFileWrapper mainFile = new NSFileWrapper(new NSDictionary());
 		NSFileWrapper imagesFolder =
 			new NSFileWrapper(new NSDictionary());
 		imagesFolder.setPreferredFilename("multimedia");
 		String images1 = mainFile.addFileWrapper(imagesFolder);
 		//      NSData data = new NSAttributedString("some data goes
 		// here").RTFFromRange(new NSRange(0, 15), new NSDictionary());
 		//      mainFile.addRegularFileWithContents(NSPropertyListSerialization.dataFromPropertyList(individuals),
 		// "individuals.plist");
 		//         for (int i=0; i< 10000; i++) {
 		//            families.takeValueForKey("familyvalue"+i, ""+i);
 		//         }
 
 		//         notes.takeValueForKey("notevalue", "notekey");
 		//         log.debug("start serialize");
 		//      String plist1 =
 		// mainFile.addRegularFileWithContents(NSPropertyListSerialization.XMLDataFromPropertyList(families),
 		// "families.plist");
 		//      String plist2 =
 		// mainFile.addRegularFileWithContents(NSPropertyListSerialization.XMLDataFromPropertyList(notes),
 		// "notes.plist");
 		//         log.debug("end serialize");
 //				String file1 =
 //					imagesFolder.addFileWithPath(
 //						"/Projects/MacPAFTest/macpaf-screenshot.png");
 //				String file2 =
 //					imagesFolder.addFileWithPath(
 //						"/Projects/MacPAFTest/macpaf-screenshot.tiff");
 //				String file3 =
 //					imagesFolder.addFileWithPath(
 //						"/Projects/MacPAFTest/macpaf-screenshot.jpg");
 //				String file4 =
 //					imagesFolder.addFileWithPath(
 //						"/Projects/Mcreenshot.gif");
 		String dataXmlFilename = DEFAULT_XML_FILENAME;
 		String dataGedcomFilename = DEFAULT_GEDCOM_FILENAME;
 //		if (fileName() != null && fileName().length() > 0) {
 //		  dataXmlFilename = fileName()+".xml";
 //		  dataGedcomFilename = fileName()+".ged";
 //}
 		String xml1 =
 			mainFile.addRegularFileWithContents(
 			new NSData(baos.toByteArray()),
 			dataXmlFilename);
 		String ged1 =
 			mainFile.addRegularFileWithContents(
 			new NSData(baosGed.toByteArray()),
 			dataGedcomFilename);
 		log.debug(""
 //					"file1="
 //						+ file1
 //						+ ", file2="
 //						+ file2
 //						+ ", file3="
 //						+ file3
 //						+ ", file4="
 //						+ file4
 				  + ", images1="
 				  + images1
 				  + ", xml1="
 				  + xml1
 				  + ", ged1="
 				  + ged1);
 		return mainFile; //super.fileWrapperRepresentationOfType(s);
 	  }
 	  // unknown file type
 	  log.error("fileWrapperRep unknown file type " + s);
 	  return null;
 	}
 	catch (Exception e) {
 	  // TODO Auto-generated catch block
 	  log.error("Exception: ", e);
 	  return null;
 	}
   }
 
   public boolean loadFileWrapperRepresentation(NSFileWrapper nsFileWrapper, String s) {
 	log.debug("MyDocument.loadFileWrapperRepresentation:" + nsFileWrapper + ":" + s);
 	try {
 	  log.debug("wrapper isDir:" + nsFileWrapper.isDirectory());
 	  log.debug("attributes:" + nsFileWrapper.fileAttributes());
 	  log.debug("filename: " + fileName() + " type:" + fileType());
 	  log.debug("setFileAttr result=" +
 				NSPathUtilities.setFileAttributes(fileName() + "/" + DEFAULT_GEDCOM_FILENAME,
 												  new
 												  NSDictionary(new Integer(NSHFSFileTypes.hfsTypeCodeFromFileType(
 		  "TEXT")),
 		  CocoaUtils.FileHFSTypeCode)));
 	  String[] errors = new String[1];
 	  if (MACPAF.equals(s)) {
 		if (nsFileWrapper.isDirectory()) {
 		  log.debug("wrappers:" + nsFileWrapper.fileWrappers());
 		}
 //			        NSFileWrapper familiesPlist = (NSFileWrapper) nsFileWrapper.fileWrappers().valueForKey("families.plist");
 //			        log.error("start extract");
 //			        families = (NSMutableDictionary) NSPropertyListSerialization.propertyListFromData(familiesPlist.regularFileContents(), NSPropertyListSerialization.PropertyListMutableContainersAndLeaves, new int[]{NSPropertyListSerialization.PropertyListXMLFormat}, errors);
 //			        log.error("end extract");
 		Enumeration en = nsFileWrapper.fileWrappers().objectEnumerator();
 		while (en.hasMoreElements()) {
 		  NSFileWrapper wrapper = ( (NSFileWrapper) en.nextElement());
 		  log.error(wrapper.filename() + " subattr:" + wrapper.fileAttributes());
 		  if (NSPathUtilities.pathExtension(wrapper.filename()).equalsIgnoreCase("ged")) {
 			String fullPath = fileName() + "/" + wrapper.filename();
 			log.debug("..................Loading gedcom: " + fullPath);
 			doc.loadXMLFile(new File(fullPath));
 			// save individualList in a temporary so I can restore it when the
 			// Cocoa startup sequence calls the constructor twice and clobbers it
 //			startupIndividualList = individualList;
 		  }
 		}
 	  }
 	  return true;
 	}
 	catch (Exception e) {
 	  // TODO Auto-generated catch block
 	  log.error("Exception: ", e);
 	}
 	showUserErrorMessage("There was a problem opening the file:\n"+nsFileWrapper.filename()+".",
 			"This is usually caused by a corrupt file. If the file is a GEDCOM file, it may not be the right version"+
 			" or it may not conform to the GEDCOM 5.5 specification. If this is a MacPAF file, it is missing "+
 			"some required data or the data has become corrupted. This can often be fixed by manually inspecting "+
 			"the contents of the file. Please contact support to see if this can be fixed.");
 	return false;
   }
 
   /**
  * @param string
  * @param string2
  */
 public static void showUserErrorMessage(String message, String details) {
 	NSAlertPanel.runCriticalAlert(message, details, "OK", null, null);
 }
 
 /**
  * @param string
  * @param string2
  */
 public static boolean confirmCriticalActionMessage(String message, String details, String confirmActionText, String cancelActionText) {
 	int result = NSAlertPanel.runCriticalAlert(message, details, confirmActionText, cancelActionText, null);
 	return result == NSAlertPanel.DefaultReturn;
 }
 
 
 /* (non-Javadoc)
    * @see com.apple.cocoa.application.NSDocument#setFileName(java.lang.String)
    */
   public void setFileName(String arg0) {
 	try {
 	  super.setFileName(arg0);
 	  log.debug("setFilename setting lastOpenedDocument to "+fileName());
 	  NSUserDefaults.standardUserDefaults().setObjectForKey(fileName(), "com.redbugz.macpaf.lastOpenedDocument");
 	  log.debug("mydoc.setfilename() done." + arg0);
 	  log.debug("setfilename set file attrs result=" +
 				NSPathUtilities.setFileAttributes(arg0 + "/" + DEFAULT_GEDCOM_FILENAME,
 												  new NSDictionary(new Integer(
 		  NSHFSFileTypes.hfsTypeCodeFromFileType("'TEXT'")), CocoaUtils.FileHFSTypeCode)));
 //	  log.debug("setfilename " + arg0 + "/" + DEFAULT_GEDCOM_FILENAME + " attr aft:" +
 //				NSPathUtilities.fileAttributes(arg0 + "/" + DEFAULT_GEDCOM_FILENAME, false));
 	}
 	catch (Exception e) {
 	  // TODO Auto-generated catch block
 	  log.error("Exception: ", e);
 	}
   }
 
   public void addFamily(Family newFamily) {
 	try {
 	  doc.addFamily(newFamily);
 	  // TODO notification
 //	  if (familyListTableView != null) {
 //		familyListTableView.reloadData();
 //	  }
 	}
 	catch (Exception ex) {
 	  ex.printStackTrace();
 	}
   }
 
   public void addIndividual(Individual newIndividual) {
 	try {
 	  doc.addIndividual(newIndividual);
 	  log.debug("added newIndividual with key: "+newIndividual.getId()+" newIndividual name: "+newIndividual.getFullName());
 	  // TODO notification
 //	  if (individualListTableView != null) {
 //		individualListTableView.reloadData();
 //	  }
 	}
 	catch (Exception ex) {
 	  ex.printStackTrace(); /** @todo figure out what to do here */
 	}
   }
 
 //  public NSData dataRepresentationOfType(String aType) {
 //	// Insert code here to create and return the data for your document.
 //	log.debug("MyDocument.dataRepresentationOfType():" + aType);
 //	return new NSAttributedString("some data goes here").RTFFromRange(new NSRange(0, 15), new NSDictionary());
 //  }
 //
 //  public boolean loadDataRepresentation(NSData data, String aType) {
 //	// Insert code here to read your document from the given data.
 //	log.debug("MyDocument.loadDataRepresentation():" + aType);
 //	log.debug("load data:" + aType + ":" + new NSStringReference(data, NSStringReference.ASCIIStringEncoding));
 //	return true;
 //  }
 
   private void assignIndividualToButton(Individual indiv, NSButton button) {
 	try {
 		button.setObjectValue(indiv);
 		NSAttributedString newLine = new NSAttributedString("\n");
 		NSMutableAttributedString nameText = new NSMutableAttributedString(indiv.getFullName());
 		button.setEnabled(true);
 		if (indiv instanceof Individual.UnknownIndividual) {
 		  nameText = new NSMutableAttributedString("Unknown");
 		  button.setEnabled(false);
 		}
 		NSMutableAttributedString birthdateText = new NSMutableAttributedString("");
 		NSMutableAttributedString birthplaceText = new NSMutableAttributedString("");
 		NSMutableAttributedString ordinancesText = makeOrdinanceString(indiv);
 		if (indiv.getBirthEvent() != null) {
 		  birthdateText = new NSMutableAttributedString(indiv.getBirthEvent().getDateString());
 		  if (indiv.getBirthEvent().getPlace() != null) {
 			birthplaceText = new NSMutableAttributedString(indiv.getBirthEvent().getPlace().getFormatString());
 		  }
 		}
 		NSMutableAttributedString text = nameText;
 		text.appendAttributedString(newLine);
 		text.appendAttributedString(birthdateText);
 		text.appendAttributedString(newLine);
 		text.appendAttributedString(birthplaceText);
 		text.appendAttributedString(newLine);
 		text.appendAttributedString(ordinancesText);
 		button.setAttributedTitle(text);
 //	URL imageURL = indiv.getImagePath();
 //	if (imageURL != null && imageURL.toString().length() > 0) {
 		  NSImage testImage = MultimediaUtils.makeImageFromMultimedia(indiv.getPreferredImage());
 //		  log.debug("button image:"+testImage);
 		  if (!testImage.size().isEmpty()) {
 			  testImage.setSize(new NSSize(50f, 50f));
 			  testImage.setScalesWhenResized(true);
 			  button.setImage(testImage);
 		  }
 //	}
 		individualsButtonMap.setObjectForKey(indiv, button.toString());
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
   }
 
 private NSMutableAttributedString makeOrdinanceString(Individual individual) {
 	NSMutableAttributedString ordinanceString = new NSMutableAttributedString("BEPSC");
 	if (individual.getLDSBaptism().isCompleted()) {
 		ordinanceString.applyFontTraitsInRange(NSAttributedString.UnderlineStrikethroughMask, new NSRange(0,1));
 	}
 	if (individual.getLDSEndowment().isCompleted()) {
 		ordinanceString.applyFontTraitsInRange(NSAttributedString.UnderlineStrikethroughMask, new NSRange(1,1));
 	}
 	if (individual.getLDSSealingToParents().isCompleted()) {
 		ordinanceString.applyFontTraitsInRange(NSAttributedString.UnderlineStrikethroughMask, new NSRange(2,1));
 	}
 	if (individual.getPreferredFamilyAsSpouse().getSealingToSpouse().isCompleted()) {
 		ordinanceString.applyFontTraitsInRange(NSAttributedString.UnderlineStrikethroughMask, new NSRange(3,1));
 	}
 	if (individual.childrensOrdinancesAreCompleted()) {
 		ordinanceString.applyFontTraitsInRange(NSAttributedString.UnderlineStrikethroughMask, new NSRange(4,1));
 	}
 	return ordinanceString;
 }
 
   public int numberOfRowsInTableView(NSTableView nsTableView) {
 	try {
 		log.debug("MyDocument.numberOfRowsInTableView():" + nsTableView.tag());
 		if (nsTableView.tag() == 1) {
 		  if (getPrimaryIndividual().getPreferredFamilyAsSpouse() != null) {
 			int numChildren = getCurrentFamily().getChildren().size();
 			log.debug("numberOfRowsInTableView children: " + numChildren);
 			return numChildren;
 		  }
 		}
 		else if (nsTableView.tag() == 2) {
 		  int numSpouses = getPrimaryIndividual().getSpouseList().size();
 		  log.debug("numberOfRowsInTableView spouses: " + numSpouses);
 		  return numSpouses;
 		}
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 	return 0;
   }
 
   public Object tableViewObjectValueForLocation(NSTableView nsTableView, NSTableColumn nsTableColumn, int i) {
 	try {
 //			log.debug("nsTableColumn=" + nsTableColumn.identifier()+"; i="+i+"; tag="+nsTableView.tag());
 	  if (nsTableView.tag() == 1) {
 		if (nsTableColumn.identifier() != null && nsTableColumn.identifier().equals("num")) {
 		  nsTableColumn.setWidth(5.0f);
 		  return String.valueOf(i + 1);
 		}
 		Individual individual = (Individual) getCurrentFamily().getChildren().get(i);
 		individualsButtonMap.setObjectForKey(individual, "child" + i);
 		return individual.getFullName();
 	  }
 	  else if (nsTableView.tag() == 2) {
 		Individual individual = (Individual) getPrimaryIndividual().getSpouseList().get(i);
 		individualsButtonMap.setObjectForKey(individual, "spouse" + i);
 		return individual.getFullName();
 	  }
 	  return "Unknown";
 	}
 	catch (Exception e) {
 	  // TODO Auto-generated catch block
 	  log.error("Exception: ", e);
 	  return "An Error Has Occurred";
 	}
   }
   
   // used to display row in bold if the child also has children
   public void tableViewWillDisplayCell(NSTableView aTableView, Object aCell, NSTableColumn aTableColumn, int rowIndex) {
 //	  log.debug("MyDocument.tableViewWillDisplayCell():"+aTableView.tag()+":"+aCell+":"+aTableColumn.headerCell().stringValue()+":"+rowIndex);
 //	  Thread.dumpStack();
 	  try {
 		  if (aTableView.tag() == 1 ) {
 			  if (aCell instanceof NSCell) {
 				  NSCell cell = (NSCell) aCell;
 //				  log.debug("cell str val:"+cell.stringValue());
 				  Individual child = (Individual) getCurrentFamily().getChildren().get(rowIndex);
 				  if (child.getPreferredFamilyAsSpouse().getChildren().size() > 0) {
 //					  log.debug("bolding child name:"+child.getFullName());
 					  cell.setFont(NSFontManager.sharedFontManager().convertFontToHaveTrait(cell.font(), NSFontManager.BoldMask));
 				  } else {
 //					  log.debug("unbolding child name:"+child.getFullName());
 					  cell.setFont(NSFontManager.sharedFontManager().convertFontToNotHaveTrait(cell.font(), NSFontManager.BoldMask));
 				  }
 			  }
 		  }
 	  } catch (RuntimeException e) {
 		  // TODO Auto-generated catch block
 		  e.printStackTrace();
 	  }
   }
   
   private Family getCurrentFamily() {
 	  Family result = getPrimaryIndividual().getPreferredFamilyAsSpouse();
 	  List families = getPrimaryIndividual().getFamiliesAsSpouse();
 	  if (getPrimaryIndividual().getSpouseList().size() > 0 && spouseTable.selectedRow() >= 0) {
 	  Individual selectedSpouse = (Individual) getPrimaryIndividual().getSpouseList().get(spouseTable.selectedRow());
 	  for (Iterator iter = families.iterator(); iter.hasNext();) {
 		Family family = (Family) iter.next();
 			if (family.getMother().getId().equals(selectedSpouse.getId()) || family.getFather().getId().equals(selectedSpouse.getId())) {
 				result = family;
 			}
 	  	}
 	  }
 	return result;
 }
 
   private void setCurrentSpouse(Individual spouse) {
 	  log.debug("MyDocument.setCurrentSpouse():"+spouse.getFullName());
 		assignIndividualToButton(spouse, spouseButton);
 		  familyAsSpouseButton.setTitle("Family: "+getCurrentFamily().getId());
 		  childrenTable.reloadData();
 	}
 
   public void setCurrentFamily(Family family) {
 	  setPrimaryIndividual(family.getFather());
 	  setCurrentSpouse(family.getMother());
   }
 
 public void printShowingPrintPanel(boolean showPanels) {
 	log.debug("printshowingprintpanel:" + showPanels);
 	try {
 		// Obtain a custom view that will be printed
 		printInfo().setTopMargin(36);
 		printInfo().setBottomMargin(36);
 		printInfo().setLeftMargin(72);
 		printInfo().setRightMargin(36);
 		setPrintInfo(printInfo());
 //       NSView printView = printableView;
 
 		// Construct the print operation and setup Print panel
 		NSPrintOperation op = NSPrintOperation.printOperationWithView(printableView, printInfo());
 		log.debug("papersize: " + printInfo().paperSize());
 		log.debug("left margin: " + printInfo().leftMargin());
 		log.debug("right margin: " + printInfo().rightMargin());
 		log.debug("top margin: " + printInfo().topMargin());
 		log.debug("bottom margin: " + printInfo().bottomMargin());
 		op.setShowPanels(showPanels);
 		if (showPanels) {
 		  // Add accessory view, if needed
 		}
 
 		// Run operation, which shows the Print panel if showPanels was YES
 		runModalPrintOperation(op, null, null, null);
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
   }
 
 //   private NSView printableView() {
 //      return printableView;
 //      return new PedigreeView(new NSRect(0,0,printInfo().paperSize().width()-printInfo().leftMargin()-printInfo().rightMargin(),printInfo().paperSize().height()-printInfo().topMargin()-printInfo().bottomMargin()), getPrimaryIndividual(), 4);
 //      return new FamilyGroupSheetView(new NSRect(0,0,printInfo().paperSize().width()-printInfo().leftMargin()-printInfo().rightMargin(),printInfo().paperSize().height()-printInfo().topMargin()-printInfo().bottomMargin()), getPrimaryIndividual());
 //      return new IndividualSummaryView(new NSRect(0,0,printInfo().paperSize().width()-printInfo().leftMargin()-printInfo().rightMargin(),printInfo().paperSize().height()-printInfo().topMargin()-printInfo().bottomMargin()), getPrimaryIndividual());
 //   }
 
   /**
    * Saves this document to disk with all files in the file package
    */
   public void save() {
 	log.debug("___ save()");
 	long watch = System.currentTimeMillis();
 	// call the standard Cocoa save action
 	saveDocument(this);
 	log.debug("it took "+(System.currentTimeMillis()-watch)+" ms to save files");
 
 //	individualListWindowController.refreshData();
 //	familyListWindowController.refreshData();
 	log.debug("MyDocument.save() end ("+(System.currentTimeMillis()-watch)+" ms), filename=" + fileName());
   }
   
   public void openImportSheet(Object sender) { /* IBAction */
 		log.debug("openImportSheet");
 		NSApplication nsapp = NSApplication.sharedApplication();
 		nsapp.beginSheet(importWindow, mainWindow, this, CocoaUtils.didEndSelector(), null);
 		// sheet is up here, control passes to the sheet controller
   }
 
   public void importFile(Object sender) { /* IBAction */
 	  log.debug("importFile: " + sender);
 	  try {
 		  //        NSApplication nsapp = NSApplication.sharedApplication();
 		  //        nsapp.beginSheet(importWindow, mainWindow, this, null, null);
 		  NSOpenPanel panel = NSOpenPanel.openPanel();
 		  //panther only?        panel.setMessage("Please select a GEDCOM file to import into this MacPAF file.");
 		  panel.beginSheetForDirectory(null, null, new NSArray(new Object[] {"GED"}), mainWindow,
 				  this,
 				  new NSSelector("openPanelDidEnd", new Class[] {NSOpenPanel.class, int.class, Object.class}), null);
 	  } catch (Exception e) {
 		  // TODO Auto-generated catch block
 		  e.printStackTrace();
 	  }
   }
 
   public void openPanelDidEnd(NSOpenPanel sheet, int returnCode, Object contextInfo) {
 	if (returnCode == NSPanel.OKButton) {
 	  log.debug("import filename:" + sheet.filename());
 	  if (TEMPLEREADY_UPDATE_EXTENSION.equalsIgnoreCase(NSPathUtilities.pathExtension(sheet.filename()))) {
 		  importTempleReadyUpdate(new File(sheet.filename()));
 	  }
 	  importGEDCOM(new File(sheet.filename()));
 	}
   }
 
   private void importTempleReadyUpdate(File file) {
 	  showUserErrorMessage("We're sorry, but we do not yet handle TempleReady Update Files", "This functionality will be added later. Look for a future update.");
   }
 
 private void importGEDCOM(File importFile) {
 	log.debug("MyDocument.importGEDCOM():" + importFile);
 	try {
 		doc.loadXMLFile(importFile);
 		if (getPrimaryIndividual().equals(Individual.UNKNOWN)) {
 		  // set first individual in imported file to primary individual
 		  setPrimaryIndividual(individualList.getSelectedIndividual());
   }
 //  individualListTableView.reloadData();
 //  familyListTableView.reloadData();
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
   }
 
   public void exportFile(Object sender) { /* IBAction */
 	log.debug("exportFile: " + sender);
 try {
 	//        save();
 		NSSavePanel panel = NSSavePanel.savePanel();
 		// Jaguar-only code
 //		panel.setCanSelectHiddenExtension(true);
 //		panel.setExtensionHidden(false);
 	//panther only?        panel.setMessage("Choose the name and location for the exported GEDCOM file.\n\nThe file name should end with .ged");
 	//        panel.setNameFieldLabel("Name Field Label:");
 	//        panel.setPrompt("Prompt:");
 		panel.setRequiredFileType("ged");
 	//        panel.setTitle("Title");
 		panel.beginSheetForDirectory(null, null, mainWindow, this,
 									 new NSSelector("savePanelDidEndReturnCode", new Class[] {NSSavePanel.class, int.class,
 													Object.class}), null);
 } catch (Exception e) {
 	// TODO Auto-generated catch block
 	e.printStackTrace();
 }
   }
 
   public void savePanelDidEndReturnCode(NSSavePanel sheet, int returnCode, Object contextInfo) {
 	log.debug("MyDocument.savePanelDidEndReturnCode(sheet, returnCode, contextInfo):" + sheet + ":" + returnCode + ":" +
 			  contextInfo);
 	if (returnCode == NSPanel.OKButton) {
 	  log.debug("export filename:" + sheet.filename());
 	  try {
 			// bring up selection window to choose what to export
 		  
 		  if (contextInfo.toString().indexOf("oup") > 0) {
 				doc.outputToTempleReady(new FileOutputStream(sheet.filename()));
 		  }
 		doc.outputToGedcom(new FileOutputStream(sheet.filename()));
 	  }
 	  catch (Exception e) {
 		log.error("Exception: ", e); //To change body of catch statement use Options | File Templates.
 	  }
 	}
 	else if (returnCode == NSPanel.CancelButton) {
 	  log.debug("save panel cancelled, sheet filename=" + sheet.filename() + ", doc filename=" + fileName());
 	  if (fileName() == null || fileName().length() == 0) {
 		log.debug("cancel with null filename, should I close the document?");
 //          close();
 	  }
 	}
   }
 
   public void windowDidBecomeMain(NSNotification aNotification) {
 	log.debug("MyDocument.windowDidBecomeMain()");
 	save();
   }
 
   public Individual createAndInsertNewIndividual() {
 	return doc.createAndInsertNewIndividual();//new IndividualJDOM(doc);
   }
 
   public Family createAndInsertNewFamily() {
 	return doc.createAndInsertNewFamily();//new IndividualJDOM(doc);
   }
 
   /**
    * prepareSavePanel
    *
    * @param nSSavePanel NSSavePanel
    * @return boolean
    * @todo Implement this com.apple.cocoa.application.NSDocument method
    */
   public boolean prepareSavePanel(NSSavePanel nSSavePanel) {
 	log.debug("MyDocument.prepareSavePanel(nSSavePanel):" + nSSavePanel);
 	nSSavePanel.setDelegate(this);
 	return true;
   }
 
   public boolean panelIsValidFilename(Object sender,
 									  String filename) {
 	log.debug("MyDocument.panelIsValidFilename(sender, filename):" + sender + ":" + filename);
 	return true;
   }
 
   public void addNewIndividual(Object sender) { /* IBAction */
 	log.debug("addNewIndividual: " + sender);
 	try {
 		Individual newIndividual = createAndInsertNewIndividual();
 //		addIndividual(newIndividual);
 		setPrimaryIndividual(newIndividual);
 		save();
 		openIndividualEditSheet(this);
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
   }
 
   public void addNewChild(Object sender) { /* IBAction */
 	log.debug("addNewChild: " + sender);
 	try {
 		Family familyAsSpouse = getPrimaryIndividual().getPreferredFamilyAsSpouse();
 		if (familyAsSpouse instanceof Family.UnknownFamily) {
 			familyAsSpouse = createAndInsertNewFamily();
 			if (Gender.MALE.equals(getPrimaryIndividual().getGender())) {
 				familyAsSpouse.setFather(getPrimaryIndividual());
 			} else {
 				familyAsSpouse.setMother(getPrimaryIndividual());
 			}
 			getPrimaryIndividual().setFamilyAsSpouse(familyAsSpouse);
 		}
 		
 		Individual newChild = createAndInsertNewIndividual();
 		familyAsSpouse.addChild(newChild);
 		newChild.setFamilyAsChild(familyAsSpouse);
 		setPrimaryIndividual(newChild);
 		save();
 		openIndividualEditSheet(this);
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
   }
 
   public void addNewSpouse(Object sender) { /* IBAction */
 	log.debug("addNewSpouse: " + sender);
 	try {
 		boolean isMale = Gender.MALE.equals(getPrimaryIndividual().getGender());
 		Family familyAsSpouse = getPrimaryIndividual().getPreferredFamilyAsSpouse();
 		if (familyAsSpouse instanceof Family.UnknownFamily) {
 			familyAsSpouse = createAndInsertNewFamily();
 			if (isMale) {
 				familyAsSpouse.setFather(getPrimaryIndividual());
 			} else {
 				familyAsSpouse.setMother(getPrimaryIndividual());
 			}
 			getPrimaryIndividual().setFamilyAsSpouse(familyAsSpouse);
 		}
 		Individual newSpouse = createAndInsertNewIndividual();
 		getPrimaryIndividual().addSpouse(newSpouse);
 		newSpouse.addSpouse(getPrimaryIndividual());
 		setPrimaryIndividual(newSpouse);
 		save();
 		openIndividualEditSheet(this);
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
   }
 
   public void addNewFather(Object sender) { /* IBAction */
 	log.debug("addNewFather: " + sender);
 	try {
 		Family familyAsChild = getPrimaryIndividual().getFamilyAsChild();
 		if (familyAsChild instanceof Family.UnknownFamily) {
 			familyAsChild = createAndInsertNewFamily();
 			familyAsChild.addChild(getPrimaryIndividual());
 			getPrimaryIndividual().setFamilyAsChild(familyAsChild);
 		}
 		Individual newFather = createAndInsertNewIndividual();
 		newFather.setGender(Gender.MALE);
 		newFather.setSurname(getPrimaryIndividual().getSurname());
 		newFather.setFamilyAsSpouse(familyAsChild);
 		familyAsChild.setFather(newFather);
 		setPrimaryIndividual(newFather);
 		save();
 		openIndividualEditSheet(this);
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
   }
 
   public void addNewMother(Object sender) { /* IBAction */
 	log.debug("addNewMother: " + sender);
 	try {
 		Family familyAsChild = getPrimaryIndividual().getFamilyAsChild();
 		if (familyAsChild instanceof Family.UnknownFamily) {
 			familyAsChild = createAndInsertNewFamily();
 			familyAsChild.addChild(getPrimaryIndividual());
 			getPrimaryIndividual().setFamilyAsChild(familyAsChild);
 		}
 		Individual newMother = createAndInsertNewIndividual();
 		newMother.setGender(Gender.FEMALE);
 		newMother.setFamilyAsSpouse(familyAsChild);
 		familyAsChild.setMother(newMother);
 		setPrimaryIndividual(newMother);
 		save();
 		openIndividualEditSheet(this);
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
   }
 
   public void addNewFamily(Object sender) { /* IBAction */
 	log.debug("addNewFamily: " + sender);
 	try {
 		// I think the family edit sheet takes care of creating a new family
 //		Family newFamily = createAndInsertNewFamily();
 //		save();
 		openFamilyEditSheet(this);
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
   }
 
   public void addNewFamilyAsSpouse(Object sender) { /* IBAction */
 	log.debug("addNewFamilyAsSpouse: " + sender);
 	try {
 		Family newFamily = createAndInsertNewFamily();
 		getPrimaryIndividual().setFamilyAsSpouse(newFamily);
 		if (Gender.MALE.equals(getPrimaryIndividual().getGender())) {
 			newFamily.setFather(getPrimaryIndividual());
 		} else {
 			newFamily.setMother(getPrimaryIndividual());
 		}
 		save();
 		openFamilyEditSheet(this);
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
   }
 
   public void addNewFamilyAsChild(Object sender) { /* IBAction */
 	log.debug("addNewFamilyAsChild: " + sender);
 	try {
 		Family newFamily = createAndInsertNewFamily();
 		getPrimaryIndividual().setFamilyAsChild(newFamily);
 		newFamily.addChild(getPrimaryIndividual());
 		save();
 		openFamilyEditSheet(this);
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
   }
   
   public void displayFamilyView(Object sender) { /* IBAction */
 	  mainTabView.selectFirstTabViewItem(sender);
   }
 
   public void displayPedigreeView(Object sender) { /* IBAction */
 	  mainTabView.selectTabViewItemAtIndex(1);
   }
 
   public void displayIndividualListView(Object sender) { /* IBAction */
 	  mainTabView.selectTabViewItemAtIndex(2);
   }
 
   public void displayFamilyListView(Object sender) { /* IBAction */
 	  mainTabView.selectLastTabViewItem(sender);
   }
 
   public void showFamilyList(Object sender) { /* IBAction */
 	log.debug("showFamilyList: " + sender);
 	try {
 		if (familyListWindowController == null) {
 			familyListWindowController = new FamilyListController(familyList);
 		}
 		addWindowController(familyListWindowController);
 		familyListWindowController.showWindow(this);
 //		log.debug(familyListWindowController.familyCountText.stringValue());
 //		log.debug(familyListWindowController.window());
 //		log.debug(	familyListWindowController.owner());
 //		familyListWindowController.window().makeKeyAndOrderFront(this);
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
   }
 
   public void showIndividualList(Object sender) { /* IBAction */
 	log.debug("showIndividualList: " + sender);
 	try {
 		if (individualListWindowController == null) {
 			individualListWindowController = new IndividualListController(individualList);
 		}
 		addWindowController(individualListWindowController);
 		individualListWindowController.showWindow(this);
 	} catch (Exception e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
   }
 
   public void showBugReportWindow(Object sender) { /* IBAction */
 	log.debug("showBugReportWindow: " + sender);
 	if (bugReportWindow != null) {
 		bugReportText.setString("");
 		bugReportWindow.makeKeyAndOrderFront(sender);
 	}
   }
   
   public void transmitBugReport(Object sender) { /* IBAction */
 		log.debug("transmitBugReport: " + sender);
 	    // These are the files to include in the ZIP file		
 		NSMutableArray filePaths = new NSMutableArray();
 		NSArray searchPaths = NSPathUtilities.searchPathForDirectoriesInDomains(NSPathUtilities.LibraryDirectory, NSPathUtilities.UserDomainMask, true);
 		if (searchPaths.count() > 0) {
 			String logFileDirectoryPath = NSPathUtilities.stringByAppendingPathComponent((String) searchPaths.objectAtIndex(0), "Logs");
 			log.debug("logFileDirectoryPath:"+logFileDirectoryPath);
 			String logFileBasePath = NSPathUtilities.stringByAppendingPathComponent(logFileDirectoryPath, "macpaf.log");
 			log.debug("logFileBasePath:"+logFileBasePath);
			NSArray logFileExtensions = new NSArray(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"});
 			log.debug("logFileExtensions:"+logFileExtensions);
			String logFilePath = NSPathUtilities.stringByStandardizingPath(logFileBasePath);
			log.debug("logFilePath:"+logFilePath);
			if (new File(logFilePath).exists()) {
				log.debug(logFilePath+" exists!");
				filePaths.addObject(logFilePath);
			}
 			Enumeration extensions = logFileExtensions.objectEnumerator();
 			while (extensions.hasMoreElements()) {
 				String extension = (String) extensions.nextElement();
				logFilePath = NSPathUtilities.stringByStandardizingPath(NSPathUtilities.stringByAppendingPathExtension(logFileBasePath, extension));
 				log.debug("logFilePath:"+logFilePath);
 				if (new File(logFilePath).exists()) {
 					log.debug(logFilePath+" exists!");
 					filePaths.addObject(logFilePath);
 				}
 			}
 		}
 		if (bugReportFileCheckbox.state() == NSCell.OnState) {
 			filePaths.addObject(NSPathUtilities.stringByAppendingPathComponent(fileName(),DEFAULT_XML_FILENAME));
 		}
 		log.debug("filePaths:"+filePaths);
 		String targetURL = "http://www.macpaf.org/submitbug.php";
 		PostMethod filePost = new PostMethod(targetURL);
 		try {
 			List parts = new ArrayList();
 			parts.add(new StringPart("message", bugReportText.string()));
 			if (filePaths.count() > 0) {
 				File targetFile = createZipFile(null, CocoaUtils.arrayAsList(filePaths));//new File("/Users/logan/Library/Logs/macpaf.log");
 				FilePart filePart = new FilePart("fileatt", targetFile);
 				log.debug("Uploading " + targetFile.getName() + " to " + targetURL);
 				parts.add(filePart);
 			}
			Object[] srcArray = parts.toArray();
			Part[] targetArray =	new Part[srcArray.length];
			System.arraycopy(srcArray, 0, targetArray, 0, srcArray.length);
			filePost.setRequestEntity(new MultipartRequestEntity(targetArray, filePost.getParams()));
 //			HttpClient client = new HttpClient();
 //			int status = client.executeMethod(filePost);
 			
 //			filePost.addParameter(targetFile.getName(), targetFile);
 			HttpClient client = new HttpClient();
 			client.setConnectionTimeout(5000);
 			int status = client.executeMethod(filePost);
 			if (status == HttpStatus.SC_OK) {
 				log.debug(
 						"Upload complete, response=" + filePost.getResponseBodyAsString()
 				);
 			} else {
 				log.debug(
 						"Upload failed, response=" + HttpStatus.getStatusText(status)
 				);
 			}
 		} catch (Exception ex) {
 			log.debug("Error trasmitting bug report: " + ex.getMessage());
 			ex.printStackTrace();
 			showUserErrorMessage("Could not submit feedback.", "If you are not connected to the internet, please try again after connecting.");
 		} finally {
 			filePost.releaseConnection();
 		}
         bugReportWindow.close();
   }
 
 private File createZipFile(String outFilename, Collection filenames) throws IOException {
 	// Create a buffer for reading the files
 	byte[] buf = new byte[1024];
 	if (StringUtils.isEmpty(outFilename)) {
 		outFilename = NSPathUtilities.stringByAppendingPathComponent(NSPathUtilities.temporaryDirectory(),"macpaf"+DateUtils.makeFileTimestampString()+".zip");
 	}
 	
 //	try {
 	    // Create the ZIP file
 	    ZipOutputStream out = new ZipOutputStream(new FileOutputStream(outFilename));
 	
 	    // Compress the files
 	    for (Iterator iter = filenames.iterator(); iter.hasNext();) {
 			String filename = (String) iter.next();
 			File file = new File(filename);
 			
 //		}
 //	    for (int i=0; i<filenames.length; i++) {
 			if (file.exists()) {
 	        FileInputStream in = new FileInputStream(file);
 	
 	        // Add ZIP entry to output stream.
 	        out.putNextEntry(new ZipEntry(NSPathUtilities.lastPathComponent(filename)));
 	
 	        // Transfer bytes from the file to the ZIP file
 	        int len;
 	        while ((len = in.read(buf)) > 0) {
 	            out.write(buf, 0, len);
 	        }
 	
 	        // Complete the entry
 	        out.closeEntry();
 	        in.close();
 			} else {
 				log.warn("createZipFile could not find file: "+filename);
 			}
 	    }
 	
 	    // Complete the ZIP file
 	    out.close();
 //	} catch (IOException e) {
 //	}
 	return new File(outFilename);
 }
 
   public void forgetIndividual(Object sender) { /* IBAction */
     	log.debug("MyDocument.forgetIndividual():"+sender);
     	historyController.forgetIndividual(getPrimaryIndividual());
     }
 
     public void recallIndividual(Object sender) { /* IBAction */
     	log.debug("MyDocument.recallIndividual():"+sender);
     	historyController.recallIndividual(((NSMenuItem) sender).tag());
     }
 
     public void recallLastFoundIndividual(Object sender) { /* IBAction */
     	log.debug("MyDocument.recallLastFoundIndividual():"+sender);
     	historyController.recallLastFoundIndividual();
     }
 
     public void recallLastSavedIndividual(Object sender) { /* IBAction */
     	log.debug("MyDocument.recallLastSavedIndividual():"+sender);
     	historyController.recallLastSavedIndividual();
     }
 
     public void rememberIndividual(Individual individual) { /* IBAction */
     	log.debug("MyDocument.rememberIndividual():"+individual);
     	historyController.rememberIndividual(getPrimaryIndividual());
 	}
     
 	public boolean validateMenuItem(_NSObsoleteMenuItemProtocol menuItem) {
 		if ("History".equals(menuItem.menu().title())) {
 			log.debug("MyDocument.validateMenuItem():"+menuItem);
 			log.debug("tag:"+menuItem.tag());
 			log.debug("action:"+menuItem.action().name());
 			log.debug("target:"+menuItem.target());
 			log.debug("representedObject:"+menuItem.representedObject());
 			log.debug("menu:"+menuItem.menu().title());
 //			log.debug("menu DELEGATE:"+menuItem.menu().delegate());
 			return historyController.validateMenuItem(menuItem);
 		} else if (menuItemShouldBeInactiveWithUnknownIndividual(menuItem)) {
 //			familyAsChildButton.setEnabled(false);
 			return false;
 		} else if (menuItemShouldBeInactiveWithUnknownFamily(menuItem)) {
 //			familyAsChildButton.setEnabled(false);
 			
 			return false;
 		} else {
 //			familyAsChildButton.setEnabled(true);
 			return super.validateMenuItem((NSMenuItem) menuItem);
 		}
 	}
 
 
 private boolean menuItemShouldBeInactiveWithUnknownIndividual(_NSObsoleteMenuItemProtocol menuItem) {
 	NSArray menuItemsToDeactivate = new NSArray(new String[] {
 			"Add Family As Spouse",
 			"Add Family As Child",
 			"Add Spouse",
 			"Add Father",
 			"Delete Individual"
 	});
 		return menuItemsToDeactivate.containsObject(menuItem.title()) && getPrimaryIndividual() instanceof Individual.UnknownIndividual;
 	}
 
 private boolean menuItemShouldBeInactiveWithUnknownFamily(_NSObsoleteMenuItemProtocol menuItem) {
 	NSArray menuItemsToDeactivate = new NSArray(new String[] {
 			"Delete Family",
 			"Edit Family"
 	});
 		return menuItemsToDeactivate.containsObject(menuItem.title()) && getCurrentFamily() instanceof Family.UnknownFamily;
 	}
 
 /* (non-Javadoc)
  * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
  */
 public void update(Observable o, Object arg) {
 	log.debug("MyDocument.update(): o="+o+" : arg="+arg);
 	// The MacPAFDocumentJDOM has changed
 	// @todo: do something here
 	updateChangeCount(NSDocument.ChangeDone);
 	if (familyListWindowController != null) {
 		familyListWindowController.refreshData();
 	}
 	if (individualListWindowController != null) {
 		individualListWindowController.refreshData();
 	}
 	refreshData();
 }
 
 private void refreshData() {
 	log.debug("MyDocument.refreshData() suppress:"+suppressUpdates);
 	if (!suppressUpdates) {
 		setPrimaryIndividual(getPrimaryIndividual());
 		childrenTable.reloadData();
 		spouseTable.reloadData();
 		tabFamilyListController.refreshData();
 		tabIndividualListController.refreshData();
 	}
 }
 
 public void startSuppressUpdates() {
 	log.debug("MyDocument.startSuppressUpdates()");
 	NDC.push("suppressUpdates");
 	suppressUpdates = true;
 	doc.startSuppressUpdates();
 }
 
 public void endSuppressUpdates() {
 	doc.endSuppressUpdates();
 	suppressUpdates = false;
 	NDC.pop();
 }
 }
