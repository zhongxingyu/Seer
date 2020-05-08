 /*
  * MassFind 2: A Diamond application for exploration of breast tumors
  *
  * Copyright (c) 2007-2008 Carnegie Mellon University. All rights reserved.
  * Additional copyrights may be listed below.
  *
  * This program and the accompanying materials are made available under
  * the terms of the Eclipse Public License v1.0 which accompanies this
  * distribution in the file named LICENSE.
  *
  * Technical and financial contributors are listed in the file named
  * CREDITS.
  */
 
 package edu.cmu.cs.diamond.massfind2;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.awt.image.WritableRaster;
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
 
 import org.jdesktop.swingx.graphics.GraphicsUtilities;
 
 import edu.cmu.cs.diamond.massfind2.Truth.Biopsy;
 import edu.cmu.cs.diamond.massfind2.Truth.MassMargin;
 import edu.cmu.cs.diamond.massfind2.Truth.MassShape;
 
 public class Main extends JFrame {
     private static final String FILTER_DIR_TXT = "filter_dir";
 
     private static final String INDEX_FILE_TXT = "index_file";
 
     final static private boolean FULL_SCREEN = true;
 
     final private CaseViewer caseViewer;
 
     final private List<Case> cases = new ArrayList<Case>();
 
     final private JButton prevButton = new JButton("Previous Case");
 
     final private JButton nextButton = new JButton("Next Case");
 
     protected int currentCase = 0;
 
     final private JLabel caseLabel = new JLabel();
 
     public static void main(String[] args) {
         Main m;
 
         if (args.length < 2) {
             System.out.println("Usage: " + Main.class.getName() + " "
                     + INDEX_FILE_TXT + " " + FILTER_DIR_TXT);
             System.exit(1);
         }
 
         File index = new File(args[0]);
         File filterdir = new File(args[1]);
         File exe = new File(index.getParent(), "CAD_RegionGrowth.exe");
 
         // verify files
         if (!(index.canRead() && index.isFile())) {
             System.out.println("error: " + INDEX_FILE_TXT
                     + " must be readable file");
             System.exit(1);
         }
 
         if (!(filterdir.canRead() && filterdir.isDirectory())) {
             System.out.println("error: " + FILTER_DIR_TXT
                     + " must be readable directory");
             System.exit(1);
         }
 
         if (!(exe.canRead() && exe.isFile())) {
             System.out.println("error: " + exe.toString()
                     + " must be executable file");
             System.exit(1);
         }
 
         try {
             m = new Main(index, filterdir, exe);
             m.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 
             if (FULL_SCREEN) {
                 m.setUndecorated(true);
                m.setBounds(0, 0, dim.width, dim.height);
             } else {
                 m.setSize(m.getMinimumSize());
             }
 
             m.toFront();
             m.setVisible(true);
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     public Main(File index, File filterdir, File regionFinderExe)
             throws IOException {
         super("Diamond MassFind");
 
         caseViewer = new CaseViewer(filterdir, regionFinderExe);
 
         setMinimumSize(new Dimension(800, 600));
 
         setBackground(Color.BLACK);
 
         Container cp = getContentPane();
         cp.setBackground(null);
 
         readIndex(index);
 
         setupWindow(new File(index.getParentFile(), "logos"));
 
         updateButtonAndCaseState();
     }
 
     private void setupWindow(File logoDir) {
         Banner banner = new Banner(logoDir);
 
         add(banner, BorderLayout.NORTH);
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
         File dir = file.getAbsoluteFile().getParentFile();
         String line;
 
         System.out.println("Reading truthfile");
         File truthfile = new File(dir, "Truthfile.txt");
         BufferedReader truthReader = new BufferedReader(new FileReader(
                 truthfile));
         Map<String, String[]> truth = new HashMap<String, String[]>();
 
         try {
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
             for (Map.Entry<String, String[]> e : truth.entrySet()) {
                 System.out.print(e.getKey() + " -> [");
                 for (String val : e.getValue()) {
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
 
             try {
                 while ((line = reader.readLine()) != null) {
                     Matcher m = p.matcher(line);
                     if (m.matches()) {
                         final String exactName = (m.group(1) + m.group(2) + m
                                 .group(3)).intern();
 
                         String parsedName = (m.group(1) + "xxx" + m.group(3))
                                 .intern();
                         if (name == null) {
                             name = parsedName;
                         } else {
                             // compare
                             if (name != parsedName) {
                                 throw new RuntimeException("Index file corrupt");
                             }
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
                             File roiPix = new File(s.substring(0, s
                                     .lastIndexOf(".")));
 
                             System.out
                                     .println("reading ROI image as " + roiPix);
                             roi = new ROI(new FileInputStream(f[0]), ImageIO
                                     .read(roiPix));
                             // roi = new ROI(new FileInputStream(f[0]),
                             // readMammogramImage(roiPix));
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
 
                             t = new Truth(birad, subtlety, density, age,
                                     biopsy, shape, margin, roi);
                             // System.out.println(t);
                         }
 
                         // first try .img format
                         String base = line.substring(0, line
                                 .lastIndexOf(".jpg"));
                         File imgFile = new File(dir, base + ".img");
                         System.out.println("reading " + imgFile);
                         BufferedImage img = readMammogramImage(imgFile);
 
                         // then, fall back to jpg
                         if (img == null) {
                             System.out.println(" falling back to jpeg");
                             imgFile = new File(dir, base + ".jpg");
                             img = GraphicsUtilities.toCompatibleImage(ImageIO
                                     .read(imgFile));
                             Graphics2D g = img.createGraphics();
                             g.setColor(new Color(0.0f, 0.0f, 1.0f, 0.2f));
                             g.fillRect(0, 0, img.getWidth(), img.getHeight());
                             g.dispose();
                         }
 
                         CasePiece cp = new CasePiece(img, t, imgFile
                                 .getCanonicalPath());
 
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
 
                     if (name != null && lcc != null && lml != null
                             && rcc != null && rml != null) {
                         System.out.println("Adding case " + name);
                         cases.add(new Case(rcc, lcc, rml, lml, name));
                         name = null;
                         lcc = lml = rcc = rml = null;
                     }
                 }
             } finally {
                 reader.close();
             }
         } finally {
             truthReader.close();
         }
     }
 
     private BufferedImage readMammogramImage(File file) throws IOException {
         DataInputStream in = null;
         try {
             in = new DataInputStream(new BufferedInputStream(
                     new FileInputStream(file)));
         } catch (FileNotFoundException e) {
             return null;
         }
 
         try {
             // skip to num_rows
             in.readFully(new byte[16 + 8 + 8 + 16 + 8 + 4]);
 
             // read h
             byte buf[] = new byte[8];
             in.readFully(buf);
             String num = new String(buf);
             int h = Integer.parseInt(num.substring(0, num.indexOf('\0')));
 
             // read w
             in.readFully(buf);
             num = new String(buf);
             int w = Integer.parseInt(num.substring(0, num.indexOf('\0')));
 
             // skip to image
             in.readFully(new byte[12 + 4 + 8 + 8 + 4 + 16 + 32 + 16 + 2 + 16
                     + 16 + 16 + 16 + 8 + 6]);
 
             short data[] = new short[w * h];
             System.out.println(" " + w + "x" + h);
             for (int i = 0; i < w * h; i++) {
                 // read pixels
                 data[i] = (short) (65535 - (in.readShort() << 4));
                 if (i % 10000 == 0) {
                     System.out.print(" " + (100 * i / (w * h)) + "  \r");
                     System.out.flush();
                 }
             }
             System.out.println();
             BufferedImage img = new BufferedImage(w, h,
                     BufferedImage.TYPE_USHORT_GRAY);
             WritableRaster wr = img.getRaster();
             wr.setDataElements(0, 0, w, h, data);
 
             return GraphicsUtilities.toCompatibleImage(img);
         } finally {
             in.close();
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
