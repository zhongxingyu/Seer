 package edu.northwestern.bioinformatics.studycalendar.security;
 
 import edu.northwestern.bioinformatics.studycalendar.dao.UserDao;
 import edu.northwestern.bioinformatics.studycalendar.domain.Fixtures;
 import edu.northwestern.bioinformatics.studycalendar.domain.User;
 import edu.northwestern.bioinformatics.studycalendar.testing.StudyCalendarTestCase;
 import edu.northwestern.bioinformatics.studycalendar.service.UserService;
 import org.acegisecurity.userdetails.UserDetails;
 import static org.easymock.EasyMock.expect;
 
 public class PscUserDetailsServiceTest extends StudyCalendarTestCase {
     private User user;
     private UserService userService;
     private PscUserDetailsService service;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
 
         userService = registerMockFor(UserService.class);
 
         service = new PscUserDetailsService();
         service.setUserService(userService);
 
         user = Fixtures.createUser(1, "John", 1L, true);
     }
 
     public void testLoadUserGivesActualPscUser() throws Exception {
         expect(userService.getUserByName(user.getName())).andReturn(user);
         replayMocks();
 
         UserDetails actualUserDetails = service.loadUserByUsername(user.getName());
         verifyMocks();
 
         assertSame(user, actualUserDetails);
     }
 }
