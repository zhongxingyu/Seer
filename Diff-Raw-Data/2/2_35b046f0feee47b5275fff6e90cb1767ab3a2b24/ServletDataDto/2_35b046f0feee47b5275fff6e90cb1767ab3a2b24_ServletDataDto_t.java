 package com.akjava.gwt.webappmaker.client;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.akjava.gwt.webappmaker.client.resources.Bundles;
 import com.akjava.lib.common.form.FormData;
 import com.akjava.lib.common.form.FormFieldDataDto;
 import com.akjava.lib.common.functions.HtmlFunctions;
 import com.akjava.lib.common.utils.TemplateUtils;
 import com.akjava.lib.common.utils.ValuesUtils;
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.collect.Lists;
 import com.google.gwt.user.client.Window;
 
 public class ServletDataDto {
 private ServletDataDto(){}
 
 public static ServletDataToServletWebXmlFunction getServletDataToServletWebXmlFunction(){
 	return ServletDataToServletWebXmlFunction.INSTANCE;
 }
 public enum ServletDataToServletWebXmlFunction implements Function<ServletData,ServletWebXmlData>{
 	INSTANCE;
 	@Override
 	public ServletWebXmlData apply(ServletData data) {
 		
 		ServletWebXmlData xdata=new ServletWebXmlData();
 		xdata.setFullClassName(data.getBasePackage()+data.getLastPackage()+"."+data.getServletClassName());
 		
 		String head="";
 		if(!data.getLastPackage().equals("main")){
			head="/"+data.getLastPackage();
 		}
 		xdata.setPath(head+data.getPath());
 		
 		xdata.setName(getName(data.getLastPackage(),data.getDataClassName(),data.getServletType()));
 		return xdata;
 	}
 	
 }
 
 public static String getName(String lastPackage,String dataName,String type){
 	if(lastPackage.equals("main")){
 		return dataName+type;
 	}else{
 		return ValuesUtils.toUpperCamel(lastPackage)+dataName+type;
 	}
 }
 
 
 public static class FormDataToMainServletDataFunction implements Function<FormData,List<ServletData>>{
 	private String basePackage;
 	public FormDataToMainServletDataFunction(String basePackage){
 		this.basePackage=basePackage;
 	}
 	@Override
 	public List<ServletData> apply(FormData fdata) {
 		List<ServletData> datas=Lists.newArrayList();
 		datas.add(new ServletData(basePackage,"main",ServletData.TYPE_LIST,fdata,"/index.html"));
 		datas.add(new ServletData(basePackage,"main",ServletData.TYPE_SHOW,fdata,"/show"));
 		
 		return datas;
 	}	
 }
 
 public static class FormDataToAdminServletDataFunction implements Function<FormData,List<ServletData>>{
 	private String basePackage;
 	public FormDataToAdminServletDataFunction(String basePackage){
 		this.basePackage=basePackage;
 	}
 	@Override
 	public List<ServletData> apply(FormData fdata) {
 		List<ServletData> datas=Lists.newArrayList();
 		
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_LIST,fdata,"/"));
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_SHOW,fdata,"/show"));
 		
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_ADD,fdata,"/add"));
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_ADD_CONFIRM,fdata,"/add_confirm"));
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_ADD_EXEC,fdata,"/add_exec"));
 		
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_EDIT,fdata,"/edit"));
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_EDIT_CONFIRM,fdata,"/edit_confirm"));
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_EDIT_EXEC,fdata,"/edit_exec"));
 		
 		
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_DELETE_CONFIRM,fdata,"/delete_confirm"));
 		datas.add(new ServletData(basePackage,"admin",ServletData.TYPE_DELETE_EXEC,fdata,"/delete_exec"));
 		return datas;
 	}	
 }
 
 public static class ServletDataToServletFileFunction implements Function<ServletData,FileNameAndText>{
 
 	@Override
 	public FileNameAndText apply(ServletData data) {
 		FileNameAndText file=new FileNameAndText();
 		file.setName(data.getServletClassName()+".java");
 		String javaTemplate=null;
 		if(data.getServletType().equals(ServletData.TYPE_LIST)){
 			javaTemplate=Bundles.INSTANCE.list_servlet().getText();
 		}else if(data.getServletType().equals(ServletData.TYPE_SHOW)){
 			javaTemplate=Bundles.INSTANCE.show_servlet().getText();
 		}else if(data.getServletType().equals(ServletData.TYPE_ADD)){
 			javaTemplate=Bundles.INSTANCE.show_servlet().getText();
 		}else if(data.getServletType().equals(ServletData.TYPE_ADD_CONFIRM)){
 			javaTemplate=Bundles.INSTANCE.show_servlet().getText();
 		}else if(data.getServletType().equals(ServletData.TYPE_ADD_EXEC)){
 			javaTemplate=Bundles.INSTANCE.show_servlet().getText();
 		}else if(data.getServletType().equals(ServletData.TYPE_EDIT)){
 			javaTemplate=Bundles.INSTANCE.show_servlet().getText();
 		}else if(data.getServletType().equals(ServletData.TYPE_EDIT_CONFIRM)){
 			javaTemplate=Bundles.INSTANCE.show_servlet().getText();
 		}else if(data.getServletType().equals(ServletData.TYPE_EDIT_EXEC)){
 			javaTemplate=Bundles.INSTANCE.show_servlet().getText();
 		}else if(data.getServletType().equals(ServletData.TYPE_DELETE_CONFIRM)){
 			javaTemplate=Bundles.INSTANCE.show_servlet().getText();
 		}else if(data.getServletType().equals(ServletData.TYPE_DELETE_EXEC)){
 			javaTemplate=Bundles.INSTANCE.show_servlet().getText();
 		}
 		
 		if(javaTemplate==null){
 			Window.alert("invalid type:"+data.getServletType());
 		}
 		Map<String,String> map=new HashMap<String,String>();
 		map.put("className", data.getServletClassName());
 		map.put("basePackage", data.getBasePackage());
 		map.put("package", data.getBasePackage()+data.getLastPackage());
 		
 		map.put("baseTemplate", data.getLastPackage()+"Base.html");
 		String mainTemplate=data.getDataClassName()+"_"+data.getServletType();
 		map.put("mainTemplate", mainTemplate.toLowerCase()+".html");
 		map.put("title", data.getFormData().getName()+" "+Internationals.getMessage(data.getServletType().toLowerCase()));
 		
 		map.put("mainRowTemplate", mainTemplate.toLowerCase()+"_row"+".html");
 		map.put("dataClassName", data.getDataClassName());
 		map.put("path",data.getPath());
 		
 		String path=data.getPath();
 		if(!path.endsWith("/")){
 			int last=path.lastIndexOf("/");
 			if(last!=-1){
 				String indexPath=path.substring(0,last+1);
 				map.put("indexPath", indexPath);
 			}
 		}
 		
 		String text=TemplateUtils.createAdvancedText(javaTemplate, map);
 		file.setText(text);
 		
 		return file;
 	}
 	
 	
 }
 
 public static class ServletDataToTemplateFileFunction implements Function<ServletData,List<FileNameAndText>>{
 
 	@Override
 	public List<FileNameAndText> apply(ServletData data) {
 		List<FileNameAndText> files=new ArrayList<FileNameAndText>();
 		String type=data.getServletType();
 		
 		FileNameAndText file=new FileNameAndText();
 		String head="";
 		if(!data.getLastPackage().equals("main")){
 			head=data.getLastPackage()+"_";
 		}
 		file.setName(head+data.getDataClassName().toLowerCase()+"_"+ValuesUtils.upperCamelToUnderbar(type)+".html");
 		
 		String htmlTemplate=null;
 		
 		if(type.equals(ServletData.TYPE_LIST)){
 			if(data.getLastPackage().equals("admin")){
 			htmlTemplate=Bundles.INSTANCE.admin_list_html().getText();	
 			}else{
 			htmlTemplate=Bundles.INSTANCE.list_html().getText();
 			}
 			
 			FileNameAndText file2=new FileNameAndText();
 			file2.setName(head+data.getDataClassName().toLowerCase()+"_"+type.toLowerCase()+"_row.html");
 			files.add(file2);
 			if(data.getLastPackage().equals("admin")){
 				file2.setText(Bundles.INSTANCE.admin_list_row_html().getText());	
 			}else{
 			file2.setText(Bundles.INSTANCE.list_row_html().getText());
 			}
 			
 			} else if (type.equals(ServletData.TYPE_SHOW)) {
 				if (data.getLastPackage().equals("admin")) {
 					//TODO admin
 					htmlTemplate = Bundles.INSTANCE.show_html().getText();
 				} else {
 					htmlTemplate = Bundles.INSTANCE.show_html().getText();
 				}
 
 		}else if(type.equals(ServletData.TYPE_ADD)){
 			htmlTemplate=Bundles.INSTANCE.show_html().getText();
 		}else if(type.equals(ServletData.TYPE_ADD_CONFIRM)){
 			htmlTemplate=Bundles.INSTANCE.show_html().getText();
 		}else if(type.equals(ServletData.TYPE_ADD_EXEC)){
 			htmlTemplate=Bundles.INSTANCE.show_html().getText();
 		}else if(type.equals(ServletData.TYPE_EDIT)){
 			htmlTemplate=Bundles.INSTANCE.show_html().getText();
 		}else if(type.equals(ServletData.TYPE_EDIT_CONFIRM)){
 			htmlTemplate=Bundles.INSTANCE.show_html().getText();
 		}else if(type.equals(ServletData.TYPE_EDIT_EXEC)){
 			htmlTemplate=Bundles.INSTANCE.show_html().getText();
 		}else if(type.equals(ServletData.TYPE_DELETE_CONFIRM)){
 			htmlTemplate=Bundles.INSTANCE.show_html().getText();
 		}else if(type.equals(ServletData.TYPE_DELETE_EXEC)){
 			htmlTemplate=Bundles.INSTANCE.show_html().getText();
 		}
 		
 		if(htmlTemplate==null){
 			Window.alert("invalid type:"+data.getServletType());
 		}
 		file.setText(htmlTemplate);
 		files.add(file);
 		
 		
 		Map<String,String> map=new HashMap<String,String>();
 		//headers
 		List<String> names=Lists.transform(data.getFormData().getFormFieldDatas(), FormFieldDataDto.getFormFieldToNameFunction());
 		List<String> ths=Lists.transform(names
 				, HtmlFunctions.getStringToTHFunction());
 		map.put("headers", Joiner.on("\n").join(ths));
 		//columns
 		List<String> keys=Lists.transform(data.getFormData().getFormFieldDatas(), FormFieldDataDto.getFormFieldToKeyFunction());
 		List<String> tds=Lists.transform(
 				Lists.transform(
 				keys
 				,new HtmlFunctions.StringToPreFixAndSuffix("${","}"))
 				, HtmlFunctions.getStringToTDFunction());
 		map.put("columns", Joiner.on("\n").join(tds));
 		
 		
 		//name trtd for show
 		List<List<String>> vs=new ArrayList<List<String>>();
 		vs.add(names);
 		vs.add(tds);
 		map.put("name_key_trtd",
 		HtmlFunctions.getStringToTRTDFunction().apply(vs)
 		);
 		
 		map.put("show_title", Internationals.getMessage("show"));
 		map.put("add_title", Internationals.getMessage("add"));
 		map.put("edit_title", Internationals.getMessage("edit"));
 		map.put("delete_title", Internationals.getMessage("delete"));
 		
 		for(FileNameAndText fileText:files){
 			String text=TemplateUtils.createAdvancedText(fileText.getText(), map);
 			fileText.setText(text);
 		}
 		
 		return files;
 	}
 	
 	
 }
 
 }
