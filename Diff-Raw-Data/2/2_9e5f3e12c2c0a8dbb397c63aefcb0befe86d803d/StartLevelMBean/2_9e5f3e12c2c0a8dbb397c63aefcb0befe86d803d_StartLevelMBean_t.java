 package com.buschmais.osgi.maexo.mbeans.osgi.core;
 
 import javax.management.AttributeNotFoundException;
 import javax.management.InstanceNotFoundException;
 import javax.management.MBeanException;
 import javax.management.ObjectName;
 import javax.management.ReflectionException;
 
 public interface StartLevelMBean {
 
 	/**
 	 * Return the assigned start level value for the specified Bundle.
 	 * 
 	 * @see org.osgi.service.startlevel.StartLevel#getBundleStartLevel(Bundle)
 	 * 
 	 * @param objectName
 	 *            the object name of the bundle
 	 * @return The start level value of the specified Bundle.
 	 * @throws AttributeNotFoundException
 	 * @throws InstanceNotFoundException
 	 * @throws MBeanException
 	 * @throws ReflectionException
 	 */
 	public Integer getBundleStartLevel(ObjectName objectName)
 			throws AttributeNotFoundException, InstanceNotFoundException,
 			MBeanException, ReflectionException;
 
 	/**
 	 * Return the assigned start level value for the specified Bundle.
 	 * 
 	 * @see org.osgi.service.startlevel.StartLevel#getBundleStartLevel(Bundle)
 	 * 
 	 * @param id
 	 *            the id of the bundle
 	 * @return The start level value of the specified Bundle.
 	 */
 	public Integer getBundleStartLevel(Long id);
 
 	/**
 	 * Return the initial start level value that is assigned to a Bundle when it
 	 * is first installed.
 	 * 
 	 * @see org.osgi.service.startlevel.StartLevel#getInitialBundleStartLevel()
 	 * 
 	 * @return The initial start level value for Bundles.
 	 */
 	public Integer getInitialBundleStartLevel();
 
 	/**
 	 * Return the active start level value of the Framework. If the Framework is
 	 * in the process of changing the start level this method must return the
 	 * active start level if this differs from the requested start level.
 	 * 
 	 * @see org.osgi.service.startlevel.StartLevel#getStartLevel()
 	 * 
 	 * @return The active start level value of the Framework.
 	 */
 	public Integer getStartLevel();
 
 	/**
 	 * Return the persistent state of the specified bundle.
 	 * 
 	 * @see org.osgi.service.startlevel.StartLevel#isBundlePersistentlyStarted(Bundle)
 	 * 
 	 * @param objectName
 	 *            the object name of the bundle
 	 * @return true if the bundle is persistently marked to be started, false if
 	 *         the bundle is not persistently marked to be started.
 	 * @throws AttributeNotFoundException
 	 * @throws InstanceNotFoundException
 	 * @throws MBeanException
 	 * @throws ReflectionException
 	 */
 	public Boolean isBundlePersistentlyStarted(ObjectName objectName)
 			throws AttributeNotFoundException, InstanceNotFoundException,
 			MBeanException, ReflectionException;
 
 	/**
 	 * Return the persistent state of the specified bundle.
 	 * 
 	 * @see org.osgi.service.startlevel.StartLevel#isBundlePersistentlyStarted(Bundle)
 	 * 
 	 * @param id
 	 *            the id of the bundle
 	 * @return true if the bundle is persistently marked to be started, false if
 	 *         the bundle is not persistently marked to be started.
 	 */
 	public Boolean isBundlePersistentlyStarted(Long id);
 
 	/**
 	 * Assign a start level value to the specified Bundle.
 	 * 
 	 * @see org.osgi.service.startlevel.StartLevel#setBundleStartLevel(Bundle,
 	 *      int)
 	 * 
 	 * @param objectName
 	 *            the object name of the bundle
 	 * @param startLevel
 	 *            The new start level for the specified Bundle.
 	 * @throws AttributeNotFoundException
 	 * @throws InstanceNotFoundException
 	 * @throws MBeanException
 	 * @throws ReflectionException
 	 */
 	public void setBundleStartLevel(ObjectName objectName,
 			Integer startLevel) throws AttributeNotFoundException,
 			InstanceNotFoundException, MBeanException, ReflectionException;
 
 	/**
 	 * Assign a start level value to the specified Bundle.
 	 * 
 	 * @see org.osgi.service.startlevel.StartLevel#setBundleStartLevel(Bundle,
 	 *      int)
 	 * 
 	 * @param id
 	 *            the id name of the bundle
 	 * @param startLevel
 	 *            The new start level for the specified Bundle.
 	 */
 	public void setBundleStartLevel(Long id, Integer startLevel);
 
 	/**
 	 * Set the initial start level value that is assigned to a Bundle when it is
 	 * first installed.
 	 * 
 	 * @see org.osgi.service.startlevel.StartLevel#setInitialBundleStartLevel(int)
 	 * 
 	 * @param startLevel
 	 *            The initial start level for newly installed bundles.
 	 */
 	public void setInitialBundleStartLevel(Integer startLevel);
 
 	/**
 	 * Modify the active start level of the Framework.
 	 * 
 	 * @see org.osgi.service.startlevel.StartLevel#setStartLevel(int)
 	 * 
 	 * @param startLevel
 	 *            The requested start level for the Framework.
 	 */
 	public void setStartLevel(Integer startLevel);
 
 }
