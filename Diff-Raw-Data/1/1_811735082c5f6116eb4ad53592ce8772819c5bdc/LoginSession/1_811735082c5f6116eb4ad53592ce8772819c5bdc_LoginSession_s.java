 package formel0api.beans;
 
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 
 import formel0api.Game;
 
 /**
  * An instance of this class manage the login of a player.
  * The Player must already be known by the Registrar in order to
  * successfully login.
  *
  */
 @ManagedBean(name="loginControl")
 @SessionScoped
 public class LoginSession {
 	
     @ManagedProperty(value="#{registrar}")
     private Registrar registrar;
     
     private Player player;
     
     private Player computer;
     
     private Game game;
 
     boolean loginfailed = false;
     
     private String playerName;
     
     private String playerPassword;
 
     /** Creates a new instance of LoginCtrl */
     public LoginSession() {
     	
     }
 
     //Getters and Setters for Bean
     
     public void setRegistrar(Registrar registrar) {
     	this.registrar = registrar;
     }
     
     public String getPlayerName() {
         return playerName;
     }
 
     public void setPlayerName(String name) {
         this.playerName = name;
     }
     
     public String getPlayerPassword() {
         return playerPassword;
     }
 
     public void setPlayerPassword(String pwd) {
         this.playerPassword = pwd;
     }
     
     public Player getPlayer() {
     	return player;
     }
 
     public boolean isLoginfailed() {
         return loginfailed;
     }
 
     public void setLoginfailed(boolean loginfailed) {
         this.loginfailed = loginfailed;
     }
     
     public Game getGame(){
     	return game;
     }
 
     //Login - check password
     public String login() { 
 		if (registrar.getPlayer(playerName)!=null && registrar.getPlayer(playerName).getPassword().equals(playerPassword)) {
 			player = registrar.getPlayer(playerName);
 			computer = new Player();
 			computer.setFirstName("Super");
 			computer.setLastName("C");
             loginfailed = false;
             startNewGame();
             return "/table.xhtml";
         } else {
             loginfailed = true;
             return "/index.xhtml";
         }
     }
     
     // logout current user
     public String logout() {
         FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
         return "/index.xhtml";
     }
     
     public void play() {
     	if(!game.isGameOver()) {
     		game.setRound(game.getRound()+1);
     		game.rollthedice(game.getPlayer());
     		if(!game.isGameOver()) {
     			game.rollthedice(game.getComputer());
     		}
     	}
     }
     
     public String startNewGame() {
     	player.reset();
     	computer.reset();
     	
     	game = new Game(player, computer);
     	return "/table.xhtml";
     }
 
 }
