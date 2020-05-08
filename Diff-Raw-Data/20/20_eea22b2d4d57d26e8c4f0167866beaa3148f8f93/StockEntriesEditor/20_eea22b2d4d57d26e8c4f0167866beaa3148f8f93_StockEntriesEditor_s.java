 package net.sf.jmoney.stocks.pages;
 
 import net.sf.jmoney.stocks.model.StockAccount;
import net.sf.jmoney.views.NodeEditorInput;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.part.EditorPart;
 
 public class StockEntriesEditor extends EditorPart {
 
 	static public final String ID = "net.sf.jmoney.stocks.stockEntriesEditor";
 	
 	/**
 	 * The account being shown in this page.
 	 */
 	private StockAccount account;
     
 	@Override
 	public void init(IEditorSite site, IEditorInput input)
 			throws PartInitException {
 		
 		setSite(site);
 		setInput(input);
 		
     	// Set the account that this page is viewing and editing.
		NodeEditorInput input2 = (NodeEditorInput)input;
    	account = (StockAccount) input2.getNode();
 	}
 
 	@Override
 	public boolean isDirty() {
 		// Page is never dirty
 		return false;
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		// Will never be called because editor is never dirty.
 	}
 
 	@Override
 	public void doSaveAs() {
 		// Will never be called because editor is never dirty and 'save as' is not allowed anyway.
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
         FormToolkit toolkit = new FormToolkit(parent.getDisplay());
     	ScrolledForm form = toolkit.createScrolledForm(parent);
         form.getBody().setLayout(new GridLayout());
         
 		// Get the handler service and pass it on so that handlers can be activated as appropriate
 		IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
 
         final EntriesSection fEntriesSection = new EntriesSection(form.getBody(), account, toolkit, handlerService);
         fEntriesSection.getSection().setLayoutData(new GridData(GridData.FILL_BOTH));
 
         form.setText("Investment Account Entries");
 	}
 	
 	@Override
 	public void setFocus() {
 		// Don't bother to do anything.  User can select as required.
 	}
 }
