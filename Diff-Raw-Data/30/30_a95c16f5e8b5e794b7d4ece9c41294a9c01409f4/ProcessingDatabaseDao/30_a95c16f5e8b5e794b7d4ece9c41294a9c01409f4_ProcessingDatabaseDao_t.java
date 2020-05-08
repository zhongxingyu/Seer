 package org.kalibro.core.persistence;
 
 import static org.kalibro.core.persistence.DatePicker.*;
 
 import java.util.Date;
 
 import javax.persistence.TypedQuery;
 
 import org.kalibro.MetricConfiguration;
 import org.kalibro.ProcessState;
 import org.kalibro.Processing;
 import org.kalibro.Repository;
 import org.kalibro.core.persistence.record.MetricConfigurationSnapshotRecord;
 import org.kalibro.core.persistence.record.ProcessingRecord;
 import org.kalibro.dao.ProcessingDao;
 
 /**
  * Database access implementation for {@link ProcessingDao}.
  * 
  * @author Carlos Morais
  */
 public class ProcessingDatabaseDao extends DatabaseDao<Processing, ProcessingRecord> implements ProcessingDao {
 
 	ProcessingDatabaseDao() {
 		super(ProcessingRecord.class);
 	}
 
 	@Override
 	public boolean hasProcessing(Long repositoryId) {
 		return exists(repositoryId, "");
 	}
 
 	@Override
 	public boolean hasReadyProcessing(Long repositoryId) {
 		return exists(repositoryId, " AND processing.state = 'READY'");
 	}
 
 	@Override
 	public boolean hasProcessingAfter(Date date, Long repositoryId) {
 		return exists(repositoryId, AFTER, date);
 	}
 
 	@Override
 	public boolean hasProcessingBefore(Date date, Long repositoryId) {
 		return exists(repositoryId, BEFORE, date);
 	}
 
 	private boolean exists(Long repositoryId, String extraCondition) {
 		return exists(ANY.existenceClause() + extraCondition, "repositoryId", repositoryId);
 	}
 
 	private boolean exists(Long repositoryId, DatePicker datePicker, Date date) {
 		return exists(datePicker.existenceClause(), "repositoryId", repositoryId, "date", date.getTime());
 	}
 
 	@Override
 	public ProcessState lastProcessingState(Long repositoryId) {
		return lastProcessing(repositoryId).getState();
 	}
 
 	@Override
 	public Processing lastReadyProcessing(Long repositoryId) {
 		return processing(repositoryId, LAST, "state = 'READY'");
 	}
 
 	@Override
 	public Processing firstProcessing(Long repositoryId) {
 		return processing(repositoryId, FIRST, "");
 	}
 
 	@Override
 	public Processing lastProcessing(Long repositoryId) {
 		return processing(repositoryId, LAST, "");
 	}
 
 	@Override
 	public Processing firstProcessingAfter(Date date, Long repositoryId) {
 		return processing(repositoryId, FIRST_AFTER, date);
 	}
 
 	@Override
 	public Processing lastProcessingBefore(Date date, Long repositoryId) {
 		return processing(repositoryId, LAST_BEFORE, date);
 	}
 
 	private Processing processing(Long repositoryId, DatePicker datePicker, String extraCondition) {
 		TypedQuery<ProcessingRecord> query = createRecordQuery(datePicker.processingClause(extraCondition));
 		query.setParameter("repositoryId", repositoryId);
 		return query.getSingleResult().convert();
 	}
 
 	private Processing processing(Long repositoryId, DatePicker datePicker, Date date) {
 		TypedQuery<ProcessingRecord> query = createRecordQuery(datePicker.processingClause());
 		query.setParameter("repositoryId", repositoryId);
 		query.setParameter("date", date.getTime());
 		return query.getSingleResult().convert();
 	}
 
 	public Processing createProcessingFor(Repository repository) {
 		ProcessingRecord record = save(new ProcessingRecord(new Processing(repository)));
 		for (MetricConfiguration configuration : repository.getConfiguration().getMetricConfigurations())
 			save(new MetricConfigurationSnapshotRecord(configuration, record));
 		return record.convert();
 	}
 
 	public void save(Processing processing) {
 		save(new ProcessingRecord(processing));
 	}
 }
