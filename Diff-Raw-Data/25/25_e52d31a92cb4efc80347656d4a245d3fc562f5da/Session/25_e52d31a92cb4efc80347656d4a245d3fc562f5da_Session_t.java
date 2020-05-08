 package com.nuecho.grammarserver.api;
 
 import java.io.IOException;
 
 import org.json.simple.JSONObject;
 
 /**
  * Objects implementing this interface represent opened sessions with NuGram
  * Hosted Server.
  * 
  * @author Nu Echo
  */
 public interface Session
 {
     /**
      * Returns the session ID.
      * @return the session ID
      */
     public String getSessionId();
 
     /**
      * Uploads the given ABNF grammar to NuGram Hosted Server.
      * @param grammarPath the grammar path (unique Id) of the grammar to upload
      * @param content the source ABNF grammar to upload
      * @throws IOException
      */
     public void upload(String grammarPath, String content) throws GrammarServerException;
 
     /**
      * Instructs NuGram Hosted Server to load the given grammar in memory. his
      * is usually done when the application wants to do some semantic
      * interpretation using the grammar. Returns an InstantiatedGrammar object.
      * 
      * @param grammarPath the grammar path (unique id) of the grammar to load
      * @return an InstantiatedGrammar object
      * @throws IOException
      */
     public InstantiatedGrammar load(String grammarPath) throws GrammarServerException;
 
     /**
      * Instantiates a dynamic grammar with the given data. Returns an
      * InstantiatedGrammar object upon successful instantiation.
      * 
      * @param grammarPath the path of the grammar to instantiate
      * @param the data used to populate the dynamic grammar template (the
      * instantiation context)
      * @return an InstantiatedGrammar object
      * @throws IOException
      */
     public InstantiatedGrammar instantiate(String grammarPath, JSONObject data) throws GrammarServerException;
 
     /**
      * Instantiates a dynamic grammar with the given data, encoded as a JSON string. Returns an
      * InstantiatedGrammar object upon successful instantiation.
      * 
      * @param grammarPath the path of the grammar to instantiate
      * @param the data used to populate the dynamic grammar template (the
      * instantiation context) encoded as a JSON string
      * @return an InstantiatedGrammar object
      * @throws IOException
      */
     public InstantiatedGrammar instantiate(String grammarPath, String data) throws GrammarServerException;
     
     /**
      * Terminates the session with NuGram Server.
      */
     public void disconnect();
 }
