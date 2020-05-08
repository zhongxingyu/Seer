 package nz.ac.vuw.ecs.rprofs.server.data;
 
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.TypedQuery;
 
 import nz.ac.vuw.ecs.rprofs.server.context.Context;
 import nz.ac.vuw.ecs.rprofs.server.context.ContextManager;
 import nz.ac.vuw.ecs.rprofs.server.domain.Dataset;
 import nz.ac.vuw.ecs.rprofs.server.request.DatasetService;
 
 import com.google.gwt.requestfactory.shared.Locator;
import com.ibm.icu.util.Calendar;
 
 public class DatasetManager extends Locator<Dataset, Long> implements DatasetService {
 
 	@Override
 	public List<Dataset> findAllDatasets() {
 		return em().createNamedQuery("allDatasets", Dataset.class).getResultList();
 	}
 
 	@Override
 	public Dataset findDataset(String handle) {
 		TypedQuery<Dataset> q = em().createNamedQuery("findDataset", Dataset.class);
 		q.setParameter("handle", handle);
 		return q.getSingleResult();
 	}
 
 	public Dataset findDataset(short id) {
 		return em().find(Dataset.class, id);
 	}
 
 	public void add(Dataset dataset) {
 		em().persist(dataset);
 	}
 
 	@Override
 	public void stopDataset(String dataset) {
 		Dataset ds = findDataset(dataset);
 
 		ds.setStopped(Calendar.getInstance().getTime());
 	}
 
 	@Override
 	public void deleteDataset(String handle) {
 		EntityManager em = em();
 
 		em.remove(findDataset(handle));
 		em.createNativeQuery("drop table if exists run_" + handle + "_classes cascade;").executeUpdate();
 		em.createNativeQuery("drop table if exists run_" + handle + "_events cascade;").executeUpdate();
 		em.createNativeQuery("drop table if exists run_" + handle + "_event_args cascade;").executeUpdate();
 		em.createNativeQuery("drop table if exists run_" + handle + "_events_args cascade;").executeUpdate();
 		em.createNativeQuery("drop table if exists run_" + handle + "_fields cascade;").executeUpdate();
 		em.createNativeQuery("drop table if exists run_" + handle + "_instances cascade;").executeUpdate();
 		em.createNativeQuery("drop table if exists run_" + handle + "_methods cascade;").executeUpdate();
 		em.createNativeQuery("drop table if exists run_" + handle + "_profiler_runs cascade;").executeUpdate();
 		em.createNativeQuery("drop table if exists run_" + handle + "_field_writes cascade;").executeUpdate();
 	}
 
 	@Override
 	public Dataset create(java.lang.Class<? extends Dataset> clazz) {
 		return new Dataset();
 	}
 
 	@Override
 	public Dataset find(java.lang.Class<? extends Dataset> clazz, Long id) {
 		return em().find(Dataset.class, id.shortValue());
 	}
 
 	@Override
 	public java.lang.Class<Dataset> getDomainType() {
 		return Dataset.class;
 	}
 
 	@Override
 	public Long getId(Dataset dataset) {
 		return (long) dataset.getId();
 	}
 
 	@Override
 	public java.lang.Class<Long> getIdType() {
 		return Long.class;
 	}
 
 	@Override
 	public Integer getVersion(Dataset dataset) {
 		return dataset.getVersion();
 	}
 
 	private EntityManager em() {
 		Context c = ContextManager.getInstance().getDefault();
 		if (!c.isOpen()) {
 			c.open();
 		}
 		return c.em();
 	}
 }
