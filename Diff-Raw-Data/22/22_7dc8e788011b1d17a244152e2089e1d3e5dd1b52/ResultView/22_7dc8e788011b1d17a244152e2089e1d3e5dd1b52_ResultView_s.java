 package com.tung;
 
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.lcdui.List;
 
 // TODO add totals per team
 // TODO separate view for home and visitor team, switch by single click instead of scrolling?
 public abstract class ResultView extends List implements CommandListener {
 	
 	private static final String SCORE_SEPARATOR = "-";
 
 	protected static final String HOME_DESC = "Home";
 
 	protected static final String VISITOR_DESC = "Visit";
 
 	private Result result;
 	
 	private Display display;
 
 	private Main main;
 	
 	public ResultView(Result result, Display display, Main main, String team) {
 		super("Result " + team, List.EXCLUSIVE);
 		
 		this.main = main;
 		
 		this.result = result;
 		this.display = display;
 
 		addCommand(main.navigation.sendCommand);
 		addCommand(main.navigation.editResult);
 		setCommandListener(this);
 	}
 	
 	protected void addPlayerResult(int team, int player) {
 		int[] entry = result.getSummary(team, player);
 		String prefix = Integer.toString(player) + "-- " + getDescription(team);
 		StringBuffer summary = new StringBuffer(prefix);
 		summary = addFormattedSummary(summary, entry);
 		
 		append(summary.toString(), null);
 	}
 
 	private StringBuffer addFormattedSummary(StringBuffer summary, int[] entry) {
 		summary.append(": ");
 		summary.append(entry[2]).append(SCORE_SEPARATOR).append(entry[3]);
 		summary.append(" | ");
 		summary.append(entry[0]).append(SCORE_SEPARATOR).append(entry[1]);
 		return summary;
 	}
 	
 	protected void addDoublesResult(int team) {
 		int[] entry = result.getDoubleSets();
 		if (team == Result.VISITOR) {
 			entry = Result.invert(entry);
 		}
 		String prefix = "DBL " + getDescription(team); 
 		StringBuffer summary = new StringBuffer(prefix);
 		summary = addFormattedSummary(summary, entry);
 		
 		append(summary.toString(), null);
 	}
 	
 	protected void addTotals(int team) {
 		int[] totals = result.getTotals();
 		if (team == Result.VISITOR) {
 			totals = Result.invert(totals);
 		}
 		String prefix = "== " + getDescription(team);
 		StringBuffer summary = new StringBuffer(prefix);
 		summary = addFormattedSummary(summary, totals);
 		
 		append(summary.toString(), null);
 		
 	}
 
 	protected String getDescription(int team) {
 		return Result.HOME == team ? HOME_DESC : VISITOR_DESC;
 	}
 
 	public void commandAction(Command cmd, Displayable displayable) {
 		if (cmd == main.navigation.sendCommand) {
 			if (main.matchInfo.validateInput()) {
 				display.setCurrent(main.resultSender);
 			} else {
 				display.setCurrent(main.matchInfo);
 			}
 			
 		} else if (cmd == main.navigation.editResult) {
 			display.setCurrent(main.enterResultForm);
 			
 		} else if (cmd == main.navigation.showHomeResult) {
 			display.setCurrent(main.homeResultView);
 			
 		} else if (cmd == main.navigation.showVisitorResult) {
 			display.setCurrent(main.visitorResultView);
 		}
 	}
 
 	abstract public void refresh();
 
 }
