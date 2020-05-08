 package server;
 
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 
 import org.primefaces.component.menuitem.MenuItem;
 import org.primefaces.component.submenu.Submenu;
 import org.primefaces.model.DefaultMenuModel;
 import org.primefaces.model.MenuModel;
 
 import ctrl.DBExRules;
 import ctrl.DBField;
 import ctrl.DBModule;
 import ctrl.DBNotification;
 import ctrl.DBSubject;
 import data.ModulActionListener;
 import data.Editable;
 import data.ExRules;
 import data.Field;
 import data.ModManual;
 import data.Modification;
 import data.ModificationNotification;
 import data.Module;
 import data.Subject;
 
 @ManagedBean(name = "ModulBean")
 @SessionScoped
 public class ModulBean {
 
 	public static final String PRUEFORDNUNG = "Pr\u00FCfungsordnungen";
 	public static final String MODMANUAL = "Modulhandb\u00FCcher";
 	public static final String MODULE = "Module";
 	public static final String FAECHER = "F\u00E4cher";
 	public static final String TO_FOR = "message-log";
 
 	// Add your tag id's for ajax-update on menuItemClick here.
 	public static final String UPDATE_AJAX = "list-menu-edit back-menu-edit scrollPanel-edit";
 
 	private String exRules, modMan, module;
 	private List<ExRules> exRulesList;
 	private List<ModManual> modManList;
 	private List<Module> moduleList;
 	private List<Subject> subjectList;
 	List<Field> fieldList, oldFieldList;
 	private MenuModel model, backModel;
 
 	private String title, description, ects, aim;
 	private boolean mainVisible, ectsAimVisible, addInfoVisible;
 	private boolean descriptionEdit, ectsEdit, aimEdit, fieldTitleEdit,
 			fieldDescriptionEdit;
 	private boolean editable, emptyFieldList;
 	private Editable selectedEditable;
 	private String message;
 
 	public ModulBean() {
 		backModel = new DefaultMenuModel();
 
 		model = new DefaultMenuModel();
 		Submenu submenu = new Submenu();
 		submenu.setLabel(PRUEFORDNUNG);
 
 		exRulesList = DBExRules.loadAllExRules();
 
 		for (int i = 0; i < exRulesList.size(); i++) {
 			MenuItem m = new MenuItem();
 			m.setValue(exRulesList.get(i).getExRulesTitle());
 			m.setAjax(true);
 			m.setUpdate(UPDATE_AJAX);
 			m.addActionListener(new ModulActionListener());
 			submenu.getChildren().add(m);
 		}
 		model.addSubmenu(submenu);
 
 	}
 
 	/**
 	 * Adds a new field to fieldList
 	 */
 	public void addField(int index) {
 		if (selectedEditable instanceof Subject) {
 			Subject sub = (Subject) selectedEditable;
 			if (!emptyFieldList) {
 				fieldList.add(
 						index + 1,
 						new Field("", sub.getVersion(), sub.getSubTitle(), sub
 								.getModTitle(), ""));
 			} else {
 				fieldList.add(new Field("", sub.getVersion(),
 						sub.getSubTitle(), sub.getModTitle(), ""));
 			}
 
 			System.out.println(fieldList.size() + " " + oldFieldList.size());
 			emptyFieldList = false;
 		}
 	}
 
 	/**
 	 * removes a field from fieldList
 	 */
 	public void removeField(int index) {
 		fieldList.remove(index);
 		if (fieldList.size() == 0)
 			emptyFieldList = true;
 	}
 
 	/**
 	 * this method is called when clicking a Subject in the menu parameter is
 	 * the clicked subject
 	 * 
 	 * @param sub
 	 */
 	public void handleSubject(Subject sub) {
 		// TODO Paste your event handling here
 		selectedEditable = sub;
 		oldFieldList = DBField.loadFieldList(sub.getModTitle(),
 				sub.getVersion(), sub.getSubTitle());
 		fieldList = new LinkedList<Field>(oldFieldList);
 		if (fieldList.size() == 0)
 			emptyFieldList = true;
 		title = sub.getSubTitle();
 		description = sub.getDescription();
 		ects = String.valueOf(sub.getEcts());
 		aim = sub.getAim();
 		mainVisible = true;
 		ectsAimVisible = true;
 		addInfoVisible = true;
 		// sets the ability to edit
 		descriptionEdit = false;
 		ectsEdit = false;
 		aimEdit = false;
 		fieldTitleEdit = false;
 		fieldDescriptionEdit = false;
 		// set editable
 		editable = true;
 		// Only Test and can be removed
 		System.out.println(sub.getSubTitle());
 	}
 
 	/**
 	 * this method is called when clicking a Module in the menu parameter is the
 	 * clicked module
 	 * 
 	 * @param mod
 	 */
 	public void handleModule(Module mod) {
 		// TODO Paste your event handling here
 		selectedEditable = mod;
 		title = mod.getModTitle();
 		description = mod.getDescription();
 		mainVisible = true;
 		ectsAimVisible = false;
 		addInfoVisible = false;
 		// sets the ability to edit
 		descriptionEdit = true;
 		ectsEdit = true;
 		aimEdit = true;
 		fieldTitleEdit = true;
 		fieldDescriptionEdit = true;
 		// set editable
 		editable = false;
 		// Only Test and can be removed
 		System.out.println(mod.getModTitle());
 		System.out.println(mod.getDescription());
 	}
 
 	/**
 	 * this method is called when clicking a ModManual in the menu parameter is
 	 * the clicked modManual
 	 * 
 	 * @param modMan
 	 */
 	public void handleModManual(ModManual modMan) {
 		// TODO Paste your event handling here
 		selectedEditable = modMan;
 		title = modMan.getModManTitle();
 		description = modMan.getDescription();
 		mainVisible = true;
 		ectsAimVisible = false;
 		addInfoVisible = false;
 		// sets the ability to edit
 		descriptionEdit = true;
 		ectsEdit = true;
 		aimEdit = true;
 		fieldTitleEdit = true;
 		fieldDescriptionEdit = true;
 		// set editable
 		editable = false;
 		// Only Test and can be removed
 		System.out.println(modMan.getModManTitle());
 
 	}
 
 	/**
 	 * this method is called when clicking a ExRule in the menu parameter is the
 	 * clicked exRule
 	 * 
 	 * @param rule
 	 */
 	public void handleExRule(ExRules rule) {
 		// TODO Paste your event handling here
 		selectedEditable = rule;
 		title = rule.getExRulesTitle();
 		description = "";
 		mainVisible = true;
 		ectsAimVisible = false;
 		addInfoVisible = false;
 		// sets the ability to edit
 		descriptionEdit = true;
 		ectsEdit = true;
 		aimEdit = true;
 		fieldTitleEdit = true;
 		fieldDescriptionEdit = true;
 		// set editable
 		editable = false;
 		// Only Test and can be removed
 		System.out.println(rule.getExRulesTitle());
 	}
 
 	/**
 	 * accept the changes that were made
 	 */
 	public void accept() {
 		if (isEditable()) {
 			// get the highest Version
 			int max = 0;
 			if (selectedEditable instanceof Subject) {
 				Subject oldSub = (Subject) selectedEditable;
 				max = oldSub.getVersion();
 				if (oldSub.isAck()) {
 					Subject newSub = new Subject(max + 1, title,
 							oldSub.getModTitle(), description, aim,
 							Integer.valueOf(ects), false);
 
 					// if old sub isn't the same as the new sub
 					// we create new database entries and create a notification
 					if (!oldSub.equals(newSub)) {
 						System.out.println("not equal");
 						if (handleAccept(newSub, oldSub)) {
 							oldFieldList = new LinkedList<Field>(fieldList);
 							selectedEditable = newSub;
 							addMessage(TO_FOR, "Erfolg: ",
 									"Ihre Änderung wurde erfolgreich verschickt.");
 						}
 					} else {
 						System.out.println(fieldList.size() + " "
 								+ oldFieldList.size());
 						boolean differ = false;
 						if (fieldList.size() != oldFieldList.size()) {
 							System.out.println("Unterschiedlich lang");
 							differ = true;
 						} else {
 							for (int i = 0; i < oldFieldList.size(); i++) {
 								if (!fieldList.get(i).equals(
 										oldFieldList.get(i))) {
 									differ = true;
 									break;
 								}
 							}
 						}
 						if (differ) {
 							System.out.println("differ");
 							boolean empty = false;
 							for (Field field : fieldList) {
 								if (field.getFieldTitle() == "") {
 									// TODO inform about empty field
 									addErrorMessage(TO_FOR, "Leeres Feld: ",
 											"Bitte tragen Sie einen Titel ein oder löschen Sie das Feld.");
 									empty = true;
 								}
 							}
 							if (!empty) {
 								if (handleAccept(newSub, oldSub)) {
 									oldFieldList = new LinkedList<Field>(
 											fieldList);
 									selectedEditable = newSub;
 									addMessage(TO_FOR, "Erfolg: ",
 											"Ihre Änderung wurde erfolgreich verschickt.");
 								} else {
 									// TODO inform about existing change
 									addErrorMessage(TO_FOR,
 											"Änderung existiert bereits: ",
 											"Bitte löschen Sie die bestehende Änderung oder warten Sie auf Bestätigung.");
 								}
 							}
 						} else {
 							addErrorMessage(TO_FOR, "Fach ist identisch: ",
 									"Bitte tätigen Sie zuerst eine Änderung.");
 						}
 					}
 				} else {
 					addErrorMessage(TO_FOR, "Änderung existiert bereits: ",
 							"Bitte löschen Sie die bestehende Änderung oder warten Sie auf Bestätigung.");
 				}
 			} else if (selectedEditable instanceof Module) {
 				Module oldMod = (Module) selectedEditable;
 				Module newMod = new Module(title, description);
 				if (!oldMod.equals(newMod)) {
 					if (handleAccept(newMod, oldMod)) {
 						selectedEditable = newMod;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Declines changes that were made
 	 */
 	public void decline() {
 		if (selectedEditable instanceof Subject) {
 			Subject oldSub = (Subject) selectedEditable;
 			fieldList = new LinkedList<Field>(oldFieldList);
 			title = oldSub.getSubTitle();
 			description = oldSub.getDescription();
 			ects = String.valueOf(oldSub.getEcts());
 			aim = oldSub.getAim();
 		} else if (selectedEditable instanceof Module) {
 			Module mod = (Module) selectedEditable;
 			title = mod.getModTitle();
 			description = mod.getDescription();
 		} else if (selectedEditable instanceof ModManual) {
 			ModManual modMan = (ModManual) selectedEditable;
 			title = modMan.getModManTitle();
 			description = modMan.getDescription();
 		} else if (selectedEditable instanceof ExRules) {
 			ExRules exRule = (ExRules) selectedEditable;
 			title = exRule.getExRulesTitle();
 		}
 	}
 
 	/**
 	 * 
 	 * @param newSub
 	 * @param oldSub
 	 * @return true if changes in database were made false otherwise
 	 */
 	private boolean handleAccept(Subject newSub, Subject oldSub) {
 		try {
 			DBSubject.saveSubject(newSub);
 			for (int i = 0; i < fieldList.size(); i++) {
 				fieldList.get(i).setSubjectversion(newSub.getVersion());
 				DBField.saveField(fieldList.get(i));
 			}
 			ModificationNotification mn = new ModificationNotification(
 					"adam.admin@uni-ulm.de", "adam.admin@uni-ulm.de",
 					new Timestamp(System.currentTimeMillis()), message, "edit",
 					"queued", false, new Modification(oldSub, newSub));
 			DBNotification.saveNotification(mn);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * 
 	 * @param newMod
 	 * @param oldMod
 	 * @return true if changes in database were made false otherwise
 	 */
 	private boolean handleAccept(Module newMod, Module oldMod) {
 		try {
 			DBModule.updateModule(newMod, oldMod.getModTitle());
 		} catch (Exception e) {
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * if anything goes wrong - display an Error
 	 * 
 	 * @param title
 	 * @param msg
 	 */
 	public void addErrorMessage(String toFor, String title, String msg) {
 		FacesContext.getCurrentInstance().addMessage(toFor,
 				new FacesMessage(FacesMessage.SEVERITY_FATAL, title, msg));
 	}
 
 	/**
 	 * Informs the User via message.
 	 * 
 	 * @param title
 	 * @param msg
 	 */
 	public void addMessage(String toFor, String title, String msg) {
 		FacesContext.getCurrentInstance().addMessage(toFor,
 				new FacesMessage(FacesMessage.SEVERITY_INFO, title, msg));
 	}
 
 	/**
 	 * @return the backModel
 	 */
 	public MenuModel getBackModel() {
 		return backModel;
 	}
 
 	/**
 	 * @param backModel
 	 *            the backModel to set
 	 */
 	public void setBackModel(MenuModel backModel) {
 		this.backModel = backModel;
 	}
 
 	/**
 	 * @return the exRules
 	 */
 	public String getExRules() {
 		return exRules;
 	}
 
 	/**
 	 * @param exRules
 	 *            the exRules to set
 	 */
 	public void setExRules(String exRules) {
 		this.exRules = exRules;
 	}
 
 	/**
 	 * @return the modMan
 	 */
 	public String getModMan() {
 		return modMan;
 	}
 
 	/**
 	 * @param modMan
 	 *            the modMan to set
 	 */
 	public void setModMan(String modMan) {
 		this.modMan = modMan;
 	}
 
 	/**
 	 * @return the module
 	 */
 	public String getModule() {
 		return module;
 	}
 
 	/**
 	 * @param module
 	 *            the module to set
 	 */
 	public void setModule(String module) {
 		this.module = module;
 	}
 
 	/**
 	 * @return the exRulesList
 	 */
 	public List<ExRules> getExRulesList() {
 		return exRulesList;
 	}
 
 	/**
 	 * @param exRulesList
 	 *            the exRulesList to set
 	 */
 	public void setExRulesList(List<ExRules> exRulesList) {
 		this.exRulesList = exRulesList;
 	}
 
 	/**
 	 * @return the modManList
 	 */
 	public List<ModManual> getModManList() {
 		return modManList;
 	}
 
 	/**
 	 * @param modManList
 	 *            the modManList to set
 	 */
 	public void setModManList(List<ModManual> modManList) {
 		this.modManList = modManList;
 	}
 
 	/**
 	 * @return the moduleList
 	 */
 	public List<Module> getModuleList() {
 		return moduleList;
 	}
 
 	/**
 	 * @param moduleList
 	 *            the moduleList to set
 	 */
 	public void setModuleList(List<Module> moduleList) {
 		this.moduleList = moduleList;
 	}
 
 	/**
 	 * @return the model
 	 */
 	public MenuModel getModel() {
 		return model;
 	}
 
 	/**
 	 * @param model
 	 *            the model to set
 	 */
 	public void setModel(MenuModel model) {
 		this.model = model;
 	}
 
 	/**
 	 * @return the subjectList
 	 */
 	public List<Subject> getSubjectList() {
 		return subjectList;
 	}
 
 	/**
 	 * @param subjectList
 	 *            the subjectList to set
 	 */
 	public void setSubjectList(List<Subject> subjectList) {
 		this.subjectList = subjectList;
 	}
 
 	/**
 	 * @return the fieldList
 	 */
 	public List<Field> getFieldList() {
 		return fieldList;
 	}
 
 	/**
 	 * @param fieldList
 	 *            the fieldList to set
 	 */
 	public void setFieldList(List<Field> fieldList) {
 		this.fieldList = fieldList;
 	}
 
 	/**
 	 * @return the title
 	 */
 	public String getTitle() {
 		return title;
 	}
 
 	/**
 	 * @param title
 	 *            the title to set
 	 */
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * @param description
 	 *            the description to set
 	 */
 	public void setDescription(String description) {
 		System.out.println(description);
 		this.description = description;
 	}
 
 	/**
 	 * @return the ects
 	 */
 	public String getEcts() {
 		return ects;
 	}
 
 	/**
 	 * @param ects
 	 *            the ects to set
 	 */
 	public void setEcts(String ects) {
 		this.ects = ects;
 	}
 
 	/**
 	 * @return the aim
 	 */
 	public String getAim() {
 		return aim;
 	}
 
 	/**
 	 * @param aim
 	 *            the aim to set
 	 */
 	public void setAim(String aim) {
 		this.aim = aim;
 	}
 
 	/**
 	 * @return the mainVisible
 	 */
 	public boolean isMainVisible() {
 		return mainVisible;
 	}
 
 	/**
 	 * @param mainVisible
 	 *            the mainVisible to set
 	 */
 	public void setMainVisible(boolean mainVisible) {
 		this.mainVisible = mainVisible;
 	}
 
 	/**
 	 * @return the ectsAimVisible
 	 */
 	public boolean isEctsAimVisible() {
 		return ectsAimVisible;
 	}
 
 	/**
 	 * @param ectsAimVisible
 	 *            the ectsAimVisible to set
 	 */
 	public void setEctsAimVisible(boolean ectsAimVisible) {
 		this.ectsAimVisible = ectsAimVisible;
 	}
 
 	/**
 	 * @return the addInfoVisible
 	 */
 	public boolean isAddInfoVisible() {
 		return addInfoVisible;
 	}
 
 	/**
 	 * @param addInfoVisible
 	 *            the addInfoVisible to set
 	 */
 	public void setAddInfoVisible(boolean addInfoVisible) {
 		this.addInfoVisible = addInfoVisible;
 	}
 
 	/**
 	 * @return the descriptionEdit
 	 */
 	public boolean isDescriptionEdit() {
 		return descriptionEdit;
 	}
 
 	/**
 	 * @param descriptionEdit
 	 *            the descriptionEdit to set
 	 */
 	public void setDescriptionEdit(boolean descriptionEdit) {
 		this.descriptionEdit = descriptionEdit;
 	}
 
 	/**
 	 * @return the ectsEdit
 	 */
 	public boolean isEctsEdit() {
 		return ectsEdit;
 	}
 
 	/**
 	 * @param ectsEdit
 	 *            the ectsEdit to set
 	 */
 	public void setEctsEdit(boolean ectsEdit) {
 		this.ectsEdit = ectsEdit;
 	}
 
 	/**
 	 * @return the aimEdit
 	 */
 	public boolean isAimEdit() {
 		return aimEdit;
 	}
 
 	/**
 	 * @param aimEdit
 	 *            the aimEdit to set
 	 */
 	public void setAimEdit(boolean aimEdit) {
 		this.aimEdit = aimEdit;
 	}
 
 	/**
 	 * @return the fieldTitleEdit
 	 */
 	public boolean isFieldTitleEdit() {
 		return fieldTitleEdit;
 	}
 
 	/**
 	 * @param fieldTitleEdit
 	 *            the fieldTitleEdit to set
 	 */
 	public void setFieldTitleEdit(boolean fieldTitleEdit) {
 		this.fieldTitleEdit = fieldTitleEdit;
 	}
 
 	/**
 	 * @return the fieldDescriptionEdit
 	 */
 	public boolean isFieldDescriptionEdit() {
 		return fieldDescriptionEdit;
 	}
 
 	/**
 	 * @param fieldDescriptionEdit
 	 *            the fieldDescriptionEdit to set
 	 */
 	public void setFieldDescriptionEdit(boolean fieldDescriptionEdit) {
 		this.fieldDescriptionEdit = fieldDescriptionEdit;
 	}
 
 	/**
 	 * @return the editable
 	 */
 	public boolean isEditable() {
 		return editable;
 	}
 
 	/**
 	 * @param editable
 	 *            the editable to set
 	 */
 	public void setEditable(boolean editable) {
 		this.editable = editable;
 	}
 
 	/**
 	 * @return the oldFieldList
 	 */
 	public List<Field> getOldFieldList() {
 		return oldFieldList;
 	}
 
 	/**
 	 * @param oldFieldList
 	 *            the oldFieldList to set
 	 */
 	public void setOldFieldList(List<Field> oldFieldList) {
 		this.oldFieldList = oldFieldList;
 	}
 
 	/**
 	 * @return the selectedEditable
 	 */
 	public Editable getSelectedEditable() {
 		return selectedEditable;
 	}
 
 	/**
 	 * @param selectedEditable
 	 *            the selectedEditable to set
 	 */
 	public void setSelectedEditable(Editable selectedEditable) {
 		this.selectedEditable = selectedEditable;
 	}
 
 	/**
 	 * @return the message
 	 */
 	public String getMessage() {
 		return message;
 	}
 
 	/**
 	 * @param message
 	 *            the message to set
 	 */
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	/**
 	 * @return the emptyFieldList
 	 */
 	public boolean isEmptyFieldList() {
 		return emptyFieldList;
 	}
 
 	/**
 	 * @param emptyFieldList
 	 *            the emptyFieldList to set
 	 */
 	public void setEmptyFieldList(boolean emptyFieldList) {
 		this.emptyFieldList = emptyFieldList;
 	}
 
 }
