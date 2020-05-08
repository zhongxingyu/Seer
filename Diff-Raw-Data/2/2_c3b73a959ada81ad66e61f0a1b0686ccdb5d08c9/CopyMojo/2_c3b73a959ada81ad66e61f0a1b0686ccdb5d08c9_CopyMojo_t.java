 package com.isb.jVoice.application.builder;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Properties;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipInputStream;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 
 import com.vectorsf.jvoice.model.base.BasePackage;
 import com.vectorsf.jvoice.model.base.JVApplication;
 import com.vectorsf.jvoice.model.base.JVModule;
 import com.vectorsf.jvoice.model.operations.OperationsPackage;
 
 /**
  * Goal which touches a timestamp file.
  * 
  * @goal copyXML
  * 
  * @phase generate-resources
  * @requiresDependencyResolution compile+runtime
  */
 public class CopyMojo extends AbstractMojo {
 
 	private static final String SRC_MAIN_RESOURCES = "/src/main/resources";
 	private static final String PROPERTIES_RESOURCES = "src/main/resources/properties";
 	private static final String STATIC = "static/";
 	private static final String WAV_EXT = ".wav";
 	private static final String XML_EXT = ".xml";
 	private static final String JAR_EXT = ".jar";
 	private static final String GRXML_EXT = ".grxml";
 	private static final String PROJECT_INFORMATION = "projectInformation";
 	private static final String PROJECT_INFORMATION_EXT = ".projectInformation";
 	private static final String SEPARATOR = "/";
 	private static final String DOT = ".";
 	private static final String JVOICES = "jVoice";
 	private static final String DESTINO = "flows";
 	private static final String APPSERVLET = "spring/appServlet";
 	private static final String FLOW = "-flow";
 	private static final String AUDIOS = "audios";
 	private static final String GRAMMARS = "grammars";
 	private static final String DESTINOWAV = "resources";
 	private static final String ARCHIVE_FILE = "archive:file:/";
 	private static final String SEPARATOR2 = "\\";
 	private static final String LOGGER_CONFIG_DIR = "/com/vectorsf/jvoiceframework/config/logger";
 	private static final String WEBAPP_RESOURCES = "/src/main/webapp/resources";
 	private static final String PROPERTIES = "properties";
 
 	/**
 	 * Location of the target directory.
 	 * 
 	 * @parameter expression="${basedir}/src/main/webapp/WEB-INF"
 	 */
 	private File outputDirectory;
 
 	/**
 	 * The Maven Project Object
 	 * 
 	 * @parameter expression="${project}"
 	 * @required
 	 */
 	protected MavenProject mavenProject;
 
 	/**
 	 * @parameter expression="${project.runtimeClasspathElements}"
 	 */
 	private List<String> runtimeClasspathElements;
 
 	private List<JVModule> modules = new ArrayList<JVModule>();
 	private ClassLoader runtimeClassLoader;
 	private static final List<String> EXTENSIONS = Arrays.asList(new String[] { XML_EXT, WAV_EXT, GRXML_EXT });
 
 	private URL[] buildURLs() {
 		List<URL> urls = new ArrayList<URL>(runtimeClasspathElements.size());
 		for (String element : runtimeClasspathElements) {
 
 			try {
 				urls.add(new File(element).toURI().toURL());
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 			}
 
 		}
 		return urls.toArray(new URL[urls.size()]);
 	}
 
 	private ClassLoader createClassLoaderForProjectDependencies() {
 		URL[] urls = buildURLs();
 		return new URLClassLoader(urls, this.getClass().getClassLoader());
 	}
 
 	@Override
 	public void execute() throws MojoExecutionException {
 
 		runtimeClassLoader = createClassLoaderForProjectDependencies();
 
 		try {
 			Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(PROJECT_INFORMATION,
 					new XMIResourceFactoryImpl());
 			EPackage.Registry.INSTANCE.put(BasePackage.eNS_URI, BasePackage.eINSTANCE);
 			EPackage.Registry.INSTANCE.put(OperationsPackage.eNS_URI, OperationsPackage.eINSTANCE);
 
 			JVApplication application = getProjectInformation();
 			File flows = new File(outputDirectory, DESTINO);
 			flows.mkdirs();
 			generateAppEventsHandlers(application, flows);
 
			File finalFlowFloder = new File(flows + "/jVoiceArchFlows/jVoiceArch-end");
 			finalFlowFloder.mkdirs();
 
 			copyFile("flows/jVoiceArch-end-flow.xml", new File(finalFlowFloder, "jVoiceArch-end-flow.xml"));
 
 			// copia los ficheros .wav y .xml, además recopila las propiedades
 			// de los módulos de los que depende, preferencia de las propiedades
 			// de los módulos por orden alfabetico del los nombres de los
 			// módulos.
 			searchInJarFiles();
 
 			// Creamos la carpeta estatica spring/appServlet
 			File appServlet = new File(outputDirectory, APPSERVLET);
 			appServlet.mkdirs();
 			// Generamos el XML servlet-context.xml de la carpeta spring
 			XMLGeneratorServlet.generate(new File(appServlet, "servlet-context.xml"));
 
 			// Creamos el app-context.xml en dentro de la carpeta WEB-INF
 			File spring = new File(outputDirectory, "spring");
 			XMLGeneratorAPP.generate(new File(spring, "app-context.xml"), modules);
 
 			String pathDisProp = mavenProject.getBasedir().getAbsolutePath() + SEPARATOR + PROPERTIES_RESOURCES;
 			File dDirProperties = new File(pathDisProp);
 			File[] listFolderProperties = {};
 			if (dDirProperties.exists()) {
 				listFolderProperties = dDirProperties.listFiles();
 			}
 
 			XMLGeneratorJFC.generate(new File(spring, "jvoiceframework-context.xml"), application.getName(),
 					application.isLegacyLogger(), listFolderProperties);
 			XMLGeneratorRC.generate(new File(spring, "root-context.xml"));
 
 			// Creamos el web.xml en dentro de la carpeta WEB-INF
 			XMLGeneratorWeb.generate(new File(outputDirectory, "web.xml"));
 
 			// Creamos la carpeta estatica views
 			File views = new File(outputDirectory, "views");
 			views.mkdirs();
 			XMLGeneratorRHTML.generate(new File(views, "renderHTML.jsp"));
 			XMLGeneratorRVXI.generate(new File(views, "renderVXI.jsp"));
 			copyFile("views/_initHTML.jsp", new File(views, "_initHTML.jsp"));
 			copyFile("views/_initVXI.jsp", new File(views, "_initVXI.jsp"));
 
 			// Creamos la carpeta src/main/resources/com/vectorsf/jvoiceframework/config/logger
 			File configLogger = new File(mavenProject.getBasedir().getAbsolutePath() + SRC_MAIN_RESOURCES
 					+ LOGGER_CONFIG_DIR);
 			configLogger.mkdirs();
 
 			// Generamos el archivo "padre" de configuración del logger
 			// (logback.xml) en la carpeta src/main/resources
 			XMLGeneratorLogback.generate(new File(mavenProject.getBasedir().getAbsolutePath() + SRC_MAIN_RESOURCES,
 					"logback.xml"), application.isLegacyLogger());
 
 			// Copiamos el archivo de configuración general del logger
 			// (logback-core.xml) en la carpeta
 			// src/main/resources/com/vectorsf/jvoiceframework/config/logger
 			copyFile("logback-core.xml", new File(configLogger, "logback-core.xml"));
 
 			// Copiamos/borramos los archivos que necesita el legacy logger en
 			// función de si se usa o no
 			handleLegacyLoggerFiles(application.isLegacyLogger(), configLogger);
 
 		} catch (IOException e) {
 			throw new MojoExecutionException("Error in CopyMojo:execute()", e);
 		}
 
 	}
 
 	private boolean isPropertiesFile(String entryNameDots) {
 		return entryNameDots.endsWith(PROPERTIES) && entryNameDots.startsWith(PROPERTIES);
 	}
 
 	private void handleLegacyLoggerFiles(boolean legacyLogger, File configLogger) throws MojoExecutionException {
 
 		File isbanLoggerConfig = new File(configLogger, "isban-logger-config.xml");
 		File logbackIsbanLogger = new File(configLogger, "logback-isban-logger.xml");
 		File js = new File(mavenProject.getBasedir().getAbsolutePath() + WEBAPP_RESOURCES, "js");
 		File isbanLoggerJs = new File(js, "isban-logger.js");
 
 		if (legacyLogger) {
 			// Si se usa el isban logger:
 			// Copiamos los archivos de configuración necesarios en la carpeta
 			// src/main/resources/com/vectorsf/jvoiceframework/config/logger
 			copyFile("isban-logger-config.xml", isbanLoggerConfig);
 			copyFile("logback-isban-logger.xml", logbackIsbanLogger);
 			// Creamos la carpeta js dentro de src/main/webapp/resources
 			js.mkdirs();
 			// Copiamos el archivo isban-logger.js en ella
 			copyFile("isban-logger.js", isbanLoggerJs);
 		} else {
 			// Si no se usa el legacy logger, si están los archivos, se borran.
 			if (isbanLoggerConfig.exists()) {
 				isbanLoggerConfig.delete();
 			}
 			if (logbackIsbanLogger.exists()) {
 				logbackIsbanLogger.delete();
 			}
 			if (isbanLoggerJs.exists()) {
 				isbanLoggerJs.delete();
 				js.delete();
 			}
 		}
 	}
 
 	private void searchInJarFiles() throws IOException, MojoExecutionException {
 
 		List<File> dependencies = getProjectDependencies();
 		for (File dependency : dependencies) {
 			if (dependency != null && dependency.toURI().toString().endsWith(JAR_EXT)) {
 				processDependency(dependency);
 			}
 		}
 	}
 
 	private List<File> getProjectDependencies() throws MojoExecutionException {
 
 		List<File> dependencies = new ArrayList<File>();
 
 		for (String resource : runtimeClasspathElements) {
 			if (resource.endsWith(JAR_EXT)) {
 				try (ZipInputStream zip = new ZipInputStream(new FileInputStream(resource));) {
 					if (zip != null) {
 						ZipEntry ze = null;
 						while ((ze = zip.getNextEntry()) != null) {
 							String entryName = ze.getName();
 							if (entryName.equals(PROJECT_INFORMATION_EXT)) {
 								File f = new File(resource);
 								dependencies.add(f);
 								break;
 							}
 						}
 					}
 				} catch (IOException e1) {
 					throw new MojoExecutionException("Error in CopyMojo:getProjectDependencies()", e1);
 				}
 			}
 		}
 		Collections.sort(dependencies);
 		return dependencies;
 	}
 
 	private void copyFile(String origName, File destFile) throws MojoExecutionException {
 		try (InputStream is = runtimeClassLoader.getResourceAsStream(STATIC + origName);
 				FileOutputStream fos = new FileOutputStream(destFile)) {
 			if (is != null) {
 				int read = -1;
 				byte[] buf = new byte[4096];
 
 				while ((read = is.read(buf)) != -1) {
 					fos.write(buf, 0, read);
 				}
 			}
 		} catch (IOException e) {
 			throw new MojoExecutionException("Error copying resources", e);
 		}
 	}
 
 	/**
 	 * Metodo que accede al projectInformation para obtener informacion de el.
 	 */
 	private JVApplication getProjectInformation() {
 		String ruta = "file:///" + mavenProject.getBasedir().getAbsolutePath() + SEPARATOR + PROJECT_INFORMATION_EXT;
 		ResourceSet resSet = new ResourceSetImpl();
 		URI uri = URI.createURI(ruta.replace(SEPARATOR2, SEPARATOR));
 		Resource res = resSet.getResource(uri, true);
 		JVApplication project = (JVApplication) res.getContents().get(0);
 		return project;
 	}
 
 	/**
 	 * Método que genera el flujo que controla los eventos globales de la aplicación.
 	 * Lo crea siempre en la carpeta "_AppEventsHandlers", con el nombre eventsHandlers-flow.xml
 	 * @param project Aplicación para la que se genera el flujo de eventos globales.
 	 * @param mainFolder Carpeta donde se crean los flujos de webflow.
 	 */
 	private void generateAppEventsHandlers(JVApplication project, File mainFolder) {
 		File folder = new File(new File(mainFolder, "_AppEventsHandlers"), "eventsHandlers");
 		folder.mkdirs();
 		MainFlowGenerator.compile(new File(folder, "eventsHandlers-flow.xml"), project);
 	}
 
 	/**
 	 * @param name
 	 * 
 	 */
 	protected void copyFile(InputStream in, String name, String projectName) {
 
 		File destino = null;
 		// Se crea en rutas diferentes dependiendo de si se trata de un audio
 		// o un flujo (xml)
 		if (name.endsWith(XML_EXT)) {
 			String newName = name.substring(name.indexOf(SEPARATOR) + 1, name.length());
 			File ruta = new File(newName);
 			File pathname = new File(outputDirectory, DESTINO + SEPARATOR + newName.replace(ruta.getName(), "").trim()
 					+ ruta.getName().substring(0, ruta.getName().indexOf(DOT)).trim());
 			/*
 			 * Comprobamos que exista el directorio base donde vamos a crear los XML. Si no existe, se crea.
 			 */
 			pathname.mkdirs();
 
 			destino = new File(pathname, ruta.getName().substring(0, ruta.getName().indexOf(DOT)) + FLOW
 					+ ruta.getName().substring(ruta.getName().indexOf(DOT)));
 
 		} else {
 			File ruta = new File(name);
 			File pathname = new File(outputDirectory.getParentFile(), DESTINOWAV + SEPARATOR
 					+ name.replace(ruta.getName(), "").trim() + SEPARATOR + projectName);
 			/*
 			 * Comprobamos que exista el directorio base donde vamos a crear los grxml. Si no existe, se crea.
 			 */
 			pathname.mkdirs();
 
 			destino = new File(pathname, ruta.getName());
 		}
 
 		try {
 			OutputStream out = new FileOutputStream(destino);
 			byte[] buf = new byte[4096];
 			int len;
 
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 
 			in.close();
 			out.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void processDependency(File file) throws IOException {
 		String currentProject = null;
 		try (ZipInputStream zip = new ZipInputStream(new FileInputStream(file))) {
 			ZipEntry ze = null;
 
 			while ((ze = zip.getNextEntry()) != null) {
 				String entryName = ze.getName();
 				if (entryName.equals(PROJECT_INFORMATION_EXT)) {
 					JVModule module = getProject(entryName, file);
 					modules.add(module);
 					currentProject = module.getName();
 					break;
 				}
 			}
 		}
 
 		if (currentProject == null) {
 			return;
 		}
 
 		try (ZipFile zip = new ZipFile(file)) {
 			for (Enumeration<? extends ZipEntry> enu = zip.entries(); enu.hasMoreElements();) {
 				ZipEntry ze = enu.nextElement();
 				String entryName = ze.getName();
 				if (entryName.lastIndexOf(DOT) == -1) {
 					continue;
 				}
 				String entryNameDots = entryName.replace(SEPARATOR, DOT);
 				if (isPropertiesFile(entryName)) {
 					manageProperties(zip.getInputStream(ze), entryName.substring(entryName.lastIndexOf(SEPARATOR)));
 				} else {
 					/*
 					 * Recorremos la lista para comprobar la extension del fichero. En el caso de que coincida, se trata
 					 * de un tipo de fichero que necesitamos copiar del jar.
 					 */
 					for (String element : EXTENSIONS) {
 						// Comprobamos si el archivo es xml o jVoices
 						// para proceder a su copia. El resto no se
 						// copia en la aplicacion.
 						if (entryNameDots.endsWith(element)
 								&& (entryNameDots.contains(JVOICES + DOT) || entryNameDots.contains(AUDIOS + DOT) || entryNameDots
 										.contains(GRAMMARS + DOT))) {
 							InputStream inputStream = zip.getInputStream(ze);
 							copyFile(inputStream, entryName, currentProject);
 							break;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private void manageProperties(InputStream inputStream, String sFileName) throws IOException {
 		String pathDisProp = mavenProject.getBasedir().getAbsolutePath() + SEPARATOR + PROPERTIES_RESOURCES;
 		File dDirProperties = new File(pathDisProp);
 		if (!dDirProperties.exists()) {
 			dDirProperties.mkdirs();
 		}
 		String pathFile = pathDisProp + sFileName;
 		File fFile = new File(pathFile);
 
 		Properties merged = new Properties();
 		merged.load(inputStream);
 
 		boolean save = false;
 
 		if (fFile.exists()) {
 			Properties pAuxDisk = new Properties();
 			pAuxDisk.load(new FileInputStream(pathFile));
 
 			if (!pAuxDisk.keySet().containsAll(merged.keySet())) {
 				merged.putAll(pAuxDisk);
 				save = true;
 			}
 		} else {
 			save = true;
 		}
 
 		if (save) {
 			// guardamos en disco el fichero properties
 			merged.store(new FileOutputStream(pathFile), null);
 		}
 	}
 
 	/**
 	 * @param entryName
 	 */
 	protected JVModule getProject(String entryName, File file) {
 
 		String ruta = ARCHIVE_FILE + file.getAbsolutePath().replace(SEPARATOR2, SEPARATOR) + "!/" + entryName;
 		ResourceSet resSet = new ResourceSetImpl();
 		URI uri = URI.createURI(ruta);
 
 		Resource res = resSet.getResource(uri, true);
 		return (JVModule) res.getContents().get(0);
 
 	}
 
 }
