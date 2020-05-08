 package com.svilendobrev.storage;
 
 import com.svilendobrev.jbase.funk;
 //import com.svilendobrev.jbase.Model;
 
 import java.io.Serializable;
 
 /*
 Ektorp/jackson can use annotations like:
 {
     @JsonCreator public SomeClass( @JsonProperty("id") int id, @JsonProperty("name") String name) { .. }
     @JsonProperty("a") public String xA = "a";
 }
 
     ...
     mapper.registerSubtypes( SomeClass.class);
 
 or
     SimpleModule module = new SimpleModule("test", Version.unknownVersion());
     module.addSerializer( String.class, new AnnotatedContextualSerializer());
     mapper.registerModule( module);
 */
 
 import org.ektorp.ReplicationCommand;
 import org.ektorp.CouchDbInstance;
 import org.ektorp.CouchDbConnector;
 import org.ektorp.UpdateConflictException;
 import org.ektorp.ViewQuery;
 import org.ektorp.ViewResult;
 import org.ektorp.http.HttpClient;
 import org.ektorp.impl.StdCouchDbInstance;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.JsonNodeFactory;
 import com.fasterxml.jackson.annotation.JsonIgnore;
 import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
 
 import org.ektorp.android.util.CouchbaseViewListAdapter;
 import org.ektorp.DbAccessException;
 import org.ektorp.android.util.EktorpAsyncTask;
 
 import org.ektorp.support.CouchDbRepositorySupport;
 //import org.ektorp.support.DesignDocument;
 
 //import com.couchbase.touchdb.TDDatabase;
 import com.couchbase.touchdb.TDServer;
 //import com.couchbase.touchdb.TDView;
 import com.couchbase.touchdb.ektorp.TouchDBHttpClient;
 import com.couchbase.touchdb.router.TDURLStreamHandlerFactory;
 
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.content.Context;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Collection;
 import java.util.Iterator;
 
 
 public abstract
 class touchdb {
     protected
     static String TAG = "dbTouch";
 
     static public void init() {
         //static inializer to ensure that touchdb:// URLs are handled properly
         TDURLStreamHandlerFactory.registerSelfIgnoreError();
     }
 
     protected
     static TDServer server;
     static HttpClient httpClient;
     public
     CouchDbInstance dbInstance;
     public
     HashMap< String, CouchDbConnector>      databases = new HashMap();
     public
     ArrayList< CouchbaseViewListAdapter>    adapters = new ArrayList();
 
     public
     HashMap< String, Map< String, Boolean>> dbname2view2value_is_doc = new HashMap();
     public
     Boolean view_value_is_doc( String dbname, String view) {
         Map< String, Boolean> vv = dbname2view2value_is_doc.get( dbname);
         if (funk.not(vv)) return null;
         return vv.get( view);
     }
 
     // {local_name: remote_name|""|null } i.e. remote_name,same_name,dont_replicate
     public
     HashMap< String, String> dbname2open = new HashMap();
     public void add_db_repl( String dbname, String remote_name) { dbname2open.put( dbname, remote_name); }
     public void add_db_repl_samename( String dbname)     { add_db_repl( dbname, ""); }
     public void add_db_no_repl( String dbname)           { add_db_repl( dbname, null); }
     public void add_db_no_repl_if_missing( String dbname)  { if (dbname2open.get( dbname)==null) add_db_repl( dbname, null); }
 
 
     public
     void open() {
         Log.v( TAG, "startDb");
 
         String filesDir = ctx().getFilesDir().getAbsolutePath();
         try {
             server = new TDServer( filesDir);
         } catch (IOException e) { Log.e(TAG, "Error starting TDServer", e); }
         if (server == null) return;
 
         databases.clear();
         adapters.clear();
         dbname2view2value_is_doc.clear();
         dbname2open.clear();
 
         Log.v( TAG, "installViewDefinitions");
         installViewDefinitions();
 
         Log.v( TAG, "starting ektorp");
         if (httpClient != null) httpClient.shutdown();
 
         httpClient = new TouchDBHttpClient( server);
         dbInstance = new StdCouchDbInstance( httpClient);
 
         (new AsyncTask4db() {
             @Override protected void doInBackground() {
                 for (String dbname : dbname2open.keySet()) {
                     databases.put( dbname, dbInstance.createConnector( dbname, true));
                     Log.v( TAG, "opened " + dbname);
                 }
             }
             @Override protected void onSuccess() {
                 Log.v( TAG, "databasesOK");
                 _isDatabaseOK = true;
                 onDatabasesOK();
                 startReplications();
                 Log.v( TAG, "startedOK");
                 onStartedOK();
             }
         }).execute();
     }
 
     public
     void close() {
         Log.v( TAG, "stopDB");
         _isDatabaseOK = false;
 
         //stop the async tasks that follow the changes feed
         for (CouchbaseViewListAdapter a : adapters)
             a.cancelContinuous();
         adapters.clear();
 
         //for (CouchDbConnector c: databases.getValues()) ..
         databases.clear();
 
         dbInstance = null;
         //clean up http client connection manager
         if (httpClient != null) httpClient.shutdown();
         httpClient = null;
 
         if (server != null) server.close();
         server = null;
     }
 
     protected abstract
     Context ctx(); // { return null; }   //XXX do override
 
     public abstract
     void installViewDefinitions();     //XXX do override
         //Log.e( TAG, "call genViews4Touchdb.views", server, "version", dbname2open );
 
     public
     static String _DEFAULT_dburl = "";
     public
     String DEFAULT_dburl() { return _DEFAULT_dburl; }    //XXX do override
 
     public
     static String _SYNC_URL_pref = "sync_url";
     public
     String SYNC_URL() {                     //XXX override if needed
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( ctx());
         return prefs.getString( _SYNC_URL_pref, DEFAULT_dburl() );
     }
 
     protected   boolean _isDatabaseOK = false;
     public      boolean isDatabaseOK() { return _isDatabaseOK; }
     protected
     void onDatabasesOK() {}                 //XXX do override
         //Log.d( TAG, "onDatabasesOK - attach adapters to the list etc");
         /*
         itemListView.setAdapter( makeAdapter( dbname, viewQuery) );
         */
     protected
     void onStartedOK() {}                 //XXX do override
 
 
     static
     public class TouchDbRepositorySupport<T> extends CouchDbRepositorySupport<T> {
         //fixes getAll to check the native views first
         //XXX uses static .server
 
 	    protected TouchDbRepositorySupport( Class<T> type, CouchDbConnector db, String designDocName) { super( type, db, designDocName); }
         @Override
         public List<T> getAll() {
             if (native_designDocContainsAllView()) return queryView("all");
             return super.getAll();
         }
 	    boolean native_designDocContainsAllView() {
             funk.assertTrue( stdDesignDocumentId.startsWith( "_design/") );
             String dd = stdDesignDocumentId.substring( funk.len( "_design/") );
             return null != server.getDatabaseNamed( db.getDatabaseName(), false ).getExistingViewNamed( dd+"/all" );
 	    }
     }
 
 ///////////// internals
 
     public
     void startReplications() {
         Log.v( TAG, "startReplications");
         String sync_url = SYNC_URL();
         for (String dbname : dbname2open.keySet()) startReplication( dbname, sync_url);
     }
     public
     void startReplication( String dbname, String sync_url) {
         String remote_name = dbname2open.get( dbname);
         if (remote_name == null) return;
         if (funk.not( remote_name)) remote_name = dbname;
         _startReplication( dbname, sync_url + "/" + remote_name);
     }
     public
     void startReplication( String dbname) { startReplication( dbname, SYNC_URL()); }
 
     //additional databases: put remote_name (or "" for same_name) in .dbname2open if replicable
     public
     CouchDbConnector open_db( final String dbname) {
         CouchDbConnector db = databases.get( dbname);
         if (db != null) return db;
         db = dbInstance.createConnector( dbname, true);
         databases.put( dbname, db);
         Log.v( TAG, "opened " + dbname);
         startReplication( dbname );
         return db;
     }
 
 /////////////
 
     static public ViewQuery
     query2( String design_doc, String view_name) {
         return new ViewQuery().designDocId( "_design/"+design_doc ).viewName( view_name);
     }
     static public ViewQuery
     query( String design_view_name) {
         String[] r = design_view_name.split("/");
         return query2( r[0], r[1]);
     }
     static public ViewQuery
     query_alldocs( boolean includeDocs) { return new ViewQuery().allDocs().includeDocs( includeDocs); }
 
     public ViewResult
     query_raw( String dbname, ViewQuery query ) {
         return databases.get( dbname).queryView( query);
     }
 
     static public ViewQuery
     _q_descending( ViewQuery q ) {
         Object startKey = q.getStartKey();
         Object endKey   = q.getEndKey();
         return q.descending( true
             ).startKey( endKey
             ).endKey( startKey
             );
     }
 
     public ViewQuery
     _query_value_is_doc( ViewQuery q, String dbname, String view) {
         if (funk.not( view_value_is_doc( dbname, view))) q = q.includeDocs( true);
         return q;
     }
     public ViewQuery
     query4doc( String dbname, String design_view_name) {
         return _query_value_is_doc( query( design_view_name), dbname, design_view_name);
     }
 
     static public JsonNode
     row2doc( ViewResult.Row x, Boolean use_value) {
         JsonNode v = (use_value!=null && use_value) ? x.getValueAsNode() : x.getDocAsNode();
         if (use_value==null && v==null) v = x.getValueAsNode();     //no doc, try value
         return v;
     }
     static public JsonNode row2doc( ViewResult.Row x) { return row2doc( x, null); }
 
     static public
     ObjectNode jsonObject() { return JsonNodeFactory.instance.objectNode(); }
     static public
     ArrayNode  jsonArray()  { return JsonNodeFactory.instance.arrayNode(); }
 
     static public Collection< String>
     query_only_id( ViewResult vr, Collection< String> r) {
         if (r==null) r = new ArrayList();
         for (ViewResult.Row x: vr.getRows())
             r.add( x.getId() );
         return r;
     }
     static public Collection< String>
     query_only_id( ViewResult vr) { return query_only_id( vr, null); }
     public Collection< String>
     query_only_id( ViewQuery q, String dbname, Collection< String> r) { return query_only_id( query_raw( dbname, q), r); }
 
 /*
     public
     CouchbaseViewListAdapter makeAdapter( String dbname, ViewQuery viewQuery) {
         //ViewQuery viewQuery = query( design_doc, view_name);
         //if (descending) viewQuery.descending( true);
         CouchbaseViewListAdapter a = null; //new SyncListAdapter( databases.get( dbname), viewQuery);
         adapters.add( a);
         return a;
         //itemListView.setAdapter( itemListViewAdapter);
     }
 */
 
     void _startReplication( String dbname, String sync_url) {
 
         Log.v( TAG, "startReplication push " +dbname + " > " + sync_url);
         final ReplicationCommand pushReplicationCommand = new ReplicationCommand.Builder()
             .source( dbname)
             .target( sync_url)
             .continuous( true)
             .build();
         (new AsyncTask4db() {
             @Override protected void doInBackground() { dbInstance.replicate( pushReplicationCommand); }
         }).execute();
 
         Log.v( TAG, "startReplication pull " +dbname + " < " + sync_url);
         final ReplicationCommand pullReplicationCommand = new ReplicationCommand.Builder()
             .source( sync_url)
             .target( dbname)
             .continuous( true)
             .build();
         (new AsyncTask4db() {
             @Override protected void doInBackground() { dbInstance.replicate( pullReplicationCommand); }
         }).execute();
     }
 
     static public long      //XXX used as _count in genViews/reduce
     reduce_count( List< Object> values, boolean rereduce) {
         if (!rereduce) return values.size();
         long l = 0;
         for (Object o: values)
             if (o instanceof Integer) l += (Integer)o;
             else if (o instanceof Long) l += (Long)o;
             else Log.w( TAG, "Warning non-int/long value found in _count: " + o);
         return l;
     }
 
     //XXX funk.first_non_null( Object... values)  used for a || b || c in genViews
 
 
     ////////
 
     static public abstract class AsyncTask4db extends EktorpAsyncTask {
         @Override protected void onDbAccessException( DbAccessException dbAccessException) {
             Log.e( TAG, "DbAccessException in background", dbAccessException);
         }
     }
     static public abstract class AsyncTask4dbitem extends AsyncTask4db {
         Object _item;
         String op;
         public AsyncTask4dbitem( Object item, String op) { this._item = item; this.op = op; }
         @Override protected void onSuccess() { Log.d( TAG, "Document " + op + " ok"); }
         @Override protected void onUpdateConflict( UpdateConflictException updateConflictException) {
             Log.i( TAG, "conflict " + op + ": " + _item.toString());
         }
     }
 
     //id/rev goes into item + return rev
     public String _create( String dbname, ObjectNode item) { databases.get( dbname).create( item); return db2string( item.get( "_rev")); }
     public String _update( String dbname, ObjectNode item) { databases.get( dbname).update( item); return db2string( item.get( "_rev")); }
     //return rev
     public String _delete( String dbname, ObjectNode item) { return databases.get( dbname).delete( item); }
 
     //return rev
     public String _op( String dbname, ObjectNode item, String op) {
         if ("create".equals( op)) return _create( dbname, item);
         if ("update".equals( op)) return _update( dbname, item);
         if ("delete".equals( op)) return _delete( dbname, item);
         funk.fail( "unknown op "+op);
         return null;
     }
 
     public String _create( String dbname, Model x) {
         Base b = modelklas2db.get( x.getClass() );
         ObjectNode o = b.save( x, null);
         String rev = _create( dbname, o);
         b.load( x, o);  //replace id/rev/whatever
         return rev;
     }
     public String _update( String dbname, Model x) {
         Base b = modelklas2db.get( x.getClass() );
         ObjectNode o = b.save( x, null);
         String rev = _update( dbname, o);
         b.load( x, o);  //replace id/rev/whatever
         return rev;
     }
     public String _delete( String dbname, Model x) {
         Base b = modelklas2db.get( x.getClass() );
         ObjectNode o = b.save( x, null);
         String rev = _delete( dbname, o);
         o.put( "_rev", rev);
         b.load( x, o);  //replace id/rev/whatever
         return rev;
     }
     public String _op( String dbname, Model item, String op) {
         if ("create".equals( op)) return _create( dbname, item);
         if ("update".equals( op)) return _update( dbname, item);
         if ("delete".equals( op)) return _delete( dbname, item);
         funk.fail( "unknown op "+op);
         return null;
     }
 
 
     public String _op( String dbname, Object x, String op) {
         if (x instanceof Model)      return _op( dbname, (Model)x, op);
         if (x instanceof ObjectNode) return _op( dbname, (ObjectNode)x, op);
         funk.fail( "unknown type "+x);
         return null;
     }
 
     public void _op( final String dbname, final Object item, boolean async, final String op) {
         if (!async)
             _op( dbname, item, op);
         else
         (new AsyncTask4dbitem( item, op ) { @Override protected void doInBackground() {
             _op( dbname, item, op);
         }}).execute();
     }
 
     public void create( final String dbname, Object item, boolean async ) { _op( dbname, item, async, "create" ); }
     public void update( final String dbname, Object item, boolean async ) { _op( dbname, item, async, "update" ); }
     public void delete( final String dbname, Object item, boolean async ) { _op( dbname, item, async, "delete" ); }
 
     /////////// data i/o conversion
 
     static public String  db2string( JsonNode a)  { return a == null ? null : a.textValue(); }
     static public Integer db2int(    JsonNode a)  { return a == null ? null : a.intValue(); }
     static public Long    db2long(   JsonNode a)  { return a == null ? null : a.longValue(); }
     static public Double  db2float(  JsonNode a)  { return a == null ? null : a.doubleValue(); }
     static public Boolean db2bool(   JsonNode a)  { return a == null ? null : a.booleanValue(); }
 
     static public String  db2string( JsonNode a, String _default)  { return a == null ? _default : a.textValue(); }
     static public Integer db2int(    JsonNode a, int    _default)  { return a == null ? _default : a.intValue(); }
     static public Long    db2long(   JsonNode a, long   _default)  { return a == null ? _default : a.longValue(); }
     static public Double  db2float(  JsonNode a, double _default)  { return a == null ? _default : a.doubleValue(); }
     static public Boolean db2bool(   JsonNode a, boolean _default) { return a == null ? _default : a.booleanValue(); }
 
     static public HashMap< String, ArrayList< String>> db2map_str_list_str( JsonNode a) {
        if (a == null || a.isMissingNode() || a.isNull()) return null;
         HashMap< String, ArrayList< String>> rmap = new HashMap();
         Iterator< Map.Entry< String, JsonNode>> entries = ((ObjectNode)a).fields();
         while (entries.hasNext()) {
             Map.Entry< String, JsonNode> entry = entries.next();
             ArrayList< String> ra = null;
             JsonNode ev = entry.getValue();
            if (ev != null && !ev.isMissingNode() && !ev.isNull()) {
                 ra = new ArrayList();
                for (Iterator< JsonNode> items = ((ArrayNode)ev).elements(); items.hasNext(); )
                    ra.add( db2string( items.next() ) );
                //if (funk.not(ra)) ra = null;
             }
             rmap.put( entry.getKey(), ra );
         }
         return rmap;
     }
     static public ObjectNode map_str_list_str2db( Map< String, ArrayList< String>> imap ) {
         if (funk.not( imap)) return null;
         ObjectNode o = jsonObject();
         for (String k: imap.keySet()) {
             List< String> vi = imap.get(k);
             ArrayNode ra = null;
             if (funk.any( vi)) {
                 ra = jsonArray();
                 for (String i: vi) ra.add( i);
             }
             o.put( k, ra);
        }
        return o;
     }
     //Date    db2TimeStamp( float a)
 
     ////////////
     //XXX all model.attr values can be null. use funk.defaults( m.attr, defvalue), or shortcuts or getters
     static public boolean getfalse( Boolean x)    { return funk.defaults( x, false); }
     static public int     get0( Integer x)        { return funk.defaults( x, 0); }
     static public String  get( String x)          { return funk.defaults( x, ""); }
     //...
     ////////////
 
     @JsonIgnoreProperties(ignoreUnknown=true)       //ignore anything unknown at read
     static public abstract
     class Model
     //extends com.svilendobrev.jbase.Model
     implements Serializable
     {
     /*
         public String _id  = null;
         public String _rev = null;
         @Override
         public String getKey()  { return _id; } //persistence
     */
         //@Override
         //public long getId()     { return 1/0; }
         @JsonIgnore     //XXX Ektorp/jackson will map+write anything with get* ..
         public abstract String getId() ;// { return _id; } //persistence
         public String toString()        { return dump(); }      //visualisation/short text /explain
         public String dump() {
             return //getClass().getName() + "/" +
                 super.toString();
         }
 
         @Override
         public int hashCode()   { String id = getId(); return id==null ? 0 : id.hashCode(); }
 
         static public interface Many extends Collection<Model> {};
         static public class _List extends ArrayList<Model> implements Many {
             public _List() { super(); }
             public _List( Collection c) { super(c); }
         };
         static public Many newCollection() { return new _List(); }
         static public Many newCollection( Collection c) { return new _List( c); }
 
     }
 
     static protected
     HashMap< Class, Base> modelklas2db = new HashMap();
 
     static public
     abstract class Base {   //see com.svilendobrev.storage.sqlite.Base
         //create an instance of this to autoregister, BEFORE db is created
         public Base() {
             touchdb.modelklas2db.put( modelClass(), this);
         }
         public abstract Class modelClass();
         public abstract Model load( Model target, JsonNode c);   //ret target
         public abstract ObjectNode save( Model source, JsonNode result); //ret result
         public Model load( JsonNode c) { return load( null, c); }
 
         public Model.Many
         load_as_Models( ViewResult vr, Model.Many r, Boolean use_value ) {
             if (r==null) r = Model.newCollection();
             for (ViewResult.Row x: vr.getRows())
                 r.add( load( row2doc( x, use_value) ));
             return r;
         }
         public Model.Many load_as_Models( ViewResult vr, Model.Many r)  { return load_as_Models( vr, r, null); }
         public Model.Many load_as_Models( ViewResult vr, boolean use_value)   { return load_as_Models( vr, null, use_value ); }
         public Model.Many load_as_Models( ViewResult vr)                      { return load_as_Models( vr, null, null); }
 
         public <T extends Model> List< T>
         load_org( ViewResult vr, List< T> r, Boolean use_value) {
             if (r==null) r = new ArrayList();
             for (ViewResult.Row x: vr.getRows())
                 r.add( (T)load( touchdb.row2doc( x, use_value)));
             return r;
         }
         public <T extends Model> List< T> load_org_doc(   ViewResult vr, List< T> r) { return load_org( vr, r, false); }
         public <T extends Model> List< T> load_org_value( ViewResult vr, List< T> r) { return load_org( vr, r, true); }
         public <T extends Model> List< T> load_org_guess( ViewResult vr, List< T> r) { return load_org( vr, r, null); }
         public <T extends Model> List< T> load_org_doc( ViewResult vr)   { return load_org_doc( vr, null); }
         public <T extends Model> List< T> load_org_value( ViewResult vr) { return load_org_value( vr, null); }
         public <T extends Model> List< T> load_org_guess( ViewResult vr) { return load_org_guess( vr, null); }
         public <T extends Model> List< T> load_org( ViewResult vr, boolean use_value)   { return load_org( vr, null, use_value); }
         public <T extends Model> List< T> load_org( ViewResult vr, List< T> r)          { return load_org_guess( vr, r); }
         public <T extends Model> List< T> load_org( ViewResult vr)                      { return load_org_guess( vr); }
     }
 
     //public void insert( Model x, String dbname, boolean async ) { create( dbname, x, async); }
     //public void update( Model x, String dbname, boolean async ) { update( dbname, x, async); }
     //public void delete( Model x, String dbname, boolean async ) { delete( dbname, x, async); }
 
 
     ////////// app specific XXX - inherit then whatever /////////
 /*  //as in jbase.sqlite:
     //all Base/db-model-clones - generated or handmade - have to be registered (just create one)
     //BEFORE db is created; e.g. in some static block/method
     //beware that static stuff is called ONLY when owner class is used which may be too late
     //and the order inside a class is AS written
 
     static {
         //new genSQLite.PersonalChannel();  not needed
         new genSQLite.Program();
         new db4Category();
         new genSQLite.KeyValue();
         new Order4Category.db();
         new SavedFilter.db();
     }
 */
 
 
 /*
     public ViewResult
     q_dbs_raw() {
         String dbname = "cc";
         ViewQuery q = query( "cc/db_by_user" );
         return databases.get( dbname ).queryView( q);
 	}
     public List< String>
     q_dbs() {
         ViewResult vr = q_dbs_raw();
 	    //List<Row> listRows = vr.getRows();
         List< String> r = new ArrayList();
         for (ViewResult.Row x: vr.getRows()) r.add( x.getId() );
         return r;
     }
     public ViewQuery
     _q_places_by_owner_raw( String owner) {
         ViewQuery q = query( "places/all");
         if (owner != null) q = q.startKey( owner ).endKey( owner+"/");
         return q;
     }
     public List< Models.Place>
     _q_places( ViewQuery q, List< Models.Place> r) {
         ViewResult vr = databases.get( dbname4place).queryView( q);
         return genTouchdb.Place.base.load_org( vr, r);
     }
     public List< Models.Place>
     q_places_by_owner( String owner) { return _q_places( _q_places_by_owner_raw( owner) ); }
 
 */
 /*
  - needs at-least empty design-doc for each view, as getAll() checks for document presence.
    done workaround TouchDbRepositorySupport: override CouchDbRepositorySupport.getAll()
 
  - QueryResultParser expects row to be ordered map... TDView.queryWithOptions returns maps
     fixed in Ektorp 1.4.*
    or
     use ViewResult, then for each, mapper.convertValue( node, target.class) - unclear how much slower this is
 */
 
 } //dbtouch
 // vim:ts=4:sw=4:expandtab
