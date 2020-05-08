 package net.kbserve.pyjdt.properties.view;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.TreeItem;
 
 public class PropertyComposite extends Composite {
 	private LibraryTableComposite libsTbl;
 
 	public PropertyComposite(Composite parent, int style, final IProject project) {
 		super(parent, style);
 		setLayout(new GridLayout(1, true));
 		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 		libsTbl = new LibraryTableComposite(this, SWT.NONE, project);
 		
 	}
 
 	public TreeItem[] getItems() {
 		return libsTbl.getItems();
 	}
 
 	@Override
 	public void pack(boolean changed) {
 		libsTbl.pack(changed);
		super.pack(changed);
		getParent().layout(changed, true);
		
		
 	}
 
 }
