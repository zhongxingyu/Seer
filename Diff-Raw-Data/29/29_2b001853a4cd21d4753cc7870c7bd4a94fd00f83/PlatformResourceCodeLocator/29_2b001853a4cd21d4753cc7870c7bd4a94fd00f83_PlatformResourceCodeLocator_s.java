 /*******************************************************************************
  * Copyright (c) 2008 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *   Pavel Erofeev - refactored for parsing with regexps
  *                   locator for platform:/resource URIs
  *******************************************************************************/
 
 package org.eclipse.wazaabi.locator.platform.resource.codelocators;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.wazaabi.engine.edp.coderesolution.AbstractCodeDescriptor;
 import org.eclipse.wazaabi.engine.edp.coderesolution.AbstractCodeLocator;
 import org.eclipse.wazaabi.locator.platform.resource.codedescriptors.ResourceCodeDescriptor;
 
 public class PlatformResourceCodeLocator extends AbstractCodeLocator {
 
 	public static final String FACTORY_ID = PlatformResourceCodeLocator.class
 			.getName();
 
	static private final String URI_PREFIX = "platform:/plugin/"; //$NON-NLS-1$ 
 	static private final String LANGUAGE = "java"; //$NON-NLS-1$
 
 	private static final Pattern PATTERN = Pattern.compile(URI_PREFIX
 			+ "([^/]+)/([^\\?]+)(\\?language=(\\w+))?"); //$NON-NLS-1$ 
 	private static final int PATTERN_BUNDLE = 1;
 	private static final int PATTERN_PATH = 2;
 	private static final int PATTERN_LANGUAGE = 4;
 
 	private final ResourceSet rset = new ResourceSetImpl();
 
 	public AbstractCodeDescriptor resolveCodeDescriptor(String uri) {
 		Matcher m = PATTERN.matcher(uri);
 		if (m.matches())
 			return new ResourceCodeDescriptor(m.group(PATTERN_BUNDLE),
 					m.group(PATTERN_PATH));
 		return null;
 	}
 
 	public InputStream getResourceInputStream(String uri) throws IOException {
 		URI u = URI.createURI(uri);
 		if (u != null)
 			return rset.getURIConverter().createInputStream(u);
 		return null;
 	}
 
 	@Override
 	public Object createComponent(Object callingContext, Object model,
 			Object creationHint) {
 		if (model instanceof String) {
 			Matcher m = PATTERN.matcher((String) model);
 			if (m.matches())
 				return new ResourceCodeDescriptor(m.group(PATTERN_BUNDLE),
 						m.group(PATTERN_PATH));
 		}
 		return null;
 	}
 
 	@Override
 	public String getFactoryID() {
 		return FACTORY_ID;
 	}
 
 	@Override
 	public boolean isFactoryFor(Object callingContext, Object model, Object creationHint) {
 		if (!(model instanceof String))
 			return false;
 		Matcher m = PATTERN.matcher((String) model);
 		if (m.matches()) {
 			String language = m.group(PATTERN_LANGUAGE);
 			return language == null || LANGUAGE.equals(language);
 		}
 		return false;
 	}
 }
