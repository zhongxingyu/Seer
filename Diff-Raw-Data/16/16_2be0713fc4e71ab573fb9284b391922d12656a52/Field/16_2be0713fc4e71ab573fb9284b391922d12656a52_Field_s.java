 package it.univr.GameOfLife;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Hashtable;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButton;
 import javax.swing.JSlider;
 //import javax.swing.UIManager;
 //import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 /**
  * 
  * 
  *
  *<p>
  * La classe Field  il corpo del nostro programma, gestisce diversi elementi tra cui
  * l'oggetto widget, che permette di scegliere tra diverse opzioni run-time e non, e il
  * selettore delle figure, il quale permette di inserire varie figure pre impostate.
  * <p>
  * In sostanza la classe Field corrisponde al campo di gioco e da essa parte l'esecuzione
  * del programma.
  * <p>
  * @author Nicola "Field" Castellani
  * @author Pietro "Drawer" Musoni
  */
 public class Field extends JFrame{
 	//vettore di 2 elementi con la dimensione selezionata dall'utente
 	private static int sizeSelected[] = new int[2];	
 	//true se l'utente ha selezionato la dimensione
 	private static boolean endSelect = false;
 	//true se  stato selezionato un radiobutton
 	private static boolean radioButtonClicked = false;
 	//dimensione dello schermo del computer host
 	private static Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
 	//container della classe Field
 	private Container contentPane = getContentPane();
 	//matrice principale di gioco
 	public static fieldButtons[][] buttons;
 	//matrice secondaria per calcolare la generazione successiva
 	static fieldButtons[][] nextGen;
 	//matrice secondaria per catturare una generazione, utile se si vuole memorizzare
 	//una determinata struttura e la sua evoluzione
 	private static fieldButtons[][] screenShot;
 	//velocit selezionata di gioco
 	private static int gameSpeed = 1;
 	//true se l'utente ha deciso di iniziare il gioco
 	private static boolean startTheGame = false;
 	//true se  la prima volta che premiamo start
 	private static boolean firstTime = true;
 	//vettore con le varie velocit di gioco
 	private static final int[] velocity = {2000,1500,1000,700,600,500,400,200,100,50};
 	//Jlabel per la visualizzazione della generazione corrente
 	private static JLabel generations;
 	//numero della generazione corrente
 	private static int generationNumber = 0;
 	//true se l'utente ha deciso di effettuare uno screenshot
 	private static boolean screenShotSelected = false;
 	//true se l'utente ha deciso di mettere in pausa
 	private boolean pause = false;
 	//true se la generazione corrente non ha cellule vive
 	private boolean isEmpty = false;
 	//true se l'utente vuole pulire il campo
 	private boolean clear = false;
 	//numero della partita corrente
 	private int matchNumber = 1;
 	//finestra di selezione delle figure preimpostate
 	private SelectionWindow select;
 	//menu di gioco
 	private Widget menu;
 	//l'oggetto che si occupa di disegnare le figure selezionate dall'utente
 	public static DrawFigure drawer;
 	//numero di thread, 5 di default
 	private static int numOfThreads = 5;
 
 	/**
 	 * Costruttore della classe Field
 	 */
 	public Field(){
 		super("The Game Of Life");
 		//metodo che specifica l'azione da intraprendere in caso di chiusura
 		actionOnExit();
 		
 		this.getContentPane().setBackground(Color.BLACK);
 
 		contentPane.setLayout(null);
 
 		getContentPane().setLayout(null);
 		//oggetto Loader, schermata iniziale
 		Loader load = new Loader();
 		//frame che permette di impostare la dimensione del campo di gioco
 		startingOptions start = new startingOptions();
 		//aspetto che l'utente scelga la dimensione
 		while(!endSelect){
 			sleepFor(100);
 		};
 		//inserisco il numero di threads
 		do{
 			if(numOfThreads <= 0){
 				
 				JOptionPane.showMessageDialog(null, "Non puoi inserire un valore non valido", "Errore", JOptionPane.ERROR_MESSAGE);
 				
 			}
 			
 			numOfThreads =Integer.parseInt(JOptionPane.showInputDialog("Inserisci il numero di threads \n" +
					"ATTENZIONE: per un funzionamento corretto dell'applicazione\n" +
					" necessario scegliere un numero di thread che non superi\n" +
					"il rapporto di 1:1 con il numero di righe della matrice"));
 			
 		}while(numOfThreads <= 0);
 			
 		//metodo per definire le dimensioni del frame
 		setDimension(sizeSelected[0],sizeSelected[1]);
 		//metodo per creare i bottoni per il gioco
 		createButtons(sizeSelected[0],sizeSelected[1]);
 		
 		menu = new Widget();
 
 		select = new SelectionWindow(sizeSelected[0],sizeSelected[0]);
 	
 		drawer = new DrawFigure();
 
 		this.setVisible(true);
 		//metodo per iniziare il gioco
 		start(buttons);
 
 	}
 
 	/**
 	 * Il metodo start fa iniziare il gioco dopo che l'utente ha premuto start
 	 * @param buttons
 	 * la matrice di gioco principale
 	 */
 	private void start(fieldButtons[][] buttons){
 		//thread che lavorano sul campo per il cambio di generazioni
 		Controllers[] c = new Controllers[numOfThreads];
 
 		int dummy = 0;
 		
 		int i = 0;
 
 		int j = 1;
 
 		int num = buttons.length/numOfThreads;
 
 		do{
 			//questa  la fase prima del gioco, durante questa fase vengono controllate diverse
 			//azioni che l'utente pu compiere
 			while(!startTheGame){
 
 				sleepFor(100);
 				//se l'utente vuole pulire il campo
 				if(clear){
 					
 					generations.setBounds(60,150,200,20);
 
 					generations.setText("STO PULENDO IL CAMPO");
 
 					sleepFor(200);
 
 					generations.setBounds(65,150,200,20);
 
 					generations.setText("RICOMINCIO DA 0");
 					//uccido i bottoni in modo da avere un campo pulito
 					for(fieldButtons[] b : buttons){
 
 						for(fieldButtons butt : b){
 
 							butt.changeToWhite();
 
 							butt.isInGame(false);
 						}
 
 					}
 
 					sleepFor(400);
 
 					generations.setBounds(35,150,200,20);
 
 					generations.setText("CAMPO OK, PARTITA NUMERO " + matchNumber);
 
 					sleepFor(700);
 
 					generationNumber = 0;
 
 					generations.setBounds(70,150,200,20);
 
 					generations.setText("GENERAZIONE #");
 
 					clear = false;
 				}
 
 			}
 
 			setButtons();
 
 			isEmpty = true;
 
 			while(pause){
 
 					sleepFor(100);
 					
 			}
 			//azzero la generazione successiva
 			for(fieldButtons[] b : nextGen){
 
 				for(fieldButtons butt : b){
 
 					butt.changeToWhite();
 
 				}
 
 			}
 
 			i = 0;
 
 			j = 1;
 
 			for(int x = 0; x < c.length; x++){
 				//assegno a ogni thread il suo spazio di lavoro
				if(++dummy == numOfThreads){
 					//se sono l'ultima thread mi prendo l'ultimo pezzo di matrice
 					c[x] = new Controllers(i*num,buttons.length - 1);
 				}else{
 					//altrimenti mi prendo il mio pezzo in proporzione al numero di thread
 					c[x] = new Controllers(i*num,(j*num) - 1);
 				}
 
 				i++;
 
 				j++;
 
 			}

 			for(Controllers co : c){
 				
 				co.run();
 
 				try {
 					//aspetto che abbiano finito
 					co.join();
 
 				} catch (InterruptedException e) {
 
 					// TODO Auto-generated catch block
 
 					e.printStackTrace();
 
 				}
 
 			}
 			//aggiorno la matrice buttons in modo che l'utente riesca a vedere la prossima generazione
 			for(i = 0; i < buttons.length; i++){
 
 				for(j = 0; j < buttons[0].length; j++){
 
 					if(nextGen[i][j].isBlack()){
 
 						buttons[i][j].changeToBlack();
 						//se anche solo una cellula  viva allora non devo fare il refresh automatico
 						isEmpty = false;
 						
 					}else{
 
 						buttons[i][j].changeToWhite();
 
 					}
 
 				}
 
 			}
 			//incremento la generazione attuale
 			generationNumber++;
 
 			generations.setText("GENERAZIONE #" + generationNumber);
 			//lo faccio dormire 
 			sleepFor(velocity[gameSpeed - 1]);
 			//se il campo  vuoto significa che non ci sono pi cellule vive
 			//quindi il programma deve pulire in automatico il campo
 			if(isEmpty){
 				
 				generations.setBounds(50,150,200,20);
 
 				generations.setText("GENERAZIONI TERMINATE");
 
 				startTheGame = false;
 
 				generations.setBounds(65,150,200,20);
 
 				generations.setText("RICOMINCIO DA 0");
 				//incremento il numero della partita attuale
 				matchNumber++;
 
 				sleepFor(600);
 				//faccio capire ai bottoni che non sono pi in gioco
 				for(i = 0; i < buttons.length; i++){
 
 					for(j = 0; j < buttons[0].length; j++){
 
 						buttons[i][j].isInGame(false);
 
 					}
 
 				}
 
 				generations.setBounds(35,150,200,20);
 
 				generations.setText("CAMPO OK, PARTITA NUMERO " + matchNumber);
 
 				sleepFor(700);
 
 				generationNumber = 0;
 
 				sleepFor(2000);
 
 				generations.setBounds(70,150,200,20);
 
 				generations.setText("GENERAZIONE #");
 
 			}
 			//se l'utente ha deciso di pulire il campo
 			if(clear){
 
 				generations.setBounds(60,150,200,20);
 
 				generations.setText("STO PULENDO IL CAMPO");
 
 				sleepFor(200);
 
 				generations.setBounds(65,150,200,20);
 
 				generations.setText("RICOMINCIO DA 0");
 
 				matchNumber++;
 
 				sleepFor(600);
 
 				for(fieldButtons[] b : buttons){
 
 					for(fieldButtons butt : b){
 
 						butt.changeToWhite();
 
 						butt.isInGame(false);
 
 					}
 
 				}
 
 				generations.setBounds(35,150,200,20);
 
 				generations.setText("CAMPO OK, PARTITA NUMERO " + matchNumber);
 
 				sleepFor(700);
 
 				generationNumber = 0;
 
 				generations.setBounds(70,150,200,20);
 
 				generations.setText("GENERAZIONE #");
 
 				clear = false;
 				//fermo il gioco
 				startTheGame=false;
 
 			}
 
 		}while(true);
 
 	}
 	/**
 	 * Il metodo setButtons() fa capire ai bottoni che sono in gioco, in questo modo se vengono cliccati
 	 * sanno che vengono uccisi
 	 */
 
 	private void setButtons(){
 
 		for(fieldButtons[] rowButtons : buttons){
 
 			for(fieldButtons p : rowButtons){
 
 				p.isInGame(true);
 
 			}
 
 		}
 
 	}
 	/**
 	 * Il metodo sleepFor mette in attesa la thread che lo chiama
 	 * <p>
 	 */
 	public void sleepFor(int milliseconds) {
 
 		try {
 
 			Thread.sleep(milliseconds);
 
 		} catch (InterruptedException e) {}
 
 	}
 	/**
 	 * Il metodo createButtons inizializza le matrici utili per il gioco e posiziona le principali
 	 * nel campo di gioco.
 	 * <p>
 	 * 
 	 * @param x il numero di bottoni sull'ascissa
 	 * @param y il numero di bottoni sull'ordinata
 	 */
 	private void createButtons(int x, int y){
 		//numero di bottoni orrizzontali
 		int xButtons = (int)x/10;							
 		//numero di bottoni verticali
 		int yButtons = (int)y/10;							
 
 		int i=0;
 
 		int j=0;
 
 
 		buttons = new fieldButtons[xButtons][yButtons];
 
 		nextGen = new fieldButtons[xButtons][yButtons];
 
 		screenShot = new fieldButtons[xButtons][yButtons];
 
 		for(i = 0; i < xButtons; i++){
 
 			for(j = 0; j < yButtons; j++){
 
 				buttons[i][j] = new fieldButtons();
 
 				nextGen[i][j] = new fieldButtons();
 
 				screenShot[i][j] = new fieldButtons();
 				//posiziono i bottoni nel campo
 				buttons[i][j].posizionaIn(i,j);
 
 				contentPane.add(buttons[i][j]);
 
 			}
 
 		}
 
 	}
 
 	/**
 	 * Il metodo actionOnExit definisce il comportamento del frame alla pressione del tasto di chiusura
 	 */
 
 	private void actionOnExit(){
 		//Field  l'unico che esce se premiamo sul tasto x, mentre gli altri frame non si chiudono mai
 		//se non nel caso in cui chiudiamo Field
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 	}
 
 	/**
 	 * Il metodo setDimension,dati 2 parametri in ingresso, stabilisce la grandezza del frame
 	 * e la sua locazione sullo schermo
 	 * <p>
 	 * @param x la dimensione orrizzontale
 	 * @param y la dimensione verticale
 	 */
 	private void setDimension(int x, int y){
 		//coordinate per spostare le finestre al centro dello schermo
 		int xLocation = ((int)screen.getWidth()/2) - (x/2);	
 
 		int yLocation = ((int)screen.getHeight()/2) - (y/2);
 
 		this.setSize(x, y + 22);
 
 		this.setResizable(false);							//non  modificabile
 
 		setLocation(xLocation,yLocation);
 
 	}
 
 	/**
 	 * <p>
 	 * Questa classe permette di scegliere la dimensione del campo di gioco tra 3 dimensioni possibili:
 	 * <p>
 	 * 250x250
 	 * <p>
 	 * 500x500
 	 * <p>
 	 * 750x750
 	 *@author Nicola Castellani
 	 *@author Pietro Musoni
 	 *@param xLocation 
 	 *locazione sulla ascissa dello schermo
 	 *@param yLocation 
 	 *locazione dull'ordinata dello schermo
 	 */
 	private class startingOptions extends JFrame{
 		/**
 		 * Costruttore della classe startingOptions
 		 */
 		private startingOptions(){
 
 			super("The Game Of Life");
 
 			int xLocation = ((int)screen.getWidth()/2) - 100; 
 
 			int yLocation = ((int)screen.getHeight()/2) - 100;
 
 			JButton bottoneOK = new JButton("OK");
 
 			JRadioButton r1 = new JRadioButton("250x250");
 
 			JRadioButton r2 = new JRadioButton("500x500");
 
 			JRadioButton r3 = new JRadioButton("750x750");
 
 			ActionListener listener = new JRadioButtonListener();
 
 			r1.addActionListener(listener);
 
 			r2.addActionListener(listener);
 
 			r2.addActionListener(listener);
 
 			setBounds(100,200,400,400);
 
 			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 			setLocation(xLocation,yLocation);
 
 			Container contentPane = getContentPane();
 
 			contentPane.setLayout(new BorderLayout());
 
 			JLabel intro = new JLabel("SELEZIONA UNA DIMENSIONE");
 
 			intro.setForeground(Color.black);
 
 			contentPane.add(intro, BorderLayout.NORTH);
 
 			contentPane.add(r1,BorderLayout.WEST);
 
 			contentPane.add(r2,BorderLayout.CENTER);
 
 			contentPane.add(r3,BorderLayout.EAST);
 
 			contentPane.add(bottoneOK,BorderLayout.SOUTH);
 
 
 			//aggiungo un listener al bottone cosi quando clicco registro l'azione
 			bottoneOK.addActionListener(new ActionListener() {
 
 				public void actionPerformed(ActionEvent arg0) {
 
 					endSelect = true;
 				}
 
 			});
 
 			setResizable(false);
 
 			pack();
 
 			this.setVisible(true);
 
 			while(!endSelect){//finch non premo il bottone
 
 				if(radioButtonClicked){//se un radioButton  stato spuntato
 
 					if(r1.isSelected() ){
 
 						r1.setSelected(true);
 
 						r2.setSelected(false);
 
 						r3.setSelected(false);
 
 					}
 
 					if(r2.isSelected() ){
 
 						r2.setSelected(true);
 
 						r3.setSelected(false);
 
 						r1.setSelected(false);
 
 					}
 
 					if(r3.isSelected() ){
 
 						r3.setSelected(true);
 
 						r1.setSelected(false);
 
 						r2.setSelected(false);
 
 					}
 
 				}
 
 			};
 
 			if(r1.isSelected() ){
 
 				sizeSelected[0]=250;
 
 				sizeSelected[1]=250;
 
 			}else if(r2.isSelected() ){
 
 				sizeSelected[0]=500;
 
 				sizeSelected[1]=500;
 
 			}else if(r3.isSelected()){
 
 				sizeSelected[0]=750;
 
 				sizeSelected[1]=750;
 
 			}else{
 				//dimensioni de default
 				sizeSelected[0]=500;
 
 				sizeSelected[1]=500;
 
 			}
 
 			this.setVisible(false);
 
 			endSelect = true;
 			//chiudo il frame e dealloco le risorse che utilizzava
 			this.dispose();	
 
 		}
 
 	}
 	/**
 	 * 
 	 * <p>
 	 * La classe Controllers definisce le thread che lavorano sul campo di gioco. In particolare queste
 	 * thread lavorano su uno spazio limitato del gioco in modo che lavorino in contemporanea e non in maniera concorrente.
 	 * <p>
 	 * @author Nicola Castellani
 	 * @author Pietro Musoni
 	 */
 	private class Controllers extends Thread{
 
 		private int start, end;
 		/**
 		 * Costruttore della classe Controllers, come input riceve la riga iniziale e la riga finale per
 		 * ritagliarsi il suo spazio di lavoro nella matrice principale
 		 * 
 		 * @param start 
 		 * la riga dove inizia a controllare
 		 * @param end 
 		 * la riga dove finisce di controllare
 		 */
 		public Controllers(int start, int end){
 
 			this.start = start;
 
 			this.end = end;
 
 		}
 
 		/**
 		 * Il metodo run consiste nella scansione di ogni bottone dello spazio assegnato al controllore.
 		 * <p>
 		 * Una volta controllato il numero di cellule vive attorno a quella che si sta studiando determina
 		 * se quest'ultima dovr vivere o morire nella prossima generazione
 		 */
 		public void run(){
 
 			int temp = 0;
 
 			for(int i = start; i <= end; i++){
 
 				for(int j = 0; j < buttons[0].length; j++){
 					//controllo quante cellule vive ci sono intorno a quella presa in considerazione
 					temp = check(i,j);
 
 					if(temp == 3 && !buttons[i][j].isBlack()){
 						//nasce
 						nextGen[i][j].changeToBlack();
 
 					}else if(temp !=3 && temp != 2 && buttons[i][j].isBlack()){
 						//morte per soffocamento o per mancanza di un numero sufficiente di cellule vive
 						nextGen[i][j].changeToWhite();
 
 					}else{
 						//rimane lo stesso
 						if(buttons[i][j].isBlack()){
 							
 							nextGen[i][j].changeToBlack();
 
 						}else{
 
 							nextGen[i][j].changeToWhite();
 
 						}
 
 					}
 
 				}
 
 			}
 
 		}
 		/**
 		 * Controlla il numero di cellule vive attorno a una cellula scelta dal controllore
 		 * 
 		 * @param xPos 
 		 * posizione x della cellula nella matrice
 		 * @param 
 		 * yPos posizione y della cellula nella matrice
 		 * @return 
 		 * il numero di cellule vive attorno alla cellula studiata
 		 */
 		public int check(int xPos,int yPos){
 
 			int counter = 0;
 
 			for(int i = -1;i < 2; i++){
 
 				for(int j = -1; j < 2; j++){
 
 					if(i != 0 || j != 0)
 
 						try{
 
 							if(buttons[xPos + i][yPos + j].isBlack()){
 
 								counter++;
 
 							}
 
 						}catch(ArrayIndexOutOfBoundsException e){}
 
 				}
 
 			}
 
 			return counter;
 
 		}
 
 	}
 
 	private class JRadioButtonListener implements ActionListener {
 
 		public void actionPerformed(ActionEvent e) {
 
 			radioButtonClicked = true;
 
 		}
 
 	}
 	/**
 	 * <p>
 	 * La classe Widget permette di creare un frame per la gestione della velocit di gioco
 	 * la sua messa in pausa e la partenza del gioco stesso, la cattura di una generazione 
 	 * e il restart del gioco in esecuzione o la pulizia del campo stesso
 	 * @author Nicola Castellani
 	 * @author Pietro Musoni
 	 * @param xLocation
 	 */
 
 	private class Widget extends JFrame{
 
 		private Widget(){
 
 			super("Strumenti di gioco");
 
 			int xLocation = 0;//in questo modo si trover sempre a fianco del frame
 
 			if(sizeSelected[0] == 250){
 
 				xLocation = ((int)screen.getWidth()/2) - 500;
 
 			}else if(sizeSelected[0] == 500){
 
 				xLocation = ((int)screen.getWidth()/2) - 600;
 
 			}else{
 
 				xLocation = ((int)screen.getWidth()/2) - 700;
 
 			}
 
 			Container contentPane = getContentPane();
 
 			contentPane.setLayout(null);
 
 			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 
 			//posiziono la finestra
 
 			this.setSize(250,500);
 
 			this.setResizable(false);
 
 			this.setLocation(xLocation,200);
 
 			//aggiungo label e scrollbar
 			JLabel l1 = new JLabel("Imposta la velocita'");
 
 			Hashtable<Integer, JLabel> labelTable = new Hashtable<Integer, JLabel>();
 
 			labelTable.put(new Integer(1), new JLabel("1"));
 
 			labelTable.put(new Integer(2), new JLabel("2"));
 
 			labelTable.put(new Integer(3), new JLabel("3"));
 
 			labelTable.put(new Integer(4), new JLabel("4"));
 
 			labelTable.put(new Integer(5), new JLabel("5"));
 
 			labelTable.put(new Integer(6), new JLabel("6"));
 
 			labelTable.put(new Integer(7), new JLabel("7"));
 
 			labelTable.put(new Integer(8), new JLabel("8"));
 
 			labelTable.put(new Integer(9), new JLabel("9"));
 
 			labelTable.put(new Integer(10), new JLabel("10"));
 
 			l1.setBounds(70,50, 200, 25);
 
 			final JLabel selected = new JLabel("Velocita' selezionata: 1");
 
 			selected.setBounds(60,120,200,20);
 
 			JSlider slider = new JSlider(JSlider.HORIZONTAL,1,10,1);
 
 			slider.setBounds(30, 70, 200, 40);
 
 			slider.setPaintTicks(true);
 
 			slider.setMajorTickSpacing(9);
 
 			slider.setMinorTickSpacing(1);
 
 			slider.setLabelTable(labelTable);
 
 			slider.setPaintLabels(true);
 
 			slider.addChangeListener(new ChangeListener() {
 
 				public void stateChanged(ChangeEvent e) {
 
 					JSlider source = (JSlider)e.getSource();
 
 					gameSpeed = source.getValue();
 
 					selected.setText("Velocita' selezionata: "+ gameSpeed);
 
 				}
 
 			});
 
 			generations = new JLabel("GENERAZIONE # ");
 
 			generations.setBounds(70,150,200,20);
 
 			final JButton screenshoot = new JButton("TAKE A SCREENSHOOT");
 
 			screenshoot.setBounds(27,190,200,25);
 
 			screenshoot.addActionListener(new ActionListener() {
 
 				public void actionPerformed(ActionEvent arg0) {
 
 					if(!screenShotSelected){
 
 						screenshoot.setText("REPLACE SCREENSHOOT");
 
 						screenShotSelected = true;
 
 						for(int i = 0; i < buttons.length; i++){
 
 							for(int j = 0; j < buttons[0].length; j++){
 
 								if(buttons[i][j].isBlack()){
 
 									screenShot[i][j].changeToBlack();
 
 								}else{
 
 									screenShot[i][j].changeToWhite();
 
 								}
 
 							}
 
 						}
 
 					}else{
 
 						screenShotSelected = false;
 
 						screenshoot.setText("TAKE A SCREENSHOOT");
 
 						for(int i = 0; i < buttons.length; i++){
 
 							for(int j = 0; j < buttons[0].length; j++){
 
 								if(screenShot[i][j].isBlack()){
 
 									buttons[i][j].changeToBlack();
 
 								}else{
 
 									buttons[i][j].changeToWhite();
 
 								}
 
 							}
 
 						}
 
 					}
 
 				}		
 
 			});
 
 			JButton quit = new JButton("ESCI");
 
 			quit.setBounds(75,410,100,25);
 
 			quit.addActionListener(new ActionListener() {
 
 				public void actionPerformed(ActionEvent arg0) {
 
 					dispose();
 
 					System.exit(0);
 
 				}		
 
 			});
 
 			final JButton pausa = new JButton("PAUSA");
 
 			pausa.setBounds(75,380,100,25);
 
 			pausa.addActionListener(new ActionListener() {
 
 				public void actionPerformed(ActionEvent arg0) {
 
 					if(pause){
 
 						pause = false;
 
 						pausa.setText("PAUSA");
 
 					}else{
 
 						pause = true;
 						
 						pausa.setText("RIPRENDI");
 
 					}
 				}
 
 			});
 
 			//aggiungo il bottone start
 
 			JButton restart = new JButton("RESTART");
 
 			restart.setBounds(75,350,100,25);
 
 			restart.addActionListener(new ActionListener() {
 
 				public void actionPerformed(ActionEvent arg0) {
 					
 					clear = true;
 
 					firstTime=true;
 
 				}
 
 			});
 
 			JButton start = new JButton("START");
 
 			start.setBounds(75,320,100,25);
 
 			start.addActionListener(new ActionListener() {
 
 				public void actionPerformed(ActionEvent arg0) {
 
 					startTheGame = true;
 
 					if(firstTime){			//se  la prima volta che lo premo
 
 						setButtons();
 
 						firstTime=false;
 
 					}
 
 				}
 
 			});
 
 			//aggiungo i componenti
 			contentPane.add(l1);
 
 			contentPane.add(slider);
 
 			contentPane.add(quit);
 
 			contentPane.add(restart);
 
 			contentPane.add(start);
 
 			contentPane.add(pausa);
 
 			contentPane.add(screenshoot);
 
 			contentPane.add(generations);
 
 			contentPane.add(selected);
 
 			this.setVisible(true);
 
 		}
 
 	}
 	/**
 	 * <p>
 	 * La classe Loader  un semplice frame che presenta un immagine del gioco prima di farlo partire
 	 * 
 	 * @author Nicola Castellani
 	 * @author Pietro Musoni
 	 */
 	private class Loader extends JFrame{
 
 		public Loader(){
 
 			contentPane.setLayout(null);
 
 			getContentPane().setLayout(null);
 
 			int xLocation = ((int)screen.getWidth()/2) - (320);	//coordinate per spostare le finestre al centro dello schermo
 
 			int yLocation = ((int)screen.getHeight()/2) - (250);
 
 			setSize(600,420);
 
 			setLocation(xLocation,yLocation);
 
 			setUndecorated(true);
 			//immagine di sfondo
 			setContentPane(new JLabel(new ImageIcon("images/initial2.png")));
 
 			this.setVisible(true);
 
 			sleepFor(5000);
 
 			this.dispose();
 
 		}
 	
 	}
 	
 }
