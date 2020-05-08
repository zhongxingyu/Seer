 /*
  * Created on Jan 17, 2005
  *
  * TODO To change the template for this generated file go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 package org.eclipse.jst.j2ee.internal;
 
 import java.util.Arrays;
 
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTableViewer;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jst.j2ee.application.internal.operations.ClassPathSelection;
 import org.eclipse.jst.j2ee.application.internal.operations.ClasspathElement;
 import org.eclipse.jst.j2ee.internal.common.ClasspathModel;
 import org.eclipse.jst.j2ee.internal.listeners.IValidateEditListener;
 import org.eclipse.jst.j2ee.internal.project.J2EEComponentUtilities;
 import org.eclipse.jst.j2ee.internal.wizard.AvailableJarsProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Listener;
 
 /**
  * @author jialin
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Style - Code Templates
  */
 public class ClasspathTableManager implements Listener, ICommonManifestUIConstants {
 
 	protected Button useClientJARsBtn;
 	protected Button useServerJARsBtn;
 	protected Button useAnyJARsBtn;
 	protected Button upButton;
 	protected Button downButton;
 	protected Button selectAllButton;
 	protected Button deselectAllButton;
 	protected IClasspathTableOwner owner;
 	protected Composite buttonColumn;
 	protected ClasspathModel model;
 	protected CheckboxTableViewer availableJARsViewer;
 	protected IValidateEditListener validateEditListener;
 	protected boolean isWLPEntry;
 	protected Group radioGroup;
 	
 	protected boolean readOnly;
 	protected AvailableJarsProvider availableJarsProvider;
 
 	/**
 	 * Constructor for ButtonBarManager.
 	 */
 	public ClasspathTableManager(IClasspathTableOwner owner, ClasspathModel model) {
 		super();
 		this.owner = owner;
 		this.model = model;
 	}
 	
 	public ClasspathTableManager(IClasspathTableOwner owner, ClasspathModel model, IValidateEditListener listener) {
 		super();
 		this.owner = owner;
 		this.model = model;
 		this.validateEditListener = listener;
 	}
 	public void fillComposite(Composite parent) {
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		layout.marginHeight = 0;
 		parent.setLayout(layout);
 		parent.setLayoutData(new GridData(GridData.FILL_BOTH));
 		createRadioGroup(parent);
 		createTable(parent);
 		createButtonColumn(parent);
 	}
 	
 	private void initializeEJBClientDefaults() {
 		if (model == null || model.getClassPathSelection() == null)
 			return;
 		boolean shouldUseServerJARs = false;
 		ClassPathSelection selection = model.getClassPathSelection(); 
 		int filterLevel;
 		boolean ejbSelected = selection.isAnyEJBJarSelected();
 		boolean clientSelected = selection.isAnyEJBClientJARSelected();
 		if (ejbSelected && clientSelected)
 			filterLevel = ClassPathSelection.FILTER_NONE;
 		else if (shouldUseServerJARs) {
 			if (clientSelected)
 				filterLevel = ClassPathSelection.FILTER_NONE;
 			else
 				filterLevel = ClassPathSelection.FILTER_EJB_CLIENT_JARS;
 		} else {
 			if (ejbSelected)
 				filterLevel = ClassPathSelection.FILTER_NONE;
 			else
 				filterLevel = ClassPathSelection.FILTER_EJB_SERVER_JARS;
 		}
 		initFilterLevel(filterLevel);
 	}
 	
 	private void initFilterLevel(int filterLevel) {
 		model.getClassPathSelection().setFilterLevel(filterLevel);
 		switch (filterLevel) {
 			case (ClassPathSelection.FILTER_NONE) :
 				useAnyJARsBtn.setSelection(true);
 				useClientJARsBtn.setSelection(false);
 				useServerJARsBtn.setSelection(false);
 				break;
 			case (ClassPathSelection.FILTER_EJB_CLIENT_JARS) :
 				useAnyJARsBtn.setSelection(false);
 				useClientJARsBtn.setSelection(false);
 				useServerJARsBtn.setSelection(true);
 				break;
 			case (ClassPathSelection.FILTER_EJB_SERVER_JARS) :
 				useAnyJARsBtn.setSelection(false);
 				useClientJARsBtn.setSelection(true);
 				useServerJARsBtn.setSelection(false);
 				break;
 		}
 	}
 
 	private void createRadioGroup(Composite parent) {
 		radioGroup = owner.createGroup(parent);
 		radioGroup.setText(EJB_CLIENT_RADIO_UI_);
 		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL);
 		data.horizontalSpan = 2;
 		data.horizontalIndent = 0;
 		GridLayout layout = new GridLayout(3, false);
 		radioGroup.setLayout(layout);
 		radioGroup.setLayoutData(data);
 		
 		useServerJARsBtn = createRadioButton(USE_EJB_SERVER_JARs_UI_, radioGroup);
 		useClientJARsBtn = createRadioButton(USE_EJB_CLIENT_JARs_UI_, radioGroup);
 		useAnyJARsBtn	 = createRadioButton(USE_BOTH_UI_, radioGroup);
 		initializeEJBClientDefaults();
 	}
 	
 
 	protected void createButtonColumn(Composite parent) {
 		buttonColumn = owner.createButtonColumnComposite(parent);
 		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END);
 		buttonColumn.setLayoutData(data);
 		createPushButtons();
 	}
 	
 	protected void createTable(Composite parent) {
 		availableJARsViewer = owner.createAvailableJARsViewer(parent);
 		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.FILL_VERTICAL);
 		availableJARsViewer.getTable().setLayoutData(gd);
 		availableJarsProvider = new AvailableJarsProvider();
 		availableJARsViewer.setContentProvider(availableJarsProvider);
 		availableJARsViewer.setLabelProvider(availableJarsProvider);
 		addTableListeners();
 	}
 		
 
 	protected void createPushButtons() {
 		upButton = createPushButton(UP_BUTTON);
 		downButton = createPushButton(DOWN_BUTTON);
 		selectAllButton = createPushButton(SELECT_ALL_BUTTON);
 		deselectAllButton = createPushButton(DE_SELECT_ALL_BUTTON);
 		if (isReadOnly()) {
 			upButton.setEnabled(false);
 			downButton.setEnabled(false);
 			selectAllButton.setEnabled(false);
 			deselectAllButton.setEnabled(false);
 		}
 	}
 
 	protected Button createPushButton(String label) {
 		Button aButton = owner.primCreatePushButton(label, buttonColumn);
 		aButton.addListener(SWT.Selection, this);
 		aButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		return aButton;
 	}
 	
 	protected Button createRadioButton(String label, Composite parent) {
 		Button aButton = owner.primCreateRadioButton(label, parent);
 		aButton.addListener(SWT.Selection, this);
 		return aButton;
 	}
 	/**
 	 * @see Listener#handleEvent(Event)
 	 */
 	public void handleEvent(Event event) {
 		if (event.widget == upButton)
 			upButtonSelected();
 		else if (event.widget == downButton)
 			downButtonSelected();
 		else if (event.widget == selectAllButton)
 			selectAllButtonSelected();
 		else if (event.widget == deselectAllButton)
 			deselectAllButtonSelected();
 		else if (event.widget == useServerJARsBtn)
 			handleServerJARsButtonSelected();
 		else if (event.widget == useClientJARsBtn)
 			handleClientJARsButtonSelected();
 		else if (event.widget == useAnyJARsBtn)
 			handleAnyJARsButtonSelected();
 	}
 	
 	private void handleServerJARsButtonSelected() {
 		model.selectFilterLevel(ClassPathSelection.FILTER_EJB_CLIENT_JARS);
 		refresh();
 	}
 
 	private void handleClientJARsButtonSelected() {
 		model.selectFilterLevel(ClassPathSelection.FILTER_EJB_SERVER_JARS);
 		refresh();
 	}
 
 	private void handleAnyJARsButtonSelected() {
 		model.selectFilterLevel(ClassPathSelection.FILTER_NONE);
 		refresh();
 	}
 
 	public boolean validatateEdit() {
 		return validateEditListener.validateState().isOK();
 	}
 
 	protected void deselectAllButtonSelected() {
 		if (!validatateEdit())
 			return;
 		availableJARsViewer.setAllChecked(false);
 		model.setAllClasspathElementsSelected(false);
 	}
 
 	protected void selectAllButtonSelected() {
 		if (!validatateEdit())
 			return;
 		availableJARsViewer.setAllChecked(true);
 		model.setAllClasspathElementsSelected(Arrays.asList(availableJARsViewer.getCheckedElements()), true);
 	}
 
 	protected java.util.List getSelectionAsList() {
 		return ((IStructuredSelection) availableJARsViewer.getSelection()).toList();
 	}
 
 	protected void downButtonSelected() {
 		if (!validatateEdit())
 			return;
 		model.moveDown(getSelectionAsList());
 		refresh();
 	}
 
 	protected void upButtonSelected() {
 		if (!validatateEdit())
 			return;
 		model.moveUp(getSelectionAsList());
 		refresh();
 	}
 
 	/*
 	 * Updates the enable state of the all buttons
 	 */
 	protected void updateButtonEnablements() {
 		int[] indices = availableJARsViewer.getTable().getSelectionIndices();
 		upButton.setEnabled(canMoveUp(indices));
 		downButton.setEnabled(canMoveDown(indices, availableJARsViewer.getTable().getItemCount()));
 	}
 
 	protected boolean canMoveUp(int[] indices) {
 		return canMove(indices, 0);
 	}
 
 	protected boolean canMoveDown(int[] indices, int itemCount) {
 		return canMove(indices, itemCount - 1);
 	}
 
 	protected boolean canMove(int[] indices, int referenceIndex) {
 		int length = indices.length;
 		if (length == 0)
 			return false;
 		for (int i = 0; i < length; i++) {
 			if (indices[i] == referenceIndex)
 				return false;
 		}
 		return true;
 	}
 	
 	protected void addTableListeners() {
 		addCheckStateListener();
 		addSelectionListener();
 	}
 
 	protected void addCheckStateListener() {
 		availableJARsViewer.addCheckStateListener(new ICheckStateListener() {
 			public void checkStateChanged(CheckStateChangedEvent event) {
 				availableJARCheckStateChanged(event);
 			}
 		});
 	}
 
 	protected void availableJARCheckStateChanged(CheckStateChangedEvent event) {
		if (isReadOnly() || !validatateEdit() || (isMyClientJAR(event) && !event.getChecked())) {
 			availableJARsViewer.setChecked(event.getElement(), !event.getChecked());
 			return;
 		}
 		ClasspathElement element = (ClasspathElement) event.getElement();
 		model.setSelection(element, event.getChecked());
 	}
 
 	/**
 	 * @param event
 	 * @return
 	 */
 	private boolean isMyClientJAR(CheckStateChangedEvent event) {
 		ClasspathElement element = (ClasspathElement) event.getElement();
 		return getClasspathSelection().isMyClientJAR(element);
 	}
 
 	protected void addSelectionListener() {
 		availableJARsViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			public void selectionChanged(SelectionChangedEvent event) {
 				tableSelectionChanged();
 			}
 		});
 	}
 
 	protected void tableSelectionChanged() {
 		if (!isReadOnly())
 			updateButtonEnablements();
 	}
 
 	protected ClassPathSelection getClasspathSelection() {
 		if (model == null || model.getSelectedEARComponent() == null && !J2EEComponentUtilities.isStandaloneWebComponent(model.getComponent()))
 			return null;
 		return model.getClassPathSelection();
 	}
 
 	public void refreshCheckedItems() {
 		if (getClasspathSelection() == null)
 			return;
 		java.util.List elements = getClasspathSelection().getClasspathElements();
 		for (int i = 0; i < elements.size(); i++) {
 			ClasspathElement element = (ClasspathElement) elements.get(i);
 			availableJARsViewer.setChecked(element, element.isSelected());
 		}
 	}
 
 	public void refresh() {
 		if (!isWLPEntry() && !J2EEComponentUtilities.isStandaloneWebComponent(model.getComponent())) {
 			availableJARsViewer.setInput(getClasspathSelection());
 			model.setWLPModel(false);
 			GridData data = new GridData(GridData.FILL_BOTH);
 			int numlines = Math.min(10, availableJARsViewer.getTable().getItemCount());
 			data.heightHint = availableJARsViewer.getTable().getItemHeight() * numlines;
 			availableJARsViewer.getTable().setLayoutData(data);
 			refreshCheckedItems();
 			updateButtonEnablements();
 		} else {
 			availableJARsViewer.setInput(model.getClassPathSelectionForWLPs());
 			model.setWLPModel(true);
 			GridData data = new GridData(GridData.FILL_BOTH);
 			int numlines = Math.min(10, availableJARsViewer.getTable().getItemCount());
 			data.heightHint = availableJARsViewer.getTable().getItemHeight() * numlines;
 			availableJARsViewer.getTable().setLayoutData(data);
 			refreshWLPCheckedItems();
 		}
 	}
 	
 	private void refreshWLPCheckedItems() {
 		if (availableJARsViewer.getInput() != null) {
 			java.util.List elements = ((ClassPathSelection) availableJARsViewer.getInput()).getClasspathElements();
 			for (int i = 0; i < elements.size(); i++) {
 				ClasspathElement element = (ClasspathElement) elements.get(i);
 				availableJARsViewer.setChecked(element, element.isSelected());
 			}
 		}
 	}
 
 	/**
 	 * Gets the model.
 	 * @return Returns a ClasspathModel
 	 */
 	public ClasspathModel getModel() {
 		return model;
 	}
 
 	/**
 	 * Sets the model.
 	 * @param model The model to set
 	 */
 	public void setModel(ClasspathModel model) {
 		this.model = model;
 		initializeEJBClientDefaults();
 	}
 
 	/**
 	 * Returns the readOnly.
 	 * @return boolean
 	 */
 	public boolean isReadOnly() {
 		return readOnly;
 	}
 
 
 	/**
 	 * Sets the readOnly.
 	 * @param readOnly The readOnly to set
 	 */
 	public void setReadOnly(boolean readOnly) {
 		this.readOnly = readOnly;
 	}
 
 	public boolean isWLPEntry() {
 		return isWLPEntry;
 	}
 
 	public void setWLPEntry(boolean isWLPEntry) {
 		this.isWLPEntry = isWLPEntry;
 	}
 
 
 }
