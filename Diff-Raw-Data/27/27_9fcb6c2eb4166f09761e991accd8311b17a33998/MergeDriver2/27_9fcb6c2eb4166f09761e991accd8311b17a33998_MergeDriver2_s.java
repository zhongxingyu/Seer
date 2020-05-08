 import simpledb.server.*;
 import simpledb.index.*;
 import simpledb.index.hash.*;
 import simpledb.query.*;
 import simpledb.materialize.SortPlan;
 import simpledb.record.RID;
 import simpledb.record.Schema;
 import simpledb.record.TableInfo;
 import simpledb.tx.Transaction;
 import java.util.*;
 
 public class MergeDriver2{
 
    
 	public static void main(String[] args) {
 	  SimpleDB.init("studentdb"); 
 	  Transaction tx = new Transaction();
 	  Plan p = new TablePlan("messy", tx);
 	  UpdateScan s = (UpdateScan)p.open();
 	  //remove rows from table
 	  while(s.next()){
 		s.delete();
 	  }
 	  s.beforeFirst();
 	  for(int i = 0; i<40; i++){
 		s.insert();
 	    s.setInt("col1",(180-i));
 	    s.setString("col2","aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		System.out.println(180-i);
 		s.insert();
 		s.setInt("col1",(101+i));
 	    s.setString("col2","aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
		System.out.println(101+i);
 	  }
 	  s.close();
 	  List<String> fields = new ArrayList<String>();
 	  fields.add("col1");
 	  fields.add("col2");
	  SortPlan sp = new SortPlan(p, fields, tx, 3);
 	  sp.open();
 	}
 }
