 package org.motechproject.carereporting.indicator;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.text.StrSubstitutor;
 import org.dwQueryBuilder.builders.ComputedColumnBuilder;
 import org.dwQueryBuilder.builders.DwQueryBuilder;
 import org.dwQueryBuilder.builders.DwQueryCombinationBuilder;
 import org.dwQueryBuilder.builders.GroupByConditionBuilder;
 import org.dwQueryBuilder.builders.HavingConditionBuilder;
 import org.dwQueryBuilder.builders.SelectColumnBuilder;
 import org.dwQueryBuilder.builders.WhereConditionBuilder;
 import org.dwQueryBuilder.builders.WhereConditionGroupBuilder;
 import org.dwQueryBuilder.builders.steps.ComputedColumnBuilderOperationStep;
 import org.dwQueryBuilder.data.ComputedColumn;
 import org.dwQueryBuilder.data.DwQuery;
 import org.dwQueryBuilder.data.DwQueryCombination;
 import org.dwQueryBuilder.data.GroupBy;
 import org.dwQueryBuilder.data.conditions.where.WhereCondition;
 import org.dwQueryBuilder.data.enums.CombineType;
 import org.dwQueryBuilder.data.enums.ComparisonType;
 import org.dwQueryBuilder.data.enums.OperatorType;
 import org.dwQueryBuilder.data.enums.SelectColumnFunctionType;
 import org.dwQueryBuilder.data.enums.WhereConditionJoinType;
 import org.motechproject.carereporting.domain.AreaEntity;
 import org.motechproject.carereporting.domain.CalculationEndDateConditionEntity;
 import org.motechproject.carereporting.domain.CombinationEntity;
 import org.motechproject.carereporting.domain.ComputedFieldEntity;
 import org.motechproject.carereporting.domain.ConditionEntity;
 import org.motechproject.carereporting.domain.DateDiffComparisonConditionEntity;
 import org.motechproject.carereporting.domain.DateRangeComparisonConditionEntity;
 import org.motechproject.carereporting.domain.DateValueComparisonConditionEntity;
 import org.motechproject.carereporting.domain.DwQueryEntity;
 import org.motechproject.carereporting.domain.EnumRangeComparisonConditionEntity;
 import org.motechproject.carereporting.domain.EnumRangeComparisonConditionValueEntity;
 import org.motechproject.carereporting.domain.FieldOperationEntity;
 import org.motechproject.carereporting.domain.GroupedByEntity;
 import org.motechproject.carereporting.domain.HavingEntity;
 import org.motechproject.carereporting.domain.PeriodConditionEntity;
 import org.motechproject.carereporting.domain.SelectColumnEntity;
 import org.motechproject.carereporting.domain.ValueComparisonConditionEntity;
 import org.motechproject.carereporting.domain.WhereGroupEntity;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class DwQueryHelper {
 
     private static final int SECONDS_PER_DAY = 86_400;
 
     public DwQuery buildDwQuery(DwQueryEntity dwQueryEntity, AreaEntity area) {
         DwQuery dwQuery = buildDwQuery(dwQueryEntity);
         addAreaJoinAndCondition(dwQuery, area);
         return dwQuery;
     }
 
     public String formatFromDateAndToDate(String query, Date from, Date to) {
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
         Map<String, String> params = new HashMap<>();
         params.put("fromDate", dateFormat.format(from));
         params.put("toDate", dateFormat.format(to));
         return formatNamesParams(query, params);
     }
 
     private String formatNamesParams(String strToFormat, Map<String, String> params) {
         StrSubstitutor sub = new StrSubstitutor(params, "%(", ")");
         return sub.replace(strToFormat);
     }
 
     private DwQuery buildDwQuery(DwQueryEntity dwQueryEntity) {
         DwQueryBuilder dwQueryBuilder = new DwQueryBuilder();
         if (dwQueryEntity.getCombination() != null) {
             dwQueryBuilder.withCombination(prepareCombination(dwQueryEntity.getCombination()));
         }
         if (dwQueryEntity.getGroupedBy() != null) {
             dwQueryBuilder.withGroupBy(prepareGroupBy(dwQueryEntity.getGroupedBy()));
         }
         for (SelectColumnEntity selectColumn: dwQueryEntity.getSelectColumns()) {
             dwQueryBuilder.withSelectColumn(prepareSelectColumn(selectColumn, dwQueryEntity.getTableName(), null));
         }
         dwQueryBuilder.withTableName(dwQueryEntity.getTableName());
         if (dwQueryEntity.getWhereGroup() != null) {
             dwQueryBuilder.withWhereConditionGroup(prepareWhereConditionGroup(dwQueryEntity.getWhereGroup()));
         }
         return dwQueryBuilder.build();
     }
 
     private GroupBy prepareGroupBy(GroupedByEntity groupByEntity) {
        return new GroupByConditionBuilder()
                .withField(groupByEntity.getTableName(), groupByEntity.getFieldName())
                .withHaving(prepareHaving(groupByEntity.getHaving(), groupByEntity.getTableName(), groupByEntity.getFieldName()))
                .build();
     }
 
     private HavingConditionBuilder prepareHaving(HavingEntity havingEntity, String tableName, String fieldName) {
         return new HavingConditionBuilder()
                 .withComparison(ComparisonType.fromSymbol(havingEntity.getOperator()), havingEntity.getValue())
                 .withSelectColumn(prepareSelectColumn(havingEntity.getSelectColumnEntity(), tableName, fieldName));
     }
 
     private SelectColumnBuilder prepareSelectColumn(SelectColumnEntity selectColumnEntity, String defaultTableName, String defaultFieldName) {
         SelectColumnBuilder builder;
         if (selectColumnEntity.getComputedField() == null) {
             builder = new SelectColumnBuilder().withColumn(defaultTableName, defaultFieldName != null ? defaultFieldName : "*");
         } else {
             builder = new SelectColumnBuilder()
                 .withColumn(prepareComputedColumn(selectColumnEntity.getComputedField()));
         }
         if (StringUtils.isNotEmpty(selectColumnEntity.getFunctionName())) {
             builder.withFunction(
                     SelectColumnFunctionType.valueOf(selectColumnEntity.getFunctionName()));
         }
         return builder;
     }
 
     private DwQueryCombinationBuilder prepareCombination(CombinationEntity combinationEntity) {
         return new DwQueryCombinationBuilder()
                 .withCombineType(CombineType.valueOf(combinationEntity.getType()))
                 .withDwQuery(buildDwQuery(combinationEntity.getDwQuery()))
                 .withKeys(combinationEntity.getForeignKey(), combinationEntity.getReferencedKey());
     }
 
     private WhereConditionGroupBuilder prepareWhereConditionGroup(WhereGroupEntity whereGroupEntity) {
         WhereConditionGroupBuilder builder = new WhereConditionGroupBuilder();
         for (WhereGroupEntity nestedGroup: whereGroupEntity.getWhereGroups()) {
             builder.withGroup(prepareWhereConditionGroup(nestedGroup));
         }
         for (ConditionEntity condition: whereGroupEntity.getConditions()) {
             addCondition(builder, condition);
         }
         if (whereGroupEntity.getOperator() != null) {
             builder.withJoinType(WhereConditionJoinType.valueOf(whereGroupEntity.getOperator()));
         }
         return builder;
     }
 
     private void addCondition(WhereConditionGroupBuilder builder, ConditionEntity condition) {
         if (condition instanceof ValueComparisonConditionEntity) {
             builder.withCondition(prepareValueComparisonCondition((ValueComparisonConditionEntity) condition));
         } else if (condition instanceof DateDiffComparisonConditionEntity) {
             builder.withCondition(prepareDateDiffComparisonCondition((DateDiffComparisonConditionEntity) condition));
         } else if (condition instanceof DateRangeComparisonConditionEntity) {
             builder.withCondition(prepareDateRangeComparisonCondition((DateRangeComparisonConditionEntity) condition));
         } else if (condition instanceof DateValueComparisonConditionEntity) {
             builder.withCondition(prepareDateValueComparisonCondition((DateValueComparisonConditionEntity) condition));
         } else if (condition instanceof EnumRangeComparisonConditionEntity) {
             builder.withCondition(prepareEnumRangeComparisonCondition((EnumRangeComparisonConditionEntity) condition));
         } else if (condition instanceof PeriodConditionEntity) {
             PeriodConditionEntity periodCondition = (PeriodConditionEntity) condition;
             if (periodCondition.getOffset() != null) {
                 builder.withCondition(preparePeriodConditionFromDate(periodCondition));
                 builder.withCondition(preparePeriodConditionToDate(periodCondition));
             } else {
                 builder.withCondition(prepareDateBetweenCondition(periodCondition));
             }
         } else if (condition instanceof CalculationEndDateConditionEntity) {
             CalculationEndDateConditionEntity conditionEntity = (CalculationEndDateConditionEntity) condition;
             builder.withCondition(prepareCalculationEndDateCondition(conditionEntity));
         } else {
             throw new IllegalArgumentException("Condition type not supported.");
         }
     }
 
     private WhereConditionBuilder prepareValueComparisonCondition(ValueComparisonConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withValueComparison(
                         prepareComputedField(condition.getField1()),
                         ComparisonType.fromSymbol(condition.getOperator().getName()),
                         condition.getValue());
     }
 
     private WhereConditionBuilder prepareDateDiffComparisonCondition(DateDiffComparisonConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withDateDiffComparison(
                         prepareComputedField(condition.getField1()),
                         ComparisonType.fromSymbol(condition.getOperator().getName()),
                         prepareComputedField(condition.getField2()),
                         SECONDS_PER_DAY * condition.getValue(),
                         condition.getOffset1(),
                         condition.getOffset2());
     }
 
     private WhereConditionBuilder prepareDateRangeComparisonCondition(DateRangeComparisonConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withDateRangeComparison(
                         prepareComputedField(condition.getField1()),
                         condition.getDate1().toString(),
                         condition.getDate2().toString(),
                         condition.getOffset1()
                 );
     }
 
     private WhereConditionBuilder prepareDateValueComparisonCondition(DateValueComparisonConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withDateValueComparison(
                         prepareComputedField(condition.getField1()),
                         ComparisonType.fromSymbol(condition.getOperator().getName()),
                         condition.getValue().toString(),
                         condition.getOffset1()
                 );
     }
 
     private WhereConditionBuilder prepareEnumRangeComparisonCondition(EnumRangeComparisonConditionEntity condition) {
         Set<String> values = new LinkedHashSet<>();
         for (EnumRangeComparisonConditionValueEntity value : condition.getValues()) {
             values.add(value.getValue());
         }
 
         return new WhereConditionBuilder()
                 .withEnumRangeComparison(
                         prepareComputedField(condition.getField1()),
                         values
                 );
     }
 
     private WhereConditionBuilder preparePeriodConditionToDate(PeriodConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withDateValueComparison(
                         prepareComputedField(condition.getField1()),
                         ComparisonType.Less,
                         "%(toDate)",
                         condition.getOffset() < 0 ? condition.getOffset() : 0);
     }
 
     private WhereConditionBuilder preparePeriodConditionFromDate(PeriodConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withDateValueComparison(
                         prepareComputedField(condition.getField1()),
                         ComparisonType.GreaterEqual,
                         "%(fromDate)",
                         condition.getOffset() > 0 ? condition.getOffset() : 0);
     }
 
     private WhereConditionBuilder prepareCalculationEndDateCondition(CalculationEndDateConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withDateValueComparison(
                         prepareComputedField(condition.getField1()),
                         ComparisonType.Less,
                         "%(toDate)",
                         (condition.getOffset() == null) ? 0 : condition.getOffset());
     }
 
     private WhereConditionBuilder prepareDateBetweenCondition(PeriodConditionEntity condition) {
         return new WhereConditionBuilder()
                 .withDateRangeComparison(prepareComputedField(condition.getField1()),
                         "%(fromDate)",
                         "%(toDate)");
     }
 
     private SelectColumnBuilder prepareComputedField(ComputedFieldEntity computedFieldEntity) {
         return new SelectColumnBuilder().withColumn(prepareComputedColumn(computedFieldEntity));
     }
 
     private ComputedColumn prepareComputedColumn(ComputedFieldEntity computedFieldEntity) {
         if (computedFieldEntity.isRegularField()) {
             return new ComputedColumnBuilder().withComputedColumn(computedFieldEntity.getForm().getTableName(),
                     computedFieldEntity.getName()).build();
         } else {
             ComputedColumnBuilder builder = new ComputedColumnBuilder();
             Iterator<FieldOperationEntity> operations = computedFieldEntity.getFieldOperations().iterator();
             FieldOperationEntity operation = operations.next();
             ComputedColumnBuilderOperationStep stepBuilder = builder.withComputedColumn(prepareComputedColumn(operation.getField1()))
                     .withComputedColumn(OperatorType.fromName(operation.getOperatorType().getName()),
                             prepareComputedColumn(operation.getField2()));
             while (operations.hasNext()) {
                 operation = operations.next();
                 stepBuilder.withComputedColumn(OperatorType.fromName(operation.getOperatorType().getName()),
                         prepareComputedColumn(operation.getField2()));
 
             }
             return stepBuilder.build();
         }
     }
 
     private void addAreaJoinAndCondition(DwQuery dwQuery, AreaEntity area) {
         DwQueryCombination areaJoinCombination = prepareAreaJoin();
 
         if (dwQuery.getCombineWith() != null) {
             for (DwQueryCombination dwQueryCombination : dwQuery.getCombineWith()) {
                 addAreaJoinAndCondition(dwQueryCombination.getDwQuery(), area);
             }
         } else {
             dwQuery.setCombineWith(new LinkedHashSet<DwQueryCombination>());
         }
 
         dwQuery.getCombineWith().add(areaJoinCombination);
         if (dwQuery.getWhereConditionGroup() == null) {
             dwQuery.setWhereConditionGroup(
                     new WhereConditionGroupBuilder()
                             .withCondition(prepareAreaWhereCondition(area))
                             .build()
             );
         } else {
             if (dwQuery.getWhereConditionGroup().getConditions() == null) {
                 dwQuery.getWhereConditionGroup().setConditions(new LinkedHashSet<WhereCondition>());
             }
 
             dwQuery.getWhereConditionGroup().addCondition(prepareAreaWhereCondition(area));
         }
     }
 
     private DwQueryCombination prepareAreaJoin() {
         return new DwQueryCombinationBuilder()
                 .withKeys("id", "user_id")
                 .withCombineType(CombineType.Join)
                 .withDwQuery(
                         new DwQueryBuilder()
                                 .withSelectColumn(
                                         new SelectColumnBuilder()
                                                 .withColumn("*")
                                 )
                                 .withTableName("flw")
                 )
                 .build();
     }
 
     private WhereCondition prepareAreaWhereCondition(AreaEntity area) {
         return new WhereConditionBuilder()
                 .withValueComparison("flw", area.getLevel().getName(), ComparisonType.Equal, area.getName())
                 .build();
     }
 }
