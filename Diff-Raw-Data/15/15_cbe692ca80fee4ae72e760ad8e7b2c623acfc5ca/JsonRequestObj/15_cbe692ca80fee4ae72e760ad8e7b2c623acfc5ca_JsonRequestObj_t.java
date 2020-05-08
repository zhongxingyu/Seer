 package edu.limen.model.json;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 public class JsonRequestObj implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -6460473647358567350L;
 	private String type;
 	private String input;
 	private Integer userId;
 	private List<Integer> userIdList = new ArrayList<Integer>();
	private Integer status;
 
 	public String getType() {
 		return type;
 	}
 
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public String getInput() {
 		return input;
 	}
 
 	public void setInput(String input) {
 		this.input = input;
 	}
 
 	public Integer getUserId() {
 		return userId;
 	}
 
 	public void setUserId(Integer userId) {
 		this.userId = userId;
 	}
 
 	public List<Integer> getUserIdList() {
 		return userIdList;
 	}
 
 	public void setUserIdList(List<Integer> userIdList) {
 		this.userIdList = userIdList;
 	}
 
	public Integer getStatus() {
 		return status;
 	}
 
	public void setStatus(Integer status) {
 		this.status = status;
 	}
 }
