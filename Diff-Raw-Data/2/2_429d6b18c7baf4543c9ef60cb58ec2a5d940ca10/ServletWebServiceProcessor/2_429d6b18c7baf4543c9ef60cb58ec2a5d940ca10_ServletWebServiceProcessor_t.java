 package net.sf.javascribe.patterns.servlet;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import net.sf.javascribe.api.AttributeHolder;
 import net.sf.javascribe.api.CodeExecutionContext;
 import net.sf.javascribe.api.ProcessorContext;
 import net.sf.javascribe.api.JavascribeException;
 import net.sf.javascribe.api.JavascribeUtils;
 import net.sf.javascribe.api.VariableType;
 import net.sf.javascribe.api.annotation.Processor;
 import net.sf.javascribe.api.annotation.ProcessorMethod;
 import net.sf.javascribe.api.annotation.Scannable;
 import net.sf.javascribe.api.expressions.ExpressionUtil;
 import net.sf.javascribe.api.expressions.ValueExpression;
 import net.sf.javascribe.langsupport.java.JavaBeanType;
 import net.sf.javascribe.langsupport.java.JavaCode;
 import net.sf.javascribe.langsupport.java.JavaOperation;
 import net.sf.javascribe.langsupport.java.JavaServiceObjectType;
 import net.sf.javascribe.langsupport.java.JavaUtils;
 import net.sf.javascribe.langsupport.java.JavaVariableType;
 import net.sf.javascribe.langsupport.java.jsom.JavascribeVariableTypeResolver;
 import net.sf.javascribe.langsupport.java.jsom.JsomUtils;
 import net.sf.jsom.CodeGenerationException;
 import net.sf.jsom.java5.Java5CodeSnippet;
 import net.sf.jsom.java5.Java5CompatibleCodeSnippet;
 import net.sf.jsom.java5.Java5DeclaredMethod;
 import net.sf.jsom.java5.Java5SourceFile;
 
 @Scannable
 @Processor
 public class ServletWebServiceProcessor {
 
 	public static final String SERVLET_WEB_SERVICE_PKG = "net.sf.javascribe.patterns.servlet.ServletWebService.pkg";
 
 	@ProcessorMethod(componentClass=ServletWebService.class)
 	public void process(ServletWebService webService,ProcessorContext ctx) throws JavascribeException {
 		Java5SourceFile src = null;
 		UrlWebServiceType type = null;
 		
 		ctx.setLanguageSupport("Java");
 
 		try {
 			System.out.println("Processing servlet web service with path '"+webService.getPath()+"'");
 
 			String servicePkg = JavaUtils.findPackageName(ctx, ctx.getRequiredProperty(SERVLET_WEB_SERVICE_PKG));
 			String serviceTypeName = webService.getWebServiceModule();
 
 			// Add to the web service type
 			type = (UrlWebServiceType)ctx.getTypes().getType(serviceTypeName);
 			if (type==null) {
 				type = new UrlWebServiceType();
 				type.setWebService(serviceTypeName);
 				ctx.getTypes().addType(type);
 			}
 			WebUtils.addHttpTypes(ctx);
 
 			String serviceClassName = webService.getWebServiceModule();
 			src = JsomUtils.getJavaFile(servicePkg+'.'+serviceClassName, ctx);
 			Java5DeclaredMethod serviceMethod = null;
 			if (src==null) {
 				src = JsomUtils.createJavaSourceFile(ctx);
 				src.setPackageName(servicePkg);
 				src.getPublicClass().setClassName(serviceClassName);
 				src.getPublicClass().setSuperClass("javax.servlet.http.HttpServlet");
 				serviceMethod = initWebServiceFile(src,ctx);
 				JsomUtils.addJavaFile(src, ctx);
 			}
 			else {
 				serviceMethod = (Java5DeclaredMethod)src.getPublicClass().getDeclaredMethod("service");
 			}
 			Java5CompatibleCodeSnippet methodCode = serviceMethod.getMethodBody();
 			CodeExecutionContext execCtx = new CodeExecutionContext(null,ctx.getTypes());
 
 			// Determine if this request is for this web service, read path parameters into a map if so.
 			methodCode.append("if (!_requestProcessed) {\nString path = request.getPathInfo();\n");
 			methodCode.append("String template = \""+webService.getPath()+"\";\n");
 			methodCode.append("params = retrieveParameters(template,path);\n");
 			methodCode.append("if (params!=null) {\n");
 
 			// Determine if this web service requires a HTTP method.
 			String httpMethod = null;
 			if (webService.getHttpMethod().trim().length()>0) {
 				httpMethod = webService.getHttpMethod();
 				if ((!httpMethod.equalsIgnoreCase("POST")) && (!httpMethod.equalsIgnoreCase("GET"))
 						&& (!httpMethod.equalsIgnoreCase("PUT")) && (!httpMethod.equalsIgnoreCase("DELETE"))) {
 					throw new JavascribeException("URL web service supports HTTP methods POST, PUT, GET, DELETE");
 				}
 				httpMethod = httpMethod.toUpperCase();
 				methodCode.append("if (request.getMethod().equals(\""+httpMethod+"\")) {\n");
 			}
 			
 			methodCode.append("_requestProcessed = true;\n");
 			
 			// Read query parameters into local variables.
 			String params = webService.getQueryParams();
 			if (params.trim().length()>0) {
 				String[] p = params.split(",");
 				for(String arg : p) {
 					String typeName = ctx.getAttributeType(arg);
 					WebUtils.handleQueryParam(ctx, arg, typeName, methodCode, execCtx);
 				}
 			}
 
 			// Read path parameters into local variables.
 			List<String> pathParams = readPathParams(webService.getPath());
 			for(String p : pathParams) {
 				String t = ctx.getAttributeType(p);
 				if (t==null) throw new JavascribeException("Found unrecognized web service path parameter '"+p+"'");
 				appendPathParamCode(ctx,p,t,methodCode,execCtx);
 			}
 			
 			// Read request body.
 			if (webService.getRequestBody().trim().length()>0) {
 				String attr = webService.getRequestBody();
 				String t = ctx.getAttributeType(attr);
 				if (t==null) {
 					throw new JavascribeException("Servlet web servlet request body is an unrecognized attribute");
 				}
 				JavaVariableType ty = (JavaVariableType)ctx.getType(t);
 				if (!(ty instanceof AttributeHolder)) {
 					throw new JavascribeException("Request body of a servlet web service must be a data object");
 				}
 				if (execCtx.getVariableType(t)!=null) {
 					throw new JavascribeException("Unable to process servlet WS request body - an attribute with this name already exists in the current code execution context");
 				}
 				methodCode.addImport(ty.getImport());
 				methodCode.append(ty.declare(attr, execCtx).getCodeText());
 				methodCode.addImport("org.codehaus.jackson.map.ObjectMapper");
 				methodCode.append("ObjectMapper _objectMapper = new ObjectMapper();\n");
 				methodCode.append(attr+" = _objectMapper.readValue(request.getReader(),"+ty.getClassName()+".class);\n");
 			}
 
 			// Read user session data if it is there.
 			String sessionDataType = webService.getSessionDataType();
 			if (sessionDataType.trim().length()>0) {
 				VariableType vtype = ctx.getTypes().getType(sessionDataType);
 				if (vtype==null) throw new JavascribeException("Could not find session data type '"+sessionDataType+"'");
 				if (!(vtype instanceof JavaBeanType)) throw new JavascribeException("Type '"+sessionDataType+"' is not a data object");
 				JavaBeanType dataObjectType = (JavaBeanType)vtype;
 				String sessionDataVar = JavascribeUtils.getLowerCamelName(sessionDataType);
 				JsomUtils.merge(methodCode, (JavaCode)dataObjectType.declare(sessionDataVar,execCtx));
 				methodCode.addImport(dataObjectType.getImport());
 				methodCode.append(sessionDataVar+" = ("+dataObjectType.getClassName()+")request.getSession(true).getAttribute(\""+sessionDataVar+"\");\n");
 				execCtx.addVariable(sessionDataVar, sessionDataType);
 			}
 
 			// Call service layer
 			String resultName = null;
 			String serviceResultType = null; // For putting into the web service variable type.
 			if (webService.getService().trim().length()>0) {
 				String objName = JavascribeUtils.getObjectName(webService.getService());
 				String ruleName = JavascribeUtils.getRuleName(webService.getService());
 				String objInst = JavascribeUtils.getLowerCamelName(objName);
 				JavaBeanType resultType = null;
 
 				JavaServiceObjectType obj = (JavaServiceObjectType)ctx.getType(objName);
 				if (obj==null) {
					throw new JavascribeException("Couldn't find business object type '"+objName+"'");
 				}
 				JavaOperation op = obj.getMethod(ruleName);
 				if (op==null) {
 					throw new JavascribeException("Couldn't find business rule '"+objName+"."+ruleName+"'");
 				}
 				JsomUtils.merge(methodCode, (JavaCode)obj.declare(objInst));
 				JsomUtils.merge(methodCode, (JavaCode)obj.instantiate(objInst,null));
 				resultName = op.getReturnType();
 				if (resultName!=null) {
 					resultType = (JavaBeanType)ctx.getTypes().getType(resultName);
 					JsomUtils.merge(methodCode, (JavaCode)resultType.declare("serviceResult",execCtx));
 					execCtx.addVariable("serviceResult", resultName);
 					methodCode.append(JavaUtils.callJavaOperation("serviceResult", objInst, op, execCtx, null));
 				} else {
 					methodCode.append(JavaUtils.callJavaOperation(null, objInst, op, execCtx, null));
 				}
 			}
 
 			// Handle web service response
 			String format = webService.getReturnFormat();
 			String returnValue = webService.getReturnValue();
 
 			if (format.trim().length()==0) {
 				throw new JavascribeException("ReturnFormat attribute is required for servlet web service");
 			}
 			if (format.equalsIgnoreCase("json")) {
 				// Set return content type, serialize result to JSON via Jackson API.
 				methodCode.append("response.setContentType(\"application/json\");\n");
 				methodCode.append("java.io.PrintWriter out = response.getWriter();\n");
 
 				if ((returnValue==null) || (returnValue.trim().length()==0)) {
 					methodCode.append("out.print(\"{ \"status\" : \"0\" }\"");
 				} else {
 					methodCode.addImport("org.codehaus.jackson.JsonFactory");
 					methodCode.addImport("org.codehaus.jackson.map.ObjectMapper");
 					methodCode.addImport("org.codehaus.jackson.JsonGenerator");
 					methodCode.append("JsonFactory factory = new JsonFactory(new ObjectMapper());\n");
 					methodCode.append("JsonGenerator generator = factory.createJsonGenerator(out);\n");
 					methodCode.append("generator.writeObject(");
 					ValueExpression expr = ExpressionUtil.buildValueExpression(returnValue, "object", execCtx);
 					serviceResultType = expr.getVarReferenceExpressionEntry(0).getType().getName();
 					methodCode.append(ExpressionUtil.getEvaluatedExpression(expr, execCtx));
 					methodCode.append(");\ngenerator.flush();\n");
 				}
 
 				methodCode.append("out.flush();\n");
 			} else {
 				throw new CodeGenerationException("Found unsupported format '"+format+"' for servlet web service");
 			}
 
 			// Done
 			if (httpMethod!=null) {
 				methodCode.append("}\n");
 			}
 			methodCode.append("}\n}\n// End of WebService "+webService.getPath()+'\n');
 
 			// Add web service type.
 			addType(webService,serviceResultType,type);
 
 			modifyWebXml(ctx,webService,servicePkg,serviceClassName);
 		} catch(CodeGenerationException e) {
 			throw new JavascribeException("JSOM exception while processing component",e);
 		}
 	}
 
 	private void appendPathParamCode(ProcessorContext ctx,String param,String typeName,Java5CompatibleCodeSnippet code,CodeExecutionContext execCtx) throws CodeGenerationException,JavascribeException {
 		JavaVariableType type = (JavaVariableType)ctx.getType(typeName);
 
 		if (type==null) throw new JavascribeException("Found path parameter of unsupported type '"+typeName+"'");
 
 		JsomUtils.merge(code, (JavaCode)type.declare(param,execCtx));
 		execCtx.addVariable(param, typeName);
 		if (typeName.equals("string")) {
 			code.append(param+" = params.get(\""+param+"\");\n");
 			code.append("if (("+param+"==null) || ("+param+".trim().length()==0)) "+param+" = null;\n");
 		}
 		else if (typeName.equals("integer")) {
 			code.append("if ((request.getParameter(\""+param+"\")!=null) && (request.getParameter(\""+param+"\").trim().length()>0)) {\n");
 			code.append("try {\n");
 			code.append(param+" = Integer.parseInt(request.getParameter(\""+param+"\"));\n");
 			code.append("} catch(Exception e) { }\n}\n");
 		}
 		else {
 			// No other parameter types supported yet
 			throw new JavascribeException("Found a webServlet input parameter '"+param+"' of unsupported type '"+typeName+"'");
 		}
 	}
 
 	private List<String> readPathParams(String path) {
 		List<String> ret = new ArrayList<String>();
 		int index = path.indexOf("${");
 		int end = 0;
 		String eval = null;
 
 		while(index>=0) {
 			end = path.indexOf('}', index+1);
 			eval = path.substring(index+2, end);
 			index = path.indexOf("${", end);
 			ret.add(eval);
 		}
 
 		return ret;
 	}
 
 	private void modifyWebXml(ProcessorContext ctx,ServletWebService webService,String pkg,String className) throws JavascribeException {
 		boolean exists = WebUtils.webXmlHasServlet(ctx, webService.getWebServiceModule());
 		WebXmlFile webXml = WebUtils.getWebXml(ctx);
 		String mod = webService.getWebServiceModule();
 
 		if (!exists) {
 			webXml.addServlet(mod, mod, pkg+'.'+className);
 			webXml.addServletMapping(mod, '/'+mod+"/*");
 		}
 
 		// Add filter mappings for this particular module/service
 		// Look at the component first, then look for the context property.
 		String filters = webService.getFilters();
 
 		if ((filters==null) || (filters.trim().length()==0)) {
 			filters = ctx.getProperty("servletWebService.defaultFilters");
 		}
 
 		if ((filters!=null) && (filters.trim().length()>0)) {
 			String f[] = filters.split(",");
 			for(String s : f) {
 				webXml.addFilterMapping(s, '/'+mod+webService.getPath());
 			}
 		}
 	}
 
 	private Java5DeclaredMethod initWebServiceFile(Java5SourceFile src,ProcessorContext ctx) throws CodeGenerationException {
 		Java5DeclaredMethod serviceMethod = null;
 		
 		serviceMethod = new Java5DeclaredMethod(new JavascribeVariableTypeResolver(ctx));
 		serviceMethod.setMethodName("service");
 		src.getPublicClass().addMethod(serviceMethod);
 		Java5CodeSnippet methodCode = new Java5CodeSnippet();
 
 		serviceMethod.addArg("HttpServletRequest", "request");
 		serviceMethod.addArg("HttpServletResponse", "response");
 		serviceMethod.addThrownException("ServletException");
 		serviceMethod.addThrownException("IOException");
 		methodCode.append("boolean _requestProcessed = false;\n");
 		methodCode.append("Map<String,String> params = null;\n");
 		methodCode.addImport("java.util.Map");
 		serviceMethod.setMethodBody(methodCode);
 
 		Java5DeclaredMethod utilMethod = new Java5DeclaredMethod(new JavascribeVariableTypeResolver(ctx));
 		utilMethod.addArg("string", "template");
 		utilMethod.addArg("string", "path");
 		utilMethod.setMethodName("retrieveParameters");
 		utilMethod.setReturnType("map-string");
 		Java5CodeSnippet code = new Java5CodeSnippet();
 		code.append(RETRIEVE_PARAMETER_CODE);
 		code.addImport("java.util.HashMap");
 		utilMethod.setMethodBody(code);
 		src.getPublicClass().addMethod(utilMethod);
 		
 		return serviceMethod;
 	}
 
 	private void addType(ServletWebService webService,String serviceResultType,UrlWebServiceType type) throws CodeGenerationException {
 		SingleUrlService srv = new SingleUrlService();
 
 		srv.setPath(webService.getWebServiceModule());
 		String str = webService.getQueryParams();
 		srv.setReturnType(serviceResultType);
 
 		if ((str!=null) && (str.trim().length()>0)) {
 			String[] params = str.split(",");
 			for(String s : params) {
 				if (s.indexOf(':')>=0) {
 					throw new CodeGenerationException("Servlet web service only supports parameters that are already defined attributes in the application.");
 				}
 				srv.getQueryParams().add(s);
 			}
 		}
 
 		if (webService.getReturnFormat()==null) {
 			throw new CodeGenerationException("No return format specified for service "+webService.getWebServiceModule()+"/"+webService.getPath());
 		}
 		if (!webService.getReturnFormat().equals("json")) {
 			throw new CodeGenerationException("Web Service currently only supports return format 'json'");
 		}
 		srv.setReturnFormat(webService.getReturnFormat());
 		String path = '/'+webService.getWebServiceModule()+webService.getPath();
 		srv.setPath(path);
 		type.getServices().put(path,srv);
 	}
 
 	private static final String RETRIEVE_PARAMETER_CODE = 
 			"		Map<String,String> ret = new HashMap<String,String>();\n"+
 					"		int templateStart = 0,pathStart = 0; // Place in template and path where we are scanning from\n"+
 					"		boolean done = false;\n"+
 					"\n"+
 					"		while(!done) {\n"+
 					"			int paramIndex = template.indexOf(\"${\",templateStart);\n"+
 					"			if (paramIndex==templateStart) {\n"+
 					"				int paramEnd = template.indexOf('}',paramIndex);\n"+
 					"				if (paramEnd < 0) throw new RuntimeException(\"Found invalid web service template string '\"+template+\"'\");\n"+
 					"				String paramName = template.substring(paramIndex+2, paramEnd);\n"+
 					"				String paramValue = null;\n"+
 					"				if (paramEnd == template.length()-1) {\n"+
 					"					// This is the end of the template... The parameter string is the rest of the path\n"+
 					"					paramValue = path.substring(pathStart);\n"+
 					"					done = true;\n"+
 					"				} else {\n"+
 					"					char end = template.charAt(paramEnd+1);\n"+
 					"					int endI = path.indexOf(end, pathStart);\n"+
 					"					if (endI<0) return null;\n"+
 					"					paramValue = path.substring(pathStart, endI);\n"+
 					"					templateStart = paramEnd+1;\n"+
 					"					pathStart = endI;\n"+
 					"				}\n"+
 					"				ret.put(paramName, paramValue);\n"+
 					"			} else {\n"+
 					"				// Compare next segment of template to next segment of path\n"+
 					"				String templateCompare = null;\n"+
 					"				if (paramIndex<0) {\n"+
 					"					// Comparing the end of the path to the end of the template\n"+
 					"					templateCompare = template.substring(templateStart);\n"+
 					"					done = true;\n"+
 					"				} else {\n"+
 					"					templateCompare = template.substring(templateStart,paramIndex);\n"+
 					"				}\n"+
 					"				if (path.indexOf(templateCompare, pathStart)==pathStart) {\n"+
 					"					// match.  If paramIndex < 0, at end of string so return\n"+
 					"					if (!done) {\n"+
 					"						templateStart = paramIndex;\n"+
 					"						pathStart += templateCompare.length();\n"+
 					"					}\n"+
 					"                   else { // Check ends of path and template\n"+
 					"                       if (!template.substring(templateStart).equals(path.substring(pathStart))) {\n"+
 					"                           return null;\n"+
 					"                       }\n"+
 					"                   }\n"+
 					"				} else {\n"+
 					"					// fail.\n"+
 					"					return null;\n"+
 					"				}\n"+
 					"			}\n"+
 					"		}\n"+
 					"\n"+
 					"		return ret;";
 
 }
 
 /*
 					} else { // Check end of paths
 						if (!template.substring(templateStart).equals(path.substring(pathStart))) {
 							return null;
 						}
 					}
 
 
  */
