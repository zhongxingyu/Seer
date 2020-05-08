 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package com.mti.webshare.daoimpl;
 
 import com.mti.webshare.dao.FileDAO;
 import com.mti.webshare.model.Event;
 import com.mti.webshare.model.FileUploaded;
 import com.mti.webshare.model.User;
 import com.mti.webshare.model.UserFile;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 import org.hibernate.Query;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  *
  * @author yoan
  * EPITA MTI 2013
  *
  */
 
 @Repository
 @Transactional
 public class FileDAOImpl implements FileDAO
 {
     @Autowired
     private SessionFactory sessionFactory;
 
     @Override
     public Integer create(String name, Boolean ispublic, String path, Boolean isDir, User user, Boolean isroot, Integer parent_id)
     {
         try
         {
             FileUploaded file = new FileUploaded();
             
             file.setDeleted(Boolean.FALSE);
             file.setIsDir(isDir);
             file.setIsPublic(ispublic);
             file.setName(name);
             file.setPath(path);
             if (parent_id != null && parent_id != 0)
             {
                 file.setParent_id(parent_id);
             }
             sessionFactory.getCurrentSession().save(file);
             
             if (parent_id != null && parent_id != 0)
             {
                 UserFile userfile = new UserFile();
                 userfile.setFile(file);
                 userfile.setUser(user);
                 userfile.setState(Boolean.TRUE);
 
                 file.getUserFile().add(userfile);
                 sessionFactory.getCurrentSession().save(userfile);
             }
             
             Event event = new Event();
             event.setEventAction(1);
             event.setEventDate(new Date());
             event.setFile_id(file.getId());
             event.setUser_id(user.getId());
             
             sessionFactory.getCurrentSession().save(event);
             
             return file.getId();
         }
         catch (Exception e)
         {
             return null;
         }
     }
 
     @Override
     public Boolean update(FileUploaded file)
     {
         try
         {
             sessionFactory.getCurrentSession().update(file);
             return true;
         }
         catch (Exception e)
                 {
                     return false;
                 }
     }
     
     @Override
     public Boolean deleted(FileUploaded file)
     {
         try
         {
             file.setDeleted(Boolean.TRUE);
             sessionFactory.getCurrentSession().update(file);
             return true;
         }
         catch (Exception e)
         {
             return false;
         }
     }
 
     @Override
     public FileUploaded get(int id)
     {
         try
         {
             Query q = sessionFactory.getCurrentSession().createQuery("from FileUploaded where id = ?");
             q.setParameter(0, id);
             FileUploaded file = (FileUploaded) q.uniqueResult();
             sessionFactory.getCurrentSession().persist(file);
             return file;
         }
         catch (Exception e) 
         {          
             return null;
         }
     }
 
     @Override
     public List<FileUploaded> getList()
     {
         try 
         {
             Query q = sessionFactory.getCurrentSession().createQuery("from FileUploaded");  
             List<FileUploaded> files = q.list();
             
             return files;
         }
         catch (Exception e) 
         {
             return null;
         }
     }
     
     @Override
     public List<FileUploaded> getFolderContent(int id) {
         try 
         {
             Query q = sessionFactory.getCurrentSession().createQuery("from FileUploaded where parent_id = :parent_id");  
             q.setParameter("parent_id", id);
             List<FileUploaded> files = q.list();
             
             return files;
         }
         catch (Exception e) 
         {
             return null;
         }
     }
     
     @Override
     public String toJson(FileUploaded file){
         JSONObject json = new JSONObject();
         try {
             json.put("id", file.getId());
             json.put("name", file.getName());
             json.put("path",file.getPath());
             json.put("isPublic", file.getIsPublic());
             json.put("isDirectory", file.getIsDir());
             return json.toString();
         } catch (JSONException ex) {
             Logger.getLogger(UserDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         }
     }
 
      @Override
      public String toJson(List<FileUploaded> file_list){
     
         JSONObject json = new JSONObject();
                 try {
             for(FileUploaded file:file_list){
              json.accumulate("files", toJson(file));   
             }
             return json.toString();
         } catch (JSONException ex) {
             Logger.getLogger(UserDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         }
     }
 
     @Override
     public List<FileUploaded> getRootFolder(int id) {
          try 
         {
            Query q = sessionFactory.getCurrentSession().createQuery("from user where id = :id");  
             q.setParameter("id", id);
             User user = (User) q.uniqueResult();
             Set<UserFile> set_files = user.getUserFile();
             List<FileUploaded> list = new ArrayList<FileUploaded>();
             for (UserFile userfile : set_files)
             {
                 list.add(userfile.getFile());
             }
             
             return list;
         }
         catch (Exception e) 
         {
             return null;
         }
     }
 
 
 }
