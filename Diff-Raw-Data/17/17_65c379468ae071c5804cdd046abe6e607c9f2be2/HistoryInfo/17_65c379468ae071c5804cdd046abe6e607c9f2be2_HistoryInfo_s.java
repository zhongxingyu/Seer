 package nl.napauleon.sabber.history;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONTokener;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 public class HistoryInfo {
 	private String item;
 	private Date dateDownloaded;
     private String message;
     private Status status;
 
     public HistoryInfo(String item, Long dateDownloaded, Status status, String message) {
 		this.item = item;
 		this.dateDownloaded = new Date(dateDownloaded);
         this.message = message;
         this.status = status;
 	}
 
     public static ArrayList<HistoryInfo> createHistoryList(String jsonResponse) throws JSONException {
         JSONArray slots = ((JSONObject) new JSONTokener(jsonResponse).nextValue())
                 .getJSONObject("history").getJSONArray("slots");
         ArrayList<HistoryInfo> historyItems = new ArrayList<HistoryInfo>(slots.length());
         for (int i = 0; i < slots.length(); i++) {
             historyItems.add(createHistoryInfo(slots.getJSONObject(i)));
         }
         return historyItems;
     }
 
     private static HistoryInfo createHistoryInfo(JSONObject slot) throws JSONException {
         Status status;
         try {
             status = Status.valueOf(slot.getString("status"));
         } catch (IllegalArgumentException e) {
             status = Status.Unknown;
         }
 
         return new HistoryInfo(
                 slot.getString("nzb_name").replace(".nzb", ""),
                 slot.getLong("completed") * 1000L,
                 status,
                status == Status.Failed ? slot.getString("fail_message") : slot.getString("action_line"));
     }
     
     public boolean isProcessingComplete() {
 		return status == Status.Completed || status == Status.Failed;
 	}
 
     public Date getDateDownloaded() {
         return dateDownloaded;
     }
 
 	public String getDateDownloadedAsString() {
 		return new SimpleDateFormat("yyyy-MM-dd HH:mm").format(getDateDownloaded());
 	}
 
     public String getItem() {
 		return item;
 	}
 
     public String getMessage() {
         return message;
     }
 
 	public Status getStatus() {
 		return status;
 	}
     
     
 }
