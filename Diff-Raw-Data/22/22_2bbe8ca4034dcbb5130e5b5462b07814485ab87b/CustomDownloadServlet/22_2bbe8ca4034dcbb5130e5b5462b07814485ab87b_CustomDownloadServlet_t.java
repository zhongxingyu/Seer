 package gov.nih.nci.cadsr.cdecurate.tool;
 
 import gov.nih.nci.cadsr.cdecurate.util.AdministeredItemUtil;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.sql.Array;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.Struct;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.apache.poi.hssf.usermodel.HSSFCellStyle;
 import org.apache.poi.hssf.usermodel.HSSFFont;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.Font;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Text;
 
 import com.sun.org.apache.xml.internal.serialize.OutputFormat;
 import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
 
 
 
 public class CustomDownloadServlet extends CurationServlet {
 
 	public static final Logger logger = Logger.getLogger(CustomDownloadServlet.class.getName());
 
 	private static final int GRID_MAX_DISPLAY = 10;
 	private static int MAX_DOWNLOAD = 0;
 	private static String xmlColumns = null;
 
 	public CustomDownloadServlet() {
 	}
 
 	public CustomDownloadServlet(HttpServletRequest req, HttpServletResponse res,
 			ServletContext sc) {
 		super(req, res, sc);
 
 	}
 
 	public void execute(ACRequestTypes reqType) throws Exception {	
 
 		switch (reqType){
 		case showDEfromOutside:
 			prepDisplayPage("O-CDE");
 			break;
 		case showDEfromSearch:
 			prepDisplayPage("CDE"); 
 			break;
 		case jsonRequest:
 			returnJSONFromSession("Return");
 			break;
 		case jsonLayout:
 			returnJSONFromSession("Layout");
 			break;
 		case dlExcelColumns:
 			ArrayList<String[]> downloadRows = getRecords(false, false);
 			createDownloadColumns(downloadRows);
 			break;
 		case dlXMLColumns:
 			ArrayList<String[]> xmlDownloadRows = getRecords(false, false);
 			createXMLDownload(xmlDownloadRows);
 			break;
 		case createExcelDownload:
 			createDownload();
 			break;
 		case showVDfromSearch:
 			prepDisplayPage("VD"); 
 			break;
 		case showDECfromSearch:
 			prepDisplayPage("DEC"); 
 			break;
 		case createFullDEDownload:
 			setDownloadIDs("CDE",false);
 			setColHeadersAndTypes("CDE");
 			ArrayList<String[]> allRows = getRecords(true, false);
 			createDownloadColumns(allRows);
 			break;
 		}	
 	}
 
 	private void prepDisplayPage(String type) {
 
 		boolean outside = false;
 
 		if (type.startsWith("O")){
 			type = type.substring(2);
 			outside = true;
 		}
 
 		if (this.MAX_DOWNLOAD == 0) { 
 			GetACService getAC = new GetACService(m_classReq, m_classRes, this);
 			Vector vList = getAC.getToolOptionData("CURATION", "CUSTOM_DOWNLOAD_LIMIT", "");
 
 			if (vList != null && vList.size()>0)
 			{
 				TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean)vList.elementAt(0);
 				if (tob != null){
 					this.MAX_DOWNLOAD = Integer.valueOf(tob.getVALUE());
 					System.out.println("DL Limit: "+tob.getVALUE());	
 				}
 			}
 		}	
 
 		setDownloadIDs(type, outside);
 		setColHeadersAndTypes(type);
 		ArrayList<String[]> rows = getRecords(false, true);
 
 		m_classReq.getSession().setAttribute("rows", rows);
 		ForwardJSP(m_classReq, m_classRes, "/CustomDownload.jsp");
 	}
 
 	private void setDownloadIDs(String type, boolean outside) {
 		ArrayList<String> downloadID = new ArrayList<String>();
 
 		if (!outside) {
 			Set<String> paramNames = this.m_classReq.getParameterMap().keySet();
 			Vector<String> searchID= (Vector<String>) this.m_classReq.getSession().getAttribute("SearchID");
 
 			for(String name:paramNames) {
 				if (name.startsWith("CK")) {
 					int ndx = Integer.valueOf(name.substring(2));
 					downloadID.add(searchID.get(ndx));
 				}
 			}
 		} else {
 			String searchIDCSV= (String) this.m_classReq.getParameter("SearchID");			
 			String[] ids = searchIDCSV.split(",");
 			for(String id: ids) 
 				downloadID.add(id);
 		}
 
 
 		m_classReq.getSession().setAttribute("downloadIDs", downloadID);
 		m_classReq.getSession().setAttribute("downloadType", type);
 		m_classReq.getSession().setAttribute("downloadLimit", Integer.toString(this.MAX_DOWNLOAD));
 
 		if (downloadID.size() > this.MAX_DOWNLOAD)
 			ForwardJSP(m_classReq, m_classRes, "/CustomOverLimit.jsp");
 
 	}
 
 	private ArrayList<String[]> getRecords(boolean full, boolean restrict) {
 
 		ArrayList<String[]> rows = new ArrayList<String[]>();
 
 		ArrayList<HashMap<String,List<String[]>>> arrayData = new ArrayList<HashMap<String,List<String[]>>>();
 
 		ResultSet rs = null;
 		PreparedStatement ps = null;
 		try {
 			if (getConn() == null) {
 				ErrorLogin(m_classReq, m_classRes);
 			} else {
 				int rowNum = 0;
 				List<String> sqlStmts = getSQLStatements(full, restrict);
 				for (String sqlStmt: sqlStmts) {
 					ps = getConn().prepareStatement(sqlStmt);
 					rs = ps.executeQuery();
 
 					ResultSetMetaData rsmd = rs.getMetaData();
 					int numColumns = rsmd.getColumnCount();
 
 					ArrayList<String> columnTypes = (ArrayList<String>)m_classReq.getSession().getAttribute("types");
 					HashMap<String,ArrayList<String[]>> typeMap = (HashMap<String, ArrayList<String[]>>)m_classReq.getSession().getAttribute("typeMap");
 
 					while (rs.next()) {
 						String[] row = new String[numColumns];
 						HashMap<String,List<String[]>> typeArrayData = null;
 
 						for (int i=0; i<numColumns; i++) {
 							if (columnTypes.get(i).endsWith("_T")) {
 								List<String[]> rowArrayData = getRowArrayData(rs, columnTypes.get(i), i);
 
 								if (typeArrayData == null) {
 									typeArrayData = new HashMap<String,List<String[]>>();
 								}
 								typeArrayData.put(columnTypes.get(i), rowArrayData);
 							} else {
 								//GF30779 truncate timestamp
 								if(columnTypes.get(i).equalsIgnoreCase("Date")) {
 									row[i] = AdministeredItemUtil.truncateTime(rs.getString(i+1));
 								} else {
 									row[i] = rs.getString(i+1);
 								}
 								//System.out.println("rs.getString(i+1) = " + rs.getString(i+1));
 							}
 						}
 						//If there were no arrayData added, add null to keep parity with rows.
 						if (typeArrayData == null) {
 							arrayData.add(null);
 						}	
 						else {
 							arrayData.add(rowNum, typeArrayData);
 						}
 
 						rows.add(row);
 						rowNum++;
 					}
 				}
 
 				m_classReq.getSession().setAttribute("arrayData", arrayData);
 			
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (rs!=null) try{rs.close();}catch(Exception e) {}
 			if (ps!=null) try{ps.close();}catch(Exception e) {}
 		}
 
 		return rows;
 	}
 
 	private List<String[]> getRowArrayData(ResultSet rs, String columnType, int index) throws Exception{
 		List<String[]> rowArrayData = new ArrayList<String[]>();
 		Array array = null;
 		//Special case: first row has info on derivation, others on data elements
 		if (columnType.indexOf("DERIVED") > 0) {
 			Object derivedObject = rs.getObject(index+1);
 			Struct struct = (Struct) derivedObject;
 			Object[] valueStruct = struct.getAttributes();
 			//Fifth entry is the array with DE's 
 			array = (Array) valueStruct[5];
 
 			if (array != null){
 				String[] derivationInfo = new String[5];
 				for (int z = 0; z < 5; z++)
 					derivationInfo[z] =(valueStruct[z] != null)? valueStruct[z].toString(): "";
 
 					rowArrayData.add(derivationInfo);
 
 					ResultSet nestedRs = array.getResultSet(); 
 
 					while (nestedRs.next()) {
 						Struct deStruct = (Struct) nestedRs.getObject(2);
 						Object[] valueDatum = deStruct.getAttributes();
 						String[] values = new String[valueDatum.length];
 
 						for (int a = 0; a < valueDatum.length; a++) {
 							if (valueDatum[a] != null) {
 								Class c = valueDatum[a].getClass();
 								String s = c.getName();
 								values[a]= valueDatum[a].toString();	
 							} 
 						}
 						rowArrayData.add(values);
 					}
 			}
 		} else {
 			array = rs.getArray(index+1);
 			if (array != null) {
 				ResultSet nestedRs = array.getResultSet(); 
 
 				while (nestedRs.next()) {
 					Struct valueStruct = (Struct) nestedRs.getObject(2);
 					Object[] valueDatum = valueStruct.getAttributes();
 					String[] values = new String[valueDatum.length];
 					int slide = 0;
 					for (int a = 0; a < valueDatum.length; a++) {
 						if (valueDatum[a] != null) {
 							Class c = valueDatum[a].getClass();
 							String s = c.getName();
							String truncatedTimeStamp = null; //GF30799
 
 							if (c.getName().toUpperCase().contains("STRUCT")) {
 								Struct str = (Struct) valueDatum[a];
 								Object[] strValues = str.getAttributes();
 								values = new String[valueDatum.length+strValues.length-1]; 
 								slide = -1;
 								for (int b = 0; b < strValues.length; b++){
									truncatedTimeStamp = strValues[b].toString(); //begin GF30779
									System.out.println("At line 299 of CustomDownloadServlet.java" + truncatedTimeStamp);
									if (columnType.contains("VALID_VALUE") && truncatedTimeStamp != null && truncatedTimeStamp.contains(":")) {
										truncatedTimeStamp = AdministeredItemUtil.truncateTime(truncatedTimeStamp);
										System.out.println("At line 302 of CustomDownloadServlet.java" + truncatedTimeStamp);
									} //end GF30779
									values[b] = truncatedTimeStamp;
 									slide++;
 								}
 							} else {
								truncatedTimeStamp = valueDatum[a].toString(); //begin GF30779
								System.out.println("At line 309 of CustomDownloadServlet.java" + truncatedTimeStamp);
								if (columnType.contains("VALID_VALUE") && truncatedTimeStamp != null && truncatedTimeStamp.contains(":")) {
									truncatedTimeStamp = AdministeredItemUtil.truncateTime(truncatedTimeStamp);
									System.out.println("At line 312 of CustomDownloadServlet.java" + truncatedTimeStamp);
								} //end GF30779
								values[a+slide]= truncatedTimeStamp;
 							}
 
 						} else {
 							values[a]= "";
 						}	
 					}
 					rowArrayData.add(values);
 				}
 			}
 		}
 		return rowArrayData;
 	}
 
 	private List<String> getSQLStatements(boolean full, boolean restrict) {
 		List<String> sqlStmts  = new ArrayList<String>();
 		ArrayList<String> downloadIDs = (ArrayList<String>)m_classReq.getSession().getAttribute("downloadIDs");
 		String type = (String)m_classReq.getSession().getAttribute("downloadType");
 
 		String sqlStmt = null;
 		if (!full){
 			StringBuffer[] whereBuffers = getWhereBuffers(downloadIDs);
 			for (StringBuffer wBuffer: whereBuffers) {
 				sqlStmt =
 					"SELECT * FROM "+type+"_EXCEL_GENERATOR_VIEW " + "WHERE "+type+"_IDSEQ IN " +
 					" ( " + wBuffer.toString() + " )  ";
 				if (restrict) {
 					sqlStmt += " and ROWNUM <= "+GRID_MAX_DISPLAY;
 					sqlStmts.add(sqlStmt);
 					break;
 				} else {
 					sqlStmts.add(sqlStmt);
 				}
 			}
 		} else {
 			sqlStmt = "SELECT * FROM "+type+"_EXCEL_GENERATOR_VIEW";
 			if (restrict) sqlStmt += " where ROWNUM <= "+GRID_MAX_DISPLAY;
 
 			sqlStmts.add(sqlStmt);
 		}
 
 		return sqlStmts;
 	}
 
 	private StringBuffer[] getWhereBuffers(ArrayList<String> downloadIds) {
 		StringBuffer whereBuffer = null;
 		List<StringBuffer> whereBuffers = null;
 
 		if (downloadIds.size() <= 1000 ){ //make sure that there are no more than 1000 ids in each 'IN' clause
 			whereBuffer = new StringBuffer();
 			for (String id:downloadIds) {
 				whereBuffer.append("'" + id + "',");
 			}
 			whereBuffer.deleteCharAt(whereBuffer.length()-1);
 		} else {
 			whereBuffers = new ArrayList<StringBuffer>();
 			int counter = 0;
 			whereBuffer = new StringBuffer();
 
 			for (String id:downloadIds) {
 				whereBuffer.append("'" + id + "',");
 
 				counter++;
 
 				if (counter%1000 == 0) {
 					whereBuffer.deleteCharAt(whereBuffer.length()-1);
 					whereBuffers.add(whereBuffer);
 					whereBuffer = new StringBuffer();
 				}
 			}
 
 			// add the final chunk to the list
 			if (whereBuffer.length()>0) {
 				whereBuffer.deleteCharAt(whereBuffer.length()-1);
 				whereBuffers.add(whereBuffer);
 			}
 		}
 
 		if (whereBuffers == null) {
 			whereBuffers = new ArrayList<StringBuffer>(1);
 			whereBuffers.add(whereBuffer);
 		}
 
 		return whereBuffers.toArray(new StringBuffer[0]);
 	}
 
 	private void setColHeadersAndTypes(String type)  {
 		ArrayList<String> columnHeaders = new ArrayList<String>();
 		ArrayList<String> columnTypes = new ArrayList<String>();
 		HashMap<String,ArrayList<String[]>> typeMap = new HashMap<String,ArrayList<String[]>>();
 		HashMap<String,String> arrayColumnTypes = new HashMap<String,String>();
 		ArrayList<String> allExpandedColumnHeaders = new ArrayList<String>();
 		Vector vList = new Vector();
 		String sList = new String();
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		
 		GetACService getAC = new GetACService(this.m_classReq, m_classRes, this);
 		if (vList == null || vList.size()<1)
 	      {
 	        vList = getAC.getToolOptionData("CURATION", "CUSTOM.COLUMN.EXCLUDED", "");
 	      
 			if (vList != null && vList.size()>0)
 	        {
 	          TOOL_OPTION_Bean tob = (TOOL_OPTION_Bean)vList.elementAt(0);
 	          if (tob != null) sList = tob.getVALUE();
 	        }
 	      }
 		
 		if (sList == "")
 			sList = "CDE_IDSEQ,DEC_IDSEQ,VD_IDSEQ,Conceptual Domain Public ID,Conceptual Domain Short Name,Conceptual Domain Version,Conceptual Domain Context Name";
 		
 		ArrayList<String> excluded = new ArrayList<String>();
 		
 		for (String col: sList.split(",")){
 			excluded.add(col);
 		}
 		
 		
 		try {
 			String qry = "SELECT * FROM "+type+"_EXCEL_GENERATOR_VIEW where 1=2";
 			ps = getConn().prepareStatement(qry);
 			rs = ps.executeQuery();
 			ResultSetMetaData rsmd = rs.getMetaData();
 
 			int numColumns = rsmd.getColumnCount();
 			// Get the column names and types; column indices start from 1
 			for (int i=1; i<numColumns+1; i++) {
 				String columnName = rsmd.getColumnName(i);
 				columnName = prettyName(columnName);
 				columnHeaders.add(columnName);
 
 				String columnType = rsmd.getColumnTypeName(i);
 
 				if (columnType.endsWith("_T") && !typeMap.containsKey(columnType)) {
 					String typeKey = i+":"+columnType;
 
 					columnTypes.add(typeKey);
 					ArrayList<String[]> typeBreakdown = getType(typeKey, columnName, type);
 					typeMap.put(i+":"+columnType,typeBreakdown);
 
 					if (typeBreakdown.size() >0) {
 						String[] typeColNames = typeBreakdown.get(0);
 						
 						String[] orderedTypeColNames = getOrderedTypeNames(typeKey, columnName,type);
 						for (int c = 0; c<orderedTypeColNames.length; c++) {
 							arrayColumnTypes.put(typeColNames[c], typeKey);  // 2 lists should be same length.
 							allExpandedColumnHeaders.add(orderedTypeColNames[c]);  //Adding sorted list to the display list
 						}
 					} else allExpandedColumnHeaders.add(columnName);
 
 				} else {
 					columnTypes.add(columnType);
 					allExpandedColumnHeaders.add(columnName);
 				}
 
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (rs!=null) try{rs.close();}catch(Exception e) {}
 			if (ps!=null) try{ps.close();}catch(Exception e) {}
 		}
 
 		m_classReq.getSession().setAttribute("excludedHeaders",excluded);
 		m_classReq.getSession().setAttribute("headers",columnHeaders);
 		m_classReq.getSession().setAttribute("allExpandedHeaders",allExpandedColumnHeaders);
 		m_classReq.getSession().setAttribute("types", columnTypes);
 		m_classReq.getSession().setAttribute("typeMap", typeMap);
 		m_classReq.getSession().setAttribute("arrayColumnTypes", arrayColumnTypes);
 	}
 
 	//Due to Col Name limit to 30 chars, we need to expand the names once we get them.
 	private String prettyName(String name) {
 
 		if (name.startsWith("DE "))
 			return name.replace("DE ", "Data Element ");
 		else if (name.startsWith("DEC "))
 			return name.replace("DEC ", "Data Element Concept ");
 		else if (name.startsWith("VD "))
 			return name.replace("VD ", "Value Domain ");
 		else if (name.startsWith("OC "))
 			return name.replace("OC ", "Object Class ");
 		else if (name.startsWith("CD "))
 			return name.replace("CD ", "Conceptual Domain ");
 
 		return name;
 	}
 
 	private ArrayList<String[]> getType(String type, String name, String download) {
 
 		ArrayList<String[]> colNamesAndTypes = new ArrayList<String[]>();
 
 		ArrayList<String> attrName = new ArrayList<String>();
 		ArrayList<String> attrTypeName = new ArrayList<String>();
 
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		String sqlStmt = "select * from sbrext.custom_download_types c where UPPER(c.type_name) = ? order by c.column_index";
 		String[] splitType = type.split("\\.");
 
 		type = splitType[1];
 
 		try {
 			ps = getConn().prepareStatement(sqlStmt);
 			ps.setString(1, type);
 			rs = ps.executeQuery();
 			int i = 0;		
 			while (rs.next()) {
 				i++;
 				String col = rs.getString("DISPLAY_NAME");
 				String ctype = rs.getString("DISPLAY_TYPE");
 				if (type.toUpperCase().contains("CONCEPT")) {
 					if (name.toUpperCase().startsWith("REP"))
 						name = "Representation Concept";
 					else if (name.toUpperCase().startsWith("VD"))
 						name = "Value Domain Concept";
 					else if (name.toUpperCase().startsWith("OC"))
 						name = "Object Class Concept";
 					else if (name.startsWith("PROP"))
 						name = "Property Concept";
 
 					col = name+" "+col;
 				}
 				if (type.toUpperCase().contains("DESIGNATION")) {
 					if (download.equals("CDE"))
 						download = "Data Element";
 					else if (download.equals("VD"))
 						download = "Value Domain";
 					else if (download.equals("DEC"))
 						download = "Data Element Concept";
 					
 					col = download+" "+col;
 				}
 
 				attrName.add(col);
 				attrTypeName.add(ctype);
 			}
 			//System.out.println(type + " "+i);
 			rs.close();
 			ps.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		String[] attrNames = new String[attrName.size()];
 		String[] attrTypeNames = new String[attrTypeName.size()];
 
 		for (int i=0; i < attrName.size(); i++) {
 			attrNames[i] = attrName.get(i);
 			attrTypeNames[i] = attrTypeName.get(i);
 		}
 		colNamesAndTypes.add(attrNames);
 		colNamesAndTypes.add(attrTypeNames);
 
 		return colNamesAndTypes;
 	}
 
 	private String[] getOrderedTypeNames(String type, String name, String download) {
 
 		ArrayList<String> attrName = new ArrayList<String>();
 
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		String sqlStmt = "select * from sbrext.custom_download_types c where UPPER(c.type_name) = ? order by c.display_column_index";
 		String[] splitType = type.split("\\.");
 
 		type = splitType[1];
 
 		try {
 			ps = getConn().prepareStatement(sqlStmt);
 			ps.setString(1, type);
 			rs = ps.executeQuery();
 			int i = 0;		
 			while (rs.next()) {
 				i++;
 				String col = rs.getString("DISPLAY_NAME");
 				String ctype = rs.getString("DISPLAY_TYPE");
 				if (type.toUpperCase().contains("CONCEPT")) {
 					if (name.toUpperCase().startsWith("REP"))
 						name = "Representation Concept";
 					else if (name.toUpperCase().startsWith("VD"))
 						name = "Value Domain Concept";
 					else if (name.toUpperCase().startsWith("OC"))
 						name = "Object Class Concept";
 					else if (name.startsWith("PROP"))
 						name = "Property Concept";
 
 					col = name+" "+col;
 				}
 				if (type.toUpperCase().contains("DESIGNATION")) {
 					if (download.equals("CDE"))
 						download = "Data Element";
 					else if (download.equals("VD"))
 						download = "Value Domain";
 					else if (download.equals("DEC"))
 						download = "Data Element Concept";
 					
 					col = download+" "+col;
 				}
 
 				attrName.add(col);
 			}
 			//System.out.println(type + " "+i);
 			rs.close();
 			ps.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		String[] attrNames = new String[attrName.size()];
 		
 		for (int i=0; i < attrName.size(); i++) {
 			attrNames[i] = attrName.get(i);
 		
 		}
 		return attrNames;
 
 	}
 
 
 	private void returnJSONFromSession(String JSPName) {
 		ArrayList<String[]> displayRows = getRecords(false, true);
 		m_classReq.getSession().setAttribute("rows", displayRows);
 		ForwardJSP(m_classReq, m_classRes, "/JSON"+JSPName+".jsp");
 	}
 
 	private void createXMLDownload(ArrayList<String[]> allRows) {
 		//Limited columns?  If xmlColumns is not null
 		//Setup columns
 		String colString = (String) this.m_classReq.getParameter("cdlColumns");
 		ArrayList<String> allHeaders = (ArrayList<String>) m_classReq.getSession().getAttribute("headers");
 		ArrayList<String> allExpandedHeaders = (ArrayList<String>) m_classReq.getSession().getAttribute("allExpandedHeaders");
 		ArrayList<String> allTypes = (ArrayList<String>) m_classReq.getSession().getAttribute("types");
 		HashMap<String, String> arrayColumnTypes = (HashMap<String,String>) m_classReq.getSession().getAttribute("arrayColumnTypes");
 		HashMap<String,ArrayList<String[]>> typeMap = (HashMap<String,ArrayList<String[]>>) m_classReq.getSession().getAttribute("typeMap");
 		ArrayList<HashMap<String,ArrayList<String[]>>> arrayData = (ArrayList<HashMap<String,ArrayList<String[]>>>) m_classReq.getSession().getAttribute("arrayData");
 		String downloadType = (String)m_classReq.getSession().getAttribute("downloadType");
 
 		String[] columns = null;
 		if (xmlColumns != null && !xmlColumns.trim().equals("")) {
 			columns = xmlColumns.split(",");
 		}
 		else if (colString != null && !colString.trim().equals("")){
 			columns = colString.split(",");
 		} else {
 			//Different from Excel.  Handling of nested columns is different
 			columns = allHeaders.toArray(new String[allHeaders.size()]);
 		}
 
 		int[] colIndices = new int[columns.length];
 		for (int i=0; i < columns.length; i++) {
 			String colName = columns[i];
 			if (allHeaders.indexOf(colName) < 0){
 				String tempType = arrayColumnTypes.get(colName);
 				int temp = allTypes.indexOf(tempType);
 				colIndices[i]=temp;
 			} else {
 				int temp = allHeaders.indexOf(colName);
 				colIndices[i]=temp;
 			}
 		}
 
 		Document dom = null;
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		try {
 			//get an instance of builder
 			DocumentBuilder db = dbf.newDocumentBuilder();
 
 			//create an instance of DOM
 			dom = db.newDocument();
 
 			String colName = "Element";
 			
 			if (downloadType.toUpperCase().equals("CDE"))
 				colName = "DataElement";
 			else if (downloadType.toUpperCase().equals("DEC"))
 				colName = "DataElementConcept";
 			else if (downloadType.toUpperCase().equals("VD"))
 				colName = "ValueDomain";
 			
 			Element rootEle = dom.createElement(colName+"sList");
 			dom.appendChild(rootEle);
 
 
 			for(int i = 0; i < allRows.size(); i++) {
 
 				String[] row = allRows.get(i);
 				//For each row create approppriate element and attach it to root, pass all the column/type data to do this
 				Element deElement = createElement(row, i, dom, columns, colIndices, allTypes, typeMap, arrayData, colName);
 				rootEle.appendChild(deElement);
 			}
 
 
 			OutputFormat format = new OutputFormat(dom);
 			format.setIndenting(true);
 
 			//generate output
 			XMLSerializer serializer = new XMLSerializer();
 
 			m_classRes.setContentType( "text/xml" );
 			m_classRes.setHeader( "Content-Disposition", "attachment; filename=\"customDownload.xml\"" );
 
 			OutputStream out = m_classRes.getOutputStream();
 
 			serializer.setOutputByteStream(out);  
 			serializer.setOutputFormat(format);
 			serializer.serialize(dom);
 
 			out.close();     
 
 		}catch(ParserConfigurationException pce) {
 			//dump it
 			System.out.println("Error while trying to instantiate DocumentBuilder " + pce);
 			pce.printStackTrace();
 		} catch (IOException ioe){
 			System.out.println("Error while trying to serialize  " + ioe);
 			ioe.printStackTrace();
 		} catch (Exception e) {
 			System.out.println("Error  " + e);
 			e.printStackTrace();
 		
 		}
 
 	}
 
 	private Element createElement(String[] row, int rowNumber, Document dom, String[] columns, int[] colIndices, 
 			ArrayList<String> allTypes, HashMap<String,ArrayList<String[]>> typeMap, ArrayList<HashMap<String,ArrayList<String[]>>> arrayData, String elementType){
 
 
 		Element deElement = dom.createElement(elementType);
 		deElement.setAttribute("num",Integer.toString(rowNumber+1));
 		String oldType = "";
 		Element collectionElement = null;
 		for (int j = 0; j < colIndices.length; j++) {
 
 			Element elem = null;
 			String currentType = allTypes.get(colIndices[j]);
 			
 			if (currentType.endsWith("_T"))
 			{
 				//Create collection only if new one
 				if (!currentType.equals(oldType)){
 					if (oldType.length() > 0)
 						deElement.appendChild(collectionElement);
 					oldType = currentType;
 					String collectionName = "";
 					String colNameArr[] = columns[j].split(" ");
 					int dropNumber = 1;
 					if (currentType.endsWith("ID"))
 						dropNumber = 2;
 					for (int nameIndex = 0; nameIndex < colNameArr.length-dropNumber; nameIndex++ ){
 						collectionName = collectionName+colNameArr[nameIndex];
 					}
 					collectionName = collectionName+"LIST";
 					collectionElement = dom.createElement(collectionName);
 				}
 				//Deal with CS/CSI
 				String[] originalArrColNames = typeMap.get(currentType).get(0);
 
 				//Find current column in original data
 
 				int originalColumnIndex = -1;
 				for (int a = 0; a < originalArrColNames.length ; a++) { 
 					if (columns[j].equals(originalArrColNames[a])){
 						originalColumnIndex = a;
 						break;
 					}
 				}
 
 				HashMap<String,ArrayList<String[]>> typeArrayData = arrayData.get(rowNumber);
 				ArrayList<String[]> rowArrayData = typeArrayData.get(currentType);
 
 				if (rowArrayData != null) {
 					for (int nestedRowIndex = 0; nestedRowIndex < rowArrayData.size(); nestedRowIndex++) {
 						//Get subType column names and iterate over those and create nested elements
 						Element nestedElement = dom.createElement(columns[j].replace(" ", ""));
 						//Add element and data close element
 						String[] nestedData = rowArrayData.get(nestedRowIndex);
 						String data = "";
 						if (currentType.contains("DERIVED")) {
 							//Derived data element is special double nested, needs to be modified to be more general.
 							
 							//General DDE information is in the first 4 columns, but contained in the first row of the Row Array Data
 							if (originalColumnIndex < 4) {
 								if (nestedRowIndex == 0)
 									data = (originalColumnIndex > 0)? nestedData[originalColumnIndex]:nestedData[originalColumnIndex+1];  //This skips the 2nd entry, description, which is not to be shown.
 									nestedElement.setTextContent(data);
 									collectionElement.appendChild(nestedElement);
 							} else {
 								if (nestedRowIndex+1 < rowArrayData.size()){
 									data = rowArrayData.get(nestedRowIndex+1)[originalColumnIndex-4];
 									nestedElement.setTextContent(data);
 									collectionElement.appendChild(nestedElement);
 								}
 							}						
 						}else {
 							data = nestedData[originalColumnIndex];
 							nestedElement.setTextContent(data);
 							collectionElement.appendChild(nestedElement);
 						}
 					}
 				}
 				
 			} else {
 				//Add element and Data, close element
 				elem = dom.createElement(columns[j].replace(" ", ""));
 				elem.setTextContent(row[colIndices[j]]);
 				deElement.appendChild(elem);
 			}
 		}
 
 		return deElement;
 
 	}
 
 	private void createDownloadColumns(ArrayList<String[]> allRows){
 		final int MAX_ROWS = 65000;
 
 		String sheetName = "Custom Download";
 		int sheetNum = 1;
 
 		String colString = (String) this.m_classReq.getParameter("cdlColumns");
 		String fillIn = (String) this.m_classReq.getParameter("fillIn");
 
 		ArrayList<String> allHeaders = (ArrayList<String>) m_classReq.getSession().getAttribute("headers");
 		ArrayList<String> allExpandedHeaders = (ArrayList<String>) m_classReq.getSession().getAttribute("allExpandedHeaders");
 		ArrayList<String> allTypes = (ArrayList<String>) m_classReq.getSession().getAttribute("types");
 		HashMap<String,ArrayList<String[]>> typeMap = (HashMap<String,ArrayList<String[]>>) m_classReq.getSession().getAttribute("typeMap");
 		ArrayList<HashMap<String,ArrayList<String[]>>> arrayData = (ArrayList<HashMap<String,ArrayList<String[]>>>) m_classReq.getSession().getAttribute("arrayData"); 
 		HashMap<String, String> arrayColumnTypes = (HashMap<String,String>) m_classReq.getSession().getAttribute("arrayColumnTypes");
 
 		String[] columns = null;
 		if (colString != null && !colString.trim().equals("")) {
 			columns = colString.split(",");
 		}
 		else {
 			ArrayList<String> defaultHeaders = new ArrayList<String>();
 			
 			for (String cName: allExpandedHeaders){
 				if (cName.endsWith("IDSEQ") || cName.startsWith("CD ") || cName.startsWith("Conceptual Domain"))
 					{ /*skip*/ }
 				else {
 					System.out.println("cName = " + cName);
 					defaultHeaders.add(cName);
 				}
 			}
 			columns = defaultHeaders.toArray(new String[defaultHeaders.size()]);	
 	
 		}
 
 		int[] colIndices = new int[columns.length];
 		for (int i=0; i < columns.length; i++) {
 			String colName = columns[i];
 			if (allHeaders.indexOf(colName) < 0){
 				String tempType = arrayColumnTypes.get(colName);
 				int temp = allTypes.indexOf(tempType);
 				colIndices[i]=temp;
 			} else {
 				int temp = allHeaders.indexOf(colName);
 				colIndices[i]=temp;
 			}
 		}
 
 		Workbook wb =  new HSSFWorkbook();
 
 		Sheet sheet = wb.createSheet(sheetName);
 		Font font = wb.createFont(); //GF30779
 		font.setBoldweight(Font.BOLDWEIGHT_BOLD); //GF30779
 		CellStyle boldCellStyle = wb.createCellStyle(); //GF30779
 		boldCellStyle.setFont(font); //GF30779
 		boldCellStyle.setAlignment(CellStyle.ALIGN_GENERAL); //GF30779
 
 		Row headerRow = sheet.createRow(0);
 		headerRow.setHeightInPoints(12.75f);
 		String temp;
 		for (int i = 0; i < columns.length; i++) {
 			Cell cell = headerRow.createCell(i);
 			temp = columns[i];
 			cell.setCellValue(temp);
 			cell.setCellStyle(boldCellStyle); //GF30779
 		}
 
 		//freeze the first row
 		sheet.createFreezePane(0, 1);
 
 		Row row = null;
 		Cell cell;
 		int rownum = 1;
 		int bump = 0;
 		boolean fillRow = false;
 		int i = 0;
 		long startTime = System.currentTimeMillis();
 		try {
 			System.out.println("Total CDEs to download ["+allRows.size()+"]");
 			for (i = 0; i < allRows.size(); i++, rownum++) {
 				//Check if row already exists
 				int maxBump = 0;
 				if (sheet.getRow(rownum+bump) == null) {
 					row = sheet.createRow(rownum+bump);	
 				}
 
 				if(allRows.get(i) == null) continue;
 
 				for (int j = 0; j < colIndices.length; j++) {
 
 					cell = row.createCell(j);
 					String currentType = allTypes.get(colIndices[j]);
 					if (currentType.endsWith("_T"))
 					{
 						//Deal with CS/CSI
 						String[] originalArrColNames = typeMap.get(currentType).get(0);
 
 						//Find current column in original data
 
 						int originalColumnIndex = -1;
 						for (int a = 0; a < originalArrColNames.length ; a++) { 
 							if (columns[j].equals(originalArrColNames[a])){
 								originalColumnIndex = a;
 								break;
 							}
 						}
 
 						HashMap<String,ArrayList<String[]>> typeArrayData = arrayData.get(i);
 						ArrayList<String[]> rowArrayData = typeArrayData.get(currentType);
 
 						if (rowArrayData != null ) {
 							int tempBump = 0;
 							for (int nestedRowIndex = 0; nestedRowIndex < rowArrayData.size(); nestedRowIndex++) {
 
 								String[] nestedData = rowArrayData.get(nestedRowIndex);
 								String data = "";
 								if (currentType.contains("DERIVED")) {
 									//Derived data element is special double nested, needs to be modified to be more general.
 									
 									//General DDE information is in the first 4 columns, but contained in the first row of the Row Array Data
 									if (originalColumnIndex < 5) {
 										if (nestedRowIndex == 0)
 											data = (originalColumnIndex > 0)? nestedData[originalColumnIndex]:nestedData[originalColumnIndex+1];  //This skips the 2nd entry, description, which is not to be shown.
 									} else {
 										if (nestedRowIndex+1 < rowArrayData.size()){
 											data = rowArrayData.get(nestedRowIndex+1)[originalColumnIndex-5];
 										}
 									}
 										
 										
 								}else 
 									data = nestedData[originalColumnIndex];
 								System.out.println("at line 947 of CustomDownloadServlet.java*****"+ data + currentType);
								if (currentType.contains("VALID_VALUE")) { //GF30779
 									data = AdministeredItemUtil.truncateTime(data);
 								}
 								cell.setCellValue(data);
 
 								tempBump++;
 
 								if (nestedRowIndex < rowArrayData.size()-1){
 									row = sheet.getRow(rownum+bump+tempBump);
 									if (row == null) {
 										if (rownum+bump+tempBump >= MAX_ROWS) {
 											sheet = wb.createSheet(sheetName+"_"+sheetNum);
 											sheetNum++;
 											rownum = 1;
 											bump = 0;
 											tempBump = 0;
 										}
 										row = sheet.createRow(rownum+bump+tempBump);
 									}
 
 									cell = row.createCell(j);
 
 								} else {
 									//Go back to top row 
 									row = sheet.getRow(rownum + bump);
 									if (tempBump > maxBump)
 										maxBump = tempBump;
 								}
 							}
 						}
 					} else {
 						temp = allRows.get(i)[colIndices[j]];
 						System.out.println("at line 980 of CustomDownloadServlet.java*****"+ temp + currentType);
 						if (currentType.equalsIgnoreCase("Date")) { //GF30779
 							temp = AdministeredItemUtil.truncateTime(temp);
 						}
 						cell.setCellValue(temp);
 					}
 
 				}
 
 				bump = bump + maxBump;
 
 				if (fillIn != null && (fillIn.equals("true") || fillIn.equals("yes") && bump > 0)) {
 					sheet = fillInBump(sheet, i, rownum, bump, allRows, allTypes, colIndices);
 					rownum = rownum + bump;
 					bump = 0;
 				}
 			}
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 
 //		sheet.setZoom(3, 4); //GF30779
 
 
 		// Write the output to response stream.
 		try {
 			m_classRes.setContentType( "application/vnd.ms-excel" );
 			m_classRes.setHeader( "Content-Disposition", "attachment; filename=\"customDownload.xls\"" );
 
 			OutputStream out = m_classRes.getOutputStream();
 			wb.write(out);
 			out.close();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}        
 	}
 
 	private String getNestedData(ArrayList<String[]> rowArrayData, int originalColumnIndex,
 			String currentType, int nestedRowIndex) {
 		String ret = "";
 		String[] nestedData = rowArrayData.get(nestedRowIndex);
 		if (currentType.contains("DERIVED"))
 			{
 				if (originalColumnIndex > 4)
 					nestedData = rowArrayData.get(nestedRowIndex+1);
 				
 				ret = nestedData[originalColumnIndex-5];
 			}
 		else 
 			ret = nestedData[originalColumnIndex];
 		return ret;
 	}
 
 	private Sheet fillInBump(Sheet sheet, int originalRow, int rownum, int bump, ArrayList<String[]> allRows, ArrayList<String> allTypes, int[] colIndices) {
 		String temp = null;
 		for (int a = rownum; a < rownum+bump; a++) {
 			Row row = sheet.getRow(a);
 
 			for (int j = 0; j < colIndices.length; j++) {
 
 
 				String currentType = allTypes.get(colIndices[j]);
 				if (currentType.endsWith("_T"))
 				{
 					//Do nothing
 				} else {
 					Cell cell = row.createCell(j);
 					temp = allRows.get(originalRow)[colIndices[j]];
 					System.out.println("at line 1048 of CustomDownloadServlet.java*****"+ temp + currentType);
 					if (currentType.equalsIgnoreCase("Date")) { //GF30779
 						temp = AdministeredItemUtil.truncateTime(temp);
 					}
 					cell.setCellValue(temp);
 				}
 
 			}
 		}
 		return sheet;
 	}
 	private void createDownload() {
 
 
 		ForwardJSP(m_classReq, m_classRes, "/DownloadComplete.jsp");
 	}
 
 
 
 	//initiate the column information as per the source
 	private List initColumnInfo() {
 		List<ColumnInfo> columnInfo = new ArrayList<ColumnInfo>();
 
 		columnInfo.add(
 				new ColumnInfo("PREFERRED_NAME", "Data Element Short Name", "String"));
 		columnInfo.add(
 				new ColumnInfo("LONG_NAME", "Data Element Long Name", "String"));
 		columnInfo.add(
 				new ColumnInfo("DOC_TEXT", "Data Element Preferred Question Text", "String"));
 		columnInfo.add(
 				new ColumnInfo(
 						"PREFERRED_DEFINITION", "Data Element Preferred Definition", "String"));
 		columnInfo.add(new ColumnInfo("VERSION", "Data Element Version", "String"));
 		columnInfo.add(
 				new ColumnInfo("DE_CONTE_NAME", "Data Element Context Name", "String"));
 		columnInfo.add(
 				new ColumnInfo(
 						"DE_CONTE_VERSION", "Data Element Context Version", "Number"));
 		columnInfo.add(
 				new ColumnInfo("CDE_ID", "Data Element Public ID", "Number"));    
 		////The deSearch condition is added for the new version of excel files
 
 		columnInfo.add(
 				new ColumnInfo("DE_WK_FLOW_STATUS", "Data Element Workflow Status", "String"));
 		columnInfo.add(
 				new ColumnInfo("REGISTRATION_STATUS", "Data Element Registration Status", "Number"));
 		columnInfo.add(new ColumnInfo("BEGIN_DATE", "Data Element Begin Date", "Date"));
 		columnInfo.add(new ColumnInfo("ORIGIN", "Data Element Source", "String"));
 
 
 		//data element concept
 		columnInfo.add(
 				new ColumnInfo("DEC_ID", "Data Element Concept Public ID", "Number"));
 		columnInfo.add(
 				new ColumnInfo(
 						"DEC_PREFERRED_NAME", "Data Element Concept Short Name", "String"));
 		columnInfo.add(
 				new ColumnInfo(
 						"DEC_LONG_NAME", "Data Element Concept Long Name", "String"));
 		columnInfo.add(
 				new ColumnInfo("DEC_VERSION", "Data Element Concept Version", "Number"));
 		columnInfo.add(
 				new ColumnInfo(
 						"DEC_CONTE_NAME", "Data Element Concept Context Name", "String"));
 		columnInfo.add(
 				new ColumnInfo(
 						"DEC_CONTE_VERSION", "Data Element Concept Context Version", "Number"));
 
 		//object class concept
 		columnInfo.add(new ColumnInfo("OC_ID", "Object Class Public ID", "String"));
 		columnInfo.add(
 				new ColumnInfo("OC_LONG_NAME", "Object Class Long Name", "String"));
 		columnInfo.add(
 				new ColumnInfo(
 						"OC_PREFERRED_NAME", "Object Class Short Name", "String"));
 		columnInfo.add(
 				new ColumnInfo("OC_CONTE_NAME", "Object Class Context Name", "String"));
 		columnInfo.add(
 				new ColumnInfo("OC_VERSION", "Object Class Version", "String"));
 
 		List<ColumnInfo> ocConceptInfo = new ArrayList<ColumnInfo>();
 		ocConceptInfo.add(new ColumnInfo(1, "Name"));
 		ocConceptInfo.add(new ColumnInfo(0, "Code"));
 		ocConceptInfo.add(new ColumnInfo(2, "Public ID", "Number"));
 		ocConceptInfo.add(new ColumnInfo(3, "Definition Source"));    
 		ocConceptInfo.add(new ColumnInfo(5, "EVS Source"));
 		ocConceptInfo.add(new ColumnInfo(6, "Primary Flag"));
 
 		ColumnInfo ocConcepts =
 			new ColumnInfo("oc_concepts", "Object Class Concept ", "Array");
 		ocConcepts.nestedColumns = ocConceptInfo;
 		columnInfo.add(ocConcepts);
 
 		//property concept
 		columnInfo.add(new ColumnInfo("PROP_ID", "Property Public ID", "String"));
 		columnInfo.add(
 				new ColumnInfo("PROP_LONG_NAME", "Property Long Name", "String"));
 		columnInfo.add(
 				new ColumnInfo(
 						"PROP_PREFERRED_NAME", "Property Short Name", "String"));
 		columnInfo.add(
 				new ColumnInfo("PROP_CONTE_NAME", "Property Context Name", "String"));
 		columnInfo.add(
 				new ColumnInfo("PROP_VERSION", "Property Version", "String"));
 
 		List<ColumnInfo> propConceptInfo = new ArrayList<ColumnInfo>();
 		propConceptInfo.add(new ColumnInfo(1, "Name"));
 		propConceptInfo.add(new ColumnInfo(0, "Code"));
 		propConceptInfo.add(new ColumnInfo(2, "Public ID", "Number"));
 		propConceptInfo.add(new ColumnInfo(3, "Definition Source"));
 		propConceptInfo.add(new ColumnInfo(5, "EVS Source"));
 		propConceptInfo.add(new ColumnInfo(6, "Primary Flag"));
 
 		ColumnInfo propConcepts =
 			new ColumnInfo("prop_concepts", "Property Concept ", "Array");
 		propConcepts.nestedColumns = propConceptInfo;
 		columnInfo.add(propConcepts);
 
 		//value domain
 		columnInfo.add(new ColumnInfo("VD_ID", "Value Domain Public ID", "Number"));
 		columnInfo.add(
 				new ColumnInfo(
 						"VD_PREFERRED_NAME", "Value Domain Short Name", "String"));
 		columnInfo.add(
 				new ColumnInfo("VD_LONG_NAME", "Value Domain Long Name", "String"));
 		columnInfo.add(
 				new ColumnInfo("VD_VERSION", "Value Domain Version", "Number"));
 		columnInfo.add(
 				new ColumnInfo("VD_CONTE_NAME", "Value Domain Context Name", "String"));
 		columnInfo.add(
 				new ColumnInfo(
 						"VD_CONTE_VERSION", "Value Domain Context Version", "Number"));
 		columnInfo.add(new ColumnInfo("VD_TYPE", "Value Domain Type", "String"));
 		columnInfo.add(
 				new ColumnInfo("DTL_NAME", "Value Domain Datatype", "String"));
 		columnInfo.add(
 				new ColumnInfo("MIN_LENGTH_NUM", "Value Domain Min Length", "Number"));
 		columnInfo.add(
 				new ColumnInfo("MAX_LENGTH_NUM", "Value Domain Max Length", "Number"));
 		columnInfo.add(
 				new ColumnInfo("LOW_VALUE_NUM", "Value Domain Min Value", "Number"));
 		columnInfo.add(
 				new ColumnInfo("HIGH_VALUE_NUM", "Value Domain Max Value", "Number"));
 		columnInfo.add(
 				new ColumnInfo("DECIMAL_PLACE", "Value Domain Decimal Place", "Number"));
 		columnInfo.add(
 				new ColumnInfo("FORML_NAME", "Value Domain Format", "String"));
 
 		//Value Domain Concept
 		List<ColumnInfo> vdConceptInfo = new ArrayList<ColumnInfo>();
 		vdConceptInfo.add(new ColumnInfo(1, "Name"));
 		vdConceptInfo.add(new ColumnInfo(0, "Code"));
 		vdConceptInfo.add(new ColumnInfo(2, "Public ID", "Number"));
 		vdConceptInfo.add(new ColumnInfo(3, "Definition Source"));
 		vdConceptInfo.add(new ColumnInfo(5, "EVS Source"));
 		vdConceptInfo.add(new ColumnInfo(6, "Primary Flag"));
 
 		ColumnInfo vdConcepts =
 			new ColumnInfo("vd_concepts", "Value Domain Concept ", "Array");
 		vdConcepts.nestedColumns = vdConceptInfo;
 		columnInfo.add(vdConcepts);    
 		//representation concept
 		//The deSearch condition is added to support both the old and the new version of excel files
 
 		columnInfo.add(new ColumnInfo("REP_ID", "Representation Public ID", "String"));
 		columnInfo.add(
 				new ColumnInfo("REP_LONG_NAME", "Representation Long Name", "String"));
 		columnInfo.add(
 				new ColumnInfo(
 						"REP_PREFERRED_NAME", "Representation Short Name", "String"));
 		columnInfo.add(
 				new ColumnInfo("REP_CONTE_NAME", "Representation Context Name", "String"));
 		columnInfo.add(
 				new ColumnInfo("REP_VERSION", "Representation Version", "String"));
 
 		List<ColumnInfo> repConceptInfo = new ArrayList<ColumnInfo>();
 		repConceptInfo.add(new ColumnInfo(1, "Name"));
 		repConceptInfo.add(new ColumnInfo(0, "Code"));
 		repConceptInfo.add(new ColumnInfo(2, "Public ID", "Number"));
 		repConceptInfo.add(new ColumnInfo(3, "Definition Source"));
 		repConceptInfo.add(new ColumnInfo(5, "EVS Source"));
 		repConceptInfo.add(new ColumnInfo(6, "Primary Flag"));
 
 		ColumnInfo repConcepts =
 			new ColumnInfo("rep_concepts", "Representation Concept ", "Array");
 		repConcepts.nestedColumns = repConceptInfo;
 		columnInfo.add(repConcepts);
 
 
 		//Valid Value
 		List<ColumnInfo> validValueInfo = new ArrayList<ColumnInfo>();
 		validValueInfo.add(new ColumnInfo(0, "Valid Values"));
 		//The deSearch condition is added to support both the (3.2.0.1) old and the (3.2.0.2)new version of excel files
 
 		validValueInfo.add(new ColumnInfo(1, "Value Meaning Name"));
 		validValueInfo.add(new ColumnInfo(2, "Value Meaning Description"));
 		validValueInfo.add(new ColumnInfo(3, "Value Meaning Concepts"));
 		//*	Added for 4.0	
 		validValueInfo.add(new ColumnInfo(4, "PVBEGINDATE","PV Begin Date", "Date"));
 		validValueInfo.add(new ColumnInfo(5, "PVENDDATE","PV End Date", "Date"));
 		validValueInfo.add(new ColumnInfo(6, "VMPUBLICID", "Value Meaning PublicID", "Number"));
 		validValueInfo.add(new ColumnInfo(7, "VMVERSION", "Value Meaning Version", "Number"));
 		//	Added for 4.0	*/
 
 		ColumnInfo validValue = new ColumnInfo("VALID_VALUES", "", "Array");
 		validValue.nestedColumns = validValueInfo;
 		columnInfo.add(validValue);
 
 		//Classification Scheme
 		List<ColumnInfo> csInfo = new ArrayList<ColumnInfo>();
 
 		csInfo.add(new ColumnInfo(0, 3, "Preferred Name", "String"));
 
 		//}
 		csInfo.add(new ColumnInfo(0, 4, "Version","Number"));
 		csInfo.add(new ColumnInfo(0, 1, "Context Name", "String"));
 		csInfo.add(new ColumnInfo(0, 2, "Context Version","Number"));
 		csInfo.add(new ColumnInfo(1, "Item Name"));
 		csInfo.add(new ColumnInfo(2, "Item Type Name"));
 		//	Added for 4.0 
 		csInfo.add(new ColumnInfo(3, "CsiPublicId","Item Public Id", "Number"));
 		csInfo.add(new ColumnInfo(4, "CsiVersion","Item Version", "Number"));
 		//	Added for 4.0	
 		ColumnInfo classification =
 			new ColumnInfo("CLASSIFICATIONS", "Classification Scheme ", "Array");
 		classification.nestedColumns = csInfo;
 		columnInfo.add(classification);
 
 		//Alternate name
 		List<ColumnInfo> altNameInfo = new ArrayList<ColumnInfo>();
 		altNameInfo.add(new ColumnInfo(0, "Context Name"));
 		altNameInfo.add(new ColumnInfo(1, "Context Version", "Number"));
 		altNameInfo.add(new ColumnInfo(2, ""));
 		altNameInfo.add(new ColumnInfo(3, "Type"));
 		ColumnInfo altNames;
 		altNames = new ColumnInfo("designations", "Data Element Alternate Name ", "Array");
 
 		altNames.nestedColumns = altNameInfo;
 		columnInfo.add(altNames);
 
 		//Reference Document
 		List<ColumnInfo> refDocInfo = new ArrayList<ColumnInfo>();
 		refDocInfo.add(new ColumnInfo(3, ""));
 		refDocInfo.add(new ColumnInfo(0, "Name"));
 		refDocInfo.add(new ColumnInfo(2, "Type"));
 
 		ColumnInfo refDoc = new ColumnInfo("reference_docs", "Document ", "Array");
 		refDoc.nestedColumns = refDocInfo;
 		columnInfo.add(refDoc);
 
 		//Derived data elements
 		columnInfo.add(
 				new ColumnInfo(0, "DE_DERIVATION", "Derivation Type", "Struct"));
 		columnInfo.add(
 				new ColumnInfo(2, "DE_DERIVATION", "Derivation Method", "Struct"));
 		columnInfo.add(
 				new ColumnInfo(3, "DE_DERIVATION", "Derivation Rule", "Struct"));
 		columnInfo.add(
 				new ColumnInfo(4, "DE_DERIVATION", "Concatenation Character", "Struct"));
 
 		List<ColumnInfo> dedInfo = new ArrayList<ColumnInfo>();
 		dedInfo.add(new ColumnInfo(0, "Public ID", "Number"));
 		dedInfo.add(new ColumnInfo(1, "Long Name"));
 		dedInfo.add(new ColumnInfo(4, "Version", "Number"));
 		dedInfo.add(new ColumnInfo(5, "Workflow Status"));
 		dedInfo.add(new ColumnInfo(6, "Context"));
 		dedInfo.add(new ColumnInfo(7, "Display Order", "Number"));
 
 		ColumnInfo deDrivation =
 			new ColumnInfo(5, "DE_DERIVATION", "DDE ", "StructArray");
 		deDrivation.nestedColumns = dedInfo;
 		columnInfo.add(deDrivation);    
 
 		return columnInfo;
 	}
 
 	//various column formats
 	private class ColumnInfo {
 		String rsColumnName;
 		int rsIndex;
 		int rsSubIndex = -1;
 		String displayName;
 		String type;
 		List nestedColumns;
 
 		/**
 		 * Constructor for a regular column that maps to one result set column
 		 */
 		ColumnInfo(
 				String rsColName,
 				String excelColName,
 				String colType) {
 			super();
 
 			rsColumnName = rsColName;
 			displayName = excelColName;
 			type = colType;
 		}
 
 		/**
 		 * Constructor for a column that maps to one result set object column,
 		 * e.g., the Derived Data Element columns
 		 */
 		ColumnInfo(
 				int colIdx,
 				String rsColName,
 				String excelColName,
 				String colType) {
 			super();
 
 			rsIndex = colIdx;
 			rsColumnName = rsColName;
 			displayName = excelColName;
 			type = colType;
 		}
 
 		/**
 		 * Constructor for a regular column that maps to one column inside an Aarry
 		 * of type String
 		 */
 		ColumnInfo(
 				int rsIdx,
 				String excelColName) {
 			super();
 
 			rsIndex = rsIdx;
 			displayName = excelColName;
 			type = "String";
 		}
 
 		/**
 		 * Constructor for a regular column that maps to one column inside an Aarry
 		 */
 		ColumnInfo(
 				int rsIdx,
 				String excelColName,
 				String colClass) {
 			super();
 
 			rsIndex = rsIdx;
 			displayName = excelColName;
 			type = colClass;
 		}
 
 		/**
 		 * Constructor for a regular column that maps to one column inside an
 		 * Object of the Aarry type.  E.g., the classification scheme information
 		 */
 		ColumnInfo(
 				int rsIdx,
 				int rsSubIdx,
 				String excelColName,
 				String colType) {
 			super();
 
 			rsIndex = rsIdx;
 			rsSubIndex = rsSubIdx;
 			displayName = excelColName;
 			type = colType;
 		}
 	}
 }
