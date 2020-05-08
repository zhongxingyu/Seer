 package com.orangeleap.tangerine.test.dataprovider;
 
import java.util.ArrayList;
import java.util.List;

 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 
import com.orangeleap.tangerine.security.TangerineAuthenticationToken;
 import com.orangeleap.tangerine.util.TangerineUserHelper;
 
 public class MockTangerineUserHelperProvider {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
     
     public static TangerineUserHelper createMockUserHelper() {
         final Mockery tangerineUserHelperMock = new Mockery();
         final TangerineUserHelper tangerineUserHelper = tangerineUserHelperMock.mock(TangerineUserHelper.class);
        final List<String> roles = new ArrayList<String>();
        roles.add("ROLE_USER");
        
         tangerineUserHelperMock.checking(new Expectations() {{
             allowing (tangerineUserHelper).lookupUserId(); will(returnValue(new Long(1)));
             allowing (tangerineUserHelper).lookupUserName(); will(returnValue("nryan"));
             allowing (tangerineUserHelper).lookupUserSiteName(); will(returnValue("company1"));
             allowing (tangerineUserHelper).lookupUserPassword(); will(returnValue("password"));
            allowing (tangerineUserHelper).lookupUserRoles(); will(returnValue(roles));
            allowing (tangerineUserHelper).getToken(); will(returnValue(new TangerineAuthenticationToken("", "", "company1")));
         }});
         return tangerineUserHelper;
     }
 }
