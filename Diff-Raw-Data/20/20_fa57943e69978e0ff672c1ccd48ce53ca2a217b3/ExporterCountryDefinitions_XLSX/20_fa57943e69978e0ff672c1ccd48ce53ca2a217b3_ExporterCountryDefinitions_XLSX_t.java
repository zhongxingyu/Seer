 package org.ocha.hdx.exporter.country;
 
 import java.util.Comparator;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.poi.ss.util.WorkbookUtil;
 import org.apache.poi.xssf.usermodel.XSSFRow;
 import org.apache.poi.xssf.usermodel.XSSFSheet;
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.ocha.hdx.exporter.Exporter;
 import org.ocha.hdx.exporter.Exporter_XLSX;
 import org.ocha.hdx.exporter.QueryData.CHANNEL_KEYS;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorType;
 import org.ocha.hdx.service.ExporterService;
 
 /**
  * Exporter for a country - definitions.
  * 
  * @author bmichiels
  * 
  */
 public class ExporterCountryDefinitions_XLSX extends Exporter_XLSX<ExporterCountryQueryData> {
 
 	public ExporterCountryDefinitions_XLSX(final ExporterService exporterService) {
 		super(exporterService);
 	}
 
 	public ExporterCountryDefinitions_XLSX(final Exporter<XSSFWorkbook, ExporterCountryQueryData> exporter) {
 		super(exporter);
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public XSSFWorkbook export(final XSSFWorkbook workbook, final ExporterCountryQueryData queryData) throws Exception {
 
 		final TreeSet<String[]> indicatorTypes = new TreeSet<String[]>(new Comparator<String[]>() {
 			@Override
 			public int compare(final String[] o1, final String[] o2) {
 				if (o1[0].equals(o2[0])) {
 					return o1[1].compareTo(o2[1]);
 				} else {
 					return o1[0].compareTo(o2[0]);
 				}
 			}
 		});
 
 		indicatorTypes.addAll((Set<String[]>) queryData.getChannelValue(CHANNEL_KEYS.INDICATOR_TYPES));
 
 		/* TODO i18n */
 
 		// Creating the sheet
 		final String safeName = WorkbookUtil.createSafeSheetName("Indicators definitions");
 		final XSSFSheet sheet = workbook.createSheet(safeName);
 
		workbook.setSheetOrder(safeName, 1);

 		// Assign the headers to the title row
 		final String[] headers = { "Indicator type", "Definition", "Source dataset" };
 		createColumnHeaderCells(sheet, headers);
 
 		// Fill with the data
 		int rowIndex = 0;
 		for (final String[] indicatorTypeCode : indicatorTypes) {
 
 			// Create the row (not overwriting the headers :-) )
 			final XSSFRow row = sheet.createRow(++rowIndex);
 
 			// Indicator type
 			createLinkCell(row, 0, indicatorTypeCode[0], "'" + indicatorTypeCode[1] + "'!A1");
 
 			// Definition
 			final IndicatorType indicatorType = exporterService.getIndicatorTypeByCode(indicatorTypeCode[0]);
 			createCell(row, 1, indicatorType.getName().getDefaultValue());
 		}
 
 		// Auto size the columns, except columns A and C, which have a fixed width
 		for (int i = 0; i < headers.length; i++) {
 			sheet.autoSizeColumn(i);
 		}
 
 		return super.export(workbook, queryData);
 	}
 }
