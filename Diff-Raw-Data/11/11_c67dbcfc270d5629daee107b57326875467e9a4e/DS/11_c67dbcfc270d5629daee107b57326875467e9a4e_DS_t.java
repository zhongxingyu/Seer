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
 import com.google.appengine.api.datastore.Cursor;
 import com.google.appengine.api.datastore.Email;
 import com.google.appengine.api.datastore.Entities;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.FetchOptions;
 import com.google.appengine.api.datastore.GeoPt;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.PreparedQuery;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.CompositeFilter;
 import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
 import com.google.appengine.api.datastore.Query.Filter;
 import com.google.appengine.api.datastore.Query.FilterPredicate;
 import com.google.appengine.api.datastore.QueryResultList;
 import com.google.appengine.api.datastore.Text;
 import com.google.appengine.api.datastore.Transaction;
 import com.google.appengine.api.datastore.TransactionOptions;
 import com.google.appengine.api.users.User;
 import com.google.appengine.repackaged.com.google.common.base.Objects;
 import com.google.apphosting.api.ApiProxy;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.io.UnsupportedEncodingException;
 import java.io.Writer;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.security.DigestOutputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.DateFormat;
 import java.text.DateFormatSymbols;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.bind.JAXBElement;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 import net.opengis.kml.AbstractStyleSelectorType;
 import net.opengis.kml.BalloonStyleType;
 import net.opengis.kml.DocumentType;
 import net.opengis.kml.IconStyleType;
 import net.opengis.kml.LineStringType;
 import net.opengis.kml.LinkType;
 import net.opengis.kml.LookAtType;
 import net.opengis.kml.ObjectFactory;
 import net.opengis.kml.PlacemarkType;
 import net.opengis.kml.PointType;
 import net.opengis.kml.StyleType;
 import net.opengis.kml.TimeSpanType;
 import net.opengis.kml.TimeStampType;
 import org.vesalainen.kml.KMZ;
 import static org.vesalainen.mailblog.BlogConstants.BaseKey;
 import static org.vesalainen.mailblog.BlogConstants.BlogKind;
 import static org.vesalainen.mailblog.BlogConstants.DescriptionProperty;
 import static org.vesalainen.mailblog.BlogConstants.LocationProperty;
 import static org.vesalainen.mailblog.BlogConstants.PlacemarkKind;
 import static org.vesalainen.mailblog.BlogConstants.TimestampProperty;
 import static org.vesalainen.mailblog.BlogConstants.TitleProperty;
 import static org.vesalainen.mailblog.CachingDatastoreService.getRootKey;
 import org.vesalainen.mailblog.types.GeoPtType;
 import org.vesalainen.rss.Channel;
 import org.vesalainen.rss.Item;
 import org.vesalainen.rss.RSS;
 
 /**
  * @author Timo Vesalainen
  */
 public class DS extends CachingDatastoreService implements BlogConstants
 {
 
     private static Map<String, DS> nsMap = new HashMap<>();
     private String namespace;
 
     private DS(String namespace)
     {
         this.namespace = namespace;
     }
 
     public static DS get()
     {
         String ns = NamespaceManager.get();
         if (ns == null)
         {
             ns = "";
         }
         DS ds = nsMap.get(ns);
         if (ds == null)
         {
             ds = new DS(ns);
             nsMap.put(ns, ds);
         }
         return ds;
     }
 
     public String getNamespace()
     {
         return namespace;
     }
 
     public String createNamespaceSelect()
     {
         StringBuilder sb = new StringBuilder();
         sb.append("<select name=\"namespace\" class=\"namespaceSelect\">");
         Query query = new Query(Entities.NAMESPACE_METADATA_KIND);
         PreparedQuery prepared = prepare(query);
         for (Entity entity : prepared.asIterable())
         {
             String ns = Entities.getNamespaceFromNamespaceKey(entity.getKey());
             if (Objects.equal(namespace, ns))
             {
                 sb.append("<option selected value=\"" + ns + "\">" + ns + "</option>");
             }
             else
             {
                 sb.append("<option value=\"" + ns + "\">" + ns + "</option>");
             }
         }
         sb.append("</select>");
         return sb.toString();
     }
 
     public static String getBlogDigest(Entity blog)
     {
         try
         {
             MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
             DigestOutputStream dos = new DigestOutputStream(new NullOutputStream(), messageDigest);
             ObjectOutputStream oos = new ObjectOutputStream(dos);
             oos.writeObject(blog.getKey());
             oos.writeObject(blog.getProperty(SubjectProperty));
             oos.writeObject(blog.getProperty(SenderProperty));
             oos.writeObject(blog.getProperty(DateProperty));
             oos.close();
             byte[] digest = messageDigest.digest();
             return Hex.convert(digest);
         }
         catch (IOException ex)
         {
             throw new IllegalArgumentException(ex);
         }
         catch (NoSuchAlgorithmException ex)
         {
             throw new IllegalArgumentException(ex);
         }
     }
 
     public static String getDigest(Serializable serializable)
     {
         try
         {
             MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
             DigestOutputStream dos = new DigestOutputStream(new NullOutputStream(), messageDigest);
             ObjectOutputStream oos = new ObjectOutputStream(dos);
             oos.writeObject(serializable);
             oos.close();
             byte[] digest = messageDigest.digest();
             return Hex.convert(digest);
         }
         catch (IOException ex)
         {
             throw new IllegalArgumentException(ex);
         }
         catch (NoSuchAlgorithmException ex)
         {
             throw new IllegalArgumentException(ex);
         }
     }
 
     public static String getDigest(String str)
     {
         try
         {
             return getDigest(str.getBytes("utf-8"));
         }
         catch (UnsupportedEncodingException ex)
         {
             throw new IllegalArgumentException(ex);
         }
     }
 
     public static String getDigest(byte[] bytes)
     {
         try
         {
             MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
             byte[] digest = messageDigest.digest(bytes);
             return Hex.convert(digest);
         }
         catch (NoSuchAlgorithmException ex)
         {
             throw new IllegalArgumentException(ex);
         }
     }
 
     /**
      * Convenience method that sets contentType to "text/html" and charset to
      * "utf-8"
      *
      * @param response
      * @return
      * @throws IOException
      */
     public CacheWriter createCacheWriter(HttpServletRequest request, HttpServletResponse response) throws IOException
     {
         return createCacheWriter(request, response, "text/html", "utf-8", false);
     }
 
     public CacheWriter createCacheWriter(HttpServletRequest request, HttpServletResponse response, String contentType, String charset, boolean isPrivate) throws IOException
     {
         response.setContentType(contentType);
         response.setCharacterEncoding(charset);
         return new CacheWriter(request, response, isPrivate);
     }
 
     public CacheWriter createCacheWriter(HttpServletRequest request, HttpServletResponse response, String eTag, String contentType, String charset, boolean isPrivate) throws IOException
     {
         response.setContentType(contentType);
         response.setCharacterEncoding(charset);
         return new CacheWriter(request, response, eTag, isPrivate);
     }
 
     public CacheOutputStream createCacheOutputStream(HttpServletRequest request, HttpServletResponse response, String contentType, String charset, boolean isPrivate) throws IOException
     {
         response.setContentType(contentType);
         response.setCharacterEncoding(charset);
         return new CacheOutputStream(request, response, isPrivate);
     }
 
     public boolean sameETagOrCached(HttpServletRequest request, HttpServletResponse response) throws IOException
     {
         return sameETag(request, response) || serveFromCache(request, response);
     }
 
     public boolean serveFromCache(HttpServletRequest request, HttpServletResponse response) throws IOException
     {
         String cacheKey = getCacheKey(request);
         CachedContent cachedContent = (CachedContent) cache.get(cacheKey);
         if (cachedContent != null)
         {
             System.err.println("serve " + cacheKey);
             cachedContent.serve(response);
             return true;
         }
         else
         {
             return false;
         }
     }
 
     public boolean sameETag(HttpServletRequest request, HttpServletResponse response) throws IOException
     {
         String ifNoneMatch = request.getHeader("If-None-Match");
         if (ifNoneMatch != null)
         {
             if (!changedETAG(ifNoneMatch))
             {
                 response.sendError(HttpServletResponse.SC_NOT_MODIFIED);
                 return true;
             }
         }
         return false;
     }
 
     public String getCacheKey(HttpServletRequest request)
     {
         String cacheKey = MaidenheadLocator2.getCacheKey(request);
         if (cacheKey != null)
         {
             return cacheKey;
         }
         StringBuilder sb = new StringBuilder();
         String ServletPath = request.getServletPath();
         if (ServletPath != null)
         {
             sb.append(ServletPath);
         }
         String pathInfo = request.getPathInfo();
         if (pathInfo != null)
         {
             sb.append(pathInfo);
         }
         String queryString = request.getQueryString();
         if (queryString != null)
         {
             sb.append('?' + queryString);
         }
         return sb.toString();
     }
 
     public Entity getPageEntity(String path)
     {
         try
         {
             return get(KeyFactory.createKey(getRootKey(), PageKind, path));
         }
         catch (EntityNotFoundException ex)
         {
             return null;
         }
     }
 
     public void setPage(Entity page)
     {
         assert PageKind.equals(page.getKind());
         Transaction tr = beginTransaction();
         try
         {
             Entity backup = new Entity(PageBackupKind, System.currentTimeMillis(), page.getKey());
             backup.setPropertiesFrom(page);
             put(backup);
             put(page);
             tr.commit();
         }
         finally
         {
             if (tr.isActive())
             {
                 tr.rollback();
             }
         }
     }
 
     public Key getBlogKey(String messageId)
     {
         return KeyFactory.createKey(getRootKey(), BlogKind, messageId);
     }
 
     public Key getTrackKey(String guid)
     {
         return KeyFactory.createKey(getRootKey(), TrackKind, guid);
     }
 
     public Entity getBlogFromMessageId(String messageId)
     {
         Key key = getBlogKey(messageId);
         try
         {
             return get(key);
         }
         catch (EntityNotFoundException ex)
         {
             return new Entity(key);
         }
     }
 
     public Key getMetadataKey(String sha1)
     {
         return KeyFactory.createKey(getRootKey(), MetadataKind, sha1);
     }
 
     public Entity getMetadata(String sha1)
     {
         Key metadataKey = getMetadataKey(sha1);
         try
         {
             return get(metadataKey);
         }
         catch (EntityNotFoundException ex)
         {
             return new Entity(metadataKey);
         }
     }
 
     public void writeRSS(URL base, CacheWriter cw) throws IOException, HttpException
     {
         try
         {
             URI baseUri = base.toURI();
             Settings settings = getSettings();
             RSS rss = new RSS();
             Channel channel = new Channel();
             rss.getChannel().add(channel);
             channel.setTitle(settings.getTitle());
             channel.setDescription(settings.getDescription());
             channel.setLanguage(settings.getLocale().getLanguage());
             Date last = new Date(0);
             Query query = new Query(BlogKind);
             query.addSort(DateProperty, Query.SortDirection.DESCENDING);
             PreparedQuery prepared = prepare(query);
             for (Entity blog : prepared.asIterable())
             {
                 String subject = (String) Objects.nonNull(blog.getProperty(SubjectProperty));
                 Date date = (Date) Objects.nonNull(blog.getProperty(DateProperty));
                 if (date.after(last))
                 {
                     last = date;
                 }
                 Email sender = (Email) Objects.nonNull(blog.getProperty(SenderProperty));
                 String nickname = getNickname(sender);
                 Settings bloggerSettings = getSettingsFor(sender);
                 nickname = bloggerSettings.getNickname();
                 Item item = new Item();
                 item.setAuthor(nickname);
                 item.setTitle(subject);
                 item.setPubDate(date);
                 URI uri = baseUri.resolve("/index.html?blog=" + KeyFactory.keyToString(blog.getKey()));
                 item.setLink(uri);
                 channel.getItem().add(item);
             }
             channel.setPubDate(last);
             channel.setLastBuildDate(last);
             rss.marshall(cw);
             cw.cache();
         }
         catch (URISyntaxException ex)
         {
             throw new IllegalArgumentException(ex);
         }
     }
 
     public void getBlogList(String blogCursor, URL base, boolean all, CacheWriter sb) throws HttpException, IOException
     {
         Date begin = null;
         Date end = null;
         Cursor cursor = null;
         if (blogCursor != null)
         {
             BlogCursor bc = new BlogCursor(blogCursor);
             if (bc.isSearch())
             {
                 Searches.getBlogListFromSearch(bc, base, sb);
                 return;
             }
             begin = bc.getBegin();
             end = bc.getEnd();
             cursor = bc.getDatastoreCursor();
         }
         Settings settings = getSettings();
         FetchOptions options = FetchOptions.Builder.withDefaults();
         if (!all)
         {
             options.limit(settings.getShowCount());
         }
         if (cursor != null)
         {
             options = options.startCursor(cursor);
         }
         Query query = new Query(BlogKind);
         query.addSort(DateProperty, Query.SortDirection.DESCENDING);
         List<Filter> filters = new ArrayList<Filter>();
         filters.add(new FilterPredicate(PublishProperty, Query.FilterOperator.EQUAL, true));
         if (begin != null)
         {
             filters.add(new FilterPredicate(DateProperty, Query.FilterOperator.GREATER_THAN_OR_EQUAL, begin));
         }
         if (end != null)
         {
             filters.add(new FilterPredicate(DateProperty, Query.FilterOperator.LESS_THAN_OR_EQUAL, end));
         }
         if (filters.size() == 1)
         {
             query.setFilter(filters.get(0));
         }
         else
         {
             if (filters.size() > 1)
             {
                 CompositeFilter filter = new CompositeFilter(CompositeFilterOperator.AND, filters);
                 query.setFilter(filter);
             }
         }
         System.err.println(query);
         PreparedQuery prepared = prepare(query);
         QueryResultList<Entity> list = prepared.asQueryResultList(options);
         cursor = list.getCursor();
         BlogCursor bc = new BlogCursor()
                 .setDatastoreCursor(cursor)
                 .setBegin(begin)
                 .setEnd(end);
         for (Entity entity : list)
         {
             sb.append(getBlog(entity, base));
         }
         if (list.size() == settings.getShowCount())
         {
             sb.append("<span id=\"nextPage\" class=\"hidden\">" + bc.getWebSafe() + "</span>");
         }
         sb.cache();
     }
 
     public void getComments(Key blogKey, User loggedUser, CacheWriter cw) throws HttpException, IOException
     {
         Entity blog;
         try
         {
             blog = get(blogKey);
         }
         catch (EntityNotFoundException ex)
         {
             throw new HttpException(HttpServletResponse.SC_NOT_FOUND, blogKey + " not found");
         }
         Email sender = (Email) Objects.nonNull(blog.getProperty(SenderProperty));
         Settings senderSettings;
         senderSettings = Objects.nonNull(getSettingsFor(sender));
         String commentTemplate = senderSettings.getCommentTemplate();
         Locale locale = senderSettings.getLocale();
         DateFormat dateFormat = senderSettings.getDateFormat();
         Query query = new Query(CommentsKind);
         query.setAncestor(blogKey);
         query.addSort(TimestampProperty, Query.SortDirection.ASCENDING);
         PreparedQuery prepared = prepare(query);
         for (Entity comment : prepared.asIterable())
         {
             User user = (User) comment.getProperty(UserProperty);
             Date date = (Date) comment.getProperty(TimestampProperty);
             Text text = (Text) comment.getProperty(CommentProperty);
             String hidden = "hidden";
             if (user.equals(loggedUser))
             {
                 hidden = "";
             }
             if (user != null && text != null && date != null)
             {
                 String nickname = getNickname(user);
                 String dateString = dateFormat.format(date);
                 cw.append(String.format(locale, commentTemplate, nickname, dateString, text.getValue(), hidden, KeyFactory.keyToString(comment.getKey())));
             }
         }
         cw.cache();
     }
 
     public void addComment(Key blogKey, User user, String text)
     {
         text = text.replace("<", "&lt;");
         text = text.replace(">", "&gt;");
         Entity comment = new Entity(CommentsKind, blogKey);
         comment.setProperty(UserProperty, user);
         comment.setProperty(CommentProperty, new Text(text));
         comment.setProperty(TimestampProperty, new Date());
         put(comment);
         sendMailToAdmins(user.getEmail()+" added comment", text);
     }
 
     public void removeComment(Key commentKey, User currentUser) throws HttpException
     {
         try
         {
             Entity comment = get(commentKey);
             User user = (User) comment.getProperty(UserProperty);
             if (currentUser.equals(user))
             {
                 delete(commentKey);
             }
             else
             {
                 throw new HttpException(HttpServletResponse.SC_FORBIDDEN);
             }
         }
         catch (EntityNotFoundException ex)
         {
             throw new HttpException(HttpServletResponse.SC_NOT_FOUND);
         }
         sendMailToAdmins(currentUser.getEmail()+" removed comment", "");
     }
 
     private void sendMailToAdmins(String subject, String message)
     {
         Properties props = new Properties();
         Session session = Session.getDefaultInstance(props, null);
 
         try
         {
             Message msg = new MimeMessage(session);
             String appId = ApiProxy.getCurrentEnvironment().getAppId();
             msg.setFrom(new InternetAddress("admin@"+appId+".appspotmail.com"));
             msg.addRecipient(Message.RecipientType.TO,
                     new InternetAddress("admins"));
             msg.setSubject(subject);
             msg.setText(message);
             Transport.send(msg);
 
         }
         catch (AddressException e)
         {
             e.printStackTrace();
         }
         catch (MessagingException e)
         {
             e.printStackTrace();
         }
     }    
 
     private String getNickname(Email email)
     {
         return getNickname(email.getEmail());
     }
 
     private String getNickname(User user)
     {
         return getNickname(user.getEmail());
     }
 
     private String getNickname(String email)
     {
         int idx = email.indexOf('@');
         if (idx == -1)
         {
             throw new IllegalArgumentException(email + " not email");
         }
         String name = email.substring(0, idx);
         idx = name.lastIndexOf('.');
         if (idx != -1)
         {
             return name.substring(0, idx);
         }
         else
         {
             return name;
         }
     }
 
     public void getBlog(Key key, URL base, CacheWriter cw) throws HttpException, IOException
     {
         Entity entity;
         try
         {
             entity = get(key);
         }
         catch (EntityNotFoundException ex)
         {
             throw new HttpException(HttpServletResponse.SC_NOT_FOUND, key + " not found");
         }
         cw.append(getBlog(entity, base));
         cw.cache();
     }
 
     public String getBlog(Entity entity, URL base) throws HttpException, IOException
     {
         String subject = (String) Objects.nonNull(entity.getProperty(SubjectProperty));
         Date date = (Date) Objects.nonNull(entity.getProperty(DateProperty));
         Email sender = (Email) Objects.nonNull(entity.getProperty(SenderProperty));
         Text body = (Text) Objects.nonNull(entity.getProperty(HtmlProperty));
         return getBlog(sender, subject, date, body.getValue(), KeyFactory.keyToString(entity.getKey()), base);
     }
 
     public String getBlog(String sender, String subject, Date date, String body, String key, URL base) throws HttpException
     {
         return getBlog(new Email(sender), subject, date, body, key, base);
     }
 
     public String getBlog(Email sender, String subject, Date date, String body, String key, URL base) throws HttpException
     {
         Settings senderSettings = Objects.nonNull(getSettingsFor(sender));
         Locale locale = senderSettings.getLocale();
         DateFormat dateFormat = senderSettings.getDateFormat();
         String dateString = dateFormat.format(date);
         return String.format(locale, senderSettings.getBlogTemplate(), subject, dateString, senderSettings.getNickname(), body, base.toString(), key);
     }
 
     public void getCalendar(CacheWriter cw) throws IOException
     {
         Settings settings = getSettings();
         Locale locale = settings.getLocale();
         DateFormatSymbols dfs = new DateFormatSymbols(locale);
         String[] months = dfs.getMonths();
         Calendar calendar = Calendar.getInstance(locale);
         Map<Integer, Map<Integer, List<Entity>>> yearMap = new TreeMap<Integer, Map<Integer, List<Entity>>>(Collections.reverseOrder());
         Query query = new Query(BlogKind);
         query.setFilter(new FilterPredicate(PublishProperty, Query.FilterOperator.EQUAL, true));
         query.addSort(DateProperty, Query.SortDirection.DESCENDING);
         PreparedQuery prepared = prepare(query);
         for (Entity entity : prepared.asIterable(FetchOptions.Builder.withDefaults()))
         {
             String subject = (String) Objects.nonNull(entity.getProperty(SubjectProperty));
             Date date = (Date) Objects.nonNull(entity.getProperty(DateProperty));
             Email sender = (Email) Objects.nonNull(entity.getProperty(SenderProperty));
             Text body = (Text) Objects.nonNull(entity.getProperty(HtmlProperty));
             calendar.setTime(date);
             int year = calendar.get(Calendar.YEAR);
             int month = calendar.get(Calendar.MONTH);
             Map<Integer, List<Entity>> monthMap = yearMap.get(year);
             if (monthMap == null)
             {
                 monthMap = new TreeMap<Integer, List<Entity>>(Collections.reverseOrder());
                 yearMap.put(year, monthMap);
             }
             List<Entity> list = monthMap.get(month);
             if (list == null)
             {
                 list = new ArrayList<Entity>();
                 monthMap.put(month, list);
             }
             list.add(entity);
             //list.add("<div class=\"blog-entry\" id=\""+KeyFactory.keyToString(entity.getKey())+"\">"+subject+"</div>");
         }
         for (int year : yearMap.keySet())
         {
             Map<Integer, List<Entity>> monthMap = yearMap.get(year);
             int yearCount = 0;
             for (List<Entity> list : monthMap.values())
             {
                 yearCount += list.size();
             }
             cw.append("<div id=\"year" + year + "\" class=\"calendar-menu\">" + year + " (" + yearCount + ")</div>\n");
             cw.append("<div class=\"hidden year" + year + " calendar-indent\">\n");
             for (int month : monthMap.keySet())
             {
                 List<Entity> blogList = monthMap.get(month);
                 Date end = (Date) Objects.nonNull(blogList.get(0).getProperty(DateProperty));
                 Date begin = (Date) Objects.nonNull(blogList.get(blogList.size() - 1).getProperty(DateProperty));
                 BlogCursor bc = new BlogCursor()
                         .setBegin(begin)
                         .setEnd(end);
                 String monthId = bc.getWebSafe();
                 cw.append("<div id=\"" + monthId + "\" class=\"calendar-menu\">" + prettify(months[month]) + " (" + blogList.size() + ")</div>\n");
                 cw.append("<div class=\"hidden " + monthId + " calendar-indent\">\n");
                 for (Entity entity : blogList)
                 {
                     String subject = (String) Objects.nonNull(entity.getProperty(SubjectProperty));
                     cw.append("<div class=\"blog-entry\" id=\"" + KeyFactory.keyToString(entity.getKey()) + "\">" + subject + "</div>");
                 }
                 cw.append("</div>\n");
             }
             cw.append("</div>\n");
             yearCount = 0;
         }
         cw.cache();
     }
 
     private String prettify(String str)
     {
         return str.substring(0, 1).toUpperCase() + str.substring(1);
     }
 
     public Settings getSettings()
     {
         try
         {
             Key key = KeyFactory.createKey(getRootKey(), SettingsKind, BaseKey);
             Entity entity = get(key);
             if (entity != null)
             {
                 return new Settings(this, entity);
             }
             else
             {
                 throw new IllegalArgumentException("Root Settings not found");
             }
         }
         catch (EntityNotFoundException ex)
         {
             throw new IllegalArgumentException(ex);
         }
     }
 
     public Settings getSettingsFor(Email email) throws HttpException
     {
         try
         {
             Key parent = KeyFactory.createKey(getRootKey(), SettingsKind, BaseKey);
             Key key = KeyFactory.createKey(parent, SettingsKind, email.getEmail());
             Entity entity = get(key);
             if (entity != null)
             {
                 return new Settings(this, entity);
             }
             else
             {
                 throw new IllegalArgumentException(email.getEmail());
             }
         }
         catch (EntityNotFoundException ex)
         {
             throw new HttpException(HttpServletResponse.SC_CONFLICT, "settings for " + email.getEmail() + " not found");
         }
     }
 
     public void deleteWithChilds(Key key)
     {
         Transaction tr = beginTransaction(TransactionOptions.Builder.withXG(true));
         try
         {
             Query query = new Query();
             query.setAncestor(key);
             query.setKeysOnly();
             PreparedQuery prepared = prepare(query);
             for (Entity entity : prepared.asIterable())
             {
                 delete(entity.getKey());
             }
             delete(key);
             tr.commit();
         }
         finally
         {
             if (tr.isActive())
             {
                 tr.rollback();
             }
         }
     }
 
     public void saveBlog(Entity blog)
     {
         assert BlogKind.equals(blog.getKind());
         put(blog);
         Searches.saveBlog(blog);
     }
 
     public void getKeywordSelect(CacheWriter cw) throws IOException
     {   // TODO performance
         Query query = new Query(BlogKind);
 
         PreparedQuery prepared = prepare(query);
         Map<String, Integer> map = new TreeMap<String, Integer>();
         for (Entity blog : prepared.asIterable())
         {
             Collection<String> kwSet = (Collection<String>) blog.getProperty(KeywordsProperty);
             if (kwSet != null)
             {
                 for (String kw : kwSet)
                 {
                     if (kw != null && !kw.isEmpty())
                     {
                         Integer count = map.get(kw);
                         if (count == null)
                         {
                             map.put(kw, 1);
                         }
                         else
                         {
                             map.put(kw, count + 1);
                         }
                     }
                 }
             }
         }
         cw.append("<select id=\"keywordSelect\">");
         cw.append("<option>------</option>");
         for (String kw : map.keySet())
         {
             cw.append("<option value=\"Keyword:" + kw + "\">" + kw + "(" + map.get(kw) + ")</option>");
         }
         cw.append("</select>");
         cw.cache();
     }
 
     public void handleBlogAction(Key blogKey, String action, String auth, CacheWriter cw) throws HttpException, IOException
     {
         try
         {
             Entity blog = get(blogKey);
             String subject = (String) blog.getProperty(SubjectProperty);
             String digest = getBlogDigest(blog);
             if (!digest.equals(auth))
             {
                 System.err.println("digest not match " + digest + " <> " + auth + " request is illegal or blog has been changed");
                 throw new HttpException(HttpServletResponse.SC_FORBIDDEN);
             }
             if (PublishParameter.equals(action))
             {
                 blog.setProperty(PublishProperty, true);
                 put(blog);
                 cw.append("Blog " + subject + " has been published!");
             }
             else
             {
                 if (RemoveParameter.equals(action))
                 {
                     deleteWithChilds(blogKey);
                     cw.append("Blog " + subject + " has been deleted!");
                 }
                 else
                 {
                     throw new HttpException(HttpServletResponse.SC_CONFLICT, action + " unknown");
                 }
             }
         }
         catch (EntityNotFoundException ex)
         {
             throw new HttpException(HttpServletResponse.SC_NOT_FOUND, blogKey + " not found");
         }
     }
 
     public void updateKml(MaidenheadLocator2[] bb, URL reqUrl, CacheOutputStream outputStream) throws IOException
     {
         KMZ kmz = new KMZ();
         ObjectFactory factory = kmz.getFactory();
         DatatypeFactory dtFactory = kmz.getDtFactory();
         JAXBElement<DocumentType> document = factory.createDocument(factory.createDocumentType());
         kmz.getKml().getValue().setAbstractFeatureGroup(document);
         // styles
         setStyles(document, factory);
         // placemarks
         Entity firstPlacemark = null;
         Entity lastPlacemark = null;
         for (Entity placemark : fetchPlacemarks())
         {
             System.err.println(placemark);
             PlacemarkType placemarkType = factory.createPlacemarkType();
             populatePlacemark(placemarkType, placemark, factory, dtFactory);
 
             JAXBElement<PlacemarkType> pm = factory.createPlacemark(placemarkType);
             document.getValue().getAbstractFeatureGroup().add(pm);
             if (firstPlacemark == null)
             {
                 firstPlacemark = placemark;
             }
             lastPlacemark = placemark;
         }
         /*
         if (lastPlacemark != null)
         {
             GeoPt coordinate = (GeoPt) lastPlacemark.getProperty(LocationProperty);
             JAXBElement<LookAtType> lookAt = factory.createLookAt(factory.createLookAtType());
             lookAt.getValue().setLatitude((double) coordinate.getLatitude());
             lookAt.getValue().setLongitude((double) coordinate.getLongitude());
             lookAt.getValue().setRange(2000.0);
             Date lastTimestamp = (Date) lastPlacemark.getProperty(TimestampProperty);
             if (lastTimestamp != null)
             {
                 Date firstTimestamp = (Date) firstPlacemark.getProperty(TimestampProperty);
                 if (firstPlacemark != null)
                 {
                     TimeSpanType timeSpanType = factory.createTimeSpanType();
                     GregorianCalendar cal1 = new GregorianCalendar();
                     cal1.setTime(firstTimestamp);
                     XMLGregorianCalendar xCal1 = dtFactory.newXMLGregorianCalendar(cal1);
                     timeSpanType.setBegin(xCal1.toXMLFormat());
                     GregorianCalendar cal2 = new GregorianCalendar();
                     cal2.setTime(lastTimestamp);
                     XMLGregorianCalendar xCal2 = dtFactory.newXMLGregorianCalendar(cal2);
                     timeSpanType.setEnd(xCal2.toXMLFormat());
                     lookAt.getValue().getAbstractViewObjectExtensionGroup().add(factory.createTimeSpan(timeSpanType));
                 }
             }
             document.getValue().setAbstractViewGroup(lookAt);
         }
         */
         kmz.write(outputStream);
     }
     /**
      * @deprecated 
      * @param document
      * @param factory 
      */
     private void setStyles(JAXBElement<DocumentType> document, ObjectFactory factory)
     {
         Settings settings = getSettings();
         List<JAXBElement<? extends AbstractStyleSelectorType>> abstractStyleSelectorGroup = document.getValue().getAbstractStyleSelectorGroup();
         for (SpotType type : SpotType.values())
         {
             JAXBElement<StyleType> style = factory.createStyle(factory.createStyleType());
             abstractStyleSelectorGroup.add(style);
             style.getValue().setId(SpotType.getSpotStyleId(type));
 
             BalloonStyleType balloonStyle = factory.createBalloonStyleType();
             balloonStyle.setText("$[name]<div>$[description]</div>");
             style.getValue().setBalloonStyle(balloonStyle);
 
             LinkType icon = factory.createLinkType();
             icon.setHref(settings.getSpotIcon(type));
             IconStyleType iconStyle = factory.createIconStyleType();
             iconStyle.setIcon(icon);
             style.getValue().setIconStyle(iconStyle);
         }
     }
 
     public void writeLastPosition(CacheWriter cacheWriter) throws IOException
     {
         Settings settings = getSettings();
         Entity lastPlacemark = fetchLastPlacemark(settings);
         if (lastPlacemark != null)
         {
             Date timestamp = (Date) lastPlacemark.getProperty(TimestampProperty);
             GeoPt location = (GeoPt) lastPlacemark.getProperty(LocationProperty);
             String locator = (String) lastPlacemark.getProperty(SubsquareProperty);
             if (timestamp != null && location != null)
             {
                 cacheWriter.append("<div>");
                 DateFormat dateFormat = settings.getDateFormat();
                 cacheWriter.append(dateFormat.format(timestamp));
                 cacheWriter.append("</div>");
                 cacheWriter.append("<div>");
                 String locationString = GeoPtType.getString(location);
                 cacheWriter.append(locationString);
                 cacheWriter.append("</div>");
                 cacheWriter.append("<div title=\"Maidenhead Locator\">");
                 cacheWriter.append(locator);
                 cacheWriter.append("</div>");
                 cacheWriter.cache();
             }
         }
     }
 
     public Entity fetchLastPlacemark(Settings settings)
     {
         RunInNamespace<Entity> rin = new RunInNamespace()
         {
             @Override
             protected Entity run()
             {
                 Query placemarkQuery = new Query(PlacemarkKind);
                 placemarkQuery.addSort(TimestampProperty, Query.SortDirection.DESCENDING);
                 System.err.println(placemarkQuery);
                 PreparedQuery placemarkPrepared = prepare(placemarkQuery);
                 for (Entity placemark : placemarkPrepared.asIterable(FetchOptions.Builder.withLimit(1)))
                 {
                     return placemark;
                 }
                 return null;
             }
         };
         return rin.doIt(null, settings.isCommonPlacemarks());
     }
 
     public List<Entity> fetchPlacemarks()
     {
         Settings settings = getSettings();
         RunInNamespace<List<Entity>> rin = new RunInNamespace()
         {
             @Override
             protected List<Entity> run()
             {
                 Query placemarkQuery = new Query(PlacemarkKind);
                 placemarkQuery.addSort(TimestampProperty);
                 System.err.println(placemarkQuery);
                 PreparedQuery placemarkPrepared = prepare(placemarkQuery);
                 return placemarkPrepared.asList(FetchOptions.Builder.withDefaults());
             }
         };
         return rin.doIt(null, settings.isCommonPlacemarks());
     }
 
     public Entity findBlogFor(Entity placemark)
     {
         Query blogQuery = new Query(BlogKind);
         List<Filter> blogFilters = new ArrayList<>();
         blogFilters.add(new FilterPredicate(PublishProperty, Query.FilterOperator.EQUAL, true));
         List<Date> timestamp = getTimestamp(placemark);
         if (timestamp.isEmpty())
         {
             return null;
         }
         else
         {
             if (timestamp.size() == 1)
             {
                 blogFilters.add(new FilterPredicate(DateProperty, Query.FilterOperator.GREATER_THAN_OR_EQUAL, addHours(timestamp.get(0), -12)));
                 blogFilters.add(new FilterPredicate(DateProperty, Query.FilterOperator.LESS_THAN_OR_EQUAL, addHours(timestamp.get(0), 12)));
             }
             else
             {
                 blogFilters.add(new FilterPredicate(DateProperty, Query.FilterOperator.GREATER_THAN_OR_EQUAL, timestamp.get(0)));
                 blogFilters.add(new FilterPredicate(DateProperty, Query.FilterOperator.LESS_THAN_OR_EQUAL, timestamp.get(timestamp.size() - 1)));
             }
         }
         blogQuery.setFilter(new CompositeFilter(CompositeFilterOperator.AND, blogFilters));
         System.err.println(blogQuery);
         PreparedQuery blogPrepared = prepare(blogQuery);
         Entity blog = null;
         for (Entity b : blogPrepared.asIterable(FetchOptions.Builder.withDefaults()))
         {
             if (blog == null)
             {
                 blog = b;
             }
             else
             {
                 if (timeDiff(placemark, blog) > timeDiff(placemark, b))
                 {
                     blog = b;
                 }
             }
         }
         return blog;
     }
 
     public Entity findPlacemarkFor(Entity blog)
     {
         Query placemarkQuery = new Query(PlacemarkKind);
         List<Filter> placemarkFilters = new ArrayList<>();
         List<Date> timestamp = getTimestamp(blog);
         if (timestamp.isEmpty())
         {
             return null;
         }
         else
         {
             if (timestamp.size() == 1)
             {
                 placemarkFilters.add(new FilterPredicate(TimestampProperty, Query.FilterOperator.GREATER_THAN_OR_EQUAL, addHours(timestamp.get(0), -12)));
                 placemarkFilters.add(new FilterPredicate(TimestampProperty, Query.FilterOperator.LESS_THAN_OR_EQUAL, addHours(timestamp.get(0), 12)));
             }
             else
             {
                 placemarkFilters.add(new FilterPredicate(TimestampProperty, Query.FilterOperator.GREATER_THAN_OR_EQUAL, timestamp.get(0)));
                 placemarkFilters.add(new FilterPredicate(TimestampProperty, Query.FilterOperator.LESS_THAN_OR_EQUAL, timestamp.get(timestamp.size() - 1)));
             }
         }
         placemarkQuery.setFilter(new CompositeFilter(CompositeFilterOperator.AND, placemarkFilters));
         System.err.println(placemarkQuery);
         PreparedQuery placemarkPrepared = prepare(placemarkQuery);
         Entity placemark = null;
         for (Entity b : placemarkPrepared.asIterable(FetchOptions.Builder.withDefaults()))
         {
             if (placemark == null)
             {
                 placemark = b;
             }
             else
             {
                 if (timeDiff(placemark, placemark) > timeDiff(placemark, b))
                 {
                     placemark = b;
                 }
             }
         }
         return placemark;
     }
 
     private long timeDiff(Entity e1, Entity e2)
     {
         List<Date> ts1 = getTimestamp(e1);
         if (ts1 == null || ts1.isEmpty())
         {
             return Long.MAX_VALUE;
         }
         List<Date> ts2 = getTimestamp(e2);
         if (ts2 == null || ts2.isEmpty())
         {
             return Long.MAX_VALUE;
         }
         return Math.abs(center(ts1) - center(ts2));
     }
 
     private long center(List<Date> ts)
     {
         return (ts.get(0).getTime() + ts.get(ts.size() - 1).getTime()) / 2;
     }
 
     private Date addHours(Date date, int hours)
     {
         Calendar cal = Calendar.getInstance();
         cal.setTime(date);
         cal.add(Calendar.HOUR_OF_DAY, hours);
         return cal.getTime();
     }
 
     private void populatePlacemark(PlacemarkType placemarkType, Entity placemark, ObjectFactory factory, DatatypeFactory dtFactory)
     {
         String description = (String) placemark.getProperty(DescriptionProperty);
         SpotType spotType = SpotType.getSpotType(description);
         placemarkType.setStyleUrl("#"+SpotType.getSpotStyleId(spotType));
         String title = (String) placemark.getProperty(TitleProperty);
         //placemarkType.setName(title);
         //placemarkType.setDescription(description);
         String id = KeyFactory.keyToString(placemark.getKey());
 
         placemarkType.setId(id);
 
         GeoPt location = (GeoPt) placemark.getProperty(LocationProperty);
         if (location != null)
         {
             PointType pointType = factory.createPointType();
             pointType.getCoordinates().add(String.format(Locale.US, "%1$f,%2$f,0", location.getLongitude(), location.getLatitude()));
             placemarkType.setAbstractGeometryGroup(factory.createPoint(pointType));
         }
         /*
         Date timestamp = (Date) placemark.getProperty(TimestampProperty);
         if (timestamp != null)
         {
             TimeStampType timeStampType = factory.createTimeStampType();
             GregorianCalendar cal = new GregorianCalendar();
             cal.setTime(timestamp);
             XMLGregorianCalendar xCal = dtFactory.newXMLGregorianCalendar(cal);
             timeStampType.setWhen(xCal.toXMLFormat());
             placemarkType.setAbstractTimePrimitiveGroup(factory.createTimeStamp(timeStampType));
         }
         */
     }
 
     public static GeoPt center(Collection<GeoPt> list)
     {
         float lat = 0;
         float lon = 0;
         for (GeoPt pt : list)
         {
             lat += pt.getLatitude();
             lon += pt.getLongitude();
         }
         return new GeoPt(lat / list.size(), lon / list.size());
     }
     /**
      * @deprecated 
      * @param entity
      * @return 
      */
     public List<GeoPt> getCoordinates(Entity entity)
     {
         Object ob = entity.getProperty(LocationProperty);
         if (ob == null)
         {
             return Collections.EMPTY_LIST;
         }
         if (ob instanceof GeoPt)
         {
             GeoPt loc = (GeoPt) ob;
             return Collections.singletonList(loc);
         }
         return (List<GeoPt>) ob;
     }
     /**
      * @deprecated 
      * @param entity
      * @return 
      */
     private List<Date> getTimestamp(Entity entity)
     {
         Object ob = entity.getProperty(TimestampProperty);
         if (ob == null)
         {
             return Collections.EMPTY_LIST;
         }
         if (ob instanceof Date)
         {
             Date date = (Date) ob;
             return Collections.singletonList(date);
         }
         return (List<Date>) ob;
     }
 
     private String getCoordinatesString(Collection<GeoPt> coordinates)
     {
         StringBuilder sb = new StringBuilder();
         for (GeoPt location : coordinates)
         {
             if (sb.length() > 0)
             {
                 sb.append(' ');
             }
             sb.append(String.format(Locale.US, "%1$f,%2$f,0", location.getLongitude(), location.getLatitude()));
         }
         return sb.toString();
     }
 
     public void addPlacemark(final Date time, final GeoPt geoPt, final String title, final String description)
     {
         Entity placemark = null;
         Settings settings = getSettings();
         RunInNamespace<Entity> rin = new RunInNamespace()
         {
             @Override
             protected Entity run()
             {
                 Entity placemark = new Entity(PlacemarkKind, time.getTime(), getRootKey());
                 MaidenheadLocator2.setLocation(placemark, geoPt, MaidenheadLocator2.LocatorLevel.Field);
                 placemark.setProperty(LocationProperty, geoPt);
                 placemark.setProperty(TimestampProperty, time);
                 placemark.setUnindexedProperty(TitleProperty, title);
                 placemark.setUnindexedProperty(DescriptionProperty, description);
                 System.err.println(placemark);
                 put(placemark);
                 return placemark;
             }
         };
         placemark = rin.doIt(null, settings.isCommonPlacemarks());
         updateBlogCoordinate(placemark);
         clearPlacemarkCache();
     }
 
     public void addPlacemark(final Entity placemark)
     {
         Settings settings = getSettings();
         RunInNamespace<Entity> rin = new RunInNamespace()
         {
             @Override
             protected Entity run()
             {
                 put(placemark);
                 return placemark;
             }
         };
         rin.doIt(null, settings.isCommonPlacemarks());
         updateBlogCoordinate(placemark);
         clearPlacemarkCache();
     }
 
     public Entity createPlacemark()
     {
         Settings settings = getSettings();
         RunInNamespace<Entity> rin = new RunInNamespace()
         {
             @Override
             protected Entity run()
             {
                 return new Entity(PlacemarkKind, getRootKey());
             }
         };
         return rin.doIt(null, settings.isCommonPlacemarks());
     }
 
     private void updateBlogCoordinate(Entity placemark)
     {
         DS ds = DS.get();
         Entity blog = ds.findBlogFor(placemark);
         if (blog != null)
         {
             Entity placemarkFor = ds.findPlacemarkFor(blog);
             if (placemark.equals(placemarkFor))
             {
                 List<GeoPt> coordinates = ds.getCoordinates(placemark);
                 if (!coordinates.isEmpty())
                 {
                     blog.setProperty(LocationProperty, DS.center(coordinates));
                     put(blog);
                 }
             }
         }
     }
 
     private void clearPlacemarkCache()
     {
         RunForAllNamespaces rfan = new RunForAllNamespaces()
         {
             @Override
             protected void run(String namespace)
             {
                 DS ds = DS.get();
                 ds.cache.delete("/lastPosition");
             }
         };
         rfan.start(this);
     }
 
     public class CacheWriter extends Writer
     {
 
         private Writer out;
         private StringWriter stringWriter = new StringWriter();
         private HttpServletResponse response;
         private String eTag;
         private String cacheKey;
         private boolean isPrivate;
         private boolean cached;
 
         private CacheWriter(HttpServletRequest request, HttpServletResponse response, boolean isPrivate) throws IOException
         {
             this(request, response, getETag(), isPrivate);
         }
 
         private CacheWriter(HttpServletRequest request, HttpServletResponse response, String eTag, boolean isPrivate) throws IOException
         {
             out = response.getWriter();
             this.cacheKey = getCacheKey(request);
             this.response = response;
             this.eTag = eTag;
             this.isPrivate = isPrivate;
             response.setHeader("ETag", eTag);
             if (isPrivate)
             {
                 response.setHeader("Cache-Control", "private");
             }
             else
             {
                 response.setHeader("Cache-Control", "public");
             }
         }
 
         public void cache()
         {
             if (!isPrivate)
             {
                 String content = stringWriter.toString();
                 CachedContent cachedContent;
                 try
                 {
                     cachedContent = new CachedContent(content, response.getContentType(), response.getCharacterEncoding(), eTag, isPrivate);
                 }
                 catch (UnsupportedEncodingException ex)
                 {
                     throw new IllegalArgumentException(ex);
                 }
                 cache.put(cacheKey, cachedContent);
                 System.err.println("caching as "+cacheKey);
             }
             cached = true;
         }
 
         @Override
         public void write(char[] buf, int off, int len) throws IOException
         {
             out.write(buf, off, len);
             stringWriter.write(buf, off, len);
         }
 
         @Override
         public void flush() throws IOException
         {
         }
 
         @Override
         public void close() throws IOException
         {
             if (!cached)
             {
                 System.err.println("closing without caching");
             }
         }
     }
 
     public class CacheOutputStream extends OutputStream
     {
 
         private OutputStream out;
         private ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
         private HttpServletResponse response;
         private String eTag;
         private String cacheKey;
         private boolean isPrivate;
         private boolean cached;
 
         private CacheOutputStream(HttpServletRequest request, HttpServletResponse response, boolean isPrivate) throws IOException
         {
             this(request, response, getETag(), isPrivate);
         }
 
         private CacheOutputStream(HttpServletRequest request, HttpServletResponse response, String eTag, boolean isPrivate) throws IOException
         {
             out = response.getOutputStream();
             this.cacheKey = getCacheKey(request);
             this.response = response;
             this.eTag = eTag;
             this.isPrivate = isPrivate;
             response.setHeader("ETag", eTag);
             if (isPrivate)
             {
                 response.setHeader("Cache-Control", "private");
             }
             else
             {
                 response.setHeader("Cache-Control", "public");
             }
         }
 
         public void cache()
         {
             if (!isPrivate)
             {
                 byte[] content = byteStream.toByteArray();
                 CachedContent cachedContent;
                 cachedContent = new CachedContent(content, response.getContentType(), response.getCharacterEncoding(), eTag, isPrivate);
                 cache.put(cacheKey, cachedContent);
                 System.err.println("caching as "+cacheKey);
             }
             cached = true;
         }
 
         @Override
         public void write(int b) throws IOException
         {
             out.write(b);
             byteStream.write(b);
         }
 
         @Override
         public void close() throws IOException
         {
             if (!cached)
             {
                 System.err.println("closing without caching");
             }
         }
     }
 
     private static class NullOutputStream extends OutputStream
     {
 
         @Override
         public void write(int b) throws IOException
         {
         }
     }
 }
