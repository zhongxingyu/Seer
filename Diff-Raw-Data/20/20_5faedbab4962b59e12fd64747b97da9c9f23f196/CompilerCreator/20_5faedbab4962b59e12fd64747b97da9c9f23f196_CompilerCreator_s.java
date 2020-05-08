 /*******************************************************************************
  * Copyright (c) 2010, 2011 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.docs.intent.client.compiler.launcher;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.mylyn.docs.intent.client.compiler.repositoryconnection.CompilerRepositoryClient;
 import org.eclipse.mylyn.docs.intent.collab.common.location.IntentLocations;
 import org.eclipse.mylyn.docs.intent.collab.handlers.RepositoryObjectHandler;
 import org.eclipse.mylyn.docs.intent.collab.handlers.adapters.RepositoryAdapter;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.ReadWriteRepositoryObjectHandlerImpl;
 import org.eclipse.mylyn.docs.intent.collab.handlers.impl.notification.typeListener.TypeNotificator;
 import org.eclipse.mylyn.docs.intent.collab.handlers.notification.Notificator;
 import org.eclipse.mylyn.docs.intent.collab.repository.Repository;
 import org.eclipse.mylyn.docs.intent.collab.repository.RepositoryConnectionException;
 import org.eclipse.mylyn.docs.intent.core.document.IntentDocumentPackage;
 import org.eclipse.mylyn.docs.intent.core.genericunit.GenericUnitPackage;
 import org.eclipse.mylyn.docs.intent.core.modelingunit.ModelingUnitPackage;
 
 /**
  * Creates Compiler repository clients.
  * 
  * @author <a href="mailto:alex.lagarde@obeo.fr">Alex Lagarde</a>
  */
 public final class CompilerCreator {
 
 	/**
 	 * CompilerCreator constructor.
 	 */
 	private CompilerCreator() {
 
 	}
 
 	/**
 	 * Creates a CompilerRepositoryClient.
 	 * 
 	 * @param repository
 	 *            is the repository containing the modeling units to compile
 	 * @throws RepositoryConnectionException
 	 *             if a connection to the given repository cannot be established
 	 * @return the created CompilerRepositoryClient
 	 */
 	public static CompilerRepositoryClient createCompilerClient(Repository repository)
 			throws RepositoryConnectionException {
 
 		// Step 1 : initialize the listened types
 		Set<EStructuralFeature> listenedTypes = new LinkedHashSet<EStructuralFeature>();
 
 		for (EObject obj : ModelingUnitPackage.eINSTANCE.eContents()) {
 			if (obj instanceof EClass) {
 				listenedTypes.addAll(TypeNotificator.getStructuralFeaturesForEClass((EClass)obj));
 			}
 		}
 		listenedTypes.remove(IntentDocumentPackage.eINSTANCE.getIntentGenericElement_CompilationStatus());
 		listenedTypes.remove(GenericUnitPackage.eINSTANCE.getGenericUnit_Instructions());
 		listenedTypes.remove(GenericUnitPackage.eINSTANCE.getGenericUnit_UnitName());
 
 		// Step 2 : defining all the resource that once been saved should not cause a session warning
 		Collection<String> resourcesToIgnorePaths = new ArrayList<String>();
 		resourcesToIgnorePaths.add(IntentLocations.INTENT_FOLDER);
 
 		// Step 3 : create the adapter and the handler for these types
 		final RepositoryAdapter repositoryAdapter = repository.createRepositoryAdapter();
 
 		repositoryAdapter.setSendSessionWarningBeforeSaving(resourcesToIgnorePaths);
 		RepositoryObjectHandler handler = new ReadWriteRepositoryObjectHandlerImpl(repositoryAdapter);
 
 		Notificator notificator = new TypeNotificator(listenedTypes);
 		handler.addNotificator(notificator);
 
 		// Step 4 : create the compiler
 		CompilerRepositoryClient compilerClient = new CompilerRepositoryClient();
 		compilerClient.setRepository(repository);
 		compilerClient.addRepositoryObjectHandler(handler);
 
 		return compilerClient;
 
 	}
 }
