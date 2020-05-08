 package com.akjava.lib.common.form;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.akjava.lib.common.functions.LabelAndValueDto;
 import com.akjava.lib.common.functions.SplitLineFunction;
 import com.akjava.lib.common.tag.LabelAndValue;
 import com.akjava.lib.common.tag.Tag;
 import com.akjava.lib.common.tag.TagBuilder;
 import com.akjava.lib.common.utils.TagUtil;
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 
 
 public class FormFieldDataDto {
 	private static final CsvToFormFieldFunction csvToFormFieldFunctionOptionWithNumber=new CsvToFormFieldFunction(true);
 	private static final CsvToFormFieldFunction csvToFormFieldFunction=new CsvToFormFieldFunction(false);
 	//TODO convert list
 	
 	public  static CsvToFormFieldFunction getCsvToFormFieldFunction(boolean withNumber){
 		if(withNumber){
 			return csvToFormFieldFunctionOptionWithNumber;
 		}else{
 			return csvToFormFieldFunction;
 		}
 	}
 
 	
 	public static Map<String,String> formDataToMap(FormFieldData data){
 		Map<String,String> hashMap=new LinkedHashMap<String, String>();
 		hashMap.put("name", data.getName());
 		hashMap.put("key", data.getKey());
 		hashMap.put("type", FormFieldData.getTypeByNumber(data.getType()));
 		hashMap.put("optionValues", LabelAndValueDto.labelAndValueToString(data.getOptionValues()));
 		hashMap.put("defaultValue", data.getDefaultValue());
 		hashMap.put("createAuto", data.isCreateAuto()?"yes":"");
 		hashMap.put("validators", ValidatorDto.validatorListToNamesLine(data.getValidators()));
 		hashMap.put("modifiers", ModifierDto.modifierListToNamesLine(data.getModifiers()));
 		hashMap.put("placeHolder", data.getPlaceHolder());
 		hashMap.put("comment", data.getComment());
 		return hashMap;
 	}
 	
 	
 
 	public static class  OptionLabelToValueFunction implements Function<String,String >{
 		private List<LabelAndValue> optionValues;
 		public OptionLabelToValueFunction(List<LabelAndValue> optionValues){
 			this.optionValues=optionValues;
 		}
 		@Override
 		public String apply(String label) {
 			
 			for(LabelAndValue lv:optionValues){
 				if(lv.getPrintValue().equals(label)){
 					return lv.getValue();
 				}
 			}
 			return null;
 		}
 	}
 	
 	public static class  OptionValueToLabelFunction implements Function<String,String >{
 		private List<LabelAndValue> optionValues;
 		public OptionValueToLabelFunction(List<LabelAndValue> optionValues){
 			this.optionValues=optionValues;
 		}
 		@Override
 		public String apply(String value) {
 			
 			for(LabelAndValue lv:optionValues){
 				if(lv.getValue().equals(value)){
 					return lv.getPrintValue();
 				}
 			}
 			return null;
 		}
 	}
 	
 	public static FormFieldToNameFunction getFormFieldToNameFunction(){
 		return FormFieldToNameFunction.INSTANCE;
 	}
 	public enum  FormFieldToNameFunction implements Function<FormFieldData,String >{
 		INSTANCE;
 		@Override
 		public String apply(FormFieldData data) {
 			// TODO Auto-generated method stub
 			return data.getName();
 		}
 	}
 	
 	
 	
 	
 	public static FormFieldToHiddenTagFunction getFormFieldToHiddenTagFunction(){
 		return FormFieldToHiddenTagFunction.INSTANCE;
 	}
 	public enum  FormFieldToHiddenTagFunction implements Function<FormFieldData,Tag>{
 		INSTANCE;
 		@Override
 		public Tag apply(FormFieldData data) {
 			Tag tag=null;
 			tag=TagBuilder.createHidden(data.getKey(), null);
 			return tag;
 		}
 	}
 	/*
 	public static FormFieldToEditTagFunction getFormFieldToEditTagFunction(){
 		return FormFieldToEditTagFunction.INSTANCE;
 	}
 	
 	public enum  FormFieldToEditTagFunction implements Function<FormFieldData,Tag>{
 		INSTANCE;
 		@Override
 		public Tag apply(FormFieldData data) {
 			Tag tag=null;
 			if(data.getType()==FormFieldData.TYPE_TEXT_LONG){
 				tag=TagBuilder.createTextArea(data.getKey(), null);
 			}else if(data.getType()==FormFieldData.TYPE_ID){//usually ignore it
 				tag=TagBuilder.createText(data.getKey(), null);
 			}else if(data.getType()==FormFieldData.TYPE_CHECK){
 				tag=TagBuilder.createCheckbox(data.getKey(), null,false);
 			}else if(data.getType()==FormFieldData.TYPE_SELECT_SINGLE){
 				tag=TagBuilder.createSelect(data.getKey(), data.getOptionValues(), false);
 			}else if(data.getType()==FormFieldData.TYPE_SELECT_MULTI){
 				tag=TagBuilder.createSelect(data.getKey(), data.getOptionValues(), true);
 			}else if(data.getType()==FormFieldData.TYPE_CREATE_DATE){//usually ignore it
 				tag=TagBuilder.createText(data.getKey(), null);
 			}else if(data.getType()==FormFieldData.TYPE_CREATE_USER){//usually ignore it
 				tag=TagBuilder.createText(data.getKey(), null);
 			}else{
 				//default text
 				tag=TagBuilder.createText(data.getKey(), null);
 			}
 			return tag;
 		}
 	}
 	*/
 	
 	public static FormFieldToInputTagFunction getFormFieldToInputTagFunction(){
 		return FormFieldToInputTagFunction.INSTANCE;
 	}
 	public enum  FormFieldToInputTagFunction implements Function<FormFieldData,Tag>{
 		INSTANCE;
 		@Override
 		public Tag apply(FormFieldData data) {
 			Tag tag=null;
 			if(data.getType()==FormFieldData.TYPE_TEXT_LONG){
 				tag=TagBuilder.createTextArea(data.getKey(), null);
 			}else if(data.getType()==FormFieldData.TYPE_ID){//usually ignore it
 				tag=TagBuilder.createText(data.getKey(), null);
 			}else if(data.getType()==FormFieldData.TYPE_CHECK){
 				tag=TagBuilder.createCheckbox(data.getKey(), null,false);
 			}else if(data.getType()==FormFieldData.TYPE_SELECT_SINGLE){
 				tag=TagBuilder.createSelect(data.getKey(), data.getOptionValues(), false);
 			}else if(data.getType()==FormFieldData.TYPE_SELECT_MULTI){
 				tag=TagBuilder.createSelect(data.getKey(), data.getOptionValues(), true);
 			}else if(data.getType()==FormFieldData.TYPE_CREATE_DATE){//usually ignore it
 				tag=TagBuilder.createText(data.getKey(), null);
 			}else if(data.getType()==FormFieldData.TYPE_CREATE_USER){//usually ignore it
 				tag=TagBuilder.createText(data.getKey(), null);
 			}else{
 				//default text
 				tag=TagBuilder.createText(data.getKey(), null);
 			}
 			return tag;
 		}
 	}
 	
 
 	public static class  FormFieldToHiddenTagWithValueFunction implements Function<FormFieldData,Tag>{
 		private Map<String,String> valueMap;
 		public FormFieldToHiddenTagWithValueFunction(Map<String,String> valueMap){
 			this.valueMap=valueMap;
 		}
 		@Override
 		public Tag apply(FormFieldData data) {
 			Tag tag=null;
 			tag=TagBuilder.createHidden(data.getKey(), valueMap.get(data.getKey()));
 			return tag;
 		}
 	}
 	public static FormFieldToInputTemplateTagFunction getFormFieldToInputTemplateTagFunction(){
 		return FormFieldToInputTemplateTagFunction.INSTANCE;
 	}
 	public static enum  FormFieldToInputTemplateTagFunction implements Function<FormFieldData,Tag>{
 		INSTANCE;
 		@Override
 		public Tag apply(FormFieldData data) {
 			Tag tag=null;
 			if(data.getType()==FormFieldData.TYPE_TEXT_LONG){
 				tag=TagBuilder.createTextArea(data.getKey(), "${value_"+data.getKey()+"}");
 			}else if(data.getType()==FormFieldData.TYPE_ID){//usually ignore it
 				tag=TagBuilder.createText(data.getKey(), "${value_"+data.getKey()+"}");
 			}else if(data.getType()==FormFieldData.TYPE_CHECK){
 				
 				tag=TagBuilder.createCheckbox(data.getKey(), null,false);
 				tag.setAttribute("checked","${checked_"+data.getKey()+"}");
 			}else if(data.getType()==FormFieldData.TYPE_SELECT_SINGLE){
 				tag=TagBuilder.createSelect(data.getKey(), null, false);
 				int index=0;
 				for(LabelAndValue lv:data.getOptionValues()){
 					Tag option=new Tag("option");
 					String value=lv.getValue();
 					String label=lv.getLabel();
 					if(label!=null){
 						option.setText(label);
 						option.setAttribute("value", value);
 					}else{
 						option.setText(value);
 					}
					option.setAttribute("selected", "${selected_"+data.getKey()+index+"}");
 					index++;
 					tag.addChild(option);
 				}
 			}else if(data.getType()==FormFieldData.TYPE_SELECT_MULTI){
 				tag=TagBuilder.createSelect(data.getKey(), null, true);
 				int index=0;
 				for(LabelAndValue lv:data.getOptionValues()){
 					Tag option=new Tag("option");
 					String value=lv.getValue();
 					String label=lv.getLabel();
 					if(label!=null){
 						option.setText(label);
 						option.setAttribute("value", value);
 					}else{
 						option.setText(value);
 					}
 					option.setAttribute("selected", "${selected_"+data.getKey()+index+"}");
 					index++;
 					tag.addChild(option);
 				}
 			}else if(data.getType()==FormFieldData.TYPE_CREATE_DATE){//usually ignore it
 				tag=TagBuilder.createText(data.getKey(), "${value_"+data.getKey()+"}");
 			}else if(data.getType()==FormFieldData.TYPE_CREATE_USER){//usually ignore it
 				tag=TagBuilder.createText(data.getKey(), "${value_"+data.getKey()+"}");
 			}else{
 				//default text
 				tag=TagBuilder.createText(data.getKey(), "${value_"+data.getKey()+"}");
 			}
 			return tag;
 		}
 	}
 	
 	public static class  FormFieldToInputEditTagFunction implements Function<FormFieldData,Tag>{
 		private Map<String,String> valueMap;
 		public FormFieldToInputEditTagFunction(Map<String,String> valueMap){
 			this.valueMap=valueMap;
 		}
 		@Override
 		public Tag apply(FormFieldData data) {
 			Tag tag=null;
 			if(data.getType()==FormFieldData.TYPE_TEXT_LONG){
 				tag=TagBuilder.createTextArea(data.getKey(), valueMap.get(data.getKey()));
 			}else if(data.getType()==FormFieldData.TYPE_ID){//usually ignore it
 				tag=TagBuilder.createText(data.getKey(), valueMap.get(data.getKey()));
 			}else if(data.getType()==FormFieldData.TYPE_CHECK){
 				String value=valueMap.get(data.getKey());
 				boolean checked=false;
 				if(value!=null && value.equals("on")){
 					checked=true;
 				}
 				tag=TagBuilder.createCheckbox(data.getKey(), null,checked);
 			}else if(data.getType()==FormFieldData.TYPE_SELECT_SINGLE){
 				String value=valueMap.get(data.getKey());
 				
 				List<LabelAndValue> lvalues=new ArrayList<LabelAndValue>();
 				for(LabelAndValue lv:data.getOptionValues()){
 					LabelAndValue cloned=lv.clone();
 					if(cloned.getValue().equals(value)){
 						cloned.setSelected(true);
 					}else{
 						cloned.setSelected(false);
 					}
 					lvalues.add(cloned);
 				}
 				tag=TagBuilder.createSelect(data.getKey(), lvalues, false);
 			}else if(data.getType()==FormFieldData.TYPE_SELECT_MULTI){
 				String value=valueMap.get(data.getKey());
 				List<String> valueList=new ArrayList<String>();
 				if(value!=null){
 					String[] values=value.split(":");
 					for(String v:values){
 					valueList.add(v);
 					}
 				}
 				
 				List<LabelAndValue> lvalues=new ArrayList<LabelAndValue>();
 				for(LabelAndValue lv:data.getOptionValues()){
 					LabelAndValue cloned=lv.clone();
 					if(valueList.contains(cloned.getValue())){
 						cloned.setSelected(true);
 					}else{
 						cloned.setSelected(false);
 					}
 					lvalues.add(cloned);
 				}
 				tag=TagBuilder.createSelect(data.getKey(), lvalues, true);
 			}else if(data.getType()==FormFieldData.TYPE_CREATE_DATE){//usually ignore it
 				tag=TagBuilder.createText(data.getKey(), valueMap.get(data.getKey()));
 			}else if(data.getType()==FormFieldData.TYPE_CREATE_USER){//usually ignore it
 				tag=TagBuilder.createText(data.getKey(), valueMap.get(data.getKey()));
 			}else{
 				//default text
 				tag=TagBuilder.createText(data.getKey(), valueMap.get(data.getKey()));
 			}
 			return tag;
 		}
 	}
 	
 	
 	public static FormFieldToKeyFunction getFormFieldToKeyFunction(){
 		return FormFieldToKeyFunction.INSTANCE;
 	}
 	public enum  FormFieldToKeyFunction implements Function<FormFieldData,String >{
 		INSTANCE;
 		@Override
 		public String apply(FormFieldData data) {
 			// TODO Auto-generated method stub
 			return data.getKey();
 		}
 	}
 	
 	public static FormFieldToCsvFunction getFormFieldToCsvFunction(){
 		return FormFieldToCsvFunction.INSTANCE;
 	}
 	
 	public enum  FormFieldToCsvFunction implements Function<FormFieldData,String >{
 		INSTANCE;
 		@Override
 		public String apply(FormFieldData data) {
 			
 			Map<String,String> map=formDataToMap(data);
 			return Joiner.on("\t").useForNull("").join(map.values());
 		}
 	}
 	 static class CsvToFormFieldFunction implements Function<String, FormFieldData>{
 		private boolean optionWithNumber=true;
 		public CsvToFormFieldFunction(boolean optionWithNumber){
 			this.optionWithNumber=optionWithNumber;
 		}
 		
 		/*
 		public void setOptionWithNumber(boolean optionWithNumber) {
 			this.optionWithNumber = optionWithNumber;
 		}*/
 		@Override
 		public FormFieldData apply(String value) {
 			FormFieldData data=new FormFieldData();
 			List<String> csvs=new SplitLineFunction(true, false).apply(value);
 			
 			if(csvs.size()>0){
 				data.setName(csvs.get(0));//String
 			}
 			if(csvs.size()>1){
 				data.setKey(csvs.get(1));//String
 			}
 			
 			if(csvs.size()>2){//TODO not use number
 				int type=0;
 				String v=csvs.get(2).toLowerCase();
 				type=FormFieldData.getTypeByLabel(v);
 				data.setType(type);
 			}
 			
 			if(csvs.size()>3){
 				String optionText=csvs.get(3);
 				if(!optionText.isEmpty()){
 					if(optionWithNumber && data.getType()!=FormFieldData.TYPE_CHECK){
 						data.setOptionValues(LabelAndValueDto.lineToLabelAndValuesWithNumber(optionText));
 					}else{
 						data.setOptionValues(LabelAndValueDto.lineToLabelAndValues(optionText));
 					}
 				}
 				
 				
 			}
 			
 			if(csvs.size()>4){
 				data.setDefaultValue(csvs.get(4));
 			}
 			
 			if(csvs.size()>5){
 				String isAuto=csvs.get(5).toLowerCase();
 				if(isAuto.equals("true") || isAuto.equals("yes")){
 					data.setCreateAuto(true);
 				}
 			}
 			
 			//TODO parse validator
 			if(csvs.size()>6){
 				String validatorText=csvs.get(6);
 				if(!validatorText.isEmpty()){
 					data.setValidators(ValidatorDto.namesLineToValidatorList(validatorText));
 				}
 				
 			}
 			//
 			if(csvs.size()>7){
 				String modifierText=csvs.get(7);
 				if(!modifierText.isEmpty()){
 					data.setModifiers(ModifierDto.namesLineToModifierList(modifierText));
 				}
 			}
 			
 			if(csvs.size()>8){
 				data.setPlaceHolder(csvs.get(8));
 			}
 			
 			if(csvs.size()>9){
 				data.setComment(csvs.get(9));
 			}
 			
 			return data;
 		}
 		
 	}
 	public static String formFieldToCsv(FormFieldData field) {
 		// TODO Auto-generated method stub
 		return getFormFieldToCsvFunction().apply(field);
 	}
 
 
 	public static FormFieldData csvToFormField(String collect) {
 		// TODO Auto-generated method stub
 		return getCsvToFormFieldFunction(true).apply(collect);
 	}
 }
