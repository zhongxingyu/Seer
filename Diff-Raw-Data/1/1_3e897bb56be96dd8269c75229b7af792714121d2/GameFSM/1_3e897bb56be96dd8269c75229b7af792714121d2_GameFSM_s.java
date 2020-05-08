 package com.difane.games.ticktacktoe;
 
 import com.difane.games.ticktacktoe.exceptions.GameBoardImpossibleException;
 import com.difane.games.ticktacktoe.exceptions.GameBoardLineLengthException;
 import com.difane.games.ticktacktoe.exceptions.GameBoardLinePointsCountException;
 import com.difane.games.ticktacktoe.exceptions.GameBoardLinePositionException;
 import com.difane.games.ticktacktoe.exceptions.GameBoardLineRequirementsException;
 import com.livescribe.afp.PageInstance;
 import com.livescribe.display.BrowseList;
 import com.livescribe.event.HWRListener;
 import com.livescribe.event.StrokeListener;
 import com.livescribe.geom.Point;
 import com.livescribe.geom.PolyLine;
 import com.livescribe.geom.Rectangle;
 import com.livescribe.geom.Stroke;
 import com.livescribe.icr.ICRContext;
 import com.livescribe.icr.Resource;
 import com.livescribe.penlet.Region;
 import com.livescribe.storage.StrokeStorage;
 
 public class GameFSM implements StrokeListener, HWRListener {
 
 	/*
 	 * DI Container
 	 */
 	private Container container;
 	
 	/*
 	 * Available game states
 	 */
 	static public final int FSM_STATE_UNDEFINED = -1;
 	static public final int FSM_STATE_START = 0;
 	static public final int FSM_STATE_MAIN_MENU_START_GAME = 1;
 	static public final int FSM_STATE_MAIN_MENU_HELP = 2;
 	static public final int FSM_STATE_HELP_MENU_RULES = 3;
 	static public final int FSM_STATE_HELP_MENU_RULES_DISPLAYED = 4;
 	static public final int FSM_STATE_HELP_MENU_HOW_TO_DRAW_BOARD = 5;
 	static public final int FSM_STATE_HELP_MENU_HOW_TO_DRAW_BOARD_DISPLAYED = 6;
 	static public final int FSM_STATE_HELP_MENU_HOW_TO_PLAY = 7;
 	static public final int FSM_STATE_HELP_MENU_HOW_TO_PLAY_DISPLAYED = 8;
 	static public final int FSM_STATE_MAIN_MENU_ABOUT = 9;
 	static public final int FSM_STATE_MAIN_MENU_ABOUT_DISPLAYED = 10;
 	static public final int FSM_STATE_LEVEL_MENU_EASY = 11;
 	static public final int FSM_STATE_LEVEL_MENU_HARD = 12;
 	static public final int FSM_STATE_DRAW_BOARD_FIRST_VERTICAL_LINE = 13;
 	static public final int FSM_STATE_DRAW_BOARD_SECOND_VERTICAL_LINE = 14;
 	static public final int FSM_STATE_DRAW_BOARD_FIRST_HORIZONTAL_LINE = 15;
 	static public final int FSM_STATE_DRAW_BOARD_SECOND_HORIZONTAL_LINE = 16;
 	static public final int FSM_STATE_GAME_SELECT_PLAYER_ORDER = 17;
 	static public final int FSM_STATE_GAME_HUMAN_TURN = 18;
 	static public final int FSM_STATE_GAME_PEN_TURN = 19;
 	static public final int FSM_STATE_GAME_END_HUMAN_WINS = 20;
 	static public final int FSM_STATE_GAME_END_PEN_WINS = 21;
 	static public final int FSM_STATE_GAME_END_DRAW = 22;
 	static public final int FSM_STATE_END = 23;
 
 	/**
 	 * Current game state
 	 */
 	private int currentState = FSM_STATE_UNDEFINED;
 
 	/**
 	 * Context for an ICR
 	 */
 	protected ICRContext icrContext;
 	
 	/**
 	 * Next event, that must be handled after transition
 	 */
 	private int nextEvent = NEXT_EVENT_NONE;
 
 	/*
 	 * Available next events, that can be handled diring and right after
 	 * transition
 	 */
 	static public final int NEXT_EVENT_NONE = -1;
 	static public final int NEXT_EVENT_PLAYER_SELECTED_HUMAN_TURN_NEXT = 0;
 	static public final int NEXT_EVENT_PLAYER_SELECTED_PEN_TURN_NEXT = 1;
 	static public final int NEXT_EVENT_GAME_PEN_TURN_READY = 2;
 	static public final int NEXT_EVENT_GAME_END_HUMAN_WINS = 3;
 	static public final int NEXT_EVENT_GAME_END_PEN_WINS = 4;
 	static public final int NEXT_EVENT_GAME_END_DRAW = 5;
 	static public final int NEXT_EVENT_END = 6;
 
 	/**
 	 * Constructor
 	 */
 	public GameFSM(Container c) {
 		this.container = c;
 		
 		this.currentState = FSM_STATE_START;
 		
 		this.getContainer()
 			.getLoggerComponent()
 			.debug("[GameFSM] Component initialized");
 	}
 
 	/**
 	 * This event must be called, when application will be started. It will try
 	 * to start application by displaying it's main menu
 	 */
 	public void eventStartApplication() {
 		this.getContainer().getLoggerComponent().debug("[GameFSM] eventStartApplication received");
 
 		initializeICRContext();
 
 		transition(currentState, FSM_STATE_MAIN_MENU_START_GAME);
 	}
 
 	/**
 	 * This event must be called, when DOWN in the menu be pressed
 	 */
 	public boolean eventMenuDown() {
 		this.getContainer().getLoggerComponent().debug("[GameFSM] eventMenuDown received");
 		switch (currentState) {
 		case FSM_STATE_MAIN_MENU_START_GAME:
 			transition(currentState, FSM_STATE_MAIN_MENU_HELP);
 			break;
 		case FSM_STATE_MAIN_MENU_HELP:
 			transition(currentState, FSM_STATE_MAIN_MENU_ABOUT);
 			break;
 		case FSM_STATE_HELP_MENU_RULES:
 			transition(currentState, FSM_STATE_HELP_MENU_HOW_TO_DRAW_BOARD);
 			break;
 		case FSM_STATE_HELP_MENU_HOW_TO_DRAW_BOARD:
 			transition(currentState, FSM_STATE_HELP_MENU_HOW_TO_PLAY);
 			break;
 		case FSM_STATE_LEVEL_MENU_EASY:
 			transition(currentState, FSM_STATE_LEVEL_MENU_HARD);
 			break;
 		default:
 			this.getContainer().getLoggerComponent().warn("[GameFSM] Unexpected eventMenuDown received");
 		}
 
 		// Menu down are always handled
 		return true;
 	}
 
 	/**
 	 * This event must be called, when UP in the menu be pressed
 	 */
 	public boolean eventMenuUp() {
 		this.getContainer().getLoggerComponent().debug("[GameFSM] eventMenuUp received");
 		switch (currentState) {
 		case FSM_STATE_MAIN_MENU_HELP:
 			transition(currentState, FSM_STATE_MAIN_MENU_START_GAME);
 			break;
 		case FSM_STATE_MAIN_MENU_ABOUT:
 			transition(currentState, FSM_STATE_MAIN_MENU_HELP);
 			break;
 		case FSM_STATE_HELP_MENU_HOW_TO_DRAW_BOARD:
 			transition(currentState, FSM_STATE_HELP_MENU_RULES);
 			break;
 		case FSM_STATE_HELP_MENU_HOW_TO_PLAY:
 			transition(currentState, FSM_STATE_HELP_MENU_HOW_TO_DRAW_BOARD);
 			break;
 		case FSM_STATE_LEVEL_MENU_HARD:
 			transition(currentState, FSM_STATE_LEVEL_MENU_EASY);
 			break;
 		default:
 			this.getContainer().getLoggerComponent().warn("[GameFSM] Unexpected eventMenuUp received");
 		}
 
 		// Menu up are always handled
 		return true;
 	}
 
 	/**
 	 * This event must be called, when LEFT in the menu be pressed
 	 */
 	public boolean eventMenuLeft() {
 		this.getContainer().getLoggerComponent().debug("[GameFSM] eventMenuLeft received");
 
 		boolean result = false;
 
 		switch (currentState) {
 		case FSM_STATE_LEVEL_MENU_EASY:
 		case FSM_STATE_LEVEL_MENU_HARD:
 			transition(currentState, FSM_STATE_MAIN_MENU_START_GAME);
 			result = true;
 			break;
 		case FSM_STATE_HELP_MENU_RULES:
 		case FSM_STATE_HELP_MENU_HOW_TO_DRAW_BOARD:
 		case FSM_STATE_HELP_MENU_HOW_TO_PLAY:
 			transition(currentState, FSM_STATE_MAIN_MENU_HELP);
 			result = true;
 			break;
 		case FSM_STATE_MAIN_MENU_ABOUT_DISPLAYED:
 			transition(currentState, FSM_STATE_MAIN_MENU_ABOUT);
 			result = true;
 			break;
 
 		case FSM_STATE_HELP_MENU_RULES_DISPLAYED:
 			transition(currentState, FSM_STATE_HELP_MENU_RULES);
 			result = true;
 			break;
 		case FSM_STATE_HELP_MENU_HOW_TO_DRAW_BOARD_DISPLAYED:
 			transition(currentState, FSM_STATE_HELP_MENU_HOW_TO_DRAW_BOARD);
 			result = true;
 			break;
 		case FSM_STATE_HELP_MENU_HOW_TO_PLAY_DISPLAYED:
 			transition(currentState, FSM_STATE_HELP_MENU_HOW_TO_PLAY);
 			result = true;
 			break;
 		default:
 			result = false;
 			this.getContainer().getLoggerComponent().warn("[GameFSM] Unexpected eventMenuLeft received");
 		}
 
 		return result;
 	}
 
 	/**
 	 * This event must be called, when RIGHT in the menu be pressed
 	 */
 	public boolean eventMenuRight() {
 		this.getContainer().getLoggerComponent().debug("[GameFSM] eventMenuRight received");
 
 		switch (currentState) {
 		case FSM_STATE_MAIN_MENU_START_GAME:
 			transition(currentState, FSM_STATE_LEVEL_MENU_EASY);
 			break;
 		case FSM_STATE_MAIN_MENU_HELP:
 			transition(currentState, FSM_STATE_HELP_MENU_RULES);
 			break;
 		case FSM_STATE_MAIN_MENU_ABOUT:
 			transition(currentState, FSM_STATE_MAIN_MENU_ABOUT_DISPLAYED);
 			break;
 		case FSM_STATE_HELP_MENU_RULES:
 			transition(currentState, FSM_STATE_HELP_MENU_RULES_DISPLAYED);
 			break;
 		case FSM_STATE_HELP_MENU_HOW_TO_DRAW_BOARD:
 			transition(currentState,
 					FSM_STATE_HELP_MENU_HOW_TO_DRAW_BOARD_DISPLAYED);
 			break;
 		case FSM_STATE_HELP_MENU_HOW_TO_PLAY:
 			transition(currentState, FSM_STATE_HELP_MENU_HOW_TO_PLAY_DISPLAYED);
 			break;
 		case FSM_STATE_LEVEL_MENU_EASY:
 			this.getContainer().getGameLogicComponent().setAiLevel(GameLogic.AI_LEVEL_EASY);
 			transition(currentState, FSM_STATE_DRAW_BOARD_FIRST_VERTICAL_LINE);
 			break;
 		case FSM_STATE_LEVEL_MENU_HARD:
 			this.getContainer().getGameLogicComponent().setAiLevel(GameLogic.AI_LEVEL_HARD);
 			transition(currentState, FSM_STATE_DRAW_BOARD_FIRST_VERTICAL_LINE);
 			break;
 		default:
 			this.getContainer().getLoggerComponent().warn("[GameFSM] Unexpected eventMenuRight received");
 		}
 		
 		// Menu right are always handled
 		return true;
 	}
 
 	/**
 	 * This event must be called, when first board vertical line be ready
 	 */
 	public void eventFirstVerticalLineReady() {
 		this.getContainer().getLoggerComponent().debug("[GameFSM] eventFirstVerticalLineReady received");
 		if (currentState == FSM_STATE_DRAW_BOARD_FIRST_VERTICAL_LINE) {
 			transition(currentState, FSM_STATE_DRAW_BOARD_SECOND_VERTICAL_LINE);
 		}
 	}
 
 	/**
 	 * This event must be called, when second board vertical line be ready
 	 */
 	public void eventSecondVerticalLineReady() {
 		this.getContainer().getLoggerComponent().debug("[GameFSM] eventSecondVerticalLineReady received");
 		if (currentState == FSM_STATE_DRAW_BOARD_SECOND_VERTICAL_LINE) {
 			transition(currentState, FSM_STATE_DRAW_BOARD_FIRST_HORIZONTAL_LINE);
 		}
 	}
 
 	/**
 	 * This event must be called, when first board horizontal line be ready
 	 */
 	public void eventFirstHorizontalLineReady() {
 		this.getContainer().getLoggerComponent().debug("[GameFSM] eventFirstHorizontalLineReady received");
 		if (currentState == FSM_STATE_DRAW_BOARD_FIRST_HORIZONTAL_LINE) {
 			transition(currentState,
 					FSM_STATE_DRAW_BOARD_SECOND_HORIZONTAL_LINE);
 		}
 	}
 
 	/**
 	 * This event must be called, when second board horizontal line be ready
 	 */
 	public void eventSecondHorizontalLineReady() {
 		this.getContainer().getLoggerComponent().debug("[GameFSM] eventSecondHorizontalLineReady received");
 		if (currentState == FSM_STATE_DRAW_BOARD_SECOND_HORIZONTAL_LINE) {
 			transition(currentState, FSM_STATE_GAME_SELECT_PLAYER_ORDER);
 		}
 	}
 
 	/**
 	 * This event must be called, when player order is selected and next turn is
 	 * human's
 	 */
 	public void eventPlayerSelectedHumanTurnNext() {
 		this.getContainer().getLoggerComponent().debug("[GameFSM] eventPlayerSelectedHumanTurnNext received");
 		if (currentState == FSM_STATE_GAME_SELECT_PLAYER_ORDER) {
 			this.getContainer().getGameDisplayComponent().displayHumanStartsGame();
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				// If no sleep will made - it is not error at this time
 			}
 			this.transition(currentState, FSM_STATE_GAME_HUMAN_TURN);
 		}
 	}
 
 	/**
 	 * This event must be called, when player order is selected and next turn is
 	 * pen's
 	 */
 	public void eventPlayerSelectedPenTurnNext() {
 		this.getContainer().getLoggerComponent().debug("[GameFSM] eventPlayerSelectedPenTurnNext received");
 		if (currentState == FSM_STATE_GAME_SELECT_PLAYER_ORDER) {
 			this.getContainer().getGameDisplayComponent().displayPenStartsGame();
 			this.transition(currentState, FSM_STATE_GAME_PEN_TURN);
 		}
 	}
 
 	/**
 	 * This event must be called, when player makes his turn
 	 */
 	public void eventHumanTurnReady() {
 		this.getContainer().getLoggerComponent().debug("[GameFSM] eventHumanTurnReady received");
 		if (currentState == FSM_STATE_GAME_HUMAN_TURN) {
 			this.transition(currentState, FSM_STATE_GAME_PEN_TURN);
 		}
 	}
 
 	/**
 	 * This event must be called, when pen makes his turn
 	 */
 	public void eventPenTurnReady() {
 		this.getContainer().getLoggerComponent().debug("[GameFSM] eventPenTurnReady received");
 		if (currentState == FSM_STATE_GAME_PEN_TURN) {
 			this.transition(currentState, FSM_STATE_GAME_HUMAN_TURN);
 		}
 	}
 
 	/**
 	 * This event must be called, when game ends and human wins
 	 */
 	public void eventHumanWins() {
 		this.getContainer().getLoggerComponent().debug("[GameFSM] eventHumanWins received");
 		if (currentState == FSM_STATE_GAME_HUMAN_TURN) {
 			this.transition(currentState, FSM_STATE_GAME_END_HUMAN_WINS);
 		} else {
 			
 			this.getContainer().getLoggerComponent()
 					.error("[GameFSM] Impossible situation - human wins without making a turn");
 		}
 	}
 
 	/**
 	 * This event must be called, when game ends and pen wins
 	 */
 	public void eventPenWins() {
 		this.getContainer().getLoggerComponent().debug("[GameFSM] eventPenWins received");
 		
 		// Pen can win during human turn also
 		if (currentState == FSM_STATE_GAME_PEN_TURN
 				|| currentState == FSM_STATE_GAME_HUMAN_TURN) {
 			this.transition(currentState, FSM_STATE_GAME_END_PEN_WINS);
 		} else {
 			this.getContainer().getLoggerComponent()
 					.error("[GameFSM] Impossible situation - pen wins without making a turn");
 		}
 	}
 
 	/**
 	 * This event must be called, when game ends and draw appears
 	 */
 	public void eventDraw() {
 		this.getContainer().getLoggerComponent().debug("[GameFSM] eventDraw received");
 
 		if (currentState == FSM_STATE_GAME_HUMAN_TURN
 				|| currentState == FSM_STATE_GAME_PEN_TURN) {
 			this.transition(currentState, FSM_STATE_GAME_END_DRAW);
 		}
 	}
 
 	/**
 	 * This event ends application
 	 */
 	public void eventEndApplication() {
 		this.getContainer().getLoggerComponent().debug("[GameFSM] eventEndApplication received");
 
 		this.destroyICRContext();
 		
 		if (currentState == FSM_STATE_GAME_END_HUMAN_WINS
 				|| currentState == FSM_STATE_GAME_END_PEN_WINS
 				|| currentState == FSM_STATE_GAME_END_DRAW) {
 			this.transition(currentState, FSM_STATE_END);
 		}
 	}
 
 	
 
 	private void transition(int currentState, int transitionState) {
 		this.getContainer().getLoggerComponent().debug("[GameFSM] ---> transition started ( " + currentState
 				+ " -> " + transitionState + " )");
 		try {
 			switch (transitionState) {
 			case FSM_STATE_MAIN_MENU_START_GAME:
 				if (currentState == FSM_STATE_START
 						|| currentState == FSM_STATE_LEVEL_MENU_EASY
 						|| currentState == FSM_STATE_LEVEL_MENU_HARD) {
 					// Select "Help" in the main menu, if not selected
 					this.getContainer().getGameDisplayComponent().selectMainMenuItemIfNotSelected(0);
 
 					// Main menu must be displayed
 					this.getContainer().getGameDisplayComponent().displayMainMenu();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] Main menu was displayed with active item 0");
 
 				} else if (currentState == FSM_STATE_MAIN_MENU_HELP) {
 					// Main menu must be focused to the previous item
 					this.getContainer().getGameDisplayComponent().focusMainMenuToPrevious();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] Main menu item 0 was activated");
 				}
 				break;
 			case FSM_STATE_MAIN_MENU_HELP:
 				if (currentState == FSM_STATE_MAIN_MENU_START_GAME) {
 					// Main menu must be focused to the next item
 					this.getContainer().getGameDisplayComponent().focusMainMenuToNext();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] Main menu item 1 was activated");
 				} else if (currentState == FSM_STATE_MAIN_MENU_ABOUT) {
 					// Main menu must be focused to the previous item
 					this.getContainer().getGameDisplayComponent().focusMainMenuToPrevious();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] Main menu item 1 was activated");
 				} else if (currentState == FSM_STATE_HELP_MENU_RULES
 						|| currentState == FSM_STATE_HELP_MENU_HOW_TO_DRAW_BOARD
 						|| currentState == FSM_STATE_HELP_MENU_HOW_TO_PLAY) {
 					// Select "Help" in the main menu, if not selected
 					this.getContainer().getGameDisplayComponent().selectMainMenuItemIfNotSelected(1);
 
 					// Main menu must be displayed
 					this.getContainer().getGameDisplayComponent().displayMainMenu();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] Main menu was displayed with active item 1");
 				}
 				break;
 			case FSM_STATE_MAIN_MENU_ABOUT:
 				if (currentState == FSM_STATE_MAIN_MENU_HELP) {
 					// Main menu must be focused to the next item
 					this.getContainer().getGameDisplayComponent().focusMainMenuToNext();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] Main menu item 2 was activated");
 				} else if (currentState == FSM_STATE_MAIN_MENU_ABOUT_DISPLAYED) {
 					// Select "About" in the main menu, if not selected
 					this.getContainer().getGameDisplayComponent().selectMainMenuItemIfNotSelected(2);
 
 					// Main menu must be displayed
 					this.getContainer().getGameDisplayComponent().displayMainMenu();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] Main menu was displayed with active item 2");
 				}
 				break;
 			case FSM_STATE_MAIN_MENU_ABOUT_DISPLAYED:
 				if (currentState == FSM_STATE_MAIN_MENU_ABOUT) {
 					this.getContainer().getGameDisplayComponent().displayAbout();
 					this.getContainer().getLoggerComponent().debug("[GameFSM] About was displayed");
 				}
 				break;
 			case FSM_STATE_LEVEL_MENU_EASY:
 				if (currentState == FSM_STATE_MAIN_MENU_START_GAME) {
 					// Select "Easy" in the level select menu, if not selected
 					this.getContainer().getGameDisplayComponent().selectLevelSelectMenuItemIfNotSelected(0);
 
 					// Level select menu must be displayed
 					this.getContainer().getGameDisplayComponent().displayLevelSelectMenu();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] Level select menu was displayed");
 				} else if (currentState == FSM_STATE_LEVEL_MENU_HARD) {
 					// Main menu must be focused to the next item
 					this.getContainer().getGameDisplayComponent().focusLevelSelectMenuToPrevious();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] Level select menu item 0 was activated");
 				}
 				break;
 			case FSM_STATE_LEVEL_MENU_HARD:
 				if (currentState == FSM_STATE_LEVEL_MENU_EASY) {
 					this.getContainer().getGameDisplayComponent().focusLevelSelectMenuToNext();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] Level select menu item 1 was activated");
 				}
 				break;
 			case FSM_STATE_HELP_MENU_RULES:
 				if (currentState == FSM_STATE_MAIN_MENU_HELP
 						|| currentState == FSM_STATE_HELP_MENU_RULES_DISPLAYED) {
 					// Select "Rules" in the help menu, if not selected
 					this.getContainer().getGameDisplayComponent().selectHelpMenuItemIfNotSelected(0);
 
 					// Level select menu must be displayed
 					this.getContainer().getGameDisplayComponent().displayHelpMenu();
 					this.getContainer().getLoggerComponent().debug("[GameFSM] Help menu was displayed");
 				} else if (currentState == FSM_STATE_HELP_MENU_HOW_TO_DRAW_BOARD) {
 					// Help menu must be focused to the previous item
 					this.getContainer().getGameDisplayComponent().focusHelpMenuToPrevious();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] Help menu item 0 was activated");
 				}
 				break;
 			case FSM_STATE_HELP_MENU_HOW_TO_DRAW_BOARD:
 				if (currentState == FSM_STATE_HELP_MENU_HOW_TO_DRAW_BOARD_DISPLAYED) {
 					// Select "How to draw board" in the help menu, if not
 					// selected
 					this.getContainer().getGameDisplayComponent().selectHelpMenuItemIfNotSelected(1);
 
 					// Level select menu must be displayed
 					this.getContainer().getGameDisplayComponent().displayHelpMenu();
 					this.getContainer().getLoggerComponent().debug("[GameFSM] Help menu was displayed");
 				} else if (currentState == FSM_STATE_HELP_MENU_RULES) {
 					// Help menu must be focused to the next item
 					this.getContainer().getGameDisplayComponent().focusHelpMenuToNext();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] Help menu item 1 was activated");
 				} else if (currentState == FSM_STATE_HELP_MENU_HOW_TO_PLAY) {
 					// Help menu must be focused to the next item
 					this.getContainer().getGameDisplayComponent().focusHelpMenuToPrevious();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] Help menu item 1 was activated");
 				}
 				break;
 			case FSM_STATE_HELP_MENU_HOW_TO_PLAY:
 				if (currentState == FSM_STATE_HELP_MENU_HOW_TO_PLAY_DISPLAYED) {
 					// Select "How to play" in the help menu, if not selected
 					this.getContainer().getGameDisplayComponent().selectHelpMenuItemIfNotSelected(2);
 
 					// Level select menu must be displayed
 					this.getContainer().getGameDisplayComponent().displayHelpMenu();
 					this.getContainer().getLoggerComponent().debug("[GameFSM] Help menu was displayed");
 				} else if (currentState == FSM_STATE_HELP_MENU_HOW_TO_DRAW_BOARD) {
 					// Help menu must be focused to the next item
 					this.getContainer().getGameDisplayComponent().focusHelpMenuToNext();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] Help menu item 2 was activated");
 				}
 				break;
 			case FSM_STATE_HELP_MENU_RULES_DISPLAYED:
 				if (currentState == FSM_STATE_HELP_MENU_RULES) {
 					this.getContainer().getGameDisplayComponent().displayRules();
 					this.getContainer().getLoggerComponent().debug("[GameFSM] Rules was displayed");
 				}
 				break;
 			case FSM_STATE_HELP_MENU_HOW_TO_DRAW_BOARD_DISPLAYED:
 				if (currentState == FSM_STATE_HELP_MENU_HOW_TO_DRAW_BOARD) {
 					this.getContainer().getGameDisplayComponent().displayHowToDrawBoard();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] How to draw board was displayed");
 				}
 				break;
 			case FSM_STATE_HELP_MENU_HOW_TO_PLAY_DISPLAYED:
 				if (currentState == FSM_STATE_HELP_MENU_HOW_TO_PLAY) {
 					this.getContainer().getGameDisplayComponent().displayHowToPlay();
 					this.getContainer().getLoggerComponent().debug("[GameFSM] How to play was displayed");
 				}
 				break;
 			case FSM_STATE_DRAW_BOARD_FIRST_VERTICAL_LINE:
 				if (currentState == FSM_STATE_LEVEL_MENU_EASY
 						|| currentState == FSM_STATE_LEVEL_MENU_HARD) {
 					this.getContainer().getGameDisplayComponent().displayDrawFirstVerticalLine();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] Draw first vertical line was displayed");
 				}
 				break;
 			case FSM_STATE_DRAW_BOARD_SECOND_VERTICAL_LINE:
 				if (currentState == FSM_STATE_DRAW_BOARD_FIRST_VERTICAL_LINE) {
 					this.getContainer().getGameDisplayComponent().displayDrawSecondVerticalLine();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] Draw second vertical line was displayed");
 				}
 				break;
 			case FSM_STATE_DRAW_BOARD_FIRST_HORIZONTAL_LINE:
 				if (currentState == FSM_STATE_DRAW_BOARD_SECOND_VERTICAL_LINE) {
 					this.getContainer().getGameDisplayComponent().displayDrawFirstHorizontalLine();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] Draw first horizontal line was displayed");
 				}
 				break;
 			case FSM_STATE_DRAW_BOARD_SECOND_HORIZONTAL_LINE:
 				if (currentState == FSM_STATE_DRAW_BOARD_FIRST_HORIZONTAL_LINE) {
 					this.getContainer().getGameDisplayComponent().displayDrawSecondHorizontalLine();
 					this.getContainer().getLoggerComponent()
 							.debug("[GameFSM] Draw second horizontal line was displayed");
 				}
 				break;
 			case FSM_STATE_GAME_SELECT_PLAYER_ORDER:
 				if (currentState == FSM_STATE_DRAW_BOARD_SECOND_HORIZONTAL_LINE) {
 					boolean humanFirst = this.getContainer()
 							.getGameLogicComponent().selectPlayersOrder();
 					if (humanFirst) {
 						this.setNextEvent(NEXT_EVENT_PLAYER_SELECTED_HUMAN_TURN_NEXT);
 					} else {
 						this.setNextEvent(NEXT_EVENT_PLAYER_SELECTED_PEN_TURN_NEXT);
 					}
 				}
 				break;
 			case FSM_STATE_GAME_HUMAN_TURN:
 				if (currentState == FSM_STATE_GAME_SELECT_PLAYER_ORDER
 						|| currentState == FSM_STATE_GAME_PEN_TURN) {
 					// 1. Checking, that game is not end
 					if (this.checkGameStatus()) {
 						// 2. Redrawing board with text "Your turn"
 						this.getContainer().getGameDisplayComponent().redrawBoard(true);
 					}
 				}
 				break;
 			case FSM_STATE_GAME_PEN_TURN:
 				if (currentState == FSM_STATE_GAME_SELECT_PLAYER_ORDER
 						|| currentState == FSM_STATE_GAME_HUMAN_TURN) {
 					// 1. Checking, that game is not end
 					if (this.checkGameStatus()) {
 						// 2. Redrawing board with text "Your turn"
 						this.getContainer().getGameDisplayComponent().redrawBoard(false);
 						// 3. Performing pen turn;
 						this.getContainer().getGameLogicComponent().aiTurn();
 						this.setNextEvent(NEXT_EVENT_GAME_PEN_TURN_READY);
 					}
 				}
 				break;
 			case FSM_STATE_GAME_END_HUMAN_WINS:
 				this.getContainer().getGameDisplayComponent().displayHumanWins();
 				this.getContainer().getLoggerComponent().debug("[GameFSM] Human wins was displayed");
 				Thread.sleep(2000);
 				this.setNextEvent(NEXT_EVENT_END);
 				break;
 			case FSM_STATE_GAME_END_PEN_WINS:
 				this.getContainer().getGameDisplayComponent().displayPenWins();
 				this.getContainer().getLoggerComponent().debug("[GameFSM] Pen wins was displayed");
 				Thread.sleep(2000);
 				this.setNextEvent(NEXT_EVENT_END);
 				break;
 			case FSM_STATE_GAME_END_DRAW:
 				this.getContainer().getGameDisplayComponent().displayDraw();
 				this.getContainer().getLoggerComponent().debug("[GameFSM] Draw was displayed");
 				Thread.sleep(2000);
 				this.setNextEvent(NEXT_EVENT_END);
 				break;
 			case FSM_STATE_END:
 				this.getContainer().getGameDisplayComponent().displayEnd();
 				this.getContainer().getLoggerComponent().debug("[GameFSM] Game end reached");
 			default:
 				// Unrecognized target state. Rejecting it
 				this.getContainer().getLoggerComponent().warn("[GameFSM] Unrecognized target state: "
 						+ transitionState);
 				return;
 			} // switch (transitionState)
 
 			this.currentState = transitionState;
 		} catch (Exception e) {
 			this.getContainer().getLoggerComponent().error("[GameFSM] Exception appears: " + e);
 		}
 
 		this.getContainer().getLoggerComponent().debug("[GameFSM] Starting processing next event");
 		this.processNextEvent();
 		this.getContainer().getLoggerComponent().debug("[GameFSM] Next event was processed");
 
 		this.getContainer().getLoggerComponent().debug("[GameFSM] <--- transition");
 	}
 
 	
 
 	/**
 	 * Checks game status and forwards to the corresponded state, if game
 	 * completed
 	 * @return false, if game status changed and game end, true otherwise
 	 */
 	private boolean checkGameStatus() {
 		int status = this.getContainer().getGameLogicComponent().getGameStatus();
 		this.getContainer().getLoggerComponent().debug("[GameFSM].checkGameStatus. Game status is: "
 				+ status + ". Current state is" + currentState);
 
 		boolean result = true;
 		
 		switch (status) {
 		case GameLogic.GAME_STATUS_X_WINS:
 			this.getContainer().getLoggerComponent().debug("[GameFSM].checkGameStatus. 'X' player wins");
 			if (this.getContainer().getGameLogicComponent().getHumanType() == GameLogic.FIELD_X) {
 				this.getContainer().getLoggerComponent()
 						.debug("[GameFSM].checkGameStatus. 'X' was played by human. Human wins");
 				// Human wins
 				this.setNextEvent(NEXT_EVENT_GAME_END_HUMAN_WINS);
 			} else {
 				this.getContainer().getLoggerComponent()
 						.debug("[GameFSM].checkGameStatus. 'X' was played by pen. Pen wins");
 				// Pen wins
 				this.setNextEvent(NEXT_EVENT_GAME_END_PEN_WINS);
 			}
 			result = false;
 			break;
 		case GameLogic.GAME_STATUS_O_WINS:
 			this.getContainer().getLoggerComponent().debug("[GameFSM].checkGameStatus. 'O' player wins");
 			if (this.getContainer().getGameLogicComponent().getHumanType() == GameLogic.FIELD_O) {
 				this.getContainer().getLoggerComponent()
 						.debug("[GameFSM].checkGameStatus. 'O' was played by human. Human wins");
 				// Human wins
 				this.setNextEvent(NEXT_EVENT_GAME_END_HUMAN_WINS);
 			} else {
 				this.getContainer().getLoggerComponent()
 						.debug("[GameFSM].checkGameStatus. 'O' was played by pen. Pen wins");
 				// Pen wins
 				this.setNextEvent(NEXT_EVENT_GAME_END_PEN_WINS);
 			}
 			result = false;
 			break;
 		case GameLogic.GAME_STATUS_DRAW:
 			this.getContainer().getLoggerComponent().debug("[GameFSM].checkGameStatus. Game draw");
 			this.setNextEvent(NEXT_EVENT_GAME_END_DRAW);
 			result = false;
 			break;
 		default:
 			this.getContainer().getLoggerComponent()
 					.debug("[GameFSM].checkGameStatus. Game is not completed yet");
 			// Game continues. Do nothing
 			result = true;
 			break;
 		}
 		
 		return result;
 	}
 
 	
 
 	
 
 	
 
 	public void strokeCreated(long time, Region region,
 			PageInstance pageInstance) {
 		this.getContainer().getLoggerComponent()
 				.debug("[GameFSM] New stroke was created. Current state: "
 						+ this.currentState);
 
 		PolyLine line = null;
 
 		if (currentState == FSM_STATE_DRAW_BOARD_FIRST_VERTICAL_LINE
 				|| currentState == FSM_STATE_DRAW_BOARD_SECOND_VERTICAL_LINE
 				|| currentState == FSM_STATE_DRAW_BOARD_FIRST_HORIZONTAL_LINE
 				|| currentState == FSM_STATE_DRAW_BOARD_SECOND_HORIZONTAL_LINE) {
 
 			this.getContainer().getLoggerComponent().debug("[GameFSM] Trying to get line from stroke");
 
 			StrokeStorage ss = new StrokeStorage(pageInstance);
 			Stroke stroke = ss.getStroke(time);
 
 			// Create a line, based on the first and last points of the stroke
 			int numPoints = stroke.getNumberofVertices();
 			this.getContainer().getLoggerComponent().debug("[GameFSM] Number of vertices in the stroke is "
 					+ numPoints);
 
 			if (numPoints >= 2) {
 				line = new PolyLine(2);
 				line.setXY(0, stroke.getX(0), stroke.getY(0));
 				line.setXY(1, stroke.getX(numPoints - 1), stroke
 						.getY(numPoints - 1));
 				
 				this.getContainer().getLoggerComponent().debug("[GameFSM] Creating line from two points: "+line);
 			}
 		}
 
 		if (currentState == FSM_STATE_DRAW_BOARD_FIRST_VERTICAL_LINE) {
 			try {
 				this.getContainer().getLoggerComponent()
 						.debug("[GameFSM] Trying to use this line as first vertical line");
 				this.getContainer().getGameBoardComponent().setFirstVerticalLine(line);
 				this.getContainer().getLoggerComponent()
 						.debug("[GameFSM] First vertical line was successfully created");
 				this.eventFirstVerticalLineReady();
 			} catch (GameBoardLinePointsCountException e) {
 				// TODO Auto-generated catch block
 				this.getContainer().getLoggerComponent().error("GameBoardLinePointsCountException");
 				this.getContainer().getGameDisplayComponent().displayErrorDrawFirstVerticalLine();
 			} catch (GameBoardLineLengthException e) {
 				// TODO Auto-generated catch block
 				this.getContainer().getLoggerComponent()
 						.error("GameBoardLineLengthException. Reason: "
 								+ e.getReason());
 				this.getContainer().getGameDisplayComponent().displayErrorDrawFirstVerticalLine();
 			}
 		} else if (currentState == FSM_STATE_DRAW_BOARD_SECOND_VERTICAL_LINE) {
 			try {
 				this.getContainer().getLoggerComponent()
 						.debug("[GameFSM] Trying to use this line as second vertical line");
 				this.getContainer().getGameBoardComponent().setSecondVerticalLine(line);
 				this.getContainer().getLoggerComponent()
 						.debug("[GameFSM] Second vertical line was successfully created");
 				this.eventSecondVerticalLineReady();
 			} catch (GameBoardLinePointsCountException e) {
 				// TODO Auto-generated catch block
 				this.getContainer().getLoggerComponent().error("GameBoardLinePointsCountException");
 				this.getContainer().getGameDisplayComponent().displayErrorDrawSecondVerticalLine();
 			} catch (GameBoardLineRequirementsException e) {
 				// TODO Auto-generated catch block
 				this.getContainer().getLoggerComponent().error("GameBoardLineRequirementsException");
 				this.getContainer().getGameDisplayComponent().displayErrorDrawSecondVerticalLine();
 			} catch (GameBoardLineLengthException e) {
 				// TODO Auto-generated catch block
 				this.getContainer().getLoggerComponent()
 						.error("GameBoardLineLengthException. Reason: "
 								+ e.getReason());
 				this.getContainer().getGameDisplayComponent().displayErrorDrawSecondVerticalLine();
 			} catch (GameBoardLinePositionException e) {
 				// TODO Auto-generated catch block
 				this.getContainer().getLoggerComponent()
 						.error("GameBoardLinePositionException. Reason: "
 								+ e.getReason());
 				this.getContainer().getGameDisplayComponent().displayErrorDrawSecondVerticalLine();
 			}
 		} else if (currentState == FSM_STATE_DRAW_BOARD_FIRST_HORIZONTAL_LINE) {
 			try {
 				this.getContainer().getLoggerComponent()
 						.debug("[GameFSM] Trying to use this line as first horizontal line");
 				this.getContainer().getGameBoardComponent().setFirstHorizontalLine(line);
 				this.getContainer().getLoggerComponent()
 						.debug("[GameFSM] First horizontal line was successfully created");
 				this.eventFirstHorizontalLineReady();
 			} catch (GameBoardLineRequirementsException e) {
 				this.getContainer().getLoggerComponent().error("GameBoardLineRequirementsException");
 				this.getContainer().getGameDisplayComponent().displayErrorDrawFirstHorizontalLine();
 			} catch (GameBoardLinePointsCountException e) {
 				this.getContainer().getLoggerComponent().error("GameBoardLinePointsCountException");
 				this.getContainer().getGameDisplayComponent().displayErrorDrawFirstHorizontalLine();
 			} catch (GameBoardLinePositionException e) {
 				this.getContainer().getLoggerComponent()
 						.error("GameBoardLinePositionException. Reason: "
 								+ e.getReason());
 				this.getContainer().getGameDisplayComponent().displayErrorDrawFirstHorizontalLine();
 			} catch (GameBoardLineLengthException e) {
 				this.getContainer().getLoggerComponent()
 						.error("GameBoardLineLengthException. Reason: "
 								+ e.getReason());
 				this.getContainer().getGameDisplayComponent().displayErrorDrawFirstHorizontalLine();
 			}
 		} else if (currentState == FSM_STATE_DRAW_BOARD_SECOND_HORIZONTAL_LINE) {
 			try {
 				this.getContainer().getLoggerComponent()
 						.debug("[GameFSM] Trying to use this line as second horizontal line");
 				this.getContainer().getGameBoardComponent().setSecondHorizontalLine(line);
 				this.getContainer().getLoggerComponent()
 						.debug("[GameFSM] Second horizontal line was successfully created. Calculating game board");
 				this.getContainer().getGameBoardComponent().calculateBoard();
 				this.eventSecondHorizontalLineReady();
 			} catch (GameBoardLineRequirementsException e) {
 				this.getContainer().getLoggerComponent().error("GameBoardLineRequirementsException");
 				this.getContainer().getGameDisplayComponent().displayErrorDrawSecondHorizontalLine();
 			} catch (GameBoardLinePointsCountException e) {
 				this.getContainer().getLoggerComponent().error("GameBoardLinePointsCountException");
 				this.getContainer().getGameDisplayComponent().displayErrorDrawSecondHorizontalLine();
 			} catch (GameBoardLinePositionException e) {
 				this.getContainer().getLoggerComponent()
 						.error("GameBoardLinePositionException. Reason: "
 								+ e.getReason());
 				this.getContainer().getGameDisplayComponent().displayErrorDrawSecondHorizontalLine();
 			} catch (GameBoardLineLengthException e) {
 				this.getContainer().getLoggerComponent()
 						.error("GameBoardLineLengthException. Reason: "
 								+ e.getReason());
 				this.getContainer().getGameDisplayComponent().displayErrorDrawSecondHorizontalLine();
 			} catch (GameBoardImpossibleException e) {
 				this.getContainer().getLoggerComponent().error("GameBoardImpossibleException");
 				this.getContainer().getGameDisplayComponent().displayErrorDrawSecondHorizontalLine();
 			}
 		} else if (currentState == FSM_STATE_GAME_HUMAN_TURN) {
 			this.icrContext.addStroke(pageInstance, time);
 			this.getContainer().getLoggerComponent()
 					.debug("[GameFSM] StrokeCreated. Stroke was added to ICR context");
 		}
 	}
 
 	
 
 	
 
 	/**
 	 * Called when the user crosses out text
 	 */
 	public void hwrCrossingOut(long time, String result) {
 	}
 
 	/**
 	 * Called when an error occurs during handwriting recognition
 	 */
 	public void hwrError(long time, String error) {
 	}
 
 	/**
 	 * When the ICR engine detects an acceptable series or strokes
 	 */
 	public void hwrResult(long time, String result) {
 		this.getContainer().getLoggerComponent().debug("[GameFSM][ICR] Intermediate result: " + result);
 	}
 
 	/**
 	 * When the user pauses (pause time specified by the wizard), all strokes in
 	 * the ICRContext are cleared
 	 */
 	public void hwrUserPause(long time, String result) {
 		this.getContainer().getLoggerComponent().debug("[GameFSM][ICR] Result: " + result
 				+ " (x) | Human type: " + this.getContainer().getGameLogicComponent().getHumanType());
 
 		// At first we must check, that result is the required symbol
 		boolean symbolCorrect = false;
 
 		if (result.equalsIgnoreCase("x")) {
 			this.getContainer().getLoggerComponent().debug("[GameFSM][ICR] User has draw X");
 			if (this.getContainer().getGameLogicComponent().getHumanType() == GameLogic.FIELD_X) {
 				this.getContainer().getLoggerComponent().debug("[GameFSM][ICR] User played with X");
 				symbolCorrect = true;
 			}
 		} else if (result.equalsIgnoreCase("o")) {
 			this.getContainer().getLoggerComponent().debug("[GameFSM][ICR] User has draw O");
 			if (this.getContainer().getGameLogicComponent().getHumanType() == GameLogic.FIELD_O) {
 				this.getContainer().getLoggerComponent().debug("[GameFSM][ICR] User played with O");
 				symbolCorrect = true;
 			}
 		}
 
 		if (symbolCorrect) {
 
 			this.getContainer().getLoggerComponent()
 					.debug("[GameFSM][ICR] User has draw required symbol. Trying to get it's position.");
 			// Required symbol appears. Now we must check, which field is used
 			// Retrieving center of the user symbol
 
 			this.getContainer().getLoggerComponent()
 					.debug("[GameFSM][ICR] Receiving symbol rectangle. ICRContext: "
 							+ this.icrContext);
 
 			Rectangle r = this.icrContext.getTextBoundingBox();
 
 			this.getContainer().getLoggerComponent().debug("[GameFSM][ICR] Rectangle: " + r);
 			this.getContainer().getLoggerComponent()
 					.debug("[GameFSM][ICR] symbol rectangle was received ("
 							+ r.toString() + "). Calculating it's center point");
 			Point p = new Point(r.getX() + r.getWidth() / 2, r.getY()
 					+ r.getHeight() / 2);
 
 			this.getContainer().getLoggerComponent().debug("[GameFSM][ICR] Point of the user symbol is ("
 					+ p.getX() + "," + p.getY() + ")");
 
 			int field = this.container.getGameBoardComponent().getTurnField(p);
 			if (field != -1) {
 				this.getContainer().getLoggerComponent()
 						.debug("[GameFSM][ICR] Turn was done in the correct field");
 				this.getContainer().getGameLogicComponent().humanTurn(field);
 				this.eventHumanTurnReady();
 			} else {
 				this.getContainer().getLoggerComponent()
 						.debug("[GameFSM][ICR] Turn was done outside board");
 			}
 		} else {
 			this.getContainer().getLoggerComponent()
 					.debug("[GameFSM][ICR] User has not draw required symbol");
 		}
 
 		// ICR Strokes clearing must be done in any case
 		this.icrContext.clearStrokes();
 	}
 
 	/**
 	 * Initializes ICR context for an application
 	 */
 	private void initializeICRContext() {
 		this.getContainer().getLoggerComponent().info("[GameFSM] Initializing ICR context");
 
 		try {
 			this.icrContext = this.getContainer().getPenletComponent().getContext().getICRContext(1000, this);
 			Resource[] resources = {
 					this.icrContext.getDefaultAlphabetKnowledgeResource(),
 					this.icrContext
 							.createAppResource("/icr/LEX_smartpen-ticktacktoe.res"),
 					this.icrContext
 							.createAppResource("/icr/SK_smartpen-ticktacktoe.res") };
 			this.icrContext.addResourceSet(resources);
 			this.getContainer().getLoggerComponent().info("[GameFSM] ICR context was successfully initialized");
 		} catch (Exception e) {
 			String msg = "[GameFSM] Error initializing handwriting recognition resources: "
 					+ e.getMessage();
 			this.getContainer().getLoggerComponent().error(msg);
 			this.getContainer().getGameDisplayComponent().displayMessage(msg, true);
 		}
 	}
 
 	/**
 	 * Destroys ICR context
 	 */
 	private void destroyICRContext() {
 		icrContext.dispose();
 		icrContext = null;
 		this.getContainer().getLoggerComponent().info("[GameFSM] ICR context was destroyed");
 	}
 
 	/**
 	 * Processing of the next event, scheduled during transition
 	 */
 	private void processNextEvent() {
 		if (nextEvent != NEXT_EVENT_NONE) {
 			switch (nextEvent) {
 			case NEXT_EVENT_PLAYER_SELECTED_HUMAN_TURN_NEXT:
 				eventPlayerSelectedHumanTurnNext();
 				break;
 			case NEXT_EVENT_PLAYER_SELECTED_PEN_TURN_NEXT:
 				eventPlayerSelectedPenTurnNext();
 				break;
 			case NEXT_EVENT_GAME_PEN_TURN_READY:
 				eventPenTurnReady();
 				break;
 			case NEXT_EVENT_GAME_END_HUMAN_WINS:
 				eventHumanWins();
 				break;
 			case NEXT_EVENT_GAME_END_PEN_WINS:
 				eventPenWins();
 				break;
 			case NEXT_EVENT_GAME_END_DRAW:
 				eventDraw();
 				break;
 			case NEXT_EVENT_END:
 				eventEndApplication();
 				break;
 
 			default:
 				this.getContainer().getLoggerComponent()
 						.debug("[GameFSM] Invalid next event, that cannot be processed ("
 								+ nextEvent + ")");
 				break;
 			}
 		}
 	}
 
 	/**
 	 * Set's new next event to process
 	 * 
 	 * @param nextEvent
 	 */
 	public void setNextEvent(int nextEvent) {
 		this.nextEvent = nextEvent;
 	}
 	
 	/**
 	 * Returns container
 	 * 
 	 * @return container
 	 */
 	public Container getContainer() {
 		return container;
 	}
 }
