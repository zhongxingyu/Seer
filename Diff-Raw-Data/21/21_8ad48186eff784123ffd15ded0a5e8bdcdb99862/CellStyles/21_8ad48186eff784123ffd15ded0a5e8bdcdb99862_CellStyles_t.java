 package excel;
 
 
 import java.util.Set;
 
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.ConditionalFormattingRule;
import org.apache.poi.ss.usermodel.Drawing;
 import org.apache.poi.ss.usermodel.Font;
 import org.apache.poi.ss.usermodel.IndexedColors;
 import org.apache.poi.ss.usermodel.PatternFormatting;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.util.CellRangeAddress;
 
 public class CellStyles {
 	public static final String DATE_FORMAT = "dd-mmm";
 	public static final String NUMBER_FORMAT = "0.00";
 	public static final short BORDER_LEFT, BORDER_RIGHT, BORDER_TOP, BORDER_BOTTOM, BORDER_ALL;
 	public static final short BORDER_LEFT_THIN, BORDER_RIGHT_THIN, BORDER_TOP_THIN, BORDER_BOTTOM_THIN, BORDER_ALL_THIN;
 	public static final short BOLD, GRAY_FILL, NUMBER, NUMBER_1, NUMBER_2, NUMBER_3, NUMBER_4, DATE;
 	static {
 		short i = 0;
 		BORDER_LEFT = i++;
 		BORDER_RIGHT = i++;
 		BORDER_TOP = i++;
 		BORDER_BOTTOM = i++;
 		BORDER_ALL = i++;
 		BORDER_LEFT_THIN = i++;
 		BORDER_RIGHT_THIN = i++;
 		BORDER_TOP_THIN = i++;
 		BORDER_BOTTOM_THIN = i++;
 		BORDER_ALL_THIN = i++;
 		BOLD = i++;
 		GRAY_FILL = i++;
 		NUMBER = i++;
 		NUMBER_1 = i++;
 		NUMBER_2 = i++;
 		NUMBER_3 = i++;
 		NUMBER_4 = i++;
 		DATE = i++;
 		
 	}
 
 	public static Cell applyStyles(Set<Short> styles, Cell c) {
 		Workbook wb = c.getSheet().getWorkbook();
 		CellStyle cs = wb.createCellStyle();
 		for(Short s : styles) {
 			if(s == BOLD) {
 				applyBoldStyle(cs, wb);
 			}
 			else if(s >= BORDER_LEFT && s <= BORDER_ALL_THIN) {
 				applyBorderStyle(cs, s);
 			}
 			else if(s == GRAY_FILL) {
 				applyGrayFillStyle(cs);
 			}
 			else if(s >= NUMBER && s <= NUMBER_4) {
 				applyNumberStyle(cs, wb, s - NUMBER);
 			}
 			else if(s == DATE) {
 				applyDateStyle(cs, wb);
 			}
 			else {
 				//System.out.println("Invalid choice");
 			}
 		}
 		c.setCellStyle(cs);
 		return c;
 	}
 	
 	/**
 	 * 
 	 * @param s
 	 * @param format
 	 */
	public static void applyConditionalFormat(Sheet s) {
 		SheetConditionalFormatting scf = s.getSheetConditionalFormatting();
 		ConditionalFormattingRule r1 = scf.createConditionalFormattingRule("$B1=TODAY()");
 		PatternFormatting fill = r1.createPatternFormatting();
 		fill.setFillBackgroundColor(IndexedColors.YELLOW.getIndex());
 		fill.setFillPattern(PatternFormatting.SOLID_FOREGROUND);
		CellRangeAddress[] regions = { CellRangeAddress.valueOf("$A1:$S"+s.getLastRowNum()+1) };
 		scf.addConditionalFormatting(regions, r1);
 	}
 	
 	private static CellStyle applyBoldStyle(CellStyle cs, Workbook wb) {
 		Font f = wb.createFont();
 		f.setBoldweight(Font.BOLDWEIGHT_BOLD);
 		cs.setFont(f);
 		return cs;
 	}
 	
 	private static CellStyle applyBorderStyle(CellStyle cs, short dir) {
 		short type = (dir >= BORDER_LEFT_THIN && dir <= BORDER_ALL_THIN) ? CellStyle.BORDER_THIN : CellStyle.BORDER_MEDIUM;
 		if(dir == BORDER_LEFT || dir == BORDER_LEFT_THIN || dir == BORDER_ALL || dir == BORDER_ALL_THIN)
 			cs.setBorderLeft(type);
 		if(dir == BORDER_RIGHT || dir == BORDER_RIGHT_THIN || dir == BORDER_ALL || dir == BORDER_ALL_THIN)
 			cs.setBorderRight(type);
 		if(dir == BORDER_BOTTOM || dir == BORDER_BOTTOM_THIN || dir == BORDER_ALL || dir == BORDER_ALL_THIN)
 			cs.setBorderBottom(type);
 		if(dir == BORDER_TOP || dir == BORDER_TOP_THIN || dir == BORDER_ALL || dir == BORDER_ALL_THIN)
 			cs.setBorderTop(type);
 		return cs;
 	}
 	
 	private static CellStyle applyGrayFillStyle(CellStyle cs) {
 		cs.setFillForegroundColor(IndexedColors.GREY_40_PERCENT.getIndex());
 		cs.setFillPattern(CellStyle.SOLID_FOREGROUND);
 		return cs;
 	}
 	
 	private static CellStyle applyNumberStyle(CellStyle cs, Workbook wb, int prec) {
 		String format = "0" + (prec == 0 ? "" : ".");
 		for(int i = 0; i < prec; i++)
 			format += 0;
 		return applyNumberStyle(cs, wb, format);
 	}
 	private static CellStyle applyNumberStyle(CellStyle cs, Workbook wb, String format) {
 		cs.setDataFormat(wb.createDataFormat().getFormat(format));
 		return cs;
 	}
 	
 	private static CellStyle applyDateStyle(CellStyle cs, Workbook wb) {
 		cs.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat(DATE_FORMAT));
 		return cs;
 	}
 	
 }
