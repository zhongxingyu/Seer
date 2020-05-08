 package com.pjf.mat.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import com.pjf.mat.api.AttrSysType;
 import com.pjf.mat.api.Attribute;
 import com.pjf.mat.api.Status;
 import com.pjf.mat.util.attr.IntegerAttribute;
 import com.pjf.mat.util.attr.StringAttribute;
 
 public class ElementStatus implements Status{
 	private String basisState;					// state of basis of element
 	private int el_state;						// state of element
 	private long evt_cnt;						// #evts processed by element
 	
 	public ElementStatus() {
 		this.basisState = "unknown";
 		this.el_state = 0;
 		this.evt_cnt = -1;
 	}
 
 	public ElementStatus(String basisState, int elState, long ip_evt_cnt) {
 		this.basisState = basisState;
 		this.el_state = elState;
 		this.evt_cnt = ip_evt_cnt;
 	}
 
 	public void setElementStatus(String basis_state, int el_state, long evt_cnt) {
 		this.basisState = basis_state;
 		this.el_state = el_state;
 		this.evt_cnt = evt_cnt;
 	}
 
 	@Override
 	public String getBaseState() {
 		return basisState;
 	}
 
 	@Override
 	public String getRunState() {
		return Integer.toString(el_state);
 	}
 
 	@Override
 	public long getEventInCount() {
 		return evt_cnt;
 	}
 
 	@Override
 	public Collection<Attribute> getAttributes() throws Exception {
 		Collection<Attribute> attrs = new ArrayList<Attribute>();
 		Attribute attr;
 		attr = new StringAttribute(null,"basis state",0,AttrSysType.NORMAL,0,null);
 		try {
 			attr.setValue(basisState);
 		} catch (Exception e) {
 			// dont set
 		}	
 		attrs.add(attr);
 		attr = new IntegerAttribute(null,"element state",0,AttrSysType.NORMAL,0,null);
 		try {
 			attr.setValue(Integer.toBinaryString(el_state));
 		} catch (Exception e) {
 			// dont set
 		}	
 		attrs.add(attr);
 		attr = new IntegerAttribute(null,"ip_evt_cnt",0,AttrSysType.NORMAL,0,null);
 		try {
 			attr.setValue(Long.toString(evt_cnt));
 		} catch (Exception e) {
 			// dont set
 		}	
 		attrs.add(attr);		
 		return attrs;
 	}
 
 	@Override
 	public int getRawRunState() {
 		return el_state;
 	}
 	
 	@Override
 	public String toString() {
 		return "[" + basisState + "," + el_state + "," + evt_cnt + "]";
 	}
 
 	@Override
 	public boolean isInConfigState() {
 		return basisState.equals(Status.CFG);
 	}
 
 	@Override
 	public boolean isInRunState() {
 		return basisState.equals(Status.RUN);
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result
 				+ ((basisState == null) ? 0 : basisState.hashCode());
 		result = prime * result + el_state;
 		result = prime * result + (int) (evt_cnt ^ (evt_cnt >>> 32));
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		ElementStatus other = (ElementStatus) obj;
 		if (basisState == null) {
 			if (other.basisState != null)
 				return false;
 		} else if (!basisState.equals(other.basisState))
 			return false;
 		if (el_state != other.el_state)
 			return false;
 		if (evt_cnt != other.evt_cnt)
 			return false;
 		return true;
 	}
 	
 	
 
 }
