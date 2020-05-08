 package com.home.giraffe.objects.socialnews;
 
 import com.home.giraffe.objects.Jive.JiveContainer;
 import com.home.giraffe.objects.Jive.JiveObject;
 
 public class LevelSocialNewsItem extends BaseSocialNewsItem {
     private String mLevel;
     private String mLevelImageUrl;
 
     public LevelSocialNewsItem(JiveContainer jiveContainer){
         fromJiveContainer(jiveContainer);
     }
 
     public String getLevel() {
         return mLevel;
     }
 
     public void setLevel(String level) {
         this.mLevel = level;
     }
 
     public String getLevelImageUrl() {
         return mLevelImageUrl;
     }
 
     public void setLevelImageUrl(String levelImageUrl) {
         this.mLevelImageUrl = levelImageUrl;
     }
 
     @Override
     public void fromJiveContainer(JiveContainer jiveContainer) {
         super.fromJiveContainer(jiveContainer);
 
         JiveObject jiveObject = jiveContainer.getObject();
         setLevel(jiveObject.getDisplayName());
        if(jiveObject.getImage() != null)
            setLevelImageUrl(jiveObject.getImage().getUrl());
     }
 }
