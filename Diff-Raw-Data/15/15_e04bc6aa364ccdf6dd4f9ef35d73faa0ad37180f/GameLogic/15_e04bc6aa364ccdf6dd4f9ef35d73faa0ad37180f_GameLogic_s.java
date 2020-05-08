 package Dama;
 
 import javax.swing.JOptionPane;
 
 public class GameLogic {
     
     public static Scacchiera Board;
 
     private static Giocatore player1;
     private static Giocatore player2;
 
     private static Giocatore activePlayer;
     
     private static Casella Casellaorigin,Casellatarget,Casellaswap1,Casellaswap2,Casellaswap3,Casellaswap4;
     private static int RilevaPedinaMove,RilevaPedinaEat,RilevaPedinaTearget; //impone la visione della mossa relativa alla posizione del relativo player con la scacchiera 
     private static Pedina Target;
     
     public GameLogic(Giocatore pPlayer1, Giocatore pPlayer2) {
         player1 = pPlayer1;
         player2 = pPlayer2;
         
         //definiamo la scacchiera e disponiamo le pedine dei due giocatori
         Board = new Scacchiera();
         Board.initScacchiera(player1,player2);
         
         //inizia giocatore 1 con i bianchi
         activePlayer = player1;
         
         Casellaorigin = new Casella(Scacchiera.Colori.NERO);
         Casellatarget = new Casella(Scacchiera.Colori.NERO);
         Casellaswap1 = new Casella(Scacchiera.Colori.NERO);
         Casellaswap2 = new Casella(Scacchiera.Colori.NERO);
         Casellaswap3 = new Casella(Scacchiera.Colori.NERO);
         Casellaswap4 = new Casella(Scacchiera.Colori.NERO);
         
         Target = new Pedina(Pedina.Colori.NULL);
         
     }
     
     //Verifica quali mosse sono ammissibili nel movimento di una pedina
     public static void ifPedinaCanMove(Casella pCasellaSelect){
         //nel caso in cui premo una casella con pedina nera/bianca
         if(!pCasellaSelect.getPedina().getColore().equals(Pedina.Colori.NULL)){
             //Se ho selezionato una pedina del player active
             if(pCasellaSelect.getPedina().getColore().equals(getActivePlayer().getPedina().getColore())){
                     if(getActivePlayer().getPedina().getColore().equals(Pedina.Colori.BIANCO)){
                         RilevaPedinaMove = -1;
                     } else {
                         RilevaPedinaMove = 1;
                     }
                 //Controlla se sono arrivato in fondo alla scacchiera
                 if((pCasellaSelect.getPedina().getColore().equals(Pedina.Colori.BIANCO) && pCasellaSelect.getRiga()!=0) ||
                         (pCasellaSelect.getPedina().getColore().equals(Pedina.Colori.NERO) && pCasellaSelect.getRiga()!=Board.scacchiera[0].length-1) ){
                     //Controlla se ho selezionato l'ultima pedina a sinistra
                     if(pCasellaSelect.getColonna()==0){
                        if(Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+RilevaPedinaMove].getPedina().getColore().equals(Pedina.Colori.NULL)){
                             //evidenzia le mosse ammissibili
                             Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+RilevaPedinaMove].setContentAreaFilled(true);
                                 //oltre ad evidenziarla deve salvarla in memoria per poter accertare la previsione di una possibile mossa
                                 Casellaorigin = pCasellaSelect;
                                 Casellaswap1 = Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+RilevaPedinaMove];
                         }
                     //Controlla se ho selezionato l'ultima pedina a destra
                     } else if(Board.scacchiera[0].length-1==pCasellaSelect.getColonna()){
                        if(Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+RilevaPedinaMove].getPedina().getColore().equals(Pedina.Colori.NULL)){
                             Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+RilevaPedinaMove].setContentAreaFilled(true);
                                 Casellaorigin = pCasellaSelect;
                                 Casellaswap1 = Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+RilevaPedinaMove];
                         }
                     } else {
                        if(Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+RilevaPedinaMove].getPedina().getColore().equals(Pedina.Colori.NULL)){
                             Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+RilevaPedinaMove].setContentAreaFilled(true);
                                 Casellaorigin = pCasellaSelect;
                                 Casellaswap1 = Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+RilevaPedinaMove];
 
                         }
                        if(Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+RilevaPedinaMove].getPedina().getColore().equals(Pedina.Colori.NULL)){
                             Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+RilevaPedinaMove].setContentAreaFilled(true);
                                 Casellaorigin = pCasellaSelect;
                                 Casellaswap2 = Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+RilevaPedinaMove];
                         }
                 }//
             }
             //Se ho selezionato una pedina avversaria
             } else {
                 JOptionPane.showMessageDialog(null, "Tocca al giocatore con la pedina " + getActivePlayer().getPedina().getColore());
             }
         } else {
             //nel caso ho riselezionato la stessa casella
             if(Casellaorigin.getPedina().getColore().equals(Pedina.Colori.NULL)){
             //nel caso in cui sto schiacciando una casella che ho previsto per lo spostamento DI UNA CASELLA
             }else if(pCasellaSelect.getPedina().equals(Casellaswap1.getPedina()) && Casellaorigin.getRiga()+RilevaPedinaMove==Casellaswap1.getRiga()){
                 //sposto la pedina nella casella successiva
                 Board.scacchiera[Casellaswap1.getColonna()][Casellaswap1.getRiga()].
                         setPedina(Board.scacchiera[Casellaorigin.getColonna()][Casellaorigin.getRiga()].getPedina());
                 //cancella la precedente
                 Board.scacchiera[Casellaorigin.getColonna()][Casellaorigin.getRiga()].
                         setPedina(new Pedina(Pedina.Colori.NULL));
                 //smemorizza la pedina origine
                 Casellaorigin = new Casella(Scacchiera.Colori.NERO);
                 //deseleziona l'altra possibilita' di movimento
                 Board.scacchiera[Casellaswap2.getColonna()][Casellaswap2.getRiga()].setContentAreaFilled(false);
                 
                 //controlla se si puo fare dama
                 CanBeDama(Board.scacchiera[Casellaswap1.getColonna()][Casellaswap1.getRiga()]);
                 
                 //Scambio turni tra giocatori
                 CambiaTurno();
                 
             } else if (pCasellaSelect.getPedina().equals(Casellaswap2.getPedina()) && Casellaorigin.getRiga()+RilevaPedinaMove==Casellaswap2.getRiga()){
                 //sposto la pedina nella casella successiva
                 Board.scacchiera[Casellaswap2.getColonna()][Casellaswap2.getRiga()].
                         setPedina(Board.scacchiera[Casellaorigin.getColonna()][Casellaorigin.getRiga()].getPedina());
                 
                 //cancella la precedente
                 Board.scacchiera[Casellaorigin.getColonna()][Casellaorigin.getRiga()].
                         setPedina(new Pedina(Pedina.Colori.NULL));
                 //smemorizza la pedina origine
                 Casellaorigin = new Casella(Scacchiera.Colori.NERO);
                 
                 //deseleziona l'altra possibilita' di movimento
                 Board.scacchiera[Casellaswap1.getColonna()][Casellaswap1.getRiga()].setContentAreaFilled(false);
                 
                 //controlla se si puo fare dama
                 CanBeDama(Board.scacchiera[Casellaswap2.getColonna()][Casellaswap2.getRiga()]);
                 
                 //Scambio turni tra giocatori
                 CambiaTurno();
             }
         }
     }
     
     //Controllo se si puo' mangiare con una pedina
     public static void ifPedinaCanEat(Casella pCasellaSelect){
         //nel caso in cui premo una casella con pedina nera/bianca
         if(!pCasellaSelect.getPedina().getColore().equals(Pedina.Colori.NULL)){
             if(getActivePlayer().getPedina().getColore().equals(Pedina.Colori.BIANCO)){
                         RilevaPedinaEat = -2;
                         RilevaPedinaTearget = -1;
                         Target = new Pedina(Pedina.Colori.NERO);
                     } else {
                         RilevaPedinaEat = 2;
                         RilevaPedinaTearget = 1;
                         Target = new Pedina(Pedina.Colori.BIANCO);
                     }
             //Controlla se sono arrivato in fondo (alla penultima) alla scacchiera
             if((pCasellaSelect.getPedina().getColore().equals(Pedina.Colori.BIANCO) && pCasellaSelect.getRiga()!=1) ||
                     (pCasellaSelect.getPedina().getColore().equals(Pedina.Colori.NERO) && pCasellaSelect.getRiga()!=Board.scacchiera[0].length-2) ){
                 //Se ho selezionato una pedina bianca
                 if(pCasellaSelect.getPedina().getColore().equals(getActivePlayer().getPedina().getColore())){
                     //Controlla se ho selezionato l'ultima o la penultima pedina a sinistra
                     if(pCasellaSelect.getColonna()<=1){
                         //Controllo una possibile mangiata
                         if(Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+RilevaPedinaTearget].getPedina().getColore().equals(Target.getColore()) &&
                             Board.scacchiera[pCasellaSelect.getColonna()+2][pCasellaSelect.getRiga()+RilevaPedinaEat].getPedina().getColore().equals(Pedina.Colori.NULL) ){
                                 Board.scacchiera[pCasellaSelect.getColonna()+2][pCasellaSelect.getRiga()+RilevaPedinaEat].setContentAreaFilled(true);
                                     Casellaorigin = pCasellaSelect;
                                     Casellaswap1 = Board.scacchiera[pCasellaSelect.getColonna()+2][pCasellaSelect.getRiga()+RilevaPedinaEat];
                                     Casellatarget = Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+RilevaPedinaTearget];
                         }
                     //Controlla se ho selezionato l'ultima o la penultima pedina a destra
                     }else if(Board.scacchiera[0].length-2==pCasellaSelect.getColonna() || Board.scacchiera[0].length-1==pCasellaSelect.getColonna()){
                         //Controllo una possibile mangiata
                         if(Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+RilevaPedinaTearget].getPedina().getColore().equals(Target.getColore()) &&
                             Board.scacchiera[pCasellaSelect.getColonna()-2][pCasellaSelect.getRiga()+RilevaPedinaEat].getPedina().getColore().equals(Pedina.Colori.NULL) ){
                                 Board.scacchiera[pCasellaSelect.getColonna()-2][pCasellaSelect.getRiga()+RilevaPedinaEat].setContentAreaFilled(true);
                                     Casellaorigin = pCasellaSelect;
                                     Casellaswap1 = Board.scacchiera[pCasellaSelect.getColonna()-2][pCasellaSelect.getRiga()+RilevaPedinaEat];
                                     Casellatarget = Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+RilevaPedinaTearget];
                         }
                     } else {
                         //Controllo una possibile mangiata
                         if(Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+RilevaPedinaTearget].getPedina().getColore().equals(Target.getColore()) &&
                             Board.scacchiera[pCasellaSelect.getColonna()+2][pCasellaSelect.getRiga()+RilevaPedinaEat].getPedina().getColore().equals(Pedina.Colori.NULL) ){
                                 Board.scacchiera[pCasellaSelect.getColonna()+2][pCasellaSelect.getRiga()+RilevaPedinaEat].setContentAreaFilled(true);
                                     Casellaorigin = pCasellaSelect;
                                     Casellaswap1 = Board.scacchiera[pCasellaSelect.getColonna()+2][pCasellaSelect.getRiga()+RilevaPedinaEat];
                                     Casellatarget = Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+RilevaPedinaTearget];
                         }
                         //Controllo una possibile mangiata
                         if(Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+RilevaPedinaTearget].getPedina().getColore().equals(Target.getColore()) &&
                             Board.scacchiera[pCasellaSelect.getColonna()-2][pCasellaSelect.getRiga()+RilevaPedinaEat].getPedina().getColore().equals(Pedina.Colori.NULL) ){
                                 Board.scacchiera[pCasellaSelect.getColonna()-2][pCasellaSelect.getRiga()+RilevaPedinaEat].setContentAreaFilled(true);
                                     Casellaorigin = pCasellaSelect;
                                     Casellaswap2 = Board.scacchiera[pCasellaSelect.getColonna()-2][pCasellaSelect.getRiga()+RilevaPedinaEat];
                                     Casellatarget = Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+RilevaPedinaTearget];
                         }
                     }
                 //nel caso in cui ho cliccato una pedina avversaria
                 } 
             
             }
         } else {
             //nel caso ho riselezionato la stessa casella
             if(Casellaorigin.getPedina().getColore().equals(Pedina.Colori.NULL)){
             //nel caso in cui sto schiacciando una casella che ho previsto per lo spostamento e la mangiata
             }else if(pCasellaSelect.getPedina().equals(Casellaswap1.getPedina()) && 
                     Casellaorigin.getRiga()+RilevaPedinaEat==Casellaswap1.getRiga()){
                 //sposto la pedina nella casella successiva
                 Board.scacchiera[Casellaswap1.getColonna()][Casellaswap1.getRiga()].
                         setPedina(Board.scacchiera[Casellaorigin.getColonna()][Casellaorigin.getRiga()].getPedina());
                 //cancella la precedente
                 Board.scacchiera[Casellaorigin.getColonna()][Casellaorigin.getRiga()].
                         setPedina(new Pedina(Pedina.Colori.NULL));
                 //smemorizza la pedina origine
                 Casellaorigin = new Casella(Scacchiera.Colori.NERO);
                 //Cancella la pedina mangiata
                 Board.scacchiera[Casellatarget.getColonna()][Casellatarget.getRiga()].
                         setPedina(new Pedina(Pedina.Colori.NULL));
                 Casellatarget = new Casella(Scacchiera.Colori.NERO);
                 //deseleziona l'altra possibilita' di movimento
                 Board.scacchiera[Casellaswap2.getColonna()][Casellaswap2.getRiga()].setContentAreaFilled(false);
                 
                 //controlla se si puo fare dama
                 CanBeDama(Board.scacchiera[Casellaswap1.getColonna()][Casellaswap1.getRiga()]);
                 
                 //Scambio turni tra giocatori
                 CambiaTurno();
                 
             } else if (pCasellaSelect.getPedina().equals(Casellaswap2.getPedina()) && 
                     Casellaorigin.getRiga()+RilevaPedinaEat==Casellaswap2.getRiga()){
                 //sposto la pedina nella casella successiva
                 Board.scacchiera[Casellaswap2.getColonna()][Casellaswap2.getRiga()].
                         setPedina(Board.scacchiera[Casellaorigin.getColonna()][Casellaorigin.getRiga()].getPedina());
                 //cancella la precedente
                 Board.scacchiera[Casellaorigin.getColonna()][Casellaorigin.getRiga()].
                         setPedina(new Pedina(Pedina.Colori.NULL));
                 //smemorizza la pedina origine
                 Casellaorigin = new Casella(Scacchiera.Colori.NERO);
                 //Cancella la pedina mangiata
                 Board.scacchiera[Casellatarget.getColonna()][Casellatarget.getRiga()].
                         setPedina(new Pedina(Pedina.Colori.NULL));
                 Casellatarget = new Casella(Scacchiera.Colori.NERO);
                 //deseleziona l'altra possibilita' di movimento
                 Board.scacchiera[Casellaswap1.getColonna()][Casellaswap1.getRiga()].setContentAreaFilled(false);
                 
                 //controlla se si puo fare dama
                 CanBeDama(Board.scacchiera[Casellaswap2.getColonna()][Casellaswap2.getRiga()]);
                 
                 //Scambio turni tra giocatori
                 CambiaTurno();
             }
         }
     }
     
     //Verifica quali mosse sono ammissibili nel movimento di una Dama
     public static void ifDamaCanMove(Casella pCasellaSelect){
         //controlla se ho selezionato una dama
         if(!pCasellaSelect.getDama().getColore().equals(Dama.Colori.NULL)){
             if(pCasellaSelect.getDama().getColore().equals(activePlayer.getDama().getColore())){
                 //controllo se sono sulla parte inferiore della scacchiera
                 if(pCasellaSelect.getRiga()==Board.scacchiera[0].length-1){
                     //sono nello spigolo inferiore destro
                     if(pCasellaSelect.getColonna()==Board.scacchiera.length-1){
                         if(Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()-1].getDama().getColore().equals(Dama.Colori.NULL) && 
                                 Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()-1].getPedina().getColore().equals(Pedina.Colori.NULL)){
                             Casellaorigin = Board.scacchiera[pCasellaSelect.getColonna()][pCasellaSelect.getRiga()];
                             Casellaswap2 = Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()-1];
                             Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()-1].setContentAreaFilled(true);
                         }
                     } else {
                         if(Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()-1].getDama().getColore().equals(Dama.Colori.NULL) && 
                                 Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()-1].getPedina().getColore().equals(Pedina.Colori.NULL)){
                             Casellaorigin = Board.scacchiera[pCasellaSelect.getColonna()][pCasellaSelect.getRiga()];
                             Casellaswap1 = Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()-1];
                             Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()-1].setContentAreaFilled(true);
                         } if(Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()-1].getDama().getColore().equals(Dama.Colori.NULL) && 
                                 Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()-1].getPedina().getColore().equals(Pedina.Colori.NULL)){
                             Casellaorigin = Board.scacchiera[pCasellaSelect.getColonna()][pCasellaSelect.getRiga()];
                             Casellaswap2 = Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()-1];
                             Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()-1].setContentAreaFilled(true);
                         }
                     }
                 //controlla se sono nella parte superiore della scacchiera
                 } else if(pCasellaSelect.getRiga()==0){
                     //sono nello spigolo in alto a sinistra
                     if(pCasellaSelect.getColonna()==0){
                         if(Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+1].getDama().getColore().equals(Dama.Colori.NULL) && 
                                 Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+1].getPedina().getColore().equals(Pedina.Colori.NULL)){
                             Casellaorigin = Board.scacchiera[pCasellaSelect.getColonna()][pCasellaSelect.getRiga()];
                             Casellaswap1 = Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+1];
                             Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+1].setContentAreaFilled(true);
                         }
                     } else {
                         if(Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+1].getDama().getColore().equals(Dama.Colori.NULL) && 
                                 Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+1].getPedina().getColore().equals(Pedina.Colori.NULL)){
                             Casellaorigin = Board.scacchiera[pCasellaSelect.getColonna()][pCasellaSelect.getRiga()];
                             Casellaswap1 = Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+1];
                             Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+1].setContentAreaFilled(true);
                         } if(Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+1].getDama().getColore().equals(Dama.Colori.NULL) && 
                                 Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+1].getPedina().getColore().equals(Pedina.Colori.NULL)){
                             Casellaorigin = Board.scacchiera[pCasellaSelect.getColonna()][pCasellaSelect.getRiga()];
                             Casellaswap2 = Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+1];
                             Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+1].setContentAreaFilled(true);
 
                         }
                     }
                 //se sono troppo a sinistra
                 }else if(pCasellaSelect.getColonna()==0){
                     if(Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+1].getDama().getColore().equals(Dama.Colori.NULL) && 
                             Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+1].getPedina().getColore().equals(Pedina.Colori.NULL)){
                         Casellaorigin = Board.scacchiera[pCasellaSelect.getColonna()][pCasellaSelect.getRiga()];
                         Casellaswap1 = Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+1];
                         Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+1].setContentAreaFilled(true);
                     } if(Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()-1].getDama().getColore().equals(Dama.Colori.NULL) && 
                             Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()-1].getPedina().getColore().equals(Pedina.Colori.NULL)){
                         Casellaorigin = Board.scacchiera[pCasellaSelect.getColonna()][pCasellaSelect.getRiga()];
                         Casellaswap3 = Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()-1];
                         Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()-1].setContentAreaFilled(true);
                     }
                 //se sono troppo a destra
                 }else if(pCasellaSelect.getColonna()==Board.scacchiera.length-1){
                     if(Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+1].getDama().getColore().equals(Dama.Colori.NULL) && 
                             Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+1].getPedina().getColore().equals(Pedina.Colori.NULL)){
                         Casellaorigin = Board.scacchiera[pCasellaSelect.getColonna()][pCasellaSelect.getRiga()];
                         Casellaswap2 = Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+1];
                         Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+1].setContentAreaFilled(true);
                     } if(Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()-1].getDama().getColore().equals(Dama.Colori.NULL) && 
                             Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()-1].getPedina().getColore().equals(Pedina.Colori.NULL)){
                         Casellaorigin = Board.scacchiera[pCasellaSelect.getColonna()][pCasellaSelect.getRiga()];
                         Casellaswap4 = Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()-1];
                         Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()-1].setContentAreaFilled(true);
                     }
                 } else {
                     //funziopne generica di movimento
                     if(Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+1].getDama().getColore().equals(Dama.Colori.NULL) && 
                             Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+1].getPedina().getColore().equals(Pedina.Colori.NULL)){
                         Casellaorigin = Board.scacchiera[pCasellaSelect.getColonna()][pCasellaSelect.getRiga()];
                         Casellaswap1 = Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+1];
                         Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()+1].setContentAreaFilled(true);
                     } if(Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+1].getDama().getColore().equals(Dama.Colori.NULL) && 
                             Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+1].getPedina().getColore().equals(Pedina.Colori.NULL)){
                         Casellaorigin = Board.scacchiera[pCasellaSelect.getColonna()][pCasellaSelect.getRiga()];
                         Casellaswap2 = Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+1];
                         Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()+1].setContentAreaFilled(true);
                     } if(Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()-1].getDama().getColore().equals(Dama.Colori.NULL) && 
                             Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()-1].getPedina().getColore().equals(Pedina.Colori.NULL)){
                         Casellaorigin = Board.scacchiera[pCasellaSelect.getColonna()][pCasellaSelect.getRiga()];
                         Casellaswap3 = Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()-1];
                         Board.scacchiera[pCasellaSelect.getColonna()+1][pCasellaSelect.getRiga()-1].setContentAreaFilled(true);
                     } if(Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()-1].getDama().getColore().equals(Dama.Colori.NULL) && 
                             Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()-1].getPedina().getColore().equals(Pedina.Colori.NULL)){
                         Casellaorigin = Board.scacchiera[pCasellaSelect.getColonna()][pCasellaSelect.getRiga()];
                         Casellaswap4 = Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()-1];
                         Board.scacchiera[pCasellaSelect.getColonna()-1][pCasellaSelect.getRiga()-1].setContentAreaFilled(true);
                     }
                 }
             } else {
                 //JOptionPane.showMessageDialog(null, "Tocca al giocatore con la pedina " + getActivePlayer().getPedina().getColore());
             }
         //se nel caso procedo con la mossa successiva
         } else {
             //nel caso ho riselezionato la stessa casella
             if(Casellaorigin.getDama().getColore().equals(Dama.Colori.NULL)){
             //nel caso in cui sto schiacciando una casella che ho previsto per lo spostamento DI UNA CASELLA
             }else if(pCasellaSelect.getDama().equals(Casellaswap1.getDama()) && Casellaorigin.getRiga()+RilevaPedinaMove==Casellaswap1.getRiga()){
                 //sposto la pedina nella casella successiva
                 Board.scacchiera[Casellaswap1.getColonna()][Casellaswap1.getRiga()].
                         setDama(Board.scacchiera[Casellaorigin.getColonna()][Casellaorigin.getRiga()].getDama());
                 //cancella la precedente
                 Board.scacchiera[Casellaorigin.getColonna()][Casellaorigin.getRiga()].
                         setDama(new Dama(Dama.Colori.NULL));
                 //smemorizza la pedina origine
                 Casellaorigin = new Casella(Scacchiera.Colori.NERO);
                 
                 //deseleziona l'altra possibilita' di movimento
                 Board.scacchiera[Casellaswap2.getColonna()][Casellaswap2.getRiga()].setContentAreaFilled(false);
                 Board.scacchiera[Casellaswap3.getColonna()][Casellaswap3.getRiga()].setContentAreaFilled(false);
                 Board.scacchiera[Casellaswap4.getColonna()][Casellaswap4.getRiga()].setContentAreaFilled(false);
                 
                 //Scambio turni tra giocatori
                 CambiaTurno();
                 
             } else if (pCasellaSelect.getDama().equals(Casellaswap2.getDama()) && Casellaorigin.getRiga()+RilevaPedinaMove==Casellaswap2.getRiga()){
                 //sposto la pedina nella casella successiva
                 Board.scacchiera[Casellaswap2.getColonna()][Casellaswap2.getRiga()].
                         setDama(Board.scacchiera[Casellaorigin.getColonna()][Casellaorigin.getRiga()].getDama());
                 
                 //cancella la precedente
                 Board.scacchiera[Casellaorigin.getColonna()][Casellaorigin.getRiga()].
                         setDama(new Dama(Dama.Colori.NULL));
                 //smemorizza la pedina origine
                 Casellaorigin = new Casella(Scacchiera.Colori.NERO);
                 
                 //deseleziona l'altra possibilita' di movimento
                 Board.scacchiera[Casellaswap1.getColonna()][Casellaswap1.getRiga()].setContentAreaFilled(false);
                 Board.scacchiera[Casellaswap3.getColonna()][Casellaswap3.getRiga()].setContentAreaFilled(false);
                 Board.scacchiera[Casellaswap4.getColonna()][Casellaswap4.getRiga()].setContentAreaFilled(false);
                 
                 //Scambio turni tra giocatori
                 CambiaTurno();
             }else if(pCasellaSelect.getDama().equals(Casellaswap3.getDama()) && Casellaorigin.getRiga()+RilevaPedinaMove==Casellaswap3.getRiga()){
                 //sposto la pedina nella casella successiva
                 Board.scacchiera[Casellaswap3.getColonna()][Casellaswap3.getRiga()].
                         setDama(Board.scacchiera[Casellaorigin.getColonna()][Casellaorigin.getRiga()].getDama());
                 //cancella la precedente
                 Board.scacchiera[Casellaorigin.getColonna()][Casellaorigin.getRiga()].
                         setDama(new Dama(Dama.Colori.NULL));
                 //smemorizza la pedina origine
                 Casellaorigin = new Casella(Scacchiera.Colori.NERO);
                 
                 //deseleziona l'altra possibilita' di movimento
                 Board.scacchiera[Casellaswap1.getColonna()][Casellaswap1.getRiga()].setContentAreaFilled(false);
                 Board.scacchiera[Casellaswap2.getColonna()][Casellaswap2.getRiga()].setContentAreaFilled(false);
                 Board.scacchiera[Casellaswap4.getColonna()][Casellaswap4.getRiga()].setContentAreaFilled(false);
                 
                 //Scambio turni tra giocatori
                 CambiaTurno();
                 
             } else if (pCasellaSelect.getDama().equals(Casellaswap4.getDama()) && Casellaorigin.getRiga()+RilevaPedinaMove==Casellaswap4.getRiga()){
                 //sposto la pedina nella casella successiva
                 Board.scacchiera[Casellaswap4.getColonna()][Casellaswap4.getRiga()].
                         setDama(Board.scacchiera[Casellaorigin.getColonna()][Casellaorigin.getRiga()].getDama());
                 
                 //cancella la precedente
                 Board.scacchiera[Casellaorigin.getColonna()][Casellaorigin.getRiga()].
                         setDama(new Dama(Dama.Colori.NULL));
                 //smemorizza la pedina origine
                 Casellaorigin = new Casella(Scacchiera.Colori.NERO);
                 
                 //deseleziona l'altra possibilita' di movimento
                 Board.scacchiera[Casellaswap1.getColonna()][Casellaswap1.getRiga()].setContentAreaFilled(false);
                 Board.scacchiera[Casellaswap2.getColonna()][Casellaswap2.getRiga()].setContentAreaFilled(false);
                 Board.scacchiera[Casellaswap3.getColonna()][Casellaswap3.getRiga()].setContentAreaFilled(false);
                 
                 //Scambio turni tra giocatori
                 CambiaTurno();
             }
         }
     }
     
     //Controllo se si puo' mangiare con una dama
     public static void ifDamaCanEat(Casella pCasellaSelect){
         
     }
     
     /*
     //Funzione che pulisce ogni tipo di suggerimento precedente 
     public static void ClearHint(){
         for (int i=0; i< Board.scacchiera.length;i++){
             for (int j=0; j< Board.scacchiera[0].length;i++){
                 Board.scacchiera[i][j].setContentAreaFilled(false);
             }
         }
     }*/
     //Trasforma una pedina in una dama
     public static void CanBeDama(Casella pCasella){
         //controlla se la casella non ha ancora una dama
         if(pCasella.getDama().getColore().equals(Dama.Colori.NULL)){
             //capisce su quale versante va fatta la dama
             if(pCasella.getPedina().getColore().equals(Pedina.Colori.BIANCO)){
                 //Capisce se sono arrivato in fondo alla scacchiera
                 if(pCasella.getRiga()==0){
                     Board.scacchiera[pCasella.getColonna()][pCasella.getRiga()].setDama(new Dama(pCasella.getPedina().getColore()));
                 }
             } else {
                 if(pCasella.getRiga()==Board.scacchiera[0].length-1){
                     Board.scacchiera[pCasella.getColonna()][pCasella.getRiga()].setDama(new Dama(pCasella.getPedina().getColore()));
                 }
             }
         }
     }
     
     public void setGameBoardVisible(boolean pset){
         Board.Set_Visible(pset);
     }
 
     public static void CambiaTurno(){
         if(activePlayer.equals(player1)){
             activePlayer = player2;
         } else {
             activePlayer = player1;
         }
     }
     
     public static void setActivePlayer(Giocatore pactivePlayer) {
         activePlayer = pactivePlayer;
     }
     
     public static Giocatore getActivePlayer() {
         return activePlayer;
     }
 
     public static Giocatore getPlayer1() {
         return player1;
     }
 
     public static Giocatore getPlayer2() {
         return player2;
     }
     
     
 }
