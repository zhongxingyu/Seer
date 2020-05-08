 package fearlesscode;
 
 import java.util.ArrayList;
 import fearlesscode.util.*;
 
 /**
  * A PlayField-et felépítő osztály.
  * 
  * A PlayField összerakásáért felelős, elkészíti a blokkokat, elhelyezi rajtuk az objektumokat, a játékost, a blokkokat összerendezi a PlayField objektumban.
  */
 public class PlayFieldBuilder
 {
 	/**
 	 * A PlayField-et felépítő metódus.
 	 *
 	 * Felépítí a PlayField-et a megadott file alapján, felhasználva a MapFromJson segédosztályt. A szekvencia diagrammok között megadott 4.4.1-es diagram szerint.
 	 * A MapFromJson osztályból a pályára vonatkozó összes információ lekérhető get metódusok segítségével, igy ezekből az információkból az PlayField könnyen felépíthető. 
 	 *
 	 * @param game A Game objektum referenciája.
 	 * @param file A pályát tartalmazó file elérési útja.
 	 */
 	public static PlayField createPlayField(Game game, String file) throws CommandException
 	{
 		PlayField pf = new PlayField(game);
 		ArrayList<Block> blocks = new ArrayList<Block>();
 		MapFromJson mfj = new MapFromJson(file);
 		
 		for(int i=0; i<mfj.getNumberOfBlocks();i++)
 		{
 			if(mfj.isFilledBlock(i))
 			{
 				blocks.add(new FilledBlock(pf));
 			}
 			else
 			{
 				blocks.add(new EmptyBlock(pf));
 			}
 		}
 		
		for(int i=0; i<mfj.getMapSizeY()-1;i++)
		{
			int j;
			for(j=0;j<mfj.getMapSizeX()-1;j++)
			{
				blocks.get(i*mfj.getMapSizeX()+j).setNeighbour(blocks.get(i*mfj.getMapSizeX()+j+1),1,true); //vizszintes beállítás
				blocks.get(i*mfj.getMapSizeX()+j).setNeighbour(blocks.get((i+1)*mfj.getMapSizeX()+j),2,true); //függőleges beállítás
			}
			blocks.get(i*mfj.getMapSizeX()+j).setNeighbour(blocks.get((i+1)*mfj.getMapSizeX()+j),2,true); //függőleges beállítás a legszélsőre
			
		}
		for(int i=0; i<mfj.getMapSizeX()-1;i++)
		{
			blocks.get((mfj.getMapSizeY()-1)*mfj.getMapSizeX()+i).setNeighbour(blocks.get((mfj.getMapSizeY()-1)*mfj.getMapSizeX()+i+1),1,true);
		}
 		
 		for(int i=0; i<mfj.getNumberOfBlocks();i++)
 		{
 			if(mfj.isFilledBlock(i))								//ha FilledBlock-ról van szó
 			{
 				for(int j=0;j<mfj.getNumberOfEntitiesInBlock(i);j++)
 				{
 					EntityPosition entityPos = new EntityPosition(mfj.getXPositionOfEntityInBlock(i, j),mfj.getYPositionOfEntityInBlock(i, j));
 					if("Wall".equals(mfj.getTypeOfEntityInBlock(i, j)))
 					{
 						double width = mfj.getWidthOfWall(i, j);
 						double height = mfj.getHeightOfWall(i, j);
 						blocks.get(i).addEntity(entityPos, new Wall(pf,width,height));
 					}
 					else if("Door".equals(mfj.getTypeOfEntityInBlock(i,j)))
 					{
 						int keys = mfj.getNumberOfRequiredKeys(i, j);
 						blocks.get(i).addEntity(entityPos, new Door(keys,pf));
 					}
 					else if("Key".equals(mfj.getTypeOfEntityInBlock(i,j)))
 					{
 						blocks.get(i).addEntity(entityPos, new Key(pf));
 					}
 					else if("SpawnPoint".equals(mfj.getTypeOfEntityInBlock(i,j)))
 					{
 						double xSP = mfj.getXPositionOfEntityInBlock(i, j);
 						double ySP = mfj.getYPositionOfEntityInBlock(i, j);
 						EntityPosition spawnPos = new EntityPosition(xSP,ySP);
 						SpawnPoint spawnPoint = new SpawnPoint(pf);
 						blocks.get(i).addEntity(spawnPos,spawnPoint);
 						Player player = new Player(mfj.getPlayerIDofSpawnPoint(i,j));
 						pf.addPlayer(player, spawnPoint);
 						player.enterBlock(blocks.get(i), spawnPos);
 					}	
 				}
 			}
 		}
 		
 		for(int i=0; i<mfj.getNumberOfBlocks();i++)
 		{
 			Position pos = new Position(mfj.getXPositionOfBlock(i),mfj.getYPositionOfBlock(i));
 			pf.addBlock(pos, blocks.get(i));
 		}
 		
 		return pf;
 	}
 
 }
