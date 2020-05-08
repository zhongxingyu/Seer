 package it.univr.GameOfLife;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 
 public class fieldButtons extends JButton{
 	private int riga = 0;
 	private int colonna = 0;
 	private static int DIMENSIONE = 10;
 	private boolean gameOn = false;
 	private boolean black = false;
 	private boolean alreadyChecked = false;
 	static boolean userHasToDraw = false;
 	private boolean isChecked = false;
 	
 	protected fieldButtons(){
 		super("");	
 		setBackground(Color.WHITE);
 		setToolTipText("xPos : " + riga + " yPos : "+ colonna + " died");
 		addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(SelectionWindow.SetFigure)
 					DrawFigure.putFigure(e);					
 				if(getColor() == Color.white && !gameOn){
 					changeToBlack(); //se sono ancora in fase pre gioco lo creo
 					setToolTipText("xPos : " + (riga+1) + " yPos : "+ (colonna + 1) + " alive");
 				}else if(getColor() == Color.black && gameOn){ 
 					changeToWhite(); //lo uccido se sono in gioco
 					setToolTipText("xPos : " + (riga + 1) + " yPos : "+ (colonna + 1) + " died");
 				}else{
 					//nothing to do
 				}
 			}
 		});
 	}
 	public boolean isBlack(){
 		return black;
 	}
 	public int getRow(){
 		return this.riga;
 	}
 	public int getColumn(){
 		return this.colonna;
 	}
 	public boolean Checked(){
 		return this.isChecked;
 	}
 	public void userHasToDraw(boolean b){
 		if(b){
 			this.userHasToDraw = true;
 			this.alreadyChecked = false;
 		}else{
 			this.userHasToDraw = false;
 			this.alreadyChecked = false;
			this.isChecked = true;
 		}
 		
 	}
 	public void isInGame(boolean b){
 		gameOn = b;
 	}
 	public void changeToBlack(){
 		setBackground(Color.black);
 		setToolTipText("xPos : " + riga + " yPos : "+ colonna + " alive");
 		black = true;
 	}
 	public void changeToWhite(){
 		setBackground(Color.white);
 		setToolTipText("xPos : " + riga + " yPos : "+ colonna + " died");
 		black = false;
 	}
 	public Color getColor(){
 		return getBackground();
 	}
 	public final void posizionaIn(int riga, int colonna) {
 		this.riga = riga;
 		this.colonna = colonna;
 
 		setBounds(colonna * DIMENSIONE, riga * DIMENSIONE, DIMENSIONE, DIMENSIONE);
 		changeToWhite();
 	}
 	public void setColor(Color c){
 		if(c == Color.black)
 			this.black = true;
 		this.setBackground(c);
 	}
 
 }
