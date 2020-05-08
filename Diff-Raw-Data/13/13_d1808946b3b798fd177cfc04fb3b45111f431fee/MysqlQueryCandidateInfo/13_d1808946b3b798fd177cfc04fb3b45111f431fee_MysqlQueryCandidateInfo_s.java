 package ee.ut.cs.veebirakendus2013.kurivaim.jettytest.mysql;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class MysqlQueryCandidateInfo {
 	private transient boolean isLoaded = false;
 	
 	private int candidateId;
 	private int userId;
 	private int regionId;
 	private int partyId;
 	private int voteCount;
 	private String realName;
 	
 	private final MysqlConnectionHandler sqlHandler;
 	
 	public MysqlQueryCandidateInfo(MysqlConnectionHandler sqlHandler) {
 		this.sqlHandler = sqlHandler;
 	}
 	
 	public MysqlQueryCandidateInfo querySingleByUserId(int queryUserId) {
 		try {
 			PreparedStatement statement = sqlHandler.getConnection().prepareStatement("SELECT * FROM ev_candidates WHERE userId = ?");
 			statement.setInt(1, queryUserId);
 			
 			return fillSingleDataFromResults(statement.executeQuery());
 		} catch (SQLException e) {
 			//TODO: log this error somewhere
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	public List<MysqlQueryCandidateInfo> queryAllByFilter(int queryRegionId, int queryPartyId, String namePrefix, int orderingMethod) {
 		try {
 			String orderingString = (orderingMethod == 0) ? " ORDER BY voteCount DESC" : " ORDER BY realName ASC";
 			String regionFilter = (queryRegionId > 0) ? " AND regionId = " + queryRegionId : "";
			String partyFilter = (queryPartyId > 0) ? " AND partyId = ?" + queryPartyId : "";
 			String nameFilter = (namePrefix != null && namePrefix.length() > 0) ? " AND realName LIKE ?" : "";
 			
 			PreparedStatement statement = sqlHandler.getConnection().prepareStatement("SELECT * FROM ev_candidates WHERE 1" + regionFilter + partyFilter + nameFilter + orderingString);
 			if(namePrefix != null && namePrefix.length() > 0) statement.setString(1, namePrefix + "%");
 			
 			return fillMultiDataFromResults(statement.executeQuery());
 		} catch (SQLException e) {
 			//TODO: log this error somewhere
 			e.printStackTrace();
 		}
 		
 		return null;
 	}
 	
 	private MysqlQueryCandidateInfo fillSingleDataFromResults(ResultSet results) throws SQLException {
 		if(results.next()) {
 			return fillDataFromNextResult(results);
 		}
 		
 		return null;
 	}
 	
 	private List<MysqlQueryCandidateInfo> fillMultiDataFromResults(ResultSet results) throws SQLException {
 		ArrayList<MysqlQueryCandidateInfo> lines = new ArrayList<MysqlQueryCandidateInfo>();
 		
 		while(results.next()) {
 			lines.add(new MysqlQueryCandidateInfo(null).fillDataFromNextResult(results));
 		}
 		
 		return lines;
 	}
 	
 	private MysqlQueryCandidateInfo fillDataFromNextResult(ResultSet results) throws SQLException {
 		isLoaded = true;
 		
 		candidateId = results.getInt("id");
 		userId = results.getInt("userId");
 		regionId = results.getInt("regionId");
 		partyId = results.getInt("partyId");
 		voteCount = results.getInt("voteCount");
 		realName = results.getString("realName");
 		
 		return this;
 	}
 	
 	public boolean isFilled() {
 		return isLoaded;
 	}
 	
 	public int getCandidateId() {
 		return candidateId;
 	}
 	
 	public int getUserId() {
 		return userId;
 	}
 	
 	public int getRegionId() {
 		return regionId;
 	}
 	
 	public int getPartyId() {
 		return partyId;
 	}
 	
 	public int getVoteCount() {
 		return voteCount;
 	}
 	
 	public String getRealName() {
 		return realName;
 	}
 }
