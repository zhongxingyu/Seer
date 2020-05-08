 package com.nbcedu.function.schoolmaster2.core.action;
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.apache.struts2.ServletActionContext;
 
 import com.nbcedu.function.schoolmaster2.core.pager.Pager;
 import com.nbcedu.function.schoolmaster2.core.pager.PagerModel;
 import com.opensymphony.xwork2.ActionContext;
 import com.opensymphony.xwork2.ActionSupport;
 
 /**
  * <p></p>
  * @author 黎青春
  * Create at:2012-2-21 上午08:49:19
  */
 @SuppressWarnings("serial")
 public class BaseAction extends ActionSupport {
 
 	public static Logger logger = Logger.getLogger(BaseAction.class);
 
 	protected Pager pager = new Pager();
 	
 	protected PagerModel pm = new PagerModel();
 	
 	protected String id;
 	
 	protected List<String> idList = new ArrayList<String>();
 	
	protected HttpServletResponse response = ServletActionContext.getResponse();
	
	protected HttpServletRequest request = ServletActionContext.getRequest();
 	
 	protected String getUserId() {
 		return (String)ActionContext.getContext().getSession().get("sm2_init");
 	}
 	
 	protected Map<String,Object> getSession(){
 		return ActionContext.getContext().getSession();
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected Map<String,Object> getRequestMap(){
 		return (Map<String, Object>) ActionContext.getContext().get("request");
 	}
 	
 	protected HttpServletRequest getRequest(){
		return this.request = ServletActionContext.getRequest();
 	}
 	
 	protected Object getFromSession(Object key){
 		return ActionContext.getContext().getSession().get(key);
 	}
 	
 	/**
 	 * 进行增删改操作后,以redirect方式重新打开action默认页的result名
 	 */
 	public static final String RELOAD = "reload";
 	/**
 	 * 显示列表
 	 */
 	public static final String LIST = "list";
 	/**
 	 * 查看详细
 	 */
 	public static final String LOOK = "look";
 	
 	
 	//////////////////////////
 	///////getters&setters////
 	//////////////////////////
 	public PagerModel getPm() {
 		return pm;
 	}
 	public void setPm(PagerModel pm) {
 		this.pm = pm;
 	}
 	public Pager getPager() {
 		return pager;
 	}
 	public String getId() {
 		return id;
 	}
 	public void setId(String id) {
 		this.id = id;
 	}
 	public void setPager(Pager pager) {
 		this.pager = pager;
 	}
 	public List<String> getIdList() {
 		return idList;
 	}
 	public void setIdList(List<String> idList) {
 		this.idList = idList;
 	}
 	
 }
