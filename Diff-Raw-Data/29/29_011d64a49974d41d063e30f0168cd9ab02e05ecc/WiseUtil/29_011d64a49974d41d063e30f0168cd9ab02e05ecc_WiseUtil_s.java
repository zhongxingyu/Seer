 /******************************************************************************* 
  * Copyright (c) 2013 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.wise.ui.internal.util;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.ConnectException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.wsdl.Definition;
 
 import org.apache.cxf.staxutils.StaxUtils;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.progress.IProgressService;
 import org.jboss.wise.core.client.WSDynamicClient;
 import org.jboss.wise.core.client.WSMethod;
 import org.jboss.wise.core.client.WebParameter;
 import org.jboss.wise.core.client.builder.WSDynamicClientBuilder;
 import org.jboss.wise.core.client.factories.WSDynamicClientFactory;
 import org.jboss.wise.core.exception.InvocationException;
 import org.jboss.wise.core.exception.ResourceNotAvailableException;
 import org.jboss.wise.core.exception.WiseRuntimeException;
 import org.jboss.wise.core.utils.JavaUtils;
 import org.jboss.wise.tree.ElementBuilder;
 import org.jboss.wise.tree.ElementBuilderFactory;
 
 /**
  * @author bfitzpat
  *
  */
 public final class WiseUtil {
     
     private static String _messagePreview = null;
     
     private WiseUtil() {
         // private constructor
     }
 
     /**
      * @param wsdlDefinition WSDL we're generating the SOAP message from
      * @param serviceName Name of the service in the WSDL
      * @param portName Name of the port in the WSDL
      * @param bindingName Name of the binding in the WSDL
      * @param opName Name of the operation in the WSDL
      * @return String containing XML for the SOAP message
      */
     public static String getSampleSOAPInputMessageFromWISE ( final Definition wsdlDefinition, 
             final String serviceName, final String portName, final String bindingName, 
             final String opName ) {
         
         _messagePreview = null;
         IWorkbench wb = PlatformUI.getWorkbench();
         IProgressService ps = wb.getProgressService();
         try {
             ps.busyCursorWhile(new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) {
                     monitor.beginTask(Messages.WiseUtil_Job_header, 100);
                     Thread current = Thread.currentThread();
                     ClassLoader oldLoader = current.getContextClassLoader();
                     try {
                         monitor.subTask(Messages.WiseUtil_WSDL_task);
                         monitor.worked(33);
                         URL wsdlURL = new URL(wsdlDefinition.getDocumentBaseURI());
                         monitor.subTask(Messages.WiseUtil_WISE_task);
                         monitor.worked(33);
                         WSDynamicClientBuilder clientBuilder = WSDynamicClientFactory.getJAXWSClientBuilder();
                         current.setContextClassLoader(StaxUtils.class.getClassLoader());
                         WSDynamicClient client = clientBuilder.tmpDir(createTempDirectory().getAbsolutePath())
                                 .verbose(true).keepSource(true).wsdlURL(wsdlURL.toString()).build();
                         WSMethod method = client.getWSMethod(serviceName, portName, opName);
                         monitor.subTask(Messages.WiseUtil_Generating_SOAP_task);
                         monitor.worked(33);
                         _messagePreview = previewMessage(method, client);
                     } catch (MalformedURLException mfe) {
                        // ignore
                     } catch (ConnectException ce) {
                        // ignore
                     } catch (IllegalStateException ise) {
                        // ignore
                     } catch (WiseRuntimeException wre) {
                        // ignore
                     } catch (ResourceNotAvailableException rnae) {
                        // ignore
                     } catch (InvocationException ie) {
                        // ignore
                     } catch (IOException ioe) {
                        // ignore
                     } finally {
                         current.setContextClassLoader(oldLoader);
                     }
                 }
             });
             return _messagePreview;
         } catch (InvocationTargetException e) {
             // ignore
         } catch (InterruptedException e) {
             // ignore
         }
         return null;
     }
 
     private static File createTempDirectory() throws IOException {
         final File temp;
 
         temp = File.createTempFile("temp", Long.toString(System.nanoTime())); //$NON-NLS-1$
 
         if (!(temp.delete())) {
             throw new IOException("Could not delete temp file: " + temp.getAbsolutePath()); //$NON-NLS-1$
         }
 
         if (!(temp.mkdir())) {
             throw new IOException("Could not create temp directory: " + temp.getAbsolutePath()); //$NON-NLS-1$
         }
 
         return (temp);
     }
     
     private static String previewMessage(WSMethod method, WSDynamicClient client) throws InvocationException {
         Map<String, ? extends WebParameter> pars = method.getWebParams();
         ElementBuilder builder = ElementBuilderFactory.getElementBuilder().client(client).request(true).useDefautValuesForNullLeaves(false);
         Map<String, org.jboss.wise.tree.Element> elementsMap = new HashMap<String, org.jboss.wise.tree.Element>();
         for (Entry<String, ? extends WebParameter> par : pars.entrySet()) {
             String parName = par.getKey();
             org.jboss.wise.tree.Element parElement = builder.buildTree(par.getValue().getType(), parName, null, true);
             populateElement(parElement, 1);
             elementsMap.put(parName, parElement);
         }
         Map<String, Object> args = new java.util.HashMap<String, Object>();
         for (Entry<String, org.jboss.wise.tree.Element> elem : elementsMap.entrySet()) {
             args.put(elem.getKey(), ((org.jboss.wise.tree.Element) elem.getValue()).toObject());
         }
 
         ByteArrayOutputStream bos = new ByteArrayOutputStream();
         method.writeRequestPreview(args, bos);
         return bos.toString();
     }
 
     private static void populateElement(org.jboss.wise.tree.Element element, int remainingLazyExpansions) {
         if (element.isLazy()) {
             if (!(element.isResolved())) {
                 if (remainingLazyExpansions > 0) {
                     element.getChildrenCount(); // force resolution
                     populateElement(element, --remainingLazyExpansions);
                 } else {
                     return;
                 }
             }
         }
         element.setNil(false);
         if (element.isLeaf()) {
             element.setValue(getDefaultValue((Class<?>) element.getClassType()));
         } else {
             if (element.isGroup()) {
                 element.incrementChildren();
             }
             for (Iterator<? extends org.jboss.wise.tree.Element> it = element.getChildren(); it.hasNext();) {
                 populateElement(it.next(), remainingLazyExpansions);
             }
         }
     }
 
     private static String getDefaultValue(Class<?> cl) {
         if (cl.isPrimitive()) {
             cl = JavaUtils.getWrapperType(cl);
         }
         String cn = cl.getName();
         if ("java.lang.Boolean".equals(cn)) { //$NON-NLS-1$
             return "false";//$NON-NLS-1$
         } else if ("java.lang.String".equals(cn)) {//$NON-NLS-1$
             return "?";//$NON-NLS-1$
         } else if ("java.lang.Byte".equals(cn)) {//$NON-NLS-1$
             return "0";//$NON-NLS-1$
         } else if ("java.lang.Double".equals(cn)) {//$NON-NLS-1$
             return "0.0";//$NON-NLS-1$
         } else if ("java.lang.Float".equals(cn)) {//$NON-NLS-1$
             return "0.0";//$NON-NLS-1$
         } else if ("java.lang.Integer".equals(cn)) {//$NON-NLS-1$
             return "0";//$NON-NLS-1$
         } else if ("java.lang.Long".equals(cn)) {//$NON-NLS-1$
             return "0";//$NON-NLS-1$
         } else if ("java.lang.Short".equals(cn)) {//$NON-NLS-1$
             return "0";//$NON-NLS-1$
         } else if ("java.math.BigDecimal".equals(cn)) {//$NON-NLS-1$
             return "0.0";//$NON-NLS-1$
         } else if ("java.math.BigInteger".equals(cn)) {//$NON-NLS-1$
             return "0";//$NON-NLS-1$
         } else if ("javax.xml.datatype.Duration".equals(cn)) {//$NON-NLS-1$
             return "0";//$NON-NLS-1$
         } else if ("javax.xml.datatype.XMLGregorianCalendar".equals(cn)) {//$NON-NLS-1$
             return "1970-01-01T00:00:00.000Z";//$NON-NLS-1$
         } else {
             return "";//$NON-NLS-1$
         }
     }
  }
