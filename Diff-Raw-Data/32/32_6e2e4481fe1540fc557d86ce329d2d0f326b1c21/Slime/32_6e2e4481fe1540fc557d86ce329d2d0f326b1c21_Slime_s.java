 package ntu.csie.oop13spring;
 
 import java.awt.Color;
 import java.awt.Image;
 import javax.swing.ImageIcon;
 import java.util.Random;
 
 public class Slime extends Pet{
 	
 	static {
 		_no_img = 10;
 		_imgs = new Image[_no_img];
 		_imgs[0] = Filter.filterOutBackground((new ImageIcon("Images/Slime.png")).getImage(), new Color(0, 0, 0));
 		_imgs[1] = Filter.filterOutBackground((new ImageIcon("Images/Slime_2.png")).getImage(), new Color(0, 0, 0));
 		_imgs[2] = Filter.filterOutBackground((new ImageIcon("Images/Red_Slime.png")).getImage(), new Color(0, 0, 0));
 		_imgs[3] = Filter.filterOutBackground((new ImageIcon("Images/Red_Slime_2.png")).getImage(), new Color(0, 0, 0));
 		_rnd = new Random();
 	}
 	
 	protected POOConstant.Dir _direction;
 	protected static Random _rnd;
 
 	public Slime(){
 		
 		
 		_img_id = _rnd.nextInt(2);
 		_sight_range = 4;
 		_tta = 3;
 		_count_down = _tta;
 		
 		
 		setHP(10);
 		setMP(1);
 		setAGI(10);
 		
 	}
 	
 	protected boolean beAngry(){
 		if(getAngry())
 			return false;
 		
 		setAngry();
 		_img_id = _rnd.nextInt(2) + 2;
 		return true;
 	}
 	
 	protected POOAction act(POOArena arena){
 		POOAction action = new POOAction();
 		action.skill = new TinyAttackSkill();
 		return action;
 	}
 	
 	protected POOCoordinate move(POOArena arena){
 		POOCoordinate pos = arena.getPosition(this);
 		POOCoordinate border = ((Arena)arena).getSize();
 		switch(_direction){
 			case RIGHT:
 				if(pos.x + 1 < border.x)
 					pos.x += 1;
 				break;
 			case DOWN:
 				if(pos.y + 1 < border.y)
 					pos.y += 1;
 				break;
 			case LEFT:
 				if(pos.x - 1 >= 0)
 					pos.x -= 1;
 				break;
 			case UP:
 				if(pos.y - 1 >= 0)
 					pos.y -= 1;
 				break;
 			default:;
 		}
 		return pos;
 	}
 	
 	public Action Strategy(POOArena arena){
 		
 		_sight = ((Arena)arena).getSight((POOPet)this);
 		boolean found = false;
 		for(int i = 0; i < 2*_sight_range+1; i++){
 			for(int j = 0; j < 2*_sight_range+1; j++){
 				if(_sight[i][j] != null && _sight[i][j].getType() != POOConstant.Type.EMPTY && (i!=_sight_range || j!=_sight_range) ){
 					beAngry();
 					if(getMP() > 0 && ((i==_sight_range && (j==_sight_range-1 || j==_sight_range+1)) || (j==_sight_range && (i==_sight_range-1 || i==_sight_range+1)))){
 						setMP(getMP()-1);
 						POOCoordinate pos = arena.getPosition(this);
 						return new Action (POOConstant.Type.SKILL, new TinyAttackSkill(), new Coordinate(i + pos.x - _sight_range, j + pos.y - _sight_range));
 					}
 					if(i - _sight_range < 0){
 						_direction = POOConstant.Dir.LEFT;
 					}else if(i - _sight_range > 0){
 						_direction = POOConstant.Dir.RIGHT;
 					}else if(j - _sight_range < 0){
 						_direction = POOConstant.Dir.UP;
 					}else if(j - _sight_range > 0){
 						_direction = POOConstant.Dir.DOWN;
 					}
 					//POOCoordinate pos = arena.getPosition(this);
 					//System.out.println("found " + ((Arena)arena).getPetId((Pet)_sight[i][j].getObject()) + " at " + (i+pos.x-_sight_range) + ", " + (j+pos.y-_sight_range));
 					found = true;
 					break;
 				}
 			}
 		}
 		if(!found)
 			_direction = POOConstant.Dir.getRandom();//_rnd.nextInt(4);
 		
 		return new Action(POOConstant.Type.MOVE, move(arena));
 	}
 	
 	public Action OneTimeStep(POOArena arena){
 		if(_count_down <= 0){
 			_count_down = _tta + 1;
 			
 			if(!isPlayer())
 				return Strategy(arena);
 			
 			/* for player control */
 			if(_cmds.isEmpty()){
 				System.out.println("Empty");
 				return null;
 			}
 			switch(_cmds.get(0).get()){
 				case UP:
 					_direction = POOConstant.Dir.UP;
 					System.out.println("UP");
					return new Action(POOConstant.Type.MOVE, move(arena));
 				case DOWN:
 					_direction = POOConstant.Dir.DOWN;
 					System.out.println("DOWN");
					return new Action(POOConstant.Type.MOVE, move(arena));
 				case LEFT:
 					_direction = POOConstant.Dir.LEFT;
 					System.out.println("LEFT");
					return new Action(POOConstant.Type.MOVE, move(arena));
 				case RIGHT:
 					_direction = POOConstant.Dir.RIGHT;
 					System.out.println("RIGHT");
					return new Action(POOConstant.Type.MOVE, move(arena));
 				default:;
 			}
			
 		}else if(_count_down % 2 == 0){
 			if(!getAngry()){
 				_img_id = (_img_id+1)%2;
 			}else{
 				_img_id = (_img_id+1)%2 + 2;
 			}
 		}
 		_count_down -= 1;
 		return null;
 	}
 }
