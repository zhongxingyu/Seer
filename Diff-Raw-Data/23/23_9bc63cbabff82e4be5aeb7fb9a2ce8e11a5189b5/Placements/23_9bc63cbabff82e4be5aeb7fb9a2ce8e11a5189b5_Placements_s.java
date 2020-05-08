 package org.andrill.conop.pp;
 
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 
 import org.andrill.conop.pp.SummarySpreadsheet.Summary;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.Font;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 
 /**
  * Generates a placements summary sheet with event placements and interpolated
  * ages.
  * 
  * @author Josh Reed (jareed@andrill.org)
  */
 public class Placements implements Summary {
 
 	protected int after(final List<Map<String, String>> events, final int position) {
 		for (int i = position + 1; i < events.size(); i++) {
 			if (events.get(i).containsKey("agemin")) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	protected int before(final List<Map<String, String>> events, final int position) {
 		for (int i = position - 1; i >= 0; i--) {
 			if (events.get(i).containsKey("agemin")) {
 				return i;
 			}
 		}
 		return -1;
 	}
 
 	@Override
 	public void generate(final Workbook workbook, final RunInfo run) {
		Sheet sheet = workbook.createSheet();
 
 		// sort our sections
 		List<Map<String, String>> sections = run.getSections();
 		Collections.sort(sections, new Comparator<Map<String, String>>() {
 			@Override
 			public int compare(final Map<String, String> o1, final Map<String, String> o2) {
 				return new Integer(o1.get("id")).compareTo(new Integer(o2.get("id")));
 			}
 		});
 
 		// sort our events
 		List<Map<String, String>> events = run.getEvents();
 		Collections.sort(events, new Comparator<Map<String, String>>() {
 			@Override
 			public int compare(final Map<String, String> o1, final Map<String, String> o2) {
 				return new Integer(o2.get("solution")).compareTo(new Integer(o1.get("solution")));
 			}
 		});
 
 		// create our header style
 		CellStyle style = sheet.getWorkbook().createCellStyle();
 		Font font = sheet.getWorkbook().createFont();
 		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
 		style.setFont(font);
 		style.setBorderBottom(CellStyle.BORDER_THIN);
 
 		// write out our header row
 		Row header = sheet.createRow(0);
 		header.createCell(0).setCellValue("Event");
 		header.createCell(1).setCellValue("Type");
 		header.createCell(2).setCellValue("Rank");
 		header.createCell(3).setCellValue("Min Age");
 		header.createCell(4).setCellValue("Max Age");
 		for (int i = 0; i < sections.size(); i++) {
			header.createCell(i + 5).setCellValue(sections.get(i).get("name"));
 		}
 		for (Cell cell : header) {
 			cell.setCellStyle(style);
 		}
 
 		for (int i = 0; i < events.size(); i++) {
 			Map<String, String> event = events.get(i);
 			Row row = sheet.createRow(i + 1);
 			row.createCell(0).setCellValue(event.get("name"));
 			row.createCell(1).setCellValue(event.get("typename"));
 			row.createCell(2).setCellValue(Integer.parseInt(event.get("solution")));
 			if (event.containsKey("agemin")) {
 				row.createCell(3).setCellValue(Double.parseDouble(event.get("agemin")));
 			} else {
 				int before = before(events, i);
 				int after = after(events, i);
 				double a1 = (before == -1 ? 0.0 : Double.parseDouble(events.get(before).get("agemin")));
 				double a2 = (after == -1 ? a1 : Double.parseDouble(events.get(after).get("agemin")));
 				if (after == -1) {
 					after = events.size();
 				}
				row.createCell(3).setCellValue(a1 + ((a2 - a1) / (after - before)));
 			}
 			if (event.containsKey("agemax")) {
 				row.createCell(4).setCellValue(Double.parseDouble(event.get("agemax")));
 			} else {
 				int before = before(events, i);
 				int after = after(events, i);
 				double a1 = (before == -1 ? 0.0 : Double.parseDouble(events.get(before).get("agemax")));
 				double a2 = (after == -1 ? a1 : Double.parseDouble(events.get(after).get("agemax")));
 				if (after == -1) {
 					after = events.size();
 				}
				row.createCell(4).setCellValue(a1 + ((a2 - a1) / (after - before)));
 			}
			for (int j = 1; j <= sections.size(); j++) {
				row.createCell(j + 4).setCellValue(Double.parseDouble(event.get("placed." + j)));
 			}
 		}
 	}
 }
