 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package silvertrout.plugins.livescore;
 import java.util.ArrayList;
 import org.htmlparser.*;
 import org.htmlparser.filters.*;
 import org.htmlparser.nodes.TextNode;
 import org.htmlparser.tags.LinkTag;
 import org.htmlparser.util.NodeList;
 /**
  *
  * @author Hasse
  */
 public class LiveScoreParser {
     public ArrayList<FootballEvent> getScorers(String link) {
         Parser parser = new Parser();
         ArrayList<FootballEvent> events = new ArrayList<FootballEvent>();
         try {
             NodeFilter tagNameFilter = new TagNameFilter("table");
             parser.setResource(link);
             NodeList nl = parser.parse(tagNameFilter);
             Node node = nl.toNodeArray()[0];
             Node[] nodes = node.getChildren().toNodeArray();
             Node[] tempnodes;
             Node[] innestTemp;
             Tag t;
             String string;
             String matchtime = "";
             String score = "";
             String playername = "";
             String imglink = "";
             for (int i = 4; i < nodes.length; i++) {
                 if (nodes[i].getChildren() == null) {
                     continue;
                 }
                 tempnodes = nodes[i].getChildren().toNodeArray();
                 for (int j = 0; j < tempnodes.length; j++) {
                     if (tempnodes[j].getChildren() == null) {
                         continue;
                     }
                     innestTemp = tempnodes[j].getChildren().toNodeArray();
                     for (int k = 0; k < innestTemp.length; k++) {
                         if (innestTemp[k] instanceof TextNode) {
                             string = innestTemp[k].getText();
                             if (string.contains("'")) {
                                 matchtime = string;
                             } else if (string.contains("[")) {
                                 score = string;
                             } else if (string.contains(".")) {
                                 playername = string;
                             }
                         } else if (innestTemp[k] instanceof Tag && ((Tag) innestTemp[k]).getAttribute("src") != null) {
                             t = (Tag) innestTemp[k];
                             imglink = t.getAttribute("src");
                             events.add(new FootballEvent(matchtime, score, playername, imglink));
                         }
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return events;
     }
 
     public ArrayList<FootballGame> getGames() {
         Parser parser = new Parser();
         ArrayList<FootballGame> games = new ArrayList<FootballGame>();
         try {
             NodeFilter tagNameFilter = new TagNameFilter("table");
             HasAttributeFilter attrFilter = new HasAttributeFilter("bgcolor", "#666666");
             parser.setResource("http://livescores.com/");
             NodeList nl = parser.parse(tagNameFilter);
             nl = nl.extractAllNodesThatMatch(attrFilter);
             attrFilter = new HasAttributeFilter("width", "331");
             nl = nl.extractAllNodesThatMatch(attrFilter);
             Node node = nl.remove(0);
             nl = node.getChildren();
             Node[] nodes = nl.toNodeArray();
             Tag tag;
             String country = "";
             String league = "";
             String hometeam = "";
             String awayteam = "";
             String gametime = "";
             String link = "";
             String result = "";
             for (int i = 0; i < nodes.length; i++) {
                 if (nodes[i] instanceof Tag) {
                     tag = (Tag) nodes[i];
                     String str = tag.getAttribute("bgcolor");
                     if (str != null) {
                         //if(str.contains("11111"))
                         //NEW LEAGUE!
                         //    ;
                         if (str.contains("3333")) {
                             tag = (Tag) tag.getFirstChild();
                             str = tag.getAttribute("class");
                             if (str != null && str.contains("title")) {
                                 country = tag.getChildren().toNodeArray()[2].getText();
                                 league = tag.getChildren().toNodeArray()[4].getText();
                             }
                         } else if (str.contains("f")) {
                             Node[] tempnodes = tag.getChildren().toNodeArray();
                             gametime = tempnodes[0].getFirstChild().getText().split(";")[1];
                             hometeam = tempnodes[1].getFirstChild().getText();
                             awayteam = tempnodes[3].getFirstChild().getText();
                             //RESULTAT
                             if (tempnodes[2].getFirstChild().getFirstChild() != null) {
                                 //MED LÄNK
                                 result = tempnodes[2].getFirstChild().getFirstChild().getText();
                                 link = ((LinkTag) (tempnodes[2].getFirstChild())).extractLink();
                             } else {
                                 //UTAN LÄNK
                                 result = tempnodes[2].getFirstChild().getText();
                                 link = null;
                             }
                             ArrayList<FootballEvent> ev = new ArrayList<FootballEvent>();
                             if (link != null) {
                                 ev = getScorers(link);
                             }
                             games.add(new FootballGame(country, league, hometeam, awayteam, gametime, ev, result));
                         }
                     }
                 }
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         return games;
     }
 }
