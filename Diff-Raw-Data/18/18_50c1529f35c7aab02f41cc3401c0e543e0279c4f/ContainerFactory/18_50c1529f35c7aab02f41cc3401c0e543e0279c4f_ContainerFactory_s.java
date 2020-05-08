 /*
  * Copyright 2012 Vincent Demeester<vincent+shortbrain@demeester.fr>.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.shortbrain.vaadin.container;
 
 import java.util.List;
 
 import org.shortbrain.vaadin.container.annotation.ContainerType;
 import org.shortbrain.vaadin.container.property.AnnotationReaderAlgorithm;
 import org.shortbrain.vaadin.container.property.AttributeReaderAlgorithm;
 import org.shortbrain.vaadin.container.property.GetterReaderAlgorithm;
 import org.shortbrain.vaadin.container.property.PropertyReaderAlgorithm;
 
 import com.vaadin.data.Container;
 
 /**
  * ContainerFactory abstract class that define methods to facilitate the
  * creation of Containers from a list of objects using different algorithms.
  * 
  * @author Vincent Demeester <vincent@demeester.fr>
  * 
  * @param <BEAN>
  *            type of the beans.
  */
 public abstract class ContainerFactory<BEAN> {
 
 	/**
 	 * Return a container of type BEAN from a list of BEAN objects. It will
 	 * update the given container if no null.
 	 * 
 	 * @param container
 	 *            container to be populated.
 	 * @param beans
 	 *            list of beans.
 	 * @return a container of Beans.
 	 */
 	public abstract Container getContainerFromList(Container container,
 			List<BEAN> beans);
 
 	/**
 	 * Return a container of type BEAN from a list of BEAN objects. It will
 	 * update the given container if no null.
 	 * 
 	 * The returned container will be of the given type (containerClass).
 	 * 
 	 * @param container
 	 *            container to be populated.
 	 * @param beans
 	 *            list of beans.
 	 * @param containerClass
 	 *            type of the container to return.
 	 * @return a container of Beans.
 	 */
 	public abstract Container getContainerFromList(Container container,
 			List<BEAN> beans, Class<? extends Container> containerClass);
 
 	/**
 	 * Return a container of type BEAN from a list of BEAN objects.
 	 * 
 	 * The returned container will be of the given type (containerClass).
 	 * 
 	 * @param beans
 	 *            list of beans.
 	 * @param containerClass
 	 *            type of the container to return.
 	 * @return a container of Beans.
 	 */
 	public abstract Container getContainerFromList(List<BEAN> beans,
 			Class<? extends Container> containerClass);
 
 	/**
 	 * Create a ContainerFactory of type T using a default
 	 * {@link AttributeReaderAlgorithm}.
 	 * 
 	 * @see ContainerFactory#getByAlgorithm(Class, PropertyReaderAlgorithm)
 	 * 
 	 * @param beanClass
 	 *            type of the container.
 	 * @return a ContainerFactory.
 	 */
 	public final static <T> ContainerFactory<T> getByAttributes(
 			Class<? extends T> beanClass) {
 		return getByAlgorithm(beanClass, new AttributeReaderAlgorithm());
 	}
 
 	/**
 	 * Create a ContainerFactory of type T using a default
 	 * {@link GetterReaderAlgorithm}.
 	 * 
 	 * @see ContainerFactory#getByAlgorithm(Class, PropertyReaderAlgorithm)
 	 * 
 	 * @param beanClass
 	 *            type of the container.
 	 * @return a ContainerFactory.
 	 */
 	public final static <T> ContainerFactory<T> getByGetters(
 			Class<? extends T> beanClass) {
 		return getByAlgorithm(beanClass, new GetterReaderAlgorithm());
 	}
 
 	/**
 	 * Create a ContainerFactory of type T using a default
 	 * {@link AnnotationReaderAlgorithm}.
 	 * 
 	 * @see ContainerFactory#getByAlgorithm(Class, PropertyReaderAlgorithm)
 	 * 
 	 * @param beanClass
 	 *            type of the container.
 	 * @param containerType
 	 *            type of container (used by AnnotationReaderAlgorithm)
 	 * @return a ContainerFactory.
 	 */
 	public final static <T> ContainerFactory<T> getByAnnotation(
 			Class<? extends T> beanClass, ContainerType containerType) {
 		return getByAlgorithm(beanClass, new AnnotationReaderAlgorithm(
 				containerType));
 	}
 
 	/**
 	 * Create a ContainerFactory of type T using a
 	 * {@link PropertyReaderAlgorithm}.
 	 * 
 	 * @param beanClass
 	 *            type of the container.
 	 * @param algorithm
 	 *            PropertyReaderAlgorithm to use.
 	 * @return a ContainerFactory.
 	 */
 	public final static <T> ContainerFactory<T> getByAlgorithm(
 			Class<? extends T> beanClass, PropertyReaderAlgorithm algorithm) {
 		return new AbstractContainerFactory<T>(beanClass, algorithm) {
 		};
 	}
 }
