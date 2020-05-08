 package toctep.skynet.backend.dal.domain.tweet;
 
 import toctep.skynet.backend.dal.dao.KeywordDao;
 import toctep.skynet.backend.dal.dao.impl.mysql.DaoFacadeImpl;
 import toctep.skynet.backend.dal.domain.Domain;
 
 public class Keyword extends Domain<Integer> implements IKeyword {
 	
 	private String keyword = "";
 	
 	public Keyword() {
 		super();
 	}
 	
 	public Keyword(String keyword) {
 		super();
 		this.keyword = keyword.toLowerCase();
 	}
 	
 	@Override
 	public String getKeyword() {
 		return keyword;
 	}
 	
 	public void setKeyword(String keyword) {
 		this.keyword = keyword;
 	}
 
 	@Override
 	public void setDao() {
 		dao = DaoFacadeImpl.getInstance().getKeywordDao();
 	}
 	
 	public static IKeyword select(Integer id) {
 		KeywordDao dao = DaoFacadeImpl.getInstance().getKeywordDao();
 		
 		if (dao.exists(id)) {
 			return (Keyword) dao.select(id);
 		}
 		
 		return NullKeyword.getInstance();
 	}
 	
 }
