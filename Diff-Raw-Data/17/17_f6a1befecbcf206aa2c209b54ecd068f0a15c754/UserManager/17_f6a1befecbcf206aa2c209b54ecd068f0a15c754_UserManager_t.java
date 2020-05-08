 package de.flower.rmt.service;
 
 import com.google.common.base.Predicate;
 import de.flower.common.service.security.IPasswordGenerator;
 import de.flower.common.util.Check;
 import de.flower.common.util.NameFinder;
 import de.flower.rmt.model.db.entity.*;
 import de.flower.rmt.model.db.entity.event.Event;
 import de.flower.rmt.model.dto.Password;
 import de.flower.rmt.repository.IRoleRepo;
 import de.flower.rmt.repository.IUserRepo;
 import de.flower.rmt.repository.Specs;
 import de.flower.rmt.service.mail.INotificationService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.jpa.domain.Specification;
 import org.springframework.security.authentication.encoding.PasswordEncoder;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.persistence.metamodel.Attribute;
 import javax.validation.Validator;
 import java.util.List;
 
 import static de.flower.common.util.Check.*;
 import static de.flower.rmt.repository.Specs.*;
 import static org.springframework.data.jpa.domain.Specifications.where;
 
 /**
  * @author flowerrrr
  */
 @Service
 @Transactional(readOnly = true, propagation = Propagation.REQUIRED)
 public class UserManager extends AbstractService implements IUserManager {
 
     @Autowired
     private IUserRepo userRepo;
 
     @Autowired
     private IRoleRepo roleRepo;
 
     @Autowired
     private PasswordEncoder passwordEncoder;
 
     @Autowired
     private IPasswordGenerator passwordGenerator;
 
     @Autowired
     private INotificationService notificationService;
 
     @Autowired
     private IRoleManager roleManager;
 
     @Autowired
     private IPlayerManager playerManager;
 
     @Autowired
     private Validator validator;
 
     @Override
     public User loadById(Long id, final Attribute... attributes) {
         User entity = userRepo.findOne(where(eq(User_.id, id)).and(fetch(attributes)).and(fetch(User_.club)));
         Check.notNull(entity);
         assertClub(entity);
         return entity;
     }
 
     @Override
     public User findByUsername(String username, final Attribute... attributes) {
         User entity = userRepo.findOne(where(eq(User_.email, username)).and(fetch(attributes)));
         return entity;
     }
 
     @Override
     @Transactional(readOnly = false)
     public void save(User user) {
         // check that a role is assigned
         boolean isNew = user.isNew();
         if (isNew) {
             notEmpty(user.getRoles(), "user has no role(s) assigned");
         }
         userRepo.save(user);
         if (isNew) {
             for (Role role : user.getRoles()) {
                 roleRepo.save(role);
             }
         }
     }
 
     @Override
     @Transactional(readOnly = false)
     public void save(final User user, final boolean isManager) {
         validate(user);
         save(user);
         if (isManager) {
             roleManager.addRole(user.getId(), Role.Roles.MANAGER.getRoleName());
         } else {
             roleManager.removeRole(user.getId(), Role.Roles.MANAGER.getRoleName());
         }
     }
 
     @Override
     public List<User> findAll(final Attribute... attributes) {
         List<User> list = userRepo.findAll(where(fetch(attributes)).and(asc(User_.fullname)));
         return list;
     }
 
     @Override
     public List<User> findAllFetchTeams(final Attribute... attributes) {
         List<User> list = userRepo.findAllFetchTeams(getClub());
         return list;
     }
 
     @Override
     public List<User> findAllByTeam(final Team team) {
         Check.notNull(team);
         Specification spec = Specs.joinEq(User_.players, Player_.team, team);
         return userRepo.findAll(where(spec).and(asc(User_.fullname)));
     }
 
     @Override
     public List<User> findAllUnassignedPlayers(final Team team) {
         return userRepo.findAllUnassignedPlayers(team, getClub());
     }
 
     @Override
     public List<User> findAllUninvitedPlayers(final Event event) {
         return userRepo.findAllUninvitedPlayers(event, getClub());
     }
 
     @Override
     public List<User> findAllUninvitedPlayersByTeam(final Event event, final Team team) {
         // get list of uninvited users and list of users of this team and intersect both sets.
         List<User> uninvited = findAllUninvitedPlayers(event);
         List<User> usersByTeam = findAllByTeam(team);
         uninvited.retainAll(usersByTeam);
         return uninvited;
     }
 
     @Override
     @Transactional(readOnly = false)
     public void delete(Long id) {
         User entity = loadById(id);
         // do not allow to delete currently logged in user
         if (securityService.isCurrentUser(entity)) {
             throw new IllegalArgumentException("Cannot delete currently logged in user.");
         }
         entity.setEmail(NameFinder.delete(entity.getEmail(), new Predicate<String>() {
             @Override
             public boolean apply(final String candidate) {
                 return userRepo.findByEmail(candidate) == null;
             }
         }));
         entity.setFullname(NameFinder.delete(entity.getFullname(), new Predicate<String>() {
             @Override
             public boolean apply(final String candidate) {
                 return userRepo.findByFullname(candidate) == null;
             }
         }));
         userRepo.softDelete(entity);
         // delete user from all teams
         playerManager.removeUserFromAllTeams(entity);
 
     }
 
     @Override
     public User newInstance() {
 
         User user = new User(getClub());
         Role role = new Role(Role.Roles.PLAYER.getRoleName());
         user.getRoles().add(role);
         role.setUser(user);
         initPassword(user);
         // by default a new user is assumed to be fit.
         user.setStatus(User.Status.FIT);
         user.setEnabled(true);
         return user;
     }
 
     @Override
     @Transactional(readOnly = false)
     public void resetPassword(final User user, final boolean sendMail) {
         initPassword(user);
         if (sendMail) {
             notificationService.sendResetPasswordMail(user, securityService.getUser());
             // even if user hasen't received invitation mail yet
             user.setInvitationSent(true);
         }
         userRepo.save(user);
     }
 
     private void initPassword(User user) {
         String initialPassword = passwordGenerator.generatePassword();
         user.setInitialPassword(initialPassword);
         user.setEncryptedPassword(encodePassword(initialPassword));
     }
 
     @Override
     public void updatePassword(final Long userId, Password password) {
         Check.notNull(userId);
         User user = loadById(userId);
         validate(password);
         user.setInitialPassword(null);
         user.setEncryptedPassword(encodePassword(password.getNewPassword()));
        userRepo.save(user);
     }
 
     private String encodePassword(String password) {
         return passwordEncoder.encodePassword(password, null);
     }
 
     @Override
     @Transactional(readOnly = false)
     public void sendInvitation(Long userId) {
         User user = loadById(userId);
         notificationService.sendInvitationNewUser(user, securityService.getUser());
         user.setInvitationSent(true);
         userRepo.save(user);
     }
 
 }
