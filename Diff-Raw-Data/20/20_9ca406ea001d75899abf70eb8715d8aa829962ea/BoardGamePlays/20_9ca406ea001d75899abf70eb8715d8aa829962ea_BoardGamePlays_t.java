 package org.smartreaction.boardgamegeek.view;
 
 import org.omnifaces.util.Faces;
 import org.smartreaction.boardgamegeek.services.BoardGameGeekService;
 import org.smartreaction.boardgamegeek.xml.plays.Play;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.ViewScoped;
 import javax.xml.bind.JAXBException;
 import java.net.MalformedURLException;
 import java.util.List;
 
 @ManagedBean
 @ViewScoped
 public class BoardGamePlays
 {
     @ManagedProperty(value="#{userSession}")
     UserSession userSession;
 
     @EJB
     BoardGameGeekService boardGameGeekService;
 
     private List<Play> plays;
 
     private boolean loaded;
 
     private long gameId;
 
     private boolean showingPlaysForGame;
 
     @PostConstruct
     public void setup()
     {
         if (Faces.getRequestParameter("gameId") != null) {
             gameId = Long.parseLong(Faces.getRequestParameter("gameId"));
             showingPlaysForGame = true;
         }
     }
 
     public void loadPlays() throws JAXBException, MalformedURLException
     {
         if (userSession.getGames() == null) {
             userSession.syncGames();
         }
 
         if (showingPlaysForGame) {
            plays = boardGameGeekService.getPlays(userSession.getUsername(), gameId);
         }
         else {
            plays = boardGameGeekService.getPlays(userSession.getUsername());
         }
 
         loaded = true;
     }
 
     public List<Play> getPlays()
     {
         return plays;
     }
 
     public boolean isLoaded()
     {
         return loaded;
     }
 
     public long getGameId()
     {
         return gameId;
     }
 
     public boolean isShowingPlaysForGame()
     {
         return showingPlaysForGame;
     }
 
     @SuppressWarnings("UnusedDeclaration")
     public void setUserSession(UserSession userSession)
     {
         this.userSession = userSession;
     }
 }
