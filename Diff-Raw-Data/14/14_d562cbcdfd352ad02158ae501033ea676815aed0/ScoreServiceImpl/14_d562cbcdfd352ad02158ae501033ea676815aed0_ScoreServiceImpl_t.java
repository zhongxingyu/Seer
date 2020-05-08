 package de.groupon.hcktn.groupong.service.impl;
 
 import de.groupon.hcktn.groupong.domain.response.MatchDTO;
 import de.groupon.hcktn.groupong.model.dao.UserDAO;
 import de.groupon.hcktn.groupong.model.dao.impl.UserDAOImpl;
 import de.groupon.hcktn.groupong.model.entity.Match;
 import de.groupon.hcktn.groupong.model.entity.User;
 import de.groupon.hcktn.groupong.service.ScoreService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service
 public class ScoreServiceImpl implements ScoreService {
     final Integer scoreMultiplier = 100;
     final Integer winBonus = 50;
 
     @Autowired
     private UserDAO myUserDAO;
 
     @Override
     public void score(Match match, MatchDTO matchDTO) {
         if ((match.getStatusId()==4 || match.getStatusId()==5) && matchDTO.getStatusId()==6) {
            User user1 = myUserDAO.retrieve(match.getUser1Id());
            User user2 = myUserDAO.retrieve(match.getUser2Id());
             Integer user1ActualScore = user1.getScore();
             Integer user2ActualScore = user2.getScore();
            Integer user1NewScore = user1ActualScore + (match.getScoreUser1() * scoreMultiplier);
            Integer user2NewScore = user2ActualScore + (match.getScoreUser2() * scoreMultiplier);
            if (match.getScoreUser1() > match.getScoreUser2()) {
                 user1NewScore += winBonus;
            } else if (match.getScoreUser1() < match.getScoreUser2()) {
                 user2NewScore += winBonus;
             }
             user1.setScore(user1NewScore);
             user2.setScore(user2NewScore);
             myUserDAO.update(user1);
             myUserDAO.update(user2);
         }
     }
 }
