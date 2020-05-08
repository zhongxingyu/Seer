 /**
  *
  */
 package org.ocha.hdx.service;
 
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import javax.annotation.Resource;
 
import org.apache.derby.tools.sysinfo;
 import org.ocha.hdx.model.api2.ApiIndicatorValue;
 import org.ocha.hdx.model.api2.ApiResultWrapper;
 import org.ocha.hdx.model.api2util.RequestParamsWrapper;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.google.common.cache.LoadingCache;
 
 /**
  * @author alexandru-m-g
  *
  */
 public class ApiV2BackendServiceImpl implements ApiV2BackendService {
 
 	private static org.slf4j.Logger logger = LoggerFactory.getLogger(ApiV2BackendServiceImpl.class);
 
 	@Autowired
 	IntermediaryBackendService intermediaryBackendService;
 
 	@Resource
 	LoadingCache<RequestParamsWrapper, ApiResultWrapper<ApiIndicatorValue>> indicatorResultCache;
 
 	/* (non-Javadoc)
 	 * @see org.ocha.hdx.service.ApiV2BackendService#listIndicatorsByCriteriaWithPagination(java.util.List, java.util.List, java.util.List, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.Integer, java.lang.String)
 	 */
 	@Override
 	public ApiResultWrapper<ApiIndicatorValue> listIndicatorsByCriteriaWithPagination(
 			final List<String> indicatorTypeCodes, final List<String> sourceCodes,
 			final List<String> entityCodes, final Integer startYear, final Integer endYear,
 			final Integer pageNum, final Integer pageSize, final String lang) {
 
 		final RequestParamsWrapper paramsWrapper = new RequestParamsWrapper(indicatorTypeCodes, sourceCodes,
 				entityCodes, startYear, endYear, pageNum, pageSize, lang);
 		final ApiResultWrapper<ApiIndicatorValue> result = this.searchViaCache(paramsWrapper);
 		return result;
 	}
 
 
 	/* (non-Javadoc)
 	 * @see org.ocha.hdx.service.ApiV2BackendService#listIndicatorsByCriteria(java.util.List, java.util.List, java.util.List, java.lang.Integer, java.lang.Integer, java.lang.String)
 	 */
 	@Override
 	public ApiResultWrapper<ApiIndicatorValue> listIndicatorsByCriteria(final List<String> indicatorTypeCodes, final List<String> sourceCodes, final List<String> entityCodes,
 			final Integer startYear, final Integer endYear, final String lang) {
 
 		final RequestParamsWrapper paramsWrapper = new RequestParamsWrapper(indicatorTypeCodes, sourceCodes,
 				entityCodes, startYear, endYear, null, null, lang);
 		final ApiResultWrapper<ApiIndicatorValue> result = this.searchViaCache(paramsWrapper);
 		return result;
 	}
 
 	/**
 	 * @param paramsWrapper
 	 */
 	private ApiResultWrapper<ApiIndicatorValue> searchViaCache(final RequestParamsWrapper paramsWrapper) {
 		ApiResultWrapper<ApiIndicatorValue> result;
 		try {
 			result = this.indicatorResultCache.get(paramsWrapper);
 			System.out.println( this.indicatorResultCache.stats().toString() );
 			return result;
 		} catch (final ExecutionException e) {
 			logger.error(e.getMessage());
 			e.printStackTrace();
 			return null;
 		}
 	}
 
 }
