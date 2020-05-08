 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Toolkit;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map.Entry;
 import java.util.Random;
 
 import javax.imageio.ImageIO;
 
 
 /**
  * @author Jonathan Mackenzie
  * 
  * The animation canvas for the simulation.
  *
  */
 public class CoralAnimation extends Canvas {
     /**
      * 
      */
     private static final long serialVersionUID = 946419120335674464L;
     // A map from colony number to a species, and a set of integers indicating the positions of cells belonging to that 
     // colony
     private HashMap<Integer, Colony> colonies = new HashMap<Integer, Colony>();
     private int colCount = 0;
     final private String LINE_SEP = System.getProperty("line.separator");
     private Simulation sim;
     private int tick, rows, columns;
     private BufferStrategy bf;
     private Random rng;
     private ArrayList<Species> species;
     private StringBuilder notes; // Comments about the simulation for a  tick
     
     /**
      * The CoralAnimation class encapsulate a visual component to simulate the growth of coral
      * Uses a HashMap from colony numbers to colonies of cells of a specific species and a set of cell indexes
      * 
      * @param s the simulation component that this simulation is running inside
      */
     public CoralAnimation(final Simulation s) {
         rng = new Random();
         notes = new StringBuilder();
         bf = getBufferStrategy();
         this.setBackground(Color.white);
         this.sim = s;
         reset();
         addMouseMotionListener(new MouseMotionAdapter() {
             
             @Override
             public void mouseMoved(MouseEvent e) {
                 // Report information about the cell we are mousing over to 
                 // this sidebar
                 Pair<Integer, Integer> p = getXY(e);
                 Pair<Integer, Species> cell = getSpecies(p);
                 if(cell == null)
                     sim.sp.setXY(p,null,0,0);
                 else
                     sim.sp.setXY(p,cell.y,cell.x,colonies.get(cell.x).getCells().size());
             }
         });
         addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 
                 // Clicking a cell will flip it,
                 // Killing the cell or bringing it back to life
                 Pair<Integer,Integer> p = getXY(e);
                 Pair<Integer, Species> cell = getSpecies(p);
                 if(cell != null) { 
                     notes.append("Removing cell at: x="+p.x+" y="+p.y+", "+cell.y).append(LINE_SEP);
                     removeCell(p.x, p.y, cell.x);
                 } else {
                     addCell(p.x,p.y, sim.sp.getSelectedSpecies(),++colCount,colonies) ;
                     notes.append("Marking cell at: x="+p.x+" y="+p.y+" as "+sim.sp.getSelectedSpecies()+ ", colony no. "+colCount).append(LINE_SEP);
                     detectMerges();
                 }
                 repaint();
             }
         });
         repaint();
     }
     /***
      * Return the species that a cell occupies on the grid at an x,y location
      * @param p a pair of x,y coordinates
      * @return
      */
     private Pair<Integer,Species> getSpecies(Pair<Integer,Integer> p) {
         for (Entry<Integer, Colony> c : colonies.entrySet()) {
             if(c.getValue().getCells().contains(p.x*columns + p.y)) 
                 return new Pair<Integer,Species>(c.getKey(),c.getValue().getSpecies());
         }
         return null;
     }
     /**
      * Converts a mouse event into its x,y location on the grid
      * @param e the mouse event
      * @return an x,y pair of where the mouse is on the grid
      */
     private Pair<Integer,Integer> getXY(MouseEvent e) {
         int x =  e.getX()/(getWidth()/sim.sp.getRows());
         int y =  e.getY()/(getHeight()/sim.sp.getColumns());
         return new Pair<Integer,Integer>(x,y);
     }
     /**
      * Add a cell to a colony
      * @param z the cell index to add
      * @param col the colony to add the cell to
      */
     private void addCell(int z, Colony col) {
         col.getCells().add(z);
     }
     /**
      * Add a cell to a given colony using x,y coordinates
      * @param x the x coordinate of the cell
      * @param y the y coordinate of the cell
      * @param s the species of the cell to add
      * @param colonyNo the colony number to add the cell to
      * @param col the map of colonies we are adding the cell to
      */
     private void addCell(int x, int y, Species s, int colonyNo, HashMap<Integer, Colony> col) {
         if(!col.containsKey(colonyNo)) {
             col.put(colonyNo, new Colony(s));
         }
         col.get(colonyNo).getCells().add(x*columns +y);
     }
     /**
      * Add a cell to a given colony using a cell index
      * @param z the cell index
      * @param s the species of the cell to add
      * @param colonyNo the colony number to add the cell to
      * @param col the map of colonies to add the cell to 
      */
     private void addCell(Integer z, Species s, int colonyNo, HashMap<Integer, Colony> col) {
         addCell(z/rows,z%rows, s,  colonyNo, col);
     }
     /**
      * Merge 2 colony counts, as selected when performing a step
      * @param colony1 the first colony to merge
      * @param colony2 the 2nd colony to merge
      */
     private void mergeColonies(int colony1, int colony2) {
         notes.append("Merging colony ").append(colony1).append(" with ").append(colony2).append(LINE_SEP);
         if(colonies.containsKey(colony1) && colonies.containsKey(colony2)) {
             colonies.get(colony1).getCells().addAll(colonies.get(colony2).getCells());
             colonies.remove(colony2);
         }
     }
     /**
      * Kill the given colony
      * @param colonyNumber the colony to kill
      */
     private void killColony(int colonyNumber) {
         colonies.remove(colonyNumber);
     }
     /**
      * Remove a cell from the simulation, should complete in O(1) time
      * @param x x coordinate of the cell
      * @param y y coordinate of the cell
      * @param colonyNumber the colony to remove the cell from
      */
     private void removeCell(int x, int y, int colonyNumber) {
         // Remove a cell at coordinates if any
     //    System.out.printf("Removing cell at %d,%d\n",x,y);
         colonies.get(colonyNumber).getCells().remove(x*columns + y);
         if(colonies.get(colonyNumber).getCells().isEmpty()) {
             colonies.remove(colonyNumber);
             notes.append("Colony ").append(colonyNumber).append(" has died out due to shrinkage");
         }
     }
     
 
     /**
      * Paint the graphics
      */
     public void paint(Graphics g) {
 
         setColumns(sim.sp.getColumns());
         setRows(sim.sp.getRows());
         createBufferStrategy(1);
         // Use a bufferstrategy to remove that annoying flickering of the display
         // when rendering
         bf = getBufferStrategy();
         g = null;
         try{
             g = bf.getDrawGraphics();
             render(g);
         } finally {
             g.dispose();
         }
         bf.show();
         Toolkit.getDefaultToolkit().sync();
         
         
     }
     
     /**
      * Render the cells in the frame with a neat border around each cell
      * @param g
      */
     private void render(Graphics g) {
         for (Entry<Integer,Colony> colony : colonies.entrySet()) {
             for (Pair<Integer, Integer> i : colony.getValue().getPositions(rows, columns)) {
                 int x = i.x;
                 int y = i.y;
                 // Draw the cell
                 g.setColor(colony.getValue().getSpecies().getColor());
                 g.fillRect(x*(getWidth()/columns), y*(getHeight()/rows),getWidth()/columns ,getHeight()/rows);
                 // Draw the colony number
                 
             }
         }
         // Draw the grid on top of colony numbers and cells
         for(int x = 0; x < columns; x++ ) {
             for (int y = 0; y < rows; y++) {
                 g.setColor(Color.GRAY);
                 g.drawRect(x*(getWidth()/columns), y*(getHeight()/rows),getWidth()/columns ,getHeight()/rows);
             }
         } 
         // Draw the colony numbers on top
         if(sim.sp.chckbxShowColNo.isSelected()) {
             for (Entry<Integer,Colony> colony : colonies.entrySet()) {
                 Pair<Integer,Integer> i = toXY(colony.getValue().getCells().iterator().next());
                 g.setColor(Color.black);
                 g.setFont(new Font("Monospaced", Font.BOLD , 14));
                 g.drawString(colony.getKey()+"",i.x*(getWidth()/columns), i.y*(getHeight()/rows)+g.getFontMetrics().getHeight());
             }
         }
         
     }
     /**
      * Iterate the simulation through 1 year
      */
     public int tick() {
         tick++;
         // merge any colonies who have neighbouring cells and of the same species
         detectMerges();
         // Kill any colonies that might die, before we start competing and growing/shrinking
         detectDeaths();
         //recruit new colonies
         recruitColonies();
         HashMap<Integer, Colony> newColonies = new HashMap<Integer, Colony>();
         // Iterate through each colony and either kill, grow or shrink
         ArrayList<Integer> toKill = new ArrayList<Integer>(); // Colonies to kill off because they shrank to death
         for (Entry<Integer, Colony> colony : colonies.entrySet()) {
             boolean competing = false;
             // Detect if this colony is competing
             for (Integer cell : colony.getValue().getCells()) {
                 Pair<Integer,Integer> xy = toXY(cell);
                 for(int i : getNeighbours(xy.x, xy.y)) {
                     for (Entry<Integer, Colony> otherColony : colonies.entrySet()) {
                         if(otherColony.getKey() != colony.getKey()
                                 && otherColony.getValue().getCells().contains(i)
                                 && otherColony.getValue().getSpecies() != colony.getValue().getSpecies()) {
                             competing = true;
                             break;
                         }
                     }
                     if(competing) break;
                 }
             }
             
             double newColonySize = 0d;
             Species s = colony.getValue().getSpecies();
             HashSet<Integer> cells = colony.getValue().getCells();
             Integer colonySize = cells.size();
             Integer colonyNumber = colony.getKey();
             double shrinkage,growth,growShrinkP;
             
             if(competing) {
                 shrinkage  = s.getShrinkC(colonySize);
                 growth     = s.getGrowC(colonySize);
                 growShrinkP = s.getGrowShrinkPC(colonySize);
             } else {
                 shrinkage   = s.getShrink(colonySize);
                 growth      = s.getGrow(colonySize);
                 growShrinkP = s.getGrowShrinkP(colonySize);
             }
             Colony newColony = new Colony(s);
             growth    += colony.getValue().getRemainingGrowth();
             shrinkage += colony.getValue().getRemainingGrowth();
         //    System.out.println("remaining growth for "+colonyNumber+": "+colony.getValue().getRemainingGrowth());
             // Check if this colony will grow or shrink this period
             boolean growing = growShrinkP > rng.nextFloat();
             notes.append("Colony ").append(colonyNumber).append(" ").append(s).append(" (Competing:").append(competing).append(") ").
                 append(growing?"grew":"shrank").append(" by ").
                 append(growing?growth:shrinkage).append( "cm").append(LINE_SEP);
 
             newColony.getCells().addAll(cells);
             HashSet<Integer> allOtherCells = new HashSet<Integer>();
             for (Entry<Integer, Colony> col : colonies.entrySet()) {
                 if(col.getKey() != colonyNumber)
                     allOtherCells.addAll(col.getValue().getCells());
             }
             // Empty colonies don't grow
            if(!(newColonySize + allOtherCells.size() > rows*columns)) {
                 // The colony is growing
                 if(growing) {
                     newColony.setRemainingGrowth(growth - (long) growth);
                     for(int i = 1; i < growth; i++) {
                         ArrayList<Integer> toAdd = new ArrayList<Integer>();
                         for (Integer c : newColony.getCells()) {
                             for (int n :  getNeighbours(c)) {
                                 if(newColony.getCells().contains(n)) {
                                     continue;
                                 } else {
                                     if(!allOtherCells.contains(n) && !toAdd.contains(n)){
                                    //     System.out.println("\tadding "+toXY(n));
                                         toAdd.add(n);
                                     }
                                 }
                             }
                         }
                         newColony.getCells().addAll(toAdd);
                     }
                 } else {
                     // We need to shrink the colony
                     if(newColony.getCells().size() == 1){
                         // Empty colonies die
                         toKill.add(colonyNumber);
                     } else {
                         // Leftover shrinkage is maintained for next iteration
                         newColony.setRemainingGrowth((-1)*(shrinkage-(long) shrinkage));
                         for(int i = 1; i < shrinkage ; i++){
                             ArrayList<Integer> toRemove = new ArrayList<Integer>();  
                             for(Integer c : newColony.getCells()) {
                                 int comp = 0;
                                 // Detect if the cell is a border cell
                                 for(int n : getNeighbours(c)) {
                                     if(/*allOtherCells.contains(n) ||*/ newColony.getCells().contains(n)) {
                                         comp++;
                                     }
                                 }
                                 if(comp < 4 &&  !toRemove.contains(c)) {
                                     toRemove.add(c);
                                    // System.out.println("\tRemoving "+toXY(c));
                                 }
                             }
                             newColony.getCells().removeAll(toRemove);
                         }
                     }
                 }
             }
       //      System.out.println("New remaining growth is "+newColony.getRemainingGrowth());
             newColonies.put(colonyNumber, newColony);
         }
         for (Integer i : toKill) {
             newColonies.remove(i);
         }
      //   System.out.println(getReport());
         colonies = newColonies;
         return tick;
     }
     
     /**
      * Update the species that the simulation knows about. Should only occur once before the simulation starts
      * @param specs the list of species that the simulation will know about
      */
     public void setSpecies(ArrayList<Species> specs) {
         species = specs;
     }
     
     /**
      * Recruit the specified number of colonies from each species 
      * to a random location on the grid
      */
     private void recruitColonies() {
         // Set up a set of all cells so that we don't end up adding over
         // existing cells
         HashSet<Integer> allCells = new HashSet<Integer>();
         for (Colony col : colonies.values()) {
                 allCells.addAll(col.getCells());
         }
         for (Species s : species) {
             for (int i = 0; i < s.getRecruits(); i++) {
                 boolean placed = false;
                 while(!placed){
                     int toPlace = rng.nextInt(columns*rows);
                     if(!allCells.contains(toPlace)) {
                         placed = true;
                         // Pick a random coral type to recruit
                         notes.append("New ").append(s).append(" recuited at ").append(toXY(toPlace)).append(LINE_SEP);
                         addCell(toPlace, s,++colCount,colonies) ;
                     } 
                 }
             }
         }
         
         
     }
     /**
      * Iterate through the colonies and see if they die this iteration
      */
     private void detectDeaths() {
         ArrayList<Integer> toKill = new ArrayList<Integer>();
         for (Entry<Integer, Colony> colony : colonies.entrySet()) {
             Colony c = colony.getValue();
             boolean died = false;
             if(c.getCells().size() == 0) {
                 notes.append("Colony: "+colony.getKey()+" "+c.getSpecies() +" died from shrinkage").append(LINE_SEP);
                 died = true;
             } else if (c.getSpecies().getDie(c.getCells().size()) > rng.nextFloat()) {
                 notes.append("Colony: "+colony.getKey()+" "+c.getSpecies() +" died").append(LINE_SEP);
                died = true;
             }
             if(died)
                 toKill.add(colony.getKey());
         }
         for (Integer i : toKill) {
            killColony(i);
         }
     }
     /**
      * Merge any adjacent colonies
      */
     private void detectMerges() {
         ArrayList<Pair<Integer,Integer>> toMerge = new ArrayList<Pair<Integer,Integer>>();
         for (Entry<Integer, Colony> colony : colonies.entrySet()) {
             for (Integer cell : colony.getValue().getCells()) {
                 Pair<Integer,Integer> xy = toXY(cell);
                 for(int i : getNeighbours(xy.x, xy.y)) {
                     for (Entry<Integer, Colony> otherColony : colonies.entrySet()) {
                         if(otherColony.getKey() != colony.getKey()
                             && otherColony.getValue().getSpecies() ==  colony.getValue().getSpecies()
                             && otherColony.getValue().getCells().contains(i)) {
                                 Pair<Integer,Integer> cols = new Pair<Integer,Integer>(otherColony.getKey() , colony.getKey());
                                 boolean contains = false;
                                 // Make sure we don't try to merge the same colony twice (even though checking is O(1) it clogged up the logs)
                                 // and checking through the list of colonies to merge is O(n), it will remain small because we are nubbing it 
                                 // every time
                                 for (Pair<Integer, Integer> pair : toMerge) {
                                     if(pair.x == cols.x && pair.y == cols.y || pair.x == cols.y && pair.y == cols.x) {
                                         contains = true;
                                         break;
                                     }
                                 }
                                 if(!contains) {
                                     toMerge.add(cols);
                                     break;
                                 }
                         }
                     }
                 }
             }
         }
      //   System.out.println("Merging: "+toMerge);
         for (Pair<Integer, Integer> pair : toMerge) {
             mergeColonies(pair.x, pair.y);
         }
     }
     /**
      * Gets the current tick count
      * @return the current tick
      */
     public int getTick() {
         return tick;
     }
 
     /**
      * @return number of columns the simulation uses
      */
     public int getColumns() {
         return columns;
     }
 
     /**
      *  Sets the number of columns to be used in the simulation
      * @param columns the number of columns to use
      */
     public void setColumns(int columns) {
         this.columns = columns;
     }
     
     /**
      * Sets the number of rows used in the simulation
      * @param rows
      */
     public void setRows(int rows) {
         this.rows = rows;
     }
     
     /**
      * Reset the simulation
      */
     public void reset() {
         tick = 0;
         colonies = new HashMap<Integer, Colony>();
         repaint();
         colCount = 0;
     }
     /**
      * Get the neighbours of a cell
      * @param z the index of the cell
      * @return an array of neighbours
      */
     private int[] getNeighbours(int z) {
         return getNeighbours(z/rows,z%columns);
     }
     /**
      * Get the neighbours of a given cell in a toroidal fashion
      * @param x the x coordinate of the cell
      * @param y the y coordinate of the cell
      * @return an array of cell indexes
      */
     private int[] getNeighbours(int x, int y) {
         int ip = (((x+1)%columns)+columns)%columns;
         int im = (((x-1)%columns)+columns)%columns;
         int jp = (((y+1)%rows)+rows)%rows;
         int jm = (((y-1)%rows)+rows)%rows;
         
         int[] neighbours = {
             im*columns + y,
             ip*columns + y,
             x*columns + jp,
             x*columns + jm,
          //   ip*columns +jp,
          //   ip*columns +jm,
           //  im*columns +jp,
          //   im*columns +jm
         };
         return neighbours;
     }
     /**
      * Convert a cell index into it's equivalent x,y coordinates
      * @param z the cell index
      * @return a pair containing the x,y coordinates for the given index
      */
     private Pair<Integer,Integer> toXY(Integer z) {
         return new Pair<Integer, Integer>(z/rows, z%columns);
     }
     /**
      * Gets the report for the current tick, contains information about colony merges, mortality and growth along
      * with colony sizes
      * @return a string of the report
      */
     public String getReport() {
         StringBuilder s = new StringBuilder(128);
         
         s.append("Tick:").append(tick).append(LINE_SEP);
         // Loop through each colony, report size, species
         for (Entry<Integer, Colony> p: colonies.entrySet()) {
             s.append("Colony id:").
                 append(p.getKey()).append(", ").
                 append(p.getValue().getSpecies()).
                 append(", size: ").
                 append(p.getValue().getCells().size()).
                 append(LINE_SEP);
         }
         s.append(notes);
         notes = new StringBuilder();
         s.append("--------------------------------------------------------------------").append(LINE_SEP);
         return s.toString();
     }
     public String getCSVReport() {
         StringBuilder sb = new StringBuilder(64);
         for (Entry<Integer, Colony> p: colonies.entrySet()) {
             sb.append(tick).append(",").append(p.getKey()).append(",").append(p.getValue().getCells().size()).append(",\"").append(p.getValue().getSpecies()).append("\"").append(LINE_SEP);
         }
         return sb.toString();
     }
     
     /**
      * Export the the display area to a file
      * @param imageName the image to save the file to
      */
     public void exportImage(String imageName) {
         BufferedImage image = new  BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
         Graphics2D graphics = image.createGraphics();
         // Set a white background on the image (default is black)
         graphics.setColor(Color.white);
         graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
         // Use render here instead of paint because the buffer strategy does something weird
         render(graphics);
         graphics.dispose();
         try {
        //     System.out.println("Exporting image: "+imageName);
             FileOutputStream out = new FileOutputStream(imageName);
             ImageIO.write(image, "png", out);
             out.close();
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         
     }
 }
