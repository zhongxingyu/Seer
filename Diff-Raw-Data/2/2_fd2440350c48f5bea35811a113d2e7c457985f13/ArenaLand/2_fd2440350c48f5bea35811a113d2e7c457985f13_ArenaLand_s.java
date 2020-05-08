 package ntu.csie.oop13spring;
 import java.util.ArrayList;
 import java.util.Random;
 
 
 public class ArenaLand extends POOArena{
 	public final int size = 16;
 	private boolean init =false;
 	private ArrayList<POOPet> allpets = new ArrayList<POOPet>(0);
 	private ArrayList<Coordinate> allposi = new ArrayList<Coordinate>(0);
     private int round = 0;
     private boolean end = false;
     public void addPet(POOPet p){
         allpets.add(p);
         Coordinate c = new Coordinate();
         allposi.add(makeRandomCoordinate(c));
         
     }
     
 	@Override
 	public boolean fight(){
 		if(init==false){ 
 		// introduction
 			System.out.println("###################################");
 			System.out.println("#       RELATIONSHIP  ARENA       #");
 			System.out.println("###################################");
 			
 			System.out.println("It was a true story long time ago,");
			System.out.println("A bad boy always wanna make girlfriend;");
 			System.out.println("while a cute girl do not wanna to have a bad boyfriend.");
 			System.out.println("It's a fight between them..");
 			System.out.println();
 			init=true;
 		}
 		if(!end){
 			System.out.println("\n(Enter \"enter\" key to continue..)");
 		}else{
 			System.out.println("\nFinally...");
 			if(round==1)
 				System.out.println("The BadBoy is in a relationship with the CuteGirl!");
 			else 
 				System.out.println("The CuteGirl is free herself from the BadBoy!");
 		}
 		
 		
 		POOAction action = new POOAction();
 		
 		//enter any key to continue
 		try{
 	          System.in.read();
 	    }catch(Exception e){}
 	    //enter any key to continue
 	  	try{
 	  	       System.in.read();
 	  	}catch(Exception e){}
 	  	
 	  	//clear the screen
 	  	final String clear = "\033[2J";
 	  	System.out.print(clear);
 	  	
 	  	
 		//action
 		action = allpets.get(round).act(this);
 		// executive action
 		action.skill.act(action.dest);
 		
 		round+=1;
 		round%=allpets.size();
 		
 		// end the game in the next roop
 		if(action.dest.getHP()<=0){
 			end = true;
 		}
 	    
 		return true;
 		
 	}
 	
 	@Override
     public void show(){
 		int x,y;
 		
 		// create basic map
 		char[][] map = new char[size][size];
 		for(int i=0;i<size;i++)
     		for(int j=0;j<size;j++)
     			map[i][j] = '.';
     	
     	// locate pets' positions
     	x = allposi.get(0).getX();
     	y = allposi.get(0).getY();
     	if(x<0)
     		x=0;
     	else if(x>size-1)
     		x=size-1;
     	if(y<0)
     		y=0;
     	else if(y>size-1)
     		y=size-1;
     	map[y][x]= 'M'; 
     	
     	x = allposi.get(1).getX();
     	y = allposi.get(1).getY();
     	if(x<0)
     		x=0;
     	else if(x>size-1)
     		x=size-1;
     	if(y<0)
     		y=0;
     	else if(y>size-1)
     		y=size-1;
     	map[y][x]= 'W'; 
     	
     	printMap(map);
     	
     	for(int i=0;i<allpets.size();i++)
     		showStatus(allpets.get(i));
     }
 	private void showStatus(POOPet pet){
 		
 		System.out.print("["+pet.getName()+"] ");
 		System.out.print("HP:"+pet.getHP()+" ");
 		System.out.print("MP:"+pet.getMP()+" ");
 		System.out.println("AGI:"+pet.getAGI()+" ");
 	}
 	
 	private void printMap(char[][] map){
     	for(int i=0;i<size;i++){
     		for(int j=0;j<size;j++)
     			System.out.print(map[i][j]);
     		System.out.print("\n");
     	}
 	}
 	
 	
 	
 	
     @Override
     public POOCoordinate getPosition(POOPet p){
     	return null;
     }
     
 
     
     public int getPetNum(){
     	return allpets.size();
     }
     
     public Coordinate[] getAllPosi(){
         Coordinate[] parr = new Coordinate[0];
         return allposi.toArray(parr);
     }
     public final POOPet[] getAllPets2(){
         POOPet[] parr = new POOPet[0];
         return allpets.toArray(parr);
     }
     private Coordinate makeRandomCoordinate(Coordinate c){
     	Random ran = new Random();
         c.setX(ran.nextInt(size));
         c.setY(ran.nextInt(size));
     	return c;
     }
 }
 
 class Coordinate extends POOCoordinate{
 	
 	public  boolean equals(POOCoordinate other){
 		
 		if(this.getX()==((Coordinate)other).getX() && this.getY()==((Coordinate)other).getY())
 			return true;
 		return false;
 	}
 
 	
 	public void setX(int x){
 		this.x = x;
 	}
 	
 	public void setY(int y){
 		this.y = y;
 	}
 	
 	public int getX(){
 		return x;
 	}
 	
 	public int getY(){
 		return y;
 	}
 }
