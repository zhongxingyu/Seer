 package com.punmac.footballmatchup.webapp.controller;
 
 import com.punmac.footballmatchup.config.FootballMatchUpProperties;
 import com.punmac.footballmatchup.core.dao.MatchDao;
 import com.punmac.footballmatchup.core.dao.PlayerMatchDao;
 import com.punmac.footballmatchup.core.dao.PlayerRatingDao;
 import com.punmac.footballmatchup.core.model.Match;
 import com.punmac.footballmatchup.core.model.Player;
 import com.punmac.footballmatchup.core.model.PlayerMatch;
 import com.punmac.footballmatchup.core.model.PlayerRating;
 import com.punmac.footballmatchup.core.service.RatingService;
 import com.punmac.footballmatchup.webapp.bean.display.JoinedPlayerDisplay;
 import com.punmac.footballmatchup.webapp.bean.display.MatchCardDisplay;
 import com.punmac.footballmatchup.webapp.bean.form.MatchSearchForm;
 import com.punmac.footballmatchup.webapp.search.MatchSearch;
 import com.punmac.footballmatchup.webapp.typeeditor.DateTimeTypeEditor;
 import com.punmac.footballmatchup.webapp.util.CookieSessionUtil;
 import com.punmac.footballmatchup.webapp.validator.SaveMatchValidator;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.ArrayList;
 import java.util.List;
 
 @Controller
 @RequestMapping(value = "match")
 public class MatchController {
 
     private static final Logger log = LoggerFactory.getLogger(MatchController.class);
 
     @Autowired
     private MatchDao matchDao;
 
     @Autowired
     private MatchSearch matchSearch;
 
     @Autowired
     private PlayerMatchDao playerMatchDao;
 
     @Autowired
     private PlayerRatingDao playerRatingDao;
 
     @Autowired
     private DateTimeTypeEditor dateTimeTypeEditor;
 
     @Autowired
     private SaveMatchValidator saveMatchValidator;
 
     @Autowired
     private FootballMatchUpProperties footballMatchUpProperties;
 
     @Autowired
     private RatingService ratingService;
 
     @RequestMapping(value = {"/", "home"})
     public String home(Model model, HttpServletRequest request) {
         model.addAttribute("pageContent", "match/home");
         loadMatch(model, request);
         return "layout";
     }
 
     @RequestMapping(value = "info/{matchId}")
     public String info(Model model, HttpServletRequest request, @PathVariable(value = "matchId") String matchId) {
         // Check whether loggedInPlayer already join this match or not.
         Player loggedInPlayer = CookieSessionUtil.getLoggedInPlayer(request);
         Match match = matchDao.findById(matchId);
         if (loggedInPlayer != null) {
             PlayerMatch playerMatch = playerMatchDao.findByPlayerIdAndMatchId(loggedInPlayer.getId(), matchId);
             log.debug("PlayerMatch : {}", playerMatch);
             if(playerMatch != null) { // Player already join this match.
                 log.debug("Player (username = {}) already join this match (id = {})", loggedInPlayer.getUsername(), matchId);
                 model.addAttribute("playerMatch", playerMatch);
             }
         }
         if (match.getPlayTime().isBeforeNow()) {
             model.addAttribute("past", true);
         }
 
         // Find player who joined this match.
         List<PlayerMatch> playerMatchList = playerMatchDao.findAllPlayerInMatch(matchId);
         // Generate display bean.
         List<JoinedPlayerDisplay> joinedPlayerDisplayList = new ArrayList<>();
         for(PlayerMatch pm : playerMatchList) {
             JoinedPlayerDisplay joinedPlayerDisplay = new JoinedPlayerDisplay();
             joinedPlayerDisplay.setPlayer(pm.getPlayer());
             joinedPlayerDisplay.setMatch(pm.getMatch());
             // If logged in, player can see their own rating.
             if(loggedInPlayer != null) {
                 PlayerRating playerRating = playerRatingDao.findByPlayerIdAndMatchIdAndRaterId(pm.getPlayer().getId(),
                         pm.getMatch().getId(), loggedInPlayer.getId());
                 joinedPlayerDisplay.setPlayerRating(playerRating);
             }
             joinedPlayerDisplayList.add(joinedPlayerDisplay);
         }
         model.addAttribute("match", match);
         model.addAttribute("joinedPlayerDisplayList", joinedPlayerDisplayList);
         model.addAttribute("pageTitle", match.getName());
         model.addAttribute("pageContent", "match/info");
         return "layout";
     }
 
     @RequestMapping(value = "join/{matchId}")
     public String join(Model model, HttpServletRequest request, @PathVariable(value = "matchId") String matchId) {
         // Init PlayerMatch in here because don't want user to see playerId and matchId in hidden field.
         Player player = CookieSessionUtil.getLoggedInPlayer(request);
         if (player == null) {
             model.addAttribute("alert", "<strong>Warning!</strong> You need to sign in to create a match");
             model.addAttribute("alertCss", "alert alert-warning");
             return "forward:/login";
         }
         Match match = new Match();
         match.setId(matchId); // Set Id only because Id is reference to playerMatch.player.
         if (playerMatchDao.findByPlayerIdAndMatchId(player.getId(),match.getId()) != null) {
             model.addAttribute("alert", "<strong>Warning!</strong> You already joined the match");
             model.addAttribute("alertCss", "alert alert-warning");
             model.addAttribute("pageContent", "match/home");
             return "forward:/match/info/" + matchId;
         }
         PlayerMatch playerMatch = new PlayerMatch();
         playerMatch.setPlayer(player);
         playerMatch.setMatch(match);
         log.debug("PlayerMatch : {}", playerMatch);
         playerMatchDao.save(playerMatch);
         model.addAttribute("alert", "<strong>Success!</strong> You've joined the match");
         model.addAttribute("alertCss", "alert alert-success");
         model.addAttribute("pageContent", "match/home");
         return "forward:/match/info/" + matchId;
     }
 
     @RequestMapping(value = "create")
     public String create(Model model, HttpServletRequest request, @ModelAttribute Match match,
                          BindingResult bindingResult) {
         Player player = CookieSessionUtil.getLoggedInPlayer(request);
         if (player == null) {
             model.addAttribute("alert", "<strong>Warning!</strong> You need to sign in to create a match");
             model.addAttribute("alertCss", "alert alert-warning");
             return "forward:/login";
         }
         if(RequestMethod.POST.toString().equals(request.getMethod())) {
             // Set creator as loggedIn player
             match.setCreator(CookieSessionUtil.getLoggedInPlayer(request));
             log.debug("Match : {}", match.toString());
             saveMatchValidator.validate(match, bindingResult);
             if(!bindingResult.hasErrors()) {
                 matchDao.save(match);
                 model.addAttribute("alert", "<strong>Success!</strong> Match created");
                 model.addAttribute("alertCss", "alert alert-success");
                return "forward:/match/";
             }
         } else {
             match.setPlayTime(DateTime.now());
         }
         model.addAttribute("btnSubmitValue", "Create");
         model.addAttribute("pageTitle", "Create Match");
         model.addAttribute("pageContent", "match/save");
         return "layout";
     }
 
     @RequestMapping(value = "edit/{matchId}")
     public String edit(Model model, HttpServletRequest request, @PathVariable(value = "matchId") String matchId,
                        @ModelAttribute Match match, BindingResult bindingResult) {
         if(RequestMethod.POST.toString().equals(request.getMethod())) {
             log.debug("Match : {}", match.toString());
             saveMatchValidator.validate(match, bindingResult);
             if(!bindingResult.hasErrors()) {
                 matchDao.save(match);
                 model.addAttribute("alert", "<strong>Success!</strong> Match edited");
                 model.addAttribute("alertCss", "alert alert-success");
                 return "forward:/match/info/" + matchId;
             }
         } else {
             match = matchDao.findById(matchId);
             model.addAttribute("match", match);
         }
         model.addAttribute("btnSubmitValue", "Save");
         model.addAttribute("pageTitle", "Edit Match");
         model.addAttribute("pageContent", "match/save");
         return "layout";
     }
 
     /**
      * This method will be use in match/home page.
      * When click on "Load More", Request will be send to this method to get more match and display in page.
      */
     @RequestMapping(value = "rest/include/loadmore", method = RequestMethod.POST)
     public String restIncludeLoadMore(Model model, @RequestParam int start) {
         MatchSearchForm matchSearchForm = new MatchSearchForm();
         matchSearchForm.setStart(start);
         List<Match> matchList = matchSearch.searchMatch(matchSearchForm);
         model.addAttribute("matchList", matchList);
         return "match/include/home_loadmore";
     }
 
     /**
      * This method will be use in match/info page.
      * When give rating, Request will be send to this method to give rating score to player.
      */
     @RequestMapping(value = "rest/giverating", method = RequestMethod.POST)
     public @ResponseBody PlayerRating restGiveRating(HttpServletRequest request, @RequestParam int score,
                                  @RequestParam String playerRatingId, @RequestParam String playerId,
                                  @RequestParam String matchId) {
         Player player = new Player();
         player.setId(playerId);
         Match match = new Match();
         match.setId(matchId);
         PlayerRating playerRating = new PlayerRating();
         if(!"".equals(playerRatingId)) { // When edit rating, playerRatingId will not be "".
             playerRating.setId(playerRatingId);
         }
         playerRating.setRating(score);
         playerRating.setPlayer(player);
         playerRating.setMatch(match);
         playerRating.setRater(CookieSessionUtil.getLoggedInPlayer(request));
         log.debug("PlayerRating : {}", playerRating);
         playerRatingDao.save(playerRating);
         return playerRating;
     }
 
     @InitBinder
     public void binder(WebDataBinder binder) {
         binder.registerCustomEditor(DateTime.class, dateTimeTypeEditor);
     }
 
     private void loadMatch(Model model, HttpServletRequest request) {
         List<Match> matchList = matchDao.findAll();
         List<MatchCardDisplay> matchCardDisplayList = new ArrayList<>();
         for (Match match : matchList) {
             MatchCardDisplay matchCardDisplay = new MatchCardDisplay();
             matchCardDisplay.setPlayerCount(playerMatchDao.findAllPlayerInMatch(match.getId()).size());
             matchCardDisplay.setMatch(match);
             Player loggedInPlayer = CookieSessionUtil.getLoggedInPlayer(request);
             if (loggedInPlayer == null) { // Player not login.
                 matchCardDisplay.setCardColor("matchcard-needlogin");
                 matchCardDisplay.setButtonName("Please sign-in");
                 matchCardDisplay.setButtonLink("login/");
             } else if (match.getPlayTime().isAfterNow()) { // Player logged in and playtime is not start,
                 matchCardDisplay.setCardColor("matchcard-future");
                 matchCardDisplay.setButtonName("Join");
                 matchCardDisplay.setButtonLink("match/join/" + match.getId());
             } else if (match.getPlayTime().plusHours(1).isBeforeNow()) { // Player logged in and playtime is end.
                 matchCardDisplay.setCardColor("matchcard-past");
                 matchCardDisplay.setButtonName("Rate");
                 matchCardDisplay.setButtonLink("match/info/" + match.getId());
             }
             matchCardDisplayList.add(matchCardDisplay);
         }
         model.addAttribute("matchCardDisplayList", matchCardDisplayList);
     }
 }
