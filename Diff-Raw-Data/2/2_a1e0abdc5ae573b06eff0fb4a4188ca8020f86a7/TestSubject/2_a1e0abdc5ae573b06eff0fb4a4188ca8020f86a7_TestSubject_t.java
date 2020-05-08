 package se.bthstudent.sis.afk.GLaDOS;
 
 import java.io.Serializable;
 
 /**
  * Class representing a test subject, storing the current nick and know aliases.
  * Also stores the set mode for the subject.
  * 
 * @author Sabbath, Cromigon
  */
 public class TestSubject implements Serializable {
 
 	/**
 	 * Field serialVersionUID. (value is -1285284334132918163)
 	 */
 	private static final long serialVersionUID = -1285284334132918163L;
 
 	/**
 	 * Current nick.
 	 */
 	private String nick;
 
 	/**
 	 * Known aliases
 	 */
 	private String[] alias;
 
 	/**
 	 * Possible modes a subject can have.
 	 */
 	public enum Mode {
 		/**
 		 * Operator mode.
 		 */
 		OP, /**
 		 * Voice mode.
 		 */
 		VOICE, /**
 		 * No mode.
 		 */
 		NONE;
 	}
 
 	/**
 	 * Current/stored mode.
 	 */
 	private Mode mode;
 
 	private boolean admin;
 	
 	private boolean ignored;
 
 	/**
 	 * Constructor for TestSubject.
 	 */
 	public TestSubject() {
 		this.setNick("");
 		this.alias = new String[0];
 		this.setMode(Mode.NONE);
 		this.admin = false;
 		this.ignored = false;
 	}
 
 	/**
 	 * Constructor for TestSubject.
 	 * 
 	 * @param nick
 	 *            Current nick.
 	 * @param alias
 	 *            Known aliases.
 	 * @param mode
 	 *            Current mode.
 	 */
 	public TestSubject(String nick, String[] alias, Mode mode, boolean admin, boolean ignored) {
 		this.setNick(nick);
 		this.alias = alias;
 		this.setMode(mode);
 		this.admin = admin;
 		this.ignored = ignored;
 	}
 
 	/**
 	 * @param nick
 	 *            the nick to set
 	 */
 	public void setNick(String nick) {
 		this.nick = nick;
 	}
 
 	/**
 	 * 
 	 * @return the nick
 	 */
 	public String getNick() {
 		return nick;
 	}
 
 	/**
 	 * @param mode
 	 *            the mode to set
 	 */
 	public void setMode(Mode mode) {
 		this.mode = mode;
 	}
 
 	/**
 	 * 
 	 * @return the mode
 	 */
 	public Mode getMode() {
 		return mode;
 	}
 
 	public void setAdmin(boolean admin) {
 		this.admin = admin;
 	}
 
 	public boolean getAdmin() {
 		return this.admin;
 	}
 	
 	public void setIgnored(boolean Ignored) {
 		this.ignored = Ignored;
 	}
 	
 	public boolean getIgnored() {
 		return this.ignored;
 	}
 
 	/**
 	 * Adds an alias to the test subjects nick
 	 * 
 	 * @param alias
 	 *            Alias to be added
 	 */
 	public void addAlias(String alias) {
 		String[] temp = new String[this.alias.length + 1];
 
 		System.arraycopy(this.alias, 0, temp, 0, this.alias.length);
 
 		temp[this.alias.length + 1] = alias;
 
 		this.alias = temp;
 	}
 
 	/**
 	 * Checks if the test subject have a specific alias
 	 * 
 	 * @param toFind
 	 *            The alias to search for.
 	 * 
 	 * @return Returns true if the alias is found, false otherwise
 	 */
 	public boolean checkForAlias(String toFind) {
 		boolean found = false;
 
 		for (String alias : this.alias) {
 			if (alias.equals(toFind))
 				found = true;
 		}
 
 		return found;
 	}
 }
