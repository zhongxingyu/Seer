 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.messenger.manbeans;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.component.UICommand;
 import javax.faces.context.FacesContext;
 
 import com.messenger.dao.Factory;
 import com.messenger.pojo.Letter;
 import com.messenger.pojo.LetterSenderReceiver;
 import com.messenger.pojo.RegistredUser;
 import com.messenger.service.LetterService;
 
 /**
  * 
  * @author I
  */
 @ManagedBean(name = "letterBean")
 @SessionScoped
 public class LetterBean implements Serializable {
 
 	Letter letter;
 
         private LetterSenderReceiver lsr;
 	private String receiverEmail = "";
 	private String senderEmail = "";
 	private String content = "";
 	private String subject = "";
 	private Boolean isInTrash = false;
 
 	private LetterService ls;
 
 	private UICommand replyButton;
 	private UICommand deleteButton;
 	private UICommand restoreButton;
 
 	@ManagedProperty(value = "#{letterWriteBean}")
 	private LetterWriteBean letterWriteBean;
 
 	public String getReceiverEmail() {
 		return receiverEmail;
 	}
 
 	public void setReceiverEmail(String receiverEmail) {
 		this.receiverEmail = receiverEmail;
 	}
 
 	public String getSenderEmail() {
 		return senderEmail;
 	}
 
 	public void setSenderEmail(String senderEmail) {
 		this.senderEmail = senderEmail;
 	}
 
 	public String getContent() {
 		return content;
 	}
 
 	public void setContent(String context) {
 		this.content = context;
 	}
 
 	public String getSubject() {
 		return subject;
 	}
 
 	public void setSubject(String subject) {
 		this.subject = subject;
 	}
 
 	public LetterWriteBean getLetterWriteBean() {
 		return letterWriteBean;
 	}
 
 	public void setLetterWriteBean(LetterWriteBean letterWriteBean) {
 		this.letterWriteBean = letterWriteBean;
 	}
 
         
         
 
 	public String showLetter(int id) {
 		LetterSenderReceiver lsr = (LetterSenderReceiver) Factory.getInstance()
 				.getLetterSenderReceiverDAO()
 				.getById(id, LetterSenderReceiver.class);
 		RegistredUser user = ((MaskUser) FacesContext.getCurrentInstance()
 				.getExternalContext().getSessionMap().get("maskbean"))
 				.getUser();
 		ls = new LetterService();
		List<LetterSenderReceiver> lsrList = ls.showLetter(lsr.getLetter().getId());
 		this.lsr = lsr;
		this.letter = lsr.getLetter();		
 		this.content = letter.getContent();
 		this.subject = letter.getTheme();
 		this.senderEmail = lsr.getSender().getLogin();
 		this.receiverEmail = "";
		for(LetterSenderReceiver temp:lsrList)
			this.receiverEmail+=temp.getReceiver().getLogin()+", ";
		if((lsr.getSender().equals(user) && lsr.isIsSenderTrash())
				|| lsr.getReceiver().equals(user) && lsr.isIsRecTrash())
 			isInTrash = true;
 		/*
 		 * FacesContext context = FacesContext.getCurrentInstance();
 		 * this.deleteButton
 		 * =context.getViewRoot().findComponent("letterViewForm:dBtn");
 		 * this.replyButton
 		 * =context.getViewRoot().findComponent("letterViewForm:rBtn");
 		 * this.restoreButton
 		 * =context.getViewRoot().findComponent("letterViewForm:rsBtn");
 		 */
 		// System.out.println(this.deleteButton.getId());
 
 		return "letterView";
 	}
 
 	public String newLetter() {
 
 		return "letterWrite";
 	}
 
 	public String reply() {
 
 		letterWriteBean.setReceiverEmail(this.senderEmail);
 		letterWriteBean.setSubject("Re: " + this.subject);
 		/*
 		 * lb = (LetterBean)
 		 * FacesContext.getCurrentInstance().getExternalContext
 		 * ().getSessionMap().get("bean");
 		 * 
 		 * 
 		 * this.senderEmail=this.user.getLogin(); this.receiverEmail =
 		 * lb.getSenderEmail(); this.subject="Re: "+lb.getSubject();
 		 */
 		return "letterWrite";
 
 	}
 
 	public String delete() {
                     
             ls = new LetterService();
             RegistredUser user = ((MaskUser)FacesContext.getCurrentInstance() 
 			.getExternalContext().getSessionMap().get("maskbean")).getUser();
             ls.delete(this.lsr, user);
 
 		return "account";
 	}
 
 	public String restore() {
 
             ls = new LetterService();
             RegistredUser user = ((MaskUser)FacesContext.getCurrentInstance() 
 			.getExternalContext().getSessionMap().get("maskbean")).getUser();
             ls.restore(this.lsr, user);
             
 		return "account";
 	}
 
 	public Boolean getIsInTrash() {
 		return isInTrash;
 	}
 
 	public void setIsInTrash(Boolean isInTrash) {
 		this.isInTrash = isInTrash;
 	}
 
 	public Letter getLetter() {
 		return letter;
 	}
 
 	public void setLetter(Letter letter) {
 		this.letter = letter;
 	}
 
 	public UICommand getReplyButton() {
 		return replyButton;
 	}
 
 	public void setReplyButton(UICommand replyButton) {
 		this.replyButton = replyButton;
 	}
 
 	public UICommand getDeleteButton() {
 		return deleteButton;
 	}
 
 	public void setDeleteButton(UICommand deleteButton) {
 		this.deleteButton = deleteButton;
 	}
 
 	public UICommand getRestoreButton() {
 		return restoreButton;
 	}
 
 	public void setRestoreButton(UICommand restoreButton) {
 		this.restoreButton = restoreButton;
 	}
 
 }
