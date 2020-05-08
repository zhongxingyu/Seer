 package movieTheater.GUI;
 
 import java.awt.BorderLayout;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.concurrent.CountDownLatch;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.text.MaskFormatter;
 
 import movieTheater.Movie.Movie;
 import movieTheater.Show.Show;
 import movieTheater.main.MovieController;
 
 @SuppressWarnings("serial")
 public class EditShow extends JFrame{
 	private JPanel contentPane;
 	private JFormattedTextField startDate;
 	private JFormattedTextField startTime;
 	private SimpleDateFormat dateFormat;
 	private DateFormat timeFormat;
 	private JComboBox comboBoxPris;
 	private JComboBox comboBoxSal;
 	private JComboBox comboBoxFilm;
 	private boolean dataOk;
 	private boolean areChangesMade;
 	private MaskFormatter showDate;
 	private MaskFormatter showStart;
 	private Date date;
 	private Show show;
 	private int hall;
 	private long time;
 	public CountDownLatch latch = new CountDownLatch(1); //venter p brugerens input.
 	
 	
 	public EditShow(final Show show) {
 		setTitle("Redig\u00E9r forestilling");
 		this.show = show;
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		areChangesMade = false;
 		dataOk = false;
 		
 		try
 		{
 			showStart = new MaskFormatter("##:##");
 			showDate = new MaskFormatter("##-##-####");
 		}
 		catch (ParseException e1)
 		{
 			e1.printStackTrace();
 		}
 		dateFormat = new SimpleDateFormat("dd-MM-yyyy");
 		timeFormat = new SimpleDateFormat("hh:mm");
 		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setBounds(100, 100, 326, 386);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(new BorderLayout(0, 0));
 		setContentPane(contentPane);
 		
 		JPanel panel = new JPanel();
 		contentPane.add(panel, BorderLayout.CENTER);
 		panel.setLayout(null);
 		
 		comboBoxFilm = new JComboBox(MovieController.movies.toArray());
 		comboBoxFilm.setBounds(107, 64, 162, 20);
 		comboBoxFilm.setSelectedItem(show.getMovie());//FIXME!!!
 		panel.add(comboBoxFilm);
 		
 
 		JLabel lblVlgFillm = new JLabel("V\u00E6lg film");
 		lblVlgFillm.setBounds(10, 67, 72, 14);
 		panel.add(lblVlgFillm);
 		
 		JLabel lblStartTid = new JLabel("Dato");
 		lblStartTid.setBounds(10, 100, 46, 14);
 		panel.add(lblStartTid);
 		
 		
 		comboBoxPris = new JComboBox();
 		comboBoxPris.setModel(new DefaultComboBoxModel(new String[] {"1", "1.5", "2"}));
 		comboBoxPris.setToolTipText("1 - Normal, 1.5 Medium, 2 - Peak");
 		comboBoxPris.setMaximumRowCount(3);
 		comboBoxPris.setBounds(197, 185, 72, 20);
 		comboBoxPris.setSelectedItem(show.getPriceCategory());//Skal mske typecastes
 		panel.add(comboBoxPris);
 		
 		JLabel lblVlgPris = new JLabel("V\u00E6lg pris");
 		lblVlgPris.setBounds(10, 183, 72, 25);
 		panel.add(lblVlgPris);
 		
 		JButton btnAnnuller = new JButton("Annuller");
 		btnAnnuller.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				latch.countDown();
				EditShow.this.dispose();
 				dataOk = true;
 			}
 		});
 		btnAnnuller.setBounds(55, 255, 89, 23);
 		panel.add(btnAnnuller);
 		
 		JLabel lblAendreForestilling = new JLabel("Rediger Forestilling");
 		lblAendreForestilling.setFont(new Font("Tahoma", Font.BOLD, 13));
 		lblAendreForestilling.setBounds(10, 11, 134, 14);
 		panel.add(lblAendreForestilling);
 		
 		startDate = new JFormattedTextField(showDate);
 		startDate.setBounds(197, 97, 72, 20);
 		panel.add(startDate);
 		
 		JLabel lblVlgSal = new JLabel("V\u00E6lg sal");
 		lblVlgSal.setBounds(10, 157, 72, 25);
 		panel.add(lblVlgSal);
 		
 		comboBoxSal = new JComboBox();
 		comboBoxSal.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3"}));
 		comboBoxSal.setBounds(197, 159, 72, 20);
 		panel.add(comboBoxSal);
 		
 		JButton btnGem = new JButton("Gem");
 		btnGem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				Movie movie		= (Movie) comboBoxFilm.getSelectedItem();
 				
 				double price = Double.parseDouble((String)comboBoxPris.getSelectedItem());
 				hall 	= Integer.parseInt((String)comboBoxSal.getSelectedItem());
 				
 				try 
 				{
 					date = dateFormat.parse(startDate.getText());
 					time = timeFormat.parse(startTime.getText()).getTime();
 				}
 				catch(Exception e1)
 				{
 					e1.printStackTrace();
 				}		
 
 				show.setMovie(movie);
 				show.setPrice(price);
 
 				areChangesMade = true;
 				EditShow.this.dispose();
 				latch.countDown();
 			}
 		});
 		btnGem.setBounds(154, 255, 89, 23);
 		panel.add(btnGem);
 		
 		JLabel lblStartTid_1 = new JLabel("Start tid");
 		lblStartTid_1.setBounds(10, 127, 56, 14);
 		panel.add(lblStartTid_1);
 		
 		startTime = new JFormattedTextField(showStart);
 		startTime.setBounds(197, 130, 72, 20);
 		
 //		startTime.getSelectedText((String)show.getStart());
 		panel.add(startTime);
 		
 }
	public boolean areChangesMade()
	{
 		return areChangesMade;
 	}
	public Show getShow()
	{
 		return show;
 	}
 	public Date getDate()
 	{
 		return date;
 	}
 	public long getTime()
 	{
 		return time;
 	}
 	public int getHall()
 	{
 		return hall;
 	}
 	public boolean getDataOk()
 	{
 		return dataOk;
 	}
 	public void setDataOk()
 	{
 		dataOk = true;
 	}
 	public void showErrorWrongDate()
 	{
 		JOptionPane.showMessageDialog(new JFrame(), "Der kan ikke oprettes forestillinger med dato mindre end i dag"); 
 	}
 	public void createLatch()
 	{
 		latch = new CountDownLatch(1); //venter p brugerens input.
 	}
 }
