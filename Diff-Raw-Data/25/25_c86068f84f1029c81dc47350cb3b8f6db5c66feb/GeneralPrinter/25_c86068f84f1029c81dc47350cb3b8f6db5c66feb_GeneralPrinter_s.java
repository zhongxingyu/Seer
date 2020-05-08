 package de.unisiegen.tpml.ui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.Line2D;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.LinkedList;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import com.lowagie.text.Document;
 import com.lowagie.text.DocumentException;
 import com.lowagie.text.PageSize;
 import com.lowagie.text.Rectangle;
 import com.lowagie.text.pdf.PRAcroForm;
 import com.lowagie.text.pdf.PdfContentByte;
 import com.lowagie.text.pdf.PdfCopy;
 import com.lowagie.text.pdf.PdfImportedPage;
 import com.lowagie.text.pdf.PdfReader;
 import com.lowagie.text.pdf.PdfWriter;
 
 import de.unisiegen.tpml.graphics.AbstractProofComponent;
 import de.unisiegen.tpml.graphics.StyledLanguageEditor;
 import de.unisiegen.tpml.graphics.bigstep.BigStepComponent;
 import de.unisiegen.tpml.graphics.editor.TypeEditorPanel;
 import de.unisiegen.tpml.ui.netbeans.PdfDialog;
 
 public class GeneralPrinter {
     private Document document;
 
     private JPanel caller;
 
     private AbstractProofComponent comp;
 
     private Rectangle pageFormat;
     
     private java.awt.Graphics2D g1;
 
     private java.awt.Graphics2D g2;
     
     private java.awt.Graphics2D g3;
     
     private java.awt.Graphics2D g4;
 
     LinkedList<LinkedList<Component>> pages;
 
     private double scale = .6;
 
     private int right = 40;
 
     private int above = 40;
 
     private int naturalright = 20;
 
     private int naturalabove = 20;
     
     private String tmpdir;
     
     private String filename;
 
     public GeneralPrinter(JPanel caller) {
 	// document = new Document(PageSize.A4);
 	// document = new Document(PageSize.A4.rotate());
 	this.caller = caller;
     }
     
     public void print (TypeEditorPanel editorPanel){
 	System.out.println("TypeEditorPanel print!");
 	if(!openDiaglog()) return;
 	try {
 	    createSplitPage(0);
 	    editorPanel.getEditor().paint(g3);
 	    editorPanel.getEditor2().paint(g4);
 	    closePage();
 	    this.concatenatePages(1);
 	    this.deleteFiles(1);
 	    System.out.println("TypeEditorPanel print!");
 	} catch (FileNotFoundException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	} catch (DocumentException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
     }
     
     public void print (StyledLanguageEditor editor){
 	if(!openDiaglog()) return;
 	boolean l33t = false;
 
 	try {
 	    JPanel printarea = createPage(0);
 	    Graphics remember = editor.getGraphics();
 	l33t = editor.getSize().height > g2.getClipBounds().height;
 	    editor.paint(g2);
 	    editor.paint(remember);
 	    closePage();
 	    
 //	  concatenate the temporary pages
 	    this.concatenatePages(1);
 
 	    // remove the temporary pages now
 	    this.deleteFiles(1);
 	    if (l33t){
 		 JOptionPane.showMessageDialog(caller, "Your Sourcecode is too long to be printed properly...\n" +
 		 		"I give up, you are too elite.\n" +
 		 		"Actually you are most likely Kurt or Ben...\n" +
 		 		"It's lonely at the top. But you may get yourself a coffe now\n" +
 		 		"and enjoy the comfort to look down upon everyone at the bottom.");
 	    } else {
 		 JOptionPane.showMessageDialog(caller, "Document has been printed!");
 	    }
 	   
 	    
 	} catch (FileNotFoundException e) {
 	    JOptionPane.showMessageDialog(caller, "Can not access the file, might be open");
 	    
 	    e.printStackTrace();
 	} catch (DocumentException e) {
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 	
     }
 
     public void print(AbstractProofComponent icomp) {
 
 	if(!openDiaglog()) return;
 		
 	this.comp = icomp;
 	comp.setBackground(new Color(255,255,255));
 	comp.setOpaque(false);
 
 	try {
 	    // creating a temporary file to wirte to:
 	    //tmpdir = System.getProperty("java.io.tmpdir");
 	    //System.out.println(tmpdir);
 
 	    // Number Of Pages
 	    int nop = -1;
 	    int i = 0;
 
 	    do {
 		JPanel printarea = createPage(i);
 		
 		if (nop == -1) {
 		    comp.setAvailableWidth(g2.getClipBounds().width);
 		    comp.setAvailableHeight(g2.getClipBounds().height);
 		    nop = (comp.getHeight() / printarea.getHeight() + 1);
 		}
 		printarea.add(comp);
 		
 		// on the first page we will have to eliminate the natural top spacing
 		if (i == 0) {
		    comp.setBounds(-naturalright, -naturalabove - i * printarea.getHeight(), comp.getWidth(), comp.getHeight());
		} else {
		    comp.setBounds(-naturalright, -i * printarea.getHeight(), comp.getWidth(), comp.getHeight());
 		}
		printarea.paint(g2);
 		
 		closePage();
 		i++;
 	    } while (i < nop);
 
 	    // concatenate the temporary pages
 	    this.concatenatePages(nop);
 
 	    // remove the temporary pages now
 	    this.deleteFiles(nop);
 
 	    JOptionPane.showMessageDialog(caller, "Document has been printed!");
 
 	} catch (Exception de) {
 	    de.printStackTrace();
 	}
     }
 
     private void closePage() {
 	g2.dispose();
 	g1.dispose();
 	document.close();
     }
 
     private JPanel createPage(int i) throws DocumentException, FileNotFoundException {
 	document = new Document(pageFormat);
 	PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(tmpdir+"/tmp" + i + ".pdf"));
 	document.open();
 	PdfContentByte cb = writer.getDirectContent();
 	// do not use the scale factor in the next one!
 	g1 = cb.createGraphicsShapes(pageFormat.getWidth(), pageFormat.getHeight());
 	g1.setBackground(new Color(255,255,255));
 	g1.scale(scale, scale);
 	// g2.setClip(right, above,
 	// g2.getClipBounds().width,g2.getClipBounds().height);
 	g2 = (Graphics2D) g1.create(right, above, g1.getClipBounds().width - 2 * right, g1.getClipBounds().height - 2 * above);
 	g2.setBackground(new Color(255,255,255));
 	JPanel j1 = new JPanel();
 	j1.setSize(g2.getClipBounds().width, g2.getClipBounds().height);
 	j1.setBackground(new Color(255, 255, 255));
 	j1.setOpaque(true);
 	return j1;
     }
     
     private void createSplitPage(int i) throws DocumentException, FileNotFoundException {
 	document = new Document(pageFormat);
 	PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(tmpdir+"/tmp" + i + ".pdf"));
 	document.open();
 	PdfContentByte cb = writer.getDirectContent();
 	// do not use the scale factor in the next one!
 	g1 = cb.createGraphicsShapes(pageFormat.getWidth(), pageFormat.getHeight());
 	g1.setBackground(new Color(255,255,255));
 	g1.scale(scale, scale);
 	// g2.setClip(right, above,
 	// g2.getClipBounds().width,g2.getClipBounds().height);
 	g2 = (Graphics2D) g1.create(right, above, g1.getClipBounds().width - 2 * right, g1.getClipBounds().height - 2 * above);
 	g3 = (Graphics2D) g2.create(0, 0, g2.getClipBounds().width, g2.getClipBounds().height/2-5);
 	g4 = (Graphics2D) g2.create(0, g2.getClipBounds().height/2+5, g2.getClipBounds().width, g2.getClipBounds().height/2-5);
 	g2.drawRect(0, g2.getClipBounds().height/2, g2.getClipBounds().width, 1);
     }
 
     private boolean openDiaglog() {
 	PdfDialog dialog = new PdfDialog((JFrame)caller.getTopLevelAncestor(), true);
 
 	dialog.setLocationRelativeTo(caller);
 	dialog.setVisible(true);
 	//System.out.println(dialog.filechooser.getSelectedFile());
 	//System.out.println(dialog.landscape);
 	
 	if (dialog.cancelled) return false;
 	
 	this.filename = dialog.filechooser.getSelectedFile().getAbsolutePath();
 	if (! this.filename.substring(filename.length()-4).equalsIgnoreCase(".pdf")){
 	    this.filename = this.filename+".pdf";
 	}
 	tmpdir = dialog.filechooser.getSelectedFile().getParent();
 	
 	if (dialog.landscape){
 	    pageFormat = PageSize.A4.rotate();
 	}else {
 	    pageFormat = PageSize.A4;
 	}
 	return true;
     }
 
     private void concatenatePages(int nop) {
 	if (nop < 1) return; // nothing to concatenate...
 	// concatenate temporary files here
 	try {
 	    int pageOffset = 0;
 	    ArrayList master = new ArrayList();
 	    int f = 0;
 	    String outFile = filename;
 	    Document document = null;
 	    PdfCopy writer = null;
 	    while (f < nop) {
 		// we create a reader for a certain document
 		PdfReader reader = new PdfReader(tmpdir+"/tmp" + f + ".pdf");
 		reader.consolidateNamedDestinations();
 		// we retrieve the total number of pages
 		int n = reader.getNumberOfPages();
 
 		pageOffset += n;
 
 		if (f == 0) {
 		    // step 1: creation of a document-object
 		    document = new Document(reader.getPageSizeWithRotation(1));
 		    // step 2: we create a writer that listens to the
 		    // document
 		    writer = new PdfCopy(document, new FileOutputStream(outFile));
 		    // step 3: we open the document
 		    document.open();
 		}
 		// step 4: we add content
 		PdfImportedPage page;
 		for (int i = 0; i < n;) {
 		    ++i;
 		    page = writer.getImportedPage(reader, i);
 		    writer.addPage(page);
 		}
 		PRAcroForm form = reader.getAcroForm();
 		if (form != null)
 		    writer.copyAcroForm(reader);
 		f++;
 	    }
 	    if (!master.isEmpty())
 		writer.setOutlines(master);
 	    // step 5: we close the document
 	    document.close();
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
     }
 
     private void deleteFiles(int nop) {
 	if (nop < 1) return; //nothing to delete
 	for (int i = 0; i < nop; i++) {
 	    File f = new File(tmpdir+"/tmp" + i + ".pdf");
 	    f.delete();
 	}
     }
 
 }
