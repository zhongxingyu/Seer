 /*===========================================================================
   Copyright (C) 2009 by the Okapi Framework contributors
 -----------------------------------------------------------------------------
   This library is free software; you can redistribute it and/or modify it 
   under the terms of the GNU Lesser General Public License as published by 
   the Free Software Foundation; either version 2.1 of the License, or (at 
   your option) any later version.
 
   This library is distributed in the hope that it will be useful, but 
   WITHOUT ANY WARRANTY; without even the implied warranty of 
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser 
   General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License 
   along with this library; if not, write to the Free Software Foundation, 
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 
   See also the full LGPL text here: http://www.gnu.org/copyleft/lesser.html
 ===========================================================================*/
 
 package net.sf.okapi.common;
 
 import java.util.Hashtable;
 
 /**
  * Provides definitions for common mime types and mappings from file extensions
  * to mime type.
  */
 public final class MimeTypeMapper {
 	private static final Hashtable<String, String> extensionToMimeMap = new Hashtable<String, String>();
 
 	public static final String DEFAULT_MIME_TYPE = "application/octet-stream";
 	public static final String XML_MIME_TYPE = "text/xml";
 	public static final String ODF_MIME_TYPE = "text/x-odf";
 	public static final String HTML_MIME_TYPE = "text/html";
 	public static final String PO_MIME_TYPE = "application/x-gettext";
 	public static final String XLIFF_MIME_TYPE = "application/x-xliff+xml";
 	public static final String RTF_MIME_TYPE = "text/rtf";
 	public static final String MS_DOC_MIME_TYPE = "application/msword";
 	public static final String MS_EXCEL_MIME_TYPE = "application/vnd.ms-excel";
 	public static final String MS_POWERPOINT_MIME_TYPE = "application/vnd.ms-powerpoint";
 	public static final String JAVASCRIPT_MIME_TYPE = "application/x-javascript";
 	public static final String CSV_MIME_TYPE = "text/csv";
 	public static final String INDESIGN_MIME_TYPE = "text/inx";
 	public static final String MIF_MIME_TYPE = "application/vnd.mif";
 	public static final String PLAIN_TEXT_MIME_TYPE = "text/plain";
 	public static final String QUARK_MIME_TYPE = "text/qml";
 	public static final String FLASH_MIME_TYPE = "text/x-flash-xml";
 	public static final String PROPERTIES_MIME_TYPE = "text/x-properties";
 	public static final String SERVER_SIDE_INCLUDE_MIME_TYPE = "text/x-ssi";
 	public static final String DOCX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
 	public static final String XLSX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
 	public static final String PPTX_MIME_TYPE = "application/vnd.openxmlformats-officedocument.presentationml.presentation";
 	public static final String DOCM_MIME_TYPE = "application/vnd.ms-word.document.macroEnabled";
 	public static final String XLSM_MIME_TYPE = "application/vnd.ms-excel.sheet.macroEnabled";
 	public static final String PPTM_MIME_TYPE = "application/vnd.ms-powerpoint.presentation.macroEnabled";
 
 	static {
 		extensionToMimeMap.put("xml", XML_MIME_TYPE);
 		extensionToMimeMap.put("odf", ODF_MIME_TYPE);
 		extensionToMimeMap.put("html", HTML_MIME_TYPE);
 		extensionToMimeMap.put("htm", HTML_MIME_TYPE);
 		extensionToMimeMap.put("po", PO_MIME_TYPE);
 		extensionToMimeMap.put("rtf", RTF_MIME_TYPE);
 		extensionToMimeMap.put("doc", MS_DOC_MIME_TYPE);
 		extensionToMimeMap.put("xls", MS_EXCEL_MIME_TYPE);
 		extensionToMimeMap.put("ppt", MS_POWERPOINT_MIME_TYPE);
 		extensionToMimeMap.put("js", JAVASCRIPT_MIME_TYPE);
 		extensionToMimeMap.put("csv", CSV_MIME_TYPE);
 		extensionToMimeMap.put("inx", INDESIGN_MIME_TYPE);
 		extensionToMimeMap.put("mif", MIF_MIME_TYPE);
 		extensionToMimeMap.put("txt", PLAIN_TEXT_MIME_TYPE);
 		extensionToMimeMap.put("qml", QUARK_MIME_TYPE);
 		extensionToMimeMap.put("flash", FLASH_MIME_TYPE);
 		extensionToMimeMap.put("properties", PROPERTIES_MIME_TYPE);
 		extensionToMimeMap.put("ssi", SERVER_SIDE_INCLUDE_MIME_TYPE);
 		extensionToMimeMap.put("docx", DOCX_MIME_TYPE);
 		extensionToMimeMap.put("docm", DOCM_MIME_TYPE);
 		extensionToMimeMap.put("xlsx", XLSX_MIME_TYPE);
		extensionToMimeMap.put("xlsm", XLSM_MIME_TYPE);
 		extensionToMimeMap.put("pptx", PPTX_MIME_TYPE);
 		extensionToMimeMap.put("pptm", PPTM_MIME_TYPE);
 	}
 
 	/**
 	 * Gets the mime type associated with the provided file extension. Some mime
 	 * types map to many file extensions. Some file extensions map to many mime
 	 * types. For example, there are many types of xml files which have
 	 * different mime types.
 	 * 
 	 * @param extension
 	 *            the file extension to lookup (without the dot prefix)
 	 * @return the mime type (UNKOWN_MIME_TYPE if the extension is not known).
 	 */
 	public static String getMimeType(String extension) {
 		String mimeType = extensionToMimeMap.get(extension);
 		if ( mimeType == null ) {
 			return DEFAULT_MIME_TYPE;
 		}
 		return mimeType;
 	}
 
 }
