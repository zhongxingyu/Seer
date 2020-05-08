 package com.nkhoang.gae.service.impl;
 
 import com.nkhoang.gae.dao.UserWordDao;
 import com.nkhoang.gae.dao.VocabularyDao;
 import com.nkhoang.gae.model.User;
 import com.nkhoang.gae.model.UserWord;
 import com.nkhoang.gae.model.Word;
 import com.nkhoang.gae.service.UserService;
 import org.eclipse.jetty.server.Authentication;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 public class UserServiceImpl implements UserService {
     @Autowired
     private VocabularyDao vocabularyDao;
     @Autowired
     private UserWordDao userWordDao;
 
 
     public User getCurrentUser() {
         Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
         User currentUser = null;
         if (principal != null) {
             currentUser = (User) principal;
         }
 
         return currentUser;
     }
 
     @PreAuthorize("hasRole('ROLE_USER')")
     public UserWord addWord(Long wordId) {
         // check word existence.
         Word w = vocabularyDao.get(wordId);
         if (w != null) {
             // ok to proceed.
             User currentUser = getCurrentUser();
             if (currentUser != null) {
                return userWordDao.save(currentUser.getId(), wordId);
             }
         }
         return null;
     }
 
     @PreAuthorize("hasRole('ROLE_USER')")
     public List<Word> getWordsFromUser() {
         User user = getCurrentUser();
         if (user != null) {
             List<UserWord> userWords = userWordDao.getWordFromUser(user.getId());
             List<Long> wordIds = new ArrayList<Long>();
             for (UserWord userWord : userWords) {
                 wordIds.add(userWord.getWordId());
             }
 
             List<Word> words = vocabularyDao.get(wordIds);
 
             return words;
         }
 
         return null;
     }
 
     public void setVocabularyDao(VocabularyDao vocabularyDao) {
         this.vocabularyDao = vocabularyDao;
     }
 
     public void setUserWordDao(UserWordDao userWordDao) {
         this.userWordDao = userWordDao;
     }
 }
