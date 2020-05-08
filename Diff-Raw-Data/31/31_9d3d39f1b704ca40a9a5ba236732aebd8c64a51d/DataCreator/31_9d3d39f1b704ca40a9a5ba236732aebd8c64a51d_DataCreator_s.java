 package com.bizosys.hsearch.treetable.example;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 
 import com.bizosys.hsearch.hbase.HDML;
 import com.bizosys.hsearch.hbase.HWriter;
 import com.bizosys.hsearch.hbase.NV;
 import com.bizosys.hsearch.hbase.RecordScalar;
 import com.bizosys.hsearch.treetable.client.HSearchProcessingInstruction;
 import com.bizosys.hsearch.treetable.client.partition.IPartition;
 import com.bizosys.hsearch.treetable.example.impl.donotmodify.*;
 import com.bizosys.hsearch.treetable.storage.HBaseTableSchemaDefn;
 
 public class DataCreator {
 	/**
 	 *  Range Query - [1:7432]
 	 *  Match All - *
 	 *  Exact match - m
 	 */
     
     public static void main(String[] args) throws Exception {
     
     	HBaseTableSchemaDefn schema = HBaseTableSchema.getInstance().getSchema();
     	String colFamily = schema.columnPartions.keySet().iterator().next();
     	
     	IPartition<Double> partition = schema.columnPartions.get(colFamily);
 		for ( String famExt : partition.getPartitionNames() ) {
        	HDML.truncate(schema.tableName, new NV(
         		(schema.columnPartions.keySet().iterator().next() + "_" + famExt) .getBytes(), "1".getBytes() ));
     	}
     	
 		Map<String, HSearchTableExamResult> tableParts = new HashMap<String, HSearchTableExamResult>();
 		
 		int [] ages = new int[] {22,23,24,25,26};
 		int agesCounter = 0;
 		
 		String [] role = new String[] {"scout","monitor","captain","student"};
 		int roleCounter = 0;
 
 		for ( int i=0; i<101; i++) {
 			
 			HSearchTableExamResult part = null;
 			
 	    	String family = partition.getColumnFamily( (double) i/10);
 	    	if (tableParts.containsKey(family)) {
 	    		part = tableParts.get(family);
 	    	} else {
 	    		part = new HSearchTableExamResult();
 	    		tableParts.put(family, part);
 	    	}
 	    	
 	    	part.put( ages[agesCounter], role[roleCounter], "classx", i, (float) i/10 );
 	    	
 	    	agesCounter++;
 	    	if ( agesCounter > 4) agesCounter = 0;
 	    	
 	    	roleCounter++;
 	    	if ( roleCounter > 3) roleCounter = 0;
 		}
 		
 		
 		List<RecordScalar> records = new ArrayList<RecordScalar>();
 		for (String family : tableParts.keySet()) {
 			System.out.println("Writting to Family :" + family);
 			records.add(  new RecordScalar (
     			"row1".getBytes(), 
     			new NV ( family.getBytes(), "1".getBytes() , tableParts.get(family).toBytes() ) ) 
 			);
 		}
 		
    	HWriter.getInstance(true).insertScalar(schema.tableName, records);
 	}
 }
