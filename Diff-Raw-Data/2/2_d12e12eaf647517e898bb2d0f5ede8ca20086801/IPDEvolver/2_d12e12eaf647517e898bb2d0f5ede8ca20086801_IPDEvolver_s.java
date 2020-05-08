 // -*- c-basic-offset: 2 -*-
 
 import java.awt.*;
 import java.util.*;
 import java.io.*;
 
 public class IPDEvolver implements Serializable {
   public static final int HEIGHT = 700;
   public static final int MAX_MEM = 100;
   public static final String SAVE_FILE = "ipd.dat";
   public static final String STORAGE_FILE = "ipdstorage.txt";
   public static final int TESTS = 100;
   public static final int WIDTH = 700;
 
   public static final int COMPRESSION_THRESHOLD = 8;
   public static final double DECREASE_MEM = 0.00001;
   public static final double INCREASE_MEM = 0.00001;
   public static final double MUTATE_PROB = 0.01;
   public static final double ERROR = 0.002;
   public static final int P = 3;
   public static final int S = 0;
   public static final int T = 5;
   public static final int R = 1;
 
   transient private PrintStream storage;
   transient private DrawingPanel panel;
   transient private Graphics g;
   transient private DrawingPanel stats;
   transient private Graphics statG;
   private short[][][] data = new short[WIDTH][HEIGHT][];
   private short[][][] aux = new short[WIDTH][HEIGHT][];
   private int[][] scores = new int[WIDTH][HEIGHT];
   private short[] mem1 = new short[MAX_MEM];
   private short[] mem2 = new short[MAX_MEM];
   private int[] scoreData = new int[2];
   private Random rand = new Random();
   private int generation = 0;
 
   private ArrayList<short[]> species = new ArrayList<short[]>();;
   private ArrayList<Integer> population = new ArrayList<Integer>();;
   private ArrayList<Color> colors = new ArrayList<Color>();;
 
   public static void main (String[] args) throws IOException, ClassNotFoundException {
     File save = new File(SAVE_FILE);
     IPDEvolver evolver;
     if (save.exists()) {
       ObjectInputStream input = new ObjectInputStream(new FileInputStream(save));
       evolver = (IPDEvolver)input.readObject();
       input.close();
     } else {
       evolver = new IPDEvolver();
     }
 
     evolver.run();
   }
 
   public IPDEvolver () throws FileNotFoundException {
     initTransient();
 
     species.add(new short[2]);
     short[] species2 = new short[2];
     species2[1] = 1;
     species.add(species2);
     species2 = new short[2];
     species2[0] = 1;
     species.add(species2);
     species2 = new short[2];
     species2[0] = 1;
     species2[1] = 1;
     species.add(species2);
 
     for (int i = 0; i < 4; i++)
       population.add(0);
 
     colors.add(Color.WHITE);
     colors.add(Color.RED);
     colors.add(Color.BLUE);
     colors.add(Color.BLACK);
 
     for (int i = 0; i < WIDTH; i++) {
       for (int j = 0; j < HEIGHT; j++) {
         data[i][j] = new short[2];
         data[i][j][0] = (short) rand.nextInt(2);
         data[i][j][1] = (short) rand.nextInt(2);
         population.set(data[i][j][0] * 2 + data[i][j][1], population.get(data[i][j][0] * 2 + data[i][j][1]) + 1);
       }
     }
   }
 
   private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException {
     in.defaultReadObject();
     initTransient();
   }
 
  private void initTransient() throws FileNotFoundException {
     storage = new PrintStream(new FileOutputStream(new File(STORAGE_FILE), true));
     panel = new DrawingPanel(WIDTH, HEIGHT);
     g = panel.getGraphics();
     stats = new DrawingPanel(WIDTH, HEIGHT);
     statG = stats.getGraphics();
   }
 
   public void run () throws FileNotFoundException, IOException {
     while (true) {
       generation++;
 
       message("Initializing generation " + generation + "...");
       for (int i = 0; i < WIDTH; i++) {
         for (int j = 0; j < HEIGHT; j++) {
           scores[i][j] = 0;
         }
       }
       for (int i = 0; i < WIDTH; i++) {
         for (int j = 0; j < HEIGHT; j++) {
           score(data[i][j], data[(i + WIDTH - 1) % WIDTH][j]);
           scores[i][j] += scoreData[0];
           scores[(i + WIDTH - 1) % WIDTH][j] += scoreData[1];
           score(data[i][j], data[(i + 1) % WIDTH][j]);
           scores[i][j] += scoreData[0];
           scores[(i + 1) % WIDTH][j] += scoreData[1];
           score(data[i][j], data[i][(j + HEIGHT - 1) % HEIGHT]);
           scores[i][j] += scoreData[0];
           scores[i][(j + HEIGHT - 1) % HEIGHT] += scoreData[1];
           score(data[i][j], data[i][(j + 1) % HEIGHT]);
           scores[i][j] += scoreData[0];
           scores[i][(j + 1) % HEIGHT] += scoreData[1];
         }
         if (i % (WIDTH / 10) == 0)
           statG.drawString("\t" + (100 * i / WIDTH) + "% complete.", WIDTH / 2 + 5, 40 + 200 * i / WIDTH);
       }
 
       message("Scoring complete. Processing...");
       for (int i = 0; i < WIDTH; i++) {
         for (int j = 0; j < HEIGHT; j++) {
           aux[i][j] = data[i][j];
           int score = scores[i][j];
           if (scores[(i + WIDTH - 1) % WIDTH][j] > score || (scores[(i + WIDTH - 1) % WIDTH][j] == score && Math.random() < 0.5)) {
             score = scores[(i + WIDTH - 1) % WIDTH][j];
             aux[i][j] = data[(i + WIDTH - 1) % WIDTH][j];
           }
           if (scores[(i + 1) % WIDTH][j] > score || (scores[(i + 1) % WIDTH][j] == score && Math.random() < 0.5)) {
             score = scores[(i + 1) % WIDTH][j];
             aux[i][j] = data[(i + 1) % WIDTH][j];
           }
           if (scores[i][(j + HEIGHT - 1) % HEIGHT] > score || (scores[i][(j + HEIGHT - 1) % HEIGHT] == score && Math.random() < 0.5)) {
             score = scores[i][(j + HEIGHT - 1) % HEIGHT];
             aux[i][j] = data[i][(j + HEIGHT - 1) % HEIGHT];
           }
           if (scores[i][(j + 1) % HEIGHT] > score || (scores[i][(j + 1) % HEIGHT] == score && Math.random() < 0.5)) {
             score = scores[i][(j + 1) % HEIGHT];
             aux[i][j] = data[i][(j + 1) % HEIGHT];
           }
         }
       }
 
       message("Processing complete. Copying...");
       for (int i = 0; i < WIDTH; i++) {
         for (int j = 0; j < HEIGHT; j++) {
           for (int k = 0; k < species.size(); k++) {
             if (species.get(k).length == data[i][j].length) {
               boolean match = true;
               for (int n = 0; n < data[i][j].length; n++)
                 match = match && data[i][j][n] == species.get(k)[n];
               if (match)
                 population.set(k, population.get(k) - 1);
             }
           }
           data[i][j] = mutate(aux[i][j]);
           boolean found = false;
           for (int k = 0; k < species.size(); k++) {
             if (species.get(k).length == data[i][j].length) {
               boolean match = true;
               for (int n = 0; n < data[i][j].length; n++)
                 match = match && data[i][j][n] == species.get(k)[n];
               if (match) {
                 population.set(k, population.get(k) + 1);
                 found = true;
               }
             }
           }
           if (!found) {
             species.add(data[i][j]);
             population.add(1);
             colors.add(new Color(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256)));
           }
         }
       }
 
       message("Copy complete. Drawing...");
       draw();
       drawStats(storage);
 
       message("Drawing complete. Saving...");
       ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(SAVE_FILE));
       output.writeObject(this);
       output.close();
 
       if (generation % 10 == 0) {
         message("Save complete. Copying file to IPD/ipd" + generation + ".dat...");
 
         File dir = new File("IPD");
         if (!dir.exists())
           dir.mkdir();
 
         output = new ObjectOutputStream(new FileOutputStream("IPD/ipd" + generation + ".dat"));
         output.writeObject(this);
         output.close();
 
         message("File copy complete. Saving image to IPD/ipd" + generation + ".png...");
         panel.save("IPD/ipd" + generation + ".png");
       }
     }
   }
 
   private void message (String message) {
     statG.setColor(Color.WHITE);
     statG.fillRect(WIDTH / 2, 0, WIDTH / 2, HEIGHT);
     statG.setColor(Color.BLACK);
     statG.drawString(message, WIDTH / 2 + 5, 20);
   }
 
   private void draw () {
     for (int i = 0; i < WIDTH; i++) {
       for (int j = 0; j < HEIGHT; j++) {
         for (int k = 0; k < species.size(); k++) {
           if (species.get(k).length == data[i][j].length) {
             boolean match = true;
             for (int n = 0; n < data[i][j].length; n++)
               match = match && data[i][j][n] == species.get(k)[n];
             if (match)
               g.setColor(colors.get(k));
           }
         }
         g.drawLine(i, j, i, j);
       }
     }
   }
 
   private short[] mutate (short[] data) {
     short[] child = null;
     if (Math.random() < INCREASE_MEM && data.length < Math.pow(2, MAX_MEM))
       child = new short[data.length * 2];
     else if (Math.random() < DECREASE_MEM && data.length > 1)
       child = new short[data.length / 2];
     else
       child = new short[data.length];
     int k = Math.random() < 0.5 ? 0 : data.length / 2;
     for (int i = 0; i < child.length; i++)
       child[i] = data[i % data.length + ((child.length < data.length) ? k : 0)];
     while (Math.random() < MUTATE_PROB) {
       int index = rand.nextInt(child.length);
       child[index] = (short) (1 - child[index]);
     }
     return child;
   }
 
   private void score (short[] data1, short[] data2) {
     int memory1 = 0;
     int memory2 = 0;
     scoreData[0] = 0;
     scoreData[1] = 0;
     for (int i = 0; i < TESTS; i++) {
       int action1 = data1[memory1];
       int action2 = data2[memory2];
       if (Math.random() < ERROR)
         action1 = 1 - action1;
       if (Math.random() < ERROR)
         action2 = 1 - action2;
       if (action1 == 0 && action2 == 0) {
         scoreData[0] += P;
         scoreData[1] += P;
       } else if (action1 == 0 && action2 == 1) {
         scoreData[0] += S;
         scoreData[1] += T;
       } else if (action1 == 1 && action2 == 0) {
         scoreData[0] += T;
         scoreData[1] += S;
       } else {
         scoreData[0] += R;
         scoreData[1] += R;
       }
       memory1 = (memory1 * 2) % data1.length + (Math.random() > ERROR ? action2 : 1 - action2);
       memory2 = (memory2 * 2) % data2.length + (Math.random() > ERROR ? action1 : 1 - action1);
     }
   }
 
   private void drawStats (PrintStream output) {
     statG.setColor(Color.WHITE);
     statG.fillRect(0, 0, WIDTH, HEIGHT);
     statG.setColor(Color.BLACK);
     statG.drawString("Generation " + generation + ":", 10, 20);
     int line = 0;
     int column = 0;
     ArrayList<short[]> aux = new ArrayList<short[]>();
     ArrayList<Integer> auxPop = new ArrayList<Integer>();
     ArrayList<Color> auxCol = new ArrayList<Color>();
     int n = 0;
     while (species.size() != 0) {
       int i = 0;
       for (int k = 1; k < population.size(); k++) {
         if (population.get(k) > population.get(i))
           i = k;
       }
       if (population.get(i) != 0)
         n++;
       if (population.get(i) != 0 && column < 3) {
         statG.setColor(colors.get(i));
         statG.fillRect(10 + 100 * column, line * 20 + 24, 16, 16);
         statG.setColor(Color.BLACK);
         statG.drawRect(10 + 100 * column, line * 20 + 24, 16, 16);
         String id = "";
         if (species.get(i).length < COMPRESSION_THRESHOLD) {
           for (int j = 0; j < species.get(i).length; j++) {
             id += species.get(i)[j];
           }
         } else {
           id += "C:";
           for (int j = 0; j < species.get(i).length / 4; j++) {
             int val = species.get(i)[4 * j] * 8 + species.get(i)[4 * j + 1] * 4 + species.get(i)[4 * j + 2] * 2 + species.get(i)[4 * j + 3];
             if (val < 10)
               id += val;
             else if (val == 10)
               id += "A";
             else if (val == 11)
               id += "B";
             else if (val == 12)
               id += "C";
             else if (val == 13)
               id += "D";
             else if (val == 14)
               id += "E";
             else
               id += "F";
           }
         }
         statG.drawString(id + ": " + population.get(i), 30 + 100 * column, line * 20 + 40);
         line++;
         if (line > 32) {
           line = 0;
           column++;
         }
       }
       auxCol.add(colors.get(i));
       auxPop.add(population.get(i));
       aux.add(species.get(i));
       colors.remove(i);
       population.remove(i);
       species.remove(i);
     }
     for (short[] s : aux)
       species.add(s);
     for (int generation : auxPop)
       population.add(generation);
     for (Color c : auxCol)
       colors.add(c);
     double stdev = 0;
     for (int generation : population) {
       if (generation != 0)
         stdev += Math.pow(generation - (double) (WIDTH * HEIGHT) / n, 2);
     }
     stdev = Math.sqrt(stdev) / n;
     double diverse = n / stdev;
     statG.drawString("Diversity Metric: " + ((int) (diverse * 1000) / 1000.0), 110, 20);
     if (output != null)
       output.println(diverse);
   }
 }
