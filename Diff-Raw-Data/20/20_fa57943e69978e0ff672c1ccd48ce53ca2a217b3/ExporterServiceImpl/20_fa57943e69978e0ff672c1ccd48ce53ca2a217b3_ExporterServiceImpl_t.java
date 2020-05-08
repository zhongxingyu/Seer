 package org.ocha.hdx.service;
 
 import java.io.File;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.ocha.hdx.exporter.Exporter;
 import org.ocha.hdx.exporter.country.ExporterCountry5Years_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryCapacity_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryCrisisHistory_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryData_CSV;
 import org.ocha.hdx.exporter.country.ExporterCountryDefinitions_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryOther_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryOverview_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryQueryData;
 import org.ocha.hdx.exporter.country.ExporterCountryReadme_TXT;
 import org.ocha.hdx.exporter.country.ExporterCountryReadme_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountrySocioEconomic_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryVulnerability_XLSX;
 import org.ocha.hdx.exporter.helper.ReadmeHelper;
 import org.ocha.hdx.exporter.helper.ReportRow;
 import org.ocha.hdx.exporter.indicator.ExporterIndicatorData_XLSX;
 import org.ocha.hdx.exporter.indicator.ExporterIndicatorQueryData;
 import org.ocha.hdx.exporter.indicator.ExporterIndicatorReadme_XLSX;
 import org.ocha.hdx.exporter.indicator.ExporterIndicatorTypeOverview_XLSX;
 import org.ocha.hdx.persistence.dao.metadata.AdditionalDataDAO;
 import org.ocha.hdx.persistence.dao.view.IndicatorDataDAO;
 import org.ocha.hdx.persistence.dao.view.IndicatorTypeOverviewDAO;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorType;
 import org.ocha.hdx.persistence.entity.metadata.AdditionalData;
 import org.ocha.hdx.persistence.entity.metadata.AdditionalData.EntryKey;
 import org.ocha.hdx.persistence.entity.view.IndicatorData;
 import org.ocha.hdx.persistence.entity.view.IndicatorTypeOverview;
 import org.springframework.beans.factory.annotation.Autowired;
 
 public class ExporterServiceImpl implements ExporterService {
 
 	@Autowired
 	private CuratedDataService curatedDataService;
 
 	@Autowired
 	private AdditionalDataDAO additionalDataDAO;
 
 	@Autowired
 	private IndicatorTypeOverviewDAO indicatorTypeOverviewDAO;
 
 	@Autowired
 	private IndicatorDataDAO indicatorDataDAO;
 
 	@Autowired
 	private ReadmeHelper readmeHelper;
 
 	/* ******************************* */
 	/* Delegates to CuratedDataService */
 	/* ******************************* */
 
 	@Override
 	public IndicatorType getIndicatorTypeByCode(final String code) {
 		return curatedDataService.getIndicatorTypeByCode(code);
 	}
 
 	@Override
 	public List<Object[]> listIndicatorsForCountryOverview(final String countryCode, final String languageCode) {
 		return curatedDataService.listIndicatorsForCountryOverview(countryCode, languageCode);
 	}
 
 	/* ******* */
 	/* Exports */
 	/* ******* */
 
 	/**
 	 * Export a country report as XLSX.
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public XSSFWorkbook exportCountry_XLSX(final String countryCode, final Integer fromYear, final Integer toYear, final String language) throws Exception {
 		// Set the query data
 		final ExporterCountryQueryData queryData = new ExporterCountryQueryData();
 		queryData.setCountryCode(countryCode);
 		queryData.setFromYear(fromYear);
 		queryData.setToYear(toYear);
 		queryData.setLanguage(language);
 		queryData.setReadmeHelper(readmeHelper);
 
 		// Define the exporter
 		// Country report contains :
 		// 1. Country overview
 		// 2. Country crisis history
 		// 3. Country socio-economic
 		// 4. Country vulnerability
 		// 5. Country capacity
 		// 6. Country - other
 		// 7. Country - 5-years data
 		// 8. Read me
 
		final Exporter<XSSFWorkbook, ExporterCountryQueryData> exporter = new ExporterCountryReadme_XLSX(new ExporterCountryOverview_XLSX(new ExporterCountryCrisisHistory_XLSX(
				new ExporterCountrySocioEconomic_XLSX(new ExporterCountryVulnerability_XLSX(new ExporterCountryCapacity_XLSX(new ExporterCountryOther_XLSX(new ExporterCountry5Years_XLSX(
						new ExporterCountryDefinitions_XLSX(this)))))))));
 
 		// final Exporter<XSSFWorkbook, ExporterIndicatorQueryData> countryExporter = new ExporterIndicatorTypeOverview_XLSX(this);
 
 		// Export the data in a new workbook
 		final XSSFWorkbook workbook = new XSSFWorkbook();
 		exporter.export(workbook, queryData);
 
 		// Return the workbook
 		return workbook;
 	}
 
 	/**
 	 * Export a country report as CSV.
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public File exportCountry_CSV(final String countryCode, final Integer fromYear, final Integer toYear, final String language) throws Exception {
 		// Set the query data
 		final ExporterCountryQueryData queryData = new ExporterCountryQueryData();
 		queryData.setCountryCode(countryCode);
 		queryData.setFromYear(fromYear);
 		queryData.setToYear(toYear);
 		queryData.setLanguage(language);
 		queryData.setReadmeHelper(readmeHelper);
 
 		// Define the exporter
 		// Country report contains :
 		// 1. Country (all data)
 
 		final Exporter<File, ExporterCountryQueryData> exporter = new ExporterCountryData_CSV(this);
 
 		// Export the data in a new file
 		final File file = File.createTempFile("Country_" + new Date().getTime() + "_", ".csv");
 		exporter.export(file, queryData);
 
 		// Return the workbook
 		return file;
 	}
 
 	/**
 	 * Export a country readme as TXT.
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public File exportCountryReadMe_TXT(final String countryCode, final String language) throws Exception {
 		// Set the query data
 		final ExporterCountryQueryData queryData = new ExporterCountryQueryData();
 		queryData.setCountryCode(countryCode);
 		queryData.setLanguage(language);
 		queryData.setReadmeHelper(readmeHelper);
 
 		// Define the exporter
 		final Exporter<File, ExporterCountryQueryData> exporter = new ExporterCountryReadme_TXT(this);
 
 		// Export the data in a new file
 		final File file = File.createTempFile("CountryReadme_" + new Date().getTime() + "_", ".txt");
 		exporter.export(file, queryData);
 
 		// Return the workbook
 		return file;
 	}
 
 	/**
 	 * Export an indicator report as XLSX.
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public XSSFWorkbook exportIndicator_XLSX(final String indicatorTypeCode, final String sourceCode, final Long fromYear, final Long toYear, final String language) throws Exception {
 		// Set the query data
 		final ExporterIndicatorQueryData queryData = new ExporterIndicatorQueryData();
 		queryData.setIndicatorTypeCode(indicatorTypeCode);
 		queryData.setSourceCode(sourceCode);
 		queryData.setFromYear(fromYear);
 		queryData.setToYear(toYear);
 		queryData.setLanguage(language);
 		queryData.setReadmeHelper(readmeHelper);
 
 		// Define the exporter
 		// Indicator report contains :
 		// 1. Indicator overview
 		// 2. Indicator data
 		// 3. Read me
 		final Exporter<XSSFWorkbook, ExporterIndicatorQueryData> exporter = new ExporterIndicatorReadme_XLSX(new ExporterIndicatorTypeOverview_XLSX(new ExporterIndicatorData_XLSX((this))));
 
 		// Export the data in a new workbook
 		final XSSFWorkbook workbook = new XSSFWorkbook();
 		exporter.export(workbook, queryData);
 
 		// Return the workbook
 		return workbook;
 	}
 
 	/* **************** */
 	/* Country reports. */
 	/* **************** */
 
 	/**
 	 * Country - overview.
 	 */
 	@Override
 	public List<Object[]> getCountryOverviewData(final ExporterCountryQueryData queryData) {
 		return curatedDataService.listIndicatorsForCountryOverview(queryData.getCountryCode(), queryData.getLanguage());
 	}
 
 	/**
 	 * Country - crisis history.
 	 */
 	@Override
 	public Map<String, ReportRow> getCountryCrisisHistoryData(final ExporterCountryQueryData queryData) {
 		final Map<Integer, List<Object[]>> listIndicatorsForCountryCrisisHistory = curatedDataService.listIndicatorsForCountryCrisisHistory(queryData.getCountryCode(),
 				Integer.valueOf(queryData.getFromYear()), Integer.valueOf(queryData.getToYear()), queryData.getLanguage());
 
 		return convertCountryIndicatorsToReports(listIndicatorsForCountryCrisisHistory);
 
 	}
 
 	/**
 	 * Country - socio-economic.
 	 */
 	@Override
 	public Map<String, ReportRow> getCountrySocioEconomicData(final ExporterCountryQueryData queryData) {
 		final Map<Integer, List<Object[]>> listIndicatorsForCountrySocioEconomic = curatedDataService.listIndicatorsForCountrySocioEconomic(queryData.getCountryCode(),
 				Integer.valueOf(queryData.getFromYear()), Integer.valueOf(queryData.getToYear()), queryData.getLanguage());
 
 		return convertCountryIndicatorsToReports(listIndicatorsForCountrySocioEconomic);
 
 	}
 
 	/**
 	 * Country - vulnerability.
 	 */
 	@Override
 	public Map<String, ReportRow> getCountryVulnerabilityData(final ExporterCountryQueryData queryData) {
 
 		final Map<Integer, List<Object[]>> listIndicatorsForCountryCrisisHistory = curatedDataService.listIndicatorsForCountryVulnerability(queryData.getCountryCode(),
 				Integer.valueOf(queryData.getFromYear()), Integer.valueOf(queryData.getToYear()), queryData.getLanguage());
 
 		return convertCountryIndicatorsToReports(listIndicatorsForCountryCrisisHistory);
 
 	}
 
 	/**
 	 * Country - 5-years data.
 	 */
 	@Override
 	public Map<String, ReportRow> getCountry5YearsData(final ExporterCountryQueryData queryData) {
 
 		final Map<Integer, List<Object[]>> listIndicatorsForCountryCrisisHistory = curatedDataService.list5YearsIndicatorsForCountry(queryData.getCountryCode(),
 				Integer.valueOf(queryData.getFromYear()), Integer.valueOf(queryData.getToYear()), queryData.getLanguage());
 
 		return convertCountryIndicatorsToReports(listIndicatorsForCountryCrisisHistory);
 	}
 
 	/**
 	 * Country - capacity.
 	 */
 	@Override
 	public Map<String, ReportRow> getCountryCapacityData(final ExporterCountryQueryData queryData) {
 
 		final Map<Integer, List<Object[]>> listIndicatorsForCountryCapacity = curatedDataService.listIndicatorsForCountryCapacity(queryData.getCountryCode(), Integer.valueOf(queryData.getFromYear()),
 				Integer.valueOf(queryData.getToYear()), queryData.getLanguage());
 
 		return convertCountryIndicatorsToReports(listIndicatorsForCountryCapacity);
 
 	}
 
 	/**
 	 * Country - other.
 	 */
 	@Override
 	public Map<String, ReportRow> getCountryOtherData(final ExporterCountryQueryData queryData) {
 
 		final Map<Integer, List<Object[]>> listIndicatorsForCountryOther = curatedDataService.listIndicatorsForCountryOther(queryData.getCountryCode(), Integer.valueOf(queryData.getFromYear()),
 				Integer.valueOf(queryData.getToYear()), queryData.getLanguage());
 
 		return convertCountryIndicatorsToReports(listIndicatorsForCountryOther);
 
 	}
 
 	/**
 	 * Convert a list of country indicators to report rows.
 	 * 
 	 * @param listOfIndicators
 	 * @return
 	 */
 	private Map<String, ReportRow> convertCountryIndicatorsToReports(final Map<Integer, List<Object[]>> listOfIndicators) {
 		final Map<String, ReportRow> reportRows = new HashMap<String, ReportRow>();
 
 		for (final Integer key : listOfIndicators.keySet()) {
 			for (final Object[] record : listOfIndicators.get(key)) {
 				final String indicatorTypeCode = record[0].toString();
 				// records with only 1 value are just placeholders, but don't contain actual data
 				if (record.length > 1) {
 					if (reportRows.containsKey(indicatorTypeCode)) {
 						reportRows.get(indicatorTypeCode).addValue(key, record[3].toString());
 						// add a value
 					} else {
 						final String sourceCode = record[5].toString();
 						final ReportRow row = new ReportRow(indicatorTypeCode, record[1].toString(), sourceCode, record[2].toString());
 
 						final AdditionalData datasetSummary = additionalDataDAO.getAdditionalDataByIndicatorTypeCodeAndSourceCodeAndEntryKey(indicatorTypeCode, sourceCode, EntryKey.DATASET_SUMMARY);
 						final String datasetSummaryAsString = datasetSummary != null ? datasetSummary.getEntryValue().getDefaultValue() : "";
 						row.addMetadata(EntryKey.DATASET_SUMMARY, datasetSummaryAsString);
 
 						final AdditionalData methodology = additionalDataDAO.getAdditionalDataByIndicatorTypeCodeAndSourceCodeAndEntryKey(indicatorTypeCode, sourceCode, EntryKey.METHODOLOGY);
 						final String methodologyAsString = methodology != null ? methodology.getEntryValue().getDefaultValue() : "";
 						row.addMetadata(EntryKey.METHODOLOGY, methodologyAsString);
 
 						final AdditionalData moreInfo = additionalDataDAO.getAdditionalDataByIndicatorTypeCodeAndSourceCodeAndEntryKey(indicatorTypeCode, sourceCode, EntryKey.MORE_INFO);
 						final String moreInfoAsString = moreInfo != null ? moreInfo.getEntryValue().getDefaultValue() : "";
 						row.addMetadata(EntryKey.MORE_INFO, moreInfoAsString);
 
 						final AdditionalData termsOfUse = additionalDataDAO.getAdditionalDataByIndicatorTypeCodeAndSourceCodeAndEntryKey(indicatorTypeCode, sourceCode, EntryKey.TERMS_OF_USE);
 						final String termsOfUseAsString = termsOfUse != null ? termsOfUse.getEntryValue().getDefaultValue() : "";
 						row.addMetadata(EntryKey.TERMS_OF_USE, termsOfUseAsString);
 
 						row.addValue(key, record[3].toString());
 						reportRows.put(indicatorTypeCode, row);
 					}
 				}
 			}
 		}
 		return reportRows;
 	}
 
 	/* ****************** */
 	/* Indicator reports. */
 	/* ****************** */
 
 	/**
 	 * Indicator - overview.
 	 */
 	@Override
 	public IndicatorTypeOverview getIndicatorTypeOverviewData(final ExporterIndicatorQueryData queryData) {
 		// return curatedDataService.getIndicatorTypeOverview(queryData.getIndicatorTypeCode(), queryData.getSourceCode(), queryData.getLanguage());
 		final IndicatorTypeOverview indicatorTypeOverview = indicatorTypeOverviewDAO.getIndicatorTypeOverview(queryData.getIndicatorTypeCode(), queryData.getSourceCode());
 		return indicatorTypeOverview;
 	}
 
 	/**
 	 * Indicator - data.
 	 */
 	@Override
 	public Map<Long, Map<String, IndicatorData>> getIndicatorDataData(final ExporterIndicatorQueryData queryData) {
 		if (queryData.getToYear().equals(new Long(0l))) {
 			queryData.setToYear(Long.MAX_VALUE);
 		}
 		final Map<Long, Map<String, IndicatorData>> result = indicatorDataDAO.getIndicatorData(queryData.getIndicatorTypeCode(), queryData.getSourceCode(), queryData.getFromYear(),
 				queryData.getToYear());
 		return result;
 		/*
 		 * final Map<Integer, List<Object[]>> listIndicatorsForCountryCrisisHistory = curatedDataService.listIndicatorsForCountryCrisisHistory(queryData.getIndicatorTypeCode(),
 		 * Integer.valueOf(queryData.getFromYear()), Integer.valueOf(queryData.getToYear()), queryData.getLanguage());
 		 * 
 		 * return convertCountryIndicatorsToReports(listIndicatorsForCountryCrisisHistory);
 		 */
 	}
 
 	/* ********* */
 	/* Utilities */
 	/* ********* */
 
 }
