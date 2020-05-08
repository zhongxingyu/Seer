 package com.profbingo.android.webdata;
 
 import java.util.List;
 
 import com.profbingo.android.model.Category;
 import com.profbingo.android.model.GameBoard;
 import com.profbingo.android.model.Professor;
 
 
 public interface WebDataAdapter {
     
     public String login(String email, String password);
     
     public boolean login(String authCode);
     
     public boolean register(String email, String firstName, String lastName, String password);
     
     public boolean logout();
     
     public boolean isLoggedIn();
     
     public List<Category> getCategories();
     
     public List<Professor> getProfessors();
     
     public GameBoard getNewBoard(Professor professor);
 }
