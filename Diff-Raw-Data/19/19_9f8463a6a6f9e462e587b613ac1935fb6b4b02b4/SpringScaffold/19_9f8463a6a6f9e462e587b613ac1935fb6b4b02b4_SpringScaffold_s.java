 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2011, Red Hat, Inc., and individual contributors
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package org.jboss.forge.scaffold.spring;
 
 import static org.jvnet.inflector.Noun.pluralOf;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.enterprise.event.Event;
 import javax.inject.Inject;
 import org.jboss.forge.parser.JavaParser;
 import org.jboss.forge.parser.java.JavaClass;
 import org.jboss.forge.parser.java.JavaInterface;
 import org.jboss.forge.parser.xml.Node;
 import org.jboss.forge.parser.xml.XMLParser;
 import org.jboss.forge.project.Project;
 import org.jboss.forge.project.dependencies.DependencyBuilder;
 import org.jboss.forge.project.facets.BaseFacet;
 import org.jboss.forge.project.facets.DependencyFacet;
 import org.jboss.forge.project.facets.JavaSourceFacet;
 import org.jboss.forge.project.facets.MetadataFacet;
 import org.jboss.forge.project.facets.WebResourceFacet;
 import org.jboss.forge.project.facets.events.InstallFacets;
 import org.jboss.forge.resources.FileResource;
 import org.jboss.forge.resources.Resource;
 import org.jboss.forge.resources.ResourceFilter;
 import org.jboss.forge.scaffold.AccessStrategy;
 import org.jboss.forge.scaffold.ScaffoldProvider;
 import org.jboss.forge.scaffold.TemplateStrategy;
 import org.jboss.forge.scaffold.spring.metawidget.config.ForgeConfigReader;
 import org.jboss.forge.scaffold.util.ScaffoldUtil;
 import org.jboss.forge.shell.ShellPrompt;
 import org.jboss.forge.shell.plugins.Alias;
 import org.jboss.forge.shell.plugins.RequiresFacet;
 import org.jboss.forge.shell.util.Streams;
 import org.jboss.forge.spec.javaee.PersistenceFacet;
 import org.jboss.seam.render.TemplateCompiler;
 import org.jboss.seam.render.spi.TemplateResolver;
 import org.jboss.seam.render.template.CompiledTemplateResource;
 import org.jboss.seam.render.template.resolver.ClassLoaderTemplateResolver;
 import org.metawidget.statically.StaticUtils.IndentedWriter;
 import org.metawidget.statically.javacode.StaticJavaMetawidget;
 import org.metawidget.statically.html.widgetbuilder.HtmlTag;
 import org.metawidget.statically.spring.StaticSpringMetawidget;
 import org.metawidget.util.CollectionUtils;
 import org.metawidget.util.XmlUtils;
 import org.metawidget.util.simple.StringUtils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 
 /**
  * Facet to generate a UI using the Spring JSP taglib.
  * <p>
  * This facet utilizes <a href="http://metawidget.org">Metawidget</a> internally. This enables the use of the Metawidget
  * SPI (pluggable WidgetBuilders, Layouts etc) for customizing the generated User Interface. For more information on
  * writing Metawidget plugins, see <a href="http://metawidget.org/documentation.php">the Metawidget documentation</a>.
  * <p>
  * This Facet does <em>not</em> require Metawidget to be in the final project.
  * 
  * @author <a href="mailto:ryan.k.bradley@gmail.com">Ryan Bradley</a>
  */
 
 @Alias("spring")
 @RequiresFacet({ DependencyFacet.class,
             WebResourceFacet.class,
             PersistenceFacet.class})
 public class SpringScaffold extends BaseFacet implements ScaffoldProvider {
     
     //
     // Private statics
     //
 
     private static String XMLNS_PREFIX = "xmlns:";
 
     private static final String SPRING_CONTROLLER_TEMPLATE = "scaffold/spring/SpringControllerTemplate.jv";
     private static final String DAO_INTERFACE_TEMPLATE = "scaffold/spring/DaoInterfaceTemplate.jv";
     private static final String DAO_IMPLEMENTATION_TEMPLATE = "scaffold/spring/DaoImplementationTemplate.jv";
     private static final String VIEW_TEMPLATE = "scaffold/spring/view.jsp";
     private static final String VIEW_ALL_TEMPLATE = "scaffold/spring/viewAll.jsp";
     private static final String UPDATE_TEMPLATE = "scaffold/spring/update.jsp";
     private static final String CREATE_TEMPLATE = "scaffold/spring/create.jsp";
     private static final String NAVIGATION_TEMPLATE = "scaffold/spring/pageTemplate.jsp";
     
     private static final String ERROR_TEMPLATE = "scaffold/spring/error.jsp";
     private static final String INDEX_TEMPLATE = "scaffold/spring/index.jsp";
 
     //
     // Protected members (nothing is private, to help sub-classing)
     //
 
     protected int backingBeanTemplateQbeMetawidgetIndent;
 
     protected CompiledTemplateResource springControllerTemplate;
     protected CompiledTemplateResource daoInterfaceTemplate;
     protected CompiledTemplateResource daoImplementationTemplate;
 
     protected CompiledTemplateResource viewAllTemplate;
     protected CompiledTemplateResource viewTemplate;
     protected Map<String, String> viewTemplateNamespaces;
     protected int viewTemplateEntityMetawidgetIndent;
 
     protected CompiledTemplateResource updateTemplate;
     protected Map<String, String> updateTemplateNamespaces;
     protected int updateTemplateEntityMetawidgetIndent;
 
     protected CompiledTemplateResource createTemplate;
     protected Map<String, String> createTemplateNamespaces;
     protected int createTemplateEntityMetawidgetIndent;
 
     protected CompiledTemplateResource navigationTemplate;
     protected int navigationTemplateIndent;
 
     protected CompiledTemplateResource errorTemplate;
     protected CompiledTemplateResource indexTemplate;   
     private TemplateResolver<ClassLoader> resolver;
     
     private ShellPrompt prompt;
     private TemplateCompiler compiler;
     private Event<InstallFacets> install;
     private StaticSpringMetawidget entityMetawidget;
     private StaticJavaMetawidget qbeMetawidget;
 
     private List<Resource<?>> generatedResources;
 
     //
     // Constructor
     //
     
     @Inject
     public SpringScaffold(final ShellPrompt prompt,
                     final TemplateCompiler compiler,
                     final Event<InstallFacets> install)
     {
         this.prompt = prompt;
         this.compiler = compiler;
         this.install = install;
         
         this.resolver = new ClassLoaderTemplateResolver(SpringScaffold.class.getClassLoader());
         
         if(this.compiler != null)
         {
             this.compiler.getTemplateResolverFactory().addResolver(this. resolver);
         }
 
         this.generatedResources = new ArrayList<Resource<?>>();
     }
     
     //
     // Public methods
     //
 
     @Override
     public List<Resource<?>> setup(String targetDir, Resource<?> template, boolean overwrite)
     {
         DependencyFacet deps = this.project.getFacet(DependencyFacet.class);
 
         List<Resource<?>> result = generateIndex(targetDir, template, overwrite);
 
         result.add(setupMVCContext(targetDir));
         result.add(setupTilesLayout(targetDir));
         result.add(updateWebXML());
 
         generatedResources.addAll(result);
 
         deps.addDirectDependency(DependencyBuilder.create("org.jboss.spec.javax.servlet:jboss-servlet-api_3.0_spec"));
         deps.addDirectDependency(DependencyBuilder.create("org.apache.tiles:tiles-jsp:2.1.3"));
 
         return result;
     }
 
     /**
      * Overridden to setup the Metawidgets.
      * <p>
      * Metawidgets must be configured per project <em>and per Forge invocation</em>. It is not sufficient to simply
      * configure them in <code>setup</code> because the user may restart Forge and not run <code>scaffold setup</code> a
      * second time.
      */    
     
     @Override
     public void setProject(Project project)
     {
         super.setProject(project);
         
         ForgeConfigReader configReader = new ForgeConfigReader(project);
         
         this.entityMetawidget = new StaticSpringMetawidget();
         this.entityMetawidget.setConfigReader(configReader);
         this.entityMetawidget.setConfig("scaffold/spring/metawidget-entity.xml");
         
         this.qbeMetawidget = new StaticJavaMetawidget();
         this.qbeMetawidget.setConfigReader(configReader);
         this.qbeMetawidget.setConfig("scaffold/spring/metawidget-qbe.xml");
     }
 
     @Override
     public List<Resource<?>> generateFromEntity(String targetDir, Resource<?> template, JavaClass entity, boolean overwrite)
     {
 
         // Save the current thread's ContextClassLoader, so that it can be restored later
 
         ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
 
         // Track the list of resources generated
 
         List<Resource<?>> result = new ArrayList<Resource<?>>();
 
         try {
 
             // Force the current thread to use the ScaffoldProvider's ContextClassLoader
 
             Thread.currentThread().setContextClassLoader(SpringScaffold.class.getClassLoader());
 
             try
             {
                 JavaSourceFacet java = this.project.getFacet(JavaSourceFacet.class);
                 WebResourceFacet web = this.project.getFacet(WebResourceFacet.class);
                 MetadataFacet meta = this.project.getFacet(MetadataFacet.class);
 
                 loadTemplates();
 
                 // Set context for Java and JSP generation
 
                 Map<Object, Object> context = CollectionUtils.newHashMap();
                 context = getTemplateContext(template);
                 context.put("entity", entity);
                 String ccEntity = StringUtils.decapitalize(entity.getName());
                 context.put("ccEntity", ccEntity);
                 context.put("daoPackage", meta.getTopLevelPackage() + ".repo");
                 context.put("entityName", StringUtils.uncamelCase(entity.getName()));
                 context.put("mvcPackage",  meta.getTopLevelPackage() + ".mvc");
                 String entityPlural = pluralOf(entity.getName());
                 context.put("entityPlural", entityPlural);
                 context.put("entityPluralName", pluralOf(StringUtils.uncamelCase(entity.getName())));
 
                 // Prepare qbeMetawidget
 
                 this.qbeMetawidget.setPath(entity.getQualifiedName());
                 StringWriter writer = new StringWriter();
                 this.qbeMetawidget.write(writer, backingBeanTemplateQbeMetawidgetIndent);
 
                 context.put("qbeMetawidget", writer.toString().trim());
                 context.put("qbeMetawidgetImports",
                         CollectionUtils.toString(this.qbeMetawidget.getImports(), ";\r\n", true, false));
 
                 // Prepare entity metawidget
 
                 this.entityMetawidget.putAttribute("value", ccEntity);
                 this.entityMetawidget.setPath(entity.getQualifiedName());
                 this.entityMetawidget.setReadOnly(false);
 
                 // Create a views.xml file containing all tiles definitions.
 
                 Node tilesDefinitions = new Node("tiles-definitions");
 
                 // Generate create
     
                 writeEntityMetawidget(context, this.createTemplateEntityMetawidgetIndent, this.createTemplateNamespaces);
     
                 result.add(ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource(targetDir + "/views/create" + entity.getName() + ".jsp"),
                         this.createTemplate.render(context), overwrite));
 
                 Node createDefinition = new Node("definition", tilesDefinitions);
                 createDefinition.attribute("name", "create" + entity.getName());
                 createDefinition.attribute("extends", "standard");
 
                 Node createTitleAttribute = new Node("put-attribute", createDefinition);
                 createTitleAttribute.attribute("name", "title");
                 createTitleAttribute.attribute("value", "Create New " + StringUtils.uncamelCase(entity.getName()));
 
                 Node createBodyAttribute = new Node("put-attribute", createDefinition);
                 createBodyAttribute.attribute("name", "body");
                 createBodyAttribute.attribute("value", targetDir + "/views/create" + entity.getName() + ".jsp");
 
                 // Generate update
 
                 writeEntityMetawidget(context, this.updateTemplateEntityMetawidgetIndent, this.updateTemplateNamespaces);
 
                 result.add(ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource(targetDir + "/views/update" + entity.getName() + ".jsp"),
                         this.updateTemplate.render(context), overwrite));
 
                 Node updateDefinition = new Node("definition", tilesDefinitions);
                 updateDefinition.attribute("name", "update" + entity.getName());
                 updateDefinition.attribute("extends", "standard");
 
                 Node updateTitleAttribute = new Node("put-attribute", updateDefinition);
                 updateTitleAttribute.attribute("name", "title");
                 updateTitleAttribute.attribute("value", "update " + StringUtils.uncamelCase(entity.getName()));
 
                 Node updateBodyAttribute = new Node("put-attribute", updateDefinition);
                 updateBodyAttribute.attribute("name", "body");
                 updateBodyAttribute.attribute("value", targetDir + "/views/update" + entity.getName() + ".jsp");
 
                 // Generate search and viewAll
 
                 writeEntityMetawidget(context, this.viewTemplateEntityMetawidgetIndent, this.viewTemplateNamespaces);
 
                 result.add(ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource(targetDir + "/views/" + entityPlural.toLowerCase() + ".jsp"),
                         this.viewAllTemplate.render(context), overwrite));
 
                 Node viewAllDefinition = new Node("definition", tilesDefinitions);
                 viewAllDefinition.attribute("name", entityPlural.toLowerCase());
                 viewAllDefinition.attribute("extends", "standard");
 
                 Node viewAllTitleAttribute = new Node("put-attribute", viewAllDefinition);
                 viewAllTitleAttribute.attribute("name", "title");
                 viewAllTitleAttribute.attribute("value", "View All " + pluralOf(StringUtils.uncamelCase(entity.getName())));
 
                 Node viewAllBodyAttribute = new Node("put-attribute", viewAllDefinition);
                 viewAllBodyAttribute.attribute("name", "body");
                 viewAllBodyAttribute.attribute("value", targetDir + "/views/" + entityPlural.toLowerCase() + ".jsp");
 
                 // Generate view
     
                 this.entityMetawidget.setReadOnly(true);
                 writeEntityMetawidget(context, this.viewTemplateEntityMetawidgetIndent, this.viewTemplateNamespaces);
     
                 result.add(ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource(targetDir + "/views/view" + entity.getName() + ".jsp"),
                         this.viewTemplate.render(context), overwrite));
 
                 Node viewDefinition = new Node("definition", tilesDefinitions);
                 viewDefinition.attribute("name", "view" + entity.getName());
                 viewDefinition.attribute("extends", "standard");
 
                 Node viewTitleAttribute = new Node("put-attribute", viewDefinition);
                 viewTitleAttribute.attribute("name", "title");
                 viewTitleAttribute.attribute("value", "View " + StringUtils.uncamelCase(entity.getName()));
 
                 Node viewBodyAttribute = new Node("put-attribute", viewDefinition);
                 viewBodyAttribute.attribute("name", "body");
                 viewBodyAttribute.attribute("value", targetDir + "/views/view" + entity.getName() + ".jsp");
 
                 // Generate navigation
     
                 result.add(generateNavigation(targetDir, overwrite));
 
                 Node indexDefinition = new Node("definition", tilesDefinitions);
                 indexDefinition.attribute("name", "index");
                 indexDefinition.attribute("extends", "standard");
 
                 Node indexTitleAttribute = new Node("put-attribute", indexDefinition);
                 indexTitleAttribute.attribute("name", "title");
                 indexTitleAttribute.attribute("value", "Welcome to Forge");
 
                 Node indexBodyAttribute = new Node("put-attribute", indexDefinition);
                 indexBodyAttribute.attribute("name", "body");
                indexBodyAttribute.attribute("value", targetDir + "/views/index.jsp");
 
                 String viewsFile = XMLParser.toXMLString(tilesDefinitions);
                 viewsFile = viewsFile.substring(0, 55) + "\n<!DOCTYPE tiles-definitions PUBLIC\n\"-//Apache Software Foundation"
                 + "//DTD Tiles Configuration 2.0//EN\"\n\"http://tiles.apache.org/dtds/tiles-config_2_0.dtd\">\n\n" + viewsFile.substring(55);
                 result.add(web.createWebResource(viewsFile.toCharArray(), targetDir + "/views/views.xml"));
 
                 JavaInterface daoInterface = JavaParser.parse(JavaInterface.class, this.daoInterfaceTemplate.render(context));
                 JavaClass daoImplementation = JavaParser.parse(JavaClass.class, this.daoImplementationTemplate.render(context));
     
                 // Save the created interface and class implementation, so they can be referenced by the controller.
     
                 java.saveJavaSource(daoInterface);
                 result.add(ScaffoldUtil.createOrOverwrite(this.prompt, java.getJavaResource(daoInterface),
                         daoInterface.toString(), overwrite));
     
                 java.saveJavaSource(daoImplementation);
                 result.add(ScaffoldUtil.createOrOverwrite(this.prompt, java.getJavaResource(daoImplementation),
                         daoImplementation.toString(), overwrite));
     
                 // Create a Spring MVC controller for the passed entity, using SpringControllerTemplate.jv
     
                 JavaClass entityController = JavaParser.parse(JavaClass.class, this.springControllerTemplate.render(context));
                 java.saveJavaSource(entityController);
                 result.add(ScaffoldUtil.createOrOverwrite(this.prompt, java.getJavaResource(entityController),
                         entityController.toString(), overwrite));
 
             }
             catch (Exception e)
             {
                 throw new RuntimeException("Error generating Spring scaffolding: " + entity.getName(), e);
             }
         }
         finally {
 
             // Restore the original ContextClassLoader
 
             Thread.currentThread().setContextClassLoader(oldClassLoader);
         }
 
         generatedResources.addAll(result);
 
         return result;
     }
 
     @Override
     @SuppressWarnings("unchecked")    
     public boolean install()
     {
 
         if(!(this.project.hasFacet(WebResourceFacet.class) && this.project.hasFacet(PersistenceFacet.class)))
         {
             this.install.fire(new InstallFacets(WebResourceFacet.class, PersistenceFacet.class));
         }
         
         return true;
     }
 
     @Override
     public boolean isInstalled()
     {
         return true;
     }
 
     @Override
     public List<Resource<?>> generateIndex(String targetDir, Resource<?> template, boolean overwrite)
     {
         List<Resource<?>> result = new ArrayList<Resource<?>>();
         WebResourceFacet web = this.project.getFacet(WebResourceFacet.class);
 
         loadTemplates();
 
 //        generateTemplates(overwrite);
         HashMap<Object, Object> context = getTemplateContext(template);
 
         // Basic pages
 
        result.add(ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource(targetDir + "/views/index.jsp"),
                 this.indexTemplate.render(context), overwrite));
 
        result.add(ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource(targetDir + "/views/error.jsp"),
                 this.errorTemplate.render(context), overwrite));
 
         // Static resources
 
         result.add(ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource(targetDir + "/add.png"),
                 getClass().getResourceAsStream("/scaffold/spring/add.png"), overwrite));
 
        result.add(ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource(targetDir + "/background.gif"),
                 getClass().getResourceAsStream("/scaffold/spring/background.gif"), overwrite));
 
        result.add(ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource(targetDir + "/false.png"),
                 getClass().getResourceAsStream("/scaffold/spring/false.png"), overwrite));
 
        result.add(ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource(targetDir + "/favicon.ico"),
                 getClass().getResourceAsStream("/scaffold/spring/favicon.ico"), overwrite));
 
        result.add(ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource(targetDir + "/forge-logo.png"),
                 getClass().getResourceAsStream("/scaffold/spring/forge-logo.png"), overwrite));
 
        result.add(ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource(targetDir + "/forge-style.css"),
                 getClass().getResourceAsStream("/scaffold/spring/forge-style.css"), overwrite));
 
        result.add(ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource(targetDir + "/jboss-community.png"),
                 getClass().getResourceAsStream("/scaffold/spring/jboss-community.png"), overwrite));
 
        result.add(ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource(targetDir + "/remove.png"),
                 getClass().getResourceAsStream("/scaffold/spring/remove.png"), overwrite));
 
        result.add(ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource(targetDir + "/search.png"),
                 getClass().getResourceAsStream("/scaffold/spring/search.png"), overwrite));
 
        result.add(ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource(targetDir + "/true.png"),
                 getClass().getResourceAsStream("/scaffold/spring/true.png"), overwrite));
 
        // TODO: Perhaps should be modified to only add index.jsp and error.jsp, and not all static resources.
 
        generatedResources.addAll(result);
 
        return result;
     }
 
     @Override
     public List<Resource<?>> generateTemplates(String targetDir, final boolean overwrite)
     {
         List<Resource<?>> result = new ArrayList<Resource<?>>();
 
         try
         {
 /*            WebResourceFacet web = this.project.getFacet(WebResourceFacet.class);
 
             result.add(ScaffoldUtil.createOrOverwrite(this.prompt,
                     web.getWebResource("/resources/scaffold/paginator.xhtml"),
                     getClass().getResourceAsStream("/resources/scaffold/paginator.xhtml"),
                     overwrite));*/
 
             result.add(generateNavigation(targetDir, overwrite));
         } catch (Exception e)
         {
             throw new RuntimeException("Error generating default templates.", e);
         }
 
         return result;
     }
 
     // TODO: Perhaps this method should retrieve all generated resources in targetDir, but instead retrieves any generated resource.
 
     @Override
     public List<Resource<?>> getGeneratedResources(String targetDir)
     {
         return this.generatedResources;
     }
 
     @Override
     public AccessStrategy getAccessStrategy()
     {
         // No AccessStrategy required for Spring.
 
         return null;
     }
 
     @Override
     public TemplateStrategy getTemplateStrategy()
     {
         // No TemplateStrategy required for Spring.
 
         return null;
     }
 
     //
     // Protected methods (nothing is private, to help sub-classing)
     //
 
     protected Resource<?> setupMVCContext(String targetDir)
     {
         WebResourceFacet web = this.project.getFacet(WebResourceFacet.class);
         MetadataFacet meta = this.project.getFacet(MetadataFacet.class);
 
         // Create an mvc-context.xml file for the web application.
 
         Node beans = new Node("beans");
 
         // Add the appropriate schema references.
 
         beans.attribute("xmlns", "http://www.springframework.org/schema/beans");
         beans.attribute(XMLNS_PREFIX + "xsi", "http://www.w3.org/2001/XMLSchema-instance");
         beans.attribute(XMLNS_PREFIX + "mvc", "http://www.springframework.org/schema/mvc");
         beans.attribute(XMLNS_PREFIX + "context", "http://www.springframework.org/schema/context");
 
         String schemaLoc = "http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd";
         schemaLoc += " http://www.springframework.org/schema/context http://www.springframework.org/schema/context/spring-context.xsd";
         schemaLoc += " http://www.springframework.org/schema/mvc http://www.springframework.org/schema/mvc/spring-mvc.xsd";
         beans.attribute("xsi:schemaLocation", schemaLoc);
 
         // Scan the given package for any classes with MVC annotations.
 
         String mvcPackage = meta.getTopLevelPackage() + ".mvc";
         Node contextScan = new Node("context:component-scan", beans);
         contextScan.attribute("base-package", mvcPackage);
 
         // Indicate the use of annotations for Spring MVC, such as @Controller or @RequestMapping
 
         // Map view names to Tiles Definitions with support for partial re-rendering
 
         Node viewResolver = new Node("bean", beans);
         viewResolver.attribute("class", "org.springframework.web.servlet.view.tiles2.TilesViewResolver");
         viewResolver.attribute("id", "viewResolver");
 
         Node viewClass = new Node("property", viewResolver);
         viewClass.attribute("name", "viewClass");
         viewClass.attribute("value", "org.springframework.web.servlet.view.tiles2.TilesView");
 
         // Initialize the Apache Tiles CompositeView system
 
         Node tilesConfigurer = new Node("bean", beans);
         tilesConfigurer.attribute("class", "org.springframework.web.servlet.view.tiles2.TilesConfigurer");
         tilesConfigurer.attribute("id", "tilesConfigurer");
 
         Node definitions = new Node("property", tilesConfigurer);
         definitions.attribute("name", "definitions");
         Node list = new Node("list", definitions);
         list.createChild("value").text(targetDir + "/**/layouts.xml");
         list.createChild("value").text(targetDir + "/**/views.xml");;
 
         beans.createChild("mvc:annotation-driven");
 
         // Use the Spring MVC default servlet handler
         
         beans.createChild("mvc:default-servlet-handler");
 
         // Add a ViewResolver for any view generated by an error
 
 /*        Node errorViewResolver = new Node("bean", beans);
         errorViewResolver.attribute("class", "org.springframework.web.servlet.handler.SimpleMappingExceptionResolver");
         errorViewResolver.attribute("id", "errorViewResolver");
 
         Node exceptionProperty = new Node("property", errorViewResolver);
         exceptionProperty.attribute("name", "exceptionMappings");
         Node props = new Node("props", exceptionProperty);
         Node prop = new Node("prop", props);
         prop.attribute("key", "java.lang.Exception");
         prop.text("error");*/
 
         // Unnecessary if there is no static content, but harmless
 
         Node mvcStaticContent = new Node("mvc:resources", beans);
         mvcStaticContent.attribute("mapping", "/static/**");
         mvcStaticContent.attribute("location", "/");
 
         // Write the mvc-context file to 'src/main/webapp/WEB-INF/{lowercase-project-name}-mvc-context.xml'.
 
         String mvcContextFile = XMLParser.toXMLString(beans);
         String filename = "WEB-INF/" + meta.getProjectName().toLowerCase().replace(' ', '-') + "-mvc-context.xml";
         web.createWebResource(mvcContextFile.toCharArray(), filename);
         
         return web.getWebResource(filename);
     }
 
     private Resource<?> setupTilesLayout(String targetDir)
     {
         WebResourceFacet web = this.project.getFacet(WebResourceFacet.class);
 
         Node tilesDefinitions = new Node("tiles-definitions");
         Node standardDefinition = new Node("definition", tilesDefinitions);
         standardDefinition.attribute("name", "standard");
         standardDefinition.attribute("template", targetDir + "/layouts/pageTemplate.jsp");
 
         String tilesDefinitionFile = XMLParser.toXMLString(tilesDefinitions);
         tilesDefinitionFile = tilesDefinitionFile.substring(0, 55) + "\n<!DOCTYPE tiles-definitions PUBLIC\n\"-//Apache Software Foundation"
         + "//DTD Tiles Configuration 2.0//EN\"\n\"http://tiles.apache.org/dtds/tiles-config_2_0.dtd\">\n\n" + tilesDefinitionFile.substring(55);
 
         return web.createWebResource(tilesDefinitionFile.toCharArray(), targetDir + "/layouts/layouts.xml"); 
     }
 
     protected Resource<?> updateWebXML()
     {
         WebResourceFacet web = this.project.getFacet(WebResourceFacet.class);
         MetadataFacet meta = this.project.getFacet(MetadataFacet.class);
 
         String projectName = meta.getProjectName();
         String filename = "/WEB-INF/" + projectName.toLowerCase().replace(' ', '-') + "-mvc-context.xml";
 
         // Retrieve the existing web.xml file
 
         FileResource<?> webXML = web.getWebResource("WEB-INF/web.xml");
         Node webapp = XMLParser.parse(webXML.getResourceInputStream());
 
         // Define a dispatcher servlet, named after the project.
 
         if (webapp.get("servlet").isEmpty())
         {
             Node servlet = new Node("servlet", webapp);
             String servName = projectName.replace(' ', (char) 0);
             Node servletName = new Node("servlet-name", servlet);
             servletName.text(servName);
             Node servletClass = new Node("servlet-class", servlet);
             servletClass.text("org.springframework.web.servlet.DispatcherServlet");
             Node initParam = new Node("init-param", servlet);
             Node paramName = new Node("param-name", initParam);
             paramName.text("contextConfigLocation");
             Node paramValue = new Node("param-value", initParam);
             paramValue.text(filename);
             Node loadOnStartup = new Node("load-on-startup", servlet);
             loadOnStartup.text(1);            
         }
 
         // Map the servlet to the '/' URL
 
         if (webapp.get("servlet-mapping").isEmpty())
         {
             Node servletMapping = new Node("servlet-mapping", webapp);
             Node servletNameRepeat = new Node("servlet-name", servletMapping);
             servletNameRepeat.text(projectName.replace(' ', (char) 0));
             Node url = new Node("url-pattern", servletMapping);
             url.text('/');            
         }
 
         // Add a unique mapping for the error page
 
        // TODO: This may need to be refactored later, to allow multiple error page locations.
 
 /*        if (webapp.get("error-page").isEmpty())
         {
             Node errorPage = new Node("error-page", webapp);
             Node exceptionType = new Node("exception-type", errorPage);
             exceptionType.text("java.lang.Exception");
             Node location = new Node("location", errorPage);
             location.text("/WEB-INF/views/error.jsp");            
         }*/
 
         // Save the updated web.xml file
 
         String file = XMLParser.toXMLString(webapp);
         web.createWebResource(file.toCharArray(), "WEB-INF/web.xml");
         
         return web.getWebResource("WEB-INF/web.xml");
     }
 
     protected void loadTemplates()
     {
         // Compile the DAO interface Java template.
         
         if (this.daoInterfaceTemplate == null) {
             this.daoInterfaceTemplate = compiler.compile(DAO_INTERFACE_TEMPLATE);
         }
         
         // Compile the DAO interface implementation Java template.
         
         if (this.daoImplementationTemplate == null) {
             this.daoImplementationTemplate = compiler.compile(DAO_IMPLEMENTATION_TEMPLATE);
         }
         
         // Compile the Spring MVC controller Java template.
         
         if (this.springControllerTemplate == null) {
             this.springControllerTemplate = compiler.compile(SPRING_CONTROLLER_TEMPLATE);
         }
 
         if (this.viewAllTemplate == null)
         {
             this.viewAllTemplate = compiler.compile(VIEW_ALL_TEMPLATE);
         }
 
         if (this.viewTemplate == null)
         {
             this.viewTemplate = compiler.compile(VIEW_TEMPLATE);
             String template = Streams.toString(this.viewTemplate.getSourceTemplateResource().getInputStream());
             this.viewTemplateEntityMetawidgetIndent = parseIndent(template, "@{metawidget}");
         }
 
         if (this.updateTemplate == null)
         {
             this.updateTemplate = compiler.compile(UPDATE_TEMPLATE);
             String template = Streams.toString(this.updateTemplate.getSourceTemplateResource().getInputStream());
             this.updateTemplateEntityMetawidgetIndent = parseIndent(template, "@{metawidget}");
         }
 
         if (this.createTemplate == null)
         {
             this.createTemplate = compiler.compile(CREATE_TEMPLATE);
             String template = Streams.toString(this.createTemplate.getSourceTemplateResource().getInputStream());
             this.createTemplateEntityMetawidgetIndent = parseIndent(template, "@{metawidget}");
         }
 
         if (this.navigationTemplate == null)
         {
             this.navigationTemplate = compiler.compile(NAVIGATION_TEMPLATE);
             String template = Streams.toString(this.navigationTemplate.getSourceTemplateResource().getInputStream());
             this.navigationTemplateIndent = parseIndent(template, "@{navigation}");
         }
 
         if (this.errorTemplate == null)
         {
             this.errorTemplate = compiler.compile(ERROR_TEMPLATE);
         }
 
         if (this.indexTemplate == null)
         {
             this.indexTemplate = compiler.compile(INDEX_TEMPLATE);
         }
     }
 
     protected HashMap<Object, Object> getTemplateContext(final Resource<?> template)
     {
         HashMap<Object, Object> context = new HashMap<Object, Object>();
         context.put("template", template);
         context.put("templateStrategy", getTemplateStrategy());
         return context;
     }
 
     /**
      * Generates the navigation menu based on scaffolded entities.
      */
 
     protected Resource<?> generateNavigation(String targetDir, final boolean overwrite)
             throws IOException
     {
         WebResourceFacet web = this.project.getFacet(WebResourceFacet.class);
         HtmlTag unorderedList = new HtmlTag("ul");
 
         ResourceFilter filter = new ResourceFilter()
         {
             @Override
             public boolean accept(Resource<?> resource)
             {
                 FileResource<?> file = (FileResource<?>) resource;
 
                 if ( file.isDirectory() || file.getName().equals("META-INF")
                         || file.getName().equals("WEB-INF")
                         || file.getName().equals("resources"))
                 {
                     return false;
                 }
 
                 return true;
             }
         };
         
         for (Resource<?> resource : web.getWebResource(targetDir + "/views/").listResources(filter))
         {
 /*            HtmlAnchor link = new HtmlAnchor();
             link.putAttribute("href", targetDir+ "/views/" + pluralOf(resource.getName()).toLowerCase());
             link.setTextContent(StringUtils.uncamelCase(resource.getName()));
 
             HtmlTag listItem = new HtmlTag("li");
             listItem.getChildren().add(link);
             unorderedList.getChildren().add(listItem);
 */        }
 
         Writer writer = new IndentedWriter(new StringWriter(), this.navigationTemplateIndent);
         unorderedList.write(writer);
 
         Map<Object, Object> context = CollectionUtils.newHashMap();
         context.put("navigation", writer.toString().trim());
         context.put("targetDir", targetDir);
 
         if (this.navigationTemplate == null)
         {
             loadTemplates();
         }
 
         return ScaffoldUtil.createOrOverwrite(this.prompt, web.getWebResource(targetDir + "/layouts/pageTemplate.jsp"),
                 this.navigationTemplate.render(context), overwrite);
     }
 
     /**
      * Parses the given XML and determines what namespaces it already declares. These are later removed from the list of
      * namespaces that Metawidget introduces.
      */
 
     protected Map<String, String> parseNamespaces(final String template)
     {
         Map<String, String> namespaces = CollectionUtils.newHashMap();
         Document document = XmlUtils.documentFromString(template);
         Element element = document.getDocumentElement();
         NamedNodeMap attributes = element.getAttributes();
 
         for (int i = 0; i < attributes.getLength(); i++)
         {
             org.w3c.dom.Node node = attributes.item(i);
             String nodeName = node.getNodeName();
             int indexOf = nodeName.indexOf(XMLNS_PREFIX);
 
             if (indexOf == -1)
             {
                 continue;
             }
 
             namespaces.put(nodeName.substring(indexOf + XMLNS_PREFIX.length()), node.getNodeValue());
         }
 
         return namespaces;
     }
 
     /**
      * Parses the given XML and determines the indent of the given String namespaces that Metawidget introduces.
      */
 
     protected int parseIndent(final String template, final String indentOf)
     {
         int indent = 0;
         int indexOf = template.indexOf(indentOf);
 
         while ((indexOf > 0) && (template.charAt(indexOf) != '\n'))
         {
             if (template.charAt(indexOf) == '\t')
             {
                 indent++;
             }
 
             indexOf--;
         }
 
         return indent;
     }
 
     /**
      * Writes the entity Metawidget and its namespaces into the given context.
      */
 
     protected void writeEntityMetawidget(final Map<Object, Object> context, final int entityMetawidgetIndent,
             final Map<String, String> existingNamespaces)
     {
         StringWriter writer = new StringWriter();
         this.entityMetawidget.write(writer, entityMetawidgetIndent);
         context.put("metawidget", writer.toString().trim());
 
         Map<String, String> namespaces = this.entityMetawidget.getNamespaces();
 
         if (namespaces.keySet() != null && existingNamespaces != null)
         {
             namespaces.keySet().removeAll(existingNamespaces.keySet());
         }
 
         context.put("metawidgetNamespaces", namespacesToString(namespaces));
     }
 
     protected String namespacesToString(Map<String, String> namespaces)
     {
         StringBuilder builder = new StringBuilder();
 
         for (Map.Entry<String, String> entry : namespaces.entrySet())
         {
             // At the start, break out of the current quote. Field must be in quotes so that we're valid XML
 
             builder.append("\"\r\n\txmlns:");
             builder.append(entry.getKey());
             builder.append("=\"");
             builder.append(entry.getValue());
         }
 
         return builder.toString();
     }
 }
