 package task9;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.ParseException;
 import java.util.Random;
 
 import chiffre.Grundlagen;
 import de.tubs.cs.iti.jcrypt.chiffre.BigIntegerUtil;
 import de.tubs.cs.iti.krypto.protokoll.Communicator;
 import de.tubs.cs.iti.krypto.protokoll.Protocol;
 
 public final class Vertrag implements Protocol {
 	// Schalter
 	private final boolean DEBUG = true; // DEBUG, Allgemein
 	private final boolean DEBUG_OB = false; // DEBUG für Task7 Elemente
 	private final boolean DEBUG_SS = true; // DEBUG für Task8 Elemente
 	private final boolean DEBUG_V = true; // DEBUG für Task9 Elemente
 	private final boolean TEST = true; // für Testwerte
 
 	// hier true setzen, wenn Alice beim secretsharing betrügen soll, d.h. Bits
 	// manipulieren
 	private final boolean BETRUG_ = false;
 	// hier true setzen, wenn Alice beim oblivious cheaten soll d.h. 2 gleiche
 	// Geheimnisse
 	private boolean betrug_ = false;
 
 	private static final int RADIX_SEND_ = 36;
 	private static final int SCHLEIFE_ = 0;
 
 	private BigInteger zwei = new BigInteger("2", 10);
 	private final BigInteger ZERO = new BigInteger("0");
 	private final BigInteger ONE = new BigInteger("1");
 	private final BigInteger TWO = new BigInteger("2");
 
 	static private int MinPlayer = 2;
 	static private int MaxPlayer = 2;
 	static private String NameOfTheGame = "Vertrag";
 
 	// ElGamal Eigene
 	private BigInteger myGamalP;
 	private BigInteger myGamalG;
 	private BigInteger myY;
 	private BigInteger myX;
 	private BigInteger help;
 	// ElGamal Partner
 	private BigInteger partnerGamalP;
 	private BigInteger partnerGamalG;
 	private BigInteger partnerY;
 
 	// Secret Sharing
 	private final BigInteger WORD_MAX = new BigInteger("zzzzzzzzzz", 36);
 	private BigInteger ssk; // = new BigInteger("7"); // 0...7
 	private BigInteger ssn; // = new BigInteger("10"); // Geheimnispaare, max 10
 	private BigInteger ssm = new BigInteger("52"); // Wortlänge (binary), max 52
 													// bits
 	private BigInteger ssChanceA; // Berechnungsvorteil A:B
 	private BigInteger ssChanceB; // Berechnungsvorteil A:B
 
 	// Task9 Konstanten
 	private final int symbolCount = 1;
 	private final String digestType = "SHA";
 	private String path = "../protokoll/vertrag.txt";
 	int target;
 
 	private Communicator Com;
 
 	public void setCommunicator(Communicator com) {
 		Com = com;
 	}
 
 	public void sendFirst() {
 		target = 1;
 		// (0)a Alice erzeugt sich einen ElGamal Key
 		makeElGamal();
 		// (0)b Alice sendet ihren PublicKey an Bob
 		sendElGamal(1);
 		// (0) Alice empfängt Bobs Schlüssel
 		receiveElGamal();
 
 		// (SS1) n und k für beide festlegen; k global: ssk; n global: ssn
 		ssn = BigIntegerUtil.randomSmallerThan((new BigInteger("10"))).add(ONE); // 1<=n<11
 		ssk = BigIntegerUtil.randomSmallerThan(new BigInteger("5")); // 0<=k<8
 		// (SS1) Berechnung der Berechnungsvorteile
 		setAdvantage(ssk);
 		// (SS1) Bob n und k senden
 		Com.sendTo(1, ssn.toString(RADIX_SEND_));
 		Com.sendTo(1, ssk.toString(RADIX_SEND_));
 		if (DEBUG_SS) {
 			System.out.println("DDD| (SS1) Sende n und k an Bob");
 			System.out.println("DDD| \t n = " + ssn.toString(RADIX_SEND_));
 			System.out.println("DDD| \t k = " + ssk.toString(RADIX_SEND_));
 		}
 
 		// p_A Primzahl < 2^52 und M << p_A zufällig bestimmen
 		BigInteger myP = PohligHellmann.generatePrime(51); // Primzahl < 2^52
 		// M << p_a jetzt als M <= p_a/16 umgesetzt
 		BigInteger myM = BigIntegerUtil.randomBetween(ZERO,
 				myP.divide(zwei.pow(4)));
 		// p_A und M an Bob senden
 		Com.sendTo(1, myP.toString(RADIX_SEND_));
 		Com.sendTo(1, myM.toString(RADIX_SEND_));
 		if (DEBUG_V)
 			System.out.println("DDD| myP = " + myP.toString(RADIX_SEND_));
 		if (DEBUG_V)
 			System.out.println("DDD| myM = " + myM.toString(RADIX_SEND_));
 
 		// p_B von Bob empfangen
 		BigInteger partnerP = new BigInteger(Com.receive(), RADIX_SEND_);
 		if (DEBUG_V)
 			System.out.println("DDD| partnerP = "
 					+ partnerP.toString(RADIX_SEND_));
 
 		// (SS2) aij mit i=1,...,n und j=1,2 erzeugen
 		// so dass ggT(aij,myP-1)=1 ist, d.h. die aij können als
 		// PohligHellmann Schlüssel verwendet werden
 		BigInteger[][] ssa = new BigInteger[ssn.intValue()][2];
 		for (int i = 0; i < ssa.length; i++) {
 			boolean isold = false;
 			do {
 				ssa[i][0] = genKey(myP);
 				ssa[i][1] = genKey(myP);
 				for (int j = 0; j < i; j++) {
 					if (ssa[i][0].equals(ssa[j][0]))
 						isold = true;
 					if (ssa[i][1].equals(ssa[j][1]))
 						isold = true;
 				}
 			} while (isold);
 		}
 
 		// Für jedes aij berechne cij = myM ^ aij mod myP
 		BigInteger[][] myC = new BigInteger[ssn.intValue()][2];
 		for (int i = 0; i < myC.length; i++) {
 			myC[i][0] = PohligHellmann.encipher(myM, myP, ssa[i][0]);
 			myC[i][1] = PohligHellmann.encipher(myM, myP, ssa[i][1]);
 		}
 
 		/*
 		 * //Dieser Betrug ist jetzt sinnlos -> auskommentiert if(betrug_){ int
 		 * randpair = ((int)(Math.random()*ssn.intValue()))%ssn.intValue();
 		 * ssc[randpair][1] = ssc[randpair][0]; }
 		 */
 
 		if (DEBUG_SS) {
 			System.out.println("DDD| Generierte aij:");
 			for (int i = 0; i < ssa.length; i++) {
 				System.out.print("DDD| \t ");
 				System.out.print(ssa[i][0].toString(RADIX_SEND_));
 				System.out.print("\t und ");
 				System.out.print(ssa[i][1].toString(RADIX_SEND_));
 				System.out.println();
 			}
 		}
 
 		// Alice und Bob senden sich gegenseitig ihre kompletten ssc
 		BigInteger[][] partnerC = new BigInteger[myC.length][2];
 		for (int i = 0; i < myC.length; i++) {
 			Com.sendTo(1, myC[i][0].toString(RADIX_SEND_));
 			partnerC[i][0] = new BigInteger(Com.receive(), RADIX_SEND_);
 			Com.sendTo(1, myC[i][1].toString(RADIX_SEND_));
 			partnerC[i][1] = new BigInteger(Com.receive(), RADIX_SEND_);
 		}
 
 		if (DEBUG_SS) {
 			System.out.println("DDD| Empfangene Cij:");
 			for (int i = 0; i < myC.length; i++) {
 				System.out.print("DDD| \t ");
 				System.out.print(partnerC[i][0].toString(RADIX_SEND_));
 				System.out.print("\t und ");
 				System.out.print(partnerC[i][1].toString(RADIX_SEND_));
 				System.out.println();
 			}
 		}
 
 		// Erklärung
 		// HERE A Erklärung
 		BigInteger[] statement; // Erklärung
 		BigInteger[] vertrag; // Vertrag
 		BigInteger[] contract; // Erklärung+Vertrag
 		BigInteger[] partnerStatement; // Erklärung des Partners
 		{
 			BigInteger myHashBig; // eigener Hash
 			BigInteger myHashS; // eigener Hash signiert
 			BigInteger partnerHashBig; // Partner Hash
 			BigInteger partnerHashS; // Partner Hash signiert
 			
 			BigInteger[][] agreementBigs = makeAgreement("A", "Bob", path,
 					symbolCount);
 			myHashBig = agreementBigs[0][0];
 			myHashS = agreementBigs[1][0];
 			statement = agreementBigs[2];
 			vertrag = agreementBigs[3];
 			contract = agreementBigs[4];
 			// A Erklärung senden
 			Com.sendTo(target, Erklaerung.changeToString(statement));
 			Com.sendTo(target, myHashBig.toString(RADIX_SEND_));
 			Com.sendTo(target, myHashS.toString(RADIX_SEND_));
 			// A Erklärung Partner empfangen
 			partnerStatement = Erklaerung.changeToBigs(Com.receive(),symbolCount);
 			partnerHashBig = new BigInteger(Com.receive(), RADIX_SEND_);
 			partnerHashS = new BigInteger(Com.receive(), RADIX_SEND_);
 
 			if(DEBUG_V) {
 				String dState, dVertrag, dComplete;
 				dState = Erklaerung.changeToString(statement);
 				dVertrag = Erklaerung.changeToString(vertrag);
 				dComplete = Erklaerung.changeToString(contract);
 				System.out.println("DDD| Eigener Gesamtvertrag:");
 				System.out.println("DDD| \t Erklärung:     " + dState);
 				System.out.println("DDD| \t Vertrag:       " + dVertrag);
 				System.out.println("DDD| \t Gesamtvertrag: " + dComplete);
 				
 				String pState, pVertrag, pComplete;
 				pState = Erklaerung.changeToString(partnerStatement);
 				pVertrag = Erklaerung.changeToString(vertrag);
 				BigInteger[] partnerComplete = Erklaerung.createStateContract(partnerStatement, vertrag);
 				pComplete = Erklaerung.changeToString(partnerComplete);
 				boolean hashOk = checkHash(partnerHashBig, partnerHashS);
 				System.out.println("DDD| Partner Gesamtvertrag:");
 				System.out.println("DDD| \t Erklärung:     " + pState);
 				System.out.println("DDD| \t Vertrag:       " + pVertrag);
 				System.out.println("DDD| \t Gesamtvertrag: " + pComplete);
 				System.out.println("DDD| \t Hash verify:   " + hashOk);
 			}
 			
 			// Erklärung prüfe Hash
 			boolean isHashOK = checkHash(partnerHashBig, partnerHashS);
 			if (!isHashOK) {
 				System.err.println("Abbruch, da Hash vom Partner ungültig!");
 				System.exit(1);
 			}
 		}
 
 		// Alice sendet oblivious die Hälfte der aij
 		sendSecrets(1, ssa);
 		// Alice empfängt oblivious die Hälfte der bij
 		BigInteger[][] ssb = receiveSecrets(1, ssn.intValue());
 		if (DEBUG_SS) {
 			System.out.println("DDD| (SS3) empfangene Geheimnisse: ");
 			for (int i = 0; i < ssa.length; i++) {
 				System.out.print("DDD| \t ");
 				if (ssb[i][0] != null) {
 					System.out.print(ssb[i][0].toString(RADIX_SEND_));
 				} else {
 					System.out.print("null");
 				}
 				System.out.print("\t und ");
 				if (ssb[i][1] != null) {
 					System.out.print(ssb[i][1].toString(RADIX_SEND_));
 				} else {
 					System.out.print("null");
 				}
 				System.out.println();
 			}
 		}
 		// Überprüfe, ob die bij die Lösungen der entsprechenden M-Puzzles sind
 		// d.h. gilt partnerC[i][j] = yM ^ ssb[i][j] mod partnerP für die
 		// bekannten ssb[i][j]?
 		if (DEBUG_V)
 			System.out.println("Prüfe bisher empfangene Schlüssel: ");
 		boolean keysOk = checkPartialPuzzles(partnerP, myM, partnerC, ssb);
 		// Abbruch falls keysOk = false
 		if (!keysOk) {
 			System.err.println("Cheater! Abbruch!");
 		} else {
 			// Falls Alice cheatet, flipt sie in jedem Paar ein random bit
 			if (BETRUG_) {
 				for (int i = 0; i < ssa.length; i++) {
 					int randindex = ((int) (Math.random() * 100)) % 2;
 					int randbit = ((int) (Math.random() * ssm.intValue()))
 							% ssm.intValue();
 					ssa[i][randindex] = ssa[i][randindex].flipBit(randbit);
 				}
 			}
 
 			// y-Listen generieren
 			BigInteger anzY = zwei.pow(ssk.intValue() + 1);
 			BigInteger[][][] my_yListen = new BigInteger[ssn.intValue()][2][anzY
 					.intValue()];
 			BigInteger[][][] their_yListen = new BigInteger[ssn.intValue()][2][anzY
 					.intValue()];
 			fillyListen(my_yListen);
 			fillyListen(their_yListen);
 
 			// (SS3) Tausche y aus
 			// Solange weniger als m bits gesendet
 			int sendM = ssk.intValue() + 1; // Anzahl der im ersten Schrit
 											// versendeten
 											// Bits
 			int whileEnde = ssm.intValue();
 			if ((SCHLEIFE_ > 0) && ((sendM + SCHLEIFE_) < whileEnde))
 				whileEnde = sendM + SCHLEIFE_;
 
 			// es werden 2^{k} verschiedene y ausgetauscht
 			int anzMes = (zwei.pow(ssk.intValue())).intValue();
 
 			while (sendM <= whileEnde) {
 				int target = 1;
 				// Sende an Bob Indizes y, die aus den yListen entfernt werden
 				// können
 				sendPrefixIndizes(ssa, my_yListen, sendM, anzMes, target);
 
 				// Empfange von Bob Indizes y die aus den yListen entfernt
 				// werden können
 				receivePrefixIndizes(their_yListen, sendM, anzMes);
 
 				if (sendM != whileEnde) {// im letzten Durchlauf nix mehr
 											// anhängen
 					// my_yListen aufräumen und mit 0 und 1 ergänzen
 					clean_yListen(my_yListen, sendM, anzMes);
 
 					// their_yListen aufräumen und mit 0 und 1 ergänzen
 					clean_yListen(their_yListen, sendM, anzMes);
 				}
 
 				// System.out.println("Ich habe übrig: ");
 				// show_yListen(my_yListen);
 				// System.out.println("In Bobs Geheimnissen ist übrig: ");
 				// show_yListen(their_yListen);
 
 				// Nächste Runde
 				sendM = sendM + 1;
 			}
 			// nun sind nock anzMes nachrichten der Länge ssm übrig, von denen
 			// anzMes-1 ausgeschlossen werden müssen
 			sendM = ssm.intValue();
 			anzMes--;
 			int target = 1;
 			// Sende an Bob Indizes y, die aus den yListen entfernt werden
 			// können
 			sendPrefixIndizes(ssa, my_yListen, sendM, anzMes, target);
 
 			// Empfange von Bob Indizes y die aus den yListen entfernt werden
 			// können
 			receivePrefixIndizes(their_yListen, sendM, anzMes);
 
 			// Jetzt sollte noch genau 1 Wort pro Liste übrig sein, mal gucken
 			// System.out.println("Ich habe übrig: ");
 			// show_yListen(my_yListen);
 			System.out.println("Bobs Geheimnisse sind: ");
 			show_yListen(their_yListen);
 
 			// Überprüfe, ob nun zu jedem M-Puzzle die Lösung vorhanden ist
 			// d.h. gilt partnerC[i][j] = yM ^ ssb[i][j] mod partnerP ?
 			if (DEBUG_V)
 				System.out
 						.println("Prüfe, ob alle M-Puzzle eine Lösung haben: ");
 			boolean allPuzzles = checkPuzzles(partnerP, myM, partnerC,
 					their_yListen);
 			if (allPuzzles)
 				System.out.println("Alles ok. Verträge sind unterzeichnet.");
 		}
 	}
 
 	public void receiveFirst() {
 		target = 0;
 		String sReceive;
 
 		// (0) Bob empfängt den Public-Key von Alice
 		receiveElGamal();
 		// (0) Bob macht sich eigenen ElGamal Key
 		makeElGamal();
 		// (0) Bob sendet public Key an Partner
 		sendElGamal(0);
 
 		// (SS1) Bob empfängt n und k
 		sReceive = Com.receive();
 		ssn = new BigInteger(sReceive, RADIX_SEND_);
 		sReceive = Com.receive();
 		ssk = new BigInteger(sReceive, RADIX_SEND_);
 		setAdvantage(ssk);
 		if (DEBUG_SS) {
 			System.out.println("DDD| (SS1) Empfangen:");
 			System.out.println("DDD| \t n = " + ssn.toString(RADIX_SEND_));
 			System.out.println("DDD| \t k = " + ssk.toString(RADIX_SEND_));
 		}
 
 		// p_A und M_A von Alice empfangen
 		BigInteger partnerP = new BigInteger(Com.receive(), RADIX_SEND_);
 		BigInteger partnerM = new BigInteger(Com.receive(), RADIX_SEND_);
 		if (DEBUG_V)
 			System.out.println("DDD| parterP = "
 					+ partnerP.toString(RADIX_SEND_));
 		if (DEBUG_V)
 			System.out.println("DDD| parterM = "
 					+ partnerM.toString(RADIX_SEND_));
 		// p_B Primzahl zufällig bestimmen, mit M << p_B < 2^52
 		BigInteger myP = PohligHellmann.generatePrime(51); // Primzahl < 2^52
 		// es gilt automatisch M << partnerP, da M höchstens 48 bit hat
 		// p_B an Alice senden
 		Com.sendTo(0, myP.toString(RADIX_SEND_));
 		if (DEBUG_V)
 			System.out.println("DDD| myP = " + myP.toString(RADIX_SEND_));
 
 		// (SS2) bij mit i=1,...,n und j=1,2 erzeugen
 		// so dass ggT(bij,myP-1)=1 ist, d.h. die bij können als
 		// PohligHellmann Schlüssel verwendet werden
 		BigInteger[][] ssb = new BigInteger[ssn.intValue()][2];
 		for (int i = 0; i < ssb.length; i++) {
 			boolean isold = false;
 			do {
 				ssb[i][0] = genKey(myP);
 				ssb[i][1] = genKey(myP);
 				for (int j = 0; j < i; j++) {
 					if (ssb[i][0].equals(ssb[j][0]))
 						isold = true;
 					if (ssb[i][1].equals(ssb[j][1]))
 						isold = true;
 				}
 			} while (isold);
 		}
 
 		// Für jedes bij berechne cij = M ^ bij mod myP
 		BigInteger[][] myC = new BigInteger[ssn.intValue()][2];
 		for (int i = 0; i < myC.length; i++) {
 			myC[i][0] = PohligHellmann.encipher(partnerM, myP, ssb[i][0]);
 			myC[i][1] = PohligHellmann.encipher(partnerM, myP, ssb[i][1]);
 		}
 
 		// if (DEBUG_SS) {
 		System.out.println("DDD| Generierte bij:");
 		for (int i = 0; i < ssb.length; i++) {
 			System.out.print("DDD| \t ");
 			System.out.print(ssb[i][0].toString(RADIX_SEND_));
 			System.out.print("\t und ");
 			System.out.print(ssb[i][1].toString(RADIX_SEND_));
 			System.out.println();
 		}
 		// }
 
 		// Alice und Bob senden sich gegenseitig ihre kompletten ssc
 		BigInteger[][] partnerC = new BigInteger[myC.length][2];
 		for (int i = 0; i < myC.length; i++) {
 			partnerC[i][0] = new BigInteger(Com.receive(), RADIX_SEND_);
 			Com.sendTo(0, myC[i][0].toString(RADIX_SEND_));
 			partnerC[i][1] = new BigInteger(Com.receive(), RADIX_SEND_);
 			Com.sendTo(0, myC[i][1].toString(RADIX_SEND_));
 		}
 
 		if (DEBUG_SS) {
 			System.out.println("DDD| Empfangene Cij:");
 			for (int i = 0; i < myC.length; i++) {
 				System.out.print("DDD| \t ");
 				System.out.print(partnerC[i][0].toString(RADIX_SEND_));
 				System.out.print("\t und ");
 				System.out.print(partnerC[i][1].toString(RADIX_SEND_));
 				System.out.println();
 			}
 		}
 
 		// Erklärung
 		// HERE B Erklärung
 		BigInteger[] statement; // Erklärung
 		BigInteger[] vertrag; // Vertrag
 		BigInteger[] contract; // Erklärung+Vertrag
 		BigInteger[] partnerStatement; // Erklärung des Partners
 		{
 			BigInteger myHashBig; // eigener Hash
 			BigInteger myHashS; // eigener Hash signiert
 			BigInteger partnerHashBig; // Partner Hash
 			BigInteger partnerHashS; // Partner Hash signiert
 			
 			BigInteger[][] agreementBigs = makeAgreement("B", "Alice", path,
 					symbolCount);
 			myHashBig = agreementBigs[0][0];
 			myHashS = agreementBigs[1][0];
 			statement = agreementBigs[2];
 			vertrag = agreementBigs[3];
 			contract = agreementBigs[4];
 			// B Erklärung Partner empfangen
 			partnerStatement = Erklaerung.changeToBigs(Com.receive(),symbolCount);
 			partnerHashBig = new BigInteger(Com.receive(), RADIX_SEND_);
 			partnerHashS = new BigInteger(Com.receive(), RADIX_SEND_);
 			// B Erklärung senden
 			Com.sendTo(target, Erklaerung.changeToString(statement));
 			Com.sendTo(target, myHashBig.toString(RADIX_SEND_));
 			Com.sendTo(target, myHashS.toString(RADIX_SEND_));
 
 			if(DEBUG_V) {
 				String dState, dVertrag, dComplete;
 				dState = Erklaerung.changeToString(statement);
 				dVertrag = Erklaerung.changeToString(vertrag);
 				dComplete = Erklaerung.changeToString(contract);
 				System.out.println("DDD| Eigener Gesamtvertrag:");
 				System.out.println("DDD| \t Erklärung:     " + dState);
 				System.out.println("DDD| \t Vertrag:       " + dVertrag);
 				System.out.println("DDD| \t Gesamtvertrag: " + dComplete);
 				
 				String pState, pVertrag, pComplete;
 				pState = Erklaerung.changeToString(partnerStatement);
 				pVertrag = Erklaerung.changeToString(vertrag);
 				BigInteger[] partnerComplete = Erklaerung.createStateContract(partnerStatement, vertrag);
 				pComplete = Erklaerung.changeToString(partnerComplete);
 				boolean hashOk = checkHash(partnerHashBig, partnerHashS);
 				System.out.println("DDD| Partner Gesamtvertrag:");
 				System.out.println("DDD| \t Erklärung:     " + pState);
 				System.out.println("DDD| \t Vertrag:       " + pVertrag);
 				System.out.println("DDD| \t Gesamtvertrag: " + pComplete);
 				System.out.println("DDD| \t Hash verify:   " + hashOk);
 			}
 			
 			// Erklärung prüfe Hash
 			boolean isHashOK = checkHash(partnerHashBig, partnerHashS);
 			if (!isHashOK) {
 				System.err.println("Abbruch, da Hash vom Partner ungültig!");
 				System.exit(1);
 			}
 		}
 
 		// Bob empfängt oblivious die Hälfte der aij
 		BigInteger[][] ssa = receiveSecrets(0, ssn.intValue());
 		if (DEBUG_SS) {
 			System.out.println("DDD| (SS3) empfangene Geheimnisse: ");
 			for (int i = 0; i < ssa.length; i++) {
 				System.out.print("DDD| \t ");
 				if (ssa[i][0] != null) {
 					System.out.print(ssa[i][0].toString(RADIX_SEND_));
 				} else {
 					System.out.print("null");
 				}
 				System.out.print("\t und ");
 				if (ssa[i][1] != null) {
 					System.out.print(ssa[i][1].toString(RADIX_SEND_));
 				} else {
 					System.out.print("null");
 				}
 				System.out.println();
 			}
 		}
 		// Überprüfe, ob die aij die Lösungen der entsprechenden M-Puzzles sind
 		// d.h. gilt partnerC[i][j] = partnerM ^ ssa[i][j] mod partnerP für die
 		// bekannten ssa[i][j]?
 		if (DEBUG_V)
 			System.out.println("Prüfe bisher empfangene Schlüssel: ");
 		boolean keysOk = checkPartialPuzzles(partnerP, partnerM, partnerC, ssa);
 		// Abbruch falls keysOk = false
 		if (!keysOk) {
 			System.err.println("Cheater! Abbruch!");
 		} else {
 
 			// Bob sendet oblivious die Hälfte der bij
 			sendSecrets(0, ssb);
 
 			// y-Listen generieren
 			BigInteger anzY = zwei.pow(ssk.intValue() + 1);
 			BigInteger[][][] my_yListen = new BigInteger[ssn.intValue()][2][anzY
 					.intValue()];
 			BigInteger[][][] their_yListen = new BigInteger[ssn.intValue()][2][anzY
 					.intValue()];
 			fillyListen(my_yListen);
 			fillyListen(their_yListen);
 
 			// (SS3) Tausche y aus
 			// Solange weniger als m bits gesendet
 			int sendM = ssk.intValue() + 1; // Anzahl der im ersten Schrit
 											// versendeten
 											// Bits
 			int whileEnde = ssm.intValue();
 			if ((SCHLEIFE_ > 0) && ((sendM + SCHLEIFE_) < whileEnde))
 				whileEnde = sendM + SCHLEIFE_;
 
 			// es werden 2^{k} verschiedene y ausgetauscht
 			int anzMes = (zwei.pow(ssk.intValue())).intValue();
 			boolean cheater = false;
 			while (sendM <= whileEnde && !cheater) {
 
 				// Empfange von Alice Indizes y die aus den yListen entfernt
 				// werden können
 				receivePrefixIndizes(their_yListen, sendM, anzMes);
 
 				int target = 0;
 				// Sende an Alice Indizes y, die aus den yListen entfernt werden
 				// können
 				sendPrefixIndizes(ssb, my_yListen, sendM, anzMes, target);
 
 				if (sendM != whileEnde) {// im letzten Durchlauf nix mehr
 											// anhängen
 					// my_yListen aufräumen und mit 0 und 1 ergänzen
 					clean_yListen(my_yListen, sendM, anzMes);
 
 					// their_yListen aufräumen und mit 0 und 1 ergänzen
 					clean_yListen(their_yListen, sendM, anzMes);
 				}
 
 				// checken, ob Alice manipuliert
 				cheater = checkBitflipCheat(ssa, their_yListen, sendM, cheater);
 
 				// System.out.println("Ich habe übrig: ");
 				// show_yListen(my_yListen);
 				// System.out.println("In Alices Geheimnissen ist übrig: ");
 				// show_yListen(their_yListen);
 
 				// Nächste Runde
 				sendM = sendM + 1;
 			}
 
 			// nun sind nock anzMes nachrichten der Länge ssm übrig, von denen
 			// anzMes-1 ausgeschlossen werden müssen
 			sendM = ssm.intValue();
 			anzMes--;
 			;
 			// Empfange von Alice Indizes y die aus den yListen entfernt werden
 			// können
 			receivePrefixIndizes(their_yListen, sendM, anzMes);
 
 			// gucke, ob Alice nicht doch betrogen hat
 			cheater = checkSameCheat(their_yListen, cheater);
 			// nur weitermachen, falls Alice nicht cheatet
 			if (!cheater) {
 				int target = 0;
 				// Sende an Alice Indizes y, die aus den yListen entfernt werden
 				// können
 				sendPrefixIndizes(ssb, my_yListen, sendM, anzMes, target);
 
 				// jetzt sollte noch genau 1 Wort pro yListe übrig sein, mal
 				// gucken
 				// System.out.println("Ich habe übrig: ");
 				// show_yListen(my_yListen);
 
 				System.out.println("Alices Geheimnisse sind: ");
 				show_yListen(their_yListen);
 
 				// Überprüfe, ob nun zu jedem M-Puzzle die Lösung vorhanden ist
 				// d.h. gilt partnerC[i][j] = yM ^ ssb[i][j] mod partnerP ?
 				if (DEBUG_V)
 					System.out
 							.println("Prüfe, ob alle M-Puzzle eine Lösung haben: ");
 				boolean allPuzzles = checkPuzzles(partnerP, partnerM, partnerC,
 						their_yListen);
 				if (allPuzzles)
 					System.out
 							.println("Alles ok. Verträge sind unterzeichnet.");
 			}
 		}
 	}
 
 	private boolean checkPartialPuzzles(BigInteger partnerP,
 			BigInteger partnerM, BigInteger[][] partnerC, BigInteger[][] ssa) {
 		boolean keysOk = true;
 		for (int i = 0; i < ssa.length; i++) {
 			if (ssa[i][0] != null) {
 				if (!partnerC[i][0].equals(PohligHellmann.encipher(partnerM,
 						partnerP, ssa[i][0]))) {
 					System.err.println("Fehlerhafter Schlüssel!");
 					keysOk = false;
 				} else {
 					if (DEBUG_V)
 						System.out.println("DDD| ssa[" + i + "][0]="
 								+ ssa[i][0].toString(RADIX_SEND_)
 								+ " als Key für C[" + i + "][0]="
 								+ partnerC[i][0].toString(RADIX_SEND_)
 								+ " bestätigt.");
 				}
 			}
 			if (ssa[i][1] != null) {
 				if (!partnerC[i][1].equals(PohligHellmann.encipher(partnerM,
 						partnerP, ssa[i][1]))) {
 					System.err.println("Fehlerhafter Schlüssel!");
 					keysOk = false;
 				} else {
 					if (DEBUG_V)
 						System.out.println("DDD| ssa[" + i + "][1]="
 								+ ssa[i][1].toString(RADIX_SEND_)
 								+ " als Key für C[" + i + "][1]="
 								+ partnerC[i][1].toString(RADIX_SEND_)
 								+ " bestätigt.");
 				}
 			}
 		}
 		return keysOk;
 	}
 
 	private boolean checkBitflipCheat(BigInteger[][] ssa,
 			BigInteger[][][] their_yListen, int sendM, boolean cheater) {
 		for (int i = 0; i < their_yListen.length; i++) {
 			// für jedes Paar
 			boolean manipulate = true;
 			BigInteger known = ssa[i][0];
 			if (known == null)
 				known = ssa[i][1];
 			// bilde das entsprechende Präfix von known
 			BigInteger modo = zwei.pow(sendM + 1);
 			known = known.mod(modo);
 			for (int j = 0; j < their_yListen[i].length; j++) {
 				for (int k = 0; k < their_yListen[i][j].length; k++) {
 					if (their_yListen[i][j][k] != null) {
 						BigInteger temp = their_yListen[i][j][k].mod(modo);
 						if (temp.equals(known)) {
 							manipulate = false;
 							j = their_yListen[i].length;
 							break;
 						}
 					}
 				}
 			}
 			if (manipulate) {
 				System.err.println("Alice manipuliert bits!");
 				cheater = true;
 				break;
 			}
 
 		}
 		return cheater;
 	}
 
 	private boolean checkSameCheat(BigInteger[][][] their_yListen,
 			boolean cheater) {
 		for (int i = 0; i < their_yListen.length && !cheater; i++) {
 			for (int j = 0; j < their_yListen[i][0].length && !cheater; j++) {
 				for (int k = 0; k < their_yListen[i][1].length && !cheater; k++) {
 					if (their_yListen[i][0][j] != null) {
 						if (their_yListen[i][1][k] != null) {
 							if (their_yListen[i][0][j]
 									.equals(their_yListen[i][1][k])) {
 								System.err
 										.println("Alice hat zwei gleiche Geheimnisse eingegeben. Betrug!");
 								cheater = true;
 								break;
 							}
 						}
 					}
 				}
 			}
 		}
 		return cheater;
 	}
 
 	private boolean checkPuzzles(BigInteger partnerP, BigInteger partnerM,
 			BigInteger[][] partnerC, BigInteger[][][] their_yListen) {
 		boolean allPuzzles = true;
 		for (int i = 0; i < partnerC.length; i++) {
 			for (int j = 0; j < partnerC[i].length; j++) {
 				for (int k = 0; k < their_yListen[i][j].length; k++) {
 					if (their_yListen[i][j][k] != null) {
 						if (!partnerC[i][j].equals(PohligHellmann.encipher(
 								partnerM, partnerP, their_yListen[i][j][k]))) {
 							System.err.println("Fehlerhafter Schlüssel!");
 							allPuzzles = false;
 						} else {
 							if (DEBUG_V)
 								System.out.println("DDD| ssa["
 										+ i
 										+ "]["
 										+ j
 										+ "]="
										+ their_yListen[i][j][k]
 												.toString(RADIX_SEND_)
 										+ " als Key für C[" + i + "][" + j
 										+ "]="
 										+ partnerC[i][j].toString(RADIX_SEND_)
 										+ " bestätigt.");
 						}
 					}
 				}
 			}
 		}
 		return allPuzzles;
 	}
 
 	private BigInteger genKey(BigInteger myP) {
 		BigInteger e;
 		boolean isGGT1 = false;
 		do {
 			// e \in setN mit 1<e<phi(p) = p-1 = 2<=e<phi(p)
 			e = BigIntegerUtil.randomBetween(zwei, myP.subtract(zwei),
 					new Random());
 			isGGT1 = (e.gcd(myP.subtract(ONE)).equals(ONE)); // ggT(e,p-1)=1
 		} while (!isGGT1);
 		return e;
 	}
 
 	private void show_yListen(BigInteger[][][] my_yListen) {
 		for (int i = 0; i < my_yListen.length; i++) {
 			for (int j = 0; j < my_yListen[i].length; j++) {
 				for (int k = 0; k < my_yListen[i][j].length; k++) {
 					if (my_yListen[i][j][k] != null) {
 						System.out.println("Geheimnis[" + i + "][" + j + "] = "
 								+ my_yListen[i][j][k].toString(RADIX_SEND_));
 					}
 				}
 			}
 		}
 	}
 
 	private void sendPrefixIndizes(BigInteger[][] ssa,
 			BigInteger[][][] my_yListen, int sendM, int anzMes, int target) {
 		// System.err.println(">>>entered sendPrefixIndizes");
 		// für jedes Geheimnispaar
 		for (int i = 0; i < my_yListen.length; i++) {
 			for (int j = 0; j < my_yListen[i].length; j++) {
 				int tobesent = anzMes;
 				// anzMes mal ein y senden und dann null setzen
 				while (tobesent > 0) {
 					// Index y auswürfeln
 					int y = (int) (Math.random() * (my_yListen[i][j].length));
 					if (y >= my_yListen[i][j].length)
 						y = 0;
 					// check ob da überhaupt noch was drin steht
 					if (my_yListen[i][j][y] != null) {
 						// check ob es kein Präfix ist
 						BigInteger modo = zwei.pow(sendM);
 						BigInteger prae = ssa[i][j].mod(modo);
 						if (!prae.equals(my_yListen[i][j][y])) {
 							// im Erfolgsfall schicke Index y und lösche den
 							// Eintrag
 							Com.sendTo(target, "" + y);
 							tobesent--;
 							my_yListen[i][j][y] = null;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private void receivePrefixIndizes(BigInteger[][][] their_yListen,
 			int sendM, int anzMes) {
 		// System.err.println(">>>entered receivePrefixIndizes");
 		// für jedes Geheimnispaar
 		for (int i = 0; i < their_yListen.length; i++) {
 			for (int j = 0; j < their_yListen[i].length; j++) {
 				int tobesent = anzMes;
 				// anzMes mal ein y senden und dann null setzen
 				while (tobesent > 0) {
 					// Index y empfangen
 					String sy = Com.receive();
 					BigInteger y = new BigInteger(sy, 10);
 					// Eintrag bei Index y löschen
 					their_yListen[i][j][y.intValue()] = null;
 					tobesent--;
 				}
 			}
 		}
 	}
 
 	private void clean_yListen(BigInteger[][][] my_yListen, int sendM,
 			int anzMes) {
 		BigInteger modo = zwei.pow(sendM);
 		for (int i = 0; i < my_yListen.length; i++) {
 			for (int j = 0; j < my_yListen[i].length; j++) {
 				// es sind noch anzMes Einträge != null, die werden nach vorn
 				// verschoben (und implizit 0 angehängt)
 				int index = 0;
 				int lauf = 0;
 				while (index < anzMes) {
 					if (my_yListen[i][j][lauf] != null) {
 						my_yListen[i][j][index] = my_yListen[i][j][lauf];
 						index++;
 					}
 					lauf++;
 				}
 				// danach alle Einträge noch mal kopieren aber 1 anhängen
 				for (int k = anzMes; k < my_yListen[i][j].length; k++) {
 					my_yListen[i][j][k] = my_yListen[i][j][k - anzMes]
 							.add(modo);
 				}
 			}
 		}
 	}
 
 	private void fillyListen(BigInteger[][][] my_yListen) {
 		for (int i = 0; i < my_yListen.length; i++) {
 			for (int j = 0; j < my_yListen[i].length; j++) {
 				for (int k = 0; k < my_yListen[i][j].length; k++) {
 					my_yListen[i][j][k] = new BigInteger("" + k, 10);
 				}
 			}
 		}
 	}
 
 	public String nameOfTheGame() {
 		return NameOfTheGame;
 	}
 
 	public int minPlayer() {
 		return MinPlayer;
 	}
 
 	public int maxPlayer() {
 		return MaxPlayer;
 	}
 
 	private String askString() {
 		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 		String s = "Tschüss bis zum nächsten Mal"; // dummymessage
 		try {
 			if ((s = in.readLine()) != null && s.length() != 0) {
 				// nix tun, in s steht jetzt der String
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return s;
 	}
 
 	/**
 	 * Setzt den Berechnungsvorteil anhand k
 	 */
 	private void setAdvantage(BigInteger k) {
 		ssChanceA = TWO.pow(k.intValue()).add(ONE);
 		ssChanceB = TWO.pow(k.intValue());
 	}
 
 	// ElGamal
 	/**
 	 * 
 	 */
 	private void makeElGamal() {
 		if (TEST) {
 			myGamalP = new BigInteger(
 					"13261063939096985426999424781129436987736604484071841574839029035275097976621226106248381646461633027127647215070176806960882462844165647876651836347109303");
 			myGamalG = new BigInteger(
 					"11449415071830494793854044177711897602839781159400329949451774490076059017229975065899539762216842867220320484076072264155276684642243703364069496832384226");
 			myY = new BigInteger(
 					"12291108192856071170865558012429961903760322492409283286333655332690208506622303938336813071688446187130979374752507108428801107842293004214159912505057697");
 			myX = new BigInteger(
 					"338247438063093584360735553456651782895945714953753136968197534452413025437614400799748890371900646240882573007655796701481099145579155445557798688838152");
 			help = myGamalP.subtract(BigIntegerUtil.TWO);
 		} else {
 			System.out.print("A: Generiere El-Gamal Key für mich... ");
 			System.out.print("Augenblick...");
 			int bitLength = 512;
 			BigInteger[] prime = Grundlagen.generatePrimePQ(bitLength);
 			myGamalP = prime[0];
 			myGamalG = Grundlagen.calcPrimeRoot(myGamalP, prime[1]);
 			System.out.println("\t [OK]");
 			// A wählt x zufällig in {1,...,p-2}
 			help = myGamalP.subtract(BigIntegerUtil.TWO);
 			myX = BigIntegerUtil.randomBetween(BigInteger.ONE, help);
 			// A berechnet y = g^xA mod p
 			myY = myGamalG.modPow(myX, myGamalP);
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void sendElGamal(int target) {
 		Com.sendTo(target, myGamalP.toString(RADIX_SEND_)); // p
 		Com.sendTo(target, myGamalG.toString(RADIX_SEND_)); // g
 		Com.sendTo(target, myY.toString(RADIX_SEND_)); // yA
 		if (DEBUG_OB) {
 			System.out.println("DDD| (0) Sende an Partner:");
 			System.out.println("DDD| \t p = " + myGamalP);
 			System.out.println("DDD| \t g = " + myGamalG);
 			System.out.println("DDD| (1) Sende an Partner: ");
 			System.out.println("DDD| \t y = " + myY);
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void receiveElGamal() {
 		String sReceive = Com.receive();
 		partnerGamalP = new BigInteger(sReceive, RADIX_SEND_);
 		sReceive = Com.receive();
 		partnerGamalG = new BigInteger(sReceive, RADIX_SEND_);
 		sReceive = Com.receive();
 		partnerY = new BigInteger(sReceive, RADIX_SEND_);
 		if (DEBUG_OB) {
 			System.out.println("DDD| (0) B empfängt von A:");
 			System.out.println("DDD| \t p = " + partnerGamalP);
 			System.out.println("DDD| \t g = " + partnerGamalG);
 			System.out.println("DDD| \t y = " + partnerY);
 		}
 	}
 
 	// Senden und Empfangen nach Oblivious Transfer
 	/**
 	 * @param messM0
 	 * @param messM1
 	 */
 	private void sendOblivious(int target, BigInteger messM0, BigInteger messM1) {
 		// (1)a Alice wählt zufällig zwei weitere Nachrichten m1 und m2;
 		BigInteger[] m = new BigInteger[2];
 		m[0] = BigIntegerUtil.randomBetween(BigInteger.ONE, help);
 		m[1] = BigIntegerUtil.randomBetween(BigInteger.ONE, help);
 		// (1)b Alice sendet m1 und m2 an Bob
 		Com.sendTo(target, m[0].toString(RADIX_SEND_)); // m1
 		Com.sendTo(target, m[1].toString(RADIX_SEND_)); // m2
 		if (DEBUG_OB) {
 			System.out.println("DDD| (1) Senden:");
 			System.out.println("DDD| \t m1 = " + m[0]);
 			System.out.println("DDD| \t m2 = " + m[1]);
 		}
 		// (2) Alice empfängt q von Bob
 		String getq = Com.receive();
 		BigInteger q = new BigInteger(getq, RADIX_SEND_);
 		if (DEBUG_OB) {
 			System.out.println("DDD| (2) Empfangen:");
 			System.out.println("DDD| \t q = " + q);
 		}
 		// (3)a Alice berechnet k0' und k1' und signiert sie
 		// ki'=(D_A((q-m[i])mod p²))mod p
 		BigInteger[] k = new BigInteger[2];
 		k[0] = (q.subtract(m[0])).mod(myGamalP.multiply(myGamalP));
 		k[0] = Grundlagen.elGamalDecipher(k[0], myX, myGamalP);
 		k[0] = k[0].mod(myGamalP);
 		k[1] = (q.subtract(m[1])).mod(myGamalP.multiply(myGamalP));
 		k[1] = Grundlagen.elGamalDecipher(k[1], myX, myGamalP);
 		k[1] = k[1].mod(myGamalP);
 		// (3)b Alice signiert k0 und k1
 		BigInteger[] Sk = new BigInteger[2];
 		Sk[0] = Grundlagen.elGamalSign(k[0], myGamalP, myGamalG, myY, myX);
 		Sk[1] = Grundlagen.elGamalSign(k[1], myGamalP, myGamalG, myY, myX);
 		if (betrug_) {
 			BigInteger i = BigIntegerUtil.randomBetween(BigInteger.ONE, help);
 			int ii = (i.mod(zwei)).intValue();
 			System.out.println("DDD| Fälsche Signatur k" + ii);
 			Sk[ii] = BigIntegerUtil.randomBetween(BigInteger.ONE,
 					myGamalP.subtract(zwei));
 		}
 		if (DEBUG_OB) {
 			boolean t0 = Grundlagen.elGamalVerify(k[0], Sk[0], myGamalP,
 					myGamalG, myY);
 			boolean t1 = Grundlagen.elGamalVerify(k[1], Sk[1], myGamalP,
 					myGamalG, myY);
 			System.out.println("DDD| (3)b Prüfe Signaturen:");
 			System.out.println("DDD| \t Sk[0] ist " + t0);
 			System.out.println("DDD| \t Sk[1] ist " + t1);
 		}
 		// (3)c Alice sendet beide Signaturen an Bob
 		Com.sendTo(target, Sk[0].toString(RADIX_SEND_));
 		Com.sendTo(target, Sk[1].toString(RADIX_SEND_));
 		if (DEBUG_OB) {
 			System.out.println("DDD| (3) Senden:");
 			System.out.println("DDD| \t S(k0) = " + Sk[0]);
 			System.out.println("DDD| \t S(k1) = " + Sk[1]);
 		}
 		// (3)d Alice wählt zufällig s aus {0,1}
 		BigInteger sbig = BigIntegerUtil.randomBetween(BigInteger.ONE, help);
 		sbig = sbig.mod(zwei);
 		int s;
 		// (3)e Alice berechnet (M_0+ks')mod n, (M_1+ks+1')mod n
 		s = sbig.intValue(); // s
 		BigInteger send0 = k[s].add(messM0).mod(myGamalP); // (M_0 + k[s]') mod
 															// n
 		s = sbig.xor(BigInteger.ONE).intValue(); // s xor 1
 		BigInteger send1 = k[s].add(messM1).mod(myGamalP); // (M_1 + k[s+1]')
 															// mod n
 		// (3)f send0 und send1 beides und s an Bob senden
 		Com.sendTo(target, send0.toString(RADIX_SEND_)); // send0
 		Com.sendTo(target, send1.toString(RADIX_SEND_)); // send1
 		Com.sendTo(target, sbig.toString(RADIX_SEND_)); // s
 		if (DEBUG_OB) {
 			System.out.println("DDD| (3)d-f Berechnete Werte");
 			System.out.println("DDD| \t s = " + sbig);
 			System.out
 					.println("DDD| \t send0 = " + send0.toString(RADIX_SEND_));
 			System.out
 					.println("DDD| \t send1 = " + send1.toString(RADIX_SEND_));
 		}
 
 		// (4) nichts tun
 	}
 
 	/**
 	 * 
 	 */
 	private BigInteger[] receiveAndCheckOblivious(int target) {
 		String sReceive;
 		// (1)b Bob empfängt m1 und m2
 		String m1 = Com.receive();
 		String m2 = Com.receive();
 		BigInteger[] m = new BigInteger[2];
 		m[0] = new BigInteger(m1, RADIX_SEND_);
 		m[1] = new BigInteger(m2, RADIX_SEND_);
 		if (DEBUG_OB) {
 			System.out.println("DDD| (0) B empfängt von A:");
 			System.out.println("DDD| \t m1 = " + m1);
 			System.out.println("DDD| \t m2 = " + m2);
 			System.out.println("DDD| \t m[0] = " + m[0]);
 			System.out.println("DDD| \t m[1] = " + m[1]);
 		}
 		// (2)a Bob wählt zufällig r aus {0,1} und k aus Z_p
 		BigInteger k = BigIntegerUtil.randomBetween(BigInteger.ONE,
 				partnerGamalP);
 		BigInteger r_z = BigIntegerUtil.randomBetween(BigInteger.ONE,
 				zwei.multiply(zwei));
 		int r = (r_z.mod(zwei)).intValue();
 		// (2)b Bob berechnet q=(E_A(k)+m_r)mod p²
 		BigInteger eak = Grundlagen.elGamalEncipher(k, partnerGamalP,
 				partnerGamalG, partnerY);
 		BigInteger q = (eak.add(m[r])).mod(partnerGamalP
 				.multiply(partnerGamalP));
 		// (2)c Bob sendet q an Alice
 		Com.sendTo(target, q.toString(RADIX_SEND_));
 		if (DEBUG_OB) {
 			System.out.println("DDD| (2) B sendet an A:");
 			System.out.println("DDD| \t r = " + r);
 			System.out.println("DDD| \t q = " + q);
 		}
 		// (3)b Bob empfängt die Signaturen von k0' und k1' von Alice
 		sReceive = Com.receive();
 		BigInteger Sk0 = new BigInteger(sReceive, RADIX_SEND_);
 		sReceive = Com.receive();
 		BigInteger Sk1 = new BigInteger(sReceive, RADIX_SEND_);
 		if (DEBUG_OB) {
 			System.out.println("DDD| (3) B empfängt von A:");
 			System.out.println("DDD| \t Sk0 = " + Sk0);
 			System.out.println("DDD| \t Sk1 = " + Sk1);
 		}
 		// (3)d Bob empfängt (M_0+ks')mod n, (M_1+ks+1')mod n und s von Alice
 		sReceive = Com.receive();
 		BigInteger rec0 = new BigInteger(sReceive, RADIX_SEND_);
 		sReceive = Com.receive();
 		BigInteger rec1 = new BigInteger(sReceive, RADIX_SEND_);
 		sReceive = Com.receive();
 		BigInteger s = new BigInteger(sReceive, RADIX_SEND_);
 		if (DEBUG_OB) {
 			System.out.println("DDD| (3)d Bob hat empfangen");
 			System.out.println("DDD| \t rec0 (send0) = "
 					+ rec0.toString(RADIX_SEND_));
 			System.out.println("DDD| \t rec1 (send1) = "
 					+ rec1.toString(RADIX_SEND_));
 			System.out.println("DDD| \t s = " + s.toString(RADIX_SEND_));
 		}
 
 		// (4)a Bob berechnet M_(s xor r)
 		BigInteger biR = new BigInteger("" + r, 10);
 		BigInteger calc;
 		BigInteger calcQuer; // Das ungenutzte, andere received
 		BigInteger t = biR.xor(s); // t = r xor s
 		if (s.intValue() == 0 && r == 0) { // t=0
 			calc = rec0; // M0 = M0 + k0
 			calcQuer = rec1; // M1 = M1 + k1
 		} else if (s.intValue() == 0 && r == 1) { // t=1
 			calc = rec1; // M1 = M1 + k1
 			calcQuer = rec0; // M0 = M0 + k0
 		} else if (s.intValue() == 1 && r == 1) { // t=0
 			calc = rec0; // M0 = M0 + k1
 			calcQuer = rec1; // M1 = M1 + k0
 		} else { // t=1
 			calc = rec1; // M1 = M1+k0
 			calcQuer = rec0; // M0 = M0 + k1
 		}
 		// M_(s xor r)
 		calc = calc.mod(partnerGamalP);
 		calc = calc.subtract(k); // sendT - k
 		calc = calc.mod(partnerGamalP); // sendT - k mod p
 
 		// kQuer_(r xor 1) = (calcQuer mod p - calc) mod p
 		BigInteger kQuer;
 		kQuer = calcQuer.mod(partnerGamalP);
 		kQuer = kQuer.subtract(calc);
 		kQuer = kQuer.mod(partnerGamalP);
 
 		BigInteger test = calcQuer.mod(partnerGamalP);
 		test = test.subtract(k);
 		test = test.mod(partnerGamalP);
 
 		if (DEBUG_OB) {
 			System.out.println("DDD| (4)a Bob berechnet M_(s xor r)");
 			System.out
 					.println("DDD| \t s xor r = " + s + " xor " + r + "=" + t);
 			System.out.println("DDD| \t M_(s xor r) = " + calc);
 			System.out.println("DDD| \t M_(s xor r) = " + calc.toString(36));
 			System.out.println("DDD| \t Test = " + test);
 			System.out.println("DDD| \t Test = " + test.toString(36));
 		}
 
 		// (4)b Bob prüft, ob Alice betrogen hat
 		boolean checkCheat = false;
 		// (4)b Prüfe, ob Signaturen für k und/oder kQuer gelten
 		boolean s0OK = Grundlagen.elGamalVerify(k, Sk0, partnerGamalP,
 				partnerGamalG, partnerY);
 		boolean s1OK = Grundlagen.elGamalVerify(k, Sk1, partnerGamalP,
 				partnerGamalG, partnerY);
 		boolean sQ0OK = Grundlagen.elGamalVerify(kQuer, Sk0, partnerGamalP,
 				partnerGamalG, partnerY);
 		boolean sQ1OK = Grundlagen.elGamalVerify(kQuer, Sk1, partnerGamalP,
 				partnerGamalG, partnerY);
 		if (DEBUG_OB) {
 			System.out.println("DDD| (4)b Prüfe gültige Siganturen");
 			System.out.println("DDD| \t k  : " + s0OK + " \t " + s1OK);
 			System.out.println("DDD| \t kQ : " + sQ0OK + " \t " + sQ1OK);
 		}
 
 		if (DEBUG_OB) {
 			System.out.println("DDD| (4)b BETRUGSVERSUCHE:");
 		}
 		// (4)b Signatur auf k ist falsch
 		boolean cheat1;
 		cheat1 = !(s0OK ^ s1OK);
 		// (4)b Signatur gilt für beide k
 		boolean cheat2;
 		cheat2 = ((s0OK && sQ0OK) || (s1OK && sQ1OK));
 		// (4)b Sk0=Sk1, identische Signaturen
 		boolean cheat3;
 		cheat3 = Sk0.equals(Sk1);
 		// (4)b EA(kQuer(rxor1)) = (q-m(rxor1)) mod p
 		boolean cheat4;
 		BigInteger eQuer;
 		BigInteger qQuer;
 		eQuer = Grundlagen.elGamalEncipher(kQuer, partnerGamalP, partnerGamalG,
 				partnerY);
 		qQuer = q.subtract(m[biR.xor(BigInteger.ONE).intValue()]).mod(
 				partnerGamalP);
 		cheat4 = eQuer.equals(qQuer);
 		if (DEBUG_OB) {
 			System.out.println("DDD| \t s(k) gilt nicht für k     : " + cheat1);
 			System.out.println("DDD| \t s(k) gilt für k und kQ    : " + cheat2);
 			System.out.println("DDD| \t s(k0)==s(k1)              : " + cheat3);
 			System.out.println("DDD| \t EA(kQ)==(q-m)mod n        : " + cheat4);
 		}
 
 		checkCheat = cheat1 || cheat2 || cheat3 || cheat4;
 		if (checkCheat) { // Betrüger
 			return null;
 		} else { // Ehrlich
 			BigInteger[] back = new BigInteger[2];
 			back[0] = calc;
 			back[1] = biR.xor(s);
 			return back;
 		}
 	}
 
 	/**
 	 * @param secrets
 	 */
 	private void sendSecrets(int target, BigInteger[][] secrets) {
 		for (int i = 0; i < secrets.length; i++) {
 			BigInteger send0 = secrets[i][0];
 			BigInteger send1 = secrets[i][1];
 			sendOblivious(target, send0, send1);
 		}
 	}
 
 	/**
 	 * @param ssb
 	 * @return
 	 */
 	private BigInteger[][] receiveSecrets(int target, int n) {
 		BigInteger[][] ssa = new BigInteger[n][2];
 		for (int i = 0; i < n; i++) {
 			BigInteger[] rec = receiveAndCheckOblivious(target);
 			if (rec == null) {
 				System.err.println("Alice betrügt!");
 				return null;
 			}
 			ssa[i][rec[1].intValue()] = rec[0];
 			ssa[i][rec[1].xor(ONE).intValue()] = null;
 		}
 		return ssa;
 	}
 
 	/**
 	 * 
 	 * @param myName
 	 * @param partnerName
 	 * @param path
 	 * @param symbolCount
 	 * @return Doppel-Array, der beinhaltet: [0][0] = eigenen Hash [1][0] =
 	 *         eigenen Hash Signiert [2] = Erklärung als BigInteger[] [3] =
 	 *         Vertrag als BigInteger[] [4] = Erklärung+Vertrag als BigInteger[]
 	 */
 	private BigInteger[][] makeAgreement(String myName, String partnerName,
 			String path, int symbolCount) {
 		int loc_n = ssn.intValue();
 		// Vertragstext-Datei einlesen
 		BigInteger[] vertragBigs;
 		vertragBigs = Grundlagen.readFile(path, symbolCount);
 		// Erkärung generieren
 		String stateS;
 		stateS = Erklaerung.generateStatement(myName, partnerName, loc_n);
 		BigInteger[] stateBigs = Erklaerung.changeToBigs(stateS, symbolCount);
 
 		// Erklärung (Erkärung+Vertrag) zusammenstellen
 		BigInteger[] combinedBigs = Erklaerung.createStateContract(stateBigs,
 				vertragBigs);
 
 		try {
 			// Erklärung hashen
 			MessageDigest myMD;
 			myMD = MessageDigest.getInstance(digestType);
 			byte[] myStateBytes = Erklaerung.changeToBytes(combinedBigs);
 			myMD.update(myStateBytes);
 			byte[] myHash = myMD.digest();
 			BigInteger myHashBig = Erklaerung.changeToBig(myHash);
 			// Erklärunghash signieren (elGamal)
 			BigInteger myHashS = Grundlagen.elGamalSign(myHashBig, myGamalP,
 					myGamalG, myY, myX);
 
 			// Return
 			BigInteger[][] back = new BigInteger[5][1];
 			back[0][0] = myHashBig;
 			back[1][0] = myHashS;
 			back[2] = stateBigs;
 			back[3] = vertragBigs;
 			back[4] = combinedBigs;
 
 			return back;
 		} catch (NoSuchAlgorithmException e) {
 			String errS = "Algorithmus " + digestType;
 			errS = errS + " für das Hashen wurde nicht gefunden!";
 			System.err.println(errS);
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 
 	/**
 	 * @param partnerHashBig
 	 * @param partnerHashS
 	 */
 	private boolean checkHash(BigInteger partnerHashBig, BigInteger partnerHashS) {
 		boolean ok;
 		ok = Grundlagen.elGamalVerify(partnerHashBig, partnerHashS,
 				partnerGamalP, partnerGamalG, partnerY);
 
 		return ok;
 	}
 }
