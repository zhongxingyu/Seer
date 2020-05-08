 /* Copyright (c) 2006 Jan S. Rellermeyer
  * Information and Communication Systems Research Group (IKS),
  * Institute for Pervasive Computing, ETH Zurich.
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of ETH Zurich nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package ch.ethz.iks.r_osgi.impl;
 
 import java.lang.reflect.Method;
 import java.util.Dictionary;
 import java.util.HashMap;
 import org.objectweb.asm.Type;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 
 import ch.ethz.iks.r_osgi.RemoteOSGiService;
 import ch.ethz.iks.slp.ServiceLocationException;
 import ch.ethz.iks.slp.ServiceURL;
 
 /**
  * RemoteService encapsulates a service registered for remoting.
  * 
  * @author Jan S. Rellermeyer
  * @since 0.1
  */
 final class ProxiedServiceRegistration extends RemoteServiceRegistration {
 
 	/**
 	 * the service object.
 	 */
 	private final Object serviceObject;
 
 	private final HashMap methodTable = new HashMap(0);
 
 	private HashMap deliverServiceMessages;
 
 	/**
 	 * creates a new RemoteService object.
 	 * 
 	 * @param ref
 	 *            the <code>ServiceReference</code>.
 	 * @param interfaceNames
 	 *            the names of the service interfaces.
 	 * @param smartProxy
 	 *            an optional abstract class as smart proxy.
 	 * @param injectionClasses
 	 *            optional class injections.
 	 * @throws ClassNotFoundException
 	 *             if one of the interface classes cannot be found.
 	 * @throws ServiceLocationException
 	 */
 	ProxiedServiceRegistration(final ServiceReference ref, final ServiceReference service)
 			throws ClassNotFoundException, ServiceLocationException {
 
		super(ref, service);
 
 		// get the service object
 		this.serviceObject = RemoteOSGiServiceImpl.context.getService(service);
 		if (serviceObject == null) {
 			throw new IllegalStateException("Service is not present.");
 		}
 
 		// get the interface classes
 		final ClassLoader bundleLoader = serviceObject.getClass()
 				.getClassLoader();
 		final String[] interfaceNames = (String[]) service
 				.getProperty(Constants.OBJECTCLASS);
 		final int interfaceCount = interfaceNames.length;
 		final Class[] serviceInterfaces = new Class[interfaceCount];
 
 		// build up the method table for each interface
 		for (int i = 0; i < interfaceCount; i++) {
 			serviceInterfaces[i] = bundleLoader.loadClass(interfaceNames[i]);
 			final Method[] methods = serviceInterfaces[i].getMethods();
 			for (int j = 0; j < methods.length; j++) {
 				methodTable.put(methods[j].getName()
 						+ Type.getMethodDescriptor(methods[j]), methods[j]);
 			}
 		}
 
 		final Dictionary headers = service.getBundle().getHeaders();
 		final CodeAnalyzer inspector = new CodeAnalyzer(bundleLoader,
 				(String) headers.get(Constants.IMPORT_PACKAGE),
 				(String) headers.get(Constants.EXPORT_PACKAGE));
 		deliverServiceMessages = new HashMap(interfaceNames.length);
 		try {
 			for (int i = 0; i < interfaceNames.length; i++) {
 				deliverServiceMessages
 						.put(
 								interfaceNames[i].replace('.', '/'),
 								inspector
 										.analyze(
 												interfaceNames[i],
 												(String) ref
 														.getProperty(RemoteOSGiService.SMART_PROXY),
 												(String[]) ref
 														.getProperty(RemoteOSGiService.INJECTIONS),
 												(String) ref
 														.getProperty(RemoteOSGiServiceImpl.PRESENTATION)));
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * get the service object.
 	 * 
 	 * @return the service object.
 	 */
 	Object getServiceObject() {
 		return serviceObject;
 	}
 
 	Method getMethod(final String signature) {
 		return (Method) methodTable.get(signature);
 	}
 
 	DeliverServiceMessage getMessage(final FetchServiceMessage fetchReq)
 			throws ServiceLocationException {
 		final String serviceURL = fetchReq.getServiceURL();
 		final DeliverServiceMessage msg = (DeliverServiceMessage) deliverServiceMessages
 				.get(new ServiceURL(serviceURL, 0).getServiceType()
 						.getConcreteTypeName());
 		msg.init(fetchReq, getProperties());
 		return msg;
 	}
 }
