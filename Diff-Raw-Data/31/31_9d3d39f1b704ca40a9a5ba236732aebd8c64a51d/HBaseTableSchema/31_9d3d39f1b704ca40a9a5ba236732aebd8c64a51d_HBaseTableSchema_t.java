 package com.bizosys.hsearch.treetable.example.impl.donotmodify;
 
 import java.io.IOException;
 import java.util.Map;
 
 import com.bizosys.hsearch.treetable.client.partition.IPartition;
 import com.bizosys.hsearch.treetable.client.partition.PartitionNumeric;
 import com.bizosys.hsearch.treetable.storage.HBaseTableSchemaCreator;
 import com.bizosys.hsearch.treetable.storage.HBaseTableSchemaDefn;
 
 public class HBaseTableSchema {
 
 	private static HBaseTableSchema singleton = null; 
 
 	public static HBaseTableSchema getInstance() throws IOException {
 		if ( null == singleton ) singleton = new HBaseTableSchema();
 		return singleton;
 	}
 	
 	public String TABLE_NAME = "htable-test";
 	private HBaseTableSchema() throws IOException {
 		
 		Map<String, IPartition> columns = HBaseTableSchemaDefn.getInstance(TABLE_NAME).columnPartions;
 		columns.put("ExamResult",new PartitionNumeric());
 		columns.get("ExamResult").setPartitionsAndRange(
 			"ExamResult",
 			"a,b,c,d,e,f,g,h,i,j",
 			"[*:1],[1:2],[2:3],[3:4],[4:5],[5:6],[6:7],[7:8],[8:9],[9:*]",
 			4);
 
 
 	}
 
 	public HBaseTableSchemaDefn getSchema() {
 		return HBaseTableSchemaDefn.getInstance(TABLE_NAME);
 	}
 	
 	public void createSchema() {
		new HBaseTableSchemaCreator().init(TABLE_NAME);
 	}
 	
 	public static void main(String[] args) throws Exception {
 		HBaseTableSchema.getInstance().createSchema();
 	}
 }
