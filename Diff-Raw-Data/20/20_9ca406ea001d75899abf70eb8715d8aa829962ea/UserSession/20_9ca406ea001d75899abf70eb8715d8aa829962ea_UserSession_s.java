 package org.smartreaction.boardgamegeek.view;
 
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 import org.omnifaces.util.Faces;
 import org.smartreaction.boardgamegeek.BoardGameGeekConstants;
 import org.smartreaction.boardgamegeek.business.BoardGameUtil;
 import org.smartreaction.boardgamegeek.db.dao.GameDao;
 import org.smartreaction.boardgamegeek.db.dao.UserDao;
 import org.smartreaction.boardgamegeek.db.entities.Game;
 import org.smartreaction.boardgamegeek.db.entities.User;
 import org.smartreaction.boardgamegeek.db.entities.UserGame;
 import org.smartreaction.boardgamegeek.services.BoardGameGeekService;
 import org.smartreaction.boardgamegeek.util.UAgentInfo;
 import org.smartreaction.boardgamegeek.xml.plays.Play;
 
 import javax.ejb.EJB;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.ws.rs.core.Cookie;
 import javax.ws.rs.core.MultivaluedMap;
 import javax.ws.rs.core.NewCookie;
 import javax.xml.bind.JAXBException;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 @ManagedBean
 @SessionScoped
 public class UserSession implements Serializable
 {
     @ManagedProperty(value="#{boardGameGeekClient}")
     BoardGameGeekClient boardGameGeekClient;
 
     @EJB
     UserDao userDao;
 
     @EJB
     GameDao gameDao;
 
     @EJB
     BoardGameUtil boardGameUtil;
 
     @EJB
     BoardGameGeekService boardGameGeekService;
 
     private boolean usernameSet;
     private boolean loggedIn;
     private boolean mobile;
     private boolean mobileDetected;
 
     private User user;
     private List<Game> games;
     private List<Game> gamesWithoutExpansions;
     private List<Game> filteredGames;
     private Map<Long, UserGame> userGamesMap;
     private boolean collectionLoaded;
     private boolean forceSync;
 
     private String username;
     private String password;
 
     private List<Cookie> cookies;
 
     private boolean hideCollectionExpansions;
     private boolean showLastPlayed;
     private boolean loadedLastPlayed;
 
     public String login() throws MalformedURLException, JAXBException
     {
         loadUser();
         usernameSet = true;
         if (Faces.getSession().getAttribute("redirectPage") != null) {
             return Faces.getSession().getAttribute("redirectPage") + "?faces-redirect=true";
         }
         return "index.xhtml?faces-redirect=true";
     }
 
     public String loginWithPassword() throws MalformedURLException, JAXBException
     {
         loadUser();
         loginToBoardGameGeek();
         if (loggedIn) {
             if (Faces.getSession().getAttribute("redirectPage") != null) {
                 return Faces.getSession().getAttribute("redirectPage") + "?faces-redirect=true";
             }
             return "index.xhtml?faces-redirect=true";
         }
         return "login.xhtml?faces-redirect=true";
     }
 
     public void refreshCollection()
     {
         collectionLoaded = false;
         forceSync = true;
     }
 
     public void syncGames()
     {
         try {
             if (userGamesMap == null || forceSync) {
                 if (!user.isCollectionLoaded()) {
                     if (user.isTopGamesLoaded()) {
                         gameDao.deleteUserGames(user.getId());
                         user.setTopGamesLoaded(false);
                     }
                     userGamesMap = new HashMap<>(0);
                     boardGameUtil.syncUserGames(user.getId(), user.getUsername(), userGamesMap);
                     userGamesMap = gameDao.getUserGamesMap(user.getId());
                 }
                 else {
                     userGamesMap = gameDao.getUserGamesMap(user.getId());
                     if (forceSync) {
                         forceSync = false;
                         boardGameUtil.syncUserGames(user.getId(), user.getUsername(), userGamesMap);
                     }
                     else {
                         boardGameUtil.syncUserGamesAsynchronous(user.getId(), user.getUsername(), userGamesMap);
                     }
                 }
             }
             games = gameDao.getGames(user.getId());
             gamesWithoutExpansions = new ArrayList<>();
             for (Game game : games) {
                 if (!game.isExpansion()) {
                     gamesWithoutExpansions.add(game);
                 }
             }
 
             if(!user.isCollectionLoaded()) {
                 user.setCollectionLoaded(true);
                 userDao.saveUser(user);
             }
             collectionLoaded = true;
             loadedLastPlayed = false;
             showLastPlayed = false;
         }
         catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     private void loadUser() throws MalformedURLException, JAXBException
     {
         user = userDao.getUser(username);
         if (user == null) {
             user = new User();
             user.setUsername(username);
             user.setLogins(1);
             user.setCreatedDate(new Date());
             user.setLastLogin(new Date());
             userDao.createUser(user);
         }
         else {
             user.setLogins(user.getLogins() + 1);
             user.setLastLogin(new Date());
             userDao.saveUser(user);
         }
     }
 
     public String logout() throws ServletException
     {
         if (loggedIn) {
             removeCookies();
 
             WebResource webResource = boardGameGeekClient.getClient().resource(BoardGameGeekConstants.BBG_WEBSITE + "/logout");
 
             ClientResponse clientResponse = webResource.get(ClientResponse.class);
             if (clientResponse.getStatus() == 200) {
                 loggedIn = false;
             }
         }
         usernameSet = false;
         Faces.invalidateSession();
         return "login.xhtml?faces-redirect=true";
     }
 
     private void removeCookies()
     {
         javax.servlet.http.Cookie[] httpCookies = Faces.getRequest().getCookies();
 
         if (cookies != null) {
             for (javax.servlet.http.Cookie httpCookie : httpCookies) {
                 httpCookie.setValue(null);
                 httpCookie.setMaxAge(0);
                 Faces.getResponse().addCookie(httpCookie);
             }
             cookies.clear();
         }
     }
 
     private void loginToBoardGameGeek()
     {
         if (!loggedIn) {
             WebResource webResource = boardGameGeekClient.getClient().resource(BoardGameGeekConstants.BBG_WEBSITE + "/login");
 
             MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
             queryParams.add("username", username);
             queryParams.add("password", password);
 
             ClientResponse response = webResource.queryParams(queryParams).post(ClientResponse.class);
 
             int status = response.getStatus();
             if (status == 200) {
                 cookies = new ArrayList<>();
                 for (NewCookie newCookie : response.getCookies()) {
                     Cookie cookie = new Cookie(newCookie.getName(), newCookie.getValue(), "/", "boardgamegeek.com");
                     cookies.add(cookie);
 
                    FacesContext.getCurrentInstance()
                            .getExternalContext()
                            .addResponseCookie(newCookie.getName(), newCookie.getValue(), null);
                 }
                 usernameSet = true;
                 loggedIn = true;
             }
         }
     }
 
     public synchronized void loginFromCookies(javax.servlet.http.Cookie[] httpCookies)
     {
         try {
             if (!usernameSet) {
                 cookies = new ArrayList<>();
                 for (javax.servlet.http.Cookie httpCookie : httpCookies) {
                     if (httpCookie.getName().equalsIgnoreCase("bggusername")) {
                         username = httpCookie.getValue();
                     }
                     Cookie cookie = new Cookie(httpCookie.getName(), httpCookie.getValue(), "/", "boardgamegeek.com");
                     cookies.add(cookie);
                 }
 
                 loadUser();
 
                 usernameSet = true;
                 loggedIn = true;
             }
         }
         catch (MalformedURLException | JAXBException e) {
             e.printStackTrace();
         }
     }
 
     public void loadLastPlayed()
     {
         if (!loadedLastPlayed) {
             for (Game game : games) {
                 try {
                     List<Play> plays = boardGameGeekService.getPlays(username, game.getId());
                     if (!plays.isEmpty()) {
                         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                         game.setLastPlayed(simpleDateFormat.parse(plays.get(0).getDate()));
                     }
                 }
                 catch (Exception e) {
                     e.printStackTrace();
                 }
             }
             loadedLastPlayed = true;
         }
     }
 
     public void updateUserGame(UserGame userGame)
     {
         gameDao.updateUserGame(userGame);
     }
 
     public void switchSite()
     {
         mobile = !mobile;
     }
 
     public boolean isUsernameSet()
     {
         return usernameSet;
     }
 
     public boolean isLoggedIn()
     {
         return loggedIn;
     }
 
     public List<Cookie> getCookies()
     {
         return cookies;
     }
 
     public String getUsername()
     {
         return username;
     }
 
     public void setUsername(String username)
     {
         this.username = username;
     }
 
     public String getPassword()
     {
         return password;
     }
 
     public void setPassword(String password)
     {
         this.password = password;
     }
 
     public boolean isMobile()
     {
         if (!mobileDetected) {
             detectMobile();
         }
         return mobile;
     }
 
     private void detectMobile()
     {
         HttpServletRequest request = Faces.getRequest();
         UAgentInfo agentInfo = new UAgentInfo(request.getHeader("User-Agent"), request.getHeader("Accept"));
         mobile = agentInfo.detectSmartphone();
         mobileDetected = true;
     }
 
     public User getUser()
     {
         return user;
     }
 
     public List<Game> getGames()
     {
         return games;
     }
 
     public List<Game> getCollectionGames()
     {
         if (hideCollectionExpansions) {
             return gamesWithoutExpansions;
         }
         else {
             return games;
         }
     }
 
     public boolean hasGames()
     {
         return !games.isEmpty();
     }
 
     public Map<Long, UserGame> getUserGamesMap()
     {
         return userGamesMap;
     }
 
     public boolean isCollectionLoaded()
     {
         return collectionLoaded;
     }
 
     public List<Game> getFilteredGames()
     {
         return filteredGames;
     }
 
     public void setFilteredGames(List<Game> filteredGames)
     {
         this.filteredGames = filteredGames;
     }
 
     public boolean isHideCollectionExpansions()
     {
         return hideCollectionExpansions;
     }
 
     public void setHideCollectionExpansions(boolean hideCollectionExpansions)
     {
         this.hideCollectionExpansions = hideCollectionExpansions;
     }
 
     public List<Game> getGamesWithoutExpansions()
     {
         return gamesWithoutExpansions;
     }
 
     public boolean isShowLastPlayed()
     {
         return showLastPlayed;
     }
 
     public void setShowLastPlayed(boolean showLastPlayed)
     {
         this.showLastPlayed = showLastPlayed;
     }
 
     @SuppressWarnings("UnusedDeclaration")
     public void setBoardGameGeekClient(BoardGameGeekClient boardGameGeekClient)
     {
         this.boardGameGeekClient = boardGameGeekClient;
     }
 }
