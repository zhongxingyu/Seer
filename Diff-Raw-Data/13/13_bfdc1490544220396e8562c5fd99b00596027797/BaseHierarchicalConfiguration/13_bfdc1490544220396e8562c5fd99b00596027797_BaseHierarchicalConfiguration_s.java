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
 package de.pellepelster.myadmin.client.base.modules.hierarchical;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Nullable;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 
 import de.pellepelster.myadmin.client.base.modules.dictionary.IDictionaryDescriptor;
 
 public abstract class BaseHierarchicalConfiguration
 {
 
 	private Map<String, List<String>> hierarchy = new HashMap<String, List<String>>();
 
 	private final String id;
 
 	public BaseHierarchicalConfiguration(String id)
 	{
 		this.id = id;
 	}
 
 	protected void addHierarchy(IDictionaryDescriptor dictionaryDescriptor, IDictionaryDescriptor... parentDictionaryDescriptors)
 	{
		if (parentDictionaryDescriptors.length == 0)
 		{
 			List<String> parentDictionaryNames = Lists.transform(Arrays.asList(parentDictionaryDescriptors), new Function<IDictionaryDescriptor, String>()
 			{
 				@Override
 				@Nullable
 				public String apply(@Nullable IDictionaryDescriptor input)
 				{
 					return input.getId();
 				}
 			});
 
 			this.hierarchy.put(dictionaryDescriptor.getId(), parentDictionaryNames);
 		}
 		else
 		{
			this.hierarchy.put(dictionaryDescriptor.getId(), Arrays.asList(new String[] { null }));
 		}
 	}
 
 	public HierarchicalConfigurationVO getHierarchyConfigurationVO()
 	{
 		return new HierarchicalConfigurationVO(getId(), this.hierarchy);
 	}
 
 	public String getId()
 	{
 		return this.id;
 	}
 
 }
