 package com.pactera.eclipse.efficient.poi;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.poifs.filesystem.POIFSFileSystem;
 
 import com.pactera.eclipse.efficient.module.db.Table;
 import com.pactera.eclipse.efficient.module.define.ActionDefination;
 import com.pactera.eclipse.efficient.module.define.BizDefination;
 import com.pactera.eclipse.efficient.module.define.DesignDefination;
 import com.pactera.eclipse.efficient.module.define.FlowDefination;
 import com.pactera.eclipse.efficient.module.define.MvcDefination;
 import com.pactera.file.util.FileUtil;
 import com.pactera.util.StringUtil;
 
 /**
  * EMPģ
  * 
  * @author ruanzr
  * 
  */
 public class DesignParser {
 
 	public static DesignDefination parseDesignFile(File file) throws IOException {
 		FileInputStream input = null;
 		try {
 			DesignDefination designDefine = new DesignDefination();
 			input = new FileInputStream(file);
 			HSSFSheet sheet = new HSSFWorkbook(new POIFSFileSystem(input)).getSheetAt(0);
 			HSSFRow row = sheet.getRow(0);
 			designDefine.setProjectName(getCellValue(row, 1));
 			row = sheet.getRow(1);
 			designDefine.setRequirementName(getCellValue(row, 1));
 			designDefine.setDirectoryName(getCellValue(row, 3));
 
 			String name = null;
 			boolean mvcFlag = false;
			for (int i = 3; i < sheet.getLastRowNum(); i++) {
 				row = sheet.getRow(i);
 				final String value = getCellValue(row, 0);
 				if (!StringUtil.isEmpty(value)) {
 					if (!mvcFlag && "mvc".equals(value)) {
 						mvcFlag = true;
 						continue;
 					}
 					name = value;
 				}
 				if (!mvcFlag) {
 					BizDefination biz = designDefine.getBiz(name);
 					final FlowDefination flow = new FlowDefination(getCellValue(row, 1), getCellValue(row, 2));
 					if (biz != null) {
 						biz.addFlow(flow);
 					} else {
 						BizDefination bizDef = new BizDefination(name, name);
 						bizDef.addFlow(flow);
 						designDefine.AddBiz(name, bizDef);
 					}
 				} else {
 					MvcDefination mvc = designDefine.getMvc(name);
 					String bizRef = getCellValue(row, 3);
 					if (!StringUtil.isEmpty(bizRef)) {
 						bizRef = designDefine.getDirectoryName() + "\\" + bizRef;
 					}
 					String targetJsp = getCellValue(row, 4);
 					if (targetJsp.indexOf('/') == -1) {
 						targetJsp = designDefine.getDirectoryName() + "/" + targetJsp;
 					}
 					ActionDefination action = new ActionDefination(getCellValue(row, 1), getCellValue(row, 2), bizRef, targetJsp);
 					if (mvc != null) {
 						mvc.addAction(action);
 					} else {
 						MvcDefination mvcDef = new MvcDefination(name);
 						mvcDef.addAction(action);
 						designDefine.AddMvc(name, mvcDef);
 					}
 				}
 			}
 			return designDefine;
 		} finally {
 			FileUtil.close(input);
 		}
 	}
 
 	public static List<Table> parseDBFile(File file) throws IOException {
 		List<Table> tables = new ArrayList<Table>();
 		FileInputStream input = null;
 		try {
 			input = new FileInputStream(file);
 			HSSFWorkbook hssfWorkbook = new HSSFWorkbook(new POIFSFileSystem(input));
 			int num = hssfWorkbook.getNumberOfSheets();
 			for (int i = 0; i < num; i++) {
 				HSSFSheet sheet = hssfWorkbook.getSheetAt(i);
 				Table table = new TableSheet(sheet).getTable();
 				tables.add(table);
 			}
 		} finally {
 			FileUtil.close(input);
 		}
 		return tables;
 	}
 
 	private static String getCellValue(HSSFRow row, int col) {
 		return StringUtil.nvl(row.getCell(col).getStringCellValue()).trim();
 	}
 
 }
