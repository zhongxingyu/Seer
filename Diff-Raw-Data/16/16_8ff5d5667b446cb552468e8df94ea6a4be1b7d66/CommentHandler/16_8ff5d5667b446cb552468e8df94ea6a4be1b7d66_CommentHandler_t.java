 package com.google.buzz.parser.handler;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.XMLReader;
 
 import com.google.buzz.model.BuzzComment;
 import com.google.buzz.util.DateUtils;
 
 /**
  * Handler for xml element: <b>Feed Entry</b>
  * 
  * @author roberto.estivill
  */
 public class CommentHandler
     extends BaseHandler
 {
     /**
      * XML elements handled by this handler
      */
     private static final String ENTRY = "entry";
     private static final String ACTIVITY_OBJECT_TYPE = "activity:object-type";
     private static final String PUBLISHED = "published";
     private static final String ID = "id";
     private static final String LINK = "link";
     private static final String AUTHOR = "author";
     private static final String CONTENT = "content";
     private static final String ORIGINAL_CONTENT = "buzz:original-content";
     private static final String IN_REPLY_TO = "thr:in-reply-to";
 
     /**
      * Position flags
      */
     private boolean insidePublished = false;
     private boolean insideId = false;
     private boolean insideActivityObjectType = false;
 
     /**
      * Children handlers
      */
     private LinkHandler linkHandler;
     private AuthorHandler authorHandler;
     private ContentHandler contentHandler;
     private ReplyHandler replyHandler;
 
     /**
      * Object to return
      */
     private BuzzComment comment;
 
     /**
      * Constructor method to create a child handler.
      * 
      * @param parentHandler handler
      */
     public CommentHandler( BaseHandler aParent )
     {
         super( aParent );
         comment = new BuzzComment();
     }
 
     /**
      * Constructor method to create a root handler.
      * 
      * @param parentHandler handler
      */
     public CommentHandler( XMLReader xmlReader )
     {
         super( xmlReader );
         comment = new BuzzComment();
     }
 
     /**
      * @return the created entry object
      */
     public BuzzComment getBuzzComment()
     {
         return comment;
     }
 
     /**
      * Method to be called every time an xml element starts
      */
     public void startElement( String uri, String name, String qName, Attributes attributes )
         throws SAXException
     {
         if ( ACTIVITY_OBJECT_TYPE.equals( qName ) )
         {
             insideActivityObjectType = true;
         }
         else if ( PUBLISHED.equals( name ) )
         {
             insidePublished = true;
         }
         else if ( ID.equals( name ) )
         {
             insideId = true;
         }
         else if ( LINK.equals( name ) )
         {
             linkHandler = new LinkHandler( this );
             linkHandler.startHandlingEvents();
             linkHandler.startElement( uri, name, qName, attributes );
         }
         else if ( AUTHOR.equals( name ) )
         {
             authorHandler = new AuthorHandler( this );
             authorHandler.startHandlingEvents();
             authorHandler.startElement( uri, name, qName, attributes );
         }
        else if ( CONTENT.equals( name ) ) 
        {
            contentHandler = new ContentHandler( this );
            contentHandler.startHandlingEvents();
            contentHandler.startElement( uri, name, qName, attributes );
        }
        else if ( ORIGINAL_CONTENT.equals( name ) ) 
         {
             contentHandler = new ContentHandler( this );
             contentHandler.startHandlingEvents();
             contentHandler.startElement( uri, name, qName, attributes );
         }
         else if ( IN_REPLY_TO.equals( qName ) )
         {
             replyHandler = new ReplyHandler( this );
             replyHandler.startHandlingEvents();
             replyHandler.startElement( uri, name, qName, attributes );
         }
     }
 
     /**
      * Method to be called every time an xml element ends
      */
     public void endElement( String uri, String name, String qName )
         throws SAXException
     {
         if ( ENTRY.equals( name ) && parentHandler != null )
         {
             stopHandlingEvents();
             parentHandler.endElement( uri, name, qName );
         }
         else if ( ACTIVITY_OBJECT_TYPE.equals( qName ) )
         {
             insideActivityObjectType = false;
         }
         else if ( PUBLISHED.equals( name ) )
         {
             insidePublished = false;
         }
         else if ( ID.equals( name ) )
         {
             insideId = false;
         }
         else if ( AUTHOR.equals( name ) )
         {
             comment.setAuthor( authorHandler.getBuzzAuthor() );
         }
        else if ( CONTENT.equals( name ) )
         {
             comment.setContent( contentHandler.getBuzzContent() );
         }
        else if ( ORIGINAL_CONTENT.equals( name ) ) 
        {
            comment.setOriginalContent( contentHandler.getBuzzContent() );
        }
         else if ( LINK.equals( name ) )
         {
             comment.getLinks().add( linkHandler.getBuzzLink() );
         }
         else if ( IN_REPLY_TO.equals( qName ) )
         {
             comment.setReply( replyHandler.getBuzzReply() );
         }
     }
 
     /**
      * Method to be called between the beginning and the end of the xml elements.
      */
     public void characters( char[] ch, int start, int length )
         throws SAXException
     {
         String content = ( new String( ch ).substring( start, start + length ) );
 
         if ( insideActivityObjectType )
         {
             comment.setActivityObjectType( content );
         }
         else if ( insidePublished )
         {
             comment.setPublished( DateUtils.parseDate( content ) );
         }
         else if ( insideId )
         {
             comment.setId( content );
         }
     }
 }
