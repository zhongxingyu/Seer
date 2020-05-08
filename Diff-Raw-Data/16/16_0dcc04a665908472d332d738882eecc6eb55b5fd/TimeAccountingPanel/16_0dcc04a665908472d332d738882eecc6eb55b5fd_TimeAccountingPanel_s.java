 package edu.nrao.dss.client;
 
 import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
 import com.extjs.gxt.ui.client.Style.VerticalAlignment;
 import com.extjs.gxt.ui.client.event.BaseEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.Listener;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.LayoutContainer;
 import com.extjs.gxt.ui.client.widget.form.Field;
 import com.extjs.gxt.ui.client.widget.form.FieldSet;
 import com.extjs.gxt.ui.client.widget.form.FormPanel;
 import com.extjs.gxt.ui.client.widget.form.NumberField;
 import com.extjs.gxt.ui.client.widget.form.TextArea;
 import com.extjs.gxt.ui.client.widget.form.Validator;
 import com.extjs.gxt.ui.client.widget.layout.FitLayout;
 import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
 import com.extjs.gxt.ui.client.widget.layout.FormData;
 import com.extjs.gxt.ui.client.widget.layout.RowLayout;
 import com.extjs.gxt.ui.client.widget.layout.TableData;
 import com.extjs.gxt.ui.client.widget.layout.TableLayout;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.NumberFormat;
 
 public class TimeAccountingPanel extends ContentPanel { 
 	
     protected NumberField scheduled = new NumberField();
     protected NumberField timeBilled = new NumberField();
     protected NumberField observed = new NumberField();
     protected NumberField unaccounted = new NumberField();
     protected NumberField notBillable = new NumberField();
     protected NumberField shortNotice = new NumberField();
     protected NumberField lt = new NumberField();
     protected NumberField ltw = new NumberField();
     protected NumberField ltr = new NumberField();
     protected NumberField lto = new NumberField();
     protected NumberField lp = new NumberField();
     protected NumberField os = new NumberField();
     protected NumberField osw = new NumberField();
     protected NumberField osr = new NumberField();
     protected NumberField oso = new NumberField();
     protected TextArea desc = new TextArea();
     
     protected int fieldWidth = 55;
     protected int fieldHeight = 20;
     
     protected FormData fd = new FormData(fieldWidth, fieldHeight);
     
 	public TimeAccountingPanel() {
 		initLayout();
 	}
 	
 	protected void initLayout() {
 		
 		setLayout(new RowLayout());
 		setCollapsible(true); 
 		setHeaderVisible(true);
 		//setHeading("Time Accounting");
 		setBorders(true);
 		//setAutoHeight(true);
 		//setHeight(920);
 		// first row - a 3 column table for times
 		LayoutContainer row1 = new LayoutContainer();
 		TableLayout row1table = new TableLayout(4);
 		row1table.setWidth("100%");
 		row1table.setBorder(1);
 		row1.setBorders(true);
 		row1.setLayout(row1table);
 		TableData td = new TableData();
 		td.setVerticalAlign(VerticalAlignment.TOP);
 		td.setHorizontalAlign(HorizontalAlignment.CENTER);
 
 		// 1st column of first row is a form for misc. times
 		FormPanel miscTimes = new FormPanel();
 		miscTimes.setHeaderVisible(true);
 		miscTimes.setHeading("Times (Hrs)");
 		miscTimes.setBodyBorder(false);
 		
 		// start adding the misc. times
 		miscTimes.add(scheduled, fd);
 		miscTimes.add(observed, fd);
 		miscTimes.add(timeBilled, fd);
 		miscTimes.add(notBillable, fd);
 		miscTimes.add(lp, fd);
 		miscTimes.add(shortNotice, fd);
 		miscTimes.add(unaccounted, fd);
 		
 		row1.add(miscTimes, td);
 		
         // lost time is the middle column in the first row 
         FormPanel lostTimePanel = new FormPanel();
         lostTimePanel.setHeading("Lost Time (Hrs)");
         lostTimePanel.setHeaderVisible(true);
         lostTimePanel.setBodyBorder(false);
         
 		lostTimePanel.add(lt, fd);
 		lostTimePanel.add(ltw, fd);
 		lostTimePanel.add(ltr, fd);
 		lostTimePanel.add(lto, fd);
 		
 		row1.add(lostTimePanel, td);
 		
 		// time to other session to right in second row
         FormPanel otherTimePanel = new FormPanel();
         otherTimePanel.setHeading("Time to Other Session (Hrs)");
         otherTimePanel.setHeaderVisible(true);
         otherTimePanel.setBodyBorder(false);
         
 		otherTimePanel.add(os, fd);
 		otherTimePanel.add(osw, fd);
 		otherTimePanel.add(osr, fd);
         otherTimePanel.add(oso, fd);
 		
 		row1.add(otherTimePanel, td);
 		
 		add(row1);
 
 		// second row is just for the description for now
 		LayoutContainer row2 = new LayoutContainer();
 		row2.setLayout(new FlowLayout());
 		row2.setBorders(true);
 		//row2.setWidth("100%");
 		//row2.setHeight("100%");
 		
 		FormPanel miscTimes2 = new FormPanel();
 		miscTimes2.setHeaderVisible(false);
 		miscTimes2.setHeading("Times (Hrs)");
 		
 		//TextArea desc = new TextArea();
 		//desc.setFieldLabel("Description");
 		
 		miscTimes2.add(desc, new FormData(600, 50));	
         
 		row2.add(miscTimes2, td);
 		
 		add(row2);
 		
 		setFieldAttributes();
 	}	
 	
 	protected void setFieldAttributes() {
 		setDefaultField("Scheduled", scheduled);
 		setDefaultField("Not Billable", notBillable);
 		setDefaultField("Short Notice", shortNotice);
 		setDefaultField("Time Billed", timeBilled);
 		setDefaultField("LT Bill Proj", lp);
 		setDefaultField("Unaccounted", unaccounted);
 		setDefaultField("Observed", observed);
 		
 
 		
 		setDefaultField("Lost Time", lt);
 		setDefaultField("LT Weather", ltw);
 		setDefaultField("LT RFI", ltr);
 		setDefaultField("LT Other", lto);
 		
 		setDefaultField("Other Session", os);
 		setDefaultField("OS Weather", osw);
 		setDefaultField("OS RFI", osr);
 		setDefaultField("OS Other", oso);
 
 		desc.setFieldLabel("Description");
 	    // remind the user that they've changed a value		
 		desc.addListener(Events.Blur, new Listener<BaseEvent>() {
 			@Override
 			public void handleEvent(BaseEvent be) {
 	            String value = ((TextArea) be.getSource()).getValue();
 	            String orgvl = ((TextArea) be.getSource()).getOriginalValue();
 	            String color = (orgvl.compareTo(value) == 0) ? "black" : "red";
            	((TextArea) be.getSource()).el().firstChild().setStyleAttribute("color", color);
 			}			
     	});		
 	}
 	
 	private void setDefaultField(String label, NumberField nf) {
 		nf.setFieldLabel(label);
 		nf.setReadOnly(true);
 		// TODO: read only fields should just have their background color darkened!
 		nf.setFormat(NumberFormat.getFormat("#0.00"));
 		nf.setValidator(new DSSTimeValidator()); 
         // remind the user that they've changed a value		
 		nf.addListener(Events.Blur, new Listener<BaseEvent>() {
 			@Override
 			public void handleEvent(BaseEvent be) {
 	            double value = ((NumberField) be.getSource()).getValue().doubleValue();
 	            double orgvl = ((NumberField) be.getSource()).getOriginalValue().doubleValue();
 	            String color = (orgvl == value) ? "black" : "red";
             	((NumberField) be.getSource()).el().firstChild().setStyleAttribute("color", color);
 			}			
     	});
 	}
 	
 	protected void setEditable(NumberField nf) {
 		//TODO: set background color to grey?
 		nf.setReadOnly(false);
 	}
 	
 	public void setDescription(String value) {
 		if (value == null) {
 			value = "";
 		}
 	    // we will reset the state as well
 		desc.setValue(value);
 		desc.setOriginalValue(value);
		desc.el().firstChild().setStyleAttribute("color", "black");
 	}
 	
 	public String getDescription() {
 		return (desc.getValue() == null) ? "" : desc.getValue();
 	}
 }
