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
 
 package org.jeconfig.api.dto;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.jeconfig.api.scope.IScopePath;
 import org.jeconfig.api.util.Assert;
 
 /**
  * Holds all information about a complex configuration object.
  */
 public class ComplexConfigDTO extends AbstractConfigDTO {
 	private static final long serialVersionUID = 1L;
 
	private long classVersion = Long.MIN_VALUE;
 
 	private final Map<String, ConfigListDTO> listProperties;
 	private final Map<String, ConfigSimpleValueDTO> simpleProperties;
 	private final Map<String, ConfigSetDTO> setProperties;
 	private final Map<String, ConfigMapDTO> mapProperties;
 	private final Map<String, ComplexConfigDTO> complexProperties;
 
 	private String idPropertyName;
 	private boolean nulled = false;
 
 	/**
 	 * Creates a new complex configuration DTO without properties.
 	 */
 	public ComplexConfigDTO() {
 		listProperties = new HashMap<String, ConfigListDTO>();
 		simpleProperties = new HashMap<String, ConfigSimpleValueDTO>();
 		setProperties = new HashMap<String, ConfigSetDTO>();
 		mapProperties = new HashMap<String, ConfigMapDTO>();
 		complexProperties = new HashMap<String, ComplexConfigDTO>();
 	}
 
 	/**
 	 * Creates a flat copy of the DTO.
 	 * 
 	 * @return a flat copy
 	 */
 	public ComplexConfigDTO flatCopy() {
 		final ComplexConfigDTO config = new ComplexConfigDTO();
 		config.setPropertyType(getPropertyType());
 		config.setPropertyName(getPropertyName());
 		config.setDefiningScopePath(getDefiningScopePath());
 		config.setPolymorph(isPolymorph());
 		config.setVersion(getVersion());
 		config.setParentVersion(getParentVersion());
 		config.setParentScopeName(getParentScopeName());
 		config.setClassVersion(getClassVersion());
 		config.setNulled(isNulled());
 		return config;
 	}
 
 	/**
 	 * Creates a flat copy of the DTO.
 	 * 
 	 * @param version
 	 * @param parentScopeName
 	 * @param parentVersion
 	 * @return a flat copy
 	 */
 	public ComplexConfigDTO flatCopy(final long version, final String parentScopeName, final long parentVersion) {
 		final ComplexConfigDTO config = new ComplexConfigDTO();
 		config.setPropertyType(getPropertyType());
 		config.setPropertyName(getPropertyName());
 		config.setDefiningScopePath(getDefiningScopePath());
 		config.setPolymorph(isPolymorph());
 		config.setVersion(version);
 		config.setParentVersion(parentVersion);
 		config.setParentScopeName(parentScopeName);
 		config.setClassVersion(getClassVersion());
 		config.setNulled(isNulled());
 		return config;
 	}
 
 	/**
 	 * Indicates which properties are declared by this complex DTO.
 	 * 
 	 * @return a set holding the declared properties
 	 */
 	public Set<String> getDeclaredProperties() {
 		final Set<String> ret = new HashSet<String>();
 		ret.addAll(complexProperties.keySet());
 		ret.addAll(simpleProperties.keySet());
 		ret.addAll(listProperties.keySet());
 		ret.addAll(setProperties.keySet());
 		ret.addAll(mapProperties.keySet());
 		return ret;
 	}
 
 	/**
 	 * Indicates whether this DTO represents a <code>null</code> value.<br>
 	 * If this property is <code>true</code>, no child properties must be declared.
 	 * 
 	 * @return <code>true</code> if this DTO represents a <code>null</code> value
 	 */
 	public boolean isNulled() {
 		return nulled;
 	}
 
 	/**
 	 * Indicates that this DTO should represent a <code>null</code> value.<br>
 	 * If this property is <code>true</code>, no child properties must be declared.
 	 * 
 	 * @param nulled
 	 */
 	public void setNulled(final boolean nulled) {
 		this.nulled = nulled;
 	}
 
 	/**
 	 * Sets the class version of this DTO. Only allowed if this DTO is the root of the configuration tree.
 	 * 
 	 * @param classVersion
 	 */
 	public void setClassVersion(final long classVersion) {
 		this.classVersion = classVersion;
 	}
 
 	/**
 	 * Indicates the class version of this DTO. Only set if this DTO is the root of the configuration tree.
 	 * 
 	 * @return the class version
 	 */
 	public long getClassVersion() {
 		return classVersion;
 	}
 
 	/**
 	 * Returns the complex property child DTO with the given name.
 	 * 
 	 * @param name the name of the property to return
 	 * @return the child DTO or <code>null</code> if not declared
 	 */
 	public ComplexConfigDTO getComplexProperty(final String name) {
 		return complexProperties.get(name);
 	}
 
 	/**
 	 * Returns the set property child DTO with the given name.
 	 * 
 	 * @param name the name of the property to return
 	 * @return the child DTO or <code>null</code> if not declared
 	 */
 	public ConfigSetDTO getSetProperty(final String name) {
 		return setProperties.get(name);
 	}
 
 	/**
 	 * Returns the list property child DTO with the given name.
 	 * 
 	 * @param name the name of the property to return
 	 * @return the child DTO or <code>null</code> if not declared
 	 */
 	public ConfigListDTO getListProperty(final String name) {
 		return listProperties.get(name);
 	}
 
 	/**
 	 * Returns the map property child DTO with the given name.
 	 * 
 	 * @param name the name of the property to return
 	 * @return the child DTO or <code>null</code> if not declared
 	 */
 	public ConfigMapDTO getMapProperty(final String name) {
 		return mapProperties.get(name);
 	}
 
 	/**
 	 * Returns the simple property child DTO with the given name.
 	 * 
 	 * @param name the name of the property to return
 	 * @return the child DTO or <code>null</code> if not declared
 	 */
 	public ConfigSimpleValueDTO getSimpleValueProperty(final String name) {
 		return simpleProperties.get(name);
 	}
 
 	/**
 	 * Adds a new complex property child DTO.<br>
 	 * Overwrites the old DTO with the same name if declared.
 	 * 
 	 * @param property
 	 */
 	public void addComplexProperty(final ComplexConfigDTO property) {
 		complexProperties.put(property.getPropertyName(), property);
 	}
 
 	/**
 	 * Adds a new set property child DTO.<br>
 	 * Overwrites the old DTO with the same name if declared.
 	 * 
 	 * @param property
 	 */
 	public void addSetProperty(final ConfigSetDTO property) {
 		setProperties.put(property.getPropertyName(), property);
 	}
 
 	/**
 	 * Adds a new list property child DTO.<br>
 	 * Overwrites the old DTO with the same name if declared.
 	 * 
 	 * @param property
 	 */
 	public void addListProperty(final ConfigListDTO property) {
 		listProperties.put(property.getPropertyName(), property);
 	}
 
 	/**
 	 * Adds a new map property child DTO.<br>
 	 * Overwrites the old DTO with the same name if declared.
 	 * 
 	 * @param property
 	 */
 	public void addMapProperty(final ConfigMapDTO property) {
 		mapProperties.put(property.getPropertyName(), property);
 	}
 
 	/**
 	 * Adds a new simple property child DTO.<br>
 	 * Overwrites the old DTO with the same name if declared.
 	 * 
 	 * @param property
 	 */
 	public void addSimpleValueProperty(final ConfigSimpleValueDTO property) {
 		simpleProperties.put(property.getPropertyName(), property);
 	}
 
 	/**
 	 * Returns the name of the property which is the unique identifier of this DTO.
 	 * 
 	 * @return the name of the ID property or <code>null</code> if not set
 	 */
 	public String getIdPropertyName() {
 		return idPropertyName;
 	}
 
 	/**
 	 * Sets the name of the property which is the unique identifier of this DTO.
 	 * 
 	 * @param idPropertyName
 	 */
 	public void setIdPropertyName(final String idPropertyName) {
 		this.idPropertyName = idPropertyName;
 	}
 
 	/**
 	 * Adds a new property child DTO.<br>
 	 * Overwrites the old DTO with the same name if declared.
 	 * 
 	 * @param property
 	 */
 	public void addProperty(final IConfigDTO property) {
 		Assert.paramNotNull(property, "property"); //$NON-NLS-1$
 		if (property instanceof ComplexConfigDTO) {
 			addComplexProperty((ComplexConfigDTO) property);
 		} else if (property instanceof ConfigSetDTO) {
 			addSetProperty((ConfigSetDTO) property);
 		} else if (property instanceof ConfigListDTO) {
 			addListProperty((ConfigListDTO) property);
 		} else if (property instanceof ConfigMapDTO) {
 			addMapProperty((ConfigMapDTO) property);
 		} else if (property instanceof ConfigSimpleValueDTO) {
 			addSimpleValueProperty((ConfigSimpleValueDTO) property);
 		} else {
 			throw new IllegalArgumentException("Got unknown property: " + property); //$NON-NLS-1$
 		}
 	}
 
 	public void removeProperty(final String propertyName) {
 		simpleProperties.remove(propertyName);
 		complexProperties.remove(propertyName);
 		listProperties.remove(propertyName);
 		mapProperties.remove(propertyName);
 		setProperties.remove(propertyName);
 	}
 
 	/**
 	 * Returns the child property DTO with the given name.
 	 * 
 	 * @param propertyName
 	 * @return the child DTO with the given name or <code>null</code> if not declared
 	 */
 	public IConfigDTO getProperty(final String propertyName) {
 		IConfigDTO result = null;
 		result = getSimpleValueProperty(propertyName);
 		if (result != null) {
 			return result;
 		}
 		result = getComplexProperty(propertyName);
 		if (result != null) {
 			return result;
 		}
 		result = getListProperty(propertyName);
 		if (result != null) {
 			return result;
 		}
 		result = getSetProperty(propertyName);
 		if (result != null) {
 			return result;
 		}
 		result = getMapProperty(propertyName);
 
 		return result;
 	}
 
 	/**
 	 * Returns all complex child DTOs of this DTO.
 	 * 
 	 * @return all complex child DTOs
 	 */
 	public Set<ComplexConfigDTO> getComplexProperties() {
 		return new HashSet<ComplexConfigDTO>(complexProperties.values());
 	}
 
 	/**
 	 * Returns all list child DTOs of this DTO.
 	 * 
 	 * @return all list child DTOs
 	 */
 	public Set<ConfigListDTO> getListProperties() {
 		return new HashSet<ConfigListDTO>(listProperties.values());
 	}
 
 	/**
 	 * Returns all map child DTOs of this DTO.
 	 * 
 	 * @return all map child DTOs
 	 */
 	public Set<ConfigMapDTO> getMapProperties() {
 		return new HashSet<ConfigMapDTO>(mapProperties.values());
 	}
 
 	/**
 	 * Returns all set child DTOs of this DTO.
 	 * 
 	 * @return all set child DTOs
 	 */
 	public Set<ConfigSetDTO> getSetProperties() {
 		return new HashSet<ConfigSetDTO>(setProperties.values());
 	}
 
 	/**
 	 * Returns all simple child DTOs of this DTO.
 	 * 
 	 * @return all simple child DTOs
 	 */
 	public Set<ConfigSimpleValueDTO> getSimpleProperties() {
 		return new HashSet<ConfigSimpleValueDTO>(simpleProperties.values());
 	}
 
 	@Override
 	public ComplexConfigDTO deepCopy() {
 		return deepCopyToScopePath(getDefiningScopePath());
 	}
 
 	@Override
 	public ComplexConfigDTO deepCopyToScopePath(final IScopePath scopePath) {
 		final ComplexConfigDTO result = flatCopy();
 		result.setDefiningScopePath(scopePath);
 
 		for (final Entry<String, ComplexConfigDTO> entry : complexProperties.entrySet()) {
 			result.complexProperties.put(entry.getKey(), entry.getValue().deepCopyToScopePath(scopePath));
 		}
 		for (final Entry<String, ConfigSimpleValueDTO> entry : simpleProperties.entrySet()) {
 			result.simpleProperties.put(entry.getKey(), entry.getValue().deepCopyToScopePath(scopePath));
 		}
 		for (final Entry<String, ConfigSetDTO> entry : setProperties.entrySet()) {
 			result.setProperties.put(entry.getKey(), entry.getValue().deepCopyToScopePath(scopePath));
 		}
 		for (final Entry<String, ConfigListDTO> entry : listProperties.entrySet()) {
 			result.listProperties.put(entry.getKey(), entry.getValue().deepCopyToScopePath(scopePath));
 		}
 		for (final Entry<String, ConfigMapDTO> entry : mapProperties.entrySet()) {
 			result.mapProperties.put(entry.getKey(), entry.getValue().deepCopyToScopePath(scopePath));
 		}
 
 		return result;
 	}
 
 	@Override
 	public void visit(final IConfigDtoVisitor visitor) {
 		visitor.visitComplexDto(this);
 
 		for (final ConfigSimpleValueDTO simpleDto : simpleProperties.values()) {
 			simpleDto.visit(visitor);
 		}
 		for (final ComplexConfigDTO complexDto : complexProperties.values()) {
 			complexDto.visit(visitor);
 		}
 		for (final ConfigListDTO listDto : listProperties.values()) {
 			listDto.visit(visitor);
 		}
 		for (final ConfigSetDTO setDto : setProperties.values()) {
 			setDto.visit(visitor);
 		}
 		for (final ConfigMapDTO mapDto : mapProperties.values()) {
 			mapDto.visit(visitor);
 		}
 	}
 
 	@SuppressWarnings("nls")
 	@Override
 	public String toString() {
 		return "ComplexConfigDTO[declaredProperties=" + getDeclaredProperties() + "]";
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = super.hashCode();
 		result = prime * result + (int) (classVersion ^ (classVersion >>> 32));
 		result = prime * result + ((complexProperties == null) ? 0 : complexProperties.hashCode());
 		result = prime * result + ((idPropertyName == null) ? 0 : idPropertyName.hashCode());
 		result = prime * result + ((listProperties == null) ? 0 : listProperties.hashCode());
 		result = prime * result + ((mapProperties == null) ? 0 : mapProperties.hashCode());
 		result = prime * result + (nulled ? 1231 : 1237);
 		result = prime * result + ((setProperties == null) ? 0 : setProperties.hashCode());
 		result = prime * result + ((simpleProperties == null) ? 0 : simpleProperties.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(final Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (!super.equals(obj)) {
 			return false;
 		}
 		if (getClass() != obj.getClass()) {
 			return false;
 		}
 		final ComplexConfigDTO other = (ComplexConfigDTO) obj;
 		if (classVersion != other.classVersion) {
 			return false;
 		}
 		if (complexProperties == null) {
 			if (other.complexProperties != null) {
 				return false;
 			}
 		} else if (!complexProperties.equals(other.complexProperties)) {
 			return false;
 		}
 		if (idPropertyName == null) {
 			if (other.idPropertyName != null) {
 				return false;
 			}
 		} else if (!idPropertyName.equals(other.idPropertyName)) {
 			return false;
 		}
 		if (listProperties == null) {
 			if (other.listProperties != null) {
 				return false;
 			}
 		} else if (!listProperties.equals(other.listProperties)) {
 			return false;
 		}
 		if (mapProperties == null) {
 			if (other.mapProperties != null) {
 				return false;
 			}
 		} else if (!mapProperties.equals(other.mapProperties)) {
 			return false;
 		}
 		if (nulled != other.nulled) {
 			return false;
 		}
 		if (setProperties == null) {
 			if (other.setProperties != null) {
 				return false;
 			}
 		} else if (!setProperties.equals(other.setProperties)) {
 			return false;
 		}
 		if (simpleProperties == null) {
 			if (other.simpleProperties != null) {
 				return false;
 			}
 		} else if (!simpleProperties.equals(other.simpleProperties)) {
 			return false;
 		}
 		return true;
 	}
 
 }
