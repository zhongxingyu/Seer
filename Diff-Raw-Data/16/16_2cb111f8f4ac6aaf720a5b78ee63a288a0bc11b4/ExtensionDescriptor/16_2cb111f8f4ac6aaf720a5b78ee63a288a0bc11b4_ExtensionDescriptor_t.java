 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.core.injector.extension;
 
 import org.osgi.framework.BundleContext;
 
 import org.eclipse.core.runtime.Assert;
 
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.internal.core.injector.extension.ExtensionMapper;
 import org.eclipse.riena.internal.core.injector.extension.ExtensionPointId;
 
 /**
  * ExtensionDescriptor and ExtensionInjector simplify locating configuration
  * (extensions) and injects them into a target object.
  * <p>
  * To do so the ExtensionInjector tracks the extension registry for changes of
  * appearing and disappearing extensions and injects them into the target. A
  * target object defines a named and typed bind (update) method. The
  * ExtensionInjector calls the update method when the specified extension/point
  * was registered or modified.
  * <p>
  * The type of the update method can be either of type array of <i>interface
  * type</i> or just <i>interface type</i>. The <i>interface type</i> is just a
  * simple java interface with <i>getters</i> where their name corresponds to
  * attribute names or element names for nested extensions of an extension. For a
  * detailed description of the interface/extension mapping see
  * {@link ExtensionMapper}.
  * <p>
  * The extension injector starts tracking the specified extension with
  * {@link #andStart(BundleContext)}. It stops tracking with {@link #stop()}.
  * <p>
  * Configuration modifications as defined by <code>ConfigurationPlugin</code>
  * will also be performed.
  * <p>
  * The ExtensionDescriptor and ExtensionInjector are implemented as a fluent
  * interface allowing constructs like:
  * <ul>
  * <li>Inject.extension("id1").into(target).andStart(context)</li>
  * <li>
  * Inject.extension("id2").useType(interface).into(target).update("configure").
  * andStart(context)</li>
  * <li>Inject.extension("id3").expectExactly(1).into(target).andStart(context)</li>
  * <li>Inject.extension().into(target).andStart(context)</li>
  * <li>..</li>
  * </ul>
  * If the id is omitted, the extension injector will try to retrieve the
  * extension point id by
  * <ul>
  * <li>inspecting the {@code ExtensionInterface} annotation for the id</li>
  * <li>inspecting the extension interface for a String field named {@code ID}</li>
  * </ul>
  * <p>
  * If the id is simple, i.e. does not contain a dot <b>.</b> than it will prefix
  * the simple id with the symbolic name of the bundle which loaded the extension
  * interface.
  * <p>
  * This fluent interface makes a few assumptions (defaults) that makes writing
  * extension injectors short and expressive , e.g. the first example in the
  * above list means: try to retrieve <i>interface type</i> by reflection, expect
  * zero to unbound occurrences of extensions and the update method name is
  * "update".
  * <p>
  * The expected cardinality of extensions (min/max occurrences) can be specified
  * with <code>expectingMinMax()</code> or with <code>expectingExactly()</code>.
  */
 public class ExtensionDescriptor {
 
 	private final ExtensionPointId extensionPointId;
 	private boolean homogeneous = true;
 	private Class<?> interfaceType;
 	private int minOccurrences = 0;
 	private int maxOccurrences = UNBOUNDED;
 
 	public static final int UNBOUNDED = Integer.MAX_VALUE;
 
 	/**
 	 * Create an extension descriptor. Attempts are made to retrieve the
 	 * required extension point id from annotations, e.g.
 	 * {@code ExtensionInterface} or {@code InjectExtension}
 	 */
 	public ExtensionDescriptor() {
 		this.extensionPointId = new ExtensionPointId();
 	}
 
 	/**
 	 * Create an extension descriptor for the given extension point id.
 	 * 
 	 * @param extensionPointId
 	 */
 	public ExtensionDescriptor(final String extensionPointId) {
 		Assert.isLegal(StringUtils.isGiven(extensionPointId),
 				"The extension id must be given, i.e. not null and not empty."); //$NON-NLS-1$
 		this.extensionPointId = new ExtensionPointId(extensionPointId);
 	}
 
 	/**
 	 * Define the interface type used as a bean for injecting. <br>
 	 * If not defined the extension injector tries to figure it out by
 	 * reflection and the name of the update method.
 	 * 
 	 * @param interfaceType
 	 * @return itself
 	 */
 	public ExtensionDescriptor useType(final Class<?> interfaceType) {
 		Assert.isNotNull(interfaceType, "Interface type must not be null."); //$NON-NLS-1$
 		Assert.isTrue(interfaceType.isInterface(), "Interface type must be an interface."); //$NON-NLS-1$
 		Assert.isTrue(this.interfaceType == null, "Interface type has already been set."); //$NON-NLS-1$
 		this.interfaceType = interfaceType;
 		return this;
 	}
 
 	/**
 	 * Set that the extension is heterogeneous.<br>
 	 * A homogeneous extension (that is the default) is an extension that only
 	 * has sub-elements of the same element type. A heterogeneous extension is
 	 * an extension that has sub-elements of various types.
 	 * 
 	 * @return itself
 	 */
 	public ExtensionDescriptor heterogeneous() {
 		homogeneous = false;
 		return this;
 	}
 
 	/**
 	 * Inject the extensions into the specified target object.
 	 * 
 	 * @param target
 	 * @return itself
 	 */
 	public ExtensionInjector into(final Object target) {
 		Assert.isNotNull(target, "The target must not be null."); //$NON-NLS-1$
 		return new ExtensionInjector(this, target);
 	}
 
 	/**
 	 * Defines the minimum and the maximum occurrences of extensions allowed for
 	 * the extension point. These values should correspond to the definitions
 	 * within the extension point schema.<br>
 	 * Future implementations might retrieve these values from the schema.
 	 * 
 	 * @param min
 	 * @param max
 	 * @return itself
 	 */
 	public ExtensionDescriptor expectingMinMax(final int min, final int max) {
 		Assert.isLegal(max >= min, "min must not be greater than max."); //$NON-NLS-1$
 		Assert.isLegal(min >= 0, "min must be greater or equal than zero."); //$NON-NLS-1$
 		Assert.isLegal(max > 0, "max must be greater than zero."); //$NON-NLS-1$
 		this.minOccurrences = min;
 		this.maxOccurrences = max;
 		return this;
 	}
 
 	/**
 	 * Defines the expected number of extensions for the extension point.
 	 * 
 	 * @param exactly
 	 *            expected number of occurrences
 	 * @return itself
 	 */
 	public ExtensionDescriptor expectingExactly(final int exactly) {
 		return expectingMinMax(exactly, exactly);
 	}
 
 	/**
 	 * @return the extension point id
 	 */
 	public ExtensionPointId getExtensionPointId() {
 		return extensionPointId;
 	}
 
 	/**
 	 * @return the interfaceType
 	 */
 	public Class<?> getInterfaceType() {
 		return interfaceType;
 	}
 
 	/**
 	 * @return the minOccurrences
 	 */
 	int getMinOccurrences() {
 		return minOccurrences;
 	}
 
 	/**
 	 * @return the maxOccurrences
 	 * 
 	 * @since 2.0
 	 */
 	int getMaxOccurrences() {
 		return maxOccurrences;
 	}
 
 	/**
 	 * @return the form
 	 * 
 	 * @since 2.0
 	 */
 	public boolean isHomogeneous() {
 		return homogeneous;
 	}
 
 	/**
 	 * @return
 	 */
 	public boolean requiresArrayUpdateMethod() {
 		return minOccurrences > 1 || maxOccurrences > 1;
 	}
 }
