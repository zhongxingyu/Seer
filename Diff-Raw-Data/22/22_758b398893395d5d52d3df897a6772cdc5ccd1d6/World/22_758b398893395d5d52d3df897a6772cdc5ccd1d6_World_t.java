 package EvolvingPlants;
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import tCode.RenderableObject;
 import tComponents.components.TButton;
 import tComponents.components.TMenu;
 import tComponents.components.TSlider;
 import tComponents.utils.events.TActionEvent;
 import tComponents.utils.events.TScrollEvent;
 import tools.ColTools;
 import tools.NumTools;
 
 public class World extends RenderableObject
 	{
 		// Constant variables
 		private static final byte NONE = 0, MAX_AGE = 1, SEED_STEMS = 2, LEAF_STEMS = 3, CHANCE_STEMS = 4, MAX_STEMS = 5, STEM_ANGLE = 6, SEED_ENERGY = 7, LEAF_ALPHA = 8;
 
 		// Simulation variables
 		private double timePassed = 0, timePerTick = 0.2;
 
 		// Lighting
 		public boolean lightUpdateNeeded = true;
 		private BufferedImage lightMap = new BufferedImage(800, 600, BufferedImage.TYPE_INT_ARGB);
 		public double lightEnergy = 75;
 
 		// Plants
 		LinkedList<Plant> plantsToAdd = new LinkedList<Plant>();
 		private LinkedList<Plant> plants = new LinkedList<Plant>();
 		private Iterator<Plant> plantIter;
 
 		// Left World options
 		private int plantSpacing = 12;
 		public float UVIntensity = 50;
 		public float UVDamage = 50;
 
 		// Right World options
 		private int plantSpacing2 = 12;
 		public float UVIntensity2 = 50;
 		public float UVDamage2 = 50;
 
 		// World Option sliders
 		private final TSlider photonEnergySlider = new TSlider(1010, 130, 180, TSlider.HORIZONTAL, 2);
 		private final TSlider plantSpacingSlider = new TSlider(1010, 170, 180, TSlider.HORIZONTAL, 2);
 		private final TSlider UVIntensitySlider = new TSlider(1010, 210, 180, TSlider.HORIZONTAL, 2);
 		private final TSlider UVDamageSlider = new TSlider(1010, 250, 180, TSlider.HORIZONTAL, 2);
 
 		// Universal options
 		private boolean showLight = false;
 		protected boolean viewSeeds = true;
 		protected boolean viewDeathAnimation = true;
 
 		private double nextDnaLength = (int) Genes.dnaLength;
 
 		private final TMenu universalMenu = new TMenu(200, 0, 800, 70, TMenu.HORIZONTAL);
 
 		private final TButton viewPhotonsButton = new TButton(0, 0, "View Light?   [" + (showLight ? "on]" : "off]"));
 		private final TButton viewSeedsButton = new TButton(0, 0, "View Seeds?   [" + (viewSeeds ? "on]" : "off]"));
 		private final TButton viewDeathAnimationButton = new TButton(0, 0, "View Dead Plants?   [" + (viewDeathAnimation ? "on]" : "off]"));
 		private final TButton reColourAllButton = new TButton(0, 0, "Re-Colour All");
 		private final TButton getMostProliferous = new TButton(0, 0, "Select Most Proliferous");
 		private final TButton resetButton = new TButton(1010, 520, 175, 40, "RESET PLANTS");
 
 		private final TSlider dnaLengthSlider = new TSlider(1010, 290, 180, TSlider.HORIZONTAL);
 		private final TSlider dnaMutabilitySlider = new TSlider(1010, 330, 180, TSlider.HORIZONTAL);
 		private final TSlider speciesDefSlider = new TSlider(1010, 370, 180, TSlider.HORIZONTAL);
 		private final TSlider pollenReachSlider = new TSlider(1010, 410, 180, TSlider.HORIZONTAL);
 
 		// Plant Options
 		private boolean showRelations = false;
 
 		private Plant selectedPlant;
 
 		private final TButton reColourButton = new TButton(5, 30, "Re-Colour");
 		private final TButton reColourSpeciesButton = new TButton(75, 30, "Re-Colour Species");
 		private final TButton showRelationsButton = new TButton(100, 3, "Show Relation");
 
 		// Genes options
 		private byte selectedSlider = NONE;
 
 		private Genes currentGenes = new Genes(null, null, null);
 
 		private final TButton getGenesButton = new TButton(65, 190, "Get Genes from Plant");
 
 		private final TSlider maxAgeSlider = new TSlider(10, 230, 180, TSlider.HORIZONTAL);
 		private final TSlider stemLengthSlider = new TSlider(10, 270, 180, TSlider.HORIZONTAL);
 		private final TSlider stemsPerSeedSlider = new TSlider(10, 310, 180, TSlider.HORIZONTAL);
 		private final TSlider stemsPerLeafSlider = new TSlider(10, 350, 180, TSlider.HORIZONTAL);
 		private final TSlider chanceGrowingStemsSlider = new TSlider(10, 390, 180, TSlider.HORIZONTAL);
 		private final TSlider maxStemsSlider = new TSlider(10, 430, 180, TSlider.HORIZONTAL);
 		private final TSlider stemAngleVarSlider = new TSlider(10, 470, 180, TSlider.HORIZONTAL);
 		private final TSlider seedEnergySlider = new TSlider(10, 510, 180, TSlider.HORIZONTAL);
 		private final TSlider alphaValueSlider = new TSlider(10, 550, 180, TSlider.HORIZONTAL);
 
 		// Mouse
 
 		private float mouseX = 0;
 		private float mouseY = 0;
 
 		private final byte SELECT = 0;
 		private final byte PLANT = 1;
 		private final byte KILL = 2;
 		private final byte GETGENES = 3;
 		private final byte RECOLOUR = 4;
 
 		private byte mouseState = SELECT;
 
 		private final TButton reColourSelectButton = new TButton(0, 0, "RE-Colour Plant");
 		private final TButton mouseSelectButton = new TButton(0, 0, "Select Plant");
 		private final TButton mousePlantButton = new TButton(0, 0, "Plant Seed");
 		private final TButton mouseKillButton = new TButton(0, 0, "Kill Plant");
 		private final TButton mouseGetGenesButton = new TButton(0, 0, "Get Genes");
 
 		@Override
 		protected void initiate()
 			{
 				// World Option components
 				add(photonEnergySlider);
 				photonEnergySlider.setPercent(3000, 0);
 				photonEnergySlider.setPercent(70, 1);
 				add(plantSpacingSlider);
 				plantSpacingSlider.setPercent(60, 0);
 				plantSpacingSlider.setPercent(60, 1);
 				add(UVIntensitySlider);
 				UVIntensitySlider.setPercent(40, 0);
 				UVIntensitySlider.setPercent(60, 1);
 				add(UVDamageSlider);
 				UVDamageSlider.setPercent(60, 0);
 				UVDamageSlider.setPercent(40, 1);
 				add(dnaLengthSlider);
 				dnaLengthSlider.setPercent(32);
 				add(dnaMutabilitySlider);
 				dnaMutabilitySlider.setPercent(35);
 				add(speciesDefSlider);
 				speciesDefSlider.setPercent(18);
 				add(pollenReachSlider);
 				pollenReachSlider.setPercent(60);
 
 				// Universal Options
 				add(universalMenu);
 				universalMenu.setBorderSize(2);
 				universalMenu.setTComponentSpacing(2);
 				universalMenu.add(reColourAllButton, true);
 				universalMenu.add(viewPhotonsButton, true);
 				universalMenu.add(viewSeedsButton, true);
 				universalMenu.add(viewDeathAnimationButton, true);
 				add(resetButton);
 
 				// Plant components
 				add(reColourButton);
 				add(reColourSpeciesButton);
 				add(showRelationsButton);
 
 				// Genes Components
 				add(getGenesButton);
 				add(maxAgeSlider);
 				add(stemLengthSlider);
 				add(stemsPerSeedSlider);
 				add(stemsPerLeafSlider);
 				add(chanceGrowingStemsSlider);
 				add(maxStemsSlider);
 				add(stemAngleVarSlider);
 				add(seedEnergySlider);
 				add(alphaValueSlider);
 
 				// Mouse Components
 				universalMenu.add(getMostProliferous, true);
 				universalMenu.add(mouseSelectButton, true);
 				universalMenu.add(mouseKillButton, true);
 				universalMenu.add(reColourSelectButton, true);
 				universalMenu.add(mousePlantButton, true);
 				universalMenu.add(mouseGetGenesButton, true);
 			}
 
 		@Override
 		protected void refresh()
 			{
 				plants.clear();
 				selectedPlant = null;
 
 				Genes.dnaLength = nextDnaLength;
 				dnaMutabilitySlider.setPercent(dnaMutabilitySlider.getPercent());
 				speciesDefSlider.setPercent(speciesDefSlider.getPercent());
 				// TODO something fishy going on here...
 
 				// add 10 equally spaced new plants
 				for (int i = 0; i < 10; i++)
 					for (int tries = 0; tries < 5; tries++)
 						{
 							addPlant(new Plant(null, null, (i * 80) + 240, 500));
 						}
 
 				setGeneSliders();
 			}
 
 		@Override
 		public void tick(double secondsPassed)
 			{
 				timePassed += secondsPassed;
 
 				// Do things that need to happen more than once per render
 				while (timePassed > timePerTick)
 					{
 						// Add new plants
 						for (Plant p : plantsToAdd)
 							plants.add(p);
 						plantsToAdd.clear();
 
 						// process plants
 						Plant plant;
 						plantIter = plants.iterator();
 						while (plantIter.hasNext())
 							{
 								plant = plantIter.next();
 								if (!plant.exists)
 									plantIter.remove();
 								else
 									plant.tick();
 							}
 
 						timePassed -= secondsPassed;
 					}
 
 				// Do things that only ever need to be done once per render
 				// update light
 				if (lightUpdateNeeded)
 					{
 						Graphics2D g2d = (Graphics2D) lightMap.getGraphics();
 
 						g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
 						g2d.fill(new Rectangle2D.Double(0, 0, 800, 600));
 						g2d.dispose();
 
 						Graphics lm = lightMap.getGraphics();
 						plantIter = plants.iterator();
 						while (plantIter.hasNext())
 							{
 								Plant p = plantIter.next();
 								if (p.age < p.genes.maxAge)
 									{
 										lm.setColor(new Color(0, 0, 0, p.genes.leafColour.getAlpha() / 2));
 										for (Leaf l : p.leaves)
 											lm.fillRect((int) (l.x - 12) - 200, (int) (l.y), 24, (int) (600 - l.y));
 									}
 							}
 						lm.dispose();
 						lightUpdateNeeded = false;
 					}
 			}
 
 		@Override
 		public void render(Graphics2D g)
 			{
 				g.setColor(new Color(100, 100, 255));
 				g.fillRect(200, 0, 800, 600);
 
 				if (showLight)
 					g.drawImage(lightMap, 200, 0, getObserver());
 
 				if (showRelations && selectedPlant != null)
 					{
 						g.setColor(Color.WHITE);
 						g.fillRect(200, 70, 800, 110);
 
 						g.setColor(Color.BLUE);
 						g.drawLine(200, 75 + (int) ((Genes.speciesDef / Genes.dnaLength) * 100), 1000, 75 + (int) ((Genes.speciesDef / Genes.dnaLength) * 100));
 
 						g.setColor(Color.BLACK);
 						for (int i = 0; i < 6; i++)
 							// TODO five numbers to indicate num differences
 							// between individuals
 							g.drawString("" + (int) (Genes.dnaLength * (i * 0.2)), 210, 80 + (i * 20));
 					}
 
 				for (Plant p : plants)
 					{
 						p.render(g);
 						if (showRelations && selectedPlant != null && p.seed.germinated)
 							{
 								g.setColor(Color.BLACK);
 								g.drawString("*", Math.round(p.x), 80 + (int) ((p.genes.getSpeciesDifference(selectedPlant.genes.DNA) / Genes.dnaLength) * 100));
 							}
 					}
 
 				if (selectedSlider != NONE)
 					{
 						g.setColor(Color.WHITE);
 						g.fillRect(200, 70, 800, 110);
 
 						g.setColor(Color.BLACK);
 						for (Plant p : plants)
 							if (p.seed.germinated)
 								switch (selectedSlider)
 									{
 										case (MAX_AGE):
 											g.drawString("*", Math.round(p.x), 170 - (int) ((p.genes.maxAge - 3) / 79.97f));
 											break;
 										case (SEED_STEMS):
 											g.drawString("*", Math.round(p.x), 170 - (int) ((p.genes.numberOfSeedStems - 1) / 0.14f));
 											break;
 										case (LEAF_STEMS):
 											g.drawString("*", Math.round(p.x), 170 - (int) (p.genes.numberOfLeafStems / 0.15f));
 											break;
 										case (CHANCE_STEMS):
 											g.drawString("*", Math.round(p.x), 170 - (int) (p.genes.chanceOfGrowingStems));
 											break;
 										case (MAX_STEMS):
 											g.drawString("*", Math.round(p.x), 170 - (int) ((p.genes.maxStems - 1) / 0.29f));
 											break;
 										case (STEM_ANGLE):
 											g.drawString("*", Math.round(p.x), 170 - (int) (p.genes.stemAngleVariation / 1.80));
 											break;
 										case (SEED_ENERGY):
 											g.drawString("*", Math.round(p.x), 170 - (int) (p.genes.seedEnergy / 5));
 
 											break;
 										case (LEAF_ALPHA):
 											g.drawString("*", Math.round(p.x), 70 + (int) ((255 - p.genes.leafColour.getAlpha()) / 2.55f));
 											break;
 									}
 					}
 
 				g.setColor(Color.GRAY);
 				g.fillRect(0, 0, 200, 600);
 				g.fillRect(1000, 0, 200, 600);
 
 				g.setColor(Color.BLACK);
 				g.drawString("World Options:", 1008, 20);
 				g.drawString("Plant Spacing: " + plantSpacing + " | " + plantSpacing2, 1020, 170);
 				g.drawString("Chance of mutation: " + (int) UVIntensity + " | " + (int) UVIntensity2, 1020, 210);
 				g.drawString("Size of mutation: " + (int) UVDamage + " | " + (int) UVDamage2, 1020, 250);
 				g.drawString("DNA Length (must RESET): " + (int) nextDnaLength, 1020, 290);
 				g.drawString("DNA Mutability: " + Genes.dnaMutability, 1020, 330);
 				g.drawString("Species Definition: " + Genes.speciesDef, 1020, 370);
 				g.drawString("Pollen Reach: " + Leaf.maximunPollenReach, 1020, 410);
 
 				g.setColor(Color.BLACK);
 				g.drawString("Selected Plant:", 8, 20);
 
 				if (selectedPlant != null)
 					{
 						g.drawString("Age:           " + selectedPlant.age, 10, 80);
 						g.drawString("/ " + selectedPlant.genes.maxAge, 120, 80);
 
 						g.drawString("Stems:      " + (int) (selectedPlant.genes.maxStems - selectedPlant.numberOfStemsLeft), 10, 100);
 						g.drawString("/ " + selectedPlant.genes.maxStems, 120, 100);
 						g.drawString("Stem Length:            " + selectedPlant.genes.maxStemLength, 10, 120);
 
 						g.drawString("Germinated Seeds:      " + selectedPlant.numGerminatedOffspring, 10, 140);
 						g.drawString("Energy per Seed:     " + selectedPlant.genes.seedEnergy, 10, 160);
 						g.drawString("Leaf Transparency:      " + ((255f - selectedPlant.genes.leafColour.getAlpha()) / 2.55f) + " %", 10, 180);
 					}
 
 				g.drawString("GENES:", 6, 210);
 				g.drawString("Max Age: " + currentGenes.maxAge, 14, 230);
 				g.drawString("Stem Length: " + currentGenes.maxStemLength, 14, 270);
 				g.drawString("Stems per Seed: " + currentGenes.numberOfSeedStems, 14, 310);
 				g.drawString("Stems per Leaf: " + currentGenes.numberOfLeafStems, 14, 350);
 				g.drawString("Chance leaf has Stems: " + currentGenes.chanceOfGrowingStems, 14, 390);
 				g.drawString("Maximum stem Number: " + currentGenes.maxStems, 14, 430);
 				g.drawString("Stem Angle Variation: " + currentGenes.stemAngleVariation, 14, 470);
 				g.drawString("Energy to Seed: " + currentGenes.seedEnergy, 14, 510);
 				g.drawString("Leaf Transparency: " + ((255f - currentGenes.leafColour.getAlpha()) / 2.55f) + " %", 14, 550);
 
 				if (mouseX > 200 && mouseY > 0 && mouseX < 1000 && mouseY < 600)
 					switch (mouseState)
 						{
 							case SELECT:
 								g.drawString("SELECT", Math.round(mouseX), Math.round(mouseY));
 								break;
 							case PLANT:
 								g.drawString("PLANT", Math.round(mouseX), Math.round(mouseY));
 								break;
 							case KILL:
 								g.drawString("KILL", Math.round(mouseX), Math.round(mouseY));
 								break;
 							case GETGENES:
 								g.drawString("GETGENES", Math.round(mouseX), Math.round(mouseY));
 								break;
 							case RECOLOUR:
 								g.drawString("RE-COLOUR", Math.round(mouseX), Math.round(mouseY));
 								;
 								break;
 						}
 			}
 
 		public double getLightMapAlphaValueAt(Leaf leaf)
 			{
 				if (leaf.x < 0 || leaf.x > 800)
 					return 0;
 
 				if (leaf.y < 0 || leaf.y > 600)
 					return 0;
 
 				return (lightMap.getRGB((int) leaf.x, (int) leaf.y) >> 24) & 0xff;
 			}
 
 		public final void addPlant(Plant plantToAdd)
 			{
 				plantsToAdd.add(plantToAdd);
 			}
 
 		public final boolean isSpaceToGerminate(Plant seedling)
 			{
 				if (seedling.x < 200 || seedling.x > 1000)
 					return false;
 
 				int spacing = seedling.x < 600 ? plantSpacing : plantSpacing2;
 
 				for (Plant plant : plants)
 					if (plant.seed.germinated)
 						if (NumTools.distance(seedling.x, seedling.y, plant.x, plant.y) < spacing)
 							return false;
 
 				return true;
 			}
 
 		@Override
 		public void mousePressed(MouseEvent event)
 			{
 				if (event.getY() > 70)
 					switch (mouseState)
 						{
 							case SELECT:
 								for (Plant p : plants)
 									for (Leaf l : p.leaves)
 										if (l.containsPoint(event.getX(), event.getY()))
 											{
 												setSelectedPlant(p);
 												break;
 											}
 								break;
 							case PLANT:
 								addPlant(new Plant(null, null, event.getX(), event.getY(), currentGenes));
 								break;
 							case KILL:
 								for (Plant p : plants)
 									for (Leaf l : p.leaves)
 										if (l.containsPoint(event.getX(), event.getY()))
 											{
 												p.age = p.genes.maxAge;
 												break;
 											}
 								break;
 							case GETGENES:
 								for (Plant p : plants)
 									for (Leaf l : p.leaves)
 										if (l.containsPoint(event.getX(), event.getY()))
 											{
 												currentGenes = p.genes;
 												break;
 											}
 								setGeneSliders();
 								break;
 							case RECOLOUR:
 								for (Plant p : plants)
 									for (Leaf l : p.leaves)
 										if (l.containsPoint(event.getX(), event.getY()))
 											{
 												Color c = ColTools.randColour();
 												p.genes.leafColour = new Color(c.getRed(), c.getGreen(), c.getBlue(), p.genes.leafColour.getAlpha());
 												p.genes.seedColour = new Color(255 - c.getRed(), 255 - c.getGreen(), 255 - c.getBlue());
 											}
 								break;
 						}
 
 				if (event.getX() < 200)
 					{
 						if (maxAgeSlider.isActive())
 							selectedSlider = MAX_AGE;
 
 						else if (stemsPerSeedSlider.isActive())
 							selectedSlider = SEED_STEMS;
 
 						else if (stemsPerLeafSlider.isActive())
 							selectedSlider = LEAF_STEMS;
 
 						else if (chanceGrowingStemsSlider.isActive())
 							selectedSlider = CHANCE_STEMS;
 
 						else if (maxStemsSlider.isActive())
 							selectedSlider = MAX_STEMS;
 
 						else if (stemAngleVarSlider.isActive())
 							selectedSlider = STEM_ANGLE;
 
 						else if (seedEnergySlider.isActive())
 							selectedSlider = SEED_ENERGY;
 
 						else if (alphaValueSlider.isActive())
 							selectedSlider = LEAF_ALPHA;
 					}
 			}
 
 		@Override
 		public void mouseReleased(MouseEvent event)
 			{
 				selectedSlider = NONE;
 			}
 
 		@Override
 		public void mouseDragged(MouseEvent event)
 			{
 				mouseX = event.getX();
 				mouseY = event.getY();
 
 				if (event.getY() > 70)
 					if (mouseState == KILL)
 						for (Plant p : getPlants())
 							for (Leaf l : p.leaves)
 								if (l.containsPoint(event.getX(), event.getY()))
 									{
 										p.age = p.genes.maxAge;
 										break;
 									}
 			}
 
 		@Override
 		public void mouseMoved(MouseEvent event)
 			{
 				mouseX = event.getX();
 				mouseY = event.getY();
 			}
 
 		@Override
 		public void tActionEvent(TActionEvent event)
 			{
 				// TODO override pressed in TButton for each of these methods
 				if (event.getSource() == viewPhotonsButton)
 					{
 						showLight = !showLight;
 						viewPhotonsButton.setLabel("View Light?   [" + (showLight ? "on]" : "off]"), true);
 					}
 
 				else if (event.getSource() == viewSeedsButton)
 					{
 						viewSeeds = !viewSeeds;
 						viewSeedsButton.setLabel("View Seeds?   [" + (viewSeeds ? "on]" : "off]"), true);
 					}
 
 				else if (event.getSource() == viewDeathAnimationButton)
 					{
 						viewDeathAnimation = !viewDeathAnimation;
 						viewDeathAnimationButton.setLabel("View Dead Plants?   [" + (viewDeathAnimation ? "on]" : "off]"), true);
 					}
 
 				else if (event.getSource() == resetButton)
 					{
 						refresh();
 					}
 
 				else if (event.getSource() == showRelationsButton)
 					{
 						showRelations = !showRelations;
 					}
 
 				else if (event.getSource() == reColourButton)
 					{
 						if (selectedPlant != null)
 							{
 								Color c = ColTools.randColour();
 								selectedPlant.genes.leafColour = new Color(c.getRed(), c.getGreen(), c.getBlue(), selectedPlant.genes.leafColour.getAlpha());
 								selectedPlant.leafColour = new Color(c.getRed(), c.getGreen(), c.getBlue(), selectedPlant.genes.leafColour.getAlpha());
 							}
 					}
 
 				else if (event.getSource() == reColourSpeciesButton)
 					{
 						if (selectedPlant != null)
 							{
 								Color c = ColTools.randColour();
 								selectedPlant.genes.leafColour = new Color(c.getRed(), c.getGreen(), c.getBlue(), selectedPlant.genes.leafColour.getAlpha());
 								selectedPlant.leafColour = new Color(c.getRed(), c.getGreen(), c.getBlue(), selectedPlant.genes.leafColour.getAlpha());
 
 								for (Plant p : plants)
 									if (p.genes.isSameSpecies(selectedPlant))
 										{
 											p.genes.leafColour = selectedPlant.genes.leafColour;
 											p.leafColour = selectedPlant.genes.leafColour;
 										}
 							}
 					}
 
 				else if (event.getSource() == reColourAllButton)
 					for (Plant p : plants)
 						{
 							Color c = ColTools.randColour();
 							p.genes.leafColour = new Color(c.getRed(), c.getGreen(), c.getBlue(), p.genes.leafColour.getAlpha());
 							p.leafColour = new Color(c.getRed(), c.getGreen(), c.getBlue(), p.genes.leafColour.getAlpha());
 						}
 
 				else if (event.getSource() == getGenesButton)
 					{
 						if (selectedPlant != null)
 							{
 								currentGenes = selectedPlant.genes;
 								setGeneSliders();
 							}
 					}
 
 				else if (event.getSource() == getMostProliferous)
 					{
 						Plant mostProliferous = null;
 
 						for (Plant p : plants)
 							if (mostProliferous == null || p.numGerminatedOffspring > mostProliferous.numGerminatedOffspring)
 								mostProliferous = p;
 
 						setSelectedPlant(mostProliferous);
 					}
 
 				else if (event.getSource() == mouseSelectButton)
 					mouseState = SELECT;
 				else if (event.getSource() == mousePlantButton)
 					mouseState = PLANT;
 				else if (event.getSource() == mouseKillButton)
 					mouseState = KILL;
 				else if (event.getSource() == mouseGetGenesButton)
 					mouseState = GETGENES;
 				else if (event.getSource() == reColourSelectButton)
 					mouseState = RECOLOUR;
 			}
 
 		private final void setSelectedPlant(Plant p)
 			{
 				if (selectedPlant != null)
 					selectedPlant.selected = false;
 				selectedPlant = p;
 				selectedPlant.selected = true;
 			}
 
 		private final void setGeneSliders()
 			{
 				Genes g = currentGenes;
 
 				maxAgeSlider.setPercent((g.maxAge - 3) / 79.97f);
 				stemLengthSlider.setPercent((g.maxStemLength - 10) / 1.9f);
 				stemsPerSeedSlider.setPercent((g.numberOfSeedStems - 1) / 0.14f);
 				stemsPerLeafSlider.setPercent(g.numberOfLeafStems / 0.15f);
 				chanceGrowingStemsSlider.setPercent(g.chanceOfGrowingStems);
 				maxStemsSlider.setPercent((g.maxStems - 1) / 0.29f);
 				stemAngleVarSlider.setPercent(g.stemAngleVariation / 1.80);
 				seedEnergySlider.setPercent(g.seedEnergy / 5);
 				alphaValueSlider.setPercent((255 - g.leafColour.getAlpha()) / 2.55f);
 			}
 
 		protected synchronized final Plant[] getPlants()
 			{
 				Plant[] plants = new Plant[this.plants.size()];
 				this.plants.toArray(plants);
 				return plants;
 			}
 
 		@Override
 		public void tScrollEvent(TScrollEvent event)
 			{
 				// LEFT
 				if (event.getScrollIndex() == 0)
 					{
 						if (event.getSource() == plantSpacingSlider)
 							plantSpacing = (int) ((plantSpacingSlider.getPercent(0) * 0.15) + 5);
 						else if (event.getSource() == UVIntensitySlider)
 							UVIntensity = (float) UVIntensitySlider.getPercent(0);
 						else if (event.getSource() == UVDamageSlider)
 							UVDamage = (float) UVDamageSlider.getPercent(0);
 					}
 
 				// RIGHT
 				if (event.getScrollIndex() == 1)
 					{
 						if (event.getSource() == plantSpacingSlider)
 							plantSpacing2 = (int) ((plantSpacingSlider.getPercent(1) * 0.15) + 5);
 						else if (event.getSource() == UVIntensitySlider)
 							UVIntensity2 = (float) UVIntensitySlider.getPercent(1);
 						else if (event.getSource() == UVDamageSlider)
 							UVDamage2 = (float) UVDamageSlider.getPercent(1);
 					}
 
 				else if (event.getSource() == photonEnergySlider)
 					lightEnergy = (int) ((photonEnergySlider.getPercent(0) * 0.49) + 1);
 				else if (event.getSource() == dnaLengthSlider)
 					nextDnaLength = (int) ((dnaLengthSlider.getPercent() * 0.75) + 5);
 				else if (event.getSource() == dnaMutabilitySlider)
 					Genes.dnaMutability = (int) ((dnaMutabilitySlider.getPercent() * 0.49) + 1);
 				else if (event.getSource() == speciesDefSlider)
 					Genes.speciesDef = (int) ((speciesDefSlider.getPercent() * Genes.dnaLength) / 100);
 
 				else if (event.getSource() == pollenReachSlider)
 					Leaf.maximunPollenReach = (int) ((pollenReachSlider.getPercent() * 4.5) + 50);
 
 				// GENES
 				else if (event.getSource() == maxAgeSlider)
 					currentGenes.maxAge = (int) (maxAgeSlider.getPercent() * 79.97f) + 3;
 				else if (event.getSource() == stemLengthSlider)
 					currentGenes.maxStemLength = (float) ((stemLengthSlider.getPercent() * 1.9f) + 10);
 				else if (event.getSource() == stemsPerSeedSlider)
 					currentGenes.numberOfSeedStems = (float) ((stemsPerSeedSlider.getPercent() * 0.14f) + 1);
 				else if (event.getSource() == stemsPerLeafSlider)
 					currentGenes.numberOfLeafStems = (float) (stemsPerLeafSlider.getPercent() * 0.15f);
 				else if (event.getSource() == chanceGrowingStemsSlider)
 					currentGenes.chanceOfGrowingStems = (float) chanceGrowingStemsSlider.getPercent();
 				else if (event.getSource() == maxStemsSlider)
 					currentGenes.maxStems = (float) ((maxStemsSlider.getPercent() * 0.29f) + 1);
 				else if (event.getSource() == stemAngleVarSlider)
 					currentGenes.stemAngleVariation = (float) (stemAngleVarSlider.getPercent() * 1.8f);
 				else if (event.getSource() == seedEnergySlider)
 					currentGenes.seedEnergy = (float) (seedEnergySlider.getPercent() * 5);
 				else if (event.getSource() == alphaValueSlider)
 					{
 						Color c = currentGenes.leafColour;
 						currentGenes.leafColour = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (255 - (alphaValueSlider.getPercent() * 2.55f)));
 						for (Plant p : plants)
 							if (p.genes == currentGenes)
 								p.leafColour = c;
 					}
 			}
 	}
