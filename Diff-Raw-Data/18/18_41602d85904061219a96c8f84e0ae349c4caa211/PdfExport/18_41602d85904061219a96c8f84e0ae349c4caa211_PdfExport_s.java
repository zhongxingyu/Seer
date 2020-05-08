 package de.unisiegen.informatik.bs.alvis.export;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.ImageLoader;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.ui.internal.Workbench;
 import org.eclipse.ui.texteditor.AbstractTextEditor;
 
 import com.itextpdf.text.Anchor;
 import com.itextpdf.text.Chapter;
 import com.itextpdf.text.Document;
 import com.itextpdf.text.DocumentException;
 import com.itextpdf.text.Element;
 import com.itextpdf.text.Font;
 import com.itextpdf.text.Paragraph;
 import com.itextpdf.text.html.simpleparser.HTMLWorker;
 import com.itextpdf.text.html.simpleparser.StyleSheet;
 import com.itextpdf.text.pdf.PdfWriter;
 
 import de.unisiegen.informatik.bs.alvis.Activator;
 import de.unisiegen.informatik.bs.alvis.editors.Messages;
 import de.unisiegen.informatik.bs.alvis.extensionpoints.IExportItem;
 
 //TODO replace static strings with dynamic, language-specific files
 /**
  * class which creates the export PDF file
  * 
  * @author Frank Weiler
  */
 public class PdfExport extends Document{
 
 	private static Font titleFont = new Font(Font.FontFamily.TIMES_ROMAN, 32,
 			Font.BOLD);
 	private static Font catFont = new Font(Font.FontFamily.TIMES_ROMAN, 18,
 			Font.BOLD);
 	private static Font subFont = new Font(Font.FontFamily.TIMES_ROMAN, 16,
 			Font.BOLD);
 	private static Font smallBold = new Font(Font.FontFamily.TIMES_ROMAN, 12,
 			Font.BOLD);
 
 	private Anchor anchor;
 	private Chapter chapter;
 	private Paragraph paragraph;
 
 	/**
 	 * the constructor creates export PDF file, opens file dialog to ask where
 	 * to save the file
 	 * 
 	 * @throws DocumentException
 	 */
 	public PdfExport() throws DocumentException,IOException {
 
 		FileDialog saveDialog = MyFileDialog.getExportDialog();
 
 		String path = MyFileDialog.open(saveDialog);
 		try {
 
 			PdfWriter.getInstance(this, new FileOutputStream(path));
 			open();
 
 			addMetaData();
 			addTitle();
 			addContent();
 			addContentOutsideParagraph();
 			
 			close();
 
 		} catch (NullPointerException npe) {
 		} catch (FileNotFoundException fnfe) {
 		}
 
 	}
 
 	/**
 	 * adds meta data to this document
 	 */
 	private void addMetaData() {
 		addTitle(Messages.getLabel("alvisExport"));
 		addSubject(Messages.getLabel("alvisExport"));
 		addKeywords("Alvis, Java, PDF");
 		addAuthor("AlvisPg2011");
 		addCreator("Alvis");
 	}
 
 	/**
 	 * adds title page to this document
 	 * 
 	 * @throws DocumentException
 	 */
 	private void addTitle() throws DocumentException {
 		Paragraph title = new Paragraph();
 		addEmptyLine(title, 1);
 
 		title.add(new Paragraph(Messages.getLabel("alvisExport"), titleFont));
 
 		addEmptyLine(title, 1);
 
 		title.add(new Paragraph(Messages.getLabel("generatedBy") + ": "
 				+ System.getProperty("user.name") + ", " + new Date(),
 				smallBold));
 
 		add(title);
 
 	}
 
 	private void addContent() throws DocumentException {
 
 		anchor = new Anchor("anchor", catFont);
 		anchor.setName("anchor");
 
 		ArrayList<IExportItem> exportItems = Activator.getDefault()
 				.getExportItems();
 
 		// adding all source code parts:
 		chapter = new Chapter(new Paragraph(anchor), 1);
 		for (IExportItem exportItem : exportItems) {
 			String sourceCode = exportItem.getSourceCode();
 			if (sourceCode != null) {
 				paragraph = toParagraph(sourceCode);
 				chapter.add(paragraph);
 			}
 		}
 		add(chapter);
 
 		// adding all images:
 		chapter = new Chapter(new Paragraph(anchor), 2);
 		for (IExportItem exportItem : exportItems) {
 			Image image = exportItem.getImage();
 			if (image != null) {
 				paragraph = toParagraph(image);
 				chapter.add(paragraph);
 			}
 		}
 		add(chapter);
 
 		// // GET THE ACTIVE EDITOR
 		// IExportItem activeEditor = (IExportItem) Activator.getDefault()
 		// .getWorkbench().getActiveWorkbenchWindow().getActivePage()
 		// .getActiveEditor();
 		// addSourceCode(activeEditor.getSourceCode());
 		// addImage(activeEditor.getImage());
 		//
 		// // PROBLEM NULLPOINTEREXCEOTION weil der die Instanz des IExportItem
 		// // neu erzeugt und nicht den aktiven Graph nimmt.
 		// IExtensionRegistry registry = Platform.getExtensionRegistry();
 		// IExtensionPoint extensionPoint = registry
 		// .getExtensionPoint("de.unisiegen.informatik.bs.alvis.export");
 		//
 		// for (IExtension extension : extensionPoint.getExtensions()) {
 		//
 		// IConfigurationElement[] elements = extension
 		// .getConfigurationElements();
 		//
 		// for (IConfigurationElement element : elements) {
 		//
 		// try {
 		// IExportItem myExportItem = (IExportItem) element
 		// .createExecutableExtension("class");
 		//
 		// // add all export items to the export file:
 		// String sourceCode = myExportItem.getSourceCode();
 		// addSourceCode(sourceCode);
 		// Image image = myExportItem.getImage();
 		// addImage(image);
 		// } catch (CoreException e) {
 		// e.printStackTrace();
 		// }
 		//
 		// }
 		//
 		// }
 
 	}
 
 	/**
 	 * adds organized, structured, highlighted source code to new paragraph and
 	 * returns it
 	 * 
 	 * @author Sebastian Schmitz & Frank Weiler
 	 * @param sourceCode
 	 *            the source code as string including html tags for highlighting
 	 *            etc
 	 * @return a paragraph including the source code
 	 * @throws DocumentException
 	 *             will be thrown when new paragraph could not have been added
 	 */
 	private Paragraph toParagraph(String sourceCode) throws DocumentException {
 		if (sourceCode == null)
 			return null;
 		String content = getContentFromActiveEditor();
 		// Dieser Block kann wahrscheinlich entfernt werden! Veraltet.
 //		String file = "";
 //		String path = "";
 //		// Absoluten Pfad durch Pfad relativ zum Projekt ersetzen! Done, siehe unten
 //		try {
 //			path = "/home/basti/Programmierung";
 //			path += "/runtime-build.product";
 //			path += "/peter/src/algorithm.algo";
 //
 //			file = readFile(path);
 //			String myfile = FileLocator.getBundleFile(
 //					Activator.getDefault().getBundle()).getAbsolutePath();			
 //			System.out.println("file: " + path);// wenn wieder einkommentiert: weg damit!
 //			System.out.println("\nmyfile: " + myfile);// wenn wieder einkommentiert: weg damit!
 //			System.out.println("\nsource code: " + sourceCode);// wenn wieder einkommentiert: weg damit!
 //
 //		} catch (IOException e) {
 //			e.printStackTrace();
 //		}
 		//file = putFormattedHTMLStringToHTMLCode(highlightString(file));
 		
 		Paragraph paragraph = new Paragraph(Messages.getLabel("sourceCode") + ":\n", subFont);
 		
 		if(content != null){
 			content = highlightString(content);
 			
 			ArrayList bodyText;
 			StyleSheet styles = new StyleSheet();
 			styles.loadTagStyle("ol", "leading", "16,0");
 			try {
 					bodyText =  (ArrayList) HTMLWorker.parseToList(new StringReader(content), styles);
 					
 				for(int k = 0; k < bodyText.size(); k++)
 					paragraph.add((Element) bodyText.get(k));
 	//			paragraph.addAll(bodyText);
 			} catch (IOException e) {
 				paragraph.add(Messages.getLabel("noSourceCodeAdded"));
 			}
 		}
 
 		return paragraph;
 
 	}
 
 	/**
 	 * adds image to new paragraph and returns it
 	 * 
 	 * @param image
 	 *            the image which will be parsed to a pdf-valid format
 	 * @return paragraph a paragraph including the image
 	 * @throws DocumentException
 	 *             will be thrown when new paragraph could not have been added
 	 */
 	private Paragraph toParagraph(Image image) throws DocumentException {
 		if (image == null)
 			return null;
 
 		String path = Messages.getLabel("tmpAlvisImage") + ".png";
 		com.itextpdf.text.Image pdfImage;
 
 		Paragraph paragraph = new Paragraph(Messages.getLabel("image"), subFont);
 
 		ImageLoader loader = new ImageLoader();
 		loader.data = new ImageData[] { image.getImageData() };
 		loader.save(path, SWT.IMAGE_PNG);
 
 		addEmptyLine(paragraph, 2);
 		try {
 			pdfImage = com.itextpdf.text.Image.getInstance(path);
 			float height, width;
 			width = Math.min(530, pdfImage.getScaledWidth());
 			height = pdfImage.getScaledHeight() / pdfImage.getScaledWidth()
 					* width;
 			pdfImage.scaleAbsolute(width, height);
 			paragraph.add(pdfImage);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.err.println(Messages.getLabel("errorAddingImage"));
 		}
 		addEmptyLine(paragraph, 2);
 
 		image.dispose();
 		return paragraph;
 
 	}
 
 	/**
 	 * adds empty lines (i.e. line breaks) to given paragraph
 	 * 
 	 * @param paragraph
 	 *            the paragraph to add empty lines to
 	 * @param amount
 	 *            the amount of empty lines
 	 */
 	private void addEmptyLine(Paragraph paragraph, int amount) {
 		for (int i = 0; i < amount; i++) {
 			paragraph.add(new Paragraph(" "));
 		}
 	}
 
 	/**
 	 * @author Sebastian Schmitz returns formatted content of the selected .algo
 	 *         file as string
 	 * @param pathToFile
 	 * @return
 	 * @throws IOException
 	 */
 	private String readFile(String pathToFile) throws IOException {
 		FileInputStream in = new FileInputStream(pathToFile);
 		String content = "";
 		int b = in.read();
 		while (b != -1) {
 			content = content.concat((Character.toString((char) b)));
 			b = in.read();
 		}
 		in.close();
 		return content;
 	}
 
 	/**
 	 * @author Sebastian Schmitz Embed the highlighted Text in a HTML file
 	 * @param string
 	 * @return String representing a completed HTML file
 	 */
 	private String putFormattedHTMLStringToHTMLCode(String string) {
 
 		String HTMLCode = string.replaceAll("\n", "<br>\n");
 
 		// String HTMLCode = "";
 		// HTMLCode += "" + "<html> " + '\n';
 		// HTMLCode += "" + '\t' + "<head>" + '\n';
 		// HTMLCode += "" + '\t' + '\t'
 		// + "<title> Source Code generated by Alvis Version X </title>"
 		// + '\n';// Versionsnummer?!
 		// HTMLCode += "" + '\t' + "</head>" + '\n';
 		// HTMLCode += "" + '\t' + "<body>" + '\n';
 		// HTMLCode += "" + '\t' + '\t' + "<pre>" + '\n';
 		// HTMLCode += "" + string;
 		// HTMLCode += "" + '\t' + '\t' + "</pre>" + '\n';
 		// HTMLCode += "" + '\t' + "</body>" + '\n';
 		// HTMLCode += "" + "</html> " + '\n';
 
 		return HTMLCode;
 
 	}
 
 	/**
 	 * accepts string and returns it with tokens highlighted using HTML tags
 	 * 
 	 * @param stringToHighight
 	 * @return highlighted String
 	 */
 	private String highlightString(String stringToHighight) {
 
 		stringToHighight = stringToHighight.replaceAll("<", "&#060");
 		stringToHighight = stringToHighight.replaceAll(">", "&#062");
 
 		ArrayList<String> tokenList = new ArrayList<String>();
 		tokenList.add("end");
 		tokenList.add("begin");
 		tokenList.add("while");
 		tokenList.add("for");
 		tokenList.add("if");
 		tokenList.add("in");
 		tokenList.add("infty");
 		tokenList.add("null");
 		// TODO Diese Liste unbedingt mit der aus der Grammatik erzeugten
 		// Tokenlist ersetzen!
 		String temp = "";
 		int indentationCounter = 0;
 		int indentationDepth = 40; 
 		String toReturn ="<p style=\"padding-left:" + indentationCounter * indentationDepth + "px; margin: 0;\">"; 
 		
 		// Read the String charwise
 		for (int i = 0; i < stringToHighight.length(); i++) {
 			if (stringToHighight.charAt(i) != '\t'
 					&& stringToHighight.charAt(i) != '\n'
 					&& stringToHighight.charAt(i) != '\r'
 					&& stringToHighight.charAt(i) != ' ') {
 				// read the next word
 				temp += stringToHighight.charAt(i);
 			} else {
 				// if the token begins a new indented block => increase the indentation counter...
 				if (temp.equals("begin"))
 					indentationCounter++;
 				else if (temp.equals("end")){ // ... if it ends one => decrease it
 					indentationCounter--;
 					temp = "<p style=\"padding-left:" + indentationCounter * indentationDepth + "px; margin: 0px; margin-bottom: -12pt;\"><font color=\"#FF00FF\">" + temp + "</font></p>";
 				}	
 
 				// if the word is complete, check if it is a token
 				if (tokenList.contains(temp)) {
 					// if so, surround it with a color tag
 					temp = "<font color=\"#FF00FF\">" + temp + "</font>";
 				}
 				toReturn += temp;
 				temp = "";
 				if (stringToHighight.charAt(i) == '\n' || stringToHighight.charAt(i) == '\r'){
 					toReturn += "</p>";
 					toReturn += "<p style=\"padding-left:" + indentationCounter * indentationDepth + "px; margin: 0;\">";
 				}
 				else if (stringToHighight.charAt(i) != '\t'){
 					// write the current whitespace
 					toReturn += stringToHighight.charAt(i);
 				}
 			}
 			
 		}
 		toReturn += "</p>";
 		return toReturn;
 	}
 	
 	/**
 	 * @author Sebastian Schmitz
 	 * This method adds the content from the active editor to the document.
 	 * The content added is not inside of a paragraph, let's hope we can find a fix for that.
 	 * @throws IOException
 	 * @throws DocumentException
 	 */
 	private void addContentOutsideParagraph() throws IOException, DocumentException {
 		ArrayList bodyText;
 		StyleSheet styles = new StyleSheet();
 		styles.loadTagStyle("ol", "leading", "16,0");
 		String pseudoCode = getContentFromActiveEditor();
 		
 		if(pseudoCode != null){
 			bodyText = (ArrayList) HTMLWorker.parseToList(new StringReader
 																(highlightString
 																		(pseudoCode
 																)), styles);	
 			for(int k = 0; k < bodyText.size(); k++)
 				add((Element) bodyText.get(k));
 		}
 		else{
 			// TODO: Possible usage site for our logger?
 			System.out.println("ERROR: The content retrieved from the editor was empty!");
 		}
 	}
 	
 	/**
 	 * @author Sebastian Schmitz
 	 * this function grabs the content from the active editor and returns it as String
 	 * if you use this, checking whether the returned String is null is advised!
 	 * @return content of active editor
 	 */
 	private String getContentFromActiveEditor(){
		AbstractTextEditor part = (AbstractTextEditor) Workbench.getInstance()
        								.getActiveWorkbenchWindow().getActivePage().getActiveEditor()
        								.getAdapter(AbstractTextEditor.class);
	
 		if (part != null) {
 		    IDocument document = part.getDocumentProvider().getDocument(
 		    							part.getEditorInput());
 	
 		    return document.get();
 		}
 		return null;
 	}
 
 }
