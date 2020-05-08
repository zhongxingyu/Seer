 package de.groupon.hcktn.groupong.achievements;
 
 import de.groupon.hcktn.groupong.domain.mappers.MatchDTOMapper;
 import de.groupon.hcktn.groupong.domain.response.MatchDTO;
 import de.groupon.hcktn.groupong.model.dao.MatchDAO;
 import de.groupon.hcktn.groupong.model.entity.Match;
 import de.groupon.hcktn.groupong.model.entity.User;
 import de.groupon.hcktn.groupong.service.MatchService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.List;
 
 @Component("twoInARowAchievement")
 public class TwoInARowAchievement implements Achievement {
 
     @Autowired
     private MatchService matchService;
 
     @Autowired
     private MatchDAO matchDAO;
 
     @Autowired
     private MatchDTOMapper matchDTOMapper;
 
     @Override
     public Integer getId() {
         return 3;
     }
 
 
     private MatchDTO findLastMatch(List<MatchDTO> matches) {
        Long lastDate = matchDTOMapper.formatTime(matches.get(0).getDate());
         MatchDTO lastMatch = matches.get(0);
         for (MatchDTO thisMatch : matches) {
            Long thisDate = matchDTOMapper.formatTime(thisMatch.getDate());
            if (thisDate >= lastDate) {
                 lastDate = thisDate;
                 lastMatch = thisMatch;
             }
         }
         return lastMatch;
     }
 
 
     private Boolean isUserWinner(Match match, User user) {
         return isUserWinner(matchDTOMapper.mapToMatchDTO(match), user);
     }
 
 
     private Boolean isUserWinner(MatchDTO match, User user) {
         if (match.getScoreUser1() > match.getScoreUser2()) {
            if (user.getId().equals(match.getUser1Id())) {
                 return true;
             }
         }
         if (match.getScoreUser2() > match.getScoreUser1()) {
            if (user.getId().equals(match.getUser2Id())) {
                 return true;
             }
         }
         return false;
     }
 
 
     @Override
     public Boolean isAchieved(final User user, final Match match) {
         if (isUserWinner(match, user)) {
             List<MatchDTO> oldMatches = matchService.fetchMatchesByUserIdStatusId(user.getId(), 6);
            if ( (oldMatches == null) || (oldMatches.isEmpty() )) {
                 return false;
             }
             MatchDTO lastMatch = findLastMatch(oldMatches);
             if(isUserWinner(lastMatch, user)) {
                 return true;
             } else {
                 return false;
             }
         }   else {
             return false;
         }
     }
 }
