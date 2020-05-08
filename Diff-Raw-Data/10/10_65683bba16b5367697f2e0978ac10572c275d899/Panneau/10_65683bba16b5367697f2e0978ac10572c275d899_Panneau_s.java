 import java.awt.Color;
 import java.awt.Graphics;
 
 
 import javax.swing.JPanel;
 
 
 @SuppressWarnings("serial")
 public class Panneau extends JPanel
 {
 	
 	Map laMap;
 	
 	//Graphics save;
 		
 	
 	@Override
 	public void paintComponent(Graphics imMap) 
 	{	
 		imMap.setColor(Color.DARK_GRAY);
 		imMap.fillRect(0, 0, 1000 ,1000);
 		
 		
 		// partie cration murs
 		int w = 20;
 		int h = 20;
 		
 		for( int i = 0 ; i < laMap.getH() ; i++ )
 		{
 			for( int j = 0 ; j < laMap.getW() ; j++)
 			{
 				switch(laMap.getCaseTabConst(j, i).getType()){
 				case MUR:
 					imMap.setColor(Color.BLUE);
 					break;
 				case VIDE:
 					imMap.setColor(Color.black);
 					break;
 				case TP:
 					imMap.setColor(Color.GREEN);
 					break;
 				case MUR_SPAWN:
 					imMap.setColor(Color.DARK_GRAY);
 					break;
 				default:
 					// rajouter ici exception
 					new Exception_Pacman("erreur lors du chargement des murs");
 					
 					imMap.setColor(Color.RED);
 					break;
 				}
 
 				imMap.fillRect(j*w,i*h, w, h);
 
 			}
 		}
 		
 		//partie cration points/pastilles
 		
 		// ---> les redimmensionner  -> pas beau ...
 		for( int i = 0 ; i < laMap.getH() ; i++ )
 		{
 			for( int j = 0 ; j < laMap.getW() ; j++)
 			{
 				switch(laMap.getCaseTabVar(j, i).getType()){
 				case PASTILLE:
 					imMap.setColor(Color.white);
 					imMap.fillOval(j*w+(w/3),i*h+(h/3), w/3, h/3);
 					break;
 				case NON_PASTILLE:
 					// ne rien faire
 					break;
 				case PASTILLE_SPE:
 					imMap.setColor(Color.white);
 					imMap.fillOval( j*w +(int)(w/4) , i*h + (int)(h/4) , (int)(w/1.5) , (int)(h/1.5) );
 					break;
 					
 				case PACMAN_POS:
 					imMap.setColor( new Color(252,249,20) ); //<--
 					imMap.fillOval( j*w  , i*h , w , h );
 					break;
 				default:
 					// indiquer une erreur
 					// le faire autrement ... -> Exception
 					new Exception_Pacman("erreur lors du chargement des pastilles");
 					
 					imMap.setColor(Color.RED);
 					imMap.fillOval(j*w,i*h, w, h);
 					break;
 					
 				}
 			}
 		}
 		
 		
 	}
 	
 	

	
	
 	public void peindre( Map map )
 	{
 		this.setMap(map);
 		Graphics imMap = this.getGraphics();
 		
 		
 		this.paintComponent(imMap);
 	}
 	
 
 	public void setMap( Map map )
 	{
 		this.laMap = map;
 	}
 	
 	
 }
