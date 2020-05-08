 package org.araqne.logdb.metadata;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Invalidate;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.araqne.log.api.LoggerRegistry;
 import org.araqne.logdb.AccountService;
 import org.araqne.logdb.MetadataCallback;
 import org.araqne.logdb.MetadataProvider;
 import org.araqne.logdb.MetadataService;
 import org.araqne.logdb.QueryContext;
 import org.araqne.logdb.QueryParseException;
 import org.araqne.logdb.Row;
 import org.slf4j.LoggerFactory;
 
 @Component(name = "logdb-logger-metadata")
 public class LoggerMetadataProvider implements MetadataProvider {
 	private final org.slf4j.Logger slog = LoggerFactory.getLogger(LoggerMetadataProvider.class);
 
 	@Requires
 	private AccountService accountService;
 
 	@Requires
 	private MetadataService metadataService;
 
 	@Requires
 	private LoggerRegistry loggerRegistry;
 
 	@Validate
 	public void start() {
 		metadataService.addProvider(this);
 	}
 
 	@Invalidate
 	public void stop() {
 		if (metadataService != null)
 			metadataService.removeProvider(this);
 	}
 
 	@Override
 	public String getType() {
 		return "loggers";
 	}
 
 	@Override
 	public void verify(QueryContext context, String queryString) {
		if (!context.getSession().isAdmin()) {
			// throw new QueryParseException("no-read-permission", -1);
			throw new QueryParseException("90510", -1, -1, null);
 		}
 	}
 
 	@Override
 	public void query(QueryContext context, String queryString, MetadataCallback callback) {
 		try {
 			for (org.araqne.log.api.Logger logger : loggerRegistry.getLoggers()) {
 				Map<String, Object> m = new HashMap<String, Object>();
 				m.put("namespace", logger.getNamespace());
 				m.put("name", logger.getName());
 				m.put("factory_namespace", logger.getFactoryNamespace());
 				m.put("factory_name", logger.getFactoryName());
				m.put("status", logger.isRunning() ? "running" : "stopped");
 				m.put("interval", logger.getInterval());
 				m.put("log_count", logger.getLogCount());
 				m.put("drop_count", logger.getDropCount());
 				m.put("last_start_at", logger.getLastStartDate());
 				m.put("last_run_at", logger.getLastRunDate());
 				m.put("last_log_at", logger.getLastLogDate());
 				m.put("last_write_at", logger.getLastWriteDate());
 				callback.onPush(new Row(m));
 			}
 		} catch (Throwable t) {
 			slog.error("araqne logdb: failed to load logger status");
 			Map<String, String> params = new HashMap<String, String>();
			params.put("msg", t.getMessage());
			throw new QueryParseException("95011", -1, -1, params);
			// throw new QueryParseException("logger-load-fail", -1);
 		}
 	}
 }
