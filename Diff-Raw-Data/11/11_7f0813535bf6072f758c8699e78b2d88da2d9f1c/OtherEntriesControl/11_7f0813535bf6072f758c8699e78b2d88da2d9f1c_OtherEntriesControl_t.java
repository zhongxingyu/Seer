 package net.sf.jmoney.entrytable;
 
 
 import net.sf.jmoney.JMoneyPlugin;
 import net.sf.jmoney.model2.Entry;
 import net.sf.jmoney.model2.ExtendableObject;
 import net.sf.jmoney.model2.SessionChangeAdapter;
 import net.sf.jmoney.model2.SessionChangeListener;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StackLayout;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Layout;
 
 public class OtherEntriesControl extends Composite {
 
 	private Block<Entry, SplitEntryRowControl> rootBlock;
 	private RowSelectionTracker selectionTracker;
 	private FocusCellTracker focusCellTracker;
 	
 	/**
 	 * The composite containing whatever is to the left of
 	 * the drop-down button.  This has a StackLayout.
 	 */
 	private Composite childComposite;
 
 	private StackLayout stackLayout;
 	
 	/**
 	 * The label that is shown if this is a split entry.
 	 * This label is shown instead of all the fields for
 	 * properties that come from the other entry.
 	 */
 	private Label splitLabel;
 
 	/**
 	 * The composite that contains the fields for
 	 * properties that come from the other entry.
 	 * This composite is shown only if the entry is not split.
 	 */
 	private SplitEntryRowControl otherEntryControl;
 
 	/**
 	 * The small drop-down button to the right that shows
 	 * the split entry data.
 	 */
 	private Button downArrowButton;
 
 	private EntryData entryData;
 
 	private SessionChangeListener splitEntryListener = new SessionChangeAdapter() {
 
 		@Override
 		public void objectInserted(ExtendableObject newObject) {
 			if (newObject instanceof Entry
 					&& ((Entry)newObject).getTransaction() == entryData.getEntry().getTransaction()
 					&& entryData.getEntry().getTransaction().getEntryCollection().size() == 3) {
 				// Is now split but was not split before
 				stackLayout.topControl = splitLabel;
 				childComposite.layout(false);
 			}
 		}
 
 		@Override
 		public void objectRemoved(ExtendableObject deletedObject) {
 			if (deletedObject instanceof Entry
 					&& ((Entry)deletedObject).getTransaction() == entryData.getEntry().getTransaction()
 					&& deletedObject != entryData.getEntry()
 					&& entryData.getEntry().getTransaction().getEntryCollection().size() == 2) {
 				// Is now not split but was split before
 				otherEntryControl.setContent(entryData.getOtherEntry());
 				stackLayout.topControl = otherEntryControl;
 				childComposite.layout(false);
 			}
 		}
 	};
 	
 	static private Image downArrowImage = null;
 
 	public OtherEntriesControl(EntryRowControl parent, Block<Entry, SplitEntryRowControl> rootBlock, RowSelectionTracker selectionTracker, FocusCellTracker focusCellTracker) {
 		super(parent, SWT.NONE);
 		this.rootBlock = rootBlock;
 		this.selectionTracker = selectionTracker;
 		this.focusCellTracker = focusCellTracker;
 		
 		setLayout(new DropdownButtonLayout());
 		
 		Control childArea = createChildComposite();
 		Control dropDownButton = createDownArrowButton();
 	}
 
 	private Control createDownArrowButton() {
 		downArrowButton = new Button(this, SWT.NO_TRIM);
 		if (downArrowImage == null) {
 			ImageDescriptor descriptor = JMoneyPlugin.createImageDescriptor("icons/comboArrow.gif");
 			downArrowImage = descriptor.createImage();
 		}
 		downArrowButton.setImage(downArrowImage);
 
 		downArrowButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 		    public void widgetSelected(SelectionEvent event) {
 				final OtherEntriesShell shell = new OtherEntriesShell(getShell(), SWT.ON_TOP, entryData, rootBlock, true);
     	        Display display = getDisplay();
     	        Rectangle rect = display.map(OtherEntriesControl.this.getParent(), null, getBounds());
     	        shell.open(rect);
 			}
 		});
 		
 		return downArrowButton;
 	}
 
 	private Control createChildComposite() {
 		childComposite = new Composite(this, SWT.NONE);
 		
 		stackLayout = new StackLayout();
 		childComposite.setLayout(stackLayout);
 		
 		splitLabel = new Label(childComposite, SWT.NONE);
 		splitLabel.setText(JMoneyPlugin.getResourceString("SplitCategory.name"));
 
 		otherEntryControl = new SplitEntryRowControl(childComposite, SWT.NONE, rootBlock, true, selectionTracker, focusCellTracker);
 		
 		return childComposite;
 	}
 	
 	public void load(final EntryData entryData) {
 		// TODO: this should be done in a 'row release' method??
 		if (this.entryData != null) {
 			this.entryData.getEntry().getDataManager().removeChangeListener(splitEntryListener);
 		}
 		
 		this.entryData = entryData;
 		
 		if (entryData.getSplitEntries().size() == 1) {
 			otherEntryControl.setContent(entryData.getOtherEntry());
 			stackLayout.topControl = otherEntryControl;
 		} else {
 			stackLayout.topControl = splitLabel;
 		}
 		childComposite.layout(false);
 
 		// Listen for changes so this control is kept up to date.
 		entryData.getEntry().getDataManager().addChangeListener(splitEntryListener);
 	}
 
 	public void save() {
		/*
		 * The focus cell in our child table may not have been saved. Data in
		 * each cell is normally saved when another cell gets focus. However,
		 * there is a separate focus cell tracker for the cells inside this
		 * control and the cells outside.
		 */
		ICellControl cell = focusCellTracker.getFocusCell();
		if (cell != null) {
			cell.save();
		}
 	}
 
 	/**
 	 * Internal class for laying out this control.  There are two child
 	 * controls - the composite with the data for the other entries and
 	 * a drop-down button.
 	 */
 	private class DropdownButtonLayout extends Layout {
 	    @Override	
 		public void layout(Composite composite, boolean force) {
 			Rectangle bounds = composite.getClientArea();
 			childComposite.setBounds(0, 0, bounds.width-OtherEntriesBlock.DROPDOWN_BUTTON_WIDTH, bounds.height);
 			downArrowButton.setBounds(bounds.width-OtherEntriesBlock.DROPDOWN_BUTTON_WIDTH, 0, OtherEntriesBlock.DROPDOWN_BUTTON_WIDTH, bounds.height);
 		}
 
 	    @Override	
 		public Point computeSize(Composite composite, int wHint, int hHint, boolean force) {
 			/*
 			 * The button is always a fixed width.  Therefore we simply pass on to the contents,
 			 * after adjusting for the button width. 
 			 */
 			int contentsWidthHint = (wHint == SWT.DEFAULT) ? SWT.DEFAULT : wHint - OtherEntriesBlock.DROPDOWN_BUTTON_WIDTH; 
 			Point contentsSize = childComposite.computeSize(contentsWidthHint, hHint, force);
 			return new Point(contentsSize.x + OtherEntriesBlock.DROPDOWN_BUTTON_WIDTH, contentsSize.y);
 		}
 	}
 }
