 package org.kalibro.core.persistence;
 
import static org.junit.Assert.assertEquals;
 
 import java.util.*;
 
 import javax.persistence.TypedQuery;
 
 import org.junit.Test;
 import org.kalibro.Granularity;
 import org.kalibro.MetricResult;
 import org.kalibro.Module;
 import org.kalibro.ModuleResult;
 import org.kalibro.core.persistence.record.DescendantResultRecord;
 import org.kalibro.core.persistence.record.MetricResultRecord;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 
 @PrepareForTest({MetricResultDatabaseDao.class, ModuleResult.class})
 public class MetricResultDatabaseDaoTest extends
 	DatabaseDaoTestCase<MetricResult, MetricResultRecord, MetricResultDatabaseDao> {
 
 	private static final Long ID = new Random().nextLong();
 	private static final Long TIME = new Random().nextLong();
 	private static final Date DATE = new Date(TIME);
 	private static final String METRIC_NAME = "MetricResultDatabaseDaoTest metric name";
 
 	@Test
 	public void shouldGetDescendantResults() {
 		DescendantResultRecord result = mock(DescendantResultRecord.class);
 		TypedQuery<DescendantResultRecord> descendantQuery = mock(TypedQuery.class);
 		doReturn(descendantQuery).when(dao).createQuery(
 			"SELECT d FROM DescendantResult d WHERE d.metricResult.id = :metricResultId", DescendantResultRecord.class);
 		when(descendantQuery.getResultList()).thenReturn(list(result));
 		when(result.convert()).thenReturn(42.0);
 
 		assertDeepEquals(list(42.0), dao.descendantResultsOf(ID));
 		verify(descendantQuery).setParameter("metricResultId", ID);
 	}
 
 	@Test
 	public void shouldGetMetricResultsOfModuleResult() {
 		assertDeepEquals(set(entity), dao.metricResultsOf(ID));
 
 		verify(dao).createRecordQuery("metricResult.moduleResult.id = :moduleResultId");
 		verify(query).setParameter("moduleResultId", ID);
 	}
 
 	@Test
 	public void shouldGetMetricResultHistory() throws Exception {
 		mockModuleResult("org");
 		TypedQuery<Object[]> historyQuery = mock(TypedQuery.class);
 		doReturn(historyQuery).when(dao).createQuery("SELECT processing.date, metricResult " +
 			"FROM MetricResult metricResult JOIN metricResult.moduleResult.processing processing " +
 			"WHERE metricResult.configuration.metricName = :metricName " +
 			"AND metricResult.moduleResult.moduleName = :moduleName AND processing.repository.id = " +
 			"(SELECT mor.processing.repository.id FROM ModuleResult mor WHERE mor.id = :moduleResultId)",
 			Object[].class);
 		List<Object[]> results = new ArrayList<Object[]>();
 		results.add(new Object[]{TIME, record});
 		when(historyQuery.getResultList()).thenReturn(results);
 
 		SortedMap<Date, MetricResult> history = dao.historyOf(METRIC_NAME, ID);
 		assertEquals(1, history.size());
 		assertDeepEquals(entity, history.get(DATE));
 		verify(historyQuery).setParameter("metricName", METRIC_NAME);
		verify(historyQuery).setParameter("moduleName", list("org"));
 		verify(historyQuery).setParameter("moduleResultId", ID);
 	}
 
 	private void mockModuleResult(String moduleName) throws Exception {
 		Module module = new Module(Granularity.PACKAGE, moduleName);
 		ModuleResult moduleResult = mock(ModuleResult.class);
 		ModuleResultDatabaseDao moduleResultDao = mock(ModuleResultDatabaseDao.class);
 		whenNew(ModuleResultDatabaseDao.class).withNoArguments().thenReturn(moduleResultDao);
 		when(moduleResultDao.get(ID)).thenReturn(moduleResult);
 		when(moduleResult.getModule()).thenReturn(module);
 	}
 }
