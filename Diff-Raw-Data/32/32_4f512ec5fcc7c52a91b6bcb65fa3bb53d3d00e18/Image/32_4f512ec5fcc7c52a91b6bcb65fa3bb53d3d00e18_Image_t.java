 package modele;
 /******************************************************
 Cours : LOG121
 Session : A2012
 Groupe : 04
 Projet : Laboratoire #4
 tudiant(e)(s) : Philippe Charbonneau
 				 Patrice Robitaille
 				 Mathieu Battah
 Code(s) perm. :  CHAP07110906
                  ROBP2002805 
                  BATM19038902 
 				
 Professeur : Ghizlane El boussaidi
 Charg de labo  : Alvine Boaye Belle
 Nom du fichier : Image.java
 Date cre :       2012-11-19
 Date dern. modif. : 2012-11-20
 
 *******************************************************
 Historique des modifications
 *******************************************************
 * 2012-11-19 : Cration de la classe
 * 2012-11-20 : ajout des mthodes get,set : width,height
 ********************************************************/
 
 import java.awt.image.*;
 import java.io.File;
 import java.io.IOException;
 import javax.imageio.*;
 
 /**
  * 
  * Class Image
  * Classe reprsentant une image
  * sur laquelle il est possible d'efefctuer
  * des oprations de changement.
  *
  */
 public class Image extends Subject {
 	
 	private static Image instance;
 	private int posX =0; //Position en X de l'image
 	private int posY =0; //Position en Y de l'image
 	private int width =-1;
 	private int heigth =-1;
 	private BufferedImage bufferImg = null;
 	private String filename;
 	
 	
 	private Image(){}
 	
 	/**
 	 * Retourne l'instance de l'image
 	 * @return
 	 */
 	public static Image getInstance(){
 		if (instance == null)
 			instance = new Image();
 		return instance;
 	}
 	
 	public void setPosX(int newX){
 		posX = newX;
 	}
 	
 	public void setPosY(int newY){
 		posY = newY;
 	}
 	
 	public int getPosX(){
 		return posX;
 	}
 	
 	public int getPosY(){
 		return posY;
 	}
 	
 	public void setWidth(int newWidth){
 		width = newWidth;
 	}
 	public void setHeigth(int newHeigth){
 		heigth = newHeigth;
 	}
 	
 	public int getWidth(){
 		return width;
 	}
 	public int getHeigth(){
 		return heigth;
 	}
 	
 	
 	
 	/**
 	 * Prend un path et load l'image reli au path;
 	 * @param path
 	 * @throws IOException 
 	 */
 	public void setImg(String path) throws IOException{
 		
 		try{
 			bufferImg = ImageIO.read(new File(path));
 			setHeigth(bufferImg.getHeight());
 			setWidth(bufferImg.getWidth());
 		}catch(IOException except){
 			throw except;
 		}
 		finally{
 			notifyObservers();
			PerspectiveModel.getInstance().reset();
 		}
 	}
 	
 	/**
 	 * Recevoir l'image lue
 	 * @return
 	 * @throws Exception
 	 */
 	public BufferedImage getImg() throws Exception{
 
 		if(bufferImg != null){
 			return bufferImg;
 		}
 		else
 			throw new Exception("must load an image before");
 	}
 	
 	public void setFilename(String name) throws Exception{
 		
 		int index = name.lastIndexOf('.');
 		if (index>0 && index <= name.length() - 2 ) {
 			filename = name.substring(0, index);
 		}
 		else
 			throw new Exception("L'image n'a pas une extension valide");
 		
 	}
 	
 	public String getFilename(){
 		return filename;
 	}
 	
 }
