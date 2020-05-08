 package com.xone.action.web.login;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.commons.beanutils.BeanUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.struts2.ServletActionContext;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.opensymphony.xwork2.ActionContext;
 import com.xone.action.base.Action;
 import com.xone.model.hibernate.entity.Person;
 import com.xone.service.app.PersonService;
 
 public class LoginWebAction extends Action {
 	
 	private static final long serialVersionUID = 2316404862441984223L;
 	protected String error;
 	protected String lastname;
 	protected String message;
 	protected String name;
	protected String errorType;
 	protected List<Person> list = new ArrayList<Person>();
 	
 	@Autowired
 	protected PersonService personService;
 
 	public String login() throws Exception {
 		Map<String, ?> session = ActionContext.getContext().getSession();
 		this.lastname = (String) session.get("SPRING_SECURITY_LAST_USERNAME");
 		try {
 			Exception e = (Exception) session
 					.get("SPRING_SECURITY_LAST_EXCEPTION");
 			this.message = e.getMessage();
 		} catch (Exception e) {
 		}
 
 		if (this.message == null && this.lastname != null) {
 			return SUCCESS;
 		} else {
 			if (this.message != null) {
 				session.put("SPRING_SECURITY_LAST_EXCEPTION", null);
 			}
 			return INPUT;
 		}
 
 	}
 	
 	@SuppressWarnings("unchecked")
 	public String userList() throws Exception {
 		List<Person> l = getPersonService().findAll();
 		if (null != l && !l.isEmpty()) {
 			getList().addAll(l);
 		}
 		return SUCCESS;
 	}
 	
 	public String loginFailed() throws Exception {
 		return SUCCESS;
 	}
 	
 	@SuppressWarnings({"unchecked" })
 	public String welcome() throws Exception {
 //		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
 		HttpServletRequest request = ServletActionContext.getRequest();
 		if (null == request.getUserPrincipal()) {
 			return ERROR;
 		}
 		String username = request.getUserPrincipal().getName();
 		if (StringUtils.isBlank(username)) {
 			return ERROR;
 		}
 		if (null == getUserMap() || getUserMap().isEmpty()) {
 			this.setName(request.getUserPrincipal().getName());
 			this.setMessage("Successful Struts spring secuirty authentication");
 			Person p = new Person();
 			p.setUsername(username);
 			List<Person> pList = getPersonService().findAllByPerson(p);
 			if (pList.size() > 1 || pList.size() <= 0) {//没有记录或者能匹配到多个记录,都要求重新登陆
 				return ERROR;
 			}
 			Map<String, String> porperties = null;
 			try {
 				porperties = BeanUtils.describe(pList.get(0));
 			} catch (Exception e) {
 				porperties = Collections.EMPTY_MAP;
 				e.printStackTrace();
 			}
 			if (null != porperties && !porperties.isEmpty()) {
 				getSession().setAttribute(USER, porperties);
 			}
 		}
 		return SUCCESS;
 	}
 
 	/**
 	 * @return the message
 	 */
 	public String getMessage() {
 		return message;
 	}
 
 	/**
 	 * @param message
 	 *            the message to set
 	 */
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 	/**
 	 * @return the error
 	 */
 	public String getError() {
 		return error;
 	}
 
 	/**
 	 * @param error
 	 *            the error to set
 	 */
 	public void setError(String error) {
 		this.error = error;
 	}
 
 	/**
 	 * @return the lastname
 	 */
 	public String getLastname() {
 		return lastname;
 	}
 
 	/**
 	 * @param lastname
 	 *            the lastname to set
 	 */
 	public void setLastname(String lastname) {
 		this.lastname = lastname;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public List<Person> getList() {
 		return list;
 	}
 
 	public void setList(List<Person> list) {
 		this.list = list;
 	}
 
 	public PersonService getPersonService() {
 		return personService;
 	}
 
 	public void setPersonService(PersonService personService) {
 		this.personService = personService;
 	}
 
	public String getErrorType() {
		return errorType;
	}

	public void setErrorType(String errorType) {
		this.errorType = errorType;
	}

 }
