 package com.punmac.footballmatchup.webapp.controller;
 
 import com.punmac.footballmatchup.config.FootballMatchUpProperties;
 import com.punmac.footballmatchup.core.dao.MatchDao;
 import com.punmac.footballmatchup.core.dao.PlayerMatchDao;
 import com.punmac.footballmatchup.core.dao.PlayerRatingDao;
 import com.punmac.footballmatchup.core.model.Match;
 import com.punmac.footballmatchup.core.model.Player;
 import com.punmac.footballmatchup.core.model.PlayerMatch;
 import com.punmac.footballmatchup.core.model.PlayerRating;
 import com.punmac.footballmatchup.core.service.TeamMatchingService;
 import com.punmac.footballmatchup.webapp.bean.display.JoinedPlayerDisplay;
 import com.punmac.footballmatchup.webapp.bean.display.MatchCardDisplay;
 import com.punmac.footballmatchup.webapp.bean.display.PlayerMatchDisplay;
 import com.punmac.footballmatchup.webapp.bean.form.search.MatchSearchForm;
 import com.punmac.footballmatchup.webapp.search.MatchSearch;
 import com.punmac.footballmatchup.webapp.typeeditor.DateTimeTypeEditor;
 import com.punmac.footballmatchup.webapp.typeeditor.HtmlEscapeEditor;
 import com.punmac.footballmatchup.webapp.util.CookieSessionUtil;
 import com.punmac.footballmatchup.webapp.validator.SaveMatchValidator;
 import org.joda.time.DateTime;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.propertyeditors.StringTrimmerEditor;
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
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 import javax.servlet.http.HttpServletRequest;
 import java.math.BigDecimal;
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
     private TeamMatchingService teamMatchingService;
 
     @RequestMapping(value = {"/", "home"})
     public String home(Model model, HttpServletRequest request) {
         model.addAttribute("pageContent", "match/home");
         loadMatch(model, request);
         return "layout";
     }
 
     @RequestMapping(value = "info/{matchId}")
     public String info(Model model, HttpServletRequest request, @PathVariable String matchId) {
         Player loggedInPlayer = CookieSessionUtil.getLoggedInPlayer(request);
         Match match = matchDao.findById(matchId);
         if (loggedInPlayer != null) {
             // Check whether loggedInPlayer already join this match or not.
             PlayerMatch playerMatch = playerMatchDao.findByPlayerIdAndMatchId(loggedInPlayer.getId(), matchId);
             if(playerMatch != null) { // Player already join this match. Only for disable join button
                 log.debug("Player (username = {}) already join this match (id = {})", loggedInPlayer.getUsername(), matchId);
                 model.addAttribute("playerMatch", playerMatch);
             }
             if (match.getCreator() != null && match.getCreator().getId().equals(loggedInPlayer.getId())) {
                 // for creator tag
                 model.addAttribute("creator", true);
             }
         }
         if (match.getPlayTime().isBeforeNow()) {
             // if match is past then player can not join (disable join button)
             model.addAttribute("past", true);
         }
         // Find player who joined this match.
         List<PlayerMatch> playerMatchList = playerMatchDao.findAllPlayerInMatch(matchId);
         // Generate display bean.
         List<JoinedPlayerDisplay> joinedPlayerDisplayList = new ArrayList<>();
         List<JoinedPlayerDisplay> joinedPlayerTeamADisplayList = new ArrayList<>();
         List<JoinedPlayerDisplay> joinedPlayerTeamBDisplayList = new ArrayList<>();
         for(PlayerMatch pm : playerMatchList) {
             JoinedPlayerDisplay joinedPlayerDisplay = new JoinedPlayerDisplay();
             joinedPlayerDisplay.setPlayer(pm.getPlayer());
             joinedPlayerDisplay.setMatch(pm.getMatch());
             // If logged in, player can see their own rating.
             if(loggedInPlayer != null && pm.getPlayer() != null) {
                 PlayerRating playerRating = playerRatingDao.findByPlayerIdAndMatchIdAndRaterId(pm.getPlayer().getId(),
                         pm.getMatch().getId(), loggedInPlayer.getId());
                 joinedPlayerDisplay.setPlayerRating(playerRating);
             }
             joinedPlayerDisplay.setPlayerMatch(pm);
             if(pm.getTeam() == 0) { // Player have no team
                 joinedPlayerDisplayList.add(joinedPlayerDisplay);
             } else if(pm.getTeam() == 1) { // Player is in team-a
                 joinedPlayerTeamADisplayList.add(joinedPlayerDisplay);
             } else if(pm.getTeam() == 2) { // Player is in team-b
                 joinedPlayerTeamBDisplayList.add(joinedPlayerDisplay);
             }
         }
         model.addAttribute("match", match);
         model.addAttribute("joinedPlayerDisplayList", joinedPlayerDisplayList);
         model.addAttribute("joinedPlayerTeamADisplayList", joinedPlayerTeamADisplayList);
         model.addAttribute("joinedPlayerTeamBDisplayList", joinedPlayerTeamBDisplayList);
         model.addAttribute("pageTitle", match.getName());
         model.addAttribute("pageContent", "match/info");
         return "layout";
     }
 
     @RequestMapping(value = "create")
     public String create(Model model, RedirectAttributes redirectAttributes, HttpServletRequest request,
                          @ModelAttribute Match match, BindingResult bindingResult) {
         Player loggedInPlayer = CookieSessionUtil.getLoggedInPlayer(request);
         if (loggedInPlayer == null) {
             redirectAttributes.addFlashAttribute("alert", "<strong>Warning!</strong> You need to sign in to create a match");
             redirectAttributes.addFlashAttribute("alertCss", "alert alert-warning");
             return "redirect:/login";
         }
         if(RequestMethod.POST.toString().equals(request.getMethod())) {
             log.debug("Creating new Match, Match = {}", match.toString());
             saveMatchValidator.validate(match, bindingResult);
             if(!bindingResult.hasErrors()) {
                 // Set creator as loggedInPlayer
                 match.setCreator(loggedInPlayer);
                 matchDao.save(match);
                 redirectAttributes.addFlashAttribute("alert", "<strong>Success!</strong> Match created");
                 redirectAttributes.addFlashAttribute("alertCss", "alert alert-success");
                 return "redirect:/match/info/" + match.getId();
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
     public String edit(Model model, RedirectAttributes redirectAttributes, HttpServletRequest request,
                        @PathVariable String matchId, @ModelAttribute Match match, BindingResult bindingResult) {
         Player loggedInPlayer = CookieSessionUtil.getLoggedInPlayer(request);
         if (loggedInPlayer == null) {
             redirectAttributes.addFlashAttribute("alert", "<strong>Warning!</strong> You need to sign in to edit this match");
             redirectAttributes.addFlashAttribute("alertCss", "alert alert-warning");
             return "redirect:/login";
         }
         if(RequestMethod.POST.toString().equals(request.getMethod())) {
             log.debug("Editing Match, Match : {}", match.toString());
             saveMatchValidator.validate(match, bindingResult);
             if(!bindingResult.hasErrors()) {
                 matchDao.save(match);
                 redirectAttributes.addFlashAttribute("alert", "<strong>Success!</strong> Match edited");
                 redirectAttributes.addFlashAttribute("alertCss", "alert alert-success");
                 return "redirect:/match/info/" + matchId;
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
 
     @RequestMapping(value = "join/{matchId}")
     public String join(HttpServletRequest request, @PathVariable(value = "matchId") String matchId,
                        RedirectAttributes redirectAttributes) {
         Player loggedInPlayer = CookieSessionUtil.getLoggedInPlayer(request);
         log.debug("Player {} is joining Match {}", loggedInPlayer.getUsername(), matchId);
         if (loggedInPlayer == null) {
             redirectAttributes.addFlashAttribute("alert", "<strong>Warning!</strong> You need to sign in to create a match");
             redirectAttributes.addFlashAttribute("alertCss", "alert alert-warning");
             return "redirect:/login";
         }
         if (playerMatchDao.findByPlayerIdAndMatchId(loggedInPlayer.getId(), matchId) != null) {
             redirectAttributes.addFlashAttribute("alert", "<strong>Warning!</strong> You already joined the match");
             redirectAttributes.addFlashAttribute("alertCss", "alert alert-warning");
             return "redirect:/match/info/" + matchId;
         }
         Match match = new Match();
         match.setId(matchId); // Set Id only because Id is reference to playerMatch.player.
         PlayerMatch playerMatch = new PlayerMatch();
         playerMatch.setPlayer(loggedInPlayer);
         playerMatch.setMatch(match);
         playerMatchDao.save(playerMatch);
         redirectAttributes.addFlashAttribute("alert", "<strong>Success!</strong> You've joined the match");
         redirectAttributes.addFlashAttribute("alertCss", "alert alert-success");
         return "redirect:/match/info/" + matchId;
     }
 
     @RequestMapping(value = "matchup/{matchId}")
     public String matchup(Model model, @PathVariable(value = "matchId") String matchId) {
         teamMatchingService.matchUp(matchId);
         return "forward:/match/info/" + matchId;
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
     public @ResponseBody PlayerRating restGiveRating(HttpServletRequest request,
                                                      @RequestParam int score,
                                                      @RequestParam(required = false) String playerRatingId,
                                                      @RequestParam String playerId,
                                                      @RequestParam String matchId) {
         Player player = new Player();
         player.setId(playerId);
         Match match = new Match();
         match.setId(matchId);
         PlayerRating playerRating = new PlayerRating();
        if(!playerRatingId.equals("0")) { // When edit rating, playerRatingId will not be "".
             playerRating.setId(playerRatingId);
         }
         // Just to make sure score can not be more than 5 and less than 0
         if (score > 5) {
             score = 5;
         } else if (score < 0) {
             score = 0;
         }
         playerRating.setRating(score);
         playerRating.setPlayer(player);
         playerRating.setMatch(match);
         playerRating.setRater(CookieSessionUtil.getLoggedInPlayer(request));
         log.debug("Rating, PlayerRating = {}", playerRating.toString());
         playerRatingDao.save(playerRating);
         return playerRating;
     }
 
     /**
      * This method will be use in match/info page.
      * When user change player team, Request will be send to this method to update player team.
      */
     @RequestMapping(value = "rest/playerchangeteam", method = RequestMethod.POST)
     public @ResponseBody PlayerMatchDisplay restPlayerChangeTeam(@RequestParam String playerId,
                                                                  @RequestParam String matchId,
                                                                  @RequestParam(required = false) String playerMatchId,
                                                                  @RequestParam String team) {
         Match match = matchDao.findById(matchId);
         Player player = new Player();
         player.setId(playerId);
         PlayerMatch playerMatch = new PlayerMatch();
         if(!"".equals(playerMatchId)) { // When edit playerMatch, playerMatchId will not be "".
             playerMatch.setId(playerMatchId);
         }
         int playerTeam = 0;
         if("team-a".equals(team)) {
             playerTeam = 1;
         }
         if("team-b".equals(team)) {
             playerTeam = 2;
         }
         playerMatch.setTeam(playerTeam);
         playerMatch.setMatch(match);
         playerMatch.setPlayer(player);
         log.debug("Changing player team, PlayerMatch = {}", playerMatch);
         playerMatchDao.save(playerMatch);
         PlayerMatchDisplay playerMatchDisplay = new PlayerMatchDisplay();
         playerMatchDisplay.setPlayerMatch(playerMatch);
 
         // Update winning percentage
         match.setTeamAWinning(teamMatchingService.teamWinningPercentage(matchId,1));
         match.setTeamBWinning(teamMatchingService.teamWinningPercentage(matchId,2));
         matchDao.save(match);
 
         return playerMatchDisplay;
     }
 
     private void loadMatch(Model model, HttpServletRequest request) {
         MatchSearchForm matchSearchForm = new MatchSearchForm();
         List<Match> matchList = matchSearch.searchMatch(matchSearchForm);
         model.addAttribute("countMatch", matchSearch.countMatch(matchSearchForm));
         model.addAttribute("loadMoreLimit", footballMatchUpProperties.getPaginationLoadMoreLimit());
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
             } else if (match.getPlayTime().isBeforeNow()) { // Player logged in and playtime is end.
                 matchCardDisplay.setCardColor("matchcard-past");
                 matchCardDisplay.setButtonName("Rate");
                 matchCardDisplay.setButtonLink("match/info/" + match.getId());
             }
             matchCardDisplayList.add(matchCardDisplay);
         }
         model.addAttribute("matchCardDisplayList", matchCardDisplayList);
     }
 
     @InitBinder
     private void binder(WebDataBinder binder) {
         binder.registerCustomEditor(DateTime.class, dateTimeTypeEditor);
         binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
         binder.registerCustomEditor(String.class, "detail", new HtmlEscapeEditor());
     }
 }
