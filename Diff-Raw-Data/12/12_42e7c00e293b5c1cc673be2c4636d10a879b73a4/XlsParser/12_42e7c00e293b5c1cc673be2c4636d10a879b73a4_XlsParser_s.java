 package com.patbaumgartner.zbw.utils;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.commons.lang.time.DateUtils;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.poifs.filesystem.POIFSFileSystem;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 
 import com.patbaumgartner.zbw.domain.Event;
 
 public class XlsParser {
 
 	String fileName;
 	Workbook wb;
 
 	public XlsParser(String fileName) {
 		this.fileName = fileName;
 	}
 
 	public Workbook initHSSFWorkbook() throws IOException {
 		InputStream inp = new FileInputStream(fileName);
 		wb = new HSSFWorkbook(new POIFSFileSystem(inp));
 		return wb;
 	}
 
 	public List<Event> collectEvents() {
 		List<Event> events = new ArrayList<Event>();
 		Sheet sheet1 = wb.getSheetAt(0);
 		for (Row row : sheet1) {
 			Event event = new Event();
 			if (row.getRowNum() > 7) {
 				for (Cell cell : row) {
 
 					switch (cell.getColumnIndex()) {
 					case 1:
						event.setDate(DateUtils.toCalendar(cell.getDateCellValue()));
 						break;
 					case 2:
 						break;
 					case 3:
 						Calendar startDate = (Calendar) event.getDate().clone();
 						double startTime = cell.getNumericCellValue();
 						int startHours = (int) Math.floor(startTime);
 						startDate.set(Calendar.HOUR, startHours);
 						int startMinutes = (int) ((startTime - startHours) * 100);
 						startDate.set(Calendar.MINUTE, startMinutes);
 						event.setStartDate(startDate);
 						break;
 					case 4:
 						Calendar endDate = (Calendar) event.getDate().clone();
 						double endTime = cell.getNumericCellValue();
 						int endHours = (int) Math.floor(endTime);
 						endDate.set(Calendar.HOUR, endHours);
 						int endMinutes = (int) ((endTime - endHours) * 100);
 						endDate.set(Calendar.MINUTE, endMinutes);
 						event.setEndDate(endDate);
 						break;
 					case 5:
 						event.setCourse(cell.getStringCellValue());
 						break;
 					case 6:
 						event.setTitle(cell.getStringCellValue());
 						break;
 					case 7:
						event.setRoom(String.valueOf((int)cell.getNumericCellValue()));
 					default:
 					}
 				}
				events.add(event);
 			}
 		}
 		return events;
 	}
 
 }
