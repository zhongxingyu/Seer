 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Dialog;
 import java.awt.EventQueue;
 import java.awt.Frame;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.JScrollPane;
 import javax.swing.SwingConstants;
 
 import java.awt.Toolkit;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 public class Error extends JDialog {
 
 	private JPanel contentPane;
 
 	/**
 	 * Create the frame.
 	 * @wbp.parser.constructor
 	 */
 	public Error(Component parent) {
 	  super((Frame) parent);
 		setIconImage(Toolkit.getDefaultToolkit().getImage(Error.class.getResource("/gui/resources/img16x16/dialog-close-2.png")));
 		setTitle("Error");
 		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		setBounds(100, 100, 450, 218);
 		setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		
 		JPanel panel = new JPanel();
 		
 		JButton btnOk = new JButton("OK");
 		btnOk.addActionListener(new ActionListener() {
 		  public void actionPerformed(ActionEvent arg0) {
 		    setVisible(false);
 		  }
 		});
 		GroupLayout gl_contentPane = new GroupLayout(contentPane);
 		gl_contentPane.setHorizontalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addComponent(panel, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
 				.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
 					.addContainerGap(189, Short.MAX_VALUE)
 					.addComponent(btnOk)
 					.addGap(186))
 		);
 		gl_contentPane.setVerticalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE)
 					.addGap(18)
 					.addComponent(btnOk)
 					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 		);
 		panel.setLayout(new BorderLayout(0, 0));
 		
 		JScrollPane scrollPane = new JScrollPane();
     panel.add(scrollPane, BorderLayout.CENTER);
     contentPane.setLayout(gl_contentPane);
     JLabel label = new JLabel("Error: An unknown Error occured!");
     scrollPane.setViewportView(label);
     label.setVerticalAlignment(SwingConstants.TOP);
 	}
 	
	public Error(Component parent, String message)
 	{
 	  super((Frame) parent);
 		setIconImage(Toolkit.getDefaultToolkit().getImage(Error.class.getResource("/gui/resources/img16x16/dialog-close-2.png")));
		setTitle("Error");
 		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 		setModalityType(Dialog.ModalityType.DOCUMENT_MODAL);
 		setBounds(100, 100, 450, 218);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		
 		JPanel panel = new JPanel();
 		
 		JButton btnOk = new JButton("OK");
 		btnOk.addActionListener(new ActionListener() {
       public void actionPerformed(ActionEvent arg0) {
         setVisible(false);
       }
 		});
 		GroupLayout gl_contentPane = new GroupLayout(contentPane);
 		gl_contentPane.setHorizontalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addComponent(panel, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
 				.addGroup(Alignment.TRAILING, gl_contentPane.createSequentialGroup()
 					.addContainerGap(189, Short.MAX_VALUE)
 					.addComponent(btnOk)
 					.addGap(186))
 		);
 		gl_contentPane.setVerticalGroup(
 			gl_contentPane.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_contentPane.createSequentialGroup()
 					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 117, GroupLayout.PREFERRED_SIZE)
 					.addGap(18)
 					.addComponent(btnOk)
 					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 		);
 		panel.setLayout(new BorderLayout(0, 0));
 		
 		JScrollPane scrollPane = new JScrollPane();
 		panel.add(scrollPane, BorderLayout.CENTER);
 		contentPane.setLayout(gl_contentPane);
 		JLabel label = new JLabel(message);
 		scrollPane.setViewportView(label);
 		label.setVerticalAlignment(SwingConstants.TOP);
 	}
 }
