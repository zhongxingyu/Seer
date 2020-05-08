 package de.groupon.hcktn.groupong.domain.mappers;
 
 import de.groupon.hcktn.groupong.domain.response.MatchDTO;
 import de.groupon.hcktn.groupong.model.entity.Match;
 import de.groupon.hcktn.groupong.service.StatusService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class MatchDTOMapper extends BaseDTOMapper {
 
     @Autowired
     private StatusService statusService;
 
     public MatchDTO mapToMatchDTO(final Match match)  {
         final MatchDTO matchDTO = new MatchDTO();
         matchDTO.setId(match.getId());
         matchDTO.setUser1Id(match.getUser1Id());
         matchDTO.setUser2Id(match.getUser2Id());
         matchDTO.setScoreUser1(match.getScoreUser1());
         matchDTO.setScoreUser2(match.getScoreUser2());
        matchDTO.setDate(formatTime(Long.valueOf(match.getMatchDate())));
         if (match.getStatusId() != null ) {
            matchDTO.setStatus(statusService.fetchStatus(match.getStatusId()).getDescription());
         }
         return matchDTO;
     }
 
     public Match mapToMatch(final MatchDTO matchDTO) {
         return update(new Match(), matchDTO);
     }
 
     public Match update(final Match match, final MatchDTO matchDTO) {
         if (matchDTO.getId() != null) {
             match.setId(matchDTO.getId());
         }
         if (matchDTO.getUser1Id() != null) {
             match.setUser1Id(matchDTO.getUser1Id());
         }
         if (matchDTO.getUser2Id() != null) {
             match.setUser2Id(matchDTO.getUser2Id());
         }
         if (matchDTO.getScoreUser1() != null) {
             match.setScoreUser1(matchDTO.getScoreUser1());
         }
         if (matchDTO.getScoreUser2() != null) {
             match.setScoreUser2(matchDTO.getScoreUser2());
         }
         if (matchDTO.getDate() != null) {
            match.setMatchDate(formatTime(Long.valueOf(matchDTO.getDate())));
         }
         if (matchDTO.getStatusId() != null) {
             match.setStatusId(matchDTO.getStatusId());
         }
         return match;
     }
 }
