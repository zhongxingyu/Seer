 package org.ita.neutrino.abstracrefactoring;
 
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.ITypeRoot;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.ita.neutrino.abstracttestparser.TestBattery;
 import org.ita.neutrino.abstracttestparser.TestParserException;
 import org.ita.neutrino.astparser.ASTParser;
 import org.ita.neutrino.astparser.ASTSelection;
 import org.ita.neutrino.codeparser.Environment;
 import org.ita.neutrino.codeparser.ParserException;
 import org.ita.neutrino.eclipseaction.ActionException;
 import org.ita.neutrino.eclipseaction.Activator;
 import org.ita.neutrino.eclipseaction.IAction;
 import org.ita.neutrino.junit4parser.JUnitParser;
 
 public abstract class AbstractEclipseRefactoringAction implements IAction {
 
 	private ISelection selection;
 	private AbstractRefactoring refactoringObject;
 
 	@Override
 	public ISelection getSelection() {
 		return selection;
 	}
 
 	@Override
 	public void setSelection(ISelection selection) {
 		this.selection = selection;
 	}
 
 	/**
 	 * Deve devolver um nome amigável para a refatoração, esse valor será utilizado nos diálogos com o usuário.
 	 * 
 	 * @return
 	 */
 	protected abstract String getRefactoringName();
 
 	@Override
 	public void run() throws ActionException {
 		verifyPreConditions();
 
 		Environment environment = doCodeParsing();
 
 		TestBattery battery = doTestParsing(environment);;
 
 		refactoringObject = createRefactoringObject();
 
 		refactoringObject.setBattery(battery);
 		refactoringObject.setTargetFragment(battery.getSelection().getSelectedFragment());
 
 		verifyInitialConditions();
 
 		if (!prepareRefactoringObject()) {
 			return;
 		}
 
 		try {
 			refactoringObject.refactor();
 		} catch (RefactoringException e) {
 			throw new ActionException(e);
 		}
 	}
 
 	private void verifyPreConditions() throws ActionException {
 		List<String> problems = checkPreConditions();
 
		if ((problems != null) && (problems.size() > 0)) {
 			String message = RefactoringException.getMessageForProblemList(problems);
 
 			MessageDialog.openWarning(null, getRefactoringName(), message);
 
 			throw new ActionException(message);
 		}
 	}
 
 	/**
 	 * Permite fazer uma checagem prévia das condições no Eclipse antes de fazer
 	 * qualquer outra coisa.
 	 * 
 	 * @return
 	 */
 	protected abstract List<String> checkPreConditions();
 
 	private Environment doCodeParsing() throws ActionException {
 		ASTParser codeParser = new ASTParser();
 
 		try {
 			codeParser.setCompilationUnits(RefactoringUtils.getAllWorkspaceCompilationUnits(null).toArray(new ICompilationUnit[0]));
 		} catch (CoreException e) {
 			throw new ActionException(e);
 		}
 
 		ICompilationUnit activeCompilationUnit = getActiveCompilationUnit();
 		codeParser.setActiveCompilationUnit(activeCompilationUnit);
 		ASTSelection codeSelection = codeParser.getSelection();
 		codeSelection.setSourceFile(activeCompilationUnit);
 
 		ITextSelection textSelection = (ITextSelection) selection;
 		codeSelection.setSelectionStart(textSelection.getOffset());
 		codeSelection.setSelectionLength(textSelection.getLength());
 
 		try {
 			codeParser.parse();
 		} catch (ParserException e) {
 			throw new ActionException(e);
 		}
 
 		return codeParser.getEnvironment();
 	}
 
 	private TestBattery doTestParsing(Environment environment) throws ActionException {
 		JUnitParser testParser = new JUnitParser();
 
 		testParser.setEnvironment(environment);
 
 		try {
 			testParser.parse();
 		} catch (TestParserException e) {
 			throw new ActionException(e);
 		}
 		
 		return testParser.getBattery();
 	}
 
 	/**
 	 * Deve instanciar e devolver o objeto de refatoração.
 	 * 
 	 * @return
 	 */
 	protected abstract AbstractRefactoring createRefactoringObject();
 
 	private void verifyInitialConditions() throws ActionException {
 		List<String> errors = refactoringObject.checkInitialConditions();
 
 		if (errors.size() > 0) {
 			String message = RefactoringException.getMessageForProblemList(errors);
 
 			MessageDialog.openWarning(null, getRefactoringName(), message);
 
 			throw new ActionException(message);
 		}
 	}
 
 	/**
 	 * Preparação final do objeto de refatoração. Deve devolver true caso a
 	 * refatoração deva continuar. Se devolver false, nenhuma exceção é lançada.
 	 * 
 	 * @return
 	 */
 	protected boolean prepareRefactoringObject() {
 		return true;
 	}
 
 	private ICompilationUnit getActiveCompilationUnit() {
 		IWorkbench workbench = Activator.getDefault().getWorkbench();
 
 		IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
 
 		IWorkbenchPage page = workbenchWindow.getActivePage();
 
 		IEditorPart editorPart = page.getActiveEditor();
 
 		if (editorPart == null) {
 			// Nenhuma janela de edição ativa
 			return null;
 		}
 
 		IEditorInput editorInput = editorPart.getEditorInput();
 
 		ITypeRoot typeRoot = JavaUI.getEditorInputTypeRoot(editorInput);
 
 		return (ICompilationUnit) typeRoot;
 	}
 
 }
