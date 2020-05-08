 package ohtu.refero.repositories;
 
 import ohtu.refero.models.Article;
 import org.springframework.data.jpa.repository.JpaRepository;
 
 public interface ArticleRepository extends JpaRepository<Article, Long> {
 
//    Article findByReferenceId(String id);
 }
