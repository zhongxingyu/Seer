 package com.ibm.opensocial.landos;
 
 import static org.easymock.EasyMock.anyObject;
 import static org.easymock.EasyMock.eq;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.expectLastCall;
 import static org.junit.Assert.assertEquals;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 
 import org.easymock.EasyMock;
 import org.easymock.IMocksControl;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.collect.Maps;
 
 public class RunServletTest {
   private RunServlet servlet;
   private Connection connection;
   private DataSource source;
   private Map<String, Object> attributes;
   private StringWriter output;
   private IMocksControl control;
   private HttpServletRequest req;
   private HttpServletResponse res;
   private final long startTime = 1000000000;
   private final long endTime = startTime + 1;
   private final int expectedId = 9001;
 
   @Before
   public void before() throws Exception {
     servlet = new RunServlet();
     output = new StringWriter();
     control = EasyMock.createControl();
     connection = TestControlUtils.mockConnection(control);
    connection.close(); expectLastCall().once();
     source = TestControlUtils.mockDataSource(control, connection);
     attributes = Maps.newHashMap();
     res = TestControlUtils.mockResponse(control, output);
   }
 
   @Test
   public void testGetRun() throws SQLException, IOException {
     String pathInfo = "/" + expectedId;
     req = TestControlUtils.mockRequest(control, attributes, source, pathInfo);
     PreparedStatement stmt = control.createMock(PreparedStatement.class);
     ResultSet result = control.createMock(ResultSet.class);
 
     expect(connection.prepareStatement(anyObject(String.class))).andReturn(stmt).once();
     stmt.setInt(1, expectedId);
     expectLastCall().once();
     expect(stmt.executeQuery()).andReturn(result);
     expect(result.first()).andReturn(true).once();
     expect(result.getTimestamp(2)).andReturn(new Timestamp(startTime)).once();
     expect(result.getTimestamp(3)).andReturn(new Timestamp(endTime)).once();
     expect(result.getBoolean(4)).andReturn(false).once();
 
     // Run test
     control.replay();
     servlet.doGet(req, res);
     control.verify();
     verifyRunServletOutput();
   }
 
   @Test
   public void testPutNewRun() throws SQLException, IOException {
     // Mocks
     String pathInfo = "/" + startTime + "/" + endTime;
     req = TestControlUtils.mockRequest(control, attributes, source, pathInfo);
     Timestamp start = new Timestamp(startTime);
     Timestamp end = new Timestamp(endTime);
     PreparedStatement stmt1 = control.createMock(PreparedStatement.class);
     PreparedStatement stmt2 = control.createMock(PreparedStatement.class);
     ResultSet result = control.createMock(ResultSet.class);
     ResultSet ids = control.createMock(ResultSet.class);
 
     // Set up expectations
     expect(connection.prepareStatement(anyObject(String.class))).andReturn(stmt1).once();
 
     expect(
             connection.prepareStatement(anyObject(String.class),
                     eq(Statement.RETURN_GENERATED_KEYS))).andReturn(stmt2).once();
     stmt1.setTimestamp(1, start);
     expectLastCall().once();
     stmt1.setTimestamp(2, end);
     expectLastCall().once();
     stmt2.setTimestamp(1, start);
     expectLastCall().once();
     stmt2.setTimestamp(2, end);
     expectLastCall().once();
     stmt2.setBoolean(3, false);
     expectLastCall().once();
     expect(stmt1.executeQuery()).andReturn(result).once();
     expect(result.first()).andReturn(false).once();
     expect(stmt2.executeUpdate()).andReturn(1).once();
     expect(stmt2.getGeneratedKeys()).andReturn(ids).once();
     expect(ids.first()).andReturn(true).once();
     expect(ids.getInt(1)).andReturn(expectedId).once();
 
     // Run test
     control.replay();
     servlet.doPut(req, res);
     control.verify();
     verifyRunServletOutput();
   }
   
   @Test
   public void testDeleteRun() throws SQLException, IOException {
     String pathInfo = "/" + expectedId;
     req = TestControlUtils.mockRequest(control, attributes, source, pathInfo);
     PreparedStatement stmt = control.createMock(PreparedStatement.class);
     
     expect(connection.prepareStatement(anyObject(String.class))).andReturn(stmt).once();
     stmt.setInt(1, expectedId);
     expectLastCall().once();
     expect(stmt.executeUpdate()).andReturn(1).once();
     
     control.replay();
     servlet.doDelete(req, res);
     control.verify();
     
     assertEquals("Verify servlet output", "{\"id\":" + expectedId + "}", output.toString());
   }
 
   /**
    * Checks that the servlet output a complete run object.
    */
   private void verifyRunServletOutput() {
     assertEquals("Verify servlet output", "{\"id\":" + expectedId + ",\"start\":" + startTime
             + ",\"end\":" + endTime + ",\"test\":false}", output.toString());
   }
 }
