 package pl.edu.agh.two.mud.common.world.model;
 
 import java.util.HashSet;
 import java.util.Random;
 import java.util.Set;
 
 import pl.edu.agh.two.mud.common.Creature;
 import pl.edu.agh.two.mud.common.ICreature;
 
 public class SampleBoard extends Board {
 
 	private static final String[] MONSTERS_NAMES = new String[] { "Szkielet", "Ork", "Duch", "Troll" };
 
 	private static final int MAX_MONSTER_POWER = 1;
 
 	private static final int MAX_MONSTER_LEVEL = 1;
 
 	private static final int MAX_BASE_HP = 4;
 
 	private Thread respawnThread;
 
 	private ICreature createRandomCreature() {
 		Random r = new Random();
 		int nameIndex = r.nextInt(MONSTERS_NAMES.length);
 
 		int strength = r.nextInt(MAX_MONSTER_POWER);
 		int power = 0;
 		int agility = 0;
 
 		int level = r.nextInt(MAX_MONSTER_LEVEL + 1);
 		int hp = r.nextInt(MAX_BASE_HP + 1) * level;
 
 		ICreature creature = new Creature();
 		creature.setName(MONSTERS_NAMES[nameIndex]);
 		creature.setStrength(strength);
 		creature.setPower(power);
 		creature.setAgililty(agility);
 
 		creature.setLevel(level);
 
 		creature.setMaxHealthPoints(hp);
 		creature.setHealthPoints(hp);
 
 		return creature;
 
 	}
 
 	public SampleBoard() {
 		final Field[][] fields = new Field[5][5];
 
 		// Creating fields
 
 		fields[0][0] = new Field(0, 0, "Pole startowe", "Budzisz sie, a za plecami masz portal.");
 		fields[0][1] = new Field(0, 1, "Sciezka na wschod", "Znajdujesz sie na sciezce biegnacej na wschod.");
 		fields[0][2] = new Field(0, 2, "Sciezka na wschod", "Przed Toba i za Toba jak okiem siegnac widac tylko droge.");
 		fields[0][3] = new Field(
 				0,
 				3,
 				"Skrzyzowanie",
 				"Docierasz do skrzyowania. Od cieki na wschd odchodzi dukt na poudnie. W oddali na wschodzie majaczy swietlna bariera.");
 		fields[0][4] = new Field(0, 4, "Koniec swiata",
 				"Przed Toba bariera swiatla. Znajdujesz sie na wschodnim koncu swiata.");
 
 		fields[1][3] = new Field(1, 3, "Sciezka na poludnie", "Znajdujesz sie na sciezce biegnacej na poludnie.");
 
 		fields[2][0] = new Field(2, 0, "Wejscie do lochow", "Przed Toba wejscie do mrocznych lochow");
 		fields[2][1] = new Field(2, 1, "Brukowana droga na zachod", "Podazasz po brukowanej drodze w strone lochow");
 		fields[2][2] = new Field(2, 2, "Przydrozna karczma",
 				"Ten okazaly budenk przyciaga wielu milosnikow mocnych trunkow");
 		fields[2][3] = new Field(2, 3, "Rozdroza", "Sciezki rozchodza sie w wszystkie strony swiata");
 		fields[2][4] = new Field(2, 4, "Koniec swiata",
 				"Przed Toba bariera swiatla. Znajdujesz sie na wschodnim koncu swiata.");
 
 		fields[3][0] = new Field(3, 0, "Mroczne lochy", "Mroczne lochy sa siedliskiem straszliwych stworow.");
 		fields[3][1] = new Field(3, 1, "Sala tortur w lochah", "Czuc zgnilizna, wszedzie wisza truchla");
 		fields[3][3] = new Field(3, 3, "Sciezka na poludnie", "Waski dukt biegnie kreto w kierunku poludniowym.");
 
 		fields[4][0] = new Field(4, 0, "Dolny poziom lochow", "Tu czai sie zlo. Wiedz, ze cos sie dzieje");
 		fields[4][3] = new Field(4, 3, "Koniec swiata", "Docierasz do poludniowego konca swiata");
 
 		// Creating creatures
 
 		fields[2][4].addCreature(createRandomCreature());
 		fields[3][0].addCreature(createRandomCreature());
 		fields[3][1].addCreature(createRandomCreature());
 		fields[4][0].addCreature(createRandomCreature());
 		fields[4][3].addCreature(createRandomCreature());
 
 		// Setting board
 
 		fields[0][0].setBoard(this);
 		fields[0][1].setBoard(this);
 		fields[0][2].setBoard(this);
 		fields[0][3].setBoard(this);
 		fields[0][4].setBoard(this);
 
 		fields[1][3].setBoard(this);
 
 		fields[2][0].setBoard(this);
 		fields[2][1].setBoard(this);
 		fields[2][2].setBoard(this);
 		fields[2][3].setBoard(this);
 		fields[2][4].setBoard(this);
 
 		fields[3][0].setBoard(this);
 		fields[3][1].setBoard(this);
 		fields[3][3].setBoard(this);
 
 		fields[4][0].setBoard(this);
 		fields[4][3].setBoard(this);
 
 		this.setFields(fields);
 		this.setStartingField(fields[0][0]);
 		this.setxAxisSize(5);
 		this.setyAxisSize(5);
 
 		respawnThread = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				Set<Field> fieldsToRespawn = new HashSet<Field>();
 				fieldsToRespawn.add(fields[2][4]);
 				fieldsToRespawn.add(fields[3][0]);
 				fieldsToRespawn.add(fields[3][1]);
 				fieldsToRespawn.add(fields[4][0]);
 				fieldsToRespawn.add(fields[4][3]);
 				while (true) {
 
 					for (Field field : fieldsToRespawn) {
 						if (field.getCreatures().size() == 0) {
 							field.addCreature(createRandomCreature());
 						}
 					}
 					try {
 						Thread.sleep(100000);
 					} catch (InterruptedException e) {
 
 					}
 				}
 			}
 		});
 	}
 }
