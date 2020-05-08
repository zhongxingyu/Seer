 /**
  * Copyright 2004
  * 
  * This file is part of Peertrust.
  * 
  * Peertrust is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  * 
  * Peertrust is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Peertrust; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 package org.peertrust.config;
 
 import java.beans.BeanInfo;
 import java.beans.IntrospectionException;
 import java.beans.Introspector;
 import java.beans.PropertyDescriptor;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import net.jxta.edutella.util.RdfUtilities;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.peertrust.exception.ConfigurationException;
 import org.peertrust.exception.InitializationException;
 
 import com.hp.hpl.jena.mem.ModelMem;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.NodeIterator;
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFException;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.Selector;
 import com.hp.hpl.jena.rdf.model.Seq;
 import com.hp.hpl.jena.rdf.model.SimpleSelector;
 import com.hp.hpl.jena.rdf.model.Statement;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.vocabulary.RDF;
 
 /**
  * <p>
  * This class reads a configuration file and set up the system accordingly.
  * </p><p>
 * $Id: PTConfigurator.java,v 1.8 2005/08/18 14:37:08 dolmedilla Exp $
  * <br/>
  * Date: 05-Dec-2003
  * <br/>
 * Last changed: $Date: 2005/08/18 14:37:08 $
  * by $Author: dolmedilla $
  * </p>
  * @author olmedilla 
  */
 public class PTConfigurator {
 	
 	// name of the log configuration file
 	private static final String LOG_CONFIG_FILE = ".logconfig" ;
 	private static final String CONFIGURATOR_PARENT_NAME = "ConfiguratorParent" ;
 	
 	private static Logger log ; //= Logger.getLogger(PeertrustConfigurator.class);
 	
 	private final Object EMPTY = "\\?$Empty@~?" ;
 	private final String SCAPE_CHARACTER_REG_EXP = "\\\\" ;
	private final String SCAPE_CHARACTER_REPLACEMENT = "\\\\" ;
 
     // The Configuration File (stored as a Jena RDF Model)
     private Model _configuration ;
 
     //private Vector _vectorRegistry ;
     
     // Arguments from the command line
     private String[] _confFiles ;
     
     // The root node of the peers configuration RDF graph: PeerTrust engine
     private Resource _peer ;
     
     // The event dispatcher
 //    private Resource _eventDispatcher ;
     
     // A flag to increasy the noisyness
     private boolean _verbose = false ;
 
     // registry of the different components index by their URI
     private Hashtable  _registry ;
     
     // Class passed to the configurator
     Object _configuratorParent = null ;
 
 	public PTConfigurator()
 	{
 		super();
 		init() ;
 	}
 	
 	public PTConfigurator(Object configuratorParent)
 	{
 		super();
 		_configuratorParent = configuratorParent ;
 		init() ;
 		
 	}
 	
 	private void init ()
 	{
 		// Configure logging
 		PropertyConfigurator.configure(LOG_CONFIG_FILE) ;
 		log = Logger.getLogger(PTConfigurator.class);
         
         log.info("Log4j configured based on file \"" + LOG_CONFIG_FILE + "\"");
 
		log.debug("$Id: PTConfigurator.java,v 1.8 2005/08/18 14:37:08 dolmedilla Exp $");
 		
 		log.info("Current directory: " + System.getProperty("user.dir")) ;
 	}
 	/**
      * 
      * @{inheritDoc}
      * 
      * @param newArgs
      * @return
      * @throws ConfigException
      * @see net.jxta.edutella.component.EdutellaComponent#startApp(java.lang.String[])
      */
     public void startApp(String[] confFiles, String [] components) throws ConfigurationException
     {
         log.debug(".startApp()");
         
         _registry = new Hashtable() ;
 //        _vectorRegistry = new Vector() ;
         _confFiles = confFiles;
         try
 		{
         	_configuration = loadConfiguration(_confFiles);
             _peer = baseConfigure(_configuration, _configuration.createResource(components[0]));
 //            _eventDispatcher = getResource(_configuration, Vocabulary.EventDispatcher) ;
             
             // if the parent of the configurator was passed as argument, we initialize the registry with it
             if (_configuratorParent != null)
             	_registry.put(CONFIGURATOR_PARENT_NAME, _configuratorParent) ;
             	
             createComponent(_peer) ;
         }
         catch (ConfigurationException e)
 		{
         	throw new ConfigurationException (e.getMessage() + ". Please, check your configuration file") ;
 		}
         
         log.info ("Basic peer configuration succeded") ;
         
 //        createComponent(_eventDispatcher) ;
 //        log.info ("Event architecture configuration succeded") ;
     }
     
     private Model loadConfiguration(String [] args) throws ConfigurationException {
         log.debug(".loadConfiguration()");
                 
         Model model ;
         try {
             model = loadRdf(args[0]);
         } catch (InitializationException ie) {
             throw new ConfigurationException ("Could not load rdf peer configuration " + args[0], ie);
         }
 
         return model ;
     }
     
     /**
      * 
      */
     private Model loadRdf(String url) throws InitializationException
     {
         log.debug(".loadConfiguration() (" + url + ")");
         Model model ;
         try {
             model = new ModelMem();
             model.read(url);
             
             log.info("Read " + model.size() + " "
                     + "statements from configuration file");
             
             if (isVerbose()) {
                 StmtIterator i = model.listStatements();
                 while(i.hasNext()) {
                     log.debug(i.next().toString());
                 }
             }
         } catch (RDFException rdfe) {
             log.error("RDFException: ", rdfe);
             throw(new InitializationException(rdfe));
         }
         return model ;
     }
     
     private Resource baseConfigure(Model model, Resource res) throws ConfigurationException {
         log.debug(".baseConfigure()");
         
         log.info("Retrieving root node of configuration file...");
         Resource resource = null;
         
         try {
             resource = RdfUtilities.findSubject(model, RDF.type, res);
             
             log.info("Resource configured: " + resource.getURI());
             
         } catch (RDFException rdfe) {
             log.error("Can not find the Resouce describing this peer: ", rdfe);
             throw(new ConfigurationException(rdfe));
         }
         return resource ;
     }
     
     public Object createComponent (Resource identifier) throws ConfigurationException
 //    public Object createComponent (Resource identifier, boolean register) throws ConfigurationException
 	{
     	Object object = _registry.get(identifier.getLocalName()) ; 
         if (object == null)
         {
         	// we introduce it in the registry but without the object as it is not created yet
         	_registry.put(identifier.getLocalName(), EMPTY) ;
         	object = createObject(_configuration, identifier) ;
 
         	// now we add the object
         	_registry.put(identifier.getLocalName(), object) ;
 
         }
         else
         {
         	
         	if (object == EMPTY)
         	{
         		String message = "There is a loop in the definition of the object " + identifier.getURI() ;
         		log.error(message) ;
         		throw new ConfigurationException (message) ;
         	}
         	else
         	{
         		log.debug("The object " + identifier.getURI() + " is already defined in the configuration file. Reusing object") ;
         	}
         }
 
     	return object ;
 	}
     
     public Object createObject(Model model, Resource identifier) throws ConfigurationException {
         
         log.debug(".createObject() [Resource:" + identifier.getLocalName() + "]");
 
         if ( ( model == null ) || ( _registry == null) ){
             log.error("Configuration not available");
             throw new ConfigurationException ("Configuration not available") ;
         }
         
         Object object ;
         
         try {
         	//Resource resource = RdfUtilities.findSubject(model, RDF.type, identifier) ;
         	
             // RDF property pd:javaClass identifies java class for this object.
             Literal literal = RdfUtilities.findObjectLiteral(model,
                     identifier, Vocabulary.javaClass);
             
             String objectClass = literal.getString();
             
             object = Class.forName(objectClass).newInstance();
             
             configure(model, identifier, object);
             
             if (object instanceof Configurable)
             	( (Configurable) object).init() ;
             else
             	log.warn("Class " + object.getClass().getName() + " is not configurable") ;
                 
         } catch (RDFException rdfe) {
             log.error("RDFException ", rdfe);
             throw new ConfigurationException ("Error creating object " + identifier.getURI(), rdfe) ;
         } catch (ClassNotFoundException cnfe) {
             log.error("ClassNotFoundException: ", cnfe);
             throw new ConfigurationException ("Error creating object " + identifier.getURI(), cnfe) ;
         } catch (InstantiationException ie) {
             log.error("InstantiationeException: ", ie);
             throw new ConfigurationException ("Error creating object " + identifier.getURI(), ie) ;
         } catch (IllegalAccessException iae) {
             log.error("IllegalAccessException: ", iae);
             throw new ConfigurationException ("Error creating object " + identifier.getURI(), iae) ;
         }
         
         return object ;
     }
     
     /**
      * 
      */
     public void configure(Model model, Resource identifier, Object object) throws ConfigurationException
 	{
         log.debug(".configure() [Object:" + identifier.getURI() + "]");
         
         if ( ( model == null ) || ( _registry == null) ){
             log.error("Configuration not available");
             throw new ConfigurationException ("Configuration not available") ;
         }
         
         try {
             // Retrieve name of the Configurable
             // Resource resource = model.createResource(object.getIdentifier());
             
             // Find all Statements, that belong to the Object
             Selector selector = new SimpleSelector(identifier, (Property) null, (RDFNode) null);
             StmtIterator stmtI = model.listStatements(selector);
             
             while ( stmtI.hasNext() ) {
                 Statement stmt = stmtI.nextStatement();
                 
                 Property predicate = stmt.getPredicate();
                 RDFNode node = stmt.getObject();
                 
                 if ( predicate.equals(RDF.type) ) {
                     // Dont set RDF Type
                 } else {
                     
                     String _predicate = predicate.getLocalName().trim();
                     String _object = "";
                     
                     if ( node instanceof Literal ) {
                         _object = ((Literal) node).getString().trim();
                     } else if ( node instanceof Resource ) {
                         _object = ((Resource) node).getURI().trim();
                     }
                       
                     setFieldOnObject(model, identifier, object, _predicate, _object);
                 }
             }
         } catch (RDFException rdfe) {
             log.error("RDFException: ", rdfe);
         }
     }
 
     private void setFieldOnObject(Model model, Resource identifier, Object object, String attr, String value)
     	throws ConfigurationException {
     	
     	if (_verbose)
     		log.debug(".setFieldOnObject()");
         
         // Getting Properties from BeanInfo, that match <code>name</code>
         PropertyDescriptor propertyDescriptors[] = null;
         
         try {
             BeanInfo beaninfo = Introspector.getBeanInfo(object.getClass());
             propertyDescriptors = beaninfo.getPropertyDescriptors();
         } catch (IntrospectionException ie) {
             log.error("IntrospectionException! Could not get bean info: ", ie);
             throw new ConfigurationException ("Exception " + ie.getMessage());
         }
         
         Iterator i = Arrays.asList(propertyDescriptors).iterator();
         while ( i.hasNext() ) {
             PropertyDescriptor pd = (PropertyDescriptor) i.next();
             String attribute = pd.getName();
             Method setter = pd.getWriteMethod();
             
             if ( attr.equals(attribute) && (setter != null) ) {
                 
                 Class type = pd.getPropertyType();
                 
                 try {
                     Object parameter = null;
                     
                     if (type.equals(String.class)) {
                     	// regular expression for system properties (e.g., ${user.home}
                 		String regex = "\\$\\{[a-zA-Z0-9.]*}" ;
                 		Pattern pattern = Pattern.compile(regex);
                 		Matcher matcher ; 
                 			
                 		matcher = pattern.matcher(value) ;
 
             			StringBuffer sb = new StringBuffer();
             	        // Loop through and create a new String 
             	        // with the replacements
             	        while(matcher.find())
             	        {
             	        	String group = matcher.group() ;
             	        	String propertyString = System.getProperty(group.substring("${".length(), group.length()-"}".length())) ;
             	        	String newPropertyString = propertyString.replaceAll(SCAPE_CHARACTER_REG_EXP, SCAPE_CHARACTER_REPLACEMENT) ;
             	        	
             	            matcher.appendReplacement(sb, newPropertyString);
             	        }
 
             	        // Add the last segment of input to 
             	        // the new String
             	        matcher.appendTail(sb);
 
             	        parameter = sb.toString() ;
             			
             			log.debug("Property '"+ attr + "', value '" + (String) parameter + "'") ;
                     } else if (type.equals(int.class)) {
                         parameter = Integer.valueOf(value);
                     } else if (type.equals(long.class)) {
                         parameter = Long.valueOf(value);
                     } else if (type.equals(boolean.class)) {
                         parameter = Boolean.valueOf(value);
                     } else if (type.equals(Resource.class)) {
                         parameter = this._configuration.createResource(value);
                     } else if (type.equals(Vector.class)) {
                         parameter = fillVector(identifier, object, attribute, value);
                     } else {
                     	// recursive version
                     	parameter = createComponent (model.getResource(value)) ;
                     }
                     
                     
                     if (parameter != null) {
                         setter.invoke(object,
                                 new Object[] { parameter });
                     }
                     
                 } catch (NullPointerException npe) {
                     log.error("NullPointerException: ", npe);
                 } catch (IllegalAccessException iae) {
                     log.error("IllegalAccessException: " +
                             "Access rights prohibit setting attribute '"
                             + attribute + "'", iae);
                 } catch (InvocationTargetException ite) {
                     log.error("InvocationTargetException: " +
                             "Error invoking setter for '"
                             + attribute + "' on object " + object, ite);
                 } catch (NumberFormatException nfe) {
                     log.error("NumberFormatException: " +
                             "Attribute '" +
                             attribute + "' type mismatch. " +
                             "(Value: " + value + ")", nfe);
                 }
             }
         }
     }
 
     private Vector fillVector(Resource identifier, Object component, String attribute, String value) {
         log.debug(".fillVector()");
         
         Resource subject = identifier ;
         Property predicate = this._configuration.getProperty(Vocabulary.uri + attribute);
         Resource object = this._configuration.getResource(value);
         
         Vector vector = new Vector();
         
         if (_verbose)
         {
         	log.debug("Subject: " + subject.getURI().toString());
         	log.debug("Predicate: " + predicate.getURI().toString());
         	log.debug("Object: " + object.getURI().toString());
         }
         
         StmtIterator i = this._configuration.listStatements(subject, predicate, object);
         
         if (i.hasNext()) {
             Statement statement = i.nextStatement();
             log.debug("Statement: " + statement.toString());
             
             // Property predicate = this.configuration.getProperty(PeerDescription.uri + attribute);
             Seq seq = statement.getSeq();
             log.debug("Seq contains " + seq.size() + " elements.");
 
             NodeIterator j = seq.iterator();
 
             while (j.hasNext()) {
                 RDFNode node = j.nextNode();
                 if (node instanceof Literal) {
                     Literal literal = (Literal) node;
                     vector.add(literal.getString());
                 } else if (node instanceof Resource) {
                     Resource resource = (Resource) node;
                     vector.add(resource.getURI().toString());
                 }
             }
         }
         return(vector);
     }
 
     /* **
      * @return Returns the verbose.
      */
     public boolean isVerbose() {
         return _verbose;
     }
 
     public Object getComponent (Resource resource)
     {
     	if (_configuration == null)
     	{
     		log.error("There not exist a valid configuration") ;
     		return null ;
     	}
     	
     	Resource res = null;
         
         try {
             res = RdfUtilities.findSubject(_configuration, RDF.type, resource);
             
             log.info("Peer configured: " + res.getURI());
             
         } catch (RDFException rdfe) {
             log.error("Cannot find the resource describing " + resource.getURI() + ": ", rdfe) ;
             return null ;
         }
         
     	return getComponent(res.getLocalName()) ;
     }
     
     public Object getComponent (String resource)
     {
     	return _registry.get(resource) ;
     }
     
 //    public Object getEventDispatcher ()
 //    {
 //    	return _registry.get(_eventDispatcher.getLocalName()) ;
 //    }
     
     public static void main (String args[]) throws ConfigurationException
     {
     	String [] confFiles = { "peertrustConfig.rdf" } ;
     	String [] components = { Vocabulary.PeertrustEngine.toString() } ;
     	PTConfigurator ptconf = new PTConfigurator() ;
     	ptconf.startApp(confFiles, components) ;
     }
 }
