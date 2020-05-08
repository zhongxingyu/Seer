 /**
  * 
  */
 package net.mindsoup.pathfindercharactersheet.util;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import net.mindsoup.pathfindercharactersheet.pf.PfAttributes;
 import net.mindsoup.pathfindercharactersheet.pf.PfCharacter;
 import net.mindsoup.pathfindercharactersheet.pf.PfClasses;
 import net.mindsoup.pathfindercharactersheet.pf.PfHandedness;
 import net.mindsoup.pathfindercharactersheet.pf.PfPace;
 import net.mindsoup.pathfindercharactersheet.pf.PfRaces;
 import net.mindsoup.pathfindercharactersheet.pf.feats.PfFeats;
 import net.mindsoup.pathfindercharactersheet.pf.items.Item;
 import net.mindsoup.pathfindercharactersheet.pf.items.ItemEffects;
 import net.mindsoup.pathfindercharactersheet.pf.items.ItemSlots;
 import net.mindsoup.pathfindercharactersheet.pf.items.Weapon;
 import net.mindsoup.pathfindercharactersheet.pf.items.Wearable;
 import net.mindsoup.pathfindercharactersheet.pf.races.PfChooseBonusAttributeRace;
 import net.mindsoup.pathfindercharactersheet.pf.skills.PfSkill;
 import net.mindsoup.pathfindercharactersheet.pf.skills.PfSkills;
 import net.mindsoup.pathfindercharactersheet.pf.util.Dice;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.provider.BaseColumns;
 
 /**
  * @author Valentijn
  *
  */
 public class DatabaseHelper extends SQLiteOpenHelper {
 	// db
 	private static final String DATABASE = "SimplePathfinderCharacterSheet.db";
 	private static final int DATABASE_VERSION = 22;
 	
 	public static abstract class Db implements BaseColumns {
 
 		// table characters
 		private static final String CHARACTER_TABLE = "Characters";
 		private static final String CHAR_NAME = "name";
 		private static final String CHAR_CLASS = "class";
 		private static final String CHAR_RACE = "race";
 		private static final String CHAR_BONUS_STAT = "bonusStat";
 		private static final String CHAR_XP = "xp";
 		private static final String CHAR_MONEY = "money";
 		private static final String CHAR_NEWLEVELS = "newLevels";
 		private static final String CHAR_HITPOINTS = "hitpoints";
 		private static final String CHAR_PACE = "pace";
 		private static final String CHAR_ASRANKS = "availableSkillRanks";
 		private static final String CHAR_AVAILABLE_FEATS = "availableFeats";
 		private static final String CHAR_CHA = "charisma";
 		private static final String CHAR_CON = "constitution";
 		private static final String CHAR_DEX = "dexterity";
 		private static final String CHAR_INT = "intelligence";
 		private static final String CHAR_STR = "strength";
 		private static final String CHAR_WIS = "wisdom";
 			
 		//create
 		public static final String CREATE_CHARACTER_TABLE = "CREATE TABLE IF NOT EXISTS " + Db.CHARACTER_TABLE + " (" +
 				Db._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +  
 				Db.CHAR_NAME + " TEXT NOT NULL ON CONFLICT FAIL," +
 				Db.CHAR_CLASS + " SMALLINT NOT NULL ON CONFLICT FAIL," + 
 				Db.CHAR_RACE + " SMALLINT NOT NULL ON CONFLICT FAIL," + 
 				Db.CHAR_BONUS_STAT + " SMALLINT NOT NULL ON CONFLICT FAIL DEFAULT 0," +
 				Db.CHAR_XP + " INT NOT NULL ON CONFLICT FAIL DEFAULT 0," + 
 				Db.CHAR_MONEY + " INT NOT NULL ON CONFLICT FAIL DEFAULT 0," +
 				Db.CHAR_HITPOINTS + " INT NOT NULL DEFAULT 0," +
 				Db.CHAR_NEWLEVELS + " INT NOT NULL DEFAULT 0," + 
 				Db.CHAR_PACE + " SMALLINT NOT NULL ON CONFLICT FAIL DEFAULT 1," + 
 				Db.CHAR_ASRANKS + " INT NOT NULL ON CONFLICT FAIL DEFAULT 0," + 
 				Db.CHAR_AVAILABLE_FEATS + " INT NOT NULL ON CONFLICT FAIL DEFAULT 1," +
 				Db.CHAR_CHA + " INT DEFAULT 1," +
 				Db.CHAR_CON + " INT DEFAULT 1," + 
 				Db.CHAR_DEX + " INT DEFAULT 1," + 
 				Db.CHAR_INT + " INT DEFAULT 1," + 
 				Db.CHAR_STR + " INT DEFAULT 1," + 
 				Db.CHAR_WIS + " INT DEFAULT 1);";
 		
 		private static final String DROP_CHARACTERS = "DROP TABLE IF EXISTS " + Db.CHARACTER_TABLE;
 		
 		// table skills
 		private static final String SKILLS_TABLE = "TrainedSkills";
 		private static final String SKILL_ID = "skill";
 		private static final String SKILL_RANKS = "ranks";
 		private static final String SKILL_CHAR_ID = "character_id";		
 		
 		public static final String CREATE_SKILLS_TABLE = "CREATE TABLE IF NOT EXISTS " + Db.SKILLS_TABLE + " (" +
 				  Db._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
 				  Db.SKILL_ID + " SMALLINT NOT NULL ON CONFLICT FAIL," + 
 				  Db.SKILL_RANKS + " INT NOT NULL ON CONFLICT FAIL DEFAULT 1," + 
 				  Db.SKILL_CHAR_ID + " INT NOT NULL ON CONFLICT FAIL CONSTRAINT character_id_fk REFERENCES " + Db.CHARACTER_TABLE + "(" + Db._ID + ") ON DELETE CASCADE ON UPDATE CASCADE," +
 				  "CONSTRAINT skill_and_char_ids UNIQUE (" + Db.SKILL_ID + ", " + Db.SKILL_CHAR_ID + "));";
 
 		public static final String CREATE_SKILLS_INDEX = "CREATE INDEX IF NOT EXISTS character_id_index ON " + Db.SKILLS_TABLE + " (" + Db.SKILL_CHAR_ID + ")";
 		
 		private static final String DROP_SKILLS = "DROP TABLE IF EXISTS " + Db.SKILLS_TABLE;
 		
 		// table feats
 		private static final String FEATS_TABLE = "Feats";
 		private static final String FEAT_ID = "feat";
 		private static final String FEAT_CHAR_ID = "character_id";
 		
 		public static final String CREATE_FEATS_TABLE = "CREATE TABLE IF NOT EXISTS " + Db.FEATS_TABLE + " (" +
 				  Db._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
 				  Db.FEAT_ID + " SMALLINT NOT NULL ON CONFLICT FAIL," + 
 				  Db.FEAT_CHAR_ID + " INT NOT NULL ON CONFLICT FAIL CONSTRAINT character_id_fk REFERENCES " + Db.CHARACTER_TABLE + "(" + Db._ID + ") ON DELETE CASCADE ON UPDATE CASCADE," +
 				  "CONSTRAINT feat_and_char_ids UNIQUE (" + Db.FEAT_ID + ", " + Db.FEAT_CHAR_ID + "));";
 		
 		private static final String DROP_FEATS = "DROP TABLE IF EXISTS " + Db.FEATS_TABLE;
 		
 		// table items
 		private static final String ITEM_TABLE = "items";
 		private static final String ITEM_NAME = "name";
 		private static final String ITEM_DESCRIPTION = "description";
 		private static final String ITEM_WEIGHT = "weight";
 		private static final String ITEM_AMOUNT = "amount";
 		private static final String ITEM_VALUE = "itemvalue";
 		private static final String ITEM_CHAR_ID = "character_id";
 		private static final String ITEM_SLOT = "slot";
 		private static final String ITEM_EQUIPPED = "equipped";
 		
 		public static final String CREATE_ITEMS_TABLE = "CREATE TABLE IF NOT EXISTS " + Db.ITEM_TABLE + " (" + 
 				Db._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
 				Db.ITEM_CHAR_ID + " INT NOT NULL ON CONFLICT FAIL CONSTRAINT character_id_fk REFERENCES " + Db.CHARACTER_TABLE + "(" + Db._ID + ") ON DELETE CASCADE ON UPDATE CASCADE," +
 				Db.ITEM_NAME + " TEXT NOT NULL, " + 
 				Db.ITEM_DESCRIPTION + " TEXT NOT NULL DEFAULT '', " + 
 				Db.ITEM_WEIGHT + " REAL NOT NULL DEFAULT 1, " +
 				Db.ITEM_AMOUNT + " SMALLINT NOT NULL DEFAULT 1, " +
 				Db.ITEM_VALUE + " INT NOT NULL DEFAULT 0, " +  // value in copperpieces
 				Db.ITEM_SLOT + " INT NOT NULL DEFAULT 0, " +
 				Db.ITEM_EQUIPPED + " BOOL NOT NULL DEFAULT 0, " +
 				"CONSTRAINT item_and_char_ids UNIQUE (" + Db.ITEM_NAME + ", " + Db.ITEM_CHAR_ID + "));";		
 		
 		public static final String DROP_ITEMS = "DROP TABLE IF EXISTS " + Db.ITEM_TABLE;
 		
 		public static final String CREATE_ITEM_TRIGGER = "CREATE TRIGGER IF NOT EXISTS delete_item_when_zero UPDATE OF " + Db.ITEM_AMOUNT + " ON " + Db.ITEM_TABLE + " WHEN NEW." + Db.ITEM_AMOUNT + " < 1 " + 
 				"BEGIN DELETE FROM " + Db.ITEM_TABLE + " WHERE " + Db._ID + " = NEW." + Db._ID + "; END;";
 		
 		// table effects
 		private static final String ITEMEFFECT_TABLE = "item_effects";
 		private static final String ITEMEFFECT_TYPE = "type";
 		private static final String ITEMEFFECT_VALUE = "effectvalue";
 		private static final String ITEMEFFECT_ITEM_ID = "item_id";
 		
 		public static final String CREATE_ITEMEFFECTS_TABLE = "CREATE TABLE IF NOT EXISTS " + Db.ITEMEFFECT_TABLE + " (" +
 				Db._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
 				Db.ITEMEFFECT_TYPE + " SMALLINT NOT NULL," +
 				Db.ITEMEFFECT_VALUE + " SMALLINT NOT NULL," + 
 				Db.ITEMEFFECT_ITEM_ID + " INT NOT NULL ON CONFLICT FAIL CONSTRAINT items_id_fk REFERENCES " + Db.ITEM_TABLE + "(" + Db._ID + ") ON DELETE CASCADE ON UPDATE CASCADE);";
 		
 		public static final String DROP_ITEMEFFECTS = "DROP TABLE IF EXISTS " + Db.ITEMEFFECT_TABLE;
 		
 		// table weapons
 		private static final String WEAPONS_TABLE = "weapons";
 		private static final String WEAPON_DAMAGE = "damage";
 		private static final String WEAPON_CRIT_MULTIPLIER = "multiplier";
 		private static final String WEAPON_CRIT_RANGE = "critrange";
 		private static final String WEAPON_RANGE = "range";
 		private static final String WEAPON_DAMAGE_TYPE = "damagetype";
 		private static final String WEAPON_HANDEDNESS = "handedness";
 		private static final String WEAPON_ITEM_ID = "itemid";
 		
 		public static final String CREATE_WEAPONS_TABLE = "CREATE TABLE IF NOT EXISTS " + Db.WEAPONS_TABLE + " (" +
 				Db._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
 				Db.WEAPON_DAMAGE + " TEXT NOT NULL," +
 				Db.WEAPON_CRIT_MULTIPLIER + " SMALLINT NOT NULL DEFAULT 2," + 
 				Db.WEAPON_CRIT_RANGE + " SMALLINT NOT NULL DEFAULT 1," +
 				Db.WEAPON_RANGE + " SMALLINT NOT NULL DEFAULT 1," +
 				Db.WEAPON_DAMAGE_TYPE + " TEXT NOT NULL DEFAULT 'S'," +
 				Db.WEAPON_HANDEDNESS + " SMALLINT NOT NULL DEFAULT 1," +
 				Db.WEAPON_ITEM_ID + " INT NOT NULL ON CONFLICT FAIL CONSTRAINT items_id_fk REFERENCES " + Db.ITEM_TABLE + "(" + Db._ID + ") ON DELETE CASCADE ON UPDATE CASCADE," +
 				"CONSTRAINT item_ids UNIQUE (" + Db.WEAPON_ITEM_ID  + "));";
 
 		
 		public static final String DROP_WEAPONS = "DROP TABLE IF EXISTS " + Db.WEAPONS_TABLE;
 		
 		//table armor
 		private static final String ARMOR_TABLE = "armor";
 		private static final String ARMOR_AC_BONUS = "acbonus";
 		private static final String ARMOR_MAX_DEX = "maxdex";
 		private static final String ARMOR_CHECK = "armorcheck";
 		private static final String ARMOR_SPELLFAIL = "spellfail";
 		private static final String ARMOR_SPEED = "speedpenalty";
 		private static final String ARMOR_ITEM_ID = "itemid";
 		
 		public static final String CREATE_ARMOR_TABLE = "CREATE TABLE IF NOT EXISTS " + Db.ARMOR_TABLE + " (" +
 				Db._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
 				Db.ARMOR_AC_BONUS + " SMALLINT NOT NULL DEFAULT 1," +
 				Db.ARMOR_MAX_DEX + " SMALLINT NOT NULL DEFAULT 99," + 
 				Db.ARMOR_CHECK + " SMALLINT NOT NULL DEFAULT 0," +
 				Db.ARMOR_SPELLFAIL + " SMALLINT NOT NULL DEFAULT 5," +
 				Db.ARMOR_SPEED + " SMALLINT NOT NULL DEFAULT 0," +
 				Db.ARMOR_ITEM_ID + " INT NOT NULL ON CONFLICT FAIL CONSTRAINT items_id_fk REFERENCES " + Db.ITEM_TABLE + "(" + Db._ID + ") ON DELETE CASCADE ON UPDATE CASCADE," +
 				"CONSTRAINT item_ids UNIQUE (" + Db.ARMOR_ITEM_ID  + "));";
 		
 		public static final String DROP_ARMOR = "DROP TABLE IF EXISTS " + Db.ARMOR_TABLE;
 	}	
 
 	public DatabaseHelper(Context context) {
 		super(context, DATABASE, null, DATABASE_VERSION);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
 	 */
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(Db.CREATE_CHARACTER_TABLE);
 		db.execSQL(Db.CREATE_SKILLS_TABLE);
 		db.execSQL(Db.CREATE_SKILLS_INDEX);
 		db.execSQL(Db.CREATE_FEATS_TABLE);
 		db.execSQL(Db.CREATE_ITEMS_TABLE);
 		db.execSQL(Db.CREATE_ITEM_TRIGGER);
 		db.execSQL(Db.CREATE_ITEMEFFECTS_TABLE);
 		db.execSQL(Db.CREATE_WEAPONS_TABLE);
 		db.execSQL(Db.CREATE_ARMOR_TABLE);
 	}
 
 	/* (non-Javadoc)
 	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
 	 */
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 			db.execSQL(Db.DROP_ITEMS);
 			db.execSQL(Db.DROP_CHARACTERS);
 			db.execSQL(Db.DROP_SKILLS);
 			db.execSQL(Db.DROP_FEATS);
 			db.execSQL(Db.DROP_ITEMEFFECTS);
 			db.execSQL(Db.DROP_WEAPONS);
 			db.execSQL(Db.DROP_ARMOR);
 	
 			onCreate(db);
 	}
 	
 	public List<PfCharacter> getCharacters() {
 		SQLiteDatabase db = this.getReadableDatabase();
 		
 		List<PfCharacter> characters = new ArrayList<PfCharacter>();
 		
 		String[] columns = {Db._ID, Db.CHAR_NAME, Db.CHAR_CLASS, Db.CHAR_RACE, Db.CHAR_XP, Db.CHAR_MONEY, Db.CHAR_PACE, Db.CHAR_CHA, Db.CHAR_CON, Db.CHAR_DEX, Db.CHAR_INT, Db.CHAR_STR, Db.CHAR_WIS, Db.CHAR_NEWLEVELS, Db.CHAR_ASRANKS, Db.CHAR_BONUS_STAT, Db.CHAR_AVAILABLE_FEATS, Db.CHAR_HITPOINTS};
 		String orderBy = Db._ID + " ASC";
 		
 		Cursor c = db.query(Db.CHARACTER_TABLE, columns, null, null, null, null, orderBy);
 		
 		if(c.moveToFirst()) {
 			do {
 				characters.add(getNewCharacter(c, db));
 			} while(c.moveToNext());
 		}
 		
 		db.close();
 		
 		return characters;
 	}
 	
 	private PfCharacter getNewCharacter(Cursor c, SQLiteDatabase db) {
 		String name = c.getString(c.getColumnIndex(Db.CHAR_NAME));
 		PfClasses charClass = PfClasses.getPfClass(c.getInt(c.getColumnIndex(Db.CHAR_CLASS)));
 		PfRaces charRace = PfRaces.getRace(c.getInt(c.getColumnIndex(Db.CHAR_RACE)));
 		int xp = c.getInt(c.getColumnIndex(Db.CHAR_XP));
 		PfPace pace = PfPace.getPace(c.getInt(c.getColumnIndex(Db.CHAR_PACE)));
 		long id = c.getLong(c.getColumnIndex(Db._ID));
 		
 		int cha = c.getInt(c.getColumnIndex(Db.CHAR_CHA));
 		int con = c.getInt(c.getColumnIndex(Db.CHAR_CON));
 		int dex = c.getInt(c.getColumnIndex(Db.CHAR_DEX));
 		int in = c.getInt(c.getColumnIndex(Db.CHAR_INT));
 		int str = c.getInt(c.getColumnIndex(Db.CHAR_STR));
 		int wis = c.getInt(c.getColumnIndex(Db.CHAR_WIS));
 		int newLevels = c.getInt(c.getColumnIndex(Db.CHAR_NEWLEVELS));
 		int available_skill_ranks = c.getInt(c.getColumnIndex(Db.CHAR_ASRANKS));
 		int available_feats = c.getInt(c.getColumnIndex(Db.CHAR_AVAILABLE_FEATS));
 		int money = c.getInt(c.getColumnIndex(Db.CHAR_MONEY));
 		int hitpoints = c.getInt(c.getColumnIndex(Db.CHAR_HITPOINTS));
 		
 		PfCharacter newChar = new PfCharacter(PfRaces.getRace(charRace), PfClasses.getPfClass(charClass), name);
 		newChar.setNewLevels(newLevels);
 		newChar.setPace(pace);
 		newChar.setXp(xp);
 		newChar.setId(id);
 		newChar.setBaseStats(cha, con, dex, in, str, wis);
 		newChar.setAvailableSkillRanks(available_skill_ranks);
 		newChar.setAvailableFeats(available_feats);
 		newChar.setMoney(money);
 		newChar.setHitpoints(hitpoints);
 		
 		if(newChar.canChooseBonusStat()) {
 			int bonus_stat = c.getInt(c.getColumnIndex(Db.CHAR_BONUS_STAT));
 			((PfChooseBonusAttributeRace)newChar.getRace()).setBonusAttribute(PfAttributes.getAttribute(bonus_stat));
 		}
 		
 		// load skills
 		String[] columns = {Db._ID, Db.SKILL_ID, Db.SKILL_RANKS};
 		String orderBy = Db.SKILL_ID + " ASC";
 		String selection = Db.SKILL_CHAR_ID + " = ?";
 		String[] selectionArgs = {Long.toString(id)};
 		
 		// select skills that this character has
 		Cursor skills_cursor = db.query(Db.SKILLS_TABLE, columns, selection, selectionArgs, null, null, orderBy);
 		
 		// for each skill, add it to the character
 		if(skills_cursor.moveToFirst()) {
 			do {
 				
 				int skill_id = skills_cursor.getInt(skills_cursor.getColumnIndex(Db.SKILL_ID));
 				int skill_ranks = skills_cursor.getInt(skills_cursor.getColumnIndex(Db.SKILL_RANKS));
 				newChar.putSkillRanksInSkill(PfSkills.getSkill(skill_id), skill_ranks);
 			} while(skills_cursor.moveToNext());
 		}
 		
 		// feats
 		String[] feat_columns = {Db._ID, Db.FEAT_ID, Db.FEAT_CHAR_ID};
 		orderBy = Db.FEAT_ID + " ASC";
 		selection = Db.FEAT_CHAR_ID + " = ?";
 		String[] feat_SelectionArgs = {Long.toString(id)};
 		// select feats that this character has
 		Cursor feats_cursor = db.query(Db.FEATS_TABLE, feat_columns, selection, feat_SelectionArgs, null, null, orderBy);
 		
 		// for each feat, add it to the character
 		if(feats_cursor.moveToFirst()) {
 			do {
 				PfFeats feat = PfFeats.getFeat(feats_cursor.getInt(feats_cursor.getColumnIndex(Db.FEAT_ID)));
 				newChar.setAvailableFeats(newChar.getAvailableFeats() + 1);
 				newChar.gainFeat(feat);
 			} while(feats_cursor.moveToNext());
 		}
 				
 		// inventory
 		String[] item_SelectionArgs = {Long.toString(id)};
 		// select items that this character has
 		final String ITEMID = "theItemId";
 		
 		String queryString = "SELECT i." + Db._ID + " AS " + ITEMID + ", * FROM " + Db.ITEM_TABLE + " AS i" +
 				" LEFT JOIN " + Db.WEAPONS_TABLE + " AS w ON i." + Db._ID + " = w." + Db.WEAPON_ITEM_ID + 
 				" LEFT JOIN " + Db.ARMOR_TABLE + " AS a ON i." + Db._ID + " = a." + Db.ARMOR_ITEM_ID +
 				" LEFT JOIN " + Db.ITEMEFFECT_TABLE + " AS e ON i." + Db._ID + " = e." + Db.ITEMEFFECT_ITEM_ID +
 				" WHERE i." + Db.ITEM_CHAR_ID + " = ? ORDER BY " + Db.ITEM_NAME + " ASC";
 		
 		//System.out.println(queryString);
 		Cursor items_cursor = db.rawQuery(queryString , item_SelectionArgs);
 		
 		if(items_cursor.moveToFirst()) {
 			long itemId = -1;
 			Item item = null;
 			do {
 				long thisRowId = items_cursor.getLong(items_cursor.getColumnIndex(ITEMID));
 				
 				// if we encounter a new item
 				if(thisRowId != itemId) {
 					// first remember that we're now working on a new row
 					itemId = thisRowId; 
 					
 					// second, add the previous item to the character
 					// as we have finished processing rows from that one
 					if(item != null)
 						newChar.addItem(item);
 					
 					// determine the item slot of this item
 					ItemSlots slot = ItemSlots.getItemSlot(items_cursor.getInt(items_cursor.getColumnIndex(Db.ITEM_SLOT)));
 					
 					// determine the name of this item
 					String itemName = items_cursor.getString(items_cursor.getColumnIndex(Db.ITEM_NAME));
 					// make a new item and set its properties, depending on its type
 					switch(slot) {
 						case NOT_EQUIPABLE:
 							item = new Item(itemName);
 							break;
 						case WEAPON:
 							Dice damage = new Dice(items_cursor.getString(items_cursor.getColumnIndex(Db.WEAPON_DAMAGE)));
 							int multiplier = items_cursor.getInt(items_cursor.getColumnIndex(Db.WEAPON_CRIT_MULTIPLIER));
 							int critrange = items_cursor.getInt(items_cursor.getColumnIndex(Db.WEAPON_CRIT_RANGE));
 							PfHandedness handedness = PfHandedness.getHandedness(items_cursor.getInt(items_cursor.getColumnIndex(Db.WEAPON_HANDEDNESS)));
 							item = new Weapon(itemName,damage, multiplier, critrange, handedness);
 							((Weapon)item).setDamageType( items_cursor.getString(items_cursor.getColumnIndex(Db.WEAPON_DAMAGE_TYPE)));
 							((Weapon)item).setRange(items_cursor.getInt(items_cursor.getColumnIndex(Db.WEAPON_RANGE)));
 							break;
 						default: // equipable items like armor, rings, etc
 							
 							int ac = items_cursor.getInt(items_cursor.getColumnIndex(Db.ARMOR_AC_BONUS));
 							int maxDex = items_cursor.getInt(items_cursor.getColumnIndex(Db.ARMOR_MAX_DEX));
 							int armorPenalty = items_cursor.getInt(items_cursor.getColumnIndex(Db.ARMOR_CHECK));
 							item = new Wearable(itemName, ac, maxDex, armorPenalty);
 							// TODO: add spellcheck/speed penalties
 							break;
 					}
 					
 					// set generic item properties
 					item.setDescription(items_cursor.getString(items_cursor.getColumnIndex(Db.ITEM_DESCRIPTION)));
 					item.setStackSize(items_cursor.getInt(items_cursor.getColumnIndex(Db.ITEM_AMOUNT)));
 					item.setValue(items_cursor.getInt(items_cursor.getColumnIndex(Db.ITEM_VALUE)));
 					item.setWeight(items_cursor.getFloat(items_cursor.getColumnIndex(Db.ITEM_WEIGHT)));
 					item.setSlot(slot);
 					
 					int equipped = items_cursor.getInt(items_cursor.getColumnIndex(Db.ITEM_EQUIPPED));
 					if(equipped == 1) {
 						newChar.equipItem(item);
 					}
 				} 
 				// we only get here when we are processing multiple item effects for an item
 				// add magic effects to item
 				ItemEffects e = ItemEffects.getEffect(items_cursor.getInt(items_cursor.getColumnIndex(Db.ITEMEFFECT_TYPE)));
 				int effectValue = items_cursor.getInt(items_cursor.getColumnIndex(Db.ITEMEFFECT_VALUE));
 				
 				if(item != null) {
 					item.addEffect(e, effectValue);
 				}
 				
 				
 				
 			} while(items_cursor.moveToNext());
 			
 			// add the last item to the character
 			newChar.addItem(item);
 		}
 					
 		return newChar;
 	}
 	
 	public long addCharacter(PfCharacter character) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		values.put(Db.CHAR_NAME, character.getName());
 		values.put(Db.CHAR_CLASS, character.getPfClass().getPfClass().ordinal());
 		values.put(Db.CHAR_RACE, character.getRace().getRace().ordinal());
 		values.put(Db.CHAR_XP, character.getXp());
 		values.put(Db.CHAR_PACE, character.getPace().ordinal());
 		values.put(Db.CHAR_HITPOINTS, character.getMaxHitpoints().sum());
 		
 		if(character.canChooseBonusStat()) {
 			values.put(Db.CHAR_BONUS_STAT, ((PfChooseBonusAttributeRace)character.getRace()).getBonusAttribute().ordinal());
 		}
 		
 		long result = db.insert(Db.CHARACTER_TABLE, null, values);
 		db.close();
 		
 		return result;
 		
 	}
 	
 	public void deleteCharacter(PfCharacter character) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		String whereClause = Db._ID + " = ?";
 		String[] whereArgs = {Long.toString(character.getId())};
 		
 		db.delete(Db.CHARACTER_TABLE, whereClause, whereArgs);
 		db.close();
 	}
 	
 	public void updateCharacterAttributes(PfCharacter character) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(Db.CHAR_CHA, character.getBaseCharisma());
 		values.put(Db.CHAR_CON, character.getBaseConsistution());
 		values.put(Db.CHAR_DEX, character.getBaseDexterity());
 		values.put(Db.CHAR_INT, character.getBaseIntelligence());
 		values.put(Db.CHAR_STR, character.getBaseStrength());
 		values.put(Db.CHAR_WIS, character.getBaseWisdom());
 		values.put(Db.CHAR_ASRANKS, character.getAvailableSkillRanks());
 		values.put(Db.CHAR_AVAILABLE_FEATS, character.getAvailableFeats());
 		values.put(Db.CHAR_XP, character.getXp());
 		values.put(Db.CHAR_MONEY, character.getMoney());
		values.put(Db.CHAR_HITPOINTS, character.getBaseHitpoints());
 		values.put(Db.CHAR_NEWLEVELS, character.getNewLevels());
 		String whereClause = Db._ID + " = ?";
 		String[] whereArgs = {Long.toString(character.getId())};
 		
 		db.update(Db.CHARACTER_TABLE, values, whereClause, whereArgs);
 		
 		
 		Map<PfSkills, PfSkill> skills = character.getTrainedSkills();
 		
 		// for each skill this character knows
 		for(PfSkill skill : skills.values()) {
 			values.clear();
 			values.put(Db.SKILL_CHAR_ID, character.getId());
 			values.put(Db.SKILL_ID, skill.getType().ordinal());
 			values.put(Db.SKILL_RANKS, skill.getRank());
 			
 			db.replace(Db.SKILLS_TABLE, null, values);
 			
 		}
 		
 		db.close();
 	}
 	
 	public void deleteFeat(PfCharacter character, PfFeats feat) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		String whereClause = Db.FEAT_CHAR_ID + " = ? AND " + Db.FEAT_ID + " = ?";
 		String[] whereArgs = {Long.toString(character.getId()), Integer.toString(feat.ordinal())};
 		db.delete(Db.FEATS_TABLE, whereClause, whereArgs);
 		db.close();
 	}
 	
 	public void addFeat(PfCharacter character, PfFeats feat) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		
 		values.put(Db.FEAT_CHAR_ID, character.getId());
 		values.put(Db.FEAT_ID, feat.ordinal());
 		db.insert(Db.FEATS_TABLE, null, values);
 		db.close();
 	}
 
 	public void addItem(PfCharacter character, Item item) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		values.put(Db.ITEM_AMOUNT, item.getStackSize());
 		values.put(Db.ITEM_NAME, item.getName());
 		values.put(Db.ITEM_DESCRIPTION, item.getDescription());
 		values.put(Db.ITEM_VALUE, item.getValue());
 		values.put(Db.ITEM_WEIGHT, item.getWeight());
 		values.put(Db.ITEM_CHAR_ID, character.getId());
 		values.put(Db.ITEM_SLOT, item.getSlot().ordinal());
 		
 		long row = db.replace(Db.ITEM_TABLE, null, values);
 		
 		if(row > -1) { // if a new row was added
 			values = new ContentValues();
 			switch(item.getType()) {
 				case WEAPON:
 					Weapon w = (Weapon)item;
 					values.put(Db.WEAPON_DAMAGE, w.getDamage().toString());
 					values.put(Db.WEAPON_CRIT_MULTIPLIER, w.getCriticalMultiplier());
 					values.put(Db.WEAPON_CRIT_RANGE, w.getCriticalRangeNumeric());
 					values.put(Db.WEAPON_DAMAGE_TYPE, w.getDamageType());
 					values.put(Db.WEAPON_HANDEDNESS, w.getHandedness().ordinal());
 					values.put(Db.WEAPON_ITEM_ID, row);
 					values.put(Db.WEAPON_RANGE, w.getRange());
 					
 					db.replace(Db.WEAPONS_TABLE, null, values);
 					
 					break;
 				case WEARABLE:
 					Wearable a = (Wearable)item;
 					values.put(Db.ARMOR_AC_BONUS, a.getArmorClass());
 					values.put(Db.ARMOR_CHECK, a.getArmorPenalty());
 					values.put(Db.ARMOR_ITEM_ID, row);
 					values.put(Db.ARMOR_MAX_DEX, a.getMaxDexBonus());
 					values.put(Db.ARMOR_SPEED, a.getSpeedPenalty()); 
 					values.put(Db.ARMOR_SPELLFAIL, a.getSpellFailureChance()); 
 					
 					db.replace(Db.ARMOR_TABLE, null, values);
 					break;
 				default:
 					break;
 			}
 			Map<ItemEffects, Integer> effects = item.getEffects();
 			
 			if(effects.size() > 0) {
 				values = new ContentValues();
 				for(ItemEffects e : effects.keySet()) {
 					values.put(Db.ITEMEFFECT_TYPE, e.ordinal());
 					values.put(Db.ITEMEFFECT_VALUE, effects.get(e));
 					values.put(Db.ITEMEFFECT_ITEM_ID, row);
 					db.replace(Db.ITEMEFFECT_TABLE, null, values);
 				}
 			}
 		}
 		db.close();
 	}
 
 	public void removeItem(PfCharacter character, Item item, int amount) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		ContentValues values = new ContentValues();
 		
 		values.put(Db.ITEM_AMOUNT, amount);
 
 		String whereClause = Db.ITEM_CHAR_ID + " = ? AND " + Db.ITEM_NAME + " = ?";
 		String[] whereArgs = {Long.toString(character.getId()), item.getName()};
 		db.update(Db.ITEM_TABLE, values, whereClause, whereArgs);
 		
 		db.close();
 		
 	}
 	
 	public void equipItem(PfCharacter character, Item item) {
 		SQLiteDatabase db = this.getWritableDatabase();
 		
 		ContentValues values = new ContentValues();
 		
 		values.put(Db.ITEM_EQUIPPED, item.isEquiped());
 
 		String whereClause = Db.ITEM_CHAR_ID + " = ? AND " + Db.ITEM_NAME + " = ?";
 		String[] whereArgs = {Long.toString(character.getId()), item.getName()};
 		db.update(Db.ITEM_TABLE, values, whereClause, whereArgs);
 		
 		db.close();
 	}
 }
