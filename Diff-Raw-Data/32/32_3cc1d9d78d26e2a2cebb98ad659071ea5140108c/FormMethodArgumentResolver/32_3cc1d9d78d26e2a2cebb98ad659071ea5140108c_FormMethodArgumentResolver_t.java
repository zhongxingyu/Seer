 package org.antstudio.support.spring;
 
 import java.io.File;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 
 import org.antstudio.support.spring.annotation.FormParam;
 import org.antstudio.utils.FileUtils;
 import org.springframework.core.MethodParameter;
 import org.springframework.web.bind.support.WebDataBinderFactory;
 import org.springframework.web.context.request.NativeWebRequest;
 import org.springframework.web.method.support.HandlerMethodArgumentResolver;
 import org.springframework.web.method.support.ModelAndViewContainer;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.multipart.MultipartHttpServletRequest;
 
 import com.reeham.component.ddd.model.ModelContainer;
 import com.reeham.component.ddd.model.ModelUtils;
 
 /**
  * the customer parameter type 
  * @author Gavin
  * @version 1.0
  * @date 2012-12-3
  */
 public class FormMethodArgumentResolver implements HandlerMethodArgumentResolver{
 
 	@Resource
 	private ModelContainer modelContainer;
 	   public FormMethodArgumentResolver() {
 	    }
 
 	    @Override
 	    public boolean supportsParameter(MethodParameter parameter) {
 	    	if(parameter.hasParameterAnnotation(FormParam.class)){
 	            return true;
 	        }
 	        return false;
 	    }
 
 	   
 	    public final Object resolveArgument(MethodParameter parameter,
 	                                        ModelAndViewContainer mavContainer,
 	                                        NativeWebRequest request,
 	                                        WebDataBinderFactory binderFactory) throws Exception {
	    	try{
 	    	FormParam formParam = parameter.getParameterAnnotation(FormParam.class);
 	        String name = formParam.value();
 	        Object target = (mavContainer.containsAttribute(name)) ?
 	                mavContainer.getModel().get(name) :createAttribute(parameter,request,formParam.model()); 
 	      
 	      
 	         
 	        //WebDataBinder binder = binderFactory.createBinder(request, target, name);
 	        //target = binder.getTarget();
 	        
 	      //  target = binder.convertIfNecessary(binder.getTarget(), parameter.getParameterType());
 	         mavContainer.addAttribute(name, target);
 	       
 	        
 	        return target;
	    	}catch(Exception e){
	    		return null;
	    	}
 	    }
 
 	    public Object createAttribute(MethodParameter parameter,NativeWebRequest request,boolean model) throws Exception{
 	    	Class<?> paramType = parameter.getParameterType();
 	    	String prefix = getPrefix(parameter);
 	    	Set<String> paramNames = getParamNamesByPrefix(request,prefix);
 	    	if(model){
 	    		if(paramNames.contains(prefix+"id")){
 	    		Object target = modelContainer.getModel(ModelUtils.asModelKey(paramType,request.getParameter(prefix)+"id" ));
 	    	    if(target!=null){
 	    	    	return target;
 	    	    }
 	    	    throw new Exception("the model:"+paramType.getName()+" with id:"+(request.getParameter(prefix)+"id" )+" is not cached in modelContainer");
 	    		}
 	    		throw new Exception("get model "+paramType.getName()+" from modelContainer,must based on "+prefix+"id");
 	    	}
 	    	
 	    	Iterator<String> paramName = paramNames.iterator();
 	    	Map<String,Object> matchParams = new HashMap<String,Object>();
 	    	String temp;
 	    	while(paramName.hasNext()){
 	    		temp = paramName.next();
 	    		matchParams.put(temp.substring(prefix.length()), request.getParameter(temp));
 	    	}
 	    	
 	    	HttpServletRequest servletRequest = request.getNativeRequest(HttpServletRequest.class);
 	    	if(servletRequest instanceof MultipartHttpServletRequest){//handle the file upload if has some
 	    		MultipartHttpServletRequest multipartHttpServletRequest = (MultipartHttpServletRequest)servletRequest;
 	    		paramName = multipartHttpServletRequest.getFileNames();
 	    		while(paramName.hasNext()){
 	    			temp = paramName.next();
 	    			matchParams.put(temp.substring(prefix.length()), multipartHttpServletRequest.getFile(temp));
 	    		}
 	    	}
 	    	
 	    	return createParamInstance(paramType,matchParams,servletRequest);
 	    }
 	    
 	    public Object createParamInstance(Class<?> parameterType,Map<String,Object> paramsMap,HttpServletRequest request) throws Exception{
 	    	
 	    	Object instance = null;
 	    	for(Constructor<?> con:parameterType.getConstructors()){
 	    		if(con.getParameterTypes().length==0){
 	    			instance= con.newInstance();
 	    		}
 	    	}
 	    	
 	    	if(instance!=null){
 	    		Field field;
 	    		Method[] methods =parameterType.getMethods();
 	    		Class<?> type;
 	    		Object value;
 	    		for(String fieldName:paramsMap.keySet()){
 	    			try{
 	    			field = parameterType.getDeclaredField(fieldName);
 	    			if(field!=null){
 	    				if(!field.isAccessible())
 	    					field.setAccessible(true);
 	    			value=paramsMap.get(fieldName);
 	    			if(value instanceof MultipartFile){
 	    				File dir = new File(request.getSession().getServletContext().getRealPath("/upload"));
 	    				if(!dir.exists())
 	    					dir.mkdir();
 	    				FileUtils.save(((MultipartFile)value).getInputStream(), new File(request.getSession().getServletContext().getRealPath("/upload/")+((MultipartFile)value).getOriginalFilename()));
 	    				field.set(instance, "/upload/"+((MultipartFile)value).getOriginalFilename());
 	    			}
 	    			else
 	    				field.set(instance, paramsMap.get(fieldName));
 	    			}
 	    			}catch(Exception e){//当字段无法访问时，即可能是父类的private属性，通过setter方法进行处理，如果找不到setter方法直接抛出异常
 	    				System.out.println(fieldName+"..........");
 	    				e.printStackTrace();
 	    				value=paramsMap.get(fieldName).toString();
 	    				for(Method m:methods){
 	    					if(m.getName().equals("set"+fieldName.substring(0,1).toUpperCase()+fieldName.substring(1))&&m.getParameterTypes().length==1)
 	    					{	
 	    						type=m.getParameterTypes()[0];
 	    						//待增加属性为自定义类型(如：classroom.student.name,classroom.student.no)&&已经传入列表和数据等类型
 								if (type == int.class || type == Integer.class) {
 									try {
 										m.invoke(instance, Integer.parseInt(value.toString()));
 									} catch (Exception exception) {
 										m.invoke(instance, 0);
 									}
 								} else if (type == long.class || type == Long.class) {
 									try {
 										m.invoke(instance, Long.parseLong(value.toString()));
 									} catch (Exception exception) {
 										m.invoke(instance, 0L);
 									}
 								} else if (type == double.class || type == Double.class) {
 									try {
 										m.invoke(instance, Double.parseDouble(value.toString()));
 									} catch (Exception exception) {
 										m.invoke(instance, 0.00);
 									}
 								} else if (type == float.class || type == Float.class) {
 									try {
 										m.invoke(instance, Float.parseFloat(value.toString()));
 									} catch (Exception exception) {
 										m.invoke(instance, 0.0);
 									}
 								} else if (type ==boolean.class&&type==Boolean.class){
 									try {
 										m.invoke(instance, Boolean.parseBoolean(value.toString()));
 									} catch (Exception exception) {
 										m.invoke(instance, false);
 									}
 								} else m.invoke(instance, value);
 	    						continue;
 	    					}
 	    				}
 	    			}
 	    		}
 	    		return instance;
 	    	}
 	    	else
 	    	throw new Exception("The Injected parameter class:"+parameterType.getName()+" has no Nullary construtor");
 	    	
 	    }
 	    
 	    /**
 	     * get the parameter prefix
 	     * @param parameter
 	     * @return
 	     */
 	    public String getPrefix(MethodParameter parameter){
 	    	return parameter.getParameterAnnotation(FormParam.class).value()+".";
 	    }
 	    /**
 	     * get the parameter names which match the prefix (start with prefix+".") 
 	     * @param request
 	     * @param prefix
 	     * @return
 	     */
 	    public Set<String> getParamNamesByPrefix(NativeWebRequest request,String prefix){
 	    	 Iterator<String> itor = request.getParameterNames();
 		        Set<String> paramNames = new TreeSet<String>();
 		        String paramName ;
 
 			       while( itor.hasNext()){
 			    	   paramName =  itor.next();
 			    	   if(paramName.startsWith(prefix)){//找到匹配的参数
 			    		   paramNames.add(paramName);
 			    	   }
 			       }
 			       
 	    	return paramNames;
 	    }
 	    
 
 }
