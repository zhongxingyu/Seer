 package kkckkc.syntaxpane;
 
 import kkckkc.syntaxpane.model.FoldManager;
 import kkckkc.syntaxpane.model.LineManager.Line;
 import kkckkc.syntaxpane.model.Scope;
 import kkckkc.syntaxpane.model.SourceDocument;
 import kkckkc.syntaxpane.style.ScopeSelectorManager;
 import kkckkc.syntaxpane.style.TextStyle;
 
 import javax.swing.event.DocumentEvent;
 import javax.swing.text.*;
 import java.awt.*;
 
 
 
 public class SourceView extends FoldablePlainView {
 
 	private ScopeSelectorManager scopeSelectorManager = new ScopeSelectorManager();
 	private SourceEditorKit editorKit;
 	private ScrollableSourcePane sourcePane;
 
 	public SourceView(Element elem, SourceEditorKit editorKit, ScrollableSourcePane sourcePane) {
 		super(elem);
 		this.editorKit = editorKit;
 		this.sourcePane = sourcePane;
 	}
 
 	
 	@Override
 	protected void updateDamage(DocumentEvent changes, Shape a, ViewFactory f) {
 		super.updateDamage(changes, a, f);
 
 		// TODO: Can we improve performance here
 		java.awt.Component host = getContainer();
 		host.repaint();
 	}
 
 
 	@Override
 	protected int drawUnselectedText(Graphics graphics, int x, int y, int p0,
 			int p1) throws BadLocationException {
 		Graphics2D graphics2d = (Graphics2D) graphics;
 		graphics2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
 				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
 		graphics2d.setRenderingHint(RenderingHints.KEY_TEXT_LCD_CONTRAST, 200);
 
 		SourceDocument doc = getDocument();
 
 		Line line = doc.getLineManager().getLineByPosition(p0);
 
 		Segment segment = getLineBuffer();
 
 		if (line == null) {
 			getDocument().getText(p0, p1 - p0, segment);
 			TextStyle defaultStyle = editorKit.getSourcePane().getStyleScheme().getTextStyle();
 			return draw(defaultStyle, graphics2d, x, y, segment, this);
 		}
 
 		int start = p0 - line.getStart();
 		int end = p1 - line.getStart();
 
 		int origX = x;
 		
 		x = draw(graphics2d, x, y, start, end, segment, this, line, line.getScope().getRoot());
 		
 		FoldManager.State s = doc.getFoldManager().getFoldState(line.getIdx());
 		if (s == FoldManager.State.FOLDED_FIRST_LINE) {
 			x = draw(editorKit.getSourcePane().getStyleScheme().getTextStyle(), graphics,
 					x + 10, y, new Segment(new char[] { '.', '.', '.' }, 0, 3),
 					this);
 		}
 
         if (sourcePane.isShowInvisibles())
 		    drawHiddenText(segment, p0, p1, graphics2d, origX, y);
 
 		return x;
 	}
 
 	private void drawHiddenText(Segment segment, int p0, int p1, Graphics2D g, int origX, int y) throws BadLocationException {
 		getDocument().getText(p0, p1 - p0, segment);
 
 		g.setColor(sourcePane.getStyleScheme().getInvisiblesColor());
 		
 		int width = g.getFontMetrics().stringWidth(" ");
         
         // Draw hidden characters
         int hx = origX;
 		for (int j = 0; j < segment.length(); j++) {
 			char c = segment.charAt(j);
 			if (c == '\n') {
                 g.drawString("\u00AC", hx, y);
     			hx += width;
             } else if (c == '\t') {
                 g.drawString("\u203A", hx, y);
                 hx = (int) nextTabStop(hx, j);
             } else {
     			hx += width;
             }
 		}
     }
 
 
 	private int draw(Graphics graphics, int x, int y, int s, int e,
 			Segment segment, TabExpander tabExpander, Line line, Scope scope)
 			throws BadLocationException {
		TextStyle style = scopeSelectorManager.getMatch(scope, editorKit
 				.getSourcePane().getStyleScheme().getStyles());
 		if (style == null) {
 			style = editorKit.getSourcePane().getStyleScheme().getTextStyle();
 		}
 
 		if (!scope.hasChildren()) {
 			int f = Math.max(scope.getStart(), s);
 			int t = Math.min(scope.getEnd(), e);
 			if (f < t) {
 				getText(line, segment, f, t);
 				x = draw(style, graphics, x, y, segment, tabExpander);
 			}
 		} else {
 			int o = scope.getStart();
 			for (Scope c : scope.getChildren()) {
 				if (c.getStart() > o) {
 					int f = Math.max(o, s);
 					int t = Math.min(c.getStart(), e);
 
 					if (f < t) {
 						getText(line, segment, f, t);
 						x = draw(style, graphics, x, y, segment, tabExpander);
 					}
 				}
 
 				x = draw(graphics, x, y, s, e, segment, tabExpander, line, c);
 				o = c.getEnd();
 			}
 
 			// Draw tail
 			int end = Math.min(scope.getEnd(), line.getLength());
 			int start = scope.getChildren().get(scope.getChildren().size() - 1)
 					.getEnd();
 
 			if (end > start) {
 				int f = Math.max(start, s);
 				int t = Math.min(end, e);
 				if (f < t) {
 					getText(line, segment, f, t);
 					x = draw(style, graphics, x, y, segment, tabExpander);
 				}
 			}
 		}
 
 		return x;
 	}
 
 	private void getText(Line line, Segment segment, int start, int end)
 			throws BadLocationException {
 		int startOffset = line.getStart() + Math.max(start, 0);
 		int endOffset = line.getStart() + Math.min(end, line.getLength());
 		int length = endOffset - startOffset;
 		getDocument().getText(startOffset, length, segment);
 	}
 	
 	private int draw(TextStyle style, Graphics graphics, int x, int y, Segment segment,
 			TabExpander tabExpander) {
 		graphics.setColor(style.getColor());
 		Font f = graphics.getFont();

 		Font newFont = f;
 		if (style.isBold() || style.isItalic() || style.isUnderline())
 			newFont = newFont.deriveFont((style.isBold() ? Font.BOLD : 0)
 					| (style.isItalic() ? Font.ITALIC : 0));
 		graphics.setFont(newFont);
 		
 		int i = Utilities.drawTabbedText(segment, x, y, graphics, tabExpander, 0);
 		graphics.setFont(f);
 		return i;
 	}
 
 }
