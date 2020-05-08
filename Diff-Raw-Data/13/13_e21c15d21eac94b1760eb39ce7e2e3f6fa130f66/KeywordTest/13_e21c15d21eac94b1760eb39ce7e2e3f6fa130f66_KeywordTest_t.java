 package toctep.skynet.backend.test.dal;
 
 import toctep.skynet.backend.dal.domain.tweet.Keyword;
 
 public class KeywordTest extends DomainTest {
 	private Keyword keyword;
 	private String keywordValue;
 
 	@Override
 	public void setUp() {
 		super.setUp();
 		
 		keyword = new Keyword();
 		keywordValue = "cvketel";
 		
 		keyword.setKeyword(keywordValue);
 	}
 	@Override
 	public void testCreate() {
 		assertNotNull(keyword);
 		
 		assertEquals("getKeyword: ", keywordValue, keyword.getKeyword());
 	}
 
 	@Override
 	public void testInsert() {
 		keyword.save();
 		
 		Keyword postKeyword = (Keyword) keywordDao.select(keyword.getId());
 		assertEquals("getKeyword: ", keyword.getKeyword(), postKeyword.getKeyword());
 	}
 
 	@Override
 	public void testSelect() {
 		keyword.save();
 		
 		Keyword postKeyword = (Keyword) keywordDao.select(keyword.getId());
 		
 		assertTrue(postKeyword.getKeyword().equals(postKeyword.getKeyword()));
 	}
 
 	@Override
 	public void testUpdate() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void testDelete() {
		keyword.save();
		assertEquals(1, keywordDao.count());
		keyword.delete();
		assertEquals(0, keywordDao.count());	
 	}
 
 }
