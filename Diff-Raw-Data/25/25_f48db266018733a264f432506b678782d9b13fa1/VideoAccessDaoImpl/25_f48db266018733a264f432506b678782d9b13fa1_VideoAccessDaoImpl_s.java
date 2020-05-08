 package com.team33.entities.dao;
 
 import com.team33.entities.LoginToken;
 import com.team33.entities.VideoInfo;
 import com.team33.services.exception.AccountNotActivatedException;
 import com.team33.services.exception.DataAccessException;
 import java.util.List;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 /**
  * Data Access layer for the video access features
  *
  * @author Mark,Samual
  */
 @Repository
 public class VideoAccessDaoImpl implements VideoAccessDao {
 
     @Autowired
     private SessionFactory sessionFactory;
 
     /**
      * Retrieves the video info for video with videoInfoId for user with login
      * token id
      *
      * @param videoInfoId
      * @param uuid
      * @return VideoInfo
      * @throws DataAccessException
      * @throws AccountNotActivatedException
      */
     @Override
     public VideoInfo getVideoInfo(int videoInfoId, int uuid) throws DataAccessException, AccountNotActivatedException {
         return (VideoInfo) this.sessionFactory.getCurrentSession().get(VideoInfo.class, videoInfoId);
     }
 
     /**
      * Retrieves a list of video info provided the login token id
      *
      * @param uuid
      * @return List<VideoInfo>
      * @throws DataAccessException
      * @throws AccountNotActivatedException
      */
     @Override
     public List<VideoInfo> getVideoInfoList(int uuid) throws DataAccessException, AccountNotActivatedException {
         return sessionFactory.getCurrentSession().getNamedQuery("VideoInfo.findAll").list();
     }
 
     /**
      * Retrieves a login token with id
      *
      * @param uuid
      * @return LoginToken
      * @throws DataAccessException
      */
     @Override
     public LoginToken getLoginToken(int uuid) throws DataAccessException {
        if (sessionFactory.getCurrentSession().get(LoginToken.class, uuid) != null) {
            return (LoginToken) sessionFactory.getCurrentSession().get(LoginToken.class, uuid);
         }
        throw new DataAccessException("The activation key is invalid");
     }
 }
