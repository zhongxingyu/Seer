 package aigilas.creatures;
 
 import aigilas.classes.CreatureClass;
 import aigilas.entities.Elements;
 import aigilas.gods.God;
 import aigilas.gods.GodId;
 import aigilas.hud.HudContainer;
 import aigilas.items.Equipment;
 import aigilas.items.GenericItem;
 import aigilas.items.Inventory;
 import aigilas.management.Commands;
 import aigilas.reactions.ComboMeter;
 import aigilas.skills.SkillId;
 import aigilas.skills.SkillLogic;
 import aigilas.skills.SkillPool;
 import aigilas.statuses.BaseStatus;
 import aigilas.statuses.StatusComponent;
 import aigilas.statuses.StatusPool;
 import aigilas.strategies.BaseStrategy;
 import aigilas.strategies.Strategy;
 import aigilas.strategies.TargetSet;
 import sps.bridge.ActorType;
 import sps.bridge.DrawDepth;
 import sps.bridge.EntityType;
 import sps.core.Logger;
 import sps.core.Point2;
 import sps.entities.CoordVerifier;
 import sps.entities.Entity;
 import sps.entities.EntityManager;
 import sps.entities.IActor;
 import sps.io.Input;
 import sps.paths.Path;
 import sps.text.ActionText;
 import sps.text.ActionTextHandler;
 import sps.text.TextManager;
 import sps.util.IntegerStorage;
 import sps.util.StringStorage;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public abstract class BaseCreature extends Entity implements IActor {
     protected BaseStrategy _strategy;
     protected BaseCreature _master;
     protected CreatureClass _class;
     protected Stats _baseStats;
     protected Stats _maxStats;
     protected God _god;
 
     protected final List<Elements> _composition = new ArrayList<Elements>();
 
     protected SkillPool _skills;
     protected final Point2 _skillVector = new Point2(0, 0);
     protected ComboMeter _combo;
     protected final StatusPool _statuses = new StatusPool();
 
     protected Inventory _inventory;
     protected Equipment _equipment;
 
     protected HudContainer _hudContainer;
     protected final ActionTextHandler _damageText = new ActionTextHandler();
 
     protected int _playerIndex = -1;
     protected boolean _isPlaying = true;
     protected int _currentLevel = 1;
     protected float _experience;
     protected static final float __levelUpAmount = 50;
     protected float _nextLevelExperience = __levelUpAmount;
 
     protected ActorType _actorType;
 
     protected void setup(Point2 location, ActorType type, Stats stats, CreatureClass creatureClass, boolean setClass) {
         _entityType = EntityType.Actor;
         initialize(location, type.Sprite, EntityType.Actor, DrawDepth.Creature);
         init(type, stats, creatureClass, setClass);
     }
 
     protected void setup(Point2 location, ActorType type, Stats stats, CreatureClass creatureClass) {
         setup(location, type, stats, creatureClass, true);
     }
 
     protected void setup(Point2 location, ActorType type, Stats stats) {
         setup(location, type, stats, null, true);
     }
 
     protected void SetClass(CreatureClass cClass) {
         if (_class != cClass || cClass == null || cClass == CreatureClass.NULL) {
             _class = (cClass == null) ? CreatureClass.NULL : cClass;
             if (_skills == null || _actorType == ActorType.Player) {
                 _skills = new SkillPool(this);
             }
             if (_actorType != ActorType.Player) {
                 for (SkillId skillId : SkillLogic.get().getElementalSkills(getActorType(), _composition)) {
                     _skills.add(skillId);
                 }
             }
             _skills.add(_class.getLevelSkills(_currentLevel));
         }
     }
 
     private void init(ActorType type, Stats stats, CreatureClass creatureClass, boolean setClass) {
         if (setClass) {
             SetClass(creatureClass);
         }
         _inventory = new Inventory(this);
         _equipment = new Equipment(this);
         _combo = new ComboMeter(this);
         if (_playerIndex > -1) {
             _hudContainer = new HudContainer(this, _inventory, _equipment);
         }
         if (_isBlocking == null) {
             _isBlocking = true;
         }
         _actorType = type;
         _baseStats = new Stats(stats);
         _maxStats = new Stats(_baseStats);
     }
 
     public void pickupItem(GenericItem item) {
         _inventory.add(item);
         EntityManager.get().removeObject(item);
     }
 
     public void equip(GenericItem item) {
         if (_inventory.getItemCount(item) > 0 && !_equipment.isRegistered(item)) {
             _equipment.register(item);
             _inventory.remove(item);
         }
         else {
             if (_equipment.isRegistered(item)) {
                 _equipment.unregister(item);
             }
         }
     }
 
     public void drop(GenericItem item) {
         if (item != null) {
 
             if (_inventory.getItemCount(item) > 0) {
                 EntityManager.get().addObject(new GenericItem(item, getLocation()));
                 _inventory.remove(item);
             }
             else {
                 if (_inventory.getItemCount(item) == 0) {
                     _equipment.unregister(item);
                     EntityManager.get().addObject(new GenericItem(item, getLocation()));
                     _inventory.remove(item);
                 }
             }
         }
     }
 
     public GenericItem destroyRandomItemFromInventory() {
         GenericItem item = _inventory.getNonZeroEntry();
         if (item != null) {
             _inventory.remove(item);
         }
         return item;
     }
 
     public boolean isCooledDown() {
         return get(StatType.Move_Cool_Down) >= getMax(StatType.Move_Cool_Down);
     }
 
     @Override
     public void update() {
         _statuses.update();
         if (get(StatType.Health) <= 0) {
             _isActive = false;
         }
         if (_statuses.allows(CreatureAction.Movement)) {
             if (_isPlaying) {
                 if (!isCooledDown()) {
                     adjust(StatType.Move_Cool_Down, 1);
                 }
                 else {
                     _statuses.act();
                 }
                 regenerate();
             }
             if (_strategy != null) {
                 _strategy.act();
                 _combo.update();
             }
         }
         if (_hudContainer != null) {
             _hudContainer.update();
         }
         _damageText.update();
     }
 
     private void regenerate() {
         if (_statuses.allows(CreatureAction.Regeneration)) {
             for (StatType stat : StatType.values()) {
                 if (stat != StatType.Move_Cool_Down && stat != StatType.Regen) {
                     if (_baseStats.getRaw(stat) < _maxStats.getRaw(stat)) {
                         adjust(stat, _baseStats.get(StatType.Regen) / 50);
                     }
                 }
             }
         }
     }
 
     @Override
     public void draw() {
         Path path = _strategy.getPath();
         if (path != null) {
             path.draw();
         }
         super.draw();
         _combo.draw();
         _damageText.draw();
     }
 
     public void write(String text) {
         _damageText.writeAction(text, 30, IntegerStorage.get(getLocation().PosCenterX), IntegerStorage.get(getLocation().PosCenterY));
     }
 
     public boolean toggleInventoryVisibility() {
         return _hudContainer != null && _hudContainer.toggleInventory();
     }
 
     public void setPlaying(boolean isPlaying) {
         _isPlaying = isPlaying;
     }
 
     public boolean isPlaying() {
         return _isPlaying;
     }
 
     public float get(StatType stat) {
         return _baseStats.get(stat) + calculateEquipmentBonus(stat) + calculateInstrinsicBonus(stat);
     }
 
     private float getRaw(StatType stat, boolean isMax) {
         return isMax ? _maxStats.getRaw(stat) : _baseStats.getRaw(stat);
     }
 
     private float getRaw(StatType stat) {
         return getRaw(stat, false);
     }
 
     private float calculateInstrinsicBonus(StatType stat) {
         if (_class == null) {
             return 0;
         }
         return _class.getBonus(_currentLevel, stat);
     }
 
     private float calculateEquipmentBonus(StatType stat) {
         if (_equipment != null) {
             return _equipment.calculateBonus(stat);
         }
         return 0;
     }
 
     public float getMax(StatType stat) {
         return (int) _maxStats.get(stat) + calculateEquipmentBonus(stat) + calculateInstrinsicBonus(stat);
     }
 
     public float setBase(StatType stat, float value) {
         return _baseStats.set(stat, value);
     }
 
     public float setMax(StatType stat, float value) {
         return _maxStats.set(stat, value);
     }
 
     public float set(StatType stat, float value, boolean setMax) {
         return setMax ? setMax(stat, value) : setBase(stat, value);
     }
 
     public float set(StatType stat, float value) {
         return set(stat, value, false);
     }
 
     protected void InitStat(StatType stat, float value) {
         _maxStats.set(stat, value);
         _baseStats.set(stat, value);
     }
 
     public int getPlayerIndex() {
         return _playerIndex;
     }
 
     @Override
     public ActorType getActorType() {
         return _actorType;
     }
 
     protected float adjust(StatType stat, float adjustment, boolean adjustMax) {
         float result = getRaw(stat) + adjustment;
         if (!adjustMax) {
             if (result > getMax(stat)) {
                 result = getMax(stat);
             }
         }
         return set(stat, (result), adjustMax);
     }
 
     protected float adjust(StatType stat, float adjustment) {
         return adjust(stat, adjustment, false);
     }
 
     public void applyDamage(float damage, BaseCreature attacker, boolean showDamage, StatType statType) {
         if (attacker != null) {
             attacker.passOn(this, StatusComponent.Contagion);
             this.passOn(attacker, StatusComponent.Passive);
         }
         if (statType == null) {
             damage -= _baseStats.get(StatType.Defense);
         }
         if (damage <= 0 && statType == null) {
             damage = 0;
         }
         if (showDamage) {
             _damageText.writeAction(StringStorage.get(damage), 30, IntegerStorage.get(getLocation().PosCenterX), IntegerStorage.get(getLocation().PosCenterY));
         }
         if (damage > 0 && _statuses.allows(CreatureAction.ReceiveHealing)) {
             Logger.gameplay(this.toString() + " taking " + damage + " damage" + " from " + attacker);
             adjust((statType == null) ? StatType.Health : statType, -damage);
         }
         if (get(StatType.Health) <= 0) {
            setInactive();
             if (attacker != null) {
                 attacker.addExperience(calculateExperience());
                 attacker.passOn(attacker, StatusComponent.KillReward);
             }
         }
     }
 
     public void applyDamage(float damage, BaseCreature attacker, boolean showDamage) {
         applyDamage(damage, attacker, showDamage, null);
     }
 
     public void applyDamage(float damage, BaseCreature attacker) {
         applyDamage(damage, attacker, true, null);
     }
 
     public void applyDamage(float damage) {
         applyDamage(damage, null, true, null);
     }
 
     public boolean lowerStat(StatType stat, float amount) {
         if (amount != 0) {
             if (get(stat) >= amount) {
                 adjust(stat, -amount);
                 return true;
             }
             return false;
         }
         return true;
     }
 
     public void addBuff(StatBuff buff, boolean applyToMax) {
         if (!applyToMax) {
             _baseStats.addBuff(buff);
         }
         else {
             _maxStats.addBuff(buff);
         }
     }
 
     public void addBuff(StatBuff buff) {
         addBuff(buff, false);
     }
 
     protected float CalculateDamage() {
         return get(StatType.Strength);
     }
 
     private final Point2 target = new Point2(0, 0);
     private final List<BaseCreature> creatures = new ArrayList<BaseCreature>();
 
     public void moveIfPossible(float xVel, float yVel) {
 
         if (_statuses.allows(CreatureAction.Movement)) {
             if ((xVel != 0 || yVel != 0) && isCooledDown()) {
                 target.reset(xVel + getLocation().PosX, yVel + getLocation().PosY);
                 if (!isBlocking() || !CoordVerifier.isBlocked(target)) {
                     move(xVel, yVel);
                     set(StatType.Move_Cool_Down, 0);
                 }
                 if (_statuses.allows(CreatureAction.Attacking)) {
                     creatures.clear();
                     for (IActor actor : EntityManager.get().getActorsAt(target)) {
                         creatures.add((BaseCreature) actor);
                     }
                     if (creatures.size() > 0) {
                         for (BaseCreature creature : creatures) {
                             if (creature != this) {
                                 if ((creature.getActorType() != ActorType.Player && _actorType == ActorType.Player) || (creature.getActorType() == ActorType.Player && _actorType != ActorType.Player) || !_statuses.allows(CreatureAction.WontHitNonTargets)) {
                                     creature.applyDamage(CalculateDamage(), this);
                                     if (!creature.isActive()) {
                                         addExperience(creature.calculateExperience());
                                     }
                                 }
                             }
                         }
                         set(StatType.Move_Cool_Down, 0);
                     }
                 }
             }
         }
     }
 
     public void moveTo(Point2 targetPosition) {
         moveIfPossible(targetPosition.PosX - getLocation().PosX, targetPosition.PosY - getLocation().PosY);
     }
 
     public Point2 getSkillVector() {
         if (null == _skillVector) {
             return null;
         }
         return _skillVector;
     }
 
     public void setSkillVector(Point2 skillVector) {
         _skillVector.setX(skillVector.X);
         _skillVector.setY(skillVector.Y);
     }
 
     public void addExperience(float amount) {
 
         while (amount > 0) {
             float diff = amount;
             if (amount > _nextLevelExperience) {
                 diff = _nextLevelExperience;
                 amount -= _nextLevelExperience;
             }
             else {
                 amount = 0;
             }
             _experience += diff;
             if (_experience > _nextLevelExperience) {
                 _nextLevelExperience += __levelUpAmount;
                 _experience = 0;
                 _currentLevel++;
                 if (_class != null) {
                     _skills.add(_class.getLevelSkills(_currentLevel));
                 }
                 TextManager.add(new ActionText("LEVEL UP!", 100, (int) getLocation().PosX, (int) getLocation().PosY));
             }
         }
     }
 
     public float calculateExperience() {
         return _currentLevel + _baseStats.getSum();
     }
 
     public void cycleActiveSkill(int velocity) {
         if (_statuses.allows(CreatureAction.SkillCycle) && velocity != 0) {
             _skills.cycle(velocity);
         }
     }
 
     public String getActiveSkillName() {
         return _skills.getActiveName();
     }
 
     float lastSum = 0;
 
     public void useActiveSkill() {
         if (_statuses.allows(CreatureAction.SkillUsage)) {
             lastSum = _baseStats.getSum();
             _skills.useActive();
             if (lastSum != _baseStats.getSum()) {
                 _damageText.writeAction(getActiveSkillName(), 40, IntegerStorage.get(getLocation().PosCenterX), IntegerStorage.get(getLocation().PosY));
             }
         }
     }
 
     public TargetSet getTargets() {
         if (_strategy == null) {
             return _master.getTargets();
         }
         return _strategy.getTargets();
     }
 
     private StatType getLowestStat() {
         StatType result = StatType.Age;
         float min = Float.MAX_VALUE;
         List<StatType> possibleStats = new ArrayList<StatType>();
         for (StatType stat : StatType.values()) {
             if (get(stat) < min && stat != StatType.Age && stat != StatType.Move_Cool_Down && stat != StatType.Piety) {
                 possibleStats.add(stat);
             }
         }
         for (StatType stat : possibleStats) {
             if (min > get(stat)) {
                 result = stat;
                 min = get(stat);
             }
         }
         return result;
     }
 
     public void sacrifice(God god, GenericItem sacrifice) {
         assignGod(god);
         adjust(StatType.Piety, sacrifice.Modifiers.getSum() * ((_god.isGoodSacrifice(sacrifice.getItemClass())) ? 3 : 1) * ((_god.isBadSacrifice(sacrifice.getItemClass())) ? -2 : 1), true);
         adjust(StatType.Piety, sacrifice.Modifiers.getSum() * ((_god.isGoodSacrifice(sacrifice.getItemClass())) ? 3 : 1) * ((_god.isBadSacrifice(sacrifice.getItemClass())) ? -2 : 1));
         sacrifice.setInactive();
     }
 
     static final int pietyCost = 500;
 
     public void pray(God god) {
         assignGod(god);
         if (get(StatType.Piety) >= pietyCost) {
             StatType lowest = getLowestStat();
             float adjustment = (get(StatType.Piety) / 100);
             set(lowest, getMax(lowest) + adjustment);
             set(lowest, adjustment, true);
             adjust(StatType.Piety, -pietyCost);
             if (get(StatType.Piety) < 0) {
                 set(StatType.Piety, 0);
             }
         }
         performInteraction();
     }
 
     protected void assignGod(God god) {
         if (_god != god && _god != null) {
             applyDamage(get(StatType.Piety));
             set(StatType.Piety, 0);
             SetClass(god.getCreatureClass());
         }
         _god = god;
     }
 
     public void combo(List<Elements> attack) {
         for (Elements element : attack) {
             _combo.add(element);
         }
     }
 
     public void addStatus(BaseStatus status) {
         _statuses.add(status);
     }
 
     public void passOn(BaseCreature target, StatusComponent componentType) {
         _statuses.passOn(target, componentType);
     }
 
     public void setStrategy(BaseStrategy strategy) {
         _strategy = strategy;
     }
 
     public Strategy getStrategyId() {
         return _strategy.getId();
     }
 
     public float getInventoryCount() {
         return _inventory.nonZeroCount();
     }
 
     public void removeLeastUsedSkill() {
         _skills.removeLeastUsed();
     }
 
     public void react(SkillId skillId) {
         if (_actorType == ActorType.Player && skillId != SkillId.Forget_Skill && _god.Id == GodId.Gluttony) {
             if (_skills.count() < _currentLevel) {
                 _skills.add(skillId);
             }
         }
     }
 
     @Override
     public void performInteraction() {
         setInteracting(false);
         Input.lock(Commands.Confirm, getPlayerIndex());
     }
 
     public void markHotSkill(Commands hotSkillSlot) {
         _skills.makeActiveSkillHot(hotSkillSlot);
     }
 
     public boolean setHotSkillActive(Commands hotkey) {
         return _skills.setHotSkillsActive(hotkey);
     }
 
     public String getHotSkillName(Commands hotSkillSlot) {
         return _skills.getHotSkillName(hotSkillSlot);
     }
 
     public List<ActorType> getTargetActorTypes() {
         return _strategy.getTargetActorTypes();
     }
 
     public float getCurrentSkillCost() {
         return _skills.getCurrentCost();
     }
 
     public SkillId getActiveSkill() {
         return _skills.getActive();
     }
 }
