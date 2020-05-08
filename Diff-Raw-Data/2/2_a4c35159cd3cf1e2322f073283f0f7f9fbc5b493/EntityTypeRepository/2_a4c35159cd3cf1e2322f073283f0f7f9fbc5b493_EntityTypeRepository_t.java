 /*******************************************************************************
  * Stefan Meyer, 2012
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package org.atemsource.atem.api;
 
 import java.util.Collection;
 
 import org.atemsource.atem.api.type.EntityType;
 import org.atemsource.atem.api.type.PrimitiveType;
 import org.atemsource.atem.api.type.Type;
 
 // TODO: Auto-generated Javadoc
 /**
  * The application singleton that provides access to all types.
  * 
  * 
  * @author Stefan Meyer
  */
 public interface EntityTypeRepository {
 
 	/**
 	 * Get the EntityType for a class. Returns null if none was found.
 	 * 
 	 * @param <J>
 	 *            the generic type
 	 * @param clazz
 	 *            the class
 	 * @return the EntityType representing the class
 	 */
 	<J> EntityType<J> getEntityType(Class<J> clazz);
 
 	/**
 	 * Get the EntityType for an entity. Returns null if none was found.
 	 * 
 	 * @param <J>
 	 *            the generic type
 	 * @param entity
 	 *            the entity
 	 * @return the EntityType describing the entity
 	 */
 	<J> EntityType<J> getEntityType(J entity);
 
 	/**
 	 * Get the EntityType by its code. Returns null if none was found.
 	 * 
 	 * @param <J>
 	 *            the generic type
 	 * @param typeCode
 	 *            the code/id of the EntityType.
 	 * @return the EntityType
 	 */
 	<J> EntityType<J> getEntityType(String typeCode);
 
 	/**
 	 * Get all EntityTypes.
 	 * 
 	 * @param <J>
 	 *            the generic type
 	 * @return all EntityTypes
 	 */
	Collection<EntityType<?>> getEntityTypes();
 
 	/**
 	 * Get the Type for a class. Returns null if none was found.
 	 * 
 	 * @param <J>
 	 *            the generic type
 	 * @param clazz
 	 *            the class
 	 * @return the Type representing the class
 	 */
 	<J> Type<J> getType(Class<J> clazz);
 
 	/**
 	 * Get the Type for an entity. Returns null if none was found.
 	 * 
 	 * @param <J>
 	 *            the generic type
 	 * @param value
 	 *            the value
 	 * @return the Type describing the value
 	 */
 	<J> Type<J> getType(J value);
 	/**
 	 * 
 	 * @param clazz
 	 * @param primitiveType
 	 */
 	 void registerType(Class<?> clazz, PrimitiveType primitiveType);
 
 
 }
