 package name.stokito.service;
 
 import name.stokito.units.TableModel;
 
import java.util.Map;

 /**
  * author: november
  * Date: 07.07.12
  */
 public interface DataBaseService {
 
	Map<String, Object> getSelect(String selectQuery);
 
 	int getFunc(String query);
 }
