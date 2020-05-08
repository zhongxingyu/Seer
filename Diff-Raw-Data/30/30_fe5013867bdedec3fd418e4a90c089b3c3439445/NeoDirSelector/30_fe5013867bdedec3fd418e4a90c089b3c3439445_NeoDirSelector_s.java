 package org.amanzi.awe.catalog.neo.actions;
 
 import java.io.File;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 
 public class NeoDirSelector 
 {
	public void run()
 	{
	Display display = new Display();
     Shell shell = new Shell(display);
     shell.setText("File Dialog");
     createContents(shell);
     shell.pack();
     shell.open();
     while (!shell.isDisposed()) {
       if (!display.readAndDispatch()) {
         display.sleep();
       }
     }
    display.dispose();
	
 	
 	}
 
 	private void createContents(final Shell shell) 
 	{// TODO Auto-generated method stub
 		 shell.setLayout(new GridLayout(5, true));
 		 Button openDir = new Button(shell, SWT.PUSH);
 		    openDir.setText("Select Neo database location...");
 		    openDir.addSelectionListener(new SelectionAdapter()
 		    {
 		    	 public void widgetSelected(SelectionEvent event) 
 		    	 {
 		    		   DirectoryDialog dirDlg=new DirectoryDialog(shell);
 		    		   String dirLocation=dirDlg.open();
 		    		 if(dirLocation!=null)
 		    		 {
 		    		    File neoLocationFile=new File(dirLocation);
 		    		 if(neoLocationFile.isDirectory())
 		    		 {
 		    			if(neoLocationFile.list().length>0)
 		    			{
 		    				for(int j=0;j<neoLocationFile.list().length;j++)
 		    				{
 		    					if(neoLocationFile.list()[j].equals("neostore.nodestore"))
 		    					{
 		    						//here should be loaded data from Neo database
 		    						break;
 		    					}
 		    				}
 		    			} 
 		    		 }
 		    		 }
 		    	 }
 		    });
 	}
 	
 
 }
