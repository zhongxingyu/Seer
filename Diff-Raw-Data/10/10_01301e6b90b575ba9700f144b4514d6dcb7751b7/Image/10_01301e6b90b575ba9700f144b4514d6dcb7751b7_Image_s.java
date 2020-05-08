 package lt.banelis.aurelijus.data;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import javax.imageio.ImageIO;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import lt.banelis.aurelijus.connectors.Synchronizer;
 
 /**
  * Image representation.
  *
  * @author Aurelijus Banelis
  */
 public class Image extends AbstractDataStructure {
     private static final int MAX_WIDTH = 400;
     private static final int MAX_HEIGT = 400;
     private List<Boolean> data = new LinkedList<Boolean>();
     private BufferedImage image = null;
     private boolean loading = false;
     private JScrollPane container = new JScrollPane();
     private JButton sendButton = new JButton("Siųsti kanalu");
     private static Component self = null;
             
     public Image(boolean inputEnabled) {
         super(inputEnabled);
         if (inputEnabled) {
             initialiseEditable();
         } else {
             initialiseVisible();
         }
     }
     
     
     /*
      * Data storage
      */
     
     @Override
     protected void putDataImplementation(Collection<Boolean> data) {
         this.data.addAll(data);
         binaryToImage(data);
     }
     
     private void binaryToImage(Collection<Boolean> data) {
         Iterator<Boolean> iterator = data.iterator();
         int width = toInteger(getNextInteger(iterator));
         int height = toInteger(getNextInteger(iterator));
         self = this;
         if (width > 0 && height > 0) {
             image = toImage(data);
         } else {
             image = toImage(this.data);
         }
         updateImage();
     }
     
     protected Collection<Boolean> viewData() {
         return data;
     }
 
     @Override
     protected Collection<Boolean> retrieveDataImplementation() {
         Collection<Boolean> toRetrieve = data;
         data = new LinkedList<Boolean>();
         updateImage();
         return toRetrieve;
     }
 
     @Override
     public void resetOwn() {
         data = new LinkedList<Boolean>();
         image = null;
         loading = false;
         sendButton.setEnabled(true);
     }
 
     
     /*
      * Data transformation
      */
     
     protected static Collection<Boolean> toBinary(BufferedImage image) {
         int size = image.getWidth() * image.getHeight() + 2;
         ArrayList<Boolean> result = new ArrayList<Boolean>(size);
         result.addAll(toBinary(image.getWidth()));
         result.addAll(toBinary(image.getHeight()));
         for (int y = 0; y < image.getHeight(); y++) {
             if (y % 20 == 0) {
                 Synchronizer.progress = y / (double) image.getHeight();
                 Synchronizer.updateProgress();
             }
             for (int x = 0; x < image.getWidth(); x++) {
                 result.addAll(toBinary(image.getRGB(x, y)));
             }
         }
         return result;
     }
     
     protected static BufferedImage toImage(Collection<Boolean> data) {
         Iterator<Boolean> iterator = data.iterator();
         int width = toInteger(getNextInteger(iterator));
         int height = toInteger(getNextInteger(iterator));
         if (width > 0 && height > 0) {
             BufferedImage image;
             if (width < MAX_WIDTH && height < MAX_HEIGT) {
                 image = new BufferedImage(width, height,
                                           BufferedImage.TYPE_INT_RGB);
             } else {
                 errorMessage("Atkuriamas mažesnis " + MAX_WIDTH + "x" +
                              MAX_HEIGT + " vietoj " + width + "x" + height +
                              "paveikslėlis");
                 image = new BufferedImage(MAX_WIDTH, MAX_HEIGT,
                                           BufferedImage.TYPE_INT_RGB);
             }
             for (int y = 0; y < height && y < MAX_HEIGT; y++) {
                 if (y % 20 == 0) {
                     Synchronizer.progress = y / (double) height;
                     Synchronizer.updateProgress();
                 }
                 for (int x = 0; x < width && x < MAX_WIDTH; x++) {
                     int color = toInteger(getNextInteger(iterator));
                     image.setRGB(x, y, color);
                 }
             }
             return image;
         } else {
             return null;
         }
     }
     
     protected static ArrayList<Boolean> toBinary(int number) {
         ArrayList<Boolean> result = new ArrayList<Boolean>(Integer.SIZE);
         long multipier = 1;
         for (int i = 0; i < Integer.SIZE; i++) {
             if ((number & multipier) != 0) {
                 result.add(Boolean.TRUE);
             } else {
                 result.add(Boolean.FALSE);
             }
             multipier <<= 1;
         }
         return result;
     }
     
     protected static int toInteger(Collection<Boolean> data) {
         int result = 0;
         long multiplier = 1;
         for (Boolean bit : data) {
             if (bit) {
                 result += multiplier;
             }
             multiplier <<= 1;
         }
         return result;
     }
     
     private static <T> Collection<T> getNextInteger(Iterator<T> iterator) {
         return getNext(iterator, Integer.SIZE);
     }
     
     private static <T> Collection<T> getNext(Iterator<T> iterator, int size) {
         ArrayList<T> part = new ArrayList<T>(size);
         for (int i = 0; i < size; i++) {
             if (iterator.hasNext()) {
                 part.add(iterator.next());
             }
         }
         return part;
     }
 
     
     /*
      * Graphical user interface
      */
     
     private void initialiseEditable() {
         JPanel controlls = new JPanel();
         controlls.setLayout(new BoxLayout(controlls, BoxLayout.X_AXIS));
         JButton load = new JButton("Pasirinkti failą");
         load.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 JFileChooser fileChooser = new JFileChooser();
                 fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                 if (fileChooser.showOpenDialog(container) ==
                     JFileChooser.APPROVE_OPTION) {
                     loadImage(fileChooser.getSelectedFile().getPath());
                 }
             }
         });
         controlls.add(load);
                 
         JButton example = new JButton("Pavyzdinis paveikslėlis");
         example.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 image = sampleImage(100, 100);
                 updateImage();
                 sendButton.setEnabled(true);
             }
         });
         controlls.add(example);
         
         sendButton.setEnabled(false);
         sendButton.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                if (image != null && image.getWidth() > MAX_WIDTH ||
                                     image.getHeight() > MAX_HEIGT) {
                     self = Image.this;
                     errorMessage("Dideli paveikslėliai bus ilgai koduojami " +
                             "ir užims daug atminties. Naudokite " + MAX_WIDTH +
                             "x" + MAX_HEIGT + " ir mažesnius paveikslėlius");
                 } else if (image != null) {
                     putData(toBinary(image));
                     sendButton.setText("Siųsti kanalu");
                     sendButton.setEnabled(true);
                 }
             }
         });
         controlls.add(sendButton);
         
         JButton finalize = new JButton("Paskutinis");
         finalize.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 putData(dataToSynchronize());
             }
         });
         finalize.setToolTipText("Sintėjo-gavėjo sinchronizacijai");
         controlls.add(finalize);
         
         setLayout(new BorderLayout());
         add(container, BorderLayout.CENTER);
         add(controlls, BorderLayout.SOUTH);
     }
     
     private void loadImage(final String file) {
         Thread openning = new Thread() {
             @Override
             public void run() {
                 try {
                     image = ImageIO.read(new File(file));
                 } catch (IOException ex) {
                     image = null;
                     errorMessage("Nepavyko atidaryti paveikslėlio: " + file);
                 }
                 updateImage();
                 loading = false;
                 sendButton.setEnabled(true);
             }
         };
         loading = true;
         sendButton.setEnabled(false);
         image = null;
         openning.start();
     }
     
     private static void errorMessage(String message) {
         if (self != null) {
             JOptionPane.showMessageDialog(self, message, "Klaida",
                                           JOptionPane.WARNING_MESSAGE);
         } else {
             System.err.println(message);
         }
         
     }
     
     protected static BufferedImage sampleImage(int width, int heigh) {
         BufferedImage result = new BufferedImage(width, heigh,
                                                 BufferedImage.TYPE_INT_RGB);
         Graphics g = result.getGraphics();
         for (int x = 0; x < width; x++) {
             int y = (int) (x / (double) width * heigh);
             float r = x / (float) width;
             float b = 1 - x / (float) width;
             float green = r * 0.5f;
             g.setColor(new Color(r, b, b));
             g.drawLine(x, 0, x, y);
             g.setColor(new Color(r, green, b));
             g.drawLine(x, y, x, heigh);
         }
         return result;
     }
     
     private void updateImage() {
         JPanel canvas = new JPanel() {
             @Override
             protected void paintComponent(Graphics g) {
                 super.paintComponent(g);
                 if (image != null) {
                     g.drawImage(image, 0, 0, null);
                 } else if (loading) {
                     g.setColor(Color.BLUE);
                     g.drawString("Atidaroma...", 5, 5);
                 } else {
                     g.setColor(Color.RED);
                     g.drawLine(0, 0, getWidth(), getHeight());
                     g.drawLine(0, getHeight(), getWidth(), 0);
                 }
             }
         };
         if (image != null) {
             canvas.setPreferredSize(new Dimension(image.getWidth(),
                                                   image.getHeight()));
             canvas.setToolTipText("Paveikslėlis: " + image.getWidth() + "x" +
                                                      image.getHeight());
         } else {
             canvas.setPreferredSize(new Dimension(50, 100));
             canvas.setToolTipText("Nėra paveikslėlio");
         }
         container.add(canvas);
         container.setViewportView(canvas);
         container.setVerticalScrollBarPolicy(
                   JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
         container.setHorizontalScrollBarPolicy(
                   JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         container.repaint();
     }
     
     private void initialiseVisible() {
         setLayout(new BorderLayout());
         add(container, BorderLayout.CENTER);
         add(getStreamPanel(), BorderLayout.SOUTH);
     }
 }
