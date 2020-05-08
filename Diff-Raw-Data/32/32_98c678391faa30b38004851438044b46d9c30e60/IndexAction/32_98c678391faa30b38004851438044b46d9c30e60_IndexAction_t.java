 package me.friskit.hw.furp.action;
 
import org.apache.struts2.ServletActionContext;

 import com.opensymphony.xwork2.ActionSupport;
 
 public class IndexAction extends ActionSupport{
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3516968777297528496L;
 	private String treePath;
 
 	public String getTreePath() {
 		return treePath;
 	}
 
 	public void setTreePath(String treePath) {
 		this.treePath = treePath;
 	}
 	
 	@Override
 	public String execute() throws Exception {
 		// TODO Auto-generated method stub
		ServletActionContext.getResponse().setCharacterEncoding("UTF-8");
 		//Ҫݲͬĵ½ݴ벻ֵͬ
 		treePath = new String("/FriskitURP/data/FullTree.tr");
 		
 		return "success";
 	}
 	
 }
