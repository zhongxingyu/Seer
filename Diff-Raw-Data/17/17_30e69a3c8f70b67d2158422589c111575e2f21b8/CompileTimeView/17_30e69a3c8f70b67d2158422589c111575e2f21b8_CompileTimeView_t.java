 package descent.internal.ui.infoviews;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.resource.JFaceResources;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.IRegion;
 import org.eclipse.jface.text.ITextOperationTarget;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.text.TextViewer;
 import org.eclipse.jface.text.source.SourceViewer;
 import org.eclipse.jface.util.Assert;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.text.edits.TextEdit;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.texteditor.IAbstractTextEditorHelpContextIds;
 
 import descent.core.ICodeAssist;
 import descent.core.ICompilationUnit;
 import descent.core.IJavaElement;
 import descent.core.ISourceRange;
 import descent.core.ISourceReference;
 import descent.core.JavaCore;
 import descent.core.JavaModelException;
 import descent.core.ToolFactory;
 import descent.core.dom.AST;
 import descent.core.dom.CompilationUnit;
 import descent.core.formatter.CodeFormatter;
 import descent.internal.ui.IJavaHelpContextIds;
 import descent.internal.ui.JavaPlugin;
 import descent.internal.ui.javaeditor.JavaEditor;
 import descent.internal.ui.javaeditor.JavaSourceViewer;
 import descent.internal.ui.text.JavaCodeReader;
 import descent.internal.ui.text.SimpleJavaSourceViewerConfiguration;
 import descent.internal.ui.util.SelectionUtil;
 import descent.ui.IContextMenuConstants;
 import descent.ui.actions.IJavaEditorActionDefinitionIds;
 import descent.ui.actions.JdtActionConstants;
 import descent.ui.actions.OpenAction;
 import descent.ui.text.IJavaPartitions;
 import descent.ui.text.JavaSourceViewerConfiguration;
 
 /**
  * View which shows source for a given Java element.
  *
  * @since 3.0
  */
 public class CompileTimeView extends AbstractInfoView implements IMenuListener {
 
 	/** Symbolic Java editor font name. */
 	private static final String SYMBOLIC_FONT_NAME= "descent.ui.editors.textfont"; //$NON-NLS-1$
 
 	/**
 	 * Internal property change listener for handling changes in the editor's preferences.
 	 *
 	 * @since 3.0
 	 */
 	class PropertyChangeListener implements IPropertyChangeListener {
 		/*
 		 * @see IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
 		 */
 		public void propertyChange(PropertyChangeEvent event) {
 			if (fViewer == null)
 				return;
 
 			if (fViewerConfiguration.affectsTextPresentation(event)) {
 				fViewerConfiguration.handlePropertyChangeEvent(event);
 				fViewer.invalidateTextPresentation();
 			}
 		}
 	}
 
 	/**
 	 * Internal property change listener for handling workbench font changes.
 	 */
 	class FontPropertyChangeListener implements IPropertyChangeListener {
 		/*
 		 * @see IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
 		 */
 		public void propertyChange(PropertyChangeEvent event) {
 			if (fViewer == null)
 				return;
 
 			String property= event.getProperty();
 
 			if (SYMBOLIC_FONT_NAME.equals(property))
 				setViewerFont();
 		}
 	}
 
 	/**
 	 * The Javadoc view's select all action.
 	 */
 	private static class SelectAllAction extends Action {
 
 		private TextViewer fTextViewer;
 
 		/**
 		 * Creates the action.
 		 *
 		 * @param textViewer the text viewer
 		 */
 		public SelectAllAction(TextViewer textViewer) {
 			super("selectAll"); //$NON-NLS-1$
 
 			Assert.isNotNull(textViewer);
 			fTextViewer= textViewer;
 
 			setText(InfoViewMessages.SelectAllAction_label);
 			setToolTipText(InfoViewMessages.SelectAllAction_tooltip);
 			setDescription(InfoViewMessages.SelectAllAction_description);
 
 			PlatformUI.getWorkbench().getHelpSystem().setHelp(this, IAbstractTextEditorHelpContextIds.SELECT_ALL_ACTION);
 		}
 
 		/**
 		 * Selects all in the viewer.
 		 */
 		public void run() {
 			fTextViewer.doOperation(ITextOperationTarget.SELECT_ALL);
 		}
 	}
 
 	/** This view's source viewer */
 	private SourceViewer fViewer;
 	/** The viewers configuration */
 	private JavaSourceViewerConfiguration fViewerConfiguration;
 	/** The viewer's font properties change listener. */
 	private IPropertyChangeListener fFontPropertyChangeListener= new FontPropertyChangeListener();
 	/**
 	 * The editor's property change listener.
 	 * @since 3.0
 	 */
 	private IPropertyChangeListener fPropertyChangeListener= new PropertyChangeListener();
 	/** The open action */
 	private OpenAction fOpen;
 	/** The number of removed leading comment lines. */
 	private int fCommentLineCount;
 	/** The select all action. */
 	private SelectAllAction fSelectAllAction;
 	/** Element opened by the open action. */
 	private IJavaElement fLastOpenedElement;
 	
 	private ICompilationUnit lastCompilationUnit;
 	private boolean maintainTopIndex;
 
 	/*
 	 * @see AbstractInfoView#internalCreatePartControl(Composite)
 	 */
 	protected void internalCreatePartControl(Composite parent) {
 		IPreferenceStore store= JavaPlugin.getDefault().getCombinedPreferenceStore();
 		fViewer= new JavaSourceViewer(parent, null, null, false, SWT.V_SCROLL | SWT.H_SCROLL, store);
 		fViewerConfiguration= new SimpleJavaSourceViewerConfiguration(JavaPlugin.getDefault().getJavaTextTools().getColorManager(), store, null, IJavaPartitions.JAVA_PARTITIONING, false);
 		fViewer.configure(fViewerConfiguration);
 		fViewer.setEditable(false);
 
 		setViewerFont();
 		JFaceResources.getFontRegistry().addListener(fFontPropertyChangeListener);
 
 		store.addPropertyChangeListener(fPropertyChangeListener);
 
 		getViewSite().setSelectionProvider(fViewer);
 	}
 
 	/*
 	 * @see AbstractInfoView#internalCreatePartControl(Composite)
 	 */
 	protected void createActions() {
 		super.createActions();
 		fSelectAllAction= new SelectAllAction(fViewer);
 
 		// Setup OpenAction
 		fOpen= new OpenAction(getViewSite()) {
 
 			/*
 			 * @see descent.ui.actions.SelectionDispatchAction#getSelection()
 			 */
 			public ISelection getSelection() {
 				return convertToJavaElementSelection(fViewer.getSelection());
 			}
 
 			/*
 			 * @see descent.ui.actions.OpenAction#run(IStructuredSelection)
 			 */
 			public void run(IStructuredSelection selection) {
 				if (selection.isEmpty()) {
 					getShell().getDisplay().beep();
 					return;
 				}
 				super.run(selection);
 			}
 
 			/*
 			 * @see descent.ui.actions.OpenAction#getElementToOpen(Object)
 			 */
 			public Object getElementToOpen(Object object) throws JavaModelException {
 				if (object instanceof IJavaElement)
 					fLastOpenedElement= (IJavaElement)object;
 				else
 					fLastOpenedElement= null;
 				return super.getElementToOpen(object);
 			}
 
 			/*
 			 * @see descent.ui.actions.OpenAction#run(Object[])
 			 */
 			public void run(Object[] elements) {
 				stopListeningForSelectionChanges();
 				super.run(elements);
 				startListeningForSelectionChanges();
 			}
 		};
 	}
 
 
 	/*
 	 * @see descent.internal.ui.infoviews.AbstractInfoView#getSelectAllAction()
 	 * @since 3.0
 	 */
 	protected IAction getSelectAllAction() {
 		return fSelectAllAction;
 	}
 
 	/*
 	 * @see AbstractInfoView#fillActionBars(IActionBars)
 	 */
 	protected void fillActionBars(IActionBars actionBars) {
 		super.fillActionBars(actionBars);
 		actionBars.setGlobalActionHandler(JdtActionConstants.OPEN, fOpen);
 		fOpen.setActionDefinitionId(IJavaEditorActionDefinitionIds.OPEN_EDITOR);
 	}
 
 	/*
 	 * @see AbstractInfoView#getControl()
 	 */
 	protected Control getControl() {
 		return fViewer.getControl();
 	}
 
 	/*
 	 * @see AbstractInfoView#menuAboutToShow(IMenuManager)
 	 */
 	public void menuAboutToShow(IMenuManager menu) {
 		super.menuAboutToShow(menu);
 		menu.appendToGroup(IContextMenuConstants.GROUP_OPEN, fOpen);
 	}
 
 	/*
 	 * @see AbstractInfoView#setForeground(Color)
 	 */
 	protected void setForeground(Color color) {
 		fViewer.getTextWidget().setForeground(color);
 	}
 
 	/*
 	 * @see AbstractInfoView#setBackground(Color)
 	 */
 	protected void setBackground(Color color) {
 		fViewer.getTextWidget().setBackground(color);
 	}
 	
 	/*
 	 * @see descent.internal.ui.infoviews.AbstractInfoView#getBackgroundColorKey()
 	 * @since 3.2
 	 */
 	protected String getBackgroundColorKey() {
 		return "descent.ui.CompileTimeView.backgroundColor";		 //$NON-NLS-1$
 	}
 	
 	/**
 	 * Converts the given selection to a structured selection
 	 * containing Java elements.
 	 *
 	 * @param selection the selection
 	 * @return a structured selection with Java elements
 	 */
 	private IStructuredSelection convertToJavaElementSelection(ISelection selection) {
 
 		if (!(selection instanceof ITextSelection && fCurrentViewInput instanceof ISourceReference))
 			return StructuredSelection.EMPTY;
 
 		ITextSelection textSelection= (ITextSelection)selection;
 
 		Object codeAssist= fCurrentViewInput.getAncestor(IJavaElement.COMPILATION_UNIT);
 		if (codeAssist == null)
 			codeAssist= fCurrentViewInput.getAncestor(IJavaElement.CLASS_FILE);
 
 		if (codeAssist instanceof ICodeAssist) {
 			IJavaElement[] elements= null;
 			try {
 				ISourceRange range= ((ISourceReference)fCurrentViewInput).getSourceRange();
 				elements= ((ICodeAssist)codeAssist).codeSelect(range.getOffset() + getOffsetInUnclippedDocument(textSelection), textSelection.getLength());
 			} catch (JavaModelException e) {
 				return StructuredSelection.EMPTY;
 			}
 			if (elements != null && elements.length > 0) {
 				return new StructuredSelection(elements[0]);
 			} else
 				return StructuredSelection.EMPTY;
 		}
 
 		return StructuredSelection.EMPTY;
 	}
 
 	/**
 	 * Computes and returns the offset in the unclipped document
 	 * based on the given text selection from the clipped
 	 * document.
 	 *
 	 * @param textSelection
 	 * @return the offest in the unclipped document or <code>-1</code> if the offset cannot be computed
 	 */
 	private int getOffsetInUnclippedDocument(ITextSelection textSelection) {
 		IDocument unclippedDocument= null;
 		try {
 			unclippedDocument= new Document(((ISourceReference)fCurrentViewInput).getSource());
 		} catch (JavaModelException e) {
 			return -1;
 		}
 		IDocument clippedDoc= (IDocument)fViewer.getInput();
 		try {
 			IRegion unclippedLineInfo= unclippedDocument.getLineInformation(fCommentLineCount + textSelection.getStartLine());
 			IRegion clippedLineInfo= clippedDoc.getLineInformation(textSelection.getStartLine());
 			int removedIndentation= unclippedLineInfo.getLength() - clippedLineInfo.getLength();
 			int relativeLineOffset= textSelection.getOffset() - clippedLineInfo.getOffset();
 			return unclippedLineInfo.getOffset() + removedIndentation + relativeLineOffset ;
 		} catch (BadLocationException ex) {
 			return -1;
 		}
 	}
 
 	/*
 	 * @see AbstractInfoView#internalDispose()
 	 */
 	protected void internalDispose() {
 		fViewer= null;
 		fViewerConfiguration= null;
 		JFaceResources.getFontRegistry().removeListener(fFontPropertyChangeListener);
 		JavaPlugin.getDefault().getCombinedPreferenceStore().removePropertyChangeListener(fPropertyChangeListener);
 	}
 
 	/*
 	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
 	 */
 	public void setFocus() {
 		fViewer.getTextWidget().setFocus();
 	}
 
 	/*
 	 * @see AbstractInfoView#computeInput(Object)
 	 */
 	protected Object computeInput(Object input) {
 
 		if (fViewer == null || !(input instanceof ICompilationUnit))
 			return null;
 		
 		ICompilationUnit unit = (ICompilationUnit) input;
 		
 		maintainTopIndex = lastCompilationUnit != null && unit.equals(lastCompilationUnit);
 		lastCompilationUnit = unit;
 		
 		CompilationUnit comp;
 		try {
			comp = unit.getResolvedAtCompileTime(unit.getJavaProject().getApiLevel());
 		} catch (JavaModelException e) {
 			return "";
 		}
 		String result = comp.toString();
 		if (result.startsWith("!descent.core.dom.CompilationUnit")) {
 			return "Module " + unit.getFullyQualifiedName() + " has syntax or semantic errors due to mixins";
 		}
 		return format(result, unit.getJavaProject().getApiLevel());
 	}
 	
 	private String format(String text, int apiLevel) {
 		Map options = new HashMap();
 		options.put(JavaCore.COMPILER_SOURCE, String.valueOf(apiLevel));
 		CodeFormatter formatter = ToolFactory.createCodeFormatter(options);
 		try {
 			// The most common example is something inside a function 
 			TextEdit edit = formatter.format(CodeFormatter.K_COMPILATION_UNIT,
 					text, 0, text.length(), 0, "\n"); //$NON-NLS-1$
 			if (edit != null) {
 				Document doc = new Document(text);
 				edit.apply(doc);
 				text = doc.get();
 			}
 		} catch (Exception e) {
 		}
 		return text;
 	}
 
 	/*
 	 * @see AbstractInfoView#setInput(Object)
 	 */
 	protected void setInput(Object input) {
 		int topIndex = fViewer.getTopIndex();
 		
 		if (input instanceof IDocument)
 			fViewer.setInput(input);
 		else if (input == null)
 			fViewer.setInput(new Document("")); //$NON-NLS-1$
 		else {
 			IDocument document= new Document(input.toString());
 			JavaPlugin.getDefault().getJavaTextTools().setupJavaDocumentPartitioner(document, IJavaPartitions.JAVA_PARTITIONING);			
 			fViewer.setInput(document);
 		}
 
 		if (maintainTopIndex) { 
 			fViewer.setTopIndex(topIndex);
 		}
 	}
 	
 	@Override
 	protected IJavaElement findSelectedJavaElement(IWorkbenchPart part, ISelection selection) {
 		if (part instanceof JavaEditor && selection instanceof ITextSelection) {
 			JavaEditor editor = (JavaEditor) part;
 			IEditorInput input = editor.getEditorInput();
 			return (IJavaElement) input.getAdapter(IJavaElement.class);
 		} else if (selection instanceof IStructuredSelection) {
 			Object obj = SelectionUtil.getSingleElement(selection);
 			if (obj instanceof IJavaElement) {
 				return (IJavaElement) obj;
 			}
 		} else {
 			return null;
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Removes the leading comments from the given source.
 	 *
 	 * @param source the string with the source
 	 * @return the source without leading comments
 	 */
 	private String removeLeadingComments(String source) {
 		JavaCodeReader reader= new JavaCodeReader();
 		IDocument document= new Document(source);
 		int i;
 		try {
 			reader.configureForwardReader(document, 0, document.getLength(), true, false);
 			int c= reader.read();
 			while (c != -1 && (c == '\r' || c == '\n' || c == '\t')) {
 				c= reader.read();
 			}
 			i= reader.getOffset();
 			reader.close();
 		} catch (IOException ex) {
 			i= 0;
 		} finally {
 			try {
 				if (reader != null)
 					reader.close();
 			} catch (IOException ex) {
 				JavaPlugin.log(ex);
 			}
 		}
 
 		try {
 			fCommentLineCount= document.getLineOfOffset(i);
 		} catch (BadLocationException e) {
 			fCommentLineCount= 0;
 		}
 
 		if (i < 0)
 			return source;
 
 		return source.substring(i);
 	}
 
 	/**
 	 * Sets the font for this viewer sustaining selection and scroll position.
 	 */
 	private void setViewerFont() {
 		Font font= JFaceResources.getFont(SYMBOLIC_FONT_NAME);
 
 		if (fViewer.getDocument() != null) {
 
 			Point selection= fViewer.getSelectedRange();
 			int topIndex= fViewer.getTopIndex();
 
 			StyledText styledText= fViewer.getTextWidget();
 			Control parent= fViewer.getControl();
 
 			parent.setRedraw(false);
 
 			styledText.setFont(font);
 
 			fViewer.setSelectedRange(selection.x , selection.y);
 			fViewer.setTopIndex(topIndex);
 
 			if (parent instanceof Composite) {
 				Composite composite= (Composite) parent;
 				composite.layout(true);
 			}
 
 			parent.setRedraw(true);
 
 
 		} else {
 			StyledText styledText= fViewer.getTextWidget();
 			styledText.setFont(font);
 		}
 	}
 
 	/*
 	 * @see descent.internal.ui.infoviews.AbstractInfoView#getHelpContextId()
 	 * @since 3.1
 	 */
 	protected String getHelpContextId() {
 		return IJavaHelpContextIds.SOURCE_VIEW;
 	}
 	
 	@Override
 	protected boolean isIgnoringNewInput(IJavaElement je, IWorkbenchPart part, ISelection selection) {
 		return false;
 	}
 	
 }
