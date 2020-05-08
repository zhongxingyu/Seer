 package gameLogic;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.*;
 
 public class Game {
 
 	private long round;
 	volatile private boolean paused;
 	volatile private int speed;
 	protected List<Spray> sprays; //!! minek ennek lista kt fle van ? ezzel csak a szvs lesz, kzben megoldottam de akkor is bazd meg
 	protected Map map;
 	protected MapCreator mapCreator;
 	protected List<Item> items;
 	
 	public Game(){
 		round = 0;
 		paused = false;
 		speed = 0;
 		mapCreator = new MapCreator();
 		map = null;
 		sprays = new ArrayList<Spray>(2);
 		items = null;
 	}
 
 	public void startGame() {
 		
 		this.map = this.mapCreator.createMap(100, 100, 0);
 		sprays.add(new KillerSpray(10, 5)); // TODO ezeket a konstansokat globalba !
 		sprays.add(new OdorNeutralizerSpray(10, 5)); // TODO ezeket a konstansokat globalba !
 		
 		while (!this.isGameEnded()) {
 			
 			this.actItems();
 			map.decreaseAntOdor();
 			
 			this.round++;
 			
 		}
 		
 	}
 	
 	private boolean isGameEnded() { // TODO j fvny!
 		
 		// TODO kitallni
 		
 		return false;
 	}
 	
 	private void actItems(){
 		for (Item i : items)
 			i.act();
 	}
 	
	private void waitTillNextRound(){
 		
 		// pause
 		while (this.paused);
 		
 		// roundtime
 		try {
 			Thread.sleep(1000 / speed);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void protoTest() {
 		//A feladatkirs szerint a prototpus clja, hogy bemutassa,
 		//a program elkszlt, helyesen mkdik s minden feladatnak elvgzsre kpes.
 		//Ez a vltozat egy elkszlt program csak a grafikus interfsz hinyzik belle,
 		//tervezsi szempontbl ksznek tekinthet, valamennyi metdusa mr a vgleges algoritmusokat hajtja vgre. 
 		//A prototpus vezrlse konzolon keresztl fog trtnni, a tesztelshez elre meghatrozott teszteseteket ksztnk.	
 		//A prototpus a szabvnyos be- s kimeneten keresztl kommunikl a felhasznlval.
 		
 		BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
 		List<Item> actitems = new ArrayList<Item>();
 		File file = null;
 		BufferedWriter out = null;
 		String nl = System.getProperty("line.separator");
 		boolean exit = false; 
 		try {
 			while (!exit) {
 				String[] comm = bf.readLine().split(" ");
 				if (comm[0].equals("newTest")) {
 					//newTest <tesztmegnevezse>
 					//Lers: j tesztet indt. Kimenetet kirja <tesztmegnevezse>.txt-be.
 					//Opcik: teszt megnevezse
 					
 					map = null;
 					sprays = new ArrayList<Spray>(2);
 					items = null;
 					actitems = new ArrayList<Item>();
 					
 					//fjlba rs nem vizsglja hogy ltezik-e mr ilyen nev
 					if(out==null){
 						file = new File(System.getProperty("user.dir")+"\\prototest\\"+comm[1]+".txt");
 						file.createNewFile();
 						out = new BufferedWriter(new FileWriter(file));
 					}
 					else{
 						out.close();
 						file = new File(System.getProperty("user.dir")+"\\prototest\\"+comm[1]+".txt");
 						file.createNewFile();
 						out = new BufferedWriter(new FileWriter(file));
 					}
 				}
 				else if (comm[0].equals("createMap")) {
 					//createMap <width> <height>
 					//Lers: Csinl egy <width> x<height> mret plyt
 					//Opcik: oszlopok szma, sorok szma
 
 					map = mapCreator.createMap(Integer.parseInt(comm[1]), Integer.parseInt(comm[2]), 0);
 				}
 				else if (comm[0].equals("createKillerSpray")) {
 					//createKillerSpray <charge> <radian>
 					//Lers: Hangyal spray ltrehozsa,
 					//meglehet adni a tltttsget s a hattvolsgot
 					//Opcik: tltttsg, hattvolsg
 
 					sprays.add(0, new KillerSpray(Integer.parseInt(comm[1]), Integer.parseInt(comm[2])));
 				}
 				else if (comm[0].equals("createOdorSpray")) {
 					//createOdorSpray <charge> <radian>
 					//Lers: Hangyaszag semlegest spray ltrehozsa,
 					//meglehet adni a tltttsget s a hattvolsgot
 					//Opcik: tltttsg, hattvolsg
 
 					sprays.add(1,new OdorNeutralizerSpray(Integer.parseInt(comm[1]), Integer.parseInt(comm[2])));
 				}
 				else if (comm[0].equals("addAnt")) {
 					//addAnt <row> <column> <carry>
 					//Lers: Csinl egy hangyt az adott mezre,
 					//megadhat hogy cipel-e telt
 					//Opcik: sorszm, oszlopszm, 0-1
 					//hangya carriedItem carry adattagja public lett a protoban
 					
 					Ant ant = new Ant(map.getField(Integer.parseInt(comm[1]), Integer.parseInt(comm[2]) ));
 					if(comm[3]=="1"){
 						Field field = new Field();
 						Food food = new Food(field);
 						field.deregister();
 						ant.carriedItem = food;
 						ant.carry = true;
 					};
 					items.add(ant);
 					actitems.add(ant);
 				}
 				else if (comm[0].equals("addAntEater")) {
 					//addAntEater <row> <column> <hunger>
 					//Lers: Csinl egy hangyszsnt az adott mezre,
 					//megadhat hogy mennyire hes
 					//Opcik: sorszm, oszlopszm, 0-1-2-3
 					//hangysz hunger adattagja public lett a protoban
 					
 					AntEater anteater = new AntEater(map.getField(Integer.parseInt(comm[1]), Integer.parseInt(comm[2]) ));
 					anteater.hunger= Integer.parseInt(comm[3]);
 					items.add(anteater);
 					actitems.add(anteater);
 				}
 				else if (comm[0].equals("addFood")) {
 					//addFood <row> <column>
 					//Lers: Csinl egy telt az adott mezre
 					//Opcik: sorszm, oszlopszm
 					
 					Food food = new Food(map.getField(Integer.parseInt(comm[1]), Integer.parseInt(comm[2]) ));
 					items.add(food);
 					actitems.add(food);
 				}
 				else if (comm[0].equals("addStone")) {
 					//addStone <row> <column>
 					//Lers: Csinl egy kvet az adott mezre
 					//Opcik: sorszm, oszlopszm
 					
 					Stone stone = new Stone(map.getField(Integer.parseInt(comm[1]), Integer.parseInt(comm[2]) ));
 					items.add(stone);
 					actitems.add(stone);
 				}
 				else if (comm[0].equals("addBarrier")) {
 					//addBarrier <row> <column>
 					//Lers: Csinl egy akadlyt az adott mezre
 					//Opcik: sorszm, oszlopszm
 					
 					Barrier barrier = new Barrier(map.getField(Integer.parseInt(comm[1]), Integer.parseInt(comm[2]) ));
 					items.add(barrier);
 					actitems.add(barrier);
 				}
 				else if (comm[0].equals("addAntLion")) {
 					//addAntLion <row> <column>  
 					//Lers: Csinl egy hangyalest az adott mezre
 					//Opcik: sorszm, oszlopszm
 					
 					AntLion antlion = new AntLion(map.getField(Integer.parseInt(comm[1]), Integer.parseInt(comm[2]) ));
 					items.add(antlion);
 					actitems.add(antlion);
 				}
 				else if (comm[0].equals("addAntHill")) {
 					//addAntHill <row> <column> <passive>
 					//Lers: Csinl egy hangyabolyt az adott mezre,
 					//megadhat hogy ne dobjon ki j hangyt, teht ne lpjen semelyik krben
 					//Opcik: sorszm, oszlopszm, 0-1
 					
 					AntHill anthill = new AntHill(map.getField(Integer.parseInt(comm[1]), Integer.parseInt(comm[2]) ));
 					items.add(anthill);
 					if(comm[3]=="0"){
 						actitems.add(anthill);
 					}
 				}
 				else if (comm[0].equals("addAntEaterColony")) {
 					//addAntEaterColony <row> <column> <passive>
 					//Lers: Csinl egy hangyszsn kolnit az adott mezre,
 					//megadhat hogy ne dobjon ki j hangyszt, teht ne lpjen semelyik krben
 					//Opcik: sorszm, oszlopszm, 0-1
 					
 					AntEaterColony colony = new AntEaterColony(map.getField(Integer.parseInt(comm[1]), Integer.parseInt(comm[2]) ));
 					items.add(colony);
 					if(comm[3]=="0"){
 						actitems.add(colony);
 					}
 				}
 				else if (comm[0].equals("setAntOdor")) {
 					//setAntOdor <row> <column> <antodor>
 					//Lers: Belltja az adott mezn a hangyaszagot*
 					//Opcik: sorszm, oszlopszm, 0-1-2-3-4
 					
 					//*antodor public legyen a protoban
 
 					map.getField(Integer.parseInt(comm[1]), Integer.parseInt(comm[2])).getOdor().ant = Integer.parseInt(comm[3]);
 				}
 				else if (comm[0].equals("actAllItem")) {
 					//actAllItem 
 					//Lers: Lpteti az sszes elemet a map-on, valamilyen sorrend szerint
 					//Opcik: -
 					
 					for (Iterator<Item> iterator = actitems.iterator(); iterator
 							.hasNext();) {
 						Item item = (Item) iterator.next();
 						item.act();
 					}
 				}
 				else if (comm[0].equals("actItem")) {
 					//actItem <row> <column>
 					//Lers: Lpteti az adott elemet a map-on
 					//Opcik: sorszm, oszlopszm
 
 					map.getField(Integer.parseInt(comm[1]), Integer.parseInt(comm[2])).item.act(); 
 					//!!ht ezt nem tudom mire vlni field-ben mirt protected az item,
 					//!!ha mgis private akkor itt castolni kell, nem szp de itt elmegy
 				}
 				else if (comm[0].equals("showMap")) {
 					//showMap <map>
 					//Lers: Kirja a map tartalmt, lehet vlasztani az Item-ek 
 					//s ngy fajta Odor kztt
 					//Opcik: item-antodor-colonyodor-foododor-hillodor
 
 					//!!ezt nem rom meg mert ehhez kell a createMap
 					//!! itt rdemes lenne leellenrizni hogy a megfelel mezk egyms szomszdai-e ha nem akkor valami zenet
 					//!! egybknt meg a map az nem nagy cucc
 					;
 				}
 				else if (comm[0].equals("useKillerSpray")) {
 					//useKillerSpray <row> <column>
 					//Lers: Hangyal spray hasznlata az adott mez kzppontjbl
 					//Opcik: sorszm, oszlopszm
 					
 					if(sprays.get(0).getCharge()==0){
 						System.out.println("KillerSpray is empty");
 						if(out!=null){
 							out.write("KillerSpray is empty"+nl);
 						}
 					}
 					else{
 						sprays.get(0).mechanism(map.getField(Integer.parseInt(comm[1]), Integer.parseInt(comm[2])));
 						System.out.println("KillerSpray charge "+sprays.get(0).getCharge());
 						if(out!=null){
 							out.write("KillerSpray charge "+sprays.get(0).getCharge()+nl);
 						}
 					}
 				}
 				else if (comm[0].equals("useOdorSpray")) {
 					//useOdorSpray <row> <column>
 					//Lers: Hangyaszag semlegest spray hasznlata az adott mez kzppontjbl
 					//Opcik: sorszm, oszlopsz
 
 					if(sprays.get(1).getCharge()==0){
 						System.out.println("OdorSpray is empty");
 						if(out!=null){
 							out.write("OdorSpray is empty"+nl);
 						}
 					}
 					else{
 						sprays.get(1).mechanism(map.getField(Integer.parseInt(comm[1]), Integer.parseInt(comm[2])));
 						System.out.println("odorSpray charge "+sprays.get(1).getCharge());
 						if(out!=null){
 							out.write("OdorSpray charge "+sprays.get(1).getCharge()+nl);
 						}
 					}
 				}
 				else if (comm[0].equals("exit")) {
 					//exit
 					//Lers: kilps a programbl
 					//Opcik: -
 					
 					if(out!=null){
 						out.close();
 					}
 					exit = true;
 				}
 				else {
 					System.out.println("Hibs parancs");
 				}
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void skeletonTest() {
 		int eset;
 		
 		System.out.println("Skeleton test - stack_overlord");
 		
 		while (true) {
 			eset = 0;
 			System.out.print("\nTesztesetek 1-tl 16-ig: ");
 			
 			try {
 				BufferedReader bf = new BufferedReader(new InputStreamReader(System.in));
 				eset = Integer.parseInt(bf.readLine());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			switch (eset) {
 				case 1:
 					hangyaHangya();
 					break;
 				case 2:
 					hangyaHangyaszSun();
 					break;
 				case 3:
 					hangyaHangyaszKolonia();
 					break;
 				case 4:
 					hangyaHangyaBoy();
 					break;
 				case 5:
 					hangyaHangyaleso();
 					break;
 				case 6:
 					hangyaAkadaly();
 					break;
 				case 7:
 					hangyaEtel();
 					break;
 				case 8:
 					hangyaUresMezo();
 					break;
 				case 9:
 					hsunHangya();
 					break;
 				case 10:
 					hsunHsun();
 					break;
 				case 11:
 					hsunKolonia();
 					break;
 				case 12:
 					hsunHangyaboy();
 					break;
 				case 13:
 					hsunHangyaleso();
 					break;
 				case 14:
 					hsunAkadaj();
 					break;
 				case 15:
 					hsunEtel();
 					break;
 				case 16:
 					hsunUres();
 					break;
 				default:
 					System.out.println("Invalid teszteset!");
 			}
 		}
 	}
 	
 	private void hangyaHangya() {
 		Field f1 = new Field();
 		Field f2 = new Field();
 		Ant ant = new Ant(f1);
 		Ant ant2 = new Ant(f2);
 		
 		f1.register(ant);
 		f2.register(ant2);
 		
 		f1.addNeighbour(f2,3);
 		f2.addNeighbour(f1,0);
 		
 		ant.act();
 	}
 	
 	private void hangyaHangyaszSun() {
 		Field f1 = new Field();
 		Field f2 = new Field();
 		Ant ant = new Ant(f1);
 		AntEater ae = new AntEater(f2);
 		
 		f1.register(ant);
 		f2.register(ae);
 		
 		f1.addNeighbour(f2,3);
 		f2.addNeighbour(f1,0);
 		
 		ant.act();
 	}
 	
 	private void hangyaHangyaszKolonia() {
 		Field f1 = new Field();
 		Field f2 = new Field();
 		Ant ant = new Ant(f1);
 		AntEaterColony ac = new AntEaterColony(f2);
 		
 		f1.register(ant);
 		f2.register(ac);
 		
 		f1.addNeighbour(f2,3);
 		f2.addNeighbour(f1,0);
 		
 		ant.act();
 	}
 	
 	private void hangyaHangyaBoy() {
 		Field f1 = new Field();
 		Field f2 = new Field();
 		Ant ant = new Ant(f1);
 		AntHill ah = new AntHill(f2);
 		
 		f1.register(ant);
 		f2.register(ah);
 		
 		f1.addNeighbour(f2,3);
 		f2.addNeighbour(f1,0);
 		
 		ant.act();
 	}
 	
 	private void hangyaHangyaleso() {
 		Field f1 = new Field();
 		Field f2 = new Field();
 		Ant ant = new Ant(f1);
 		AntLion al = new AntLion(f2);
 		
 		f1.register(ant);
 		f2.register(al);
 		
 		f1.addNeighbour(f2,3);
 		f2.addNeighbour(f1,0);
 		
 		ant.act();
 	}
 	
 	private void hangyaAkadaly() {
 		Field f1 = new Field();
 		Field f2 = new Field();
 		Ant ant = new Ant(f1);
 		Barrier b = new Barrier(f2);
 		
 		f1.register(ant);
 		f2.register(b);
 		
 		f1.addNeighbour(f2,3);
 		f2.addNeighbour(f1,0);
 		
 		ant.act();
 	}
 	
 	private void hangyaEtel() {
 		Field f1 = new Field();
 		Field f2 = new Field();
 		Ant ant = new Ant(f1);
 		Food food = new Food(f2);
 		
 		f1.register(ant);
 		f2.register(food);
 		
 		f1.addNeighbour(f2,3);
 		f2.addNeighbour(f1,0);
 		
 		ant.act();
 	}
 	
 	private void hangyaUresMezo() {
 		Field f1 = new Field();
 		Field f2 = new Field();
 		Ant ant = new Ant(f1);
 		
 		f1.register(ant);
 		
 		f1.addNeighbour(f2,3);
 		f2.addNeighbour(f1,0);
 		
 		ant.act();
 	}
 	
 	private void hsunHangya() {
 		Field f1 = new Field();
 		Field f2 = new Field();
 		AntEater antEater = new AntEater(f1);
 		Ant a = new Ant(f2);
 		
 		f1.register(antEater);
 		f2.register(a);
 		
 		f1.addNeighbour(f2,3);
 		f2.addNeighbour(f1,0);
 		
 		antEater.act();
 	}
 	
 	private void hsunHsun() {
 		Field f1 = new Field();
 		Field f2 = new Field();
 		AntEater antEater = new AntEater(f1);
 		AntEater ae = new AntEater(f2);
 		
 		f1.register(antEater);
 		f2.register(ae);
 		
 		f1.addNeighbour(f2,3);
 		f2.addNeighbour(f1,0);
 		
 		antEater.act();
 	}
 	
 	private void hsunKolonia() {
 		Field f1 = new Field();
 		Field f2 = new Field();
 		AntEater antEater = new AntEater(f1);
 		AntEaterColony ac = new AntEaterColony(f2);
 		
 		f1.register(antEater);
 		f2.register(ac);
 		
 		f1.addNeighbour(f2,3);
 		f2.addNeighbour(f1,0);
 		
 		antEater.act();
 	}
 	
 	private void hsunHangyaboy() {
 		Field f1 = new Field();
 		Field f2 = new Field();
 		AntEater antEater = new AntEater(f1);
 		AntHill ah = new AntHill(f2);
 		
 		f1.register(antEater);
 		f2.register(ah);
 		
 		f1.addNeighbour(f2,3);
 		f2.addNeighbour(f1,0);
 		
 		antEater.act();
 	}
 	
 	private void hsunHangyaleso() {
 		Field f1 = new Field();
 		Field f2 = new Field();
 		AntEater antEater = new AntEater(f1);
 		AntLion al = new AntLion(f2);
 		
 		f1.register(antEater);
 		f2.register(al);
 		
 		f1.addNeighbour(f2,3);
 		f2.addNeighbour(f1,0);
 		
 		antEater.act();
 	}
 	
 	private void hsunAkadaj() {
 		Field f1 = new Field();
 		Field f2 = new Field();
 		AntEater antEater = new AntEater(f1);
 		Barrier b = new Barrier(f2);
 		
 		f1.register(antEater);
 		f2.register(b);
 		
 		f1.addNeighbour(f2,3);
 		f2.addNeighbour(f1,0);
 		
 		antEater.act();
 	}
 	
 	private void hsunEtel() {
 		Field f1 = new Field();
 		Field f2 = new Field();
 		AntEater antEater = new AntEater(f1);
 		Food food = new Food(f2);
 		
 		f1.register(antEater);
 		f2.register(food);
 		
 		f1.addNeighbour(f2,3);
 		f2.addNeighbour(f1,0);
 		
 		antEater.act();
 	}
 	
 	private void hsunUres() {
 		Field f1 = new Field();
 		Field f2 = new Field();
 		AntEater antEater = new AntEater(f1);
 		
 		f1.register(antEater);
 		
 		f1.addNeighbour(f2,3);
 		f2.addNeighbour(f1,0);
 		
 		antEater.act();
 	}
 }
