 package vues;
 
 import java.awt.Image;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Rectangle2D;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import noyau.Aeroport;
 import noyau.Rail;
 import noyau.Aeroport.Mode;
 
 import java.lang.Math;
 public class VueRail extends Vue {
 	
 	private Rail rail;
 	private Point pointA;
 	private Point pointB;
 	private double alpha;
 
 	public VueRail(VueGenerale vueGenerale, Image image, Rail rail) {
 		super(vueGenerale, image);
 		this.rail = rail;
 		constructionRectangle();
 		constructionImage();
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	boolean clic(int x, int y) {
 		if(Aeroport.getMode() == Mode.MANUEL){
 			Point p = new Point(x, y);
 			return dansRectangle(p);	
 		}
 		else{
 			return false;
 		}
 	}
 
 	@Override
 	void dessin(Graphics g) {
 		Graphics2D g2d = (Graphics2D)g;
 		g2d.rotate(alpha, pointA.x, pointA.y);
 		g2d.drawImage(image, (int)Math.round(pointA.x - image.getWidth(vueGenerale)/2), (int)Math.round(pointA.y - image.getHeight(vueGenerale)/2), vueGenerale);
 		g2d.rotate(-alpha, pointA.x, pointA.y);
 	}
 
 	@Override
 	void action() {
 		this.selectionner();
 		vueGenerale.setGuichetCourant(null);
 		vueGenerale.setTobogganCourant(null);
 		vueGenerale.getChariotCourant().ajouterNoeud(this.rail.getNoeudSortie());
 	}
 	
 	/**
 	 * On fait la construction du rectangle
 	 * @param r rail que on va utiliser pour la construction du rectangle
 	 */
 	private void constructionRectangle(){
 		pointA = rail.getNoeudEntree().getCoordonnees();
 		pointB = rail.getNoeudSortie().getCoordonnees();
 		pointA.x = (int) Math.round(pointA.getX()*vueGenerale.getEchelle());
 		pointA.y = (int) Math.round(pointA.getY()*vueGenerale.getEchelle());
 		pointB.x = (int) Math.round(pointB.getX()*vueGenerale.getEchelle());
 		pointB.y = (int) Math.round(pointB.getY()*vueGenerale.getEchelle());
 		/**
 		 * Ici on a les deux points...on va commencer les transformations mathematics pour obtenir le bon rectangle 
 		 * 
 		 */
 		double h = pointB.y - pointA.y;
 		double b = pointB.x - pointA.x;
		alpha = Math.atan2(h,b);
 		rectangle = new Rectangle((int)Math.round(pointA.x - image.getWidth(vueGenerale)/2), (int)Math.round(pointA.y - image.getHeight(vueGenerale)/2),
 				image.getWidth(vueGenerale), image.getHeight(vueGenerale));
 		AffineTransform rotation = AffineTransform.getRotateInstance(alpha, pointA.x, pointA.y);
 		rectangle = (Rectangle)rotation.createTransformedShape(rectangle);
 	}
 	
 	private void constructionImage(){
 		Image temp = image;
 		Graphics2D g = (Graphics2D)temp.getGraphics();
 		for (int i = 0; i < Math.round(rectangle.width/image.getWidth(vueGenerale)); i++) {
 			g.drawImage(image, pointA.x + i*image.getWidth(vueGenerale), pointA.y, vueGenerale);
 		}
 		image = temp;
 	}
 	
 
 }
