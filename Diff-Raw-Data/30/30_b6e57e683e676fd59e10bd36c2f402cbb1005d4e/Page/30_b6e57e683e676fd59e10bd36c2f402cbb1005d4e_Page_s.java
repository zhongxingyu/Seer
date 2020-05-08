 package com.xlthotel.foundation.common;
 
 import java.util.List;
 
 public interface Page {
 	int getIndex();
 
 	void setIndex(int index);
 
 	int getCount();
 
 	void setCount(int count);
 	
 	List<Integer> getCurrentIndexes();
 
 	void setCurrentIndexes(List<Integer> currentIndexes);
 	
 	int getTotalCount();
 	
 	void setTotalCount(int totalCount);
 }
