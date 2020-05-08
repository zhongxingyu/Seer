 /*
  * Copyright (c) 2009 Stephan Schloepke and innoQ Deutschland GmbH
  *
  * Stephan Schloepke: http://www.schloepke.de/
  * innoQ Deutschland GmbH: http://www.innoq.com/
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.atomify.service.support;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.Writer;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Type;
 import java.nio.charset.Charset;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 import javax.ws.rs.WebApplicationException;
 import javax.ws.rs.core.Context;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.Response.Status;
 import javax.ws.rs.ext.ContextResolver;
 import javax.ws.rs.ext.MessageBodyWriter;
 import javax.ws.rs.ext.Providers;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.util.JAXBSource;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 public class XHTMLStylesheetAttachedXMLProvider implements MessageBodyWriter<XHTMLStylesheetAttachedJAXB<?>> {
 	private static final Charset UTF8 = Charset.forName("UTF-8"); //$NON-NLS-1$
 	private static final Charset UTF16 = Charset.forName("UTF-16"); //$NON-NLS-1$
 	private static final Map<Class<?>, JAXBContext> JAXB_CONTEXT_CACHE = new WeakHashMap<Class<?>, JAXBContext>();
 
 	private final Providers providers;
 
 	public XHTMLStylesheetAttachedXMLProvider(@Context final Providers providers) {
 		this.providers = providers;
 	}
 
 	public long getSize(final XHTMLStylesheetAttachedJAXB<?> t, final Class<?> type, final Type genericType, final Annotation[] annotations,
 			final MediaType mediaType) {
 		return -1;
 	}
 
 	public boolean isWriteable(final Class<?> type, final Type genericType, final Annotation[] annotations, final MediaType mediaType) {
 		return XHTMLStylesheetAttachedJAXB.class.isAssignableFrom(type) && (MediaType.APPLICATION_XHTML_XML_TYPE.equals(mediaType) ||
 				MediaType.APPLICATION_XML_TYPE.equals(mediaType) ||
 				MediaType.TEXT_HTML_TYPE.equals(mediaType) ||
 				MediaType.TEXT_XML_TYPE.equals(mediaType) ||
 				(mediaType != null && mediaType.getSubtype().endsWith("+xml"))); //$NON-NLS-1$
 	}
 
 	public void writeTo(final XHTMLStylesheetAttachedJAXB<?> t, final Class<?> type, final Type genericType, final Annotation[] annotations,
 			final MediaType mediaType,
 			final MultivaluedMap<String, Object> httpHeaders, final OutputStream entityStream) throws IOException, WebApplicationException {
 		try {
 			final Marshaller marshaller = getMarshaller(t.getEntityType(), mediaType);
 			Charset charset = XHTMLStylesheetAttachedXMLProvider.UTF8;
 			if (mediaType != null) {
 				String temp = mediaType.getParameters().get("charset"); //$NON-NLS-1$
 				if (temp != null) {
 					charset = Charset.forName(temp);
 				}
 			}
 			Writer w = new OutputStreamWriter(entityStream, charset);
			if (MediaType.APPLICATION_XHTML_XML_TYPE.equals(mediaType) || MediaType.TEXT_HTML_TYPE.equals(mediaType)) {
 				writeToXhtml(t, mediaType, charset, marshaller, w);
 			} else {
 				writeToXml(t, mediaType, charset, marshaller, w);
 			}
 		} catch (JAXBException e) {
 			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
 		}
 	}
 
 	private void writeToXhtml(final XHTMLStylesheetAttachedJAXB<?> t, final MediaType mediaType, final Charset charset,
 			final Marshaller marshaller, final Writer w) throws JAXBException, IOException {
 		try {
 			Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(t.getStylesheet().toURL().openStream()));
 			Source source = new JAXBSource(marshaller, t.getEntity());
 			Result result = new StreamResult(w);
 			transformer.transform(source, result);
 		} catch (TransformerException e) {
 			throw new WebApplicationException(e, Status.INTERNAL_SERVER_ERROR);
 		}
 	}
 
 	private void writeToXml(final XHTMLStylesheetAttachedJAXB<?> t, final MediaType mediaType, final Charset charset,
 			final Marshaller marshaller, final Writer w) throws JAXBException, IOException {
 		Object jaxbFragmentProperty = marshaller.getProperty(Marshaller.JAXB_FRAGMENT);
 		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
 		w.write("<?xml version=\"1.0\" "); //$NON-NLS-1$
 		if (charset != XHTMLStylesheetAttachedXMLProvider.UTF8 && charset != XHTMLStylesheetAttachedXMLProvider.UTF16) {
 			w.write("encoding=\""); //$NON-NLS-1$
 			w.write(charset.name());
 			w.write("\" "); //$NON-NLS-1$
 		}
 		w.write("?>\n"); //$NON-NLS-1$
 		w.write("<?xml-stylesheet type=\"text/xsl\" href=\""); //$NON-NLS-1$
 		w.write(t.getStylesheet().toASCIIString());
 		w.write("\" ?>\n\n"); //$NON-NLS-1$
 		marshaller.marshal(t.getEntity(), w);
 		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, jaxbFragmentProperty);
 	}
 
 	protected final Marshaller getMarshaller(final Class<?> type, final MediaType mt) throws JAXBException {
 		final ContextResolver<Marshaller> marshallerResolver = this.providers.getContextResolver(Marshaller.class, mt);
 		Marshaller marshaller = null;
 		if (marshallerResolver != null) {
 			marshaller = marshallerResolver.getContext(type);
 		}
 		if (marshaller == null) {
 			JAXBContext context = null;
 			final ContextResolver<JAXBContext> cr = this.providers.getContextResolver(JAXBContext.class, mt);
 			if (cr != null) {
 				context = cr.getContext(type);
 			} else {
 				synchronized (XHTMLStylesheetAttachedXMLProvider.JAXB_CONTEXT_CACHE) {
 					context = XHTMLStylesheetAttachedXMLProvider.JAXB_CONTEXT_CACHE.get(type);
 					if (context == null) {
 						context = JAXBContext.newInstance(type);
 						XHTMLStylesheetAttachedXMLProvider.JAXB_CONTEXT_CACHE.put(type, context);
 					}
 				}
 			}
 			marshaller = context.createMarshaller();
 			marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 		}
 		return marshaller;
 	}
 
 }
