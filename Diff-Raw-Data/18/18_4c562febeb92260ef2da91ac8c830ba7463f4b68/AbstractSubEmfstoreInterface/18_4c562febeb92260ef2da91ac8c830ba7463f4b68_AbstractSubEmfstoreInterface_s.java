 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  ******************************************************************************/
 package org.eclipse.emf.emfstore.server.core;
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.common.model.util.ModelUtil;
 import org.eclipse.emf.emfstore.server.accesscontrol.AuthorizationControl;
 import org.eclipse.emf.emfstore.server.core.helper.ResourceHelper;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.exceptions.FatalEmfStoreException;
 import org.eclipse.emf.emfstore.server.exceptions.InvalidInputException;
 import org.eclipse.emf.emfstore.server.model.ServerSpace;
 import org.eclipse.emf.emfstore.server.model.SessionId;
 
 /**
  * This is the super class for all subinterfaces of emfstore. Main interfaces, such as {@link EmfStoreImpl}, check and
  * than delegates method calls to these subinterfaces, where the actual functionality is implemented. Subinterfaces
  * shouldn't be accessed without the corresponding main interface, because they rely on the sanity checks of the main
  * interfaces. The idea behind subinterfaces is to divide an emfstore interface into logical pieces and to avoid huge
  * classes.
  * 
  * @author wesendon
  */
 public abstract class AbstractSubEmfstoreInterface {
 
 	private final AbstractEmfstoreInterface parentInterface;
 	private ResourceHelper resourceHelper;
 
 	/**
 	 * Default constructor.
 	 * 
 	 * @param parentInterface parentInterface
 	 * @throws FatalEmfStoreException if parent interface is null
 	 */
 	public AbstractSubEmfstoreInterface(AbstractEmfstoreInterface parentInterface) throws FatalEmfStoreException {
 		if (parentInterface == null) {
 			throw new FatalEmfStoreException();
 		}
 		this.parentInterface = parentInterface;
 	}
 
 	/**
 	 * This method is called after the initialisation of the parent interface.
 	 * 
 	 * @throws FatalEmfStoreException exception
 	 */
 	protected void initSubInterface() throws FatalEmfStoreException {
 		resourceHelper = new ResourceHelper(parentInterface.getServerSpace());
 	}
 
 	/**
 	 * Returns the ResourceHelper.
 	 * 
 	 * @return resourceHelper
 	 */
 	public ResourceHelper getResourceHelper() {
 		return resourceHelper;
 	}
 
 	/**
 	 * Saves an eObject.
 	 * 
 	 * @param object the object
 	 * @throws FatalEmfStoreException in case of failure
 	 */
 	protected void save(EObject object) throws FatalEmfStoreException {
 		resourceHelper.save(object);
 	}
 
 	/**
 	 * Saves an eObject together with all model element IDs of the given project.
 	 * 
 	 * @param object the object
 	 * @param project the project, that contains the model element IDs to be saved
 	 * @throws FatalEmfStoreException in case of failure
 	 */
 	protected void saveWithProject(EObject object, Project project) throws FatalEmfStoreException {
 		resourceHelper.saveWithProject(object, project);
 	}
 
 	/**
 	 * Returns the serverspace. Please always use a monitor ({@link #getMonitor()}) when operating on the serverspace.
 	 * 
 	 * @return serverspace
 	 */
 	protected ServerSpace getServerSpace() {
 		return parentInterface.getServerSpace();
 	}
 
 	/**
 	 * Return a monitor object which should be used when operating on the serverspace.
 	 * 
 	 * @return monitor object
 	 */
 	protected Object getMonitor() {
 		return parentInterface.getMonitor();
 	}
 
 	/**
 	 * Returns the authorizationControl.
 	 * 
 	 * @return authorizationControl
 	 */
 	protected AuthorizationControl getAuthorizationControl() {
 		return parentInterface.getAuthorizationControl();
 	}
 
 	/**
 	 * This method gets a subinterface from the parent interface. Can be used if you need some functionality from
 	 * another subinterface.
 	 * 
 	 * @param <T> subinterface type
 	 * @param clazz class of subinterface
 	 * @return subinterface
 	 */
 	protected <T> T getSubInterface(Class<T> clazz) {
 		return parentInterface.getSubInterface(clazz);
 	}
 
 	/**
 	 * Executes the given method. This will check if the method need the session id as first parameter and invoke the
 	 * method with the correct parameters.
 	 * 
 	 * @param method the method to invoke
 	 * @param args parameters
 	 * @return result of the operation
 	 * @throws EmfStoreException thrown if operation could not be executed properly
 	 */
 	public Object execute(Method method, Object[] args) throws EmfStoreException {
 		try {
 			if (method.getParameterTypes()[0] == SessionId.class) {
 				return method.invoke(this, args);
 			}
 			Object[] argsWoSessionId = new Object[args.length - 1];
 			System.arraycopy(args, 1, argsWoSessionId, 0, args.length - 1);
 			return method.invoke(this, argsWoSessionId);
 		} catch (IllegalArgumentException e) {
 			ModelUtil.logWarning("this must not happen, bad parameters", e);
 			throw new EmfStoreException(e);
 		} catch (IllegalAccessException e) {
 			ModelUtil.logWarning("this must not happen, method is not accessible", e);
 			throw new EmfStoreException(e);
 		} catch (InvocationTargetException e) {
 			ModelUtil.logWarning("exception on execution", e);
 			throw new EmfStoreException(e.getTargetException());
 		}
 	}
 
 	/**
 	 * Applies a sanity check {@link #sanityCheckObject(Object)} to all given objects. Elements will be checked in the
 	 * same order as the input. This allows you to check attributes as well. E.g.: <code>sanityCheckObjects(element,
 	 * element.getAttribute())</code>. Due to the order, it is important to enter the element BEFORE the attribute,
 	 * otherwise a NPE would occur, if the element would be null.
 	 * 
 	 * @param objects objects to check
 	 * @throws InvalidInputException is thrown if the check fails
 	 */
 	protected void sanityCheckObjects(Object... objects) throws InvalidInputException {
 		for (Object object : objects) {
 			sanityCheckObject(object);
 		}
 	}
 
 	/**
 	 * Checks whether a given object is null. Further sanity checks could be added. <strong>Note:</strong> Maybe we
 	 * should use specialized sanity checks for EObjects or other types.
 	 * 
 	 * @param object object to check
 	 * @throws InvalidInputException is thrown if the check fails
 	 */
 	private void sanityCheckObject(Object object) throws InvalidInputException {
 		if (object == null) {
 			throw new InvalidInputException();
 		}
 	}
 
 }
