 package fr.vergne.livingwallpaper.bot;
 
 import java.util.LinkedList;
 
 import fr.vergne.livingwallpaper.bot.action.Action;
 import fr.vergne.livingwallpaper.bot.action.ActionFactory;
 import fr.vergne.livingwallpaper.bot.action.ActionStatus;
 import fr.vergne.livingwallpaper.environment.Environment;
 
 public class Bot {
 	private float x = 0;
 	private float y = 0;
 	private int pixelsPerSeconds = 0;
	private final LinkedList<Action> runningActions = new LinkedList<Action>();
	private final LinkedList<Action> facultativeActions = new LinkedList<Action>();
 	private final ActionFactory actionFactory = new ActionFactory();
 	private final Environment environment = new Environment();
 
 	public Bot() {
		facultativeActions.add(actionFactory.createRandomWalkingAction());
 	}
 
 	public float getX() {
 		return x;
 	}
 
 	public void setX(float x) {
 		this.x = x;
 	}
 
 	public float getY() {
 		return y;
 	}
 
 	public void setY(float y) {
 		this.y = y;
 	}
 
 	public int getPixelsPerSecond() {
 		return pixelsPerSeconds;
 	}
 
 	public void setPixelsPerSecond(int pixelsPerSecond) {
 		this.pixelsPerSeconds = pixelsPerSecond;
 	}
 
 	public void addAction(Action action) {
		runningActions.add(action);
 	}
 
 	private Action lastAction;
 
 	public void executeAction() {
		lastAction = runningActions.isEmpty() ? facultativeActions
				.removeFirst() : runningActions.removeFirst();
 		ActionStatus status = lastAction.execute(this);
 		if (status == ActionStatus.RUNNING) {
			runningActions.addLast(lastAction);
 		} else if (status == ActionStatus.FACULTATIVE) {
			facultativeActions.addLast(lastAction);
 		} else if (status == ActionStatus.FINISHED) {
 			// forget the action
 		} else {
 			throw new IllegalStateException(status + " is not a managed "
 					+ status.getClass().getSimpleName());
 		}
 	}
 
 	public Environment getEnvironment() {
 		return environment;
 	}
 }
