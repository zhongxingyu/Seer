 package clientQT;
 
 import java.util.HashMap;
 import java.util.Map;
 
import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Point3d;
 import javax.vecmath.Vector2d;
 
 import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.gui.QMatrix;
 
 import serverData.CardStack;
 import util.Matrix2d;
 
 import clientData.Client;
 import clientData.ClientCard;
 import clientData.ClientGameData;
 import clientData.ClientPlayerData;
 
 
 /**
  * Control of the position of cards
  *
  */
 public class BoardLayout
 	{
	boolean first=true;
	
	
 	private Map<ClientCard,AnimatedCard> mapCC_AC=new HashMap<ClientCard, AnimatedCard>();
	
 	private boolean needRedraw;
 	
 	/**
 	 * Update the card placement
 	 * 
 	 * @return True if scene has to be redrawn
 	 */
 	public boolean doLayout(BoardView view, Client client)
 		{
 		needRedraw=false;
 		mapCC_AC.clear();
 		view.emptyPosList.clear();
 
 		
 		ClientGameData gamedata=view.gameData;
 
 		////////// temp////////////
 		if(first)
 			{
 			for(int ap=0;ap<2;ap++)
 				{
 				ClientPlayerData pdata=new ClientPlayerData();
 				if(ap==0)
 					gamedata.playerMap.put(client.getClientID(),pdata);
 				else
 					gamedata.playerMap.put(-ap,pdata); //until we have more players
 				
 				
 				
 				
 				CardStack<ClientCard> onestack=new CardStack<ClientCard>();
 				pdata.stackMap.put("os", onestack);
 
 				for(int i=1;i<=10;i++)
 					{
 					ClientCard cdata=new ClientCard();
 					cdata.front="poker Spades "+i;
 					cdata.back="poker back";
 					pdata.stackMap.get("os").addCard(cdata);
 					}
 				}
 			
 			first=false;
 			
 			System.out.println("---------layout1!");
 			}
 		///////////////////////
 		
 
 		//Each player
 		for(int playerID:gamedata.playerMap.keySet())
 			{
 			//Here, create a transform for this players coordinate system
 			Matrix2d transformRot=new Matrix2d();
 			double baseRotAngle=Math.PI/4*playerID;
 			transformRot.setRot(baseRotAngle);
 			Vector2d transformMove=new Vector2d(100,100);
 			
 			ClientPlayerData pdata=gamedata.playerMap.get(playerID);
 
 				
 			layoutForOnePlayer(view, client, playerID, baseRotAngle, transformRot, transformMove, pdata);
 			
 			
 			}
 
 		//TODO common area
 		//call layoutforoneplayer
 		
 		
 		return needRedraw;
 		}
 	
 	
	private void layoutForOnePlayer(BoardView view, Client client, int playerID, double baseRotAngle, Matrix2d transformRot, Vector2d transformMove,
 			ClientPlayerData pdata)
 		{
 		
 
 
 		//Create a reversible map of the cards
 		for(AnimatedCard ac:view.cards)
 			mapCC_AC.put(ac.cardData, ac);
 
 		//For each stack
 		for(String stackName:pdata.stackMap.keySet())
 			{
 			CardStack<ClientCard> onestack=pdata.stackMap.get(stackName);
 
 			//Place position beneath
 			view.emptyPosList.add(new QPoint(0,0));
 
 			//Place cards
 			for(int i=0;i<onestack.size();i++)
 				{
 				ClientCard cc=onestack.getCard(i);
 				AnimatedCard ac=mapCC_AC.get(cc);
 
 				Vector2d shouldBe=new Vector2d(i*10, i*10);
 				shouldBe.add(transformMove);
 				transformRot.transform(shouldBe);
 				shouldBe.add(new Vector2d(100,100)); //To center rotation around midpos
 				
 				//If a card does not have an animated card, then just create it in the right location
 				if(ac==null)
 					{
 					System.out.println("creating card "+i);
 
 					ac=new AnimatedCard(cc);
 					ac.posX=shouldBe.x;
 					ac.posY=shouldBe.y;
 					ac.posZ=10-i;
 					ac.rotation=baseRotAngle;
 					
 					view.cards.add(ac);
 					mapCC_AC.put(cc, ac);
 					needRedraw=true;
 					}
 				else
 					{
 					//If a card is not in the right location, then animate it moving there
 
 					if(!ac.isBeingDragged && (ac.posX!=shouldBe.x || ac.posY!=shouldBe.y))
 						{
 						//System.out.println("out of pos");
 
 						double mvx=ac.posX-shouldBe.x;
 						double mvy=ac.posY-shouldBe.y;
 
 						//System.out.println(mvx+"  "+mvy);
 						//								if(mvx>5)
 						//									mvx=5;
 
 						ac.posX-=mvx*0.2;
 						ac.posY-=mvy*0.2;
 
 						//When card is close enough, make sure it is exactly in the right position so updates can stop
 						double stopRadius=3;
 						if(Math.abs(ac.posX-shouldBe.x)<stopRadius && Math.abs(ac.posY-shouldBe.y)<stopRadius)
 							{
 							ac.posX=shouldBe.x;
 							ac.posY=shouldBe.y;
 							}
 
 						needRedraw=true;
 						}
 
 					}
 				}
 
 
 			}
 
 		
 		}
 	
 	
 	}
