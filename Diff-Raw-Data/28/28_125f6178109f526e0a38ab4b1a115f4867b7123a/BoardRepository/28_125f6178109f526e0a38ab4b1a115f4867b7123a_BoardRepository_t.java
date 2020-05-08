 package com.thoughtworks.orteroid.repositories;
 
 import com.thoughtworks.orteroid.Callback;
 import com.thoughtworks.orteroid.constants.Constants;
 import com.thoughtworks.orteroid.models.Board;
 import com.thoughtworks.orteroid.models.Point;
 import com.thoughtworks.orteroid.utilities.ContentFetcher;
 import com.thoughtworks.orteroid.utilities.JSONParser;
 import com.thoughtworks.orteroid.utilities.URLGenerator;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.List;
 
 public class BoardRepository {
 
     private static BoardRepository boardRepository;
     private static URLGenerator urlGenerator;
 
 
     private BoardRepository() {
         urlGenerator = new URLGenerator();
     }
 
     public void retrieveBoard(String boardKey, String boardId, final Callback<Board> callback) {
         String boardURL = urlGenerator.getBoardURL(boardKey, boardId);
         String pointsURL = urlGenerator.getPointsURL(boardKey, boardId);
         Callback<List<String>> serverCallback = generateServerCallbackForGetRequestForBoard(callback);
         executeAsyncTask(serverCallback, boardURL, pointsURL);
     }
 
     public void retrievePoints(String boardKey, String boardId, Callback<List<Point>> pointsCallback) {
         String pointsURL = urlGenerator.getPointsURL(boardKey, boardId);
         Callback<List<String>> serverCallback = generateServerCallbackForGetRequestForPoints(pointsCallback);
         executeAsyncTask(serverCallback, pointsURL);
     }
 
     private void executeAsyncTask(Callback<List<String>> serverCallback, String... urls) {
         ContentFetcher contentFetcher = new ContentFetcher(serverCallback, Constants.GET);
         int length = urls.length;
         if(length == 1){
             contentFetcher.execute(urls[0]);
         } else {
             contentFetcher.execute(urls[0], urls[1]);
         }
     }
 
     public void addIdea(String idea, int sectionId, final Callback<Boolean> callback) {
         String encodedMessage = null;
         try {
             encodedMessage = URLEncoder.encode(idea, "UTF-8");
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         }
         String response = urlGenerator.setUrlForAddingIdea(sectionId, encodedMessage);
         Callback<List<String>> serverCallback = generateServerCallbackForPostRequest(callback);
         ContentFetcher contentFetcher = new ContentFetcher(serverCallback, Constants.POST);
         contentFetcher.execute(response);
     }
 
     public void editIdea(String editedIdea, int ideaId, final Callback<Boolean> callback) {
         String encodedMessage = editedIdea.replace(" ", "%20");
         String response = urlGenerator.urlForEditingIdea(ideaId, encodedMessage);
         Callback<List<String>> serverCallback = generateServerCallbackForPutRequest(callback);
         ContentFetcher contentFetcher = new ContentFetcher(serverCallback, Constants.PUT);
         contentFetcher.execute(response);
     }
 
     public void deletePoint(Point selectedPoint, final Callback<Boolean> callback) {
         String response = urlGenerator.urlForDeletingIdea(selectedPoint);
         Callback<List<String>> serverCallback = generateServerCallbackForGetRequest(callback);
         ContentFetcher contentFetcher = new ContentFetcher(serverCallback, Constants.GET);
         contentFetcher.execute(response);
     }
 
     private Callback<List<String>> generateServerCallbackForGetRequest(final Callback<Boolean> callback) {
         return  new Callback<List<String>>() {
             @Override
             public void execute(List<String> jsonResponseList) {
                 callback.execute(true);
                 if(jsonResponseList.get(0).equals("404 error")){
                     callback.execute(null);
                 }
             }
         };
     }
 
     private Callback<List<String>> generateServerCallbackForGetRequestForBoard(final Callback<Board> callback) {
         return new Callback<List<String>>() {
             @Override
             public void execute(List<String> jsonResponseList){
                 try{
                     JSONObject jsonObject = new JSONObject(jsonResponseList.get(0));
                     final Board boardSkeleton = JSONParser.parseToBoard(jsonObject);
                     boardSkeleton.update(JSONParser.parseToPoints(jsonResponseList.get(1)));
                     callback.execute(boardSkeleton);
                } catch (JSONException e) {
                    callback.execute(null);
                 }
             }
         };
     }
 
     private Callback<List<String>> generateServerCallbackForGetRequestForPoints(final Callback<List<Point>> callback) {
         return new Callback<List<String>>() {
             @Override
             public void execute(List<String> jsonResponseList)  {
                 final List<Point> points;
                 try {
                     points = JSONParser.parseToPoints(jsonResponseList.get(0));
                 } catch (JSONException e) {
                     if(jsonResponseList.get(0).equals("404 error")){
                         callback.execute(null);
                     }
                     throw new RuntimeException(e);
                 }
                 callback.execute(points);
             }
         };
     }
 
     private Callback<List<String>> generateServerCallbackForPostRequest(final Callback<Boolean> callback) {
         return new Callback<List<String>>() {
             @Override
             public void execute(List<String> jsonResponseList)  {
                 try {
                     JSONObject jsonObject = new JSONObject(jsonResponseList.get(0));
                     JSONParser.parseToPoint(jsonObject);
                     callback.execute(true);
                 } catch (JSONException e) {
                     callback.execute(false);
                     e.printStackTrace();
                 }
             }
         };
     }
 
     private Callback<List<String>> generateServerCallbackForPutRequest(final Callback<Boolean> callback) {
         return new Callback<List<String>>() {
             @Override
             public void execute(List<String> jsonResponseList){
                 if(jsonResponseList.get(0).equals("404 error")){
                     callback.execute(null);
                 }
                 callback.execute(true);
             }
         };
     }
 
     public static BoardRepository getInstance() {
         if (boardRepository == null) {
             boardRepository = new BoardRepository();
         }
         return boardRepository;
     }
 
     public static void setBoardRepository(BoardRepository boardRepository) {
         BoardRepository.boardRepository = boardRepository;
     }
 }
