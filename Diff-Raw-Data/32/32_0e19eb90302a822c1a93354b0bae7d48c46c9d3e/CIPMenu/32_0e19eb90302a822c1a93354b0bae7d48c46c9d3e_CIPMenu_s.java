 package de.haw.cip.gui;
 
 import de.haw.cip.gui.actions.AllAnalysisAction;
 import de.haw.cip.gui.actions.CloseFileAction;
 import de.haw.cip.gui.actions.CodeBuilderAction;
 import de.haw.cip.gui.actions.DisplayInfoDialogAction;
 import de.haw.cip.gui.actions.LexicalAnalysisAction;
 import de.haw.cip.gui.actions.OpenFileAction;
 import de.haw.cip.gui.actions.OpenLastFileAction;
 import de.haw.cip.gui.actions.SaveAsFileAction;
 import de.haw.cip.gui.actions.SaveFileAction;
 import de.haw.cip.gui.actions.SaveOutputAction;
 import de.haw.cip.gui.actions.SemanticAnalysisAction;
 import de.haw.cip.gui.actions.SyntaxAnalysisAction;
 import de.mb.swing.*;
 import de.mk.exception.CompilerErrorException;
 
 import java.io.File;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 
 /**
  * @author Martin Koose
  *
  * To change this generated comment edit the template variable "typecomment":
  * Window>Preferences>Java>Templates.
  * To enable and disable the creation of type comments go to
  * Window>Preferences>Java>Code Generation.
  */
 public class CIPMenu {
 	private CIPWindow app;
 
	// Hauptmen
 	private JMenuBar menuBar;
	// Men: File
 	private JMenu file;
 	private JMenuItem file_new;
 	private JMenuItem file_open;
 	private JMenuItem file_save;
 	private JMenuItem file_saveas;
 	private JMenuItem file_saveoutput;
 	private JMenuItem file_close;
 	private JMenuItem file_exit;
	// Men: Compiler
 	private JMenu compiler;
 	private JMenuItem compiler_all;
 	private JMenuItem compiler_lexicalanalysis;
 	private JMenuItem compiler_syntaxanalysis;
 	private JMenuItem compiler_semanticalanalysis;
 	private JMenuItem compiler_codebuilder;
 
	// Men: Ansicht
 	private CIPMenuAnsicht view;
 
	// Men: Hilfe
 	private JMenu help;
 	private JMenuItem help_info;
 
 	private CIPMenu() {
 	}
 
 	public CIPMenu(CIPWindow win) {
 		app = win;
 	}
 
 	/** Activates Lexical Analysis Menu Button
 	 * */
 	public void activateLexicalAnalysis() {
 		compiler_lexicalanalysis.setEnabled(true);
 	}
 	/** Activates Syntax Analysis Menu Button
 	 * */
 	public void activateSyntaxAnalysis() {
 		compiler_syntaxanalysis.setEnabled(true);
 	}
 	/** Activates Semantic Analysis Menu Button
 	 * */
 	public void activateSemanticAnalysis() {
 		compiler_semanticalanalysis.setEnabled(true);
 	}
 	/** Activates CodeBuilder Menu Button
 	 * */
 	public void activateCodeBuilder() {
 		compiler_codebuilder.setEnabled(true);
 	}
 	/** Activates AllAnalysis Menu Button
 	 * */
 	public void activateAllAnalysis() {
 		compiler_all.setEnabled(true);
 	}
 	/** DeActivates Lexical Analysis Menu Button
 	 * */
 	public void deactivateLexicalAnalysis() {
 		compiler_lexicalanalysis.setEnabled(false);
 	}
 	/** DeActivates Syntax Analysis Menu Button
 	 * */
 	public void deactivateSyntaxAnalysis() {
 		compiler_syntaxanalysis.setEnabled(false);
 	}
 	/** DeActivates Semantic Analysis Menu Button
 	 * */
 	public void deactivateSemanticAnalysis() {
 		compiler_semanticalanalysis.setEnabled(false);
 	}
 	/** DeActivates CodeBuilder Menu Button
 	 * */
 	public void deactivateCodeBuilder() {
 		compiler_codebuilder.setEnabled(false);
 	}
 	/** DeActivates AllAnalysis Menu Button
 	 * */
 	public void deactivateAllAnalysis() {
 		compiler_all.setEnabled(false);
 	}
 	private JMenuItem getJMenuItem(String desc) {
 		JMenuItem result = new JMenuItem(desc);
 		return result;
 	}
 
 	public JMenu getFileMenu() {
 		file_open = getJMenuItem(Ressource_Bundle.getString("menu_file_open")); //$NON-NLS-1$
 		file_open.addActionListener(new OpenFileAction(app));
 
 		file_save = getJMenuItem(Ressource_Bundle.getString("menu_file_save")); //$NON-NLS-1$
 		file_save.addActionListener(new SaveFileAction(app));
 
 		file_saveas = getJMenuItem(Ressource_Bundle.getString("menu_file_saveas")); //$NON-NLS-1$
 		file_saveas.addActionListener(new SaveAsFileAction(app));
 
 		file_saveoutput = getJMenuItem(Ressource_Bundle.getString("menu_file_saveoutput")); //$NON-NLS-1$
 		file_saveoutput.addActionListener(new SaveOutputAction(app));
 
 		file_exit = getJMenuItem(Ressource_Bundle.getString("menu_file_exit")); //$NON-NLS-1$
 		file_exit.addActionListener(new CloseFileAction(app));
 
 		file = new JMenu(Ressource_Bundle.getString("menu_file")); //$NON-NLS-1$
 		file.add(file_open);
 		file.add(file_save);
 		file.add(file_saveas);
 		file.add(file_saveoutput);
 		file.addSeparator();
 		file.add(file_exit);
 
 		return file;
 	}
 
 	public JMenu getCompilerMenu() {
 		compiler_all = getJMenuItem(Ressource_Bundle.getString("menu_compiler_allanalysis")); //$NON-NLS-1$
 		compiler_all.addActionListener(new AllAnalysisAction(app));
 		compiler_all.setEnabled(false);
 
 		compiler_lexicalanalysis = getJMenuItem(Ressource_Bundle.getString("menu_compiler_lexicalanalysis")); //$NON-NLS-1$
 		compiler_lexicalanalysis.addActionListener(
 			new LexicalAnalysisAction(app));
 		compiler_lexicalanalysis.setEnabled(false);
 
 		compiler_syntaxanalysis = getJMenuItem(Ressource_Bundle.getString("menu_compiler_syntacticalanalysis")); //$NON-NLS-1$
 		compiler_syntaxanalysis.addActionListener(
 			new SyntaxAnalysisAction(app));
 		compiler_syntaxanalysis.setEnabled(false);
 
 		compiler_semanticalanalysis = getJMenuItem(Ressource_Bundle.getString("menu_compiler_semanticalanalysis")); //$NON-NLS-1$
 		compiler_semanticalanalysis.addActionListener(
 			new SemanticAnalysisAction(app));
 		compiler_semanticalanalysis.setEnabled(false);
 
 		compiler_codebuilder = getJMenuItem(Ressource_Bundle.getString("menu_compiler_codebuilder")); //$NON-NLS-1$
 		compiler_codebuilder.addActionListener(new CodeBuilderAction(app));
 		compiler_codebuilder.setEnabled(false);
 
 		compiler = new JMenu(Ressource_Bundle.getString("menu_compiler")); //$NON-NLS-1$
 		compiler.add(compiler_all);
 		compiler.addSeparator();
 		compiler.add(compiler_lexicalanalysis);
 		compiler.add(compiler_syntaxanalysis);
 		compiler.add(compiler_semanticalanalysis);
 		compiler.add(compiler_codebuilder);
 
 		return compiler;
 	}
 
 	private JMenu getHelpMenu() {
 		help_info = getJMenuItem(Ressource_Bundle.getString("menu_help_info"));
 		help_info.addActionListener(new DisplayInfoDialogAction(app));
 		help_info.setEnabled(true);
 
 		help = new JMenu(Ressource_Bundle.getString("menu_help"));
 		help.add(help_info);
 
 		return help;
 	}
 
 	public JMenuBar getMyMenuBar() throws CompilerErrorException {
 		JMenuBar result = new JMenuBar();
 
 		result.add(this.getFileMenu());
 		result.add(this.getCompilerMenu());
 		result.add(view = new CIPMenuAnsicht(this,"Ansicht"));
 		result.add(this.getHelpMenu());
 		return result;
 	}
 
 	public void addLastOpenedFile(File f, int lastOpenedFiles) {
 		if (!app.hasLastOpenedFiles()) {
 			file.addSeparator();
 		}
 		JMenuItemLastOpenedFile temp =
 			new JMenuItemLastOpenedFile(f, lastOpenedFiles);
 		temp.addActionListener(new OpenLastFileAction(app));
 		file.add(temp);
 	}
 
 	public CIPWindow getApp() {
 		return app;
 	}
 	
 	public void selectViewSource(boolean selected) {
 		this.view.selectViewSource( selected);
 	}
 	public void selectViewOutput(boolean selected) {
 		this.view.selectViewOutput( selected);
 	}
 	public void selectViewStatus(boolean selected) {
 		this.view.selectViewSource( selected);
 	}
 
 }
