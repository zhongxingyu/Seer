 package com.example.simpletalk;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.content.Context;
 import android.os.AsyncTask;
 
 public class ConversationEngine implements Engine {
     private final static String YAHOO_MORPHO_URL =
         "http://jlp.yahooapis.jp/MAService/V1/parse";
     private final static String YAHOO_KEYPHRASE_URL =
         "http://jlp.yahooapis.jp/KeyphraseService/V1/extract";
     private final static String APP_ID =
         "dj0zaiZpPUZrMk1HTUwxalZoOSZkPVlXazlaWGcwYkdWaE0yTW1jR285TUEtLSZzPWNvbnN1bWVyc2VjcmV0Jng9YTk-";
 
     private enum Type {MORPHOLOGICAL, KEYPHRASE};
     private Type mState = null;
     private Map<String, String> mPayload = new HashMap<String, String>();
     private Context mContext;
     private ResponseListener mListener;
     private HttpRequestTask mTask;
 
     public ConversationEngine(Context context) {
         this.mContext = context;
     }
 
     private void setDefaultPayload(Type type) {
         switch (type) {
             case MORPHOLOGICAL:
                 mPayload.put("appid", APP_ID);
                 mPayload.put("results", "ma");
                 mPayload.put("response", "surface,reading,pos");
                 break;
             case KEYPHRASE:
                 mPayload.put("appid", APP_ID);
                 mPayload.put("output", "xml");
                 break;
         }
     }
 
     private void setSentence(String sentence) {
         mPayload.put("sentence", sentence);
     }
 
     @Override
     public void request(String sentence) {
         analyze(sentence);
     }
 
     private void analyze(String sentence) {
         // simple greeting?
         String response = Greeting.getResponse(mContext, sentence);
 
         // not simple greeting, then analyze morphological
         if (response == null) {
             mState = Type.MORPHOLOGICAL;
             setDefaultPayload(Type.MORPHOLOGICAL);
             setSentence(sentence);
             mTask = new HttpRequestTask(YAHOO_MORPHO_URL, mPayload);
             mTask.execute();
         }
     }
 
     private String parseMorphologic(String xml) {
        MorphoResponseParser mParser = new MorphoResponseParser(mContext);
         mParser.parse(xml);
         String result = mParser.parrot();
         return result;
     }
 
     private String parseResponse(String xml) {
         String str = null;
         switch (mState) {
             case MORPHOLOGICAL:
                 str = parseMorphologic(xml);
                 break;
            case KEYPHRASE:
            	break;
         }
         return str;
     }
 
     @Override
     public void setResponseListener(ResponseListener listener) {
         this.mListener = listener;
     }
 
     private class HttpRequestTask extends AsyncTask<Void, Void, String> {
         private final String mUrl;
         private final Map<String, String>mPayload;
         private HttpRequest request = new HttpRequest();
 
         HttpRequestTask(final String url, final Map<String, String>payload) {
             this.mUrl = url;
             this.mPayload = payload;
         }
 
         @Override
         protected String doInBackground(Void... params) {
             return parseResponse(request.post(mUrl, mPayload));
         }
 
         @Override
         protected void onPostExecute(final String result) {
             if (mListener != null) {
                 mListener.onResult(result);
             }
         }
     }
 }
