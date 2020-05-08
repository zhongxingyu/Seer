 package de.fkoeberle.autocommit.message.ui;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.SashForm;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.editor.FormEditor;
 import org.eclipse.ui.forms.editor.FormPage;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 
 import de.fkoeberle.autocommit.message.CommitMessageFactoryDescription;
 import de.fkoeberle.autocommit.message.ProfileIdResourceAndName;
 import de.fkoeberle.autocommit.message.ui.Model.CMFList;
 import de.fkoeberle.autocommit.message.ui.Model.ICurrentProfileListener;
 import de.fkoeberle.autocommit.message.ui.Model.IDirtyPropertyListener;
 
 public class AdvancedPage extends FormPage {
	public static final String ID = "de.fkoeberle.autocommit.message.ui.AdvancedPage"; //$NON-NLS-1$
 	private final Model model;
 	private Table usedFactoriesTable;
 	private Composite factoriesComposite;
 	private Table unusedFactoriesTable;
 
 	public AdvancedPage(FormEditor editor, Model model) {
 		super(editor, ID, "Advanced");
 		this.model = model;
 	}
 
 	/**
 	 * Create contents of the editor part.
 	 * 
 	 * @param parent
 	 */
 	@Override
 	public void createFormContent(IManagedForm managedForm) {
 		super.createFormContent(managedForm);
 
 		ScrolledForm scrolledForm = managedForm.getForm();
 		// TODO documentation says I should use the toolkit to create form child
 		// elements like buttons
 		// FormToolkit toolkit = managedForm.getToolkit();
 		//
 		//
 		// TODO do I want a title?
 		// scrolledForm.setText("Hello World");
 		Composite parent = scrolledForm.getBody();
 		parent.setLayout(new FillLayout(SWT.HORIZONTAL));
 		Composite container = new Composite(parent, SWT.NONE);
 		container.setLayout(new GridLayout(1, false));
 
 		Composite header = new Composite(container, SWT.NONE);
 		header.setLayout(new GridLayout(2, false));
 
 		Label generateLabel = new Label(header, SWT.NONE);
 		generateLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false,
 				false, 1, 1));
 		generateLabel.setText("Generate:");
 
 		final ComboViewer comboViewer = new ComboViewer(header, SWT.READ_ONLY);
 		final Combo combo = comboViewer.getCombo();
 		GridData comboLayoutData = new GridData(SWT.FILL, SWT.CENTER, true,
 				false, 1, 1);
 		comboLayoutData.widthHint = 315;
 		combo.setLayoutData(comboLayoutData);
 		comboViewer
 				.addSelectionChangedListener(new ISelectionChangedListener() {
 
 					@Override
 					public void selectionChanged(SelectionChangedEvent event) {
 						IStructuredSelection selection = (IStructuredSelection) comboViewer
 								.getSelection();
 						Object selectedObject = selection.iterator().next();
 						try {
 							model.switchToProfile((ProfileIdResourceAndName) selectedObject);
 						} catch (ExecutionException e) {
 							reportError(combo.getShell(),
 									"Failed to switch profile", e);
 						}
 					}
 				});
 
 		comboViewer.setContentProvider(new ObservableListContentProvider());
 		comboViewer.setLabelProvider(new DefaultProfileLabelProvider());
 		comboViewer.setInput(model.getProfiles());
 
 		ProfileComboBoxUpdater profileComboBoxUpdater = new ProfileComboBoxUpdater(
 				comboViewer);
 		model.addCurrentProfileListener(new ProfileComboBoxUpdater(comboViewer));
 		profileComboBoxUpdater.currentProfileChanged();
 
 		SashForm sashForm = new SashForm(container, SWT.NONE);
 		sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,
 				1));
 
 		Composite leftComposite = new Composite(sashForm, SWT.BORDER);
 		leftComposite.setLayout(new GridLayout(1, false));
 
 		Composite leftHeader = new Composite(leftComposite, SWT.NONE);
 		leftHeader.setLayout(new GridLayout(1, false));
 
 		Label lblCommitMessageFactories = new Label(leftHeader, SWT.NONE);
 		lblCommitMessageFactories.setText("Used commit message factories:");
 
 		final TableViewer usedFactoriesTableViewer = new TableViewer(
 				leftComposite, SWT.BORDER | SWT.MULTI);
 		usedFactoriesTable = usedFactoriesTableViewer.getTable();
 		usedFactoriesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
 				true, 1, 1));
 		usedFactoriesTableViewer
 				.setContentProvider(new ObservableListContentProvider());
 		usedFactoriesTableViewer.setLabelProvider(new FactoryLabelProvider());
 		usedFactoriesTableViewer.setInput(model.getFactoryDescriptions());
 
 		Composite middleComposite = new Composite(sashForm, SWT.BORDER);
 		middleComposite.setLayout(new GridLayout(1, false));
 
 		Composite middleHeader = new Composite(middleComposite, SWT.NONE);
 		middleHeader.setLayout(new GridLayout(1, false));
 
 		Label lblUnused = new Label(middleHeader, SWT.NONE);
 		lblUnused.setText("Unused commit message factories:");
 
 		TableViewer unusedFactoriesTableViewer = new TableViewer(
 				middleComposite, SWT.BORDER | SWT.MULTI);
 		unusedFactoriesTable = unusedFactoriesTableViewer.getTable();
 		unusedFactoriesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL,
 				true, true, 1, 1));
 		unusedFactoriesTableViewer
 				.setContentProvider(new ObservableListContentProvider());
 		unusedFactoriesTableViewer.setLabelProvider(new FactoryLabelProvider());
 		unusedFactoriesTableViewer.setInput(model
 				.getUnusedFactoryDescriptions());
 
 		addDragAndDropSupport(usedFactoriesTableViewer,
 				unusedFactoriesTableViewer);
 
 		usedFactoriesTableViewer
 				.addSelectionChangedListener(new FactoriesSelectionListener(
 						CMFList.USED, usedFactoriesTableViewer,
 						unusedFactoriesTableViewer));
 		unusedFactoriesTableViewer
 				.addSelectionChangedListener(new FactoriesSelectionListener(
 						CMFList.UNUSED, unusedFactoriesTableViewer,
 						usedFactoriesTableViewer));
 
 		Composite rightComposite = new Composite(sashForm, SWT.BORDER);
 		rightComposite.setLayout(new GridLayout(1, false));
 
 		Composite rightHeader = new Composite(rightComposite, SWT.NONE);
 		rightHeader.setLayout(new GridLayout(1, false));
 
 		Label lblSelected = new Label(rightHeader, SWT.NONE);
 		lblSelected.setText("Selected commit message factories:");
 
 		final ScrolledComposite scrolledComposite = new ScrolledComposite(
 				rightComposite, SWT.V_SCROLL | SWT.BORDER);
 		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
 				true, 1, 1));
 		factoriesComposite = new Composite(scrolledComposite, SWT.NONE);
 		scrolledComposite.setContent(factoriesComposite);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 1;
 		factoriesComposite.setLayout(layout);
 		scrolledComposite.addControlListener(new ControlAdapter() {
 			@Override
 			public void controlResized(ControlEvent e) {
 				Rectangle r = scrolledComposite.getClientArea();
 				factoriesComposite.setSize(factoriesComposite.computeSize(
 						r.width, SWT.DEFAULT));
 				factoriesComposite.layout();
 			}
 		});
 		scrolledComposite.setAlwaysShowScrollBars(true);
 		sashForm.setWeights(new int[] { 180, 180, 350 });
 
 		model.addDirtyPropertyListener(new IDirtyPropertyListener() {
 
 			@Override
 			public void handleDirtyPropertyChange() {
 				firePropertyChange(PROP_DIRTY);
 			}
 		});
 		container.addDisposeListener(new DisposeListener() {
 
 			@Override
 			public void widgetDisposed(DisposeEvent e) {
 				model.dispose();
 			}
 		});
 	}
 
 	private void addDragAndDropSupport(
 			final TableViewer usedFactoriesTableViewer,
 			TableViewer unusedFactoriesTableViewer) {
 		Transfer[] transfers = new Transfer[] { UniqueIdTransfer.INSTANCE };
 
 		Map<Long, Model.CMFList> listIdToTypeMap = new HashMap<Long, Model.CMFList>();
 		Long usedListId = Long.valueOf(new Random().nextLong());
 		Long unusedListId = Long.valueOf(usedListId.longValue() + 1);
 		listIdToTypeMap.put(usedListId, CMFList.USED);
 		listIdToTypeMap.put(unusedListId, CMFList.UNUSED);
 
 		Map<CMFList, TableViewer> listIdToTableViewerMap = new HashMap<CMFList, TableViewer>();
 		listIdToTableViewerMap.put(CMFList.USED, usedFactoriesTableViewer);
 		listIdToTableViewerMap.put(CMFList.UNUSED, unusedFactoriesTableViewer);
 
 		unusedFactoriesTableViewer.addDragSupport(DND.DROP_MOVE, transfers,
 				new CMFDragSource(unusedFactoriesTableViewer, unusedListId));
 		unusedFactoriesTableViewer.addDropSupport(DND.DROP_MOVE, transfers,
 				new CMFDropAdapter(Model.CMFList.UNUSED, model,
 						listIdToTypeMap, listIdToTableViewerMap));
 
 		usedFactoriesTableViewer.addDragSupport(DND.DROP_MOVE, transfers,
 				new CMFDragSource(usedFactoriesTableViewer, usedListId));
 		usedFactoriesTableViewer.addDropSupport(DND.DROP_MOVE, transfers,
 				new CMFDropAdapter(Model.CMFList.USED, model, listIdToTypeMap,
 						listIdToTableViewerMap));
 	}
 
 	private final class ProfileComboBoxUpdater implements
 			ICurrentProfileListener {
 		private final ComboViewer comboViewer;
 
 		private ProfileComboBoxUpdater(ComboViewer comboViewer) {
 			this.comboViewer = comboViewer;
 		}
 
 		@Override
 		public void currentProfileChanged() {
 			IStructuredSelection selection = (IStructuredSelection) comboViewer
 					.getSelection();
 			if (!selection.isEmpty()) {
 				Object selectedObject = selection.iterator().next();
 				if (selectedObject == model.getCurrentProfile()) {
 					return;
 				}
 			}
 			comboViewer.setSelection(
 					new StructuredSelection(model.getCurrentProfile()), true);
 		}
 	}
 
 	private final class FactoriesSelectionListener implements
 			ISelectionChangedListener {
 		private final CMFList listType;
 		private final TableViewer tableViewer;
 		private final TableViewer otherTableViewer;
 
 		private FactoriesSelectionListener(CMFList listType,
 				TableViewer tableViewer, TableViewer otherTableViewer) {
 			this.listType = listType;
 			this.tableViewer = tableViewer;
 			this.otherTableViewer = otherTableViewer;
 		}
 
 		@Override
 		public void selectionChanged(SelectionChangedEvent event) {
 			int[] indices = tableViewer.getTable().getSelectionIndices();
 			if (indices.length != 0) {
 				setRightFactorySelection(listType, indices);
 				otherTableViewer.getTable().setSelection(new int[] {});
 			}
 			int otherSelectionCount = otherTableViewer.getTable()
 					.getSelectionCount();
 			if (indices.length == 0 && otherSelectionCount == 0) {
 				setRightFactorySelection(listType, new int[] {});
 			}
 		}
 	}
 
 	public void setRightFactorySelection(CMFList list, int[] indices) {
 		for (Control child : factoriesComposite.getChildren()) {
 			child.dispose();
 		}
 		for (int factoryIndex : indices) {
 			CommitMessageFactoryDescription factory = (CommitMessageFactoryDescription) model
 					.getList(list).get(factoryIndex);
 			CommitMessageFactoryComposite factoryComposite = new CommitMessageFactoryComposite(
 					factoriesComposite, SWT.NONE, model, factory);
 			factoryComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP,
 					true, false, 1, 1));
 		}
 
 		factoriesComposite.layout(true, true);
 		factoriesComposite.setSize(factoriesComposite.computeSize(
 				factoriesComposite.getSize().x, SWT.DEFAULT));
 	}
 
 	@Override
 	public boolean isDirty() {
 		return model.isDirty();
 	}
 
 	@Override
 	public boolean isSaveAsAllowed() {
 		return false;
 	}
 
 	public static void reportError(Shell shell, String message, Exception e) {
 		MessageDialog.openError(shell, message,
 				NLS.bind("{0}: See error log for details", message));
 		Activator
 				.getDefault()
 				.getLog()
 				.log(new Status(Status.ERROR, Activator.PLUGIN_ID, Status.OK,
 						message, e));
 	}
 
 	private final class FactoryLabelProvider extends LabelProvider {
 		@Override
 		public String getText(Object element) {
 			CommitMessageFactoryDescription factoryDescription = (CommitMessageFactoryDescription) element;
 			return factoryDescription.getTitle();
 		}
 	}
 
 	private final class DefaultProfileLabelProvider extends LabelProvider {
 		@Override
 		public String getText(Object element) {
 			ProfileIdResourceAndName factoryDescription = (ProfileIdResourceAndName) element;
 			if (element == null) {
 				return "Custom Commit Messages";
 			}
 			return factoryDescription.getName();
 		}
 	}
 }
