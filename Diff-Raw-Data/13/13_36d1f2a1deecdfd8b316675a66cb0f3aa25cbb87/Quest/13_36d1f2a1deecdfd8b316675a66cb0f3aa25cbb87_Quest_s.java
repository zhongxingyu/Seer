 package gamedev.quests;
 
 import gamedev.scenes.GameMapScene;
 
 public abstract class Quest {
 
 	protected String title;
 	protected String description;
 	protected boolean isActive = false;
 	protected String status;
 	protected GameMapScene map;
 	protected boolean isCompleted = false;
 	protected boolean isFinished = false;
 
 	// TODO: Give rewards!
 
 	public Quest(GameMapScene map) {
 		this.map = map;
 	}
 
 	public abstract void onFinish();
 
 	public abstract String getStatus();
 
 	public abstract boolean isCompleted();
 
 	public boolean isActive() {
 		return this.isActive;
 	}
 
 	public void setActive(boolean isActive) {
 		this.isActive = isActive;
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public String getDescription() {
 		return this.description;
 	}
 
 	public void setCompleted(boolean completed) {
 		this.isCompleted = completed;
 	}
 
 	public boolean isFinished() {
 		return this.isFinished;
 	}
 
 	public void setFinished(boolean finished) {
 		this.isFinished = finished;
 	}
 }
