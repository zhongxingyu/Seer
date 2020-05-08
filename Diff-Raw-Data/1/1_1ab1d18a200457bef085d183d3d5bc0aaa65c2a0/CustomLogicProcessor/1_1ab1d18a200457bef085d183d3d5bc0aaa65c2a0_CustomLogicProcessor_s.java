 package net.sf.javascribe.patterns.custom;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import net.sf.javascribe.api.CodeExecutionContext;
 import net.sf.javascribe.api.JavascribeException;
 import net.sf.javascribe.api.JavascribeUtils;
 import net.sf.javascribe.api.ProcessorContext;
 import net.sf.javascribe.api.VariableType;
 import net.sf.javascribe.api.annotation.Processor;
 import net.sf.javascribe.api.annotation.ProcessorMethod;
 import net.sf.javascribe.api.annotation.Scannable;
 import net.sf.javascribe.langsupport.java.Injectable;
 import net.sf.javascribe.langsupport.java.JavaCode;
 import net.sf.javascribe.langsupport.java.JavaOperation;
 import net.sf.javascribe.langsupport.java.JavaServiceObjectType;
 import net.sf.javascribe.langsupport.java.JavaUtils;
 import net.sf.javascribe.langsupport.java.JavaVariableType;
 import net.sf.javascribe.langsupport.java.LocatedJavaServiceObjectType;
 import net.sf.javascribe.langsupport.java.jsom.JavascribeVariableTypeResolver;
 import net.sf.javascribe.langsupport.java.jsom.JsomUtils;
 import net.sf.jsom.CodeGenerationException;
 import net.sf.jsom.java5.Java5ClassConstructor;
 import net.sf.jsom.java5.Java5CodeSnippet;
 import net.sf.jsom.java5.Java5DeclaredMethod;
 import net.sf.jsom.java5.Java5SourceFile;
 
 import org.apache.log4j.Logger;
 
 @Scannable
 @Processor
 public class CustomLogicProcessor {
 
 	private static final Logger log = Logger.getLogger(CustomLogicProcessor.class);
 	
 	private static final String CUSTOM_LOGIC_PKG = "net.sf.javascribe.patterns.custom.CustomLogic.pkg";
 
 	private CustomLogicObjectType processJavaFile(File f,ProcessorContext ctx,CustomLogic component,String locatorClassName) throws IOException,JavascribeException {
 		String className = null;
 		String pkg = component.getPkg();
 
 		className = f.getName().substring(0,f.getName().length()-5);
 		CustomLogicObjectType ret = new CustomLogicObjectType(locatorClassName,className,pkg,className);
 		ret.setClassName(className);
 		ret.setPkg(pkg);
 		ctx.addAttribute(JavascribeUtils.getLowerCamelName(className), className);
 
 		log.debug("Processing Custom Logic class '"+className+"'");
 
 		StringBuilder fileContents = readFileContents(f);
 		int index = fileContents.indexOf("public class")+1;
 
 		index = fileContents.indexOf("public ");
 		index = fileContents.indexOf("public ",index+1);
 		while(index>0) {
 			processDeclarationAtLocation(fileContents,index,ret,ctx,component);
 			index = fileContents.indexOf("public ",index+1);
 		}
 
 		return ret;
 	}
 
 	private void processDeclarationAtLocation(StringBuilder s,int index,JavaServiceObjectType obj,ProcessorContext ctx,CustomLogic component) throws JavascribeException {
 		int end = 0;
 		end = s.indexOf("{", index+1);
 
 		if (end < 0) {
 			return;
 		} else if (s.indexOf("public void set", index-1)==index) {
 			int i = s.indexOf("(",index+1);
 			String dep = s.substring(index+15,i);
 			obj.addDependancy(dep);
 		} else {
 			// This is an operation
 			JavaOperation op = new JavaOperation();
 			String name = null;
 			String line = s.substring(index, end);
 			if (line.indexOf(';')>0) return;
 			op.setReturnType(findReturnType(line,ctx));
 			name = findName(line);
 			if (name!=null) {
 				op.setName(findName(line));
 				addParameters(op,line,ctx,component);
 			} else return;
 			obj.addMethod(op);
 		}
 	}
 
 	private String findReturnType(String line,ProcessorContext ctx) {
 		int begin = 7;
 		int end = line.indexOf(' ', begin+1);
 		String name = line.substring(begin, end);
 		String ret = findType(name,ctx);
 
 		return ret;
 	}
 
 	private String findType(String typeString,ProcessorContext ctx) {
 		String ret = null;
 
 		if (typeString.startsWith("List<")) {
 			if (typeString.startsWith("List<? extends ")) {
 				ret = "list/"+findType(typeString.substring(15, typeString.length()-1),ctx);
 			} else {
 				ret = "list/"+findType(typeString.substring(5, typeString.length()-1),ctx);
 			}
 		} else {
 			if (typeString.equals("void")) ret = null;
 			else if (typeString.equals("String")) ret = "string";
 			else if (typeString.equals("Integer")) ret = "integer";
 			else if (typeString.equals("Long")) ret = "longint";
 			else if (typeString.equals("int")) ret = "integer";
 			else if (typeString.equals("long")) ret = "longint";
 			else if (typeString.equals("Date")) ret = "date";
 			else if (typeString.equals("Boolean")) ret = "boolean";
 			else {
 				ret = ctx.getAttributeType(typeString);
 				if (ret==null) {
 					ret = typeString;
 				}
 			}
 		}
 		return ret;
 	}
 
 	private String findName(String line) {
 		String ret = null;
 		int index = line.indexOf(' ',8);
 		int end = line.indexOf('(', index+1);
 		if (end>0) 
 			ret = line.substring(index, end).trim();
 
 		return ret;
 	}
 
 	private void addParameters(JavaOperation op,String line,ProcessorContext ctx,CustomLogic component) throws JavascribeException {
 		int start = line.indexOf('(');
 		int end = line.indexOf(')');
 		String sub = line.substring(start+1, end);
 		StringTokenizer tok = new StringTokenizer(sub,",");
 		while(tok.hasMoreTokens()) {
 			String s = tok.nextToken().trim();
 			int index = s.indexOf(' ');
 			if (index<0) {
 				throw new JavascribeException("Couldn't process declaration in line '"+line+"'");
 			}
 			String name = s.substring(index+1);
 			if (name.equals("fac")) name = "ds";
 			String type = s.substring(0, index);
 			type = this.findType(type,ctx);
 			op.addParameter(name, type);
 			if (ctx.getAttributeType(name)==null) {
 				ctx.addAttribute(name, type);
 			} else if (!ctx.getAttributeType(name).equals(type)) {
 				throw new JavascribeException("Inconsistent attribute type: Attribute '"+name+"' is defined as type '"+ctx.getAttributeType(name)+"' but custom logic '"+component.getServiceGroupName()+"' defines it as '"+type+"'");
 			}
 		}
 	}
 
 	private StringBuilder readFileContents(File f) throws IOException {
 		FileReader reader = null;
 		StringBuilder ret = new StringBuilder();
 
 		try {
 			reader = new FileReader(f);
 
 			int read = reader.read();
 			while(read>-1) {
 				ret.append((char)read);
 				read = reader.read();
 			}
 		} finally {
 			if (reader!=null) {
 				try { reader.close(); } catch(Exception e) { }
 			}
 		}
 		return ret;
 	}
 
 	@ProcessorMethod(componentClass=CustomLogic.class)
 	public void process(CustomLogic component,ProcessorContext ctx) throws JavascribeException {
 		List<LocatedJavaServiceObjectType> domainServices = new ArrayList<LocatedJavaServiceObjectType>();
 
 		ctx.setLanguageSupport("Java");
 
 		if (component.getServiceGroupName()==null) {
 			throw new JavascribeException("Found no serviceGroupName for service group");
 		}
 		String pkg = JavaUtils.findPackageName(ctx, ctx.getRequiredProperty(CUSTOM_LOGIC_PKG));
 		String locatorName = component.getServiceGroupName()+"Locator";
 		String locatorClassName = pkg+'.'+locatorName;
 
 		log.info("Processing custom logic group name '"+component.getServiceGroupName()+"'");
 
 		File dir = new File(component.getSrc());
 		if ((!dir.exists()) || (!dir.isDirectory())) {
 			throw new JavascribeException("Invalid directory '"+dir.getAbsolutePath()+"' specified for domain services.");
 		}
 		File contents[] = dir.listFiles();
 		try {
 			for(File f : contents) {
 				if ((!f.isDirectory()) && (f.getName().endsWith(".java"))) {
 					LocatedJavaServiceObjectType type = processJavaFile(f,ctx,component,locatorClassName);
 					ctx.getTypes().addType(type);
 					domainServices.add(type);
 				}
 			}
 		} catch(IOException e) {
 			e.printStackTrace();
 			throw new JavascribeException("IOException while reading domain files");
 		}
 
 		// Generate domain service locator
 		Java5SourceFile locatorFile = JsomUtils.getJavaFile(pkg+'.'+locatorClassName, ctx);
 		if (locatorFile==null) {
 			locatorFile = JsomUtils.createJavaSourceFile(ctx);
 			locatorFile.setPackageName(pkg);
 			locatorFile.getPublicClass().setClassName(locatorName);
 			Java5ClassConstructor con = JsomUtils.createConstructor(locatorFile, ctx);
 			locatorFile.getPublicClass().addMethod(con);
 			JsomUtils.addJavaFile(locatorFile, ctx);
 		}
 
 		try {
 			// For each service class in the group, generate the locator method that 
 			// creates it.
 			for(LocatedJavaServiceObjectType service : domainServices) {
 				Java5DeclaredMethod method = new Java5DeclaredMethod(new JavascribeVariableTypeResolver(ctx.getTypes()));
 				method.setReturnType(service.getName());
 				method.setAccessLevel("public");
 				method.setMethodName("get"+service.getName());
 				Java5CodeSnippet code = new Java5CodeSnippet();
 				method.setMethodBody(code);
 				locatorFile.getPublicClass().addMethod(method);
 				CodeExecutionContext execCtx = new CodeExecutionContext(null,ctx.getTypes());
 				String resultName = JavaUtils.findAttributeName(service.getName());
 				code.append(service.getName()+" "+resultName+" = new "+service.getName()+"();\n");
 				execCtx.addVariable(resultName, service.getName());
 				initDependencies(ctx,resultName,service,code,execCtx,domainServices);
 				code.append("return "+resultName+";\n");
 			}
 		} catch(CodeGenerationException e) {
 			throw new JavascribeException("JSOM exception while processing",e);
 		}
 
 	}
 
 	private void initDependencies(ProcessorContext ctx,String resultName, LocatedJavaServiceObjectType service, Java5CodeSnippet code, CodeExecutionContext execCtx,List<LocatedJavaServiceObjectType> domainServices) throws JavascribeException,CodeGenerationException {
 		for(String dep : service.getDependancyNames()) {
 			String var = JavaUtils.findAttributeName(dep);
 			VariableType type = execCtx.getType(dep);
 
 			if (type==null) {
 				String typeName = ctx.getAttributeType(JavascribeUtils.getLowerCamelName(dep));
 				if (typeName==null) {
 					throw new JavascribeException("Could not find dependency "+dep);
 				}
 				type = ctx.getType(typeName);
 			}
 
 			if (type==null) {
 				throw new JavascribeException("Could not find dependancy "+dep);
 			}
 			if (!(type instanceof JavaVariableType)) {
 				throw new JavascribeException("Can only inject Java types into a custom logic object");
 			}
 
 			JavaVariableType javaType = (JavaVariableType)type;
 			code.addImport(javaType.getImport());
 			if (hasService(dep,domainServices)) {
 				code.append(var+" = get"+dep+"();\n");
 			} else if (type instanceof Injectable) {
 				Injectable inj = (Injectable)type;
 				JavaCode c = inj.getInstance(var, execCtx);
 				JsomUtils.merge(code, c);
 //				code.merge(JsomUtils.declareAndInstantiateObject((JavaServiceObjectType)type, var, execCtx));
 			} else {
 				throw new JavascribeException("blah");
 			}
 			code.append(resultName+".set"+dep+"("+var+");\n");
 		}
 	}
 
 	private boolean hasService(String name,List<LocatedJavaServiceObjectType> domainServices) {
 		for(LocatedJavaServiceObjectType o : domainServices) {
 			if (o.getName().equals(name))
 				return true;
 		}
 		return false;
 	}
 
 	/** End of processing logic **/
 
 	public void cleanup() { }
 
 }
 
