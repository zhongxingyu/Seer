 import java.awt.event.*;
 import java.awt.*;
 import javax.swing.*;
 import java.util.ArrayList;
 
 public class Seeker implements ActionListener{
  Board parent;
  int[] position = new int[2];
  Timer timer;
  int type = 0;
  int curTarget = 1;
 
  int[] phantomPosition = new int[2];
  ArrayList<Integer> tempPath = new ArrayList<Integer>();
  ArrayList<Integer> path = new ArrayList<Integer>();
  
  public Seeker(int[] positiono, Board parento, int typeo){
   position=positiono;
   parent=parento;
   type=typeo;
   timer = new Timer(141,this);
   timer.start();
  }
 
  public void actionPerformed(ActionEvent e){
   int distance1, distance2;
   distance1 = Math.abs(position[0]-parent.positionX1);
   distance1 = distance1+Math.abs(position[1]-parent.positionY1);
   distance2 = Math.abs(position[0]-parent.positionX2);
   distance2 = distance2+Math.abs(position[1]-parent.positionY2);
   if (parent.mode==2 || parent.mode==3){
    if (!parent.player1 && !parent.player2){
    } else if (!parent.player1){
     distance1=200;
    } else if (!parent.player2){
     distance2=200;
    }
   }
   if (distance1<distance2){
    curTarget=1;
    tryMove(new int[]{parent.positionX1,parent.positionY1});
   } else if (distance2<distance1){
    curTarget=2;
    tryMove(new int[]{parent.positionX2,parent.positionY2});
   } else {
    if (Math.random()>.5){
     curTarget=1;
     tryMove(new int[]{parent.positionX1,parent.positionY1});
    } else {
     curTarget=2;
     tryMove(new int[]{parent.positionX2,parent.positionY2});
    }
   }
  }
 
  public void tryMove(int[] target){
   path = new ArrayList<Integer>();
   for (int i=0; i<100; i++){
    path.add(0);
   }
   if (type==0){ //tries to push down
    if (position[0]==target[0] && position[1]==target[1]-1){
   } else if (target[1]-1>=0 && parent.board[target[1]-1][target[0]]==1){
    } else if (position[0]==target[0] && position[1]==target[1]+1){
     if (Math.random()>.49){
      target[0]--;
      target[1]++;
     } else {
      target[0]++;
      target[1]++;
     }
   } else if ((position[0]==target[0]-1 && position[1]==target[1]+1) || (position[0]==target[0]-1 && position[1]==target[1]) && parent.board[target[1]][target[0]-1]!=1 && parent.board[target[1]-1][target[0]-1]!=1){
     target[0]--;
     target[1]--;
   } else if ((position[0]==target[0]+1 && position[1]==target[1]+1) || (position[0]==target[0]+1 && position[1]==target[1]) && parent.board[target[1]][target[0]+1]!=1 && parent.board[target[1]-1][target[0]+1]!=1){
     target[0]++;
     target[1]--;
    } else {
     target[1]--;
    }
   } else if (type==1){ //tries to push right
    if (position[0]==target[0]-1 && position[1]==target[1]){
    } else if (target[0]-1<0){
    } else if (target[0]-1>0 && parent.board[target[1]][target[0]-1]==1){//Think had an error here
    } else if ((position[0]==target[0]+1 && position[1]==target[1]+1) || (position[0]==target[0] && position[1]==target[1]+1)){
     target[0]--;
     target[1]++;
    } else if ((position[0]==target[0]+1 && position[1]==target[1]-1) || (position[0]==target[0] && position[1]==target[1]-1)){
     target[0]--;
     target[1]--;
    } else {
     target[0]--;
    }
   } else if (type==2){ //tries to push left
    if (position[0]==target[0]+1 && position[1]==target[1]){
    } else if (target[0]+1>=parent.boardWidth){
    } else if (target[0]+1<parent.boardWidth && parent.board[target[1]][target[0]+1]==1){//Think had an error here
    } else if ((position[0]==target[0]-1 && position[1]==target[1]+1) || (position[0]==target[0] && position[1]==target[1]+1)){
     target[0]++;
     target[1]++;
    } else if ((position[0]==target[0]-1 && position[1]==target[1]-1) || (position[0]==target[0] && position[1]==target[1]-1)){
     target[0]++;
     target[1]--;
    } else {
     target[0]++;
    }
   }
   for (int i=0; i<30; i++){
    pathSeek(target);
   }
   try {
    if (path.get(0)==0){
     path.add(0,3);
    }
   } catch (Exception e){
    path.add(3);
    //System.out.println("Got switched to 0");
   }
   if (position[0]==target[0] && position[1]==target[1]){
    path.add(0,3);
   }
   if (path.get(0)==1){
    //System.out.println("UP");
    if (curTarget==1){
    if (position[1]-1>0 && parent.player1){
      if (parent.positionX1==position[0] && parent.positionY1==position[1]-1){
       if (position[1]-2>1 && parent.board[position[1]-2][position[0]]!=1 && !(parent.positionX2==position[0] && parent.positionY2==position[1]-2 && parent.player2)){
        parent.piece1[parent.positionY1][parent.positionX1]=0;
        parent.positionY1=position[1]-2;
        parent.piece1[parent.positionY1][parent.positionX1]=1;
        position[1]--;
       }
      } else {
       position[1]--;
      }
     }
    } else if (curTarget==2){
    if (position[1]-1>0 && parent.player2){
      if (parent.positionX2==position[0] && parent.positionY2==position[1]-1){
       if (position[1]-2>1 && parent.board[position[1]-2][position[0]]!=1 && !(parent.positionX1==position[0] && parent.positionY1==position[1]-2 && parent.player2)){
        parent.piece2[parent.positionY2][parent.positionX2]=0;
        parent.positionY2=position[1]-2;
        parent.piece2[parent.positionY2][parent.positionX2]=1;
        position[1]--;
       }
      } else {
       position[1]--;
      }
     }
    }
   } else if (path.get(0)==2){
    //System.out.println("RIGHT");
    if (curTarget==1){
     if (position[0]+1<parent.boardWidth && parent.player1){
      if (parent.positionX1==position[0]+1 && parent.positionY1==position[1]){
       if (position[0]+2<parent.boardWidth && parent.board[position[1]][position[0]+2]!=1 && !(parent.positionX2==position[0]+2 && parent.positionY2==position[1] && parent.player2)){
        parent.piece1[parent.positionY1][parent.positionX1]=0;
        parent.positionX1=position[0]+2;
        parent.piece1[parent.positionY1][parent.positionX1]=1;
        position[0]++;
       }
      } else {
       position[0]++;
      }
     }
    } else if (curTarget==2){
     if (position[0]+1<parent.boardWidth && parent.player2){
      if (parent.positionX2==position[0]+1 && parent.positionY2==position[1]){
       if (position[0]+2<parent.boardWidth && parent.board[position[1]][position[0]+2]!=1 && !(parent.positionX1==position[0]+2 && parent.positionY1==position[1] && parent.player1)){
        parent.piece2[parent.positionY2][parent.positionX2]=0;
        parent.positionX2=position[0]+2;
        parent.piece2[parent.positionY2][parent.positionX2]=1;
        position[0]++;
       }
      } else {
       position[0]++;
      }
     }
    }
   } else if (path.get(0)==3){
    //System.out.println("DOWN");
    if (curTarget==1){
     if (position[1]+1<parent.boardHeight && parent.player1){
      if (parent.positionX1==position[0] && parent.positionY1==position[1]+1){
       if (position[1]+2<parent.boardHeight && parent.board[position[1]+2][position[0]]!=1 && !(parent.positionX2==position[0] && parent.positionY2==position[1]+2 && parent.player2)){
        parent.piece1[parent.positionY1][parent.positionX1]=0;
        parent.positionY1=position[1]+2;
        parent.piece1[parent.positionY1][parent.positionX1]=1;
        position[1]++;
       }
      } else {
       position[1]++; 
      }
     }
    } else if (curTarget==2){
     if (position[1]+1<parent.boardHeight && parent.player2){
      if (parent.positionX2==position[0] && parent.positionY2==position[1]+1){
       if (position[1]+2<parent.boardHeight && parent.board[position[1]+2][position[0]]!=1 && !(parent.positionX1==position[0] && parent.positionY1==position[1]+2 && parent.player1)){
        parent.piece2[parent.positionY2][parent.positionX2]=0;
        parent.positionY2=position[1]+2;
        parent.piece2[parent.positionY2][parent.positionX2]=1;
        position[1]++;
       }
      } else {
       position[1]++; 
      }
     }
    }
   } else if (path.get(0)==4){
    //System.out.println("LEFT");
    if (curTarget==1){
     if (position[0]-1>=0 && parent.player1){
      if (parent.positionX1==position[0]-1 && parent.positionY1==position[1]){
       if (position[0]-1>0 && parent.board[position[1]][position[0]-2]!=1 && !(parent.positionX2==position[0]-2 && parent.positionY2==position[1] && parent.player2)){
        parent.piece1[parent.positionY1][parent.positionX1]=0;
        parent.positionX1=position[0]-2;
        parent.piece1[parent.positionY1][parent.positionX1]=1;
        position[0]--;
       }
      } else {
       position[0]--;
      }
     }
    } else if (curTarget==2){
     if (position[0]-1>=0 && parent.player2){
      if (parent.positionX2==position[0]-1 && parent.positionY2==position[1]){
       if (position[0]-1>=0 && parent.board[position[1]][position[0]-2]!=1 && !(parent.positionX1==position[0]-2 && parent.positionY1==position[1] && parent.player1)){
        parent.piece2[parent.positionY2][parent.positionX2]=0;
        parent.positionX2=position[0]-2;
        parent.piece2[parent.positionY2][parent.positionX2]=1;
        position[0]--;
       }
      } else {
       position[0]--;
      }
     }
    }
   }
   parent.repaint();
  }
 
  public void pathSeek(int[] target){
   tempPath = new ArrayList<Integer>();
   phantomPosition[0]=position[0];
   phantomPosition[1]=position[1];
   for (int k=1; k<100; k++){
    int[] direction = {1,2,3,4};
    int xChange = target[0]-phantomPosition[0];
    int yChange = target[1]-phantomPosition[1];
    if (xChange>0 && yChange<0){
     if (Math.random()>.6){
      direction = new int[]{1,2,4,3};
     } else {
      direction = new int[]{2,1,4,3};
     }
    } else if (xChange>0 && yChange>0){
     direction = new int[]{2,3,1,4};
    } else if (xChange<0 && yChange<0){
     if (Math.random()>.6){
      direction = new int[]{1,4,2,3};
     } else {
      direction = new int[]{4,1,2,3};
     }
    } else if (xChange<0 && yChange>0){
     direction = new int[]{4,3,1,2};
    } else if (xChange>0){
     direction = new int[]{2,1,3,4};
    } else if (xChange<0){
     direction = new int[]{4,1,3,2};
    } else if (yChange<0){
     direction = new int[]{1,2,4,3};
    } else if (yChange>0){
     direction = new int[]{3,2,4,1};
    }
    int temp;
    double random = Math.random();
    if (random<.7 && random>.35){
     temp = direction[1];
     direction[1]=direction[2];
     direction[2]=temp;
    } else if (random>.7 && random<.76){
     temp = direction[2];
     direction[2]=direction[3];
     direction[3]=temp;
    } else if (random>.76 && random<.82){
     temp = direction[1];
     direction[1]=direction[3];
     direction[3]=temp;
    } else if (random>.82 && random<.88){
     temp = direction[1];
     direction[1]=direction[0];
     direction[0]=temp;
    } else if (random>.88 && random<.94){
     temp=direction[2];
     direction[2]=direction[0];
     direction[0]=temp;
    } else if (random>.94){
     temp=direction[3];
     direction[3]=direction[0];
     direction[0]=temp;
    }
    for (int i=0; i<4; i++){
     if (attempt(direction[i])){
      tempPath.add(direction[i]);
      break;
     }
    }
    if (phantomPosition[0]==target[0] && phantomPosition[1]==target[1]){
     break;
    }
   }
   if (tempPath.size()<path.size()){
    path=tempPath;
   }
  }
 
  public boolean attempt(int direction){
   int[][] board = parent.board;
   if (direction==1){
    if ((phantomPosition[1]-1)>=1){
     if (board[phantomPosition[1]-1][phantomPosition[0]]!=1){
      phantomPosition[1]--;
      return true;
     }
    }
   } else if (direction==2){
    if ((phantomPosition[0]+1)<parent.boardWidth){
     if (board[phantomPosition[1]][phantomPosition[0]+1]!=1){
      phantomPosition[0]++;
      return true;
     }
    }
   } else if (direction==3){
    if ((phantomPosition[1]+1)<parent.boardHeight){
     if (board[phantomPosition[1]+1][phantomPosition[0]]!=1){
      phantomPosition[1]++;
      return true;
     }
    }
   } else if (direction==4){
    if ((phantomPosition[0]-1)>=0){
     if (board[phantomPosition[1]][phantomPosition[0]-1]!=1){
      phantomPosition[0]--;
      return true;
     }
    }
   }
   return false;
  }
  
  /*public void pathSeek(int[] target){
   for (int i=0; i<16; i++){
    while (true){
     
    }
   }
  }
 
  public int checkDirection(int direction){
   if (direction==1){
    if (parent.board[phantomPosition[0]][phantomPosition[1]-1]==1){
     return 100;
    } else if (targetQuadrant==0){
     return 0;
    } else if (targetQuadrant==1 || targetQuadrant==7){
     return 1;
    } else if (targetQuadrant==2 || targetQuadrant==6){
     return 2;
    } else if (targetQuadrant==3 || targetQuadrant==5){
     return 3;
    } else if (targetQuadrant==4){
     return 4;
    }
   } else if (direction==2){
    if (parent.board[phantomPosition[0]+1][phantomPosition[1]]==1){
     return 100;
    } else if (targetQuadrant==2){
     return 0;
    } else if (targetQuadrant==1 || targetQuadrant==3){
     return 1;
    } else if (targetQuadrant==0 || targetQuadrant==4){
     return 2;
    } else if (targetQuadrant==7 || targetQuadrant==5){
     return 3;
    } else if (targetQuadrant==6){
     return 4;
    }
   } else if (direction==3){
    if (parent.board[phantomPosition[0]][phantomPosition[1]+1]==1){
     return 100;
    } else if (targetQuadrant==4){
     return 0;
    } else if (targetQuadrant==3 || targetQuadrant==5){
     return 1;
    } else if (targetQuadrant==2 || targetQuadrant==6){
     return 2;
    } else if (targetQuadrant==1 || targetQuadrant==7){
     return 3;
    } else if (targetQuadrant==0){
     return 4;
    }
   } else if (direction==4){
    if (parent.board[phantomPosition[0]-1][phantomPosition[1]]==1){
     return 100;
    } else if (targetQuadrant==6){
     return 0;
    } else if (targetQuadrant==5 || targetQuadrant==7){
     return 1;
    } else if (targetQuadrant==4 || targetQuadrant==0){
     return 2;
    } else if (targetQuadrant==3 || targetQuadrant==1){
     return 3;
    } else if (targetQuadrant==2){
     return 4;
    }
   }
  }*/
 
 }
 
 
 
 
 
 
 
 
 
 
 
