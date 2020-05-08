 package com.google.buzz.model;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class BuzzComment
 {
     /**
      * The object type of the activity.
      */
     private String activityObjectType;
 
     /**
      * The comment id.
      */
     private String id;
 
     /**
      * The published date
      */
     private Date published;
 
     /**
      * The comment author
      */
     private BuzzAuthor author;
 
     /**
      * The comment content
      */
     private BuzzContent content;
 
     /**
      * The comment original content
      */
     private BuzzContent originalContent;
 
     /**
      * The comment links
      */
     private List<BuzzLink> links = new ArrayList<BuzzLink>( 0 );
 
     /**
      * The comment reply
      */
     private BuzzReply reply;
 
     /**
      * @return the activityObjectType
      */
     public String getActivityObjectType()
     {
         return activityObjectType;
     }
 
     /**
      * @param activityObjectType the activityObjectType to set
      */
     public void setActivityObjectType( String activityObjectType )
     {
         this.activityObjectType = activityObjectType;
     }
 
     /**
      * @return the id
      */
     public String getId()
     {
         return id;
     }
 
     /**
      * @param id the id to set
      */
     public void setId( String id )
     {
         this.id = id;
     }
 
     /**
      * @return the published
      */
     public Date getPublished()
     {
         return published;
     }
 
     /**
      * @param published the published to set
      */
     public void setPublished( Date published )
     {
         this.published = published;
     }
 
     /**
      * @return the author
      */
     public BuzzAuthor getAuthor()
     {
         return author;
     }
 
     /**
      * @param author the author to set
      */
     public void setAuthor( BuzzAuthor author )
     {
         this.author = author;
     }
 
     /**
      * @return the content
      */
     public BuzzContent getContent()
     {
         return content;
     }
 
     /**
      * @param content the content to set
      */
     public void setContent( BuzzContent content )
     {
         this.content = content;
     }
 
     /**
      * @return the originalContent
      */
     public BuzzContent getOriginalContent()
     {
         return originalContent;
     }
 
     /**
      * @param originalContent the originalContent to set
      */
     public void setOriginalContent( BuzzContent originalContent )
     {
         this.originalContent = originalContent;
     }
 
     /**
      * @return the links
      */
     public List<BuzzLink> getLinks()
     {
         return links;
     }
 
     /**
      * @param links the links to set
      */
     public void setLinks( List<BuzzLink> links )
     {
         this.links = links;
     }
 
     /**
      * @return the reply
      */
     public BuzzReply getReply()
     {
         return reply;
     }
 
     /**
      * @param reply the reply to set
      */
     public void setReply( BuzzReply reply )
     {
         this.reply = reply;
     }
 
     /**
      * Overwrite the default toString method
      * 
      * @return the string representation of the object
      */
     public String toString()
     {
         return toString( "\n" );
     }
 
     /**
      * Print the object in a pretty way.
      * 
      * @param indent to print the attributes
      * @return a formatted string representation of the object
      */
     public String toString( String indent )
     {
         StringBuilder sb = new StringBuilder();
         String newIndent = indent + "\t";
         sb.append( indent + "BuzzComment:" );
         sb.append( newIndent + "Activity Object Type: " + activityObjectType );
         sb.append( newIndent + "Published: " + published );
         sb.append( newIndent + "Id: " + id );
         sb.append( newIndent + "Author: " + author.toString( newIndent + "\t" ) );
        sb.append( newIndent + "Content: " + content.toString( newIndent + "\t" ) );
        sb.append( newIndent + "Original Content: " + originalContent.toString( newIndent + "\t" ) );
         for ( int i = 0; i < links.size(); i++ )
         {
             sb.append( links.get( i ).toString( newIndent + "\t" ) );
         }
         sb.append( newIndent + "Reply: " + reply.toString( newIndent + "\t" ) );
         return sb.toString();
     }
 }
