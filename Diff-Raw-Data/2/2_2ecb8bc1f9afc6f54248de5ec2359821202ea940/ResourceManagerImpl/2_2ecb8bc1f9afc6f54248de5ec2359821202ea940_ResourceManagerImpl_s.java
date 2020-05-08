 /*
  * Copyright (c) 2006, 2009 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Artem Tikhomirov (Borland)
  *     Boris Blajer (Borland) - support for composite resources
  */
 package org.eclipse.gmf.internal.xpand.util;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.gmf.internal.xpand.Activator;
 import org.eclipse.gmf.internal.xpand.ResourceManager;
 import org.eclipse.gmf.internal.xpand.model.XpandResource;
 import org.eclipse.gmf.internal.xpand.xtend.ast.QvtFile;
 import org.eclipse.gmf.internal.xpand.xtend.ast.QvtResource;
 import org.eclipse.m2m.internal.qvt.oml.common.MdaException;
 import org.eclipse.m2m.internal.qvt.oml.compiler.CompiledUnit;
 import org.eclipse.m2m.internal.qvt.oml.compiler.QVTOCompiler;
 import org.eclipse.m2m.internal.qvt.oml.compiler.QvtCompilerOptions;
 import org.eclipse.m2m.internal.qvt.oml.compiler.UnitProxy;
 import org.eclipse.m2m.internal.qvt.oml.compiler.UnitResolver;
 
 // FIXME it's not a good idea to parse file on every proposal computation
 public abstract class ResourceManagerImpl implements ResourceManager {
 
 	private final Map<String, XpandResource> cachedXpand = new TreeMap<String, XpandResource>();
 
 	private final Map<String, QvtResource> cachedQvt = new TreeMap<String, QvtResource>();
 
 	private QVTOCompiler qvtCompiler;
 
 	private QvtCompilerOptions qvtCompilerOptions;
 
 	public QvtResource loadQvtResource(String fullyQualifiedName) {
 		try {
 			return loadQvtResourceThroughCache(fullyQualifiedName);
 		} catch (FileNotFoundException ex) {
 			return null; // Missing resource is an anticipated situation, not a
 			// error that should be handled
 		} catch (IOException e) {
 			Activator.logError(e);
 		} catch (ParserException e) {
 			// TODO: check if any exceptions present here at all..
 			handleParserException(e);
 		}
 		return null;
 	}
 
 	protected QvtResource loadQvtResourceThroughCache(String qualifiedName) throws IOException, ParserException {
 		if (hasCachedQvt(qualifiedName)) {
 			return cachedQvt.get(qualifiedName);
 		}
 		final QvtResource loaded = doLoadQvtResource(qualifiedName);
 		assert loaded != null; // this is the contract of loadXtendResource
 		if (shouldCache()) {
 			cachedQvt.put(qualifiedName, loaded);
 		}
 		return loaded;
 	}
 
 	private QvtResource doLoadQvtResource(String fullyQualifiedName) throws IOException, ParserException {
 		String compilationUnitQName = fullyQualifiedName.replace(TypeNameUtil.NS_DELIM, "."); //$NON-NLS-1$ 
 		CompiledUnit compiledUnit = null;
 		try {
			UnitProxy unitProxy = getQVTUnitResolver().resolveUnit(fullyQualifiedName);
 			if (unitProxy == null) {
 				throw new FileNotFoundException("Failed to resolve: " + fullyQualifiedName); //$NON-NLS-1$
 			}
 			compiledUnit = getQvtCompiler().compile(unitProxy, getQvtCompilerOptions(), null);
 		} catch (MdaException e) {
 			throw new FileNotFoundException(fullyQualifiedName);
 		}
 
 		if (compiledUnit == null) {
 			throw new FileNotFoundException(fullyQualifiedName);
 		}
 		return new QvtFile(compiledUnit, fullyQualifiedName);
 	}
 
 	abstract protected String resolveCFileFullPath(String fullyQualifiedName, String fileExtension);
 
 	/**
 	 * Using singleton QvtCompiler instance with "history". To prevent same
 	 * (native) libraries from being loaded twice into if (indirectly)
 	 * references by two different XpandResources.
 	 */
 	private QVTOCompiler getQvtCompiler() {
 		if (qvtCompiler == null) {
 			// TODO: use different kind of ImportResolver being able to
 			// construct referenced CFiles using ResourceManagerImpl	
 			qvtCompiler = QVTOCompiler.createCompilerWithHistory(getMetamodelResourceSet());
 		}
 		return qvtCompiler;
 	}
 
 	protected ResourceSet getMetamodelResourceSet() {
 		return Activator.getWorkspaceMetamodelsResourceSet();
 	}
 
 	private QvtCompilerOptions getQvtCompilerOptions() {
 		if (qvtCompilerOptions == null) {
 			qvtCompilerOptions = new QvtCompilerOptions();
 			qvtCompilerOptions.setGenerateCompletionData(true);
 			qvtCompilerOptions.setShowAnnotations(false);
 		}
 		return qvtCompilerOptions;
 	}
 
 	public XpandResource loadXpandResource(String fullyQualifiedName) {
 		try {
 			return loadXpandThroughCache(fullyQualifiedName);
 		} catch (FileNotFoundException ex) {
 			// Missing resource is an anticipated situation, not a error that should be handled
 			return null;
 		} catch (IOException ex) {
 			// XXX come up with better handling
 			Activator.logWarn(ex.getMessage());
 		} catch (ParserException ex) {
 			handleParserException(ex);
 		}
 		return null;
 	}
 
 	protected XpandResource loadXpandThroughCache(String qualifiedName) throws IOException, ParserException {
 		if (hasCachedXpand(qualifiedName)) {
 			return cachedXpand.get(qualifiedName);
 		}
 		final XpandResource loaded = doLoadXpandResource(qualifiedName);
 		if (shouldCache()) {
 			cachedXpand.put(qualifiedName, loaded);
 		}
 		return loaded;
 	}
 
 	private XpandResource doLoadXpandResource(String fullyQualifiedName) throws IOException, ParserException {
 		Reader[] rs1 = resolveMultiple(fullyQualifiedName, XpandResource.TEMPLATE_EXTENSION);
 		assert rs1 != null && rs1.length > 0; // exception should be thrown to
 												// indicate issues with resolve
 		XpandResource[] unadvised = loadXpandResources(rs1, fullyQualifiedName);
 		XpandResource[] advices = null;
 		try {
 			String aspectsTemplateName = getAspectsTemplateName(fullyQualifiedName);
 			Reader[] rs2 = resolveMultiple(aspectsTemplateName, XpandResource.TEMPLATE_EXTENSION);
 			// XXX relax resolveMultiple to return empty array and use length==0
 			// here instead of exception
 			advices = loadXpandResources(rs2, aspectsTemplateName);
 		} catch (FileNotFoundException e) {
 		} catch (IOException ex) {
 			// XXX come up with better handling
 			Activator.logWarn(ex.getMessage());
 		} catch (ParserException ex) {
 			handleParserException(ex);
 		}
 		if (advices == null && unadvised.length == 1) {
 			return unadvised[0];
 		}
 		return new CompositeXpandResource(this, unadvised, advices);
 	}
 
 	/**
 	 * XXX: only to simplify tests, should be private or inlined
 	 */
 	protected String getAspectsTemplateName(String fullyQualifiedName) {
 		return ASPECT_PREFIX + fullyQualifiedName;
 	}
 
 	/**
 	 * If the given fully-qualified name is an aspect, transforms it to its
 	 * "host" fully-qualified name. Otherwise, returns the given fully-qualified
 	 * name.
 	 */
 	protected String getNonAspectsTemplateName(String possiblyAspectedFullyQualifiedName) {
 		if (possiblyAspectedFullyQualifiedName == null) {
 			return null;
 		}
 		if (possiblyAspectedFullyQualifiedName.startsWith(ASPECT_PREFIX)) {
 			return possiblyAspectedFullyQualifiedName.substring(ASPECT_PREFIX.length());
 		}
 		return possiblyAspectedFullyQualifiedName;
 	}
 
 	protected abstract void handleParserException(ParserException ex);
 
 	/**
 	 * Returns an array of resolutions, in the order from newest to oldest. This
 	 * is to enable one template to partially override only a subset of parent
 	 * templates.
 	 * 
 	 * @return never return <code>null</code> or an empty array, throw exception
 	 *         instead
 	 * @throws IOException
 	 *             in case resource can't be read. Throw
 	 *             {@link java.io.FileNotFoundException} to indicate resource
 	 *             was not found.
 	 */
 	protected abstract Reader[] resolveMultiple(String fullyQualifiedName, String extension) throws IOException;
 
 	/**
 	 * Readers get closed after parse attempt.
 	 */
 	protected XpandResource[] loadXpandResources(Reader[] readers, String fullyQualifiedName) throws IOException, ParserException {
 		XpandResource[] result = new XpandResource[readers.length];
 		for (int i = 0; i < readers.length; i++) {
 			assert readers[i] != null;
 			try {
 				result[i] = new XpandResourceParser().parse(readers[i], fullyQualifiedName);
 				assert result[i] != null; // this is the contract of parse
 			} finally {
 				try {
 					readers[i].close();
 				} catch (Exception ex) {/* IGNORE */
 				}
 			}
 		}
 		return result;
 	}
 
 	protected abstract boolean shouldCache();
 
 	protected final boolean hasCachedXpand(String fullyQualifiedName) {
 		return shouldCache() && cachedXpand.containsKey(fullyQualifiedName);
 	}
 
 	protected final boolean hasCachedQvt(String fullyQualifiedName) {
 		return shouldCache() && cachedQvt.containsKey(fullyQualifiedName);
 	}
 
 	protected final void forgetCachedXpand(String fullyQualifiedName) {
 		cachedXpand.remove(fullyQualifiedName);
 	}
 
 	protected final void forgetCachedQvt(String fullyQualifiedName) {
 		cachedQvt.remove(fullyQualifiedName);
 	}
 
 	protected final void forgetAll() {
 		cachedXpand.clear();
 		cachedQvt.clear();
 		qvtCompiler = null;
 	}
 
 	protected abstract UnitResolver getQVTUnitResolver();
 
 	private static final String ASPECT_PREFIX = "aspects" + TypeNameUtil.NS_DELIM; //$NON-NLS-1$
 }
