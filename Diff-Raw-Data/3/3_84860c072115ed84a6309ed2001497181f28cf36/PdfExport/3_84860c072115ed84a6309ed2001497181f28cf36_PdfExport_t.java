 package de.unisiegen.informatik.bs.alvis.export;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.ImageLoader;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.PlatformUI;
 
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
 
 import org.eclipse.xtext.ui.editor.XtextEditor;
 
 import de.unisiegen.informatik.bs.alvis.Activator;
 import de.unisiegen.informatik.bs.alvis.editors.AlgorithmEditor;
 import de.unisiegen.informatik.bs.alvis.editors.Messages;
 import de.unisiegen.informatik.bs.alvis.extensionpoints.IExportItem;
 
 /**
  * class which creates the export PDF file
  * 
  * @author Frank Weiler & Sebastian Schmitz
  */
 public class PdfExport extends Document {
 
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
 	public PdfExport() throws DocumentException, IOException {
 
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
 
 		String content = getContentFromAlgoEditor();
 
 		Paragraph paragraph = new Paragraph(Messages.getLabel("sourceCode")
 				+ ":\n", subFont);
 
 		if (content != null) {
 			content = indentCode(content);
 
 			List<Element> bodyText;
 			StyleSheet styles = new StyleSheet();
 			styles.loadTagStyle("ol", "leading", "16,0");
 			try {
 				bodyText = HTMLWorker.parseToList(new StringReader(content),
 						styles);
 
 				for (Element elem : bodyText) {
 					paragraph.add(elem);
 				}
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
 	 * this method takes the String from the Editor (which has been highlighted
 	 * already by getContentFromAlgoEditor) and translates the indendation of
 	 * the pseudo code to HTML
 	 * 
 	 * The String is read linewise and indented via the
 	 * <p padding-left:" XYpx;">
 	 * HTML-tag
 	 * 
 	 * @param stringToIndent
 	 * @return indented String
 	 */
 	private String indentCode(String stringToIndent) {
 		String line = ""; // storage for the lines
 		
 		// Support for different styles of indendation (four spaces, eight spaces and tabs):
 		stringToIndent = stringToIndent.replaceAll("\t", "    ");
 		stringToIndent = stringToIndent.replaceAll("    ", "\t");
 		
		// in case someone meddled with weird windows text editors
		stringToIndent = stringToIndent.replaceAll("\r\n", "\n");
		
 		int indentationCounter = 0; // variable holding knowledge about how deep
 									// the current line has to be indented
 		int indentationDepth = 20;
 		boolean onlyWhiteSpacesYet = true; // tabs after the first char that's
 											// not a white space are ignored
 		String toReturn = "";
 		for(Character curr : stringToIndent.toCharArray()){
 			if (curr == '\t' && onlyWhiteSpacesYet == true) // if we are at the
 															// beginning of a
 															// line, we want to
 															// count how deep
 				indentationCounter++; // the current line has to be indented
 			else if (!(curr == '\n' || curr == '\r')) { // if we read a common
 														// character, just add
 														// it to the line
 				onlyWhiteSpacesYet = false;
 				line += curr;
 			}
 			else if (line.isEmpty() && (curr == '\n' || curr == '\r')){ // Support for empty lines
 				toReturn += "<br/>";
 			}
 			else {
 				// the line is complete. Surround it with "tabs" and append it
 				// to the returned String
 				toReturn += "<p style=\"padding-left:" + indentationCounter
 						* indentationDepth + "px; margin: 0;\">";
 				toReturn += line;
 				toReturn += "</p>";
 				indentationCounter = 0; // reset
 				onlyWhiteSpacesYet = true; // reset, because the next line is
 											// independent from this one
 				line = ""; // reset
 			}
 		}
 		return toReturn;
 	}
 
 	/**
 	 * @author Sebastian Schmitz This method adds the content from the active
 	 *         editor to the document. The content added is not inside of a
 	 *         paragraph, let's hope we can find a fix for that.
 	 * @throws IOException
 	 * @throws DocumentException
 	 */
 	private void addContentOutsideParagraph() throws IOException,
 			DocumentException {
 		List<Element> bodyText;
 		StyleSheet styles = new StyleSheet();
 		styles.loadTagStyle("ol", "leading", "16,0");
 		String pseudoCode = getContentFromAlgoEditor();
 
 		if (pseudoCode != null) {
 			bodyText = HTMLWorker.parseToList(new StringReader(
 					indentCode(pseudoCode)), styles);
 			for (Element elem : bodyText) {
 				add(elem);
 			}
 		} else {
 			System.out
 					.println("ERROR: The content retrieved from the editor was empty!");
 		}
 	}
 
 	/**
 	 * @author Sebastian Schmitz this function grabs the content from the editor
 	 *         containing a file with ".algo"-ending and returns it as String if
 	 *         you use this, checking whether the returned String is null is
 	 *         advised!
 	 * @return content of the editor
 	 */
 
 	private String getContentFromAlgoEditor() {
 		String codeWithHTMLStyleTags = "";
 		AlgorithmEditor part = null; // TODO: weg damit! AlgorithmEditor is obsolete. XTextEditor is the futuer!
 		XtextEditor edit = null;
 		
 		// Get open pages
 		IWorkbenchPage pages[] = PlatformUI.getWorkbench()
 				.getActiveWorkbenchWindow().getPages();
 		// Cycle through these pages
 		for (IWorkbenchPage page : pages) {
 			// Cycle through every page's editors
 			for(IEditorReference ref : page.getEditorReferences()){
 				String title = ref.getTitle();
 				IEditorPart refpart = ref.getEditor(true);
 				// Get algo-editor
 				if (title.contains(".algo")) {
 					if (refpart.getAdapter(refpart.getClass()) instanceof XtextEditor)
 						edit = (XtextEditor) ref.getEditor(true).getAdapter(XtextEditor.class);
 					if(refpart.getAdapter(refpart.getClass()) instanceof AlgorithmEditor)
 						part = (AlgorithmEditor) ref.getEditor(true).getAdapter(
 								AlgorithmEditor.class);
 				}
 
 			}
 
 		}
 		/*
 		 *  The AlgorithmEditor is only still included for compatibility reasons
 		 *  If you don't have the language-Plugins (de.~.alvis.language.*) you're 
 		 *  still using the obsolete AlgorithmEditor. This will be changed!
 		 *  The AlgorithmEditor will not be used in the release build and all 
 		 *  references to it in this class will be erased.
 		 */
 		StyledText style = null;
 		if(part != null)
 			style = part.getTextWidget();
 		if(edit != null)
 			style = edit.getInternalSourceViewer().getTextWidget();
 		
 		if ((edit != null || part != null) && style != null) {
 			RGB rgb;
 			RGB black = new RGB(0,0,0);
 		    String text = style.getText(); 				// the complete text grabbed from the editor
 		    StyleRange[] range = style.getStyleRanges();// ranges declaring the styles of each part of the text
 
 		    for(StyleRange ran : range){				// cycle through these ranges and style them using HTML
 		    	String word = "";
 		    	for(int i = ran.start; i < ran.start+ran.length; i++){
 		    		if (text.charAt(i) == '<') // Replace "<" and ">" otherwise they will be interpreted and thus erased by the HTMLWorker
 		    			word += "&lt;";
 		    		else if (text.charAt(i) == '>')
 		    			word += "&gt;";
 		    		else
 		    			word += text.charAt(i);  // if the character is neither "<" nor ">" append it to the current word
 		    	}
 		    	Color col = ran.foreground;
 		    	if(col == null){ // color must not be null
 		    		rgb = black;
 		    	}
 		    	else
 		    		rgb = col.getRGB();
 		    	if(!rgb.equals(black)) // black is assumed as standard color, other color will be included here.
 		    		word = 	"<font color=\"#"+
 								FormatStringCorrectly(Integer.toHexString(rgb.red))+
 								FormatStringCorrectly(Integer.toHexString(rgb.green))+
 								FormatStringCorrectly(Integer.toHexString(rgb.blue))+
 							"\">" + word + "</font>";
 		    	// add font style to the current word
 //		    	if (ran.fontStyle == 0)
 //		    		word = "<u>" + word + "</u>";
 		    	if(ran.fontStyle == 1)
 		    		word = "<b>" + word + "</b>";
 		    	if(ran.fontStyle == 2){
 		    		word = "<i>" + word + "</i>";
 		    	}
 		    	codeWithHTMLStyleTags += word;
 		    	
 		    	
 		    }		    
 		    return codeWithHTMLStyleTags + "\n";
 
 		}
 		else
 			System.out.println("ERROR: No Editor could be found");
 		return null;
 	}
 	/**
 	 * Adds a '0' character in front a string.
 	 * Important for color values: "0" instead of "00" or "f" instead of "0f"
 	 *  would be misinterpreted by the HTMLParser
 	 * @param toFormat
 	 * @return formatted String
 	 */
 	private String FormatStringCorrectly(String toFormat){
 		if (toFormat.length() == 1)
 			return "0"+toFormat;
 		return toFormat;
 	}
 
 }
