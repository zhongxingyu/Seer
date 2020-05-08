 /***************************************************************************
  *                                                                         *
  *   This program is free software; you can redistribute it and/or modify  *
  *   it under the terms of the GNU General Public License as published by  *
  *   the Free Software Foundation; either version 2 of the License, or     *
  *   (at your option) any later version.                                   *
  *                                                                         *
  ***************************************************************************
  *
  * GraphicBoard.java
  *
  * Created on 10. Oktober 2004, 11:23
  */
 
 
 import java.awt.*;
 import java.awt.event.*;
 
 /** the visible version of board
  * @author  peter
  */
 public class GraphicBoard extends Board implements ActionListener
 { /**only ONE Board is visible, so we have to make the buttons static
    */
   static Button buttonBoard[][];
   static Button finish1,finish2,start1,start2;
   static boolean buttonInit=false;
 
   /** creates a new instance of the graphical version of our board
    */
   public GraphicBoard(ActionListener al,Player player1,Player player2)
   { super();    
     setActionListenerToBoard(this);
     startStack1.setActionListener(this);
     startStack2.setActionListener(this);
     finishStack1.setActionListener(this);
     finishStack2.setActionListener(this);
     removedCards1.setActionListener(this);
     removedCards2.setActionListener(this);
     if(!buttonInit) 
     { buttonInit=true;
       buttonBoard=new Button[5][];
       
       for(int i=0; i<5; i++)
       { buttonBoard[i]=new Button[5];
       }      
       for(int i=0; i<5; i++)
       { for(int j=0; j<5; j++)
         { buttonBoard[i][j]=init(i,j,al);
         }
       }
       finish1=init(Const.finish1,Const.yAll,al);
       finish2=init(Const.finish2,Const.yAll,al);
       start1=init(Const.start1,Const.yAll,al);
       start2=init(Const.start2,Const.yAll,al);      
       setBackground(Const.NORMAL_BACKGROUND, Const.starterFieldsColor1, 
                     Const.starterFieldsColor2);
     }
     else
     { for(int i=0; i<5; i++)
       { for(int j=0; j<5; j++)
         { buttonBoard[i][j].setLabel("");
         }
       }
       finish1.setLabel("");
       finish2.setLabel("");
       start1.setLabel("");
       start2.setLabel("");      
     }
     
     setPlayer(player1,player2);
     init(startStack1,player1);
     init(startStack2,player2);        
   }
   
   /** color1- main color; color2-starterfield1; color3-starterfield2
    */
   public void setBackground(Color c1,Color c2,Color c3)
   { for(int a=0; a<5; a++)
     { for(int b=0; b<5; b++)
       { setBackground(a,b,c1);      
       }
     }
     start1.setBackground(c1);
     start2.setBackground(c1);
     finish1.setBackground(c1);
     finish2.setBackground(c1);
     
     buttonBoard[0][0].setBackground(c2);
     buttonBoard[0][1].setBackground(c2);
     buttonBoard[1][0].setBackground(c2);    
     buttonBoard[4][4].setBackground(c3);
     buttonBoard[4][3].setBackground(c3);
     buttonBoard[3][4].setBackground(c3);    
   }
   
   public void setBackground(int x,int y,Color c)
   { if(y==Const.yAll) 
     { if(x==Const.finish1) finish1.setBackground(c);
       else if(x==Const.finish2) finish2.setBackground(c);
       else if(x==Const.start1) start1.setBackground(c);
       else if(x==Const.start2) start2.setBackground(c);      
     }
     else
     { buttonBoard[x][y].setBackground(c);
     }
   }
   
   public void setActionListenerToBoard(ActionListener action)
   { for(int a=0; a<5; a++)
     { for(int b=0; b<5; b++)
       { brett[a][b].setActionListener(action);
       }
     }    
   }    
   
   private Button init(int x,int y,ActionListener al)
   { Button button=new Button();
     button.addActionListener(al);
     button.setActionCommand(GraphicBoard.getActionCommand(x,y));
     return button;
   }
   
   public Panel getPanel()
   { //get the newest version of player settings
     setPlayer(player1,player2);
       
     Panel panel=new Panel();
     panel.setLayout(new GridLayout(5,5));
 
     //depends from boards orientation; swap x and y is neccessary, cause we
     //add the buttons line by line 
     for(int b=0; b<5; b++)
     { for(int a=0; a<5; a++)
       { panel.add(buttonBoard[a][b]);
       }
     }
     
     Panel p=new Panel();
     p.setLayout(new BorderLayout());
     p.add(start1,BorderLayout.SOUTH);
     p.add(start2,BorderLayout.NORTH);
     p.add(finish1,BorderLayout.WEST);
     p.add(finish2,BorderLayout.EAST);
     p.add(panel,BorderLayout.CENTER);
     
     return p;
   }
    
   public void setPlayer(Player o1,Player o2)
   { super.setPlayer(o1,o2);
     
     refresh(start1,o1.getColor());
     refresh(start2,o2.getColor());
     refresh(finish1,o1.getColor());
     refresh(finish2,o2.getColor());
   }
   
   private void refresh(Button b,Color c)
   { String str=b.getLabel();
     b.setForeground(c);
     b.setLabel(str);
   }
   
   public Color getPlayerColor(int no)
   { Color color;
     if(no==Const.NO_1) color=player1.getColor();
     else              color=player2.getColor();
     return color;
   }
   
   public void setStartStack1Label(String str)
   { setGraphics(start1, player1.getColor(),str);    
   }
   
   public void setStartStack2Label(String str)
   { setGraphics(start2, player2.getColor(),str);    
   }
   
   public void setFinishStack1Label(String str)
   { setGraphics(finish1, player1.getColor(),str);    
   }
   
   public void setFinishStack2Label(String str)
   { setGraphics(finish2, player2.getColor(),str);    
   }
   
   public void setStartStackLabel(String str,int no)
   { Button b;
     Color color=getPlayerColor(no);
     
     if(no==Const.NO_1) b=start1;
     else              b=start2;
       
     setGraphics(b,color,str);    
   }
   
   public void setFinishStackLabel(String str,int no)
   { Button b;
     Color color=getPlayerColor(no);
     
     if(no==Const.NO_1) b=finish1;
     else             b=finish2;
       
     setGraphics(b,color,str);    
   } 
     
   /** please make sure that x,y are valid
    */
   public void labelSet(String str, int x, int y,Color color)
   { setGraphics(buttonBoard[x][y],color,str);    
   }
   
   static public void setGraphics(Button b,Color c,String str)
   { b.setFont(new Font("Dialog",Font.BOLD,22));
    b.setForeground(c);    
     b.setLabel(str);
   }
   
   public Panel getStackPanel(int x,int y)
   { return brett[x][y].getStackPanel();
   }  
      
   public Board getBoard()
   { Board board=new Board(); 
     
     // do not clone the players!?
     board.setPlayer(player1,player2);
     
     for(int a=0; a<5; a++)
     { for(int b=0; b<5; b++)
       { board.brett[a][b]=brett[a][b].getClone();
       }
     }  
     
     board.startStack1=(StartField)startStack1.getClone(player1);
     board.startStack2=(StartField)startStack2.getClone(player2);
     board.finishStack1=(OwnerField)finishStack1.getClone(player1);
     board.finishStack2=(OwnerField)finishStack2.getClone(player2);
     board.removedCards1=(OwnerField)removedCards1.getClone(player1);
     board.removedCards2=(OwnerField)removedCards2.getClone(player2);     
     
     return board;
   }
 
   //BE SURE IF YOU CHANGE DELIMER , THAT getActionCommand works correctly
   static public String COORD_DEL=";";
   
   static public String getActionCommand(int x,int y)
   { return ""+x+COORD_DEL+y+COORD_DEL;
   }
   
   static public Punkt parseActionCommand(String cl)
   { int delimer1=cl.indexOf(COORD_DEL);
     if(delimer1==-1) throw new IndexOutOfBoundsException(" delimer not found");
     int a=Integer.parseInt(cl.substring(0,delimer1));
     int delimer2=cl.indexOf(COORD_DEL,delimer1+1);
     if(delimer2==-1) throw new IndexOutOfBoundsException(" delimer not found");
     int b=Integer.parseInt(cl.substring(delimer1+1,delimer2));
     
     return new Punkt(a,b);
   } 
     
   /** the fields can send a message via ActionListener to the Layouter=> here GraphicBoard
     */
   public void actionPerformed(ActionEvent ae) 
   { Field field=(Field)ae.getSource();
     String cl=ae.getActionCommand();
     int x=field.x, y=field.y;
       
     if(field instanceof OwnerField) //if(y==Const.yAll)
     { if(x==Const.start1 )      setStartStackLabel(cl,Const.NO_1);
       else if(x==Const.start2)  setStartStackLabel(cl,Const.NO_2);
       else if(x==Const.finish1) setFinishStackLabel(cl,Const.NO_1);
       else if(x==Const.finish2) setFinishStackLabel(cl,Const.NO_2);
     }
     else if(field instanceof CardField)//if(x>=0 && x<5 && y>=0 && y<5)        
     { 
       labelSet(cl,x,y,field.getColor());
     }
   } 
 }
 
 class Punkt
 { public int x,y;
   public Punkt(int x,int y)
   { this.x=x;
     this.y=y;
   }
   public int getX()
   { return x;
   }
   public int getY()
   { return y;
   }
   
   public String toString()
   { return "x="+x+" | y="+y;
   }
 }
