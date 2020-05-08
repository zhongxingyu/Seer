 package swag49.web.model;
 
 import swag49.model.*;
 
 import java.util.Set;
 
 public class TileOverviewDTOFull {
 	private Integer x;
 	private Integer y;
 
 	private String info;
 	private ResourceType specialResource;
 
 	private boolean enemyTerritory;
 
     private Set<Troop> troops;
     private Base base;
 
     private Set<Square> squares;
 
     public Set<Square> getSquares() {
         return squares;
     }
 
     public void setSquares(Set<Square> squares) {
         this.squares = squares;
     }
 
 
     public boolean isHasTroops() {
         if(getTroops() != null)
         {
             if(!getTroops().isEmpty())
             {
                  return true;
             }
         }
         return false;
     }
 
     public boolean isHasBase() {
 		if(getBase() != null)
             return true;
         return false;
 	}
 
     public TileOverviewDTOFull(Tile tile) {
 		this.x = tile.getId().getX();
 		this.y = tile.getId().getY();
 	}
 
 	public Integer getX() {
 		return x;
 	}
 
 	public void setX(Integer x) {
 		this.x = x;
 	}
 
 	public Integer getY() {
 		return y;
 	}
 
 	public void setY(Integer y) {
 		this.y = y;
 	}
 
 	public String getInfo() {
 		return info;
 	}
 
 	public void setInfo(String info) {
 		this.info = info;
 	}
 
 	public void setSpecialResource(ResourceType special) {
 		this.specialResource = special;
 	}
 
 	public ResourceType getSpecialResource() {
 		return specialResource;
 	}
 
 
 
 	public boolean isEnemyTerritory() {
 		return enemyTerritory;
 	}
 
 	public void setEnemyTerritory(boolean enemyTerritory) {
 		this.enemyTerritory = enemyTerritory;
 	}
 
     public Set<Troop> getTroops() {
         return troops;
     }
 
     public void setTroops(Set<Troop> troops) {
         this.troops = troops;
     }
 
     public Base getBase() {
         return base;
     }
 
     public void setBase(Base base) {
         this.base = base;
     }
 }
