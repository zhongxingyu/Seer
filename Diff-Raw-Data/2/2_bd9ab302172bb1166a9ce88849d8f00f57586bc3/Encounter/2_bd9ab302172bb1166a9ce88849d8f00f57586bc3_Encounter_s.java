 package cm.model;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.SortedMap;
 import java.util.SortedSet;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.XMLStreamWriter;
 
 import org.xml.sax.InputSource;
 
 import cm.model.EffectBase.Duration;
 import cm.util.DiceBag;
 import cm.view.RechargeWin;
 import cm.view.SavingThrows;
 
 import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;
 import com.sun.xml.internal.ws.api.streaming.XMLStreamWriterFactory;
 
 /**
  * Defines a D&D 4e encounter.
  * @author matthew.rinehart
  *
  */
 public class Encounter {
 	private StatLibrary _statLib = new StatLibrary();
 	private SortedMap<String, Combatant> _roster = new TreeMap<String, Combatant>();
 	private Boolean _useRollMod = false;
 	private Boolean _ongoingPopup = true;
 	private String _globalNotesCoded = "";
 	private String _selectedFighterHandle = "";
 	private Hashtable<Integer, Effect> _activeEffects = new Hashtable<Integer, Effect>();
 	private Integer _nextEffectID = 1;
 	private Boolean _rollEffectSaves = true;
 	private Boolean _rollPowerRecharge = true;
 	private JFrame _parent = null;
 
 	/**
 	 * Creates a new encounter using the given {@link StatLibrary} and {@link DiceBag}.
 	 * @param statLibrary the {@link StatLibrary}
 	 * @param diceBag the {@link DiceBag} to use for dice rolls
 	 * @param useRoleMod if true, rolls will be modified according to fighter role
 	 */
 	public Encounter(StatLibrary statLibrary, DiceBag diceBag, Boolean useRoleMod, JFrame parent) {
 		clearAll();
 		setStatLib(statLibrary);
 		setUseRollMod(useRoleMod);
 		setOngoingPopup(Settings.doPopupForOngoingDamage());
 		setRollPowerRecharge(Settings.doPowerRecharge());
 		setRollEffectSaves(Settings.doSavingThrows());
 		setParent(parent);
 	}
 
 	/**
 	 * Sets the parent frame for the encounter.
 	 * @param parent the parent frame
 	 */
 	private void setParent(JFrame parent) {
 		_parent = parent;
 	}
 
 	/**
 	 * Returns the table of active status effects for this encounter.
 	 * @return the active effects table
 	 */
 	private Hashtable<Integer, Effect> getActiveEffects() {
 		return _activeEffects;
 	}
 
 	/**
 	 * Returns the {@link Combatant} whose turn it is
 	 * @return the {@link Combatant}
 	 */
 	public Combatant getCurrentFighter() {
 		return getRoster().get(getCurrentFighterHandle());
 	}
 
 	/**
 	 * Returns the combat handle of the {@link Combatant} whose turn it is
 	 * @return the combat handle, or "" if no one has rolled initiative
 	 */
 	public String getCurrentFighterHandle() {
 		if (getRolledList().size() > 0) {
 			return getRolledList().get(getRolledList().firstKey());
 		} else {
 			return "";
 		}
 	}
 
 	/**
 	 * Returns the initiative sequence of the current {@link Combatant}.
 	 * @return the initiative sequence
 	 */
 	private Integer getCurrentInitSequence() {
 		return getCurrentFighter().getInitSequence();
 	}
 
 	/**
 	 * Returns the highest round number of all {@link Combatant}s who have rolled initiative.
 	 * @return the round number
 	 */
 	public Integer getCurrentRound() {
 		Integer round = 999;
 		for (String handle : getRolledList().values()) {
 			Combatant fighter = getRoster().get(handle);
 			if (fighter.getRound() < round) {
 				round = fighter.getRound();
 			}
 		}
 		if (round.equals(999)) {
 			return 0;
 		} else {
 			return round;
 		}
 	}
 
 	/**
 	 * Returns a list of active {@link Effect}s from the given source.
 	 * @param sourceHandle the source handle
 	 * @return the list of {@link Effect}s
 	 */
 	private List<Effect> getEffectsBySource(String sourceHandle) {
 		List<Effect> list = new ArrayList<Effect>();
 		
 		for (Effect eff : getActiveEffects().values()) {
 			if (eff.getSourceHandle().contentEquals(sourceHandle) && eff.isActive(getCurrentInitSequence())) {
 				list.add(eff);
 			}
 		}
 		
 		return list;
 	}
 
 	/**
 	 * Returns a list of active {@link Effect}s on the given target.
 	 * @param targetHandle the target handle
 	 * @return the list of {@link Effect}s
 	 */	
 	public ArrayList<Effect> getEffectsByTarget(String targetHandle) {
 		ArrayList<Effect> list = new ArrayList<Effect>();
 		
 		for (Effect eff : getActiveEffects().values()) {
 			if (eff.getTargetHandle().contentEquals(targetHandle) && eff.isActive(getCurrentInitSequence())) {
 				list.add(eff);
 			}
 		}
 		
 		return list;
 	}
 
 	/**
 	 * Returns a list of unique {@link Effect}s by the source.
 	 * @param sourceHandle the source handle
 	 * @return the list
 	 */
 	public ArrayList<Effect> getEffectsUniqueHistoryBySource(String sourceHandle) {
 		ArrayList<String> uniqueList = new ArrayList<String>();
 		ArrayList<Effect> returnList = new ArrayList<Effect>();
 		
 		for (Effect eff : _activeEffects.values()) {
 			if (eff.getSourceHandle().contentEquals(sourceHandle)) {
 				if (!uniqueList.contains(eff.getEffectBaseID())) {
 					uniqueList.add(eff.getEffectBaseID());
 					returnList.add(eff);
 				}
 			}
 		}
 		
 		return returnList;
 	}
 
 	/**
 	 * Returns the {@link Combatant} at the requested index.
 	 * @param index the index requested
 	 * @return the {@link Combatant}, or {@code null} if none exists at the requested index
 	 */
 	public Combatant getFighterByIndex(Integer index) {
 		SortedMap<String, Combatant> list = new TreeMap<String, Combatant>();
 		
 		for (Combatant fighter : getRoster().values()) {
 			list.put(getInitSortValue(fighter), fighter);
 		}
 		
 		if (index >= list.size()) {
 			return null;
 		}
 		return (Combatant) list.values().toArray()[index];
 	}
 
 	/**
 	 * Returns the global notes for the encounter.
 	 * @return the global notes
 	 */
 	public String getGlobalNotes() {
 		return _globalNotesCoded.replace("###", "\n");
 	}
 	
 	/**
 	 * Sets the global notes for the encounter.
 	 * @param value the global notes
 	 */
 	public void setGlobalNotes(String value) {
 		_globalNotesCoded = value.replace("\n", "###");
 	}
 	
 	/**
 	 * Returns the coded form of the global notes.
 	 * @return global notes (coded)
 	 */
 	private String getGlobalNotesCoded() {
 		return _globalNotesCoded;
 	}
 
 	/**
 	 * Sets the coded form of the global notes.
 	 * @param string global notes
 	 */
 	private void setGlobalNotesCoded(String string) {
 		_globalNotesCoded = string.replace("\n", "###");	
 	}
 
 	/**
 	 * Returns a generated list of combatants who have rolled initiative, but are inactive.
 	 * @return the combatants who have rolled initiative, but are inactive
 	 */
 	private SortedSet<Combatant> getInactiveRolledFighters() {
 		SortedSet<Combatant> list = new TreeSet<Combatant>();
 		for (Combatant fighter : getRoster().values()) {
 			if (!fighter.isActive() && fighter.getInitSequence() > 0) {
 				list.add(fighter);
 			}
 		}
 		return list;
 	}
 
 	/** Returns the initiative sort value for the provided {@link Combatant}.
 	 * @param fighter the {@link Combatant} whose value we want
 	 * @return the fighter's {@link Combatant#getInitSort()}
 	 */
 	private String getInitSortValue(Combatant fighter) {
 		return fighter.getInitSort();
 	}
 
 	/**
 	 * Sets the initiative status of the fighter with the supplied handle to the provided status.
 	 * @param combatHandle the combat handle
 	 * @param status the init status
 	 * @see #setInitStatus(Combatant, String)
 	 */
 	private void setInitStatus(String combatHandle, String status) {
 		Combatant fighter = getRoster().get(combatHandle);
 		setInitStatus(fighter, status);
 	}
 
 	/**
 		 * Sets the initiative status of the supposed fighter to the provided status.
 		 * @param fighter the {@link Combatant}
 		 * @param status the init status
 		 */
 		public void setInitStatus(Combatant fighter, String status) {
 			if (fighter != null) {
 				if (status.contentEquals("Ready")) {
 	//				if (!fighter.getCombatHandle().contentEquals(getCurrentFighterHandle())) {
 	//					effectRemoveStart(fighter);
 	//				}
 	//				effectRemoveEnd(fighter);
 	//				fighter.initClear();
 	//				fighter.setInitStatus(status);
 					if (!fighter.getInitStatus().contentEquals("Reserve")) {
 						fighter.setReady(true);
 					}
 				} else if (status.contentEquals("Delay")) {
 					if (!fighter.getCombatHandle().contentEquals(getCurrentFighterHandle())) {
 						effectRemoveStart(fighter);
 					}
 					effectRemoveEnd(fighter);
 					fighter.initClear();
 					fighter.setInitStatus(status);
 				} else if (status.contentEquals("Reserve")) {
 					fighter.resetInit();
 					if (!fighter.isPC()) {
 						fighter.resetHealth();
 					}
 					effectRemoveAllByTarget(fighter.getCombatHandle());
 				}
 			}
 		}
 
 	/**
 	 * Returns the next effect ID.
 	 * @return the effect ID
 	 */
 	private Integer getNextEffectID() {
 		return _nextEffectID;
 	}
 
 	/**
 	 * Sets the next effect ID to the provided value.
 	 * @param id the effect ID
 	 */
 	private void setNextEffectID(Integer id) {
 		_nextEffectID = id;
 	}
 
 	/**
 	 * Indicates if the encounter is ongoing.
 	 * @return {@link #getRoster()}.size() != {@link #getReserveList()}.size()
 	 */
 	public Boolean isOngoingFight() {
 		return (getRoster().size() != getReserveList().size());
 	}
 
 	/**
 	 * Indicates if a popup should be displayed reminding the user of ongoing damage in effect on the current fighter.
 	 * @return true if the popup is desired in the application configuration
 	 */
 	private boolean doOngoingPopup() {
 		return _ongoingPopup;
 	}
 
 	/**
 	 * Sets the indicator of ongoing effect popups.
 	 * @param ongoingPopup true if the application should remind the user of ongoing effects
 	 * at the start of the fighter's turn.
 	 */
 	public void setOngoingPopup(Boolean ongoingPopup) {
 		_ongoingPopup = ongoingPopup;
 	}
 
 	/**
 	 * Returns the combat handle of the {@link Combatant} whose turn has just ended
 	 * @return the combat handle
 	 */
 	public String getPriorFighterHandle() {
 		if (getRolledList().size() > 0) {
 			return getRolledList().get(getRolledList().lastKey());
 		} else {
 			return "";
 		}
 	}
 
 	/**
 	 * Returns the {@link Combatant} whose turn has just ended.
 	 * @return the {@link Combatant}
 	 */
 	private Combatant getPriorFighter() {
 		return getRoster().get(getPriorFighterHandle());
 	}
 
 	/**
 	 * Returns a list of all fighters in this encounter.
 	 * @return the list
 	 */
 	public SortedSet<Combatant> getFullList() {
 		SortedSet<Combatant> list = new TreeSet<Combatant>();
 		for (Combatant fighter : getRoster().values()) {
 			list.add(fighter);
 		}
 		return list;
 	}
 
 	/**
 	 * Returns a generated list of combatants who are in reserve.
 	 * @return the combatants who are in reserve
 	 */
 	public SortedSet<String> getReserveList() {
 		SortedSet<String> list = new TreeSet<String>();
 		for (Combatant fighter : getRoster().values()) {
 			if (fighter.getInitStatus().contentEquals("Reserve")) {
 				list.add(fighter.getCombatHandle());
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * Returns a generated list of combatants who have rolled initiative.
 	 * @return the combatants who have rolled initiative
 	 */
 	public SortedMap<Integer, String> getRolledList() {
 		SortedMap<Integer, String> list = new TreeMap<Integer, String>();
 		for (Combatant fighter : getRoster().values()) {
 			if (fighter.getInitStatus().contentEquals("Rolled")) {
 				if (list.containsKey(fighter.getInitSequence())) {
 					fighter.setInitStatus("Rolling");
 				} else {
 					list.put(fighter.getInitSequence(), fighter.getCombatHandle());
 				}
 			}
 		}
 		
 		return list;
 	}
 	
 	/**
 	 * Indicates if the application should roll saving throws for fighters.
 	 * @return true if the application has been configured to roll saving throws
 	 */
 	private boolean doRollEffectSaves() {
 		return _rollEffectSaves;
 	}
 
 	/**
 	 * Sets the indicator of rolling saving throws.
 	 * @param rollEffectSaves true if the application should automatically roll saving throws
 	 */
 	public void setRollEffectSaves(Boolean rollEffectSaves) {
 		_rollEffectSaves = rollEffectSaves;
 	}
 
 	/**
 	 * Indicates if the application should roll to recharge fighter powers.
 	 * @return true if the application has been configured to roll recharges for fighter powers
 	 */
 	private boolean doRollPowerRecharge() {
 		return _rollPowerRecharge;
 	}
 
 	/**
 	 * Sets the indicator of rolling power recharges.
 	 * @param rollPowerRecharge true if the application should roll power recharges automatically
 	 */
 	public void setRollPowerRecharge(Boolean rollPowerRecharge) {
 		_rollPowerRecharge = rollPowerRecharge;
 	}
 
 	/**
 	 * Returns the encounter roster.
 	 * @return the encounter roster
 	 */
 	private SortedMap<String, Combatant> getRoster() {
 		return _roster;
 	}
 	
 	/**
 	 * Returns an indicator of if a fighter is selected in the roster.
 	 * @return !{@link #getSelectedFighterHandle()}.contentEquals("")
 	 */
 	public Boolean hasSelectedFighter() {
 		return (!getSelectedFighterHandle().contentEquals(""));
 	}
 
 	/**
 	 * Returns the selected {@link Combatant}, or {@code null} if there is none.
 	 * @return the {@link Combatant}
 	 */
 	public Combatant getSelectedFighter() {
 		if (!getSelectedFighterHandle().contentEquals("") && getRoster().containsKey(getSelectedFighterHandle())) {
 			return getRoster().get(getSelectedFighterHandle());
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Sets the selected fighter to the one provided.
 	 * @param value the {@link Combatant}
 	 * @see #setSelectedFighterHandle(String)
 	 */
 	public void setSelectedFighter(Combatant value) {
 		setSelectedFighterHandle(value.getCombatHandle());
 	}
 
 	/**
 	 * Returns the combat handle of the selected fighter.
 	 * @return the combat handle
 	 */
 	public String getSelectedFighterHandle() {
 		return _selectedFighterHandle;
 	}
 
 	/**
 	 * Sets the selected fighter handle to the provided value, or {@code null} if no such fighter is in the roster.
 	 * @param value the combat handle
 	 */
 	public void setSelectedFighterHandle(String value) {
 		if (getRoster().containsKey(value)) {
 			_selectedFighterHandle = value;
 		} else {
 			_selectedFighterHandle = "";
 		}
 	}
 
 	/**
 	 * Returns the stat library for the encounter.
 	 * @return the {@link StatLibrary}
 	 */
 	private StatLibrary getStatLib() {
 		return _statLib;
 	}
 
 	/**
 	 * Sets the {@link StatLibrary} to be used in the encounter.
 	 * @param statLib the {@link StatLibrary}
 	 */
 	private void setStatLib(StatLibrary statLib) {
 		_statLib = statLib;
 	}
 
 	/**
 	 * Indicates if the role modifier should be used.
 	 * @return true if the role modifier should be used
 	 */
 	private Boolean getUseRollMod() {
 		return _useRollMod;
 	}
 
 	/**
 	 * Sets the indicator of using the role modifier to effect dice rolls.
 	 * @param useRollMod true if the role modifier should modify dice rolls
 	 */
 	private void setUseRollMod(Boolean useRollMod) {
 		_useRollMod = useRollMod;
 	}
 
 	/**
 	 * Adds a new {@link Combatant} with the supplied stats to the encounter.
 	 * Calls {@link #add(Combatant, Boolean, Boolean)} with a final parameter of 'true'.
 	 * @param stats the {@link Stats}
 	 * @param libUpdate if true, calls {@link Combatant#updateLibrary(StatLibrary, Boolean)}
 	 */
 	public void add(Stats stats, Boolean libUpdate) {
 		Combatant newFighter = new Combatant(stats);
 		add(newFighter, libUpdate, true);
 	}
 	
 	/**
 	 * Adds a new {@link Combatant} with the supplied stats and role modifier to the encounter.
 	 * Calls {@link #add(Combatant, Boolean, Boolean)} with a final parameter of 'true'.
 	 * @param stats the {@link Stats}
 	 * @param rolemod the role modifier for the {@link Combatant}
 	 * @param libUpdate if true, calls {@link Combatant#updateLibrary(StatLibrary, Boolean)}
 	 */
 	private void add(Stats stats, String rolemod, Boolean libUpdate) {
 		Combatant newFighter = new Combatant(stats, rolemod);
 		add(newFighter, libUpdate, true);
 	}
 	
 	/**
 	 * Adds a new {@link Combatant} to the encounter.
 	 * @param combatant the {@link Combatant}
 	 * @param libUpdate if true, calls {@link Combatant#updateLibrary(StatLibrary, Boolean)}
 	 * @param configEntry if true, updates a non-PC's fighter number from 0 to 1
 	 */
 	public void add(Combatant combatant, Boolean libUpdate, Boolean configEntry) {
 		if (configEntry) {
 			if (!combatant.isPC() && combatant.getFighterNumber() == 0) {
 				combatant.setFighterNumber(1);
 			}
 		}
 		
 		while(getRoster().containsKey(combatant.getCombatHandle())) {
 			combatant.setFighterNumber(combatant.getFighterNumber() + 1);
 		}
 		
 		getRoster().put(combatant.getCombatHandle(), combatant);
 		if (libUpdate) {
 			combatant.updateLibrary(getStatLib(), configEntry);
 		}
 		if (!getUseRollMod()) {
 			combatant.setRoleMod("");
 		}
 	}
 	
 	/**
 	 * Rolls initiative for all in the roster and selects the current fighter.
 	 * @param groupSimilar if true, similar NPCs will share the same initiative count
 	 */
 	public void startFight(Boolean groupSimilar) {
 		if (groupSimilar) {
 			rollAllInitGrouped();
 		} else {
 			rollAllInitUngrouped();
 		}
 		setSelectedFighter(getCurrentFighter());
 	}
 
 	/**
 	 * Selects the current fighter and starts its turn.
 	 */
 	private void beginTurn() {
 		selectCurrentFighter();
 		fighterStartTurn(getSelectedFighterHandle());
 	}
 
 	/**
 	 * Finishes the current fighter's turn and starts the next fighter's turn.
 	 */
 	public void finishCurrentTurn() {
 		if (getRolledList().size() > 0) {
 			Combatant fighter = getCurrentFighter();
 			fighterEndTurn(fighter.getCombatHandle());
 			skipInactive();
 			selectCurrentFighter();
 			fighterStartTurn(getSelectedFighterHandle());
 		}
 	}
 
 	/**
 	 * Updates one fighter's initiative to match another's.
 	 * @param movingHandle the combat handle of the {@link Combatant} who is being updated
 	 * @param targetHandle the combat handle of the {@link Combatant} who is being matched
 	 * @param moveAfter if true, the moved fighter will go after the target instead of before
 	 * @see #fighterInitUpdate(String, Integer, Integer, Integer, Boolean)
 	 */
 	private void fighterInitMatch(String movingHandle, String targetHandle, Boolean moveAfter) {
 		Combatant target = getRoster().get(targetHandle);
 		
 		fighterInitUpdate(movingHandle, target.getRound(), target.getInitRoll(), target.getRandom3(), moveAfter);
 		if (movingHandle.contentEquals(getCurrentFighterHandle())) {
 			fighterStartTurn(getCurrentFighterHandle());
 		}
 	}
 
 	/**
 	 * Updates the initiative roll of the fighter with the given combat handle to the result provided.
 	 * @param combatHandle the combat handle
 	 * @param init the new init roll
 	 * @see #fighterInitUpdate(String, Integer, Integer, Integer, Boolean)
 	 */
 	public void fighterInitRollUpdate(String combatHandle, Integer init) {
 		if (getRoster().containsKey(combatHandle)) {
 			Combatant fighter = getRoster().get(combatHandle);
 			
			fighterInitUpdate(combatHandle, fighter.getRound(), fighter.getInitRoll(), fighter.getRandom3(), true);
 			
 			if (fighter.getCombatHandle().contentEquals(getCurrentFighterHandle())) {
 				fighterStartTurn(getCurrentFighterHandle());
 			}
 		}
 	}
 
 	/**
 	 * Updates the initiative roll, current round, and random3 value of the fighter with the provided combat handle.
 	 * @param combatHandle the combat handle
 	 * @param round the new round
 	 * @param init the new init roll
 	 * @param random3 the new random3 value
 	 * @param goAfter if true, the fighter will be placed after others with the same initiative result instead of before
 	 * @see #fighterInitUpdate(Combatant, Boolean)
 	 */
 	private void fighterInitUpdate(String combatHandle, Integer round, Integer init,
 			Integer random3, Boolean goAfter) {
 		if (getRoster().containsKey(combatHandle)) {
 			Combatant fighter = getRoster().get(combatHandle);
 			
 			fighter.setRound(round);
 			fighter.setInitRoll(init);
 			fighter.setRandom3(random3);
 			
 			fighterInitUpdate(fighter, goAfter);
 		}
 	}
 
 	/**
 	 * Updates the initiative roll of the fighter provided.
 	 * @param fighter the {@link Combatant}
 	 * @param goAfter if true, the fighter will be placed after others with the same initiative result instead of before
 	 */
 	private void fighterInitUpdate(Combatant fighter, Boolean goAfter) {
 		if (fighter != null) {
 			fighter.setInitStatus("Rolling");
 			
 			while (getRolledList().containsKey(fighter.getInitSequence())) {
 				if(goAfter) {
 					fighter.setRandom3(fighter.getRandom3() + 1);
 				} else {
 					fighter.setRandom3(fighter.getRandom3() - 1);
 				}
 			}
 		}
 		
 		fighter.setInitStatus("Rolled");
 	}
 
 	/**
 	 * Starts the turn of the fighter with the specified combat handle.
 	 * @param combatHandle the combat handle
 	 * @see #effectRemoveStart(Combatant)
 	 * @see #doRollPowerRecharge()
 	 * @see #doOngoingPopup()
 	 */
 	private void fighterStartTurn(String combatHandle) {
 		Combatant fighter = getRoster().get(combatHandle);
 		effectRemoveStart(fighter);
 		fighter.setReady(false);
 		if (doRollPowerRecharge()) {
 			powerCheckRecharge(fighter);
 		}
 		
 		if (doOngoingPopup()) {
 			String text = new String("");
 			
 			for (Effect eff : getActiveEffects().values()) {
 				if (eff.isActive(getCurrentInitSequence())
 						&& eff.getTargetHandle().contentEquals(fighter.getCombatHandle()) 
 						&& eff.getName().toLowerCase().contains("ongoing")) {
 					text += "\n" + eff.getName();
 				}
 			}
 			
 			if (text.length() > 0) {
 				JOptionPane.showMessageDialog(null, "The following ongoing effects are in place on this monster:\n" + text, "Reminder", JOptionPane.INFORMATION_MESSAGE);
 			}
 		}
 	}
 
 	/**
 	 * Ends the turn of the fighter with the provided combat handle.
 	 * @param combatHandle the combat handle
 	 * @see #effectRemoveEnd(Combatant)
 	 * @see #effectMakeSaves(Combatant)
 	 */
 	private void fighterEndTurn(String combatHandle) {
 		Combatant fighter = getRoster().get(combatHandle);
 		effectRemoveEnd(fighter);
 		if (doRollEffectSaves()) {
 			effectMakeSaves(fighter);
 		}
 		fighterInitUpdate(fighter.getCombatHandle(), fighter.getRound() + 1, fighter.getInitRoll(), fighter.getRandom3(), true);
 	}
 
 	/**
 	 * Reverses initiative tracking to the start of the turn of the fighter with the supplied combat handle.
 	 * @param combatHandle the combat handle
 	 * @see #fighterUndoTurn(Combatant)
 	 */
 	public void fighterUndoTurn(String combatHandle) {
 		Combatant fighter = getRoster().get(combatHandle);
 		fighterUndoTurn(fighter);
 	}
 
 	/**
 	 * Reverses initiative tracking to the start of the turn of the provided fighter.
 	 * @param fighter the {@link Combatant}
 	 */
 	private void fighterUndoTurn(Combatant fighter) {
 		if (fighter != null) {
 			Integer newRound = fighter.getRound();
 			newRound -= 1;
 			
 			if (newRound < 0) {
 				newRound = 0;
 			}
 			
 			fighterInitUpdate(fighter.getCombatHandle(), newRound, fighter.getInitRoll(), fighter.getRandom3(), true);
 			setSelectedFighterHandle(getCurrentFighterHandle());
 			fighterStartTurn(getCurrentFighterHandle());
 		}
 	}
 
 	/**
 	 * Resets the encounter to default (empty) values.
 	 */
 	public void clearAll() {
 		getRoster().clear();
 		
 		setSelectedFighterHandle("");
 		setGlobalNotes("");
 		getActiveEffects().clear();
 	}
 
 	/**
 	 * Remvoes all enemies from the roster and resets the encounter.
 	 */
 	public void clearNPCs() {
 		List<String> toRemove = new ArrayList<String>();
 		
 		for (Combatant fighter : getRoster().values()) {
 			if (!fighter.isPC()) {
 				toRemove.add(fighter.getCombatHandle());
 			}
 		}
 		
 		for (String handle : toRemove) {
 			getRoster().remove(handle);
 		}
 		
 		resetEncounter(false);
 	}
 
 	/**
 	 * Removes all PCs from the roster and resets the encounter.
 	 */
 	public void clearPCs() {
 		List<String> toRemove = new ArrayList<String>();
 		
 		for (Combatant fighter : getRoster().values()) {
 			if (fighter.isPC()) {
 				toRemove.add(fighter.getCombatHandle());
 			}
 		}
 		
 		for (String handle : toRemove) {
 			getRoster().remove(handle);
 		}
 		
 		resetEncounter(true);
 	}
 
 	/**
 	 * Calls {@link #setSelectedFighterHandle(String)} with a parameter of "".
 	 */
 	public void clearSelectedFighter() {
 		setSelectedFighterHandle("");
 	}
 
 	/**
 	 * Adds the provided effect to the encounter.
 	 * @param eff the effect
 	 * @see #effectAdd(String, String, String, Duration, Boolean)
 	 */
 	public void effectAdd(Effect eff) {
 		effectAdd(eff.getName(), eff.getSourceHandle(), eff.getTargetHandle(), eff.getDurationCode(), eff.isBeneficial());
 	}
 
 	/**
 	 * Adds a new {@link Effect} to the encounter with the provided properties.
 	 * @param name the name of the effect
 	 * @param source the source handle of the effect
 	 * @param target the target handle of the effect
 	 * @param dur the {@link Duration} of the effect
 	 * @param beneficial true if the effect is beneficial to the target
 	 */
 	public void effectAdd(String name, String source, String target, Duration dur, Boolean beneficial) {
 		Effect newEffect = new Effect(name, _nextEffectID, source, target, effectTillRound(source, target, dur), dur, beneficial);
 		
 		if (newEffect.isValid()) {
 			if (!getActiveEffects().containsKey(_nextEffectID)) {
 				// remove prior marks if effect is a mark
 				if (newEffect.isMark()) {
 					effectRemoveMarksByTarget(target);
 				}
 				newEffect.setActive();
 				getActiveEffects().put(newEffect.getEffectID(), newEffect);
 				setNextEffectID(getNextEffectID() + 1);
 			}
 		}
 	}
 
 	/**
 	 * TODO: review original code; what this is doing doesn't make much sense
 	 * @param eff the effect
 	 */
 	public void effectChange(Effect eff) {
 		if (eff.isValid()) {
 			if (getActiveEffects().containsKey(eff.getEffectID())) {
 				eff.setEndInitSeq(getActiveEffects().get(eff.getEffectID()).getEndInitSeq());
 			}
 		}
 	}
 
 	/*public void effectRemoveEndDelay(Combatant fighter) {
 		ArrayList<Effect> list = new ArrayList<Effect>();
 	
 		for (Effect eff : getEffectsBySource(fighter.getCombatHandle())) {
 			if (eff.getDurationCode() == Duration.SourceEnd || eff.getDurationCode() == Duration.TurnEnd) {
 				Boolean goodForFighter = eff.isBeneficial();
 				if (fighter.isPC() != _roster.get(eff.getTargetHandle()).isPC()) {
 					goodForFighter = !eff.isBeneficial();
 				}
 				if (goodForFighter) {
 					list.add(eff);
 				}
 			} else if (eff.getDurationCode() == Duration.Sustained) {
 				list.add(eff);
 			}
 		}
 		
 		for (Effect eff : getEffectsByTarget(fighter.getCombatHandle())) {
 			if (eff.getDurationCode() == Duration.TargetEnd || eff.getDurationCode() == Duration.TurnEnd) {
 				if (eff.getRoundTill() <= fighter.getRound() && eff.isBeneficial()) {
 					list.add(eff);
 				}
 			}
 		}
 		
 		for (Effect eff : list) {
 			eff.setInactive(getCurrentInitSequence() + 1);
 		}
 	}*/
 	
 	/**
 	 * Handle effects that a save can end for the given fighter.
 	 * @param fighter the {@link Combatant}
 	 */
 	private void effectMakeSaves(Combatant fighter) {
 		if (!fighter.isAlive()) {
 			return;
 		}
 		
 		List<Effect> list = new ArrayList<Effect>();
 		
 		for (Effect eff : getEffectsByTarget(fighter.getCombatHandle())) {
 			if (eff.getDurationCode() == Duration.SaveEnds) {
 				list.add(eff);
 			}
 		}
 		
 		if (list.size() > 0) {
 			SavingThrows saveWin = new SavingThrows(list, fighter.getStats().getSaveBonus(), getParent());
 			saveWin.setVisible(true);
 			
 			if (saveWin.getSuccessfulSaves().size() > 0) {
 				for (Integer id : saveWin.getSuccessfulSaves()) {
 					Effect eff = getActiveEffect(id);
 					eff.setInactive(getCurrentInitSequence() + 1);
 				}
 			}
 			
 			saveWin.dispose();
 		}
 	}
 
 	/**
 	 * Sets the {@link Effect} with the given effect ID to be inactive. 
 	 * @param id the effect ID
 	 */
 	public void effectRemove(Integer id) {
 		if (getActiveEffects().containsKey(id)) {
 			getActiveEffects().get(id).setInactive(getCurrentInitSequence());
 		}
 	}
 
 	/**
 	 * Removes all {@link Effect}s on the given target from them.
 	 * @param targetHandle the target handle
 	 */
 	private void effectRemoveAllByTarget(String targetHandle) {
 		List<Integer> list = new ArrayList<Integer>();
 		
 		for (Effect eff : getEffectsByTarget(targetHandle)) {
 			list.add(eff.getEffectID());
 		}
 		
 		for (Integer id : list) {
 			effectRemove(id);
 		}
 	}
 
 	/**
 	 * Removes all marks on the given from them.
 	 * @param targetHandle the target handle
 	 */
 	private void effectRemoveMarksByTarget(String targetHandle) {
 		ArrayList<Effect> list = new ArrayList<Effect>();
 		
 		for (Effect eff : getEffectsByTarget(targetHandle)) {
 			if (eff.isMark()) {
 				list.add(eff);
 			}
 		}
 		
 		for (Effect eff : list) {
 			effectRemove(eff.getEffectID());
 		}
 	}
 
 	/**
 	 * Removes effects relating to the provided fighter at the start of its turn.
 	 * @param fighter the {@link Combatant}
 	 */
 	private void effectRemoveStart(Combatant fighter) {
 		List<Effect> list = new ArrayList<Effect>();
 		
 		for (Effect eff : getEffectsBySource(fighter.getCombatHandle())) {
 			if (eff.getDurationCode() == Duration.SourceStart) {
 				if (eff.getRoundTill() <= fighter.getRound()) {
 					list.add(eff);
 				}
 			}
 		}
 	
 		for (Effect eff : getEffectsByTarget(fighter.getCombatHandle())) {
 			if (eff.getDurationCode() == Duration.TargetStart) {
 				if (eff.getRoundTill() <= fighter.getRound()) {
 					list.add(eff);
 				}
 			}
 		}
 		
 		for (Effect eff : list) {
 			eff.setInactive(getCurrentInitSequence());
 		}
 	}
 
 	/**
 	 * Removes effects relating to the provided fighter at the end of its turn.
 	 * @param fighter the {@link Combatant}
 	 */
 	private void effectRemoveEnd(Combatant fighter) {
 		List<Effect> list = new ArrayList<Effect>();
 		
 		for (Effect eff : getEffectsBySource(fighter.getCombatHandle())) {
 			if (eff.getDurationCode() == Duration.SourceEnd || eff.getDurationCode() == Duration.TurnEnd) {
 				if (eff.getRoundTill() <= fighter.getRound()) {
 					list.add(eff);
 				}
 			}
 		}
 	
 		for (Effect eff : getEffectsByTarget(fighter.getCombatHandle())) {
 			if (eff.getDurationCode() == Duration.TargetEnd || eff.getDurationCode() == Duration.TurnEnd) {
 				if (eff.getRoundTill() <= fighter.getRound()) {
 					list.add(eff);
 				}
 			}
 		}
 		
 		for (Effect eff : list) {
 			eff.setInactive(getCurrentInitSequence() + 1);
 		}
 	}
 
 	/**
 	 * Returns the round in which an effect with the given duration would expire.
 	 * @param source the source handle
 	 * @param target the target handle
 	 * @param dur the {@link Duration}
 	 * @return the round in which the effect would expire
 	 */
 	private Integer effectTillRound(String source, String target, Duration dur) {
 		Integer tillRound = 99;
 		
 		switch (dur) {
 		case TargetStart:
 		case TargetEnd:
 			tillRound = getRoster().get(target).getRound();
 			if (target.contentEquals(getCurrentFighterHandle())) {
 				tillRound++;
 			}
 			break;
 		case SourceStart:
 		case SourceEnd:
 		case Sustained:
 			tillRound = getRoster().get(source).getRound();
 			if (source.contentEquals(getCurrentFighterHandle())) {
 				tillRound++;
 			}
 			break;
 		case TurnEnd:
 			tillRound = getCurrentRound();
 			break;
 		}
 		return tillRound;
 	}
 
 	/**
 	 * Relocates the provided fighter to the top of the initiative order.
 	 * @param fighter the {@link Combatant}
 	 * @see #fighterInitMatch(String, String, Boolean)
 	 */
 	public void moveToTop(Combatant fighter) {
 		if (fighter != null) {
 			fighterInitMatch(fighter.getCombatHandle(), getCurrentFighterHandle(), false);
 		}
 	}
 
 	/*public void effectRemoveEndDelay(Combatant fighter) {
 		ArrayList<Effect> list = new ArrayList<Effect>();
 	
 		for (Effect eff : getEffectsBySource(fighter.getCombatHandle())) {
 			if (eff.getDurationCode() == Duration.SourceEnd || eff.getDurationCode() == Duration.TurnEnd) {
 				Boolean goodForFighter = eff.isBeneficial();
 				if (fighter.isPC() != _roster.get(eff.getTargetHandle()).isPC()) {
 					goodForFighter = !eff.isBeneficial();
 				}
 				if (goodForFighter) {
 					list.add(eff);
 				}
 			} else if (eff.getDurationCode() == Duration.Sustained) {
 				list.add(eff);
 			}
 		}
 		
 		for (Effect eff : getEffectsByTarget(fighter.getCombatHandle())) {
 			if (eff.getDurationCode() == Duration.TargetEnd || eff.getDurationCode() == Duration.TurnEnd) {
 				if (eff.getRoundTill() <= fighter.getRound() && eff.isBeneficial()) {
 					list.add(eff);
 				}
 			}
 		}
 		
 		for (Effect eff : list) {
 			eff.setInactive(getCurrentInitSequence() + 1);
 		}
 	}*/
 	
 	/**
 	 * Handle recharge powers for the given fighter.
 	 * @param fighter the {@link Combatant}
 	 */
 	private void powerCheckRecharge(Combatant fighter) {
 		if (!fighter.isAlive()) {
 			return;
 		}
 		
 		List<Power> list = new ArrayList<Power>();
 		
 		for (Power pow : fighter.getStats().getPowerList()) {
 			if (pow.getRechargeVal() > 0) {
 				if (fighter.isPowerUsed(pow.getName())) {
 					list.add(pow);
 				}
 			}
 		}
 		
 		if (list.size() > 0) {
 			RechargeWin win = new RechargeWin(fighter.getCombatHandle(), list, getParent());
 			win.setVisible(true);
 			
 			if (win.getRecharged().size() > 0) {
 				for (String powName : win.getRecharged()) {
 					fighter.setPowerUsed(powName, false);
 				}
 			}
 			
 			win.dispose();
 		}
 	}
 
 	/**
 	 * Returns the parent frame registered with this encounter.
 	 * @return the parent frame
 	 */
 	private JFrame getParent() {
 		return _parent;
 	}
 
 	/**
 	 * Removes a given fighter from the roster.
 	 * @param combatHandle the fighter's combat handle
 	 */
 	public void remove(String combatHandle) {
 		getRoster().remove(combatHandle);
 	}
 
 	/**
 	 * Resets initiative for all {@link Combatant}s in the encounter.
 	 * @param resetPCs if true, resets health and power usage for PCs in addition to enemies
 	 */
 	public void resetEncounter(Boolean resetPCs) {
 		for (Combatant fighter : getRoster().values()) {
 			fighter.resetInit();
 			if (!fighter.isPC() || resetPCs) {
 				fighter.resetHealth();
 				fighter.resetPowersUsage(true, true);
 			}
 		}
 		clearSelectedFighter();
 		getActiveEffects().clear();
 	}
 	
 	/**
 	 * Resets the health of all PCs in the encounter roster.
 	 */
 	private void resetPCHealth() {
 		for (Combatant fighter : getRoster().values()) {
 			if (fighter.isPC()) {
 				fighter.resetHealth();
 			}
 		}
 	}
 	
 	/**
 	 * Rolls initiative for each group of fighters, then starts the first fighter's turn.
 	 */
 	private void rollAllInitGrouped() {
 		Hashtable<String, String> initList = new Hashtable<String, String>();
 		Combatant firstRoller;
 	
 		for (Combatant fighter : getRoster().values()) {
 			if (initList.contains(fighter.getHandle())) {
 				if (!fighter.getInitStatus().contentEquals("Rolled")) {
 					firstRoller = getRoster().get(initList.get(fighter.getHandle()));
 					fighter.resetInit();
 					fighterInitUpdate(fighter.getCombatHandle(), firstRoller.getRound(),
 							firstRoller.getInitRoll(), firstRoller.getRandom3(), true);
 				}
 			} else {
 				if (!fighter.getInitStatus().contentEquals("Rolled")) {
 					fighter.resetInit();
 					fighter.rollInitiative();
 					fighter.setRound(getCurrentRound());
 					fighterInitUpdate(fighter, true);
 				}
 				initList.put(fighter.getHandle(), fighter.getCombatHandle());
 			}
 		}
 		fighterStartTurn(getCurrentFighterHandle());
 	}
 
 	/**
 	 * Individually rolls initiative for each {@link Combatant} in the encounter roster,
 	 * then starts the first player's turn.
 	 */
 	private void rollAllInitUngrouped() {
 		for (Combatant fighter : getRoster().values()) {
 			if (!fighter.getInitStatus().contentEquals("Rolled")) {
 				fighter.resetInit();
 				fighter.rollInitiative();
 				fighter.setRound(getCurrentRound());
 				fighterInitUpdate(fighter, true);
 				fighter.setInitStatus("Rolled");
 			}
 		}
 		fighterStartTurn(getCurrentFighterHandle());
 	}
 
 	/**
 	 * Rolls initiative for a single fighter, and starts their turn if it puts them at the top of the list.
 	 * @param fighter the {@link Combatant} for which we are rolling
 	 */
 	public void rollOneInit(Combatant fighter) {
 		if (fighter != null) {
 			if (!fighter.getInitStatus().contentEquals("Rolled")) {
 				fighter.resetInit();
 				fighter.rollInitiative();
 				fighter.setRound(getCurrentRound());
 				fighterInitUpdate(fighter, true);
 				fighter.setInitStatus("Rolled");
 			}
 		}
 		if (fighter.getCombatHandle().contentEquals(getCurrentFighterHandle())) {
 			fighterStartTurn(getCurrentFighterHandle());
 		}
 	}
 
 	/**
 	 * Selects the current fighter. Calls {@link #setSelectedFighter(Combatant)} with {@link #getCurrentFighter()}.
 	 */
 	private void selectCurrentFighter() {
 		setSelectedFighter(getCurrentFighter());
 	}
 
 	/**
 	 * Starts and immediately ends the turns of any inactive fighters between the previous active and the next active.
 	 */
 	private void skipInactive() {
 		Integer lowestActiveInt = getCurrentInitSequence();
 		
 		if (lowestActiveInt > 0) {
 			for (Combatant fighter : getInactiveRolledFighters()) {
 				if (fighter.getInitSequence() < lowestActiveInt) {
 					fighterStartTurn(fighter.getCombatHandle());
 					fighterEndTurn(fighter.getCombatHandle());
 				}
 			}
 		}
 	}
 
 	public Integer size() {
 		return getRoster().size();
 	}
 
 	/**
 	 * Removes all enemies from the roster and applies a short rest to the PCs.
 	 */
 	public void takeShortRest() {
 		clearNPCs();
 		resetEncounter(false);
 		for (Combatant fighter : getRoster().values()) {
 			if (fighter.isPC()) {
 				fighter.resetTempHP();
 				fighter.resetPowersUsage(false, false);
 			}
 		}
 	}
 	
 	/**
 	 * Removes all enemies from the roster and applies a short rest to the PCs.
 	 * Also grants an action point to each PC.
 	 */
 	public void takeShortRestWithMilestone() {
 		clearNPCs();
 		resetEncounter(false);
 		for (Combatant fighter : getRoster().values()) {
 			if (fighter.isPC()) {
 				fighter.resetTempHP();
 				fighter.resetPowersUsage(false, true);
 				fighter.getStats().setActionPoints(fighter.getStats().getActionPoints() + 1);
 			}
 		}
 	}
 	
 	/**
 	 * Removes all enemies from the roster and applies an extended rest to the PCs.
 	 */
 	public void takeExtendedRest() {
 		clearNPCs();
 		resetEncounter(true);
 	}
 	
 	/**
 	 * Calls {@link Combatant#updateLibrary(StatLibrary, Boolean)} for each fighter in the roster.
 	 * @see #getStatLib()
 	 * @param configEntries this is passed as the second parameter to {@link Combatant#updateLibrary(StatLibrary, Boolean)}
 	 */
 	public void updateAllStats(Boolean configEntries) {
 		for (Combatant fighter: getRoster().values()) {
 			fighter.updateLibrary(getStatLib(), configEntries);
 		}
 	}
 	
 	/*public void effectRemoveEndDelay(Combatant fighter) {
 		ArrayList<Effect> list = new ArrayList<Effect>();
 
 		for (Effect eff : getEffectsBySource(fighter.getCombatHandle())) {
 			if (eff.getDurationCode() == Duration.SourceEnd || eff.getDurationCode() == Duration.TurnEnd) {
 				Boolean goodForFighter = eff.isBeneficial();
 				if (fighter.isPC() != _roster.get(eff.getTargetHandle()).isPC()) {
 					goodForFighter = !eff.isBeneficial();
 				}
 				if (goodForFighter) {
 					list.add(eff);
 				}
 			} else if (eff.getDurationCode() == Duration.Sustained) {
 				list.add(eff);
 			}
 		}
 		
 		for (Effect eff : getEffectsByTarget(fighter.getCombatHandle())) {
 			if (eff.getDurationCode() == Duration.TargetEnd || eff.getDurationCode() == Duration.TurnEnd) {
 				if (eff.getRoundTill() <= fighter.getRound() && eff.isBeneficial()) {
 					list.add(eff);
 				}
 			}
 		}
 		
 		for (Effect eff : list) {
 			eff.setInactive(getCurrentInitSequence() + 1);
 		}
 	}*/
 	
 	/**
 	 * Writes the encounter data out to an XML stream.
 	 * @param writer the XML stream
 	 * @throws XMLStreamException from the writer
 	 */
 	private void exportXML(XMLStreamWriter writer) throws XMLStreamException {
 		writer.writeStartElement("encounter");
 		
 		writer.writeStartElement("ongoingfight");
 		writer.writeCharacters(isOngoingFight().toString());
 		writer.writeEndElement();
 		
 		writer.writeStartElement("globalnotes");
 		writer.writeCharacters(getGlobalNotesCoded());
 		writer.writeEndElement();
 		
 		for (Combatant fighter : getRoster().values()) {
 			fighter.exportXML(writer, isOngoingFight());
 		}
 		
 		if (isOngoingFight()) {
 			writer.writeStartElement("nexteffectID");
 			writer.writeCharacters(getNextEffectID().toString());
 			writer.writeEndElement();
 			
 			for (Effect eff : getActiveEffects().values()) {
 				eff.exportXML(writer);
 			}
 		}
 		
 		writer.writeEndElement();
 	}
 	
 	/**
 	 * Sets encounter properties from an XML stream.
 	 * @param reader the XML stream
 	 * @return true on success
 	 * @throws XMLStreamException from the reader
 	 */
 	private Boolean importXML(XMLStreamReader reader) throws XMLStreamException {
 		String elementName = "";
 		Combatant fighter;
 		Effect eff;
 		Boolean resetInit = true;
 		
 		if (reader.isStartElement() && reader.getName().toString().contentEquals("encounter")) {
 			while (reader.hasNext()) {
 				reader.next();
 				if (reader.isStartElement()) {
 					if (reader.getName().toString().contentEquals("combatant")) {
 						fighter = new Combatant();
 						fighter.importXML(reader);
 						add(fighter, true, resetInit);
 					} else if (reader.getName().toString().contentEquals("effect")) {
 						eff = new Effect();
 						eff.importXML(reader);
 						getActiveEffects().put(eff.getEffectID(), eff);
 					} else {
 						elementName = reader.getName().toString();
 					}
 				} else if (reader.isCharacters()) {
 					if (elementName.contentEquals("globalnotes")) {
 						if (!getGlobalNotesCoded().isEmpty()) {
 							setGlobalNotesCoded(getGlobalNotesCoded() + "###" + reader.getText());
 						} else {
 							setGlobalNotesCoded(reader.getText());
 						}
 					} else if (elementName.contentEquals("ongoingfight")) {
 						if (Boolean.valueOf(reader.getText())) {
 							if (getRoster().size() > 0) {
 								/*
                                 MsgBox("An ongoing encounter cannot be imported with preexisting combatants." & ControlChars.NewLine & _
                                     "Please clear the list before attempting this operation.", _
                                     MsgBoxStyle.OkOnly + MsgBoxStyle.Exclamation, "Import Error")
                                 Return False
 								 */
 								resetInit = false;
 							}
 						}
 					} else if (elementName.contentEquals("nexteffectID")) {
 						setNextEffectID(Integer.valueOf(reader.getText()));						
 					}
 				} else if (reader.isEndElement()) {
 					elementName = "";
 					if (reader.getName().toString().contentEquals("encounter")) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Loads encounter data from a file.
 	 * @param filename the filename to load from
 	 * @param clearBeforeLoading true if the encounter should be cleared before loading the file
 	 * @return true on success
 	 */
 	public Boolean loadFromFile(String filename, Boolean clearBeforeLoading) {
 		File encounter = new File(filename);
 		
 		if (encounter.exists()) {
 			try {
 				InputSource input = new InputSource(new FileInputStream(encounter));
 				XMLStreamReader reader = XMLStreamReaderFactory.create(input, false);
 				while (reader.hasNext() && !reader.isStartElement()) {
 					reader.next();
 				}
 				if (clearBeforeLoading) {
 					clearAll();
 				}
 				importXML(reader);
 			} catch (FileNotFoundException e) {
 				// this shouldn't happen, should it? We checked for file existence above with encounter.exists()
 				e.printStackTrace();
 				return false;
 			} catch (XMLStreamException e) {
 				e.printStackTrace();
 				return false;
 			}
 		} else {
 			return false;
 		}
 		return true;
 	}
 	
 	/**
 	 * Writes the encounter data out to a file.
 	 * @param filename the filename to save to
 	 * @return true on success
 	 * @throws IOException from the file channels
 	 */
 	public Boolean saveToFile(String filename) {
 		String tmpFilename = filename + ".tmp";
 		File encounter = new File(filename);
 		File tmpFile = new File(tmpFilename);
 		FileChannel src = null;
 		FileChannel dst = null;
 		
 		if (tmpFile.exists()) {
 			if (!tmpFile.delete()) {
 				return false;
 			}
 		}
 		try {
 			OutputStream output = new FileOutputStream(tmpFile);
 			XMLStreamWriter writer = XMLStreamWriterFactory.create(output);
 			exportXML(writer);
 			
 			src = new FileInputStream(tmpFile).getChannel();
 			dst = new FileOutputStream(encounter).getChannel();
 			dst.transferFrom(src, 0, src.size());
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return false;
 		} catch (XMLStreamException e) {
 			e.printStackTrace();
 			return false;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		} finally {
 			if (src != null) {
 				try {
 					src.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 					return false;
 				}
 			}
 			if (dst != null) {
 				try {
 					dst.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 					return false;
 				}
 			}
 		}
 		
 		tmpFile.delete();
 		return true;
 	}
 
 	/**
 	 * Returns the active effect with the provided effect ID.
 	 * @param effectID the effect ID
 	 * @return the active effect
 	 */
 	public Effect getActiveEffect(Integer effectID) {
 		return getActiveEffects().get(effectID);
 	}
 
 	/**
 	 * Returns a {@link Combatant} with the matching combat handle.
 	 * @param handle the combat handle
 	 * @return the {@link Combatant}, or null if none was found
 	 */
 	public Combatant getFighterByHandle(String handle) {
 		for (Combatant fighter : getRoster().values()) {
 			if (fighter.getCombatHandle().contentEquals(handle)) {
 				return fighter;
 			}
 		}
 		return null;
 	}
 }
 /*
 Public ReadOnly Property nEncounterLevel() As Integer
     Get
         Return DnD4e_EncounterLevel(nPartySize, nEncounterXP)
     End Get
 End Property
 Public ReadOnly Property nEncounterXP() As Integer
     Get
         Dim xptotal As Integer = 0
         For Each fighter As Combatant In Roster.Values
             If Not fighter.bPC Then
                 xptotal += fighter.nXP
             End If
         Next
         Return xptotal
     End Get
 End Property
 Public ReadOnly Property nPartySize() As Integer
     Get
         Dim nPCCount As Integer = 0
         For Each fighter As Combatant In Roster.Values
             If fighter.bPC Then
                 nPCCount += 1
             End If
         Next
         Return nPCCount
     End Get
 End Property
 End Class
 */
