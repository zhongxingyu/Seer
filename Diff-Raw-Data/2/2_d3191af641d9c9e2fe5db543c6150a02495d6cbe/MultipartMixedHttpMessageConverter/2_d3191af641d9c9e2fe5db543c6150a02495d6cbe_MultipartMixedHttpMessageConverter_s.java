 /*
  * Copyright 2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.springframework.data.keyvalue.riak.client.http;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Enumeration;
 
 import javax.mail.BodyPart;
 import javax.mail.Header;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.internet.MimeMultipart;
 import javax.mail.util.ByteArrayDataSource;
 
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.data.keyvalue.riak.client.data.RiakResponse;
 import org.springframework.data.keyvalue.riak.client.data.RiakRestResponse;
 import org.springframework.http.HttpInputMessage;
 import org.springframework.http.HttpOutputMessage;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.converter.AbstractHttpMessageConverter;
 import org.springframework.http.converter.HttpMessageNotReadableException;
 import org.springframework.http.converter.HttpMessageNotWritableException;
 
 /**
  * @author Andrew Berman
  * 
  */
 public class MultipartMixedHttpMessageConverter<T> extends
 		AbstractHttpMessageConverter<Collection<RiakResponse<T>>> {
 
 	private Class<T> contentClass;
 
 	private static ObjectMapper mapper = new ObjectMapper();
 
 	public MultipartMixedHttpMessageConverter(Class<T> contentClass) {
 		super(MIMEType.MULTIPART_MIXED);
 		this.contentClass = contentClass;
 	}
 
 	@Override
 	protected boolean supports(Class<?> clazz) {
 		return Collection.class.isAssignableFrom(clazz);
 	}
 
 	@Override
 	protected Collection<RiakResponse<T>> readInternal(
 			Class<? extends Collection<RiakResponse<T>>> clazz,
 			final HttpInputMessage inputMessage) throws IOException,
 			HttpMessageNotReadableException {
 		try {
 			Collection<RiakResponse<T>> responses = new ArrayList<RiakResponse<T>>();
 			processMultipart(new MimeMultipart(
 					new ByteArrayDataSource(inputMessage.getBody(),
 							MIMEType.MULTIPART_MIXED.toString())), responses);
 			return responses;
 		} catch (MessagingException e) {
 			throw new HttpMessageNotReadableException(
					"Soemthing wrong with the MIME content", e);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void processMultipart(Multipart mpart,
 			Collection<RiakResponse<T>> collection) throws MessagingException,
 			IOException {
 		for (int index = 0; index < mpart.getCount(); index++) {
 			BodyPart part = mpart.getBodyPart(index);
 			if (part.getContent() instanceof Multipart) {
 				processMultipart((Multipart) part.getContent(), collection);
 			} else {
 
 				HttpHeaders headers = new HttpHeaders();
 				for (Enumeration<?> e = part.getAllHeaders(); e
 						.hasMoreElements();) {
 					Header header = (Header) e.nextElement();
 					headers.add(header.getName(), header.getValue());
 				}
 
 				collection.add(new RiakRestResponse<T>(
 						this.contentClass.isAssignableFrom(part.getContent()
 								.getClass()) ? (T) part.getContent() : mapper
 								.convertValue(part.getContent(),
 										this.contentClass), headers,
 						HttpStatus.OK.toString()));
 			}
 		}
 	}
 
 	@Override
 	protected void writeInternal(Collection<RiakResponse<T>> t,
 			HttpOutputMessage outputMessage) throws IOException,
 			HttpMessageNotWritableException {
 		throw new UnsupportedOperationException("This is currently unsupported");
 
 	}
 }
