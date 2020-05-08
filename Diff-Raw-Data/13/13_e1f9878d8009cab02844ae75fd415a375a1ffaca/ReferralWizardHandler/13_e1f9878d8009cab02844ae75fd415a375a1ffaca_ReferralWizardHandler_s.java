 /**
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright (c) 2009 Sun Microsystems Inc. All Rights Reserved
  *
  * The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: ReferralWizardHandler.java,v 1.8 2009-06-10 04:46:22 farble1670 Exp $
  */
 package com.sun.identity.admin.handler;
 
 import com.icesoft.faces.context.effects.Effect;
 import com.sun.identity.admin.NamePattern;
 import com.sun.identity.admin.Resources;
 import com.sun.identity.admin.dao.ReferralDao;
 import com.sun.identity.admin.effect.InputFieldErrorEffect;
 import com.sun.identity.admin.model.LinkBean;
 import com.sun.identity.admin.model.MessageBean;
 import com.sun.identity.admin.model.MessagesBean;
 import com.sun.identity.admin.model.NextPopupBean;
 import com.sun.identity.admin.model.QueuedActionBean;
 import com.sun.identity.admin.model.RealmBean;
 import com.sun.identity.admin.model.ReferralBean;
 import com.sun.identity.admin.model.ReferralResource;
 import com.sun.identity.admin.model.ReferralWizardBean;
 import com.sun.identity.admin.model.ReferralWizardStep;
 import com.sun.identity.admin.model.Resource;
 import com.sun.identity.admin.model.ViewEntitlement;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import javax.faces.application.FacesMessage;
 import javax.faces.event.ActionEvent;
 
 public abstract class ReferralWizardHandler extends WizardHandler {
 
     private MessagesBean messagesBean;
     private QueuedActionBean queuedActionBean;
     private ReferralDao referralDao;
 
     public void setMessagesBean(MessagesBean messagesBean) {
         this.messagesBean = messagesBean;
     }
 
     private boolean validateSteps() {
         if (!validateName()) {
             return false;
         }
         if (!validateResources()) {
             return false;
         }
         if (!validateSubjects()) {
             return false;
         }
 
         return true;
     }
 
     public abstract void doFinishNext();
 
     public abstract void doCancelNext();
 
     @Override
     public void finishListener(ActionEvent event) {
         if (!validateSteps()) {
             return;
         }
 
         ReferralBean rb = getReferralWizardBean().getReferralBean();
         referralDao.add(rb);
 
         getWizardBean().reset();
 
         doFinishNext();
     }
 
     @Override
     public void cancelListener(ActionEvent event) {
         getWizardBean().reset();
 
         doCancelNext();
     }
 
     @Override
     public void expandListener(ActionEvent event) {
         if (!validateSteps()) {
             return;
         }
 
         super.expandListener(event);
     }
 
     private boolean validateName() {
         String name = getReferralWizardBean().getReferralBean().getName();
         Matcher matcher = NamePattern.get().matcher(name);
 
         if (!matcher.matches()) {
             MessageBean mb = new MessageBean();
             Resources r = new Resources();
             mb.setSummary(r.getString(this, "invalidNameSummary"));
             mb.setDetail(r.getString(this, "invalidNameDetail"));
             mb.setSeverity(FacesMessage.SEVERITY_ERROR);
 
             Effect e;
 
             e = new InputFieldErrorEffect();
             getReferralWizardBean().setNameInputEffect(e);
 
             getMessagesBean().addMessageBean(mb);
             getReferralWizardBean().gotoStep(ReferralWizardStep.NAME.toInt());
 
             return false;
         }
 
         return true;
     }
 
     private boolean validateResources() {
         List<Resource> resources = getReferralWizardBean().getReferralBean().getResources();
         if (resources != null) {
             for (Resource r : getReferralWizardBean().getReferralBean().getResources()) {
                 ReferralResource rr = (ReferralResource) r;
                 ViewEntitlement ve = rr.getViewEntitlement();
                 if (ve.getResources() != null && ve.getResources().size() > 0) {
                     return true;
                 }
             }
         }
 
         MessageBean mb = new MessageBean();
         Resources r = new Resources();
         mb.setSummary(r.getString(this, "noResourcesSummary"));
         mb.setDetail(r.getString(this, "noResourcesDetail"));
         mb.setSeverity(FacesMessage.SEVERITY_ERROR);
         getMessagesBean().addMessageBean(mb);
 
         return false;
     }
 
     private boolean validateSubjects() {
         List<RealmBean> realmBeans = getReferralWizardBean().getReferralBean().getRealmBeans();
         if (realmBeans == null || realmBeans.size() == 0) {
             MessageBean mb = new MessageBean();
             Resources r = new Resources();
             mb.setSummary(r.getString(this, "noSubjectsSummary"));
             mb.setDetail(r.getString(this, "noSubjectsDetail"));
             mb.setSeverity(FacesMessage.SEVERITY_ERROR);
             getMessagesBean().addMessageBean(mb);
             
             return false;
         }
 
         return true;
     }
 
     @Override
     public void nextListener(ActionEvent event) {
         int step = getStep(event);
         ReferralWizardStep rws = ReferralWizardStep.valueOf(step);
 
         switch (rws) {
             case NAME:
                 if (!validateName()) {
                     return;
                 }
                 break;
 
             case RESOURCES:
                 if (!validateResources()) {
                     return;
                 }
                 break;
 
             case SUBJECTS:
                 if (!validateSubjects()) {
                     return;
                 }
                 break;
 
             default:
                 throw new AssertionError("unhandled step: " + rws);
         }
 
         super.nextListener(event);
     }
 
     @Override
     public void previousListener(ActionEvent event) {
         int step = getStep(event);
         ReferralWizardStep rws = ReferralWizardStep.valueOf(step);
 
         switch (rws) {
             case NAME:
                 if (!validateName()) {
                     return;
                 }
                 break;
 
             case RESOURCES:
                 if (!validateResources()) {
                     return;
                 }
                 break;
 
             case SUBJECTS:
                 if (!validateSubjects()) {
                     return;
                 }
                 break;
 
             default:
                 throw new AssertionError("unhandled step: " + rws);
         }
 
         super.previousListener(event);
     }
 
     public ReferralWizardBean getReferralWizardBean() {
         return (ReferralWizardBean) getWizardBean();
     }
 
     public abstract String getBeanName();
 
     public void subjectsAddListener(ActionEvent event) {
         /*
         PhaseEventAction pea = new PhaseEventAction();
         pea.setDoBeforePhase(true);
         pea.setPhaseId(PhaseId.RENDER_RESPONSE);
         pea.setAction("#{" + getBeanName() + ".handleSubjectsAdd}");
         pea.setParameters(new Class[]{});
         pea.setArguments(new Object[]{});
 
         queuedActionBean.getPhaseEventActions().add(pea);
          */
         handleSubjectsAdd();
     }
 
     public void handleSubjectsAdd() {
         List<RealmBean> availableValue = getReferralWizardBean().getSelectedAvailableRealmBeans();
         getReferralWizardBean().setSelectedAvailableRealmBeans(null);
         List<RealmBean> available = getReferralWizardBean().getAvailableRealmBeans();
         List<RealmBean> selected = getReferralWizardBean().getReferralBean().getRealmBeans();
 
         available.removeAll(availableValue);
         selected.addAll(availableValue);
     }
 
     public void subjectsRemoveListener(ActionEvent event) {
         /*
         PhaseEventAction pea = new PhaseEventAction();
         pea.setDoBeforePhase(true);
         pea.setPhaseId(PhaseId.RENDER_RESPONSE);
         pea.setAction("#{" + getBeanName() + ".handleSubjectsRemove}");
         pea.setParameters(new Class[]{});
         pea.setArguments(new Object[]{});
 
         queuedActionBean.getPhaseEventActions().add(pea);
          */
         handleSubjectsRemove();
     }
 
     public void handleSubjectsRemove() {
         List<RealmBean> selectedValue = getReferralWizardBean().getSelectedRealmBeans();
         getReferralWizardBean().setSelectedRealmBeans(null);
         List<RealmBean> available = getReferralWizardBean().getAvailableRealmBeans();
         List<RealmBean> selected = getReferralWizardBean().getReferralBean().getRealmBeans();
 
        getReferralWizardBean().resetAvailableRealmBeans();
         selected.removeAll(selectedValue);
     }
 
     public void setQueuedActionBean(QueuedActionBean queuedActionBean) {
         this.queuedActionBean = queuedActionBean;
     }
 
     public void setReferralDao(ReferralDao referralDao) {
         this.referralDao = referralDao;
     }
 
     public MessagesBean getMessagesBean() {
         return messagesBean;
     }
 }
