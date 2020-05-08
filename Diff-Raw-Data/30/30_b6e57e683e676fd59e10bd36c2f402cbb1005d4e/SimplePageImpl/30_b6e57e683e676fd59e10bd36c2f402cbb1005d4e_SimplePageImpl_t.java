 package com.xlthotel.foundation.common;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 
 public class SimplePageImpl implements Page {
 	private static final int defualtIndexesSize = 7;
 	private int index;
 	private int count;
 	private int totalCount;
 	private List<Integer> currentIndexes;
 	
 	/**
 	 * 页码
 	 */
 	@Override
 	public int getIndex() {
 		return index;
 	}
 
 	@Override
 	public void setIndex(int index) {
 		if (index < 0) {
 			this.index = 0;
 		} else {
 			this.index = index;
 		}
 	}
 
 	/**
 	 * 一页多少条数据
 	 */
 	@Override
 	public int getCount() {
 		return count;
 	}
 
 	@Override
 	public void setCount(int count) {
 		if (count < 1) {
 			this.count = 1;
 		} else {
 			this.count = count;
 		}
 	}
 	
 	/**
 	 * 当前可显示的页数
 	 */
 	@Override
 	public List<Integer> getCurrentIndexes() {
 		currentIndexes = new LinkedList<Integer>();
 		int totalIndexes = (totalCount - 1) / count;
 		int lower = 0;
 		int upper = totalIndexes;
 		int currentIndexesSize = Math.min(totalIndexes + 1, defualtIndexesSize);
 		index = Math.max(0, Math.min(index, totalIndexes));
 		for (int i = 0; i < currentIndexesSize; i++) {
 			int first = index - i;
 			int last = index + i;
 			if (first < lower && last > upper) {
 				break;
 			}
 			if (i == 0) {
 				currentIndexes.add(index);
 				continue;
 			}
 			if (first >= lower && currentIndexes.size() < currentIndexesSize) {
 				currentIndexes.add(0, first);
 			}
 			if (last <= upper && currentIndexes.size() < currentIndexesSize) {
 				currentIndexes.add(currentIndexes.size(), last);
 			}
 		}
 		return currentIndexes;
 	}
 	
 	@Override
 	public void setCurrentIndexes(List<Integer> currentIndexes) {
 		if (currentIndexes == null) {
 			this.currentIndexes = Collections.emptyList();
 		} else {
 			this.currentIndexes = currentIndexes;
 		}
 	}
 
 	/**
 	 * 总数据条数
 	 */
 	@Override
 	public int getTotalCount() {
 		return totalCount;
 	}
 	
 	@Override
 	public void setTotalCount(int totalCount) {
 		if (totalCount < 0) {
 			this.totalCount = 0;
 		}
 		this.totalCount = totalCount;
 	}
	
	@Override
	public int getLastIndex() {
		return (totalCount - 1) / count;
	}
 }
