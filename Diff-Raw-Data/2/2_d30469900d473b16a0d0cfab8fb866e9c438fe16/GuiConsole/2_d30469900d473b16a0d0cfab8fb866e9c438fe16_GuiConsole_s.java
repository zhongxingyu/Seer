 package sharConsole.common.gui;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.input.Keyboard;
 
 import sharConsole.common.SharConsole;
 import sharConsole.shar.util.TextColors;
 import sharConsole.shar.util.Debug;
 import cpw.mods.fml.client.FMLClientHandler;
 
 import net.minecraft.client.gui.GuiButton;
 import net.minecraft.client.gui.GuiChat;
 import net.minecraft.util.ChatAllowedCharacters;
 
 /**
  * The console's gui class.
  * @author Sharingan616/Benjamin Tovar-Prince (http://benjamintovar-prince.com)
  *
  */
 public class GuiConsole extends GuiChat {
 	private static GuiConsole instance;
 	private TextColors c;
 	/** A list of command lines. **/
 	public ArrayList comList = new ArrayList();
 	public String[] lines = new String[comList.size()];
 	/** A list of command line descriptions. **/
 	public ArrayList descList = new ArrayList();
 	public String[] descriptions = new String[descList.size()];
 	/** A list of command lines entered by the user. **/
 	public ArrayList histList = new ArrayList();
 	public String[] history = new String[histList.size()];
 	private int historyPoint = history.length;
 	private int counter; 
 	private boolean tickedOn;
 	private int selected;
 	private int charMargin;
 	public int increment;
 	/** A string that is created from the characters the user has typed.*/
 	public String msg;
 	/** The maximum character length allowed for the string {@link #msg msg}*/
 	public int maxMsgLength;
 	
 	private List sentMessages = FMLClientHandler.instance().getClient().ingameGUI.getChatGUI().getSentMessages();
 	/**
 	 * Gui for the console.
 	 */
 	public GuiConsole() {
 		this.charMargin = 0;
 		this.selected = 0;
 		this.counter = 0;
 		this.tickedOn = false;
 		this.increment = 5;
 		this.msg = "";
 		this.maxMsgLength = 100;
 		addCommandLines();
 	}
 
 	/**
 	 * Gui for the console.
 	 * @param str default string for displaying pre-filled text
 	 */
 	public GuiConsole(String str) {
 		super(str);
 		this.charMargin = 0;
 		this.selected = 0;
 		this.counter = 0;
 		this.tickedOn = false;
 		this.increment = 5;
 		this.msg = "";
 		this.maxMsgLength = 100;
 		addCommandLines();
 	}
 	
     /**
      * Adds the buttons (and other controls) to the screen in question.
      */
     public void initGui() {
     	Keyboard.enableRepeatEvents(true);
     }
     
     /**
      * Draws the screen and all the components in it.
      * @param par1
      * @param par2
      * @param par3
      */
     public void drawScreen(int par1, int par2, float par3) {
     	charMargin = fontRenderer.getStringWidth(msg.substring(msg.length()-selected, msg.length()));
     	if(counter%increment == 0) {
         	if((counter/increment)%2 == 0) 	tickedOn = true;
         	else 							tickedOn = false;}
     	String title = c.Rose+"SCC "+SharConsole.getVersion()+c.White;
     	String line = title+"> "+msg;
     	drawRect(2, 2, this.width - 2, 14, Integer.MIN_VALUE);
     	//drawGradientRect(2, 2, this.width - 2, 14+48, Integer.MIN_VALUE, 0);
 
     	fontRenderer.drawStringWithShadow(line, 4, 4, 0xFFFFFF);
         if(tickedOn)
         	fontRenderer.drawStringWithShadow("|", 4+fontRenderer.getStringWidth(line)-charMargin, 4, 0xFFFFFF);
         
         //Begin suggestion drawing
         int j = 0;
         for(int i = 0; i < lines.length; i++)
     	{	
         	if(((lines[i].startsWith(msg) && !msg.startsWith(lines[i])) || (msg.startsWith("/") && lines[i].startsWith(msg.substring(1)) && !msg.substring(1).startsWith(lines[i]))) && ((msg.length() > 0 && !msg.startsWith("/")) || (msg.length() > 1 && msg.startsWith("/"))))
         	{
         		int bound = msg.length();
         		if(msg.startsWith("/"))
         			bound -= 1;
         		String output = lines[i].replaceFirst(lines[i].substring(0, bound), c.Red+lines[i].substring(0, bound)+c.White);
         		int k = 0;
         		j += 12;
         		if(msg.startsWith("/"))
         			k += fontRenderer.getStringWidth("/");
             	drawRect(k+2+fontRenderer.getStringWidth(title+"> "), 2+j, this.width - 2, 14+j, Integer.MIN_VALUE);
             	fontRenderer.drawStringWithShadow(output, k+4+fontRenderer.getStringWidth(title+"> "), 4+j, 0xF0F0F0);
         	}
         	else if( (msg.startsWith(lines[i])) || (msg.startsWith("/"+lines[i])) )
         	{
         		String ending = "";
         		if(fontRenderer.getStringWidth(descriptions[i]) > (this.width-fontRenderer.getStringWidth(title+"> ")-6))
         			ending ="....";
         		int k = 0;
         		j += 12;
         		if(msg.startsWith("/"))
         			k += fontRenderer.getStringWidth("/");
         		drawRect(k+2+fontRenderer.getStringWidth(title+"> "), 2+j, this.width - 2, 14+j, Integer.MIN_VALUE);
             	fontRenderer.drawStringWithShadow(fontRenderer.trimStringToWidth(descriptions[i], this.width-fontRenderer.getStringWidth(title+"> ")-6-fontRenderer.getStringWidth(ending))+ending, k+4+fontRenderer.getStringWidth(title+"> "), 4+j, 0xFFFFFF);
         	}
     	}
     }
     
     /**
      * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
      * @param c char version of the key typed
      * @param key int version of the key typed
      */
     protected void keyTyped(char c, int key) {
     	if(key == Keyboard.KEY_ESCAPE) {
     		if(msg.length()>0) {
     			selected = 0;
     			msg = "";
     		}
     		else
     			this.mc.displayGuiScreen(null);
     		historyPoint = history.length;
     	}
     	else if(key == Keyboard.KEY_RETURN) {
     		processStringAsCommand(msg);
     		selected = 0;
     		msg = "";
 			this.mc.displayGuiScreen(null);
     	}
     	else if(key == Keyboard.KEY_BACK)	{
     		if(msg.substring(0,msg.length()-selected).length()>0)
     			this.msg = msg.substring(0, msg.length()-selected-1)+msg.substring(msg.length()-selected,msg.length());
     	}
     	else if(key == Keyboard.KEY_UP) {
 			if(historyPoint > 0)
 				historyPoint--;
     		if(history.length > 0) {
     			this.msg = history[historyPoint];
     		}
     	}
     	else if(key == Keyboard.KEY_DOWN) {
 			if(historyPoint < history.length) {
 				historyPoint++;
 			}
     		if(historyPoint < history.length) {
     			this.msg = history[historyPoint];
     		}
     		if(historyPoint == history.length) {
     			msg = "";
     		}
     	}
     	else if(key == Keyboard.KEY_RIGHT) {
     		if(this.selected > 0)
     			this.selected--;
     	}
     	else if(key == Keyboard.KEY_LEFT) {
     		if(this.selected < msg.length())
     			this.selected++;
     	}
    	else if(ChatAllowedCharacters.isAllowedCharacter(c)) {
     		msg = msg.substring(0, msg.length()-selected)+c+msg.substring(msg.length()-selected,msg.length());
     	}
     }
     
     /**
      * Takes in a string as command input, then initiates the command.
      * @param input string to be processed as command
      */
     protected void processStringAsCommand(String input) {
     	addToHistory(input);
     	if(input.startsWith("/"))    		
     		FMLClientHandler.instance().getClient().thePlayer.sendChatMessage(input);
     	else
     		FMLClientHandler.instance().getClient().thePlayer.sendChatMessage("/"+input);
     }
     
     
     /**
      * Called from the main game loop to update the screen.
      */
     public void updateScreen() {
     	counter++;
     	/*
     	if(counter%increment == 0)
     	debug.println(counter/increment);
     	*/
     }
 	
     /**
      * Adds command lines and descriptions (to be later used for suggestions).
      */
 	public void addCommandLines() {
 		addCom("debug","debug [start:stop] - Starts or stops debug profiling.");
 		addCom("defaultgamemode","defaultgamemode <mode> - Switches the world's default gamemode to Adventure, Creative, or Survival.");
 		addCom("difficulty","difficulty <new difficulty> - Switches the world's difficulty between Peaceful, Easy, Normal or Hard.");
 		addCom("gamemode","gamemode <mode> [player] -  Switches the current game mode to Survival, Creative, or Adventure.");
 		addCom("gamerule","gamerule <rule name> [value] - Allows the player to adjust several base settings.");
 		addCom("give","give <player> <block id> [amount] [data value] - Gives item/block of specified amount, using specified data value (doesn't work to give entities).");
 		addCom("help","help [page:command name] - Displays all available commands and how to use them.");
 		addCom("?","? [page:command name] - Displays all available commands and how to use them.");
 		addCom("kill","kill - Suicide.");
 		addCom("me","me <actiontext> - Sends a narrative message to the other players.");
 		addCom("publish","publish - Opens your single-player game for LAN friends to join.");
 		addCom("say","say <message> - Sends a message as the \"Server\".");
 		addCom("seed","seed - Displays the current seed for the world.");
 		addCom("spawnpoint","spawnpoint [player] [x] [y] [z] - Sets the spawnpoint of the target player to the current or entered destination.");
 		addCom("time","time <set:add> <value> - Sets time to specified integer - 0 is dawn, 6000 is midday, 12000 is dusk, and 18000 is midnight.");
 		addCom("toggledownfall","toggledownfall - Toggles rain/snow.");
 		addCom("tp","tp [target player] <destination player>, <x> <y> <z> - Teleports target player to entered destination.");
 		addCom("weather","weather <clear:rain:thunder> [duration in seconds] - Sets the weather.");
 		addCom("xp","xp <amount> [player] - Grants experience of the specified amount");
 		addCom("clear","clear <player> [item] [data] - Clears a player's current inventory or specific blocks/items.");
 	}
 	
 	/**
 	 * Adds a string to the {@link #comList comList} array
 	 * and to the {@link #descList descList} array.
 	 * <br />
 	 * This method also changes the color of the text to match the console formatting correctly.
 	 * @param command the string to be added to the {@link #comList comList} array
 	 * @param description the string to be added to the {@link #descList descList} array
 	 */
 	public void addCom(String command, String description) {
 		description = c.LightPurple+description;
 		description=description.replaceFirst("-", c.Green+"-"+c.LightGray);
 		description=replaceWithColor(description, "\\]", c.Green);
 		description=replaceWithColor(description, "\\[", c.Green);
 		description=replaceWithColor(description, ":", c.Green);
 		description=replaceWithColor(description, "<", c.Green);
 		description=replaceWithColor(description, ">", c.Green);
 		
 		comList.add(command);
 		descList.add(description);
 		
 		lines = (String[]) comList.toArray(lines);
 		descriptions = (String[]) descList.toArray(descriptions);
 	}
 	
 	/**
 	 * Adds string to {@link #histList history}.
 	 * @param commandLine the string to be added
 	 */
 	public void addToHistory(String commandLine) {
 		histList.add(commandLine);
 		history = (String[]) histList.toArray(history);
 		
 		historyPoint = history.length;
 	}
 	
 	/**
 	 * Uses the replaceAll string method to add color to the givin string.
 	 * @param fullString the string to be edited
 	 * @param string the string to be detected
 	 * @param color the color code to be used
 	 * @return 
 	 */
 	public String replaceWithColor(String fullString, String string, String color) {
 		return fullString.replaceAll(string, color+string+c.White);
 	}
 	
 	public static GuiConsole getInstance() {
 		if(instance == null)
 		{
 			instance = new GuiConsole();
 		}
 		return instance;
 	}
     
     /**
      * Called when the mouse is clicked.
      */
     protected void mouseClicked(int par1, int par2, int par3){}
     
     /**
      * Returns true if this GUI should pause the game when it is displayed in single-player
      */
     public boolean doesGuiPauseGame() {return false;}
     
     /**
      * Called when the screen is unloaded. Used to disable keyboard repeat events
      */
     public void onGuiClosed()
     {
         Keyboard.enableRepeatEvents(false);
     }
 
 }
