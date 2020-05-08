 /*
  Copyright 2010 Bizosys Technologies Limited
 *
 * Licensed to the Bizosys Technologies Limited (Bizosys) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The Bizosys licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.bizosys.hsearch.treetable.compiler;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.bizosys.hsearch.treetable.CellGenerator;
 import com.bizosys.hsearch.treetable.compiler.Schema.Column;
 import com.bizosys.hsearch.treetable.compiler.Schema.Field;
 import com.google.gson.Gson;
 
 public class HSearchCompiler {
 	public static void main(String[] args) throws Exception{
 		
 		if ( args.length != 2) {
 			System.err.println("\n\n\nUsage : java -classpath gson-2.2.2.jar;hsearch-core.jar com.bizosys.hsearch.treetable.compiler.HSearchCompiler schema.json output_path\n\n\n");
 			return;
 		}
 		
 		Gson gson = new Gson();
 		String schemaStr = fileToString(args[0]);
 		
 		schemaStr = schemaStr.replace("\"indexes\": \"schema-offset\"", 
 				ReadFileFromJar.getTextFileContent(
 					"/com/bizosys/hsearch/treetable/compiler/schema-offset.json" ) );
 		
 		schemaStr = schemaStr.replace("\"indexes\": \"schema-frequency\"", 
 				ReadFileFromJar.getTextFileContent(
 					"/com/bizosys/hsearch/treetable/compiler/schema-frequency.json" ) );
 
 		schemaStr = schemaStr.replace("\"indexes\": \"schema-positions\"", 
 				ReadFileFromJar.getTextFileContent(
 					"/com/bizosys/hsearch/treetable/compiler/schema-positions.json" ) );
 
 		System.out.println(schemaStr);
 		Schema newSchema = gson.fromJson(schemaStr, Schema.class);
 		String path = args[1] + "/" + newSchema.module.replace(".", "/");
 		
 		File file = new File(path);
 		if ( ! file.mkdirs() ) {
 			System.err.println("Not able to create directory : " + file.getAbsolutePath());
 			return;
 		}
 
 		File file1 = new File(path + "/donotmodify");
 		if ( ! file1.mkdirs() ) {
 			System.err.println("Not able to create directory : " + file1.getAbsolutePath());
 			return;
 		}
 		
 
 		FileWriterUtil.downloadToFile(generateClient(newSchema.module).getBytes(), 
 				new File(path + "/Client.java") );
 		
 		FileWriterUtil.downloadToFile(generateFilter(newSchema).getBytes(), 
 				new File(path + "/Filter.java") );
 
 		FileWriterUtil.downloadToFile(generateReducer(newSchema.module).getBytes(), 
 				new File(path + "/Reducer.java") );
 
 		FileWriterUtil.downloadToFile(generateWebservice(newSchema.module).getBytes(), 
 				new File(path + "/Webservice.java") );
 
 		FileWriterUtil.downloadToFile(generateHSearchTableCombinerImpl(newSchema).getBytes(), 
 				new File(path + "/donotmodify/HSearchTableCombinerImpl.java") );
 		
 		FileWriterUtil.downloadToFile(generateHSearchTableMultiQueryProcessorImpl(newSchema.module).getBytes(), 
 				new File(path + "/donotmodify/HSearchTableMultiQueryProcessorImpl.java") );
 
 		FileWriterUtil.downloadToFile(generateHBaseTableSchema(newSchema).getBytes(), 
 				new File(path + "/donotmodify/HBaseTableSchema.java") );
 		
 		for (Column col : newSchema.columns) {
 			List<Field> allFields = new ArrayList<Schema.Field>();
 			allFields.addAll(col.indexes);
 			allFields.add (col.key );
 			allFields.add(col.value);
 			
 			FileWriterUtil.downloadToFile(generateMapper(newSchema.module, col.name , col.key, col.value, allFields).getBytes(), 
 					new File(path + "/Mapper" + col.name + ".java") );
 
 			FileWriterUtil.downloadToFile(generatePluginBase(newSchema.module, col.name , col.key, col.value, allFields).getBytes(), 
 					new File(path + "/donotmodify/Plugin" + col.name + "Base.java") );
 
 			//check if the number of columns is 2 or more
 			if(allFields.size() > 2){
 				FileWriterUtil.downloadToFile(generateHSearchTable(newSchema.module, col.indexType , col.key, col.value, col.name, allFields).getBytes(), 
 						new File(path + "/donotmodify/HSearchTable" + col.name + ".java") );				
 			}			
 			else{
 				FileWriterUtil.downloadToFile(generateHSearchTableCell2(newSchema.module, col.key, col.value, col.name, allFields).getBytes(), 
 						new File(path + "/donotmodify/HSearchTable" + col.name + ".java") );								
 			}
 
 		}
 	}
 
 	public static String generateHSearchTable(String module, String indexType, Field fldKey, Field fldValue,
 			String colName, List<Field> allFields) throws Exception {
 		String template = ReadFileFromJar.getTextFileContent(
 				"/com/bizosys/hsearch/treetable/compiler/templates/HSearchTable.tmp");
 		template = template.replace("--PACKAGE--", module);
 		
 		if (  null != indexType) {
 			template = template.replace("implements IHSearchTable", "implements " + indexType);
 		}
 		
 		template = CodePartGenerator.setKeyComparision(template, fldKey.datatype);
 		template = CodePartGenerator.setAbsValue(template, fldKey.datatype);
 		
 		//Required Cells
 		String reqCells = "";
 		for ( int i=2; i<=allFields.size(); i++) {
 			reqCells = reqCells + "import com.bizosys.hsearch.treetable.Cell" + i + ";\n";
 		}
 		template = template.replace("--IMPORT-CELLS--", reqCells);
 		template = 	template.replace("--COLUMN-NAME--", colName );
 
 		CodePartGenerator cg = new CodePartGenerator(allFields);
 
 		Integer CELLMAX = allFields.size();
 		Integer CELLMAX_MINUS_1 = CELLMAX - 1;
 		String CELLMAX_MINUS_SIGN = CodePartGenerator.cellSignatures.get(CELLMAX_MINUS_1.toString()) ;
 		String dataType = allFields.get(0).datatype.equals("Short") ? "Integer" : allFields.get(0).datatype;
 		String CELLMAX_SIGN =  CELLMAX_MINUS_SIGN.replace(  (CELLMAX_MINUS_1 + "<") , (CELLMAX + "<" + dataType + ","));
 		CELLMAX_SIGN =  CELLMAX_SIGN.replaceFirst(dataType + ", ", ""); 
 		
 		CodePartGenerator.cellSignatures.put(CELLMAX.toString(), CELLMAX_SIGN);
 		
 		String cellClass = "";
 		for ( int i=1; i < (CELLMAX - 2) ; i++) {
 			cellClass = cellClass + cg.createCellClass(allFields, i ) ; 
 		}
 		template = template.replace("--CELL-CLASS--", cellClass);
 		
 		String cellMinus1DataType = allFields.get(CELLMAX_MINUS_1).datatype;
 		if ( "Short".equals(cellMinus1DataType)) cellMinus1DataType = "Integer";
 		template = template.replace("--CELLMAX_MINUS_1-SIGN--", cellMinus1DataType);
 		
 		String cellMinus2DataType = allFields.get(CELLMAX - 2).datatype;
 		if ( "Short".equals(cellMinus2DataType)) cellMinus2DataType = "Integer";
 		template = template.replace("--CELLMAX_MINUS_2-SIGN--", cellMinus2DataType);
 		
 		String treeNodes = "";
 		int count = 0;
 		String datatype = "";
 		for ( int i=CELLMAX_MINUS_1; i>=2; i--) {
 			datatype = CodePartGenerator.getPrimitive(allFields.get(count).datatype);
 			treeNodes = treeNodes + "public "+datatype+" cell" + i + "Key;\n\t\t";
 			count++;
 		}
 		
 		template = template.replace("--TREE-NODE-KEY-SIGN--", treeNodes);
 		template = template.replace("--CELLMAX-SIGN--",  CELLMAX_SIGN);
 		template = template.replace("--CELL-SORTERS--",  cg.generateSorters(allFields) );
 		
 		template = template.replace("--VAL-COMPARATOR--",  cg.generateComparator(fldValue) );
 		
 		
 		template = template.replace("--PUT-PARAMS-SIGNS--",  cg.generatePutParamsSigns(allFields));
 		template = template.replace("--PUT-PARAMS--",  cg.generatePutParams(allFields));
 		
 		template = template.replace("--CELL-DATA--TYPES--", cg.generateParamTypes(allFields) );
 
 		template = template.replace("--DEFINE-EXACT-FIRST--", cg.generatematchingCell(allFields, 1, true));
 		template = template.replace("--DEFINE-MIN-FIRST--", cg.generatematchingCell(allFields, 2, true));
 		template = template.replace("--DEFINE-MAX-FIRST--", cg.generatematchingCell(allFields, 3, true));
 		
 		
 		template = template.replace("--VAL-DATATYPE--", fldValue.datatype);
 				
 		template = 	template.replace("--CELL-MAX-MINUS_1-PARAMS--", CodePartGenerator.getParams(allFields, 1, false));
 				
 		template = 	template.replace("--CELL-MAX-MINUS-1--", CELLMAX_MINUS_1.toString());
 		template = 	template.replace("--CELL-MAX-MINUS-1-SIGN--", CELLMAX_MINUS_SIGN);
 		
 		String keyDataType = fldKey.datatype;
 		String valDataType = fldValue.datatype;
 		String valParentDataType = allFields.get( allFields.size() - 3).datatype;
 		if ( "Short".equals(keyDataType) ) keyDataType = "Integer";
 		if ( "Short".equals(valDataType) ) valDataType = "Integer";
 		if ( "Short".equals(valParentDataType) ) valParentDataType = "Integer";
 
 		template = 	template.replace("--KEY_DATATYPE--", keyDataType);
 		template = 	template.replace("--VAL_DATATYPE--", valDataType);
 		template = 	template.replace("--VAL-PARENT-DATATYPE--", valParentDataType);
 		
 		template = 	template.replace("--CELL-MAX-MINUS-2--", new Integer(CELLMAX - 2).toString());
 		String keys = "";
 		boolean isFirstTime = true;
 		for ( int i=CELLMAX_MINUS_1; i>1; i--) {
 			if ( isFirstTime ) isFirstTime = false;
 			else keys = keys + ", ";
 			keys = keys + "cell" + i + "Key";
 		}
 		if(0 != keys.length())keys= keys + ", ";
 		template = 	template.replace("--TREE-NODE-KEYS--", keys);
 		
 		
 		return template;
 	}
 
 	public static String generateHSearchTableCell2(String module, Field fldKey, Field fldValue,
 			String colName, List<Field> allFields) throws Exception {
 		String template = ReadFileFromJar.getTextFileContent(
 			"/com/bizosys/hsearch/treetable/compiler/templates/HSearchTableCell2.tmp");
 		template = template.replace("--PACKAGE--", module);
 		template = 	template.replace("--COLUMN-NAME--", colName );
 
 		String keyDataType = fldKey.datatype;
 		String valDataType = fldValue.datatype;
 		if ( "Short".equals(keyDataType) ) keyDataType = "Integer";
 		if ( "Short".equals(valDataType) ) valDataType = "Integer";
 
 		template = 	template.replace("--KEY_DATATYPE--", keyDataType);
 		template = 	template.replace("--VAL_DATATYPE--", valDataType);
 		template = 	template.replace("--KEY_DATATYPE_PRIMITIVE--", CodePartGenerator.getPrimitive(keyDataType));
 		template = 	template.replace("--VAL_DATATYPE_PRIMITIVE--", CodePartGenerator.getPrimitive(valDataType));
 
 		CodePartGenerator cg = new CodePartGenerator();
 
 		String CELLMAX_SIGN = "Cell2<"+keyDataType+","+valDataType+">";
 		template = 	template.replace("--CELLMAX-SIGN--", CELLMAX_SIGN);
 		template = template.replace("--CELL-SORTERS--",  cg.generateSorters(allFields) );
 		template = template.replace("--VAL-COMPARATOR--",  cg.generateComparator(fldValue) );
 		template = template.replace("--PUT-PARAMS-SIGNS--",  cg.generatePutParamsSigns(allFields));
 		template = template.replace("--PUT-PARAMS--",  cg.generatePutParams(allFields));
 		
 		template = template.replace("--DEFINE-EXACT-FIRST--", cg.generatematchingCell(allFields, 1, true));
 		template = template.replace("--DEFINE-MIN-FIRST--", cg.generatematchingCell(allFields, 2, true));
 		template = template.replace("--DEFINE-MAX-FIRST--", cg.generatematchingCell(allFields, 3, true));
 		
 		return template;
 	}
 
 	
 	public static String generateMapper(String module, String colName, Field key, Field val, List<Field> allFields) throws Exception {
 		String template = ReadFileFromJar.getTextFileContent(
 			"/com/bizosys/hsearch/treetable/compiler/templates/Mapper.tmp");
 
 		template = template.replace("--PACKAGE--", module);
 		template = 	template.replace("--COLUMN-NAME--", colName );
 		template = 	template.replace("--KEY_DATATYPE--", CodePartGenerator.getPrimitive(key.datatype));
 		template = 	template.replace("--VAL_DATATYPE--", CodePartGenerator.getPrimitive(val.datatype));
 		
 		String allParams = generatePrmitives(allFields);		
 		template = 	template.replace("--ALL_COLS--", allParams);
 		return template;
 	}	
 
 	public static String generatePluginBase(String module, String colName, Field key, Field val, List<Field> allFields) throws Exception {
 		String template = ReadFileFromJar.getTextFileContent(
 			"/com/bizosys/hsearch/treetable/compiler/templates/PluginBase.tmp");
 
 		template = template.replace("--PACKAGE--", module);
 		template = 	template.replace("--COLUMN-NAME--", colName );
 		template = 	template.replace("--KEY_DATATYPE--", CodePartGenerator.getPrimitive(key.datatype));
 		template = 	template.replace("--VAL_DATATYPE--", CodePartGenerator.getPrimitive(val.datatype));
 		
 		String allParams = generatePrmitives(allFields);		
 		template = 	template.replace("--ALL_COLS--", allParams);
 		return template;
 	}	
 	public static String generatePrmitives(List<Field> allFields){
 		String allParams = "";		
 		boolean firstTime = true;
 		int seq = 1;
 		for ( Field fld : allFields) {
 			if ( firstTime ) {
 				firstTime = false;
 			} else allParams = allParams + ", ";
 
 			if ( null == fld) {
 				System.err.println("\n\n ***** Error : Compiler is not able to parse your schema json. Check the ending commas and other syntax.\n\n");
 				System.exit(1);
 			}
 			String type = CodePartGenerator.getPrimitive(fld.datatype);
 			String name = toCamelCase(fld.name);
 			allParams = allParams + " final " + type + " " + name;
 			seq++;
 		}
 		return allParams;
 	}
 
 	public static String generateFilter(Schema schema) throws Exception {
 		String template = ReadFileFromJar.getTextFileContent(
 			"/com/bizosys/hsearch/treetable/compiler/templates/Filter.tmp");
 		template = template.replace("--PACKAGE--", schema.module);
 		
 		StringBuilder plugins = new StringBuilder(4096);
 		boolean isFirst = true;
 		for ( Column column : schema.columns ) {
 			
 			if ( isFirst ) isFirst = false;
 			else plugins.append("\t\telse ");
 			
 			plugins.append("if ( type.equals(\"").append(column.name).append("\") ) {\n");
 			plugins.append("\t\t\treturn new Mapper").append(column.name).append("();\n");
 			plugins.append("\t\t}\n");
 		}
 		
 		template = template.replace("--CREATE-PLUGINS--", plugins.toString());
 		
 		
 		return template;
 	}
 
 	public static String generateClient(String module) throws Exception {
 		String template = ReadFileFromJar.getTextFileContent(
 			"/com/bizosys/hsearch/treetable/compiler/templates/Client.tmp");
 		template = template.replace("--PACKAGE--", module);
 		return template;
 	}
 
 	public static String generateHBaseTableSchema(Schema schema) throws Exception {
 		String template = ReadFileFromJar.getTextFileContent(
 			"/com/bizosys/hsearch/treetable/compiler/templates/HBaseTableSchema.tmp");
 		template = template.replace("--PACKAGE--", schema.module);
 		
 		StringBuilder families = new StringBuilder(4096);
 		for ( Column column : schema.columns ) {
 			
 			if ( "numeric".equals(column.partitions.type) ) {
 				families.append("\t\t").append("columns.put(\"").append(column.name).append("\"," +
 						"new PartitionNumeric());\n");
 			} else if ( "text".equals(column.partitions.type) ) {
 				families.append("\t\t").append("columns.put(\"").append(column.name).append("\"," +
 						"new PartitionByFirstLetter());\n");
 			}
 			
 			families.append("\t\tcolumns.get(\"").append(column.name).append("\").setPartitionsAndRange(\n");
 			families.append("\t\t\t\"").append(column.name).append("\",\n");
 			families.append("\t\t\t\"").append(column.partitions.names).append("\",\n");
 			families.append("\t\t\t\"").append(column.partitions.ranges).append("\",\n");
 			
 			int seq = 0;
 			for ( Field fld : column.indexes) {
 				if ( fld.name.equals(column.partitions.column) ) {
					families.append("\t\t\t").append(seq).append(");");
 					break;
 				}
 				seq++;
 			}
 			
 			if ( column.key.name.equals(column.partitions.column) ) {
				families.append("\t\t\t").append(seq).append(");");
 			}
 			seq++;
 			
 			if ( column.value.name.equals(column.partitions.column) ) {
				families.append("\t\t\t").append(seq).append(");");
 			}
 			seq++;
 		}
 
 		template = template.replace("--CREATE-COL-FAMILIES--", families.toString());
 		template = template.replace("--TABLE-NAME--", schema.table);
 		return template;
 	}
 
 	
 	public static String generateHSearchTableCombinerImpl(Schema schema) throws Exception {
 		String template = ReadFileFromJar.getTextFileContent(
 			"/com/bizosys/hsearch/treetable/compiler/templates/HSearchTableCombinerImpl.tmp");
 		template = template.replace("--PACKAGE--", schema.module);
 		
 		StringBuilder tables = new StringBuilder(4096);
 		for ( Column column : schema.columns ) {
 			tables.append("\t\tif ( tableType.equals(\"").append(column.name).append("\")) return new HSearchTable");
 			tables.append(column.name).append("();\n");
 		}
 
 		template = template.replace("--CREATE-TABLES--", tables.toString());
 		return template;
 	}
 
 	public static String generateHSearchTableMultiQueryProcessorImpl(String module) throws Exception {
 		String template = ReadFileFromJar.getTextFileContent(
 			"/com/bizosys/hsearch/treetable/compiler/templates/HSearchTableMultiQueryProcessorImpl.tmp");
 		template = template.replace("--PACKAGE--", module);
 		return template;
 	}
 
 	public static String generateReducer(String module) throws Exception {
 		String template = ReadFileFromJar.getTextFileContent(
 			"/com/bizosys/hsearch/treetable/compiler/templates/Reducer.tmp");
 		template = template.replace("--PACKAGE--", module);
 		return template;
 	}	
 	
 	public static String generateWebservice(String module) throws Exception {
 		String template = ReadFileFromJar.getTextFileContent(
 			"/com/bizosys/hsearch/treetable/compiler/templates/Webservice.tmp");
 		template = template.replace("--PACKAGE--", module);
 		return template;
 	}	
 
 	public static String fileToString(String fileName) 
 	{
 		
 		File aFile = getFile(fileName);
 		BufferedReader reader = null;
 		InputStream stream = null;
 		StringBuilder sb = new StringBuilder();
 		try {
 			stream = new FileInputStream(aFile); 
 			reader = new BufferedReader ( new InputStreamReader (stream) );
 			String line = null;
 			String newline = "\n";
 			while((line=reader.readLine())!=null) {
 				if (line.length() == 0) continue;
 				sb.append(line).append(newline);	
 			}
 			return sb.toString();
 		} 
 		catch (Exception ex) 
 		{
 			throw new RuntimeException(ex);
 		} 
 		finally 
 		{
 			try {if ( null != reader ) reader.close();
 			} catch (Exception ex) {ex.printStackTrace(System.err);}
 			try {if ( null != stream) stream.close();
 			} catch (Exception ex) {ex.printStackTrace(System.err);}
 		}
 	}	
 	
 	   public static File getFile(String fileName) 
 	    {
 			File aFile = new File(fileName);
 			if (aFile.exists()) return aFile;
 			
 			aFile = new File("/" + fileName);
 			if (aFile.exists()) return aFile;
 
 			aFile = new File("conf/" + fileName);
 			if (aFile.exists()) return aFile;
 			
 			aFile = new File("resources/" + fileName);
 			if (aFile.exists()) return aFile;
 			
 			try {
 				URL resource = CellGenerator.class.getClassLoader().getResource(fileName);
 				if ( resource != null) aFile = new File(resource.toURI());
 			} 
 			catch (URISyntaxException ex) {
 				throw new RuntimeException(ex);
 			}
 
 			if (aFile.exists()) return aFile;
 
 			throw new RuntimeException("FileResourceUtil > File does not exist :" + fileName);
 		}
 	   
 	   public static String toCamelCase(String value) {
 		    StringBuilder sb = new StringBuilder();
 
 		    final char delimChar = '_';
 		    boolean lower = false;
 		    for (int charInd = 0; charInd < value.length(); ++charInd) {
 		      final char valueChar = value.charAt(charInd);
 		      if (valueChar == delimChar) {
 		        lower = false;
 		      } else if (lower) {
 		        sb.append(Character.toLowerCase(valueChar));
 		      } else {
 		    	  if ( charInd == 0 ) sb.append(Character.toLowerCase(valueChar));
 		    	  else sb.append(Character.toUpperCase(valueChar));
 		        lower = true;
 		      }
 		    }
 
 		    return sb.toString();
 		  }	   
 	    		
 }
