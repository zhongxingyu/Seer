 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.editablepages.domain;
 
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.Table;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 import javax.persistence.UniqueConstraint;
 
 import org.compass.annotations.Searchable;
 import org.compass.annotations.SearchableId;
 import org.compass.annotations.SearchableProperty;
 
 import org.apache.commons.lang.Validate;
 
 /** Defines a page that can be modified by a user.
  *
  * A page has a life cycle: once created, it starts its life as a dirty page
  * with no published content. Such a page is considered non-existent to users
  * without enough privileges.
  *
  * After the page is created, it must be published to be accessible by final
  * users.
  *
  * If a user modifies a page, the modified content is not yet accessible to the
  * final user until it is published.
  *
  * A page registers who was the last person to modify the page and at what time
  * it was last published.
  */
 @Entity
 @Table(name = "pages", uniqueConstraints
     = { @UniqueConstraint(columnNames = { "site_name", "name" }) })
 @Searchable
 public class Page {
 
   /** The length in characters of the page name.
    */
   private static final int PAGE_NAME_LENGTH = 50;
 
   /** The length in characters of the page title.
    */
   private static final int TITLE_LENGTH = 50;
 
   /** The length in characters of the creator's name.
    */
   private static final int CREATOR_NAME_LENGTH = 50;
 
   /** The length in characters of the page content.
    */
   private static final int CONTENT_LENGTH = 100000;
 
   /** The id of the page, 0 for a newly created page.
    */
   @Id
   @GeneratedValue(strategy = GenerationType.AUTO)
   @Column(name = "id", nullable = false)
   @SearchableId
   private long id = 0;
 
   /** The name of the site that the page belongs to.
    *
    * It is null only for a new page.
    */
   @SuppressWarnings("unused")
   @Column(name = "site_name", nullable = false)
   @SearchableProperty
   private String siteName;
 
   /** The name of the page.
    *
    * The page name is used in a way analogous to a physical file. It is never
    * null.
    */
   @Column(name = "name", nullable = false, unique = false,
       length = PAGE_NAME_LENGTH)
   @SearchableProperty
   private String name;
 
   /** The title of the page
    *
    * The title is inserted in the html title element.
    */
   @Column(name = "title", nullable = false, length = TITLE_LENGTH)
   @SearchableProperty
   private String title;
 
   /** The content of the page.
    *
    * This is the content that is shown to the user. It is null if the page was
    * created and not yet published.
    */
   @Column(name = "content", nullable = true, length = CONTENT_LENGTH)
   @SearchableProperty
   private String content = null;
 
   /** The unpublished content of the page (html code).
    */
   @Column(name = "unpublished_content", nullable = true, length =
       CONTENT_LENGTH)
   private String unpublishedContent;
 
   /** The date that the page was last published.
    *
    * It is null for new pages not yet published.
    */
   @Temporal(TemporalType.TIMESTAMP)
   @Column(name = "publication_date", nullable = true)
   private Date publicationDate = null;
 
   /** The name of the user that last modified the page.
    *
    * When the page is originally created, this is the user that created the
    * page. It is never null.
    */
   @Column(name = "modifier", nullable = false, length = CREATOR_NAME_LENGTH)
   @SearchableProperty
   private String modifier;
 
   /** The default constructor to make hibernate happy.
    */
   protected Page() {
   }
 
   /** A custom constructor.
    *
    * Builds a page with the most basic data it needs to have. The page starts
    * unpublished
    *
    * @param theName The page name. It cannot be null.
    *
    * @param theTitle The page title, shown in the html header. It cannot be
    * null.
    *
    * @param initialContent The page content, it cannot be null, it should be
    * html code.
    *
    * @param theCreator The page creator's name, he will be also the last
    * modifier of the page, it cannot be null. The user should exists
    */
   public Page(final String theCreator, final String theName,
       final String theTitle, final String initialContent) {
     Validate.notNull(theCreator, "The page creator cannot be null");
     Validate.notNull(theName, "The page name cannot be null");
     Validate.notNull(theTitle, "The page title cannot be null");
     Validate.notNull(initialContent, "The page content cannot be null");
     name = theName;
     unpublishedContent = initialContent;
     title = theTitle;
     modifier = theCreator;
   }
 
   /** Modifies the page.
    *
    * @param user the name of the user who modifies the page, it cannot be null
    *
    * @param newName The new name of the page. It cannot be null.
    *
    * @param newTitle The new title for the page. It cannot be null.
    *
    * @param newContent The new content of the page. It cannot be null It
    * becomes available for view when published, previous content is saved and
    * shown until published.
    */
   public void modify(final String user, final String newName,
       final String newTitle, final String newContent) {
     Validate.notNull(user, "The user who modifies the table cant be null");
     Validate.notNull(newName, "The page name cannot be null");
     Validate.notNull(newTitle, "The page title cannot be null");
     Validate.notNull(newContent, "The page content cannot be null");
     modifier = user;
     name = newName;
     title = newTitle;
     unpublishedContent = newContent;
   }
 
   /** Publishes the page, making the modified content available to the final
    * user.
    *
    * You can only publish dirty pages.
    */
   public void publish() {
     Validate.notNull(unpublishedContent);
     content = unpublishedContent;
     unpublishedContent = null;
     publicationDate = new Date();
   }
 
   /** Reverts the modification that are not published, if any.
    *
    * If the page has no unpublished content, this operation has no effect.
    */
   public void revert() {
     unpublishedContent = null;
   }
 
   /** Returns if the page has some modification that can be published.
    *
    * @return false if it has not.
    */
   public boolean isDirty() {
     return unpublishedContent != null;
   }
 
   /** Returns the id of the page.
    *
    * @return Returns the user id, 0 if the page was not persisted yet.
    */
   public long getId() {
     return id;
   }
 
   /** Returns the page name.
    *
    * @return returns the name which is never null.
    */
   public String getName() {
     return name;
   }
 
   /** Returns the page title.
   *
   * @return returns the title which is never null.
   */
  public String getTitle() {
    return title;
  }
 
   /** Returns the content of the page.
    *
   * TODO Decice if returning null is the best option. An empty string is also
    * a good choice.
    *
    * @return The published content. If it is a new page that was never
    * published, this operation returns null.
    */
   public String getContent() {
     return content;
   }
 
   /** Returns the content modified by an administrator that is not yet
    * published.
    *
    * @return The content that is not yet published. If it has not unpublished
    * content returns null.
    */
   public String getUnpublishedContent() {
     return unpublishedContent;
   }
 
   /** Returns the date that the page was last published.
    *
    * @return The date of the last publication, null for a new page that was
    * never published.
    */
   public Date getPublicationDate() {
     if (publicationDate == null) {
       return null;
     } else {
       return new Date(publicationDate.getTime());
     }
   }
 
   /** Returns the name of the last person who modified the page.
    *
    * @return the last person who modified the page, it is null if the page is
    * not dirty.
    */
   public String getModifier() {
     return modifier;
   }
 
   /**
    * Sets the name of the site that the page belongs to, it's only accessible by
    * the page repository.
    *
    * This is intended to be called from the page repository only.
    *
    * @param newSiteName it cannot be null.
    */
   void setSiteName(final String newSiteName) {
     Validate.notNull(newSiteName);
     siteName = newSiteName;
   }
 }
 
