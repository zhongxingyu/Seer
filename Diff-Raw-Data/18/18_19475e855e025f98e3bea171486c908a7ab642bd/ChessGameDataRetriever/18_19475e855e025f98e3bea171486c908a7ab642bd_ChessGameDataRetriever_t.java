 /**
  * ChessGameDataRetriever.java
  */
 package de.uni_leipzig.informatik.swp13_sc.sparql;
 
import java.util.List;

 import virtuoso.jena.driver.VirtGraph;
 import virtuoso.jena.driver.VirtuosoQueryExecution;
 import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
 
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Literal;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 
 import de.uni_leipzig.informatik.swp13_sc.datamodel.ChessGame;
import de.uni_leipzig.informatik.swp13_sc.datamodel.ChessMove;
 import de.uni_leipzig.informatik.swp13_sc.datamodel.ChessPlayer;
 import de.uni_leipzig.informatik.swp13_sc.datamodel.pgn.ChessPGNVocabulary;
 import de.uni_leipzig.informatik.swp13_sc.datamodel.rdf.ChessRDFVocabulary;
 
 /**
  * A retriever class for querying a triple store over a VirtGraph (only
  * Virtuoso therefore). It returns for given URI/IRIs the first found
  * resource and wraps it into a {@link ChessGame} object.
  *
  * @author Erik
  *
  */
 public class ChessGameDataRetriever
 {
     /**
      * The virtuosoGraph (Virtuoso triple store) which will be queried.
      */
     private final VirtGraph virtuosoGraph;
     
     /**
      * Internal ChessPlayerDataRetriever to resolve player URI/IRIs and assign
      * the resources to the chess games.
      */
     private ChessPlayerDataRetriever cpdr;
     
     /**
      * Internal ChessMoveListDataRetriever to retrieve chess move data from
      * game URI/IRIs and assign the resources to the chess games.
      */
     private ChessMoveListDataRetriever cmdr;
     
     /**
      * Constructor. Requires the connection to the datastore (Virtuoso)
      * 
      * @param   virtuosoGraph   the triplestore which will be queried
      * @throws  Exception   if argument is null
      */
     public ChessGameDataRetriever(VirtGraph virtuosoGraph)
         throws Exception
     {
         if (virtuosoGraph == null)
         {
             throw new NullPointerException("virtuosoGraph is null");
         }
         
         this.virtuosoGraph = virtuosoGraph;
         
         this.createDefaultChessMoveListDataRetriever();
     }
     
     /**
      * Sets a {@link ChessPlayerDataRetriever} object to resolve chess player
      * resources in chess games.
      * 
      * @param   cpdr    {@link ChessPlayerDataRetriever}
      * @return  {@link ChessGameDataRetriever}
      */
     public ChessGameDataRetriever setChessPlayerDataRetriever(ChessPlayerDataRetriever cpdr)
     {
         this.cpdr = cpdr;
         return this;
     }
     
     /**
      * Creates a default {@link ChessPlayerDataRetriever} object to retrieve
      * chess player resources.
      * 
      * @return  {@link ChessGameDataRetriever}
      */
     public ChessGameDataRetriever createDefaultChessPlayerDataRetriever()
     {
         try
         {
             this.cpdr = new ChessPlayerDataRetriever(this.virtuosoGraph);
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
         return this;
     }
     
     /**
      * Creates a default {@link ChessMoveListDataRetriever} object to retrieve
      * chess move resources.
      * 
      * @return  {@link ChessGameDataRetriever}
      */
     public ChessGameDataRetriever createDefaultChessMoveListDataRetriever()
     {
         try
         {
             this.cmdr = new ChessMoveListDataRetriever(this.virtuosoGraph);
         }
         catch (Exception e)
         {
             e.printStackTrace();
         }
         return this;
     }
     
     /**
      * Returns the internal {@link ChessPlayerDataRetriever} object used to
      * retrieve chess player resources from URI/IRIs.
      * 
      * @return  {@link ChessPlayerDataRetriever}
      */
     public ChessPlayerDataRetriever getChessPlayerDataRetriever()
     {
         return this.cpdr;
     }
     
     /**
      * Determines if the given URI/IRI is a chess game resource.
      * 
      * @param   gameURI URI/IRI to check
      * @return  true if chess game resource else false
      */
     public boolean isGame(String gameURI)
     {
         if (gameURI == null)
         {
             return false;
         }
         
         StringBuilder sb = new StringBuilder();
         sb.append("SELECT *\nWHERE\n{\n  <")
             .append(gameURI)
             .append("> a <")
             .append(ChessRDFVocabulary.ChessGame)
             .append(">.\n}");
         
         // DEBUG:
         //System.out.println(sb.toString());
         
         VirtuosoQueryExecution vqeS = VirtuosoQueryExecutionFactory.create(sb.toString(), this.virtuosoGraph);
         
         boolean ret = false;
         
         try
         {
             ResultSet results = vqeS.execSelect();
             
             if (results.hasNext())
             {
                 ret = true;
             }
             
             vqeS.close();
         }
         catch (Exception e)
         {
             e.printStackTrace();
             return false;
         }
         
         return ret;
     }
     
     /**
      * Returns a {@link ChessGame} for a given game URI/IRI. If a
      * {@link ChessPlayerDataRetriever} is set it will also try to get the
      * chess game data. (But it will create a new {@link ChessPlayer} object
      * for each game - there is no caching yet.)
      * 
      * @param   gameURI
      * @return  {@link ChessGame}
      */
     public ChessGame getGame(String gameURI)
     {
         if (gameURI == null)
         {
             return null;
         }
         
         StringBuilder sb = new StringBuilder();
         sb.append("SELECT DISTINCT ?prop ?value\nWHERE\n{  <")
             .append(gameURI)
             .append("> ?prop ?value.\n}");
         
         // DEBUG:
         //System.out.println(sb.toString());
         
         VirtuosoQueryExecution vqeS = VirtuosoQueryExecutionFactory.create(sb.toString(), this.virtuosoGraph);
         
         try
         {
             ResultSet results = vqeS.execSelect();
             
             ChessGame.Builder cgb = new ChessGame.Builder();
             
             while (results.hasNext())
             {
                 QuerySolution result = (QuerySolution) results.next();
                 // DEBUG:
                 //System.out.println(result.toString());
                 
                 if (result.get("value").isLiteral()) 
                 {
                     Resource l_prop = result.getResource("prop");
                     String prop = l_prop.getLocalName();
 
                     Literal l_value = result.getLiteral("value");
                     String value = l_value.toString();
                     
                     cgb.addMetaData(prop, value);
                     
                     if (ChessPGNVocabulary.Meta_Key_Site.equalsIgnoreCase(prop))
                     {
                         cgb.setSite(value);
                     }
                     else if (ChessPGNVocabulary.Meta_Key_Date.equalsIgnoreCase(prop))
                     {
                         cgb.setDate(value);
                     }
                     else if (ChessPGNVocabulary.Meta_Key_Event.equalsIgnoreCase(prop))
                     {
                         cgb.setEvent(value);
                     }
                     else if (ChessPGNVocabulary.Meta_Key_Result.equalsIgnoreCase(prop))
                     {
                         cgb.setResult(value);
                     }
                     else if (ChessPGNVocabulary.Meta_Key_Round.equalsIgnoreCase(prop))
                     {
                         cgb.setRound(value);
                     }
                     
                     // DEBUG:
                     //System.out.println("Prop: " + prop + ", Value: " + value);
                 }
             }
             
             if (this.cpdr != null)
             {
                 cgb.setWhitePlayer(this.getWhiteChessPlayer(gameURI))
                     .setBlackPlayer(this.getBlackChessPlayer(gameURI));
                 
                 // TODO: black and white meta keys are not set yet ...
             }
             
             cgb.setMoves(this.cmdr.getMoves(gameURI));
             
             vqeS.close();
             
             return cgb.build();
         }
         catch (Exception e)
         {
             e.printStackTrace();
             return null;
         }
     }
     
     /**
      * Returns a single String value for a given property to the specified game
      * URI/IRI. On error it will return null.
      * 
      * @param   gameURI     URI/IRI of the chess game
      * @param   property    String property of the chess game
      * @return  String or null on error
      */
     public String getSingleGameProperty(String gameURI, String property)
     {
         if (gameURI == null)
         {
             return null;
         }
         
         String var_name = "?var";
         StringBuilder sb = new StringBuilder();
         sb.append("SELECT DISTINCT ")
             .append(var_name)
             .append("\nWHERE\n{\n  <")
             .append(gameURI)
             .append("> <")
             .append(property)
             .append("> ")
             .append(var_name)
             .append(".\n}\nLIMIT 1");
         
         // DEBUG:
         //System.out.println(sb.toString());
         
         VirtuosoQueryExecution vqeS = VirtuosoQueryExecutionFactory.create(sb.toString(), this.virtuosoGraph);
         
         try
         {
             ResultSet results = vqeS.execSelect();
             
             if (results.hasNext())
             {
                 QuerySolution result = (QuerySolution) results.next();
                 
                 RDFNode n_var = result.get(var_name);
                 
                 vqeS.close();
                 
                 return (n_var == null) ? null : n_var.toString();
             }
         }
         catch (Exception e)
         {
             e.printStackTrace();
             return null;
         }
         
         return null;
     }
     
     /**
      * Returns the URI/IRI of the white player for the given game URI/IRI.
      * 
      * @param   gameURI white player of this game
      * @return  String if successful, null on error
      */
     public String getWhiteChessPlayerURI(String gameURI)
     {
         return this.getSingleGameProperty(gameURI, ChessRDFVocabulary.whitePlayer.toString());
     }
     
     /**
      * Returns the URI/IRI of the black player for the given game URI/IRI.
      * 
      * @param   gameURI black player of this game
      * @return  String if successful, null on error
      */
     public String getBlackChessPlayerURI(String gameURI)
     {
         return this.getSingleGameProperty(gameURI, ChessRDFVocabulary.blackPlayer.toString());
     }
     /**
      * Returns a {@link ChessPlayer} object from a chess player resource. It
      * will retrieve them for the given game URI/IRI.
      * 
      * @param   gameURI URI/IRI of chess game in which the player participated.
      * @return  {@link ChessPlayer} or null on error
      */
     public ChessPlayer getWhiteChessPlayer(String gameURI)
     {
         if (this.cpdr != null)
         {
             return this.cpdr.getPlayer(this.getWhiteChessPlayerURI(gameURI));
         }
         return null;
     }
     /**
      * Returns a {@link ChessPlayer} object from a chess player resource. It
      * will retrieve them for the given game URI/IRI.
      * 
      * @param   gameURI URI/IRI of chess game in which the player participated.
      * @return  {@link ChessPlayer} or null on error
      */
     public ChessPlayer getBlackChessPlayer(String gameURI)
     {
         if (this.cpdr != null)
         {
             return this.cpdr.getPlayer(this.getBlackChessPlayerURI(gameURI));
         }
         return null;
     }
    
    /**
     * Returns a list of {@link ChessMove}s for a given game URI/IRI. On error
     * it will return an empty list.
     * 
     * @param   uri URI/IRI of game or opening
     * @return  List<{@link ChessMove}>
     * @see ChessMoveListDataRetriever
     */
    public List<ChessMove> getMoves(String uri)
    {
        return this.cmdr.getMoves(uri);
    }
 }
