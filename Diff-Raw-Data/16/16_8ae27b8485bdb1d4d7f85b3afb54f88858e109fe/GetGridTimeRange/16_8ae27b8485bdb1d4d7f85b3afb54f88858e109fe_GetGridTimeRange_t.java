 package gov.usgs.cida.gdp.wps.algorithm.discovery;
 
 import com.google.common.base.Preconditions;
 import gov.usgs.cida.gdp.dataaccess.helper.OpendapServerHelper;
 import gov.usgs.cida.gdp.dataaccess.bean.Response;
 import gov.usgs.cida.gdp.dataaccess.bean.Time;
 import gov.usgs.cida.gdp.dataaccess.cache.ResponseCache;
 import gov.usgs.cida.gdp.dataaccess.cache.ResponseCache.CacheIdentifier;
 import org.apache.commons.lang.StringUtils;
 import org.n52.wps.algorithm.annotation.Algorithm;
 import org.n52.wps.algorithm.annotation.ComplexDataOutput;
 import org.n52.wps.algorithm.annotation.Execute;
 import org.n52.wps.algorithm.annotation.LiteralDataInput;
 import org.n52.wps.algorithm.annotation.LiteralDataOutput;
 import org.n52.wps.server.AbstractAnnotatedAlgorithm;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author isuftin
  */
 @Algorithm(
 		version = "1.0.0",
 		title = "Get Grid Time Range",
 		abstrakt = "Gets the start and end time from a grid")
 public class GetGridTimeRange extends AbstractAnnotatedAlgorithm {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(GetGridTimeRange.class);
 	private static final String PARAM_CATALOG_URL = "catalog-url";
 	private static final String PARAM_GRID = "grid";
 	private static final String PARAM_USE_CACHE = "allow-cached-response";
 	private static final String PARAM_RESULT_DEFAULT = "result";
 	private static final String PARAM_RESULT_STRING = "result_as_string";
 	private static final String PARAM_RESULT_JSON = "result_as_json";
 	private static final String PARAM_RESULT_XML = "result_as_xml";
 	private String catalogURL;
 	private String grid;
 	private boolean useCache = false;  // optional arguemnt, set default value
 	private Response response = null;
 
 	@LiteralDataInput(identifier = PARAM_CATALOG_URL)
 	public void setCatalogURL(String catalogURL) {
 		this.catalogURL = catalogURL;
 	}
 
 	@LiteralDataInput(identifier = PARAM_GRID)
 	public void setGrids(String grid) {
 		this.grid = grid;
 	}
 
 	@LiteralDataInput(identifier = PARAM_USE_CACHE, minOccurs = 0, defaultValue = "false")
 	public void setUseCache(boolean useCache) {
 		this.useCache = useCache;
 	}
 
 	@LiteralDataOutput(
 			identifier = PARAM_RESULT_DEFAULT,
 			abstrakt = "Returns the output an XML fragment")
 	public String getResult() {
 		return response.toXMLFragment();
 	}
 
 	@LiteralDataOutput(
 			identifier = PARAM_RESULT_STRING,
 			abstrakt = "Returns the time range as a string in the format of 'start-time|end-time'")
 	public String getResultAsString() {
 		return response.toString();
 	}
 
 	@ComplexDataOutput(
 			identifier = PARAM_RESULT_XML,
 			binding = gov.usgs.cida.gdp.wps.binding.XMLBinding.class,
 			abstrakt = "Returns XML")
 	public String getResultAsXML() {
 		return ((Time) response).toXML();
 	}
 
 	@ComplexDataOutput(
 			identifier = PARAM_RESULT_JSON,
 			binding = gov.usgs.cida.gdp.wps.binding.JSONBinding.class,
 			abstrakt = "Returns JSON")
 	public String getResultAsJSON() {
 		return response.toJSON();
 	}
 
 	@Execute
 	public void process() {
 		Preconditions.checkArgument(StringUtils.isNotBlank(catalogURL), "Invalid " + PARAM_CATALOG_URL);
 		Preconditions.checkArgument(StringUtils.isNotBlank(grid), "Invalid " + PARAM_GRID);
 
 		CacheIdentifier cacheIdentifier = new CacheIdentifier(
 				catalogURL,
 				CacheIdentifier.CacheType.TIME_RANGE,
 				grid);
 		try {
 			if (useCache && ResponseCache.hasCachedResponse(cacheIdentifier)) {
 				this.response = Time.buildFromCache(cacheIdentifier);
 			} 
 			
 			if (this.response == null) {
 				this.response = OpendapServerHelper.getTimeBean(catalogURL, grid);
				if (this.response != null && ((Time) this.response).getTime().length > 0 && useCache) {
 					this.response.writeToCache(cacheIdentifier);
 				}
 			}
 		} catch (Exception ex) {
 			LOGGER.error(ex.getMessage());
 			addError(ex.getMessage());
 			throw new RuntimeException("Error occured while getting time range.  Function halted.", ex);
 		}
 	}
 }
