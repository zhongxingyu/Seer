 /*******************************************************************************
  * Copyright (c) 2010 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.eef.runtime.query.ocl;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EEnumLiteral;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EOperation;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EParameter;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.eef.runtime.query.EEFQuery;
 import org.eclipse.emf.eef.runtime.query.QueryProcessor;
 import org.eclipse.emf.eef.runtime.query.QueryResult;
 import org.eclipse.emf.eef.runtime.query.QueryWrapper;
 import org.eclipse.ocl.OCL;
 import org.eclipse.ocl.ParserException;
 import org.eclipse.ocl.ecore.CallOperationAction;
 import org.eclipse.ocl.ecore.Constraint;
 import org.eclipse.ocl.ecore.EcoreEnvironmentFactory;
 import org.eclipse.ocl.ecore.SendSignalAction;
 import org.eclipse.ocl.expressions.OCLExpression;
 import org.eclipse.ocl.helper.OCLHelper;
 
 /**
  * @author <a href="mailto:goulwen.lefur@obeo.fr">Goulwen Le Fur</a>
  */
 public class OCLProcessor implements QueryProcessor {
 
 	public static final String OCL_KIND = "ocl";
 	
 	private OCL<EPackage,EClassifier,EOperation,EStructuralFeature,EEnumLiteral,EParameter,EObject,CallOperationAction,SendSignalAction,org.eclipse.ocl.ecore.Constraint,EClass,EObject> oclEnvironment;
 
 	/**
 	 * Constructor.
 	 */
 	public OCLProcessor() {
 		super();
 		oclEnvironment = OCL.newInstance(EcoreEnvironmentFactory.INSTANCE);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.emf.eef.runtime.query.QueryProcessor#canEvaluate(org.eclipse.emf.eef.runtime.query.QueryWrapper)
 	 */
 	public boolean canEvaluate(QueryWrapper query) {
 		return query.getKind() == OCLProcessor.OCL_KIND;
 	}
 
 
 
 	/**
 	 * {@inheritDoc}
 	 * @see org.eclipse.emf.eef.runtime.query.QueryProcessor#evaluate(org.eclipse.emf.ecore.EObject, org.eclipse.emf.eef.runtime.query.QueryWrapper)
 	 */
 	public QueryResult evaluate(EObject context, QueryWrapper query) {
 		try {
 			OCLQuery oclQuery = (OCLQuery)query;
 			OCLHelper<EClassifier, EOperation, EStructuralFeature, Constraint> oclHelper = oclEnvironment.createOCLHelper();
 			oclHelper.setContext(oclQuery.getContext());
			OCLExpression<EClassifier> parsedException = oclHelper.createQuery(oclQuery.getOclQuery());
			Object evaluationResult = oclEnvironment.evaluate(context, parsedException);
			return new QueryResult(Status.OK_STATUS, evaluationResult);
 		} catch (ParserException e) {
 			return new QueryResult(new Status(IStatus.ERROR, EEFQuery.PLUGIN_ID, "Error parsing ocl query.", e), null);
 		}
 	}
 
 }
