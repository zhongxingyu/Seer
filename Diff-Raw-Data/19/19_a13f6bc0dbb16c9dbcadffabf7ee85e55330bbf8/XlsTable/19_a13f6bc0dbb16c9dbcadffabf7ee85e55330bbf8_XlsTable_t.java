 package com.tobeface.modules.table;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 
 import com.tobeface.modules.lang.Ghost;
 import com.tobeface.modules.lang.IOs;
 import com.tobeface.modules.lang.Lang;
 import com.tobeface.modules.lang.Preconditions;
 import com.tobeface.modules.table.annotations.TableField;
 import com.tobeface.modules.table.annotations.TableValueConverters;
 
 /**
  * 
  * @author loudyn
  * 
  */
 final class XlsTable implements Table {
 
 	private final File workspace;
 	private File template;
 	private File config;
 
 	XlsTable(File workspace, File template) {
 		this(workspace, template, null);
 	}
 
 	XlsTable(File workspace, File template, File config) {
 		Preconditions.notNull(workspace);
 		Preconditions.notNull(template);
 		this.workspace = workspace;
 		this.template = template;
 		this.config = config;
 	}
 
 	public <T> void insert(List<T> objects) {
 
 		InputStream in = null;
 		OutputStream out = null;
 
 		try {
 
 			XlsConf conf = XlsTableUtils.loadConf(config);
 
 			File copy = XlsTableUtils.copyTemplate(template);
 			in = new FileInputStream(copy);
 			HSSFWorkbook wb = new HSSFWorkbook(in);
 
 			HSSFSheet sheet = XlsTableUtils.loadSheet(conf, wb);
 			Map<String, Integer> headers = XlsTableUtils.loadSheetHeader(conf, sheet);
 
 			int rowNum = conf.startRow;
 			for (T obj : objects) {
 				Ghost<T> ghost = (Ghost<T>) Ghost.me(obj);
 				HSSFRow row = sheet.createRow(rowNum++);
 				insertEachRow(headers, row, ghost, obj);
 			}
 
 			out = new FileOutputStream(workspace);
 			wb.write(out);
 		} catch (Exception e) {
 			throw Lang.uncheck(e);
 		} finally {
 			IOs.freeQuietly(in, out);
 		}
 	}
 
 	private <T> void insertEachRow(Map<String, Integer> headers, HSSFRow row, Ghost<T> ghost, T obj) {
 		Field[] tableFields = TableFields.tableFields(obj);
 		for (Field field : tableFields) {
 			
 			TableField tableField = field.getAnnotation(TableField.class);
 			TableValueConverters converters = field.getAnnotation(TableValueConverters.class);
 			int cellIndex = XlsTableUtils.selectColumnIndex(
 																tableField.columnName(),
 																tableField.columnIndex(),
 																headers
 														);
 			
			Object cellValue = TableValues.downstreamConvert(
 																converters,
 																ghost.ejector(obj, field).eject()
														);
 			
			row.createCell(cellIndex).setCellValue(null == cellValue ? "" : cellValue.toString());
 		}
 	}
 
 	@Override
 	public <T> List<T> select(Class<T> clazz) {
 
 		List<T> result = new LinkedList<T>();
 		InputStream in = null;
 		try {
 
 			XlsConf conf = XlsTableUtils.loadConf(config);
 			
 			in = new FileInputStream(workspace);
 			HSSFWorkbook wb = new HSSFWorkbook(in);
 			
 			HSSFSheet sheet = XlsTableUtils.loadSheet(conf, wb);
 			Map<String, Integer> headers = XlsTableUtils.loadSheetHeader(conf, sheet);
 			
 			for (int i = conf.startRow; i < sheet.getLastRowNum(); i++) {
 				HSSFRow row = sheet.getRow(i);
 				Ghost<T> ghost = Ghost.me(clazz);
 				T obj = ghost.born();
 				selectEachRow(headers, row, ghost, obj);
 				result.add(obj);
 			}
 
 			return result;
 		} catch (Exception e) {
 			throw Lang.uncheck(e);
 		} finally {
 			IOs.freeQuietly(in);
 		}
 	}
 
 	private <T> void selectEachRow(Map<String, Integer> headers, HSSFRow row, Ghost<T> ghost, T obj) throws IllegalAccessException, InvocationTargetException {
 		Field[] tableFields = TableFields.tableFields(obj);
 		for (Field field : tableFields) {
 			
 			TableField tableField = field.getAnnotation(TableField.class);
 			int cellIndex = XlsTableUtils.selectColumnIndex(
 																tableField.columnName(),
 																tableField.columnIndex(),
 																headers
 													);
 
 			TableValueConverters converters = field.getAnnotation(TableValueConverters.class);
			
			String raw = null;
			HSSFCell cell = row.getCell(cellIndex);
			if(null != cell){
				raw = cell.getStringCellValue();
			}
			
 			Object value = TableValues.upstreamConvert(converters, raw);
 			BeanUtils.setProperty(obj, field.getName(), value);
 		}
 	}
 
 	/**
 	 * 
 	 * @author loudyn
 	 * 
 	 */
 	static class XlsConf {
 		int sheet = 0;
 		int headerRow = 0;
 		int startRow = 1;
 	}
 }
