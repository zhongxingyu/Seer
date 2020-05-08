 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2010
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.chemistry.webdav;
 
 import com.bradmcevoy.http.*;
 import com.bradmcevoy.http.exceptions.ConflictException;
 import com.bradmcevoy.http.exceptions.NotAuthorizedException;
 import com.generationjava.io.xml.SimpleXmlWriter;
 import com.generationjava.io.xml.XmlWriter;
 import org.apache.chemistry.*;
 
 import java.io.*;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static com.flexive.chemistry.webdav.AuthenticationFilter.getConnection;
 
 /**
  * @author Daniel Lichtenberger (daniel.lichtenberger@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class FolderResource extends ObjectResource<Folder>
        implements CollectionResource, MakeCollectionableResource, PutableResource, QuotaResource {
     private static final Pattern PAT_OBJECTID = Pattern.compile("objectId=\"([^\"]+)\"");
     private static final String NS_XHTML = "http://www.w3.org/1999/xhtml";
 
     public FolderResource(ChemistryResourceFactory resourceFactory, String path, Folder object) {
         super(resourceFactory, path, object);
     }
 
     /**
      * {@inheritDoc}
      */
     public FolderResource createCollection(String newName) throws NotAuthorizedException, ConflictException {
         final Folder newFolder = object.newFolder(object.getBaseType().getId());
         try {
             newFolder.setName(newName);
             newFolder.save();
         } catch (CMISException e) {
             throw CMISExceptionWrapper.wrap(e);
         }
         return new FolderResource(resourceFactory, getChildPath(newName), newFolder);
     }
 
     /**
      * {@inheritDoc}
      */
     public List<? extends Resource> getChildren() {
         final List<Resource> result = new ArrayList<Resource>();
         for (CMISObject object : getObject().getChildren()) {
             result.add(resourceFactory.createResource(getChildPath(object.getName()), object));
         }
         return result;
     }
 
     /**
      * {@inheritDoc}
      */
     public Resource child(String childName) {
         for (CMISObject child : getObject().getChildren()) {
             if (childName.equals(child.getName())) {
                 return resourceFactory.createResource(path + object.getName(), child);
             }
         }
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException, NotAuthorizedException {
         // TODO: send folder form?
         final XmlWriter xml = new SimpleXmlWriter(new PrintWriter(out));
         xml.writeEntity("html");
         xml.writeAttribute("xmlns", NS_XHTML);
 
         xml.writeEntity("body");
         xml.writeEntity("h1").writeText("Index of " + path).endEntity();
         xml.writeEntity("pre");
 
         final int namePadding = 50;
 
         // parent directory entry
         if (!"/".equals(path)) {
             writeDirectoryListingEntry(
                     xml, namePadding,
                     "../", "Parent Directory",
                     null, null
             );
         }
         // directory entries
         for (Resource resource : sortChildrenForListing()) {
             // link
             final boolean isFolder = resource instanceof CollectionResource;
             final String displayName = String.format("%." + namePadding + "s", resource.getName())
                     + (isFolder ? "/" : "");
             // size
             final Long contentLength = resource instanceof GetableResource
                     ? ((GetableResource) resource).getContentLength()
                     : null;
 
             writeDirectoryListingEntry(
                     xml, namePadding,
                     URLEncoder.encode(resource.getName(), "UTF-8").replace("+", "%20")
                             + (isFolder ? "/" : ""),
                     displayName,
                     resource.getModifiedDate(),
                     contentLength
             );
         }
         xml.endEntity();    // pre
         xml.endEntity();    // body
         xml.endEntity();    // html
         xml.close();
     }
 
     /**
      * Render a "pre"-formatted entry of the directory listing for the HTTP directory index view.
      *
      * @param xml           the XML output writer
      * @param namePadding   size of the "name" column
      * @param path          the actual path to be rendered
      * @param displayName   the name to be rendered for the item
      * @param lastModified  the modification date, or null if it is unknown
      * @param contentLength the content length, or null if it is unknown
      * @throws IOException  on output errors
      */
     protected void writeDirectoryListingEntry(XmlWriter xml, int namePadding, String path, String displayName, Date lastModified, Long contentLength) throws IOException {
         // formatter for modification date
         final SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy hh:mm");
 
         xml.writeEntity("a")
                 .writeAttribute("href", path)
                 .writeText(displayName)
                 .endEntity();
 
         // name (right) padding
         for (int i = displayName.length(); i < namePadding; i++) {
             xml.writeText(" ");
         }
 
         // last modified
         final String formattedDate = sdf.format(lastModified != null ? lastModified : new Date());
         xml.writeText("  ");
         if (lastModified != null) {
             xml.writeText(formattedDate);
         } else {
             // write empty placeholder
             for (int i = 0; i < formattedDate.length(); i++) {
                 xml.writeText(" ");
             }
         }
 
         xml.writeText("  " + formatContentLength(contentLength));
 
         xml.writeEmptyEntity("br");
     }
 
     /**
      * Format a content length for the directory listing.
      *
      * @param contentLength the content length, or null if it is unknown
      * @return  the formatted length
      */
     protected String formatContentLength(Long contentLength) {
         final String formattedSize;
         if (contentLength != null) {
             if (contentLength < 1024) {
                 formattedSize = String.valueOf(contentLength);
             } else {
                 final long sizeKiB = contentLength / 1024;
                 if (sizeKiB < 1024) {
                     formattedSize = sizeKiB + "K";
                 } else {
                     formattedSize = String.format("%.1f", sizeKiB / 1024.0) + "M";
                 }
             }
         } else {
             formattedSize = "-";
         }
         return formattedSize;
     }
 
     /**
      * Sort the children of this folder for the browser directory listing. The sort order is
      * directories first, then lexical (case-insensitive).
      *
      * @return  the sorted (direct) children of this folder
      */
     protected List<Resource> sortChildrenForListing() {
         final List<Resource> children = new ArrayList<Resource>(getChildren());
         Collections.sort(
                 children,
                 new Comparator<Resource>() {
                     public int compare(Resource r1, Resource r2) {
                         // sort children: folders first, then sort lexically
                         final boolean isFolder1 = r1 instanceof CollectionResource;
                         final boolean isFolder2 = r2 instanceof CollectionResource;
                         if (isFolder1 && !isFolder2) {
                             return -1;
                         } else if (isFolder2 && !isFolder1) {
                             return 1;
                         } else {
                             return r1.getName().compareToIgnoreCase(r2.getName());
                         }
                     }
                 }
         );
         return children;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getContentType(String accepts) {
         return "text/html";
     }
 
     /**
      * {@inheritDoc}
      */
     public Long getContentLength() {
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public Resource createNew(String newName, InputStream inputStream, Long length, String contentType) throws IOException {
         // if needed wrap as BufferedInputStream, which always supports mark().
         final InputStream peekStream = inputStream.markSupported() ? inputStream : new BufferedInputStream(inputStream);
         // check if we're facing a TextDocument
         final byte[] header = new byte[256];
         peekStream.mark(header.length);
         final int read = peekStream.read(header);
         peekStream.reset();     // reset input to first position
         Resource result = null;
         if (read > 0) {
             result = handleTextDocument(peekStream, header, read, newName);
         }
         if (result == null) {
             result = handleBinaryDocument(peekStream, length, header, contentType, newName);
         }
         return result;
     }
 
     protected Resource handleBinaryDocument(InputStream inputStream, Long length, byte[] header, String contentType, String newName) throws IOException {
 
         // split detected content types
         final List<String> mimeTypes = contentType == null
                 ? new ArrayList<String>(0)
                 : Arrays.asList(contentType.split(","));
 
         final Document doc = getObject().newDocument(getDocumentType(newName, header, mimeTypes));
         try {
             doc.setName(newName);
             if (inputStream != null) {
                 final String mimeType = getMimeType(mimeTypes);
                 doc.setContentStream(new UploadContentStream(inputStream, newName, mimeType, length));
             }
             doc.save();
         } catch (CMISException e) {
             throw CMISExceptionWrapper.wrap(e);
         }
 
         return resourceFactory.createResource(getChildPath(newName), doc);
     }
 
     /**
      * Return the CMIS document type for the given MIME type(s).
      *
      * @parma name          the filename
      * @param header        the first 256 bytes of the file
      * @param mimeTypes     the detected MIME type(s) of the content
      * @return              the CMIS document type to be used for creating a new instance
      */
     protected String getDocumentType(String name, byte[] header, List<String> mimeTypes) {
         return BaseType.DOCUMENT.getId();
     }
 
     /**
      * Return the primary MIME type to be used if the repository does not support multiple MIME types.
      *
      * @param mimeTypes the detected MIME type(s)
      * @return          the MIME type to be used for the content stream
      */
     protected String getMimeType(List<String> mimeTypes) {
         // if more than one mime type is detected, use the first one
         return mimeTypes.isEmpty() ? "unknown/unknown" : mimeTypes.get(0);
     }
 
     protected Resource handleTextDocument(InputStream input, byte[] header, int read, String newName) throws IOException {
         final String stringHeader;
         try {
             stringHeader = new String(header, 0, read, "UTF-8");
         } catch (Exception e) {
             // not a string
             return null;
         }
         if (stringHeader.contains("<document") && stringHeader.contains("objectId") && stringHeader.contains("typeId")) {
 
             // got XML document generated with TextDocumentResource. First use the original document as a template,
             // then patch using the stream's XML representation
 
             final String objectId = parseObjectId(stringHeader);
 
             // copy original document to this folder
             final Connection conn = getConnection();
             final TextDocumentResource doc = resourceFactory.createTextDocument(
                     getChildPath(newName),
                     (Document) conn.getObject(
                             conn.getSPI().newObjectId(objectId)
                     )
             );
             doc.copyObject(this, newName, false);
 
             // patch new properties into new document instance, but force the new document name
             final Document newDoc = ((DocumentResource) resourceFactory.getResource(null, getChildPath(newName))).getObject();
             final TextDocumentResource newResource = resourceFactory.createTextDocument(getChildPath(newName), newDoc);
             newResource.processXmlProperties(input);
             try {
                 newDoc.setName(newName);
                 newDoc.save();
             } catch (CMISException e) {
                 throw CMISExceptionWrapper.wrap(e);
             } 
 
             return newResource;
         }
         return null;
     }
 
     protected String parseObjectId(String stringHeader) {
         final Matcher matcher = PAT_OBJECTID.matcher(stringHeader);
         if (!matcher.find()) {
             throw new IllegalStateException("objectId not set in XML document head.");
         }
         return matcher.group(1);
     }
 
     /**
      * {@inheritDoc}
      */
     public void delete() {
         try {
             object.deleteTree(Unfiling.DELETE_SINGLE_FILED);
         } catch (CMISException e) {
             throw CMISExceptionWrapper.wrap(e);
         }
     }

    public Long getQuotaAvailable() {
        // return unlimited quota to avoid confusing clients that check quotas (MIL-67)
        return Long.valueOf(Long.MAX_VALUE);
    }

    public Long getQuotaUsed() {
        return Long.valueOf(0);
    }

 }
