 package org.oobium.build.clients.blazeds;
 
 import static org.oobium.build.util.ProjectUtils.getPrefsFileDate;
 import static org.oobium.utils.FileUtils.copy;
 import static org.oobium.utils.FileUtils.createFolder;
 import static org.oobium.utils.FileUtils.deleteContents;
 import static org.oobium.utils.FileUtils.writeFile;
 import static org.oobium.utils.StringUtils.join;
 import static org.oobium.utils.StringUtils.source;
 import static org.oobium.utils.StringUtils.varName;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.oobium.build.clients.JavaClientExporter;
 import org.oobium.build.gen.model.PropertyDescriptor;
 import org.oobium.build.model.ModelDefinition;
 import org.oobium.build.util.MethodCreator;
 import org.oobium.build.util.SourceFile;
 import org.oobium.build.workspace.Module;
 import org.oobium.build.workspace.Project;
 import org.oobium.build.workspace.Workspace;
 
 public class BlazeProjectGenerator {
 
 	private final Workspace workspace;
 	private final Module module;
 	private final File project;
 	
 	private String server;
 	private boolean force;
 	
 	public BlazeProjectGenerator(Workspace workspace, Module module) {
 		this(workspace, module, null);
 	}
 	
 	public BlazeProjectGenerator(Workspace workspace, Module module, File project) {
 		this.workspace = workspace;
 		this.module = module;
 		if(project == null) {
 			project = module.file.getParentFile();
 		}
 		if(project.isDirectory()) {
 			this.project = new File(project, module.name + ".blazeds");
 		} else {
 			this.project = project;
 		}
 	}
 	
 	public void create() throws IOException {
 		if(force) {
 			deleteContents(project);
 		}
 		else if(project.exists()) {
 			throw new UnsupportedOperationException(project.getName() + " already exists");
 		}
 		
 		if(server == null || server.trim().length() == 0) {
 			server = "localhost:5000";
 		}
 
 		createBuildFile();
 		createClassPathFile();
 		createManifest();
 		createPrefsFile();
 		createProjectFile();
 		createWebXmlFile(project.getName(), project.getName() + " BlazeDS project");
 		
 		createFolder(project, "classes");
 		createFolder(project, "lib");
 
 		File flex = createFolder(project, "flex");
 		createFlexFiles(flex);
 
 		File src = createFolder(project, "src");
 		
 		Project target = workspace.load(project);
 		JavaClientExporter javaExporter = new JavaClientExporter(workspace, module);
 		javaExporter.setTarget(target);
 		javaExporter.export();
 		
 		for(File file : module.findModels()) {
 			ModelDefinition model = new ModelDefinition(file);
 			createController(src, model);
 			createModel(src, model);
 			createNotifier(src, model);
 		}
 		
 		writeFile(src, "org/oobium/persist/blaze/ModelNotifier.java", getClass().getResourceAsStream("ModelNotifier.java.blaze"));
 		
 		createChannelController(src);
 		createApplicationController(src);
 		// createSessionController(src);
 
 		String[] names = {
 				"commons-codec-1.3.jar",
 				"commons-httpclient-3.0.1.jar",
 				"commons-logging.jar",
 				"flex-messaging-common.jar",
 				"flex-messaging-core.jar",
 				"flex-messaging-opt.jar",
 				"flex-messaging-proxy.jar",
 				"flex-messaging-remoting.jar"
 		};
 		for(String name : names) {
 			File jar = new File(createFolder(project, "lib"), name);
 			copy(getClass().getResourceAsStream("/lib/blazeds/" + name), jar);
 			target.addBuildPath("lib/" + name, "lib");
 		}
 	}
 
 	private void createApplicationController(File srcFolder){
 		SourceFile sf = new SourceFile();
 
 		sf.packageName = module.packageName(module.controllers);
 		sf.simpleName = "ApplicationController";
 		sf.imports.add("org.oobium.persist.http.HttpPersistService");
 
 		sf.staticInitializers.add("new HttpPersistService(\"{server}\", true)".replace("{server}", server));
 
 		writeFile(srcFolder, sf.getFilePath(), sf.toSource());
 	}
 	
 	private void createBuildFile() {
 		writeFile(project, "build.properties", join('\n',
 				"source.. = src/",
 				"output.. = classes/",
 				"bin.includes = META-INF/,\\",
 				".",
 				""
 			));
 	}
 
 	private void createChannelController(File srcFolder){
 		System.out.println("BLAZE_PROJECT_GENERATOR::CREATE_CHANNEL_CONTROLLER");
 
 		String mType = "Channel";
 		SourceFile sf = new SourceFile();
 		sf.packageName = module.packageName(module.controllers);
 		sf.simpleName = mType + "Controller";
 
 		sf.imports.add("flex.messaging.Destination");
 		sf.imports.add("flex.messaging.MessageBroker");
 		sf.imports.add("flex.messaging.MessageDestination");
 		sf.imports.add("flex.messaging.services.MessageService");
 		
 		sf.methods.put("0removeChannel", source(
 				"public static void removeChannel(String channelName){",
 				" try {",
 				"  MessageBroker broker = MessageBroker.getMessageBroker(null);",
 				"  MessageService service = (MessageService) broker.getService(\"message-service\");",
 				"  Destination destination = (MessageDestination) service.getDestination(channelName);",
 				"  if(destination != null) {",
 				"   service.removeDestination(destination.getId());",
 				"  }",
 				" } catch(Exception e){",
 				"  //LogHelper.write(e.toString());",
 				" }",
 				"}"
 			));
 		
 		MethodCreator m = new MethodCreator("1addChannel");
 		m.addLine("public static void addChannel(String channelName){");
 		m.addLine("try{");
 		m.addLine("MessageBroker broker = MessageBroker.getMessageBroker(null);");
 		m.addLine("MessageService service = (MessageService) broker.getService(\"message-service\");");
 		m.addLine("Destination findDestination = (MessageDestination) service.getDestination(channelName);");
 		m.addLine("if(findDestination == null) {");
 		m.addLine("Destination destination = (MessageDestination) service.createDestination(channelName);");
 		m.addLine("//LogHelper.write((\"CHANNEL_CONTROLLER::SERVICE_IS_STARTED::\"+service.isStarted()));");
 		m.addLine("if (service.isStarted()) {");
 		m.addLine("destination.start();");
 		m.addLine("}");	
 		m.addLine("} else {");
 		m.addLine("//LogHelper.write(\"CHANNEL_CONTROLLER::CHANNEL_ALREADY_EXISTS::ChannelName=\"+channelName);");
 		m.addLine("}");
 		m.addLine("} catch(Exception e){");
 		m.addLine("//LogHelper.write(e.toString());");
 		m.addLine("}");
 		m.addLine("}");
 		
 		sf.methods.put(m.name, m.toString());
 		writeFile(srcFolder, sf.getFilePath(), sf.toSource());
 	}
 
 	private void createClassPathFile() {
 		writeFile(project, ".classpath", source(
 				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
 				"<classpath>",
 				" <classpathentry kind=\"src\" path=\"src\"/>",
 				" <classpathentry kind=\"con\" path=\"org.eclipse.jdt.launching.JRE_CONTAINER/org.eclipse.jdt.internal.debug.ui.launcher.StandardVMType/JavaSE-1.6\"/>",
 				" <classpathentry kind=\"output\" path=\"classes\"/>",
 				"</classpath>"
 			));
 	}
 	
 	private void createController(File srcFolder, ModelDefinition model) {
 		String mType = model.getSimpleName();
 		String mVar = varName(mType);
 
 		SourceFile sf = new SourceFile();
 		sf.packageName = module.packageName(module.controllers);
 		sf.simpleName = mType + "Controller";
 		sf.superName = "ApplicationController";
 
 		sf.imports.add(AtomicBoolean.class.getCanonicalName());
 		sf.imports.add("flex.messaging.FlexContext");
 		sf.imports.add("flex.messaging.FlexSession");
 		sf.imports.add(List.class.getCanonicalName());
 		sf.imports.add(model.getCanonicalName());
 		sf.imports.add(model.packageName + ".notifiers." + model.getSimpleName() + "Notifier");
 
 		sf.staticVariables.put("addNotifier", "private static AtomicBoolean addNotifier = new AtomicBoolean(true);");
 		
 		sf.methods.put("addObserver", source(
 				"public String addObserver() {",
 				" FlexSession session = FlexContext.getFlexSession();",
 				" String channelName = (String) session.getAttribute(\"userName\");",
 				" if(channelName == null || channelName.trim().length() == 0) {",
 				"  channelName = session.getId();",
 				" }",
 				"",
 				" ChannelController.addChannel(channelName);",
 				"",
 				" if(addNotifier.getAndSet(false)) {",
 				"  {mType}.addObserver(new {mType}Notifier(channelName));",
 				" }",
 				"",
 				" return channelName;",
 				"}"
 			).replace("{mType}", mType));
 		
 		sf.methods.put("find(int id)", source(
 				"public {type} find(int id) throws Exception {",
 				" {type}.getPersistService().closeSession();",
 				" return {type}.find(id);",
 				"}"
 			).replace("{type}", mType));
 		
 		sf.methods.put("find(String where)", source(
 				"public {type} find(String where) throws Exception {",
 				" {type}.getPersistService().closeSession();",
 				" return {type}.find(where);",
 				"}"
 			).replace("{type}", mType));
 		
 		sf.methods.put("findAll", source(
 				"public List<{type}> findAll() throws Exception {",
 				" {type}.getPersistService().closeSession();",
 				" return {type}.findAll();",
 				"}"
 			).replace("{type}", mType));
 		
 		sf.methods.put("findAll(String where)", source(
 				"public List<{type}> findAll(String where) throws Exception {",
 				" {type}.getPersistService().closeSession();",
 				" return {type}.findAll(where);",
 				"}"
 			).replace("{type}", mType));
 		
 		sf.methods.put("create", source(
 				"public {type} create({type} {var}) throws Exception {",
 				" {var}.create();",
 				" return {var};",
 				"}"
 			).replace("{type}", mType).replace("{var}", mVar));
 
 		sf.methods.put("destroy", source(
 				"public {type} destroy({type} {var}) throws Exception {",
 				" {var}.destroy();",
 				" return {var};",
 				"}"
 			).replace("{type}", mType).replace("{var}", mVar));
 
 		sf.methods.put("update", source(
 				"public {type} update({type} {var}) throws Exception {",
 				" {var}.update();",
 				" return {var};",
 				"}"
 			).replace("{type}", mType).replace("{var}", mVar));
 
 		for(PropertyDescriptor prop : model.getProperties().values()) {
 			if(prop.hasMany()) {
 				sf.imports.add(Set.class.getCanonicalName());
 				sf.imports.add(prop.fullType());
 				String cType = prop.castType();
 				String type = prop.type();
 				String name = prop.variable();
 				sf.methods.put(name, source(
 						"public {cast}<{type}> {name}({mType} {mVar}) {",
 						" return {mVar}.{name}();",
 						"}"
 					).replace("{cast}", cType).replace("{type}", type).replace("{name}", name).replace("{mType}", mType).replace("{mVar}", mVar));
 			}
 		}
 		
 		writeFile(srcFolder, sf.getFilePath(), sf.toSource());
 	}
 	
 	private void createFlexFiles(File flex) {
 		createMessagingConfigFile(flex);
 		createProxyConfigFile(flex);
 		createRemotingConfigFile(flex);
 		createServicesConfigFile(flex);
 		
 		writeFile(flex, "version.properties", source(
 				"#{date}",
 				"build=4.0.0.14931",
 				"minimumSDKVersion=3.5"
 			).replace("{date}", getPrefsFileDate()));
 	}
 	
 	private void createManifest() {
 		writeFile(project, "META-INF" + File.separator + "MANIFEST.MF", join('\n',
 				"Manifest-Version: 1.0",
 				"Ant-Version: Apache Ant 1.7.0",
 				"Created-By: 1.5.0_15-b04 (Sun Microsystems Inc.)",
 				"Sealed: false",
 				"Implementation-Title: {projectName}",
 				"Implementation-Version: 4.0.0.14931",
 				"Implementation-Vendor: Oobium"
 			).replace("{projectName}", project.getName()));
 	}
 
 	private void createMessagingConfigFile(File flex){
 		writeFile(flex, "messaging-config.xml", source(
 				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
 				"<service id=\"message-service\" class=\"flex.messaging.services.MessageService\">",
 				"",
 				" <adapters>",
 				"  <adapter-definition id=\"actionscript\" class=\"flex.messaging.services.messaging.adapters.ActionScriptAdapter\" default=\"true\" />",
 				"  <adapter-definition id=\"jms\" class=\"flex.messaging.services.messaging.adapters.JMSAdapter\"/>",
 				" </adapters>",
 				" ",
 				" <default-channels>",
 				"  <channel ref=\"my-streaming-amf\"/>",
 				"  <channel ref=\"my-polling-amf\"/>",
 				" </default-channels>",
 				" <destination id=\"feed\">",
 				"  <!-- Destination specific channel configuration can be defined if needed",
 				"  <channels>",
 				"   <channel ref=\"my-streaming-amf\"/>",
 				"  </channels>  ",
 				"  -->",
 				" </destination>",
 				"",
 				" <destination id=\"chat\"/>",
 				"",
 				" <destination id=\"dashboard\"/>",
 				" ",
 				" <destination id=\"market-data-feed\">",
 				"  <properties>",
 				"   <server>",
 				"    <allow-subtopics>true</allow-subtopics>",
 				"    <subtopic-separator>.</subtopic-separator>s",
 				"   </server>",
 				"  </properties>",
 				"  <channels>",
 				"	<channel ref=\"my-polling-amf\"/>",
 				"	<channel ref=\"my-streaming-amf\"/>",
 				"   <channel ref=\"per-client-qos-polling-amf\"/>",
 				"  </channels>  ",
 				" </destination> ",
 				"",
 				"</service>"
 			));
 	}
 
 	private void createModel(File srcFolder, ModelDefinition model) throws IOException {
 		String type = model.getSimpleName();
 		String var = varName(type);
 		String plural = varName(type, true);
 		Collection<PropertyDescriptor> props = model.getProperties().values();
 		
 		SourceFile sf = new SourceFile();
 		sf.packageName = model.getPackageName();
 		sf.imports.add(List.class.getCanonicalName());
 		sf.imports.add(Set.class.getCanonicalName());
 		sf.simpleName = type;
 		sf.superName = sf.simpleName + "Model";
 
 		int i = 0;
 		List<String> l;
 		
 		l = new ArrayList<String>();
 		l.add("public static {type} setVars({type} {var}) {");
 		l.add(" if({var} != null) {");
 		l.add("  {var}.id = {var}.getId(int.class);");
 		l.add("  {var}.errors = {var}.getErrorsList();");
 		for(PropertyDescriptor prop : props) {
 			String pvar = prop.variable();
 			String getter = prop.getterName();
 			if(prop.isAttr()) {
 				l.add("  {var}.{pvar} = {var}.{getter}();".replace("{pvar}", pvar).replace("{getter}", getter));
 			}
 			else if(prop.hasOne()) {
 				l.add("  {var}.{pvar} = {ptype}.setVars({var}.{getter}());".replace("{pvar}", pvar).replace("{getter}", getter).replace("{ptype}", prop.type()));
 			}
 		}
 		l.add(" }");
 		l.add(" return {var};");
 		l.add("}");
 		sf.staticMethods.put(String.valueOf(i++), source(l).replace("{type}", type).replace("{var}", var));
 		
 		sf.staticMethods.put(String.valueOf(i++), source(
 				"private static List<{type}> setVars(List<{type}> {plural}) {",
 				" for({type} {var} : {plural}) {",
 				"  setVars({var});",
 				" }",
 				" return {plural};",
 				"}"
 			).replace("{type}", type).replace("{var}", var).replace("{plural}", plural));
 		
 		sf.staticMethods.put(String.valueOf(i++), source(
 				"public static Set<{type}> setVars(Set<{type}> {plural}) {",
 				" for({type} {var} : {plural}) {",
 				"  setVars({var});",
 				" }",
 				" return {plural};",
 				"}"
 			).replace("{type}", type).replace("{var}", var).replace("{plural}", plural));
 		
 		l = new ArrayList<String>();
 		l.add("private static {type} setFields({type} {var}) {");
 		l.add(" if({var} != null) {");
 		l.add("  {var}.setId({var}.id);");
 		l.add("  {var}.setErrors({var}.errors);");
 		for(PropertyDescriptor prop : props) {
 			if(!prop.hasMany()) {
 				l.add("  {var}.set({enum}, {var}.{prop});".replace("{enum}", prop.enumProp()).replace("{prop}", prop.variable()));
 			}
 		}
 		l.add(" }");
 		l.add(" return {var};");
 		l.add("}");
 		sf.staticMethods.put(String.valueOf(i++), source(l).replace("{type}", type).replace("{var}", var));
 		
 		sf.staticMethods.put(String.valueOf(i++), source(
 				"public static {type} find(int id) throws Exception {",
 				" return setVars({super}.findById(id));",
 				"}"
 			).replace("{type}", type).replace("{super}", sf.superName));
 		
 		sf.staticMethods.put(String.valueOf(i++), source(
 				"public static {type} find(String where) throws Exception {",
 				" return setVars({super}.find(where));",
 				"}"
 			).replace("{type}", type).replace("{super}", sf.superName));
 		
 		sf.staticMethods.put(String.valueOf(i++), source(
 				"public static List<{type}> findAll() throws Exception {",
 				" return setVars({super}.findAll());",
 				"}"
 			).replace("{type}", type).replace("{super}", sf.superName));
 		
 		sf.staticMethods.put(String.valueOf(i++), source(
 				"public static List<{type}> findAll(String where) throws Exception {",
 				" return setVars({super}.findAll(where));",
 				"}"
 			).replace("{type}", type).replace("{super}", sf.superName));
 		
 
 		sf.variables.put("", "public int id");
 		sf.variables.put(" ", "public List<String> errors");
 		for(PropertyDescriptor prop : props) {
 			if(prop.hasMany()) {
 				sf.imports.add(Set.class.getCanonicalName());
 				String name = prop.variable();
 				sf.methods.put(name, source(
 						"@Override",
 						"public Set<{type}> {name}() {",
 						" setFields(this);",
 						" return {type}.setVars(super.{name}());",
 						"}"
 					).replace("{type}", prop.type()).replace("{name}", name));
 			} else {
 				if(prop.hasImport() && !prop.fullType().startsWith("java.lang")) {
 					sf.imports.add(prop.fullType());
 				}
 				String cast = "Map".equals(prop.castType()) ? "Map<String, String>" : prop.castType();
 				sf.variables.put(prop.variable(), "public " + cast + " " + prop.variable());
 			}
 //			sf.imports.addAll(prop.imports());
 		}
 
 		sf.methods.put("create", source(
 				"@Override",
 				"public boolean create() {",
 				" setFields(this);",
 				" boolean result = super.create();",
 				" setVars(this);",
 				" return result;",
 				"}"
 			));
 
 		sf.methods.put("update", source(
 				"@Override",
 				"public boolean update() {",
 				" setFields(this);",
 				" boolean result = super.update();",
 				" setVars(this);",
 				" return result;",
 				"}"
 			));
 
 		sf.methods.put("destroy", source(
 				"@Override",
 				"public boolean destroy() {",
 				" setFields(this);",
 				" boolean result = super.destroy();",
 				" errors = getErrorsList();",
 				" return result;",
 				"}"
 			));
 		
 		writeFile(srcFolder, sf.getFilePath(), sf.toSource());
 	}
 	
 	private void createNotifier(File srcFolder, ModelDefinition model) throws IOException {
 		String name = model.packageName.replace('.', '/') + "/notifiers/" + model.getSimpleName() + "Notifier.java";
 		writeFile(srcFolder, name, source(
 				"package {modelsPackage}.notifiers;",
 				"",
 				"import {modelsPackage}.*;",
 				"import org.oobium.persist.blaze.ModelNotifier;",
 				"",
 				"public class {type}Notifier extends ModelNotifier<{type}> {",
 				"",
 				" public {type}Notifier(String channelName) {",
 				"  super(channelName);",
 				" }",
 				"",
 				"}"
 			).replace("{modelsPackage}", model.packageName).replace("{type}", model.getSimpleName()));
 	}
 	
 	private void createPrefsFile() {
 		writeFile(createFolder(project, ".settings"), "org.eclipse.jdt.core.prefs", source(
 				"#{date}",
 				"eclipse.preferences.version=1",
 				"org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode=enabled",
 				"org.eclipse.jdt.core.compiler.codegen.targetPlatform=1.6",
 				"org.eclipse.jdt.core.compiler.codegen.unusedLocal=preserve",
 				"org.eclipse.jdt.core.compiler.compliance=1.6",
 				"org.eclipse.jdt.core.compiler.debug.lineNumber=generate",
 				"org.eclipse.jdt.core.compiler.debug.localVariable=generate",
 				"org.eclipse.jdt.core.compiler.debug.sourceFile=generate",
 				"org.eclipse.jdt.core.compiler.problem.assertIdentifier=error",
 				"org.eclipse.jdt.core.compiler.problem.enumIdentifier=error",
 				"org.eclipse.jdt.core.compiler.source=1.6"
 			).replace("{date}", getPrefsFileDate()));
 	}
 	
 	private void createProjectFile() {
 		writeFile(project, ".project", source(
 				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
 				"<projectDescription>",
 				" <name>{projectName}</name>",
 				" <comment></comment>",
 				" <projects>",
 				" </projects>",
 				" <buildSpec>",
 				"  <buildCommand>",
 				"   <name>org.eclipse.jdt.core.javabuilder</name>",
 				"   <arguments>",
 				"   </arguments>",
 				"  </buildCommand>",
 				" </buildSpec>",
 				" <natures>",
 				"  <nature>org.oobium.blazenature</nature>",
 				"  <nature>org.eclipse.jdt.core.javanature</nature>",
 				" </natures>",
 				"</projectDescription>"
 			).replace("{projectName}", project.getName()));
 	}
 	
 	private void createProxyConfigFile(File flex) {
 		writeFile(flex, "proxy-config.xml", source(
 				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
 				"<service id=\"proxy-service\" class=\"flex.messaging.services.HTTPProxyService\">",
 				"",
 				" <properties>",
 				"  <connection-manager>",
 				"   <max-total-connections>100</max-total-connections>",
 				"   <default-max-connections-per-host>2</default-max-connections-per-host>",
 				"  </connection-manager>",
 				"",
 				"  <allow-lax-ssl>true</allow-lax-ssl>",
 				" </properties>",
 				"",
 				" <default-channels>",
 				"  <channel ref=\"my-http\"/>",
 				"  <channel ref=\"my-amf\"/>",
 				" </default-channels>",
 				"",
 				" <adapters>",
 				"  <adapter-definition id=\"http-proxy\" class=\"flex.messaging.services.http.HTTPProxyAdapter\" default=\"true\"/>",
 				"  <adapter-definition id=\"soap-proxy\" class=\"flex.messaging.services.http.SOAPProxyAdapter\"/>",
 				" </adapters>",
 				"",
 				" <destination id=\"DefaultHTTP\">",
 				"  <properties>",
 				"  </properties>",
 				" </destination>",
 				"",
 				" <destination id=\"catalog\">",
 				"  <properties>",
 				"   <url>/{context.root}/testdrive-httpservice/catalog.jsp</url>",
 				"  </properties>",
 				" </destination>",
 				"",
 				" <destination id=\"ws-catalog\">",
 				"  <properties>",
 				"   <wsdl>http://feeds.adobe.com/webservices/mxna2.cfc?wsdl</wsdl>",
 				"   <soap>*</soap>",
 				"  </properties>",
 				"  <adapter ref=\"soap-proxy\"/>",
 				" </destination>",
 				"",
 				"</service>"
 			).replace("{context.root}", "TODO"));
 	}
 	
 	private void createRemotingConfigFile(File flex) {
 		StringBuilder sb = new StringBuilder();
 		for(File model : module.findModels()) {
 			File file = module.getControllerFor(model);
 			String name = module.getControllerName(file);
 			String type = module.getControllerType(file);
 			sb.append('\n');
 			sb.append(source('\t', '\t',
 				"<destination id=\"{name}\">",
 				" <properties>",
 				"  <source>{type}</source>",
 				"  <scope>application</scope>",
 				" </properties>",
 				"</destination>"
 			).replace("{name}", name).replace("{type}", type));
 		}
 		sb.append('\n');

 		writeFile(flex, "remoting-config.xml", source(
 				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
 				"<service id=\"remoting-service\"",
 				" class=\"flex.messaging.services.RemotingService\">",
 				"",
 				" <adapters>",
 				"  <adapter-definition id=\"java-object\" class=\"flex.messaging.services.remoting.adapters.JavaAdapter\" default=\"true\"/>",
 				" </adapters>",
 				"",
 				" <default-channels>",
 				"  <channel ref=\"my-amf\"/>",
 				" </default-channels>",
 				"",
 				" {controllers}",
 				"",
 				"</service>"
 			).replace("{controllers}", sb.toString()));
 		
 	}
 
 	private void createServicesConfigFile(File flex) {
 		writeFile(flex, "services-config.xml", source(
 				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
 				"<services-config>",
 				"",
 				" <services>",
 				"",
 				"",
 				"  <service-include file-path=\"remoting-config.xml\" />",
 				"  <service-include file-path=\"proxy-config.xml\" />",
 				"  <service-include file-path=\"messaging-config.xml\" />",
 				"",
 				"",
 				"  <!-- ",
 				"  Application level default channels. Application level default channels are ",
 				"  necessary when a dynamic destination is being used by a service component",
 				"  and no ChannelSet has been defined for the service component. In that case,",
 				"  application level default channels will be used to contact the destination.",
 				"  -->",
 				"  <default-channels>",
 				"   <channel ref=\"my-amf\"/>",
 				"  </default-channels>",
 				" ",
 				" </services>",
 				"",
 				"",
 				" <security>",
 				"  <security-constraint id=\"sample-users\">",
 				"   <!--<service class=\"flex.samples.runtimeconfig.EmployeeRuntimeRemotingDestination\" id=\"runtime-employee-ro\" />-->",
 				"   <auth-method>Custom</auth-method>",
 				"   <roles>",
 				"    <role>sampleusers</role>",
 				"   </roles>",
 				"  </security-constraint>",
 				"",
 				"  <login-command class=\"flex.messaging.security.TomcatLoginCommand\" server=\"Tomcat\"/>  ",
 				"  <!-- Uncomment the correct app server",
 				"  <login-command class=\"flex.messaging.security.TomcatLoginCommand\" server=\"JBoss\">",
 				"  <login-command class=\"flex.messaging.security.JRunLoginCommand\" server=\"JRun\"/>",
 				"  <login-command class=\"flex.messaging.security.WeblogicLoginCommand\" server=\"Weblogic\"/>",
 				"  <login-command class=\"flex.messaging.security.WebSphereLoginCommand\" server=\"WebSphere\"/>  ",
 				"  -->",
 				" </security>",
 				"",
 				" <channels>",
 				" ",
 				"  <channel-definition id=\"my-streaming-amf\" class=\"mx.messaging.channels.StreamingAMFChannel\">",
 				"   <endpoint url=\"http://{server.name}:{server.port}/{context.root}/messagebroker/streamingamf\" class=\"flex.messaging.endpoints.StreamingAMFEndpoint\"/>",
 				"  </channel-definition>",
 				" ",
 				"  <channel-definition id=\"my-amf\" class=\"mx.messaging.channels.AMFChannel\">",
 				"   <endpoint url=\"http://{server.name}:{server.port}/{context.root}/messagebroker/amf\" class=\"flex.messaging.endpoints.AMFEndpoint\"/>",
 				"   <properties>",
 				"    <polling-enabled>false</polling-enabled>",
 				"   </properties>",
 				"  </channel-definition>",
 				"",
 				"  <channel-definition id=\"my-secure-amf\" class=\"mx.messaging.channels.SecureAMFChannel\">",
 				"   <endpoint url=\"https://{server.name}:{server.port}/{context.root}/messagebroker/amfsecure\" class=\"flex.messaging.endpoints.SecureAMFEndpoint\"/>",
 				"   <properties>",
 				"    <add-no-cache-headers>false</add-no-cache-headers>",
 				"   </properties>",
 				"  </channel-definition>",
 				"",
 				"  <channel-definition id=\"my-polling-amf\" class=\"mx.messaging.channels.AMFChannel\">",
 				"   <endpoint url=\"http://{server.name}:{server.port}/{context.root}/messagebroker/amfpolling\" class=\"flex.messaging.endpoints.AMFEndpoint\"/>",
 				"   <properties>",
 				"    <polling-enabled>true</polling-enabled>",
 				"    <polling-interval-seconds>4</polling-interval-seconds>",
 				"   </properties>",
 				"  </channel-definition>",
 				"",
 				"  <channel-definition id=\"my-http\" class=\"mx.messaging.channels.HTTPChannel\">",
 				"   <endpoint url=\"http://{server.name}:{server.port}/{context.root}/messagebroker/http\" class=\"flex.messaging.endpoints.HTTPEndpoint\"/>",
 				"  </channel-definition>",
 				"",
 				"  <channel-definition id=\"my-secure-http\" class=\"mx.messaging.channels.SecureHTTPChannel\">",
 				"   <endpoint url=\"https://{server.name}:{server.port}/{context.root}/messagebroker/httpsecure\" class=\"flex.messaging.endpoints.SecureHTTPEndpoint\"/>",
 				"   <properties>",
 				"    <add-no-cache-headers>false</add-no-cache-headers>",
 				"   </properties>",
 				"  </channel-definition>",
 				"",
 				"  <channel-definition id=\"per-client-qos-polling-amf\" class=\"mx.messaging.channels.AMFChannel\">",
 				"   <endpoint url=\"http://{server.name}:{server.port}/{context.root}/messagebroker/qosamfpolling\" class=\"flex.messaging.endpoints.AMFEndpoint\"/>",
 				"   <properties>",
 				"    <polling-enabled>true</polling-enabled>",
 				"    <polling-interval-millis>500</polling-interval-millis>",
 				"    <flex-client-outbound-queue-processor class=\"flex.samples.qos.CustomDelayQueueProcessor\">",
 				"     <!--<properties><flush-delay>5000</flush-delay></properties>-->",
 				"    </flex-client-outbound-queue-processor>",
 				"   </properties>",
 				"  </channel-definition>",
 				"",
 				" </channels>",
 				"",
 				" <logging>",
 				"  <!-- You may also use flex.messaging.log.ServletLogTarget -->",
 				"  <target class=\"flex.messaging.log.ConsoleTarget\" level=\"Error\">",
 				"   <properties>",
 				"    <prefix>[BlazeDS] </prefix>",
 				"    <includeDate>false</includeDate>",
 				"    <includeTime>false</includeTime>",
 				"    <includeLevel>true</includeLevel>",
 				"    <includeCategory>false</includeCategory>",
 				"   </properties>",
 				"   <filters>",
 				"    <pattern>Endpoint.*</pattern>",
 				"    <pattern>Service.*</pattern>",
 				"    <pattern>Configuration</pattern>",
 				"   </filters>",
 				"  </target>",
 				" </logging>",
 				"",
 				" <system>",
 				"  <redeploy>",
 				"   <enabled>true</enabled>",
 				"   <watch-interval>20</watch-interval>",
 				"   <watch-file>{context.root}/WEB-INF/flex/services-config.xml</watch-file>",
 				"   <watch-file>{context.root}/WEB-INF/flex/proxy-config.xml</watch-file>",
 				"   <watch-file>{context.root}/WEB-INF/flex/remoting-config.xml</watch-file>",
 				"   <watch-file>{context.root}/WEB-INF/flex/messaging-config.xml</watch-file>",
 				"   <touch-file>{context.root}/WEB-INF/web.xml</touch-file>",
 				"  </redeploy>",
 				" </system>",
 				"",
 				"</services-config>"
 			));
 	}
 	
 	private void createSessionController(File srcFolder){
 		System.out.println("BLAZE_PROJECT_GENERATOR::CREATE_USER_SESSION_CONTROLLER");
 
 		String mType = "UserSession";
 
 		SourceFile sf = new SourceFile();
 		sf.packageName = module.packageName(module.controllers);
 		sf.simpleName = mType + "Controller";
 
 		sf.imports.add("import java.util.List");
 		sf.imports.add("import com.dn2k.blazeds.models.User");
 		sf.imports.add("import com.dn2k.blazeds.stub.UserSvcStub");
 		sf.imports.add("import com.dn2k.blazeds.util.LogHelper");
 		sf.imports.add("import flex.messaging.FlexContext");
 		sf.imports.add("import flex.messaging.FlexSession");
 
 		sf.variables.put("User", "private static User user");
 		sf.variables.put("FlexSession", "private static FlexSession mySession");
 		
 		MethodCreator m0 = new MethodCreator("0UserSessionController");
 		m0.addLine("public UserSessionController(){");
 		m0.addLine("mySession = FlexContext.getFlexSession();");
 		m0.addLine("}");
 		sf.methods.put(m0.name, m0.toString());
 		
 		MethodCreator m1 = new MethodCreator("1getSessionId()");
 		m1.addLine("public String getSessionId(){");
 		m1.addLine("return mySession.getId();");
 		m1.addLine("}");
 		sf.methods.put(m1.name, m1.toString());
 
 		MethodCreator m2 = new MethodCreator("2getUserId()");
 		m2.addLine("public int getUserId(){");
 		m2.addLine("return user.id;");
 		m2.addLine("}");
 		sf.methods.put(m2.name, m2.toString());
 
 		MethodCreator m3 = new MethodCreator("3getUserName()");
 		m3.addLine("public String getUserName(){");
 		m3.addLine("//LogHelper.write(\"SESSION_CONTROLLER::GET_USER\");");
 		m3.addLine("if(user==null) {");
 		m3.addLine("return \"\";");
 		m3.addLine("}");
 		m3.addLine("//LogHelper.write(\"SESSION_CONTROLLER::userName=\"+user.userName);");
 		m3.addLine("return user.userName;");
 		m3.addLine("}");
 		sf.methods.put(m3.name, m3.toString());
 
 		MethodCreator m4 = new MethodCreator("4getPassword()");
 		m4.addLine("public String getPassword(){");
 		m4.addLine("return user.password;");
 		m4.addLine("}");
 		sf.addMethod(m4);
 
 		MethodCreator m5 = new MethodCreator("5login()");
 		m5.addLine("public boolean login(String userName, String password) {");
 		m5.addLine("//LogHelper.write(\"LOGIN::userName=\"+userName+\", password=\"+password);");
 		m5.addLine("user = authenticate(userName, password);");
 		m5.addLine("if(user!=null){");
 		m5.addLine("return true;");
 		m5.addLine("} else {");
 		m5.addLine("return false;");
 		m5.addLine("}");
 		m5.addLine("}");
 		sf.addMethod(m5);
 
 		MethodCreator m6 = new MethodCreator("6logout()");
 		m6.addLine("public void logout() {");
 		m6.addLine("mySession = FlexContext.getFlexSession();");
 		m6.addLine("mySession.removeAttribute(\"userName\");");
 		m6.addLine("mySession.removeAttribute(\"password\");");
 		m6.addLine("}");
 		sf.addMethod(m6);
 		
 		MethodCreator m7 = new MethodCreator("7authenticate()");
 		m7.addLine("public User authenticate(String userName, String password){");
 			m7.addLine("try {");
 				m7.addLine("mySession = FlexContext.getFlexSession();");
 				m7.addLine("if(mySession==null){");
 					m7.addLine("//LogHelper.write(\"SESSION is NULL!!!!!!!!!!!!!\");");
 				m7.addLine("}");
 				m7.addLine("List userList = UserSvcStub.findAll();");
 				m7.addLine("//LogHelper.write(\"USER_LIST_SIZE::size=\"+userList.size());");
 				m7.addLine("for(Object obj:userList){");
 					m7.addLine("User mUser = (User)obj;");
 					m7.addLine("//LogHelper.write(\"SESSION_CONTROLLER::authenticate::userName\"+userName+\", password=\"+password);");
 					m7.addLine("//LogHelper.write(\"USER_OBJECT::username=\"+mUser.userName+\", password=\"+mUser.password);");
 					m7.addLine("if(mUser.userName.equals(userName)){");
 						m7.addLine("if(mUser.password.equals(password)){");
 							m7.addLine("//LogHelper.write(\"MATCH USER::\"+mUser.userName);");
 							m7.addLine("mySession.setAttribute(\"userName\", mUser.userName);");
 							m7.addLine("mySession.setAttribute(\"password\", mUser.password);");
 							m7.addLine("user = mUser;");
 							m7.addLine("return mUser;");
 						m7.addLine("}");
 					m7.addLine("}");
 				m7.addLine("}");
 			m7.addLine("} catch(Exception e){");
 				m7.addLine("//LogHelper.write(e.toString());");
 				m7.addLine("for(StackTraceElement element: e.getStackTrace()){");
 					m7.addLine("//LogHelper.write(element.toString());");
 				m7.addLine("}");
 			m7.addLine("}");
 			m7.addLine("return null;");
 		m7.addLine("}");
 		sf.addMethod(m7);
 		
 		writeFile(srcFolder, sf.getFilePath(), sf.toSource());
 	}
 	
 	private void createWebXmlFile(String name, String description) {
 		writeFile(project, "web.xml", source(
 				"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
 				"<!DOCTYPE web-app PUBLIC \"-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN\" \"http://java.sun.com/dtd/web-app_2_3.dtd\">",
 				"<web-app>",
 				" <display-name>{name}</display-name>",
 				" <description>{description}</description>",
 				"",
 				" <!-- Http Flex Session attribute and binding listener support -->",
 				" <listener>",
 				"  <listener-class>flex.messaging.HttpFlexSession</listener-class>",
 				" </listener>",
 				"",
 				" <!-- MessageBroker Servlet -->",
 				" <servlet>",
 				"  <servlet-name>MessageBrokerServlet</servlet-name>",
 				"  <display-name>MessageBrokerServlet</display-name>",
 				"  <servlet-class>flex.messaging.MessageBrokerServlet</servlet-class>",
 				"  <init-param>",
 				"   <param-name>services.configuration.file</param-name>",
 				"   <param-value>/WEB-INF/flex/services-config.xml</param-value>",
 				"  </init-param>",
 				"  <load-on-startup>1</load-on-startup>",
 				" </servlet>",
 				" <servlet-mapping>",
 				"  <servlet-name>MessageBrokerServlet</servlet-name>",
 				"  <url-pattern>/messagebroker/*</url-pattern>",
 				" </servlet-mapping>",
 				"",
 				" <welcome-file-list>",
 				"  <welcome-file>index.htm</welcome-file>",
 				" </welcome-file-list>",
 				"",
 				"</web-app>"
 			).replace("{name}", name).replace("{description}", description));
 	}
 
 	public File getProject() {
 		return project;
 	}
 	
 	public void setForce(boolean force) {
 		this.force = force;
 	}
 
 	public void setServer(String server) {
 		this.server = server;
 	}
 	
 }
