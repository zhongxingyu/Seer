 import java.awt.EventQueue;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import java.awt.BorderLayout;
 
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 
 import java.awt.Font;
 
 import javax.swing.JButton;
 
 import net.miginfocom.swing.MigLayout;
 
 import javax.swing.JTextField;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.Color;
 
 import javax.swing.UIManager;
 import javax.swing.border.TitledBorder;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.MatteBorder;
 
 
 public class CountingAutoTyper {
 
	public JFrame frame;
 	private JTextField textField;
	public static JButton btnStart ;
 	private static final String FONT_NAME = "Arial Rounded MT Bold";
 	private JTextField textField_1;
 	private JTextField textField_2;
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					CountingAutoTyper window = new CountingAutoTyper();
 					window.frame.setVisible(true);
 					window.frame.setAlwaysOnTop(true);
 					window.frame.setVisible(true);
 					window.frame.setTitle("Counting AutoTyper by zst123");
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 
 	public CountingAutoTyper() {
 		initialize();
 	}
 
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBackground(new Color(0, 0, 0));
 		frame.setBounds(100, 100, 252, 255);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setAlwaysOnTop(true);
 		JPanel panel = new JPanel();
 		frame.getContentPane().add(panel, BorderLayout.NORTH);
 		panel.setLayout(new BorderLayout(0, 0));
 		
 		JLabel lblCountingAutoTyper = new JLabel("Counting Auto Typer");
 		lblCountingAutoTyper.setForeground(Color.LIGHT_GRAY);
 		lblCountingAutoTyper.setVerticalAlignment(SwingConstants.BOTTOM);
 		lblCountingAutoTyper.setFont(new Font(FONT_NAME, Font.BOLD, 23));
 		lblCountingAutoTyper.setHorizontalAlignment(SwingConstants.CENTER);
 		panel.add(lblCountingAutoTyper);
 		
 		JLabel lblNewLabel = new JLabel("(Accending Digits Spammer)");
 		lblNewLabel.setForeground(Color.LIGHT_GRAY);
 		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		lblNewLabel.setFont(new Font(FONT_NAME, Font.BOLD | Font.ITALIC, 13));
 		panel.add(lblNewLabel, BorderLayout.SOUTH);
 		
 		
 		
 		
 		JPanel panel_1 = new JPanel();
 		panel_1.setBackground(Color.BLACK);
 		frame.getContentPane().add(panel_1, BorderLayout.CENTER);
 		panel_1.setLayout(new MigLayout("", "[][grow]", "[][][][][]"));
 		
 		JLabel lblNewLabel_1 = new JLabel("Delay Per Spam (ms)");
 		lblNewLabel_1.setForeground(new Color(255, 0, 0));
 		lblNewLabel_1.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblNewLabel_1.setFont(new Font(FONT_NAME, Font.BOLD, 14));
 		panel_1.add(lblNewLabel_1, "cell 0 0,alignx trailing");
 		
 		textField = new JTextField();
 		textField.setForeground(new Color(200, 0, 0));
 		textField.setText("75");
 		textField.setFont(new Font(FONT_NAME, Font.BOLD, 14));
 		panel_1.add(textField, "cell 1 0,growx");
 		textField.setColumns(5);
 		
 		JLabel lblInitialDelayms = new JLabel("Initial Delay (ms)");
 		lblInitialDelayms.setForeground(new Color(0, 255, 255));
 		lblInitialDelayms.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblInitialDelayms.setFont(new Font(FONT_NAME, Font.BOLD, 14));
 		panel_1.add(lblInitialDelayms, "cell 0 1,alignx trailing");
 		
 		textField_1 = new JTextField();
 		textField_1.setForeground(new Color(0, 190, 190));
 		textField_1.setText("3000");
 		textField_1.setColumns(5);
 		textField_1.setFont(new Font(FONT_NAME, Font.BOLD, 14));
 		panel_1.add(textField_1, "cell 1 1,growx");
 		
 		JLabel lblNoToSpam = new JLabel("Times to repeat");
 		lblNoToSpam.setForeground(Color.GREEN);
 		lblNoToSpam.setHorizontalAlignment(SwingConstants.RIGHT);
 		lblNoToSpam.setFont(new Font(FONT_NAME, Font.BOLD, 14));
 		panel_1.add(lblNoToSpam, "cell 0 2,alignx trailing");
 		
 		textField_2 = new JTextField();
 		textField_2.setForeground(new Color(0, 128, 0));
 		textField_2.setText("100");
 		textField_2.setToolTipText("");
 		textField_2.setColumns(5);
 		textField_2.setFont(new Font(FONT_NAME, Font.BOLD, 14));
 		panel_1.add(textField_2, "cell 1 2,growx");
 		
 		JLabel lbluse = new JLabel("       (0 for infinite until stopped) ");
 		lbluse.setForeground(new Color(255, 204, 255));
 		lbluse.setHorizontalAlignment(SwingConstants.CENTER);
 		panel_1.add(lbluse, "cell 0 3 2 1");
 		lbluse.setFont(new Font(FONT_NAME, Font.BOLD|Font.ITALIC, 13));
 		
 		JPanel panel_2 = new JPanel();
 		panel_2.setBackground(new Color(0, 0, 0));
 		frame.getContentPane().add(panel_2, BorderLayout.SOUTH);
 		 panel_2.setLayout(new BorderLayout(0, 0));
 		
 		
 		 btnStart = new JButton("START");
 		 btnStart.setBackground(new Color(255, 215, 0));
 		 btnStart.setForeground(new Color(255, 215, 0));
 		 panel_2.add(btnStart);
 		 btnStart.addActionListener(new ActionListener() {
 		 	
 		 	public void actionPerformed(ActionEvent arg0) {
 		 		if(Common.SPAM_STARTED){
 		 		updateVariable(false);
 		 		}else{
 		 		updateVariable(true);
 		 		startSpam(textField,textField_1,textField_2);
 		 		}
 		 	}
 		 });
 		 btnStart.setFont(new Font(FONT_NAME, Font.BOLD, 20));
 		 btnStart.setBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 215, 0), new Color(255, 215, 0)));
 	}
	public static void updateVariable(boolean on){
 		Common.SPAM_STARTED = on;
 		if (on){
 			btnStart.setText("STOP");
 		}else{
 			btnStart.setText("START");
 		}
 	}
 	private void removeSpace(JTextField tF){
 		String newString = tF.getText().replaceAll( "[^\\d]", "" ); //Remove all non-numeric characters
 		tF.setText(newString);
 	}
 	
 	private void startSpam(JTextField delayTF, JTextField initialDelayTF, JTextField repeatTF){
 		removeSpace(delayTF);
 		removeSpace(initialDelayTF);
 		removeSpace(repeatTF);
 		int delay = Integer.parseInt(delayTF.getText());
 		final int initial = Integer.parseInt(initialDelayTF.getText());
 		final int loops = Integer.parseInt(repeatTF.getText());
 		if (delay<1) delay = 1;
 		final int delay2 = delay;
 		Runnable r = new Runnable(){
 			@Override
 			public void run() {
 				Common.type(loops, initial, delay2);
 			}
 		};	
 		Thread d = new Thread(r);
 		d.start();
 	}
 }
