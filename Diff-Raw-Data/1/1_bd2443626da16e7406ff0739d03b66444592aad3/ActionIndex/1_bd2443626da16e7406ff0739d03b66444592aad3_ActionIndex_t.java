 /**
  * 
  */
 package org.powertac.tourney.actions;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.context.FacesContext;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletResponse;
 
 import org.powertac.tourney.beans.Broker;
 import org.powertac.tourney.beans.Game;
 import org.powertac.tourney.services.Database;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 /**
  * @author constantine
  * 
  */
 
 @Component("actionIndex")
 @Scope("request")
 public class ActionIndex
 {
 
   private String sortColumn = null;
   private boolean sortAscending = true;
   private int rowCount = 5;
 
   public List<Game> getGameList ()
   {
     List<Game> games = new ArrayList<Game>();
 
     Database db = new Database();
     try {
       db.startTrans();
       games = db.getGames();
       db.commitTrans();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
     }
 
     return games;
   }
   public void getDownload(Game g){
     ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
     HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
     //response.setContentType("application/force-download");
     response.setContentType("application/x-tar");
     String downloadFile = "game-"+g.getGameId()+"-sim-logs.tar.gz";
     response.addHeader("Content-Disposition", "attachment; filename=\"" + downloadFile + "\"");
     byte[] buf = new byte[1024];
     try{
       String realPath = "/project/msse01/powertac/game-logs/" + downloadFile;//context.getRealPath("/resources/" + downloadFile);
       File file = new File(realPath);
       long length = file.length();
       BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
       ServletOutputStream out = response.getOutputStream();
       response.setContentLength((int)length);
       while ((in != null) && ((length = in.read(buf)) != -1)) {
         out.write(buf, 0, (int)length);
       }
       in.close();
       out.close();
     }catch (Exception exc){
       exc.printStackTrace();
     } 
   }
 
   public List<Game> getGameCompleteList ()
   {
     List<Game> games = new ArrayList<Game>();
 
     Database db = new Database();
     try {
       db.startTrans();
       games = db.getCompleteGames();
       db.commitTrans();
     }
     catch (SQLException e) {
       db.abortTrans();
       e.printStackTrace();
     }
 
     return games;
   }
 
   public String getBrokersInGame (Game g)
   {
     List<Broker> brokersRegistered = new ArrayList<Broker>();
 
     Database db = new Database();
 
     try {
       db.startTrans();
       brokersRegistered = db.getBrokersInGame(g.getGameId());
       db.commitTrans();
     }
     catch (Exception e) {
       db.abortTrans();
       e.printStackTrace();
     }
 
     String result = "";
 
     for (Broker b: brokersRegistered) {
       result += b.getBrokerName() + "\n";
     }
 
     return result;
 
   }
 
   public String getTournamentNameByGame (Game g)
   {
     String result = "";
 
     Database db = new Database();
 
     try {
       db.startTrans();
       result = db.getTournamentByGameId(g.getGameId()).getTournamentName();
       db.commitTrans();
     }
     catch (Exception e) {
       db.abortTrans();
       e.printStackTrace();
     }
 
     return result;
   }
 
   public String getSortColumn ()
   {
     return sortColumn;
   }
 
   public void setSortColumn (String sortColumn)
   {
     this.sortColumn = sortColumn;
   }
 
   public boolean isSortAscending ()
   {
     return sortAscending;
   }
 
   public void setSortAscending (boolean sortAscending)
   {
     this.sortAscending = sortAscending;
   }
 
   public int getRowCount ()
   {
     return rowCount;
   }
 
   public void setRowCount (int rowCount)
   {
     this.rowCount = rowCount;
   }
 
 }
