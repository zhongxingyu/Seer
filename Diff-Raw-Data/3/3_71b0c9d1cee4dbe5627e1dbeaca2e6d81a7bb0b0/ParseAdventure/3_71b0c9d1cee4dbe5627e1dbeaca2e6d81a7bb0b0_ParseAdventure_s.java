 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package tools;
 
 import beans.MonsterBean;
 import java.util.Date;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  *
  * @author xbmc
  */
 public class ParseAdventure {
         
     public static String processAdventure(String response){
         
         Pattern pattern = Pattern.compile("<span.+span>");
         Matcher matcher = pattern.matcher(response);
         String monsterString="";
         if(matcher.find()){
             monsterString=matcher.group();
         }
         
         pattern = Pattern.compile("<img.+<span");
         matcher = pattern.matcher(response);
         String monsterPic="";
         if(matcher.find()){
             String potential=matcher.group();
             pattern=Pattern.compile("<img.+gif");
             matcher = pattern.matcher(potential);
             if(matcher.find()){
                 monsterPic=matcher.group()+"\"/>";    
             }
         }
         
         String body="";
         if(response.contains("You win the fight!") 
                 || response.contains("> Adventure Again ")
                 || response.contains(">Go back to ")){
             pattern = Pattern.compile("<body.*body>");
             matcher = pattern.matcher(response);
             if(matcher.find()){
                 String match = matcher.group();
                 pattern = Pattern.compile("<script.+script>");
                 matcher = pattern.matcher(match);
                 if(matcher.find()){
                     match=match.replace(matcher.group(), "");    
                 }
                 match=match.replace("body", "div")
                         .replace("border: 1px solid blue;", "")
                         .replace("bgcolor=blue", "")
                         .replace("<b>Combat!</b>", "")
                         .replace("<b>Adventure Again:</b>", "");
                 return "<center>"+match+"</center>";
             } else{
                 return 
                     "<center>"
                     + "<div>What are you doing?</div>"
                     + "<div><a href='main.php'>Go Back to Main.</div>"
                     + "</center>";
             }
         } else if (response.contains("You lose. You slink away, dejected and defeated.")){
             return 
                 "<center>"
                 + "<div>You lose. You slink away, dejected and defeated.</div>"
                 + "<div><a href='main.php'>Go Back to Main.</div>"
                 + "</center>";
         } else if (!response.contains("fight.php") && !response.contains("adventure.php")){
             if(response.contains("<img") && response.contains("form>")){
                 pattern = Pattern.compile("<img.+form>");
                 matcher = pattern.matcher(response);
                 if(matcher.find()){
                     return "<center>"+matcher.group()+"</center>";
                 }
             } else{
                 return 
                     "<center>"
                     + "<div>What are you doing?</div>"
                     + "<div><a href='main.php'>Go Back to Main.</div>"
                     + "</center>";
             }
         } else{
             MonsterBean monster = MonsterBean.getMonsterFromString(monsterString);
             String monsterStats="";
             String drops="";
 
             if(monster!=null){
                 monsterStats="<div>"+monster.getAttributes()+"</div>";
                 if(monster.getDrops()!=null && !monster.getDrops().isEmpty()){
                     for(String drop : monster.getDrops()){
                         drops+="<div>"+drop+"</div>";
                     }
                 }
             }
             body=monsterStats+drops+"<br/><a href='fight.php?action=attack&timestamp="+(new Date()).getTime()+"'>ATTACK!</a>";
         }
         
         String display = 
                 "<center>"
                 + monsterPic
                 + "<div><b>You're fighting "+monsterString+"</b></div>"
                 + "<br/>"
                 + body
                 + "</center>";
         
         return display;
     }
 }
