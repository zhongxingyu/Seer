 package com.transcend.monitor.worker;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.TimeZone;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.slf4j.Logger;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.StopWatch;
 
 import com.amazonaws.services.cloudwatch.model.StandardUnit;
 import com.msi.tough.core.Appctx;
 import com.msi.tough.core.DateHelper;
 import com.msi.tough.core.GenericCommaObject;
 import com.msi.tough.core.QueryBuilder;
 import com.msi.tough.model.AccountBean;
 import com.msi.tough.model.monitor.DimensionBean;
 import com.msi.tough.monitor.common.MonitorConstants;
 import com.msi.tough.query.ServiceRequestContext;
 import com.msi.tough.utils.CWUtil;
 import com.msi.tough.workflow.core.AbstractWorker;
 import com.transcend.monitor.message.GetMetricStatisticsMessage.GetMetricStatisticsRequest;
 import com.transcend.monitor.message.GetMetricStatisticsMessage.GetMetricStatisticsResponse;
 import com.transcend.monitor.message.GetMetricStatisticsMessage.GetMetricStatisticsResponse.Datapoint;
 import com.transcend.monitor.message.MetricAlarmMessage.Dimension;
import com.transcend.monitor.message.MetricAlarmMessage.Statistic;
 import com.transcend.monitor.message.MetricAlarmMessage.Unit;
 
 public class GetMetricStatisticsWorker extends
     AbstractWorker<GetMetricStatisticsRequest, GetMetricStatisticsResponse> {
 
     private static final Logger logger = Appctx.getLogger(GetMetricStatisticsWorker.class
             .getName());
 
     /**
      * We need a local copy of this doWork to provide the transactional
      * annotation.  Transaction management is handled by the annotation, which
      * can only be on a concrete class.
      * @param req
      * @return
      * @throws Exception
      */
     @Transactional
     public GetMetricStatisticsResponse doWork(
             GetMetricStatisticsRequest req) throws Exception {
         logger.debug("Performing work for GetMetricStatistics.");
         return super.doWork(req, getSession());
     }
 
     /* (non-Javadoc)
      * @see com.transcend.compute.worker.AbstractWorker#doWork0(com.google.protobuf.Message)
      */
     @Override
     @Transactional
     public GetMetricStatisticsResponse
         doWork0(GetMetricStatisticsRequest request,
         ServiceRequestContext context) throws Exception {
 
         final GetMetricStatisticsResponse.Builder result =
         GetMetricStatisticsResponse.newBuilder();
         AccountBean account = context.getAccountBean();
 
         final long acctId = account.getId();
         StopWatch stopWatch = new StopWatch("GetMetricStatistics");
         stopWatch.start("Prepare");
 
         final Calendar startTime = DateHelper.
                 getCalendarFromISO8601String(request.getStartTime(),
                 TimeZone.getTimeZone("GMT"));
         Date startDate = startTime.getTime();
 
         Calendar endTime = DateHelper.getCalendarFromISO8601String(request.getEndTime(),
                 TimeZone.getTimeZone("GMT"));
 
         Session session = getSession();
         final GenericCommaObject<Long> cdim = new GenericCommaObject<Long>();
         if (request.getDimensionCount() > 0) {
             for (final Dimension dim : request.getDimensionList()) {
                 final DimensionBean db = CWUtil.getDimensionBean(session,
                         acctId, dim.getName(), dim.getValue(), false);
                 if (db == null) {
                     continue;
                 }
                 cdim.add(db.getId());
             }
         }
         if (cdim.getList().size() == 0) {
             logger.warn("Found no dimensions matching request, " +
                     "returning empty set.");
             result.setLabel(request.getMetricName());
             return result.buildPartial();
         }
         //TODO: figure out what error to return on too much data; only 1440
         // data points will be returned.
         //TODO: Limit queried data 400 instances / hr, 35 instances / 24 hr,
         // 2 instances / 2 weeks.
         Date endTick = startTime.getTime();
         while (startDate.before(endTime.getTime())) {
             endTick.setTime(endTick.getTime() + request.getPeriod() * 1000);
             QueryBuilder builder = new QueryBuilder("SELECT 1");
            if (request.getStatisticList().contains(Statistic.Sum)) {
                 builder.append(", sum(m.value) as Sum");
             }
            if (request.getStatisticList().contains(Statistic.Maximum)) {
                 builder.append(", max(m.value) as Maximum");
             }
            if (request.getStatisticList().contains(Statistic.Minimum)) {
                 builder.append(", min(m.value) as Minimum");
             }
            if (request.getStatisticList().contains(Statistic.Average)) {
                 builder.append(", avg(m.value) as Average");
             }
            if (request.getStatisticList().contains(Statistic.SampleCount)) {
                 builder.append(", count(m.value) as SampleCount");
             }
             builder.append(", min(m.timestamp) as Timestamp");
             builder.append("from MeasureBean m").
                 append("inner join m.dimensions as d").
                 greaterThan("m.timestamp", startDate).
                 lessThanOrEqual("m.timestamp", endTick).
                 equals("m.name",  request.getMetricName()).
                 in("d.id",  cdim.toList()).
                 append("GROUP BY m.name");
             Query query = builder.toQuery(session);
             @SuppressWarnings("unchecked")
             final List<Object[]> rows = query.list();
             int index = 1;
             for (final Object[] row : rows) {
                 final Datapoint.Builder dp = Datapoint.newBuilder();
                if (request.getStatisticList().contains(Statistic.Sum)) {
                     dp.setSum(row[index] != null ? (Double) row[index++] : 0.0);
                 }
                if (request.getStatisticList().contains(Statistic.Maximum)) {
                     dp.setMaximum(row[index] != null ? (Double) row[index++] : 0.0);
                 }
                if (request.getStatisticList().contains(Statistic.Minimum)) {
                     dp.setMinimum(row[index] != null ? (Double) row[index++] : 0.0);
                 }
                if (request.getStatisticList().contains(Statistic.Average)) {
                     dp.setAverage(row[index] != null ? (Double) row[index++] : 0.0);
                 }
                if (request.getStatisticList().contains(Statistic.SampleCount)) {
                     Long val = (Long) row[index++];
                     val = val != null? val : 0;
                     dp.setSampleCount((double) val);
                 }
                 dp.setUnit(getUnitForMetric(request.getMetricName()));
                 if (! request.hasUnit()) {
                     // TODO: scale/translate unit
                     dp.setUnit(request.getUnit());
                 }
                 String timestamp = DateHelper.
                         getISO8601Date((java.util.Date)row[index]);
                 dp.setTimestamp(timestamp);
                 result.addDataPoint(dp);
             }
             startDate = new Date(endTick.getTime()+1);
         }
         stopWatch.stop();
         stopWatch.start("Calculate");
         result.setLabel(request.getMetricName());
         if (logger.isDebugEnabled()) {
             logger.debug("SW:" + stopWatch.prettyPrint());
         }
         return result.buildPartial();
     }
 
     private Unit getUnitForMetric(String metric) {
         if (metric == MonitorConstants.NETWORK_IN_COMMAND) {
             return Unit.valueOf(StandardUnit.Bytes.name());
         }
         else if (metric == MonitorConstants.NETWORK_OUT_COMMAND) {
             return Unit.valueOf(StandardUnit.Bytes.name());
         }
         else if (metric == MonitorConstants.CPU_UTILIZATION_COMMAND) {
             return Unit.valueOf(StandardUnit.Percent.name());
         }
         else if (metric == MonitorConstants.DISK_READ_BYTES_COMMAND) {
             return Unit.valueOf(StandardUnit.Bytes.name());
         }
         else if (metric == MonitorConstants.DISK_WRITE_BYTES_COMMAND) {
             return Unit.valueOf(StandardUnit.Bytes.name());
         }
         else if (metric == MonitorConstants.DISK_READ_BYTES_COMMAND) {
             return Unit.valueOf(StandardUnit.Count.name());
         }
         else if (metric == MonitorConstants.DISK_WRITE_OPS_COMMAND) {
             return Unit.valueOf(StandardUnit.Count.name());
         }
         return Unit.None;
     }
 }
