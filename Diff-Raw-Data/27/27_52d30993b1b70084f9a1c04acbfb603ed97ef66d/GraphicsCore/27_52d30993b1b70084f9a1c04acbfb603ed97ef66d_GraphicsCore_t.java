 /**
  * 
  */
 package net.esiade.client;
 
 import java.util.ArrayList;
 
 import net.esiade.client.sprite.Food;
 import net.esiade.client.sprite.Individual;
 import net.esiade.client.sprite.Obstacle;
 
 import com.google.gwt.user.client.Random;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.Widget;
 import com.google.gwt.canvas.client.Canvas;
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.canvas.dom.client.CssColor;
 import java.util.HashMap;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 
 
 /**
  * @author spydon
  *
  */
 public class GraphicsCore {
 	private Timer timer;
 	private Canvas canvas, canvasBuffer;
 	private Context2d context, contextBuffer;
 	public static int WIDTH, HEIGHT;
 	private final int REFRESH_RATE = 20;
 	private int day = 0;
 	private int epochLength;
 	private boolean isEpochBased;
 	private final CssColor REDRAW_COLOR = CssColor.make("red");
     private ArrayList<Individual> individuals;
     private ArrayList<Obstacle> obstacles;
     private ArrayList<Food> foods;
 	private ArrayList<Poisson> poissons;
     private CollisionManager collisionManager;
    private Label l_day, l_ind, l_food, l_obs, l_epoch;
     private HashMap<String, Widget> state;
 
 	public GraphicsCore(ArrayList<Individual> individuals,
 						ArrayList<Obstacle> obstacles,
 						ArrayList<Food> foods,
 						ArrayList<Poisson> poissons,
 						CollisionManager collisionManager, 
 						int epochLength,
 						boolean isEpochBased,
 						HashMap<String, Widget> state) {
 		GraphicsCore.WIDTH = Esiade.WIDTH;
 		GraphicsCore.HEIGHT = Esiade.HEIGHT;
 		this.individuals = individuals;
 		this.obstacles = obstacles;
 		this.foods = foods;
 		this.poissons = poissons;
 		this.collisionManager = collisionManager;
 		this.epochLength = epochLength;
 		this.isEpochBased = isEpochBased;
 		this.state = state;
 		
 		l_day = new Label("Day: " + day);
		l_epoch = new Label("Epoch: " + (int)(day/epochLength));
 		l_ind = new Label("Alive individuals: " + individuals.size());
 		l_food = new Label("Food in environment: " + foods.size());
 		l_obs = new Label("Obstacles in environment: " + obstacles.size());
 		
 		canvas = Canvas.createIfSupported();
 		canvas.setWidth(WIDTH + "px");
 		canvas.setHeight(HEIGHT + "px");
 		canvas.setCoordinateSpaceWidth(WIDTH);
 		canvas.setCoordinateSpaceHeight(HEIGHT);
 		canvas.setVisible(true);
 		
 		canvasBuffer = Canvas.createIfSupported();
 		canvasBuffer.setWidth(WIDTH + "px");
 		canvasBuffer.setHeight(HEIGHT + "px");
 		canvasBuffer.setCoordinateSpaceWidth(WIDTH);
 		canvasBuffer.setCoordinateSpaceHeight(HEIGHT);
 		
 		context = canvas.getContext2d();
 		context.setFillStyle(REDRAW_COLOR);
 		context.fillRect(0, 0, WIDTH, HEIGHT);
 		contextBuffer = canvasBuffer.getContext2d();
 		
 		Button randomInd = new Button("Random individual stats");
 		randomInd.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				ArrayList<Individual> individuals = getIndividuals();
 				StatisticsCore.individualResult(individuals.get(Random.nextInt(individuals.size())));
 			}
 		});
 		
 		Button settings = new Button("<- Settings");
 		settings.addClickHandler(new ClickHandler() {
 			@Override
 			public void onClick(ClickEvent event) {
 				timer.cancel();
 				RootPanel.get("canvasholder").clear();
 				RootPanel.get("settingsholder").clear();
 				RootPanel.get("statisticsholder").clear();
 				RootPanel.get().clear();
 				new Esiade(getState());
 			}
 		});
 
 		RootPanel.get("canvasholder").add(canvas);
 		
 		RootPanel.get("statisticsholder").add(settings);
 		RootPanel.get("statisticsholder").add(randomInd);
 		RootPanel.get("statisticsholder").add(l_day);
		RootPanel.get("statisticsholder").add(l_epoch);
 		RootPanel.get("statisticsholder").add(l_ind);
 		RootPanel.get("statisticsholder").add(l_food);
 		RootPanel.get("statisticsholder").add(l_obs);
 
 		onModuleLoad();
 	}
 	
 	public HashMap<String, Widget> getState() {
 		return state;
 	}
 	
 	public void onModuleLoad() {
 	    timer = new Timer() {
 	        @Override
 	        public void run() {
 	          if(individuals.size() == 0)
 	        	  this.cancel();
 	          doUpdate();
 	        }
 	      };
 	      timer.scheduleRepeating(REFRESH_RATE);
 	}
 	
 	public ArrayList<Individual> getIndividuals() {
 		return individuals;
 	}
 	
 	private void doUpdate() {
 		day++;
 		contextBuffer.setFillStyle(CssColor.make("GREEN"));
 		contextBuffer.fillRect(0, 0, WIDTH, HEIGHT);
 		if(!isEpochBased) {
 			collisionManager.checkCollision(false);		
 			for(Individual i : individuals) {
 				i.updatePos();
 				if(day%i.getStarveRate()==0)
 					i.starve();
 				if(i.getFood() <= 0) {
 					if(individuals.size()==1)
 						StatisticsCore.individualResult(i);
 					individuals.remove(i);
 				}
 				i.draw(contextBuffer);
 			}
 		} else {
 			collisionManager.checkCollision(true);
 			for(Individual i : individuals) {
 				i.updatePos();
 				i.draw(contextBuffer);
 			}
 			if(day%epochLength==0)
 				individuals = EvolutionCore.EpochReproduction(individuals);
 		}
 		
 		if(Random.nextDouble() <= Food.spawnRate)
 			foods.add(new Food(poissons.get(Random.nextInt(poissons.size())).getVector()));
 		
 		for(Food f : foods)
 			f.draw(contextBuffer);
 		
 		for(Obstacle o : obstacles)
 			o.draw(contextBuffer);
 		
 		updateStatistics();
 	    context.drawImage(contextBuffer.getCanvas(), 0, 0);
 	}
 	
 	private void updateStatistics() {
 		l_day.setText("Day: " + day);
		l_epoch.setText("Epoch: " + (int)(day/epochLength));
 		l_ind.setText("Alive individuals: " + individuals.size());
 		l_food.setText("Food in environment: " + foods.size());
 		l_obs.setText("Obstacles in environment: " + obstacles.size());
 	}
 }
