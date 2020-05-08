 package org.blackacrelabs.usreports.tokenizer;
 
 import java.awt.Dimension;
 import java.awt.Rectangle;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.pdfbox.cos.COSBase;
 import org.apache.pdfbox.pdfviewer.PageDrawer;
 import org.apache.pdfbox.pdmodel.PDDocument;
 import org.apache.pdfbox.pdmodel.PDPage;
 import org.apache.pdfbox.util.PDFOperator;
 import org.apache.pdfbox.util.TextPosition;
 import org.apache.pdfbox.util.operator.OperatorProcessor;
 
 public class PDFTokenizer extends PageDrawer {
 
 	public static float PAGE_ONE_PAGE_NUMBER_Y = 644.0f;
 	// public static float FOOTNOTE_PARAGRAPH_INDENT = 169.116f; // Asterisk
 	// footnote markers BTL
 	public static float FOOTNOTE_PARAGRAPH_INDENT = 165.93759f;
 
 	public LinkedList<Token> tokens = new LinkedList<Token>();
 	private float lastX = Float.MAX_VALUE;
 	private float footnoteLineY = Integer.MAX_VALUE;
 	private Boolean emittedFootnoteToken = false;
 	private float pageHeight = 0;
 
 	public PDFTokenizer() throws IOException {
 		super();
 		// FIXME hack to get rid of annoying warnings
 		OperatorProcessor doNothing = new OperatorProcessor() {
 			@Override
 			public void process(PDFOperator operator, List<COSBase> arguments)
 					throws IOException {
 				// do nothing
 
 			}
 		};
 		super.registerOperatorProcessor("BDC", doNothing);
 		super.registerOperatorProcessor("EMC", doNothing);
 	}
 
 	@SuppressWarnings("unchecked")
 	public void tokenize(PDDocument document) throws IOException {
 		processPages(document.getDocumentCatalog().getAllPages());
 	}
 
 	/**
 	 * Detect when strokes are painted to determine where the footnote separator
 	 * line falls on the page. Convert the reported bounding box coordinates
 	 * from PageDrawer to those used by the text operators.
 	 */
 	@Override
 	public void strokePath() throws IOException {
 		Rectangle bounds = getLinePath().getBounds();
 		double x = bounds.getX();
 		if (x == 160.0) {
 			footnoteLineY = (float) pageHeight + bounds.y;
 		}
 		super.strokePath();
 	}
 
 	protected void processPages(List<PDPage> pages) throws IOException {
 		for (Iterator<PDPage> pageIterator = pages.iterator(); pageIterator
 				.hasNext();) {
 			PDPage page = pageIterator.next();
 			startPage(page);
 			// FIXME terrible hack! painting the PDF to nowhere
 			drawPage(new StubGraphics2D(), page, new Dimension());
 			endPage(page);
 		}
 	}
 
 	@Override
 	protected void processTextPosition(TextPosition text) {
 		String character = text.getCharacter();
 		float size = text.getFontSizeInPt();
 		String font = text.getFont().getBaseFont();
 		float x = text.getX();
 		float y = text.getY();
 
 		// exclude headers
 		if (y < 170.0)
 			return;
 		// exclude page 1 page numbers at bottom of pages
 		if (y > PAGE_ONE_PAGE_NUMBER_Y)
 			return;
 
 		if (lastX > x) {
 			if (tokens.size() > 0
 					&& (tokens.get(tokens.size() - 1).type != TokenType.LINE)
 					&& (tokens.get(tokens.size() - 1).type != TokenType.PAGE)) {
 				addToken(TokenType.LINE);
 			}
 		}
 		if (emittedFootnoteToken == false && y > footnoteLineY) {
 			emittedFootnoteToken = true;
 			addToken(TokenType.FOOT);
 		}
 		if (lastX > x) {
 			if (x > 171.0) {
 				addToken(TokenType.INDENT);
 			} else if (emittedFootnoteToken && x >= FOOTNOTE_PARAGRAPH_INDENT) {
 				addToken(TokenType.INDENT);
 			}
 		}
 		lastX = x;
 
 		if (character.equals(" ")) {
 			addToken(TokenType.S);
 		} else {
 			addToken(FontFamily.tokenTypeForFont(font), character);
 		}
 	}
 
 	protected void endPage(PDPage page) throws IOException {
 		addToken(TokenType.LINE);
 		addToken(TokenType.PAGE);
 	}
 
 	private void addToken(TokenType type) {
 		tokens.add(new Token(type));
 	}
 
 	private void addToken(TokenType type, String text) {
 		Token lastToken = tokens.getLast();
 		if (lastToken.type == type && lastToken.text != null)
 			lastToken.text = lastToken.text + text;
 		else
 			tokens.add(new Token(type, text));
 	}
 
 	protected void startPage(PDPage page) throws IOException {
 		emittedFootnoteToken = false;
 		footnoteLineY = Integer.MAX_VALUE;
 		pageHeight = page.getMediaBox().getHeight();
 	}
 }
