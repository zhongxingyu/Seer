 package com.player.bll;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.*;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.Transaction;
 import org.hibernate.cfg.Configuration;
 
 public class UserAccount {
   
   public long id;
   public String username;
   public String password;
   public String password2; 
   public String email;
   public ArrayList<Playlist> playlists;
   
   public UserAccount() {
   }
 
   public UserAccount( String username, String password ) {
    System.out.println( "USERNAME: "+username+", PASSWORD: "+password );
     Query query = DataAccess.getSession().createQuery( "FROM UserAccount WHERE username = :username AND password = :password");
     
     UserAccount returnedUser = (UserAccount)query
             .setParameter( "username", username )
             .setParameter( "password", password )
             .uniqueResult();
     if (returnedUser != null ) {
       this.id = returnedUser.getId();
       this.username = returnedUser.getUsername();
       this.password = returnedUser.getPassword();
       this.email = returnedUser.getEmail();
     }
   }
   
   public UserAccount( String username, String password, String email ) {
     this.username = username;
     this.password = password;
     this.password2 = password2;
     this.email = email;
     Transaction tx = DataAccess.getSession().beginTransaction();
     DataAccess.getSession().save( this );
     tx.commit();
   }
 
   
   public static boolean userExists( String username ) {
     Query query = DataAccess.getSession().createQuery( "FROM UserAccount WHERE username = :username");
     query.setParameter( "username", username );
     if (username != null) {
             return ( query.list().size() > 0 );
         }
     else {
             return true;
         }
   }
   
   public boolean validate() {
     return ( this.username != null && this.password != null );
   }
   
   public boolean isValidPassword(String pwd) {
     String passReq="((?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{6,16})";  
     CharSequence input = pwd;  
     Pattern pattern = Pattern.compile(passReq);  
     Matcher matcher = pattern.matcher(input);  
     return matcher.matches();  
     }
     
     public boolean isValidUsername(String username) {
     String userReq="^[a-zA-Z][a-zA-Z0-9]{2,15}$";  
     CharSequence input = username;  
     Pattern pattern = Pattern.compile(userReq,Pattern.CASE_INSENSITIVE);  
     Matcher matcher = pattern.matcher(input);  
     return matcher.matches();  
     }
     
     public boolean isValidEmail(String emailAddress)
     {  
     String emailReq="^[\\w\\-]([\\.\\w])+[\\w]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";  
     CharSequence input = emailAddress;  
     Pattern pattern = Pattern.compile(emailReq,Pattern.CASE_INSENSITIVE);  
     Matcher matcher = pattern.matcher(input);  
     return matcher.matches();  
     }
   
   public boolean passwordMatch(String pwd, String cpwd){
       this.password = pwd;
       this.password2 = cpwd;
       return (cpwd.equals(pwd)); 
   }
   
   public long getId() {
     return id;
   }
   
   public void setId( long id ) {
     this.id = id;
   }
   
   public String getUsername() {
     return username;
   }
   
   public void setUsername( String username ) {
     this.username = username;
   }
   
   public String getPassword() {
     return password;
   }
   
   public void setPassword( String password ) {
     this.password = password;
   }
   
   public String getEmail() {
     return email;
   }
   
   public void setEmail( String email ) {
     this.email = email;
   }
 

  
   public ArrayList<Playlist> getPlaylists() {
     if ( playlists == null ) {
       Query query = DataAccess.getSession().createQuery( "FROM Playlist WHERE user_id = :user" );
       query.setParameter( "user", this.getId() );
       playlists = (ArrayList)query.list();
     }
     return playlists;
   }
 
   public void deletePlaylist( Playlist p ) {
     p.empty();
     playlists.remove( p );
     DataAccess.getSession().delete( p );
     Transaction tx = DataAccess.getSession().beginTransaction();
     tx.commit();
   }
 
   public void createPlaylist( String name ) {
     Playlist p = new Playlist( this, name );
     playlists.add( p );
   }
 
   public Object[][] getPlaylistsVector( ArrayList<Playlist> playlists ) {
     int arraySize = playlists.size();
     int objectSize = ( arraySize < 10 ) ? 10 : arraySize;
     Object[][] objectToReturn = new Object[objectSize][1];
     for ( int i = 0; i < arraySize; i++ ) {
       objectToReturn[i] = new Object[]{
         playlists.get(i).getName()
       };
     }
     return objectToReturn;
   }
   
 
 }
