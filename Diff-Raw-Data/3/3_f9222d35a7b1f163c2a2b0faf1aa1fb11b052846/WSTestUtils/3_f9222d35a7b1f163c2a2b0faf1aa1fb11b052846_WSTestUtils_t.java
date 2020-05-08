 /******************************************************************************* 
  * Copyright (c) 2010 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/
 package org.jboss.tools.ws.ui.utils;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.jboss.tools.ws.ui.JBossWSUIPlugin;
 import org.jboss.tools.ws.ui.messages.JBossWSUIMessages;
 import org.xml.sax.SAXException;
 
 /**
  * Static utility methods for testing JAX-RS and JAX-WS web services
  * @author bfitzpat
  *
  */
 public class WSTestUtils {
 	
 	public static String addNLsToXML( String incoming ) {
 		String outgoing = null;
 		if (incoming != null) {
 			outgoing = incoming.replaceAll("><",">\n<");//$NON-NLS-1$ //$NON-NLS-2$
 		}
 		return outgoing;
 	}
 	
 	public static String stripNLsFromXML ( String incoming ) {
 		String outgoing = null;
 		if (incoming != null) {
			String find = "(>)\n\\s*+(<)";//$NON-NLS-1$
			outgoing = incoming.replaceAll(find, "><");//$NON-NLS-1$
 			if (outgoing.contains("\n"))//$NON-NLS-1$ 
 				outgoing.replaceAll("\n"," ");//$NON-NLS-1$ //$NON-NLS-2$
 			if (outgoing.contains("\r"))//$NON-NLS-1$ 
 				outgoing.replaceAll("\r"," ");//$NON-NLS-1$ //$NON-NLS-2$
 		}
 		return outgoing;
 	}
 
 	public static IStatus saveTextToFile ( String text ) {
 		FileDialog fd = new FileDialog(new Shell(Display.getCurrent()), SWT.SAVE);
 		fd.setText(JBossWSUIMessages.WSTestUtils_SaveResponseText_Title);
 		String[] filterExt = { "*.txt", "*.xml"}; //$NON-NLS-1$ //$NON-NLS-2$
 		fd.setFilterExtensions(filterExt);
 		String selected = fd.open();
 		if (selected != null) {
 			FileOutputStream out;
 			PrintStream p;
 
 			try
 			{
 				// Create a new file output stream for the file
 				out = new FileOutputStream(selected);
 
 				// Connect print stream to the output stream
 				p = new PrintStream( out );
 
 				// print to it and close
 				p.println (text);
 				p.close();
 
 				return Status.OK_STATUS;
 			}
 			catch (Exception e)
 			{
 				Status rtnStatus = new Status(IStatus.ERROR, 
 						JBossWSUIPlugin.PLUGIN_ID,
 						JBossWSUIMessages.WSTestUtils_SaveResponseText_Error_Msg,
 						e);
 				return rtnStatus;
 			}
 		}
 		return Status.CANCEL_STATUS;
 	}
 	
 	public static boolean isTextXML ( String text ) {
 		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 		dbf.setValidating(false);
 		try {
 			//Using factory get an instance of document builder
 			DocumentBuilder db = dbf.newDocumentBuilder();
 			
 			ByteArrayInputStream bais = new ByteArrayInputStream(text.getBytes());
 			
 			//parse using builder to get DOM representation of the XML file
 			db.parse(bais);
 			
 			return true;
 
 		}catch(ParserConfigurationException pce) {
 			// ignore
 		}catch(SAXException se) {
 			// ignore
 		} catch (IOException e) {
 			// ignore
 		}
 		return false;
 	}
 }
