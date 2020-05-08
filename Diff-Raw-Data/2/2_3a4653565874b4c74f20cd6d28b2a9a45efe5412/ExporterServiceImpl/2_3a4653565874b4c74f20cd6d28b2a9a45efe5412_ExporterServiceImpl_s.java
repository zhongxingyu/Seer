 package org.ocha.hdx.service;
 
 import java.io.File;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.NoResultException;
 
 import org.apache.poi.xssf.usermodel.XSSFWorkbook;
 import org.ocha.hdx.exporter.Exporter;
 import org.ocha.hdx.exporter.country.ExporterCountryData_CSV;
 import org.ocha.hdx.exporter.country.ExporterCountryData_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryDefinitions_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryFTSData_CSV;
 import org.ocha.hdx.exporter.country.ExporterCountryFTSData_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryFTSOverview_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryFTSReadme_TXT;
 import org.ocha.hdx.exporter.country.ExporterCountryOverview_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryQueryData;
 import org.ocha.hdx.exporter.country.ExporterCountryRWData_CSV;
 import org.ocha.hdx.exporter.country.ExporterCountryRWData_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryRWOverview_XLSX;
 import org.ocha.hdx.exporter.country.ExporterCountryRWReadme_TXT;
 import org.ocha.hdx.exporter.country.ExporterCountryReadme_TXT;
 import org.ocha.hdx.exporter.country.ExporterCountryReadme_XLSX;
 import org.ocha.hdx.exporter.helper.ReadmeHelper;
 import org.ocha.hdx.exporter.helper.ReportRow;
 import org.ocha.hdx.exporter.indicator.ExporterIndicatorData_CSV;
 import org.ocha.hdx.exporter.indicator.ExporterIndicatorData_XLSX;
 import org.ocha.hdx.exporter.indicator.ExporterIndicatorFTSReadme_TXT;
 import org.ocha.hdx.exporter.indicator.ExporterIndicatorFTS_XLSX;
 import org.ocha.hdx.exporter.indicator.ExporterIndicatorMetadataQueryData;
 import org.ocha.hdx.exporter.indicator.ExporterIndicatorMetadata_CSV;
 import org.ocha.hdx.exporter.indicator.ExporterIndicatorQueryData;
 import org.ocha.hdx.exporter.indicator.ExporterIndicatorRWReadme_TXT;
 import org.ocha.hdx.exporter.indicator.ExporterIndicatorRW_XLSX;
 import org.ocha.hdx.exporter.indicator.ExporterIndicatorReadme_TXT;
 import org.ocha.hdx.exporter.indicator.ExporterIndicatorReadme_XLSX;
 import org.ocha.hdx.exporter.indicator.ExporterIndicatorTypeFTSOverview_XLSX;
 import org.ocha.hdx.exporter.indicator.ExporterIndicatorTypeOverview_XLSX;
 import org.ocha.hdx.exporter.indicator.ExporterIndicatorTypeRWOverview_XLSX;
 import org.ocha.hdx.model.DataSerie;
 import org.ocha.hdx.persistence.dao.metadata.DataSerieMetadataDAO;
 import org.ocha.hdx.persistence.dao.view.IndicatorDataDAO;
 import org.ocha.hdx.persistence.dao.view.IndicatorTypeOverviewDAO;
 import org.ocha.hdx.persistence.entity.curateddata.Indicator.Periodicity;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorType;
 import org.ocha.hdx.persistence.entity.curateddata.Source;
 import org.ocha.hdx.persistence.entity.metadata.DataSerieMetadata;
 import org.ocha.hdx.persistence.entity.metadata.DataSerieMetadata.MetadataName;
 import org.ocha.hdx.persistence.entity.view.IndicatorData;
 import org.ocha.hdx.persistence.entity.view.IndicatorTypeOverview;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 public class ExporterServiceImpl implements ExporterService {
 
 	private static Logger logger = LoggerFactory.getLogger(ExporterServiceImpl.class);
 
 	@Autowired
 	private CuratedDataService curatedDataService;
 
 	@Autowired
 	private DataSerieMetadataDAO dataSerieMetadataDAO;
 
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
 	public Source getSourceByCode(final String code) {
 		try {
 			return curatedDataService.getSourceByCode(code);
 		} catch (final NoResultException e) {
 			logger.debug(String.format("Could not find Source from SourceCode : %s", code));
 			return null;
 		}
 	}
 
 	@Override
 	public List<DataSerieMetadata> getMetadataForDataSerie(final DataSerie dataSerie) {
 		return curatedDataService.getMetadataForDataSerie(dataSerie);
 	}
 
 	@Override
 	public Map<String, Timestamp> getMinMaxDatesForDataSeries(final DataSerie dataSeries) {
 		return curatedDataService.getMinMaxDatesForDataSeries(dataSeries);
 	}
 
 	@Override
 	public List<Object[]> listIndicatorsForCountryOverview(final String countryCode, final String languageCode, final String[] indicatorsList) {
 		return curatedDataService.listIndicatorsForCountryOverview(countryCode, languageCode, indicatorsList);
 	}
 
 	/* ******* */
 	/* Exports */
 	/* ******* */
 
 	// SW
 
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
 		final Exporter<XSSFWorkbook, ExporterCountryQueryData> exporter = new ExporterCountryReadme_XLSX(new ExporterCountryOverview_XLSX(new ExporterCountryData_XLSX(new ExporterCountryData_XLSX(
 				new ExporterCountryData_XLSX(new ExporterCountryData_XLSX(new ExporterCountryData_XLSX(new ExporterCountryData_XLSX(new ExporterCountryDefinitions_XLSX(this), Periodicity.FIVE_YEARS,
 						DataSerie.COUNTRY_5_YEARS_dataSeries, "5 Years indicators"), DataSerie.COUNTRY_OTHER_dataSeries, "Other"), DataSerie.COUNTRY_CAPACITY_dataSeries, "Capacity"),
 						DataSerie.COUNTRY_VULNERABILITY_dataSeries, "Vulnerability"), DataSerie.COUNTRY_SOCIO_ECONOMIC_dataSeries, "Socio-economic"), DataSerie.COUNTRY_CRISIS_HISTORY_dataSeries,
 				"Crisis history")));
 
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
 
 		// Return the file
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
 
 	/**
 	 * Export an indicator report as CSV.
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public File exportIndicator_CSV(final String indicatorTypeCode, final String sourceCode, final Long fromYear, final Long toYear, final String language) throws Exception {
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
 		// 1. Indicator data
 		final Exporter<File, ExporterIndicatorQueryData> exporter = new ExporterIndicatorData_CSV(this);
 
 		// Export the data in a new file
 		final File file = File.createTempFile("Indicator_" + new Date().getTime() + "_", ".csv");
 		exporter.export(file, queryData);
 
 		// Return the file
 		return file;
 	}
 
 	/**
 	 * Export an indicator type readme as TXT.
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public File exportIndicatorReadMe_TXT(final String indicatorTypeCode, final String sourceCode, final String language) throws Exception {
 		// Set the query data
 		final ExporterIndicatorQueryData queryData = new ExporterIndicatorQueryData();
 		queryData.setIndicatorTypeCode(indicatorTypeCode);
 		queryData.setSourceCode(sourceCode);
 		queryData.setLanguage(language);
 		queryData.setReadmeHelper(readmeHelper);
 
 		// Define the exporter
 		final Exporter<File, ExporterIndicatorQueryData> exporter = new ExporterIndicatorReadme_TXT(this);
 
 		// Export the data in a new file
 		final File file = File.createTempFile("IndicatorReadme_" + new Date().getTime() + "_", ".txt");
 		exporter.export(file, queryData);
 
 		// Return the workbook
 		return file;
 	}
 
 	@Override
 	public File exportIndicatorAllMetadata_CSV(final String language) throws Exception {
 		// Set the query data
 		final ExporterIndicatorMetadataQueryData queryData = new ExporterIndicatorMetadataQueryData();
 		queryData.setIndicatorTypeCode(null);
 		queryData.setLanguage(language);
 
 		// Define the exporter
 		// Indicator metadata contains :
 		// 1. Metadata (all data)
 
 		final Exporter<File, ExporterIndicatorMetadataQueryData> exporter = new ExporterIndicatorMetadata_CSV(this);
 
 		// Export the data in a new file
 		final File file = File.createTempFile("IndicatorAllMetadata_" + new Date().getTime() + "_", ".csv");
 		exporter.export(file, queryData);
 
 		// Return the workbook
 		return file;
 	}
 
 	@Override
 	public File exportIndicatorMetadata_CSV(final String indicatorTypeCode, final String language) throws Exception {
 		// Set the query data
 		final ExporterIndicatorMetadataQueryData queryData = new ExporterIndicatorMetadataQueryData();
 		queryData.setIndicatorTypeCode(indicatorTypeCode);
 		queryData.setLanguage(language);
 
 		// Define the exporter
 		// Indicator metadata contains :
 		// 1. Metadata (all data)
 
 		final Exporter<File, ExporterIndicatorMetadataQueryData> exporter = new ExporterIndicatorMetadata_CSV(this);
 
 		// Export the data in a new file
 		final File file = File.createTempFile("IndicatorMetadata_" + new Date().getTime() + "_", ".csv");
 		exporter.export(file, queryData);
 
 		// Return the workbook
 		return file;
 	}
 
 	// RW 
 
 	/**
 	 * Export a country RW report as XLSX.
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public XSSFWorkbook exportCountryRW_XLSX(final String countryCode, final Integer fromYear, final Integer toYear, final String language) throws Exception {
 		// Set the query data NOTE We use the same query data as for the SW country exporter, as the parameters are the same ; see later if we have to change this
 		final ExporterCountryQueryData queryData = new ExporterCountryQueryData();
 		queryData.setCountryCode(countryCode);
 		queryData.setFromYear(fromYear);
 		queryData.setToYear(toYear);
 		queryData.setLanguage(language);
 		queryData.setReadmeHelper(readmeHelper);
 
 		// Define the exporter
 		final Exporter<XSSFWorkbook, ExporterCountryQueryData> exporter = new ExporterCountryReadme_XLSX(new ExporterCountryRWOverview_XLSX(new ExporterCountryRWData_XLSX(
 				new ExporterCountryDefinitions_XLSX(this))));
 
 		// final Exporter<XSSFWorkbook, ExporterIndicatorQueryData> countryExporter = new ExporterIndicatorTypeOverview_XLSX(this);
 
 		// Export the data in a new workbook
 		final XSSFWorkbook workbook = new XSSFWorkbook();
 		exporter.export(workbook, queryData);
 
 		// Return the workbook
 		return workbook;
 	}
 
 	/**
 	 * Export a RW country report as CSV.
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public File exportCountryRW_CSV(final String countryCode, final Integer fromYear, final Integer toYear, final String language) throws Exception {
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
 
 		final Exporter<File, ExporterCountryQueryData> exporter = new ExporterCountryRWData_CSV(this);
 
 		// Export the data in a new file
 		final File file = File.createTempFile("Country_" + new Date().getTime() + "_", ".csv");
 		exporter.export(file, queryData);
 
 		// Return the workbook
 		return file;
 	}
 
 	/**
 	 * Export a country RW readme as TXT.
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public File exportCountryRWReadMe_TXT(final String countryCode, final String language) throws Exception {
 		// Set the query data
 		final ExporterCountryQueryData queryData = new ExporterCountryQueryData();
 		queryData.setCountryCode(countryCode);
 		queryData.setLanguage(language);
 		queryData.setReadmeHelper(readmeHelper);
 
 		// Define the exporter
 		final Exporter<File, ExporterCountryQueryData> exporter = new ExporterCountryRWReadme_TXT(this);
 
 		// Export the data in a new file
 		final File file = File.createTempFile("CountryRWReadme_" + new Date().getTime() + "_", ".txt");
 		exporter.export(file, queryData);
 
 		// Return the workbook
 		return file;
 	}
 
 	@Override
 	public XSSFWorkbook exportIndicatorRW_XLSX(final Long fromYear, final Long toYear, final String language) throws Exception {
 		// Set the query data
 		final ExporterIndicatorQueryData queryData = new ExporterIndicatorQueryData();
 		queryData.setFromYear(fromYear);
 		queryData.setToYear(toYear);
 		queryData.setLanguage(language);
 		queryData.setReadmeHelper(readmeHelper);
 
 		// Define the exporter
 		// Indicator report contains :
 		// 1. Read me
 		// 2. Overview
 		// 3. Indicator data for RW001
 		// 3. Indicator data for RW002
 		final Exporter<XSSFWorkbook, ExporterIndicatorQueryData> exporter = new ExporterIndicatorReadme_XLSX(new ExporterIndicatorTypeRWOverview_XLSX(new ExporterIndicatorRW_XLSX(
				new ExporterIndicatorRW_XLSX(this, "RW001", "Number of Disasters"), "RW002", "Number of Reports")));
 
 		// Export the data in a new workbook
 		final XSSFWorkbook workbook = new XSSFWorkbook();
 		exporter.export(workbook, queryData);
 
 		// Return the workbook
 		return workbook;
 	}
 
 	/**
 	 * Export an indicator type RW readme as TXT.
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public File exportIndicatorRWReadMe_TXT(final String language) throws Exception {
 		// Set the query data
 		final ExporterIndicatorQueryData queryData = new ExporterIndicatorQueryData();
 		queryData.setLanguage(language);
 		queryData.setReadmeHelper(readmeHelper);
 
 		// Define the exporter
 		final Exporter<File, ExporterIndicatorQueryData> exporter = new ExporterIndicatorRWReadme_TXT(this);
 
 		// Export the data in a new file
 		final File file = File.createTempFile("IndicatorRWReadme_" + new Date().getTime() + "_", ".txt");
 		exporter.export(file, queryData);
 
 		// Return the workbook
 		return file;
 	}
 
 	// FTS 
 
 	/**
 	 * Export a country FTS report as XLSX.
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public XSSFWorkbook exportCountryFTS_XLSX(final String countryCode, final Integer fromYear, final Integer toYear, final String language) throws Exception {
 		// Set the query data NOTE We use the same query data as for the SW country exporter, as the parameters are the same ; see later if we have to change this
 		final ExporterCountryQueryData queryData = new ExporterCountryQueryData();
 		queryData.setCountryCode(countryCode);
 		queryData.setFromYear(fromYear);
 		queryData.setToYear(toYear);
 		queryData.setLanguage(language);
 		queryData.setReadmeHelper(readmeHelper);
 
 		// Define the exporter
 		final Exporter<XSSFWorkbook, ExporterCountryQueryData> exporter = new ExporterCountryReadme_XLSX(new ExporterCountryFTSOverview_XLSX(new ExporterCountryFTSData_XLSX(
 				new ExporterCountryDefinitions_XLSX(this))));
 
 		// final Exporter<XSSFWorkbook, ExporterIndicatorQueryData> countryExporter = new ExporterIndicatorTypeOverview_XLSX(this);
 
 		// Export the data in a new workbook
 		final XSSFWorkbook workbook = new XSSFWorkbook();
 		exporter.export(workbook, queryData);
 
 		// Return the workbook
 		return workbook;
 	}
 
 	/**
 	 * Export a FTS country report as CSV.
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public File exportCountryFTS_CSV(final String countryCode, final Integer fromYear, final Integer toYear, final String language) throws Exception {
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
 
 		final Exporter<File, ExporterCountryQueryData> exporter = new ExporterCountryFTSData_CSV(this);
 
 		// Export the data in a new file
 		final File file = File.createTempFile("Country_" + new Date().getTime() + "_", ".csv");
 		exporter.export(file, queryData);
 
 		// Return the workbook
 		return file;
 	}
 
 	/**
 	 * Export a country FTS readme as TXT.
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public File exportCountryFTSReadMe_TXT(final String countryCode, final String language) throws Exception {
 		// Set the query data
 		final ExporterCountryQueryData queryData = new ExporterCountryQueryData();
 		queryData.setCountryCode(countryCode);
 		queryData.setLanguage(language);
 		queryData.setReadmeHelper(readmeHelper);
 
 		// Define the exporter
 		final Exporter<File, ExporterCountryQueryData> exporter = new ExporterCountryFTSReadme_TXT(this);
 
 		// Export the data in a new file
 		final File file = File.createTempFile("CountryFTSReadme_" + new Date().getTime() + "_", ".txt");
 		exporter.export(file, queryData);
 
 		// Return the workbook
 		return file;
 	}
 
 	@Override
 	public XSSFWorkbook exportIndicatorFTS_XLSX(final Long fromYear, final Long toYear, final String language) throws Exception {
 		// Set the query data
 		final ExporterIndicatorQueryData queryData = new ExporterIndicatorQueryData();
 		queryData.setFromYear(fromYear);
 		queryData.setToYear(toYear);
 		queryData.setLanguage(language);
 		queryData.setReadmeHelper(readmeHelper);
 
 		// Define the exporter
 		// Indicator report contains :
 		// 1. Read me
 		// 2. Overview
 		// 3. Indicator data for every FTS indicator
 
 		final List<DataSerie> countryFtsDataseries = DataSerie.COUNTRY_FTS_dataSeries;
 		final Exporter<XSSFWorkbook, ExporterIndicatorQueryData> exporter = new ExporterIndicatorReadme_XLSX(new ExporterIndicatorTypeFTSOverview_XLSX(buildSheets(countryFtsDataseries)));
 
 		// Export the data in a new workbook
 		final XSSFWorkbook workbook = new XSSFWorkbook();
 		exporter.export(workbook, queryData);
 
 		// Return the workbook
 		return workbook;
 	}
 	
 	/**
 	 * Build the exporters for each FTS Indicator.
 	 */
 	private ExporterIndicatorFTS_XLSX buildSheets(final List<DataSerie> dss) {
 		ExporterIndicatorFTS_XLSX previous = null;
 		for (final DataSerie ds : dss) {
 			ExporterIndicatorFTS_XLSX aSheet = null;
 			if (previous == null) {
 				aSheet = new ExporterIndicatorFTS_XLSX(this, ds.getIndicatorCode(), ds.getIndicatorCode());
 			} else {
 				aSheet = new ExporterIndicatorFTS_XLSX(previous, ds.getIndicatorCode(), ds.getIndicatorCode());
 			}
 			previous = aSheet;
 		}
 		return previous;
 	}
 
 	/**
 	 * Export an indicator type FTS readme as TXT.
 	 * 
 	 * @throws Exception
 	 */
 	@Override
 	public File exportIndicatorFTSReadMe_TXT(final String language) throws Exception {
 		// Set the query data
 		final ExporterIndicatorQueryData queryData = new ExporterIndicatorQueryData();
 		queryData.setLanguage(language);
 		queryData.setReadmeHelper(readmeHelper);
 
 		// Define the exporter
 		final Exporter<File, ExporterIndicatorQueryData> exporter = new ExporterIndicatorFTSReadme_TXT(this);
 
 		// Export the data in a new file
 		final File file = File.createTempFile("IndicatorFTSReadme_" + new Date().getTime() + "_", ".txt");
 		exporter.export(file, queryData);
 
 		// Return the workbook
 		return file;
 	}
 
 	/* ********************************* */
 	/* Data getters for Country reports. */
 	/* ********************************* */
 
 	/**
 	 * Country - overview.
 	 */
 	@Override
 	public List<Object[]> getCountryOverviewData(final ExporterCountryQueryData queryData, final String[] indicatorsList) {
 		return curatedDataService.listIndicatorsForCountryOverview(queryData.getCountryCode(), queryData.getLanguage(), indicatorsList);
 	}
 
 	/**
 	 * Country - data.
 	 */
 	@Override
 	public Map<String, ReportRow> getCountryData(final ExporterCountryQueryData queryData, final List<DataSerie> dataSeries) {
 		final Map<Integer, List<Object[]>> listIndicatorsForCountry = curatedDataService.listIndicatorsForCountry(queryData.getCountryCode(), Integer.valueOf(queryData.getFromYear()),
 				Integer.valueOf(queryData.getToYear()), queryData.getLanguage(), dataSeries);
 
 		return convertCountryIndicatorsToReports(listIndicatorsForCountry);
 
 	}
 
 	@Override
 	public Map<String, ReportRow> getCountryData(final ExporterCountryQueryData queryData, final Periodicity periodicity, final List<DataSerie> dataSeries) {
 		final Map<Integer, List<Object[]>> listIndicatorsForCountry = curatedDataService.listIndicatorsForCountry(queryData.getCountryCode(), Integer.valueOf(queryData.getFromYear()),
 				Integer.valueOf(queryData.getToYear()), queryData.getLanguage(), periodicity, dataSeries);
 
 		return convertCountryIndicatorsToReports(listIndicatorsForCountry);
 
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
 						final String sourceCode = record[6].toString();
 						final ReportRow row = new ReportRow(indicatorTypeCode, record[1].toString(), sourceCode, record[2].toString());
 
 						final List<DataSerieMetadata> results = getMetadataForDataSerie(new DataSerie(indicatorTypeCode, sourceCode));
 
 						DataSerieMetadata methodologyMetadata = null;
 						DataSerieMetadata moreInfoMetadata = null;
 						DataSerieMetadata datasetSummaryMetadata = null;
 						DataSerieMetadata termOfUseMetadata = null;
 						for (final DataSerieMetadata dataSerieMetadata : results) {
 							switch (dataSerieMetadata.getEntryKey()) {
 							case METHODOLOGY:
 								methodologyMetadata = dataSerieMetadata;
 								break;
 							case MORE_INFO:
 								moreInfoMetadata = dataSerieMetadata;
 								break;
 							case DATASET_SUMMARY:
 								datasetSummaryMetadata = dataSerieMetadata;
 								break;
 							case TERMS_OF_USE:
 								termOfUseMetadata = dataSerieMetadata;
 								break;
 
 							default:
 								break;
 							}
 							final String methodologyMetadataAsString = methodologyMetadata != null ? methodologyMetadata.getEntryValue().getDefaultValue() : "";
 							row.addMetadata(MetadataName.METHODOLOGY, methodologyMetadataAsString);
 
 							final String moreInfoMetadataAsString = moreInfoMetadata != null ? moreInfoMetadata.getEntryValue().getDefaultValue() : "";
 							row.addMetadata(MetadataName.MORE_INFO, moreInfoMetadataAsString);
 
 							final String datasetSummaryMetadataAsString = datasetSummaryMetadata != null ? datasetSummaryMetadata.getEntryValue().getDefaultValue() : "";
 							row.addMetadata(MetadataName.DATASET_SUMMARY, datasetSummaryMetadataAsString);
 
 							final String termOfUseMetadataAsString = termOfUseMetadata != null ? termOfUseMetadata.getEntryValue().getDefaultValue() : "";
 							row.addMetadata(MetadataName.TERMS_OF_USE, termOfUseMetadataAsString);
 
 						}
 
 						row.addValue(key, record[3].toString());
 						reportRows.put(indicatorTypeCode, row);
 					}
 				}
 			}
 		}
 		return reportRows;
 	}
 
 	/* *********************************** */
 	/* Data getters for Indicator reports. */
 	/* *********************************** */
 
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
 
 	@Override
 	public List<DataSerieMetadata> getIndicatorMetadataData(final ExporterIndicatorMetadataQueryData queryData) {
 		return dataSerieMetadataDAO.listDataSerieMetadataByIndicatorTypeCode(queryData.getIndicatorTypeCode());
 	}
 
 	/* ********* */
 	/* Utilities */
 	/* ********* */
 
 }
