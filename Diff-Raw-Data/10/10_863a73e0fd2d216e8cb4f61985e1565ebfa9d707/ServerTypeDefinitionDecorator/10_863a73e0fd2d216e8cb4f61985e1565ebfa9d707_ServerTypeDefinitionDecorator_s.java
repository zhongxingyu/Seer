 package org.eclipse.jst.server.generic.ui.internal;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jst.server.generic.servertype.definition.Property;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.wst.server.ui.internal.SWTUtil;
 import org.eclipse.wst.server.ui.wizard.IWizardHandle;
 /**
  * Provides the UI objects for gathering user information
  * for server properties.
  * 
  * @author Gorkem Ercan
  */
 public abstract class ServerTypeDefinitionDecorator implements GenericServerCompositeDecorator {
 
 	protected static final String CONTEXT_RUNTIME = Property.CONTEXT_RUNTIME;
 	protected static final String CONTEXT_SERVER = Property.CONTEXT_SERVER;
 	private ServerRuntime fDefinition;
     private Map fProperties;
 	private String fContext;
 	private GenericServerComposite fComposite;
 	private Map fStatusMap= new HashMap();
 	private String fLastMessage = null;
 	protected IWizardHandle fWizard;
 	private List fPropertyControls= new ArrayList();
 
 	private final class PathModifyListener implements ModifyListener {
 		public void modifyText(ModifyEvent e) {
 			String path = ((Text) e.widget).getText();
 			if(!pathExist(path)){
 				fLastMessage = path+" is not valid";
 				fWizard.setMessage(fLastMessage,IMessageProvider.ERROR);
			}
			else 
			if(fLastMessage!=null && fLastMessage.equals(fWizard.getMessage())){
				fLastMessage=null;
				fWizard.setMessage(null,IMessageProvider.NONE);
 				validate();
 			}
 		}
 		private boolean pathExist(String path){
 			File f = new File(path);
 			return f.exists();
 		}
 	}
 
 	
 	public ServerTypeDefinitionDecorator(ServerRuntime definition, Map initialProperties, String context, IWizardHandle handle) {
 		super();
 		fDefinition = definition;
 		fProperties = initialProperties;
 		fContext = context;
 		fWizard = handle;
 	}
 
 	public void decorate(GenericServerComposite composite) {
 		fComposite=composite;
 		List properties =null; 
 		if(fDefinition==null){
 			properties= new ArrayList(0);
 		}
 		else{
 			properties= fDefinition.getProperty();
 		}
 		for (int i = 0; i < properties.size(); i++) {
 			Property property = (Property) properties.get(i);
 			if (this.fContext.equals(property.getContext()))
 				createPropertyControl(composite, property);
 		}
 	}
 
 	
     private void createPropertyControl(Composite parent, Property property){
     	if( "directory".equals(property.getType())) {
     		Text path = createLabeledPath(property.getLabel(),getPropertyValue(property),parent);
     		path.setData(property);
     		registerControl(path);
      	} else if( "file".equals(property.getType())) {
     	    Text file = createLabeledFile(property.getLabel(),getPropertyValue(property),parent);
     		file.setData(property);
     		registerControl(file);
        	} else if( "string".equals(property.getType())) {
     	    Text str = createLabeledText(property.getLabel(),getPropertyValue(property),parent);
     		str.setData(property);
     		registerControl(str);
        	} else if( "boolean".equals(property.getType())) {
     	    Button bool =createLabeledCheck(property.getLabel(),("true".equals( getPropertyValue(property))),	parent);
     		bool.setData(property);
     		registerControl(bool);
        	} else  {
     	    Text defaultText= createLabeledText(property.getLabel(),getPropertyValue(property),parent);
     		defaultText.setData(property);
     		registerControl(defaultText);
     	}
     }
     private void registerControl(Control control)
     {
     	fPropertyControls.add(control);
     }
     private Button createLabeledCheck(String title, boolean value, Composite defPanel) {
     	GridData gridData;
     	Label label = new Label(defPanel, SWT.WRAP);
     	gridData = new GridData();
     	label.setLayoutData(gridData);
     	label.setText(title);
 
     	Button fButton = new Button(defPanel, SWT.CHECK);
     	
     	gridData = new GridData(GridData.FILL_HORIZONTAL
     			| GridData.GRAB_HORIZONTAL);
     	gridData.horizontalSpan = 2;
     	fButton.setLayoutData(gridData);
     	fButton.setSelection(value);
     	fButton.addSelectionListener(new SelectionListener() {
             public void widgetSelected(SelectionEvent e) {
               
             }
 
             public void widgetDefaultSelected(SelectionEvent e) {
   
             }
         });
     	
     	return fButton;
     }
     private Text createLabeledFile(String title, String value,final Composite defPanel) {
     	GridData gridData;
     	Label label = new Label(defPanel, SWT.WRAP);
     	gridData = new GridData();
     	label.setLayoutData(gridData);
     	label.setText(title);
     
     	final Text fText = new Text(defPanel, SWT.SHADOW_IN | SWT.BORDER);
     	gridData = new GridData(GridData.FILL_HORIZONTAL
     			| GridData.GRAB_HORIZONTAL);
     	gridData.horizontalSpan = 1;
     	fText.setLayoutData(gridData);
     	fText.setText(value);
     	fText.addModifyListener(new PathModifyListener());
     	Button fButton = SWTUtil.createButton(defPanel,GenericServerUIMessages.getString("serverTypeGroup.label.browse") );
     	
     	fButton.addSelectionListener(new SelectionListener() {
     		public void widgetSelected(SelectionEvent e) {
     			FileDialog dlg = new FileDialog(defPanel.getShell());
     			dlg.setFileName(fText.getText());
     			String res = dlg.open();
     			if (res != null) {
     				fText.setText(res);
 
     			}
     		}
     
     		public void widgetDefaultSelected(SelectionEvent e) {
     			widgetSelected(e);
     		}
     
     	});
     
     	return fText;
     }
 	
     private Text createLabeledPath(String title, String value,
     		final Composite parent) {
     	GridData gridData;
     	Label label = new Label(parent, SWT.WRAP);
     	gridData = new GridData();
     	label.setLayoutData(gridData);
     	label.setText(title);
     
     	final Text fText = new Text(parent, SWT.SHADOW_IN | SWT.BORDER);
     	gridData = new GridData(GridData.FILL_HORIZONTAL);
     	gridData.horizontalSpan = 1;
     	fText.setLayoutData(gridData);
     	fText.setText(value);
     	fText.addModifyListener(new PathModifyListener());
     	Button fButton = SWTUtil.createButton(parent,GenericServerUIMessages.getString("serverTypeGroup.label.browse"));
     	fButton.addSelectionListener(new SelectionListener() {
     		public void widgetSelected(SelectionEvent e) {
     			DirectoryDialog dlg = new DirectoryDialog(parent.getShell());
     			dlg.setFilterPath(fText.getText());
     			String res = dlg.open();
     			if (res != null) {
     				fText.setText(res);
 
     			}
     		}
     
     		public void widgetDefaultSelected(SelectionEvent e) {
     			widgetSelected(e);
     		}
     
     	});
     
     	return fText;
     }
     private Text createLabeledText(String title, String value,
     		Composite defPanel) {
     	GridData gridData;
     	Label label = new Label(defPanel, SWT.WRAP);
     	gridData = new GridData();
     	label.setLayoutData(gridData);
     	label.setText(title);
     
     	Text fText = new Text(defPanel, SWT.SHADOW_IN | SWT.BORDER);
     	gridData = new GridData(GridData.FILL_HORIZONTAL
     			| GridData.GRAB_HORIZONTAL);
     	gridData.horizontalSpan = 2;
     	fText.setLayoutData(gridData);
     	fText.setText(value);
 //    	fText.addModifyListener(new PropertyModifyListener());
     	return fText;
     }
 	private String getPropertyValue(Property property)
 	{
 		String value = property.getDefault();
 		if(fProperties!=null && fProperties.isEmpty()==false)
 			value=(String)fProperties.get(property.getId()); 
 		return value;
 	}	
 
 
 	
    /**
     * Returns the property name/value pairs.
     * @return
     */
 	public Map getValues()
     {
 		Map propertyMap = new HashMap();
     	for(int i=0; i<fPropertyControls.size();i++)
     	{
     		if(fPropertyControls.get(i)instanceof Button)
     		{
     			Button button = (Button)fPropertyControls.get(i);
     			Property prop = (Property)button.getData();
     			propertyMap.put(prop.getId(),Boolean.toString(button.getSelection()));
     		}
     		else
     		{
     			Text text = (Text)fPropertyControls.get(i);
     			Property prop = (Property)text.getData();
     			propertyMap.put(prop.getId(),text.getText());
     		}
     	}
     	return propertyMap;
     }
 }
