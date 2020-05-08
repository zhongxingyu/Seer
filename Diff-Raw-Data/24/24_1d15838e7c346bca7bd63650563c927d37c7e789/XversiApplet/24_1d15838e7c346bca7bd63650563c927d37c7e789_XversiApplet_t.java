 /*
 # This file XversiApplet.java is part of xversi-java-parallel, a board game called reversi or othello.
 #
 # Copyright (c) 2002-2006 Chung Shin Yee <cshinyee@gmail.com>
 #
 #       http://cshinyee.blogspot.com/index.html
 #
 # This program is free software; you can redistribute it and/or
 # modify it under the terms of the GNU General Public License as
 # published by the Free Software Foundation; either version 2 of the
 # License, or (at your option) any later version.
 #
 # This program is distributed in the hope that it will be useful,
 # but WITHOUT ANY WARRANTY; without even the implied warranty of
 # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 # GNU General Public License for more details.
 #
 # You should have received a copy of the GNU General Public License
 # along with this program; if not, write to the Free Software
 # Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 # USA.
 #
 # The GNU General Public License is contained in the file COPYING.
 #
 */
 
 import java.applet.Applet;
 import java.lang.Math ;
 import java.awt.* ;
 import java.awt.event.* ;
 import java.io.* ;
 import java.lang.Integer ;
 import java.net.* ;
 import java.rmi.* ;
 
 /* applet for GUI */
 public class XversiApplet extends Applet implements ActionListener,MouseListener  {

    private static final long serialVersionUID = 2006L ;

     protected int numThreads = 1 ;
     protected int numComputeNode = 1 ;
     protected RandomAccessFile nodefile ;    
     protected String[] node ;
 
     
     private Spot s = new Spot() ;
     
     XversiBoard b = new XversiBoard() ;
     XversiAgent agent = new XversiAgent() ;
     XversiPlayer player = new XversiPlayer(0) ;
     
     private boolean paintNeeded ;
     private int turn ;
     private int turnNum ;
     private boolean progress ;
     private int lastMove ;
     
     Button resetButton ;
     Button switchButton ;
     Label turnLabel ;
     Label humanLabel ;
     Label blackLabel ;
     Label whiteLabel ;
     
     protected XversiInterface evalInterface[] = new XversiInterface[numComputeNode] ;  
 
     /* read nodeinfo file, lookup service, initialize */     
     public void init() {
         
 	try {
 	nodefile = new RandomAccessFile("nodeinfo.txt" , "r") ;
         numComputeNode = Integer.parseInt(nodefile.readLine()) ;
 	System.out.println("Number of Node : " + numComputeNode) ;
 	} catch(FileNotFoundException e) {
 	  System.out.println(e.getMessage()) ;
 	  System.exit(1) ;
 	} catch(IOException e) {
 	  System.out.println(e.getMessage()) ;
 	  System.exit(1) ;
 	}
 
         String host = new String("155.69.8.41") ;
         //String host=new String("155.69.146.48") ;
         String port=new String("9394") ;
         //  bWin = 0 ;
         //  wWin = 0 ;
         //  bwDraw = 0 ;
 
 	String[] namelist ;        
 	node = new String[numComputeNode] ;
 
 
 	evalInterface = new XversiInterface[numComputeNode] ;
 
 	int i ;
  	int j ;
         try {
 	    System.out.println("before lookup") ;
 	    for(j = 0 ; j < numComputeNode ; j++) {
 
 	      node[j] = nodefile.readLine() ;
 	      System.out.println("Node " + j + " " + node[j]) ;
 
 	      try {
 	        namelist = Naming.list("rmi://"+node[j]) ;
 
  	        for(i = 0 ; i < namelist.length ; i++) {
 		  System.out.println("name " + i + " " + namelist[i]) ;
 	        }
 
                 evalInterface[j] = (XversiInterface) Naming.lookup("rmi://"+node[j]+"/xversiInterface") ;
 
 	      } catch(NotBoundException e) {
 		int k ;
 		System.out.println(e.getMessage()) ;
 		numComputeNode = numComputeNode - 1 ;
 	        if(numComputeNode <= 0) {
 		  System.out.println("Num working compute node <= 0") ;
 		}
 		for(k = j ; k < numComputeNode ; k++) {
 		  node[k] = node[k+1] ;
 		}
 		j-- ;
 		
 	     
 	      } catch(RemoteException e) {
 		System.out.println(e.getMessage()) ;
 		int k ;
 		System.out.println(e.getMessage()) ;
 		numComputeNode = numComputeNode - 1 ;
 	        if(numComputeNode <= 0) {
 		  System.out.println("Num working compute node <= 0") ;
 		}
 		for(k = j ; k < numComputeNode ; k++) {
 		  node[k] = node[k+1] ;
 		}
 		j-- ;
 
 	      }
 		
 	    }
 	    System.out.println("after lookup") ;
         } catch (Exception e) { System.out.println("error: " + e) ; }
         
         Power2.Init() ;
         resetButton = new Button("Reset") ;
         add(resetButton) ;
         switchButton = new Button("Switch") ;
         add(switchButton) ;
         blackLabel = new Label("Black: 2") ;
         add(blackLabel) ;
         whiteLabel = new Label("White: 2") ;
         add(whiteLabel) ;
         humanLabel = new Label("You: Black") ;
         add(humanLabel) ;
         turnLabel = new Label("Turn: Black") ;
         add(turnLabel) ;
         
         resetButton.addActionListener(this) ;
         switchButton.addActionListener(this) ;
         addMouseListener(this) ;
         
         reset() ;
     }
     
     /* reset for next game */
     private void reset() {
         b.Init() ;
         agent.Init() ;
         player.Init(1) ;
         
         // Let turn = 1, to activate Computer vs Computer mode.
         // turn = 1 ;
         turn = 0 ;
         turnNum = 4 ;
         humanLabel.setText("You: Black") ;
         turnLabel.setText("Turn: Black") ;
         progress = true ;
         lastMove = -1 ;
         paintNeeded = true ;
         repaint() ;
     }
     
     /* start to select a move */
     public void start() {
         
         XversiMoveList list ;
         XversiMoveList bestList ;
         XversiMoveList tempList ;
         
         int i  ;
         Integer bestMove = new Integer(0) ;
         Integer bestScore = new Integer(0) ;
         Sema lock = new Sema(1) ;
         int move ;
         Integer score = new Integer(0) ;
         Integer count = new Integer(0) ;
         int totalM = 0 ;    
         
         if(paintNeeded == true) {
             repaint() ;
             return ;
         }
         
         if(progress && player.GetSide() == turn) {
             if((list = XversiAgent.GenerateMove(b , turn)) != null) {
                 
                 totalM = list.GetNum() + 1 ;
                 bestScore = new Integer(-9999999) ;
                 bestList = new XversiMoveList(totalM) ;
                 
                 evalPara parameters = new evalPara(0 , bestMove , bestScore , score , lock , count , bestList) ;
                 
                 for(i = 0 ; i < totalM ; i++) {
                     XversiBoard bb = new XversiBoard() ;
                     move = list.GetMove(i) ;
                     XversiAgent.TranslatePosition(move , s) ;
                     bb.Copy(b) ;
                     XversiAgent.MakeMove(bb , s.x , s.y , turn) ;
                     
                     score = new Integer(-999999) ;
                     
                     try {
                         evalThread ethread = new evalThread(parameters , bb , turn , (turn+1)%2 , turnNum+1 , move , evalInterface[i%numComputeNode] , node[i%numComputeNode])  ;
                         ethread.start() ;
                                                                      
                     } catch (Exception e) { System.out.println(e) ; }
                     
 /*                    
 
                     while(true) {
                         try {
                         Thread.sleep(100) ;
                         } catch(InterruptedException e) { }
                         lock.down() ;
                         //System.out.println("count val : " + parameters.counter.intValue()) ;
                         //System.out.println("i " + i) ;
                         //System.out.println("score : " + parameters.s.intValue() ) ;
                         if(parameters.counter.intValue() >= totalM || parameters.counter.intValue() + numThreads*numComputeNode - 1 > i ) {
                             
                             lock.up() ;
                             break ;
                         }
                         lock.up() ;
                     }
 */
 
                 }
 
 
                 
                 try {
                     while(true) {
                         Thread.sleep(200) ;
                         lock.down() ;
                         //System.out.println("count val : " + parameters.counter.intValue()) ;
                         //System.out.println("totalM " + totalM) ;
                         if(parameters.counter.intValue() >= totalM) {
                             
                             lock.up() ;
                             break ;
                         }
                         lock.up() ;
 
                     }
                 } catch (InterruptedException e) { }
 
 
 		System.out.println("best score :  " + parameters.bS.intValue()) ;		
                 
                 int k ;
                 k = (int)(Math.random() * (bestList.GetNum()+1)) ;
                 k = bestList.GetMove(k) ;
 
                 lastMove = k ;
                 XversiAgent.TranslatePosition(k , s) ;
                 System.out.println("Move take: " + k + " x = " + s.x + " y = " + s.y) ;
                 XversiAgent.MakeMove(b , s.x , s.y , turn) ;
                 turnNum++ ;
                 
                 paintNeeded = true ;
                 repaint() ;
             }
             
             if(XversiAgent.CountMove(b , (turn+1)%2) > 0) {
                 turn = (turn + 1) % 2 ;
 		// Let player switch side, and call start(), to activate Computer vs Computer mode.
 		// player.SwitchSide() ;
 		// start() ;
             } else {
                 if(XversiAgent.CountMove(b , turn) > 0) {
                     start() ;
                 } else {
                     progress = false ;
                 }
                 
             }
             if(turn == 0) {
                 turnLabel.setText("Turn: Black") ;
             } else {
                 turnLabel.setText("Turn: White") ;
             }
             
         }
         
         
     }
 
     /* update screen */    
     public void paint(Graphics g) {
         
         int i,j ;
         int bc , wc ;
         int w ;
         int h ;
         
         
         bc = wc = 0 ;
         
         g.setColor(Color.gray) ;
         g.drawRect(1,1, getSize().width - 1 , getSize().height - 1) ;
         
         g.setColor(Color.black) ;
         for(i = 0 ; i <= 8 ; i++) {
             g.drawLine((getSize().width-1)* i / 8 , getSize().height / 9 , (getSize().width-1) * i / 8  , getSize().height) ;
             g.drawLine(0 , ((getSize().height - 1) * (i+1)) / 9 , getSize().width , ((getSize().height - 1) * (i+1)) / 9) ;
         }
         
         w = getSize().width / 8 ;
         h = getSize().height / 9 ;
         for(i = 1 ; i <= XversiBoard.BOARD_SIZE ; i++) {
             for(j = 1 ; j <= XversiBoard.BOARD_SIZE ; j++) {
                 if(b.GetBoard(i,j) >= 0) {
                     if(b.GetBoard(i,j) == 0) {
                         g.setColor(Color.black) ;
                         bc++ ;
                     } else {
                         g.setColor(Color.red) ;
                         wc++ ;
                     }
                     s.x = (i-1) * w + w/2 ;
                     s.y = j * h + h/2 ;
                     g.fillOval(s.x - w*4/10 , s.y - h*4/10 , w*4/5 ,h*4/5) ;
                 }
             }
         }
         
         if(lastMove > 0) {
             XversiAgent.TranslatePosition(lastMove , s) ;
             g.setColor(Color.green) ;
             s.x = (s.x-1) * (getSize().width / 8) + (getSize().width/16) ;
             s.y = s.y * (getSize().height / 9) + (getSize().height/18) ;
             g.fillOval(s.x - 2 , s.y - 2, 2*2 , 2*2) ;
         }
         
         whiteLabel.setText("White: " + wc ) ;
         blackLabel.setText("Black: " + bc ) ;
         
        
         if(paintNeeded == true) {
             paintNeeded = false ;
             start() ;
         }
         
     }
     
     public void mousePressed(MouseEvent event) {
 
         int x , y ;
         
         if(progress && player.GetSide() != turn) {
             
             x = event.getX() / (getSize().width / 8) + 1 ;
             y = event.getY() / (getSize().height / 9) ;
             
             if(XversiAgent.CheckXY(x,y)) {
                 
                 if(XversiAgent.CheckMove(b , x , y , turn) > 0) {
                     XversiAgent.MakeMove(b , x , y , turn) ;
                     lastMove = -1 ;
                     turnNum ++ ;
                     paintNeeded = true ;
                     repaint() ;
                     if(XversiAgent.CountMove(b , (turn + 1)%2) > 0) {
                         turn = (turn+1) % 2 ;
                         if(turn == 0) {
                             turnLabel.setText("Turn: Black") ;
                         } else {
                             turnLabel.setText("Turn: White") ;
                         }
                         start() ;
                         
                     } else {
                         if(XversiAgent.CountMove(b , turn) <= 0) {
                             progress = false ;
                         }
                     }
                     
                 }
                 
             } else { start() ; }
         } else {
             repaint() ;
         }
 
     }
 
     /* response to mouse click */
     public void mouseClicked(MouseEvent event) {
         
         int x , y ;
         
         if(progress && player.GetSide() != turn) {
             
             x = event.getX() / (getSize().width / 8) + 1 ;
             y = event.getY() / (getSize().height / 9) ;
             
             if(XversiAgent.CheckXY(x,y)) {
                 
                 if(XversiAgent.CheckMove(b , x , y , turn) > 0) {
                     XversiAgent.MakeMove(b , x , y , turn) ;
                     lastMove = -1 ;
                     turnNum ++ ;
                     paintNeeded = true ;
                     repaint() ;
                     if(XversiAgent.CountMove(b , (turn + 1)%2) > 0) {
                         turn = (turn+1) % 2 ;
                         if(turn == 0) {
                             turnLabel.setText("Turn: Black") ;
                         } else {
                             turnLabel.setText("Turn: White") ;
                         }
                         start() ;
                         
                     } else {
                         if(XversiAgent.CountMove(b , turn) <= 0) {
                             progress = false ;
                         }
                     }
                     
                 }
                 
             } else { start() ; }
         } else {
             repaint() ;
         }
         
     }
     
     public void stop() {
         paintNeeded = true ;
         repaint() ;
         
     }
     
     public void mouseReleased(MouseEvent event) {}
     public void mouseEntered(MouseEvent event) {}
     public void mouseExited(MouseEvent event) {}
     
     
     
     public void actionPerformed(ActionEvent e) {
         if(e.getSource() == resetButton) {
             reset() ;
         } else if(e.getSource() == switchButton) {
             player.SwitchSide() ;
             if(player.GetSide() == 1) {
                 // computer is white
                 humanLabel.setText("You: Black") ;
             } else {
                 // computer is black
                 humanLabel.setText("You: White") ;
             }
             paintNeeded = true ;
             repaint() ;
             
         }
         
     }
     
     
 }
