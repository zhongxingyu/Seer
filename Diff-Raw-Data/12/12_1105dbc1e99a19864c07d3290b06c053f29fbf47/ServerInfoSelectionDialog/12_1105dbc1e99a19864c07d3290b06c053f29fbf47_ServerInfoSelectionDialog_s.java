 package org.eclipse.emf.emfstore.client.ui.dialogs.login;
 
 import org.eclipse.emf.edit.provider.ComposedAdapterFactory;
 import org.eclipse.emf.edit.ui.provider.AdapterFactoryLabelProvider;
 import org.eclipse.emf.emfstore.client.model.ServerInfo;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ListViewer;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Shell;
 
 public class ServerInfoSelectionDialog extends TitleAreaDialog {
 
 	private final java.util.List<ServerInfo> servers;
 	private ServerInfo result;
 	private ListViewer listViewer;
 	private ServerInfoLabelProvider labelProvider;
 
 	/**
 	 * Create the dialog.
 	 * 
 	 * @param parentShell
 	 */
 	public ServerInfoSelectionDialog(Shell parentShell, java.util.List<ServerInfo> servers) {
 		super(parentShell);
 		this.servers = servers;
 		this.result = null;
 	}
 
 	/**
 	 * Create contents of the dialog.
 	 * 
 	 * @param parent
 	 */
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		setMessage("In order to execute your requested operation, you have to select a server.");
 		setTitle("Please select a Server");
 		Composite area = (Composite) super.createDialogArea(parent);
 		Composite container = new Composite(area, SWT.NONE);
 		container.setLayout(new GridLayout(1, false));
 		container.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		listViewer = new ListViewer(container, SWT.BORDER | SWT.V_SCROLL);
 		List list = listViewer.getList();
 		list.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		listViewer.setContentProvider(ArrayContentProvider.getInstance());
 		labelProvider = new ServerInfoLabelProvider();
 		listViewer.setLabelProvider(labelProvider);
 		listViewer.setInput(servers);
 		if (servers.size() == 1) {
 			listViewer.setSelection(new StructuredSelection(servers.get(0)));
 		}
 		return area;
 	}
 
 	@Override
 	protected void okPressed() {
 		ISelection selection = listViewer.getSelection();
 		if (selection instanceof IStructuredSelection
 			&& ((IStructuredSelection) selection).getFirstElement() instanceof ServerInfo) {
 			result = (ServerInfo) ((IStructuredSelection) selection).getFirstElement();
 		}
 		super.okPressed();
 	}
 
 	public ServerInfo getResult() {
 		return result;
 	}
 
 	@Override
 	public boolean close() {
 		labelProvider.dispose();
 		return super.close();
 	}
 
 	/**
 	 * Create contents of the button bar.
 	 * 
 	 * @param parent
 	 */
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
 		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
 	}
 
 	/**
 	 * Return the initial size of the dialog.
 	 */
 	@Override
 	protected Point getInitialSize() {
 		return new Point(355, 403);
 	}
 
 	private class ServerInfoLabelProvider extends AdapterFactoryLabelProvider {
 
 		public ServerInfoLabelProvider() {
 			super(new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE));
 		}
 
 		@Override
		public String getColumnText(Object object, int columnIndex) {
			String text = super.getColumnText(object, columnIndex);
 			if (object instanceof ServerInfo) {
 				ServerInfo server = (ServerInfo) object;
				text += " [" + server.getUrl() + " : " + server.getPort() + "]";
 			}
			return text;
 		}
 	}

 }
