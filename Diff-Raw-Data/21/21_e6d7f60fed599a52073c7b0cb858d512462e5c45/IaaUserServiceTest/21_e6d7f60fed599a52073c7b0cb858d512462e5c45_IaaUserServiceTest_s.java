 package edu.wisc.portlet.blackboardvcportlet.service.impl;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.sql.ResultSet;
 import java.util.Set;
 
 import org.jasig.portlet.blackboardvcportlet.data.BasicUser;
 import org.jasig.portlet.blackboardvcportlet.data.BasicUserImpl;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.mockito.stubbing.Answer;
 import org.springframework.jdbc.core.JdbcOperations;
 import org.springframework.jdbc.core.RowMapper;
 
 import com.google.common.collect.ImmutableList;
 
 @RunWith(MockitoJUnitRunner.class)
 @SuppressWarnings("unchecked")
 public class IaaUserServiceTest {
     @InjectMocks private IaaUserService iaaUserService;
     @Mock private JdbcOperations jdbcOperations;
     
 
     @Test
     public void testSearchByEmptyName() {
         final Set<BasicUser> result = iaaUserService.searchForUserByName("");
         assertEquals(0, result.size());
     }
     
     @Test
     public void testSearchByFirstName() {
         final Set<BasicUser> result = iaaUserService.searchForUserByName("John");
         assertEquals(0, result.size());
         
         verify(jdbcOperations).query(
                eq("select EPPN, FIRST_NAME, MIDDLE_NAME, LAST_NAME, EMAIL_ADDRESS from iaaviews.portal_blackboard_lookup where upper(first_name) like upper(?) OR upper(last_name) like upper(?) OR upper(FIRST_NAME || ' ' || MIDDLE_NAME || ' ' || LAST_NAME) like ?"), 
                 any(RowMapper.class), 
                 eq("John%"),
                 eq("John%"),
                 eq("John%"));
     }
     
     @Test
     public void testSearchByFirstLastName() {
         final Set<BasicUser> result = iaaUserService.searchForUserByName("John Doe");
         assertEquals(0, result.size());
         
         verify(jdbcOperations).query(
                eq("select EPPN, FIRST_NAME, MIDDLE_NAME, LAST_NAME, EMAIL_ADDRESS from iaaviews.portal_blackboard_lookup where (upper(first_name) like upper(?) AND upper(last_name) like upper(?)) OR upper(FIRST_NAME || ' ' || MIDDLE_NAME || ' ' || LAST_NAME) like ?"), 
                 any(RowMapper.class), 
                 eq("John%"),
                 eq("Doe%"),
                 eq("John%Doe%"));
     }
     
     @Test
     public void testSearchByFirstMiddleLastName() {
         final Set<BasicUser> result = iaaUserService.searchForUserByName("John C Doe");
         assertEquals(0, result.size());
         
         verify(jdbcOperations).query(
                eq("select EPPN, FIRST_NAME, MIDDLE_NAME, LAST_NAME, EMAIL_ADDRESS from iaaviews.portal_blackboard_lookup where (upper(first_name) like upper(?) AND upper(last_name) like upper(?)) OR upper(FIRST_NAME || ' ' || MIDDLE_NAME || ' ' || LAST_NAME) like ?"), 
                 any(RowMapper.class), 
                 eq("John%"),
                 eq("Doe%"),
                 eq("John%C%Doe%"));
     }
     
     @Test
     public void testSearchByFirstMiddleMultiLastName() {
         final Set<BasicUser> result = iaaUserService.searchForUserByName("John C Doe Dum");
         assertEquals(0, result.size());
         
         verify(jdbcOperations).query(
                eq("select EPPN, FIRST_NAME, MIDDLE_NAME, LAST_NAME, EMAIL_ADDRESS from iaaviews.portal_blackboard_lookup where (upper(first_name) like upper(?) AND upper(last_name) like upper(?)) OR upper(FIRST_NAME || ' ' || MIDDLE_NAME || ' ' || LAST_NAME) like ?"), 
                 any(RowMapper.class), 
                 eq("John%"),
                 eq("Dum%"),
                 eq("John%C%Doe%Dum%"));
     }
     
     @Test
     public void testSearchByEmail() {
         final Set<BasicUser> result = iaaUserService.searchForUserByEmail("john.doe@example");
         assertEquals(0, result.size());
         
         verify(jdbcOperations).query(
                 eq("select EPPN, FIRST_NAME, MIDDLE_NAME, LAST_NAME, EMAIL_ADDRESS from iaaviews.portal_blackboard_lookup where upper(EMAIL_ADDRESS) like upper(?)"), 
                 any(RowMapper.class), 
                 eq("john.doe@example%"));
     }
     
     @Test
     public void testFindUser() {
         final BasicUser user = iaaUserService.findUser("ID");
         assertNull(user);
         
         verify(jdbcOperations).query(
                 eq("select EPPN, FIRST_NAME, MIDDLE_NAME, LAST_NAME, EMAIL_ADDRESS from iaaviews.portal_blackboard_lookup where spvi = (pase.get_pvi_by_eppn(?))"), 
                 any(RowMapper.class), 
                 eq("ID"));
     }
 
     @Test
     public void testFindUserWithMapper() {
         when(jdbcOperations.query(
                 eq("select EPPN, FIRST_NAME, MIDDLE_NAME, LAST_NAME, EMAIL_ADDRESS from iaaviews.portal_blackboard_lookup where spvi = (pase.get_pvi_by_eppn(?))"), 
                 any(RowMapper.class), 
                 eq("ID"))).thenAnswer(new Answer<Object>() {
             @Override
             public Object answer(InvocationOnMock invocation) throws Throwable {
                 final Object[] args = invocation.getArguments();
                 final RowMapper<BasicUser> mapper = (RowMapper<BasicUser>)args[1];
                 
                 final ResultSet rs = mock(ResultSet.class);
 
                 when(rs.getString("EPPN")).thenReturn("jdoe");
                 
                 return ImmutableList.of(mapper.mapRow(rs, 0));
             }
         });
         
         final BasicUser user = iaaUserService.findUser("ID");
         
         final BasicUserImpl expected = new BasicUserImpl("jdoe", null, null);
         
         assertEquals(expected, user);
         assertEquals(expected.getEmail(), user.getEmail());
         assertEquals(expected.getDisplayName(), user.getDisplayName());
         assertEquals(expected.getAdditionalEmails(), user.getAdditionalEmails());
     }
 }
