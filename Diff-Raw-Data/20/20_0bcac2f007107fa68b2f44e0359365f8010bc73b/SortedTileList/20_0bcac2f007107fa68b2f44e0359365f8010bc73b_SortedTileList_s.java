 package domain.map;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.awt.image.ImageObserver;
 import java.util.TreeSet;
 
 import Items.Item;
 
 import domain.Player;
 import domain.Tile;
 import domain.useble;
 import domain.backGroundTile.Grass;
 import domain.backGroundTile.Water;
 
 public class SortedTileList extends TreeSet<Tile> {
 	BufferedImage over,under=null;
 	int x,y=0;
 	boolean updateOnChange=false;
 	public SortedTileList(int x, int y) {
 		super();
 		this.x=x;
 		this.y=y;
 	}
 	Tile getTileOfType(Tile tile){
 		for(Tile loop:this){
 			if(loop.getClass()==tile.getClass())//TODO shood get a bether way
 				return loop;
 		}
 		return null;
 	}
 	boolean removeTileOfType(Tile tile){
 		Tile remove=getTileOfType(tile);
 		if(remove!=null){
 			return this.remove(remove);
 		}
 		return false;
 	}
 	boolean existTileOfType(Tile tile){
 		for(Tile loop:this){
 			if(loop.getClass()==tile.getClass()){
 				return true;
 			
 			}
 		}
 		return false;
 	}
 	
 	public void addTile(Tile tile){
		System.out.println("adding tile "+tile);
 		this.add(tile);
 		if(updateOnChange){
			System.out.println("updateing:"+x+":"+y);
 			calculateImage();
 		}
 			
 	}
 	void removeTile(Tile tile){
 	
 			this.remove(tile);
 			if(updateOnChange){
 				calculateImage();
 			}
 	}
 	
 	
 	public void calculateImage(){
 		updateOnChange=true;
 		do{
 			over = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
 			Graphics overG = over.getGraphics();
 			
 			under = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
 			Graphics underG = under.getGraphics();
 			
 			for(Tile tile:this){
 				if(tile.getZValue()>100){
 					tile.drawItSelf(overG, null, 0, 0,32,32, 1);
 				}else{
 					tile.drawItSelf(underG,null, 0, 0,32,32, 1);
 				}
 			}
 		}while(size()<1);
 	}
 	public void drawUnderIT( Graphics g,ImageObserver io, int x,int y,float zoom){
 		
 		g.drawImage(under, x,y,x+(int) (32*zoom),y+(int)(32*zoom),0, 0,(int)(32*zoom),(int) (32*zoom), io);
 	}
 	public void drawOverIT( Graphics g,ImageObserver io, int x,int y,float zoom){
 		//Graphics overG = over.getGraphics();
 		//overG.drawChars((size()+"").toCharArray(), 0, 1, 10, 10);
 		g.drawImage(over, x,y,x+(int) (64*zoom),y+(int)(64*zoom),0, 0, 64,64, io);
 	}
 	public Item pickUp(Player player){
 		
 		Item item=null;
 		System.out.println(size());
 		for(Tile tile:this){
 			System.out.println(x +" : "+ y);
 			System.out.println(tile);
 			System.out.println(tile.getClass());
 			System.out.println(tile.getClass().getSuperclass());
 			
 			if(tile.getClass().getSuperclass().equals(Item.class)) {
 				item=(Item)tile;
 				System.out.println("pickup mushrom");
 			}
 		}if(item!=null){
 			removeTile(item);
 		}
 		return item;
 	}
 	public void use(Player pl) {
 		
 		for(Tile tile:this){
 			System.out.println(tile);
 			System.out.println(tile.getClass());
 			System.out.println(tile.getClass().getSuperclass());
 			if(tile  instanceof useble){
 				System.out.println("saken används!!!");
 				System.out.println("updateing:"+x+":"+y);
 				((useble) tile).use(pl);
 				break;
 			}
 		}
 	}
 }
