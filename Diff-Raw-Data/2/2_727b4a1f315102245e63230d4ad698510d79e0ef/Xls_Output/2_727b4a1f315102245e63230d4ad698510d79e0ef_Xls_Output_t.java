 package reporting_tool;
 
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.ListIterator;
 
 import org.apache.poi.hssf.record.CFRuleRecord.ComparisonOperator;
 import org.apache.poi.hssf.usermodel.*;
 import org.apache.poi.ss.usermodel.*;
 import org.apache.poi.ss.util.*;
 
 public class Xls_Output {
 
 	private static final int HEADER_NB_ROWS = 17;
 	private static final int DATA_FIRST_COLUMN = 3;
 	private static final int VERS_NUM_FIRST_ROW = 14;
 	private static final int VERS_NUM_FIRST_COL = 2;
 	private static final int STAT_FIRST_ROW = 6;
 	private static final int STAT_NB_ROWS = 6;
 	private static final int NBFAILS_FIRST_ROW = 4;
 	private static final int NBFAILS_FIRST_COL = 2;
 	private static final int FIRST_COLUMN = 0;
 	private static final int FIRST_ROW = 0;
 	private static CellStyle PERCENT_STYL_ID;
 	private static CellStyle BOLDLEFT_STYL_ID;
 	private static CellStyle LEFT_STYL_ID;
 	private static CellStyle BOLDRIGHT_STYL_ID;
 	private static CellStyle BOLDCENTER_STYL_ID;
 	private static CellStyle BOLDCENTERWRAP_STYL_ID;
 	private static CellStyle FAIL_STYL_ID;
 	private static CellStyle BOLDCENTER_STYL_ID_16;
 	private static CellStyle BOLDLEFT_STYL_ID_16;
 	private static Workbook wb;
 	private PrintStream output;
 
 	public Xls_Output(String name) {
 		try {
 			output = new PrintStream(new FileOutputStream(name), true);
 			wb = new HSSFWorkbook();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 	private static Cell cur_cell;
 
 	private void string_cell(Row myrow, int position, CellStyle style, String string) {
 		cur_cell = myrow.createCell(position);
 		cur_cell.setCellStyle(style);
 		cur_cell.setCellValue(string);
 	}
 
 	private void string_cell(Row myrow, int position, String string) {
 		cur_cell = myrow.createCell(position);
 		cur_cell.setCellValue(string);
 	}
 
 	private void string_out(String string) {
 		cur_cell.setCellValue(cur_cell.getStringCellValue() + string);
 	}
 
 	private void number_cell(Row myrow, int postition, CellStyle style, int val) {
 		cur_cell = myrow.createCell(postition);
 		cur_cell.setCellStyle(style);
 		cur_cell.setCellValue(val);
 		cur_cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
 	}
 
 	private void number_cell(Row myrow, int postition, long val) {
 		cur_cell = myrow.createCell(postition);
 		cur_cell.setCellValue(val);
 		cur_cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
 	}
 
 	private void formula_cell(Row myrow, int position, CellStyle style, String string) {
 		cur_cell = myrow.createCell(position);
 		cur_cell.setCellStyle(style);
 		cur_cell.setCellFormula(string);
 		cur_cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
 	}
 
 	private void formula_cell(Row myrow, int position, String string) {
 		cur_cell = myrow.createCell(position);
 		cur_cell.setCellFormula(string);
 		cur_cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
 	}
 
 	public void generate_header() {
 		Font fbold = wb.createFont();
 		fbold.setFontHeightInPoints((short) 10);
 		fbold.setColor(IndexedColors.AUTOMATIC.getIndex());
 		fbold.setBoldweight(Font.BOLDWEIGHT_BOLD);
 
 		Font fbold16 = wb.createFont();
 		fbold16.setFontHeightInPoints((short) 16);
 		fbold16.setColor(IndexedColors.AUTOMATIC.getIndex());
 		fbold16.setBoldweight(Font.BOLDWEIGHT_BOLD);
 
 		Font fwhite = wb.createFont();
 		fwhite.setFontHeightInPoints((short) 10);
 		fwhite.setColor(IndexedColors.WHITE.getIndex());
 
 		PERCENT_STYL_ID = wb.createCellStyle();
 		PERCENT_STYL_ID.setAlignment(CellStyle.ALIGN_CENTER);
 		DataFormat format = wb.createDataFormat();
 		PERCENT_STYL_ID.setDataFormat(format.getFormat("0.000%"));
 
 		LEFT_STYL_ID = wb.createCellStyle();
 		LEFT_STYL_ID.setAlignment(CellStyle.ALIGN_LEFT);
 		format = wb.createDataFormat();
 		LEFT_STYL_ID.setDataFormat(format.getFormat("#"));
 		LEFT_STYL_ID.setFont(fbold);
 
 
 		BOLDLEFT_STYL_ID = wb.createCellStyle();
 		BOLDLEFT_STYL_ID.setAlignment(CellStyle.ALIGN_LEFT);
 		BOLDLEFT_STYL_ID.setFont(fbold);
 
 		BOLDRIGHT_STYL_ID = wb.createCellStyle();
 		BOLDRIGHT_STYL_ID.setAlignment(CellStyle.ALIGN_RIGHT);
 		BOLDRIGHT_STYL_ID.setFont(fbold);
 
 		BOLDCENTER_STYL_ID = wb.createCellStyle();
 		BOLDCENTER_STYL_ID.setAlignment(CellStyle.ALIGN_CENTER);
 		BOLDCENTER_STYL_ID.setFont(fbold);
 
 		BOLDCENTERWRAP_STYL_ID = wb.createCellStyle();
 		BOLDCENTERWRAP_STYL_ID.setAlignment(CellStyle.ALIGN_CENTER);
 		BOLDCENTERWRAP_STYL_ID.setFont(fbold);
 		BOLDCENTERWRAP_STYL_ID.setWrapText(true);
 
 		FAIL_STYL_ID = wb.createCellStyle();
 		FAIL_STYL_ID.setFillPattern(CellStyle.SOLID_FOREGROUND);
 		FAIL_STYL_ID.setFillForegroundColor(IndexedColors.RED.getIndex());
 		FAIL_STYL_ID.setFont(fwhite);
 
 		BOLDCENTER_STYL_ID_16 = wb.createCellStyle();
 		BOLDCENTER_STYL_ID_16.setAlignment(CellStyle.ALIGN_CENTER);
 		BOLDCENTER_STYL_ID_16.setFont(fbold16);
 
 		BOLDLEFT_STYL_ID_16 = wb.createCellStyle();
 		BOLDLEFT_STYL_ID_16.setAlignment(CellStyle.ALIGN_LEFT);
 		BOLDLEFT_STYL_ID_16.setFont(fbold16);
 
 	}
 
 	public void excel_terminate() {
 		try {
 			wb.write(output);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		output.close();
 	}
 
 	public void create_summary(RootDataClass rootdata) {
 		Sheet summary_sheet = wb.createSheet("Summary");
 		Row my_cur_row;
 		summary_sheet.setZoom(3, 4);
 		// Columns formatting
 		int col = 0;
 		summary_sheet.setColumnWidth(col++, 30 * 50);
 		summary_sheet.setColumnWidth(col++, 70 * 50);
 		summary_sheet.setColumnWidth(col++, 90 * 50);
 		summary_sheet.setColumnWidth(col++, 220 * 50);
 		summary_sheet.setColumnWidth(col++, 90 * 50);
 		summary_sheet.setColumnWidth(col++, 70 * 50);
 		summary_sheet.setColumnWidth(col++, 70 * 50);
 		summary_sheet.setColumnWidth(col++, 220 * 50);
 		summary_sheet.setColumnWidth(col++, 150 * 50);
 		summary_sheet.setColumnWidth(col++, 150 * 50);
 		summary_sheet.setColumnWidth(col++, 400 * 50);
 
 		// First line
 		my_cur_row = summary_sheet.createRow(FIRST_ROW);
 		string_cell(my_cur_row, DATA_FIRST_COLUMN, BOLDCENTER_STYL_ID, "Test Summary");
 
 
 		// We go directly to the row 5
 		col = 1;
 		my_cur_row = summary_sheet.createRow(5);
 		string_cell(my_cur_row, FIRST_COLUMN, BOLDCENTER_STYL_ID, "ID");
 		string_cell(my_cur_row, FIRST_COLUMN + col++, BOLDCENTER_STYL_ID, "Session");
 		string_cell(my_cur_row, FIRST_COLUMN + col++, BOLDCENTER_STYL_ID, "Launch date");
 		string_cell(my_cur_row, FIRST_COLUMN + col++, BOLDCENTER_STYL_ID, "Compiler Version");
 		string_cell(my_cur_row, FIRST_COLUMN + col++, BOLDCENTER_STYL_ID, "Compiler Date");
		string_cell(my_cur_row, FIRST_COLUMN + col++, BOLDCENTER_STYL_ID, "Build No");
 		string_cell(my_cur_row, FIRST_COLUMN + col++, BOLDCENTER_STYL_ID, "Executor");
 		string_cell(my_cur_row, FIRST_COLUMN + col++, BOLDCENTER_STYL_ID, "Compiler Flags");
 		string_cell(my_cur_row, FIRST_COLUMN + col++, BOLDCENTER_STYL_ID, "Simulator Version");
 		string_cell(my_cur_row, FIRST_COLUMN + col++, BOLDCENTER_STYL_ID, "Simulator Flags");
 		string_cell(my_cur_row, FIRST_COLUMN + col++, BOLDCENTER_STYL_ID, "Toolset Path");
 
 		int i;
 		for (i = 0; i < rootdata.get_nb_sessions(); i++) {
 			TestSession my_session = rootdata.get_session(i);
 			my_cur_row = summary_sheet.createRow(6 + i);
 			col=1;
 			number_cell(my_cur_row, FIRST_COLUMN, i + 1);
 			string_cell(my_cur_row, FIRST_COLUMN + col++, my_session.name);
 			string_cell(my_cur_row, FIRST_COLUMN + col++, my_session.date.print_date() + " " + my_session.date.print_time());
 			string_cell(my_cur_row, FIRST_COLUMN + col++, my_session.get_cc_ver());
 			string_cell(my_cur_row, FIRST_COLUMN + col++, my_session.get_cc_date());
 			string_cell(my_cur_row, FIRST_COLUMN + col++, my_session.build_number);
 			string_cell(my_cur_row, FIRST_COLUMN + col++, my_session.executor_name);
 			string_cell(my_cur_row, FIRST_COLUMN + col++, "");
 			ListIterator<String> iter_elem = my_session.compiler_flags.listIterator();
 			while (iter_elem.hasNext()) {
 				string_out(iter_elem.next() + " ");
 			}
 			string_cell(my_cur_row, FIRST_COLUMN + col++, my_session.get_sim_ver());
 			string_cell(my_cur_row, FIRST_COLUMN + col++, "");
 			iter_elem = my_session.simulator_flags.listIterator();
 			while (iter_elem.hasNext()) {
 				string_out(iter_elem.next() + " ");
 			}
 			string_cell(my_cur_row, FIRST_COLUMN + col++, my_session.toolst_path);
 		}
 	}
 
 	private String create_column_name(int offset) {
 		if (offset <= 25) {
 			return String.valueOf((char) ('A' + offset));
 		} else if (offset <= 51) {
 			return "A" + String.valueOf((char) ('A' + offset - 26));
 		} else if (offset <= 77) {
 			return "B" + String.valueOf((char) ('A' + offset - 52));
 		} else if (offset <= 103) {
 			return "C" + String.valueOf((char) ('A' + offset - 78));
 		}
 		return null;
 	}
 
 	private void create_full_page(RootDataClass rootdata, String name, String title, Discriminent disc, Sections sec) {
 		int nb_sessions_to_dump = rootdata.get_nb_sessions();
 		int nb_max_data = rootdata.get_nb_max_data();
 		int COMP_FIRST_COLUMN = nb_sessions_to_dump + DATA_FIRST_COLUMN + 2;
 		int i;
 		Sheet computed_sheet = wb.createSheet(name);
 		Row cur_work_row;
 		// Columns formatting
 		computed_sheet.setColumnWidth(FIRST_COLUMN, 90 * 50);
 		computed_sheet.setColumnWidth(FIRST_COLUMN + 1, 180 * 50);
 		computed_sheet.setColumnWidth(FIRST_COLUMN + 2, 70 * 50);
 		computed_sheet.setColumnHidden(COMP_FIRST_COLUMN - 2, true);
 		computed_sheet.setColumnWidth(COMP_FIRST_COLUMN - 1, 100 * 50);
 		for (i = 0; i < nb_sessions_to_dump; i++) {
 			computed_sheet.setColumnWidth(DATA_FIRST_COLUMN + i, 80 * 50);
 			computed_sheet.setColumnWidth(COMP_FIRST_COLUMN + i, 80 * 50);
 		}
 
 		computed_sheet.createFreezePane(DATA_FIRST_COLUMN, 0);
 
 		// Title
 		string_cell(computed_sheet.createRow(FIRST_ROW), FIRST_COLUMN, BOLDLEFT_STYL_ID_16, title);
 
 		// Statistics
 		Row rfail = computed_sheet.createRow(NBFAILS_FIRST_ROW);
 		Row ravg = computed_sheet.createRow(STAT_FIRST_ROW);
 		Row rgeo = computed_sheet.createRow(STAT_FIRST_ROW + 1);
 		Row rmin = computed_sheet.createRow(STAT_FIRST_ROW + 2);
 		Row rmax = computed_sheet.createRow(STAT_FIRST_ROW + 3);
 		Row rmed = computed_sheet.createRow(STAT_FIRST_ROW + 4);
 		Row rtot = computed_sheet.createRow(STAT_FIRST_ROW + 5);
 
 		string_cell(rfail, NBFAILS_FIRST_COL, BOLDCENTER_STYL_ID, "Failures");
 		string_cell(ravg, COMP_FIRST_COLUMN - 1, BOLDCENTER_STYL_ID, "Average");
 		string_cell(rgeo, COMP_FIRST_COLUMN - 1, BOLDCENTER_STYL_ID, "Geomean");
 		string_cell(rmin, COMP_FIRST_COLUMN - 1, BOLDCENTER_STYL_ID, "Min");
 		string_cell(rmax, COMP_FIRST_COLUMN - 1, BOLDCENTER_STYL_ID, "Max");
 		string_cell(rmed, COMP_FIRST_COLUMN - 1, BOLDCENTER_STYL_ID, "Median");
 		string_cell(rtot, COMP_FIRST_COLUMN - 1, BOLDCENTER_STYL_ID, "Total");
 
 		for (i = 0; i < nb_sessions_to_dump; i++) {
 			String reffail = create_column_name(i + DATA_FIRST_COLUMN) + "14:" + create_column_name(i + DATA_FIRST_COLUMN) + String.valueOf(HEADER_NB_ROWS + nb_max_data);
 			// Number of FAILs
 			formula_cell(rfail, i + 1 + NBFAILS_FIRST_COL, "COUNTIF(" + reffail + ",\"FAIL*\")");
 			// Average - Geomean - Min - Max - Median - Total
 			String ref = create_column_name(i + COMP_FIRST_COLUMN) + String.valueOf(HEADER_NB_ROWS + 1) + ":" + create_column_name(i + COMP_FIRST_COLUMN) + String.valueOf(HEADER_NB_ROWS + nb_max_data);
 			formula_cell(ravg, i + COMP_FIRST_COLUMN, PERCENT_STYL_ID, "AVERAGE(" + ref + ")");
 			formula_cell(rgeo, i + COMP_FIRST_COLUMN, PERCENT_STYL_ID, "GEOMEAN(" + ref + ")");
 			formula_cell(rmin, i + COMP_FIRST_COLUMN, PERCENT_STYL_ID, "MIN(" + ref + ")");
 			formula_cell(rmax, i + COMP_FIRST_COLUMN, PERCENT_STYL_ID, "MAX(" + ref + ")");
 			formula_cell(rmed, i + COMP_FIRST_COLUMN, PERCENT_STYL_ID, "MEDIAN(" + ref + ")");
 			String right = create_column_name(i + DATA_FIRST_COLUMN) + String.valueOf(HEADER_NB_ROWS + 1) + ":" + create_column_name(i + DATA_FIRST_COLUMN) + String.valueOf(HEADER_NB_ROWS + nb_max_data);
 			String left = create_column_name(COMP_FIRST_COLUMN - 2) + String.valueOf(HEADER_NB_ROWS + 1) + ":" + create_column_name(COMP_FIRST_COLUMN - 2) + String.valueOf(HEADER_NB_ROWS + nb_max_data);
 			String formula_any = "SUMIF(" + left + ",\">0\"," + right + ")/SUMIF(" + right + ",\">0\"," + left + ")";
 			String prev_val = create_column_name(i - 1 + DATA_FIRST_COLUMN) + String.valueOf(HEADER_NB_ROWS + 1) + ":" + create_column_name(i - 1 + DATA_FIRST_COLUMN) + String.valueOf(HEADER_NB_ROWS + nb_max_data);
 			String formula_prev = "SUMIF(" + prev_val + ",\">0\"," + right + ")/SUMIF(" + right + ",\">0\"," + prev_val + ")";
 			formula_cell(rtot, i + COMP_FIRST_COLUMN, PERCENT_STYL_ID, "IF($B$15=0," + formula_prev + "," + formula_any + ")");
 		}
 
 		cur_work_row = computed_sheet.createRow(VERS_NUM_FIRST_ROW);
 		string_cell(cur_work_row, FIRST_COLUMN, BOLDCENTER_STYL_ID, "Reference");
 		number_cell(cur_work_row, FIRST_COLUMN + 1, BOLDLEFT_STYL_ID, 1);
 		string_cell(cur_work_row, VERS_NUM_FIRST_COL, BOLDCENTER_STYL_ID, "Session");
 
 		for (i = 0; i < nb_sessions_to_dump; i++) {
 			number_cell(cur_work_row, DATA_FIRST_COLUMN + i, BOLDCENTER_STYL_ID, i + 1);
 			string_cell(cur_work_row, COMP_FIRST_COLUMN + i, BOLDCENTER_STYL_ID, (i + 1) + " Vs REF");
 		}
 
 		// Tags
 		cur_work_row = computed_sheet.createRow(VERS_NUM_FIRST_ROW + 1);
 		string_cell(cur_work_row, VERS_NUM_FIRST_COL, BOLDCENTER_STYL_ID, "Tags");
 
 		for (i = 0; i < nb_sessions_to_dump; i++) {
 			TestSession my_session = rootdata.get_session(i);
 			if (sqa_report.Cruise_Control) {
 				string_cell(cur_work_row, DATA_FIRST_COLUMN + i, BOLDCENTERWRAP_STYL_ID, my_session.compiler_name);
 				string_cell(cur_work_row, COMP_FIRST_COLUMN + i, BOLDCENTERWRAP_STYL_ID, my_session.compiler_name);
 			} else {
 				string_cell(cur_work_row, DATA_FIRST_COLUMN + i, BOLDCENTERWRAP_STYL_ID, my_session.name);
 				string_cell(cur_work_row, COMP_FIRST_COLUMN + i, BOLDCENTERWRAP_STYL_ID, my_session.name);
 			}
 		}
 
 		// VALUES
 		ArrayList<RootTest> my_tests = rootdata.get_disc();
 		for (int disc_iter=0 ; disc_iter < my_tests.size(); disc_iter++) {
 			int cur_row_number = HEADER_NB_ROWS + disc_iter;
 			String indir_cell = "$" + create_column_name(COMP_FIRST_COLUMN - 2) + String.valueOf(cur_row_number + 1);
 			RootTest my_test = my_tests.get(disc_iter);
 			cur_work_row = computed_sheet.createRow(cur_row_number);
 
 			//Dump 3 first columns (test name, target, options)
 			string_cell(cur_work_row, FIRST_COLUMN, my_test.get_test());
 			string_cell(cur_work_row, FIRST_COLUMN + 1, my_test.get_target());
 			string_cell(cur_work_row, FIRST_COLUMN + 2, "");
 			ListIterator<String> iter_elem = my_test.get_options().listIterator();
 			while (iter_elem.hasNext()) {
 				string_out(iter_elem.next() + " ");
 			}
 			formula_cell(cur_work_row, COMP_FIRST_COLUMN - 2, "INDIRECT(ADDRESS(ROW(),3+$B$" + (VERS_NUM_FIRST_ROW + 1) + ",1,TRUE))");
 			// Dump values: <data> <Vs Any>
 			for (int j = 0; j < rootdata.get_nb_sessions(); j++) {
 				TestSession my_session = rootdata.get_session(j);
 				long val;
 				if (disc != Discriminent.SPEED) {
 					val = my_session.get_size(my_test, sec, disc, false);
 				} else {
 					val = my_session.get_cycle(my_test);
 				}
 				switch ((int) val) {
 				case CommonData.NOT_EXECUTED:
 					string_cell(cur_work_row, DATA_FIRST_COLUMN + j, "(not executed)");
 					break;
 				case CommonData.HAS_FAILED:
 					string_cell(cur_work_row, DATA_FIRST_COLUMN + j, "FAIL(make error)");
 					break;
 				case CommonData.NOT_RELEVANT:
 					string_cell(cur_work_row, DATA_FIRST_COLUMN + j, "Not Relevant.");
 					break;
 				case -1:
 					string_cell(cur_work_row, DATA_FIRST_COLUMN + j, "No result.");
 					break;
 				default:
 					number_cell(cur_work_row, DATA_FIRST_COLUMN + j, val);
 					break;
 				}
 				String myvalcell = "$" + create_column_name(DATA_FIRST_COLUMN + j) + String.valueOf(cur_row_number + 1);
 				String prevvalcell = "$" + create_column_name(DATA_FIRST_COLUMN + j - 1) + String.valueOf(cur_row_number + 1);
 				String vs_anyphase = "IF(T(" + myvalcell + ")<>\"\"," + myvalcell + ",IF(OR(" + indir_cell + "=0,T(" + indir_cell + ")<>\"\"),\"- \"," + myvalcell + "/" + indir_cell + "))";
 				String vs_prevphase = "IF(T(" + myvalcell + ")<>\"\"," + myvalcell + ",IF(OR(" + prevvalcell + "=0,T(" + prevvalcell + ")<>\"\"),\"- \"," + myvalcell + "/" + prevvalcell + "))";
 				String full = "IF($B$" + (VERS_NUM_FIRST_ROW + 1) + "=0," + vs_prevphase + "," + vs_anyphase + ")";
 				formula_cell(cur_work_row, COMP_FIRST_COLUMN + j, PERCENT_STYL_ID, full);
 			}
 		}
 		computed_sheet.setZoom(3, 4);
 		// Generate formatting
 		HSSFPatternFormatting CurFmt;
 		HSSFFontFormatting CurFnt;
 		HSSFConditionalFormattingRule Rule1, Rule2, Rule3;
 
 		//---------------------------------------------------------------------------------------------------------
 		// Conditional formatting for MAIN TABLE
 		CellRangeAddress[] range1 = {
 				new CellRangeAddress(HEADER_NB_ROWS, HEADER_NB_ROWS + nb_max_data - 1, COMP_FIRST_COLUMN, COMP_FIRST_COLUMN + nb_sessions_to_dump - 1),
 				new CellRangeAddress(STAT_FIRST_ROW, STAT_FIRST_ROW + STAT_NB_ROWS - 1, COMP_FIRST_COLUMN, COMP_FIRST_COLUMN + nb_sessions_to_dump - 1),};
 		CellRangeAddress[] range2 = {
 				new CellRangeAddress(NBFAILS_FIRST_ROW, NBFAILS_FIRST_ROW, NBFAILS_FIRST_COL + 1, NBFAILS_FIRST_COL + nb_sessions_to_dump)
 		};
 		CellRangeAddress[] range3 = {
 				new CellRangeAddress(HEADER_NB_ROWS, HEADER_NB_ROWS + nb_max_data - 1, DATA_FIRST_COLUMN + 1, DATA_FIRST_COLUMN + nb_sessions_to_dump - 1),};
 		HSSFSheetConditionalFormatting mycf = ((HSSFSheet) computed_sheet).getSheetConditionalFormatting();
 		String ref = "INDIRECT(ADDRESS(ROW(),COLUMN(),,))";
 
 		//Failure
 		Rule1 = mycf.createConditionalFormattingRule("OR(" + ref + "=\"FAIL(make error)\"," + ref + "=\"FAIL(Diff)\")");
 		CurFmt = Rule1.createPatternFormatting();
 		CurFmt.setFillBackgroundColor(IndexedColors.VIOLET.getIndex());
 		CurFnt = Rule1.createFontFormatting();
 		CurFnt.setFontColorIndex(IndexedColors.WHITE.getIndex());
 
 		// Bad
 		Rule2 = mycf.createConditionalFormattingRule("AND(T(" + ref + ")=\"\"," + ref + ">1)");
 		CurFmt = Rule2.createPatternFormatting();
 		CurFmt.setFillBackgroundColor(IndexedColors.RED.getIndex());
 		CurFnt = Rule2.createFontFormatting();
 		CurFnt.setFontColorIndex(IndexedColors.WHITE.getIndex());
 
 		// Good
 		Rule3 = mycf.createConditionalFormattingRule("AND(T(" + ref + ")=\"\"," + ref + "<1)");
 		CurFmt = Rule3.createPatternFormatting();
 		CurFmt.setFillBackgroundColor(IndexedColors.GREEN.getIndex());
 		CurFnt = Rule3.createFontFormatting();
 		CurFnt.setFontColorIndex(IndexedColors.WHITE.getIndex());
 		HSSFConditionalFormattingRule[] cfRules = {Rule1, Rule2, Rule3};
 		mycf.addConditionalFormatting(range1, cfRules);
 
 		//---------------------------------------------------------------------------------------------------------
 		// Conditional formatting for NUMBER OF FAILS
 		// Bad
 		Rule1 = mycf.createConditionalFormattingRule(ComparisonOperator.GT, "0", null);
 		CurFmt = Rule1.createPatternFormatting();
 		CurFmt.setFillBackgroundColor(IndexedColors.RED.getIndex());
 		CurFnt = Rule1.createFontFormatting();
 		CurFnt.setFontColorIndex(IndexedColors.WHITE.getIndex());
 
 		// Good
 		Rule2 = mycf.createConditionalFormattingRule(ComparisonOperator.EQUAL, "0", null);
 		CurFmt = Rule2.createPatternFormatting();
 		CurFnt = Rule2.createFontFormatting();
 		CurFnt.setFontColorIndex(IndexedColors.WHITE.getIndex());
 		CurFmt.setFillBackgroundColor(IndexedColors.GREEN.getIndex());
 		mycf.addConditionalFormatting(range2, Rule1, Rule2);
 
 		//---------------------------------------------------------------------------------------------------------
 		// Conditional formatting for VS Prev
 		// Bad
 		String ref2 = "INDIRECT(ADDRESS(ROW(),COLUMN()-1,,))";
 		Rule1 = mycf.createConditionalFormattingRule(ref + ">" + ref2);
 		CurFmt = Rule1.createPatternFormatting();
 		CurFmt.setFillBackgroundColor(IndexedColors.RED.getIndex());
 		CurFnt = Rule1.createFontFormatting();
 		CurFnt.setFontColorIndex(IndexedColors.WHITE.getIndex());
 
 		// Good
 		Rule2 = mycf.createConditionalFormattingRule(ref + "<" + ref2);
 		CurFmt = Rule2.createPatternFormatting();
 		CurFnt = Rule2.createFontFormatting();
 		CurFnt.setFontColorIndex(IndexedColors.WHITE.getIndex());
 		CurFmt.setFillBackgroundColor(IndexedColors.GREEN.getIndex());
 		mycf.addConditionalFormatting(range3, Rule1, Rule2);
 
 	}
 
 	public void create_new_page(RootDataClass rootdata, Discriminent disc, Sections sec) {
 		if (rootdata.get_nb_max_data() == 0) {
 			return;
 		}
 		if (disc == Discriminent.SIZE_OBJ || disc == Discriminent.SIZE_BIN || disc == Discriminent.SIZE_FUNC || disc == Discriminent.SIZE_APPLI) {
 			String name = null, data_name, title;
 			switch (sec) {
 			case TEXT:
 				name = "Text";
 				break;
 			case DATA:
 				name = "Data";
 				break;
 			case BSS:
 				name = "Bss";
 				break;
 			case RODATA:
 				name = "Rodata";
 				break;
 			case TOTAL:
 				name = "Total";
 				break;
 			case RODATA_PLUS_TEXT:
 				name = "Text_Rodata";
 				break;
 			default:
 				System.err.println("ERROR : Section undefined");
 				System.exit(1);
 			}
 			if (disc == Discriminent.SIZE_OBJ) {
 				name = name.concat("_Obj");
 			}
 			if (disc == Discriminent.SIZE_BIN) {
 				name = name.concat("_Bin");
 			}
 			if (disc == Discriminent.SIZE_FUNC) {
 				name = name.concat("_Func");
 			}
 			if (disc == Discriminent.SIZE_APPLI) {
 				name = name.concat("_Appli");
 			}
 			data_name = "Size_" + name;
 			title = "Size analysis on " + name + " Section";
 			create_full_page(rootdata, data_name, title, disc, sec);
 		} else {
 			create_full_page(rootdata, "Cycles", "Cycles Analysis", disc, sec);
 		}
 	}
 
 }
