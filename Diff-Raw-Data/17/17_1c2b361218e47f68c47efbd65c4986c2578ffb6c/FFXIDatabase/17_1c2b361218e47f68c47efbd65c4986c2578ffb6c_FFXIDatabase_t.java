 /*
    Copyright 2011-2013 kanata3249
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
 */
 package com.github.kanata3249.ffxieq.android.db;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.channels.FileChannel;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import com.github.kanata3249.ffxi.*;
 import com.github.kanata3249.ffxi.status.StatusType;
 import com.github.kanata3249.ffxieq.Atma;
 import com.github.kanata3249.ffxieq.BlueMagic;
 import com.github.kanata3249.ffxieq.BlueMagicJobTrait;
 import com.github.kanata3249.ffxieq.Combination;
 import com.github.kanata3249.ffxieq.Equipment;
 import com.github.kanata3249.ffxieq.Food;
 import com.github.kanata3249.ffxieq.JobTrait;
 import com.github.kanata3249.ffxieq.Magic;
 
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.database.Cursor;
 import android.database.sqlite.*;
 import android.os.Environment;
 
 public class FFXIDatabase extends SQLiteOpenHelper implements FFXIDAO {
 	public static final String DB_NAME = "ffxieq.db";
 	public static final String DB_NAME_ASSET = "ffxieq.zip";
 	public static String DB_PATH;
 	public static String SD_PATH;
 	public static String EXTERNAL_SD_PATH;
 
 	static final Character[][] RaceToStatusRank = {
 		// TODO This table should be read from DB.
 		// HP MP STR DEX VIT AGI INT MND CHR  0:A 1:B 2:C 3:D 4:E 5:F 6:G
 		{  'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D', 'D' }, // Hum
 		{  'C', 'E', 'B', 'E', 'C', 'F', 'F', 'B', 'D' }, // Elv
 		{  'G', 'A', 'F', 'D', 'E', 'C', 'A', 'E', 'D' }, // Tar
 		{  'D', 'D', 'E', 'A', 'E', 'B', 'D', 'E', 'F' }, // Mit
 		{  'A', 'G', 'C', 'D', 'A', 'E', 'E', 'D', 'F' }, // Gal
 	};
 
 
 	String [][] JobToRank;
 
 	Context mContext;
 	HPTable mHpTable;
 	MPTable mMpTable;
 	JobRankTable mJobRankTable;
 	StatusTable mStatusTable;
 	SkillCapTable mSkillCapTable;
 	EquipmentTable mEquipmentTable;
 	AtmaTable mAtmaTable;
 	JobTraitTable mJobTraitTable;
 	MeritPointTable mMeritPointTable;
 	FoodTable mFoodTable;
 	MagicTable mMagicTable;
 	VWAtmaTable mVWAtmaTable;
 	BlueMagicTable mBlueMagicTable;
 	StringTable mStringTable;
 	
 	boolean mUseExternalDB;
 	FFXIEQSettings mSettings;
 	String mOriginalDBName;
 
 	// Constructor
 	public FFXIDatabase(Context context, boolean useExternal, FFXIEQSettings settings) {
 		super(context, DB_NAME, null, 1);
 
 		DB_PATH = Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/databases/";
 		SD_PATH = Environment.getExternalStorageDirectory() + "/" + context.getPackageName() + "/"; 
 		EXTERNAL_SD_PATH = Environment.getExternalStorageDirectory() + "/external_sd/" + context.getPackageName() + "/"; 
 
 		mContext = context;
 		mHpTable = new HPTable();
 		mMpTable = new MPTable();
 		mJobRankTable = new JobRankTable();
 		mSkillCapTable = new SkillCapTable();
 		mStatusTable = new StatusTable();
 		mEquipmentTable = new EquipmentTable();
 		mAtmaTable = new AtmaTable();
 		mJobTraitTable = new JobTraitTable();
 		mMeritPointTable = new MeritPointTable();
 		mFoodTable = new FoodTable();
 		mMagicTable = new MagicTable();
 		mVWAtmaTable = new VWAtmaTable();
 		mBlueMagicTable = new BlueMagicTable();
 		mStringTable = new StringTable();
 		
 		mUseExternalDB = useExternal;
 
 		mSettings = settings;
 		String [] nameAndExt = DB_NAME.split("\\.");
 		mOriginalDBName = nameAndExt[0] + "_" + mSettings.getDatabaseLang() + "." + nameAndExt[1];
 		try {
			String dbpath = getDBFullPath(useExternal);
			if (checkDatabase(dbpath)) {
				copyDatabaseFromAssets(dbpath);
 			}
 		} catch (IOException e) {
 		}
 	}
 	
 	static public boolean supportsDBonSD() {
 		return android.os.Build.VERSION.SDK_INT > 7;
 	}
 
 	static public String getDBPath(boolean useExternalDB) {
 		if (DB_PATH == null)
 			return DB_NAME;
 
 		if (!supportsDBonSD())
 			return DB_NAME;
 
		return getDBFullPath(useExternalDB);
	}

	static public String getDBFullPath(boolean useExternalDB) {
		String path; 

 		if (useExternalDB) {
 			File extdir = new File(EXTERNAL_SD_PATH);
 
 			if (extdir.isDirectory()) {
 				path = EXTERNAL_SD_PATH + DB_NAME;
 			} else {
 				path = SD_PATH + DB_NAME;
 			}
 		} else {
 			path = DB_PATH + DB_NAME;
 		}
 		return path;
 	}
 
 	@Override
 	public void onCreate(SQLiteDatabase db) {
 	}
 
 	@Override
 	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
 	}
 	
 	@Override
 	public synchronized SQLiteDatabase getWritableDatabase() {
 		SQLiteException e = new SQLiteException();
 		throw e;
 	}
 	
 	public boolean checkDatabase(String pathToCheck) throws IOException {
 		if (pathToCheck.endsWith("/"))
 			pathToCheck += DB_NAME;
 
 		File in = new File(pathToCheck);
 		long lastmod;
 		
 		if (in.isFile()) {
 			lastmod = getLastModifiedFromAssets();
 			if (in.lastModified() >= lastmod) {
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public long getLastModifiedFromAssets() throws IOException {
 		InputStream in = mContext.getAssets().open(DB_NAME_ASSET, AssetManager.ACCESS_STREAMING);
 		ZipInputStream zipIn = new ZipInputStream(in);
 		ZipEntry zipEntry = zipIn.getNextEntry();
 		long result;
 		
 		result = 0;
 		while (zipEntry != null) {
 			if (zipEntry.getName().equalsIgnoreCase(mOriginalDBName)) {
 				result = zipEntry.getTime();
 			}
 			zipIn.closeEntry();
 			zipEntry = zipIn.getNextEntry();
 		}
 		
 		zipIn.close();
 		in.close();
 
 		return result;
 	}
 
 	public void copyDatabaseFromAssets(String pathToCopy) throws IOException {
 		InputStream in = mContext.getAssets().open(DB_NAME_ASSET, AssetManager.ACCESS_STREAMING);
 		ZipInputStream zipIn = new ZipInputStream(in);
 		ZipEntry zipEntry = zipIn.getNextEntry();
 		
 		String [] nameAndExt = DB_NAME.split("\\.");
 		mOriginalDBName = nameAndExt[0] + "_" + mSettings.getDatabaseLang() + "." + nameAndExt[1];
 		if (pathToCopy == null) {
			pathToCopy = getDBFullPath(mUseExternalDB);
 		}
 		if (pathToCopy.endsWith("/"))
 			pathToCopy += DB_NAME;
 		File outDir = new File(pathToCopy);
 		
 		try {
 			SQLiteDatabase db;
 
 			db = getReadableDatabase();
 			if (db != null)
 				db.close();
 			mStringTable.invalidateStringCache();
 		} catch (SQLiteException e) {
 			// ignore this
 		}
 
 		outDir.mkdir();
 		File dbfile = new File(pathToCopy);
 		dbfile.delete();
 		while (zipEntry != null) {
 			byte[] buffer = new byte[4096];
 			int size;
 
 			if (zipEntry.getName().equalsIgnoreCase(mOriginalDBName)) {
 				OutputStream out = new FileOutputStream(pathToCopy);
 				while ((size = zipIn.read(buffer, 0, buffer.length)) > -1) {
 					out.write(buffer, 0, size);
 				}
 				out.flush();
 				out.close();
 				
 				File to = new File(pathToCopy);
 				to.setLastModified(zipEntry.getTime());
 			}
 			zipIn.closeEntry();
 			zipEntry = zipIn.getNextEntry();
 		}
 		
 		zipIn.close();
 		in.close();
 	}
 
 	void copyDatabaseFromSD(String sd_path) throws IOException {
 		File outDir = new File(DB_PATH);
 
 		outDir.mkdir();
 		FileChannel channelSource = new FileInputStream(sd_path + DB_NAME).getChannel();
 		FileChannel channelTarget = new FileOutputStream(DB_PATH + DB_NAME).getChannel();
 		channelSource.transferTo(0, channelSource.size(), channelTarget);
 
 		channelSource.close();
 		channelTarget.close();
 		
 		// Copy last modified
 		File from = new File(sd_path + DB_NAME);
 		File to = new File(DB_PATH + DB_NAME);
 		to.setLastModified(from.lastModified());
 	}
 
 	void copyDatabaseToSD(String sd_path) throws IOException {
 		File outDir = new File(sd_path);
 
 		outDir.mkdir();
 		FileChannel channelSource = new FileInputStream(DB_PATH + DB_NAME).getChannel();
 		FileChannel channelTarget = new FileOutputStream(sd_path + DB_NAME).getChannel();
 		channelSource.transferTo(0, channelSource.size(), channelTarget);
 
 		channelSource.close();
 		channelTarget.close();
 		
 		// Copy last modified
 		File from = new File(DB_PATH + DB_NAME);
 		File to = new File(sd_path + DB_NAME);
 		to.setLastModified(from.lastModified());
 	}
 	
 	public boolean setUseExternalDB(boolean useExternalDB) {
 		if (useExternalDB == mUseExternalDB)
 			return true;
 		
 		try {
 			if (useExternalDB) {
 				try {
 					copyDatabaseToSD(EXTERNAL_SD_PATH);
 				} catch (IOException e) {
 					copyDatabaseToSD(SD_PATH);
 				}
 				getReadableDatabase().close();
 				mStringTable.invalidateStringCache();
 				File oldDB = new File(DB_PATH + DB_NAME);
 				oldDB.delete();
 			} else {
 				String olddbpath;
 				try {
 					copyDatabaseFromSD(EXTERNAL_SD_PATH);
 					olddbpath = EXTERNAL_SD_PATH + DB_NAME;
 				} catch (IOException e) {
 					copyDatabaseFromSD(SD_PATH);
 					olddbpath = SD_PATH + DB_NAME;
 				}
 				getReadableDatabase().close();
 				mStringTable.invalidateStringCache();
 				File oldDB = new File(olddbpath);
 				oldDB.delete();
 			}
 			mUseExternalDB = useExternalDB;
 			
 			return true;
 		} catch (IOException e) {
 			return false;
 		}
 	}
 
 	// DA methods
 	public String jobToRank(int job, StatusType type) {
 		String ret;
 		
 		if (JobToRank == null)
 			JobToRank = mJobRankTable.buildJobRankTable(getReadableDatabase());
 		ret = JobToRank[job][type.ordinal()];
 		if (ret == null) {
 			ret = "-";
 		}
 		return ret;
 	}
 	public int getHP(int race, int job, int joblevel, int subjob, int subjoblevel) {
 		return mHpTable.getHP(getReadableDatabase(), RaceToStatusRank[race][StatusType.HP.ordinal()].toString(), jobToRank(job, StatusType.HP), joblevel, jobToRank(subjob, StatusType.HP), subjoblevel);
 	}
 	public int getMP(int race, int job, int joblevel, int subjob, int subjoblevel) {
 		return mMpTable.getMP(getReadableDatabase(), RaceToStatusRank[race][StatusType.MP.ordinal()].toString(), jobToRank(job, StatusType.MP), joblevel, jobToRank(subjob, StatusType.MP), subjoblevel);
 	}
 	public int getStatus(StatusType type, int race, int job, int joblevel, int subjob, int subjoblevel) {
 		return mStatusTable.getStatus(getReadableDatabase(), RaceToStatusRank[race][type.ordinal()].toString(), jobToRank(job, type), joblevel, jobToRank(subjob, type), subjoblevel);
 	}
 	public int getSkillCap(StatusType type, int job, int joblevel, int subjob, int subjoblevel) {
 		return mSkillCapTable.getSkillCap(getReadableDatabase(), jobToRank(job, type), joblevel, jobToRank(subjob, type), subjoblevel);
 	}
 	public int getSkillCap(StatusType type, String rank, int joblevel) {
 		return mSkillCapTable.getSkillCap(getReadableDatabase(), rank, joblevel, "-", 0);
 	}
 
 	public String getString(int id) {
 		return mStringTable.getString(getReadableDatabase(), (long)id);
 	}
 	
 	public Equipment instantiateEquipment(long id, long augId) {
 		Equipment instance;
 		if (augId >= 0)
 			instance = mSettings.instantiateEquipment(this, id, augId);
 		else
 			instance = mEquipmentTable.newInstance(this, getReadableDatabase(), id, -1);
 		if (instance != null)
 			mEquipmentTable.setCombinationID(getReadableDatabase(), instance);
 		
 		return instance;
 	}
 	public Equipment findEquipment(String name, int level, String part, String weapon) {
 		Equipment instance;
 
 		instance = mEquipmentTable.findEquipment(this, getReadableDatabase(), name, level, part, weapon);
 		if (instance != null)
 			mEquipmentTable.setCombinationID(getReadableDatabase(), instance);
 		
 		return instance;
 	}
 	public Atma instantiateAtma(long id) {
 		return mAtmaTable.newInstance(this, getReadableDatabase(), id);
 	}
 	public JobTrait[] getJobTraits(int job, int level) {
 		return mJobTraitTable.getJobTraits(this, getReadableDatabase(), getString(FFXIString.JOB_DB_WAR + job), level);
 	}
 	public Combination instantiateCombination(long combiID, int numMatches) {
 		return mEquipmentTable.newCombinationInstance(this, getReadableDatabase(), combiID, numMatches);
 	}
 	public Combination searchCombination(String names[]) {
 		return mEquipmentTable.searchCombinationAndNewInstance(this, getReadableDatabase(), names);
 	}
 	public Magic instantiateMagic(long id) {
 		return mMagicTable.newInstance(this, getReadableDatabase(), id);
 	}
 	public Magic findMagic(String name) {
 		return mMagicTable.findMagic(this, getReadableDatabase(), name);
 	}
 	public Atma instantiateVWAtma(long id) {
 		return mVWAtmaTable.newInstance(this, getReadableDatabase(), id);
 	}
 	public BlueMagic instantiateBlueMagic(long id) {
 		return mBlueMagicTable.newInstance(this, getReadableDatabase(), id);
 	}
 	public Cursor getEquipmentCursor(int part, int race, int job, int level, String[] columns, String orderBy, String filter, String weaponType) {
 		return mEquipmentTable.getCursor(this, getReadableDatabase(), part, race, job, level, columns, orderBy, filter, weaponType);
 	}
 	public String []getAvailableWeaponTypes(int part, int race, int job, int level, String filter) {
 		return mEquipmentTable.getAvailableWeaponTypes(this, getReadableDatabase(), part, race, job, level, filter);
 	}
 	public Cursor getAugmentCursor(int part, int race, int job, int level, String[] columns, String orderBy, String filter, String weaponType) {
 		return mSettings.getAugmentCursor(this, part, race, job, level, columns, orderBy, filter, weaponType);
 	}
 	public String []getAvailableAugmentWeaponTypes(int part, int race, int job, int level, String filter) {
 		return mSettings.getAvailableAugmentWeaponTypes(this, part, race, job, level, filter);
 	}
 	public long saveAugment(long augId, String augment, long baseId) {
 		return mSettings.saveAugment(this, baseId, augId, augment);
 	}
 	public void deleteAugment(long augId) {
 		mSettings.deleteAugment(this, augId);
 	}
 	public Cursor getAtmaCursor(String[] columns, String orderBy, String filter) {
 		return mAtmaTable.getCursor(this, getReadableDatabase(), columns, orderBy, filter);
 	}
 	public String[] getJobSpecificMeritPointItems(int job, int category) {
 		return mMeritPointTable.getJobSpecificMeritPointItems(this, getReadableDatabase(), getString(FFXIString.JOB_DB_WAR + job), category);
 	}
 	public long[] getJobSpecificMeritPointItemIds(int job, int category) {
 		return mMeritPointTable.getJobSpecificMeritPointItemIds(this, getReadableDatabase(), getString(FFXIString.JOB_DB_WAR + job), category);
 	}
 	public JobTrait instantiateMeritPointJobTrait(long id, int level) {
 		return mMeritPointTable.newInstance(this, getReadableDatabase(), id, level);
 	}
 	public Food instantiateFood(long id) {
 		return mFoodTable.newInstance(this, getReadableDatabase(), id);
 	}
 	public Cursor getFoodsCursor(String[] columns, String orderBy, String filter, String foodType) {
 		return mFoodTable.getCursor(this, getReadableDatabase(), columns, orderBy, filter, foodType);
 	}
 	public String []getAvailableFoodTypes(String filter) {
 		return mFoodTable.getAvailableFoodTypes(this, getReadableDatabase(), filter);
 	}
 	public Cursor getMagicCursor(long subid, String[] columns, String orderBy, String weaponType) {
 		return mMagicTable.getCursor(this, getReadableDatabase(), subid, columns, orderBy, weaponType);
 	}
 	public Cursor getVWAtmaCursor(long subID, String[] columns, String orderBy, String filter) {
 		return mVWAtmaTable.getCursor(this, getReadableDatabase(), subID, columns, orderBy, filter);
 	}
 	public Cursor getBlueMagicCursor(String[] columns, String orderBy) {
 		return mBlueMagicTable.getCursor(this, getReadableDatabase(), columns, orderBy);
 	}
 	public BlueMagicJobTrait[] getBlueMagicJobTraits() {
 		return mBlueMagicTable.getJobTraits(this, getReadableDatabase());
 	}
 }
