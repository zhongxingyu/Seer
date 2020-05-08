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
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Dictionary;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 import ch.ethz.iks.r_osgi.RemoteOSGiException;
 import ch.ethz.iks.slp.ServiceLocationException;
 
 /**
  * a registration for a service that has been registered with TRANSFER_BUNDLE
  * policy. Since R-OSGi has to run on every framework and some implementations
  * do funny things with the bundle JARs once they are installed, there is no
  * generic way to get access to the bundle JAR on the frameowrk's private
  * storage. So we try to get the bundle by location, update location or a
  * special manifest entry that can be set if both is not available. This copy
  * here is stored for the case that the bundle source does not have permanent
  * availability.
  * 
  * @author Jan S. Rellermeyer, ETH Zurich.
  * @since 0.5
  */
 final class BundledServiceRegistration extends RemoteServiceRegistration {
 
 	/**
 	 * the buffer size.
 	 */
 	private static final int BUFFER_SIZE = 2048;
 
 	/**
 	 * the location of the bundle's copy.
 	 */
 	private String location;
 
 	/**
 	 * constructor.
 	 * 
 	 * @param ref
 	 *            the service reference.
 	 * @param storage
 	 *            the storage location for keeping the copies of the bundles.
 	 * @throws RemoteOSGiException
 	 *             if something goes wrong, e.g., the bundle cannot be found.
 	 * @throws ServiceLocationException
 	 *             if the generated SLP service url is invalid.
 	 */
 	BundledServiceRegistration(final ServiceReference ref, final ServiceReference service, final String storage)
 			throws RemoteOSGiException, ServiceLocationException {
		super(ref, service);
 
 		final Bundle bundle = service.getBundle();
 		final Dictionary headers = bundle.getHeaders();
 		final String[] attempts = new String[] { bundle.getLocation(),
 				(String) headers.get(Constants.BUNDLE_UPDATELOCATION),
 				(String) headers.get(RemoteOSGiServiceImpl.BUNDLE_URL) };
 
 		final File file = new File(storage, "bundle" + bundle.getBundleId());
 		for (int i = 0; i < attempts.length; i++) {
 			try {
 				storeFile(file, new URL(attempts[i]).openStream());
 				location = file.getAbsolutePath();
 				return;
 			} catch (IOException ignore) {
 				ignore.printStackTrace();
 			}
 		}
 
 		// TODO: check by code analysis if the bundle registers a configuration.
 		// If this is the case, the configuration should be migrated with the
 		// bundle.
 
 		throw new RemoteOSGiException("Cannot retrieve bundle "
 				+ bundle.getBundleId() + ". Registration failed");
 	}
 
 	/**
 	 * store a file on the storage.
 	 * 
 	 * @param file
 	 *            the file.
 	 * @param input
 	 *            the input stream.
 	 */
 	private static void storeFile(final File file, final InputStream input) {
 		try {
 			file.getParentFile().mkdirs();
 			final FileOutputStream fos = new FileOutputStream(file);
 
 			int available = input.available();
 
 			byte[] buffer = new byte[BUFFER_SIZE];
 			int len;
 			while (available > 0
 					&& (len = input.read(buffer, 0,
 							available < BUFFER_SIZE ? available : BUFFER_SIZE)) > -1) {
 				fos.write(buffer, 0, len);
 				available = input.available();
 			}
 			input.close();
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}
 
 	/**
 	 * get the bundle's bytes.
 	 * 
 	 * @return the raw bytes.
 	 * @throws IOException
 	 *             in case of IO failures.
 	 */
 	byte[] getBundle() throws IOException {
 		final ByteArrayOutputStream out = new ByteArrayOutputStream();
 		final FileInputStream fis = new FileInputStream(location);
 		byte[] chunk = new byte[BUFFER_SIZE];
 		int len;
 		while ((len = fis.read(chunk, 0, BUFFER_SIZE)) > 0) {
 			out.write(chunk, 0, len);
 		}
 		return out.toByteArray();
 	}
 }
