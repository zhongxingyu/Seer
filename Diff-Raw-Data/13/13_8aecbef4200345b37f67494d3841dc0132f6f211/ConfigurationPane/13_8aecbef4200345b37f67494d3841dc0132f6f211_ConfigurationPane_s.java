 package br.ufpr.c3sl.view.config;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JApplet;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 import javax.swing.border.LineBorder;
 import javax.swing.border.TitledBorder;
 
 import netscape.javascript.JSObject;
 import br.ufpr.c3sl.connection.Internet;
 import br.ufpr.c3sl.dao.UserDAO;
 import br.ufpr.c3sl.daoFactory.DAOFactory;
 import br.ufpr.c3sl.exception.UserException;
 import br.ufpr.c3sl.model.User;
 import br.ufpr.c3sl.session.Session;
 import br.ufpr.c3sl.util.Util;
 import br.ufpr.c3sl.view.principal.JpCarrie;
 import br.ufpr.c3sl.view.util.EnableDisable;
 import br.ufpr.c3sl.view.util.LoadingPanel;
 
 public class ConfigurationPane extends JPanel {
 
 	private static final long serialVersionUID = 6616103282260587283L;
 	private JPanel jpHeader;
 	private JPanel jpBody;
 	private JPanel jpFooter;
 	private JPanel jpMain;
 
 	private JButton jbOK;
 	private JButton jbCancel;
 
 	private ButtonGroup group;
 
 	private JRadioButton jrbServer;
 	private JRadioButton jrbLocal;
 
 	private JCheckBox jcbAgree;
 
 	private JTextField jtfEmail;
 	private JLabel lbErrors;
 
 	private JPanel contentPane;
 
 	private LoadingPanel loading;
 
 	public ConfigurationPane() {
 		loading = new LoadingPanel();
 
 		this.setLayout(new GridBagLayout());
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.anchor = GridBagConstraints.CENTER;
 		gbc.weighty = 1;
 		contentPane = new JPanel(new BorderLayout());
 		contentPane.setBorder(BorderFactory.createTitledBorder("Configuração"));
 		this.add(contentPane, gbc);
 		build();
 	}
 
 	private void build(){
 		jpMain = new JPanel(new BorderLayout());
 		jpMain.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		contentPane.add(loading, BorderLayout.NORTH);
 		contentPane.add(jpMain, BorderLayout.CENTER);
 
 		jpMain.add(lbErrors = new JLabel(), BorderLayout.NORTH);
 		lbErrors.setForeground(Color.red);
 		lbErrors.setVisible(false);
 
 		GridBagConstraints c = new GridBagConstraints();
 
 		jpBody = new JPanel(new GridBagLayout());
 		jpMain.add(jpBody, BorderLayout.CENTER);
 
 		jpHeader = new JPanel();
 		jpHeader.add(new JLabel("Configurações Iniciais da Aplicação"));
 
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.gridwidth = 2;
 		jpBody.add(jpHeader, c);
 
 		c.gridwidth = 1;
 		c.gridx = 0;
 		c.gridy = 1;
 		c.weighty = 2.0;
 		c.insets = new Insets(10,0,0,0);
 		jpBody.add(new JLabel("Por favor escolha o modo de execução:"), c);
 
 		c.gridy = 2;
 
 		JPanel radioPane = new JPanel();
 		radioPane.setLayout(new BoxLayout(radioPane, BoxLayout.Y_AXIS));
 
 		jrbServer = new JRadioButton("Server Mode");
 		jrbLocal = new JRadioButton("Local Mode");
 
 		group = new ButtonGroup();
 		group.add(jrbLocal);
 		group.add(jrbServer);
 
 		radioPane.add(jrbServer);
 		radioPane.add(jrbLocal);
 
 		jpBody.add(radioPane, c);
 
 		c.gridx = 0;
 		c.gridy = 3;
 		jpBody.add(new JLabel("Entre com seu Email?"), c);
 
 		c.gridy = 4;
 		jtfEmail = new JTextField(); //"diego@gmail.com"
 		jpBody.add(jtfEmail, c);
 
 		c.gridy = 5;
 
 		FlowLayout flow = new FlowLayout();
 		flow.setAlignment(FlowLayout.LEFT);
 		JPanel agreePanel = new JPanel(flow);
 
 		jcbAgree = new JCheckBox();
 
 		agreePanel.add(jcbAgree);
 		JLabel lbAgreeText = new JLabel("<html><div style='color:black'>Concordo em ceder os dados gerados por este <br />" +
 		" software para futuras análises de Pesquisas");
 		agreePanel.add(lbAgreeText);
 		jpBody.add(agreePanel, c);
 
 		jpFooter = new JPanel();
 		jbOK = new JButton("OK");
 		jbOK.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				cmdOK();
 			}
 		});
 
 		jbCancel = new JButton("Cancelar");
 		jbCancel.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				cmdCancel();
 			}
 		});
 
 		jpFooter.add(jbOK);
 		//jpFooter.add(jbCancel);
 		jpMain.add(jpFooter, BorderLayout.SOUTH);
 	}
 
 	private void cmdOK() {
 		validateAnwser();
 	}
 
 	private void cmdCancel(){
 		if (this.getRootPane().getParent() instanceof JApplet){
 			JOptionPane.showMessageDialog(null, "Olá");
 			JSObject win = JSObject.getWindow((JApplet) this.getRootPane().getParent());
 			win.eval("alert('oi');window.close();alert('oi');");
 		}
 	}
 
 	private void validateAnwser(){
 		ArrayList<String> errors = new ArrayList<String>();
 		String msgs = "";
 
 		if(!jrbLocal.isSelected() && !jrbServer.isSelected()){
 			errors.add("Você deve selecionar um dos modos!");
 		}
 
 		if(!Util.validateEmail(jtfEmail.getText())){
 			errors.add("Email Inválido!");
 		}
 
 		if(!jcbAgree.isSelected()){
 			errors.add("Para executar o sofwre você deve concordar em " +
 			"<br />fornecer os dados");
 		}
 
 		if (errors.size() > 0){
 			for (int i = 0; i < errors.size(); i++) 
 				msgs += "<p>"+(i+1)+" - " + errors.get(i) + "</p>";
 
 			lbErrors.setBorder(BorderFactory.createTitledBorder(
 					BorderFactory.createTitledBorder(
 							new LineBorder(new Color(250, 0, 0), 2), "Incorreto",
 							TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.CENTER, getFont(), Color.black)));
 
 			lbErrors.setText("<html><div>" + msgs + "</div>");
 			lbErrors.setVisible(true);
 		}else{
 			configureSession();
 		}
 	}
 
 	private void configureSession(){
 		loading.startLoading();
 		EnableDisable.setComponentsEnabled(jpMain, false);
 		
 		Thread config = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				DAOFactory.DATABASE_CHOOSE = jrbLocal.isSelected() ? DAOFactory.DB4O : DAOFactory.MYSQL;
 
 				Internet.verifyConnection("Foi verificado que não existe conexão com a internet," +
 				"\n seu modo de execução foi alterado para local");
 
 				//System.out.println(DAOFactory.DATABASE_CHOOSE);
 
 				DAOFactory bd = DAOFactory.getDAOFactory(DAOFactory.DATABASE_CHOOSE);
 				UserDAO userDAO = bd.getUserDAO();
 
 				String email = jtfEmail.getText(); 
 
 				try {
 					User user = userDAO.findOrCreateByEmail(email);
					Session.setMode(jrbLocal.isSelected() ? "Local" : "Server");
 					Session.setCurrentUser(user);
 				} catch (UserException e) {
 					e.printStackTrace();
 				}
 
 				loading.stopLoading();
 				Container root = ConfigurationPane.this.getRootPane().getParent();
 				if (root instanceof JApplet){
 					((JApplet) root).setContentPane(JpCarrie.getInstance());
 					JpCarrie.getInstance().finalConfiguration();
 					root.validate();
 				}
 			}
 		});
 		config.start();
 	}
 }
