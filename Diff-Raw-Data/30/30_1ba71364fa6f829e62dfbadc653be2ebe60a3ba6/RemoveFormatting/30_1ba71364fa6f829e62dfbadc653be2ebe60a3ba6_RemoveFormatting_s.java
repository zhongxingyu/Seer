 package Frames;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.LinkedList;
 
 import javax.imageio.ImageIO;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 import ClassAdminBackEnd.Format;
 import ClassAdminBackEnd.SuperEntity;
 import ClassAdminFrontEnd.BackgroundGradientPanel;
 import ClassAdminFrontEnd.FrmTable;
 import ClassAdminFrontEnd.ReflectionButton;
 
 public class RemoveFormatting extends JFrame {
 
 	private JPanel contentPane;
 	private BackgroundGradientPanel backgroundPanel;
 	private ReflectionButton btnNewButton;
 
 	/**
 	 * @param table
 	 *            creates a new removeformatting frame
 	 */
 	public RemoveFormatting(final FrmTable table) {
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setBounds(100, 100, 376, 236);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		this.setTitle("Remove Formatting");
 		
 		Image icon = Toolkit.getDefaultToolkit().getImage("icons/ConditionalFormattingFrame.png");
 		this.setIconImage(icon);
 
 		backgroundPanel = new BackgroundGradientPanel(contentPane);
 		backgroundPanel.setBounds(0, 0, 446, 286);
 		contentPane.add(backgroundPanel);
 		backgroundPanel.setLayout(null);
 
 		JLabel lblRemove = new JLabel("Remove this Format");
		lblRemove.setBounds(36, 46, 149, 14);
 		lblRemove.setForeground(new Color(0xEDEDED));
 		backgroundPanel.add(lblRemove);
				
		lblRemove.setBounds(10, 11, 149, 14);
		contentPane.add(lblRemove);
 
 		final JComboBox cbxFormatting = new JComboBox();
		cbxFormatting.setBounds(181, 36, 155, 34);
 		backgroundPanel.add(cbxFormatting);
 		
 		try {
 			btnNewButton = new ReflectionButton(ImageIO.read(getClass().getResource("/ClassAdminFrontEnd/resources/ConditionalFormattingFrameLabelRemove.png")));
			btnNewButton.setBounds(256, 91, 85, 92);
 			backgroundPanel.add(btnNewButton);
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
				
		cbxFormatting.setBounds(203, 8, 149, 20);
		contentPane.add(cbxFormatting);

		JButton btnNewButton = new JButton("Remove Format");
		btnNewButton.setBounds(12, 133, 340, 61);
		contentPane.add(btnNewButton);
 
 		final LinkedList<Integer> formats = new LinkedList<Integer>();
 		final LinkedList<SuperEntity> heads;
 
 		heads = table.project.getHead().getHeadersLinkedList();
 
 		for (int x = heads.size() - 1; x >= 0; x--) {
 			if (heads.get(x).getType().getIsTextField()) {
 				heads.remove(x);
 			}
 		}
 
 		for (int x = 0; x < heads.size(); x++) {
 			if (heads.get(x).getType().getFormatting().size() > 0) {
 
 				formats.add(x);
 
 			}
 		}
 
 		String[] bcases = new String[formats.size()];
 
 		for (int x = 0; x < formats.size(); x++) {
 			bcases[x] = heads.get(formats.get(x)).getType().getName();
 		}
 
 		cbxFormatting.setModel(new DefaultComboBoxModel(bcases));
 
 		btnNewButton.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				try {
 					table.headersList
 							.get(table.headPoints.get(formats.get(cbxFormatting
 									.getSelectedIndex()))).getType()
 							.clearFormatting();
 					closeFrame();
 					table.repaint();
 				} catch (Exception e) {
 					closeFrame();
 				}
 
 			}
 		});
 	}
 
 	/**
 	 * closes the frame
 	 */
 	public void closeFrame() {
 		this.dispose();
 	}
 
 }
