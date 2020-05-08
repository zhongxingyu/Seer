 package de.flower.rmt.service;
 
 import de.flower.common.util.Check;
 import de.flower.rmt.model.*;
 import de.flower.rmt.repository.IPlayerRepo;
 import de.flower.rmt.repository.ITeamRepo;
 import de.flower.rmt.repository.IUserRepo;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.jpa.domain.Specification;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.criteria.*;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author flowerrrr
  */
 @Service
 @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
 public class TeamManager extends AbstractService implements ITeamManager {
 
     @Autowired
     private ITeamRepo teamRepo;
 
     @Autowired
     private IPlayerRepo playerRepo;
 
     @Autowired
     private IUserRepo userRepo;
 
     @Override
     @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
     public void save(Team entity) {
         teamRepo.save(entity);
     }
 
     @Override
     public Team loadById(Long id) {
         return Check.notNull(teamRepo.findOne(id));
     }
 
     @Override
     public List<Team> findAll() {
         return teamRepo.findAllByClub(getClub());
     }
 
     @Override
     public List<Team> findAllByUserPlayer(final User user) {
         Specification spec = new Specification<Team>() {
             @Override
             public Predicate toPredicate(final Root<Team> root, final CriteriaQuery<?> query, final CriteriaBuilder cb) {
                 final ListJoin<Team,Player> join = root.join(Team_.players);
                 return cb.equal(join.get(Player_.user), user);
             }
         };
         return teamRepo.findAll(spec);
     }
 
 
     @Override
     @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
     public void delete(Team entity) {
         // TODO (flowerrrr - 11.06.11) decide whether to soft or hard delete entity.
         teamRepo.delete(entity);
     }
 
     @Override
     public Team newInstance() {
         return new Team(getClub());
     }
 
     @Override
     @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
     public void addPlayer(Team team, User user) {
         addPlayers(team, Arrays.asList(user));
     }
 
     @Override
     @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
     public void addPlayers(Team team, List<User> users) {
         teamRepo.reattach(team);
         for (User user : users) {
             userRepo.reattach(user);
             // TODO (flowerrrr - 22.10.11) use service call of playerManager
             Player player = new Player(team, user);
             player.setOptional(false);
             player.setNotification(true);
             player.setRetired(false);
             team.getPlayers().add(player);
             user.getPlayers().add(player);
             playerRepo.save(player);
         }
     }
 
     @Override
     @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
     public void removePlayer(Team team, Player player) {
         teamRepo.reattach(team);
         Check.isTrue(team.getPlayers().contains(player));
         deletePlayer(player);
     }
 
     public void deletePlayer(Player player) {
         playerRepo.delete(player);
     }
 }
