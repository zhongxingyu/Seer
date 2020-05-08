 package ru.mrgrechkinn.java.foh.dao;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import ru.mrgrechkinn.java.foh.model.User;
 import ru.mrgrechkinn.java.foh.util.IOUtils;
 
 public class UserDAOImpl implements UserDAO {
 
     public static final String fileName = "test.txt";
     private File file;
 
     public UserDAOImpl(){
         file = new File(fileName);
         if (!file.exists()){
             try {
                 file.createNewFile();
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
         }
     }
     
     @Override
     public boolean save(User user) {
         boolean isNewUser = true;
         long lastUserId = 0;
         List<User> users = new ArrayList<User>();
         try {
             BufferedReader reader = new BufferedReader(new FileReader(file));
             
             for (String s = reader.readLine(); s != null; s = reader.readLine()) {
                 User newUser = new User();
                 long userId = Long.parseLong(s);
                 if (userId > lastUserId) {
                     lastUserId = userId;
                 }
                 newUser.setId(userId);
                 newUser.setLogin(reader.readLine());
                 newUser.setPassword(reader.readLine());
                 newUser.setFullName(reader.readLine());
                 users.add(newUser);
             }
 
             if (!users.isEmpty()) {
                 for (User u : users) {
                     if (u.getLogin().equals(user.getLogin())) {
                         u.setFullName(user.getFullName());
                         u.setPassword(user.getPassword());
                         isNewUser = false;
                         break;
                     }
                 }
             }
 
             if (isNewUser) {
                 user.setId(lastUserId + 1);
                 users.add(user);
             }
             IOUtils.closeQuietly(reader);
             
             FileWriter writer = new FileWriter(file);
             for (User u : users) {
                 writer.write(u.getId() + "\n");
                 writer.write(u.getLogin() + "\n");
                 writer.write(u.getPassword() + "\n");
                 writer.write(u.getFullName() + "\n");
             }
             IOUtils.closeQuietly(writer);
             return true;
         } catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return false;
     }
 
     @Override
     public User getUserById(long id) {
        User user = new User();
         try {
           
             BufferedReader reader = new BufferedReader(new FileReader(file));
             List<User> users = new ArrayList<User>();
            
             for (String s = reader.readLine(); s != null; s = reader.readLine()) {
                 User newUser = new User();
                 newUser.setId(Long.parseLong(s));
                 newUser.setLogin(reader.readLine());
                 newUser.setPassword(reader.readLine());
                 newUser.setFullName(reader.readLine());
                 users.add(newUser);
             }
             IOUtils.closeQuietly(reader);
             
             for (User u : users) {
                 if (u.getId() == id) {
                     return u;
                 }
             }
            return null;
            
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
        
        return user;
     }
 
     @Override
     public boolean delete(User user) {
         
         try {
             
             BufferedReader reader = new BufferedReader(new FileReader(file));
             
             List<User> users = new ArrayList<User>();
             List<User> usersChange = new ArrayList<User>();
             
             try {
                 for (String s = reader.readLine(); s != null; s = reader.readLine()) {
                     User newUser = new User();
                     newUser.setId(Long.parseLong(s));
                     newUser.setLogin(reader.readLine());
                     newUser.setPassword(reader.readLine());
                     newUser.setFullName(reader.readLine());
                     users.add(newUser);
                 }
                 IOUtils.closeQuietly(reader);
                 
                 for (User u: users) {
                     User newUser = new User();
                     if (!u.getLogin().equals(user.getLogin())) {
                         newUser.setId(u.getId());
                         newUser.setLogin(u.getLogin());
                         newUser.setPassword(u.getPassword());
                         newUser.setFullName(u.getFullName());
                         usersChange.add(newUser);
                         
                     }
                 }
                 FileWriter writer = new FileWriter(file);
                 for (User u: usersChange) {
                     writer.write(u.getId() + "\n");
                     writer.write(u.getLogin() + "\n");
                     writer.write(u.getPassword() + "\n");
                     writer.write(u.getFullName() + "\n");
                 }
                 IOUtils.closeQuietly(writer);
                 return true;
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             
         } catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         
         return false;
     }
 
     @Override
     public List<User> getAllUsers() {
         
         List<User> listUser = new ArrayList<User>();
         
         try {
             BufferedReader reader = new BufferedReader(new FileReader(file));
             
             
             try {
                 
                 for (String s = reader.readLine(); s != null; s = reader.readLine()) {
                     User newUser = new User();
                     newUser.setId(Long.parseLong(s));
                     newUser.setLogin(reader.readLine());
                     newUser.setPassword(reader.readLine());
                     newUser.setFullName(reader.readLine());
                     listUser.add(newUser);
                 }
                 IOUtils.closeQuietly(reader);
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             
         } catch (FileNotFoundException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return listUser;
     }
     
     
 }
