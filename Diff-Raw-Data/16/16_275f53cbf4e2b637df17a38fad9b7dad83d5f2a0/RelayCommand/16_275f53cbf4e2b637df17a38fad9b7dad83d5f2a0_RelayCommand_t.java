package de.htwg.seapal.common.command;
 
 public abstract class RelayCommand implements ICommand {
 
 	/**
 	 * The command key.
 	 */
 	protected char key;
 	
 	/**
 	 * The command title.
 	 */
 	protected String title;
 	
 	/**
 	 * Instantiates a new command.
 	 * @param key The command key
 	 * @param title The command title/description.
 	 */
 	public RelayCommand(char key, String title) {
 		this.key = key;
 		this.title = title;
 	}
 	
 	@Override
 	public char getKey() {
 		return this.key;
 	}
 
 	@Override
 	public String getTitle() {
 		return this.title;
 	}
 	
 	@Override
 	public boolean equals(Object other) {
 		if (other == this) {
 			return true;
 		}
 		
 		if (!(other instanceof RelayCommand)) {
 			return false;
 		}
 		
 		RelayCommand command = (RelayCommand)other;
 		return this.key == command.key;
 	}
 }
