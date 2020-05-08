 package org.ocha.hdx.service;
 
 import java.io.File;
 import java.util.Date;
 import java.util.List;
 import java.util.Map.Entry;
 
 import javax.annotation.Resource;
 
 import org.ocha.hdx.config.DummyConfigurationCreator;
 import org.ocha.hdx.importer.HDXWithCountryListImporter;
 import org.ocha.hdx.importer.PreparedData;
 import org.ocha.hdx.importer.PreparedIndicator;
 import org.ocha.hdx.importer.ScraperImporter;
 import org.ocha.hdx.importer.ScraperValidatingImporter;
 import org.ocha.hdx.model.validation.ValidationReport;
 import org.ocha.hdx.model.validation.ValidationStatus;
 import org.ocha.hdx.persistence.dao.ImportFromCKANDAO;
 import org.ocha.hdx.persistence.dao.currateddata.EntityDAO;
 import org.ocha.hdx.persistence.dao.currateddata.EntityTypeDAO;
 import org.ocha.hdx.persistence.dao.dictionary.SourceDictionaryDAO;
 import org.ocha.hdx.persistence.entity.ImportFromCKAN;
 import org.ocha.hdx.persistence.entity.ckan.CKANDataset;
 import org.ocha.hdx.persistence.entity.ckan.CKANDataset.Type;
 import org.ocha.hdx.persistence.entity.configs.ResourceConfiguration;
 import org.ocha.hdx.persistence.entity.curateddata.Indicator;
 import org.ocha.hdx.validation.DummyValidator;
 import org.ocha.hdx.validation.ScraperValidator;
 import org.ocha.hdx.validation.itemvalidator.IValidatorCreator;
 import org.ocha.hdx.validation.prevalidator.IPreValidatorCreator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 public class FileEvaluatorAndExtractorImpl implements FileEvaluatorAndExtractor {
 
 	private static Logger logger = LoggerFactory.getLogger(FileEvaluatorAndExtractorImpl.class);
 
 	@Autowired
 	private ImportFromCKANDAO importFromCKANDAO;
 
 	@Autowired
 	private EntityDAO entityDAO;
 
 	@Autowired
 	private EntityTypeDAO entityTypeDAO;
 
 	@Autowired
 	private SourceDictionaryDAO sourceDictionaryDAO;
 
 	@Autowired
 	private CuratedDataService curatedDataService;
 
 	@Autowired
 	private IndicatorCreationService indicatorCreationService;
 
 	@Resource
 	private List<IValidatorCreator> validatorCreators;
 
 	@Resource
 	private List<IPreValidatorCreator> preValidatorCreators;
 
 	@Autowired
 	private DummyConfigurationCreator dummyConfigurationCreator;
 
 	@Override
 	public ValidationReport evaluateResource(final File file, final Type type) {
 		// FIXME we probably want something else here, map of HDXValidator, or
 		// Factory....
 		switch (type) {
 		case DUMMY:
 			return new DummyValidator().evaluateFile(file);
 		case SCRAPER_VALIDATING:
 
 		case SCRAPER:
 			return new ScraperValidator().evaluateFile(file);
 
 		default:
 			return this.defaultValidationFail(file);
 		}
 	}
 
 	@Override
 	public boolean transformAndImportDataFromResource(final File file, final Type type, final String resourceId, final String revisionId,
 			final ResourceConfiguration config, final ValidationReport report) {
 
 		HDXWithCountryListImporter importer	= null;
 		// FIXME we probably want something else here, map of HDXImporter, or
 		// Factory....
 		final PreparedData preparedData;
 		switch (type) {
 		case DUMMY:
 			preparedData = this.defaultImportFail(file);
 			break;
 		case SCRAPER:
 			importer = new ScraperImporter(this.sourceDictionaryDAO.getSourceDictionariesByImporter("scraper"), this.indicatorCreationService);
 			preparedData = this.prepareDataForImport(file, importer);
 			break;
 		case SCRAPER_VALIDATING:
			importer = new ScraperValidatingImporter(this.sourceDictionaryDAO.getSourceDictionariesByImporter("scraper-validator"),
 					config, this.validatorCreators, this.preValidatorCreators, report,  this.indicatorCreationService);
 			preparedData = this.prepareDataForImport(file, importer);
 			break;
 		default:
 			preparedData = this.defaultImportFail(file);
 		}
 		if (preparedData.isSuccess()) {
 			final List<Indicator> indicators =  importer.transformToFinalFormat();
 			this.saveReadIndicatorsToDatabase(indicators, resourceId, revisionId);
 		}
 
 		return preparedData.isSuccess();
 
 	}
 
 	/**
 	 * see {@link FileEvaluatorAndExtractor#incorporatePreparedDataForImport(PreparedData, String, String)}
 	 */
 	@Override
 	@Deprecated
 	public void incorporatePreparedDataForImport(final PreparedData preparedData, final String resourceId, final String revisionId) {
 		final ImportFromCKAN importFromCKAN = this.importFromCKANDAO.createNewImportRecord(resourceId, revisionId, new Date());
 		for (final PreparedIndicator preparedIndicator : preparedData.getIndicatorsToImport()) {
 			try {
 				this.curatedDataService.createIndicator(preparedIndicator, importFromCKAN);
 			} catch (final Exception e) {
 				logger.debug(String.format("Error trying to create preparedIndicator : %s", preparedIndicator.toString()));
 			}
 		}
 	}
 
 	@Override
 	public void saveReadIndicatorsToDatabase(final List<Indicator> indicators, final String resourceId, final String revisionId) {
 		final ImportFromCKAN importFromCKAN = this.importFromCKANDAO.createNewImportRecord(resourceId, revisionId, new Date());
 		for (final Indicator indicator : indicators ) {
 			try {
 				this.curatedDataService.createIndicator(indicator, importFromCKAN);
 			} catch (final Exception e) {
 				logger.debug(String.format("Error trying to save Indicator : %s", indicator.toString()));
 			}
 		}
 	}
 
 	private ValidationReport defaultValidationFail(final File file) {
 		final ValidationReport report = new ValidationReport(CKANDataset.Type.SCRAPER);
 
 		report.addEntry(ValidationStatus.ERROR, "Mocked evaluator, always failing");
 		return report;
 	}
 
 	private PreparedData defaultImportFail(final File file) {
 		final PreparedData preparedData = new PreparedData(false, null);
 		return preparedData;
 	}
 
 	private PreparedData prepareDataForImport(final File file, final HDXWithCountryListImporter importer) {
 		final PreparedData preparedData;
 		for (final Entry<String, String> entry : importer.getCountryList(file).entrySet()) {
 			try {
 				this.curatedDataService.createEntity(entry.getKey(), entry.getValue(), "country");
 			} catch (final Exception e) {
 				logger.debug(String.format("Not creating country : %s already exist", entry.getKey()));
 			}
 		}
 		preparedData = importer.prepareDataForImport(file);
 
 		return preparedData;
 	}
 }
