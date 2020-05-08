 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package ohtu.refero.service;
 
import java.util.ArrayList;
 import java.util.List;
 import ohtu.refero.models.Author;
 import ohtu.refero.repositories.AuthorRepository;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
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
         throw new UnsupportedOperationException("Not supported yet.");
     }
     
 }
