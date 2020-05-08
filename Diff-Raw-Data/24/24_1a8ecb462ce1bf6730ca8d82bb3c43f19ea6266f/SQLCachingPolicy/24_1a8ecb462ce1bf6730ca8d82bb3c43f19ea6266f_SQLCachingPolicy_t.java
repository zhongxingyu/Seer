 package org.xmodel.caching.sql;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.Set;
 import org.xmodel.IModelObject;
 import org.xmodel.ModelObject;
 import org.xmodel.Xlate;
 import org.xmodel.caching.sql.transform.DefaultSQLRowTransform;
 import org.xmodel.caching.sql.transform.ISQLColumnTransform;
 import org.xmodel.caching.sql.transform.SQLColumnMetaData;
 import org.xmodel.caching.sql.transform.SQLDirectColumnTransform;
 import org.xmodel.caching.sql.transform.SQLXmlColumnTransform;
 import org.xmodel.caching.sql.transform.SimpleSQLParser;
 import org.xmodel.compress.TabularCompressor;
 import org.xmodel.external.CachingException;
 import org.xmodel.external.ConfiguredCachingPolicy;
 import org.xmodel.external.ICache;
 import org.xmodel.external.IExternalReference;
 import org.xmodel.external.ITransaction;
 import org.xmodel.xaction.IXAction;
 import org.xmodel.xaction.XActionDocument;
 import org.xmodel.xpath.expression.IContext;
 import org.xmodel.xpath.expression.IExpression;
 import org.xmodel.xpath.expression.StatefulContext;
 
 /**
  * An ICachingPolicy that uses an arbitrary SQL query statement to load data.  The caching policy can also generate
  * statements for inserting, updating and deleting elements when the <i>update</i> flag is true.  By default the 
  * following transformation rules apply:
  * <ul>
  * <li>Each table row is transformed into a child element of the table element.</li>
  * <li>The name of each row element is the same as the name of the table element unless <i>name</i> is specified.</li>
  * <li>Table columns are stored in child elements of the row element with the following exceptions.</li>
  * <li>Table columns listed in <i>indexes</i> are stored in static attributes of the row element.</li>
  * <li>Table columns listed in <i>attributes</i> are stored in non-static attributes of the row element.</li>
  * <li>The names of row element attributes and children are the same as the table column name or alias.</li>
  * </ul>
  * 
  * <h3>Thin Wrapping vs. Cross-Platform</h3>
  * Whenever possible, SQL queries should be written using generic SQL syntax that does not tie the query to a
  * specific vendor.  To that end, this class will provide support for some common query keywords that are do
  * not have generic representations.  For example, restricting a query result to a maximum number of rows is 
  * accomplished with the "LIMIT" keyword in MySQL, while SQLServer uses the "TOP" keyword.  The "limit" and
  * "offset" expressions provide a cross-platform mechanism for selecting a specific subset of the records.
  * 
  * <h3>Two-Layer Caching</h3>
  * When the <i>shallow</i> flag evaluates true, only the indexed columns for each row element are loaded from the
  * database, and a second caching policy is assigned to each row element.  When a non-static node of a row element
  * is accessed, the row element is loaded from the database using a modified version of the SQL query.  The SQL
  * is modified by discarding the WHERE clause, if present, and adding a WHERE clause that will uniquely select
  * the row using the data in the static attributes.  This will only work for queries that have the following
  * general regular expression form: <br/>
  * <pre>
  *   SELECT (.*) FROM (.*) WHERE (.*)
  * </pre>
  * 
  * <h3>Database Updates</h3>
  * When the <i>update</i> flag evaluates true, changes to the data-model will make corresponding changes to the
  * database.  The following table summarizes the types of database updates:
  * <ul>
  * <li>Inserting a row element will cause a row to be added to the database table.</li>
  * <li>Deleting a row element will cause a row to be deleted from the database table.</li>
  * <li>Updating any column of a row element will cause that column to be updated in the database.</li>
  * <li>Any changes to the data-model of an XML column will cause that column to be updated in the database.</li>
  * <li>Caching policy transactions allow multiple updates to be committed together.</li>
  * <li>Insert and delete statements appearing in caching policy transactions are optimized by batch updating.</li>
  * </ul> 
  * 
  * <h3>Reference</h3>
  * <ul>
  * <li>provider - An optional expression that identifies the implementation of ISQLProvider.</li>
  * <li>query - A string expression that gives the query to be executed.</li>
  * <li>name - The name to be used for the row elements instead of the table element name.</li>
  * <li>indexes - A string expression giving comma-separated list of indexed columns where the first index is primary key.</li>
  * <li>attributes - An optional string expression giving a comma-separated list of column names to store in attributes.</li>
  * <li>shallow - A boolean expression that specifies whether row content is loaded on demand (default: false()).</li>
  * <li>limit - The maximum number of rows to return.</li>
  * <li>offset - The offset of the first record to be returned from the result-set.</li>
  * </ul>
  * 
  * <h3>Example:</h3>
  * <pre>
  * &lt;create var="table"&gt;
  *   &lt;table&gt;
  *     &lt;extern:cache class="org.xmodel.caching.sql.SQLCachingPolicy"&gt;
  *       &lt;provider&gt;$provider&lt;/provider&gt;
  *       &lt;query&gt;"SELECT id, name, age FROM employee"&lt;query&gt;
  *       &lt;indexes&gt;"id"&lt;indexes&gt;
  *     &lt;/extern:cache&gt;
  *   &lt;/table&gt;
  * &lt;create&gt;
  * </pre>
  */
 public class SQLCachingPolicy extends ConfiguredCachingPolicy
 {
   public SQLCachingPolicy( ICache cache)
   {
     super( cache);
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.external.ConfiguredCachingPolicy#configure(org.xmodel.xpath.expression.IContext, org.xmodel.IModelObject)
    */
   @Override
   public void configure( IContext context, IModelObject annotation) throws CachingException
   {
     super.configure( context, annotation);
     
     this.metadata = new SQLColumnMetaData();
     this.metadataReady = false;
     
     IExpression queryExpr = Xlate.childGet( annotation, "query", (IExpression)null);
     query = queryExpr.evaluateString( context);
     
     IExpression updateExpr = Xlate.childGet( annotation, "update", (IExpression)null);
     update = (updateExpr != null)? updateExpr.evaluateBoolean( context): false;
     
     IExpression shallowExpr = Xlate.childGet( annotation, "shallow", (IExpression)null);
     boolean shallow = (shallowExpr != null)? shallowExpr.evaluateBoolean( context): false;
     
     IExpression offsetExpr = Xlate.childGet( annotation, "offset", (IExpression)null);
     offset = (offsetExpr != null)? (int)offsetExpr.evaluateNumber( context): -1;
     
     IExpression limitExpr = Xlate.childGet( annotation, "limit", (IExpression)null);
     limit = (limitExpr != null)? (int)limitExpr.evaluateNumber( context): -1;
     
     parser = new SimpleSQLParser( query);
     configureProvider( context, annotation);
     configureRowTransform( annotation, shallow);
     
     if ( shallow) rowQuery = String.format( "%s WHERE %s = $?", parser.getQueryWithoutPredicate(), primaryKey);
   }
 
   /**
    * Configure the ISQLProvider instance from the annotation.
    * @param context The context.
    * @param annotation The caching policy annotation.
    */
   private void configureProvider( IContext context, IModelObject annotation) throws CachingException
   {
     try
     {
       IExpression providerExpr = Xlate.childGet( annotation, "provider", (IExpression)null);
       IModelObject providerConfig = (providerExpr != null)? providerExpr.queryFirst( context): annotation;
       provider = SQLProviderFactory.getProvider( providerConfig);
     }
     catch( Exception e)
     {
       throw new CachingException( e.getMessage());
     }
   }
     
   /**
    * Configure the ISQLRowTransform from the annotation.
    * @param annotation The caching policy annotation.
    * @param shallow True if two-level caching is requested.
    */
   private void configureRowTransform( IModelObject annotation, boolean shallow) throws CachingException
   {
     // create list of column names that will be attributes
     Set<String> attributes = new HashSet<String>();
     
     // indexes
     String indexes = Xlate.childGet( annotation, "indexes", (String)null);
     if ( indexes != null)
     {
       String[] split = indexes.split( "\\s*,\\s*");
       for( String index: split) 
       {
         addStaticAttribute( index);
         attributes.add( index);
       }
       
       if ( split.length > 0) primaryKey = split[ 0];
     }
     
     // other attributes
     for( IModelObject element: annotation.getChildren( "attribute"))
     {
       Object attribute = element.getValue();
       if ( attribute != null) attributes.add( attribute.toString());
     }
     
     // xml columns
     Set<String> xmlColumns = null;
     String xml = Xlate.childGet( annotation, "xml", (String)null);
     if ( xml != null)
     {
       xmlColumns = new HashSet<String>();
       String[] split = xml.split( "\\s*,\\s*");
       for( String field: split) 
         xmlColumns.add( field);
     }
 
     // get row element name
     String rowElementName = Xlate.childGet( annotation, "name", annotation.getParent().getType());
     
     // create row transform
     transform = new DefaultSQLRowTransform( rowElementName);
     for( String column: parser.getColumnNames())
     {
       // two-layer caching means only static attributes are populated during first layer sync
       if ( !shallow || isStaticAttribute( column))
       {
         if ( xmlColumns.contains( column))
         {
           ISQLColumnTransform columnTransform = new SQLXmlColumnTransform( metadata, column, column, new TabularCompressor());
           transform.defineColumn( column, columnTransform);
         }
         else
         {
           ISQLColumnTransform columnTransform = new SQLDirectColumnTransform( metadata, column, column, attributes.contains( column));
           transform.defineColumn( column, columnTransform);
         }
       }
     }
   }
     
   /* (non-Javadoc)
    * @see org.xmodel.external.ConfiguredCachingPolicy#syncImpl(org.xmodel.external.IExternalReference)
    */
   @Override
   protected void syncImpl( IExternalReference reference) throws CachingException
   {
     if ( tableReference == null) tableReference = reference;
     
     if ( reference == tableReference) 
     {
       syncTable( reference); 
     }
     else if ( reference.getParent() == tableReference) 
     { 
       syncRow( reference);
     }
     else
     {
       throw new IllegalStateException( "SQLCachingPolicy instance used by more than one element.");
     }
   }
   
   /**
    * Synchronize a table reference.
    * @param reference The reference.
    */
   protected void syncTable( IExternalReference reference) throws CachingException
   {
     IModelObject prototype = new ModelObject( reference.getType());
 
     try
     {
       Connection connection = provider.leaseConnection();
       PreparedStatement statement = provider.createStatement( connection, query, limit, offset);
       ResultSet rowCursor = statement.executeQuery();
       
       if ( !metadataReady) 
       {
         metadata.setColumnTypes( rowCursor.getMetaData());
         metadataReady = true;
       }
       
       while( rowCursor.next())
       {
         IModelObject rowElement = transform.importRow( rowCursor);
         prototype.addChild( rowElement);
       }
       
       statement.close();
     }
     catch( SQLException e)
     {
       String message = String.format( "Unable to sync reference with query, '%s'", query);
       throw new CachingException( message, e);
     }
     
     update( reference, prototype);
   }
   
   /**
    * Synchronize a table reference.
    * @param reference The reference.
    */
   protected void syncRow( IExternalReference reference) throws CachingException
   {
     try
     {
       Connection connection = provider.leaseConnection();
       
       PreparedStatement statement = connection.prepareStatement( rowQuery);
       statement.setObject( 1, Xlate.get( reference, primaryKey, ""));
       
       ResultSet rowCursor = statement.executeQuery();
       if ( rowCursor.next())
       {
         IModelObject rowElement = transform.importRow( rowCursor);
         update( reference, rowElement);
       }
       
       statement.close();
     }
     catch( SQLException e)
     {
       String message = String.format( "Unable to sync reference with query, '%s'", query);
       throw new CachingException( message, e);
     }
   }
   
   /* (non-Javadoc)
    * @see org.xmodel.external.AbstractCachingPolicy#transaction()
    */
   @Override
   public ITransaction transaction()
   {
     return null;
   }
 
   public static void main( String[] args) throws Exception
   {
     IXAction script = XActionDocument.parseScript( 
         "<script>" +
         "  <create var='provider'>" +
         "    <provider provider='mysql' host='localhost'>" +
         "      <username>root</username>" +
         "      <password>root</password>" +
         "      <database>ip6sonar</database>" +
         "    </provider>" +
         "  </create>" +
         "" +
         "  <create var='users'>" +
         "    <users>" +
         "      <extern:cache class='org.xmodel.caching.sql.SQLCachingPolicy'>" +
         "        <provider>$provider</provider>" +
        "        <query>'SELECT * FROM user'</query>" +
         "        <xml>purchases</xml>" +
         "      </extern:cache>" +
         "    </users>" +
         "  </create>" +
         "" +
         "  <print>$users</print>" +
         "</script>");
     
     StatefulContext context = new StatefulContext();
     script.run( context);
   }
   
   protected ISQLProvider provider;
   protected String query;
   protected String rowQuery;
   protected String primaryKey;
   protected SimpleSQLParser parser;
   protected boolean update;
   protected SQLColumnMetaData metadata;
   protected boolean metadataReady;
   protected DefaultSQLRowTransform transform;
   protected IExternalReference tableReference;
   protected long limit;
   protected long offset;
 }
