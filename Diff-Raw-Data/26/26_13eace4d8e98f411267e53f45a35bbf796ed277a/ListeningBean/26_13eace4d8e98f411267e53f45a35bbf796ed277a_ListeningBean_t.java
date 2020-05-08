 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.home.hibernatejsf.web;
 
 import com.home.hibernatejsf.model.Music;
 import com.home.hibernatejsf.model.MusicType;
 import com.home.hibernatejsf.model.User;
 import com.home.hibernatejsf.service.MusicService;
 import com.home.hibernatejsf.service.UserService;
 import com.home.hibernatejsf.service.UtilService;
 import com.home.hibernatejsf.util.SessionAttributes;
 import com.home.hibernatejsf.util.Utils;
 import java.io.Serializable;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import javax.faces.bean.ManagedBean;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author Mike
  */
 @ManagedBean
 public class ListeningBean implements Serializable{
     private static final Logger LOG = Logger.getLogger(ListeningBean.class);
     private UtilService utilService;
     private MusicService musicService;
     private UserService userService;
     // Listen page elements
     private Integer typeOfRecords;
     // TODO: need to update music list when we swith from other tab!
     private List<Music> musicList;
     private int currentMusicIndex;
     private String currentMusic;
     // prev and next links
     private boolean disabledPrevLink;
     private boolean disabledNextLink;
     // utils variable
     private static final String PLAY_LIST_EMPTY = "Playlist is empty!";
     
 
     
     public void setUtilService(UtilService utilService){
         this.utilService = utilService;
     }
     
     public void setMusicService(MusicService musicService){
         this.musicService = musicService;
     }
     
     public void setUserService(UserService userService){
         this.userService = userService;
     }
     /**
      * @return the typeOfRecords!
      */
     public Integer getTypeOfRecords() {
         return typeOfRecords;
     }
 
     /**
      * @param typeOfRecords the typeOfRecords to set
      */
     public void setTypeOfRecords(Integer typeOfRecords) {
         this.typeOfRecords = typeOfRecords;
         updateMusicListByMusicType();
     }
     
     public Map<String, Integer> getList(){
     Map<String, Integer> resultList = new LinkedHashMap<String, Integer>();
     for (MusicType mt : utilService.getAllMusicTypes()){
         resultList.put(mt.getTitle(), mt.getMusicTypeId());
     }
     return resultList;
     }
     
     private void updateMusicListByMusicType(){
         if(typeOfRecords == null){
             currentMusicIndex = 0;
         }
         else{
             MusicType mt = utilService.getMusicTypeById(typeOfRecords);
             setMusicList(musicService.getMusicListByMusicType(mt));
             currentMusicIndex = 0;
         }
     }
 
     /**
      * @return the currentMusic
      */
     public String getCurrentMusic() {
         return currentMusic;
     }
 
     /**
      * @param currentMusic the currentMusic to set
      */
     public void setCurrentMusic(String currentMusic) {
         this.currentMusic = currentMusic;
     }
 
     /**
      * @return the currentMusicIndex
      */
     public Integer getCurrentMusicIndex() {
         return currentMusicIndex;
     }
 
     /**
      * @param currentMusicIndex the currentMusicIndex to set
      */
     public void setCurrentMusicIndex(Integer currentMusicIndex) {
         this.currentMusicIndex = currentMusicIndex;
     }
 
     /**
      * @return the musicList
      */
     public List<Music> getMusicList() {
         return musicList;
     }
 
     /**
      * @param musicList the musicList to set
      */
     public void setMusicList(List<Music> musicList) {
         this.musicList = musicList;
     }
 
     /**
      * @return the disabledPrevLink
      */
     public boolean isDisabledPrevLink() {
         if(currentMusicIndex > 0){
             disabledPrevLink = false;
         }
         else{
             disabledPrevLink = true;
         }
         return disabledPrevLink;
     }
 
     /**
      * @param disabledPrevLink the disabledPrevLink to set
      */
     public void setDisabledPrevLink(boolean disabledPrevLink) {
         this.disabledPrevLink = disabledPrevLink;
     }
 
     /**
      * @return the disabledNextLink
      */
     public boolean isDisabledNextLink() {
         if(musicList != null && (currentMusicIndex  < (musicList.size()-1))){
             disabledNextLink = false;
         }
         else{ 
             disabledNextLink = true;
         }
         return disabledNextLink;
     }
 
     /**
      * @param disabledNextLink the disabledNextLink to set
      */
     public void setDisabledNextLink(boolean rendredNextLink) {
         this.disabledNextLink = rendredNextLink;
     }
     
     public void decrementCurrentMusicIndex()
     {
         currentMusicIndex--;
         updateCurrentMusic();
     }
     
     public void incrementCurrentMusicIndex()
     {
         currentMusicIndex++;
         updateCurrentMusic();
     }
     
     public void updateCurrentMusic()
     {
         if(musicList != null && !musicList.isEmpty())
             currentMusic = musicList.get(currentMusicIndex).getMusicContent();
         else
             currentMusic = PLAY_LIST_EMPTY;
     }
     
     public void buyCurrentMusic(){
        if (musicList != null) {
            Long currentUserId = (Long) Utils.getAtributeFromSession(SessionAttributes.UserId.getAttributeName());
            User currentUser = userService.getUserById(currentUserId);
            if (currentUser != null) {
                List<Music> userMusicList = currentUser.getListMusic();
                if (userMusicList == null) {
                    userMusicList = new LinkedList<Music>();
                }
                if (!userMusicList.contains(musicList.get(currentMusicIndex))) {
                    userMusicList.add(musicList.get(currentMusicIndex));
                    userService.updateUser(currentUser);
                }
             }
         }
     }
 }
