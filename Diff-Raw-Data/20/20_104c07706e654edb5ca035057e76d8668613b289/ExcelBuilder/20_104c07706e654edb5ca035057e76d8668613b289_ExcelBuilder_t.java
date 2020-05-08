 package pt.utl.ist.fenix.tools.spreadsheet;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFCellStyle;
 import org.apache.poi.hssf.usermodel.HSSFFont;
 import org.apache.poi.hssf.usermodel.HSSFRow;
 import org.apache.poi.hssf.usermodel.HSSFSheet;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.hssf.util.HSSFColor;
 import org.apache.poi.ss.usermodel.RichTextString;
 import org.apache.poi.ss.util.CellRangeAddress;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.joda.time.YearMonthDay;
 
 import pt.utl.ist.fenix.tools.spreadsheet.SheetData.Cell;
 import pt.utl.ist.fenix.tools.spreadsheet.converters.CellConverter;
 import pt.utl.ist.fenix.tools.spreadsheet.converters.excel.BigDecimalCellConverter;
 import pt.utl.ist.fenix.tools.spreadsheet.converters.excel.DateTimeCellConverter;
 import pt.utl.ist.fenix.tools.spreadsheet.converters.excel.IntegerCellConverter;
 import pt.utl.ist.fenix.tools.spreadsheet.converters.excel.LocalDateCellConverter;
 import pt.utl.ist.fenix.tools.spreadsheet.converters.excel.MultiLanguageStringCellConverter;
 import pt.utl.ist.fenix.tools.spreadsheet.converters.excel.YearMonthDayCellConverter;
 import pt.utl.ist.fenix.tools.spreadsheet.styles.CellAlignment;
 import pt.utl.ist.fenix.tools.spreadsheet.styles.CellBorder;
 import pt.utl.ist.fenix.tools.spreadsheet.styles.CellDateFormat;
 import pt.utl.ist.fenix.tools.spreadsheet.styles.CellFillForegroundColor;
 import pt.utl.ist.fenix.tools.spreadsheet.styles.CellFillPattern;
 import pt.utl.ist.fenix.tools.spreadsheet.styles.CellStyle;
 import pt.utl.ist.fenix.tools.spreadsheet.styles.CellVerticalAlignment;
 import pt.utl.ist.fenix.tools.spreadsheet.styles.CellWrapText;
 import pt.utl.ist.fenix.tools.spreadsheet.styles.ComposedCellStyle;
 import pt.utl.ist.fenix.tools.spreadsheet.styles.FontColor;
 import pt.utl.ist.fenix.tools.spreadsheet.styles.FontHeight;
 import pt.utl.ist.fenix.tools.spreadsheet.styles.FontWeight;
 import pt.utl.ist.fenix.tools.spreadsheet.styles.StyleCache;
 
 class ExcelBuilder extends AbstractSheetBuilder {
     static Map<Class<?>, CellConverter> BASE_CONVERTERS;
 
     static {
 	// TODO: grow this list to all common basic types.
 	BASE_CONVERTERS = new HashMap<Class<?>, CellConverter>();
 	BASE_CONVERTERS.put(Integer.class, new IntegerCellConverter());
 	BASE_CONVERTERS.put(DateTime.class, new DateTimeCellConverter());
 	BASE_CONVERTERS.put(YearMonthDay.class, new YearMonthDayCellConverter());
 	BASE_CONVERTERS.put(LocalDate.class, new LocalDateCellConverter());
 	BASE_CONVERTERS.put(BigDecimal.class, new BigDecimalCellConverter());
 	BASE_CONVERTERS.put(MultiLanguageStringCellConverter.class, new MultiLanguageStringCellConverter());
     }
 
     private static Map<Class<?>, CellStyle> TYPE_STYLES;
 
     static {
 	TYPE_STYLES = new HashMap<Class<?>, CellStyle>();
 	TYPE_STYLES.put(DateTime.class, new CellDateFormat());
 	TYPE_STYLES.put(YearMonthDay.class, new CellDateFormat("dd/MM/yyyy"));
 	TYPE_STYLES.put(LocalDate.class, new CellDateFormat("dd/MM/yyyy"));
 	TYPE_STYLES.put(GregorianCalendar.class, new CellDateFormat());
 	TYPE_STYLES.put(Date.class, new CellDateFormat());
     }
 
     private static List<CellStyle> ROW_STYLES = Collections.emptyList();
 
     private static CellStyle HEADER_STYLE = new ComposedCellStyle() {
 	{
 	    merge(new FontColor(new HSSFColor.BLACK()));
 	    merge(new FontWeight(HSSFFont.BOLDWEIGHT_BOLD));
 	    merge(new FontHeight((short) 8));
 	    merge(new CellAlignment(HSSFCellStyle.ALIGN_CENTER));
 	    merge(new CellFillForegroundColor(new HSSFColor.GREY_25_PERCENT()));
 	    merge(new CellFillPattern(HSSFCellStyle.SOLID_FOREGROUND));
 	    merge(new CellBorder(HSSFCellStyle.BORDER_THIN));
 	    merge(new CellVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER));
 	    merge(new CellWrapText(true));
 	}
     };
 
     {
 	converters.putAll(BASE_CONVERTERS);
     }
 
     private CellStyle headerStyle = HEADER_STYLE;
 
     private final Map<Class<?>, CellStyle> typeStyles = new HashMap<Class<?>, CellStyle>(TYPE_STYLES);
 
     private List<CellStyle> rowStyles = new ArrayList<CellStyle>(ROW_STYLES);
 
     private StyleCache styleCache;
 
     int usefulAreaStart;
 
     int usefulAreaEnd;
 
     protected void setHeaderStyle(CellStyle style) {
 	headerStyle = style;
     }
 
     protected void appendHeaderStyle(CellStyle style) {
 	ComposedCellStyle composed = new ComposedCellStyle();
 	composed.merge(headerStyle);
 	composed.merge(style);
 	headerStyle = composed;
     }
 
     protected void addTypeStyle(Class<?> type, CellStyle style) {
 	typeStyles.put(type, style);
     }
 
     protected void setRowStyle(CellStyle... styles) {
 	rowStyles = Arrays.asList(styles);
     }
 
     protected void setValue(HSSFWorkbook book, HSSFCell cell, Object value, short span) {
 	ComposedCellStyle style = new ComposedCellStyle();
 	if (!rowStyles.isEmpty()) {
 	    style.merge(rowStyles.get(cell.getRowIndex() % rowStyles.size()));
 	}
 	if (value != null && typeStyles.containsKey(value.getClass())) {
 	    style.merge(typeStyles.get(value.getClass()));
 	}
 	setValue(book, cell, value, span, styleCache.getStyle(style));
     }
 
     private void setValue(HSSFWorkbook book, HSSFCell cell, Object value, short span, HSSFCellStyle style) {
 	if (value != null) {
 	    Object content = convert(value);
 	    if (content instanceof Boolean) {
 		cell.setCellValue((Boolean) content);
 	    } else if (content instanceof Double) {
 		cell.setCellValue((Double) content);
 	    } else if (content instanceof String) {
 		cell.setCellValue((String) content);
 	    } else if (content instanceof GregorianCalendar) {
 		cell.setCellValue((GregorianCalendar) content);
 	    } else if (content instanceof Date) {
 		cell.setCellValue((Date) content);
 	    } else if (content instanceof RichTextString) {
 		cell.setCellValue((RichTextString) content);
 	    } else if (content instanceof Formula) {
 		cell.setCellFormula(((Formula) content).getFormula(cell, usefulAreaStart, usefulAreaEnd));
 	    } else {
 		cell.setCellValue(content.toString());
 	    }
 	} else {
 	    cell.setCellValue((String) null);
 	}
 	if (span > 1) {
	    CellRangeAddress region = new CellRangeAddress(cell.getRowIndex(), cell.getRowIndex(), cell.getColumnIndex(),
		    cell.getColumnIndex() + span - 1);
 	    cell.getSheet().addMergedRegion(region);
 	}
 	cell.setCellStyle(style);
     }
 
     public void build(Map<String, SheetData<?>> sheets, OutputStream output) throws IOException {
 	try {
 	    HSSFWorkbook book = new HSSFWorkbook();
 	    styleCache = new StyleCache(book);
 	    for (Entry<String, SheetData<?>> entry : sheets.entrySet()) {
 		final HSSFSheet sheet = book.createSheet(entry.getKey());
 		int rownum = 0;
 		int colnum = 0;
 
 		SheetData<?> data = entry.getValue();
 		if (!data.headers.get(0).isEmpty()) {
 		    for (List<Cell> headerRow : data.headers) {
 			colnum = 0;
 			final HSSFRow row = sheet.createRow(rownum++);
 			for (Cell cell : headerRow) {
 			    setValue(book, row.createCell(colnum++), cell.value, cell.span, styleCache.getStyle(headerStyle));
 			    colnum = colnum + cell.span - 1;
 			}
 		    }
 		}
 		usefulAreaStart = rownum;
 		for (final List<Cell> line : data.matrix) {
 		    colnum = 0;
 		    final HSSFRow row = sheet.createRow(rownum++);
 		    for (Cell cell : line) {
 			setValue(book, row.createCell(colnum++), cell.value, cell.span);
 			colnum = colnum + cell.span - 1;
 		    }
 		}
 		usefulAreaEnd = rownum - 1;
 		if (data.hasFooter()) {
 		    colnum = 0;
 		    final HSSFRow row = sheet.createRow(rownum++);
 		    for (Cell cell : data.footer) {
 			setValue(book, row.createCell(colnum++), cell.value, cell.span);
 			colnum = colnum + cell.span - 1;
 		    }
 		}
 		for (int i = 0; i < sheet.getLastRowNum(); i++) {
 		    sheet.autoSizeColumn(i);
 		}
 	    }
 	    book.write(output);
 	} finally {
	    output.flush();
 	    output.close();
 	}
     }
 }
