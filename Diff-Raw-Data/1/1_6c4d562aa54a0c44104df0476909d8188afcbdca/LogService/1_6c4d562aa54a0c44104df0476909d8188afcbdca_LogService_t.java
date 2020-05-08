 package com.tp.service;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.lucene.search.IndexSearcher;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.tp.cache.MemcachedObjectType;
 import com.tp.cache.SpyMemcachedClient;
 import com.tp.dao.MarketDao;
 import com.tp.dao.ThemeFileDao;
 import com.tp.dao.log.LogCmccResultDao;
 import com.tp.dao.log.LogJdbcDao;
 import com.tp.dao.log.LogCountClientDao;
 import com.tp.dao.log.LogCountContentDao;
 import com.tp.dao.log.LogCountContentMarketDao;
 import com.tp.dao.log.LogCountGetClientDao;
 import com.tp.dao.log.LogCountUnzipDao;
 import com.tp.dao.log.LogForCmccDao;
 import com.tp.dao.log.LogForContentDao;
 import com.tp.dao.log.LogForPollDao;
 import com.tp.dao.log.LogForRedirectDao;
 import com.tp.dao.log.LogFromClientDao;
 import com.tp.dao.log.LogInHomeDao;
 import com.tp.entity.Market;
 import com.tp.entity.ThemeFile;
 import com.tp.entity.log.LogCmccResult;
 import com.tp.entity.log.LogContentMarket;
 import com.tp.entity.log.LogCountClient;
 import com.tp.entity.log.LogCountContent;
 import com.tp.entity.log.LogCountGetClient;
 import com.tp.entity.log.LogCountUnzip;
 import com.tp.entity.log.LogForCmcc;
 import com.tp.entity.log.LogForContent;
 import com.tp.entity.log.LogForPoll;
 import com.tp.entity.log.LogForRedirect;
 import com.tp.entity.log.LogFromClient;
 import com.tp.entity.log.LogInHome;
 import com.tp.mapper.JsonMapper;
 import com.tp.orm.Page;
 import com.tp.orm.PropertyFilter;
 import com.tp.search.LogSearch;
 import com.tp.utils.Constants;
 import com.tp.utils.DateFormatUtils;
 
 @Component
 @Transactional
 public class LogService {
 
 	private static Logger logger = LoggerFactory.getLogger(LogService.class);
 
 	private static final long COUNTER_LOG_HOME = 400L;
 	private static final long COUNTER_LOG_CLIENT = 100L;
 	private static final long COUNTER_LOG_CONTENT = 100L;
 
 	private List<String> key_log_homes = Lists.newArrayList();
 
 	private List<String> key_log_client = Lists.newArrayList();
 
 	private List<String> key_log_content = Lists.newArrayList();
 
 	private LogFromClientDao logClientDao;
 	private LogInHomeDao logHomeDao;
 	private LogForContentDao logContentDao;
 	private LogCountClientDao countClientDao;
 	private LogCountContentDao countContentDao;
 	private LogCountContentMarketDao ccMarketDao;
 	private LogForCmccDao cmccDao;
 	private LogForRedirectDao redirectDao;
 	private LogForPollDao pollDao;
 	private LogJdbcDao logJdbcDao;
 
 	private LogCountUnzipDao logCountUnzipDao;
 	private LogCountGetClientDao logCountGetClientDao;
 
 	private ThemeFileDao themeDao;
 	private MarketDao marketDao;
 
 	private LogCmccResultDao cmccResultDao;
 
 	private LogSearch logSearch;
 	private SpyMemcachedClient memcachedClient;
 	private JsonMapper mapper = JsonMapper.buildNormalMapper();
 
 	public void saveCmcc(LogForCmcc entity) {
 		cmccDao.save(entity);
 	}
 
 	public void saveRedirect(LogForRedirect entity) {
 		redirectDao.save(entity);
 	}
 
 	public void savePoll(LogForPoll entity) {
 		pollDao.save(entity);
 	}
 
 	public void saveCmccResult(LogCmccResult entity) {
 		cmccResultDao.save(entity);
 	}
 
 	public List<LogInHome> queryLogInHomeByDate(String sdate, String edate) {
 		return logHomeDao.queryByDate(sdate, edate);
 	}
 
 	public Page<LogCountUnzip> searchContentUnzip(final Page<LogCountUnzip> page, final List<PropertyFilter> filters) {
 		return logCountUnzipDao.findPage(page, filters);
 	}
 
 	public Page<LogCountGetClient> searchGetClient(final Page<LogCountGetClient> page,
 			final List<PropertyFilter> filters) {
 		return logCountGetClientDao.findPage(page, filters);
 	}
 
 	public void saveLogFromClient(LogFromClient entity) {
 		String counter = MemcachedObjectType.COUNTER_CLIENT.getPrefix();
 		int expTime = MemcachedObjectType.COUNTER_CLIENT.getExpiredTime();
 		long count = cachedLogs(counter, expTime, MemcachedObjectType.LOG_CLIENT.getPrefix(), mapper.toJson(entity));
 		if (count % COUNTER_LOG_CLIENT == 0) {
 			memcachedClient.set(counter, expTime, "0");
 			batchLogClient(count);
 		}
 	}
 
 	public void saveLogContent(LogForContent entity) {
 		String counter = MemcachedObjectType.COUNTER_CONTENT.getPrefix();
 		int expTime = MemcachedObjectType.COUNTER_CONTENT.getExpiredTime();
 
 		long count = cachedLogs(counter, expTime, MemcachedObjectType.LOG_CONTENT.getPrefix(), mapper.toJson(entity));
 		if (count % COUNTER_LOG_CONTENT == 0) {
 			memcachedClient.set(counter, expTime, "0");
 			batchLogContent(count);
 		}
 	}
 
 	public void saveLogInHome(LogInHome entity) {
 
 		String counter = MemcachedObjectType.COUNTER_PAGE.getPrefix();
 		int expTime = MemcachedObjectType.COUNTER_PAGE.getExpiredTime();
 
 		long count = cachedLogs(counter, expTime, MemcachedObjectType.LOG_PAGE.getPrefix(), mapper.toJson(entity));
 
 		if (count % COUNTER_LOG_HOME == 0) {
 			memcachedClient.set(counter, expTime, "0");
 			batchLogInHomes(count);
 		}
 	}
 
 	private long cachedLogs(String counterKey, int exp, String logKey, String logValue) {
 		long counter = memcachedClient.getMemcachedClient().incr(counterKey, 1);
 		if (counter == -1) {
 			memcachedClient.getMemcachedClient().add(counterKey, exp, "1");
 			counter = 1;
 		}
 		memcachedClient.set(logKey + counter, exp, logValue);
 		return counter;
 	}
 
 	private void batchLogInHomes(long contentSize) {
 
 		Map<String, Object> results = getFromCached(MemcachedObjectType.LOG_PAGE.getPrefix(), contentSize,
 				key_log_homes);
 		int i = 0;
 		for (Entry<String, Object> entry : results.entrySet()) {
 			String value = (String) entry.getValue();
 			LogInHome entity = mapper.fromJson(value, LogInHome.class);
 			try {
 				logHomeDao.save(entity);
 				if (++i % 20 == 0) {
 					logHomeDao.flush();
 				}
 			} catch (Exception e) {
 				logger.error(e.getMessage() + " 异常记录为：" + mapper.toJson(entity));
 			}
 
 		}
 
 		key_log_homes.clear();
 	}
 
 	private void batchLogClient(long contentSize) {
 		Map<String, Object> results = getFromCached(MemcachedObjectType.LOG_CLIENT.getPrefix(), contentSize,
 				key_log_client);
 		int i = 0;
 		for (Entry<String, Object> entry : results.entrySet()) {
 			String value = (String) entry.getValue();
 			LogFromClient entity = mapper.fromJson(value, LogFromClient.class);
 			try {
 				logClientDao.save(entity);
 				if (++i % 20 == 0) {
 					logClientDao.flush();
 				}
 			} catch (Exception e) {
 				logger.error(e.getMessage() + " 异常记录为：" + mapper.toJson(entity));
 			}
 
 		}
 		key_log_client.clear();
 	}
 
 	private void batchLogContent(long contentSize) {
 		Map<String, Object> results = getFromCached(MemcachedObjectType.LOG_CONTENT.getPrefix(), contentSize,
 				key_log_content);
 		int i = 0;
 		for (Entry<String, Object> entry : results.entrySet()) {
 			String value = (String) entry.getValue();
 			LogForContent entity = mapper.fromJson(value, LogForContent.class);
 			try {
 				logContentDao.save(entity);
 				if (++i % 20 == 0) {
 					logContentDao.flush();
 				}
 			} catch (Exception e) {
 				logger.error(e.getMessage() + " 异常记录为：" + mapper.toJson(entity));
 			}
 
 		}
 		key_log_content.clear();
 	}
 
 	private Map<String, Object> getFromCached(String logKey, long count, List<String> keys) {
 		for (int i = 1; i <= count; i++) {
 			keys.add(logKey + i);
 		}
 		return memcachedClient.getBulk(keys);
 	}
 
 	public void saveLogCountClient(LogCountClient entity) {
 		countClientDao.save(entity);
 	}
 
 	public List<LogCountContent> getAllContents() {
 		return countContentDao.getAll();
 	}
 
 	public List<LogCountContent> getContentByThemeOrDate(String theme, String date) {
 		return countContentDao.getByContentOrDate(theme, date);
 	}
 
 	public LogCountClient getLogClientCountByDate(String date) {
 		return countClientDao.findUniqueBy("createTime", date);
 	}
 
 	public Page<LogCountClient> searchLogCountClient(final Page<LogCountClient> page, final List<PropertyFilter> filters) {
 		return countClientDao.findPage(page, filters);
 	}
 
 	public Page<LogCountContent> searchLogCountContent(final Page<LogCountContent> page,
 			final List<PropertyFilter> filters) {
 		return countContentDao.findPage(page, filters);
 	}
 
 	public LogCountClient getLogCountClient(Long id) {
 		return countClientDao.get(id);
 	}
 
 	public void saveCountContentUnzip(String sdate, String edate) {
 		List<Map<String, Object>> unzips = logJdbcDao.countContentUnzip(sdate, edate);
 		int i = 0;
 		for (Map<String, Object> content : unzips) {
 			LogCountUnzip unzip = new LogCountUnzip();
 			unzip.setAppName((String) content.get("app_name"));
 			unzip.setUnzip((Long) content.get("unzip"));
 			unzip.setMarketName((String) content.get("market"));
 			unzip.setCreateTime(sdate);
 			try {
 				logCountUnzipDao.save(unzip);
 				if (++i % 20 == 0) {
 					logCountUnzipDao.flush();
 				}
 			} catch (Exception e) {
 				logger.error(e.getMessage() + " 错误记录为：" + mapper.toJson(unzip));
 			}
 		}
 	}
 
 	public void createGetClientPerMarketReport(String sdate, String edate) {
 		List<Map<String, Object>> getClients = logJdbcDao.countGetClientPerMarket(sdate, edate);
 		for (Map<String, Object> content : getClients) {
 			LogCountGetClient entity = new LogCountGetClient();
 			entity.setAppName((String) content.get("app_name"));
 			entity.setMarketName((String) content.get("market"));
 			entity.setDownload((Long) content.get("get_client"));
			entity.setCreateTime(sdate);
 			logCountGetClientDao.save(entity);
 		}
 	}
 
 	private Map<String, Object> getClientInstall(String sdate, String edate) {
 		List<Map<String, Object>> installs = logJdbcDao.countClientInstall(sdate, edate);
 		Map<String, Object> results = Maps.newHashMap();
 		long fm = 0;
 		long nofm = 0;
 		long all = 0;
 		for (Map<String, Object> content : installs) {
 			String fromMarket = (String) content.get("from_market");
 			Long installed = (Long) content.get("installed");
 			all += installed;
 			if (fromMarket != null && !fromMarket.isEmpty()) {
 				fm += installed;
 			} else {
 				nofm += installed;
 			}
 		}
 		results.put("all", all);
 		results.put("fm", fm);
 		results.put("nofm", nofm);
 		return results;
 	}
 
 	public void createClientReport(IndexSearcher searcher, String sdate, String edate) throws Exception {
 		LogCountClient client = new LogCountClient();
 		long start = System.currentTimeMillis();
 		long downTotal = logHomeDao.countClientDownload(Constants.METHOD_GET_CLIENT, sdate, edate);
 		long downByContent = logHomeDao.countClientDownByContent(Constants.METHOD_GET_CLIENT, "%cv:%", sdate, edate);
 		long downByShare = logSearch.downloadByShare(searcher, sdate, edate);
 		long totalUser = countTotalUser(edate);
 		long perTotalUser = 0L;
 
 		LogCountClient perCount = getLogClientCountByDate(DateFormatUtils.getPerDate(sdate));
 		if (perCount != null) {
 			perTotalUser = perCount.getTotalUser();
 		}
 
 		Map<String, Object> install = getClientInstall(sdate, edate);
 		client.setCreateTime(sdate);
 		client.setDownByContent(downByContent);
 		client.setDownByShare(downByShare);
 		client.setTotalDownload(downTotal);
 		client.setDownByOther(downTotal - downByContent - downByShare);
 		client.setVisitStoreCount(logSearch.storeVisits(searcher, sdate, edate));
 		client.setVisitStoreUser(countVisitUser(sdate, edate));
 		client.setOpenCount(countUse(sdate, edate));
 		client.setTotalUser(totalUser);
 		client.setIncrementUser(totalUser - perTotalUser);
 		client.setOpenUser(countOpenUser(sdate, edate));
 		client.setTotalInstall((Long) install.get("all"));
 		client.setInstallNonfm((Long) install.get("nofm"));
 		client.setInstallWithfm((Long) install.get("fm"));
 		long end = System.currentTimeMillis();
 		client.setTakeTimes(end - start);
 		countClientDao.save(client);
 	}
 
 	/**
 	 * 查询用户量
 	 */
 	private Long countOpenUser(String sdate, String edate) {
 		return logClientDao.countUserByDate(sdate, edate);
 	}
 
 	/**
 	 * 查询总用户量
 	 */
 
 	private Long countTotalUser(String edate) {
 		return logClientDao.countTotalUser(edate);
 	}
 
 	/**
 	 * 查询客户端启用次数
 	 */
 	private Long countUse(String sdate, String edate) {
 		return logClientDao.countOpenUseByDate(sdate, edate);
 	}
 
 	/**
 	 * 查询商店访问用户量
 	 */
 	private Long countVisitUser(String sdate, String edate) {
 		return logHomeDao.countUserInHome(Constants.METHOD_EXECUTE, sdate, edate);
 	}
 
 	public void createContentReport(IndexSearcher searcher, String sdate, String edate) throws Exception {
 		List<ThemeFile> themes = themeDao.getByStore("lock");
 		List<Market> markets = marketDao.getAll();
 		for (ThemeFile theme : themes) {
 			LogCountContent lcct = new LogCountContent();
 
 			String fid = String.valueOf(theme.getId());
 			long totalVisit = logSearch.contentVisits(searcher, fid, sdate, edate);
 			long visitByAD = logSearch.contentVisitByAD(searcher, fid, sdate, edate);
 			long marketDown = logSearch.contentDownFromMarket(searcher, theme.getMarketURL(), sdate, edate);
 			long selfDown = logSearch.contentDownFromStore(searcher, fid, sdate, edate);
 
 			lcct.setLogDate(sdate);
 			lcct.setThemeName(theme.getTitle());
 			lcct.setTotalVisit(totalVisit);
 			lcct.setTotalDown(marketDown + selfDown);
 			lcct.setVisitByAd(visitByAD);
 			lcct.setVisitByStore(totalVisit - visitByAD);
 			lcct.setDownByStore(selfDown);
 			countContentDao.save(lcct);
 			perMarketDown(searcher, theme, lcct, markets, sdate, edate);
 		}
 	}
 
 	@Deprecated
 	public void createGetClientByContentReport(IndexSearcher searcher, String sdate, String edate) throws Exception {
 		List<ThemeFile> themes = themeDao.getByStore("lock");
 		int i = 0;
 		for (ThemeFile theme : themes) {
 
 			long count = logSearch.countGetClientByContent(searcher, sdate, edate, theme.getTitle());
 			LogCountGetClient entity = new LogCountGetClient();
 			entity.setAppName(theme.getTitle());
 			entity.setDownload(count);
 			entity.setCreateTime(sdate);
 			try {
 				logCountGetClientDao.save(entity);
 				if (++i % 20 == 0) {
 					logCountGetClientDao.flush();
 				}
 			} catch (Exception e) {
 				logger.error("error:" + e.getMessage() + mapper.toJson(entity));
 			}
 		}
 	}
 
 	private void perMarketDown(IndexSearcher searcher, ThemeFile theme, LogCountContent lcc, List<Market> markets,
 			String sdate, String edate) throws Exception {
 
 		for (Market market : markets) {
 			if (market.getThemes().contains(theme)) {
 				LogContentMarket ccMarket = new LogContentMarket();
 
 				String marketKey = market.getMarketKey();
 				marketKey = escape(marketKey);
 				if (marketKey.equals("marketclient")) {
 					marketKey = market.getPkName();
 				}
 				String fpack = theme.getMarketURL();
 				long perMarketDown = logSearch.contentPerMarketDown(searcher, sdate, edate, marketKey, fpack);
 				ccMarket.setMarketName(market.getName());
 				ccMarket.setTotalDown(perMarketDown);
 				ccMarket.setLogContent(lcc);
 				ccMarketDao.save(ccMarket);
 			}
 		}
 	}
 
 	private String escape(String marketKey) {
 		if (marketKey != null && !marketKey.isEmpty()) {
 			String[] strs = StringUtils.split(marketKey, ":");
 			if (strs.length > 0) {
 				return strs[0];
 			}
 		}
 		return "";
 
 	}
 
 	@Autowired
 	public void setLogClientDao(LogFromClientDao logClientDao) {
 		this.logClientDao = logClientDao;
 	}
 
 	@Autowired
 	public void setLogHomeDao(LogInHomeDao logHomeDao) {
 		this.logHomeDao = logHomeDao;
 	}
 
 	@Autowired
 	public void setCountClientDao(LogCountClientDao countClientDao) {
 		this.countClientDao = countClientDao;
 	}
 
 	@Autowired
 	public void setThemeDao(ThemeFileDao themeDao) {
 		this.themeDao = themeDao;
 	}
 
 	@Autowired
 	public void setMarketDao(MarketDao marketDao) {
 		this.marketDao = marketDao;
 	}
 
 	@Autowired
 	public void setCcMarketDao(LogCountContentMarketDao ccMarketDao) {
 		this.ccMarketDao = ccMarketDao;
 	}
 
 	@Autowired
 	public void setCountContentDao(LogCountContentDao countContentDao) {
 		this.countContentDao = countContentDao;
 	}
 
 	@Autowired
 	public void setLogContentDao(LogForContentDao logContentDao) {
 		this.logContentDao = logContentDao;
 	}
 
 	@Autowired
 	public void setLogSearch(LogSearch logSearch) {
 		this.logSearch = logSearch;
 	}
 
 	@Autowired
 	public void setCmccResultDao(LogCmccResultDao cmccResultDao) {
 		this.cmccResultDao = cmccResultDao;
 	}
 
 	@Autowired
 	public void setCmccDao(LogForCmccDao cmccDao) {
 		this.cmccDao = cmccDao;
 	}
 
 	@Autowired
 	public void setPollDao(LogForPollDao pollDao) {
 		this.pollDao = pollDao;
 	}
 
 	@Autowired
 	public void setRedirectDao(LogForRedirectDao redirectDao) {
 		this.redirectDao = redirectDao;
 	}
 
 	@Autowired
 	public void setMemcachedClient(SpyMemcachedClient memcachedClient) {
 		this.memcachedClient = memcachedClient;
 	}
 
 	@Autowired
 	public void setLogJdbcDao(LogJdbcDao logJdbcDao) {
 		this.logJdbcDao = logJdbcDao;
 	}
 
 	@Autowired
 	public void setLogCountUnzipDao(LogCountUnzipDao logCountUnzipDao) {
 		this.logCountUnzipDao = logCountUnzipDao;
 	}
 
 	@Autowired
 	public void setLogCountGetClientDao(LogCountGetClientDao logCountGetClientDao) {
 		this.logCountGetClientDao = logCountGetClientDao;
 	}
 
 }
