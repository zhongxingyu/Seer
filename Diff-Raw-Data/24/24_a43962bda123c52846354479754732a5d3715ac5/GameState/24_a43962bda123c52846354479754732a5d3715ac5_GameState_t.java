 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package LogicaNegocioServidor;
 
 import Share.GameRoom;
 import Share.Hand;
 import Share.Piece;
 import Share.User;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Random;
 
 /**
  *
  * @author Andre
  */
 class GameState {
 
     private int BOARDSIZEX = 7, BOARDSIZEY = 20, DECKSIZE = 28;
     private String name;
     Piece[] deck;
     Piece[] board;
     Hand[] hands;
     Random rand;
     private Hashtable<User, Hand> playerHands;
    
     User activePlayer;
     private Piece leftside;
     private Piece rightside;
 
     /**
      * Contrutor da classe GameState. Este inicializa todas as variáveis que vão
      * ser necessárias para o jogo.
      *
      * @param startingRoom é a GameRoom associada a este jogo, e que contém
      * algumas das informações necessárias para a inicialização do jogo.
      */
     public GameState(GameRoom startingRoom) {
        
         deck = new Piece[DECKSIZE];
         playerHands = new Hashtable<User, Hand>();
         hands = new Hand[startingRoom.getCurPlayers()];
         rand = new Random();
         
         name = startingRoom.getName();
 
         for (int j = 0; j < startingRoom.getCurPlayers(); j++) {
             hands[j] = new Hand();
         }
         int c = 0;
         for (int i = 0; i <= 6; i++) {
             for (int j = i; j <= 6; j++) {
                 deck[c] = new Piece(i, j,"peca"+i+""+j+".png");
                 c++;
             }
         }
     }
 
     /**
      * Get genérico
      *
      * @return String roomName
      */
     public String getName() {
         return name;
     }
 
     /**
      * Este método retorna o índice do player activo no momento do query
      *
      * @return int activePlayer
      */
     public User getCurPlayer() {
         return activePlayer;
     }
 
     /**
      * Set genérico
      *
      * @param name - Novo nome para a Sala
      */
     public void setName(String name) {
         this.name = name;
     }
 
     /**
      * Get default.
      *
      * @return Retorna o tabuleiro de jogo.
      */
     public Piece[] getBoard() {
         return board;
     }
 
     /**
      * Set comum.
      *
      * @param board é o novo tabuleiro a ser guardado.
      */
     public void setBoard(Piece[] board) {
         this.board = board;
     }
 
 
     /**
      * Este método implementa um gerador aleatório de inteiros de 0 a 28, com o
      * objectivo de dar peças aleatóreas aos jogadores. A peça sorteada é então
      * retirada do baralho e o seu valor correspondente (previamente
      * inicializado) é retornado.
      *
      * @return Retorna a Piece que foi sorteado aleatóreamente.
      */
     public Piece DrawPiece() {
         while (true) {
             int seed = rand.nextInt(28);
             if (deck[seed] != null) {
                 Piece temp = deck[seed];
                 deck[seed] = null;
                 return temp;
             }
         }
     }
 
     /**
      * Este método inicializa todas as mãos dos jogadores duma sala, chamando o
      * método DrawPiece() 7 vezes para as tantas peças com que cada User começa,
      * e inicializando as restantes com valores em branco.
      *
      * @param players é o Array de Users que irão ter as suas mãos sorteadas.
      */
     public void DrawHand(ArrayList<User> players) {
         ArrayList<Piece> tempHand = new ArrayList<Piece>();
         tempHand.clear();
         for(int i = 0; i < players.size(); i++) {
             tempHand.clear();
             for(int j = 0; j < 7; j++) {
                 tempHand.add(this.DrawPiece());
             }
             
             hands[i].setPieces(tempHand);
             this.playerHands.put(players.get(i), hands[i]);
            
         }
          
     }
 
     /**
      * Get comum.
      *
      * @return Retorna a Hashtable com <String, Hand>
      */
     public Hashtable<User, Hand> getPlayerHands() {
         return playerHands;
     }
     
     public User getActivePlayer() {
         return this.activePlayer;
     }
     
     public void setActivePlayer(User user) {
         this.activePlayer = user;
     }
     
     public Piece getLeftSide() {
         return this.leftside;
     }
     
     public Piece getRightSide() {
         return this.rightside;
     }
     
     public void setLeftSide(Piece leftSide) {
         this.leftside = leftSide;
     }
     
     public void setRightSide(Piece rightSide) {
         this.rightside = rightSide;
     }
 }
