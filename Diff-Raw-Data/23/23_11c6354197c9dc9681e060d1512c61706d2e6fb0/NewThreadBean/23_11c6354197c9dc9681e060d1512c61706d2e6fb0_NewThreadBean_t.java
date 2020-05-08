 package beans;
 
 import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.RequestScoped;
 import javax.faces.context.FacesContext;
 
 import models.ThreadModel;
 
 @ManagedBean
 @RequestScoped
 public class NewThreadBean {
 	private String title, content;
 	
	@ManagedProperty(value = "#{loginBean}") 
	private LoginBean loginBean;
	
	public LoginBean getLoginBean() {
		return loginBean;
	}

	public void setLoginBean(LoginBean loginBean) {
		this.loginBean = loginBean;
	}

	public String save() throws Throwable {
 		ThreadModel thread = new ThreadModel();
 		
		if(!loginBean.getLoggedin()) {
			return null;
		}
		
 		int boardId = Integer.parseInt(FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("boardId"));
 		
 		thread.setBoardId(boardId);
 		thread.setContent(content);
 		thread.setTitle(title);
		thread.setUserId(loginBean.getUser().getUserId());
 		
 		thread.save();
 		
 		String re = String.format("thread.xhtml?threadId=%d", thread.getThreadId());
 		FacesContext.getCurrentInstance().getExternalContext().redirect(re);
 		
 		return null;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public void setContent(String content) {
 		this.content = content;
 	}
 }
