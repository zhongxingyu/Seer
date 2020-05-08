 /**
  * Copyright (c) 2013 Christian Pelster.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Christian Pelster - initial API and implementation
  */
 package de.pellepelster.myadmin.dsl.generator;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 
 import com.google.common.collect.Iterators;
 import com.google.common.collect.Lists;
 
 import de.pellepelster.myadmin.dsl.DictionaryControlResolver;
 import de.pellepelster.myadmin.dsl.myAdminDsl.DictionaryControl;
 import de.pellepelster.myadmin.dsl.myAdminDsl.Entity;
 import de.pellepelster.myadmin.dsl.myAdminDsl.Enumeration;
 import de.pellepelster.myadmin.dsl.myAdminDsl.Model;
 import de.pellepelster.myadmin.dsl.myAdminDsl.ModelScope;
 import de.pellepelster.myadmin.dsl.myAdminDsl.PackageDeclaration;
 import de.pellepelster.myadmin.dsl.myAdminDsl.SimpleVO;
 import de.pellepelster.myadmin.dsl.util.EntityModelUtil;
 import de.pellepelster.myadmin.dsl.util.ModelUtil;
 
 public class Extensions
 {
 
 	public static final String SIMPLE_VO_POSTFIX = "SimpleVO";
 
 	public static final String WEB_ROOT_PACKAGE_POSTFIX = "client.web";
 
 	public static final String MOBILE_ROOT_PACKAGE_POSTFIX = "mobile.web";
 
 	public static final String WEB_SERVICE_ROOT_PACKAGE_POSTFIX = "client.web";
 
 	public static final String SERVER_ROOT_PACKAGE_POSTFIX = "server";
 
 	public static final String CLIENT_BASE_ROOT_PACKAGE_POSTFIX = "client.base";
 
 	public static List<EObject> allElements(EObject anElementInRootResource)
 	{
 		ResourceSet resourceSet = anElementInRootResource.eResource().getResourceSet();

 		Iterator<EObject> iter = Iterators.filter(EcoreUtil.getAllContents(resourceSet, true), EObject.class);
 
 		return Lists.newArrayList(iter);
 	}
 
 	/**
 	 * Name of the entity for the current model scope
 	 * 
 	 * @param eObject
 	 *            the entity object enumeration/entity
 	 * 
 	 * @return the name
 	 */
 	public static String entityName(EObject eObject)
 	{
 		return entityName(eObject, getModelScope());
 	}
 
 	/**
 	 * Name of the entity for a model scope
 	 * 
 	 * @param eObject
 	 *            the entity object enumeration/entity
 	 * @param modelScope
 	 *            the model scope
 	 * 
 	 * @return the name
 	 */
 	public static String entityName(EObject eObject, ModelScope modelScope)
 	{
 		String postfix = null;
 
 		switch (modelScope)
 		{
 			case WEB:
 				postfix = "VO";
 				break;
 			case MOBILE:
 				postfix = "MobileVO";
 				break;
 
 			default:
 				postfix = "";
 				break;
 		}
 
 		if (eObject instanceof Entity)
 		{
 			return toFirstUpper(((Entity) eObject).getName()) + postfix;
 		}
 		if (eObject instanceof SimpleVO)
 		{
 			return toFirstUpper(((SimpleVO) eObject).getName()) + SIMPLE_VO_POSTFIX;
 		}
 		else if (eObject instanceof Enumeration)
 		{
 			return toFirstUpper(((Enumeration) eObject).getName());
 		}
 		else
 		{
 			throw new RuntimeException(String.format("unsupported object type '%s'", eObject.getClass()));
 		}
 	}
 
 	public static String fullQualifiedEntityName(EObject eObject)
 	{
 		if (eObject instanceof Enumeration)
 		{
 			return fullQualifiedEntityName(eObject, ModelScope.CLIENT_BASE);
 		}
 		if (eObject instanceof SimpleVO)
 		{
 			return fullQualifiedEntityName(eObject, ModelScope.CLIENT_BASE);
 		}
 		else
 		{
 			return fullQualifiedEntityName(eObject, getModelScope());
 		}
 	}
 
 	public static String fullQualifiedEntityName(EObject eObject, ModelScope modelScope)
 	{
 		return getPackageName(eObject, modelScope) + "." + entityName(eObject, modelScope);
 	}
 
 	private static ModelScope getModelScope()
 	{
 		Object modelScope = org.eclipse.xtend.util.stdlib.GlobalVarExtensions.getGlobalVar("model_scope");
 
 		if (modelScope != null && modelScope instanceof ModelScope)
 		{
 			return (ModelScope) modelScope;
 		}
 		else
 		{
 			throw new RuntimeException(String.format("unsupported model scope '%s'", modelScope));
 		}
 	}
 
 	/**
 	 * Package name for an Enumeration/Entity in a specific model scope
 	 * 
 	 * @param eObject
 	 *            enumeration/entity
 	 * @return the package name for the enumeration/entity
 	 */
 	public static String getPackageName(EObject eObject)
 	{
 		return getPackageName(eObject, getModelScope());
 	}
 
 	/**
 	 * Package name for an Enumeration/Entity in a specific model scope
 	 * 
 	 * @param eObject
 	 *            enumeration/entity
 	 * @param modelScope
 	 *            the model scope
 	 * @return the package name for the enumeration/entity
 	 */
 	public static String getPackageName(EObject eObject, ModelScope modelScope)
 	{
 		assert eObject instanceof Entity || eObject instanceof Enumeration : "eObject must be an Enumeration/Entity";
 
 		switch (modelScope)
 		{
 			case MOBILE:
 				return getPackageName(eObject, MOBILE_ROOT_PACKAGE_POSTFIX);
 			case WEB:
 				return getPackageName(eObject, WEB_ROOT_PACKAGE_POSTFIX);
 			case CLIENT_BASE:
 				return getPackageName(eObject, CLIENT_BASE_ROOT_PACKAGE_POSTFIX);
 			case SERVER:
 				return getPackageName(eObject, SERVER_ROOT_PACKAGE_POSTFIX);
 			default:
 				throw new RuntimeException(String.format("unsupported model scope '%s'", getModelScope()));
 		}
 
 	}
 
 	private static String getPackageName(EObject eObject, String rootPackagePostfix)
 	{
 		List<String> packageElements = new ArrayList<String>();
 
 		while (eObject.eContainer() instanceof PackageDeclaration)
 		{
 
 			PackageDeclaration packageDeclaration = (PackageDeclaration) eObject.eContainer();
 			packageElements.add(packageDeclaration.getName().replaceAll("\\^", ""));
 			eObject = eObject.eContainer();
 		}
 
 		if (!packageElements.isEmpty())
 		{
 			packageElements.add(packageElements.size() - 1, rootPackagePostfix);
 		}
 
 		String packageName = "";
 		String delimiter = "";
 		for (String packageElement : packageElements)
 		{
 			packageName = packageElement + delimiter + packageName;
 			delimiter = ".";
 		}
 
 		return packageName;
 	}
 
 	public static List<Entity> getReferencedEntitiesWithoutDuplicates(Entity entity)
 	{
 		return EntityModelUtil.getLinkedEntitiesWithoutDuplicates(entity);
 	}
 
 	public static Model getRootModel(EObject eObject)
 	{
 		while (!(eObject instanceof Model) && eObject != null)
 		{
 			eObject = eObject.eContainer();
 		}
 
 		return (Model) eObject;
 	}
 
 	public static String resolveControlName(DictionaryControl dictionaryControl)
 	{
 		return DictionaryControlResolver.resolveControlName(dictionaryControl);
 	}
 
 	public static String getRootWebServicePackageName(Model model)
 	{
 		return ModelUtil.getSingleRootPackage(model).getName() + "." + WEB_SERVICE_ROOT_PACKAGE_POSTFIX;
 	}
 
 	public static PackageDeclaration getSingleRootPackage(Model model)
 	{
 		return ModelUtil.getSingleRootPackage(model);
 	}
 
 	public static String getServiceImplementationPackageName(de.pellepelster.myadmin.dsl.myAdminDsl.RemoteService remoteService)
 	{
 		return getPackageName(remoteService, SERVER_ROOT_PACKAGE_POSTFIX);
 	}
 
 	/**
 	 * Capitalizes the first character of a string
 	 * 
 	 * @param s
 	 * @return
 	 */
 	public static String toFirstUpper(String s)
 	{
 		StringBuilder sb = new StringBuilder(s);
 
 		int i = 0;
 		return sb.replace(i, i + 1, sb.substring(i, i + 1).toUpperCase()).toString();
 	}
 
 }
