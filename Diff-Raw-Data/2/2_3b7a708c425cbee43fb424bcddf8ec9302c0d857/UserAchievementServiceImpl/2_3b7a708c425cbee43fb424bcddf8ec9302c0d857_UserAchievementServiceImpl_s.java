 package de.groupon.hcktn.groupong.service.impl;
 
 import de.groupon.hcktn.groupong.model.dao.UserAchievementDAO;
 import de.groupon.hcktn.groupong.model.entity.UserAchievement;
 import de.groupon.hcktn.groupong.service.UserAchievementService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Service
 public class UserAchievementServiceImpl implements UserAchievementService {
 
     @Autowired
     private UserAchievementDAO userAchievementDAO;
 
     @Override
     public List<Integer> fetchUserAchievementsByUserId(final Integer userId) {
         final List<Integer> achievementsIds = new ArrayList<Integer>();
         final List<UserAchievement> userAchievements = userAchievementDAO.retrieveByUserId(userId);
         if (userAchievements != null) {
             for (UserAchievement userAchievement : userAchievements) {
                achievementsIds.add(userAchievement.getUserId());
             }
         }
         return achievementsIds;
     }
 }
