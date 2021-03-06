 package com.agilejava.docbkx.maven;
 
 /*
  * Copyright 2006 Wilfred Springer
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.servlet.jsp.el.ELException;
 import javax.servlet.jsp.el.VariableResolver;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.URIResolver;
 import javax.xml.transform.sax.SAXSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import nu.xom.Builder;
 import nu.xom.ParsingException;
 import nu.xom.Serializer;
 import nu.xom.ValidityException;
 import nu.xom.xinclude.XIncludeException;
 import nu.xom.xinclude.XIncluder;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.DependencyResolutionRequiredException;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.antrun.AntPropertyHelper;
 import org.apache.maven.project.MavenProject;
 import org.apache.tools.ant.DefaultLogger;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.PropertyHelper;
 import org.apache.tools.ant.Target;
 import org.apache.tools.ant.types.Path;
 import org.apache.xerces.jaxp.SAXParserFactoryImpl;
 import org.apache.xml.resolver.CatalogManager;
 import org.apache.xml.resolver.tools.CatalogResolver;
 import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
 import org.codehaus.plexus.component.configurator.expression.DefaultExpressionEvaluator;
 import org.codehaus.plexus.util.DirectoryScanner;
 import org.codehaus.plexus.util.StringUtils;
 import org.jaxen.JaxenException;
 import org.jaxen.XPath;
 import org.jaxen.dom.DOMXPath;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.xml.sax.EntityResolver;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 import com.icl.saxon.Controller;
 import com.icl.saxon.TransformerFactoryImpl;
 
 /**
  * The abstract Mojo base for concrete Mojos that generate some kind of output
  * format from DocBook. This Mojo will search documents in the directory
  * returned by {@link #getTargetDirectory()}, and apply the stylesheets on
  * these documents. This Mojo will be subclassed by Mojo's that generate a
  * particular type of output.
  * 
  * @author Wilfred Springer
  */
 public abstract class AbstractTransformerMojo extends AbstractMojo {
 
 	/**
 	 * Builds the actual output document.
 	 */
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		preProcess();
 		File targetDirectory = getTargetDirectory();
 		File sourceDirectory = getSourceDirectory();
 		if (!sourceDirectory.exists()) {
 			return; // No sources, so there is nothing to render.
 		}
 		if (!targetDirectory.exists()) {
 			org.codehaus.plexus.util.FileUtils.mkdir(targetDirectory
 					.getAbsolutePath());
 		}
 		DirectoryScanner scanner = new DirectoryScanner();
 		scanner.setBasedir(sourceDirectory);
 		scanner.setIncludes(getIncludes());
 		scanner.scan();
 		String[] included = scanner.getIncludedFiles();
 		CatalogManager catalogManager = createCatalogManager();
 		CatalogResolver catalogResolver = new CatalogResolver(catalogManager);
 		URIResolver uriResolver = null;
 		try {
 			URL url = getNonDefaultStylesheetURL() == null ? getDefaultStylesheetURL()
 					: getNonDefaultStylesheetURL();
 			uriResolver = new StylesheetResolver("urn:docbkx:stylesheet",
 					new StreamSource(url.openStream(), url.toExternalForm()),
 					catalogResolver);
 		} catch (IOException ioe) {
 			throw new MojoExecutionException("Failed to read stylesheet.", ioe);
 		}
 		TransformerBuilder builder = createTransformerBuilder(uriResolver);
 		EntityResolver resolver = catalogResolver;
 		InjectingEntityResolver injectingResolver = null;
 		if (getEntities() != null) {
 			injectingResolver = new InjectingEntityResolver(getEntities(),
 					resolver, getType(), getLog());
 			resolver = injectingResolver;
 		}
 		SAXParserFactory factory = createParserFactory();
 		for (int i = included.length - 1; i >= 0; i--) {
 			try {
 				if (injectingResolver != null) {
 					injectingResolver.forceInjection();
 				}
 				String filename = included[i];
 				String targetFilename = filename.substring(0,
 						filename.length() - 4)
 						+ "." + getTargetFileExtension();
 				File targetFile = new File(targetDirectory, targetFilename);
 				File sourceFile = new File(sourceDirectory, filename);
 				if (!targetFile.exists()
 						|| (targetFile.exists() && FileUtils.isFileNewer(
 								sourceFile, targetFile))) {
                     Result result = new StreamResult(targetFile.getAbsolutePath());
 					XMLReader reader = factory.newSAXParser().getXMLReader();
 					reader.setEntityResolver(resolver);
 					PreprocessingFilter filter = new PreprocessingFilter(reader);
 					ProcessingInstructionHandler resolvingHandler = new ExpressionHandler(
 							new VariableResolver() {
 
 								public Object resolveVariable(String name)
 										throws ELException {
 									if ("date".equals(name)) {
 										return DateFormat.getDateInstance(
 												DateFormat.LONG).format(
 												new Date());
 									} else if ("project".equals(name)) {
 										return getMavenProject();
 									} else {
 										return getMavenProject().getProperties().get(name);
 									}
 								}
 
 							}, getLog());
 					filter.setHandlers(Arrays
 							.asList(new Object[] { resolvingHandler }));
 					filter.setEntityResolver(resolver);
 					getLog().info("Processing " + filename);
 
 					SAXSource xmlSource = null;
 					// if both properties are set, XOM is used for a better
 					// XInclude support.
 					if (getXIncludeSupported()
 							&& getGeneratedSourceDirectory() != null) {
             getLog().debug("Advanced XInclude mode entered");
 						final Builder xomBuilder = new Builder();
 						try {
 							final nu.xom.Document doc = xomBuilder
 									.build(sourceFile);
 							XIncluder.resolveInPlace(doc);
 							// TODO also dump PIs computed and Entities included
 							final File dump = dumpResolvedXML(filename, doc);
 							xmlSource = new SAXSource(filter, new InputSource(
 									dump.getAbsolutePath()));
 						} catch (ValidityException e) {
 							throw new MojoExecutionException(
 									"Failed to validate source", e);
 						} catch (ParsingException e) {
 							throw new MojoExecutionException(
 									"Failed to parse source", e);
 						} catch (IOException e) {
 							throw new MojoExecutionException(
 									"Failed to read source", e);
 						} catch (XIncludeException e) {
 							throw new MojoExecutionException(
 									"Failed to process XInclude", e);
 						}
 					} else // else fallback on Xerces XInclude support.
 					{
             getLog().debug("Xerces XInclude mode entered");
 						InputSource inputSource = new InputSource(sourceFile
 								.getAbsolutePath());
 						xmlSource = new SAXSource(filter, inputSource);
 					}
 					Transformer transformer = builder.build();
 					adjustTransformer(transformer,
 							sourceFile.getAbsolutePath(), targetFile);
 					transformer.transform(xmlSource, result);
 					postProcessResult(targetFile);
           getLog().debug(targetFile + " has been generated.");
 				} else {
 					getLog().debug(targetFile + " is up to date.");
 				}
 			} catch (SAXException saxe) {
 				throw new MojoExecutionException("Failed to parse "
 						+ included[i] + ".", saxe);
 			} catch (TransformerException te) {
 				throw new MojoExecutionException("Failed to transform "
 						+ included[i] + ".", te);
 			} catch (ParserConfigurationException pce) {
 				throw new MojoExecutionException("Failed to construct parser.",
 						pce);
 			}
 		}
 		postProcess();
 	}
 
 	/**
 	 * Saves the Docbook XML file with all XInclude resolved.
 	 * 
 	 * @param initialFilename
 	 *            Filename of the root docbook source file.
 	 * @param doc
 	 *            XOM Document resolved.
 	 * @return The new file generated.
 	 * @throws MojoExecutionException
 	 */
 	protected File dumpResolvedXML(String initialFilename, nu.xom.Document doc)
 			throws MojoExecutionException {
 		final File file = new File(initialFilename);
 		final String parent = file.getParent();
 		File resolvedXML = null;
 		if (parent != null) {
 			resolvedXML = new File(getGeneratedSourceDirectory(), parent);
 			resolvedXML.mkdirs();
 			resolvedXML = new File(resolvedXML, "(gen)" + file.getName());
 		} else {
      getGeneratedSourceDirectory().mkdirs();
 			resolvedXML = new File(getGeneratedSourceDirectory(), "(gen)"
 					+ initialFilename);
 		}
 
 		FileOutputStream fos = null;
 		try {
 			fos = new FileOutputStream(resolvedXML);
 		} catch (FileNotFoundException e) {
 			throw new MojoExecutionException("Failed to open dump file", e);
 		}
 		if (fos != null) {
 			getLog().info("Dumping to " + resolvedXML.getAbsolutePath());
 			final BufferedOutputStream bos = new BufferedOutputStream(fos);
 			final Serializer serializer = new Serializer(bos);
 
 			try {
 				serializer.write(doc);
 				bos.flush();
 				bos.close();
 				fos.close();
 				return resolvedXML;
 			} catch (IOException e) {
 				throw new MojoExecutionException(
 						"Failed to write to dump file", e);
 			} finally {
 				IOUtils.closeQuietly(bos);
 				IOUtils.closeQuietly(fos);
 			}
 		}
 		throw new MojoExecutionException("Failed to open dump file");
 	}
 
 	/**
 	 * Returns the SAXParserFactory used for constructing parsers.
 	 * 
 	 */
 	private SAXParserFactory createParserFactory() {
 		SAXParserFactory factory = new SAXParserFactoryImpl();
 		factory.setXIncludeAware(getXIncludeSupported());
 		return factory;
 	}
 
 	/**
 	 * Returns a boolean indicating if XInclude should be supported.
 	 * 
 	 * @return A boolean indicating if XInclude should be supported.
 	 */
 	protected abstract boolean getXIncludeSupported();
 
 	/**
 	 * Returns the directory to use to save the resolved docbook XML before it
 	 * is given to the Transformer.
 	 * 
 	 * @return
 	 */
 	protected abstract File getGeneratedSourceDirectory();
 
 	/**
 	 * The stylesheet location override by a class in the mojo hierarchy.
 	 * 
 	 * @return The location of the stylesheet set by one of the superclasses, or
 	 *         <code>null</code>.
 	 */
 	protected String getNonDefaultStylesheetLocation() {
 		return null;
 	}
 
 	/**
 	 * The operation to override when it is required to make some adjustments to
 	 * the {@link Transformer} right before it is applied to a certain source
 	 * file. The two parameters provide some context, allowing implementers to
 	 * respond to specific conditions for specific files.
 	 * 
 	 * @param transformer
 	 *            The <code>Transformer</code> that must be adjusted.
 	 * @param sourceFilename
 	 *            The name of the source file that is being transformed.
 	 * @param targetFile
 	 *            The target File.
 	 */
 	public void adjustTransformer(Transformer transformer,
 			String sourceFilename, File targetFile) {
 		// To be implemented by subclasses.
 	}
 
     /**
      * Allows subclasses to add their own specific pre-processing logic.
      *
      * @throws MojoExecutionException If the Mojo fails to pre-process the results.
      */
     public void preProcess() throws MojoExecutionException {
         // save system properties
         originalSystemProperties = (Properties) System.getProperties().clone();
         // set the new properties
         if (getSystemProperties() != null) {
             final Enumeration props = getSystemProperties().keys();
             while(props.hasMoreElements()) {
                 final String key = (String)props.nextElement();
                 System.setProperty(key, getSystemProperties().getProperty(key));
             }
         }
 
         if (getPreProcess() != null) {
             executeTasks(getPreProcess(), getMavenProject());
 		}
 	}
 
 	/**
 	 * Allows classes to add their own specific post-processing logic.
 	 * 
 	 * @throws MojoExecutionException
 	 *             If the Mojo fails to post-process the results.
 	 */
 	public void postProcess() throws MojoExecutionException {
 		if (getPostProcess() != null) {
 			executeTasks(getPostProcess(), getMavenProject());
 		}
 
         // restore system properties
         if (originalSystemProperties != null) {
             System.setProperties(originalSystemProperties);
         }
     }
 
 	/**
 	 * Post-processes the file. (Might be changed in the future to except an XML
 	 * representation instead of a file, in order to prevent the file from being
 	 * parsed.)
 	 * 
 	 * @param result
 	 *            An individual result.
 	 */
 	public void postProcessResult(File result) throws MojoExecutionException {
 
 	}
 
 	/**
 	 * Creates a <code>CatalogManager</code>, used to resolve DTDs and other
 	 * entities.
 	 * 
 	 * @return A <code>CatalogManager</code> to be used for resolving DTDs and
 	 *         other entities.
 	 */
 	protected CatalogManager createCatalogManager() {
 		CatalogManager manager = new CatalogManager();
 		manager.setIgnoreMissingProperties(true);
 		ClassLoader classLoader = Thread.currentThread()
 				.getContextClassLoader();
 		StringBuffer builder = new StringBuffer();
 		boolean first = true;
 		try {
 			Enumeration enumeration = classLoader.getResources("/catalog.xml");
 			while (enumeration.hasMoreElements()) {
 				if (!first) {
 					builder.append(';');
 				} else {
 					first = false;
 				}
 				URL resource = (URL) enumeration.nextElement();
 				builder.append(resource.toExternalForm());
 			}
 		} catch (IOException ioe) {
 			getLog().warn("Failed to search for catalog files.");
 			// Let's be a little tolerant here.
 		}
 		String catalogFiles = builder.toString();
 		if (catalogFiles.length() == 0) {
 			getLog().warn("Failed to find catalog files.");
 		} else {
 			manager.setCatalogFiles(catalogFiles);
 		}
 		return manager;
 	}
 
 	/**
 	 * Creates a <code>DocumentBuilder</code> to be used to parse DocBook XML
 	 * documents.
 	 * 
 	 * @return A <code>DocumentBuilder</code> instance.
 	 * @throws MojoExecutionException
 	 *             If we cannot create an instance of the
 	 *             <code>DocumentBuilder</code>.
 	 */
 	protected DocumentBuilder createDocumentBuilder()
 			throws MojoExecutionException {
 		try {
 			DocumentBuilderFactory factory = DocumentBuilderFactory
 					.newInstance();
 			DocumentBuilder builder = factory.newDocumentBuilder();
 			return builder;
 		} catch (ParserConfigurationException pce) {
 			throw new MojoExecutionException("Failed to construct parser.", pce);
 		}
 	}
 
 	/**
 	 * Creates an instance of an XPath expression for picking the title from a
 	 * document.
 	 * 
 	 * @return An XPath expression to pick the title from a document.
 	 * @throws MojoExecutionException
 	 *             If the XPath expression cannot be parsed.
 	 */
 	protected XPath createTitleXPath() throws MojoExecutionException {
 		try {
 			StringBuffer builder = new StringBuffer();
 			builder
 					.append("(article/title|article/articleinfo/title|book/title|book/bookinfo/title)[position()=1]");
 			return new DOMXPath(builder.toString());
 		} catch (JaxenException je) {
 			throw new MojoExecutionException("Failed to parse XPath.", je);
 		}
 	}
 
 	/**
 	 * Constructs the default {@link TransformerBuilder}.
 	 */
 	protected TransformerBuilder createTransformerBuilder(URIResolver resolver) {
 		return new CachingTransformerBuilder(new DefaultTransformerBuilder(
 				resolver));
 	}
 
 	/**
 	 * Returns the title of the document.
 	 * 
 	 * @param document
 	 *            The document from which we want the title.
 	 * @return The title of the document, or <code>null</code> if we can't
 	 *         find the title.
 	 */
 	private String getTitle(Document document) throws MojoExecutionException {
 		try {
 			XPath titleXPath = createTitleXPath();
 			Node titleNode = (Node) titleXPath.selectSingleNode(document);
 			if (titleNode != null) {
 				return titleNode.getNodeValue();
 			} else {
 				return null;
 			}
 		} catch (JaxenException je) {
 			getLog().debug("Failed to find title of document.");
 			return null;
 		}
 	}
 
 	protected void executeTasks(Target antTasks, MavenProject mavenProject)
 			throws MojoExecutionException {
 		try {
 			ExpressionEvaluator exprEvaluator = (ExpressionEvaluator) antTasks
 					.getProject().getReference("maven.expressionEvaluator");
 			Project antProject = antTasks.getProject();
 			PropertyHelper propertyHelper = PropertyHelper
 					.getPropertyHelper(antProject);
 			propertyHelper.setNext(new AntPropertyHelper(exprEvaluator,
 					getLog()));
 			DefaultLogger antLogger = new DefaultLogger();
 			antLogger.setOutputPrintStream(System.out);
 			antLogger.setErrorPrintStream(System.err);
 			antLogger.setMessageOutputLevel(2);
 			antProject.addBuildListener(antLogger);
 			antProject.setBaseDir(mavenProject.getBasedir());
 			Path p = new Path(antProject);
 			p.setPath(StringUtils.join(mavenProject.getArtifacts().iterator(),
 					File.pathSeparator));
 			antProject.addReference("maven.dependency.classpath", p);
 			p = new Path(antProject);
 			p.setPath(StringUtils.join(mavenProject
 					.getCompileClasspathElements().iterator(),
 					File.pathSeparator));
 			antProject.addReference("maven.compile.classpath", p);
 			p = new Path(antProject);
 			p.setPath(StringUtils.join(mavenProject
 					.getRuntimeClasspathElements().iterator(),
 					File.pathSeparator));
 			antProject.addReference("maven.runtime.classpath", p);
 			p = new Path(antProject);
 			p.setPath(StringUtils.join(mavenProject.getTestClasspathElements()
 					.iterator(), File.pathSeparator));
 			antProject.addReference("maven.test.classpath", p);
 			List artifacts = getArtifacts();
 			List list = new ArrayList(artifacts.size());
 			File file;
 			for (Iterator i = artifacts.iterator(); i.hasNext(); list.add(file
 					.getPath())) {
 				Artifact a = (Artifact) i.next();
 				file = a.getFile();
 				if (file == null)
 					throw new DependencyResolutionRequiredException(a);
 			}
 
 			p = new Path(antProject);
 			p.setPath(StringUtils.join(list.iterator(), File.pathSeparator));
 			antProject.addReference("maven.plugin.classpath", p);
 			getLog().info("Executing tasks");
 			antTasks.execute();
 			getLog().info("Executed tasks");
 		} catch (Exception e) {
 			throw new MojoExecutionException("Error executing ant tasks", e);
 		}
 	}
 
 	/**
 	 * The default policy for constructing Transformers.
 	 */
 	private class DefaultTransformerBuilder implements TransformerBuilder {
 
 		/**
 		 * The standard {@link URIResolver}.
 		 */
 		private URIResolver resolver;
 
 		public DefaultTransformerBuilder(URIResolver resolver) {
 			this.resolver = resolver;
 		}
 
 		public Transformer build() throws TransformerBuilderException {
 			Transformer transformer = createTransformer(resolver);
 			transformer.setURIResolver(resolver);
 			return transformer;
 		}
 
 		/**
 		 * Returns a <code>Transformer</code> capable of rendering a
 		 * particular type of output from DocBook input.
 		 * 
 		 * @param uriResolver
 		 * 
 		 * @return A <code>Transformer</code> capable of rendering a
 		 *         particular type of output from DocBook input.
 		 * @throws MojoExecutionException
 		 *             If the operation fails to create a
 		 *             <code>Transformer</code>.
 		 */
 		protected Transformer createTransformer(URIResolver uriResolver)
 				throws TransformerBuilderException {
 			URL url = getStylesheetURL();
 			try {
 				TransformerFactory transformerFactory = new TransformerFactoryImpl();
 				transformerFactory.setURIResolver(uriResolver);
 				Source source = new StreamSource(url.openStream(), url
 						.toExternalForm());
 				Transformer transformer = transformerFactory
 						.newTransformer(source);
 				Controller controller = (Controller) transformer;
 				try {
 					controller.makeMessageEmitter();
 					controller.getMessageEmitter().setWriter(new NullWriter());
 				} catch (TransformerException te) {
 					getLog()
 							.error("Failed to redirect xsl:message output.", te);
 				}
 				if (getCustomizationParameters() != null) {
 					getLog().info("Applying customization parameters");
 					final Iterator iterator = getCustomizationParameters()
 							.iterator();
 					while (iterator.hasNext()) {
 						Parameter param = (Parameter) iterator.next();
 						if (param.getName() != null) // who knows
 						{
 							transformer.setParameter(param.getName(), param
 									.getValue());
 						}
 					}
 				}
 				configure(transformer);
 				return transformer;
 			} catch (IOException ioe) {
 				throw new TransformerBuilderException(
 						"Failed to read stylesheet from "
 								+ url.toExternalForm(), ioe);
 			} catch (TransformerConfigurationException tce) {
 				throw new TransformerBuilderException(
 						"Failed to build Transformer from "
 								+ url.toExternalForm(), tce);
 			}
 		}
 
 	}
 
 	/**
 	 * Configure the Transformer by passing in some parameters.
 	 * 
 	 * @param transformer
 	 *            The Transformer that needs to be configured.
 	 */
 	protected abstract void configure(Transformer transformer);
 
 	/**
 	 * Returns the target directory in which all results should be placed.
 	 * 
 	 * @return The target directory in which all results should be placed.
 	 */
 	protected abstract File getTargetDirectory();
 
 	/**
 	 * Returns the source directory containing the source XML files.
 	 * 
 	 * @return The source directory containing the source XML files.
 	 */
 	protected abstract File getSourceDirectory();
 
 	/**
 	 * Returns the include patterns, as a comma-seperate collection of patterns.
 	 */
 	protected abstract String[] getIncludes();
 
 	/**
 	 * Returns the URL of the stylesheet. You can override this operation to
 	 * return a URL pointing to a stylesheet residing on a location that can be
 	 * adressed by a URL. By default, it will return a stylesheet that will be
 	 * loaded from the classpath, using the resource name returned by
 	 * {@link #getStylesheetLocation()}.
 	 * 
 	 * @return The URL of the stylesheet.
 	 */
 	protected URL getStylesheetURL() {
 		URL url = this.getClass().getClassLoader().getResource(
 				getStylesheetLocation());
 		if (url == null) {
 			try {
 				if (getStylesheetLocation().startsWith("http://")) {
 					return new URL(getStylesheetLocation());
 				}
 				return new File(getStylesheetLocation()).toURL();
 			} catch (MalformedURLException mue) {
 				return null;
 			}
 		} else {
 			return url;
 		}
 	}
 
 	/**
 	 * Returns the URL of the default stylesheet.
 	 * 
 	 * @return The URL of the stylesheet.
 	 */
 	protected URL getNonDefaultStylesheetURL() {
 		if (getNonDefaultStylesheetLocation() != null) {
 			URL url = this.getClass().getClassLoader().getResource(
 					getNonDefaultStylesheetLocation());
 			return url;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the URL of the default stylesheet.
 	 * 
 	 * @return The URL of the stylesheet.
 	 */
 	protected URL getDefaultStylesheetURL() {
 		URL url = this.getClass().getClassLoader().getResource(
 				getDefaultStylesheetLocation());
 		return url;
 	}
 
 	/**
 	 * Returns the default stylesheet location within the root of the stylesheet
 	 * distribution.
 	 * 
 	 * @return The location of the directory containing the stylesheets.
 	 */
 	protected abstract String getDefaultStylesheetLocation();
 
 	/**
 	 * Returns the actual stylesheet location.
 	 * 
 	 * @return The actual stylesheet location.
 	 */
 	protected abstract String getStylesheetLocation();
 
 	/**
 	 * Returns the extension of the target files, e.g. "html" for HTML files,
 	 * etc.
 	 * 
 	 * @return The extension of the target files.
 	 */
 	protected abstract String getTargetFileExtension();
 
 	/**
 	 * Returns a list of {@link Entity Entities}
 	 */
 	protected abstract List getEntities();
 
 	/**
      * A list of additional XSL parameters to give to the XSLT engine.
      * These parameters overrides regular docbook ones as they are last
      * configured.<br/>
      * For regular docbook parameters prefer the use of this plugin facilities
      * offering named paramters.<br/>
      * These parameters feet well for custom properties you may have defined
      * within your customization layer.
      * 
 	 * {@link Parameter customizationParameters}
 	 */
 	protected abstract List getCustomizationParameters();
 
     /**
      * A copy of JVM system properties before plugin process.
      */
     private Properties originalSystemProperties;
 
     /**
      * Returns the additional System Properties. JVM System Properties are copied back if no problem have occurred
      * during the plugin process.
      *
      * @return The current forked System Properties.
      */
     protected abstract Properties getSystemProperties();
 
     /**
      * Returns the tasks that should be executed before the transformation.
 	 * 
 	 * @return The tasks that should be executed before the transformation.
 	 */
 	protected abstract Target getPreProcess();
 
 	/**
 	 * Returns the tasks that should be executed after the transformation.
 	 * 
 	 * @return The tasks that should be executed after the transformation.
 	 */
 	protected abstract Target getPostProcess();
 
 	/**
 	 * Returns a reference to the current project.
 	 * 
 	 * @return A reference to the current project.
 	 */
 	protected abstract MavenProject getMavenProject();
 
 	/**
 	 * Returns the plugin dependencies.
 	 * 
 	 * @return The plugin dependencies.
 	 */
 	protected abstract List getArtifacts();
 
 	/**
 	 * Returns the type of conversion.
 	 */
 	protected abstract String getType();
 
 	/**
 	 * Converts a String parameter to the type expected by the XSLT processor.
 	 */
 	protected Object convertStringToXsltParam(String value) {
 		return value;
 	}
 
 	/**
 	 * Converts a Boolean parameter to the type expected by the XSLT processor.
 	 */
 	protected Object convertBooleanToXsltParam(String value) {
 		String trimmed = value.trim();
 		if ("false".equals(trimmed) || "0".equals(trimmed)) {
 			return "0";
 		} else {
 			return "1";
 		}
 	}
 
 }
