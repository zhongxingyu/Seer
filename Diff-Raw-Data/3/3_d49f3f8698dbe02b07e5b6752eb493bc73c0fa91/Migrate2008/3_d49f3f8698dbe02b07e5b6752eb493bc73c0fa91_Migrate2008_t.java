 /*
  * Copyright (c) 2008 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.internal.codegen.util;
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenPackage;
 import org.eclipse.gmf.internal.common.migrate.FilteringCopier;
 
 /**
  * Migrate GMFGen model from year 2006-2007 (as of 2.0 release) to version of 
  * year 2008 (release 2.1)
  * @author artem
  */
 public /*package-local, but need to be visible from tests*/ class Migrate2008 {
 	private boolean myIsMigrationApplied = false;
 
 	private final EPackage myMetaPackage;
 	
 	public Migrate2008() {
 		myMetaPackage = GMFGenPackage.eINSTANCE;
 	}
 
 	@SuppressWarnings("unchecked")
 	public EObject go(EObject o) {
 		
 		EPackage oldGenModel = o.eClass().getEPackage();
 		final EStructuralFeature modelElementSelector = ((EClass) oldGenModel.getEClassifier("TypeModelFacet")).getEStructuralFeature("modelElementSelector");
 		final EStructuralFeature valueExprLanguage = ((EClass) oldGenModel.getEClassifier("ValueExpression")).getEStructuralFeature("language");
 		final EStructuralFeature gfvsBody = ((EClass) oldGenModel.getEClassifier("GenFeatureValueSpec")).getEStructuralFeature("body");
 		final EStructuralFeature lcSourceEnd = ((EClass) oldGenModel.getEClassifier("GenLinkConstraints")).getEStructuralFeature("sourceEnd");
 		final EStructuralFeature lcTargetEnd = ((EClass) oldGenModel.getEClassifier("GenLinkConstraints")).getEStructuralFeature("targetEnd");
 		final EStructuralFeature auditRule = ((EClass) oldGenModel.getEClassifier("GenAuditRule")).getEStructuralFeature("rule");
 		final EStructuralFeature metricRule = ((EClass) oldGenModel.getEClassifier("GenMetricRule")).getEStructuralFeature("rule");
 		final EStructuralFeature providers = ((EClass) oldGenModel.getEClassifier("GenExpressionProviderContainer")).getEStructuralFeature("providers");
 		final EStructuralFeature ctxSelectorClassName = ((EClass) oldGenModel.getEClassifier("GenAuditRule")).getEStructuralFeature("contextSelectorLocalClassName");
 		//
 		FilteringCopier cc = new FilteringCopier(myMetaPackage);
 		cc.ignore(modelElementSelector);
 		cc.ignore(valueExprLanguage);
 		cc.ignore(gfvsBody);
 		cc.ignore(lcSourceEnd);
 		cc.ignore(lcTargetEnd);
 		cc.ignore(auditRule);
 		cc.ignore(metricRule);
 		cc.ignore(providers);
 		cc.ignore(ctxSelectorClassName);
 		EObject result = cc.go(o);
 
 		//
 		HashMap<EObject, EObject> oldValueExpr2New = new HashMap<EObject, EObject>();
 		assert cc.getIgnoredOwners(providers).size() < 2;
 		EObject providerContainer = cc.getIgnoredOwners(providers).isEmpty() ? null : cc.getIgnoredOwners(providers).get(0);
 		if (providerContainer != null) {
 			EObject newProviderContainer = cc.get(providerContainer);
 			List<EObject> allNewProviders = (List<EObject>) newProviderContainer.eGet(newProviderContainer.eClass().getEStructuralFeature(providers.getName()));
 			for (EObject oldProvider : (List<EObject>) providerContainer.eGet(providers)) {
 				EClass newProviderClass = (EClass) myMetaPackage.getEClassifier(oldProvider.eClass().getName());
 				EObject newProvider = myMetaPackage.getEFactoryInstance().create(newProviderClass);
 				if ("GenExpressionInterpreter".equals(oldProvider.eClass().getName())) {
 					EStructuralFeature oldLang = oldProvider.eClass().getEStructuralFeature("language");
 					EStructuralFeature oldClassName = oldProvider.eClass().getEStructuralFeature("className");
 					newProvider.eSet(newProviderClass.getEStructuralFeature(oldLang.getName()), cc.transformValue((EAttribute) oldLang, oldProvider.eGet(oldLang)));
 					newProvider.eSet(newProviderClass.getEStructuralFeature(oldClassName.getName()), oldProvider.eGet(oldClassName));
 				}
 				allNewProviders.add(newProvider);
 				EStructuralFeature provBaseExpressions = ((EClass) oldGenModel.getEClassifier("GenExpressionProviderBase")).getEStructuralFeature("expressions");
 				List<EObject> newProviderExpressions = (List<EObject>) newProvider.eGet(newProvider.eClass().getEStructuralFeature(provBaseExpressions.getName())); 
 				for (EObject oldVE : (List<EObject>) oldProvider.eGet(provBaseExpressions)) {
 					EObject newVE;
 					if (oldVE.eClass().getName().equals("GenConstraint")) {
 						newVE = myMetaPackage.getEFactoryInstance().create((EClass) myMetaPackage.getEClassifier("GenConstraint"));
 					} else {
 						// intentionally transform GenFeatureValueSpec into plain ValueExpression
 						newVE = myMetaPackage.getEFactoryInstance().create((EClass) myMetaPackage.getEClassifier("ValueExpression"));
 					}
 					Object bodyValue = oldVE.eGet(oldVE.eClass().getEStructuralFeature("body"));
 					newVE.eSet(newVE.eClass().getEStructuralFeature("body"), bodyValue);
 					oldValueExpr2New.put(oldVE, newVE);
 					newProviderExpressions.add(newVE);
 				}
 			}
 			containment2AssociationCase(cc, modelElementSelector, oldValueExpr2New, allNewProviders);
 			containment2AssociationCase(cc, lcSourceEnd, oldValueExpr2New, allNewProviders);
 			containment2AssociationCase(cc, lcTargetEnd, oldValueExpr2New, allNewProviders);
 			containment2AssociationCase(cc, auditRule, oldValueExpr2New, allNewProviders);
 			containment2AssociationCase(cc, metricRule, oldValueExpr2New, allNewProviders);
 			for (EObject vs : cc.getIgnoredOwners(gfvsBody)) {
 				assert vs.eClass().getName().equals("GenFeatureValueSpec");
 				myIsMigrationApplied = true;
 				EObject newVS = cc.get(vs);
 				EObject newVE = oldValueExpr2New.get(vs);
 				if (newVE == null) {
 					// isCopy == true, need to match by lang/body
 					newVE = matchCopyVE(vs, allNewProviders);
 				}
 				if (newVE != null) {
 					newVS.eSet(newVS.eClass().getEStructuralFeature("value"), newVE);
 				}
 			}
 		}
 		for (EObject oldRule : cc.getIgnoredOwners(ctxSelectorClassName)) {
 			assert "GenAuditRule".equals(oldRule.eClass().getName());
 			if (!oldRule.eIsSet(ctxSelectorClassName)) {
 				continue;
 			}
 			EObject root = cc.get(oldRule.eGet(oldRule.eClass().getEStructuralFeature("root")));
 			assert root != null;
 			myIsMigrationApplied = true;
 			String className = (String) oldRule.eGet(ctxSelectorClassName);
 			EObject context = getOrCreateContext(root, className);
 			EObject target = cc.get(oldRule.eGet(oldRule.eClass().getEStructuralFeature("target")));
 			if (target != null) {
 				target.eSet(((EClass) myMetaPackage.getEClassifier("GenAuditable")).getEStructuralFeature("contextSelector"), context);
 			}
 		}
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	private static EObject matchCopyVE(EObject oldVE, List<EObject> allProviders) {
 		final EStructuralFeature langFeature = oldVE.eClass().getEStructuralFeature("language");
 		final EStructuralFeature bodyFeature = oldVE.eClass().getEStructuralFeature("body");
 		if (langFeature == null || bodyFeature == null) {
 			return null;
 		}
 		final Object langValue = oldVE.eClass().getEPackage().getEFactoryInstance().convertToString((EDataType) langFeature.getEType(), oldVE.eGet(langFeature));
 		final Object bodyValue = oldVE.eGet(bodyFeature);
 		if (bodyValue != null) {
 			for (EObject p : allProviders) {
 				EStructuralFeature provLangFeat = p.eClass().getEStructuralFeature("language");
				// java provider has no language feature, hence hardcoded "java" value
				Object providerLang = provLangFeat == null ? "java" : p.eClass().getEPackage().getEFactoryInstance().convertToString((EDataType) provLangFeat.getEType(), p.eGet(provLangFeat));
 				if (providerLang != null && providerLang.equals(langValue)) {
 					for (EObject ve : (List<EObject>) p.eGet(p.eClass().getEStructuralFeature("expressions"))) {
 						EStructuralFeature veBodyFeature = ve.eClass().getEStructuralFeature("body");
 						if (veBodyFeature != null /* just in case */&& bodyValue.equals(ve.eGet(veBodyFeature))) {
 							return ve;
 						}
 					}
 				}
 			}
 		} // body == null, no much sense to match...
 		return null;
 	}
 
 	private void containment2AssociationCase(FilteringCopier cc, EStructuralFeature oldFeature, HashMap<EObject, EObject> old2newVE, List<EObject> allProviders) {
 		for (EObject o : cc.getIgnoredOwners(oldFeature)) {
 			EObject n = cc.get(o);
 			Object oldVE = o.eGet(oldFeature);
 			if (oldVE == null) {
 				continue; //nothing to do.
 			}
 			myIsMigrationApplied = true;
 			EObject newVE = old2newVE.get(oldVE);
 			if (newVE == null) {
 				// isCopy == true, need to match by lang/body
 				if (oldVE instanceof EObject) {
 					newVE = matchCopyVE((EObject) oldVE, allProviders);
 				}
 			}
 			if (newVE != null) {
 				n.eSet(n.eClass().getEStructuralFeature(oldFeature.getName()), newVE);
 			}
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private static EObject getOrCreateContext(EObject root, String className) {
 		assert root != null && "GenAuditRoot".equals(root.eClass().getName());
 		EPackage metaPackage = root.eClass().getEPackage();
 		EClass class_genAuditContext = (EClass) metaPackage.getEClassifier("GenAuditContext");
 		EStructuralFeature feature_genAuditContext_className = class_genAuditContext.getEStructuralFeature("className");
 		EStructuralFeature feature_genAuditContext_id = class_genAuditContext.getEStructuralFeature("id");
 		EStructuralFeature feature_genAuditRoot_clientContexts = root.eClass().getEStructuralFeature("clientContexts");
 		EObject context = null;
 		for (EObject next : (List<EObject>) root.eGet(feature_genAuditRoot_clientContexts)) {
 			String explicit = (String) next.eGet(feature_genAuditContext_className);
 			if (className == explicit || (className != null && className.equals(explicit)) || (explicit == null && className.equals(next.eGet(feature_genAuditContext_id)))) {
 				context = next;
 				break;
 			}
 		}
 		if (context == null) {
 			context = metaPackage.getEFactoryInstance().create(class_genAuditContext);
 			String id = generateUnique(root, className == null ? "" : className);
 			context.eSet(feature_genAuditContext_id, id);
 			if (!id.equals(className)) {
 				context.eSet(feature_genAuditContext_className, className);
 			}
 			((List<EObject>) root.eGet(feature_genAuditRoot_clientContexts)).add(context);
 		}
 		return context;
 	}
 
 	@SuppressWarnings("unchecked")
 	private static String generateUnique(EObject root, String defaultId) {
 		assert root != null && "GenAuditRoot".equals(root.eClass().getName());
 		String id = defaultId;
 		int i = 0;
 		boolean haveSuchId = false;
 		EStructuralFeature feature_genAuditRoot_clientContexts = root.eClass().getEStructuralFeature("clientContexts");
 		do {
 			haveSuchId = false;
 			for (EObject next : (List<EObject>) root.eGet(feature_genAuditRoot_clientContexts)) {
 				EStructuralFeature feature_genAuditContext_id = next.eClass().getEStructuralFeature("id");
 				if (id.equals(next.eGet(feature_genAuditContext_id))) {
 					haveSuchId = true;
 					id = defaultId + (++i);
 					break;
 				}
 			}
 		} while (haveSuchId);
 		return id;
 	}
 
 	public boolean wasMigrationApplied() {
 		return myIsMigrationApplied;
 	}
 }
