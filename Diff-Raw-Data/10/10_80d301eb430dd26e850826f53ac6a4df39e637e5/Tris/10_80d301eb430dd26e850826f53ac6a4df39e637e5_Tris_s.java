 /*
 	Shin Darth & Helias World
 	http://shinworld.altervista.org
 	
 	Tris ~ Coded By Helias
 */
 //package tris;
 
 import javax.swing.JOptionPane;
 import java.awt.Toolkit;
 import java.awt.Dimension;
 
 public class Tris extends javax.swing.JFrame {
 
     int Giocatore=0;
     int Bot=0;
     int[] scacchiera=new int[10];
     int[][] scacchiera2=new int[4][10];
     boolean g=false;
     String mod="Benvenuto, in che modalita' preferisci giocare? \n(1)single-player \n(2)multiplayer?";
     int P=0;
     int D=0;
     public Tris() {
         initComponents();
     }
 
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jButton1 = new javax.swing.JButton();
         jButton2 = new javax.swing.JButton();
         jButton3 = new javax.swing.JButton();
         jButton4 = new javax.swing.JButton();
         jButton5 = new javax.swing.JButton();
         jButton6 = new javax.swing.JButton();
         jButton7 = new javax.swing.JButton();
         jButton8 = new javax.swing.JButton();
         jButton9 = new javax.swing.JButton();
         jMenuBar1 = new javax.swing.JMenuBar();
         jMenu1 = new javax.swing.JMenu();
         jMenuItem1 = new javax.swing.JMenuItem();
         jMenuItem2 = new javax.swing.JMenuItem();
 
 	Toolkit myKit=Toolkit.getDefaultToolkit();
 	Dimension myScreenSize = myKit.getScreenSize();
 
 	Player_Request();
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle("Tic Tac Toe");
         setBackground(new java.awt.Color(0, 255, 242));
         setResizable(false);
         setLocation(myScreenSize.width/4, myScreenSize.height/4);
 	
         jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/vuoto.png")));
         jButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton1ActionPerformed(evt);
             }
         });
 
         jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/vuoto.png")));
         jButton2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton2ActionPerformed(evt);
             }
         });
 
         jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/vuoto.png")));
         jButton3.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton3ActionPerformed(evt);
             }
         });
 
         jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/vuoto.png")));
         jButton4.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton4ActionPerformed(evt);
             }
         });
 
         jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/vuoto.png")));
         jButton5.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton5ActionPerformed(evt);
             }
         });
 
         jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/vuoto.png")));
         jButton6.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton6ActionPerformed(evt);
             }
         });
 
         jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/vuoto.png")));
         jButton7.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton7ActionPerformed(evt);
             }
         });
 
         jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/vuoto.png")));
         jButton8.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton8ActionPerformed(evt);
             }
         });
 
         jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/vuoto.png")));
         jButton9.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jButton9ActionPerformed(evt);
             }
         });
 
         jMenu1.setText("File");
 
         jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
         jMenuItem1.setText("Nuova Partita");
         jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem1ActionPerformed(evt);
             }
         });
         jMenu1.add(jMenuItem1);
 
         jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
         jMenuItem2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/Exit.png")));
         jMenuItem2.setText("Esci");
         jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jMenuItem2ActionPerformed(evt);
             }
         });
         jMenu1.add(jMenuItem2);
 
         jMenuBar1.add(jMenu1);
 
         setJMenuBar(jMenuBar1);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(5, 5, 5)
                         .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 94, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jButton7, javax.swing.GroupLayout.PREFERRED_SIZE, 93, Short.MAX_VALUE)
                             .addComponent(jButton4, 0, 0, Short.MAX_VALUE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jButton5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jButton8, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))))
                 .addGap(6, 6, 6)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(jButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(jButton6, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(jButton2, 0, 0, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(jButton5, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(jButton7, 0, 0, Short.MAX_VALUE)
                     .addComponent(jButton8, javax.swing.GroupLayout.PREFERRED_SIZE, 91, Short.MAX_VALUE)
                     .addComponent(jButton9, javax.swing.GroupLayout.PREFERRED_SIZE, 91, Short.MAX_VALUE))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {
         Player(jButton1, 1);
         Check();
     }
 
     private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {
         System.exit(0);
     }
 
     private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {
         Giocatore=0;
         g=false;
         D=0;
         for(int i=1; i<10; i++){scacchiera[i]=0;}
         for(int i=1; i<3; i++)
         {
             for(int j=1; j<3; j++){scacchiera2[i][j]=0;}
         }
 
         jButton1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/vuoto.png")));
         jButton2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/vuoto.png")));
         jButton3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/vuoto.png")));
         jButton4.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/vuoto.png")));
         jButton5.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/vuoto.png")));
         jButton6.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/vuoto.png")));
         jButton7.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/vuoto.png")));
         jButton8.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/vuoto.png")));
         jButton9.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/vuoto.png")));
         Player_Request();
     }
 
      private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {
         Player(jButton2, 2);
 	Check();
     }                                        
 
      private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {
         Player(jButton3, 3);
 	Check();
     }                                        
 
      private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {
         Player(jButton4, 4);
 	Check();
     }                                        
 
      private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {
         Player(jButton5, 5);
 	Check();
     }                                        
 
      private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {
         Player(jButton6, 6);
 	Check();
     }                                        
 
      private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {
         Player(jButton7, 7);
 	Check();
     }                                        
 
      private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {
         Player(jButton8, 8);
         Check();
     }                                        
 
      private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {
         Player(jButton9, 9);
         Check();
     }                                        
 
     public void Player(javax.swing.JButton jButton, int n)
     {
         if(scacchiera[n]==0 && g==false)
         {
             if(Giocatore==1)
             {
                 scacchiera[n]=2;
                 Giocatore=2;
                 jButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/X.png")));
             }
             else
             {
                 scacchiera[n]=1;
                 Giocatore=1;
                 jButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/O.png")));
                 if(P==1 && g==false)
                 {
                     Check();
                     switch(D)
                     {
                         case 1:
                             if(g==false)
 			    {
 				Scegli_Bot();
 			    }
                             break;
                         case 2:
                             for(int i=1; i<3; i++)
                             {
                                 for(int j=1; j<3; j++)
                                 {
                                     if(i==2){j+=3;}
                                     if(i==3){j+=6;}
                                     scacchiera2[i][j]=scacchiera[j];
                                 }
                             }
                             break;
                     }
                 }
             }
         }
     }
 
     public void Scegli_Bot()
     {
 	switch(Bot)
 	{
 		case 1:
 			Player1();
 			break;
 		case 2:
 			Player2();
 			break;
 		case 3:
 			Player3();
 			break;
 		default:
 			Player2();
 			break;
 	}
     }
     
     // I.A. Facile
     public void Player1()
     {
         Giocatore=2;
         int r=(int)((Math.random()*9)+1);
         switch(r)
         {
             case 1:
                 Bot1(jButton1, r);
                 break;
             case 2:
                 Bot1(jButton2, r);
                 break;
             case 3:
                 Bot1(jButton3, r);
                 break;
             case 4:
                 Bot1(jButton4, r);
                 break;
             case 5:
                 Bot1(jButton5, r);
                 break;
             case 6:
                 Bot1(jButton6, r);
                 break;
             case 7:
                 Bot1(jButton7, r);
                 break;
             case 8:
                 Bot1(jButton8, r);
                 break;
             case 9:
                 Bot1(jButton9, r);
                 break;
         }
     }
     
     // I.A. Normale
     public void Player2()
     {
         Giocatore=2;
         int r=(int)((Math.random()*9)+1);
 	boolean eseguita=false;
 
 	//Esegui mossa
 	for(int j=2; j>0; j--)
 	{
 		//Orizzontali
 		for(int i=1; i<9; i+=3)
 		{
 			if(scacchiera[i]==0 & scacchiera[i+1]==j & scacchiera[i+2]==j){r=i; eseguita=true; break;}
 			if(scacchiera[i]==j & scacchiera[i+1]==0 & scacchiera[i+2]==j){r=i+1; eseguita=true; break;}
 			if(scacchiera[i]==j & scacchiera[i+1]==j & scacchiera[i+2]==0){r=i+2; eseguita=true; break;}
 		}		
 		if(eseguita==true){break;}
 
 		//Verticali
 		for(int i=1; i<4; i++)
 		{
 			if(scacchiera[i]==0 & scacchiera[i+3]==j & scacchiera[i+6]==j){r=i; eseguita=true; break;}
 			if(scacchiera[i]==j & scacchiera[i+3]==0 & scacchiera[i+6]==j){r=i+3; eseguita=true; break;}
 			if(scacchiera[i]==j & scacchiera[i+3]==j & scacchiera[i+6]==0){r=i+6; eseguita=true; break;}
 		}
 		if(eseguita==true){break;}
 
 		//Diagonali
 		if(scacchiera[1]==0 & scacchiera[5]==j & scacchiera[9]==j){r=1; break;}
 		if(scacchiera[1]==j & scacchiera[5]==0 & scacchiera[9]==j){r=5; break;}
 		if(scacchiera[1]==j & scacchiera[5]==j & scacchiera[9]==0){r=9; break;}
 
 		//Diagonali inverse
 		if(scacchiera[3]==0 & scacchiera[5]==j & scacchiera[7]==j){r=3; break;}
 		if(scacchiera[3]==j & scacchiera[5]==0 & scacchiera[7]==j){r=5; break;}
 		if(scacchiera[3]==j & scacchiera[5]==j & scacchiera[7]==0){r=7; break;}
 	}
 
         switch(r)
         {
             case 1:
                 Bot1(jButton1, r);
                 break;
             case 2:
                 Bot1(jButton2, r);
                 break;
             case 3:
                 Bot1(jButton3, r);
                 break;
             case 4:
                 Bot1(jButton4, r);
                 break;
             case 5:
                 Bot1(jButton5, r);
                 break;
             case 6:
                 Bot1(jButton6, r);
                 break;
             case 7:
                 Bot1(jButton7, r);
                 break;
             case 8:
                 Bot1(jButton8, r);
                 break;
             case 9:
                 Bot1(jButton9, r);
                 break;
         }
     }
     
     // I.A. Difficile
     public void Player3()
     {
         Giocatore=2;
         int r=(int)((Math.random()*9)+1);
 	boolean eseguita=false;
 
 	for(int j=2; j>0; j--)
 	{
 		//Orizzontali
 		for(int i=1; i<9; i+=3)
 		{
 			if(scacchiera[i]==0 & scacchiera[i+1]==j & scacchiera[i+2]==j){r=i; eseguita=true; break;}
 			if(scacchiera[i]==j & scacchiera[i+1]==0 & scacchiera[i+2]==j){r=i+1; eseguita=true; break;}
 			if(scacchiera[i]==j & scacchiera[i+1]==j & scacchiera[i+2]==0){r=i+2; eseguita=true; break;}
 		}
 		if(eseguita==true){break;}
 		
 		//Verticali
 		for(int i=1; i<4; i++)
 		{
 			if(scacchiera[i]==0 & scacchiera[i+3]==j & scacchiera[i+6]==j){r=i; eseguita=true; break;}
 			if(scacchiera[i]==j & scacchiera[i+3]==0 & scacchiera[i+6]==j){r=i+3; eseguita=true; break;}
 			if(scacchiera[i]==j & scacchiera[i+3]==j & scacchiera[i+6]==0){r=i+6; eseguita=true; break;}
 		}
 		if(eseguita==true){break;}
 
 		//Diagonali
 		if(scacchiera[1]==0 & scacchiera[5]==j & scacchiera[9]==j){r=1; eseguita=true; break;}
 		if(scacchiera[1]==j & scacchiera[5]==0 & scacchiera[9]==j){r=5; eseguita=true; break;}
 		if(scacchiera[1]==j & scacchiera[5]==j & scacchiera[9]==0){r=9; eseguita=true; break;}
 
 		//Diagonali inverse
 		if(scacchiera[3]==0 & scacchiera[5]==j & scacchiera[7]==j){r=3; eseguita=true; break;}
 		if(scacchiera[3]==j & scacchiera[5]==0 & scacchiera[7]==j){r=5; eseguita=true; break;}
 		if(scacchiera[3]==j & scacchiera[5]==j & scacchiera[7]==0){r=7; eseguita=true; break;}
 	}
 
 	if(eseguita==false)
 	{
		if(scacchiera[5]==0){r=5;}
 		else if(scacchiera[1]==0){r=1;}
 		else if(scacchiera[3]==0){r=3;}
 		else if(scacchiera[7]==0){r=7;}
 		else if(scacchiera[9]==0){r=9;}
 		
 		if((scacchiera[1]==1 & scacchiera[9]==1) || (scacchiera[3]==1 & scacchiera[7]==1))
 		{
 			while(r==1 || r==3 || r==7 || r==9)
 			{
 				r=(int)((Math.random()*9)+1);
 			}
 		}
 	}
 
         switch(r)
         {
             case 1:
                 Bot1(jButton1, r);
                 break;
             case 2:
                 Bot1(jButton2, r);
                 break;
             case 3:
                 Bot1(jButton3, r);
                 break;
             case 4:
                 Bot1(jButton4, r);
                 break;
             case 5:
                 Bot1(jButton5, r);
                 break;
             case 6:
                 Bot1(jButton6, r);
                 break;
             case 7:
                 Bot1(jButton7, r);
                 break;
             case 8:
                 Bot1(jButton8, r);
                 break;
             case 9:
                 Bot1(jButton9, r);
                 break;
         }
     }
     
 
 
     public void Bot1(javax.swing.JButton jButton, int n)
     {
         if(scacchiera[n]==0)
         {
             jButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tris/images/X.png")));
             scacchiera[n]=2;
             Check();
         }
         else
         {
             Scegli_Bot();
         }
     }
 
     public void Check()
     {
         if(g==false){
         if((scacchiera[1]==1 & scacchiera[2]==1 & scacchiera[3]==1) |
           (scacchiera[4]==1 & scacchiera[5]==1 & scacchiera[6]==1) |
           (scacchiera[7]==1 & scacchiera[8]==1 & scacchiera[9]==1) |
           (scacchiera[1]==1 & scacchiera[5]==1 & scacchiera[9]==1) |
           (scacchiera[1]==1 & scacchiera[4]==1 & scacchiera[7]==1) |
           (scacchiera[2]==1 & scacchiera[5]==1 & scacchiera[8]==1) |
           (scacchiera[3]==1 & scacchiera[6]==1 & scacchiera[9]==1) |
           (scacchiera[3]==1 & scacchiera[5]==1 & scacchiera[7]==1))
         {
             JOptionPane.showMessageDialog(null,"Cerchio vince!");
             g=true;
         }
 
         if((scacchiera[1]==2 & scacchiera[2]==2 & scacchiera[3]==2) |
           (scacchiera[4]==2 & scacchiera[5]==2 & scacchiera[6]==2) |
           (scacchiera[7]==2 & scacchiera[8]==2 & scacchiera[9]==2) |
           (scacchiera[1]==2 & scacchiera[5]==2 & scacchiera[9]==2) |
           (scacchiera[1]==2 & scacchiera[4]==2 & scacchiera[7]==2) |
           (scacchiera[2]==2 & scacchiera[5]==2 & scacchiera[8]==2) |
           (scacchiera[3]==2 & scacchiera[6]==2 & scacchiera[9]==2) |
           (scacchiera[3]==2 & scacchiera[5]==2 & scacchiera[7]==2))
         {
             JOptionPane.showMessageDialog(null,"Croce vince!");
             g=true;
         }
 
         if(scacchiera[1]!=0 & scacchiera[2]!=0 & scacchiera[3]!=0 &
            scacchiera[4]!=0 & scacchiera[5]!=0 & scacchiera[6]!=0 &
            scacchiera[7]!=0 & scacchiera[8]!=0 & scacchiera[9]!=0
            & g==false)
         {
 		JOptionPane.showMessageDialog(null, "Pareggio!");
 		g=true;
         }
         }
     }
     
     public void Player_Request()
     {
 	P=Integer.parseInt(JOptionPane.showInputDialog(mod));
         if(P != 1 && P != 2)
 	{
 		JOptionPane.showMessageDialog(null, "Inserisci 1 per single-player o 2 per multiplayer!");
 		mod="Scegli la modalita' di gioco: \n(1)single-player \n(2)multiplayer";
 		Player_Request();
 	}
         else
 	{
 		D=1;
 		if(P==1){Bot=Integer.parseInt(JOptionPane.showInputDialog("Scegli la difficolta' \n(1)Facile \n(2)Normale \n(3)Difficile"));}
 	}
     }
     
     public static void main(String args[]) {
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new Tris().setVisible(true);
             }
         });
     }
 
     private javax.swing.JButton jButton1;
     private javax.swing.JButton jButton2;
     private javax.swing.JButton jButton3;
     private javax.swing.JButton jButton4;
     private javax.swing.JButton jButton5;
     private javax.swing.JButton jButton6;
     private javax.swing.JButton jButton7;
     private javax.swing.JButton jButton8;
     private javax.swing.JButton jButton9;
     private javax.swing.JMenu jMenu1;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JMenuItem jMenuItem1;
     private javax.swing.JMenuItem jMenuItem2;   
 }
