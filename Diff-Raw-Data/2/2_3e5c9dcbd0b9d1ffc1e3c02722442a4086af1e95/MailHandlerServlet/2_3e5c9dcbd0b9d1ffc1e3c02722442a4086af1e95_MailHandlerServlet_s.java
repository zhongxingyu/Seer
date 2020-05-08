 /*
  * Copyright (C) 2012 Timo Vesalainen
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.vesalainen.mailblog;
 
 import com.google.appengine.api.NamespaceManager;
 import com.google.appengine.api.blobstore.BlobstoreService;
 import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.Email;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.GeoPt;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Text;
 import com.google.appengine.api.datastore.Transaction;
 import com.google.appengine.api.images.Image;
 import com.google.appengine.api.images.ImagesService;
 import com.google.appengine.api.images.ImagesServiceFactory;
 import com.google.appengine.api.images.Transform;
 import com.google.appengine.api.mail.MailService;
 import com.google.appengine.api.mail.MailService.Message;
 import com.google.appengine.api.mail.MailServiceFactory;
 import com.google.appengine.api.urlfetch.FetchOptions;
 import com.google.appengine.api.urlfetch.HTTPHeader;
 import com.google.appengine.api.urlfetch.HTTPMethod;
 import com.google.appengine.api.urlfetch.HTTPRequest;
 import com.google.appengine.api.urlfetch.HTTPResponse;
 import com.google.appengine.api.urlfetch.URLFetchService;
 import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
 import com.google.apphosting.api.ApiProxy;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.io.PrintWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Properties;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import javax.mail.Address;
 import javax.mail.BodyPart;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.Session;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeUtility;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.bind.JAXBException;
 import org.vesalainen.gpx.GPX;
 import org.vesalainen.kml.KML;
 import org.vesalainen.kml.KMZ;
 import static org.vesalainen.mailblog.BlogConstants.DateProperty;
 import static org.vesalainen.mailblog.BlogConstants.OriginalSizeProperty;
 import org.vesalainen.mailblog.MaidenheadLocator.LocatorLevel;
 import org.vesalainen.mailblog.exif.ExifParser;
 import org.vesalainen.mailblog.types.ContentCounter;
 
 /**
  *
  * @author Timo Vesalainen
  */
 public class MailHandlerServlet extends HttpServlet implements BlogConstants
 {
     private static final String CRLF = "\r\n";
     private static final String WinlinkSuffix = "winlink.org>";
 
     /**
      * Handles the HTTP
      * <code>GET</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException
     {
     }
 
     /**
      * Handles the HTTP
      * <code>POST</code> method.
      *
      * @param request servlet request
      * @param response servlet response
      * @throws ServletException if a servlet-specific error occurs
      * @throws IOException if an I/O error occurs
      */
     @Override
     protected void doPost(HttpServletRequest request, HttpServletResponse response)
             throws ServletException, IOException
     {
         try
         {
             String removeKey = request.getParameter(RemoveParameter);
             if (removeKey != null)
             {
                 remove(removeKey, response);
             }
             else
             {
                 try
                 {
                     BlogAuthor blogAuthor = setNamespace(request, response);
                     if (blogAuthor == null)
                     {
                         return;
                     }
                     handleMail(request, response, blogAuthor);
                 }
                 catch (MessagingException ex)
                 {
                     log(ex.getMessage(), ex);
                     response.sendError(HttpServletResponse.SC_CONFLICT);
                 }
                 catch (EntityNotFoundException ex)
                 {
                     log(ex.getMessage(), ex);
                     response.sendError(HttpServletResponse.SC_FORBIDDEN);
                 }
             }
         }
         catch (HttpException ex)
         {
             log(ex.getMessage(), ex);
             ex.sendError(response);
         }
     }
     private BlogAuthor setNamespace(HttpServletRequest request, HttpServletResponse response) throws IOException
     {
         String pathInfo = request.getPathInfo();
         log("pathInfo="+pathInfo);
         if (pathInfo == null)
         {
             log("pathInfo=null");
             response.sendError(HttpServletResponse.SC_FORBIDDEN);
             return null;
         }
         int idx = pathInfo.indexOf('@');
         if (idx == -1)
         {
             log("pathInfo doesn't contain @");
             response.sendError(HttpServletResponse.SC_FORBIDDEN);
             return null;
         }
         String namespace = pathInfo.substring(1, idx);
         NamespaceManager.set(namespace);
         log("namespace set to "+namespace);
         String address = pathInfo.substring(idx+1);
         return new BlogAuthor(namespace, address);
     }
     private void handleMail(HttpServletRequest request, HttpServletResponse response, BlogAuthor blogAuthor) throws IOException, ServletException, EntityNotFoundException, MessagingException, HttpException
     {
         DS ds = DS.get();
         Properties props = new Properties();
         Session session = Session.getDefaultInstance(props, null);
         MimeMessage message = new MimeMessage(session, request.getInputStream());
         String messageId = getMessageId(message);
         String contentType = message.getContentType();
         
         if (messageId == null)
         {
             log("messageID missing");
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return;
         }
         log("Message-ID="+messageId);
         // TODO authorization
         if (handleSpot(message))
         {
             return;
         }
         InternetAddress sender = (InternetAddress) message.getSender();
         log("sender="+sender);
         if (sender == null)
         {
             Address[] from = message.getFrom();
             if (from != null && from.length != 0)
             {
                 sender = (InternetAddress) from[0];
             }
         }
         if (sender == null)
         {
             log("Sender missing");
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return;
         }
         Email senderEmail = new Email(sender.getAddress());
         Settings settings = ds.getSettingsFor(senderEmail);
         if (settings == null)
         {
             log(senderEmail.getEmail()+" not allowed to send blogs");
             response.sendError(HttpServletResponse.SC_FORBIDDEN);
             return;
         }
         String[] ripperDate = message.getHeader(BlogRipper+"Date");
         boolean ripping = ripperDate != null && ripperDate.length > 0;
         Object content = message.getContent();
         if (content instanceof Multipart)
         {
             Multipart multipart = (Multipart) message.getContent();
             List<BodyPart> bodyPartList = findParts(multipart);
             try
             {
                 Entity blog = null;
                 String htmlBody = getHtmlBody(bodyPartList);
                 if (htmlBody != null && htmlBody.length() > 10)
                 {
                     boolean publishImmediately = settings.isPublishImmediately();
                     blog = updateBlog(messageId, message, htmlBody, publishImmediately, senderEmail);
                     if (!ripping)
                     {
                         if (blog != null)
                         {
                             sendMail(request, blogAuthor, blog, settings);
                         }
                     }
                     else
                     {
                         log("not sending email because ripping");
                     }
                 }
                 else
                 {
                     log("no html body");
                 }
                 List<Future<HTTPResponse>> futureList = new ArrayList<Future<HTTPResponse>>();
                 for (BodyPart bodyPart : bodyPartList)
                 {
                     Collection<Future<HTTPResponse>> futures = handleBodyPart(request, blog, bodyPart, settings);
                     if (futures != null)
                     {
                         futureList.addAll(futures);
                     }
                 }
                 long remainingMillis = ApiProxy.getCurrentEnvironment().getRemainingMillis();
                 log("remainingMillis="+remainingMillis);
                 for (Future<HTTPResponse> res : futureList)
                 {
                     try
                     {
                         HTTPResponse hr = res.get();
                         log("code="+hr.getResponseCode());
                         if (hr.getResponseCode() != HttpServletResponse.SC_OK)
                         {
                             throw new ServletException("blob upload failed code="+hr.getResponseCode());
                         }
                     }
                     catch (InterruptedException ex)
                     {
                         throw new IOException(ex);
                     }
                     catch (ExecutionException ex)
                     {
                         throw new IOException(ex);
                     }
                 }
             }
             catch (MessagingException ex)
             {
                 throw new IOException(ex);
             }
         }
         else
         {
             if (content instanceof String)
             {
                 String bodyPart = (String) content;
                 if (contentType.startsWith("text/plain"))
                 {
                     bodyPart = textPlainToHtml(bodyPart);
                 }
                 boolean publishImmediately = settings.isPublishImmediately();
                 Entity blog = updateBlog(messageId, message, bodyPart, publishImmediately, senderEmail);
                 if (blog != null)
                 {
                     sendMail(request, blogAuthor, blog, settings);
                 }
             }
             else
             {
                 log("body not MultiPart of String");
             }
         }
     }
 
     private void setProperty(MimeMessage message, String name, Entity blog, boolean indexed) throws MessagingException, IOException
     {
         Object header = getHeader(message, name);
         if (header != null)
         {
             if (indexed)
             {
                 blog.setProperty(name, header);
             }
             else
             {
                 blog.setUnindexedProperty(name, header);
             }
         }
     }
     private Object getHeader(MimeMessage message, String name) throws MessagingException, IOException
     {
         String[] header = message.getHeader(BlogRipper+name);
         if (header == null || header.length == 0)
         {
             header = message.getHeader(name);
         }
         else
         {
             log("using "+BlogRipper+name);
         }
         if (header == null || header.length == 0)
         {
             return null;
         }
         if (DateProperty.equals(name))
         {
             SimpleDateFormat df = new SimpleDateFormat(RFC1123Format, Locale.US);
             try
             {
                 return df.parse(header[0]);
             }
             catch (ParseException ex)
             {
                 throw new IOException(ex);
             }
         }
         if ("Sender".equals(name))
         {
             String email = header[0];
             int i1 = email.indexOf('<');
             int i2 = email.indexOf('>');
             if (i1 != -1 && i2 != -1)
             {
                 email = email.substring(i1+1, i2);
             }
             return new Email(email);
         }
         return MimeUtility.decodeText(header[0]);
     }
     private Collection<Future<HTTPResponse>> handleBodyPart(HttpServletRequest request, Entity blog, BodyPart bodyPart, final Settings settings) throws MessagingException, IOException
     {
         ImagesService imagesService = ImagesServiceFactory.getImagesService();
         DS ds = DS.get();
         Collection<Future<HTTPResponse>> futures = new ArrayList<Future<HTTPResponse>>();
         String contentType = bodyPart.getContentType();
         log(contentType);
         Object content = bodyPart.getContent();
         if (content instanceof InputStream)
         {
             String filename = bodyPart.getFileName();
             byte[] bytes = getBytes(bodyPart);
             String digestString = DS.getDigest(bytes);
             createMetadata(digestString, filename, contentType, bytes);
             String[] cids = bodyPart.getHeader("Content-ID");
             if (cids != null && cids.length > 0)
             {
                 replaceBlogRef(blog, cids[0], digestString);
             }
             if (contentType.startsWith("image/"))
             {
                 Image image = ImagesServiceFactory.makeImage(bytes);
                 if (settings.isFixPic())
                 {
                     Transform makeImFeelingLucky = ImagesServiceFactory.makeImFeelingLucky();
                     image = imagesService.applyTransform(makeImFeelingLucky, image);
                 }
                 if (
                         image.getHeight() > settings.getPicMaxHeight() ||
                         image.getWidth() > settings.getPicMaxWidth()
                         )
                 {
                     log("shrinking ["+image.getHeight()+", "+image.getWidth()+"] > ["+settings.getPicMaxHeight()+", "+settings.getPicMaxWidth()+"]");
                     Transform makeResize = ImagesServiceFactory.makeResize(settings.getPicMaxHeight(), settings.getPicMaxWidth());
                     Image shrinken = imagesService.applyTransform(makeResize, image);
                     Future<HTTPResponse> res = postBlobs(filename, contentType, digestString, shrinken.getImageData(), WebSizeProperty, request);
                     futures.add(res);
                 }
                 else
                 {
                     Future<HTTPResponse> res = postBlobs(filename, contentType, digestString, bytes, WebSizeProperty, request);
                     futures.add(res);
                 }
                 Future<HTTPResponse> res = postBlobs(filename, contentType, digestString, bytes, OriginalSizeProperty, request);
                 futures.add(res);
             }
             if (contentType.startsWith("application/vnd.google-earth.kml+xml") || filename.endsWith(".kml"))
             {
                 try
                 {
                     InputStream is = (InputStream) content;
                     KML kml = new KML(is);
                     PlacemarkUpdater pu = new PlacemarkUpdater(ds, kml, LocatorLevel.Field);
                     pu.visit(kml, null);
                 }
                 catch (JAXBException ex)
                 {
                     log("reading kml failed", ex);
                 }
             }        
             if (contentType.startsWith("application/vnd.google-earth.kmz") || filename.endsWith(".kmz"))
             {
                 try
                 {
                     InputStream is = (InputStream) content;
                     KMZ kmz = new KMZ(is);
                     PlacemarkUpdater pu = new PlacemarkUpdater(ds, kmz, LocatorLevel.Field);
                     pu.visit(kmz, null);
                 }
                 catch (JAXBException ex)
                 {
                     log("reading kmz failed", ex);
                 }
             }        
             if (filename.endsWith(".gpx"))
             {
                 try
                 {
                     InputStream is = (InputStream) content;
                     final GPX gpx = new GPX(is);
                     final OpenCPNTrackHandler handler = new OpenCPNTrackHandler(ds);
                     RunInNamespace rin = new RunInNamespace() 
                     {
                         @Override
                         protected Object run()
                         {
                             gpx.browse(
                                     settings.getTrackBearingTolerance(), 
                                     settings.getTrackMinimumDistance(), 
                                     handler
                                     );
                             return null;
                         }
                     };
                     rin.doIt(null, settings.isCommonPlacemarks());
                 }
                 catch (JAXBException ex)
                 {
                     log("reading kmz failed", ex);
                 }
             }        
             if (contentType.startsWith("application/X-jsr179-location-nmea") || filename.endsWith(".nmea"))
             {
                 log("NMEA not yet supported");
             }        
         }
         return futures;
     }
 
     private Future<HTTPResponse> postBlobs(String filename, String contentType, String sha1, byte[] data, String metadataSize, HttpServletRequest request) throws MessagingException, IOException
     {
         try
         {
             URLFetchService fetchService = URLFetchServiceFactory.getURLFetchService();
             BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
             URI reqUri = new URI(request.getScheme(), request.getServerName(), "", "");
             URI uri = reqUri.resolve("/blob?"+NamespaceParameter+"="+NamespaceManager.get()+"&"+SizeParameter+"="+metadataSize);
             URL uploadUrl = new URL(blobstore.createUploadUrl(uri.toASCIIString()));
             log("post blob to "+uploadUrl);
             HTTPRequest httpRequest = new HTTPRequest(uploadUrl, HTTPMethod.POST, FetchOptions.Builder.withDeadline(60));
             String uid = UUID.randomUUID().toString();
             httpRequest.addHeader(new HTTPHeader("Content-Type", "multipart/form-data; boundary="+uid));
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             PrintStream ps = new PrintStream(baos);
             ps.append("--"+uid);
             ps.append(CRLF);
             ps.append("Content-Disposition: form-data; name=\""+sha1+"\"; filename=\""+filename+"\"");
             ps.append(CRLF);
             ps.append("Content-Type: "+contentType);
             ps.append(CRLF);
             ps.append("Content-Transfer-Encoding: binary");
             ps.append(CRLF);
             ps.append(CRLF);
             ps.write(data);
             ps.append(CRLF);
             ps.append("--"+uid+"--");
             ps.append(CRLF);
             ps.close();
             log("sending blob size="+baos.size());
             httpRequest.setPayload(baos.toByteArray());
             return fetchService.fetchAsync(httpRequest);
         }
         catch (URISyntaxException ex)
         {
             throw new IOException(ex);
         }
     }
 
     private List<BodyPart> findParts(Multipart multipart) throws MessagingException, IOException
     {
         List<BodyPart> list = new ArrayList<BodyPart>();
         findParts(list, multipart);
         return list;
     }
     private void findParts(List<BodyPart> list, Multipart multipart) throws MessagingException, IOException
     {
         for (int ii=0;ii<multipart.getCount();ii++)
         {
             BodyPart bodyPart = multipart.getBodyPart(ii);
             list.add(bodyPart);
             Object content = bodyPart.getContent();
             if (content instanceof Multipart)
             {
                 Multipart mp = (Multipart) content;
                 findParts(list, mp);
             }
         }
     }
     private void remove(String encoded, HttpServletResponse response) throws IOException
     {
         BlobstoreService blobstore = BlobstoreServiceFactory.getBlobstoreService();
         DatastoreService datastore = DS.get();
         Key key = KeyFactory.stringToKey(encoded);
         Transaction tr = datastore.beginTransaction();
         try
         {
             Entity blog = datastore.get(key);
             datastore.delete(key);
             response.setContentType("text/plain");
             PrintWriter writer = response.getWriter();
             writer.println("Deleted blog: "+blog.getProperty("Subject"));
             tr.commit();
         }
         catch (EntityNotFoundException ex)
         {
             throw new IOException(ex);
         }
         finally
         {
             if (tr.isActive())
             {
                 tr.rollback();
             }
         }
     }
 
         
     private byte[] getBytes(BodyPart bodyPart) throws IOException, MessagingException
     {
         Object content = bodyPart.getContent();
         InputStream is = (InputStream) content;
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         byte[] buffer = new byte[8192];
         int rc = is.read(buffer);
         while (rc != -1)
         {
             baos.write(buffer, 0, rc);
             rc = is.read(buffer);
         }
         return baos.toByteArray();
     }
     
     private void sendMail(HttpServletRequest request, BlogAuthor blogAuthor, Entity blog, Settings settings) throws IOException
     {
         if (settings.dontSendEmail())
         {
             return;
         }
         try
         {
             String digest = DS.getBlogDigest(blog);
             MailService mailService = MailServiceFactory.getMailService();
             Message reply = new Message();
             reply.setSender(blogAuthor.toString());
             Email sender = (Email) blog.getProperty(SenderProperty);
             reply.setTo(sender.getEmail());
             String subject = (String) blog.getProperty(SubjectProperty);
             reply.setSubject("Blog: "+subject+" received");
             StringBuilder sb = new StringBuilder();
             URI reqUri = new URI(request.getScheme(), NamespaceManager.get(), "", "");
             if (!settings.isPublishImmediately())
             {
                 sb.append("<div>");
                 sb.append("<p>Blog is not yet published because it was sent from untrusted email address "+sender.getEmail()+". </p>");
                 URI publishUri = reqUri.resolve("/blog?action=publish&blog="+KeyFactory.keyToString(blog.getKey())+"&auth="+digest);
                 sb.append("<a href=\""+publishUri.toASCIIString()+"\">Publish Blog</a>");
                 sb.append("</div>");
             }
             sb.append("<div>");
             sb.append("<p>If blog is not ok, you can delete and then resend it.</p>");
             URI deleteUri = reqUri.resolve("/blog?action=remove&blog="+KeyFactory.keyToString(blog.getKey())+"&auth="+digest);
             sb.append("<a href=\""+deleteUri.toASCIIString()+"\">Delete Blog</a>");
             sb.append("</div>");
             reply.setHtmlBody(sb.toString());
             mailService.send(reply);
         }
         catch (URISyntaxException ex)
         {
             throw new IOException(ex);
         }
     }
 
     private void replaceBlogRef(final Entity origBlog, final String cidStr, final String sha1) throws IOException
     {
         if (origBlog != null)
         {
             final Key blogKey = origBlog.getKey();
             Updater<Object> updater = new Updater<Object>()
             {
                 @Override
                 protected Object update() throws IOException
                 {
                     DS ds = DS.get();
                     Entity blog;
                     try
                     {
                         blog = ds.get(blogKey);
                     }
                     catch (EntityNotFoundException ex)
                     {
                         throw new IOException(ex);
                     }
                     Collection<Key> attachments = (Collection<Key>) blog.getProperty(AttachmentsProperty);
                     if (attachments == null)
                     {
                         attachments = new HashSet<Key>();
                         blog.setUnindexedProperty(AttachmentsProperty, attachments);
                     }
                     attachments.add(ds.getMetadataKey(sha1));
                     String cid = cidStr;
                     if (cid.startsWith("<") && cid.endsWith(">"))
                     {
                         cid = cid.substring(1, cid.length()-1);
                     }
                     Text text = (Text) blog.getProperty(HtmlProperty);
                     String body = text.getValue();
                     int start = body.indexOf("cid:"+cid);
                     if (start != -1)
                     {
                         int end = body.indexOf(">", start);
                         start = body.lastIndexOf("<img", start);
                         if (start != -1 && end != -1)
                         {
                             StringBuilder sb = new StringBuilder();
                             sb.append(body.substring(0, start));
                             sb.append("<img src=\"/blob?"+Sha1Parameter+"="+sha1+"\">");
                             sb.append(body.substring(end+1));
                             blog.setUnindexedProperty(HtmlProperty, new Text(sb.toString()));
                         }
                     }
                     ds.put(blog);
                     return null;
                 }
             };
             updater.start();
         }
     }
 
     private String getHtmlBody(List<BodyPart> bodyPartList) throws MessagingException, IOException
     {
         String htmlBody = null;
         for (BodyPart bodyPart : bodyPartList)
         {
             String contentType = bodyPart.getContentType();
             Object content = bodyPart.getContent();
             if (contentType.startsWith("text/html"))
             {
                 htmlBody = (String) content;
             }
             else
             {
                 if (contentType.startsWith("text/plain"))
                 {
                     if (htmlBody == null)
                     {
                         htmlBody = (String) content;
                         htmlBody = textPlainToHtml(htmlBody);
                     }
                 }
             }
         }
         return htmlBody;
     }
 
     private Entity updateBlog(final String messageId, final MimeMessage message, final String htmlBody, final boolean publishImmediately, final Email senderEmail) throws IOException
     {
         Updater<Entity> updater = new Updater<Entity>()
         {
             @Override
             protected Entity update() throws IOException
             {
                 try
                 {
                     String subject = (String) getHeader(message, SubjectProperty);
                     if (subject.startsWith("//WL2K "))
                     {
                         subject = subject.substring(7);
                     }
                     DS ds = DS.get();
                     Settings settings = ds.getSettings();
                     Entity blog = ds.getBlogEntity(messageId, subject);
                     if (
                             subject == null || 
                             subject.length() < 2 ||
                             htmlBody == null ||
                             ContentCounter.countChars(htmlBody) < 5
                             )
                     {
                        ds.delete(blog.getKey());
                         return null;
                     }
                     int idx = subject.indexOf('{');
                     if (idx != -1)
                     {
                         Set<String> keywords = getKeywords(subject.substring(idx+1));
                         subject = subject.substring(0, idx-1).trim();
                         if (!keywords.isEmpty())
                         {
                             blog.setProperty(KeywordsProperty, keywords);
                         }
                     }
                     blog.setProperty(SubjectProperty, subject);
                     blog.setProperty(PublishProperty, publishImmediately);
                     blog.setProperty(SubjectProperty, subject);
                     blog.setProperty(SenderProperty, senderEmail);
                     setProperty(message, DateProperty, blog, true);
                     blog.setUnindexedProperty(HtmlProperty, new Text(htmlBody));
                     blog.setProperty(TimestampProperty, new Date());
                     Entity placemark = ds.fetchLastPlacemark(settings);
                     if (placemark != null)
                     {
                         GeoPt location = (GeoPt) placemark.getProperty(LocationProperty);
                         if (location != null)
                         {
                             blog.setProperty(LocationProperty, location);
                         }
                     }
                     ds.saveBlog(blog);
                     return blog;
                 }
                 catch (MessagingException ex)
                 {
                     throw new IOException(ex);
                 }
             }
 
         };
         return updater.start();
     }
 
     static Set<String> getKeywords(String str)
     {
         Set<String> set = new HashSet<String>();
         int idx = str.lastIndexOf('}');
         if (idx != -1)
         {
             str = str.substring(0, idx-1);
         }
         str = str.trim();
         String[] ss = str.split(" ");
         for (String s : ss)
         {
             if (!s.isEmpty())
             {
                 set.add(s);
             }
         }
         return set;
     }
     
     private Entity createMetadata(final String digestString, final String filename, final String contentType, final byte[] bytes) throws IOException
     {
         Updater<Entity> updater = new Updater<Entity>()
         {
             @Override
             protected Entity update() throws IOException
             {
                 DS ds = DS.get();
                 Entity metadata = ds.getMetadata(digestString);
                 log(metadata.toString());
                 if (metadata.getProperties().isEmpty())
                 {
                     metadata.setUnindexedProperty(FilenameProperty, filename);
                     metadata.setUnindexedProperty(ContentTypeProperty, contentType);
                     metadata.setUnindexedProperty(TimestampProperty, new Date());
                     try
                     {
                         if (contentType.startsWith("image/jpeg"))
                         {
                             ExifParser exif = new ExifParser(bytes);
                             Date timestamp = exif.getTimestamp();
                             if (timestamp != null)
                             {
                                 exif.populate(metadata);
                             }
                         }
                     }
                     catch (Exception ex)
                     {
                     }
                     ds.put(metadata);
                 }
                 return metadata;
             }
         };
         return updater.start();
     }
 
     private boolean handleSpot(MimeMessage message) throws IOException, MessagingException
     {
         String spotTime = getSpotHeader(message, "X-SPOT-Time");
         if (spotTime != null)
         {
             String spotLatitude = getSpotHeader(message, "X-SPOT-Latitude");
             String spotLongitude = getSpotHeader(message, "X-SPOT-Longitude");
             String spotMessenger = getSpotHeader(message, "X-SPOT-Messenger");
             String spotType = getSpotHeader(message, "X-SPOT-Type");
             Date time = new Date(Long.parseLong(spotTime)*1000);
             GeoPt geoPt = new GeoPt(Float.parseFloat(spotLatitude), Float.parseFloat(spotLongitude));
             DS ds = DS.get();
             ds.addPlacemark(time, geoPt, spotMessenger, spotType);
             return true;
         }
         return false;
     }
 
     private String getSpotHeader(MimeMessage message, String name) throws MessagingException
     {
         String[] spotHeader = message.getHeader(name);
         if (spotHeader != null && spotHeader.length > 0)
         {
             return spotHeader[0];
         }
         return null;
     }
 
     private String getMessageId(MimeMessage message) throws IOException, MessagingException
     {
         String messageID = (String) getHeader(message, "Message-ID");
         System.err.println(messageID);
         if (messageID.endsWith(WinlinkSuffix))
         {
             int idx = messageID.indexOf("@");
             if (idx != -1)
             {
                 messageID = messageID.substring(0, idx+1)+WinlinkSuffix;
             }
         }
         System.err.println(messageID);
         return messageID;
     }
 
     private String textPlainToHtml(String htmlBody)
     {
         return "<p>"+htmlBody.replaceAll("[\r\n]+", "\n<p>");
     }
     private class BlogAuthor
     {
         private String blogNamespace;
         private String blogAddress;
 
         public BlogAuthor(String blogNamespace, String blogAddress)
         {
             this.blogNamespace = blogNamespace;
             this.blogAddress = blogAddress;
         }
 
         public String getBlogNamespace()
         {
             return blogNamespace;
         }
 
         public String getBlogAddress()
         {
             return blogAddress;
         }
 
         @Override
         public String toString()
         {
             return blogNamespace + "@" + blogAddress;
         }
 
         
     }
 }
