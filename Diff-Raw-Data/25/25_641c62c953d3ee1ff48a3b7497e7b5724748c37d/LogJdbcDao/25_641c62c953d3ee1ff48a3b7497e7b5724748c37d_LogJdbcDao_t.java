 package com.tp.dao.log;
 
 import java.util.*;
 
 import javax.annotation.Resource;
 import javax.sql.DataSource;
 
 import com.tp.cache.MemcachedObjectType;
 import com.tp.cache.SpyMemcachedClient;
 import com.tp.mapper.JsonMapper;
 import com.tp.utils.DateFormatUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.stereotype.Component;
 
 @Component
 public class LogJdbcDao {
 
     private static final String QUERY_CONTENT_UNZIP = "SELECT count(*) as unzip,app_name,m.name as market FROM log_f_content l"
             + " LEFT JOIN f_market m ON m.pk_name=l.from_market  WHERE l.create_time BETWEEN"
             + " ? AND ? AND l.do_type='unzip'  GROUP BY app_name,from_market ORDER BY app_name";
 
     private static final String QEURY_DOTYPE_GROUP_BY_MARKET = "SELECT count(*) as installed ,count(distinct(l.imei)) as discount ,"
             + " m.name as from_market,app_name"
             + " FROM log_f_content l LEFT JOIN f_market m ON m.pk_name=l.from_market"
             + " WHERE l.create_time BETWEEN ? AND ? AND l.do_type=?  GROUP BY m.name ORDER BY NULL";
 
     private static final String QEURY_INSTALL_GROUP_BY_MARKET = "SELECT count(*) as installed ,m.name as from_market,app_name"
             + " FROM log_f_content l LEFT JOIN f_market m ON m.pk_name=l.from_market"
             + " WHERE l.create_time BETWEEN ? AND ? AND l.do_type=?  GROUP BY m.name,app_name ORDER BY app_name";
 
     private static final String QUERY_GETCLIENT_PERMARKET = "select l.app_name,m.name as market,count(*) as get_client"
             + " from log_f_store2 l left join f_market m on l.from_market=m.pk_name"
             + " where l.create_time between ? and ? and l.request_method='getClient' group by l.app_name,l.from_market order by l.app_name";
 
     private static final String QUERY_HOTTEST_DOWNLOAD = "SELECT ffi.f_id,ffi.title,ffi.short_description ,f.icon_path FROM f_file f right join"
             + " (select c.theme_name,sum(c.total_down) as download from log_count_content c"
             + " WHERE c.create_time BETWEEN ? AND ? GROUP BY c.theme_name) as l on f.title=l.theme_name"
             + " JOIN f_file_info ffi ON f.id=ffi.f_id JOIN f_file_store fs ON f.id=fs.f_id WHERE ffi.language=? AND fs.s_id=? ORDER BY l.download desc limit ?,20";
 
     private JdbcTemplate jdbcTemplate;
     private SpyMemcachedClient memcachedClient;
    private JsonMapper jsonMapper = JsonMapper.buildNormalMapper();
 
     /**
      * 内容解压安装统计
      */
     public List<Map<String, Object>> countContentUnzip(String sdate, String edate) {
         return jdbcTemplate.queryForList(QUERY_CONTENT_UNZIP, sdate, edate);
     }
 
     /**
      * 客户端安装统计（各个市场）
      */
     public List<Map<String, Object>> countClientInstallPerMarket(String sdate, String edate) {
         return jdbcTemplate.queryForList(QEURY_DOTYPE_GROUP_BY_MARKET, sdate, edate, "client");
     }
 
     /**
      * 内容客户端捆绑安装统计
      */
     public List<Map<String, Object>> countInstallWithContentPerMarket(String sdate, String edate) {
         return jdbcTemplate.queryForList(QEURY_INSTALL_GROUP_BY_MARKET, sdate, edate, "install");
     }
 
     /**
      * 客户端各市场下载统计
      */
     public List<Map<String, Object>> countGetClientPerMarket(String sdate, String edate) {
         return jdbcTemplate.queryForList(QUERY_GETCLIENT_PERMARKET, sdate, edate);
     }
 
     /**
      * 下载排行
      */
    public List<Map<String, Object>> countThemeFileDownload(String language, Long sid, Long pageNo) {
        String key = MemcachedObjectType.THEME_SORT.getPrefix() + pageNo + language;
        String json = memcachedClient.get(key);
        if (json == null) {
             String edate = DateFormatUtils.convertDate(new Date());
             String sdate = DateFormatUtils.getPerMonthDate(edate);
            List<Map<String, Object>> lists = jdbcTemplate.queryForList(QUERY_HOTTEST_DOWNLOAD, sdate, edate, language, sid, (pageNo - 1L) * 20);
            json = jsonMapper.toJson(lists);
            memcachedClient.set(key, MemcachedObjectType.THEME_SORT.getExpiredTime(), json);
         }
        return jsonMapper.fromJson(json, List.class);
     }
 
     @Resource
     public void setDataSource(DataSource dataSource) {
         jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     @Autowired
    public void setMemcachedClient(SpyMemcachedClient memcachedClient) {
        this.memcachedClient = memcachedClient;
     }
 }
