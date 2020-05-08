 package org.vamdc.portal.session.queryBuilder.fields;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.event.ValueChangeEvent;
 import javax.faces.model.SelectItem;
 
 import org.vamdc.dictionary.Restrictable;
 import org.vamdc.portal.session.queryBuilder.unitConv.Converter;
 import org.vamdc.portal.session.queryBuilder.unitConv.CustomConverters;
 import org.vamdc.portal.session.queryBuilder.unitConv.UnitConverter;
 
 /**
  * Field that proxies several range fields and does intercorversion into the primary one
  * @author doronin
  *
  */
 public class ProxyRangeField extends UnitConvRangeField{
 
 	private static final long serialVersionUID = -7871244183813395761L;
 	private List<UnitConvRangeField> proxyFields = new ArrayList<UnitConvRangeField>();
 	private List<Converter> fieldConverters = new ArrayList<Converter>();
 	private int selectedField = 0;
 
 	public ProxyRangeField(Restrictable keyword, String title, UnitConverter converter) {
 		super(keyword, title, converter);
 		addProxyField(this,CustomConverters.Direct());
 	}
 
 	public void addProxyField(UnitConvRangeField field, Converter fieldConverter){
 		proxyFields.add(field);
 		fieldConverters.add(fieldConverter);
 		options.add(new SelectItem(proxyFields.indexOf(field),field.getTitle(),field.getDescription()));
 
 	}
 
 	@Override
 	public String getView() { return "/xhtml/query/fields/proxyRangeField.xhtml"; }
 
 	//Display options for the field selection
 	private List<SelectItem> options = new ArrayList<SelectItem>();
 	public List<SelectItem> getOptions() {
 		return options;
 	}
 
 	public int getSelectedField() {	return selectedField; }
 	public void setSelectedField(int selectedField) { this.selectedField = selectedField; }
 
 	public void selectField(ValueChangeEvent event){
 		System.out.println(selectedField);
 		this.fixCompareOrder();
 	}
 
 	@Override
 	public void setUserHiValue(String userHiValue) {
 		super.setUserHiValue(userHiValue);
 		if (selectedField!=0){
 			UnitConvRangeField proxied = proxyFields.get(selectedField);
 			Converter conv = fieldConverters.get(selectedField);
 			proxied.setUserHiValue(userHiValue);
			this.setHiValue(
					conv.convert(Double.valueOf(proxied.getHiValue())).toString()
					);
 		};
 
 	}
 
 	@Override
 	public void setUserLoValue(String userLoValue) { 
 		super.setUserLoValue(userLoValue);
 		if (selectedField!=0){
 			UnitConvRangeField proxied = proxyFields.get(selectedField);
 			Converter conv = fieldConverters.get(selectedField);
 			proxied.setUserLoValue(userLoValue);
			this.setLoValue(
					conv.convert(Double.valueOf(proxied.getLoValue())).toString()
					);
 		};
 	}
 
 
 	@Override
 	public void update(ValueChangeEvent event){
 		if (selectedField>0)
 			proxyFields.get(selectedField).update(event); 
 		else
 			super.update(event);
 	}
	
 	@Override
 	public UnitConverter getConverter(){
 		if (selectedField>0)
 			return proxyFields.get(selectedField).getConverter();
 		else 
 			return super.getConverter();
 	}
 
 
 
 }
