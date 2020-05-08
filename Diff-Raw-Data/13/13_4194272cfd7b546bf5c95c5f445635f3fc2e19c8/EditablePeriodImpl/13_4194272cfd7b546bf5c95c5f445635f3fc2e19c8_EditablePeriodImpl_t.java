 package com.iq4j.utils.model;
 
 import java.util.Date;
 
 public abstract class EditablePeriodImpl extends PeriodImpl implements EditablePeriod {
 	
 	private static final long serialVersionUID = 3017142957499312562L;
 
 	public EditablePeriodImpl(Date min, Date max) {
 		setMin(min);
 		setMax(max);
 	}
 	
	
	
	@Override
	public abstract Date getMin();

	@Override
	public abstract Date getMax();

	@Override
	public abstract void setMin(Date min);
	
	@Override
	public abstract void setMax(Date min);
 }
