 package matdue.raidstatus.data.database;
 
 import java.math.BigDecimal;
 import java.text.Collator;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Date;
 
 import matdue.raidstatus.data.Player;
 import matdue.raidstatus.data.Raid;
 import matdue.raidstatus.data.RaidClass;
 import matdue.raidstatus.data.RaidMember;
 import android.content.ContentValues;
 import android.content.Context;
 import android.database.Cursor;
 import android.database.DatabaseUtils.InsertHelper;
 import android.database.sqlite.SQLiteDatabase;
 import android.database.sqlite.SQLiteOpenHelper;
 import android.util.Log;
 
 public class RaidDatabase extends SQLiteOpenHelper {
 
 	private static final String DATABASE_NAME = "raid.db";
 	private static final int DATABASE_VERSION = 1;
 	
 	public RaidDatabase(Context context) {
 		super(context, DATABASE_NAME, null, DATABASE_VERSION);
 	}
 	
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 		db.execSQL(PlayerTable.SQL_CREATE);
 		db.execSQL(RaidTable.SQL_CREATE);
 		db.execSQL(RaidClassTable.SQL_CREATE);
 		db.execSQL(RaidMemberTable.SQL_CREATE);
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 		db.execSQL(RaidMemberTable.SQL_DROP);
 		db.execSQL(RaidClassTable.SQL_DROP);
 		db.execSQL(RaidTable.SQL_DROP);
 		db.execSQL(PlayerTable.SQL_DROP);
 		onCreate(db);
 	}
 	
 	public void deleteAllData() {
 		SQLiteDatabase db = getWritableDatabase();
 		try {
 			db.execSQL(RaidMemberTable.STMT_CLEAR);
 			db.execSQL(RaidClassTable.STMT_CLEAR);
 			db.execSQL(RaidTable.STMT_CLEAR);
 			db.execSQL(PlayerTable.STMT_CLEAR);
 		} finally {
 			db.close();
 		}
 	}
 
 	/**
 	 * Stores all players in database and sets property "_id" to
 	 * primary key of that entity in database.
 	 * 
 	 * @param players Collection of Player entity objects
 	 */
 	public void insertPlayers(Collection<Player> players) {
 		SQLiteDatabase db = getWritableDatabase();
 		InsertHelper insertHelper = new InsertHelper(db, PlayerTable.TABLE_NAME);
 		try {
 			db.beginTransaction();
 			for (Player player : players) {
 				ContentValues values = new ContentValues();
 				values.put(PlayerColumns.NAME, player.getName());
 				values.put(PlayerColumns.CLASSNAME, player.getClassName());
 				values.put(PlayerColumns.CURRENTDKP, player.getCurrentDkp().doubleValue());
 				player.set_id(insertHelper.insert(values));
 			}
 			db.setTransactionSuccessful();
 			db.endTransaction();
 		} finally {
 			insertHelper.close();
 			db.close();
 		}
 	}
 	
 	public void insertRaids(Collection<Raid> raids) {
 		SQLiteDatabase db = getWritableDatabase();
 		InsertHelper insertHelper = new InsertHelper(db, RaidTable.TABLE_NAME);
 		try {
 			db.beginTransaction();
 			for (Raid raid : raids) {
 				ContentValues values = new ContentValues();
 				values.put(RaidColumns.NAME, raid.getName());
 				values.put(RaidColumns.ICON, raid.getIcon());
 				values.put(RaidColumns.NOTE, raid.getNote());
 				values.put(RaidColumns.START, raid.getStart().getTime() / 1000);
 				values.put(RaidColumns.FINISH, raid.getFinish().getTime() / 1000);
 				values.put(RaidColumns.INVITE, raid.getInvite().getTime() / 1000);
 				values.put(RaidColumns.SUBSCRIPTION, raid.getSubscription().getTime() / 1000);
 				values.put(RaidColumns.ATTENDEES, raid.getAttendees());
 				raid.set_id(insertHelper.insert(values));
 
 				insertRaidClasses(db, raid.getRaidClasses(), raid.get_id());
 				insertRaidMembers(db, raid.getRaidMembers(), raid.get_id());
 			}
 			db.setTransactionSuccessful();
 			db.endTransaction();
 		} finally {
 			insertHelper.close();
 			db.close();
 		}
 	}
 	
 	public void insertRaidClasses(SQLiteDatabase db, Collection<RaidClass> raidClasses, long raidID) {
 		InsertHelper insertHelper = new InsertHelper(db, RaidClassTable.TABLE_NAME);
 		try {
 			for (RaidClass raidClass : raidClasses) {
 				ContentValues values = new ContentValues();
 				values.put(RaidClassColumns.RAIDID, raidID);
 				values.put(RaidClassColumns.CLASSNAME, raidClass.getClassName());
 				values.put(RaidClassColumns.COUNT, raidClass.getCount());
 				raidClass.set_id(insertHelper.insert(values));
 			}
 		} finally {
 			insertHelper.close();
 		}
 	}
 	
 	public void insertRaidMembers(SQLiteDatabase db, Collection<RaidMember> raidMembers, long raidID) {
 		InsertHelper insertHelper = new InsertHelper(db, RaidMemberTable.TABLE_NAME);
 		try {
 			for (RaidMember raidMember : raidMembers) {
 				ContentValues values = new ContentValues();
 				values.put(RaidMemberColumns.SUBSCRIBED, raidMember.getSubscribed());
 				values.put(RaidMemberColumns.PLAYERID, raidMember.getPlayer().get_id());
 				values.put(RaidMemberColumns.RAIDID, raidID);
 				values.put(RaidMemberColumns.NOTE, raidMember.getNote());
 				values.put(RaidMemberColumns.ROLE, raidMember.getRole());
 				raidMember.set_id(insertHelper.insert(values));
 			}
 		} finally {
 			insertHelper.close();
 		}
 	}
 	
 	public Raid loadRaid(int idx) {
 		SQLiteDatabase db = getReadableDatabase();
 		
 		// Load raid
 		Raid raid = null;
 		Cursor cursor = db.query(RaidTable.TABLE_NAME, 
 				new String[] { 
 					RaidColumns.ID,
 					RaidColumns.NAME, 
 					RaidColumns.START,
 					RaidColumns.ICON,
 					RaidColumns.ATTENDEES}, 
 				null, null, null, null, 
 				RaidColumns.START,  // order by 
 				Integer.toString(idx) + ",1");  // offset,limit
 		if (cursor.moveToNext()) {
 			raid = new Raid();
 			raid.set_id(cursor.getLong(0));
 			raid.setName(cursor.getString(1));
 			raid.setStart(new Date(1000L * cursor.getInt(2)));
 			raid.setIcon(cursor.getString(3));
 			raid.setAttendees(cursor.getInt(4));
 		}
 		cursor.close();
 		
 		// Load its members
 		if (raid != null) {
 			raid.setRaidMembers(new ArrayList<RaidMember>());
 			String query = "SELECT p." + PlayerColumns.NAME + ", p." + PlayerColumns.CLASSNAME + ", p." + PlayerColumns.CURRENTDKP +
 				", m." + RaidMemberColumns.SUBSCRIBED + ", m." + RaidMemberColumns.NOTE + ", m." + RaidMemberColumns.ROLE +
 				" FROM " + RaidMemberTable.TABLE_NAME + " m, " + PlayerTable.TABLE_NAME + " p" +
 				" WHERE m." + RaidMemberColumns.RAIDID + " = ?" +
 				" AND m." + RaidMemberColumns.PLAYERID + " = p." + PlayerColumns.ID;
 			cursor = db.rawQuery(query, new String[] { Long.toString(raid.get_id()) });
 			while (cursor.moveToNext()) {
 				Player player = new Player();
 				player.setName(cursor.getString(0));
 				player.setClassName(cursor.getString(1));
 				player.setCurrentDkp(new BigDecimal(cursor.getString(2)));
 
 				RaidMember member = new RaidMember();
 				member.setPlayer(player);
 				member.setSubscribed(cursor.getInt(3));
 				member.setNote(cursor.getString(4));
 				member.setRole(cursor.getString(5));
 				
 				raid.getRaidMembers().add(member);
 			}
 			cursor.close();
 		}
 		
 		// Load its classes
 		if (raid != null) {
 			raid.setRaidClasses(new ArrayList<RaidClass>());
 			cursor = db.query(RaidClassTable.TABLE_NAME, 
 					new String[] { RaidClassColumns.CLASSNAME, RaidClassColumns.COUNT }, 
 					RaidClassColumns.RAIDID + " = ?", 
 					new String[] { Long.toString(raid.get_id()) }, 
 					null, null, null);
 			while (cursor.moveToNext()) {
 				RaidClass raidClass = new RaidClass();
 				raidClass.setClassName(cursor.getString(0));
 				raidClass.setCount(cursor.getInt(1));
 				
 				raid.getRaidClasses().add(raidClass);
 			}
 			cursor.close();
 		}
 		
 		db.close();
 		
 		return raid;
 	}
 	
 	public String[] loadPlayerNames() {
 		String[] result = new String[0];
 		
 		// Load data
 		SQLiteDatabase db = null;
 		try {
 			db = getReadableDatabase();
 			Cursor cursor = db.query(PlayerTable.TABLE_NAME, new String[] { PlayerColumns.NAME }, null, null, null, null, null);
 			result = new String[cursor.getCount()];
 			while (cursor.moveToNext()) {
 				result[cursor.getPosition()] = cursor.getString(0);
 			}
 			cursor.close();
 		} catch (Exception e) {
 			Log.e("DKPRaidstatus", "Error loading player names", e);
 		} finally {
 			if (db != null) {
 				db.close();
 			}
 		}
 		
 		// Sort names
 		final Collator col = Collator.getInstance();
 		Arrays.sort(result, new Comparator<String>() {
 			public int compare(String string1, String string2) {
 				return col.compare(string1, string2);
 			}}
 		);
 		
 		return result;
 	}
 }
