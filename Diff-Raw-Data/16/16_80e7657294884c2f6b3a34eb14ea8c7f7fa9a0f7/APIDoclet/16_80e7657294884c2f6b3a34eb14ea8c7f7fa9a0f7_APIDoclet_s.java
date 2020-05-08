 package com.pehrs.spring.api.doc;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeSet;
 
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.PatternLayout;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.pehrs.spring.api.doc.model.ClassDesc;
 import com.pehrs.spring.api.doc.model.MethodDesc;
 import com.pehrs.spring.api.doc.model.ParameterDesc;
 import com.pehrs.spring.api.doc.model.PathInfo;
 import com.sun.javadoc.AnnotationDesc;
 import com.sun.javadoc.AnnotationDesc.ElementValuePair;
 import com.sun.javadoc.AnnotationValue;
 import com.sun.javadoc.ClassDoc;
 import com.sun.javadoc.MethodDoc;
 import com.sun.javadoc.Parameter;
 import com.sun.javadoc.ProgramElementDoc;
 import com.sun.javadoc.RootDoc;
 import com.sun.javadoc.Tag;
 
 import freemarker.cache.ClassTemplateLoader;
 import freemarker.cache.FileTemplateLoader;
 import freemarker.cache.TemplateLoader;
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 
 public class APIDoclet {
 
 	static Logger log = LoggerFactory.getLogger(APIDoclet.class);
 
 	final static String CONTROLLER_TEMPLATE_FILE = "controller.ftl";
 	final static String API_TEMPLATE_FILE = "api.ftl";
 
 	public static boolean start(RootDoc root) {
 
 		initLogging();
 
 		// JacksonJSONGenerator jsonGenerator = new JacksonJSONGenerator();
 		JSONSampleGenerator jsonGenerator = new JSONSampleGenerator(
 				new JSONTextDecorator());
 
 		Map<String, ClassDesc> model = new HashMap<String, ClassDesc>();
 
 		ClassDoc[] classes = root.classes();
 		for (ClassDoc classDoc : classes) {
 
 			AnnotationDesc controllerDoc = getControllerAnnotation(classDoc);
 			// AnnotationDesc reqMapDoc = getReqeustMappingAnnotation(classDoc);
 			if (controllerDoc != null) {
 				log.debug("CLASS: " + classDoc.name());
 
 				ClassDesc controller = new ClassDesc();
 				controller.setName(classDoc.name());
 				controller.setPkgName(classDoc.containingPackage().name());
 				model.put(classDoc.name(), controller);
 
 				log.debug("  controller: " + controllerDoc);
 				// xml.append(ODTGenerator.odfBody(classDoc.qualifiedName()));
 
 				String comment = classDoc.getRawCommentText();
 				log.debug("  comment=" + comment);
 				controller.setComment(comment);
 
 				String urlRoot = "";
 
 				AnnotationDesc controllerReqMappingDoc = getReqeustMappingAnnotation(classDoc);
 				log.debug("  requestmapping:" + controllerReqMappingDoc);
 				if (controllerReqMappingDoc != null) {
 					urlRoot = getStrippedElementValue(controllerReqMappingDoc,
 							"value");
 				}
 
 				log.debug("  url.root=" + urlRoot);
 				controller.setUrlRoot(urlRoot);
 
 				for (MethodDoc methodDoc : classDoc.methods()) {
 					AnnotationDesc methodReqMapDoc = getReqeustMappingAnnotation(methodDoc);
 					if (methodReqMapDoc != null) {
 
 						AnnotationValue reqMethodValue = getElementAnnotationValue(
 								methodReqMapDoc, "method");
 						if (reqMethodValue != null) {
 							Object[] reqMethods = (Object[]) reqMethodValue
 									.value();
 							for (Object springReqMethod : reqMethods) {
 								String reqMethod = getHttpMethodName(""
 										+ springReqMethod);
 
 								String methodUrl = getStrippedElementValue(
 										methodReqMapDoc, "value");
 								if (methodUrl == null) {
 									methodUrl = "";
 								}
 								log.debug("  METHOD: " + methodDoc.name() + " "
										+ urlRoot + methodUrl + "  "
										+ reqMethod);
 								MethodDesc method = new MethodDesc();
 								controller.addMethod(method);
 								method.setName(methodDoc.name());
 								method.setRequestMappingUrl(methodUrl);
 								method.setRequestMappingMethod(reqMethod);
 
 								// Get Parameter Types
 								Method classMethod = getMethod(classDoc,
 										methodDoc);
 								Parameter[] parameterDocs = methodDoc
 										.parameters();
 
 								@SuppressWarnings("rawtypes")
 								Class methodReturnType = classMethod
 										.getReturnType();
 
 								java.lang.reflect.Type retType = classMethod
 										.getGenericReturnType();
 								// if(retType instanceof ParameterizedType) {
 								// ParameterizedType pt =
 								// (ParameterizedType)retType;
 								// log.debug("====================="+Arrays.toString(pt.getActualTypeArguments()));
 								// }
 
 								String methodReturnTypeJson = jsonGenerator
 										.generateJSONSample(methodReturnType,
 												retType);
 								log.debug("      JSON Response Sample:\n"
 										+ methodReturnTypeJson);
 								method.setResponseJSONSample(methodReturnTypeJson);
 								method.setCommentText(methodDoc
 										.getRawCommentText());
 
 								for (Parameter jdParameter : parameterDocs) {
 									if (jdParameter.typeName().equals(
 											"HttpServletRequest")
 											|| jdParameter.typeName().equals(
 													"HttpServletResponse")) {
 										// IGnore the HTTP Request/response
 										// arguments
 										// for the method
 										continue;
 									}
 
 									int parameterIndex = 0;
 									for (; parameterIndex < parameterDocs.length; parameterIndex++) {
 										if (parameterDocs[parameterIndex] == jdParameter) {
 											break;
 										}
 									}
 
 									@SuppressWarnings("rawtypes")
 									Class parameterClass = getParameterClass(
 											classMethod, jdParameter,
 											parameterDocs);
 									log.debug("    parameter.class="
 											+ parameterClass.getName());
 									// java.lang.reflect.Type parameterType =
 									// method.getGenericParameterTypes()[parameterIndex];
 									// log.debug("    parameter.type="+parameterType);
 
 									ParameterDesc parameter = new ParameterDesc();
 									method.addParameter(parameter);
 									parameter.setName(jdParameter.name());
 									parameter.setTypeName(jdParameter
 											.typeName());
 
 									AnnotationDesc pathVariable = getPathVariableAnnotation(jdParameter);
 									if (pathVariable != null) {
 										parameter.setPathVariable(true);
 										parameter
 												.setPathVariableValue(getElementValue(
 														pathVariable, "value"));
 									}
 
 									AnnotationDesc reqParam = getRequestParamAnnotation(jdParameter);
 									if (reqParam != null) {
 										boolean required = false;
 										// @RequestParam(defaultValue="id",
 										// required=false, value="orderBy")
 										// final
 										// String
 										// orderByColumn,
 										String requiredStr = getElementValue(
 												reqParam, "required");
 										String reqP_defaultValue = getElementValue(
 												reqParam, "defaultValue");
 										String reqP_value = getElementValue(
 												reqParam, "value");
 										if (reqP_value == null
 												|| reqP_value.length() == 0) {
 											reqP_value = jdParameter.name();
 										}
 										if (requiredStr == null) {
 											required = false;
 										} else {
 											required = Boolean
 													.parseBoolean(requiredStr);
 										}
 										parameter.setRequestParam(true);
 										parameter
 												.setRequestParamRequired(required);
 										parameter
 												.setRequestParamDefaultValue(reqP_defaultValue);
 										parameter
 												.setRequestParamValue(reqP_value);
 										log.debug("    request parameter: "
 												+ jdParameter.typeName()
 												+ " "
 												+ jdParameter.name()
 												+ " "
 												+ (reqParam == null ? ""
 														: " required="
 																+ required));
 									}
 
 									AnnotationDesc requestBodyAnnotation = getRequestBodyAnnotation(jdParameter);
 									boolean parameterIsBodyReq = requestBodyAnnotation != null;
 									log.debug("    parameter: "
 											+ jdParameter.typeName()
 											+ " "
 											+ jdParameter.name()
 											+ " "
 											+ (parameterIsBodyReq ? " BODY REQUEST"
 													: ""));
 									if (parameterIsBodyReq) {
 										String json = jsonGenerator
 												.generateJSONSample(
 														parameterClass,
 														parameterClass
 																.getGenericSuperclass());
 										log.debug("      JSON Request Sample:\n"
 												+ json);
 
 										method.setRequestJSONSample(json);
 										parameter.setRequestBody(true);
 										parameter
 												.setRequestBodyJSONSample(json);
 									}
 									String paramDoc = "";
 									for (Tag tag : methodDoc.tags()) {
 										log.debug(">>>>>>>>> METHOD TAG: name="
 												+ tag.name() + ", text="
 												+ tag.text());
 										if ("@param".equals(tag.name())) {
 											String txt = tag.text();
 											int index = txt.indexOf(' ');
 											if (index != -1) {
 												String pName = txt.substring(0,
 														index).trim();
 												if (jdParameter.name().equals(
 														pName)) {
 													paramDoc = ": "
 															+ txt.substring(index);
 													break;
 												}
 											}
 										}
 									}
 									log.debug("    paramDoc=" + paramDoc);
 								}
 
 							}
 						}
 					}
 				}
 			}
 		}
 
 		log.debug("============================");
 		log.debug("" + model);
 		log.debug("============================");
 
 		// Generate result
 		try {
 			File targetDir = new File(System.getProperty("target"));
 			targetDir.mkdirs();
 
 			/*
 			 * for (ClassDesc controller : model.values()) { Map<String, Object>
 			 * datamodel = new HashMap<String, Object>();
 			 * datamodel.put("controller", controller);
 			 * datamodel.put("pom_group_id", System.getProperty("pom.group.id",
 			 * "")); datamodel.put("pom_artifact_id",
 			 * System.getProperty("pom.artifact.id", ""));
 			 * datamodel.put("pom_name", System.getProperty("pom.name", ""));
 			 * datamodel.put("pom_version", System.getProperty("pom.version",
 			 * "")); fmGenerateAPIHtml4Controller(datamodel, targetDir); }
 			 */
 
 			Map<String, Object> datamodel = new HashMap<String, Object>();
 			datamodel.put("model", model);
 			datamodel.put("pom_group_id",
 					System.getProperty("pom.group.id", ""));
 			datamodel.put("pom_artifact_id",
 					System.getProperty("pom.artifact.id", ""));
 			datamodel.put("pom_name", System.getProperty("pom.name", ""));
 			datamodel.put("pom_version", System.getProperty("pom.version", ""));
 			fmGenerateAPIHtml(datamodel, targetDir);
 
 		} finally {
 
 		}
 
 		return true;
 	}
 
 	private static String getHttpMethodName(String reqMethod) {
 		if (reqMethod
 				.equals("org.springframework.web.bind.annotation.RequestMethod.GET")) {
 			return "GET";
 		}
 		if (reqMethod
 				.equals("org.springframework.web.bind.annotation.RequestMethod.POST")) {
 			return "POST";
 		}
 		if (reqMethod
 				.equals("org.springframework.web.bind.annotation.RequestMethod.DELETE")) {
 			return "DELETE";
 		}
 		if (reqMethod
 				.equals("org.springframework.web.bind.annotation.RequestMethod.PUT")) {
 			return "PUT";
 		}
 		if (reqMethod
 				.equals("org.springframework.web.bind.annotation.RequestMethod.HEAD")) {
 			return "HEAD";
 		}
 		if (reqMethod
 				.equals("org.springframework.web.bind.annotation.RequestMethod.OPTIONS")) {
 			return "OPTIONS";
 		}
 		if (reqMethod
 				.equals("org.springframework.web.bind.annotation.RequestMethod.TRACE")) {
 			return "TRACE";
 		}
 		return "GET";
 	}
 
 	private static void initLogging() {
 		org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
 		Level level = Level.toLevel(System.getProperty("logging.level", ""
 				+ Level.DEBUG));
 		root.setLevel(level);
 		root.removeAllAppenders();
 		root.addAppender(new ConsoleAppender(new PatternLayout(
 				PatternLayout.DEFAULT_CONVERSION_PATTERN)));
 	}
 
 	@SuppressWarnings("rawtypes")
 	private static Class getParameterClass(Method method, Parameter parameter,
 			Parameter[] parameterDocs) {
 
 		int i = 0;
 		for (; i < parameterDocs.length; i++) {
 			if (parameterDocs[i] == parameter) {
 				break;
 			}
 		}
 		return method.getParameterTypes()[i];
 	}
 
 	@SuppressWarnings("rawtypes")
 	private static Method getMethod(ClassDoc classDoc, MethodDoc methodDoc) {
 		try {
 			Class klass = Class.forName(classDoc.qualifiedName());
 
 			for (Method method : klass.getMethods()) {
 				if (methodDoc.name().equals(method.getName())) {
 					if (method.getParameterTypes().length == methodDoc
 							.parameters().length) {
 						return method;
 					}
 				}
 			}
 
 			return null;
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private static String getElementValue(AnnotationDesc desc, String key) {
		return "" + getElementAnnotationValue(desc, key);
 	}
 
 	private static AnnotationValue getElementAnnotationValue(
 			AnnotationDesc desc, String key) {
 		if (desc == null) {
 			return null;
 		}
 		for (ElementValuePair val : desc.elementValues()) {
 			String name = val.element().name();
 			if (key.equals(name)) {
 				return val.value();
 			}
 		}
 		return null;
 	}
 
 	private static String getStrippedElementValue(AnnotationDesc desc,
 			String key) {
 		String tmp = getElementValue(desc, key);
 		if (tmp == null) {
 			return null;
 		}
 		return tmp.substring(1, tmp.length() - 1);
 	}
 
 	private static AnnotationDesc getPathVariableAnnotation(Parameter doc) {
 		for (AnnotationDesc annotation : doc.annotations()) {
 			if ("org.springframework.web.bind.annotation.PathVariable"
 					.equals(annotation.annotationType().qualifiedName())) {
 				return annotation;
 			}
 		}
 		return null;
 	}
 
 	private static AnnotationDesc getRequestParamAnnotation(Parameter doc) {
 		for (AnnotationDesc annotation : doc.annotations()) {
 			if ("org.springframework.web.bind.annotation.RequestParam"
 					.equals(annotation.annotationType().qualifiedName())) {
 				return annotation;
 			}
 		}
 		return null;
 	}
 
 	private static AnnotationDesc getRequestBodyAnnotation(Parameter doc) {
 		for (AnnotationDesc annotation : doc.annotations()) {
 			if ("org.springframework.web.bind.annotation.RequestBody"
 					.equals(annotation.annotationType().qualifiedName())) {
 				return annotation;
 			}
 		}
 		return null;
 	}
 
 	private static AnnotationDesc getReqeustMappingAnnotation(
 			ProgramElementDoc doc) {
 		for (AnnotationDesc annotation : doc.annotations()) {
 			if ("org.springframework.web.bind.annotation.RequestMapping"
 					.equals(annotation.annotationType().qualifiedName())) {
 				return annotation;
 			}
 		}
 		return null;
 	}
 
 	private static AnnotationDesc getControllerAnnotation(ProgramElementDoc doc) {
 		for (AnnotationDesc annotation : doc.annotations()) {
 			if ("org.springframework.stereotype.Controller".equals(annotation
 					.annotationType().qualifiedName())) {
 				return annotation;
 			}
 		}
 		return null;
 	}
 
 	static Configuration cfg = new Configuration();
 	static {
 		String templateDir = System.getProperty("template.dir");
 		setTemplateDir(templateDir);
 	}
 
 	public static void setTemplateDir(String dir) {
 		try {
 			String templateDir = dir;
 			if (templateDir == null) {
 				TemplateLoader loader = new ClassTemplateLoader(
 						APIDoclet.class, "/com/pehrs/spring/api/doc/templates");
 				cfg.setTemplateLoader(loader);
 			} else {
 				cfg.setTemplateLoader(new FileTemplateLoader(new File(
 						templateDir)));
 			}
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	static void fmGenerateAPIHtml4Controller(Map<String, Object> datamodel,
 			File targetDir) {
 
 		String classPrefix = System.getProperty("class.prefix", "");
 		String classSuffix = System.getProperty("class.suffix", "");
 		String fileExt = System.getProperty("file.ext", ".html");
 
 		FileWriter out = null;
 		try {
 			ClassDesc controller = (ClassDesc) datamodel.get("controller");
 
 			String pkgPath = controller.getPkgName().replace('.', '/');
 
 			File pkgDir = new File(targetDir.getAbsolutePath() + "/" + pkgPath);
 			pkgDir.mkdirs();
 
 			String path = targetDir.getAbsolutePath() + "/" + pkgPath + "/"
 					+ classPrefix + controller.getName() + classSuffix
 					+ fileExt;
 			log.debug("Generating " + path);
 			out = new FileWriter(path);
 
 			Template tpl = cfg.getTemplate(CONTROLLER_TEMPLATE_FILE);
 
 			// OutputStreamWriter output = new OutputStreamWriter(System.out);
 
 			tpl.process(datamodel, out);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} catch (TemplateException e) {
 			throw new RuntimeException(e);
 		} finally {
 
 			if (out != null) {
 				try {
 					out.close();
 				} catch (IOException e) {
 					throw new RuntimeException(e);
 				}
 			}
 		}
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private static void fmGenerateAPIHtml(Map<String, Object> datamodel,
 			File targetDir) {
 
 		String fileExt = System.getProperty("file.ext", ".html");
 
 		FileWriter out = null;
 		try {
 			Map<String, ClassDesc> model = (Map<String, ClassDesc>) datamodel
 					.get("model");
 
 			TreeSet<ClassDesc> sortedControllers = new TreeSet<ClassDesc>(
 					model.values());
 			datamodel.put("controllers", sortedControllers.iterator());
 
 			// api_${method.getRequestMappingMethod()}_${method.getName()}
 
 			TreeSet<PathInfo> paths = new TreeSet<PathInfo>();
 			for (ClassDesc controller : model.values()) {
 				String urlRoot = controller.getUrlRoot();
 				for (MethodDesc method : controller.getMethods()) {
 					String requestPath = urlRoot
 							+ method.getRequestMappingUrl();
 
 					String methodId = "api_" + controller.getJSId() + "_"
 							+ method.getRequestMappingMethod() + "_"
 							+ method.getName();
 
 					PathInfo info = new PathInfo(controller, method,
 							requestPath, method.getRequestMappingMethod(),
 							controller.getPkgNameAsPath() + "/"
 									+ controller.getName() + fileExt, methodId);
 					paths.add(info);
 
 					controller.getPathInfos().add(info);
 
 					log.debug("PATH: " + info);
 				}
 			}
 			datamodel.put("paths", paths.iterator());
 
 			String path = targetDir.getAbsolutePath() + "/"
 					+ datamodel.get("pom_group_id") + "."
 					+ datamodel.get("pom_artifact_id") + "-"
 					+ datamodel.get("pom_version") + fileExt;
 			log.debug("Generating " + path);
 			out = new FileWriter(path);
 
 			Template tpl = cfg.getTemplate(API_TEMPLATE_FILE);
 			tpl.process(datamodel, out);
 
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		} catch (TemplateException e) {
 			throw new RuntimeException(e);
 		} finally {
 
 			if (out != null) {
 				try {
 					out.close();
 				} catch (IOException e) {
 					throw new RuntimeException(e);
 				}
 			}
 		}
 
 	}
 
 }
