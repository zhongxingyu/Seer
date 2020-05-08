 package ro.satrapu.homebudget.ui.model;
 
 import javax.enterprise.inject.Model;
import org.primefaces.model.LazyDataModel;
 import ro.satrapu.homebudget.services.persistence.model.Category;
 
 /**
  *
  * @author satrapu
  */
 @Model
 public class CategoryList extends EntityList<Category> {
 
     private static final long serialVersionUID = 1L;

    @Override
    public LazyDataModel<Category> getData() {
        return super.getData();
    }
 }
