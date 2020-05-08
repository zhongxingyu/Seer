 package org.kalibro.core.persistence;
 
 import java.util.*;
 
 import javax.persistence.TypedQuery;
 
 import org.kalibro.MetricResult;
 import org.kalibro.ModuleResult;
 import org.kalibro.core.persistence.record.DescendantResultRecord;
 import org.kalibro.core.persistence.record.MetricResultRecord;
 import org.kalibro.dao.MetricResultDao;
 import org.kalibro.dto.DataTransferObject;
 
 /**
  * Database access implementation for {@link MetricResultDao}.
  * 
  * @author Carlos Morais
  */
 class MetricResultDatabaseDao extends DatabaseDao<MetricResult, MetricResultRecord> implements MetricResultDao {
 
 	MetricResultDatabaseDao() {
 		super(MetricResultRecord.class);
 	}
 
 	@Override
 	public List<Double> descendantResultsOf(Long metricResultId) {
 		TypedQuery<DescendantResultRecord> query = createQuery(
 			"SELECT d FROM DescendantResult d WHERE d.metricResult.id = :metricResultId", DescendantResultRecord.class);
 		query.setParameter("metricResultId", metricResultId);
 		return DataTransferObject.toList(query.getResultList());
 	}
 
 	@Override
 	public SortedSet<MetricResult> metricResultsOf(Long moduleResultId) {
 		TypedQuery<MetricResultRecord> query = createRecordQuery("metricResult.moduleResult.id = :moduleResultId");
 		query.setParameter("moduleResultId", moduleResultId);
 		return DataTransferObject.toSortedSet(query.getResultList());
 	}
 
 	@Override
 	public SortedMap<Date, MetricResult> historyOf(String metricName, Long moduleResultId) {
		// FIXME module result id query
 		ModuleResult moduleResult = new ModuleResultDatabaseDao().get(moduleResultId);
 		List<String> moduleName = Arrays.asList(moduleResult.getModule().getName());
		TypedQuery<Object[]> query = createQuery("SELECT proc.date, meResult FROM MetricResult meResult " +
			"JOIN meResult.moduleResult moResult JOIN meResult.configuration meConf JOIN meConf.processing proc " +
			"WHERE meConf.metricName = :metricName AND moResult.moduleName = :moduleName", Object[].class);
 		query.setParameter("metricName", metricName);
 		query.setParameter("moduleName", moduleName);
 		List<Object[]> results = query.getResultList();
 
 		SortedMap<Date, MetricResult> history = new TreeMap<Date, MetricResult>();
 		for (Object[] result : results)
 			history.put(new Date((Long) result[0]), ((MetricResultRecord) result[1]).convert());
 		return history;
 	}
 }
