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
 package de.pellepelster.myadmin.server.services;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import de.pellepelster.myadmin.client.base.db.vos.IBaseVO;
 import de.pellepelster.myadmin.client.base.db.vos.IHierarchicalVO;
 import de.pellepelster.myadmin.client.base.jpql.GenericFilterVO;
 import de.pellepelster.myadmin.client.base.modules.dictionary.model.IDictionaryModel;
 import de.pellepelster.myadmin.client.base.modules.hierarchical.BaseHierarchicalConfiguration;
 import de.pellepelster.myadmin.client.base.modules.hierarchical.HierarchicalConfigurationVO;
 import de.pellepelster.myadmin.client.base.modules.hierarchical.VOHierarchy;
 import de.pellepelster.myadmin.client.core.query.ClientGenericFilterBuilder;
 import de.pellepelster.myadmin.client.web.entities.dictionary.DictionaryHierarchicalNodeVO;
 import de.pellepelster.myadmin.client.web.modules.dictionary.base.DictionaryUtil;
 import de.pellepelster.myadmin.client.web.services.IBaseEntityService;
 import de.pellepelster.myadmin.client.web.services.IDictionaryService;
 import de.pellepelster.myadmin.client.web.services.IHierachicalServiceGWT;
 import de.pellepelster.myadmin.db.IBaseVODAO;
 import de.pellepelster.myadmin.db.util.EntityVOMapper;
 
 public class HierachicalServiceImpl implements IHierachicalServiceGWT, InitializingBean
 {
 	@Autowired
 	private IBaseEntityService baseEntityService;
 
 	@Autowired
 	private IBaseVODAO baseVODAO;
 
 	@Autowired
 	private IDictionaryService dictionaryService;
 
 	@Autowired
 	private EntityVOMapper entityVOMapper;
 
 	private List<Class<? extends IHierarchicalVO>> hierarchicalClasses;
 
 	private List<HierarchicalConfigurationVO> hierarchicalConfigurations = new ArrayList<HierarchicalConfigurationVO>();
 
 	private Map<String, List<VOHierarchy>> voHierarchies = null;
 
 	/** {@inheritDoc} */
 	@Override
 	public void afterPropertiesSet() throws Exception
 	{
 		this.hierarchicalClasses = new ArrayList<Class<? extends IHierarchicalVO>>();
 
 		for (Class<? extends IBaseVO> voClass : this.entityVOMapper.getVOClasses())
 		{
 			if (IHierarchicalVO.class.isAssignableFrom(voClass))
 			{
 				this.hierarchicalClasses.add((Class<? extends IHierarchicalVO>) voClass);
 			}
 		}
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public List<DictionaryHierarchicalNodeVO> getChildNodes(String id, Long voId, String voClassName)
 	{
 		if (voId == null && voClassName == null)
 		{
 			return getRootNodes(id);
 		}
 		else
 		{
 			List<DictionaryHierarchicalNodeVO> result = new ArrayList<DictionaryHierarchicalNodeVO>();
 
 			for (VOHierarchy voHierarchy : getVOHierarchyByParents(id, voClassName))
 			{
 				result.addAll(getChildNodes(voHierarchy, voId, voClassName));
 
 			}
 
 			return result;
 		}
 	}
 
 	private List<DictionaryHierarchicalNodeVO> getChildNodes(VOHierarchy voHierarchy, Long parentId, String parentClassName)
 	{
 
 		List<DictionaryHierarchicalNodeVO> result = new ArrayList<DictionaryHierarchicalNodeVO>();
 
 		@SuppressWarnings({ "rawtypes", "unchecked" })
 		GenericFilterVO<IHierarchicalVO> genericFilter = new GenericFilterVO(voHierarchy.getClazz());
 		genericFilter.addCriteria(IHierarchicalVO.FIELD_PARENT_CLASSNAME, parentClassName);
 		genericFilter.addCriteria(IHierarchicalVO.FIELD_PARENT_ID, parentId);
 
 		for (IHierarchicalVO vo : this.baseEntityService.filter(genericFilter))
 		{
 			DictionaryHierarchicalNodeVO hierarchicalNode = new DictionaryHierarchicalNodeVO();
 
 			String defaultLabel = "<none>";
 			if (vo != null)
 			{
 				defaultLabel = vo.toString();
 			}
 
 			hierarchicalNode.setLabel(DictionaryUtil.getLabel(voHierarchy.getDictionaryModel().getLabelControls(), vo, defaultLabel));
 			hierarchicalNode.setDictionaryName(voHierarchy.getDictionaryModel().getName());
 			hierarchicalNode.setVoId(vo.getId());
 			hierarchicalNode.setVoClassName(vo.getClass().getName());
 			hierarchicalNode.setHasChildren(hasChildren(vo.getClass().getName(), vo.getId()));
 
 			result.add(hierarchicalNode);
 		}
 
 		return result;
 	}
 
 	@Override
 	public HierarchicalConfigurationVO getConfigurationById(String id)
 	{
 		for (HierarchicalConfigurationVO hierarchicalConfiguration : this.hierarchicalConfigurations)
 		{
 			if (hierarchicalConfiguration.getId().equals(id))
 			{
 				return hierarchicalConfiguration;
 			}
 		}
 
 		throw new RuntimeException(String.format("unknown hierarchical configuration '%s'", id));
 	}
 
 	@SuppressWarnings("unchecked")
 	private Class<? extends IHierarchicalVO> getHierarchicalClass(String className)
 	{
 		Class<?> clazz = null;
 
 		try
 		{
 			clazz = Class.forName(className);
 		}
 		catch (Exception e)
 		{
 			throw new RuntimeException(e);
 		}
 
 		if (IHierarchicalVO.class.isAssignableFrom(clazz))
 		{
 			return (Class<? extends IHierarchicalVO>) clazz;
 		}
 		else
 		{
 			throw new RuntimeException(String.format("'%s' ist not a IHierarchicalVO", className));
 		}
 	}
 
 	private List<VOHierarchy> getRootHierarchies(String id)
 	{
 		List<VOHierarchy> result = new ArrayList<VOHierarchy>();
 
 		for (VOHierarchy voHierarchy : this.getVOHierarchy(id))
 		{
 			if (voHierarchy.getParents().isEmpty())
 			{
 				result.add(voHierarchy);
 			}
 		}
 
 		return result;
 
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public List<DictionaryHierarchicalNodeVO> getRootNodes(String id)
 	{
 		List<DictionaryHierarchicalNodeVO> result = new ArrayList<DictionaryHierarchicalNodeVO>();
 		for (VOHierarchy voHierarchy : getRootHierarchies(id))
 		{
 			result.addAll(getChildNodes(voHierarchy, null, null));
 		}
 
 		return result;
 	}
 
 	private List<VOHierarchy> getVOHierarchyByParents(String id, String parentClassName)
 	{
 		List<VOHierarchy> result = new ArrayList<VOHierarchy>();
 
 		for (VOHierarchy voHierarchy : this.getVOHierarchy(id))
 		{
 			Class<?> parentClass = getHierarchicalClass(parentClassName);
 
 			if (voHierarchy.getParents().contains(parentClass))
 			{
 				result.add(voHierarchy);
 			}
 		}
 
 		return result;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public Boolean hasChildren(String voClassName, Long voId)
 	{
 		long count = 0;
 
 		for (Class<? extends IHierarchicalVO> hierarchicalClass : this.hierarchicalClasses)
 		{
 			GenericFilterVO<? extends IHierarchicalVO> childrenFilter = ClientGenericFilterBuilder.createGenericFilter(hierarchicalClass)
 					.addCriteria(IHierarchicalVO.FIELD_PARENT_CLASSNAME, voClassName).addCriteria(IHierarchicalVO.FIELD_PARENT_ID, voId).getGenericFilter();
 
 			count += this.baseVODAO.getCount(childrenFilter);
 		}
 
 		return count > 0;
 	}
 
 	private List<VOHierarchy> getVOHierarchy(String hierarchicalConfigurationId)
 	{
 		if (getAndInitVOHierarchies().containsKey(hierarchicalConfigurationId))
 		{
 			return getAndInitVOHierarchies().get(hierarchicalConfigurationId);
 		}
 		else
 		{
 			return Collections.EMPTY_LIST;
 		}
 	}
 
 	private Map<String, List<VOHierarchy>> getAndInitVOHierarchies()
 	{
 		if (this.voHierarchies == null)
 		{
 			this.voHierarchies = new HashMap<String, List<VOHierarchy>>();
 
 			for (HierarchicalConfigurationVO hierarchicalConfiguration : this.hierarchicalConfigurations)
 			{
 
 				for (Map.Entry<String, List<String>> dictionaryHierarchy : hierarchicalConfiguration.getHierarchy().entrySet())
 				{
 					IDictionaryModel dictionaryModel = this.dictionaryService.getDictionary(dictionaryHierarchy.getKey());
 
 					Class<? extends IHierarchicalVO> voClass = getHierarchicalClass(dictionaryModel.getVOName());
 					List<Class<? extends IHierarchicalVO>> parentClasses = new ArrayList<Class<? extends IHierarchicalVO>>();
 
 					for (String parentDictionaryName : dictionaryHierarchy.getValue())
 					{
 						if (parentDictionaryName == null)
 						{
 							parentClasses.add(null);
 						}
 						else
 						{
 							String parentClassName = this.dictionaryService.getDictionary(parentDictionaryName).getVOName();
 							parentClasses.add(getHierarchicalClass(parentClassName));
 						}
 					}
 
 					if (!this.voHierarchies.containsKey(hierarchicalConfiguration.getId()))
 					{
 						this.voHierarchies.put(hierarchicalConfiguration.getId(), new ArrayList<VOHierarchy>());
 					}
 
 					this.voHierarchies.get(hierarchicalConfiguration.getId()).add(new VOHierarchy(dictionaryModel, voClass, parentClasses));
 				}
 
 			}
 		}
 
 		return this.voHierarchies;
 	}
 
 	public void setBaseVODAO(IBaseVODAO baseVODAO)
 	{
 		this.baseVODAO = baseVODAO;
 	}
 
 	public void setDictionaryService(IDictionaryService dictionaryService)
 	{
 		this.dictionaryService = dictionaryService;
 	}
 
 	public void setEntityVOMapper(EntityVOMapper entityVOMapper)
 	{
 		this.entityVOMapper = entityVOMapper;
 	}
 
 	@Autowired(required = false)
 	public void setHierarchicalConfigurations(List<BaseHierarchicalConfiguration> hierarchicalConfigurations)
 	{
 		for (BaseHierarchicalConfiguration hierarchicalConfiguration : hierarchicalConfigurations)
 		{
 			this.hierarchicalConfigurations.add(hierarchicalConfiguration.getHierarchyConfigurationVO());
 		}
 	}
 
 }
