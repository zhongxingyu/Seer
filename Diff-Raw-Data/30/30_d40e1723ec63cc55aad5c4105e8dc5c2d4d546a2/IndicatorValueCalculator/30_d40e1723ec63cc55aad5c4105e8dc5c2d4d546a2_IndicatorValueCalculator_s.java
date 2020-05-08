 package org.motechproject.carereporting.indicator;
 
 import org.apache.log4j.Logger;
 import org.dwQueryBuilder.builders.QueryBuilder;
 import org.dwQueryBuilder.data.queries.DwQuery;
 import org.jooq.SQLDialect;
 import org.motechproject.carereporting.domain.AreaEntity;
 import org.motechproject.carereporting.domain.ComplexDwQueryEntity;
 import org.motechproject.carereporting.domain.DwQueryEntity;
 import org.motechproject.carereporting.domain.FactEntity;
 import org.motechproject.carereporting.domain.FrequencyEntity;
 import org.motechproject.carereporting.domain.IndicatorEntity;
 import org.motechproject.carereporting.domain.IndicatorValueEntity;
 import org.motechproject.carereporting.service.AreaService;
 import org.motechproject.carereporting.service.IndicatorService;
 import org.motechproject.carereporting.utils.date.DateResolver;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.annotation.Resource;
 import javax.sql.DataSource;
 import java.math.BigDecimal;
 import java.util.Date;
 
 @Component
 @Transactional
 public class IndicatorValueCalculator {
 
     private static final Logger LOG = Logger.getLogger(IndicatorValueCalculator.class);
 
     private static final SQLDialect SQL_DIALECT = SQLDialect.POSTGRES;
 
     @Value("${care.jdbc.schema}")
     private String schemaName;
 
     @Resource(name = "careDataSource")
     private DataSource careDataSource;
 
     @Autowired
     private IndicatorService indicatorService;
 
     @Autowired
     private AreaService areaService;
 
     private DwQueryHelper dwQueryHelper = new DwQueryHelper();
 
     @Transactional(readOnly = false)
     public void calculateIndicatorValues(FrequencyEntity frequency, Date date) {
         LOG.info("Running indicator values calculation for " + frequency.getFrequencyName() + " frequency.");
         int totalIndicatorValuesCalculated = 0;
         Date[] dates = DateResolver.resolveDates(frequency, date);
         Date from = dates[0];
         Date to = dates[1];
         for (IndicatorEntity indicator: indicatorService.getAllIndicators()) {
             calculateAndPersistIndicatorValue(indicator, frequency, from, to);
             ++totalIndicatorValuesCalculated;
             LOG.info("Calculating values for indicator: " + indicator.getName() + " finished.");
         }
         LOG.info("Indicator values calculation finished [total indicators values calculated = "
                 + totalIndicatorValuesCalculated + "].");
     }
 
     public void calculateAndPersistIndicatorValue(IndicatorEntity indicator, FrequencyEntity frequency, Date from, Date to) {
         for (AreaEntity area : areaService.getAllAreas()) {
             IndicatorValueEntity value = calculateIndicatorValueForArea(indicator, area, from, to);
             value.setArea(area);
            value.setDate(from);
             value.setFrequency(frequency);
             value.setIndicator(indicator);
             persistIndicatorValue(value);
         }
     }
 
     private IndicatorValueEntity calculateIndicatorValueForArea(IndicatorEntity indicator,
                                                                 AreaEntity area, Date from, Date to) {
         BigDecimal denominatorValue = calculateDwQueryValue(indicator.getDenominator(), area, from, to);
         BigDecimal numeratorValue = indicator.getNumerator() != null
                 ? calculateDwQueryValue(indicator.getNumerator(), area, from, to)
                 : null;
         return prepareIndicatorValueEntity(denominatorValue, numeratorValue);
     }
 
     private BigDecimal calculateDwQueryValue(DwQueryEntity dwQueryEntity, AreaEntity area, Date from, Date to) {
         DwQuery query = dwQueryHelper.buildDwQuery(dwQueryEntity, area);
         String sqlQuery = QueryBuilder.getDwQueryAsSQLString(SQL_DIALECT,
                 schemaName, query, false);
         if (shouldBindDatesTo(dwQueryEntity)) {
             sqlQuery = dwQueryHelper.formatFromDateAndToDate(sqlQuery, from, to);
         }
         return executeQuery(sqlQuery);
     }
 
     private boolean shouldBindDatesTo(DwQueryEntity dwQueryEntity) {
         return dwQueryEntity.getHasPeriodCondition()
                 || shouldBindToCombination(dwQueryEntity)
                 || shouldBindToOneOfFacts(dwQueryEntity);
     }
 
     private boolean shouldBindToCombination(DwQueryEntity dwQueryEntity) {
         return (dwQueryEntity.getCombination() != null && shouldBindDatesTo(dwQueryEntity.getCombination().getDwQuery()));
     }
 
     private boolean shouldBindToOneOfFacts(DwQueryEntity dwQueryEntity) {
         if (dwQueryEntity instanceof ComplexDwQueryEntity) {
             for (FactEntity fact: ((ComplexDwQueryEntity) dwQueryEntity).getFacts()) {
                 if (shouldBindDatesTo(fact.getTable())) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     private BigDecimal executeQuery(String query) {
         JdbcTemplate jdbcTemplate = new JdbcTemplate(careDataSource);
         return jdbcTemplate.queryForObject(query, BigDecimal.class);
     }
 
     private IndicatorValueEntity prepareIndicatorValueEntity(BigDecimal denominatorValue,
                                                              BigDecimal numeratorValue) {
         IndicatorValueEntity value = new IndicatorValueEntity();
         value.setDenominator(denominatorValue);
         value.setNumerator(numeratorValue);
 
         BigDecimal indicatorValue = numeratorValue == null
                 ? denominatorValue
                 : numeratorValue.divide(denominatorValue);
 
         value.setValue(indicatorValue);
         return value;
     }
 
     private void persistIndicatorValue(IndicatorValueEntity value) {
         indicatorService.createNewIndicatorValue(value);
     }
 }
