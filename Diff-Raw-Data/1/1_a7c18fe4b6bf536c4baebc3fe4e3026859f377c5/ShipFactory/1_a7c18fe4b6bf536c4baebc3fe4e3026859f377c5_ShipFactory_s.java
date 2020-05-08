 package fr.umlv.escape.ship;
 
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.Filter;
 
 import android.graphics.Bitmap;
 
 import fr.umlv.escape.Objects;
 import fr.umlv.escape.bonus.BonusFactory;
 import fr.umlv.escape.front.FrontApplication;
 import fr.umlv.escape.front.SpriteShip.SpriteType;
 import fr.umlv.escape.game.Game;
 import fr.umlv.escape.move.DownMove;
 import fr.umlv.escape.move.KamikazeMove;
 import fr.umlv.escape.move.LeftDampedMove;
 import fr.umlv.escape.move.LeftMove;
 import fr.umlv.escape.move.LeftRightMove;
 import fr.umlv.escape.move.Movable;
 import fr.umlv.escape.move.PlayerMove;
 import fr.umlv.escape.move.RightDampedMove;
 import fr.umlv.escape.move.RightMove;
 import fr.umlv.escape.move.SquareLeft;
 import fr.umlv.escape.move.SquareRight;
 import fr.umlv.escape.move.UpMove;
 import fr.umlv.escape.weapon.BatShipShoot;
 import fr.umlv.escape.weapon.DoNotShoot;
 import fr.umlv.escape.weapon.FirstBossShoot;
 import fr.umlv.escape.weapon.ListWeapon;
 import fr.umlv.escape.weapon.SecondBossShoot;
 import fr.umlv.escape.weapon.ShootDown;
 import fr.umlv.escape.weapon.ShootPlayer;
 import fr.umlv.escape.weapon.ThirdBossShoot;
 import fr.umlv.escape.world.Bodys;
 import fr.umlv.escape.world.EscapeWorld;
 
 /**This class supplies methods to create properly a {@link Ship}.
  */
 public class ShipFactory {
 	private static ShipFactory TheShipFactory;
 	private static int ghostShoot=0;
 
 	private ShipFactory(){
 	}
 
 	private enum EnumTrajectory {
 		PlayerMove,
 		LeftMove,
 		RightMove,
 		DownMove,
 		UpMove,
 		SquareRight,
 		SquareLeft,
 		StraightLine,
 		LeftDampedMove,
 		RightDampedMove,
 		KamikazeMove,
 		LeftRight
 	}
 	
 	private enum EnumShipName {
 		default_ship_player,
 		default_ship,
 		kamikaze_ship,
 		bat_ship,
 		first_boss,
 		second_boss,
 		third_boss
 	}
 
 	/**
 	 * Create a {@link Ship}.
 	 * 
 	 * @param shipName the name of the ship to create.
 	 * @param posX the x position of the ship to create.
 	 * @param posY the y position of the ship to create.
 	 * @param health the health of the ship to create.
 	 * @param trajectory the name of the move behavior of the ship.
 	 * @return the ship created.
 	 */
 	public Ship createShip(String shipName,int posX, int posY,int health, String trajectory){
 		Objects.requireNonNull(shipName);
 		
 		Movable move;
 		EnumTrajectory enumTrajectory = EnumTrajectory.valueOf(trajectory);
 		switch (enumTrajectory){
 			case PlayerMove:
 				move=new PlayerMove();
 				break;
 			case LeftMove:
 				move=new LeftMove();
 				break;
 			case RightMove:
 				move=new RightMove();
 				break;
 			case DownMove:
 				move=new DownMove();
 				break;
 			case UpMove:
 				move=new UpMove();
 				break;
 			case SquareRight:
 				move=new SquareRight();
 				break;
 			case SquareLeft:
 				move=new SquareLeft();
 				break;
 			case StraightLine:
 				move=new UpMove();
 				break;
 			case LeftDampedMove:
 				move=new LeftDampedMove();
 				break;
 			case RightDampedMove:
 				move=new RightDampedMove();
 				break;
 			case KamikazeMove:
 				move=new KamikazeMove();
 				break;
 			case LeftRight:
 				move=new LeftRightMove();
 				break;
 			default:
 				throw new IllegalArgumentException(trajectory+"not accepted");
 		}
 		Bitmap img = FrontApplication.frontImage.getImage(shipName);
 		
 		Body body=Bodys.createBasicRectangle((posX+((float)img.getWidth()/2)), (posY+((float)img.getHeight()/2)), img.getWidth(), img.getHeight(), 0);
 		Filter filter=new Filter();
 		if(shipName.equals("default_ship_player")){
 			filter.categoryBits=EscapeWorld.CATEGORY_PLAYER;
 			filter.maskBits=EscapeWorld.CATEGORY_BONUS | EscapeWorld.CATEGORY_ENNEMY | EscapeWorld.CATEGORY_BULLET_ENNEMY | EscapeWorld.CATEGORY_DECOR;
 			body.setLinearDamping(3);
 			
 		}else{
 			filter.categoryBits=EscapeWorld.CATEGORY_ENNEMY;
 			filter.maskBits=EscapeWorld.CATEGORY_PLAYER | EscapeWorld.CATEGORY_BULLET_PLAYER;
 		}
 		body.getFixtureList().setFilterData(filter);
 		Ship ship;
 		
 		EnumShipName enumShipName = EnumShipName.valueOf(shipName);
 		switch (enumShipName){
 		case default_ship:
 			ship = new Ship("DefaultShip",health,body,img,move,new ShootDown());
 			ship.getCurrentWeapon().setInfinityQty();
 			ship.getCurrentWeapon().setGhostShoot(ghostShoot);
 			ghostShoot=(ghostShoot+1)%7;
 			break;
 		case default_ship_player:
 			ship= new PlayerShip("DefaultShipPlayer",health,body,img,move,new ShootPlayer());
 			ship.getListWeapon().setCurrentWeapon("missile_launcher");
 			ship.getCurrentWeapon().addQte(ListWeapon.BASIC_QTY_BULLET);
 			ship.setCurrentSprite(SpriteType.BASIC_IMAGE_PLAYER);
 			body.setActive(true);
 			break;
 		case kamikaze_ship:
 			ship = new Ship("KamikazeShip",health,body,img,move,new DoNotShoot());
 			ship.getCurrentWeapon().addQte(Integer.MIN_VALUE);
 			break;
 		case bat_ship:
 			ship = new Ship("BatShip",health,body,img,move,new BatShipShoot());
 			ship.getListWeapon().addWeapon("flame_thrower",Integer.MIN_VALUE);
 			ship.getListWeapon().setCurrentWeapon("flame_thrower");
 			break;
 		case first_boss:
 			ship = new FirstBoss(health,body,img,move,new ShootDown());
 			ship.setShootBehaviour(new FirstBossShoot((FirstBoss) ship));
 			ship.getListWeapon().addWeapon("shiboleet_thrower",Integer.MIN_VALUE);
 			ship.getListWeapon().setCurrentWeapon("shiboleet_thrower");
 			break;
 		case second_boss:
 			ship = new SecondBoss(health,body,img,move,new ShootDown());
 			ship.setShootBehaviour(new SecondBossShoot((SecondBoss) ship));
 			ship.getListWeapon().addWeapon("shiboleet_thrower",Integer.MIN_VALUE);
 			ship.getListWeapon().setCurrentWeapon("shiboleet_thrower");
 			break;
 		case third_boss:
 			ship = new ThirdBoss(health,body,img,move,new ShootDown());
 			ship.setShootBehaviour(new ThirdBossShoot((ThirdBoss) ship));
 			ship.getListWeapon().addWeapon("missile_launcher",Integer.MIN_VALUE);
 			ship.getListWeapon().addWeapon("flame_thrower",Integer.MIN_VALUE);
 			ship.getListWeapon().addWeapon("shiboleet_thrower",Integer.MIN_VALUE);
 			ship.getListWeapon().setCurrentWeapon("shiboleet_thrower");
 			break;
 		default:
 			throw new IllegalArgumentException(shipName+"not accepted");
 		}
 		return ship;
 	}
 	
 	/** Get the unique instance of {@link BonusFactory}
 	 * @return The unique instance of {@link BonusFactory}
 	  */
 	public static ShipFactory getTheShipFactory(){
 		if(ShipFactory.TheShipFactory==null){
 			ShipFactory.TheShipFactory = new ShipFactory();
 		}
 		return ShipFactory.TheShipFactory;
 	}
 }
