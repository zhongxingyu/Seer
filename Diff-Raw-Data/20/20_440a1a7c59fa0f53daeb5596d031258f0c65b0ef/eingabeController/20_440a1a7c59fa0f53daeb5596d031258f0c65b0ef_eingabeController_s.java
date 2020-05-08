 package de.runinho.maneger;
 
 import javax.swing.JDialog;
 
 public class eingabeController {
 	public static void clearInfo(){
 		gui.Sonstige = 0;
 		gui.herrenOrden = 0;
 		gui.damenOrden = 0;
 		gui.textPane.setText("");
 		gui.korrektur = false;
 		gui.Name.setText("");
 		gui.ort.setText("");
 		gui.plz.setText("");
 		gui.strasse.setText("");
 		gui.nr.setText("");
 	}
 	public static void setInfo(){
 		gui.massage = "";
 		if(gui.herrenOrden != 0 )
			gui.massage += gui.herrenOrden+"x Herrenorden a "+gui.herrenPreis+" fr "+(gui.herrenOrden*gui.herrenPreis)+""+gui.nl;
 		if(gui.damenOrden != 0 )
			gui.massage += gui.damenOrden+"x Damenorden a "+gui.damenPreis+" fr "+(gui.damenOrden*gui.damenPreis)+""+gui.nl;
 		if(gui.Sonstige != 0)
 			gui.massage += gui.Sonstige+"x Sonstiges "+gui.nl;
 		gui.textPane.setText(gui.massage);
 	}
 	public static void  plusHerren(){
 		if(!gui.korrektur){
 			gui.herrenOrden++;
 		}else
 		{
 			gui.herrenOrden--;
 		}
 		setInfo();
 	}
 	public static void plusDamen(){
 		if(!gui.korrektur){
 			gui.damenOrden++;
 		}else{
 			gui.damenOrden--;
 		}
 		setInfo();
 	}
 	public static void bestatigen(){
 		if(gui.Name.getText().length() != 0){
 			String namestr = gui.Name.getText();
 			String ortstr;
 			String plzstr;
 			String strassestr;
 			String nrstr;
 			if(gui.massage.length() != 0){
 				if(gui.ort.getText().length() != 0){
 					ortstr = gui.ort.getText();
 				}
 				else{
					ortstr = "Kln";
 				}
 				if(gui.plz.getText().length() != 0){
 					plzstr = gui.plz.getText();
 					
 					if(gui.strasse.getText().length() !=0 ){
 						strassestr = gui.strasse.getText();
 						
 						if(gui.nr.getText().length() !=0){
 							nrstr = gui.nr.getText();
 									writer.writeBestellung(gui.massage,namestr, ortstr, plzstr, strassestr, nrstr);
 									infoController.updateInfo();
 									clearInfo();
 						}
 						else{
 							try {
								errorGui dialog1 = new errorGui("Das Feld NR wurde nicht" + gui.nl + "ausgefllt sommit ist ihre" + gui.nl + "eingabe ungltig. Bitte" + gui.nl + "berprfen sie Ihre eingabe!");
 								dialog1.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 								dialog1.setVisible(true);
 								return;
 							} catch (Exception e) {
 								e.printStackTrace();
 							}
 						} 
 					}else{
 						try {
							errorGui dialog1 = new errorGui("Das Feld STRAE  wurde nicht" + gui.nl + "ausgefllt sommit ist ihre" + gui.nl + "eingabe ungltig. Bitte" + gui.nl + "berprfen sie Ihre eingabe!");
 							dialog1.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 							dialog1.setVisible(true);
 							return;
 						} catch (Exception e) {
 							e.printStackTrace();
 						}
 					}
 					
 				}else{
 					try {
						errorGui dialog1 = new errorGui("Das Feld PLZ  wurde nicht" + gui.nl + "ausgefllt sommit ist ihre" + gui.nl + "eingabe ungltig. Bitte" + gui.nl + "berprfen sie Ihre eingabe!");
 						dialog1.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 						dialog1.setVisible(true);
 						return;
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 				}
 			}else{
 				try {
					errorGui dialog1 = new errorGui("Sie haben noch garkeine" + gui.nl + "Artikel zum Warenkorb" + gui.nl + "hinzugefgt." + gui.nl + "ihre Bestellung wurde" + gui.nl + "abgebrochen!");
 					dialog1.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 					dialog1.setVisible(true);
 					return;
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		else{
 			if(gui.massage.length() != 0){
 				writer.writeBestellung(gui.massage);
 				infoController.updateInfo();
 				clearInfo();
 			}
 		}
 	}
 }
