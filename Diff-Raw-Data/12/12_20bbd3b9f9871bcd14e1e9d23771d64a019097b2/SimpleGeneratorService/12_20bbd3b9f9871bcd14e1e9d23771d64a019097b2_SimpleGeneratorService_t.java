 package com.github.urlchopper.service.simple;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.github.urlchopper.repository.ShortUrlRepository;
 import com.github.urlchopper.service.GeneratorService;
 
 /**
  * .
  * @author Marton_Sereg
  *
  */
 @Service
 public class SimpleGeneratorService implements GeneratorService {
 
     private static final int MULTIPLY = 1000;
 
     private static final Integer GENERATED_URL_LENGTH = 4;
 
     private List<Character> characters = new ArrayList<Character>();
 
     @Autowired
     private ShortUrlRepository shortUrlRepository;
 
     /**
      * todo: constants.
      */
     @PostConstruct
     public void createCharacterList() {
         for (int i = 65; i <= 90; i++) {
             characters.add((char) i);
         }
         for (int i = 97; i <= 122; i++) {
             characters.add((char) i);
         }
         for (int i = 48; i <= 57; i++) {
             characters.add((char) i);
         }
     }
 
     @Override
     public String generate(String originalUrl) {
 
         String ret = "";
         double rnd = Math.random();
         for (int i = 0; i < GENERATED_URL_LENGTH; i++) {
             int index = (int) ((rnd * MULTIPLY) % characters.size());
            ret += characters.get(index);
         }
 
         return ret;
     }
 
     @Override
     public String findActiveShortUrl(String shortUrl) {
 
         return null;
     }

    private boolean isExistUrl(String url) {
         boolean ret = false;

         return ret;
     }
 
 }
