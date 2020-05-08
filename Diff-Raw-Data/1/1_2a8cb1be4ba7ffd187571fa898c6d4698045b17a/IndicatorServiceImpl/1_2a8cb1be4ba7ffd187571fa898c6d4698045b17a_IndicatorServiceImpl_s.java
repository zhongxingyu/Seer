 package org.motechproject.carereporting.service.impl;
 
 import org.apache.commons.lang.StringUtils;
 import org.dwQueryBuilder.builders.QueryBuilder;
 import org.hibernate.Hibernate;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.jooq.SQLDialect;
 import org.motechproject.carereporting.dao.DwQueryDao;
 import org.motechproject.carereporting.dao.IndicatorCategoryDao;
 import org.motechproject.carereporting.dao.IndicatorDao;
 import org.motechproject.carereporting.dao.IndicatorTypeDao;
 import org.motechproject.carereporting.dao.IndicatorValueDao;
 import org.motechproject.carereporting.domain.AreaEntity;
 import org.motechproject.carereporting.domain.CombinationEntity;
 import org.motechproject.carereporting.domain.ComputedFieldEntity;
 import org.motechproject.carereporting.domain.ConditionEntity;
 import org.motechproject.carereporting.domain.DashboardEntity;
 import org.motechproject.carereporting.domain.DateDiffComparisonConditionEntity;
 import org.motechproject.carereporting.domain.DateRangeComparisonConditionEntity;
 import org.motechproject.carereporting.domain.DateValueComparisonConditionEntity;
 import org.motechproject.carereporting.domain.DwQueryEntity;
 import org.motechproject.carereporting.domain.EnumRangeComparisonConditionEntity;
 import org.motechproject.carereporting.domain.EnumRangeComparisonConditionValueEntity;
 import org.motechproject.carereporting.domain.FieldComparisonConditionEntity;
 import org.motechproject.carereporting.domain.FormEntity;
 import org.motechproject.carereporting.domain.FrequencyEntity;
 import org.motechproject.carereporting.domain.GroupedByEntity;
 import org.motechproject.carereporting.domain.HavingEntity;
 import org.motechproject.carereporting.domain.IndicatorCategoryEntity;
 import org.motechproject.carereporting.domain.IndicatorEntity;
 import org.motechproject.carereporting.domain.IndicatorTypeEntity;
 import org.motechproject.carereporting.domain.IndicatorValueEntity;
 import org.motechproject.carereporting.domain.LevelEntity;
 import org.motechproject.carereporting.domain.PeriodConditionEntity;
 import org.motechproject.carereporting.domain.ReportEntity;
 import org.motechproject.carereporting.domain.ReportTypeEntity;
 import org.motechproject.carereporting.domain.RoleEntity;
 import org.motechproject.carereporting.domain.SelectColumnEntity;
 import org.motechproject.carereporting.domain.UserEntity;
 import org.motechproject.carereporting.domain.ValueComparisonConditionEntity;
 import org.motechproject.carereporting.domain.WhereGroupEntity;
 import org.motechproject.carereporting.domain.dto.DwQueryDto;
 import org.motechproject.carereporting.domain.dto.GroupByDto;
 import org.motechproject.carereporting.domain.dto.IndicatorCreationFormDto;
 import org.motechproject.carereporting.domain.dto.IndicatorDto;
 import org.motechproject.carereporting.domain.dto.IndicatorWithTrendDto;
 import org.motechproject.carereporting.domain.dto.QueryCreationFormDto;
 import org.motechproject.carereporting.domain.dto.SelectColumnDto;
 import org.motechproject.carereporting.domain.dto.TrendIndicatorCategoryDto;
 import org.motechproject.carereporting.domain.dto.WhereConditionDto;
 import org.motechproject.carereporting.domain.dto.WhereGroupDto;
 import org.motechproject.carereporting.exception.CareNoValuesException;
 import org.motechproject.carereporting.exception.CareRuntimeException;
 import org.motechproject.carereporting.indicator.DwQueryHelper;
 import org.motechproject.carereporting.initializers.IndicatorValuesInitializer;
 import org.motechproject.carereporting.service.AreaService;
 import org.motechproject.carereporting.service.ComputedFieldService;
 import org.motechproject.carereporting.service.CronService;
 import org.motechproject.carereporting.service.DashboardService;
 import org.motechproject.carereporting.service.ExportService;
 import org.motechproject.carereporting.service.FormsService;
 import org.motechproject.carereporting.service.IndicatorService;
 import org.motechproject.carereporting.service.ReportService;
 import org.motechproject.carereporting.service.UserService;
 import org.motechproject.carereporting.utils.configuration.ConfigurationLocator;
 import org.motechproject.carereporting.utils.date.DateResolver;
 import org.motechproject.carereporting.xml.XmlCaseListReportParser;
 import org.motechproject.carereporting.xml.mapping.reports.CaseListReport;
 import org.motechproject.carereporting.xml.mapping.reports.ReportField;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.annotation.Resource;
 import javax.sql.DataSource;
 import javax.xml.bind.JAXBException;
 import java.io.File;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 @Service
 @Transactional(readOnly = true)
 public class IndicatorServiceImpl implements IndicatorService {
 
     private static final int TREND_NEUTRAL = 0;
     private static final int TREND_NEGATIVE = -1;
     private static final int TREND_POSITIVE = 1;
     private static final SQLDialect SQL_DIALECT = SQLDialect.POSTGRES;
     private static final String REPLACE_SELECT_WITH_SELECT_COLUMNS =
             "select (.*?) from \\\"(.*?)\\\"\\.\\\"(\\w*?_case)\\\"(.*)?";
     private static final String CASE_LIST_REPORT_XML_DIRECTORY = ConfigurationLocator.getCareXmlDirectory()
             + File.separatorChar + "caseListReport";
 
     @Value("${care.jdbc.schema}")
     private String schemaName;
 
     @Resource(name = "careDataSource")
     private DataSource careDataSource;
 
     @Autowired
     private IndicatorDao indicatorDao;
 
     @Autowired
     private IndicatorTypeDao indicatorTypeDao;
 
     @Autowired
     private IndicatorCategoryDao indicatorCategoryDao;
 
     @Autowired
     private IndicatorValueDao indicatorValueDao;
 
     @Autowired
     private CronService cronService;
 
     @Autowired
     private AreaService areaService;
 
     @Autowired
     private DashboardService dashboardService;
 
     @Autowired
     private ReportService reportService;
 
     @Autowired
     private ExportService csvExportService;
 
     @Autowired
     private XmlCaseListReportParser xmlCaseListReportParser;
 
     @Autowired
     private UserService userService;
 
     @Autowired
     private FormsService formsService;
 
     @Autowired
     private DwQueryDao dwQueryDao;
 
     @Autowired
     private ComputedFieldService computedFieldService;
 
     @Autowired
     private SessionFactory sessionFactory;
 
     private static final Integer ADMIN_ROLE_ID = 1;
     private static final Integer READ_ONLY_ROLE_ID = 4;
 
     @Transactional
     public Set<IndicatorEntity> getAllIndicators() {
         return indicatorDao.getAllWithFields("reports");
     }
 
     @Transactional
     @Override
     @SuppressWarnings("unchecked")
     public Set<IndicatorEntity> getAllIndicatorsByUserAccess(UserEntity userEntity) {
         List <Integer> roleIds = new ArrayList<>();
         for (RoleEntity roleEntity : userEntity.getRoles()) {
             roleIds.add(roleEntity.getId());
         }
         Query query = sessionFactory.getCurrentSession()
                .createQuery("SELECT i from IndicatorEntity i " +
                            "JOIN i.roles as r " +
                            "WHERE (i.areaLevel.id >= :accessLevel " +
                            "AND r.id IN (:roles)) " +
                            "OR (i.owner.id = :ownerId)");
         query.setParameter("accessLevel", userEntity.getArea().getLevelId());
         query.setParameterList("roles", roleIds);
         query.setParameter("ownerId", userEntity.getId());
         return new LinkedHashSet<IndicatorEntity>(query.list());
     }
 
     @Override
     public List<IndicatorValueEntity> getIndicatorValuesForCsv(Integer indicatorId, Integer areaId, Integer frequencyId, Date startDate, Date endDate) {
         FrequencyEntity frequencyEntity = cronService.getFrequencyById(frequencyId);
         Date[] dates = DateResolver.resolveDates(frequencyEntity, startDate, endDate);
         List<IndicatorValueEntity> valueEntities = getIndicatorValuesForArea(indicatorId, areaId, frequencyId, dates[0], dates[1], null);
 
         for (IndicatorValueEntity indicatorValueEntity : valueEntities) {
             Hibernate.initialize(indicatorValueEntity.getArea());
             Hibernate.initialize(indicatorValueEntity.getIndicator());
         }
 
         return valueEntities;
     }
 
     @Override
     public Set<DwQueryEntity> getAllTopLevelDwQueries() {
         return dwQueryDao.getAllByField("parentQuery", null);
     }
 
     @Override
     public Set<DwQueryEntity> getAllDwQueries() {
         return dwQueryDao.getAll();
     }
 
     @Override
     public DwQueryEntity getDwQueryById(Integer dwQueryId) {
         return dwQueryDao.getById(dwQueryId);
     }
 
     @Override
     @Transactional(readOnly = false)
     public void createNewDwQuery(DwQueryDto dwQueryDto) {
         this.dwQueryDao.save(getDwQueryEntityFromDto(dwQueryDto));
     }
 
     @Override
     @Transactional(readOnly = false)
     public void deleteDwQuery(DwQueryEntity dwQueryEntity) {
         this.dwQueryDao.remove(dwQueryEntity);
     }
 
     @Transactional(readOnly = false)
     private DwQueryEntity getDwQueryEntityFromDto(DwQueryDto dwQueryDto) {
         DwQueryEntity dwQueryEntity = new DwQueryEntity();
 
         Set<SelectColumnEntity> selectColumnEntities = new LinkedHashSet<>();
         for (SelectColumnDto selectColumnDto : dwQueryDto.getSelectColumns()) {
             SelectColumnEntity selectColumnEntity = new SelectColumnEntity();
             ComputedFieldEntity computedFieldEntity = (selectColumnDto.getField() == null)
                     ? null : computedFieldService.getComputedFieldById(selectColumnDto.getField());
 
             selectColumnEntity.setComputedField(computedFieldEntity);
             selectColumnEntity.setFunctionName(selectColumnDto.getFunction());
             selectColumnEntity.setNullValue(selectColumnDto.getNullValue());
             selectColumnEntity.setDwQuery(dwQueryEntity);
             selectColumnEntities.add(selectColumnEntity);
         }
 
         dwQueryEntity.setName(dwQueryDto.getName());
         dwQueryEntity.setTableName(dwQueryDto.getDimension());
         dwQueryEntity.setSelectColumns(selectColumnEntities);
         dwQueryEntity.setWhereGroup(resolveWhereGroupDto(dwQueryEntity, dwQueryDto.getWhereGroup()));
         resolveGroupByDto(dwQueryDto.getGroupBy(), dwQueryEntity);
         resolveCombination(dwQueryEntity, dwQueryDto);
 
         return dwQueryEntity;
     }
 
     private void resolveGroupByDto(GroupByDto groupBy, DwQueryEntity dwQueryEntity) {
         if (groupBy == null) {
             return;
         }
 
         GroupedByEntity groupedByEntity = new GroupedByEntity();
         groupedByEntity.setTableName(groupBy.getTableName());
         groupedByEntity.setFieldName(groupBy.getFieldName());
 
         if (groupBy.getHaving() != null) {
             HavingEntity havingEntity = new HavingEntity();
 
             SelectColumnEntity selectColumnEntity = new SelectColumnEntity();
             selectColumnEntity.setComputedField(computedFieldService.getComputedFieldById(groupBy.getFieldId()));
             selectColumnEntity.setFunctionName(groupBy.getHaving().getFunction());
 
             havingEntity.setSelectColumnEntity(selectColumnEntity);
             havingEntity.setOperator(groupBy.getHaving().getOperator());
             havingEntity.setValue(groupBy.getHaving().getValue());
 
             groupedByEntity.setHaving(havingEntity);
         }
 
         dwQueryEntity.setGroupedBy(groupedByEntity);
     }
 
     private void resolveCombination(DwQueryEntity dwQueryEntity, DwQueryDto dwQueryDto) {
         if (dwQueryDto.getCombineWith() == null) {
             return;
         }
 
         CombinationEntity combinationEntity = new CombinationEntity();
         combinationEntity.setType(dwQueryDto.getJoinType());
         combinationEntity.setForeignKey(dwQueryDto.getKey1());
         combinationEntity.setReferencedKey(dwQueryDto.getKey2());
         combinationEntity.setDwQuery(getDwQueryEntityFromDto(dwQueryDto.getCombineWith()));
         combinationEntity.getDwQuery().setParentQuery(dwQueryEntity);
 
         dwQueryEntity.setCombination(combinationEntity);
     }
 
     private WhereGroupEntity resolveWhereGroupDto(DwQueryEntity dwQueryEntity, WhereGroupDto whereGroup) {
         WhereGroupEntity whereGroupEntity = new WhereGroupEntity();
 
         if (whereGroup.getGroups() != null) {
             for (WhereGroupDto whereGroupDto : whereGroup.getGroups()) {
                 whereGroupEntity.getWhereGroups().add(resolveWhereGroupDto(dwQueryEntity, whereGroupDto));
             }
         }
 
         if (whereGroup.getConditions() != null) {
             for (WhereConditionDto whereConditionDto : whereGroup.getConditions()) {
                 whereGroupEntity.getConditions().add(resolveWhereConditionDto(dwQueryEntity, whereConditionDto));
             }
         }
 
         return whereGroupEntity;
     }
 
     private ConditionEntity resolveWhereConditionDto(DwQueryEntity dwQueryEntity, WhereConditionDto whereConditionDto) {
         switch (whereConditionDto.getType()) {
             case "dateDiff":
                 return prepareDateDiffCondition(whereConditionDto);
             case "dateRange":
                 return prepareDateRangeCondition(whereConditionDto);
             case "dateValue":
                 return prepareDateValueCondition(whereConditionDto);
             case "enumRange":
                 return prepareEnumRangeCondition(whereConditionDto);
             case "field":
                 return prepareFieldCondition(whereConditionDto);
             case "value":
                 return prepareValueCondition(whereConditionDto);
             case "period":
                 dwQueryEntity.setHasPeriodCondition(true);
                 return preparePeriodCondition(whereConditionDto);
             default:
                 return null;
         }
     }
 
     private ConditionEntity prepareDateDiffCondition(WhereConditionDto whereConditionDto) {
         DateDiffComparisonConditionEntity dateDiffCondition = new DateDiffComparisonConditionEntity();
         dateDiffCondition.setField1(computedFieldService.getComputedFieldById(whereConditionDto.getField1()));
         dateDiffCondition.setField2(computedFieldService.getComputedFieldById(whereConditionDto.getField2()));
         dateDiffCondition.setOperator(computedFieldService.getComparisonSymbolByName(whereConditionDto.getOperator()));
         dateDiffCondition.setOffset1(whereConditionDto.getFieldOffset1());
         dateDiffCondition.setOffset2(whereConditionDto.getFieldOffset2());
         dateDiffCondition.setValue(Integer.parseInt(whereConditionDto.getValue()));
         return dateDiffCondition;
     }
 
     private ConditionEntity prepareDateRangeCondition(WhereConditionDto whereConditionDto) {
         DateRangeComparisonConditionEntity dateRangeCondition = new DateRangeComparisonConditionEntity();
         dateRangeCondition.setField1(computedFieldService.getComputedFieldById(whereConditionDto.getField1()));
         dateRangeCondition.setOffset1(whereConditionDto.getFieldOffset1());
         dateRangeCondition.setDate1(whereConditionDto.getDate1());
         dateRangeCondition.setDate2(whereConditionDto.getDate2());
         return dateRangeCondition;
     }
 
     private ConditionEntity prepareDateValueCondition(WhereConditionDto whereConditionDto) {
         DateValueComparisonConditionEntity dateValueCondition = new DateValueComparisonConditionEntity();
         dateValueCondition.setField1(computedFieldService.getComputedFieldById(whereConditionDto.getField1()));
         dateValueCondition.setOperator(computedFieldService.getComparisonSymbolByName(whereConditionDto.getOperator()));
         dateValueCondition.setOffset1(whereConditionDto.getFieldOffset1());
         dateValueCondition.setValue(new Date(whereConditionDto.getValue()));
         return dateValueCondition;
     }
 
     private ConditionEntity prepareEnumRangeCondition(WhereConditionDto whereConditionDto) {
         EnumRangeComparisonConditionEntity enumRangeCondition = new EnumRangeComparisonConditionEntity();
         enumRangeCondition.setField1(computedFieldService.getComputedFieldById(whereConditionDto.getField1()));
         Set<EnumRangeComparisonConditionValueEntity> values = new LinkedHashSet<>();
         for (String value : whereConditionDto.getValues()) {
             EnumRangeComparisonConditionValueEntity valueEntity = new EnumRangeComparisonConditionValueEntity();
             valueEntity.setCondition(enumRangeCondition);
             valueEntity.setValue(value);
             values.add(valueEntity);
         }
         enumRangeCondition.setValues(values);
         return enumRangeCondition;
     }
 
     private ConditionEntity prepareFieldCondition(WhereConditionDto whereConditionDto) {
         FieldComparisonConditionEntity fieldCondition = new FieldComparisonConditionEntity();
         fieldCondition.setField1(computedFieldService.getComputedFieldById(whereConditionDto.getField1()));
         fieldCondition.setField2(computedFieldService.getComputedFieldById(whereConditionDto.getField2()));
         fieldCondition.setOperator(computedFieldService.getComparisonSymbolByName(whereConditionDto.getOperator()));
         String offset1 = (whereConditionDto.getFieldOffset1() == null) ? null : whereConditionDto.getFieldOffset1().toString();
         String offset2 = (whereConditionDto.getFieldOffset2() == null) ? null : whereConditionDto.getFieldOffset2().toString();
         fieldCondition.setOffset1(offset1);
         fieldCondition.setOffset2(offset2);
         return fieldCondition;
     }
 
     private ConditionEntity prepareValueCondition(WhereConditionDto whereConditionDto) {
         ValueComparisonConditionEntity valueCondition = new ValueComparisonConditionEntity();
         valueCondition.setField1(computedFieldService.getComputedFieldById(whereConditionDto.getField1()));
         valueCondition.setOperator(computedFieldService.getComparisonSymbolByName(whereConditionDto.getOperator()));
         valueCondition.setValue(whereConditionDto.getValue());
         return valueCondition;
     }
 
     private ConditionEntity preparePeriodCondition(WhereConditionDto whereConditionDto) {
         PeriodConditionEntity periodConditionEntity = new PeriodConditionEntity();
         periodConditionEntity.setTableName(whereConditionDto.getTableName1());
         periodConditionEntity.setColumnName(computedFieldService.getComputedFieldById(whereConditionDto.getField1()).getName());
         periodConditionEntity.setOffset(whereConditionDto.getFieldOffset1());
         return periodConditionEntity;
     }
 
     @Transactional
     @Override
     public Set<IndicatorEntity> getIndicatorsByCategoryId(Integer categoryId) {
         return indicatorDao.getIndicatorsByCategoryId(categoryId);
     }
 
     @Transactional
     @Override
     public IndicatorEntity getIndicatorById(Integer id) {
         return indicatorDao.getById(id);
     }
 
     @Transactional(readOnly = false)
     @Override
     public void createNewIndicator(IndicatorEntity indicatorEntity) {
         indicatorDao.save(indicatorEntity);
         calculateIndicator(indicatorEntity);
     }
 
     @Override
     @Transactional(readOnly = false)
     public void createNewIndicatorFromDto(IndicatorDto indicatorDto) {
         IndicatorEntity indicatorEntity = new IndicatorEntity();
         indicatorEntity.setCategories(findIndicatorCategoryEntitiesFromDto(indicatorDto));
         indicatorEntity.setReports(indicatorDto.getReports());
         for (ReportEntity reportEntity : indicatorEntity.getReports()) {
             reportEntity.setIndicator(indicatorEntity);
         }
         indicatorEntity.setDefaultFrequency(findFrequencyEntityFromDto(indicatorDto));
         indicatorEntity.setAreaLevel(findLevelEntityFromDto(indicatorDto));
         indicatorEntity.setName(indicatorDto.getName());
         indicatorEntity.setTrend(indicatorDto.getTrend());
         indicatorEntity.setOwner(findUserEntityFromDto(indicatorDto));
         indicatorEntity.setRoles(findRoleEntitiesFromDto(indicatorDto));
         indicatorEntity.setNumerator(this.getDwQueryById(indicatorDto.getNumerator()));
         if (indicatorDto.getDenominator() != null) {
             indicatorEntity.setDenominator(this.getDwQueryById(indicatorDto.getDenominator()));
         }
         indicatorEntity.setComputed(false);
         indicatorEntity.setAdditive(indicatorDto.isAdditive());
         indicatorEntity.setCategorized(indicatorDto.isCategorized());
         createNewIndicator(indicatorEntity);
     }
 
     private Set<RoleEntity> findRoleEntitiesFromDto(IndicatorDto indicatorDto) {
         Set<RoleEntity> roleEntities = new LinkedHashSet<>();
 
         for (Integer roleEntityId : indicatorDto.getOwners()) {
             if (roleEntityId != 0) {
                 roleEntities.add(userService.getRoleById(roleEntityId));
             }
         }
 
         return roleEntities;
     }
 
     private UserEntity findUserEntityFromDto(IndicatorDto indicatorDto) {
         if (indicatorDto.getOwners().size() == 1 && Integer.valueOf(0).equals(indicatorDto.getOwners().iterator().next())) {
             return userService.getCurrentlyLoggedUser();
         }
 
         return null;
     }
 
     private LevelEntity findLevelEntityFromDto(IndicatorDto indicatorDto) {
         return areaService.getLevelById(indicatorDto.getLevel());
     }
 
     private FrequencyEntity findFrequencyEntityFromDto(IndicatorDto indicatorDto) {
         return cronService.getFrequencyById(indicatorDto.getFrequency());
     }
 
     @Transactional(readOnly = false)
     @Override
     public void updateIndicator(IndicatorEntity indicatorEntity) {
         indicatorDao.update(indicatorEntity);
     }
 
     @Transactional(readOnly = false)
     @Override
     public void setComputedForIndicator(IndicatorEntity indicatorEntity, Boolean value) {
         indicatorEntity.setComputed(value);
         indicatorDao.update(indicatorEntity);
     }
 
     @Transactional(readOnly = false)
     @Override
     public void updateIndicatorFromDto(IndicatorDto indicatorDto) {
 
     }
 
     private Set<IndicatorCategoryEntity> findIndicatorCategoryEntitiesFromDto(
             IndicatorDto indicatorDto) {
         Set<IndicatorCategoryEntity> indicatorCategoryEntities = new LinkedHashSet<>();
 
         for (Integer indicatorCategoryId : indicatorDto.getCategories()) {
             IndicatorCategoryEntity indicatorCategoryEntity = this.getIndicatorCategoryById(indicatorCategoryId);
 
             indicatorCategoryEntities.add(indicatorCategoryEntity);
         }
 
         return indicatorCategoryEntities;
     }
 
     @Transactional(readOnly = false)
     @Override
     public void deleteIndicator(IndicatorEntity indicatorEntity) {
         indicatorDao.remove(indicatorEntity);
     }
 
     @Override
     public void deleteAllIndicators() {
         indicatorDao.removeAll();
     }
 
     // IndicatorTypeEntity
 
     @Transactional
     @Override
     public Set<IndicatorTypeEntity> getAllIndicatorTypes() {
         return indicatorTypeDao.getAll();
     }
 
     @Transactional
     @Override
     public IndicatorTypeEntity getIndicatorTypeById(Integer id) {
         return indicatorTypeDao.getById(id);
     }
 
     // IndicatorCategoryEntity
 
     @Transactional
     @Override
     public Set<IndicatorCategoryEntity> getAllIndicatorCategories() {
         return indicatorCategoryDao.getAll();
     }
 
     @Transactional
     @Override
     public IndicatorCategoryEntity getIndicatorCategoryById(Integer id) {
         return indicatorCategoryDao.getById(id);
     }
 
     @Transactional(readOnly = false)
     @Override
     public void createNewIndicatorCategory(IndicatorCategoryEntity indicatorCategoryEntity) {
         DashboardEntity dashboardForCategory = createDashboardForNewIndicatorCategory(indicatorCategoryEntity.getName());
         indicatorCategoryEntity.setDashboard(dashboardForCategory);
         indicatorCategoryDao.save(indicatorCategoryEntity);
     }
 
     private DashboardEntity createDashboardForNewIndicatorCategory(String name) {
         Short newDashboardTabPosition = dashboardService.getTabPositionForNewDashboard();
         return new DashboardEntity(name, newDashboardTabPosition);
     }
 
     @Transactional(readOnly = false)
     @Override
     public void updateIndicatorCategory(IndicatorCategoryEntity indicatorCategoryEntity) {
         indicatorCategoryEntity.getDashboard().setName(indicatorCategoryEntity.getName());
         indicatorCategoryDao.update(indicatorCategoryEntity);
     }
 
     @Transactional(readOnly = false)
     @Override
     public void deleteIndicatorCategory(IndicatorCategoryEntity indicatorCategoryEntity) {
         for (IndicatorEntity indicatorEntity : indicatorCategoryEntity.getIndicators()) {
             indicatorEntity.getCategories().remove(indicatorCategoryEntity);
         }
 
         indicatorCategoryDao.remove(indicatorCategoryEntity);
     }
 
     @Transactional
     @Override
     public Set<IndicatorValueEntity> getAllIndicatorValues() {
         return indicatorValueDao.getAll();
     }
 
     // IndicatorValueEntity
 
     @Transactional
     @Override
     public IndicatorValueEntity getIndicatorValueById(Integer id) {
         return indicatorValueDao.getById(id);
     }
 
     @Transactional(readOnly = false)
     @Override
     public void createNewIndicatorValue(IndicatorValueEntity indicatorValueEntity) {
         indicatorValueDao.save(indicatorValueEntity);
     }
 
     @Transactional(readOnly = false)
     @Override
     public void updateIndicatorValue(IndicatorValueEntity indicatorValueEntity) {
         indicatorValueDao.update(indicatorValueEntity);
     }
 
     @Transactional(readOnly = false)
     @Override
     public void deleteIndicatorValue(IndicatorValueEntity indicatorValueEntity) {
         indicatorValueDao.remove(indicatorValueEntity);
     }
 
     @Override
     @Transactional
     public List<IndicatorValueEntity> getIndicatorValuesForArea(Integer indicatorId, Integer areaId, Integer frequencyId,
                                                                 Date startDate, Date endDate, String category) {
         List<IndicatorValueEntity> values = indicatorValueDao.getIndicatorValuesForArea(indicatorId, areaId,
                 frequencyId, startDate, endDate, category);
         if (values.size() == 0) {
             throw new CareNoValuesException();
         }
         return values;
     }
 
     @Override
     @Transactional
     public Set<TrendIndicatorCategoryDto> getIndicatorsWithTrendsUnderUser(UserEntity user, Date startDate, Date endDate, Integer areaId, Integer frequencyId) {
         Set<TrendIndicatorCategoryDto> categories = new LinkedHashSet<>();
         Set<IndicatorCategoryEntity> indicatorCategories = getAllIndicatorCategories();
         AreaEntity area = areaService.getAreaById(areaId);
 
         for (IndicatorCategoryEntity indicatorCategory: indicatorCategories) {
             TrendIndicatorCategoryDto trendCategory = new TrendIndicatorCategoryDto(indicatorCategory.getName());
             categories.add(trendCategory);
             for (IndicatorEntity indicator: indicatorCategory.getIndicators()) {
 
                 if (isIndicatorAccessibleForUser(indicator, user) && indicator.getTrend() != null) {
                     IndicatorWithTrendDto trendIndicator = new IndicatorWithTrendDto(indicator,
                             getTrendForIndicator(area, indicator, frequencyId, startDate, endDate));
                     trendCategory.getIndicators().add(trendIndicator);
                 }
             }
         }
         return categories;
     }
 
     private boolean isIndicatorAccessibleForUser(IndicatorEntity indicatorEntity, UserEntity userEntity) {
         return userEntity.equals(indicatorEntity.getOwner()) ||
                 hasIndicatorCommonRoleWithUser(indicatorEntity, userEntity) && userEntity.getArea().getLevel().getId() <= indicatorEntity.getAreaLevel().getId();
     }
 
     private boolean hasIndicatorCommonRoleWithUser(IndicatorEntity indicatorEntity, UserEntity userEntity) {
         return !Collections.disjoint(indicatorEntity.getRoles(), userEntity.getRoles()) || userEntity.getRoles().contains(userService.getRoleById(ADMIN_ROLE_ID)) || userEntity.getRoles().contains(userService.getRoleById(READ_ONLY_ROLE_ID));
     }
 
     @Override
     public Map<AreaEntity, Integer> getIndicatorTrendForChildAreas(Integer indicatorId, Integer parentAreaId, Integer frequencyId, Date startDate, Date endDate) {
         IndicatorEntity indicator = indicatorDao.getById(indicatorId);
 
         if (indicator.getTrend() == null) {
             throw new IllegalArgumentException("Cannot calculate trend value for indicator with null trend.");
         }
         Set<AreaEntity> areas = areaService.getAreasByParentAreaId(parentAreaId);
 
         Map<AreaEntity, Integer> areasTrends = new LinkedHashMap<>();
         for (AreaEntity area: areas) {
             int trend = getTrendForIndicator(area, indicator, frequencyId, startDate, endDate);
             areasTrends.put(area, trend);
         }
         return areasTrends;
     }
 
     @Override
     public byte[] getCaseListReportAsCsv(IndicatorEntity indicatorEntity, Integer areaId, Date fromDate, Date toDate) {
         try {
             DwQueryEntity numerator = indicatorEntity.getNumerator();
             DwQueryHelper dwQueryHelper = new DwQueryHelper();
             String tableName = numerator.getTableName();
             CaseListReport caseListReport = getCaseListReportFromXml(tableName);
             AreaEntity areaEntity = areaService.getAreaById(areaId);
 
             String sqlString = QueryBuilder.getDwQueryAsSQLString(SQL_DIALECT,
                     schemaName, dwQueryHelper.buildDwQuery(numerator, areaEntity), false);
             if (fromDate != null && toDate != null) {
                 if (fromDate.compareTo(toDate) > 0) {
                     throw new CareRuntimeException("Value of field 'fromDate' must be less or equal to 'toDate'.");
                 }
 
                 sqlString = dwQueryHelper.formatFromDateAndToDate(sqlString, fromDate, toDate);
             }
 
             List<String> fields = new ArrayList<>();
             List<String> headers = new ArrayList<>();
             if (caseListReport.getFields() == null) {
                 return null;
             }
 
             for (ReportField reportField : caseListReport.getFields()) {
                 fields.add(constructCaseListReportFieldName(tableName, reportField));
                 headers.add(reportField.getDisplayName());
             }
 
             sqlString = sqlString.replaceFirst(REPLACE_SELECT_WITH_SELECT_COLUMNS,
                     String.format("select %s from ", StringUtils.join(fields, ", "))
                             + "\"$2\".\"$3\"$4");
 
             JdbcTemplate jdbcTemplate = new JdbcTemplate(careDataSource);
             return csvExportService.convertRowMapToBytes(headers, jdbcTemplate.queryForList(sqlString));
         } catch (Exception e) {
             throw new CareRuntimeException(e);
         }
     }
 
     private String constructCaseListReportFieldName(String tableName, ReportField reportField) {
         return "\"" + tableName + "\".\"" + reportField.getDbName() + "\"";
     }
 
     private CaseListReport getCaseListReportFromXml(String tableName) throws JAXBException, IOException {
         String xmlFilePath = CASE_LIST_REPORT_XML_DIRECTORY + File.separator + tableName + ".xml";
         File caseListXmlFile = new File(xmlFilePath);
 
         if (caseListXmlFile.exists()) {
             return xmlCaseListReportParser.parse(caseListXmlFile);
         } else {
             ClassPathResource caseListReportXmlFile = new ClassPathResource("xml/" + tableName + ".xml");
             return xmlCaseListReportParser.parse(caseListReportXmlFile.getFile());
         }
     }
 
     @Override
     public void calculateIndicator(IndicatorEntity indicatorEntity) {
         Thread thread = new Thread(new IndicatorValuesInitializer(indicatorEntity));
         thread.start();
     }
 
     @Override
     @Transactional(readOnly = false)
     public void calculateAllIndicators(Integer categoryId) {
         Set<IndicatorEntity> indicatorEntities;
 
         if (categoryId != 0) {
             indicatorEntities = indicatorCategoryDao.getById(categoryId).getIndicators();
         } else {
             indicatorEntities = indicatorDao.getAll();
         }
 
         for (IndicatorEntity indicator : indicatorEntities) {
             indicatorValueDao.removeByIndicator(indicator);
             Hibernate.initialize(indicator.getNumerator());
             setComputedForIndicator(indicator, false);
             calculateIndicator(indicator);
         }
     }
 
     @Override
     public IndicatorCreationFormDto getIndicatorCreationFormDto() {
         Set<IndicatorCategoryEntity> categoryEntities = this.getAllIndicatorCategories();
         Set<RoleEntity> roleEntities = userService.getAllRoles();
         Set<ReportTypeEntity> reportTypes = reportService.getAllReportTypes();
         Set<FrequencyEntity> frequencyEntities = cronService.getAllFrequencies();
         Set<LevelEntity> levelEntities = areaService.getAllLevels();
         Set<DwQueryEntity> dwQueries = this.getAllTopLevelDwQueries();
 
         return new IndicatorCreationFormDto(
                 categoryEntities,
                 roleEntities,
                 levelEntities,
                 frequencyEntities,
                 reportTypes,
                 dwQueries);
     }
 
     @Override
     public QueryCreationFormDto getIndicatorQueryCreationFormDto() {
         Set<FormEntity> formEntities = formsService.getAllForms();
 
         return new QueryCreationFormDto(
                 formEntities);
     }
 
     private int getTrendForIndicator(AreaEntity area, IndicatorEntity indicator, Integer frequencyId, Date startDate, Date endDate) {
         FrequencyEntity frequency = cronService.getFrequencyById(frequencyId);
         Date[] dates = DateResolver.resolveDates(frequency, startDate, endDate);
 
         List<IndicatorValueEntity> values = indicatorValueDao.getIndicatorValuesForArea(indicator.getId(), area.getId(),
                 frequencyId, dates[0], dates[1], null);
 
         if (values.size() < 2) {
             return TREND_NEUTRAL;
         }
 
         BigDecimal diff = values.get(values.size() - 1).getValue().subtract(values.get(0).getValue());
 
         if (diff.compareTo(indicator.getTrend().negate()) < 0) {
             return TREND_NEGATIVE;
         } else if (diff.compareTo(indicator.getTrend()) > 0) {
             return TREND_POSITIVE;
         }
 
         return TREND_NEUTRAL;
     }
 
 }
