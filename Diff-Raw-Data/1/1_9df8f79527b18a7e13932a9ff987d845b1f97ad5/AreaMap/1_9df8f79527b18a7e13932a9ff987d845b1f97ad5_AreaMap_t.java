 package com.livejournal.karino2.subtitle2;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 
 
 import com.jsonengine.model.JEDoc;
 
 public class AreaMap extends JEDocWrapper {
 
 
     public static final int TEXTS_PER_AREA = 20;
     
     public static final int STATUS_EMPTY = 1;
     public static final int STATUS_ASSIGNED = 2;
     public static final int STATUS_DONE = 3;
     
     // 24 hour
     public static final long FREE_AFTER = 24*60*60*1000;
 
     public AreaMap(JEDoc json) {
         super(json);
     }
     
     public int getTextNum() {
         return getInt("textNum");
     }
     
     public int getAreaNum() {
         return 1 + (getTextNum()-1)/TEXTS_PER_AREA;
     }
     
     public long getNowTick() {
         long now = (new Date()).getTime();
         return now;
     }
 
     public void assignArea(int areaIndex, String account) {
         List<Object> obj = getAreaJSON(areaIndex);
         obj.set(0, "a");
         obj.set(1,  account);
         obj.set(2, getNowTick());
     }
     public void doneArea(int areaIndex) {
         List<Object> obj = getAreaJSON(areaIndex);
         obj.set(0, "d");
         obj.set(1,  "");
         obj.set(2, getNowTick());
     }
     public void freeArea(int areaIndex) {
         List<Object> obj = getAreaJSON(areaIndex);
         obj.set(0, "e");
         obj.set(1,  "");
         obj.set(2, getNowTick());
     }
         
     Random rand = new Random();
     public int findEmptyArea(int avoidId, String account) {
         List<Integer> empties = collectEmptyAreas(account);
         if(avoidId != -1) {
             empties.remove((Integer)avoidId);
         }
         if(empties.size() == 0)
             return 0;
         int res = findMyArea(account, avoidId);
         if(res != -1)
             return res;
         return empties.get(rand.nextInt(empties.size()));
     }
 
     public int findMyArea(String account, int avoidId) {
         for(int i = 1; i <= getAreaNum(); i++) {
             if(i == avoidId)
                 continue;
             if(isMyArea(account, i))
                 return i;
         }
         return -1;
     }
 
     private List<Integer> collectEmptyAreas(String account) {
         List<Integer> res = new ArrayList<Integer>();
         for(int i = 1; i <= getAreaNum(); i++) {
             if(isEmpty(i, account))
                 res.add(i);
         }
         return res;
     }
 
     private boolean isEmpty(int areaIndex, String account) {
         if(areaIndex < 1 ||
                 areaIndex > getAreaNum())
             return false;
         if(STATUS_EMPTY == getStatus(areaIndex))
             return true;
         if(STATUS_DONE == getStatus(areaIndex))
             return false;
         if(isMyArea(account, areaIndex))
             return true;
         return isOldEnough(areaIndex);
     }
 
     private boolean isOldEnough(int areaIndex) {
         long tick = getUpdateTick(areaIndex);
         long now = getNowTick();
         return (now-tick) > FREE_AFTER;
     }
 
     private long getUpdateTick(int areaIndex) {
         List<Object> obj = getAreaJSON(areaIndex);
         return (Long)obj.get(2);
     }
 
     private boolean isMyArea(String account, int areaIndex) {
         return account.equals(getAccount(areaIndex));
     }
 
     private String getAccount(int areaIndex) {
         List<Object> array = getAreaJSON(areaIndex);
         if(array.get(1) == null)
             return "";
         return (String)array.get(1);
     }
 
     private int getStatus(int areaIndex) {
         List<Object> array = getAreaJSON(areaIndex);
 
         String stat = (String)array.get(0);
         if(stat.equals("a"))
             return STATUS_ASSIGNED;
         if(stat.equals("d"))
             return STATUS_DONE;
         return STATUS_EMPTY;
     }
 
     @SuppressWarnings("unchecked")
     private List<Object> getAreaJSON(int areaIndex) {
         return (List<Object>)get("a" + areaIndex);
     }
     
     public Range getRange(int areaIndex) {
         return new Range(firstTextIndex(areaIndex), lastTextIndex(areaIndex));
     }
     
     private Range getAreaRangeWithHeaderFooter(int areaIndex) {
         int beginIdx = firstTextIndex(areaIndex)-2;
         int endIdx = lastTextIndex(areaIndex)+2;
         return new Range(Math.max(1, beginIdx), Math.min(endIdx, getTextNum()));
     }
     
     public List<Text> getAreaTextWithHeaderFooter(List<JEDoc> orderedText, int areaIndex) {
         Range range = getAreaRangeWithHeaderFooter(areaIndex);
         List<Text> res = new ArrayList<Text>();
         for(JEDoc doc: orderedText) {
             Text txt = new Text(doc);
             if(range.inside(txt.getIndex())) {
                 res.add(txt);
             }
         }
         return res;
     }
     
     private int lastTextIndex(int areaIndex) {
         int endIdx = areaIndex*TEXTS_PER_AREA;
         return Math.min(endIdx, getTextNum());
     }
     private int firstTextIndex(int areaIndex) {
         int beginIdx = 1+ (areaIndex-1)*TEXTS_PER_AREA;
         return beginIdx;
     }
     
 }
