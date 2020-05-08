 package ntu.csie.oop13spring;
 
 import javax.swing.*;
 import java.util.Random;
 import java.util.ArrayList;
 import java.awt.event.*;
 
 public class ArenaPark extends Arena{
 
 	private Timer _timer;
 	private ArenaFrame _window;
 	private int _no_cell_x = 40;
 	private int _no_cell_y = 20;
 	private Cell[][] _map; 
 	
 	private Random rnd;
 	private int _game_status; /* -1: ?, 0: after init */
 	public static final int interval = 10;
 	
 	private POOPet[] _parr;
 	private Coordinate _pet_pos[];
 	private ArrayList<Cell> _carr;
 	public ArenaPark(){
 		try{
 			_timer = new Timer(interval, this);
 			_map = new Cell[_no_cell_x][];
 			for(int i = 0; i < _no_cell_x; i++){
 				_map[i] = new Cell[_no_cell_y];
 				for(int j = 0; j < _no_cell_y; j++){
 					_map[i][j] = new Cell();
 				}
 			}
 			_window = new ArenaFrame("Park", "Images/Park.png", _no_cell_x, _no_cell_y);
 			_carr = new ArrayList<Cell>(0);
 			_game_status = -1;
 			rnd = new Random();
 		}catch(Exception e){
 			System.out.print("ArenaPark(): ");
 			System.out.println(e);
 		}
 		
 	}
 	
 	public void actionPerformed(ActionEvent e){
 		
 		for(int id = 0; id < _carr.size(); id++){
 			if(_carr.get(id).getObject() instanceof Skill){
 				if(((Skill)_carr.get(id).getObject()).oneTimeStep(_map, _carr.get(id).getPos())){ // should vanish
 					_window.removeFromIOPanel(_carr.get(id).getId());
 					_carr.remove(id);
 					id--;
 				}else{
 					_window.addToArenaIOPanel(((Skill)_carr.get(id).getObject()).getImage(), _carr.get(id).getPos().x, _carr.get(id).getPos().y, _carr.get(id).getId());
 				}
 			}
 		}
 		
 		POOCoordinate prev_pos, new_pos;
 		ArrayList<Action> actions;
 		int tmp;
 		boolean dead;
 		for(int id = _parr.length - 1; id >= 0; id--){
			if(_parr[id] == null)
				continue;
			
 			prev_pos = getPosition(_parr[id]);
 			actions = ((Pet)_parr[id]).OneTimeStep(this);
 			new_pos = prev_pos;
 			dead = false;
 			if(actions == null){
 			}else{
 				Action act;
 				while(!actions.isEmpty()){
 					act = actions.get(0);
 					if(act._type == POOConstant.Type.MOVE){
 						new_pos = act._pos;
 						if((!prev_pos.equals(new_pos)) && _map[new_pos.x][new_pos.y].add(POOConstant.Type.PET, id, new_pos, _parr[id])){
 							_map[prev_pos.x][prev_pos.y].setEmpty();
 							_pet_pos[id] = (Coordinate)new_pos;
 						}else{
 							new_pos = prev_pos;
 						}
 						
 					}else if(act._type == POOConstant.Type.SKILL){
 						tmp = _window.addToArenaIOPanel(act._skill.getImage(), act._pos.x, act._pos.y);
 						_carr.add(new Cell(POOConstant.Type.SKILL, tmp , new Coordinate(act._pos.x, act._pos.y), (Object)(act._skill)));
					}else if(act._type == POOConstant.Type.DEAD && _map[prev_pos.x][prev_pos.y].getType() != POOConstant.Type.DEAD){
 						_map[prev_pos.x][prev_pos.y].setDead();
 						dead = true;
 					}
 					actions.remove(0);
 				}
 			}
 			if(dead){
 				_window.removeFromIOPanel(id);
 				_window.addToBackground(((Pet)_parr[id]).getImage(), new_pos.x, new_pos.y);
				_parr[id] = null;
 			}else{
 				_window.addToArenaIOPanel(((Pet)_parr[id]).getImage(), new_pos.x, new_pos.y, id);
 			}
 		}
 		_window.redraw();
 	}
 	
 	public void init(){
 		try{
 			_parr = getAllPets();
 			_pet_pos = new Coordinate[_parr.length];
 			int x, y;
 			for(int id = 0; id < _parr.length; id++){
 				if(id == 0){
 					((Pet)_parr[id]).setPlayer();
 					_window.addCommandListener(((Pet)_parr[id]).getCmdListener());
 				}
 				
 				while(true){
 					x = rnd.nextInt(_no_cell_x);
 					y = rnd.nextInt(_no_cell_y);
 					if(_map[x][y].add(POOConstant.Type.PET, id, _pet_pos[id], _parr[id])){
 						_pet_pos[id] = new Coordinate(x, y);
 						((Pet)_parr[id]).setId(id);
 						_window.addToArenaIOPanel(((Pet)_parr[id]).getImage(), x, y, id);
 						break;
 					}
 				}
 			}
 		}catch(Exception e){
 			System.out.print("init(): ");
 			System.out.println(e);
 		}
 	}
 	
 	public boolean fight(){
 		while(true){
 			switch(_game_status){
 				case 0:
 					
 					break;
 				case -1:
 					init();
 					_game_status = 0;
 					_timer.start();
 					break;
 				default:;
 			}
 		}
 	}
 	
 	public void show(){
 		_window.redraw();
 	}
 	
 	public POOCoordinate getPosition(POOPet p){
 		for(int id = 0; id < _parr.length; id++){
 			if(p == _parr[id]){
 				return (POOCoordinate) new Coordinate(_pet_pos[id]);
 			}
 		}
 		return null;
 	}
 	
 	public POOCoordinate getSize(){
 		return (POOCoordinate) new Coordinate(_no_cell_x, _no_cell_y);
 	}
 	
 	public int getPetId(POOPet pet){
 		for(int id = 0; id < _parr.length; id++)
 			if(_parr[id] == pet)
 				return id;
 		return -1;
 	}
 	
 	public Cell[][] getMap(){
 		return _map;
 	}
 	
 	public Cell[][] getSight(POOPet pet){
 		int sight_range = ((Pet)pet).getSightRange();
 		Cell[][] sight = new Cell[2*sight_range+1][2*sight_range+1];
 		for(int i = 0; i < 2*sight_range+1; i++){
 			for(int j = 0; j < 2*sight_range+1; j++)
 				sight[i][j] = null;
 		}
 		POOCoordinate pos = getPosition(pet);
 		int x_lower = 0, y_lower = 0, x_upper = _no_cell_x - 1, y_upper = _no_cell_y - 1;
 		if(pos.x - sight_range > x_lower)
 			x_lower = pos.x - sight_range;
 		if(pos.y - sight_range > y_lower)
 			y_lower = pos.y - sight_range;
 		if(pos.x + sight_range < x_upper)
 			x_upper = pos.x + sight_range;
 		if(pos.y + sight_range < y_upper)
 			y_upper = pos.y + sight_range;
 		
 		for(int i = x_lower; i <= x_upper; i++){
 			for(int j = y_lower; j <= y_upper; j++){
 				sight[i - pos.x + sight_range][j - pos.y + sight_range] = new Cell(_map[i][j]);
 			}
 		}
 		return sight;
 	}
 }
 
