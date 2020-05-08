 package com.duggan.workflow.client.ui.admin.formbuilder.component;
 
 import java.util.Iterator;
 
 import com.duggan.workflow.shared.model.DataType;
 import com.duggan.workflow.shared.model.form.KeyValuePair;
 import com.duggan.workflow.shared.model.form.Property;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.i18n.client.CurrencyData;
 import com.google.gwt.i18n.client.CurrencyList;
 import com.google.gwt.i18n.client.NumberFormat;
 
 public class CurrencyField extends NumberField{
 	
 	private static CurrencyList currencyCodeMapConstants = GWT.create(CurrencyList.class);
 	
 	public CurrencyField(){
 		super();
 		lblEl.setInnerText("Currency Field");
 		Iterator<CurrencyData> iter=  currencyCodeMapConstants.iterator();
 		Property property = new Property(CURRENCY, "Currency", DataType.SELECTBASIC);
 		while(iter.hasNext()){
 			CurrencyData d = iter.next();
 			property.addSelectionItem(new KeyValuePair(d.getCurrencyCode(), currencyCodeMapConstants.lookupName(d.getCurrencyCode())));
 		}
 		
 		property.sort();				
 		addProperty(property);
 	}
 	
 	@Override
 	public void setValue(Object value) {
 		if(value!=null){
 			txtComponent.setValue((Double)value);
 			
 			String currencyCode = getPropertyValue(CURRENCY);
 			
 			if(currencyCode!=null){
 				CurrencyData data = currencyCodeMapConstants.lookup(currencyCode); 
 				
 				NumberFormat fmt = NumberFormat.getCurrencyFormat(data);
 			    String formatted = fmt.format((Double)value);
 			    lblReadOnly.setText(formatted);
 				
 			}else{
 				lblReadOnly.setText(value.toString());	
 			}
 		}
 
 	}
 	
 }
