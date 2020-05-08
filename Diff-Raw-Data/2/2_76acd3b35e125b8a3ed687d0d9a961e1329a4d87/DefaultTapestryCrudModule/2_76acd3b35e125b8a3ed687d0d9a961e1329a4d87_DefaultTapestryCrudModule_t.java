 // Copyright 2008 Thiago H. de Paula Figueiredo
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 package br.com.arsmachina.tapestrycrud.module;
 
 import java.util.Set;
 
 import org.apache.tapestry5.PrimaryKeyEncoder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import br.com.arsmachina.module.Module;
 import br.com.arsmachina.tapestrycrud.beanmodel.BeanModelCustomizer;
 import br.com.arsmachina.tapestrycrud.encoder.ActivationContextEncoder;
 import br.com.arsmachina.tapestrycrud.encoder.Encoder;
 import br.com.arsmachina.tapestrycrud.encoder.LabelEncoder;
 import br.com.arsmachina.tapestrycrud.tree.SingleTypeTreeService;
 
 /**
  * Default {@link TapestryCrudModule} implementation.
  * 
  * @author Thiago H. de Paula Figueiredo
  */
 public class DefaultTapestryCrudModule implements TapestryCrudModule {
 
 	final private Module module;
 
 	final private Logger logger = LoggerFactory.getLogger(DefaultTapestryCrudModule.class);
 
 	/**
 	 * Single constructor of this class.
 	 * 
 	 * @param module a {@link Module}. It cannot be null.
 	 */
 	public DefaultTapestryCrudModule(Module module) {
 
 		if (module == null) {
 			throw new IllegalArgumentException("Parameter module cannot be null");
 		}
 
 		this.module = module;
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T> Class<? extends ActivationContextEncoder<T>> getActivationContextEncoderClass(
 			Class<T> entityClass) {
 
 		if (contains(entityClass)) {
 			return getClass(getActivationContextEncoderClassName(entityClass));
 		}
 		else {
 			return null;
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T> Class<? extends Encoder<T, ?>> getEncoderClass(Class<T> entityClass) {
 
 		if (contains(entityClass)) {
 			return getClass(getEncoderClassName(entityClass));
 		}
 		else {
 			return null;
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T> Class<? extends LabelEncoder<T>> getLabelEncoderClass(Class<T> entityClass) {
 
 		if (contains(entityClass)) {
 			return getClass(getLabelEncoderClassName(entityClass));
 		}
 		else {
 			return null;
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T> Class<? extends PrimaryKeyEncoder<?, T>> getPrimaryKeyEncoderClass(
 			Class<T> entityClass) {
 
 		if (contains(entityClass)) {
 			return getClass(getPrimaryKeyEncoderClassName(entityClass));
 		}
 		else {
 			return null;
 		}
 
 	}
 
 	/**
 	 * Returns the fully-qualified name of the activation context encoder for a given entity class.
 	 * 
 	 * @param clasz a {@link Class}. It cannot be null.
 	 * @return a {@link String} or null (if no corresponding one is found).
 	 */
 	protected String getActivationContextEncoderClassName(Class<?> entityClass) {
 
 		return String.format("%s.tapestry.encoder.activationcontext.%sActivationContextEncoder",
 				getRootPackage(), entityClass.getSimpleName());
 
 	}
 
 	/**
 	 * Returns the fully-qualified name of the encoder for a given entity class.
 	 * 
 	 * @param clasz a {@link Class}. It cannot be null.
 	 * @return a {@link String} or null (if no corresponding one is found).
 	 */
 	protected String getEncoderClassName(Class<?> entityClass) {
 
 		return String.format("%s.tapestry.encoder.%sEncoder", getRootPackage(),
 				entityClass.getSimpleName());
 
 	}
 
 	/**
 	 * Returns the fully-qualified name of the label encoder for a given entity class.
 	 * 
 	 * @param clasz a {@link Class}. It cannot be null.
 	 * @return a {@link String} or null (if no corresponding one is found).
 	 */
 	protected String getLabelEncoderClassName(Class<?> entityClass) {
 
 		return String.format("%s.tapestry.encoder.label.%sLabelEncoder", getRootPackage(),
 				entityClass.getSimpleName());
 
 	}
 
 	/**
 	 * Returns the fully-qualified name of the primary key encoder for a given entity class.
 	 * 
 	 * @param clasz a {@link Class}. It cannot be null.
 	 * @return a {@link String}.
 	 */
 	protected String getPrimaryKeyEncoderClassName(Class<?> entityClass) {
 
 		return String.format("%s.tapestry.encoder.primarykey.%sPrimaryKeyEncoder",
 				getRootPackage(), entityClass.getSimpleName());
 
 	}
 
 	/**
 	 * Returns the fully-qualified name of the bean model customizer for a given entity class.
 	 * 
 	 * @param clasz a {@link Class}. It cannot be null.
 	 * @return a {@link String} or null.
 	 */
 	public String getBeanModelCustomizerClassName(Class<?> entityClass) {
 
 		return String.format("%s.beanmodel.%sBeanModelCustomizer", getTapestryPackage(),
 				entityClass.getSimpleName());
 
 	}
 
 	/**
 	 * Returns the fully-qualified name of the tree node factory for a given entity class.
 	 * 
 	 * @param clasz a {@link Class}. It cannot be null.
 	 * @return a {@link String} or null.
 	 */
 	public String getTreeServiceClassName(Class<?> entityClass) {
 
		return String.format("%s.tree.%sTreeService", getTapestryPackage(),
 				entityClass.getSimpleName());
 
 	}
 
 	public String getEditPageClassName(Class<?> entityClass) {
 
 		final String className = entityClass.getSimpleName();
 
 		return String.format("%s.pages.%s.%s%s", getTapestryPackage(),
 				className.toLowerCase(), getEditPagePrefix(), className);
 
 	}
 
 	public String getListPageClassName(Class<?> entityClass) {
 
 		final String className = entityClass.getSimpleName();
 
 		return String.format("%s.pages.%s.%s%s", getTapestryPackage(),
 				className.toLowerCase(), getListPagePrefix(), className);
 
 	}
 
 	public String getViewPageClassName(Class<?> entityClass) {
 
 		final String className = entityClass.getSimpleName();
 
 		return String.format("%s.pages.%s.%s%s", getTapestryPackage(), 
 				className.toLowerCase(), getViewPagePrefix(), className);
 
 	}
 
 	@Override
 	public String toString() {
 		return String.format("TapestryCrudModule %s (%s)", getId(), getRootPackage());
 	}
 
 	/**
 	 * Returns the name of the package where the Tapestry-related packages are located (i.e. under
 	 * which the <code>pages</code> component is located). This implementation returns
 	 * <code>[rootPackage].tapestry</code>.
 	 * 
 	 * @return a {@link String}.
 	 */
 	public String getTapestryPackage() {
 		return getRootPackage() + ".tapestry";
 	}
 
 	private String getModulePath() {
 
 		String path = getId();
 
 		if (path != null) {
 			path = path + "/";
 		}
 		else {
 			path = "";
 		}
 
 		return path;
 
 	}
 
 	public boolean contains(Class<?> entityClass) {
 		return module.contains(entityClass);
 	}
 
 	public Set<Class<?>> getEntityClasses() {
 		return module.getEntityClasses();
 	}
 
 	public String getId() {
 		return module.getId();
 	}
 
 	private String getRootPackage() {
 		return module.getRootPackage();
 	}
 
 	/**
 	 * Returns <code>getClass(name, false)</code>.
 	 * 
 	 * @param name a {@link String}. It cannot be null.
 	 * @return a {@link Class}.
 	 */
 	@SuppressWarnings("unchecked")
 	protected Class getClass(String name) {
 		return getClass(name, true);
 	}
 
 	/**
 	 * Returns the {@link Class} instance given a class name.
 	 * 
 	 * @param name a {@link String}. It cannot be null.
 	 * @param lenient a <code>boolean</code>. If <code>false</code>, it will throw an exception if
 	 * the class is not found. Otherwise, this method will return null.
 	 * @return a {@link Class} or null.
 	 */
 	@SuppressWarnings("unchecked")
 	protected Class getClass(String name, boolean lenient) {
 
 		Class<?> clasz = null;
 
 		try {
 			clasz = Thread.currentThread().getContextClassLoader().loadClass(name);
 		}
 		catch (ClassNotFoundException e) {
 
 			if (lenient == false) {
 				throw new RuntimeException(e);
 			}
 			else {
 
 				if (logger.isDebugEnabled()) {
 					logger.debug("Class not found: " + name);
 				}
 
 			}
 
 		}
 
 		return clasz;
 
 	}
 
 	public String getEditPageURL(Class<?> entityClass) {
 		return getURL(entityClass, getEditPagePrefix());
 	}
 
 	public String getListPageURL(Class<?> entityClass) {
 		return getURL(entityClass, getListPagePrefix());
 	}
 
 	public String getViewPageURL(Class<?> entityClass) {
 		return getURL(entityClass, getViewPagePrefix());
 	}
 
 	/**
 	 * Returns the URL for a given entity class and page name prefix.
 	 * 
 	 * @param entityClass a {@link Class} instance.
 	 * @param prefix a {@link String}.
 	 * @return a {@link String}.
 	 */
 	private String getURL(Class<?> entityClass, final String prefix) {
 
 		final String moduleSubpackage = getModulePath();
 		final String className = entityClass.getSimpleName().toLowerCase();
 		return String.format("%s%s/%s", moduleSubpackage, className, prefix);
 
 	}
 
 	public Class<?> getEditPageClass(Class<?> entityClass) {
 		return getClass(getEditPageClassName(entityClass));
 	}
 
 	public Class<?> getListPageClass(Class<?> entityClass) {
 		return getClass(getListPageClassName(entityClass));
 	}
 
 	public Class<?> getViewPageClass(Class<?> entityClass) {
 		return getClass(getViewPageClassName(entityClass));
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T> Class<? extends BeanModelCustomizer<T>> getBeanModelCustomizerClass(
 			Class<T> entityClass) {
 
 		return getClass(getBeanModelCustomizerClassName(entityClass));
 
 	}
 
 	@SuppressWarnings("unchecked")
 	public <T> Class<? extends SingleTypeTreeService<T>> getTreeServiceClass(
 			Class<T> entityClass) {
 
 		return getClass(getTreeServiceClassName(entityClass));
 
 	}
 
 	/**
 	 * Returns the value of the <code>editPagePrefix</code> property.
 	 * 
 	 * @return a {@link String}.
 	 */
 	public String getEditPagePrefix() {
 		return "Edit";
 	}
 
 	/**
 	 * Returns the value of the <code>listPagePrefix</code> property.
 	 * 
 	 * @return a {@link String}.
 	 */
 	public String getListPagePrefix() {
 		return "List";
 	}
 
 	/**
 	 * Returns the value of the <code>viewPagePrefix</code> property.
 	 * 
 	 * @return a {@link String}.
 	 */
 	public String getViewPagePrefix() {
 		return "View";
 	}
 
 }
