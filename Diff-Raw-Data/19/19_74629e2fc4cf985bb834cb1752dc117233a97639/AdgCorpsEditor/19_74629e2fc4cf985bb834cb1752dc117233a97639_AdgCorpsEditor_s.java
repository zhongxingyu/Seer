 package fr.abu.newlab.adg.client.editor;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 import com.google.gwt.editor.client.Editor;
 import com.google.gwt.editor.client.adapters.ListEditor;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.user.client.ui.Grid;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.TextBox;
 
 import fr.abu.newlab.adg.client.editor.source.TroopEditorSource;
 import fr.abu.newlab.adg.client.manager.ArmyManager;
 import fr.abu.newlab.adg.client.manager.TotalComputer;
 import fr.abu.thelab.common.model.adg.AdgCorps;
 import fr.abu.thelab.common.model.adg.AdgTroop;
 import fr.abu.thelab.common.ui.widget.DoubleLabel;
 import fr.abu.thelab.common.ui.widget.IntegerLabel;
 import fr.abu.thelab.common.ui.widget.StringListBox;
 
 public class AdgCorpsEditor extends EditorWithCss implements Editor<AdgCorps> {
 
 	private final static Logger LOG = Logger.getLogger(AdgCorpsEditor.class
 			.getName());
 
 	@Ignore
 	Label lbGeneralName;
 
 	@Ignore
 	Label lbGeneralQuality;
 
 	@Ignore
 	Label lbGeneralType;
 
 	@Ignore
 	Label lbGeneralBudget;
 
 	@Ignore
 	Label lbAlly;
 
 	@Ignore
 	Label lbTroopQty;
 
 	@Ignore
 	Label lbTroopDescription;
 
 	@Ignore
 	Label lbTroopType;
 
 	@Ignore
 	Label lbTroopQuality;
 
 	@Ignore
 	Label lbTroopBudget;
 
 	@Ignore
 	Label lbTroopTotal;
 
 	@Path("name")
 	Label corpsName;
 
 	TextBox ally;
 
 	@Path("general.name")
 	TextBox generalName;
 
 	@Path("general.quality")
 	StringListBox generalQuality;
 
 	@Path("general.type")
 	StringListBox generalType;
 
 	@Ignore
 	DoubleLabel generalBudget;
 
 	@Ignore
 	IntegerLabel unitQty;
 
 	@Ignore
 	DoubleLabel corpsTotal;
 
 	private final Grid grid;
	private boolean formatReduit;
 	private final int corpsIndex;
 
 	public AdgCorpsEditor(Grid grid, int corpsIndex) {
 		this.grid = grid;
		formatReduit = vm.isFormatReduit();
 		this.corpsIndex = corpsIndex;
 
 		int rowIndex = ArmyManager.getInstance().computeCorpsRowIndex(
 				corpsIndex);
 		grid.insertRow(rowIndex);
 		grid.insertRow(rowIndex);
 		grid.insertRow(rowIndex);
 		grid.insertRow(rowIndex);
 		grid.insertRow(rowIndex);
 
 		// premiere ligne
 		corpsName = new Label();
 		lbGeneralName = new Label();
 		lbGeneralName.setText(uiConstants.lbGeneralName());
 		lbGeneralQuality = new Label();
 		lbGeneralQuality.setText(uiConstants.lbGeneralQuality());
 		lbGeneralType = new Label();
 		lbGeneralType.setText(uiConstants.lbGeneralType());
 		lbGeneralBudget = new Label();
 		lbGeneralBudget.setText(uiConstants.lbBudget());
 
 		// deuxieme ligne
 		lbAlly = new Label();
 		lbAlly.setText(uiConstants.lbAlly());
 		ally = new TextBox();
 		generalName = new TextBox();
 		generalQuality = new StringListBox();
 		generalType = new StringListBox();
 		generalBudget = new DoubleLabel();
 
 		// troisieme ligne
 		lbTroopQty = new Label();
 		lbTroopQty.setText(uiConstants.lbNombre());
 		lbTroopDescription = new Label();
 		lbTroopDescription.setText(uiConstants.lbTroopDescription());
 		lbTroopType = new Label();
 		lbTroopType.setText(uiConstants.lbTroopType());
 		lbTroopQuality = new Label();
 		lbTroopQuality.setText(uiConstants.lbTroopQuality());
 		lbTroopBudget = new Label();
 		lbTroopBudget.setText(uiConstants.lbBudget());
 		lbTroopTotal = new Label();
 		lbTroopTotal.setText(uiConstants.lbTotal());
 
 		// quatrime ligne
 		corpsTotal = new DoubleLabel();
 		unitQty = new IntegerLabel();
 
 		// remplissage des combo
 		for (String item : uiConstants.generalQualities()) {
 			generalQuality.addItem(item);
 		}
 
 		for (String item : vm.getGeneralTypes()) {
 			generalType.addItem(item);
 		}
 
 		addHandlers();
 
 		// Placement des widgets dans la grille
 		grid.setWidget(rowIndex, 0, corpsName);
 		grid.setWidget(rowIndex, 2, lbGeneralName);
 		grid.setWidget(rowIndex, 3, lbGeneralQuality);
 		grid.setWidget(rowIndex, 4, lbGeneralType);
 		grid.setWidget(rowIndex, 5, lbGeneralBudget);
 
 		grid.setWidget(rowIndex + 1, 0, lbAlly);
 		grid.setWidget(rowIndex + 1, 1, ally);
 		grid.setWidget(rowIndex + 1, 2, generalName);
 		grid.setWidget(rowIndex + 1, 3, generalQuality);
 		grid.setWidget(rowIndex + 1, 4, generalType);
 		grid.setWidget(rowIndex + 1, 5, generalBudget);
 
 		grid.setWidget(rowIndex + 2, 0, lbTroopQty);
 		grid.setWidget(rowIndex + 2, 1, lbTroopDescription);
 		grid.setWidget(rowIndex + 2, 2, lbTroopType);
 		grid.setWidget(rowIndex + 2, 3, lbTroopQuality);
 		grid.setWidget(rowIndex + 2, 4, lbTroopBudget);
 		grid.setWidget(rowIndex + 2, 5, lbTroopTotal);
 
 		grid.setWidget(rowIndex + 3, 0, unitQty);
 		grid.setWidget(rowIndex + 3, 5, corpsTotal);
 
 		addStyles(rowIndex);
 
 		createTroopListEditor(corpsIndex);
 	}
 
 	private void addHandlers() {
 		ChangeHandler changeGeneralHandler = new ChangeHandler() {
 
 			@Override
 			public void onChange(ChangeEvent event) {
 				computeGeneralTotal();
 			}
 		};
 		generalQuality.addChangeHandler(changeGeneralHandler);
 		generalType.addChangeHandler(changeGeneralHandler);
 	}
 
 	private void computeGeneralTotal() {
 		Double oldBudget = generalBudget.getValue();
 		Double newBudget = TotalComputer.computeTotalGeneral(
 				generalQuality.getValue(), generalType.getValue());
 
 		if (!oldBudget.equals(newBudget)) {
 			generalBudget.setValue(newBudget);
 			dm.fireChangeArmy();
 		}
 	}
 
 	private void computeBudgetTotal() {
 		corpsTotal.setValue(TotalComputer.computeTotalCorps(am
 				.getCorps(corpsIndex)));
 	}
 
 	private void computeUnitTotal() {
 		unitQty.setValue(TotalComputer.computeUnitQty(am.getCorps(corpsIndex)));
 	}
 
 	public void removeFromGrid() {
 
 		// on supprime les troupes
 		listEditor.getList().clear();
 
 		dm.removeTroopDriver(corpsIndex);
 
 		// on parcours la grille pour rechercher l'emplacement du corps
 		int rowIndex = -1;
 		for (int index = 0; index < grid.getRowCount(); index++) {
 			if (grid.getWidget(index, 0) == corpsName) {
 				// on supprime aussi la ligne blanche au dessus du corps
 				rowIndex = index - 1;
 				break;
 			}
 		}
 
 		if (rowIndex >= 0) {
 			LOG.info("remove " + corpsName.getText() + " starting at rowIndex "
 					+ rowIndex);
 			grid.removeRow(rowIndex);
 			grid.removeRow(rowIndex);
 			grid.removeRow(rowIndex);
 			grid.removeRow(rowIndex);
 			grid.removeRow(rowIndex);
 		}
 	}
 
 	@Override
 	public void refreshUI() {
 		// reconstruire la liste box des types si le format a chang
 		if (formatHasChanged()) {
			formatReduit = vm.isFormatReduit();
 
 			generalType.clear();
 			for (String item : vm.getGeneralTypes()) {
 				generalType.addItem(item);
 			}
 			generalType.setValue(vm.getGeneralTypes()[0]);
 
 			dm.fireChangeArmy();
 		}
 
 		computeGeneralTotal();
 		computeBudgetTotal();
 		computeUnitTotal();
 
 		for (AdgTroopEditor editor : listEditor.getEditors()) {
 			editor.refreshUI();
 		}
 
 		if (isFull()) {
 			List<AdgTroop> displayedList = listEditor.getList();
 			displayedList.add(am.addNewTroop(corpsIndex));
 		}
 	}
 
 	private boolean isFull() {
 		for (AdgTroop troop : am.getCorps(corpsIndex).getTroops()) {
 			if (TotalComputer.computeTotalTroop(troop.getQuantity(),
 					troop.getQuality(), troop.getType()) == 0) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Dtecte si un passage du format normal eu format rduit ou vice versa a
 	 * eu lieu.<br>
 	 * Au format rduit le type de gnral est rduit puisqu'il n'y a pas de /s
 	 * gnral.
 	 */
 	private boolean formatHasChanged() {
		return formatReduit != vm.isFormatReduit();
 	}
 
 	@Override
 	protected void addStyles(int row) {
 		//FIX pour un bug d'affichage qui apparait avec 4me corps
 		//la ligne vide de sparation contient alors des bordures latrales
 		setCellRowStyle(grid, row - 1, css.emptyLine());
 		
 		setHeaderRow(grid, row);
 		setInputCells(grid, row + 1, 0, 4, 5);
 		setRightInputCells(grid, row + 1, 3);
 		setBoldRightInputCells(grid, row + 1, 1, 2);
 		setResumeRow(grid, row + 2);
 
 		setResumeCell(grid, row + 3, 0);
 		setResumeCell(grid, row + 3, 5);
 		for (int i = 1; i < 5; i++) {
 			setResumeEmptyCell(grid, row + 3, i);
 		}
 
 		ally.setWidth(uiConstants.columnsWidth()[1]);
 		generalName.setWidth(uiConstants.columnsWidth()[2]);
 		generalQuality.setWidth(uiConstants.columnsWidth()[3]);
 		generalType.setWidth(uiConstants.columnsWidth()[4]);
 	}
 
 	// GESTION DES TROUPES
 
 	private ListEditor<AdgTroop, AdgTroopEditor> listEditor;
 
 	private void createTroopListEditor(int corpsIndex) {
 
 		// Use a ListEditor that uses our AdgTroopSource
 		listEditor = ListEditor.of(new TroopEditorSource(grid, corpsIndex));
 
 		dm.addTroopDriver(corpsIndex, listEditor);
 
 		List<AdgTroop> displayedList = listEditor.getList();
 		displayedList.addAll(ArmyManager.getInstance().getTroops(corpsIndex));
 	}
 
 }
