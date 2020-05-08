 package hu.palkonyves.jsfshowcase.datatable;
 
 import hu.palkonyves.jsfshowcase.business.Banana;
 import hu.palkonyves.jsfshowcase.business.BananaStore;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 @RequestScoped
 @Named
 public class StyledDatatable {
 
     @Inject
     BananaStore store;
 
     public List<Banana> getBananas() {
	return store.getAllBanana();
     }
 
    public List<String> getBananaRowClasses() {
	List<String> result = new ArrayList<String>();
	for (Banana b : getBananas()) {
	    String rowClass = getRowClass(b);
	    result.add(rowClass);
	}
 
	return result;
     }
 
     private String getRowClass(Banana b) {
	if (b.getYellowness() >= b.getBrowness()) {
	    return "yellow";
	}
	return "brown";
     }
 }
