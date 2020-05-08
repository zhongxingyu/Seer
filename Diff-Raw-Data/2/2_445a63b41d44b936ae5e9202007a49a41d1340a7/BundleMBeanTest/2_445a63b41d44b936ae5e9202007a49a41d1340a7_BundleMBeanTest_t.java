 package com.buschmais.osgi.maexo.test.mbeans.osgi.core;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.management.MBeanServer;
 import javax.management.MBeanServerConnection;
 import javax.management.MBeanServerInvocationHandler;
 import javax.management.ObjectName;
 import javax.management.openmbean.TabularData;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleEvent;
 import org.osgi.framework.BundleListener;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 
 import com.buschmais.osgi.maexo.framework.commons.mbean.objectname.ObjectNameFactoryHelper;
 import com.buschmais.osgi.maexo.mbeans.osgi.core.BundleMBean;
 import com.buschmais.osgi.maexo.test.Constants;
 import com.buschmais.osgi.maexo.test.MaexoTests;
 
 /**
  * @see MaexoTests
  */
 public class BundleMBeanTest extends MaexoTests implements BundleListener {
 
 	/** Symbolic name for the testbundle. */
 	private static final String TESTBUNDLE_SYMBOLIC_NAME = "com.buschmais.osgi.maexo.test.testbundle";
 
 	/** Set containing all triggered BundleEvents. */
 	private Set<Integer> bundleEvents;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void onSetUp() throws Exception {
 		super.onSetUp();
 		bundleEvents = new HashSet<Integer>();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected String[] getTestBundlesNames() {
 		return new String[] { Constants.ARTIFACT_SWITCHBOARD,
 				Constants.ARTIFACT_PLATFORM_MBEAN_SERVER,
 				Constants.ARTIFACT_COMMONS_MBEAN,
 				Constants.ARTIFACT_OSGI_CORE_MBEAN,
 				Constants.ARTIFACT_TESTBUNDLE, Constants.ARTIFACT_EASYMOCK };
 	}
 
 	/**
 	 * Returns a TestBundle from OSGI container for testing of general Bundle
 	 * functionality.
 	 * 
 	 * @return the bundle
 	 * @throws InvalidSyntaxException
 	 *             if no PackageAdmin could be found
 	 */
 	private Bundle getTestBundle() throws InvalidSyntaxException {
 		ServiceReference[] serviceReferences = this.bundleContext
 				.getServiceReferences(
 						org.osgi.service.packageadmin.PackageAdmin.class
 								.getName(), null);
 		assertTrue(serviceReferences.length == 1);
 		final org.osgi.service.packageadmin.PackageAdmin packageAdmin = (org.osgi.service.packageadmin.PackageAdmin) bundleContext
 				.getService(serviceReferences[0]);
 		Bundle[] bundles = packageAdmin.getBundles(TESTBUNDLE_SYMBOLIC_NAME,
 				"0.0.0");
 		assertTrue(bundles.length == 1);
 		Bundle bundle = bundles[0];
 		return bundle;
 	}
 
 	/**
 	 * Returns a BundleMBean for the given Bundle.
 	 * 
 	 * @param bundle
 	 *            the Bundle
 	 * @return the BundleMBean
 	 */
 	private BundleMBean getTestBundleMBean(Bundle bundle) {
 		// get corresponding BundleMBean
 		ObjectNameFactoryHelper objectNameFactoryHelper = new ObjectNameFactoryHelper(
 				this.bundleContext);
 		ObjectName objectName = objectNameFactoryHelper.getObjectName(bundle,
 				Bundle.class);
 		ServiceReference serviceReference = super.bundleContext
 				.getServiceReference(MBeanServer.class.getName());
 		MBeanServerConnection mbeanServer = (MBeanServer) super.bundleContext
 				.getService(serviceReference);
 		final BundleMBean bundleMBean = (BundleMBean) MBeanServerInvocationHandler
 				.newProxyInstance(mbeanServer, objectName, BundleMBean.class,
 						false);
 		return bundleMBean;
 	}
 
 	/**
 	 * Tests if all Bundles are registered on MBeanServer.
 	 */
 	public void test_allBundlesRegisteredAsMBeans() throws IOException {
 		ObjectNameFactoryHelper objectNameFactoryHelper = new ObjectNameFactoryHelper(
 				this.bundleContext);
 		Bundle[] bundles = this.bundleContext.getBundles();
 		ServiceReference serviceReference = super.bundleContext
 				.getServiceReference(MBeanServer.class.getName());
 		try {
 			MBeanServerConnection mbeanServer = (MBeanServer) super.bundleContext
 					.getService(serviceReference);
 			for (Bundle bundle : bundles) {
 				ObjectName objectName = objectNameFactoryHelper.getObjectName(
 						bundle, Bundle.class);
 				assertTrue(String.format("BundleMBean %s is not registered.",
 						objectName), mbeanServer.isRegistered(objectName));
 			}
 		} finally {
 			super.bundleContext.ungetService(serviceReference);
 		}
 	}
 
 	/**
 	 * Tests Bundle attributes.
 	 * 
 	 * @throws Exception
 	 *             on error
 	 */
 	public void test_testBundleAttributes() throws Exception {
 		Bundle bundle = getTestBundle();
 		BundleMBean bundleMBean = getTestBundleMBean(bundle);
 
 		assertTrue(bundle.getBundleId() == bundleMBean.getBundleId()
 				.longValue());
 		assertTrue(bundle.getLastModified() == bundleMBean.getLastModified()
 				.longValue());
 		assertEquals(bundle.getLocation(), bundleMBean.getLocation());
 	}
 
 	/**
 	 * Tests method <code>getServiceinUse()</code>.
 	 * 
 	 * @throws Exception
 	 *             on error
 	 */
 	public void test_getServicesInUse() throws Exception {
 		Bundle bundle = getTestBundle();
 		BundleMBean bundleMBean = getTestBundleMBean(bundle);
 
 		ServiceReference[] bundleServiceReferences = bundle.getServicesInUse();
 		ObjectName[] bundleMBeanServicesInUse = bundleMBean.getServicesInUse();
 		assertTrue(bundleServiceReferences.length == bundleMBeanServicesInUse.length);
 		for (int i = 0; i < bundleServiceReferences.length; i++) {
 			ServiceReference reference = bundleServiceReferences[i];
 			ObjectNameFactoryHelper objectNameFactoryHelper = new ObjectNameFactoryHelper(
 					this.bundleContext);
 			ObjectName bundleServiceObjectName = objectNameFactoryHelper
 					.getObjectName(reference, ServiceReference.class);
 			assertEquals(bundleServiceObjectName, bundleMBeanServicesInUse[i]);
 		}
 
 	}
 
 	/**
 	 * Tests method <code>getRegisteredServices()</code>.
 	 * 
 	 * @throws Exception
 	 *             on error
 	 */
 	public void test_getRegisteredServices() throws Exception {
 		Bundle bundle = getTestBundle();
 		BundleMBean bundleMBean = getTestBundleMBean(bundle);
 
 		ObjectNameFactoryHelper objectNameFactoryHelper = new ObjectNameFactoryHelper(
 				this.bundleContext);
 		final ServiceReference[] registeredBundleServices = bundle
 				.getRegisteredServices();
 		final ObjectName[] objectNameMBeanServices = bundleMBean
 				.getRegisteredServices();
 		assertTrue(registeredBundleServices.length == objectNameMBeanServices.length);
 		for (int i = 0; i < registeredBundleServices.length; i++) {
 			ServiceReference registeredBundleService = registeredBundleServices[i];
 			final ObjectName objectNameBundleService = objectNameFactoryHelper
 					.getObjectName(registeredBundleService,
 							ServiceReference.class);
 			assertEquals(objectNameBundleService, objectNameMBeanServices[i]);
 		}
 	}
 
 	/**
 	 * Tests method <code>getHeaders()</code>.
 	 * 
 	 * @throws Exception
 	 *             on error
 	 */
 	@SuppressWarnings("unchecked")
 	public void test_getHeaders() throws Exception {
 		Bundle bundle = getTestBundle();
 		BundleMBean bundleMBean = getTestBundleMBean(bundle);
 		final Enumeration bundleKeys = bundle.getHeaders().keys();
 		final Enumeration bundleValues = bundle.getHeaders().elements();
 		final TabularData bundleMBeanHeaders = bundleMBean.getHeaders();
 		int bundleHeaderCount = 0;
 		while (bundleKeys.hasMoreElements()) {
 			bundleHeaderCount++;
 			String key = (String) bundleKeys.nextElement();
 			String value = (String) bundleValues.nextElement();
 			assertTrue(bundleMBeanHeaders.get(new String[] { key }).values()
 					.contains(value));
 		}
 		assertTrue(bundleHeaderCount == bundleMBeanHeaders.size());
 	}
 
 	/**
 	 * Tests methods causing change Events (
 	 * <code>start(), stop(), update(), uninstall()</code>.
 	 * 
 	 * @throws Exception
 	 *             on error
 	 */
 	public void test_changeEvents() throws Exception {
 		Bundle bundle = getTestBundle();
 		BundleMBean bundleMBean = getTestBundleMBean(bundle);
 
 		this.bundleContext.addBundleListener(this);
 		// make sure bundle is started
 		if (Bundle.ACTIVE != bundle.getState()) {
 			bundleMBean.start();
 		}
 		assertTrue(bundle.getState() == bundleMBean.getState().longValue()
 				&& bundle.getState() == Bundle.ACTIVE);
 		// test stop bundle
 		bundleMBean.stop();
 		assertTrue(bundle.getState() == bundleMBean.getState().longValue()
 				&& bundle.getState() == Bundle.RESOLVED);
 		// test start bundle
 		bundleMBean.start();
 		assertTrue(bundle.getState() == bundleMBean.getState().longValue()
 				&& bundle.getState() == Bundle.ACTIVE);
 		// test update bundle
 		bundleMBean.update();
		assertTrue(bundleEvents.contains(Integer.valueOf(BundleEvent.UPDATED)));
 		// test uninstall bundle
 		bundleMBean.uninstall();
 		assertTrue(bundle.getState() == Bundle.UNINSTALLED);
 		this.bundleContext.removeBundleListener(this);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public void bundleChanged(BundleEvent event) {
 		bundleEvents.add(Integer.valueOf(event.getType()));
 	}
 
 }
