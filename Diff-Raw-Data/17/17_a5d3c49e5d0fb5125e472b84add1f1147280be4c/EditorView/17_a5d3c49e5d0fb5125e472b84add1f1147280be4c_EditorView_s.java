 package gui;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.Bullet;
 import org.eclipse.swt.custom.ST;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.custom.StyleRange;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.graphics.GlyphMetrics;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.ScrollBar;
 
 import misc.Editor;
 import misc.Keyword;
 
 public class EditorView extends Composite {
 	private Editor editor;
 	private Display parentdisplay;
 	private StyledText textfield;
 	private StyledText linenumbers;
 	
 	private static final int MAX_LINES = 500;
 	
 	public EditorView(Composite parent, int def, Editor editor) {
 		super(parent, def);
 		this.editor = editor; 
 		this.editor.setView(this);
 		
 		//Setting the Layout
 		GridLayout gLayout = new GridLayout();
 		gLayout.numColumns = 30;
 		gLayout.makeColumnsEqualWidth = true;
 		this.setLayout(gLayout);
 		GridData gData = new GridData(GridData.FILL_BOTH);
 		this.setLayoutData(gData);
 		
 		//Initialize editor components
 		this.parentdisplay = parent.getDisplay();
 		
 		//Text field for line numbers
 		final ScrolledComposite sc1 = new ScrolledComposite (this, SWT.V_SCROLL 
 				| SWT.H_SCROLL | SWT.RIGHT_TO_LEFT);
 		this.linenumbers = new StyledText(sc1, SWT.LEFT | SWT.MULTI | SWT.WRAP);	
 		sc1.getVerticalBar().setEnabled(false);
 		this.linenumbers.setSize(30, 7500);
 		
 		String s = "";
 		for (int i = 0; i < MAX_LINES - 1; i++) {
 			s += "\r";
 		}
 		this.linenumbers.setText(s);
 		
 		for (int i = 0; i < MAX_LINES; i++) {		
 			StyleRange style = new StyleRange();
 			style.length = 0;
 			style.metrics = new GlyphMetrics(0, 0, 0);
 			Bullet b = new Bullet(ST.BULLET_TEXT, style);
 			b.text = (i + 1) + " ";
 			this.linenumbers.setLineBullet(i, 1, b);
 		}
 		
 		this.linenumbers.setEditable(false);
 		this.linenumbers.setBackground(new Color(this.getDisplay(), 211, 211, 211));
 		this.linenumbers.setCursor(new Cursor(parent.getDisplay(), SWT.CURSOR_HAND));
 		sc1.setContent(this.linenumbers);
 		
 		//Text field for source code
 		final ScrolledComposite sc2 = new ScrolledComposite (this, SWT.V_SCROLL | SWT.H_SCROLL);
 		this.textfield = new StyledText(sc2, SWT.NONE);
 		this.textfield.setLeftMargin(5);
 		this.textfield.setSize(1200, 7500);
 		this.textfield.setFocus();
 		sc2.setContent(this.textfield);
 		
 		//Position the text fields
 		gData = new GridData(GridData.FILL_BOTH);
 		gData.horizontalSpan= 2;
 		sc1.setLayoutData(gData);
 		gData = new GridData(GridData.FILL_BOTH);
 		gData.horizontalSpan= 28;
 		sc2.setLayoutData(gData);
 		
 		//Simultaneous scrolling of the text fields
 		final ScrollBar vBar1 = sc1.getVerticalBar();
 		final ScrollBar vBar2 = sc2.getVerticalBar();
 		SelectionListener listener = new SelectionAdapter () {
 			public void widgetSelected (SelectionEvent e) {
 				int x =  sc1.getOrigin().x;
 				int y =  vBar2.getSelection() * (vBar1.getMaximum() - vBar1.getThumb()) 
 						/ Math.max(1, vBar2.getMaximum() - vBar2.getThumb());
 				sc1.setOrigin (x, y);
 			}
 		};
 		vBar2.addSelectionListener(listener); 
 	}
 	
 	public void updateView() {
 		if(!this.textfield.getText().equals(this.editor.getSource())) {
 			this.textfield.setText(editor.getSource());
 		}
 		
 		textfield.setStyleRange(null);
 		int linebreakAddend = 0;
 		for(Keyword word : this.editor.getColorArray()) {
 			StyleRange stylerange = new StyleRange();
 			//System.out.println(word.getStart() + " ## " + word.getLength());// + " ## " + this.textfield.getLineAtOffset(word.getStart()) + " ## ");//  + this.textfield.getText(word.getStart(), word.getStart()+word.getLength()-1));
 			stylerange.start = word.getStart();// + this.textfield.getLineAtOffset(word.getStart());
 			stylerange.length = word.getLength();
 			stylerange.fontStyle = SWT.BOLD;
 			stylerange.foreground = new Color(this.textfield.getDisplay(), word.getColor());
 			textfield.setStyleRange(stylerange);
 		}
 	}
 	
 	public String getText() {
 		return this.textfield.getText();
 	}
 	public StyledText getTextField() {
 		return this.textfield;
 	}
 	public StyledText getLineNumbers() {
 		return this.linenumbers;
 	}
 }
