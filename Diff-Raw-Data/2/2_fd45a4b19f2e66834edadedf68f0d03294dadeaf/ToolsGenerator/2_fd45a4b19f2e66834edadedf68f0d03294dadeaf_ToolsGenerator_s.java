 package com.akjava.gwt.webappmaker.client;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.tools.ant.taskdefs.XSLTProcess.Param;
 
 import com.akjava.gwt.html5.client.file.ui.FileNameAndText;
 import com.akjava.gwt.webappmaker.client.ServletDataDto.FormDataToCreateFormFieldFunction;
 import com.akjava.gwt.webappmaker.client.resources.Bundles;
 import com.akjava.lib.common.form.FormData;
 import com.akjava.lib.common.form.FormFieldData;
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
 
 
 public class ToolsGenerator {
 private FormData formData;
 private String packageString;
 public ToolsGenerator(FormData formData,String packageString){
 	this.formData=formData;
 	this.packageString=packageString;
 }
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
 List<String> getLabelAndValue=Lists.newArrayList(
 Iterables.filter(
 	     Lists.transform(formData.getFormFieldDatas(), getFormFieldDataToGetLabelAndValueFunction()),
 	     StringPredicates.getNotEmpty()
 	  )
 	  );
 
 map.put("getLabelAndValue",nLineJoiner.join(getLabelAndValue));
 
 
 
 Collection<String> methods=Collections2.transform(formData.getFormFieldDatas(), new FormDataToCreateFormFieldFunction());
 String methodText=Joiner.on("\n").skipNulls().join(methods);
 
 //map.put("createFormFields", methodText);
 map.put("insertFormField", methodText);
 
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
				pmap.put("dataClassName", fdata.getParent().getClassName());
 				
 				
 				map.put("key", fdata.getKey());
 				map.put("options", TemplateUtils.createAdvancedText(Bundles.INSTANCE.getlabelandvalue_relative().getText(), pmap));
 				return TemplateUtils.createText(template, map);
 			}else{
 			
 			String options="";
 			for(LabelAndValue lv:fdata.getOptionValues()){
 					Map<String,String> tmp=new HashMap<String, String>();
 					tmp.put("label", lv.getLabel()!=null?"\""+lv.getLabel()+"\"":"null");
 					tmp.put("value", lv.getValue());
 					tmp.put("selected", ""+lv.isSelected());
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
 		}
 		return "";
 	}
 	
 }
 }
