 package ch.ubx.startlist.server;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import jxl.Workbook;
 import jxl.WorkbookSettings;
 import jxl.format.Alignment;
 import jxl.write.DateFormat;
 import jxl.write.DateTime;
 import jxl.write.Formula;
 import jxl.write.Label;
 import jxl.write.WritableCellFormat;
 import jxl.write.WritableFont;
 import jxl.write.WritableSheet;
 import jxl.write.WritableWorkbook;
 import jxl.write.WriteException;
 import ch.ubx.startlist.shared.FlightEntry;
 import ch.ubx.startlist.shared.TextConstants;
 
 public class ExcelSheet implements TextConstants {
 
 	private static final String[] columns = new String[] { TXT_DATE, TXT_START_PLACE, TXT_START_TIME + "[UTC]", TXT_LANDING_TIME_TOWPLANE + "[UTC]",
 			TXT_DURATION_TOWPLANE + "[Min.]", TXT_SHORT_REGISTRATION_TOWPLANE, TXT_TOWPLANE_PILOT, TXT_LANDING_PLACE, TXT_LANDING_TIME_GLIDER + "[UTC]", TXT_DURATION_GLIDER + "[Min.]",
 			TXT_SHORT_REGISTRATION_GLIDER, TXT_PILOT, TXT_PASSENGER_OR_INSTRUCTOR, TXT_TRAINING, TXT_REMARKS };
 
 	private static final int[] columnswidth = new int[] { 10, 18, 18, 18, 15, 18, 18, 18, 18, 15, 18, 20, 20, 6, 60 };
 
	private static final String TXT_COLUMN_LANDING_TIME_GLIDER = "I";
 	private static final String TXT_COLUMN_LANDING_TIME_TOWPLANE = "D";
 	private static final String TXT_COLUMN_START_TIME = "C";
 
 	public ExcelSheet() {
 		if (columns.length != columnswidth.length) {
 			throw new RuntimeException("columns text array and width array have a different length");
 		}
 	}
 
 	/**
 	 * @param flightEnties
 	 * @param outputStream
 	 * @param sheetLabel
 	 * @throws IOException
 	 */
 	public static void createExcel(final List<FlightEntry> flightEnties, final OutputStream outputStream, final String sheetLabel) throws IOException {
 		final WorkbookSettings ws = new WorkbookSettings();
 		ws.setLocale(Locale.GERMAN);
 		final WritableWorkbook workbook = Workbook.createWorkbook(outputStream, ws);
 		final WritableSheet sheet = workbook.createSheet(sheetLabel, 0);
 		final WritableFont times12font = new WritableFont(WritableFont.TIMES, 12, WritableFont.BOLD, false);
 		final WritableCellFormat cellHeaderFormat = new WritableCellFormat(times12font);
 		final WritableCellFormat cellDateFormat = new WritableCellFormat(new DateFormat("dd.MM"));
 		final WritableCellFormat cellTimeFormat = new WritableCellFormat(new DateFormat("hh:mm"));
 		try {	
 			cellDateFormat.setAlignment(Alignment.LEFT);
 			cellTimeFormat.setAlignment(Alignment.LEFT);
 
 			// write header
 			int col = 0;
 			int row = 0;
 			for (String labelText : columns) {
 				sheet.setColumnView(col, columnswidth[col]);
 				sheet.addCell(new Label(col++, row, labelText, cellHeaderFormat));
 			}
 			row++;
 			sheet.getSettings().setVerticalFreeze(row);
 
 			// write rows
 			for (FlightEntry flightEntry : flightEnties) {
 
 				col = 0;
 
 				// column A: date
 				Date time = new Date(flightEntry.getStartTimeInMillis());
 				sheet.addCell(new DateTime(col++, row, time, cellDateFormat));
 
 				// column B: start place
 				sheet.addCell(new Label(col++, row, flightEntry.getPlace()));
 
 				// column C: start time
 				if (!flightEntry.isStartTimeValid()) {
 					time.setTime(0);
 				}
 				sheet.addCell(new DateTime(col++, row, time, cellTimeFormat));
 
 				// column D: end time towplane
 				if (flightEntry.isEndTimeTowplaneValid()) {
 					time.setTime(flightEntry.getEndTimeTowplaneInMillis());
 				} else {
 					time.setTime(0);
 				}
 				sheet.addCell(new DateTime(col++, row, time, cellTimeFormat));
 
 				// column E: tow duration: let excel calculate it
 				if (flightEntry.isEndTimeTowplaneValid()) {
 					// formula: duration in minutes = STUNDE(Startzeit-Landezeit) * 60 + MINUTE(Startzeit-Landezeit)
 					String durationBasicValueTowplane = TXT_COLUMN_LANDING_TIME_TOWPLANE + (row + 1) + "-" + TXT_COLUMN_START_TIME + (row + 1);
 					String durationFormulaTowplane = "STUNDE(" + durationBasicValueTowplane + ")*60+MINUTE(" + durationBasicValueTowplane + ")";
 					sheet.addCell(new Formula(col++, row, durationFormulaTowplane));
 				} else {
 					// no tow duration because of unknown endTime of towplane
 					col++; 
 				}
 
 				// column F: registration towplane
 				sheet.addCell(new Label(col++, row, flightEntry.getRegistrationTowplane()));
 				
 				// column G: towplane pilot
 				sheet.addCell(new Label(col++, row, flightEntry.getTowplanePilot()));
 
 				// column H: landing place
 				sheet.addCell(new Label(col++, row, flightEntry.getLandingPlace()));
 
 				// column I: end time glider
 				if (flightEntry.isEndTimeGliderValid()) {
 					time.setTime(flightEntry.getEndTimeGliderInMillis());
 				} else {
 					time.setTime(0);
 				}
 				sheet.addCell(new DateTime(col++, row, time, cellTimeFormat));
 
 				// column J: glider duration: let excel calculate it
 				if (flightEntry.isEndTimeGliderValid()) {
 					String durationBasicValueGlider = TXT_COLUMN_LANDING_TIME_GLIDER + (row + 1) + "-" + TXT_COLUMN_START_TIME + (row + 1);
 					String durationFormulaGlider = "STUNDE(" + durationBasicValueGlider + ")*60+MINUTE(" + durationBasicValueGlider + ")";
 					sheet.addCell(new Formula(col++, row, durationFormulaGlider));
 				} else {
 					// no glider duration because of unknown endTime of glider
 					col++;	
 				}
 
 				// column K: registration glider
 				sheet.addCell(new Label(col++, row, flightEntry.getRegistrationGlider()));
 
 				// column L: name of pilot
 				sheet.addCell(new Label(col++, row, flightEntry.getPilot()));
 
 				// column M: name of passenger or instructor
 				sheet.addCell(new Label(col++, row, flightEntry.getPassengerOrInstructor()));
 
 				// column N: training or not?
 				sheet.addCell(new Label(col++, row, flightEntry.isTraining() ? TXT_Y : TXT_N));
 
 				// column O: remarks
 				sheet.addCell(new Label(col++, row, flightEntry.getRemarks()));
 
 				row++;
 			}
 
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// All sheets and cells added. Now write out the workbook
 		workbook.write();
 		try {
 			workbook.close();
 		} catch (WriteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
