package us.exultant.ahs.terminal;
 
 public class TermCodes {
 	/** Control Sequence Initiator */
 	public static final String	CSI			= "\033[";
 	
 	public static final String	REND_COLOR_FG_BLACK		= CSI+"30m";
 	public static final String	REND_COLOR_FG_RED		= CSI+"31m";
 	public static final String	REND_COLOR_FG_GREEN		= CSI+"32m";
 	public static final String	REND_COLOR_FG_YELLOW		= CSI+"33m";
 	public static final String	REND_COLOR_FG_BLUE		= CSI+"34m";
 	public static final String	REND_COLOR_FG_MAGENTA		= CSI+"35m";
 	public static final String	REND_COLOR_FG_CYAN		= CSI+"36m";
 	public static final String	REND_COLOR_FG_WHITE		= CSI+"37m";
 			// what i've chosen to label as "bright" here is sometimes more... "bold".
 			//   which is insanely shitty when it comes to trying to put a stateful layer on top of this.
 			//   also, in, for example, konsole?  brightblack is a dark grey, and COMPLETELY distinct on a black background.  on xterm?  still grey, thank god, but also bold.
 	public static final String	REND_COLOR_FG_BRIGHTBLACK	= CSI+"1;30m";
 	public static final String	REND_COLOR_FG_BRIGHTRED		= CSI+"1;31m";
 	public static final String	REND_COLOR_FG_BRIGHTGREEN	= CSI+"1;32m";
 	public static final String	REND_COLOR_FG_BRIGHTYELLOW	= CSI+"1;33m";
 	public static final String	REND_COLOR_FG_BRIGHTBLUE	= CSI+"1;34m";
 	public static final String	REND_COLOR_FG_BRIGHTMAGENTA	= CSI+"1;35m";
 	public static final String	REND_COLOR_FG_BRIGHTCYAN	= CSI+"1;36m";
 	public static final String	REND_COLOR_FG_BRIGHTWHITE	= CSI+"1;37m";
 	public static final String	REND_COLOR_FG_DEFAULT		= CSI+"39m";
 	
 	public static final String	REND_COLOR_BG_BLACK		= CSI+"40m";
 	public static final String	REND_COLOR_BG_RED		= CSI+"41m";
 	public static final String	REND_COLOR_BG_GREEN		= CSI+"42m";
 	public static final String	REND_COLOR_BG_YELLOW		= CSI+"43m";
 	public static final String	REND_COLOR_BG_BLUE		= CSI+"44m";
 	public static final String	REND_COLOR_BG_MAGENTA		= CSI+"45m";
 	public static final String	REND_COLOR_BG_CYAN		= CSI+"46m";
 	public static final String	REND_COLOR_BG_WHITE		= CSI+"47m";
 	public static final String	REND_COLOR_BG_DEFAULT		= CSI+"49m";
 
 	public static final String	REND_BLINK_ON			= CSI+"5m";
 	public static final String	REND_BLINK_OFF			= CSI+"25m";
 	public static final String	REND_UNDERLINE_ON		= CSI+"4m";
 	public static final String	REND_UNDERLINE_OFF		= CSI+"24m";
 			// 22 is "normal intensity", but that's a crappy mixture of boldness and brightness that doesn't really make much sense without some functional layers to add sanity statefully.
 	
 	public static final String	REND_RESET			= CSI+"m";	// or "0m".  but brevity ftw
 	
 	public static final String	CLEAR_SCREEN		= CSI+"2J";
 }
