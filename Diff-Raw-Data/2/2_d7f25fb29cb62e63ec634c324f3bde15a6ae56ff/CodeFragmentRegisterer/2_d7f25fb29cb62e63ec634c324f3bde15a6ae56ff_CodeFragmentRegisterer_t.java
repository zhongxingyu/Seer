 package jp.ac.osaka_u.ist.sdl.ectec.data.registerer;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import jp.ac.osaka_u.ist.sdl.ectec.data.CodeFragmentInfo;
 import jp.ac.osaka_u.ist.sdl.ectec.db.DBConnectionManager;
 
 /**
  * A class that represents a registerer for code fragments
  * 
  * @author k-hotta
  * 
  */
 public class CodeFragmentRegisterer extends
 		AbstractElementRegisterer<CodeFragmentInfo> {
 
 	/**
 	 * the constructor
 	 * 
 	 * @param dbManager
 	 * @param maxBatchCount
 	 */
 	public CodeFragmentRegisterer(DBConnectionManager dbManager,
 			int maxBatchCount) {
 		super(dbManager, maxBatchCount);
 	}
 
 	@Override
 	protected String createPreparedStatementQueue() {
		return "insert into CODE_FRAGMENT values (?,?,?,?,?,?,?,?)";
 	}
 
 	@Override
 	protected void setAttributes(PreparedStatement pstmt,
 			CodeFragmentInfo element) throws SQLException {
 		int column = 0;
 		pstmt.setLong(++column, element.getId());
 		pstmt.setLong(++column, element.getOwnerFileId());
 		pstmt.setLong(++column, element.getCrdId());
 		pstmt.setLong(++column, element.getStartRevisionId());
 		pstmt.setLong(++column, element.getEndRevisionId());
 		pstmt.setLong(++column, element.getHash());
 		pstmt.setInt(++column, element.getStartLine());
 		pstmt.setInt(++column, element.getEndLine());
 	}
 
 }
