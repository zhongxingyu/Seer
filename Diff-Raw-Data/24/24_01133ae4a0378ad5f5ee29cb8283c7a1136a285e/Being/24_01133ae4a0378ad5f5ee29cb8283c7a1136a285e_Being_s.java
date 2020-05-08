 package concept.being;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.List;
 
 import map.GeoMap;
 import br.com.etyllica.core.video.Graphic;
 import concept.Concept;
 import concept.nutrient.Need;
 import concept.nutrient.Nutrient;
 
 public abstract class Being extends Concept{
 
 	private double metabolism = 1f;
 
 	private List<Need> needs;
 
 	protected int interactionRadius = 42;
 
 	private GeoMap geomap;
 
 	public Being(int x, int y) {
 		super(x, y);
 
 		geomap = new GeoMap();
 
 		needs = new ArrayList<Need>();
 		
 		setFrames(3);
 		animaEmX = false;		
 		
 	}
 
 	protected void addNeed(Need need){
 		needs.add(need);
 	}
 
 	@Override
 	public void draw(Graphic g){
 
 		g.setColor(colorFill);
 		g.fillOval(x+mapX*TILE_SIZE, y+mapY*TILE_SIZE, TILE_SIZE, TILE_SIZE);
 
 		g.setColor(colorDraw);
 		g.drawOval(x+mapX*TILE_SIZE, y+mapY*TILE_SIZE, TILE_SIZE, TILE_SIZE);
 
 		if(visible){
 
 			if(opacity<0xff){
 				g.setOpacity(opacity);
 			}
 			
 			this.draw(g, getTransform());
 			
 			g.resetTransform();
 
 			if(opacity<0xff){
 				g.resetOpacity();
 			}
 
 		}
 
 	}
 
 	public void drawInteractionRadius(Graphic g){
 		g.setColor(Color.BLACK);
 		g.setAlpha(70);
 		g.fillCircle(x+mapX*TILE_SIZE+(TILE_SIZE/2), y+mapY*TILE_SIZE+(TILE_SIZE/2), interactionRadius);
 		g.setAlpha(100);
 	}
 
 	public void react(){
 
 		for(Need need: needs){
 
 			if(need.getAmountReched()<need.getAmountPerDay()){
 
 				Concept concept = geomap.search(need.getNutrient());
 
 				if(concept!=null){
 					walkTo(concept.getX(), concept.getY());
 				}
 
 			}
 
 		}
 
 	}	
 
 	private void walkTo(int x, int y){
 
 		if(this.y<y){
 			this.y++;
 		}
 		else if(this.y>y){
 			this.y--;
 		}
 
 		if(this.x<x){
 			this.x++;
 			this.xImage = 1*TILE_SIZE;
 		}
 		else if(this.x>x){
 			this.x--;
 			this.xImage = 3*TILE_SIZE;
 		}
 
 		animate();
 	}
 
 	public GeoMap getGeomap() {
 		return geomap;
 	}
 
 	public void setGeomap(GeoMap geomap) {
 		this.geomap = geomap;
 	}
 
 	public List<Need> getNeeds() {
 		return needs;
 	}
 
 	public void setNeeds(List<Need> needs) {
 		this.needs = needs;
 	}
 
 	public boolean haveNeed(Nutrient nutrient){
 
 		for(Need need: needs){
 
 			if(need.getNutrient()==nutrient){
 				return true;
 			}
 
 		}
 
 		return false;
 	}
 
 }
