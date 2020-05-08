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
 package de.pellepelster.myadmin.client.base.modules.dictionary.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.pellepelster.myadmin.client.base.jpql.IAssociation;
 import de.pellepelster.myadmin.client.base.modules.dictionary.model.containers.IAssignmentTableModel;
 import de.pellepelster.myadmin.client.base.modules.dictionary.model.containers.IBaseContainerModel;
 import de.pellepelster.myadmin.client.base.modules.dictionary.model.containers.ICompositeModel;
 import de.pellepelster.myadmin.client.base.modules.dictionary.model.controls.IBaseControlModel;
 
 public final class DictionaryModelUtil
 {
 
 	public static void createReferenceContainerList(IBaseContainerModel parentContainerModel, List<String> dictionaryNames)
 	{
 
 		String dictionaryName = null;
 
 		if (parentContainerModel instanceof IAssignmentTableModel)
 		{
 			IAssignmentTableModel assignmentTableModel = (IAssignmentTableModel) parentContainerModel;
 			dictionaryName = assignmentTableModel.getDictionaryName();
 		}
 
 		if (dictionaryName != null && !dictionaryNames.contains(dictionaryName))
 		{
 			dictionaryNames.add(dictionaryName);
 		}
 
 		for (IBaseContainerModel baseContainerModel : parentContainerModel.getChildren())
 		{
 			createReferenceContainerList(baseContainerModel, dictionaryNames);
 		}
 	}
 
 	public static void createReferenceControlsList(IBaseContainerModel parentContainerModel, List<String> dictionaryNames)
 	{
 
 		createReferenceControlsList(parentContainerModel.getControls(), dictionaryNames);
 
 		for (IBaseContainerModel baseContainerModel : parentContainerModel.getChildren())
 		{
 			createReferenceControlsList(baseContainerModel, dictionaryNames);
 		}
 	}
 
 	public static void createReferenceControlsList(List<IBaseControlModel> baseControlModels, List<String> dictionaryNames)
 	{
 		String dictionaryName = null;
 
 		for (IBaseControlModel baseControlModel : baseControlModels)
 		{
 			if (baseControlModel instanceof IBaseLookupControlModel)
 			{
 				IBaseLookupControlModel baseLookupControlModel = (IBaseLookupControlModel) baseControlModel;
 				dictionaryName = baseLookupControlModel.getDictionaryName();
 
 				if (dictionaryName != null && !dictionaryNames.contains(dictionaryName))
 				{
 					dictionaryNames.add(dictionaryName);
 				}
 
 			}
 		}
 
 	}
 
 	public static List<String> getReferencedDictionaryModels(ICompositeModel parentCompositeModel)
 	{
 		List<String> result = new ArrayList<String>();
 
 		createReferenceContainerList(parentCompositeModel, result);
 		createReferenceControlsList(parentCompositeModel, result);
 
 		return result;
 	}
 
 	public static List<String> getReferencedDictionaryModels(IDictionaryModel dictionaryModel)
 	{
 		List<String> result = new ArrayList<String>();
 
 		createReferenceControlsList(dictionaryModel.getLabelControls(), result);
 		createReferenceControlsList(dictionaryModel.getSearchModel().getResultModel().getControls(), result);
 
 		return result;
 	}
 
 	public static IBaseModel getRootModel(IBaseModel baseModel)
 	{
 		IBaseModel result = null;
 
 		while (baseModel.getParent() != null)
 		{
 			result = baseModel;
 		}
 
 		return result;
 	}
 
 	public static void populateAssociations(IAssociation association, IBaseContainerModel baseContainerModel)
 	{
 		for (IBaseControlModel baseControlModel : baseContainerModel.getControls())
 		{
 
 			if (baseControlModel instanceof IBaseLookupControlModel)
 			{
 				association.addAssociation(baseControlModel.getAttributePath());
 			}
 
 		}
 
 		for (IBaseContainerModel lBaseContainerModel : baseContainerModel.getChildren())
 		{
 			if (lBaseContainerModel instanceof IDatabindingAwareModel)
 			{
 				IDatabindingAwareModel databindingAwareModel = (IDatabindingAwareModel) lBaseContainerModel;
 				IAssociation lAssociation = association.addAssociation(databindingAwareModel.getAttributePath());
 
 				populateAssociations(lAssociation, lBaseContainerModel);
 			}
 			else
 			{
 				populateAssociations(association, lBaseContainerModel);
 			}
 		}
 
 	}
 
 	public static String getDebugId(IBaseControlModel baseControlModel)
 	{
 		String debugId = "";
 		String delimiter = "";
 
 		IBaseModel baseModel = baseControlModel;
 
 		do
 		{
			if (!"RootComposite".equals(baseModel.getName()))
 			{
 				debugId = baseModel.getName() + delimiter + debugId;
 				delimiter = "-";
 			}
 
 			baseModel = baseModel.getParent();
 		}
 		while (baseModel != null);
 
 		return debugId;
 	}
 
 	private DictionaryModelUtil()
 	{
 	}
 
 }
