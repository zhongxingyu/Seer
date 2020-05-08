 package de.ptb.epics.eve.editor.views;
 
 
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Combo;
 
 import de.ptb.epics.eve.data.scandescription.Axis;
 import de.ptb.epics.eve.data.scandescription.PositionMode;
 import de.ptb.epics.eve.data.scandescription.PluginController;
 import de.ptb.epics.eve.data.scandescription.ScanModule;
 import de.ptb.epics.eve.editor.Activator;
 
 /**
  * <code>MotorAxisView</code> is a composite to input the parameters
  * of a motor axis from a scanModul.
  * @author Hartmut Scherr
  *
  */
 public class MotorAxisView extends ViewPart {
 
 	public static final String ID = "de.ptb.epics.eve.editor.views.MotorAxisView";
 
 	private Composite top = null;
 	private ScrolledComposite sc = null;
 
 	private Label stepfunctionLabel;
 	private Combo stepFunctionCombo;
 	private Label stepfunctionErrorLabel;
 	
 	private Label positionModeLabel;
 	private Combo positionModeCombo;
 	private Label positionModeErrorLabel;
 	
 	private Composite holderComposite;
 	
 	private Axis axis;
 	private double stepamount;
 	private String[] stepfunctions;
 	
 	private ScanModule scanModul;
 
 	@Override
 	public void createPartControl( final Composite parent ) {
 		
 		parent.setLayout(new FillLayout());
 
 		if( Activator.getDefault().getMeasuringStation() == null ) {
 			final Label errorLabel = new Label( parent, SWT.NONE );
 			errorLabel.setText( "No Measuring Station has been loaded. Please check Preferences!" );
 			return;
 		}
 		this.stepfunctions = Activator.getDefault().getMeasuringStation().getSelections().getStepfunctions();
 		
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 3;
 		GridData gridData;
 		
		this.sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
 
 		this.top = new Composite( sc, SWT.NONE );
 		this.top.setLayout( gridLayout );
 
 		sc.setContent(this.top);
         sc.setExpandHorizontal(true);
         sc.setExpandVertical(true);
 
 		this.stepfunctionLabel = new Label( this.top, SWT.NONE );
 		this.stepfunctionLabel.setText( "Step function: " );
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.stepfunctionLabel.setLayoutData( gridData );
 		
 		this.stepFunctionCombo = new Combo( this.top, SWT.READ_ONLY );
 		this.stepFunctionCombo.setItems( this.stepfunctions );
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		this.stepFunctionCombo.setLayoutData( gridData );
 		this.stepFunctionCombo.addModifyListener( new StepFunctionComboModifyListener());
 		
 		this.stepfunctionErrorLabel = new Label( this.top, SWT.NONE );
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.stepfunctionErrorLabel.setLayoutData( gridData );
 		
 		this.positionModeLabel = new Label( this.top, SWT.NONE );
 		this.positionModeLabel.setText( "Position mode: " );
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.positionModeLabel.setLayoutData( gridData );
 		
 		this.positionModeCombo = new Combo( this.top, SWT.READ_ONLY );
 		this.positionModeCombo.setItems( PositionMode.getPossiblePositionModes() );
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		this.positionModeCombo.setLayoutData( gridData );
 		this.positionModeCombo.addModifyListener( new PositionModeComboModifyListener());
 		
 		this.positionModeErrorLabel = new Label( this.top, SWT.NONE );
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		this.positionModeErrorLabel.setLayoutData( gridData );
 		
 		this.holderComposite = new Composite( top, SWT.NONE );
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalSpan = 3;
 		this.holderComposite.setLayoutData( gridData );
 		
 		this.stepFunctionCombo.setEnabled( false );
 		this.positionModeCombo.setEnabled( false );
 
 	}
 
 	@Override
 	public void setFocus() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void setAxis( final Axis axis, final double stepamount ) {
 		this.axis = axis;
 		this.stepamount = stepamount;
 		if( this.axis != null ) {
 			this.setPartName( this.axis.getMotorAxis().getFullIdentifyer() );
 			this.stepFunctionCombo.setText( this.axis.getStepfunctionString() );
 			this.positionModeCombo.setText( PositionMode.typeToString( this.axis.getPositionMode() ) );
 			
 			this.stepFunctionCombo.setEnabled( true );
 			this.positionModeCombo.setEnabled( true );
 		} else {
 			this.setPartName( "Motor Axis View" );
 			this.holderComposite.dispose();
 			
 			this.holderComposite = new Composite( top, SWT.NONE );
 			GridData gridData = new GridData();
 			gridData.horizontalAlignment = GridData.FILL;
 			gridData.verticalAlignment = GridData.FILL;
 			gridData.grabExcessHorizontalSpace = true;
 			gridData.grabExcessVerticalSpace = true;
 			gridData.horizontalSpan = 2;
 			this.holderComposite.setLayoutData( gridData );
 			this.stepFunctionCombo.setText( "" );
 			this.positionModeCombo.setText( "" );
 			this.stepFunctionCombo.setEnabled( false );
 			this.positionModeCombo.setEnabled( false );
 		}
 	}
 
 	public void setAxis( final Axis axis, final double stepamount, final ScanModule scanModul ) {
 		this.scanModul = scanModul;
 		setAxis(axis, stepamount);
 	}
 
 	///////////////////////////////////////////////////////////
 	// Hier kommen jetzt die verschiedenen Listener Klassen
 	///////////////////////////////////////////////////////////
 
 	/**
 	 * <code>ModifyListener</code> of StepFunction Combo from
 	 * <code>MotorAxisView</code>
 	 */
 	class StepFunctionComboModifyListener implements ModifyListener {	
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modifyText(ModifyEvent e) {
 			if( axis != null ) {
 				axis.setStepfunction( stepFunctionCombo.getText() );
 				if( axis.getStepfunctionString().equals( "File" ) ) {
 					holderComposite.dispose();
 					holderComposite = new MotorAxisFileComposite( top, SWT.NONE );
 					((MotorAxisFileComposite)holderComposite).setAxis( axis );
 					GridData gridData = new GridData();
 					gridData.horizontalAlignment = GridData.FILL;
 					gridData.verticalAlignment = GridData.FILL;
 					gridData.grabExcessHorizontalSpace = true;
 					gridData.grabExcessVerticalSpace = true;
 					gridData.horizontalSpan = 3;
 					holderComposite.setLayoutData( gridData );
 					holderComposite.layout();
 					holderComposite.setVisible( true );
 					top.layout();
 					// setting minimum width and height of ScrolledComposite
 					int targetHeight = ((MotorAxisFileComposite)holderComposite).getTargetHeight() + holderComposite.getBounds().y;
 					int targetWidth = ((MotorAxisFileComposite)holderComposite).getTargetWidth() + holderComposite.getBounds().x;
 					sc.setMinSize(targetWidth, targetHeight);
 				} else if( axis.getStepfunctionString().equals( "Plugin" ) ) { 
 					if( axis.getPositionPluginController() == null ) {
 						axis.setPositionPluginController( new PluginController() );
 					}
 					holderComposite.dispose();
 					holderComposite = new MotorAxisPluginComposite( top, SWT.NONE );
 					((MotorAxisPluginComposite)holderComposite).setAxis( axis, scanModul );
 					GridData gridData = new GridData();
 					gridData.horizontalAlignment = GridData.FILL;
 					gridData.verticalAlignment = GridData.FILL;
 					gridData.grabExcessHorizontalSpace = true;
 					gridData.grabExcessVerticalSpace = true;
 					gridData.horizontalSpan = 3;
 					holderComposite.setLayoutData( gridData );
 					holderComposite.layout();
 					holderComposite.setVisible( true );
 					top.layout();
 					// setting minimum width and height of ScrolledComposite
 					int targetHeight = ((MotorAxisPluginComposite)holderComposite).getTargetHeight() + holderComposite.getBounds().y;
 					int targetWidth = ((MotorAxisPluginComposite)holderComposite).getTargetWidth() + holderComposite.getBounds().x;
 					sc.setMinSize(targetWidth, targetHeight);
 				} else if( axis.getStepfunctionString().equals( "Positionlist" ) ) {
 					holderComposite.dispose();
 					holderComposite = new MotorAxisPositionlistComposite( top, SWT.NONE );
 					((MotorAxisPositionlistComposite)holderComposite).setAxis( axis );
 					GridData gridData = new GridData();
 					gridData.horizontalAlignment = GridData.FILL;
 					gridData.verticalAlignment = GridData.FILL;
 					gridData.grabExcessHorizontalSpace = true;
 					gridData.grabExcessVerticalSpace = true;
 					gridData.horizontalSpan = 3;
 					holderComposite.setLayoutData( gridData );
 					holderComposite.layout();
 					holderComposite.setVisible( true );
 					top.layout();
 					// setting minimum width and height of ScrolledComposite
 					int targetHeight = ((MotorAxisPositionlistComposite)holderComposite).getTargetHeight() + holderComposite.getBounds().y;
 					int targetWidth = ((MotorAxisPositionlistComposite)holderComposite).getTargetWidth() + holderComposite.getBounds().x;
 					sc.setMinSize(targetWidth, targetHeight);
 				} else {
 					holderComposite.dispose();
 					holderComposite = new MotorAxisStartStopStepwidthComposite( top, SWT.NONE );
 					((MotorAxisStartStopStepwidthComposite)holderComposite).setAxis( axis, stepamount );
 					GridData gridData = new GridData();
 					gridData.horizontalAlignment = GridData.FILL;
 					gridData.verticalAlignment = GridData.FILL;
 					gridData.grabExcessHorizontalSpace = true;
 					gridData.grabExcessVerticalSpace = true;
 					gridData.horizontalSpan = 3;
 					holderComposite.setLayoutData( gridData );
 					holderComposite.layout();
 					holderComposite.setVisible( true );
 					top.layout();
 					// setting minimum width and height of ScrolledComposite
 					int targetHeight = ((MotorAxisStartStopStepwidthComposite)holderComposite).getTargetHeight() + holderComposite.getBounds().y;
 					int targetWidth = ((MotorAxisStartStopStepwidthComposite)holderComposite).getTargetWidth() + holderComposite.getBounds().x;
 					sc.setMinSize(targetWidth, targetHeight);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * <code>ModifyListener</code> of StepFunction Combo from
 	 * <code>MotorAxisView</code>
 	 */
 	class PositionModeComboModifyListener implements ModifyListener {	
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void modifyText(ModifyEvent e) {
 			if( axis != null ) {
 				axis.setPositionMode(PositionMode.stringToType(positionModeCombo.getText()));
 			}
 		}
 	}
 	
 }  //  @jve:decl-index=0:visual-constraint="-17,-49,363,149"
