 /*******************************************************************************
  * Copyright (c) 2007, 2013 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.core.util;
 
 import java.io.File;
import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.osgi.framework.FrameworkUtil;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.variables.IDynamicVariable;
 import org.eclipse.core.variables.IDynamicVariableResolver;
 
 /**
  * A dynamic variable resolver for function evaluation.
  * <p>
  * The usage is as follows:
  * 
  * <pre>
  * ${fn:&lt;funtionName&gt;[ , argument ]* }
  * </pre>
  * 
  * For example - it is possible to write nested calls such as:
  * 
  * <pre>
  * ${fn:toFile,${java.system.property:osgi.instance.area}}
  * </pre>
  * 
 * Currently available is only the function "toFile" which expects one argument
 * that is the URL representation of a file and it returns the canonical form of
  * the file representation.
  * 
  * @since 3.0
  */
 public class FunctionResolver implements IDynamicVariableResolver {
 
 	private final Map<String, IFun> funs = new HashMap<String, FunctionResolver.IFun>();
 
 	public FunctionResolver() {
 		funs.put("toFile", new ToFile()); //$NON-NLS-1$
 	}
 
 	/*
 	 * {@inheritDoc}
 	 */
 	public String resolveValue(final IDynamicVariable variable, final String argument) throws CoreException {
 		if (argument == null) {
 			throw createCoreException("expects at least one argument (the function name)", null); //$NON-NLS-1$
 		}
 		final String[] arguments = argument.split(","); //$NON-NLS-1$
 		if (arguments.length == 0) {
 			throw createCoreException("expects at least one argument (the function name)", null); //$NON-NLS-1$
 		}
 		final IFun fun = funs.get(arguments[0]);
 		if (fun == null) {
 			throw createCoreException("called with unknown function '" + arguments[0] + "'", null); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		try {
 			final String[] funArgs = new String[arguments.length - 1];
 			System.arraycopy(arguments, 1, funArgs, 0, arguments.length - 1);
 			return fun.apply(funArgs);
 		} catch (final Throwable t) {
 			throw createCoreException(" failed calling '" + arguments[0] + "'", t); //$NON-NLS-1$//$NON-NLS-2$
 		}
 	}
 
 	private CoreException createCoreException(final String message, final Throwable t) {
		return new CoreException(new Status(Status.ERROR, FrameworkUtil.getBundle(FunctionResolver.class)
				.getSymbolicName(), FunctionResolver.class.getSimpleName() + " " + message, t)); //$NON-NLS-1$
 	}
 
 	private interface IFun {
 		String apply(String[] arguments) throws Exception;
 	}
 
 	private static class ToFile implements IFun {
 
 		public String apply(final String[] arguments) throws Exception {
 			Assert.isLegal(arguments.length == 1, "exactly one argument expected (a file URL)"); //$NON-NLS-1$
			final URL url = new URL(arguments[0]);
			return new File(url.getPath()).getAbsolutePath().replace('\\', '/');
 		}
 	}
 }
