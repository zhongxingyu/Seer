 package ai.cs4730;
/**
 * Represents a genreal task or mission
 * given units, buildings, and resources to accomplish it
 */
 public abstract class Manager {
 
 	public Manager() {}
 	
 	public abstract void update();
 	public abstract void assignUnits();
 	public abstract void assignResources();
 
 }
