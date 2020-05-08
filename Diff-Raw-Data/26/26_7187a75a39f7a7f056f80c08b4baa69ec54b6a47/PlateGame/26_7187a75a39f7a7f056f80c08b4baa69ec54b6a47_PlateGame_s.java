 
 
 import java.awt.*;
 import java.awt.event.*;
 import java.text.BreakIterator;
 import java.util.GregorianCalendar;
 import javax.swing.*;
 import javax.swing.Icon;
 
 class PlateGame extends JFrame implements ActionListener,Runnable,MouseListener{
         public static final  int NUMOFTABLE = 9;
         public static final  int NUMOFBOMB = 10;
 	private int  numOfBomb=9;
 	private int  numOfTable=10;
 	private int[][] table; 
 	private int[][] visible ;
 	int checkIsAllCorrect=0,checkIsOpened=0,startTimer=0;
 	int i,j,k;
         /////////5555555555
         //Ver2//////////////////////
         JPanel startTab = new JPanel( new GridLayout(1,3));
         JLabel time  = new JLabel("    time :  00:00");
         JLabel stat = new JLabel("...");
         JButton reset = new JButton("New Game");
         
         //////////////////////////////
 	//set path bomb image
 	Icon pic =new ImageIcon("D:/tae/Red-Devil/Sweep/bomb.jpg");
         Icon pic2 = new ImageIcon("D:/tae/Red-Devil/Sweep/flag.jpg");
 	JPanel tablePanel = new JPanel();
 	JPanel blank = new JPanel();
 	JButton[][] block = new JButton[9][9];
         int[][] blockMark = new int[9][9];
         int[][] MarkOpen = new int[9][9];
         //private int Icon;
 	PlateGame(){
 		super("GameMineSweeper");
 		setBounds(200,200,400,480);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setResizable(false);
 		
 		numOfBomb = NUMOFBOMB;
 		 numOfTable = NUMOFTABLE;
 		
 		tablePanel.setLayout(new GridLayout(9,9,0,0));
 		//add all Button 
 		for(int i=0;i<9;i++){
 		for(int j=0;j<9;j++){
 			table = new int[ numOfTable][ numOfTable];
 			visible = new int[ numOfTable][ numOfTable];
 			block[i][j] = new JButton("");
 			tablePanel.add(block[i][j]);
 			block[i][j].addActionListener(this);
                         block[i][j].addMouseListener(this);
 		}
 		}
 		RandomizeTable ran = new RandomizeTable( numOfBomb, numOfTable);
 		ran.takeNumber();
 		table = ran.getTableArray();
                 
                 
                 //ver2
                 startTab.add(time);
                 startTab.add(reset);
                 reset.addActionListener(this);
                 add(startTab,"North");
                 ////////
 		add(tablePanel,"Center");
                 
                 
 	}
 	
 	public void actionPerformed(ActionEvent e){
             
 		for(int i=0;i<9;i++){
 		for(int j=0;j<9;j++){
 			if(e.getSource()== block[i][j])
 			{
                             MarkOpen[i][j] = 1;
                          if(startTimer==0)
                              new Thread(this).start();
 					startTimer=1;
 			block[i][j].setEnabled(false);
 			visible[i][j]=1;
 			if(table[i][j]>0&&table[i][j]<=8)
 				block[i][j].setText(Integer.toString(table[i][j]));
 					if(table[i][j]==999){
 						block[i][j].setIcon(pic);
 						block[i][j].setDisabledIcon(pic);
 						block[i][j].setEnabled(true);
 						block[i][j].setBackground(Color.RED);
 						restart(2);
 					}	
 					if(table[i][j]==0&&visible[i][j]==1)
 					zeroAction();
 					checkEnd();
                                     }
                                }
                              }
                 if(e.getSource()==reset)
                      {
                     restart(0);
                       }
         }
 public void zeroAction()
 	{
 		int u=0;
 		int i=0,j=0;
 		while(u<( numOfTable)){		
 		 	for(i=0;i< numOfTable;i++){
 			for(j=0;j< numOfTable;j++){
 			  	 if(visible[i][j]==1&&table[i][j]==0){
 					if(((i-1)>=0)&&((j-1)>=0)){
 						visible[i-1][j-1]=1;
 						block[i-1][j-1].setEnabled(false);
 						if(table[i-1][j-1]!=0)
 						block[i-1][j-1].setText(Integer.toString(table[i-1][j-1]));
                                                 block[i-1][j-1].setIcon(null);
                                                 MarkOpen[i-1][j-1] = 1;
 					}
 					if((i-1)>=0){
 						visible[i-1][j]=1;
 						block[i-1][j].setEnabled(false);
 						if(table[i-1][j]!=0)
 							block[i-1][j].setText(Integer.toString(table[i-1][j]));
                                                 block[i-1][j].setIcon(null);
                                                 MarkOpen[i-1][j] = 1;
 					}
 					if((i-1)>=0&&(j+1)< numOfTable){
 						visible[i-1][j+1]=1;
 						block[i-1][j+1].setEnabled(false);
 						if(table[i-1][j+1]!=0)
 							block[i-1][j+1].setText(Integer.toString(table[i-1][j+1]));
                                                 block[i-1][j+1].setIcon(null);
                                                 MarkOpen[i-1][j+1] = 1;
                                                 
 					}
 					if((j-1)>=0){
 						visible[i][j-1]=1;
 						block[i][j-1].setEnabled(false);
 						if(table[i][j-1]!=0)
 							block[i][j-1].setText(Integer.toString(table[i][j-1]));
                                                 block[i][j-1].setIcon(null);
                                                 MarkOpen[i][j-1] = 1;
 					}
 					
 					if((j+1)< numOfTable){
 						visible[i][j+1]=1;
 						block[i][j+1].setEnabled(false);
 						if(table[i][j+1]!=0)
 							block[i][j+1].setText(Integer.toString(table[i][j+1]));
                                                 block[i][j+1].setIcon(null);
                                                 MarkOpen[i][j+1] = 1;
 					}
 					if((i+1)< numOfTable&&(j-1)>=0){
 						visible[i+1][j-1]=1;
 						block[i+1][j-1].setEnabled(false);
 						if(table[i+1][j-1]!=0)
 							block[i+1][j-1].setText(Integer.toString(table[i+1][j-1]));
                                                 block[i+1][j-1].setIcon(null);
                                                 MarkOpen[i+1][j-1] = 1;
 					}
 						
 					if((i+1)< numOfTable){
 						visible[i+1][j]=1;
 						block[i+1][j].setEnabled(false);
 						if(table[i+1][j]!=0)
 							block[i+1][j].setText(Integer.toString(table[i+1][j]));
                                                 block[i+1][j].setIcon(null);
                                                 MarkOpen[i+1][j] = 1;
 					}
 						
 					if((i+1)< numOfTable&&(j+1)< numOfTable){
 						visible[i+1][j+1]=1;
 						block[i+1][j+1].setEnabled(false);
 						if(table[i+1][j+1]!=0)
 							block[i+1][j+1].setText(Integer.toString(table[i+1][j+1]));
                                                 block[i+1][j+1].setIcon(null);
                                                 MarkOpen[i+1][j+1] = 1;
 					}					
 				}
 			}
 			}
 		   u++;	
 		}
 	}
 	
 public void checkEnd(){
 		
 		checkIsAllCorrect=0;
 		
 		for(int i=0;i<9;i++){
 			for(int j=0;j<9;j++){
 				if(visible[i][j]==1&&table[i][j]!=8)
 					checkIsAllCorrect++;	
 			}
 		}
 		checkIsOpened=checkIsAllCorrect;
 		
 		if(checkIsAllCorrect==(( numOfTable* numOfTable)- numOfBomb)){
                      for(int i=0;i<9;i++){
 		for(int j=0;j<9;j++){
 			if(table[i][j]==999){
 						block[i][j].setIcon(pic);
                         }
                        //winGame
                         JOptionPane.showMessageDialog(tablePanel,"   Finish!!  --   Your time: 0"+j+":0"+k); 
                        if(k<=9)
     			stat.setText("   Finish!!  --   Your time: 0"+j+":0"+k);
     		else
     			stat.setText("   Finish!!  --   Your time: 0"+j+":"+k);
                         }
                      }
                      restart(1);
                 }
 	}
 	
 	public void restart(int end){
 		for(int m=0;m<9;m++){
 			for(int n=0;n<9;n++){
 					visible[m][n]=1;
 			}
 		}
 		if(end==0)
 			newGame();
 		else if(end==1){
 			for(int i=0;i<9;i++){
 				for(int j=0;j<9;j++){
 					if(table[i][j]==999){
 						block[i][j].setBackground(Color.green);
 						block[i][j].setEnabled(false);
 						block[i][j].setIcon(pic);
 						block[i][j].setDisabledIcon(pic);
 					}
 				}
 			}
 		}
 		else if(end==2){
 			for(int i=0;i<9;i++){
 				for(int j=0;j<9;j++){
 					block[i][j].setEnabled(false);
 					if(table[i][j]>0&&table[i][j]<8)
 						block[i][j].setText(Integer.toString(table[i][j]));
 					else if(table[i][j]==0)block[i][j].setText("");
 					else{
 						block[i][j].setIcon(pic);
 						block[i][j].setDisabledIcon(pic);
                     
 					}
 				}	
 			}
                         //lose
                         for(int i=0;i<9;i++){
                         for(int j=0;j<9;j++){
                             MarkOpen[i][j] =1;
                                 blockMark[i][j] = 1;
                             }
                                                  }
                          JOptionPane.showMessageDialog(tablePanel,"   You lose!!  --   55555");
 		}
 	}
 	
 	public void newGame(){
 		//add all Button 
 		for(int i=0;i<9;i++){
 			for(int j=0;j<9;j++){
 				visible[i][j]=0;
 				block[i][j].setEnabled(true);
 				block[i][j].setText("");
 				tablePanel.remove(block[i][j]);
 				block[i][j]  = new JButton("");
 				tablePanel.add(block[i][j]);
 				block[i][j].addActionListener(this);
                                 block[i][j].addMouseListener(this);
                                 MarkOpen[i][j] =0;
                                 blockMark[i][j] = 0;
 			}
 		}
 			repaint();
 		j=0;
 		k=0;	
 		checkIsOpened=0;	
                 time.setText("    time :  00:00");
                 
 		RandomizeTable ran = new RandomizeTable( numOfBomb, numOfTable);
 		ran.takeNumber();
                 
 		table = ran.getTableArray();
                 
 		System.out.println("");
 		System.out.println("");
 		for(int i=0;i<9;i++){
 		for(int j=0;j<9;j++){
 			if(table[i][j]==999)
 				System.out.print("X  ");
 			else
 				System.out.print(table[i][j]+"  ");
 		}
 		System.out.println("");
 		System.out.println("");
 		}
 		
 	}
         public void run(){
 		
 		while(startTimer!=0)
 		{
 			if(k==60){
    				k=0;j++;
    			}
 	   		if(k<=9)
     			time.setText("    time :  0"+j+":0"+k);	
     		else
     			time.setText("    time :  0"+j+":"+k);
     			
 			try{
 				Thread.sleep(1000);
 			}
 			catch(Exception e){}
 			
 			GregorianCalendar timer = new GregorianCalendar();
 		
    			k++;
 		}
 	}
 
     @Override
     public void mouseClicked(MouseEvent e) {
         for(int i=0;i<9;i++){
 		for(int j=0;j<9;j++){
 			if(e.getSource()== block[i][j]&&e.getButton()==MouseEvent.BUTTON3)
                             {
                                 if(blockMark[i][j] == 0)
                                     {
                                         if(MarkOpen[i][j] == 0&&blockMark[i][j]!=1){
                                          block[i][j].setIcon(pic2);
 					 block[i][j].setEnabled(true);
                                          blockMark[i][j] = 1;
                                          MarkOpen[i][j] = 1;
                                         }else
                                         { 
                                             
                                         }
                                     
                                 }else
                                 {
                                    if(checkIsOpened == 0&&MarkOpen[i][j] == 1){
                                     block[i][j].setIcon(null);
                                     blockMark[i][j] = 0;
                                     MarkOpen[i][j] = 0;
                                     }
                                     else
                                     {
                                    }
                                 }           
                             }
                        if(e.getSource()== block[i][j]&&e.getButton()==MouseEvent.BUTTON1)
                        {block[i][j].setIcon(null);}
 		}
 	}
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
     }
 }
