 package pl.psnc.dl.wf4ever.notifications;
 
 import java.io.Serializable;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 import org.joda.time.DateTime;
 
 import pl.psnc.dl.wf4ever.model.RDF.Thing;
 import pl.psnc.dl.wf4ever.model.RO.ResearchObject;
 import pl.psnc.dl.wf4ever.monitoring.ChecksumVerificationJob.Mismatch;
 import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectComponentSerializable;
 import pl.psnc.dl.wf4ever.vocabulary.ORE;
 
 import com.hp.hpl.jena.vocabulary.DCTerms;
 import com.sun.syndication.feed.atom.Content;
 import com.sun.syndication.feed.atom.Entry;
 import com.sun.syndication.feed.atom.Link;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndLink;
 
 /**
  * Represents a simple entry of the feed, contains a simple report of the event like update, damage and so on.
  * 
  * @author pejot
  */
 @Entity
 @Table(name = "atom_feed_entries")
 public class Notification implements Serializable {
 
     /** Serialization. */
     private static final long serialVersionUID = 1L;
     /** Id. */
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private int id;
 
     /** Timestamp. */
     @Column(nullable = false)
     private Date created;
 
     /** Title. */
     private String title;
 
     /** URI of the service that created this notification. */
     private String source;
 
     /** Source human-readable name. */
     private String sourceName;
 
     /** Entry human-friendly content. */
     private String summary;
 
     /** Related object (for example Research Object uri). */
     @Column(nullable = false)
     private String subject;
 
 
     /**
      * Create an entry based on the builder. Used builder only.
      * 
      * @param builder
      *            the builder that has the fields to set
      */
     private Notification(Builder builder) {
         this.created = builder.created;
         this.title = builder.title;
         this.source = builder.source;
         this.sourceName = builder.sourceName;
         this.subject = builder.subject;
         this.summary = builder.summary;
         this.title = builder.title;
     }
 
 
     /**
      * Default constructor.
      */
     public Notification() {
     }
 
 
     public Integer getId() {
         return id;
     }
 
 
     public void setId(Integer id) {
         this.id = id;
     }
 
 
     public String getSource() {
         return source;
     }
 
 
     public void setSource(String source) {
         this.source = source;
     }
 
 
     /**
      * Set complete source.
      * 
      * @param source
      *            source
      * @param name
      *            source name
      */
     public void setSource(String source, String name) {
         this.source = source;
         this.sourceName = name;
     }
 
 
     public String getTitle() {
         return title;
     }
 
 
     public void setTitle(String title) {
         this.title = title;
     }
 
 
     public String getSummary() {
         return summary;
     }
 
 
     public void setSummary(String summary) {
         this.summary = summary;
     }
 
 
     public Date getCreated() {
         return created;
     }
 
 
     public void setCreated(Date created) {
         this.created = created;
     }
 
 
     public String getSubject() {
         return subject;
     }
 
 
     public void setSubject(String subject) {
         this.subject = subject;
     }
 
 
     /**
      * Convert this entry to a {@link Entry}.
      * 
      * @param feedUri
      *            the URI of this feed, used for example for resolving relative URIs
      * @return a new instance
      */
     public Entry asFeedEntry(URI feedUri) {
         Entry resultEntry = new Entry();
         resultEntry.setId("urn:X-rodl:" + id);
         if (title != null) {
             resultEntry.setTitle(title);
         }
         resultEntry.setCreated(created);
         resultEntry.setPublished(created);
         //set summary
         Content content = new Content();
         if (summary != null) {
             content.setValue(StringEscapeUtils.escapeHtml4(summary));
             content.setType(Content.HTML);
         }
         resultEntry.setSummary(content);
         //set links
         List<Link> links = new ArrayList<>();
         if (source != null) {
             Link sourceLink = new Link();
             sourceLink.setHref(feedUri != null ? feedUri.resolve(source).toString() : source.toString());
             sourceLink.setRel(DCTerms.source.toString());
             if (sourceName != null) {
                 sourceLink.setTitle(sourceName);
             } else {
                 sourceLink.setTitle(source);
             }
             links.add(sourceLink);
         }
         if (subject != null) {
             Link sourceLink = new Link();
             sourceLink.setHref(subject);
             sourceLink.setRel(ORE.describes.toString());
             sourceLink.setTitle("Description for");
             links.add(sourceLink);
         }
 
         resultEntry.setOtherLinks(links);
         return resultEntry;
     }
 
 
     /**
      * Generate a notification from given entry.
      * 
      * @param entry
      *            given feed entry
      * @return notification
      */
 
     public static Notification fromEntry(SyndEntry entry) {
         Notification notification = new Notification();
         notification.setTitle(entry.getTitle());
         notification.setCreated(entry.getPublishedDate());
         for (Object ob : entry.getLinks()) {
             SyndLink ln = (SyndLink) ob;
             if (ln.getRel().equals(DCTerms.source.getURI())) {
                 String sourceName;
                 if (ln.getTitle() != null) {
                     sourceName = ln.getTitle();
                 } else {
                     sourceName = ln.getHref();
                 }
                 notification.setSource(ln.getHref(), sourceName);
             } else if (ln.getRel().equals(ORE.describes.getURI())) {
                 notification.setSubject(ln.getHref());
             }
         }
         notification.setSummary(entry.getDescription().getValue());
         return notification;
     }
 
 
     /**
      * A builder class that allows to create a {@link Notification} using the builder design pattern.
      * 
      * @author piotrekhol
      * 
      */
     public static class Builder {
 
         /** Timestamp. */
         private Date created = DateTime.now().toDate();
 
         /** Title. */
         private String title;
 
         /** URI of the service that created this notification. */
         private String source;
 
         /** Source human-readable name. */
         private String sourceName;
 
         /** Entry human-friendly content. */
         private String summary;
 
         /** Related object (for example Research Object URI). */
         private String subject;
 
 
         /**
          * Constructor, all required fields are parameters.
          * 
          * @param subject
          *            Related object URI
          */
         public Builder(URI subject) {
             this.subject = subject.toString();
         }
 
 
         /**
          * Constructor, all required fields are parameters.
          * 
          * @param subject
          *            Related object (for example Research Object)
          */
         public Builder(Thing subject) {
             this(subject.getUri());
         }
 
 
         /**
          * Finish the build process and return an {@link Notification}.
          * 
          * @return an atom feed entry with fields provided using this builder
          */
         public Notification build() {
             return new Notification(this);
         }
 
 
         /**
          * Creation timestamp.
          * 
          * @param created
          *            timestamp
          * @return this builder
          */
         public Builder created(Date created) {
             this.created = created;
             return this;
         }
 
 
         /**
          * Creation timestamp.
          * 
          * @param created
          *            timestamp
          * @return this builder
          */
         public Builder created(DateTime created) {
             this.created = created.toDate();
             return this;
         }
 
 
         /**
          * Entry title.
          * 
          * @param title
          *            entry title
          * @return this builder
          */
         public Builder title(String title) {
             this.title = title;
             return this;
         }
 
 
         /**
          * URI of the service that created this notification.
          * 
          * @param source
          *            URI of the service that created this notification
          * @return this builder
          */
         public Builder source(URI source) {
             this.source = source.toString();
             return this;
         }
 
 
         /**
          * URI of the service that created this notification.
          * 
          * @param source
          *            URI of the service that created this notification
          * @return this builder
          */
         public Builder source(String source) {
             this.source = source;
             return this;
         }
 
 
         /**
          * Source human-readable name.
          * 
          * @param sourceName
          *            Source human-readable name
          * @return this builder
          */
         public Builder sourceName(String sourceName) {
             this.sourceName = sourceName;
             return this;
         }
 
 
         /**
          * Entry human-friendly content.
          * 
          * @param summary
          *            Entry human-friendly content
          * @return this builder
          */
         public Builder summary(String summary) {
             this.summary = summary;
             return this;
         }
 
     }
 
 
     /**
      * Commonly used entry titles.
      * 
      * @author piotrekhol
      * 
      */
     public static class Title {
 
         /**
          * An RO has been created.
          * 
          * @param researchObject
          *            the new RO
          * @return a title in plain text
          */
         public static String created(ResearchObject researchObject) {
             return String.format("Research Object %s has been created", researchObject.getName());
         }
 
 
         /**
          * An RO has been deleted.
          * 
          * @param researchObject
          *            the deleted RO
          * @return a title in plain text
          */
         public static String deleted(ResearchObject researchObject) {
             return String.format("Research Object %s has been deleted", researchObject.getName());
         }
 
 
         /**
          * A resource has been created.
          * 
          * @param component
          *            the resource that has been added
          * @return a title in plain text
          */
         public static String created(ResearchObjectComponentSerializable component) {
             return String.format("A resource has been added to the Research Object %s", component.getResearchObject()
                     .getName());
         }
 
 
         /**
          * A resource has been deleted.
          * 
          * @param component
          *            the resource that has been deleted
          * @return a title in plain text
          */
         public static String deleted(ResearchObjectComponentSerializable component) {
             return String.format("A resource has been deleted from the Research Object %s", component
                     .getResearchObject().getName());
         }
 
 
         /**
          * A resource has been updated.
          * 
          * @param component
          *            the resource that has been updated
          * @return a title in plain text
          */
         public static String updated(ResearchObjectComponentSerializable component) {
             return String.format("A resource has been updated in the Research Object %s", component.getResearchObject()
                     .getName());
         }
 
 
         /**
          * Checksum mismatches have been detected.
          * 
          * @param researchObject
          *            the RO
          * @return a title in plain text
          */
         public static String checksumMismatch(ResearchObject researchObject) {
             return String.format("Research Object %s has become corrupt!", researchObject.getName());
         }
     }
 
 
     /**
      * Commonly used entry content.
      * 
      * @author piotrekhol
      * 
      */
     public static class Summary {
 
         private static String wrap(String message) {
             return String.format("<html><body>%s</body></html>", message);
         }
 
 
         /**
          * An RO has been created.
          * 
          * @param researchObject
          *            the new RO
          * @return a message in HTML
          */
         public static String created(ResearchObject researchObject) {
             return wrap(String
                     .format(
                         "<p>A new Research Object has been created.</p><p>The Research Object URI is <a href=\"%s\">%<s</a>.</p>",
                         researchObject.toString()));
         }
 
 
         /**
          * An RO has been deleted.
          * 
          * @param researchObject
          *            the deleted RO
          * @return a message in HTML
          */
         public static String deleted(ResearchObject researchObject) {
             return wrap(String.format(
                 "<p>A Research Object has been deleted.</p><p>The Research Object URI was <em>%s</em>.</p>",
                 researchObject.toString()));
         }
 
 
         /**
          * A resource has been created.
          * 
          * @param component
          *            the resource that has been updated
          * @return a message in HTML
          */
         public static String created(ResearchObjectComponentSerializable component) {
             return wrap(String
                     .format(
                         "<p>A resource has been added to the Research Object.</p><ul><li>The Research Object: <a href=\"%s\">%<s</a>.</li><li>The resource: <a href=\"%s\">%<s</a>.</li></ul>",
                         component.getResearchObject().getUri().toString(), component.getUri().toString()));
         }
 
 
         /**
          * A resource has been deleted.
          * 
          * @param component
          *            the resource that has been deleted
          * @return a message in HTML
          */
         public static String deleted(ResearchObjectComponentSerializable component) {
             return wrap(String
                     .format(
                         "<p>A resource has been deleted from the Research Object.</p><ul><li>The Research Object: <a href=\"%s\">%<s</a>.</li><li>The resource: <em>%s</em>.</li></ul>",
                         component.getResearchObject().getUri().toString(), component.getUri().toString()));
         }
 
 
         /**
          * A resource has been updated.
          * 
          * @param component
          *            the resource that has been updated
          * @return a message in HTML
          */
         public static String updated(ResearchObjectComponentSerializable component) {
             return wrap(String
                     .format(
                         "<p>A resource has been updated in the Research Object.</p><ul><li>The Research Object: <a href=\"%s\">%<s</a>.</li><li>The resource: <a href=\"%s\">%<s</a>.</li></ul>",
                         component.getResearchObject().getUri().toString(), component.getUri().toString()));
         }
 
 
         /**
          * Checksum mismatches have been detected.
          * 
          * @param researchObject
          *            the RO
          * @param mismatches
          *            a collection of resource checksum mismatches
          * @return a message in HTML
          */
         public static String checksumMismatch(ResearchObject researchObject, Collection<Mismatch> mismatches) {
             StringBuilder sb = new StringBuilder();
             sb.append("<p>The following changes have been detected:</p>");
             sb.append("<ol>");
             for (Mismatch mismatch : mismatches) {
                 sb.append(String.format("<li>File %s: expected checksum %s, found %s</li>", mismatch.getResourcePath(),
                     mismatch.getExpectedChecksum(), mismatch.getCalculatedChecksum()));
             }
             sb.append("</ol>");
             return wrap(sb.toString());
         }
     }
 }
