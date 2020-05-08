 package gui;
 
 import org.eclipse.swt.*;
import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.*;
 import org.eclipse.ui.forms.widgets.*;
 
 public abstract class ParameterArea extends Composite{
 	protected FormToolkit toolkit;
 	protected ScrolledForm form;
 	
 	public ParameterArea(Composite parent){
 		super(parent, SWT.NONE);
 		toolkit = new FormToolkit(Display.getCurrent());
		setLayout(new FillLayout());
 		form = toolkit.createScrolledForm(this);
 		form.setText("估算参数设置");
 		Composite body = form.getBody();
 		body.setLayout(new ColumnLayout());
 		createContents(body);
 	}
 	
 	protected abstract void createContents(Composite parent);
 	
 	@Override
 	public void dispose(){
 		toolkit.dispose();
 		super.dispose();
 	}
 }
