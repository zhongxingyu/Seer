 package org.pathwayeditor.curationtool;
 
 import java.awt.BorderLayout;
 import java.awt.Dialog;
 import java.awt.Dimension;
import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.regex.Pattern;
 
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JSplitPane;
 import javax.swing.KeyStroke;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.filechooser.FileFilter;
 
 import org.pathwayeditor.businessobjects.drawingprimitives.ICanvas;
 import org.pathwayeditor.businessobjects.exchange.FileXmlCanvasPersistenceManager;
 import org.pathwayeditor.businessobjects.exchange.IXmlPersistenceManager;
 import org.pathwayeditor.businessobjects.management.INotationSubsystemPool;
 import org.pathwayeditor.curationtool.sentences.ISentenceSelectionChangeEvent;
 import org.pathwayeditor.curationtool.sentences.ISentenceSelectionChangeListener;
 import org.pathwayeditor.curationtool.sentences.SentenceFromSInterationIterator;
 import org.pathwayeditor.curationtool.sentences.SentencesPanel;
 import org.pathwayeditor.notations.annotator.ndom.IMapDiagram;
 import org.pathwayeditor.notations.annotator.ndom.ISentence;
 import org.pathwayeditor.notations.annotator.ndom.parser.BoParser;
 import org.pathwayeditor.notations.annotator.ndom.parser.BoTreeLexer;
 import org.pathwayeditor.notations.annotator.ndom.parser.NdomBuilder;
 import org.pathwayeditor.notations.annotator.ndom.parser.TreeParseException;
 import org.pathwayeditor.visualeditor.NotationSubsystemPool;
 import org.pathwayeditor.visualeditor.PathwayEditor;
 
 public class CurationToolUI extends JFrame {
 	private static final long serialVersionUID = 1L;
 
 	private static final int WIDTH = 1200;
 	private static final int HEIGHT = 800;
 
 	private static final String APP_NAME = "BMCCurator";
 	private JMenuBar menuBar;
 	private SentencesPanel sentencesPanel;
 	private File currentFile = null;
 
 	private PathwayEditor insp;
 
 	private IXmlPersistenceManager canvasPersistenceManager;
 	
 	public CurationToolUI(String title){
 		super(title);
 		this.setLayout(new BorderLayout());
 		this.menuBar = new JMenuBar();
 		initFileMenu();
 		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		WindowListener windowsListener = new WindowListener(){
 
 			@Override
 			public void windowActivated(WindowEvent e) {
 				
 			}
 
 			@Override
 			public void windowClosed(WindowEvent e) {
 			}
 
 			@Override
 			public void windowClosing(WindowEvent e) {
 				int reply = JOptionPane.showConfirmDialog(CurationToolUI.this, "Are you sure you want to exit?", "Exit Confirmation", JOptionPane.YES_NO_OPTION);
 				if(reply == JOptionPane.YES_OPTION){
 					System.exit(0);
 				}
 			}
 
 			@Override
 			public void windowDeactivated(WindowEvent e) {
 				
 			}
 
 			@Override
 			public void windowDeiconified(WindowEvent e) {
 				
 			}
 
 			@Override
 			public void windowIconified(WindowEvent e) {
 				
 			}
 
 			@Override
 			public void windowOpened(WindowEvent e) {
 				
 			}
 			
 		};
 		this.addWindowListener(windowsListener);
 		this.setJMenuBar(menuBar);
 		Dialog dialog = new Dialog(this, "Synonym Dialog");
 		dialog.setAlwaysOnTop(true);
 		dialog.addWindowListener(windowsListener);
 		this.sentencesPanel = new SentencesPanel(dialog);
 		this.sentencesPanel.setPreferredSize(new Dimension(WIDTH, HEIGHT/3));
 		this.insp = new PathwayEditor();
 		this.insp.setPreferredSize(new Dimension(WIDTH, 2*HEIGHT/3));
 		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.sentencesPanel, this.insp);
 		splitPane.setDividerLocation(0.33);
 		splitPane.setOneTouchExpandable(true);
 		this.add(splitPane);
 		this.pack();
 		this.setVisible(true);
 	}
 
 	private void initComponentLinkage() {
 		this.sentencesPanel.addSentenceSelectionChangeListener(new ISentenceSelectionChangeListener() {
 			
 			@Override
 			public void sentenceSelectionChangeEvent(ISentenceSelectionChangeEvent e) {
 				ISentence selectedSentence = e.getSelectedSentence();
 				insp.selectAndFocusOnElement(selectedSentence.getArc().getLinkEdge());
 			}
 		});
 	}
 
 	private void initFileMenu(){
 		JMenu fileMenu = new JMenu("File");
 		fileMenu.setMnemonic(KeyEvent.VK_F);
 		fileMenu.getAccessibleContext().setAccessibleDescription(
 		        "The only menu in this program that has menu items");
 		menuBar.add(fileMenu);
 		//a group of JMenuItems
 		JMenuItem fileMenuOpenItem = new JMenuItem("Open", KeyEvent.VK_O);
 		fileMenuOpenItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		fileMenuOpenItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
 		fileMenuOpenItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JFileChooser chooser = new JFileChooser();
 //				chooser.setCurrentDirectory(new File("/Users/smoodie/Documents/workspace351_64/GraphicsEngine/test/org/pathwayeditor/graphicsengine"));
 				chooser.setCurrentDirectory(new File("/Users/smoodie/Documents/workspace351_64/GraphicsEngine"));
 				chooser.setFileFilter(new FileFilter(){
 					
 					@Override
 					public boolean accept(File f) {
 						String fileName = f.getName();
 						return Pattern.matches(".*\\.pwe$", fileName);
 					}
 
 					@Override
 					public String getDescription() {
 						return "Pathway Editor files";
 					}
 					
 				});
 				int response = chooser.showOpenDialog(CurationToolUI.this);
 				if(response == JFileChooser.APPROVE_OPTION){
 					currentFile = chooser.getSelectedFile();
 					openFile(currentFile);
 				}
 			}
 		});
 		fileMenu.add(fileMenuOpenItem);
 		JMenuItem fileMenuSaveItem = new JMenuItem("Save", KeyEvent.VK_S);
 		fileMenuSaveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		fileMenuSaveItem.getAccessibleContext().setAccessibleDescription("Save");
 		fileMenuSaveItem.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(currentFile != null){
 					try {
 						saveFile(currentFile);
 					} catch (IOException e1) {
 						JOptionPane.showMessageDialog(CurationToolUI.this, "Error message: " + e1.getLocalizedMessage(), "Error saving file", JOptionPane.ERROR_MESSAGE);
 					}
 				}
 			}
 			
 		});
 		fileMenu.add(fileMenuSaveItem);
 		JMenuItem fileMenuExitItem = new JMenuItem("Exit", KeyEvent.VK_X);
 		fileMenuExitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
 		fileMenuExitItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
 		fileMenuExitItem.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				processEvent(new WindowEvent(CurationToolUI.this, WindowEvent.WINDOW_CLOSING));
 			}
 		});
 		fileMenu.add(fileMenuExitItem);
 	}
 	
 
 	private void saveFile(File currentFile2) throws IOException {
 		InputStream in = canvasPersistenceManager.writeCanvasToStream();
 		OutputStream out = new FileOutputStream(currentFile2);
 		byte buf[] = new byte[1024*1024];
 		int c = -1;
 		while((c = in.read(buf)) != -1){
 			out.write(buf, 0, c);
 		}
 		in.close();
 		out.close();
 	}
 
 	public void openFile(File file){
 		try{
 			INotationSubsystemPool subsystemPool = new NotationSubsystemPool();
 			canvasPersistenceManager = new FileXmlCanvasPersistenceManager(subsystemPool);
 			InputStream in = new FileInputStream(file);
 			canvasPersistenceManager.readCanvasFromStream(in);
 			in.close();
 			ICanvas canvas = canvasPersistenceManager.getCurrentCanvas();
 			NdomBuilder builder = new NdomBuilder();
 			BoParser parser = new BoParser(builder);
 			BoTreeLexer lexer = new BoTreeLexer(canvas);
 			parser.parse(lexer);
 			IMapDiagram ndom = builder.getNdom();
 			sentencesPanel.loadData(new SentenceFromSInterationIterator(ndom.sInteractionIterator()));
 			insp.loadCanvas(canvas);
 			this.validate();
 			initComponentLinkage();
 			insp.selectAndFocusOnElement(sentencesPanel.getSelectedSentence().getArc().getLinkEdge());
 		}
 		catch(IOException ex){
 			JOptionPane.showMessageDialog(this, "Error message: " + ex.getLocalizedMessage(), "Error opening file", JOptionPane.ERROR_MESSAGE);
 		} catch (TreeParseException e) {
 			JOptionPane.showMessageDialog(this, "Error message: " + e.getLocalizedMessage(), "Bug detected loading file", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	
 	public static final void main(String argv[]) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
 		System.setProperty("apple.laf.useScreenMenuBar", "true");
 		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		CurationToolUI visualEditor = new CurationToolUI(APP_NAME);
 	}
 }
