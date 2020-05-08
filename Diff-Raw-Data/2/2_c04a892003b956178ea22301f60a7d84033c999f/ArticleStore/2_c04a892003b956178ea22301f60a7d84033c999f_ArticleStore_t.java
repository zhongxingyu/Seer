 package Server;
 
 import Compute.Article;
 import org.skife.jdbi.v2.StatementContext;
 import org.skife.jdbi.v2.sqlobject.Bind;
 import org.skife.jdbi.v2.sqlobject.BindBean;
 import org.skife.jdbi.v2.sqlobject.SqlQuery;
 import org.skife.jdbi.v2.sqlobject.SqlUpdate;
 import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
 import org.skife.jdbi.v2.tweak.ResultSetMapper;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.*;
 
 /**
  * Database-backed possibly persistent article store.
  * <p/>
  * Thread-safety is dictated by the backing database--practically, instances will
  * be thread safe thanks to JDBI/H2.
  */
 @RegisterMapper(ArticleStore.ArticleMapper.class)
 public interface ArticleStore
 {
     @SqlUpdate("create table if not exists " +
               "articles (id int primary key auto_increment, parent int, content text)")
     void initializeTable();
 
     /**
      * Id is auto-incremented for ordering. Article id on the parameter is ignored. Used
      * by client's POST method.
      */
     @SqlUpdate("insert into articles (content, parent) values (:content, :parent)")
     void insert(@BindBean Article article);
 
     /**
      * Used for server replication, not by clients.
      */
     @SqlUpdate("insert into articles (id, content, parent) values (:id, :content, :parent)")
     void insertWithId(Article article);
 
     @SqlQuery("select * from articles")
     List<Article> getAll();
 
     @SqlQuery("select * from articles where id = :id")
     Article get(@Bind("id") int id);
 
     class ArticleMapper implements ResultSetMapper<Article>
     {
         @Override
         public Article map(int i, ResultSet resultSet, StatementContext statementContext)
         throws SQLException
         {
             return new Article(resultSet.getString("content"), resultSet.getInt("parent"));
         }
     }
 }
