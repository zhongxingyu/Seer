 package net.sf.jmoney.entrytable;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 
 public abstract class RowControl<T, R extends RowControl<T,R>> extends Composite {
 
 	protected static final Color selectedCellColor = new Color(Display
 			.getCurrent(), 255, 255, 255);
 
 	protected abstract boolean commitChanges();
 	
 	protected abstract void setSelected(boolean isSelected);
 	
 	// Although currently the keys of this map are never used
 	// (and it may as well be a list of the values only), a map
 	// allows us to do stuff like move the focus to the control
 	// in error during transaction validation.
 	protected Map<CellBlock, ICellControl<? super T>> controls = new HashMap<CellBlock, ICellControl<? super T>>();
 
 	protected RowSelectionTracker<R> selectionTracker;
 	protected FocusCellTracker focusCellTracker;
 	
 	/**
 	 * the current input, being always a non-null value if this row
 	 * is active and undefined if this row is inactive 
 	 */
 	protected T input;
 	
 	public RowControl(Composite parent, int style) {
 		super(parent, style);
 
 		/*
 		 * By default the child controls get the same background as
 		 * this composite.
 		 */
 		setBackgroundMode(SWT.INHERIT_FORCE);
 	}
 
 	/**
 	 * This method must always be called by the constructor of the final derived
 	 * classes of this class.  Why do we not just call it from the constructor
 	 * of this class?  The reason is because the controls that are created by this method have a back reference to
 	 * this object.  These back references are typed (using generics) to the
 	 * final derived type.  These controls will expect field initializers and
 	 * possibly constructor initialization to have been done on the final derived type.
 	 * However, at the time the base constructor is called, neither will have
 	 * been initialized.
 	 * 
 	 * @param rootBlock
 	 * @param selectionTracker
 	 * @param focusCellTracker
 	 */
 	protected void init(R thisRowControl, Block<T, ? super R> rootBlock,
 			RowSelectionTracker<R> selectionTracker,
 			FocusCellTracker focusCellTracker) {
 		this.selectionTracker = selectionTracker;
 		this.focusCellTracker = focusCellTracker;
 		
 		for (CellBlock<? super T, ? super R> cellBlock: rootBlock.buildCellList()) {
 			// Create the control with no content set.
 			createCellControl(this, cellBlock);
 		}
 	}
 
 	/**
 	 * This method creates the controls.
 	 * 
 	 * This method is usually called from init to create the controls.  However, in the
 	 * case of the StackBlock, controls are created for the top block only which means if
 	 * the top block changes then controls may need to be created at a later time.  This method
 	 * should be called to create the controls in order to ensure that the controls are properly
 	 * adapted with the correct listeners and so on.
 	 * @param <R>
 	 * @param parent
 	 * @param thisRowControl
 	 * @param selectionTracker
 	 * @param focusCellTracker
 	 * @param cellBlock
 	 */
 	public void createCellControl(Composite parent,	CellBlock<? super T, ? super R> cellBlock) {
 		final ICellControl<? super T> cellControl = cellBlock.createCellControl(parent, getThis());
 		controls.put(cellBlock, cellControl);
 
 		if (input != null) {
 			cellControl.load(input);
 		}
 		
 		FocusListener controlFocusListener = new CellFocusListener<R>(getThis(), cellControl, selectionTracker, focusCellTracker);
 		
 		Control control = cellControl.getControl();
 //			control.addKeyListener(keyListener);
 		addFocusListenerRecursively(control, controlFocusListener);
 //			control.addTraverseListener(traverseListener);
 		
 		// This is needed in case more child controls are created at a
 		// later time.  This is not the cleanest code, but the UI for  these
 		// split entries may be changed at a later time anyway.
 		cellControl.setFocusListener(controlFocusListener);
 	}
 
 	protected abstract R getThis();
 
 	/**
 	 * Add listeners to each control.
 	 * 
 	 * @param control The control to listen to.
 	 */
 	protected void addFocusListenerRecursively(Control control, FocusListener listener) {
 		control.addFocusListener(listener);
 		
 		if (control instanceof Composite) {
 			Composite composite = (Composite) control;
 			for (int i = 0; i < composite.getChildren().length; i++) {
 				Control childControl = composite.getChildren()[i];
 				addFocusListenerRecursively(childControl, listener);
 			}
 		}
 	}
 	
 	/**
 	 * This version is called when the selection changed as a result
 	 * of the user clicking on a control in another row.  Therefore
 	 * we do not set the cell selection.
 	 */
 	public void arrive() {
 		setSelected(true);
 	}
 
	/**
	 * This method should be called whenever a row is to lose selection.
	 * It makes whatever changes are necessary to the display of the row
	 * and saves if necessary the data in the row.
	 * 
	 * @return true if this method succeeded (or failed in some way that
	 * 		is not the user's fault and that the user cannot correct), 
	 * 		false if this method could not save the data because the user
	 * 		has not properly entered the data and so selection should remain
	 * 		on the row (in which case this method will display an appropriate
	 * 		message to the user)
	 */
 	public boolean canDepart() {
 		if (!commitChanges()) {
 			return false;
 		}
 
 		setSelected(false);
 		return true;
 	}
 	
 	/**
 	 * This method is called when the selected row may not be visible (because the
 	 * user scrolled the table) but we want to make it visible again because there
 	 * was an error in it and we were unable to move the selection off the row.
 	 */
 	protected abstract void scrollToShowRow();
 	
 	
 }
