 package com.rssninja.utils;
 
 import java.sql.*;
 import java.util.Properties;
 import com.rssninja.models.*;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 public class Database {
 
     public static Database INSTANCE = new Database();
     private  String userName = "dev";
     private String password = "secreto";
     private String dbms = "mysql";
     private String serverName = "localhost";
     private String portNumber = "3306";
 
     private final String insertWordSQL = "INSERT INTO word (value) VALUES (?)";
     private final String selectWordSQL = "SELECT * FROM word WHERE value = ?";
     private final String insertSemanticSQL = "INSERT INTO Semantic (word1,word2,relation_factor) VALUES (?,?,?)";
     private final String selectSemanticSQL = "SELECT * FROM Semantic WHERE word1 = ? AND word2 = ?";
     private final String updateSemanticSQL = "UPDATE Semantic SET relation_factor = ? WHERE word1 = ? AND word2 = ?";
     private final String insertKeywordSQL = "INSERT INTO keyword (value) values(?)";
     private final String insertLinkSQL = "INSERT INTO link (value,fecha,keyword_id) values(?,?.?)";
     private final String insertKnowledgeSQL = "INSERT INTO knowledge (link,servicio,relevancia) values(?,?,?)";
     private final String selectLinkSQL = "SELECT * FROM link WHERE value = ?";
     private final String selectKeywordSQL = "SELECT * FROM keyword WHERE value = ?";
     private final String selectLinkByIdSQL = "SELECT * FROM link WHERE id = ?";
     private final String selectKeywodByIdSQL = "SELECT * FROM link WHERE id = ?";
     private final String getKnowledgeByServiceSQL = "SELECT * FROM knowledge WHERE servicio = ?";
     private final String getNewLinks = "SELECT * FROM link WHERE id NOT IN(SELECT link FROM knowledge) AND keyword_id = ?";
     private final String getTagId = "SELECT id  FROM keyword WHERE value LIKE ?";
     private final String saveLinkQUERY = "INSERT INTO link (value,fecha,keyword_id) VALUES (?,?,?)";
    private final String getKnowledgeByKeywordSQL = "select k.id, l.id, l.value, k.servicio, k.relevancia from knowledge as k LEFT JOIN link as l ON k.link = l.id where l.keyword_id IN (SELECT id from keyword where value= ? ) order by k.relevancia desc";
 
     private final String getRelatedTags1 = "select w.value, s.relation_factor from Semantic as s LEFT JOIN word as w ON s.word2 = w.id where s.word1 IN (SELECT id from word where value = ?) order by s.relation_factor desc";
     private final String getRelatedTags2 = "SELECT word1 FROM Semantic WHERE word2=(SELECT id FROM word WHERE value=?) ORDER_BY relation_factor";
 
     private Database(){        
     }
 
     private synchronized Connection getConnection(){
         Connection conn = null;
         Properties connectionProps = new Properties();
         connectionProps.put("user", userName);
         connectionProps.put("password", password);
         try{
             //String url = "jdbc:" + dbms + "://" + serverName + "/RSSNinja";
             //System.out.println(connStr);
             //conn = DriverManager.getConnection(connStr, connectionProps);
             String url = "jdbc:mysql://Localhost/RSSNinja";
             //Class.forName("com.mysql.jdbc.Driver").newInstance();
             conn = DriverManager.getConnection(url,connectionProps);
             
         }catch(SQLException e){
             System.out.println("Error while connecting to database");
         //}catch(ClassNotFoundException e){
          //   System.out.println("Class not found");
         }catch(Exception e){
             System.out.println("Another Exception!!");
         }
         if(conn != null){
             //System.out.println("Connected to database");
         }
         return conn;
     }
 
     /*
      * Inserts a word object in DB and returns the auto generated ID for that word
      */
     public Word insertWord(String word){
         Word wDB = getWord(word);
         if( wDB != null){
             return wDB;
         }
         PreparedStatement st = null;
         Connection c = null;
         int autoID = -1;
         try{
             c= getConnection();
             st = c.prepareStatement(insertWordSQL,Statement.RETURN_GENERATED_KEYS);
             st.setString(1, word);
             st.executeUpdate();
             ResultSet rs = st.getGeneratedKeys();
             if (rs.next()) {
                 autoID = rs.getInt(1);
             }else {
                     throw new SQLException("Row not inserted");
             }            
 //            System.out.println("AutoID: "+autoID);
             rs.close();
             rs = null;
         }catch(SQLException e){
             e.printStackTrace();
         }finally{
             try{
             st.close();
             }catch(SQLException e){
               e.printStackTrace();
             }finally{
                 try{
                 c.close();
                 }catch(SQLException e){
                     e.printStackTrace();
                 }
             }
         }
         if(autoID == -1)
             return null;
         
         return new Word(autoID,word);
     }
     
     public Word getWord(String value){
         Word w = null;
         PreparedStatement st = null;
         Connection c = null;        
         try{
             c= getConnection();
             st = c.prepareStatement(selectWordSQL);
             st.setString(1, value);
             ResultSet rs = st.executeQuery();
             if(rs.next()){
                 w = new Word(rs.getInt(1), rs.getString(2));
             }
             rs.close();
             rs = null;
         }catch(SQLException e){
             e.printStackTrace();
         }finally{
             try{
             st.close();
             }catch(SQLException e){
               e.printStackTrace();
             }finally{
                 try{
                 c.close();
                 }catch(SQLException e){
                     e.printStackTrace();
                 }
             }
         }
         return w;
     }
 
     public Semantic insertSemantic(Word word1, Word word2, float relation_factor){
         Semantic sDB = getSemantic(word1, word2);
         if(sDB != null){
             return updateSemantic(sDB, relation_factor);
         }
         int autoID = -1;
 
         PreparedStatement st = null;
         Connection c = null;
 
         try{
             c= getConnection();
             st = c.prepareStatement(insertSemanticSQL, Statement.RETURN_GENERATED_KEYS);
             st.setInt(1, word1.getId());
             st.setInt(2, word2.getId());
             st.setFloat(3, relation_factor);
             st.executeUpdate();
             ResultSet rs = st.getGeneratedKeys();
             if (rs.next()) {
                 autoID = rs.getInt(1);
             }else {
                 System.out.println("not inserted");
             }
             rs.close();
             rs = null;
         }catch(SQLException e){            
             e.printStackTrace();
         }finally{
             try{
             st.close();
             }catch(SQLException e){                
               e.printStackTrace();
             }finally{
                 try{
                 c.close();
                 }catch(SQLException e){                    
                     e.printStackTrace();
                 }
             }
         }
         
         if(autoID==-1)
             return null;
         return new Semantic(autoID,word1, word2, relation_factor);
     }
 
     public Semantic getSemantic(Word word1, Word word2){
         Semantic s = null;
         PreparedStatement st = null;
         Connection c = null;
         try{
             c= getConnection();
             st = c.prepareStatement(selectSemanticSQL, Statement.RETURN_GENERATED_KEYS);
             st.setInt(1, word1.getId());
             st.setInt(2, word2.getId());            
             ResultSet rs = st.executeQuery();
             if(rs.next()){
                 s = new Semantic(rs.getInt(1), word1, word2, rs.getFloat(4));
             }
             rs.close();
             rs = null;
         }catch(SQLException e){
             e.printStackTrace();
         }finally{
             try{
             st.close();
             }catch(SQLException e){
               e.printStackTrace();
             }finally{
                 try{
                 c.close();
                 }catch(SQLException e){
                     e.printStackTrace();
                 }
             }
         }
         return s;
     }
 
     public Semantic updateSemantic(Semantic sem, float newRelationFactor){
         PreparedStatement st = null;
         Connection c = null;
         try{
             c= getConnection();
             st = c.prepareStatement(updateSemanticSQL);
             st.setFloat(1, sem.getRelation_factor() + newRelationFactor);
             st.setInt(2, sem.getWord1().getId());
             st.setInt(3, sem.getWord2().getId());
             st.executeUpdate();
             sem.setRelation_factor(sem.getRelation_factor() + newRelationFactor);
         }catch(SQLException e){
             e.printStackTrace();
         }finally{
             try{
             st.close();
             }catch(SQLException e){
               e.printStackTrace();
             }finally{
                 try{
                 c.close();
                 }catch(SQLException e){
                     e.printStackTrace();
                 }
             }
         }
         return sem;
     }
 
 
     public Keyword insertKeyword(String value){
         PreparedStatement iK = null;
         Connection c = null;
         int autoID = -1;
         try {
                c = getConnection();
                iK = c.prepareStatement(insertKeywordSQL, Statement.RETURN_GENERATED_KEYS);
                iK.setString(1, value);
                iK.executeUpdate();
                ResultSet keys = iK.getGeneratedKeys();
                if(keys.next()){
                    autoID = keys.getInt(1);
                }else{
                    System.out.println("Can't insert the keyword");
                }
                keys.close();
                keys = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }finally{
                try {
                 iK.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            return new Keyword(autoID, value);
     }
 
     public Link insertLink(Keyword keyword,String value,String fecha){
         PreparedStatement iL = null;
         Connection c = null;
         int autoID = -1;
         try {
             c = getConnection();
             iL = c.prepareStatement(insertLinkSQL, Statement.RETURN_GENERATED_KEYS);
             iL.setInt(1, keyword.getId());
             iL.setString(2, value);
             iL.setString(3, fecha);
             iL.executeUpdate();
             ResultSet keys = iL.getGeneratedKeys();
             if(keys.next()){
                 autoID = keys.getInt(1);
             }else{
                 System.out.println("Can't insert the Link");
             }
             keys.close();
             keys = null;
         } catch (SQLException e) {
             e.printStackTrace();
         }finally{
             try {
              iL.close();
             } catch (SQLException e) {
                 e.printStackTrace();
             }
         }
         return new Link(autoID ,keyword,value,fecha);
     }
 
     public Knowledge insertKnowledge(Link link,String service, String tag,int relevance){
     PreparedStatement iK = null;
     Connection c = null;
      int autoID = -1;
      try {
             c = getConnection();
             iK = c.prepareStatement(insertKnowledgeSQL, Statement.RETURN_GENERATED_KEYS);
             iK.setInt(1, link.getId());
             iK.setString(2,service);
             iK.setInt(3,relevance);
             iK.executeUpdate();            
             ResultSet keys = iK.getGeneratedKeys();
             if(keys.next()){
                 autoID = keys.getInt(1);
             }else{
                 System.out.println("Can't insert the knowledge");
             }
             keys.close();
             keys = null;
         } catch (SQLException e) {
             e.printStackTrace();
         }finally{
             try {
              iK.close();
             } catch (SQLException e) {
                 e.printStackTrace();
             }
         }
         return new Knowledge(autoID,link,service,relevance);
     }
 
     public Link getLink(String value){
         Link l = null;
         PreparedStatement gL = null;
         Connection c = null;
         try{
             c= getConnection();
             gL = c.prepareStatement(selectLinkSQL);
             gL.setString(1, value);
             ResultSet rs = gL.executeQuery();
             if(rs.next()){
                 l = new Link(rs.getInt(1), getKeywordById(rs.getInt(2)),rs.getString(3),rs.getString(4));
             }
             rs.close();
             rs = null;
         }catch(SQLException e){
             e.printStackTrace();
         }finally{
             try{
             gL.close();
             }catch(SQLException e){
               e.printStackTrace();
             }finally{
                 try{
                 c.close();
                 }catch(SQLException e){
                     e.printStackTrace();
                 }
             }
         }
         return l;
     }
 
     public Keyword getKeyword(String value){
         Keyword k = null;
         PreparedStatement gK = null;
         Connection c = null;
         try{
             c= getConnection();
             gK = c.prepareStatement(selectKeywordSQL);
             gK.setString(1, value);
             ResultSet rs = gK.executeQuery();
             if(rs.next()){
                 k = new Keyword(rs.getInt(1), rs.getString(2));
             }
             rs.close();
             rs = null;
         }catch(SQLException e){
             e.printStackTrace();
         }finally{
             try{
             gK.close();
             }catch(SQLException e){
               e.printStackTrace();
             }finally{
                 try{
                 c.close();
                 }catch(SQLException e){
                     e.printStackTrace();
                 }
             }
         }
         return k;
     }
 
     public Keyword getKeywordById(int id){
         Keyword k = null;
         PreparedStatement gK = null;
         Connection c = null;
         try{
             c= getConnection();
             gK = c.prepareStatement(selectKeywodByIdSQL);
             gK.setInt(1, id);
             ResultSet rs = gK.executeQuery();
             if(rs.next()){
                 k = new Keyword(rs.getInt(1), rs.getString(2));
             }
             rs.close();
             rs = null;
         }catch(SQLException e){
             e.printStackTrace();
         }finally{
             try{
             gK.close();
             }catch(SQLException e){
               e.printStackTrace();
             }finally{
                 try{
                 c.close();
                 }catch(SQLException e){
                     e.printStackTrace();
                 }
             }
         }
         return k;
     }
 
     public Link getLinkById(int id){
         Link l = null;
         PreparedStatement gK = null;
         Connection c = null;
         try{
             c= getConnection();
             gK = c.prepareStatement(selectLinkByIdSQL);
             gK.setInt(1, id);
             ResultSet rs = gK.executeQuery();
             if(rs.next()){
                 l = new Link(rs.getInt(1),getKeywordById(rs.getInt(2)),rs.getString(3),rs.getString(4));
             }
             rs.close();
             rs = null;
         }catch(SQLException e){
             e.printStackTrace();
         }finally{
             try{
             gK.close();
             }catch(SQLException e){
               e.printStackTrace();
             }finally{
                 try{
                 c.close();
                 }catch(SQLException e){
                     e.printStackTrace();
                 }
             }
         }
         return l;
     }
 
     public Collection<Knowledge> getKnowledgeByService(String service){
         PreparedStatement gK = null;
         Connection c = null;
         ResultSet result = null;
         List<Knowledge> resultList = new ArrayList<Knowledge>();
 
         try {
             c = getConnection();
             gK = c.prepareStatement(getKnowledgeByServiceSQL);
             gK.setString(1, service);
             result = gK.executeQuery();
             while(result.next()){
                 resultList.add(new Knowledge(result.getInt(1), 
                         getLinkById(result.getInt(2)),
                         result.getString(3),
                         result.getInt(4)));
             }
             result.close();
             result = null;
         } catch (SQLException e) {
             e.printStackTrace();
         }finally{
             try {
                 c.close();
             } catch (SQLException e) {
                 e.printStackTrace();
             }
         }
         return resultList;
 
 }
 
 public Collection<Knowledge> getKnowledgeByKeyword(String keyword){
         PreparedStatement gK = null;
         Connection c = null;
         ResultSet result = null;
         List<Knowledge> resultList = new ArrayList<Knowledge>();
 
         try {
             c = getConnection();
             gK = c.prepareStatement(getKnowledgeByKeywordSQL);
             gK.setString(1, keyword);
             result = gK.executeQuery();            
             while(result.next()){
                 resultList.add(new Knowledge(result.getInt(1),
                         new Link(result.getInt(2),getKeyword(keyword),result.getString(3),""),
                         result.getString(4),
                         result.getInt(5)));
             }
             result.close();
             result = null;
         } catch (SQLException e) {
             e.printStackTrace();
         }finally{
             try {
                 c.close();
             } catch (SQLException e) {
                 e.printStackTrace();
             }
         }
         return resultList;
 }
 
     public Collection<Link> getNewLinks(String tag){
 
         PreparedStatement gK = null;
         Connection c = null;
         ResultSet result = null;
         List<Link> resultList = new ArrayList<Link>();
         try {
             c = getConnection();
             gK = c.prepareStatement(getNewLinks);
             gK.setInt(1, getKeyword(tag).getId());
             result = gK.executeQuery();
             while(result.next()){
                 resultList.add(
                         new Link(result.getInt(1),
                         getKeywordById(result.getInt(2)),
                         result.getString(3),
                         result.getString(4))
                 );
             }
             result.close();
             result = null;
         } catch (SQLException e) {
             e.printStackTrace();
         }finally{
             try {
                 c.close();
             } catch (SQLException e) {
                 e.printStackTrace();
             }
         }
         return resultList;
     }
 
     public int getTagId(String tagvalue){
         PreparedStatement gi = null;
         Connection c = null;
         ResultSet result = null;
         int id = 0;
         try{
             c = getConnection();
             gi = c.prepareStatement(getTagId);
             gi.setString(1, tagvalue);
             result = gi.executeQuery();
             if(result.next()){
                 id = result.getInt(1);
             }
             result.close();
         }catch(SQLException e){
             e.printStackTrace();
         }finally{
             try{
                 c.close();
             }catch(SQLException e){
                 e.printStackTrace();
             }
         }
         return id;
     }
 
     public Link saveLink(String link, String tag){
         PreparedStatement sl = null;
         Connection c = null;
         int tag_id = getTagId(tag);
         int autoID = -1;
         try {
             c = getConnection();
             sl = c.prepareStatement(saveLinkQUERY,Statement.RETURN_GENERATED_KEYS);
             sl.setString(1, link);
             sl.setString(2, "nada");
             sl.setInt(3, tag_id);
             sl.executeUpdate();
             ResultSet keys = sl.getGeneratedKeys();
             if(keys.next()){
                 autoID = keys.getInt(1);
             }else{
                 System.out.println("Can't insert the link");
             }
             keys.close();
             keys = null;
         } catch (SQLException e) {
             e.printStackTrace();
         }finally{
             try{
                 c.close();
             }catch(SQLException e){
                 e.printStackTrace();
             }
         }
         return new Link(autoID, new Keyword(tag_id, tag), link, "");
     }
 
     public Collection<String> getRelatedWords(String word, int column){
         PreparedStatement gK = null;
         Connection c = null;
         ResultSet result = null;
         List<String> resultList = new ArrayList<String>();
         try {
             c = getConnection();
             if(column==1){
                 gK = c.prepareStatement(getRelatedTags1);
             }else{
                 gK = c.prepareStatement(getRelatedTags2);
             }
             gK.setString(1, word);
             result = gK.executeQuery();
             while(result.next()){
                 resultList.add(result.getString(1));
             }
             result.close();
             result = null;
         } catch (SQLException e) {
             e.printStackTrace();
         }finally{
             try {
                 c.close();
             } catch (SQLException e) {
                 e.printStackTrace();
             }
         }
         return resultList;
     }
 }
