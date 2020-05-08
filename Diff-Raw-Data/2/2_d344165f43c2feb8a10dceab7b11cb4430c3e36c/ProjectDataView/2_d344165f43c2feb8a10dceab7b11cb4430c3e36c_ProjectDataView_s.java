 package ch.hsr.waktu.gui.qt.view.projectmanagment;
 
 import ch.hsr.waktu.controller.datacontroller.ProjectController;
 import ch.hsr.waktu.domain.Project;
 import ch.hsr.waktu.domain.Usr;
 import ch.hsr.waktu.gui.qt.model.ComboBoxData;
 import ch.hsr.waktu.guicontroller.GuiController;
 import ch.hsr.waktu.guicontroller.LanguageController;
 import ch.hsr.waktu.services.WaktuException;
 
 import com.trolltech.qt.core.QCoreApplication;
 import com.trolltech.qt.gui.QWidget;
 
 public class ProjectDataView extends QWidget {
 
 	private Ui_ProjectData ui = new Ui_ProjectData();
 	private Project project;
 	public Signal1<String> errorMessage = new Signal1<String>();
 
 	public ProjectDataView(Project project) {
 		this.project = project;
 	}
 
 	public void initialize() {
 		ui.setupUi(this);
 		ui.btnAdd.clicked.connect(this, "addClicked()");
 		try {
 			ComboBoxData.createProjectManagerComboBox(ui.cmbProjectManager);
 		} catch (WaktuException e) {
 			errorMessage.emit(e.getMessage());
 		}
 
 		ProjectController.getInstance().update.connect(this, "updateData()");
 		ProjectController.getInstance().add.connect(this, "addData(Project)");
 		
 
 		LanguageController.getInstance().languageChanged.connect(this, "translate()");
 
 		setFields();
 	}
 
 	private void setFields() {
 		if (project != null) {
 			if (GuiController.getInstance().canModifyProject()) {
 				ui.btnAdd.setVisible(true);
 				ui.btnAdd.setText(QCoreApplication.translate("ProjectDataView", "Save", null));
 
 				ui.txtDescription.setText(project.getDescription());
 				ui.txtDescription.setEnabled(true);
 
 				ui.txtProjectnumber.setText(project.getProjectIdentifier());
 				ui.txtProjectnumber.setEnabled(false);
 
 				ui.txtPlannedTime.setValue(project.getPlannedTime());
 				ui.txtPlannedTime.setEnabled(true);
 
 				ui.txtPlannedTime.setValue(project.getPlannedTime());
 				ui.txtPlannedTime.setEnabled(true);
 
 				ui.checkBox.setChecked(!project.isActive());
 				ui.checkBox.setEnabled(true);
 
 			} else {
 				ui.btnAdd.setVisible(false);
 
 				ui.txtDescription.setText(project.getDescription());
 				ui.txtDescription.setEnabled(false);
 
 				ui.txtProjectnumber.setText(project.getProjectIdentifier());
 				ui.txtProjectnumber.setEnabled(false);
 
 				ui.txtPlannedTime.setValue(project.getPlannedTime());
 				ui.txtPlannedTime.setEnabled(false);
 
 				ui.txtPlannedTime.setValue(project.getPlannedTime());
 				ui.txtPlannedTime.setEnabled(false);
 
 				ui.checkBox.setChecked(!project.isActive());
 				ui.checkBox.setEnabled(false);
 			}
 		} else {
 			ui.btnAdd.setVisible(true);
 
 			ui.txtDescription.setEnabled(true);
 			ui.txtProjectnumber.setEnabled(true);
 			ui.txtPlannedTime.setEnabled(true);
 			ui.txtPlannedTime.setEnabled(true);
 			ui.checkBox.setEnabled(true);
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private void addClicked() {
 		if (project == null) {
 			addNewProject();
 		} else {
 			saveProject();
 		}
 		setFields();
 	}
 
 	private void addNewProject() {
 		try {
 			project = ProjectController.getInstance().addProject(ui.txtProjectnumber.text(), ui.txtDescription.text(),
 					(Usr) ui.cmbProjectManager.itemData(ui.cmbProjectManager.currentIndex()), ui.txtPlannedTime.value());
 		} catch (WaktuException e) {
 			errorMessage.emit(e.getMessage());
 		}
 	}
 
 	private void saveProject() {
		project.setDescription(ui.lblDescription.text());
 		project.setPlannedTime(ui.txtPlannedTime.value());
 		project.setProjectManager((Usr) ui.cmbProjectManager.itemData(ui.cmbProjectManager.currentIndex()));
 		project.setActiveState(!ui.checkBox.isChecked());
 		try {
 			ProjectController.getInstance().updateProject(project);
 		} catch (WaktuException e) {
 			errorMessage.emit(e.getMessage());
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private void updateData() {
 		setFields();
 	}
 
 	@SuppressWarnings("unused")
 	private void addData(Project project) {
 		setFields();
 	}
 
 	@SuppressWarnings("unused")
 	private void translate() {
 		ui.retranslateUi(this);
 		changeText();
 	}
 
 	private void changeText() {
 		if (project != null) {
 			ui.btnAdd.setText(QCoreApplication.translate("ProjectDataView", "Save", null));
 		} else {
 			ui.btnAdd.setText(QCoreApplication.translate("ProjectDataView", "Add", null));
 		}
 	}
 }
