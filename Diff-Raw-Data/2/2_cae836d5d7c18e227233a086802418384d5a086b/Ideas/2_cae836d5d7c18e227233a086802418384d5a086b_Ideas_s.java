 package com.masterofcode.android._10ideas.objects;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import java.util.Vector;
 
 /**
  * Created with IntelliJ IDEA.
  * User: boss1088
  * Date: 5/26/12
  * Time: 12:42 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Ideas {
 
     Vector<Idea> _ideas = new Vector<Idea>();
     protected int total = 0;
 
     public Ideas() {
     }
 
    public static Ideas fromJson(JSONArray json) throws JSONException/*, NullPointerException*/ {
         Ideas collection = new Ideas();
         collection.setTotal(json.length());
 
         for (int i = 0; i < json.length(); i++) {
             JSONObject obj = (JSONObject) json.get(i);
             /*if(obj.optJSONObject("seminar").optInt("status")==3){*/
             collection.addSeminar(new Idea(obj));
             /*}*/
         }
         collection.setTotal(collection.getItems().size());
         return collection;
     }
 
     public Vector getItems() {
         return _ideas;
     }
 
     public void addSeminar(Idea s) {
         _ideas.addElement(s);
     }
 
     public int getTotal() {
         return total;
     }
 
     public void setTotal(int total) {
         this.total = total;
     }
 }
