 /**
  *
  */
 package org.ocha.hdx.service;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.ocha.hdx.config.api2.Constants;
 import org.ocha.hdx.exceptions.apiv2.ApiV2ProcessingException;
 import org.ocha.hdx.model.api2.ApiIndicatorValue;
 import org.ocha.hdx.model.api2.ApiResultWrapper;
 import org.ocha.hdx.model.api2util.IntermediaryIndicatorValue;
 import org.ocha.hdx.model.api2util.RequestParamsWrapper;
 import org.ocha.hdx.model.api2util.RequestParamsWrapper.PeriodType;
 import org.ocha.hdx.model.api2util.RequestParamsWrapper.SortingOption;
 import org.ocha.hdx.persistence.dao.currateddata.IndicatorDAO;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.CollectionUtils;
 
 import com.google.common.collect.ImmutableList;
 
 /**
  * @author alexandru-m-g
  *
  */
 public class IntermediaryBackendServiceImpl implements IntermediaryBackendService {
 	@Autowired
 	private IndicatorDAO indicatorDAO;
 
 //	@Autowired
 //	private IndicatorMaxDateDAO indicatorMaxDateDAO;
 
 	@Override
 	public ApiResultWrapper<Integer> listAvailablePeriods(final RequestParamsWrapper paramsWrapper) {
 		final Integer[] yearPair = this.findYearPeriod(paramsWrapper);
 		final Integer startYear = yearPair[0];
 		final Integer endYear = yearPair[1];
 
 		final List<Integer> periods = this.indicatorDAO.listAvailablePeriods(paramsWrapper.getIndicatorTypeCodes(), paramsWrapper.getSourceCodes(),
 				paramsWrapper.getEntityCodes(), startYear, endYear);
 
 		ApiResultWrapper<Integer> result;
 		if (periods != null) {
 			result = new ApiResultWrapper<>(periods, periods.size(), null, null, null, true, null, false);
 		}
 		else {
 			result = new ApiResultWrapper<>("There was a problem getting the list of periods");
 		}
 
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see org.ocha.hdx.service.IntermediaryBackendService#listIndicatorsByCriteriaWithPagination(java.util.List, java.util.List, java.util.List, java.lang.Integer, java.lang.Integer,
 	 * java.lang.Integer, java.lang.Integer, java.lang.String)
 	 */
 	@Transactional(readOnly = true)
 	@Override
 	public ApiResultWrapper<ApiIndicatorValue> listIndicatorsByCriteriaWithPagination(final RequestParamsWrapper paramsWrapper) throws ApiV2ProcessingException {
 		if (PeriodType.LATEST_YEAR_BY_COUNTRY.equals(paramsWrapper.getPeriodType())) {
 			throw new ApiV2ProcessingException("Period type 'LATEST_YEAR_BY_COUNTRY' doesn't currently support pagination");
 		}
 
 		final Integer[] yearPair = this.findYearPeriod(paramsWrapper);
 		final Integer startYear = yearPair[0];
 		final Integer endYear = yearPair[1];
 
 		final Integer[] paginationParams = this.decidePaginationParameters(paramsWrapper);
 		final Integer maxResults = paginationParams[0];
 		final Integer currentPage = paginationParams[1];
 		final Integer startPosition = currentPage * maxResults;
 
 
 		final String languageCode = paramsWrapper.getLang() != null ? paramsWrapper.getLang() : Constants.DEFAULT_LANGUAGE;
 		final List<IntermediaryIndicatorValue> interimValues = this.indicatorDAO.listIndicatorsByCriteria(paramsWrapper.getIndicatorTypeCodes(), paramsWrapper.getSourceCodes(),
 				paramsWrapper.getEntityCodes(), startYear, endYear, ImmutableList.of(paramsWrapper.getSortingOption()),
 				startPosition, maxResults, languageCode);
 
 		final ApiResultWrapper<ApiIndicatorValue> resultWrapper;
 		if (interimValues != null) {
 			final List<ApiIndicatorValue> values = this.transformAll(interimValues, interimValues.size());
 			final Long totalCount = this.indicatorDAO.countIndicatorsByCriteria(paramsWrapper.getIndicatorTypeCodes(), paramsWrapper.getSourceCodes(), paramsWrapper.getEntityCodes(), startYear,
 					endYear);
 			final Long totalPages = totalCount / maxResults + 1;
 			resultWrapper = new ApiResultWrapper<ApiIndicatorValue>(values, totalCount.intValue(), currentPage, totalPages.intValue(), maxResults, true, "None", currentPage < totalPages);
 		} else {
 			throw new ApiV2ProcessingException("No items could be found");
 		}
 
 		return resultWrapper;
 	}
 
 	/**
 	 * @param paramsWrapper
 	 * @return
 	 */
 	private Integer[] decidePaginationParameters(final RequestParamsWrapper paramsWrapper) {
 		Integer maxResults = paramsWrapper.getPageSize();
 		if (paramsWrapper.getPageSize() == null || paramsWrapper.getPageSize() > Constants.MAX_RESULTS) {
 			maxResults = Constants.MAX_RESULTS;
 		}
 
 		final Integer currentPage = (paramsWrapper.getPageNum() != null ? paramsWrapper.getPageNum() : 0);
 
 		return new Integer[]{maxResults, currentPage};
 	}
 
 	/*
 	 * (non-Javadoc)
 	 *
 	 * @see org.ocha.hdx.service.IntermediaryBackendService#listIndicatorsByCriteria(java.util.List, java.util.List, java.util.List, java.lang.Integer, java.lang.Integer, java.lang.String)
 	 */
 	@Transactional(readOnly = true)
 	@Override
 	public ApiResultWrapper<ApiIndicatorValue> listIndicatorsByCriteria(final RequestParamsWrapper paramsWrapper) throws ApiV2ProcessingException {
 		List<SortingOption> sortingOptions = ImmutableList.of(paramsWrapper.getSortingOption());
 		Integer maxResults = Constants.MAX_RESULTS;
 		if (PeriodType.LATEST_YEAR_BY_COUNTRY.equals(paramsWrapper.getPeriodType()) ) {
 			if	( CollectionUtils.isEmpty(paramsWrapper.getIndicatorTypeCodes())
 					|| CollectionUtils.isEmpty(paramsWrapper.getSourceCodes()) ) {
 				throw new ApiV2ProcessingException("Period type 'LATEST_YEAR_BY_COUNTRY' needs to be specified together with indicatory type and source");
 			}
 			else {
 				sortingOptions = ImmutableList.of(SortingOption.INDICATOR_TYPE_ASC, SortingOption.SOURCE_TYPE_ASC, SortingOption.COUNTRY_ASC);
 				maxResults = Integer.MAX_VALUE-2;
 			}
 
 		}
 
 		final Integer[] yearPair = this.findYearPeriod(paramsWrapper);
 		final Integer startYear = yearPair[0];
 		final Integer endYear = yearPair[1];
 
 		final String languageCode = paramsWrapper.getLang() != null ? paramsWrapper.getLang() : Constants.DEFAULT_LANGUAGE;
 		final List<IntermediaryIndicatorValue> interimValues = this.indicatorDAO.listIndicatorsByCriteria(paramsWrapper.getIndicatorTypeCodes(), paramsWrapper.getSourceCodes(),
 				paramsWrapper.getEntityCodes(), startYear, endYear, sortingOptions, 0, maxResults + 1, languageCode);
 
 		final ApiResultWrapper<ApiIndicatorValue> resultWrapper;
 		if (interimValues != null) {
 			boolean moreResults = false;
 			int limit = interimValues.size();
 			if (interimValues.size() == maxResults + 1) {
 				moreResults = true;
 				limit = interimValues.size() - 1;
 			}
 
 			List<ApiIndicatorValue> values;
 			if (PeriodType.LATEST_YEAR_BY_COUNTRY.equals(paramsWrapper.getPeriodType()) ) {
 				final List<IntermediaryIndicatorValue> filteredInterimValues =
 					this.keepOnlyMaxValues(interimValues, this.getComparator(SortingOption.START_DATE_ASC),limit );
 
 				Collections.sort( filteredInterimValues, this.getComparator(paramsWrapper.getSortingOption()) );
 				values = this.transformAll(filteredInterimValues, filteredInterimValues.size());
 			}
 			else {
 				values = this.transformAll(interimValues, limit);
 			}
 
 			resultWrapper = new ApiResultWrapper<>(values, values.size(), null, null, null, true, "None", moreResults);
 		} else {
 			throw new ApiV2ProcessingException("No items could be found");
 		}
 
 		return resultWrapper;
 
 	}
 
 	private Comparator<IntermediaryIndicatorValue> getComparator(final SortingOption sortingOption) {
 		Comparator<IntermediaryIndicatorValue> c = null;
 		switch (sortingOption) {
 			case COUNTRY_ASC:
 				c = new Comparator<IntermediaryIndicatorValue>() {
 					@Override
 					public int compare(final IntermediaryIndicatorValue o1, final IntermediaryIndicatorValue o2) {
 						return o1.getLocationName().compareTo(o2.getLocationName());
 					}
 				};
 				break;
 			case COUNTRY_DESC:
 				c = new Comparator<IntermediaryIndicatorValue>() {
 					@Override
 					public int compare(final IntermediaryIndicatorValue o1, final IntermediaryIndicatorValue o2) {
 						return o2.getLocationName().compareTo(o1.getLocationName());
 					}
 				};
 				break;
 			case INDICATOR_TYPE_ASC:
 				c = new Comparator<IntermediaryIndicatorValue>() {
 					@Override
 					public int compare(final IntermediaryIndicatorValue o1, final IntermediaryIndicatorValue o2) {
 						return o1.getIndicatorTypeName().compareTo(o2.getIndicatorTypeName());
 					}
 				};
 				break;
 			case INDICATOR_TYPE_DESC:
 				c = new Comparator<IntermediaryIndicatorValue>() {
 					@Override
 					public int compare(final IntermediaryIndicatorValue o1, final IntermediaryIndicatorValue o2) {
 						return o2.getIndicatorTypeName().compareTo(o1.getIndicatorTypeName());
 					}
 				};
 				break;
 			case SOURCE_TYPE_ASC:
 				c = new Comparator<IntermediaryIndicatorValue>() {
 					@Override
 					public int compare(final IntermediaryIndicatorValue o1, final IntermediaryIndicatorValue o2) {
 						return o1.getSourceName().compareTo(o2.getSourceName());
 					}
 				};
 				break;
 			case SOURCE_TYPE_DESC:
 				c = new Comparator<IntermediaryIndicatorValue>() {
 					@Override
 					public int compare(final IntermediaryIndicatorValue o1, final IntermediaryIndicatorValue o2) {
 						return o2.getSourceName().compareTo(o1.getSourceName());
 					}
 				};
 				break;
 			case VALUE_ASC:
 				c = new Comparator<IntermediaryIndicatorValue>() {
 					@Override
 					public int compare(final IntermediaryIndicatorValue o1, final IntermediaryIndicatorValue o2) {
 						final Double d1 = o1.getValue();
 						final Double d2 = o2.getValue();
 						return d1.compareTo(d2);
 					}
 				};
 				break;
 			case VALUE_DESC:
 				c = new Comparator<IntermediaryIndicatorValue>() {
 					@Override
 					public int compare(final IntermediaryIndicatorValue o1, final IntermediaryIndicatorValue o2) {
 						final Double d1 = o1.getValue();
 						final Double d2 = o2.getValue();
 						return d2.compareTo(d1);
 					}
 				};
 				break;
 			case START_DATE_ASC:
 				c = new Comparator<IntermediaryIndicatorValue>() {
 					@Override
 					public int compare(final IntermediaryIndicatorValue o1, final IntermediaryIndicatorValue o2) {
 						return o1.getStartDate().compareTo(o2.getStartDate());
 					}
 				};
 				break;
 			case START_DATE_DESC:
 				c = new Comparator<IntermediaryIndicatorValue>() {
 					@Override
 					public int compare(final IntermediaryIndicatorValue o1, final IntermediaryIndicatorValue o2) {
 						return o2.getStartDate().compareTo(o1.getStartDate());
 					}
 				};
 				break;
 		}
 
 		return c;
 	}
 
 	private Integer[] findYearPeriod(final RequestParamsWrapper paramsWrapper) {
 		Integer startYear = paramsWrapper.getStartYear();
 		Integer endYear = paramsWrapper.getEndYear();
 		if (RequestParamsWrapper.PeriodType.LATEST_YEAR.equals(paramsWrapper.getPeriodType())) {
 			startYear = this.indicatorDAO.latestYearForIndicatorsByCriteria(paramsWrapper.getIndicatorTypeCodes(), paramsWrapper.getSourceCodes(), paramsWrapper.getEntityCodes());
 			endYear = startYear;
 		}
 		return new Integer[] { startYear, endYear };
 	}
 
 	private List<IntermediaryIndicatorValue> keepOnlyMaxValues(final List<IntermediaryIndicatorValue> srcValues, final Comparator<IntermediaryIndicatorValue> comparator, final int limit) {
 		final List<IntermediaryIndicatorValue> result = new ArrayList<>(100);
 
 		String prevIndicatorTypeCode = null;
 		String prevSourceCode = null;
 		String prevEntityCode = null;
 
 		IntermediaryIndicatorValue maxValue = null;
 		for (int i = 0; i < limit; i++) {
 			final IntermediaryIndicatorValue value = srcValues.get(i);
 
 			if ( !value.getIndicatorTypeCode().equals(prevIndicatorTypeCode) ||
 					!value.getSourceCode().equals(prevSourceCode) ||
 					!value.getLocationCode().equals(prevEntityCode) ) {
 
 				if (maxValue != null) {
 					result.add(maxValue);
 					maxValue = null;
 				}
 			}
 
 			prevIndicatorTypeCode = value.getIndicatorTypeCode();
 			prevSourceCode = value.getSourceCode();
 			prevEntityCode = value.getLocationCode();
 
 			if ( maxValue == null || comparator.compare(value, maxValue) > 0 ) {
 				maxValue = value;
 			}
 
 		}
		if ( maxValue != null ) {
			result.add(maxValue);
		}
 
 		return result;
 	}
 
 	private List<ApiIndicatorValue> transformAll(final List<IntermediaryIndicatorValue> interimValues, final int limit) {
 		if (interimValues != null) {
 			final List<ApiIndicatorValue> values = new ArrayList<>(limit);
 
 			for (int i = 0; i<limit; i++ ) {
 				final IntermediaryIndicatorValue interimValue = interimValues.get(i);
 				values.add(this.transform(interimValue));
 			}
 			return values;
 		}
 		return null;
 	}
 
 	private ApiIndicatorValue transform(final IntermediaryIndicatorValue interimValue) {
 		final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
 
 		final ApiIndicatorValue value = new ApiIndicatorValue(interimValue.getValue(), interimValue.getIndicatorTypeCode(), interimValue.getIndicatorTypeName(),
 				interimValue.getUnitCode(), interimValue.getUnitName(), interimValue.getLocationCode(),	interimValue.getLocationName(),
 				interimValue.getSourceCode(), interimValue.getSourceName(), format.format(interimValue.getStartDate()));
 
 		return value;
 	}
 
 }
