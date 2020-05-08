 /*
  * EditorPanelExpression.java
  *
  * Created on 17. September 2006, 14:59
  */
 
 package de.unisiegen.tpml.ui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JToggleButton;
 import javax.swing.filechooser.FileFilter;
 import org.apache.log4j.Logger;
 import de.unisiegen.tpml.core.bigstep.BigStepProofModel;
 import de.unisiegen.tpml.core.expressions.Expression;
 import de.unisiegen.tpml.core.languages.Language;
 import de.unisiegen.tpml.core.languages.LanguageFactory;
 import de.unisiegen.tpml.core.languages.NoSuchLanguageException;
 import de.unisiegen.tpml.core.minimaltyping.MinimalTypingProofModel;
 import de.unisiegen.tpml.core.smallstep.SmallStepProofModel;
 import de.unisiegen.tpml.core.typechecker.TypeCheckerProofModel;
 import de.unisiegen.tpml.core.typeinference.TypeInferenceProofModel;
 import de.unisiegen.tpml.core.util.beans.AbstractBean;
 import de.unisiegen.tpml.graphics.AbstractProofComponent;
 import de.unisiegen.tpml.graphics.EditorComponent;
 import de.unisiegen.tpml.graphics.ProofViewFactory;
 import de.unisiegen.tpml.graphics.editor.TextEditorPanel;
 import de.unisiegen.tpml.ui.netbeans.EditorPanelForm;
 import de.unisiegen.tpml.ui.proofview.ProofViewComponent;
 
 /**
  * Part of the UI displayed in the tabbed pane. It includes one open file and
  * all {@link de.unisiegen.tpml.graphics.EditorComponent}s open for that file.
  * 
  * @author Christoph Fehling
  */
   @SuppressWarnings("all")public class EditorPanelExpression extends AbstractBean implements EditorPanel{
 	private static final Logger logger = Logger.getLogger(EditorPanelExpression.class);
 
 	private MainWindow window;
 
 	private EditorPanelForm mypanel;
 	/**
 	 * 
 	 */
 	private TextEditorPanel code;
 
 	private EditorComponent smallstep;
 
 	private EditorComponent bigstep;
 
 	private EditorComponent typechecker;
 	
 	private EditorComponent typeinference;
 	
 	private EditorComponent minimaltyping;
 
 	private EditorComponent activeEditorComponent;
 
 	private PropertyChangeListener editorComponentListener;
 
 	/**
 	 * Filename displayed in the tab.
 	 */
 	private String filename;
 
 	/**
 	 * static number counting the new files with default name.
 	 */
 	static private int num = 0;
 	
 	private boolean advanced;
 
 	/**
 	 * The language used in this Editor.
 	 */
 	private Language language;
 
 	/**
 	 * The file to which this document is saved.
 	 */
 	private File file;
 
 	/**
 	 * Indicated if the file was changed.
 	 */
 	//private boolean changed;
 
 	/**
 	 * Indicates the status of the Undo function.
 	 */
 	private boolean undoStatus;
 
 	/**
 	 * Indicates the status of the Redo function.
 	 */
 	private boolean redoStatus;
 
 	/**
 	 * Indicated if the displayed component is a text editor.
 	 * 
 	 */
 	private boolean texteditor;
 	/**
 	 * The serial version UID
 	 * 
 	 */
 	private static final long serialVersionUID = -272175525193942130L;
 
 	/** Creates new form EditorPanelExpression */
 	public EditorPanelExpression(Language language, MainWindow window) {
 		//initComponents();
 	    	mypanel = new EditorPanelForm(this);
 
 		this.window = window;
 		// setting the default button states
 		mypanel.nextButton.setVisible ( false );
 		mypanel.pongButton.setVisible ( false );
 		mypanel.smallstepButton.setVisible(false);
 		mypanel.bigstepButton.setVisible(false);
 		mypanel.typecheckerButton.setVisible(false);
 		mypanel.typeinferenceButton.setVisible(false);
 		mypanel.minimalTypingButton.setVisible(false);
 		mypanel.subTypingButton.setVisible ( false );
 		mypanel.subTypingRecButton.setVisible ( false );
 		//finished setting the default states
 
                 // hack to get consistent heights
 		mypanel.codeButton.setPreferredSize(new Dimension(mypanel.codeButton.getPreferredSize().width, mypanel.pongButton.getPreferredSize().height));
 		mypanel.smallstepButton.setPreferredSize(new Dimension(mypanel.smallstepButton.getPreferredSize().width, mypanel.pongButton.getPreferredSize().height));
 		mypanel.bigstepButton.setPreferredSize(new Dimension(mypanel.bigstepButton.getPreferredSize().width, mypanel.pongButton.getPreferredSize().height));
 		mypanel.typecheckerButton.setPreferredSize(new Dimension(mypanel.typecheckerButton.getPreferredSize().width, mypanel.pongButton.getPreferredSize().height));
 		mypanel.typeinferenceButton.setPreferredSize(new Dimension(mypanel.typeinferenceButton.getPreferredSize().width, mypanel.pongButton.getPreferredSize().height));
 		mypanel.minimalTypingButton.setPreferredSize(new Dimension(mypanel.minimalTypingButton.getPreferredSize().width, mypanel.pongButton.getPreferredSize().height));
                 //There will be no SubTypingButton
                 //TODO vielleicht auch machen müssen
 		
 		this.language = language;
 		
 		//TODO PREFERENCES get this from the preferences
 		setAdvanced(false);
 		
 		setFileName("newfile" + num + "." + language.getName());
 		num++;
 		editorComponentListener = new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
 				componentStatusChanged(evt.getPropertyName(), evt.getNewValue());
 			}
 		};
 		initEditor();
 
 
 		mypanel.addComponentListener(new java.awt.event.ComponentAdapter() {
 			public void componentShown(java.awt.event.ComponentEvent evt) {
 				code.getEditor().requestFocus();
 			}
 		});
 	}
 
 
 
 
 
 	/**
 	 * Is called when a status of the displayed component in changed
 	 * 
 	 * @param ident
 	 *            name of the changed status
 	 * @param newValue
 	 *            new value of the status
 	 */
 	private void componentStatusChanged(String ident, Object newValue) {
 		if (ident.equals("nextStatus")) {
 		    mypanel.nextButton.setEnabled((Boolean) newValue);
 		} else if (ident.equals("pongStatus")) {
 		    mypanel.pongButton.setVisible((Boolean) newValue);
 		} else if (ident.equals("redoStatus")) {
 			setRedoStatus((Boolean) newValue);
 		} else if (ident.equals("title")) {
 			setFileName((String) newValue);
 		} else if (ident.equals("undoStatus")) {
 			setUndoStatus((Boolean) newValue);
 		} else if (ident.equals("changed")) {
 			//setChanged((Boolean) newValue);
 			setUndoStatus((Boolean) newValue);
 		}
 
 	}
 
 	private void updateComponentStates(EditorComponent comp) {
 		setRedoStatus(comp.isRedoStatus());
 		setUndoStatus(comp.isUndoStatus());
 		mypanel.nextButton.setEnabled(comp.isNextStatus());
 		mypanel.nextButton.setVisible(comp != this.code);
 		mypanel.pongButton.setVisible(comp.isPongStatus());
 	}
 
 	/**
 	 * Sets the Component shown in the Editor Panel.
 	 * 
 	 * @param comp
 	 */
 	private void setComponent(EditorComponent comp) {
 		
 	    mypanel.editorPanel.removeAll();
 	    mypanel.editorPanel.add((JComponent) comp, BorderLayout.CENTER);
 		activeEditorComponent = comp;
 		updateComponentStates(comp);
 		mypanel.paintAll(mypanel.getGraphics());
 	}
 	
 	private EditorComponent getComponent() {
 		return (EditorComponent) mypanel.editorPanel.getComponent(0);
 	}
 
 	/**
 	 * This method is called from within the constructor to initialize the
 	 * source editor.
 	 */
 	private void initEditor() {
 		code = new TextEditorPanel(language);
 
 		mypanel.editorPanel.removeAll();
 		mypanel.editorPanel.add((JPanel) code, BorderLayout.CENTER);
 		((JPanel) code).addPropertyChangeListener(editorComponentListener);
 		code.setDefaultStates();
 		updateComponentStates(code);
 		deselectButtons();
 		mypanel.codeButton.setSelected(true);
 		mypanel.codeButton.setEnabled(true);
 
 		mypanel.paintAll(mypanel.getGraphics());
 	}
 
 	/**
 	 * Starts the Small Step Interpreter.
 	 * 
 	 */
 	public void handleSmallStep() {
 		setTexteditor(false);
 		try {
 			SmallStepProofModel model = language.newSmallStepProofModel(code
 					.getDocument().getExpression());
 			smallstep = new ProofViewComponent(ProofViewFactory
 					.newSmallStepView(model), model);
 			mypanel.editorPanel.removeAll();
 			activateFunction(mypanel.smallstepButton, smallstep);
 			smallstep.setAdvanced(this.advanced);
 			mypanel.paintAll(mypanel.getGraphics());
 			
 
 		} catch (Exception e) {
 			JOptionPane.showMessageDialog(mypanel,
 					java.util.ResourceBundle.getBundle("de/unisiegen/tpml/ui/ui").getString("CouldNotSmallStep")+
 					"\n"+e.getMessage()+".",
 					"Small Step", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	/**
 	 * Starts the Big Step Interpreter.
 	 */
 	public void handleBigStep() {
 		setTexteditor(false);
 		try {
 			BigStepProofModel model = language.newBigStepProofModel(code
 					.getDocument().getExpression());
 			bigstep = new ProofViewComponent(ProofViewFactory
 					.newBigStepView(model), model);
 			mypanel.editorPanel.removeAll();
 			activateFunction(mypanel.bigstepButton, bigstep);
 			bigstep.setAdvanced(this.advanced);
 			mypanel.paintAll(mypanel.getGraphics());
 			
 
 		} catch (Exception e) {
 			logger.debug("Could not create new BigStepView", e);
 			JOptionPane.showMessageDialog(mypanel,
 					java.util.ResourceBundle.getBundle("de/unisiegen/tpml/ui/ui").getString("CouldNotBigStep")+
 					"\n"+e.getMessage()+".", "Big Step",
 					JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	/**
 	 * Starts the Type Checker.
 	 */
 	public void handleTypeChecker() {
 		setTexteditor(false);
 		try {
 			TypeCheckerProofModel model = language
 					.newTypeCheckerProofModel(code.getDocument()
 							.getExpression());
 			typechecker = new ProofViewComponent(ProofViewFactory
 					.newTypeCheckerView(model), model);
 			mypanel.editorPanel.removeAll();
 			activateFunction(mypanel.typecheckerButton, typechecker);
 			typechecker.setAdvanced(this.advanced);
 			mypanel.paintAll(mypanel.getGraphics());
 			
 
 		} catch (Exception e) {
 			logger.debug("Could not create new TypeCheckerView", e);
 			JOptionPane.showMessageDialog(mypanel,
 					java.util.ResourceBundle.getBundle("de/unisiegen/tpml/ui/ui").getString("CouldNotTypeChecker")+
 					"\n"+e.getMessage()+".",
 					"Type Checker", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	/**
 	 * Starts the TypeInference.
 	 */
 	public void handleTypInference() {
 		setTexteditor(false);
 		
 		try {
 			TypeInferenceProofModel model = language.newTypeInferenceProofModel(code.getDocument()
 							.getExpression());
 			//typechecker = new ProofViewComponent(ProofViewFactory
 			//		.newTypeCheckerView(model), model);
 			
 			typeinference = new ProofViewComponent(ProofViewFactory.newTypeInferenceView(model), model);
 			mypanel.editorPanel.removeAll();
 			//activateFunction(typecheckerButton, typechecker);
 			activateFunction(mypanel.typeinferenceButton, typeinference);
 			typeinference.setAdvanced(this.advanced);
 			mypanel.paintAll(mypanel.getGraphics());
 			
 
 		} catch (Exception e) {
 			logger.debug("Could not create new TypeInferenceView", e);
 			JOptionPane.showMessageDialog(mypanel,
 					java.util.ResourceBundle.getBundle("de/unisiegen/tpml/ui/ui").getString("CouldNotTypeInference")+
 					"\n"+e.getMessage()+".",
 					"Type Inference", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 	
 	
 	/**
 	 * Starts the MinimalTyping Interpreter.
 	 */
 	public void handleMinimalTyping() {
 		setTexteditor(false);
 		try {
 			MinimalTypingProofModel model = language.newMinimalTypingProofModel(code
 					.getDocument().getExpression(), isAdvaced ( ));
 			minimaltyping = new ProofViewComponent(ProofViewFactory
 					.newMinimalTypingView(model), model);
 			mypanel.editorPanel.removeAll();
 			activateFunction(mypanel.minimalTypingButton, minimaltyping);
 			minimaltyping.setAdvanced(this.advanced);
 			mypanel.paintAll(mypanel.getGraphics());
 			
 
 		} catch (Exception e) {
 			logger.debug("Could not create new MinimalTypingView", e);
 			JOptionPane.showMessageDialog(mypanel,
 					java.util.ResourceBundle.getBundle("de/unisiegen/tpml/ui/ui").getString("CouldNotMinimalType")+
 					"\n"+e.getMessage()+".", "Minimal Typing",
 					JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	public void handleCut() {
 		this.code.handleCut();
 	}
 
 	public void handleCopy() {
 		this.code.handleCopy();
 	}
 
 	public void handlePaste() {
 		this.code.handlePaste();
 	}
 
 	/**
 	 * activates one of the following: smallstep, bigstep, typechecker etc.
 	 * buttons and special component functions.
 	 * 
 	 * @param button
 	 *            the button to be activated
 	 * @param comp
 	 *            the component related to that button
 	 */
 	private void activateFunction(JToggleButton button, EditorComponent comp) {
 		comp.setDefaultStates();
 		((JComponent) comp).addPropertyChangeListener(editorComponentListener);
 		setComponent(comp);
 		deselectButtons();
 		if (button != null)
 		{
 			button.setSelected(true);
 			button.setVisible(true);
 			mypanel.nextButton.setVisible(true);
 			
 		}
 		
 	}
 
 	/**
 	 * Sets the select states of the code, smallstep, bigstep and typechecker
 	 * buttons to false.
 	 * 
 	 */
 	private void deselectButtons() {
 	    mypanel.codeButton.setSelected(false);
 	    mypanel.smallstepButton.setSelected(false);
 	    mypanel.bigstepButton.setSelected(false);
 	    mypanel.typecheckerButton.setSelected(false);
 	    mypanel.typeinferenceButton.setSelected(false);
 	    mypanel.minimalTypingButton.setSelected ( false );
 	}
 
 	/**
 	 * Returns the redo status
 	 * 
 	 * @return true if redo is available
 	 */
 	public boolean isRedoStatus() {
 		return redoStatus;
 	}
 
 	/**
 	 * Sets the redo status.
 	 * 
 	 * @param redoStatus
 	 *            redo status to be set.
 	 */
 	public void setRedoStatus(boolean redoStatus) {
 		boolean oldRedoStatus = this.redoStatus;
 		this.redoStatus = redoStatus;
 		firePropertyChange("redoStatus", oldRedoStatus, redoStatus);
 	}
 
 	/**
 	 * Returns the file name.
 	 * 
 	 * @return the file name.
 	 */
 	public String getFileName() {
 		return filename;
 	}
 
 	/**
 	 * Sets the file name.
 	 * 
 	 * @param filename
 	 *            the file name to be set.
 	 * 
 	 * @exception NullPointerException
 	 *                if <code>filename</code> is <code>null</code>
 	 */
 	public void setFileName(String filename) {
 		if (filename == null)
 			throw new NullPointerException("filename is null");
 		String oldFilename = this.filename;
 		this.filename = filename;
 		firePropertyChange("filename", oldFilename, filename);
 	}
 
 	/**
 	 * Returns the file name.
 	 * 
 	 * @return the file name.
 	 */
 	public File getFile() {
 		return file;
 	}
 
 	/**
 	 * Sets the <code>File</code> for this editor.
 	 * 
 	 * @param file
 	 *            the <code>File</code> to be set.
 	 * 
 	 * @throws NullPointerException
 	 *             if the <code>File</code> is <code>null</code>.
 	 */
 	public void setFile(File file) {
 		if (file == null)
 			throw new NullPointerException("File is null");
 		//if (this.file != null) window.removeRecentlyUsed(this.file);
 		this.file = file;
 		window.addRecentlyUsed(new HistoryItem (this.file));
 		setFileName(file.getName());
 		
 	}
 
 	/**
 	 * Returns the language used in this editor.
 	 * 
 	 * @return the language used.
 	 */
 	public Language getLanguage() {
 		return language;
 	}
 	
 	public EditorComponent getActiveEditorComponent(){
 	    return activeEditorComponent;
 	}
 
 //	/**
 //	 *  add documentation here
 //	 * 
 //	 * @return <code>true</code> if the editor's document was changed.
 //	 */
 ////	public boolean isChanged() {
 ////		return this.changed;
 ////	}
 
 	/**
 	 * Sets the change status of the editor
 	 * 
 	 * @param changed
 	 *            true if the editor's document was changed.
 	 */
 //	public void setChanged(boolean changed) {
 //		firePropertyChange("changed", this.changed, changed);
 //		this.changed = changed;
 //	}
 
 	public boolean isTexteditor() {
 		return this.texteditor;
 	}
 
 	public void setTexteditor(boolean texteditor) {
 	   firePropertyChange("texteditor", this.texteditor, texteditor);
 		logger.debug("Texteditor is active");
 		this.texteditor = texteditor;
 	}
 
 	public String getEditorText() {
 		return code.getText();
 	}
 
 	public void setEditorText(String text) {
 		code.setText(text);
 	}
 
 	/**
 	 * Sets the undo status
 	 * 
 	 * @return true if the undo function is available
 	 */
 	public boolean isUndoStatus() {
 		return undoStatus;
 	}
 	
 	public boolean isSaveStatus() {
 		return undoStatus;
 	}
 
 	public void setUndoStatus(boolean undoStatus) {
 		if (this.undoStatus != undoStatus) {
 			logger.debug("UndoStatus of EditorPanelExpression set to "+undoStatus);
 			boolean oldUndoStatus = this.undoStatus;
 			this.undoStatus = undoStatus;
 			firePropertyChange("undoStatus", oldUndoStatus, undoStatus);
 			if (this.isTexteditor()) firePropertyChange("changed", oldUndoStatus, undoStatus);
 		}
 	}
 	
 	public void setAdvanced(boolean state){
 		if (bigstep != null) bigstep.setAdvanced(state);
 		if (smallstep != null) smallstep.setAdvanced(state);
 		if (typechecker != null) typechecker.setAdvanced(state);
 		if (typeinference != null) typeinference.setAdvanced(state);
 		if (minimaltyping != null) minimaltyping.setAdvanced(state);
 		this.advanced = state;
 	}
 	
 	public boolean isAdvaced (){
 		return this.advanced;
 	}
 	
 	public boolean shouldBeSaved(){
 		return code.isUndoStatus();
 	}
 
 	public void handleUndo() {
 		getComponent().handleUndo();
 	};
 
 	public void handleRedo() {
 		getComponent().handleRedo();
 	};
 
 	public boolean handleSave() {
 		if (file == null)
 			return handleSaveAs();
 		else
 			return writeFile();
 	};
 
 	/**
 	 * Saves the active editor component.
 	 * 
 	 * @return true if the file could be saved.
 	 */
 	public boolean handleSaveAs() {
 		// setup the file chooser
 		final LanguageFactory factory = LanguageFactory.newInstance();
 		PreferenceManager prefmanager = PreferenceManager.get();
 		JFileChooser chooser = new JFileChooser(prefmanager.getWorkingPath());
 		chooser.addChoosableFileFilter(new FileFilter() {
 			@Override
 			public boolean accept(File f) {
 				if (f.isDirectory()) {
 					return true;
 				}
 				try {
 					factory.getLanguageByFile(f);
 					return true;
 				} catch (NoSuchLanguageException e) {
 					return false;
 				}
 			}
 
 			@Override
 			public String getDescription() {
 				Language[] languages = factory.getAvailableLanguages();
 				StringBuilder builder = new StringBuilder(128);
 				builder.append("Source Files (");
 				for (int n = 0; n < languages.length; ++n) {
 					if (n > 0) {
 						builder.append("; ");
 					}
 					builder.append("*.");
 					builder.append(languages[n].getName().toLowerCase());
 				}
 				builder.append(')');
 				return builder.toString();
 			}
 		});
 		chooser.setAcceptAllFileFilterUsed(false);
 		prefmanager.setWorkingPath(chooser.getCurrentDirectory().getAbsolutePath());
 		
 		// determine the file name
 		File outfile;
 		for (;;) {
 			// run the dialog
 			int n = chooser.showSaveDialog(mypanel.getParent());
 			
 			if (n != JFileChooser.APPROVE_OPTION) {
 				logger.debug("Save as dialog cancelled");
 				return false;
 			}
 
 			// check the extension
 			File f = chooser.getSelectedFile();
 			String name = f.getName();
 			int i = name.lastIndexOf('.');
 			if (i > 0 && i < name.length()) {
 				if (!name.substring(i + 1).equalsIgnoreCase(
 						this.language.getName())) {
 					JOptionPane.showMessageDialog(mypanel,
 							java.util.ResourceBundle.getBundle("de/unisiegen/tpml/ui/ui").getString("FileMustEndWith")+" \"."
 									+ this.language.getName().toLowerCase()
 									+ "\".", java.util.ResourceBundle.getBundle("de/unisiegen/tpml/ui/ui").getString("Save"), JOptionPane.ERROR_MESSAGE);
 					continue;
 				}
 			} else {
 				name = name + "." + this.language.getName().toLowerCase();
 			}
 
 			// try to create the new file
 			try {
 				outfile = new File(f.getParent(), name);
 				if (!outfile.createNewFile()) {
 					// TODO: Christoph, this doesn't work propertly!
 					int j = JOptionPane
 							.showConfirmDialog(
 								mypanel,
 									java.util.ResourceBundle.getBundle("de/unisiegen/tpml/ui/ui").getString("The_File")+" \""
 											+ outfile.getName()
 											+ "\" " +java.util.ResourceBundle.getBundle("de/unisiegen/tpml/ui/ui").getString("alreadyExists"),
 									java.util.ResourceBundle.getBundle("de/unisiegen/tpml/ui/ui").getString("Overwrite"),
 									JOptionPane.YES_NO_CANCEL_OPTION,
 									JOptionPane.QUESTION_MESSAGE);
 					if (j == JFileChooser.CANCEL_OPTION) {
 						logger.debug("Cancelled overwrite of \""
 								+ outfile.getName() + "\"");
 						return false;
 					} else if (j == JOptionPane.NO_OPTION) {
 						// next try
 						continue;
 					}
 				}
 
 				// save to the new file
 				setFile(outfile);
 				setFileName(outfile.getName());
 				return writeFile();
 			} catch (IOException e) {
 				logger.error("Selected file could not be created.", e);
 				JOptionPane.showMessageDialog(mypanel,
 						java.util.ResourceBundle.getBundle("de/unisiegen/tpml/ui/ui").getString("FileCantBeCreated"), java.util.ResourceBundle.getBundle("de/unisiegen/tpml/ui/ui").getString("Save"),
 						JOptionPane.ERROR_MESSAGE);
 				return false;
 			}
 		}
 	}
 
   /**
    * Writes content of the source panel to a specified file.
    * 
    * @return true if the file could be written
    */
   private boolean writeFile ( )
   {
     try
     {
       BufferedWriter out = new BufferedWriter ( new OutputStreamWriter (
           new FileOutputStream ( file ) , "UTF8" ) ) ; //$NON-NLS-1$
       out.write ( code.getText ( ) ) ;
       out.close ( ) ;
       // TODO: Christoph, what about this one?
       code.clearHistory ( ) ;
       return true ;
     }
     catch ( UnsupportedEncodingException e )
     {
       logger.error ( "Could not write to file" , e ) ;
       JOptionPane.showMessageDialog ( mypanel , java.util.ResourceBundle
           .getBundle ( "de/unisiegen/tpml/ui/ui" ).getString (
               "CouldNotWriteToFile" ) , java.util.ResourceBundle.getBundle (
           "de/unisiegen/tpml/ui/ui" ).getString ( "WriteFile" ) ,
           JOptionPane.ERROR_MESSAGE ) ;
       return false ;
     }
     catch ( IOException e )
     {
       logger.error ( "Could not write to file" , e ) ;
       JOptionPane.showMessageDialog ( mypanel , java.util.ResourceBundle
           .getBundle ( "de/unisiegen/tpml/ui/ui" ).getString (
               "CouldNotWriteToFile" ) , java.util.ResourceBundle.getBundle (
           "de/unisiegen/tpml/ui/ui" ).getString ( "WriteFile" ) ,
           JOptionPane.ERROR_MESSAGE ) ;
       return false ;
     }
   }
 
 public void handlePrint() {
 	  GeneralPrinter printer = new GeneralPrinter(mypanel);
 	  if (this.isTexteditor()){
 	      printer.print(code.getEditor());
 	  } else{
 	      printer.print(((AbstractProofComponent)getComponent().getPrintPart()));
 	  }
 	  
 }
 
 //TODO Einbau der Latex-komponente... by Michgael
 public void handleLatexExport (){
 	if  (this.isTexteditor()){
 		try
 		{
 			Expression exp = this.code.getDocument().getExpression();
 			GeneralLaTex laTex = new GeneralLaTex(exp, mypanel);
 			laTex.export();
 		}
 		catch (Exception e)
 		{
 			// no real expression
 			
 			JOptionPane.showMessageDialog ( mypanel, java.util.ResourceBundle.getBundle ( "de/unisiegen/tpml/ui/ui" ) 
 					.getString ( "CouldNotLaTeXExpression" ), "Editor", JOptionPane.ERROR_MESSAGE ); 
 			
 			//JOptionPane.showMessageDialog(mypanel, "Sorry, no Expression enterd!");
 		}
 	}
 	else
 	{
 		GeneralLaTex laTex = new GeneralLaTex(((ProofViewComponent)getComponent()).getModel(), mypanel);
 		laTex.export();
 	}
 	
 	
 	
 }
 
 public void selectTypeChecker() {
 	setTexteditor(false);
 	setComponent(typechecker);
 	deselectButtons();
 	mypanel.typecheckerButton.setSelected(true);
     
 }
 
 public void selectTypeInference() {
     	setTexteditor(false);
 	setComponent(typeinference);
 	deselectButtons();
 	mypanel.typeinferenceButton.setSelected(true);
     
 }
 
 public void selectBigStep(){
 	setTexteditor(false);
 	setComponent(bigstep);
 	deselectButtons();
 	mypanel.bigstepButton.setSelected(true);
 }
 
 public void selectMinimalTyping(){
 	setTexteditor(false);
 	setComponent(minimaltyping);
 	deselectButtons();
 	mypanel.minimalTypingButton.setSelected(true);
 }
 
 public void selectSmallStep(){
 	setTexteditor(false);
 	setComponent(smallstep);
 	deselectButtons();
 	mypanel.smallstepButton.setSelected(true);
 }
 
 public void selectCode(){
 	setTexteditor(true);
 	setComponent(code);
 	deselectButtons();
 	mypanel.codeButton.setSelected(true);
 	code.getEditor().requestFocus();
 }
 
 
 
 
 
 public JPanel getPanel() {
     return this.mypanel;
     
 }
 
 
 }
