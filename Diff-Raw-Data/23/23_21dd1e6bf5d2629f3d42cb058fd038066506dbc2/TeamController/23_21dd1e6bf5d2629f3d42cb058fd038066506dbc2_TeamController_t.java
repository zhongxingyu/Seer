 package com.africaapps.league.web.server.controller.team;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import com.africaapps.league.dto.PlayerMatchStatisticSummary;
 import com.africaapps.league.dto.PlayerMatchSummary;
 import com.africaapps.league.dto.PoolPlayerSummary;
 import com.africaapps.league.dto.TeamSummary;
 import com.africaapps.league.dto.UserPlayerSummary;
 import com.africaapps.league.dto.UserTeamListSummary;
 import com.africaapps.league.dto.UserTeamScoreHistorySummary;
 import com.africaapps.league.dto.UserTeamSummary;
 import com.africaapps.league.exception.InvalidPlayerException;
 import com.africaapps.league.exception.LeagueException;
 import com.africaapps.league.model.game.PoolPlayer;
 import com.africaapps.league.model.game.TeamFormat;
 import com.africaapps.league.model.game.User;
 import com.africaapps.league.model.game.UserLeague;
 import com.africaapps.league.model.game.UserPlayerStatus;
 import com.africaapps.league.model.game.UserTeam;
 import com.africaapps.league.model.game.UserTeamStatus;
 import com.africaapps.league.model.league.BlockType;
 import com.africaapps.league.model.league.League;
 import com.africaapps.league.service.game.team.UserTeamService;
 import com.africaapps.league.service.pool.PoolService;
 import com.africaapps.league.service.team.TeamService;
 import com.africaapps.league.service.user.UserService;
 import com.africaapps.league.web.server.controller.BaseLeagueController;
 
 @Controller
 @RequestMapping(value = "/team")
 public class TeamController extends BaseLeagueController {
 
 	@Autowired
 	private UserService userService;
 	@Autowired
 	private UserTeamService userTeamService;
 	@Autowired
 	private TeamService teamService;
 	@Autowired
 	private PoolService poolService;
 
 	private static Logger logger = LoggerFactory.getLogger(TeamController.class);
 
 	@RequestMapping(value = "")
 	public String catchAll(
 			@RequestParam(required = false, value = NEW_USER_PARAM) String newUser,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = USERNAME_PARAM) String username,
 			@RequestParam(required = false, value = MESSAGE_PARAM) String message,
 			@RequestParam(required = false, value = "notification") String notification, ModelMap model) {
 		return "redirect:/team/list";
 	}
 
 	@RequestMapping(value = "/list")
 	public String getUserTeams(HttpServletRequest request,
 			@RequestParam(required = false, value = NEW_USER_PARAM) String newUser,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = USERNAME_PARAM) String username,
 			@RequestParam(required = false, value = MESSAGE_PARAM) String message,
 			@RequestParam(required = false, value = "notification") String notification,
 			ModelMap model) {
 		logger.info("Getting teams for user: " + userId + " newUser:" + newUser + " username: " + username);
 		// Get the current user
 		User user = getUser(request, userId, username);
 		if (user != null) {
 			removeAndAdd(model, USER_ID_PARAM, user.getId().toString());
 			logger.debug("Found corresponding user: " + user);
 		} else {
 			String mxitUser = getMxitUser(request);
 			if (isValid(mxitUser)) {
 				try {
 					user = new User();
 					user.setUsername(mxitUser);
 					String nickName = getMxitNickName(request);
 					if (isValid(nickName)) {
 						user.setFirstName(nickName);
 					}
 					userService.saveUser(user);
 					removeAndAdd(model, USER_ID_PARAM, user.getId().toString());
 					logger.info("Saved new user: "+user);
 					newUser = "true";
 				} catch (LeagueException e) {
 					model.addAttribute("message", "Unable to save your user!");
 					return TEAMS_PAGE_MAPPING;
 				}
 			} else {
 				model.addAttribute("message", "Unknown current user");
 				return TEAMS_PAGE_MAPPING;
 			}
 		}
 
 		if (newUser == null || !newUser.equalsIgnoreCase("true")) {
 			try {
 				List<UserTeamListSummary> teams = userTeamService.getTeamSummaries(user.getId());
 				logger.info("Got teams: " + teams.size());
 				model.addAttribute("teams", teams);				
 			} catch (LeagueException e) {
 				model.addAttribute("message", "Unable to load your teams");
 				return TEAMS_PAGE_MAPPING;
 			}
 		} else {
 			model.addAttribute("newUser", "true");
 		}
 		
 		if (isValid(message)) {
 			removeAndAdd(model, MESSAGE_PARAM, message);
 		}
 		if (isValid(notification)) {
 			removeAndAdd(model, "notification", notification);
 		}
 		return TEAMS_PAGE_MAPPING;
 	}
 
 	@RequestMapping(value = "/create")
 	public String startCreateTeam(HttpServletRequest request,
 			@RequestParam(required = false, value = TEAM_NAME_PARAM) String teamName,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = USERNAME_PARAM) String username, 
 			ModelMap model) {
 		User user = getUser(request, userId, username);
 		try {
 			if (user != null) {
 				removeAndAdd(model, USER_ID_PARAM, user.getId().toString());
 				if (isValid(teamName)) {
 					UserTeam userTeam = userTeamService.getTeam(user.getId(), teamName);
 					if (userTeam == null) {
 						UserLeague league = getDefaultUserLeague();
 						userTeam = new UserTeam();
 						userTeam.setName(teamName);
 						userTeam.setCurrentScore(0);
 						userTeam.setAvailableMoney(getInitTeamMoney(league.getLeague()));
 						userTeam.setCurrentFormat(getDefaultTeamFormat(league.getLeague()));
 						userTeam.setStatus(UserTeamStatus.INCOMPLETE);
 						userTeam.setUserLeague(league);
 						userTeam.setUser(user);
 						userTeamService.saveTeam(userTeam);
 						logger.info("Created team: " + userTeam);
 						//Go to the new team page
 						removeAndAdd(model, "team", userTeam);
 						return NEW_TEAM_PAGE_MAPPING;
 					} else {
 						logger.info("Got existing team: " + userTeam);
 					}
 					return "redirect:/team/list";
 				} else {
 					model.addAttribute("message", "Enter a team name");
 					return TEAMS_PAGE_MAPPING;
 				}
 			} else {
 				logger.error("startCreateTeam() Unknown user!");
 				return REGISTER;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error creating team: ", e);
 			model.addAttribute("message", "Unable to create your team!");
 		}
 		return TEAMS_PAGE_MAPPING;
 	}
 
 	protected Long getInitTeamMoney(League league) throws LeagueException {
 		return userTeamService.getDefaultAvailableMoney(league);
 	}
 
 	protected UserLeague getDefaultUserLeague() throws LeagueException {
 		return userTeamService.getDefaultUserLeague();
 	}
 
 	protected TeamFormat getDefaultTeamFormat(League league) throws LeagueException {
 		return userTeamService.getDefaultTeamFormat(league);
 	}
 
 	@RequestMapping(value = "/players")
 	public String getUserTeamPlayers(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = MESSAGE_PARAM) String message, 
 			@RequestParam(required = false, value = "notification") String notification, 
 			ModelMap model) {
 		logger.info("Getting players for user: " + userId + " teamId:" + teamId);
 		User user = getUser(request, userId, null);
 		try {
 			removeAndAdd(model, USER_ID_PARAM, userId);
 			removeAndAdd(model, TEAM_ID_PARAM, teamId);
 			removeAndAdd(model, MESSAGE_PARAM, message);
 			removeAndAdd(model, "notification", notification);
 			if (user != null) {
 				if (isValidId(teamId)) {
 					UserTeamSummary userTeam = userTeamService.getTeamWithPlayers(Long.valueOf(teamId));
 					if (userTeam != null) {
 						logger.info("UserTeamSummary: def:" + userTeam.getDefenders().size() + " goalie:"
 								+ userTeam.getGoalKeepers().size() + " mid:" + userTeam.getMidfielders().size() + " strikers:"
 								+ userTeam.getStrikers().size() + " sub:" + userTeam.getSubstitutes().size());
 						userTeam.setUserId(user.getId());
 					}
 					model.addAttribute("team", userTeam);
 					return PLAYERS_PAGE_MAPPING;
 				} else {
 					model.addAttribute("message", "No team specified");
 					return PLAYERS_PAGE_MAPPING;
 				}
 			} else {
 				// Check for any of the user's identification and if nothing, go back to the default mapping
 				removeAndAdd(model, MESSAGE_PARAM, "No user identification found :( ");
 				return DEFAULT_MAPPING;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting team's players: ", e);
 			model.addAttribute("message", "Unable to view your players");
 			return TEAMS_PAGE_MAPPING;
 		}
 	}
 
 	@RequestMapping(value = "/findPlayer")
 	public String addPlayer(
 			HttpServletRequest request, 
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = "type") String playerType, 
 			ModelMap model) {
 		logger.info("Finding player: " + userId + " teamId:" + teamId + " playerType:" + playerType);
 		User user = getUser(request, userId, null);
 		try {
 			removeAndAdd(model, USER_ID_PARAM, userId);
 			removeAndAdd(model, TEAM_ID_PARAM, teamId);
 			removeAndAdd(model, "type", playerType);
 			if (user != null) {
 				if (isValid(playerType) && isValidId(teamId)) {
 					List<TeamSummary> teams = teamService.getTeams(Long.valueOf(teamId));
 					Collections.sort(teams, new Comparator<TeamSummary>() {
 						@Override
 						public int compare(TeamSummary o1, TeamSummary o2) {
 							return o1.getTeamName().compareTo(o2.getTeamName());
 						}
 					});
 					model.addAttribute("teams", teams);
 					return TEAMS_SEARCH_PAGE_MAPPING;
 				} else if (!isValid(playerType)) {
 					model.addAttribute("message", "No player type specified");
 					return TEAMS_SEARCH_PAGE_MAPPING;
 				} else {
 					model.addAttribute("message", "No team specified");
 					return TEAMS_SEARCH_PAGE_MAPPING;
 				}
 			} else {
 				// Check for any of the user's identification and if nothing, go back to the default mapping
 				removeAndAdd(model, MESSAGE_PARAM, "No user identification found :( ");
 				return DEFAULT_MAPPING;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting teams: ", e);
 			model.addAttribute("message", "Unable to view teams");
 			return TEAM_PLAYERS_PAGE_MAPPING;
 		}
 	}
 
 	@RequestMapping(value = "/teamPlayers")
 	public String findPlayer(HttpServletRequest request, 
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = "type") String type,
 			@RequestParam(required = false, value = "team") String team,
 			@RequestParam(required = false, value = "teamLogo") String teamLogo,
 			ModelMap model) {
 		logger.info("Finding team's player: " + userId + " teamId:" + teamId + " playerType:" + type + " team:" + team);
 		User user = getUser(request, userId, null);
 		try {
 			removeAndAdd(model, USER_ID_PARAM, userId);
 			removeAndAdd(model, TEAM_ID_PARAM, teamId);
 			removeAndAdd(model, "type", type);
 			removeAndAdd(model, "team", team);
 			if (user != null) {
 				if (isValidId(teamId) && isValidId(team)) {
 					List<UserPlayerSummary> players = userTeamService.getTeamPlayers(Long.valueOf(team), Long.valueOf(teamId), type);
 					if (type.equalsIgnoreCase(BlockType.SUBSTITUTE.name())) {
 						logger.debug("Sorting subs");
 						UserPlayerSummary s = null;
 				    for(int i=players.size()-1;i>=0;i--) {
 				    	s = players.get(i);
 				    	if (!isValid(s.getBlock())) {				    		
 				    		logger.info("Removing sub with no block: "+s);
 				    		players.remove(i);
 				    	}
 				    }
 						Collections.sort(players, new SubstituteComparator());
 					} else {
 						logger.debug("Sorting players");
 						Collections.sort(players, new NameComparator());
 					}
 					logger.info("Got " + players.size() + " of type:" + type);
 					removeAndAdd(model, "players", players);
 					// User Team's money
 					Long availableMoney = userTeamService.getAvailableMoney(Long.valueOf(teamId));
 					removeAndAdd(model, "availableMoney", availableMoney);
 					//Team's logo
 					removeAndAdd(model, "teamLogo", teamLogo);
 					return TEAM_PLAYERS_PAGE_MAPPING;
 				} else {
 					model.addAttribute("message", "No teams specified");
 					return TEAM_PLAYERS_PAGE_MAPPING;
 				}
 			} else {
 				// Check for any of the user's identification and if nothing, go back to the default mapping
 				removeAndAdd(model, MESSAGE_PARAM, "No user identification found :( ");
 				return DEFAULT_MAPPING;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting team's players: ", e);
 			model.addAttribute("message", "Unable to view team's players");
 		}
 		return "/team/players?userid=" + userId + "&teamid=" + teamId;
 	}
 
 	@RequestMapping(value = "/addPlayer")
 	public String addPlayer(HttpServletRequest request, @RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = "type") String type,
 			@RequestParam(required = false, value = "team") String team,
 			@RequestParam(required = false, value = POOL_PLAYER_ID_PARAM) String poolPlayerId, ModelMap model) {
 		logger.info("Adding player to team  userId:" + userId + " teamId:" + teamId + " playerType:" + type + " team:" + team
 				+ " poolPlayerId:" + poolPlayerId);
 		User user = getUser(request, userId, null);
 		try {
 			if (user != null) {
 				if (isValidId(teamId) && isValidId(team) && isValidId(poolPlayerId)) {
 					model.remove(USER_ID_PARAM);
 					model.addAttribute(USER_ID_PARAM, userId);
 					model.remove(TEAM_ID_PARAM);
 					model.addAttribute(TEAM_ID_PARAM, teamId);
 					userTeamService.addPlayerToUserTeam(user, Long.valueOf(teamId), Long.valueOf(poolPlayerId), type);
 				} else {
 					model.addAttribute("message", "No team or player specified");
 				}
 			} else {
 				// Check for any of the user's identification and if nothing, go back to the default mapping
 				removeAndAdd(model, MESSAGE_PARAM, "No user identification found :( ");
 				return DEFAULT_MAPPING;
 			}
 		} catch (InvalidPlayerException e) {
 			logger.error("Invalid player to add to team:" + e.getMessage());
 			model.addAttribute("message", e.getMessage());
 		} catch (LeagueException e) {
 			logger.error("Error adding player to team: ", e);
 			model.addAttribute("message", "Unable to add player to team");
 		}
 		String url = "redirect:/team/players";
 		logger.info("Going back to players page:" + url);
 		return url;
 	}
 
 	@RequestMapping(value = "/changePlayerStatus")
 	public String startChangePlayerStatus(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = "type") String type,
 			@RequestParam(required = false, value = POOL_PLAYER_ID_PARAM) String poolPlayerId,
 			@RequestParam(required = false, value = "captain") String captain,
 			@RequestParam(required = false, value = "teamstatus") String teamStatus,
 			@RequestParam(required = false, value = "captainid") String captainId,
 			ModelMap model) {
 		logger.info("Changing player's status - userId:" + userId + " teamId:" + teamId + " playerType:" + type
 				+ " poolPlayerId:" + poolPlayerId + " captain:" + captain);
 		User user = getUser(request, userId, null);
 		try {
 			removeAndAdd(model, USER_ID_PARAM, userId);
 			removeAndAdd(model, TEAM_ID_PARAM, teamId);
 			removeAndAdd(model, "captainid", captainId);
 			if (user != null) {
 				if (isValidId(teamId)) {
 					if (isValid(captain) && Boolean.TRUE.toString().equals(captain)) {
 						logger.info("Choosing the captain...");
 						UserTeamSummary userTeam = userTeamService.getTeamWithPlayers(Long.valueOf(teamId));
 						if (userTeam != null) {
 							userTeam.setUserId(user.getId());
 							logger.info("UserTeamSummary: def:" + userTeam.getDefenders().size() + " goalie:"
 									+ userTeam.getGoalKeepers().size() + " mid:" + userTeam.getMidfielders().size() + " strikers:"
 									+ userTeam.getStrikers().size() + " sub:" + userTeam.getSubstitutes().size());
 						}
 						model.addAttribute("team", userTeam);
 						// Indicate the current captain
 						model.addAttribute("captainId", poolPlayerId);
 						return "captainStatus";
 					} else if (isValidId(poolPlayerId) && isValid(teamStatus)
 							&& UserTeamStatus.COMPLETE.name().equalsIgnoreCase(teamStatus)) {
 						logger.info("Choosing player for substitute...");
 						UserTeamSummary userTeam = userTeamService.getTeamWithSamePlayers(Long.valueOf(teamId), Long.valueOf(poolPlayerId));
 						if (userTeam != null) {
 							userTeam.setUserId(user.getId());
 							logger.info("UserTeamSummary: def:" + userTeam.getDefenders().size() + " goalie:"
 									+ userTeam.getGoalKeepers().size() + " mid:" + userTeam.getMidfielders().size() + " strikers:"
 									+ userTeam.getStrikers().size() + " sub:" + userTeam.getSubstitutes().size());
 						}
 						model.addAttribute("team", userTeam);
 						// The substitute's id
 						removeAndAdd(model, "substituteid", poolPlayerId);
 						return "substituteStatus";
 					} else if (isValidId(poolPlayerId)) {
 						logger.info("Changing player's status...");
 						UserPlayerSummary player = userTeamService.getTeamPlayer(Long.valueOf(teamId), Long.valueOf(poolPlayerId));
 						if (player != null) {
 							model.remove("statuses");
 							model.addAttribute("statuses", getStatuses(player.getStatus(), teamStatus));
 							model.remove("player");
 							model.addAttribute("player", player);
 						}
 					} else {
 						model.addAttribute("message", "No player specified");
 					}
 				} else {
 					model.addAttribute("message", "No team specified");
 				}
 			} else {
 				// Check for any of the user's identification and if nothing, go back to the default mapping
 				removeAndAdd(model, MESSAGE_PARAM, "No user identification found :( ");
 				return DEFAULT_MAPPING;
 			}
 		} catch (InvalidPlayerException e) {
 			logger.error("Invalid player to change status:" + e.getMessage());
 			model.addAttribute("message", e.getMessage());
 		} catch (LeagueException e) {
 			logger.error("Error getting player to change status: ", e);
 			model.addAttribute("message", "Unable to change player's status");
 		}
 		return "playerStatus";
 	}
 
 	@RequestMapping(value = "/setPlayerStatus")
 	public String setPlayerStatus(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = POOL_PLAYER_ID_PARAM) String poolPlayerId,
 			@RequestParam(required = false, value = "status") String status, ModelMap model) {
 		logger.info("Updating player's status - userId:" + userId + " teamId:" + teamId + " poolPlayerId:" + poolPlayerId
 				+ " status:" + status);
 		User user = getUser(request, userId, null);
 		try {
 			removeAndAdd(model, USER_ID_PARAM, userId);
 			removeAndAdd(model, TEAM_ID_PARAM, teamId);
 			if (user != null) {
 				if (isValidId(teamId)) {
 					if (isValidId(poolPlayerId)) {
 						if (isValid(status)) {
 							try {
 								userTeamService.setPlayerStatus(Long.valueOf(teamId), Long.valueOf(poolPlayerId), status);
 							} catch (InvalidPlayerException e) {
 								model.addAttribute("message", e.getMessage());
 							}
 						} else {
 							model.addAttribute("message", "No status specified");
 						}
 					} else {
 						model.addAttribute("message", "No player specified");
 					}
 				} else {
 					model.addAttribute("message", "No team specified");
 				}
 			} else {
 				// Check for any of the user's identification and if nothing, go back to the default mapping
 				removeAndAdd(model, MESSAGE_PARAM, "No user identification found :( ");
 				return DEFAULT_MAPPING;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error changing player's status: ", e);
 			model.addAttribute("message", "Unable to change player's status");
 		}
 		String url = "redirect:/team/players";
 		logger.info("Going back to players page:" + url);
 		return url;
 	}
 
 	@RequestMapping(value = "/swapPlayers")
 	public String swapPlayers(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = POOL_PLAYER_ID_PARAM) String poolPlayerId,
 			@RequestParam(required = false, value = "substituteid") String substituteId, ModelMap model) {
 		logger.info("Swapping players userId:" + userId + " teamId:" + teamId + " poolPlayerId:" + poolPlayerId
 				+ " substituteId:" + substituteId);
 		User user = getUser(request, userId, null);
 		try {
 			removeAndAdd(model, USER_ID_PARAM, userId);
 			removeAndAdd(model, TEAM_ID_PARAM, teamId);
 			if (user != null) {
 				if (isValidId(teamId)) {
 					if (isValidId(substituteId)) {
 						if (isValidId(poolPlayerId)) {
 							try {
 								userTeamService.swapPlayers(Long.valueOf(teamId), Long.valueOf(substituteId), Long.valueOf(poolPlayerId));
 								removeAndAdd(model, "notification", "Players swapped!");
 							} catch (InvalidPlayerException e) {
 								model.addAttribute("message", e.getMessage());
 							}
 						} else {
 							model.addAttribute("message", "No player specified");
 						}
 					} else {
 						model.addAttribute("message", "No substitute specified");
 					}
 				} else {
 					model.addAttribute("message", "No team specified");
 				}
 			} else {
 				// Check for any of the user's identification and if nothing, go back to the default mapping
 				removeAndAdd(model, MESSAGE_PARAM, "No user identification found :( ");
 				return DEFAULT_MAPPING;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error changing player's status: ", e);
 			model.addAttribute("message", "Unable to change player's status");
 		}
 		String url = "redirect:/team/players";
 		logger.info("Going back to players page:" + url);
 		return url;
 	}
 
 	@RequestMapping(value = "/startTrade")
 	public String startPlayerTrade(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId, ModelMap model) {
 		logger.info("Starting trade for user: " + userId + " teamId:" + teamId);
 		User user = getUser(request, userId, null);
 		try {
 			removeAndAdd(model, USER_ID_PARAM, userId);
 			removeAndAdd(model, TEAM_ID_PARAM, teamId);
 			if (user != null) {
 				if (isValidId(teamId)) {
 					if (userTeamService.isUserTeamAbleToTrade(Long.valueOf(teamId))) {
 						List<TeamSummary> teams = teamService.getTeams(Long.valueOf(teamId));
 						Collections.sort(teams, new Comparator<TeamSummary>() {
 							@Override
 							public int compare(TeamSummary o1, TeamSummary o2) {
 								return o1.getTeamName().compareTo(o2.getTeamName());
 							}
 						});
 						model.addAttribute("teams", teams);
 						return TRADE_PLAYER_MAPPING;
 					} else {
 						removeAndAdd(model, MESSAGE_PARAM, "You can not trade a player as you only get 1 free trade every 2 weeks");
 						return "redirect:/team/players";
 					}
 				} else {
 					model.addAttribute("message", "No team specified");
 					return PLAYERS_PAGE_MAPPING;
 				}
 			} else {
 				// Check for any of the user's identification and if nothing, go back to the default mapping
 				removeAndAdd(model, MESSAGE_PARAM, "No user identification found :( ");
 				return DEFAULT_MAPPING;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting team's players for trade: ", e);
 			model.addAttribute("message", "Unable to view your players for trade");
 			return PLAYERS_PAGE_MAPPING;
 		}
 	}
 
 	@RequestMapping(value = "/tradeTeamPlayers")
 	public String tradeTeamPlayer(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String userTeamId,
 			@RequestParam(required = false, value = "team") String teamId, ModelMap model) {
 		logger.info("Trade player: finding team's player: " + userId + " userTeamId:" + userTeamId + " teamId:" + teamId);
 		User user = getUser(request, userId, null);
 		try {
 			removeAndAdd(model, USER_ID_PARAM, userId);
 			removeAndAdd(model, TEAM_ID_PARAM, userTeamId);
 			removeAndAdd(model, "team", teamId);
 			model.remove("teams");
 			if (user != null) {
 				if (isValidId(userTeamId) && isValidId(teamId)) {
 					List<UserPlayerSummary> players = userTeamService.getTeamPlayers(Long.valueOf(teamId), Long.valueOf(userTeamId), BlockType.SUBSTITUTE.name());
 					UserPlayerSummary s = null;
 			    for(int i=players.size()-1;i>=0;i--) {
 			    	s = players.get(i);
 			    	if (!isValid(s.getBlock())) {				    		
 			    		logger.info("Removing player with no block: "+s);
 			    		players.remove(i);
 			    	}
 			    }
 					Collections.sort(players, new SubstituteComparator());
 					logger.info("Got " + players.size());
 					removeAndAdd(model, "players", players);
 					// User Team's money
 					Long availableMoney = userTeamService.getAvailableMoney(Long.valueOf(teamId));
 					removeAndAdd(model, "availableMoney", availableMoney);
 					return TRADE_PLAYER_MAPPING;
 				} else {
 					model.addAttribute("message", "No team specified");
 					return PLAYERS_PAGE_MAPPING;
 				}
 			} else {
 				// Check for any of the user's identification and if nothing, go back to the default mapping
 				removeAndAdd(model, MESSAGE_PARAM, "No user identification found :( ");
 				return DEFAULT_MAPPING;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting pool players: ", e);
 			model.addAttribute("message", "Unable to get pool players");
 		}
 		return PLAYERS_PAGE_MAPPING;
 	}
 
 	@RequestMapping(value = "/selectTradePoolPlayer")
 	public String selectTradePoolPlayer(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String userTeamId,
 			@RequestParam(required = false, value = POOL_PLAYER_ID_PARAM) String poolPlayerId,
 			@RequestParam(required = false, value = MESSAGE_PARAM) String message, ModelMap model) {
 		logger
 				.info("Selected new PoolPlayer: userId:" + userId + " userTeamId:" + userTeamId + " poolPlayerId: " + poolPlayerId);
 		User user = getUser(request, userId, null);
 		try {
 			removeAndAdd(model, USER_ID_PARAM, userId);
 			removeAndAdd(model, TEAM_ID_PARAM, userTeamId);
 			removeAndAdd(model, MESSAGE_PARAM, message);
 			model.remove("players");
 			if (user != null) {
 				if (isValidId(userTeamId)) {
 					if (isValidId(poolPlayerId)) {
 						// Pool player
 						PoolPlayer poolPlayer = poolService.getPoolPlayer(Long.valueOf(poolPlayerId));
 						if (poolPlayer != null) {
 							PoolPlayerSummary summary = new PoolPlayerSummary();
 							summary.setBlock(formatBlock(poolPlayer.getPlayer().getBlock()));
 							summary.setCurrentScore(poolPlayer.getPlayerCurrentScore());
 							summary.setFirstName(poolPlayer.getPlayer().getFirstName());
 							summary.setLastName(poolPlayer.getPlayer().getLastName());
 							summary.setPoolPlayerId(poolPlayer.getId());
 							summary.setPrice(poolPlayer.getPlayerPrice());
 							removeAndAdd(model, "poolPlayer", summary);
 						}
 						// Get the current team players
 						UserTeamSummary userTeam = userTeamService.getTeamWithPlayers(Long.valueOf(userTeamId));
 						if (userTeam != null) {
 							userTeam.setUserId(user.getId());
 							logger.debug("Got userTeam: " + userTeam);
 						}
 						removeAndAdd(model, "userTeam", userTeam);
 						// User Team's money
 						Long availableMoney = userTeamService.getAvailableMoney(Long.valueOf(userTeamId));
 						removeAndAdd(model, "availableMoney", availableMoney);
 						return TRADE_PLAYER_MAPPING;
 					} else {
 						model.addAttribute("message", "No pool player specified");
 						return PLAYERS_PAGE_MAPPING;
 					}
 				} else {
 					model.addAttribute("message", "No team specified");
 					return PLAYERS_PAGE_MAPPING;
 				}
 			} else {
 				// Check for any of the user's identification and if nothing, go back to the default mapping
 				removeAndAdd(model, MESSAGE_PARAM, "No user identification found :( ");
 				return DEFAULT_MAPPING;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting pool players: ", e);
 			model.addAttribute("message", "Unable to get pool players");
 		}
 		return PLAYERS_PAGE_MAPPING;
 	}
 	
 	private String formatBlock(BlockType block) {
 		if (block != null) {
 			String firstLetter = block.name().substring(0,1).toUpperCase();
 			return firstLetter + block.name().substring(1).toLowerCase();
 		} else {
 			return "";
 		}
 	}
 
 	@RequestMapping(value = "/tradePlayer")
 	public String tradePlayer(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = POOL_PLAYER_ID_PARAM) String poolPlayerId,
 			@RequestParam(required = false, value = "selectedid") String selectedId, ModelMap model) {
 		logger.info("Trading player - userId:" + userId + " teamId:" + teamId + " poolPlayerId:" + poolPlayerId + " selectedId:"
 				+ selectedId);
 		User user = getUser(request, userId, null);
 		try {
 			removeAndAdd(model, USER_ID_PARAM, userId);
 			removeAndAdd(model, TEAM_ID_PARAM, teamId);
 			removeAndAdd(model, POOL_PLAYER_ID_PARAM, poolPlayerId);
 			if (user != null) {
 				if (isValidId(teamId)) {
 					if (isValidId(poolPlayerId)) {
 						if (isValidId(selectedId)) {
 							try {
 								userTeamService.tradePlayers(user, Long.valueOf(teamId), Long.valueOf(poolPlayerId),
 										Long.valueOf(selectedId));
 								// Trade was successful
 								removeAndAdd(model, "notification", "Player was successfully traded");
 								return "redirect:/team/players";
 							} catch (InvalidPlayerException e) {
 								model.addAttribute("message", e.getMessage());
 							}
 						} else {
 							model.addAttribute("message", "No pool player selected");
 						}
 					} else {
 						model.addAttribute("message", "No team player specified");
 					}
 				} else {
 					model.addAttribute("message", "No team specified");
 				}
 			} else {
 				// Check for any of the user's identification and if nothing, go back to the default mapping
 				removeAndAdd(model, MESSAGE_PARAM, "No user identification found :( ");
 				return DEFAULT_MAPPING;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error changing player's status: ", e);
			model.addAttribute("message", "Error trading player");
 		}
 		return "redirect:/team/selectTradePoolPlayer";
 	}
 
 	@RequestMapping(value = "/set")
 	public String acceptTeam(HttpServletRequest request, @RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId, ModelMap model) {
 		logger.info("Setting team's status userId:" + userId + " teamId:" + teamId);
 		User user = getUser(request, userId, null);
 		try {
 			updateAttributes(model, userId, teamId, null, null);
 			if (user != null) {
 				if (isValidId(teamId)) {
 					try {
 						userTeamService.setTeam(user, Long.valueOf(teamId));
 						model.addAttribute("notification", "Your team is ready to play!");
 						return "redirect:/team/list";
 					} catch (InvalidPlayerException e) {
 						model.addAttribute("message", e.getMessage());
 					}
 				} else {
 					model.addAttribute("message", "No team specified");
 				}
 			} else {
 				// Check for any of the user's identification and if nothing, go back to the default mapping
 				removeAndAdd(model, MESSAGE_PARAM, "No user identification found :( ");
 				return DEFAULT_MAPPING;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error accepting team: ", e);
 			model.addAttribute("message", "Unable to accept team");
 		}
 		String url = "redirect:/team/players";
 		logger.info("Going back to players page:" + url);
 		return url;
 	}
 
 	private List<String> getStatuses(String playerStatus, String teamStatus) {
 		List<String> statuses = new ArrayList<String>();
 		if (isValid(teamStatus) && UserTeamStatus.COMPLETE.name().equalsIgnoreCase(teamStatus)) {
 			statuses.add(UserPlayerStatus.PLAYER.name());
 		} else {
 			for (UserPlayerStatus s : UserPlayerStatus.values()) {
 				if (!s.name().equalsIgnoreCase(playerStatus) 
 						&& !(s.name().equalsIgnoreCase(UserPlayerStatus.CAPTAIN.name()) 
 								&& playerStatus.equalsIgnoreCase(UserPlayerStatus.SUBSTITUTE.name()))) {
 					statuses.add(s.name());
 				}
 			}
 		}
 		return statuses;
 	}
 
 	@RequestMapping(value = "/changeFormat")
 	public String startChangeFormat(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId, ModelMap model) {
 		logger.info("Finding team's current format: " + userId + " teamId:" + teamId);
 		User user = getUser(request, userId, null);
 		try {
 			removeAndAdd(model, USER_ID_PARAM, userId);
 			removeAndAdd(model, TEAM_ID_PARAM, teamId);
 			if (user != null) {
 				model.remove(USER_ID_PARAM);
 				model.addAttribute(USER_ID_PARAM, userId);
 				if (isValidId(teamId)) {
 					UserTeam team = userTeamService.getTeam(Long.valueOf(teamId));
 					model.remove("formats");
 					if (team != null) {
 						model.remove("team");
 						model.addAttribute("team", team);
 						List<TeamFormat> formats = userTeamService.getTeamFormats(team.getUserLeague().getLeague().getLeagueType()
 								.getId());
 						for (int i = formats.size() - 1; i >= 0; i--) {
 							TeamFormat format = formats.get(i);
 							if (format.getId().equals(team.getCurrentFormat().getId())) {
 								formats.remove(i);
 								break;
 							}
 						}
 						model.addAttribute("formats", formats);
 						logger.info("Added formats:" + formats.size());
 					} else {
 						model.addAttribute("formats", new ArrayList<TeamFormat>());
 					}
 					return CHANGE_TEAM_FORMAT_MAPPING;
 				} else {
 					model.addAttribute("message", "No team specified");
 					return CHANGE_TEAM_FORMAT_MAPPING;
 				}
 			} else {
 				// Check for any of the user's identification and if nothing, go back to the default mapping
 				removeAndAdd(model, MESSAGE_PARAM, "No user identification found :( ");
 				return DEFAULT_MAPPING;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting team formats: ", e);
 			model.addAttribute("message", "Unable to get team formats");
 		}
 		return "/team/players?userid=" + userId + "&teamid=" + teamId;
 	}
 
 	@RequestMapping(value = "/setFormat")
 	public String changeFormat(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = "formatid") String formatId, ModelMap model) {
 		logger.info("Changing team's current format userId: " + userId + " teamId:" + teamId + " formatId:" + formatId);
 		User user = getUser(request, userId, null);
 		try {
 			removeAndAdd(model, USER_ID_PARAM, userId);
 			if (user != null) {
 				model.remove("team");
 				model.remove("teamid");
 				model.addAttribute("teamid", teamId);
 				if (isValidId(teamId) && isValidId(formatId)) {
 					try {
 						userTeamService.setTeamFormat(Long.valueOf(teamId), Long.valueOf(formatId));
 					} catch (InvalidPlayerException e) {
 						model.addAttribute("message", e.getMessage());
 					}
 				} else if (!isValidId(teamId)) {
 					model.addAttribute("message", "No team specified");
 				} else if (!isValidId(formatId)) {
 					model.addAttribute("message", "No format specified");
 				}
 			} else {
 				// Check for any of the user's identification and if nothing, go back to the default mapping
 				removeAndAdd(model, MESSAGE_PARAM, "No user identification found :( ");
 				return DEFAULT_MAPPING;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error changing team format: ", e);
 			model.addAttribute("message", "Unable to change team's format");
 		}
 		return "redirect:/team/players";
 	}
 
 	@RequestMapping(value = "/viewPlayerMatches")
 	public String viewPlayerMatches(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = POOL_PLAYER_ID_PARAM) String poolPlayerId, ModelMap model) {
 		User user = getUser(request, userId, null);
 		try {
 			if (user != null) {
 				if (isValidId(teamId) && isValidId(poolPlayerId)) {
 					updateAttributes(model, userId, teamId, poolPlayerId, null);
 					List<PlayerMatchSummary> matches = userTeamService.getPoolPlayerMatches(Long.valueOf(poolPlayerId));
 					model.addAttribute("matches", matches);
 					return PLAYER_MATCHES_PAGE_MAPPING;
 				} else {
 					model.addAttribute("message", "No team or player specified");
 					return PLAYER_MATCHES_PAGE_MAPPING;
 				}
 			} else {
 				// Check for any of the user's identification and if nothing, go back to the default mapping
 				removeAndAdd(model, MESSAGE_PARAM, "No user identification found :( ");
 				return DEFAULT_MAPPING;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting player's matches team: ", e);
 			model.addAttribute("message", "Unable to view player's matches");
 		}
 		String url = "redirect:/team/viewPlayerMatches";
 		logger.info("Going back to players page:" + url);
 		return url;
 	}
 
 	@RequestMapping(value = "/viewMatchStats")
 	public String viewPlayerMatchStats(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = POOL_PLAYER_ID_PARAM) String poolPlayerId,
 			@RequestParam(required = false, value = MATCH_ID_PARAM) String matchId,
 			@RequestParam(required = false, value = "fromteam") String fromTeam,
 			ModelMap model) {
 		User user = getUser(request, userId, null);
 		try {
 			if (user != null) {
 				removeAndAdd(model, "fromteam", fromTeam);
 				if (isValidId(teamId) && isValidId(poolPlayerId) && isValidId(matchId)) {
 					updateAttributes(model, userId, teamId, poolPlayerId, matchId);
 					List<PlayerMatchStatisticSummary> stats = userTeamService.getPoolPlayerMatchStats(
 							Long.valueOf(poolPlayerId),
 							Long.valueOf(matchId));
 					model.addAttribute("stats", stats);
 					return PLAYER_MATCH_STATS_PAGE_MAPPING;
 				} else {
 					model.addAttribute("message", "No team, player or match specified");
 					return PLAYER_MATCH_STATS_PAGE_MAPPING;
 				}
 			} else {
 				// Check for any of the user's identification and if nothing, go back to the default mapping
 				removeAndAdd(model, MESSAGE_PARAM, "No user identification found :( ");
 				return DEFAULT_MAPPING;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting player's matches team: ", e);
 			model.addAttribute("message", "Unable to view player's matches");
 		}
 		String url = "redirect:/team/teamHistory";
 		logger.info("Going back to teamHistory page:" + url);
 		return url;
 	}
 
 	@RequestMapping(value = "/teamHistory")
 	public String viewTeamHistoryPoints(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = MESSAGE_PARAM) String message, 
 			ModelMap model) {
 		logger.info("Getting team's history userId:" + userId + " teamId:" + teamId);
 		User user = getUser(request, userId, null);
 		if (message != null) {
 			removeAndAdd(model, MESSAGE_PARAM, message);
 		}
 		try {
 			updateAttributes(model, userId, teamId, null, null);
 			if (user != null) {
 				if (isValidId(teamId)) {
 					List<UserTeamScoreHistorySummary> scores = userTeamService.getUserTeamScoreHistory(user, Long.valueOf(teamId));
 					model.addAttribute("scores", scores);
 					logger.info("Got scores: " + scores.size());
 					return USER_TEAM_SCORE_HISTORY_PAGE_MAPPING;
 				} else {
 					model.addAttribute("message", "No team specified");
 					return USER_TEAM_SCORE_HISTORY_PAGE_MAPPING;
 				}
 			} else {
 				// Check for any of the user's identification and if nothing, go back to the default mapping
 				removeAndAdd(model, MESSAGE_PARAM, "No user identification found :( ");
 				return DEFAULT_MAPPING;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting team's history: ", e);
 			model.addAttribute("message", "Unable to view team's history");
 			return "redirect:/team/list";
 		}
 	}
 
 	@RequestMapping(value = "/teamHistoryPlayersPoints")
 	public String viewTeamHistoryPlayersPoints(HttpServletRequest request,
 			@RequestParam(required = false, value = USER_ID_PARAM) String userId,
 			@RequestParam(required = false, value = TEAM_ID_PARAM) String teamId,
 			@RequestParam(required = false, value = MATCH_ID_PARAM) String matchId, 
 			ModelMap model) {
 		logger.info("Getting team's player points history userId:" + userId + " teamId:" + teamId + " matchId: " + matchId);
 		User user = getUser(request, userId, null);
 		try {
 			updateAttributes(model, userId, teamId, null, matchId);
 			if (user != null) {
 				if (isValidId(teamId) && isValidId(matchId)) {
 					List<UserTeamScoreHistorySummary> scores = userTeamService.getUserTeamScorePlayersHistory(user,
 							Long.valueOf(teamId), Long.valueOf(matchId));
 					model.addAttribute("scores", scores);
 					logger.info("Got scores: " + scores.size());
 					return USER_TEAM_SCORE_PLAYERS_HISTORY_PAGE_MAPPING;
 				} else {
 					model.addAttribute("message", "No team or match specified");
 					return USER_TEAM_SCORE_PLAYERS_HISTORY_PAGE_MAPPING;
 				}
 			} else {
 				// Check for any of the user's identification and if nothing, go back to the default mapping
 				removeAndAdd(model, MESSAGE_PARAM, "No user identification found :( ");
 				return DEFAULT_MAPPING;
 			}
 		} catch (LeagueException e) {
 			logger.error("Error getting team's player points history: ", e);
 			model.addAttribute("message", "Unable to view team's player points history");
 		}
 		return "redirect:/team/teamHistory";
 	}
 }
