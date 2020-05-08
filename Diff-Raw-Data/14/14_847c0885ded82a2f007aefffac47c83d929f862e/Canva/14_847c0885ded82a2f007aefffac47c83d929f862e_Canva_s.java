 /*
  * Canva.java
  *
  * date:    12.09.2011
  * auth:    Gomon Serge
  * 
  * Класс канвы для рисования фигур. У данного класса есть список фигур, которые
  * реализуюм интерфейс AbstractShape. Эти фигуры прорисовываются в методе
  * paintComponent(). Также данный класс отвечает за обработку событий нажатия 
  * клавишь мыши.
  */
 
 package canva;
 
 import graphicseditor.MainWindow;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.JPanel;
 import shapes.AbstractShape;
 
 public class Canva extends javax.swing.JPanel 
 {
     
     
     //Константные параметры канвы
     private final int DEFAULT_CELL_SIZE = 15;
     private final Color BACKGROUND_COLOR = Color.white;
     private final Color LINE_COLOR = Color.gray;
     
     private MainWindow mainWindow;
     
     private int height;		    //Высота поля (клеток)
     private int width;		    //Ширина поля (клеток)
     private int cellSize;	    //Размер клетки (пикселей)
     
     private CanvaAxis absciss;
     private CanvaAxis ordinate;
     private boolean showGrid;
     private BufferedImage gridImage;
     
     private List<AbstractShape> shapes;
     private AbstractShape currentShape;	    //Фигура, которая рисуется в настоящий момент
     
     public Canva(MainWindow mainWindow,
 	    CanvaAxis absciss, CanvaAxis ordinate, 
 	    int height, int width, int cellSize) 
     {
 	this.mainWindow = mainWindow;
 	this.height = height;
 	this.width = width;
 	this.cellSize = cellSize;
	this.cellSize = DEFAULT_CELL_SIZE;
 	this.absciss = absciss;
 	this.ordinate = ordinate;
 	this.showGrid = false;
 	this.shapes = new ArrayList<AbstractShape>();
 	
 	setBackground(BACKGROUND_COLOR);
         initComponents();	
 	
 	this.addMouseMotionListener(new MouseMotionListener() 
 	{
 	    @Override
 	    public void mouseDragged(MouseEvent e) 
 	    {
 		int x = e.getX()/Canva.this.cellSize;
 		int y = e.getY()/Canva.this.cellSize;
 		int height = Canva.this.height;
 		int width = Canva.this.width;
 		
 		if(x > width) x = width;
 		else if(x < 0) x = 0;
 		
 		if(y > height) y = height;
 		else if(y < 0) y = 0;
 		
 		Canva.this.mainWindow.setStatusBarMousePosition(x, y);
 	    }
 
 	    @Override
 	    public void mouseMoved(MouseEvent e) 
 	    {
 		int x = e.getX()/Canva.this.cellSize;
 		int y = e.getY()/Canva.this.cellSize;
 		int height = Canva.this.height;
 		int width = Canva.this.width;
 		
 		if(x > width) x = width;
 		else if(x < 0) x = 0;
 		
 		if(y > height) y = height;
 		else if(y < 0) y = 0;
 		
 		Canva.this.mainWindow.setStatusBarMousePosition(x, y);
 	    }
 	});
 	
 	this.addMouseListener(new MouseAdapter() 
 	{
 	    @Override
 	    public void mouseClicked(MouseEvent e) 
 	    {
 		if(currentShape != null) {
 		    JPanel panel = currentShape.getPanel();
 		    if(panel != null) Canva.this.mainWindow.setPanel(panel);		    
 		    
 		    int x = e.getX()/Canva.this.cellSize;
 		    int y = e.getY()/Canva.this.cellSize;
 		    int height = Canva.this.height;
 		    int width = Canva.this.width;
 
 		    if(x > width) x = width - 1;
 		    else if(x < 0) x = 0;
 
 		    if(y > height) y = height - 1;
 		    else if(y < 0) y = 0;		    
 		    
 		    currentShape.setPoint(x, y);
 		    update();
 		    
 		    //Если фигура уже нарисована, то создаем новую фигуру,
 		    //которая сейчас будет нарисована (при этом боковая панель
 		    //относится к уже нарисованной фигуре)
 		    if(currentShape.isComplete()) {
 			addShape(currentShape.createShape());
 		    }	
 		}		
 	    }
 	});
     }
     
     public void addShape(AbstractShape shape)
     {
 	if(currentShape != null) {
 	    if(!currentShape.isComplete()) shapes.remove(currentShape);
 	    currentShape = null;
 	}
 	
 	currentShape = shape;
 	shapes.add(shape);
     }
     
     public CanvaAxis getAbsciss() 
     {
 	return absciss;
     }
     
     public CanvaAxis getOrdinate()
     {
 	return ordinate;
     }
     
     public void showGrid(boolean show)
     {
 	this.showGrid = show;
     }
     
     public void setCellSize(int cellSize)
     {
 	this.cellSize = cellSize;
     }
     
     public void setHeight(int height)
     {
 	this.height = height;
     }
     
     public void setWidth(int width)
     {
 	this.width = width;
     }
     
     public void update()
     {
 	updateGridImage();
 	setPreferredSize(new Dimension(cellSize*width, cellSize*height));
 	revalidate();
 	updateAxes();
 	update(getGraphics());
     }
     
     public void clear()
     {
 	shapes.clear();
 	if(currentShape != null && !currentShape.isComplete()) {
 	    shapes.add(currentShape);
 	}
 	update();
     }
     
     //Перерисовывает сетку
     protected void updateGridImage()
     {
 	if(width == 0 || height == 0 || cellSize == 0) return;
         gridImage = new BufferedImage(
 		cellSize*width + 1, cellSize*height + 1, 
 		BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = gridImage.createGraphics();
         g.setColor(LINE_COLOR);
         for(int i = 0; i <= width; i++) {
 	    g.drawLine(i*cellSize, 0, i*cellSize, cellSize*height);
 	}
         for(int i = 0; i <= height; i++) {
 	    g.drawLine(0, i*cellSize, cellSize*width, i*cellSize);
 	}
     }
     
     protected void updateAxes()
     {
 	absciss.setCellSize(cellSize);
 	ordinate.setCellSize(cellSize);
 	absciss.setPreferredWidth(width);
 	ordinate.setPreferredHeight(height);
 	absciss.revalidate();
 	ordinate.revalidate();
 	absciss.update();
 	ordinate.update();
     }
     
     @Override
     protected void paintComponent(Graphics g)
     {
         super.paintComponent(g);
         
 	if(cellSize > 1 && showGrid) g.drawImage(gridImage, 0, 0, this);
 	
 	CanvaGraphics gf2d = new CanvaGraphics(
 		getWidth(), getHeight(), cellSize, (Graphics2D)g);
 	
 	for(AbstractShape shape: shapes) {
 	    shape.draw(gf2d);
 	}
     }
     
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 400, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 300, Short.MAX_VALUE)
         );
     }// </editor-fold>//GEN-END:initComponents
     // Variables declaration - do not modify//GEN-BEGIN:variables
     // End of variables declaration//GEN-END:variables
 }
