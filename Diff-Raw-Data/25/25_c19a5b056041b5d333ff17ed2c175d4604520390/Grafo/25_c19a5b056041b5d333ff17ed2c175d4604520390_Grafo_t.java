 package ar.edu.uade.tesis_grupo13.vistas.imagenes.componentes;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 import ar.edu.uade.tesis_grupo13.grafos.Coordenada;
 
 public class Grafo extends ImagenRenderizable {
 	
 	private static Grafo instance;	
 	
 	public Grafo() {
 		super();
 		render();
 	}
 	
 	public static Grafo getInstance() {
 		if (instance ==  null) {
 			instance = new Grafo();			
 		}
 		return instance;
 	}
 
 	public void render() {
 							
 			BufferedImage imagen = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 			Graphics2D g = imagen.createGraphics();
 			
 			boolean[][] matriz = map.getMatrizParedes();
 						
 			for (int y = 0; y < matriz.length; y++) {
 				for (int x = 0; x < matriz[0].length; x++) {				
																
 					
 					for (Coordenada coord : map.getGrafo().getVertices(x, y)) {						
 						int x2 = coord.getMatrizX();
 						int y2 = coord.getMatrizY();
 						double weight = map.getGrafo().getEdgeWeight(x, y, x2, y2);
 						
 						if (weight <= 1) {
 							g.setColor(Color.CYAN);
 						} else if (weight > 1 && weight < 5) {
 							g.setColor(Color.GREEN);
 						} else if (weight >= 5 && weight < 7.5) {
 							g.setColor(Color.YELLOW);
 						} else if (weight >= 7.5){
 							g.setColor(Color.RED);
 						}
 						
 						g.drawLine((x*config.gridSize) + config.gridSize / 2, (y*config.gridSize) + config.gridSize / 2, (((x2)*config.gridSize) + config.gridSize / 2), ((y2)*config.gridSize) + config.gridSize / 2);
					}
					
					//Grafico vertice
					g.setColor(Color.BLUE);
					g.fillRect((x*config.gridSize) + (config.gridSize - 4)/ 2, (y*config.gridSize) + (config.gridSize - 4)/2, 4, 4);
 				}
 			}
 			
 			buffer = imagen;				
 		}
 
 }
