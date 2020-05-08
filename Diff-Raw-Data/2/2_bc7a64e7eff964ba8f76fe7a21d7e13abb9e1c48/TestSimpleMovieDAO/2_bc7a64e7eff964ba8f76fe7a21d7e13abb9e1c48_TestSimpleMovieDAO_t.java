 package edu.sjsu.videolibrary.db;
 
 import static org.junit.Assert.*;
 import static org.mockito.Matchers.*;
 import static org.mockito.Mockito.*;
 
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import edu.sjsu.videolibrary.model.Movie;
 import edu.sjsu.videolibrary.test.BaseTestCase;
 
 public class TestSimpleMovieDAO extends BaseTestCase{
 	String result = null;
 
 	@Before
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	@After
 	protected void tearDown() throws Exception {
 		super.tearDown();		
 	}
 	
 	@Test
 	public void testCreateNewMovieWrongInput() throws Exception {
 		SimpleMovieDAO dao = new SimpleMovieDAO();
 		setupConnection(dao);
 		
 		stub(stmt.executeUpdate(anyString())).toReturn(0);
 		stub( stmt.getGeneratedKeys()).toReturn(rs);
 		if(rs.next()){
 			 
 			result = anyString();}
 		
 		try{
 			String result = dao.createNewMovie("aa", "bb", "2012", 1, 1);
 			assertEquals("", result);
 		}catch(Exception e){
 			fail(e.getMessage());
 		}
 	}
 	
 @Test
 public void testCreateNewMovieNullValueInput() throws Exception {
 	SimpleMovieDAO dao = new SimpleMovieDAO();
 	setupConnection(dao);
 	stub(stmt.executeUpdate(anyString())).toReturn(0);
 	stub( stmt.getGeneratedKeys()).toReturn(rs);
 	if(rs.next()){
 		 
 		result = anyString();}
 	
 	try{
 		String result = dao.createNewMovie(null, null, "2012",5, 1);
 		assertEquals("", result);
 	}catch(Exception e){
 		fail(e.getMessage());
 	}
 }
 
 public void testCreateNewMovieCorrectInput() throws Exception {
 	SimpleMovieDAO dao = new SimpleMovieDAO();
 	setupConnection(dao);
 	
 	stub(stmt.executeUpdate(anyString())).toReturn(1);
 	stub( stmt.getGeneratedKeys()).toReturn(rs);
 	if(rs.next()){
 		 
 		result = anyString();}
 	
 	try{
 		String result = dao.createNewMovie("aa", "bb", "2012",5,1);
		assertNotNull(result);
 	}catch(Exception e){
 		fail(e.getMessage());
 	}
 }
 
 	@Test
 	public void testCreateNewMovieSQLException() throws Exception {
 		SimpleMovieDAO dao = new SimpleMovieDAO();
 		setupConnection(dao);
 		
 		stub(stmt.executeUpdate(anyString())).toThrow(new SQLException());
 		try{
 		String result =dao.createNewMovie("aa", "bb", "2012",5,1);
 		assertEquals(null,result);
 		}catch(Exception e){fail(e.getMessage());};
 	}
 	
 	@Test
 	public final void testDeleteMovieWrongInput() throws Exception {
 		SimpleMovieDAO dao = new SimpleMovieDAO();
 		setupConnection(dao);
 		
 		stub(stmt.executeUpdate(anyString())).toReturn(0);
 		try{
 			result = dao.deleteMovie("23");
 			assertEquals("",result);
 		}catch(Exception e){
 			fail(e.getMessage());
 		}
 	}
 	
 //	@Test
 //	public final void testDeleteMovieNullValueInput() throws Exception {
 //		SimpleMovieDAO dao = new SimpleMovieDAO();
 //		setupConnection(dao);
 //		
 //		stub(stmt.executeUpdate(anyString())).toReturn(0);
 //		try{
 //			result = dao.deleteMovie(null);
 //			assertEquals("",result);
 //		}catch(Exception e){
 //			fail(e.getMessage());
 //		}
 //	}
 
 	
 	@Test
 	public final void testDeleteMovieCorrectInput() throws Exception {
 		SimpleMovieDAO dao = new SimpleMovieDAO();
 		setupConnection(dao);
 		
 		stub(stmt.executeUpdate(anyString())).toReturn(1);
 		try{
 			result = dao.deleteMovie("123456789");
 			assertEquals("true",result);
 		}catch(Exception e){
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public final void testDeleteMovieSQLException() throws Exception {
 		SimpleMovieDAO dao = new SimpleMovieDAO();
 		setupConnection(dao);
 		
 		stub(stmt.executeUpdate(anyString())).toThrow(new SQLException());
 		try{
 			result = dao.deleteMovie("23");
 			assertEquals(null,result);
 		}catch(Exception e){
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public final void testReturnMovieWrongInput() throws Exception{
 		SimpleMovieDAO dao = new SimpleMovieDAO();
 		setupConnection(dao);
 		
 		stub(stmt.executeUpdate(anyString())).toReturn(0);
 		
 		try{
 			result = dao.returnMovie(1, "23");
 			assertEquals("false",result);
 		}catch(Exception e){
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public final void testReturnMovieNullValueInput() throws Exception{
 		SimpleMovieDAO dao = new SimpleMovieDAO();
 		setupConnection(dao);
 		
 		stub(stmt.executeUpdate(anyString())).toReturn(0);
 		
 		try{
 			result = dao.returnMovie(1, null);
 			assertEquals("false",result);
 		}catch(Exception e){
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public final void testReturnMovieCorrectInput() throws Exception{
 		SimpleMovieDAO dao = new SimpleMovieDAO();
 		setupConnection(dao);
 		
 		stub(stmt.executeUpdate(anyString())).toReturn(1);
 		
 		try{
 			result = dao.returnMovie(1, "254364678658");
 			assertEquals("true",result);
 		}catch(Exception e){
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public final void testReturnMovieSQLException() throws Exception{
 		SimpleMovieDAO dao = new SimpleMovieDAO();
 		setupConnection(dao);
 		
 		stub(stmt.executeUpdate(anyString())).toThrow(new SQLException());
 		
 		try{
 			dao.returnMovie(1, "254364678658");
 			assertEquals(null,result);
 		}catch(Exception e){
 			fail(e.getMessage());
 		}
 	}	
 
 	@Test
 	public final void testListCategoriesSQLException()  throws Exception {
 		SimpleMovieDAO dao = new SimpleMovieDAO();
 		setupConnection(dao);
 		
 		stub(stmt.executeQuery(anyString())).toThrow(new SQLException());
 		try{
 			String[] s =dao.listCategories();
 			assertEquals(null,s);
 		}catch(Exception e){};
 	}
 
 //	@Test
 //	public final void testListMoviesByCategoryCorrectInput() throws Exception {
 //		SimpleMovieDAO dao = new SimpleMovieDAO();
 //		setupConnection(dao);
 //		
 //		stub(stmt.executeQuery(anyString())).toReturn(rs);
 //		stub(rs.next()).toReturn(true).toReturn(true).toReturn(false);
 //		stub(rs.getInt(1)).toReturn(112);
 //		stub(rs.getString(2)).toReturn("aa");
 //		stub(rs.getString(3)).toReturn("bb");
 //		stub(rs.getString(4)).toReturn("12-23-2011");
 //		stub(rs.getInt(5)).toReturn(anyInt());
 //		
 //		try{
 //			Movie[] m = dao.listMoviesByCategory("Fantasy",10,10);
 //			assertEquals(anyInt(),m.length);
 //			assertEquals(anyInt(),m[0].getMovieId());
 //			assertEquals(anyString(),m[0].getMovieName());
 //			assertEquals(anyString(),m[0].getMovieBanner());
 //			assertEquals(anyString(),m[0].getReleaseDate());
 //			assertEquals(anyInt(),m[0].getAvailableCopies());
 //		}catch(Exception e){
 //			fail(e.getMessage());
 //		}
 //	}
 
 //	@Test
 //	public final void testListMoviesByCategorySQLException() throws Exception {
 //		SimpleMovieDAO dao = new SimpleMovieDAO();
 //		setupConnection(dao);
 //		
 //		stub(stmt.executeQuery(anyString())).toThrow(new SQLException());
 //		
 //		try{
 //			Movie[] m = dao.listMoviesByCategory("Drama",10,10);
 //			assertEquals(anyInt(),m.length);
 //		}catch(Exception e){
 //			fail(e.getMessage());
 //		}
 //	}
 	
 	@Test
 	public final void testListAllMoviesSQLExceptions() throws Exception {
 		SimpleMovieDAO dao = new SimpleMovieDAO();
 		setupConnection(dao);
 		
 		stub(stmt.executeQuery(anyString())).toThrow(new SQLException());
 		
 		try{
 			Movie[] m = dao.listAllMovies();
 			assertEquals(null,m);
 		}catch(Exception e){};
 	}
 
 	@Test
 	public final void testUpdateCopiesCountWrongInput() throws Exception{
 		SimpleMovieDAO dao = new SimpleMovieDAO();
 		setupConnection(dao);
 		
 		stub(stmt.executeUpdate(anyString())).toReturn(0);
 		
 		try{
 			String sstr = dao.updateCopiesCount(1, 10);
 			assertEquals("false",sstr);
 		}catch(Exception e){
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public final void testUpdateCopiesCountNullValueInput() throws Exception{
 		SimpleMovieDAO dao = new SimpleMovieDAO();
 		setupConnection(dao);
 		
 		stub(stmt.executeUpdate(anyString())).toReturn(0);
 		
 		try{
 			String sstr = dao.updateCopiesCount(1,(Integer) null);
 			assertEquals("false",sstr);
 		}catch(Exception e){
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public final void testUpdateCopiesCountCorrectInput() throws Exception{
 		SimpleMovieDAO dao = new SimpleMovieDAO();
 		setupConnection(dao);
 		
 		stub(stmt.executeUpdate(anyString())).toReturn(1);
 		
 		try{
 			String sstr = dao.updateCopiesCount(1, 10);
 			assertEquals("true",sstr);
 		}catch(Exception e){
 			fail(e.getMessage());
 		}
 	}
 	
 	@Test
 	public final void testUpdateCopiesCountSQLException() throws Exception{
 		SimpleMovieDAO dao = new SimpleMovieDAO();
 		setupConnection(dao);
 		
 		stub(stmt.executeUpdate(anyString())).toThrow(new SQLException());
 		
 		try{
 			String sstr = dao.updateCopiesCount(1, 10);
 			assertEquals(null,sstr);
 		}catch(Exception e){
 			fail(e.getMessage());
 		}
 	}
 
 	@Test
 	public final void testGetAvailableCopiesCorrectInput() throws Exception{
 		SimpleMovieDAO dao = new SimpleMovieDAO();
 		setupConnection(dao);
 		
 		stub(stmt.executeQuery(anyString())).toReturn(rs);
 		stub(rs.next()).toReturn(true).toReturn(true).toReturn(false);
 		stub(rs.getInt(anyString())).toReturn(0);
 		
 		try{
 			int i = dao.getAvailableCopies(1);
 			assertEquals(anyInt(),i);
 		}catch(Exception e){
 			fail(e.getMessage());
 		}
 		
 	}
 	
 	@Test
 	public final void testGetAvailableCopiesSQLExceptions() throws Exception {
 		SimpleMovieDAO dao = new SimpleMovieDAO();
 		setupConnection(dao);
 		
 		stub(stmt.executeQuery(anyString())).toThrow(new SQLException());
 		
 		try{
 			int i = dao.getAvailableCopies(1);
 			assertEquals(0,i);
 		}catch(Exception e){};
 	}
 
 //	@Test
 //	public final void testSearchMovie() {
 //		fail("Not yet implemented"); // TODO
 //	}
 //
 }
