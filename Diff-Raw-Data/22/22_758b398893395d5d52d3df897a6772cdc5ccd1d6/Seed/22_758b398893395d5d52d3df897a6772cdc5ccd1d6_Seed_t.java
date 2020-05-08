 package EvolvingPlants;
 
 import java.awt.Graphics;
 
 import tools.RandTools;
 
 public class Seed extends PlantPart
 	{
 		private Stem[] stems = null;
 
 		public boolean germinated = false;
 
 		double xMod = 0;
 
 		private double energyPerGrow;
 
 		public Seed(Plant thisPlant, float x, float y)
 			{
 				super(thisPlant, x, y);
 
 				energy = thisPlant.genes.seedEnergy;
 
 				energyPerGrow = thisPlant.genes.energyTransfer * Math.pow(thisPlant.genes.energyTransfer + 0.2, 1.25);
 
 				xMod = RandTools.getDouble(-thisPlant.genes.seedSpread, thisPlant.genes.seedSpread);
 			}
 
 		@Override
 		public void tick()
 			{
 				if (!germinated)
 					{
 						if (y < Main.frame.getHeight() - 30)
 							{
 								y += 2;
 								thisPlant.y += 2;
 
								double var = (RandTools.getDouble(-xMod, xMod) / 2);
 
 								x += xMod + var;
 								thisPlant.x += xMod + var;
 							}
 						else if (!thisPlant.genes.germinate)
 							thisPlant.exists = false;
 						else if (stems == null)
 							{
 								germinate();
 							}
 					}
 				else
 					{
 						for (Stem s : stems)
 							{
 								s.tick();
 								if (energy > 0)
 									{
 										energy -= energyPerGrow;
 										s.energy += energyPerGrow;
 									}
 							}
 					}
 			}
 
 		@Override
 		public void render(Graphics g)
 			{
 				if (stems != null)
 					for (Stem s : stems)
 						s.render(g);
 
 				if (Main.world.viewSeeds)
 					{
 						g.setColor(thisPlant.seedColour);
 						int seedSize = (int) (energy / 15);
 						g.fillOval(Math.round(x - (seedSize / 2)), Math.round(y - (seedSize / 2)), seedSize, seedSize);
 					}
 			}
 
 		public void germinate()
 			{
 				if (energy > thisPlant.genes.seedEnergy / 2)
 					{
 						energy -= 0.4;
 						if (thisPlant.genes.germinate && Main.world.isSpaceToGerminate(thisPlant))
 							{
 								germinated = true;
 								if (thisPlant.parentOne != null && thisPlant.parentTwo != null)
 									{
 										thisPlant.parentOne.numGerminatedOffspring++;
 										thisPlant.parentTwo.numGerminatedOffspring++;
 									}
 
 								int numStems = (int) Math.min(thisPlant.numberOfStemsLeft, thisPlant.genes.numberOfSeedStems);
 								stems = new Stem[numStems];
 								for (int i = 0; i < numStems; i++)
 									stems[i] = new Stem(thisPlant, x, y);
 								thisPlant.numberOfStemsLeft -= numStems;
 							}
 					}
 				else
 					thisPlant.exists = false;
 			}
 	}
