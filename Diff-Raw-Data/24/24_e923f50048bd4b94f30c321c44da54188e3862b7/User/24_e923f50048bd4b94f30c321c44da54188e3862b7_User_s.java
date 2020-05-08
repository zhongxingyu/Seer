 package mobile.group1.DB;
 
 public class User 
 {
 	private String username;
 	private String password;
 	private String current_game;
 	private String score;
 	private byte[] found_items;
 	private String placeholder1;
 	private String placeholder2;
 	
 	public User()
 	{
 		
 	}
 	
 	public User(String username, 
 				String password, 
 				String current_game, 
 				String score, 
 				String found_items, 
 				String placeholder1, 
 				String placeholder2)
 	{
 		this.username     = username;
 		this.password     = password;
 		this.current_game = current_game;
 		this.score        = score;
 		this.found_items  = found_items.getBytes();
 		this.placeholder1 = placeholder1;
 		this.placeholder2 = placeholder2;
 	}
 			
 			
 			
 	public String getUsername() {
 		return username;
 	}
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	public String getPassword() {
 		return password;
 	}
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	public String getCurrent_game() {
 		return current_game;
 	}
 	public void setCurrent_game(String current_game) {
 		this.current_game = current_game;
 	}
	public String getScore() {
 		return score;
 	}
	public void setScore(String score) {
 		this.score = score;
 	}
 	public byte[] getFound_items() {
 		return found_items;
 	}
 	public void setFound_items(byte[] found_items) {
 		this.found_items = found_items;
 	}
 	public String getPlaceholder1() {
 		return placeholder1;
 	}
 	public void setPlaceholder1(String placeholder1) {
 		this.placeholder1 = placeholder1;
 	}
 	public String getPlaceholder2() {
 		return placeholder2;
 	}
 	public void setPlaceholder2(String placeholder2) {
 		this.placeholder2 = placeholder2;
 	}
 }
