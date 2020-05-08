 package edu.nrao.dss.client;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.List;
 
 import com.extjs.gxt.ui.client.Style.Orientation;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.Style.VerticalAlignment;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.NumberField;
 import com.extjs.gxt.ui.client.widget.form.TextArea;
 import com.extjs.gxt.ui.client.widget.layout.RowData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.extjs.gxt.ui.client.widget.layout.TableData;
 import com.extjs.gxt.ui.client.widget.layout.TableLayout;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.json.client.JSONObject;
 
 public class PeriodTimeAccountPanel extends TimeAccountingPanel {
 	
 	private Period period;
 	private TimeAccounting parent; // for callbacks
 	
 //	protected void initLayout() {
 //	    super.initLayout();	
 //	    
 //	    // add the save bar below the description 
 //	}
 	
 	public void setPeriod(Period p) {
 		period = p;
 		setValues(p);
 	}
 	
 	private void setValues(Period p) {
 		GWT.log("PeriodTimeAccountPanel.setValues", null);
 		if (p != null) {
 			setValue(scheduled, p.getScheduled());
 			setValue(observed, p.getObserved());
 			setValue(timeBilled, p.getBilled());
 			setValue(unaccounted, p.getUnaccounted());
 			setValue(notBillable, p.getNot_billable());
 			setValue(shortNotice, p.getShort_notice());
 			setValue(lt, p.getLost_time());
 			setValue(ltw, p.getLost_time_weather());
 			setValue(ltr, p.getLost_time_rfi());
 			setValue(lto, p.getLost_time_other());
 			setValue(os, p.getOther_session());
 			setValue(osw, p.getOther_session_weather());
 			setValue(osr, p.getOther_session_rfi());
 			setValue(oso, p.getOther_session_other());
			//setValue(desc, p.getDescription());
			desc.setValue(p.getDescription());
			desc.setOriginalValue(p.getDescription());
 		}
 	}
 	
 	public void setValue(NumberField nf, double value) {
 		// getting the fields in sync w/ the database should be reflected 
 		// in the fields state
 	    nf.setValue(value);
 	    nf.setOriginalValue(value);
 	    if (!nf.isReadOnly()) {
 	    	nf.setStyleAttribute("color", "black");
 	    }	
 	}
 	
 	public void sendUpdates() {
 		
 		if (period == null) {
 			GWT.log("sendUpdates has null period", null);
 			return;
 		}
 		
 		// send all the non-derived values to the server so that time accounting
 		// for the entire project can be updated
 		// 1. update the period object
         GWT.log("setting period values", null);
         
 		period.setDescription(getDescription());
 		period.setScheduled(scheduled.getValue().doubleValue());
 		period.setNot_billable(notBillable.getValue().doubleValue());
 		period.setShort_notice(shortNotice.getValue().doubleValue());
 		period.setLost_time_weather(ltw.getValue().doubleValue());
 		period.setLost_time_rfi(ltr.getValue().doubleValue());
 		period.setLost_time_other(lto.getValue().doubleValue());
 		period.setOther_session_weather(osw.getValue().doubleValue());
 		period.setOther_session_rfi(osr.getValue().doubleValue());
 		period.setOther_session_other(oso.getValue().doubleValue());
 		
 		// 2. convert this info to JSON like stuff
 		HashMap <String, Object> keys = period.toHashMap();
 		GWT.log("setting keys", null);
 		
 		// 3. send the json
 		JSONRequest.post("/period/" + Integer.toString(period.getId()) + "/time_accounting", keys,
 				new JSONCallbackAdapter() {
 					public void onSuccess(JSONObject json) {
 						GWT.log("periods/time_accounting onSuccess", null);
                         // TODO: now get all the project level time accounting again, since
 						// it all may have changed.
 						if (parent != null) {
 							GWT.log("calling parent.setTimeAcctFromJSON", null);
 							parent.setTimeAccountingFromJSON(json);
 						}
 						// now, make sure we update ourselves
 						if (period != null) {
 							updatePeriodForm(period.getId());
 						}	
 						
 					}
 				});		
 	}
 	
     public void updatePeriodForm(int periodId) {
     	// get this period from the server and populate the form
         GWT.log("updatePeriodForm", null);
         // TODO - should pick up timezone from Schedule
     	JSONRequest.get("/periods/UTC/" + Integer.toString(periodId)
     		      , new JSONCallbackAdapter() {
     		public void onSuccess(JSONObject json) {
             	// JSON period -> JAVA period
              	Period period = Period.parseJSON(json.get("period").isObject());
              	setPeriod(period);
                 GWT.log("period onSuccess", null);          
     		}
     	});    	
     }
     
 	public void setParent(TimeAccounting p) {
 		parent = p;
 	}
 	
 	protected void setFieldAttributes() {
 		
 		super.setFieldAttributes();
 		
 		// they almost all can be set!
 		setEditable(scheduled);
 		setEditable(notBillable);
 		setEditable(shortNotice);
 		setEditable(ltw);
 		setEditable(ltr);
 		setEditable(lto);
 		setEditable(osw);
 		setEditable(osr);
 		setEditable(oso);
 		
 	}
 }
 
