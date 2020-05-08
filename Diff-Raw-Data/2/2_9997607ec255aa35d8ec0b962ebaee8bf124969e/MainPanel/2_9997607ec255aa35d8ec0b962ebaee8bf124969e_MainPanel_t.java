 package ch.bfh.ti.soed.white.mhc_pms.ui;
 
 import ch.bfh.ti.soed.white.mhc_pms.controller.ComponentChangeListener;
 import ch.bfh.ti.soed.white.mhc_pms.controller.NavigationEvent;
 import ch.bfh.ti.soed.white.mhc_pms.ui.MenuBarComponent.ButtonEnum;
 
 import com.vaadin.navigator.Navigator;
 import com.vaadin.navigator.View;
 import com.vaadin.ui.HorizontalSplitPanel;
 import com.vaadin.ui.Panel;
 import com.vaadin.ui.VerticalSplitPanel;
 
 /**
  * @author		Gruppe White, I2p, BFH Bern, <a href="https://github.com/fabaff/ch.bfh.bti7081.s2013.white">Contact</a>
  * @version		0.0.2
  * @since		0.0.1
  * 
  * The main panel of the GUI.
  */
 class MainPanel extends HorizontalSplitPanel implements ComponentChangeListener {
 
 	// Static vars for positioning
 	private static final long serialVersionUID = 6726671929546867989L;
 	private static final float VERTICAL_SPLIT_POS = 20.0f;
 	private static final float HORIZONTAL_SPLIT_POS = 10.0f;
 
 	/*
 	 * Screen layout of the GUI 
 	 * +----+-------------------------+ 
 	 * |    |                     10% |
 	 * |    +-------------------------+ 
 	 * |    |                         |
 	 * |    |                         |
 	 * | 20%|                         |
 	 * +----+-------------------------+
 	 */
 
 	// Layout elements section
 	private VerticalSplitPanel verticalPanel = new VerticalSplitPanel();
 	private TitleBarComponent titleBar = new TitleBarComponent();
 	private PatientTableComponent patientTable = new PatientTableComponent();
 	private PatientInfoComponent patInfo = new PatientInfoComponent();
 	private CaseInfoComponent caseInfo = new CaseInfoComponent();
 	private PatientProgressComponent progressComp = new PatientProgressComponent();
 	private DiagnosisComponent diagnosisComp = new DiagnosisComponent();
 	private MedicationComponent medComp = new MedicationComponent();
 	private NewPatientComponent newPatientComp = new NewPatientComponent();
 	private EditCaseInfoComponent editCaseInfoComp = new EditCaseInfoComponent();
 	private MenuBarComponent menuBar;
 	private Navigator navigator;
 
 	public MainPanel(PmsUI pmsUI) {
 		Panel detailPanel = new Panel();
 		this.navigator = new Navigator(pmsUI, detailPanel);
 		this.menuBar = new MenuBarComponent(this.navigator);
 		this.newPatientComp = new NewPatientComponent();
 		this.editCaseInfoComp = new EditCaseInfoComponent();
 
 		detailPanel.setSizeFull();
 		this.setFirstComponent(this.menuBar);
 		this.setSecondComponent(this.verticalPanel);
 		
 		// Unit of the values is % (percent)
 		this.setSplitPosition(HORIZONTAL_SPLIT_POS, Unit.PERCENTAGE);
 		this.verticalPanel
 				.setSplitPosition(VERTICAL_SPLIT_POS, Unit.PERCENTAGE);
 		this.verticalPanel.setFirstComponent(this.titleBar);
 		this.verticalPanel.setSecondComponent(detailPanel);
 
 		this.addNavigatorViews();
 		this.addMenuBarListeners();
 		this.addPatientTableListeners();
 		this.addPatientInfoListeners();
 		this.addNewPatientListeners();
 		this.addCaseInfoListeners();
 		this.addEditCaseListeners();
 
 		// TODO Add missing Component Listener
 		// setComponentAlignment(button, Alignment.MIDDLE_CENTER);
 		// Notification.show("Welcome to the Animal Farm");
 	}
 
 	private void addEditCaseListeners() {
 		// Edit Case section
 		this.editCaseInfoComp.addPmsComponentListener(this.titleBar);
 		this.editCaseInfoComp.addPmsComponentListener(this.patientTable);
 		this.editCaseInfoComp.addPmsComponentListener(this.patInfo);
 		this.editCaseInfoComp.addPmsComponentListener(this.caseInfo);
 		this.editCaseInfoComp.addPmsComponentListener(this.progressComp);
 		this.editCaseInfoComp.addPmsComponentListener(this.diagnosisComp);
 		this.editCaseInfoComp.addPmsComponentListener(this.medComp);
 		this.editCaseInfoComp.addUIActivationListener(this.menuBar);
 		this.editCaseInfoComp.addUIActivationListener(this.titleBar);
 		this.editCaseInfoComp.addUIActivationListener(this.menuBar);
 		this.editCaseInfoComp.addUIActivationListener(this.titleBar);
 		this.editCaseInfoComp.addComponentChangeListener(this);
 	}
 
 	private void addCaseInfoListeners() {
 		// Case info section
 		this.caseInfo.addUIActivationListener(this.menuBar);
 		this.caseInfo.addUIActivationListener(this.titleBar);
 		this.caseInfo.addComponentChangeListener(this);
 		this.caseInfo.addNewCaseListener(this.editCaseInfoComp);
 	}
 
 	private void addNewPatientListeners() {
 		// New Patient section
 		this.newPatientComp.addPmsComponentListener(this.titleBar);
 		this.newPatientComp.addPmsComponentListener(this.patientTable);
 		this.newPatientComp.addPmsComponentListener(this.patInfo);
 		this.newPatientComp.addPmsComponentListener(this.caseInfo);
 		this.newPatientComp.addPmsComponentListener(this.progressComp);
 		this.newPatientComp.addPmsComponentListener(this.diagnosisComp);
 		this.newPatientComp.addPmsComponentListener(this.medComp);
 		this.newPatientComp.addUIActivationListener(this.menuBar);
 		this.newPatientComp.addUIActivationListener(this.titleBar);
 		this.newPatientComp.addComponentChangeListener(this);
 	}
 
 	private void addPatientInfoListeners() {
 		// Patient info section
 		this.patInfo.addUIActivationListener(this.menuBar);
 		this.patInfo.addUIActivationListener(this.titleBar);
 		this.patInfo.addComponentChangeListener(this);
 	}
 
 	private void addPatientTableListeners() {
 		// Patient table section
 		this.patientTable.addPmsComponentListener(this.titleBar);
 		this.patientTable.addPmsComponentListener(this.patInfo);
 		this.patientTable.addPmsComponentListener(this.caseInfo);
 		this.patientTable.addPmsComponentListener(this.progressComp);
 		this.patientTable.addPmsComponentListener(this.diagnosisComp);
 		this.patientTable.addPmsComponentListener(this.medComp);
 		this.patientTable.addUIActivationListener(this.menuBar);
 		this.patientTable.addUIActivationListener(this.titleBar);
 		this.patientTable.addComponentChangeListener(this);
 		this.patientTable.addNewCaseListener(this.editCaseInfoComp);
 	}
 
 	private void addMenuBarListeners() {
 		this.menuBar.addPmsComponentListener(this.titleBar);
 		this.menuBar.addPmsComponentListener(this.patientTable);
 		this.menuBar.addPmsComponentListener(this.patInfo);
 		this.menuBar.addPmsComponentListener(this.caseInfo);
 		this.menuBar.addPmsComponentListener(this.progressComp);
 		this.menuBar.addPmsComponentListener(this.diagnosisComp);
 		this.menuBar.addPmsComponentListener(this.medComp);
 	}
 
 	private void addNavigatorViews() {
 		this.navigator.addView(MenuBarComponent.ButtonEnum.HOME.toString(),
 				this.patientTable);
 		this.navigator.addView(
 				MenuBarComponent.ButtonEnum.PATIENT_INFO.toString(),
 				this.patInfo);
 		this.navigator
 				.addView(MenuBarComponent.ButtonEnum.CASE_INFO.toString(),
 						this.caseInfo);
 		this.navigator.addView(
 				MenuBarComponent.ButtonEnum.PATIENT_PROGRESS.toString(),
 				this.progressComp);
 		this.navigator.addView(
 				MenuBarComponent.ButtonEnum.DIAGNOSIS.toString(),
 				this.diagnosisComp);
 		this.navigator
 				.addView(MenuBarComponent.ButtonEnum.MEDICATION.toString(),
 						this.medComp);
 		this.navigator.navigateTo(MenuBarComponent.ButtonEnum.HOME.toString());
 	}
 
 	@Override
 	public void componentChange(NavigationEvent event) {
 		switch (event) {
 		case PATIENT:
 			this.navigate(MenuBarComponent.ButtonEnum.PATIENT_INFO, this.newPatientComp);
 			break;
 		case PCASE:
 			this.navigate(MenuBarComponent.ButtonEnum.CASE_INFO, this.editCaseInfoComp);
 			break;
 		case PATIENT_BACK:
 			this.navigate(MenuBarComponent.ButtonEnum.PATIENT_INFO, this.patInfo);
 			break;
 		case PCASE_BACK:
 			this.navigate(MenuBarComponent.ButtonEnum.CASE_INFO, this.caseInfo);
 			break;
 		}
 	}
 
 	private void navigate(ButtonEnum btnEnum, View view) {
		this.navigator.addView(MenuBarComponent.ButtonEnum.PATIENT_INFO.toString(), this.patInfo);
		this.navigator.addView(MenuBarComponent.ButtonEnum.CASE_INFO.toString(), this.caseInfo);
 		this.navigator.addView(btnEnum.toString(), view);
 		this.navigator.navigateTo(btnEnum.toString());
 	}
 }
