 package sk.action;
 
 import java.awt.event.KeyEvent;
 
 import org.powerbot.core.script.job.Task;
 import org.powerbot.game.api.methods.Settings;
 import org.powerbot.game.api.methods.Widgets;
 import org.powerbot.game.api.methods.input.Keyboard;
 import org.powerbot.game.api.methods.input.Mouse;
 import org.powerbot.game.api.util.Random;
 import org.powerbot.game.api.util.Timer;
 import org.powerbot.game.api.wrappers.node.Item;
 import org.powerbot.game.api.wrappers.widget.WidgetChild;
 
 import sk.action.ability.Emote;
 import sk.action.ability.MapIcon;
 import sk.action.book.BookAbility;
 import sk.action.book.magic.Spell;
 import sk.general.Completion;
 import sk.general.TimedCondition;
 import sk.tab.MainTabs;
 
 public class ActionBar {
 
 	private static final int CHATBOX_WIDGET = 137, TEXT_BAR = 56;
 
 	public static boolean dragToSlot(final Ability a, final int slot) {
 		int id = getAbilityId(slot);
 		return (a != null && checkIndex(slot))
 				&& ((id > 0 && id == a.getAbilityId()) || (setLocked(false) && a.show() && makeReadyForInteract()
 						&& dragBetween(a.getChild(), getMainChild(slot)) && new TimedCondition(2000) {
 					@Override
 					public boolean isDone() {
 						return getAbilityId(slot) == a.getAbilityId();
 					}
 				}.waitStop()));
 	}
 
 	public static boolean dragToSlot(final WidgetChild wc, final int slot) {
 		return setLocked(false) && makeReadyForInteract() && dragBetween(wc, getMainChild(slot));
 	}
 
 	public static boolean dragToSlot(final Item i, final int slot) {
 		int id = getItemId(slot);
 		WidgetChild wc;
 		return (i != null && checkIndex(slot))
 				&& ((id > 0 && id == i.getId()))
 				|| (setLocked(false) && MainTabs.INVENTORY.open() && (wc = i.getWidgetChild()) != null && wc.visible()
 						&& makeReadyForInteract() && dragBetween(wc, getMainChild(slot)) && new TimedCondition(2000) {
 					@Override
 					public boolean isDone() {
 						return getItemId(slot) == i.getId();
 					}
 				}.waitStop());
 	}
 
 	/**
 	 * Attempts to remove the ability or item from the action bar
 	 * 
 	 * @param slot
 	 *            the slot to remove
 	 * @return <tt>true</tt> if the slot no longer has anything in it
 	 */
 	public static boolean trashSlot(final int slot) {
 		return getSlotType(slot) == ActionSlotType.NOTHING
 				|| (checkIndex(slot) && setLocked(false) && makeReadyForInteract()
 						&& dragBetween(getMainChild(slot), getTrashButton()) && new TimedCondition(2000) {
 
 					@Override
 					public boolean isDone() {
 						return getSlotType(slot) == ActionSlotType.NOTHING;
 					}
 				}.waitStop());
 	}
 
 	private static boolean dragBetween(WidgetChild start, WidgetChild end) {
 		if (start == null || end == null || !start.visible() || !end.visible())
 			return false;
 		if (!hoverChild(start))
 			return false;
 		Mouse.drag(end.getNextViewportPoint());
 		return true;
 	}
 
 	private static boolean hoverChild(WidgetChild wc) {
 		if (wc == null || !wc.visible())
 			return false;
 		wc.hover();
 		Timer t = new Timer(2000);
 		while (t.isRunning()) {
 			if (wc.contains(Mouse.getLocation())) {
 				break;
 			}
 			wc.hover();
 		}
 		return wc.contains(Mouse.getLocation());
 	}
 
 	public static boolean useAbility(Ability a) {
 		if (a == null || !a.available())
 			return false;
 		int slot = findAbility(a);
 		if (slot >= 0) {
 			return useSlot(slot);
 		}
 		if (!a.show())
 			return false;
 		final WidgetChild wc = a.getChild();
 		if (a instanceof BookAbility) {
 			BookAbility ba = (BookAbility) a;
 			final WidgetChild r = ba.getReloadChild();
 			return wc != null && wc.visible() && wc.getTextColor() == 0xFFFFFF && r != null && r.validate()
 					&& !r.visible() && wc.click(true) && new TimedCondition(2000) {
 						@Override
 						public boolean isDone() {
 							return r.visible();
 						}
 					}.waitStop();
 		} else {
 			final Completion c = a.getChange();
 			return wc != null && wc.visible() && c != null && wc.click(true) && new TimedCondition(2000) {
 
 				@Override
 				public boolean isDone() {
 					return c == null || c.isDone();
 				}
 			}.waitStop();
 		}
 	}
 
 	/**
 	 * Attempts to find the {@link Ability} in the action bar
 	 * 
 	 * @param a
 	 *            the Ability to find
 	 * @return the slot in the action bar or <tt>-1</tt> if the Ability was not
 	 *         found
 	 */
 	public static int findAbility(Ability a) {
 		if (a == null)
 			return -1;
 		for (int i = 0; i < NUM_SLOTS; i++) {
 			if (getAbilityId(i) == a.getAbilityId())
 				return i;
 		}
 		return -1;
 	}
 
 	public static boolean useSlot(int slot) {
 		if (!checkIndex(slot) || !setExpanded(true) || !isReady(slot))
 			return false;
 		WidgetChild main = getMainChild(slot);
 		Ability thisAbility = getAbilityInSlot(slot);
 		if (thisAbility != null && !thisAbility.available())
 			return false;
 		final Completion ret = (thisAbility == null) ? Completion.TRUE : thisAbility.getChange();
 		int key = getKeyBind(slot);
 		boolean keyed = false;
 		if (key != -1 && clearKeyboard()) {
 			Keyboard.sendKey((char) key);
 			keyed = true;
 		}
 		return (keyed || main.visible() && main.click(true)) && new TimedCondition(2000) {
 			@Override
 			public boolean isDone() {
				return ret == null || ret.isDone();
 			}
 		}.waitStop();
 	}
 
 	public static boolean interactSlot(int slot, String action) {
 		if (!checkIndex(slot) || !setExpanded(true))
 			return false;
 		if (action == null || action.length() == 0)
 			return useSlot(slot);
 		WidgetChild child = getMainChild(slot);
 		if (child.visible()) {
 			return child.interact(action);
 		}
 		return false;
 	}
 
 	/**
 	 * Makes the action bar ready for interaction
 	 * 
 	 * @return <tt>true</tt> if the action bar is ready after changes
 	 */
 	public static boolean makeReadyForInteract() {
 		return setExpanded(true) && clearKeyboard();
 	}
 
 	/**
 	 * Checks to see if the action bar is ready for interaction
 	 * 
 	 * @return <tt>true</tt> if the action bar is ready
 	 */
 	public static boolean isReadyForInteract() {
 		return isExpanded() && !isKeyboardFocused();
 	}
 
 	private static boolean clearKeyboard() {
 		if (isKeyboardFocused()) {
 			int reps = Random.nextInt(3, 5);
 			for (int i = 0; i < reps; i++) {
 				Keyboard.sendKey((char) KeyEvent.VK_UNDEFINED, KeyEvent.VK_ESCAPE, 50);
 				Task.sleep(50, 100);
 			}
 		}
 		return new TimedCondition(500) {
 
 			@Override
 			public boolean isDone() {
 				return !isKeyboardFocused();
 			}
 		}.waitStop();
 	}
 
 	private static boolean isKeyboardFocused() {
 		return isExpanded() && Widgets.get(CHATBOX_WIDGET, TEXT_BAR).getTextColor() == 0x0000FF;
 	}
 
 	/**
 	 * Expands the bar to either open or closed
 	 * 
 	 * @param expanded
 	 *            whether the bar should end open or closed
 	 * @return <tt>true</tt> if the bar ended expanded like the argument
 	 */
 	public static boolean setExpanded(final boolean expanded) {
 		if (isExpanded() == expanded)
 			return true;
 		WidgetChild tc = getExpandButton();
 		return tc.visible() && tc.click(true) && new TimedCondition(1500) {
 
 			@Override
 			public boolean isDone() {
 				return isExpanded() == expanded;
 			}
 		}.waitStop();
 	}
 
 	/**
 	 * Locks the bar to either locked or unlocked
 	 * 
 	 * @param locked
 	 *            whether the bar should end locked or unlocked
 	 * @return <tt>true</tt> if the bar ended locked like the argument
 	 */
 	public static boolean setLocked(final boolean locked) {
 		if (isLocked() == locked)
 			return true;
 		if (!setExpanded(true))
 			return false;
 		WidgetChild tc = getLockButton();
 		return tc.visible() && tc.click(true) && new TimedCondition(1500) {
 
 			@Override
 			public boolean isDone() {
 				return isLocked() == locked;
 			}
 		}.waitStop();
 	}
 
 	private static final int BAR_WIDGET = 640;
 	private static final int ADRENALINE = 679;
 	private static final int BAR_LOCKED = 682, BAR_LOCKED_MASK = 0x10;
 
 	private static final int CURRENT_BAR_SETTING = 682, CURRENT_BAR_MASK = 0x7, CURRENT_BAR_SHIFT = 5;
 	private static final int PREV_BAR = 24, NEXT_BAR = 23;
 
 	private static final int MAIN_BAR_CHILD = 4;
 	private static final int EXPAND_BUTTON = 3, MINIMIZE_BUTTON = 30;
 	private static final int TARGET_EXPANDED = 31, TARGET_MINIMIZED = 117;
 
 	private static final int LOCK_BUTTON = 26, TRASH_BUTTON = 27;
 
 	/**
 	 * Gets whether the bar is expanded
 	 * 
 	 * @return <tt>true</tt> if the bar is expanded
 	 */
 	public static boolean isExpanded() {
 		return Widgets.get(BAR_WIDGET, MAIN_BAR_CHILD).visible();
 	}
 
 	/**
 	 * Gets whether the bar is locked
 	 * 
 	 * @return <tt>true</tt> if the bar is locked
 	 */
 	public static boolean isLocked() {
 		return Settings.get(BAR_LOCKED, BAR_LOCKED_MASK) == BAR_LOCKED_MASK;
 	}
 
 	/**
 	 * Gets the amount of adrenaline ranging from 0-1000
 	 * 
 	 * @return the amount of adrenaline
 	 */
 	public static int getAdrenaline() {
 		return Settings.get(ADRENALINE);
 	}
 
 	public static WidgetChild getExpandButton() {
 		return Widgets.get(BAR_WIDGET, (isExpanded()) ? MINIMIZE_BUTTON : EXPAND_BUTTON);
 	}
 
 	public static WidgetChild getTargetButton() {
 		return Widgets.get(BAR_WIDGET, (isExpanded()) ? TARGET_EXPANDED : TARGET_MINIMIZED);
 	}
 
 	public static WidgetChild getLockButton() {
 		return Widgets.get(BAR_WIDGET, LOCK_BUTTON);
 	}
 
 	public static WidgetChild getTrashButton() {
 		return Widgets.get(BAR_WIDGET, TRASH_BUTTON);
 	}
 
 	public static WidgetChild getNextButton() {
 		return Widgets.get(BAR_WIDGET, NEXT_BAR);
 	}
 
 	public static WidgetChild getPrevButton() {
 		return Widgets.get(BAR_WIDGET, PREV_BAR);
 	}
 
 	/**
 	 * Gets the index of the current bar
 	 * 
 	 * @return the index of the current bar
 	 */
 	public static int getCurrentBar() {
 		return Settings.get(CURRENT_BAR_SETTING, CURRENT_BAR_SHIFT, CURRENT_BAR_MASK);
 	}
 
 	// Slot based methods
 
 	private static final int NUM_SLOTS = 12;
 
 	private static final int[] ITEM_SETTINGS = new int[NUM_SLOTS], ABILITY_SETTINGS = new int[NUM_SLOTS],
 			MAIN_CHILD = { 34, 38, 41, 44, 47, 50, 53, 56, 59, 62, 65, 68 }, KEY_CHILD = { 70, 75, 79, 83, 87, 91, 95,
 					99, 103, 107, 111, 115 }, COOLDOWN_CHILD = { 36, 73, 77, 81, 85, 89, 93, 97, 101, 105, 109, 113 },
 			ITEM_CHILD = { 32, 72, 76, 80, 84, 88, 92, 96, 100, 104, 108, 112 };
 
 	private static final int ITEM_SETTING_START = 811, ABILITY_SETTING_START = 727;
 	private static final int DEFAULT_ITEM_SETTING = -1, DEFAULT_ABILITY_SETTING = 0;
 	private static final int ITEM_AVAILABLE_TEXT_COLOR = 0xFFFFFF;
 
 	static {
 		for (int i = 0; i < NUM_SLOTS; i++) {
 			ITEM_SETTINGS[i] = ITEM_SETTING_START + i;
 			ABILITY_SETTINGS[i] = ABILITY_SETTING_START + i;
 		}
 	}
 
 	/**
 	 * Gets the keybind for this slot as an int
 	 * 
 	 * @param slot
 	 *            the slot to check
 	 * @return the keybind char or <tt>-1</tt> if no such keybind exists
 	 */
 	public static int getKeyBind(int slot) {
 		WidgetChild wc = getKeyChild(slot);
 		if (wc == null)
 			return -1;
 		String text = wc.getText();
 		if (text == null || text.length() <= 0)
 			return -1;
 		return text.charAt(0);
 	}
 
 	/**
 	 * Checks to see if the slot is ready (can be used and no reload is present)
 	 * 
 	 * @param slot
 	 *            the slot to check
 	 * @return <tt>true</tt> if the slot is ready
 	 */
 	public static boolean isReady(int slot) {
 		if (getSlotType(slot) == ActionSlotType.NOTHING)
 			return false;
 		WidgetChild rchild = getReloadChild(slot), ichild = getItemChild(slot);
 		return rchild != null && rchild.validate() && !rchild.visible() && ichild != null && ichild.validate()
 				&& ichild.getTextColor() == ITEM_AVAILABLE_TEXT_COLOR;
 	}
 
 	/**
 	 * Gets the item id at the slot from settings
 	 * 
 	 * @param slot
 	 *            the slot to get
 	 * @return the item id if the slot is valid or the
 	 *         {@link ActionBar#DEFAULT_ITEM_SETTING} if it is not
 	 */
 	public static int getItemId(int slot) {
 		return checkIndex(slot) ? Settings.get(ITEM_SETTINGS[slot]) : DEFAULT_ITEM_SETTING;
 	}
 
 	/**
 	 * Gets the ability id at the slot from settings
 	 * 
 	 * @param slot
 	 *            the slot to get
 	 * @return the ability id if the slot is valid or the
 	 *         {@link ActionBar#DEFAULT_ABILITY_SETTING} if it is
 	 *         not
 	 */
 	public static int getAbilityId(int slot) {
 		return checkIndex(slot) ? Settings.get(ABILITY_SETTINGS[slot]) : DEFAULT_ABILITY_SETTING;
 	}
 
 	/**
 	 * Gets the type of this slot
 	 * 
 	 * @param slot
 	 *            the slot to check
 	 * @return {@link ActionSlotType#ITEM} if this slot is an item,
 	 *         {@link ActionSlotType#ABILITY} if the slot
 	 *         is an ability, or {@link ActionSlotType#NOTHING} if the slot is
 	 *         invalid or there is nothing in
 	 *         the slot
 	 */
 	public static ActionSlotType getSlotType(int slot) {
 		if (getItemId(slot) > DEFAULT_ITEM_SETTING)
 			return ActionSlotType.ITEM;
 		else if (getAbilityId(slot) > DEFAULT_ABILITY_SETTING)
 			return ActionSlotType.ABILITY;
 		else
 			return ActionSlotType.NOTHING;
 	}
 
 	/**
 	 * Gets the main child for this slot
 	 * 
 	 * @param slot
 	 *            the slot to get
 	 * @return the main {@link WidgetChild} for this slot or <tt>null</tt> if
 	 *         the slot is invalid
 	 */
 	public static WidgetChild getMainChild(int slot) {
 		return checkIndex(slot) ? Widgets.get(BAR_WIDGET, MAIN_CHILD[slot]) : null;
 	}
 
 	/**
 	 * Gets the child with the keybind as text for this slot
 	 * 
 	 * @param slot
 	 *            the slot to get
 	 * @return the {@link WidgetChild} that has the keybind as text for this
 	 *         slot or <tt>null</tt> if the slot
 	 *         is invalid
 	 */
 	public static WidgetChild getKeyChild(int slot) {
 		return checkIndex(slot) ? Widgets.get(BAR_WIDGET, KEY_CHILD[slot]) : null;
 	}
 
 	/**
 	 * Gets the child with the reload progress for this slot
 	 * 
 	 * @param slot
 	 *            the slot to get
 	 * @return the {@link WidgetChild} that has the reload progress for this
 	 *         slot or <tt>null</tt> if the slot
 	 *         is invalid
 	 */
 	public static WidgetChild getReloadChild(int slot) {
 		return checkIndex(slot) ? Widgets.get(BAR_WIDGET, COOLDOWN_CHILD[slot]) : null;
 	}
 
 	/**
 	 * Gets the child with the item for this slot
 	 * 
 	 * @param slot
 	 *            the slot to get
 	 * @return the item {@link WidgetChild} for this slot or <tt>null</tt> if
 	 *         the slot was invalid
 	 */
 	public static WidgetChild getItemChild(int slot) {
 		return checkIndex(slot) ? Widgets.get(BAR_WIDGET, ITEM_CHILD[slot]) : null;
 	}
 
 	/**
 	 * Checks to see if this slot is valid
 	 * 
 	 * @param slot
 	 *            the slot to check
 	 * @return <tt>true</tt> if this slot is valid
 	 */
 	public static boolean checkIndex(int slot) {
 		return slot >= 0 && slot < NUM_SLOTS;
 	}
 
 	private static Ability loader = null;
 
 	/**
 	 * Gets the {@link Ability} in a slot in the action bar
 	 * 
 	 * @param slot
 	 *            the slot to get
 	 * @return the Ability that corresponds to this slot or <tt>null</tt> if the
 	 *         ability was not recognized or
 	 *         there is no ability in the slot
 	 * @see ActionBar#getAbilityWithId(int)
 	 */
 	public static Ability getAbilityInSlot(int slot) {
 		return getAbilityWithId(getAbilityId(slot));
 	}
 
 	/**
 	 * Gets the {@link Ability} that has the corresponding id and also
 	 * initializes all ability enums
 	 * 
 	 * @param aid
 	 *            the ability id
 	 * @return the Ability that corresponds to this id or <tt>null</tt> if no
 	 *         such ability exists
 	 */
 	public static Ability getAbilityWithId(int aid) {
 		load();
 		return Ability.ALL_ABILITIES.get(aid);
 	}
 
 	/**
 	 * Loads all the abilities from their respective enums. ESSENTIAL FUNCTON to calls!
 	 */
 	public static void load() {
 		if (loader == null) {
 			loader = BookAbility.ANTICIPATION;
 			loader = Emote.ANGRY;
 			loader = MapIcon.HEALTH;
 			loader = Spell.HOME_TELEPORT;
 			// TODO add more loaders;
 		}
 	}
 
 }
