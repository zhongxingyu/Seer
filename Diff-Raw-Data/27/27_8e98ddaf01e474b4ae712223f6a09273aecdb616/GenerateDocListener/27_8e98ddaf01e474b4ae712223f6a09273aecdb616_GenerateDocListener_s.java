 package com.masterdoc.listener;
 
 import static org.springframework.util.StringUtils.capitalize;
 
 import java.beans.IntrospectionException;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Serializable;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.lang.reflect.ParameterizedType;
 import java.lang.reflect.Type;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.ws.rs.Consumes;
 import javax.ws.rs.DELETE;
 import javax.ws.rs.GET;
 import javax.ws.rs.OPTIONS;
 import javax.ws.rs.POST;
 import javax.ws.rs.PUT;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 
 import org.apache.camel.Consume;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.plexus.logging.console.ConsoleLogger;
 import org.reflections.Reflections;
 import org.springframework.scheduling.annotation.Async;
 
 import com.googlecode.gentyref.GenericTypeReflector;
 import com.masterdoc.pojo.AbstractEntity;
 import com.masterdoc.pojo.CamelConsumeAnnotation;
 import com.masterdoc.pojo.Entity;
 import com.masterdoc.pojo.Enumeration;
 import com.masterdoc.pojo.Param;
 import com.masterdoc.pojo.Resource;
 import com.masterdoc.pojo.ResourceEntry;
 
 public class GenerateDocListener {
 
   // ----------------------------------------------------------------------
   // Constants
   // ----------------------------------------------------------------------
 
   public static final String   CLASS               = "class ";
   private List<Resource>       resources;
   private List<AbstractEntity> entities;
   private Set<Serializable>    entityList;
   private MavenProject         project;
   private ClassLoader          originalClassLoader = Thread.currentThread().getContextClassLoader();
 
   // ----------------------------------------------------------------------
   // Variables
   // ----------------------------------------------------------------------
 
   private ConsoleLogger        consoleLogger       = new ConsoleLogger();
 
   // ----------------------------------------------------------------------
   // Constructors
   // ----------------------------------------------------------------------
 
   public GenerateDocListener(MavenProject project, String... packageDocumentationResources) throws SecurityException, NoSuchFieldException,
       IllegalArgumentException, IllegalAccessException {
     consoleLogger.info("GenerateDocListener started");
     resources = new ArrayList<Resource>();
     entities = new ArrayList<AbstractEntity>();
     entityList = new HashSet<Serializable>();
     this.project = project;
     // ////////////////////
     List<URL> urls = new ArrayList<URL>();
 
     // get all the dependencies which are hidden in resolvedArtifacts of project object
     Field dependencies = MavenProject.class.
         getDeclaredField("resolvedArtifacts");
 
     dependencies.setAccessible(true);
 
     LinkedHashSet<Artifact> artifacts = (LinkedHashSet<Artifact>) dependencies.get(project);
 
     // noinspection unchecked
     for (Artifact artifact : artifacts) {
       try {
         urls.add(artifact.getFile().toURI().toURL());
       } catch (MalformedURLException e) {
         // logger.error(e);
       }
     }
 
     try {
       urls.add(new File(project.getBuild().getOutputDirectory()).toURI().toURL());
     } catch (MalformedURLException e) {
       consoleLogger.error(e.getMessage());
     }
 
     consoleLogger.debug("urls = \n" + urls.toString().replace(",", "\n"));
 
    Thread.currentThread().setContextClassLoader(new URLClassLoader(urls.toArray(new URL[urls.size()]), originalClassLoader));
     // ////////////////////
     for (String packageDocumentationResource : packageDocumentationResources) {
       consoleLogger.info("Generate REST documentation on package " + packageDocumentationResource + "...");
       startGeneration(new String[] { packageDocumentationResource });
     }
     // restore original classloader
     Thread.currentThread().setContextClassLoader(originalClassLoader);
   }
 
   // ----------------------------------------------------------------------
   // Public methods
   // ----------------------------------------------------------------------
 
   public void startGeneration(String[] args) {
     String packageDocumentationResource = args[0];
 
     if (null != packageDocumentationResource
         && packageDocumentationResource.length() > 0) {
       try {
         getDocResource(packageDocumentationResource);
         getEntities();
       } catch (NoSuchMethodException e) {
         e.printStackTrace(); // To change body of catch statement use
         // File | Settings | File Templates.
       } catch (ClassNotFoundException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
       } catch (IntrospectionException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
       }
     } else {
       consoleLogger
           .info("packageDocumentationResources not defined in plugin configuration");
     }
 
     ObjectMapper mapper = new ObjectMapper();
     // consoleLogger.info(mapper.defaultPrettyPrintingWriter()
     // .writeValueAsString(resources));
     // consoleLogger.info(mapper.defaultPrettyPrintingWriter()
     // .writeValueAsString(entities));
     try {
       File fileEntities = new File("/etc/entities.txt");
       BufferedWriter output = new BufferedWriter(new FileWriter(
           fileEntities));
       output.write(mapper.defaultPrettyPrintingWriter()
           .writeValueAsString(entities));
       output.close();
     } catch (IOException e) {
       e.printStackTrace();
     }
 
     try {
       File fileResources = new File("/etc/resources.txt");
       BufferedWriter output = new BufferedWriter(new FileWriter(
           fileResources));
       output.write(mapper.defaultPrettyPrintingWriter()
           .writeValueAsString(resources));
       output.close();
     } catch (IOException e) {
       e.printStackTrace();
     }
     int j;
   }
 
   // ----------------------------------------------------------------------
   // Private methods
   // ----------------------------------------------------------------------
   private void getEntities() throws ClassNotFoundException,
       IntrospectionException {
     for (Iterator<Serializable> iterator = entityList.iterator(); iterator
         .hasNext();) {
       Serializable entity = iterator.next();
       final Class<?> entityClass;
       try {
        entityClass = Class.forName(entity.toString());
       } catch (Exception e) {
         consoleLogger.info(entity.toString() + " is not forNamable");
         continue;
       }
       Entity newEntity = new Entity();
 
       newEntity.setName(entity.toString());
 
       if (entityClass.isEnum()) {
         newEntity.setEnumeration(true);
         newEntity.setFields(extractEnumFields(entity));
       } else {
         newEntity.setFields(extractFields(entity));
         entities.add(newEntity);
       }
     }
   }
 
   /**
    * Method to switch from class OR enum
    * 
    * @param entity
    * @return
    * @throws ClassNotFoundException
    * @throws IntrospectionException
    */
   private Map<String, AbstractEntity> extractFieldsSwitcher(
       Serializable entity) throws ClassNotFoundException,
       IntrospectionException {
     final Class<?> entityClass;
     try {
      entityClass = Class.forName(entity.toString());
     } catch (Exception e) {
       consoleLogger.info(entity.toString() + " is not forNamable");
       return null;
     }
     if (entityClass.isEnum()) {
       return extractEnumFields(entity);
     } else {
       return extractFields(entity);
     }
   }
 
   /**
    * Method to extract the class values.
    * 
    * @param entity
    * @return
    * @throws ClassNotFoundException
    * @throws IntrospectionException
    */
   private Map<String, AbstractEntity> extractFields(Serializable entity)
       throws ClassNotFoundException, IntrospectionException {
     Map<String, AbstractEntity> fields = new HashMap<String, AbstractEntity>();
    final Class<?> entityClass = Class.forName(entity.toString());
     consoleLogger
         .info(">>Extract fields for class " + entityClass + " ...");
     final java.lang.reflect.Field[] declaredFields = entityClass
         .getDeclaredFields();
     for (int i = 0; i < declaredFields.length; i++) {
       java.lang.reflect.Field declaredField = declaredFields[i];
       consoleLogger.info(">>>Extract field " + declaredField.getName()
           + " ...");
       final String type = extractTypeFromType(declaredField.getType());
       try {
         entityClass.getDeclaredMethod("get"
             + capitalize(declaredField.getName())); // GET OR IS
         Entity field = new Entity();
         field.setName(type);
         final Class<?> currEntityClass;
         try {
          currEntityClass = Class.forName(type);
          field.setEnumeration(Class.forName(type).isEnum());
         } catch (Exception e) {
           consoleLogger
               .info(entity.toString() + " is not forNamable");
           continue;
         }
         fields.put(declaredField.getName(), field);
         if (!entityList.contains(type)) {
           Entity newEntity = new Entity();
           newEntity.setName(field.getName());
           newEntity.setFields(extractFieldsSwitcher(type));
           newEntity.setEnumeration(currEntityClass.isEnum());
           // enum are added as enumeration object in enumeration
           // extract field method (do not add them as entity again)
           if (!currEntityClass.isEnum()) {
             entities.add(newEntity);
           }
         }
       } catch (NoSuchMethodException e) {
         try {
           entityClass.getDeclaredMethod("is"
               + capitalize(declaredField.getName())); // GET OR IS
           Entity field = new Entity();
           field.setName(declaredField.getName());
           final Class<?> currEntityClass;
           try {
            currEntityClass = Class.forName(type);
            field.setEnumeration(Class.forName(type).isEnum());
           } catch (Exception e2) {
             consoleLogger.info(entity.toString()
                 + " is not forNamable");
             continue;
           }
           fields.put(declaredField.getName(), field);
           if (!entityList.contains(type)) {
             Entity newEntity = new Entity();
             newEntity.setName(field.getName());
             newEntity.setFields(extractFieldsSwitcher(type));
             newEntity.setEnumeration(currEntityClass.isEnum());
             // enum are added as enumeration object in enumeration
             // extract field method (do not add them as entity
             // again)
             if (!currEntityClass.isEnum()) {
               entities.add(newEntity);
             }
           }
         } catch (NoSuchMethodException ex) {
           consoleLogger.info(">>>>Bypass : " + entityClass.toString()
               + "." + declaredField.getName());
           ; // Not a field with getter => bypass
         }
 
       }
     }
     return fields;
   }
 
   /**
    * Method to extract the enuration values.
    * 
    * @param entity
    * @return
    * @throws ClassNotFoundException
    * @throws IntrospectionException
    */
   private Map<String, AbstractEntity> extractEnumFields(Serializable entity)
       throws ClassNotFoundException, IntrospectionException {
     Map<String, AbstractEntity> fields = new HashMap<String, AbstractEntity>();
     List<String> values = new ArrayList<String>();
     String entityString = entity.toString();
     if (entityString.startsWith(CLASS)) {
       entityString = entityString.substring(entityString.indexOf(CLASS)
           + CLASS.length());
     }
     consoleLogger.info(">>>Extract enum " + entityString + " ...");
    final Class<?> entityClass = Class.forName(entityString);
     final Object[] declaredEnumConstants = entityClass.getEnumConstants();
     Enumeration newEnumeration = new Enumeration();
     newEnumeration.setName(entityString);
     for (int i = 0; i < declaredEnumConstants.length; i++) {
       values.add(declaredEnumConstants[i].toString());
     }
     newEnumeration.setValues(values);
     // do not add an enum if its already in the entities list
     if (!entities.contains(newEnumeration)) {
       entities.add(newEnumeration);
     }
     return fields;
   }
 
   /**
    * Get all @Path class and export all methods
    * 
    * @throws NoSuchFieldException
    * @throws SecurityException
    */
 
   private void getDocResource(String packageDocumentationResource) throws NoSuchMethodException {
     String mediaTypeProduces = null, mediaTypeConsumes = null;
 
     Reflections reflections = new Reflections(packageDocumentationResource);
     Set<Class<?>> reflectionResources = reflections
         .getTypesAnnotatedWith(Path.class);
     Set<String> serializables = reflections.getStore().getSubTypesOf(
         "java.io.Serializable");
     consoleLogger.info("Ressources : " + reflectionResources);
     consoleLogger.info("Possibles Dto(s) : " + serializables);
     for (Iterator<Class<?>> iterator = reflectionResources.iterator(); iterator
         .hasNext();) {
       Class<?> resource = iterator.next();
       if (!resource.isInterface()) {
         Resource res = new Resource();
 
         Annotation[] annotations = resource.getAnnotations();
         Method[] declaredMethods = resource.getDeclaredMethods();
 
         res.setEntryList(new TreeMap<String, ResourceEntry>());
         // Annotations for resource
         for (int i = 0; i < annotations.length; i++) {
           Annotation annotation = annotations[i];
           if (annotation instanceof Path) {
             res.setRootPath(((Path) annotation).value());
           }
           if (annotation instanceof Produces) {
             mediaTypeProduces = ((Produces) annotation).value()[0];
           }
           if (annotation instanceof Consumes) {
             mediaTypeConsumes = ((Consumes) annotation).value()[0];
           }
         }
 
         // Check methods
         Class<?> superclass = resource.getSuperclass();
         if (null != superclass
             && !superclass.getCanonicalName().equals(
                 "java.lang.Object")) {
           Method[] superclassDeclaredMethods = superclass
               .getDeclaredMethods();
           for (int i = 0; i < superclassDeclaredMethods.length; i++) {
             Method superclassDeclaredMethod = superclassDeclaredMethods[i];
             ResourceEntry resourceEntry = createResourceEntryFromMethod(
                 superclassDeclaredMethod, mediaTypeConsumes,
                 mediaTypeProduces, resource);
             if (null != resourceEntry) {
               resourceEntry.setFullPath(res.getRootPath() + "/"
                   + resourceEntry.getPath());
               res.getEntryList().put(
                   resourceEntry.calculateUniqKey(),
                   resourceEntry);
             }
           }
         }
         for (int i = 0; i < declaredMethods.length; i++) {
           Method declaredMethod = declaredMethods[i];
           ResourceEntry resourceEntry = createResourceEntryFromMethod(
               declaredMethod, mediaTypeConsumes,
               mediaTypeProduces, null);
           if (null != resourceEntry) {
             if (res.getEntryList().containsKey(
                 resourceEntry.calculateUniqKey())) {
               res.getEntryList().remove(
                   resourceEntry.calculateUniqKey());
             }
             resourceEntry.setFullPath(res.getRootPath() + "/"
                 + resourceEntry.getPath());
             res.getEntryList()
                 .put(resourceEntry.calculateUniqKey(),
                     resourceEntry);
           }
         }
 
         consoleLogger.info(">> " + resource.getCanonicalName());
         resources.add(res);
         extractEntityFromResourceEntries(res);
       } else {
         consoleLogger.info(">>skip " + resource.getCanonicalName());
       }
     }
   }
 
   private void extractEntityFromResourceEntries(Resource res) {
     Map<String, ResourceEntry> entryList = res.getEntryList();
     Set<String> set = entryList.keySet();
     for (Iterator<String> iterator = set.iterator(); iterator.hasNext();) {
       String key = iterator.next();
       ResourceEntry resourceEntry = entryList.get(key);
       Serializable requestEntity = resourceEntry.getRequestEntity();
       Serializable responseEntity = resourceEntry.getResponseEntity();
       if (null != requestEntity && !"null".equals(requestEntity)
           && !"void".equals(requestEntity)) {
         entityList.add(removeList(requestEntity));
       }
       if (null != responseEntity && !"null".equals(responseEntity)
           && !"void".equals(responseEntity)) {
         entityList.add(removeList(responseEntity));
       }
     }
   }
 
   private String removeList(Serializable entity) {
     String string = String.valueOf(entity);
     if (string.indexOf("<") > 0) {
       string = string.substring(string.indexOf("<") + 1,
           string.length() - 1);
     }
     return string;
   }
 
   private ResourceEntry createResourceEntryFromMethod(Method declaredMethod,
       String mediaTypeConsumes, String mediaTypeProduces,
       Class childResourceClass) {
     ResourceEntry resourceEntry = new ResourceEntry(mediaTypeConsumes,
         mediaTypeProduces);
     resourceEntry.setPath("/");
     Annotation[] declaredAnnotations = declaredMethod
         .getDeclaredAnnotations();
     for (int i = 0; i < declaredAnnotations.length; i++) {
       Annotation declaredAnnotation = declaredAnnotations[i];
       if (declaredAnnotation instanceof Path) {
         resourceEntry.setPath(((Path) declaredAnnotation).value());
       }
       if (declaredAnnotation instanceof Produces) {
         resourceEntry
             .setMediaTypeProduces(((Produces) declaredAnnotation)
                 .value()[0]);
       }
       if (declaredAnnotation instanceof Consumes) {
         resourceEntry
             .setMediaTypeConsumes(((Consumes) declaredAnnotation)
                 .value()[0]);
       }
       if (declaredAnnotation instanceof Consume) {
         CamelConsumeAnnotation camelConsumeAnnotation = new CamelConsumeAnnotation();
         camelConsumeAnnotation
             .setContext((((Consume) declaredAnnotation)).context());
         camelConsumeAnnotation.setUri((((Consume) declaredAnnotation))
             .uri());
         resourceEntry.setCamelConsume(camelConsumeAnnotation);
       }
       if (declaredAnnotation instanceof GET) {
         resourceEntry.setVerb("GET");
       }
       if (declaredAnnotation instanceof POST) {
         resourceEntry.setVerb("POST");
       }
       if (declaredAnnotation instanceof PUT) {
         resourceEntry.setVerb("PUT");
       }
       if (declaredAnnotation instanceof DELETE) {
         resourceEntry.setVerb("DELETE");
       }
       if (declaredAnnotation instanceof OPTIONS) {
         resourceEntry.setVerb("OPTIONS");
       }
       if (declaredAnnotation instanceof Async) {
         resourceEntry.setAsync(true);
       }
 
     }
 
     if (null == resourceEntry.getVerb()) {
       return null;
     }
 
     Object[] pType;
     if (null != childResourceClass) {
       pType = GenericTypeReflector.getExactParameterTypes(declaredMethod,
           childResourceClass);
     } else {
       pType = declaredMethod.getParameterTypes();
     }
 
     Annotation[][] pAnnot = declaredMethod.getParameterAnnotations();
     for (int i = 0; i < pType.length; i++) {
       String typeName = ((Class) pType[i]).getName();
       if ("java.util.HashMap".equals(typeName)) {
         Type[] types = declaredMethod.getGenericParameterTypes();
         ParameterizedType paramType = (ParameterizedType) types[i];
         typeName = extractTypeFromType(paramType);
       }
       if (pAnnot[i].length == 0) {
         resourceEntry.setRequestEntity(typeName);
       }
       for (int j = 0; j < pAnnot[i].length; j++) {
         Annotation annotation = pAnnot[i][j];
         if (annotation instanceof PathParam) {
           Param param = new Param();
           param.setType("PathParam");
           param.setClassName(typeName);
           param.setName(((PathParam) annotation).value());
           resourceEntry.getPathParams().add(param);
         }
         if (annotation instanceof QueryParam) {
           Param param = new Param();
           param.setType("QueryParam");
           param.setClassName(typeName);
           param.setName(((QueryParam) annotation).value());
           resourceEntry.getQueryParams().add(param);
         }
       }
     }
 
     if (null != childResourceClass) {
       Type exactReturnType = GenericTypeReflector.getExactReturnType(
           declaredMethod, childResourceClass);
       resourceEntry
           .setResponseEntity(extractTypeFromType(exactReturnType));
     } else {
       resourceEntry.setResponseEntity(extractTypeFromType(declaredMethod
           .getGenericReturnType()));
     }
 
     return resourceEntry;
   }
 
   private String extractTypeFromType(Type type) {
     String returnType = null;
     if (type instanceof ParameterizedType) {
       returnType = type.toString();
       if (returnType.startsWith(CLASS)) {
         returnType = returnType.substring(returnType.indexOf(CLASS));
       }
     } else {
       returnType = ((Class) type).getName();
     }
     return returnType;
   }
 }
