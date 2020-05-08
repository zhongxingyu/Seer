 import java.awt.*;
 import java.awt.event.*;
 import java.awt.TexturePaint;
 import javax.swing.*;
 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
 import javax.swing.SwingUtilities;
 
 class Celula
     { 
 		Polygon p = new Polygon();
 		BufferedImage ime;
 		Graphics2D Gime;
 		int r;
 
 		Celula(int x, int y, int r , BufferedImage t) {
 			ime = t;
 			this.r = r;
 
 			for (int i = 0; i < 6; i++)
 				p.addPoint(x + (int) (r * Math.cos(i * 2 * Math.PI / 6)),
 						   y + (int) (r * Math.sin(i * 2 * Math.PI / 6)));
 			
 			//Gime = ime.createGraphics();
 		}
 
 		void draw(Graphics g) { 
 			Graphics2D g2d = (Graphics2D) g;
 			Rectangle rec = new Rectangle(0,0,100,100);
 			g2d.setPaint(new TexturePaint(ime, rec));
 			//g2d.setColor(Color.blue);
 			g2d.fill(p);
 	//		try{			Thread.sleep(50);}catch(Exception e){System.out.println("HUE");} 
 		}	
 
 		void trans(int dx, int dy) {
 			p.translate(dx, dy);
 	}
 }	
 
 class Campo extends JPanel
 {
 	Celula[][] cel;
 
 	public Campo(Celula[][] cel)
 	{
 		this.cel = cel;
 	}
 
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		Graphics2D g2d = (Graphics2D) g;
 		for (int i = 0; i < cel.length; i++) 
 			for (int j = 0; j < cel[0].length; j++)
 				cel[i][j].draw(g2d); // pinta as células no contexto gráfico
 	}
 }
 
 
class Tela extends JFrame
 {
 	public Tela(Campo campo, int H, int W) {
 		setTitle("Polygon");
 		setSize(H, W);
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				System.exit(0);
 			}
 		});
 		add(campo);
 		setVisible(true);
 	}
 }
 
 public class MapaVisual
 {
 	Mapa mapa;
 	Celula[][] cel;
 	int W,H, L;
 
 	public MapaVisual(Mapa mapa, int W, int H, int L) {
 		this.mapa = mapa;
 		this.W = W;
 		this.H = H;
 		this.L = L;
 		/*
 		setTitle("Polygon");
 		setSize(W, H);
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				System.exit(0);
 			}
 		});
 		add(new Campo(cel));
 		setVisible(true);*/
 	}
 
 
 	public void criaVisual()
 	{
 		Celula celTemp;
 	
 		double DELTA = 0.0;
 		double Dx = 3.0*L/2.0;
 		double Dy = (L*Math.sqrt(3));
 		BufferedImage lama = null;
 		BufferedImage grama = null;
 		BufferedImage agua = null;
 
 		try 
 		{
 			lama = ImageIO.read(this.getClass().getResource("lama.jpg"));
 			grama = ImageIO.read(this.getClass().getResource("grama.jpg"));
 			agua = ImageIO.read(this.getClass().getResource("agua.jpg"));
 		}
 		catch (Exception e) {System.exit(1);}
 
 		cel = new Celula[mapa.largura()][mapa.altura()];
 
 		for(int i = 0; i < mapa.largura(); i++)
 		{
 			for(int j = 0; j < mapa.altura(); j++)
 			{
 				DELTA = i%2 == 1? Dy/2.0 : 0;
 				if(mapa.getTerreno(j, i).eRugoso())
 					cel[j][i] = new Celula((int)(L + i*Dx),(int) (DELTA + L + j*Dy), L, lama);
 				else if(mapa.getTerreno(j, i).eLiso())
 					cel[j][i] = new Celula((int)(L + i*Dx),(int) (DELTA + L + j*Dy), L, grama);
 				else if(mapa.getTerreno(j, i).eAgua())
 					cel[j][i] = new Celula((int)(L + i*Dx),(int) (DELTA + L + j*Dy), L, agua);
 
 			}
 		}
 
 	}
 
 	public void abreJanela() {
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 Tela tela = new Tela(new Campo(cel), H, W);
                 tela.setVisible(true);
             }
         });
     }
 
 
 
 }
