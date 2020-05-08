 package com.orangeleap.tangerine.test.dataprovider;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 
 import com.orangeleap.tangerine.util.TangerineUserHelper;
 
 public class MockTangerineUserHelperProvider {
 
     /** Logger for this class and subclasses */
     protected final Log logger = LogFactory.getLog(getClass());
     
     public static TangerineUserHelper createMockUserHelper() {
         final Mockery tangerineUserHelperMock = new Mockery();
         final TangerineUserHelper tangerineUserHelper = tangerineUserHelperMock.mock(TangerineUserHelper.class);

         tangerineUserHelperMock.checking(new Expectations() {{
             allowing (tangerineUserHelper).lookupUserId(); will(returnValue(new Long(1)));
             allowing (tangerineUserHelper).lookupUserName(); will(returnValue("nryan"));
             allowing (tangerineUserHelper).lookupUserSiteName(); will(returnValue("company1"));
             allowing (tangerineUserHelper).lookupUserPassword(); will(returnValue("password"));
         }});
         return tangerineUserHelper;
     }
 }
