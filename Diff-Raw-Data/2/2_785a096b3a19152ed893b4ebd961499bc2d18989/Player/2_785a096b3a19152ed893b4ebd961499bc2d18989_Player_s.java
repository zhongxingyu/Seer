 package com.kg.lhsfantasyfootball;
 
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 /**
  *
  * @author Kaustubh
  */
 public class Player {
 
     String name;
     String team;
     String position;
     String imageURL;
     String teamImageURL;
     int completions;
     int attempts;
     int passingYards;
     int passingTouchdowns;
     int interceptions;
     int rushes;
     int rushingYards;
     int rushingTouchdowns;
     int receptions;
     int recievingYards;
     int recievingTouchdowns;
     int fantasyPoints;
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getTeam() {
         return team;
     }
 
     public void setTeam(String team) {
         this.team = team;
     }
 
     public String getPosition() {
         return position;
     }
 
     public void setPosition(String position) {
         this.position = position;
     }
 
     public String getImageURL() {
         return imageURL;
     }
 
     public void setImageURL(String imageURL) {
         this.imageURL = imageURL;
     }
 
     public String getTeamImageURL() {
         return teamImageURL;
     }
 
     public void setTeamImageURL(String teamImageURL) {
         this.teamImageURL = teamImageURL;
     }
 
     public int getCompletions() {
         return completions;
     }
 
     public void setCompletions(int completions) {
         this.completions = completions;
     }
 
     public int getAttempts() {
         return attempts;
     }
 
     public void setAttempts(int attempts) {
         this.attempts = attempts;
     }
 
     public int getPassingYards() {
         return passingYards;
     }
 
     public void setPassingYards(int passingYards) {
         this.passingYards = passingYards;
     }
 
     public int getPassingTouchdowns() {
         return passingTouchdowns;
     }
 
     public void setPassingTouchdowns(int passingTouchdowns) {
         this.passingTouchdowns = passingTouchdowns;
     }
 
     public int getInterceptions() {
         return interceptions;
     }
 
     public void setInterceptions(int interceptions) {
         this.interceptions = interceptions;
     }
 
     public int getRushes() {
         return rushes;
     }
 
     public void setRushes(int rushes) {
         this.rushes = rushes;
     }
 
     public int getRushingYards() {
         return rushingYards;
     }
 
     public void setRushingYards(int rushingYards) {
         this.rushingYards = rushingYards;
     }
 
     public int getRushingTouchdowns() {
         return rushingTouchdowns;
     }
 
     public void setRushingTouchdowns(int rushingTouchdowns) {
         this.rushingTouchdowns = rushingTouchdowns;
     }
 
     public int getReceptions() {
         return receptions;
     }
 
     public void setReceptions(int receptions) {
         this.receptions = receptions;
     }
 
     public int getRecievingYards() {
         return recievingYards;
     }
 
     public void setRecievingYards(int recievingYards) {
         this.recievingYards = recievingYards;
     }
 
     public int getRecievingTouchdowns() {
         return recievingTouchdowns;
     }
 
     public void setRecievingTouchdowns(int recievingTouchdowns) {
         this.recievingTouchdowns = recievingTouchdowns;
     }
 
     public int getFantasyPoints() {
         return fantasyPoints;
     }
 
     public void setFantasyPoints(int fantasyPoints) {
         this.fantasyPoints = fantasyPoints;
     }
 
     public Player(Element e) {
         try {
             Elements eles = e.getElementsByTag("td");
             String nameString = eles.get(1).text();
             int indexOfComma = nameString.indexOf(",");
             name = nameString.substring(0, indexOfComma);
             if (name.charAt(name.length() - 1) == '*') {
                 name = name.substring(0, name.length() - 1);
             }
             String teamString = nameString.substring(indexOfComma + 2);
             int spaceIndex = teamString.indexOf(160);
             team = teamString.substring(0, spaceIndex);
             if (teamString.charAt(spaceIndex + 1) == 'K') {
                 position = "K";
             } else {
                 position = teamString.substring(spaceIndex + 1, spaceIndex + 3);
             }
             String imgURL = getPlayerImage();
             if (imgURL == null) {
                 imgURL = "http://www.plated.com/assets/blank_profile.png";
             }
             imageURL = imgURL;
             String completionString = eles.get(2).text();
             int indexOfSlash = completionString.indexOf("/");
             completions = Integer.parseInt(completionString.substring(0, indexOfSlash));
             attempts = Integer.parseInt(completionString.substring(indexOfSlash + 1));
             passingYards = Integer.parseInt(eles.get(3).text());
             passingTouchdowns = Integer.parseInt(eles.get(4).text());
             interceptions = Integer.parseInt(eles.get(5).text());
             rushes = Integer.parseInt(eles.get(6).text());
             rushingYards = Integer.parseInt(eles.get(7).text());
             rushingTouchdowns = Integer.parseInt(eles.get(8).text());
             receptions = Integer.parseInt(eles.get(9).text());
             recievingYards = Integer.parseInt(eles.get(10).text());
             recievingTouchdowns = Integer.parseInt(eles.get(11).text());
             fantasyPoints = Integer.parseInt(eles.get(12).text());
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
     private String getPlayerImage() {
         try {
             String n = name.replace(" ", "-").toLowerCase();
             Document doc = Jsoup.connect("http://search.espn.go.com/" + n + "/").get();
             String href = doc.getElementsByTag("h3").get(0).getElementsByTag("a").get(0).attr("href");
             doc = Jsoup.connect(href).get();
            if (doc.getElementsByClass("main-headshot").size() > 0) {
                 String src = doc.getElementsByClass("main-headshot").get(0).getElementsByTag("img").get(0).attr("src");
                 src = src.substring(0, src.indexOf("png") + 3);
                 return src;
             } else{
                 return null;
             }
         } catch (Exception ex) {
             Logger.getLogger(Player.class.getName()).log(Level.SEVERE, null, ex);
         }
         return null;
     }
 }
