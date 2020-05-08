 package org.ocha.hdx.service;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.ocha.hdx.exporter.Exporter;
 import org.ocha.hdx.exporter.country.ExporterCountry5Years_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryCapacity_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryCrisisHistory_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryOther_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryOverview_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryQueryData;
 import org.ocha.hdx.exporter.country.ExporterCountrySocioEconomic_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryVulnerability_XLSX;
 import org.ocha.hdx.exporter.helper.ReportRow;
 import org.ocha.hdx.persistence.dao.metadata.AdditionalDataDao;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorType;
 import org.ocha.hdx.persistence.entity.metadata.AdditionalData;
 import org.ocha.hdx.persistence.entity.metadata.AdditionalData.EntryKey;
 import org.springframework.beans.factory.annotation.Autowired;
 
 public class ExporterServiceImpl implements ExporterService {
 
 	@Autowired
 	private CuratedDataService curatedDataService;
 
 	@Autowired
 	private AdditionalDataDao additionalDataDao;
 
 	/*
 	 * Delegates to CuratedDataService
 	 */
 
 	@Override
 	public IndicatorType getIndicatorTypeByCode(final String code) {
 		return curatedDataService.getIndicatorTypeByCode(code);
 	}
 
 	@Override
 	public List<Object[]> getCountryOverviewData(final ExporterCountryQueryData queryData) {
 		return curatedDataService.listIndicatorsForCountryOverview(queryData.getCountryCode(), queryData.getLanguage());
 	}
 
 	@Override
 	public Map<String, ReportRow> getCountryCrisisHistoryData(final ExporterCountryQueryData queryData) {
 		final Map<Integer, List<Object[]>> listIndicatorsForCountryCrisisHistory = curatedDataService.listIndicatorsForCountryCrisisHistory(queryData.getCountryCode(),
 				Integer.valueOf(queryData.getFromYear()), Integer.valueOf(queryData.getToYear()), queryData.getLanguage());
 
 		return convertToReports(listIndicatorsForCountryCrisisHistory);
 
 	}
 
 	@Override
 	public Map<String, ReportRow> getCountrySocioEconomicData(final ExporterCountryQueryData queryData) {
 		final Map<Integer, List<Object[]>> listIndicatorsForCountrySocioEconomic = curatedDataService.listIndicatorsForCountrySocioEconomic(queryData.getCountryCode(),
 				Integer.valueOf(queryData.getFromYear()), Integer.valueOf(queryData.getToYear()), queryData.getLanguage());
 
 		return convertToReports(listIndicatorsForCountrySocioEconomic);
 
 	}
 
 	@Override
 	public Map<String, ReportRow> getCountryVulnerabilityData(final ExporterCountryQueryData queryData) {
 
 		final Map<Integer, List<Object[]>> listIndicatorsForCountryCrisisHistory = curatedDataService.listIndicatorsForCountryVulnerability(queryData.getCountryCode(),
 				Integer.valueOf(queryData.getFromYear()), Integer.valueOf(queryData.getToYear()), queryData.getLanguage());
 
 		return convertToReports(listIndicatorsForCountryCrisisHistory);
 
 	}
 
 	@Override
 	public Map<String, ReportRow> getCountry5YearsData(final ExporterCountryQueryData queryData) {
 
 		final Map<Integer, List<Object[]>> listIndicatorsForCountryCrisisHistory = curatedDataService.list5YearsIndicatorsForCountry(queryData.getCountryCode(),
 				Integer.valueOf(queryData.getFromYear()), Integer.valueOf(queryData.getToYear()), queryData.getLanguage());
 
 		return convertToReports(listIndicatorsForCountryCrisisHistory);
 	}
 
 	@Override
 	public Map<String, ReportRow> getCountryCapacityData(final ExporterCountryQueryData queryData) {
 
 		final Map<Integer, List<Object[]>> listIndicatorsForCountryCapacity = curatedDataService.listIndicatorsForCountryCrisisHistory(queryData.getCountryCode(),
 				Integer.valueOf(queryData.getFromYear()), Integer.valueOf(queryData.getToYear()), queryData.getLanguage());
 
 		return convertToReports(listIndicatorsForCountryCapacity);
 
 	}
 
 	@Override
 	public Map<String, ReportRow> getCountryOtherData(final ExporterCountryQueryData queryData) {
 
 		final Map<Integer, List<Object[]>> listIndicatorsForCountryOther = curatedDataService.listIndicatorsForCountryOther(queryData.getCountryCode(), Integer.valueOf(queryData.getFromYear()),
 				Integer.valueOf(queryData.getToYear()), queryData.getLanguage());
 
 		return convertToReports(listIndicatorsForCountryOther);
 
 	}
 
 	private Map<String, ReportRow> convertToReports(final Map<Integer, List<Object[]>> listOfIndicators) {
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
 
 						final AdditionalData datasetSummary = additionalDataDao.getAdditionalDataByIndicatorTypeCodeAndSourceCodeAndEntryKey(indicatorTypeCode, sourceCode, EntryKey.DATASET_SUMMARY);
 						final String datasetSummaryAsString = datasetSummary != null ? datasetSummary.getEntryValue().getDefaultValue() : "";
 						row.addMetadata(EntryKey.DATASET_SUMMARY, datasetSummaryAsString);
 
 						final AdditionalData methodology = additionalDataDao.getAdditionalDataByIndicatorTypeCodeAndSourceCodeAndEntryKey(indicatorTypeCode, sourceCode, EntryKey.METHODOLOGY);
 						final String methodologyAsString = methodology != null ? methodology.getEntryValue().getDefaultValue() : "";
 						row.addMetadata(EntryKey.METHODOLOGY, methodologyAsString);
 
 						final AdditionalData moreInfo = additionalDataDao.getAdditionalDataByIndicatorTypeCodeAndSourceCodeAndEntryKey(indicatorTypeCode, sourceCode, EntryKey.MORE_INFO);
 						final String moreInfoAsString = moreInfo != null ? moreInfo.getEntryValue().getDefaultValue() : "";
 						row.addMetadata(EntryKey.MORE_INFO, moreInfoAsString);
 
 						final AdditionalData termsOfUse = additionalDataDao.getAdditionalDataByIndicatorTypeCodeAndSourceCodeAndEntryKey(indicatorTypeCode, sourceCode, EntryKey.TERMS_OF_USE);
 						final String termsOfUseAsString = termsOfUse != null ? termsOfUse.getEntryValue().getDefaultValue() : "";
						row.addMetadata(EntryKey.MORE_INFO, termsOfUseAsString);
 
 						row.addValue(key, record[3].toString());
 						reportRows.put(indicatorTypeCode, row);
 					}
 				}
 			}
 		}
 
 		return reportRows;
 	}
 
 	@Override
 	public List<Object[]> listIndicatorsForCountryOverview(final String countryCode, final String languageCode) {
 		return curatedDataService.listIndicatorsForCountryOverview(countryCode, languageCode);
 	}
 
 	/*
 	 * Exports
 	 */
 
 	/**
 	 * Export a country report as XLSX
 	 */
 	@Override
 	public XSSFWorkbook exportCountry_XLSX(final String countryCode, final Integer fromYear, final Integer toYear, final String language) {
 		// Set the query data
 		final ExporterCountryQueryData exporterCountryQueryData = new ExporterCountryQueryData();
 		exporterCountryQueryData.setCountryCode(countryCode);
 		exporterCountryQueryData.setFromYear(fromYear);
 		exporterCountryQueryData.setToYear(toYear);
 		exporterCountryQueryData.setLanguage(language);
 
 		// Define the exporter
 		// Country report contains :
 		// 1. Country overview
 		// 2. Country crisis history
 		// 3. ... TODO
 		final Exporter<XSSFWorkbook, ExporterCountryQueryData> countryExporter = new ExporterCountryOverview_XLSX(new ExporterCountryCrisisHistory_XLSX(new ExporterCountrySocioEconomic_XLSX(
 				new ExporterCountryVulnerability_XLSX(new ExporterCountryCapacity_XLSX(new ExporterCountryOther_XLSX(new ExporterCountry5Years_XLSX(this)))))));
 
 		// final Exporter<XSSFWorkbook, ExporterCountryQueryData> countryExporter = new ExporterCountryOverview_XLSX(this);
 
 		// Export the data in a new workbook
 		final XSSFWorkbook workbook = new XSSFWorkbook();
 		countryExporter.export(workbook, exporterCountryQueryData);
 
 		// Return the workbook
 		return workbook;
 	}
 }
