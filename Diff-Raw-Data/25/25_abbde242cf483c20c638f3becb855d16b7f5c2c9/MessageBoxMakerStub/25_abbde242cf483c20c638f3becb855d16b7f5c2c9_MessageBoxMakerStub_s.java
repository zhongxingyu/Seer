 package tests.atrStubs;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 import acceptanceTests.Register;
 
 import pg13.presentation.IMessageBoxStrategy;
 
 public class MessageBoxMakerStub implements IMessageBoxStrategy
 {
	private Label lblMessage;
 	
 	public MessageBoxMakerStub()
	{
		//super(null, SWT.NONE);
		//this.lblMessage = new Label(null, SWT.NONE);	
 		Register.newWindow(this);		
 	}
 
 	@Override
 	public void errorMessage(Shell shell, String header, String message) 
 	{
		//lblMessage.setText(message);
		
 	}
 
 	@Override
 	public void infoMessage(Shell shell, String header, String message) 
 	{
		//lblMessage.setText(message);
		
 	}
 
 }
