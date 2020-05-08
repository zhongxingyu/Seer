 package com.music.fmv.fragments;
 
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.inputmethod.EditorInfo;
 import android.widget.*;
 import com.music.fmv.R;
 import com.music.fmv.core.BaseFragment;
 import com.music.fmv.models.SearchBandModel;
 import com.music.fmv.tasks.SearchBandTask;
 import com.music.fmv.utils.ViewUtils;
 import com.music.fmv.views.GlowButton;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: lebed
  * Date: 7/14/13
  * Time: 9:03 AM
  * To change this template use File | Settings | File Templates.
  */
 
 public class SearchFragment  extends BaseFragment{
     private ListView artist_result_list;
     private ListView album_result_list;
     private ListView name_result_list;
 
     private GlowButton artistTab;
     private GlowButton albumTab;
     private GlowButton nameTab;
 
     private EditText searchField;
 
     private String lastArtistRequest;
     private String lastAlbumRequest;
     private String lastNameRequest;
 
     private SearchType searchType;
 
     private ArrayList<SearchBandModel> searchBandList = new ArrayList<SearchBandModel>();
 
     public enum SearchType {
         ARTIST, NAME, ALBUM
     }
 
     @Override
     public void createView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         mainView = inflater.inflate(R.layout.search_fragment, container, false);
         initUI();
         artistTabClicked();
     }
 
     //----------------------------------------------------------------------------------------------------------------//
     private void searchBand(String query, Integer page){
         if (TextUtils.isEmpty(query)) return;
 
         SearchBandTask task = new SearchBandTask(query, page, baseActivity){
             @Override
             protected void onPostExecute(List<SearchBandModel> searchBandModels) {
                 if (searchBandModels != null && searchBandModels.size() > 0){
                     updateBandList(searchBandModels);
                     return;
                 }
 
                 if (searchBandModels != null && searchBandModels.size() == 0){
                     Toast.makeText(baseActivity, getString(R.string.empty_result), Toast.LENGTH_SHORT).show();
                     return;
                 }
 
                 if (isError || searchBandModels == null){
                     Toast.makeText(baseActivity, getString(R.string.request_error), Toast.LENGTH_SHORT).show();
                 }
             }
         };
         if (runTask(task)){
             lastArtistRequest = query;
         }
     }
 
     private void getNextBandPage(){
         searchBand(lastArtistRequest, null);
     }
 
     private void updateBandList(List<SearchBandModel> searchBandModels){
         searchBandList.addAll(searchBandModels);
     }
 
     //----------------------------------------------------------------------------------------------------------------//
     private void searchAlbum(String query){
 
     }
 
     private void getNextAlbumPage(){
 
     }
 
     private void updateAlbumList(){
 
     }
 
     //----------------------------------------------------------------------------------------------------------------//
     private void searchSongs(String query){
 
     }
 
     private void getNextSongsPage(){
 
     }
 
     private void updateSongsList(){
 
     }
 
     private void lastItemVisible() {
         switch (searchType){
             case ALBUM:
                 getNextAlbumPage();
                 break;
             case NAME:
                 getNextSongsPage();
                 break;
             case ARTIST:
                 getNextBandPage();
         }
     }
 
     private void processSearch(String query) {
         switch (searchType){
             case ALBUM:
                 searchAlbum(query);
                 break;
             case NAME:
                 searchSongs(query);
                 break;
             case ARTIST:
                 searchBand(query, null);
         }
     }
 
     private void initUI() {
         artist_result_list = (ListView) mainView.findViewById(R.id.artist_result_list);
         name_result_list = (ListView) mainView.findViewById(R.id.name_result_list);
         album_result_list = (ListView) mainView.findViewById(R.id.album_result_list);
 
         artistTab = (GlowButton) mainView.findViewById(R.id.by_artist);
         albumTab = (GlowButton) mainView.findViewById(R.id.by_album);
         nameTab = (GlowButton) mainView.findViewById(R.id.by_name);
 
         artistTab.setOnClickListener(tabListener);
         albumTab.setOnClickListener(tabListener);
         nameTab.setOnClickListener(tabListener);
     }
 
     private View.OnClickListener tabListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             switch (v.getId()){
                 case R.id.by_artist:
                     artistTabClicked();
                     break;
                 case R.id.by_album:
                     albumTabClicked();
                     break;
                 case R.id.by_name:
                     nameTabClicked();
 
             }
         }
     };
 
     private void albumTabClicked() {
         ViewUtils.setVisible(album_result_list, View.GONE, name_result_list, artist_result_list);
         searchType = SearchType.ALBUM;
     }
 
     private void nameTabClicked() {
         ViewUtils.setVisible(name_result_list, View.GONE, artist_result_list, album_result_list);
         searchType = SearchType.NAME;
     }
 
     private void artistTabClicked() {
         ViewUtils.setVisible(artist_result_list, View.GONE, name_result_list, album_result_list);
         searchType = SearchType.ARTIST;
     }
 
     private AdapterView.OnItemClickListener bandClickListener = new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
         }
     };
 
     private AdapterView.OnItemClickListener albumClickListener = new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
         }
     };
 
     private AdapterView.OnItemClickListener songsClickListener = new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 
         }
     };
 
     private AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {
         @Override public void onScrollStateChanged(AbsListView view, int scrollState) {}
         @Override
         public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
             final int lastItem = firstVisibleItem + visibleItemCount;
             if(lastItem == totalItemCount) {
                 lastItemVisible();
             }
         }
     };
 
     private TextView.OnEditorActionListener searchListener = new TextView.OnEditorActionListener() {
         @Override
         public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
             if (actionId == EditorInfo.IME_ACTION_SEARCH){
                 processSearch(searchField.getText().toString());
                 return true;
             }
             return false;
         }
     };
 }
