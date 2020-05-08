 package aigilas.hud;
 
 import aigilas.Common;
 import aigilas.creatures.BaseCreature;
 import aigilas.items.Equipment;
 import aigilas.items.GenericItem;
 import aigilas.items.Inventory;
 import aigilas.items.ItemClass;
 import com.badlogic.gdx.graphics.Color;
 import sps.bridge.Commands;
 import sps.bridge.DrawDepths;
 import sps.core.Point2;
 import sps.graphics.Renderer;
 import sps.io.Input;
 import sps.text.Text;
 import sps.text.TextPool;
 import sps.util.StringSquisher;
 import sps.util.StringStorage;
 
 import java.util.HashMap;
 
 public class InventoryHud extends BaseHud {
     private int _currentClass = 0;
     private final Inventory _inventory;
     private final Equipment _equipment;
     private int _endingItem = 4;
     private int _startingItem = 0;
     private HashMap<GenericItem, Integer> _currentClassItems;
     private GenericItem _currentSelectedItem = null;
 
     private final EquipmentHud _equipHud;
     private final DeltasHud _deltas;
 
     public InventoryHud(BaseCreature owner, Inventory inventory, Equipment equipment) {
         super(owner, Renderer.VirtualWidth / 2, Renderer.VirtualHeight / 2);
         _inventory = inventory;
         _equipment = equipment;
         _deltas = new DeltasHud(owner, equipment);
         _equipHud = new EquipmentHud(owner, equipment);
         _currentClassItems = _inventory.getItems(ItemClass.values()[_currentClass]);
     }
 
     @Override
     public void draw() {
         if (_isVisible) {
             Renderer.get().draw(_menuBase, getInventoryAnchor(), DrawDepths.get(Common.Hud_BG), Color.BLACK, (int) Renderer.get().center().X, (int) Renderer.get().center().Y);
         }
     }
 
     private void handleInput() {
         if (Input.isActive(Commands.get(Common.CycleLeft), _parent.getPlayerIndex())) {
             _currentClass--;
             if (_currentClass < 0) {
                 _currentClass = ItemClass.values().length - 1;
             }
             _startingItem = 0;
             _endingItem = 4;
             forceRefresh = true;
         }
 
         if (Input.isActive(Commands.get(Common.CycleRight), _parent.getPlayerIndex())) {
             _currentClass++;
             if (_currentClass >= ItemClass.values().length) {
                 _currentClass = 0;
             }
             _startingItem = 0;
             _endingItem = 4;
             forceRefresh = true;
         }
 
         if (Input.isActive(Commands.get(Common.MoveDown), _parent.getPlayerIndex())) {
             if (_startingItem < _currentClassItems.size() - 1) {
                 _startingItem++;
                 _endingItem++;
                 forceRefresh = true;
             }
         }
 
         if (Input.isActive(Commands.get(Common.MoveUp), _parent.getPlayerIndex())) {
             if (_startingItem > 0) {
                 _startingItem--;
                 _endingItem--;
                 forceRefresh = true;
             }
         }
         if (Input.isActive(Commands.get(Common.Confirm), _parent.getPlayerIndex())) {
             _parent.equip(_currentSelectedItem);
             forceRefresh = true;
         }
         if (Input.isActive(Commands.get(Common.Cancel), _parent.getPlayerIndex())) {
             _parent.drop(_currentSelectedItem);
             forceRefresh = true;
         }
     }
 
     public void update() {
         if (_isVisible) {
             handleInput();
             updateInventoryDisplay();
         }
         else {
             if (header != null) {
                 header.hide();
                 itemClass.hide();
                 for (Text text : itemList) {
                     if (text != null) {
                         text.hide();
                     }
                 }
             }
         }
         _equipHud.update(forceRefresh);
         _deltas.update(_currentSelectedItem, forceRefresh);
         if (forceRefresh) {
             forceRefresh = false;
         }
     }
 
     @Override
     public void toggle() {
         super.toggle();
         _deltas.toggle();
         _equipHud.toggle();
         forceRefresh = true;
     }
 
     private static final HashMap<Integer, String> __classStrings = new HashMap<Integer, String>();
 
     private String getClassDisplay() {
         if (!__classStrings.containsKey(_currentClass)) {
             __classStrings.put(_currentClass, ItemClass.values()[_currentClass].hudName());
         }
         return __classStrings.get(_currentClass);
     }
 
     private static final String __delim = ")";
     private static final String __equipDelim = "~";
     private static final String __seper = " x";
 
     private String displayString = "";
     private boolean forceRefresh = false;
     private String[] list = new String[10];
 
     private Text header;
     private Text itemClass;
     private Text[] itemList = new Text[10];
 
     private Point2 inventoryPosition = new Point2(0, 0);
 
     private void updateInventoryDisplay() {
         //This needs to be rendered up here, because it won't be rendered if the class contains no items
         if (forceRefresh) {
             if (header != null) {
                 header.hide();
             }
            inventoryPosition.reset(getInventoryAnchor().X + 20, getInventoryAnchor().Y + (int) (_dimensions.Y * .9), false);
             header = TextPool.get().write(getClassDisplay(), inventoryPosition);
 
             _currentClassItems = _inventory.getItems(ItemClass.values()[_currentClass]);
             if (_currentClassItems.size() > 0) {
                 int ii = 0;
 
                 StringSquisher.clear();
                 displayString = StringSquisher.flush();
                 int count = 0;
                list = new String[10];
                 for (GenericItem item : _currentClassItems.keySet()) {
                     if (ii == _startingItem) {
                         _currentSelectedItem = item;
                     }
                     if (!_equipment.isRegistered(item) && _inventory.getItemCount(item) <= 0) {
                         continue;
                     }
 
                     if (ii >= _startingItem && ii < _endingItem && ii < _currentClassItems.keySet().size()) {
                         count++;
                         StringSquisher.clear();
                         StringSquisher.squish(StringStorage.get(ii), __delim, (_equipment.isRegistered(item)) ? __equipDelim : "", item.Name);
                         if (_currentClassItems.get(item) > -1) {
                             StringSquisher.squish(__seper, StringStorage.get(_currentClassItems.get(item)));
                         }
                         if (count < 10) {
                             list[count] = StringSquisher.flush();
                         }
                     }
                     ii++;
                 }
 
             }
             inventoryPosition.reset(getInventoryAnchor().X + 50, getInventoryAnchor().Y + 60, false);
             if (itemClass != null) {
                 itemClass.hide();
             }
             itemClass = TextPool.get().write(displayString, inventoryPosition);
             for (int jj = 0; jj < 10; jj++) {
                 if (itemList[jj] != null) {
                     itemList[jj].hide();
                 }
                 if (list[jj] != null) {
                     inventoryPosition.reset(getInventoryAnchor().X + 40, getInventoryAnchor().Y + (int) (_dimensions.Y * .9) - 60 * jj, false);
                     itemList[jj] = TextPool.get().write(list[jj], inventoryPosition);
                 }
             }
         }
     }
 }
