 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author headb
  */
 import java.beans.*;
 import java.io.Serializable;
 import javax.swing.JPanel;
 import java.awt.Graphics;
 import java.awt.Color;
 import java.awt.event.MouseEvent;
 import javax.swing.event.MouseInputAdapter;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 //import java.lang.Math;
 import java.util.Vector;
 
 public class SandpilePanel extends JPanel implements ActionListener, Serializable{
 	public static final int ADD_VERT_STATE = 0;
 	public static final int DEL_VERT_STATE = 1;
 	public static final int ADD_EDGE_STATE = 2;
 	public static final int DEL_EDGE_STATE = 3;
 	public static final int ADD_UNDI_EDGE_STATE = 4;
 	public static final int DEL_UNDI_EDGE_STATE = 5;
 	public static final int ADD_SAND_STATE = 6;
 	public static final int DEL_SAND_STATE = 7;
 	public static final int MAKE_GRID_STATE = 8;
 	
 	private boolean repaint = true;
 	private boolean labels = true;
 	private boolean color = true;
 	
     private SandpileGraph sg;
     
     static final int VERT_RADIUS = 10;
     static final Color[] SAND_COLOR = {Color.gray ,  Color.blue, Color.cyan ,Color.green, Color.red, Color.orange, Color.yellow};
 	static final Color[] SAND_MONOCHROME = {new Color(25,25,25) , new Color(50,50,50), new Color(100,100,100), new Color(150,150,150), new Color(200,200,200), new Color(225,225,225), new Color(255,255,255)};
 	
     
     Vector<Integer[]> vertexData;
     Vector<Integer[]> edges;
     
     private int selectedVertex;
 	
 	
 	public SandpilePanel() {
 		initWithSandpileGraph(new SandpileGraph());
 	}
     
     public SandpilePanel(SandpileGraph sg) {
 		initWithSandpileGraph(sg);
     }
 	
 	private void initWithSandpileGraph(SandpileGraph sg) {
 				//this.curState = ADD_VERT_STATE;
 		
         this.sg = sg;
         vertexData = new Vector<Integer[]>();
         edges = new Vector<Integer[]>();
         //Integer[] pos1 = {20,20,0};
         //Integer[] pos2 = {20,50,0};
         //vertexData.add(pos1);
         //vertexData.add(pos2);
         
         //Integer[] e = {0,1};
         //edges.add(e);
         
         selectedVertex = -1;
         addMouseListener(new MouseInputAdapter() {
             public void mouseReleased(MouseEvent evt) {/*
                 System.out.println("Got mouse click");
                 int x = evt.getX();
                 int y = evt.getY();
                 int touchVert = touchingVertex(x,y);
                 if(touchVert<0){
                     if(selectedVertex>=0) {
                         selectedVertex = -1;
                     }else{
                         addVertex(x,y);
                     }
                 }else{
                     if(selectedVertex<0) {
                         selectedVertex = touchVert;
                     } else {
                         if(touchVert==selectedVertex) {
                             if(evt.getButton() == MouseEvent.BUTTON1) {
                                 addSand(selectedVertex, 1);
                             }else if(evt.getButton() == MouseEvent.BUTTON3){
                                 addSand(selectedVertex, -1);
                             }
                         }else{
                             addEdge(selectedVertex,touchVert);
                         }
                     }
                 }*/
 				
 				int x = evt.getX();
                 int y = evt.getY();
                 int touchVert = touchingVertex(x,y);
 				if(touchVert<0) {
 					selectedVertex = -1;
 				}
 				/*
 				switch(curState) {
 					case ADD_VERT_STATE:
 						addVertMouseListener(evt);
 						break;
 					case DEL_VERT_STATE:
 						delVertMouseListener(evt);
 						break;
 					case ADD_EDGE_STATE:
 						addEdgeMouseListener(evt);
 						break;
 					case DEL_EDGE_STATE:
 						delEdgeMouseListener(evt);
 						break;
 					case ADD_UNDI_EDGE_STATE:
 						addUndiEdgeMouseListener(evt);
 						break;
 					case DEL_UNDI_EDGE_STATE:
 						delUndiEdgeMouseListener(evt);
 						break;
 					//case ADD_SAND_STATE:
 					//	addSandMouseListener(evt);
 					//	break;
 					//case DEL_SAND_STATE:
 					//	delSandMouseListener(evt);
 					//	break;
 					//case MAKE_GRID_STATE:
 					//	makeGrid(gridRows, gridCols, x,y);
 					//	break;
 				}*/
                 repaint();
             }
 			
 			public void mouseDragged(MouseEvent evt) {
 				System.out.println("Mouse drag");
 				int x = evt.getX();
 				int y = evt.getY();
 				
 				int touchVert = touchingVertex(x,y);
 				if( touchVert>=0) {
 					vertexData.get(touchVert)[0] = x;
 					vertexData.get(touchVert)[1] = y;
 				}
 			}
 			
         });
 	}
 	
 	public void setRepaint(boolean val) {
 		repaint = val;
 		repaint();
 	}
 	
 	public void setColor(boolean val) {
 		color = val;
 		repaint();
 	}
 	
 	public void setLabels(boolean val) {
 		labels = val;
 		repaint();
 	}
     
     public void actionPerformed( ActionEvent evt) {
         update();
     }
     
     public void update() {
         sg.update();
         repaint();
     }
 	
 	/**
 	 * @Override
 	 */
     public void paintComponent(Graphics g) {
 		if(!repaint)	return;
 		//System.out.println("Repaint");
         super.paintComponent(g);
         
         updateSandCounts();
         
         
         for(Integer[] e : edges) {
             Integer[] pos1 = vertexData.get(e[0]);
             Integer[] pos2 = vertexData.get(e[1]);
             g.setColor(Color.white);
             g.drawLine(pos1[0], pos1[1], pos2[0], pos2[1]);
             g.setColor(Color.pink);
 			if(labels)
 				g.drawString(String.valueOf(e[2]), (int)(pos1[0]+0.8*(pos2[0]-pos1[0])), (int)(pos1[1]+0.8*(pos2[1] - pos1[1])));
             
         }
         for(int i = 0; i<vertexData.size(); i++) {
 			
             Integer[] v = vertexData.get(i);
 			int radius = VERT_RADIUS;
 			if(sg.degree(i)>0 && sg.degree(i)>v[2])
 				radius = (int)(((float)v[2])/sg.degree(i) * VERT_RADIUS);
             int colorNum = Math.max(0,Math.min(v[2],SAND_COLOR.length-1));
 			if(color){
 				g.setColor(SAND_COLOR[colorNum]);
 			}else{
 				g.setColor(SAND_MONOCHROME[colorNum]);
 			}
 			//g.setColor(new Color(+64));
             g.fillOval(v[0]-radius, v[1]-radius, radius*2, radius*2);
             if(i==selectedVertex){
                 g.setColor(Color.cyan);
                 g.drawOval(v[0]-VERT_RADIUS, v[1]-VERT_RADIUS, VERT_RADIUS*2, VERT_RADIUS*2);
             }
             g.setColor(Color.black);
 			if(labels)
 				g.drawString(String.valueOf(v[2]), v[0]-VERT_RADIUS/2, v[1]+VERT_RADIUS/2);
         }
         
     }
 	
 	public void addVertexControl(int x, int y) {
 		int touchVert = touchingVertex(x,y);
 		if(touchVert<0){
 	        if(selectedVertex>=0) {
 			    selectedVertex = -1;
             }else{
 				//System.out.println("Adding vertex "+x+" "+y);
 				addVertex(x,y);
             }
         }else{
 			selectedVertex = touchVert;
 		}
 	}
 	
 	public void delVertexControl(int x, int y) {
 		int touchVert = touchingVertex(x,y);
 		if(touchVert>=0){
 			//System.out.println("Del vertex "+x+" "+y);
 			delVertex(touchVert);
 		}else{
 			selectedVertex = -1;
 		}
 	
 	}
 	
 	public void addEdgeControl(int x, int y, int weight) {
 		int touchVert = touchingVertex(x,y);
 		if(touchVert>=0) {
 			if(selectedVertex<0) {
 				selectedVertex = touchVert;
 			}else if(touchVert!=selectedVertex){
 				addEdge(selectedVertex, touchVert, weight);
 			}
 		}else{
 			selectedVertex=-1;
 		}
 	}
 	
 	public void delEdgeControl(int x, int y, int weight) {
 		int touchVert = touchingVertex(x,y);
 		if(touchVert>=0) {
 			if(selectedVertex<0) {
 				selectedVertex = touchVert;
 			}else if(touchVert!=selectedVertex){
 				delEdge(selectedVertex, touchVert, weight);
 			}
 		}else{
 			selectedVertex = -1;
 		}
 	}
 	
 	public void addUndiEdgeControl(int x, int y, int weight) {
 		int touchVert = touchingVertex(x,y);
 		if(touchVert>=0) {
 			if(selectedVertex<0) {
 				selectedVertex = touchVert;
 			}else if(touchVert!=selectedVertex){
 				addEdge(selectedVertex, touchVert, weight);
 				addEdge(touchVert, selectedVertex, weight);
 			}
 		}
 	}
 	
 	public void delUndiEdgeControl(int x, int y, int weight) {
         int touchVert = touchingVertex(x,y);
 		if(touchVert>=0) {
 			if(selectedVertex<0) {
 				selectedVertex = touchVert;
 			}else if(touchVert!=selectedVertex){
 				delEdge(selectedVertex, touchVert, weight);
 				delEdge(touchVert, selectedVertex, weight);
 			}
 		}
 	}
 	
 	public void addSandControl(int x, int y, int amount) {
 		int touchVert = touchingVertex(x,y);
 		if(touchVert>=0) {
 			selectedVertex = touchVert;
 			addSand(touchVert,amount);
 		}
 	}
 	
 	public void makeGrid2(int rows, int cols, int x, int y, int nBorder, int sBorder, int eBorder, int wBorder) {
 		int gridSpacing = VERT_RADIUS*2;
 		//int curVertDataSize = vertexData.size();
 		
 		int[][] gridRef = new int[rows][cols];
 		int[] nBorderRef = new int[cols];
 		int[] sBorderRef = new int[cols];
 		int[] eBorderRef = new int[rows];
 		int[] wBorderRef = new int[rows];
 		
 		//create vertices
 		for(int i=0; i<rows; i++) {
 			for(int j=0; j<cols; j++){
 				gridRef[i][j]=vertexData.size();
 				addVertex(x+j*gridSpacing, y+i*gridSpacing);
 			}
 		}
 		
 		for(int i=0; i<cols; i++) {
 			if(nBorder<2){
 				nBorderRef[i]=vertexData.size();
 				addVertex(x+i*gridSpacing, y-gridSpacing);
 			}
 			if(sBorder<2){
 				sBorderRef[i]=vertexData.size();
 				addVertex(x+i*gridSpacing, y+(rows)*gridSpacing);
 			}
 				
 		}
 		
 		for(int i=0; i<rows; i++) {
 			if(wBorder<2){
 				wBorderRef[i]=vertexData.size();
 				addVertex(x-gridSpacing, y+i*gridSpacing);
 			}
 			if(eBorder<2){
 				eBorderRef[i]=vertexData.size();
 				addVertex(x+(cols)*gridSpacing, y+i*gridSpacing);
 			}	
 		}
 		
 		//create edges
 		for(int i=0; i<rows; i++) {
 			for(int j=0; j<cols; j++){
 				if(i==0) {
 					if(nBorder==0){
 						addEdge(gridRef[i][j], nBorderRef[j],1 );
 					}else if(nBorder==1) {
 						addEdge(gridRef[i][j], nBorderRef[j],1 );
 						addEdge( nBorderRef[j], gridRef[i][j],1 );
 					}
 				}else{
 					addEdge(gridRef[i][j], gridRef[i-1][j] );
 				}
 				
 				if(i==rows-1){
 					if(sBorder==0){
 						addEdge(gridRef[i][j], sBorderRef[j],1 );
 					}else if(sBorder==1) {
 						addEdge(gridRef[i][j], sBorderRef[j],1 );
 						addEdge( sBorderRef[j], gridRef[i][j],1 );
 					}
 				}else{
 					addEdge(gridRef[i][j], gridRef[i+1][j] );
 				}
				if(j==cols-1) {
 					if(eBorder==0){
 						addEdge(gridRef[i][j], eBorderRef[i],1 );
 					}else if(eBorder==1) {
 						addEdge(gridRef[i][j], eBorderRef[i],1 );
 						addEdge( eBorderRef[i], gridRef[i][j],1 );
 					}
 				}else{
					addEdge(gridRef[i][j], gridRef[i][j+1] );
 				}
 				
				if(j==0){
 					if(wBorder==0){
 						addEdge(gridRef[i][j], wBorderRef[i],1 );
 					}else if(wBorder==1) {
 						addEdge(gridRef[i][j], wBorderRef[i],1 );
 						addEdge( wBorderRef[i], gridRef[i][j],1 );
 					}
 				}else{
					addEdge(gridRef[i][j], gridRef[i][j-1] );
 				}
 				
 			}
 		}
 	}
 	
 	public void makeGrid(int rows, int cols, int x, int y, int nBorder, int sBorder, int eBorder, int wBorder) {
 		int gridSpacing = VERT_RADIUS*2;
 		
 		int curVertDataSize = vertexData.size();
 		//note: curVertDataSize + i*cols + j will give the index of the i,jth
 		//vertex in the grid
 		for(int i = 0; i<rows+2; i++) {
 			if(nBorder==2&&i==0)
 				continue;
 			if(sBorder==2&&i==rows+1)
 				continue;
 			
 			for(int j = 0; j<cols+2; j++) {
 				if(wBorder==2&&j==0)
 					continue;
 				if(eBorder==2&&j==rows+1)
 					continue;
 				if((i==0 && j==0) || (i==rows+1 && j==cols+1) || (i==0 && j==cols+1) || (i==rows+1 && j==0) )
 					continue;
 				addVertex(x+j*gridSpacing, y+i*gridSpacing);
 			}
 		}
 		
 		int colSizeAdjust = 0;
 		int westAdjust = 0;
 		if(eBorder<2)	colSizeAdjust++;
 		if(wBorder<2){
 			colSizeAdjust++;
 			westAdjust++;
 		}
 		int indexAdjust = curVertDataSize + cols+westAdjust;
 		if(nBorder ==2)
 			indexAdjust=curVertDataSize;
 		for(int i = 0; i<rows; i++) {
 			for(int j = 0; j<cols; j++) {
 				if(i==0) {
 					if(nBorder<2){
 						addEdge( indexAdjust + (i)*(cols+colSizeAdjust)+j, curVertDataSize+j);
 					}
 					if(nBorder==1){
 						addEdge( curVertDataSize+j, indexAdjust + (i)*(cols+colSizeAdjust)+j);
 					}
 				}else{
 					addEdge( indexAdjust + (i)*(cols+colSizeAdjust)+j, indexAdjust + (i-1)*(cols+colSizeAdjust)+j);
 							
 				}
 				if(i==rows-1){
 					if(sBorder<2){
 						addEdge( indexAdjust + (i)*(cols+colSizeAdjust)+j, indexAdjust-1 + rows*(cols+colSizeAdjust)+j);
 					}
 					if(sBorder==1){
 						addEdge(indexAdjust-1 + rows*(cols+colSizeAdjust)+j, indexAdjust + (i)*(cols+colSizeAdjust)+j);
 					}
 				}else {
 					addEdge( indexAdjust + (i)*(cols+colSizeAdjust)+j, indexAdjust + (i+1)*(cols+colSizeAdjust)+j);
 					
 				}
 				if(j>0){
 					addEdge( indexAdjust + (i)*(cols+colSizeAdjust)+j, indexAdjust + (i)*(cols+colSizeAdjust)+j-1);
 								
 				}else{
 					if(wBorder<2){
 						addEdge( indexAdjust + (i)*(cols+colSizeAdjust)+j, indexAdjust + (i)*(cols+colSizeAdjust)+j-1);
 					}
 					if(wBorder==1){
 						addEdge( indexAdjust + (i)*(cols+colSizeAdjust)+j-1, indexAdjust + (i)*(cols+colSizeAdjust)+j);
 					}
 				}
 				if(j<rows-1){
 					addEdge( indexAdjust + (i)*(cols+colSizeAdjust)+j, indexAdjust + (i)*(cols+colSizeAdjust)+j+1);
 					
 				}else{	
 					if(eBorder<2)
 						addEdge( indexAdjust + (i)*(cols+colSizeAdjust)+j, indexAdjust + (i)*(cols+colSizeAdjust)+j+1);
 					if(eBorder==1)
 						addEdge( indexAdjust + (i)*(cols+colSizeAdjust)+j+1, indexAdjust + (i)*(cols+colSizeAdjust)+j);
 				}
 			}
 			
 		}
 		
 	}
 	
 	public void setControlState(int controlState){
 		//curState = controlState;
 	}
     
     public void updateSandCounts() {
         for(int i = 0; i<vertexData.size(); i++) {
             vertexData.get(i)[2]=sg.getSand(i);
         }
     }
 	
 	public void maxStableConfig() {
 		for(int i = 0; i<vertexData.size(); i++) {
 			if(sg.degree(i)>0){
 				vertexData.get(i)[2] = sg.degree(i)-1;
 				sg.setSand(i, vertexData.get(i)[2]);
 			}
 		}
 		repaint();
 	}
 	
 	public void clearSand() {
 		for(int i = 0; i<vertexData.size(); i++) {
 			vertexData.get(i)[2] = 0;
 			sg.setSand(i, 0);
 		}
 		repaint();
 	}
     
     public void addVertex(int x, int y) {
         sg.addVertex();
         Integer[] newPos = {x,y,0};
         vertexData.add(newPos);
         repaint();
     }
 	
 	public void delVertex(int v) {
 		vertexData.remove(v);
 		sg.removeVertex(v);
 		for(int i = 0; i<edges.size(); i++){
 			if( (edges.get(i)[0] == v) || (edges.get(i)[1] == v)){
 				edges.remove(i);
 				i--;
 			}			
 		}
 		for(int i = 0; i<edges.size(); i++){
 			if(edges.get(i)[0]>v){
 				edges.get(i)[0]--;
 			}
 			if(edges.get(i)[1]>v){
 				edges.get(i)[1]--;
 			}
 			
 		}
 	}
 	
 	void addEdge(int originVert, int destVert) {
         addEdge(originVert, destVert, 1);
     }
 	
     public void addEdge(int originVert, int destVert, int weight) {
         sg.addEdge(originVert,destVert, weight);
         
         for(Integer[] e : edges) {
             if( e[0] == originVert && e[1] == destVert) {
                 e[2]+=weight;
                 return;
             }
         }
 		Integer[] newEdge = {originVert, destVert, weight};
         edges.add(newEdge);
         repaint();
     }
 	
 	public void delEdge(int originVert, int destVert) {
 		for(int i =0; i<edges.size(); i++) {
 			if(edges.get(i)[0] == originVert && edges.get(i)[1] == destVert){
 				sg.removeEdge(originVert, destVert);
 				edges.get(i)[2]--;
 				if(edges.get(i)[2]<1)
 					edges.remove(i);
 				return;
 			}
 		}
 	}
 	
 	public void delEdge(int originVert, int destVert, int weight) {
 		for(int i =0; i<edges.size(); i++) {
 			if(edges.get(i)[0] == originVert && edges.get(i)[1] == destVert){
 				sg.removeEdge(originVert, destVert,weight);
 				edges.get(i)[2]-=weight;
 				if(edges.get(i)[2]<1)
 					edges.remove(i);
 				return;
 			}
 		}
 	}
 	
 	public void addSandEverywhere(int amount) {
 		for(int i=0; i<vertexData.size(); i++) {
 			addSand(i,amount);
 		}
 		repaint();
 	}
     
     public void addSand(int vert, int amount) {
         sg.addSand(vert,amount);
         vertexData.get(vert)[2]+=amount;
     }
     
     private int touchingVertex(int x, int y) {
         for( int i = 0; i<vertexData.size(); i++ ) {
             Integer[] v = vertexData.get(i);
             if(Math.sqrt((x-v[0])*(x-v[0]) + (y-v[1])*(y-v[1]))<=VERT_RADIUS) {
                 return i;
             }
         }
         return -1;
     }
 }
