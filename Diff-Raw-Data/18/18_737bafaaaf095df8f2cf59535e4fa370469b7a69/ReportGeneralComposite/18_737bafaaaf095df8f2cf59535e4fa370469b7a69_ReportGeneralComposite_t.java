 package de.hswt.hrm.inspection.ui.part;
 
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.LinkedList;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 
 import org.eclipse.e4.core.contexts.ContextInjectionFactory;
 import org.eclipse.e4.core.contexts.IEclipseContext;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.window.IShellProvider;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DateTime;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.List;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.observer.Observer;
 import de.hswt.hrm.common.ui.swt.forms.FormUtil;
 import de.hswt.hrm.common.ui.swt.layouts.LayoutUtil;
 import de.hswt.hrm.common.ui.swt.utils.ContentProposalUtil;
 import de.hswt.hrm.common.ui.swt.wizards.WizardCreator;
 import de.hswt.hrm.component.model.Component;
 import de.hswt.hrm.contact.model.Contact;
 import de.hswt.hrm.inspection.model.Inspection;
 import de.hswt.hrm.inspection.model.Layout;
 import de.hswt.hrm.inspection.service.InspectionService;
 import de.hswt.hrm.inspection.service.LayoutService;
 import de.hswt.hrm.inspection.ui.dialog.ContactSelectionDialog;
 import de.hswt.hrm.inspection.ui.dialog.PlantSelectionDialog;
 import de.hswt.hrm.photo.model.Photo;
 import de.hswt.hrm.photo.ui.wizard.PhotoWizard;
 import de.hswt.hrm.plant.model.Plant;
 import de.hswt.hrm.summary.model.Summary;
 import de.hswt.hrm.summary.service.SummaryService;
 
 public class ReportGeneralComposite extends AbstractComponentRatingComposite {
 
     private final static Logger LOG = LoggerFactory.getLogger(ReportGeneralComposite.class);
 
     @Inject
     private InspectionService inspectionService;
 
     @Inject
     private SummaryService evaluationService;
 
     @Inject
     private LayoutService layoutService;
 
     @Inject
     private IEclipseContext context;
 
     @Inject
     private IShellProvider shellProvider;
 
     private FormToolkit formToolkit = new FormToolkit(Display.getDefault());
 
     private Section generalSection;
     private Section personsSection;
     private Section visualSection;
     private Section biologicalSection;
     private Section physicalSection;
 
     private Text titleText;
     private Text plantText;
     private DateTime reportDateTime;
     private DateTime inspectionDateTime;
     private DateTime nextInspectionDateTime;
     private Combo overallCombo;
 
     private List photosList;
     private Text legionellaText;
     private Text sumGermsText;
     private Combo legionellaWeightCombo;
     private Combo legionellaGradeCombo;
     private Combo legionellaCommentCombo;
     private Combo sumGermsGradeCombo;
     private Combo sumGermsWeightCombo;
     private Combo sumGermsCommentCombo;
 
     private Text temperatureText;
     private Combo temperatureGradeCombo;
     private Combo temperatureWeightCombo;
     private Combo temperatureCommentCombo;
     private Text humidityText;
     private Combo humidityGradeCombo;
     private Combo humidityWeightCombo;
     private Combo humitiyCommentCombo;
 
     Combo reportStyleCombo;
 
     private InspectionPart inspectionPart;
 
     java.util.List<Photo> photos = new LinkedList<Photo>();
 
     private Inspection inspection;
 
     private Text customerNameText;
     private Text customerStreetText;
     private Text customerCityText;
 
     private Text requestorNameText;
     private Text requestorStreetText;
     private Text requestorCityText;
 
     private Text controllerNameText;
     private Text controllerStreetText;
     private Text controllerCityText;
 
     private ComboViewer titlePhotoComboViewer;
 
     private ComboViewer plantPhotoComboViewer;
 
     private Photo selectedTitlePhoto;
 
     private Photo selectedPlantPhoto;
 
     /**
      * Do not use this constructor when instantiate this composite! It is only included to make the
      * WindowsBuilder working.
      * 
      * @param parent
      * @param style
      */
     private ReportGeneralComposite(Composite parent, int style) {
         super(parent, SWT.NONE);
         createControls();
     }
 
     /**
      * Create the composite.
      * 
      * @param parent
      */
     public ReportGeneralComposite(Composite parent, InspectionPart inspectionPart) {
         super(parent, SWT.NONE);
         this.inspectionPart = inspectionPart;
         formToolkit.dispose();
         formToolkit = FormUtil.createToolkit();
     }
 
     @PostConstruct
     public void createControls() {
         GridLayout gl = new GridLayout(1, false);
         setLayout(gl);
         setBackground(getDisplay().getSystemColor(SWT.COLOR_WHITE));
 
         Composite c = new Composite(this, SWT.NONE);
         c.setLayoutData(LayoutUtil.createFillData());
         c.setLayout(new FillLayout());
 
         ScrolledComposite sc = new ScrolledComposite(c, SWT.H_SCROLL | SWT.V_SCROLL);
         sc.setExpandVertical(true);
         sc.setExpandHorizontal(true);
 
         Composite composite = new Composite(sc, SWT.None);
         composite.setBackgroundMode(SWT.INHERIT_FORCE);
         gl = new GridLayout(2, true);
         gl.marginWidth = 0;
         gl.marginHeight = 0;
         composite.setLayout(gl);
         sc.setContent(composite);
 
         generalSection = formToolkit.createSection(composite, Section.TITLE_BAR);
         formToolkit.paintBordersFor(generalSection);
         generalSection.setText("General");
         generalSection.setLayoutData(LayoutUtil.createHorzFillData());
         FormUtil.initSectionColors(generalSection);
 
         personsSection = formToolkit.createSection(composite, Section.TITLE_BAR);
         formToolkit.paintBordersFor(personsSection);
         personsSection.setText("Persons");
         personsSection.setLayoutData(LayoutUtil.createHorzFillData(1, 2));
         FormUtil.initSectionColors(personsSection);
 
         visualSection = formToolkit.createSection(composite, Section.TITLE_BAR);
         formToolkit.paintBordersFor(visualSection);
         visualSection.setText("Visual");
         visualSection.setLayoutData(LayoutUtil.createHorzFillData());
         FormUtil.initSectionColors(visualSection);
 
         biologicalSection = formToolkit.createSection(composite, Section.TITLE_BAR);
         formToolkit.paintBordersFor(biologicalSection);
         biologicalSection.setText("Saturator water");
         biologicalSection.setLayoutData(LayoutUtil.createHorzFillData());
         FormUtil.initSectionColors(biologicalSection);
 
         physicalSection = formToolkit.createSection(composite, Section.TITLE_BAR);
         formToolkit.paintBordersFor(physicalSection);
         physicalSection.setText("Physical parameter");
         physicalSection.setLayoutData(LayoutUtil.createHorzFillData());
         FormUtil.initSectionColors(physicalSection);
 
         createGeneralComponents();
         createPersonsComponents();
         createVisualComponents();
         createBiologicalComponents();
         createPhysicalComponents();
 
         // TODO remove the following when it is no longer needed
         // GENERAL
         // report title, given at creation time (ReportsOverviewComposite)
         // Button select plant => opens wizard with contacts to select contact
         // Button or dropdown to select overall comment
         // field with date picker => default current
         // field for next inspection with date picker
         // => radio buttons for 2 or 3 years
         // => see pictures in Redmine
 
         // PERSONS
         // Button select customer => opens wizard with contacts to select contact
         // Button select requester => opens wizard with contacts to select contact
         // Button select controller => opens wizard with contacts to select contact
 
         // VISUAL
         // combo to select report style
         // selection of plant image
 
         // rating of physical parameter (Temperatur, relative Luftfeuchtigkeit)
         // rating of wasserkeimzahl???
 
         sc.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
     }
 
     private void createGeneralComponents() {
         Composite generalComposite = new Composite(generalSection, SWT.NONE);
         generalComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
         formToolkit.adapt(generalComposite);
         formToolkit.paintBordersFor(generalComposite);
         GridLayout gl = new GridLayout(5, false);
         gl.marginWidth = 1;
         generalComposite.setLayout(gl);
         generalSection.setClient(generalComposite);
 
         Label titleLabel = new Label(generalComposite, SWT.NONE);
         titleLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(titleLabel, true, true);
         titleLabel.setText("Title");
 
         titleText = new Text(generalComposite, SWT.NONE);
         titleText.setTextLimit(50);
         titleText.setLayoutData(LayoutUtil.createHorzCenteredFillData(4, 1));
         formToolkit.adapt(titleText, true, true);
 
         Label plantLabel = new Label(generalComposite, SWT.NONE);
         plantLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(plantLabel, true, true);
         plantLabel.setText("Plant");
 
         plantText = new Text(generalComposite, SWT.READ_ONLY);
         plantText.setLayoutData(LayoutUtil.createHorzCenteredFillData(2, 1));
         formToolkit.adapt(plantText, true, true);
 
         Button plantSelectionButton = formToolkit.createButton(generalComposite, "Select plant",
                 SWT.PUSH);
         plantSelectionButton.setLayoutData(LayoutUtil.createRightGridData(2));
         plantSelectionButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 PlantSelectionDialog psd = new PlantSelectionDialog(shellProvider.getShell(),
                         context);
                 psd.create();
                 if (psd.open() == Window.OK) {
                     if (inspection != null) {
                         inspection.setPlant(psd.getPlant());
                     }
                 }
             }
         });
 
         Label reportDateLabel = new Label(generalComposite, SWT.NONE);
         reportDateLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(reportDateLabel, true, true);
         reportDateLabel.setText("Report date");
 
         reportDateTime = new DateTime(generalComposite, SWT.BORDER | SWT.DATE | SWT.DROP_DOWN);
         formToolkit.adapt(reportDateTime);
         formToolkit.paintBordersFor(reportDateTime);
         reportDateTime.setLayoutData(LayoutUtil.createHorzFillData(4));
 
         Label inspectionDateLabel = new Label(generalComposite, SWT.NONE);
         inspectionDateLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(inspectionDateLabel, true, true);
         inspectionDateLabel.setText("Inspection data");
 
         inspectionDateTime = new DateTime(generalComposite, SWT.BORDER | SWT.DATE | SWT.DROP_DOWN);
         formToolkit.adapt(inspectionDateTime);
         formToolkit.paintBordersFor(inspectionDateTime);
         inspectionDateTime.setLayoutData(LayoutUtil.createHorzFillData(4));
 
         Label nextInspectionLabel = new Label(generalComposite, SWT.NONE);
         nextInspectionLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(nextInspectionLabel, true, true);
         nextInspectionLabel.setText("Next inspection");
 
         nextInspectionDateTime = new DateTime(generalComposite, SWT.BORDER | SWT.DATE
                 | SWT.DROP_DOWN);
         formToolkit.adapt(nextInspectionDateTime);
         formToolkit.paintBordersFor(nextInspectionDateTime);
         nextInspectionDateTime.setLayoutData(LayoutUtil.createHorzFillData());
         nextInspectionDateTime.setYear(inspectionDateTime.getYear() + 2);
 
         Button twoYearsButton = formToolkit.createButton(generalComposite, "2 years", SWT.PUSH);
         twoYearsButton.setLayoutData(LayoutUtil.createRightGridData(2));
         twoYearsButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 nextInspectionDateTime.setYear(inspectionDateTime.getYear() + 2);
             }
         });
 
         Button threeYearsButton = formToolkit.createButton(generalComposite, "3 years", SWT.PUSH);
         threeYearsButton.setLayoutData(LayoutUtil.createRightGridData());
         threeYearsButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 nextInspectionDateTime.setYear(inspectionDateTime.getYear() + 3);
             }
         });
 
         Label reportStyleLabel = new Label(generalComposite, SWT.NONE);
         reportStyleLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(reportStyleLabel, true, true);
         reportStyleLabel.setText("Report Layout");
 
         reportStyleCombo = new Combo(generalComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
         initLayouts(reportStyleCombo);
         reportStyleCombo.setLayoutData(LayoutUtil.createHorzCenteredFillData(4));
 
         formToolkit.adapt(reportStyleCombo);
         formToolkit.paintBordersFor(reportStyleCombo);
 
         Label overallLabel = new Label(generalComposite, SWT.NONE);
         overallLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(overallLabel, true, true);
         overallLabel.setText("Overall comment");
 
         overallCombo = new Combo(generalComposite, SWT.NONE);
         overallCombo.setLayoutData(LayoutUtil.createHorzCenteredFillData(4));
         formToolkit.adapt(overallCombo);
         formToolkit.paintBordersFor(overallCombo);
         initializePhotosList();
         
         initializePhotoCombos();
 
     }
 
     private void initializePhotosList() {
 		// FIXME Fill List with the Photos belonging to the Inscpection
     	//photos = service.findAllPhotosBelonging to the Insepction
 		
 	}
 
 	private void initializePhotoCombos() {
    	if(inspection != null){
			if(inspection.getPlantpicture().isPresent()){
	    		selectedPlantPhoto = inspection.getPlantpicture().get();
	    		titlePhotoComboViewer.setSelection(new StructuredSelection(selectedPlantPhoto));
	    		
	    	}
	    	if(inspection.getFrontpicture().isPresent()){
	    		selectedTitlePhoto = inspection.getFrontpicture().get(); 
	    		plantPhotoComboViewer.setSelection(new StructuredSelection(selectedPlantPhoto));
	    	}
     	}
 	}
 
 	private void plantSelected(Plant plant) {
         plantText.setText(plant.getDescription());
     }
 
     private void initLayouts(Combo combo) {
 
         try {
             // Obtain all layouts form the DB
             java.util.List<Layout> layouts = (java.util.List<Layout>) layoutService.findAll();
 
             String[] items = new String[layouts.size()];
             int index = 0;
             for (Layout l : layouts) {
                 items[index] = l.getName();
                 index++;
 
             }
             combo.setItems(items);
             index = 0;
             for (String s : items) {
                 combo.setData(s, layouts.get(index));
                 index++;
             }
         }
 
         catch (DatabaseException e) {
             e.printStackTrace();
         }
 
     }
 
     private void createPersonsComponents() {
         Composite personsComposite = new Composite(personsSection, SWT.NONE);
         personsComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
         formToolkit.adapt(personsComposite);
         formToolkit.paintBordersFor(personsComposite);
         GridLayout gl = new GridLayout(3, false);
         gl.marginWidth = 1;
         personsComposite.setLayout(gl);
         personsSection.setClient(personsComposite);
 
         Label customerLabel = new Label(personsComposite, SWT.NONE);
         formToolkit.adapt(customerLabel, true, true);
         customerLabel.setText("Customer");
         customerLabel.setLayoutData(LayoutUtil.createLeftGridData());
 
         Button customerClearButton = formToolkit.createButton(personsComposite, "Clear customer",
                 SWT.PUSH);
         customerClearButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
 
         Button customerSelectionButton = formToolkit.createButton(personsComposite,
                 "Select customer", SWT.PUSH);
         customerSelectionButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
 
         Label customerNameLabel = new Label(personsComposite, SWT.NONE);
         customerNameLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(customerNameLabel, true, true);
         customerNameLabel.setText("Name");
 
         customerNameText = new Text(personsComposite, SWT.READ_ONLY);
         customerNameText.setLayoutData(LayoutUtil.createHorzCenteredFillData(2));
         formToolkit.adapt(customerNameText, true, true);
 
         Label customerStreetLabel = new Label(personsComposite, SWT.NONE);
         customerStreetLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(customerStreetLabel, true, true);
         customerStreetLabel.setText("Street");
 
         customerStreetText = new Text(personsComposite, SWT.READ_ONLY);
         customerStreetText.setLayoutData(LayoutUtil.createHorzCenteredFillData(2));
         formToolkit.adapt(customerStreetText, true, true);
 
         Label customerCityLabel = new Label(personsComposite, SWT.NONE);
         customerCityLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(customerCityLabel, true, true);
         customerCityLabel.setText("City");
 
         customerCityText = new Text(personsComposite, SWT.READ_ONLY);
         customerCityText.setLayoutData(LayoutUtil.createHorzCenteredFillData(2));
         formToolkit.adapt(customerCityText, true, true);
 
         Label dummy = new Label(personsComposite, SWT.NONE);
         dummy.setLayoutData(LayoutUtil.createFillData(2));
         new Label(personsComposite, SWT.NONE);
 
         Label requestorLabel = new Label(personsComposite, SWT.NONE);
         formToolkit.adapt(requestorLabel, true, true);
         requestorLabel.setText("Requestor");
         requestorLabel.setLayoutData(LayoutUtil.createLeftGridData());
 
         Button requestorClearButton = formToolkit.createButton(personsComposite, "Clear requestor",
                 SWT.PUSH);
         requestorClearButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
 
         Button requestorSelectionButton = formToolkit.createButton(personsComposite,
                 "Select requestor", SWT.PUSH);
         requestorSelectionButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
 
         Label requestorNameLabel = new Label(personsComposite, SWT.NONE);
         requestorNameLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(requestorNameLabel, true, true);
         requestorNameLabel.setText("Name");
 
         requestorNameText = new Text(personsComposite, SWT.READ_ONLY);
         requestorNameText.setLayoutData(LayoutUtil.createHorzCenteredFillData(2));
         formToolkit.adapt(requestorNameText, true, true);
 
         Label requestorStreetLabel = new Label(personsComposite, SWT.NONE);
         requestorStreetLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(requestorStreetLabel, true, true);
         requestorStreetLabel.setText("Street");
 
         requestorStreetText = new Text(personsComposite, SWT.READ_ONLY);
         requestorStreetText.setLayoutData(LayoutUtil.createHorzCenteredFillData(2));
         formToolkit.adapt(requestorStreetText, true, true);
 
         Label requestorCityLabel = new Label(personsComposite, SWT.NONE);
         requestorCityLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(requestorCityLabel, true, true);
         requestorCityLabel.setText("City");
 
         requestorCityText = new Text(personsComposite, SWT.READ_ONLY);
         requestorCityText.setLayoutData(LayoutUtil.createHorzCenteredFillData(2));
         formToolkit.adapt(requestorCityText, true, true);
 
         dummy = new Label(personsComposite, SWT.NONE);
         dummy.setLayoutData(LayoutUtil.createFillData(2));
         new Label(personsComposite, SWT.NONE);
 
         Label controllerLabel = new Label(personsComposite, SWT.NONE);
         formToolkit.adapt(controllerLabel, true, true);
         controllerLabel.setText("Controller");
         controllerLabel.setLayoutData(LayoutUtil.createLeftGridData());
 
         Button controllerClearButton = formToolkit.createButton(personsComposite, "Clear customer",
                 SWT.PUSH);
         controllerClearButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));
 
         Button controllerSelectionButton = formToolkit.createButton(personsComposite,
                 "Select customer", SWT.PUSH);
         controllerSelectionButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
 
         Label controllerNameLabel = new Label(personsComposite, SWT.NONE);
         controllerNameLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(controllerNameLabel, true, true);
         controllerNameLabel.setText("Name");
 
         controllerNameText = new Text(personsComposite, SWT.READ_ONLY);
         controllerNameText.setLayoutData(LayoutUtil.createHorzCenteredFillData(2));
         formToolkit.adapt(controllerNameText, true, true);
 
         Label controllerStreetLabel = new Label(personsComposite, SWT.NONE);
         controllerStreetLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(controllerStreetLabel, true, true);
         controllerStreetLabel.setText("Street");
 
         controllerStreetText = new Text(personsComposite, SWT.READ_ONLY);
         controllerStreetText.setLayoutData(LayoutUtil.createHorzCenteredFillData(2));
         formToolkit.adapt(controllerStreetText, true, true);
 
         Label controllerCityLabel = new Label(personsComposite, SWT.NONE);
         controllerCityLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(controllerCityLabel, true, true);
         controllerCityLabel.setText("City");
 
         controllerCityText = new Text(personsComposite, SWT.READ_ONLY);
         controllerCityText.setLayoutData(LayoutUtil.createHorzCenteredFillData(2));
         formToolkit.adapt(controllerCityText, true, true);
 
         customerClearButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 clearCustomer();
             }
         });
 
         customerSelectionButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
 
                 ContactSelectionDialog csd = new ContactSelectionDialog(shellProvider.getShell(),
                         context);
                 csd.create();
                 if (csd.open() == Window.OK) {
                     customerNameText.setText(csd.getContact().getName());
                     customerStreetText.setText(csd.getContact().getStreet() + " "
                             + csd.getContact().getStreetNo());
                     customerCityText.setText(csd.getContact().getCity() + " "
                             + csd.getContact().getPostCode());
                 }
 
             }
         });
 
         controllerClearButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 clearController();
             }
 
         });
 
         controllerSelectionButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 ContactSelectionDialog csd = new ContactSelectionDialog(shellProvider.getShell(),
                         context);
                 csd.create();
                 if (csd.open() == Window.OK) {
                     controllerNameText.setText(csd.getContact().getName());
                     controllerStreetText.setText(csd.getContact().getStreet() + " "
                             + csd.getContact().getStreetNo());
                     controllerCityText.setText(csd.getContact().getCity() + " "
                             + csd.getContact().getPostCode());
                 }
             }
         });
 
         requestorClearButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 clearRequestor();
             }
         });
 
         requestorSelectionButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 ContactSelectionDialog csd = new ContactSelectionDialog(shellProvider.getShell(),
                         context);
                 csd.create();
                 if (csd.open() == Window.OK) {
                     requestorNameText.setText(csd.getContact().getName());
                     requestorStreetText.setText(csd.getContact().getStreet() + " "
                             + csd.getContact().getStreetNo());
                     requestorCityText.setText(csd.getContact().getCity() + " "
                             + csd.getContact().getPostCode());
                 }
             }
         });
 
     }
 
     private void createVisualComponents() {
         Composite visualComposite = new Composite(visualSection, SWT.NONE);
         visualComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
         formToolkit.adapt(visualComposite);
         formToolkit.paintBordersFor(visualComposite);
         GridLayout gl = new GridLayout(3, false);
         gl.marginWidth = 1;
         visualComposite.setLayout(gl);
         visualSection.setClient(visualComposite);
 
         Label photosLabel = new Label(visualComposite, SWT.NONE);
         photosLabel.setLayoutData(LayoutUtil.createLeftGridData());
         formToolkit.adapt(photosLabel, true, true);
         photosLabel.setText("Photos");
 
         photosList = new List(visualComposite, SWT.BORDER);
         formToolkit.adapt(photosList, true, true);
         photosList.setLayoutData(LayoutUtil.createHorzFillData());
 
         Button photoEditButton = new Button(visualComposite, SWT.NONE);
         formToolkit.adapt(photoEditButton, true, true);
         photoEditButton.setText("Edit / Import");
         photoEditButton.setLayoutData(LayoutUtil.createRightGridData());
         photoEditButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 importPhotos();
             }
         });
 
         Label titlePhotoLabel = new Label(visualComposite, SWT.NONE);
         titlePhotoLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
         titlePhotoLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(titlePhotoLabel, true, true);
         titlePhotoLabel.setText("Title photo");
 
         titlePhotoComboViewer = new ComboViewer(visualComposite, SWT.NONE);
         Combo titlePhotoCombo = titlePhotoComboViewer.getCombo();
         titlePhotoCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
         formToolkit.paintBordersFor(titlePhotoCombo);
         titlePhotoComboViewer.setContentProvider(ArrayContentProvider.getInstance());
         titlePhotoComboViewer.setLabelProvider(new LabelProvider() {
             @Override
             public String getText(Object element) {
                 if (element instanceof Photo) {
                     Photo current = (Photo) element;
                     return current.getLabel();
                 }
                 return super.getText(element);
             }
         });
         titlePhotoComboViewer.setInput(photos);
         titlePhotoComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
             @Override
             public void selectionChanged(SelectionChangedEvent event) {
                 IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                 selectedTitlePhoto = (Photo) selection.getFirstElement();
                 inspection.setFrontpicture(selectedTitlePhoto);
             }
         });
 
         Label plantPhotoLabel = new Label(visualComposite, SWT.READ_ONLY);
         plantPhotoLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
         plantPhotoLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(plantPhotoLabel, true, true);
         plantPhotoLabel.setText("Plant photo");
 
         plantPhotoComboViewer = new ComboViewer(visualComposite, SWT.NONE);
         Combo plantPhotoCombo = plantPhotoComboViewer.getCombo();
         plantPhotoCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
         formToolkit.paintBordersFor(plantPhotoCombo);
         plantPhotoComboViewer.setContentProvider(ArrayContentProvider.getInstance());
 
         plantPhotoComboViewer.setLabelProvider(new LabelProvider() {
             @Override
             public String getText(Object element) {
                 if (element instanceof Photo) {
                     Photo current = (Photo) element;
                     return current.getLabel();
                 }
                 return super.getText(element);
             }
         });
         plantPhotoComboViewer.setInput(photos);
         plantPhotoComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
             @Override
             public void selectionChanged(SelectionChangedEvent event) {
                 IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                 selectedPlantPhoto = (Photo) selection.getFirstElement();
                 inspection.setPlantpicture(selectedPlantPhoto);
             }
         });
     }
 
     private void createBiologicalComponents() {
         Composite biologicalComposite = new Composite(biologicalSection, SWT.NONE);
         biologicalComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
         formToolkit.adapt(biologicalComposite);
         formToolkit.paintBordersFor(biologicalComposite);
         GridLayout gl = new GridLayout(5, false);
         gl.marginWidth = 1;
         biologicalComposite.setLayout(gl);
         biologicalSection.setClient(biologicalComposite);
 
         new Label(biologicalComposite, SWT.NONE);
 
         Label germsLabel = new Label(biologicalComposite, SWT.NONE);
         formToolkit.adapt(germsLabel, true, true);
         germsLabel.setText("Germs / ml");
         germsLabel.setLayoutData(LayoutUtil.createHorzFillData());
 
         Label gradeLabel = new Label(biologicalComposite, SWT.NONE);
         formToolkit.adapt(gradeLabel, true, true);
         gradeLabel.setText("Grade");
         gradeLabel.setLayoutData(LayoutUtil.createHorzFillData());
 
         Label weightLabel = new Label(biologicalComposite, SWT.NONE);
         formToolkit.adapt(weightLabel, true, true);
         weightLabel.setText("Weight");
         weightLabel.setLayoutData(LayoutUtil.createHorzFillData());
 
         Label commentLabel = new Label(biologicalComposite, SWT.NONE);
         formToolkit.adapt(commentLabel, true, true);
         commentLabel.setText("Comment");
         commentLabel.setLayoutData(LayoutUtil.createHorzFillData());
 
         Label legionellaLabel = new Label(biologicalComposite, SWT.NONE);
         legionellaLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(legionellaLabel, true, true);
         legionellaLabel.setText("Legionella");
 
         legionellaText = new Text(biologicalComposite, SWT.NONE);
         legionellaText.setLayoutData(LayoutUtil.createHorzCenteredFillData());
         formToolkit.adapt(legionellaText, true, true);
         // TODO only numbers (double), fill with value if exists
 
         legionellaGradeCombo = new Combo(biologicalComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
         legionellaGradeCombo.setLayoutData(LayoutUtil.createHorzFillData());
         formToolkit.adapt(legionellaGradeCombo);
         formToolkit.paintBordersFor(legionellaGradeCombo);
         fillGrades(legionellaGradeCombo);
         // TODO select grade if exists
 
         legionellaWeightCombo = new Combo(biologicalComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
         legionellaWeightCombo.setLayoutData(LayoutUtil.createHorzFillData());
         formToolkit.adapt(legionellaWeightCombo);
         formToolkit.paintBordersFor(legionellaWeightCombo);
         fillWeights(legionellaWeightCombo);
         // TODO select weight if exists
 
         legionellaCommentCombo = new Combo(biologicalComposite, SWT.NONE);
         legionellaCommentCombo.setLayoutData(LayoutUtil.createHorzFillData());
         formToolkit.adapt(legionellaCommentCombo);
         formToolkit.paintBordersFor(legionellaCommentCombo);
         ContentProposalUtil.enableContentProposal(legionellaCommentCombo);
         // TODO set comment if exists
 
         Label sumGermsLabel = new Label(biologicalComposite, SWT.NONE);
         sumGermsLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(sumGermsLabel, true, true);
         sumGermsLabel.setText("Sum germs");
 
         sumGermsText = new Text(biologicalComposite, SWT.NONE);
         sumGermsText.setLayoutData(LayoutUtil.createHorzCenteredFillData());
         formToolkit.adapt(sumGermsText, true, true);
         // TODO only numbers (double), fill with value if exists
 
         sumGermsGradeCombo = new Combo(biologicalComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
         sumGermsGradeCombo.setLayoutData(LayoutUtil.createHorzFillData());
         formToolkit.adapt(sumGermsGradeCombo);
         formToolkit.paintBordersFor(sumGermsGradeCombo);
         fillGrades(sumGermsGradeCombo);
         // TODO select grade if exists
 
         sumGermsWeightCombo = new Combo(biologicalComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
         sumGermsWeightCombo.setLayoutData(LayoutUtil.createHorzFillData());
         formToolkit.adapt(sumGermsWeightCombo);
         formToolkit.paintBordersFor(sumGermsWeightCombo);
         fillWeights(sumGermsWeightCombo);
         // TODO select weight if exists
 
         sumGermsCommentCombo = new Combo(biologicalComposite, SWT.NONE);
         sumGermsCommentCombo.setLayoutData(LayoutUtil.createHorzFillData());
         formToolkit.adapt(sumGermsCommentCombo);
         formToolkit.paintBordersFor(sumGermsCommentCombo);
         ContentProposalUtil.enableContentProposal(sumGermsCommentCombo);
         // TODO set comment if exist
     }
 
     private void createPhysicalComponents() {
         Composite physicalComposite = new Composite(physicalSection, SWT.NONE);
         physicalComposite.setBackgroundMode(SWT.INHERIT_DEFAULT);
         formToolkit.adapt(physicalComposite);
         formToolkit.paintBordersFor(physicalComposite);
         GridLayout gl = new GridLayout(5, false);
         gl.marginWidth = 1;
         physicalComposite.setLayout(gl);
         physicalSection.setClient(physicalComposite);
 
         new Label(physicalComposite, SWT.NONE);
 
         Label meassurementLabel = new Label(physicalComposite, SWT.NONE);
         formToolkit.adapt(meassurementLabel, true, true);
         meassurementLabel.setText("Meassurement");
         meassurementLabel.setLayoutData(LayoutUtil.createHorzFillData());
 
         Label gradeLabel = new Label(physicalComposite, SWT.NONE);
         formToolkit.adapt(gradeLabel, true, true);
         gradeLabel.setText("Grade");
         gradeLabel.setLayoutData(LayoutUtil.createHorzFillData());
 
         Label weightLabel = new Label(physicalComposite, SWT.NONE);
         formToolkit.adapt(weightLabel, true, true);
         weightLabel.setText("Weight");
         weightLabel.setLayoutData(LayoutUtil.createHorzFillData());
 
         Label commentLabel = new Label(physicalComposite, SWT.NONE);
         formToolkit.adapt(commentLabel, true, true);
         commentLabel.setText("Comment");
         commentLabel.setLayoutData(LayoutUtil.createHorzFillData());
 
         Label temperatureLabel = new Label(physicalComposite, SWT.NONE);
         temperatureLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(temperatureLabel, true, true);
         temperatureLabel.setText("Temperature");
 
         temperatureText = new Text(physicalComposite, SWT.NONE);
         temperatureText.setLayoutData(LayoutUtil.createHorzCenteredFillData());
         formToolkit.adapt(temperatureText, true, true);
         // TODO only numbers (double), fill with value if exists
 
         temperatureGradeCombo = new Combo(physicalComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
         temperatureGradeCombo.setLayoutData(LayoutUtil.createHorzFillData());
         formToolkit.adapt(temperatureGradeCombo);
         formToolkit.paintBordersFor(temperatureGradeCombo);
         fillGrades(temperatureGradeCombo);
         // TODO select grade if exists
 
         temperatureWeightCombo = new Combo(physicalComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
         temperatureWeightCombo.setLayoutData(LayoutUtil.createHorzFillData());
         formToolkit.adapt(temperatureWeightCombo);
         formToolkit.paintBordersFor(temperatureWeightCombo);
         fillWeights(temperatureWeightCombo);
         // TODO select weight if exists
 
         temperatureCommentCombo = new Combo(physicalComposite, SWT.NONE);
         temperatureCommentCombo.setLayoutData(LayoutUtil.createHorzFillData());
         formToolkit.adapt(temperatureCommentCombo);
         formToolkit.paintBordersFor(temperatureCommentCombo);
         ContentProposalUtil.enableContentProposal(temperatureCommentCombo);
         // TODO set comment if exists
 
         Label humidityLabel = new Label(physicalComposite, SWT.NONE);
         humidityLabel.setLayoutData(LayoutUtil.createLeftCenteredGridData());
         formToolkit.adapt(humidityLabel, true, true);
         humidityLabel.setText("Relative humidity");
 
         humidityText = new Text(physicalComposite, SWT.NONE);
         humidityText.setLayoutData(LayoutUtil.createHorzCenteredFillData());
         formToolkit.adapt(humidityText, true, true);
         // TODO only numbers (double), fill with value if exists
 
         humidityGradeCombo = new Combo(physicalComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
         humidityGradeCombo.setLayoutData(LayoutUtil.createHorzFillData());
         formToolkit.adapt(humidityGradeCombo);
         formToolkit.paintBordersFor(humidityGradeCombo);
         fillGrades(humidityGradeCombo);
         // TODO select grade if exists
 
         humidityWeightCombo = new Combo(physicalComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
         humidityWeightCombo.setLayoutData(LayoutUtil.createHorzFillData());
         formToolkit.adapt(humidityWeightCombo);
         formToolkit.paintBordersFor(humidityWeightCombo);
         fillWeights(humidityWeightCombo);
         // TODO select weight if exists
 
         humitiyCommentCombo = new Combo(physicalComposite, SWT.NONE);
         humitiyCommentCombo.setLayoutData(LayoutUtil.createHorzFillData());
         formToolkit.adapt(humitiyCommentCombo);
         formToolkit.paintBordersFor(humitiyCommentCombo);
         ContentProposalUtil.enableContentProposal(humitiyCommentCombo);
         // TODO set comment if exist
 
     }
 
     private void importPhotos() {
         Optional<java.util.List<Photo>> n = Optional.absent();
 
         PhotoWizard pw = new PhotoWizard(photos);
         ContextInjectionFactory.inject(pw, context);
 
         WizardDialog wd = WizardCreator.createWizardDialog(shellProvider.getShell(), pw);
         wd.open();
         // TODO handle input
 
         String[] tableItems = new String[photos.size()];
         int i = 0;
         for (Photo photo : photos) {
             tableItems[i] = photo.getLabel();
             i++;
         }
         photosList.setItems(tableItems);
         plantPhotoComboViewer.refresh();
         titlePhotoComboViewer.refresh();
 
     }
 
     @Override
     public void setSelectedComponent(Component component) {
         // TODO Auto-generated method stub
 
     }
 
     private void fillGrades(Combo combo) {
         for (int i = 0; i < 6; i++) {
             combo.add(Integer.toString(i));
         }
         combo.select(0);
     }
 
     private void fillWeights(Combo combo) {
         for (int i = 1; i <= 6; i++) {
             combo.add(Integer.toString(i));
         }
         combo.select(0);
     }
 
     @Override
     public void dispose() {
         formToolkit.dispose();
         super.dispose();
     }
 
     @Override
     protected void checkSubclass() {
         // Disable the check that prevents subclassing of SWT components
     }
 
     public void setInspection(Inspection inspection) {
         if (this.inspection != inspection) {
             this.inspection = inspection;
             if (inspection != null) {
                 inspection.addPlantObserver(new Observer<Plant>() {
 
                     @Override
                     public void changed(Plant item) {
                         plantSelected(item);
                     }
                 });
             }
         }
     }
 
     private void initAutoCompletion(Combo combo) {
 
         try {
             Collection<Summary> summaries = evaluationService.findAll();
             String[] s = new String[summaries.size()];
             int i = 0;
 
             for (Summary e : summaries) {
                 s[i] = e.getText();
                 i++;
             }
 
             combo.setItems(s);
             ContentProposalUtil.enableContentProposal(combo);
         }
 
         catch (DatabaseException e) {
 
         }
 
     }
 
     public boolean refreshGeneralInformation() {
 
         if (inspection == null) {
             // TODO Übersetzen
             MessageDialog.openError(shellProvider.getShell(), "Selection error",
                     "No inspection selected.");
             return false;
         }
 
         titleText.setText(inspection.getTitle());
         reportDateTime.setDay(inspection.getReportDate().get(Calendar.DAY_OF_MONTH));
         reportDateTime.setMonth(inspection.getReportDate().get(Calendar.MONTH));
         reportDateTime.setMonth(inspection.getReportDate().get(Calendar.YEAR));
 
         inspectionDateTime.setDay(inspection.getReportDate().get(Calendar.DAY_OF_MONTH));
         inspectionDateTime.setMonth(inspection.getReportDate().get(Calendar.MONTH));
         inspectionDateTime.setYear(inspection.getReportDate().get(Calendar.YEAR));
 
         nextInspectionDateTime
                 .setDay(inspection.getNextInspectionDate().get(Calendar.DAY_OF_MONTH));
         nextInspectionDateTime.setMonth(inspection.getNextInspectionDate().get(Calendar.MONTH));
         nextInspectionDateTime.setYear(inspection.getNextInspectionDate().get(Calendar.YEAR));
 
         plantText.setText(inspection.getPlant().getDescription());
         setlayout(inspection);
 
         fillOptionalFields(inspection);
 
         return true;
 
     }
 
     private void fillOptionalFields(Inspection inspection) {
 
         Contact c = null;
         clearFields();
 
         if (inspection.getContractor().isPresent()) {
             c = inspection.getContractor().get();
             customerNameText.setText(c.getName());
             customerStreetText.setText(c.getStreet() + " " + c.getStreetNo());
             customerCityText.setText(c.getCity() + " " + c.getPostCode());
         }
         if (inspection.getRequester().isPresent()) {
             c = inspection.getRequester().get();
             requestorNameText.setText(c.getName());
             requestorStreetText.setText(c.getStreet() + " " + c.getStreetNo());
             requestorCityText.setText(c.getCity() + " " + c.getPostCode());
         }
         if (inspection.getChecker().isPresent()) {
             c = inspection.getChecker().get();
             controllerNameText.setText(c.getName());
             controllerStreetText.setText(c.getStreet() + " " + c.getStreetNo());
             controllerCityText.setText(c.getCity() + " " + c.getPostCode());
         }
 
         if (!inspection.getSummary().isPresent()) {
 
             initAutoCompletion(overallCombo);
 
         }
         else {
             overallCombo.setText(inspection.getSummary().get());
         }
     }
 
     private void setlayout(Inspection inspection) {
 
         String[] items = reportStyleCombo.getItems();
         int i = 0;
         while (!inspection.getLayout().getName().equals(items[i])) {
             i++;
         }
         reportStyleCombo.select(i);
 
     }
 
     protected Inspection getInspection() {
         return inspection;
     }
 
     private void clearFields() {
         clearCustomer();
         clearRequestor();
         clearController();
 
     }
 
     private void clearCustomer() {
         customerNameText.setText("");
         customerStreetText.setText("");
         customerCityText.setText("");
     }
 
     private void clearRequestor() {
         requestorNameText.setText("");
         requestorStreetText.setText("");
         requestorCityText.setText("");
     }
 
     private void clearController() {
         controllerNameText.setText("");
         controllerStreetText.setText("");
         controllerCityText.setText("");
 
     }
 }
