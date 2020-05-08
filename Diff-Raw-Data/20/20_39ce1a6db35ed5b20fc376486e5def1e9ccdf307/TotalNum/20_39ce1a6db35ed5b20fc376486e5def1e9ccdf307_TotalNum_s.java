 package org.apache.hadoop.hbase.coprocessor;
 
public class TotalNum {
 	public long total;
 	public int num;
 	
 	TotalNum(){
 		num = 0;
 		total = 0;
 	}
 	
 	public void add(long t){
 		total += t;
 		num++;
 	}
 	
 	public void merge(TotalNum tn){
 		num += tn.num;
 		total += tn.total;
 	}
 }
