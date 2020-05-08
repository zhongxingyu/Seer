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
 package org.eclipse.riena.communication.publisher.hessian;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import javax.servlet.GenericServlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.caucho.hessian.io.AbstractHessianOutput;
 import com.caucho.hessian.io.Hessian2Input;
 import com.caucho.hessian.io.Hessian2Output;
 import com.caucho.hessian.io.HessianOutput;
 import com.caucho.hessian.io.SerializerFactory;
 import com.caucho.hessian.server.HessianSkeleton;
 
 import org.osgi.service.log.LogService;
 
 import org.eclipse.equinox.log.Logger;
 
 import org.eclipse.riena.communication.core.RemoteServiceDescription;
 import org.eclipse.riena.communication.core.zipsupport.ReusableBufferedInputStream;
 import org.eclipse.riena.core.Log4r;
 import org.eclipse.riena.core.exception.IExceptionHandlerManager;
 import org.eclipse.riena.core.service.Service;
import org.eclipse.riena.internal.communication.factory.hessian.serializer.RienaSerializerFactory;
 import org.eclipse.riena.internal.communication.publisher.hessian.Activator;
 import org.eclipse.riena.internal.communication.publisher.hessian.HessianRemoteServicePublisher;
 import org.eclipse.riena.internal.communication.publisher.hessian.MessageContext;
 import org.eclipse.riena.internal.communication.publisher.hessian.MessageContextHolder;
 
 /**
  * TODO: JavaDoc
  */
 @SuppressWarnings("serial")
 public class RienaHessianDispatcherServlet extends GenericServlet {
 
 	private SerializerFactory serializerFactory = null;
 
 	private final static Logger LOGGER = Log4r.getLogger(Activator.getDefault(), RienaHessianDispatcherServlet.class);
 
 	@SuppressWarnings("restriction")
 	@Override
 	public void init(final ServletConfig config) throws ServletException {
 		super.init(config);
 		serializerFactory = new SerializerFactory();
 		serializerFactory.setAllowNonSerializable(true);
 		serializerFactory.addFactory(new RienaSerializerFactory());
 
 		LOGGER.log(LogService.LOG_DEBUG, "initialized"); //$NON-NLS-1$
 	}
 
 	@Override
 	public void service(final ServletRequest req, final ServletResponse res) throws ServletException, IOException {
 
 		final HttpServletRequest httpReq = (HttpServletRequest) req;
 		final HttpServletResponse httpRes = (HttpServletResponse) res;
 
 		// set the message context
 		MessageContextHolder.setMessageContext(new MessageContext(httpReq, httpRes));
 
 		final HessianRemoteServicePublisher publisher = getPublisher();
 		if (publisher == null) {
 			if (httpReq.getMethod().equals("GET")) { //$NON-NLS-1$
 				if (httpReq.getRemoteHost().equals("127.0.0.1")) { //$NON-NLS-1$
 					final PrintWriter pw = new PrintWriter(res.getOutputStream());
 					pw.write("no webservices available"); //$NON-NLS-1$
 					pw.flush();
 					pw.close();
 					return;
 				} else {
 					httpRes.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Hessian requires POST"); //$NON-NLS-1$
 					return;
 				}
 			} else {
 				httpRes.sendError(HttpServletResponse.SC_NOT_FOUND, "webservice not found"); //$NON-NLS-1$
 				return;
 			}
 		}
 		String requestURI = httpReq.getRequestURI();
 		final String contextPath = httpReq.getContextPath();
 		if (contextPath.length() > 1) {
 			requestURI = requestURI.substring(contextPath.length());
 		}
 		final RemoteServiceDescription rsd = publisher.findService(requestURI);
 		log("call " + rsd); //$NON-NLS-1$
 		if (httpReq.getMethod().equals("GET")) { //$NON-NLS-1$
 			if (httpReq.getRemoteHost().equals("127.0.0.1")) { //$NON-NLS-1$
 				final PrintWriter pw = new PrintWriter(res.getOutputStream());
 				if (rsd == null) {
 					pw.write("call received from browser, no remote service registered with this URL"); //$NON-NLS-1$
 				} else {
 					pw.write("calls " + rsd); //$NON-NLS-1$
 				}
 				pw.flush();
 				pw.close();
 				return;
 			} else {
 				httpRes.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Hessian requires POST"); //$NON-NLS-1$
 				return;
 			}
 		}
 		if (rsd == null) {
 			httpRes.sendError(HttpServletResponse.SC_NOT_FOUND, "unknown url :" + httpReq.getRequestURI()); //$NON-NLS-1$
 			return;
 		}
 
 		final String gzip = httpReq.getHeader("Content-Encoding"); //$NON-NLS-1$
 		final boolean gzipFlag = "x-hessian-gzip".equals(gzip); //$NON-NLS-1$
 		boolean inputWasGZIP = false;
 
 		InputStream requestInputStream = httpReq.getInputStream();
 		if (gzipFlag) {
 			final BufferedInputStream tempInput = new ReusableBufferedInputStream(requestInputStream);
 			if (tempInput.markSupported()) {
 				tempInput.mark(20);
 				final int readMAGIC = tempInput.read() + tempInput.read() * 256;
 				inputWasGZIP = (readMAGIC == GZIPInputStream.GZIP_MAGIC);
 				tempInput.reset();
 			}
 			requestInputStream = inputWasGZIP ? new GZIPInputStream(tempInput) : tempInput;
 		}
 
 		final Hessian2Input inp = new Hessian2Input(requestInputStream);
 		inp.setSerializerFactory(serializerFactory);
 		inp.setCloseStreamOnClose(true);
 
 		final int code = inp.read();
 		if (code != 'c') {
 			throw new IOException("expected 'c' in hessian input at " + code); //$NON-NLS-1$
 		}
 		final int major = inp.read();
 		inp.read(); // read/skip the minor version - not used currently
 
 		if (inputWasGZIP) {
 			httpRes.setHeader("Content-Encoding", "x-hessian-gzip"); //$NON-NLS-1$//$NON-NLS-2$
 		}
 
 		OutputStream outputStream = httpRes.getOutputStream();
 		if (inputWasGZIP) {
 			outputStream = new GZIPOutputStream(outputStream);
 		}
 
 		AbstractHessianOutput out;
 		if (major >= 2) {
 			out = new Hessian2Output(outputStream);
 			((Hessian2Output) out).setCloseStreamOnClose(true);
 		} else {
 			out = new HessianOutput(outputStream);
 		}
 
 		out.setSerializerFactory(serializerFactory);
 
 		// TODO TCCL causes problems wit log4j: http://articles.qos.ch/classloader.html
 		//		final ClassLoader original = Thread.currentThread().getContextClassLoader();
 		try {
 			//			Thread.currentThread().setContextClassLoader(new ServiceClassLoader(original, rsd.getBundle()));
 			final HessianSkeleton sk = new HessianSkeleton(rsd.getService(), rsd.getServiceInterfaceClass());
 			sk.invoke(inp, out);
 		} catch (final Throwable t) {
 			Throwable t2 = t;
 			while (t2.getCause() != null) {
 				t2 = t2.getCause();
 			}
 			LOGGER.log(LogService.LOG_ERROR, t.getMessage(), t2);
 			Service.get(IExceptionHandlerManager.class).handleException(t2);
 			throw new ServletException(t);
 		} finally {
 			inp.close();
 			out.close(); // Hessian2Output forgets to close if the service throws an exception
 			//			Thread.currentThread().setContextClassLoader(original);
 		}
 	}
 
 	/**
 	 * 
 	 * @return the publisher
 	 */
 	protected HessianRemoteServicePublisher getPublisher() {
 		return Activator.getDefault().getPublisher();
 	}
 
 }
