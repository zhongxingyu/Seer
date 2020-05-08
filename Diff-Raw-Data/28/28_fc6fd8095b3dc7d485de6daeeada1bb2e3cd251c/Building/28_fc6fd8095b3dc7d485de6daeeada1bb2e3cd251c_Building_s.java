 /**
 Copyright (c) 2012 Babin Philippe
 All rights reserved.
 
 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 
     Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
     Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 
 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.*/
 
 package zis.map;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.newdawn.slick.geom.Rectangle;
 
 import zis.CONST;
 import zis.util.Rand;
 import zis.util.Vector2i;
 
 /***
 * Randomly generate a office building
  * 
  * @author Philippe Babin
  */
 public class Building {
 	
 	private int x, y, W, H;
 	
 	/*** Randomizer */
 	private Rand rand;
 	
 	/*** City that contain the building */
 	private City city;
 	 
 	/*** Stack of possible rooms */
 	private ArrayList<Rectangle> temRegion = new ArrayList<Rectangle>();
 
 	/*** Stack of generated rooms */
 	private ArrayList<Rectangle> buildRegion = new ArrayList<Rectangle>();
 
 	/*** Stack of generated hallway */
 	private ArrayList<Rectangle> hallway = new ArrayList<Rectangle>();
 
 	/*** List of the building's rooms */
 	private ArrayList<Room> rooms = new ArrayList<Room>();
 
 	/*** Name of the building */
 	private String name = "Test&RD Inc.";
 
 	/***
 	 * Constructor of a office building
 	 * @param city Reference to the City
 	 * @param seed Seed of the building
 	 * @param r Rectangle of the building
 	 */
 	public Building( City city, int seed, Rectangle r){
 		this.city = city;
 		this.x = (int) r.getX() + 1;
 		this.y = (int) r.getY() + 1;
 		this.W = (int) r.getWidth() - 2;
 		this.H = (int) r.getHeight() - 2;
 		rand = new Rand( seed);
 		
 		create();
 	}
 	/***
 	 * Constructor of a office building
 	 * @param city Reference to the City
 	 * @param seed Seed of the building
 	 * @param x Position X of the Building
 	 * @param y Position Y of the Building
 	 * @param W Width of the Building
 	 * @param H Height of the Building
 	 */
 	public Building( City city, int seed, int x, int y, int W, int H){
 		this.city = city;
 		this.x = x + 1;
 		this.y = y + 1;
 		this.W = W - 2;
 		this.H = H - 2;
 		rand = new Rand( seed);
 		
 		create();
 	}
 	
 	/***
 	 * Procedurally generate a building in the target region
 	 */
 	public void create(){
 		addFloor();
 	}
 	
 	/***
 	 * Procedurally generate a floor
 	 */
 	public void addFloor(){
 		temRegion.clear();
 		buildRegion.clear();
 		hallway.clear();
 		rooms.clear();
 		
 		temRegion.add( new Rectangle( x, y, W, H));
 
 		int position = 25 * rand.nextInt(3) + 25;
 		
 		if( rand.nextBoolean()){
 			
 			if( W >= 50 && H >= 30){
 				addHallway( 0, position, true);
 				if( position >= 50){
 					addLoopHallway( 0);
 					if( temRegion.get( temRegion.size() - 1).getWidth() > 30)
 						addHallway( temRegion.size() - 1, 33, false);
 					addHallway( temRegion.size() - 1, 50, false);
 					
 				}
 				if( position == 50 && H >= 40){
 					addLoopHallway( 0);
 					if( temRegion.get( temRegion.size() - 2).getWidth() > 30){
 						addHallway( temRegion.size() - 2, 33, false);
 						addHallway( temRegion.size() - 1, 50, false);
 					}
 					else
 						addHallway( temRegion.size() - 1, 33, false);
 				}
 				else if( position == 25){
 					addLoopHallway( 1);
 					if( temRegion.get( temRegion.size() - 2).getWidth() > 30){
 						addHallway( temRegion.size() - 2, 33, false);
 						addHallway( temRegion.size() - 1, 50, false);
 					}
 					else
 						addHallway( temRegion.size() - 2, 50, false);
 				}
 			}
 			else if(W >= 25 && H >=25){
 				addHallway( 0, 25, true);
 				addHallway( 1, 50, true);
 				if( H >=50){
 					addHallway( 1, 63, false);
 					addHallway( 2, 50, false);
 				}
 				else{
 					addHallway( 1, 48, false);
 				}
 			}
 			else{
 				addHallway( 0, 50, H < W);
 			}
 		}
 		else{
 			if( H >= 55 && W >= 30){
 				addHallway( 0, position, false);
 				if( position >= 50){
 					addLoopHallway( 0);
 					addHallway( temRegion.size() - 4, 50, true);
 				}
 				if( position == 50 && W >= 40 && H >= 70){
 					addLoopHallway( 0);
 					addHallway( temRegion.size() - 5, 50, true);
 				}
 				else if( position == 25){
 					addLoopHallway( 1);
 					addHallway( temRegion.size() - 5, 50, true);
 				}
 			}
 			else if(H >= 25  && W >=25){
 				addHallway( 0, 30, false);
 				addHallway( 1, 50, false);
 				if( W >=50){
 					addHallway( 1, 63, true);
 					addHallway( 2, 50, true);
 				}
 				else{
 					addHallway( 1, 48, true);
 				}
 			}
 			else{
 				addHallway( 0, 50, H < W);
 			}
 			
 		}
 		
 		int nbrRegion = temRegion.size();
 		for(int l = 0; l < nbrRegion; l++){
 			sliceInHalf( l, 1);
 		}
 
 		/** Add the outline of then room */
 		int j = 0;
 		for(Rectangle r : buildRegion){
 			city.drawOutline( r, 3);
 			rooms.add( new Room( r));
 			
 			j++;
 		}
 		
 		addInterRoomDoor();
 
 		addHallwayDoor();
 	}
 	
 	/***
	 * Slice in two temporary region a temporary rectangle
 	 * @param id Id of the temporary rectangle 
	 * @param interator Interator of the recursion
 	 */
	public void sliceInHalf(int id, int interator){
 		Rectangle r = temRegion.get( id);
 		int line;
 		if(  r.getHeight() > r.getWidth()){
 			if( r.getHeight() > 10 && 
					( rand.nextInt( interator) <= 2 ||
 					r.getHeight() * r.getWidth() >= 196)){
 				
 				if( CONST.SYMMETRICROOM)
 					line = (int)r.getHeight()/2;
 				else
 					line = (int)r.getHeight()/2 - rand.nextInt( -1, 2);
 				
 				int newId = temRegion.size();
 				
 				temRegion.add(
 						new Rectangle( r.getX(),
 						r.getY(),
 						r.getWidth(),
 						line));
 				temRegion.add(
 						new Rectangle( r.getX(),
 						r.getY() + line - 1,
 						r.getWidth(),
 						r.getHeight() - line + 1));
 				
				sliceInHalf( newId, interator + 1);
				sliceInHalf( newId + 1, interator + 1);
 			}
 			else{
 				buildRegion.add( r);
 			}
 		}
 		else{
 			if( r.getWidth() > 10 &&
					(rand.nextInt( interator) <= 2 ||
 					r.getHeight() * r.getWidth() >= 196)){
 
 				if( CONST.SYMMETRICROOM)
 					line = (int)r.getWidth()/2;
 				else
 					line = (int)r.getWidth()/2 - rand.nextInt( -1, 2);
 				
 				int newId = temRegion.size();
 				
 				temRegion.add(
 						new Rectangle( r.getX(),
 						r.getY(),
 						line,
 						r.getHeight()));
 				temRegion.add(
 						new Rectangle( r.getX() + line - 1,
 						r.getY(),
 						r.getWidth() - line + 1,
 						r.getHeight()));
 						
				sliceInHalf( newId, interator + 1);
				sliceInHalf( newId + 1, interator + 1);
 			}
 			else{
 				buildRegion.add( r);
 			}
 		}
 	}	
 
 	/***
 	 * Add a Loop hallway to a temporary region
 	 * @param id Id of the temporary region where the Loop is build
 	 */
 	public void addLoopHallway(int id){
 		Rectangle r = temRegion.get( id);
 		int c = (int) (r.getWidth() * 0.2 + rand.nextInt((int) Math.abs(r.getWidth() * 0.05))) ;
 		int p = (int) (r.getWidth() * 0.5 - CONST.HALLWAY_WIDTH - c);
 
 //		int cy = (int) (c * 1.618);
 		int cy = (int) (r.getHeight() * 0.2);
 		int py = (int) (r.getHeight() * 0.5 - cy - CONST.HALLWAY_WIDTH);
 		
 		hallway.add(
 				new Rectangle( r.getX() + p,
 						r.getY() + py,
 						2 * (CONST.HALLWAY_WIDTH + c),
 						CONST.HALLWAY_WIDTH));
 		hallway.add(
 				new Rectangle( r.getX() + p,
 						r.getY() + py + 2 * cy + CONST.HALLWAY_WIDTH,
 						2 * (CONST.HALLWAY_WIDTH + c),
 						CONST.HALLWAY_WIDTH));
 		hallway.add(
 				new Rectangle( r.getX() + p,
 						r.getY() + py,
 						CONST.HALLWAY_WIDTH,
 						2 * (CONST.HALLWAY_WIDTH + cy)));
 		hallway.add(
 				new Rectangle( r.getX() + p + 2 * c + CONST.HALLWAY_WIDTH,
 						r.getY() + py,
 						CONST.HALLWAY_WIDTH,
 						2 * (CONST.HALLWAY_WIDTH + cy)));
 
 		/*** North and South side*/
 		temRegion.add(
 				new Rectangle( r.getX(),
 				r.getY(),
 				r.getWidth(),
 				py + 1));
 		temRegion.add(
 				new Rectangle( r.getX(),
 				r.getY() + py + 2 * CONST.HALLWAY_WIDTH + 2 * cy - 1,
 				r.getWidth(),
 				py + 2));
 
 		/*** Center region*/
 		temRegion.add(
 				new Rectangle( r.getX() + p + CONST.HALLWAY_WIDTH - 1,
 				r.getY() + py + CONST.HALLWAY_WIDTH - 1,
 				2 * c + 2,
 				2 * cy + 2));
 		
 		/*** West and East side*/
 		temRegion.add(
 				new Rectangle( r.getX(),
 				r.getY() + py,
 				p + 1,
 				2 * (cy + CONST.HALLWAY_WIDTH)));
 		temRegion.add(
 				new Rectangle( r.getX() + p + 2 * (c + CONST.HALLWAY_WIDTH) - 1,
 				r.getY() + py,
 				r.getWidth() - p - 2 * (c + CONST.HALLWAY_WIDTH) + 1,
 				2 * (cy + CONST.HALLWAY_WIDTH)));
 
 		
 		temRegion.remove(id);
 	}
 	
 	/***
 	 * Add a hallway to a temporary region
 	 * @param id Id of the temporary region
 	 * @param position Percentage of the region where the hallway is added
 	 */
 	public void addHallway(int id, int position){
 		addHallway( id, position, temRegion.get( id).getHeight() < temRegion.get( id).getWidth());
 	}
 	
 	/***
 	 * Add a hallway to a temporary region
 	 * @param id Id of the temporary region
 	 * @param position Percentage of the region where the hallway is added
 	 * @param ver Direction of the hallway, true if vertical
 	 */
 	public void addHallway(int id, int position, boolean ver){
 		
 		Rectangle r = temRegion.get( id);
 		int line;
 		
 		if( ver){
 			line = (int)(r.getWidth() * position / 100);
 			
 			hallway.add(
 					new Rectangle( r.getX() + line - 1,
 							r.getY(),
 							CONST.HALLWAY_WIDTH,
 							r.getHeight()));
 			
 			temRegion.add(
 					new Rectangle( r.getX(),
 					r.getY(),
 					line,
 					r.getHeight()));
 			temRegion.add(
 					new Rectangle( r.getX() + line + CONST.HALLWAY_WIDTH - 2,
 					r.getY(),
 					r.getWidth() - line - CONST.HALLWAY_WIDTH + 2,
 					r.getHeight()));
 		}
 		else{
 			line = (int)(r.getHeight() * position / 100);
 			
 			hallway.add(
 					new Rectangle( r.getX(),
 							r.getY() + line - 1,
 							r.getWidth(),
 							CONST.HALLWAY_WIDTH));
 			
 			temRegion.add(
 					new Rectangle( r.getX(),
 					r.getY(),
 					r.getWidth(),
 					line));
 			temRegion.add(
 					new Rectangle( r.getX(),
 					r.getY() + line + CONST.HALLWAY_WIDTH - 2,
 					r.getWidth(),
 					r.getHeight() - line - CONST.HALLWAY_WIDTH + 2));
 		}
 		
 		temRegion.remove(id);
 		
 	}
 	
 	/***
 	 * Add door between every room of a floor
 	 */
 	public void addInterRoomDoor(){
 		Vector2i portal = new Vector2i( 0, 0);
 		Vector2i newDoor = new Vector2i( 0, 0);
 		
 		for(Room room : rooms){
 			Rectangle r = room.getRect();
 				
 			for( Room childRoom : rooms){
 				/** Horizontal doors*/
 				if( childRoom.getRect() != r && childRoom.getRect().intersects( r) &&  r.getWidth() > 4){
 					Rectangle rc = childRoom.getRect();
 					if(rc.getWidth() > r.getWidth()){
 						portal.x = (int)(r.getX() + r.getWidth()/2);
 						portal.y = (int)(r.getY() + r.getHeight()) ;
 						
 						if( rc.contains( portal.x, portal.y)){
 							newDoor.x = (int)(r.getX() + rand.nextInt( (int) r.getWidth() - 2) + 1);
 							newDoor.y = (int)(r.getY() + r.getHeight()) - 1;
 							childRoom.setDoor( CONST.NORTH);
 							
 							city.addDoor( newDoor, true);
 						}
 					}
 					else{
 						portal.x = (int)(rc.getX() + rc.getWidth()/2);
 						portal.y = (int)(rc.getY());
 						
 						if( r.contains( portal.x, portal.y)){
 							newDoor.x = (int)(rc.getX() + rand.nextInt( (int) rc.getWidth() - 2) + 1);
 							newDoor.y = (int)rc.getY();
 							childRoom.setDoor( CONST.NORTH);
 							
 							city.addDoor( newDoor, true);
 						}
 						
 					}
 				}
 				/** Vertical doors*/
 				if( childRoom.getRect() != r && childRoom.getRect().intersects( r) &&  r.getHeight() > 4){
 					Rectangle rc = childRoom.getRect();
 					if(rc.getHeight() > r.getHeight()){
 						portal.x = (int)(r.getX() + r.getWidth());
 						portal.y = (int)(r.getY() + r.getHeight()/2) ;
 						
 						if( rc.contains( portal.x, portal.y)){
 							newDoor.x = (int)(r.getX() + r.getWidth()) - 1;
 							newDoor.y = (int)(r.getY() + rand.nextInt( (int) r.getHeight() - 2)) + 1;
 							childRoom.setDoor( CONST.WEST);
 							room.setDoor( CONST.EAST);
 							
 							city.addDoor( newDoor, false);
 						}
 					}
 					else{
 						portal.x = (int)(rc.getX());
 						portal.y = (int)(rc.getY() + rc.getHeight()/2);
 						
 						if( r.contains( portal.x, portal.y)){
 							newDoor.x = (int)rc.getX();
 							newDoor.y = (int)rc.getY() + rand.nextInt( (int) rc.getHeight() - 2) + 1;
 							childRoom.setDoor( CONST.WEST);
 							room.setDoor( CONST.EAST);
 							
 							city.addDoor( newDoor, false);
 						}
 						
 					}
 				}
 			}
 		}
 		
 	}
 	
 	/***
 	 * Add a door between every room and hallway
 	 */
 	public void addHallwayDoor(){
 		Vector2i portal = new Vector2i( 0, 0);
 		Vector2i newDoor = new Vector2i( 0, 0);
 	
 		for(Rectangle r: hallway){
 				
 			for( Room publicRoom : rooms){
 				Rectangle rc = publicRoom.getRect();
 				/** Horizontal hallway doors*/
 				if( rc.intersects( r) &&  rc.getWidth() > 4){
 					/** South door */
 					portal.x = (int)(rc.getX() + rc.getWidth()  /2);
 					portal.y = (int)(rc.getY() + rc.getHeight()) ;
 					if( !publicRoom.getDoor( CONST.SOUTH) && r.contains( portal.x, portal.y)){
 						
 						publicRoom.setDoor( CONST.SOUTH);
 						
 						newDoor.x = (int)(rc.getX() + rand.nextInt( (int) rc.getWidth() - 2) + 1);
 						newDoor.y = (int)(rc.getY() + rc.getHeight()) - 1;
 						
 						city.addDoor( newDoor, true);
 					}
 					/** North door */
 					portal.x = (int)(rc.getX() + rc.getWidth() / 2);
 					portal.y = (int)(rc.getY()) ;
 					if( !publicRoom.getDoor( CONST.NORTH) && r.contains( portal.x, portal.y)){
 						
 						publicRoom.setDoor( CONST.NORTH);
 						
 						newDoor.x = (int)(rc.getX() + rand.nextInt( (int) rc.getWidth() - 2) + 1);
 						newDoor.y = (int)rc.getY();
 						
 						city.addDoor( newDoor, true);
 					}
 				}
 				/** Vertical hallway doors*/
 				if( rc.intersects( r) &&  rc.getHeight() > 4){
 					/** West door */
 					portal.x = (int)rc.getX();
 					portal.y = (int)(rc.getY() + rc.getHeight() / 2) ;
 					if( !publicRoom.getDoor( CONST.WEST) && r.contains( portal.x, portal.y)){
 						
 						publicRoom.setDoor( CONST.WEST);
 						
 						newDoor.x = (int)rc.getX();
 						newDoor.y = (int)rc.getY() + rand.nextInt( (int) rc.getHeight() - 2) + 1;
 						
 						city.addDoor( newDoor, false);
 					}
 					/** East door */
 					portal.x = (int)(rc.getX() + rc.getWidth());
 					portal.y = (int)(rc.getY() + rc.getHeight() / 2) ;
 					if( !publicRoom.getDoor( CONST.EAST) && r.contains( portal.x, portal.y)){
 						
 						publicRoom.setDoor( CONST.EAST);
 						
 						newDoor.x = (int)(rc.getX() + rc.getWidth()) - 1;
 						newDoor.y = (int)rc.getY() + rand.nextInt( (int) rc.getHeight() - 2) + 1;
 						
 						city.addDoor( newDoor, false);
 					}
 				}
 			}
 		}
 		
 	}
 
 	/***
 	 * Return the Rooms of the building
 	 * @return building's Rooms
 	 */
 	public ArrayList< Room> getRooms() {
 		return rooms;
 	}
 
 	/***
 	 * Return the Hallways of the building
 	 * @return building's Rooms
 	 */
 	public ArrayList< Rectangle> getHallways() {
 		return hallway;
 	}
 
 	/***
 	 * Return the number of office Rooms of the building
 	 * @return the number of office Rooms of the building
 	 */
 	public int getNbrOffice(){
 		int nbrOffice = 0;
 		for( Room r : rooms){
 			if( r.getSurface() <= CONST.MAX_OFFICE_ROOM_DOMAIN)
 				nbrOffice++;
 		}
 			
 		return nbrOffice;
 	}
 }
