 package org.apache.hadoop.hbase.coprocessor;
 
import java.io.Serializable;

public class TotalNum implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8630535750024456779L;
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
