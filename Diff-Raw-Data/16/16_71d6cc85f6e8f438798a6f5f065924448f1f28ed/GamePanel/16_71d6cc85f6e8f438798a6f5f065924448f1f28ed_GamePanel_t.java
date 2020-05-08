 package de.dhbw.muehle.gui.menus;
 
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Image;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 import de.dhbw.muehle.gui.View;
 import de.dhbw.muehle.gui.ViewController;
 import de.dhbw.muehle.gui.viewactions.GamePanelVA;
 import de.dhbw.muehle.model.spielstein.EPositionIndex;
 import de.dhbw.muehle.model.spielstein.Position;
 import de.dhbw.muehle.model.theme.Theme;
 
 public class GamePanel extends Menu {
 	
 	private JPanel gameField;
 	private LblGameStone lblGameStone[][][];
 	public LblStoneStack schwarzeSteine,
 				   		 weisseSteine;
 	
 	private GamePanelVA vActions;
 	
 	private View view;
 	
 	
 	public GamePanel(ViewController vController, View view) {
 		super(view);
 		this.view = view;
 		
 		// Listener initialisieren
 		vActions = new GamePanelVA(vController);
 		
 		
 		setLayout(new FormLayout(new ColumnSpec[] {
 				ColumnSpec.decode("1dlu:grow(6)"),
 				ColumnSpec.decode("1dlu:grow"),},
 			new RowSpec[] {
 				RowSpec.decode("default:grow"),}));
 		
 		gameField = new JPanel();
 		gameField.setOpaque(false);
 		add(gameField, "1, 1, fill, fill");
 		gameField.setLayout(new FormLayout(new ColumnSpec[] {
 				ColumnSpec.decode("default:grow"),
 				ColumnSpec.decode("default:grow"),
 				ColumnSpec.decode("default:grow"),
 				ColumnSpec.decode("default:grow"),
 				ColumnSpec.decode("default:grow"),
 				ColumnSpec.decode("default:grow"),
 				ColumnSpec.decode("default:grow"),},
 			new RowSpec[] {
 				RowSpec.decode("default:grow"),
 				RowSpec.decode("default:grow"),
 				RowSpec.decode("default:grow"),
 				RowSpec.decode("default:grow"),
 				RowSpec.decode("default:grow"),
 				RowSpec.decode("default:grow"),
 				RowSpec.decode("default:grow"),}));
 		
 		JPanel stoneField = new JPanel();
 		stoneField.setOpaque(false);
 		add(stoneField, "2, 1, fill, fill");
 		stoneField.setLayout(new FormLayout(new ColumnSpec[] {
 				ColumnSpec.decode("default:grow"),},
 			new RowSpec[] {
 				RowSpec.decode("default:grow"),
 				RowSpec.decode("default:grow(6)"),
 				RowSpec.decode("default:grow(6)"),
 				RowSpec.decode("default:grow"),}));
 		
 		weisseSteine = new LblStoneStack(view, "weiss", 9);
 		stoneField.add(weisseSteine, "1, 2, fill, fill");
 		
 		schwarzeSteine = new LblStoneStack(view, "schwarz", 9);
 		stoneField.add(schwarzeSteine, "1, 3, fill, fill");
 		
 		
 		
 		// Spielsteine initialisieren
 		lblGameStone = new LblGameStone[3][3][3];
 		
 		// Labels für die Spielsteine einfügen
 		generateJLabels();
 	}
 	
 	
 	/**
 	 * Labels für Spielsteine erzeugen und auf dem Spielbrett einfügen
 	 */
 	private void generateJLabels(){
 		int i = 3; // Hilfsvariable
 		for(int e=0;e<3;e++){
 			for(int x=0;x<3;x++){
 				for(int y=0;y<3;y++){
 					if(! (x==1 && y==1)){
 						lblGameStone[e][x][y] = new LblGameStone(e+1, x+1, y+1, vActions);
 						gameField.add(lblGameStone[e][x][y], (x*i+e+1)+", "+(y*i+e+1)+", fill, fill");
 					}
 				}
 			}
 			i--;
 		}
 	}
 	
 	
 	/**
 	 * Liefert das Label auf dem Spielstein anhand der angegbenen Position
 	 * @param pos Position
 	 */
 	public JLabel getLabel(Position pos){
 		return lblGameStone[pos.getEbene().getValue()-1][pos.getX().getValue()-1][pos.getY().getValue()-1];
 	}
 	
 	
 	/**
 	 * Prüft, ob Stack leer ist
 	 * @param stach Zu überprüfender Stack
 	 * @return true/false
 	 */
 	public boolean isStackEmpty(LblStoneStack stack){
 		if(stack.getCountStones() == 0)
 			return true;
 		else
 			return false;
 	}
 	
 	
 	/**
 	 * Erhöht oder veringert einen Stack
 	 * @param stack Zu ändernder Stack
 	 * @param change Nur 1 und -1 erlaubt
 	 */
 	public void updateStack(LblStoneStack stack, int change){
 		if(change >= 1)
 			stack.setCountStones(stack.getCountStones() + 1);
 		else
 			stack.setCountStones(stack.getCountStones() - 1);
 	}
 	
 	
 	
 	/**
 	 * Spielbrettbild dynamisch auf das GamePanel zeichnen
 	 * @param g Graphics
 	 */
     @Override
     public void paintComponent(Graphics g) {
     	// Hintergrundbild dynamisch zeichnen
         g.drawImage(view.getTheme().getSpielBrett(), 0, 0, getWidth(), getHeight(), this);
     }
 
 
 
 
 
 	public class LblGameStone extends JLabel{
 		
 		private Image image;
 		private Position pos;
 		
 		public LblGameStone(int ebene, int x, int y, GamePanelVA vActions) {	
 			pos = new Position(ebene, x, y);
 			
 			setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 			addMouseListener(vActions.new lblGameStoneMouse());
 		}
 		
 		
 		/**
 		 * Liefert die Position des Labels auf dem Spielbrett
 		 * @return Position
 		 */
 		public Position getPosition(){
 			return pos;
 		}
 		
 		
 		/**
 		 * Weist dem Stein ein bestimmtes Bild zu
 		 * <br/>Dadurch wird der Stein sichtbar
 		 * @param image Bild, das auf dem Label gezeichnet werden soll
 		 */
 		public void setImage(Image image){
 			this.image = image;
 			repaint();
 		}
 		
 		
 		/**
 		 * Entfernt das aktuelle Bild vom Label
 		 * <br/>Dadurch wird es unsichtbar
 		 */
 		public void removeImage(){
 			this.image = null;
 			repaint();
 		}
 		
 		
 		/**
 		 * Bild dynamisch auf den Stein zeichnen
 		 */
 		@Override
 		public void paintComponent(Graphics g) {
 			// Bild dynamisch zeichnen
 			g.drawImage(image, 0, 0, getWidth(), getHeight(), this);
 		}
 	}
 	
 	
 	public class LblStoneStack extends JLabel{
 		
 		private Image gameStoneImage;
 		private int remainingStones; // Anzahl der noch nicht gesetzten Spielsteine
 		private String type;
 		
 		private View view;
 		
 		
 		public LblStoneStack() {
 			this(null, null, 0);
 		}
 		
 		public LblStoneStack(View view, String type, int remainingStones) {
 			this.view = view;
 			this.type = type;
 			setImage(type);
 			setCountStones(remainingStones);
 		}
 		
 		
 		/**
 		 * Die Farbe für den Spielsteinstapel setzen
 		 * @param type Typ des Spielsteins ("schwarz" oder "weiß")
 		 */
 		public void setImage(String type){
 			if(type.equals("weiss"))
 				gameStoneImage = view.getTheme().getSpielSteinWeiss();
 			else if(type.equals("schwarz"))
 				gameStoneImage = view.getTheme().getSpielSteinSchwarz();
 			repaint();
 		}
 		
 		
 		/**
 		 * Setzt die Anzahl der noch nicht gesetzten Spielsteine
 		 * @param count Anzehl der noch nicht gestzten Spielsteine
 		 */
 		public void setCountStones(int count){
 			this.remainingStones = count;
 			repaint(); // Zeichnung updaten
 		}
 		
 		
 		/**
 		 * Liefert die Anzahl der noch nicht gesetzten Spielsteine
 		 * @return count Anzehl der noch nicht gestzten Spielsteine
 		 */
 		public int getCountStones(){
 			return remainingStones;
 		}
 		
 		
 		/**
 		 * Stapel an noch nicht gesetzten Steinen zeichnen
 		 */
 		@Override
 		public void paintComponent(Graphics g) {
 			// Bild auf aktuelle Theme aktualisieren
 			setImage(type);
 			
 			// Höhe und Breite berechnen
 			int h = getHeight()/4;
 			int w = (int) (getWidth()*0.6);
 			// Bild dynamisch zeichnen
 			for(int i=0;i<remainingStones;i++){
 				if(i>=4 && i<7){
 					g.drawImage(gameStoneImage, getWidth()*15/100, ((i-4)*h)+h/2, w, h, this);
 					continue;
 				}
 				
 				if(i>=7){
 					g.drawImage(gameStoneImage, getWidth()*15/100, ((i-7)*h)+h/2+h/2, w, h, this);
 					continue;
 				}
 				
 				g.drawImage(gameStoneImage, getWidth()*15/100, i*h, w, h, this);
 			}
 		}
 	}
 }
