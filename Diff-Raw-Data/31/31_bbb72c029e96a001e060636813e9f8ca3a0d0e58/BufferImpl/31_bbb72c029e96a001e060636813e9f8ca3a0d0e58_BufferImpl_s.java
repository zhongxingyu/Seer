 package kkckkc.jsourcepad.model;
 
 import kkckkc.jsourcepad.model.Doc.StateListener;
 import kkckkc.jsourcepad.model.Finder.Options;
 import kkckkc.jsourcepad.model.bundle.BundleManager;
 import kkckkc.jsourcepad.model.bundle.PrefKeys;
 import kkckkc.jsourcepad.model.settings.AdvancedSettings;
 import kkckkc.jsourcepad.util.ui.CompoundUndoManager;
 import kkckkc.syntaxpane.model.*;
 import kkckkc.syntaxpane.model.LineManager.Line;
 import kkckkc.syntaxpane.parse.grammar.Language;
 import kkckkc.syntaxpane.regex.JoniPatternFactory;
 import kkckkc.syntaxpane.style.Style;
 import kkckkc.utils.CharSequenceUtils;
 import org.jetbrains.annotations.NotNull;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.plaf.TextUI;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Caret;
 import javax.swing.text.Highlighter.HighlightPainter;
 import javax.swing.text.JTextComponent;
 import java.awt.*;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class BufferImpl implements Buffer {
     private static Pattern WORD_PATTERN;
 
 	// State
 	private InsertionPoint insertionPoint;
 	private TextInterval selection;
 	private int unmodifiedLength = 0;
 	private int unmodifiedHash = 0;
 	private boolean modified = false;
 
 	// View references
 	private Caret caret;
 	private SourceDocument document;
 	private JTextComponent textComponent;
 
 	// Structure fields
 	private Window window;
 	private Doc doc;
 
 	// Helper
 	private DocumentStateListener documentStateListener;
 	private AnchorManager anchorManager;
     private CompletionManager completionManager;
 
 	// Restricted editing
     private RestrictedEditor restrictedEditor;
 
 	private CharacterPairsHandler characterPairsHandler;
 
     // Search and replace
     private Finder finder;
     private CompoundUndoManager undoManager;
 
 
     public BufferImpl(SourceDocument sourceDocument, Doc doc, Window window) {
 		this.window = window;
 	    this.document = sourceDocument;
 	    
 	    this.doc = doc;    
 
         this.documentStateListener = new DocumentStateListener();
 		this.anchorManager = new AnchorManager();
 
         this.completionManager = new CompletionManager(this);
 
         synchronized (BufferImpl.class) {
             if (WORD_PATTERN == null) {
                 AdvancedSettings advancedSettings = Application.get().getSettingsManager().get(AdvancedSettings.class);
                 WORD_PATTERN = Pattern.compile(advancedSettings.getWordPattern());
             }
         }
 
     }
 
     public void close() {
         textComponent.getCaret().deinstall(textComponent);
         textComponent.setDocument(new SourceDocument());
 
         for (DocumentListener dl : document.getDocumentListeners()) {
             document.removeDocumentListener(dl);
         }
 
         this.caret.deinstall(textComponent);
         document.setDocumentFilter(null);
     }
 
     @Override
     public JTextComponent getTextComponent() {
         return textComponent;
     }
 
     @Override
 	public void bind(final JTextComponent jtc) {
 		jtc.setDocument(document);
         this.caret = jtc.getCaret();
 
         // Work around issue with substance look and feel
         EventQueue.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 BufferImpl.this.caret = jtc.getCaret();
                 BufferImpl.this.caret.addChangeListener(new ChangeListener() {
                     @Override
                     public void stateChanged(ChangeEvent e) {
                         if (restrictedEditor != null) restrictedEditor.caretPositionChanged(caret.getDot());
                     }
                 });
 
                 BufferImpl.this.caret.addChangeListener(new ChangeListener() {
                     @Override
                     public void stateChanged(ChangeEvent e) {
                         if (caret.getDot() == caret.getMark()) {
                             selection = null;
                             if (insertionPoint == null || insertionPoint.getPosition() != caret.getDot() || selection != null) {
                                 insertionPoint = new InsertionPoint(caret.getDot(), document.getScopeForPosition(caret.getDot()), document.getLineManager());
                                 postInsertionPointUpdate();
                             }
                         } else {
                             selection = new BufferTextInterval(caret.getDot(), caret.getMark());
                             window.topic(Buffer.SelectionListener.class).post().selectionModified(BufferImpl.this);
                         }
                     }
                 });
 
                 BufferImpl.this.caret.addChangeListener(completionManager);
             }
         });
 
 		document.addDocumentListener(new DocumentListener() {
             @Override
 			public void removeUpdate(DocumentEvent e) {
 				if (restrictedEditor != null) restrictedEditor.textChanged(e);
 			}
 
             @Override
 			public void insertUpdate(DocumentEvent e) {
 				if (restrictedEditor != null) restrictedEditor.textChanged(e);
 			}
 
             @Override
 			public void changedUpdate(DocumentEvent e) {
 			}
 		});
 
 		document.addDocumentListener(documentStateListener);
 		document.addDocumentListener(anchorManager);
 
 	    this.textComponent = jtc;
 
 	    characterPairsHandler = new CharacterPairsHandler(this, anchorManager);
 	    document.setDocumentFilter(characterPairsHandler);
 
         undoManager = new CompoundUndoManager(jtc);
 
 		postInsertionPointUpdate();
 	}
 
 	@Override
 	public String getText(Interval interval) {
 		if (interval == null || interval.isEmpty()) return "";
 	    try {
 	        return document.getText(interval.getStart(), interval.getLength());
         } catch (BadLocationException e) {
 	        throw new RuntimeException(e);
         }
     }
 
 	@Override
     public void replaceText(Interval interval, String s, Anchor[] anchors) {
         boolean reselect = false;
         if (interval.equals(getSelection())) {
             reselect = true;
         }
 
 		adjustAnchorList(interval.getStart(), anchors);
 		
 		try {
             undoManager.beginCompoundOperation();
             try {
 			    document.replace(interval.getStart(), interval.getLength(), s, null);
                 if (reselect) {
                     Interval newSelection = Interval.createWithLength(interval.getStart(), s.length());
                     setSelection(newSelection);
                 }
             } finally {
                 undoManager.endCompoundOperation();
             }
 		} catch (BadLocationException e) {
 			throw new RuntimeException(e);
 		}
 		
 		anchorManager.addAnchors(anchors);
 	}
 
 	@Override
     public void insertText(int position, String content, Anchor[] anchors) {
 		adjustAnchorList(position, anchors);
 		try {
 	        document.insertString(Math.max(position, 0), content, null);
         } catch (BadLocationException e) {
         	throw new RuntimeException(e);
         }
 		anchorManager.addAnchors(anchors);
     }
 
 
 	@Override
     public InsertionPoint getInsertionPoint() {
 		if (this.insertionPoint == null) {
 			this.insertionPoint = new InsertionPoint(0, document.getScopeForPosition(0), document.getLineManager());
 		}
 		return insertionPoint;
     }
 	
 	@Override
 	public TextInterval getSelection() {
     // TODO: Investigate if we should enable the following
 //        if (selection == null || selection.isEmpty()) return null;
 		return selection;
 	}
 
     @NotNull
     @Override
 	public TextInterval getSelectionOrCurrentLine() {
 		if (selection != null && ! selection.isEmpty()) return selection;
 		
 		Line line = document.getLineManager().getLineByPosition(caret.getDot());
 		return new BufferTextInterval(line.getStart(), line.getEnd());
 	}
 
     @Override
     public TextInterval getSelectionOrCurrentParagraph() {
         if (selection != null && ! selection.isEmpty()) return selection;
 
         LineManager lm = document.getLineManager();
 
         Line line = lm.getLineByPosition(caret.getDot());
         Line endLine = line;
         while (endLine != null && ! CharSequenceUtils.isWhitespace(endLine.getCharSequence(false))) {
             endLine = lm.getNext(endLine);
         }
         
         Line startLine = line;
         while (startLine != null && ! CharSequenceUtils.isWhitespace(startLine.getCharSequence(false))) {
             startLine = lm.getPrevious(startLine);
         }
 
         int start = startLine == null ? 0 : startLine.getEnd() + 1;
         int end = endLine == null ? document.getLength() : endLine.getStart() - 1;
 
         return new BufferTextInterval(start, end);
     }
 
 
     @Override
 	public void setSelection(Interval selection) {
 		this.selection = new BufferTextInterval(selection);
 		this.caret.setDot(selection.getStart());
 		if (! selection.isEmpty()) {
 			this.caret.moveDot(selection.getEnd());
 		}
 	}
 
 	@Override
     public TextInterval getCompleteDocument() {
 	    return new BufferTextInterval(0, document.getLength());
     }
 
 	@Override
     public void remove(Interval interval) {
 	    try {
 	        document.remove(interval.getStart(), interval.getLength());
         } catch (BadLocationException e) {
 	        throw new RuntimeException(e);
         }
     }
 
 	@Override
 	public void setLanguage(Language language) {
 		documentStateListener.disable();
 		document.setLanguage(language);
 		documentStateListener.enable();
 
 		if (caret != null && insertionPoint != null) {
 			int pos = insertionPoint.getPosition();
 			this.insertionPoint = new InsertionPoint(pos, document.getScopeForPosition(pos), document.getLineManager());
 			postInsertionPointUpdate();
 		}
 		
 		window.topic(Buffer.LanguageListener.class).post().languageModified(this);
 	}
 	
     @Override
 	public Language getLanguage() {
 		return document.getLanguage();
 	}
 	
 	@Override
     public Doc getDoc() {
 	    return doc;
     }
 
 	@Override
     public boolean isModified() {
 	    return modified;
     }
 
 	@Override
     public void clearModified() {
 	    this.modified = false;
 	    
 	    Interval all = getCompleteDocument();
 		this.unmodifiedLength = all.getLength();
 		this.unmodifiedHash = getText(all).hashCode();
 	}
 
 	@Override
     public void setText(Language language, BufferedReader br) throws IOException {
 		documentStateListener.disable();
 
 		document.setLanguage(language);
         try {
             document.remove(0, document.getLength());
         } catch (BadLocationException e) {
             throw new RuntimeException(e);
         }
 
         doc.getDocList().getWindow().beginWait(true, null);
 
         StringBuilder builder = new StringBuilder();
 		try {
 			String line;
 			while ((line = br.readLine()) != null) {
                 builder.append(line).append("\n");
 			}
 
             document.insertString(0, builder.toString(), null);
 		} catch (BadLocationException e) {
 			throw new RuntimeException(e);
 		}
 
 		clearModified();
 
         doc.getDocList().getWindow().endWait();
 
 
 		documentStateListener.enable();
 
 		if (caret != null) {
 			caret.setDot(0);
 		}
     }
 
 	@Override
     public void indent(Interval interval) {
 	    Line line = document.getLineManager().getLineByPosition(interval.getStart());
 	    while (line.getStart() <= interval.getEnd()) {
 	    	indent(line);
 
 	    	line = document.getLineManager().getNext(line);
 	    	if (line == null) return;
 	    }
     }
 
 	private void adjustAnchorList(int position, Anchor[] anchors) {
     	if (anchors == null) return;
     	for (Anchor a : anchors) {
     		a.move(position);
     	}
     }
 
 	private void postInsertionPointUpdate() {
         window.topic(Buffer.InsertionPointListener.class).post().update(getInsertionPoint());
     	window.topic(Buffer.SelectionListener.class).post().selectionModified(this);
 
 		characterPairsHandler.highlight();
 	}
 
 	private void indent(Line current) {
 		if (current == null) return;
 		
 		Line prev = document.getLineManager().getPrevious(current);
 		if (prev == null) return;
 
 		BundleManager bundleManager = Application.get().getBundleManager();
 		
     	CharSequence prevLine = prev.getCharSequence(false);
     	int indentCount = doc.getTabManager().getTabCount(prevLine);
     	
     	String decrease = (String) bundleManager.getPreference(PrefKeys.INDENT_DECREASE, current.getScope());
     	String increase = (String) bundleManager.getPreference(PrefKeys.INDENT_INCREASE, current.getScope());
     	String indentNextLine = (String) bundleManager.getPreference(PrefKeys.INDENT_NEXT_LINE, current.getScope());
     	String unIndentedLinePattern = (String) bundleManager.getPreference(PrefKeys.INDENT_IGNORE, current.getScope());
     	
     	int position = caret.getDot();
     	
     	if (matches(unIndentedLinePattern, prevLine)) {
     		// Do nothing
     	} else if (matches(increase, prevLine)) {
     		indentCount++;
     	} else if (matches(indentNextLine, prevLine)) {
     		indentCount++;
     	} else if (matches(decrease, prevLine)) {
     		
     		Line prevprev = document.getLineManager().getPrevious(prev);
     		if (prevprev != null) {
     			if (doc.getTabManager().getTabCount(prevprev.getCharSequence(false)) == indentCount && indentCount > 0) {
     				String s = doc.getTabManager().getFirstIndentionString(prev.getCharSequence(false));
     				Interval interval = Interval.createWithLength(prev.getStart(), s.length());
 	                doc.getActiveBuffer().remove(interval);
 	                position -= s.length();
     				
     	    		indentCount--;
     			}
     		}
 
     	} else {
     		Line prevprev = document.getLineManager().getPrevious(prev);
     		if (prevprev != null) {
     			if (matches(indentNextLine, prevprev.getCharSequence(false))) {
     				indentCount--;
     			}
     		}
     	}
 
         if (matches(decrease, current.getCharSequence(false))) {
             indentCount--;
         }
 
     	// Remove old indent
 		String s;
 		while ((s = doc.getTabManager().getFirstIndentionString(current.getCharSequence(false))) != null) {
 			Interval interval = Interval.createWithLength(current.getStart(), s.length());
             doc.getActiveBuffer().remove(interval);
             position -= s.length();
 		}
 
     	String indent = doc.getTabManager().createIndent(indentCount);
         doc.getActiveBuffer().insertText(current.getStart(), indent, null);
     }
 
 	private boolean matches(String pattern, CharSequence charSequence) {
 		JoniPatternFactory factory = new JoniPatternFactory();
 	    return pattern != null && factory.create(pattern).matcher(charSequence).matches();
     }
 	
 	@Override
 	public void shift(Interval interval, int length) {
 		LineManager lm = document.getLineManager();
 		
 		Line line = lm.getLineByPosition(interval.getStart());
 		
 		int end = interval.getEnd();
 		String indent = doc.getTabManager().createIndent(Math.abs(length));
 		while (true) {
             if (line.getStart() >= end) break;
 
 			if (length > 0) {
 				insertText(line.getStart(), indent, null);
                 end = end + indent.length();
 			} else {
 				if (doc.getTabManager().getTabCount(line.getCharSequence(false)) > 0) {
 	                remove(Interval.createWithLength(line.getStart(), indent.length()));
 	                end = end - indent.length();
 				}   
 			}
 
 			line = lm.getNext(line);
 			if (line == null) break;
 		}
 	}
 
     @Override
     public Finder getFinder() {
         return this.finder;
     }
 
     @Override
     public Finder newFinder(Interval scope, String searchFor, Options options) {
         return (this.finder = new Finder(this, scope, searchFor, options));
     }
 	
 	
 	class DocumentStateListener implements DocumentListener {
 		private boolean disabled = false;
 		
 		public void disable() {
 			this.disabled = true;
 		}
 		
 		public void enable() {
 			this.disabled = false;
 		}
 		
         @Override
 		public void removeUpdate(DocumentEvent e) {
 			modify();
 		}
 		
         @Override
 		public void insertUpdate(DocumentEvent e) {
 			modify();
 		}
 		
         @Override
 		public void changedUpdate(DocumentEvent e) {
 		}
		
 		private void modify() {
 			if (disabled) return;
 
             boolean oldState = modified;
 
 			// If document is already modified, check if it is modified
 			// back to the original document
 			if (modified) {
 
 				// Optimization: Only calculate new hash if new length is
 				// same as length of unmodified file
 				if (document.getLength() == unmodifiedLength) {
 					int newHash = getText(getCompleteDocument()).hashCode();
 					if (newHash == unmodifiedHash) {
 						modified = false;
 					}
 				}
 
                 window.topic(StateListener.class).post().modified(getDoc(), modified, oldState);
 
 			// Else, this modification will tag the buffer as modified
 			} else {
 				
 				modified = true;
 				window.topic(StateListener.class).post().modified(getDoc(), modified, oldState);
 			}
 		}
 	}
 	
 	@Override
     public void beginRestrictedEditing(final RestrictedEditor restrictedEditor) {
 		restrictedEditor.init(textComponent.getInputMap(), textComponent.getActionMap());
 		this.restrictedEditor = restrictedEditor;
 	}
 
 	@Override
 	public void endRestrictedEditing() {
         restrictedEditor = null;
     }
 
 	@Override
     public int getLength() {
 	    return document.getLength();
     }
 
     @Override
     public void scrollTo(int position, ScrollAlignment scrollAlignment) {
         try {
             Rectangle re = this.textComponent.modelToView(position);
             if (scrollAlignment == ScrollAlignment.MIDDLE)
                 re.translate(0, this.textComponent.getVisibleRect().height / 2);
             JViewport jvp = (JViewport) this.textComponent.getParent();
             jvp.setViewPosition(re.getLocation());
         } catch (BadLocationException e) {
             throw new RuntimeException(e);
         }
     }
 
     @Override
     public int getTopLeftPosition() {
         Point location = this.textComponent.getVisibleRect().getLocation();
         return this.textComponent.viewToModel(location);  
     }
 
     @Override
     public TextInterval getCurrentLine() {
 		Line line = document.getLineManager().getLineByPosition(caret.getDot());
         return new BufferTextInterval(line.getStart(), line.getEnd());
     }
 
 	@Override
     public TextInterval getCurrentWord() {
         Interval lineInterval = getCurrentLine();
         if (lineInterval == null) return null;
         
 		String line = getText(lineInterval);
 		int index = getInsertionPoint().getLineIndex();
 		
 		Matcher matcher = WORD_PATTERN.matcher(line);
 		while (matcher.find()) {
 			if (matcher.start() <= index && matcher.end() >= index) {
 				return new BufferTextInterval(lineInterval.getStart() + matcher.start(), lineInterval.getStart() + matcher.end());
 			}
 		}
 		
 		return null;
     }
 
     @Override
     public TextInterval getCurrentScope() {
         Scope scope = insertionPoint.getScope();
         TextInterval interval = getMultiLineScope(scope);
 
         while (getSelection() != null && getSelection().contains(interval)) {
             scope = scope.getParent();
             if (scope == null) return getCompleteDocument();
 
             interval = getMultiLineScope(scope);
         }
 
         return interval;
     }
 
     private TextInterval getMultiLineScope(final Scope scope) {
         String path = scope.getPath();
 
         Line line = getLineManager().getLineByPosition(insertionPoint.getPosition());
 
         Scope currentScope = scope;
         while (currentScope.getStart() == Integer.MIN_VALUE) {
             line = getLineManager().getPrevious(line);
             if (line == null) break;
             currentScope = document.getScopeForPosition(line.getEnd());
             while (! currentScope.getPath().equals(path)) {
                 currentScope = currentScope.getParent();
             }
         }
 
         int start = line == null ? 0 : line.getStart() + currentScope.getStart();
 
         currentScope = scope;
         line = getLineManager().getLineByPosition(insertionPoint.getPosition());
 
         while (currentScope.getEnd() == Integer.MAX_VALUE) {
             line = getLineManager().getNext(line);
             if (line == null) break;
             currentScope = document.getScopeForPosition(line.getStart());
             while (! currentScope.getPath().equals(path)) {
                 currentScope = currentScope.getParent();
             }
         }
 
         int end = line == null ? getLength() : (line.getStart() + currentScope.getEnd());
 
         return new BufferTextInterval(start, end);
     }
 
     @Override
     public Highlight highlight(final Interval interval, final HighlightType type, final Style style, final boolean isTransient) {
 	    try {
 			final Object o = textComponent.getHighlighter().addHighlight(interval.getStart(), interval.getEnd(), new HighlightPainter() {
 				@Override
 	            public void paint(Graphics g, int o0, int o1, Shape bounds, JTextComponent component) {
 				    try {
 						TextUI mapper = component.getUI();
 						Rectangle p0 = mapper.modelToView(component, o0);
 						Rectangle p1 = mapper.modelToView(component, o1);
 						
 						if (p0.y == p1.y) {
 						    // same line, render a rectangle
 						    Rectangle r = p0.union(p1);
 						    
 						    if (type == HighlightType.Box) {
 							    if (style.getBackground() != null) {
 							    	g.setColor(style.getBackground());
 								    g.fillRect(r.x, r.y, r.width - 1, r.height - 1);
 							    }
 							    
 							    if (style.getBorder() != null) {
 							    	g.setColor(style.getBorder());
 								    g.drawRect(r.x, r.y, r.width - 1, r.height - 1);
 							    }
 						    } else {
 						    	g.setColor(style.getColor());
 						    	g.drawLine(p0.x, p0.y + p0.height, p1.x + p1.width, p0.y + p0.height);
 						    }
 						    
 						} else {
 							throw new UnsupportedOperationException("Cross-line highlights not implemented yet");
 						}
 				    } catch (BadLocationException ble) {
 				    	throw new RuntimeException(ble);
 				    }
 	            }
 			});
 
 			this.document.addDocumentListener(new DocumentListener() {
 				@Override
 				public void removeUpdate(DocumentEvent e) {
 	                textComponent.getHighlighter().removeHighlight(o);
 	                document.removeDocumentListener(this);
 				}
 				
 				@Override
 				public void insertUpdate(DocumentEvent e) {
 	                textComponent.getHighlighter().removeHighlight(o);
 	                document.removeDocumentListener(this);
 				}
 				
 				@Override
 				public void changedUpdate(DocumentEvent e) {
 	                textComponent.getHighlighter().removeHighlight(o);
 	                document.removeDocumentListener(this);
 				}
 			});
 
 			this.caret.addChangeListener(new ChangeListener() {
 				@Override
 				public void stateChanged(ChangeEvent e) {
 	                textComponent.getHighlighter().removeHighlight(o);
 	                caret.removeChangeListener(this);
 				}
 			});
 
 			return new Highlight() {
                 @Override
                 public void clear() {
 	                textComponent.getHighlighter().removeHighlight(o);
                 }
 			};
 	    } catch (BadLocationException ble) {
 	    	throw new RuntimeException(ble);
 	    }
     }
 
 	@Override
     public Interval find(int position, String pattern, FindType type, Direction direction) {
 		try {
 			if (type == FindType.Regexp) {
 				throw new UnsupportedOperationException("Not implemented yet");
 			} else {
 				if (direction == Direction.Backward) {
 					String s = document.getText(0, position);
 					int p = s.lastIndexOf(pattern);
 					if (p < 0) return null;
 					return Interval.createWithLength(p, pattern.length());
 				} else {
 					if (getLength() <= (position + 1)) return null;
 					String s = document.getText(position + 1, getLength() - (position + 1));
 					int p = s.indexOf(pattern);
 					if (p < 0) return null;
 					return Interval.createWithLength(p + position + 1, pattern.length());
 				}
 			}
 		} catch (BadLocationException ble) {
 			throw new RuntimeException(ble);
 		}
     }
 
 	@Override
 	public LineManager getLineManager() {
 		return document.getLineManager();
 	}
 
     @Override
     public FoldManager getFoldManager() {
         return document.getFoldManager();
     }
 
     @Override
     public ActionMap getActionMap() {
 	    return textComponent.getActionMap();
     }
 
     public CompletionManager getCompletionManager() {
         return this.completionManager;
     }
 
     @Override
     public void undo() {
         undoManager.undo();
     }
 
     @Override
     public void redo() {
         undoManager.redo();
     }
 
     @Override
     public boolean canUndo() {
         return undoManager.canUndo();
     }
 
     @Override
     public boolean canRedo() {
         return undoManager.canRedo();
     }
 
 
     class BufferTextInterval extends TextInterval {
         public BufferTextInterval(int start, int end) {
             super(start, end);
         }
 
         public BufferTextInterval(Interval interval) {
             super(interval.getStart(), interval.getEnd());
         }
 
         @Override
         public String getText() {
             return BufferImpl.this.getText(this);
         }
     }
 }
