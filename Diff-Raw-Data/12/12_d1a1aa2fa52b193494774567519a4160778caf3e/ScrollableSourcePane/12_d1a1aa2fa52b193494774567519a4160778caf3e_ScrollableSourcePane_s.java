 package kkckkc.syntaxpane;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.Reader;
 
 import javax.swing.JEditorPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import kkckkc.syntaxpane.model.SourceDocument;
 import kkckkc.syntaxpane.parse.grammar.Language;
 import kkckkc.syntaxpane.parse.grammar.LanguageManager;
 import kkckkc.syntaxpane.style.StyleScheme;
 import kkckkc.syntaxpane.util.Wiring;
 
 
 
 public class ScrollableSourcePane extends JPanel {
 	private JEditorPane editorPane;
 	private JScrollPane scrollPane;
 	
 	private StyleScheme styleScheme;
 	private SourceEditorKit editorKit;
 	private LineNumberMargin lineNumberPane;
 	private FoldMargin foldMargin;
 	private Color origBackground;
 	
 	public ScrollableSourcePane(LanguageManager languageManager) {
 		super(new BorderLayout());
 		
 		editorKit = new SourceEditorKit(this, languageManager);
 
 		editorPane = new SourceJEditorPane();
 		editorPane.setOpaque(false);
 		editorPane.setEditorKit(editorKit);
 		editorPane.setUI(new javax.swing.plaf.basic.BasicEditorPaneUI());
 		
 		Wiring.wire(this, editorPane, "font", "background", "foreground");
 		
 		foldMargin = new FoldMargin(editorPane);
 		lineNumberPane = new LineNumberMargin(editorPane);
 		
 		scrollPane = new JScrollPane(editorPane);
 		
 		JPanel rowHeaderPane = new JPanel();
 		rowHeaderPane.setLayout(new BorderLayout());
 		rowHeaderPane.add(lineNumberPane, BorderLayout.WEST);
 		rowHeaderPane.add(foldMargin, BorderLayout.EAST);
 		
 		scrollPane.setRowHeaderView(rowHeaderPane);
 		scrollPane.setViewportBorder(null);
 		
 		setFont(new Font("Liberation Mono", Font.PLAIN, 12));
 //		setFont(new Font("Courier New", Font.PLAIN, 12));
 //		setFont(new Font("Monaco", Font.PLAIN, 12));
 		origBackground = super.getBackground();
 		setBackground(Color.WHITE);
 		setForeground(Color.BLACK);
 
 //		editorPane.setBorder(new EmptyBorder(0, 0, 0, 0));
 		
 		add(scrollPane, BorderLayout.CENTER);
 	}
 
 	public JScrollPane getScrollPane() {
 		return scrollPane;
 	}
 	
 	public Color getBackground() {
 		return origBackground;
 	}
 	
 	public void setStyleScheme(StyleScheme styleScheme) {
 		this.styleScheme = styleScheme;
 		
 		setBackground(this.styleScheme.getTextStyle().getBackground());
 		setForeground(this.styleScheme.getTextStyle().getColor());
 		
 		editorPane.setCaretColor(this.styleScheme.getCaretColor());
 		
 		editorPane.setSelectionColor(this.styleScheme.getSelectionStyle().getBackground());
 		editorPane.setSelectedTextColor(this.styleScheme.getSelectionStyle().getColor());
 		
 		lineNumberPane.setBackground(this.styleScheme.getLineNumberStyle().getBackground());
 		lineNumberPane.setForeground(this.styleScheme.getLineNumberStyle().getColor());
 		
 		foldMargin.setBackground(this.styleScheme.getLineNumberStyle().getBackground());
 		foldMargin.setForeground(this.styleScheme.getLineNumberStyle().getColor());
 		foldMargin.setBorderColor(this.styleScheme.getLineNumberStyle().getBorder());
 		
 		CurrentLinePainter.apply(new CurrentLinePainter(this.styleScheme.getLineSelectionColor()), editorPane);
 	}
 
 	public JEditorPane getEditorPane() {
 		return editorPane;
 	}
 
 	public void setLanguage(Language lang) {
 		((SourceDocument) editorPane.getDocument()).setLanguage(lang);
 	}
 
 	public StyleScheme getStyleScheme() {
 		return styleScheme;
 	}
 	
 	public SourceDocument getDocument() {
 		return (SourceDocument) editorPane.getDocument();
 	}
 	
 	public void read(Reader reader) throws IOException {
 		editorPane.read(reader, null);
 	}
 	
 	public void read(File f) throws IOException {
 		editorPane.read(new FileReader(f), f);
 	}
 	
 	private final class SourceJEditorPane extends JEditorPane {
 		public static final int FOLD_MARGIN = 120;
 		
 	    @Override
 	    public void paint(Graphics graphics) {
 	    	Graphics2D graphics2d = (Graphics2D) graphics;
 
 	    	int wm = graphics.getFontMetrics().charWidth('m');
 	    	
 	    	Rectangle clip = graphics.getClipBounds();
 	    	
 	    	graphics2d.setColor(getStyleScheme().getRightMargin().getBackground());
 	    	
 	    	graphics2d.fillRect(FOLD_MARGIN * wm, clip.y, clip.width, clip.height);
 	    	
 	    	graphics2d.setColor(getStyleScheme().getRightMargin().getColor());
 	    	graphics2d.drawLine(FOLD_MARGIN * wm, clip.y, FOLD_MARGIN * wm, clip.y + clip.height);
 
 	    	super.paint(graphics);
 	    }
     }
 }
