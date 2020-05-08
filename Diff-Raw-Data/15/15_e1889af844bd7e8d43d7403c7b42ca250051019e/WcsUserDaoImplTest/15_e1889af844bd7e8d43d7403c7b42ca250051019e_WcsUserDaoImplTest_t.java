 package org.deegree.securityproxy.authentication.repository;
 
 import static org.deegree.securityproxy.commons.WcsOperationType.GETCAPABILITIES;
 import static org.deegree.securityproxy.commons.WcsOperationType.GETCOVERAGE;
 import static org.deegree.securityproxy.commons.WcsServiceVersion.VERSION_100;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.junit.Assert.assertThat;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.deegree.securityproxy.authentication.WcsGeometryFilterInfo;
 import org.deegree.securityproxy.authentication.WcsUser;
 import org.deegree.securityproxy.authentication.wcs.WcsPermission;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
 import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 /**
  * @author <a href="goltz@lat-lon.de">Lyn Goltz</a>
  * @author <a href="erben@lat-lon.de">Alexander Erben</a>
  * @author last edited by: $Author: erben $
  * 
  * @version $Revision: $, $Date: $
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath*:org/deegree/securityproxy/authentication/repository/UserDaoTestContext.xml" })
 public class WcsUserDaoImplTest {
 
     private EmbeddedDatabase db;
 
     @Autowired
     private WcsUserDao source;
 
     @Before
     public void setUp() {
         db = new EmbeddedDatabaseBuilder().build();
     }
 
     @Test
     public void testRetrieveWcsUserByIdValidHeaderShouldReturnUserDetails() {
         UserDetails details = source.retrieveWcsUserById( "VALID_HEADER" );
         assertThat( details.getUsername(), is( "USER" ) );
         assertThat( details.getPassword(), is( "PASSWORD" ) );
     }
 
     @Test
     public void testRetrieveWcsUserByIdValidHeaderShouldReturnUserDetailWithGetCapabilitiesPermission() {
         UserDetails details = source.retrieveWcsUserById( "VALID_HEADER_GETCAPABILITIES" );
         Collection<? extends GrantedAuthority> authorities = details.getAuthorities();
         assertThat( authorities.size(), is( 1 ) );
         WcsPermission firstAuthority = (WcsPermission) authorities.iterator().next();
         assertThat( firstAuthority.getOperationType(), is( GETCAPABILITIES ) );
         assertThat( firstAuthority.getServiceVersion(), is( VERSION_100 ) );
         assertThat( firstAuthority.getServiceName(), is( "serviceName" ) );
         assertThat( firstAuthority.getCoverageName(), is( nullValue() ) );
     }
 
     @Test
     public void testRetrieveWcsUserByIdValidHeaderShouldReturnUserDetailWithGetCoveragePermission() {
         UserDetails details = source.retrieveWcsUserById( "VALID_HEADER_GETCOVERAGE" );
         Collection<? extends GrantedAuthority> authorities = details.getAuthorities();
         assertThat( authorities.size(), is( 1 ) );
         WcsPermission firstAuthority = (WcsPermission) authorities.iterator().next();
         assertThat( firstAuthority.getOperationType(), is( GETCOVERAGE ) );
         assertThat( firstAuthority.getServiceVersion(), is( VERSION_100 ) );
         assertThat( firstAuthority.getServiceName(), is( "serviceName" ) );
         assertThat( firstAuthority.getCoverageName(), is( "layerName" ) );
     }
 
     @Test
     public void testRetrieveWcsUserByIdValidHeaderShouldReturnUserDetailWithMultiplePermissions() {
         UserDetails details = source.retrieveWcsUserById( "VALID_HEADER_MULTIPLE" );
         Collection<? extends GrantedAuthority> authorities = details.getAuthorities();
         assertThat( authorities.size(), is( 2 ) );
     }
 
     @Test
     public void testRetrieveWcsUserByIdValidHeaderShouldReturnUserDetailWithOnePermissionsButMultipleVersions() {
         UserDetails details = source.retrieveWcsUserById( "VALID_HEADER_MULTIPLE_VERSIONS" );
         Collection<? extends GrantedAuthority> authorities = details.getAuthorities();
         assertThat( authorities.size(), is( 3 ) );
     }
 
     @Test
     public void testRetrieveWcsUserByIdInvalidHeaderShouldReturnNull() {
         UserDetails details = source.retrieveWcsUserById( "INVALID_HEADER" );
         assertThat( details, nullValue() );
     }
 
     @Test
     public void testLoadUserDetailsForUserWithNotWcsPermissionFails() {
         UserDetails details = source.retrieveWcsUserById( "WMS_VALID_HEADER" );
         assertThat( details, nullValue() );
     }
 
     @Test
     public void testLoadUserDetailsForUserSubscriptionOk() {
         UserDetails details = source.retrieveWcsUserById( "VALID_HEADER_SUBSCRIPTION_OK" );
         assertThat( details, notNullValue() );
     }
 
     @Test
     public void testLoadUserDetailsForUserSubscriptionExpired() {
         UserDetails details = source.retrieveWcsUserById( "VALID_HEADER_SUBSCRIPTION_EXPIRED" );
         assertThat( details, nullValue() );
     }
 
     @Test
     public void testRetrieveWcsUserByIdShouldThrowExceptionOnEmptyHeader() {
         WcsUser wcsUser = source.retrieveWcsUserById( "" );
         assertThat( wcsUser, nullValue() );
     }
 
     @Test
     public void testRetrieveWcsUserByIdShouldThrowExceptionOnNullArgument() {
         WcsUser wcsUser = source.retrieveWcsUserById( null );
         assertThat( wcsUser, nullValue() );
     }
 
     @Test
     public void testRetrieveWcsUserByIdValidHeaderWithGeometryLimitShouldReturnEmptyCollection() {
         WcsUser wcsUser = source.retrieveWcsUserById( "VALID_HEADER_WITH_NULL_GEOMETRY_LIMIT" );
         List<WcsGeometryFilterInfo> emptyList = Collections.<WcsGeometryFilterInfo> emptyList();
         assertThat( wcsUser.getWcsGeometryFilterInfos(), is( emptyList ) );
     }
 
     @Test
     public void testRetrieveWcsUserByIdValidHeaderWithGeometryLimitShouldReturnCollectionWithOneRecord() {
         int expectedSize = 1;
        String expectedCoverageName = "layer1";
         String expectedGeometryLimit = "SRID=4326;MULTIPOLYGON(((-89.739 20.864,-89.758 20.876,-89.765 20.894,-89.748 20.897,-89.73 20.91,-89.708 20.928,-89.704 20.948,-89.716 20.964,-89.729 20.99,-89.73 21.017,-89.712 21.021,-89.685 21.031,-89.667 21.025,-89.641 21.017,-89.62 21.019,-89.599 21.018,-89.575 20.995,-89.568 20.97,-89.562 20.934,-89.562 20.91,-89.577 20.89,-89.609 20.878,-89.636 20.877,-89.664 20.881,-89.683 20.904,-89.683 20.917,-89.664 20.941,-89.662 20.954,-89.674 20.965,-89.687 20.983,-89.705 20.989,-89.703 20.974,-89.696 20.961,-89.686 20.949,-89.683 20.935,-89.694 20.919,-89.705 20.901,-89.722 20.875,-89.727 20.869,-89.739 20.864),(-89.627 20.985,-89.603 20.962,-89.62 20.936,-89.634 20.943,-89.639 20.961,-89.649 20.975,-89.627 20.985)))";
 
         WcsUser wcsUser = source.retrieveWcsUserById( "VALID_HEADER_WITH_GEOMETRY_LIMIT_ONE_RECORD" );
         List<WcsGeometryFilterInfo> responseFilters = wcsUser.getWcsGeometryFilterInfos();
         WcsGeometryFilterInfo responseFilter = responseFilters.get( 0 );
         String coverageName = responseFilter.getCoverageName();
         String geometryLimit = responseFilter.getGeometryString();
 
         assertThat( responseFilters.size(), is( expectedSize ) );
         assertThat( coverageName, is( expectedCoverageName ) );
         assertThat( geometryLimit, is( expectedGeometryLimit ) );
     }
 
     @Test
     public void testRetrieveWcsUserByIdValidHeaderWithGeometryLimitShouldReturnCollectionWithTwoRecords() {
         int expectedSize = 2;
        String expectedFirstCoverageName = "layer1";
         String expectedFirstGeometryLimit = "SRID=4326;MULTIPOLYGON(((-89.739 20.864,-89.758 20.876,-89.765 20.894,-89.748 20.897,-89.73 20.91,-89.708 20.928,-89.704 20.948,-89.716 20.964,-89.729 20.99,-89.73 21.017,-89.712 21.021,-89.685 21.031,-89.667 21.025,-89.641 21.017,-89.62 21.019,-89.599 21.018,-89.575 20.995,-89.568 20.97,-89.562 20.934,-89.562 20.91,-89.577 20.89,-89.609 20.878,-89.636 20.877,-89.664 20.881,-89.683 20.904,-89.683 20.917,-89.664 20.941,-89.662 20.954,-89.674 20.965,-89.687 20.983,-89.705 20.989,-89.703 20.974,-89.696 20.961,-89.686 20.949,-89.683 20.935,-89.694 20.919,-89.705 20.901,-89.722 20.875,-89.727 20.869,-89.739 20.864),(-89.627 20.985,-89.603 20.962,-89.62 20.936,-89.634 20.943,-89.639 20.961,-89.649 20.975,-89.627 20.985)))";
        String expectedSecondCoverageName = "layer2";
         String expectedSecondGeometryLimit = "POLYGON";
 
         WcsUser wcsUser = source.retrieveWcsUserById( "VALID_HEADER_WITH_GEOMETRY_LIMIT_TWO_RECORDS" );
         List<WcsGeometryFilterInfo> responseFilters = wcsUser.getWcsGeometryFilterInfos();
         WcsGeometryFilterInfo firstResponseFilter = responseFilters.get( 0 );
         WcsGeometryFilterInfo secondResponseFilter = responseFilters.get( 1 );
         String firstCoverageName = firstResponseFilter.getCoverageName();
         String firstGeometryLimit = firstResponseFilter.getGeometryString();
         String secondCoverageName = secondResponseFilter.getCoverageName();
         String secondGeometryLimit = secondResponseFilter.getGeometryString();
 
         assertThat( responseFilters.size(), is( expectedSize ) );
         assertThat( firstCoverageName, is( expectedFirstCoverageName ) );
         assertThat( firstGeometryLimit, is( expectedFirstGeometryLimit ) );
         assertThat( secondCoverageName, is( expectedSecondCoverageName ) );
         assertThat( secondGeometryLimit, is( expectedSecondGeometryLimit ) );
     }
 
     @After
     public void tearDown() {
         db.shutdown();
     }
 }
