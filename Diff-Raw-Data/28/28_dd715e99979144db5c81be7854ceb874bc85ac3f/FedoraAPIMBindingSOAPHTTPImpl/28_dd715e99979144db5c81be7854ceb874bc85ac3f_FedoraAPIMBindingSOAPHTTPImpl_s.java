 package fedora.server.management;
 
 import fedora.server.Context;
 import fedora.server.ReadOnlyContext;
 import fedora.server.Server;
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.ObjectIntegrityException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.ServerInitializationException;
 import fedora.server.errors.StorageDeviceException;
 import fedora.server.management.Management;
 import fedora.server.storage.TestFileStreamStorage;
 import fedora.server.storage.lowlevel.ILowlevelStorage;
 import fedora.server.storage.lowlevel.FileSystemLowlevelStorage;
 import fedora.server.utilities.AxisUtility;
 import fedora.server.utilities.TypeUtility;
 
 import java.io.File;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.rmi.RemoteException;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import javax.servlet.http.HttpServletRequest;
 import org.apache.axis.AxisEngine;
 import org.apache.axis.MessageContext;
 import org.apache.axis.transport.http.HTTPConstants;
 import org.apache.axis.types.NonNegativeInteger;
 
 /**
  *
  * <p><b>Title:</b> FedoraAPIMBindingSOAPHTTPImpl.java</p>
  * <p><b>Description:</b> </p>
  *
  * -----------------------------------------------------------------------------
  *
  * <p><b>License and Copyright: </b>The contents of this file are subject to the
  * Mozilla Public License Version 1.1 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a copy of the License
  * at <a href="http://www.mozilla.org/MPL">http://www.mozilla.org/MPL/.</a></p>
  *
  * <p>Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
  * the specific language governing rights and limitations under the License.</p>
  *
  * <p>The entire file consists of original code.  Copyright &copy; 2002, 2003 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author cwilper@cs.cornell.edu
  * @version $Id$
  */
 public class FedoraAPIMBindingSOAPHTTPImpl
         implements FedoraAPIM {
 
     /** The Fedora Server instance */
     private static Server s_server;
 
     /** Whether the service has initialized... true if we got a good Server instance. */
     private static boolean s_initialized;
 
     /** The exception indicating that initialization failed. */
     private static InitializationException s_initException;
 
     private static Management s_management;
 
     private static ILowlevelStorage s_st;
 
     /** Before fulfilling any requests, make sure we have a server instance. */
     static {
         try {
             String fedoraHome=System.getProperty("fedora.home");
             if (fedoraHome==null) {
                 s_initialized=false;
                 s_initException=new ServerInitializationException(
                     "Server failed to initialize: The 'fedora.home' "
                     + "system property was not set.");
             } else {
                 s_server=Server.getInstance(new File(fedoraHome));
                 s_initialized=true;
                 s_management=(Management) s_server.getModule("fedora.server.management.Management");
             }
             s_st=FileSystemLowlevelStorage.getObjectStore();  // FIXME: Move this
         } catch (InitializationException ie) {
             System.err.println(ie.getMessage());
             s_initialized=false;
             s_initException=ie;
         }
     }
 
     private Context getContext() {
         HashMap h=new HashMap();
         h.put("application", "apim");
         h.put("useCachedObject", "false");
         h.put("userId", "fedoraAdmin");
         HttpServletRequest req=(HttpServletRequest) MessageContext.
                 getCurrentContext().getProperty(
                 HTTPConstants.MC_HTTP_SERVLETREQUEST);
         h.put("host", req.getRemoteAddr());
         return new ReadOnlyContext(h);
     }
 
 /*
     public String createObject()
             throws RemoteException {
         assertInitialized();
         try {
             return s_management.createObject(getContext());
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         }
     }
 */
 
     private void logStackTrace(Exception e) {
         StackTraceElement[] els=e.getStackTrace();
         StringBuffer lines=new StringBuffer();
         boolean skip=false;
         for (int i=0; i<els.length; i++) {
             if (els[i].toString().indexOf("FedoraAPIMBindingSOAPHTTPSkeleton")!=-1) {
                 skip=true;
             }
             if (!skip) {
                 lines.append(els[i].toString());
                 lines.append("\n");
             }
         }
         s_server.logFiner("Error carried up to API-M level: " + e.getClass().getName() + "\n" + lines.toString());
     }
 
     public String ingestObject(byte[] METSXML, String logMessage) throws java.rmi.RemoteException {
         assertInitialized();
         try {
           // always gens pid, unless pid in stream starts with "test: or demo:"
             return s_management.ingestObject(getContext(),
                     new ByteArrayInputStream(METSXML), logMessage, "metslikefedora1", "UTF-8", true);
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         } catch (Exception e) {
             logStackTrace(e);
             throw AxisUtility.getFault(e);
         }
     }
 
     public void modifyObject(String PID, String state, String label,
             String logMessage)
             throws RemoteException {
         assertInitialized();
         try {
             s_management.modifyObject(getContext(), PID, state, label,
                     logMessage);
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         } catch (Exception e) {
             logStackTrace(e);
             throw AxisUtility.getFault(e);
         }
     }
 
 
     public byte[] getObjectXML(String PID)
             throws RemoteException {
         assertInitialized();
         try {
             InputStream in=s_management.getObjectXML(getContext(), PID, "metslikefedora1", "UTF-8");
             ByteArrayOutputStream out=new ByteArrayOutputStream();
             pipeStream(in, out);
             return out.toByteArray();
         } catch (ServerException se) {
             throw AxisUtility.getFault(se);
         } catch (Exception e) {
             throw AxisUtility.getFault(new ServerInitializationException(e.getClass().getName() + ": " + e.getMessage()));
         }
     }
 
     public byte[] exportObject(String PID)
             throws RemoteException {
         assertInitialized();
         try {
             InputStream in=s_management.exportObject(getContext(), PID, "metslikefedora1export", "UTF-8");
             ByteArrayOutputStream out=new ByteArrayOutputStream();
             pipeStream(in, out);
             return out.toByteArray();
         } catch (ServerException se) {
             throw AxisUtility.getFault(se);
         } catch (Exception e) {
             throw AxisUtility.getFault(new ServerInitializationException(e.getClass().getName() + ": " + e.getMessage()));
         }
     }
 
     // temporarily here
     private void pipeStream(InputStream in, OutputStream out)
             throws StorageDeviceException {
         try {
             byte[] buf = new byte[4096];
             int len;
             while ( ( len = in.read( buf ) ) != -1 ) {
                 out.write( buf, 0, len );
             }
         } catch (IOException ioe) {
             throw new StorageDeviceException("Error writing to stream");
         } finally {
             try {
                 out.close();
                 in.close();
             } catch (IOException closeProb) {
               // ignore problems while closing
             }
         }
     }
 
     public void purgeObject(String PID, String logMessage) throws java.rmi.RemoteException {
         assertInitialized();
         try {
             s_management.purgeObject(getContext(), PID, logMessage);
         } catch (ServerException se) {
             logStackTrace(se);
             AxisUtility.throwFault(se);
         }
     }
 
 /*
     public fedora.server.types.gen.AuditRecord[] getObjectAuditTrail(String PID) throws java.rmi.RemoteException {
         assertInitialized();
         return null;
     }
 
     public String addDatastreamExternal(String PID, String dsLabel, String dsLocation) throws java.rmi.RemoteException {
         assertInitialized();
         return null;
     }
 
     public String addDatastreamManagedContent(String PID, String dsLabel, String MIMEType, byte[] dsContent) throws java.rmi.RemoteException {
         assertInitialized();
         return null;
     }
 
     public String addDatastreamXMLMetadata(String PID, String dsLabel, String MDType, byte[] dsInlineMetadata) throws java.rmi.RemoteException {
         assertInitialized();
         return null;
     }
 */
 
     public String addDatastream(String PID,
                                 String label,
                                 String MIMEType,
                                 String location,
                                 String controlGroup,
                                 String MDClass,
                                 String MDType,
                                 String dsState) throws RemoteException {
         assertInitialized();
         try {
             return s_management.addDatastream(getContext(), PID, label, MIMEType,
                     location, controlGroup, MDClass, MDType, dsState);
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         }
     }
 
     public void modifyDatastreamByReference(String PID, String datastreamID,
             String dsLabel, String logMessage, String dsLocation, String dsState)
             throws java.rmi.RemoteException {
         assertInitialized();
         try {
             s_management.modifyDatastreamByReference(getContext(), PID,
                     datastreamID, dsLabel, logMessage, dsLocation, dsState);
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         }
     }
 
     public void modifyDatastreamByValue(String PID, String datastreamID, String dsLabel, String logMessage, byte[] dsContent, String dsState) throws java.rmi.RemoteException {
         assertInitialized();
         try {
             ByteArrayInputStream byteStream=null;
             if (dsContent!=null && dsContent.length>0) {
                 byteStream=new ByteArrayInputStream(dsContent);
             }
             s_management.modifyDatastreamByValue(getContext(), PID,
                     datastreamID, dsLabel, logMessage, byteStream, dsState);
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         }
     }
 
     public void setDatastreamState(String PID, String datastreamID, String dsState, String logMessage) throws java.rmi.RemoteException {
         assertInitialized();
         try {
             s_management.setDatastreamState(getContext(), PID,
                     datastreamID, dsState, logMessage);
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         }
     }
 
     public void setDisseminatorState(String PID, String disseminatorID, String dissState, String logMessage) throws java.rmi.RemoteException {
         assertInitialized();
         try {
             s_management.setDisseminatorState(getContext(), PID,
                     disseminatorID, dissState, logMessage);
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         }
     }
 
     public java.util.Calendar[] purgeDatastream(String PID, String datastreamID, java.util.Calendar endDT) throws java.rmi.RemoteException {
         assertInitialized();
         try {
             return s_management.purgeDatastream(getContext(), PID, datastreamID, endDT);
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         } catch (Exception e) {
             throw AxisUtility.getFault(new ServerInitializationException(e.getClass().getName() + ": " + e.getMessage()));
         }
     }
 
     public fedora.server.types.gen.Datastream getDatastream(String PID, String datastreamID, java.util.Calendar asOfDateTime) throws java.rmi.RemoteException {
         assertInitialized();
         try {
             fedora.server.storage.types.Datastream ds=s_management.getDatastream(getContext(), PID, datastreamID, asOfDateTime);
             return TypeUtility.convertDatastreamToGenDatastream(ds);
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         } catch (Exception e) {
             throw AxisUtility.getFault(new ServerInitializationException(e.getClass().getName() + ": " + e.getMessage()));
         }
     }
 
     public fedora.server.types.gen.Datastream[] getDatastreams(String PID, java.util.Calendar asOfDateTime, String state) throws java.rmi.RemoteException {
         assertInitialized();
         try {
             fedora.server.storage.types.Datastream[] intDatastreams=s_management.getDatastreams(getContext(), PID, asOfDateTime, state);
             return getGenDatastreams(intDatastreams);
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         } catch (Exception e) {
             throw AxisUtility.getFault(new ServerInitializationException(e.getClass().getName() + ": " + e.getMessage()));
         }
     }
 
     private fedora.server.types.gen.Datastream[] getGenDatastreams(
             fedora.server.storage.types.Datastream[] intDatastreams) {
         fedora.server.types.gen.Datastream[] genDatastreams=
                 new fedora.server.types.gen.Datastream[intDatastreams.length];
     	for (int i=0; i<intDatastreams.length; i++) {
     	    genDatastreams[i]=TypeUtility.convertDatastreamToGenDatastream(
    		        intDatastreams[i]);
 		}
         return genDatastreams;
     }
 
     private fedora.server.types.gen.Disseminator[] getGenDisseminators(
             fedora.server.storage.types.Disseminator[] intDisseminators) {
         fedora.server.types.gen.Disseminator[] genDisseminators=
                 new fedora.server.types.gen.Disseminator[intDisseminators.length];
             for (int i=0; i<intDisseminators.length; i++) {
                 genDisseminators[i]=TypeUtility.convertDisseminatorToGenDisseminator(
                            intDisseminators[i]);
                 }
         return genDisseminators;
     }
 
     public fedora.server.types.gen.Datastream[] getDatastreamHistory(String PID, String datastreamID)
             throws java.rmi.RemoteException {
         assertInitialized();
         try {
             fedora.server.storage.types.Datastream[] intDatastreams=
                     s_management.getDatastreamHistory(
                             getContext(),
                             PID,
                             datastreamID);
             return getGenDatastreams(intDatastreams);
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         } catch (Exception e) {
             throw AxisUtility.getFault(new ServerInitializationException(e.getClass().getName() + ": " + e.getMessage()));
         }
     }
 
 	public String addDisseminator(String PID, String bDefPID, String bMechPID, String dissLabel, String bDefLabel, String bMechLabel, fedora.server.types.gen.DatastreamBindingMap bindingMap, String dissState) throws java.rmi.RemoteException {
 		assertInitialized();
 		try {
 			return s_management.addDisseminator(getContext(), PID, bDefPID, bMechPID, dissLabel, bDefLabel, bMechLabel,
 					TypeUtility.convertGenDatastreamBindingMapToDSBindingMap(bindingMap), dissState);
 		} catch (ServerException se) {
 			logStackTrace(se);
 			throw AxisUtility.getFault(se);
 	}
 }
 
     public java.util.Calendar[] purgeDisseminator(String PID,
             String disseminatorID, java.util.Calendar endDT)
             throws java.rmi.RemoteException {
         assertInitialized();
         try {
             return s_management.purgeDisseminator(getContext(), PID, disseminatorID, endDT);
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         } catch (Exception e) {
             throw AxisUtility.getFault(new ServerInitializationException(e.getClass().getName() + ": " + e.getMessage()));
         }
     }
 
     public fedora.server.types.gen.Disseminator[] getDisseminatorHistory(String PID, String disseminatorID) throws java.rmi.RemoteException {
       assertInitialized();
       try {
           fedora.server.storage.types.Disseminator[] intDisseminators=
                   s_management.getDisseminatorHistory(
                           getContext(),
                           PID,
                           disseminatorID);
           return getGenDisseminators(intDisseminators);
       } catch (ServerException se) {
           logStackTrace(se);
           throw AxisUtility.getFault(se);
       } catch (Exception e) {
           throw AxisUtility.getFault(new ServerInitializationException(e.getClass().getName() + ": " + e.getMessage()));
         }
     }
 
     public fedora.server.types.gen.Disseminator getDisseminator(String PID, String disseminatorID, java.util.Calendar asOfDateTime) throws java.rmi.RemoteException {
       assertInitialized();
       try {
           fedora.server.storage.types.Disseminator diss=s_management.getDisseminator(getContext(), PID, disseminatorID, asOfDateTime);
           return TypeUtility.convertDisseminatorToGenDisseminator(diss);
       } catch (ServerException se) {
           logStackTrace(se);
           throw AxisUtility.getFault(se);
       } catch (Exception e) {
           throw AxisUtility.getFault(new ServerInitializationException(e.getClass().getName() + ": " + e.getMessage()));
         }
     }
 
     public fedora.server.types.gen.Disseminator[] getDisseminators(String PID, java.util.Calendar asOfDateTime, String dissState) throws java.rmi.RemoteException {
       assertInitialized();
           try {
               fedora.server.storage.types.Disseminator[] intDisseminators=s_management.getDisseminators(getContext(), PID, asOfDateTime, dissState);
               return getGenDisseminators(intDisseminators);
           } catch (ServerException se) {
               logStackTrace(se);
               throw AxisUtility.getFault(se);
           } catch (Exception e) {
               throw AxisUtility.getFault(new ServerInitializationException(e.getClass().getName() + ": " + e.getMessage()));
         }
     }
 
     public void modifyDisseminator(String PID, String disseminatorID, String bMechPID, String dissLabel, String bDefLabel, String bMechLabel, fedora.server.types.gen.DatastreamBindingMap bindingMap, String logMessage, String dissState) throws java.rmi.RemoteException {
         assertInitialized();
         try {
             s_management.modifyDisseminator(getContext(), PID,
                     disseminatorID, bMechPID, dissLabel, bDefLabel, bMechLabel,
                     TypeUtility.convertGenDatastreamBindingMapToDSBindingMap(bindingMap),
                     logMessage, dissState);
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         }
     }
 
     public java.lang.String[] getNextPID(NonNegativeInteger numPIDs,
             String namespace)
             throws java.rmi.RemoteException {
         assertInitialized();
         try {
             if(numPIDs==null) numPIDs=new NonNegativeInteger("1");
             return s_management.getNextPID(getContext(), numPIDs.intValue(), namespace);
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         } catch (Exception e) {
             throw AxisUtility.getFault(new ServerInitializationException(e.getClass().getName() + ": " + e.getMessage()));
         }
     }
 
 
     private void assertInitialized()
             throws java.rmi.RemoteException {
         if (!s_initialized) {
             AxisUtility.throwFault(s_initException);
         }
     }
 
 }
