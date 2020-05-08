 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ohtu.refero.service;
 
 import java.util.ArrayList;
 import java.util.List;
 import ohtu.refero.models.Article;
 import ohtu.refero.models.Author;
 import ohtu.refero.models.Book;
 import ohtu.refero.models.Inproceedings;
 import ohtu.refero.repositories.AuthorRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.jpa.repository.Modifying;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 public class AuthorServiceImp implements AuthorService {
     
     @Autowired
     AuthorRepository authorRepo;
     
     @Override
     public Author save(Author author) {
         return authorRepo.save(author);
     }
 
     @Override
     public List<Author> findAll() {
         return authorRepo.findAll();
     }
     
     @Modifying
     @Transactional
     public void setArticle(Long id, Article article) {
         Author auth = authorRepo.findOne(id);
         List<Article> articleList = auth.getArticleReferenceList();
         
         if (articleList == null) {
             articleList = new ArrayList<Article>();
         }
         
         articleList.add(article);
         auth.setArticleReferenceList(articleList);
         save(auth);
     }
    @Transactional
     @Override
     public List<Author> save(List<Author> authors) {      
         List<Author> actualAuthors = new ArrayList<Author>();
         
         for (Author author : authors) {
             if(authorRepo.findByFirstNameAndSurName(author.getFirstName(), author.getSurName()) == null) {
                 actualAuthors.add(save(author));
             } else {
                actualAuthors.add(authorRepo.findByFirstNameAndSurName(author.getFirstName(), author.getSurName())); 
             }
         }
        
         return actualAuthors;
 
     }
     
     @Override
     public Author findById(Long id) {
         return authorRepo.findOne(id);
     }
    @Modifying
    @Transactional
     @Override
     public void setBook(Long id, Book book) {
         Author auth = authorRepo.findOne(id);
         List<Book> bookList = auth.getBookReferenceList();
         
         if (bookList == null) {
             bookList = new ArrayList<Book>();
         }
         
         bookList.add(book);
         auth.setBookReferenceList(bookList);
         save(auth);
     }
    @Modifying
    @Transactional
     @Override
     public void setInproceedings(Long id, Inproceedings inproc) {
         Author auth = authorRepo.findOne(id);
         List<Inproceedings> inproList = auth.getInproceedingsReferenceList();
         
         if (inproList == null) {
             inproList = new ArrayList<Inproceedings>();
         }
         
         inproList.add(inproc);
         auth.setInproceedingsReferenceList(inproList);
         save(auth);
     }
     
 }
