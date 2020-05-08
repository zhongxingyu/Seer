 package ru.aim.anotheryetbashclient.helper.actions;
 
 import android.content.ContentValues;
 import android.content.Intent;
 import android.support.v4.content.LocalBroadcastManager;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpUriRequest;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 import ru.aim.anotheryetbashclient.ActionsAndIntents;
 import ru.aim.anotheryetbashclient.helper.BaseAction;
 import ru.aim.anotheryetbashclient.helper.DbHelper;
 import ru.aim.anotheryetbashclient.helper.f.Block;
 
 import java.util.ArrayList;
 
 import static ru.aim.anotheryetbashclient.helper.DbHelper.QUOTE_PUBLIC_ID;
 import static ru.aim.anotheryetbashclient.helper.Utils.rethrowWithRuntime;
 import static ru.aim.anotheryetbashclient.helper.actions.Package.getCharsetFromResponse;
 
 /**
  *
  */
 @SuppressWarnings("unused")
 abstract class AbstractAction extends BaseAction {
 
     static final String TAG = "AbstractAction";
     ArrayList<String> ids = new ArrayList<>();
 
     @Override
     public final void apply() {
         rethrowWithRuntime(new Block() {
             @Override
             public void apply() throws Exception {
                 Intent intent = new Intent(ActionsAndIntents.REFRESH);
                 HttpUriRequest httpRequest = getHttpRequest();
                 HttpResponse httpResponse = getHttpClient().execute(httpRequest);
                 String encoding = getCharsetFromResponse(httpResponse);
                 Document document = Jsoup.parse(httpResponse.getEntity().getContent(), encoding, getUrl());
                 beforeParsing(document);
                 Elements quotesElements = document.select("div[class=quote]");
                 for (Element e : quotesElements) {
                     onEachElement(e);
                 }
                 afterParsing(intent);
                sendIntent(intent);
             }
         });
     }
 
    protected void sendIntent(Intent intent) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.sendBroadcast(intent);
    }

     protected void onEachElement(Element e) {
         Elements idElements = e.select("a[class=id]");
         Elements dateElements = e.select("span[class=date]");
         Elements textElements = e.select("div[class=text]");
         if (!textElements.isEmpty()) {
             if (getDbHelper().notExists(idElements.html())) {
                 ContentValues values = new ContentValues();
                 values.put(QUOTE_PUBLIC_ID, idElements.html());
                 values.put(DbHelper.QUOTE_DATE, dateElements.html());
                 values.put(DbHelper.QUOTE_IS_NEW, 1);
                 values.put(DbHelper.QUOTE_TEXT, textElements.html().trim());
                 ids.add(idElements.html());
                 saveQuote(values);
             }
         }
     }
 
     protected void saveQuote(ContentValues values) {
         getDbHelper().addNewQuote(values);
     }
 
     protected abstract String getUrl();
 
     protected void beforeParsing(Document document) {
     }
 
     protected void afterParsing(Intent intent) {
         intent.putExtra(ActionsAndIntents.IDS, ids);
     }
 
     protected HttpUriRequest getHttpRequest() {
         return new HttpGet(getUrl());
     }
 }
