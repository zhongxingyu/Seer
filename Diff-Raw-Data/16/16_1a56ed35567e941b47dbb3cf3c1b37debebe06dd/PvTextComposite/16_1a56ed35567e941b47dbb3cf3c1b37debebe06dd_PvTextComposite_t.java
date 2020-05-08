 /**
  * 
  */
 package de.ptb.epics.eve.viewer;
 
 import org.csstudio.platform.model.pvs.IProcessVariableAddress;
 import org.csstudio.platform.model.pvs.ProcessVariableAdressFactory;
 import org.csstudio.platform.simpledal.ConnectionException;
 import org.csstudio.platform.simpledal.ConnectionState;
 import org.csstudio.platform.simpledal.IProcessVariableConnectionService;
 import org.csstudio.platform.simpledal.IProcessVariableValueListener;
 import org.csstudio.platform.simpledal.ProcessVariableConnectionServiceFactory;
 import org.csstudio.platform.simpledal.ValueType;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 import org.epics.css.dal.Timestamp;
 
 /**
  * 
  * Text widget registers its pvname with dal-epics and shows the value
  * in the text field. Text changes from GUI are sent to the pv on focus lost event
  * 
  * @author eden
  * 
  */
 public class PvTextComposite extends Composite implements IProcessVariableValueListener, SelectionListener {
 
 	private IProcessVariableAddress pv;
 	private String pvname;
 	private Text textWidget;
 	/**
 	 * @param parent parent widget 
 	 * @param style SWT style
 	 * @param pvname the PV to connect to
 	 */
 	public PvTextComposite(Composite parent, int style, String pvname) {
 		super(parent, style);
 		this.pvname = pvname;
 		textWidget = new Text(this, SWT.BORDER);
 		this.setLayout(new FillLayout());
 		textWidget.setToolTipText(pvname);
 		textWidget.setText("unknown");
 		IProcessVariableConnectionService service = ProcessVariableConnectionServiceFactory.getDefault().getProcessVariableConnectionService();
 		ProcessVariableAdressFactory pvFactory = ProcessVariableAdressFactory.getInstance();
 		pv = pvFactory.createProcessVariableAdress("dal-epics://" + pvname);
 		service.register( this, pv, ValueType.STRING );
 
 		// add a listener for the TextInput
 		final String writepvname = pvname;
 		textWidget.addFocusListener( new FocusListener() {
 		
			
 			public void focusLost(FocusEvent e) {
 				IProcessVariableConnectionService service = ProcessVariableConnectionServiceFactory.getDefault().getProcessVariableConnectionService();
 				ProcessVariableAdressFactory pvFactory = ProcessVariableAdressFactory.getInstance();
 				String prefix = "dal-epics://";
 				IProcessVariableAddress pv = pvFactory.createProcessVariableAdress("dal-epics://" + writepvname);
 
 				try {
 					service.writeValueSynchronously( pv, textWidget.getText(), ValueType.STRING );
 				} catch( final ConnectionException e1 ) {
 					e1.printStackTrace();
 				}
 			}
 		
 			public void focusGained(FocusEvent e) {
 				textWidget.setForeground(Activator.getDefault().getColor("COLOR_PV_INITIAL"));
 			
 			}
 		});	
 	}
 
	/*
	 * (non-Javadoc)
 	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
 	 */
 	public void widgetDefaultSelected(SelectionEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
	/*
	 * (non-Javadoc)
 	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 	 */
 	public void widgetSelected(SelectionEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void connectionStateChanged( final ConnectionState connectionState ) {
 		if (!isDisposed()) getDisplay().syncExec( new Runnable() {
 
 			public void run() {
 				
 				String newText = "";
 				Color newColor=Activator.getDefault().getColor("COLOR_PV_INITIAL");
 				switch( connectionState ) {
 					case CONNECTED:
 						newText = "connected";
 						newColor = Activator.getDefault().getColor("COLOR_PV_OK");
 						break;
 					case CONNECTION_FAILED:
 						newText = "connection failed";
 						newColor = Activator.getDefault().getColor("COLOR_PV_ALARM");
 						break;
 					case CONNECTION_LOST:
 						newText = "connection lost";
 						newColor = Activator.getDefault().getColor("COLOR_PV_ALARM");
 						break;
 					case INITIAL:
 						newText = "initial";
 						newColor = Activator.getDefault().getColor("COLOR_PV_INITIAL");
 						break;
 					case UNKNOWN:
 						newText = "unknown";
 						newColor = Activator.getDefault().getColor("COLOR_PV_UNKNOWN");
 				}
 				Activator.getDefault().getMessagesContainer().addMessage( new ViewerMessage( MessageSource.APPLICATION, MessageTypes.INFO, pv.getFullName() + " changed connection state to " + newText + "." ) );
 				if (!textWidget.isDisposed()) textWidget.setForeground(newColor);
 			}
 		});
 	}
 
 	public void errorOccured( final String error ) {
 		System.err.println( "error  in " + pvname );
 	}
 
 	public void valueChanged( final Object value, final Timestamp timestamp ) {
 		
 		if (!isDisposed()) getDisplay().syncExec( new Runnable() {
 
 			public void run() {
 				
 				if (!textWidget.isDisposed()){
 					textWidget.setForeground(Activator.getDefault().getColor("COLOR_PV_OK"));
 					textWidget.setText( value.toString() );
 				}
 			}
 		});
 	}
 
 	public void setText(String text){
 		textWidget.setText( text );
 	}
 
 	public void setFont(Font font){
 		textWidget.setFont( font );
 	}
 
 	public void dispose() {
 		IProcessVariableConnectionService service = ProcessVariableConnectionServiceFactory.getDefault().getProcessVariableConnectionService();
 		service.unregister( this );
 		super.dispose();
 	}
 }
