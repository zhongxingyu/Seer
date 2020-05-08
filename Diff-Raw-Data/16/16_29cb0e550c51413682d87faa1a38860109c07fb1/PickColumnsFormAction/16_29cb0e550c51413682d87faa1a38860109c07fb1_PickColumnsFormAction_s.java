 /**************************************************************************************************
  * This file is part of [SpringAtom] Copyright [kornicameister@gmail.com][2014]                   *
  *                                                                                                *
  * [SpringAtom] is free software: you can redistribute it and/or modify                           *
  * it under the terms of the GNU General Public License as published by                           *
  * the Free Software Foundation, either version 3 of the License, or                              *
  * (at your option) any later version.                                                            *
  *                                                                                                *
  * [SpringAtom] is distributed in the hope that it will be useful,                                *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of                                 *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                  *
  * GNU General Public License for more details.                                                   *
  *                                                                                                *
  * You should have received a copy of the GNU General Public License                              *
  * along with [SpringAtom].  If not, see <http://www.gnu.org/licenses/gpl.html>.                  *
  **************************************************************************************************/
 
 package org.agatom.springatom.web.flows.wizards.wizard.rbuilder.actions;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import org.agatom.springatom.web.flows.wizards.actions.WizardAction;
 import org.agatom.springatom.web.locale.SMessageSource;
 import org.agatom.springatom.web.rbuilder.ReportConfiguration;
 import org.agatom.springatom.web.rbuilder.bean.RBuilderBean;
 import org.agatom.springatom.web.rbuilder.bean.RBuilderColumn;
 import org.agatom.springatom.web.rbuilder.bean.RBuilderEntity;
 import org.agatom.springatom.web.rbuilder.support.RColumnRenderClass;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.i18n.LocaleContextHolder;
 import org.springframework.core.convert.TypeDescriptor;
 import org.springframework.core.convert.converter.Converter;
 import org.springframework.format.support.FormattingConversionService;
 import org.springframework.util.Assert;
 import org.springframework.util.ClassUtils;
 import org.springframework.util.StringUtils;
 import org.springframework.validation.Errors;
 import org.springframework.validation.Validator;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.webflow.execution.Event;
 import org.springframework.webflow.execution.RequestContext;
 
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 
 @WizardAction(value = "pickColumnsFormAction")
 public class PickColumnsFormAction
         extends ReportWizardFormAction {
     private static final Logger LOGGER           = Logger.getLogger(PickColumnsFormAction.class);
     private static final String ENTITY_TO_COLUMN = "entityToColumn";
     private static final String RENDER_PROP      = "colToRenderProp";
 
     @Autowired(required = false)
     private SMessageSource messageSource      = null;
     @Value("#{rbuilderProperties['reports.objects.conversion.types']}")
     private String         rawConversionTypes = null;
     @Value("#{webProperties['sa.delimiter']}")
     private String         valueDelimiter     = null;
     private Set<Class<?>>  conversionClasses  = null;
 
     public PickColumnsFormAction() {
         super();
         this.setValidator(new AreColumnsSelectedForAllEntitiesValidator());
     }
 
     @Override
     public void afterPropertiesSet() throws Exception {
         super.afterPropertiesSet();
         final Set<String> strings = StringUtils.commaDelimitedListToSet(this.rawConversionTypes);
         final Set<Class<?>> conversionClasses = Sets.newHashSetWithExpectedSize(strings.size());
         for (String clazzName : strings) {
             clazzName = StringUtils.trimAllWhitespace(clazzName);
             final Class<?> aClass = ClassUtils.resolveClassName(clazzName, this.getClass().getClassLoader());
             if (aClass != null) {
                 conversionClasses.add(aClass);
             }
         }
         this.conversionClasses = conversionClasses;
     }
 
     @Override
     public Event setupForm(final RequestContext context) throws Exception {
         context.getViewScope().put(ENTITY_TO_COLUMN, this.reportWizard.getEntityToColumnForReport());
         context.getViewScope().put(RENDER_PROP, this.getColumnsRenderProperties(this.reportWizard.getReportConfiguration()));
         return super.setupForm(context);
     }
 
     @Override
     protected WebDataBinder doInitBinder(final WebDataBinder binder, final FormattingConversionService conversionService) {
         binder.setIgnoreUnknownFields(true);
         conversionService.addConverter(new StringArrayToReportColumnListConverter());
         conversionService.addConverter(new StringToReportColumnListConverter());
         return binder;
     }
 
     @Override
     public Event resetForm(final RequestContext context) throws Exception {
         final ReportConfiguration report = this.reportWizard.getReportConfiguration();
         Assert.notNull(report);
         report.clearColumns();
         return success();
     }
 
     // TODO add support for rendering given column as the concatenated report
     private Object getColumnsRenderProperties(final ReportConfiguration cfg) {
         final Map<Integer, List<RColumnRenderClass>> map = Maps.newHashMap();
         final List<RBuilderEntity> entities = cfg.getEntities();
         final Locale locale = LocaleContextHolder.getLocale();
 
         for (final RBuilderEntity entity : entities) {
             final Set<RBuilderColumn> columns = entity.getColumns();
             Assert.notEmpty(columns);
 
             for (final RBuilderColumn column : columns) {
                 final Class<?> sourceType = column.getColumnClass();
                 final List<RColumnRenderClass> properties = Lists.newArrayList();
 
                 for (final Class<?> targetType : this.conversionClasses) {
                     if (this.canConvert(sourceType, targetType)) {
                         properties.add(new RColumnRenderClass(
                                 sourceType,
                                 targetType,
                                 this.messageSource.getMessage(targetType, locale).getName()
                         ));
                     }
                 }
 
                 map.put(column.getId(), properties);
             }
         }
 
         return map;
     }
 
     private boolean canConvert(final Class<?> sourceType, final Class<?> targetType) {
         final boolean springConversionPossible = this.conversionService.canConvert(sourceType, targetType);
        return !(springConversionPossible && this.isConversionBetweenCollectionAndBasic(sourceType, targetType)) && springConversionPossible;
     }
 
     private boolean isConversionBetweenCollectionAndBasic(final Class<?> sourceType, final Class<?> targetType) {
         final boolean leftIsCollectionRightNot = this.isCollectionLike(sourceType) && !this.isCollectionLike(targetType);
         final boolean leftIsNotCollectionRightIs = !this.isCollectionLike(sourceType) && this.isCollectionLike(targetType);
         return leftIsCollectionRightNot || leftIsNotCollectionRightIs;
     }
 
     private boolean isCollectionLike(final Class<?> sourceType) {
         return ClassUtils.isAssignable(Iterable.class, sourceType) || ClassUtils.isAssignable(Map.class, sourceType);
     }
 
     private abstract class BaseConverter
             extends MatcherConverter {
         protected Set<RBuilderColumn> doConvert(final Set<String> list) {
             LOGGER.trace(String.format("converting with selected clazz=%s", list));
             Preconditions.checkNotNull(list);
             Preconditions.checkArgument(!list.isEmpty());
             final Set<RBuilderColumn> reportedColumns = Sets.newTreeSet();
             for (final String javaClassName : list) {
                 final RBuilderBean bean = reportWizard.getReportableBean(Integer.valueOf(javaClassName));
                 if (ClassUtils.isAssignable(RBuilderColumn.class, bean.getClass())) {
                     reportedColumns.add((RBuilderColumn) bean);
                 }
             }
             return reportedColumns;
         }
     }
 
     private class StringArrayToReportColumnListConverter
             extends BaseConverter
             implements Converter<String[], Set<RBuilderColumn>> {
 
         @Override
         public Set<RBuilderColumn> convert(final String[] attributes) {
             return this.doConvert(Sets.newHashSet(attributes));
         }
 
         @Override
         public boolean matches(final TypeDescriptor sourceType, final TypeDescriptor targetType) {
             final Class<?> sourceTypeClass = sourceType.getType();
             return ClassUtils.isAssignable(String[].class, sourceTypeClass)
                     && ClassUtils.isAssignable(Set.class, targetType.getType());
         }
     }
 
     private class StringToReportColumnListConverter
             extends BaseConverter
             implements Converter<String, Set<RBuilderColumn>> {
 
         @Override
         public Set<RBuilderColumn> convert(final String clazz) {
             return this.doConvert(Sets.newHashSet(clazz));
         }
 
         @Override
         public boolean matches(final TypeDescriptor sourceType, final TypeDescriptor targetType) {
             final Class<?> sourceTypeClass = sourceType.getType();
             return ClassUtils.isAssignable(String.class, sourceTypeClass)
                     && ClassUtils.isAssignable(Set.class, targetType.getType());
         }
     }
 
     private class AreColumnsSelectedForAllEntitiesValidator
             implements Validator {
         @Override
         public boolean supports(final Class<?> clazz) {
             return ClassUtils.isAssignable(ReportConfiguration.class, clazz);
         }
 
         @Override
         public void validate(final Object target, final Errors errors) {
             Preconditions.checkNotNull(target, "Target must not be null");
             Preconditions.checkArgument(ClassUtils.isAssignable(ReportConfiguration.class, target.getClass()));
 
             final String shortName = ClassUtils.getShortName(AreColumnsSelectedForAllEntitiesValidator.class);
             final ReportConfiguration targetReport = (ReportConfiguration) target;
             boolean hasNoErrors = true;
 
             if (!targetReport.hasEntities()) {
                 errors.rejectValue("entities", "wizard.NewReportWizard.error.noEntitiesSelected");
                 hasNoErrors = false;
             }
             if (hasNoErrors) {
                 for (final RBuilderEntity entity : targetReport.getEntities()) {
                     if (!targetReport.hasColumn(entity)) {
                         errors.rejectValue("entities", "wizard.NewReportWizard.error.noColumnsSelectedForEntity", new Object[]{entity
                                 .getName()}, null);
                     }
                 }
             }
             LOGGER.trace(String.format("%s validated target=%s...valid=%s",
                     shortName,
                     target,
                     errors.hasErrors() ? "true" : "false"
             ));
         }
     }
 }
