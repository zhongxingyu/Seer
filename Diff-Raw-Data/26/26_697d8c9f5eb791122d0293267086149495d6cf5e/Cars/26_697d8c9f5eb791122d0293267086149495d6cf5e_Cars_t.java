 package cars;
 
 import static java.lang.Double.parseDouble;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import javax.swing.AbstractListModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import som.net.SOMNet;
 
 public class Cars {
	public static final int COUNT = 250;
 	public static final int WIDTH = 100;
 	public static final int HEIGHT = 100;
 	private static final int FINALSIZE = WIDTH * HEIGHT;
 	public static final int ATTR_COUNT = 17;
 	private static final int WINNERS_CARS = 10;
 	private static final int WINNERS_NEURONS = FINALSIZE;
 
 	public CarNet carnet = new CarNet();
 
 	private JFrame frame;
 	private JTextField maxEpochTxt;
 	private JTextField minDTxt;
 	private JTextField maxDTxt;
 	private JTextField minNiTxt;
 	private JTextField maxNiTxt;
 	private JCheckBox chckbxGauss;
 	private JFileChooser chooser;
 	private JTextField textCena;
 	private JTextField textRok;
 	private JTextField textPrzebieg;
 	private JTextField textMoc;
 	private JTextField textPojemnosc;
 	private JTextField textPojemnosc2;
 	private JTextField textMoc2;
 	private JTextField textPrzebieg2;
 	private JTextField textRok2;
 	private JTextField textCena2;
 	private JTextField textSkrzynia2;
 	private JTextField textPaliwo2;
 	private JTextField textDrzwi2;
	private JComboBox<String> comboSkrzynia;
	private JComboBox<String> comboPaliwo;
	private JComboBox<String> comboDrzwi;
 	private JCheckBox checkKlimatyzacja;
 	private JCheckBox checkCentralnyZamek;
 	private JCheckBox checkAlufelgi;
 	private JCheckBox check4x4;
 	private JCheckBox checkElLusterka;
 	private JCheckBox checkElSzyby;
 	private JCheckBox checkKomputer;
 	private JCheckBox checkSzyberdach;
 	private JCheckBox checkAbs2;
 	private JCheckBox checkKlimatyzacja2;
 	private JCheckBox checkCentralnyZamek2;
 	private JCheckBox checkAlufelgi2;
 	private JCheckBox check4x42;
 	private JCheckBox checkElLusterka2;
 	private JCheckBox checkElSzyby2;
 	private JCheckBox checkKomputer2;
 	private JCheckBox checkSzyberdach2;
 	private JCheckBox checkAbs;
 	private JList<String> list;
 	
 	JButton szukajBtn;
 	JButton loadNetBtn;
 	JButton saveNetBtn;
 	JButton loadTrainsetBtn;
 	JButton resetWeightsBtn;
 	JButton teachNetBtn;
 
 	private int[] carsNo;
 	private int[] neuronsNo;
 	public BufferedImage image;
 	private JPanel netMap;
 
 	public static void main(String[] args) {
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
 			e.printStackTrace();
 		}
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Cars window = new Cars();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	public Cars() {
 		initialize();
 	}
 
 	private void initialize() {
 		carnet.net = new SOMNet(WIDTH, HEIGHT, ATTR_COUNT);
 		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
 
 		frame = new JFrame();
 		frame.setBounds(100, 100, 800, 660);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		JPanel panel = new JPanel();
 		frame.getContentPane().add(panel, BorderLayout.CENTER);
 		panel.setLayout(null);
 
 		netMap = new DisplayPanel();
 		netMap.setBounds(10, 11, 300, 300);
 		panel.add(netMap);
 
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.setBounds(320, 11, 462, 611);
 		panel.add(tabbedPane);
 
 		JPanel panel_1 = new JPanel();
 		tabbedPane.addTab("Wyszukiwanie samochod\u00F3w", null, panel_1, null);
 		panel_1.setLayout(null);
 
 		JPanel panel_5 = new JPanel();
 		panel_5.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Cechy", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel_5.setBounds(10, 11, 437, 190);
 		panel_1.add(panel_5);
 		panel_5.setLayout(null);
 
 		szukajBtn = new JButton("Szukaj");
 		
 		szukajBtn.setBounds(336, 156, 91, 23);
 		panel_5.add(szukajBtn);
 
 		JLabel lblCena = new JLabel("Cena:");
 		lblCena.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblCena.setBounds(10, 14, 70, 14);
 		panel_5.add(lblCena);
 
 		JLabel lblRokProdukcji = new JLabel("Rok produkcji:");
 		lblRokProdukcji.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblRokProdukcji.setBounds(10, 39, 70, 14);
 		panel_5.add(lblRokProdukcji);
 
 		JLabel lblPrzebieg = new JLabel("Przebieg:");
 		lblPrzebieg.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblPrzebieg.setBounds(10, 64, 70, 14);
 		panel_5.add(lblPrzebieg);
 
 		JLabel lblSkrzynia = new JLabel("Skrzynia:");
 		lblSkrzynia.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblSkrzynia.setBounds(10, 89, 70, 14);
 		panel_5.add(lblSkrzynia);
 
 		JLabel lblMoc = new JLabel("Moc:");
 		lblMoc.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblMoc.setBounds(10, 114, 70, 14);
 		panel_5.add(lblMoc);
 
 		JLabel lblPojemno = new JLabel("Pojemno\u015B\u0107:");
 		lblPojemno.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblPojemno.setBounds(10, 138, 70, 14);
 		panel_5.add(lblPojemno);
 
 		JLabel lblPaliwo = new JLabel("Paliwo:");
 		lblPaliwo.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblPaliwo.setBounds(150, 15, 70, 14);
 		panel_5.add(lblPaliwo);
 
 		JLabel lblDrzwi = new JLabel("Drzwi:");
 		lblDrzwi.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblDrzwi.setBounds(150, 40, 70, 14);
 		panel_5.add(lblDrzwi);
 
 		checkAbs = new JCheckBox("ABS");
 		checkAbs.setBounds(150, 61, 130, 20);
 		panel_5.add(checkAbs);
 
 		textCena = new JTextField();
 		textCena.setText("50000");
 		textCena.setBounds(90, 11, 50, 20);
 		panel_5.add(textCena);
 		textCena.setColumns(10);
 
 		textRok = new JTextField();
 		textRok.setText("2000");
 		textRok.setBounds(90, 36, 50, 20);
 		panel_5.add(textRok);
 		textRok.setColumns(10);
 
 		textPrzebieg = new JTextField();
 		textPrzebieg.setText("100000");
 		textPrzebieg.setBounds(90, 61, 50, 20);
 		panel_5.add(textPrzebieg);
 		textPrzebieg.setColumns(10);
 
		comboSkrzynia = new JComboBox<String>();
		comboSkrzynia.setModel(new DefaultComboBoxModel<String>(new String[] { "automatyczna", "manualna" }));
 		comboSkrzynia.setBounds(90, 85, 50, 22);
 		panel_5.add(comboSkrzynia);
 
 		textMoc = new JTextField();
 		textMoc.setText("100");
 		textMoc.setBounds(90, 111, 50, 20);
 		panel_5.add(textMoc);
 		textMoc.setColumns(10);
 
 		textPojemnosc = new JTextField();
 		textPojemnosc.setText("2000");
 		textPojemnosc.setColumns(10);
 		textPojemnosc.setBounds(90, 135, 50, 20);
 		panel_5.add(textPojemnosc);
 
 		comboPaliwo = new JComboBox<String>();
		comboPaliwo.setModel(new DefaultComboBoxModel<String>(new String[] { "diesel", "benzyna" }));
 		comboPaliwo.setBounds(230, 11, 50, 22);
 		panel_5.add(comboPaliwo);
 
 		comboDrzwi = new JComboBox<String>();
		comboDrzwi.setModel(new DefaultComboBoxModel<String>(new String[] { "4/5", "2/3" }));
 		comboDrzwi.setBounds(230, 36, 50, 22);
 		panel_5.add(comboDrzwi);
 
 		checkKlimatyzacja = new JCheckBox("klimatyzacja");
 		checkKlimatyzacja.setBounds(150, 89, 130, 19);
 		panel_5.add(checkKlimatyzacja);
 
 		checkCentralnyZamek = new JCheckBox("centralny zamek");
 		checkCentralnyZamek.setBounds(150, 110, 130, 19);
 		panel_5.add(checkCentralnyZamek);
 
 		checkAlufelgi = new JCheckBox("alufelgi");
 		checkAlufelgi.setBounds(150, 134, 130, 19);
 		panel_5.add(checkAlufelgi);
 
 		check4x4 = new JCheckBox("4x4");
 		check4x4.setBounds(290, 14, 129, 15);
 		panel_5.add(check4x4);
 
 		checkElLusterka = new JCheckBox("el. lusterka");
 		checkElLusterka.setBounds(290, 38, 130, 19);
 		panel_5.add(checkElLusterka);
 
 		checkElSzyby = new JCheckBox("el. szyby");
 		checkElSzyby.setBounds(290, 63, 130, 19);
 		panel_5.add(checkElSzyby);
 
 		checkKomputer = new JCheckBox("komputer");
 		checkKomputer.setBounds(290, 88, 130, 19);
 		panel_5.add(checkKomputer);
 
 		checkSzyberdach = new JCheckBox("szyberdach");
 		checkSzyberdach.setBounds(290, 113, 130, 19);
 		panel_5.add(checkSzyberdach);
 
 		JPanel panel_6 = new JPanel();
 		panel_6.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Lista znalezionych samochod\u00F3w", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel_6.setBounds(10, 212, 437, 160);
 		panel_1.add(panel_6);
 		panel_6.setLayout(null);
 
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setBounds(10, 17, 417, 132);
 		panel_6.add(scrollPane);
 
 		list = new JList<>();
 		list.setModel(new AbstractListModel<String>() {
 			private static final long serialVersionUID = 1L;
 			String[] values = new String[0];
 
 			public int getSize() {
 				return values.length;
 			}
 
 			public String getElementAt(int index) {
 				return values[index];
 			}
 		});
 		scrollPane.setViewportView(list);
 		
 
 		JPanel panel_7 = new JPanel();
 		panel_7.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "Cechy", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel_7.setBounds(10, 383, 437, 192);
 		panel_1.add(panel_7);
 		panel_7.setLayout(null);
 
 		JLabel label = new JLabel("Cena:");
 		label.setHorizontalAlignment(SwingConstants.RIGHT);
 		label.setBounds(10, 14, 70, 14);
 		panel_7.add(label);
 
 		JLabel label_1 = new JLabel("Rok produkcji:");
 		label_1.setHorizontalAlignment(SwingConstants.RIGHT);
 		label_1.setBounds(10, 39, 70, 14);
 		panel_7.add(label_1);
 
 		JLabel label_2 = new JLabel("Przebieg:");
 		label_2.setHorizontalAlignment(SwingConstants.RIGHT);
 		label_2.setBounds(10, 64, 70, 14);
 		panel_7.add(label_2);
 
 		JLabel label_3 = new JLabel("Skrzynia:");
 		label_3.setHorizontalAlignment(SwingConstants.RIGHT);
 		label_3.setBounds(10, 89, 70, 14);
 		panel_7.add(label_3);
 
 		JLabel label_4 = new JLabel("Moc:");
 		label_4.setHorizontalAlignment(SwingConstants.RIGHT);
 		label_4.setBounds(10, 114, 70, 14);
 		panel_7.add(label_4);
 
 		JLabel label_5 = new JLabel("Pojemno\u015B\u0107:");
 		label_5.setHorizontalAlignment(SwingConstants.RIGHT);
 		label_5.setBounds(10, 138, 70, 14);
 		panel_7.add(label_5);
 
 		textPojemnosc2 = new JTextField();
 		textPojemnosc2.setEditable(false);
 		textPojemnosc2.setColumns(10);
 		textPojemnosc2.setBounds(90, 135, 50, 20);
 		panel_7.add(textPojemnosc2);
 
 		textMoc2 = new JTextField();
 		textMoc2.setEditable(false);
 		textMoc2.setColumns(10);
 		textMoc2.setBounds(90, 111, 50, 20);
 		panel_7.add(textMoc2);
 
 		textPrzebieg2 = new JTextField();
 		textPrzebieg2.setEditable(false);
 		textPrzebieg2.setColumns(10);
 		textPrzebieg2.setBounds(90, 61, 50, 20);
 		panel_7.add(textPrzebieg2);
 
 		textRok2 = new JTextField();
 		textRok2.setEditable(false);
 		textRok2.setColumns(10);
 		textRok2.setBounds(90, 36, 50, 20);
 		panel_7.add(textRok2);
 
 		textCena2 = new JTextField();
 		textCena2.setEditable(false);
 		textCena2.setColumns(10);
 		textCena2.setBounds(90, 11, 50, 20);
 		panel_7.add(textCena2);
 
 		JLabel label_6 = new JLabel("Paliwo:");
 		label_6.setHorizontalAlignment(SwingConstants.RIGHT);
 		label_6.setBounds(150, 15, 70, 14);
 		panel_7.add(label_6);
 
 		JLabel label_7 = new JLabel("Drzwi:");
 		label_7.setHorizontalAlignment(SwingConstants.RIGHT);
 		label_7.setBounds(150, 40, 70, 14);
 		panel_7.add(label_7);
 
 		checkAbs2 = new JCheckBox("ABS");
 		checkAbs2.setEnabled(false);
 		checkAbs2.setBounds(150, 61, 130, 19);
 		panel_7.add(checkAbs2);
 
 		checkKlimatyzacja2 = new JCheckBox("klimatyzacja");
 		checkKlimatyzacja2.setEnabled(false);
 		checkKlimatyzacja2.setBounds(150, 89, 130, 19);
 		panel_7.add(checkKlimatyzacja2);
 
 		checkCentralnyZamek2 = new JCheckBox("centralny zamek");
 		checkCentralnyZamek2.setEnabled(false);
 		checkCentralnyZamek2.setBounds(150, 110, 130, 19);
 		panel_7.add(checkCentralnyZamek2);
 
 		checkAlufelgi2 = new JCheckBox("alufelgi");
 		checkAlufelgi2.setEnabled(false);
 		checkAlufelgi2.setBounds(150, 134, 130, 19);
 		panel_7.add(checkAlufelgi2);
 
 		check4x42 = new JCheckBox("4x4");
 		check4x42.setEnabled(false);
 		check4x42.setBounds(290, 14, 129, 19);
 		panel_7.add(check4x42);
 
 		checkElLusterka2 = new JCheckBox("el. lusterka");
 		checkElLusterka2.setEnabled(false);
 		checkElLusterka2.setBounds(290, 39, 129, 19);
 		panel_7.add(checkElLusterka2);
 
 		checkElSzyby2 = new JCheckBox("el. szyby");
 		checkElSzyby2.setEnabled(false);
 		checkElSzyby2.setBounds(290, 63, 129, 19);
 		panel_7.add(checkElSzyby2);
 
 		checkKomputer2 = new JCheckBox("komputer");
 		checkKomputer2.setEnabled(false);
 		checkKomputer2.setBounds(290, 88, 129, 19);
 		panel_7.add(checkKomputer2);
 
 		checkSzyberdach2 = new JCheckBox("szyberdach");
 		checkSzyberdach2.setEnabled(false);
 		checkSzyberdach2.setBounds(290, 113, 129, 19);
 		panel_7.add(checkSzyberdach2);
 
 		textSkrzynia2 = new JTextField();
 		textSkrzynia2.setEditable(false);
 		textSkrzynia2.setColumns(10);
 		textSkrzynia2.setBounds(90, 86, 50, 20);
 		panel_7.add(textSkrzynia2);
 
 		textPaliwo2 = new JTextField();
 		textPaliwo2.setEditable(false);
 		textPaliwo2.setColumns(10);
 		textPaliwo2.setBounds(230, 11, 50, 20);
 		panel_7.add(textPaliwo2);
 
 		textDrzwi2 = new JTextField();
 		textDrzwi2.setEditable(false);
 		textDrzwi2.setColumns(10);
 		textDrzwi2.setBounds(230, 36, 50, 20);
 		panel_7.add(textDrzwi2);
 
 		JPanel panel_2 = new JPanel();
 		tabbedPane.addTab("Uczenie sieci", null, panel_2, null);
 		panel_2.setLayout(null);
 
 		JPanel panel_3 = new JPanel();
 		panel_3.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel_3.setBounds(10, 11, 437, 37);
 		panel_2.add(panel_3);
 
 		loadNetBtn = new JButton("Wczytaj sie\u0107");
 		panel_3.add(loadNetBtn);
 
 		saveNetBtn = new JButton("Zapisz sie\u0107");
 		
 		panel_3.add(saveNetBtn);
 
 		JPanel panel_4 = new JPanel();
 		panel_4.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel_4.setBounds(10, 62, 437, 202);
 		panel_2.add(panel_4);
 		panel_4.setLayout(null);
 
 		loadTrainsetBtn = new JButton("Wczytaj zbi\u00F3r ucz\u0105cy");
 		
 		loadTrainsetBtn.setBounds(10, 11, 140, 23);
 		panel_4.add(loadTrainsetBtn);
 
 		resetWeightsBtn = new JButton("Zresetuj wagi");
 		
 		resetWeightsBtn.setBounds(10, 45, 140, 23);
 		panel_4.add(resetWeightsBtn);
 
 		teachNetBtn = new JButton("Ucz sie\u0107");
 		
 		teachNetBtn.setBounds(10, 79, 140, 23);
 		panel_4.add(teachNetBtn);
 
 		JLabel lblIteracji = new JLabel("Iteracji:");
 		lblIteracji.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblIteracji.setBounds(231, 15, 100, 14);
 		panel_4.add(lblIteracji);
 
 		maxEpochTxt = new JTextField();
 		maxEpochTxt.setText("100");
 		maxEpochTxt.setBounds(341, 12, 86, 20);
 		panel_4.add(maxEpochTxt);
 		maxEpochTxt.setColumns(10);
 
 		JLabel lblMinD = new JLabel("Min d:");
 		lblMinD.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblMinD.setBounds(231, 49, 100, 14);
 		panel_4.add(lblMinD);
 
 		minDTxt = new JTextField();
 		minDTxt.setText("1");
 		minDTxt.setBounds(341, 45, 86, 20);
 		panel_4.add(minDTxt);
 		minDTxt.setColumns(10);
 
 		JLabel lblMaxD = new JLabel("Max d:");
 		lblMaxD.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblMaxD.setBounds(231, 83, 100, 14);
 		panel_4.add(lblMaxD);
 
 		maxDTxt = new JTextField();
 		maxDTxt.setText("10");
 		maxDTxt.setBounds(341, 80, 86, 20);
 		panel_4.add(maxDTxt);
 		maxDTxt.setColumns(10);
 
 		JLabel lblMinNi = new JLabel("Min ni:");
 		lblMinNi.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblMinNi.setBounds(231, 114, 100, 14);
 		panel_4.add(lblMinNi);
 
 		minNiTxt = new JTextField();
 		minNiTxt.setText("0.1");
 		minNiTxt.setBounds(341, 111, 86, 20);
 		panel_4.add(minNiTxt);
 		minNiTxt.setColumns(10);
 
 		JLabel lblMaxNi = new JLabel("Max ni:");
 		lblMaxNi.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblMaxNi.setBounds(231, 145, 100, 14);
 		panel_4.add(lblMaxNi);
 
 		maxNiTxt = new JTextField();
 		maxNiTxt.setText("1.0");
 		maxNiTxt.setBounds(341, 142, 86, 20);
 		panel_4.add(maxNiTxt);
 		maxNiTxt.setColumns(10);
 
 		chckbxGauss = new JCheckBox("Gauss");
 		chckbxGauss.setBounds(341, 169, 86, 23);
 		panel_4.add(chckbxGauss);
 
 		chooser = new JFileChooser();
 		
 		registerListeners();
 		tabbedPane.setSelectedIndex(1);
 	}
 	
 	
 	private void registerListeners(){
 		
 		szukajBtn.addActionListener(new ActionListener() {
 
 			public void actionPerformed(ActionEvent e) {
 				double cena = parseDouble(textCena.getText());
 				double rok = parseDouble(textRok.getText());
 				double przebieg = parseDouble(textPrzebieg.getText());
 				double skrzynia = comboSkrzynia.getSelectedIndex() == 0 ? 1 : 0;
 				double moc = parseDouble(textMoc.getText());
 				double pojemnosc = parseDouble(textPojemnosc.getText());
 				double paliwo = comboPaliwo.getSelectedIndex() == 0 ? 1 : 0;
 				double drzwi = comboDrzwi.getSelectedIndex() == 0 ? 1 : 0;
 				double abs = checkAbs.isSelected() ? 1 : 0;
 				double klimatyzacja = checkKlimatyzacja.isSelected() ? 1 : 0;
 				double centralny = checkCentralnyZamek.isSelected() ? 1 : 0;
 				double alufelgi = checkAlufelgi.isSelected() ? 1 : 0;
 				double x4x4 = check4x4.isSelected() ? 1 : 0;
 				double lusterka = checkElLusterka.isSelected() ? 1 : 0;
 				double szyby = checkElSzyby.isSelected() ? 1 : 0;
 				double komputer = checkKomputer.isSelected() ? 1 : 0;
 				double szyberdach = checkSzyberdach.isSelected() ? 1 : 0;
 
 				double[] input = new double[ATTR_COUNT];
 				input[0] = cena / 150000.;
 				input[1] = (rok - 1990.) / 22.;
 				input[2] = przebieg / 250000.;
 				input[3] = skrzynia;
 				input[4] = (moc - 50.) / 150.;
 				input[5] = (pojemnosc - 500.) / 4500.;
 				input[6] = paliwo;
 				input[7] = drzwi;
 				input[8] = abs;
 				input[9] = klimatyzacja;
 				input[10] = centralny;
 				input[11] = alufelgi;
 				input[12] = x4x4;
 				input[13] = lusterka;
 				input[14] = szyby;
 				input[15] = komputer;
 				input[16] = szyberdach;
 
 				carnet.net.setInput(input);
 				carnet.net.calculateDistancesToInput();
 
 				findWinners();
 			}
 
 		});
 		
 		list.addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				int selectedIndex = list.getSelectedIndex();
 				if (selectedIndex >= 0) {
 					updateCarInfo(selectedIndex);
 					fillNetMap();
 					int neuronNo = neuronsNo[selectedIndex];
 					image.setRGB(neuronNo % WIDTH, neuronNo / WIDTH, 0xFF0000);
 					netMap.repaint();
 				}
 			}
 
 		});
 		
 		
 		loadNetBtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int val = chooser.showOpenDialog(frame);
 
 				if (val == JFileChooser.APPROVE_OPTION) {
 					carnet = load(chooser.getSelectedFile().getPath());
 				}
 			}
 
 			public CarNet load(String filename) {
 				try {
 					FileInputStream fis = new FileInputStream(filename);
 					GZIPInputStream gzis = new GZIPInputStream(fis);
 					ObjectInputStream in = new ObjectInputStream(gzis);
 					CarNet net = (CarNet) in.readObject();
 					in.close();
 					return net;
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				return null;
 			}
 		});
 		
 		saveNetBtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				int val = chooser.showSaveDialog(frame);
 
 				if (val == JFileChooser.APPROVE_OPTION) {
 					save(chooser.getSelectedFile().getPath(), carnet);
 				}
 			}
 
 			public void save(String filename, CarNet net) {
 				try {
 					FileOutputStream fos = new FileOutputStream(filename);
 					GZIPOutputStream gzos = new GZIPOutputStream(fos);
 					ObjectOutputStream out = new ObjectOutputStream(gzos);
 					out.writeObject(net);
 					out.flush();
 					out.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		
 		loadTrainsetBtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				DbReader.Db db = DbReader.read("cars.txt", COUNT);
 				carnet.carDb = db.db;
 				carnet.attrDb = db.attrDb;
 				carnet.winnerCarForNeuron = new int[FINALSIZE];
 			}
 		});
 		
 		resetWeightsBtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
                carnet.net.randomizeWeights();
 			}
 		});
 		
 		teachNetBtn.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				teachingProcedure();
 			}
 		});
 	}
 
 	private void updateCarInfo(int index) {
 		double[] attr = carnet.carDb[carsNo[index]].attr;
 
 		textCena2.setText(Double.toString(attr[0] * 150000));
 		textRok2.setText(Double.toString(attr[1] * 22 + 1990));
 		textPrzebieg2.setText(Double.toString(attr[2] * 250000));
 		textSkrzynia2.setText(attr[3] == 1. ? "automatyczna" : "manualna");
 		textMoc2.setText(Double.toString(attr[4] * 150 + 50));
 		textPojemnosc2.setText(Double.toString(attr[5] * 4500 + 500));
 		textPaliwo2.setText(attr[6] == 1. ? "diesel" : "benzyna");
 		textDrzwi2.setText(attr[7] == 1. ? "4/5" : "2/3");
 		checkAbs2.setSelected(attr[8] == 1.);
 		checkKlimatyzacja2.setSelected(attr[9] == 1.);
 		checkCentralnyZamek2.setSelected(attr[10] == 1.);
 		checkAlufelgi2.setSelected(attr[11] == 1.);
 		check4x42.setSelected(attr[12] == 1.);
 		checkElLusterka2.setSelected(attr[13] == 1.);
 		checkElSzyby2.setSelected(attr[14] == 1.);
 		checkKomputer2.setSelected(attr[15] == 1.);
 		checkSzyberdach2.setSelected(attr[16] == 1.);
 
 	}
 
 	private void updateNetMap() {
 		fillNetMap();
 		netMap.repaint();
 	}
 
 	public void fillNetMap() {
 		int winner = carnet.net.winner();
 		int looser = carnet.net.looser();
 		float minDistance = (float) carnet.net.distances[winner];
 		float maxDistance = (float) carnet.net.distances[looser] - minDistance;
 
 		for (int i = 0; i < HEIGHT; i++) {
 			for (int j = 0; j < WIDTH; j++) {
 				float distance = (float) carnet.net.distances[i * HEIGHT + j] - minDistance;
 				Color color = new Color(distance / maxDistance, distance / maxDistance, distance / maxDistance, 0f);
 				image.setRGB(j, i, color.getRGB());
 			}
 		}
 	}
 	
 	private void findWinners(){
 		int[] winners = carnet.net.winners(WINNERS_NEURONS);
 
 		carsNo = new int[WINNERS_CARS];
 		neuronsNo = new int[WINNERS_CARS];
 		int found = 0;
 		boolean notThere;
 		
 		for(int i = 0; i < WINNERS_NEURONS && found < WINNERS_CARS; ++i){
 			notThere = true;
 			for(int j = 0; j < found; ++j){
 				if(carsNo[j] == carnet.winnerCarForNeuron[winners[i]]){
 					notThere = false;
 					break;
 				}
 			}
 			if(notThere){
 				neuronsNo[found] = winners[i];
 				carsNo[found] = carnet.winnerCarForNeuron[winners[i]];
 				++found;
 			}
 		}
 		
 		list.setModel(new AbstractListModel<String>() {
 			private static final long serialVersionUID = 1L;
 
 			public int getSize() {
 				return carsNo.length;
 			}
 
 			public String getElementAt(int index) {
 				return carnet.carDb[carsNo[index]].make + " " + carnet.carDb[carsNo[index]].model;
 			}
 		});
 
 		updateNetMap();
 	}
 	
 	private void teachingProcedure(){
 		carnet.net.teach(carnet.attrDb, Double.parseDouble(minNiTxt.getText()), Double.parseDouble(maxNiTxt.getText()), Double.parseDouble(minDTxt.getText()),
 				Double.parseDouble(maxDTxt.getText()), Integer.parseInt(maxEpochTxt.getText()), chckbxGauss.isSelected());
 
 		double[] temp = new double[FINALSIZE];
 		for(int j = 0; j < FINALSIZE; ++j){
 			temp[j] = 99999;
 		}
 		
 		for (int i = 0; i < COUNT; ++i) {
 			carnet.net.setInput(carnet.carDb[i].attr);
 			carnet.net.calculateDistancesToInput();
 			for(int j = 0; j < FINALSIZE; ++j){
 				if(temp[j] > carnet.net.distances[j]){
 					temp[j] = carnet.net.distances[j];
 					carnet.winnerCarForNeuron[j] = i;
 				}
 			}
 		}
 	}
 
 	class DisplayPanel extends JPanel {
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		public void paint(Graphics g) {
 			super.paint(g);
 			g.drawImage(image, 0, 0, 300, 300, null);
 			repaint();
 		}
 	}
 }
