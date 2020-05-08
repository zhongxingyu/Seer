 /**
  * JAFER Toolkit Project.
  * Copyright (C) 2002, JAFER Toolkit Project, Oxford University.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  *
  *
  */
 
 /**
  *  Title: JAFER Toolkit
  *  Description:
  *  Copyright: Copyright (c) 2001
  *  Company: Oxford University
  *
  *@author     Antony Corfield; Matthew Dovey; Colin Tatham
  *@version    1.0
  */
 
 package org.jafer.record;
 
 import org.jafer.util.Config;
 import org.jafer.exception.JaferException;
 
 import asn1.BEREncoding;
 import asn1.ASN1External;
 import asn1.ASN1OctetString;
 import asn1.ASN1Exception;
 
 import java.io.IOException;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 
 // Imported DOM classes
 import org.w3c.dom.Node;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.NamedNodeMap;
 import java.io.*;
 
 /**
  * <p>Serializes MARC record to XML (http://www.openarchives.org/OAI/1.1/oai_marc schema).
  * Method getXML returns record node (created from supplied DOM document);
  * method getBER builds ASN1External from XML and returns a BER object.
  * Transformations etc. are managed by org.jafer.record.RecordFactory</p>
  * @author Antony Corfield; Matthew Dovey; Colin Tatham
  * @version 1.0
  */
 public class MARCRecord extends DataObject {
 
   public static final  char RECORD_DELIMITER   = (char)Integer.parseInt("1D", 16);
   public static final  char FIELD_DELIMITER    = (char)Integer.parseInt("1E", 16);
   public static final  char SUBFIELD_DELIMITER = (char)Integer.parseInt("1F", 16);
   private static final char MARC8_ENCODING     = (char)Integer.parseInt("20", 16);
   private static final char UTF8_ENCODING      = (char)Integer.parseInt("61", 16);
 
   public static final String MARC8 = "US-ASCII";
   public static final String UTF8  = "UTF-8";
   public static final String OAI_NAMESPACE     = "http://www.openarchives.org/OAI/1.1/oai_marc";
   public static final String[] OAI_ATTRIBUTE_NAMES = new String[] {
     /*  5   optional */ "status",
     /*  6   required */ "type",
     /*  7   required */ "level",
     /*  8   optional */ "ctlType",
     /*  9   optional */ "charEnc",
     /*  17  optional */ "encLvl",
     /*  18  optional */ "catForm",
     /*  19  optional */ "lrRqrd",
   };
 
   private Node root;
   private BEREncoding ber;
   private MARC8Unicode marc8Unicode;
   private char encoding;
 
   public MARCRecord(String dbName, BEREncoding ber) {
 
     super(dbName, ber);
     this.ber = ber;
     marc8Unicode = new MARC8Unicode();
   }
 
   public MARCRecord(String dbName, Node root) {
 
     super(dbName, root);
     this.root = root.getFirstChild();
     marc8Unicode = new MARC8Unicode();
   }
 
   private void setEncoding(String encoding) {
 
     this.encoding = encoding.equalsIgnoreCase(UTF8) ? UTF8_ENCODING : MARC8_ENCODING;
   }
 
   private void setEncoding(char encoding) {
 
     this.encoding = encoding;
   }
 
   private String getEncodingAsString() {
 
     return getEncoding() == UTF8_ENCODING ? UTF8 : MARC8;
   }
 
   private char getEncoding() {
 
     return encoding;
   }
 
   public Node getXML(Document document) throws RecordException {
 
     if (document == null)
         return null;
 
     if (root != null) {
       if (document.equals(root.getOwnerDocument()))
         return root;
       else
         return document.importNode(root, true);
     }
 
     if (this.ber == null)
         return null;
 
     String message;
     this.root = document.createElementNS(OAI_NAMESPACE, "oai_marc");
 //    this.root = document.createElement("oai_marc");
     ((Element)root).setAttribute("xmlns", OAI_NAMESPACE);
     ((Element)root).setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
    ((Element)root).setAttribute("xsi:schemaLocation", OAI_NAMESPACE +
               " http://www.openarchives.org/OAI/1.1/oai_marc.xsd");
 
 
     ASN1External asn1External = null;
     try{
       asn1External = new ASN1External(this.ber, true);
       byte[] bytes = asn1External.c_octetAligned.get_bytes();
       setEncoding((char)bytes[9]);
       int recordLength = getInt(bytes, 0, 5);
       int baseAddress = getInt(bytes, 12, 5);
       int numFields = (baseAddress - 1 - 24) / 12;
 
       for (int i = 5; i <= 19; i++) {
         if (getOAIAttributeName(i) != "" && bytes[i] != 32)
 //          ((Element)root).setAttributeNS(OAI_NAMESPACE, getOAIAttributeName(i), getString(bytes, i, 1));
           ((Element)root).setAttribute(getOAIAttributeName(i), getString(bytes, i, 1));
       }
 
       processRecord(document, bytes, baseAddress, numFields);
 
     } catch (ASN1Exception e) {
       message = "ASN1Exception processing MARCRecord; " + e.toString();
       throw new RecordException(message, e);
     } catch (NullPointerException e) {
       message = "NullPointerException processing MARCRecord; " + e.toString();
       throw new RecordException(message, e);
 //    } catch (UnsupportedEncodingException e) {
 //      message = "UnsupportedEncodingException processing MARCRecord; " + e.toString();
 //      throw new RecordException(message, e);
     } catch (IOException e) {
       message = "IOException processing MARCRecord; " + e.toString();
       throw new RecordException(message, e);
     } catch (JaferException e) {
       message = "JaferException processing MARCRecord; " + e.toString();
       throw new RecordException(message, e);
     }
 
     return root;
   }
 
   private void processRecord(Document document, byte[] bytes, int baseAddress, int numFields) throws IOException, JaferException {
 
     ByteArrayInputStream byteIn = null;
     Node field;
 
     for (int i = 0; i < numFields; i++) {
 
       int offset = i * 12 + 24;
       int len = getInt(bytes, offset + 3, 4);
       int start = getInt(bytes, offset + 7, 5);
       String tag = getJavaIdentifierTag(getString(bytes, offset, 3));
       byteIn = new ByteArrayInputStream(bytes,
                     baseAddress + start, len - 1);
 
      if (isFixfield(tag))
         field = document.createElementNS(OAI_NAMESPACE, "fixfield");
 //	field = document.createElement("fixfield");
       else
         field = document.createElementNS(OAI_NAMESPACE, "varfield");
 //	field = document.createElement("varfield");
 
 //      ((Element)field).setAttributeNS(OAI_NAMESPACE, "id", tag);
       ((Element)field).setAttribute("id", tag);
       processField(document, field, byteIn);
       root.appendChild(field);
       byteIn.close();
     }
  }

	private boolean isFixfield(String tag)
	{
		try
		{
			return Integer.parseInt(tag) < 10;
		}
		catch( NumberFormatException nfe )
		{
			return false;
		}
	}
 
   private void processField(Document document, Node field, ByteArrayInputStream byteIn) throws IOException, JaferException {
 
     ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
     String ind1 = null, ind2 = null;
 
     int b;
     while((b = byteIn.read()) != -1) {
 
       if(b == SUBFIELD_DELIMITER) {
 
         if(ind1 == null && ind2 == null) {
           ind1 = getString(byteOut, 0, 1);
           ind2 = getString(byteOut, 1, 1);
         }
         else
           field.appendChild(getSubFieldElement(document, byteOut));
         byteOut.reset();
       }
       else byteOut.write(b);
     }
 
     if(ind1 != null && ind2 != null) {
       field.appendChild(getSubFieldElement(document, byteOut));
 //      ((Element)field).setAttributeNS(OAI_NAMESPACE, "i1", ind1);
 //      ((Element)field).setAttributeNS(OAI_NAMESPACE, "i2", ind2);
       ((Element)field).setAttribute("i1", ind1);
       ((Element)field).setAttribute("i2", ind2);
     }
     else
       field.appendChild(document.createTextNode("\""+getString(byteOut, 0)+"\""));
 
     byteOut.close();
   }
 
   private Node getSubFieldElement(Document document, ByteArrayOutputStream byteOut) throws IOException, JaferException {
 
     String tag = getJavaIdentifierTag(getString(byteOut, 0, 1));
     Element subfield = document.createElementNS(OAI_NAMESPACE, "subfield");
 //    Element subfield = document.createElement("subfield");
 //    ((Element)subfield).setAttributeNS(OAI_NAMESPACE, "label", tag);
     ((Element)subfield).setAttribute("label", tag);
     subfield.appendChild(document.createTextNode(getString(byteOut, 1)));
     return subfield;
   }
 
   private String getString(byte[] bytes, int offset, int len) {
 
     try {
       if (getEncoding() == MARC8_ENCODING) {
           return marc8Unicode.toUnicode(bytes, offset, len);
       }
       else /** @todo check this */ {
           return new String(bytes, offset, len, getEncodingAsString());
       }
     } catch (JaferException ex) {/** @todo handle exception */
       System.out.println("ERROR: " + ex.toString());
       return null;
     } catch (UnsupportedEncodingException ex) {/** @todo handle exception */
       System.out.println("ERROR: " + ex.toString());
       return null;
     }
   }
 
   private String getJavaIdentifierTag(String s) throws IOException {
 
     ByteArrayOutputStream out = new ByteArrayOutputStream();
 
     for(int i = 0; i < s.length(); i++)
       if(Character.isJavaIdentifierPart(s.charAt(i)))
         out.write(s.charAt(i));
     out.close();
 
     return out.toString(getEncodingAsString());
   }
 
   private String getString(ByteArrayOutputStream byteOut, int offset, int len) {
 
     return getString(byteOut.toByteArray(), offset, len);
   }
 
   private String getString(ByteArrayOutputStream byteOut, int offset) {
     return getString(byteOut.toByteArray(), offset, byteOut.size() - offset);
   }
 
   private int getInt(byte[] bytes, int offset, int len) {
 
     return Integer.parseInt(getString(bytes, offset, len));
   }
 
 ////////////////////////////////////////////////////////////////////////////////
 
   public BEREncoding getBER() throws RecordException {
 
     if (this.ber != null)
       return this.ber;
     if (root == null)
       return null;
 
     ByteArrayOutputStream leader = new ByteArrayOutputStream(24);
     ByteArrayOutputStream directory = new ByteArrayOutputStream();
     ByteArrayOutputStream fieldData = new ByteArrayOutputStream();
     ByteArrayOutputStream out = null;
     try {
       processRecord(directory, fieldData, root);
 
       int recordLength = 24 + directory.size() + fieldData.size() + 2;
       int baseAddress = 24 + directory.size() + 1;
       out = new ByteArrayOutputStream(recordLength);
 
       buildLeader(leader, root.getAttributes(), recordLength, baseAddress);
       leader.writeTo(out);
       leader.flush();
       leader.close();
 
       directory.writeTo(out);
       directory.flush();
       directory.close();
       out.write((byte)FIELD_DELIMITER);
 
       fieldData.writeTo(out);
       fieldData.flush();
       fieldData.close();
       out.write((byte)RECORD_DELIMITER);
 
       ASN1External asn1External = new ASN1External();
       asn1External.c_octetAligned = new ASN1OctetString(out.toByteArray());
       // set syntax
       asn1External.s_direct_reference = new asn1.ASN1ObjectIdentifier(super.getRecordSyntax());
       this.ber = asn1External.ber_encode();
     } catch (IOException e) {
 /** @todo handle exception */
       System.out.println(e.toString());
     } catch (JaferException e) {
 /** @todo handle exception */
       System.out.println(e.toString());
     } catch (ASN1Exception e) {
 /** @todo handle exception */
       // build diagnostic
       System.out.println(e.toString());
     }
     return this.ber;
   }
 
   private void processRecord(ByteArrayOutputStream directory, ByteArrayOutputStream fieldData, Node node) throws JaferException {
 
     int dataStartPos = 0;
 
     NodeList list = node.getChildNodes();
     for (int i = 0; i < list.getLength(); i++) {
       if (list.item(i).getNodeName().equals("fixfield"))
           dataStartPos = addFixField(directory, fieldData, dataStartPos, list.item(i));
       else if (list.item(i).getNodeName().equals("varfield"))
           dataStartPos = addVarField(directory, fieldData, dataStartPos, list.item(i));
     }
   }
 
   private int addFixField(ByteArrayOutputStream directory, ByteArrayOutputStream fieldData, int dataStartPos, Node fixField) throws JaferException {
 
     String tag = ((Element)fixField).getAttribute("id");
     int id = Integer.parseInt(tag);
     String data = getMARC8Value(fixField);
 
     //  fixfield is enclosed between quotes because spaces are meaningfull
     if (data.charAt(0) == '"' && data.charAt(data.length() - 1) == '"')
         data = data.substring(1, data.length() - 1);
 
     write(fieldData, data);
     fieldData.write((byte)FIELD_DELIMITER);
 
     write(directory, 3, id);
     write(directory, 4, data.length() + 1);
     write(directory, 5, dataStartPos);
     dataStartPos += data.length() + 1;
     return dataStartPos;
   }
 
   private int addVarField(ByteArrayOutputStream directory, ByteArrayOutputStream fieldData,
                                                         int dataStartPos, Node varfield) throws JaferException {
 
     String i1 = ((Element)varfield).getAttribute("i1");
     String i2 = ((Element)varfield).getAttribute("i2");
     String tag = ((Element)varfield).getAttribute("id");
 
     NodeList subfieldList = ((Element)varfield).getElementsByTagName("subfield");
 
     write(fieldData, i1);
     write(fieldData, i2);
     int len = 2;
     for (int i = 0; i < subfieldList.getLength(); i++) {
         String label = ((Element)subfieldList.item(i)).getAttribute("label");
         String value = getMARC8Value(subfieldList.item(i));
         fieldData.write((byte)SUBFIELD_DELIMITER);
         write(fieldData, label + value);
         len += (label + value).length() + 1;
     }
     fieldData.write((byte)FIELD_DELIMITER);
 
     write(directory, 3, Integer.parseInt(tag));
     write(directory, 4, len + 1);
     write(directory, 5, dataStartPos);
     dataStartPos += len + 1;
     return dataStartPos;
   }
 
   private void buildLeader(ByteArrayOutputStream leader, NamedNodeMap leaderAttributes,
                                                       int recordLength, int baseAddress) throws JaferException {
     /** @todo check defaults */
 
     write(leader, 5, recordLength);                                      //0-4   recordLength
     leader.write(getLeaderValue(leaderAttributes, 5, 'n'));              //5     Record Status: new
     leader.write(getLeaderValue(leaderAttributes, 6, 'a'));              //6     Record type: language microform
     leader.write(getLeaderValue(leaderAttributes, 7, 'm'));              //7     Bibligraphic level: m
     leader.write(getLeaderValue(leaderAttributes, 8, ' '));              //8     Type of control: none
     leader.write(getLeaderValue(leaderAttributes, 9, MARC8_ENCODING));   //9     Encoding
     leader.write('2');                                                   //10    indicator count
     leader.write('2');                                                   //11    subfield code count
     write(leader, 5, baseAddress);                                       //12-16 baseAddress
     leader.write(getLeaderValue(leaderAttributes, 17, '5'));             //17    Encoding level: partial
     leader.write(getLeaderValue(leaderAttributes, 18, ' '));             //18    descriptive cataloguing: none
     leader.write(getLeaderValue(leaderAttributes, 19, ' '));             //19    linked record requirement: none
     leader.write('4');                                                   //20    entry map
     leader.write('5');                                                   //21    entry map
     leader.write('0');                                                   //22    entry map (M)
     leader.write('0');                                                   //23    entry map
   }
 
   private char getLeaderValue(NamedNodeMap leaderAttributes, int leaderPos, char defaultValue) throws JaferException {
 
     Node n;
     char c;
 
     n = leaderAttributes.getNamedItem(getOAIAttributeName(leaderPos));
     if (n == null)
         c = defaultValue;
     else
         c = Config.getValue(n).charAt(0);
 
     return c;
   }
 
   private void write(ByteArrayOutputStream byteArray, int length, int value) {
 
     String s = Integer.toString(value);
     while (s.length() < length)
            s = "0" + s;
 
     for (int i = 0; i < length; i++)
         byteArray.write(s.charAt(i));
   }
 
   private void write(ByteArrayOutputStream byteArray, String data) {
 
     for (int i = 0; i < data.length(); i++)
         byteArray.write(data.charAt(i));
   }
 
   private String getMARC8Value(Node node) throws JaferException {
     return marc8Unicode.toMARC8(Config.getValue(node));
   }
 
   private static String getOAIAttributeName(int position) {
 
       if ((position >= 5 && position <= 9))
         return OAI_ATTRIBUTE_NAMES[position - 5];
 
       if ((position >= 17 && position <= 19))
         return OAI_ATTRIBUTE_NAMES[position - 12];
 
       return "";
   }
 }
