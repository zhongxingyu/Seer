 package com.akjava.gwt.webappmaker.client;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.akjava.gwt.html5.client.file.ui.FileNameAndText;
 import com.akjava.gwt.lib.client.LogUtils;
 import com.akjava.gwt.webappmaker.client.ServletDataDto.FormDataToCreateFormFieldFunction;
 import com.akjava.gwt.webappmaker.client.resources.Bundles;
 import com.akjava.lib.common.form.FormData;
 import com.akjava.lib.common.form.FormFieldData;
 import com.akjava.lib.common.form.FormFieldDataPredicates;
 import com.akjava.lib.common.param.Parameter;
 import com.akjava.lib.common.param.ParameterUtils;
 import com.akjava.lib.common.predicates.StringPredicates;
 import com.akjava.lib.common.tag.LabelAndValue;
 import com.akjava.lib.common.utils.TemplateUtils;
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.gwt.core.shared.GWT;
 
 
 public class ToolsGenerator {
 private FormData formData;
 private String packageString;
 public ToolsGenerator(FormData formData,String packageString){
 	this.formData=formData;
 	this.packageString=packageString;
 }
 @SuppressWarnings("unchecked")
 public String generateToolsText(){
 Joiner nLineJoiner=Joiner.on("\n").skipNulls();
 String base=Bundles.INSTANCE.tools().getText();
 
 Map<String,String> map=new HashMap<String, String>();
 map.put("package", packageString);
 map.put("dataClassName",formData.getClassName());
 
 map.put("dateFormat", Internationals.getMessage("dateformat"));//base date format for label
 map.put("message_has_error",Internationals.getMessage("has_error"));
 
 if(formData.getFormFieldDatas()!=null){
 	
 
 	//create toLabelValue	
 	List<String> toIsMultiValueText=Lists.newArrayList(
 	Iterables.filter(
 		     Lists.transform(formData.getFormFieldDatas(), getFormFieldDataToIsMultipleParameterpFunction()),
 		     StringPredicates.getNotEmpty()
 		  )
 		  );
 
 	map.put("isMultipleParameter",nLineJoiner.join(toIsMultiValueText));
 	
 
 
 	List<String> toLabelMapTexts=Lists.newArrayList(
 			Iterables.filter(
 				     Lists.transform(formData.getFormFieldDatas(), getFormFieldDataToToLabelMapFunction()),
 				     StringPredicates.getNotEmpty()
 				  )
 				  );
 
 			map.put("toLabelMap",nLineJoiner.join(toLabelMapTexts));
 	
 
 List<String> toLabelValueTexts=Lists.newArrayList(
 Iterables.filter(
 	     Lists.transform(formData.getFormFieldDatas(), getFormFieldDataToToLabelValueFunction()),
 	     StringPredicates.getNotEmpty()
 	  )
 	  );
 
 map.put("toLabelValue",nLineJoiner.join(toLabelValueTexts));
 
 
 List<String> toGetKeyListsTexts=Lists.newArrayList(
 Iterables.filter(
 	     Lists.transform(formData.getFormFieldDatas(), getFormFieldDataToGetKeyListsFunction()),
 	     StringPredicates.getNotEmpty()
 	  )
 	  );
 
 map.put("getKeyLists",nLineJoiner.join(toGetKeyListsTexts));
 
 
 
 //create getLabelAndValue() for EditServlet or AddServlet
 @SuppressWarnings("rawtypes")
 List<String> getLabelAndValue=Lists.newArrayList(
 Iterables.filter(
 	     Lists.transform(new ArrayList(Collections2.filter(formData.getFormFieldDatas(), FormFieldDataPredicates.getHaveLabelAndValue())), getFormFieldDataToGetLabelAndValueFunction()),
 	     StringPredicates.getNotEmpty()
 	  )
 	  );
 
 map.put("getLabelAndValue",nLineJoiner.join(getLabelAndValue));
 
 
 
 Collection<String> methods=Collections2.transform(formData.getFormFieldDatas(), new FormDataToCreateFormFieldFunction());
 String methodText=Joiner.on("\n").skipNulls().join(methods);
 
 //map.put("createFormFields", methodText);
 map.put("insertFormField", methodText);
 
 }
 
 /**
  * 
  * get value from DefaultValue column
  * text or @COOKIE(name,<defaultValue>);
  * name can write name,name+1,name+4 or name-1
  * 
  * this cookie setted at AddExec setted at Class Option column
  */
 //generate setDefaultValues inside
 Collection<String> cookies=Collections2.transform(formData.getFormFieldDatas(), getFormFieldDataToToSetDefaultValuesCookie());
 String cookieText=Joiner.on("\n").skipNulls().join(cookies);
 map.put("setDefaultValues_cookies", cookieText);
 
 Collection<String> defaults=Collections2.transform(formData.getFormFieldDatas(), getFormFieldDataToToSetDefaultValues());
 String defaultText=Joiner.on("\n").skipNulls().join(defaults);
 map.put("setDefaultValues_defaults", defaultText);
 
 return TemplateUtils.createAdvancedText(base, map);
 }
 
 
 
 public FileNameAndText createFileNameAndText(){
 	String generatorText=this.generateToolsText();
 	FileNameAndText ftext=new FileNameAndText();
 	ftext.setName(formData.getClassName()+"Tools.java");
 	ftext.setText(generatorText);
 	return ftext;
 }
 
 
 /*
 public enum ServletDataToToolsGeneratorFunction implements Function<ServletData,ToolsGenerator>{
 	;
 
 	@Override
 	public ToolsGenerator apply(ServletData data) {
 		return new ToolsGenerator(data);
 	}
 	
 }
 */
 
 public FormFieldDataToIsMultipleParameterpFunction getFormFieldDataToIsMultipleParameterpFunction(){
 	return FormFieldDataToIsMultipleParameterpFunction.INSTANCE;
 }
 public enum FormFieldDataToIsMultipleParameterpFunction implements Function<FormFieldData,String>{
 	INSTANCE
 	;
 	public static String template="if(key.equals(\"${value}\")){\n" +
 			"\t\treturn true;\n"+
 			"\t}" +
 			"\n";
 	@Override
 	public String apply(FormFieldData fdata) {
 		if(fdata.getType()==FormFieldData.TYPE_SELECT_MULTI){
 		return TemplateUtils.createText(template, fdata.getKey());
 		}else{
 			return null;
 		}
 	}
 }
 
 
 public FormFieldDataToGetLabelAndValueFunction getFormFieldDataToGetLabelAndValueFunction(){
 	return FormFieldDataToGetLabelAndValueFunction.INSTANCE;
 }
 public enum FormFieldDataToGetLabelAndValueFunction implements Function<FormFieldData,String>{
 	INSTANCE
 	;
 	public static String add_label="lvalues.add(new LabelAndValue(${label}, \"${value}\",${selected}));";
 	public static String template="if(key.equals(\"${key}\")){\n" +
 			"\tList<LabelAndValue> lvalues=new ArrayList<LabelAndValue>();\n"+
 			"\t${options}\n"+
 			"\treturn lvalues;\n"+
 			"\t}" +
 			"\n";
 	
 	@Override
 	public String apply(FormFieldData fdata) {
 		if(fdata.getOptionValues()!=null && fdata.getOptionValues().size()>0){
 			Map<String,String> map=new HashMap<String, String>();
 			if(isSpecialRelative(fdata)){
 				Parameter parameter=toSpecialRelativeParam(fdata);
 				//TODO check error
 				if(parameter.size()==0){
 					return "//invalid parameter:"+fdata.getOptionText()+",usually @DATANAME(LABEL_COLUMN:VALUE_COLUMN)ORDER";
 				}
 				
 				if(parameter.size()==1){
 					
 					LogUtils.log("parameter size is 1(first one is label,second is id-column-name), are you sure?if first param is not id,it would faild to get value after added");
 					
 				}
 				
 				String label=null;
 				String value=null;
 				if(parameter.size()==1){
 					label=parameter.get(0);
 					value=parameter.get(0);
 				}else if(parameter.size()==2){
 					label=parameter.get(0);
 					value=parameter.get(1);
 				}
 				
 				Map<String,String> pmap=new HashMap<String, String>();
 				pmap.put("label_name", label);
 				pmap.put("value_name", value);
 				pmap.put("param", fdata.getOptionText());
 				pmap.put("dataClassName", parameter.getName());
 				
 				
 				map.put("key", fdata.getKey());
 				map.put("options", TemplateUtils.createAdvancedText(Bundles.INSTANCE.getlabelandvalue_relative().getText(), pmap));
 				return TemplateUtils.createText(template, map);
 			}else{
 			
 			String options="";
 			for(LabelAndValue lv:fdata.getOptionValues()){
 					Map<String,String> tmp=new HashMap<String, String>();
 					tmp.put("label", lv.getLabel()!=null?"\""+lv.getLabel()+"\"":"null");
 					tmp.put("value", lv.getValue());
 					tmp.put("selected", "false");// getLabelAndValue no need selection anymore
 					options+=TemplateUtils.createText(add_label, tmp)+"\n";
 				}
 			map.put("key", fdata.getKey());
 			map.put("options", options);
 			return TemplateUtils.createText(template, map);
 			}
 		}else{
 			return "";
 		}
 	}
 	private Parameter toSpecialRelativeParam(FormFieldData fdata){
 		return ParameterUtils.parse(fdata.getOptionText().substring(1), ':');
 	}
 	private boolean isSpecialRelative(FormFieldData fdata){
 		return fdata.getOptionText()!=null && fdata.getOptionText().startsWith("@");
 	}
 	
 }
 
 public FormFieldDataToGetKeyListsFunction getFormFieldDataToGetKeyListsFunction(){
 	return FormFieldDataToGetKeyListsFunction.INSTANCE;
 }
 public enum FormFieldDataToGetKeyListsFunction implements Function<FormFieldData,String>{
 	INSTANCE
 	;
 	public static String template="\tlist.add(\"${value}\");";
 	@Override
 	public String apply(FormFieldData fdata) {
 		return TemplateUtils.createText(template, fdata.getKey());
 	}
 }
 
 public FormFieldDataToToLabelMapFunction getFormFieldDataToToLabelMapFunction(){
 	return FormFieldDataToToLabelMapFunction.INSTANCE;
 }
 public enum FormFieldDataToToLabelMapFunction implements Function<FormFieldData,String>{
 	INSTANCE
 	;
 	public static String template="\tmap.put(\"${value}\", toLabelValue(\"${value}\",map.get(\"${value}\")));";
 	
 	@Override
 	public String apply(FormFieldData fdata) {
 		return TemplateUtils.createText(template, fdata.getKey());
 	}
 }
 
 
 
 
 public FormFieldDataToToSetDefaultValues getFormFieldDataToToSetDefaultValues(){
 	return FormFieldDataToToSetDefaultValues.INSTANCE;
 }
 public enum FormFieldDataToToSetDefaultValues implements Function<FormFieldData,String>{
 	INSTANCE
 	;
 	@Override
 	public String apply(FormFieldData fdata) {
 		if(fdata.getDefaultValue()==null||fdata.getDefaultValue().isEmpty() ){
 			return null;
 		}
 		Map<String,String> map=new HashMap<String, String>();
 		map.put("name", fdata.getKey());
 		if(fdata.getDefaultValue().startsWith("@COOKIE")){
 			Parameter param=ParameterUtils.parse(fdata.getDefaultValue().substring(1));//skip first@
 			if(!param.getName().equals("COOKIE")){
 				LogUtils.log("some kind invalid CookieParam:"+param.getName());
 				return null;
 			}
 			
 			//TODO support multi
 			if(param.size()>1){//first 1 is cookie targetname,skip it
 				map.put("value", param.get(1));
 				return TemplateUtils.createText(Bundles.INSTANCE.set_default_values_default().getText(), map);
 			}
 		}else{
 			
 			//type selection,value must be valid otherwise not selected correctly
 			if(FormFieldData.isSelectionType(fdata.getType())){
 				List<String> converted=new ArrayList<String>();
 				String[] vs=fdata.getDefaultValue().split(",");//this make little difficulty
 				for(String v:vs){
 					for(LabelAndValue lv:fdata.getOptionValues()){
 						if(lv.getPrintValue().equals(v)){
 							converted.add(lv.getValue());
 						}
 					}
 				}
 				//why tab,because tab usually never used in option value,conma and : is sometime used in city name or something.
 				//actually csv base conma is used for splitter that is future task.
 				map.put("value",Joiner.on("\t").join(converted));
 			}else if(fdata.getType()==FormFieldData.TYPE_CHECK){
 				String value=fdata.getDefaultValue();
 				for(LabelAndValue lv:fdata.getOptionValues()){
 					if(lv.getPrintValue().equals(value)){
 						value=lv.getValue();
 					}
 					
 				}
 				map.put("value", value);
 			}
 			else{
 				map.put("value", fdata.getDefaultValue());
 			}
 			
 			
 			return TemplateUtils.createText(Bundles.INSTANCE.set_default_values_default().getText(), map);
 		}
 		return null;
 	}
 }
 
 
 public FormFieldDataToToSetDefaultValuesCookie getFormFieldDataToToSetDefaultValuesCookie(){
 	return FormFieldDataToToSetDefaultValuesCookie.INSTANCE;
 }
 public enum FormFieldDataToToSetDefaultValuesCookie implements Function<FormFieldData,String>{
 	INSTANCE
 	;
 	@Override
 	public String apply(FormFieldData fdata) {
 		if(fdata.getDefaultValue()!=null && fdata.getDefaultValue().startsWith("@COOKIE")){
 			Parameter param=ParameterUtils.parse(fdata.getDefaultValue().substring(1));
 			if(!param.getName().equals("COOKIE")){
 				LogUtils.log("some kind invalid CookieParam:"+param);
 				return null;
 			}
 			if(param.size()>0){
 				int lastIndex=-1;
 				String param1=param.get(0);
 				
 				if(param1.indexOf("+")!=-1){
 					lastIndex=param1.indexOf("+");
 				}else if(param1.indexOf("-")!=-1){
 					lastIndex=param1.indexOf("-");
 				}
 				
 				if(lastIndex!=-1){//number type
 					Map<String,String> map=new HashMap<String, String>();
 					map.put("name",param1.substring(0,lastIndex));
 					map.put("calculate",param1.substring(lastIndex));
 					return TemplateUtils.createText(Bundles.INSTANCE.set_default_values_cookie_number().getText(), map);
 				}else{
 					
 					return TemplateUtils.createText(Bundles.INSTANCE.set_default_values_cookie_text().getText(), param1);
 				}
 			}
 		}
 		return null;
 	}
 }
 
 
 public FormFieldDataToToLabelValueFunction getFormFieldDataToToLabelValueFunction(){
 	return FormFieldDataToToLabelValueFunction.INSTANCE;
 }
 public enum FormFieldDataToToLabelValueFunction implements Function<FormFieldData,String>{
 	INSTANCE
 	;
 	@Override
 	public String apply(FormFieldData fdata) {
 		if(fdata.getType()==FormFieldData.TYPE_CHECK){
 			Map<String,String> map=new HashMap<String, String>();
 			map.put("key", fdata.getKey());
 			map.put("true_value", "on");
 			map.put("false_value","");
 			String template=Bundles.INSTANCE.tolabelmap_boolean().getText();
 			List<LabelAndValue> options=fdata.getOptionValues();
 			if(options!=null){
 				if(options.size()>0){
 					map.put("true_value",options.get(0).getPrintValue());
 				}
 				if(options.size()>1){
 					map.put("false_value",options.get(1).getPrintValue());
 				}
 			}
 			//true
 			//false
 			return TemplateUtils.createAdvancedText(template, map);
 			
 		}//special date TODO as support create_date & modified_date
 		else if(fdata.getType()==FormFieldData.TYPE_CREATE_DATE || fdata.getType()==FormFieldData.TYPE_MODIFIED_DATE){
 			Map<String,String> map=new HashMap<String, String>();
 			map.put("key", fdata.getKey());
 			
 			String template=Bundles.INSTANCE.tolabelmap_cmdate().getText();
 			return TemplateUtils.createAdvancedText(template, map);
 		}else if(fdata.getType()==FormFieldData.TYPE_TEXT_LONG){
 			Map<String,String> map=new HashMap<String, String>();
 			map.put("key", fdata.getKey());
 			map.put("class", fdata.getKey());
 			
 			String template=Bundles.INSTANCE.tolabelmap_text_long().getText();
 			return TemplateUtils.createAdvancedText(template, map);
 		}else if(hasRelativeOption(fdata)){
 			String v=fdata.getOptionText();
 			//relative version
 			if(v!=null && v.startsWith("@")){
 				Parameter param=ParameterUtils.parse(v.substring(1));
 				if(ParameterUtils.isClosedAndHaveParameter(param)){
 					
 					Map<String,String> tmp=new HashMap<String, String>();
 					tmp.put("key", fdata.getKey());
 					tmp.put("refClass",param.getName());
 					tmp.put("refKey",param.get(0));//which label to show
 					tmp.put("refId",param.get(1));//which key is id
 					tmp.put("param", v.substring(1));//sedond Reference Option usually id-keyname,
 					return TemplateUtils.createAdvancedText(Bundles.INSTANCE.tolabelmap_number_relation().getText(), tmp);
 				}else{
 					GWT.log("invalid parameter:"+v);
 				}
 			}
 			LogUtils.log("invalid relative parameter hasRelativeOption must parsed:"+fdata);
 		}
 		else if(fdata.getType()==FormFieldData.TYPE_SELECT_SINGLE){
 			Map<String,String> map=new HashMap<String, String>();
 			map.put("key", fdata.getKey());
 			
 			String each_value="";
 			String each_template="\tif(value.equals(\"${value}\")){return \"${label}\";}\n";
 			for(LabelAndValue lv:fdata.getOptionValues()){
 				Map<String,String> tmp=new HashMap<String, String>();
 				tmp.put("value", lv.getValue());
 				tmp.put("label",lv.getLabel());
 				each_value+=TemplateUtils.createAdvancedText(each_template, tmp);	
 			}
 			map.put("each_value", each_value);
 			
 			
 			String template=Bundles.INSTANCE.tolabelmap_single().getText();
 			return TemplateUtils.createAdvancedText(template, map);
 		}else if(fdata.getType()==FormFieldData.TYPE_SELECT_MULTI){
 			Map<String,String> map=new HashMap<String, String>();
 			map.put("key", fdata.getKey());
 			
 			String each_value="";
 			String each_template="\tif(select.equals(\"${value}\")){labels.add(\"${label}\");}\n";
 			for(LabelAndValue lv:fdata.getOptionValues()){
 				Map<String,String> tmp=new HashMap<String, String>();
 				tmp.put("value", lv.getValue());
 				tmp.put("label",lv.getLabel());
 				each_value+=TemplateUtils.createAdvancedText(each_template, tmp);	
 			}
 			map.put("each_value", each_value);
 			
 			
 			String template=Bundles.INSTANCE.tolabelmap_multi().getText();
 			return TemplateUtils.createAdvancedText(template, map);
 		}else if(fdata.getType()==FormFieldData.TYPE_NUMBER){
 			String v=fdata.getOptionText();
 			
 			//relative version
 			if(v!=null && v.startsWith("@")){
 				Parameter param=ParameterUtils.parse(v.substring(1));
 				if(ParameterUtils.isClosedAndHaveParameter(param)){
 					
 					Map<String,String> tmp=new HashMap<String, String>();
 					tmp.put("key", fdata.getKey());
 					tmp.put("refClass",param.getName());
 					tmp.put("refKey",param.get(0));
 					tmp.put("param", v.substring(1));
 					return TemplateUtils.createAdvancedText(Bundles.INSTANCE.tolabelmap_number_relation().getText(), tmp);
 				}else{
 					GWT.log("invalid parameter:"+v);
 				}
 			}
 			
 		}
 		return "";
 	}
 	private boolean hasRelativeOption(FormFieldData fdata){
 		String v=fdata.getOptionText();
 		if(fdata.getType()==FormFieldData.TYPE_SELECT_MULTI){
			//now not support yet
 			return false;
 		}
 		//relative version
 		if(v!=null && v.startsWith("@")){
 			Parameter param=ParameterUtils.parse(v.substring(1));
 			if(ParameterUtils.isClosedAndHaveParameter(param)){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 }
 }
