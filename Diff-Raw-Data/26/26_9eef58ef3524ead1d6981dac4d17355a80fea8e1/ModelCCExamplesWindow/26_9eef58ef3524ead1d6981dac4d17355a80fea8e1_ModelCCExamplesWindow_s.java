 /*
  * ModelCC, under ModelCC Shared Software License, www.modelcc.org. Luis Quesada Torres.
  */
 
 
 package org.modelcc.examples.test;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.JTree;
 import javax.swing.ImageIcon;
 import javax.swing.JSplitPane;
 import javax.swing.JTextArea;
 import javax.swing.JButton;
 import javax.swing.JScrollPane;
 import javax.swing.border.MatteBorder;
 import java.awt.Color;
 
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.event.TreeWillExpandListener;
 import javax.swing.event.UndoableEditEvent;
 import javax.swing.event.UndoableEditListener;
 
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.ExpandVetoException;
 import javax.swing.tree.TreeSelectionModel;
 import javax.swing.undo.CannotRedoException;
 import javax.swing.undo.CannotUndoException;
 import javax.swing.undo.UndoManager;
 
 import org.modelcc.io.ModelReader;
 import org.modelcc.io.java.JavaModelReader;
 import org.modelcc.lexer.recognizer.PatternRecognizer;
 import org.modelcc.lexer.recognizer.regexp.RegExpPatternRecognizer;
 import org.modelcc.metamodel.Model;
 import org.modelcc.parser.Parser;
 import org.modelcc.parser.ParserException;
 import org.modelcc.parser.ParserFactory;
 import javax.swing.SwingConstants;
 import java.awt.Component;
 import javax.swing.Box;
 
 /**
  * ModelCC Examples GUI.
  * @author elezeta
  */
 public class ModelCCExamplesWindow extends JFrame {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	
 	private Class languageClass;
     private Parser parser;
     JTree examplesTree;
 	
 	private JPanel mainPanel;
 
 	protected UndoManager undoManager = new UndoManager();
 
 	private KeyListener keyListener = new KeyListener() {
 	    public void keyPressed(KeyEvent e) {
 	    	if (e.getModifiers()==KeyEvent.ALT_MASK && e.getKeyCode() == 10) { 
 	    		process();
 	    	}
 	    	if (e.getModifiers()==(KeyEvent.CTRL_MASK|KeyEvent.SHIFT_MASK) && e.getKeyCode() == 90) { 
 	    		try {
 	    			undoManager.redo();
 	    		} catch (Exception ex) {
 	    		}
     		}
 	    	else if (e.getModifiers()==KeyEvent.CTRL_MASK && e.getKeyCode() == 90) { 
 	    		try {
 	    			undoManager.undo();
 	    		} catch (Exception ex) {
 	    		}
     		}
 	    }
 
 	    public void keyReleased(KeyEvent e) { }
 
 	    public void keyTyped(KeyEvent e) {
 	    }
 	};
 	
 	class ChangeDocumentListener implements DocumentListener {
 		@Override
 		public void removeUpdate(DocumentEvent e) {
 			check();
 		}
 		
 		
 		@Override
 		public void insertUpdate(DocumentEvent e) {
 			check();
 		}
 		
 		@Override
 		public void changedUpdate(DocumentEvent e) {
 			check();
 		}
 		
 		public void check() {
 			if (!autoChange) {
 				if (!examplesTree.isSelectionEmpty()) {
 			    	if (!inputTextArea.getText().equals(originalText)) {
 						if (((InfoMutableTreeNode)examplesTree.getLastSelectedPathComponent()).getTextNumber()!=0)
 							examplesTree.setSelectionPath(examplesTree.getSelectionPath().getParentPath());
 			    	}
 				}
 			}
 		}
 		
 		boolean autoChange = false;
 		
 		public void setAutoChange(boolean autoChange) {
 			this.autoChange = autoChange;
 		}
 		public boolean getAutoChange() {
 			return autoChange;
 		}
 	};				
 
 	ChangeDocumentListener changeListener = new ChangeDocumentListener();
 	
 	/**
 	 * Create the frame.
 	 */
 	public ModelCCExamplesWindow() {
 		undoManager.setLimit(2000);
 
 		setMinimumSize(new Dimension(620,400));
 		setSize(new Dimension(620,400));
 		setIconImage(Toolkit.getDefaultToolkit().getImage(ModelCCExamplesWindow.class.getResource("/org/modelcc/examples/test/icon.png")));
 		setTitle("ModelCC Examples");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 800, 600);
 		mainPanel = new JPanel();
 		mainPanel.setBorder(new EmptyBorder(0, 0, 0, 0));
 		mainPanel.setLayout(new BorderLayout(0, 0));
 		setContentPane(mainPanel);
 		
 		JPanel leftPanel = new JPanel();
 		mainPanel.add(leftPanel, BorderLayout.WEST);
 		leftPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 		leftPanel.setLayout(new BorderLayout(0, 0));
 		
 		JLabel examplesLabel = new JLabel("Examples");
 		leftPanel.add(examplesLabel, BorderLayout.NORTH);
 		
 		JPanel examplesTreePanel = new JPanel();
 		examplesTreePanel.setBorder(new EmptyBorder(10, 0, 0, 0));
 		leftPanel.add(examplesTreePanel, BorderLayout.CENTER);
 		examplesTreePanel.setLayout(new BorderLayout(0, 0));
 		
 		JScrollPane examplesScrollPane = new JScrollPane();
 		examplesTreePanel.add(examplesScrollPane, BorderLayout.CENTER);
 		
 		examplesTree = new JTree();
 		examplesScrollPane.setViewportView(examplesTree);
 		examplesTree.setShowsRootHandles(true);
 		examplesTree.setRootVisible(false);
 		examplesTree.setModel(new DefaultTreeModel(
 			new InfoMutableTreeNode("Languages",null,null,null,0) {
 				{
 					InfoMutableTreeNode node_1;
 					Class lang;
 					String langName;
 					String langInfo;
 					
 					lang = org.modelcc.examples.language.simplearithmeticexpression.Expression.class;
 					langName = "Simple Arithmetic Expression";
 					langInfo = "SimpleArithmeticExpression_";
 					node_1 = new InfoMutableTreeNode(langName,lang,langName,langInfo,0);
 						node_1.add(new InfoMutableTreeNode("Addition",lang,langName,langInfo,1));
 						node_1.add(new InfoMutableTreeNode("Addition and subtraction",lang,langName,langInfo,2));
 						node_1.add(new InfoMutableTreeNode("Priorities",lang,langName,langInfo,3));
 						node_1.add(new InfoMutableTreeNode("Parentheses",lang,langName,langInfo,4));
 					add(node_1);
 					
 					lang = org.modelcc.examples.language.canvasdraw.CanvasDraw.class;
 					langName = "CanvasDraw";
 					langInfo = "CanvasDraw_";
 					node_1 = new InfoMutableTreeNode(langName,lang,langName,langInfo,0);
 						node_1.add(new InfoMutableTreeNode("Blackboard",lang,langName,langInfo,1));
 						node_1.add(new InfoMutableTreeNode("Polygons",lang,langName,langInfo,2));
 					add(node_1);
 					
 					lang = org.modelcc.examples.language.imperativearithmetic.ImperativeArithmetic.class;
 					langName = "Imperative Arithmetic";
 					langInfo = "ImperativeArithmetic_";
 					node_1 = new InfoMutableTreeNode(langName,lang,langName,langInfo,0);
 						node_1.add(new InfoMutableTreeNode("Output",lang,langName,langInfo,1));
 						node_1.add(new InfoMutableTreeNode("Assignment and Output",lang,langName,langInfo,2));
 					add(node_1);
 					
 					lang = org.modelcc.examples.language.graphdraw3d.Scene.class;
 					langName = "Graph Draw 3D";
 					langInfo = "GraphDraw3D_";
 					node_1 = new InfoMutableTreeNode(langName,lang,langName,langInfo,0);
 						node_1.add(new InfoMutableTreeNode("Snail",lang,langName,langInfo,1));
 						node_1.add(new InfoMutableTreeNode("Helix",lang,langName,langInfo,2));
 						node_1.add(new InfoMutableTreeNode("Big Helix",lang,langName,langInfo,3));
 						node_1.add(new InfoMutableTreeNode("PalmTree",lang,langName,langInfo,4));
 						node_1.add(new InfoMutableTreeNode("ModelCC Logo",lang,langName,langInfo,5));
 						node_1.add(new InfoMutableTreeNode("Menger Sponge Slice",lang,langName,langInfo,6));
 						node_1.add(new InfoMutableTreeNode("Palette Cube",lang,langName,langInfo,7));
						node_1.add(new InfoMutableTreeNode("Snail Variation",lang,langName,langInfo,8)); //TODO add this
 					add(node_1);
 				}
 			}
 		));
 		examplesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
 		examplesTree.setBorder(new EmptyBorder(5, 5, 5, 0));
 		examplesTree.addTreeWillExpandListener(new TreeWillExpandListener() {
 		    public void treeWillExpand(TreeExpansionEvent e) { }
 		    public void treeWillCollapse(TreeExpansionEvent e)
 		         throws ExpandVetoException {
 		     throw new ExpandVetoException(e);
 		     }
 		    });
 		final JTree et = examplesTree;
 		examplesTree.addTreeSelectionListener(
 				new TreeSelectionListener() {
 			boolean avoid = false;
 		    public void valueChanged(TreeSelectionEvent e) {
 		        InfoMutableTreeNode node = (InfoMutableTreeNode)et.getLastSelectedPathComponent();
 
 		        if (node == null) return;
 		        if (avoid == true) return;
 
 		        InfoMutableTreeNode nodeInfo = (InfoMutableTreeNode)node;
 				if (!inputTextArea.getText().equals(originalText)) {
 					if (nodeInfo.getTextNumber()!=0) {
 						int result = JOptionPane.showConfirmDialog(null, "Input has been modified. Discard changes and load the example?", "Confirm", JOptionPane.YES_NO_CANCEL_OPTION);
 						if (result == JOptionPane.YES_OPTION) {
 					        switchLanguage(nodeInfo);
 					        loadExample(nodeInfo);
 						}
 						else if (result == JOptionPane.NO_OPTION) {
 							if (nodeInfo.getLanguageClass() == languageClass) {
 								avoid = true;
 								if (nodeInfo.getTextNumber()!=0)
 									((JTree)e.getSource()).setSelectionPath(e.getNewLeadSelectionPath().getParentPath());
 								avoid = false;
 							}
 							else {
 								switchLanguage(nodeInfo);
 								if (nodeInfo.getTextNumber()!=0)
 									((JTree)e.getSource()).setSelectionPath(e.getNewLeadSelectionPath().getParentPath());
 							}
 						}
 						else {
 							avoid = true;
 							((JTree)e.getSource()).setSelectionPath(e.getOldLeadSelectionPath());
 							avoid = false;
 						}
 					}
 					else {
 				        switchLanguage(nodeInfo);
 					}
 				}
 				else { 
 			        switchLanguage(nodeInfo);
 			        loadExample(nodeInfo);
 				}
 		    }
 
 		});
 		examplesTree.setFocusable(false);
 		
 		Component horizontalStrut = Box.createHorizontalStrut(235);
 		examplesTreePanel.add(horizontalStrut, BorderLayout.NORTH);
 		for (int i = 0; i < examplesTree.getRowCount(); i++) {
 			examplesTree.expandRow(i);
 		}
 
 		JPanel logoPanel = new JPanel();
 		logoPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
 		leftPanel.add(logoPanel, BorderLayout.SOUTH);
 		logoPanel.setLayout(new BorderLayout(0, 0));
 		
 		JLabel logoLabel = new JLabel("");
 		logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		logoPanel.add(logoLabel);
 		logoLabel.setIcon(new ImageIcon(ModelCCExamplesWindow.class.getResource("/org/modelcc/examples/test/logo.png")));
 		
 		JSplitPane splitPane = new JSplitPane();
 		splitPane.setContinuousLayout(true);
 		splitPane.setResizeWeight(0.5);
 		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
 		mainPanel.add(splitPane, BorderLayout.CENTER);
 		
 		JPanel inputPanel = new JPanel();
 		inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 		splitPane.setLeftComponent(inputPanel);
 		inputPanel.setLayout(new BorderLayout(0, 0));
 		
 		JPanel inputButtonPanel = new JPanel();
 		inputButtonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
 		inputPanel.add(inputButtonPanel, BorderLayout.SOUTH);
 		inputButtonPanel.setLayout(new BorderLayout(0, 0));
 		
 		JPanel altEnterPanel = new JPanel();
 		altEnterPanel.setBorder(new EmptyBorder(0, 0, 0, 15));
 		inputButtonPanel.add(altEnterPanel, BorderLayout.CENTER);
 		altEnterPanel.setLayout(new BorderLayout(0, 0));
 		
 		JLabel altEnterLabel = new JLabel("Press ALT+ENTER in order to");
 		altEnterPanel.add(altEnterLabel, BorderLayout.EAST);
 		
 		JLabel inputLabel = new JLabel("Input");
 		inputPanel.add(inputLabel, BorderLayout.NORTH);
 		
 		JPanel inputBorderPanel = new JPanel();
 		inputBorderPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
 		inputPanel.add(inputBorderPanel, BorderLayout.CENTER);
 		inputBorderPanel.setLayout(new BorderLayout(0, 0));
 		
 		JScrollPane inputScrollPane = new JScrollPane();
 		inputBorderPanel.add(inputScrollPane);
 		inputScrollPane.setViewportBorder(new MatteBorder(5, 5, 5, 5, (Color) new Color(255, 255, 255)));
 		
 		inputTextArea = new JTextArea();
 		inputTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
 		inputScrollPane.setViewportView(inputTextArea);
 		inputTextArea.addKeyListener(keyListener);
 		inputTextArea.getDocument().addDocumentListener(changeListener);
 		inputTextArea.getDocument().addUndoableEditListener(
 		        new UndoableEditListener() {
 		          public void undoableEditHappened(UndoableEditEvent e) {
 		            undoManager.addEdit(e.getEdit());
 		          }
 		        });
 		
 		JPanel outputPanel = new JPanel();
 		outputPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
 		splitPane.setRightComponent(outputPanel);
 		outputPanel.setLayout(new BorderLayout(0, 0));
 		
 		JLabel outputLabel = new JLabel("Output");
 		outputPanel.add(outputLabel, BorderLayout.NORTH);
 		
 		JPanel outputBorderPanel = new JPanel();
 		outputBorderPanel.setBorder(new EmptyBorder(10, 0, 0, 0));
 		outputPanel.add(outputBorderPanel, BorderLayout.CENTER);
 		outputBorderPanel.setLayout(new BorderLayout(0, 0));
 		
 		JScrollPane outputScrollPane = new JScrollPane();
 		outputBorderPanel.add(outputScrollPane);
 		outputScrollPane.setViewportBorder(new MatteBorder(5, 5, 5, 5, (Color) new Color(255, 255, 255)));
 		
 
 		JButton processButton = new JButton("Process");
 		inputButtonPanel.add(processButton, BorderLayout.EAST);
 		processButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				process();
 				inputTextArea.requestFocus();
 			}
 		});
 		processButton.setFocusable(false);
 		
 		outputTextArea = new JTextArea();
 		outputTextArea.setEditable(false);
 		outputTextArea.setLineWrap(true);
 		outputTextArea.setWrapStyleWord(true);
 		outputTextArea.addKeyListener(keyListener);
 
 		outputScrollPane.setViewportView(outputTextArea);
 		
 		outputTextArea.append("Welcome to ModelCC Examples.\n");
 		outputTextArea.append("Please choose a language or an example from the left menu.\n");
 		outputTextArea.append("\n");
 		outputTextArea.setFocusable(true);
 		inputTextArea.requestFocus();
 	}
 
 	JTextArea inputTextArea;
 	JTextArea outputTextArea;
 	
 	private void switchLanguage(InfoMutableTreeNode node) {
 		String languageName = node.getLanguageName();
 		Class languageClass = node.getLanguageClass();
 		String languageInfo = node.getLanguageInfo();
 		int textNumber = node.getTextNumber();
 		if (!languageClass.equals(this.languageClass)) {
 			outputTextArea.setText("");
 			outputTextArea.append(readText("text/"+languageInfo+"Description.txt"));
 			outputTextArea.setCaretPosition(0);
 			outputTextArea.append("\n");
 			outputTextArea.append("\n");
 			outputTextArea.append("Generating parser for the "+languageName+" language.\n\n");
 			this.languageClass = languageClass;
 	        parser = null;
 	        try {
 	            ModelReader jmr = new JavaModelReader(languageClass);
 	            Model m = jmr.read();
 	            Set<PatternRecognizer> ignore = new HashSet<PatternRecognizer>();
 	            ignore.add(new RegExpPatternRecognizer("[\r \n\t]+"));
 	            ignore.add(new RegExpPatternRecognizer("%[^\n]*(\n|$)"));
 	            parser = ParserFactory.create(m,ignore);
 	        } catch (Exception ex) {
 	        	outputTextArea.append("CRITICAL ERROR: "+ex.getMessage()+"\n");
 	        } 
 	        outputTextArea.append("Parser generated.\n");
 		}
 	}
 	
 	JFrame cdFrame;
 	Graph3DThread graph3dThread;
 	
 	class Graph3DThread extends Thread {
 		org.modelcc.examples.language.graphdraw3d.resources.DisplayWrapper dw;
 		boolean running;
 		
 	    public Graph3DThread(org.modelcc.examples.language.graphdraw3d.resources.DisplayWrapper dw) {
 	    	this.dw = dw;
 	    	running = true;
 	    }
 	    
 	    public void run() {
 	    	while (running) {
 	    		dw.run();
 		    	if (!dw.isRunning())
 		    		running = false;
 	    	}
 	    	dw.stop();
 	    }
 	    	    
 	    public boolean isRunning() {
 	    	return running;
 	    }
 	    
 	    public void setScene(org.modelcc.examples.language.graphdraw3d.Scene sc) {
 	    	this.dw.setScene(sc);
 	    }
 	    
 	}
 	
 	protected void process() {
         String inp = inputTextArea.getText();
         if (languageClass==null) {
         	outputTextArea.append("No language selected.\n");
         	return;
         }
 		
 		
         if (languageClass.equals(org.modelcc.examples.language.simplearithmeticexpression.Expression.class)) {
         	try {
         		Collection<org.modelcc.examples.language.simplearithmeticexpression.Expression> exps = parser.parseAll(inp);
     			outputTextArea.append("\n");
     			outputTextArea.append("Found "+exps.size()+" parse trees.\n");
     			if (exps.size()>0)
     				outputTextArea.append("Expression value: "+exps.iterator().next().eval()+"\n");
         	} catch (ParserException e) {
        		//TODO parserexception show
         	}
         }
         if (languageClass.equals(org.modelcc.examples.language.canvasdraw.CanvasDraw.class)) {
         	try {
 	        	Collection<org.modelcc.examples.language.canvasdraw.CanvasDraw> canvases = parser.parseAll(inp);
 				outputTextArea.append("\n");
 				outputTextArea.append("Found "+canvases.size()+" parse trees.\n");
 				if (canvases.size()>0) {
 					outputTextArea.append("Opening canvas window.\n");
 					org.modelcc.examples.language.canvasdraw.CanvasDraw cd = canvases.iterator().next();
 					if (cdFrame != null) {
 						cdFrame.setVisible(false);
 						cdFrame.dispose();
 						cdFrame = null;
 					}
 	                cdFrame = new JFrame("CanvasDraw");
 	                cdFrame.setResizable(false);
 	                cdFrame.setSize(cd.getSize());
 	                Container pane = cdFrame.getContentPane();
 	                pane.add(cd, BorderLayout.CENTER);
 	                cd.setVisible(true);
 	                cdFrame.setVisible(true);
 				}
 	    	} catch (ParserException e) {
	    		//TODO parserexception show
 	    	}
         }
         if (languageClass.equals(org.modelcc.examples.language.imperativearithmetic.ImperativeArithmetic.class)) {
         	try {
 	        	Collection<org.modelcc.examples.language.imperativearithmetic.ImperativeArithmetic> imps = parser.parseAll(inp);
 				outputTextArea.append("\n");
 				outputTextArea.append("Found "+imps.size()+" parse trees.\n");
 	        	if (imps.size()>0) {
 			    	org.modelcc.examples.language.imperativearithmetic.ImperativeArithmetic imp = imps.iterator().next();
 		        	outputTextArea.append("Running program...\n");
 		        	outputTextArea.append(imp.run());
 	        	}
         	} catch (ParserException e) {
        		//TODO parserexception show
         	}
         }
         if (languageClass.equals(org.modelcc.examples.language.graphdraw3d.Scene.class)) {
         	try {
 	        	Collection<org.modelcc.examples.language.graphdraw3d.Scene> scenes = parser.parseAll(inp);
 				outputTextArea.append("\n");
 				outputTextArea.append("Found "+scenes.size()+" parse trees.\n");
 				if (scenes.size()>0) {
 					outputTextArea.append("Opening 3D draw window.\n");
 					org.modelcc.examples.language.graphdraw3d.Scene scene = scenes.iterator().next();
 	                try {
 	                	org.modelcc.examples.language.graphdraw3d.resources.DisplayWrapper dw = new org.modelcc.examples.language.graphdraw3d.resources.DisplayWrapper(scene);
 	                	if (graph3dThread != null && graph3dThread.isRunning()) {
 	                		graph3dThread.setScene(dw.getScene());
 	                	}
 	                	else {
 		                	graph3dThread = new Graph3DThread(dw);
 		                    graph3dThread.start();
 	                	}
 	                } catch(Exception e) {
 	                    e.printStackTrace();
 	                }
 	            }
         	} catch (ParserException e) {
        		//TODO parserexception show
         	}
         }
         outputTextArea.setCaretPosition(outputTextArea.getText().length());
 	}
 
     private String readText(String resourceName) {
         InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("org/modelcc/examples/test/"+resourceName);
         if (is != null) {
             Writer writer = new StringWriter();
 
             char[] buffer = new char[1024];
             try {
                 Reader reader = new BufferedReader(
                         new InputStreamReader(is, "UTF-8"));
                 int n;
                 while ((n = reader.read(buffer)) != -1) {
                     writer.write(buffer, 0, n);
                 }
             } catch (Exception e) {
             } finally {
                 try {
                     is.close();
                 } catch (Exception ex) {
                 }
             }
             return writer.toString();
         } else {        
             return "";
         }
     }
     
     private String originalText = "";
     
     private void loadExample(InfoMutableTreeNode node) {
 		String languageInfo = node.getLanguageInfo();
 		int textNumber = node.getTextNumber();
 		changeListener.setAutoChange(true);
 		if (textNumber != 0) {
 			inputTextArea.setText(readText("text/"+languageInfo+"Example"+textNumber+".txt"));
 			undoManager.discardAllEdits();
 		}
 		else {
 			inputTextArea.setText("");
 			undoManager.discardAllEdits();
 		}
 		inputTextArea.setCaretPosition(0);
 		originalText = inputTextArea.getText();
 		changeListener.setAutoChange(false);
     }
 
 }
 
 // [+] ajustar lenguaje graph3d al paper (next)
 // [+++] Corregir lenguaje, quitar autorun de objectname en graph3d
 // [+] Permitir infinitos scopes deshaciendo los cambios en graph3d.
 // ejemplos graphdraw3d
 
