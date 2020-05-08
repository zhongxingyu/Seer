 /* 
  * WhiteCat - A dynamic role injector for agents.
  *
  * This project represents a new implementation of the so called BlackCat,
  * a project I made during my thesis degree. For more information about such project please see:
  * 
  *   G. Cabri, L. Ferrari, L. Leonardi,
  *   Injecting Roles in Java Agents Through Run-Time Bytecode Manipulation
  *   IBM Systems Journal, Vol. 44, No. 1, pp.185-208, 2005
  *
  * This new approach exploits a completely different implementation, keeping the
  * same idea of BlackCat.
  * 
  *
  * Copyright (C) Luca Ferrari 2008-2010 - cat4hire@users.sourceforge.net
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package whitecat.core;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.annotation.Annotation;
 import java.security.SecureClassLoader;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 
 
 import javassist.CannotCompileException;
 import javassist.ClassPool;
 import javassist.CtClass;
 import javassist.CtConstructor;
 import javassist.CtField;
 import javassist.CtMethod;
 import javassist.CtNewMethod;
 import javassist.Loader;
 import javassist.NotFoundException;
 import javassist.bytecode.AnnotationsAttribute;
 import javassist.bytecode.ClassFile;
 import javassist.bytecode.ClassFileWriter;
 import javassist.bytecode.ConstPool;
 import javassist.bytecode.MethodInfo;
 import javassist.bytecode.annotation.*;
 
 import whitecat.core.agents.AgentProxy;
 import whitecat.core.agents.MethodForwarderGenerator;
 import whitecat.core.agents.MethodForwarderGeneratorFactory;
 import whitecat.core.agents.ProxyHandlerFactory;
 import whitecat.core.agents.WCAgent;
 import whitecat.core.role.IManipulatedClass;
 import whitecat.core.role.IRole;
 import whitecat.core.annotation.*;
 import whitecat.core.exceptions.WCForwarderMethodException;
 import whitecat.example.DBProxy;
 import whitecat.example.DatabaseAdministrator;
 import whitecat.example.ILogger;
 
 import org.apache.log4j.*;
 import org.apache.log4j.xml.DOMConfigurator;
 import org.w3c.dom.DOMConfiguration;
 
 
 /**
  * This is the central role engine of the White Cat approach. Each role operation
  * is performed starting from this class.
  * 
  * The idea is the following: once an agent requires a role injection, the role is applied
  * to the proxy of the agent, that must be passed to the role engine itself. From the proxy it is created
  * a subclass (compatible with the proxy class itself) and this subclass is forced to implement the role
  * interface. Each method of such implementation is used as a forwarder to the real role instance, that is
  * owned by the agent itself.
  * @author Luca Ferrari - cat4hire (at) sourceforge.net
  *
  */
 public class RoleBooster extends SecureClassLoader{
 
     /**
      * The logger for this class loader.
      */
     private static Logger logger = org.apache.log4j.Logger.getLogger(RoleBooster.class);
     
     // configure the logger
     static{
 	DOMConfigurator.configure("conf/log4j.xml");
     }
     
     
     /**
      * The class loader parent of this loader.
      */
     private ClassLoader parentLoader = null;
     
     /**
      * The parent class pool.
      */
     private ClassPool myPool = null;
 
     private String roleRemovalSubClassName;
 
     private String roleRemovalSuperClassName;
     
     /**
      * The method forwarder generator that must be used for the current manipulation.
      */
     private MethodForwarderGenerator methodForwarderGenerator = null;
     
     
     /**
      * The role injection type.
      */
     private RoleInjectionType injectionType = RoleInjectionType.NONE;
     
     
     /**
      * A counter for the number of manipulation performed by this engine. Such number will
      * be used to create the name of the subclass that the engine is manipulating, this to
      * prevent two classes to have the same name and thus to prevent a linkage error.
      */
     private int manipulationCounter = 0;
 
     private String annotationClassName;
     
     
     
     
     /**
      * Provides the value of the manipulationCounter field. Please note that the
      * manipulation counter is automatically increased by this operation, thus the
      * counter acts like an SQL sequence: once it has been read, it is also increased.
      * @return the manipulationCounter
      */
     protected synchronized final int getManipulationCounter() {
         return ++manipulationCounter;
     }
 
 
 
 
     /**
      * Sets the value of the injectionType field as specified
      * by the value of injectionType. This method sets also the manipulation active flag
      * depending on the type of manipulation. If the injection type is none, then
      * the manipulating flag will be activated, otherwise it will set to false.
      * @param injectionType the injectionType to set
      */
     public synchronized final void setInjectionType(RoleInjectionType injectionType) {
         this.injectionType = injectionType;
         
         // ready for do a manipulation?
         this.manipulationActive = (this.injectionType != RoleInjectionType.NONE) ;
     }
     
     
 
 
     /**
      * Provides the value of the injectionType field.
      * @return the injectionType
      */
     protected synchronized final RoleInjectionType getInjectionType() {
         return injectionType;
     }
 
 
 
 
     /**
      * Provides the value of the myPool field.
      * @return the myPool
      */
     protected synchronized final ClassPool getPool() {
         return myPool;
     }
 
 
     /**
      * Creates this booster associating a parent booster to use a chained-Java class loader.
      * @param proxyClassName the proxy to use
      * @param parent the parent role booster
      * @throws WCException
      */
     public RoleBooster(String proxyClassName, RoleBooster parent) throws WCException{
 	super();
 	try{
 	    this.parentLoader = parent;
 	}catch(Exception e){
 	    throw new WCException(e);
 	}
     }
     
     
     /**
      * Constructs this loader as a child of a parent loader.
      * @param parentLoader the parent class loader
      */
     public RoleBooster(ClassLoader parentLoader){
 	super(parentLoader);
 	
 	// save the configuration default proxy
 	Configuration conf = Configuration.getInstance();
 	this.agentProxyClassName = conf.getProperty( Configuration.DEFAULT_AGENT_PROXY );
     }
     
     /**
      * This method searches for a role type within a proxy of an agent. The method searches
      * between the annotations of the proxy to see if the specified annotation type is present
      * between those of the proxy.
      * @param roled the proxy to analyze
      * @param annotationClass the type of annotation (and thus of role visibility) to search for
      * @return true if the proxy has the specified role
      */
     private final boolean hasRoleAnnotation(AgentProxy roled, Class annotationClass){
 	// check arguments
 	if( roled == null )
 	    return false;
 
 	// search among the (possibly present) annotations of the agent proxy
 	// to see if the agent has a role or not
 	Class proxyClass = roled.getClass();
 	
 	// check if the class has the specified annotation
 	if( proxyClass.isAnnotationPresent(annotationClass) )
 	    return true;
 	else
 	    return false;
     }
     
 
     /**
      * This method checks for the validity of a role annotation. The method checks if
      * the proxy is annotated with the PublicRole annotation, and then checks if
      * the annotation has a not-null value for the role itnerface or the role annotation.
      * @param roled the proxy to check
      * @param annotation true if the PublicRole.annotationClass() must be not-empty, false
      * if the PublicRole.roleInterface() must be not-empty.
      * @return true if the proxy has the role with the specified settings
      */
     private final boolean hasPublicRole(AgentProxy roled, boolean annotation){
 	if( this.hasRoleAnnotation(roled, PublicRole.class) ){
 	    PublicRole pRole = (PublicRole) roled.getClass().getAnnotation(PublicRole.class);
 	    if( annotation )
 		return ( ! pRole.roleAnnotation().equals("") );
 	    else
 		return ( ! pRole.roleInterface().equals("") );
 		
 	}
 	else
 	    return false;
     }
     
     /**
      * Determines if the proxy is currently playing a visible (i.e., public) role. A publi role
      * must be marked thru the @PublicRole annotation.
      * @param proxy the proxy to analyze.
      * @return true if the proxy is playing a public role.
      */
     public final boolean hasPublicRoleInterface(AgentProxy proxy){
 	return this.hasPublicRole(proxy, false);
     }
     
     
     /**
      * Checks if the proxy has a public role thru an annotation.
      * @param proxy the proxy to check
      * @return true if the proxy has the annotation
      */
     public final boolean hasPublicRoleAnnotation(AgentProxy proxy){
 	return this.hasPublicRole(proxy, true);
     }
     
     /**
      * Checks if a specific annotation is a role annotation, that is an annotation
      * annotated with the @Role annotation.
      * @param annotation the annotation to check
      * @return true if the annotation is a role annotation, false if it is a normal annotation or the
      * specified annotation is null
      */
     public final boolean isRoleAnnotation(Annotation annotation){
 	if( annotation == null )
 	    return false;
 	
 	Class annotationType = annotation.annotationType();
 	for(Annotation subAnnotation : annotationType.getAnnotations() )
 	    if( subAnnotation.annotationType().equals(Role.class) )
 		return true;
 	
 	return false;
     }
     
     /**
      * Provides the name of the interface marked as public interface for a public role.
      * It searches the @PublicRole annotation and returns the name indicated into it.
      * @param proxy the proxy to search for
      * @return the fully qualified name of the role interface
      */
     public final String getPublicRoleInterfaceName(AgentProxy proxy){
 	// check arguments
 	if( proxy == null || (! ( this.hasPublicRole(proxy, false) )) )
 	    return "";
 	
 	// get the annotation role, it must a public role annotation (@PublicRole)
 	
 	PublicRole annotation = (PublicRole) proxy.getClass().getAnnotation(PublicRole.class);
 	return annotation.roleInterface();
     }
     
     /**
      * Provides the name of the annotation of a public role.
      * @param proxy the proxy to check
      * @return the fully qualified name of the annotation for the role
      */
     public final String getPublicRoleAnnotationName(AgentProxy proxy){
 	// check arguments
 	if( proxy == null || (! (this.hasPublicRole(proxy, true))) )
 	    return "";
 	
 	// get the annotation from the proxy and return the annotation class name
 	PublicRole annotation = (PublicRole) proxy.getClass().getAnnotation(PublicRole.class);
 	return annotation.roleAnnotation();
     }
     
     /**
      * Provides the name of the role class  for a public role.
      * It searches the @PublicRole annotation and returns the name indicated into it.
      * @param proxy the proxy to search for
      * @return the fully qualified name of the role class
      */
     public final String getPublicRoleClassName(AgentProxy proxy){
 	// check arguments
 	if( proxy == null || (! ( this.hasPublicRole(proxy, false) )) )
 	    return "";
 	
 	// get the annotation role, it must a public role annotation (@PublicRole)
 	PublicRole annotation = (PublicRole) proxy.getClass().getAnnotation(PublicRole.class);
 	return annotation.roleClass();
     }
     
     
     /**
      * Provides the public role interface name, that is the interface that must be applied
      * to the agent proxy.
      * @param role the role implementation, that must be annotated with the @PublicRole annotation
      * @return the name of the interface or null, if the role does not provide a public interface
      */
     public final String getPublicRoleInterfaceName(IRole role){
 	// check arguments
 	if( role == null || (! ( role.getClass().isAnnotationPresent(PublicRole.class) )))
 	    return null;
 	
 	// get all the annotation of this class and search for a public role
 	// annotation
 	PublicRole pRole = (PublicRole) role.getClass().getAnnotation(PublicRole.class);
 	return pRole.roleInterface();
     }
     
     
     /**
      * Provides the role annotation class name, that is contained as a string (fully qualified name)
      * into the PublicRole annotation of a role.
      * @param role the role to analyze
      * @return the annotation fully qualified name
      */
     public final String getPublicRoleAnnotationName(IRole role){
 	//check arguments
 	if( role == null || (! ( role.getClass().isAnnotationPresent(PublicRole.class))) )
 	    return null;
 	
 	// get the annotation
 	PublicRole pRole = (PublicRole) role.getClass().getAnnotation(PublicRole.class);
 	return pRole.roleAnnotation();
     }
     
     
     public final synchronized AgentProxy injectPublicRole(WCAgent agent, AgentProxy proxy, IRole role) throws WCException{
 	// check arguments
 	
 	// 1) don't proceed if the agent or the role have not been specified
 	if( role == null )
 	    return proxy;
 	
 	try{
 	    // get the public part of the role to apply to the agent
 	    String publicRoleInterfaceName = this.getPublicRoleInterfaceName(role);
 	    // if I've got a role interface, then I have to add it to the proxy
 	    if( publicRoleInterfaceName != null ){
 		// here I have to weave the proxy with the public interface
 		for( Class publicRoleInterface : role.getClass().getInterfaces() )
 		    if( publicRoleInterface.getName().equals(publicRoleInterfaceName) ){
 			return this.addRoleToProxy(publicRoleInterface, proxy, role, true);
 		    }
 	    }
 	} catch (WCException e) {
 	    logger.error("Exception caught while adding the role interface", e);
 	}
 	
 	return proxy;
 	
     }
     
 
     /**
      * Removes all the applied roles until the specified one (included). Since the roles are applied to
      * a proxy by means of subclasses implementing the role interfaces, this method simply removes a role
      * returnin a proxy that is the instance of the superclass of the class when the specified role has been applied.
      * This means that the agent will loose all the role acquired since the specified one.
      * @param agent the agent that wants to remove the role
      * @param proxy the proxy to change
      * @param role the role to remove
      * @return the new instance of the agent proxy without all the roles until the specified one
      * @throws WCException if something goes wrong
      */
     public synchronized AgentProxy removeUntilRole(WCAgent agent, AgentProxy proxy, IRole role) throws WCException{
 	// check params
 	if( agent == null || role == null )
 	    return null;
 
 	    String publicRoleInterfaceName = this.getPublicRoleInterfaceName(role);
 	    
 	    // I need to find the first superclass that is not implementing the role
 	    // interface, such superclass if the class I'm looking for
 	    Class currentClass = proxy.getClass();
 	    boolean found = false;
 	    
 	    do{
 		// iterate on each interface of the current class
 		Class interfaces[] = currentClass.getInterfaces();
 		for( int i = 0; (interfaces != null) && (i < interfaces.length) && (! found); i++ )
 		    if( interfaces[i].getName().equals( publicRoleInterfaceName ) )
 			found = true;
 		
 		// if not found, go with the superclass
		// ATTENTION: it could happen that the proxy has assumed only one role, so only one interface!
		// In this case the class should be the superclass, because this means that the subclass/current class
		// is the roled one!
		if( (! found) || ( interfaces.length == 1 ) )
 		    currentClass = currentClass.getSuperclass();
 		
 	    }while( found == false );
 	    
 	    
 	    
 	    try{
 		AgentProxy newProxy = (AgentProxy) currentClass.newInstance();
 		ProxyHandler proxyHandler = ProxyHandlerFactory.getProxyHandler();
 		proxyHandler.setSourceProxy( proxy );
 		proxyHandler.setDestinationProxy( newProxy );
 		proxyHandler.updateProxy();
		return newProxy;    
 		    
 	    }catch(Exception e){
 		logger.error("Exception caught while removing a role from a proxy", e);
 		throw new WCException(e);
 	    }
 	    
	    
     }
     
 
     
     public synchronized AgentProxy removePublicRole(WCAgent agent, AgentProxy proxy, IRole role) throws WCException{
 	// check params
 	if( agent == null || role == null )
 	    return null;
 
 	
 	String publicRoleInterfaceName = this.getPublicRoleInterfaceName(role);
 	
 	// store the current class
 	this.currentProxyClass = proxy.getClass();
 	
 	// this is a remotion
 	this.setInjectionType( RoleInjectionType.ROLE_PUBLIC_INTERFACE_REMOVAL_FROM_PROXY );
 	
 
 	try{
 	    // get a method forwarder
 	    MethodForwarderGenerator mGenerator = MethodForwarderGeneratorFactory.getMethodForwarderGenerator();
 	    // do not initialize it, it will be done in the find class method
 	    this.setMethodForwarderGenerator(mGenerator);
     
 	    Class currentClass = this.findClass( this.agentProxyClassName );
 	    AgentProxy newProxy = (AgentProxy) currentClass.newInstance();
 	    newProxy.initializeByCopy(proxy);
 	    proxy = newProxy;
 
 
 	}catch(Exception e){
 	    logger.error("Exception caught while removing a role from a proxy", e);
 	    System.out.println("Eccezione " + e + " - " + e.getMessage() + " - " + e.getCause());
 	    e.printStackTrace();
 	    throw new WCException(e);
 	}
 	finally{
 	    // now I'm no more manipulating the class
 	    this.setInjectionType( RoleInjectionType.NONE );
 	}
 
 	return proxy;
     }
 
 
     
     /**
      * This is the horse-power method used to add a public role to an agent (proxy).
      * The method performs the following main steps:
      * 1) obtains a proxy handler from the proxy handler factory in order to handle the proxy's state
      * 2) obtains a method forwarder generator in order to construct the method forwarders for the
      * role public methods. In its simpler form, the method forwarder will construct  a method body
      * that simply forwards the method call from the proxy (role) interface to the role one.
      * 3) loads the new class with the new role interface added
      * 4) copies the status of the previous proxy and complete the method forwarders.
      * @param publicRoleInterface the interface that will be added to the proxy
      * @param proxy the proxy on which the method is going to work on
      * @param role the role to add to the proxy
      * @param addition true if the role is going to be added 
      * @return the new proxy instance
      * @throws WCException if something goes wrong
      */
     private final AgentProxy addRoleToProxy(Class publicRoleInterface, AgentProxy proxy, IRole role, boolean addition) throws WCException{
 	
 	try{
 	    this.agentProxyClassName = proxy.getClass().getName();
 	    this.publicRoleInterfaceName = publicRoleInterface.getName();
 	    this.roleClassName = role.getClass().getName();
 	    this.manipulationActive = true;
 	    this.roleImplementationAccessKey = this.getRoleImplementationAccessKey( this.publicRoleInterfaceName );
 	    
 	    // set that this is a role addition
 	    this.setInjectionType( RoleInjectionType.ROLE_PUBLIC_INTERFACE_ADDITION_TO_PROXY );
 
 	    ProxyHandler proxyHandler = ProxyHandlerFactory.getProxyHandler();
 	    proxyHandler.setSourceProxy( proxy );
 	    
 	    // get a new method forwarder generator
 	    MethodForwarderGenerator mGenerator = MethodForwarderGeneratorFactory.getMethodForwarderGenerator();
 	    // initialize the method forwarder
 	    mGenerator.init(agentProxyClassName, roleClassName, roleImplementationAccessKey);
 	    // store it for further use within the findClass method
 	    this.setMethodForwarderGenerator(mGenerator);
 	    
 	    
 	    
 	    
 	    Class newProxyClass = this.findClass( this.agentProxyClassName );
 	    AgentProxy newProxy = (AgentProxy) newProxyClass.newInstance();
 	    // store the role implementation as key for the proxy
 	    mGenerator.bindReferences(newProxy, role);
 
 	    // update the proxy
 	    proxyHandler.setDestinationProxy( newProxy );
 	    proxyHandler.updateProxy();
 	    
     
 	    // all done
 	    return newProxy;
 	}catch(Exception e){
 	    logger.error("Exception caught while weaving a role ", e);
 	    logger.error("Public role interface was " + publicRoleInterface);
 	    logger.error("Agent proxy was " + proxy);
 	    logger.error("Role was " + role);
 	    // no more role addition required
 	    this.setInjectionType( RoleInjectionType.NONE );
 
 	    throw new WCException(e);
 	}
 	finally{
 	    // no more role addition required
 	    this.setInjectionType( RoleInjectionType.NONE );
 	}
 
 	
     }
     
     
     public final synchronized AgentProxy injectVisibleRole(WCAgent agent, AgentProxy proxy, IRole role){
 	// check params
 	if( proxy == null || role == null )
 	    return proxy;
 	
 	// get the name of the annotation for the role
 	this.annotationClassName = this.getPublicRoleAnnotationName(role);
 	this.agentProxyClassName = proxy.getClass().getName();
 	
 	
 	// this is an nnotation role ibjection
 	this.setInjectionType( RoleInjectionType.ROLE_ANNOTATION_ADDITION_TO_PROXY );
 	
 	
 
 	try {
 	    // create a new proxy handler
 	    ProxyHandler proxyHandler = ProxyHandlerFactory.getProxyHandler();
 	    proxyHandler.setSourceProxy( proxy );
 
 	    Class newProxyClass = this.findClass( proxy.getClass().getName() );
 	    AgentProxy newProxy = (AgentProxy) newProxyClass.newInstance();
 	    proxyHandler.setDestinationProxy( newProxy );
 
 	    // update the proxies
 	    proxyHandler.updateProxy();
 
 	    // all done
 	    return newProxy;
 
 	} catch (Exception e) {
 	    // TODO Auto-generated catch block
 	    logger.error("Exception caught during role addition thru annotation", e);
 	    return proxy;
 	}
 	finally{
 	    this.setInjectionType( RoleInjectionType.NONE );
 	}
     }
     
     /**
      * The current agent proxy name, that is the class of the agent proxy that
      * the loader is manipulating or will be manipulating later.
      */
     private String agentProxyClassName = "whitecat.core.agents.AgentProxy";
     
     
     /**
      * The role interface to apply to the proxy class.
      */
     private String publicRoleInterfaceName = null;
     
     /**
      * The role class name, used to be placed into an hash map within the 
      * proxy.
      */
     private String roleClassName = null;
     
     /**
      * The key used to store the class within the hashmap of the proxy.
      */
     private String roleImplementationAccessKey = null;
     
     /**
      * Indicates if the class loading involves a bytecode manipulation or not.
      */
     private boolean manipulationActive = false;
     
     /**
      * Indicates if the role must be added or not to the agent proxy.
      */
     private boolean roleAddition = true;
 
     private Class currentProxyClass;
     
     
     
     /**
      * Provides the value of the manipulationActive field.
      * @return the manipulationActive
      */
     private synchronized final boolean isManipulationActive() {
         return manipulationActive;
     }
 
 
     protected Class findClass(String name) throws ClassNotFoundException {
         try {
 	    // get the class pool for working with classes and modifying them on the fly
 	    ClassPool pool = ClassPool.getDefault();
 	    CtClass baseProxyClass = null;
 	    
 	    // the array that will store the in-memory byte code
 	    byte[] bytecode = null;
 	    
 	    String subProxyClassName = null;
 	    
 	    if( this.isManipulationActive() ){
 		// it is required to weave the role to the proxy.
 		logger.debug("Loading a class manipulating it - role injection type=" + this.injectionType );
 		
 		
 		// construct the pool for loading and manipulating the classes
 		logger.info("Obtaining the proxy class from the Javassist pool...." + this.agentProxyClassName);
 		logger.info("My pool is " + pool.getClass() + " hash " + pool.hashCode());
 		baseProxyClass = pool.get(this.agentProxyClassName);
 		logger.info("Base proxy class obtained from the pool " + baseProxyClass.hashCode());
 		subProxyClassName = baseProxyClass.getName();
 		
 		
 		if( this.injectionType == RoleInjectionType.ROLE_PUBLIC_INTERFACE_ADDITION_TO_PROXY ){
 		    // the subclass will have a name composed by a dynamic identifier
 		    // with the fixed string _roled and the id of this role engine (getID).
 		    // For instance, if the proxy is whitecat.example.DBProxy, it will become
 		    // whitecat.example.DBProxy_roled3663
 		    subProxyClassName += this.getSubClassNameSuffix();
 		    baseProxyClass = this.addRoleToProxyThruSubClass( pool, 				// the pool to use for loadtime manipulation
 			                                              baseProxyClass, 			// the current proxy class
 			                                              subProxyClassName, 		// the name of the subclass of the proxy that will be created
 			                                              this.publicRoleInterfaceName, 	// the name of the public role interface
 			                                              this.roleImplementationAccessKey,	// the access key to use for the retriving the role instance
 			                                              this.roleClassName,                // the name of the concrete role implementation
 			                                              this.methodForwarderGenerator	// the method forwarder generator
 			                                             );
 		}
 		else
 		if( this.injectionType == RoleInjectionType.ROLE_PUBLIC_INTERFACE_REMOVAL_FROM_PROXY ){
 		    
 			// get the current class of the proxy
 			Class currentClass = this.currentProxyClass;
 			
 			Class interfaces[] = currentClass.getInterfaces();
 			for( int i = 0; (interfaces != null) && i < (interfaces.length); i++ ){
 			    Class currentInterface = interfaces[i];
 			    // is the current interface a role interface?
 			    if( ! currentInterface.getName().equals(publicRoleInterfaceName) ){
 				// this proxy level does not have the role that must be removed,
 				// so inject the role in the new proxy class
 				    subProxyClassName += this.getSubClassNameSuffix();
 				    String interfaceName = currentInterface.getName();
 				    String roleImplementationAccessKey = this.getRoleImplementationAccessKey( interfaceName );
 				    this.methodForwarderGenerator.init(baseProxyClass.getName(), interfaceName, roleImplementationAccessKey);
 				    baseProxyClass = this.addRoleToProxyThruSubClass( pool, 				// the pool to use for loadtime manipulation
 					                                              baseProxyClass, 			// the current proxy class
 					                                              subProxyClassName, 		// the name of the subclass of the proxy that will be created
 					                                              interfaceName, 			// the name of the public role interface
 					                                              roleImplementationAccessKey,	// the access key to use for the retriving the role instance
 					                                              interfaceName,		        // the name of the concrete role implementation	
 					                                              this.methodForwarderGenerator	// the method forwarder generator
 					                                             );
 			    }
 			}
 
 		    
 
 		}
 		else
 		if( this.injectionType == RoleInjectionType.ROLE_ANNOTATION_ADDITION_TO_PROXY ){
 		    // create a subclass of the current proxy and add the role annotation to it
 		    subProxyClassName += this.getSubClassNameSuffix();
 		    bytecode = this.addRoleToProxyThruAnnotation(pool, baseProxyClass, subProxyClassName);
 		    baseProxyClass = null;
 		}
 		else
 		if( this.injectionType == RoleInjectionType.ROLE_ANNOTATION_REMOVAL_FROM_PROXY ){
 		    // get the current class of the proxy
 		    Class currentClass = this.currentProxyClass;
 		    Class originalProxyClass = null;
 		    
 		    // now start from this point in the inheritance chain, and add each
 		    // role annotation that is different from the one I need to exclude
 		    List<Annotation> roleAnnotationsToKeep = new LinkedList<Annotation>();
 		    
 		    do{
 			logger.debug("Checking the class " + currentClass);
 			
 			// check if the current class has an annotation that is a role and must
 			// be kept (i.e., if it is a @Role annotated annotation and the name is not
 			// the one I must remove
 			for( Annotation anno : currentClass.getAnnotations() ){
 			    logger.debug("Analyzing annotation " + anno );
 			    if( this.isRoleAnnotation(anno) 
 			       && (! (anno.annotationType().getName().equals(this.annotationClassName))) ){
 				logger.debug("Keeping the role annotation " + anno);
 				roleAnnotationsToKeep.add(anno);
 			    }
 			}
 
 			// is this the original proxy class?
 			if( ! this.isManipulatedClass(currentClass) && originalProxyClass == null  )
 			    originalProxyClass = currentClass;
 			
 			// go to the superclass
 			currentClass = currentClass.getSuperclass();
 			
 		    }while( currentClass != null && currentClass != Object.class);
 		    
 		    
 		    // now I know each role annotation I must keep, and I can start again
 		    // adding all the roles to the proxy class
 		    if( ! roleAnnotationsToKeep.isEmpty() ){
 			Iterator<Annotation> iter = roleAnnotationsToKeep.iterator();
 			while( iter.hasNext() ){
 			    Annotation toAdd = iter.next();
 			    this.annotationClassName = toAdd.annotationType().getName();
 			    subProxyClassName += this.getSubClassNameSuffix();
 			    bytecode = this.addRoleToProxyThruAnnotation(pool, baseProxyClass, subProxyClassName);
 			    baseProxyClass = null;
 			}
 		    }
 		    else{
 			// no annotations to keep, so return the original proxy class
 			return originalProxyClass;
 		    }
 		    
 		}
 		else{
 		    // TODO this should never be used
 		    subProxyClassName = baseProxyClass.getSuperclass().getName();
 		    baseProxyClass = this.removeRoleFromProxyThruSuperclass(pool, baseProxyClass, name);
 		}
 		
 		
 	    } else
 		// load the class without manipulation
 		baseProxyClass = pool.get(name);
 	    
 	    if( this.parentLoader == null ||  this.isManipulationActive()){
 		if( ! this.isAnnotationInjection() )
 		    bytecode = baseProxyClass.toBytecode();
 		
 		logger.info("Defining the class manipulated name=" + subProxyClassName);
 		return this.defineClass(subProxyClassName, bytecode, 0, bytecode.length);
 	    }
 	    else{
 		logger.debug("Asking the parent class loader to load the class " + name);
 		return super.loadClass(name);
 	    }
 
             //            return defineClass(name, b, 0, b.length);
         } catch (NotFoundException e) {
             logger.error("Exception caught while defining a class",e);
             throw new ClassNotFoundException();
         } catch (IOException e) {
             throw new ClassNotFoundException();
         } catch (CannotCompileException e) {
             logger.error("Cannot compile exception caught while removing a role", e);
             throw new ClassNotFoundException();
         }
         catch(WCForwarderMethodException e){
             logger.error("Method forwarding exception!", e);
             throw new ClassNotFoundException();
         }
     }
 
 
 
 
     /**
      * Creates the bytecode for the class of a proxy with the role annotation injected.
      * @param pool the class pool to use
      * @param baseProxyClass the base proxy class to use as the superclass of the new proxy class created
      * @param subProxyClassName the name for the subclass
      * @return the byte array with the bytecode of the class with the annotation
      * @throws CannotCompileException if some problem occurs with javassist
      * @throws IOException if a problem occurs when converting the stream of a classfile to a byte array
      */
     private byte[] addRoleToProxyThruAnnotation(ClassPool pool,
 	                                                       CtClass baseProxyClass, 
 	                                                       String subProxyClassName)
 	    throws CannotCompileException, IOException {
 	
 	// create a new class that will be the subclass of the current base proxy class
 	CtClass newProxyClass = pool.makeClass( subProxyClassName );
 	// set the superclass of the class to the base proxy
 	newProxyClass.setSuperclass( baseProxyClass );
 	logger.debug("Creating the new empty class: " + subProxyClassName + " (superclass " + baseProxyClass.getName() + ")");
 	
 	// place a default constructor in the new class (important, or reflection will not be able to
 	// create an instance of this class)
 	logger.debug("Creating a new empty constructor");
 	CtConstructor constructor = new CtConstructor(null, newProxyClass );
 	constructor.setBody(";");
 	newProxyClass.addConstructor(constructor);
 	
 	// add the manipulated interface
 	try{
 	    CtClass manipulatedClassInterface = pool.get("whitecat.core.role.IManipulatedClass");
 	    newProxyClass.addInterface( manipulatedClassInterface );
 	}catch(NotFoundException e){
 	    logger.error("Exception caught while loading the base manipulating tag interface", e);
 	    
 	}
 
 	// get the class file and add the annotation
 	logger.debug("Creating the class file for the created class and adding the annotation " + this.annotationClassName);
 	ClassFile classFile = newProxyClass.getClassFile();
 	ConstPool constantPool = classFile.getConstPool();
 	AnnotationsAttribute attr = new AnnotationsAttribute(constantPool, AnnotationsAttribute.visibleTag);
 	javassist.bytecode.annotation.Annotation a = new javassist.bytecode.annotation.Annotation(this.annotationClassName, constantPool);
 	attr.setAnnotation(a);
 	classFile.addAttribute(attr);
 	classFile.setVersionToJava5();
 	
 	
 	// transform the classfile into bytecode
 	logger.debug("Converting the class file output stream to a bytecode");
 	ByteArrayOutputStream bos = new ByteArrayOutputStream();
 	DataOutputStream os = new DataOutputStream( bos );
 	classFile.write( os );
 	os.close();
 	
 	// all done
 	return bos.toByteArray();
     }
 
 
     /**
      * This method returns true if the role engine is involved in a manipulation that
      * includes an annotation.
      * @return true if the role engine is adding or removing an annotation from a role
      */
     protected final boolean isAnnotationInjection() {
 	if( this.injectionType == RoleInjectionType.ROLE_ANNOTATION_ADDITION_TO_PROXY 
 		|| this.injectionType == RoleInjectionType.ROLE_ANNOTATION_REMOVAL_FROM_PROXY )
 	    return true;
 	else
 	    return false;
     }
 
 
 
 
     private synchronized CtClass removeRoleFromProxy(ClassPool pool, 
 	    				             String superclassName,
 	    				             String subClassName,
 	    				             String proxyName)
     	throws NotFoundException, CannotCompileException {
 	// check arguments
 	if( superclassName == null || subClassName == null || superclassName.equals(subClassName) )
 	    return null;
 	
 	// debug info
 	if( logger.isDebugEnabled() ){
 	    logger.debug("removeRoleFromProxy: the class pool is " + pool);
 	    logger.debug("Linking the two classes " + subClassName + "->" + superclassName + " to skip the class declaring the role");
 	}
 
 	// get the classes from the pool
 	CtClass superClass = pool.get( superclassName );
 	CtClass subClass   = pool.get( subClassName );
 	
 	// try to set the superclass from the subclass
 	if( subClass.isFrozen() )
 	    subClass.defrost();
 	if( superClass.isFrozen() )
 	    superClass.defrost();
 	
 	subClass.setSuperclass(superClass);
 	subClass.toClass();
 	
 
 	// now get the proxy class
 	CtClass proxyClass = pool.get( proxyName );
 	return proxyClass;
 	
     }
 
     
     
     /**
      * An utility method that searches for an item within an array.
      * @param searched the item to search in the array
      * @param array2 the array to search the item in (it is searched with equals)
      * @return true if one occurence of the searched item is found in the array
      */
     private boolean arrayContains(Object searched, Object[] array2){
 	// check params
 	if( searched == null || array2 == null )
 	    return false;
 	
 	for( Object currentItem : array2 )
 	    if( currentItem.equals(searched) )
 		return true;
 	
 	return false;
     }
 	
 	
     /**
      * @param pool
      * @param baseProxyClass
      * @throws NotFoundException
      * @throws CannotCompileException
      */
     private CtClass addRoleToProxy(ClassPool pool, CtClass baseProxyClass)
 	    throws NotFoundException, CannotCompileException {
 	// get the role interface as it is
 	logger.debug("Loading the CtClass object for the public role interface " + this.publicRoleInterfaceName);
 	CtClass roleInterface = pool.get(this.publicRoleInterfaceName);
 
 	// add the public role interface
 	logger.debug("Adding the interface to the proxy class...");
 	baseProxyClass.addInterface(roleInterface);
 
 	// I need to add to the proxy a variable to store the role reference
 	this.roleImplementationAccessKey = "_hiddenRole_" + this.publicRoleInterfaceName + this.hashCode();
 	logger.debug("The role implementation reference will be stored with the key " + this.roleImplementationAccessKey);
 
 	// iterate over each method of the interface
 	for( CtMethod interfaceMethod : roleInterface.getDeclaredMethods() ){
 	    logger.debug("Creating the method forwared for the method " + interfaceMethod);
 	    
 	    StringBuffer methodCode = new StringBuffer(200);
 	    methodCode.append(" public ");
 	    methodCode.append(interfaceMethod.getReturnType().getName());
 	    methodCode.append(" ");
 	    methodCode.append(interfaceMethod.getName());
 	    methodCode.append("( ");
 	    CtClass params[] = interfaceMethod.getParameterTypes();
 	    for(int parNo = 0; params != null && parNo < params.length; parNo++ ){
 		if( parNo > 0 )
 		    methodCode.append(",");
 
 		methodCode.append( params[parNo].getName() );
 		methodCode.append(" param" + parNo);
 	    }
 
 	    methodCode.append(" ) ");
 
 	    // body definition
 	    methodCode.append("{ ");
 	    if( ! "void".equals(interfaceMethod.getReturnType().getName()) )
 		methodCode.append(" return ");
 	    methodCode.append(" ((");
 	    methodCode.append( this.roleClassName );
 	    methodCode.append(" ) ");
 	    methodCode.append(" this.roleMap.get(\"");
 	    methodCode.append( this.roleImplementationAccessKey );
 	    methodCode.append("\")).");
 	    methodCode.append(interfaceMethod.getName());
 	    methodCode.append("($$); }");
 
 	    logger.debug("Method forwarder completed:\n\t " + methodCode.toString());
 
 	    // add the method to the class
 	    //CtMethod definedMethod = CtNewMethod.make(methodCode.toString(), roleInterface);
 	    CtMethod definedMethod = CtMethod.make(methodCode.toString(), baseProxyClass);
 	    baseProxyClass.addMethod(definedMethod);
 	    logger.debug("Method forwarder added to the proxy class");
 	}
 	
 	return baseProxyClass;
     }
     
     
     /**
      * This method is used to add a role to the proxy of an agent. This method creates a subclass
      * of the proxy and forces this class to implement the role itnerface. Each method of the
      * interface is implemented as a forwarder to the role instance.
      * @param pool the class pool used for the bytecode manipulation
      * @param baseProxyClass the proxy class that must be subclassed
      * @param subProxyName the fully qualified name of the proxy subclass, that is the name
      * which will be used in the Java system for such class
      * @param publicRoleInterfaceName the name of the public interface to inject into the proxy class
      * @param roleInstanceAccessKey the string that will be used as a key for the role access thru the proxy
      * @param roleClassName the name of the class of the concrete role implementation, used to make a cast to right
      * type of the object when the forwarder methods will be created
      * @return the Javassist CtClass object that represents the subclass of the proxy
      * @throws NotFoundException if a class is not found in the Java system
      * @throws CannotCompileException if there is a problem with the compilation of the code
      */
     private CtClass addRoleToProxyThruSubClass(ClassPool pool, 
 	                                       CtClass baseProxyClass, 
 	                                       String subProxyName, 
 	                                       String publicRoleInterfaceName, 
 	                                       String roleInstanceAccessKey, 
 	                                       String roleClassName,
 	                                       MethodForwarderGenerator methodForwarder)
     	throws NotFoundException, CannotCompileException, WCForwarderMethodException {
 
 	// debug info 
 	if( logger.isDebugEnabled() ){
 	    logger.debug("addRoleToProxyThruSubClass: class pool is " + pool);
 	    logger.debug("The base proxy class is: " + baseProxyClass);
 	    logger.debug("The public role interface is " + publicRoleInterfaceName);
 	    logger.debug("The generated proxy subclass will have a name: " + subProxyName);
 	    logger.debug("The role instance access key is " + roleInstanceAccessKey);
 	}
 	
 	// as first step I need to get the role interface class object, so that
 	// it is possible to analyze the role public interface as it is
 	CtClass roleInterface = pool.get( publicRoleInterfaceName );
 	logger.info("Loaded the public role interface " + roleInterface);
 
 
 	// create a new empty class that will be the subclass of the current proxy, and
 	// will have the superclass set to the current proxy class
 	CtClass subProxy = pool.makeClass( subProxyName );
 	// set the superclass of this class to the current proxy class
 	subProxy.setSuperclass(baseProxyClass);
 	// add the public interface to the new created class
 	subProxy.addInterface( roleInterface );
 	logger.info("Created a new empty class subclass of the current proxy");
 	
 	// So far, the new created proxy subclass is implementing the role public interfcae,
 	// but all its methods are abstract and need to be implemented. The implementation 
 	// must forward each call to the concrete role implementation, so that once 
 	// the method X is called on the proxy, this will forward the call (with all the
 	// arguments) to the method X of the role instance (owned by the agent).
 	// To construct the forwarding methods, I need to iterate over each method of the role
 	// interface, and build a new method (with source code) that will be applied to the new proxy
 	// subclass.
 	
 
 	// iterate over each method of the interface
 	for( CtMethod interfaceMethod : roleInterface.getDeclaredMethods() ){
 	    logger.debug("Analyzing the interface " + roleInterface.getName());
 	    logger.debug("Creating the method forwared for the method " + interfaceMethod);
 /*
 	    // construct the Java source code for the new method
 	    StringBuffer methodCode = new StringBuffer(200);
 	    methodCode.append(" public ");
 	    methodCode.append(interfaceMethod.getReturnType().getName());
 	    methodCode.append(" ");
 	    methodCode.append(interfaceMethod.getName());
 	    methodCode.append("( ");
 	    CtClass params[] = interfaceMethod.getParameterTypes();
 	    for(int parNo = 0; params != null && parNo < params.length; parNo++ ){
 		if( parNo > 0 )
 		    methodCode.append(",");	// more than one parameter, separate them with comma
 
 		methodCode.append( params[parNo].getName() );	// parameter type
 		methodCode.append(" param" + parNo);		// parameter identifier
 	    }
 
 	    methodCode.append(" ) ");
 
 	    // body definition: I need to cast the reference extracted from the proxy
 	    // to an object of the type of the role, and than I need to invoke the method
 	    // passing all the argument of the forwarder one. If the method has a return type
 	    // different from void, that I also need to insert a return statement.
 	    methodCode.append("{ ");
 
 	    if( ! "void".equals(interfaceMethod.getReturnType().getName()) )
 		methodCode.append(" return ");
 	    methodCode.append(" ((");
 	    methodCode.append( roleClassName );
 	    methodCode.append(" ) ");
 	    methodCode.append(" this.roleMap.get(\"");
 	    methodCode.append( roleInstanceAccessKey );
 	    methodCode.append("\")).");
 	    methodCode.append(interfaceMethod.getName());
 	    methodCode.append("($$); }");
 
 */
 	    // generate the method source code
 	    String methodCode = methodForwarder.getMethodForwarderCode(interfaceMethod);
 	    
 	    // debug info
 	    if( logger.isDebugEnabled() ){
 		logger.debug("Creating a forwarder method for the role method " + interfaceMethod);
 		logger.debug("The forwarder method code is \n\t" + methodCode);
 	    }
 
 	    // compile the dynamic method and add it to the subProxy class
 	    CtMethod definedMethod = CtMethod.make(methodCode, subProxy);
 	    subProxy.addMethod(definedMethod);
 	}
 
 	logger.info("Definition of the subproxy class completed");
 	return subProxy;
     }
     
     /**
      * Returns an identifier of this Role Engine. The default implementation is to
      * return the Java object hash code as identifier. This method is used to
      * construct an unique name of role implementation key.
      * @return the identifier of this role engine instance
      */
     protected final int getID(){
 	return this.hashCode();
     }
     
     
     /**
      * Constrcuts an unique role implementation access key, that is a string key that will be
      * used when the forwarding methods need to access the role reference.
      * @param publicRoleInterfaceName. This method will construct a key composing the public role
      * interface name and the id of the role engine (thru the method getID()).
      * @return the string to be used as key for identifying a role instance
      */
     private final String getRoleImplementationAccessKey(String publicRoleInterfaceName){
 	// check arguments
 	if( publicRoleInterfaceName == null )
 	    return "";
 	
 	// build a string with the hidden access key
 	StringBuffer buffer = new StringBuffer(30);
 	buffer.append( publicRoleInterfaceName );
 	//buffer.append( this.getID() );
 	return buffer.toString();
     }
     
     
     public CtClass removeRoleFromProxyThruSuperclass(ClassPool pool, CtClass nowProxyClass, String proxyName)
 	throws NotFoundException, CannotCompileException {
 	for( CtClass interf : nowProxyClass.getInterfaces())
 	    System.err.println("Interfaccia " + interf);
 	
 	System.err.println("Numero di interfacce implementate:" + nowProxyClass.getInterfaces().length);
 	return nowProxyClass.getSuperclass();
     }
     
     
     /**
      * Returns the name suffix to append to a class name in order to get
      * a new name for a class. The postfix is built with the fixed string "_roled_" and
      * the manipulation counter (i.e., how many classes this engione has manipulated) and
      * the id of the engine itself.
      * @return the string that must be used as suffix for the class name
      */
     protected final String getSubClassNameSuffix(){
 	return "_roled_" + this.getManipulationCounter() + "_" + this.getID();
     }
     
     /**
      * Checks if the specified class has been manipulated by the role engine. A class is
      * manipulated if it implements the IManipulatedClass interface.
      * @param classToCheck the class to check
      * @return true if the class implements the IManipulatedClass interface
      */
     public final boolean isManipulatedClass(Class classToCheck){
 	if( classToCheck == null )
 	    return false;
 	
 	for(Class interfaces : classToCheck.getInterfaces())
 	    if( interfaces.equals(IManipulatedClass.class) )
 		return true;
 	
 	return false;
     }
 
 
 
 
     public AgentProxy removeVisibleRole(AgentProxy proxy,
 	    IRole role) {
 	try{
 	    
 	    this.annotationClassName = this.getPublicRoleAnnotationName( role );
 	    this.agentProxyClassName = proxy.getClass().getName();
 	    this.currentProxyClass = proxy.getClass();
 	    this.setInjectionType( RoleInjectionType.ROLE_ANNOTATION_REMOVAL_FROM_PROXY );
 	    
 	    // create a proxy handler
 	    ProxyHandler proxyHandler = ProxyHandlerFactory.getProxyHandler();
 	    proxyHandler.setSourceProxy(proxy);
 	    
 	    Class newProxyClass = this.findClass( proxy.getClass().getName() );
 	    AgentProxy newproxy = (AgentProxy) newProxyClass.newInstance();
 	    
 	    // update the proxy
 	    proxyHandler.setDestinationProxy(newproxy);
 	    proxyHandler.updateProxy();
 	    
 	    // all done
 	    return newproxy;
 	    
 	}catch(Exception e){
 	    logger.error("Exception cuaght while removing a role of annotation", e);
 	    return proxy;
 	}
 	finally{
 	    this.setInjectionType( RoleInjectionType.NONE );
 	}
     }
 
 
 
 
     /**
      * Provides the value of the methodForwarderGenerator field.
      * @return the methodForwarderGenerator
      */
     protected synchronized final MethodForwarderGenerator getMethodForwarderGenerator() {
         return methodForwarderGenerator;
     }
 
 
 
 
     /**
      * Sets the value of the methodForwarderGenerator field as specified
      * by the value of methodForwarderGenerator.
      * @param methodForwarderGenerator the methodForwarderGenerator to set
      */
     protected synchronized final void setMethodForwarderGenerator(
     	MethodForwarderGenerator methodForwarderGenerator) {
 	this.methodForwarderGenerator = methodForwarderGenerator;
     }
     
     
     
 }
