 package ro.satrapu.homebudget.ui.model;
 
 import java.util.List;
 import java.util.Map;
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import org.primefaces.model.LazyDataModel;
 import org.primefaces.model.SortOrder;
 import ro.satrapu.homebudget.services.persistence.Entity;
 import ro.satrapu.homebudget.services.persistence.EntityClass;
 import ro.satrapu.homebudget.services.persistence.PersistenceService;
 
 /**
  *
  * @param <T>
  * @author satrapu
  */
 public abstract class EntityList<T extends Entity> {
 
     private static final long serialVersionUID = 1L;
     @Inject
     PersistenceService persistenceService;
     @Inject
     @EntityClass
     Class<T> entityClass;
     LazyDataModel<T> data;
 
     @PostConstruct
     public void init() {
         @SuppressWarnings("unchecked")
         final Class<T> clazz = entityClass;
         data = new LazyDataModel<T>() {
             private static final long serialVersionUID = 1L;
 
             @Override
             public List<T> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, String> filters) {
                 return persistenceService.fetch(clazz, first, pageSize);
             }

            @Override
            public void setRowIndex(int rowIndex) {
                /*
                 * The following is in ancestor (LazyDataModel):
                 * this.rowIndex = rowIndex == -1 ? rowIndex : (rowIndex % pageSize);
                 */
                if (rowIndex == -1 || getPageSize() == 0) {
                    super.setRowIndex(-1);
                } else {
                    super.setRowIndex(rowIndex % getPageSize());
                }
            }
         };
 
         data.setRowCount((int) persistenceService.count(clazz));
     }
 
     public LazyDataModel<T> getData() {
         return data;
     }
 
     public boolean isDataAvailable() {
         return data.getRowCount() > 0;
     }
 }
