 package cn.uc.play.japid.template;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import cn.uc.play.japid.exception.InvalidArgumentException;
 import cn.uc.play.japid.exception.TemplateCompileException;
 import cn.uc.play.japid.util.FileUtils;
 
 
 import play.Logger;
 import play.Play;
 import play.db.DB;
 import play.db.helper.JdbcHelper;
 import play.cache.Cache;
 import play.libs.Codec;
 
 /**
  * Load template(s) from mysql.
  * 
  * @author Robin Han<sakuyahan@163.com>
  * @date 2012-4-29
  */
 public class TemplateLoaderMysqlImpl implements UJapidTemplateLoader {
 
 	private Map<String, UJapidTemplate> templatesCache = new ConcurrentHashMap();
 
 	private long nativeCacheExpire = 3 * 60 * 1000;
 
 	private long remoteCacheExpire = 60 * 60 * 1000;
 
 	private File root;
 
 	public TemplateLoaderMysqlImpl(String templateDir, long nativeCacheExpire,
 			long remoteCacheExpire) throws FileNotFoundException {
 		if (nativeCacheExpire > -1) {
 			this.nativeCacheExpire = nativeCacheExpire;
 		}
 
 		if (remoteCacheExpire > -1) {
 			this.remoteCacheExpire = remoteCacheExpire / 1000;
 		}
 
 		this.root = new File(templateDir);
 		if (!root.exists()) {
 			throw new FileNotFoundException(templateDir);
 		}
 	}
 
 	@Override
 	public UJapidTemplate loadTemplate(String name) throws SQLException {
 		if (name == null || name.isEmpty()) {
 			return null;
 		}
 
 		UJapidTemplate template = templatesCache.get(name);
 
 		if (template != null) {
 			Long lastModifyInRemoteCache = Cache.get(Codec.hexMD5(name),
 					Long.class);
 
 			if (lastModifyInRemoteCache != null
 					&& template.lastModifyTime.getTime() >= lastModifyInRemoteCache) {
 				return template;
 			}
 		}
 
 		String sql = "select id, source, last_modify from TEMPLATE where id=?";
 		ResultSet rs = JdbcHelper.execute(sql, name);
 
 		if (rs.next()) {
 			UJapidTemplate t = createTemplateFromResultSet(rs);
 			return t;
 		}
 
 		return null;
 	}
 
 	@Override
 	public Map<String, UJapidTemplate> loadAllTemplates() throws SQLException {
 		Map<String, UJapidTemplate> map = new HashMap<String, UJapidTemplate>();
 
 		String sql = "select id, source, last_modify from TEMPLATE";
 
 		ResultSet rs = JdbcHelper.execute(sql);
 
 		while (rs.next()) {
 			UJapidTemplate template = createTemplateFromResultSet(rs);
 			map.put(template.nameWithPath, template);
 		}
 
 		return map;
 	}
 
 	private UJapidTemplate createTemplateFromResultSet(ResultSet rs)
 			throws SQLException {
		Date lastModifyTime = rs.getTimestamp("last_modify");
 		String path = rs.getString("id");
 
 		UJapidTemplate templateInNativeCache = templatesCache.get(path);
 		if (templateInNativeCache != null
 				&& lastModifyTime.getTime() <= templateInNativeCache.lastModifyTime
 						.getTime()) {
 			Cache.set(Codec.hexMD5(path), new Long(
 					lastModifyTime.getTime() / 1000), remoteCacheExpire + "s");
 			return templateInNativeCache;
 		}
 
 		UJapidTemplate template = new UJapidTemplate();
 		template.nameWithPath = rs.getString("id");
 		template.name = FileUtils.getFileNameInPath(template.nameWithPath);
 		template.lastModifyTime = lastModifyTime;
 		template.lastSyncTime = new Date();
 		template.source = rs.getString("source");
 		template.mode = TemplateStoreMode.DB;
 
 		try {
 			FileUtils.writeToFile(
 					root + File.separator + template.nameWithPath,
 					template.source);
 			UJapidTemplate.compileTemplate(template);
 			templatesCache.put(template.nameWithPath, template);
 			Cache.set(Codec.hexMD5(template.nameWithPath), new Long(
 					template.lastModifyTime.getTime() / 1000),
 					remoteCacheExpire + "s");
 		} catch (Exception e) {
 			revertTemplate(template.nameWithPath, e);
 		}
 
 		return template;
 	}
 
 	private void revertTemplate(String path, Exception e) {
 		UJapidTemplate template = templatesCache.containsKey(path) ? templatesCache
 				.get(path) : null;
 
 		if (template != null) {
 
 			try {
 				FileUtils.writeToFile(root + File.separator + path,
 						template.source);
 			} catch (IOException e1) {
 				Logger.error(e, "Revert template " + path + " faild.");
 			}
 			UJapidTemplate.compileTemplate(template);
 			template.lastModifyTime = new Date();
 			template.lastSyncTime = new Date();
 			templatesCache.put(path, template);
 			Logger.error(
 					e,
 					path
 							+ " compiling faild. Ignore this compiling request and return the older template object in cache.");
 		} else {
 			throw new TemplateCompileException(path + " compiling faild.", e);
 		}
 
 		if (Play.Mode.DEV == Play.mode) {
 			throw new TemplateCompileException(path + " compiling faild.", e);
 		}
 	}
 
 	@Override
 	public UJapidTemplate getTemplate(String path) throws Exception {
 
 		UJapidTemplate template = templatesCache.get(path);
 		if (template != null
 				&& (new Date().getTime() - template.lastSyncTime.getTime()) < nativeCacheExpire) {
 			return template;
 		}
 		return loadTemplate(path);
 	}
 
 }
