 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 package com.netspective.sparx.form;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Serializable;
 import java.net.UnknownHostException;
 import java.security.NoSuchAlgorithmException;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import com.netspective.commons.text.GloballyUniqueIdentifier;
 import com.netspective.sparx.form.field.DialogField;
 import com.netspective.sparx.form.field.DialogFieldStates;
 import com.netspective.sparx.value.HttpServletValueContext;
 
 /**
  * Manages server-side state data that should be secured from changes by a user. While most information for a dialog
  * or form is passed through the HTTP request, some data requires additional security.
  */
 public class DialogState implements Serializable
 {
     private static final Log log = LogFactory.getLog(DialogState.class);
 
     public static final String PARAMNAME_DEBUG_FLAGS = "debug_flags";
     public static final String PARAMNAME_PERSPECTIVE = "data_perspective";
 
     private String identifier;
     private String referer;
     private DialogDebugFlags debugFlags = new DialogDebugFlags();
     private DialogPerspectives perspectives = new DialogPerspectives();
     private boolean executeMode;
     private int runSequence;
     private int execSequence;
     private String initialFieldStatesXml;
     private boolean executeModeLocked;
 
     public DialogState()
     {
         executeModeLocked = false;
         try
         {
             identifier = GloballyUniqueIdentifier.getRandomGUID(true);
         }
         catch(NoSuchAlgorithmException e)
         {
             identifier = Integer.toString(hashCode());
         }
         catch(UnknownHostException e)
         {
             identifier = Integer.toString(hashCode());
         }
     }
 
     /**
      * Initialize the state immediately after creating it.
      */
     public void initialize(HttpServletValueContext vc)
     {
         HttpServletRequest request = vc.getHttpRequest();
 
         executeMode = false;
         runSequence = 0;
         execSequence = 0;
         referer = request.getHeader("Referer");
 
         String dataCmdStr = (String) request.getAttribute(PARAMNAME_PERSPECTIVE);
         if(dataCmdStr == null)
             dataCmdStr = request.getParameter(PARAMNAME_PERSPECTIVE);
 
         String debugFlagsStr = (String) request.getAttribute(PARAMNAME_DEBUG_FLAGS);
         if(debugFlagsStr == null)
             debugFlagsStr = request.getParameter(PARAMNAME_DEBUG_FLAGS);
 
         perspectives.setValue(dataCmdStr);
         debugFlags.setValue(debugFlagsStr);
     }
 
     /**
      * Reset the state so that it looks like it was called at the start of the first call in a dialog context
      */
     public void reset(HttpServletValueContext vc)
     {
         initialize(vc);
         incRunSequence();
     }
 
     public String getIdentifier()
     {
         return identifier;
     }
 
     public String getReferer()
     {
         return referer;
     }
 
     public DialogDebugFlags getDebugFlags()
     {
         return debugFlags;
     }
 
     public DialogPerspectives getPerspectives()
     {
         return perspectives;
     }
 
     public boolean isInInputMode()
     {
         return !executeMode;
     }
 
     public boolean isInExecuteMode()
     {
         return executeMode;
     }
 
    public void setExecuteMode()
     {
        executeMode = true;
        incExecSequence();
     }
 
     /**
      * Checks to see if the dialog has already been executed once.
      */
     public boolean isAlreadyExecuted()
     {
         return executeModeLocked;
     }
 
     /**
      * Sets a flag to indicate that the dialog has been executed once. This will be used
      * to make sure an already executed dialog will not be executed again unless
      * a new request for the dialog is made. Essestially, this is used for taking care of
      * users pressing BACK button on the browser and re-executing the dialog.
      */
     public void setAlreadyExecuted()
     {
         executeModeLocked = true;
     }
 
     public int getExecSequence()
     {
         return execSequence;
     }
 
     public void incExecSequence()
     {
         execSequence = execSequence + 1;
     }
 
     public boolean isInitialEntry()
     {
         return runSequence == 1;
     }
 
     public int getRunSequence()
     {
         return runSequence;
     }
 
     public void incRunSequence()
     {
         runSequence = runSequence + 1;
     }
 
     public String getActiveModeText()
     {
         return isInExecuteMode() ? "execute" : "input";
     }
 
     public boolean isInLoadPersistentFieldDataMode()
     {
         return !executeMode && runSequence == 1;
     }
 
     public String toString()
     {
         StringBuffer sb = new StringBuffer();
         sb.append("DialogState id = " + getIdentifier() + ", ");
         sb.append("mode = " + getActiveModeText() + ", ");
         sb.append("runSeq = " + getRunSequence() + ", ");
         sb.append("execSeq = " + getExecSequence());
         return sb.toString();
     }
 
     public void saveInitialState(DialogContext dc)
     {
         try
         {
             initialFieldStatesXml = dc.getAsXml();
         }
         catch(Exception e)
         {
             log.error("Unable to store initial states", e);
             initialFieldStatesXml = null;
         }
     }
 
     /**
      * Creates a map of initial field states
      */
     public DialogFieldStates getInitialStateFields(DialogContext dc) throws ParserConfigurationException, SAXException, IOException
     {
         DialogFieldStates initialFieldStates = new DialogFieldStates(dc);
         if(initialFieldStatesXml == null)
         {
             log.error("No initial states available.");
             return initialFieldStates;
         }
 
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
 
         InputStream is = new java.io.ByteArrayInputStream(initialFieldStatesXml.getBytes());
         Document doc = builder.parse(is);
         NodeList dcList = doc.getDocumentElement().getElementsByTagName("dialog-context");
         if(dcList.getLength() > 0)
         {
             Element dcElem = (Element) dcList.item(0);
             NodeList children = dcElem.getChildNodes();
             for(int n = 0; n < children.getLength(); n++)
             {
                 Node node = children.item(n);
                 if(node.getNodeName().equals("field"))
                 {
                     Element fieldElem = (Element) node;
                     String fieldName = fieldElem.getAttribute("name");
                     DialogField field = dc.getDialog().getFields().getByName(fieldName);
                     if(field != null)
                     {
                         DialogField.State state = field.constructStateInstance(dc);
                         state.importFromXml(fieldElem);
                         initialFieldStates.addState(state);
                     }
                 }
             }
         }
 
         return initialFieldStates;
     }
 }
