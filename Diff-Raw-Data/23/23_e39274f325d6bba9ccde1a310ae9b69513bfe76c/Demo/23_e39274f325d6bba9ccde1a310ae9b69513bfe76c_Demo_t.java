 package edu.cmu.cs.diamond.rsna2007;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.imageio.ImageIO;
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 import edu.cmu.cs.diamond.rsna2007.Truth.Biopsy;
 import edu.cmu.cs.diamond.rsna2007.Truth.MassMargin;
 import edu.cmu.cs.diamond.rsna2007.Truth.MassShape;
 
 public class Demo extends JFrame {
    final static private boolean FULL_SCREEN = true;
 
     final private CaseViewer caseViewer;
 
     final private List<Case> cases = new ArrayList<Case>();
 
     final private JButton prevButton = new JButton("Previous Case");
 
     final private JButton nextButton = new JButton("Next Case");
 
     protected int currentCase = 0;
 
     final private JLabel caseLabel = new JLabel();
 
     public static void main(String[] args) {
         Demo m;
 
         if (args.length < 2) {
             System.out.println("Usage: " + Demo.class.getName()
                     + " index_file filter_dir");
             System.exit(1);
         }
 
         File index = new File(args[0]);
         File filterdir = new File(args[1]);
 
         // verify files
         if (!(index.canRead() && index.isFile())) {
             System.out.println("error: index_file must be readable file");
             System.exit(1);
         }
 
         if (!(filterdir.canRead() && filterdir.isDirectory())) {
             System.out.println("error: filter_dir must be readable directory");
             System.exit(1);
         }
 
         GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment()
                 .getDefaultScreenDevice();
 
         try {
             m = new Demo(index, filterdir);
             m.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
             DisplayMode dm = gd.getDisplayMode();
 
             if (FULL_SCREEN) {
                 m.setUndecorated(true);
                 m.setResizable(false);
                 m.setBounds(0, 0, dm.getWidth(), dm.getHeight());
             } else {
                 m.setSize(m.getMinimumSize());
             }
 
             m.toFront();
             m.setVisible(true);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public Demo(File index, File filterdir) throws IOException {
         super("Diamond RSNA 2007");
 
         caseViewer = new CaseViewer(filterdir);
 
         setMinimumSize(new Dimension(800, 600));
 
         setBackground(Color.BLACK);
 
         Container cp = getContentPane();
         cp.setBackground(null);
 
         readIndex(index);
 
         setupWindow();
 
         updateButtonAndCaseState();
     }
 
     private void setupWindow() {
         add(new Banner(), BorderLayout.NORTH);
 
         add(caseViewer);
 
         Box h = Box.createHorizontalBox();
 
         prevButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 currentCase--;
 
                 updateButtonAndCaseState();
             }
         });
 
         nextButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 currentCase++;
 
                 updateButtonAndCaseState();
             }
         });
 
         caseLabel.setForeground(Color.WHITE);
 
         h.add(Box.createHorizontalGlue());
         h.add(prevButton);
         h.add(Box.createHorizontalGlue());
         h.add(caseLabel);
         h.add(Box.createHorizontalGlue());
         h.add(nextButton);
         h.add(Box.createHorizontalGlue());
 
         add(h, BorderLayout.SOUTH);
     }
 
     protected void updateButtonAndCaseState() {
         prevButton.setEnabled(currentCase != 0);
         nextButton.setEnabled(currentCase != cases.size() - 1);
 
         Case c = cases.get(currentCase);
         caseLabel.setText(c.getName());
         caseViewer.setCase(c);
 
         validate();
     }
 
     private void readIndex(File file) throws FileNotFoundException, IOException {
         File dir = file.getParentFile();
         String line;
 
         System.out.println("Reading truthfile");
         File truthfile = new File(dir, "Truthfile.txt");
         BufferedReader truthReader = new BufferedReader(new FileReader(
                 truthfile));
         Map<String, String[]> truth = new HashMap<String, String[]>();
 
         Pattern tp = Pattern.compile("(\\S+\\.img\\s+.*)");
         while ((line = truthReader.readLine()) != null) {
             Matcher m = tp.matcher(line);
             if (m.find()) {
                 // split
                 String splitLine[] = m.group(1).split("\\s+");
                 String key = (splitLine[0].substring(0, splitLine[0]
                         .lastIndexOf(".img")).intern());
                 String value[] = new String[splitLine.length - 1];
                 System.arraycopy(splitLine, 1, value, 0, value.length);
                 assert (!truth.containsKey(key));
                 truth.put(key, value);
             }
         }
         for (String key : truth.keySet()) {
             System.out.print(key + " -> [");
             for (String val : truth.get(key)) {
                 System.out.print(" " + val);
             }
             System.out.println(" ]");
         }
 
         System.out.println();
         System.out.println("Reading index file: " + file);
         BufferedReader reader = new BufferedReader(new FileReader(file));
 
         CasePiece rml = null, lml = null, rcc = null, lcc = null;
         String name = null;
 
         Pattern p = Pattern.compile("(.*)(LCC|LML|RCC|RML)(.*)\\.jpg");
 
         while ((line = reader.readLine()) != null) {
             Matcher m = p.matcher(line);
             if (m.matches()) {
                 final String exactName = (m.group(1) + m.group(2) + m.group(3))
                         .intern();
 
                 if (name == null) {
                     name = m.group(1) + "xxx" + m.group(3);
                 }
 
                 // build truth
                 String ti[] = truth.get(exactName);
 
                 ROI roi = null;
                 // try to load ROI file
                 File f[] = dir.listFiles(new FileFilter() {
 
                     public boolean accept(File pathname) {
                         String name = pathname.getName();
 
                         boolean result = name.contains(exactName)
                                 && name.endsWith(".text_attr");
                         if (result) {
                             System.out.println(" *** matched " + name);
                         }
                         return result;
                     }
 
                 });
                 if (f.length > 0) {
                     String s = f[0].getAbsolutePath();
                     File roiPix = new File(s.substring(0, s.lastIndexOf(".")));
                     roi = new ROI(new FileInputStream(f[0]), ImageIO
                             .read(roiPix));
                 }
 
                 Truth t = null;
                 if (ti != null) {
                     int birad = Integer.parseInt(ti[2]);
                     int subtlety = Integer.parseInt(ti[3]);
                     int density = Integer.parseInt(ti[4]);
                     int age = -1;
                     Truth.Biopsy biopsy = parseBiopsy(ti[6]);
                     Truth.MassShape shape = null;
                     Truth.MassMargin margin = null;
                     if (biopsy == Biopsy.BIOPSY_MALIGNANT) {
                         age = Integer.parseInt(ti[5]);
                         shape = parseShape(ti[7]);
                         margin = parseMargin(ti[8]);
                     }
 
                     t = new Truth(birad, subtlety, density, age, biopsy, shape,
                             margin, roi);
                     // System.out.println(t);
                 }
 
                 BufferedImage img = ImageIO.read(new File(dir, line));
                 CasePiece cp = new CasePiece(img, t);
 
                 String view = m.group(2).intern();
                 if (view == "LCC") {
                     lcc = cp;
                 } else if (view == "LML") {
                     lml = cp;
                 } else if (view == "RCC") {
                     rcc = cp;
                 } else if (view == "RML") {
                     rml = cp;
                 }
             }
 
             if (name != null && lcc != null && lml != null && rcc != null
                     && rml != null) {
                 System.out.println("Adding case " + name);
                 cases.add(new Case(rcc, lcc, rml, lml, name));
                 name = null;
                 lcc = lml = rcc = rml = null;
             }
         }
     }
 
     private MassMargin parseMargin(String s) {
         if (s.equals("CIRCUMSCRIBED")) {
             return MassMargin.MASS_MARGIN_CIRCUMSCRIBED;
         } else if (s.equals("ILL_DEFINED")) {
             return MassMargin.MASS_MARGIN_ILLDEFINED;
         } else if (s.equals("MICROLOBULATED")) {
             return MassMargin.MASS_MARGIN_MICROLOBULATED;
         } else if (s.equals("OBSCURED")) {
             return MassMargin.MASS_MARGIN_OBSCURED;
         } else if (s.equals("SPICULATED")) {
             return MassMargin.MASS_MARGIN_SPICULATED;
         } else {
             return null;
         }
     }
 
     private MassShape parseShape(String s) {
         if (s.equals("IRREGULAR")) {
             return MassShape.MASS_SHAPE_IRREGULAR;
         } else if (s.equals("LOBULATED")) {
             return MassShape.MASS_SHAPE_LOBULATED;
         } else if (s.equals("OVAL")) {
             return MassShape.MASS_SHAPE_OVAL;
         } else if (s.equals("ROUND")) {
             return MassShape.MASS_SHAPE_ROUND;
         } else {
             return null;
         }
     }
 
     private Biopsy parseBiopsy(String s) {
         if (s.equals("MALIGNANT")) {
             return Biopsy.BIOPSY_MALIGNANT;
         } else if (s.equals("BENIGN")) {
             return Biopsy.BIOPSY_BENIGN;
         } else {
             return null;
         }
     }
 }
