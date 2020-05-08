 package thesaurus.gui.canvas;
 
 import java.util.LinkedList;
 
 import javafx.event.EventHandler;
 import javafx.scene.canvas.Canvas;
 import javafx.scene.canvas.GraphicsContext;
 import javafx.scene.control.ScrollPane;
 import javafx.scene.control.ScrollPane.ScrollBarPolicy;
 import javafx.scene.input.MouseEvent;
 import javafx.scene.paint.Color;
 import javafx.scene.text.Font;
 import javafx.scene.text.TextAlignment;
 import thesaurus.gui.window.VisualisationRoot;
 import thesaurus.parser.Vertex;
 
 public class ViewGraph {
 	private static final int SYNONYM = 1;
 	private static final int ANTONYM = 0;
 	
 	private int windowWidth;
 	private int windowHeight;
 	private Vertex vertex;
 	private VisualisationRoot vr;
 	private Canvas graph;
 	private GraphicsContext gc;
 	private int xOffset = 0;
 	private int yOffset = 0;
 	
 	private int displaySynonyms = 1;
 	private int displayAntonyms = 1;
 	
 	public ViewGraph(int width, int height, Vertex vertex, VisualisationRoot vr, int displaySynonyms, int displayAntonyms){
 		windowWidth = width;
 		windowHeight = height;
 		this.vertex = vertex;
 		this.vr = vr;
 		this.displaySynonyms = displaySynonyms;
 		this.displayAntonyms = displayAntonyms;
 		
 		//Move window to right to support dual view better
 		if(windowWidth<500){
 			this.xOffset=windowWidth/2;
 		}
 		
 		//Centre origin node in canvas
 		xOffset = (int) (vertex.getPos().getX() - (windowWidth/2));
 		yOffset = (int) (vertex.getPos().getY() - (windowHeight/2));
 		
		
 		start();
 	}
 	
 	public ScrollPane returnGraph(){
 		ScrollPane sp = new ScrollPane();
 		sp.setPrefSize(windowWidth, windowHeight);
 		sp.setMaxSize(windowWidth, windowHeight);
 		sp.setHbarPolicy(ScrollBarPolicy.NEVER);
 		sp.setVbarPolicy(ScrollBarPolicy.NEVER);
 		sp.setStyle("-fx-background-color:transparent;");
 		sp.setContent(graph);
 		return sp;
 	}
 	
 	private void drawMainNode(Vertex v){
 		int nodeWidth = v.getWord().length() * 12;
 		
 		gc.setStroke(Color.BLACK);
 		gc.setFill(Color.rgb(176,220,247));
 		gc.setLineWidth(2);
 		gc.strokeOval((v.getPos().getX()-(nodeWidth/2)-xOffset),(v.getPos().getY()-13-yOffset), nodeWidth, 36);
 		gc.fillOval((v.getPos().getX()+1-(nodeWidth/2)-xOffset),(v.getPos().getY()-12-yOffset),nodeWidth-2,34);
 		gc.setFill(Color.BLACK);
 		gc.setFont(new Font(14));
 		gc.setTextAlign(TextAlignment.CENTER);
 		gc.fillText(v.getWord(), (v.getPos().getX()-xOffset), (v.getPos().getY()+10-yOffset));
 	}
 	
 	private void drawSynNode(Vertex v){
 		int nodeWidth = v.getWord().length() * 8;
 		
 		gc.setStroke(Color.BLACK);
 		gc.setFill(Color.rgb(191, 247, 176));
 		gc.setLineWidth(2);
 		gc.strokeOval((v.getPos().getX()-(nodeWidth/2)-xOffset),(v.getPos().getY()-10-yOffset), nodeWidth, 30);
 		gc.fillOval((v.getPos().getX()+1-(nodeWidth/2)-xOffset),(v.getPos().getY()-9-yOffset),nodeWidth-2,28);
 		gc.setFill(Color.BLACK);
 		gc.setFont(new Font(11));
 		gc.setTextAlign(TextAlignment.CENTER);
 		gc.fillText(v.getWord(), (v.getPos().getX()-xOffset), (v.getPos().getY()+9-yOffset));
 	}
 	
 	/*private void drawAntNode(Vertex v){
 		gc.setStroke(Color.RED);
 		gc.setFill(Color.WHITE);
 		gc.setLineWidth(3);
 		gc.strokeOval((v.getPos().getX()-37+xOffset),(v.getPos().getX()-13+yOffset), 74+xOffset, 36+yOffset);
 		gc.fillOval((v.getPos().getX()-36+xOffset),(v.getPos().getY()-12+yOffset),72+xOffset,34+yOffset);	
 		gc.setFill(Color.BLACK);
 		gc.setFont(new Font(14));
 		gc.fillText(v.getWord(), (v.getPos().getX()-25+xOffset), (v.getPos().getY()+10+yOffset));
 	}*/
 	
 	private void drawConnector(double x1, double y1, double x2, double y2, int type){
 		if(type==1){
 			//Synonym
 			gc.setStroke(Color.GREEN);
 		} else {
 			//Antonym
 			gc.setStroke(Color.RED);
 		}
 		
 		gc.setLineWidth(2);
 		gc.strokeLine(x1-xOffset, y1-yOffset, x2-xOffset, y2-yOffset);
 	}
 	
 	private void drawGraph(){
 		
 		//Draw connectors
 			//Synonyms
 			if(displaySynonyms==1){
 				double mainX = vertex.getPos().getX();
 				double mainY = vertex.getPos().getY();
 				
 				for(Vertex v:vertex.getSynomyns()){
 					//Draw connector main node to synonym
 					double childX = v.getPos().getX();
 					double childY = v.getPos().getY();
 					drawConnector(childX,childY,mainX,mainY,SYNONYM);
 					//Draw connector synonym to its synonyms
 					if(v.getSynomyns().size()!=0){
 						for(Vertex c:v.getSynomyns()){
 							drawConnector(childX,childY,c.getPos().getX(),c.getPos().getY(),SYNONYM);
 						}
 					}
 				}
 			}
 			
 			
 			//Draw synonym nodes
 			if(displaySynonyms==1){
 				for(Vertex v:vertex.getSynomyns()){
 					drawSynNode(v);
 					if(v.getSynomyns().size()!=0){
 						for(Vertex c:v.getSynomyns()){
 							drawSynNode(c);
 						}
 					}
 				}
 			}
 			
 		//Draw main node
 		drawMainNode(vertex);
 		
 	}
 	
 	private void resetGraph(){
 		gc.setFill(Color.rgb(242,242,242));
 		gc.fillRect(0, 0, windowWidth, windowHeight);
 		gc.setFill(Color.BLACK);
 	}
 	
 	public void setScale(double scale){
 		graph.setScaleX(scale);
 		graph.setScaleY(scale);
 	}
 	
 	private void start() {
 		graph = new Canvas(windowWidth,windowHeight);
 		gc = graph.getGraphicsContext2D();
 
 		graph.setWidth(windowWidth);
 		graph.setHeight(windowHeight);
 		
 		resetGraph();
 		drawGraph();
 		
 		
 		/**
 		 * Action Methods
 		 */
 		 
 		final LinkedList<Double> curX = new LinkedList<Double>();
 		final LinkedList<Double> curY = new LinkedList<Double>();
 		
 		graph.addEventHandler(MouseEvent.MOUSE_DRAGGED,
 				new EventHandler<MouseEvent>(){
 			public void handle(MouseEvent e){
 				curX.add(e.getX());
 				curY.add(e.getY());
 				
 				if(curX.size()>1){
 					xOffset += curX.get(0)-curX.get(1);
 					yOffset += curY.get(0)-curY.get(1);
 
 					resetGraph();
 					drawGraph();
 					
 					curX.removeFirst();
 					curY.removeFirst();
 				}
 			}
 		});
 		
 		graph.addEventHandler(MouseEvent.MOUSE_RELEASED,
 				new EventHandler<MouseEvent>(){
 			public void handle(MouseEvent e){
 				curX.clear();
 				curY.clear();
 			}
 		});
 		
 		graph.addEventHandler(MouseEvent.MOUSE_CLICKED, 
 				new EventHandler<MouseEvent>(){
 			public void handle(MouseEvent e){
 				if(e.getClickCount()==2){
 					//On double click, search for clicked node
 					double clickX = e.getX() + xOffset;
 					double clickY = e.getY() + yOffset;
 					
 					for(Vertex v:vertex.getSynomyns()){
 						double nodeWidth = v.getWord().length() * 5;
 						
 						if((clickX > v.getPos().getX()-nodeWidth) && clickX < (v.getPos().getX() + nodeWidth)){
 							if((clickY > v.getPos().getY()-13) && clickY < (v.getPos().getY()+13)){
 								//found.
 								System.out.println("//" + vertex.getSynomyns());
 								vr.doClickSearchGraph(v.getWord());
 								break;
 							}
 						}
 						//child nodes to go here.
 						for(Vertex c:v.getSynomyns()){
 							double nodewidth = c.getWord().length() * 5;
 							if((clickX > c.getPos().getX()-nodeWidth) && clickX < (c.getPos().getX() + nodeWidth)){
 								if((clickY > c.getPos().getY()-13) && clickY < (c.getPos().getY()+13)){
 									//found.
 									System.out.println(c.getPos());
 									vr.doClickSearchGraph(c.getWord());
 								}
 							}
 						}
 					}
 				}
 			}
 		});
 	}
 }
