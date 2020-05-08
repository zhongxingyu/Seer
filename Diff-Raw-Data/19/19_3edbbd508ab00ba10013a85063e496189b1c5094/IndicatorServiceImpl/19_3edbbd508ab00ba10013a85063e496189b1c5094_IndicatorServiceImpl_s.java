 package org.motechproject.carereporting.service.impl;
 
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.motechproject.carereporting.dao.IndicatorCategoryDao;
 import org.motechproject.carereporting.dao.IndicatorDao;
 import org.motechproject.carereporting.dao.IndicatorTypeDao;
 import org.motechproject.carereporting.dao.IndicatorValueDao;
 import org.motechproject.carereporting.domain.AreaEntity;
 import org.motechproject.carereporting.domain.ComplexConditionEntity;
 import org.motechproject.carereporting.domain.ComputedFieldEntity;
 import org.motechproject.carereporting.domain.DashboardEntity;
 import org.motechproject.carereporting.domain.IndicatorCategoryEntity;
 import org.motechproject.carereporting.domain.IndicatorEntity;
 import org.motechproject.carereporting.domain.IndicatorTypeEntity;
 import org.motechproject.carereporting.domain.IndicatorValueEntity;
 import org.motechproject.carereporting.domain.ReportEntity;
 import org.motechproject.carereporting.domain.UserEntity;
 import org.motechproject.carereporting.domain.forms.IndicatorFormObject;
 import org.motechproject.carereporting.domain.forms.IndicatorWithTrend;
 import org.motechproject.carereporting.domain.forms.TrendIndicatorCategory;
 import org.motechproject.carereporting.service.AreaService;
 import org.motechproject.carereporting.service.ComplexConditionService;
 import org.motechproject.carereporting.service.ComputedFieldService;
 import org.motechproject.carereporting.service.DashboardService;
 import org.motechproject.carereporting.service.IndicatorService;
 import org.motechproject.carereporting.service.ReportService;
 import org.motechproject.carereporting.service.UserService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 @Service
 @Transactional(readOnly = true)
 public class IndicatorServiceImpl extends AbstractService implements IndicatorService {
 
     private static final int TREND_NEUTRAL = 0;
     private static final int TREND_NEGATIVE = -1;
     private static final int TREND_POSITIVE = 1;
 
     @Autowired
     private IndicatorDao indicatorDao;
 
     @Autowired
     private IndicatorTypeDao indicatorTypeDao;
 
     @Autowired
     private IndicatorCategoryDao indicatorCategoryDao;
 
     @Autowired
     private IndicatorValueDao indicatorValueDao;
 
     @Autowired
     private AreaService areaService;
 
     @Autowired
     private ComplexConditionService complexConditionService;
 
     @Autowired
     private ComputedFieldService computedFieldService;
 
     @Autowired
     private DashboardService dashboardService;
 
     @Autowired
     private UserService userService;
 
     @Autowired
     private ReportService reportService;
 
     @Autowired
     private SessionFactory sessionFactory;
 
     public void setIndicatorDao(IndicatorDao indicatorDao) {
         this.indicatorDao = indicatorDao;
     }
 
     public void setIndicatorTypeDao(IndicatorTypeDao indicatorTypeDao) {
         this.indicatorTypeDao = indicatorTypeDao;
     }
 
     public void setIndicatorCategoryDao(IndicatorCategoryDao indicatorCategoryDao) {
         this.indicatorCategoryDao = indicatorCategoryDao;
     }
 
     public void setIndicatorValueDao(IndicatorValueDao indicatorValueDao) {
         this.indicatorValueDao = indicatorValueDao;
     }
 // IndicatorEntity
 
     @Transactional
     public Set<IndicatorEntity> findAllIndicators() {
         return indicatorDao.findAll();
     }
 
     private Set<AreaEntity> getAllChildEntities(AreaEntity areaEntity) {
         Set<AreaEntity> areas = new LinkedHashSet<>();
 
         areas.add(areaEntity);
         for (AreaEntity area : areaEntity.getChildAreas()) {
             areas.addAll(getAllChildEntities(area));
         }
 
         return areas;
     }
 
     @Transactional
     @Override
     public Set<IndicatorEntity> findAllIndicatorsUnderUserArea(Integer areaId) {
         AreaEntity areaEntity = areaService.findAreaById(areaId);
         Set<AreaEntity> areaEntities = getAllChildEntities(areaEntity);
 
         List<Integer> areaIds = new ArrayList<>();
         for (AreaEntity area : areaEntities) {
             areaIds.add(area.getId());
         }
 
         Query query = sessionFactory.getCurrentSession()
                 .createQuery("from IndicatorEntity where area.id in (:areaEntities)");
         query.setParameterList("areaEntities", areaIds);
 
         return new LinkedHashSet<IndicatorEntity>(query.list());
     }
 
     @Transactional
     @Override
     public IndicatorEntity findIndicatorById(Integer id) {
         return indicatorDao.findById(id);
     }
 
     @Transactional(readOnly = false)
     @Override
     public void createNewIndicator(IndicatorEntity indicatorEntity) {
         indicatorDao.save(indicatorEntity);
     }
 
     @Transactional(readOnly = false)
     @Override
     public void createNewIndicatorFromFormObject(IndicatorFormObject indicatorFormObject) {
 
         IndicatorEntity indicatorEntity = new IndicatorEntity(
                 findIndicatorTypeEntityFromFormObject(indicatorFormObject),
                 findIndicatorCategoryEntitiesFromFormObject(indicatorFormObject),
                 findAreaEntityFromFormObject(indicatorFormObject),
                 findUserEntitiesFromFormObject(indicatorFormObject),
                 findComputedFieldEntityFromFormObject(indicatorFormObject),
                 findComplexConditionEntityFromFormObject(indicatorFormObject),
                 findIndicatorValueEntitiesFromFormObject(indicatorFormObject),
                 indicatorFormObject.getReports(),
                 indicatorFormObject.getFrequency(),
                 indicatorFormObject.getName());
 
         indicatorEntity.setTrend(indicatorFormObject.getTrend());
         indicatorDao.save(indicatorEntity);
     }
 
     @Transactional(readOnly = false)
     @Override
     public void updateIndicator(IndicatorEntity indicatorEntity) {
         indicatorDao.update(indicatorEntity);
     }
 
     @Transactional(readOnly = false)
     @Override
     public void updateIndicatorFromFormObject(IndicatorFormObject indicatorFormObject) {
         IndicatorEntity indicatorEntity = this.findIndicatorById(indicatorFormObject.getId());
 
         indicatorEntity.setIndicatorType(findIndicatorTypeEntityFromFormObject(indicatorFormObject));
         indicatorEntity.setCategories(findIndicatorCategoryEntitiesFromFormObject(indicatorFormObject));
         indicatorEntity.setArea(findAreaEntityFromFormObject(indicatorFormObject));
         indicatorEntity.setOwners(findUserEntitiesFromFormObject(indicatorFormObject));
         indicatorEntity.setComputedField(findComputedFieldEntityFromFormObject(indicatorFormObject));
         indicatorEntity.setComplexCondition(findComplexConditionEntityFromFormObject(indicatorFormObject));
         indicatorEntity.setValues(findIndicatorValueEntitiesFromFormObject(indicatorFormObject));
         indicatorEntity.setReports(setUpdatedReports(indicatorFormObject.getReports(), indicatorEntity));
         indicatorEntity.setFrequency(indicatorFormObject.getFrequency());
         indicatorEntity.setName(indicatorFormObject.getName());
         indicatorDao.update(indicatorEntity);
     }
 
 
     private Set<ReportEntity> setUpdatedReports(Set<ReportEntity> reportsFromFormObject, IndicatorEntity indicatorEntity) {
         Set<ReportEntity> reportsUpdated = new HashSet<>();
         Set<ReportEntity> reportsToUpdate = indicatorEntity.getReports();
         for (ReportEntity reportForm : reportsFromFormObject) {
             if(reportForm.getId() == null){
                 reportForm.setIndicator(indicatorEntity);
                 reportsUpdated.add(reportForm);
                 continue;
             }
             for (ReportEntity reportToUpdate : reportsToUpdate) {
                 if (reportForm.getId() != null && reportForm.getId().equals(reportToUpdate.getId())) {
                     reportToUpdate.setReportType(reportForm.getReportType());
                     reportsUpdated.add(reportToUpdate);
                     break;
                 }
             }
         }
         reportsToUpdate.removeAll(reportsUpdated);
         reportService.deleteReportSet(reportsToUpdate);
         return reportsUpdated;
     }
 
     private IndicatorTypeEntity findIndicatorTypeEntityFromFormObject(IndicatorFormObject indicatorFormObject) {
         return findIndicatorTypeById(indicatorFormObject.getIndicatorType());
     }
 
     private Set<IndicatorCategoryEntity> findIndicatorCategoryEntitiesFromFormObject(
             IndicatorFormObject indicatorFormObject) {
         Set<IndicatorCategoryEntity> indicatorCategoryEntities = new LinkedHashSet<>();
 
         for (Integer indicatorCategoryId : indicatorFormObject.getCategories()) {
             IndicatorCategoryEntity indicatorCategoryEntity = this.findIndicatorCategoryById(indicatorCategoryId);
 
             indicatorCategoryEntities.add(indicatorCategoryEntity);
         }
 
         return indicatorCategoryEntities;
     }
 
     private AreaEntity findAreaEntityFromFormObject(IndicatorFormObject indicatorFormObject) {
         return areaService.findAreaById(indicatorFormObject.getArea());
     }
 
     private Set<UserEntity> findUserEntitiesFromFormObject(IndicatorFormObject indicatorFormObject) {
         Set<UserEntity> userEntities = new LinkedHashSet<>();
 
         for (Integer ownerId : indicatorFormObject.getOwners()) {
             UserEntity userEntity = userService.findUserById(ownerId);
 
             userEntities.add(userEntity);
         }
 
         return userEntities;
     }
 
     private ComputedFieldEntity findComputedFieldEntityFromFormObject(
             IndicatorFormObject indicatorFormObject) {
         return computedFieldService.findComputedFieldById(indicatorFormObject.getComputedField());
     }
 
     private ComplexConditionEntity findComplexConditionEntityFromFormObject(
             IndicatorFormObject indicatorFormObject) {
         if (indicatorFormObject.getComplexCondition() == null) {
             return null;
         }
 
         return complexConditionService.findComplexConditionById(indicatorFormObject.getComplexCondition());
     }
 
     private Set<IndicatorValueEntity> findIndicatorValueEntitiesFromFormObject(
             IndicatorFormObject indicatorFormObject) {
         Set<IndicatorValueEntity> indicatorValueEntities = new LinkedHashSet<>();
 
         for (Integer valueId : indicatorFormObject.getValues()) {
             IndicatorValueEntity indicatorValueEntity = this.findIndicatorValueById(valueId);
 
             indicatorValueEntities.add(indicatorValueEntity);
         }
 
         return indicatorValueEntities;
     }
 
     @Transactional(readOnly = false)
     @Override
     public void deleteIndicator(IndicatorEntity indicatorEntity) {
         indicatorDao.remove(indicatorEntity);
     }
 
     // IndicatorTypeEntity
 
     @Transactional
     @Override
     public Set<IndicatorTypeEntity> findAllIndicatorTypes() {
         return indicatorTypeDao.findAll();
     }
 
     @Transactional
     @Override
     public IndicatorTypeEntity findIndicatorTypeById(Integer id) {
         return indicatorTypeDao.findById(id);
     }
 
     @Transactional(readOnly = false)
     @Override
     public void createNewIndicatorType(IndicatorTypeEntity indicatorTypeEntity) {
         indicatorTypeDao.save(indicatorTypeEntity);
     }
 
     @Transactional(readOnly = false)
     @Override
     public void updateIndicatorType(IndicatorTypeEntity indicatorTypeEntity) {
         indicatorTypeDao.update(indicatorTypeEntity);
     }
 
     @Transactional(readOnly = false)
     @Override
     public void deleteIndicatorType(IndicatorTypeEntity indicatorTypeEntity) {
         indicatorTypeDao.remove(indicatorTypeEntity);
     }
 
     // IndicatorCategoryEntity
 
     @Transactional
     @Override
     public Set<IndicatorCategoryEntity> findAllIndicatorCategories() {
         return indicatorCategoryDao.findAll();
     }
 
     @Transactional
     @Override
     public IndicatorCategoryEntity findIndicatorCategoryById(Integer id) {
         return indicatorCategoryDao.findById(id);
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
         Set<UserEntity> dashboardOwners = new HashSet<UserEntity>();
         dashboardOwners.add(userService.findCurrentlyLoggedUser());
         return new DashboardEntity(name, newDashboardTabPosition, dashboardOwners);
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
         indicatorCategoryDao.remove(indicatorCategoryEntity);
     }
 
     @Transactional
     @Override
     public Set<IndicatorValueEntity> findAllIndicatorValues() {
         return indicatorValueDao.findAll();
     }
 
     // IndicatorValueEntity
 
     @Transactional
     @Override
     public IndicatorValueEntity findIndicatorValueById(Integer id) {
         return indicatorValueDao.findById(id);
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
     public List<IndicatorValueEntity> findIndicatorValuesForArea(Integer indicatorId, Integer areaId, Date earliestDate) {
         return indicatorValueDao.findIndicatorValuesForArea(indicatorId, areaId, earliestDate);
     }
 
     @Override
     @Transactional
     public Set<TrendIndicatorCategory> getIndicatorsWithTrendsUnderUser(UserEntity user, Date startDate, Date endDate) {
         Set<TrendIndicatorCategory> categories = new LinkedHashSet<>();
         Set<IndicatorCategoryEntity> indicatorCategories = findAllIndicatorCategories();
         for (IndicatorCategoryEntity indicatorCategory: indicatorCategories) {
             TrendIndicatorCategory trendCategory = new TrendIndicatorCategory(indicatorCategory.getName());
             categories.add(trendCategory);
             for (IndicatorEntity indicator: indicatorCategory.getIndicators()) {
                 if (indicator.getOwners().contains(user)) {
                     IndicatorWithTrend trendIndicator = new IndicatorWithTrend(indicator,
                             getTrendForIndicator(user.getArea(), indicator, startDate, endDate));
                     trendCategory.getIndicators().add(trendIndicator);
                 }
             }
         }
         return categories;
     }
 
     private int getTrendForIndicator(AreaEntity area, IndicatorEntity indicator, Date startDate, Date endDate) {
 
         IndicatorValueEntity startValue = indicatorValueDao.getIndicatorValueClosestToDate(area, indicator, startDate);
         IndicatorValueEntity endValue = indicatorValueDao.getIndicatorValueClosestToDate(area, indicator, endDate);
 
         if (startValue == null || endValue == null) {
             return TREND_NEUTRAL;
         }
 
         BigDecimal diff = endValue.getValue().subtract(startValue.getValue());
 
         if (diff.compareTo(indicator.getTrend().getNegativeDiff()) < 0) {
             return TREND_NEGATIVE;
         } else if (diff.compareTo(indicator.getTrend().getPositiveDiff()) > 0) {
             return TREND_POSITIVE;
         }
         return TREND_NEUTRAL;
     }
 
 }
