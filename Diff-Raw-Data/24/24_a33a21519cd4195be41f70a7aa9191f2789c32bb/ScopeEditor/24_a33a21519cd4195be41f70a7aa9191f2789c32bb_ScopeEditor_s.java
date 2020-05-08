 package wwe.scope;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.EditorPart;
 
 import wwe.scope.control.ScopeCanvas;
 import fhv.eclipse2013.wwe.contract.scope.ISimulationScope;
 
 public class ScopeEditor extends EditorPart {
 	public static final String ID = "WWE.scope.ScopeEditor"; //$NON-NLS-1$
 	private ISimulationScope scope;
 	private ScopeEditorInput input;
 	private ScopeCanvas canvas;
 
 	@Override
 	public void init(IEditorSite site, IEditorInput input)
 			throws PartInitException {
 		if (!(input instanceof ScopeEditorInput)) {
 			throw new RuntimeException(Messages.ScopeEditor_1);
 		}
 
 		this.input = (ScopeEditorInput) input;
 		setSite(site);
 		setInput(input);
 		scope = this.input.getScope();
 		setPartName(this.input.getName());
 	}
 
 	public ISimulationScope getScope() {
 		return scope;
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 
 		ScrolledComposite scroll = new ScrolledComposite(parent, SWT.V_SCROLL
 				| SWT.H_SCROLL | SWT.BORDER);
 		scroll.setLayout(new GridLayout(1, true));
 
		Composite scrollContent = new Composite(scroll, SWT.NONE);
		scrollContent.setLayout(new GridLayout(scope.getWidth(), false));
 
		canvas = new ScopeCanvas(scrollContent, 1, scope);
 
 		scroll.getVerticalBar().addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				canvas.redrawComplete();
 			}
 		});
 
 		scroll.getHorizontalBar().addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				canvas.redrawComplete();
 			}
 		});
 
 		// for (int y = 0; y < scope.getHeight(); y++) {
 		// for (int x = 0; x < scope.getWidth(); x++) {
 		// FieldCanvas canvas = new FieldCanvas(scrollContent, 1, scope,
 		// x, y);
 		// DropTarget dt = new DropTarget(canvas, DND.DROP_COPY
 		// | DND.DROP_MOVE);
 		// dt.setTransfer(new Transfer[] { ObjectTransfer.elementTransfer });
 		// dt.addDropListener(new ToolboxTarget(canvas));
 		// }
 		// }
 		//
 
		scroll.setMinSize(scrollContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scroll.setContent(scrollContent);
 		scroll.setExpandVertical(true);
 		scroll.setExpandHorizontal(true);
 		scroll.setAlwaysShowScrollBars(true);
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 	}
 
 	@Override
 	public void doSaveAs() {
 	}
 
 	@Override
 	public boolean isDirty() {
 		return false;
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
 	@Override
 	public void setFocus() {
 	}
 
 	public Control getCanvas() {
 		return this.canvas;
 	}
 
 }
