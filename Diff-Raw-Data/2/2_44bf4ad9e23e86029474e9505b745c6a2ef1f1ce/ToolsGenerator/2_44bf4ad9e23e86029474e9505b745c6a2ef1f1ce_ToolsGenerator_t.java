 package com.akjava.gwt.webappmaker.client;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.akjava.gwt.webappmaker.client.resources.Bundles;
 import com.akjava.lib.common.form.FormData;
 import com.akjava.lib.common.form.FormFieldData;
 import com.akjava.lib.common.predicates.StringPredicates;
 import com.akjava.lib.common.tag.LabelAndValue;
 import com.akjava.lib.common.utils.TemplateUtils;
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 
 
 public class ToolsGenerator {
 private FormData formData;
 private String packageString;
 public ToolsGenerator(FormData formData,String packageString){
 	this.formData=formData;
 	this.packageString=packageString;
 }
 public String generateToolsText(){
 Joiner joiner=Joiner.on("\n").skipNulls();
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
 
 	map.put("isMultipleParameter",joiner.join(toIsMultiValueText));
 	
 
 //create toLabelMap
 	List<String> toLabelMapTexts=Lists.newArrayList(
 			Iterables.filter(
 				     Lists.transform(formData.getFormFieldDatas(), getFormFieldDataToToLabelMapFunction()),
 				     StringPredicates.getNotEmpty()
 				  )
 				  );
 
 			map.put("toLabelMap",joiner.join(toLabelMapTexts));
 	
 //create toLabelValue	
 List<String> toLabelValueTexts=Lists.newArrayList(
 Iterables.filter(
 	     Lists.transform(formData.getFormFieldDatas(), getFormFieldDataToToLabelValueFunction()),
 	     StringPredicates.getNotEmpty()
 	  )
 	  );
 
 map.put("toLabelValue",joiner.join(toLabelValueTexts));
 
 //create toLabelValue	
 List<String> toGetKeyListsTexts=Lists.newArrayList(
 Iterables.filter(
 	     Lists.transform(formData.getFormFieldDatas(), getFormFieldDataToGetKeyListsFunction()),
 	     StringPredicates.getNotEmpty()
 	  )
 	  );
 
 map.put("getKeyLists",joiner.join(toGetKeyListsTexts));
 }
 
 
 
 
 
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
 		else if(fdata.getKey().equals("cdate") || fdata.getKey().equals("mdate")){
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
 		}else if(fdata.getType()==FormFieldData.TYPE_SELECT_SINGLE){
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
 			String each_template="\tselect.equals(\"${value}\")){labels.add(\"${label}\");\n";
 			for(LabelAndValue lv:fdata.getOptionValues()){
 				Map<String,String> tmp=new HashMap<String, String>();
 				tmp.put("value", lv.getValue());
 				tmp.put("label",lv.getLabel());
 				each_value+=TemplateUtils.createAdvancedText(each_template, tmp);	
 			}
 			map.put("each_value", each_value);
 			
 			
 			String template=Bundles.INSTANCE.tolabelmap_multi().getText();
 			return TemplateUtils.createAdvancedText(template, map);
 		}
 		return "";
 	}
 	
 }
 }
