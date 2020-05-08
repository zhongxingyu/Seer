 /*
  * Copyright 2013- Yan Bonnel
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package fr.ybonnel.breizhcamppdf;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.google.gson.Gson;
 import com.google.gson.GsonBuilder;
 import fr.ybonnel.breizhcamppdf.model.*;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.*;
 
 public class DataService {
 
     private Gson gson = new GsonBuilder().create();
 
     private Programme programme;
 
     private Programme getProgramme() {
         if (programme == null) {
             try {
                 //URL url = new URL("file:///D:/sources/Breizhcamp-cfp/conf/breizhcamp.json");
                 URL url = new URL("http://www.breizhcamp.org/json/schedule.json");
                 URLConnection connection = url.openConnection();
                 programme = gson.fromJson(new InputStreamReader(connection.getInputStream()), Event.class).getProgramme();
 
                 for (Jour jour : getProgramme().getJours()) {
                     for (Track track : jour.getTracks()) {
                         for (Talk talk : track.getProposals()) {
                             TalkService.INSTANCE.getTalkDetail(talk);
                         }
                     }
                 }
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
         return programme;
     }
 
 
 
     public List<String> getDates() {
         return Lists.transform(getProgramme().getJours(), new Function<Jour, String>() {
             @Override
             public String apply(Jour input) {
                 return input.getDate();
             }
         });
     }
 
 
     private List<Talk> talks = null;
 
     public List<Talk> getTalks() {
         if (talks == null) {
             talks = new ArrayList<>();
             for (Jour jour : getProgramme().getJours()) {
                 for (Track track : jour.getTracks()) {
                     talks.addAll(track.getProposals());
                 }
             }
         }
         return talks;
     }
 
     private Map<String, List<Talk>> talksByDate;
 
     public Map<String, List<Talk>> getTalksByDate() {
         if (talksByDate == null) {
             talksByDate = new HashMap<>();
             for (Jour jour : getProgramme().getJours()) {
                 talksByDate.put(jour.getDate(), new ArrayList<Talk>());
                 for (Track track : jour.getTracks()) {
                     talksByDate.get(jour.getDate()).addAll(track.getProposals());
                 }
             }
         }
         return talksByDate;
     }
 
     private Map<String,List<String>> roomsByDate = new HashMap<>();
 
     public List<String> getRooms(String date) {
         List<String> rooms = roomsByDate.get(date);
         if (rooms == null) {
             Set<String> roomsInSet = new HashSet<>();
             for (Talk talk : getTalksByDate().get(date)) {
                 if (talk.getRoom() == null) {
                     System.err.println("Talk without room : " + talk.getTitle());
                 }
                 roomsInSet.add(talk.getRoom());
             }
             rooms = new ArrayList<>(roomsInSet);
             Collections.sort(rooms);
             roomsByDate.put(date, rooms);
         }
         return rooms;
     }
 
     private Map<String, List<String>> creneaux;
 
     public Map<String, List<String>> getCreneaux() {
         if (creneaux == null) {
             creneaux = new HashMap<>();
             for (Map.Entry<String, List<Talk>> entry : getTalksByDate().entrySet()) {
                 Set<String> creneauxForDate = new HashSet<>();
                 for (Talk talk : entry.getValue()) {
                     creneauxForDate.add(talk.getStart());
                 }
                 List<String> creneauxInList = new ArrayList<>(creneauxForDate);
                 Collections.sort(creneauxInList);
                 creneaux.put(entry.getKey(), creneauxInList);
             }
         }
         return creneaux;
     }
 
     public Talk getTalkByDateAndCreneauxAndRoom(String date, String creneau, String room) {
         Talk talkSelected = null;
         for (Talk talk : getTalksByDate().get(date)) {
             if (creneau.equals(talk.getStart()) && room.equals(talk.getRoom())) {
                 if (talkSelected != null) {
                     throw new RuntimeException("Two talk for " + date + " " + creneau + " " + room);
                 }
                 talkSelected = talk;
             }
         }
         return talkSelected;
     }
 }
