 package it.polito.atlas.alea2.adapters;
 
 import it.polito.atlas.alea2.Project;
 import it.polito.atlas.alea2.Storage;
 import it.polito.atlas.alea2.components.MainWindowShell;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.MessageBox;
 
 public class SaveProjectAdapter extends SelectionAdapter {
 
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		Storage st=MainWindowShell.getStorage();
 		Project p=MainWindowShell.getCurrentProject();
		if (p==null)
			return;
 		if (st.containsProject(p.getName())) {
 			MessageBox mb = new MessageBox(MainWindowShell.shell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
 			mb.setText("Save on Storage");
 			mb.setMessage("The prject already exist in current storage, would you overwrite?");
 			int rc = mb.open();
 		    switch (rc) {
 		    	case SWT.YES:
 		    		break;
 		    	case SWT.NO:
 		    		return;
 		    }
 		}
 		if  ((p!=null) && (st!=null))
 			st.writeProject(p, true);
 	}
 
 }
