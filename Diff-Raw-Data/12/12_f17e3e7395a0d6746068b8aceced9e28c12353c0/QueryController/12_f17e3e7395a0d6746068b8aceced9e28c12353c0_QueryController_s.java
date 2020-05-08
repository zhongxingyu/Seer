 package com.cnnic.whois.controller;
 
 import java.io.IOException;
 import java.net.IDN;
 import java.sql.SQLException;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.HttpStatus;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 
 import com.cnnic.whois.bean.DomainQueryParam;
 import com.cnnic.whois.bean.EntityQueryParam;
 import com.cnnic.whois.bean.IpQueryParam;
 import com.cnnic.whois.bean.QueryParam;
 import com.cnnic.whois.bean.QueryType;
 import com.cnnic.whois.dao.query.QueryEngine;
 import com.cnnic.whois.execption.QueryException;
 import com.cnnic.whois.execption.RedirectExecption;
 import com.cnnic.whois.service.QueryService;
 import com.cnnic.whois.util.WhoisUtil;
 import com.cnnic.whois.util.validate.ValidateUtils;
 
 @Controller
 @RequestMapping("/{dot}well-known/rdap")
 public class QueryController extends BaseController {
 	@Autowired
 	private QueryService queryService;
 	@Autowired
 	private QueryEngine queryEngine;
 
 	@RequestMapping(value = "/", method = RequestMethod.GET)
 	public String index() {
 		return "/dot/index";
 	}
 			
 	@RequestMapping(value = "/domains", method = RequestMethod.GET)
 	@ResponseBody
 	public void fuzzyQueryDomain(@RequestParam(required = true) String name,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		name = StringUtils.trim(name);
 		name = super.getNormalization(name);
 		String punyDomainName = name;
 		Map<String, Object> resultMap = null;
 		DomainQueryParam domainQueryParam = super
 				.praseDomainQueryParams(request);
 		try {
 			punyDomainName = IDN.toASCII(name);// long lable exception
 		} catch (Exception e) {// TODO:delete ,exception filter
 			super.renderResponseError400(request, response);
 			return;
 		}
 		request.setAttribute("queryPara", IDN.toUnicode(punyDomainName));
 		if (!ValidateUtils.validateDomainName(punyDomainName)) {
 			resultMap = WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE);
 		} else {
 			domainQueryParam.setQueryType(QueryType.SEARCHDOMAIN);
 			domainQueryParam.setQ(name);
 			domainQueryParam.setDomainPuny(punyDomainName);
 			setMaxRecordsForFuzzyQ(domainQueryParam);
 			resultMap = queryService.query(domainQueryParam);
 			request.setAttribute("pageBean", domainQueryParam.getPage());
 			request.setAttribute("queryPath", "domains");
 		}
 		request.setAttribute("queryType", "domain");
 		renderResponse(request, response, resultMap, domainQueryParam);
 	}
 
 	@RequestMapping(value = "/domain/{domainName}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryDomain(@PathVariable String domainName,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		domainName = StringUtils.trim(domainName);
 		domainName = StringUtils.lowerCase(domainName);
 		domainName = super.getNormalization(domainName);
 		String punyDomainName = domainName;
 		Map<String, Object> resultMap = null;
 		DomainQueryParam domainQueryParam = super
 				.praseDomainQueryParams(request);
 		try {
 			domainName = WhoisUtil.toChineseUrl(domainName);
 			punyDomainName = IDN.toASCII(domainName);// long lable exception
 		} catch (Exception e) {
 			super.renderResponseError400(request, response);
 			return;
 		}
 		request.setAttribute("queryPara", IDN.toUnicode(punyDomainName));
 		if (!ValidateUtils.validateDomainName(punyDomainName)) {
 			resultMap = WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE);
 		} else {
 			domainQueryParam.setQueryType(QueryType.DOMAIN);
 			domainQueryParam.setQ(domainName);
 			domainQueryParam.setDomainPuny(punyDomainName);
 			resultMap = queryService.queryDomain(domainQueryParam);
 		}
 		request.setAttribute("queryType", "domain");
 		renderResponse(request, response, resultMap, domainQueryParam);
 	}
 
 	@RequestMapping(value = "/entities", method = RequestMethod.GET)
 	@ResponseBody
 	public void fuzzyQueryEntity(@RequestParam(required = false) String fn,
 			@RequestParam(required = false) String handle,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, SQLException, IOException, ServletException {
 		Map<String, Object> resultMap = null;
 		EntityQueryParam queryParam = super.praseEntityQueryParams(request);
 		if (StringUtils.isBlank(fn) && StringUtils.isBlank(handle)) {
 			super.renderResponseError400(request, response);
 			return;
 		}
 		String q = fn;
 		if (StringUtils.isNotBlank(handle)) {
 			q = handle;
 		}
 		String decodeQ = WhoisUtil.toChineseUrl(q);
 		String fuzzyQuerySolrPropName = "handle";
 		String paramName = "handle";
 		if (StringUtils.isNotBlank(fn)) {
 			fuzzyQuerySolrPropName = "entityNames";
 			paramName = "fn";
 		}
 		queryParam.setFuzzyQueryParamName(fuzzyQuerySolrPropName);
 		queryParam.setQ(decodeQ);
 		setMaxRecordsForFuzzyQ(queryParam);
 		resultMap = queryService.fuzzyQueryEntity(queryParam);
 		request.setAttribute("pageBean", queryParam.getPage());
 		request.setAttribute("queryPath", "entities");
 		request.setAttribute("queryType", "entity");
 		request.setAttribute("queryPara", paramName + ":" + decodeQ);
 		renderResponse(request, response, resultMap, queryParam);
 	}
 
 	@RequestMapping(value = "/entity/{entityName}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryEntity(@PathVariable String entityName,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, SQLException, IOException, ServletException {
 		EntityQueryParam queryParam = super.praseEntityQueryParams(request);
 		queryParam.setQ(entityName);
 		Map<String, Object> resultMap = queryService.queryEntity(queryParam);
 		request.setAttribute("queryType", "entity");
 		request.setAttribute("queryPara", entityName);
 		renderResponse(request, response, resultMap, queryParam);
 	}
 
 	@RequestMapping(value = "/nameservers", method = RequestMethod.GET)
 	@ResponseBody
	public void fuzzyQueryNs(@RequestParam(required = false) String nsName,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, SQLException, IOException, ServletException,
 			RedirectExecption {
 		Map<String, Object> resultMap = null;
 		QueryParam queryParam = super.praseQueryParams(request);		
		nsName = StringUtils.trim(nsName);
		nsName = super.getNormalization(nsName);
		if (StringUtils.isBlank(nsName)) {
 			resultMap = WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE);
 			renderResponse(request, response, resultMap, queryParam);
 			return;
 		} 
		String decodeQ = WhoisUtil.toChineseUrl(nsName);
 		String punyQ = IDN.toASCII(decodeQ);	
 		request.setAttribute("queryPara", decodeQ);
 		if (!ValidateUtils.verifyNameServer(punyQ)) {
 			resultMap = WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE);	
 		} else {
 			queryParam.setQ(punyQ);
 			request.setAttribute("pageBean", queryParam.getPage());
 			request.setAttribute("queryPath", "nameservers");
 			setMaxRecordsForFuzzyQ(queryParam);
 			resultMap = queryService.fuzzyQueryNameServer(queryParam);
 		}
 		request.setAttribute("queryType", "nameserver");
 		renderResponse(request, response, resultMap, queryParam);
 	}
 
 	@RequestMapping(value = "/nameserver/{nsName}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryNs(@PathVariable String nsName,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, SQLException, IOException, ServletException {
 		nsName = StringUtils.trim(nsName);
 		nsName = StringUtils.lowerCase(nsName);
 		nsName = super.getNormalization(nsName);
 		String punyNsName = IDN.toASCII(WhoisUtil.toChineseUrl(nsName));
 		Map<String, Object> resultMap = null;
 		QueryParam queryParam = super.praseQueryParams(request);
 		if (!ValidateUtils.verifyNameServer(punyNsName)) {
 			resultMap = WhoisUtil.processError(WhoisUtil.COMMENDRRORCODE);
 		} else {
 			queryParam.setQ(punyNsName);
 			queryParam.setQueryType(QueryType.NAMESERVER);
 			resultMap = queryService.query(queryParam);
 			request.setAttribute("queryPara", IDN.toUnicode(punyNsName));
 		}
 		request.setAttribute("queryType", "nameserver");
 		renderResponse(request, response, resultMap, queryParam);
 	}
 
 	@RequestMapping(value = "/autnum/{autnum}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryAs(@PathVariable String autnum,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		QueryParam queryParam = super.praseQueryParams(request);
 		queryParam.setQ(autnum);
 		Map<String, Object> resultMap = queryService.queryAS(queryParam);
 		request.setAttribute("queryType", "autnum");
 		renderResponse(request, response, resultMap, queryParam);
 	}
 
 	@RequestMapping(value = "/dsData/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryDsData(@PathVariable String q, HttpServletRequest request,
 			HttpServletResponse response) throws QueryException,
 			RedirectExecption, IOException, ServletException {
 		query(QueryType.DSDATA, q, request, response);
 	}
 
 	@RequestMapping(value = "/events/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryEvents(@PathVariable String q, HttpServletRequest request,
 			HttpServletResponse response) throws QueryException,
 			RedirectExecption, IOException, ServletException {
 		query(QueryType.EVENTS, q, request, response);
 	}
 
 	@RequestMapping(value = "/help/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryHelp(@PathVariable String help,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		Map<String, Object> resultMap = null;
 		QueryParam queryParam = super.praseQueryParams(request);
 		if (StringUtils.isNotBlank(help)) {
 			super.renderResponseError400(request, response);
 		} else {
 			queryParam.setQ("helpID");
 			resultMap = queryService.queryHelp(queryParam);
 			request.setAttribute("queryPara", queryParam);
 			renderResponse(request, response, resultMap, queryParam);
 		}
 	}
 
 	@RequestMapping(value = {"/ip/{ip}","/ip/{ip}/"}, method = RequestMethod.GET)
 	@ResponseBody
 	public void queryIp(@PathVariable String ip, HttpServletRequest request,
 			HttpServletResponse response) throws QueryException,
 			RedirectExecption, IOException, ServletException {
 		String net = "0";
 		doQueryIp(ip, request, response, net);
 	}
 
 	@RequestMapping(value = {"/ip/{ip}/{net}","/ip/{ip}/{net}/"}, method = RequestMethod.GET)
 	@ResponseBody
 	public void queryIpWithNet(@PathVariable String ip,
 			@PathVariable String net, HttpServletRequest request,
 			HttpServletResponse response) throws QueryException,
 			RedirectExecption, IOException, ServletException {
 		net = StringUtils.trim(net);
 		doQueryIp(ip, request, response, net);
 	}
 
 	private void doQueryIp(String ip, HttpServletRequest request,
 			HttpServletResponse response, String ipLength)
 			throws QueryException, IOException, ServletException,
 			RedirectExecption {
 		ip = StringUtils.trim(ip);
 		Map<String, Object> resultMap = null;
 		IpQueryParam queryParam = super.praseIpQueryParams(request);
 		String strInfo = ip;
 		if (!ValidateUtils.verifyIP(strInfo, ipLength)) {
 			super.renderResponseError400(request, response);
 			return;
 		}
 		queryParam.setQ(ip);
 		queryParam.setIpInfo(strInfo);
 		queryParam.setIpLength(Integer.parseInt(ipLength));
 		resultMap = queryService.queryIP(queryParam);
 		request.setAttribute("queryPara", ip);
 		request.setAttribute("queryType", "ip");
 		viewResolver.writeResponse(queryParam.getFormat(), request, response,
 				resultMap, 0);
 	}
 
 	@RequestMapping(value = "/keyData/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryKeyData(@PathVariable String q,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		query(QueryType.KEYDATA, q, request, response);
 	}
 
 	@RequestMapping(value = "/links/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryLinks(@PathVariable String q, HttpServletRequest request,
 			HttpServletResponse response) throws QueryException,
 			RedirectExecption, IOException, ServletException {
 		query(QueryType.LINKS, q, request, response);
 	}
 
 	@RequestMapping(value = "/notices/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryNotices(@PathVariable String q,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		query(QueryType.NOTICES, q, request, response);
 	}
 
 	@RequestMapping(value = "/phones/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryPhones(@PathVariable String q, HttpServletRequest request,
 			HttpServletResponse response) throws QueryException,
 			RedirectExecption, IOException, ServletException {
 		query(QueryType.PHONES, q, request, response);
 	}
 
 	@RequestMapping(value = "/postalAddress/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryPostalAddress(@PathVariable String q,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		query(QueryType.POSTALADDRESS, q, request, response);
 	}
 
 	@RequestMapping(value = "/secureDNS/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void querySecureDNS(@PathVariable String q,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		query(QueryType.SECUREDNS, q, request, response);
 	}
 
 	@RequestMapping(value = "/remarks/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryRemarks(@PathVariable String q,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		query(QueryType.REMARKS, q, request, response);
 	}
 
 	@RequestMapping(value = "/variants/{q}", method = RequestMethod.GET)
 	@ResponseBody
 	public void queryVariants(@PathVariable String q,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, RedirectExecption, IOException,
 			ServletException {
 		query(QueryType.VARIANTS, q, request, response);
 	}
 
 	private void query(QueryType queryType, String q,
 			HttpServletRequest request, HttpServletResponse response)
 			throws QueryException, IOException, ServletException {
 		Map<String, Object> resultMap = null;
 		QueryParam queryParam = praseQueryParams(request);
 		queryParam.setQueryType(queryType);
 		queryParam.setQ(q);
 		if (!ValidateUtils.isCommonInvalidStr(queryParam.getQ())) {
 			super.renderResponseError400(request, response);
 		} else {
 			resultMap = queryService.query(queryParam);
 			request.setAttribute("queryPara", queryParam);
 			renderResponse(request, response, resultMap, queryParam);
 		}
 	}
 
 	@RequestMapping(value = "/**", method = RequestMethod.GET)
 	@ResponseBody
 	public void error400(HttpServletRequest request,
 			HttpServletResponse response) throws QueryException,
 			RedirectExecption, IOException, ServletException {
 		QueryParam queryParam = praseQueryParams(request);
 		Map<String, Object> resultMap = WhoisUtil
 				.processError(WhoisUtil.COMMENDRRORCODE);
 		renderResponse(request, response, resultMap, queryParam);
 		return;
 	}
 	
 	@ExceptionHandler(value={RedirectExecption.class})
 	@ResponseStatus( value=HttpStatus.MOVED_PERMANENTLY ) 
     public String exp(Exception ex,HttpServletRequest request,HttpServletResponse response) {  
 		RedirectExecption rEx = (RedirectExecption)ex;
 		response.setHeader("Accept", getFormatCookie(request));
 		response.setHeader("Location", rEx.getRedirectURL());
 		response.setHeader("Connection", "close");
 		return null;
     } 
 }
