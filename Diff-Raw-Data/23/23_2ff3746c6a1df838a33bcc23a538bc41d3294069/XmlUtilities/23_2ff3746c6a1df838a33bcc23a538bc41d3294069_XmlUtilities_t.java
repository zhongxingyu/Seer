 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.db.xmlutil;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.jbundle.base.db.BaseTable;
 import org.jbundle.base.db.Record;
 import org.jbundle.base.field.BaseField;
 import org.jbundle.base.field.CounterField;
 import org.jbundle.base.field.DateField;
 import org.jbundle.base.field.DateTimeField;
 import org.jbundle.base.field.HtmlField;
 import org.jbundle.base.field.ObjectField;
 import org.jbundle.base.field.TimeField;
 import org.jbundle.base.field.UnusedField;
 import org.jbundle.base.field.XMLPropertiesField;
 import org.jbundle.base.field.XmlField;
 import org.jbundle.base.model.DBConstants;
 import org.jbundle.base.model.Utility;
 import org.jbundle.base.model.XMLTags;
 import org.jbundle.model.DBException;
 import org.jbundle.thin.base.db.Constants;
 import org.jbundle.thin.base.util.base64.Base64;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 
 /**
  * This utility is used to import and export to XML file(s).
  * The import utility is very flexible, including the capability to import multi-level
  * records including sub-records and fields referencing main records.
  * <p>This class also has the capability to be run alone as a utility for imports only.
  * If you want a standalone utility for exports, use the class "ExportRecordsToXMLProcess."
  * <pre>
  * The params to pass in are:
  * filename - the name of the target XML file to import.
  * import - the name of the target XML file to import.
  * export - the name of the target XML file to export.
  * overwritedups - true if you want to overwrite duplicate records in import.
  * </pre>
  */
 public class XmlUtilities extends Object
 {
     public final static String XML_ENCODING = "UTF-8";
     public final static String XML_LEAD_LINE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
     
     public final static String TEXT = "TEXT";
     public final static String CDATA = "CDATA";
     public final static String XML = "XML";
 
     public static final String TYPE_PARAM = "type";
     public static final String XML_VALUE = "XML";
     
     public static final String NEWLINE = "\n";
     public static final String RECORDTAB = "\t";
     public static final String FIELDTAB = "\t\t";
 
     public static final String TEMP_TAG_NAME = "temporary-tag";
     public static final String START_TEMP_TAG = "<" + TEMP_TAG_NAME + ">";
     public static final String END_TEMP_TAG = "</" + TEMP_TAG_NAME + ">";
 
     /**
      * The parameter for the encoding attribute.
      */
     public static final String ENCTYPE_PARAM = "enctype";
     /**
      * The default encoding for raw data fields.
      */
     public static final String BINARYENCODING = "base64"; //? "base64", "quoted-printable", "7bit", "8bit", and "binary". "uuencode"
     /**
      * The root tag for records.
      */
     public static final String ROOT_TAG = "record";
 
     /**
      * Export this table.
      * @record The record to export.
      * @strFileName The distination filename (deleted the old copy if this file exists).
      */
     public static Document exportFileToDoc(BaseTable table)
     {
         DocumentBuilder db = Utility.getDocumentBuilder();
         DocumentBuilder stringdb = Utility.getDocumentBuilder();
         
         Document doc = null;
         synchronized (db)
         {
             doc = db.newDocument();
             Element elRoot = (Element)doc.createElement(XMLTags.FILE);
             doc.appendChild(elRoot);
         
             exportFileToDOM(stringdb, table, doc, elRoot);
         }
 
         return doc;
     }
     /**
      * Export this table.
      * @record The record to export.
      * @strFileName The distination filename (deleted the old copy if this file exists).
      */
     public static void exportFileToDOM(DocumentBuilder stringdb, BaseTable table, Document doc, Element elRoot)
     {
         try   {
             table.close();
             while (table.hasNext())
             {
             	table.next();
             	Record record = table.getRecord();
                 XmlUtilities.createXMLRecord(stringdb, record, doc, elRoot);
             }
             elRoot.appendChild(doc.createTextNode(NEWLINE));
         } catch (DBException ex)    {
             ex.printStackTrace();
             System.exit(0);
         }
     }
     /**
      * Write out this record as an dom object.
      * @param stringdb The document builder that gives you access to a parser.
      * @param record The record to convert to an XML dom record.
      * @param document The XML dom document, giving you access to createElement methods.
      * @param elRoot The root element to add this XML record to.
      */
     public static void createXMLRecord(DocumentBuilder stringdb, Record record, Document doc, Element elRoot)
         throws DBException
     {
         CounterField fldCounter = (CounterField)record.getCounterField();
         Element elRecord = null;
         Element elField = null;
         elRecord = doc.createElement(record.getTableNames(false));
         elRoot.appendChild(doc.createTextNode(NEWLINE + RECORDTAB));
         elRoot.appendChild(elRecord);
         String strObjectIDName = DBConstants.STRING_OBJECT_ID_HANDLE;
         String strObjectID = null;
         Object objID = record.getHandle(DBConstants.OBJECT_ID_HANDLE);
         if (fldCounter != null)
         {
             strObjectIDName = fldCounter.getFieldName();
             if (objID == null)  // ie., for a non-persistent object
                 objID = record.getHandle(DBConstants.BOOKMARK_HANDLE);
         }
         if (objID != null)
             strObjectID = objID.toString();
         if (strObjectID != null)
             if (fldCounter == record.getField(0))
                 elRecord.setAttribute(strObjectIDName, strObjectID);
         for (int i = 0; i < record.getFieldCount(); i++)
         {
             BaseField field = record.getField(i);
             if (field == fldCounter) if (strObjectID != null)
                 continue; // Not necessary to add counter field
             if (field instanceof UnusedField)
                 continue;   // Don't add this.
             elRecord.appendChild(doc.createTextNode(NEWLINE + FIELDTAB));
             elRecord.appendChild(elField = doc.createElement(field.getFieldName()));
             if (!field.isNull())
             {
                 String string = field.toString();
                 String type = TEXT;
                 if (field instanceof ObjectField)
                 {
                     string = XmlUtilities.encodeFieldData(field);
                     type = CDATA;
                     if ((string != null) && (string.length() > 0))
                         elField.setAttribute(ENCTYPE_PARAM, BINARYENCODING);
                 }
                 else if (field instanceof DateTimeField)
                 {
                 	string = XmlUtilities.encodeDateTime((DateTimeField)field);
                 }
                 else if ((field instanceof XmlField)
                     || (field instanceof XMLPropertiesField)
                     || (field instanceof HtmlField))
                 {
                     if ((string != null)
                         && (string.length() > 0))
                     {
                         type = XML;
                         String strXML = string;
                         boolean bNoRootNode = true;
                         if ((strXML.length() < 5)
                             || (!strXML.substring(0, 5).equalsIgnoreCase(XML_LEAD_LINE.substring(0, 5))))
                                 strXML = XML_LEAD_LINE + START_TEMP_TAG + strXML + END_TEMP_TAG;    // Always
                         else
                             bNoRootNode = false;
                         InputSource is = new InputSource(new StringReader(strXML));
                         Document fieldDoc = null;
                         boolean bError = false;
                         try {
                             fieldDoc = stringdb.parse(is);
                         } catch (SAXException ex) {
                             Utility.getLogger().info("Fallback to CData");
                             bError = true;
                         } catch (IOException ex) {
                             ex.printStackTrace();
                             bError = true;
                         }
                         if (!bError)
                         {   // Add the child nodes to this node (with type="XML")
                             Node element = fieldDoc.getDocumentElement();
                             Node node = doc.importNode(element, true);
                             int iCount = 0;
                             if (bNoRootNode)
                             {
                                 while (node.hasChildNodes())
                                 {
                                     Node child = node.getFirstChild();
                                     node.removeChild(child);
                                     elField.appendChild(child);
                                     iCount++;
                                     if (child.getNodeType() != Node.TEXT_NODE)
                                         iCount++;
                                 }
                             }
                             else
                             {
                                 elField.appendChild(node);
                                 iCount = 20;    // XML type
                             }
                             if (iCount > 1)     // For everthing except a single text node type=xml
                                 elField.setAttribute(TYPE_PARAM, XML_VALUE);    // A text field would have 1 element (not XML).
                         }
                         else
                         {
                             type = TEXT;
                             if (Utility.isCData(string))
                                 type = CDATA; // If not well-formed, just pass the string as CData.
                         }
                     }
                 }
                 else if (Utility.isCData(string))
                         type = CDATA;
                 if (type == TEXT)
                     elField.appendChild(doc.createTextNode(string));
                 else if (type == CDATA)
                     elField.appendChild(doc.createCDATASection(string));
             }
         }
         elRecord.appendChild(doc.createTextNode(NEWLINE + RECORDTAB));
     }
     
     public static SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
     public static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
     public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
     /**
      * Encode date time field.
      * @param field
      * @return
      */
     public static String encodeDateTime(DateTimeField field)
     {
 		Date date = ((DateTimeField)field).getDateTime();
 		if (date == null)
 			return null;
     	if (field instanceof TimeField)
     		return timeFormat.format(date);
     	else if (field instanceof DateField)
     		return dateFormat.format(date);
     	else // if (field instanceof DateTimeField)
     		return dateTimeFormat.format(date);
     }
     /**
      * Decode date time value and set the field value.
      * @param field
      * @return
      */
     public static int decodeDateTime(DateTimeField field, String strValue)
     {
 		Date date = null;
     	try {
     		if (strValue != null)
     		{
 		        if (field instanceof TimeField)
 		        	date = timeFormat.parse(strValue);
 		        else if (field instanceof DateField)
 		        	date = dateFormat.parse(strValue);
 		        else // if (field instanceof DateTimeField)
 		        	date = dateTimeFormat.parse(strValue);
     		}
         } catch (ParseException e) {
 	        e.printStackTrace();
 	        return DBConstants.ERROR_RETURN;
         }
         return field.setDateTime(date, Constants.DISPLAY, Constants.SCREEN_MOVE);
     }
     /**
      * Convert the current record to an XML String.
      * Careful, as this method just returns the XML fragment (with header or a top-level tag).
      * @param The record to convert.
      * @return The XML representation of this record.
      */
     public static String createXMLStringRecord(Record record)
     {
         String string = DBConstants.BLANK;
 
         DocumentBuilder db = Utility.getDocumentBuilder();
         DocumentBuilder stringdb = Utility.getDocumentBuilder();
         Document doc = null;
         synchronized (db)
         {
             doc = db.newDocument();
 
             Element elRoot = (Element)doc.createElement(ROOT_TAG);
             doc.appendChild(elRoot);
             try   {
                 XmlInOut.enableAllBehaviors(record, false, true); // Disable file behaviors
     
                 createXMLRecord(stringdb, record, doc, elRoot);
     
                 elRoot.appendChild(doc.createTextNode(XmlUtilities.NEWLINE));
             } catch (DBException ex)    {
                 ex.printStackTrace();
                 return DBConstants.BLANK;
             }
         }
         try   {
             StringWriter out = new StringWriter();  //, MIME2Java.convert("UTF-8"));
 
             // Use a Transformer for output
             TransformerFactory tFactory = TransformerFactory.newInstance();
             Transformer transformer = tFactory.newTransformer();
 
             DOMSource source = new DOMSource(doc);
             StreamResult result = new StreamResult(out);
             transformer.transform(source, result);
             out.close();
             string = out.toString();
 
             int iStartString = string.indexOf("<" + ROOT_TAG + ">") + 3 + ROOT_TAG.length();
             int iEndString = string.indexOf("</" + ROOT_TAG + ">");
             if (iStartString >= iEndString)
                 string = DBConstants.BLANK;
             else
                 string = string.substring(iStartString, iEndString);
         } catch (TransformerConfigurationException tce) {
             // Error generated by the parser
             tce.printStackTrace();
         } catch (TransformerException te) {
             // Error generated by the parser
             te.printStackTrace();
         } catch (Exception ex)  {
             ex.printStackTrace();
         }
         XmlInOut.enableAllBehaviors(record, true, true);
         return string;
     }
     /**
      * Change the data in this field to base64.
      * WARNING - This requires 64bit encoding found in javax.mail!
      * @param field The field containing the binary data to encode.
      * @return The string encoded using base64.
      */
     public static String encodeFieldData(BaseField field)
     {
         if (field.getData() == null)
             return DBConstants.BLANK;
         ByteArrayOutputStream baOut = new ByteArrayOutputStream();
         DataOutputStream daOut = new DataOutputStream(baOut);
         try {
             field.write(daOut, false);
             daOut.flush();
         } catch (IOException e) {
             e.printStackTrace();
         }
         char[] chOut = Base64.encode(baOut.toByteArray());
         if (chOut == null)
             return DBConstants.BLANK;   // Never
         return new String(chOut);
     }
     /**
      * Change this base64 string to raw data and set the value in this field.
      * WARNING - This requires 64bit encoding found in javax.mail!
      * @param field The field containing the binary data to decode.
      * @param The string to decode using base64.
      */
     public static void decodeFieldData(BaseField field, String string)
     {
         if ((string == null) || (string.length() == 0))
             return;
         byte[] ba = Base64.decode(string.toCharArray());
         InputStream is = new ByteArrayInputStream(ba);
         DataInputStream daIn = new DataInputStream(is);
         field.read(daIn, false);
     }
 }
