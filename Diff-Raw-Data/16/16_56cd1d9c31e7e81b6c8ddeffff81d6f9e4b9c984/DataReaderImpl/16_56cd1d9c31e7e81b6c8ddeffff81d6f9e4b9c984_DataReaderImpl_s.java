 package com.mot.multicore.expenses.data;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Scanner;
 import java.util.Set;
 
 import com.mot.multicore.expenses.MonthBillData;
 import com.mot.multicore.expenses.OperationEntry;
 import com.mot.multicore.expenses.OperationType;
 
 import android.text.Html;
 import android.text.Spanned;
 import android.util.Log;
 
 public class DataReaderImpl implements DataReader {
 
	private static String directorySdcard = "/sdcard/";
	private static String directoryName = "mwydatki/";
	private static String fullPathToDirectory = directorySdcard + directoryName;
 	private static String encoding = "iso-8859-2";
 
 	private static final String TAG = "DataReader";
 
 	private static Map<String, Spanned> mapFileData = null;
 
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.developand.mwydatki.DataReader#readData()
 	 */
 	public Map<String, Spanned> readData(boolean allowCache) throws IOException {
 
 		Log.v(TAG, "starting reading file");
 
 		if (allowCache && null != mapFileData)
 			return mapFileData;
 
 		mapFileData = new HashMap<String, Spanned>();
 
 		File dir = new File(fullPathToDirectory);
 		
 		// create dir if not created before
 		dir.mkdir();
 
 		// set of file
 		List<String> files = new ArrayList<String>();
 		files = Arrays.asList(dir.list());
 
 		Log.v(TAG, "found files #:" + files.size());
 
 		for (String file : files) {
 			// File f = new File(directory, file);
 			Log.v(TAG, "found file:" + file);
			Spanned parsedData = Html.fromHtml(read(fullPathToDirectory + file));
 			mapFileData.put(file, parsedData);
 			Log.v(TAG, "parsed this file");
 		}
 
 		Log.v(TAG, "found spanned lists #:" + mapFileData.size());
 
 		return mapFileData;
 
 	}
 
 	private String read(String fileName) throws IOException {
 
 		StringBuilder text = new StringBuilder();
 		String NL = System.getProperty("line.separator");
 		Scanner scanner = new Scanner(new FileInputStream(fileName), encoding);
 		try {
 			while (scanner.hasNextLine()) {
 				text.append(scanner.nextLine() + NL);
 			}
 		} finally {
 			scanner.close();
 		}
 		return text.toString();
 	}
 
 	public Set<String> getFiles() {
 		if (mapFileData != null)
 			return mapFileData.keySet();
 		return null;
 	}
 
 	public List<OperationEntry> getOperations(String file) {
 		MonthBillData mb = MonthBillData.getInstance();
 		mb.parseSource(mapFileData.get(file).toString());
 		return mb.getOperations();
 	}
 
 	public List<String> getOperationsFromFileByType(String file,
 			OperationType type) {
 		MonthBillData mb = MonthBillData.getInstance();
 		mb.parseSource(mapFileData.get(file).toString());
 		List<String> opsString = new ArrayList<String>();
 		for (OperationEntry op : mb.getOperations()) {
 			switch (type) {
 			case ALL:
 				opsString.add(op.toString());
 				break;
 			case MINUS:
 				if (op.getKwotaOperacji() < 0)
 					opsString.add(op.toString());
 			case PLUS:
 				if (op.getKwotaOperacji() >= 0)
 					opsString.add(op.toString());
 			}
 		}
 		return opsString;
 	}
 
 	public List<String> getOperationsStringByIndex(Integer index,
 			OperationType type) {
 
 		if (getFiles().size() <= index)
 			return null;
 
 		return getOperationsFromFileByType(
 				getFiles().toArray()[index].toString(), type);
 	}
 
 	// used in attachment reading
 	public List<String> getOperationsFromStringByType(String source,
 			OperationType type) {
 		MonthBillData mb = MonthBillData.getInstance();
 		mb.parseSource(source);
 		List<String> opsString = new ArrayList<String>();
 		for (OperationEntry op : mb.getOperations()) {
 			switch (type) {
 			case ALL:
 				opsString.add(op.toString());
 				break;
 			case MINUS:
 				if (op.getKwotaOperacji() < 0)
 					opsString.add(op.toString());
 			case PLUS:
 				if (op.getKwotaOperacji() >= 0)
 					opsString.add(op.toString());
 			}
 		}
 		return opsString;
 	}
 
 	public List<OperationEntry> getOperationsByIndex(Integer index,
 			OperationType op) {
 		MonthBillData mb = MonthBillData.getInstance();
 		
 		// optimize if index == 0
 		if (0 == index && (mb.getOperations() != null)) {
 			return mb.getOperationsByType(op);
 		}
 		
 		// handle empty directory
 		if (mapFileData.size() == 0)
 			return new ArrayList<OperationEntry>();
 
 		mb.parseSource(mapFileData.values().toArray()[index].toString());
 		return mb.getOperationsByType(op);
 	}
 
 }
