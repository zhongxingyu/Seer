 package com.shibinck.pomodoro;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.microedition.lcdui.Command;
 import javax.microedition.lcdui.CommandListener;
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Displayable;
 import javax.microedition.media.Manager;
 import javax.microedition.media.MediaException;
 import javax.microedition.midlet.MIDlet;
 import javax.microedition.midlet.MIDletStateChangeException;
 
 public class PomodoroMIDlet extends MIDlet implements CommandListener {
 	public static final int POMODORO_STATE = 0;
 	public static final int SHORT_BREAK_STATE = 1;
 	public static final int LONG_BREAK_STATE = 2;
 	public static final int STOP_STATE = 3;
 	
 	private final Command startCommand = new Command("Start", Command.OK, 1);
 	private final Command stopCommand = new Command("Stop", Command.OK, 1);
 	private final Command exitCommand = new Command("Exit", Command.EXIT, 1);
 	private final Command optionsCommand = new Command("Options", Command.SCREEN, 1);
 	private final Command saveCommand = new Command("Save", Command.OK, 1);
 	private final Command backCommand = new Command("Back", Command.BACK, 1);
 	private OptionsScreen optionsScreen;
 	private TimerScreen timerScreen;
 	private OptionsStore store;
 	
 	private final Timer timer = new Timer();
 	private volatile ScheduledTask scheduledTask;
 	private volatile int state = STOP_STATE;
 	private volatile int pomodoroCount;
 	private volatile int minsLeft;
 
 	public PomodoroMIDlet() {
 	}
 
 	protected void destroyApp(boolean unconditional)
 			throws MIDletStateChangeException {
 	}
 
 	protected void pauseApp() {
 	}
 
 	protected void startApp() throws MIDletStateChangeException {
 		store = new OptionsStore();
 		optionsScreen = new OptionsScreen("Options", store);
 		timerScreen = new TimerScreen("Pomodoro");
 		optionsScreen.addCommand(saveCommand);
 		optionsScreen.addCommand(backCommand);
 		timerScreen.addCommand(optionsCommand);
 		timerScreen.addCommand(startCommand);
 		timerScreen.addCommand(exitCommand);
 		optionsScreen.setCommandListener(this);
 		timerScreen.setCommandListener(this);
 		state = STOP_STATE;
 		pomodoroCount = 0;
 		Display.getDisplay(this).setCurrent(timerScreen);
 	}
 	
 	public void commandAction(Command command, Displayable displayable) {
 		if (command == exitCommand) {
 			notifyDestroyed();
 		} else if (command == backCommand) {
 			Display.getDisplay(this).setCurrent(timerScreen);
 		} else if (command == optionsCommand) {
 			optionsScreen.refresh();
 			Display.getDisplay(this).setCurrent(optionsScreen);
 		} else if (command == saveCommand) {
 			store.saveOptions(optionsScreen.getPomodoroMins(), 
 					optionsScreen.getShortBreakMins(),
 					optionsScreen.getLongBreakMins(),
 					optionsScreen.getPomodoroCounts());
 			Display.getDisplay(this).setCurrent(timerScreen);
 		} else if (command == startCommand) {
 			startPomodoroCycle();
 		} else if (command == stopCommand) {
 			stopPomodoroCycle();
 		}
 	}
 	
 	private void startPomodoroCycle() {
 		state = STOP_STATE;
 		scheduledTask = new ScheduledTask();
		timer.scheduleAtFixedRate(scheduledTask, 0, 60000);
 		timerScreen.removeCommand(startCommand);
 		timerScreen.addCommand(stopCommand);
 	}
 	private void stopPomodoroCycle() {
 		state = STOP_STATE;
 		scheduledTask.cancel();
 		timerScreen.removeCommand(stopCommand);
 		timerScreen.addCommand(startCommand);
 		timerScreen.clear();
 		playSound();
 	}
 	
 	private void playSound() {
 		try {
 			int note = 100, duration = 1000;
 			switch(state) {
 			case SHORT_BREAK_STATE:
 				note = 110;
 				duration = 1000;
 				break;
 			case LONG_BREAK_STATE:
 				note = 115;
 				duration = 2000;
 				break;
 			case STOP_STATE:
 				note = 127;
 				duration = 200;
 				break;
 			case POMODORO_STATE:
 				note = 100;
 				duration = 2000;
 				break;
 			}
 		    Manager.playTone(note, duration, 100);
 		} catch (MediaException e) { 
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Task run every minute if pomodoro cycle is active
 	 */
 	class ScheduledTask extends TimerTask {
 		public void run() {
 			if (state == STOP_STATE) {
 				pomodoroCount = 0;
 				state = POMODORO_STATE;
 				minsLeft = store.getPomodoroMins();
 				playSound();
 			} else {
 				minsLeft--;
 			}
 			if (minsLeft == 0) {
 				switch(state) {
 				case POMODORO_STATE:
 					pomodoroCount++;
 					if (pomodoroCount == store.getPomodoroCounts()) {
 						state = LONG_BREAK_STATE;
 						minsLeft = store.getLongBreakMins();
 					} else {
 						state = SHORT_BREAK_STATE;
 						minsLeft = store.getShortBreakMins();
 					}
 					break;
 				case SHORT_BREAK_STATE:
 					state = POMODORO_STATE;
 					minsLeft = store.getPomodoroMins();
 					break;
 				case LONG_BREAK_STATE:
 					state = POMODORO_STATE;
 					minsLeft = store.getPomodoroMins();
 					pomodoroCount = 0;
 					break;
 				}
 				playSound();
 			}
 			timerScreen.update(minsLeft, pomodoroCount, state);
 		}
 	}
 }
 
