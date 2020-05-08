 package v1;
 
 import java.util.ArrayList;
 
 public class Map implements grensesnitt.Kart{
 public ArrayList<ArrayList<Character>> charMap = new ArrayList<ArrayList<Character>>();
 public ArrayList<Player> players = new ArrayList<Player>();
 
 	public void updateMap(Coordinate c, Player p){
 		int xTemp = p.getPlayerPos().coord[1];
 		int yTemp = p.getPlayerPos().coord[0];
 		if ((this.charMap.get(yTemp + 1).get(xTemp) == 'v') &&
 				(this.charMap.get(yTemp - 1).get(xTemp) == 'v')){
 				this.charMap.get(yTemp).set(xTemp + c.coord[1], 'p');
 				this.charMap.get(yTemp).set(xTemp, 'c');
 			} else if ((this.charMap.get(yTemp).get(xTemp + 1) == 'v') &&
 				(this.charMap.get(yTemp).get(xTemp - 1) == 'v')){
 				this.charMap.get(yTemp + c.coord[0]).set(xTemp, 'p');
 				this.charMap.get(yTemp).set(xTemp, 'c');
 			} else {
 				this.charMap.get(yTemp).set(xTemp, 'g');
 				this.charMap.get(yTemp + c.coord[0]).set(xTemp + c.coord[1], 'p');	
 			}
 	}
 
 	public Map(int x, int y){
 		for (int i = 0; i < y; i++){
 			this.charMap.add(new ArrayList<Character>());
 			for (int j = 0; j < x; j++){
 				this.charMap.get(i).add('g');
 			}
 		}
 		generateMap();
 	}
 	
 	private int[] randomCoordinates(){
 		int[] a = new int[] { (int) (this.charMap.size()*Math.random()), (int) (this.charMap.get(0).size()*Math.random())};
 		return a;
 	}
 	
 	private boolean availableSpot(int[] rC){
 		Coordinate[] i = new Coordinate[] {
 				new Coordinate(rC[0] - 1, rC[1]),//vre nabo
 				new Coordinate(rC[0] + 1, rC[1]),//Nedre nabo
 				new Coordinate(rC[0], rC[1] - 1),//Venstre nabo
 				new Coordinate(rC[0], rC[1] + 1),//Hyre nabo
 				new Coordinate(rC[0] + 1, rC[1] + 1),
 				new Coordinate(rC[0] + 1, rC[1] - 1),
 				new Coordinate(rC[0] - 1, rC[1] + 1),
 				new Coordinate(rC[0] - 1, rC[1] - 1)};
 		int count = 0;
 		for (int j = 0; j < i.length; j++){
 			if (this.charMap.get(rC[0]).get(rC[1]) == 'g'){
 				if (!this.outsideOfMap(i[j]) && 
 						this.charMap.get(i[j].coord[0]).get(i[j].coord[1]) == 'g'){
 					count++;
 				}
 			}
 		}
 		return (count >= 7);
 	}
 	
 	public boolean outsideOfMap(Coordinate rC){
 		if ((rC.coord[0] < 0) || (rC.coord[1] < 0) ||
 			(rC.coord[0] >= this.charMap.size()) ||
 			(rC.coord[1] >= this.charMap.get(rC.coord[0]).size())) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private boolean availableRiverSpot(Coordinate rC){
 		 if ((rC.coord[0] >= 0) && (rC.coord[1] >= 0) &&
 				 (rC.coord[0] < this.charMap.size()) &&
 				 (rC.coord[1] < this.charMap.get(rC.coord[0]).size()) &&
 				 (this.charMap.get(rC.coord[0]).get(rC.coord[1]) == 'g')) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	private void createRiver(){
 		int[] riverStart = randomCoordinates(); //Startpunkt
 		int[] riverEnd = randomCoordinates(); //Endepunkt
 		if (riverStart[0] == riverEnd[0] &&
 				riverStart[1] == riverEnd[1]){
 			this.createRiver(); //Om startpunkt = endepunkt, prv igjen.
 		} else {
 			Coordinate riverS = new Coordinate(riverStart); //Lager koordinat-objekter for start og slutt.
 			Coordinate riverE = new Coordinate(riverEnd);
 			stretchRiver(riverS, riverE);
 		}
 	}
 	
 	private void stretchRiver(Coordinate a, Coordinate b){
 		ArrayList<Coordinate> Path = new ArrayList<Coordinate>();
 		Path.add(b);
 		boolean incomplete = true;
 		int numberOfSteps = 0;
 		Loop:
 		for (int i = 0; i < Path.size() && incomplete; i++){ //Bredde-frst sk algoritme
 			boolean[] sjekk = new boolean[4];
 			for (int j = 0; j < sjekk.length; j++){
 				sjekk[j] = true;
 			}
 			Coordinate[] naboer = new Coordinate[] {
 					new Coordinate(Path.get(i).coord[0] - 1, Path.get(i).coord[1], Path.get(i).coord[2] + 1),//Upper Neighbour
 					new Coordinate(Path.get(i).coord[0] + 1, Path.get(i).coord[1], Path.get(i).coord[2] + 1),//Lower Neighbour
 					new Coordinate(Path.get(i).coord[0], Path.get(i).coord[1] - 1, Path.get(i).coord[2] + 1),//Left Neighbour
 					new Coordinate(Path.get(i).coord[0], Path.get(i).coord[1] + 1, Path.get(i).coord[2] + 1)};//Right Neighbour
 			for (int j = 0; j < sjekk.length; j++){
 				for (int x = 0; x < Path.size(); x++){
 					if (this.outsideOfMap(naboer[j]) || //Her sjekker vi om punktet er utenfor kartet.
 						Path.get(x).compareYX(naboer[j])){ //Her sjekker vi om punktet eksisterer i Path allerede.
 						sjekk[j] = false; //Setter en boolean-verdi til false som vi sjekker senere for  se om koordinatet skal legges i Path.
 						break; 
 					}
 				}
 			}
 			for (int x = 0; x < naboer.length; x++){ 
 				if (sjekk[x] && this.availableRiverSpot(naboer[x])){ //Her sjekker vi om vi kan legge koordinatet i Path, og gjr det om det blir godkjent.
 					Path.add(naboer[x]);
 					if (naboer[x].compareYX(a)){
 						numberOfSteps = naboer[x].coord[2];
 						incomplete = false;
 						break Loop;
 					}
 				}
 			}
 		} //Nr While-lkka er ferdig skal Path inneholde koordinatene som lager en sti, blant mange andre koordinater.
 		ArrayList<Coordinate> finalPath = new ArrayList<Coordinate>(); //Ny ArrayList som skal kun inneholde riktig sti.
 		finalPath.add(new Coordinate(a.coord[0], a.coord[1], numberOfSteps)); //Den ene enden av elven.
 		for (int i = numberOfSteps; i >= 0; i--){
 			for (int j = 0; j < Path.size(); j++){
 				if (Path.get(j).coord[2] == (i - 1) && 
 					finalPath.get(numberOfSteps - i).distance(Path.get(j)) == 1.0){
 					finalPath.add(Path.get(j)); //Hvis koordinatet som vurderes er riktig nummer i rekka (antall skritt - nvrende skritt som vurderes),
 					break;
 				}// og avstanden mellom forrige sikre skritt og skrittet som vurderes er lik 1, s godkjennes den og blir det nye sikre skrittet.
 			}
 		}
 		finalPath.add(new Coordinate(b.coord[0], b.coord[1]));
 		for (int i = 0; i < finalPath.size(); i++){
 			this.charMap.get(finalPath.get(i).coord[0]).set(finalPath.get(i).coord[1], 'v');
 		} //Printer elven
 		this.createBridge(finalPath, 0);
 	}//Lager en bro p mf s ikke elven lukker av deler av kartet. Broen kan i verste fall vre blokkert av noe annet.
 	
 	private void createBridge(ArrayList<Coordinate> river, int i){
 		Bro:
 		while (i < 20){
 			Coordinate bridge = new Coordinate(river.get((int) ((int) river.size()*Math.random())));
 			Coordinate naboer[] = new Coordinate[] {
 					new Coordinate(bridge.coord[0] - 1, bridge.coord[1]),//vre nabo
 					new Coordinate(bridge.coord[0] + 1, bridge.coord[1]),//Nedre nabo
 					new Coordinate(bridge.coord[0], bridge.coord[1] - 1),//Venstre nabo
 					new Coordinate(bridge.coord[0], bridge.coord[1] + 1)};//Hyre nabo
 			for (int j = 0; j < naboer.length; j++){
 				if (this.outsideOfMap(naboer[j])){
 					i++;
 					break Bro;
 				}
 			}
 			if (((this.charMap.get(bridge.coord[0] + 1).get(bridge.coord[1]) == 'v') &&
					(this.charMap.get(bridge.coord[0] - 1).get(bridge.coord[1]) == 'v') &&
					((this.charMap.get(bridge.coord[0]).get(bridge.coord[1] + 1) == 'g') &&
					(this.charMap.get(bridge.coord[0]).get(bridge.coord[1] - 1) == 'g'))) ||
 					((this.charMap.get(bridge.coord[0]).get(bridge.coord[1] + 1) == 'v') &&
					(this.charMap.get(bridge.coord[0]).get(bridge.coord[1] - 1) == 'v') &&
					(this.charMap.get(bridge.coord[0] + 1).get(bridge.coord[1]) == 'g') &&
					(this.charMap.get(bridge.coord[0] - 1).get(bridge.coord[1]) == 'g'))){
 				this.charMap.get(bridge.coord[0]).set(bridge.coord[1], 'c');
 			} else {
 				i++;
 				this.createBridge(river, i);
 			}
 		}
 	}
 	
 	private void randomObject(){
 		double random = Math.random();
 		if(random < 0.10){
 			this.createRiver();		
 		} else if (random < 0.25){
 			createMarket(0);
 		} else if (random < 0.4){
 			createVillage(0);
 		} else {
 			createObstruction(0);
 		}
 	}
 	
 	private void createVillage(int i){
 		while (i < 10){
 			int[] randomCoordinate = randomCoordinates();
 			if (this.availableSpot(randomCoordinate)){
 				this.charMap.get(randomCoordinate[0]).set(randomCoordinate[1], 'l');
 			} else {
 				i++;
 				this.createMarket(i);
 			}
 		}
 	}
 	
 	private void createObstruction(int i){
 		while (i < 10){
 			int[] randomCoordinate = randomCoordinates();
 			double random = Math.random();
 			if (this.availableSpot(randomCoordinate)){
 				if (random < 0.5){
 					this.charMap.get(randomCoordinate[0]).set(randomCoordinate[1], 's');
 				} else {
 					this.charMap.get(randomCoordinate[0]).set(randomCoordinate[1], 't');
 				}
 			} else {
 				i++;
 				this.createObstruction(i);
 			}
 		}
 	}
 	
 	private void createMarket(int i){
 		while (i < 10){
 			int[] randomCoordinate = randomCoordinates();
 			if (this.availableSpot(randomCoordinate)){
 				this.charMap.get(randomCoordinate[0]).set(randomCoordinate[1], 'm');
 			} else {
 				i++;
 				this.createMarket(i);
 			}
 		}
 	}
 	
 	private int spaceAvailable(){
 		int a = 0;
 		for (int i = 0; i < this.charMap.size(); i++){
 			for (int j = 0; j < this.charMap.get(0).size(); j++){
 				if (this.charMap.get(i).get(j) == 'g'){
 					a += 1;
 				}
 			}
 		}
 		return a;
 	}
 	
 	private void generateMap(){
 		int spaceToBeOccupied = (int) (this.charMap.size()*this.charMap.get(0).size()*0.3 - 1);
 		int totalSpace = (int) this.charMap.size()*this.charMap.get(0).size();
 		while(this.spaceAvailable() > (totalSpace - spaceToBeOccupied)){
 			this.randomObject();
 		}
 	}
 	
 	public String toString(){
 		String b = "";
 		for (int i = 0; i < this.charMap.size(); i++){
 			for (int j = 0; j < this.charMap.get(i).size(); j++){
 				b += this.charMap.get(i).get(j);
 			}
 		b += "\n";	
 		}
 		return b;
 	}
 }
