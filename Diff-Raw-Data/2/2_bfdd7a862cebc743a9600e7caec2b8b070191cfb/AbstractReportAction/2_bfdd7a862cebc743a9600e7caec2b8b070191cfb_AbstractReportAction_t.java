 package com.westchase.web.action.report;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.math.BigDecimal;
 import java.math.MathContext;
 import java.math.RoundingMode;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import javax.naming.InitialContext;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.mail.ByteArrayDataSource;
 import org.apache.commons.mail.MultiPartEmail;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.ClientAnchor;
 import org.apache.poi.ss.usermodel.Comment;
 import org.apache.poi.ss.usermodel.CreationHelper;
 import org.apache.poi.ss.usermodel.Drawing;
 import org.apache.poi.ss.usermodel.Font;
 import org.apache.poi.ss.usermodel.IndexedColors;
 import org.apache.poi.ss.usermodel.RichTextString;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.util.CellRangeAddress;
 import org.apache.struts2.interceptor.ServletResponseAware;
 
 import com.westchase.ejb.EmailService;
 import com.westchase.persistence.model.Employee;
 import com.westchase.utils.EmailUtils;
 import com.westchase.web.action.AbstractWestchaseAction;
 
 /**
  * @author marc
  * 
  */
 public abstract class AbstractReportAction extends AbstractWestchaseAction implements ServletResponseAware {
 
 	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HHmm");
 
 	protected final static short TITLE_ROW = 0;
 	protected final static short TITLE_COL = 0;
 //	protected final static short TITLE_WIDTH = 10;
 	protected final static short HEADER_ROW = 2;
 	protected final static short FIRST_DATA_ROW = 3;
 	
 	protected final static short FONT_HEIGHT = 11;
 	protected final static String FONT_NAME = "Arial";
 
 	protected final static String FILE_EXTENSION = ".xlsx";
 	
 	protected final static String EXCEL = "excel";
 	protected final static String EMAIL = "email";
 	protected HttpServletResponse response;
 	protected String type;
 	
 	private EmailService emailServ;
 
 	private InputStream excelStream;
 	
 	private String emailAddresses;
 	private String emailMessage;
 	private String emailSubject;
 	
 	private boolean fixAddressCaps;
 	private boolean westchaseOnly;
 	
 	public AbstractReportAction() {
 		super();
 	}
 	
 	protected String getReportDate() {
 		String dateStr = "";
 		try {
 			dateStr = DATE_FORMAT.format(new Date());
 		} catch (Exception e) {}
 		return dateStr;
 	}
 
 	public String exportToExcel() {
 		ByteArrayOutputStream bos = createWorkbook();
 		
         setExcelStream(new ByteArrayInputStream(bos.toByteArray())); 
 
		//response.setContentType("application/vnd.ms-excel");
 //		response.setContentType("application/excel");
 		response.setHeader("Content-disposition", "attachment; filename=" + getReportFileName() + FILE_EXTENSION);
 
 		setType(null);
 		
 		return EXCEL;
 	}
 	
 	public String sendEmail() {
         try {
         	InitialContext ctx = new InitialContext();
             emailServ = (EmailService) ctx.lookup("westchase/EmailServiceBean/local");
         } catch (Exception e) {
             log.error("", e); 
         } 
 		if (StringUtils.isNotBlank(emailAddresses)) {
 			List<String> tos = new ArrayList<String>();
 			if (StringUtils.indexOf(emailAddresses, ',') > -1) {
 				String[] toArray = emailAddresses.split(",");
 				tos = Arrays.asList(toArray);
 			} else {
 				tos.add(emailAddresses);
 			}
 			try {
 				Employee emp = getEmployee();
 				if (emp != null) {
 				
 					ByteArrayOutputStream bos = createWorkbook();
 					// todo: send email
 			
 					// Create the email message
 					MultiPartEmail email = new MultiPartEmail();
 					// TODO: fix in production
 //					email.setHostName("localhost");
 //			        email.setAuthentication("tesuser", "password");
 //					email.setHostName("172.25.16.2");
 //					email.setHostName("wcd01");
 //			        email.setAuthentication("MRosenthal", "*Westchase2009!");
 //			        email.setTLS(true);
 //			        email.setSSL(false);
 					email.setMailSessionFromJNDI("java:/Mail");
 					email.setFrom("reports@westchasedistrict.com", "Reports");
 	
 //					email.addTo("marcr@alumni.rice.edu", "Marc Rosenthal");
 //		        	email.addTo("mandrosen@gmail.com", "Marc");
 //					email.addTo("testuser@localhost", "Testuser");
 					for (String to : tos) {
 						EmailUtils.addAddress(email, to, null);
 					}
 					
 			        String subj = getEmailSubject();
 			        if (StringUtils.isBlank(subj)) {
 			        	subj = getReportName();
 			        }
 					email.setSubject(subj);
 					
 					String msg = getEmailMessage();
 					if (StringUtils.isBlank(msg)) {
 						msg = "Please see the attached report";
 					}
 					email.setMsg(msg);
 			
 					// add the attachment
 					email.attach(new ByteArrayDataSource(bos.toByteArray(), "application/vnd.ms-excel"), getReportName(), "report");
 			
 					// send the email
 					String messageId = email.send();
 				
 					emailServ.storeSentEmail(emp.getId(), "reports@westchase.org", emailAddresses, subj, msg, bos.toByteArray(), getReportFileName() + FILE_EXTENSION, messageId);
 				}
 			} catch (Exception e) {
 				log.error("", e);
 			}
 		}
 		
 		setType(null);
 
 		return SUCCESS;
 	}	
 	
 	protected String getReportName() {
 		return "Report";
 	}
 	
 	protected String getReportFileName() {
 		return "report";
 	}
 	
 	protected abstract ByteArrayOutputStream createWorkbook();
 
 	protected void writeTitle(Workbook wb, Sheet sheet, String title) {
 		// Setting Background colour for Cells
 		// Colour bckcolor = Colour.DARK_GREEN;
 //		WritableCellFormat cellFormat = new WritableCellFormat();
 //		cellFormat.setBackground(Colour.WHITE);
 		CellStyle style = wb.createCellStyle();
 		style.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
 		
 		// Setting Colour & Font for the Text
 //		WritableFont font = new WritableFont(WritableFont.ARIAL, 11, WritableFont.BOLD);
 		// font.setColour(Colour.GOLD);		
 //		cellFormat.setFont(font);
 		Font font = wb.createFont();
 		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
 		font.setFontName(FONT_NAME);
 		font.setFontHeightInPoints(FONT_HEIGHT);
 		style.setFont(font);
 		
 //		Label label = new Label(TITLE_COL, TITLE_ROW, title);
 //		sheet.addCell(label);
 //		WritableCell cell = sheet.getWritableCell(TITLE_COL, TITLE_ROW);
 //		cell.setCellFormat(cellFormat);
 		Row row = sheet.createRow(TITLE_ROW);
 		Cell cell = row.createCell(TITLE_COL);
 		cell.setCellStyle(style);
 		cell.setCellValue(title);
 	}
 
 	public String getEmailAddresses() {
 		return emailAddresses;
 	}
 
 	public void setEmailAddresses(String emailAddresses) {
 		this.emailAddresses = emailAddresses;
 	}
 
 	public String getEmailMessage() {
 		return emailMessage;
 	}
 
 	public void setEmailMessage(String emailMessage) {
 		this.emailMessage = emailMessage;
 	}
 
 	public String getEmailSubject() {
 		return emailSubject;
 	}
 
 	public void setEmailSubject(String emailSubject) {
 		this.emailSubject = emailSubject;
 	}
 
 	protected void writeHeaders(Workbook wb, Sheet sheet, String[] headers) {
 		writeHeaders(wb, sheet, headers, 0);
 	}
 
 	protected void writeHeaders(Workbook wb, Sheet sheet, String[] headers, int moveDown) {
 		// Setting Background colour for Cells
 //		WritableCellFormat cellFormat = new WritableCellFormat();
 //		cellFormat.setBackground(Colour.WHITE);
 		CellStyle style = wb.createCellStyle();
 		style.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
 
 		// Setting Colour & Font for the Text
 //		WritableFont font = new WritableFont(WritableFont.ARIAL, 11, WritableFont.BOLD);
 //		font.setColour(Colour.BLACK);
 //		cellFormat.setFont(font);
 		Font font = wb.createFont();
 		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
 		font.setFontName(FONT_NAME);
 		font.setFontHeightInPoints(FONT_HEIGHT);
 		style.setFont(font);
 		
 
 		Row row = sheet.createRow(HEADER_ROW + moveDown);
 		for (int i = 0; i < headers.length; i++) {
 //			Label label = new Label(i, HEADER_ROW, headers[i]);
 //			sheet.addCell(label);
 //			WritableCell cell = sheet.getWritableCell(i, HEADER_ROW);
 //			cell.setCellFormat(cellFormat);
 			Cell cell = row.createCell(i);
 			cell.setCellStyle(style);
 			cell.setCellValue(headers[i]);
 		}
 	}
 	
 	protected void writeSuperHeaderCell(Workbook wb, Sheet sheet, String value, int rowNum, int startCol, int endCol) {
 		CellStyle style = wb.createCellStyle();
 		style.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
 		style.setAlignment(CellStyle.ALIGN_GENERAL);
 
 		Font font = wb.createFont();
 		font.setBoldweight(Font.BOLDWEIGHT_BOLD);
 		font.setFontName(FONT_NAME);
 		font.setFontHeightInPoints(FONT_HEIGHT);
 		style.setFont(font);
 		
 
 		Row row = sheet.createRow(rowNum);
 		Cell cell = row.createCell(startCol);
 		cell.setCellStyle(style);
 		cell.setCellValue(value);
 		
 		for (int i = startCol; i <= endCol; i++) {
 			Cell cellTemp = row.createCell(startCol);
 			cellTemp.setCellStyle(style);
 		}
 		sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, startCol, endCol));
 	}
 	
 	protected void writeCell(Workbook wb, Sheet sheet, Row row, int col, String value, CellStyle style) throws Exception {
 		try {
 			Cell cell = row.createCell(col);
 			cell.setCellStyle(style);
 			if (value == null) {
 				cell.setCellValue("");
 			} else {
 				cell.setCellValue(value);
 			}
 		} catch (Exception e) {
 			log.error("unable to write string " + value + " to cell", e);
 			throw e;
 		}
 	}
 	
 	protected void writeCell(Workbook wb, Sheet sheet, Row row, int col, Integer value, CellStyle style) throws Exception {
 		try {
 			Cell cell = row.createCell(col);
 			cell.setCellStyle(style);
 			cell.setCellValue(value);
 			cell.setCellType(Cell.CELL_TYPE_NUMERIC);
 		} catch (Exception e) {
 			log.error("unable to write integer " + value + " to cell", e);
 			throw e;
 		}
 	}
 	
 	protected void writeCell(Workbook wb, Sheet sheet, Row row, int col, Long value, CellStyle style) throws Exception {
 		try {
 			Cell cell = null;
 			try {
 				cell = row.createCell(col);
 			} catch (Exception e) {
 				System.out.println("\n\n\n\n error writing " + value + " to cell [" + row + " ," + col + "]\n\n\n\n");
 			}
 			if (cell != null) {
 				cell.setCellStyle(style);
 				if (value == null) {
 					cell.setCellValue(0);
 				} else {
 					cell.setCellValue(value);
 				}
 				cell.setCellType(Cell.CELL_TYPE_NUMERIC);
 			}
 		} catch (Exception e) {
 			log.error("unable to write long " + value + " to cell", e);
 			throw e;
 		}
 	}
 	
 	protected void writeCell(Workbook wb, Sheet sheet, Row row, int col, Double value, CellStyle style) throws Exception {
 		try {
 			Cell cell = row.createCell(col);
 			cell.setCellStyle(style);
 			cell.setCellValue(value);
 			cell.setCellType(Cell.CELL_TYPE_NUMERIC);
 		} catch (Exception e) {
 			log.error("unable to write double " + value + " to cell", e);
 			throw e;
 		}
 	}
 
 	protected void writeCellWithComment(Workbook wb, Sheet sheet, Row row, int col, Long value, String cellCommentVal, CellStyle style) throws Exception {
 		try {
 			Cell cell = row.createCell(col);
 			cell.setCellStyle(style);
 			cell.setCellValue(value);
 			cell.setCellType(Cell.CELL_TYPE_NUMERIC);
 
 			// TODO Auto-generated method stub
 	        Drawing drawing = sheet.createDrawingPatriarch();
 	        
 	        CreationHelper factory = wb.getCreationHelper();
 
 	        ClientAnchor anchor = factory.createClientAnchor();
 
 	        Comment comment = drawing.createCellComment(anchor);
 	        RichTextString str1 = factory.createRichTextString(cellCommentVal);
 	        comment.setString(str1);
 //	        comment.setAuthor("Apache POI");
 	        cell.setCellComment(comment);
 			
 		} catch (Exception e) {
 			log.error("unable to write double " + value + " and comment " + cellCommentVal + " to cell", e);
 			throw e;
 		}
 	}
 	
 	protected void writeCellPct(Workbook wb, Sheet sheet, Row row, int col, Double value, CellStyle style) throws Exception {
 		try {
 			
 			Cell cell = row.createCell(col);
 			cell.setCellStyle(style);
 			if (value != null) {
 				BigDecimal bd = new BigDecimal(value, new MathContext(3, RoundingMode.HALF_UP));
 				cell.setCellValue(bd.doubleValue() / 100);
 			}
 			cell.setCellType(Cell.CELL_TYPE_NUMERIC);
 		} catch (Exception e) {
 			log.error("unable to write double pct " + value + " to cell", e);
 			throw e;
 		}
 	}
 
 	public InputStream getExcelStream() {
 		return excelStream;
 	}
 
 	public void setExcelStream(InputStream excelStream) {
 		this.excelStream = excelStream;
 	}
 
 	@Override
 	public void setServletResponse(HttpServletResponse response) {
 		this.response = response;
 	}
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public boolean isFixAddressCaps() {
 		return fixAddressCaps;
 	}
 
 	public void setFixAddressCaps(boolean fixAddressCaps) {
 		this.fixAddressCaps = fixAddressCaps;
 	}
 	
 	protected void setBorders(CellStyle style) {
 		style.setBorderBottom(CellStyle.BORDER_THIN);
 	    style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
 	    style.setBorderLeft(CellStyle.BORDER_THIN);
 	    style.setLeftBorderColor(IndexedColors.GREEN.getIndex());
 	    style.setBorderRight(CellStyle.BORDER_THIN);
 	    style.setRightBorderColor(IndexedColors.BLUE.getIndex());
 	    style.setBorderTop(CellStyle.BORDER_THIN);
 	    style.setTopBorderColor(IndexedColors.BLACK.getIndex());
 	}
 
 	protected void fixColumns(Sheet sheet, int cols) {
 		for (int i = 0; i < cols; i++) {
 			//		sheet.setColumnWidth(i, 25);
 			sheet.autoSizeColumn(i);
 		}
 	
 
 		// merge the title cell across all columns
 		// assumes title in cell 0!
 		sheet.addMergedRegion(new CellRangeAddress(
 				TITLE_ROW, //first row (0-based)
 				TITLE_ROW, //last row  (0-based)
 				0, //first column (0-based)
 				cols - 1  //last column  (0-based)
 	    ));
 	}
 
 	public boolean isWestchaseOnly() {
 		return westchaseOnly;
 	}
 
 	public void setWestchaseOnly(boolean westchaseOnly) {
 		this.westchaseOnly = westchaseOnly;
 	}
 }
