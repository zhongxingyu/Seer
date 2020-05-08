 package pt.utl.ist.fenix.tools.excel;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.poi.hssf.usermodel.HSSFCell;
 import org.apache.poi.hssf.usermodel.HSSFCellStyle;
 import org.apache.poi.hssf.usermodel.HSSFWorkbook;
 import org.apache.poi.ss.usermodel.RichTextString;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.joda.time.YearMonthDay;
 
 import pt.utl.ist.fenix.tools.excel.converters.BigDecimalCellConverter;
 import pt.utl.ist.fenix.tools.excel.converters.CellConverter;
 import pt.utl.ist.fenix.tools.excel.converters.DateTimeCellConverter;
 import pt.utl.ist.fenix.tools.excel.converters.IntegerCellConverter;
 import pt.utl.ist.fenix.tools.excel.converters.LocalDateCellConverter;
 import pt.utl.ist.fenix.tools.excel.converters.YearMonthDayCellConverter;
 import pt.utl.ist.fenix.tools.excel.styles.CellDataFormat;
 import pt.utl.ist.fenix.tools.excel.styles.CellStyle;
 import pt.utl.ist.fenix.tools.excel.styles.ComposedCellStyle;
 
 /**
  * Common infrastructure for the Spreadsheet builder. Handles the conversion,
  * and style application mechanisms. Subclasses must fill cells using the
  * {@link #setHeaderValue(HSSFWorkbook, HSSFCell, Object)} and
  * {@link #setValue(HSSFWorkbook, HSSFCell, Object)} methods.
  * 
  * @author Pedro Santos (pedro.miguel.santos@ist.utl.pt)
  * 
  * @param <Item>
  *            The type of object that is used to fill the lines.
  */
 public abstract class AbstractSpreadsheetBuilder<Item> {
     private static Map<Class<?>, CellConverter> BASE_CONVERTERS;
 
     static {
 	// TODO: grow this list to all common basic types.
 	BASE_CONVERTERS = new HashMap<Class<?>, CellConverter>();
 	BASE_CONVERTERS.put(Integer.class, new IntegerCellConverter());
 	BASE_CONVERTERS.put(DateTime.class, new DateTimeCellConverter());
 	BASE_CONVERTERS.put(YearMonthDay.class, new YearMonthDayCellConverter());
 	BASE_CONVERTERS.put(LocalDate.class, new LocalDateCellConverter());
 	BASE_CONVERTERS.put(BigDecimal.class, new BigDecimalCellConverter());
     }
 
     private final Map<Class<?>, CellConverter> converters = new HashMap<Class<?>, CellConverter>(BASE_CONVERTERS);
 
     private static CellStyle HEADER_STYLE = CellStyle.HEADER_STYLE;
 
     private CellStyle headerStyle = HEADER_STYLE;
 
     private static Map<Class<?>, CellStyle> TYPE_STYLES;
 
     static {
 	TYPE_STYLES = new HashMap<Class<?>, CellStyle>();
 	TYPE_STYLES.put(DateTime.class, new CellDataFormat());
 	TYPE_STYLES.put(YearMonthDay.class, new CellDataFormat("dd/MM/yyyy"));
 	TYPE_STYLES.put(LocalDate.class, new CellDataFormat("dd/MM/yyyy"));
     }
 
     private final Map<Class<?>, CellStyle> typeStyles = new HashMap<Class<?>, CellStyle>(TYPE_STYLES);
 
     private static List<CellStyle> ROW_STYLES = Collections.emptyList();
 
     private List<CellStyle> rowStyles = new ArrayList<CellStyle>(ROW_STYLES);
 
     private Object convert(Object content) {
 	if (converters.containsKey(content.getClass())) {
 	    CellConverter converter = converters.get(content.getClass());
 	    return converter.convert(content);
 	}
 	return content;
     }
 
     /**
      * Adds a custom type converter.
      * 
      * @param type
      *            The type of object to be converted
      * @param converter
      *            The converter class
      */
     protected void addConverter(Class<?> type, CellConverter converter) {
 	converters.put(type, converter);
     }
 
     /**
      * Overrides the header style.
      * 
      * @param style
      *            The style specification
      */
     protected void setHeaderStyle(CellStyle style) {
 	headerStyle = style;
     }
 
     /**
      * Merges the specified style the the existing header style.
      * 
      * @param style
      *            The style specification
      */
     protected void appendHeaderStyle(CellStyle style) {
 	headerStyle = style;
     }
 
     /**
      * Adds a new style by object type.
      * 
      * @param type
      *            The type of object (before conversion) on which all cells of
      *            that object have the specified style applied.
      * @param style
      *            The style specification
      */
     protected void addTypeStyle(Class<?> type, CellStyle style) {
 	typeStyles.put(type, style);
     }
 
     /**
      * Adds a set of row styles. If more than one is specified they are applied
      * alternated on the lines, this can be used to achieve that grey/white line
      * background alternation.
      * 
      * @param styles
      *            A set of style specifications
      */
     protected void setRowStyle(CellStyle... styles) {
 	rowStyles = Arrays.asList(styles);
     }
 
     /**
      * Sets a header cell to the specified value, converts the object if needed,
      * and decides the style to apply.
      * 
      * @param book
      *            The excel book.
      * @param cell
      *            The header cell to fill.
      * @param value
      *            The value to fill it with.
      */
     protected void setHeaderValue(HSSFWorkbook book, HSSFCell cell, Object value) {
 	setValue(book, cell, value, headerStyle.getStyle(book));
     }
 
     /**
      * Sets a cell to the specified value, converts the object if needed, and
      * decides the style to apply.
      * 
      * @param book
      *            The excel book.
      * @param cell
      *            The cell to fill.
      * @param value
      *            The value to fill it with.
      */
     protected void setValue(HSSFWorkbook book, HSSFCell cell, Object value) {
 	ComposedCellStyle style = new ComposedCellStyle();
 	if (!rowStyles.isEmpty()) {
 	    style.merge(rowStyles.get(cell.getRowIndex() % rowStyles.size()));
 	}
	if (typeStyles.containsKey(value.getClass())) {
 	    style.merge(typeStyles.get(value.getClass()));
 	}
 	setValue(book, cell, value, style.getStyle(book));
     }
 
     private void setValue(HSSFWorkbook book, HSSFCell cell, Object value, HSSFCellStyle style) {
 	if (value != null) {
 	    Object content = convert(value);
 	    if (content instanceof Boolean) {
 		cell.setCellValue((Boolean) content);
 	    } else if (content instanceof Double) {
 		cell.setCellValue((Double) content);
 	    } else if (content instanceof String) {
 		cell.setCellValue((String) content);
 	    } else if (content instanceof Calendar) {
 		cell.setCellValue((Calendar) content);
 	    } else if (content instanceof Date) {
 		cell.setCellValue((Date) content);
 	    } else if (content instanceof RichTextString) {
 		cell.setCellValue((RichTextString) content);
 	    } else {
 		cell.setCellValue(content.toString());
 	    }
 	} else {
 	    cell.setCellValue((String) null);
 	}
 	cell.setCellStyle(style);
     }
 
     abstract void build(WorkbookBuilder book);
 
     public void build(WorkbookExportFormat format, String filename) throws IOException {
 	new WorkbookBuilder().add(this).build(format, filename);
     }
 
     public void build(WorkbookExportFormat format, File file) throws IOException {
 	new WorkbookBuilder().add(this).build(format, file);
     }
 
     public void build(WorkbookExportFormat format, OutputStream output) throws IOException {
 	new WorkbookBuilder().add(this).build(format, output);
     }
 }
