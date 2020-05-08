 package fr.debnet.ircrpg.game;
 
 import fr.debnet.ircrpg.Config;
 import fr.debnet.ircrpg.DAO;
 import fr.debnet.ircrpg.Strings;
 import fr.debnet.ircrpg.enums.Action;
 import fr.debnet.ircrpg.enums.Activity;
 import fr.debnet.ircrpg.enums.Model;
 import fr.debnet.ircrpg.enums.Potion;
 import fr.debnet.ircrpg.enums.Return;
 import fr.debnet.ircrpg.enums.Skill;
 import fr.debnet.ircrpg.enums.Status;
 import fr.debnet.ircrpg.game.queues.EventQueue;
 import fr.debnet.ircrpg.interfaces.IQueue;
 import fr.debnet.ircrpg.interfaces.INotifiable;
 import fr.debnet.ircrpg.game.queues.UpdateQueue;
 import fr.debnet.ircrpg.helpers.CheckItem;
 import fr.debnet.ircrpg.helpers.CheckPlayer;
 import fr.debnet.ircrpg.helpers.CheckPotion;
 import fr.debnet.ircrpg.helpers.CheckSpell;
 import fr.debnet.ircrpg.helpers.Helpers;
 import fr.debnet.ircrpg.mock.Random;
 import fr.debnet.ircrpg.models.Event;
 import fr.debnet.ircrpg.models.Item;
 import fr.debnet.ircrpg.models.Modifiers;
 import fr.debnet.ircrpg.models.Player;
 import fr.debnet.ircrpg.models.Result;
 import fr.debnet.ircrpg.models.Spell;
 import fr.debnet.ircrpg.models.Time;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Game engine
  * @author Marc
  */
 public class Game {
 
     private Admin admin;
     private Random random;
     
     private Map<String, Player> playersByNickname;
     private Map<String, Player> playersByUsername;
     private Map<String, Item> itemsByCode;
     private Map<String, Spell> spellsByCode;
     private Map<String, Event> eventsByCode;
     
     private List<IQueue> queues;
 
     /**
      * Constructor
      */
     public Game() {
         this.admin = new Admin(this);
         this.random = new Random();
         this.queues = new ArrayList<>();
         
         this.playersByNickname = new HashMap<>();
         this.playersByUsername = new HashMap<>();
         this.itemsByCode = new HashMap<>();
         this.spellsByCode = new HashMap<>();
         this.eventsByCode = new HashMap<>();
 
         // Run queues
         this.queues.add(UpdateQueue.getInstance(this));
         this.queues.add(EventQueue.getInstance(this));
         
         // Load all entities
         if (Config.PERSISTANCE) {
             this.reloadItems();     // Items
             this.reloadSpells();    // Spells
             this.reloadEvents();    // Events
             this.reloadPlayers();   // Players (must be at the end)
         }
     }
     
     /**
      * Get the admin functions
      * @return Admin instance
      */
     public Admin getAdmin() {
         return this.admin;
     }
     
     /**
      * Reload all players
      */
     protected final void reloadPlayers() {
         List<Player> players = DAO.<Player>getObjectList("from " + Model.PLAYER);
         this.playersByNickname.clear();
         this.playersByUsername.clear();
         for (Player player : players) {
             this.playersByNickname.put(player.getNickname().toLowerCase(), player);
             this.playersByUsername.put(player.getUsername().toLowerCase(), player);
             // Update each queue with all players
             this.updateQueues(player);
         }
     }
     
     /**
      * Reload all items
      */
     protected final void reloadItems() {
         List<Item> items = DAO.<Item>getObjectList("from " + Model.ITEM);
         this.itemsByCode.clear();
         for (Item item : items) {
             this.itemsByCode.put(item.getCode().toLowerCase(), item);
         }
     }
     
     /**
      * Reload all spells
      */
     protected final void reloadSpells() {
         List<Spell> spells = DAO.<Spell>getObjectList("from " + Model.SPELL);
         this.spellsByCode.clear();
         for (Spell spell : spells) {
             this.spellsByCode.put(spell.getCode().toLowerCase(), spell);
         }
     }
     
     /**
      * Reload all events
      */
     protected final void reloadEvents() {
         List<Event> events = DAO.<Event>getObjectList("from " + Model.EVENT);
         this.eventsByCode.clear();
         for (Event event : events) {
             this.eventsByCode.put(event.getCode().toLowerCase(), event);
         }
     }
     
     /**
      * Register a notifiable object to all queues
      * @param notifiable Notifiable object
      */
     public void registerNotifiable(INotifiable notifiable) {
         for (IQueue queue : this.queues) {
             queue.register(notifiable);
         }
     }
     
     /**
      * Update queues
      * @param player Player
      */
     public void updateQueues(Player player) {
         for (IQueue queue : this.queues) {
             queue.update(player);
         }
     }
     
     /**
      * Reload queues
      */
     public void reloadQueues() {
         for (Map.Entry<String, Player> entry : this.playersByNickname.entrySet()) {
             this.updateQueues(entry.getValue());
         }
     }
     
     /**
      * Get player by nickname
      * @param nickname Nickname
      * @return Player instance or null if not found
      */
     public Player getPlayerByNickname(String nickname) {
         String key = nickname.toLowerCase();
         if (this.playersByNickname.containsKey(key)) {
             return this.playersByNickname.get(key);
         }
         return null;
     }
 
     /**
      * Get player by username
      * @param username Username
      * @return Player instance or null if not found
      */
     public Player getPlayerByUsername(String username) {
         String key = username.toLowerCase();
         if (this.playersByUsername.containsKey(key)) {
             return this.playersByUsername.get(key);
         }
         return null;
     }
     
     /**
      * Get item by its code
      * @param item Item code (unique)
      * @return Item instance or null if not found
      */
     public Item getItemByCode(String item) {
         if (this.itemsByCode.containsKey(item)) {
             return this.itemsByCode.get(item);
         }
         return null;
     }
     
     /**
      * Get spell by its code
      * @param spell Spell code (unique)
      * @return Spell instance or null if not found
      */
     public Spell getSpellByCode(String spell) {
         if (this.spellsByCode.containsKey(spell)) {
             return this.spellsByCode.get(spell);
         }
         return null;
     }
     
     /**
      * Get event by its code
      * @param event Event code (unique)
      * @return Event instance of null if not found
      */
     public Event getEventByCode(String event) {
         if (this.eventsByCode.containsKey(event)) {
             return this.eventsByCode.get(event);
         }
         return null;
     }
     
     /**
      * Get events matching player's current context
      * @param player Player
      * @return List of events
      */
     public List<Event> getEventsForPlayer(Player player) {
         List<Event> events = new ArrayList<>();
         // Get player's maximum health and mana
         Modifiers modifiers = new Modifiers(player);
         double maxHealth = player.getMaxHealth() + modifiers.getHealth();
         double maxMana = player.getMaxMana() + modifiers.getMana();
         double healthPercentage = 100 * player.getCurrentHealth() / maxHealth;
         double manaPercentage = 100 * player.getCurrentMana() / maxMana;
         
         for (Map.Entry<String, Event> entry : this.eventsByCode.entrySet()) {
             Event event = entry.getValue();
             // Parameters
             boolean isBelow = event.getValueBelow();
             boolean isPercentage = event.getValuePercentage();
             // Check activity
             boolean activity = event.getActivityCondition() == Activity.NONE || 
                     event.getActivityCondition() == player.getActivity();
             if (!activity) continue;
             // Check status
             boolean status = event.getStatusCondition() == Status.NONE || 
                     event.getStatusCondition() == player.getStatus();
             if (!status) continue;
             // Check level
             boolean level = event.getLevelCondition() == 0 || (isBelow ? 
                     event.getLevelCondition() <= player.getLevel() :
                     event.getLevelCondition() >= player.getLevel() );
             if (!level) continue;
             // Check gold
             boolean gold = event.getGoldCondition() == 0 || (isBelow ? 
                     event.getGoldCondition() <= player.getGold() :
                     event.getGoldCondition() >= player.getGold() );
             if (!gold) continue;
             // Check health
             boolean health = event.getHealthCondition() == 0 || (isPercentage ? 
                 (isBelow ? event.getHealthCondition() <= healthPercentage : 
                     event.getHealthCondition() >= healthPercentage) : 
                 (isBelow ? event.getHealthCondition() <= player.getCurrentHealth() :
                     event.getHealthCondition() >= player.getCurrentHealth() ));
             if (!health) continue;
             // Check mana
             boolean mana = event.getManaCondition() == 0 || (isPercentage ? 
                 (isBelow ? event.getManaCondition() <= manaPercentage : 
                     event.getManaCondition() >= manaPercentage) : 
                 (isBelow ? event.getManaCondition() <= player.getCurrentMana() :
                     event.getManaCondition() >= player.getCurrentMana() ));
             if (!mana) continue;
             // Add executeEvent to list (all checks passed)
             events.add(event);
         }
         return events;
     }
     
     /**
      * Update player data
      * @param sender Player's nickname
      * @return Result
      */
     public Result updatePlayerByNickname(String sender) {
         Player player = this.getPlayerByNickname(sender);
         if (player != null) {
             return this.update(player, false, false);
         }
         return null;
     }
     
     /**
      * Update a player and push returns in given result if success
      * @param result Given result instance
      * @param player Player instance
      * @param save Save the player in database ?
      * @param target Is the player targetted in the action ?
      * @return Result Update result onluy
      */
     protected Result updateAndReturn(Result result, Player player, boolean save, boolean target) {
         Result update = this.update(player, save, target);
         if (update.isSuccess()) result.addReturnList(update.getReturns());
         return update;
     }
     
     /**
      * Update player data (activity, status and experience)
      * @param player Player instance
      * @param save Save the player in database ?
      * @param target Is the player targetted in the action ?
      * @return Result
      */
     public Result update(Player player, boolean save, boolean target) {
         Result result = new Result(Action.UPDATE, player);
         // Get time difference
         java.util.Calendar lastUpdate = player.getLastUpdate();
         Calendar current = Calendar.getInstance();
         long diff = current.getTimeInMillis() - lastUpdate.getTimeInMillis();
         // Items modifiers
         Modifiers modifiers = new Modifiers(player);
         // Status
         double hours = diff > player.getStatusDuration()
             ? player.getStatusDuration() * 1d / Config.HOUR : diff * 1d / Config.HOUR;
         switch (player.getStatus()) {
             case POISONED: {
                 // Removing status if timeout
                 if (diff >= player.getStatusDuration()) {
                     result.addReturn(target ? Return.TARGET_POISON_CURED : Return.PLAYER_POISON_CURED);
                     player.setStatus(Status.NORMAL);
                     player.setStatusDuration(0l);
                 } else {
                     player.addStatusDuration(-diff);
                 }
                 // Lower health points
                 double damage = (player.getMaxHealth() * 
                     (Config.POISON_EFFECT + modifiers.getPoisonEffect())) * hours;
                 damage = damage < 0 ? 0d : damage;
                 player.addHealth(-damage);
                 // Update statistics
                 player.addDamageTaken(damage);
                 // Update return
                 result.addPlayerHealthChanges(-damage);
                 break;
             }
             case PARALYZED: {
                 // Removing status if timeout
                 if (diff >= player.getStatusDuration()) {
                     result.addReturn(target ? Return.TARGET_PARALYSIS_CURED : Return.PLAYER_PARALYSIS_CURED);
                     player.setStatus(Status.NORMAL);
                     player.setStatusDuration(0l);
                 } else {
                     player.addStatusDuration(-diff);
                 }
                 break;
             }
             case DEAD: {
                 // Removing status if timeout
                 if (diff >= player.getStatusDuration()) {
                     result.addReturn(target ? Return.TARGET_DEATH_CURED : Return.PLAYER_DEATH_CURED);
                     player.setStatus(Status.NORMAL);
                     player.setStatusDuration(0l);
                     double hp = player.getMaxHealth() + modifiers.getHealth();
                     player.setCurrentHealth(hp);
                     double mp = player.getMaxMana() + modifiers.getMana();
                     player.setCurrentMana(mp);
                     // Update return
                     result.addPlayerHealthChanges(hp);
                     result.addPlayerManaChanges(mp);
                 } else {
                     player.addStatusDuration(-diff);
                 }
                 break;
             }
         }
         // Activity
         hours = diff * 1d / Config.HOUR;
         switch (player.getActivity()) {
             case RESTING: {
                 // Limiting hours to max time
                 hours = hours > Config.RESTING_TIME_MAX / 60d ? Config.RESTING_TIME_MAX / 60d : hours;
                 // Restoring health points
                 double healRate = Config.RATE_HEALTH + modifiers.getHealthRate();
                 double heal = healRate * hours;
                 int maxHp = player.getMaxHealth() + modifiers.getHealth();
                 double hp = player.getCurrentHealth() + heal;
                 hp = hp > maxHp ? maxHp : hp;
                 player.setCurrentHealth(hp);
                 // Removing activity if timeout
                 player.addActivityDuration(diff);
                 if (player.getActivityDuration() >= Config.RESTING_TIME_MAX * Config.MINUTE) {
                     result.setValue(healRate * Config.RESTING_TIME_MAX / 60d);
                     player.setActivity(Activity.WAITING);
                     player.setActivityDuration(0l);
                     result.addReturn(target ? Return.TARGET_RESTING_ENDED : Return.PLAYER_RESTING_ENDED);
                 }
                 // Update statistics
                 player.addTimeResting((long) (hours * Config.HOUR));
                 // Update return
                 result.addPlayerHealthChanges(heal);
                 break;
             }
             case TRAINING: {
                 // Limiting hours to max time
                 hours = hours > Config.TRAINING_TIME_MAX / 60d ? Config.TRAINING_TIME_MAX / 60d : hours;
                 // Add experience points
                 double xpRate = Config.RATE_EXPERIENCE + modifiers.getExperienceRate();
                 double xp = xpRate * hours;
                 player.addExperience(xp);
                 // Removing activity if timeout
                 player.addActivityDuration(diff);
                 if (player.getActivityDuration() >= Config.TRAINING_TIME_MAX * Config.MINUTE) {
                     result.setValue(xpRate * Config.TRAINING_TIME_MAX / 60d);
                     player.setActivity(Activity.WAITING);
                     player.setActivityDuration(0l);
                     result.addReturn(target ? Return.TARGET_TRAINING_ENDED : Return.PLAYER_TRAINING_ENDED);
                 }
                 // Update statistics
                 player.addTimeTraining((long) (hours * Config.HOUR));
                 // Update return
                 result.addPlayerExperienceChanges(xp);
                 break;
             }
             case WORKING: {
                 // Limiting hours to max time
                 hours = hours > Config.WORKING_TIME_MAX / 60d ? Config.WORKING_TIME_MAX / 60d : hours;
                 // Earn gold coins 
                 double goldRate = Config.RATE_GOLD + modifiers.getGoldRate();
                 double gold = goldRate * hours;
                 player.addGold(gold);
                 // Removing activity if timeout
                 player.addActivityDuration(diff);
                 if (player.getActivityDuration() >= Config.WORKING_TIME_MAX * Config.MINUTE) {
                     result.setValue(goldRate * Config.WORKING_TIME_MAX / 60d);
                     player.setActivity(Activity.WAITING);
                     player.setActivityDuration(0l);
                     result.addReturn(target ? Return.TARGET_WORKING_ENDED : Return.PLAYER_WORKING_ENDED);
                 }
                 // Update statistics
                 player.addTimeWorking((long) (hours * Config.HOUR));
                 // Update return
                 result.addPlayerGoldChanges(gold);
                 break;
             }
             case WAITING: {
                 player.addActivityDuration(diff);
                 if (player.getActivityDuration() >= Config.ACTIVITY_PENALTY * Config.MINUTE) {
                     player.setActivity(Activity.NONE);
                     player.setActivityDuration(0l);
                     result.addReturn(target ? Return.TARGET_WAITING_ENDED : Return.PLAYER_WAITING_ENDED);
                 }
                 break;
             }
         }
         // Check if player is dead poisonned
         if (player.getCurrentHealth() <= 0 && player.getStatus() != Status.DEAD) {
             // Change status
             player.setStatus(Status.DEAD);
             player.setStatusDuration(Config.DEATH_PENALTY * Config.MINUTE);
             player.setCurrentHealth(0d);
             // Reset activity
             player.setActivity(Activity.NONE);
             player.setActivityDuration(0l);
             // Update return
             result.addReturn(target ? Return.TARGET_KILLED_BY_POISON : Return.PLAYER_KILLED_BY_POISON);
             // Update statistics
             player.addDeaths(1);
         }
         // Experience gained
         for (int level = player.getLevel() + 1; level < 100; level++) {
             int required = ((level * (level - 1)) / 2) * Config.RATE_LEVEL;
             player.setExperienceRequired(required);
             if (player.getExperience() >= required) {
                 player.addLevel(1);
                 player.addSkillPoints(Config.RATE_SKILL);
                 result.addReturn(target ? Return.TARGET_LEVEL_UP : Return.PLAYER_LEVEL_UP);
             } else break;
         }
         // Save object
         player.addTimeIngame(diff);
         player.setLastUpdate(current);
         if (save) {
             if (Config.PERSISTANCE && !DAO.<Player>setObject(player)) {
                 result.addReturn(Return.PERSISTANCE_ERROR);
                 return result;
             }
             // Update queues on save
             this.updateQueues(player);
         }
         // Return
         result.setSuccess(true);
         if (Config.PERSISTANCE && !result.getReturns().isEmpty()) {
             DAO.<Result>addObject(result);
         }
         return result;
     }
     
     /**
      * Execute an event on a player
      * @param player Player
      * @param event Event
      * @return Result
      */
     public Result executeEvent(Player player, Event event) {
         Result result = new Result(Action.EVENT, player);
         // Calculate variance
         double variance = (this.random.nextDouble() + 0.5d) * event.getVariance();
         variance = variance == 0d ? 1d : variance;
         // Health
         double health = event.getHealthModifier() * variance;
         player.addHealth(health);
         result.setPlayerHealthChanges(health);
         // Mana
         double mana = event.getManaModifier() * variance;
         player.addMana(mana);
         result.setPlayerManaChanges(mana);
         // Experience
         double xp = event.getExperienceModifier() * variance;
         player.addExperience(xp);
         result.setPlayerExperienceChanges(xp);
         // Gold
         double gold = event.getGoldModifier() * variance;
         player.addGold(gold);
         result.setPlayerGoldChanges(gold);
         // Event specific
         result.setDetails(event.getId().toString());
         result.setCustomMessage(event.getDescription());
         // Update and save player
         this.updateAndReturn(result, player, true, false);
         // Return
         result.setSuccess(true);
         if (Config.PERSISTANCE) {
             DAO.<Result>addObject(result);
         }
         return result;
     }
 
     /**
      * Fight between two players (with spell or not)
      * @param sender Attacker's nickname
      * @param target Defender's nickname
      * @param spell Spell code (or null if physical fight)
      * @return Result
      */
     public Result fight(String sender, String target, String magic) {
         Result result = new Result(Action.FIGHT);
         boolean self = sender.equals(target);
         // Get attacker
         Player attacker = this.getPlayer(result, sender);
         if (attacker == null) return result;
         // Get defender
         Player defender;
         if (!self) {
             defender = this.getPlayer(result, target, true);
             if (defender == null) return result;
         } else {
             defender = attacker;
             result.setTarget(defender);
         }
         // Update players
         this.updateAndReturn(result, attacker, false, false);
         if (!self) this.updateAndReturn(result, defender, false, true);
         // Check attacker
         if (!Helpers.checkPlayer(result, attacker, 
             CheckPlayer.from(
                 CheckPlayer.IS_BUSY, 
                 CheckPlayer.IS_DEAD, 
                 CheckPlayer.IS_PARALYZED,
                 CheckPlayer.HAS_ACTED
             )
         )) return result;
         // Check defender
         if (!self && !Helpers.checkPlayer(result, defender, 
             CheckPlayer.from(
                 CheckPlayer.IS_BUSY, 
                 CheckPlayer.IS_DEAD, 
                 CheckPlayer.IS_PARALYZED, 
                 CheckPlayer.IS_TARGET
             )
         )) return result;
         // Get spell
         Spell spell = null;
         if (magic != null) {
             spell = this.getSpellByCode(magic);
             if (spell == null) {
                 // Update return
                 result.addReturn(Return.UNKNOWN_SPELL);
                 return result;
             } else if (!attacker.getSpells().contains(spell)) {
                 // Update return
                 result.addReturn(Return.SPELL_NOT_LEARNED);
                 return result;
             }
         }
         // Items modifiers
         Modifiers attackerModifiers = new Modifiers(attacker);
         Modifiers defenderModifiers = new Modifiers(defender);
         // Attacker phase
         if (spell == null) {
             if (self) {
                 // Update return
                 result.addReturn(Return.NOT_SELF_ATTACK);
                 return result;
             }
             // With no spell
             double accuracy = Config.ATTACK_ACCURACY + attackerModifiers.getAttackAccuracy();
             double chance = random.nextDouble();
             if (chance > accuracy) {
                 // Update return
                 result.addReturn(Return.ATTACK_FAILED);
             } else {
                 // Update return
                 result.addReturn(Return.ATTACK_SUCCEED);
                 // Health change
                 double damage = (attacker.getAttack() + attackerModifiers.getAttack()) * (1d - chance);
                 defender.addHealth(-damage);
                 // Update return
                 result.addTargetHealthChanges(-damage);
                 // Update statistics
                 attacker.addDamageGiven(damage);
                 defender.addDamageTaken(damage);
                 // Is defender dead?
                 if (defender.getCurrentHealth() <= 0) {
                     defender.setStatus(Status.DEAD);
                     defender.setStatusDuration(Config.DEATH_PENALTY * Config.MINUTE);
                     defender.setCurrentHealth(0d);
                     // Update return
                     result.addReturn(Return.TARGET_KILLED);
                     // Update statistics
                     attacker.addKills(1);
                     defender.addDeaths(1);
                     // Gold looted
                     double gold = (Config.THEFT_GOLD + attackerModifiers.getTheftGold()) 
                             * defender.getGold() * random.nextDouble();
                     // Update players
                     attacker.addGold(gold);
                     defender.addGold(-gold);
                     // Update return
                     result.addPlayerGoldChanges(gold);
                     result.addTargetGoldChanges(-gold);
                     // Update statistics
                     attacker.addMoneyStolen(gold);
                 }
             }
             // Experience gained (attacker)
             double bonus = 1 + (defender.getLevel() - attacker.getLevel()) * Config.EXPERIENCE_BONUS;
             bonus = bonus < 0 ? 0 : bonus;
            double xp = (chance > accuracy ? Config.EXPERIENCE_DEFENSE : Config.EXPERIENCE_ATTACK) * 
                     (bonus + attackerModifiers.getExperienceModifier());
             attacker.addExperience(xp);
             // Update return
             result.addPlayerExperienceChanges(xp);
             // Defender phase
             if (defender.getStatus() != Status.DEAD) {
                 if (defender.getStatus() == Status.PARALYZED) {
                     // Update return
                     result.addReturn(Return.NO_STRIKE_BACK);
                 } else {
                     accuracy = Config.DEFENSE_ACCURACY + defenderModifiers.getDefenseAccuracy();
                     chance = random.nextDouble();
                     if (chance > accuracy) {
                         result.addReturn(Return.DEFENSE_FAILED);
                     } else {
                         // Update return
                         result.addReturn(Return.DEFENSE_SUCCEED);
                         // Health change
                         double damage = (defender.getDefense() + defenderModifiers.getDefense()) * (1d - chance);
                         attacker.addHealth(-damage);
                         // Update return
                         result.addPlayerHealthChanges(-damage);
                         // Update statistics
                         defender.addDamageGiven(damage);
                         attacker.addDamageTaken(damage);
                         // Is attacker dead?
                         if (attacker.getCurrentHealth() <= 0) {
                             attacker.setStatus(Status.DEAD);
                             attacker.setStatusDuration(Config.DEATH_PENALTY * Config.MINUTE);
                             attacker.setCurrentHealth(0d);
                             // Update return
                             result.addReturn(Return.PLAYER_KILLED);
                             // Update statistics
                             defender.addKills(1);
                             attacker.addDeaths(1);
                             // Gold looted
                             double gold = (Config.THEFT_GOLD + defenderModifiers.getTheftGold()) 
                                     * defender.getGold() * random.nextDouble();
                             // Update players
                             defender.addGold(gold);
                             attacker.addGold(-gold);
                             // Update return
                             result.addPlayerGoldChanges(-gold);
                             result.addTargetGoldChanges(gold);
                             // Update statistics
                             defender.addMoneyStolen(gold);
                         }
                         // Experience gained (defenser)
                         bonus = 1 + (attacker.getLevel() - defender.getLevel()) * Config.EXPERIENCE_BONUS;
                         bonus = bonus < 0 ? 0 : bonus;
                         xp = Config.EXPERIENCE_DEFENSE * (bonus + defenderModifiers.getExperienceModifier());
                         defender.addExperience(xp);
                         // Update statistics
                         result.addTargetExperienceChanges(xp);
                     }
                 }
             }
         } else {
             result.setAction(Action.MAGIC);
             result.setDetails(spell.getName());
             // With spell
             if (self && !spell.getIsSelf()) {
                 // Update return
                 result.addReturn(Return.NOT_SELF_SPELL);
                 return result;
             }
             if (attacker.getCurrentMana() < spell.getManaCost()) {
                 // Update return
                 result.addReturn(Return.NOT_ENOUGH_MANA);
                 return result;
             }
             double accuracy = Config.MAGIC_ACCURACY + attackerModifiers.getMagicAccuracy();
             if (random.nextDouble() > accuracy) {
                 // Update return
                 result.addReturn(Return.MAGIC_FAILED);
             } else {
                 // Update return
                 result.addReturn(Return.MAGIC_SUCCEED);
                 // Health change
                 double hp = defender.getCurrentHealth();
                 double maxHp = defender.getMaxHealth() + defenderModifiers.getHealth();
                 hp -= spell.getHealthDamage();
                 hp = hp > maxHp ? maxHp : hp < 0 ? 0 : hp;
                 defender.setCurrentHealth(hp);
                 // Update return
                 result.addTargetHealthChanges(-spell.getHealthDamage());
                 // Update statistics
                 if (spell.getHealthDamage() > 0) {
                     attacker.addDamageGiven(spell.getHealthDamage());
                     defender.addDamageTaken(spell.getHealthDamage());
                     // Update return
                     result.addReturn(Return.MAGIC_DAMAGE_HEALTH);
                 } else if (spell.getHealthDamage() < 0) {
                     // Update return
                     result.addReturn(Return.MAGIC_RESTORE_HEALTH);
                 }
                 // Is defender dead?
                 if (hp <= 0) {
                     defender.setStatus(Status.DEAD);
                     defender.setStatusDuration(Config.DEATH_PENALTY * Config.MINUTE);
                     defender.setCurrentHealth(0d);
                     // Update return
                     result.addReturn(Return.TARGET_KILLED);
                     // Update statistics
                     attacker.addKills(1);
                     defender.addDeaths(1);
                     // Gold looted
                     double gold = (Config.THEFT_GOLD + defenderModifiers.getTheftGold()) 
                             * defender.getGold() * random.nextDouble();
                     // Update players
                     defender.addGold(gold);
                     attacker.addGold(-gold);
                     // Update return
                     result.addPlayerGoldChanges(-gold);
                     result.addTargetGoldChanges(gold);
                     // Update statistics
                     defender.addMoneyStolen(gold);
                 } else {
                     Status status = defender.getStatus();
                     // Status change
                     if (spell.getStatus() != Status.NONE) {
                         defender.setStatus(spell.getStatus());
                         defender.setStatusDuration(spell.getStatusDuration());
                         // Update return
                         switch (spell.getStatus()) {
                             case PARALYZED: {
                                 result.addReturn(Return.TARGET_PARALYZED);
                                 break;
                             }
                             case POISONED: {
                                 result.addReturn(Return.TARGET_POISONED);
                                 break;
                             }
                             case NORMAL: {
                                 switch (status) {
                                     case NORMAL:
                                         break;
                                     case PARALYZED:
                                         result.addReturn(Return.TARGET_PARALYSIS_CURED);
                                         break;
                                     case POISONED:
                                         result.addReturn(Return.TARGET_POISON_CURED);
                                         break;
                                 }
                                 break;
                             }
                         }
                     }
                 }
                 // Experience earned (if offensive spell)
                 if (!spell.getIsSelf()) {
                     double bonus = 1 + (defender.getLevel() - attacker.getLevel()) * Config.EXPERIENCE_BONUS;
                     bonus = bonus < 0 ? 0 : bonus;
                     double xp = Config.EXPERIENCE_ATTACK * (bonus + attackerModifiers.getExperienceModifier());
                     attacker.addExperience(xp);
                     // Update statistics
                     result.addPlayerExperienceChanges(xp);
                 }
             }
             // Mana consumption
             attacker.addMana(-spell.getManaCost());
             // Update return
             result.addPlayerManaChanges(-spell.getManaCost());
         }
         // Update and save players
         attacker.setLastAction(Calendar.getInstance());
         this.updateAndReturn(result, attacker, true, false);
         if (!self) this.updateAndReturn(result, defender, true, true);
         // Return
         result.setSuccess(true);
         if (Config.PERSISTANCE) {
             DAO.<Result>addObject(result);
         }
         return result;
     }
     
     /**
      * Steal a player
      * @param sender Player's nickname
      * @param target Target's nickname
      * @return Result
      */
     public Result steal(String sender, String target) {
         Result result = new Result(Action.STEAL);
         if (sender.equals(target)) {
             result.addReturn(Return.NOT_SELF_THEFT);
             return result;
         }
         // Get attacker
         Player attacker = this.getPlayer(result, sender);
         if (attacker == null) return result;
         // Get defender
         Player defender = this.getPlayer(result, target, true);
         if (defender == null) return result;
         // Update players
         this.updateAndReturn(result, attacker, false, false);
         this.updateAndReturn(result, defender, false, true);
         // Items modifiers
         Modifiers attackerModifiers = new Modifiers(attacker);
         Modifiers defenderModifiers = new Modifiers(defender);
         // Check attacker
         if (!Helpers.checkPlayer(result, attacker, 
             CheckPlayer.from(
                 CheckPlayer.IS_BUSY,
                 CheckPlayer.IS_DEAD,
                 CheckPlayer.IS_PARALYZED,
                 CheckPlayer.HAS_ACTED
             )
         )) return result;
         // Check defender
         if (!Helpers.checkPlayer(result, defender, 
             CheckPlayer.from(
                 CheckPlayer.IS_BUSY,
                 CheckPlayer.IS_DEAD,
                 CheckPlayer.IS_TARGET
             )
         )) return result;
         // Attacker phase
         double accuracy = Config.THEFT_CHANCE + attackerModifiers.getTheftChance();
         double chance = random.nextDouble();
         if (chance > accuracy) {
             // Update return
             result.addReturn(Return.THEFT_FAILED);
             // Health change
             double damage = (defender.getDefense() + defenderModifiers.getDefense()) * (1d - chance);
             attacker.addHealth(-damage);
             // Update return
             result.addPlayerHealthChanges(-damage);
             // Update statistics
             defender.addDamageGiven(damage);
             attacker.addDamageTaken(damage);
             // Is attacker dead?
             if (attacker.getCurrentHealth() <= 0) {
                 attacker.setStatus(Status.DEAD);
                 attacker.setStatusDuration(Config.DEATH_PENALTY * Config.MINUTE);
                 attacker.setCurrentHealth(0d);
                 // Update return
                 result.addReturn(Return.PLAYER_KILLED);
                 // Update statistics
                 defender.addKills(1);
                 attacker.addDeaths(1);
                 // Gold looted
                 double gold = (Config.THEFT_GOLD + defenderModifiers.getTheftGold()) 
                         * defender.getGold() * random.nextDouble();
                 // Update players
                 defender.addGold(gold);
                 attacker.addGold(-gold);
                 // Update return
                 result.addPlayerGoldChanges(-gold);
                 result.addTargetGoldChanges(gold);
                 // Update statistics
                 defender.addMoneyStolen(gold);
             }
             // Experience gained (defenser)
             double bonus = 1 + (attacker.getLevel() - defender.getLevel()) * Config.EXPERIENCE_BONUS;
             bonus = bonus < 0 ? 0 : bonus;
             double xp = Config.EXPERIENCE_DEFENSE * (bonus + defenderModifiers.getExperienceModifier());
             defender.addExperience(xp);
             // Update statistics
             result.addTargetExperienceChanges(xp);
         } else {
             // Update return
             result.addReturn(Return.THEFT_SUCCEED);
             // Gold stolen
             double gold = (Config.THEFT_GOLD + attackerModifiers.getTheftGold()) 
                     * defender.getGold() * random.nextDouble();
             // Update players
             attacker.addGold(gold);
             defender.addGold(-gold);
             // Update return
             result.addPlayerGoldChanges(gold);
             result.addTargetGoldChanges(-gold);
             // Update statistics
             attacker.addMoneyStolen(gold);
         }
         // Update and save players
         attacker.setLastAction(Calendar.getInstance());
         this.updateAndReturn(result, attacker, true, false);
         this.updateAndReturn(result, defender, true, true);
         // Return
         result.setSuccess(true);
         if (Config.PERSISTANCE) {
             DAO.<Result>addObject(result);
         }
         return result;
     }
     
     /**
      * Drink a potion
      * @param sender Player's nickname
      * @param potionname Potion name
      * @return Result
      */
     public Result drink(String sender, String potionname) {
         Result result = new Result(Action.DRINK);
         // Get the player
         Player player = this.getPlayer(result, sender);
         if (player == null) return result;
         // Get potion
         Potion potion = Potion.from(potionname);
         // Check potion
         if (!Helpers.checkPotion(result, player, potion, 
             CheckPotion.from(
                 CheckPotion.CAN_CURE,
                 CheckPotion.HAS_ENOUGH
             )
         )) return result;
         // Update player
         this.updateAndReturn(result, player, false, false);
         // Check player
         if (!Helpers.checkPlayer(result, player, 
             CheckPlayer.from(
                 CheckPlayer.IS_BUSY,
                 CheckPlayer.IS_DEAD,
                 CheckPlayer.HAS_ACTED
             )
         )) return result;
         // Player modifiers
         Modifiers modifiers = new Modifiers(player);
         // Potion type        
         switch (potion) {
             case HEALTH: {
                 double hp = player.getCurrentHealth();
                 double maxHp = player.getMaxHealth() + modifiers.getHealth();
                 double heal = maxHp * (Config.POTION_HEALTH_RESTORE + modifiers.getPotionHealth());
                 hp = hp + heal > maxHp ? maxHp : hp + heal;
                 // Update player
                 player.setCurrentHealth(hp);
                 player.addHealthPotions(-1);
                 // Update return
                 result.addPlayerHealthChanges(heal);
                 result.addReturn(Return.HEALTH_RESTORED);
                 break;
             }
             case MANA: {
                 double mp = player.getCurrentMana();
                 double maxMp = player.getMaxMana()+ modifiers.getMana();
                 double heal = maxMp * (Config.POTION_MANA_RESTORE + modifiers.getPotionMana());
                 mp = mp + heal > maxMp ? maxMp : mp + heal;
                 // Update player
                 player.setCurrentMana(mp);
                 player.addManaPotions(-1);
                 // Update return
                 result.addPlayerManaChanges(heal);
                 result.addReturn(Return.MANA_RESTORED);
                 break;
             }
             case REMEDY: {
                 Status status = player.getStatus();
                 // Update player
                 player.setStatus(Status.NORMAL);
                 player.setStatusDuration(0l);
                 player.addRemedyPotions(-1);
                 // Update return
                 switch (status) {
                     case PARALYZED: 
                         result.addReturn(Return.PLAYER_PARALYSIS_CURED);
                         break;
                     case POISONED:
                         result.addReturn(Return.PLAYER_POISON_CURED);
                         break;
                 }
                 break;
             }
         }
         // Update and save player
         player.setLastAction(Calendar.getInstance());
         this.updateAndReturn(result, player, true, false);
         // Return
         result.setSuccess(true);
         if (Config.PERSISTANCE) {
             DAO.<Result>addObject(result);
         }
         return result;
     }
     
     /**
      * Start an activity
      * @param sender Player's nickname
      * @param activityname Activity name
      * @return Result
      */
     public Result startActivity(String sender, String activityname) {
         Result result = new Result(Action.START_ACTIVITY);
         // Get the player
         Player player = this.getPlayer(result, sender);
         if (player == null) return result;
         // Get activity
         Activity activity = Activity.from(activityname);
         // Update player
         this.updateAndReturn(result, player, false, false);
         // Check player
         if (!Helpers.checkPlayer(result, player, 
             CheckPlayer.from(
                 CheckPlayer.IS_DEAD,
                 CheckPlayer.IS_PARALYZED,
                 CheckPlayer.HAS_ACTED
             )
         )) return result;
         // Check the activity penalty
         if (player.getActivity() == Activity.WAITING) {
             Time time = new Time((Config.ACTIVITY_PENALTY * Config.MINUTE) - player.getActivityDuration());
             result.setDetails(time.toString());
             result.addReturn(Return.PLAYER_IS_WAITING);
             return result;
         }
         // Check the activity of the player
         if (player.getActivity() != Activity.NONE) {
             result.addReturn(Return.PLAYER_BUSY);
             return result;
         }
         // Set player activity
         player.setActivity(activity);
         player.setActivityDuration(0l);
         // Update and save player
         player.setLastAction(Calendar.getInstance());
         this.updateAndReturn(result, player, true, false);
         // Return
         switch (activity) {
             case RESTING:
                 result.addReturn(Return.START_RESTING);
                 break;
             case TRAINING:
                 result.addReturn(Return.START_TRAINING);
                 break;
             case WORKING:
                 result.addReturn(Return.START_WORKING);
                 break;
         }
         result.setSuccess(true);
         return result;
     }
     
     /**
      * End the current player activity
      * @param sender Player's nickname
      * @return Result
      */
     public Result endActivity(String sender) {
         Result result = new Result(Action.END_ACTIVITY);
         // Get the player
         Player player = this.getPlayer(result, sender);
         if (player == null) return result;
         // Update player
         this.updateAndReturn(result, player, false, false);
         // Player modifiers
         Modifiers modifiers = new Modifiers(player);
         // Earned
         double earned = player.getActivityDuration() * 1d / Config.HOUR;
         // Activity check
         switch (player.getActivity()) {
             case NONE:
             case WAITING: {
                 result.addReturn(Return.PLAYER_NOT_BUSY);
                 return result;
             }
             case RESTING: {
                 if (player.getActivityDuration() < Config.RESTING_TIME_MIN) {
                     result.addReturn(Return.NOT_RESTED_ENOUGH);
                     return result;
                 }
                 result.addReturn(Return.PLAYER_RESTING_ENDED);
                 earned *= Config.RATE_HEALTH + modifiers.getHealthRate();
                 break;
             }
             case WORKING: {
                 if (player.getActivityDuration() < Config.WORKING_TIME_MIN) {
                     result.addReturn(Return.NOT_WORKED_ENOUGH);
                     return result;
                 }
                 result.addReturn(Return.PLAYER_WORKING_ENDED);
                 earned *= Config.RATE_GOLD + modifiers.getGoldRate();
                 break;
             }
             case TRAINING: {
                 if (player.getActivityDuration() < Config.TRAINING_TIME_MIN) {
                     result.addReturn(Return.NOT_TRAINED_ENOUGH);
                     return result;
                 }
                 result.addReturn(Return.PLAYER_TRAINING_ENDED);
                 earned *= Config.RATE_EXPERIENCE + modifiers.getExperienceRate();
                 break;
             }
         }
         // Clear activity
         player.setActivity(Activity.WAITING);
         player.setActivityDuration(0l);
         // Update and save player
         player.setLastAction(Calendar.getInstance());
         this.updateAndReturn(result, player, true, false);
         // Return
         result.setValue(earned);
         result.setSuccess(true);
         return result;
     }
     
     /**
      * Upgrade a player
      * @param sender Player's nickname
      * @param skillname Skill name to raise
      * @return Result
      */
     public Result upgrade(String sender, String skillname) {
         Result result = new Result(Action.UPGRADE);
         // Get the player
         Player player = this.getPlayer(result, sender);
         if (player == null) return result;
         // Get the statistic
         Skill skill = Skill.from(skillname);
         // Check skill
         if (skill == Skill.NONE) {
             result.addReturn(Return.UNKNOWN_SKILL);
             return result;
         }
         // Update player
         this.updateAndReturn(result, player, false, false);
         // Check player
         if (!Helpers.checkPlayer(result, player, 
             CheckPlayer.from(
                 CheckPlayer.IS_BUSY,
                 CheckPlayer.IS_DEAD,
                 CheckPlayer.IS_PARALYZED,
                 CheckPlayer.HAS_ACTED
             )
         )) return result;
         // Check player skill points
         if (player.getSkillPoints() < 1) {
             result.addReturn(Return.NOT_ENOUGH_SKILL_POINTS);
             return result;
         }
         // Rising stats
         switch (skill) {
             case HEALTH: {
                 player.addMaxHealth(Config.SKILL_HEALTH);
                 player.addHealth(Config.SKILL_HEALTH);
                 result.addReturn(Return.HEALTH_INCREASED);
                 result.setValue(Config.SKILL_HEALTH);
                 break;
             }
             case MANA: {
                 player.addMaxMana(Config.SKILL_MANA);
                 player.addMana(Config.SKILL_MANA);
                 result.addReturn(Return.MANA_INCREASED);
                 result.setValue(Config.SKILL_MANA);
                 break;
             }
             case ATTACK: {
                 player.addAttack(Config.SKILL_ATTACK);
                 result.addReturn(Return.ATTACK_INCREASED);
                 result.setValue(Config.SKILL_ATTACK);
                 break;
             }
             case DEFENSE: {
                 player.addDefense(Config.SKILL_DEFENSE);
                 result.addReturn(Return.DEFENSE_INCREASED);
                 result.setValue(Config.SKILL_DEFENSE);
                 break;
             }
         }
         // Decrease skill points
         player.addSkillPoints(-1);
         // Update and save player
         player.setLastAction(Calendar.getInstance());
         this.updateAndReturn(result, player, true, false);
         // Return
         result.setSuccess(true);
         return result;
     }
     
     /**
      * Buy an item in the shop
      * @param sender Player's nickname
      * @param code Item's code
      * @return Result
      */
     public Result buy(String sender, String code) {
         Result result = new Result(Action.BUY);
         // Get the player
         Player player = this.getPlayer(result, sender);
         if (player == null) return result;
         // Check if item is potion
         Potion potion = Potion.from(code.toLowerCase());
         // Check item
         Item item = null;
         if (potion == Potion.NONE) {
             item = this.getItemByCode(code.toLowerCase());
             if (!Helpers.checkItem(result, player, item, 
                 CheckItem.from(
                     CheckItem.CAN_BE_AFFORDED,
                     CheckItem.CAN_BE_WORN,
                     CheckItem.HAS_ENOUGH_STOCK,
                     CheckItem.IS_ADMIN_ONLY,
                     CheckItem.IS_ALREADY_BOUGHT,
                     CheckItem.TYPE_ALREADY_EQUIPPED
                 )
             )) return result;
         }
         // Check player
         if (!Helpers.checkPlayer(result, player, 
             CheckPlayer.from(
                 CheckPlayer.IS_BUSY,
                 CheckPlayer.IS_DEAD,
                 CheckPlayer.IS_PARALYZED,
                 CheckPlayer.HAS_ACTED
             )
         )) return result;
         // Item
         if (item != null) {
             // Decrease stock
             item.addStock(-1);
             if (Config.PERSISTANCE) {
                 if (!DAO.<Item>setObject(item)) {
                     result.addReturn(Return.PERSISTANCE_ERROR);
                     return result;
                 }
             }
             // Add item in player's inventory
             player.addItem(item);
             player.addGold(-item.getGoldCost());
             result.addReturn(Return.ITEM_SUCCESSFULLY_BOUGHT);
             result.setDetails(item.getName());
             // Increase current health
             if (item.getHealthModifier() > 0) {
                 player.addHealth(item.getHealthModifier());
             }
             // Increase current mana
             if (item.getManaModifier() > 0) {
                 player.addMana(item.getManaModifier());
             }
             // Update return
             result.addPlayerGoldChanges(-item.getGoldCost());
             // Update statistics
             player.addMoneySpent(item.getGoldCost());
         // Potion
         } else {
             // Check potion price
             if (!Helpers.checkPotion(result, player, potion, 
                 CheckPotion.from(
                     CheckPotion.CAN_BE_AFFORDED
                 )
             )) return result;
             // Potion type
             switch (potion) {
                 case HEALTH: {
                     player.addHealthPotions(1);
                     player.addGold(-Config.POTION_HEALTH_COST);
                     // Update return
                     result.addPlayerGoldChanges(-Config.POTION_HEALTH_COST);
                     // Update statistics
                     player.addMoneySpent(Config.POTION_HEALTH_COST);
                     break;
                 }
                 case MANA: {
                     player.addManaPotions(1);
                     player.addGold(-Config.POTION_MANA_COST);
                     // Update return
                     result.addPlayerGoldChanges(-Config.POTION_MANA_COST);
                     // Update statistics
                     player.addMoneySpent(Config.POTION_MANA_COST);
                     break;
                 }
                 case REMEDY: {
                     player.addRemedyPotions(1);
                     player.addGold(-Config.POTION_REMEDY_COST);
                     // Update return
                     result.addPlayerGoldChanges(-Config.POTION_REMEDY_COST);
                     // Update statistics
                     player.addMoneySpent(Config.POTION_REMEDY_COST);
                     break;
                 }
             }
             result.addReturn(Return.POTION_SUCCESSFULLY_BOUGHT);
             result.setDetails(potion.toString());
         }
         // Update and save player
         player.setLastAction(Calendar.getInstance());
         this.updateAndReturn(result, player, true, false);
         // Return
         result.setSuccess(true);
         return result;
     }
     
     /**
      * Sell an item
      * @param sender Player's nickname
      * @param code Item's code
      * @return result
      */
     public Result sell(String sender, String code) {
         Result result = new Result(Action.SELL);
         // Get the player
         Player player = this.getPlayer(result, sender);
         if (player == null) return result;
         // Check item
         Item item = this.getItemByCode(code.toLowerCase());
         if (!Helpers.checkItem(result, player, item, 
             CheckItem.from(
                 CheckItem.IS_ALREADY_BOUGHT
             )
         )) return result;
         // Increase stock
         item.addStock(1);
         if (Config.PERSISTANCE) {
             if (!DAO.<Item>setObject(item)) {
                 result.addReturn(Return.PERSISTANCE_ERROR);
                 return result;
             }
         }
         // Sell item
         player.removeItem(item);
         player.addGold(item.getGoldCost() * Config.SELL_MALUS);
         result.addReturn(Return.ITEM_SUCCESSFULLY_SOLD);
         result.setDetails(item.getName());
         // Update and save player
         player.setLastAction(Calendar.getInstance());
         this.updateAndReturn(result, player, true, false);
         // Return
         result.setSuccess(true);
         return result;
     }
     
     /**
      * Learn a spell
      * @param sender Player's nickname
      * @param code Spell's code
      * @return Result
      */
     public Result learn(String sender, String code) {
         Result result = new Result(Action.LEARN);
         // Get the player
         Player player = this.getPlayer(result, sender);
         if (player == null) return result;
         // Check spell
         Spell spell = this.getSpellByCode(code.toLowerCase());
         if (!Helpers.checkSpell(result, player, spell, 
             CheckSpell.from(
                 CheckSpell.CAN_BE_AFFORDED,
                 CheckSpell.IS_ADMIN_ONLY,
                 CheckSpell.IS_ALREADY_LEARNED,
                 CheckSpell.CAN_BE_LEARNED
             )
         )) return result;
         // Check player
         if (!Helpers.checkPlayer(result, player, 
             CheckPlayer.from(
                 CheckPlayer.IS_BUSY,
                 CheckPlayer.IS_DEAD,
                 CheckPlayer.IS_PARALYZED,
                 CheckPlayer.HAS_ACTED
             )
         )) return result;
         // Add item in player's inventory
         player.addSpell(spell);
         player.addGold(-spell.getGoldCost());
         result.setDetails(spell.getName());
         // Update return
         result.addPlayerGoldChanges(-spell.getGoldCost());
         result.addReturn(Return.SPELL_SUCCESSFULLY_LEARNED);
         // Update statistics
         player.addMoneySpent(spell.getGoldCost());
         // Update and save player
         player.setLastAction(Calendar.getInstance());
         this.updateAndReturn(result, player, true, false);
         // Return
         result.setSuccess(true);
         return result;
     }
     
     /**
      * Show information about item or spell
      * @param code Item or spell code
      * @return Formated string
      */
     public String look(String code) {
         // Potion
         Potion potion = Potion.from(code.toLowerCase());
         switch (potion) {
             case HEALTH:
                 return String.format(Strings.FORMAT_POTION_HEALTH, Potion.HEALTH,
                     Config.POTION_HEALTH_RESTORE * 100, 
                     Config.POTION_HEALTH_COST);
             case MANA:
                 return String.format(Strings.FORMAT_POTION_MANA, Potion.MANA,
                     Config.POTION_MANA_RESTORE * 100, 
                     Config.POTION_MANA_COST);
             case REMEDY:
                 return String.format(Strings.FORMAT_POTION_REMEDY, Potion.REMEDY,
                     Config.POTION_REMEDY_COST);
         }
         // Item
         Item item = this.getItemByCode(code.toLowerCase());
         if (item != null) {
             return Strings.format(Strings.FORMAT_ITEM_INFOS, item.toMap());
         }
         // Spell
         Spell spell = this.getSpellByCode(code.toLowerCase());
         if (spell != null) {
             return Strings.format(Strings.FORMAT_SPELL_INFOS, spell.toMap());
         }
         return null;
     }
     
     /**
      * Show player's all items
      * @param nickname Player's nickname
      * @return Formatted string
      */
     public String showItems(String nickname) {
         Player player = this.getPlayerByNickname(nickname);
         if (player == null || !player.getOnline()) return null;
         // Update player
         this.update(player, false, false);
         // Build list of items
         List<String> items = new ArrayList<>();
         for (Item item : player.getItems()) {
             items.add(Strings.format(Strings.FORMAT_ITEM_NAME, item.toMap()));
         }
         // Return data
         if (items.isEmpty()) items.add(Strings.FORMAT_NONE);
         String string = String.format(Strings.FORMAT_PLAYER_ITEMS, Strings.join(items, ", "));
         return Strings.format(string, player.toMap());
     }
     
     /**
      * Show player's all spells
      * @param nickname Player's nickname
      * @return Formatted string
      */
     public String showSpells(String nickname) {
         Player player = this.getPlayerByNickname(nickname);
         if (player == null || !player.getOnline()) return null;
         // Update player
         this.update(player, false, false);
         // Build list of spells
         List<String> spells = new ArrayList<>();
         for (Spell spell : player.getSpells()) {
             spells.add(Strings.format(Strings.FORMAT_SPELL_NAME, spell.toMap()));
         }
         // Return data
         if (spells.isEmpty()) spells.add(Strings.FORMAT_NONE);
         String string = String.format(Strings.FORMAT_PLAYER_SPELLS, Strings.join(spells, ", "));
         return Strings.format(string, player.toMap());
     }
     
     /**
      * Show player's informations
      * @param nickname Player's nickname
      * @return Formatted string
      */
     public String showInfos(String nickname) {
         Player player = this.getPlayerByNickname(nickname);
         if (player == null || !player.getOnline()) return null;
         // Update player
         this.update(player, false, false);
         // Return data
         return Strings.format(Strings.FORMAT_PLAYER_INFOS, player.toMap());
     }
     
     /**
      * Show player's statistics
      * @param nickname Player's nickname
      * @return Formatted string
      */
     public String showStats(String nickname) {
         Player player = this.getPlayerByNickname(nickname);
         if (player == null || !player.getOnline()) return null;
         // Update player
         this.update(player, false, false);
         // Return data
         return Strings.format(Strings.FORMAT_PLAYER_STATS, player.toMap());
     }
     
     /**
      * Show items the player can afford
      * @param nickname Player's nickname
      * @return Formatted string
      */
     public String showItemsToBuy(String nickname) {
         Player player = this.getPlayerByNickname(nickname);
         if (player == null || !player.getOnline()) return null;
         // Update player
         this.update(player, false, false);
         // Get items which can be bought
         List<String> items = new ArrayList<>();
         for (Map.Entry<String, Item> entry : this.itemsByCode.entrySet()) {
             Item item = entry.getValue();
             // Check item conditions
             boolean admin = !item.getIsAdmin() || (item.getIsAdmin() && player.getAdmin());
             boolean level = item.getMinLevel() <= player.getLevel();
             boolean money = item.getGoldCost() <= player.getGold();
             boolean stock = item.getStock() > 0;
             boolean owned = !player.getItems().contains(item);
             // Add item to list
             if (admin && level && money && stock && owned) {
                 String string = Strings.format(Strings.FORMAT_ITEM_NAME, item.toMap());
                 items.add(string);
             }
         }
         // Return data
         if (items.isEmpty()) items.add(Strings.FORMAT_NONE);
         String string = String.format(Strings.FORMAT_SHOP_ITEMS, Strings.join(items, ", "));
         return Strings.format(string, player.toMap());
     }
     
     /**
      * Show spells the player can learn
      * @param nickname Player's nickname
      * @return Formatted string
      */
     public String showSpellsToLearn(String nickname) {
         Player player = this.getPlayerByNickname(nickname);
         if (player == null || !player.getOnline()) return null;
         // Update player
         this.update(player, false, false);
         // Get items which can be bought
         List<String> spells = new ArrayList<>();
         for (Map.Entry<String, Spell> entry : this.spellsByCode.entrySet()) {
             Spell spell = entry.getValue();
             // Check item conditions
             boolean admin = !spell.getIsAdmin() || (spell.getIsAdmin() && player.getAdmin());
             boolean level = spell.getMinLevel() <= player.getLevel();
             boolean money = spell.getGoldCost() <= player.getGold();
             boolean magic = spell.getManaCost() <= player.getMaxMana();
             boolean owned = !player.getSpells().contains(spell);
             // Add item to list
             if (admin && level && money && magic && owned) {
                 String string = Strings.format(Strings.FORMAT_ITEM_NAME, spell.toMap());
                 spells.add(string);
             }
         }
         // Return data
         if (spells.isEmpty()) spells.add(Strings.FORMAT_NONE);
         String string = String.format(Strings.FORMAT_SHOP_SPELLS, Strings.join(spells, ", "));
         return Strings.format(string, player.toMap());
     }
     
     /**
      * Show the list of online players
      * @return Formatted string
      */
     public String showPlayers() {
         int count = 0;
         List<String> players = new ArrayList<>();
         for (Map.Entry<String, Player> entry : this.playersByNickname.entrySet()) {
             if (entry.getValue().getOnline()) {
                 players.add(entry.getValue().getNickname());
                 count++;
             }
         }
         if (players.isEmpty()) players.add(Strings.FORMAT_NONE);
         return String.format(Strings.FORMAT_LIST_PLAYERS, count, Strings.join(players, ", "));
     }
 
     /**
      * Log in a player
      * @param username Player's username
      * @param password Player's password
      * @param nickname Player's nickname
      * @param hostname Player's hostname
      * @return Result
      */
     public Result login(String username, String password, String nickname, String hostname) {
         Result result = new Result(Action.LOGIN);
         result.setDetails(username);
         Player player = this.getPlayerByUsername(username);
         if (player != null) {
             if (!player.getOnline()) {
                 if (player.getPassword().equals(password)) {
                     // Clean player references
                     this.playersByNickname.remove(player.getNickname());
                     this.playersByNickname.remove(nickname.toLowerCase());
                     this.playersByNickname.put(nickname.toLowerCase(), player);
                     // Update player
                     player.setNickname(nickname);
                     player.setHostname(hostname);
                     player.setOnline(true);
                     player.setLastUpdate(Calendar.getInstance());
                     this.updateAndReturn(result, player, true, false);
                     // Return result
                     result.setPlayer(player);
                     result.addReturn(Return.LOGIN_SUCCEED);
                     result.setSuccess(true);
                 } else result.addReturn(Return.WRONG_PASSWORD);
             } else result.addReturn(Return.ALREADY_ONLINE);
         } else result.addReturn(Return.USERNAME_NOT_FOUND);
         return result;
     }
     
     /**
      * Try to identify a player from his nickname and hostname only
      * If fail, the player must use the login function to proceed
      * @param nickname Player's nickname
      * @param hostname Player's hostname
      * @return Result
      */
     public Result tryRelogin(String nickname, String hostname) {
         Result result = new Result(Action.LOGIN);
         result.setDetails(nickname);
         Player player = this.getPlayerByNickname(nickname);
         if (player != null && !player.getOnline()) {
             if (hostname.equals(player.getHostname())) {
                 // Update player
                 player.setOnline(true);
                 player.setLastUpdate(Calendar.getInstance());
                 this.updateAndReturn(result, player, true, false);
                 // Return result
                 result.setPlayer(player);
                 result.addReturn(Return.LOGIN_SUCCEED);
                 result.setSuccess(true);
             }
         }
         return result;
     }
 
     /**
      * Log out a player
      * @param nickname Player's nickname
      * @return Result
      */
     public Result logout(String nickname) {
         Result result = new Result(Action.LOGOUT);
         result.setDetails(nickname);
         Player player = this.getPlayerByNickname(nickname);
         if (player != null) {
             // Update player
             player.setOnline(false);
             this.updateAndReturn(result, player, true, false);
             // Return result
             result.setPlayer(player);
             result.addReturn(Return.LOGOUT_SUCCEED);
             result.setSuccess(true);
         } else result.addReturn(Return.NOT_ONLINE);
         return result;
     }
     
     /**
      * Change the player's nickname
      * @param oldNickname Old nickname
      * @param newNickname New nickname
      * @return True if success, false else
      */
     public boolean changeNickname(String oldNickname, String newNickname) {
         Player player = this.getPlayerByNickname(oldNickname);
         if (player != null) {
             player.setNickname(newNickname);
             this.playersByNickname.remove(oldNickname.toLowerCase());
             this.playersByNickname.put(newNickname.toLowerCase(), player);
             return true;
         }
         return false;
     }
     
     /**
      * Change the player's password
      * @param nickname Player's nickname
      * @param password New password
      * @return True if success, false else
      */
     public boolean changePassword(String nickname, String password) {
         Player player = this.getPlayerByNickname(nickname);
         if (player != null) {
             player.setPassword(password);
             return true;
         }
         return false;
     }
     
     /**
      * Disconnect all players
      */
     public void disconnectAll() {
         for (Map.Entry<String, Player> entry : this.playersByNickname.entrySet()) {
             Player player = entry.getValue();
             player.setOnline(false);
             this.update(player, true, false);
         }
     }
 
     /**
      * Register a new player
      * @param username Username
      * @param password Password
      * @param nickname Nickname
      * @param hostname Hostname
      * @return Result
      */
     public Result register(String username, String password, String nickname, String hostname) {
         Result result = new Result(Action.REGISTER);
         if (!this.playersByUsername.containsKey(username.toLowerCase())) {
             if (!this.playersByNickname.containsKey(nickname.toLowerCase())) {
                 Player player = new Player();
                 player.setUsername(username);
                 player.setPassword(password);
                 player.setNickname(nickname);
                 player.setHostname(hostname);
                 player.setOnline(true);
                 player.setAdmin(this.playersByNickname.isEmpty());
                 if (Config.PERSISTANCE) {
                     if (DAO.<Player>addObject(player) == 0) {
                         result.addReturn(Return.PERSISTANCE_ERROR);
                         return result;
                     }
                 }
                 this.playersByUsername.put(username.toLowerCase(), player);
                 this.playersByNickname.put(nickname.toLowerCase(), player);
                 result.setPlayer(player);
                 result.addReturn(Return.REGISTER_SUCCEED);
                 result.setSuccess(true);
             } else result.addReturn(Return.NICKNAME_IN_USE);
         } else result.addReturn(Return.USERNAME_ALREADY_TAKEN);
         return result;
     }
     
     /**
      * Get and check player
      * @see #getPlayer(fr.debnet.ircrpg.game.Result, java.lang.String) 
      * @param result [out] Result
      * @param name Player's nickname
      * @return Player if found and online, null else
      */
     private Player getPlayer(Result result, String name) {
         return this.getPlayer(result, name, false);
     }
     
     /**
      * Get and check player
      * @param result [out] Result
      * @param name Player's nickname
      * @param target Is target ?
      * @return Player if found and online, null else
      */
     private Player getPlayer(Result result, String name, boolean target) {
         result.setDetails(name);
         // Check if the player exists
         Player player = this.getPlayerByNickname(name);
         if (player == null) {
             result.addReturn(target ? Return.UNKNOWN_TARGET : Return.UNKNOWN_PLAYER);
             result.setDetails(name);
             return null;
         }
         // Add player
         if (target) result.setTarget(player); else result.setPlayer(player);
         // Check if the player is logged in
         if (!player.getOnline()) {
             result.addReturn(target ? Return.TARGET_OFFLINE : Return.PLAYER_OFFLINE);
             return null;
         }
         return player;
     }
     
     /**
      * Get random generator 
      * @return Random generator (mocked)
      */
     protected Random getRandom() {
         return this.random;
     }
     
     /**
      * Explicitly add an item in the list
      * @param item Item instance
      * @return True if the item is successfully added, false sinon
      */
     protected boolean addItem(Item item) {
         if (!this.itemsByCode.containsKey(item.getCode())) {
             this.itemsByCode.put(item.getCode(), item);
             if (Config.PERSISTANCE) {
                 return DAO.<Item>addObject(item) != 0;
             }
             return true;
         }
         return false;
     }
     
     /**
      * Explicitly add a spell in the list
      * @param spell Spell instance
      * @return True if the spell is successfully added, false sinon
      */
     protected boolean addSpell(Spell spell) {
         if (!this.spellsByCode.containsKey(spell.getCode())) {
             this.spellsByCode.put(spell.getCode(), spell);
             if (Config.PERSISTANCE) {
                 return DAO.<Spell>addObject(spell) != 0;
             }
             return true;
         }
         return false;
     }
 }
