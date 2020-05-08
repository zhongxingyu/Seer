 package fedora.server.management;
 
 import fedora.server.Context;
 import fedora.server.ReadOnlyContext;
 import fedora.server.Server;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.ServerInitializationException;
 import fedora.server.errors.StorageDeviceException;
 import fedora.server.management.Management;
 import fedora.server.utilities.AxisUtility;
 import fedora.server.utilities.DateUtility;
 import fedora.server.utilities.TypeUtility;
 
 import java.io.File;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.rmi.RemoteException;
 import java.util.Date;
 import javax.servlet.http.HttpServletRequest;
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
  * <p>The entire file consists of original code.  Copyright &copy; 2002-2005 by The
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
         } catch (InitializationException ie) {
             System.err.println(ie.getMessage());
             s_initialized=false;
             s_initException=ie;
         }
     }
 
     private Context getContext() {
         HttpServletRequest req=(HttpServletRequest) MessageContext.
                 getCurrentContext().getProperty(
                 HTTPConstants.MC_HTTP_SERVLETREQUEST);
        return ReadOnlyContext.getContext("soap", req, false);
     }
 
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
 
 	// DEPRECATED. This remains in Fedora 2.0 for backward compatibility.  
 	// It assumes METS-Fedora as the ingest format.  It will be remove in a future version.
     public String ingestObject(byte[] METSXML, String logMessage) throws java.rmi.RemoteException {
         assertInitialized();
 		return ingest(METSXML, "metslikefedora1", logMessage);
 		/**
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
         **/
     }
     
 	public String ingest(byte[] XML, String format, String logMessage) throws java.rmi.RemoteException {
 		assertInitialized();
 		try {
 		  // always gens pid, unless pid in stream starts with "test:" "demo:"
 		  // or other prefix that is configured in the retainPIDs parameter of fedora.fcfg
 			return s_management.ingestObject(getContext(),
 					new ByteArrayInputStream(XML), logMessage, format, "UTF-8", true);
 		} catch (ServerException se) {
 			logStackTrace(se);
 			throw AxisUtility.getFault(se);
 		} catch (Exception e) {
 			logStackTrace(e);
 			throw AxisUtility.getFault(e);
 		}
 	}
 
     public String modifyObject(String PID, String state, String label,
             String logMessage)
             throws RemoteException {
         assertInitialized();
         try {
             return DateUtility.convertDateToString(
                     s_management.modifyObject(
                             getContext(), PID, state, label, logMessage));
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         } catch (Exception e) {
             logStackTrace(e);
             throw AxisUtility.getFault(e);
         }
     }
 
 	public fedora.server.types.gen.Property[] getObjectProperties(String PID)
 			throws RemoteException {
 		assertInitialized();
 		try {
 			fedora.server.storage.types.Property[] properties=
 					s_management.getObjectProperties(
 							getContext(), PID);
 			return TypeUtility.convertPropertyArrayToGenPropertyArray(properties);
 		} catch (ServerException se) {
 			logStackTrace(se);
 			throw AxisUtility.getFault(se);
 		} catch (Exception e) {
 			throw AxisUtility.getFault(new ServerInitializationException(e.getClass().getName() + ": " + e.getMessage()));
 		}				
 				
 	}
 
     public fedora.server.types.gen.UserInfo describeUser(String id)
             throws RemoteException {
         if (id==null || !id.equals("fedoraAdmin")) {
             throw AxisUtility.getFault(new GeneralException("Unrecognized user: " + id));
         }
         fedora.server.types.gen.UserInfo inf=new fedora.server.types.gen.UserInfo();
         inf.setId(id);
         inf.setAdministrator(true);
         return inf;
     }
 
 
     public byte[] getObjectXML(String PID)
             throws RemoteException {
         assertInitialized();
         try {
             InputStream in=s_management.getObjectXML(getContext(), PID, "UTF-8");
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
 			InputStream in=s_management.exportObject(getContext(), PID, null, null, "UTF-8");
             ByteArrayOutputStream out=new ByteArrayOutputStream();
             pipeStream(in, out);
             return out.toByteArray();
         } catch (ServerException se) {
             throw AxisUtility.getFault(se);
         } catch (Exception e) {
             throw AxisUtility.getFault(new ServerInitializationException(e.getClass().getName() + ": " + e.getMessage()));
         }
     }
     
 	public byte[] export(String PID, String format, String exportContext)
 			throws RemoteException {
 		assertInitialized();
 		try {
 			InputStream in=s_management.exportObject(getContext(), PID, format, exportContext, "UTF-8");
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
 
     public String purgeObject(String PID, 
                               String logMessage,
                               boolean force) throws java.rmi.RemoteException {
         assertInitialized();
         try {
             return DateUtility.convertDateToString(
                     s_management.purgeObject(getContext(), 
                                              PID, 
                                              logMessage,
                                              force));
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         }
     }
 
     public String addDatastream(String pid,
                                 String dsID,
                                 String[] altIds,
                                 String label,
                                 boolean versionable,
                                 String MIMEType,
                                 String formatURI,
                                 String location,
                                 String controlGroup,
                                 String dsState,
                                 String logMessage) throws RemoteException {
         assertInitialized();
         try {
             return s_management.addDatastream(getContext(), 
                                                  pid, 
                                                  dsID,
                                                  altIds,
                                                  label, 
                                                  versionable,
                                                  MIMEType,
                                                  formatURI,
                                                  location,
                                                  controlGroup,
                                                  dsState,
                                                  logMessage);
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         }
     }
 
     public String modifyDatastreamByReference(String PID, 
                                               String datastreamID,
                                               String[] altIDs,
                                               String dsLabel, 
                                               boolean versionable,
                                               String mimeType,
                                               String formatURI,
                                               String dsLocation, 
                                               String dsState,
                                               String logMessage, 
                                               boolean force)
             throws java.rmi.RemoteException {
         assertInitialized();
         try {
             return DateUtility.convertDateToString(
                     s_management.modifyDatastreamByReference(
                             getContext(), 
                             PID,
                             datastreamID, 
                             altIDs,
                             dsLabel, 
                             versionable,
                             mimeType,
                             formatURI,
                             dsLocation, 
                             dsState,
                             logMessage, 
                             force));
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         }
     }
 
     public String modifyDatastreamByValue(String PID, 
                                           String datastreamID, 
                                           String[] altIDs,
                                           String dsLabel, 
                                           boolean versionable,
                                           String mimeType,
                                           String formatURI,
                                           byte[] dsContent, 
                                           String dsState,
                                           String logMessage, 
                                           boolean force) 
                 throws java.rmi.RemoteException {
         assertInitialized();
         try {
             ByteArrayInputStream byteStream = null;
             if (dsContent!=null && dsContent.length>0) {
                 byteStream = new ByteArrayInputStream(dsContent);
             }
             return DateUtility.convertDateToString(
                     s_management.modifyDatastreamByValue(getContext(), 
                                                          PID,
                                                          datastreamID, 
                                                          altIDs,
                                                          dsLabel, 
                                                          versionable,
                                                          mimeType,
                                                          formatURI,
                                                          byteStream, 
                                                          dsState,
                                                          logMessage, 
                                                          force));
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         }
     }
 
     public String setDatastreamState(String PID, 
                                      String datastreamID, 
                                      String dsState, 
                                      String logMessage) throws java.rmi.RemoteException {
         assertInitialized();
         try {
             return DateUtility.convertDateToString(
                     s_management.setDatastreamState(getContext(), 
                                                     PID,
                                                     datastreamID, 
                                                     dsState, 
                                                     logMessage));
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         }
     }
 
     public String setDisseminatorState(String PID, 
                                        String disseminatorID, 
                                        String dissState, 
                                        String logMessage) throws java.rmi.RemoteException {
         assertInitialized();
         try {
             return DateUtility.convertDateToString(
                     s_management.setDisseminatorState(getContext(), 
                                                       PID,
                                                       disseminatorID, 
                                                       dissState, 
                                                       logMessage));
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         }
     }
 
     public String[] purgeDatastream(String PID, 
                                     String datastreamID, 
                                     String endDT,
                                     String logMessage,
                                     boolean force) 
             throws java.rmi.RemoteException {
         assertInitialized();
         try {
             return toStringArray(
                     s_management.purgeDatastream(
                             getContext(), 
                             PID, 
                             datastreamID,
                             DateUtility.convertStringToDate(endDT),
                             logMessage,
                             force));
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         } catch (Exception e) {
             throw AxisUtility.getFault(new ServerInitializationException(e.getClass().getName() + ": " + e.getMessage()));
         }
     }
 
     private String[] toStringArray(Date[] dates) throws Exception {
         String[] out = new String[dates.length];
         for (int i = 0; i < dates.length; i++) {
             out[i] = DateUtility.convertDateToString(dates[i]);
         }
         return out;
     }
 
     public fedora.server.types.gen.Datastream getDatastream(String PID, 
                                                             String datastreamID, 
                                                             String asOfDateTime) 
             throws java.rmi.RemoteException {
         assertInitialized();
         try {
             fedora.server.storage.types.Datastream ds=
                     s_management.getDatastream(
                             getContext(), 
                             PID, 
                             datastreamID, 
                             DateUtility.convertStringToDate(asOfDateTime));
             return TypeUtility.convertDatastreamToGenDatastream(ds);
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         } catch (Exception e) {
             throw AxisUtility.getFault(new ServerInitializationException(e.getClass().getName() + ": " + e.getMessage()));
         }
     }
 
     public fedora.server.types.gen.Datastream[] getDatastreams(String PID, 
                                                                String asOfDateTime, 
                                                                String state) 
             throws java.rmi.RemoteException {
         assertInitialized();
         try {
             fedora.server.storage.types.Datastream[] intDatastreams=
                     s_management.getDatastreams(
                             getContext(), 
                             PID, 
                             DateUtility.convertStringToDate(asOfDateTime),
                             state);
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
 
 	public String addDisseminator(String PID, 
 	                              String bDefPID, 
 	                              String bMechPID, 
 	                              String dissLabel,  
 	                              fedora.server.types.gen.DatastreamBindingMap bindingMap, 
 	                              String dissState,
 	                              String logMessage) 
             throws java.rmi.RemoteException {
 		assertInitialized();
 		try {
 			return s_management.addDisseminator(
 			        getContext(), 
 		            PID, 
 	                bDefPID, 
                     bMechPID, 
                     dissLabel, 
                     TypeUtility.convertGenDatastreamBindingMapToDSBindingMap(bindingMap), 
                     dissState,
                     logMessage);
 		} catch (ServerException se) {
 			logStackTrace(se);
 			throw AxisUtility.getFault(se);
 	}
 }
 
     public String[] purgeDisseminator(String PID,
                                       String disseminatorID, 
                                       String endDT,
                                       String logMessage)
             throws java.rmi.RemoteException {
         assertInitialized();
         try {
             return toStringArray(
                     s_management.purgeDisseminator(
                             getContext(), 
                             PID, 
                             disseminatorID, 
                             DateUtility.convertStringToDate(endDT),
                             logMessage));
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         } catch (Exception e) {
             throw AxisUtility.getFault(new ServerInitializationException(e.getClass().getName() + ": " + e.getMessage()));
         }
     }
 
     public fedora.server.types.gen.Disseminator[] getDisseminatorHistory(String PID, 
                                                                          String disseminatorID) 
               throws java.rmi.RemoteException {
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
 
     public fedora.server.types.gen.Disseminator getDisseminator(String PID, 
                                                                 String disseminatorID, 
                                                                 String asOfDateTime) 
             throws java.rmi.RemoteException {
         assertInitialized();
         try {
             fedora.server.storage.types.Disseminator diss=
                     s_management.getDisseminator(
                             getContext(), 
                             PID, 
                             disseminatorID, 
                             DateUtility.convertStringToDate(asOfDateTime));
             return TypeUtility.convertDisseminatorToGenDisseminator(diss);
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         } catch (Exception e) {
             throw AxisUtility.getFault(new ServerInitializationException(e.getClass().getName() + ": " + e.getMessage()));
         }
     }
 
     public fedora.server.types.gen.Disseminator[] getDisseminators(String PID, 
                                                                    String asOfDateTime, 
                                                                    String dissState) 
               throws java.rmi.RemoteException {
         assertInitialized();
         try {
             fedora.server.storage.types.Disseminator[] intDisseminators=
                     s_management.getDisseminators(
                             getContext(), 
                             PID, 
                             DateUtility.convertStringToDate(asOfDateTime),
                             dissState);
             return getGenDisseminators(intDisseminators);
         } catch (ServerException se) {
             logStackTrace(se);
             throw AxisUtility.getFault(se);
         } catch (Exception e) {
             throw AxisUtility.getFault(new ServerInitializationException(e.getClass().getName() + ": " + e.getMessage()));
         }
     }
 
     public String modifyDisseminator(String PID, 
                                      String disseminatorID, 
                                      String bMechPID, 
                                      String dissLabel,  
                                      fedora.server.types.gen.DatastreamBindingMap bindingMap, 
                                      String dissState,
                                      String logMessage,
                                      boolean force)
             throws java.rmi.RemoteException {
         assertInitialized();
         try {
             return DateUtility.convertDateToString(
                     s_management.modifyDisseminator(
                             getContext(), 
                             PID,
                             disseminatorID, 
                             bMechPID, 
                             dissLabel, 
                             TypeUtility.convertGenDatastreamBindingMapToDSBindingMap(bindingMap),
                             dissState,
                             logMessage,
                             force));
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
