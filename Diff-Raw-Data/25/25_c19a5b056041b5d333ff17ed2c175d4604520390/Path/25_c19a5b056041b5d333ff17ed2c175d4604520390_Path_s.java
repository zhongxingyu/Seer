 package ar.edu.uade.tesis_grupo13.vistas.imagenes.componentes;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 import ar.edu.uade.tesis_grupo13.grafos.VertexList;
 
 public class Path extends ImagenRenderizable{
 	
 	private static Path instance;	
 
 	public Path() {
 		super();		
 		render();
 	}
 
 	public static Path getInstance() {		
 		instance = new Path();		
 		return instance;
 	}
 
 	public void render() {			
 		
 		BufferedImage imagen = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
 		Graphics2D g = imagen.createGraphics();
 		VertexList camino = this.config.getModelo().getCamino();
 		
 		g.setColor(Color.GREEN);
 		g.setStroke(new BasicStroke(2));
 		
 		for (int i = 0; i < camino.size()-1; i++) {
 			g.drawLine((camino.get(i).getMatrizX() * config.gridSize) + (config.gridSize)/2, (camino.get(i).getMatrizY() * config.gridSize) + (config.gridSize)/2, 
 					   (camino.get(i+1).getMatrizX() * config.gridSize) + (config.gridSize)/2, (camino.get(i+1).getMatrizY() * config.gridSize) + (config.gridSize)/2);
 			
 		}
 		
 		buffer = imagen;		
 	}
 	
 	@Override
 	public void repaint() {
 		if (ImagenRenderizable.changeType.equals("gridSize")) {			
 			super.repaint();
 		}
 	}
			
 	
 }
