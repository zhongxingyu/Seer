 package com.opensajux.view;
 
 import java.io.Serializable;
 import java.util.List;
 import java.util.Map;
 
import javax.enterprise.context.SessionScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import javax.jdo.Query;
 
 import org.primefaces.model.LazyDataModel;
 import org.primefaces.model.SortOrder;
 
 import com.opensajux.entity.TalkStream;
 
@SessionScoped
 @Named
 public class TalkStreamBean implements Serializable {
 	private static final long serialVersionUID = 8879513787369863264L;
 
 	@Inject
 	private transient PersistenceManagerFactory pmf;
 	private LazyDataModel<TalkStream> streamModel;
 
 	public TalkStreamBean() {
 	}
 
 	public LazyDataModel<TalkStream> getStream() {
 		if (streamModel == null) {
 			streamModel = new LazyDataModel<TalkStream>() {
 				private static final long serialVersionUID = -4213392862978853235L;
 
 				@Override
 				public int getRowCount() {
 					PersistenceManager pm = pmf.getPersistenceManager();
 					Long count = (Long) pm.newQuery("select count(key) from " + TalkStream.class.getName()).execute();
 					pm.close();
 					return count.intValue();
 				}
 
 				@Override
 				public List<TalkStream> load(int first, int pageSize, String sortField, SortOrder sortOrder,
 						Map<String, String> filters) {
 					PersistenceManager pm = pmf.getPersistenceManager();
 					Query query = pm.newQuery(TalkStream.class);
 					query.setRange(first, first + pageSize);
 					List<TalkStream> stream = (List<TalkStream>) query.execute();
 					pm.close();
 					return stream;
 				}
 			};
 			streamModel.setPageSize(5);
 		}
 		return streamModel;
 	}
 }
