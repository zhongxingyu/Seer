 package de.flower.rmt.service;
 
 import de.flower.common.service.security.IPasswordGenerator;
 import de.flower.common.util.Check;
 import de.flower.rmt.model.Role;
 import de.flower.rmt.model.Team;
 import de.flower.rmt.model.User;
 import de.flower.rmt.model.User_;
 import de.flower.rmt.model.type.Password;
 import de.flower.rmt.repository.IRoleRepo;
 import de.flower.rmt.repository.IUserRepo;
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
 import static de.flower.rmt.repository.Specs.eq;
 import static de.flower.rmt.repository.Specs.fetch;
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
         if (user.isNew()) {
             notEmpty(user.getRoles(), "user has no role(s) assigned");
         }
         userRepo.save(user);
         if (user.isNew()) {
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
         Specification hasClub = eq(User_.club, getClub());
         List<User> list = userRepo.findAll(where(hasClub).and(fetch(attributes)));
         return list;
     }
 
     @Override
     public List<User> findAllByTeam(final Team team) {
         Check.notNull(team);
         return userRepo.findByTeam(team);
     }
 
     @Override
     public List<User> findAllUnassignedPlayers(final Team team) {
         return userRepo.findUnassignedPlayers(team, getClub());
     }
 
     @Override
     @Transactional(readOnly = false)
     public void delete(User user) {
         // do not allow to delete currently logged in user
         if (securityService.isCurrentUser(user)) {
             throw new IllegalArgumentException("Cannot delete currently logged in user.");
         }
         userRepo.delete(user);
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
         return user;
     }
 
     @Override
     @Transactional(readOnly = false)
     public void resetPassword(final User user, final boolean sendMail) {
         initPassword(user);
         if (sendMail) {
             notificationService.sendResetPasswordMail(user);
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
