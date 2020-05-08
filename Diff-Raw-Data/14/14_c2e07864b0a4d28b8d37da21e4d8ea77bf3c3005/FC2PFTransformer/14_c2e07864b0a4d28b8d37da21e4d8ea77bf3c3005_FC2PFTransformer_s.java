 /*******************************************************************************
  * Copyright (c) 2004, 2006 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jsf.facesconfig.ui.pageflow.synchronization;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.notify.impl.AdapterImpl;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.jst.jsf.facesconfig.emf.DescriptionType;
 import org.eclipse.jst.jsf.facesconfig.emf.DisplayNameType;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigFactory;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigPackage;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigType;
 import org.eclipse.jst.jsf.facesconfig.emf.FromViewIdType;
 import org.eclipse.jst.jsf.facesconfig.emf.IconType;
 import org.eclipse.jst.jsf.facesconfig.emf.LargeIconType;
 import org.eclipse.jst.jsf.facesconfig.emf.NavigationCaseType;
 import org.eclipse.jst.jsf.facesconfig.emf.NavigationRuleType;
 import org.eclipse.jst.jsf.facesconfig.emf.SmallIconType;
 import org.eclipse.jst.jsf.facesconfig.emf.ToViewIdType;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.Pageflow;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.PageflowElement;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.PageflowFactory;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.PageflowLink;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.PageflowNode;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.PageflowPage;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.impl.PageReferenceElement;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.impl.ReferenceElement;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.util.PageflowModelManager;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.util.PageflowTransform;
 import org.eclipse.jst.jsf.facesconfig.ui.util.WebrootUtil;
 
 /**
  * Update and synchronize pageflow from faces-config or vice-versa.
  * 
  * @author hmeng
  * 
  */
 
 public class FC2PFTransformer extends AdapterImpl {
 
 	// For code debug.
 	private static final boolean DEBUG = false;
 
 	public static final int MY_NOTIFICATION_TYPE = Notification.EVENT_TYPE_COUNT + 1;
 
 	public static final int MY_NOTIFICATION_TYPE1 = MY_NOTIFICATION_TYPE + 1;
 
 	/**
 	 * The listener is active.
 	 */
 	private boolean listenToNotify;
 
 	/**
 	 * Avoid redundent event.
 	 */
 	private boolean isInEvent;
 
 	boolean pageflowChanged = false;
 
 	/**
 	 * Perform operations batched.
 	 */
 	boolean needBatchOperations = false;
 
 	/**
 	 * Catches
 	 */
 	private Map mapPaths2PF = new HashMap();
 
 	private Map mapCases2Links = new HashMap();
 
 	/**
 	 * The pageflow model.
 	 */
 	private Pageflow pageflow;
 
 	/**
 	 * The faces-config model.
 	 */
 	FacesConfigType facesConfig;
 
 	public FC2PFTransformer() {
 		super();
 		// TODO Auto-generated constructor stub
 	}
 
 	public void setListenToNotify(boolean listenToNotify) {
 		this.listenToNotify = listenToNotify;
 	}
 
 	private void postEMFChanged() {
 		startFCNotify();
 		// Refresh all facesConfig?
 		facesConfig.eNotify(new ENotificationImpl(
 				(InternalEObject) facesConfig, Notification.REMOVE,
 				FacesConfigPackage.FACES_CONFIG_TYPE__NAVIGATION_RULE, null,
 				null));
 		List rules;
 		rules = facesConfig.getNavigationRule();
 		for (int i = 0; i < rules.size(); i++) {
 			((NavigationRuleType) rules.get(i)).eNotify(new ENotificationImpl(
 					(InternalEObject) rules.get(i), Notification.REMOVE,
 					FacesConfigPackage.NAVIGATION_RULE_TYPE__NAVIGATION_CASE,
 					null, null));
 		}
 	}
 
 	/**
 	 * Set notify adapters.
 	 * 
 	 */
 	private void startFCNotify() {
 		TreeIterator iterator = facesConfig.eAllContents();
 		while (iterator.hasNext()) {
 			EObject object = (EObject) iterator.next();
 			object.eSetDeliver(true);
 		}
 		facesConfig.eSetDeliver(true);
 	}
 
 	/**
 	 * Set not notify adapters
 	 * 
 	 * @return
 	 */
 	private TreeIterator stopFCNotify() {
 		TreeIterator iterator = facesConfig.eAllContents();
 		// Stop the listener
 		while (iterator.hasNext()) {
 			EObject object = (EObject) iterator.next();
 			object.eSetDeliver(false);
 		}
 		facesConfig.eSetDeliver(false);
 		return iterator;
 	}
 
 	/**
 	 * For performance issue, we are going to update DOM only after the EMF
 	 * modifications are done.
 	 */
 	boolean isNeedBatchOperations() {
 		return needBatchOperations;
 	}
 
 	/**
 	 * @param enable
 	 */
 	public void setNeedBatchOperations(boolean enable) {
 		this.needBatchOperations = enable;
 		if (!enable) {
 			isInEvent = true;
 			postEMFChanged();
 			isInEvent = false;
 		} else {
 			stopFCNotify();
 		}
 	}
 
 	private void updateFacesRule(NavigationRuleType navigationRule,
 			PageflowPage pageSource) {
 		FacesConfigFactory factory = FacesConfigFactory.eINSTANCE;
 		if (pageSource.getComment() != null) {
 			DescriptionType description = factory.createDescriptionType();
 			description.setTextContent(pageSource.getComment());
 			navigationRule.getDescription().clear();
 			navigationRule.getDescription().add(description);
 		}
 		// set the navigation rule's icon
 		if (pageSource.getLargeicon() != null
 				|| pageSource.getSmallicon() != null) {
 			IconType icon = factory.createIconType();
 			if (pageSource.getLargeicon() != null) {
 				LargeIconType largeicon = factory.createLargeIconType();
 				largeicon.setTextContent(pageSource.getLargeicon());
 				icon.setLargeIcon(largeicon);
 			}
 
 			if (pageSource.getSmallicon() != null) {
 				SmallIconType smallicon = factory.createSmallIconType();
 				smallicon.setTextContent(pageSource.getSmallicon());
 				icon.setSmallIcon(smallicon);
 			}
 			navigationRule.getIcon().clear();
 			navigationRule.getIcon().add(icon);
 		}
 		if (pageSource.getName() != null) {
 			DisplayNameType displayName = factory.createDisplayNameType();
 			displayName.setTextContent(pageSource.getName());
 			navigationRule.getDisplayName().clear();
 			navigationRule.getDisplayName().add(displayName);
 		}
 	}
 
 	/**
 	 * TODO: If we cache the case's properties in a link, then we can restore
 	 * them.
 	 * 
 	 * @param navigationCase
 	 * @param link
 	 */
     // TODO: not used, dead?
 //	private void updateFacesCase(NavigationCaseType navigationCase,
 //			PageflowLink link) {
 //		FacesConfigFactory factory = FacesConfigFactory.eINSTANCE;
 //		if (link.getComment() != null) {
 //			DescriptionType description = factory.createDescriptionType();
 //			description.setTextContent(link.getComment());
 //			navigationCase.getDescription().clear();
 //			navigationCase.getDescription().add(description);
 //		}
 //		// set the navigation rule's icon
 //		if (link.getLargeicon() != null || link.getSmallicon() != null) {
 //			IconType icon = factory.createIconType();
 //			if (link.getLargeicon() != null) {
 //				LargeIconType largeicon = factory.createLargeIconType();
 //				largeicon.setTextContent(link.getLargeicon());
 //				icon.setLargeIcon(largeicon);
 //			}
 //
 //			if (link.getSmallicon() != null) {
 //				SmallIconType smallicon = factory.createSmallIconType();
 //				smallicon.setTextContent(link.getSmallicon());
 //				icon.setSmallIcon(smallicon);
 //			}
 //			navigationCase.getIcon().clear();
 //			navigationCase.getIcon().add(icon);
 //		}
 //		if (link.getFromaction() != null) {
 //			FromActionType fromAction = factory.createFromActionType();
 //			fromAction.setTextContent(link.getFromaction());
 //			navigationCase.setFromAction(fromAction);
 //		}
 //		if (link.getOutcome() != null) {
 //			FromOutcomeType fromOutcome = factory.createFromOutcomeType();
 //			fromOutcome.setTextContent(link.getOutcome());
 //			navigationCase.setFromOutcome(fromOutcome);
 //		}
 //	}
 
 	private void createFacesCase(PageflowLink link, PageflowPage source,
 			PageflowPage target_) {
 		NavigationRuleType rule = FacesConfigFactory.eINSTANCE
 				.createNavigationRuleType();
 		FromViewIdType from = FacesConfigFactory.eINSTANCE
 				.createFromViewIdType();
 		from.setTextContent((source).getPath());
 		rule.setFromViewId(from);
 		NavigationCaseType navCase = FacesConfigFactory.eINSTANCE
 				.createNavigationCaseType();
 		ToViewIdType toView = FacesConfigFactory.eINSTANCE.createToViewIdType();
 		toView.setTextContent((target_).getPath());
 		navCase.setToViewId(toView);
 		rule.getNavigationCase().add(navCase);
 		facesConfig.getNavigationRule().add(rule);
 		updateFacesRule(rule, source);
 		source.getFCElements().add(from);
 		target_.getFCElements().add(toView);
 		link.getFCElements().add(navCase);
 		mapCases2Links.put(navCase, link);
 	}
 
 	private void removePageflowPage(PageflowPage source) {
 		Assert.isTrue(source.getOutlinks().size() == 0
 				&& source.getInlinks().size() == 0);
 		pageflow.getNodes().remove(source);
 	}
 
 	void removeCase(NavigationCaseType navCase) {
 		NavigationRuleType rule = (NavigationRuleType) navCase.eContainer();
 		(rule).getNavigationCase().remove(navCase);
 		if (rule.getNavigationCase().size() == 0) {
 			facesConfig.getNavigationRule().remove(rule);
 		}
 	}
 
 	/**
 	 * Remove a link in pageflow model.
 	 * 
 	 * @param pLink
 	 */
 	private void removeLink(PageflowLink pLink) {
 		// Link
 		PageflowPage source = (PageflowPage) pLink.getSource();
 		PageflowPage target_ = (PageflowPage) pLink.getTarget();
 		pLink.getPageflow().getLinks().remove(pLink);
 		source.getOutlinks().remove(pLink);
 		target_.getInlinks().remove(pLink);
 		// Navigation case
 		pLink.getFCElements().clear();
 		cleanPage(source);
 		cleanPage(target_);
 	}
 
 	/**
 	 * Remove a link in pageflow model.
 	 * 
 	 * @param pLink
 	 */
 	void changePFLinkStart(PageflowLink pLink, String newValue) {
 		PageflowPage newPage = null;
 		if ((newPage = getValidPFPageInCache(newValue)) != null) {
 			if (newValue.equals(((PageflowPage) pLink.getSource()).getPath())) {
 				return;
 			}
 		} else if ((newPage = TransformUtil.findPage(newValue, pageflow)) == null) {
 			return;
 		}
 
 		// remove old case
 		NavigationCaseType caseType = (NavigationCaseType) pLink
 				.getFCElements().getData().get(0);
 		//NavigationRuleType rule = (NavigationRuleType) caseType.eContainer();
 		removeCase(caseType);
 
 		// create new rule / case
 		NavigationCaseType newCase = FacesConfigFactory.eINSTANCE
 				.createNavigationCaseType();
 		ToViewIdType toView = FacesConfigFactory.eINSTANCE.createToViewIdType();
 		toView.setTextContent(caseType.getToViewId().getTextContent());
 		newCase.setToViewId(toView);
 
 		NavigationRuleType newRule = FacesConfigFactory.eINSTANCE
 				.createNavigationRuleType();
 		FromViewIdType fromView = FacesConfigFactory.eINSTANCE
 				.createFromViewIdType();
 		fromView.setTextContent(newValue);
 		newRule.setFromViewId(fromView);
 		newRule.getNavigationCase().add(newCase);
 		facesConfig.getNavigationRule().add(newRule);
 
 		// update
 		// updateNode(rule, newRule);
 		updateNode(caseType, newCase);
 
 		pLink.getFCElements().clear();
 		pLink.getFCElements().add(newCase);
 		pLink.setSource(newPage);
 
 		updatePageflowElements(pageflow, newCase);
 	}
 
 	private void updateNode(EObject old, EObject newOne) {
 		if (old instanceof NavigationRuleType) {
 			((NavigationRuleType) newOne).getDescription().clear();
 			((NavigationRuleType) newOne).getDescription().addAll(
 					((NavigationRuleType) old).getDescription());
 			((NavigationRuleType) newOne).getDisplayName().clear();
 			((NavigationRuleType) newOne).getDisplayName().addAll(
 					((NavigationRuleType) old).getDisplayName());
 			((NavigationRuleType) newOne).getDescription().clear();
 			((NavigationRuleType) newOne).getIcon().clear();
 			((NavigationRuleType) newOne).getIcon().addAll(
 					((NavigationRuleType) old).getIcon());
 		} else if (old instanceof NavigationCaseType) {
 			((NavigationCaseType) newOne).getDescription().clear();
 			((NavigationCaseType) newOne).getDescription().addAll(
 					((NavigationCaseType) old).getDescription());
 			((NavigationCaseType) newOne).getDisplayName().clear();
 			((NavigationCaseType) newOne).getDisplayName().addAll(
 					((NavigationCaseType) old).getDisplayName());
 			((NavigationCaseType) newOne).getDescription().clear();
 			((NavigationCaseType) newOne).getIcon().clear();
 			((NavigationCaseType) newOne).getIcon().addAll(
 					((NavigationCaseType) old).getIcon());
 			((NavigationCaseType) newOne)
 					.setFromAction(((NavigationCaseType) old).getFromAction());
 			((NavigationCaseType) newOne)
 					.setFromOutcome(((NavigationCaseType) old).getFromOutcome());
 		}
 	}
 
 	void changePFLinkEnd(PageflowLink pLink, String newValue) {
 		PageflowPage newPage = null;
 		if ((newPage = getValidPFPageInCache(newValue)) != null) {
 			if (newValue.equals(((PageflowPage) pLink.getSource()).getPath())) {
 				return;
 			}
 		} else if ((newPage = TransformUtil.findPage(newValue, pageflow)) == null) {
 			return;
 		}
 
 		// remove old case
 		NavigationCaseType caseType = (NavigationCaseType) pLink
 				.getFCElements().getData().get(0);
 		ToViewIdType toView = caseType.getToViewId(); // FacesConfigFactory.eINSTANCE.createToViewIdType();
 		if (toView == null) {
 			toView = FacesConfigFactory.eINSTANCE.createToViewIdType();
 		}
 		toView.setTextContent(newValue);
 		caseType.setToViewId(toView);
 
 		pLink.setTarget(newPage);
 
 		updatePageflowElements(pageflow, caseType);
 	}
 
 	void removeLink(NavigationCaseType caseType) {
 		PageflowLink link = (PageflowLink) mapCases2Links.get(caseType);
 		if (link != null) {
 			removeLink(link);
 		}
 	}
 
 	void addLink(PageflowPage source, PageflowPage target_, PageflowLink value) {
 		if (source != null && target_ != null && value != null) {
 			createFacesCase(value, source, target_);
 		}
 	}
 
 	private void cleanPage(PageflowPage source) {
 		if (source.getInlinks().size() == 0 && source.getOutlinks().size() == 0) {
 			removePageflowPage(source);
 		}
 		source.update();
 	}
 
 	void refreshPFAdapter(EObject object) {
 		TreeIterator content = object.eAllContents();
 		while (content.hasNext()) {
 			Object next = content.next();
 			adapt((EObject) next);
 		}
 		adapt(object);
 	}
 
 	void refreshFCAdapter(EObject object) {
 		List rules = facesConfig.getNavigationRule();
		for (int i = 0; i < rules.size(); i++) {
			NavigationRuleType rule = (NavigationRuleType) rules.get(i);
			adapt(rule);
			TreeIterator children = rule.eAllContents();
			while (children.hasNext()) {
				adapt((EObject) children.next());
 			}
 		}
 		adapt(facesConfig);
 	}
 
 	public void setFacesConfig(FacesConfigType facesConfig) {
 		this.facesConfig = facesConfig;
 		refreshFCAdapter(facesConfig);
 	}
 
 	public void setPageflow(Pageflow pageflow) {
 		this.pageflow = pageflow;
 		pageflow.eAdapters().add(this);
 		refreshPFAdapter(pageflow);
 	}
 
 	/**
 	 * Install compatible synchronization adapter for particular object.
 	 * 
 	 * @param object
 	 */
 	public void adapt(EObject object) {
 		if (object != null && !isAdapted(object)) {
 			if (object instanceof PageflowElement) {
 				object.eAdapters().add(new PF2FCSynchronizer(this));
 			} else {
 				object.eAdapters().add(new FC2PFSynchronizer(this));
 			}
 		}
 	}
 
 	/**
 	 * Remove the synchronization adapter.
 	 * 
 	 * @param object
 	 */
 	public void unAdapt(EObject object) {
 		Iterator adapters = object.eAdapters().iterator();
 		while (adapters.hasNext()) {
 			Object adapter = adapters.next();
 			if (adapter instanceof FC2PFSynchronizer
 					|| adapter instanceof PF2FCSynchronizer) {
 				adapters.remove();
 			}
 		}
 	}
 
 	public boolean isAdapted(EObject object) {
 		if (object != null) {
 			Iterator adapters = object.eAdapters().iterator();
 			while (adapters.hasNext()) {
 				Object adapter = adapters.next();
 				if (adapter instanceof FC2PFSynchronizer
 						|| adapter instanceof PF2FCSynchronizer) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public void clearCaches() {
 		mapPaths2PF.clear();
 		mapCases2Links.clear();
 	}
 
 	/**
 	 * dispose of this object.
 	 */
 	public void dispose() {
 		clearCaches();
 		Pageflow pageFlow = getPageflow();
 		
 		if (pageFlow != null)
 		{
 		    pageFlow.dispose();
 		}
 	}
 
 	public Notifier getTarget() {
 		return null;
 	}
 
 	public boolean isAdapterForType(Object type) {
 		return false;
 	}
 
 	public void setTarget(Notifier newTarget) {
 	    // do nothing
 	}
 
 	public static FromViewIdType getSource(NavigationCaseType caseType) {
 		if (caseType.eContainer() instanceof NavigationRuleType) {
 			NavigationRuleType rule = (NavigationRuleType) caseType
 					.eContainer();
 			return rule.getFromViewId();
 		}
 		return null;
 	}
 
 	private PageflowPage getOrCreateStartPage(NavigationCaseType navCase) {// FromViewIdType
 		PageflowPage startPageflowNode = null;
 		String path = "*";
 		NavigationRuleType rule = (NavigationRuleType) navCase.eContainer();
 		FromViewIdType source = getSource(navCase);
 		if (source != null) {
 			path = source.getTextContent();
 		}
 		if (getValidPFPageInCache(path) == null) {
 			startPageflowNode = TransformUtil.findPage(path, pageflow);
 
 			if (startPageflowNode == null) {
 				// not exist or is "*"
 				if (DEBUG)
 					System.out.println("Create new start page");
 				startPageflowNode = createPFPage(path);
 				pageflowChanged = true;
 			}
 			mapPaths2PF.put(path, startPageflowNode);
 			PageflowTransform.getInstance().updatePageflowSourcePage(
 					startPageflowNode, rule);
 		} else {
 			startPageflowNode = (PageflowPage) mapPaths2PF.get(path);
 		}
 		if (source != null) {
 			ReferenceElement element = new PageReferenceElement(
 					startPageflowNode);
 			element.add(source);
 			startPageflowNode
 					.setReferenceLink(element.resolveReferenceString());
 			startPageflowNode.getFCElements().add(source);
 		}
 		return startPageflowNode;
 	}
 
 	/**
 	 * Search for an existing pageflow page matching to navigation case's
 	 * target, if there is no such page existing, create one.
 	 * 
 	 * @param navCase
 	 * @return
 	 */
 	private PageflowPage getOrCreateEndPage(NavigationCaseType navCase) {
 		PageflowPage endPageflowNode = null;
 		ToViewIdType target_ = navCase.getToViewId();
 		if (target_ != null) {
 			String path = target_.getTextContent();
 			if (getValidPFPageInCache(path) == null) {
 				endPageflowNode = TransformUtil.findPage(path, pageflow);
 				if (endPageflowNode == null) {
 					endPageflowNode = createPFPage(path);
 					if (DEBUG)
 						System.out.println("Create new end page");
 					pageflowChanged = true;
 				}
 				mapPaths2PF.put(path, endPageflowNode);
 				PageflowTransform.getInstance().updatePageflowTargetPage(
 						endPageflowNode, navCase);
 			} else {
 				endPageflowNode = (PageflowPage) mapPaths2PF.get(path);
 			}
 			if (target_ != null) {
 				ReferenceElement element = new PageReferenceElement(
 						endPageflowNode);
 				element.add(target_);
 				endPageflowNode.setReferenceLink(element
 						.resolveReferenceString());
 				endPageflowNode.getFCElements().add(target_);
 			}
 		}
 		return endPageflowNode;
 	}
 
 	/**
 	 * Return a valid cached pageflow node.
 	 * 
 	 * @param key
 	 * @return
 	 */
 	private PageflowPage getValidPFPageInCache(Object key) {
 		EObject element = (EObject) mapPaths2PF.get(key);
 		if (!TransformUtil.isValidPageflowElement(element)
 				|| !(element instanceof PageflowPage)
 				|| !((PageflowPage) element).getPath().equals(key)) {
 			mapPaths2PF.remove(key);
 			element = null;
 		}
 		return (PageflowPage) element;
 	}
 
 	/**
 	 * Return a valid cached pageflow link.
 	 * 
 	 * @param caseType
 	 * @return
 	 */
 	private PageflowLink getValidPFLinkInCache(EObject caseType) {
 		PageflowLink link = (PageflowLink) mapCases2Links.get(caseType);
 		if (!TransformUtil.isValidLink(link)) {
 			mapCases2Links.remove(caseType);
 			link = null;
 		}
 		return link;
 
 	}
 
 	/**
 	 * Update pageflow model elements against faces-config navigation case.
 	 */
 	public boolean updatePageflowElements(Pageflow pageflow_,
 			NavigationCaseType caseFC) {
 		this.pageflowChanged = false;
 		this.pageflow = pageflow_;
 		ToViewIdType target_ = caseFC.getToViewId();
 		if (target_ == null) {
 			return false;
 		}
 
 		PageflowPage start = null, end = null;
 		// start page
 		start = getOrCreateStartPage(caseFC);
 		String action = null;
 		// action
 		if (caseFC.getFromAction() != null) {
 			action = caseFC.getFromAction().getTextContent();
 		}
 		// from outcome
 		String fromOutCome = null;
 		if (caseFC.getFromOutcome() != null) {
 			fromOutCome = caseFC.getFromOutcome().getTextContent();
 		}
 		// end page
 		end = getOrCreateEndPage(caseFC);
 		// link
 		// Is there a link in pageflow model?
 		PageflowLink link = null;
 		if ((link = findUnmappedLink(start, end, caseFC)) == null) {
 			if (end != null) {
 				if (DEBUG)
 					System.out.println("Create new link");
 				link = createPFLink(start, end, action, fromOutCome);
 				pageflowChanged = true;
 			}
 		}
 		if (link != null) {
 			link.getFCElements().clear();
 			link.getFCElements().add(caseFC);
 			mapCases2Links.put(caseFC, link);
 		}
 		adapt(start);
 		adapt(end);
 		adapt(caseFC);
 		adapt(link);
 		return pageflowChanged;
 	}
 
 	private boolean cleanPageflowNavigationRule(Pageflow pageflow_) {
 		boolean dirty = false;
 		List links = pageflow_.getLinks();
 		Iterator linksIterator = links.iterator();
 		while (linksIterator.hasNext()) {
 			PageflowLink link = (PageflowLink) linksIterator.next();
 			if (link.getFCElements().isEmpty()) {
 				linksIterator.remove();
 				if (link.getSource() != null) {
 					link.getSource().getOutlinks().remove(link);
 				}
 				if (link.getTarget() != null) {
 					link.getTarget().getInlinks().remove(link);
 				}
 				dirty = true;
 			}
 		}
 		return dirty;
 	}
 
 	/**
 	 * merge the two navigation rules list between pageflow and face-config
 	 * file. merge rule: 1. if the from-view-id, from-action, and from-outcome
 	 * are same, they are the same navigation case, then the navigation case in
 	 * face-config file should be updated by one in the pageflow. 2. otherwise
 	 * the new navigation case should be created.
 	 * 
 	 * FIXME: Not merge, only update page flow from faces-config.
 	 * 
 	 * @param rulesFC -
 	 *            navigation rules in faces-config.
 	 * @param navigationRulesInPageflow -
 	 *            navigation rules in pageflow model.
 	 * 
 	 * @return boolean - whether add a new rule or not.
 	 */
 	private boolean updateNavigationRulesInPageflowFromFacesConfig(List rulesFC) {
 		boolean isNew = false;
 		// if the faces-config file is empty, the links should be removed.
 		if (rulesFC == null || rulesFC.isEmpty()) {
 			for (Iterator iterLink = pageflow.getLinks().iterator(); iterLink
 					.hasNext();) {
 				PageflowLink link = (PageflowLink) iterLink.next();
 				link.getSource().getOutlinks().remove(link);
 				link.getTarget().getInlinks().remove(link);
 			}
 			pageflow.getLinks().clear();
 		} else {
 			// Rule - Cases
 			for (Iterator iterRulesFC = rulesFC.iterator(); iterRulesFC
 					.hasNext();) {
 				NavigationRuleType ruleFC = (NavigationRuleType) iterRulesFC
 						.next();
 
 				List casesFC = ruleFC.getNavigationCase();
 				for (Iterator iterCasesFC = casesFC.iterator(); iterCasesFC
 						.hasNext();) {
 					NavigationCaseType navigationCaseFC = (NavigationCaseType) iterCasesFC
 							.next();
 					isNew |= updatePageflowElements(pageflow, navigationCaseFC);
 				}
 			}
 		}
 		isNew |= cleanPageflowNavigationRule(pageflow);
 		return isNew;
 	}
 
 	/**
 	 * Find a pageflow link that is not linked to a faces-config element. Since
 	 * user might edit faces-config file with other editor, so the path is not
 	 * always valid, here we still try to match link and navigation case. TODO:
 	 * should we always resolve mapping from referenceLink on PageflowElement?
 	 */
 	private PageflowLink findUnmappedLink(PageflowPage start, PageflowPage end,
 			NavigationCaseType caseFC) {
 		List links = start.getOutlinks();
 		for (int i = 0, n = links.size(); i < n; i++) {
 			PageflowLink link = (PageflowLink) links.get(i);
 			if (link.getSource() == start && link.getTarget() == end) {
 				link.update();
 				if (link.getFCElements().isEmpty()
 						|| link.getFCElements().getData().get(0) == caseFC) {
 					return link;
 				}
 			}
 		}
 		return null;
 	}
 
 	public PageflowLink createPFLink(PageflowNode start, PageflowNode target_,
 			NavigationCaseType caseFC) {
 		PageflowLink link = null;
 		String fromOutcome = caseFC.getFromOutcome() != null ? caseFC
 				.getFromOutcome().getTextContent() : null;
 		String action = caseFC.getFromAction() != null ? caseFC.getFromAction()
 				.getTextContent() : null;
 		NavigationRuleType rule = (NavigationRuleType) caseFC.eContainer();
 		link = createPFLink(fromOutcome);
 		link.setFromaction(action);
 		link.setSource(start);
 		link.setTarget(target_);
 		start.getOutlinks().add(link);
 		target_.getInlinks().add(link);
 		// The reference.
 		link.getFCElements().add(caseFC);
 		start.getFCElements().add(rule.getFromViewId());
 		target_.getFCElements().add(caseFC.getToViewId());
 		return link;
 	}
 
 	/**
 	 * create a new PFLink object according to fromOutcome attribute
 	 * 
 	 * @param fromOutcome -
 	 *            PFLink's fromOutcome attribute
 	 * @return - new PFLink object
 	 */
 	public PageflowLink createPFLink(String fromOutcome) {
 		PageflowLink link = null;
 		link = PageflowModelManager.getFactory().createPFLink();
 		if (fromOutcome != null && fromOutcome.length() > 0) {
 			link.setOutcome(fromOutcome.trim());
 		}
 		pageflow.getLinks().add(link);
 		return link;
 	}
 
 	/**
 	 * create a new PFLink object according to fromOutcome attribute
 	 * 
 	 * @param fromOutcome -
 	 *            PFLink's fromOutcome attribute
 	 * @return - new PFLink object
 	 */
 	public PageflowLink createPFLink(PageflowNode start, PageflowNode target_,
 			String action, String fromOutcome) {
 		Assert.isTrue(start != null && target_ != null);
 		PageflowLink link = null;
 		link = createPFLink(fromOutcome);
 		link.setFromaction(action);
 		link.setSource(start);
 		link.setTarget(target_);
 		start.getOutlinks().add(link);
 		target_.getInlinks().add(link);
 		return link;
 	}
 
 	/**
 	 * Create a new PFPage according to from-view-id
 	 * 
 	 * @param fromViewID -
 	 *            page's from-view-id
 	 * @return the page flow page
 	 */
 	public PageflowPage createPFPage(String fromViewID) {
 		PageflowPage page = PageflowFactory.eINSTANCE.createPFPage();
 		if (fromViewID != null && fromViewID.length() > 0) {
 			page.setPath(fromViewID.trim());
 			page.setName(WebrootUtil.getPageNameFromWebPath(fromViewID.trim()));
 		}
 		this.pageflow.getNodes().add(page);
 		return page;
 	}
 
 	/**
 	 * update pageflow model according to the face-config model
 	 * 
 	 * @author sfshi
 	 * @return true if new rules were added
 	 */
 	public boolean updatePageflowModelFromEMF() {
 		if (pageflow != null && facesConfig != null) {
 			List navigationRulesInFacesconfig = facesConfig.getNavigationRule();
 			clearCaches();
 			// if add new one, we can relayout the current pageflow.
 			return updateNavigationRulesInPageflowFromFacesConfig(navigationRulesInFacesconfig);
 		}
 		return false;
 	}
 
 	void refreshLink(NavigationCaseType caseType) {
 		PageflowLink link = getValidPFLinkInCache(caseType);
 		if (link != null) {
 			removeLink(link);
 		}
 		// update pageflow
 		if (TransformUtil.isValidFacesConfigElement(caseType)) {
 			updatePageflowElements(pageflow, caseType);
 		}
 	}
 
 	public void notifyChanged(Notification msg) {
 		int type = msg.getEventType();
 		switch (type) {
 		case MY_NOTIFICATION_TYPE:
 			setNeedBatchOperations(true);
 			break;
 		case MY_NOTIFICATION_TYPE1:
 			setNeedBatchOperations(false);
 		}
 	}
 
 	public boolean isListenToNotify() {
 		return listenToNotify && !isInEvent;
 	}
 
 	public boolean isInEvent() {
 		return isInEvent;
 	}
 
 	public void setInEvent(boolean isInEvent) {
 		this.isInEvent = isInEvent;
 	}
 
 	public FacesConfigType getFacesConfig() {
 		return facesConfig;
 	}
 
 	public Pageflow getPageflow() {
 		return pageflow;
 	}
 
 	public static FromViewIdType createRLFromViewID(String value) {
 		FromViewIdType fromView = FacesConfigFactory.eINSTANCE
 				.createFromViewIdType();
 		fromView.setTextContent(value);
 		return fromView;
 	}
 
 	public static ToViewIdType createFCToViewID(String value) {
 		ToViewIdType toView = FacesConfigFactory.eINSTANCE.createToViewIdType();
 		toView.setTextContent(value);
 		return toView;
 	}
 }
