 package br.com.sani.telas;
 
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.text.ParseException;
 
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JRadioButton;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 import javax.swing.border.EmptyBorder;
 
 import br.com.sani.bean.Endereco;
 import br.com.sani.bean.Funcionario;
 import br.com.sani.dao.EnderecoDAO;
 import br.com.sani.dao.FuncionarioDAO;
 import br.com.sani.exception.DAOException;
 import br.com.sani.exception.EntradaUsuarioException;
 import br.com.sani.util.ImagePanel;
 import br.com.sani.util.Mascara;
 import br.com.sani.util.SwingUtil;
 import br.com.sani.util.TelaUtil;
 
 import com.toedter.calendar.JDateChooser;
 
 import javax.swing.border.TitledBorder;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 
 public class frmCadastroFuncionario extends JFrame {
 
 	private static final long serialVersionUID = 1L;
 	private JPanel contentPane;
 	private JTextField txtNomeFuncionario;
 	private JTextField txtEnderecoFuncionario;
 	private JTextField txtNumeroEnderecoFuncionario;
 	private JTextField txtComplementoFuncionario;
 	private JTextField txtSiteFuncionario;
 	private JTextField txtEmailFuncionario;
 	private JTextField txtNacionalidadeFuncionario;
 	private JTextField txtLoginFuncionario;
 	private JPasswordField pfSenhaFuncionario;
 	private JPasswordField pfConfSenhaFuncionario;
 	
 	private JFormattedTextField ftCepFuncionario;
 	private JFormattedTextField ftCpfFuncionario;
 	private JFormattedTextField ftRgFuncionario;
 	private JFormattedTextField ftTelefoneResidencialFuncionario;
 	private JFormattedTextField ftTelefoneCelularFuncionario;
 	private JRadioButton rbMasculinoFuncionario;
 	private JRadioButton rbFemininoFuncionario;
 	private JComboBox cbEstadoCivilFuncionario;
 	private JRadioButton rbCargoCorretorFuncionario;
 	private JRadioButton rbCargoSecretariaFuncionario;
 	private JRadioButton rbCargoGerenteFuncionario;
 	private JRadioButton rbCargoConsultorFuncionario;
 	private ImagePanel imagePanel;
 	private JButton btnSalvarFuncionario;
 	private JButton btnCancelarFuncionario;
 	private JButton btnLimparCamposFuncionario;
 	
 	private byte[] foto;
 	private JPanel pnSexo;
 	private JPanel pnCargo;
 	private JDateChooser dtNascimento;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					frmCadastroFuncionario frame = new frmCadastroFuncionario();
 					frame.setVisible(true);
 				} catch (Throwable e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 * @throws Throwable 
 	 */
 	public frmCadastroFuncionario() throws Throwable {
 		SwingUtil.lookWindows(this);
 		setResizable(false);
 		setTitle("Cadastro Funcionario");
 		setIconImage(Toolkit.getDefaultToolkit().getImage(frmCadastroFuncionario.class.getResource("/br/com/images/home_badge.png")));
 		setBounds(100, 100, 700, 511);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		JPanel panel = new JPanel();
 		panel.setLayout(null);
 		panel.setBounds(0, 0, 694, 485);
 		contentPane.add(panel);
 		
 		JLabel lblNomeFuncionario = new JLabel("Nome:*");
 		lblNomeFuncionario.setBounds(73, 29, 37, 14);
 		panel.add(lblNomeFuncionario);
 		
 		//Cria o Button Group
 		
 		ButtonGroup grupoSexo = new ButtonGroup();  
 		
 		txtNomeFuncionario = new JTextField();
 		txtNomeFuncionario.setName("Nome");
 		txtNomeFuncionario.setColumns(10);
 		txtNomeFuncionario.setBounds(120, 26, 299, 20);
 		panel.add(txtNomeFuncionario);
 		
 		JLabel lblCpfFuncionario = new JLabel("CPF:*");
 		lblCpfFuncionario.setBounds(80, 82, 30, 14);
 		panel.add(lblCpfFuncionario);
 		
 		ftCpfFuncionario = new JFormattedTextField(Mascara.setMaskCpfInTf(ftCpfFuncionario));
 		ftCpfFuncionario.setName("CPF");
 		ftCpfFuncionario.setColumns(10);
 		ftCpfFuncionario.setBounds(120, 79, 106, 20);
 		panel.add(ftCpfFuncionario);
 		
 		JLabel lblEstadoCivilFuncionario = new JLabel("Estado Civil:");
 		lblEstadoCivilFuncionario.setBounds(444, 29, 60, 14);
 		panel.add(lblEstadoCivilFuncionario);
 		
 		cbEstadoCivilFuncionario = new JComboBox();
 		cbEstadoCivilFuncionario.setName("Estado Civil");
 		cbEstadoCivilFuncionario.setModel(new DefaultComboBoxModel(new String[] {"Solteiro (a)", "Casado (a)", "Divorciado (a)", "Vi\u00FAvo (a)"}));
 		cbEstadoCivilFuncionario.setBounds(514, 26, 137, 20);
 		panel.add(cbEstadoCivilFuncionario);
 		
 		JLabel lblRgFuncionario = new JLabel("RG:");
 		lblRgFuncionario.setBounds(293, 82, 18, 14);
 		panel.add(lblRgFuncionario);
 		
 		ftRgFuncionario = new JFormattedTextField();
 		ftRgFuncionario.setName("RG");
 		ftRgFuncionario.setColumns(10);
 		ftRgFuncionario.setBounds(321, 79, 98, 20);
 		panel.add(ftRgFuncionario);
 		
 		JLabel lblNacionalidadeFuncionario = new JLabel("Nacionalidade:");
 		lblNacionalidadeFuncionario.setBounds(433, 57, 71, 14);
 		panel.add(lblNacionalidadeFuncionario);
 		
 		txtNacionalidadeFuncionario = new JTextField();
 		txtNacionalidadeFuncionario.setName("Nacionalidade");
 		txtNacionalidadeFuncionario.setColumns(10);
 		txtNacionalidadeFuncionario.setBounds(514, 54, 137, 20);
 		panel.add(txtNacionalidadeFuncionario);
 		
 		JLabel lblEnderecoFuncionario = new JLabel("Endere\u00E7o:");
 		lblEnderecoFuncionario.setBounds(61, 132, 49, 14);
 		panel.add(lblEnderecoFuncionario);
 		
 		txtEnderecoFuncionario = new JTextField();
 		txtEnderecoFuncionario.setEditable(false);
 		txtEnderecoFuncionario.setName("Endere\u00E7o");
 		txtEnderecoFuncionario.setColumns(10);
 		txtEnderecoFuncionario.setBounds(120, 129, 299, 20);
 		panel.add(txtEnderecoFuncionario);
 		
 		JLabel lblNumeroFuncionario = new JLabel("N\u00FAmero:*");
 		lblNumeroFuncionario.setBounds(264, 107, 47, 14);
 		panel.add(lblNumeroFuncionario);
 		
 		txtNumeroEnderecoFuncionario = new JTextField();
 		txtNumeroEnderecoFuncionario.setName("N\u00FAmero da Residencia");
 		txtNumeroEnderecoFuncionario.setColumns(10);
 		txtNumeroEnderecoFuncionario.setBounds(321, 104, 98, 20);
 		panel.add(txtNumeroEnderecoFuncionario);
 		
 		JLabel lblComplementoFuncionario = new JLabel("Complemento:");
 		lblComplementoFuncionario.setBounds(39, 158, 71, 14);
 		panel.add(lblComplementoFuncionario);
 		
 		txtComplementoFuncionario = new JTextField();
 		txtComplementoFuncionario.setName("Complemento");
 		txtComplementoFuncionario.setColumns(10);
 		txtComplementoFuncionario.setBounds(120, 155, 299, 20);
 		panel.add(txtComplementoFuncionario);
 		
 		JLabel lblCepFuncionario = new JLabel("CEP:*");
 		lblCepFuncionario.setBounds(80, 107, 30, 14);
 		panel.add(lblCepFuncionario);
 		
 		ftCepFuncionario = new JFormattedTextField(Mascara.setMaskCepInTf(ftCepFuncionario));
 		ftCepFuncionario.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusLost(FocusEvent e) {
 				buscarEndereco();
 			}
 		});
 		ftCepFuncionario.setName("CEP");
 		ftCepFuncionario.setColumns(10);
 		ftCepFuncionario.setBounds(120, 104, 106, 20);
 		panel.add(ftCepFuncionario);
 		
 		JLabel lblTelefoneResidencialFuncionario = new JLabel("Telefone Res.:");
 		lblTelefoneResidencialFuncionario.setBounds(39, 57, 71, 14);
 		panel.add(lblTelefoneResidencialFuncionario);
 		
 		ftTelefoneResidencialFuncionario = new JFormattedTextField(Mascara.setMaskTelefoneInTf(ftTelefoneResidencialFuncionario));
 		ftTelefoneResidencialFuncionario.setName("Telefone Residencial");
 		ftTelefoneResidencialFuncionario.setColumns(10);
 		ftTelefoneResidencialFuncionario.setBounds(120, 54, 106, 20);
 		panel.add(ftTelefoneResidencialFuncionario);
 		
 		JLabel lblTelefoneCelularFuncionario = new JLabel("Telefone Cel.:*");
 		lblTelefoneCelularFuncionario.setBounds(236, 57, 75, 14);
 		panel.add(lblTelefoneCelularFuncionario);
 		
 		ftTelefoneCelularFuncionario = new JFormattedTextField(Mascara.setMaskCelularInTf(ftTelefoneCelularFuncionario));
 		ftTelefoneCelularFuncionario.setName("Telefone Celular");
 		ftTelefoneCelularFuncionario.setColumns(10);
 		ftTelefoneCelularFuncionario.setBounds(321, 54, 98, 20);
 		panel.add(ftTelefoneCelularFuncionario);
 		
 		JSeparator separator = new JSeparator();
 		separator.setBounds(10, 186, 674, 2);
 		panel.add(separator);
 		
 		JSeparator separator_1 = new JSeparator();
 		separator_1.setBounds(10, 350, 479, 2);
 		panel.add(separator_1);
 		
 		btnLimparCamposFuncionario = new JButton("Limpar Campos");
 		btnLimparCamposFuncionario.setIcon(new ImageIcon(frmCadastroFuncionario.class.getResource("/br/com/images/clear.png")));
 		btnLimparCamposFuncionario.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				limpaFormulario();
 			}
 		});
 		btnLimparCamposFuncionario.setBounds(514, 363, 137, 32);
 		panel.add(btnLimparCamposFuncionario);
 		
 		btnCancelarFuncionario = new JButton("Cancelar");
 		btnCancelarFuncionario.setIcon(new ImageIcon(frmCadastroFuncionario.class.getResource("/br/com/images/delete-.png")));
 		btnCancelarFuncionario.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				fechar();
 			}
 		});
 		btnCancelarFuncionario.setBounds(514, 399, 137, 32);
 		panel.add(btnCancelarFuncionario);
 		
 		btnSalvarFuncionario = new JButton("Salvar");
 		btnSalvarFuncionario.setIcon(new ImageIcon(frmCadastroFuncionario.class.getResource("/br/com/images/save.png")));
 		btnSalvarFuncionario.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				salvarCadastro();
 			}
 		});
 		btnSalvarFuncionario.setBounds(514, 435, 137, 32);
 		panel.add(btnSalvarFuncionario);
 		
 		JLabel lblContatoOnlineFuncionario = new JLabel("Contato Online:");
 		lblContatoOnlineFuncionario.setBounds(294, 377, 89, 14);
 		panel.add(lblContatoOnlineFuncionario);
 		
 		JLabel lblEmailFuncionario = new JLabel("Email:*");
 		lblEmailFuncionario.setBounds(294, 402, 46, 14);
 		panel.add(lblEmailFuncionario);
 		
 		JLabel lblSiteFuncionario = new JLabel("Site:");
 		lblSiteFuncionario.setBounds(294, 427, 46, 14);
 		panel.add(lblSiteFuncionario);
 		
 		txtSiteFuncionario = new JTextField();
 		txtSiteFuncionario.setName("Site");
 		txtSiteFuncionario.setColumns(10);
 		txtSiteFuncionario.setBounds(335, 424, 154, 20);
 		panel.add(txtSiteFuncionario);
 		
 		txtEmailFuncionario = new JTextField();
 		txtEmailFuncionario.setName("Email");
 		txtEmailFuncionario.setColumns(10);
 		txtEmailFuncionario.setBounds(335, 399, 154, 20);
 		panel.add(txtEmailFuncionario);
 		
 		//Cria o Button Group
 		
 		ButtonGroup grupoCargo = new ButtonGroup();
 		
 		JPanel panelLogin = new JPanel();
 		panelLogin.setBackground(Color.GRAY);
 		panelLogin.setBounds(39, 363, 248, 95);
 		panel.add(panelLogin);
 		panelLogin.setLayout(null);
 		
 		JLabel lblLoginFuncionario = new JLabel("Login: *");
 		lblLoginFuncionario.setBounds(10, 16, 46, 14);
 		panelLogin.add(lblLoginFuncionario);
 		
 		JLabel lblSenhaFuncionario = new JLabel("Senha: * ");
 		lblSenhaFuncionario.setBounds(10, 41, 46, 14);
 		panelLogin.add(lblSenhaFuncionario);
 		
 		txtLoginFuncionario = new JTextField();
 		txtLoginFuncionario.setName("Login");
 		txtLoginFuncionario.setColumns(10);
 		txtLoginFuncionario.setBounds(114, 13, 124, 20);
 		panelLogin.add(txtLoginFuncionario);
 		
 		JLabel lblConfSenhaFuncionario = new JLabel("Confirmar Senha: *");
 		lblConfSenhaFuncionario.setBounds(10, 66, 102, 14);
 		panelLogin.add(lblConfSenhaFuncionario);
 		
 		pfSenhaFuncionario = new JPasswordField();
 		pfSenhaFuncionario.setName("Senha");
 		pfSenhaFuncionario.setBounds(114, 38, 124, 20);
 		panelLogin.add(pfSenhaFuncionario);
 		
 		pfConfSenhaFuncionario = new JPasswordField();
 		pfConfSenhaFuncionario.setName("Confirma\u00E7\u00E3o de Senha");
 		pfConfSenhaFuncionario.setBounds(114, 63, 124, 20);
 		panelLogin.add(pfConfSenhaFuncionario);
 		
 		pnSexo = new JPanel();
 		pnSexo.setBorder(new TitledBorder(null, "Sexo*", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		pnSexo.setBounds(514, 107, 137, 68);
 		panel.add(pnSexo);
 		pnSexo.setLayout(null);
 		
 		rbMasculinoFuncionario = new JRadioButton("Masculino");
 		rbMasculinoFuncionario.setSelected(true);
 		rbMasculinoFuncionario.setBounds(17, 15, 109, 23);
 		pnSexo.add(rbMasculinoFuncionario);
 		grupoSexo.add(rbMasculinoFuncionario);  
 		
 		rbFemininoFuncionario = new JRadioButton("Feminino");
 		rbFemininoFuncionario.setBounds(17, 35, 109, 23);
 		pnSexo.add(rbFemininoFuncionario);
 		grupoSexo.add(rbFemininoFuncionario);
 		
 		JLabel lblDataNasc = new JLabel("Data Nasc.:");
 		lblDataNasc.setBounds(444, 82, 60, 14);
 		panel.add(lblDataNasc);
 		
 		pnCargo = new JPanel();
 		pnCargo.setBorder(new TitledBorder(null, "Cargo*", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		pnCargo.setBounds(433, 200, 218, 149);
 		panel.add(pnCargo);
 		pnCargo.setLayout(null);
 		
 		rbCargoCorretorFuncionario = new JRadioButton("Corretor (a) de Im\u00F3veis");
 		rbCargoCorretorFuncionario.setBounds(19, 29, 139, 23);
 		pnCargo.add(rbCargoCorretorFuncionario);
 		rbCargoCorretorFuncionario.setSelected(true);
 		grupoCargo.add(rbCargoCorretorFuncionario);  
 		
 		rbCargoSecretariaFuncionario = new JRadioButton("Secret\u00E1ria ");
 		rbCargoSecretariaFuncionario.setBounds(19, 55, 109, 23);
 		pnCargo.add(rbCargoSecretariaFuncionario);
 		grupoCargo.add(rbCargoSecretariaFuncionario);
 		
 		rbCargoGerenteFuncionario = new JRadioButton("Gerente");
 		rbCargoGerenteFuncionario.setBounds(19, 81, 109, 23);
 		pnCargo.add(rbCargoGerenteFuncionario);
 		grupoCargo.add(rbCargoGerenteFuncionario);  
 		
 		rbCargoConsultorFuncionario = new JRadioButton("Consultor (a)");
 		rbCargoConsultorFuncionario.setBounds(19, 106, 109, 23);
 		pnCargo.add(rbCargoConsultorFuncionario);
 		grupoCargo.add(rbCargoConsultorFuncionario);
 		
 		JPanel panel_1 = new JPanel();
 		panel_1.setBorder(new TitledBorder(null, "Imagem*", TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel_1.setBounds(39, 199, 380, 149);
 		panel.add(panel_1);
 		panel_1.setLayout(null);
 		
 		JButton btnProcurarImagemFuncionario = new JButton("Procurar...");
 		btnProcurarImagemFuncionario.setBounds(224, 31, 134, 23);
 		panel_1.add(btnProcurarImagemFuncionario);
 		
 		JButton btnDefinirImagemFuncionario = new JButton("Definir Imagem");
 		btnDefinirImagemFuncionario.setBounds(224, 66, 134, 23);
 		panel_1.add(btnDefinirImagemFuncionario);
 		
 		JButton btnExcluirImagemFuncionario = new JButton("Excluir Imagem");
 		btnExcluirImagemFuncionario.setBounds(224, 101, 134, 23);
 		panel_1.add(btnExcluirImagemFuncionario);
 		
 		JLabel lblExcluirImagem = new JLabel("");
 		lblExcluirImagem.setBounds(184, 101, 25, 25);
 		panel_1.add(lblExcluirImagem);
 		lblExcluirImagem.setIcon(new ImageIcon(frmCadastroFuncionario.class.getResource("/br/com/images/delete-.png")));
 		
 		JLabel label = new JLabel("");
 		label.setBounds(184, 66, 25, 25);
 		panel_1.add(label);
 		label.setIcon(new ImageIcon(frmCadastroFuncionario.class.getResource("/br/com/images/apply-.png")));
 		
 		JLabel label_1 = new JLabel("");
 		label_1.setBounds(180, 31, 25, 25);
 		panel_1.add(label_1);
 		label_1.setIcon(new ImageIcon(frmCadastroFuncionario.class.getResource("/br/com/images/search-ico.png")));
 		
 		imagePanel = new ImagePanel(null);
 		imagePanel.setName("Imagem");
 		imagePanel.setBounds(10, 19, 120, 123);
 		panel_1.add(imagePanel);
 		
 		dtNascimento = new JDateChooser();
 		dtNascimento.setName("Data de Nascimento");
 		dtNascimento.setBounds(514, 79, 89, 20);
 		panel.add(dtNascimento);
 		btnProcurarImagemFuncionario.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				try {
 					escolherFoto();
 				} catch (EntradaUsuarioException e1) {
 					e1.printStackTrace();
 				}
 			}
 		});
 		
 		setLocationRelativeTo(null);		
 	}
 	
 	public void fechar(){
 		this.dispose();
 	}
 	
 	public void limpaFormulario(){
 		txtNomeFuncionario.setText("");
 		ftCpfFuncionario.setText("");
 		ftRgFuncionario.setText("");
 		txtNacionalidadeFuncionario.setText("");
 		txtEnderecoFuncionario.setText("");
 		txtNumeroEnderecoFuncionario.setText("");
 		txtComplementoFuncionario.setText("");
 		ftCepFuncionario.setText("");
 		ftTelefoneResidencialFuncionario.setText("");
 		ftTelefoneCelularFuncionario.setText("");
 		txtLoginFuncionario.setText("");
 		txtSiteFuncionario.setText("");
 		txtEmailFuncionario.setText("");
 		txtLoginFuncionario.setText("");
 		pfSenhaFuncionario.setText("");
 		pfConfSenhaFuncionario.setText("");
 		
 		imagePanel.setImagem(null);
 		imagePanel.repaint();
 		imagePanel.revalidate();
 	}
 	
 	private String getCargo(JRadioButton[] bts){
 		if(bts[1].isSelected()){
 			return "S";
 		}else if(bts[2].isSelected()){
 			return "G";
 		}else if(bts[3].isSelected()){
 			return "C";
 		}else{
 			return "CI";
 		}
 		
 	}
 	
 	public Funcionario getBean() throws EntradaUsuarioException, ParseException {
 		JRadioButton[] rbs = {rbCargoCorretorFuncionario, rbCargoSecretariaFuncionario, rbCargoGerenteFuncionario, rbCargoConsultorFuncionario};
 		Funcionario f = new Funcionario();
 		
 		f.setNome(TelaUtil.getCampoObrigatorio(txtNomeFuncionario, true));
 		f.setSexo(TelaUtil.getCharSexo(rbMasculinoFuncionario));
 		f.setCpf(TelaUtil.getCpf(ftCpfFuncionario, true));
 		f.setRg(TelaUtil.getCampoObrigatorio(ftRgFuncionario, true));
 		f.setEstadoCivil(cbEstadoCivilFuncionario.getSelectedItem().toString().substring(0, 1));//pega somente o primeiro caractere do item selecionado
 		f.setNacionalidade(txtNacionalidadeFuncionario.getText());
 		f.setNumeroEndereco(TelaUtil.getCampoObrigatorio(txtNumeroEnderecoFuncionario, true));
 		f.setComplementoEndereco(txtComplementoFuncionario.getText());
 		f.setCep(TelaUtil.getCep(ftCepFuncionario, true));
 		f.setTelefoneResidencial(TelaUtil.getTelefone(ftTelefoneResidencialFuncionario, false));
 		f.setTelefoneCelular(TelaUtil.getCelular(ftTelefoneCelularFuncionario, true));
 		f.setCargoFuncionario(getCargo(rbs));
 		f.setImgFunc(foto);
 		f.setLoginFuncionario(TelaUtil.getCampoObrigatorio(txtLoginFuncionario, true));
 		f.setSenhaFuncionario(TelaUtil.getCompararSenhas(pfSenhaFuncionario, pfConfSenhaFuncionario));
 		f.setEmailPessoal(TelaUtil.getEmail(txtEmailFuncionario));
 		f.setSiteFuncionario(txtSiteFuncionario.getText());
 
 		System.out.println();
 		
 		return f;
 	}
 	
 	private void escolherFoto() throws EntradaUsuarioException {
 		JTextField foto = new JTextField();
 		
 		this.foto = TelaUtil.showTelaEscolheImage(new File("C:\\"), this.imagePanel, foto, this, "jpg", "gif", "png");
 		
 	}
 	
 	private void buscarEndereco() {
 		try{
 			String cep = TelaUtil.getCep(ftCepFuncionario, false);
 			Endereco end = new EnderecoDAO().buscarPorCep(cep);
 			if(end != null){
				txtEnderecoFuncionario.setText(end.getEndereco() + " - " + end.getBairro() + ", " + end.getCidade() + "/" + end.getUf());
 				txtNumeroEnderecoFuncionario.requestFocus();
 			}else{
 				JOptionPane.showMessageDialog(this, "O CEP Digitado  invalido!");
 				ftCepFuncionario.setText("");
 				txtNumeroEnderecoFuncionario.requestFocus();
 			}
 		}catch(DAOException e){
 			e.printStackTrace();
 		} catch (EntradaUsuarioException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private void salvarCadastro(){
 		try{
 			Funcionario bean = getBean();
 			new FuncionarioDAO().inserirFuncionario(bean);
 			JOptionPane.showMessageDialog(this, "Cadastro efetuado com sucesso!", "Sucesso", JOptionPane.INFORMATION_MESSAGE);
 			limpaFormulario();
 		}catch(DAOException e){
 			e.printStackTrace();
 		} catch (EntradaUsuarioException e) {
 			e.printStackTrace();
 		} catch (ParseException e) {
 			e.printStackTrace();
 		}
 	}
 }
