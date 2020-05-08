 package io.furyhunter.ld26editor;
 
 import java.awt.Graphics;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Scanner;
 
 public class Level {
 	private List<Entity> entities;
 	
 	public int offx;
 	public int offy;
 	
 	public Level() {
 		entities = new LinkedList<Entity>();
 	}
 	
 	public Level(File f) throws FileNotFoundException {
 		entities = new LinkedList<Entity>();
 		Scanner s = new Scanner(new FileInputStream(f));
 		
 		while (s.hasNext()) {
 			String type = s.next();
 			if (type.equals("Tile")) {
 				Tile t = new Tile();
 				t.x = s.nextFloat();
 				t.y = s.nextFloat();
 				entities.add(t);
 			}
 			if (type.equals("Player")) {
 				Player p = new Player();
 				p.x = s.nextFloat();
 				p.y = s.nextFloat();
 				entities.add(p);
 			}
 			if (type.equals("Spring")) {
 				float x, y;
 				x = s.nextFloat();
 				y = s.nextFloat();
 				
 				switch (s.nextInt()) {
 				case 0:
 					SpringU u = new SpringU();
 					u.x = x;
 					u.y = y;
 					entities.add(u);
 					break;
 				case 1:
 					SpringD d = new SpringD();
 					d.x = x;
 					d.y = y;
 					entities.add(d);
 					break;
 				case 2:
 					SpringL l = new SpringL();
 					l.x = x;
 					l.y = y;
 					entities.add(l);
 					break;
 				case 3:
 					SpringR r = new SpringR();
 					r.x = x;
 					r.y = y;
 					entities.add(r);
 					break;
 				}
 				
 			}
 			
 			if (type.equals("SwitchedDoor")) {
 				SwitchedTile sd = new SwitchedTile();
 				sd.x = s.nextFloat();
 				sd.y = s.nextFloat();
 				
 				entities.add(sd);
 			}
 			
 			if (type.equals("SmallTile")) {
 				SmallTile a = new SmallTile();
 				a.x = s.nextFloat();
 				a.y = s.nextFloat();
 				
 				entities.add(a);
 			}
 			
 			if (type.equals("AboveTile")) {
 				AboveTile a = new AboveTile();
 				a.x = s.nextFloat();
 				a.y = s.nextFloat();
 				
 				entities.add(a);
 			}
			
			if (type.equals("NextLevel")) {
				NextLevel a = new NextLevel();
				a.x = s.nextFloat();
				a.y = s.nextFloat();
				a.metadata = s.next();
				
				entities.add(a);
			}
 		}
 		
 		System.out.printf("%d entities found\n", entities.size());
 	}
 	
 	public void save(File p) throws FileNotFoundException {
 		PrintWriter print = new PrintWriter(new FileOutputStream(p));
 		
 		for (Entity e : entities) {
 			e.save(print);
 		}
 		print.flush();
 		print.close();
 	}
 	
 	public void draw(Graphics g) {
 		for (Entity e : entities) {
 			e.draw(g, offx, offy);
 		}
 	}
 	
 	public Class remove(int x, int y) {
 		Entity toRemove = null;
 		for (Entity e : entities) {
 			if (e.touch(x, y)) {
 				toRemove = e;
 				break;
 			}
 		}
 		
 		entities.remove(toRemove);
 		
 		return toRemove == null ? null : toRemove.getClass();
 	}
 	
 	public void add(EntityTypes type, int x, int y, String metadata) {
 		Entity en = null;
 		try {
 			en = (Entity) type.type.newInstance();
 		} catch (InstantiationException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		}
 		
 		en.x = x;
 		en.y = y;
 		en.metadata = metadata;
 		
 		entities.add(en);
 	}
 }
