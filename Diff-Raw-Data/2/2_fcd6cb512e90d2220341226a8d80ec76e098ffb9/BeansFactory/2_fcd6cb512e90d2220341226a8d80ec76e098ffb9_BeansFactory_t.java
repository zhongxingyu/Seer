 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.gda.beans;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.ObjectStreamClass;
 import java.io.StringWriter;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.List;
 import java.util.Locale;
 
 import org.apache.commons.beanutils.BeanMap;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.gda.util.beans.xml.XMLHelpers;
 
 /**
  * Holds a list of the IRichBeans used on the beamline. Has a series of utilities for interacting with the beans and the
  * xml files in which they are persisted.
  * <p>
  * The list of classes is either populated through a Spring instantiated instance of this class (server-side) or via the
  * uk.ac.common.beans.factory extension point (RCP client-side).
  */
 public class BeansFactory {
 
 	private static final Logger logger = LoggerFactory.getLogger(BeansFactory.class);
 
 	private static Class<? extends IRichBean>[] CLASSES;
 
 	/**
 	 * Check that CLASSES is init and throw an exception if it hasn't been yet. Using CLASSES in this file would
 	 * generally through a {@link NullPointerException} anyway, this is just a more informative check that is less
 	 * likely to be caught by over aggressive try/catchs that exist in this file.
 	 * 
 	 * @throws NullPointerException
 	 *             thrown if CLASSES has not been initialised
 	 */
 	private static void checkInit() throws NullPointerException {
 		if (CLASSES == null)
 			throw new NullPointerException(
 					"BeansFactory.CLASSES is null, therefore BeansFactory has not been initialized properly");
 	}
 
 	/**
 	 * Retrieves the bean given the file name, or the bean itself.
 	 * <p>
 	 * For use in command-line tools where the user can supply filenames or beans interchangeably in commands.
 	 * 
 	 * @param dir
 	 * @param beanOrFile
 	 * @return ExafsBeansFactory.getBean(new File(beanFile));
 	 * @throws Exception
 	 */
 	public static IRichBean getBeanObject(final String dir, final Object beanOrFile) throws Exception {
 		checkInit();
 
 		for (int i = 0; i < CLASSES.length; i++) {
 			if (CLASSES[i].isInstance(beanOrFile))
 				return (IRichBean) beanOrFile;
 		}
 
 		String path;
 		if(dir != null){
 			path = dir + beanOrFile;
 		} else {
 			path = beanOrFile.toString();
 		}
 		if (!path.endsWith(".xml"))
 			path = path + ".xml";
 		return BeansFactory.getBean(new File(path));
 
 	}
 
 	/**
 	 * Can inject classes from spring or with static method. The classes are needed to define which XML files have GDA
 	 * bean files with them.
 	 */
 	public BeansFactory() {
 	}
 
 	/**
 	 * Fast way to determine if XML file is a bean in the system which can be read by castor and control things on the
 	 * server.
 	 * 
 	 * @param beanFile
 	 * @return true if bean file.
 	 * @throws Exception
 	 */
 	public static boolean isBean(File beanFile) throws Exception {
 		checkInit();
 		for (int i = 0; i < CLASSES.length; i++) {
 			if (BeansFactory.isBean(beanFile, CLASSES[i]))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Will get a bean of the appropriate type from any an exafs bean file. One of ScanParameters, XanesScanParameters,
 	 * SampleParameters, DetectorParameters, OutputParameters is returned.
 	 * 
 	 * @param beanFile
 	 * @return the bean
 	 * @throws Exception
 	 */
 	public static IRichBean getBean(final File beanFile) throws Exception {
 		checkInit();
 
 		for (int i = 0; i < CLASSES.length; i++) {
 			if (BeansFactory.isBean(beanFile, CLASSES[i])) {
 				return (IRichBean) XMLHelpers.readBean(beanFile, CLASSES[i]);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Call to get a single string from a bean.
 	 * 
 	 * @param beanOrPath
 	 *            - either the bean itself or a String path to the bean.
 	 * @return string
 	 * @throws Exception
 	 */
 	public static String getXMLString(final Object beanOrPath) throws Exception {
 		Object bean = beanOrPath;
 		if (beanOrPath instanceof String) {
 			bean = getBean(new File((String) beanOrPath));
 		}
 
 		URL mapping = null;
 		final Field[] fa = bean.getClass().getFields();
 		for (int i = 0; i < fa.length; i++) {
 			if (fa[i].getName().equalsIgnoreCase("mappingurl")) {
 				mapping = (URL) fa[i].get(null);
 			}
 		}
 
 		final StringWriter writer = new StringWriter();
 		try {
 			XMLHelpers.writeToXML(mapping, bean, writer);
 		} catch (Exception e) {
 			logger.error("Exception writing bean " + beanOrPath + " to xml",e);
 		} finally {
 			writer.close();
 		}
 		return writer.toString();
 	}
 
 	/**
 	 * Save a bean to a file.
 	 * 
 	 * @param templatePath
 	 * @param editingBean
 	 * @throws Exception
 	 */
 	public static void saveBean(File templatePath, Object editingBean) throws Exception {
 		URL mapping = null;
 		final Field[] fa = editingBean.getClass().getFields();
 		for (int i = 0; i < fa.length; i++) {
 			if (fa[i].getName().equalsIgnoreCase("mappingurl")) {
 				mapping = (URL) fa[i].get(null);
 				break;
 			}
 		}
 		XMLHelpers.writeToXML(mapping, editingBean, templatePath);
 	}
 
 	/**
 	 * Find the first filename of an xml file persisting a bean of the given class
 	 * 
 	 * @param folder
 	 * @param clazz
 	 * @return String - filename
 	 * @throws Exception
 	 */
 	public static String getFirstFileName(File folder, Class<? extends IRichBean> clazz) throws Exception {
 		final File[] fa = folder.listFiles();
 		for (int i = 0; i < fa.length; i++) {
 			if (BeansFactory.isBean(fa[i], clazz)) {
 				return fa[i].getName();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Checks if the XML file is a Castor persistence file of a bean in the given list
 	 * 
 	 * @param beanFile
 	 * @return true if file is a saved version of this bean
 	 * @throws Exception
 	 */
 	public static boolean isBean(final File beanFile, final Class<? extends IRichBean> beanClass) throws Exception {
 		final BufferedReader reader = new BufferedReader(new FileReader(beanFile));
 		return isBean(reader, beanClass);
 	}
 
 	public static boolean isBean(final InputStream bean, final Class<? extends IRichBean> beanClass) throws Exception {
 		final BufferedReader reader = new BufferedReader(new InputStreamReader(bean, "UTF-8"));
 		return isBean(reader, beanClass);
 	}
 
 	private static boolean isBean(BufferedReader reader, Class<? extends IRichBean> beanClass) throws Exception {
 		try {
 			@SuppressWarnings("unused")
 			final String titleLine = reader.readLine(); // unused.
 			final String tagLine = reader.readLine();
 			if (tagLine == null)
 				return false;
 			final String tagName = beanClass.getName().substring(beanClass.getName().lastIndexOf(".") + 1);
 			if (tagName == null)
 				return false;
 			if (tagLine.trim().equalsIgnoreCase("<" + tagName + ">")
 					|| tagLine.trim().equalsIgnoreCase("<" + tagName + "/>")) {
 				return true;
 			}
 			return false;
 		} finally {
 			reader.close();
 		}
 	}
 
 	/**
 	 * Testing only TODO - Move to unit test
 	 * 
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void main(String[] args) throws Exception {
 		final File testDir = new File(
 				"/home/nv23/workspaces/gdatrunk_sep09ws/plugins/uk.ac.gda.client.microfocus/test/uk/ac/gda/client/microfocus/util/TestFiles/");
 		final File[] fa = testDir.listFiles();
 		for (int i = 0; i < fa.length; i++) {
 			if (fa[i].isDirectory())
 				continue;
 			System.out.println(fa[i]);
 			final Object bean = BeansFactory.getBean(fa[i]);
 			if (bean != null) {
 				System.out.println(bean.getClass());
 			} else {
 				System.out.println(bean);
 			}
 		}
 	}
 
 	public static Class<? extends IRichBean>[] getClasses() {
 		checkInit();
 
 		return CLASSES;
 	}
 
 	public static void setClasses(Class<? extends IRichBean>[] cLASSES) {
 		if (cLASSES == null)
 			throw new NullPointerException("cLASSES must be-non null to initialzied BeansFactory");
 		CLASSES = cLASSES;
 	}
 
 	private List<String> classList;
 
 	public List<String> getClassList() {
 		return classList;
 	}
 
 	@SuppressWarnings("unchecked")
 	public void setClassList(List<String> classList) {
 		this.classList = classList;
 
 		int i = 0;
 		final Class<? extends IRichBean>[] classes = (Class<? extends IRichBean>[]) new Class<?>[classList.size()];
 		for (String clazz : classList) {
 			try {
 				classes[i] = (Class<? extends IRichBean>) Class.forName(clazz);
 			} catch (ClassNotFoundException e) {
				logger.error("ClassNotFoundException, cannot load class " + clazz);
 			}
 			++i;
 		}
 		BeansFactory.setClasses(classes);
 	}
 
 	/**
 	 * Changes a value on the given bean using reflection
 	 * 
 	 * @param bean
 	 * @param fieldName
 	 * @param value
 	 * @throws Exception
 	 */
 	public static void setBeanValue(final Object bean, final String fieldName, final Object value) throws Exception {
 		final String setterName = getSetterName(fieldName);
 		try {
 			final Method method;
 			if (value != null) {
 				method = bean.getClass().getMethod(setterName, value.getClass());
 			} else {
 				method = bean.getClass().getMethod(setterName, Object.class);
 			}
 			method.invoke(bean, value);
 		} catch (NoSuchMethodException ne) {
 			// Happens when UI and bean types are not the same, for instance Text editing a double field,
 			// or label showing a double field.
 			final BeanMap properties = new BeanMap(bean);
 			properties.put(fieldName, value);
 		}
 	}
 
 	/**
 	 * Method gets value out of bean using reflection.
 	 * 
 	 * @param bean
 	 * @param fieldName
 	 * @return value
 	 * @throws Exception
 	 */
 	public static Object getBeanValue(final Object bean, final String fieldName) throws Exception {
 		final String getterName = getGetterName(fieldName);
 		final Method method = bean.getClass().getMethod(getterName);
 		return method.invoke(bean);
 	}
 
 	public static String getFieldWithUpperCaseFirstLetter(final String fieldName) {
 		return fieldName.substring(0, 1).toUpperCase(Locale.US) + fieldName.substring(1);
 	}
 
 	private static String getName(final String prefix, final String fieldName) {
 		return prefix + BeansFactory.getFieldWithUpperCaseFirstLetter(fieldName);
 	}
 
 	/**
 	 * There must be a smarter way of doing this i.e. a JDK method I cannot find. However it is one line of Java so
 	 * after spending some time looking have coded self.
 	 * 
 	 * @param fieldName
 	 * @return String
 	 */
 	public static String getSetterName(final String fieldName) {
 		if (fieldName == null)
 			return null;
 		return BeansFactory.getName("set", fieldName);
 	}
 
 	/**
 	 * There must be a smarter way of doing this i.e. a JDK method I cannot find. However it is one line of Java so
 	 * after spending some time looking have coded self.
 	 * 
 	 * @param fieldName
 	 * @return String
 	 */
 	public static String getGetterName(final String fieldName) {
 		if (fieldName == null)
 			return null;
 		return BeansFactory.getName("get", fieldName);
 	}
 
 	/**
 	 * Can be used to test if a given class is a class defined by the extension point.
 	 * 
 	 * @param clazz
 	 * @return true if class.
 	 */
 	public static boolean isClass(Class<? extends IRichBean> clazz) {
 		checkInit();
 
 		for (int i = 0; i < CLASSES.length; i++) {
 			if (CLASSES[i].equals(clazz))
 				return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Deep copy using serialization. All objects in the graph must serialize to use this method or an exception will be
 	 * thrown.
 	 * 
 	 * @param fromBean
 	 * @return deeply cloned bean
 	 */
 	public static <T> T deepClone(final T fromBean) throws Exception {
 		return deepClone(fromBean, fromBean.getClass().getClassLoader());
 	}
 
 	/**
 	 * Creates a clone of any serializable object. Collections and arrays may be cloned if the entries are serializable.
 	 * Caution super class members are not cloned if a super class is not serializable.
 	 */
 	public static <T> T deepClone(T toClone, final ClassLoader classLoader) throws Exception {
 		if (null == toClone)
 			return null;
 
 		ByteArrayOutputStream bOut = new ByteArrayOutputStream();
 		ObjectOutputStream oOut = new ObjectOutputStream(bOut);
 		oOut.writeObject(toClone);
 		oOut.close();
 		ByteArrayInputStream bIn = new ByteArrayInputStream(bOut.toByteArray());
 		bOut.close();
 		ObjectInputStream oIn = new ObjectInputStream(bIn) {
 			/**
 			 * What we are saying with this is that either the class loader or any of the beans added using extension
 			 * points classloaders should be able to find the class.
 			 */
 			@Override
 			protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
 				try {
 					return Class.forName(desc.getName(), false, classLoader);
 				} catch (Exception ne) {
 					for (int i = 0; i < CLASSES.length; i++) {
 						try {
 							return CLASSES[i].getClassLoader().loadClass(desc.getName());
 						} catch (Exception e) {
 							continue;
 						}
 					}
 				}
 				return null;
 			}
 		};
 		bIn.close();
 		// the whole idea is to create a clone, therefore the readObject must
 		// be the same type in the toClone, hence of T
 		@SuppressWarnings("unchecked")
 		T copy = (T) oIn.readObject();
 		oIn.close();
 
 		return copy;
 	}
 
 }
