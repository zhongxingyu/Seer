 package networking.packages;
 
 import java.util.List;
 
 public class DataResponse extends Response {
 	private static final long serialVersionUID = -1664992392938826860L;
 	private List data;
 	private DataResponseType dataResponseType;
 	public final boolean requested;
 
 	public enum DataResponseType {
 		NOTIFICATION_RESPONSE, PERSON_RESPONSE, MEETING_RESPONSE, ALARM_RESPONSE, TEAM_RESPONSE
 	}
 
 	public DataResponse(List data, DataResponseType dataResponseType, boolean requested) {
 		super(ResponseType.DATA_RESPONSE);
 		this.requested = requested;
 		this.data = data;
 		this.dataResponseType = dataResponseType;
 	}
 
 	public List getData() {
 		return data;
 	}
 
 	public DataResponseType getQueryResponseType() {
 		return dataResponseType;
 	}
 
	@Override
	public String toString() {
		return "DataResponse [data=" + data + ", dataResponseType="
				+ dataResponseType + ", requested=" + requested + "]";
	}
	
	

 }
