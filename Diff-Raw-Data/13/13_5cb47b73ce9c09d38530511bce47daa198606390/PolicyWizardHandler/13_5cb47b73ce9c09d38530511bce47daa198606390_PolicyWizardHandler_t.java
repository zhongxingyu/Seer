 package com.sun.identity.admin.handler;
 
 import com.icesoft.faces.component.dragdrop.DndEvent;
 import com.icesoft.faces.component.dragdrop.DropEvent;
 import com.icesoft.faces.context.effects.Effect;
 import com.icesoft.faces.context.effects.Fade;
 import com.icesoft.faces.context.effects.Highlight;
 import com.icesoft.faces.context.effects.SlideDown;
 import com.icesoft.faces.context.effects.SlideUp;
 import com.sun.identity.admin.Resources;
 import com.sun.identity.admin.dao.PolicyDao;
 import com.sun.identity.admin.model.ConditionType;
 import com.sun.identity.admin.effect.InputFieldErrorEffect;
 import com.sun.identity.admin.effect.MessageErrorEffect;
 import com.sun.identity.admin.model.BooleanAction;
 import com.sun.identity.admin.model.ContainerViewCondition;
 import com.sun.identity.admin.model.ContainerViewSubject;
 import com.sun.identity.admin.model.MessageBean;
 import com.sun.identity.admin.model.MessagesBean;
 import com.sun.identity.admin.model.MultiPanelBean;
 import com.sun.identity.admin.model.PhaseEventAction;
 import com.sun.identity.admin.model.PolicyWizardBean;
 import com.sun.identity.admin.model.PolicyManageBean;
 import com.sun.identity.admin.model.QueuedActionBean;
 import com.sun.identity.admin.model.SubjectType;
 import com.sun.identity.admin.model.Tree;
 import com.sun.identity.admin.model.ViewCondition;
 import com.sun.identity.admin.model.ViewSubject;
 import com.sun.identity.admin.model.WizardBean;
 import com.sun.identity.entitlement.Privilege;
 import java.io.Serializable;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.faces.application.FacesMessage;
 import javax.faces.component.UIComponent;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.PhaseId;
 import javax.faces.validator.ValidatorException;
 
 public abstract class PolicyWizardHandler
         extends WizardHandler
         implements Serializable, PolicyNameHandler, PolicySubjectsHandler,
        PolicyConditionsHandler, PolicySummaryHandler {
 
     private Pattern POLICY_NAME_PATTERN = Pattern.compile("[0-9a-zA-Z]+");
     private PolicyDao policyDao;
     private PolicyManageBean policyManageBean;
     private QueuedActionBean queuedActionBean;
     private String managedBeanName;
     private MessagesBean messagesBean;
 
     @Override
     public void setWizardBean(WizardBean wizardBean) {
         super.setWizardBean(wizardBean);
     }
 
     protected abstract String getFinishAction();
 
     protected abstract String getCancelAction();
 
     @Override
     public String finishAction() {
         Privilege privilege = getPolicyWizardBean().getPrivilegeBean().toPrivilege();
         policyDao.setPrivilege(privilege);
 
         MessageBean mb = new MessageBean();
         Resources r = new Resources();
         mb.setSummary(r.getString(this.getClass(), "finish"));
         mb.setSeverity(FacesMessage.SEVERITY_INFO);
         messagesBean.addMessageBean(mb);
 
         getPolicyWizardBean().reset();
         getPolicyManageBean().reset();
 
         return getFinishAction();
     }
 
     @Override
     public String cancelAction() {
         MessageBean mb = new MessageBean();
         Resources r = new Resources();
         mb.setSummary(r.getString(this.getClass(), "cancel"));
         mb.setSeverity(FacesMessage.SEVERITY_INFO);
         messagesBean.addMessageBean(mb);
 
         getPolicyWizardBean().reset();
 
         return getCancelAction();
     }
 
     @Override
     public void nextListener(ActionEvent event) {
         super.nextListener(event);
 
         int step = getStep(event);
         switch (step) {
             // resources
             case 1:
                 break;
         }
     }
 
     public void conditionDropListener(DropEvent dropEvent) {
         int type = dropEvent.getEventType();
         if (type == DndEvent.DROPPED) {
             ConditionType dragValue = (ConditionType) dropEvent.getTargetDragValue();
             ContainerViewCondition dropValue = (ContainerViewCondition) dropEvent.getTargetDropValue();
 
             // TODO: implicit or?
 
             ViewCondition vc = dragValue.newViewCondition();
             if (dropValue == null) {
                 getPolicyWizardBean().getPrivilegeBean().setViewCondition(vc);
             } else {
                 dropValue.addViewCondition(vc);
             }
 
             Effect e;
 
             e = new Highlight();
             e.setTransitory(false);
             e.setSubmit(true);
             getPolicyWizardBean().setDropConditionEffect(e);
         }
     }
 
     public void subjectDropListener(DropEvent dropEvent) {
         int type = dropEvent.getEventType();
         if (type == DndEvent.DROPPED) {
             Object dragValue = dropEvent.getTargetDragValue();
             assert (dragValue != null);
             ContainerViewSubject dropValue = (ContainerViewSubject) dropEvent.getTargetDropValue();
 
             SubjectType st = null;
             ViewSubject vs = null;
 
             if (dragValue instanceof SubjectType) {
                 st = (SubjectType) dragValue;
                 vs = st.newViewSubject();
             } else {
                 vs = (ViewSubject) dragValue;
             }
 
             if (dropValue == null) {
                 getPolicyWizardBean().getPrivilegeBean().setViewSubject(vs);
             } else {
                 dropValue.addViewSubject(vs);
             }
 
             Effect e;
 
             e = new Highlight();
             e.setTransitory(false);
             e.setSubmit(true);
             getPolicyWizardBean().setDropConditionEffect(e);
         }
     }
 
     private PolicyWizardBean getPolicyWizardBean() {
         return (PolicyWizardBean) getWizardBean();
     }
 
     protected int getGotoAdvancedTabIndex(ActionEvent event) {
         String i = (String) event.getComponent().getAttributes().get("gotoAdvancedTabIndex");
         int index = Integer.parseInt(i);
 
         return index;
     }
 
     protected BooleanAction getBooleanAction(ActionEvent event) {
         BooleanAction ba = (BooleanAction) event.getComponent().getAttributes().get("booleanAction");
         assert (ba != null);
 
         return ba;
     }
 
     public void editNameListener(ActionEvent event) {
         gotoStepListener(event);
     }
 
     public void editResourcesListener(ActionEvent event) {
         gotoStepListener(event);
     }
 
    public void editExceptionsListener(ActionEvent event) {
        gotoStepListener(event);
    }

     public void editSubjectsListener(ActionEvent event) {
         gotoStepListener(event);
     }
 
     public void editConditionsListener(ActionEvent event) {
         gotoStepListener(event);
     }
 
     public void editActionsListener(ActionEvent event) {
         gotoStepListener(event);
     }
 
     @Override
     public void gotoStepListener(ActionEvent event) {
         super.gotoStepListener(event);
 
         // TODO, enumerate the tabs
         if (getGotoStep(event) == 3) {
             int i = getGotoAdvancedTabIndex(event);
             getPolicyWizardBean().setAdvancedTabsetIndex(i);
         }
     }
 
     public void actionRemoveListener(ActionEvent event) {
         BooleanAction ba = getBooleanAction(event);
     }
 
     public void validatePolicyName(FacesContext context, UIComponent component, Object value) throws ValidatorException {
         String policyName = (String) value;
         Matcher matcher = POLICY_NAME_PATTERN.matcher(policyName);
 
         if (!matcher.matches()) {
             MessageBean mb = new MessageBean();
             Resources r = new Resources();
             mb.setSummary(r.getString(this.getClass(), "invalidPolicyNameSummary"));
             mb.setDetail(r.getString(this.getClass(), "invalidPolicyNameDetail"));
             mb.setDetail("Policy name must be 1 or more alpha-numeric characters");
             mb.setSeverity(FacesMessage.SEVERITY_ERROR);
 
             Effect e;
 
             e = new InputFieldErrorEffect();
             getPolicyWizardBean().setPolicyNameInputEffect(e);
 
             e = new MessageErrorEffect();
             getPolicyWizardBean().setPolicyNameMessageEffect(e);
 
             throw new ValidatorException(mb.toFacesMessage());
         }
     }
 
     public void setPolicyDao(PolicyDao policyDao) {
         this.policyDao = policyDao;
     }
 
     public void setPolicyManageBean(PolicyManageBean policyManageBean) {
         this.policyManageBean = policyManageBean;
     }
 
     public PolicyManageBean getPolicyManageBean() {
         return policyManageBean;
     }
 
     public void panelExpandListener(ActionEvent event) {
         MultiPanelBean mpb = (MultiPanelBean) event.getComponent().getAttributes().get("bean");
         assert (mpb != null);
 
         Effect e;
         if (mpb.isPanelExpanded()) {
             e = new SlideUp();
         } else {
             e = new SlideDown();
         }
 
         e.setTransitory(false);
         e.setSubmit(true);
         mpb.setPanelExpandEffect(e);
     }
 
     public void panelRemoveListener(ActionEvent event) {
         MultiPanelBean mpb = (MultiPanelBean) event.getComponent().getAttributes().get("bean");
         assert (mpb != null);
 
         Effect e = new Fade();
         e.setSubmit(true);
         e.setTransitory(false);
         mpb.setPanelEffect(e);
 
         addPanelRemoveAction(mpb);
     }
 
     public void handlePanelRemove(MultiPanelBean mpb) {
         if (mpb instanceof ViewSubject) {
             ViewSubject vs = (ViewSubject)mpb;
             Tree subjectTree = new Tree(getPolicyWizardBean().getPrivilegeBean().getViewSubject());
             ViewSubject rootVs = (ViewSubject) subjectTree.remove(vs);
             getPolicyWizardBean().getPrivilegeBean().setViewSubject(rootVs);
         } else if (mpb instanceof ViewCondition) {
             ViewCondition vc = (ViewCondition)mpb;
             Tree conditionTree = new Tree(getPolicyWizardBean().getPrivilegeBean().getViewCondition());
             ViewCondition rootVc = (ViewCondition) conditionTree.remove(vc);
             getPolicyWizardBean().getPrivilegeBean().setViewCondition(rootVc);
         } else {
             throw new RuntimeException("unhandled multi-panel bean: " + mpb);
         }
     }
 
     private void addPanelRemoveAction(MultiPanelBean mpb) {
         PhaseEventAction pea = new PhaseEventAction();
         pea.setDoBeforePhase(false);
         pea.setPhaseId(PhaseId.RENDER_RESPONSE);
         pea.setAction("#{" + managedBeanName + ".handlePanelRemove}");
         pea.setParameters(new Class[]{MultiPanelBean.class});
         pea.setArguments(new Object[]{mpb});
 
         queuedActionBean.getPhaseEventActions().add(pea);
     }
 
     public void setQueuedActionBean(QueuedActionBean queuedActionBean) {
         this.queuedActionBean = queuedActionBean;
     }
 
     public void setManagedBeanName(String managedBeanName) {
         this.managedBeanName = managedBeanName;
     }
 
     public void setMessagesBean(MessagesBean messagesBean) {
         this.messagesBean = messagesBean;
     }
 }
