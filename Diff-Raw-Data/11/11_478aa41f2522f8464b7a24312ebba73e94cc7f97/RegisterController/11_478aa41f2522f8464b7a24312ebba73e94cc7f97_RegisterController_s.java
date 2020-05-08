 package at.ac.tuwien.big.ewa.ue3.web;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.context.FacesContext;
 
 import at.ac.tuwien.big.easyholdem.player.Player;
 import at.ac.tuwien.big.easyholdem.player.PlayerDAO;
 import at.ac.tuwien.big.easyholdem.player.Player.Gender;
 
 /**
  * Controller class handling all requests and actions concerning the registration process.
  * 
  * @author Philip Langer
  */
 public class RegisterController {
 
 	/**
 	 * Expression language expression representing the <code>player</code> bean.
 	 */
 	private static final String EL_PLAYER = "#{player}";
 
 	/**
 	 * Return value indicating a unsuccessful completion of an action.
 	 */
 	private static final String FAIL = "fail";
 
 	/**
 	 * String representation of the {@link Gender} MALE.
 	 */
 	private static final String MALE = "m";
 
 	/**
 	 * Return value indicating a successful completion of an action.
 	 */
 	private static final String SUCCESS = "success";
 
 	/**
 	 * The injected {@link Player} currently logged in.
 	 */
 	private Player loggedInPlayer;
 
 	/**
 	 * The injected {@link PlayerDAO} to use for persisting {@link Player}s.
 	 */
 	private PlayerDAO playerDAO;
 
 	/**
 	 * Gender of the {@link Player} to register.
 	 */
 	private String regGender = RegisterController.MALE;
 
 	/**
 	 * {@link Player} to register.
 	 */
 	private Player regPlayer = new Player();
 
 	/***************************************************************************
 	 * Managed property methods
 	 **************************************************************************/
 
 	/**
 	 * Updates the data of the player currently logged in.
 	 */
 	public String changeUser() {
 		final FacesContext context = FacesContext.getCurrentInstance();
 		try {
 			playerDAO.update(loggedInPlayer);
 			context.addMessage(null, new FacesMessage("Die Daten wurden geändert!"));
 		} catch (final Exception e) {
 			context.addMessage(null, new FacesMessage("Leider ist folgender Fehler aufgetreten:"
 			        + e.getLocalizedMessage()));
 		}
 
 		return "changeUser";
 	}
 
 	/**
 	 * Returns the {@link Player} that is currently logged in.
 	 * 
 	 * @return currently logged in {@link Player}.
 	 */
 	public Player getLoggedInPlayer() {
 		return loggedInPlayer;
 	}
 
 	/**
 	 * Returns the previously injected {@link PlayerDAO} bean.
 	 * 
 	 * @return current used {@link PlayerDAO} instance.
 	 */
 	public PlayerDAO getPlayerDAO() {
 		return playerDAO;
 	}
 
 	/**
 	 * Returns the {@link String} representation of the gender of the player to register.
 	 * 
 	 * @return {@link String} representation of the {@link Gender}.
 	 */
 	public String getRegGender() {
 		return regGender;
 	}
 
 	/***************************************************************************
 	 * Methods invoked by pages
 	 **************************************************************************/
 
 	/**
 	 * Returns the player to register.
 	 * 
 	 * @return the player to register.
 	 */
 	public Player getRegPlayer() {
 		return regPlayer;
 	}
 
 	/**
 	 * Registers the the currently set <code>regPlayer</code> with the <code>regGender</code>.
 	 * 
 	 * @return &quot;success&quot; if registration was successful. &quot;fail&quot; otherwise.
 	 */
 	public String register() {
 		final FacesContext context = FacesContext.getCurrentInstance();
 		if (regGender.equals(RegisterController.MALE))
 			regPlayer.setGender(Player.Gender.MALE);
 		else
 			regPlayer.setGender(Player.Gender.FEMALE);
 		final Player p = playerDAO.getPlayerByUsername(regPlayer.getUserName());
 		if (p != null && p.getUserName().equals(regPlayer.getUserName())) {
 			context.addMessage(null, new FacesMessage("Benutzername ist leider schon vergeben."));
 			return RegisterController.FAIL;
 		}
 		try {
 			final Player persitentPlayer = playerDAO.persist(regPlayer);
 			context.getApplication().getExpressionFactory().createValueExpression(context.getELContext(),
 			        RegisterController.EL_PLAYER, Player.class).setValue(context.getELContext(), persitentPlayer);
 			return RegisterController.SUCCESS;
 		} catch (final Exception e) {
 			context.addMessage(null, new FacesMessage("Leider ist folgender Fehler aufgetreten:"
 			        + e.getLocalizedMessage()));
 			return RegisterController.FAIL;
 		}
 	}
 
 	/**
 	 * Sets the currently logged in player. This method is used to inject the currently logged in {@link Player} bean.
 	 * 
 	 * @param loggedInPlayer
 	 *            currently logged in {@link Player}.
 	 */
 	public void setLoggedInPlayer(Player loggedInPlayer) {
 		this.loggedInPlayer = loggedInPlayer;
 	}
 
 	/**
 	 * Sets the {@link PlayerDAO}. This method is used to inject the {@link PlayerDAO}.
 	 * 
 	 * @param playerDAO
 	 *            to set.
 	 */
 	public void setPlayerDAO(PlayerDAO playerDAO) {
 		this.playerDAO = playerDAO;
 	}
 
 	/**
 	 * Sets the {@link String} representation of the {@link Gender} of the player to register.
 	 * 
 	 * @param s
 	 *            {@link Gender} as {@link String}.
 	 */
 	public void setRegGender(String s) {
 		regGender = s;
 	}
 
 	/**
 	 * Sets the player to register.
 	 * 
 	 * @param regPlayer
 	 *            the player to register.
 	 */
 	public void setRegPlayer(Player regPlayer) {
 		this.regPlayer = regPlayer;
 	}
 
 }
