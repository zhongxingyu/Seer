 package fr.abu.newlab.adg.client.manager;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import com.google.gwt.core.client.GWT;
 
 import fr.abu.newlab.adg.client.constants.AdgUIConstants;
 import fr.abu.thelab.common.model.adg.AdgArmy;
 import fr.abu.thelab.common.model.adg.AdgCorps;
 import fr.abu.thelab.common.model.adg.AdgGeneral;
 import fr.abu.thelab.common.model.adg.AdgRule;
 import fr.abu.thelab.common.model.adg.AdgTroop;
 
 public class ArmyManager {
 
 	private final static Logger LOG = Logger.getLogger(ArmyManager.class
 			.getName());
 
 	private static VersionManager vm = VersionManager.getInstance();
 	private static BudgetManager bm = BudgetManager.getInstance();
 	private static AdgUIConstants uiConstants = GWT
 			.create(AdgUIConstants.class);
 
 	private static ArmyManager INSTANCE;
 
 	private final AdgArmy army;
 
 	private static final int headerArmySize = 7;
 	private static final int fixedCorpsSize = 5;
 
 	private ArmyManager() {
 		LOG.info("instanciation");
 		army = createDefaultArmy();
 	}
 
 	public AdgArmy getArmy() {
 		return army;
 	}
 
 	public static ArmyManager getInstance() {
 		if (INSTANCE == null) {
 			INSTANCE = new ArmyManager();
 		}
 		return INSTANCE;
 	}
 
 	private AdgArmy createDefaultArmy() {
 		AdgArmy army = new AdgArmy();
 
 		AdgRule rule = new AdgRule();
 		rule.setBudget(bm.defaultBudget());
 		rule.setVersion(vm.defaultVersion());
 		army.setRule(rule);
 
 		army.setTerrain1(uiConstants.terrains()[0]);
 		army.setTerrain2(uiConstants.terrains()[0]);
 		army.setTerrain3(uiConstants.terrains()[0]);
 		army.setTerrain4(uiConstants.terrains()[0]);
 
 		for (int index = 0; index < bm.getCorpsQty(); index++) {
 			AdgCorps corps = createCorps(index);
 			army.addCorps(corps);
 		}
 
 		return army;
 	}
 
 	private AdgCorps createCorps(int index) {
 		AdgCorps corps = new AdgCorps(uiConstants.corpsNames()[index]);
 
 		AdgGeneral general = new AdgGeneral();
 		general.setQuality(uiConstants.generalQualities()[0]);
 		general.setType(uiConstants.generalTypes()[0]);
 		corps.setGeneral(general);
 
 		List<AdgTroop> troops = new ArrayList<AdgTroop>();
 		int troopQty = uiConstants.defaultTroopQty();
 		for (int troopIndex = 0; troopIndex < troopQty; troopIndex++) {
 			troops.add(createNewTroop());
 		}
 		corps.setTroops(troops);
 		return corps;
 	}
 
 	private AdgTroop createNewTroop() {
 		AdgTroop troop = new AdgTroop();
 		troop.setQuality(uiConstants.troopQualities()[1]);
 		return troop;
 	}
 
 	public void addCorps(int index) {
 		army.addCorps(createCorps(index));
 	}
 
 	public void removeLastCorps() {
 		army.removeLastCorps();
 	}
 
 	public List<AdgCorps> getCorpsList() {
 		return army.getCorpsList();
 	}
 
 	public int computeCorpsRowIndex(int corpsIndex) {
 		int troopSize = computeTroopsSize(corpsIndex - 1, -1);
 		return headerArmySize + corpsIndex * fixedCorpsSize + troopSize;
 	}
 
 	public List<AdgTroop> getTroops(int corpsIndex) {
 		return army.getCorps(corpsIndex).getTroops();
 	}
 
 	public int computeTroopRowIndex(int corpsIndex, int troopIndex) {
 		return computeCorpsRowIndex(corpsIndex) + troopIndex + 3;
 	}
 
 	private int computeTroopsSize(int corpsIndex, int troopIndex) {
 		int troopSize = 0;
 		for(int i = 0; i <= corpsIndex; i++) {
 			troopSize += army.getCorps(i).getTroops().size();
 		}
 		return troopSize + troopIndex + 1;
 	}
 
 	public AdgCorps getCorps(int corpsIndex) {
 		return army.getCorps(corpsIndex);
 	}
 
 	public AdgTroop addNewTroop(int corpsIndex) {
 		AdgTroop troop = createNewTroop();
 		getCorps(corpsIndex).getTroops().add(troop);
 		return troop;
 	}
 
 	public int computeCorpsRowCount(int corpsIndex) {
 		int troopSize = getCorps(corpsIndex).getTroops().size();
 		return troopSize + fixedCorpsSize;
 	}
 }
