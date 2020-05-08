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
 
 import static org.openrdf.query.QueryLanguage.SPARQL;
 
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.nio.channels.ReadableByteChannel;
 import java.nio.channels.WritableByteChannel;
 import java.nio.charset.Charset;
 
 import org.openrdf.OpenRDFException;
 import org.openrdf.model.BNode;
 import org.openrdf.model.Model;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.Value;
 import org.openrdf.model.impl.LinkedHashModel;
 import org.openrdf.query.GraphQuery;
 import org.openrdf.query.GraphQueryResult;
 import org.openrdf.query.MalformedQueryException;
 import org.openrdf.query.QueryEvaluationException;
 import org.openrdf.query.QueryResult;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.object.ObjectConnection;
 import org.openrdf.repository.object.ObjectFactory;
 import org.openrdf.repository.object.RDFObject;
 
 /**
  * Writes RDF DESCRIBE from an RDFObject.
  * 
  * @author James Leigh
  * 
  */
 public class RDFObjectWriter implements MessageBodyWriter<RDFObject> {
 	private static final String DESCRIBE_SELF = "CONSTRUCT {$self ?pred ?obj}\n"
 			+ "WHERE {$self ?pred ?obj}";
 	private ModelMessageWriter delegate = new ModelMessageWriter();
 
 	public boolean isText(String mimeType, Class<?> type, Type genericType,
 			ObjectFactory of) {
 		return delegate.isText(mimeType, Model.class, Model.class, of);
 	}
 
 	public long getSize(String mimeType, Class<?> type, Type genericType,
 			ObjectFactory of, RDFObject t, Charset charset) {
 		return -1;
 	}
 
 	public boolean isWriteable(String mimeType, Class<?> type,
 			Type genericType, ObjectFactory of) {
 		Class<Model> t = Model.class;
 		if (!delegate.isWriteable(mimeType, t, t, of))
 			return false;
 		if (QueryResult.class.isAssignableFrom(type))
 			return false;
 		if (of == null)
 			return false;
 		if (Object.class.equals(type) || RDFObject.class.equals(type))
 			return true;
 		return of.isNamedConcept(type);
 	}
 
 	public String getContentType(String mimeType, Class<?> type,
 			Type genericType, ObjectFactory of, Charset charset) {
 		return delegate.getContentType(mimeType, Model.class, Model.class, of,
 				charset);
 	}
 
 	public void writeTo(String mimeType, Class<?> type, Type genericType,
 			ObjectFactory of, RDFObject result, String base, Charset charset,
 			WritableByteChannel out, int bufSize) throws IOException,
 			OpenRDFException {
 		ObjectConnection con = result.getObjectConnection();
 		Resource resource = result.getResource();
 		try {
 			Model model = new LinkedHashModel();
 			describeInto(con, resource, model);
 			delegate.writeTo(mimeType, Model.class, Model.class, of, model,
 					base, charset, out, bufSize);
 		} catch (MalformedQueryException e) {
 			throw new AssertionError(e);
 		}
 	}
 
 	public ReadableByteChannel write(String mimeType, Class<?> type,
 			Type genericType, ObjectFactory of, RDFObject result, String base,
 			Charset charset) throws IOException, OpenRDFException {
		if (result == null)
			return null;
 		ObjectConnection con = result.getObjectConnection();
 		Resource resource = result.getResource();
 		try {
 			Model model = new LinkedHashModel();
 			describeInto(con, resource, model);
 			return delegate.write(mimeType, Model.class, Model.class, of,
 					model, base, charset);
 		} catch (MalformedQueryException e) {
 			throw new AssertionError(e);
 		}
 	}
 
 	private void describeInto(ObjectConnection con, Resource resource,
 			Model model) throws MalformedQueryException, RepositoryException,
 			QueryEvaluationException {
 		GraphQuery query = con.prepareGraphQuery(SPARQL, DESCRIBE_SELF);
 		query.setBinding("self", resource);
 		GraphQueryResult result = query.evaluate();
 		try {
 			while (result.hasNext()) {
 				Statement st = result.next();
 				model.add(st);
 				Value obj = st.getObject();
 				if (obj instanceof BNode) {
 					describeInto(con, (Resource) obj, model);
 				}
 			}
 		} finally {
 			result.close();
 		}
 	}
 
 }
