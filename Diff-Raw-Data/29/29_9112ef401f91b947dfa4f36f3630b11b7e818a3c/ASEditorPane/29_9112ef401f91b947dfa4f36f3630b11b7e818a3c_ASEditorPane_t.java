 // ----------------------------------------------------------------------------
 // Copyright (C) 2003 Rafael H. Bordini, Jomi F. Hubner, et al.
 //
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 //
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 // Lesser General Public License for more details.
 //
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 //
 // To contact the authors:
 // http://www.csc.liv.ac.uk/~bordini
 // http://www.inf.furb.br/~jomi
 //----------------------------------------------------------------------------
 
 package jIDE;
 
 
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.FontMetrics;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.io.EOFException;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 import javax.swing.text.AbstractDocument;
 import javax.swing.text.BoxView;
 import javax.swing.text.ComponentView;
 import javax.swing.text.Element;
 import javax.swing.text.IconView;
 import javax.swing.text.LabelView;
 import javax.swing.text.ParagraphView;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyleContext;
 import javax.swing.text.StyledEditorKit;
 import javax.swing.text.View;
 import javax.swing.text.ViewFactory;
 import javax.swing.undo.UndoManager;
 
 class ASEditorPane extends JPanel {
 
 	JTextPane editor;
 	UndoManager undo;
 	JScrollPane editorScroller;
 	boolean modified = false;
 	boolean needsParsing = false;
 	//boolean lastKeyWasSave = false;
 
 	private String fileName = "";
 	String extension = "asl";
 	JasonID mainID = null;
 	int tabIndex;
 
 	//EditorKit editorKit = new EditorKit(new ASContext(), "text/asl");
 	//ASContext context = new ASContext();
 	ASSyntaxHighLight syntaxHL;
 	
 	
 	ASEditorPane() {
 		this(null, 0);
 	}
 
 	ASEditorPane(final JasonID mainID, final int tabIndex) {
 		super(true);
 		this.mainID = mainID;
 		this.tabIndex = tabIndex;
 		createEditor();
 		createUndoManager();
 		editorScroller = new JScrollPane();
 		editorScroller.getViewport().add(editor);
 		setLayout(new BorderLayout());
 		add(BorderLayout.CENTER, editorScroller);
 		createSyntaxHighlightThread();
 		updateFont();
 	}
 	
 	void createSyntaxHighlightThread() {
 		syntaxHL = new ASSyntaxHighLight(editor, mainID);
 		//syntaxThread.start();
 	}
 
 	public String getFileName() {
 		return fileName;
 	}
 
 	public void setFileName(String file) {
 		this.fileName = removeExtension(file);
 	}
 	
 	public void setFileName(File f) {
 		try {
 			setFileName(f.getCanonicalPath());
 		} catch (Exception e) {
 			setFileName(f.toString());
 		}
 	}
 	
     protected String removeExtension(String s) {
        	if (mainID != null) {
         	if (s.startsWith(mainID.projectDirectory)) {
         		s = s.substring(mainID.projectDirectory.length()+1);
         	}
         	if (s.startsWith("./")) {
         		s = s.substring(2);
         	}
         }
         int i = s.lastIndexOf('.');
         if (i > 0 &&  i < s.length() - 1) {
             return s.substring(0, i);
         } else {
             return s;
         }
     }
 	
 	void createNewPlainText(String text) {
 		//editor.setDocument(new DefaultStyledDocument());
 		//createUndoManager(); // only when new doc is created
 		try {
 			editor.getDocument().remove(0, editor.getDocument().getLength());
 			editor.getDocument().insertString(0, text, null);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		//syntaxThread.addDocListener(); // only when new doc is created
 		syntaxHL.repainAll();
 	}
 
 	void createNewPlainText(InputStream in) {
 		StringBuffer s = new StringBuffer();
 		try {
 			int c = in.read();
 			while (c != -1) {
 				s.append((char)c);
 				c = in.read();
 			}
 		} catch (EOFException e) {
 		} catch (IOException e) {
 			System.err.println("Error reading text!");
 			e.printStackTrace();
 		}
 		createNewPlainText(s.toString());
 	}
 
 	/**
 	 * Create an editor to represent the given document.
 	 */
 	protected void createEditor() {
 		editor = new JTextPane();//JEditorPane(); //new JTextArea();
 
 		editor.setDragEnabled(true);
 
 		editor.setEditorKit(new StyledEditorKit() {
 			public ViewFactory getViewFactory() {
 				return new NumberedViewFactory();
 			}
 		});
 		
 		editor.getDocument().addDocumentListener(new DocumentListener() {
 			public void changedUpdate(DocumentEvent arg0) {
 			}
 			public void insertUpdate(DocumentEvent arg0) {
 				updateTab();
 			}
 			public void removeUpdate(DocumentEvent arg0) {
 				updateTab();
 			}
 			void updateTab() {
 				needsParsing = true;
 				if (mainID != null) {
					if (!modified) {
						modified = true;
						mainID.updateTabTitle(tabIndex, ASEditorPane.this, null);
					}
 					if (mainID.fASParser != null) {
 						mainID.fASParser.stopWaiting();
 					}
 					if (mainID.fMAS2jThread != null) {
 						mainID.fMAS2jThread.stopWaiting();
 					}
 				}

 				if (!mainID.runMASButton.isEnabled()) {
 					mainID.runMASButton.setEnabled(true);
 				}
 				if (!mainID.debugMASButton.isEnabled()) {
 					mainID.debugMASButton.setEnabled(true);
 				}
 			}
 		});
 
 		//editor.setEditorKitForContentType(editorKit.getContentType(), editorKit);
 		//editor.setContentType("text/asl");
 	}
 
 	String strToFind = null;
 	public void askSearch() {
 		// search
 		strToFind = JOptionPane.showInputDialog(null, "What to search?", "Find", JOptionPane.QUESTION_MESSAGE);
 		search();
 	}
 	public void search() {
 		if (strToFind == null) return;
 		try {
 			int txtLength = editor.getDocument().getLength();
 			int pos = editor.getCaretPosition()+1;
 			if (pos+1 >= txtLength) {
 				pos = 0;
 			}
 			String text = editor.getDocument().getText(pos,txtLength-pos);
 			if (text != null) {
 				int loc = text.indexOf(strToFind);
 				if (loc >= 0) {
 					editor.grabFocus();
 					editor.setCaretPosition(pos+loc);
 				} else {
 					pos = 0;
 					text = editor.getDocument().getText(pos,txtLength-pos);
 					if (text != null) {
 						loc = text.indexOf(strToFind);
 						if (loc >= 0) {
 							editor.setCaretPosition(pos+loc);
 						} else {
 							Toolkit.getDefaultToolkit().beep();
 						}
 					}
 				}
 			}
 		} catch (Exception cue) {
 			System.err.println(cue);
 			cue.printStackTrace();
 		}
 	}
 
 	
 	
 	public void updateFont() {
 		if (mainID != null) {
 			String font = JasonID.userProperties.getProperty("font");
 			if (font == null) {
 				return;
 			}
 			int size = 12;
 			try {
 				size = Integer.parseInt(JasonID.userProperties.getProperty("fontSize"));
 			} catch (Exception e) {}
 			syntaxHL.setFont(font, size);
 		}
 	}
 
 	
 /*	
 	class EditorKit extends DefaultEditorKit {
 		String mime;
 		EditorKit(ASContext style, String mime) {
 			context = style;
 			this.mime = mime;
 		}
 		public ASContext getStylePreferences() {
 			return context;
 		}
 		public String getContentType() {
 			return mime;
 		}
 		public Document createDefaultDocument() {
 			return new PlainDocument();
 		}
 		public final ViewFactory getViewFactory() {
 			return getStylePreferences();
 		}
 		//public ViewFactory getViewFactory() {
 		//	return new NumberedViewFactory();
 		//}
 	}
 	*/
 
 	private void createUndoManager() {
 		// undo support
 		undo = new UndoManager();
 		undo.setLimit(200);
 		editor.getDocument().addUndoableEditListener(new UndoableEditListener() {
 			/**
 			 * Messaged when the Document has created an edit, the edit is added
 			 * to <code>undo</code>, an instance of UndoManager.
 			 */
 			public void undoableEditHappened(UndoableEditEvent e) {
 				if (!e.getEdit().getPresentationName().startsWith("style")) {
 					undo.addEdit(e.getEdit());
 				}
 			}
 		});
 	}
 
 	String getDefaultText(String s) {
 		return "// "+s+"\ndemo.\n+demo : true <- .print(\"hello world.\").";
 	}
 
 	class NumberedViewFactory implements ViewFactory {
 		public View create(Element elem) {
 			String kind = elem.getName();
 			if (kind != null)
 				if (kind.equals(AbstractDocument.ContentElementName)) {
 					return new LabelView(elem);
 				} else if (kind.equals(AbstractDocument.ParagraphElementName)) {
 					//	              return new ParagraphView(elem);
 					return new NumberedParagraphView(elem);
 				} else if (kind.equals(AbstractDocument.SectionElementName)) {
 					return new BoxView(elem, View.Y_AXIS);
 				} else if (kind.equals(StyleConstants.ComponentElementName)) {
 					return new ComponentView(elem);
 				} else if (kind.equals(StyleConstants.IconElementName)) {
 					return new IconView(elem);
 				}
 			// default to text display
 			return new LabelView(elem);
 		}
 	}
 
 	public static int NUMBERS_WIDTH = 25;
 	
 	class NumberedParagraphView extends ParagraphView {
 
 		public NumberedParagraphView(Element e) {
 			super(e);
 			short top = 0;
 			short left = 0;
 			short bottom = 0;
 			short right = 0;
 			this.setInsets(top, left, bottom, right);
 		}
 
 		protected void setInsets(short top, short left, short bottom, short right) {
 			super.setInsets(top, (short) (left + NUMBERS_WIDTH), bottom, right);
 		}
 
 		public void paintChild(Graphics g, Rectangle r, int n) {
 			super.paintChild(g, r, n);
 			FontMetrics fm = g.getFontMetrics();
 
 			int previousLineCount = getPreviousLineCount();
 			String sNB = (getPreviousLineCount() + n + 1)+" ";
 			int width = fm.charsWidth( sNB.toCharArray(), 0, sNB.length());
 			if (width > NUMBERS_WIDTH) {
 				NUMBERS_WIDTH = width + 15;
 				setInsets((short)0, (short)0, (short)0, (short)0);
 			}
 			int numberX = r.x - getLeftInset();
 			int numberY = r.y + r.height - 5;
 			g.setColor(Color.BLACK);
 			g.setFont(syntaxHL.context.getFont(syntaxHL.context.getStyle(StyleContext.DEFAULT_STYLE)));
 			g.drawString(sNB, numberX, numberY);
 		}
 
 		public int getPreviousLineCount() {
 			int lineCount = 0;
 			View parent = this.getParent();
 			int count = parent.getViewCount();
 			for (int i = 0; i < count; i++) {
 				if (parent.getView(i) == this) {
 					break;
 				} else {
 					lineCount += parent.getView(i).getViewCount();
 				}
 			}
 			return lineCount;
 		}
 	}
}
