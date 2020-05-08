 package challenge_it.racbit.model.reports.generators;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.poi.hssf.util.HSSFColor;
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.CellStyle;
 import org.apache.poi.ss.usermodel.CellValue;
 import org.apache.poi.ss.usermodel.ComparisonOperator;
 import org.apache.poi.ss.usermodel.IndexedColors;
 import org.apache.poi.ss.usermodel.Row;
 import org.apache.poi.ss.usermodel.Sheet;
 import org.apache.poi.ss.usermodel.SheetConditionalFormatting;
 import org.apache.poi.ss.usermodel.Workbook;
 import org.apache.poi.ss.util.CellRangeAddress;
 import org.apache.poi.ss.util.CellReference;
 import org.apache.poi.xssf.usermodel.XSSFConditionalFormattingRule;
 import org.apache.poi.xssf.usermodel.XSSFFormulaEvaluator;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 
 import challenge_it.racbit.model.core.Country;
 import challenge_it.racbit.model.core.IReportGenerator;
 import challenge_it.racbit.model.core.Product;
 import challenge_it.racbit.model.core.Product.InsurancePackage;
 import challenge_it.racbit.model.core.exceptions.CurrencyConversionException;
 import challenge_it.racbit.model.core.exceptions.ReportGenerationException;
 import challenge_it.racbit.model.reports.configurations.Broker;
 import challenge_it.racbit.model.reports.configurations.CrossReference;
 import challenge_it.racbit.model.reports.configurations.RateShopReportConfiguration;
 import challenge_it.racbit.model.reports.configurations.RateShopReportConfigurationReader;
 import challenge_it.racbit.model.reports.exchangeRate.ExchangeRateService;
 
 /**
  * Class that generates a RateShopUK Report
  * 
  *  @author Ctia Moreira e Joo Taborda
  *
  */
 public class RateShopUKReportGenerator implements IReportGenerator {
 	
 	/**
 	  * Class that generates a RateShopUK Report
 	 * 
 	 * Informations: 
 	 * 
 	 * 1. Since we are not using any template is always necessary to 
 	 * check whether the Row is created before using it. 
 	 * (The Rows and the Cells don't exist unless we create them)
 	 * 
 	 * 2. When merging cell with style, is necessary to apply the style
 	 * to all that are going to be merged
 	 * 
 	 *  @author Ctia Moreira e Joo Taborda
 	 *
 	 *
 	 */
 	private class RateShopUKReportInfo{
 		
 		/**
 		 * The start date of the report
 		 */
 		private Calendar _startDate;
 		
 		/**
 		 * The end date of the report
 		 */
 		private Calendar _endDate;
 		
 		/**
 		 * The destination referred by the report
 		 */
 		private String _destination;
 		
 		public RateShopUKReportInfo(Calendar puDate, Calendar doDate, String destination){
 			_startDate = puDate;
 			_endDate = doDate;
 			_destination = destination;
 		}
 		
 		/**
 		 * Gets the start date
 		 * 
 		 * @return the start date
 		 */
 		public Calendar getStartDate(){
 			return _startDate;
 		}
 		
 		/**
 		 * Gets the end date
 		 * 
 		 * @return the end date
 		 */
 		public Calendar getEndDate(){
 			return _endDate;
 		}
 		
 		/**
 		 * Gets the destination
 		 * 
 		 * @return the destination
 		 */
 		public String getDestination(){
 			return _destination;
 		}
 	}
 	
 	/**
 	 * Path to the xml configuration file
 	 */
 	private static final String XML_CONFIGURATION = "configuration/RateShop_Reports/UK_Reports/RateShopUKReportConfiguration.xml";
 	
 	/**
 	 * Path to the xml schema file
 	 */
 	private static final String XML_SCHEMA = "configuration/RateShop_Reports/RateShopReportSchema.xsd";
 	
 	/**
 	 * Path to the xml transformation file
 	 */
 	private static final String XML_TRANSFORMATION = "configuration/ReportTransformation.xsl";
 	
 	/**
 	 * Generates the UK report
 	 * 
 	 * The execution order is as follows:
 	 * 1. Read XML configuration file and apply the schema and the transformation using RateShopReportConfiguration class
 	 * 2. Set the info used by RateShopUKReportInfo 
 	 * 3. Combine the information obtained from XML file with the information from the iterator to fill the table with the values 
 	 * 3.1. The styles for the table cells are applied at the same time
 	 * 3.2. The column used for minimum values is created separately for each broker 
 	 * 3.3. The line that has the suppliers names is also created for each broker
 	 * 3.4. Because the table could have more than one broker, there is a pointer that should be set after the steps above
 	 * 4. Fill the column with the groups names
 	 * 5. Save the file
 	 * 
 	 * @param reportDate The date that should be on the file name
 	 * @param country The country that should be on the file name
 	 * @param results The iterator which have the values used to fill the table
 	 * @throws ReportGenerationException, CurrencyConversionException
 	 */
 	@Override
 	public void generate(Calendar reportDate, Country country, Iterable<Product> results) throws ReportGenerationException, CurrencyConversionException {
 		try {
 			
 			RateShopReportConfiguration config = (RateShopReportConfiguration) new RateShopReportConfigurationReader().read(XML_CONFIGURATION, XML_SCHEMA, XML_TRANSFORMATION);
 			
 			Workbook workbook = new XSSFWorkbook();
 			Sheet sheet = workbook.createSheet();
 			
 			RateShopUKReportInfo reportInfo = completeBrokerInformation(config, results);
 						
 			int brokerFirstSupplierIndex = config.getGridValuesFirstCell().getColumn();
 				
 			for(String brokerName : config.getBrokersList())
 			{
 				Broker broker = config.getBrokers().get(brokerName);
 				
 				for (Product product : broker.getProducts()) 
 				{
 					setProductCell(workbook, sheet, config, broker, brokerFirstSupplierIndex, product);
 				}
 				
 				if(broker.hasMinimum()){
 					setMinimumColumn(workbook, sheet, config, broker, brokerFirstSupplierIndex);
 					setMinimumColor(workbook, sheet, config, broker, brokerFirstSupplierIndex);
 				}
 				
 				setTableCellsWithoutValue(workbook, sheet, config, broker, brokerFirstSupplierIndex);
 
 				fillSuppliersHeader(workbook, sheet, config, broker, brokerFirstSupplierIndex);
 				
 				brokerFirstSupplierIndex += broker.getSuppliersList().size() + (broker.hasMinimum()?1:0);
 			}
 			
 			fillGroups(workbook, sheet, config);
 			setFixedValues(workbook, sheet, config, reportInfo);
 			saveFile(workbook, reportInfo, reportDate, country);
 		}
 		catch (CurrencyConversionException e){
 			throw e;
 		}
 		catch (Exception e) {
 			throw new ReportGenerationException(e);
 		}
 	}
 	
 	/**
 	 * Gets the complete information about a broker from the results
 	 * This is used to know the concrete number of suppliers each broker
 	 * 
 	 * @param config The object that holds the information read from XML file
 	 * @param results The iterator which have the values used to fill the table
 	 * @return the information about the destination and the days to fill the report values
 	 */
 	private RateShopUKReportInfo completeBrokerInformation(RateShopReportConfiguration config, Iterable<Product> results) {
 		RateShopUKReportInfo reportInfo = null;
 		
 		Map<String, Broker> brokers = config.getBrokers();
 		
 		for (Product product : results) {
 			
 			if(reportInfo == null){
 				Calendar puDate = Calendar.getInstance();
 				puDate.setTime(product.getStartDate());
 				
 				Calendar doDate = Calendar.getInstance();
 				doDate.setTime(product.getStartDate());
 				doDate.add(Calendar.DAY_OF_MONTH, product.getNumberOfDays());
 				
 				reportInfo = new RateShopUKReportInfo(puDate, doDate, product.getLocation());
 			}
 			
 			Broker b = brokers.get(product.getBroker());
 			
 			if(b != null){
 				
 				if(!b.getSuppliersList().contains(product.getSupplier())){
 					b.addSupplier(product.getSupplier(), new CrossReference(config.getGridValuesFirstCell().getRow()-1, b.getSuppliersList().size()));
 				}
 				b.addProduct(product);
 			}
 			
 		}
 		
 		return reportInfo;
 	}
 	
 	/**
 	 * Place the product in the correct cell
 	 * 
 	 * @param workbook The representation of the file
 	 * @param sheet The representation of the sheet
 	 * @param config The object that holds the information read from XML file
 	 * @param broker The broker that has the suppliers
 	 * @param brokerFirstCell The index of the designated broker
 	 * @param product The object that holds the product information
 	 */
 	private void setProductCell(Workbook workbook, Sheet sheet, RateShopReportConfiguration config, Broker broker, int brokerFirstCell, Product product) {
 		CrossReference supplierCell = broker.getSuppliersMap().get(product.getSupplier());
 		CrossReference groupCell =  config.getGroupsMap().get(product.getGroup().toString());
 		
 		if(supplierCell != null && groupCell != null)
 		{
 			Row poundRow = sheet.getRow(groupCell.getRow());
 			Row euroRow = sheet.getRow(groupCell.getRow() + config.getConversionTableOffset());
 			
 			if(poundRow == null)
 				poundRow = sheet.createRow(groupCell.getRow());
 			
 			if(euroRow == null)
 				euroRow = sheet.createRow(groupCell.getRow() + config.getConversionTableOffset());
 			
 			
 			Cell cellWithPoundValue = poundRow.createCell(supplierCell.getColumn() + brokerFirstCell);
 			Cell cellWithEuroValue = euroRow.createCell(supplierCell.getColumn() + brokerFirstCell);
 			
 			CellStyle style = CellStyles.setThinBorders(CellStyles.getDefaultCellStyle(workbook));
 
 			if(product.getInsurancePackage() == InsurancePackage.NO_EXCESS)
 				style = CellStyles.setBackground(style, HSSFColor.YELLOW.index);
 			else 
 				if(product.getInsurancePackage() == InsurancePackage.FULLY_REFUNDABLE)
 					style = CellStyles.setBackground(style, HSSFColor.LIGHT_YELLOW.index);
 
 			cellWithPoundValue.setCellStyle(style);
 			cellWithPoundValue.setCellValue(product.getPrice());
 			cellWithPoundValue.setCellType(Cell.CELL_TYPE_NUMERIC);
 			
 			
 			cellWithEuroValue.setCellFormula(CellReference.convertNumToColString(supplierCell.getColumn() + brokerFirstCell) + (groupCell.getRow()+1)
 					+ "/" + CellReference.convertNumToColString(config.getRateCell().getColumn()) + (config.getRateCell().getRow()+1));
 			cellWithEuroValue.setCellType(Cell.CELL_TYPE_FORMULA);
 			cellWithEuroValue.setCellStyle(style);
 			
 		}
 	}
 	
 	/**
 	 * Used to create a minimum column for brokers that requires it
 	 * 
 	 * @param workbook The representation of the file
 	 * @param sheet The representation of the sheet
 	 * @param config The object that holds the information read from XML file
 	 * @param broker The broker that has the suppliers
 	 * @param brokerFirstCell The index of the designated broker
 	 */
 	private void setMinimumColumn(Workbook workbook, Sheet sheet, RateShopReportConfiguration config, Broker broker, int brokerFirstCell) {
 		final int column = brokerFirstCell + broker.getSuppliersList().size();
 		final String columnLetter = CellReference.convertNumToColString(brokerFirstCell);
 				
 		Cell euroMinimum = null;
 		Cell poundMinimum = null;
 		
 		// Set the Minimum function
 		for (int row = config.getGridValuesFirstCell().getRow(); row < config.getGridValuesFirstCell().getRow() + config.getGroupsList().size(); row++) { 	
 			
 			Row poundRow = sheet.getRow(row);
 			Row euroRow = sheet.getRow(row + config.getConversionTableOffset());
 			
 			if(poundRow == null)
 				poundRow = sheet.createRow(row);
 			
 			if(euroRow == null)
 				euroRow = sheet.createRow(row + config.getConversionTableOffset());
 			
 			poundMinimum = poundRow.createCell(column);		
 			poundMinimum.setCellStyle(CellStyles.setMediumRightBorder(CellStyles.setThinBorders(CellStyles.getDefaultCellStyle(workbook))));
 			poundMinimum.setCellFormula("MIN(" + columnLetter + (row+1) +":INDIRECT(ADDRESS(ROW(),COLUMN()-1,4)))");
 			poundMinimum.setCellType(Cell.CELL_TYPE_FORMULA);
 			
 			euroMinimum = euroRow.createCell(column);
 			euroMinimum.setCellStyle(CellStyles.setMediumRightBorder(CellStyles.setThinBorders(CellStyles.getDefaultCellStyle(workbook))));
 			euroMinimum.setCellFormula("MIN(" + columnLetter + (row + config.getConversionTableOffset() +1) +":INDIRECT(ADDRESS(ROW(),COLUMN()-1,4)))");
 			euroMinimum.setCellType(Cell.CELL_TYPE_FORMULA);
 		}
 		
 		poundMinimum.setCellStyle(CellStyles.setMediumBottomBorder(CellStyles.setMediumRightBorder(CellStyles.setThinBorders(CellStyles.getDefaultCellStyle(workbook)))));
 		euroMinimum.setCellStyle(CellStyles.setMediumBottomBorder(CellStyles.setMediumRightBorder(CellStyles.setThinBorders(CellStyles.getDefaultCellStyle(workbook)))));		
 		
 		
 		Row poundRow = sheet.getRow(config.getGridValuesFirstCell().getRow()-1);
 		Row euroRow = sheet.getRow(config.getGridValuesFirstCell().getRow()-1 +  config.getConversionTableOffset());
 		
 		if(poundRow == null)
 			poundRow = sheet.createRow(config.getGridValuesFirstCell().getRow()-1);
 		
 		if(euroRow == null)
 			euroRow = sheet.createRow(config.getGridValuesFirstCell().getRow()-1 +  config.getConversionTableOffset());
 		
 		// Define the Minimum header with the name in the XML Configuration 
 		Cell poundMininumHeader = poundRow.createCell(column);	
 		poundMininumHeader.setCellStyle(CellStyles.setMediumTopBorder(CellStyles.setMediumBottomBorder(CellStyles.setMediumRightBorder(CellStyles.setThinBorders(CellStyles.getDefaultCellStyle(workbook))))));
 		poundMininumHeader.setCellValue(broker.getMinimumColumnName());
 		poundMininumHeader.setCellType(Cell.CELL_TYPE_STRING);
 		
 		Cell euroMininumHeader = euroRow.createCell(column);
 		euroMininumHeader.setCellStyle(CellStyles.setMediumTopBorder(CellStyles.setMediumBottomBorder(CellStyles.setMediumRightBorder(CellStyles.setThinBorders(CellStyles.getDefaultCellStyle(workbook))))));
 		euroMininumHeader.setCellValue(broker.getMinimumColumnName());
 		euroMininumHeader.setCellType(Cell.CELL_TYPE_STRING);
 		
 	}		
 	
 	/**
 	 * Set the cell font color to red if is value is the same as the minimum column value
 	 * 
 	 * @param workbook The representation of the file
 	 * @param sheet The representation of the sheet
 	 * @param config The object that holds the information read from XML file
 	 * @param broker The broker that has the suppliers
 	 * @param brokerFirstCell The index of the designated broker
 	 */
 	private void setMinimumColor(Workbook workbook, Sheet sheet, RateShopReportConfiguration config, Broker broker, int brokerFirstCell) {
 		final int lastColumn = brokerFirstCell + broker.getSuppliersList().size();
 		final int firstRow = config.getGridValuesFirstCell().getRow();
 		
 		for(int i = firstRow; i < firstRow+config.getGroupsList().size(); i++){
 			
 			CellValue cellEuroMinimumValue = workbook.getCreationHelper().createFormulaEvaluator().evaluate(sheet.getRow(i).getCell(lastColumn));
 			CellValue cellPoundMinimumValue = workbook.getCreationHelper().createFormulaEvaluator().evaluate(sheet.getRow(i+config.getConversionTableOffset()).getCell(lastColumn));
 			
 			SheetConditionalFormatting cf = sheet.getSheetConditionalFormatting();
 
 			XSSFConditionalFormattingRule euroRule = (XSSFConditionalFormattingRule) cf.createConditionalFormattingRule(
 			     ComparisonOperator.EQUAL, 
 			     cellEuroMinimumValue.getNumberValue() + "",
 			     null
 			);
 			
 			XSSFConditionalFormattingRule poundRule = (XSSFConditionalFormattingRule) cf.createConditionalFormattingRule(
 				     ComparisonOperator.EQUAL, 
 				     cellPoundMinimumValue.getNumberValue() + "",
 				     null
 				);
 
 			 // Create pattern with red background
 			euroRule.createFontFormatting().setFontColorIndex(HSSFColor.RED.index);
 			poundRule.createFontFormatting().setFontColorIndex(HSSFColor.RED.index);
 
 			 // Define a region containing first column
 			CellRangeAddress[] euroCRA = {new CellRangeAddress(i, i, brokerFirstCell, lastColumn-1) };
 			CellRangeAddress[] poundCRA = {new CellRangeAddress(i+config.getConversionTableOffset(), i+config.getConversionTableOffset(), brokerFirstCell, lastColumn-1) };
 			
 			// Apply Conditional Formatting rule defined above to the regions  
 			cf.addConditionalFormatting(euroCRA, euroRule);
 			cf.addConditionalFormatting(poundCRA, poundRule);
 		}		
 	}
 	
 	/**
 	 * Searches the table for cells that doesn't have any value and creates them with a specific style
 	 * 
 	 * @param workbook The representation of the file
 	 * @param sheet The representation of the sheet
 	 * @param config The object that holds the information read from XML file
 	 * @param broker The broker which table will be read
 	 * @param brokerFirstCell The initial column to start read
 	 */
 	private void setTableCellsWithoutValue(Workbook workbook, Sheet sheet, RateShopReportConfiguration config, Broker broker, int brokerFirstCell) {
 
 		final int lastRow = config.getGridValuesFirstCell().getRow() + config.getGroupsList().size();
 		
 		for(int i = config.getGridValuesFirstCell().getRow(); i < lastRow; i++)
 		{
 			Cell poundCell = null;
 			Cell euroCell = null;
 			
			Row poundRow = null;
			Row euroRow = null;
			
 			for(int j = brokerFirstCell; j < brokerFirstCell + broker.getSuppliersList().size() ; j++)
 			{
				poundRow = sheet.getRow(i);
				euroRow = sheet.getRow(i + config.getConversionTableOffset());
 				
 				if(poundRow == null)
 					poundRow = sheet.createRow(i);
 				
 				if(euroRow == null)
 					euroRow = sheet.createRow(i + config.getConversionTableOffset());
 				
 				if(poundRow.getCell(j) == null)
 				{					
 					poundCell = poundRow.createCell(j);
 					euroCell = euroRow.createCell(j);
 					
 					poundCell.setCellStyle(CellStyles.setBackground(CellStyles.setThinBorders(CellStyles.getDefaultCellStyle(workbook)),IndexedColors.GREY_40_PERCENT.getIndex()));
 					euroCell.setCellStyle(CellStyles.setBackground(CellStyles.setThinBorders(CellStyles.getDefaultCellStyle(workbook)),IndexedColors.GREY_40_PERCENT.getIndex()));
 				}
 				
 				if(i == lastRow-1){
 					poundCell.setCellStyle(CellStyles.setMediumBottomBorder(poundCell.getCellStyle()));
 					euroCell.setCellStyle(CellStyles.setMediumBottomBorder(euroCell.getCellStyle()));
 				}
 				
 			}
 			
 			if(!broker.hasMinimum()){				
				poundCell = poundRow.getCell(brokerFirstCell + broker.getSuppliersList().size() - 1);
				poundCell.setCellStyle(CellStyles.setMediumRightBorder(poundCell.getCellStyle()));
				euroCell = euroRow.getCell(brokerFirstCell + broker.getSuppliersList().size() - 1);
				euroCell.setCellStyle(CellStyles.setMediumRightBorder(euroCell.getCellStyle()));
 			}
 		}
 		
 	}
 	
 	/**
 	 * Fills the suppliers line with the names
 	 * 
 	 * @param workbook The representation of the file
 	 * @param sheet The representation of the sheet
 	 * @param config The object that holds the information read from XML file
 	 * @param broker The broker that has the suppliers
 	 * @param brokerFirstCell The index of the designated broker
 	 */
 	private void fillSuppliersHeader(Workbook workbook, Sheet sheet, RateShopReportConfiguration config, Broker broker, int brokerFirstCell) {
 
 		Row poundRow = sheet.getRow(config.getGridValuesFirstCell().getRow()-1);
 		Row euroRow = sheet.getRow(config.getGridValuesFirstCell().getRow()-1 + config.getConversionTableOffset());
 		
 		if(poundRow == null)
 			poundRow = sheet.createRow(config.getGridValuesFirstCell().getRow()-1);
 		
 		if(euroRow == null)
 			euroRow = sheet.createRow(config.getGridValuesFirstCell().getRow()-1 + config.getConversionTableOffset());
 				
 		Cell poundSuppliers = null;
 		Cell euroSuppliers = null;
 
 		for (String  supplier : broker.getSuppliersList()) {
 			poundSuppliers = poundRow.createCell(brokerFirstCell + broker.getSuppliersMap().get(supplier).getColumn());
 			poundSuppliers.setCellStyle(CellStyles.setMediumBottomBorder(CellStyles.setThinBorders(CellStyles.getDefaultCellStyle(workbook))));
 			poundSuppliers.setCellValue(supplier);
 			poundSuppliers.setCellType(Cell.CELL_TYPE_STRING);
 			
 			euroSuppliers = euroRow.createCell(brokerFirstCell + broker.getSuppliersMap().get(supplier).getColumn());
 			euroSuppliers.setCellStyle(CellStyles.setMediumBottomBorder(CellStyles.setThinBorders(CellStyles.getDefaultCellStyle(workbook))));
 			euroSuppliers.setCellValue(supplier);
 			euroSuppliers.setCellType(Cell.CELL_TYPE_STRING);
 		}
 		
 		if(!broker.hasMinimum()){
 			poundSuppliers.setCellStyle(CellStyles.setMediumBottomBorder(CellStyles.setMediumRightBorder(CellStyles.setThinBorders(CellStyles.getDefaultCellStyle(workbook)))));
 			euroSuppliers.setCellStyle(CellStyles.setMediumBottomBorder(CellStyles.setMediumRightBorder(CellStyles.setThinBorders(CellStyles.getDefaultCellStyle(workbook)))));
 		}
 
 		setBrokerHeader(workbook, sheet, config, broker, brokerFirstCell);
 		
 	}
 	
 	/**
 	 * Fills the Broker name
 	 * 
 	 * @param workbook The representation of the file
 	 * @param sheet The representation of the sheet
 	 * @param config The object that holds the information read from XML file
 	 * @param broker The broker which table will be read
 	 * @param brokerFirstCell The initial column to start read
 	 */
 	private void setBrokerHeader(Workbook workbook, Sheet sheet, RateShopReportConfiguration config, Broker broker, int brokerFirstCell){
 		
 		Row poundRow = sheet.getRow(config.getGridValuesFirstCell().getRow()-2);
 		Row euroRow = sheet.getRow(config.getGridValuesFirstCell().getRow()-2 + config.getConversionTableOffset());
 		
 		if(poundRow == null)
 			poundRow = sheet.createRow(config.getGridValuesFirstCell().getRow()-2);
 		
 		if(euroRow == null)
 			euroRow = sheet.createRow(config.getGridValuesFirstCell().getRow()-2 + config.getConversionTableOffset());
 
 		
 		
 		int lastColumn = brokerFirstCell + broker.getSuppliersList().size() + (broker.hasMinimum()?0:-1);
 		
 		for(int i = brokerFirstCell; i <= lastColumn;i++){
 			Cell poundBrokerCell = poundRow.createCell(i);
 			poundBrokerCell.setCellValue(broker.getName());
 			poundBrokerCell.setCellStyle(CellStyles.setMediumBorders(CellStyles.getDefaultCellStyle(workbook)));
 			
 			Cell euroBrokerCell = euroRow.createCell(i);
 			euroBrokerCell.setCellValue(broker.getName());
 			euroBrokerCell.setCellStyle(CellStyles.setMediumBorders(CellStyles.getDefaultCellStyle(workbook)));
 		}
 		
 		sheet.addMergedRegion(new CellRangeAddress(
 	            poundRow.getRowNum(), //first row (0-based)
 	            poundRow.getRowNum(), //last row  (0-based)
 	            brokerFirstCell, //first column (0-based)
 	            lastColumn  //last column  (0-based)
 	    ));
 		
 		sheet.addMergedRegion(new CellRangeAddress(
 				euroRow.getRowNum(), //first row (0-based)
 				euroRow.getRowNum(), //last row  (0-based)
 	            brokerFirstCell, //first column (0-based)
 	            lastColumn  //last column  (0-based)
 	    ));
 	}
 
 	/**
 	 * Fills the groups' column with the names
 	 * 
 	 * @param workbook The representation of the file
 	 * @param sheet The representation of the sheet
 	 * @param config The object that holds the information read from XML file
 	 */
 	private void fillGroups(Workbook workbook, Sheet sheet, RateShopReportConfiguration config) {
 		int row = config.getGridValuesFirstCell().getRow();
 		int column = config.getGridValuesFirstCell().getColumn()-1;
 		
 		Cell poundGroup = null;
 		Cell euroGroup = null;
 		
 		for (String  group : config.getGroupsList()) {
 
 			Row poundRow = sheet.getRow(row);
 			Row euroRow = sheet.getRow(row + config.getConversionTableOffset());
 			
 			if(poundRow == null)
 				poundRow = sheet.createRow(row);
 			
 			if(euroRow == null)
 				euroRow = sheet.createRow(row + config.getConversionTableOffset());
 			
 			poundGroup = poundRow.createCell(column);
 			poundGroup.setCellStyle(CellStyles.setMediumRightBorder(CellStyles.setThinBorders(CellStyles.getDefaultCellStyle(workbook))));
 			poundGroup.setCellValue(group);
 			poundGroup.setCellType(Cell.CELL_TYPE_STRING);
 			
 			euroGroup = euroRow.createCell(column);
 			euroGroup.setCellStyle(CellStyles.setMediumRightBorder(CellStyles.setThinBorders(CellStyles.getDefaultCellStyle(workbook))));
 			euroGroup.setCellValue(group);
 			euroGroup.setCellType(Cell.CELL_TYPE_STRING);
 			
 			row++;
 		}
 		
 		poundGroup.setCellStyle(CellStyles.setMediumBottomBorder(CellStyles.setMediumRightBorder(CellStyles.setThinBorders(CellStyles.getDefaultCellStyle(workbook)))));
 		euroGroup.setCellStyle(CellStyles.setMediumBottomBorder(CellStyles.setMediumRightBorder(CellStyles.setThinBorders(CellStyles.getDefaultCellStyle(workbook)))));
 
 	}
 
 
 	/**
 	 * Set the fixed values in the report (Destination, Month, Days and Conversion Rate)
 	 * 
 	 * @param workbook The representation of the file
 	 * @param sheet The representation of the sheet
 	 * @param config The object that holds the information read from XML file
 	 * @param reportInfo The object that holds the report basic information
 	 * @throws CurrencyConversionException
 	 */
 	private void setFixedValues(Workbook workbook, Sheet sheet, RateShopReportConfiguration config, RateShopUKReportInfo reportInfo) throws CurrencyConversionException {
 		
 		setDestination(workbook, sheet, config, reportInfo);
 		setCurrency(workbook, sheet, config);
 		setMonthAndDate(workbook, sheet, config, reportInfo);
 		setRate(workbook, sheet, config);
 		
 	}
 
 	/**
 	 * Set the Destination in the Excel File
 	 * @param workbook The workbook for the Excel File
 	 * @param sheet The sheet that is used
 	 * @param config The RateShopReportConfiguration instance
 	 * @param reportInfo The instance of RateShopUKReportInfo with the basic information
 	 */
 	private void setDestination(Workbook workbook, Sheet sheet, RateShopReportConfiguration config, RateShopUKReportInfo reportInfo) {
 		Row destination = sheet.getRow(config.getDestinationCell().getRow());
 		
 		if(destination == null)
 			destination = sheet.createRow(config.getDestinationCell().getRow());
 		
 		
 		sheet.addMergedRegion(new CellRangeAddress(
 				destination.getRowNum(), //first row (0-based)
 				destination.getRowNum(), //last row  (0-based)
 	            config.getDestinationCell().getColumn(), //first column (0-based)
 	            config.getDestinationCell().getColumn()+2  //last column  (0-based)
 	    ));	
 		
 		Cell cell = destination.createCell(config.getDestinationCell().getColumn());
 		cell.setCellValue(reportInfo.getDestination());
 		cell.setCellStyle(CellStyles.setBoldAndColor(CellStyles.getDefaultCellStyle(workbook), HSSFColor.RED.index, CellStyles.LARGE_TEXT, workbook));		
 		
 	}
 
 	/**
 	 * Set the currency
 	 * 
 	 * @param workbook The workbook for the Excel File
 	 * @param sheet The sheet that is used
 	 * @param config The RateShopReportConfiguration instance
 	 */
 	private void setCurrency(Workbook workbook, Sheet sheet, RateShopReportConfiguration config) {
 		
 		final int numberOfRowForCurrencySymbol = 2;
 		
 		for (int i = 0; i < numberOfRowForCurrencySymbol; i++) {
 			Row currency = sheet.getRow((config.getDestinationCell().getRow() + 1) + i);
 			
 			if(currency == null)
 				currency = sheet.createRow((config.getDestinationCell().getRow() + 1) + i);
 			
 			Cell cell = currency.createCell(config.getGridValuesFirstCell().getColumn()-1);
 			cell.setCellValue("");
 			cell.setCellStyle(CellStyles.setBoldAndColor(CellStyles.setMediumBorders(CellStyles.getDefaultCellStyle(workbook)), HSSFColor.RED.index, CellStyles.MEDIUM_TEXT, workbook));
 		}
 		
 		sheet.addMergedRegion(new CellRangeAddress(
 				(config.getDestinationCell().getRow() + 1), //first row (0-based)
 				(config.getDestinationCell().getRow() + 1) + 1, //last row  (0-based)
 				config.getGridValuesFirstCell().getColumn()-1, //first column (0-based)
 				config.getGridValuesFirstCell().getColumn()-1  //last column  (0-based)
 	    ));
 		
 		for (int i = 0; i < numberOfRowForCurrencySymbol; i++) {
 			Row currency = sheet.getRow((config.getDestinationCell().getRow() + 1) + config.getConversionTableOffset() + i);
 			
 			if(currency == null)
 				currency = sheet.createRow((config.getDestinationCell().getRow() + 1) + config.getConversionTableOffset() + i);
 			
 			Cell cell = currency.createCell(config.getGridValuesFirstCell().getColumn()-1);
 			cell.setCellValue("");
 			cell.setCellStyle(CellStyles.setBoldAndColor(CellStyles.setMediumBorders(CellStyles.getDefaultCellStyle(workbook)), HSSFColor.RED.index, CellStyles.MEDIUM_TEXT, workbook));
 		}
 		
 		sheet.addMergedRegion(new CellRangeAddress(
 				(config.getDestinationCell().getRow() + 1) +  config.getConversionTableOffset(), //first row (0-based)
 				(config.getDestinationCell().getRow() + 1) + 1 + config.getConversionTableOffset(), //last row  (0-based)
 				config.getGridValuesFirstCell().getColumn()-1, //first column (0-based)
 				config.getGridValuesFirstCell().getColumn()-1  //last column  (0-based)
 	    ));
 		
 	}
 	
 	/**
 	 * Set month and days for the report
 	 * 
 	 * @param workbook The workbook for the Excel File
 	 * @param sheet The sheet that is used
 	 * @param config The RateShopReportConfiguration instance
 	 * @param reportInfo The instance of RateShopUKReportInfo with the basic information
 	 */
 	private void setMonthAndDate(Workbook workbook, Sheet sheet, RateShopReportConfiguration config, RateShopUKReportInfo reportInfo) {
 		Locale l = new Locale("pt", "PT");
 		
 		for (int i = config.getMonthCell().getRow(); i <= 1 + config.getMonthCell().getRow() + config.getGroupsList().size(); i++) {
 			Row poundRow = sheet.getRow(i);
 			Row euroRow = sheet.getRow(i + config.getConversionTableOffset());
 			
 			if(poundRow == null)
 				poundRow = sheet.createRow(i);
 			
 			if(euroRow == null)
 				euroRow = sheet.createRow(i + config.getConversionTableOffset());
 			
 			
 			CellStyle monthStyle = CellStyles.setRotation(CellStyles.setMediumBorders(CellStyles.setBoldAndColor(CellStyles.getDefaultCellStyle(workbook), HSSFColor.RED.index, CellStyles.MEDIUM_TEXT, workbook)));
 			CellStyle dayStyle = CellStyles.setRotation(CellStyles.setMediumBorders(CellStyles.setColor(CellStyles.getDefaultCellStyle(workbook), HSSFColor.RED.index, CellStyles.MEDIUM_TEXT, workbook)));
 			
 			Cell poundMonthCell = poundRow.createCell(config.getMonthCell().getColumn());
 			poundMonthCell.setCellValue(reportInfo.getStartDate().getDisplayName(Calendar.MONTH, Calendar.LONG, l));
 			poundMonthCell.setCellStyle(monthStyle);
 			
 			Cell euroMonthCell = euroRow.createCell(config.getMonthCell().getColumn());
 			euroMonthCell.setCellValue(reportInfo.getStartDate().getDisplayName(Calendar.MONTH, Calendar.LONG, l));
 			euroMonthCell.setCellStyle(monthStyle);
 			
 			Cell poundDayCell = poundRow.createCell(config.getDayCell().getColumn());
 			poundDayCell.setCellValue(reportInfo.getStartDate().get(Calendar.DATE) + " a " + reportInfo.getEndDate().get(Calendar.DATE));
 			poundDayCell.setCellStyle(dayStyle);
 			
 			Cell euroDayCell = euroRow.createCell(config.getDayCell().getColumn());
 			euroDayCell.setCellValue(reportInfo.getStartDate().get(Calendar.DATE) + " a " + reportInfo.getEndDate().get(Calendar.DATE));
 			euroDayCell.setCellStyle(dayStyle);
 		}
 		
 		
 		sheet.addMergedRegion(new CellRangeAddress(
 				config.getMonthCell().getRow(), //first row (0-based)
 				config.getMonthCell().getRow() + 1 + config.getGroupsList().size(), //last row  (0-based)
 				config.getMonthCell().getColumn(), //first column (0-based)
 				config.getMonthCell().getColumn()  //last column  (0-based)
 	    ));
 		
 		
 		sheet.addMergedRegion(new CellRangeAddress(
 				config.getDayCell().getRow(), //first row (0-based)
 				config.getDayCell().getRow() + 1 + config.getGroupsList().size(), //last row  (0-based)
 				config.getDayCell().getColumn(), //first column (0-based)
 				config.getDayCell().getColumn()  //last column  (0-based)
 	    ));
 		
 		sheet.addMergedRegion(new CellRangeAddress(
 				config.getMonthCell().getRow() + config.getConversionTableOffset(), //first row (0-based)
 				config.getMonthCell().getRow() + config.getConversionTableOffset() + 1 + config.getGroupsList().size(), //last row  (0-based)
 				config.getMonthCell().getColumn(), //first column (0-based)
 				config.getMonthCell().getColumn()  //last column  (0-based)
 	    ));
 		
 		
 		sheet.addMergedRegion(new CellRangeAddress(
 				config.getDayCell().getRow() + config.getConversionTableOffset(), //first row (0-based)
 				config.getDayCell().getRow() + config.getConversionTableOffset() + 1 + config.getGroupsList().size(), //last row  (0-based)
 				config.getDayCell().getColumn(), //first column (0-based)
 				config.getDayCell().getColumn()  //last column  (0-based)
 	    ));
 		
 		sheet.autoSizeColumn(config.getMonthCell().getColumn(), true);
 		sheet.autoSizeColumn(config.getDayCell().getColumn(), true);
 		
 	}
 
 	
 	/**
 	 * Set the current conversion rate
 	 * 
 	 * @param workbook The workbook for the Excel File
 	 * @param sheet The sheet that is used
 	 * @param config The RateShopReportConfiguration instance
 	 * @throws CurrencyConversionException
 	 */
 	private void setRate(Workbook workbook, Sheet sheet, RateShopReportConfiguration config) throws CurrencyConversionException {
 		
 		double exchangeRate = ExchangeRateService.getExchangeRate("EUR", "GBP", 4);
 		
 		if(exchangeRate == 0)
 			throw new IllegalArgumentException();
 		
 		Row rate = sheet.getRow(config.getRateCell().getRow());
 		
 		if(rate == null)
 			rate = sheet.createRow(config.getRateCell().getRow());
 		
 		
 		sheet.addMergedRegion(new CellRangeAddress(
 				rate.getRowNum(), //first row (0-based)
 				rate.getRowNum(), //last row  (0-based)
 				config.getRateCell().getColumn(), //first column (0-based)
 				config.getRateCell().getColumn() + 1  //last column  (0-based)
 	    ));
 		
 		Cell excRate = rate.createCell(config.getRateCell().getColumn());
 		excRate.setCellValue(exchangeRate);
 		excRate.setCellType(Cell.CELL_TYPE_NUMERIC);
 		excRate.setCellStyle(CellStyles.setBackground(CellStyles.setBold(CellStyles.getDefaultCellStyle(workbook), CellStyles.SMALL_TEXT, workbook), IndexedColors.LIME.index));	
 	}
 	
 	
 //	/**
 //	 * Set the fixed values in the report (Destination, Month, Days, Broker and Conversion Rate)
 //	 * 
 //	 * @param brokerName The broker
 //	 * @param destination The destination
 //	 * @param puDate The initial date
 //	 * @param doDate The end date
 //	 * @throws IllegalArgumentException When the conversion rate isn't valid
 //	 * @throws IOException When can't access to the service that provides the conversion rate
 //	 * @throws ConversionException When the ExchangeRateService gives a error 
 //	 */
 //	private void setFixedValues(Workbook workbook, Sheet sheet, RateShopReportConfiguration config, RateShopUKReportInfo reportInfo) throws CurrencyConversionException {
 //		
 //		setDestination(workbook, sheet, config, reportInfo);
 //		setCurrency(workbook, sheet, config);
 //		setMonthAndDate(workbook, sheet, config, reportInfo);
 //		setRate(workbook, sheet, config);
 //		
 //	}
 //
 //	private void setRate(Workbook workbook, Sheet sheet, RateShopReportConfiguration config) throws CurrencyConversionException {
 //		
 //		double exchangeRate = ExchangeRateService.getExchangeRate("EUR", "GBP", 4);
 //		
 //		if(exchangeRate == 0)
 //			throw new IllegalArgumentException();
 //		
 //		Row rate = sheet.getRow(config.getRateCell().getRow());
 //		
 //		if(rate == null)
 //			rate = sheet.createRow(config.getRateCell().getRow());
 //		
 //		CellStyle style = CellStyles.setBackground(CellStyles.setBold(CellStyles.getDefaultCellStyle(workbook), CellStyles.SMALL_TEXT, workbook), IndexedColors.LIME.index);
 //		
 //		Cell excRate = rate.createCell(config.getRateCell().getColumn());
 //		excRate.setCellValue(exchangeRate);
 //		excRate.setCellType(Cell.CELL_TYPE_NUMERIC);
 //		excRate.setCellStyle(style);
 //		
 //		excRate = rate.createCell(config.getRateCell().getColumn() + 1);
 //		excRate.setCellValue(exchangeRate);
 //		excRate.setCellType(Cell.CELL_TYPE_NUMERIC);
 //		excRate.setCellStyle(style);
 //
 //		sheet.addMergedRegion(new CellRangeAddress(
 //				rate.getRowNum(), //first row (0-based)
 //				rate.getRowNum(), //last row  (0-based)
 //				config.getRateCell().getColumn(), //first column (0-based)
 //				config.getRateCell().getColumn() + 1  //last column  (0-based)
 //	    ));
 //		
 //	}
 //
 //	private void setMonthAndDate(Workbook workbook, Sheet sheet, RateShopReportConfiguration config, RateShopUKReportInfo reportInfo) {
 //		Locale l = new Locale("pt", "PT");
 //		
 //		for (int i = config.getMonthCell().getRow(); i <= 1 + config.getMonthCell().getRow() + config.getGroupsList().size(); i++) {
 //			Row poundRow = sheet.getRow(i);
 //			Row euroRow = sheet.getRow(i + config.getConversionTableOffset());
 //			
 //			if(poundRow == null)
 //				poundRow = sheet.createRow(i);
 //			
 //			if(euroRow == null)
 //				euroRow = sheet.createRow(i + config.getConversionTableOffset());
 //			
 //			
 //			CellStyle monthStyle = CellStyles.setRotation(CellStyles.setBoldAndColor(CellStyles.setMediumBorders(CellStyles.getDefaultCellStyle(workbook)), HSSFColor.RED.index, CellStyles.MEDIUM_TEXT, workbook));
 //			CellStyle dayStyle = CellStyles.setRotation(CellStyles.setColor(CellStyles.setMediumBorders(CellStyles.getDefaultCellStyle(workbook)), HSSFColor.RED.index, CellStyles.MEDIUM_TEXT, workbook));
 //			
 //			Cell poundMonthCell = poundRow.createCell(config.getMonthCell().getColumn());
 //			poundMonthCell.setCellValue(reportInfo.getStartDate().getDisplayName(Calendar.MONTH, Calendar.LONG, l));
 //			poundMonthCell.setCellStyle(monthStyle);
 //			
 //			Cell euroMonthCell = euroRow.createCell(config.getMonthCell().getColumn());
 //			euroMonthCell.setCellValue(reportInfo.getStartDate().getDisplayName(Calendar.MONTH, Calendar.LONG, l));
 //			euroMonthCell.setCellStyle(monthStyle);
 //			
 //			Cell poundDayCell = poundRow.createCell(config.getDayCell().getColumn());
 //			poundDayCell.setCellValue(reportInfo.getStartDate().get(Calendar.DATE) + " a " + reportInfo.getEndDate().get(Calendar.DATE));
 //			poundDayCell.setCellStyle(dayStyle);
 //			
 //			Cell euroDayCell = euroRow.createCell(config.getDayCell().getColumn());
 //			euroDayCell.setCellValue(reportInfo.getStartDate().get(Calendar.DATE) + " a " + reportInfo.getEndDate().get(Calendar.DATE));
 //			euroDayCell.setCellStyle(dayStyle);
 //		}
 //		
 //		
 //		sheet.addMergedRegion(new CellRangeAddress(
 //				config.getMonthCell().getRow(), //first row (0-based)
 //				config.getMonthCell().getRow() + 1 + config.getGroupsList().size(), //last row  (0-based)
 //				config.getMonthCell().getColumn(), //first column (0-based)
 //				config.getMonthCell().getColumn()  //last column  (0-based)
 //	    ));
 //		
 //		
 //		sheet.addMergedRegion(new CellRangeAddress(
 //				config.getDayCell().getRow(), //first row (0-based)
 //				config.getDayCell().getRow() + 1 + config.getGroupsList().size(), //last row  (0-based)
 //				config.getDayCell().getColumn(), //first column (0-based)
 //				config.getDayCell().getColumn()  //last column  (0-based)
 //	    ));
 //		
 //		sheet.addMergedRegion(new CellRangeAddress(
 //				config.getMonthCell().getRow() + config.getConversionTableOffset(), //first row (0-based)
 //				config.getMonthCell().getRow() + config.getConversionTableOffset() + 1 + config.getGroupsList().size(), //last row  (0-based)
 //				config.getMonthCell().getColumn(), //first column (0-based)
 //				config.getMonthCell().getColumn()  //last column  (0-based)
 //	    ));
 //		
 //		
 //		sheet.addMergedRegion(new CellRangeAddress(
 //				config.getDayCell().getRow() + config.getConversionTableOffset(), //first row (0-based)
 //				config.getDayCell().getRow() + config.getConversionTableOffset() + 1 + config.getGroupsList().size(), //last row  (0-based)
 //				config.getDayCell().getColumn(), //first column (0-based)
 //				config.getDayCell().getColumn()  //last column  (0-based)
 //	    ));
 //		
 //		sheet.autoSizeColumn(config.getMonthCell().getColumn(), true);
 //		sheet.autoSizeColumn(config.getDayCell().getColumn(), true);
 //		
 //	}
 //
 //	private void setCurrency(Workbook workbook, Sheet sheet, RateShopReportConfiguration config) {
 //		final int numberOfRowForCurrencySymbol = 2;
 //		
 //		for (int i = 0; i < numberOfRowForCurrencySymbol; i++) {
 //			Row currency = sheet.getRow((config.getDestinationCell().getRow() + 1) + i);
 //			
 //			if(currency == null)
 //				currency = sheet.createRow((config.getDestinationCell().getRow() + 1) + i);
 //			
 //			Cell cell = currency.createCell(config.getGridValuesFirstCell().getColumn()-1);
 //			cell.setCellValue("");
 //			cell.setCellStyle(CellStyles.setBoldAndColor(CellStyles.setMediumBorders(CellStyles.getDefaultCellStyle(workbook)), HSSFColor.RED.index, CellStyles.MEDIUM_TEXT, workbook));
 //		}
 //		
 //		sheet.addMergedRegion(new CellRangeAddress(
 //				(config.getDestinationCell().getRow() + 1), //first row (0-based)
 //				(config.getDestinationCell().getRow() + 1) + 1, //last row  (0-based)
 //				config.getGridValuesFirstCell().getColumn()-1, //first column (0-based)
 //				config.getGridValuesFirstCell().getColumn()-1  //last column  (0-based)
 //	    ));
 //		
 //		for (int i = 0; i < numberOfRowForCurrencySymbol; i++) {
 //			Row currency = sheet.getRow((config.getDestinationCell().getRow() + 1) + config.getConversionTableOffset() + i);
 //			
 //			if(currency == null)
 //				currency = sheet.createRow((config.getDestinationCell().getRow() + 1) + config.getConversionTableOffset() + i);
 //			
 //			Cell cell = currency.createCell(config.getGridValuesFirstCell().getColumn()-1);
 //			cell.setCellValue("");
 //			cell.setCellStyle(CellStyles.setBoldAndColor(CellStyles.setMediumBorders(CellStyles.getDefaultCellStyle(workbook)), HSSFColor.RED.index, CellStyles.MEDIUM_TEXT, workbook));
 //		}
 //		
 //		sheet.addMergedRegion(new CellRangeAddress(
 //				(config.getDestinationCell().getRow() + 1) +  config.getConversionTableOffset(), //first row (0-based)
 //				(config.getDestinationCell().getRow() + 1) + 1 + config.getConversionTableOffset(), //last row  (0-based)
 //				config.getGridValuesFirstCell().getColumn()-1, //first column (0-based)
 //				config.getGridValuesFirstCell().getColumn()-1  //last column  (0-based)
 //	    ));
 //		
 //	}
 //
 //	private void setDestination(Workbook workbook, Sheet sheet, RateShopReportConfiguration config, RateShopUKReportInfo reportInfo) {
 //		Row destination = sheet.getRow(config.getDestinationCell().getRow());
 //		
 //		if(destination == null)
 //			destination = sheet.createRow(config.getDestinationCell().getRow());
 //		
 //		for (int i = config.getDestinationCell().getColumn(); i <= config.getDestinationCell().getColumn()+2; i++) {
 //			Cell cell = destination.createCell(i);
 //			cell.setCellValue(reportInfo.getDestination());
 //			cell.setCellStyle(CellStyles.setBoldAndColor(CellStyles.getDefaultCellStyle(workbook), HSSFColor.RED.index, CellStyles.LARGE_TEXT, workbook));	
 //			
 //		}
 //		
 //		sheet.addMergedRegion(new CellRangeAddress(
 //				destination.getRowNum(), //first row (0-based)
 //				destination.getRowNum(), //last row  (0-based)
 //	            config.getDestinationCell().getColumn(), //first column (0-based)
 //	            config.getDestinationCell().getColumn()+2  //last column  (0-based)
 //	    ));		
 //	}
 //
 	/**
 	 * Saves a XLSX Report with a specific name
 	 * 
 	 * @param destination The destination
 	 * @param puDate The initial date
 	 * @param doDate The end date
 	 * @throws IOException When can't create the file
 	 */
 	private void saveFile(Workbook workbook, RateShopUKReportInfo reportInfo, Calendar reportDate, Country country) throws IOException{
 		FileOutputStream out;
 		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy HH_mm");
 		
 		// TODO: Specify output file location 
 		// Do we net to put the country in the name?
 		out = new FileOutputStream(String.format("R %s RATE_SHOP_UK_%s_%s_A_%s.xlsx", dateFormat.format(reportDate.getTime()), reportInfo.getDestination(), getDate(reportInfo.getStartDate()), getDate(reportInfo.getEndDate())));
 	
 		XSSFFormulaEvaluator.evaluateAllFormulaCells((XSSFWorkbook)workbook);
 		workbook.write(out);
 		out.close();
 	}
 
 	
 	
 	/**
 	 * Gets a String with the dates for the file name.
 	 * 
 	 * @param date 
 	 * @return a String with the following style 07_Set_89
 	 */
 	private String getDate(Calendar date) {
 		Locale l = new Locale("pt", "PT");
 		
 		int day = date.get(Calendar.DATE);
 		String month = date.getDisplayName(Calendar.MONTH, Calendar.SHORT, l);
 		int year = date.get(Calendar.YEAR);
 				
 		return String.format("%d_%s_%d", day, month, year);
 	}
 }
