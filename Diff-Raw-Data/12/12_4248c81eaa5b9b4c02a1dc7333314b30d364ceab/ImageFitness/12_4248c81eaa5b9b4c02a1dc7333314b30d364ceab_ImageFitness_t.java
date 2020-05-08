 package sets.lines;
 
 import java.awt.Color;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintStream;
 import java.util.List;
 import java.util.Scanner;
 
 import library.Fitness;
 import library.GPConfig;
 import library.GeneticProgram;
 
 public class ImageFitness extends Fitness {
 	public static final int SIZE = 100;
 	public static final int XSIZE = 100;
 	public static final int YSIZE = 100;
 
 	private int[][][] pixels = new int[XSIZE][YSIZE][3];
 	//private double power;
 	private static final double MAX_BADNESS = 7650000;
 
 	public ImageFitness(GPConfig conf) {
 		super(conf);
 		// Read in data;
 		//power = 1;
 	}
 
 	public void initFitness() {
 		Scanner scan;
 		try {
 			scan = new Scanner(new File("sample.pnm"));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			throw new RuntimeException("sample.pnm cannot be read. No target to copmare to");
 		}
 
 		int y, x, c;
 		scan.next();
 		scan.next();
 		scan.next();
 		scan.next();
 		for (x = 0; x < XSIZE; x++) {
 			for (y = 0; y < YSIZE; y++) {
 				for (c = 0; c < 3; c++) {
 					pixels[x][y][c] = scan.nextInt();
 				}
 			}
 		}
 
 		// close the file
 		scan.close();
 	}
 
 	public void assignFitness(List<GeneticProgram> pop) {
 		int i;
 
 		double totalFitness;
 
 		// outerloop - selects a program
 		for (i = 0; i < pop.size(); i++) {
 			// initialise fitness to zero
 			totalFitness = 0;
 			
 			ReturnImage im[] = new ReturnImage[] { new ReturnImage(SIZE, SIZE, config) };
 
 			pop.get(i).evaluate(im);
 
 			Color p;
 			for (int y = 0; y < YSIZE; y++) {
 				for (int x = 0; x < XSIZE; x++) {
 					p = im[0].getData(x, y);
 					totalFitness += Math.abs((p.getRed()) - pixels[x][y][0]);
 					totalFitness += Math.abs((p.getGreen()) - pixels[x][y][1]);
 					totalFitness += Math.abs((p.getBlue()) - pixels[x][y][2]);
 				}
 			}
 
 			// SOME EPIC MAGIC TO CONVERT TO GOODNESS
 
 			totalFitness = 1 - (totalFitness / (double) MAX_BADNESS);
 
 			// totalFitness = pow(totalFitness,power);
 			// power += 0.000075;
 			pop.get(i).setFitness(totalFitness);
 //			System.out.println("Evaluated Program as " + totalFitness);
 		}
 		// power += 0.000075;
 	}
 
 	// Needs to be fixed
 	public void outputResults(GeneticProgram program, String filename) {
 		PrintStream file;
 		try {
 			file = new PrintStream(new File(filename));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return;
 		}
 		int size = SIZE;
 
 		file.print("P3\n# best.pnm\n");
 		StringBuffer s = new StringBuffer();
 		program.print(s);
 		file.print("#");
 		file.println(s);
 		file.print(size);
 		file.print(" ");
 		file.print(size);
 		file.print("\n");
 		file.print("255\n");
 
 		ReturnImage[] im = new ReturnImage[] { new ReturnImage(size, size, config) };
 
 		program.evaluate(im);
 
 		Color p;
 		for (int y = 0; y < size; y++) {
 			for (int x = 0; x < size; x++) {
 				p = im[0].getData(y, x);
 				file.print(p.getRed());
 				file.print(" ");
 				file.print(p.getGreen());
 				file.print(" ");
 				file.print(p.getBlue());
 				file.print(" ");
 			}
 			file.println();
 		}
 
 		file.println();
 		file.close();
 	}
 
 	// This has been fixed
 	// There is no possible solution worthy of an early end point.
 	// May need to be reinabled, but in principle, this is human guided not controlled
 	// So it probably shouldn't terminate eary
 	public boolean solutionFound(List<GeneticProgram> pop) {
 		return false;
 	}
 
	public int compare(GeneticProgram gp1, GeneticProgram gp2) {
		return Double.compare(gp1.getFitness() , gp2.getFitness());
 	}
 
 	public double best() {
 		return 1;
 	}
 
 	public double worst() {
 		return 0;
 	}
 
 	// I realise, I am intentionally making square images.
 	public void getResult(GeneticProgram gp, int size) {
 
 		PrintStream file;
 		try {
 			file = new PrintStream(new File("out.pnm"));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			return;
 		}
 
 		// Set up ppm header
 
 		file.print("P3\n# best.pnm\n");
 		StringBuffer s = new StringBuffer();
 		gp.print(s);
 		file.print("#");
 		file.println(s);
 		file.print(size);
 		file.print(" ");
 		file.print(size);
 		file.print("\n");
 		file.print("255\n");
 
 		ReturnImage[] im = new ReturnImage[] { new ReturnImage(size, size, config) };
 
 		gp.evaluate(im);
 
 		Color p;
 		for (int y = 0; y < size; y++) {
 			for (int x = 0; x < size; x++) {
 				p = im[0].getData(y, x);
 				file.print(p.getRed());
 				file.print(" ");
 				file.print(p.getGreen());
 				file.print(" ");
 				file.print(p.getBlue());
 				file.print(" ");
 			}
 			file.println();
 		}
 
 		file.println();
 		file.close();
 
 	}
 }
