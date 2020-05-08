 package evolution;
 
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Random;
 
 import javax.imageio.ImageIO;
 import javax.swing.JOptionPane;
 
 import biochem.AminoAcid;
 import biochem.PolypeptideFactory;
 
 import molGenExp.FoldedProteinArchive;
 import molGenExp.MolGenExp;
 import molGenExp.ServerCommunicator;
 import preferences.MGEPreferences;
 import utilities.GeneExpresser;
 import utilities.GlobalDefaults;
 import utilities.Mutator;
 
 public class Evolver implements Runnable {
 
 	private MolGenExp mge;
 	private EvolutionWorkArea evolutionWorkArea;
 	private World world;
 	private ArrayList genePool;
 	private int current;
 	private boolean keepGoing;
 
 	private FoldedProteinArchive archive;
 
 	private MGEPreferences preferences;
 	private Mutator mutator;
 	private GeneExpresser expresser;
 	private ServerCommunicator communicator;
 
 	public Evolver(final MolGenExp mge) {
 		this.mge = mge;
 		this.evolutionWorkArea = mge.getEvolutionWorkArea();
 		this.world = mge.getEvolutionWorkArea().getWorld();
 		keepGoing = true;
 		preferences = MGEPreferences.getInstance();
 		mutator = Mutator.getInstance();
 		archive = FoldedProteinArchive.getInstance();
 		expresser = GeneExpresser.getInstance();
 		communicator = new ServerCommunicator(mge);
 	}
 
 	public void run() {
 		evolutionWorkArea.setStatusLabelText("Running");
 		while (keepGoing) {
 			savePic();
 			createGenePool();
 			makeNextGeneration();
 		}
 	}
 
 	private void createGenePool() {
 		// find the gene pool
 		// get the fitness settings
 		int[] fitnessSettings = evolutionWorkArea.getFitnessValues();
 
 		// each organism in the world contributes fitness # of alleles to pool		
 		genePool = new ArrayList();
 		for (int i = 0; i < GlobalDefaults.worldSize; i++) {
 			for (int j = 0; j < GlobalDefaults.worldSize; j++) {
 				ThinOrganism org = world.getThinOrganism(i, j);
 				int colorNumber = 
 					GlobalDefaults.colorModel.getColorNumber(org.getColor());
 				for (int x = 0; x < fitnessSettings[colorNumber]; x++) {
 					genePool.add(org.getRandomDNASequence());
 				}
 			}
 		}
 	}
 
 	private void makeNextGeneration() {
 		if (genePool.size() == 0) {
 			JOptionPane.showMessageDialog(null, "<html><body>" 
 					+ "<font color=red>No organisms contributed to the gene pool.<br>"
 					+ "</font>You should be sure that the fitness of at least one of the<br>"
 					+ "organisms in the world has a non-zero fitness.</body></html>",
 					"Empty Gene Pool",
 					JOptionPane.ERROR_MESSAGE);
 			keepGoing = false;
 			current = getLengthOfTask();
 			evolutionWorkArea.setReadyToRun();
 			return;
 		}
 
 		current = 0;
 		ThinOrganism[][] nextGeneration = 
 			new ThinOrganism[GlobalDefaults.worldSize][GlobalDefaults.worldSize];
 		//make next generation
 		// choose random alleles from gene pool, mutate and make new organisms
 		// procedure depends on if using a server or not
 		//  if server - save all requests and send at once
 		//  if locally-folded - fold as you go
 
 		if (preferences.isUseFoldingServer()) {
 			evolutionWorkArea.setStatusLabelText("Using Folding Server");
 			PairOfProteinAndDNASequences[][] pairs =
 				new PairOfProteinAndDNASequences[GlobalDefaults.worldSize][GlobalDefaults.worldSize];
 			HashSet sequencesToBeFolded = new HashSet();
 			// make the new protein sequences and see which are in archive
 			//  make a list of those that need to be folded
 			for (int i = 0; i < GlobalDefaults.worldSize; i++) {
 				for (int j = 0; j < GlobalDefaults.worldSize; j++) {
 					String DNA1 = mutator.mutateDNASequence(getRandomAlleleFromPool());
 					String protein1 = 
 						convertThreeLetterProteinStringToOneLetterProteinString((
 								expresser.expressGene(DNA1, -1)).getProtein());
 					if (!archive.isInArchive(protein1)) {
 						sequencesToBeFolded.add(protein1);
 					}
 					String DNA2 = mutator.mutateDNASequence(getRandomAlleleFromPool());
 					String protein2 = 
 						convertThreeLetterProteinStringToOneLetterProteinString((
 								expresser.expressGene(DNA2, -1)).getProtein());
 					if (!archive.isInArchive(protein2)) {
 						sequencesToBeFolded.add(protein2);
 					}
 					pairs[i][j] = new PairOfProteinAndDNASequences(
 							DNA1, protein1, DNA2, protein2);
 				}
 			}

 			if (!keepGoing) {
 				nextGeneration = null;
 				genePool = null;
 				current = getLengthOfTask();
 				return;
 			}

 			// send the sequences out to be folded
 			StringBuffer b = new StringBuffer();
 			Iterator it = sequencesToBeFolded.iterator();
 			while (it.hasNext()) {
 				b.append((String)it.next() + ",");
 			}
			if (b.length() > 0) {
				b.deleteCharAt(b.length() -1);
			}
 			evolutionWorkArea.setStatusLabelText("Sending " 
 					+ sequencesToBeFolded.size() 
 					+ " sequences to be folded");
 			String response = communicator.sendSequencesToServer(b.toString());
 
 			//parse the reply and add to archive
 			String[] responseLines = response.split("<br>");
 			for (int i = 0; i < responseLines.length; i++) {
 				String line = responseLines[i];
 				if (line.contains(";")) {
 					archive.addEntryToArchive(line);
 				}
 				if (line.contains("it took")) {
 					evolutionWorkArea.setStatusLabelText(line);
 				}
 			}

 			//make new generation using updated archive
 			for (int i = 0; i < GlobalDefaults.worldSize; i++) {
 				for (int j = 0; j < GlobalDefaults.worldSize; j++) {
 					PairOfProteinAndDNASequences pair = pairs[i][j];
 					nextGeneration[i][j] = new ThinOrganism(
 							pair.getDNA1(),
 							pair.getDNA2(),
 							GlobalDefaults.colorModel.mixTwoColors(
 									(archive.getArchiveEntry(
 											pair.getProtein1())).getColor(), 
											(archive.getArchiveEntry(
													pair.getProtein2())).getColor()));
 					current++;
 				}
 			}
 		} else {
 			for (int i = 0; i < GlobalDefaults.worldSize; i++) {
 				for (int j = 0; j < GlobalDefaults.worldSize; j++) {
 					nextGeneration[i][j] = new ThinOrganism(
 							mutator.mutateDNASequence(getRandomAlleleFromPool()),
 							mutator.mutateDNASequence(getRandomAlleleFromPool()));
 					current++;
 					if (!keepGoing) {
 						nextGeneration = null;
 						genePool = null;
 						current = getLengthOfTask();
 						return;
 					}
 				}
 			}
 		}
 		genePool = null;
 		world.setOrganisms(nextGeneration);
 		evolutionWorkArea.updateGenerationLabel();
 	}
 
 	private String getRandomAlleleFromPool() {
 		Random r = new Random();
 		int x = r.nextInt(genePool.size());
 		return (String)genePool.get(x);
 	}
 
 	private void savePic() {
 		if (preferences.isGenerationPixOn()) {
 			//draw it
 			BufferedImage pic = new BufferedImage(
 					world.pictureSize, 
 					world.pictureSize,
 					BufferedImage.TYPE_INT_RGB);
 			Graphics g = pic.getGraphics();
 			world.paint(g);
 			g.setColor(Color.DARK_GRAY);
 			g.drawString("Generation: " + evolutionWorkArea.getGeneration(), 
 					20, 20);
 
 			//save it
 			File imageFile = new File(preferences.getSavePixToPath() 
 					+ System.getProperty("file.separator")
 					+ evolutionWorkArea.getGeneration()
 					+ ".png");
 			try {
 				ImageIO.write(pic, "png", imageFile);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}

 	public String convertThreeLetterProteinStringToOneLetterProteinString(
 			String threeLetter) {
 		//insert spaces between amino acid codes
 		StringBuffer psBuffer = new StringBuffer(threeLetter);
 		for (int i = 3; i < psBuffer.length(); i = i + 4) {
 			psBuffer = psBuffer.insert(i, " ");
 		}
 		threeLetter = psBuffer.toString();
 
 		// parse input into strings, each representing an acid
 		ArrayList acidStrings = 
 			PolypeptideFactory.getInstance().getTokens(threeLetter);
 
 		// parsing each acid string into AminoAcids using the AminoAcidTable.
 		StringBuffer b = new StringBuffer();
 		for (int i = 0; i < acidStrings.size(); i++) {
 			b.append((
 					GlobalDefaults.aaTable.get(
 							(String)acidStrings.get(i))).getAbName());
 		}
 		return b.toString();
 	}
 
 	public boolean done() {
 		if (current == getLengthOfTask()) {
 			return true;
 		}
 		return false;
 	}
 
 	public int getLengthOfTask() {
 		return GlobalDefaults.worldSize * GlobalDefaults.worldSize;
 	}
 
 	public int getCurrent() {
 		return current;
 	}
 
 	public void setKeepGoing(boolean b) {
 		keepGoing = b;
 	}
 }
