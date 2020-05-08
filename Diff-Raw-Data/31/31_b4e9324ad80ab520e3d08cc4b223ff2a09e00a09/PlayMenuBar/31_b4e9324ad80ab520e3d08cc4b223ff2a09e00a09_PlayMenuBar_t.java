 package gui;
 
 import java.awt.Color;
 import java.awt.Menu;
 import java.awt.MenuBar;
 import java.awt.MenuItem;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JLayeredPane;
 
 // Klasse enthllt die Menleiste
 
 /**
  * 
  * @author Eren, Fabian
  * @version 0.1
  *
  */
 
 class PlayMenuBar extends MenuBar
 {
 	
 	/**
 	 * 
 	 */
 	private PlayFrame playFrame;
 	
 	
 	/**
 	 * 
 	 * @param PlayFrame frame
 	 * 
 	 */
 	
 	public PlayMenuBar(PlayFrame frame)
 	{
 		Menu m;
 		playFrame = frame;
 		playFrame.getContentPane().setBackground(Color.white);
 		
 		
 		//Datei
 		m = new Menu("Datei");
 		
 		//Datei Neu
 		MenuItem neu = new MenuItem("Neu");
 		neu.addActionListener(new ActionListener(){
 			
 			
 			public void actionPerformed(ActionEvent arg0)
 			{	
				int temp = 0;
				if(PlayFrame._oPlayFrame._oBoard.getCols() == 0 && PlayFrame._oPlayFrame._oBoard.getRows() == 0 || PlayFrame._oPlayFrame._oBoard.isHasChanged() == false){temp = 1;}
				else{
					QuestionToSaveDialog questionToSaveDialog = new QuestionToSaveDialog();

					if(questionToSaveDialog.isYes_no_answer() && questionToSaveDialog.isSave_is_cancel() == false){
						BoardSizeDialog boardSizeDialog = new BoardSizeDialog(playFrame);									
					}
				}
				if(temp == 1){
					BoardSizeDialog boardSizeDialog = new BoardSizeDialog(playFrame);
 				}
 			}
 			
 		});
 		
 		//Datei Laden
 		MenuItem laden = new MenuItem("Laden");
 		laden.addActionListener(new ActionListener(){
 			
 			
 			public void actionPerformed(ActionEvent arg0) 
 			{	
 				LoadDialog loadDialog = new LoadDialog();
 			}
 			
 		});
 		
 		//Datei Speichern
 		MenuItem speichern = new MenuItem("Speichern");
 		speichern.addActionListener(new ActionListener(){
 			
 			
 			public void actionPerformed(ActionEvent arg0) 
 			{
 			}
 			
 		});
 		
 		//Datei Speichern_Unter
 		MenuItem speichern_unter = new MenuItem("Speichern Unter");
 		speichern_unter.addActionListener(new ActionListener(){
 			
 			
 			public void actionPerformed(ActionEvent arg0) 
 			{	
 				SaveDialog saveDialog = new SaveDialog();
 			}
 			
 		});
 		
 		//Datei Beenden
 		MenuItem beenden = new MenuItem("Beenden");
 		beenden.addActionListener(new ActionListener(){
 			
 			
 			public void actionPerformed(ActionEvent arg0) 
 			{	
 				QuestionToSaveDialog questionToSaveDialog = new QuestionToSaveDialog();
 				if(questionToSaveDialog.isYes_no_answer()){
 					playFrame.dispose();
 				}
 			}
 			
 		});
 		
 		// Schaltflchen hinzufgen
 		if(frame._oBoard instanceof GuiElementEditorBoard){
 			m.add(neu);
 		}
 		m.add(laden);
 		m.addSeparator();
 		m.add(speichern);
 		m.add(speichern_unter);
 		m.addSeparator();
 		m.add(beenden);
 		add(m);
 		
 		//Bearbeiten
 		m = new Menu("Bearbeiten");
 				
 		//Bearbeiten Marker setzen
 		MenuItem marker_setzen = new MenuItem("Marker setzen");
 		marker_setzen.addActionListener(new ActionListener(){
 					
 			
 			public void actionPerformed(ActionEvent arg0) 
 			{	
 			}
 					
 		});
 				
 		//Bearbeiten Zurck zum Marker
 		MenuItem zurck_zum_marker = new MenuItem("Zurck zum Marker");
 		zurck_zum_marker.addActionListener(new ActionListener(){
 					
 			
 			public void actionPerformed(ActionEvent arg0) 
 			{	
 			}
 					
 		});
 				
 		//Bearbeiten Rckgnging - Zum ersten Fehler
 		MenuItem rckgngig_zum_ersten_fehler = new MenuItem("Rckgngig zum ersten Fehler");
 		rckgngig_zum_ersten_fehler.addActionListener(new ActionListener(){
 			
 			
 			public void actionPerformed(ActionEvent arg0) 
 			{	
 			}
 			
 		});
 		
 		// Schaltflchen hinzufgen
 		m.add(marker_setzen);
 		m.add(zurck_zum_marker);
 		m.addSeparator();
 		m.add(rckgngig_zum_ersten_fehler);
 		add(m);
 		
 		//Ansicht
 		m = new Menu("Ansicht");
 		
 		//Ansicht Spielmodus
 		MenuItem spielmodus = new MenuItem("Spielmodus");
 		spielmodus.addActionListener(new ActionListener(){
 			
 			
 			public void actionPerformed(ActionEvent arg0) 
 			{
             	QuestionToSaveDialog questionToSaveDialog = new QuestionToSaveDialog();
             	
             	if(questionToSaveDialog.isYes_no_answer() && questionToSaveDialog.isSave_is_cancel() == false){
             		playFrame.drawGameBoard(0,0);          	
             	}
 			}	
 			
 		});
 				
 		//Ansicht Editiermodus
 		MenuItem editiermodus = new MenuItem("Editiermodus");
 		editiermodus.addActionListener(new ActionListener(){
 			
 			
 			public void actionPerformed(ActionEvent arg0) 
 			{
             	QuestionToSaveDialog questionToSaveDialog = new QuestionToSaveDialog();
             	
             	System.out.println(questionToSaveDialog.isSave_is_cancel());
             	
             	if(questionToSaveDialog.isYes_no_answer() && questionToSaveDialog.isSave_is_cancel() == false){
             		playFrame.drawEditorBoard(0,0);          	
             	}
 			}	
 			
 		});
 		
 		// Schaltflchen hinzufgen
 		if(frame._oBoard instanceof GuiElementEditorBoard)
 		{
 			m.add(spielmodus);
 		}
 		else
 		{
 			m.add(editiermodus);
 		}
 				
 		add(m);
 		
 		//Hilfe
 		m = new Menu("Hilfe");
 		
 		//Hilfe Spielbeschreibung
 		MenuItem spielbeschreibung = new MenuItem("Spielbeschreibung");
 		spielbeschreibung.addActionListener(new ActionListener(){
 			
 			
 			public void actionPerformed(ActionEvent arg0) 
 			{
 				InstructionDialog instructionDialog = new InstructionDialog();
 			}	
 			
 		});
 		
 		//Hilfe Info
 		MenuItem info = new MenuItem("Info");
 		info.addActionListener(new ActionListener(){
 			
 			
 			public void actionPerformed(ActionEvent a) 
 			{
 				InfoDialog infofenster = new InfoDialog();
 			}	
 			
 		});
 		
 		// Schaltflchen hinzufgen
 		m.add(spielbeschreibung);
 		m.addSeparator();
 		m.add(info);
 		add(m);
 	}
 }
 
