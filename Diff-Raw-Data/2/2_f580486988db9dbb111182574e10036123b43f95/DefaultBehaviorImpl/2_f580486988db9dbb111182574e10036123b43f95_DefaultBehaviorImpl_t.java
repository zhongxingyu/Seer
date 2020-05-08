 package fedora.server.access.internalservices;
 
 import java.util.Calendar;
 import java.util.List;
 import java.util.ArrayList;
 import java.io.File;
 import java.io.InputStream;
 import java.io.IOException;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.Properties;
 import java.util.Date;
 
 import fedora.server.Context;
 import fedora.server.access.Access;
 import fedora.server.access.ObjectProfile;
 import fedora.server.errors.ServerException;
 import fedora.server.errors.DisseminationException;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.InitializationException;
 import fedora.server.errors.ServerInitializationException;
 import fedora.server.errors.ObjectIntegrityException;
 import fedora.server.errors.StreamIOException;
 import fedora.server.storage.DOReader;
 import fedora.server.storage.types.Datastream;
 import fedora.server.storage.types.DatastreamXMLMetadata;
 import fedora.server.storage.types.MethodDef;
 import fedora.server.storage.types.MethodParmDef;
 import fedora.server.storage.types.MIMETypedStream;
 import fedora.server.storage.types.ObjectMethodsDef;
 import fedora.server.storage.types.Property;
 import fedora.server.storage.ExternalContentManager;
 import fedora.server.utilities.DateUtility;
 import fedora.server.Server;
 import fedora.server.utilities.XMLConversions.ObjectInfoAsXML;
 import fedora.server.utilities.XMLConversions.DatastreamsAsXML;
 
 import javax.xml.transform.Templates;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import com.icl.saxon.expr.StringValue;
 
 /**
  * <p><b>Title: </b>DefaultBehaviorImpl.java</p>
  * <p><b>Description: </b>Implements the methods defined in the DefaultBehavior interface.
  * This is the default behavior mechanism that implements the "contract" of
  * the default behavior definition that is dynamically associated with every
  * digital object in the repository.
  *
  * This class is considered an "internal service" that is built in to the
  * Fedora system.  Its purpose is to endow every digital object with a set of
  * generic behaviors.  It is an implementation of what is known as the
  * Default Disseminator.
  *
  * Unlike other behavior definitions and mechanisms, there is no Behavior
  * Definition Object or Behavior Mechanism Object stored in the repository. </p>
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
  * <p>The entire file consists of original code.  Copyright &copy; 2002-2004 by The
  * Rector and Visitors of the University of Virginia and Cornell University.
  * All rights reserved.</p>
  *
  * -----------------------------------------------------------------------------
  *
  * @author payette@cs.cornell.edu
  * @version $Id$
  */
 public class DefaultBehaviorImpl extends InternalService implements DefaultBehavior
 {
   private Context context;
   private String PID;
   private Calendar asOfDateTime;
   private DOReader reader;
   private String reposBaseURL;
   private File reposHomeDir;
   private Access m_access;
 
   public DefaultBehaviorImpl(Context context, Calendar asOfDateTime,
     DOReader reader, Access access, String reposBaseURL, File reposHomeDir)
     throws ServerException
   {
     this.context = context;
     this.asOfDateTime = asOfDateTime;
     this.reader = reader;
     this.m_access = access;
     this.reposBaseURL = reposBaseURL;
     this.reposHomeDir = reposHomeDir;
 
   }
 
  /**
   * <p>Returns the object profile which contains key metadata from the object,
   * plus URLs for the object's Dissemination Index and Item Index.  The data
   * is returned as XML encoded to the objectprofile.xsd schema.</p>
   * @return
   * @throws ServerException
   */
   public MIMETypedStream getObjectProfile() throws ServerException
   {
     ObjectProfile profile = m_access.getObjectProfile(context, reader.GetObjectPID(), asOfDateTime);
     InputStream in = null;
     try
     {
       in = new ByteArrayInputStream(
       (new ObjectInfoAsXML().getObjectProfile(reposBaseURL, profile).getBytes("UTF-8")));
     } catch (UnsupportedEncodingException uee)
     {
       throw new GeneralException("[DefaultBehaviorImpl] An error has occurred. "
           + "The error was a \"" + uee.getClass().getName() + "\"  . The "
           + "Reason was \"" + uee.getMessage() + "\"  .");
     }
     return new MIMETypedStream("text/xml", in);
   }
 
   /**
    * <p>Returns an HTML rendering of the object profile which contains
    * key metadata from the object, plus URLs for the object's Dissemination
    * Index and Item Index.  The data is returned as HTML in a
    * presentation-oriented format.  This is accomplished by doing an XSLT
    * transform on the object profile XML that is obtained from getObjectProfile.</p>
    * @return
    * @throws ServerException
    */
   public MIMETypedStream viewObjectProfile() throws ServerException
   {
     try
     {
       InputStream in = getObjectProfile().getStream();
       ByteArrayOutputStream out = new ByteArrayOutputStream();
       File xslFile = new File(reposHomeDir, "access/viewObjectProfile.xslt");
       TransformerFactory factory = TransformerFactory.newInstance();
       Templates template = factory.newTemplates(new StreamSource(xslFile));
       Transformer transformer = template.newTransformer();
       Properties details = template.getOutputProperties();
       transformer.transform(new StreamSource(in), new StreamResult(out));
       in = new ByteArrayInputStream(out.toByteArray());
       return new MIMETypedStream("text/html", in);
     } catch (TransformerException e)
     {
       throw new DisseminationException("[DefaultBehaviorImpl] had an error "
           + "in transforming xml for viewObjectProfile. "
           + "Underlying exception was: "
           + e.getMessage());
     }
   }
 
   /**
    * <p>Returns the Dissemination Index for the object.  The Dissemination
    * Index is a list of method definitions that represent all disseminations
    * possible on the object.  The Dissemination Index is returned as XML
    * encoded to the objectMethods.xsd schema.</p>
    * @return
    * @throws ServerException
    */
   public MIMETypedStream getMethodIndex() throws ServerException
   {
     // sdp: the dissemination index doesn't work for bdef and bmech objects
     // so send back a message saying so.
     if ((reader.getFedoraObjectType().equalsIgnoreCase("D")) ||
         (reader.getFedoraObjectType().equalsIgnoreCase("M")))
     {
       return noMethodIndexMsg();
     }
 
     ObjectMethodsDef[] methods =
       m_access.getObjectMethods(context, reader.GetObjectPID(), asOfDateTime);
     InputStream in = null;
     try
     {
       in = new ByteArrayInputStream(
       (new ObjectInfoAsXML().getMethodIndex(
         reposBaseURL, reader.GetObjectPID(), methods).getBytes("UTF-8")));
     } catch (UnsupportedEncodingException uee)
     {
       throw new GeneralException("[DefaultBehaviorImpl] An error has occurred. "
           + "The error was a \"" + uee.getClass().getName() + "\"  . The "
           + "Reason was \"" + uee.getMessage() + "\"  .");
     }
     return new MIMETypedStream("text/xml", in);
   }
 
   private MIMETypedStream noMethodIndexMsg() throws GeneralException
   {
       String msg = new String("The Dissemination Index is not available"
         + " for Behavior Definition or Behavior Mechanism objects."
        + " The addition of this feature is not currently scheduled.");
       StringBuffer sb = new StringBuffer();
       sb.append("<html><head><title>Dissemination Index Not Available</title></head>");
       sb.append("<body><center>");
       sb.append("<table width=\"784\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
       sb.append("<tr><td width=\"141\" height=\"134\" valign=\"top\"><img src=\"/images/newlogo2.jpg\" width=\"141\" height=\"134\"></td>");
       sb.append("<td width=\"643\" valign=\"top\">");
       sb.append("<center><h2>Fedora Repository</h2>");
       sb.append("<h3>Dissemination Index</h3>");
       sb.append("</center></td></tr></table>");
       sb.append("<p>" + msg + "</p>");
       sb.append("</body>");
       sb.append("</html>");
       String msgOut = sb.toString();
       ByteArrayInputStream in = null;
       try
       {
         in = new ByteArrayInputStream(msgOut.getBytes("UTF-8"));
       }
       catch (UnsupportedEncodingException uee)
       {
         throw new GeneralException("[DefaultBehaviorImpl] An error has occurred. "
             + "The error was a \"" + uee.getClass().getName() + "\"  . The "
             + "Reason was \"" + uee.getMessage() + "\"  .");
       }
       return new MIMETypedStream("text/html", in);
   }
 
   /**
    * <p>Returns an HTML rendering of the Dissemination Index for the object.
    * The Dissemination Index is a list of method definitions that represent
    * all disseminations possible on the object.  The Dissemination Index
    * is returned as HTML in a presentation-oriented format.  This is
    * accomplished by doing an XSLT transform on the XML that is
    * obtained from getMethodIndex.</p>
    * @return
    * @throws ServerException
    */
   public MIMETypedStream viewMethodIndex() throws ServerException
   {
     // sdp: the dissemination index doesn't work for bdef and bmech objects
     // so send back a message saying so.
     if ((reader.getFedoraObjectType().equalsIgnoreCase("D")) ||
         (reader.getFedoraObjectType().equalsIgnoreCase("M")))
     {
       return noMethodIndexMsg();
     }
 
     try
     {
       InputStream in = getMethodIndex().getStream();
       ByteArrayOutputStream out = new ByteArrayOutputStream();
       File xslFile = new File(reposHomeDir, "access/objectMethods.xslt");
       TransformerFactory factory = TransformerFactory.newInstance();
       Templates template = factory.newTemplates(new StreamSource(xslFile));
       Transformer transformer = template.newTransformer();
       Properties details = template.getOutputProperties();
       transformer.transform(new StreamSource(in), new StreamResult(out));
       in = new ByteArrayInputStream(out.toByteArray());
       return new MIMETypedStream("text/html", in);
     } catch (TransformerException e)
     {
       throw new DisseminationException("[DefaultBehaviorImpl] had an error "
           + "in transforming xml for viewItemIndex. "
           + "Underlying exception was: "
           + e.getMessage());
     }
   }
 
   /**
    * <p>Returns the Item Index for the object.  The Item
    * Index is a list of all datastreams in the object.  The datastream items
    * can be data or metadata.  The Item Index is returned as XML
    * encoded to the objectItemIndex.xsd schema.</p>
    * @return
    * @throws ServerException
    */
   public MIMETypedStream getItemIndex() throws ServerException
   {
     Date versDate = DateUtility.convertCalendarToDate(asOfDateTime);
     InputStream is = null;
     try{
     is = new ByteArrayInputStream(
       new DatastreamsAsXML().getItemIndex(
       reposBaseURL, reader, versDate).getBytes("UTF-8"));
     } catch (Exception e) {}
     return new MIMETypedStream("text/xml", is);
   }
 
   /**
    * <p>Returns an HTML rendering of the Item Index for the object.  The Item
    * Index is a list of all datastreams in the object.  The datastream items
    * can be data or metadata.  The Item Index is returned as HTML in a
    * presentation-oriented format.  This is accomplished by doing an XSLT
    * transform on the XML that is obtained from getItemIndex.</p>
    * @return
    * @throws ServerException
    */
   public MIMETypedStream viewItemIndex() throws ServerException
   {
     try
     {
       InputStream in = getItemIndex().getStream();
       ByteArrayOutputStream out = new ByteArrayOutputStream();
       File xslFile = new File(reposHomeDir, "access/viewItemIndex.xslt");
       TransformerFactory factory = TransformerFactory.newInstance();
       Templates template = factory.newTemplates(new StreamSource(xslFile));
       Transformer transformer = template.newTransformer();
       Properties details = template.getOutputProperties();
       transformer.transform(new StreamSource(in), new StreamResult(out));
       in = new ByteArrayInputStream(out.toByteArray());
       return new MIMETypedStream("text/html", in);
     } catch (TransformerException e)
     {
       throw new DisseminationException("[DefaultBehaviorImpl] had an error "
           + "in transforming xml for viewItemIndex. "
           + "Underlying exception was: "
           + e.getMessage());
     }
   }
 
   /**
    * <p>Returns an particular item (datastream) in the object.
    * The item will by a mime-typed stream of bytes.</p>
    *
    * @param  itemID  the unique identifier for the item in
    * the object in the form of a DatastreamID.  The
    * item identifer can be discovered via the results of the
    * getItemIndex or viewItemIndex methods.
    * @return
    * @throws ServerException
    */
   public MIMETypedStream getItem(String itemID) throws ServerException
   {
     Date versDate = DateUtility.convertCalendarToDate(asOfDateTime);
     Datastream ds = reader.GetDatastream(itemID, versDate);
     InputStream in = null;
     if (ds.DSControlGrp.equalsIgnoreCase("R"))
     {
       String dsLocation = ds.DSLocation;
       try
       {
         in = new ByteArrayInputStream(dsLocation.getBytes("UTF-8"));
       } catch (UnsupportedEncodingException uee)
       {
         // Can never occur since Java fully supports UTF-8
       }
       return new MIMETypedStream("application/fedora-redirect",in);
     } else if ( ds.DSState.equals("D") &&
                 ( context.get("canUseDeletedDatastream")==null
                   || (!context.get("canUseDeletedDatastream").equals("true")) )
               )
     {
       // Datastream has been flagged for deletion so it is no longer accessible.
       StringBuffer deleteText = new StringBuffer();
       deleteText.append("<html><title>Datastream Flagged For Deletion</title>");
       deleteText.append("<body>");
       deleteText.append("<center>");
       deleteText.append("<table width=\"784\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
       deleteText.append("<tr><td width=\"141\" height=\"134\" valign=\"top\">");
       deleteText.append("<img src=\"/images/newlogo2.jpg\" width=\"141\" height=\"134\"></td>");
       deleteText.append("<td width=\"643\" valign=\"top\">");
       deleteText.append("<center>");
       deleteText.append("<h2>Fedora Digital Object</h2>");
       deleteText.append("<h3>Default Disseminator - <font color=\"red\">Error Page</font></h3>");
       deleteText.append("<h4><font color=\"red\">");
       deleteText.append("The requested Datastream has been flagged for DELETION by the Repository Administrator.<br>");
       deleteText.append("This Datastream can no longer be disseminated: </font></h4>");
       deleteText.append("</center>");
       deleteText.append("</td></tr>");
       deleteText.append("</table>");
       deleteText.append("<hr><font size=\"+1\" color=\"blue\">Object Identifier (PID):</font>");
       deleteText.append("<font size=\"+1\">" + reader.GetObjectPID() + "</font><hr>");
       deleteText.append("<p></p>");
       deleteText.append("<table width=\"784\" border=\"1\" cellspacing=\"5\" cellpadding=\"5\" bgcolor=\"silver\">");
       deleteText.append("<tr><td><b>DatastreamID</b></td><td>" + ds.DatastreamID + "</td></tr>");
       deleteText.append("<tr><td><b>DatastreamVersionID</b></td><td>" + ds.DSVersionID + "</td></tr>");
       deleteText.append("<tr><td><b>DatastreamLabel</b></td><td>" + ds.DSLabel + "</td></tr>");
       deleteText.append("<tr><td><b>DatastreamControlGroupType</b></td><td>" + ds.DSControlGrp + "</td></tr>");
       deleteText.append("<tr><td><b>DatastreamState</td></b><td>" + ds.DSState + "</td></tr>");
       deleteText.append("<tr><td><b>DatastreamMIMEType</b></td><td>" + ds.DSMIME + "</td></tr>");
       deleteText.append("</center></table>");
       deleteText.append("</body></html>");
       try
       {
         in = new ByteArrayInputStream(deleteText.toString().getBytes("UTF-8"));
       } catch (UnsupportedEncodingException uee)
       {
         // Can never occur since Java fully supports UTF-8
       }
       return new MIMETypedStream("text/html",in);
 
     } else if ( ds.DSState.equals("I") &&
                 ( context.get("canUseInactiveDatastream")==null
                   || (!context.get("canUseInactiveDatastream").equals("true")) )
               )
     {
       // Datastream has been made inactive so it is no longer accessible.
       StringBuffer inactiveText = new StringBuffer();
       inactiveText.append("<html><title>Datastream Inactive</title>");
       inactiveText.append("<body>");
       inactiveText.append("<center>");
       inactiveText.append("<table width=\"784\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
       inactiveText.append("<tr><td width=\"141\" height=\"134\" valign=\"top\">");
       inactiveText.append("<img src=\"/images/newlogo2.jpg\" width=\"141\" height=\"134\"></td>");
       inactiveText.append("<td width=\"643\" valign=\"top\">");
       inactiveText.append("<center>");
       inactiveText.append("<h2>Fedora Digital Object</h2>");
       inactiveText.append("<h3>Default Disseminator - <font color=\"red\">Error Page</font></h3>");
       inactiveText.append("<h4><font color=\"red\">");
       inactiveText.append("The requested Datastream is INACTIVE.<br>");
       inactiveText.append("This Datastream can no longer be disseminated: </font></h4>");
       inactiveText.append("</center>");
       inactiveText.append("</td></tr>");
       inactiveText.append("</table>");
       inactiveText.append("<hr><font size=\"+1\" color=\"blue\">Object Identifier (PID):</font>");
       inactiveText.append("<font size=\"+1\">" + reader.GetObjectPID() + "</font><hr>");
       inactiveText.append("<p></p>");
       inactiveText.append("<table width=\"784\" border=\"1\" cellspacing=\"5\" cellpadding=\"5\" bgcolor=\"silver\">");
       inactiveText.append("<tr><td><b>DatastreamID</b></td><td>" + ds.DatastreamID + "</td></tr>");
       inactiveText.append("<tr><td><b>DatastreamVersionID</b></td><td>" + ds.DSVersionID + "</td></tr>");
       inactiveText.append("<tr><td><b>DatastreamLabel</b></td><td>" + ds.DSLabel + "</td></tr>");
       inactiveText.append("<tr><td><b>DatastreamControlGroupType</b></td><td>" + ds.DSControlGrp + "</td></tr>");
       inactiveText.append("<tr><td><b>DatastreamState</td></b><td>" + ds.DSState + "</td></tr>");
       inactiveText.append("<tr><td><b>DatastreamMIMEType</b></td><td>" + ds.DSMIME + "</td></tr>");
       inactiveText.append("</center></table>");
       inactiveText.append("</body></html>");
       try
       {
         in = new ByteArrayInputStream(inactiveText.toString().getBytes("UTF-8"));
       } catch (UnsupportedEncodingException uee)
       {
         // Can never occur since Java fully supports UTF-8
       }
       return new MIMETypedStream("text/html",in);
 
     } else
     {
       int byteStream = 0;
       try
       {
         in = ds.getContentStream();
 
       } catch (Exception e)
         {
           StringBuffer errorText = new StringBuffer();
           errorText.append("<html><title>Datastream Flagged For Deletion</title>");
           errorText.append("<body>");
           errorText.append("<center>");
           errorText.append("<table width=\"784\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\">");
           errorText.append("<tr><td width=\"141\" height=\"134\" valign=\"top\">");
           errorText.append("<img src=\"/images/newlogo2.jpg\" width=\"141\" height=\"134\"></td>");
           errorText.append("<td width=\"643\" valign=\"top\">");
           errorText.append("<center>");
           errorText.append("<h2>Fedora Digital Object</h2>");
           errorText.append("<h3>Default Disseminator - <font color=\"red\">Error Page</font></h3>");
           errorText.append("<h4><font color=\"red\">");
           errorText.append("The requested external Datastream cannot be accessed.<br>");
           errorText.append("</font></h4>");
           errorText.append("</center>");
           errorText.append("</td></tr>");
           errorText.append("</table>");
           errorText.append("<hr><font size=\"+1\" color=\"blue\">Object Identifier (PID):</font>");
           errorText.append("<font size=\"+1\">" + reader.GetObjectPID() + "</font><hr>");
           errorText.append("<p></p>");
           errorText.append("<table width=\"784\" border=\"1\" cellspacing=\"5\" cellpadding=\"5\" bgcolor=\"silver\">");
           errorText.append("<tr><td><b>DatastreamLocation</b></td><td>" + ds.DSLocation + "</td></tr>");
           errorText.append("<tr><td><b>DatastreamVersionID</b></td><td>" + ds.DSVersionID + "</td></tr>");
           errorText.append("<tr><td><b>DatastreamLabel</b></td><td>" + ds.DSLabel + "</td></tr>");
           errorText.append("<tr><td><b>DatastreamControlGroupType</b></td><td>" + ds.DSControlGrp + "</td></tr>");
           errorText.append("<tr><td><b>DatastreamState</td></b><td>" + ds.DSState + "</td></tr>");
           errorText.append("<tr><td><b>DatastreamMIMEType</b></td><td>" + ds.DSMIME + "</td></tr>");
           errorText.append("<tr><td><b>Reason for Failure</b></td><td>" + e.getMessage() + "</td></tr>");
           errorText.append("</center></table>");
           errorText.append("</body></html>");
           try
           {
             in = new ByteArrayInputStream(errorText.toString().getBytes("UTF-8"));
           } catch (UnsupportedEncodingException uee)
           {
             // Can never occur since Java fully supports UTF-8
           }
           return new MIMETypedStream("text/html",in);
         }
         return new MIMETypedStream(ds.DSMIME, in);
     }
   }
 
   /**
    * <p>Returns the Dublin Core record for the object, if one exists.
    * The record is returned as XML encoded to the oaidc.xsd schema.</p>
    * @return
    * @throws ServerException
    */
   public MIMETypedStream getDublinCore() throws ServerException
   {
     Date versDate = DateUtility.convertCalendarToDate(asOfDateTime);
     DatastreamXMLMetadata dcmd = null;
     InputStream is = null;
     try
     {
         dcmd = (DatastreamXMLMetadata) reader.GetDatastream("DC", versDate);
         is = new ByteArrayInputStream(
              new ObjectInfoAsXML().getOAIDublinCore(dcmd).getBytes("UTF-8"));
     } catch (ClassCastException cce)
     {
         throw new ObjectIntegrityException("Object " + reader.GetObjectPID()
             + " has a DC datastream, but it's not inline XML.");
 
     } catch (UnsupportedEncodingException uee)
     {
         throw new GeneralException("[DefaultBehaviorImpl] An error has occurred. "
             + "The error was a \"" + uee.getClass().getName() + "\"  . The "
             + "Reason was \"" + uee.getMessage() + "\"  .");
     }
     return new MIMETypedStream("text/xml", is);
   }
 
   /**
    * <p>Returns the Dublin Core record for the object, if one exists.
    * The record is returned as HTML in a presentation-oriented format.</p>
    * @return
    * @throws ServerException
    */
   public MIMETypedStream viewDublinCore() throws ServerException
   {
     try
     {
       InputStream in = getDublinCore().getStream();
       ByteArrayOutputStream out = new ByteArrayOutputStream();
       File xslFile = new File(reposHomeDir, "access/viewDublinCore.xslt");
       TransformerFactory factory = TransformerFactory.newInstance();
       Templates template = factory.newTemplates(new StreamSource(xslFile));
       Transformer transformer = template.newTransformer();
       Properties details = template.getOutputProperties();
       transformer.transform(new StreamSource(in), new StreamResult(out));
       in = new ByteArrayInputStream(out.toByteArray());
       return new MIMETypedStream("text/html", in);
     } catch (TransformerException e)
     {
       throw new DisseminationException("[DefaultBehaviorImpl] had an error "
           + "in transforming xml for viewDublinCore. "
           + "Underlying exception was: "
           + e.getMessage());
     }
   }
 
   /**
    * Method implementation of reflectMethods from the InternalService interface.
    * This will return an array of method definitions that constitute the
    * official default behaviors a Fedora object.  These will be the methods
    * promulgated by the DefaultBehavior interface, which is the behavior
    * definition contract for the DefaultBehavior internal service.
    * Like any other behavior mechanism, an internal service mechanism must
    * publish the service methods it implements.
    * @return
    */
   public static MethodDef[] reflectMethods()
   {
     // FIXIT!! For now, I am just HACKING in the method defs for some
     // quick testing. To properly get the default behavior defintion,
     // we can either do java reflection on the interface DefaultBehavior
     // (see technique in original Fedora class cornell.SigGen.Lecture.java)
     // or else we can configure a Method Map XML file with the DynamicAccess
     // module.  The later solution is better since java reflection can't
     // provide all information we want about parameters (e.g., parm names).
     ArrayList methodList = new ArrayList();
     MethodDef method = null;
 
     method = new MethodDef();
     method.methodName = "getObjectProfile";
     method.methodLabel = "Get XML-encoded description of the object";
     method.methodParms = new MethodParmDef[0];
     methodList.add(method);
 
     method = new MethodDef();
     method.methodName = "viewObjectProfile";
     method.methodLabel = "View description of the object";
     method.methodParms = new MethodParmDef[0];
     methodList.add(method);
 
     method = new MethodDef();
     method.methodName = "getMethodIndex";
     method.methodLabel = "Get an XML-encoded list of dissemination methods for the object";
     method.methodParms = new MethodParmDef[0];
     methodList.add(method);
 
     method = new MethodDef();
     method.methodName = "viewMethodIndex";
     method.methodLabel = "View a list of dissemination methods in the object";
     method.methodParms = new MethodParmDef[0];
     methodList.add(method);
 
     method = new MethodDef();
     method.methodName = "getItemIndex";
     method.methodLabel = "Get an XML-encoded list of items in the object";
     method.methodParms = new MethodParmDef[0];
     methodList.add(method);
 
     method = new MethodDef();
     method.methodName = "viewItemIndex";
     method.methodLabel = "View a list of items in the object";
     method.methodParms = new MethodParmDef[0];
     methodList.add(method);
 
     method = new MethodDef();
     method.methodName = "getItem";
     method.methodLabel = "Get an item from the object";
     method.methodParms = new MethodParmDef[1];
     method.methodParms[0] = new MethodParmDef();
     method.methodParms[0].parmName = "itemID";
     method.methodParms[0].parmType = MethodParmDef.USER_INPUT;
     method.methodParms[0].parmLabel = "Item identifier in format of datastream ID";
     method.methodParms[0].parmRequired = true;
     method.methodParms[0].parmPassBy = MethodParmDef.PASS_BY_VALUE;
     method.methodParms[0].parmDefaultValue = null;
     method.methodParms[0].parmDomainValues = new String[0];
     methodList.add(method);
 
     method = new MethodDef();
     method.methodName = "getDublinCore";
     method.methodLabel = "Get an XML-encoded Dublin Core record for the object";
     method.methodParms = new MethodParmDef[0];
     methodList.add(method);
 
     method = new MethodDef();
     method.methodName = "viewDublinCore";
     method.methodLabel = "View the Dublin Core record for the object";
     method.methodParms = new MethodParmDef[0];
     methodList.add(method);
     return (MethodDef[])methodList.toArray(new MethodDef[0]);
   }
 }
