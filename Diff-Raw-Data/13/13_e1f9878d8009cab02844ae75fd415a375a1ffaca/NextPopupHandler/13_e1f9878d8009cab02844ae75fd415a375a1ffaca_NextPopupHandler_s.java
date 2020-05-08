 package com.sun.identity.admin.handler;
 
 import com.sun.identity.admin.model.CommandLinkBean;
 import com.sun.identity.admin.model.FromAction;
 import com.sun.identity.admin.model.HrefLinkBean;
 import com.sun.identity.admin.model.LinkBean;
 import com.sun.identity.admin.model.NextPopupBean;
 import com.sun.identity.admin.model.ViewId;
 import java.io.IOException;
 import java.io.Serializable;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 
 public class NextPopupHandler implements Serializable {
 
     private NextPopupBean nextPopupBean;
 
     public void setNextPopupBean(NextPopupBean nextPopupBean) {
         this.nextPopupBean = nextPopupBean;
     }
 
     public void closeListener(ActionEvent event) {
         nextPopupBean.reset();
 
         FromAction fa = FromAction.HOME;
         ViewId vid = fa.getViewId();
         FacesContext fc = FacesContext.getCurrentInstance();
         ExternalContext ec = fc.getExternalContext();
         String nextUrl = ec.getRequestContextPath() + vid.getId() + "?jsf.action=" + fa.getAction();
 
         redirect(nextUrl);
     }
 
     private void redirect(String url) {
         FacesContext fc = FacesContext.getCurrentInstance();
         ExternalContext ec = fc.getExternalContext();
         try {
             ec.redirect(url);
         } catch (IOException ioe) {
             throw new RuntimeException(ioe);
         }
 
     }
 
     public void nextListener(ActionEvent event) {
         nextPopupBean.reset();
 
         LinkBean lb = getLinkBean(event);
         FacesContext fc = FacesContext.getCurrentInstance();
         ExternalContext ec = fc.getExternalContext();
 
         String nextUrl;
         if (lb instanceof CommandLinkBean) {
             FromAction fa = FromAction.valueOfAction(lb.getValue());
             ViewId vid = fa.getViewId();
             nextUrl = ec.getRequestContextPath() + vid.getId() + "?jsf.action=" + fa.getAction();
         } else if (lb instanceof HrefLinkBean) {
             nextUrl = lb.getValue();
         } else {
            throw new AssertionError("unknown link bean type");
         }
 
         redirect(nextUrl);
     }
 
     public LinkBean getLinkBean(ActionEvent event) {
         LinkBean lb = (LinkBean) event.getComponent().getAttributes().get("linkBean");
         return lb;
     }
 }
