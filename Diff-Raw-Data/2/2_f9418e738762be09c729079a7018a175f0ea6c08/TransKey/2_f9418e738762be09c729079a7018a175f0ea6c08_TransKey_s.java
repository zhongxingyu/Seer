 /*
  * Copyright (C) 2013-2014 Dabo Ross <http://www.daboross.net/>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.daboross.bukkitdev.skywars.api.translations;
 
 import lombok.AllArgsConstructor;
 
 /**
  * Translation keys for SkyWars. This enumeration should contain one key for
  * every individual message in game.
  */
 @AllArgsConstructor
 public enum TransKey {
 
     // commands.join
     CMD_JOIN_DESCRIPTION("commands.join.description", 0),
     CMD_JOIN_CONFIRMATION("commands.join.confirmation", 0),
     CMD_JOIN_ALREADY_QUEUED("commands.join.already-queued", 0),
     CMD_JOIN_IN_GAME("commands.join.in-game", 0),
     // commands.leave
     CMD_LEAVE_DESCRIPTION("commands.leave.description", 0),
     CMD_LEAVE_REMOVED_FROM_QUEUE("commands.leave.removed-from-queue", 0),
     CMD_LEAVE_REMOVED_FROM_GAME("commands.leave.removed-from-game", 0),
     CMD_LEAVE_NOT_IN("commands.leave.not-in-any", 0),
     // commands.setlobby
     CMD_SETLOBBY_DESCRIPTION("commands.setlobby.description", 0),
     CMD_SETLOBBY_CONFIRMATION("commands.setlobby.confirmation", 0),
     CMD_SETLOBBY_IN_GAME("commands.setlobby.in-game", 0),
     // commands.setportal
     CMD_SETPORTAL_DESCRIPTION("commands.setportal.description", 0),
     CMD_SETPORTAL_CONFIRMATION("commands.setportal.confirmation", 0),
     CMD_SETPORTAL_IN_GAME("commands.setportal.in-game", 0),
     // commands.delportal
     CMD_DELPORTAL_DESCRIPTION("commands.delportal.description", 0),
     CMD_DELPORTAL_CONFIRMATION("commands.delportal.confirmation", 1),
     CMD_DELPORTAL_NO_PORTAL_ERROR("commands.delportal.error-no-portals", 0),
     // commands.cancel
     CMD_CANCEL_DESCRIPTION("commands.cancel.description", 0),
     CMD_CANCEL_NOT_AN_INT("commands.cancel.argument-not-integer", 1),
     CMD_CANCEL_NO_GAMES_WITH_ID("commands.cancel.no-games-with-id", 1),
     CMD_CANCEL_CONFIRMATION("commands.cancel.confirmation", 1),
     // commands.status
     CMD_STATUS_DESCRIPTION("commands.status.description", 0),
     CMD_STATUS_HEADER("commands.status.header", 0),
     CMD_STATUS_IN_QUEUE("commands.status.in-queue", 1),
     CMD_STATUS_QUEUE_COMMA("commands.status.queue-comma", 0),
     CMD_STATUS_ARENA_HEADER("commands.status.arena-header", 0),
     // commands.version
     CMD_VERSION_DESCRIPTION("commands.version.description", 0),
     CMD_VERSION_OUTPUT("commands.version.output", 3),
     // commands.lobby
     CMD_LOBBY_DESCRIPTION("commands.lobby.description", 0),
     CMD_LOBBY_CONFIRMATION("commands.lobby.confirmation", 0),
     CMD_LOBBY_IN_GAME("commands.lobby.in-game", 0),
     // commands.cancelall
     CMD_CANCELALL_DESCRIPTION("commands.cancelall.description", 0),
     CMD_CANCELALL_CANCELING("commands.cancelall.canceling-game", 1),
     // commands.report
     CMD_REPORT_DESCRIPTION("commands.report.description", 0),
     CMD_REPORT_START("commands.report.start", 0),
     CMD_REPORT_OUTPUT("commands.report.output", 1),
     // commands.forcestart
     CMD_FORCESTART_DESCRIPTION("commands.forcestart.description", 0),
     CMD_FORCESTART_NOT_ENOUGH("commands.forcestart.not-enough-players", 0),
     // commands.kit
     CMD_KIT_DESCRIPTION("commands.kit.description", 0),
     CMD_KIT_NO_KITS_AVAILABLE("commands.kit.no-kits-available", 0),
    CMD_KIT_UNAVAILABLE_KITS("commands.kit.unavailable-kits", 0),
     CMD_KIT_UNKNOWN_KIT("commands.kit.unknown-kit", 1),
     CMD_KIT_NOT_ENOUGH_MONEY("commands.kit.not-enough-money", 3),
     CMD_KIT_CHOSE_KIT("commands.kit.chose-kit", 1),
     CMD_KIT_CHOSE_KIT_WITH_COST("commands.kit.chose-kit-with-cost", 2),
     CMD_KIT_HOW_TO_REMOVE("commands.kit.how-to-remove", 0),
     CMD_KIT_REMOVED_KIT("commands.kit.removed-kit", 1),
     CMD_KIT_NO_KIT_REMOVED("commands.kit.no-kit-removed", 0),
     CMD_KIT_CURRENT_KIT("commands.kit.current-kit", 1),
     CMD_KIT_CURRENT_KIT_WITH_COST("commands.kit.current-kit-with-cost", 2),
     // setup-commands.start
     SWS_START_DESCRIPTION("setup-commands.start.description", 0),
     SWS_START_NAME_ARGUMENT("setup-commands.start.argument-name", 0),
     SWS_START_CONFIRMATION("setup-commands.start.confirmation", 1),
     SWS_START_ONE_ARGUMENT("setup-commands.start.one-argument", 0),
     // setup-commands.setpos1
     SWS_SETPOS1_DESCRIPTION("setup-commands.setpos1.description", 0),
     SWS_SETPOS1_POS2_OTHER_WORLD("setup-commands.setpos1.pos2-in-other-world", 0),
     SWS_SETPOS1_CONFIRMATION("setup-commands.setpos1.confirmation", 0),
     // setup-commands.setpos2
     SWS_SETPOS2_DESCRIPTION("setup-commands.setpos2.description", 0),
     SWS_SETPOS2_POS1_OTHER_WORLD("setup-commands.setpos2.pos1-in-other-world", 0),
     SWS_SETPOS2_CONFIRMATION("setup-commands.setpos2.confirmation", 0),
     // setup-commands.addspawn
     SWS_ADDSPAWN_DESCRIPTION("setup-commands.addspawn.description", 0),
     SWS_ADDSPAWN_CONFIRMATION("setup-commands.addspawn.confirmation", 1),
     // setup-commands.save
     SWS_SAVE_DESCRIPTION("setup-commands.save.description", 0),
     SWS_SAVE_SAVING("setup-commands.save.saving", 0),
     SWS_SAVE_SAVED("setup-commands.save.saved", 0),
     // top-commands
     MAIN_CMD_DESCRIPTION("top-commands.main.description", 0),
     SETUP_CMD_DESCRIPTION("top-commands.setup.description", 0),
     // generic-command-message
     TOO_MANY_PARAMS("generic-command-message.too-many-parameters", 0),
     NOT_ENOUGH_PARAMS("generic-command-message.not-enough-parameters", 0),
     // messages
     QUEUE_DEATH("messages.removed-from-queue-because-death", 0),
     NO_CLUE_COMMAND("messages.no-clue-what-command", 1),
     NOT_FULLY_ENABLED("messages.not-fully-enabled", 0),
     // messages.economy-reward
     ECO_REWARD_WIN("messages.economy-reward.win", 1),
     ECO_REWARD_KILL("messages.economy-reward.kill", 2),
     // messages.kits
     KITS_KIT_LIST("messages.kits.kit-list", 1),
     KITS_KIT_LIST_COMMA("messages.kits.kit-list-comma", 0),
     KITS_KIT_LIST_COST_ITEM("messages.kits.kit-list-item-cost", 2),
     KITS_CHOOSE_A_KIT("messages.kits.choose-a-kit", 0),
     KITS_APPLIED_KIT("messages.kits.applied-kit", 1),
     KITS_APPLIED_KIT_WITH_COST("messages.kits.applied-kit-with-cost", 2),
     KITS_NOT_ENOUGH_MONEY("messages.kits.not-enough-money", 1),
     // game.death
     GAME_DEATH_KILLED_BY_PLAYER_AND_VOID("game.death.killed-by-player-and-void", 2),
     GAME_DEATH_KILLED_BY_VOID("game.death.killed-by-void", 1),
     GAME_DEATH_KILLED_BY_PLAYER("game.death.killed-by-player", 2),
     GAME_DEATH_KILLED_BY_ENVIRONMENT("game.death.killed-by-environment", 1),
     GAME_DEATH_FORFEITED_WHILE_ATTACKED("game.death.forfeited-while-attacked", 2),
     GAME_DEATH_FORFEITED("game.death.forfeited", 2),
     // game.winning
     GAME_WINNING_SINGLE_WON("game.winning.single-won", 1),
     GAME_WINNING_MULTI_WON("game.winning.multi-won", 1),
     GAME_WINNING_MULTI_WON_COMMA("game.winning.multi-won-comma", 0),
     GAME_WINNING_NONE_WON("game.winning.none-won", 0),
     // game.starting
     GAME_STARTING_GAMESTARTING("game.starting.game-starting", 1),
     GAME_STARTING_GAMESTARTING_COMMA("game.starting.game-starting-comma", 0),;
     public static final int VERSION = 1;
     public final String key;
     public final int args;
 }
