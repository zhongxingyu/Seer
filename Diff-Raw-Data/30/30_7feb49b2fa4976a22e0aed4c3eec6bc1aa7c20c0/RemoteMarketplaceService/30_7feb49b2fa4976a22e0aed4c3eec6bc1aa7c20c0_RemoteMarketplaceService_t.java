 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.epp.internal.mpc.core.service;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.UnsupportedEncodingException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.util.Map;
 
 import javax.xml.parsers.SAXParserFactory;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.epp.internal.mpc.core.MarketplaceClientCore;
 import org.eclipse.epp.internal.mpc.core.service.xml.Unmarshaller;
 import org.eclipse.epp.internal.mpc.core.util.ITransport;
 import org.eclipse.epp.internal.mpc.core.util.TransportFactory;
 import org.eclipse.osgi.util.NLS;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 public class RemoteMarketplaceService<T> {
 
 	protected URL baseUrl;
 
 	public static final String API_URI_SUFFIX = "api/p"; //$NON-NLS-1$
 
 	protected static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
 
 	private final ITransport transport = TransportFactory.instance().getTransport();
 
 	private Map<String, String> requestMetaParameters;
 
 	protected IStatus createErrorStatus(String message, Throwable t) {
 		return new Status(IStatus.ERROR, MarketplaceClientCore.BUNDLE_ID, 0, message, t);
 	}
 
 	private void checkConfiguration() {
 		if (baseUrl == null) {
 			throw new IllegalStateException(Messages.DefaultMarketplaceService_mustConfigureBaseUrl);
 		}
 	}
 
 	public URL getBaseUrl() {
 		return baseUrl;
 	}
 
 	public void setBaseUrl(URL baseUrl) {
 		this.baseUrl = baseUrl;
 	}
 
 	protected T processRequest(String relativeUrl, IProgressMonitor monitor) throws CoreException {
		return processRequest(relativeUrl, true, monitor);
	}

	protected T processRequest(String relativeUrl, boolean withMetaParams, IProgressMonitor monitor)
			throws CoreException {
 		URI baseUri;
 		try {
 			baseUri = baseUrl.toURI();
 		} catch (URISyntaxException e) {
 			// should never happen
 			throw new IllegalStateException(e);
 		}
 
		return processRequest(baseUri.toString(), relativeUrl, withMetaParams, monitor);
 	}
 
 	protected T processRequest(String baseUri, String relativePath, IProgressMonitor monitor) throws CoreException {
		return processRequest(baseUri, relativePath, true, monitor);
	}

	@SuppressWarnings({ "unchecked" })
	protected T processRequest(String baseUri, String relativePath, boolean withMetaParams, IProgressMonitor monitor)
			throws CoreException {
 		checkConfiguration();
 		if (baseUri == null || relativePath == null) {
 			throw new IllegalArgumentException();
 		}
 
 		String uri = baseUri;
 		if (!uri.endsWith("/") && !relativePath.startsWith("/")) { //$NON-NLS-1$ //$NON-NLS-2$
 			uri += '/';
 		}
 		uri += relativePath;

		if (withMetaParams) {
			uri = addMetaParameters(uri);
		}
 
 		URI location;
 		try {
 			location = new URI(uri);
 		} catch (URISyntaxException e) {
 			String message = NLS.bind(Messages.DefaultMarketplaceService_invalidLocation, uri);
 			throw new CoreException(createErrorStatus(message, e));
 		}
 
 		final Unmarshaller unmarshaller = new Unmarshaller();
 		monitor.beginTask(NLS.bind(Messages.DefaultMarketplaceService_retrievingDataFrom, baseUri), 100);
 		try {
 			SAXParserFactory parserFactory = SAXParserFactory.newInstance();
 			parserFactory.setNamespaceAware(true);
 			final XMLReader xmlReader;
 			try {
 				xmlReader = parserFactory.newSAXParser().getXMLReader();
 			} catch (Exception e1) {
 				throw new IllegalStateException(e1);
 			}
 			xmlReader.setContentHandler(unmarshaller);
 
 			InputStream in = transport.stream(location, monitor);
 			try {
 				monitor.worked(30);
 
 				// FIXME how can the charset be determined?
 				Reader reader = new InputStreamReader(new BufferedInputStream(in), UTF_8);
 				try {
 					xmlReader.parse(new InputSource(reader));
 				} catch (final SAXException e) {
 					MarketplaceClientCore.error(
 							NLS.bind(Messages.DefaultMarketplaceService_parseError, location.toString()), e);
 					throw new IOException(e.getMessage());
 				}
 			} finally {
 				if (in != null) {
 					in.close();
 				}
 			}
 		} catch (IOException e) {
 			if (e.getCause() instanceof OperationCanceledException) {
 				throw new CoreException(Status.CANCEL_STATUS);
 			}
 			String message = NLS.bind(Messages.DefaultMarketplaceService_cannotCompleteRequest_reason,
 					location.toString(), e.getMessage());
 			throw new CoreException(createErrorStatus(message, e));
 		} finally {
 			monitor.done();
 		}
 
 		Object model = unmarshaller.getModel();
 		if (model == null) {
 			// if we reach here this should never happen
 			throw new IllegalStateException();
 		} else {
 			try {
 				return (T) model;
 			} catch (Exception e) {
 				String message = NLS.bind(Messages.DefaultMarketplaceService_unexpectedResponseContent,
 						model.getClass().getSimpleName());
 				throw new CoreException(createErrorStatus(message, null));
 			}
 		}
 	}
 
 	public String addMetaParameters(String uri) {
 		if (requestMetaParameters != null) {
 			try {
 				boolean hasQueryString = uri.indexOf('?') != -1;
 				for (Map.Entry<String, String> param : requestMetaParameters.entrySet()) {
 					if (param.getKey() == null) {
 						continue;
 					}
 					if (hasQueryString) {
 						uri += '&';
 					} else {
 						hasQueryString = true;
 						uri += '?';
 					}
 					uri += URLEncoder.encode(param.getKey(), UTF_8);
 					uri += '=';
 					if (param.getValue() != null) {
 						uri += URLEncoder.encode(param.getValue(), UTF_8);
 					}
 				}
 			} catch (UnsupportedEncodingException e) {
 				throw new IllegalStateException(e);
 			}
 		}
 		return uri;
 	}
 
 	/**
 	 * The meta-parameters to be included in API requests, or null if there are none. Typically clients will use this
 	 * facility to pass client meta-data to the server. For example, metadata might include the client identity,
 	 * operating system, etc.
 	 */
 	public Map<String, String> getRequestMetaParameters() {
 		return requestMetaParameters;
 	}
 
 	/**
 	 * The meta-parameters to be included in API requests
 	 * 
 	 * @param requestMetaParameters
 	 *            the parameters or null if there should be none
 	 */
 	public void setRequestMetaParameters(Map<String, String> requestMetaParameters) {
 		this.requestMetaParameters = requestMetaParameters;
 	}
 
 }
