 package org.seventyeight.web.model;
 
 import com.google.gson.JsonObject;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 import org.bson.types.ObjectId;
 import org.seventyeight.database.mongodb.MongoDBCollection;
 import org.seventyeight.database.mongodb.MongoDBQuery;
 import org.seventyeight.database.mongodb.MongoDocument;
 import org.seventyeight.database.mongodb.MongoUpdate;
 import org.seventyeight.markup.HtmlGenerator;
 import org.seventyeight.markup.SimpleParser;
 import org.seventyeight.utils.PostMethod;
 import org.seventyeight.web.Core;
 import org.seventyeight.web.authorization.Ownable;
 import org.seventyeight.web.nodes.User;
 import org.seventyeight.web.servlet.Request;
 import org.seventyeight.web.servlet.Response;
 import org.seventyeight.web.utilities.JsonException;
 import org.seventyeight.web.utilities.JsonUtils;
 
 import javax.xml.bind.DatatypeConverter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  *
  * This is the base implementation of a node in the tree.
  *
  * @author cwolfgang
  */
 public abstract class AbstractNode<T extends AbstractNode<T>> extends PersistedObject implements TopLevelNode, Describable<T>, Ownable {
 
     private static Logger logger = LogManager.getLogger( AbstractNode.class );
 
     public static final String MODERATORS = "moderators";
     public static final String VIEWERS = "viewers";
 
     protected Node parent;
 
     public AbstractNode( Node parent, MongoDocument document ) {
         super( document );
 
         this.parent = parent;
     }
 
 
     public void save( CoreRequest request, JsonObject jsonData ) throws ClassNotFoundException, ItemInstantiationException, SavingException {
         logger.debug( "Begin saving" );
 
         Saver saver = getSaver( request );
 
         saver.save();
 
         if( jsonData != null ) {
             logger.debug( "Handling extensions" );
             handleJsonConfigurations( request, jsonData );
         }
 
         update( request.getUser() );
 
         /*
         if( saver.getId() != null ) {
             logger.debug( "Setting id to " + saver.getId() );
             document.set( "_id", saver.getId() );
             //document.set( "_id", getUniqueIdentifier() );
         }
         */
 
         /* Persist */
         MongoDBCollection.get( getDescriptor().getCollectionName() ).save( document );
     }
 
     public Saver getSaver( CoreRequest request ) {
         return new Saver( this, request );
     }
 
     public static class Saver {
         protected AbstractNode modelObject;
         protected CoreRequest request;
 
         public Saver( AbstractNode modelObject, CoreRequest request ) {
             this.modelObject = modelObject;
             this.request = request;
         }
 
         public PersistedObject getModelObject() {
             return modelObject;
         }
 
         public void save() throws SavingException {
 
         }
 
         protected String set( String key ) throws SavingException {
             return set( key, key, true );
         }
 
         protected String set( String formkey, String dbkey ) throws SavingException {
             return set( formkey, dbkey, true );
         }
 
         protected String set( String formkey, String dbkey, boolean mandatory ) throws SavingException {
             String v = request.getValue( formkey, null );
             if( mandatory && ( v == null || v.isEmpty() ) ) {
                 throw new SavingException( "The " + formkey + " must be set" );
             }
             modelObject.document.set( dbkey, v );
 
             return v;
         }
     }
 
     public String getIdentifier() {
         return document.get( "_id" ).toString();
     }
 
     public String getUrl() {
         //return "/get/" + getIdentifier();
         return "/resource/" + getIdentifier() + "/";
     }
 
     public void handleJsonConfigurations( CoreRequest request, JsonObject jsonData ) throws ClassNotFoundException, ItemInstantiationException {
         logger.debug( "Handling extension class Json data" );
 
         List<JsonObject> extensionsObjects = JsonUtils.getJsonObjects( jsonData, JsonUtils.JsonType.extensionClass );
         logger.debug( "I got " + extensionsObjects.size() + " extension types" );
 
         for( JsonObject obj : extensionsObjects ) {
             handleJsonExtensionClass( request, obj );
         }
     }
 
     public void handleJsonExtensionClass( CoreRequest request, JsonObject extensionConfiguration ) throws ClassNotFoundException, ItemInstantiationException {
         String extensionClassName = extensionConfiguration.get( JsonUtils.__JSON_CLASS_NAME ).getAsString();
         logger.debug( "Extension class name is " + extensionClassName );
 
         /* Get Json configuration objects */
         List<JsonObject> configs = JsonUtils.getJsonObjects( extensionConfiguration );
         logger.debug( "I got " + configs.size() + " configurations" );
 
         document.setList( EXTENSIONS );
         for( JsonObject c : configs ) {
             Describable d = handleJsonConfiguration( request, c );
             document.addToList( EXTENSIONS, d.getDocument() );
         }
     }
 
 
     public Describable handleJsonConfiguration( CoreRequest request, JsonObject jsonData ) throws ItemInstantiationException, ClassNotFoundException {
         /* Get Json configuration object class name */
         String cls = jsonData.get( JsonUtils.__JSON_CLASS_NAME ).getAsString();
         logger.debug( "Configuration class is " + cls );
 
         Class<?> clazz = Class.forName( cls );
         Descriptor<?> d = Core.getInstance().getDescriptor( clazz );
         logger.debug( "Descriptor is " + d );
 
         Describable e = d.newInstance( "", this );
 
         /* Remove data!? */
         if( d.doRemoveDataItemOnConfigure() ) {
             logger.debug( "This should remove the data attached to this modelObject" );
         }
 
         return e;
 
     }
 
 
     /**
      * Return a {@link List} of {@link org.seventyeight.web.model.AbstractExtension.ExtensionDescriptor}'s that are applicable and have a certain template.
      */
    public List<Layoutable> getLayoutableHavingTemplate( String template ) {
         List<Layoutable> ds = new ArrayList<Layoutable>(  );
 
         logger.debug( "Layoutable: " + Core.getInstance().getExtensions( Layoutable.class ) );
         //logger.debug( "LIST: " +  );
 
         for( Layoutable d : Core.getInstance().getExtensions( Layoutable.class ) ) {
             if( d.isApplicable( this ) &&
                Core.getInstance().getTemplateManager().templateForClassExists( Core.getInstance().getDefaultTheme(), d.getClass(), template ) ) {
                 ds.add( d );
             }
         }
 
         return ds;
     }
 
    public List<AbstractExtension.ExtensionDescriptor<?>> getLayoutableHavingTemplate2( String template ) {
         List<AbstractExtension.ExtensionDescriptor<?>> ds = new ArrayList<AbstractExtension.ExtensionDescriptor<?>>(  );
 
         logger.debug( "DS: " + Core.getInstance().getExtensionDescriptors( Layoutable.class ) );
 
         for( Descriptor d : Core.getInstance().getExtensionDescriptors( Layoutable.class ) ) {
             if( (( AbstractExtension.ExtensionDescriptor)d).isApplicable( this ) &&
                Core.getInstance().getTemplateManager().templateForClassExists( Core.getInstance().getDefaultTheme(), d.getClazz(), template ) ) {
                 ds.add( (AbstractExtension.ExtensionDescriptor<?>) d );
             }
         }
 
         return ds;
     }
 
     @Override
     public Node getParent() {
         return parent;
     }
 
     public boolean isOwner( User user ) {
         logger.debug( "USER: " + user );
         logger.debug( "OWENR: " + getOwnerId() );
         return user.getIdentifier().equals( getOwnerId() );
     }
 
     public String getOwnerId() {
         return document.get( "owner" );
     }
 
     @Override
     public Descriptor<T> getDescriptor() {
         return Core.getInstance().getDescriptor( getClass() );
     }
 
     /**
      * Save the document of the {@link Node}.
      */
     @Override
     public void save() {
         logger.debug( "BEFORE SAVING: " + document );
         MongoDBCollection.get( getDescriptor().getCollectionName() ).save( document );
     }
 
     public void setOwner( User owner ) {
         logger.debug( "Setting owner to " + owner );
         document.set( "owner", owner.getIdentifier() );
     }
 
     public User getOwner() throws ItemInstantiationException {
         return User.getUserByUsername( this, (String) document.get( "owner" ) );
     }
 
     public String getOwnerName() {
         return document.get( "owner" );
     }
 
     public Date getCreatedAsDate() {
         //return new Date( (Long)getField( "created" ) );
         return DatatypeConverter.parseDateTime( (String) getField( "created" ) ).getTime();
     }
 
     public String getType() {
         return document.get( "type", "unknown" );
     }
 
     public Date getCreated() {
         return getField( "created" );
     }
 
     public void update( User owner ) {
         logger.debug( "Updating " + this );
 
         Date now = new Date();
 
         if( document.get( "owner", null ) == null ) {
             logger.debug( "Owner was set to " + owner );
             setOwner( owner );
         }
 
         document.set( "updated", now );
         document.set( "revision", getRevision() + 1 );
     }
 
     public Date getUpdated() {
         return getField( "updated", null );
     }
 
     public void setTitle( String title ) {
         document.set( "title", title );
     }
 
     public String getTitle() {
         String title = getField( "title", null );
         if( title != null ) {
             return title;
         } else {
             throw new IllegalStateException( "A node must have a title" );
         }
     }
 
     public void delete() {
         document.set( "deleted", new Date().getTime() );
     }
 
 
     public Date getDeletedAsDate() {
         Long l = getField( "deleted", null );
         if( l != null ) {
             return new Date( l );
         } else {
             return null;
         }
     }
 
     public Long getDeleted() {
         return getField( "deleted" );
     }
 
 
     public Long getViews() {
         return getField( "views", 0l );
     }
 
     public void incrementViews() {
         document.set( "views", getViews() + 1 );
     }
 
     public int getRevision() {
         return getField( "revision", 1 );
     }
 
     public ObjectId getObjectId() {
         Object o = document.get( "_id" );
         logger.info( "OBJECT: " + o );
         return ObjectId.massageToObjectId( o.toString() );
     }
 
     public MongoDocument getUniqueIdentifier() {
         MongoDocument d = new MongoDocument().set( "type", ((ResourceDescriptor)getDescriptor()).getType() ).set( "name", getTitle() );
         return d;
     }
 
     @PostMethod
     public void doConfigurationSubmit( Request request, Response response ) throws JsonException, ClassNotFoundException, SavingException, ItemInstantiationException, IOException {
         logger.debug( "Configuration submit" );
 
         JsonObject jsonData = JsonUtils.getJsonFromRequest( request );
         save( request, jsonData );
         response.sendRedirect( getUrl() );
     }
 
     @Override
     public boolean equals( Object obj ) {
         if( obj == this ) {
             return true;
         }
 
         if( getClass().isInstance( obj ) ) {
             AbstractNode n = (AbstractNode) obj;
             if( getIdentifier().equals( n.getIdentifier() ) ) {
                 return true;
             } else {
                 return false;
             }
         } else {
             return false;
         }
     }
 
 
     public void updateField( String collection, MongoUpdate update ) {
         MongoDBCollection.get( collection ).update( new MongoDBQuery().is( "_id", getObjectId() ), update );
     }
 
     /**
      * Get the first {@link Node} having the title.
      */
     public static <N extends Node> N getNodeByTitle( Node parent, String title ) {
         return getNodeByTitle( parent, title, null );
     }
 
     /**
      * Get the first {@link Node} having the title of the given type.
      * Type can be null and will then be disregarded.
      */
     public static <N extends Node> N getNodeByTitle( Node parent, String title, String type ) {
         MongoDBQuery q = new MongoDBQuery().is( "title", title );
         if( type != null && !type.isEmpty() ) {
             q.is( "type", type );
         }
         MongoDocument docs = MongoDBCollection.get( Core.RESOURCES_COLLECTION_NAME ).findOne( q );
         logger.debug( "DOC IS: " + docs );
 
         if( docs != null ) {
             try {
                 return (N) Core.getInstance().getItem( parent, docs );
             } catch( ItemInstantiationException e ) {
                 logger.warn( e.getMessage() );
                 return null;
             }
         } else {
             logger.debug( "The node " + title + " was not found" );
             return null;
         }
     }
 
     public static <N extends Node> List<N> getNodesByTitle( Node parent, String title, String type ) {
         MongoDBQuery q = new MongoDBQuery().is( "title", title );
         if( type != null && !type.isEmpty() ) {
             q.is( "type", type );
         }
         List<MongoDocument> docs = MongoDBCollection.get( Core.RESOURCES_COLLECTION_NAME ).find( q );
         logger.debug( "Docs are: " + docs );
 
         List<N> nodes = new ArrayList<N>( docs.size() );
 
         if( docs != null ) {
             for( MongoDocument doc : docs ) {
                 try {
                     nodes.add( (N)Core.getInstance().getItem( parent, doc ) );
                 } catch( ItemInstantiationException e ) {
                     /* TODO should this fail the entire method???? */
                     logger.error( e.getMessage() );
                 }
             }
         } else {
             logger.debug( "Any node with title " + title + " was not found" );
         }
 
         return nodes;
     }
 
     public static <N extends Node> N getNodeById( Node parent, String id ) {
         MongoDocument doc = MongoDBCollection.get( Core.RESOURCES_COLLECTION_NAME ).getDocumentById( id );
         if( doc != null ) {
             try {
                 return (N) Core.getInstance().getItem( parent, doc );
             } catch( ItemInstantiationException e ) {
                 logger.warn( e.getMessage() );
                 return null;
             }
         } else {
             logger.debug( "The node with id " + id + " was not found" );
             return null;
         }
     }
 
     /* Texts */
     public static final String TEXTS_COLLECTION = "texts";
     private static SimpleParser textParser = new SimpleParser( new HtmlGenerator() );
 
     public enum TextType {
         markUp,
         html
     }
 
     public String getText( String type, String language ) {
         logger.debug( "Getting " + type + " for " + getIdentifier() );
 
         MongoDBQuery query = new MongoDBQuery().is( "identifier", getIdentifier() ).is( "type", type ).is( "language", language );
         MongoDocument doc = MongoDBCollection.get( TEXTS_COLLECTION ).findOne( query );
 
         if( doc == null || doc.isNull() ) {
             query = new MongoDBQuery().is( "identifier", getIdentifier() ).is( "type", type );
             doc = MongoDBCollection.get( TEXTS_COLLECTION ).findOne( query );
 
             if( doc == null || doc.isNull() ) {
                 throw new IllegalStateException( language + " not found for " + type );
             }
         }
 
         return doc.getr( "texts" ).get( TextType.html.name(), "" );
     }
 
     /**
      * Set an internationalized text.
      * @param type The type of text, eg description
      * @param text The text itself
      * @param language
      */
     public void setText( String type, String text, String language ) {
         logger.debug( "Setting " + type + " for " + getIdentifier() );
 
         MongoDBQuery query = new MongoDBQuery().is( "identifier", getIdentifier() ).is( "type", type ).is( "language", language );
         MongoDocument doc = MongoDBCollection.get( TEXTS_COLLECTION ).findOne( query );
 
         if( doc == null || doc.isNull() ) {
             doc = createTextDocument( type, text, language );
         } else {
             updateTextDocument( doc, text );
         }
 
         MongoDBCollection.get( TEXTS_COLLECTION ).save( doc );
     }
 
     public void updateTextDocument( MongoDocument doc, String text ) {
         // Meta data
         doc.set( "revision", doc.get( "revision", 1 ) + 1 );
         doc.set( "updated", new Date() );
 
         StringBuilder output = textParser.parse( text );
 
         // Set the version of the parser and generator
         String version = textParser.getVersion() + ":" + textParser.getGeneratorVersion();
         doc.set( "textParserVersion", version );
 
         // Set the texts
         MongoDocument texts = doc.getSubDocument( "texts", null );
         if( texts == null || texts.isNull() ) {
             texts = new MongoDocument();
             doc.set( "texts", texts );
         }
         texts.set( TextType.markUp.name(), text );
         texts.set( TextType.html.name(), output.toString() );
     }
 
     /**
      * Create a text document
      */
     public MongoDocument createTextDocument( String type, String text, String language ) {
         logger.debug( "Creating text document" );
 
         MongoDocument doc = new MongoDocument();
 
         // Set meta data
         doc.set( "identifier", this.getIdentifier() );
         doc.set( "language", language );
         doc.set( "type", type );
         doc.set( "created", new Date() );
         doc.set( "revision", 1 );
 
         StringBuilder output = textParser.parse( text );
 
         // Set the version of the parser and generator
         String version = textParser.getVersion() + ":" + textParser.getGeneratorVersion();
         doc.set( "textParserVersion", version );
 
         // Set the texts
         MongoDocument texts = new MongoDocument();
         texts.set( TextType.markUp.name(), text );
         texts.set( TextType.html.name(), output.toString() );
         doc.set( "texts", texts );
 
         return doc;
     }
 
 
     @Override
     public String getDisplayName() {
         return getTitle();
     }
 
     @Override
     public String toString() {
         //return getDisplayName();
         return getIdentifier();
     }
 
     @Override
     public String getMainTemplate() {
         return "org/seventyeight/web/main.vm";
     }
 }
