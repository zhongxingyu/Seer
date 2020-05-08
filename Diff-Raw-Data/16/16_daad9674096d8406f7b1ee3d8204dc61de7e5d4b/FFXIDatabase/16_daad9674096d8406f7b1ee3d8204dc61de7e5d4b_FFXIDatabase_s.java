 /*
    Copyright 2011 kanata3249
 
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
 import com.github.kanata3249.ffxieq.Combination;
 import com.github.kanata3249.ffxieq.Equipment;
 import com.github.kanata3249.ffxieq.JobTrait;
 
 import android.content.Context;
 import android.content.res.AssetManager;
 import android.database.Cursor;
 import android.database.sqlite.*;
 import android.os.Environment;
 
 public class FFXIDatabase extends SQLiteOpenHelper implements FFXIDAO {
 	public static final String DB_NAME = "ffxieq";
 	public static final String DB_NAME_ASSET = "db.zip";
 	public static String DB_PATH;
 	public static String SD_PATH;
 
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
 	StringTable mStringTable;
 	
 	boolean mUseExternalDB;
 	String mDBPath;
 
 	// Constructor
 	public FFXIDatabase(Context context, boolean useExternal) {
 		super(context, DB_NAME, null, 1);
 
 		DB_PATH = Environment.getDataDirectory() + "/data/" + context.getPackageName() + "/databases/";
 		SD_PATH = Environment.getExternalStorageDirectory() + "/" + context.getPackageName() + "/"; 
 
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
 		mStringTable = new StringTable();
 		
 		if (useExternal)
 			mDBPath = SD_PATH;
 		else
 			mDBPath = DB_PATH;
 
 		if (checkDatabase(mDBPath)) {
 			try {
 				copyDatabaseFromAssets(mDBPath);
 			} catch (IOException e) {
 			}
 		}
 	}
 
 	static public String getDBPath(boolean useExternalDB) {
 		String path; 
 		if (DB_PATH == null)
 			return DB_NAME;
 
 		if (useExternalDB) {
 			path = SD_PATH + DB_NAME;
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
 	
 	public boolean checkDatabase(String pathToCheck) {
 		File in = new File(pathToCheck + DB_NAME);
 		
 		if (in.isFile())
 			return false;
 		return true;
 	}
 	
 	public void copyDatabaseFromAssets(String pathToCopy) throws IOException {
 		InputStream in = mContext.getAssets().open(DB_NAME_ASSET, AssetManager.ACCESS_STREAMING);
 		ZipInputStream zipIn = new ZipInputStream(in);
 		ZipEntry zipEntry = zipIn.getNextEntry();
 		File outDir = new File(pathToCopy);
 		
 		SQLiteDatabase db;
 
 		db = getReadableDatabase();
 		if (db != null)
 			db.close();
 
 		outDir.mkdir();
 		while (zipEntry != null) {
 			byte[] buffer = new byte[4096];
 			int size;
 			
 			OutputStream out = new FileOutputStream(pathToCopy + zipEntry.getName());
 			while ((size = zipIn.read(buffer, 0, buffer.length)) > -1) {
 				out.write(buffer, 0, size);
 			}
 			out.flush();
 			out.close();
 			zipIn.closeEntry();
 			zipEntry = zipIn.getNextEntry();
 		}
 		
 		zipIn.close();
 		in.close();
 	}
 
 	public void copyDatabaseFromSD() throws IOException {
 		File outDir = new File(DB_PATH);
 		SQLiteDatabase db;
 
 		db = getReadableDatabase();
 		if (db != null)
 			db.close();
 
 		outDir.mkdir();
 		FileChannel channelSource = new FileInputStream(SD_PATH + DB_NAME).getChannel();
 		FileChannel channelTarget = new FileOutputStream(DB_PATH + DB_NAME).getChannel();
 		channelSource.transferTo(0, channelSource.size(), channelTarget);
 
 		channelSource.close();
 		channelTarget.close();
 		
 	}
 
 	public void copyDatabaseToSD() throws IOException {
 		File outDir = new File(SD_PATH);
 
 		outDir.mkdir();
 		FileChannel channelSource = new FileInputStream(DB_PATH + DB_NAME).getChannel();
 		FileChannel channelTarget = new FileOutputStream(SD_PATH + DB_NAME).getChannel();
 		channelSource.transferTo(0, channelSource.size(), channelTarget);
 
 		channelSource.close();
 		channelTarget.close();
 	}
 	
 	public boolean setUseExternalDB(boolean useExternalDB) {
 		if (useExternalDB == mUseExternalDB)
 			return true;
 		
 		try {
 			if (useExternalDB) {
 				copyDatabaseToSD();
 				getReadableDatabase().close();
 				File oldDB = new File(DB_PATH + DB_NAME);
 				oldDB.delete();
 			} else {
 				copyDatabaseFromSD();
 				getReadableDatabase().close();
 				File oldDB = new File(SD_PATH + DB_NAME);
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
 
 	public String getString(int id) {
 		return mStringTable.getString(getReadableDatabase(), (long)id);
 	}
 	
 	public Equipment instantiateEquipment(long id) {
 		return mEquipmentTable.newInstance(this, getReadableDatabase(), id, -1);
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
 
 	public Cursor getEquipmentCursor(int part, int race, int job, int level, String[] columns, String orderBy, String filter) {
 		return mEquipmentTable.getCursor(this, getReadableDatabase(), part, race, job, level, columns, orderBy, filter);
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
 }
