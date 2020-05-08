 package de.hswt.hrm.component.ui.wizard;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 import javax.inject.Inject;
 
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.forms.widgets.Section;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.List;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.ui.swt.forms.FormUtil;
 import de.hswt.hrm.common.ui.swt.layouts.LayoutUtil;
 import de.hswt.hrm.common.ui.swt.layouts.PageContainerFillLayout;
 import de.hswt.hrm.component.model.Attribute;
 import de.hswt.hrm.component.model.Category;
 import de.hswt.hrm.component.model.Component;
 import de.hswt.hrm.component.service.CategoryService;
 import de.hswt.hrm.component.service.ComponentService;
 import de.hswt.hrm.i18n.I18n;
 import de.hswt.hrm.i18n.I18nFactory;
 
 import org.eclipse.swt.layout.GridData;
 
 public class ComponentWizardPageOne extends WizardPage {
 
     private static final I18n I18N = I18nFactory.getI18n(ComponentWizardPageOne.class);
     
 	@Inject
 	private CategoryService categoryService;
 	
 	@Inject
 	private ComponentService componentService;
 
 	private Optional<Component> component;
 
 	private final FormToolkit formToolkit = new FormToolkit(
 			Display.getDefault());
 
 	private Text nameText;
 
 	private Text attributeText;
 
 	private List attributeList;
 
 	private Combo weightCombo;
 	private ComboViewer categoryComboViewer;
 
 	private int weight;
 
 	private Category category;
 
 	private String name;
 
 	private boolean rating;
 
 	private Button ratingCheckButton;
 
 	private ArrayList<String> attributes = new ArrayList<String>();
 	
 	/**
 	 * Create the wizard.
 	 */
 	public ComponentWizardPageOne(Optional<Component> component) {
 		super(I18N.tr("Component Wizard"));
 		this.component = component;
 
 		setTitle(I18N.tr("Component Wizard"));
 		setDescription(createDescription());
 	}
 
 	/**
 	 * Create contents of the wizard.
 	 * 
 	 * @param parent
 	 */
 	public void createControl(Composite parent) {
 		parent.setLayout(new PageContainerFillLayout());
 
 		Composite container = new Composite(parent, SWT.NULL);
 		setControl(container);
 
 		GridLayout gl = new GridLayout();
 		gl.marginHeight = 0;
 		gl.marginWidth = 0;
 		container.setLayout(gl);
 
 		Section definitionSection = formToolkit.createSection(container,
 				Section.TITLE_BAR);
 		definitionSection.setLayoutData(LayoutUtil.createHorzFillData());
 		formToolkit.paintBordersFor(definitionSection);
 		definitionSection.setText(I18N.tr("Component definition"));
 		definitionSection.setExpanded(true);
 		FormUtil.initSectionColors(definitionSection);
 		
 		Composite definitioncomposite = new Composite(definitionSection,
 				SWT.NONE);
 		formToolkit.adapt(definitioncomposite);
 		formToolkit.paintBordersFor(definitioncomposite);
 		definitionSection.setClient(definitioncomposite);
 		definitioncomposite.setLayout(new GridLayout(2, false));
 
 		Label nameLabel = new Label(definitioncomposite, SWT.NONE);
 		nameLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
 		formToolkit.adapt(nameLabel, true, true);
 		nameLabel.setText(I18N.tr("Name")+":");
 
 		nameText = new Text(definitioncomposite, SWT.BORDER);
 		nameText.setLayoutData(LayoutUtil.createHorzCenteredFillData());
 		formToolkit.adapt(nameText, true, true);
 		nameText.addModifyListener(new ModifyListener() {
 			@Override
 			public void modifyText(ModifyEvent e) {
 				checkPageComplete();
 			}
 		});
 
 		Label categoryLabel = new Label(definitioncomposite, SWT.NONE);
 		categoryLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
 		formToolkit.adapt(categoryLabel, true, true);
 		categoryLabel.setText(I18N.tr("Category")+":");
 
 		categoryComboViewer = new ComboViewer(definitioncomposite, SWT.NONE);
 		categoryComboViewer.getCombo().setLayoutData(
 				LayoutUtil.createHorzCenteredFillData());
 		formToolkit.adapt(categoryComboViewer.getCombo());
 		formToolkit.paintBordersFor(categoryComboViewer.getCombo());
 		categoryComboViewer.getCombo().addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				checkPageComplete();
 				weightCombo.select(getCategory().getDefaultQuantifier() - 1);
 			}
 		});
 
 		Label weightLabel = new Label(definitioncomposite, SWT.NONE);
 		weightLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
 		formToolkit.adapt(weightLabel, true, true);
 		weightLabel.setText(I18N.tr("Weight")+":");
 
 		weightCombo = new Combo(definitioncomposite, SWT.NONE);
 		weightCombo.setLayoutData(LayoutUtil.createHorzCenteredFillData());
 		formToolkit.adapt(weightCombo);
 		formToolkit.paintBordersFor(weightCombo);
 		weightCombo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				checkPageComplete();
 			}
 		});
 
 		ratingCheckButton = new Button(definitioncomposite, SWT.CHECK);
 		ratingCheckButton.setLayoutData(LayoutUtil.createLeftCenteredGridData(
 				2, 1));
 		formToolkit.adapt(ratingCheckButton, true, true);
 		ratingCheckButton.setText(I18N.tr("with rating"));
 
 		Section attributesSection = formToolkit.createSection(container,
 				Section.TITLE_BAR);
 		attributesSection.setLayoutData(LayoutUtil.createFillData());
 		formToolkit.paintBordersFor(attributesSection);
 		attributesSection.setText(I18N.tr("Attributes"));
 		attributesSection.setExpanded(true);
 		FormUtil.initSectionColors(attributesSection);
 
 		Composite attributesComposite = new Composite(attributesSection,
 				SWT.NONE);
 		formToolkit.adapt(attributesComposite);
 		formToolkit.paintBordersFor(attributesComposite);
 		attributesSection.setClient(attributesComposite);
 		attributesComposite.setLayout(new GridLayout(3, false));
 
 		attributeText = new Text(attributesComposite, SWT.BORDER);
 		attributeText.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
 		attributeText.setLayoutData(LayoutUtil.createHorzCenteredFillData());
 		formToolkit.adapt(attributeText, true, true);
 		
 		Button addAttributeButton = new Button(attributesComposite, SWT.NONE);
 		formToolkit.adapt(addAttributeButton, true, true);
 		addAttributeButton.setText(I18N.tr("Add"));
 		addAttributeButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if(!attributeText.getText().isEmpty() && !Arrays.asList(attributeList.getItems()).contains(attributeText.getText())) {
 					attributeList.add(attributeText.getText());
 				}
 				attributes.clear();
 				for (String attri : attributeList.getItems()) {
 					attributes.add(attri);
 				}
 			}
 		});
 		new Label(attributesComposite, SWT.NONE);
 
 		attributeList = new List(attributesComposite, SWT.BORDER);
 		GridData gd_attributeList = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
 		gd_attributeList.widthHint = 500;
 		attributeList.setLayoutData(gd_attributeList);
 		attributeList.setLayoutData(LayoutUtil.createFillData(2));
 		formToolkit.adapt(attributeList, true, true);
 		new Label(attributesComposite, SWT.NONE);
 		
 		Button deleteButton = new Button(attributesComposite, SWT.NONE);
 		deleteButton.setAlignment(SWT.RIGHT);
 		formToolkit.adapt(deleteButton, true, true);
 		deleteButton.setText(I18N.tr("Delete"));
 		new Label(attributesComposite, SWT.NONE);
 		new Label(attributesComposite, SWT.NONE);
 		deleteButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if(attributeList.getSelectionIndex() != -1){
 					attributes.remove(attributeList.getSelectionIndex());
 					attributeList.remove(attributeList.getSelectionIndex());
 				}
 			}
 		});
 
 		initializeCombos();
 		categoryComboViewer.setSelection(new StructuredSelection(categoryComboViewer.getElementAt(0)));
 		if (this.component.isPresent()) {
 			updateFields(container);
 		}
 		checkPageComplete();
 	}
 
 	private String createDescription() {
 		if (component.isPresent()) {
 			return I18N.tr("Edit a component");
 		}
 		return I18N.tr("Add a new component");
 	}
 
 	private void initializeCombos() {
 		for (int i = 1; i < 7; i++) {
 			weightCombo.add(Integer.toString(i));
 		}
 		weightCombo.select(0);
 		
 		categoryComboViewer.setContentProvider(new ArrayContentProvider());
 		categoryComboViewer.setLabelProvider(new LabelProvider() {
 			@Override
 			public String getText(Object element) {
 				return ((Category) element).getName();
 			}
 		});
 
 		try {
 			categoryComboViewer.setInput(categoryService.findAll());
 		} catch (DatabaseException e) {
 			e.printStackTrace();
 		}
 		categoryComboViewer.refresh(true);
 	}
 
 	private void updateFields(Composite c) {
         Component comp = component.get();
 
         nameText.setText(comp.getName());
         ratingCheckButton.setSelection(comp.getBoolRating());
 
         if (comp.getQuantifier().isPresent()) {
             weightCombo.select(comp.getQuantifier().get() - 1);
         }
         
         StructuredSelection selection = 
         		new StructuredSelection(comp.getCategory().get());
         categoryComboViewer.setSelection(selection);
         categoryComboViewer.refresh();
         
         Collection<Attribute> coll;
 		try {
 			coll = componentService.findAttributesByComponent(comp);
 	        String[] attributes = new String[coll.size()];
 	        int i = 0;
 	        for(Attribute att : coll){
 	        	attributes[i] = att.getName();
 	        	i++;
 	        }
 	        attributeList.setItems(attributes);
 		} catch (DatabaseException e) {
 			e.printStackTrace();
 		}
 		attributeList.redraw();
     }
 
 	private void checkPageComplete() {
 		for (String attri : attributeList.getItems()) {
 			attributes.add(attri);
 		}
 		ISelection selection = categoryComboViewer.getSelection();
 		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
 		category = (Category) structuredSelection.getFirstElement();
 		name = nameText.getText();
 		rating = ratingCheckButton.getSelection();
 		weight = weightCombo.getSelectionIndex() + 1;
 
 		if (nameText.getText().isEmpty()) {
 			setPageComplete(false);
 			setErrorMessage(I18N.tr("Field is mandatory") + ": " + I18N.tr("Name"));
 			return;
 		}
 		if (category == null) {
 			setPageComplete(false);
 			setErrorMessage(I18N.tr("Please select a category."));
 			return;
 		}
 		setPageComplete(true);
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public int getQuantifier() {
 		return weight;
 	}
 
 	public boolean getRating() {
 		return rating;
 	}
 
 	public Category getCategory() {
 		return category;
 	}
 
 	public java.util.List<String> getAttributes() {
 		return attributes;
 	}
 }
