 package com.tack.android.loader;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.ProtocolException;
 import java.net.URL;
 
 import android.content.Context;
 import android.support.v4.content.AsyncTaskLoader;
 
 import com.tack.android.R;
 import com.tack.android.model.DataRequestModel;
 import com.tack.android.model.DataRequestModel.RequestType;
 import com.tack.android.model.DataResponseModel;
 import com.tack.android.model.DataResponseModel.ResultType;
 import com.tack.android.util.TackUtil;
 
 public abstract class AsyncDataLoader<T> extends AsyncTaskLoader<T> {
 
   public interface ResponseHandler<T> {
     void handleResponse(DataResponseModel<T> responseData);
   }
   
   private HttpURLConnection urlConnection;
   
   protected DataRequestModel dataRequestModel;
   protected DataResponseModel<T> dataResponseModel;
   protected AsyncDataLoader.ResponseHandler<T> responseHandler;
   protected boolean throwOnNullModel = true;
   
   public AsyncDataLoader(Context context, DataRequestModel requestModel, AsyncDataLoader.ResponseHandler<T> responseHandler) {
     super(context);
     this.dataRequestModel = requestModel;
     this.responseHandler = responseHandler;
   }
   
   @Override
   public void reset() {
     super.reset();
     dataRequestModel = null;
     dataResponseModel = null;
     responseHandler = null;
     
     close();
     urlConnection = null;
   }
   
   /**
    * Handles a request to start the Loader.
    */
   @Override
   protected void onStartLoading() {
     if (dataResponseModel != null) {
       // If we currently have a result available, deliver it immediately.
       deliverResult(dataResponseModel.data);
     }
     
     if (dataRequestModel == null) return;
 
     if (takeContentChanged() || dataResponseModel == null) {
       // If the data has changed since the last time it was loaded
       // or is not currently available, start a load.
       forceLoad();
     }
   }
   
   @Override
   public T loadInBackground() {
     if (dataRequestModel == null) return null;
     
     dataResponseModel = execute();
     
     return dataResponseModel.data;
   }
   
   private DataResponseModel<T> execute() {
     if (null == dataRequestModel) {
       if (throwOnNullModel)
         throw new NullPointerException("DataFetcher must have a non-null requestModel to make a request");
       else
         return null;
     }
     
     DataResponseModel<T> responseModel = new DataResponseModel<T>();
     URL url;
     
     // Make URL request
     try {
       URL_REQUEST: {
         try {
           url = new URL(dataRequestModel.requestURL);
         } catch (MalformedURLException mue) {
           // This really should never happen unless we typo one of the hard-coded urls
           responseModel.resultType = ResultType.ERROR_INVALID_URL;
           responseModel.responseMessage = "Invalid URL.";
           break URL_REQUEST;
         }
       
         // Check connectivity
         if (!TackUtil.isOnline(getContext())) {
           responseModel.resultType = ResultType.ERROR_NO_NETWORK;
           responseModel.responseMessage = getContext().getString(R.string.no_network_connection);
           break URL_REQUEST;
         }
       
         urlConnection = (HttpURLConnection) url.openConnection();
 
         prepareRequestHeaders(urlConnection, dataRequestModel);

        if (urlConnection == null || isAbandoned()) return null;
         
         if (dataRequestModel.requestType == RequestType.PUT || dataRequestModel.requestType == RequestType.POST) {
           byte[] postData = dataRequestModel.postData();
           urlConnection.setDoOutput(true);
           urlConnection.setFixedLengthStreamingMode(postData.length);
 
           OutputStream out = null;
           try {
             // Attempt to post data (network connection occurs here)
             out = urlConnection.getOutputStream();
             out.write(postData);
           } finally {
             if (null != out)
               out.close();
           }
         }
         
        if (urlConnection == null || isAbandoned()) return null;
        
         // Attempt to retrieve the response (network connection occurs here)
         BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream());
         
         // Retrieve header values
         responseModel.headerFields = urlConnection.getHeaderFields();
         responseModel.responseCode = urlConnection.getResponseCode();
         responseModel.responseMessage = urlConnection.getResponseMessage();
         
         // Validate response code
         if (responseModel.responseCode / 100 != 2) break URL_REQUEST;

        if (urlConnection == null || isAbandoned()) return null;
         
         onPreHandleData(responseModel);
         
         try {
           responseModel.data = processData(in);
           responseModel.resultType = ResultType.SUCCESS;
         } catch (Exception exception) {
           responseModel.resultType = ResultType.ERROR_DATA_PARSE_FAILED;
         }
         
         onPostHandleData(responseModel);
       }
       
       if (responseHandler != null) {
         responseHandler.handleResponse(responseModel);
       }
     } catch (IOException e) {
      e.printStackTrace();
       responseModel.resultType = ResultType.ERROR_UNKNOWN;
       responseModel.headerFields = urlConnection.getHeaderFields();
       try {
         responseModel.responseCode = urlConnection.getResponseCode();
         responseModel.responseMessage = urlConnection.getResponseMessage();
       } catch (IOException e2) {
         // just give up
       }
     } finally {
       close();
     }
     
     return responseModel;
   }
   
   public void prepareRequestHeaders(HttpURLConnection urlConnection, DataRequestModel requestModel) throws ProtocolException {
     // Enum request types match correct String names
     urlConnection.setRequestMethod(requestModel.requestType.toString());
 
     // Fake the Firefox user-agent to deal with potentially fussy servers
     urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.9.2.3) Gecko/20100401");
 
     urlConnection.setRequestProperty("Accept", requestModel.acceptType);
     
     if (requestModel.requestType == RequestType.POST) {
       urlConnection.setRequestProperty("Accept-Charset", requestModel.charset);
       urlConnection.setRequestProperty("Content-Type", requestModel.contentType);
     }
   }
   
   /**
    * Optional method available to override to allow subclasses to perform logic
    * before data request is made
    * @param responseModel 
    */
   public void onPreHandleData(DataResponseModel<T> responseModel) {
   }
   
   public abstract T processData(BufferedInputStream inputStream) throws Exception;
   
   /**
    * Optional method available to override to allow subclasses to perform logic
    * just after a data request has returned.
    * Occurs prior to response handler notification.
    * @param responseModel 
    */
   public void onPostHandleData(DataResponseModel<T> responseModel) {
   }
   
   public void close() {
     if (null != urlConnection) { 
       urlConnection.disconnect();    
       urlConnection = null;
     }
   }
   
   /* Getters / Setters */
   
   public void setThrowOnNullModel(boolean throwOnNullModel) {
     this.throwOnNullModel = throwOnNullModel;
   }
   
   public boolean getThrowOnNullModel() {
     return throwOnNullModel;
   }
   
   public DataResponseModel<T> getDataResponseModel() {
     return dataResponseModel;
   }
   
 }
