 package com.westchase.web.action.hcad;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.text.NumberFormat;
 import java.text.SimpleDateFormat;
 import java.util.Collections;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.CreationHelper;
 import org.apache.poi.ss.usermodel.Font;
 import org.apache.poi.ss.usermodel.IndexedColors;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 
 import com.westchase.file.HcadTaxFileReader;
 import com.westchase.file.beans.TaxRecord;
 import com.westchase.file.beans.TaxRecordByNameComparator;
 
 public class HcadTaxFileAction extends AbstractHcadFileAction {
 	
 	private File taxFile;
 	private File addressFile;
 	private File exemptionFile;
 	private File totalFile;
 	
 	private double assessmentRate;
 	
 	public HcadTaxFileAction() {
 		super();
 	}
 	
 	public String init() {
 		return SUCCESS;
 	}
 	
 	public String generate() {
 		List<TaxRecord> taxRecordList = new HcadTaxFileReader().readTaxFile(getTaxFile(), getAddressFile(), getExemptionFile(), getAssessmentRate(), getTotalFile());
 		ByteArrayOutputStream bos = createWorkbook(taxRecordList);
 		setExcelStream(new ByteArrayInputStream(bos.toByteArray()));
 		setFileName("hcad_taxes_" + new SimpleDateFormat("yyyyMMddHHmm").format(new Date()) + ".xlsx");
 		setContentLength(bos.size());
 		return "excel";
 	}
 	
 	public File getTaxFile() {
 		return taxFile;
 	}
 
 	public void setTaxFile(File taxFile) {
 		this.taxFile = taxFile;
 	}
 
 	public File getAddressFile() {
 		return addressFile;
 	}
 
 	public void setAddressFile(File addressFile) {
 		this.addressFile = addressFile;
 	}
 
 	public File getExemptionFile() {
 		return exemptionFile;
 	}
 
 	public void setExemptionFile(File exemptionFile) {
 		this.exemptionFile = exemptionFile;
 	}
 	
 	protected void createSheet(Workbook wb, String name, List<TaxRecord> taxRecordList) {
 		Sheet sheet = wb.createSheet(name);
 	    
 	    CreationHelper createHelper = wb.getCreationHelper();
 	    
 	    // Background colour for Cells
 
 		CellStyle headerStyle = wb.createCellStyle();
 		headerStyle.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
 		headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
 		
 
 		CellStyle style = wb.createCellStyle();
 		style.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
 		
 		CellStyle centerStyle = wb.createCellStyle();
 		centerStyle.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
 		centerStyle.setAlignment(CellStyle.ALIGN_CENTER);
 
 		CellStyle accountNumberStyle = wb.createCellStyle();
 		accountNumberStyle.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
 //		accountNumberStyle.setDataFormat(createHelper.createDataFormat().getFormat("000-000-000-0000"));
 		accountNumberStyle.setAlignment(CellStyle.ALIGN_CENTER);
 		
 
 		CellStyle acresNumberStyle = wb.createCellStyle();
 		acresNumberStyle.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
 		acresNumberStyle.setDataFormat(createHelper.createDataFormat().getFormat("0.0000"));
 		acresNumberStyle.setAlignment(CellStyle.ALIGN_RIGHT);
 
 		
 		CellStyle landValueNumberStyle = wb.createCellStyle();
 		landValueNumberStyle.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
 		landValueNumberStyle.setDataFormat(createHelper.createDataFormat().getFormat("0,000"));
 		landValueNumberStyle.setAlignment(CellStyle.ALIGN_RIGHT);
 		
 
 		CellStyle assessmentValueStyle = wb.createCellStyle();
 		assessmentValueStyle.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
 		assessmentValueStyle.setDataFormat(createHelper.createDataFormat().getFormat("0,000.00"));
 		assessmentValueStyle.setAlignment(CellStyle.ALIGN_RIGHT);
 		
 		
 		// Colour & Font for the Text
 		Font font = wb.createFont();
 		font.setFontName("Arial");
 		font.setFontHeightInPoints((short) 10);
 
 		Font headerFont = wb.createFont();
 		headerFont.setFontName("Arial");
 		headerFont.setFontHeightInPoints((short) 10);
 		headerFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
 		
 		headerStyle.setFont(headerFont);
 		
 		style.setFont(font);
 		centerStyle.setFont(font);
 		accountNumberStyle.setFont(font);
 		acresNumberStyle.setFont(font);
 		landValueNumberStyle.setFont(font);
 		assessmentValueStyle.setFont(font);
 		
 
        String[] headers = { "Account Number", "Jur", "Year", "Owner", "Acres", "Use Code", "Land Value", "Improvements Value", "Total Value", "WD Exemptions", "Taxable Value", "Assessments @" + formatAssessmentRate() + "/100", "Certified", "HCAD Text" };
         writeHeaders(sheet, headerStyle, 0, headers);
         
         int rowCount = 0;
         double totalAcres = 0;
         long totalLandVal = 0;
         long totalImpVal = 0;
         long totalTotalVal = 0;
         long totalTaxVal = 0;
         double totalHcadTotalVal = 0;
         BigDecimal totalAssessmentVal = new BigDecimal(0);
         
         int rowNum = 1;
         Row firstRow = sheet.createRow(1);
 		for (TaxRecord taxRecord : taxRecordList) {
 			
 			Row row = rowNum == 1 ? firstRow : sheet.createRow(rowNum);
 			int col = 0;
 			if (!taxRecord.isMissingRecord()) {
 				writeCell(sheet, accountNumberStyle, row, col++, formatAccountNumber(taxRecord.getAccountNumber()));
 				writeCell(sheet, centerStyle, row, col++, taxRecord.getJurisdiction());
 				writeCell(sheet, centerStyle, row, col++, taxRecord.getYear());
 				writeCell(sheet, style, row, col++, taxRecord.getOwner());
 				writeCell(sheet, acresNumberStyle, row, col++, taxRecord.getAcres());
 				writeCell(sheet, centerStyle, row, col++, taxRecord.getUseCode());
 				writeCell(sheet, landValueNumberStyle, row, col++, taxRecord.getLandValue());
 				if (taxRecord.getImprovementValue() == 0) {
 					writeCell(sheet, centerStyle, row, col++, "-");
 				} else {
 					writeCell(sheet, landValueNumberStyle, row, col++, taxRecord.getImprovementValue());
 				}
 				writeCell(sheet, landValueNumberStyle, row, col++, taxRecord.getTotalValue());
 				writeCell(sheet, style, row, col++, taxRecord.getWdExemptions());
 				writeCell(sheet, landValueNumberStyle, row, col++, taxRecord.getTaxableValue());
 				writeCell(sheet, assessmentValueStyle, row, col++, taxRecord.getAssessments().setScale(2, RoundingMode.HALF_UP).toString());
 				writeCell(sheet, centerStyle, row, col++, taxRecord.getCertified());
 	            if (taxRecord.getTotalFromHcad() != null) {
 	            	writeCell(sheet, assessmentValueStyle, row, col++, taxRecord.getTotalFromHcad());
 				}
 
 			
 				totalAcres += taxRecord.getAcres();
 				
 	            totalLandVal += taxRecord.getLandValue();
 	            totalImpVal += taxRecord.getImprovementValue();
 	            totalTotalVal += taxRecord.getTotalValue();
 	            totalTaxVal += taxRecord.getTaxableValue();
 	            totalAssessmentVal.add(taxRecord.getAssessments());
 
 	            
 	            if (taxRecord.getTotalFromHcad() != null) {
 	            	totalHcadTotalVal += taxRecord.getTotalFromHcad().doubleValue();
 	            }
 	            
 				rowCount++;
 				
 				rowNum++;
             
 			}
 		}
 		
 		rowNum += 2;
 		
 		
 		// create totals row
 		Row totalRow = sheet.createRow(rowNum);
 		int col = 0;
 		writeCell(sheet, headerStyle, totalRow, col++, rowCount);
 		col++;
 		col++;
 		col++;
 		writeCell(sheet, acresNumberStyle, totalRow, col++, totalAcres);
 		col++;
 		writeCell(sheet, landValueNumberStyle, totalRow, col++, totalLandVal);
 		writeCell(sheet, landValueNumberStyle, totalRow, col++, totalImpVal);
 		writeCell(sheet, landValueNumberStyle, totalRow, col++, totalTotalVal);
 		col++;
 		writeCell(sheet, landValueNumberStyle, totalRow, col++, totalTaxVal);
 		writeCell(sheet, assessmentValueStyle, totalRow, col++, totalAssessmentVal.setScale(2, RoundingMode.HALF_UP).toString());
 		col++;
 		writeCell(sheet, assessmentValueStyle, totalRow, col++, totalHcadTotalVal);
 		
 		rowNum += 5;
 		
 		Row missing = sheet.createRow(rowNum);
 		writeCell(sheet, style, missing, 0, "Missing:");
 		rowNum++;
 		// now add the missing records
 		for (TaxRecord taxRecord : taxRecordList) {
 			
 			Row row = sheet.createRow(rowNum);
 			col = 0;
 			if (taxRecord.isMissingRecord()) {
 				writeCell(sheet, accountNumberStyle, row, col++, formatAccountNumber(taxRecord.getAccountNumber()));
 				
 	            if (taxRecord.getTotalFromHcad() != null) {
 	            	writeCell(sheet, assessmentValueStyle, row, col++, taxRecord.getTotalFromHcad());
 				}
 	    		rowNum++;
 			}
 		}
 	}
 	
 	protected ByteArrayOutputStream createWorkbook(List<TaxRecord> taxRecordList) {
 		ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
 		try {
 			Workbook wb = new XSSFWorkbook();
 		    
 			createSheet(wb, "Numeric Order", taxRecordList);
 			
 			Collections.sort(taxRecordList, TaxRecordByNameComparator.getInstance());
 		    
 			createSheet(wb, "Alphabetical Order", taxRecordList);
     		
 			wb.write(bos);
 			bos.close();
             
 		} catch (Exception e) {
 			log.error("", e);
 		}
     	
 		return bos;
 	}
 
 	private String formatAccountNumber(String accountNumber) {
 		if (StringUtils.isBlank(accountNumber)) return "";
 		String firstThree = StringUtils.substring(accountNumber, 0, 3);
 		String secondThree = StringUtils.substring(accountNumber, 3, 6);
 		String thirdThree = StringUtils.substring(accountNumber, 6, 9);
 		String lastFour = StringUtils.substring(accountNumber, 9, 13);
 		StringBuffer buf = new StringBuffer(firstThree).append("-").append(secondThree).append("-").append(thirdThree).append("-").append(lastFour);
 		return buf.toString();
 	}
 
 	private String formatAssessmentRate() {
 		NumberFormat nf = NumberFormat.getNumberInstance();
 		nf.setMaximumFractionDigits(2);
 		nf.setMinimumFractionDigits(2);
 		return nf.format(getAssessmentRate());
 	}
 
 	public double getAssessmentRate() {
 		return assessmentRate;
 	}
 
 	public void setAssessmentRate(double assessmentRate) {
 		this.assessmentRate = assessmentRate;
 	}
 
 	public File getTotalFile() {
 		return totalFile;
 	}
 
 	public void setTotalFile(File totalFile) {
 		this.totalFile = totalFile;
 	}
 }
