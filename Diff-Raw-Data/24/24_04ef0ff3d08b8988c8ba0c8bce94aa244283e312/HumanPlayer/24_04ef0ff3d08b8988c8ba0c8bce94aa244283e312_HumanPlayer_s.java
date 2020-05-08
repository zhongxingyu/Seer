 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.geom.AffineTransform;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
 import java.text.DecimalFormat;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 /**
  * Spawned from the Simulator after "Play Human" option is clicked Gets host,
  * port, nGambes (and the datafile name) from the Simulator at construction
  * Creates GUI for a user to type in name and start, when game starts and it's
  * user's turn, they can select parameters to send in their bets for each gamble
  * game continues until the user quits.
  * 
  * @author ajk377
  * 
  */
 public class HumanPlayer {
   UserWindow userWindow;
   String name;
   private final int nGambles, port;
  private final String fName, host;
   private BufferedReader br;
   private BufferedWriter bw;
   private static final int windowWidth = 400;
private static final int sliderHeight = 30;
   public HumanPlayer(String host, int port, int nGambles, String fName){
    this.fName = fName;
     this.nGambles = nGambles;
     this.host = host;
     this.port = port;
     // set up GUI
     // make GUI to get user input
     userWindow = new UserWindow();
    userWindow.setSize(windowWidth, 65+nGambles*(sliderHeight+15)); //100for the top 
     userWindow.setBackground(Color.WHITE);
     userWindow.setVisible(true);
   }
 
   /**
    * Called when userWindow's start button gets pushed (after user registers his
    * name)
    * 
    * @param host
    * @param port
    */
   private void play(Double[] allocs) {
     if (connectionEstablished) {
    	//String in;
     // play=send the results
       String out = convertToString(nGambles, allocs);
       try {
         bw.write(out + "\n");
         bw.flush();
       } catch (Exception e) {
         e.printStackTrace();
       }
     }
   }
 
   public void connect(String name) {
     // connect & send name
     System.out.println("connecting");
     try {
       Socket s = new Socket(host, port);
       bw = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
       br = new BufferedReader(new InputStreamReader(s.getInputStream()));
       bw.write(name + "\n");
       bw.flush();
       String in = br.readLine();
       if (!in.equals("OK")) {
         System.out.println("got back: " + in);
         throw new RuntimeException(in);
       }
     } catch (Exception e) {
       e.printStackTrace();
     }
     // wait for OK
 
     connectionEstablished = true;
   }
 
   boolean connectionEstablished = false;
 
   public String convertToString(int nGambles, Double[] allocs) {
     DecimalFormat df = new DecimalFormat("0.00000");
     StringBuffer sb = new StringBuffer(nGambles * 8);
     for (int j = 0; j < nGambles; j++)
       sb.append(df.format(allocs[j])).append(" ");
     String out = sb.toString();
     System.out.println("sending: " + out);
     return out;
   }
 
   public static void main(String[] args) throws Exception {
     if (args.length != 4) {
       System.out.println("usage:  Java Player"
           + " <Host> <Port> <Ngambles> <Datafilename>");
       System.exit(1);
     }
     new HumanPlayer(args[0], Integer.parseInt(args[1]),
         Integer.parseInt(args[2]), args[3]);
   }
 
   class UserWindow extends JFrame {
     private static final long serialVersionUID = 1L;
     Font f1 = new Font("Dialog", Font.BOLD, 12);
     Font f2 = new Font("Dialog", Font.PLAIN, 12);
 
     FontMetrics fm = getFontMetrics(f2);
 
     JButton bPlay, bSetName, bStart;
     JTextField nameField;
     ResultsPanel pInputs;
 
     int idxOrder = -1;
     double[] rets = new double[nGambles];
     double[] drs = new double[nGambles];
     Color[] colors = new Color[nGambles];
 
     AffineTransform atVert = AffineTransform.getRotateInstance(-Math.PI / 2);
     String name;
 
     public UserWindow() {
       super("Human Player");
       this.name = "Human Player";
       buildGUI();
       addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
           System.exit(0);
         }
       });
     }
     JLabel lname;
 
     void buildGUI() {
       lname = new JLabel("Name: " + name);
       JLabel lAttrs = new JLabel("Game with " + nGambles + " gambles");
       lname.setFont(f1);
       lAttrs.setFont(f2);
       bStart = new JButton("Start");
       bPlay = new JButton("Send");
       bSetName = new JButton("Set Name");
       nameField = new JTextField("Type your name", 10);
       bPlay.setFont(f1);
       bSetName.setFont(f1);
       bSetName.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
           String newName = nameField.getText();
           System.out.println(nameField.getText());
           if (!newName.equals("Type your name")) {
             name = newName;
             lname.setText("Name: " + name);
             repaint();
           }
         }
       });
       bStart.setFont(f1);
       bPlay.setEnabled(false);
       bStart.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
           // get name from the text area or something, set it to
           // makes panel like selectable(ungray it)
           bPlay.setEnabled(true);
           bStart.setEnabled(false);
           bSetName.setEnabled(false);
           connect(name);
         }
       });
       bPlay.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
         	// get contents from the panel
         	// if turn..?
           Double[] bets = pInputs.getBets();
           play(bets);
         }
       });
       JPanel pTop = new JPanel();
 
       pTop.add(lname);
       pTop.add(lAttrs);
       pTop.add(bPlay);
       pTop.add(nameField);
       pTop.add(bSetName);
       pTop.setPreferredSize(new Dimension(100, 65));
       JPanel pMid = new JPanel();
       pMid.add(bPlay);
       pMid.add(bStart);
       pInputs = new ResultsPanel(nGambles);
       pInputs.setBackground(Color.white);
       pInputs.setPreferredSize(new Dimension(100, nGambles*100));
       Box boxNorth = Box.createVerticalBox();
       //boxNorth.setPreferredSize(new Dimension(nGambles*100+10, 65));
       boxNorth.add(pTop);
       boxNorth.add(Box.createVerticalStrut(5));
       boxNorth.add(pMid);
       boxNorth.add(pInputs);
       boxNorth.add(Box.createVerticalStrut(5));
       JPanel pane = (JPanel) getContentPane();
       pane.setLayout(new BorderLayout());
       pane.setBorder(new EmptyBorder(5, 5, 5, 5));
       pane.add(boxNorth, BorderLayout.NORTH);
     }
 
   }
 
   static class MySlider extends JSlider {
     // num is an index into the bets/slids array
     int num;
     static Font f2 = new Font("Dialog", Font.PLAIN, 12);
 
     public MySlider() {
     	super();
     	this.setMajorTickSpacing(10);
     	this.setMinorTickSpacing(1);
     	this.setFont(f2);
     	this.setPaintTicks(true);
     	this.setPaintLabels(true);
     	repaint();
     }
     
     public void setNum(int num) {
       this.num = num;
      JLabel sNum = new JLabel(num + " ");	
      sNum.setFont(f2);
      add(sNum);
     }
 
     public int getNum() {
       return this.num;
     }
 
   }
 
   static class ResultsPanel extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	Double[] bets;
     MySlider[] slids;
     int nGambles;
 
     public ResultsPanel(int nGambles) {
       super();
       setPreferredSize(new Dimension(200, 300));
       setBorder(new BevelBorder(BevelBorder.LOWERED));
       this.nGambles = nGambles;
       this.bets = new Double[nGambles];
       this.slids = new MySlider[nGambles];
       setBets();
     }
 
     public void setBets() {
      for (int i = this.nGambles - 1; i > 0; i--) {
         // make a slider for each gamble
         slids[i] = new MySlider(); // horiz with 1-100 def 50
         slids[i].setNum(i);
         slids[i].addChangeListener(sliderChanged);
         slids[i].setMajorTickSpacing(10);
         slids[i].setMinorTickSpacing(1);
         slids[i].setPaintTicks(true);
         slids[i].setPaintLabels(false);
         slids[i].setPreferredSize(new Dimension(windowWidth-25, sliderHeight));
         bets[i] = 50.0;
                add(slids[i]);
        
         repaint();//?
       }
     }
 
     ChangeListener sliderChanged = new ChangeListener() {
 	    public void stateChanged(ChangeEvent e) {
 		MySlider source = (MySlider) e.getSource();
 		if (!source.getValueIsAdjusting()) {
 		    bets[(int) source.getNum()] = (double) source.getValue();
 		    System.out.println(source.getNum() + " changed to " + source.getValue());
 		}
 	    }
 	};
 
     public Double[] getBets() {
       return this.bets;
     }
   }
 }
