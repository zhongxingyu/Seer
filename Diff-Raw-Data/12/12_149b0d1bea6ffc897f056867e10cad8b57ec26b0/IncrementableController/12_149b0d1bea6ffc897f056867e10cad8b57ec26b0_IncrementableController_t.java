 package co.edu.udea.ludens.web;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 import javax.faces.component.UIData;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 
import co.edu.udea.ludens.domain.Game;
 import co.edu.udea.ludens.domain.Incrementable;
 import co.edu.udea.ludens.domain.IncrementableConstraint;
 import co.edu.udea.ludens.enums.EnumElementType;
 import co.edu.udea.ludens.exceptions.LudensException;
 import co.edu.udea.ludens.services.ElementService;
 import co.edu.udea.ludens.services.GameService;
 import co.edu.udea.ludens.services.IncrementableService;
 import co.edu.udea.ludens.util.UpdateableView;
 
 import com.icesoft.faces.component.inputfile.InputFile;
 import common.Logger;
 
 public class IncrementableController implements UpdateableView {
 
 	private Logger logger = Logger.getLogger(getClass());
 	private Incrementable actualIncrementable = new Incrementable();
 	private IncrementableConstraint actualConstraint;
 
 	private boolean listingElements = true;
 	private boolean editingElement = false;
 	private boolean addingIncrementableImage = false;
 	private boolean settingUpConstraints = false;
 
 	private UIData incrementablesTable;
 	private UIData imagesIncrementableTable;
 
 	private static final String FACTORES_LABEL = "Factores";
 	private static final String RECURSOS_LABEL = "Materiales";
 	private String[] typesNames = { FACTORES_LABEL, RECURSOS_LABEL };
 	private List<SelectItem> incrementableTypes = new ArrayList<SelectItem>();
 	private List<SelectItem> materialItems = new ArrayList<SelectItem>();
 	private List<Incrementable> incrementables = new ArrayList<Incrementable>();
 
 	private GameController gameController;
 
 	private String constraintMaterial;
 
 	private IncrementableService incrementableService;
 	private ElementService elementService;
 	private GameService gameService;
 
 	private String action;
 
 	@PostConstruct
 	public void loadIncrementables() {
 		if (incrementableTypes == null || incrementableTypes.isEmpty()) {
 			EnumElementType[] types = EnumElementType.values();
 
 			for (EnumElementType type : types) {
 				incrementableTypes.add(new SelectItem(type.getType(), type
 						.getType()));
 			}
 		}
 
 		incrementables = incrementableService.getAllIncrementablesGame(gameController.getActualGame()
 						.getName());
 
 		if (incrementables != null && !incrementables.isEmpty()) {
 			Incrementable incr = incrementables.get(0);
 			if (incr.getConstraints() != null
 					&& !incr.getConstraints().isEmpty()) {
 				settingUpConstraints = true;
 
 				return;
 			}
 		}
 	}
 
 	public void addImageIncrementable(javax.faces.event.ActionEvent event) {
 		incrementableService.save(actualIncrementable);
 		this.addingIncrementableImage = false;
 	}
 
 	public void cancelAddImage(javax.faces.event.ActionEvent event) {
 		this.addingIncrementableImage = false;
 	}
 
 	public void settingUpGame(javax.faces.event.ActionEvent event) {
 		this.gameController.settingUpGame(event);
 		listingElements = true;
 		editingElement = false;
 		addingIncrementableImage = false;
 
 		loadIncrementables();
 	}
 
 	public void saveIncrementable(javax.faces.event.ActionEvent event) {
 		editingElement = false;
 
 		logger.info("Incrementable type to save: "
 				+ actualIncrementable.getType());
	
		Game game = gameController.getActualGame();
		actualIncrementable.setGame(game);
		
 		incrementableService.save(actualIncrementable);
 		actualIncrementable = new Incrementable();
 		loadIncrementables();
 	}
 
 	public void newIncrementable(javax.faces.event.ActionEvent event)
 			throws LudensException {
 		editingElement = true;
 		actualIncrementable = new Incrementable();
 		actualIncrementable.setType(EnumElementType.MATERIAL);
 		actualIncrementable.setGame(gameController.getActualGame());
 	}
 
 	public void setupConstraints(javax.faces.event.ActionEvent event)
 			throws LudensException {
 
 		logger.debug("action  .." + action);
 		incrementables = incrementableService
 				.getAllIncrementablesGame(gameController.getActualGame()
 						.getName());
 		int i = 0;
 		for (Incrementable incr : incrementables) {
 			i++;
 			logger.debug(i + " Incrementable " + incr.getName());
 		}
 
 		if ("action_setupconstraints".equalsIgnoreCase(action)) {
 			logger.debug("creating constraints ..");
 
 			for (Incrementable incr : incrementables) {
 				incrementableService.createResourceConstraints(incr);
 
 			}
 			settingUpConstraints = true;
 		}
 
 		if ("action_deleteallconstraints".equalsIgnoreCase(action)) {
 
 			logger.debug("Deleting constraints ..");
 
 			for (Incrementable incr : incrementables) {
 				incrementableService.deleteResourceConstraints(incr);
 			}
 			settingUpConstraints = false;
 		}
 		loadIncrementables();
 	}
 
 	public void editingIncrementable(javax.faces.event.ActionEvent event)
 			throws LudensException {
 		editingElement = true;
 		int selectedRowIndex = incrementablesTable.getRowIndex();
 		logger.info("Selected row" + selectedRowIndex);
 		actualIncrementable = incrementables.get(selectedRowIndex);
 		logger.info("Selected " + actualIncrementable.getName());
 	}
 
 	public void listElements(javax.faces.event.ActionEvent event) {
 		this.listingElements = true;
 	}
 
 	public void addNewImage(javax.faces.event.ActionEvent event) {
 		this.addingIncrementableImage = true;
 	}
 
 	public void deleteIncrementable(javax.faces.event.ActionEvent event) {
 		int selectedRowIndex = incrementablesTable.getRowIndex();
 		logger.info("Selected row" + selectedRowIndex);
 
 		if (selectedRowIndex < 0)
 			return;
 
 		Incrementable incr = incrementables.get(selectedRowIndex);
 		logger.info("Incrementable to delete " + incr.getName());
 		incrementableService.delete(incr);
 
 		loadIncrementables();
 	}
 
 	public void cancelEditing(javax.faces.event.ActionEvent event) {
 		editingElement = false;
 		actualIncrementable = new Incrementable();
 
 		loadIncrementables();
 	}
 
 	public void uploadActionListener(ActionEvent actionEvent)
 			throws LudensException {
 		InputFile inputFile = (InputFile) actionEvent.getSource();
 
 		if (!inputFile.getFileInfo().isSaved()) {
 			throw new LudensException("No fue posible guardar la imágen");
 		}
 
 		actualIncrementable.setImageUrl(inputFile.getFileInfo().getFile()
 				.getName());
 	}
 
 	public void changeType(ValueChangeEvent vce) {
 
 		if (vce.getNewValue() != null) {
 			EnumElementType type = EnumElementType.getElementType(vce
 					.getNewValue().toString());
 			logger.info("Setting type " + type);
 			actualIncrementable.setType(type);
 		}
 	}
 
 	/**
 	 * @param typesNames
 	 *            the typesNames to set
 	 */
 	public void setTypesNames(String[] typesNames) {
 		this.typesNames = typesNames;
 	}
 
 	/**
 	 * @return the typesNames
 	 */
 	public String[] getTypesNames() {
 
 		return (this.typesNames);
 	}
 
 	public Incrementable getActualIncrementable() {
 
 		return (this.actualIncrementable);
 	}
 
 	public List<SelectItem> getIncrementableTypes() {
 
 		return (this.incrementableTypes);
 	}
 
 	public List<Incrementable> getIncrementables() {
 
 		return (this.incrementables);
 	}
 
 	public void setActualIncrementable(Incrementable actualIncrementable) {
 		this.actualIncrementable = actualIncrementable;
 	}
 
 	public void setIncrementableTypes(List<SelectItem> incrementableTypes) {
 		this.incrementableTypes = incrementableTypes;
 	}
 
 	public void setIncrementables(List<Incrementable> incrementables) {
 		this.incrementables = incrementables;
 	}
 
 	/**
 	 * @param incrementablesTable
 	 *            the incrementablesTable to set
 	 */
 	public void setIncrementablesTable(UIData incrementablesTable) {
 		this.incrementablesTable = incrementablesTable;
 	}
 
 	/**
 	 * @return the incrementablesTable
 	 */
 	public UIData getIncrementablesTable() {
 
 		return (this.incrementablesTable);
 	}
 
 	/**
 	 * @param incrementableService
 	 *            the incrementableService to set
 	 */
 	public void setIncrementableService(
 			IncrementableService incrementableService) {
 		this.incrementableService = incrementableService;
 	}
 
 	/**
 	 * @return the incrementableService
 	 */
 	public IncrementableService getIncrementableService() {
 
 		return (this.incrementableService);
 	}
 
 	/**
 	 * @param imagesIncrementableTable
 	 *            the imagesIncrementableTable to set
 	 */
 	public void setImagesIncrementableTable(UIData imagesIncrementableTable) {
 		this.imagesIncrementableTable = imagesIncrementableTable;
 	}
 
 	/**
 	 * @return the imagesIncrementableTable
 	 */
 	public UIData getImagesIncrementableTable() {
 
 		return (this.imagesIncrementableTable);
 	}
 
 	/**
 	 * @param gameController
 	 *            the gameController to set
 	 */
 	public void setGameController(GameController gameController) {
 		this.gameController = gameController;
 	}
 
 	/**
 	 * @return the gameController
 	 */
 	public GameController getGameController() {
 
 		return (this.gameController);
 	}
 
 	public boolean isListingElements() {
 
 		return (this.listingElements);
 	}
 
 	public boolean isEditingElement() {
 
 		return (this.editingElement);
 	}
 
 	public boolean isAddingIncrementableImage() {
 
 		return (this.addingIncrementableImage);
 	}
 
 	public void setListingElements(boolean listingElements) {
 		this.listingElements = listingElements;
 	}
 
 	public void setEditingElement(boolean editingElement) {
 		this.editingElement = editingElement;
 	}
 
 	public void setAddingIncrementableImage(boolean addingIncrementableImage) {
 		this.addingIncrementableImage = addingIncrementableImage;
 	}
 
 	/**
 	 * @param elementService
 	 *            the elementService to set
 	 */
 	public void setElementService(ElementService elementService) {
 		this.elementService = elementService;
 	}
 
 	/**
 	 * @return the elementService
 	 */
 	public ElementService getElementService() {
 
 		return (this.elementService);
 	}
 
 	@Override
 	public void update() {
 		logger.info("Updating IncrementableController");
 
 		loadIncrementables();
 	}
 
 	/**
 	 * @param materials
 	 *            the materials to set
 	 */
 	public void setMaterials(List<SelectItem> materials) {
 		this.materialItems = materials;
 	}
 
 	public List<SelectItem> getMaterialItems() {
 
 		return (this.materialItems);
 	}
 
 	public void setMaterialItems(List<SelectItem> materialItems) {
 		this.materialItems = materialItems;
 	}
 
 	/**
 	 * @return the materials
 	 */
 	public List<SelectItem> getMaterials() {
 
 		return (this.materialItems);
 	}
 
 	/**
 	 * @param actualConstraint
 	 *            the actualConstraint to set
 	 */
 	public void setActualConstraint(IncrementableConstraint actualConstraint) {
 		this.actualConstraint = actualConstraint;
 	}
 
 	/**
 	 * @return the actualConstraint
 	 */
 	public IncrementableConstraint getActualConstraint() {
 
 		return (this.actualConstraint);
 	}
 
 	/**
 	 * @param constraintMaterial
 	 *            the constraintMaterial to set
 	 */
 	public void setConstraintMaterial(String constraintMaterial) {
 		this.constraintMaterial = constraintMaterial;
 	}
 
 	/**
 	 * @return the constraintMaterial
 	 */
 	public String getConstraintMaterial() {
 
 		return (this.constraintMaterial);
 	}
 
 	public void setGameService(GameService gameService) {
 		this.gameService = gameService;
 	}
 
 	public void setSettingUpConstraints(boolean settingUpConstraints) {
 		this.settingUpConstraints = settingUpConstraints;
 	}
 
 	public boolean isSettingUpConstraints() {
 
 		return (this.settingUpConstraints);
 	}
 
 	public void setAction(String action) {
 		this.action = action;
 	}
 }
