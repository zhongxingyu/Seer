 /**
  *
  */
 package org.ocha.hdx.importer;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ocha.hdx.config.ConfigurationConstants;
 import org.ocha.hdx.importer.helper.IndicatorTypeInformationHolder;
 import org.ocha.hdx.model.validation.ValidationReport;
import org.ocha.hdx.persistence.dao.config.ResourceConfigurationDao;
 import org.ocha.hdx.persistence.dao.dictionary.SourceDictionaryDAO;
 import org.ocha.hdx.persistence.entity.ckan.CKANDataset;
 import org.ocha.hdx.persistence.entity.configs.AbstractConfigEntry;
 import org.ocha.hdx.persistence.entity.configs.ResourceConfigEntry;
 import org.ocha.hdx.persistence.entity.configs.ResourceConfiguration;
 import org.ocha.hdx.persistence.entity.curateddata.Indicator;
 import org.ocha.hdx.persistence.entity.curateddata.Indicator.Periodicity;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorType;
 import org.ocha.hdx.persistence.entity.curateddata.IndicatorValue;
 import org.ocha.hdx.persistence.entity.curateddata.Source;
 import org.ocha.hdx.persistence.entity.dictionary.SourceDictionary;
 import org.ocha.hdx.service.IndicatorCreationService;
 import org.ocha.hdx.validation.itemvalidator.IValidatorCreator;
 import org.ocha.hdx.validation.prevalidator.IPreValidatorCreator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 
 import com.github.springtestdbunit.DbUnitTestExecutionListener;
 import com.github.springtestdbunit.annotation.DatabaseSetup;
 import com.github.springtestdbunit.annotation.DatabaseTearDown;
 import com.github.springtestdbunit.annotation.DbUnitConfiguration;
 
 /**
  * @author alexandru-m-g
  *
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:/ctx-config-test.xml", "classpath:/ctx-core.xml", "classpath:/ctx-dao.xml",
 		"classpath:/ctx-persistence-test.xml", "classpath:/ctx-service.xml" })
 @TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class})
 @DbUnitConfiguration(databaseConnection="datasource")
 public class ScraperValidatingImporterAllIndicatorTypesTest {
 
 	private static Logger logger = LoggerFactory.getLogger(ScraperValidatingImporterAllIndicatorTypesTest.class);
 
 	public final static List<String> ALLOWED_INDICATOR_TYPES_LIST = Collections.unmodifiableList(Arrays.asList("_Access to electricity (% of population)",
 			"_Children 1 year old immunized against measles, percentage", "_emdat:no_homeless", "_emdat:no_injured", "_emdat:total_affected",
 			"_GNI, PPP (current international $)", "_HDR:68606", "_International migrant stock (% of population)", "_Internet users per 100 inhabitants",
 			"_Land area (sq. km)", "_m49-name", "_Net ODA received per capita (current US$)", "_Number of infant deaths",
 			"_Population undernourished, millions", "_Population undernourished, percentage", "_Population, total", "_unterm:ISO Country alpha-2-code",
 			"_WPP2012_MORT_F02_CRUDE_DEATH_RATE", "CD010", "CD030", "CD050", "CD060", "CD070", "CD080", "CD090", "CG020", "CG030", "CG060", "CG070", "CG080",
 			"CG100", "CG120", "CG140", "CG150", "CG260", "CG290", "CH070", "CH080", "CH090", "CH100", "PCH090", "PCX090", "PCX100",
 			"PCX130", "PSE030", "PSE090", "PSE110", "PSE120", "PSE130", "PSE140", "PSE150", "PSE160", "PSE170", "PSE200", "PSE210", "PSE220", "PSP010",
 			"PSP050", "PSP060", "PSP070", "PSP080", "PSP090", "PSP100", "PSP110", "PVE010", "PVE030", "PVE110", "PVE120", "PVE130", "PVF020",
 			"PVH010", "PVH050", "PVH080", "PVH090", "PVH100", "PVH120", "PVH140", "PVH150", "PVH180", "PVH190", "PVL010", "PVL030", "PVL040", "PVN010",
 			"PVN050", "PVW010", "PVW040", "PVX010", "PVX020"));
 
 	public final static String ALLOWED_INDICATOR_TYPES = StringUtils.join(ALLOWED_INDICATOR_TYPES_LIST, "&&");
 
 
 	@Autowired
 	private IndicatorCreationService indicatorCreationService;
 
 	@Autowired
 	private SourceDictionaryDAO sourceDictionaryDAO;
 
 	@Autowired
	private ResourceConfigurationDao resourceConfigurationDao;
 
 	@Resource
 	private List<IValidatorCreator> validatorCreators;
 
 	@Resource
 	private List<IPreValidatorCreator> preValidatorCreators;
 
 	@Test
 	@DatabaseSetup("/samples/scraper/bootstrap_data.xml")
 	@DatabaseTearDown("/samples/scraper/teardown_data.xml")
 	public void testImportAllIndicatorTypes() {
 
 		final ResourceConfiguration config = this.getConfigWithInjectedAllowedIndicators();
 
 
 		final ValidationReport report		= new ValidationReport(CKANDataset.Type.SCRAPER_VALIDATING);
 
 		logger.info("Starting 1st phase of import");
 		final TestScraperValidatingImporter importer =
 				new TestScraperValidatingImporter(this.sourceDictionaryDAO.getSourceDictionariesByImporter("scraper-validator"),
 				config, this.validatorCreators, this.preValidatorCreators, report,  this.indicatorCreationService);
 		final PreparedData preparedData = importer.prepareDataForImport(null);
 
 		this.logMissingIndicators(this.getTypesFromPreparedData(preparedData));
 
 		assertTrue(preparedData.isSuccess());
 
 		assertEquals(ALLOWED_INDICATOR_TYPES_LIST.size(), preparedData.getIndicatorsToImport().size() );
 
 
 		logger.info("Starting 2nd phase of import");
 
 		final List<Indicator> indicators =  importer.transformToFinalFormat();
 
 		this.logMissingIndicators(this.getTypesFromIndicatorList(indicators));
 
 		assertEquals(ALLOWED_INDICATOR_TYPES_LIST.size(), indicators.size() );
 
 		for (final Indicator indicator: indicators) {
 			final IndicatorValue value	= indicator.getValue();
 			assertNotNull(value);
 
 			final IndicatorType type	= indicator.getType();
 			assertNotNull(type);
 
 			final Source source	= indicator.getSource();
 			assertNotNull(source);
 
 			switch (type.getValueType()) {
 			case TEXT:
 				assertNotNull(
 						String.format("The value is not of the specified ValueType for indicator: %s",indicator.toString()),
 						value.getStringValue()
 					);
 				break;
 			case NUMBER:
 				assertNotNull(
 						String.format("The value is not of the specified ValueType for indicator: %s",indicator.toString()),
 						value.getNumberValue()
 					);
 				break;
 //			case DATE:
 //				assertNotNull(
 //						String.format("The value is not of the specified ValueType for indicator: %s",indicator.toString()),
 //						value.getDateValue()
 //					);
 //				break;
 //			case DATETIME:
 //				assertNotNull(
 //						String.format("The value is not of the specified ValueType for indicator: %s",indicator.toString()),
 //						value.getDatetimeValue()
 //					);
 			default:
 				break;
 			}
 
 			final IndicatorTypeInformationHolder infoHolder	= importer.getIndicatorTypeInformationHolder(type.getCode(), source.getCode());
 			final AbstractConfigEntry configEntry	= infoHolder.getIndicatorEntries().get(ConfigurationConstants.IndicatorConfiguration.EXPECTED_TIME_FORMAT);
 
 			if (configEntry!= null) {
 				final String timeFormat	= configEntry.getEntryValue();
 				if ( timeFormat.contains("P1Y") ) {
 					assertEquals(Periodicity.YEAR, indicator.getPeriodicity());
 				}
 				else if (timeFormat.contains("P2Y")) {
 					assertEquals(Periodicity.TWO_YEARS, indicator.getPeriodicity());
 				}
 				else if (timeFormat.contains("P3Y")) {
 					assertEquals(Periodicity.THREE_YEARS, indicator.getPeriodicity());
 				}
 				else if (timeFormat.contains("P5Y")) {
 					assertEquals(Periodicity.FIVE_YEARS, indicator.getPeriodicity());
 				}
 				else if (timeFormat.contains("P10Y")) {
 					assertEquals(Periodicity.TEN_YEARS, indicator.getPeriodicity());
 				}
 			}
 
 		}
 
 	}
 
 
 	@SuppressWarnings("unchecked")
 	private void logMissingIndicators(final Collection<String> importedIndicatorTypes) {
 		final Collection<String> differenceCollection	= CollectionUtils.subtract(ALLOWED_INDICATOR_TYPES_LIST, importedIndicatorTypes);
 		if (differenceCollection != null) {
 			for (final String indTypeCode : differenceCollection) {
 				logger.warn("Indicator type: " + indTypeCode + " - was not imported");
 			}
 		}
 
 	}
 
 	private Collection<String> getTypesFromPreparedData(final PreparedData preparedData) {
 		final List<String> list	= new ArrayList<String>();
 		if ( preparedData != null && preparedData.getIndicatorsToImport() != null ) {
 			for (final PreparedIndicator preparedIndicator : preparedData.getIndicatorsToImport()) {
 				list.add(preparedIndicator.getIndicatorTypeCode());
 			}
 		}
 		return list;
 	}
 
 	private Collection<String> getTypesFromIndicatorList(final List<Indicator> indicators) {
 		final List<String> list	= new ArrayList<String>();
 		if ( indicators != null ) {
 			for (final Indicator indicator : indicators ) {
 				list.add(indicator.getType().getCode());
 			}
 		}
 		return list;
 	}
 
 
 	/**
 	 * @return
 	 */
 	private ResourceConfiguration getConfigWithInjectedAllowedIndicators() {
		final ResourceConfiguration config	= this.resourceConfigurationDao.getResourceConfigurationById(1);
 
 		ResourceConfigEntry allowedIndicatorTypesEntry	= null;
 		for (final ResourceConfigEntry entry : config.getGeneralConfigEntries()) {
 			if ( ConfigurationConstants.GeneralConfiguration.ALLOWED_INDICATOR_TYPES.getLabel().equals(entry.getEntryKey()) ) {
 				allowedIndicatorTypesEntry	= entry;
 				break;
 			}
 		}
 		if ( allowedIndicatorTypesEntry == null ) {
 			allowedIndicatorTypesEntry	= new ResourceConfigEntry(ConfigurationConstants.GeneralConfiguration.ALLOWED_INDICATOR_TYPES.getLabel(), ALLOWED_INDICATOR_TYPES);
 			config.getGeneralConfigEntries().add(allowedIndicatorTypesEntry);
 		}
 		allowedIndicatorTypesEntry.setEntryValue(ALLOWED_INDICATOR_TYPES);
 		return config;
 	}
 
 
 	public class TestScraperValidatingImporter extends ScraperValidatingImporter {
 
 		public TestScraperValidatingImporter(final List<SourceDictionary> sourceDictionaries, final ResourceConfiguration resourceConfiguration,
 				final List<IValidatorCreator> validatorCreators, final List<IPreValidatorCreator> preValidatorCreators, final ValidationReport report,
 				final IndicatorCreationService indicatorCreationService) {
 			super(sourceDictionaries, resourceConfiguration, validatorCreators, preValidatorCreators, report, indicatorCreationService);
 		}
 
 		@Override
 		protected File findValueFile(final File file) {
 			File csvValueFile;
 			try {
 				csvValueFile = new ClassPathResource("samples/scraper/one-value-for-all-indicator-types.csv").getFile();
 				return csvValueFile;
 			} catch (final IOException e) {
 				e.printStackTrace();
 				throw new IllegalArgumentException("Problem with file with sample values for indicator types");
 			}
 		}
 
 		public IndicatorTypeInformationHolder getIndicatorTypeInformationHolder(final String indTypeCode, final String srcCode) {
 			final String key	= this.generateMapKey(indTypeCode, srcCode);
 			return this.infoPerIndicatorTypeMap.get(key);
 
 		}
 
 
 	}
 
 }
