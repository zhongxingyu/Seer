 package com.sun.identity.admin.handler;
 
 import com.icesoft.faces.component.dragdrop.DndEvent;
 import com.icesoft.faces.component.dragdrop.DropEvent;
 import com.icesoft.faces.context.effects.Effect;
 import com.icesoft.faces.context.effects.Appear;
 import com.sun.identity.admin.model.Action;
 import com.sun.identity.admin.model.AndViewCondition;
 import com.sun.identity.admin.model.Application;
 import com.sun.identity.admin.model.ConditionType;
 import com.sun.identity.admin.model.NotViewCondition;
 import com.sun.identity.admin.model.OrViewCondition;
 import com.sun.identity.admin.model.PolicyCreateWizardBean;
 import com.sun.identity.admin.model.Resource;
 import com.sun.identity.admin.model.SubjectContainer;
 import com.sun.identity.admin.model.SubjectContainerType;
 import com.sun.identity.admin.model.ViewCondition;
 import com.sun.identity.admin.model.ViewSubject;
 import com.sun.identity.entitlement.Entitlement;
 import com.sun.identity.entitlement.EntitlementCondition;
 import com.sun.identity.entitlement.EntitlementSubject;
 import com.sun.identity.entitlement.OrSubject;
 import com.sun.identity.entitlement.Privilege;
 import com.sun.identity.entitlement.PrivilegeManager;
 import com.sun.identity.entitlement.ResourceAttributes;
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import javax.faces.event.ActionEvent;
 import javax.faces.event.ValueChangeEvent;
 import javax.security.auth.Subject;
 import java.util.Stack;
 
 public class PolicyCreateWizardHandler
         extends WizardHandler
         implements Serializable {
 
     @Override
     public String finishAction() {
         PolicyCreateWizardBean pcwb = getPolicyCreateWizardBean();
 
         // name, description
         String name = pcwb.getName();
         // TODO: where do we set the description
         String description = pcwb.getDescription();
 
         // subjects
         Set<EntitlementSubject> eSubjects = new HashSet<EntitlementSubject>();
         for (SubjectContainer sc : pcwb.getSubjectContainers()) {
             List<ViewSubject> viewSubjects = sc.getViewSubjects();
             for (ViewSubject vs : viewSubjects) {
                 eSubjects.add(vs.getSubject());
             }
         }
         EntitlementSubject orSubject = new OrSubject(eSubjects);
 
 
         // resources / actions
         List<Action> actions = pcwb.getActions();
         List<Resource> resources = pcwb.getSelectedResources();
         Set<Entitlement> entitlements = new HashSet<Entitlement>();
         for (Resource r : resources) {
             Entitlement e = r.getEntitlement(actions);
             entitlements.add(e);
         }
 
         // conditions
         cleanConditions();
         ViewCondition conditionTree = buildConditionExpression(pcwb.getViewConditions());
        EntitlementCondition eCondition = null;
        if (conditionTree != null) {
            eCondition = conditionTree.getEntitlementCondition();
        }
 
         // resource attrs
         // TODO
         Set<ResourceAttributes> attrs = null;
 
         Privilege privilege = new Privilege(
                 name,
                 entitlements,
                 orSubject,
                 eCondition,
                 attrs);
 
         // TODO: add SSO token to public credentials
         Subject authSubject = new Subject();
         PrivilegeManager pm = PrivilegeManager.getInstance(authSubject);
 
         try {
             pm.addPrivilege(privilege);
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
 
         pcwb.reset();
         return "policy-created";
     }
 
     private void cleanConditions() {
         ViewCondition lastVc = getPolicyCreateWizardBean().getLastVisibleCondition();
         if (lastVc == null) {
             // no conditions
             return;
         }
         while ((lastVc = getPolicyCreateWizardBean().getLastVisibleCondition()).getConditionType().isExpression()) {
             getPolicyCreateWizardBean().getViewConditions().remove(lastVc);
         }
     }
 
     private ViewCondition buildConditionExpression(List<ViewCondition> vcs) {
         Stack<ViewCondition> output = new Stack<ViewCondition>();
         Stack<ViewCondition> operators = new Stack<ViewCondition>();
 
         for (ViewCondition vc : vcs) {
             if (vc.getConditionType().isExpression()) {
                 if (operators.size() > 0 &&
                         (vc instanceof OrViewCondition ||
                         vc instanceof AndViewCondition)) {
                     output.push(operators.pop());
                 }
                 operators.push(vc);
             } else {
                 output.push(vc);
             }
         }
         while (operators.size() > 0) {
             output.push(operators.pop());
         }
 
         ViewCondition tree = buildConditionTree(output);
         return tree;
     }
 
     private ViewCondition buildConditionTree(Stack<ViewCondition> output) {
        if (output.size() == 0) {
            return null;
        }
 
         ViewCondition head = output.pop();
         if (head instanceof AndViewCondition) {
             AndViewCondition andHead = (AndViewCondition) head;
             ViewCondition left = buildConditionTree(output);
             ViewCondition right = buildConditionTree(output);
             andHead.getAndViewConditions().add(left);
             andHead.getAndViewConditions().add(right);
 
             return andHead;
         } else if (head instanceof OrViewCondition) {
             OrViewCondition orHead = (OrViewCondition) head;
             ViewCondition left = buildConditionTree(output);
             ViewCondition right = buildConditionTree(output);
             orHead.getOrViewConditions().add(left);
             orHead.getOrViewConditions().add(right);
 
             return orHead;
         } else if (head instanceof NotViewCondition) {
             NotViewCondition notHead = (NotViewCondition) head;
             ViewCondition child = buildConditionTree(output);
             notHead.setNotCondition(child);
 
             return notHead;
         }
 
         return head;
     }
 
     @Override
     public String cancelAction() {
         getWizardBean().reset();
         return "policy-create-canceled";
     }
 
     @Override
     public void nextListener(ActionEvent event) {
         super.nextListener(event);
 
         int step = getStep(event);
         switch (step) {
         }
     }
 
     public void conditionDropListener(DropEvent dropEvent) {
         int type = dropEvent.getEventType();
         if (type == DndEvent.DROPPED) {
             ConditionType ct = (ConditionType) dropEvent.getTargetDragValue();
 
             // TODO: implicit or?
 
             ViewCondition vc = ct.newViewCondition();
             getPolicyCreateWizardBean().getViewConditions().add(vc);
 
             Effect e = new Appear();
             e.setTransitory(true);
             e.setSubmit(true);
             getPolicyCreateWizardBean().setDropConditionEffect(e);
         }
     }
 
     public void subjectContainerDropListener(DropEvent dropEvent) {
         int type = dropEvent.getEventType();
         if (type == DndEvent.DROPPED) {
             SubjectContainerType sct = (SubjectContainerType) dropEvent.getTargetDragValue();
             assert (sct != null);
 
             SubjectContainer sc = sct.newSubjectContainer();
             getPolicyCreateWizardBean().getSubjectContainers().add(sc);
 
             Effect e = new Appear();
             e.setTransitory(true);
             e.setSubmit(true);
             getPolicyCreateWizardBean().setDropSubjectContainerEffect(e);
         }
     }
 
     public void applicationChanged(ValueChangeEvent event) {
         String name = (String) event.getNewValue();
         Application a = getPolicyCreateWizardBean().getApplications().get(name);
         getPolicyCreateWizardBean().setApplication(a);
     }
 
     private PolicyCreateWizardBean getPolicyCreateWizardBean() {
         return (PolicyCreateWizardBean) getWizardBean();
     }
 }
