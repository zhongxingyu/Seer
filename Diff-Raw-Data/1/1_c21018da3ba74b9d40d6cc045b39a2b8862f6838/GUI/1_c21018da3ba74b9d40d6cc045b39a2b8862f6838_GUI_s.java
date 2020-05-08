 package ru.bmstu.diplom;
 
 import static com.googlecode.javacv.cpp.opencv_highgui.cvLoadImage;
 import static com.googlecode.javacv.cpp.opencv_highgui.cvSaveImage;
 import static javax.swing.JOptionPane.ERROR_MESSAGE;
 import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
 import static javax.swing.JOptionPane.showMessageDialog;
 
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.HeadlessException;
 import java.awt.event.ActionEvent;
 import java.io.File;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.SwingUtilities;
 import javax.swing.WindowConstants;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import com.googlecode.javacv.cpp.opencv_core.IplImage;
 
 /**
  * Класс, реализующий GUI (пользовательский интерфейс) для работы с 
  * изображениями.
  * 
  * @author Victor Butenko
  */
 public final class GUI extends JFrame   {
 	
 	//----------------------------ПОЛЯ---------------------------------------------
 	
 	/** Для корректной работы сериализации */
 	private static final long serialVersionUID = 1L;
 
 	/**Компонент для выбора файла изображения (Открытие из текущей директории проекта/imgs) */
 	private final JFileChooser fileChooser = new JFileChooser(new File(".//imgs"));
 	
 	/** Компонент для отображения изображений*/
     private final JLabel imageView = new JLabel();
 
     /**Переменная, содержащая загруженное изображение*/
     private IplImage image = null;
     private IplImage original = null;
     
     /** Area coordinates*/
     private int x1, y1, x2, y2;
     /** The error of the RGB average*/
     private int error;
     
     /** Height and Weight of the image */
     private int heightIm = 600, widthIm = 800;
     
     /** Spinners for allocating the area */
     private JSpinner spinnerX1,  spinnerY1,  spinnerX2,  spinnerY2;
     /** Spinner for error choosing*/
     private JSpinner errorSpinner; 
     
   //----------------------------ПОЛЯ---------------------------------------------
 	
     
     // Конструктор
     private GUI() throws HeadlessException {
         super("The application for image processing"); 
         
     	
         //Action perfomed when "Reset" button is pressed 
         final Action resetAction = new AbstractAction("Reset") {			
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				try {
 					if (image != null) {
 						image = original.clone();
 						imageView.setIcon(new ImageIcon(image.getBufferedImage()));
 					} else {
 						showMessageDialog(GUI.this, "Image not opened",
                         		getTitle(), ERROR_MESSAGE);
 					}
 				} finally {
 					setCursor(Cursor.getDefaultCursor());
 				}
 			}
 		};
 		
 		
         // Action perfomed when "Save" button is pressed
         final Action saveAction = new AbstractAction("Save") {
 			private static final long serialVersionUID = 1L;
 
 			@Override
 			public void actionPerformed(final ActionEvent e) {
 				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 				try {
 					if (image != null) {
 						final String newImgName = 
 								fileChooser.getSelectedFile().getAbsolutePath() + "_snap.jpg";
 						cvSaveImage(newImgName, image);
 						showMessageDialog(GUI.this, 
 								"Image " + newImgName + " was successully saved.", getTitle(), 
 								INFORMATION_MESSAGE);
 					} else {
 						showMessageDialog(GUI.this, "Image not opened",
                         		getTitle(), ERROR_MESSAGE);
 					}
 				} finally {
 					setCursor(Cursor.getDefaultCursor());
 				}
 				
 			}
 		};
         
 		 //  Action performed when "Process" button is pressed
         final Action allocateAction = new AbstractAction("Allocate") {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                 try {
                     // Обработать и обновить экран, если изображение загружено
                 	if (image != null) {
                         allocateImage(image);
                         imageView.setIcon(new ImageIcon(image.getBufferedImage()));
                     } else {
                         showMessageDialog(GUI.this, "Image not opened",
                         		getTitle(), ERROR_MESSAGE);
                     }
                 } finally {
                     setCursor(Cursor.getDefaultCursor());
                 }
             }
         };
         allocateAction.setEnabled(false);
         
         
         //  Action performed when "Process" button is pressed
         final Action processAction = new AbstractAction("Process") {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                 try {
                     // Обработать и обновить экран, если изображение загружено
                 	if (image != null) {
                         processImage(image);
                         imageView.setIcon(new ImageIcon(image.getBufferedImage()));
                     } else {
                         showMessageDialog(GUI.this, "Image not opened",
                         		getTitle(), ERROR_MESSAGE);
                     }
                 } finally {
                     setCursor(Cursor.getDefaultCursor());
                 }
             }
         };
         processAction.setEnabled(false);
 
         // Action performed when "Open Image" button is pressed
         final Action openImageAction = new AbstractAction("Open Image") {
             private static final long serialVersionUID = 1L;
 
             @Override
             public void actionPerformed(final ActionEvent e) {
                 setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                 try {
                     // Load image and update display. If new image was not loaded do nothing.
                     final IplImage img = openImage();
 
                     if (img != null) {
                         image = img;
                         original = image.clone();
                         imageView.setIcon(new ImageIcon(image.getBufferedImage()));
                         allocateAction.setEnabled(true);
                         processAction.setEnabled(true);
                         
                         //Define the size of the image
                         heightIm = image.height();
                         widthIm  = image.width();
 
                     }
                 } finally {
                     setCursor(Cursor.getDefaultCursor());
                 }
             }
         };
 
 
         //
         // Create UI
         //
 
         // Create button panel
         final JPanel buttonsPanel = new JPanel(new GridLayout(0, 1, 0, 5));
         buttonsPanel.add(new JButton(openImageAction));
         buttonsPanel.add(new JButton(allocateAction));
         buttonsPanel.add(new JButton(processAction));
         buttonsPanel.add(new JButton(resetAction));
         buttonsPanel.add(new JButton(saveAction));
         
         
         
         //Create Spinner panel FIXME: Избавиться от дублирования ??!
         ChangeListener listenerX1 = new ChangeListener() {
         	public void stateChanged(ChangeEvent e) {
                 JSpinner js = (JSpinner) e.getSource();
                 x1 = (Integer) js.getValue();
             }
         };
         ChangeListener listenerY1 = new ChangeListener() {
         	public void stateChanged(ChangeEvent e) {
                 JSpinner js = (JSpinner) e.getSource();
                 y1 = (Integer) js.getValue();
             }
         };
         ChangeListener listenerX2 = new ChangeListener() {
         	public void stateChanged(ChangeEvent e) {
                 JSpinner js = (JSpinner) e.getSource();
                 x2 = (Integer) js.getValue();
             }
         };
         ChangeListener listenerY2 = new ChangeListener() {
         	public void stateChanged(ChangeEvent e) {
                 JSpinner js = (JSpinner) e.getSource();
                 y2 = (Integer) js.getValue();
             }
         };
         ChangeListener listenerError = new ChangeListener() {
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				JSpinner js = (JSpinner) e.getSource();
 				error = (Integer) js.getValue();
 			}
 		};
         
     
         // Объявление модели JSpinner'а
         SpinnerModel modelX1 = new SpinnerNumberModel(206, 0, widthIm, 1);
         SpinnerModel modelY1 = new SpinnerNumberModel(135, 0, heightIm, 1);
         SpinnerModel modelX2 = new SpinnerNumberModel(258, 0, widthIm, 1);
         SpinnerModel modelY2 = new SpinnerNumberModel(173, 0, heightIm, 1);
         SpinnerModel modError= new SpinnerNumberModel(25, 0, 255, 1);
         //Объявление JSpinner'а, которого будем слушать
         spinnerX1    = new JSpinner(modelX1);
         spinnerY1    = new JSpinner(modelY1);
         spinnerX2    = new JSpinner(modelX2);
         spinnerY2    = new JSpinner(modelY2);
         errorSpinner = new JSpinner(modError);
         
         spinnerX1.addChangeListener(listenerX1);
         spinnerY1.addChangeListener(listenerY1);
         spinnerX2.addChangeListener(listenerX2);
         spinnerY2.addChangeListener(listenerY2);
         errorSpinner.addChangeListener(listenerError);
         
         JLabel x1Label = new JLabel("x1");
         JLabel y1Label = new JLabel("y1");
         JLabel x2Label = new JLabel("x2");
         JLabel y2Label = new JLabel("y2");
         JLabel erLabel = new JLabel("error");
        
         // Layout frame contents
 
         // Action buttons on the left
         final JPanel leftPane = new JPanel();
         leftPane.add(buttonsPanel);
         add(leftPane, BorderLayout.WEST);
 
       //Action spinners on the left
         final JPanel spinnerPane = new JPanel();
         spinnerPane.add(x1Label);
         spinnerPane.add(spinnerX1);
         spinnerPane.add(y1Label);
         spinnerPane.add(spinnerY1);
         spinnerPane.add(x2Label);
         spinnerPane.add(spinnerX2);
         spinnerPane.add(y2Label);
         spinnerPane.add(spinnerY2);
         spinnerPane.add(erLabel);
         spinnerPane.add(errorSpinner);
         add(spinnerPane, BorderLayout.BEFORE_FIRST_LINE);
         
         
         // Image display in the center
         final JScrollPane imageScrollPane = new JScrollPane(imageView);
         imageScrollPane.setPreferredSize(new Dimension(640, 480));
         add(imageScrollPane, BorderLayout.CENTER);
     
     }
 	
     
     /**
      * Ask user for location and open new image.
      *
      * @return Opened image or {@code null} if image was not loaded.
      */
     private IplImage openImage() {
 
         // Ask user for the location of the image file
         if (fileChooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
             return null;
         }
 
         // Load the image
         final String path = fileChooser.getSelectedFile().getAbsolutePath();
         final IplImage newImage = cvLoadImage(path);
         if (newImage != null) {
             return newImage;
         } else {
             showMessageDialog(this, "Cannot open image file: " + path, 
             		getTitle(), ERROR_MESSAGE);
             return null;
         }
     }
 
 
     /**
      * Process image in place
      * int x1 = 206, y1 = 135, x2 = 258, y2 = 173; //Для img1 !!!!
      * int x1 = 328, y1 = 594, x2 = 734, y2 = 795; //Для forest.jpg !!!!
      * int x1 = 158, y1 = 223, x2 = 175, y2 = 268; //Для river !!!!
      * int x1 = 374, y1 = 214, x2 = 405, y2 = 286; //Для river2 !!!!
      * 
      * @param src image to process.
      */
     private void processImage(final IplImage src) {
        ImageProcessing imgProc = new ImageProcessing(src);
        x1 = (Integer) spinnerX1.getValue();
    	   y1 = (Integer) spinnerY1.getValue();
    	   x2 = (Integer) spinnerX2.getValue();
        y2 = (Integer) spinnerY2.getValue();
        imgProc.findArea(x1, y1, x2, y2, error);
         
     }
     /**Method for allocating the area */
     private void allocateImage (final IplImage src ) {
     	ImageProcessing imgProc = new ImageProcessing(src);
     	x1 = (Integer) spinnerX1.getValue();
     	y1 = (Integer) spinnerY1.getValue();
     	x2 = (Integer) spinnerX2.getValue();
     	y2 = (Integer) spinnerY2.getValue();
     	imgProc.allocatePart(x1, y1, x2, y2);
     }
 
 
     public static void main(final String[] args) {
         SwingUtilities.invokeLater(new Runnable() {
             public void run() {
                 final GUI frame = new GUI();
                 frame.pack();
                 // Mark for display in the center of the screen
                 frame.setLocationRelativeTo(null);
                 // Exit application when frame is closed.
                 frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                 frame.setVisible(true);
             }
         });
     }
 
     
 }
