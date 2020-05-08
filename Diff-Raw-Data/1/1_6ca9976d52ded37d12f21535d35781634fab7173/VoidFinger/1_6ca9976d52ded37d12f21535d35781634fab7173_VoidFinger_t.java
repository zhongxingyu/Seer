 package voidfinger;
 
 import geometry.GeometryException;
 import geometry.Point;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import kernel.KernelDensityEstimator;
 import octree.Octree;
 import octree.OctreeException;
 import potential.EPArray;
 import potential.EPArrayException;
 import visibilityGraph.Graph;
 import visibilityGraph.GraphException;
 
 public final class VoidFinger {
     private long time = 0;
     private int threads = 1;
     private boolean verbose = false;
     private String filename;
     private Octree octree = null;
     private EPArray potentials = null;
     private ArrayList<Point> centers = null;
     private KernelDensityEstimator estimator = null;
     private int medianID = -1;
 
     public int getElapsedSeconds() {
         this.time = System.nanoTime() - this.time;
         return (int)(this.time/1000000000);
     }
     
     public VoidFinger(String filename, Integer threads, boolean verbose)
             throws FileNotFoundException, IOException, OctreeException, EPArrayException {
         this.time = System.nanoTime();
         if (filename == null || filename.equals(""))
             throw new IllegalArgumentException("You must specify a PDB ID");
         if (threads == null || threads < 1)
             throw new IllegalArgumentException("This program run on less than one thread");
         this.threads = threads;
         this.verbose = verbose;
         this.filename = filename;
         System.out.println("VoidFinger: Volumetric Inner Distance Fingerprinting Utility");
         System.out.println("(c) 2012 Spyridon Smparounis, George Papakyriakopoulos");
         System.out.println("National and Kapodistrian University of Athens");
         System.out.println("Department of Informatics and Telecommunications");
         System.out.println();
         System.out.println("PDB ID:\t\t"+filename);
         System.out.println("# of threads:\t"+threads);
         System.out.println();
         if (verbose) {
             System.out.print("Parsing molecular octree from file... ");
             this.octree = Octree.parseFromFile(filename+".sog");
             System.out.println("done");
             System.out.print("Parsing electrostatic potentials from file... ");
             this.potentials = EPArray.readArrayFromFile(filename+".pot.dx", this.octree.getMinNodeLength());
             System.out.println("done");
             System.out.print("Loading cluster centers from file and integrating potentials... ");
             this.centers = this.loadClusterCenters(filename+".cluster");
             System.out.println("done");
         } else {
             System.out.print("Preparing for runs... ");
             this.octree = Octree.parseFromFile(filename+".sog");
             this.potentials = EPArray.readArrayFromFile(filename+".pot.dx", this.octree.getMinNodeLength());
            this.centers = this.loadClusterCenters(filename+".cluster");
             System.out.println("ready");
         }
     }
     
     public void performAnalysis() throws IOException {
         try {
             if (verbose) {
                 Graph graph = new Graph(this.centers, this.octree, this.threads);
                 System.out.print("Building visibility graph... ");
                 graph.buildVisibilityGraph();
                 System.out.println("done");
                 System.out.println(graph.totalEdges+" total edges created.");
                 System.out.print("Calculating inner distances... ");
                 ArrayList<Float> result = graph.getInnerDistances();
                 System.out.println("done");
                 System.out.println(result.size()+" inner distances calculated.");
                 System.out.print("Building kernel density estimator... ");
                 this.estimator = KernelDensityEstimator.generateEstimatorFromValues(this.filename, KernelDensityEstimator.KDE_GAUSSIAN, result);
                 System.out.println("done");
             } else {
                 Graph graph = new Graph(this.centers, this.octree, this.threads);
                 graph.buildVisibilityGraph();
                 ArrayList<Float> result = graph.getInnerDistances();
                 this.estimator = KernelDensityEstimator.generateEstimatorFromValues(this.filename, KernelDensityEstimator.KDE_GAUSSIAN, result);
                 System.out.println(graph.totalEdges+" edges, "+result.size()+" IDs.");
                 System.out.println("Kernel density estimator built.");
             }
         } catch (GraphException ge) {
         }
     }
         
     public void saveKDEToFiles() {
         try {
             this.estimator.writeEstimatorToFile();
             this.estimator.writeApproximateCurveToFile();
         } catch (IOException ioe) {}
     }
     
     public ArrayList<Point> loadClusterCenters(String filename) throws IOException {
         if (filename == null || filename.equals("")) throw new IllegalArgumentException();
         ArrayList<Point> result = new ArrayList<Point>();
         BufferedReader input = new BufferedReader(new FileReader(filename));
         String line;
         while (input.ready()) {
             line = input.readLine();
             if (line.equals("")) continue;
             String[] tokens = line.split("\t");
             if (tokens.length != 3) throw new IllegalArgumentException("Invalid cluster center file syntax");
             Float[] coords = new Float[4];
             coords[0] = Float.parseFloat(tokens[0]);
             coords[1] = Float.parseFloat(tokens[1]);
             coords[2] = Float.parseFloat(tokens[2]);
             coords[3] = this.potentials.getPotentialForCoordinates(coords[0], coords[1], coords[2]);
             try { result.add(new Point(coords)); } catch (GeometryException ge) {}
         }            
         input.close();
         return result;
     }
     
     public static void main(String[] args) {
         System.out.println();
         if (args.length < 2) {
             System.out.println("Invalid argument count.");
             System.out.println("Proper syntax is:");
             System.out.println("java VoidFinger [PDB code] [threads] <verbose>");
             return;
         }
         boolean verbose = (args.length == 3 && args[2].equalsIgnoreCase("verbose"))?true:false;
         try {
             VoidFinger theFinger = new VoidFinger(args[0], Integer.parseInt(args[1]), verbose);
             theFinger.performAnalysis();
             System.out.println();
             System.out.print("Saving KDE and plot to file... ");
             theFinger.saveKDEToFiles();
             System.out.println("done");
             System.out.println("Total running time: "+theFinger.getElapsedSeconds()+" sec.");
         } catch (IllegalArgumentException iae) {
             System.out.println(iae.getLocalizedMessage());
         } catch (FileNotFoundException fe) {
             System.out.println("File "+args[0]+" not found; you may have mistyped the filename.");
         } catch (IOException ioe) {
             System.out.println("An error occured when reading/writing to the disk; please, try again.");
         } catch (OctreeException oe) {
             System.out.println(oe.getLocalizedMessage());
         } catch (EPArrayException epae) {
             System.out.println(epae.getLocalizedMessage());
         }
     }
 }
