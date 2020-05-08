 /*******************************************************************************
  * Copyright (c) 2007, 2014 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets;
 
 import java.lang.reflect.Modifier;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.osgi.service.log.LogService;
 
 import org.eclipse.core.databinding.BindingException;
 import org.eclipse.equinox.log.Logger;
 
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.core.singleton.SingletonProvider;
 import org.eclipse.riena.internal.ui.ridgets.Activator;
 
 /**
  * A mapper that maps ridget interfaces to concrete ridget implementations.
  * 
  * @since 2.0
  */
 public final class ClassRidgetMapper {
 
 	private static final SingletonProvider<ClassRidgetMapper> CRM = new SingletonProvider<ClassRidgetMapper>(
 			ClassRidgetMapper.class);
 
	private final Map<Class<? extends IRidget>, Class<? extends IRidget>> mappings = new HashMap<Class<? extends IRidget>, Class<? extends IRidget>>();
 
 	private static final Logger LOGGER = Log4r.getLogger(Activator.getDefault(), ClassRidgetMapper.class);
 
 	/**
 	 * Answer the singleton <code>ClassRidgetMapper</code>
 	 * 
 	 * @return the ClassRidgetMapper singleton
 	 */
 	public static ClassRidgetMapper getInstance() {
 		return CRM.getInstance();
 	}
 
 	private ClassRidgetMapper() {
 	}
 
 	/**
 	 * Adds an Interface Class pair to the mapper. If the Interface is already
 	 * existent, the previous value will be overwritten.
 	 * 
 	 * @param ridgetInterface
 	 *            an interface extending <code>IRidget</code>
 	 * @param ridgetClazz
 	 *            a concrete class implementing <code>IRidget</code>. If
 	 *            ridgetClazz is abstract or an interface it will not be added
 	 *            and warning is logged.
 	 */
 	public void addMapping(final Class<? extends IRidget> ridgetInterface, final Class<? extends IRidget> ridgetClazz) {
 		if (ridgetInterface == null || ridgetClazz == null) {
 			return;
 		}
 
 		if (!ridgetClazz.isInterface() && !Modifier.isAbstract(ridgetClazz.getModifiers())) {
 			mappings.put(ridgetInterface, ridgetClazz);
 		} else {
 			LOGGER.log(LogService.LOG_WARNING, "The interface " + ridgetInterface.getName() + " and the class " //$NON-NLS-1$//$NON-NLS-2$
 					+ ridgetClazz.getName() + " could not be added to the map, because " + ridgetClazz.getName() //$NON-NLS-1$
 					+ " is an abstract class or an interface"); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Returns the ridget class that belongs to the ridgetInterface in the
 	 * mapper.
 	 * 
 	 * @param ridgetInterface
 	 *            the key to search for
 	 * @return the ridget class that belongs to the ridgetInterface in the
 	 *         mapper
 	 * @throws throws a <code>BindingException</code> if the ridgetInterface
 	 *         cannot be found in the mapper.
 	 */
 	public Class<? extends IRidget> getRidgetClass(final Class<? extends IRidget> ridgetInterface) {
 		final Class<? extends IRidget> ridgetClass = mappings.get(ridgetInterface);
 		if (ridgetClass != null) {
 			return ridgetClass;
 		}
 
 		throw new BindingException("No Ridget class defined for interface " + ridgetInterface.getName()); //$NON-NLS-1$
 	}
 }
