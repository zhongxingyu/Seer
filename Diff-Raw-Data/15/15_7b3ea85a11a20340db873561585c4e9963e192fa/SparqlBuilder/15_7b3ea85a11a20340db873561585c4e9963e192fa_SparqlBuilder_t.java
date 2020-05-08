 package org.neo4j.rdf.store;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.neo4j.rdf.model.Literal;
 import org.neo4j.rdf.model.Resource;
 import org.neo4j.rdf.model.Statement;
 import org.neo4j.rdf.model.Uri;
 import org.neo4j.rdf.model.Value;
 
 /**
  * Builds a SPARQL string from {@link Statement}s.
  */
 public class SparqlBuilder
 {
     private static final String URI_ENDING_DELIMITER = "#";
 
     /**
      * Does the conversion.
      * @param statement the {@link Statement}.
      * @return the string.
      */
     public static String getQuery( Statement statement )
     {
         Map<String, String> baseUriToPrefix = new HashMap<String, String>();
         StringBuffer query = new StringBuffer();
         extractPrefixes( baseUriToPrefix, statement );
         appendPrefixes( baseUriToPrefix, query );
         Set<String> variables = new HashSet<String>();
         String where = buildWhere( baseUriToPrefix, variables, statement );
         appendSelect( variables, query );
         appendWhere( where, query );
         return query.toString();
     }
     
     private static void appendSelect( Set<String> variables,
         StringBuffer query )
     {
         query.append( "SELECT " );
         int counter = 0;
         for ( String variable : variables )
         {
             if ( counter++ > 0 )
             {
                 query.append( " " );
             }
             query.append( "?" + variable );
         }
        query.append( "\n" );
     }
     
     private static void appendWhere( String where, StringBuffer query )
     {
         query.append( "WHERE {\n" + where + "}\n" );
     }
     
     private static String buildWhere( Map<String, String> baseUriToPrefix,
         Set<String> variables, Statement statement )
     {
         StringBuffer buffer = new StringBuffer();
         buffer.append( statementElement( baseUriToPrefix,
             variables, statement.getSubject() ) );
        buffer.append( " " );
         buffer.append( statementElement( baseUriToPrefix,
             variables, statement.getPredicate() ) );
        buffer.append( " " );
         Value object = statement.getObject();
         buffer.append( statementElement( baseUriToPrefix, variables, object ) );
         buffer.append( " .\n" );
         return buffer.toString();
     }
     
     private static String statementElement( Map<String, String> baseUriToPrefix,
         Set<String> variables, Object object )
     {
         String result = null;
         if ( object == null )
         {
             char variableChar = ( char ) ( 'x' + variables.size() );
             String variableName = String.valueOf( variableChar );
             variables.add( variableName );
             result = "?" + variableName;
         }
         else if ( object instanceof Uri )
         {
             Uri uri = ( Uri ) object;
             String uriString = uri.getUriAsString();
             result = shortenUri( baseUriToPrefix, uriString );
         }
         else if ( object instanceof Literal )
         {
             result = "\"" + ( ( Literal ) object ).getValue().toString() + "\"";
         }
         return result;
     }
     
     private static String shortenUri( Map<String, String> baseUriToPrefix,
         String uri )
     {
         for ( String baseUri : baseUriToPrefix.keySet() )
         {
             if ( uri.startsWith( baseUri ) )
             {
                 String shortVersion = baseUriToPrefix.get( baseUri );
                 return shortVersion + ":" + uri.substring(
                     baseUri.length() );
             }
         }
         throw new RuntimeException( "Hmm: '" + uri + "'" );
     }
     
     private static void appendPrefixes( Map<String, String> baseUriToPrefix,
         StringBuffer query )
     {
         for ( Map.Entry<String, String> prefix : baseUriToPrefix.entrySet() )
         {
             query.append( "PREFIX " + prefix.getValue() + ": <" +
                 prefix.getKey() + ">" + "\n" );
         }
     }
     
     private static void extractPrefixes( Map<String, String> baseUriToPrefix,
         Statement... statements )
     {
         for ( Statement statement : statements )
         {
             extractPrefix( baseUriToPrefix, ( Uri ) statement.getSubject() );
             extractPrefix( baseUriToPrefix, ( Uri ) statement.getPredicate() );
             if ( statement.getObject() instanceof Resource )
             {
                 extractPrefix( baseUriToPrefix, ( Uri ) statement.getObject() );
             }
         }
     }
     
     private static void extractPrefix( Map<String, String> baseUriToPrefix,
         Uri uri )
     {
         if ( uri == null || uri.getUriAsString() == null )
         {
             return;
         }
         String baseUri = getBaseUri( uri.getUriAsString(), true );
         if ( !baseUriToPrefix.containsKey( baseUri ) )
         {
             String shortVersion = "ns" + baseUriToPrefix.size() + 1;
             baseUriToPrefix.put( baseUri, shortVersion );
         }
     }
 
     private static String getBaseUri( String uri, boolean includeSeparator )
     {
         int index = uri.lastIndexOf( URI_ENDING_DELIMITER );
         if ( index == -1 )
         {
             return uri;
         }
         if ( includeSeparator )
         {
             index++;
         }
         return uri.substring( 0, index );
     }
//    
//    public static void main( String[] args )
//    {
//        Statement statement = new WildcardEnabledStatement(
//            new Uri( "http://neo4j.org#subject" ),
//            new Uri( "http://neo4j.org#predicate" ),
//            new Uri( "http://neo4j.org#object" ) );
//        System.out.println( SparqlBuilder.getQuery( statement ) );
//    }
 }
