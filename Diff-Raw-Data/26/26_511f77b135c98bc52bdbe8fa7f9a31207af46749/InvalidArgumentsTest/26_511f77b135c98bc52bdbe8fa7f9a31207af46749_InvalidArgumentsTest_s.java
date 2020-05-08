 /**
  * <copyright> Copyright (c) 2008-2009 Jonas Helming, Maximilian Koegel. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0 which accompanies this
  * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html </copyright>
  */
 package org.eclipse.emf.emfstore.client.test.server;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 
 import org.eclipse.emf.emfstore.client.model.connectionmanager.xmlrpc.XmlRpcClientManager;
 import org.eclipse.emf.emfstore.client.model.connectionmanager.xmlrpc.XmlRpcConnectionManager;
 import org.eclipse.emf.emfstore.client.test.SetupHelper;
 import org.eclipse.emf.emfstore.common.CommonUtil;
 import org.eclipse.emf.emfstore.common.model.Project;
 import org.eclipse.emf.emfstore.server.ServerConfiguration;
 import org.eclipse.emf.emfstore.server.exceptions.EmfStoreException;
 import org.eclipse.emf.emfstore.server.exceptions.InvalidInputException;
 import org.eclipse.emf.emfstore.server.exceptions.UnknownSessionException;
 import org.eclipse.emf.emfstore.server.model.ProjectId;
 import org.eclipse.emf.emfstore.server.model.SessionId;
 import org.eclipse.emf.emfstore.server.model.versioning.ChangePackage;
 import org.eclipse.emf.emfstore.server.model.versioning.HistoryQuery;
 import org.eclipse.emf.emfstore.server.model.versioning.LogMessage;
 import org.eclipse.emf.emfstore.server.model.versioning.PrimaryVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.TagVersionSpec;
 import org.eclipse.emf.emfstore.server.model.versioning.VersionSpec;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * This test calls the servermethods with null arguments.
  * 
  * @author wesendon
  */
 public class InvalidArgumentsTest extends ServerTests {
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @throws EmfStoreException in case of failure
 	 * @throws IOException
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() throws EmfStoreException, IOException {
 		ServerConfiguration.setTesting(true);
 		CommonUtil.setTesting(true);
 
 		SetupHelper.addUserFileToServer(false);
 
 		// setConnectionManager(WorkspaceManager.getInstance().getConnectionManager());
 		setServerInfo(SetupHelper.getServerInfo());
 		// login();
 		initArguments();
 
 		setConnectionManager(new XmlRpcConnectionManager() {
 			@Override
 			protected XmlRpcClientManager getConnectionProxy(SessionId sessionId) throws UnknownSessionException {
 				if (sessionId == null && getConnectionProxyMap().size() > 0) {
 					return getConnectionProxyMap().values().iterator().next();
 				}
 				return super.getConnectionProxy(sessionId);
 
 			}
 		});
 
 		SetupHelper.startSever();
 		ServerTests.login();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Test(expected = InvalidInputException.class)
 	public void deleteProjectTest() throws EmfStoreException {
 		try {
 			testAllInvalidCombinations(getConnectionManager().getClass().getMethod("deleteProject",
 				new Class[] { SessionId.class, ProjectId.class, boolean.class }));
 		} catch (SecurityException e) {
 			throw new EmfStoreException(e);
 		} catch (NoSuchMethodException e) {
 			throw new EmfStoreException(e);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Test(expected = InvalidInputException.class)
 	public void createProjectTest() throws EmfStoreException {
 		try {
 			testAllInvalidCombinations(getConnectionManager().getClass().getMethod("createEmptyProject",
 				new Class[] { SessionId.class, String.class, String.class, LogMessage.class }));
 		} catch (SecurityException e) {
 			throw new EmfStoreException(e);
 		} catch (NoSuchMethodException e) {
 			throw new EmfStoreException(e);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Test(expected = InvalidInputException.class)
 	public void createProject2Test() throws EmfStoreException {
 		try {
 			testAllInvalidCombinations(getConnectionManager().getClass().getMethod("createProject",
 				new Class[] { SessionId.class, String.class, String.class, LogMessage.class, Project.class }));
 		} catch (SecurityException e) {
 			throw new EmfStoreException(e);
 		} catch (NoSuchMethodException e) {
 			throw new EmfStoreException(e);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Test(expected = InvalidInputException.class)
 	public void createVersionTest() throws EmfStoreException {
 		try {
 			testAllInvalidCombinations(getConnectionManager().getClass().getMethod(
 				"createVersion",
 				new Class[] { SessionId.class, ProjectId.class, PrimaryVersionSpec.class, ChangePackage.class,
					LogMessage.class }));
 		} catch (SecurityException e) {
 			throw new EmfStoreException(e);
 		} catch (NoSuchMethodException e) {
 			throw new EmfStoreException(e);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Test(expected = InvalidInputException.class)
 	public void getChangesTest() throws EmfStoreException {
 		try {
 			testAllInvalidCombinations(getConnectionManager().getClass().getMethod("getChanges",
 				new Class[] { SessionId.class, ProjectId.class, VersionSpec.class, VersionSpec.class }));
 		} catch (SecurityException e) {
 			throw new EmfStoreException(e);
 		} catch (NoSuchMethodException e) {
 			throw new EmfStoreException(e);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Test(expected = InvalidInputException.class)
 	public void getHistoryInfoTest() throws EmfStoreException {
 		try {
 			testAllInvalidCombinations(getConnectionManager().getClass().getMethod("getHistoryInfo",
 				new Class[] { SessionId.class, ProjectId.class, HistoryQuery.class }));
 		} catch (SecurityException e) {
 			throw new EmfStoreException(e);
 		} catch (NoSuchMethodException e) {
 			throw new EmfStoreException(e);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Test(expected = InvalidInputException.class)
 	public void getProjectTest() throws EmfStoreException {
 		try {
 			testAllInvalidCombinations(getConnectionManager().getClass().getMethod("getProject",
 				new Class[] { SessionId.class, ProjectId.class, VersionSpec.class }));
 		} catch (SecurityException e) {
 			throw new EmfStoreException(e);
 		} catch (NoSuchMethodException e) {
 			throw new EmfStoreException(e);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Test(expected = InvalidInputException.class)
 	public void addTagTest() throws EmfStoreException {
 		try {
 			testAllInvalidCombinations(getConnectionManager().getClass().getMethod("addTag",
 				new Class[] { SessionId.class, ProjectId.class, PrimaryVersionSpec.class, TagVersionSpec.class }));
 		} catch (SecurityException e) {
 			throw new EmfStoreException(e);
 		} catch (NoSuchMethodException e) {
 			throw new EmfStoreException(e);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Test(expected = InvalidInputException.class)
 	public void removeTagTest() throws EmfStoreException {
 		try {
 			testAllInvalidCombinations(getConnectionManager().getClass().getMethod("removeTag",
 				new Class[] { SessionId.class, ProjectId.class, PrimaryVersionSpec.class, TagVersionSpec.class }));
 		} catch (SecurityException e) {
 			throw new EmfStoreException(e);
 		} catch (NoSuchMethodException e) {
 			throw new EmfStoreException(e);
 		}
 	}
 
 	private void testAllInvalidCombinations(Method method) throws EmfStoreException {
 		int parameterLength = method.getParameterTypes().length;
 		Object[] parameters = new Object[parameterLength];
 		int combinations = (int) (Math.round(Math.pow(2, parameterLength)) - 1);
 		for (int i = 0; i < combinations; i++) {
 			for (int j = 0; j < parameterLength; j++) {
 				parameters[j] = getParameter(method.getParameterTypes()[j], getArgument(j, i));
 			}
 			callMethod(method, parameters);
 		}
 	}
 
 	private void callMethod(Method method, Object[] parameters) throws EmfStoreException {
 		if (method.getParameterTypes().length != parameters.length) {
 			throw new AssertionError("parameter length not equal");
 		}
 		// for (int i = 0; i < parameters.length; i++) {
 		// System.out.print(method.getParameterTypes()[i] + " " + parameters[i] + "; ");
 		// }
 		// System.out.println("");
 		try {
 			// method.invoke(getConnectionManager(), null, null, false);
 			method.invoke(getConnectionManager(), parameters);
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 			Assert.assertTrue(false);
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 			Assert.assertTrue(false);
 		} catch (InvocationTargetException e) {
 			if (e.getCause() instanceof EmfStoreException) {
 				throw (EmfStoreException) e.getCause();
 			}
 			Assert.assertTrue(false);
 		}
 		throw new AssertionError("No exception was thrown");
 	}
 
 	private boolean getArgument(int j, int i) {
 		if (j == 0) {
 			return (i & 1) == 1;
 		}
 		int position = (int) Math.pow(2, j);
 		return (i & position) == position;
 	}
 }
