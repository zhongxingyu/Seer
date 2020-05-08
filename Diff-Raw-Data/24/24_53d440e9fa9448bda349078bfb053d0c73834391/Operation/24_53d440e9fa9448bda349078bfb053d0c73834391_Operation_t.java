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
 package org.openrdf.server.metadata.controllers;
 
 import java.io.File;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.lang.reflect.Type;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.activation.MimeTypeParseException;
 
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.object.annotations.iri;
 import org.openrdf.server.metadata.WebObject;
 import org.openrdf.server.metadata.annotations.cacheControl;
 import org.openrdf.server.metadata.annotations.method;
 import org.openrdf.server.metadata.annotations.operation;
 import org.openrdf.server.metadata.annotations.parameter;
 import org.openrdf.server.metadata.annotations.rel;
 import org.openrdf.server.metadata.annotations.title;
 import org.openrdf.server.metadata.annotations.transform;
 import org.openrdf.server.metadata.annotations.type;
 import org.openrdf.server.metadata.concepts.WebRedirect;
 import org.openrdf.server.metadata.exceptions.BadRequest;
 import org.openrdf.server.metadata.exceptions.MethodNotAllowed;
 import org.openrdf.server.metadata.http.Entity;
 import org.openrdf.server.metadata.http.Request;
 import org.openrdf.server.metadata.http.ResponseEntity;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class Operation {
 	private static int MAX_TRANSFORM_DEPTH = 100;
 	private Logger logger = LoggerFactory.getLogger(Operation.class);
 
 	private boolean exists;
 	private Request req;
 	private Method method;
 	private Method transformMethod;
 	private boolean notAllowed;
 	private boolean badRequest;
 
 	public Operation(Request req, boolean exists) throws MimeTypeParseException {
 		this.req = req;
 		this.exists = exists;
 		try {
 			String m = req.getMethod();
 			if ("GET".equals(m) || "HEAD".equals(m)) {
 				method = findMethod(m, true);
 			} else if ("PUT".equals(m) || "DELETE".equals(m)
 					|| "OPTIONS".equals(m)) {
 				method = findMethod(m, false);
 			} else {
 				method = findMethod(m);
 			}
 			transformMethod = getTransformMethodOf(method);
 		} catch (MethodNotAllowed e) {
 			notAllowed = true;
 		} catch (BadRequest e) {
 			badRequest = true;
 		}
 	}
 
 	public String toString() {
 		if (method != null)
 			return method.getName();
 		return req.toString();
 	}
 
 	public String getContentType() throws MimeTypeParseException {
 		Method m = getTransformMethod();
 		if (m == null && exists)
 			return req.getRequestedResource().getMediaType();
 		if (m == null || m.getReturnType().equals(Void.TYPE))
 			return null;
 		if (URL.class.equals(m.getReturnType()))
 			return null;
 		return req.getContentType(m);
 	}
 
 	public String getEntityTag(String contentType)
 			throws MimeTypeParseException {
 		WebObject target = req.getRequestedResource();
 		Method m = this.method;
 		String method = req.getMethod();
 		if (m == null && exists) {
 			return req.getRequestedResource().identityTag();
 		} else if (contentType != null) {
 			return target.variantTag(contentType);
 		} else if ("GET".equals(method) || "HEAD".equals(method)) {
 			if (m != null && contentType == null)
 				return target.revisionTag();
 			if (m != null)
 				return target.variantTag(contentType);
 			if (!exists && !(target instanceof WebRedirect)) {
 				Method operation;
 				if ((operation = getOperationMethod("alternate")) != null) {
 					return target.variantTag(req.getContentType(operation));
 				} else if ((operation = getOperationMethod("describedby")) != null) {
 					return target.variantTag(req.getContentType(operation));
 				}
 			}
 		} else if ("PUT".equals(method)) {
 			Method get;
 			try {
				get = getTransformMethodOf(findMethod("GET", true));
 			} catch (MethodNotAllowed e) {
 				get = null;
 			} catch (BadRequest e) {
 				get = null;
 			}
 			String media = target.getMediaType();
 			if (get == null && media == null) {
 				return null;
 			} else if (get == null && media.equals(req.getContentType())) {
 				return target.identityTag();
 			} else if (get == null) {
 				return target.variantTag(req.getContentType());
 			} else if (URL.class.equals(get.getReturnType())) {
 				return target.revisionTag();
 			} else {
 				return target.variantTag(req.getContentType(get));
 			}
 		} else {
 			Method get;
 			try {
				get = getTransformMethodOf(findMethod("GET", true));
 			} catch (MethodNotAllowed e) {
 				get = null;
 			} catch (BadRequest e) {
 				get = null;
 			}
 			String media = target.getMediaType();
 			if (get == null && media == null) {
 				return null;
 			} else if (get == null || URL.class.equals(get.getReturnType())) {
 				return target.revisionTag();
 			} else {
 				return target.variantTag(req.getContentType(get));
 			}
 		}
 		return null;
 	}
 
 	public Class<?> getEntityType() throws MimeTypeParseException {
 		String method = req.getMethod();
 		Method m = getTransformMethod();
 		if (m == null || "PUT".equals(method) || "DELETE".equals(method)
 				|| "OPTIONS".equals(method))
 			return null;
 		if (m == null && exists)
 			return File.class;
 		return m.getReturnType();
 	}
 
 	public long getLastModified() throws MimeTypeParseException {
 		String method = req.getMethod();
 		Method m = this.method;
 		if (m != null && !"PUT".equals(method) && !"DELETE".equals(method)
 				&& !"OPTIONS".equals(method)) {
 			if (m.isAnnotationPresent(cacheControl.class)) {
 				for (String value : m.getAnnotation(cacheControl.class).value()) {
 					if (value.contains("must-reevaluate"))
 						return System.currentTimeMillis() / 1000 * 1000;
 				}
 			}
 		}
 		WebObject target = req.getRequestedResource();
 		if (mustReevaluate(target.getClass()))
 			return System.currentTimeMillis() / 1000 * 1000;
 		if (m == null && exists)
 			return req.getFile().lastModified() / 1000 * 1000;
 		return target.getLastModified();
 	}
 
 	public List<String> getLinks() throws RepositoryException {
 		Map<String, List<Method>> map = getOperationMethods(req
 				.getRequestedResource(), "GET", true);
 		List<String> result = new ArrayList<String>(map.size());
 		StringBuilder sb = new StringBuilder();
 		for (Map.Entry<String, List<Method>> e : map.entrySet()) {
 			sb.delete(0, sb.length());
 			sb.append("<").append(req.getURI());
 			sb.append("?").append(e.getKey()).append(">");
 			for (Method m : e.getValue()) {
 				if (m.isAnnotationPresent(rel.class)) {
 					sb.append("; rel=\"");
 					for (String value : m.getAnnotation(rel.class).value()) {
 						sb.append(value).append(" ");
 					}
 					sb.setCharAt(sb.length() - 1, '"');
 				}
 				if (m.isAnnotationPresent(type.class)) {
 					sb.append("; type=\"");
 					for (String value : getTypes(m)) {
 						sb.append(value).append(" ");
 					}
 					sb.setCharAt(sb.length() - 1, '"');
 				}
 				if (m.isAnnotationPresent(title.class)) {
 					for (String value : m.getAnnotation(title.class).value()) {
 						sb.append("; title=\"").append(value).append("\"");
 					}
 				}
 			}
 			result.add(sb.toString());
 		}
 		return result;
 	}
 
 	protected Set<String> getAllowedMethods() throws RepositoryException {
 		Set<String> set = new LinkedHashSet<String>();
 		String name = req.getOperation();
 		File file = req.getFile();
 		WebObject target = req.getRequestedResource();
 		if (!req.isQueryStringPresent() && file.canRead()
 				|| getOperationMethods(target, "GET", true).containsKey(name)) {
 			set.add("GET");
 			set.add("HEAD");
 		}
 		if (!req.isQueryStringPresent()) {
 			if (!file.exists() || file.canWrite()) {
 				set.add("PUT");
 			}
 			if (file.exists() && file.getParentFile().canWrite()) {
 				set.add("DELETE");
 			}
 		} else if (getOperationMethods(target, "PUT", false).containsKey(name)) {
 			set.add("PUT");
 		} else if (getOperationMethods(target, "DELETE", false).containsKey(
 				name)) {
 			set.add("DELETE");
 		}
 		Map<String, List<Method>> map = getPostMethods(target);
 		for (String method : map.keySet()) {
 			set.add(method);
 		}
 		return set;
 	}
 
 	protected Method getMethod() {
 		if (notAllowed)
 			throw new MethodNotAllowed();
 		if (badRequest)
 			throw new BadRequest();
 		return method;
 	}
 
 	protected Method getOperationMethod(String rel)
 			throws MimeTypeParseException {
 		Map<String, List<Method>> map = getOperationMethods(req
 				.getRequestedResource(), "GET", true);
 		for (Map.Entry<String, List<Method>> e : map.entrySet()) {
 			for (Method m : e.getValue()) {
 				if (m.isAnnotationPresent(rel.class)) {
 					for (String value : m.getAnnotation(rel.class).value()) {
 						if (rel.equals(value) && req.isAcceptable(m)) {
 							return m;
 						}
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	protected Map<String, List<Method>> getOperationMethods(WebObject target,
 			String method, Boolean isRespBody) {
 		Map<String, List<Method>> map = new HashMap<String, List<Method>>();
 		for (Method m : target.getClass().getMethods()) {
 			if (isRespBody != null
 					&& isRespBody != !m.getReturnType().equals(Void.TYPE))
 				continue;
 			operation ann = m.getAnnotation(operation.class);
 			if (ann == null)
 				continue;
 			if (m.isAnnotationPresent(method.class)) {
 				for (String v : m.getAnnotation(method.class).value()) {
 					if (method.equals(v)) {
 						put(map, ann.value(), m);
 						break;
 					}
 				}
 			} else if ("GET".equals(method) || "HEAD".equals(method)
 					|| "PUT".equals(method) || "DELETE".equals(method)
 					|| "OPTIONS".equals(method)) {
 				// don't require method annotation for these methods
 				put(map, ann.value(), m);
 			}
 		}
 		return map;
 	}
 
 	protected Object[] getParameters(Method method, Entity input)
 			throws Exception {
 		Class<?>[] ptypes = method.getParameterTypes();
 		Annotation[][] anns = method.getParameterAnnotations();
 		Type[] gtypes = method.getGenericParameterTypes();
 		Object[] args = new Object[ptypes.length];
 		for (int i = 0; i < args.length; i++) {
 			args[i] = getParameter(anns[i], ptypes[i], input).read(ptypes[i],
 					gtypes[i]);
 		}
 		return args;
 	}
 
 	protected ResponseEntity invoke(Method method, Object[] args, boolean follow)
 			throws Exception {
 		Object result = method.invoke(req.getRequestedResource(), args);
 		ResponseEntity input = req.createResultEntity(result, method
 				.getReturnType(), method.getGenericReturnType(),
 				getTypes(method));
 		if (follow && method.isAnnotationPresent(transform.class)) {
 			for (String uri : method.getAnnotation(transform.class).value()) {
 				Method transform = getTransform(uri);
 				if (isAcceptable(transform, 0)) {
 					return invoke(transform, getParameters(transform, input),
 							follow);
 				}
 			}
 		}
 		return input;
 	}
 
 	private String[] getTypes(Method method) {
 		if (method.isAnnotationPresent(type.class))
 			return method.getAnnotation(type.class).value();
 		return new String[0];
 	}
 
 	private Method findBestMethod(List<Method> methods)
 			throws MimeTypeParseException {
 		Method best = null;
 		loop: for (Method method : methods) {
 			if (!isReadable(req.getBody(), method, 0))
 				continue loop;
 			best = method;
 			if (!method.getReturnType().equals(Void.TYPE)) {
 				if (!isAcceptable(method, 0))
 					continue loop;
 			}
 			return best;
 		}
 		return best;
 	}
 
 	private Method findMethod(String method) throws MimeTypeParseException {
 		return findMethod(method, null);
 	}
 
 	private Method findMethod(String req_method, Boolean isResponsePresent)
 			throws MimeTypeParseException {
 		Method method = null;
 		boolean isMethodPresent = false;
 		String name = req.getOperation();
 		WebObject target = req.getRequestedResource();
 		if (name != null) {
 			// lookup method
			List<Method> methods = getOperationMethods(target, req_method,
 					isResponsePresent).get(name);
 			if (methods != null) {
 				isMethodPresent = true;
 				method = findBestMethod(methods);
 			}
 		}
 		if (method == null) {
 			List<Method> methods = new ArrayList<Method>();
 			for (Method m : target.getClass().getMethods()) {
 				method ann = m.getAnnotation(method.class);
 				if (ann == null)
 					continue;
 				if (!Arrays.asList(ann.value()).contains(req_method))
 					continue;
 				if (m.isAnnotationPresent(operation.class))
 					continue;
 				methods.add(m);
 			}
 			if (!methods.isEmpty()) {
 				method = findBestMethod(methods);
 				if (method == null)
 					throw new BadRequest();
 			}
 		}
 		if (method == null) {
 			if (isMethodPresent)
 				throw new BadRequest();
 			if (req.isQueryStringPresent())
 				throw new MethodNotAllowed();
 		}
 		return method;
 	}
 
 	private Entity getParameter(Annotation[] anns, Class<?> ptype, Entity input)
 			throws Exception {
 		String[] names = getParameterNames(anns);
 		if (names == null) {
 			return getValue(anns, input);
 		} else if (names.length == 1 && names[0].equals("*")) {
 			return getValue(anns, req.getQueryString());
 		} else {
 			return getValue(anns, req.getParameter(names));
 		}
 	}
 
 	private Entity getValue(Annotation[] anns, Entity input) throws Exception {
 		for (String uri : getTransforms(anns)) {
 			Method transform = getTransform(uri);
 			if (isReadable(input, transform, 0)) {
 				Object[] args = getParameters(transform, input);
 				return invoke(transform, args, false);
 			}
 		}
 		return input;
 	}
 
 	private String[] getParameterNames(Annotation[] annotations) {
 		for (int i = 0; i < annotations.length; i++) {
 			if (annotations[i] instanceof parameter)
 				return ((parameter) annotations[i]).value();
 		}
 		return null;
 	}
 
 	private Map<String, List<Method>> getPostMethods(WebObject target) {
 		Map<String, List<Method>> map = new HashMap<String, List<Method>>();
 		for (Method m : target.getClass().getMethods()) {
 			method ann = m.getAnnotation(method.class);
 			if (ann == null)
 				continue;
 			put(map, ann.value(), m);
 		}
 		return map;
 	}
 
 	private Method getTransform(String uri) {
 		for (Method m : req.getRequestedResource().getClass().getMethods()) {
 			if (m.isAnnotationPresent(iri.class)) {
 				if (uri.equals(m.getAnnotation(iri.class).value())) {
 					return m;
 				}
 			}
 		}
 		logger.warn("Method not found: {}", uri);
 		return null;
 	}
 
 	private Method getTransformMethod() {
 		return transformMethod;
 	}
 
 	private Method getTransformMethodOf(Method method)
 			throws MimeTypeParseException {
 		if (method == null)
 			return method;
 		if (method.isAnnotationPresent(transform.class)) {
 			for (String uri : method.getAnnotation(transform.class).value()) {
 				Method transform = getTransform(uri);
 				if (isAcceptable(transform, 0))
 					return getTransformMethodOf(transform);
 			}
 		}
 		return method;
 	}
 
 	private String[] getTransforms(Annotation[] anns) {
 		for (Annotation ann : anns) {
 			if (ann.annotationType().equals(transform.class)) {
 				return ((transform) ann).value();
 			}
 		}
 		return new String[0];
 	}
 
 	private boolean isAcceptable(Method method, int depth)
 			throws MimeTypeParseException {
 		if (method == null)
 			return false;
 		if (depth > MAX_TRANSFORM_DEPTH) {
 			logger.error("Max transform depth exceeded: {}", method.getName());
 			return false;
 		}
 		if (method.isAnnotationPresent(transform.class)) {
 			for (String uri : method.getAnnotation(transform.class).value()) {
 				if (isAcceptable(getTransform(uri), ++depth))
 					return true;
 			}
 		}
 		if (method.isAnnotationPresent(type.class)) {
 			for (String media : getTypes(method)) {
 				if (req.isAcceptable(media, method.getReturnType(), method
 						.getGenericReturnType()))
 					return true;
 			}
 			return false;
 		} else {
 			return req.isAcceptable(method.getReturnType(), method
 					.getGenericReturnType());
 		}
 	}
 
 	private boolean isReadable(Entity input, Annotation[] anns, Class<?> ptype,
 			Type gtype, int depth) {
 		String[] names = getParameterNames(anns);
 		if (names != null || ptype.equals(File.class))
 			return true;
 		for (String uri : getTransforms(anns)) {
 			if (isReadable(input, getTransform(uri), ++depth))
 				return true;
 		}
 		return input.isReadable(ptype, gtype);
 	}
 
 	private boolean isReadable(Entity input, Method method, int depth) {
 		if (method == null)
 			return false;
 		if (depth > MAX_TRANSFORM_DEPTH) {
 			logger.error("Max transform depth exceeded: {}", method.getName());
 			return false;
 		}
 		Class<?>[] ptypes = method.getParameterTypes();
 		Annotation[][] anns = method.getParameterAnnotations();
 		Type[] gtypes = method.getGenericParameterTypes();
 		Object[] args = new Object[ptypes.length];
 		for (int i = 0; i < args.length; i++) {
 			if (!isReadable(input, anns[i], ptypes[i], gtypes[i], depth))
 				return false;
 		}
 		return true;
 	}
 
 	private boolean mustReevaluate(Class<?> type) {
 		if (type.isAnnotationPresent(cacheControl.class)) {
 			for (String value : type.getAnnotation(cacheControl.class).value()) {
 				if (value.contains("must-reevaluate"))
 					return true;
 			}
 		} else {
 			if (type.getSuperclass() != null) {
 				if (mustReevaluate(type.getSuperclass()))
 					return true;
 			}
 			for (Class<?> face : type.getInterfaces()) {
 				if (mustReevaluate(face))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	private void put(Map<String, List<Method>> map, String[] keys, Method m) {
 		for (String key : keys) {
 			List<Method> list = map.get(key);
 			if (list == null) {
 				map.put(key, list = new ArrayList<Method>());
 			}
 			list.add(m);
 		}
 	}
 
 }
