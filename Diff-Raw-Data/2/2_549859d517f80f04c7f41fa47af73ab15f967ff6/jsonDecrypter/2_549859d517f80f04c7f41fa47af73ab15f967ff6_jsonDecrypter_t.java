 package com.artum.shootmaniacenter.utilities;
 
 import com.artum.shootmaniacenter.structures.nadeo.PlayerData;
 import com.artum.shootmaniacenter.structures.nadeo.RankElement;
 
 import java.lang.String;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.json.JSONArray;
 
 /**
  * Created by artum on 18/05/13.
  */
 
 public class jsonDecrypter {
 
     public RankElement[] getRanksElementFromSegment(String segment, int lenght)
     {
         RankElement[] rankElements = new RankElement[lenght];
 
         try
         {
         JSONObject mainSecgment = new JSONObject(segment);
         JSONArray jsonArray = mainSecgment.getJSONArray("players");
            for (int i = 0; i < lenght; i++)
             {
                 RankElement temp = new RankElement();
                 JSONObject data = jsonArray.getJSONObject(i);
                 temp.rank = data.getInt("rank");
                 temp.points = data.getDouble("points");
                 temp.playerData = getPlayerFromSegment(data.getJSONObject("player"));
 
                 rankElements[i] = temp;
             }
         }
         catch (JSONException e) {
             e.printStackTrace();
         }
         return rankElements;
     }
 
     public PlayerData getPlayerFromSegment(JSONObject playerSegment)
     {
         PlayerData temp = new PlayerData();
 
         try {
             temp.nick = playerSegment.getString("nickname");
             temp.region = playerSegment.getString("path");
             temp.login = playerSegment.getString("login");
             temp.id = playerSegment.getInt("id");
             temp.regionId = playerSegment.getInt("idZone");
         } catch (JSONException e) {
             e.printStackTrace();
         }
         return temp;
     }
 
 }
