 /*
  * Copyright (c) 2011: Edmund Wagner, Wolfram Weidel
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * * Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  * * Neither the name of the jeconfig nor the
  * names of its contributors may be used to endorse or promote products
  * derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.jeconfig.client.internal.mapping.deserialization;
 
 import java.beans.PropertyDescriptor;
 import java.lang.annotation.Annotation;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.jeconfig.api.annotation.ConfigComplexType;
 import org.jeconfig.api.annotation.ConfigSetProperty;
 import org.jeconfig.api.conversion.ISimpleTypeConverter;
 import org.jeconfig.api.dto.ComplexConfigDTO;
 import org.jeconfig.api.dto.ConfigSetDTO;
 import org.jeconfig.api.dto.ConfigSimpleValueDTO;
 import org.jeconfig.api.dto.IConfigDTO;
 import org.jeconfig.api.scope.IScopePath;
 import org.jeconfig.client.internal.AnnotationUtil;
 import org.jeconfig.client.proxy.ConfigSetDecorator;
 import org.jeconfig.client.proxy.ProxyUpdater;
 import org.jeconfig.common.reflection.PropertyAccessor;
 
 public class SetDTODeserializer extends AbstractDTODeserializer {
 	private final PropertyAccessor propertyAccessor = new PropertyAccessor();
 	private final SimpleDTODeserializer simpleDTODeserializer;
 	private final ComplexDTODeserializer complexDTODeserializer;
 	private final ProxyUpdater proxyUpdater;
 
 	public SetDTODeserializer(
 		final SimpleDTODeserializer simpleDTODeserializer,
 		final ComplexDTODeserializer complexDTODeserializer,
 		final ProxyUpdater proxyUpdater) {
 		this.simpleDTODeserializer = simpleDTODeserializer;
 		this.complexDTODeserializer = complexDTODeserializer;
 		this.proxyUpdater = proxyUpdater;
 	}
 
 	public void handleSetProperty(
 		final ComplexConfigDTO configDTO,
 		final Object config,
 		final List<ComplexConfigDTO> dtos,
 		final IScopePath scopePath,
 		final PropertyDescriptor propDesc,
 		final Annotation annotation,
 		final String propName) {
 		final ConfigSetProperty setAnno = (ConfigSetProperty) annotation;
 		final ISimpleTypeConverter<?> customConverter = createCustomConverter(setAnno.customConverter());
 		final boolean polymorph = setAnno.polymorph();
 		final boolean complex = AnnotationUtil.getAnnotation(setAnno.itemType(), ConfigComplexType.class) != null;
 		final ConfigSetDTO setDTO = configDTO.getSetProperty(propName);
 		final Set<Object> setToSet = processSet(
 				config,
 				propDesc,
 				polymorph,
 				complex,
 				setDTO,
 				getSetPropertyDtos(dtos, propName),
 				scopePath,
 				customConverter);
 		propertyAccessor.write(config, propName, setToSet);
 	}
 
 	private List<ConfigSetDTO> getSetPropertyDtos(final List<ComplexConfigDTO> dtos, final String propName) {
 		final List<ConfigSetDTO> ret = new ArrayList<ConfigSetDTO>();
		for (final ComplexConfigDTO config : dtos) {
			if (config != null) {
				final ConfigSetDTO setProperty = config.getSetProperty(propName);
				if (setProperty != null) {
					ret.add(setProperty);
 				}
 			}
 		}
 		return ret;
 	}
 
 	private Set<Object> processSet(
 		final Object config,
 		final PropertyDescriptor propDesc,
 		final boolean polymorph,
 		final boolean complex,
 		final ConfigSetDTO setDTO,
 		final List<ConfigSetDTO> dtos,
 		final IScopePath scopePath,
 		final ISimpleTypeConverter<?> customConverter) {
 		final ConfigSetDecorator<Object> setToSet = getSetToSet(config, propDesc, setDTO);
 
 		if (setToSet != null) {
 			setToSet.setInitializingWhile(new Runnable() {
 				@Override
 				public void run() {
 					setToSet.setConfigDTOs(dtos);
 					setToSet.clear();
 					if (complex || polymorph) {
 						//complex items
 						final List<Map<String, ComplexConfigDTO>> dtoMap = calculateDTOMap(dtos);
 						for (final IConfigDTO temp : setDTO.getItems()) {
 							final ComplexConfigDTO complexDTO = (ComplexConfigDTO) temp;
 							final Object cfg = complexDTODeserializer.createComplexConfigObject(
 									complexDTO,
 									getComplexSetDtos(complexDTO, dtoMap),
 									scopePath);
 							setToSet.add(cfg);
 						}
 					} else {
 						//simple items
 						for (final IConfigDTO temp : setDTO.getItems()) {
 							final ConfigSimpleValueDTO simpleDTO = (ConfigSimpleValueDTO) temp;
 							final Object value = simpleDTODeserializer.createSimpleValue(
 									simpleDTO,
 									getTypeLoader().getPolymorphType(simpleDTO.getPropertyType()),
 									customConverter);
 							setToSet.add(value);
 						}
 					}
 				}
 			});
 		}
 		return setToSet;
 	}
 
 	private List<ComplexConfigDTO> getComplexSetDtos(
 		final ComplexConfigDTO complexDTO,
 		final List<Map<String, ComplexConfigDTO>> dtoMaps) {
 		final String idPropertyName = complexDTO.getIdPropertyName();
 		final ConfigSimpleValueDTO simpleValueProperty = complexDTO.getSimpleValueProperty(idPropertyName);
 
 		final String id = (simpleValueProperty != null) ? simpleValueProperty.getValue() : null;
 		final List<ComplexConfigDTO> ret = new ArrayList<ComplexConfigDTO>();
 		if (simpleValueProperty != null) {
 			for (final Map<String, ComplexConfigDTO> map : dtoMaps) {
 				final ComplexConfigDTO complexConfigDTO = map.get(id);
 				if (complexConfigDTO != null) {
 					ret.add(complexConfigDTO);
 				}
 			}
 		}
 		return ret;
 	}
 
 	@SuppressWarnings("unchecked")
 	private ConfigSetDecorator<Object> getSetToSet(
 		final Object parent,
 		final PropertyDescriptor propertyDescriptor,
 		final ConfigSetDTO setDTO) {
 
 		ConfigSetDecorator<Object> result;
 
 		if (setDTO.getItems() == null) {
 			result = null;
 		} else {
 			Set<Object> targetSet = (Set<Object>) propertyAccessor.read(parent, propertyDescriptor.getName());
 			if (targetSet instanceof ConfigSetDecorator) {
 				result = (ConfigSetDecorator<Object>) targetSet;
 			} else {
 				if (targetSet == null) {
 					targetSet = new HashSet<Object>();
 				}
 				result = new ConfigSetDecorator<Object>(targetSet, proxyUpdater);
 			}
 		}
 		return result;
 	}
 
 	private List<Map<String, ComplexConfigDTO>> calculateDTOMap(final List<ConfigSetDTO> dtos) {
 		final List<Map<String, ComplexConfigDTO>> ret = new ArrayList<Map<String, ComplexConfigDTO>>();
 		for (final ConfigSetDTO dto : dtos) {
 			final Map<String, ComplexConfigDTO> map = new HashMap<String, ComplexConfigDTO>();
 			if (dto.getItems() != null) {
 				for (final IConfigDTO item : dto.getItems()) {
 					final ComplexConfigDTO complexItem = (ComplexConfigDTO) item;
 					final String idPropertyName = complexItem.getIdPropertyName();
 					final ConfigSimpleValueDTO idSimpleValueProperty = complexItem.getSimpleValueProperty(idPropertyName);
 					if (idSimpleValueProperty != null) {
 						map.put(idSimpleValueProperty.getValue(), complexItem);
 					}
 				}
 			}
 		}
 		return ret;
 	}
 }
