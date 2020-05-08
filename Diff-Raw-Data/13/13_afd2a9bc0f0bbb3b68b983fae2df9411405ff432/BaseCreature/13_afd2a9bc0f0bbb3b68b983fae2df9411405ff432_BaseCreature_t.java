 package aigilas.creatures;
 
 import aigilas.Aigilas;
 import aigilas.AigilasConfig;
 import aigilas.GameplayLogger;
 import aigilas.classes.CreatureClass;
 import aigilas.entities.Darkness;
 import aigilas.entities.Elements;
 import aigilas.gods.God;
 import aigilas.gods.GodId;
 import aigilas.hud.HudContainer;
 import aigilas.items.Equipment;
 import aigilas.items.GenericItem;
 import aigilas.items.Inventory;
 import aigilas.reactions.ReactionMeter;
 import aigilas.skills.SkillId;
 import aigilas.skills.SkillLogic;
 import aigilas.skills.SkillPool;
 import aigilas.statuses.BaseStatus;
 import aigilas.statuses.StatusComponent;
 import aigilas.statuses.StatusPool;
 import aigilas.strategies.BaseStrategy;
 import aigilas.strategies.Strategy;
 import aigilas.strategies.StrategyPlan;
 import aigilas.strategies.TargetSet;
 import com.badlogic.gdx.graphics.Color;
 import sps.bridge.*;
 import sps.core.Point2;
 import sps.core.SpsConfig;
 import sps.entities.CoordVerifier;
 import sps.entities.Entity;
 import sps.entities.EntityManager;
 import sps.entities.IActor;
 import sps.io.Input;
 import sps.paths.Path;
 import sps.text.TextEffects;
 import sps.text.TextPool;
 import sps.util.StringStorage;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public abstract class BaseCreature extends Entity implements IActor {
     protected StrategyPlan _strategies;
     protected BaseCreature _master;
     protected CreatureClass _class;
     protected Stats _baseStats;
     protected Stats _maxStats;
     protected God _god;
 
     protected final int BaseWaitTime = AigilasConfig.get().defaultSpeed * AigilasConfig.get().turnsPerSecond;
     protected int waitTime = BaseWaitTime;
     protected int regenTime = BaseWaitTime;
 
     protected final List<Elements> _composition = new ArrayList<Elements>();
 
     protected SkillPool _skills;
     protected final Point2 _skillVector = new Point2(0, 0);
     protected ReactionMeter _combo;
     protected final StatusPool _statuses = new StatusPool();
 
     protected Inventory _inventory;
     protected Equipment _equipment;
 
     protected HudContainer _hudContainer;
 
     protected int _playerIndex = -1;
     protected boolean _isPlaying = true;
     protected int _currentLevel = 1;
     protected float _experience;
     protected static final float __levelUpAmount = 1500;
     protected float _nextLevelExperience = __levelUpAmount;
     protected boolean canMove = true;
     protected Color _attackColor = Color.YELLOW;
 
     protected ActorType _actorType;
 
     protected void setup(Point2 location, ActorType type, Stats stats, CreatureClass creatureClass, boolean setClass) {
         _entityType = EntityTypes.get(Sps.Entities.Actor);
         initialize(location, type.Sprite, EntityTypes.get(Sps.Entities.Actor), DrawDepths.get(Aigilas.DrawDepths.Creature));
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
             if (_skills == null || _actorType == ActorTypes.get(Sps.Actors.Player)) {
                 _skills = new SkillPool(this);
             }
             if (_actorType != ActorTypes.get(Sps.Actors.Player)) {
                 for (SkillId skillId : SkillLogic.get().getElementalSkills(getActorType(), _composition)) {
                     _skills.add(skillId);
                 }
             }
             _skills.add(_class.getLevelSkills(_currentLevel));
         }
         reinitStats();
     }
 
     private void init(ActorType type, Stats stats, CreatureClass creatureClass, boolean setClass) {
         if (setClass) {
             SetClass(creatureClass);
         }
         _inventory = new Inventory(this);
         _equipment = new Equipment(this);
         _combo = new ReactionMeter(this);
         if (_playerIndex > -1) {
             _hudContainer = new HudContainer(this, _inventory, _equipment);
         }
         if (_isBlocking == null) {
             _isBlocking = true;
         }
         _actorType = type;
         reinitStats();
         _baseStats = new Stats(_maxStats);
     }
 
     public void reinitStats() {
         _maxStats = StatsRegistry.get().baseStats(_actorType);
         if (_equipment != null) {
             _maxStats.add(_equipment.getModifiers());
             //TODO Come up with a way to factor level into stats
         }
         if (_class != null) {
             _maxStats.add(_class.getModifiers());
         }
     }
 
     public void pickupItem(GenericItem item) {
         _inventory.add(item);
         EntityManager.get().removeEntity(item);
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
         reinitStats();
     }
 
     public void drop(GenericItem item) {
         if (item != null) {
 
             if (_inventory.getItemCount(item) > 0) {
                 EntityManager.get().addEntity(new GenericItem(item, getLocation()));
                 _inventory.remove(item);
             }
             else {
                 if (_inventory.getItemCount(item) == 0) {
                     _equipment.unregister(item);
                     EntityManager.get().addEntity(new GenericItem(item, getLocation()));
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
         return waitTime <= 0;
     }
 
     public boolean canRegenAgain() {
         return regenTime <= 0;
     }
 
     @Override
     public void update() {
         _statuses.update();
         if (getRaw(StatType.Health) <= 0) {
             disable();
         }
         else {
             setPlaying(true);
         }
         if (isPlaying()) {
             if (_statuses.allows(CreatureAction.Movement)) {
                 if (_isPlaying) {
                     if (!isCooledDown()) {
                         waitTime -= get(StatType.Move_Cool_Down) + 1;
                         if (waitTime > BaseWaitTime) {
                             waitTime = BaseWaitTime;
                         }
                     }
                     else {
                         _statuses.act();
                     }
                     regenerate();
                 }
                 if (_strategies != null) {
                     _strategies.current().act();
                     _combo.update();
                 }
             }
             if (_hudContainer != null) {
                 _hudContainer.update();
             }
         }
     }
 
     private void regenerate() {
         regenTime -= get(StatType.Move_Cool_Down) + 1;
         if (regenTime > BaseWaitTime) {
             regenTime = BaseWaitTime;
         }
         if (_statuses.allows(CreatureAction.Regeneration) && canRegenAgain()) {
             for (StatType stat : StatType.values()) {
                 if (stat != StatType.Move_Cool_Down) {
                     if (getRaw(stat) < getRaw(stat, true)) {
                         adjust(stat, get(StatType.Regen));
                     }
                 }
             }
             resetRegenTime();
         }
     }
 
     private List<Entity> darkness;
 
     @Override
     public void draw() {
         if (isPlaying()) {
             if (SpsConfig.get().viewPaths) {
                 Path path = _strategies.current().getPath();
                 if (path != null) {
                     path.draw();
                 }
             }
             darkness = EntityManager.get().getEntities(EntityTypes.get(Aigilas.Entities.Darkness), getLocation());
             if ((darkness.size() > 0 && ((Darkness) darkness.get(0)).beingLit()) || darkness.size() == 0) {
                 super.draw();
                 _combo.draw();
             }
         }
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
 
     public int get(StatType stat) {
         return _baseStats.get(stat);
     }
 
     private int getRaw(StatType stat, boolean isMax) {
         return isMax ? _maxStats.getRaw(stat) : _baseStats.getRaw(stat);
     }
 
     private int getRaw(StatType stat) {
         return getRaw(stat, false);
     }
 
     public int getMax(StatType stat) {
         return _maxStats.get(stat);
     }
 
     public int setBase(StatType stat, int value) {
         return _baseStats.set(stat, value);
     }
 
     public int setMax(StatType stat, int value) {
         return _maxStats.set(stat, value);
     }
 
     public int set(StatType stat, int value, boolean setMax) {
         return setMax ? setMax(stat, value) : setBase(stat, value);
     }
 
     public int set(StatType stat, int value) {
         return set(stat, value, false);
     }
 
     protected void initStat(StatType stat, int value) {
         if (value <= 0) {
             value = 30;
         }
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
 
     protected int adjust(StatType stat, int adjustment, boolean adjustMax) {
         int result = getRaw(stat) + adjustment;
         if (!adjustMax) {
             if (result > getMax(stat)) {
                 result = getMax(stat);
             }
         }
         if (result < 0) {
             result = 0;
         }
         return set(stat, (result), adjustMax);
     }
 
     protected float adjust(StatType stat, int adjustment) {
         return adjust(stat, adjustment, false);
     }
 
     private String textPrepend = "";
 
     public void applyDamage(int damage, BaseCreature attacker, boolean showDamage, StatType statType) {
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
             textPrepend = "";
             if (damage < 0) {
                 textPrepend = "+";
             }
 
             TextPool.get().write(textPrepend + StringStorage.get(Math.abs(damage)), new Point2(getLocation().PosX + SpsConfig.get().spriteWidth / 3, getLocation().PosY + SpsConfig.get().spriteHeight), .5f, TextEffects.Fountain, (_actorType == ActorTypes.get(Sps.Actors.Player)) ? Color.WHITE : Color.BLACK, .6f);
         }
         if (damage > 0 || (damage < 0 && _statuses.allows(CreatureAction.ReceiveHealing))) {
             if (AigilasConfig.get().gameplayVerbose) {
                 GameplayLogger.log(this.toString() + " taking " + damage + " damage" + " from " + attacker);
             }
             if (_graphic != null && attacker != null) {
                 _graphic.flash(attacker.getAttackColor());
             }
             if (_actorType != ActorTypes.get(Aigilas.Actors.Dummy)) {
                 adjust((statType == null) ? StatType.Health : statType, -damage);
             }
         }
         if (getRaw(StatType.Health) <= 0) {
             disable();
             if (attacker != null) {
                 attacker.addExperience(calculateExperience());
                 attacker.passOn(attacker, StatusComponent.KillReward);
             }
         }
     }
 
     private void disable() {
         if (_actorType == ActorTypes.get(Sps.Actors.Player)) {
             setPlaying(false);
            EntityManager.get().removeFromPlay(this);
             updateLocation(new Point2(0, 0));
             _combo.clear();
         }
         else {
             setInactive();
         }
     }
 
     public void applyDamage(int damage, BaseCreature attacker, boolean showDamage) {
         applyDamage(damage, attacker, showDamage, null);
     }
 
     public void applyDamage(int damage, BaseCreature attacker) {
         applyDamage(damage, attacker, true, null);
     }
 
     public void applyDamage(int damage) {
         applyDamage(damage, null, true, null);
     }
 
     public boolean lowerStat(StatType stat, int amount) {
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
 
     protected int CalculateDamage() {
         return get(StatType.Strength);
     }
 
     private final Point2 target = new Point2(0, 0);
     private final List<BaseCreature> creatures = new ArrayList<BaseCreature>();
 
     public void moveIfPossible(float xVel, float yVel) {
 
         if (_statuses.allows(CreatureAction.Movement) && canMove) {
             if ((xVel != 0 || yVel != 0) && isCooledDown()) {
                 target.reset(xVel + getLocation().PosX, yVel + getLocation().PosY);
                 if (!isBlocking() || !CoordVerifier.isBlocked(target)) {
                     move(xVel, yVel);
                     waitTime = BaseWaitTime;
                 }
                 if (_statuses.allows(CreatureAction.Attacking)) {
                     creatures.clear();
                     for (IActor actor : EntityManager.get().getActorsAt(target)) {
                         creatures.add((BaseCreature) actor);
                     }
                     if (creatures.size() > 0) {
                         for (BaseCreature creature : creatures) {
                             if (creature != this) {
                                 if ((creature.getActorType() != ActorTypes.get(Sps.Actors.Player) && _actorType == ActorTypes.get(Sps.Actors.Player)) || (creature.getActorType() == ActorTypes.get(Sps.Actors.Player) && _actorType != ActorTypes.get(Sps.Actors.Player)) || !_statuses.allows(CreatureAction.WontHitNonTargets)) {
                                     creature.applyDamage(CalculateDamage(), this);
                                     if (!creature.isActive()) {
                                         addExperience(creature.calculateExperience());
                                     }
                                 }
                             }
                         }
                         waitTime = BaseWaitTime;
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
                 TextPool.get().write("LEVEL UP!", new Point2(getLocation().PosX, getLocation().PosY + SpsConfig.get().spriteHeight), 1);
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
                 TextPool.get().write(getActiveSkillName(), new Point2(getLocation().PosX, getLocation().PosY + SpsConfig.get().spriteHeight), .5f, TextEffects.Fountain, Color.WHITE, .8f);
             }
         }
     }
 
     public TargetSet getTargets() {
         if (_strategies.current() == null) {
             return _master.getTargets();
         }
         return _strategies.current().getTargets();
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
             int adjustment = (get(StatType.Piety) / 100);
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
         if (_god != god || _god == null) {
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
         if (_strategies == null) {
             _strategies = new StrategyPlan();
         }
         _strategies.add(strategy);
     }
 
     public void removeStrategy(BaseStrategy strategy) {
         _strategies.remove(strategy);
     }
 
     public Strategy getStrategyId() {
         return _strategies.current().getId();
     }
 
     public int getInventoryCount() {
         return _inventory.nonZeroCount();
     }
 
     public void removeLeastUsedSkill() {
         _skills.removeLeastUsed();
     }
 
     public void react(SkillId skillId) {
         if (_actorType == ActorTypes.get(Sps.Actors.Player) && skillId != SkillId.Forget_Skill && _god.Id == GodId.Gluttony) {
             if (_skills.count() < _currentLevel) {
                 _skills.add(skillId);
             }
         }
     }
 
     @Override
     public void performInteraction() {
         setInteracting(false);
         Input.get().lock(Commands.get(Aigilas.Commands.Confirm), getPlayerIndex());
     }
 
     public void markHotSkill(Command hotSkillSlot) {
         _skills.makeActiveSkillHot(hotSkillSlot);
     }
 
     public boolean setHotSkillActive(Command hotkey) {
         return _skills.setHotSkillsActive(hotkey);
     }
 
     public String getHotSkillName(Command hotSkillSlot) {
         return _skills.getHotSkillName(hotSkillSlot);
     }
 
     public List<ActorType> getTargetActorTypes() {
         return _strategies.current().getTargetActorTypes();
     }
 
     public float getCurrentSkillCost() {
         return _skills.getCurrentCost();
     }
 
     public SkillId getActiveSkill() {
         return _skills.getActive();
     }
 
     public void resetWaitTime() {
         waitTime = BaseWaitTime;
     }
 
     public void resetRegenTime() {
         regenTime = BaseWaitTime;
     }
 
     public Color getAttackColor() {
         return _attackColor;
     }
 }
