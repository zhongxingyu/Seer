 package com.vibhinna.binoy;
 
 import java.io.File;
 
 import android.provider.BaseColumns;
 
 public class Constants {
 	public static final String CMD_DD = "dd if=/dev/zero of=";
 	public static final String CMD_MKE2FS_EXT3 = "/data/data/com.vibhinna.binoy/bin/mke2fs -F -t ext3 ";
 	public static final String CACHE_IMG = "/cache.img";
 	public static final String DATA_IMG = "/data.img";
 	public static final String SYSTEM_IMG = "/system.img";
 	public static final String LOG_FO_1 = "Error Code (Formating ";
 	public static final String VALUE_DET = "value";
 	public static final String NAME_DET = "name";
 	public static final String DESC_DET = "desc";
 	public static final String STATUS_DET = "status";
 	public static final String PATH_DET = "path";
 	public static final String FOLDER_DET = "folder";
 	public static final String EMPTY = "";
 	public static final String CACHE_EXT3 = "/cache.img to ext3 ) :";
 	public static final String DATA_EXT3 = "/data.img to ext3 ) :";
 	public static final String SYSTEM_EXT3 = "/system.img to ext3 ) :";
 	public static final String CORR_S = "corrupted";
 	public static final String N_A = "N/A";
 	public static final String SD_PATH = "/mnt/sdcard";
 	public static final String MULTI_BOOT_PATH = "/mnt/sdcard/multiboot/";
 	public static final String BINARY_PATH = "/data/data/com.vibhinna.binoy/bin";
 	public static final File BINARY_FOLDER = new File(BINARY_PATH);
 	public static final int CACHE_SIZE = 10;
 	public static final int DATA_SIZE = 200;
 	public static final int MAX_IMG_SIZE = 100;
 	public static final int MIN_IMG_SIZE = 1;
 	public static final int SYSTEM_SIZE = 350;
 	public static final String[] allColumns = { BaseColumns._ID,
 		DataBaseHelper.VIRTUAL_SYSTEM_COLUMN_NAME,
 		DataBaseHelper.VIRTUAL_SYSTEM_COLUMN_PATH,
 		DataBaseHelper.VIRTUAL_SYSTEM_COLUMN_TYPE,
 		DataBaseHelper.VIRTUAL_SYSTEM_COLUMN_DESCRIPTION };
 	public static final String[] MATRIX_COLUMN_NAMES = { "_id", "name", "desc", "family", "folder",
 			"status", "vdstatus", "path" };
	public static final File MBM_ROOT = new File("/mnt/sdcard/multiboot");
 	public static final String CMD_TUNE2FS = "/data/data/com.vibhinna.binoy/bin/tune2fs -l ";
 }
