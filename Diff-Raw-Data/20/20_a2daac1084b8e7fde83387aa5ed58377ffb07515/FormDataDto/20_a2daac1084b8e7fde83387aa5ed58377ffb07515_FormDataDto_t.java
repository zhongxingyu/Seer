 package com.akjava.lib.common.form;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.akjava.gwt.lib.client.LogUtils;
 import com.akjava.lib.common.param.Parameter;
 import com.akjava.lib.common.param.ParameterUtils;
 import com.akjava.lib.common.utils.ValuesUtils;
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 
 public class FormDataDto {
 	public final static Joiner tabJoiner=Joiner.on("\t").useForNull("NULL");	
 	public final static Joiner commaJoiner=Joiner.on(",").useForNull("NULL");	
 private FormDataDto(){}
 
 
 
 public static FormDataToMapFunction getFormDataToMapFunction(){
 	return FormDataToMapFunction.INSTANCE;
 }
 public enum FormDataToMapFunction implements Function<FormData,Map<String,String>>{
 	INSTANCE;
 	@Override
 	public Map<String,String> apply(FormData data) {
 		Map<String,String> map=new LinkedHashMap<String, String>();
 		map.put("name", data.getName());
 		map.put("className",data.getClassName());
 		map.put("description", data.getDescription());
 		return map;
 	}
 }
 
 
 public static FormDataToCsvFunction getFormDataToCsvFunction(){
 	return FormDataToCsvFunction.INSTANCE;
 }
 public enum FormDataToCsvFunction implements Function<FormData,List<String>>{
 	INSTANCE;
 	@Override
 	public List<String> apply(FormData data) {
 		List<String> result=new ArrayList<String>();
 		String dataLine=getFormDataWithoutOptionsToCsvFunction().apply(data);
 		result.add(dataLine);
 		
 		if(data.getFormFieldDatas()!=null){
 			
 			List<String> optionLines=Lists.transform(data.getFormFieldDatas(), FormFieldDataDto.getFormFieldToCsvFunction());
 			Iterables.addAll(result,optionLines);
 		}
 		
 		return result;
 	}
 }
 
 //simple convert
 public static FormDataWithoutOptionsToCsvFunction getFormDataWithoutOptionsToCsvFunction(){
 	return FormDataWithoutOptionsToCsvFunction.INSTANCE;
 }
 public enum FormDataWithoutOptionsToCsvFunction implements Function<FormData,String>{
 	INSTANCE;
 	@Override
 	public String apply(FormData data) {
 		Map<String,String> map=getFormDataToMapFunction().apply(data);
 		return tabJoiner.join(map.values());
 	}
 }
 
 
 public static CsvLineToFormDataFunction getCsvLineToFormDataFunction(){
 	return CsvLineToFormDataFunction.INSTANCE;
 }
 public enum CsvLineToFormDataFunction implements Function<String,FormData>{
 	INSTANCE;
 	@Override
 	public FormData apply(String csv) {
 		if(csv.indexOf("\n")!=-1){
 			String[] lines=ValuesUtils.toArrayLines(csv);
 			csv=lines[0];
 		}
 		if(csv.isEmpty()){
 			return null;
 		}
 		FormData data=new FormData();
 		String[] values=csv.split("\t");
 		if(values.length>0){
 			data.setName(values[0]);
 		}
 		if(values.length>1){
 			data.setClassName(values[1]);
 		}
 		if(values.length>2){
 			data.setDescription(values[2]);
 		}
 		if(values.length>3){
 			parseOptions(data,values[3]);
 		}
 		return data;
 	}
 	private void parseOptions(FormData data,String line){
 
 		String params[]=line.split(",");
 		for(String p:params){
 			Parameter parameter=ParameterUtils.parse(p);
 			if(parameter.getName().equals("adminonly")){
 				data.setAdminOnly(true);
 			}else if(parameter.getName().equals("list")){
 				if(parameter.size()>0){
 					int pageSize=ValuesUtils.toInt(parameter.get(0), 10);
 					data.setPageSize(pageSize);
 				}
 				if(parameter.size()>1){
 					data.setPageOrder(parameter.get(1));
 				}
 			}else if(parameter.getName().equals("adminlist")){
 				if(parameter.size()>0){
 					int pageSize=ValuesUtils.toInt(parameter.get(0), 10);
 					data.setAdminPageSize(pageSize);
 				}
 				if(parameter.size()>1){
 					data.setAdminPageOrder(parameter.get(1));
 				}
 			}else if(parameter.getName().equals("sublist")){
 				if(parameter.size()>0){
 					int pageSize=ValuesUtils.toInt(parameter.get(0), 10);
 					data.setSubPageSize(pageSize);
 				}
 				if(parameter.size()>1){
 					data.setSubPageOrder(parameter.get(1));
 				}
 			}else{
 				System.out.println("on FormdataDto.parseOptions();some unknown option:"+parameter.getName());
 			}
 		}
 	}
 	
 }
 
 public static List<FormData> linesToFormData(List<String> lines){
 	//initial data for empty data format
 	List<FormData> formDatas=new ArrayList<FormData>();
 	FormData lastData=new FormData();
 	lastData.setName("DATA");
 	lastData.setClassName("Data");
 	lastData.setDescription("default data class");
 	
 	for(String line:lines){
 		if(line.isEmpty()){
 			continue;
 		}
 		String[] csv=line.split("\t");
 		if(csv.length>1){
 			
 			String secondValue=csv[1];
 			if(secondValue.isEmpty()){
 				System.out.println("maybe invalid line:"+line);
 				continue;//invalid any case,however checking is validator job
 			}
 			if(Character.isUpperCase(secondValue.charAt(0))){
 				//class case
 				lastData=getCsvLineToFormDataFunction().apply(line);//do parse again
 				formDatas.add(lastData);
 			}else{
 				
 				//field case
 				FormFieldData fdata=FormFieldDataDto.getCsvToFormFieldFunction(true).apply(line);
 				lastData.addFormFieldData(fdata);
 				/*
 				fdata.setParent(lastData);
 				
 				List<FormFieldData> optionValues=lastData.getFormFieldDatas();
 				if(optionValues==null){
 					optionValues=new ArrayList<FormFieldData>();
 					lastData.setFormFieldDatas(optionValues);
 					formDatas.add(lastData);
 					
 					
 				}
 				optionValues.add(fdata);
 				*/
 			}
 		}
 	}
 	
 	//for remain
 	/*
 	if(!formDatas.contains(lastData)){
 		formDatas.add(lastData);
 	}*/
 	
 	//parse relations
 	Map<String,FormData> map=new HashMap<String, FormData>();
 	for(FormData data:formDatas){
 		map.put(data.getClassName(), data);
 	}
 	for(FormData data:formDatas){
 		for(FormFieldData fdata:data.getFormFieldDatas()){
 			if(fdata.getType()==FormFieldData.TYPE_NUMBER){//in this time number only have relation
 				Parameter param=parseParameter(fdata.getOptionText());
 				if(param!=null){
 					FormData parent=map.get(param.getName());
 					if(parent==null){
 						System.out.println("invalid relationship:"+fdata.getOptionText());
 						continue;
 					}
 					
 					
 					parent.addChildren(new Relation(data,fdata.getKey()));
 					
					
 					data.addParent(new Relation(parent,param.get(0)));//usually it is id
 				}
 			}
 		}
 	}
 	
 	return formDatas;
 }
 
 public static Parameter parseParameter(String startsAtMark){
 	if(startsAtMark==null){
 		return null;
 	}
 	if(!startsAtMark.startsWith("@")){
 		return null;
 	}
 	Parameter param=ParameterUtils.parse(startsAtMark.substring(1));
 	if(!ParameterUtils.isClosedAndHaveParameter(param)){
 		return null;
 	}
 	
 	return param;
 }
 
 public static List<FormData> linesToFormData(String text){
 	List<String> lines=ValuesUtils.toListLines(text);
 	return linesToFormData(lines);
 }
 
 }
