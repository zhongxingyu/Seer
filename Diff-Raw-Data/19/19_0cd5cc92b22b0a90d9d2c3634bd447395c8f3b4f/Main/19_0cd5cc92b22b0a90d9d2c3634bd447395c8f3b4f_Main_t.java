 import java.util.ArrayList;
 import java.util.ConcurrentModificationException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map.Entry;
 import java.util.Random;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 
 import net.miginfocom.swing.MigLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JLabel;
 import javax.swing.JToggleButton;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JSpinner;
 import javax.swing.JComboBox;
 import javax.swing.JButton;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingWorker;
 import javax.swing.Timer;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ChangeEvent;
 import javax.swing.text.DefaultCaret;
 import javax.swing.JTabbedPane;
 import javax.swing.JSeparator;
 import javax.swing.JRadioButton;
 import javax.swing.ButtonGroup;
 
 /**
  * This is an application that simulates various process
  * scheduling algorithms. All processes are randomly generated
  * with a time of 1000ms or less. All processes are self managing
  * JPanels that you can call takeTime or finish to simulate the running 
  * of the process. The colour of the panel's border will change
  * to green if it is waiting, red for executing, black for finished
  *
  */
 class Main  extends JFrame{
     /**
      * 
      */
     
     private static final long serialVersionUID = -4166466089214148491L;
     // This is the time to run each process for
     private JPanel panel_processes;
     private JTextArea textArea;
     // These could be placed in a priority queue or something to similar
     // more complex scheduling algorithms
     private LinkedList<Proc> processList = new LinkedList<Proc>();
     // These are instance variables because other things need to access them
     private SpinnerNumberModel processModel, quantumModel,newProcProbModel,newProcsModel,delayModel;
     // This is the worker that does the simulating work
     private Worker worker;
     private boolean isStarted = false;
     private JComboBox algorithmSelect;
     private JToggleButton tglbtnStart;
     // Keep track of the pid we are up to
     private int pid = 0;
     int last = 0;
     // The array of algorithm names is here for easy definition
     private String[] algStrings = { "Round Robin",  "FIFO", "SJF" };
     private String[] memoryAllocationAlgorithms = {"Best Fit","Partition", "First Fit", "Worst Fit", "Next Fit"};
     private SpinnerNumberModel pageSizeModel;
     private SpinnerNumberModel memorySizeModel;
     private Block[] memoryBlocks;
     private JPanel panel_memory;
     private JComboBox memoryAllocationAlg;
     private final ButtonGroup buttonGroup = new ButtonGroup();
     private JRadioButton rdbtnDynamicMemory;
     private JRadioButton rdbtnStaticMemory;
     private SpinnerNumberModel block2Model;
     private SpinnerNumberModel block4Model;
     private SpinnerNumberModel block16Model;
     private SpinnerNumberModel block32Model;
     private SpinnerNumberModel block64Model;
     private int blockId;
     private JPanel panel_swap;
     private HDDCell[] swap = new HDDCell[20];
     
     /**
      * Run the application
      */
     
     Main(){
         // Use the OS look and feel
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         } catch (InstantiationException e) {
             e.printStackTrace();
         } catch (IllegalAccessException e) {
             e.printStackTrace();
         } catch (UnsupportedLookAndFeelException e) {
             e.printStackTrace();
         }
         setTitle("Process Scheduling");
         JPanel panel = new JPanel();
         getContentPane().add(panel, BorderLayout.CENTER);
         panel.setLayout(new BorderLayout(0, 0));
         
         // allows the user to configure the simulation
         JPanel setup = new JPanel();
         panel.setPreferredSize(new Dimension(900,900));
         setup.setPreferredSize(new Dimension(300,500));
         panel.add(setup, BorderLayout.EAST);
         setup.setLayout(new MigLayout("", "[grow]", "[][][][][][][][][][][][][][][][][][][][][grow][][grow]"));
         
         JLabel lblSetup = new JLabel("Setup");
         setup.add(lblSetup, "cell 0 0");
         
         // A reset button
         JButton btnGenereateProcesses = new JButton("Reset");
         btnGenereateProcesses.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
 
                 resetSimulation();
             }
         });
         setup.add(btnGenereateProcesses, "cell 0 1,growx");
         // A togglebutton to start/stop the simulation
         tglbtnStart = new JToggleButton("Start");
         tglbtnStart.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if(tglbtnStart.isSelected()) {
                     // We can only run the simulation 
                     // once
                     if(!isStarted) {
                         worker = new Worker();
                         worker.execute();
                         tglbtnStart.setText("Stop");
                     }
                 } else {
                     worker.cancel(true);
                     tglbtnStart.setText("Start");
                 }
             }
         });
         setup.add(tglbtnStart, "cell 0 2,growx");
         
         JSeparator separator_1 = new JSeparator();
         setup.add(separator_1, "cell 0 3,growx");
         
         JLabel lblQuantum = new JLabel("Quantum (ms)");
         setup.add(lblQuantum, "cell 0 4");
         
         // A spinner to let the user define the quantum
         quantumModel = new SpinnerNumberModel(250, 1, 1000, 10);
         JSpinner quantumSpinner = new JSpinner(quantumModel);
         setup.add(quantumSpinner, "cell 0 5,growx");
         
         JLabel lblAlgorithm = new JLabel("Process Scheduling Algorithm");
         setup.add(lblAlgorithm, "cell 0 6");
         
         // Allow the user to select what algorithm to use
         algorithmSelect = new JComboBox(algStrings);
         algorithmSelect.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent arg0) {
                 resetSimulation();
             }
         });
         setup.add(algorithmSelect, "cell 0 7,growx");
         
         // Setup the number of initial processes
         JLabel lblProcesses = new JLabel("Initial Processes");
         setup.add(lblProcesses, "cell 0 8");
         
         processModel = new SpinnerNumberModel(10, 1, 100, 1);
         JSpinner processCount = new JSpinner(processModel);
         processCount.addChangeListener(new ChangeListener() {
             
             @Override
             public void stateChanged(ChangeEvent arg0) {
                 resetSimulation();
             }
         });
         setup.add(processCount, "cell 0 9,growx");
         
         // New processes can be randomly added with a given probability
         JLabel lblProbabilityOfNew = new JLabel("Probability of New Process (%)");
         setup.add(lblProbabilityOfNew, "cell 0 10");
         
         newProcProbModel = new SpinnerNumberModel(25,0,100,1);
         JSpinner newProcProb = new JSpinner(newProcProbModel);
         setup.add(newProcProb, "cell 0 11,growx");
         
         // Input for the number of possible processes
         JLabel lblMaxNumberOf = new JLabel("Time Interval for New Process (ms)");
         setup.add(lblMaxNumberOf, "cell 0 12");
         
         newProcsModel = new SpinnerNumberModel(100,100,30000,100);
         JSpinner newProcs = new JSpinner(newProcsModel);
         setup.add(newProcs, "cell 0 13,growx");
         
         // Set the simulation delay multiplier
         JLabel lblSimulationDelay = new JLabel("Simulation Delay");
         setup.add(lblSimulationDelay, "cell 0 14");
         
         delayModel = new SpinnerNumberModel(5,1,20,1);
         JSpinner delay = new JSpinner(delayModel);
         setup.add(delay, "cell 0 15,growx");
         
         JSeparator separator = new JSeparator();
         setup.add(separator, "cell 0 16,growx");
         
         JLabel lblMemoryAllocationAlgorithm = new JLabel("Memory Allocation Algorithm");
         setup.add(lblMemoryAllocationAlgorithm, "cell 0 17");
         
         memoryAllocationAlg = new JComboBox(memoryAllocationAlgorithms);
         setup.add(memoryAllocationAlg, "cell 0 18,growx");
         
         memorySizeModel = new SpinnerNumberModel(512,16,4096,8);
         
         rdbtnStaticMemory = new JRadioButton("Static");
         buttonGroup.add(rdbtnStaticMemory);
         setup.add(rdbtnStaticMemory, "flowx,cell 0 19,growx");
         
         pageSizeModel = new SpinnerNumberModel(8,1,Math.pow(2, 10),8);
         
         JPanel panel_memoryInput = new JPanel();
         setup.add(panel_memoryInput, "cell 0 20,growx,aligny top");
         panel_memoryInput.setLayout(new BorderLayout(0, 0));
         
         final JPanel panel_static = new JPanel();
         panel_memoryInput.add(panel_static,BorderLayout.WEST);
         panel_static.setLayout(new MigLayout("", "[][grow,fill]", "[20px][]"));
         
         JLabel lblMemorySize = new JLabel("Memory Size (B)");
         panel_static.add(lblMemorySize, "cell 0 0");
         JSpinner spinner_memory_size = new JSpinner(memorySizeModel);
         panel_static.add(spinner_memory_size, "flowy,cell 1 0,growx,aligny top");
         
         JLabel lblPageSize = new JLabel("Page Size (B)");
         panel_static.add(lblPageSize, "cell 0 1,alignx left,aligny center");
         JSpinner spinner_page_size = new JSpinner(pageSizeModel);
         panel_static.add(spinner_page_size, "cell 1 1,growx,aligny top");
         
         final JPanel panel_dynamic = new JPanel();
         panel_memoryInput.add(panel_dynamic,BorderLayout.CENTER);
         panel_dynamic.setLayout(new MigLayout("", "[][grow,fill]", "[][][][][][]"));
         
         JLabel lblBlockSize = new JLabel("Block Size (B)");
         panel_dynamic.add(lblBlockSize, "cell 0 0");
         
         JLabel lblCount = new JLabel("Count");
         panel_dynamic.add(lblCount, "cell 1 0");
         
         JLabel lblNnew = new JLabel("2");
         panel_dynamic.add(lblNnew, "cell 0 1");
         
         block2Model = new SpinnerNumberModel(5,0,100,1);
         block4Model = new SpinnerNumberModel(5,0,100,1);
         block16Model = new SpinnerNumberModel(10,0,100,1);
         block32Model = new SpinnerNumberModel(10,0,100,1);
         block64Model = new SpinnerNumberModel(10,0,100,1);
         
         JSpinner spinner_block2 = new JSpinner(block2Model);
         panel_dynamic.add(spinner_block2, "cell 1 1,growx");
         
         JLabel lblNewLabel = new JLabel("4");
         panel_dynamic.add(lblNewLabel, "cell 0 2");
         
         JSpinner spinner_blocksize4 = new JSpinner(block4Model);
         panel_dynamic.add(spinner_blocksize4, "cell 1 2,grow");
         
         JLabel lblNewLabel_1 = new JLabel("16");
         panel_dynamic.add(lblNewLabel_1, "cell 0 3");
         
         JSpinner spinner_blocksize16 = new JSpinner(block16Model);
         panel_dynamic.add(spinner_blocksize16, "cell 1 3,grow");
         
         JLabel lblNewLabel_2 = new JLabel("32");
         panel_dynamic.add(lblNewLabel_2, "cell 0 4");
         
         JSpinner spinner_blocksize32 = new JSpinner(block32Model);
         panel_dynamic.add(spinner_blocksize32, "flowy,cell 1 4,grow");
         
         JLabel lblNewLabel_3 = new JLabel("64");
         panel_dynamic.add(lblNewLabel_3, "cell 0 5");
         
         JSpinner spinner_blocksize64 = new JSpinner(block64Model);
         panel_dynamic.add(spinner_blocksize64, "cell 1 5,grow");
         
         JScrollPane scrollPane = new JScrollPane();
         setup.add(scrollPane, "cell 0 21 1 2,grow");
         
         // Have a textarea for logging output
         textArea = new JTextArea();
         textArea.setWrapStyleWord(true);
         textArea.setTabSize(4);
         DefaultCaret caret = (DefaultCaret)textArea.getCaret();
         caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
         scrollPane.setViewportView(textArea);
         
         rdbtnDynamicMemory = new JRadioButton("Dynamic");
         buttonGroup.add(rdbtnDynamicMemory);
         setup.add(rdbtnDynamicMemory, "cell 0 19,growx");
         
         ChangeListener memselect = new ChangeListener() {
             
             @Override
             public void stateChanged(ChangeEvent e) {
                 if(rdbtnDynamicMemory.isSelected()){
                     panel_dynamic.setVisible(true);
                     panel_static.setVisible(false);
                 } else {
                     panel_dynamic.setVisible(false);
                     panel_static.setVisible(true);
                 }
             }
         };
         
         rdbtnDynamicMemory.addChangeListener(memselect);
         rdbtnStaticMemory.addChangeListener(memselect);
         rdbtnDynamicMemory.setSelected(true);
         JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
         panel.add(tabbedPane, BorderLayout.CENTER);
         
         panel_processes = new JPanel();
         tabbedPane.addTab("Processes", null, panel_processes, "Processes");
         panel_processes.setLayout(new GridLayout(0, 5, 4, 4));
         
         panel_memory = new JPanel();
         tabbedPane.addTab("Memory", null, panel_memory, "The memory");
         panel_memory.setLayout(new GridLayout(0, 8, 4, 4));
         
         panel_swap = new JPanel();               
         tabbedPane.addTab("Swap", null, panel_swap, "The Swap");
         panel_swap.setLayout(new GridLayout(5, 4, 4, 4));
 
    //     setupProcesses();
             
             
         }; 
         /**
          * A SwingWorker that runs the simulation
          * Uses the index of the algorithm select combobox
          * to select which algorithm to run
          * 
          */
        
         
         private class Worker extends SwingWorker<Void, String> {
             Timer t;
             boolean addingA = true;
             @Override
             protected Void doInBackground() throws Exception {
                 /**
                  * Add new processes to the waiting queue
                  * at user defined interval with user defined 
                  * probability
                  */
                 final ArrayList<Proc> waitingProcessA = new ArrayList<Proc>() ;
                 final ArrayList<Proc> waitingProcessB = new ArrayList<Proc>() ;
 
                 t = new Timer(newProcsModel.getNumber().intValue()*delayModel.getNumber().intValue(),new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent arg0) {
                         Random rng = new Random();
                         if(rng.nextInt(100) <= newProcProbModel.getNumber().intValue()) {
                             // If Swap is full, do not add more processes
                             int taken = 0;
                             for (int i = 0; i < swap.length; i++) {
                                 if(swap[i] != null)
                                     taken++;
                             }
                             if(taken == swap.length-1)
                                 return;
                             if(addingA) {
                                 waitingProcessA.add(addProcess());
                             } else{
                                 waitingProcessB.add(addProcess());
                             }
                         }
                     }
                 });
                 t.start();
 
                 
                 switch (algorithmSelect.getSelectedIndex()) {
                 case 0:
                     // Run round robin
                     int quantum = quantumModel.getNumber().intValue();
                     while(!processList.isEmpty()) {
                         ListIterator<Proc> it = processList.listIterator(last);
                         try{
                             while(it.hasNext()) {
                                 if(isCancelled()) return null;
                                 if(!waitingProcessA.isEmpty() || !waitingProcessB.isEmpty())
                                     throw new ConcurrentModificationException();
                                 Proc p = it.next(); 
                                 last++;
                                 // If the process is finished, remove it from the list
                                 if(p.takeTime(quantum) || p.getTime() == 0){
                                     it.remove();
                                     last--;
                                 } 
                             }
                             last = 0;
                         } catch (ConcurrentModificationException e) {
                             // An element has been added, just start the iteration again from
                             // where we were up to using the last variable
                             // This is stupid and not actually RR, but generated processes
                             // are executed for the quantum when they are generated
                             ArrayList<Proc> w;
                             if(addingA)  {
                                 w = waitingProcessA;
                                 addingA  =false;
                             } else         {
                                 w = waitingProcessB;
                                 addingA = true;
                             }
                             for (Proc proc : w) {
                                 proc.takeTime(quantum);
                             }
                             w.clear();
                         }
                     }
                     break;
                 case 1:
                     // Run FIFO
                     while(!processList.isEmpty()) {
                         if(isCancelled()) return null;
                         Proc p = processList.removeFirst();
                         p.finish();
                     }
                     break;
                 case 2:
                     // Run SJF
                     while(!processList.isEmpty()) {
                         ListIterator<Proc> it = processList.listIterator();
                         Proc shortest = it.next();
                         while(it.hasNext()) {
                             if(isCancelled()) return null;
                             Proc p = it.next();
                             if(p.getTime() == 0) {
                                 it.remove();
                                 continue;
                             }
                             if(p.getTime() < shortest.getTime())
                                 shortest = p;
                         }
                         shortest.finish();
                         // Takes O(n) time to remove
                         processList.remove(shortest);
                     }
                 default:
                     break;
                 }
                 
                 return null;
             }
             
             @Override
             protected void process(List<String> chunk) {
                 for (String string : chunk) {
                     textArea.append(string+"\n");
                 }
             }
             
             @Override
             protected void done() {
                 tglbtnStart.setText("Start");
                 tglbtnStart.setSelected(false);
                 isStarted = false;
                 t.stop();
                 if(processList.isEmpty())
                     publish("Simulation Complete");
                 else
                     publish("Simulation Paused");
             }    
     }
     /**
      * Add a specific process to the simulation
      * @param p the process to add
      */
     private void addProcess(Proc p) {
         processList.add(p);
         textArea.append("Added pid "+p.getId()+" with time "+p.getTime()+"ms, size "+p.getProcessSize()+"B\n");
         panel_processes.add(p);
         panel_processes.revalidate();
     }
     /**
      * Add a new process to the simulation
      * Will increment the pid and give a random time between
      * 0 and 1000
      * @return the process that was added
      */
     
     private Proc addProcess() {
         Proc p = new Proc(pid++);
         addProcess(p);
         return p;
     }
     /***
      * This will reset the simulation and setup the processes
      * according to the user inputs
      */
     private void setupProcesses() {
         pid = 0;
         int procs = processModel.getNumber().intValue();
         processList.clear();
         panel_processes.removeAll();

        panel_processes.revalidate();
         
         for(int i = 0; i < procs; i++ ) {
             addProcess();
         }
     }
     /**
      * This will reset the memory
      * 
      */
     private void setupMemory() {
         panel_memory.removeAll();
         if(rdbtnDynamicMemory.isSelected()) 
             setupMemoryDynamic();
         else
             setupMemoryStatic();
         for (int i = 0; i < memoryBlocks.length; i++) {
             panel_memory.add(memoryBlocks[i]);
         }
         panel_memory.revalidate();
     }
     
     private void setupHDD() {
         panel_swap.removeAll();
         
         for(int i =0;i < swap.length;i++) {
             HDDCell hd = new HDDCell(i);
             swap[i] = hd;
             panel_swap.add(hd);
         }
         panel_swap.revalidate();
     }
     
      /**
      * Sets up the memory with blocks of dynamic size
      */
        
     private void setupMemoryDynamic() {
         blockId = 0;
         ArrayList<Block> blocks = new ArrayList<Main.Block>();
         addBlocksOfSize(2, block2Model.getNumber().intValue(), blocks);
         addBlocksOfSize(4, block4Model.getNumber().intValue(), blocks);
         addBlocksOfSize(16, block16Model.getNumber().intValue(), blocks);
         addBlocksOfSize(32, block32Model.getNumber().intValue(), blocks);
         addBlocksOfSize(64, block64Model.getNumber().intValue(), blocks);
         memoryBlocks = new Block[blocks.size()];
         for (int i = 0; i < blocks.size(); i++) {
             memoryBlocks[i] = blocks.get(i);
         }        
     }
     
      
     private void addBlocksOfSize(int blockSize, int blockCount, ArrayList<Block> blocks) {
         for(int i = 0; i < blockCount; i++) {
             blocks.add(new Block(blockId++,blockSize));
         }
         textArea.append("Creating "+blockCount+" blocks of size "+blockSize+"B\n");
         
     }
     /**
      * Initialises the memory in a static fashion
      */
     private void setupMemoryStatic() {
         int memorySize = memorySizeModel.getNumber().intValue();
         int pageSize   = pageSizeModel.getNumber().intValue();
         int pages = memorySize/pageSize;
         textArea.append("Creating "+pages+" blocks of size "+pageSize+"B\n");
         memoryBlocks = new Block[pages];
         for (int i = 0; i < memoryBlocks.length; i++) {
             memoryBlocks[i] = new Block(i,pageSize);
         }
     }
     /**
      * Allocates Memory to a given process
      * @param p the process we are assigning memory for
      * @param bytes The number of bytes used by this process
      * @return true if the memory was successfully allocated
      */
     private boolean allocateMemory(Proc p, int bytes ) {
         // "First Fit", "Best Fit", "Worst Fit", "Next Fit"
         int toAllocate = bytes;
         switch (memoryAllocationAlg.getSelectedIndex()) {
         
         case 1:
             // Allocated memory using PARTICION
             for (Block b : memoryBlocks) {
                 if(b.allocatedTo == null) {
                     b.allocateTo(p);
                     
                     p.memoryBlocks.add(b);
                     textArea.append(String.format("Process %d allocated block %d\n", p.getId(),b.getId()));
                     toAllocate -= b.getBlockSize();
                     if(toAllocate <= 0)
                         return true;
                 }
        }
             break;
         case 0:
             //BEST FIT
             for (Block b : memoryBlocks) {
                 if(p.getProcessSize() <= b.getBlockSize() && b.allocatedTo == null) {
                     b.allocateTo(p);
                     p.memoryBlocks.add(b);
                     return true;
                 }
             }
             // No block could be found to fit this process, so terminate it
             p.terminate();
             return false;
         case 2:
             //FIRST FIT
             for (Block b : memoryBlocks) {
                     b.allocateTo(p);
                     p.memoryBlocks.add(b);
                     return true;
                 }
             // No block could be found to fit this process, so terminate it
             p.terminate();
             break;
         case 3:
             // resursive fit
             // Divide Memory Up
             TreeMap<Integer, LinkedList<Block>> memoryOfSize = new TreeMap<Integer, LinkedList<Block>>();
             memoryOfSize.put(2, new LinkedList<Block>());
             memoryOfSize.put(4, new LinkedList<Block>());
             memoryOfSize.put(16, new LinkedList<Block>());
             memoryOfSize.put(32, new LinkedList<Block>());
             memoryOfSize.put(64, new LinkedList<Block>());
             for (Block b : memoryBlocks) {
                 if(b.allocatedTo == null) {
                     memoryOfSize.get(b.getBlockSize()).add(b);
                 }
             }
             
             return recursivelyAllocateSmallest(memoryOfSize, p, toAllocate,64);            
             
         default:
             break;
         }
         return false;
         
     }
     /**
      * 
      * @param memoryOfSize
      * @param p
      * @param toAllocate
      * @return true if memory could be allocated, false otherwise
      */
     private boolean recursivelyAllocateSmallest(
             TreeMap<Integer, LinkedList<Block>> memoryOfSize, Proc p,
             int toAllocate, int maxBlockSize) {
         if(toAllocate <= 0) 
             return true;
         if(memoryOfSize.get(maxBlockSize).isEmpty()){
             switch (maxBlockSize) {
             case 64:
                 maxBlockSize = 32;
                 break;
             case 32:
                 maxBlockSize = 16;
                 break;
             case 16:
                 maxBlockSize = 4;
                 break;
             case 4:
                 maxBlockSize = 2;
                 break;
             case 2:
                 return false;
             default:
                 break;
             }
         }
         for (Entry<Integer, LinkedList<Block>> sc : memoryOfSize.entrySet()) {
             if((toAllocate <= sc.getKey() || sc.getKey() == maxBlockSize) && !sc.getValue().isEmpty()) {
                 Block b =  sc.getValue().pop();
                 allocateBlock(p,b);
                 return recursivelyAllocateSmallest(memoryOfSize, p, toAllocate-b.getBlockSize(),maxBlockSize);
             }
         }
         return false;
     }
     private void allocateBlock(Proc p, Block b) {
         b.allocateTo(p);
         p.memoryBlocks.add(b);
         textArea.append(String.format("Process %d allocated block %d\n", p.getId(),b.getId()));
     }
     /** 
      * Removes the process' memory allocation. Memory is removed completely
      * @param p the process to take the memory from
      */
     private void deallocateMemory(Proc p) {
         for (Block i : p.memoryBlocks) {
            textArea.append(String.format("Mem. Block %d deallocated from process %d\n", i.getId(),p.getId()));
             i.setBackground(Color.LIGHT_GRAY);
             i.allocateTo(null);
         }
        p.memoryBlocks.clear();
     }
     /**
      * This runs the process
      * @param args
      */
     public static void main(String args[]) {
         Main r = new Main();
         r.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         r.pack();
         r.setVisible(true);
     }
     
     public abstract class Cell extends JPanel { 
         protected JLabel idLbl;
         protected int id;
         Cell(int id) {
             setLayout(new GridLayout(0,1));
             resetBorder(Color.green);
             idLbl = new JLabel(""+id,JLabel.CENTER);
             this.id = id;
             add(idLbl);
         }    
         /**
          * 
          * @return the id of this process
          */
         public int getId() {
             return id;
         }
         /**
          * Changes the color of the border
          * @param c the color to use
          */
         protected void resetBorder(Color c) {
             setBorder(BorderFactory.createLineBorder(c, 3));
         }
     }
     /**
      * A block on the HDD
      * @author Jonathan
      *
      */
 
     private class HDDCell extends Cell {
         private JLabel processLbl = new JLabel("",JLabel.CENTER);
         private Proc storedProcess;
         /**
          * @return the storedProcess
          */
         public Proc getStoredProcess() {
             return storedProcess;
         }
         private int blockSize = 1024;
         HDDCell(int id) {
             super(id);
             setLayout(new GridLayout(0,1));
             swapProcess(null);
             resetBorder(Color.black);
             add(processLbl);
         }
         /**
          * Store a process in this hdd space
          * @param p
          */
         public void swapProcess(Proc p) {
             storedProcess = p;
             if(p == null) {
                 processLbl.setText("");
                 setToolTipText(String.format("HDD Block %d of size %d",id,blockSize ));
                 setBackground(UIManager.getColor ( "Panel.background" ));
             } else {
                 processLbl.setText("PID: "+storedProcess.getId());
                 textArea.append("Swap block "+getId()+" storing process "+storedProcess.getId()+"\n");
                 setToolTipText(String.format("HDD Block %d of size %d holding process %d",id,blockSize,storedProcess.getId()));
                 setBackground(p.getColor());
             }
         }
     }
     
     
     
     private class Block extends Cell {
         private Proc allocatedTo = null;
         private JLabel processLbl = new JLabel("",JLabel.CENTER);
         private JLabel sizeLbl = new JLabel("",JLabel.CENTER);
         // A block of bytes for use later
         private byte[] bytes;
         Block(int id, int bytesUsed) {
             super(id);
             bytes = new byte[bytesUsed];
             setToolTipText(String.format("Memory block %d starting at %d", id, id*pageSizeModel.getNumber().intValue()));
             sizeLbl.setText(bytesUsed+"B");
             add(sizeLbl);
             add(processLbl);
             resetBorder(Color.black);
         }
         /**
          * Return the number of bytes consumed by this block
          */
         public int getBlockSize() {
             return bytes.length;
         }
         public void allocateTo(Proc p) {
             allocatedTo = p;
             if(p == null) {
                 processLbl.setText("");
                 setBackground(UIManager.getColor ( "Panel.background" ));
             } else {
                 processLbl.setText("PID:"+p.getId());
                 setBackground(p.getColor());
             }
         }
     }
     /**
      * This class represents a process, each process has an id and time remaining
      * It also has a border that indicates if it is running, finished or waiting.
      * These are all managed internally.
      */
     private class Proc extends Cell{
         private JLabel timeLbl;
         private boolean inMemory = true; // If it's not in memory it must be in swap. All processes start in memory
         private static final long serialVersionUID = -2070799490577412344L;
         private int time,id, processSize;
         // Memory blocks that this process is using
         private LinkedList<Block> memoryBlocks = new LinkedList<Block>(); 
         // A list of child processes
         private TreeSet<Proc> childProcesses = new TreeSet<Main.Proc>();
         private boolean allocated = true;
         private Random rng = new Random();
         
         private Color color = generateRandomColor(new Color(255,255,255));
         Proc(int id) {
             super(id);
             time = rng.nextInt(1000)+1;
             processSize = rng.nextInt(64)+1;
             setToolTipText(String.format("Process %d of size %dB",id,processSize));
             setBackground(color);
             this.id = id;
             setLayout(new GridLayout(0,1));
             resetBorder(Color.green);
             timeLbl = new JLabel(time+"ms",JLabel.CENTER);
             add(timeLbl);
             add(new JLabel(processSize+"B",JLabel.CENTER));
             allocate();
         }
         public int getTime() {
             return time;
         }
         public void addChildProcess(Proc p) {
             childProcesses.add(p);
         }
         public TreeSet<Proc> getChildProcesses() {
             return childProcesses;
         }
         private boolean allocate() {
             if(allocateMemory(this, processSize)) {
                 allocated = true;
                 textArea.append("Process "+id+" allocated all memory\n");
             } else {
                 allocated = false;
                 textArea.append("Process "+id+" could not allocate memory\n");
                 swapOut(this);
                 deallocateMemory(this);
             }
             return allocated;
         }
         private int getProcessSize() {
             return processSize;
         }
         /**
          * Finish the process by simulating its remaining time
          * @throws InterruptedException 
          */
         private Color generateRandomColor(Color mix) {
 
             Random random = new Random();
             int red = random.nextInt(256);
             int green = random.nextInt(256);
             int blue = random.nextInt(256);
 
             // mix the color
             if (mix != null) {
                 red = (red + mix.getRed()) / 2;
                 green = (green + mix.getGreen()) / 2;
                 blue = (blue + mix.getBlue()) / 2;
             }
 
             Color color = new Color(red, green, blue);
             return color;
         }
         private Color getColor() {
             return color;
         }
         /**
          * Terminates the process
          */
         public void terminate() {
             time = 0;
             deallocateMemory(this);
             resetBorder(Color.blue);
             textArea.append(String.format("Process %d was terminated\n",id));
         }
         public void finish() throws InterruptedException {
             takeTime(time);
         }
         public String toString() {
             return String.format("%d,%d",id,time);
         }
 
         /**
          * Subtract the time from this process, simulating the execution
          * by sleeping the current thread
          * @param q the time to execute for
          * @return true if the process finished, otherwise false
          * 
          */
         public boolean takeTime(int q)  {
             if(!inMemory){
                 swapIn(this);
             }
             if(getTime() == 0)
                 return true;
             if(!allocated) {
                 resetBorder(Color.red);
                 // Attempt to get memory
                 if(!allocate())
                     return false;
             } 
             resetBorder(Color.red);
             time -= q;
             int sleep =q;
             int delayFactor = delayModel.getNumber().intValue();
             try {
                 if(time <= 0) {
                     sleep += time;
                     Thread.sleep((time+q)*delayFactor);
                 } else {
                     Thread.sleep(q*delayFactor);
                 }
             } catch (InterruptedException e) {
                 // Reset colour when sleep is interrupted,
                 // probably because the stop button was pressed
                 resetBorder(Color.green);
             }
             textArea.append("Process "+id+" executed for: "+sleep+"ms\n");
             if(time <= 0) {
                 
                 resetBorder(Color.black);
                 textArea.append("Process "+id+" finished\n");
                 time = 0;
             } else {
                 resetBorder(Color.green);
             }
 
             timeLbl.setText(time+"ms");
             if(time == 0) {
                 deallocateMemory(this);
                 return true;
             } else {
                 swapOut(this);
                 return false;
             }
         }
         public void setInMemory(boolean b) {
            inMemory = b;
         }
         public boolean getInMemory() {
             return inMemory;
         }
     }
     /**
     * Move a process into swap
      * @param proc
      * @return true if the process is loaded onto hdd
      */
     public boolean swapOut(Proc proc) {
         // TODO Auto-generated method stub
         for (Proc p : proc.getChildProcesses()) {
             swapOut(p);
         }
         if(!proc.getInMemory())
             return true;
         textArea.append("Swapping out process "+proc.getId()+"\n");
         // Find space in swap[] and call swapProcess if it doesn't already have a process in it
         for (HDDCell h : swap) {
             if(h.getStoredProcess() == null) {
                 h.swapProcess(proc);
                 proc.setInMemory(false);
                 deallocateMemory(proc);
                 return true;
             }
         }
         textArea.append("Could not swap out process "+proc.getId()+"\n");
         return false;
         
     }
     /**
      * Swap a process into memory
      * @param proc
      * @return true if the process is in memory  
      */
     public boolean swapIn(Proc proc) {
         for (Proc p : proc.getChildProcesses()) {
             swapIn(p);
         }
         if(proc.getInMemory())
             return true;
         // Find proc in swap[] and call allocateMemory on it
         for(HDDCell h : swap) {
             if(h.getStoredProcess() == proc) {
                 textArea.append("Swapping in process "+proc.getId()+"\n");
                 if(allocateMemory(proc,proc.getProcessSize())) {
                     h.swapProcess(null);
                     proc.setInMemory(true);
                     return true;
                 } else {
                     proc.setInMemory(false);
                     break;
                 }
             }
         }
        textArea.append("Memory is full or proc not in swap, process "+proc.getId()+" was not swapped into memory\n");
         return false;
     }
     /**
      * 
      */
     private void resetSimulation() {
         textArea.setText("");
         setupMemory();
         setupHDD();
         setupProcesses();
         
     }
 }
