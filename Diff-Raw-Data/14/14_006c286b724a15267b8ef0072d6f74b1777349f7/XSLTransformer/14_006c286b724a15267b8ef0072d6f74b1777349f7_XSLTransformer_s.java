 /*
  * Copyright (c) 2009, Zepheira All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * - Redistributions of source code must retain the above copyright notice, this
  *   list of conditions and the following disclaimer.
  * - Redistributions in binary form must reproduce the above copyright notice,
  *   this list of conditions and the following disclaimer in the documentation
  *   and/or other materials provided with the distribution. 
  * - Neither the name of the openrdf.org nor the names of its contributors may
  *   be used to endorse or promote products derived from this software without
  *   specific prior written permission.
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
  * 
  */
 package org.openrdf.repository.object.xslt;
 
 import info.aduna.net.ParsedURI;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.io.Reader;
 import java.io.StringReader;
 import java.net.URL;
 import java.nio.channels.Channels;
 import java.nio.channels.ReadableByteChannel;
 import java.util.Enumeration;
 import java.util.Properties;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ThreadFactory;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLEventWriter;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.transform.Source;
 import javax.xml.transform.Templates;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.URIResolver;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamSource;
 
 import org.openrdf.repository.object.RDFObject;
 import org.openrdf.repository.object.exceptions.ObjectCompositionException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.DocumentFragment;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * Applies XSL transformations with the ability to convert the input and output to a variety of formats.
  */
 public class XSLTransformer implements URIResolver {
 	private static final String FACTORY_SRV = "META-INF/"
 			+ XSLTransformer.class.getPackage().getName()
 			+ ".TransformerFactory";
 	static Executor executor = Executors
 			.newCachedThreadPool(new ThreadFactory() {
 				private volatile int COUNT = 0;
 
 				public Thread newThread(final Runnable r) {
 					Thread thread = new Thread(r, "XSL Transformer "
 							+ (++COUNT));
 					thread.setDaemon(true);
 					return thread;
 				}
 			});
 
 	private Logger logger = LoggerFactory.getLogger(XSLTransformer.class);
 	private TransformerFactory tfactory;
 	private Templates xslt;
 	private String systemId;
 	private XMLOutputFactory factory = XMLOutputFactory.newInstance();
 	private DocumentBuilderFactory builder = DocumentBuilderFactory
 			.newInstance();
 	{
 		builder.setNamespaceAware(true);
 	}
 
 	public XSLTransformer(String url) {
 		this.systemId = url;
 		ClassLoader cp = XSLTransformer.class.getClassLoader();
 		tfactory = (TransformerFactory) findProvider(cp, FACTORY_SRV);
 		if (tfactory == null) {
 			tfactory = new CachedTransformerFactory();
 		}
 }
 
 	public XSLTransformer(Reader markup, String systemId) {
 		this(systemId);
 		ErrorCatcher error = new ErrorCatcher(systemId);
 		tfactory.setErrorListener(error);
 		Source source = new StreamSource(markup, systemId);
 		try {
 			xslt = tfactory.newTemplates(source);
 			if (error.isFatal())
 				throw error.getFatalError();
 		} catch (TransformerConfigurationException e) {
 			throw new ObjectCompositionException(e);
 		} catch (TransformerException e) {
 			throw new ObjectCompositionException(e);
 		}
 	}
 
 	public String getSystemId() {
 		return systemId;
 	}
 
 	@Override
 	public String toString() {
 		return systemId;
 	}
 
 	public Source resolve(String href, String base) throws TransformerException {
 		base = resolveURI(base, systemId);
 		return tfactory.getURIResolver().resolve(resolveURI(href, base), base);
 	}
 
 	public TransformBuilder transform() throws TransformerException,
 			IOException {
 		return builder(new DOMSource(), null);
 	}
 
 	public TransformBuilder transform(File file, String systemId)
 			throws TransformerException, IOException {
 		if (file == null)
 			return transform();
 		FileInputStream in = new FileInputStream(file);
 		return builder(new StreamSource(in, systemId), in);
 	}
 
 	public TransformBuilder transform(RDFObject object, String systemId)
 			throws IOException, TransformerException {
 		if (object == null)
 			return transform();
 		String uri = object.getResource().stringValue();
 		return builder(resolve(uri, null));
 	}
 
 	public TransformBuilder transform(URL url, String systemId)
 			throws IOException, TransformerException {
 		if (url == null)
 			return transform();
 		return builder(resolve(url.toExternalForm(), null));
 	}
 
 	public TransformBuilder transform(String string, String systemId)
 			throws TransformerException, IOException {
 		if (string == null)
 			return transform();
 		return transform(new StringReader(string), systemId);
 	}
 
 	public TransformBuilder transform(CharSequence string, String systemId)
 			throws TransformerException, IOException {
 		if (string == null)
 			return transform();
 		return transform(new StringReader(string.toString()), systemId);
 	}
 
 	public TransformBuilder transform(Readable readable, String systemId)
 			throws TransformerException, IOException {
 		if (readable == null)
 			return transform();
 		if (readable instanceof Reader)
 			return transform((Reader) readable, systemId);
 		return transform(new ReadableReader(readable), systemId);
 	}
 
 	public TransformBuilder transform(Reader reader, String systemId)
 			throws TransformerException, IOException {
 		return builder(new StreamSource(reader, systemId), reader);
 	}
 
 	public TransformBuilder transform(ByteArrayOutputStream buf, String systemId)
 			throws TransformerException, IOException {
 		if (buf == null)
 			return transform();
 		return transform(buf.toByteArray(), systemId);
 	}
 
 	public TransformBuilder transform(byte[] buf, String systemId)
 			throws TransformerException, IOException {
 		if (buf == null)
 			return transform();
 		return transform(new ByteArrayInputStream(buf), systemId);
 	}
 
 	public TransformBuilder transform(ReadableByteChannel channel,
 			String systemId) throws TransformerException, IOException {
 		if (channel == null)
 			return transform();
 		return transform(Channels.newInputStream(channel), systemId);
 	}
 
 	public TransformBuilder transform(InputStream stream, String systemId)
 			throws TransformerException, IOException {
 		if (stream == null)
 			return transform();
 		return builder(new StreamSource(stream, systemId), stream);
 	}
 
 	public TransformBuilder transform(final XMLEventReader reader,
 			final String systemId) throws XMLStreamException, TransformerException,
 			IOException {
 		if (reader == null)
 			return transform();
 		PipedInputStream input = new PipedInputStream();
 		final PipedOutputStream output = new PipedOutputStream(input);
 		final TransformBuilder builder = transform(input, systemId);
 		executor.execute(new Runnable() {
 			public String toString() {
 				return "transforming " + systemId;
 			}
 
 			public void run() {
 				try {
 					XMLEventWriter writer = factory
 							.createXMLEventWriter(output);
 					try {
 						writer.add(reader);
 					} finally {
 						writer.close();
 						output.close();
 					}
 				} catch (XMLStreamException e) {
 					builder.fatalError(new TransformerException(e));
 				} catch (IOException e) {
 					builder.ioException(e);
 				}
 			}
 		});
 		return builder;
 	}
 
 	public TransformBuilder transform(Document node, String systemId)
 			throws TransformerException, IOException {
 		if (node == null)
 			return transform();
 		return builder(new DOMSource(node, systemId), null);
 	}
 
 	public TransformBuilder transform(DocumentFragment node, String systemId)
 			throws TransformerException, IOException,
 			ParserConfigurationException {
 		if (node == null)
 			return transform();
 		NodeList nodes = node.getChildNodes();
 		if (nodes.getLength() == 1)
 			return builder(new DOMSource(node.getFirstChild(), systemId), null);
 		Document doc = builder.newDocumentBuilder().newDocument();
 		Element root = doc.createElement("root");
 		root.appendChild(doc.importNode(node, true));
 		return builder(new DOMSource(root, systemId), null);
 	}
 
 	public TransformBuilder transform(Element node, String systemId)
 			throws TransformerException, IOException {
 		if (node == null)
 			return transform();
 		return builder(new DOMSource(node, systemId), null);
 	}
 
 	public TransformBuilder transform(Node node, String systemId)
 			throws TransformerException, IOException {
 		if (node == null)
 			return transform();
 		return builder(new DOMSource(node, systemId), null);
 	}
 
 	private Object findProvider(ClassLoader cl, String resource) {
 		try {
 			Enumeration<URL> resources = cl.getResources(resource);
 			while (resources.hasMoreElements()) {
 				try {
 					InputStream in = resources.nextElement().openStream();
 					try {
 						Properties properties = new Properties();
 						properties.load(in);
 						Enumeration<?> names = properties.propertyNames();
 						while (names.hasMoreElements()) {
 							String name = (String) names.nextElement();
 							try {
 								Class<?> c = forName(name, true, cl);
 								return c.newInstance();
 							} catch (ClassNotFoundException e) {
 								logger.warn(e.toString());
 							} catch (InstantiationException e) {
 								logger.warn(e.toString());
 							} catch (IllegalAccessException e) {
 								logger.warn(e.toString());
 							}
 						}
 					} finally {
 						in.close();
 					}
 				} catch (IOException e) {
 					logger.warn(e.toString());
 				}
 			}
 		} catch (IOException e) {
 			logger.warn(e.toString());
 		}
 		return null;
 	}
 
 	private Class<?> forName(String name, boolean init, ClassLoader cl)
 			throws ClassNotFoundException {
 		synchronized (cl) {
 			return Class.forName(name, init, cl);
 		}
 	}
 
 	private String resolveURI(String href, String base) {
 		if (href != null && href.contains(":"))
 			return href;
 		ParsedURI abs = null;
 		if (base != null && base.contains(":")) {
 			abs = new ParsedURI(base);
 		} else {
 			abs = new ParsedURI(systemId);
 			if (base != null) {
 				abs = abs.resolve(base);
 			}
 		}
 		if (href != null) {
 			abs = abs.resolve(href);
 		}
 		return abs.toString();
 	}
 
 	private TransformBuilder builder(Source source)
 			throws TransformerException, IOException {
 		Closeable stream = null;
 		if (source instanceof StreamSource) {
 			StreamSource ss = (StreamSource) source;
 			InputStream in = ss.getInputStream();
 			if (in == null) {
 				stream = ss.getReader();
 			} else {
 				stream = in;
 			}
 		}
 		return builder(source, stream);
 	}
 
 	private TransformBuilder builder(Source source, Closeable closeable)
 			throws TransformerException, IOException {
 		try {
 			TransformBuilder tb = new TransformBuilder(newTransformer(),
 					systemId, source, closeable, this);
 			return tb.with("xslt", systemId);
 		} catch (RuntimeException e) {
			closeable.close();
 			throw e;
 		} catch (Error e) {
			closeable.close();
 			throw e;
 		} catch (TransformerException e) {
			closeable.close();
 			throw e;
 		}
 	}
 
 	private synchronized Transformer newTransformer()
 			throws TransformerConfigurationException {
 		if (xslt != null)
 			return xslt.newTransformer();
 		StreamSource xsl = new StreamSource(systemId);
 		Templates templates = tfactory.newTemplates(xsl);
 		if (templates == null)
 			return tfactory.newTransformer();
 		return templates.newTransformer();
 	}
 
 }
