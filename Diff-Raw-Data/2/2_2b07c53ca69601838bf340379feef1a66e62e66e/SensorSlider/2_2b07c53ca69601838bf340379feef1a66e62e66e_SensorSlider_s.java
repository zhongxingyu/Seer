 package periphgeraete.sensor;
 
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.*;
 import java.io.IOException;
 import java.io.File;
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import werkzeug.BildImplementierung;
 
 
 /**
  * Die Klasse SensorSlider erstellt einen JFrame mit einem JSlider,
  * der zum Einstellen der Temperatur genutzt wird.
  *
  * @author Niklas Knauer
  */
 public class SensorSlider extends JFrame implements MouseListener {
 
   private int minimum;
   private int maximum;
 
   private ImageIcon icon;
 
   private JFrame f;
   private JPanel sensorPanel = new JPanel();
   private JLabel sensorLabel = new JLabel();
   private JLabel min_Label = new JLabel("min");
   private JLabel max_Label = new JLabel("max");
   private JLabel aktTemp_Label = new JLabel("aktuelle Temperatur");
   private JSlider sensorSlider = new JSlider();
   private JTextField aktuelleTemp_Tfield = new JTextField();
   private JTextField minTemp_Tfield = new JTextField();
   private JTextField maxTemp_Tfield = new JTextField();
   private ChangeListener listener;
 
   BildImplementierung bildkonf = new BildImplementierung();
   Sensor senor = new Sensor();
 
   /**
    * Der Standardkonstruktor der Klasse SensorJSlider,
    * bietet die Moeglichkeit den Wertebereich des JSliders
    * in der GUI ein zu stellen.
    */
   public SensorSlider(String name) {
     f = new JFrame(name);
 
     try {
       icon = bildkonf.getImageIcon("Sensor");
     } catch(IOException ex) {
       ex.printStackTrace();
     }
 
   	sensorPanel.setLayout(null);
   	sensorPanel.setSize(250, 200);
   	sensorPanel.setBackground(Color.GRAY);
   	aktuelleTemp_Tfield.setBounds(100, 115, 25, 20);
 
   	minTemp_Tfield.setBounds(100, 80, 30, 25);
   	minTemp_Tfield.setBackground(Color.green);
   	minTemp_Tfield.addMouseListener(this);
   	maxTemp_Tfield.setBounds(140, 80, 30, 25);
   	maxTemp_Tfield.setBackground(Color.RED);
   	maxTemp_Tfield.addMouseListener(this);
 
     listener = new ChangeListener() {
       @Override
       public void stateChanged(ChangeEvent event) {
         // Textfeld aktualisieren, wenn sich Schieberegler aendert
 
     		JSlider source = (JSlider) event.getSource();
     		aktuelleTemp_Tfield.setText(String.valueOf(source.getValue()));
     		minimum = Integer.parseInt(minTemp_Tfield.getText());
     		maximum = Integer.parseInt(maxTemp_Tfield.getText());
 
         sensorSlider.setMinimum(minimum);
         sensorSlider.setMaximum(maximum);
 
     		senor.setTemperatur(source.getValue());
     		senor.printTemp();
       }
     };
 
     // Teilstriche werden angezeigt
     sensorSlider.setPaintTicks(true);
 
     // setzt Teilstriche bei Vielfachen der Einheit
   	sensorSlider.setMajorTickSpacing(20);
   	sensorSlider.setMinorTickSpacing(5);
 
   	sensorSlider.setBounds(0, 101, 100, 50);
   	sensorSlider.addChangeListener(listener);
 
   	bildkonf.skalieren(icon);
     sensorLabel.setIcon(icon);
   	sensorLabel.setBounds(0, 0, 100, 100);
 
   	min_Label.setBounds(100, 60, 70, 20);
   	max_Label.setBounds(140,60,70,20);
   	aktTemp_Label.setBounds(130, 115, 120, 20);
 
   	sensorPanel.add(minTemp_Tfield);
   	sensorPanel.add(maxTemp_Tfield);
   	sensorPanel.add(aktuelleTemp_Tfield);
   	sensorPanel.add(sensorLabel);
   	sensorPanel.add(min_Label);
   	sensorPanel.add(max_Label);
   	sensorPanel.add(aktTemp_Label);
   	sensorPanel.add(sensorSlider);
  	sensorFrame.add(sensorPanel);
  	sensorFrame.setVisible(true);
 
     // Get screen dimensions
     Toolkit tk = Toolkit.getDefaultToolkit();
     Dimension screenSize = tk.getScreenSize();
     int screenHeight = screenSize.height;
     int screenWidth = screenSize.width;
 
     f.add(sensorPanel);
     f.setSize(265, 200);
 
     Dimension dim = f.getSize();
     f.setLocation(0, screenHeight/2 - dim.height);
 
     f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     f.setResizable(false);
     f.setVisible(true);
   }
 
   /**
    * Der Konstruktor mit Uebergabeparameter bietet die Moeglichkeit an
    * den Wertebereich ueber die Parameter ein zu stellen.
    *
    * @param minimumSkala
    * @param maximumSkala
    */
   public SensorSlider(int minimumSkala, int maximumSkala) {
     minimum = minimumSkala;
     maximum = maximumSkala;
 
     try {
       icon = bildkonf.getImageIcon("Sensor");
     } catch(IOException ex) {
       ex.printStackTrace();
     }
 
 
 
     sensorPanel.setLayout(null);
     sensorPanel.setSize(200, 200);
     sensorPanel.setBackground(Color.GRAY);
     aktuelleTemp_Tfield.setBounds(100, 115, 25, 20);
 
     listener = new ChangeListener() {
 	    @Override
 	    public void stateChanged(ChangeEvent e) {
     		// Textfeld aktualisieren, wenn sich Schieberegler aendert
     		JSlider source = (JSlider) e.getSource();
     		aktuelleTemp_Tfield.setText(String.valueOf(source.getValue()));
 
     		senor.setTemperatur(source.getValue());
     		senor.printTemp();
 
     		sensorSlider.setMinimum(minimum);
     		sensorSlider.setMaximum(maximum);
 	    }
     };
 
     // Teilstriche werden angezeigt
     sensorSlider.setPaintTicks(true);
 
     // setzt Teilstriche bei Vielfachen der Einheit
     sensorSlider.setMajorTickSpacing(20);
     sensorSlider.setMinorTickSpacing(5);
 
     sensorSlider.setBounds(0, 101, 100, 50);
 
     sensorSlider.addChangeListener(listener);
 
     bildkonf.skalieren(icon);
     sensorLabel.setIcon(icon);
     sensorLabel.setBounds(0, 0, 100, 100);
 
   	sensorPanel.add(aktuelleTemp_Tfield);
   	sensorPanel.add(sensorLabel);
   	sensorPanel.add(sensorSlider);
 
     f.add(sensorPanel);
     f.setSize(200, 200);
 
     f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     f.setResizable(false);
     f.setVisible(true);
   }
 
   /**
    * Bietet die Moeglichkeit das private Atribut der Klasse SensorRechnung
    * aus zu lesen.
    *
    * @return  die aktuelle Temperatur
    */
   public int getAktuellenWert() {
     return senor.getTemp();
   }
 
   @Override
   public void mouseClicked(MouseEvent e) {
     if (minTemp_Tfield.getText().equals("min"))
       minTemp_Tfield.setText("");
 
     if (maxTemp_Tfield.getText().equals("max"))
       maxTemp_Tfield.setText("");
   }
 
   @Override
   public void mouseEntered(MouseEvent e) {}
 
   @Override
   public void mouseExited(MouseEvent e) {}
 
   @Override
   public void mousePressed(MouseEvent e) {}
 
   @Override
   public void mouseReleased(MouseEvent e) {}
 }
