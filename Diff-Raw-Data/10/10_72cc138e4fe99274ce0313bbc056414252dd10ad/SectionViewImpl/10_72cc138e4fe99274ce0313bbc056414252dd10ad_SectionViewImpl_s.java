 package org.jtalks.poulpe.web.controller.section;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import javax.swing.tree.TreeNode;
 
 import org.jtalks.poulpe.model.entity.Branch;
 import org.jtalks.poulpe.model.entity.Persistent;
 import org.jtalks.poulpe.model.entity.Section;
 
 import org.jtalks.poulpe.web.controller.DialogManager;
 import org.jtalks.poulpe.web.controller.DialogManagerImpl;
 import org.jtalks.poulpe.web.controller.branch.BranchPresenter;
 import org.zkoss.util.resource.Labels;
 import org.zkoss.zk.ui.AbstractComponent;
 import org.zkoss.zk.ui.Components;
 import org.zkoss.zk.ui.event.Event;
 import org.zkoss.zk.ui.event.EventListener;
 import org.zkoss.zk.ui.event.Events;
 import org.zkoss.zk.ui.ext.AfterCompose;
 import org.zkoss.zul.Button;
 import org.zkoss.zul.Combobox;
 import org.zkoss.zul.Comboitem;
 import org.zkoss.zul.ComboitemRenderer;
 import org.zkoss.zul.DefaultTreeModel;
 import org.zkoss.zul.DefaultTreeNode;
 import org.zkoss.zul.Hbox;
 import org.zkoss.zul.Label;
 import org.zkoss.zul.ListModelList;
 import org.zkoss.zul.Listbox;
 import org.zkoss.zul.Listcell;
 import org.zkoss.zul.Listitem;
 import org.zkoss.zul.ListitemRenderer;
 import org.zkoss.zul.Radio;
 import org.zkoss.zul.SimpleTreeNode;
 import org.zkoss.zul.Textbox;
 import org.zkoss.zul.Tree;
 import org.zkoss.zul.TreeModel;
 import org.zkoss.zul.Treecell;
 import org.zkoss.zul.Treeitem;
 import org.zkoss.zul.TreeitemRenderer;
 import org.zkoss.zul.Treerow;
 import org.zkoss.zul.Vbox;
 import org.zkoss.zul.Window;
 import org.zkoss.zul.event.TreeDataListener;
 
 public class SectionViewImpl extends Window implements SectionView,
 		AfterCompose {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -2745900622179593542L;
 	private transient SectionPresenter presenter;
 
 	private Window newSectionDialog;
 	private Textbox newSectionDialog$sectionName;
 	private Textbox newSectionDialog$sectionDescription;
 	private Window editSectionDialog;
 	private Textbox editSectionDialog$sectionName;
 	private Textbox editSectionDialog$sectionDescription;
 
 	// private Window deleteSectionDialog;
 	// private Combobox deleteSectionDialog$sectionsCombobox;
 	// private Radio deleteSectionDialog$deleteAll;
 	// private Radio deleteSectionDialog$transferAll;
 
 	@Override
 	public void afterCompose() {
 		Components.addForwards(this, this);
 		Components.wireVariables(this, this);
 
 		presenter.initView(this);
 
 	}
 
 	@Override
 	public void showSection(Section section) {
 		// TODO move SectionTreeComponent creation to external factory method
 		getChildren().add(new SectionTreeComponentImpl(section, presenter));
 	}
 
 	@Override
 	public void showSections(List<Section> sections) {
 		getChildren().clear();
 		for (Section section : sections) {
 			// TODO move SectionTreeComponent creation to external factory
 			// method
 			getChildren().add(new SectionTreeComponentImpl(section, presenter));
 		}
 	}
 
 	/**
 	 * Set presenter
 	 * 
 	 * @see BranchPresenter
 	 * @param presenter
 	 *            instance presenter for view
 	 * */
 	public void setPresenter(SectionPresenter presenter) {
 		this.presenter = presenter;
 	}
 
 	// EVENT HANDLERS
 
 	/**
 	 * Event which happen when user click on '+' buttton opens dialog for adding
 	 * new section
 	 * */
 	public void onClick$addSectionButton() {
 		presenter.openNewSectionDialog();
 	}
 
 	/**
 	 * Delete section from view
 	 */
 	@Override
 	public void removeSection(Section section) {
 	}
 
 	/** Open new branch dialog */
 	@Override
 	public void openNewSectionDialog() {
 		setNewSectionName("");
 		setNewSectionDescription("");
 		newSectionDialog.setVisible(true);
 	}
 
 	/**
 	 * Event which happen when user click on cancel button in new branch dialog
 	 * window this cause close new branch dialog
 	 * */
 	public void onClick$closeButton$newSectionDialog() {
 		closeNewSectionDialog();
 	}
 
 	/**
 	 * Event occurs when the used click on cancel button in the
 	 * DeleteSectionDialog
 	 */
 	public void onClick$closeButton$deleteSectionDialog() {
 		closeDeleteSectionDialog();
 	}
 
 	/**
 	 * Event occurs when the used click on delete button in the
 	 * DeleteSectionDialog
 	 */
 	public void onClick$deleteButton$deleteSectionDialog() {
 		// if (deleteSectionDialog$deleteAll.isChecked()) {
 		// presenter.deleteSection(null);
 		// } else if (deleteSectionDialog$transferAll.isChecked()) {
 		// Object selectedObject = deleteSectionDialog$sectionsCombobox
 		// .getModel().getElementAt(
 		// deleteSectionDialog$sectionsCombobox
 		// .getSelectedIndex());
 		// presenter.deleteSection((Section) selectedObject);
 		// }
 	}
 
 	/**
 	 * Event which happen when user click on add button in new branch dialog
 	 * window this cause save new branch
 	 * */
 	public void onClick$addButton$newSectionDialog() {
 		presenter
 				.addNewSection(getNewSectionName(), getNewSectionDescription());
 	}
 
 	/**
 	 * Event which happen when user click on cancel button in new branch dialog
 	 * window this cause close new branch dialog
 	 * */
 	public void onClick$closeButton$editSectionDialog() {
 		closeEditSectionDialog();
 	}
 
 	/**
 	 * Event which happen when user click on add button in new branch dialog
 	 * window this cause save new branch
 	 * */
 	public void onClick$editButton$editSectionDialog() {
 		presenter
 				.editSection(getEditSectionName(), getEditSectionDescription());
 
 	}
 
 	// public void onClick$deleteAll$deleteSectionDialog() {
 	// if (deleteSectionDialog$deleteAll.isChecked()) {
 	// deleteSectionDialog$sectionsCombobox.setDisabled(true);
 	// }
 	// }
 	//
 	// public void onClick$transferAll$deleteSectionDialog() {
 	// if (deleteSectionDialog$transferAll.isChecked()) {
 	// deleteSectionDialog$sectionsCombobox.setDisabled(false);
 	// }
 	// }
 
 	/** {@inheritDoc} */
 	@Override
 	public void closeNewSectionDialog() {
 		newSectionDialog.setVisible(false);
 		setNewSectionDescription("");
 		setNewSectionName("");
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void closeEditSectionDialog() {
 		editSectionDialog.setVisible(false);
 		editSectionDialog$sectionName.setText("");
 		editSectionDialog$sectionDescription.setText("");
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void closeDeleteSectionDialog() {
 		// deleteSectionDialog$transferAll.setChecked(true);
 		// deleteSectionDialog$sectionsCombobox.setModel(new ListModelList());
 		// deleteSectionDialog.setVisible(false);
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void openDeleteSectionDialog(Section victim) {
 		Events.postEvent(new Event("onOpenDeleteSectionDialog", getDesktop()
 				.getPage("sectionDeleteDialog").getFellow("deleteWindow"),
 				victim));
 	}
 
 	/** Open new branch dialog */
 	@Override
 	public void openEditSectionDialog(String name, String description) {
 		setEditSectionName(name);
 		setEditSectionDescription(description);
 		editSectionDialog.setVisible(true);
 	}
 
 	/**
 	 * get the value of sectionName textField of newSectionDialog
 	 * 
 	 * @return
 	 */
 	private String getNewSectionName() {
 		return newSectionDialog$sectionName.getText();
 	}
 
 	/**
 	 * get the value of sectionDescription textField of newSectionDialog
 	 * 
 	 * @return
 	 */
 	private String getNewSectionDescription() {
 		return newSectionDialog$sectionDescription.getText();
 	}
 
 	private String getEditSectionName() {
 		return editSectionDialog$sectionName.getText();
 	}
 
 	private String getEditSectionDescription() {
 		return editSectionDialog$sectionDescription.getText();
 	}
 
 	private void setEditSectionName(String name) {
 		editSectionDialog$sectionName.setText(name);
 	}
 
 	private void setEditSectionDescription(String description) {
 		editSectionDialog$sectionDescription.setText(description);
 	}
 
 	private void setNewSectionName(String name) {
 		newSectionDialog$sectionName.setText(name);
 	}
 
 	private void setNewSectionDescription(String description) {
 		newSectionDialog$sectionDescription.setText(description);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * */
 	@Override
 	public void openErrorPopupInNewSectionDialog(String label) {
 		final String message = Labels.getLabel(label);
 		newSectionDialog$sectionName.setErrorMessage(message);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * */
 	@Override
 	public void openErrorPopupInEditSectionDialog(String label) {
 		final String message = Labels.getLabel(label);
 		editSectionDialog$sectionName.setErrorMessage(message);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * */
 
 	@Override
 	public void closeDialogs() {
 		closeNewSectionDialog();
 		closeEditSectionDialog();
 		closeDeleteSectionDialog();
 	}
 
 	@Override
 	public void openNewBranchDialog() {
 		Events.postEvent(new Event("onOpenAddDialog", getDesktop().getPage(
 				"BranchDialog").getFellow("editWindow")));
 	}
 
 	@Override
 	public void closeNewBranchDialog() {
 		// TODO : implement
 	}
 
 	@Override
 	public void openEditBranchDialog(Branch branch) {
 		Events.postEvent(new Event("onOpenEditDialog", getDesktop().getPage(
 				"BranchDialog").getFellow("editWindow"), branch));
 	}
 
 	public void openDeleteBranchDialog(Branch branch) {
 
 	}
 
 	@Override
 	public void closeEditBranchDialog() {
 
 	}
 
 	@Override
 	public boolean isDeleteDialogOpen() {
 		return false;
 		// return deleteSectionDialog.isVisible();
 	}
 
 	@Override
 	public boolean isEditDialogOpen() {
 		return editSectionDialog.isVisible();
 	}
 
 	@Override
 	public boolean isNewDialogOpen() {
 		return newSectionDialog.isVisible();
 	}
 
 	/**
 	 * Handle event when child dialog was hiding
 	 * */
 	public void onHideDialog() {
 		presenter.updateView();
 	}
 
 }
