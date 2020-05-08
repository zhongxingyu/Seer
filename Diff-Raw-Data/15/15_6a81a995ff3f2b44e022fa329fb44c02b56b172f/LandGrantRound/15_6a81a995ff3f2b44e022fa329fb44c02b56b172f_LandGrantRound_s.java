 package game.round;
 
 import game.Character;
 import game.Map;
 import game.Plot;
 import game.Session;
 
 import java.util.ArrayList;
 
 import core.Button;
 import core.render.RenderableString;
 import core.render.SimpleRender;
 
 /**
  * 
  * @author grant
  * @author
  */
 public class LandGrantRound extends Round
 {	
 	private SimpleRender characterOverview;
 	private RenderableString prompt;
 	
 	private ArrayList<Character> characters;
 	private int currentCharacterIndex;
 	private int metaRound;
 	
 	private Button passButton;
 	
 	
 	public LandGrantRound(Session session)
 	{	
 		super(session);
 		
 		metaRound = 0;
 		
 		characterOverview = new SimpleRender("assets/images/characterStatBackground.png");
 		characterOverview.setX(0);
 		characterOverview.setY(350);
 
 		currentCharacterIndex = 0;
 		
 		characters = new ArrayList<Character>();
 		for (Character character : session.getCharacters())
 		{
 			//
 			// can randomize order here since we transverse
 			// through the list one character at a time
 			//
 			characters.add(character);
 		}
 		prompt = new RenderableString();
 		prompt.setX(250);
 		prompt.setY(390);
 		
 		passButton = new Button("assets/images/passButton.png");
 		passButton.setX(550);
 		passButton.setY(360);
 		passButton.setWidth(50);
 		passButton.setHeight(30);
 	}
 
 	/**
 	 * Alert the state that an mouse click occurred
 	 * @param x The x pos in pixels
 	 * @param y The y pos in pixels
 	 * @param leftMouse Whether the left mouse was pressed
 	 */
 	public void click(int x, int y, boolean leftMouse)
 	{
 		//
 		// since we are working off of mouse input,
 		// most of the logic will happen here.
 		// update will grab the renderable images.
 		//
 		// if we had a timer for the player to click,
 		// we would do something like...
 		//
 		//		public void update()
 		//		{
 		//			timer--;
 		//			if (timer < 0)
 		//			{
 		//				System.out.println("times up!");
 		//				currentCharacterIndex++;
 		//			}
 		//			
 		//			... render ...
 		//		}
 		
		int xGridPos = (int)Math.floor(x / Plot.SIZE);
		int yGridPos = (int)Math.floor(y / Plot.SIZE);
 
 		System.out.println(characters.get(currentCharacterIndex).getName() + " selected plot(x:" + xGridPos + ", y:" + yGridPos + ")");
 		if(passButton.inBounds(x,y)){
                characters.remove(currentCharacterIndex);
         }else {
                Map map = session.getMap();
                Plot plot = map.get(xGridPos,yGridPos);
                plot.setIsOwned(true);
                characters.get(currentCharacterIndex).addPlot(plot);
                currentCharacterIndex =(++currentCharacterIndex)%characters.size();
                     
                 }
 		
 		if (isDone())
 		{
 			System.out.println("done selecting plots!");
 		}
 	}
 	
 	public void update() 
 	{
 		renderables.clear();
 		renderableStrings.clear();
 		
 		// plots
 		for (int a = 0; a < 5; a++)
 		{
 			for (int b = 0; b < 9; b++)
 			{
 				Map map = session.getMap();
 				Plot plot = map.getPlot(a, b);
 				renderables.add(plot);
 			}
 		}
 
 		renderables.add(characterOverview);
 		
 		ArrayList<Character> characters = session.getCharacters();
 		Character character = characters.get(currentCharacterIndex);
 		
 		
 		renderables.add(passButton);
 		
 		
 		prompt.setText(character.getName() + " please select a plot");
 		renderableStrings.add(prompt);
 		
 		RenderableString name = new RenderableString(character.getName(), 15, 364);
 		renderableStrings.add(name);
 		
 		RenderableString money = new RenderableString("$" + character.getMoney(), 30, 380);
 		renderableStrings.add(money);
 
 		RenderableString ore = new RenderableString("" + character.getOre(), 30, 395);
 		renderableStrings.add(ore);
 
 		RenderableString food = new RenderableString("" + character.getFood(), 30, 410);
 		renderableStrings.add(food);
 
 		RenderableString energy = new RenderableString("" + character.getEnergy(), 90, 395);
 		renderableStrings.add(energy);
 
 		RenderableString crystite = new RenderableString("" + character.getCrystite(), 90, 410);
 		renderableStrings.add(crystite);
 	}
 
 	public boolean isDone() 
 	{
 		if (0 == characters.size())
 		{
 			return true;
 		}
 		
 		return false;
 	}
 }
