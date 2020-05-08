 package ee.ut.cs.veebirakendus2013.kurivaim.jettytest.query;
 
 import java.util.List;
 
 import com.google.gson.annotations.SerializedName;
 
 import ee.ut.cs.veebirakendus2013.kurivaim.jettytest.mysql.MysqlQueryCandidateInfo;
 
 public class JsonResponseTypeCandidates implements JsonResponseInterface {
 	@SerializedName("responseType")
 	private final String responseType = "candidates";
 	
 	@SerializedName("candidateList")
 	private List<MysqlQueryCandidateInfo> candidateList;
 	
	public JsonResponseTypeCandidates(List<MysqlQueryCandidateInfo> candidateList) {
 		this.candidateList = candidateList;
 	}
 }
