 package com.sun.identity.admin.handler;
 
 import com.icesoft.faces.context.effects.Effect;
 import com.icesoft.faces.context.effects.Fade;
 import com.sun.identity.admin.dao.PolicyDao;
 import com.sun.identity.admin.model.PhaseEventAction;
 import com.sun.identity.admin.model.PolicyManageBean;
 import com.sun.identity.admin.model.PolicyWizardBean;
 import com.sun.identity.admin.model.PrivilegeBean;
 import com.sun.identity.admin.model.QueuedActionBean;
 import java.io.Serializable;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.PhaseId;
 import javax.faces.event.ValueChangeEvent;
 
 public class PolicyManageHandler implements Serializable {
 
     private PolicyManageBean policyManageBean;
     private QueuedActionBean queuedActionBean;
     private PolicyDao policyDao;
     private PolicyWizardBean policyEditWizardBean;
 
     public PrivilegeBean getPrivilegeBean(ActionEvent event) {
         PrivilegeBean pb = (PrivilegeBean) event.getComponent().getAttributes().get("privilegeBean");
         assert(pb != null);
 
         return pb;
     }
     public PolicyManageBean getPolicyManageBean() {
         return policyManageBean;
     }
 
     public void setPolicyManageBean(PolicyManageBean policyManageBean) {
         this.policyManageBean = policyManageBean;
     }
 
     public void sortTableListener(ActionEvent event) {
         policyManageBean.getPolicyManageTableBean().sort();
     }
 
     public void viewOptionsListener(ActionEvent event) {
         policyManageBean.setViewOptionsPopupVisible(!policyManageBean.isViewOptionsPopupVisible());
     }
 
     public void editListener(ActionEvent event) {
         PrivilegeBean pb = getPrivilegeBean(event);
         policyEditWizardBean.reset();
         policyEditWizardBean.setPrivilegeBean(pb);
         policyEditWizardBean.setAllEnabled(true);
         policyEditWizardBean.gotoStep(4);
     }
 
     public void removeListener(ActionEvent event) {
         PrivilegeBean pb = getPrivilegeBean(event);
         assert (pb != null);
 
         Effect e;
 
         e = new Fade();
         e.setSubmit(true);
         e.setTransitory(false);
         pb.setNameCellEffect(e);
 
         e = new Fade();
         e.setSubmit(true);
         e.setTransitory(false);
         pb.setResourcesCellEffect(e);
 
         e = new Fade();
         e.setSubmit(true);
         e.setTransitory(false);
         pb.setExceptionsCellEffect(e);
 
         e = new Fade();
         e.setSubmit(true);
         e.setTransitory(false);
         pb.setSubjectCellEffect(e);
 
         e = new Fade();
         e.setSubmit(true);
         e.setTransitory(false);
         pb.setConditionCellEffect(e);
 
         e = new Fade();
         e.setSubmit(true);
         e.setTransitory(false);
         pb.setActionCellEffect(e);
 
         e = new Fade();
         e.setSubmit(true);
         e.setTransitory(false);
         pb.setRemoveCellEffect(e);
 
         addRemoveAction(pb);
     }
 
     public void handlePolicyRemove(PrivilegeBean pb) {
         policyManageBean.getPrivilegeBeans().remove(pb);
         policyDao.removePrivilege(pb.getName());
     }
 
     private void addRemoveAction(PrivilegeBean pb) {
         PhaseEventAction pea = new PhaseEventAction();
         pea.setDoBeforePhase(false);
         pea.setPhaseId(PhaseId.RENDER_RESPONSE);
         pea.setAction("#{policyManageHandler.handlePolicyRemove}");
         pea.setParameters(new Class[]{PrivilegeBean.class});
         pea.setArguments(new Object[]{pb});
 
         queuedActionBean.getPhaseEventActions().add(pea);
     }
 
     public void setQueuedActionBean(QueuedActionBean queuedActionBean) {
         this.queuedActionBean = queuedActionBean;
     }
 
     public void setPolicyDao(PolicyDao policyDao) {
         this.policyDao = policyDao;
     }
 
     public void setPolicyEditWizardBean(PolicyWizardBean policyEditWizardBean) {
         this.policyEditWizardBean = policyEditWizardBean;
     }
 }
 
