 package com.tp.service;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.tp.dao.LogCountClientDao;
 import com.tp.dao.LogCountContentDao;
 import com.tp.dao.LogCountContentMarketDao;
 import com.tp.dao.LogFromClientDao;
 import com.tp.dao.LogInHomeDao;
 import com.tp.dao.MarketDao;
 import com.tp.dao.ThemeFileDao;
 import com.tp.entity.LogContentMarket;
 import com.tp.entity.LogCountClient;
 import com.tp.entity.LogCountContent;
 import com.tp.entity.LogFromClient;
 import com.tp.entity.LogInHome;
 import com.tp.entity.Market;
 import com.tp.entity.ThemeFile;
 import com.tp.orm.Page;
 import com.tp.orm.PropertyFilter;
 import com.tp.utils.DateFormatUtils;
 
 @Component
 @Transactional
 public class LogService {
 
 	public static final String METHOD_EXECUTE = "execute";
 	public static final String METHOD_GETCLIENT = "getClient";
 
 	private LogFromClientDao logClientDao;
 	private LogInHomeDao logHomeDao;
 	private LogCountClientDao countClientDao;
 	private LogCountContentDao countContentDao;
 	private LogCountContentMarketDao ccMarketDao;
 	private ThemeFileDao themeDao;
 	private MarketDao marketDao;
 
 	public void saveLogFromClent(LogFromClient entity) {
 		logClientDao.save(entity);
 	}
 
 	public void saveLogInHome(LogInHome entity) {
 		logHomeDao.save(entity);
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
 
 	public void createClientReport(String sdate, String edate) {
 		LogCountClient client = new LogCountClient();
 		long start = System.currentTimeMillis();
 		long downTotal = countClientDownloadTotal(sdate, edate);
 		long downByContent = countClientDownloadByContent(sdate, edate);
 		long downByShare = countClientDownloadByShare(sdate, edate);
 		long totalUser = countTotalUser(edate);
 		long perTotalUser = 0L;
 
 		LogCountClient perCount = getLogClientCountByDate(DateFormatUtils.getPerDate(sdate));
 		if (perCount != null) {
 			perTotalUser = perCount.getTotalUser();
 		}
 		client.setCreateTime(sdate);
 		client.setDownByContent(downByContent);
 		client.setDownByShare(downByShare);
 		client.setTotalDownload(downTotal);
 		client.setDownByOther(downTotal - downByContent - downByShare);
 		client.setVisitStoreCount(countVisitHome(sdate, edate));
 		client.setVisitStoreUser(countVisitUser(sdate, edate));
 		client.setOpenCount(countUse(sdate, edate));
 		client.setTotalUser(totalUser);
 		client.setIncrementUser(totalUser - perTotalUser);
 		client.setOpenUser(countOpenUser(sdate, edate));
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
 	 * 查询商店访问量
 	 */
 	private Long countVisitHome(String sdate, String edate) {
 		return logHomeDao.countByMethod(METHOD_EXECUTE, "%%", sdate, edate);
 	}
 
 	/**
 	 * 查询商店访问用户量
 	 */
 	private Long countVisitUser(String sdate, String edate) {
 		return logHomeDao.countUserInHome(METHOD_EXECUTE, sdate, edate);
 	}
 
 	/**
 	 * 查询客户端总下载量
 	 */
 	private Long countClientDownloadTotal(String sdate, String edate) {
 		return logHomeDao.countByMethod(METHOD_GETCLIENT, "%%", sdate, edate);
 	}
 
 	/**
 	 * 分享下载客户端量
 	 */
 	private Long countClientDownloadByShare(String sdate, String edate) {
 		return logHomeDao.countByMethod(METHOD_GETCLIENT, "%f:share%", sdate, edate);
 	}
 
 	/**
 	 * 从内容下载客户端量
 	 */
 	private Long countClientDownloadByContent(String sdate, String edate) {
 		return logHomeDao.countByMethod(METHOD_GETCLIENT, "%cv:%", sdate, edate);
 	}
 
 	public void createContentReport(String sdate, String edate) {
 		List<ThemeFile> themes = themeDao.getAll();
 		List<Market> markets = marketDao.getAll();
 		for (ThemeFile theme : themes) {
 			LogCountContent lcct = new LogCountContent();
 			long totalVisit = countContentTotalVisit(String.valueOf(theme.getId()), sdate, edate);
 			long visitByAd = countContentVisitByAD(String.valueOf(theme.getId()), sdate, edate);
 			long totalDown = countContentTotalDown(String.valueOf(theme.getId()), theme.getMarketURL(), sdate, edate);
 			long marketDown = countContentMarketDown(theme.getMarketURL(), sdate, edate);
 			lcct.setLogDate(sdate);
 			lcct.setThemeName(theme.getTitle());
 			lcct.setTotalVisit(totalVisit);
 			lcct.setTotalDown(totalDown);
 			lcct.setVisitByAd(visitByAd);
 			lcct.setVisitByStore(totalVisit - visitByAd);
 			lcct.setDownByStore(totalDown - marketDown);
 			countContentDao.save(lcct);
 			perMarketDown(theme, lcct, markets, sdate, edate);
 		}
 	}
 
 	private void perMarketDown(ThemeFile theme, LogCountContent lcc, List<Market> markets, String sdate, String edate) {
 
 		for (Market market : markets) {
 			if (market.getThemes().contains(theme)) {
 				LogContentMarket ccMarket = new LogContentMarket();
 				long perMarketDown = countContentPerMarketDown(market.getMarketKey(), theme.getMarketURL(), sdate,
 						edate);
 				ccMarket.setMarketName(market.getName());
 				ccMarket.setTotalDown(perMarketDown);
 				ccMarket.setLogContent(lcc);
 				ccMarketDao.save(ccMarket);
 			}
 		}
 	}
 
 	/**
 	 * 文件内容总访问量
 	 */
 	public Long countContentTotalVisit(String fid, String sdate, String edate) {
 		return logHomeDao.countContentByMethod("details", "%" + fid + "%", sdate, edate);
 	}
 
 	/**
 	 * 文件内容广告引导访问量
 	 */
 
 	public Long countContentVisitByAD(String fid, String sdate, String edate) {
 		return logHomeDao.countContentByMethod("details", "%f:ad%" + fid + "%", sdate, edate);
 	}
 
 	/**
 	 * 文件下载总量
 	 */
 	public Long countContentTotalDown(String fid, String fpack, String sdate, String edate) {
 		String[] methods = { "execute", "more", "getClient", "details" };
		return logHomeDao.countContentTotalDown(Arrays.asList(methods), "%" + fpack + "%", "%id:" + fid + "%", sdate,
 				edate);
 	}
 
 	/**
 	 * 文件对应的market访问量(下载量)
 	 */
 	public Long countContentMarketDown(String filePackage, String sdate, String edate) {
 
 		String[] methods = { "execute", "more", "getClient", "details", "file-download.action" };
 
 		return logHomeDao.countContentNotIn(Arrays.asList(methods), "%" + filePackage + "%", sdate, edate);
 	}
 
 	/**
 	 * 文件在每个市场的下载量
 	 */
 	public Long countContentPerMarketDown(String marketKey, String fpack, String sdate, String edate) {
 		return logHomeDao.countContentPerMarketDown("%" + marketKey + "%" + fpack + "%", sdate, edate);
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
 }
