 package com.cnnic.whois.controller;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.UnsupportedEncodingException;
 import java.net.IDN;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.servlet.RequestDispatcher;
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.cnnic.whois.bean.PageBean;
 import com.cnnic.whois.execption.QueryException;
 import com.cnnic.whois.execption.RedirectExecption;
 import com.cnnic.whois.service.QueryService;
 import com.cnnic.whois.util.DataFormat;
 import com.cnnic.whois.util.WhoisUtil;
 
 
 public class QueryServlet extends HttpServlet {
 
 	/**
 	 * Called by the server (via the service method) to allow a servlet to
 	 * handle a POST request. The HTTP POST method allows the client to send
 	 * data of unlimited length to the Web server a single time and is useful
 	 * when posting information such as credit card numbers. When overriding
 	 * this method, read the request data, write the response headers, get the
 	 * response's writer or output stream object, and finally, write the
 	 * response data. It's best to include content type and encoding. When using
 	 * a PrintWriter object to return the response, set the content type before
 	 * accessing the PrintWriter object.
 	 * 
 	 * The servlet container must write the headers before committing the
 	 * response, because in HTTP the headers must be sent before the response
 	 * body.
 	 * 
 	 * Where possible, set the Content-Length header (with the
 	 * ServletResponse.setContentLength(int) method), to allow the servlet
 	 * container to use a persistent connection to return its response to the
 	 * client, improving performance. The content length is automatically set if
 	 * the entire response fits inside the response buffer.
 	 * 
 	 * When using HTTP 1.1 chunked encoding (which means that the response has a
 	 * Transfer-Encoding header), do not set the Content-Length header.
 	 * 
 	 * This method does not need to be either safe or idempotent. Operations
 	 * requested through POST can have side effects for which the user can be
 	 * held accountable, for example, updating stored data or buying items
 	 * online.
 	 * 
 	 * If the HTTP POST request is incorrectly formatted, doPost returns an HTTP
 	 * "Bad Request" message.
 	 * 
 	 * @param request
 	 * @param response
 	 * @throws IOException
 	 * @throws ServletException
 	 */
 	public void doPost(HttpServletRequest request, HttpServletResponse response)
 			throws IOException, ServletException {
 		try {
 			processRequest(request, response);
 		} catch (QueryException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}		
 	}
 
 	/**
 	 * Called by the server (via the service method) to allow a servlet to
 	 * handle a GET request. Overriding this method to support a GET request
 	 * also automatically supports an HTTP HEAD request. A HEAD request is a GET
 	 * request that returns no body in the response, only the request header
 	 * fields.
 	 * 
 	 * When overriding this method, read the request data, write the response
 	 * headers, get the response's writer or output stream object, and finally,
 	 * write the response data. It's best to include content type and encoding.
 	 * When using a PrintWriter object to return the response, set the content
 	 * type before accessing the PrintWriter object.
 	 * 
 	 * The servlet container must write the headers before committing the
 	 * response, because in HTTP the headers must be sent before the response
 	 * body.
 	 * 
 	 * Where possible, set the Content-Length header (with the
 	 * ServletResponse.setContentLength(int) method), to allow the servlet
 	 * container to use a persistent connection to return its response to the
 	 * client, improving performance. The content length is automatically set if
 	 * the entire response fits inside the response buffer.
 	 * 
 	 * When using HTTP 1.1 chunked encoding (which means that the response has a
 	 * Transfer-Encoding header), do not set the Content-Length header.
 	 * 
 	 * The GET method should be safe, that is, without any side effects for
 	 * which users are held responsible. For example, most form queries have no
 	 * side effects. If a client request is intended to change stored data, the
 	 * request should use some other HTTP method.
 	 * 
 	 * The GET method should also be idempotent, meaning that it can be safely
 	 * repeated. Sometimes making a method safe also makes it idempotent. For
 	 * example, repeating queries is both safe and idempotent, but buying a
 	 * product online or modifying data is neither safe nor idempotent.
 	 * 
 	 * If the request is incorrectly formatted, doGet returns an HTTP
 	 * "Bad Request" message.
 	 * 
 	 * @param request
 	 * @param response
 	 * @throws IOException
 	 * @throws ServletException
 	 */
 	@Override
 	protected void doGet(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException, IOException {
 		try {
 			processRequest(request, response);
 		} catch (QueryException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * The request data analysis query type and parameters, depending on the
 	 * type to call the corresponding method.
 	 * 
 	 * @param request
 	 * @param response
 	 * @throws IOException
 	 *             if an input or output error is detected when the servlet
 	 *             handles the request
 	 * @throws ServletException
 	 *             if the request could not be handled
 	 * @throws QueryException 
 	 * @throws SQLException 
 	 */
 	private void processRequest(HttpServletRequest request,
 			HttpServletResponse response) throws IOException, ServletException, QueryException, SQLException {
 
 		Map<String, Object> map = null;
 		char[] n = request.getRequestURI().toCharArray();
 		byte[] b = new byte[n.length];
         for (int i = 0; i < n.length; i++) {
             b[i] = (byte)n[i];
         }
         String str = new String(b, "UTF-8"); 
 		String queryInfo = str.substring(request.getContextPath().length() + 1);
 		String queryType = "";
 		String queryPara = "";
 		
 		String format = getFormatCookie(request);
 		String role = WhoisUtil.getUserRole(request);
 		
 		if(queryInfo.indexOf("/") != -1){
			if(StringUtils.isNotBlank(queryPara)){// domains/xxx?name=z*.cn
				map = WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
				processRespone(request, response, map);
				return;
			}
 			queryType = queryInfo.substring(0, queryInfo.indexOf("/"));
 			queryPara = queryInfo.substring(queryInfo.indexOf("/") + 1); //get the parameters from the request scope and parse
 		}else{
 			queryType = queryInfo;
 			//map = WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 		}
 		boolean isFuzzyQuery = isFuzzyQueryType(queryType);
 		if(isFuzzyQuery){
 			queryPara = getFuzzyQueryString(request,queryType);
 			queryType = convertFuzzyQueryType(queryType);
 		}
 		request.setAttribute("queryType", queryType);
 		int typeIndex = Arrays.binarySearch(WhoisUtil.queryTypes, queryType); //according to the type of the parameter type query
 		PageBean page = getPageParam(request);
 		if(isFuzzyQueryType(typeIndex) && isJsonOrXmlFormat(request)){
 			page.setMaxRecords(QueryService.MAX_SIZE_FUZZY_QUERY);//json/xml set max size
 		}
 		try {
 			switch (typeIndex) {
 			case 0:
 				map = processQueryAS(queryPara, role, format);
 				break;
 			case 1:
 				String queryParaDecode = WhoisUtil.toChineseUrl(queryPara);
 				queryParaDecode = StringUtils.trim(queryParaDecode);
 				String queryParaPuny = IDN.toASCII(queryParaDecode);
 				map = processQueryDomain(isFuzzyQuery,queryParaDecode,queryParaPuny,
 						role, format,page,request);
 				queryPara = IDN.toUnicode(IDN.toASCII(WhoisUtil.toChineseUrl(queryPara)));
 				break;
 			case 2:
 				map = processQueryDsData(queryPara, role, format);
 				break;
 			case 3:
 				String queryParaWithoutPrefix = removeFuzzyPrefixIfHas(isFuzzyQuery,queryPara);
 				map = processQueryEntity(isFuzzyQuery,WhoisUtil.toChineseUrl(queryParaWithoutPrefix), role,
 						format,request,page);
 				break;
 			case 4:
 				map = processQueryEvents(queryPara, role, format);
 				break;
 			case 5:
 				map = processQueryHelp(queryPara, role, format);
 				break;
 			case 6:
 				map = processQueryIP(queryPara, role, format);
 				break;
 			case 7:
 				map = processQueryKeyData(queryPara, role, format);
 				break;
 			case 8:
 				map = processQueryLinks(queryPara, role, format);
 				break;
 			case 9:
 				map = processQueryNameServer(isFuzzyQuery,
 						IDN.toASCII(WhoisUtil.toChineseUrl(queryPara)), role, format,page,request);
 				queryPara = IDN.toUnicode(IDN.toASCII(WhoisUtil.toChineseUrl(queryPara)));
 				break;
 			case 10:
 				map = processQueryNotices(queryPara, role, format);
 				break;
 			case 11:
 				map = processQueryPhones(queryPara, role, format);
 				break;
 			case 12:
 				map = processQueryPostalAddress(queryPara, role, format);
 				break;
 			case 13:
 				map = processQueryRegistrar(queryPara, role, format);
 				break;
 			case 14:
 				map = processQueryRemarks(queryPara, role, format);
 				break;			
 			case 15:
 				map = processQuerySecureDNS(queryPara, role, format);
 				break;
 			case 16:
 				map = processQueryVariants(queryPara, role, format);
 				break;
 			default:
 				map = WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 				break;
 			}
 			String queryParaInput = queryPara;
 			queryParaInput = addPrefixBeforeParaIfEntityFuzzy(isFuzzyQuery,request, queryPara,typeIndex);;
 			request.setAttribute("queryPara", WhoisUtil.toChineseUrl(queryParaInput));
 		} catch (RedirectExecption re) {
 			String redirectUrl = re.getRedirectURL(); //to capture to exception of rediectionException and redirect
 			
 			if (!(redirectUrl.endsWith("/")))
 				redirectUrl += "/";
 			
 			response.setStatus(301);
 			
 			response.setHeader("Accept", format);
 			response.setHeader("Location", redirectUrl + queryPara);
 			response.setHeader("Connection", "close");
 			return;	
 		} catch (QueryException e) {
 			e.printStackTrace();
 			this.log(e.getMessage(), e);
 			map = WhoisUtil.processError(WhoisUtil.ERRORCODE, role, format);
 		}
 		if(isFuzzyQueryType(typeIndex) && isJsonOrXmlFormat(request)){
 			processFuzzyQueryJsonOrXmlRespone(request, response, map, typeIndex);
 		}else{
 			processRespone(request, response, map);
 		}
 	}
 
 	private PageBean getPageParam(HttpServletRequest request) {
 		Object currentPageObj = request.getParameter("currentPage");
 		PageBean page = new PageBean();
 		if(null != currentPageObj){
 			page.setCurrentPage(Integer.valueOf(currentPageObj.toString()));
 		}
 		return page;
 	}
 
 	private String addPrefixBeforeParaIfEntityFuzzy(boolean isFuzzyQuery,
 			HttpServletRequest request,
 			String queryPara, int typeIndex) {
 		if(typeIndex != 3){//only hadle entity type
 			return queryPara;
 		}
 		if(!isFuzzyQuery){
 			return queryPara;
 		}
 		if(StringUtils.isBlank(queryPara)){
 			return queryPara;
 		}
 		String fuzzyQueryParamName = getEntityFuzzyQueryParamName(request);
 		if(!queryPara.startsWith(fuzzyQueryParamName + ":")){
 			queryPara = fuzzyQueryParamName + ":" + queryPara;
 		}
 		return queryPara;
 	}
 	 
 	private String removeFuzzyPrefixIfHas(boolean isFuzzyQuery,String queryPara) {
 		if(StringUtils.isBlank(queryPara)){
 			return queryPara;
 		}
 		if(!isFuzzyQuery){
 			return queryPara;
 		}
 		return queryPara.replaceFirst("fn:", "").replaceFirst("handle:", "");
 	}
 
 	private boolean isFuzzyQueryType(String queryType) {
 		if("domains".equals(queryType)||"nameservers".equals(queryType)
 				||"entities".equals(queryType)){
 			return true;
 		}
 		return false;
 	}
 	
 	private String convertFuzzyQueryType(String queryType) {
 		if("domains".equals(queryType)){
 			return "domain";
 		}
 		if("nameservers".equals(queryType)){
 			return "nameserver";
 		}
 		if("entities".equals(queryType)){
 			return "entity";
 		}
 		return null;
 	}
 	
 	private String getFuzzyQueryString(HttpServletRequest request, String queryType) 
 			throws UnsupportedEncodingException {
 		String queryString = request.getQueryString();
 		if(StringUtils.isBlank(queryString)){
 			return StringUtils.EMPTY;
 		}
 		byte[] bytesQ = queryString.getBytes("iso8859-1");
 		String queryStringDecode = new String(bytesQ,"UTF-8");
 		Map<String, String> paramsMap = parseQueryParameter(queryStringDecode);
 		if("entities".equals(queryType)){
 			String fnParamValue = paramsMap.get("fn");
 			if(StringUtils.isBlank(fnParamValue)){
 				fnParamValue = paramsMap.get("handle");
 			}
 			return fnParamValue;
 		}else{
 			String nameParamValue = paramsMap.get("name");
 			return nameParamValue==null?"":nameParamValue;
 		}
 	}
 	
 	private String getEntityFuzzyQueryParamName(HttpServletRequest request) {
 		Map<String, String> paramsMap = parseQueryParameter(request.getQueryString());
 		if(paramsMap.containsKey("fn")){
 			return "fn";
 		}
 		return "handle";
 	}
 
 	private boolean isJsonOrXmlFormat(HttpServletRequest request) {
 		String format = getFormatCookie(request);
 		if (format.equals("application/json") || format.equals("application/rdap+json") 
 				|| format.equals("application/rdap+json;application/json") || format.equals("application/xml")) {
 			return true;
 		}
 		return false;
 	}
 
 	private void processFuzzyQueryJsonOrXmlRespone(HttpServletRequest request,
 			HttpServletResponse response, Map<String, Object> map,int queryType)
 			throws IOException, ServletException {
 		request.setCharacterEncoding("utf-8");
 		response.setCharacterEncoding("utf-8");
 		
 		String format = getFormatCookie(request);
 		PrintWriter out = response.getWriter();
 		String errorCode = "200"; 
 		
 		request.setAttribute("queryFormat", format);
 		response.setHeader("Access-Control-Allow-Origin", "*");
 		
 		//set response status
 		if(map.containsKey("errorCode") || map.containsKey("Error Code")){
 			if(map.containsKey("errorCode"))
 				errorCode = map.get("errorCode").toString();
 			if (map.containsKey("Error Code"))
 				errorCode = map.get("Error Code").toString();
 			if (errorCode.equals(WhoisUtil.ERRORCODE)){
 				response.setStatus(404);
 			}
 			if (errorCode.equals(WhoisUtil.COMMENDRRORCODE)){
 				response.setStatus(400);
 			}
 		}
 		Iterator<String> iterr = map.keySet().iterator();
 		String multiKey = null;
 		while(iterr.hasNext()){
 			String key =  iterr.next();
 			if(key.startsWith(WhoisUtil.MULTIPRX)){
 				multiKey = key;
 			}
 		}
 		if(multiKey != null){
 			Object jsonObj = map.get(multiKey);
 			map.remove(multiKey);
 			switch (queryType) {
 			case 1:
 				map.put("domainSearchResults", jsonObj);
 				break;
 			case 3:
 				map.put("entitySearchResults", jsonObj);
 				break;
 			case 9:
 				map.put("nameserverSearchResults", jsonObj);
 				break;
 			}
 		}
 		if (format.equals("application/json") || format.equals("application/rdap+json") || format.equals("application/rdap+json;application/json")) { // depending on the return type of the response corresponding data
 			response.setHeader("Content-Type", "application/json");
 			out.print(DataFormat.getJsonObject(map));
 		} else if (format.equals("application/xml")) {
 			response.setHeader("Content-Type", "application/xml");
 			out.write(DataFormat.getXmlString(map));
 		} 
 	}
 	private boolean isFuzzyQueryType(int typeIndex){
 		if(typeIndex == 1 ||typeIndex == 3 ||typeIndex == 9){
 			return true;
 		}
 		return false;
 	}
 	
 	private Map<String, Object> processQueryHelp(String queryPara, String role, String format) throws QueryException {
 		QueryService queryService = QueryService.getQueryService();
 		if(!queryPara.equals("")){
 			return WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 		}
 		return queryService.queryHelp("helpID", role, format);
 	}
 
 	
 	/**
 	 * Query ip type
 	 * 
 	 * @param queryPara
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 * @throws NumberFormatException
 	 * @throws RedirectExecption
 	 */
 	private Map<String, Object> processQueryIP(String queryPara, String role, String format)
 			throws QueryException, NumberFormatException, RedirectExecption {
 		String ipLength = "0";
 		String strInfo = queryPara;
 
 		if (queryPara.indexOf(WhoisUtil.PRX) >= 0) {
 			String[] infoArray = queryPara.split(WhoisUtil.PRX);
 			if(infoArray.length > 1){
 				strInfo = infoArray[0];
 				ipLength = infoArray[1];
 			}
 		}
 
 		if (!verifyIP(strInfo, ipLength)) {
 			return WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 		}
 
 		QueryService queryService = QueryService.getQueryService();
 		return queryService.queryIP(strInfo, Integer.parseInt(ipLength), role, format);
 	}
 
 	/**
 	 * Verifying the IP parameters
 	 * 
 	 * @param ipStr
 	 * @param ipLengthStr
 	 * @return The correct parity returns true, failure to return false
 	 */
 	private boolean verifyIP(String ipStr, String ipLengthStr) {
 		String regex = "^(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|[1-9])\\."
 				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
 				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)\\."
 				+ "(1\\d{2}|2[0-4]\\d|25[0-5]|[1-9]\\d|\\d)$";
 		if (ipLengthStr.equals("0")) {
 			if (!ipStr.matches(regex) && !isIPv6(ipStr))
 				return false;
 		} else {
 			if (!ipStr.matches(regex))
 				return false;
 		}
 		if (!ipLengthStr.matches("^[0-9]*$"))
 			return false;
 
 		return Integer.parseInt(ipLengthStr) >= 0
 				&& Integer.parseInt(ipLengthStr) <= 32;
 	}
 
 	/**
 	 * Query domain type
 	 * 
 	 * @param queryPara
 	 * @param role
 	 * @param page 
 	 * @param request 
 	 * @return map collection
 	 * @throws QueryException
 	 * @throws RedirectExecption
 	 * @throws UnsupportedEncodingException 
 	 */
 	private Map<String, Object> processQueryDomain(boolean isFuzzyQuery,String queryPara, String queryParaPuny,
 			String role, String format, PageBean page, HttpServletRequest request)
 			throws QueryException, RedirectExecption, UnsupportedEncodingException {
 		if(!validateDomainName(queryParaPuny)){
 			return WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 		}
 		QueryService queryService = QueryService.getQueryService();
 		if(isFuzzyQuery){
 			request.setAttribute("pageBean", page);
 			request.setAttribute("queryPath", "domains");
 			return queryService.fuzzyQueryDomain(queryPara,queryParaPuny, role, format,page);
 		}
 		return queryService.queryDomain(queryParaPuny, role, format);
 	}
 	
 	private boolean validateDomainName(String domainName){
 		if (!isCommonInvalidStr(domainName))
 			return false;
 		if(!domainName.startsWith("xn--") && !verifyNameServer(domainName)){
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Query as type
 	 * 
 	 * @param queryPara
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 * @throws RedirectExecption
 	 */
 	private Map<String, Object> processQueryAS(String queryPara, String role, String format)
 			throws QueryException, RedirectExecption {
 		if (!isCommonInvalidStr(queryPara))
 			return WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 
 		if (!queryPara.matches("^[0-9]*$"))
 			return WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 
 		try {
 			int queryInfo = Integer.parseInt(queryPara);
 			QueryService queryService = QueryService.getQueryService();
 			return queryService.queryAS(queryInfo, role, format);
 		} catch (NumberFormatException e) {
 			e.printStackTrace();
 			this.log(e.getMessage(), e);
 			throw new QueryException(e);
 		}
 
 	}
 
 	/**
 	 * Query entity type
 	 * 
 	 * @param queryParaValue
 	 * @param role
 	 * @param request 
 	 * @param page 
 	 * @return map collection
 	 * @throws QueryException
 	 * @throws SQLException 
 	 */
 	private Map<String, Object> processQueryEntity(boolean isFuzzyQuery,String queryParaValue, 
 			String role, String format, HttpServletRequest request, PageBean page)
 			throws QueryException, SQLException {
 		if (!isInvalidEntityStr(queryParaValue))
 			return WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 
 		QueryService queryService = QueryService.getQueryService();
 		if(isFuzzyQuery){
 			String fuzzyQueryParamName = getEntityFuzzyQueryParamName(request);
 			String fuzzyQuerySolrPropName = convertEntityFuzzyQueryParamNameToSolrPropName(fuzzyQueryParamName);
 			Map<String, Object> result = queryService.fuzzyQueryEntity(fuzzyQuerySolrPropName,
 					queryParaValue, role, format,page);
 			request.setAttribute("pageBean", page);
 			request.setAttribute("queryPath", "entities");
 			return result;
 		}
 		return queryService.queryEntity(queryParaValue, role, format);
 	}
 
 	private String convertEntityFuzzyQueryParamNameToSolrPropName(
 			String fuzzyQueryParamName) {
 		if("fn".equals(fuzzyQueryParamName)){
 			return "entityNames";
 		}
 		return fuzzyQueryParamName;
 	}
 
 	/**
 	 * Query nameServer type
 	 * 
 	 * @param queryPara
 	 * @param role
 	 * @param page 
 	 * @param request 
 	 * @return map collection
 	 * @throws QueryException
 	 * @throws RedirectExecption 
 	 */
 	private Map<String, Object> processQueryNameServer(boolean isFuzzyQuery,String queryPara,
 			String role, String format, PageBean page, HttpServletRequest request) throws QueryException, RedirectExecption {
 		if (!verifyNameServer(queryPara))
 			return WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 
 		QueryService queryService = QueryService.getQueryService();
 		if(isFuzzyQuery){
 			request.setAttribute("pageBean", page);
 			request.setAttribute("queryPath", "nameservers");
 			return queryService.fuzzyQueryNameServer(queryPara, role, format,page);
 		}
 		return queryService.queryNameServer(queryPara, role, format);
 	}
 
 	/**
 	 * Verifying the NameServer parameters
 	 * 
 	 * @param queryPara
 	 * @return The correct parity returns true, failure to return false
 	 */
 	private boolean verifyNameServer(String queryPara) {
 		if (!isCommonInvalidStr(queryPara))
 			return false;
 		String fuzzyReg = "^(?!-.)(?!.*?-$)((\\*)?[0-9a-zA-Z][0-9a-zA-Z-]{0,62}(\\*)?\\.)+([0-9a-zA-Z][0-9a-zA-Z-]{0,62}\\*?)?$";
 		if (queryPara.matches(fuzzyReg))
 			return true;
 		return false;
 	}
 	
 	/**
 	 * Query link type
 	 * 
 	 * @param queryPara
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	private Map<String, Object> processQueryLinks(String queryPara, String role, String format)
 			throws QueryException {
 		if (!isCommonInvalidStr(queryPara))
 			return WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 
 		QueryService queryService = QueryService.getQueryService();
 		return queryService.queryLinks(queryPara, role, format);
 	}
 
 	/**
 	 * Query phone type
 	 * 
 	 * @param queryPara
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	private Map<String, Object> processQueryPhones(String queryPara, String role, String format)
 			throws QueryException {
 		if (!isCommonInvalidStr(queryPara))
 			return WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 
 		QueryService queryService = QueryService.getQueryService();
 		return queryService.queryPhones(queryPara, role, format);
 	}
 
 	/**
 	 * Query postalAddress type
 	 * 
 	 * @param queryPara
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	private Map<String, Object> processQueryPostalAddress(String queryPara,
 			String role, String format) throws QueryException {
 		if (!isCommonInvalidStr(queryPara))
 			return WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 
 		QueryService queryService = QueryService.getQueryService();
 		return queryService.queryPostalAddress(queryPara, role, format);
 	}
 
 	/**
 	 * Query variants type
 	 * 
 	 * @param queryPara
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	private Map<String, Object> processQueryVariants(String queryPara,
 			String role, String format) throws QueryException {
 		if (!isCommonInvalidStr(queryPara))
 			return WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 
 		QueryService queryService = QueryService.getQueryService();
 		return queryService.queryVariants(queryPara, role, format);
 	}
 	
 	/**
 	 * Query SecureDNS
 	 * 
 	 * @param queryPara
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	private Map<String, Object> processQuerySecureDNS(String queryPara,
 			String role, String format) throws QueryException {
 		if (!isCommonInvalidStr(queryPara))
 			return WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 
 		QueryService queryService = QueryService.getQueryService();
 		return queryService.querySecureDNS(queryPara, role, format);
 	}
 	
 	/**
 	 * Query DsData
 	 * 
 	 * @param queryPara
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	private Map<String, Object> processQueryDsData(String queryPara,
 			String role, String format) throws QueryException {
 		if (!isCommonInvalidStr(queryPara))
 			return WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 
 		QueryService queryService = QueryService.getQueryService();
 		return queryService.queryDsData(queryPara, role, format);
 	}
 	
 	/**
 	 * Query KeyData
 	 * 
 	 * @param queryPara
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	private Map<String, Object> processQueryKeyData(String queryPara,
 			String role, String format) throws QueryException {
 		if (!isCommonInvalidStr(queryPara))
 			return WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 
 		QueryService queryService = QueryService.getQueryService();
 		return queryService.queryKeyData(queryPara, role, format);
 	}
 
 	/**
 	 * Query notices type
 	 * 
 	 * @param queryPara
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	private Map<String, Object> processQueryNotices(String queryPara,
 			String role, String format) throws QueryException {
 		if (!isCommonInvalidStr(queryPara))
 			return WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 
 		QueryService queryService = QueryService.getQueryService();
 		return queryService.queryNotices(queryPara, role, format);
 	}
 
 	/**
 	 * Query registrar type
 	 * 
 	 * @param queryPara
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	private Map<String, Object> processQueryRegistrar(String queryPara,
 			String role, String format) throws QueryException {
 		QueryService queryService = QueryService.getQueryService();
 		return queryService.queryRegistrar(queryPara, role, true, format);
 	}
 	/**
 	 * Query Remarks type
 	 * 
 	 * @param queryPara
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	private Map<String, Object> processQueryRemarks(String queryPara,
 			String role, String format) throws QueryException {
 		if (!isCommonInvalidStr(queryPara))
 			return WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 
 		QueryService queryService = QueryService.getQueryService();
 		return queryService.queryRemarks(queryPara, role, format);
 	}
 	
 	/**
 	 * Query Events type
 	 * 
 	 * @param queryPara
 	 * @param role
 	 * @return map collection
 	 * @throws QueryException
 	 */
 	private Map<String, Object> processQueryEvents(String queryPara,
 			String role, String format) throws QueryException {
 		if (!isCommonInvalidStr(queryPara))
 			return WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE, role, format);
 
 		QueryService queryService = QueryService.getQueryService();
 		return queryService.queryEvents(queryPara, role, format);
 	}
 	/**
 	 * Verifying the String parameters
 	 * 
 	 * @param parm
 	 * @return The correct parity returns true, failure to return false
 	 */
 	private boolean isCommonInvalidStr(String parm) {
 		String strReg = "^[a-zA-Z\\d\\*]{1}([\\w\\-\\.\\_\\*]*)$";
 
 		if (parm.equals("") || parm == null)
 			return false;
 
 		return parm.matches(strReg);
 	}
 	
 	private boolean isInvalidEntityStr(String parm) {
 //		String strReg = "^[@\\w\\-\\.\\_\\* \u4E00-\u9FFF]*$";
 //		if (parm.equals("") || parm == null)
 //			return false;
 //		return parm.matches(strReg);
 		return StringUtils.isNotBlank(parm);
 	}
 
 	/**
 	 * Returned in response to the corresponding data according to the type of
 	 * request types
 	 * 
 	 * @param request
 	 * @param response
 	 * @param map
 	 * @throws IOException
 	 * @throws ServletException
 	 */
 	private void processRespone(HttpServletRequest request,
 			HttpServletResponse response, Map<String, Object> map)
 			throws IOException, ServletException {
 		request.setCharacterEncoding("utf-8");
 		response.setCharacterEncoding("utf-8");
 		
 		String format = getFormatCookie(request);
 		Map<String, Object> htmlMap = new LinkedHashMap<String, Object>();
 		htmlMap.putAll(map);
 		
 		PrintWriter out = response.getWriter();
 		String errorCode = "200"; 
 		
 		request.setAttribute("queryFormat", format);
 		response.setHeader("Access-Control-Allow-Origin", "*");
 		
 		//set response status
 		if(map.containsKey("errorCode") || map.containsKey("Error Code")){
 			if(map.containsKey("errorCode"))
 				errorCode = map.get("errorCode").toString();
 			if (map.containsKey("Error Code"))
 				errorCode = map.get("Error Code").toString();
 			if (errorCode.equals(WhoisUtil.ERRORCODE)){
 				response.setStatus(404);
 			}
 			if (errorCode.equals(WhoisUtil.COMMENDRRORCODE)){
 				response.setStatus(400);
 			}
 		}
 		//multi-responses
 		
 		Iterator<String> iterr = map.keySet().iterator();
 		String multiKey = null;
 		while(iterr.hasNext()){
 			String key =  iterr.next();
 			if(key.startsWith(WhoisUtil.MULTIPRX)){
 				multiKey = key;
 			}
 		}
 		if(multiKey != null){
 			Object jsonObj = map.get(multiKey);
 			map.remove(multiKey);
 			map.put(multiKey.substring(WhoisUtil.MULTIPRX.length()), jsonObj);
 		}
 		
 		if (format.equals("application/json") || format.equals("application/rdap+json") || format.equals("application/rdap+json;application/json")) { // depending on the return type of the response corresponding data
 			response.setHeader("Content-Type", "application/json");
 			out.print(DataFormat.getJsonObject(map));
 		} else if (format.equals("application/xml")) {
 			response.setHeader("Content-Type", "application/xml");
 			out.write(DataFormat.getXmlString(map));
 		} else if (format.equals("application/html")) {
 			if(!errorCode.equals(WhoisUtil.ERRORCODE) && !errorCode.equals(WhoisUtil.COMMENDRRORCODE)){
 				if(htmlMap.containsKey(WhoisUtil.RDAPCONFORMANCEKEY))
 					htmlMap.remove(WhoisUtil.RDAPCONFORMANCEKEY);
 				if(htmlMap.containsKey(WhoisUtil.SEARCH_RESULTS_TRUNCATED_EKEY))
 					htmlMap.remove(WhoisUtil.SEARCH_RESULTS_TRUNCATED_EKEY);
 				request.setAttribute("JsonObject", DataFormat.getJsonObject(htmlMap));
 				RequestDispatcher rdsp = request.getRequestDispatcher("/index.jsp");
 				response.setContentType("application/html");
 				rdsp.forward(request, response);
 			}else{
 				response.sendError(Integer.parseInt(errorCode));
 			}
 		} else {
 			response.setHeader("Content-Type", "text/plain");
 			out.write(DataFormat.getPresentation(map));
 		}
 	}
 
 	/**
 	 * Get cookies format parameters
 	 * 
 	 * @param request
 	 * @return If data is returned to format data, null is returned if there is
 	 *         no
 	 */
 	private String getFormatCookie(HttpServletRequest request) {
 		Cookie[] cookies = request.getCookies();
 		String format = null;
 		
 		if (cookies != null) {
 			for (Cookie cookie : cookies) {
 				if (cookie.getName().equals("Format")) {
 					return cookie.getValue();
 				}
 			}
 		}
 		
 		if (format == null)
 			format = request.getHeader("Accept"); // determine what kind of return type
 
 		CharSequence sqhtml = "html";			
 		if(format.contains(sqhtml))
 			format = "application/html";
 
 		if (format == null)
 			format = "application/json";
 		
 		return format;
 	}
 
 	/**
 	 * Verifying the IPv6 parameters
 	 * 
 	 * @param address
 	 * @return The correct parity returns true, failure to return false
 	 */
 	private boolean isIPv6(String address) {
 		boolean result = false;
 		String regHex = "(\\p{XDigit}{1,4})";
 
 		String regIPv6Full = "^(" + regHex + ":){7}" + regHex + "$";
 
 		String regIPv6AbWithColon = "^(" + regHex + "(:|::)){0,6}" + regHex
 				+ "$";
 		String regIPv6AbStartWithDoubleColon = "^(" + "::(" + regHex
 				+ ":){0,5}" + regHex + ")$";
 		String regIPv6 = "^(" + regIPv6Full + ")|("
 				+ regIPv6AbStartWithDoubleColon + ")|(" + regIPv6AbWithColon
 				+ ")$";
 
 		if (address.indexOf(":") != -1) {
 			if (address.length() <= 39) {
 				String addressTemp = address;
 				int doubleColon = 0;
 				if (address.equals("::"))
 					return true;
 				while (addressTemp.indexOf("::") != -1) {
 					addressTemp = addressTemp
 							.substring(addressTemp.indexOf("::") + 2,
 									addressTemp.length());
 					doubleColon++;
 				}
 				if (doubleColon <= 1) {
 					result = address.matches(regIPv6);
 				}
 			}
 		}
 		return result;
 	}
 	
 	public static Map<String, String> parseQueryParameter(String queryParameter) {
 		Map<String, String> map = new HashMap<String, String>();
 		if (StringUtils.isEmpty(queryParameter)) {
 			return map;
 		}
 		int index = queryParameter.lastIndexOf("=");
 		int pos = queryParameter.length();
 		while (index != -1) {
 			String value = queryParameter.substring(index + 1, pos);
 			pos = index;
 			index = queryParameter.lastIndexOf("&", index);
 			String keyName = queryParameter.substring(index + 1, pos);
 			pos = index;
 			index = queryParameter.lastIndexOf("=", pos);
 			map.put(keyName, value);
 		}
 		return map;
 	}
 
 	/**
 	 * key is the name of the value converted to unicode map collection
 	 * 
 	 * @param map
 	 * @return Conversion completed map collection
 	 */
 //	private Map<String, Object> nameToUnicode(Map<String, Object> map) {
 //		String name = (String) map.get("LdhName");
 //		if (name != null)
 //			map.put("LdhName", IDN.toUnicode(name));
 //
 //		return map;
 //	}
 }
