 package com.aharwood.metrics.spring.jdbc;
 
 import com.yammer.metrics.core.MetricName;
 import com.yammer.metrics.core.MetricsRegistry;
 import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
 import java.util.concurrent.TimeUnit;
 import javax.sql.DataSource;
 import org.springframework.dao.DataAccessException;
 import org.springframework.jdbc.core.CallableStatementCallback;
 import org.springframework.jdbc.core.CallableStatementCreator;
 import org.springframework.jdbc.core.ConnectionCallback;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.PreparedStatementCallback;
 import org.springframework.jdbc.core.PreparedStatementCreator;
 import org.springframework.jdbc.core.SqlProvider;
 import org.springframework.jdbc.core.StatementCallback;
 
 public class InstrumentedJdbcTemplate extends JdbcTemplate {
 
     private static final String GROUP_NAME = "JdbcTemplate";
 
     private MetricsRegistry metricsRegistry;
 
     public InstrumentedJdbcTemplate() {
         super();
     }
 
     public InstrumentedJdbcTemplate(DataSource dataSource) {
         super(dataSource);
     }
 
     public InstrumentedJdbcTemplate(DataSource dataSource, boolean lazyInit) {
         super(dataSource, lazyInit);
     }
 
     public MetricsRegistry getMetricsRegistry() {
         return metricsRegistry;
     }
 
     public void setMetricsRegistry(MetricsRegistry metricsRegistry) {
         this.metricsRegistry = metricsRegistry;
     }
 
     @Override
     public <T> T execute(StatementCallback<T> action) throws DataAccessException {
        TimerContext timer = null;
         if (action instanceof SqlProvider) {
             SqlProvider sqlProvider = (SqlProvider) action;
             if (sqlProvider.getSql() == null) {
                 //probably a batch update.
                 timer = startTimer(new MetricName(GROUP_NAME, "execute", "StatementCallback", "batchUpdate"));
             } else {
                 timer = startTimer(sqlProvider, "execute.StatementCallback");
             }
         }
 
         try {
             return super.execute(action);
         } finally {
             if (timer != null) {
                 timer.stop();
             }
         }
     }
 
     @Override
 	public <T> T execute(PreparedStatementCreator psc, PreparedStatementCallback<T> action) throws DataAccessException {
        TimerContext timer = null;
         if (psc instanceof SqlProvider) {
             timer = startTimer((SqlProvider) psc, "execute.PreparedStatementCreator.PreparedStatementCallback");
         }
 
         try {
             return super.execute(psc, action);
         } finally {
             if (timer != null) {
                 timer.stop();
             }
         }
     }
 
     @Override
     public <T> T execute(CallableStatementCreator csc, CallableStatementCallback<T> action) throws DataAccessException {
        TimerContext timer = null;
         if (csc instanceof SqlProvider) {
             timer = startTimer((SqlProvider) csc, "callable.CallableStatementCreator.CallableStatementCallback");
         } else {
             timer = startTimer(new MetricName(GROUP_NAME, "callable", "CallableStatementCreator.CallableStatementCallback"));
         }
 
         try {
             return super.execute(csc, action);
         } finally {
             timer.stop();
         }
     }
 
     @Override
     public <T> T execute(ConnectionCallback<T> action) throws DataAccessException {
        TimerContext timer = startTimer(new MetricName(GROUP_NAME, "connectionCallback", "ConnectionCallback"));
 
         try {
             return super.execute(action);
         } finally {
             timer.stop();
         }
     }
 
    protected TimerContext startTimer(SqlProvider sqlProvider, String type) {
         return startTimer(new MetricName(GROUP_NAME, type, sqlProvider.getSql()));
     }
 
    protected TimerContext startTimer(MetricName metricName) {
         Timer timer = this.metricsRegistry.newTimer(metricName, TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
        return timer.time();
     }
 }
