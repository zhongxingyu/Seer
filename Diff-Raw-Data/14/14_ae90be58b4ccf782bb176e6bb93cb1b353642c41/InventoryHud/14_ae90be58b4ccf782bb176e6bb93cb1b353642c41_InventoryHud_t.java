 package aigilas.hud;
 
 import aigilas.creatures.BaseCreature;
 import aigilas.items.Equipment;
 import aigilas.items.GenericItem;
 import aigilas.items.Inventory;
 import aigilas.items.ItemClass;
 import aigilas.management.Commands;
 import com.badlogic.gdx.graphics.Color;
 import sps.bridge.DrawDepth;
 import sps.core.SpxManager;
 import sps.io.Input;
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
         super(owner, SpxManager.VirtualWidth / 2, SpxManager.VirtualHeight / 2);
         _inventory = inventory;
         _equipment = equipment;
         _deltas = new DeltasHud(owner, equipment);
         _equipHud = new EquipmentHud(owner, equipment);
         _currentClassItems = _inventory.getItems(ItemClass.values()[_currentClass]);
     }
 
     @Override
     public void draw() {
         if (_isVisible) {
             SpxManager.Renderer.draw(_menuBase, getInventoryAnchor(), DrawDepth.HudBG, Color.BLACK, (int) SpxManager.getCenter().X, (int) SpxManager.getCenter().Y);
             _textHandler.draw();
             _deltas.draw();
             _equipHud.draw();
         }
     }
 
     private void handleInput() {
         if (Input.isActive(Commands.CycleLeft, _parent.getPlayerIndex())) {
             _currentClass--;
            if (_currentClass < 0) {
                _currentClass = ItemClass.values().length - 1;
             }
             _startingItem = 0;
             _endingItem = 4;
             forceRefresh = true;
         }
 
         if (Input.isActive(Commands.CycleRight, _parent.getPlayerIndex())) {
             _currentClass++;
             if (_currentClass >= ItemClass.values().length) {
                _currentClass = 0;
             }
             _startingItem = 0;
             _endingItem = 4;
             forceRefresh = true;
         }
 
         if (Input.isActive(Commands.MoveDown, _parent.getPlayerIndex())) {
             if (_startingItem < _currentClassItems.size() - 1) {
                 _startingItem++;
                 _endingItem++;
                 forceRefresh = true;
             }
         }
 
         if (Input.isActive(Commands.MoveUp, _parent.getPlayerIndex())) {
             if (_startingItem > 0) {
                 _startingItem--;
                 _endingItem--;
                 forceRefresh = true;
             }
         }
         if (Input.isActive(Commands.Confirm, _parent.getPlayerIndex())) {
             _parent.equip(_currentSelectedItem);
             forceRefresh = true;
         }
         if (Input.isActive(Commands.Cancel, _parent.getPlayerIndex())) {
             _parent.drop(_currentSelectedItem);
             forceRefresh = true;
         }
     }
 
     public void update() {
         if (_isVisible) {
             handleInput();
             _textHandler.update();
             _deltas.update(_currentSelectedItem, forceRefresh);
             _equipHud.update(forceRefresh);
             _textHandler.clear();
             updateInventoryDisplay();
             if (forceRefresh) {
                 forceRefresh = false;
             }
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
 
     private void updateInventoryDisplay() {
        //This needs to be rendered up here, because it won't be rendered if the class contains no items
        _textHandler.writeDefault(getClassDisplay(), 20, (int) (_dimensions.Y * .9), getInventoryAnchor());

         _currentClassItems = _inventory.getItems(ItemClass.values()[_currentClass]);
         if (_currentClassItems.size() > 0) {
             int ii = 0;
             if (forceRefresh) {
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
             _textHandler.writeDefault(displayString, 50, 60, getInventoryAnchor());
             for (int jj = 0; jj < 10; jj++) {
                 if (list[jj] != null) {
                     _textHandler.writeDefault(list[jj], 40, (int) (_dimensions.Y * .9) - 60 * jj, getInventoryAnchor());
                 }
             }
         }
     }
 }
