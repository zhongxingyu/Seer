 package name.stokito.service;
 
 import name.stokito.units.TableModel;
 
 /**
  * author: november
  * Date: 07.07.12
  */
 public interface DataBaseService {
 
	TableModel getSelect(String selectQuery);
 
 	int getFunc(String query);
 }
