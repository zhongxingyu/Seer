 package de.ptb.epics.eve.editor.views.motoraxisview.positionlistcomposite;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.databinding.Binding;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.fieldassist.ControlDecoration;
 import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 import de.ptb.epics.eve.data.scandescription.Axis;
 import de.ptb.epics.eve.data.scandescription.axismode.PositionlistMode;
 import de.ptb.epics.eve.editor.views.motoraxisview.MotorAxisViewComposite;
 
 /**
  * <code>MotorAxisPositionlistComposite</code> is a 
  * {@link org.eclipse.swt.widgets.Composite} contained in the 
  * <code>MotorAxisView</code>. It allows entering position lists for 
  * motor axes using a position list as step function.
  * 
  * @author Hartmut Scherr
  * @author Marcus Michalsky
  */
 public class PositionlistComposite extends MotorAxisViewComposite implements
 		PropertyChangeListener {
 	
 	private static Logger LOGGER = 
 			Logger.getLogger(PositionlistComposite.class.getName());
 	
 	private PositionlistMode positionlistMode;
 	
 	private Label positionlistLabel;
 	private ControlDecoration positionlistLabelDecoration;
 	
 	private Text positionlistText;
 	private Binding positionlistBinding;
 	private IObservableValue positionlistModelObservable;
 	private IObservableValue positionlistGUIObservable;
 	
 	private Label positionCountLabel;
 	
 	private Image infoImage;
 	
 	/**
 	 * Constructs a <code>MotorAxisPositionlistComposite</code>.
 	 * 
 	 * @param parent the parent composite
 	 * @param style the style
 	 * @param parentView the view the composite is contained in
 	 */
 	public PositionlistComposite(final Composite parent, final int style) {
 		super(parent, style);
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 1;
 		this.setLayout(gridLayout);
 		
 		this.infoImage = FieldDecorationRegistry.getDefault().getFieldDecoration(
 				FieldDecorationRegistry.DEC_INFORMATION).getImage();
 		
 		// position list Label
 		this.positionlistLabel = new Label(this, SWT.NONE);
 		this.positionlistLabel.setText("Positionlist:");
 		this.positionlistLabelDecoration = new ControlDecoration(
 				positionlistLabel, SWT.TOP | SWT.RIGHT);
 		this.positionlistLabelDecoration.setImage(infoImage);
 		this.positionlistLabelDecoration.setDescriptionText(
 				"use , (comma) as delimiter");
 		this.positionlistLabelDecoration.show();
 		
 		// position list Text field 
 		this.positionlistText = new Text(this, SWT.BORDER | SWT.V_SCROLL);
 		GridData gridData = new GridData();
 		gridData.horizontalSpan = 2;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.horizontalIndent = 7;
 		this.positionlistText.setLayoutData(gridData);
 		this.positionlistText
 				.addFocusListener(new PositionlistTextFocusListener());
 
 		// position count label
 		this.positionCountLabel = new Label(this, SWT.NONE);
 		this.positionCountLabel.setText("0 positions");
 		
 		this.positionlistMode  = null;
 	} // end of: Constructor
 
 	/**
 	 * Calculates the height to see all entries of this composite
 	 * 
 	 * @return the needed height of Composite to see all entries
 	 */
 	public int getTargetHeight() {
 		return (positionCountLabel.getBounds().y + 
 				positionCountLabel.getBounds().height + 5);
 	}
 
 	/**
 	 * Calculates the width to see all entries of this composite
 	 * 
 	 * @return the needed width of Composite to see all entries
 	 */
 	public int getTargetWidth() {
 		return (positionCountLabel.getBounds().x + 
 				positionCountLabel.getBounds().width + 5);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setAxis(final Axis axis) {
 		if (axis == null) {
 			this.reset();
 			return;
 		}
 		if (!(axis.getMode() instanceof PositionlistMode)) {
 			LOGGER.warn("invalid axis mode");
 			return;
 		}
 		this.positionlistMode = (PositionlistMode)axis.getMode();
		this.positionlistMode.addPropertyChangeListener(
				PositionlistMode.POSITIONLIST_PROP, this);
 		this.createBinding();
		this.countPositions();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void createBinding() {
 		this.positionlistModelObservable = BeansObservables.observeValue(
 				this.positionlistMode, PositionlistMode.POSITIONLIST_PROP);
 		this.positionlistGUIObservable = SWTObservables.observeText(
 				this.positionlistText, SWT.Modify);
 		UpdateValueStrategy targetToModel = new UpdateValueStrategy(
 				UpdateValueStrategy.POLICY_UPDATE);
 		targetToModel.setAfterGetValidator(new PositionlistValidator());
 		UpdateValueStrategy modelToTarget = new UpdateValueStrategy(
 				UpdateValueStrategy.POLICY_UPDATE);
 		this.positionlistBinding = context.bindValue(positionlistGUIObservable,
 				positionlistModelObservable, targetToModel, modelToTarget);
 		ControlDecorationSupport.create(positionlistBinding, SWT.LEFT);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void reset() {
 		LOGGER.debug("reset");
 		if (this.positionlistMode != null) {
 			this.context.removeBinding(this.positionlistBinding);
 			this.positionlistBinding.dispose();
 			this.positionlistModelObservable.dispose();
 			this.positionlistGUIObservable.dispose();
			this.positionlistMode.removePropertyChangeListener(this);
 		}
 		this.positionlistMode = null;
 		this.positionCountLabel.setText("0 positions");
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals(PositionlistMode.POSITIONLIST_PROP)) {
 			this.countPositions();
 		}
 	}
 
 	/*
 	 * 
 	 */
 	private void countPositions() {
 		if(positionlistText.getText().isEmpty()) {
 			this.positionCountLabel.setText("0 positions");
 		} else {
 			int count = positionlistText.getText().split(",").length;
 			if(count == 1) {
 				this.positionCountLabel.setText("1 position");
 				LOGGER.debug("1 position");
 			} else {
 				this.positionCountLabel.setText(count + " positions");
 				if(LOGGER.isDebugEnabled()) {
 					LOGGER.debug(count + " positions");
 				}
 			}
 		}
 		this.positionCountLabel.getParent().layout();
 	}
 	
 	/* ******************************************************************** */
 	/* ************************** Listeners ******************************* */
 	/* ******************************************************************** */
 	
 	/**
 	 * @author Marcus Michalsky
 	 * @since 1.7
 	 */
 	private class PositionlistTextFocusListener implements FocusListener {
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusGained(FocusEvent e) {
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusLost(FocusEvent e) {
 			if (positionlistText.getText().isEmpty()) {
 				positionlistBinding.updateModelToTarget();
 			}
 		}
 	}
 }
