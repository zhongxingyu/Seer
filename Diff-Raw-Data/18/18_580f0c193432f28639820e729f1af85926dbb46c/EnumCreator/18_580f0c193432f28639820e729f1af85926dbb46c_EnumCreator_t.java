 package com.eiryu.oss;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.io.FileUtils;
 
 /**
  * create enum class with fields
  * 
  * @author eiryu
  * 
  */
 public class EnumCreator {
 
 	private static final String TAB = "\t";
 
	public static void create(String className, String filePath)
			throws IOException {
 
 		File file = new File(filePath);
 		if (false == file.exists()) {
 			System.out.println("File Not Found");
 			System.exit(-1);
 		}

 		boolean readHeader = false;
 		String[] headers = null;
 
 		List<String> names = new ArrayList<>();
 
 		for (Object o : FileUtils.readLines(file, "utf-8")) {
 			String row = (String) o;
 
 			// read header at first time
 			if (!readHeader) {
 				headers = row.split(TAB);
 				readHeader = true;
 
 				continue;
 			}
 			String[] columns = row.split(TAB);
 
 			StringBuilder sb = new StringBuilder();
 			for (int i = 1; i < columns.length; i++) {
 				String column = columns[i];
 				sb.append(column).append(",");
 			}
 			removeLastComma(sb);
 			names.add(String.format("%s(%s),", columns[0], sb));
 		}
 
 		List<String> fields = new ArrayList<>();
 		List<String> constructorInitializeProcesses = new ArrayList<>();
 
 		StringBuilder constructorArgs = new StringBuilder();
 		for (int i = 1; i < headers.length; i++) {
 			String paramInfo = headers[i];
 			String paramName = paramInfo.split(":")[0];
 			String type = paramInfo.split(":")[1];
 
 			fields.add(String.format("private %s %s;", type, paramName));
 			constructorArgs.append(String.format("%s %s,", type, paramName));
 			constructorInitializeProcesses.add(String.format("this.%s = %s;",
 					paramName, paramName));
 		}
 		removeLastComma(constructorArgs);
 
 		// output enum declaration
 		System.out.format("public enum %s {\n", className);

 		// output names
 		output(names);
 		System.out.println(";");
 
 		// output fields
 		output(fields);
 
 		// output constructor
 		System.out.format("private %s (%s) {\n", className, constructorArgs);
 		output(constructorInitializeProcesses);
 		System.out.println("}");

 		System.out.println("}");
 	}
 
 	private static void output(List<String> elements) {
		for (String ele : elements) {
			System.out.println(ele);
 		}
 	}
 
 	/**
 	 * remove last comma
 	 * 
 	 * @param sb
 	 * @return
 	 */
 	private static StringBuilder removeLastComma(StringBuilder sb) {
 		return sb.deleteCharAt(sb.length() - 1);
 	}
 }
