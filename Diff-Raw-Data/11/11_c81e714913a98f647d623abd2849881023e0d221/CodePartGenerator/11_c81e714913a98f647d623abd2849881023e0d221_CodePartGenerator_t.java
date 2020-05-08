 /*
 * Copyright 2010 Bizosys Technologies Limited
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
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.bizosys.hsearch.treetable.compiler.Schema.Field;
 
 public class CodePartGenerator {
 	
 	static Map<String, Character> dataTypes = new HashMap<String, Character>();
 	static {
 		dataTypes.put("Double", 'd');
 		dataTypes.put("Long", 'l');
 		dataTypes.put("Integer", 'i');
 		dataTypes.put("Float", 'f');
 		dataTypes.put("Short", 's');
 		dataTypes.put("Boolean", 'b');
 		dataTypes.put("String", 't');
 		dataTypes.put("Byte", 'c');
 		dataTypes.put("byte[]", 'a');
 	}
 	
 	public static Map<String, String> cellSignatures = new HashMap<String, String>();
 
 	public CodePartGenerator(){
 		
 	}
 	
 	public static String setKeyComparision(String template, String keyDataType)
 			throws IOException {
 		if (keyDataType.equals("Short")) {
 			template = template.replace("--COMPARE-BY-VALUE--", ".shortValue() != cell1Key.shortValue()");
 		} else if ( keyDataType.equals("Integer")) {
 			template = template.replace("--COMPARE-BY-VALUE--", ".intValue() != cell1Key.intValue()");
 		} else if ( keyDataType.equals("Float")) {
 			template = template.replace("--COMPARE-BY-VALUE--", ".floatValue() != cell1Key.floatValue()");
 		} else if ( keyDataType.equals("Double")) {
 			template = template.replace("--COMPARE-BY-VALUE--", ".doubleValue() != cell1Key.doubleValue()");
 		} else if ( keyDataType.equals("Boolean")) {
 			template = template.replace("--COMPARE-BY-VALUE--", ".booleanValue() != cell1Key.booleanValue()");
 		} else if ( keyDataType.equals("Byte")) {
 			template = template.replace("--COMPARE-BY-VALUE--", ".byteValue() != cell1Key.byteValue()");
 		} else if ( keyDataType.equals("String")) {
			template = template.replace("matchingCell--CELL-MAX-MINUS-2----COMPARE-BY-VALUE--", "! matchingCell--CELL-MAX-MINUS-2-- .equals(cell1Key)");
 		} else if ( keyDataType.equals("byte[]")) {
			template = template.replace("matchingCell--CELL-MAX-MINUS-2----COMPARE-BY-VALUE--", "! Storable.compareBytes(matchingCell--CELL-MAX-MINUS-2--, cell1Key)");
 		} else {
 			throw new IOException("Unknown Data type");
 		}
 		return template;
 	}
 
 	public static String setEqualKeyComparision(String template, String keyDataType)
 			throws IOException {
 		if (keyDataType.equals("Short")) {
 			template = template.replace("--COMPARE-BY-VALUE-EQUAL--", ".shortValue() == cell1Key.shortValue()");
 		} else if ( keyDataType.equals("Integer")) {
 			template = template.replace("--COMPARE-BY-VALUE-EQUAL--", ".intValue() == cell1Key.intValue()");
 		} else if ( keyDataType.equals("Float")) {
 			template = template.replace("--COMPARE-BY-VALUE-EQUAL--", ".floatValue() == cell1Key.floatValue()");
 		} else if ( keyDataType.equals("Double")) {
 			template = template.replace("--COMPARE-BY-VALUE-EQUAL--", ".doubleValue() == cell1Key.doubleValue()");
 		} else if ( keyDataType.equals("Boolean")) {
 			template = template.replace("--COMPARE-BY-VALUE-EQUAL--", ".booleanValue() == cell1Key.booleanValue()");
 		} else if ( keyDataType.equals("Byte")) {
 			template = template.replace("--COMPARE-BY-VALUE-EQUAL--", ".byteValue() == cell1Key.byteValue()");
 		} else if ( keyDataType.equals("String")) {
			template = template.replace("matchingCell--CELL-MAX-MINUS-2----COMPARE-BY-VALUE-EQUAL--", " matchingCell--CELL-MAX-MINUS-2-- .equals(cell1Key)");
 		} else if ( keyDataType.equals("byte[]")) {
			template = template.replace("matchingCell--CELL-MAX-MINUS-2----COMPARE-BY-VALUE-EQUAL--", " Storable.compareBytes(matchingCell--CELL-MAX-MINUS-2--, cell1Key)");
 		} else {
 			throw new IOException("Unknown Data type");
 		}
 		return template;
 	}
 	
 	public static String setAbsValue(String template, String keyDataType)
 			throws IOException {
 		if (keyDataType.equals("Short")) {
 			template = template.replace("--ABS-VALUE--", "shortValue()");
 		} else if ( keyDataType.equals("Integer")) {
 			template = template.replace("--ABS-VALUE--", "intValue()");
 		} else if ( keyDataType.equals("Float")) {
 			template = template.replace("--ABS-VALUE--", "floatValue()");
 		} else if ( keyDataType.equals("Double")) {
 			template = template.replace("--ABS-VALUE--", "doubleValue()");
 		} else if ( keyDataType.equals("Boolean")) {
 			template = template.replace("--ABS-VALUE--", "hashCode()");
 		} else if ( keyDataType.equals("Byte")) {
 			template = template.replace("--ABS-VALUE--", "byteValue()");
 		} else if ( keyDataType.equals("String")) {
 			template = template.replace("--ABS-VALUE--", "length()");
 		} else if ( keyDataType.equals("byte[]")) {
 			System.out.println("\n\n\n\nABS for bytes \n\n\n");
 			template = template.replace("--ABS-VALUE--", "length");
 		} else {
 			throw new IOException("Unknown Data type");
 		}
 		return template;
 	}
 
 	
 
 	public CodePartGenerator(List<Field> fields){
 		int totalFields = fields.size();
 		
 		int startIndex = 0;
 		int limit = totalFields;
 		int cellNo = limit; 
 		for ( int i=startIndex; i<limit-2; i++ ) {
 			String parentKeyDataType = fields.get(i).datatype;
 			if ( parentKeyDataType.equals("Short")) parentKeyDataType = "Integer";
 			
 			cellNo--;
 			String cellSign = parentKeyDataType + ", Cell" + cellNo + "<";
 			boolean firstTime = true;
 			for ( int j=i + 1; j<limit; j++ ) {
 				if ( firstTime ) {
 					firstTime = false;
 				} else cellSign = cellSign + ", ";
 				
 				if ( fields.get(j).datatype.equals("Short")) cellSign = cellSign + "Integer";
 				else cellSign = cellSign + fields.get(j).datatype;
 			}
 			cellSign = cellSign + ">";
 			cellSignatures.put(new Integer(cellNo).toString(), cellSign);
 		}		
 	}
 	
 
 	public String generatePutParamsSigns(List<Field> fields) throws Exception {
 
 		String allParams = "";
 		
 		boolean firstTime = true;
 		for ( Field fld : fields) {
 			if ( firstTime ) {
 				firstTime = false;
 			} else allParams = allParams + ", ";
 
 			String name = fld.name;
 			String dataType = fld.datatype;
 			if ( dataType.equals("Short")) dataType = "Integer";
 			
 			allParams = allParams + dataType + " " + name;
 			
 		}
 		return allParams;
 	}
 	
 	public String generatePutParams(List<Field> fields) throws Exception {
 
 		String allParams = "";
 		
 		boolean firstTime = true;
 		for ( Field fld : fields) {
 			if ( firstTime ) {
 				firstTime = false;
 			} else allParams = allParams + ", ";
 
 			String name = fld.name;
 			allParams = allParams + name;
 			
 		}
 		return allParams;
 	}	
 	
 	public String generateParamTypes(List<Field> fields) throws Exception {
 
 		String allParams = "";
 		
 		boolean firstTime = true;
 		for ( Field fld : fields) {
 			if ( firstTime ) {
 				firstTime = false;
 			} else allParams = allParams + ", ";
 
 			String type = fld.datatype;
 			if ( "Short".equals(type)) type = "Integer";
 			allParams = allParams + "\"" + type + "\"" ;
 			
 		}
 		return allParams;
 	}		
 	
 	public String generateParamSign(List<Field> fields) throws Exception {
 
 		String allParams = "";
 		
 		boolean firstTime = true;
 		int seq = 1;
 		for ( Field fld : fields) {
 			if ( firstTime ) {
 				firstTime = false;
 			} else allParams = allParams + ", ";
 
 			String type = fld.datatype;
 			if ( "Short".equals(type)) type = "Integer";
 			allParams = allParams + type + " cell" + seq;
 			seq++;
 		}
 		return allParams;
 	}	
 
 	
 	public String generateComparator(Field valField) throws Exception {
 		if ( "Integer".equals(valField.datatype) ) return "IntegerComparator";
 		else if ("Double".equals(valField.datatype)) return "DoubleComparator";
 		else if ("Float".equals(valField.datatype)) return "FloatComparator";
 		else if ("Long".equals(valField.datatype)) return "LongComparator";
 		else if ("String".equals(valField.datatype)) return "StringComparator";
 		else if ("Byte".equals(valField.datatype)) return "ByteComparator";
 		else if ("Short".equals(valField.datatype)) return "IntegerComparator";
 		else if ("byte[]".equals(valField.datatype)) return "BytesComparator";
 		else throw new Exception("Datatype is not found - " + valField.datatype );
 	}
 	
 	
 	public String generateSorters(List<Field> fields) throws Exception {
 
 		String allSorters = "";
 		
 		for ( Field field :  fields) {
 			if ( allSorters.length() > 0 ) allSorters = allSorters + ",\n\t\t\t\t";
 			
 			String dataType = field.datatype;
 			double minVal = field.minValue;
 			
 			if ("Integer".equals(dataType)) allSorters = allSorters + "SortedBytesInteger.getInstance()";
 			else if ("String".equals(dataType)) allSorters = allSorters + "SortedBytesString.getInstance()";
 			else if ("Float".equals(dataType)) allSorters = allSorters + "SortedBytesFloat.getInstance()";
 			else if ("Long".equals(dataType)) allSorters = allSorters + "SortedBytesLong.getInstance()";
 			else if ("Double".equals(dataType)) allSorters = allSorters + "SortedBytesDouble.getInstance()";
 			else if ("Byte".equals(dataType)) allSorters = allSorters + "SortedBytesChar.getInstance()";
 			else if ("byte[]".equals(dataType)) allSorters = allSorters + "SortedBytesArray.getInstanceArr()";
 			else if ("Boolean".equals(dataType)) allSorters = allSorters + "SortedBytesBoolean.getInstance()";
 			else if ("Short".equals(dataType)) allSorters = allSorters + 
 					"SortedBytesUnsignedShort.getInstanceShort().setMinimumValueLimit((short) " + minVal + " ) ";
 			
 		}
 		return allSorters;
 	}	
 		
 	public String generatematchingCell(List<Field> fields, int castType, boolean isFirst) throws Exception {
 		try {
 			int seq = -1;
 			String text = "";
 			for (Field field : fields) {
 				seq++;
 				char firstchar = dataTypes.get(field.datatype);
 				switch ( firstchar) {
 				  	case 'i':
 				  		switch ( castType ) {
 				  			case 1:
 						  		text = text +  ("\t\tInteger matchingCell" + seq + " = ( query.filterCells[" + seq + 
 						  				"] ) ? (Integer) query.exactValCellsO[" + seq + "]: null;\n");
 						  		break;
 				  			case 2:
 						  		text = text +  ("\t\tInteger cellMin" + seq + " = ( query.minValCells[" + seq + 
 						  				"] == HSearchQuery.DOUBLE_MIN_VALUE) ? null : new Double(query.minValCells[" + seq + "]).intValue();\n");
 						  		break;
 				  			case 3:
 						  		text = text +  ("\t\tInteger cellMax" + seq + " =  (query.maxValCells[" + seq + 
 						  				"] == HSearchQuery.DOUBLE_MAX_VALUE) ? null : new Double(query.maxValCells[" + seq + "]).intValue();\n");
 						  		break;
 				  			case 4:
 						  		text = text +  ("\t\tInteger[] inValues" + seq + " =  (query.inValCells[" + seq + 
 						  				"]) ? (Integer[])query.inValuesAO[" + seq + "]: null;\n");
 						  		break;
 				  		}
 				  		break;
 				  	case 'f':
 				  		switch ( castType ) {
 				  			case 1:
 						  		text = text +  ("\t\tFloat matchingCell" + seq + " = ( query.filterCells[" + seq + 
 						  				"] ) ? (Float) query.exactValCellsO[" + seq + "]: null;\n");
 						  		break;
 				  			case 2:
 						  		text = text +  ("\t\tFloat cellMin" + seq + " = ( query.minValCells[" + seq + 
 						  				"] == HSearchQuery.DOUBLE_MIN_VALUE) ? null : new Double(query.minValCells[" + seq + "]).floatValue();\n");
 						  		break;
 				  			case 3:
 						  		text = text +  ("\t\tFloat cellMax" + seq + " =  (query.maxValCells[" + seq + 
 						  				"] == HSearchQuery.DOUBLE_MAX_VALUE) ? null : new Double(query.maxValCells[" + seq + "]).floatValue();\n");
 						  		break;
 				  			case 4:
 						  		text = text +  ("\t\tFloat[] inValues" + seq + " =  (query.inValCells[" + seq + 
 						  				"]) ? (Float[])query.inValuesAO[" + seq + "]: null;\n");
 						  		break;
 				  		}
 				  		break;
 				  	case 'd':
 				  		switch ( castType ) {
 				  			case 1:
 						  		text = text +  ("\t\tDouble matchingCell" + seq + " = ( query.filterCells[" + seq + 
 						  				"] ) ? (Double) query.exactValCellsO[" + seq + "]: null;\n");
 						  		break;
 				  			case 2:
 						  		text = text +  ("\t\tDouble cellMin" + seq + " = ( query.minValCells[" + seq + 
 						  				"] == HSearchQuery.DOUBLE_MIN_VALUE) ? null : query.minValCells[" + seq + "];\n");
 						  		break;
 				  			case 3:
 						  		text = text +  ("\t\tDouble cellMax" + seq + " =  (query.maxValCells[" + seq + 
 						  				"] == HSearchQuery.DOUBLE_MAX_VALUE) ? null : query.maxValCells[" + seq + "];\n");
 						  		break;
 				  			case 4:
 						  		text = text +  ("\t\tDouble[] inValues" + seq + " =  (query.inValCells[" + seq + 
 						  				"]) ? (Double[])query.inValuesAO[" + seq + "]: null;\n");
 						  		break;
 				  		}
 				  		break;
 				  	case 'l':
 				  		switch ( castType ) {
 				  			case 1:
 						  		text = text +  ("\t\tLong matchingCell" + seq + " = ( query.filterCells[" + seq + 
 						  				"] ) ? (Long) query.exactValCellsO[" + seq + "]: null;\n");
 						  		break;
 				  			case 2:
 						  		text = text +  ("\t\tLong cellMin" + seq + " = ( query.minValCells[" + seq + 
 						  				"] == HSearchQuery.DOUBLE_MIN_VALUE) ? null : new Double(query.minValCells[" + seq + "]).longValue();\n");
 						  		break;
 				  			case 3:
 						  		text = text +  ("\t\tLong cellMax" + seq + " =  (query.maxValCells[" + seq + 
 						  				"] == HSearchQuery.DOUBLE_MAX_VALUE) ? null : new Double(query.maxValCells[" + seq + "]).longValue();\n");
 						  		break;
 				  			case 4:
 						  		text = text +  ("\t\tLong[] inValues" + seq + " =  (query.inValCells[" + seq + 
 						  				"]) ? (Long[])query.inValuesAO[" + seq + "]: null;\n");
 						  		break;
 				  		}
 				  		break;
 				  	case 's':
 				  		switch ( castType ) {
 				  			case 1:
 						  		text = text +  ("\t\tInteger matchingCell" + seq + " = ( query.filterCells[" + seq + 
 						  				"] ) ? (Integer) query.exactValCellsO[" + seq + "]: null;\n");
 						  		break;
 				  			case 2:
 						  		text = text +  ("\t\tInteger cellMin" + seq + " = ( query.minValCells[" + seq + 
 						  				"] == HSearchQuery.DOUBLE_MIN_VALUE) ? null : new Double(query.minValCells[" + seq + "]).intValue();\n");
 						  		break;
 				  			case 3:
 						  		text = text +  ("\t\tInteger cellMax" + seq + " =  (query.maxValCells[" + seq + 
 						  				"] == HSearchQuery.DOUBLE_MAX_VALUE) ? null : new Double(query.maxValCells[" + seq + "]).intValue();\n");
 						  		break;
 				  			case 4:
 						  		text = text +  ("\t\tInteger[] inValues" + seq + " =  (query.inValCells[" + seq + 
 						  				"]) ? (Integer[])query.inValuesAO[" + seq + "]: null;\n");
 						  		break;
 				  		}
 				  		break;
 				  	case 't':
 				  		switch ( castType ) {
 				  			case 1:
 						  		text = text +  ("\t\tString matchingCell" + seq + " = ( query.filterCells[" + seq + 
 						  				"] ) ? (String) query.exactValCellsO[" + seq + "]: null;\n");
 						  		break;
 				  			case 2:
 						  		text = text +  ("\t\tString cellMin" + seq + " = null;\n");
 						  		break;
 				  			case 3:
 						  		text = text +  ("\t\tString cellMax" + seq + " = null;\n");
 						  		break;
 				  			case 4:
 						  		text = text +  ("\t\tString[] inValues" + seq + " =  (query.inValCells[" + seq + 
 						  				"]) ? (String[])query.inValuesAO[" + seq + "]: null;\n");
 						  		break;
 				  		}
 				  		break;
 				  	case 'c':
 				  		switch ( castType ) {
 				  			case 1:
 						  		text = text +  ("\t\tByte matchingCell" + seq + " = ( query.filterCells[" + seq + 
 						  				"] ) ? (Byte) query.exactValCellsO[" + seq + "]: null;\n");
 						  		break;
 				  			case 2:
 						  		text = text +  ("\t\tByte cellMin" + seq + " = ( query.minValCells[" + seq + 
 						  				"] == HSearchQuery.DOUBLE_MIN_VALUE) ? null : new Double(query.minValCells[" + seq + "]).byteValue();\n");
 						  		break;
 				  			case 3:
 						  		text = text +  ("\t\tByte cellMax" + seq + " =  (query.maxValCells[" + seq + 
 						  				"] == HSearchQuery.DOUBLE_MAX_VALUE) ? null : new Double(query.maxValCells[" + seq + "]).byteValue();\n");
 						  		break;
 				  			case 4:
 						  		text = text +  ("\t\tByte[] inValues" + seq + " =  (query.inValCells[" + seq + 
 						  				"]) ? (Byte[])query.inValuesAO[" + seq + "]: null;\n");
 						  		break;
 				  		}
 				  		break;
 				  	case 'b':
 				  		switch ( castType ) {
 				  			case 1:
 						  		text = text +  ("\t\tBoolean matchingCell" + seq + " = ( query.filterCells[" + seq + 
 						  				"] ) ? (Boolean) query.exactValCellsO[" + seq + "]: null;\n");
 						  		break;
 				  			case 2:
 						  		text = text +  ("\t\tBoolean cellMin" + seq + " = null;\n");
 						  		break;
 				  			case 3:
 						  		text = text +  ("\t\tBoolean cellMax" + seq + " = null;\n");
 						  		break;
 				  			case 4:
 						  		text = text +  ("\t\tBoolean[] inValues" + seq + " =  (query.inValCells[" + seq + 
 						  				"]) ? (Boolean[])query.inValuesAO[" + seq + "]: null;\n");
 						  		break;
 				  		}
 				  		break;
 				  	case 'a':
 				  		switch ( castType ) {
 				  			case 1:
 						  		text = text +  ("\t\t byte[] matchingCell" + seq + " = ( query.filterCells[" + seq + 
 						  				"] ) ? (byte[]) query.exactValCellsO[" + seq + "]: null;\n");
 						  		break;
 				  			case 2:
 						  		text = text +  ("\t\t byte[] cellMin" + seq + " = null;\n");
 						  		break;
 				  			case 3:
 						  		text = text +  ("\t\t byte[] cellMax" + seq + " = null;\n");
 						  		break;
 				  			case 4:
 						  		text = text +  ("\t\tbyte[][] inValues" + seq + " =  (query.inValCells[" + seq + 
 						  				"]) ? (byte[][])query.inValuesAO[" + seq + "]: null;\n");
 						  		break;
 				  		}
 				  		break;
 
 				}
 		  	  }
 			
 			if(isFirst){
 				String result = "";
 				int CELLMAX = fields.size();
 				String keyCell = "";
 				String target = "";
 				if(castType == 1)keyCell = "matchingCell"+(CELLMAX - 2);
 				else if(castType == 2)keyCell = "cellMin"+(CELLMAX - 2);
 				else if(castType == 3)keyCell = "cellMax"+(CELLMAX - 2);
 				else if(castType == 4)keyCell = "inValues"+(CELLMAX - 2);
 				
 				if(castType == 4)target = fields.get(CELLMAX - 2).datatype +"[] "+keyCell;
 				else target = fields.get(CELLMAX - 2).datatype +" "+keyCell;
 				
 				int index = text.indexOf(target);
 				result = text.replace(text.substring(index, index + target.length()), "cell2Visitor."+keyCell); 
 				return result;
 			}
 			return text;
 		  } catch (Exception ex) {
 			  ex.printStackTrace(System.err);
 			  
 			  throw ex;
 		  }
 	}
 		
 	public String createCellClass(List<Field> fields, int cellNo) {
 		
 		int remainingCells = fields.size() - cellNo;
 		int remainingCellsValueIndex = remainingCells - 1;
 		
 		String cellSignatureKey = fields.get(cellNo - 1).datatype;
 		if ( cellSignatureKey.equals("Short")) cellSignatureKey = "Integer";
 		
 		String theValueCellSignature = cellSignatures.get(new Integer(remainingCells).toString());
 		theValueCellSignature = theValueCellSignature.replace("Short", "Integer");
 
 		String parentKeySignature = fields.get(cellNo).datatype;
 		if ( parentKeySignature.equals("Short")) parentKeySignature = "Integer";
 		
 		
 		String theRemainingValueCellSignature = cellSignatures.get(new Integer(remainingCellsValueIndex).toString());
 		theRemainingValueCellSignature = theRemainingValueCellSignature.replace("Short", "Integer");
 
 		StringBuilder code = new StringBuilder();
 		code.append("public static final class Cell" + remainingCells
 				+ "Map\n\t\t extends EmptyMap<" + theValueCellSignature
 				+ "> {\n\n");
 		code.append("\tpublic HSearchQuery query;\n\tpublic Cell2FilterVisitor cell2Visitor;\n");
 		
 		code.append("\tpublic " + parentKeySignature + " matchingCell" + cellNo
 				+ ";\n\tpublic " + parentKeySignature + " cellMin" + cellNo
 				+ "; \n\tpublic " + parentKeySignature + " cellMax" + cellNo
 				+ ";\n\tpublic " + parentKeySignature + "[] inValues" + cellNo+ ";\n");
 		code.append("\tpublic Map<" + theRemainingValueCellSignature + "> cell"
 				+ remainingCellsValueIndex + "L = null;\n\n");
 
 		code.append("\tpublic Cell" + remainingCells
 				+ "Map(final HSearchQuery query, final Cell2FilterVisitor cell2Visitor"
 				+ getParams(fields, cellNo, true) + ") {");
 		code.append("\n\t\tthis.query = query; \n\t\tthis.cell2Visitor = cell2Visitor;");
 		code.append("\n\t\tthis.matchingCell" + cellNo + " = matchingCell"+ cellNo 
 				  + ";\n\t\tthis.cellMin" + cellNo + " = cellMin" + cellNo 
 				  + ";\n\t\tthis.cellMax" + cellNo + " = cellMax"+ cellNo 
 				  + ";\n\t\tthis.inValues" + cellNo + " = inValues"+ cellNo
 				  + ";");
 		
 		code.append("\n\t\tthis.cell" + remainingCellsValueIndex
 				+ "L = new Cell" + remainingCellsValueIndex
 				+ "Map(query, cell2Visitor"
 				+ getParams(fields, cellNo + 1, false) + ");\n\t}");
 		code.append("\n\t@Override\n");
 
 		String completeCellSign = theValueCellSignature;
 		String currentCellSign = completeCellSign.replaceFirst(cellSignatureKey + ", ", ""); 
 
 		code.append("\tpublic final " + currentCellSign + " put( final " + cellSignatureKey
 				+ " key, final " + currentCellSign + " value) {");
 		code.append("\n\ttry {\n\t\tcell2Visitor.cell" + remainingCells
 				+ "Key = key;");
 		code.append("\n\t\tif (query.filterCells[" + cellNo + "]) {");
 		code.append("\n\t\t\tif (query.notValCells[" + cellNo + "])");
 		code.append("\n\t\t\t\tvalue.getNotMap(matchingCell" + cellNo + ", cell"+ remainingCellsValueIndex + "L);");
 		code.append("\n\t\t\telse if (query.inValCells[" + cellNo + "])");
 		code.append("\n\t\t\t\tvalue.getInMap(inValues" + cellNo + ", cell"+ remainingCellsValueIndex + "L);");
 		code.append("\n\t\t\telse");
 		code.append("\n\t\t\tvalue.getMap(matchingCell" + cellNo + ", cellMin"
 				+ cellNo + ", cellMax" + cellNo + ", cell"
 				+ remainingCellsValueIndex + "L);");
 		code.append("\n\t\t } else {\n\t\t\tvalue.sortedList = cell"
 				+ remainingCellsValueIndex
 				+ "L;\n\t\t\tvalue.parseElements();\n\t\t}\n\t\treturn value;");
 		code.append("\n\t\t} catch (IOException e) {\n\t\t\tthrow new IndexOutOfBoundsException(e.getMessage());\n\t\t}\n\t}\n}\n\n\n");
 		
 		return code.toString();
 	}
 	
 	public static String getParams(List<Field> fields, int cellNo, boolean withSignature){
 		String params = "";
 		String dataType = "";
 		int keyCellIndex = fields.size() - 2;
 		int startIndex = fields.size() - 1;
 		int endIndex = cellNo;
 
 		if(withSignature){
 			for ( int i = startIndex; i >= endIndex ; i-- ) {	
 				if(i == keyCellIndex)continue;
 				dataType = fields.get(i).datatype;
 				if ( dataType.equals("Short")) dataType = "Integer";
 				params = params + ", final " + dataType + " matchingCell" + i + ", final "
 						+ dataType + " cellMin" + i + ", final " + dataType
 						+ " cellMax" + i + ", final " + dataType
 						+ "[] inValues" + i;
 			}
 		}
 		else {
 			for ( int i = startIndex; i >= endIndex ; i-- ) {	
 				if(i == keyCellIndex)continue;
 				params = params + ",matchingCell"+i+", cellMin"+i+", cellMax"+i+", inValues"+i;
 			}
 			
 		}
 		return params;		
 	}
 	
 	public static String getPrimitive(String str){
 		String result = "";
 		Character firstcharO = dataTypes.get(str);
 		if ( null == firstcharO) {
 			System.out.println("Unknown datatype. JSON syntax error: " + dataTypes);
 			System.exit(1);
 		}
 		
 		char firstchar = firstcharO;
 		switch ( firstchar) {
 		  	case 'i':
 		  		result = "int";
 		  		break;
 		  	case 'l':
 		  		result = "long";
 		  		break;
 		  	case 'f':
 		  		result = "float";
 		  		break;
 		  	case 'd':
 		  		result = "double";
 		  		break;
 		  	case 's':
 		  		result = "int";
 		  		break;
 		  	case 'b':
 		  		result = "boolean";
 		  		break;
 		  	case 'c':
 		  		result = "byte";
 		  		break;
 		  	case 't':
 		  		result = "String";
 		  		break;
 		  	case 'a':
 		  		result = "byte[]";
 		  		break;
 		}
 		return result;
 	}
 }
