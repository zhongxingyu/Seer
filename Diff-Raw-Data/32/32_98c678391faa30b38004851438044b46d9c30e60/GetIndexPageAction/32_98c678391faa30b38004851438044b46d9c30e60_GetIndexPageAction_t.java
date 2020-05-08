 package me.friskit.hw.furp.action.getpage;
 
import org.apache.struts2.ServletActionContext;

 import com.opensymphony.xwork2.ActionSupport;
 
 public class GetIndexPageAction extends ActionSupport {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4115539774042986406L;
 
 	@Override
 	public String execute() throws Exception {
 		// TODO Auto-generated method stub
		ServletActionContext.getResponse().setCharacterEncoding("UTF-8");
 		return SUCCESS;
 	}
 
 }
