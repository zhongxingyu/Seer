 /*
  * Copyright 2009-2010, James Leigh and Zepheira LLC Some rights reserved.
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
 package org.openrdf.http.object.writers;
 
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.nio.channels.Pipe;
 import java.nio.channels.ReadableByteChannel;
 import java.nio.channels.WritableByteChannel;
 import java.nio.channels.Pipe.SinkChannel;
 import java.nio.charset.Charset;
 import java.util.concurrent.Executor;
 
 import javax.xml.stream.XMLEventReader;
 import javax.xml.stream.XMLEventWriter;
 import javax.xml.stream.XMLOutputFactory;
 import javax.xml.stream.XMLStreamException;
 
 import org.openrdf.http.object.util.ChannelUtil;
 import org.openrdf.http.object.util.ErrorReadableByteChannel;
 import org.openrdf.http.object.util.SharedExecutors;
 import org.openrdf.repository.object.ObjectFactory;
 
 /**
  * Writes an XMLEventReader into an OutputStream.
  */
 public class XMLEventMessageWriter implements MessageBodyWriter<XMLEventReader> {
 	private static Executor executor = SharedExecutors.getWriterThreadPool();
 	private XMLOutputFactory factory;
 	{
 		factory = XMLOutputFactory.newInstance();
 		factory.setProperty("javax.xml.stream.isRepairingNamespaces",
 				Boolean.TRUE);
 	}
 
 	public boolean isText(String mimeType, Class<?> type, Type genericType,
 			ObjectFactory of) {
 		return true;
 	}
 
 	public boolean isWriteable(String mediaType, Class<?> type,
 			Type genericType, ObjectFactory of) {
 		if (!XMLEventReader.class.isAssignableFrom(type))
 			return false;
 		if (mediaType != null && !mediaType.startsWith("*")
 				&& !mediaType.startsWith("text/")
 				&& !mediaType.startsWith("application/"))
 			return false;
 		return true;
 	}
 
 	public long getSize(String mimeType, Class<?> type, Type genericType,
 			ObjectFactory of, XMLEventReader t, Charset charset) {
 		return -1;
 	}
 
 	public String getContentType(String mimeType, Class<?> type,
 			Type genericType, ObjectFactory of, Charset charset) {
 		if (mimeType == null || mimeType.startsWith("*")
 				|| mimeType.startsWith("application/*"))
 			return "application/xml";
 		if (mimeType.startsWith("text/")) {
 			if (charset == null) {
 				charset = Charset.defaultCharset();
 			}
 			if (mimeType.startsWith("text/*"))
 				return "text/xml;charset=" + charset.name();
 			return mimeType + ";charset=" + charset.name();
 		}
 		return mimeType;
 	}
 
 	public void writeTo(String mimeType, Class<?> type, Type genericType,
 			ObjectFactory of, XMLEventReader result, String base,
 			Charset charset, WritableByteChannel out, int bufSize)
 			throws IOException, XMLStreamException {
 		try {
 			if (charset == null) {
 				charset = Charset.defaultCharset();
 			}
 			XMLEventWriter writer = factory.createXMLEventWriter(ChannelUtil
 					.newOutputStream(out), charset.name());
 			try {
 				writer.add(result);
 				writer.flush();
 			} finally {
 				writer.close();
 			}
 		} finally {
 			result.close();
 		}
 	}
 
 	public ReadableByteChannel write(final String mimeType,
 			final Class<?> type, final Type genericType,
 			final ObjectFactory of, final XMLEventReader result,
 			final String base, final Charset charset) throws IOException {
		if (result == null)
			return null;
 		Pipe pipe = Pipe.open();
 		final SinkChannel out = pipe.sink();
 		final ErrorReadableByteChannel in = new ErrorReadableByteChannel(pipe) {
 			public String toString() {
 				return result.toString();
 			}
 		};
 		executor.execute(new Runnable() {
 			public void run() {
 				try {
 					try {
 						writeTo(mimeType, type, genericType, of, result, base,
 								charset, out, 1024);
 					} finally {
 						out.close();
 					}
 				} catch (IOException e) {
 					in.error(e);
 				} catch (Exception e) {
 					in.error(new IOException(e));
 				} catch (Error e) {
 					in.error(new IOException(e));
 				}
 			}
 		});
 		return in;
 	}
 }
