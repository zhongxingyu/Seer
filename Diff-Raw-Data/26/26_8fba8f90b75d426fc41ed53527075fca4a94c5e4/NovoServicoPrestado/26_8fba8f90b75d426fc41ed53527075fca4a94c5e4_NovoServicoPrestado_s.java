 package view;
 
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JTextField;
 
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import control.ServicoPrestadoController;
 import exception.ServicoException;
 import model.ServicoPrestado;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import javax.swing.JComboBox;
 import javax.swing.DefaultComboBoxModel;
 
 import dao.FactoryConnection;
 import java.awt.event.ItemListener;
 import java.awt.event.ItemEvent;
 
 @SuppressWarnings("serial")
 public class NovoServicoPrestado extends JFrame {
 
 	private JPanel contentPane;
 	private JTextField textValor;
 
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					NovoServicoPrestado frame = new NovoServicoPrestado();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public NovoServicoPrestado() {
 		setTitle("Criar nova presta\u00E7\u00E3o de servi\u00E7o");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 474, 214);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 
 		JLabel lblServico = new JLabel("Servi\u00E7o:");
 		lblServico.setBounds(27, 25, 46, 14);
 		contentPane.add(lblServico);
 
 		JLabel lblRealizadoPor = new JLabel("Realizado por:");
 		lblRealizadoPor.setBounds(27, 56, 92, 14);
 		contentPane.add(lblRealizadoPor);
 
 		JLabel lblPreco = new JLabel("Pre\u00E7o (R$):");
 		lblPreco.setBounds(27, 87, 71, 14);
 		contentPane.add(lblPreco);
 
 		textValor = new JTextField();
 		textValor.setColumns(10);
 		textValor.setBounds(129, 84, 114, 20);
 		contentPane.add(textValor);
 
 		final JComboBox comboBoxBarbeiro = new JComboBox();
 		comboBoxBarbeiro.setModel(new DefaultComboBoxModel(
 				new String[] { "Selecione um barbeiro" }));
 		comboBoxBarbeiro.setBounds(129, 53, 289, 20);
 		contentPane.add(comboBoxBarbeiro);
 
 		final JComboBox comboBoxServico = new JComboBox();
 		comboBoxServico.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent arg0) {
 				Connection connection;
 				if (comboBoxServico.getSelectedIndex() != 0) {
 
 					try {
 						connection = FactoryConnection.getInstance()
 								.getConnection();
 						java.sql.PreparedStatement pst1 = connection
 								.prepareStatement("SELECT preco FROM tipoServico WHERE "
 										+ " nome = \""
 										+ comboBoxServico.getSelectedItem()
												.toString().substring(4) + "\";");
 						ResultSet rs1 = pst1.executeQuery();
 						rs1.next();
 						textValor.setText(rs1.getString("preco"));
 					} catch (SQLException e) {
 						mostrarMensagemDeErro(e.getMessage());
 					}
 
 				}
 			}
 
 		});
 		comboBoxServico.setModel(new DefaultComboBoxModel(
 				new String[] { "Selecione um tipo de servi\u00E7o" }));
 		comboBoxServico.setMaximumRowCount(4);
 		comboBoxServico.setBounds(129, 22, 289, 20);
 		contentPane.add(comboBoxServico);
 		try {
 			int cont = 0;
 			Connection connection = FactoryConnection.getInstance()
 					.getConnection();
 			java.sql.PreparedStatement pst = connection
 					.prepareStatement("SELECT nome, cadeira FROM barbeiro ORDER BY cadeira;");
 			java.sql.PreparedStatement pst2 = connection
 					.prepareStatement("SELECT nome FROM tiposervico;");
 			ResultSet rs = pst.executeQuery();
 			ResultSet rs2 = pst2.executeQuery();
 
 			while (rs.next()) {
 				String nome = rs.getString("nome");
 				String cadeira = rs.getString("cadeira");
				comboBoxBarbeiro.addItem(cadeira+" - "+nome);
 			}
 			while (rs2.next()) {
				cont ++;
 				String nome = rs2.getString("nome");
				comboBoxServico.addItem(cont+" - "+nome);
 			}
 
 		} catch (SQLException e) {
 			mostrarMensagemDeErro(e.getMessage());
 		}
 
 		JButton botaoSalvar = new JButton("Salvar");
 		botaoSalvar.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				try {
 					String data;
 					ServicoPrestado servico_prestado = new ServicoPrestado();
					servico_prestado.setNomeBarbeiro(comboBoxBarbeiro
							.getSelectedItem().toString());
					servico_prestado.setNomeServico(comboBoxServico
							.getSelectedItem().toString());
 					servico_prestado.setPreco(textValor.getText());
 					data = servico_prestado.getData();
 					servico_prestado.setData(data);
 
 					if (comboBoxServico.getSelectedIndex() == 0)
 						JOptionPane.showMessageDialog(null,
 								"Voc deve selecionar um tipo de servio.");
 					else if (comboBoxBarbeiro.getSelectedIndex() == 0)
 						JOptionPane.showMessageDialog(null,
 								"Voc deve selecionar um barbeiro.");
 					else {
 						ServicoPrestadoController servicoController = ServicoPrestadoController
 								.getInstance();
 						servicoController.inserir(servico_prestado);
 
 						JOptionPane.showMessageDialog(null,
 								"Servico criado com sucesso");
 
 						comboBoxBarbeiro.setSelectedIndex(0);
 						comboBoxServico.setSelectedIndex(0);
 
 						textValor.setText("");
 					}
 				} catch (ServicoException e) {
 					mostrarMensagemDeErro(e.getMessage());
 				} catch (SQLException e) {
 					mostrarMensagemDeErro(e.getMessage());
 				}
 			}
 		});
 		botaoSalvar.setBounds(27, 129, 89, 23);
 		contentPane.add(botaoSalvar);
 
 		JButton botaoLimparCampos = new JButton("Limpar Campos");
 		botaoLimparCampos.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				textValor.setText("");
 				comboBoxBarbeiro.setSelectedIndex(0);
 				comboBoxServico.setSelectedIndex(0);
 			}
 		});
 		botaoLimparCampos.setBounds(152, 129, 148, 23);
 		contentPane.add(botaoLimparCampos);
 
 		JButton botaoVoltar = new JButton("Voltar");
 		botaoVoltar.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				dispose();
 				CadastrarServicoPrestado frame = new CadastrarServicoPrestado();
 				frame.setVisible(true);
 				frame.setLocationRelativeTo(null);
 			}
 		});
 		botaoVoltar.setBounds(329, 129, 89, 23);
 		contentPane.add(botaoVoltar);
 	}
 
 	private void mostrarMensagemDeErro(String informacao) {
 		JOptionPane.showMessageDialog(null, informacao, "Ateno",
 				JOptionPane.INFORMATION_MESSAGE);
 	}
 }
