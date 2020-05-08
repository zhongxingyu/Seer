 package az.his.android.hisapi;
 
 import android.content.Context;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import az.his.android.persist.Transaction;
 
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 public class ApiProvider {
     private static String url = null;
 
     private static final SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
 
     public static void setUrl(String url) {
         ApiProvider.url = url;
     }
 
     public static boolean isNetworkReady(Context context) {
         ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
         return networkInfo != null && networkInfo.isConnected();
     }
 
     @SuppressWarnings("unchecked")
     public static void checkServer(Context context, ApiListener listener) {
         if (isNetworkReady(context)) {
             CheckServerTask checkServerTask = new CheckServerTask();
             checkServerTask.execute(url, listener);
         } else {
             listener.handleApiResult(false);
         }
     }
 
     @SuppressWarnings("unchecked")
     public static void getUsers(Context context, ApiListener listener) {
         if (!isNetworkReady(context)) {
             throw new IllegalStateException("Network went down");
         }
         FetchUsersTask fetchUsersTask = new FetchUsersTask();
         fetchUsersTask.execute(url, listener);
     }
 
     @SuppressWarnings("unchecked")
     public static void getCategories(Context context, ApiListener listener, Integer uid) {
         if (!isNetworkReady(context)) {
             throw new IllegalStateException("Network went down");
         }
         (new FetchCatsTask()).execute(url, listener, uid);
     }
 
     @SuppressWarnings("unchecked")
     public static void postTransactions(Context context, ApiListener listener, Integer uid,
                                         List<Transaction> transactions, boolean async) {
         StringBuilder doc = new StringBuilder("<TransactionList uid=\"");
         doc.append(uid)
                 .append("\">");
 
         for (Transaction transaction : transactions) {
             doc.append("<tr amount=\"")
                    .append(-transaction.getAmount())
                     .append("\" cat=\"")
                     .append(transaction.getCatId())
                     .append("\" date=\"")
                     .append(xmlDateFormat.format(transaction.getStamp()))
                     .append("\"/>");
         }
         doc.append("</TransactionList>");
 
         if (!isNetworkReady(context)) {
             throw new IllegalStateException("Network went down");
         }
 
         if (async) {
             new PostTransactionsTask().execute(url, listener, doc.toString());
         } else {
             try {
                 int code = NetworkUtils.getResponseCodeOnly(url + "/api/trans", "POST", doc.toString());
                 listener.handleApiResult(code == 201);
             } catch (IOException e) {
                 listener.handleApiResult(Boolean.FALSE);
             }
         }
     }
 }
