 package com.sun.identity.admin.handler;
 
 import com.sun.identity.admin.model.PhaseEventAction;
 import com.sun.identity.admin.model.PolicyCreateWizardBean;
 import com.sun.identity.admin.model.PolicyEditWizardBean;
 import com.sun.identity.admin.model.PolicyManageBean;
 import com.sun.identity.admin.model.QueuedActionBean;
 import com.sun.identity.admin.model.RealmsBean;
 import com.sun.identity.admin.model.ViewApplicationsBean;
 import java.io.Serializable;
 import javax.faces.event.PhaseId;
 import javax.faces.event.ValueChangeEvent;
 
 public class RealmsHandler implements Serializable {
     private RealmsBean realmsBean;
     private QueuedActionBean queuedActionBean;
 
     public void realmChanged(ValueChangeEvent event) {
         PhaseEventAction pea = new PhaseEventAction();
         pea.setDoBeforePhase(true);
         pea.setPhaseId(PhaseId.RENDER_RESPONSE);
         pea.setAction("#{realmsHandler.handleReset}");
         pea.setParameters(new Class[]{});
         pea.setArguments(new Object[]{});
 
         queuedActionBean.getPhaseEventActions().add(pea);
     }
 
     public void handleReset() {
         PolicyManageBean pmb = PolicyManageBean.getInstance();
        pmb.reset();;
         PolicyCreateWizardBean pcwb = PolicyCreateWizardBean.getInstance();
         pcwb.reset();
         PolicyEditWizardBean pewb = PolicyEditWizardBean.getInstance();
         pewb.reset();
         ViewApplicationsBean vasb = ViewApplicationsBean.getInstance();
         vasb.reset();
     }
 
     public void setRealmsBean(RealmsBean realmsBean) {
         this.realmsBean = realmsBean;
     }
 
     public void setQueuedActionBean(QueuedActionBean queuedActionBean) {
         this.queuedActionBean = queuedActionBean;
     }
 }
