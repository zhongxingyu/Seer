 package views;
 
 import javax.swing.JPanel;
 import javax.swing.JButton;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JEditorPane;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import net.miginfocom.swing.MigLayout;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JTextField;
 
 import views.components.NicePanel;
 
 public class ResultatenScherm extends NicePanel {
 	private JTextField txtAthene;
 	private JTextField txtAthene_1;
 	private JTextField txtAthene_2;
 	private JTextField txtAthene_3;
 	private JTextField txtAthene_4;
 	private JTextField txtAthene_5;
 	private JTextField txtAthene_6;
 	private JTextField txtAthene_7;
 	private JTextField txtGoed;
 	private JTextField txtFout;
 	private JTextField textField_1;
 	private JTextField textField_2;
 	private JTextField txtFout_1;
 	private JTextField textField_4;
 	private JTextField txtFout_2;
 	private JTextField txtFout_3;
 
 	/**
 	 * Create the panel.
 	 * @param mainWindow 
 	 */
 	public ResultatenScherm(final MainWindow mainWindow) {
 				
 		txtAthene = new JTextField();
 		txtAthene.setEditable(false);
 		txtAthene.setText("Athene");
 		txtAthene.setColumns(10);
 		
 		txtAthene_1 = new JTextField();
 		txtAthene_1.setEditable(false);
 		txtAthene_1.setText("Athene");
 		txtAthene_1.setColumns(10);
 		
 		txtAthene_2 = new JTextField();
 		txtAthene_2.setEditable(false);
 		txtAthene_2.setText("Athene");
 		txtAthene_2.setColumns(10);
 		
 		txtAthene_3 = new JTextField();
 		txtAthene_3.setEditable(false);
 		txtAthene_3.setText("Athene");
 		txtAthene_3.setColumns(10);
 		
 		txtAthene_4 = new JTextField();
 		txtAthene_4.setEditable(false);
 		txtAthene_4.setText("Athene");
 		txtAthene_4.setColumns(10);
 		
 		txtAthene_5 = new JTextField();
 		txtAthene_5.setEditable(false);
 		txtAthene_5.setText("Athene");
 		txtAthene_5.setColumns(10);
 		
 		txtAthene_6 = new JTextField();
 		txtAthene_6.setEditable(false);
 		txtAthene_6.setText("athene");
 		txtAthene_6.setColumns(10);
 		
 		txtAthene_7 = new JTextField();
 		txtAthene_7.setEditable(false);
 		txtAthene_7.setText("athene");
 		txtAthene_7.setColumns(10);
 		
 		txtGoed = new JTextField();
 		txtGoed.setEditable(false);
 		txtGoed.setText("Goed");
 		txtGoed.setColumns(10);
 		
 		txtFout = new JTextField();
 		txtFout.setEditable(false);
 		txtFout.setText("Fout");
 		txtFout.setColumns(10);
 		
 		textField_1 = new JTextField();
 		textField_1.setEditable(false);
 		textField_1.setText("Goed");
 		textField_1.setColumns(10);
 		
 		textField_2 = new JTextField();
 		textField_2.setEditable(false);
 		textField_2.setText("Goed");
 		textField_2.setColumns(10);
 		
 		txtFout_1 = new JTextField();
 		txtFout_1.setEditable(false);
 		txtFout_1.setText("Fout");
 		txtFout_1.setColumns(10);
 		
 		textField_4 = new JTextField();
 		textField_4.setEditable(false);
 		textField_4.setText("Goed");
 		textField_4.setColumns(10);
 		
 		txtFout_2 = new JTextField();
 		txtFout_2.setEditable(false);
 		txtFout_2.setText("Fout");
 		txtFout_2.setColumns(10);
 		
 		txtFout_3 = new JTextField();
 		txtFout_3.setEditable(false);
 		txtFout_3.setText("Fout");
 		txtFout_3.setColumns(10);
 		
 		JButton btnStoppen = new JButton("Stoppen");
 		btnStoppen.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			}
 		});
 		
 		JButton btnNewButton = new JButton("Verder");
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
				mainWindow.openPanel(new SpeelScherm(mainWindow));
 			}
 		});
 		
 		setLayout(new MigLayout("", "[grow][77px][86px][][95px][6px][90px][grow]", "[grow][20px][20px][20px][20.00px][20px][20px][20px][20.00px][grow]"));
 		
 		JEditorPane dtrpnScore = new JEditorPane();
 		dtrpnScore.setEditable(false);
 		dtrpnScore.setText("Aantal goed: 5 \r\nAantal fout: 4 \r\nAantal joker gebruikt: 0     \r\n \r\nScore: 1500 \r\n \r\nTijd: 90 sec");
 		add(dtrpnScore, "cell 4 1 3 5,grow");
 		add(txtAthene_5, "cell 1 6,grow");
 		add(textField_4, "cell 2 6,grow");
 		add(txtAthene_4, "cell 1 5,grow");
 		add(txtFout_1, "cell 2 5,grow");
 		add(txtAthene, "cell 1 1,grow");
 		add(txtAthene_7, "cell 1 8,grow");
 		add(txtAthene_2, "cell 1 3,grow");
 		add(txtAthene_3, "cell 1 4,grow");
 		add(txtAthene_1, "cell 1 2,grow");
 		add(txtGoed, "cell 2 1,grow");
 		add(txtFout, "cell 2 2,grow");
 		add(textField_1, "cell 2 3,grow");
 		add(textField_2, "cell 2 4,grow");
 		add(txtFout_3, "cell 2 8,grow");
 		add(txtAthene_6, "cell 1 7,grow");
 		add(txtFout_2, "cell 2 7,grow");
 		add(btnStoppen, "cell 4 7 1 2,grow");
 		add(btnNewButton, "cell 6 7 1 2,grow");
 
 	}
 }
