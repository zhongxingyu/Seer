 package com.dbstar.model;
 
 import com.dbstar.util.StringUtil;
 
 public class ReceiveEntry {
 	
 	public String Id;
 	public String Name;
 
 	public long RawProgress;
 	public long RawTotal;
 	
 	public int nProgress;
 	
 	public int ProgressUnit;
 	public String Progress;
 	public int TotalUnit;
 	public String Total;
 	
 	public String Percent;
 
 	public ReceiveEntry() {
 		ProgressUnit = StringUtil.UNIT_B;
 		TotalUnit = StringUtil.UNIT_B;
 	}
 
	public void ConverSize() {
 		
 		// normalize the value
 		if (RawProgress < 0) RawProgress = 0;
 		if (RawTotal < 0) RawTotal = 0;
 		
 		float progress, total;
 
 		StringUtil.SizePair progressPair = StringUtil.formatSize(RawProgress);
 		ProgressUnit = progressPair.Unit;
 		progress = progressPair.Value;
 		
 		StringUtil.SizePair totalPair = StringUtil.formatSize(RawTotal);
 		TotalUnit = totalPair.Unit;
 		total = totalPair.Value;
 
 		Progress = StringUtil.formatFloatValue(progress) + StringUtil.getUnitString(ProgressUnit);
 		Total = StringUtil.formatFloatValue(total) + StringUtil.getUnitString(TotalUnit);
 		float f = (float)RawProgress/RawTotal * 100.f;
 		nProgress = (int)f;
 		Percent = String.valueOf(StringUtil.formatFloatValue(f)) + "%";
 	}
 
 }
