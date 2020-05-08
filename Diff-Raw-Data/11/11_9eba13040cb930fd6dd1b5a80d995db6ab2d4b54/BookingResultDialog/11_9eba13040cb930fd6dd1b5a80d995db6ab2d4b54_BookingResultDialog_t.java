 /**
  * 
  */
 package org.teagle.vcttool.view.dialogs;
 
import java.awt.Toolkit;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.teagle.vcttool.control.OrchestrateReturn;
 import org.teagle.vcttool.control.OrchestrateReturn.LogBook;
 //import org.teagle.vcttool.control.OrchestrateReturn.LogEntry;
 
 /**
  * @author sim
  *
  */
 public class BookingResultDialog extends Dialog implements SelectionListener {
 
 	private Map<String, String> configParams = new HashMap<String, String>();
 	
 	private Group params;
 
 	private Button buttonOk;
 	
     String combine(String[] s, String glue)
     {
       int k=s.length;
       if (k==0)
         return "";
       StringBuilder out=new StringBuilder();
       out.append(s[0]);
       for (int x=1;x<k;++x)
         out.append(glue).append(s[x]);
       return out.toString();
     }
 
 	public BookingResultDialog(Shell shell, int code, String mesg, String url, OrchestrateReturn.LogBook log) {
 		super(shell, "Booking finished");
 		
 		Composite container = getContainer();
 		
 		params = new Group(container, SWT.NONE);
 
 		GridLayout layout = new GridLayout(2, false);
 		params.setLayout(layout);
 		GridData data = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
 		//data.verticalSpan = 20;
 		params.setLayoutData(data);
 
 		buttonOk = new Button(container, SWT.PUSH);
 	    buttonOk.setText("OK");
 	    buttonOk.setLayoutData(new GridData(SWT.LEFT, SWT.BOTTOM, false, false));
 	    buttonOk.addSelectionListener(this);
 	    
 	    String[] lines = mesg.split("\n");
 	    int ms = 0;
 	    if (lines != null && lines.length > 10)
 	    	ms = 150;
 	    int ls = 250;
 	    String logstr = dropLog(log, "", "");
     
 	    addConfigurationParameter("result", Integer.toString(code), "the result code");
 	    addTextArea("message", mesg, "result description", ms);
 	    addConfigurationParameter("log url", url, "location of the OE log. Look here for more information.");
 	    addTextArea("log", logstr, "The operation's log.", ls);
	    
	    params.pack();
	    container.pack();
 	}
 	
 	private String dropLog(LogBook book, String log, String indent)
 	{
 		for (int i = 0; i < book.entries.length; ++i)
 		{
 			Object entry = book.entries[i];
 			if (entry instanceof LogBook)
 			{
 				LogBook inner = (LogBook)entry;
 				log += "\nBEGIN INNER LOG: name=" + inner.name + " component=" + inner.component + "\n";
 				log += dropLog(inner, log, indent + "    ");
 				log += "\nEND INNER LOG: name=" + inner.name + " component=" + inner.component + "\n";
 			}
 			else
 			{
 				log += indent + entry;
 				if (i < book.entries.length - 1)
 					log += "\n";
 			}
 		}
 		return log;
 	}
 	
 	public void addTextArea(String name, String value, String description, int size) {
 	    Label label = new Label(params, SWT.NONE);
 	    label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
 	    label.setText(name);
 	    Text text = new Text(params, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
 	    
 	    GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
 	    if (size > 0)
 	    	data.heightHint = size;
 	    //data.verticalSpan = 20;
	    data.widthHint = Toolkit.getDefaultToolkit().getScreenSize().width / 2; 

 	    text.setLayoutData(data);
 	    text.setText(value);
 	    
 	    text.setData(name);
 	    
 	    if (description != null && !description.isEmpty()) {
 		    label.setToolTipText(description);
 		    text.setToolTipText(description);
	    }
 	}
 
 	public void addConfigurationParameter(String name, String defaultValue, String description) {
 	    Label label = new Label(params, SWT.NONE);
 	    label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
 	    label.setText(name);
 	    Text text = new Text(params, SWT.BORDER | SWT.READ_ONLY);
 	    
 	    text.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
 	    if (defaultValue != null && !defaultValue.isEmpty()) {
 	    	text.setText(defaultValue);
 	    }
 	    
 	    text.setData(name);
 	    
 	    if (description != null && !description.isEmpty()) {
 		    label.setToolTipText(description);
 		    text.setToolTipText(description);
 	    }   
 	}
 
 	public void addConfigurationParameter(String name, boolean defaultValue, String description) {
 		Button checkbox = new Button(params, SWT.CHECK | SWT.RIGHT_TO_LEFT);
 		checkbox.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
 		checkbox.setText(name);
 		checkbox.setSelection(defaultValue);
 		
 		checkbox.setData(name);
 		
 	    if (description != null && !description.isEmpty()) {
 		    checkbox.setToolTipText(description);
 	    }   
 	}
 
 	public String getConfiguratonParameter(String name) {
 		return configParams.get(name);
 	}
 	
 	public void widgetDefaultSelected(SelectionEvent arg0) {
 	}
 
 	public void widgetSelected(SelectionEvent event) {
 		if (event.widget == buttonOk) {
 			for (Control control : params.getChildren()) {
 				Object data = control.getData();
 				if (data != null) {
 					if (control instanceof Text) {
 						configParams.put(data.toString(), ((Text)control).getText());
 					} else if (control instanceof Button) {
 						configParams.put(data.toString(), String.valueOf(((Button)control).getSelection()));
 					}
 				}
 			}
 			hide(SWT.OK);					
 		} else {
 			hide(SWT.CANCEL);			
 		}
 	}
 }
