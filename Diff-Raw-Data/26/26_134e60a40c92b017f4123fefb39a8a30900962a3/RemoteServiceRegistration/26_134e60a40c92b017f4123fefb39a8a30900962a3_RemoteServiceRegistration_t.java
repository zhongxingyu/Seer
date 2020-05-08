 /* Copyright (c) 2006-2007 Jan S. Rellermeyer
  * Information and Communication Systems Research Group (IKS),
  * Department of Computer Science, ETH Zurich.
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
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 
 import ch.ethz.iks.slp.ServiceLocationException;
 import ch.ethz.iks.slp.ServiceURL;
 
 /**
  * abstract class of remote service registrations.
  * 
  * @author Jan S. Rellermeyer, ETH Zurich
  * @since 0.5
  */
 class RemoteServiceRegistration {
 	/**
 	 * the local service reference.
 	 */
 	private final ServiceReference reference;
 
 	/**
 	 * the service id.
 	 */
 	private final long serviceID;
 
 	private final ServiceURL[] urls;
 
 	private final String[] interfaceNames;
 
 	/**
 	 * constructor.
 	 * 
 	 * @param reference
 	 *            the service reference.
 	 * @throws ServiceLocationException
 	 */
	RemoteServiceRegistration(final ServiceReference reference, final ServiceReference service)
 			throws ServiceLocationException {
 		this.reference = reference;
 		this.serviceID = ((Long) reference.getProperty(Constants.SERVICE_ID))
 				.longValue();
		this.interfaceNames = (String[]) service
 				.getProperty(Constants.OBJECTCLASS);
 		final int interfaceCount = interfaceNames.length;
 		final Long serviceID = (Long) reference
 				.getProperty(Constants.SERVICE_ID);
 
 		// build the service URLs
 		this.urls = new ServiceURL[interfaceCount];
 		for (int i = 0; i < interfaceCount; i++) {
 			urls[i] = new ServiceURL("service:osgi:"
 					+ interfaceNames[i].replace('.', '/') + "://"
 					+ RemoteOSGiServiceImpl.MY_ADDRESS + ":"
 					+ RemoteOSGiServiceImpl.R_OSGI_PORT + "/" + serviceID,
 					RemoteOSGiServiceImpl.DEFAULT_SLP_LIFETIME);
 		}
 	}
 
 	/**
 	 * get the service id.
 	 * 
 	 * @return the service id.
 	 * @since 0.5
 	 */
 	final long getServiceID() {
 		return serviceID;
 	}
 
 	final ServiceReference getReference() {
 		return reference;
 	}
 
 	/**
 	 * get the attributes.
 	 * 
 	 */
 	Dictionary getProperties() {
 		final String[] keys = reference.getPropertyKeys();
 		final Dictionary props = new Hashtable(keys.length);
 		for (int i = 0; i < keys.length; i++) {
 			props.put(keys[i], reference.getProperty(keys[i]));
 		}
 		return props;
 	}
 
 	ServiceURL[] getURLs() {
 		return urls;
 	}
 
 	String[] getInterfaceNames() {
 		return interfaceNames;
 	}
 
 	/**
 	 * check, if the registration equals a service reference.
 	 * 
 	 * @param obj
 	 *            the object to check.
 	 * @return true if the object is equal.
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	public boolean equals(final Object obj) {
 		if (obj instanceof ServiceReference) {
 			final ServiceReference ref = (ServiceReference) obj;
 			return ref.equals(reference);
 		} else if (obj instanceof RemoteServiceRegistration) {
 			return ((RemoteServiceRegistration) obj).equals(reference);
 		}
 		return false;
 	}
 
 	/**
 	 * get the hash code.
 	 * 
 	 * @return the hash code.
 	 */
 	public int hashCode() {
 		return (int) serviceID;
 	}
 
 }
