 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.common.cell.state;
 
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import org.jdesktop.wonderland.common.AssetURI;
 import org.jdesktop.wonderland.common.cell.state.annotation.ServerState;
 import org.jdesktop.wonderland.common.cell.state.spi.CellServerStateSPI;
 import org.jdesktop.wonderland.common.utils.ScannedClassLoader;
 
 /**
  * The CellServerStateFactory returns marshallers and unmarshallers that can encode
  * and decode XML that is bound to JAXB-annotated classes. This class uses
  * Java's service provider mechanism to fetch the list of fully-qualified class
  * names of Java objects that have JAXB annotations.
  * <p>
  * Classes that provide such a service must have an entry in the JAR file in
  * which they are contained. In META-INF/services, a file named
  * org.jdesktop.wonderland.common.cell.setup.CellServerStateSPI should contain the
  * fully-qualified class name(s) of all classes that implement the CellServerStateSPI
  * interface.
  * 
  * @author Jordan Slott <jslott@dev.java.net>
  */
 public class CellServerStateFactory {
     /* A list of core cell setup class names, currently only for components */
     private static Class[] coreSetupClasses = {
        PositionComponentServerState.class,
         AssetURI.class
     };
     
     /* The JAXB contexts used to create marshallers and unmarshallers */
     private static JAXBContext jaxbContext = null;
     
     /* The Logger for this class */
     private static Logger logger = Logger.getLogger(CellServerStateFactory.class.getName());
      
     /* Create the XML marshaller and unmarshaller once for all setup classes */
     static {
         ScannedClassLoader scl = ScannedClassLoader.getSystemScannedClassLoader();
 
         try {
             jaxbContext = JAXBContext.newInstance(getClasses(scl));
         } catch (javax.xml.bind.JAXBException excp) {
             CellServerStateFactory.logger.log(Level.SEVERE, "[CELL] SETUP FACTORY Failed to create JAXBContext", excp);
         }
     }
     
     /**
      * Returns the object that marshalls JAXB-annotated classes into XML using
      * classes available in the supplied classLoader. If classLoader is null the
      * classloader for this class will be used.
      * 
      * @return A marhsaller for JAXB-annotated classes
      */
     public static Marshaller getMarshaller(ScannedClassLoader classLoader) {
         try {
             if (classLoader == null) {
                 classLoader = ScannedClassLoader.getSystemScannedClassLoader();
             }
         
             Class[] clazz = getClasses(classLoader);
             JAXBContext jc = JAXBContext.newInstance(clazz);
             Marshaller m = jc.createMarshaller();
             m.setProperty("jaxb.formatted.output", true);
             return m;
 
         } catch (JAXBException ex) {
             logger.log(Level.SEVERE, null, ex);
         }
             
         return null;
         
     }
     
     /**
      * Returns the object that unmarshalls XML into JAXB-annotated classes using
      * classes available in the supplied classLoader. If classLoader is null the
      * classloader for this class will be used.
      * 
      * @return An unmarshaller for XML
      */
     public static Unmarshaller getUnmarshaller(ScannedClassLoader classLoader) {
         try {
             if (classLoader == null) {
                 classLoader = ScannedClassLoader.getSystemScannedClassLoader();
             }
 
             Class[] clazz = getClasses(classLoader);
             JAXBContext jc = JAXBContext.newInstance(clazz);
             return jc.createUnmarshaller();
 
         } catch (JAXBException ex) {
             logger.log(Level.SEVERE, null, ex);
         }
             
         return null;
     }
     
     /**
      * Find and return all the classes from the classLoader that implement the CellServerStateSPI
      * inteface
      * 
      * @param classLoader
      * @return
      */
     private static Class[] getClasses(ScannedClassLoader classLoader) {
         Set<Class> setupClasses = new LinkedHashSet<Class>(Arrays.asList(coreSetupClasses));
 
         /* Attempt to load the class names using annotations */
         Iterator<CellServerState> it = classLoader.getInstances(
                 ServerState.class,
                 CellServerState.class);
 
         while (it.hasNext()) {
             setupClasses.add(it.next().getClass());
         }
 
         /* Also add the deprecated CellServerStateSPI implementations, although
          * we hope these will go away eventually
          */
         Iterator<CellServerStateSPI> it2 = classLoader.getAll(
                 ServerState.class,
                 CellServerStateSPI.class);
         while (it2.hasNext()) {
             setupClasses.add(it2.next().getClass());
         }
 
         /* Attempt to load the component class names using annotations */
         Iterator<CellComponentServerState> it3 = classLoader.getInstances(
                 ServerState.class,
                 CellComponentServerState.class);
 
         while (it3.hasNext()) {
             setupClasses.add(it3.next().getClass());
         }
 
         return setupClasses.toArray(new Class[0]);
     }
 }
