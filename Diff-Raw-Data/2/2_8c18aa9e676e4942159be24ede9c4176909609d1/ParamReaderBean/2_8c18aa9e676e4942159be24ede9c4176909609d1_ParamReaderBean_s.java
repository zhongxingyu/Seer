 package es.rchavarria.jsf;
 
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletRequest;
 
 @ManagedBean(name = "paramReader", eager = true)
 public class ParamReaderBean {
 
 	public String getTitle() {
 		return "Here is the result of reading parameters";
 	}
 
 	public List<Parameter> getParams() {
 		FacesContext fc = FacesContext.getCurrentInstance();
 		HttpServletRequest request = (HttpServletRequest) fc.getExternalContext().getRequest();
 		
 		Enumeration<String> names = request.getParameterNames();
 		if(!names.hasMoreElements()) return Collections.emptyList();
 
 		List<Parameter> params = new LinkedList<Parameter>();
 		while(names.hasMoreElements()) {
 			String name = names.nextElement();
 			String value = request.getParameter(name);
 			Parameter p = new Parameter();
 			p.setKey(name);
 			p.setValue(value);
 
 			params.add(p);
 		}
 		
 		return params;
 	}
 	
 	public String login() {
		return "home";
 	}
 }
