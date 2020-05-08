 /**
  * 
  */
 package graphenbib;
 
 import static org.junit.Assert.fail;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class MapGraphTest {
 
 	/**
 	 * Variablendefinition
 	 */
 	private static int[] laengen = new int[9];
 	
 	private static MapNode[] nodes = new MapNode[12];
 	//private static MapEdge[] edges = new MapEdge[9];
 	private static GPSCoordinate[] koord = new GPSCoordinate[12];
 	
 	private static MapGraph testGraph;
 	private static GPSCoordinate[] graphKoord = new GPSCoordinate[4];
 	
 	private static MapGraph failGraph;
 	//private static MapGraph failGraphRound;
 	
 	/*private static GPSCoordinate failKoord1;
 	private static GPSCoordinate failKoord2;
 	
 	private static GPSCoordinate failKoordRound1;
 	private static GPSCoordinate failKoordRound2; */
 	
 	//Diese Methode gibt eine Zufallszahl aus. min und max bezeichnen dabei allerdings die Vorkomma-Zahl - max.99 ist daher moeglich
 	private static float randomFloat(float min, float max) {
 		return (float) (Math.random() * (max - min + 1)) + min;
 	}
 	
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 		//bereite Variablen fuer Test vor
 		int i = 0;
 		//Koordinaten - zufaellige Werte
 		for (i=0;i<11;i++) {
 			//Longitude von -180 bis 180, latitude von -90 bis 90
 			koord[i] = new GPSCoordinate(randomFloat(-90,89),randomFloat(-180,179));
 		}
 		
 		koord[11] = new GPSCoordinate(randomFloat(-9,9),randomFloat(-10,10));
 		
 		
 		//Nodes
 		for (i=0;i<12;i++) {
 			nodes[i] = new MapNode(i,koord[i]);
 		}
 		
 		
 		
 		//Laengen
 		for (i=0;i<9;i++) {
 			laengen[i] = (int)Math.random();
 		}
 		/*
 		//Edges
 		edges[0] = new MapEdge(nodes[0],nodes[5],0,laengen[0],StreetType.LIVING_STREET);
 		edges[1] = new MapEdge(nodes[5],nodes[1],1,laengen[1],StreetType.PRIMARY);
 		edges[2] = new MapEdge(nodes[6],nodes[2],2,laengen[2],StreetType.MOTORWAY);
 		edges[3] = new MapEdge(nodes[2],nodes[6],3,laengen[3],StreetType.RESIDENTIAL);
 		edges[4] = new MapEdge(nodes[3],nodes[7],4,laengen[4],StreetType.ROAD);
 		edges[5] = new MapEdge(nodes[3],nodes[8],5,laengen[5],StreetType.SECONDARY);
 		edges[6] = new MapEdge(nodes[8],nodes[4],6,laengen[6],StreetType.TERTIARY);
 		edges[7] = new MapEdge(nodes[9],nodes[4],7,laengen[7],StreetType.TRUNK);
 		edges[8] = new MapEdge(nodes[0],nodes[5],8,laengen[8],StreetType.LIVING_STREET);
 		*/
 	
 		//Koordinaten fuer die Maps
 		graphKoord[0] = new GPSCoordinate(90,-180);
 		graphKoord[1] = new GPSCoordinate(-90,180);
 		graphKoord[2] = new GPSCoordinate(50,-150);
 		graphKoord[3] = new GPSCoordinate(-50,150);
 		
 		
 	}
 
 	/**
 	 * Test method for {@link graphenbib.MapGraph#MapGraph(graphenbib.GPS, graphenbib.GPS)}.
 	 */
 	@Test
 	public void testMapGraph() {
 		//Diese sollten funktionieren
 		try {
 			testGraph = new MapGraph(graphKoord[0],graphKoord[1]);
 		} catch (Exception e) {
 			fail("Fehler bei Graph anlegen "+e.getLocalizedMessage());
 		}
 		
 		//Dieser soll Fehler werfen (falsche Argumente, da verwechselt)
 		boolean fehler = false;
 		try {
 			@SuppressWarnings("unused") 
 			MapGraph graphFailTest = new MapGraph(graphKoord[1],graphKoord[0]);
 		} catch (Exception e) {
 			fehler = true;
 		}
 		if (fehler == false) {
 			fail("Konstruktor hat bei Failtest 1 keine Ausnahme geworfen");
 		}
 		
 		//Dieser soll Fehler werfen (ungueltige Argumente)
 		fehler = false;
 		try {
 			/*GPSCoordinate failKoord1 = new GPSCoordinate(-200,100);
 			GPSCoordinate failKoord2 = new GPSCoordinate(200,-100);*/
 			@SuppressWarnings("unused")
 			MapGraph graphFailTest = new MapGraph(null,null);
 		} catch (Exception e) {
 			fehler = true;
 		}
 		if (fehler == false) {
 			fail("Konstruktor hat bei Failtest 2 keine Ausnahme geworfen");
 		}
 		
 		//konstruiere failGraphen
 		try {
 			failGraph = new MapGraph(graphKoord[2],graphKoord[3]);
 		} catch (Exception e) {
 			fail("fehler bei failgraph am Ende "+e.getLocalizedMessage());
 		}
 		
 		
 	}
 
 	/**
 	 * Test method for {@link graphenbib.MapGraph#insertNode(int,graphenbib.GPSCoordinate)}.
 	 */
	@Test
 	public void testInsertNode() {
 		//Teste zunaechst einfuegen gueltiger Daten
 		try {
 			for (int i=0;i<11;i++) {
 				testGraph.insertNode(i, koord[i]);
 			}
 		} catch (Exception e) {
 			fail("Nodes einfuegen fehlgeschlagen: "+e.getLocalizedMessage());
 		}
 
 
 		
 		boolean fehler;
 		//Teste ungueltige Daten
 		fehler = false;
 		try {
 			failGraph.insertNode(0,null);
 		} catch (Exception e) {
 			fehler = true;
 		}
 		if (fehler == false) {
 			fail("ungueltige Node eingefuegt");
 		}
 		
 		//Test Node ausserhalb des Bereiches
 		fehler = false;
 		GPSCoordinate failGPS = null;
 		try {
 			failGPS = new GPSCoordinate(90,180);
 		} catch (Exception e) {
 			fail("Fehler bei anlegen einer GPS-Koordinate (180,90)");
 		}
 		
 		try {
 			failGraph.insertNode(5,failGPS);
 		} catch (Exception e) {
 			fehler = true;
 		}
 		if (fehler == false) {
 			fail("ungueltige Node eingefuegt (ausserhalb)");
 		}
 		/*
 		//Test Nodes im Bereich des failGraphRound
 		GPSCoordinate failNodeRound1GPS = new GPSCoordinate(175,5);
 		GPSCoordinate failNodeRound2GPS = new GPSCoordinate(165,15);
 		*/
 		/*
 		 * herausgenimmen, da 180Grad-Problematik vermutlich unwichtig
 		 * 
 		//Teste korrektes Einfuegen ueber Grenze
 		try {
 			failGraphRound.insertNode(100,failNodeRound1GPS,NodeType.CROSSING);
 		} catch (Exception e) {
 			fail("Hinzufuegen ueber die 180-Grad-Grenze erfolglos (1) "+e.getLocalizedMessage());
 		}
 		
 		//Teste inkorrektes Einfuegen ueber Grenze
 		fehler = false;
 		try {
 			failGraphRound.insertNode(101,failNodeRound2GPS,NodeType.CROSSING);
 		} catch (Exception e) {
 			fehler = true;
 		}
 		if (fehler == false) {
 			fail("Hinzufuegen ueber die 180-Grad-Grenze erfolglos (2)");
 		}
 		*/
 		
 		//gebe failGraph dennoch einen Knoten
 		try {
 			failGraph.insertNode(11,koord[11]);
 		} catch (Exception e) {
 			fail("Fehler beim Einfuegen einer Node in den failGraph "+e.getLocalizedMessage());
 		}
 		
 		//Teste nun, ob doppeltes einfuegen erkannt wird
 		fehler = false;
 		try {
 			failGraph.insertNode(11,koord[11]);
 		} catch (Exception e) {
 			fehler = true;
 		}
 		if (fehler == false)
 			fail("Es war moeglich, einen Knoten doppelt einzufuegen");
 		
 	}
 
 
 
 	/**
 	 * Test method for {@link graphenbib.MapGraph#insertEdge(int, int, int, int, graphenbib.StreetType)}.
 	 */
	@Test
 	public void testInsertEdgeIntIntIntFloatStreetType() {
 		try {
 			//Test zunaechst korrekte Daten
 			testGraph.insertEdge(0, 5, 0, laengen[0], StreetType.LIVING_STREET);
 			testGraph.insertEdge(5, 1, 1, laengen[1], StreetType.PRIMARY);
 			testGraph.insertEdge(6, 2, 2, laengen[2], StreetType.MOTORWAY);
 			testGraph.insertEdge(2, 6, 3, laengen[3], StreetType.RESIDENTIAL);
 			testGraph.insertEdge(3, 7, 4, laengen[4], StreetType.ROAD);
 			testGraph.insertEdge(3, 8, 5, laengen[5], StreetType.SECONDARY);
 			testGraph.insertEdge(8, 4, 6, laengen[6], StreetType.TERTIARY);
 			testGraph.insertEdge(9, 4, 7, laengen[7], StreetType.TRUNK);
 			testGraph.insertEdge(0, 5, 8, laengen[8], StreetType.LIVING_STREET);
 		} catch (Exception e) {
 			fail("korrektees einfuegen von Kanten (Param: int,int,int,float, StreetType) nciht funktionierend "+e.getLocalizedMessage());
 		}
 		
 		boolean fehler;
 		//Teste nun inkorrekte Daten (falsche Ids der Knoten)
 		fehler = false;
 		try {
 			failGraph.insertEdge(11, 1, 9, laengen[0], StreetType.LIVING_STREET);
 		} catch (Exception e) {
 			fehler = true;
 		}
 		if (fehler == false) {
 			fail("falsche Kante (UID-Test) eingefuegt");
 		}
 		
 		/*
 		 * herausgenommen -> Diskussion ueber Zykel nciht abgeschlossen
 		 * 
 		//Teste nun inkorrekte Daten (Zykel)
 		fehler = false;
 		try {
 			failGraph.insertEdge(11, 11, 500, laengen[0], StreetType.LIVING_STREET);
 		} catch (Exception e) {
 			fehler = true;
 		}
 		if (fehler == false) {
 			fail("falsche Kante (Zykel) eingefuegt");
 		}
 		*/
 		
 		//Teste nun inkorrekte Daten (doppelte WayID)
 		fehler = false;
 		try {
 			failGraph.insertEdge(11, 1, 4, laengen[0], StreetType.LIVING_STREET);
 		} catch (Exception e) {
 			fehler = true;
 		}
 		if (fehler == false) {
 			fail("falsche Kante (WayID-Test) eingefuegt");
 		}
 		
 		//Teste nun inkorrekte Daten (negatives Kantengewicht)
 		fehler = false;
 		try {
 			failGraph.insertEdge(11, 11, 10, -5, StreetType.LIVING_STREET);
 		} catch (Exception e) {
 			fehler = true;
 		}
 		if (fehler == false) {
 			fail("falsche Kante (laengen-Test) eingefuegt");
 		}
 	}
 
 	/**
 	 * Test method for {@link graphenbib.MapGraph#getNode(int)}.
 	 */
	@Test
 	public void testGetNode() {
 		//boolean[] korrekt1 = new boolean[11];
 		boolean[] korrekt2 = new boolean[11];
 		
 		//teste Node-Korrektheit
 		try {
 			for (int i=0;i<10;i++) {
 				korrekt2[i] = (testGraph.getNode(i).getUID() == i);
 				if (korrekt2[i] == false) {
 					fail("Daten nicht korrekt ausgegeben:"+i);
 				}
 			}
 		} catch (NullPointerException e) {
 			fail("Konnte Nodes nciht abholen mit getNode(uid) "+e.getLocalizedMessage());
 		}
 				
 		boolean[] korrekt = new boolean[11];
 		//Teste auf Graph Incomingedges
 		//Daten abholen
 		MapEdge[] liste0 = testGraph.getNode(0).getIncomingEdges();
 		MapEdge[] liste1 = testGraph.getNode(1).getIncomingEdges();
 		MapEdge[] liste2 = testGraph.getNode(2).getIncomingEdges();
 		MapEdge[] liste3 = testGraph.getNode(3).getIncomingEdges();
 		MapEdge[] liste4 = testGraph.getNode(4).getIncomingEdges();
 		MapEdge[] liste5 = testGraph.getNode(5).getIncomingEdges();
 		MapEdge[] liste6 = testGraph.getNode(6).getIncomingEdges();
 		MapEdge[] liste7 = testGraph.getNode(7).getIncomingEdges();
 		MapEdge[] liste8 = testGraph.getNode(8).getIncomingEdges();
 		MapEdge[] liste9 = testGraph.getNode(9).getIncomingEdges();
 		MapEdge[] liste10 = testGraph.getNode(10).getIncomingEdges();
 		
 		//Vergleiche Daten der leeren Listen
 		korrekt[0] = (liste0.length == 0);
 		korrekt[3] = (liste3.length == 0);
 		korrekt[9] = (liste9.length == 0);
 		korrekt[10] = (liste10.length == 0);
 		//vergleiche andere Nodes (nciht trivial)
 		//Node 1 - nur Kante 1
 		korrekt[1] = ((liste1.length==1) && (liste1[0].getUID() == 1));
 		//Node 2 - nur Kante 2
 		korrekt[2] = ((liste2.length==1) && (liste2[0].getUID() == 2));
 		//Node 4 - nur kanten 6,7 
 		korrekt[4] = ((liste4.length==2) && ((liste4[0].getUID() == 6) || (liste4[0].getUID() == 7)) &&  ((liste4[1].getUID() == 6) || (liste4[1].getUID() == 7)));
 		//Node 5 - nur kanten 0,8
 		korrekt[5] = ((liste5.length==2) && ((liste5[0].getUID() == 0) || (liste5[0].getUID() == 8)) &&  ((liste5[1].getUID() == 0) || (liste5[1].getUID() == 8)));
 		//Node 6 - nur Kante 3
 		korrekt[6] = ((liste6.length==1) && (liste6[0].getUID() == 3));
 		//Node 7 - nur Kante 4
 		korrekt[7] = ((liste7.length==1) && (liste7[0].getUID() == 4));
 		//Node 8 - nur Kante 5
 		korrekt[8] = ((liste8.length==1) && (liste8[0].getUID() == 5));
 		
 		for (int i=0;i<11;i++) {
 			if (korrekt[i] == false) fail("Fehler bei IncomingEdges-Test (2) Nummer "+i);
 		}
 		
 		//Test auf Datenkonsistenz
 		liste4 = null;
 		liste4 = testGraph.getNode(4).getIncomingEdges();
 		if (liste4[0] == null) { fail("Incoming-Liste nciht konsistent"); }
 		
 		
 		//Teste auf Graph OutgoingEedges
 		//Daten abholen
 		liste0 = testGraph.getNode(0).getOutgoingEdges();
 		liste1 = testGraph.getNode(1).getOutgoingEdges();
 		liste2 = testGraph.getNode(2).getOutgoingEdges();
 		liste3 = testGraph.getNode(3).getOutgoingEdges();
 		liste4 = testGraph.getNode(4).getOutgoingEdges();
 		liste5 = testGraph.getNode(5).getOutgoingEdges();
 		liste6 = testGraph.getNode(6).getOutgoingEdges();
 		liste7 = testGraph.getNode(7).getOutgoingEdges();
 		liste8 = testGraph.getNode(8).getOutgoingEdges();
 		liste9 = testGraph.getNode(9).getOutgoingEdges();
 		liste10 = testGraph.getNode(10).getOutgoingEdges();
 		
 		//Vergleiche Daten der leeren Listen
 		korrekt[1] = (liste1.length == 0);
 		korrekt[4] = (liste4.length == 0);
 		korrekt[7] = (liste7.length == 0);
 		korrekt[10] = (liste10.length == 0);
 			
 		//Vergleiche andere Nodes
 		//Node 0 - Nur Kanten 0,8
 		korrekt[0] = ((liste0.length==2) && ((liste0[0].getUID() == 0) || (liste0[0].getUID() == 8)) &&  ((liste0[1].getUID() == 0) || (liste0[1].getUID() == 8)));
 		//Node 2 - Nur Kante 3
 		korrekt[2] = ((liste2.length==1) &&  (liste2[0].getUID() == 3));
 		//Node 3 - Nur Kanten 4,5
 		korrekt[3] = ((liste3.length==2) && ((liste3[0].getUID() == 4) || (liste3[0].getUID() == 5)) &&  ((liste3[1].getUID() == 4) || (liste3[1].getUID() == 5)));
 		//Node 5 - Nur Kante 1
 		korrekt[5] = ((liste5.length==1) &&  (liste5[0].getUID() == 1));
 		//Node 6 - Nur Kante 2
 		korrekt[6] = ((liste6.length==1) &&  (liste6[0].getUID() == 2));
 		//Node 8 - Nur Kante 6
 		korrekt[8] = ((liste8.length==1) &&  (liste8[0].getUID() == 6));
 		//Node 9 - Nur Kante 7
 		korrekt[9] = ((liste9.length==1) &&  (liste9[0].getUID() == 7));
 		
 		for (int i=0;i<11;i++) {
 			if (korrekt[i] == false) fail("Fehler bei OutgingEdges-Test (2) "+i);
 		}
 		
 		//Test auf Datenkonsistenz
 		liste3 = null;
 		liste3 = testGraph.getNode(3).getOutgoingEdges();
 		if (liste3[0] == null) { fail("Outgoing-Liste nciht konsistent"); }
 		
 	}
 	
 	/**
 	 * Test method for {@link graphenbib.MapGraph#deleteIsolatedNodes()}. 
 	 * Verwaiste Knoten sind solche ohne eingehende und ausgehende Kanten
 	 * Daher failed der Test momentan.
 	 */
	@Test
 	public void testDeleteIsolatedNodes() {
 		/*
 		 * Hier soll die Contract-methode getestet werden.
 		 * Derzeitiges Design der Methode bedeutet:
 		 * - entferne Verwaiste Knoten, wobei verwaiste Knoten entweder keine ohne nur eingehende Kanten haben
 		 */
 		//logger.log("MapGraphTest",(testGraph));
 		try {
 			testGraph.insertNode(11, new GPSCoordinate(5,5));
 		} catch (Exception e) {
 			fail("Fehler beim Hinzufuegen einer Node: "+e.getLocalizedMessage());
 		}
 		
 		//Nutze TestGraph - nach der Contract-Methode sollten alle Knoten ausser Knoten 10 erhalten bleiben
 		try {
 			testGraph.deleteIsolatedNodes();
 		} catch (Exception e) {
 			fail("Fehler in der DIN-Methode: "+e.getLocalizedMessage());
 		}
 		
 		if (testGraph.getNode(0) == null)
 			fail("Node 0 wurde geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(1) == null)
 			fail("Node 1 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(2) == null)
 			fail("Node 2 wurde geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(3) == null)
 			fail("Node 3 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(4) == null)
 			fail("Node 4 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(5) == null)
 			fail("Node 5 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(6) == null)
 			fail("Node 6 wurde geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(7) == null)
 			fail("Node 7 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(8) == null)
 			fail("Node 8 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(9) == null)
 			fail("Node 9 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(10) != null)
 			fail("Node 10 wurde nciht geloescht, obwohl sie verwaist war");
 		if (testGraph.getNode(11) != null)
 			fail("Node 11 wurde nciht geloescht, obwohl sie verwaist war");
 		
 		//Was passiert eigentlich bei einem Aufru von Contract, bei keinen verwaisten Knoten?
 		try {
 			testGraph.deleteIsolatedNodes();
 		} catch (Exception e) {
 			fail("Fehler in der DIN-Methode (bei zweitem Aufruf): "+e.getLocalizedMessage());
 		}
 			/*
 		for (int i=0;i<10;i++) {
 			if (testGraph.getNode(i) == null)
 				fail("Es wurde Node "+i+" geloescht, obwohl diese nicht verwaist war (Nach zwei Contracts)");
 		}
 			*/
 
 
 		if (testGraph.getNode(0) == null)
 			fail("Node 0 wurde geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(1) == null)
 			fail("Node 1 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(2) == null)
 			fail("Node 2 wurde geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(3) == null)
 			fail("Node 3 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(4) == null)
 			fail("Node 4 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(5) == null)
 			fail("Node 5 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(6) == null)
 			fail("Node 6 wurde geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(7) == null)
 			fail("Node 7 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(8) == null)
 			fail("Node 8 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(9) == null)
 			fail("Node 9 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(10) != null)
 			fail("Node 10 wurde nciht geloescht, obwohl sie verwaist war");
 		if (testGraph.getNode(11) != null)
 			fail("Node 11 wurde nciht geloescht, obwohl sie verwaist war");
 
 		
 		//Was passiert eigentlich, wenn es "Luecken" bzgl. der Reihenfolge der Nodes gibt?
 		try {
 			testGraph.insertNode(100, new GPSCoordinate(5,5));
 		} catch (Exception e) {
 			fail("Fehler beim Hinzufuegen einer Node: "+e.getLocalizedMessage());
 		}
 		try {
 			testGraph.deleteIsolatedNodes();
 		} catch (Exception e) {
 			fail("Fehler beim Aufru der DIN-Methode mit \"Luecken\" in den Node-UIDs: "+e.getLocalizedMessage());
 		}
 		
 		try {
 			testGraph.deleteIsolatedNodes();
 		} catch (Exception e) {
 			fail("Fehler in der DIN-Methode: "+e.getLocalizedMessage());
 		}
 		/*
 		for (int i=0;i<10;i++) {
 			if (testGraph.getNode(i) == null)
 				fail("Es wurde Node "+i+" geloescht, obwohl diese nicht verwaist war (Nach zwei Contracts)");
 		}*/
 		
 		if (testGraph.getNode(0) == null)
 			fail("Node 0 wurde geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(1) == null)
 			fail("Node 1 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(2) == null)
 			fail("Node 2 wurde geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(3) == null)
 			fail("Node 3 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(4) == null)
 			fail("Node 4 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(5) == null)
 			fail("Node 5 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(6) == null)
 			fail("Node 6 wurde geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(7) == null)
 			fail("Node 7 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(8) == null)
 			fail("Node 8 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(9) == null)
 			fail("Node 9 wurde  geloescht, obwohl sie nciht verwaist war");
 		if (testGraph.getNode(10) != null)
 			fail("Node 10 wurde nciht geloescht, obwohl sie verwaist war");
 		if (testGraph.getNode(11) != null)
 			fail("Node 11 wurde nciht geloescht, obwohl sie verwaist war");
 		if (testGraph.getNode(100) != null)
 			fail("Node 100 wurde nciht geloescht, obwohl sie verwaist war (Nach zwei Contracts)");
 		
 	}
 	
 //	@Test
 //	public void testMerges() {
 //		MapGraph g1=null,g2=null,g3=null;
 //		try {
 //			g1 = new MapGraph(1, 1, 0, 0);
 //		} catch (Exception e) {
 //			fail("Es gabe inen Fehler beim Anlegen des Graphen g1: "+e.getLocalizedMessage());
 //		}
 //		
 //		try {
 //			g2 = new MapGraph(1.5f, 2, 0, 1);
 //		} catch (Exception e) {
 //			fail("Es gabe inen Fehler beim Anlegen des Graphen g2: "+e.getLocalizedMessage());
 //		}
 //		
 //		try {
 //			g3 = new MapGraph(2, 3, 0, 2);
 //		} catch (Exception e) {
 //			fail("Es gabe inen Fehler beim Anlegen des Graphen g3: "+e.getLocalizedMessage());
 //		}
 //		
 //		try {
 //			g1.insertNode(1, new GPSCoordinate(0.5f,0.5f));
 //			g1.insertNodeWithoutGPS(2);
 //			
 //			g2.insertNode(2, new GPSCoordinate(1.4f,1.4f));
 //			g2.insertNodeWithoutGPS(3);
 //			g2.insertNodeWithoutGPS(1);
 //			
 //			g3.insertNode(3, new GPSCoordinate(1.4f,2.4f));
 //			g3.insertNodeWithoutGPS(2);
 //		} catch (Exception e) {
 //			fail("Es gab einen Fehler beim Anlegen der Nodes: "+e.getLocalizedMessage());
 //		}
 //		
 //		try {
 //			g1.insertEdge(1, 2, 100, 500, StreetType.TERTIARY);
 //			g1.insertEdge(2, 1, 100, 500, StreetType.TERTIARY);
 //			
 //			g2.insertEdge(1, 2, 101, 500, StreetType.TERTIARY);
 //			g2.insertEdge(2, 1, 101, 500, StreetType.TERTIARY);
 //			g2.insertEdge(2, 3, 102, 500, StreetType.TERTIARY);
 //			g2.insertEdge(3, 2, 102, 500, StreetType.TERTIARY);
 //			
 //			g3.insertEdge(2, 3, 103, 500, StreetType.TERTIARY);
 //			g3.insertEdge(3, 2, 103, 500, StreetType.TERTIARY);
 //		} catch (Exception e) {
 //			fail("Fehler beim Anlegen der Edges: "+e.getLocalizedMessage());
 //		}
 //		//Versuhe zu mergen
 //		MapGraph g5 = null;
 //		try {
 //			g5 = MapGraph.mergeGraphs(g1, g2);
 //		} catch (Exception e) {
 //			fail("Es gab einen Fehler beim Mergen: "+e.getLocalizedMessage());
 //		}
 //		if (g5 == null)
 //			fail("Es wurde ein Nullpointer statt eines MapGraphen zurueckgegeben");
 //		for(int i=1; i<4; i++) {
 //			if (g5.getNode(i) == null)
 //				fail("Node "+i+" sollte eigentlich im Graphen sein, is aber nich");
 //		}
 //		ArrayList<MapNode> weg = null;
 //		try {
 //			weg = Dijkstra.bidirectional(g5, 1, 3);
 //		} catch (Exception e) {
 //			fail("Es gab einen Fehler im Dijkstra: "+e.getLocalizedMessage());
 //		}
 //		if (weg.size() != 3)
 //			fail("Der Weg wurde nciht korrekt ermittelt");
 //		
 //		//Neuer Versuch ohne g2 (sollte klappen)
 //		try {
 //			g5 = MapGraph.mergeGraphs(g1, g3);
 //		} catch (Exception e) {
 //			fail("Es gab einen Fehler beim Mergen: "+e.getLocalizedMessage());
 //		}
 //		if (g5 == null)
 //			fail("Es wurde ein Nullpointer statt eines MapGraphen zurueckgegeben");
 //		for(int i=1; i<4; i++) {
 //			if (g5.getNode(i) == null)
 //				fail("Node "+i+" sollte eigentlich im Graphen sein, is aber nich");
 //		}
 //		try {
 //			weg = Dijkstra.bidirectional(g5, 1, 3);
 //		} catch (Exception e) {
 //			fail("Es gab einen Fehler im Dijkstra: "+e.getLocalizedMessage());
 //		}
 //		if (weg.size() != 3)
 //			fail("Der Weg wurde nciht korrekt ermittelt");
 //		
 //	}
 
 }
