 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.ui.viewer;
 
 import gda.device.DeviceException;
 import gda.device.Scannable;
 import gda.device.ScannableMotionUnits;
 
 import java.text.DecimalFormat;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Layout;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.gda.richbeans.components.EventListenersDelegate;
 import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
 import uk.ac.gda.richbeans.event.ValueAdapter;
 import uk.ac.gda.richbeans.event.ValueEvent;
 import uk.ac.gda.richbeans.event.ValueListener;
 import uk.ac.gda.ui.internal.viewer.ScannablePositionSource;
 import uk.ac.gda.ui.internal.viewer.ScannableRotationSource;
 
 /**
  * A concrete viewer that displays position information about an underlying rotating motor.
  * Users may use the viewer to change position of the motor, in which case this 
  * viewer displays the target position as well as the updating position.
  * <p>
  * This class is designed to be instantiated with a pre-existing{@link Scannable}
  * which supplies the position information. The viewer registers a listener to receive
  * position updates from the underlying scannable. 
  * </p>
  * This viewer provides an option to display shortcut buttons for +/- a big step and little
  * step buttons. By default these are not shown, but can be configured by calling
  * <code>configureFixedStepButtons</code>
  * <p>
  * <dl>
  * <dt><b>Optional Styles (depending on ui configuration):</b></dt>
  * <dd>SINGLE</dd>
  * </dl>
  * This class is  not intended to be subclassed outside the viewer framework.
  * </p>
  * 
  * <p>There are 2 or 3 parts to this component:</p>
  * <ol>
  * <li>motor position viewer (name label and position text box)</li>
  * <li>(optional) 2×2 grid of fixed step buttons</li>
  * <li>nudge controls (+/- buttons, size box, and optional label)</li>
  * </ol>
  * 
  * <p>There are 3 ways to configure it:</p>
  * <ol>
  * <li>show fixed steps, or not</li>
  * <li>SWT.SINGLE style, or SWT.NONE</li>
  * <li>single line layout, or not</li>
  * </ol>
  * 
  * <p>"Show fixed steps" determines whether the fixed step buttons are shown.</p>
  * 
  * <p>The style specifies whether the nudge buttons should be on one line or not.</p>
  * <ul>
  * <li>Specifying SWT.NONE will create a 2×2 grid (+, -, "Size" label, nudge size text box).</li>
  * <li>Specifying SWT.SINGLE will create a 3×1 grid (-, +, nudge size text box). (This only takes effect if "show fixed
  *     steps" is false. If "show fixed steps" is true, the 2×2 grid of buttons is shown, which is 2 rows high, so
  *     there's no point collapsing the nudge buttons onto one line.)</li>
  * </ul>
  * 
  * <p>"Single line layout" controls whether the motor position viewer is on its own line or not. If set to false, the
  * (optional) fixed step buttons and nudge controls will be on their own row.</p>
  */
 public class RotationViewer {
 	private static final Logger logger = LoggerFactory.getLogger(RotationViewer.class);
 	
 	private IRotationSource motor;
 	private Scannable scannable;
 	
 	private boolean showFixedSteps;
 	private boolean showResetToZero;
 	private boolean singleLineLayout = false;
 	private double standardStep;
 	private double littleStep;
 	private double bigStep;
 	private String motorLabel;
 
 	private ScaleBox nudgeSizeBox;
 	
 	private Button plusBigButton;
 	private Button plusLittleButton;
 	private Button minusBigButton;
 	private Button minusLittleButton;
 	private Button posNudgeButton;
 	private Button negNudgeButton;
 	private Button resetToZeroButton;
 
 	private MotorPositionViewer motorPositionViewer;
 	
 	private EventListenersDelegate valueEventDelegate;
 
 	private static final int ACCEPTED_STYLES = SWT.SINGLE;
 	
 	/**
 	 * Creates a new rotation viewer for the given scannable.
 	 * 
 	 * @param scannable the scannable for this viewer
 	 */
 	public RotationViewer (Scannable scannable){	
 		this(scannable, 10.0);
 	}
 	/**
 	 * Creates a new rotation viewer for the given scannable.
 	 * 
 	 * @param scannable the scannable for this viewer
 	 */
 	public RotationViewer (Scannable scannable, double stepSize){	
 		if (scannable instanceof ScannableMotionUnits) {
 			this.motor = new ScannableRotationSource((ScannableMotionUnits)scannable);
 		}
 		this.scannable = scannable;
 		this.showFixedSteps = false;
 		this.standardStep = stepSize;
 	}
 	
 	public RotationViewer(Scannable scannable, String motorLabel) {
 		this(scannable, motorLabel, false);
 	}
 	
 	public RotationViewer(Scannable scannable, String motorLabel, boolean showResetToZero) {
 		this(scannable);
 		this.motorLabel = motorLabel;
 		this.showResetToZero = showResetToZero;
 	}
 	
 	/**
 	 * Configure the standard stepSize for the nudge buttons. 
 	 * This method should only be called if a different default is required.
 	 * This method must be called before invoking <code>createControls</code>
 	 * @param stepSize default stepSize
 	 */
 	public void configureStandardStep(double stepSize){
 		this.standardStep = stepSize;		
 	}
 	/**
 	 * Show shortcut step buttons for a fixed size small step and a 
 	 * fixed size big step. This method must be called before invoking <code>createControls</code>
 	 * <p>
 	 * Adds four additional buttons to this viewer. Two for +ve and -ve
 	 * small step and two for +ve and -ve big step.
 	 * <p>
 	 * @param smallStep value of small step size, ignored if enable is false
 	 * @param bigStep value of big step size, ignored if enable is true
 	 */
 	public void configureFixedStepButtons(double smallStep, double bigStep){
 		this.showFixedSteps = true;
 		this.littleStep = smallStep;
 		this.bigStep = bigStep;
 	}
 	
 	/**
 	 * Creates the UI elements for this viewer
 	 * <p>
 	 * The number of columns in the parent layout will influence the layout of this viewer.
 	 * <p>
 	 * @param parent
 	 *            the parent composite
 	 * @param style
 	 *           supported styles 
 	 * <UL>
 	 * <LI>NONE - default style,step buttons are displayed in 2 rows </LI>
 	 * <LI>SINGLE - step buttons displayed in 1 row, provided configureFixedStepButtons has not been set</LI>
 	 * </UL>	 *           
 	 */
 	public void createControls(Composite parent, int style, Layout rotationGroupLayout, Layout motorPositionLayout, Object labelLayoutData){	
 
 		createWidgets(parent, checkStyle(style),rotationGroupLayout, motorPositionLayout, labelLayoutData);
 
 		nudgeSizeBox.setUnit(motor.getDescriptor().getUnit());
 
 		nudgeSizeBox.setValue(standardStep);
 		nudgeSizeBox.on();
 		addNudgeListeners();
 		
 		if (showFixedSteps){
 			addFixedStepButtonsListeners();
 		}
 
 		motorPositionViewer.addValueListener(new ValueAdapter("Rotation Viewer" + this.toString()) {
 			
 			@Override
 			public void valueChangePerformed(ValueEvent e) {
 				notifyListenersOfMove(e);
 			}
 		});
 
 		valueEventDelegate = new EventListenersDelegate();
 	}
 
 	protected void notifyListenersOfMove(ValueEvent e) {
 		valueEventDelegate.notifyValueListeners(e);
 	}
 	
 	/**
 	 * @see EventListenersDelegate#addValueListener(ValueListener)
 	 */
 	public void addValueListener(final ValueListener l) {
 		valueEventDelegate.addValueListener(l);
 	}
 	
 	/**
 	 * @see EventListenersDelegate#removeValueListener(ValueListener)
 	 */
 	public void removeValueListener(final ValueListener l) {
 		valueEventDelegate.removeValueListener(l);
 	}
 	
 	/**
 	 * 
 	 * @param parent
 	 * @param style
 	 * @param singleLineLayout - if true the controls are put into a single line
 	 * @param rotationGroupLayout - layout for top level composite that holds the rest of the controls
 	 * @param motorPositionLayout - layout of the motorPostion Viewer
 	 * @param labelLayoutData - layoutData ( e.g. GridData if using GridLayout) for the label of the motorPositionViewer 
 	 */
 	public void createControls(Composite parent, int style,boolean singleLineLayout, 
 			Layout rotationGroupLayout, Layout motorPositionLayout, Object labelLayoutData) {
 		this.singleLineLayout = singleLineLayout;
 		createControls(parent,style, rotationGroupLayout, motorPositionLayout, labelLayoutData);
 	}
 
 	
 	public void createControls(Composite parent, int style){
 		createControls(parent, style, null, null, null);
 	}
 	/**
 	 * As createControls(Composite,int) except that the buttons appear on the same line as the textfield when
 	 * singleLineLayout is set to true.
 	 * 
 	 * @param parent
 	 * @param style
 	 * @param singleLineLayout
 	 */
 	public void createControls(Composite parent, int style,boolean singleLineLayout) {
 		createControls(parent,style, singleLineLayout, null, null, null);
 	}
 
 	
 	private static int checkStyle(int style) {
 		if ((style & ~ACCEPTED_STYLES) != 0)
 			throw new IllegalArgumentException(
 					"Invalid style being set on RotationViewer"); //$NON-NLS-1$
 		return style;
 	}
 	
 	/**
 	 * Add the +/- button listeners
 	 */
 	private void addNudgeListeners() {	
 		posNudgeButton.addSelectionListener(new SelectionAdapter(){
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				Double nudge = nudgeSizeBox.getNumericValue();
 				if(!nudge.isNaN())
 					moveMotor(true,nudge);
 			}
 		});	
 		
 		negNudgeButton.addSelectionListener(new SelectionAdapter(){
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				Double nudge = nudgeSizeBox.getNumericValue();
 				if(!nudge.isNaN())
 					moveMotor(false,nudge);
 			}
 		});
 	}
 
 	/**
 	 * Add the fixed step button listeners
 	 */
 	private void addFixedStepButtonsListeners() {
 		plusBigButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				moveMotor(true,bigStep);
 			}
 		});
 		
 		plusLittleButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				moveMotor(true,littleStep);
 			}
 		});
 		
 		minusBigButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				moveMotor(false,bigStep);
 			}
 		});
 		
 		minusLittleButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				moveMotor(false,littleStep);
 			}
 		});		
 	}
 
 	private void moveMotor(final boolean dir, final double step){
 		final String msg = "Moving " + motor.getDescriptor().getLabelText() + " by " + step;
 		final double targetVal = calculateTargetPosition(dir, step);
 		motorPositionViewer.getDemandBox().setNumericValue(targetVal);
 		motorPositionViewer.getDemandBox().demandBegin(targetVal);
 		
 		ValueEvent event = new ValueEvent(this,scannable.getName());
 		event.setDoubleValue(targetVal);
 		event.setFieldName(motorLabel);
 		
 		notifyListenersOfMove(event);
 
 		Job job = new Job(msg){
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				try {
 					((ScannablePositionSource)motor).setPosition(targetVal);
 				} catch (DeviceException e) {
 					logger.error("Exception when " + msg + ":" + e.getMessage(),e);
 				}
 				return Status.OK_STATUS; 
 			}			
 		};
 		job.setUser(true);
 		job.schedule();		
 	}
 
 	/**
 	 * Utility function to calculate the target position
 	 * @param dir direction of requested move, true is +ve, false is -ve
 	 * @param step amount to move by
 	 * @return expected target position
 	 */
 	private double calculateTargetPosition(boolean dir, double step){
 		double target=0.0;
 		try {
 			if (dir){
 				target= motor.calcMovePlusRelative(step);
 			} else{
 				target= motor.calcMoveMinusRelative(step);
 			}
 		} catch (DeviceException e1) {
 			logger.error("Error setting current value of demandBox", e1);
 		} 
 		return target;
 	}
 	
 	/**
 	 * Create widgets
 	 * @param parent composite
 	 * @param style 
 	 */
 	private void createWidgets(Composite parent, int style, Layout rotationGroupLayout, Layout motorPositionLayout, Object labelLayoutData) {
 		
 		final boolean DEBUG_LAYOUT = false;
 		
 		final boolean singleLineNudgeControls = ((style & SWT.SINGLE) != 0);
 		
 		final Composite rotationGroup = new Composite(parent, SWT.NONE);
 		int numColumns = singleLineLayout ? 2 : 1;
 		if( rotationGroupLayout == null){
 			rotationGroupLayout = GridLayoutFactory.swtDefaults().numColumns(numColumns).equalWidth(false).margins(1, 1).spacing(2, 2).create();
 		}
 		rotationGroup.setLayout(rotationGroupLayout);
 		
 		if (DEBUG_LAYOUT) {
 			rotationGroup.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
 		}
 		
 		if (motorLabel == null)
 			motorLabel = scannable.getName();
 		
 		{
 			if( motorPositionLayout == null){
 				motorPositionLayout = GridLayoutFactory.swtDefaults().numColumns(3).margins(1,1).spacing(2,2).create();
 			}
 			
 			Composite motorPositionContainer = new Composite(rotationGroup, SWT.NONE);
 			GridDataFactory.fillDefaults().applyTo(motorPositionContainer);
 			motorPositionContainer.setLayout(motorPositionLayout);
 			motorPositionViewer = new MotorPositionViewer(motorPositionContainer, scannable, motorLabel, false, labelLayoutData);
 			if (DEBUG_LAYOUT) {
 				motorPositionContainer.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
 			}
 		}
 		
 		
 		final Composite otherControls = new Composite(rotationGroup, SWT.NONE);
 		GridLayoutFactory.swtDefaults().margins(0,0).spacing(0,0).applyTo(otherControls);
 		GridDataFactory.fillDefaults().applyTo(otherControls);
 		
 		if (DEBUG_LAYOUT) {
 			otherControls.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
 		}
 		
 		
 		GridData data = new GridData();
 		data.widthHint = 60;
 		data.horizontalAlignment = GridData.CENTER;	
 		
		if (showFixedSteps){
 			Composite buttonGroup = new Composite(otherControls, SWT.NONE);
 			GridLayoutFactory.swtDefaults().numColumns(2).margins(1,1).spacing(2,2).applyTo(buttonGroup);
 			
 			if (DEBUG_LAYOUT) {
 				buttonGroup.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
 			}
 			
			DecimalFormat df = new DecimalFormat("###");
			
 			if (showResetToZero) {
 				GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
 				gridData.horizontalSpan = 2;
 				resetToZeroButton = createButton(buttonGroup, "Move to zero", null, gridData);
 				
 				resetToZeroButton.addSelectionListener(new SelectionListener() {
 					
 					@Override
 					public void widgetSelected(SelectionEvent e) {
 						final String msg = "Moving " + motor.getDescriptor().getLabelText() + " to 0.0";
 						motorPositionViewer.getDemandBox().setNumericValue(0);
 						motorPositionViewer.getDemandBox().demandBegin(0);
 
 						Job job = new Job(msg){
 							@Override
 							protected IStatus run(IProgressMonitor monitor) {
 								try {
 									((ScannablePositionSource)motor).setPosition(0.0);
 								} catch (DeviceException e) {
 									logger.error("Exception when " + msg + ":" + e.getMessage(),e);
 								}
 								return Status.OK_STATUS; 
 							}			
 						};
 						job.setUser(true);
 						job.schedule();
 					}
 					
 					@Override
 					public void widgetDefaultSelected(SelectionEvent e) {}
 				});
 			}
 			
			minusLittleButton  = createButton(buttonGroup, "-"+df.format(littleStep), null, data);
			plusLittleButton = createButton(buttonGroup, "+"+df.format(littleStep), null, data);
			minusBigButton  = createButton(buttonGroup, "-"+df.format(bigStep), null, data);
			plusBigButton  = createButton(buttonGroup, "+"+df.format(bigStep), null, data);
 		}
 		
 		Composite inOutButtonsComp = new Composite(otherControls, SWT.NONE);
 		
 		if (singleLineNudgeControls) {
 			
 			GridLayoutFactory.swtDefaults().numColumns(3).equalWidth(false).margins(1,1).spacing(2,2).applyTo(inOutButtonsComp);
 			
 			data.widthHint = 40;
 			negNudgeButton  = createButton(inOutButtonsComp, "-", null, data);
 			posNudgeButton  = createButton(inOutButtonsComp, "+", null, data);
 			nudgeSizeBox = new ScaleBox(inOutButtonsComp, SWT.NONE);
 			GridDataFactory.fillDefaults().align(GridData.END, GridData.CENTER).applyTo(nudgeSizeBox);
 		} else {
 			GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).margins(1,1).spacing(2,2).applyTo(inOutButtonsComp);
 			data.widthHint = 50;
 			negNudgeButton  = createButton(inOutButtonsComp, "-", null, data);
 			posNudgeButton  = createButton(inOutButtonsComp, "+", null, data);
 			
 			Composite sizeComposite = new Composite(inOutButtonsComp, SWT.NONE);
 			GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).margins(1,1).spacing(2,2).applyTo(sizeComposite);
 			GridDataFactory.swtDefaults().span(2, 1).grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(sizeComposite);
 			
 			if (DEBUG_LAYOUT) {
 				sizeComposite.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_MAGENTA));
 			}
 			
 			Label nudgeSizeLabel = new Label(sizeComposite, SWT.NONE);
 			nudgeSizeLabel.setText("Size");
 			nudgeSizeBox = new ScaleBox(sizeComposite, SWT.NONE);
 			GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.CENTER).applyTo(nudgeSizeLabel);
 			GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(nudgeSizeBox);
 		}
 		nudgeSizeBox.setDecimalPlaces(nudgeSizeBoxDecimalPlaces);
 	}
 	
 	/**
 	 * Utility method for creating buttons
 	 * @param nudgeButtons
 	 * @param text button text, or null if none
 	 * @param image button image, or null if no image
 	 * @param datac GridData to apply (a copy of this object will be used)
 	 * @return the newly created button
 	 */
 	protected static Button createButton(Composite nudgeButtons, String text, final Image image, GridData datac) {
 		final Button button =new Button(nudgeButtons, SWT.PUSH);
 		
 		if (text != null) button.setText(text);
 		if (image != null){
 			button.setImage(image);		
 			button.addDisposeListener(new DisposeListener() {
 				@Override
 				public void widgetDisposed(DisposeEvent e) {
 					button.dispose();	
 				}
 			});			
 		}
 		GridDataFactory.createFrom(datac).applyTo(button);
 		return button;
 	}
 	
 	private int nudgeSizeBoxDecimalPlaces = 4;
 	
 	/**
 	 * Set the number of decimal places displayed by the nudge box
 	 * @param decimalPlaces
 	 */
 	public void setNudgeSizeBoxDecimalPlaces(int decimalPlaces) {
 		this.nudgeSizeBoxDecimalPlaces = decimalPlaces;
 		
 		if (nudgeSizeBox != null) {
 			nudgeSizeBox.setDecimalPlaces(decimalPlaces);
 		}
 	}
 	
 	public void setRestoreValueWhenFocusLost(boolean restoreValueWhenFocusLost) {
 		this.motorPositionViewer.setRestoreValueWhenFocusLost(restoreValueWhenFocusLost);
 	}
 	
 	/**
 	 * Set the number of decimal places displayed by the motorpositionviewer
 	 * @param decimalPlaces
 	 */
 	public void setMotorPositionViewerDecimalPlaces(int decimalPlaces) {
 		if (motorPositionViewer == null) {
 			throw new IllegalStateException("Cannot set decimal places for this RotationViewer's MotorPositionViewer - widgets have not been created. Call createControls first");
 		}
 		
 		motorPositionViewer.setDecimalPlaces(decimalPlaces);
 	}
 	
 	public void setEnabled(boolean enabled) {
 		nudgeSizeBox.setEnabled(enabled);
 
 		if (plusBigButton != null) {
 			plusBigButton.setEnabled(enabled);
 			plusLittleButton.setEnabled(enabled);
 			minusBigButton.setEnabled(enabled);
 			minusLittleButton.setEnabled(enabled);
 		}
 		if (posNudgeButton != null) {
 			posNudgeButton.setEnabled(enabled);
 			negNudgeButton.setEnabled(enabled);
 		}
 		if (resetToZeroButton != null)
 			resetToZeroButton.setEnabled(enabled);
 		if (motorPositionViewer != null)
 			motorPositionViewer.setEnabled(enabled);
 	}
 	
 	public void dispose() {
 		nudgeSizeBox.dispose();
 		valueEventDelegate.dispose();
 
 		if (plusBigButton != null) {
 			plusBigButton.dispose();
 			plusLittleButton.dispose();
 			minusBigButton.dispose();
 			minusLittleButton.dispose();
 		}
 		if (posNudgeButton != null) {
 			posNudgeButton.dispose();
 			negNudgeButton.dispose();
 		}
 		if (resetToZeroButton != null)
 			resetToZeroButton.dispose();
 		if (motorPositionViewer != null)
 			motorPositionViewer.dispose();
 	}
 }
