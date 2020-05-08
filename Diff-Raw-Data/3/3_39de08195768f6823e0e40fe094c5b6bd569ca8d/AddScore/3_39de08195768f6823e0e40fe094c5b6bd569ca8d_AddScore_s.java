 package com.neogeohiscores.web.pages;
 
 import java.io.InputStream;
 import java.net.URL;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.tapestry5.annotations.DiscardAfter;
 import org.apache.tapestry5.annotations.InjectComponent;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.beaneditor.Validate;
 import org.apache.tapestry5.corelib.components.Form;
 import org.apache.tapestry5.corelib.components.Zone;
 import org.apache.tapestry5.hibernate.annotations.CommitAfter;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.services.Request;
 
 import com.google.common.io.ByteStreams;
 import com.neogeohiscores.common.Levels;
 import com.neogeohiscores.common.clients.AuthenticationFailed;
 import com.neogeohiscores.common.clients.Messages;
 import com.neogeohiscores.common.clients.NeoGeoFansClient;
 import com.neogeohiscores.common.clients.NeoGeoFansClientFactory;
 import com.neogeohiscores.common.imagefetcher.DirectLinkNotFoundException;
 import com.neogeohiscores.common.imagefetcher.ImageFetcher;
 import com.neogeohiscores.entities.Game;
 import com.neogeohiscores.entities.Player;
 import com.neogeohiscores.entities.Score;
 import com.neogeohiscores.web.services.GameService;
 import com.neogeohiscores.web.services.PlayerService;
 import com.neogeohiscores.web.services.ScoreService;
 import com.neogeohiscores.web.services.TitleRelockingService;
 import com.neogeohiscores.web.services.TitleUnlockingService;
 
 public class AddScore {
 
     private static final String ALL_CLEAR = "ALL CLEAR";
     public static final String DEFAULT_GAME = "3 Count Bout (3 minutes)";
     public static final long DEFAULT_POST_ID = 41930;
     public static final Logger LOG = Logger.getLogger(AddScore.class.getName());
 
     @Inject
     private ScoreService scoreService;
 
     @Inject
     private GameService gameService;
 
     @Inject
     private PlayerService playerService;
 
     @Inject
     private TitleUnlockingService titleUnlockingService;
 
     @Inject
     private TitleRelockingService titleRelockingService;
 
     @Property
     private Game game;
 
     @Property
     @Validate("required")
     private String pictureUrl;
 
     @Property
     @Validate("required")
     private String level;
 
     @Property
     @Validate("required")
     private String score;
 
     @Property
     private String stage;
 
     @Property
     private boolean allClear;
 
     @Property
     private String message;
 
     @Property
     private boolean postOnNgf;
 
     @Property
     @Validate("required")
     private String fullname;
 
     @Property
     @Validate("required")
     private String password;
 
     private static final int MAX_MESSAGE_LENGTH = 255;
 
     private NeoGeoFansClientFactory neoGeoFansClientFactory = new NeoGeoFansClientFactory();
 
     private ImageFetcher imageFetcher = new ImageFetcher();
 
     @InjectComponent
     private Form form;
 
     @InjectComponent
     private Zone imageZone;
 
     @Inject
     private Request request;
 
     @Persist
     private Score currentScore;
 
     void onActivate() {
         level = "MVS";
         game = gameService.findByName(DEFAULT_GAME);
     }
 
     void onActivate(Score score) {
         this.score = score.getValue();
         this.allClear = score.getAllClear();
         this.fullname = score.getPlayerName();
         this.game = score.getGame();
         this.level = score.getLevel();
         this.message = score.getMessage();
         this.stage = score.getStage();
         this.pictureUrl = score.getPictureUrl();
         this.currentScore = score;
     }
 
     public List<Game> getGames() {
         return gameService.findAll();
     }
 
     public List<String> getLevels() {
         return Levels.list();
     }
 
     public Object onPictureUrlChanged() {
         pictureUrl = request.getParameter("param");
         return request.isXHR() ? imageZone.getBody() : null;
     }
 
     @CommitAfter
     @DiscardAfter
     public Object onSuccess() {
         try {
             NeoGeoFansClient ngfClient = neoGeoFansClientFactory.create();
             boolean isAuthenticated = ngfClient.authenticate(fullname, password);
             if (isAuthenticated) {
                 acceptScore(ngfClient);
                 return Index.class;
             } else {
                 form.recordError("Your NGF account is invalid");
                 return this;
             }
         } catch (AuthenticationFailed ex) {
             Logger.getLogger(AddScore.class.getName()).log(Level.SEVERE, null, ex);
             form.recordError(ex.getMessage());
             return this;
         }
     }
 
     private void acceptScore(NeoGeoFansClient ngfClient) {
         Player player = playerService.findByFullname(fullname);
         if (player == null) {
             player = playerService.store(new Player(fullname));
         }
         try {
             pictureUrl = imageFetcher.get(content(pictureUrl));
         } catch (DirectLinkNotFoundException e) {
             LOG.severe(e.getMessage());
         }
         Score scoreToStore = storeScore(ngfClient, player);
         titleUnlockingService.searchUnlockedTitlesFor(player);
         titleRelockingService.relockTitles(scoreToStore);
     }
 
     private Score storeScore(NeoGeoFansClient ngfClient, Player player) {
         Score scoreToStore;
         if(currentScore == null) {
             scoreToStore = new Score(score, player, level, game, pictureUrl);
         } else {
             scoreToStore = currentScore;
         }
         scoreToStore.setAllClear(allClear || ALL_CLEAR.equals(stage));
         scoreToStore.setStage(stage);
         int end = message.length() > MAX_MESSAGE_LENGTH ? MAX_MESSAGE_LENGTH : message.length();
         scoreToStore.setMessage(message.substring(0, end));
         scoreToStore = scoreService.store(scoreToStore);
         postOnNgf(scoreToStore, game, ngfClient);
         return scoreToStore;
     }
 
     private String content(String pictureUrl) {
         try {
             InputStream stream = new URL(pictureUrl).openStream();
             byte[] bytes = ByteStreams.toByteArray(stream);
             String pageContent = new String(bytes);
             return pageContent;
         } catch (Exception e) {
             LOG.severe(e.getMessage());
         }
         return "";
     }
 
     private void postOnNgf(Score score, Game game, NeoGeoFansClient ngfClient) {
         if (postOnNgf) {
             if ("MVS".equals(level)) {
                 Long postId = game.getPostId();
                 if (postId == null) {
                     postId = 41930L;
                 }
                 ngfClient.post(Messages.toMessage(score), postId);
             }
         }
     }
 
 }
