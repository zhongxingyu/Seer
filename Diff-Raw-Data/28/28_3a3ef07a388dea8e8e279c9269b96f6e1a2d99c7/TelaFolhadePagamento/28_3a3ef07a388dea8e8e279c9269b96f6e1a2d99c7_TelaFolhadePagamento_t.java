 package br.com.telas;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 
 import javax.swing.JButton;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JLabel;
 import javax.swing.ImageIcon;
 import java.awt.Color;
 import javax.swing.border.TitledBorder;
 import java.awt.Font;
 import javax.swing.SwingConstants;
 import javax.swing.border.LineBorder;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumnModel;
 
 import java.awt.Button;
 import javax.swing.ListSelectionModel;
 
 import br.com.TableModel.TableCellRenderer;
 import br.com.bean.FolhaPagamento;
 import br.com.bean.Pedido;
 import br.com.dao.FolhadePagamentoDao;
 import br.com.dao.PedidoDao;
 import br.com.bean.Funcionario;
 import br.com.dao.FuncionarioDao;
 import br.com.exception.DaoException;
 
 import br.com.relatorio.FolhaControle;
 import br.com.util.CalcularData;
 import br.com.util.ValidacaoUtil;
 
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 
 import org.jdesktop.swingx.JXDatePicker;
 import org.jdesktop.swingx.JXSearchField;
 import javax.swing.JPopupMenu;
 import java.awt.Component;
 import java.awt.event.MouseAdapter;
 import javax.swing.JMenuItem;
 import org.joda.time.MonthDay;
 import org.joda.time.Months;
 
 public class TelaFolhadePagamento extends JFrame implements KeyListener {
     final JPanel lista = new JPanel();
     final JPanel formulario = new JPanel();
     final JPanel buttonPanel = new JPanel();
     private FolhadePagamentoDao folhaDao = new FolhadePagamentoDao();
     private FuncionarioDao funcDao = new FuncionarioDao();
     private PedidoDao pedDao = new PedidoDao();
     private Pedido objPed = new Pedido();
 	private static final long serialVersionUID = 1L;	
 
 	private JTable table;
 	private static JTextField textField;
 	private static JTextField textField_1;
 	private static JFormattedTextField textField_3;
 	private JTextField textField_5;
 	private static JTextField textField_9;
 	private static JTextField textField_2;
 	private static JTextField textField_4;
 	private JTextField textField_6;
 	
 	private JXDatePicker dateInicio;
 	private JXDatePicker dateFim;
 	
 	private Double salario, salarioTotal;
 	private Double bonus = 0.0;
 	private Double porCentCom = 0.0;
 	private Double total = 0.0;
 	private Double totalCom = 0.0;
 	private JXSearchField txtPesq;
 
 	public TelaFolhadePagamento() throws DaoException {
 		setResizable(false);
 		setIconImage(Toolkit.getDefaultToolkit().getImage(TelaFolhadePagamento.class.getResource("/br/com/images/logo_transp.png")));
 		setTitle("Folha de Pagamento");
 		int width = 800;
         int height = 600;
         Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
         int x = (screen.width-width)/2;
         int y = (screen.height-height)/3;
         setBounds(x,y,821,600);
         getContentPane().setLayout(null);
         
         final JPanel buttonPanel = new JPanel();
         buttonPanel.setBackground(UIManager.getColor("Button.background"));
         buttonPanel.setBounds(0, 0, 152, 562);
         getContentPane().add(buttonPanel);
         buttonPanel.setLayout(null);
         
         txtPesq = new JXSearchField();
         txtPesq.addKeyListener(this);
         txtPesq.setPrompt("Nome funcionrio");
         txtPesq.setToolTipText("Digite o nome do funcionrio para pesquisar");
         txtPesq.setBounds(10, 76, 132, 20);
         buttonPanel.add(txtPesq);
         txtPesq.setColumns(10);
         
         JLabel lblPesquisar = new JLabel("Pesquisar");
         lblPesquisar.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 13));
         lblPesquisar.setBounds(40, 58, 79, 14);
         buttonPanel.add(lblPesquisar);
                          
 
                          formulario.setBounds(152, 0, 632, 562);
                          getContentPane().add(formulario);
                          formulario.setLayout(null);
                          
                          JPanel panel = new JPanel();
                          panel.setBorder(new TitledBorder(new TitledBorder(new LineBorder(new Color(0, 0, 0)), "", TitledBorder.LEADING, TitledBorder.TOP, null, null), "Pagamento", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
                          panel.setLayout(null);
                          panel.setBounds(21, 33, 590, 290);
                          formulario.add(panel);
                          
                          JPopupMenu popupMenu = new JPopupMenu();
                          addPopup(panel, popupMenu);
                          
                          JMenuItem mntmPesquisarFuncionrio = new JMenuItem("Pesquisar Funcion\u00E1rio");
                          mntmPesquisarFuncionrio.setIcon(new ImageIcon(TelaFolhadePagamento.class.getResource("/br/com/images/search.png")));
                          mntmPesquisarFuncionrio.addActionListener(new ActionListener() {
                          	public void actionPerformed(ActionEvent e) {
                          		new ViewSelecionaFuncionario(1);
                          	}
                          });
                          popupMenu.add(mntmPesquisarFuncionrio);
                          
                          JLabel lblNome = new JLabel("Nome:");
                          lblNome.setHorizontalAlignment(SwingConstants.RIGHT);
                          lblNome.setFont(new Font("Arial Black", Font.PLAIN, 12));
                          lblNome.setBounds(0, 89, 70, 18);
                          panel.add(lblNome);
                          
                          
                          JLabel lblSalrio = new JLabel("Sal\u00E1rio:");
                          lblSalrio.setHorizontalAlignment(SwingConstants.RIGHT);
                          lblSalrio.setFont(new Font("Arial Black", Font.PLAIN, 12));
                          lblSalrio.setBounds(0, 171, 70, 18);
                          panel.add(lblSalrio);
                          
                          JLabel lblProfisso = new JLabel("Profiss\u00E3o:");
                          lblProfisso.setHorizontalAlignment(SwingConstants.RIGHT);
                          lblProfisso.setFont(new Font("Arial Black", Font.PLAIN, 12));
                          lblProfisso.setBounds(0, 129, 70, 18);
                          panel.add(lblProfisso);
                          
                          JLabel lbln = new JLabel("N\u00BA:");
                          lbln.setHorizontalAlignment(SwingConstants.RIGHT);
                          lbln.setFont(new Font("Arial Black", Font.PLAIN, 12));
                          lbln.setBounds(10, 45, 56, 18);
                          panel.add(lbln);
                          
                           
                           JButton btnSalvar = new JButton("");
                           btnSalvar.setIcon(new ImageIcon(TelaFolhadePagamento.class.getResource("/br/com/images/salvar.png")));
                           btnSalvar.setToolTipText("Salvar Alt+S");
                           btnSalvar.setMnemonic(KeyEvent.VK_S);                          
                           btnSalvar.setBounds(504, 244, 56, 33);
                           panel.add(btnSalvar);
                           
                           btnSalvar.addActionListener(new ActionListener() {					
 					@Override
 					public void actionPerformed(ActionEvent arg0) {
 					  					  
 					  if(validarFormulrio()){
 						FolhaPagamento obj = new FolhaPagamento();
 							
 							//obj.setSalarioFunc(Double.parseDouble(MascaraUtil.hideMascaraMoeda(textField_3)));
 							obj.setSalarioFunc(Double.parseDouble(textField_3.getText()));
 							obj.setComissaoFuncTotal(Double.parseDouble(textField_2.getText()));
 							//obj.setBonusFunc(Double.parseDouble(MascaraUtil.hideMascaraMoeda(textField_4)));
 							obj.setBonusFunc(Double.parseDouble(textField_4.getText()));
 						//	obj.setTotalFunc(Double.parseDouble(MascaraUtil.hideMascaraMoeda(textField_6))); 
 							obj.setTotalFunc(Double.parseDouble(textField_6.getText()));
 							obj.setNomeFunc(textField_9.getText());
 							obj.setNumFunc(Integer.parseInt(textField.getText()));
 							obj.setProfissaoFunc(textField_1.getText());
 							
 							SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");						
 							
 							try {
 								obj.setDataInicio(df.parse(dateInicio.getEditor().getText()));
 								obj.setDataFim(df.parse(dateFim.getEditor().getText())); 
 							} catch (ParseException e1) {
 								// TODO Auto-generated catch block
 								e1.printStackTrace();
 							} 
 							
 							
 							FolhadePagamentoDao objDAO = new FolhadePagamentoDao();
 							try {							
 								
 								if(textField_5.getText().equals("")){
 									objDAO.inserirPagamento(obj);
 									JOptionPane.showMessageDialog(formulario, "Dados salvos com sucesso!");	
 									limpaFormulario();
 								}else{
 									Integer matr = Integer.parseInt(textField_5.getText()); 
 									obj.setCodDep(matr);
 									objDAO.atualizarPagamento(obj);
 									JOptionPane.showMessageDialog(formulario, "Dados atualizados com sucesso!");									
 								}								
 								atualizaLista(table,"");
 							} catch (DaoException e) {
 								e.printStackTrace();
 							}
 					  }
 					  
 					}
 				});
                           
                           JButton btnLimpar = new JButton("");
                           btnLimpar.setIcon(new ImageIcon(TelaFolhadePagamento.class.getResource("/br/com/images/limpar.png")));
                           btnLimpar.setToolTipText("Limpar Alt+L");
                           btnLimpar.setMnemonic(KeyEvent.VK_L);
                           btnLimpar.setBounds(438, 244, 56, 33);
                           panel.add(btnLimpar);
                           btnLimpar.addActionListener(new ActionListener() {
 					
 					@Override
 					public void actionPerformed(ActionEvent e) {
 						limpaFormulario();
 						
 					}
 				});
                           
                           textField = new JTextField();
                           textField.setBounds(80, 45, 127, 20);
                           panel.add(textField);
                           textField.setColumns(10);
                           
                           textField_1 = new JTextField();
                           textField_1.setBounds(80, 129, 335, 20);
                           panel.add(textField_1);
                           textField_1.setColumns(10);
                           
                           //textField_3 = new JFormattedTextField(MascaraUtil.setMascara("R$####,##"));
                           textField_3 = new JFormattedTextField();
                         //  textField_3.setDocument(new Moeda());
                           textField_3.setBounds(80, 171, 70, 20);
                           panel.add(textField_3);
                           textField_3.setColumns(10);
                           
                           textField_5 = new JTextField();
                           textField_5.setVisible(false);
                           textField_5.setText("");
                           panel.add(textField_5);
                           
                           textField_9 = new JTextField();
                           textField_9.setBounds(80, 89, 335, 20);
                           panel.add(textField_9);
                           textField_9.setColumns(10);
                           
                           
                           dateInicio = new JXDatePicker();
                           dateInicio.getEditor().setToolTipText("Data nicial para calcular a comisso!");
                           dateInicio.getEditor();
                           dateInicio.setFormats(new String[] {"dd/MM/yyyy"});
                           dateInicio.setBounds(258, 171, 97, 20);
                           panel.add(dateInicio);
                   		
                   		dateFim = new JXDatePicker();
                   		dateFim.getEditor().setToolTipText("Data final para calcular a comisso!");
                   		dateFim.getEditor();
                   		dateFim.setFormats(new String[] {"dd/MM/yyyy"});
                   		dateFim.setBounds(392, 171, 97, 20); //
                   		panel.add(dateFim);
                           
                           JButton btnOk = new JButton("");
                           btnOk.setToolTipText("Procurar funcion\u00E1rio");
                           btnOk.setIcon(new ImageIcon(TelaFolhadePagamento.class.getResource("/br/com/images/pesquisar.png")));
                           btnOk.addActionListener(new ActionListener() {
                           	public void actionPerformed(ActionEvent e) {
                           		//PESQUISAR NO BANCO DE DADOS O NMERO DO FUNCIONRIO
                           		String aux1;
                           		aux1 = textField.getText();                                             		
                           	//	if(aux.contains("^[a-Z]")){ //mtodo para verificar se contm letras
                           		if(textField.getText().equals("")){
 	            					JOptionPane.showMessageDialog(null, "Digite um nmero!");
 	            				}else if(aux1.matches("^[0-9]*$")){
                           			chamaFuncionario(Integer.parseInt(textField.getText())); 
                           		}else{
                           			JOptionPane.showMessageDialog(null, "Digite apenas nmero!");
                           		}
                           		                         		
                           	}
                           });
                           btnOk.setBounds(217, 44, 56, 23);
                           panel.add(btnOk);
                           
                           JLabel lblComisso = new JLabel("Comiss\u00E3o:");
                           lblComisso.setFont(new Font("Arial Black", Font.PLAIN, 12));
                           lblComisso.setBounds(0, 212, 80, 14);
                           panel.add(lblComisso);
                           
                           JLabel lblBnus = new JLabel("B\u00F4nus:");
                           lblBnus.setFont(new Font("Arial Black", Font.PLAIN, 12));
                           lblBnus.setBounds(185, 212, 50, 14);
                           panel.add(lblBnus);
                           
                           JLabel lblTotal = new JLabel("Total:");
                           lblTotal.setFont(new Font("Arial Black", Font.PLAIN, 12));
                           lblTotal.setBounds(24, 257, 46, 14);
                           panel.add(lblTotal);
                           
                           textField_2 = new JTextField();                          
                           textField_2.setToolTipText("Comiss\u00E3o calculada");
                           textField_2.setBounds(80, 210, 70, 20);
                        //   textField_2.setDocument(new Moeda());
                           panel.add(textField_2);
                           textField_2.setColumns(10);
                           
                          // textField_4 = new JFormattedTextField(MascaraUtil.setMascara("R$####,##"));
                           textField_4 = new JTextField();
                     //      textField_4.setDocument(new Moeda()); 
                           textField_4.setBounds(245, 210, 80, 20);
                           panel.add(textField_4);
                           textField_4.setColumns(10);
                           
                         //  textField_6 = new JFormattedTextField(MascaraUtil.setMascara("R$####,##"));
                           textField_6 = new JTextField();                          
                      //     textField_6.setDocument(new Moeda()); 
                           textField_6.setBounds(80, 255, 70, 20);
                           panel.add(textField_6);
                           textField_6.setColumns(10);
                           
                           JButton btnCalcular = new JButton("Somar tudo");
                           btnCalcular.addActionListener(new ActionListener() {
                           	public void actionPerformed(ActionEvent e) {
                           		//Calcular quanto o funcionrio ir receber
                           	//	bonus = 0.0;
                           		//textField 3(salario) e 4 (bonus) 2 (comisso)
                           		if(validarFormulrioCalculo()){
                           			
                         			MonthDay inicio = new MonthDay(dateInicio.getDate());
                               		MonthDay fim = new MonthDay(dateFim.getDate());
                               		int mes = Months.monthsBetween(inicio, fim).getMonths();
                              		if(mes >= 2){
                              			salario = Double.parseDouble(textField_3.getText()) * mes;  
                              		}
                              		else{
                               		
                           		//		String aux = textField_4.getText().replace(",", ".").trim();
                           			//	bonus = Double.parseDouble(aux);
                           			bonus = Double.parseDouble(textField_4.getText());
                           				
                                  		salario = Double.parseDouble(textField_3.getText());  
                                   		totalCom = Double.parseDouble(textField_2.getText());
                                   		
                                   		salarioTotal = totalCom + bonus + salario;
                                   		
                                   		textField_6.setText(String.valueOf(salarioTotal));
                                   		textField_6.setEditable(false);
                              		}
                                   		
                           		}                          	
                           		
                           		
                           	}
                           });
                           btnCalcular.setBounds(343, 209, 89, 23);
                           panel.add(btnCalcular);
                           
                           JLabel lblIncioDoMs = new JLabel("In\u00EDcio do m\u00EAs:");
                           lblIncioDoMs.setFont(new Font("Arial Black", Font.PLAIN, 11));
                           lblIncioDoMs.setBounds(160, 174, 95, 14);
                           panel.add(lblIncioDoMs);
                           
                           JLabel lblAt = new JLabel("at\u00E9");
                           lblAt.setFont(new Font("Arial Black", Font.PLAIN, 11));
                           lblAt.setBounds(365, 173, 28, 14);
                           panel.add(lblAt);
                           
                           JButton btnCalcular_Com = new JButton("Calcular");
                           btnCalcular_Com.addActionListener(new ActionListener() {
                           	public void actionPerformed(ActionEvent e) {
                           		try {
 									calcularComissao();
 								} catch (ParseException e1) {
 									// TODO Auto-generated catch block
 									e1.printStackTrace();
 								}
                           	}
                           });
                           btnCalcular_Com.setToolTipText("Calcular comiss\u00E3o");
                           btnCalcular_Com.setBounds(491, 170, 89, 23);
                           panel.add(btnCalcular_Com);
                           
                           JLabel lblTodosOsCampos = new JLabel("Todos os campos s\u00E3o obrigat\u00F3rios!");
                           lblTodosOsCampos.setForeground(Color.RED);
                           lblTodosOsCampos.setFont(new Font("Tahoma", Font.PLAIN, 12));
                           lblTodosOsCampos.setBounds(201, 263, 192, 14);
                           panel.add(lblTodosOsCampos);
                           
                           
                           JButton btnVoltar = new JButton("");
                           btnVoltar.setIcon(new ImageIcon(TelaFolhadePagamento.class.getResource("/br/com/images/voltar.png")));
                           btnVoltar.setToolTipText("Voltar");
                           btnVoltar.setBounds(21, 340, 89, 23);
                           formulario.add(btnVoltar);
                           formulario.setVisible(false);
                           
                           btnVoltar.addActionListener(new ActionListener() {
                           	public void actionPerformed(ActionEvent arg0) {
                           		lista.setVisible(true);
                           		buttonPanel.setVisible(true);
                           		formulario.setVisible(false);       	
                           	}
                           });
                          
 
                          lista.setBounds(152, 0, 656, 562);
                          getContentPane().add(lista);
                          lista.setLayout(null);
                          
                          JLabel lblFuncionriosCadastrados = new JLabel("Pagamentos Efetuados");
                          lblFuncionriosCadastrados.setFont(new Font("Kalinga", Font.BOLD, 16));
                          lblFuncionriosCadastrados.setHorizontalAlignment(SwingConstants.CENTER);
                          lblFuncionriosCadastrados.setBackground(Color.WHITE);
                          lblFuncionriosCadastrados.setBounds(10, 11, 612, 29);
                          lista.add(lblFuncionriosCadastrados);
                          
 
 
                            
                            Button Novo = new Button("Adicionar");          
                            Novo.setBounds(10, 530, 70, 22);
                            lista.add(Novo);                                    
                            lista.setVisible(true);  
                            buttonPanel.setVisible(true);
                            table = new JTable();
                            table.addMouseListener(new MouseListener() {
 			
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				int linha = table.getSelectedRow();
 				int coluna = table.getSelectedColumn();
 				String matricula = (String) table.getValueAt(linha,0);
 				Integer mat = Integer.parseInt(matricula); 
 				if(coluna == 7){
 					int opcao;
 					opcao = JOptionPane.showConfirmDialog(null,"Deseja excluir o registro de matricula: "+matricula ,"Cuidado!!",JOptionPane.YES_NO_OPTION);				
 					   if(opcao == JOptionPane.YES_OPTION){  
 						   try {
 							folhaDao.excluirPagamento(mat);
 							atualizaLista(table,"");
 						} catch (DaoException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 							JOptionPane.showMessageDialog(null, "Dados excluidos com sucesso!");
 					   }
 				}
 				if (coluna == 5){
 					FolhaPagamento objFolha = new FolhaPagamento();
 
 					try {
 						objFolha = folhaDao.consultarPagamentoID(mat);
 						atualizaFormulario(objFolha);
 						buttonPanel.setVisible(false);
 					} catch (DaoException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				//Gera a folha pra imprimir
 				if (coluna == 6){
 					FolhaControle controle = new FolhaControle();
 					Funcionario objFunc = new Funcionario();
 					FolhaPagamento objFolha = new FolhaPagamento();
 					
 					Integer qntd;
 					
 					String nome = (String) table.getValueAt(linha,1);
                      try {                    	 
                     	 objFunc = funcDao.procurarFuncionarioNome(nome);
                     	 objFolha = folhaDao.procurarDataPag(mat);
                     	 qntd = folhaDao.quantidadePedido(objFunc.getNumFunc());
                     	 if(qntd == 0){
                     		 controle.gerarRelatorioFolhaSimples(objFunc.getNumFunc(), objFolha.getDataInicio(), objFolha.getDataFim());
                     	 }else
          				controle.gerarRelatorioFolha(objFunc.getNumFunc(), objFolha.getDataInicio(), objFolha.getDataFim());
          			} catch (DaoException e1) {
          				// TODO Auto-generated catch block
          				e1.printStackTrace();
          			}
 				}
 				
 			}
 		});
                            
                            
                            
                            table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
                            table.setModel(new DefaultTableModel(
         		  new Object[][] {
                                    	},
                                    	new String[] {
                                    		"N", "Nome", "Profisso", "Salrio", "Data Pagamento", "Editar","Relatrio", "Excluir"
                                    	}
                                    	
                                    )
                                    { 
 					private static final long serialVersionUID = 1L;
 
 					@Override 
                                  	  public boolean isCellEditable(int row, int col) 
                                  	  { 
                                  	  return false; 
                                  	  } 
                                  	  }
                                    );
                            table.getColumnModel().getColumn(0).setPreferredWidth(35);
                            table.getColumnModel().getColumn(0).setMinWidth(35);
                            table.getColumnModel().getColumn(1).setPreferredWidth(170);
                            table.getColumnModel().getColumn(1).setMinWidth(170);
                            table.getColumnModel().getColumn(2).setPreferredWidth(80);
                            table.getColumnModel().getColumn(2).setMinWidth(80);
                            table.getColumnModel().getColumn(3).setPreferredWidth(50);
                            table.getColumnModel().getColumn(3).setMinWidth(50);
                            
                            table.getColumnModel().getColumn(4).setPreferredWidth(100);
                            table.getColumnModel().getColumn(4).setMinWidth(100);
                            table.getColumnModel().getColumn(5).setPreferredWidth(40);
                            table.getColumnModel().getColumn(5).setMinWidth(40);
                            table.getColumnModel().getColumn(6).setPreferredWidth(50);
                            table.getColumnModel().getColumn(6).setMinWidth(50);
 
                            table.getColumnModel().getColumn(7).setPreferredWidth(40);
                            table.getColumnModel().getColumn(7).setMinWidth(40);
                            table.setBounds(39, 175, 530, 232);
                            atualizaLista(table,"");
                            
                            
                            JScrollPane scrollPane = new JScrollPane();
                            scrollPane.setBounds(10, 51, 636, 473);
                            lista.add(scrollPane);
                            
                                           scrollPane.setViewportView(table);
                                           
                                                   Novo.addActionListener(new ActionListener() {
                                                   	public void actionPerformed(ActionEvent arg0) {
                                                   		lista.setVisible(false);
                                                   		formulario.setVisible(true);
                                                   		buttonPanel.setVisible(false);
                                                   		limpaFormulario();
                                                   		try {
                                                             			atualizaLista(table,"");
                                                             		} catch (DaoException e) {
                                                             			// TODO Auto-generated catch block
                                                             			e.printStackTrace();
                                                             		}
                                                   	}
                                                   });	
 	}
 	
 	public void atualizaLista(JTable lista, String nome) throws DaoException{
 		
 		DefaultTableModel dtm = (DefaultTableModel) table.getModel();
 
         ImageIcon editar = new ImageIcon(CadFuncionario.class.getResource("/br/com/images/editar.png"));  
         ImageIcon excluir = new ImageIcon(CadFuncionario.class.getResource("/br/com/images/icon_excluir.png"));
 
         ImageIcon relatorio = new ImageIcon(CadFuncionario.class.getResource("/br/com/images/repo.png")); //mudar icon
 
         
 		TableColumnModel columnModel = table.getColumnModel();
 		
 		JTableRenderer renderer = new JTableRenderer();
 		JTableRenderer renderer1 = new JTableRenderer();
 		JTableRenderer renderer2 = new JTableRenderer();
 		
 		renderer.setValue(editar);
 		renderer.setHorizontalAlignment(JLabel.CENTER);
 		columnModel.getColumn(5).setCellRenderer(renderer);
 		
 		renderer1.setValue(excluir);
 		renderer1.setHorizontalAlignment(JLabel.CENTER);
 		columnModel.getColumn(7).setCellRenderer(renderer1);
 	
 		renderer2.setValue(relatorio);
 		renderer2.setHorizontalAlignment(JLabel.CENTER);
 		columnModel.getColumn(6).setCellRenderer(renderer2);
 
         dtm.setRowCount(0); 
 		List<FolhaPagamento> listaFolha  = new ArrayList<FolhaPagamento>();
 		listaFolha = folhaDao.consultarPagamento(nome);
  		String dados[] = new String[5]; 
 		for (FolhaPagamento obj : listaFolha) {
 			dados[0] = String.valueOf(obj.getCodDep());
 			dados[1] = obj.getNomeFunc();
 			dados[2] = obj.getProfissaoFunc();
 			dados[3] = String.valueOf(obj.getTotalFunc());
 			dados[4] = String.valueOf(obj.getDataInicio()) + " / " + String.valueOf(obj.getDataFim());
 			((DefaultTableModel) table.getModel()).addRow(dados); 
 		} 
 		table.setDefaultRenderer(Object.class, new TableCellRenderer());
 		table.repaint();
 	}
 
 	public class JTableRenderer extends DefaultTableCellRenderer {
 		private static final long serialVersionUID = 1L;
 
 		protected void setValue(Object value) {
 			if (value instanceof ImageIcon) {
 				if (value != null) {
 					ImageIcon d = (ImageIcon) value;
 					setIcon(d);
 				}
 			} else {
 				super.setValue(value);
 			}
 		}
 	}	
 
 	public void atualizaFormulario(FolhaPagamento objFolha){
 		textField_9.setText(objFolha.getNomeFunc());
 		textField.setText(objFolha.getNumFunc().toString());
 		textField_3.setText(objFolha.getSalarioFunc().toString());
 		textField_1.setText(objFolha.getProfissaoFunc());
 		textField_2.setText(objFolha.getComissaoFuncTotal().toString());
 		textField_4.setText(objFolha.getBonusFunc().toString());
 		textField_6.setText(String.valueOf(objFolha.getTotalFunc()));
 
 		dateInicio.setDate(objFolha.getDataInicio());
 		dateFim.setDate(objFolha.getDataFim());
 		
 		Integer matr = objFolha.getNumFunc();
 		textField_5.setText(matr.toString());
 		
 		lista.setVisible(false);
 		formulario.setVisible(true);
 		buttonPanel.setVisible(false);
 	}	
 	
 	public void limpaFormulario(){
 		textField.setText("");
 		textField_1.setText("");
 		textField_3.setText("");
 		textField_5.setText("");
 		textField_9.setText("");
 		textField_2.setText("");
 		textField_4.setText("");
 		textField_6.setText("");
 		dateInicio.getEditor().setText("");
 		dateFim.getEditor().setText("");
 		
 	}
 	public static void chamaFuncionario(int idFunc) {
 		Funcionario objFunc = new Funcionario();
 
 		FuncionarioDao objDao = new FuncionarioDao();				
 		
 		try {
 			objFunc = objDao.procurarFuncionarioID(idFunc);
 		} catch (DaoException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}	
 		textField.setText(String.valueOf(idFunc));
 		textField_9.setText(objFunc.getNomeFunc());
 		textField_1.setText(objFunc.getProfissaoFunc());
 		
 		textField_3.setText(String.valueOf(objFunc.getSalarioFunc()));
 		textField_3.getText().replace(",", ".");  
 		textField_3.setEditable(false);
 		
 		//Gerar a comisso automaticamente
 		textField_2.setText(String.valueOf(objFunc.getComissaoFunc()));
 		textField_2.setEditable(false);
 		
 	}
 	
 	public void calcularComissao() throws ParseException{
 		SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
     	
 		try {
 			Date data1 = dateInicio.getDate();
 			Date data2 = dateFim.getDate();
 			if(CalcularData.TirarDiferenca(data1, data2) < 0){
 				JOptionPane.showMessageDialog(null,"As datas devem ser iguais ou a data final ser superior a data inicial!");
 			}else{
 				objPed = pedDao.comissaoTotalPedido(Integer.parseInt(textField.getText()), 
 					df.parse(dateInicio.getEditor().getText()), df.parse(dateFim.getEditor().getText()));
 				total = objPed.getTotalPedido();
 				porCentCom = Double.parseDouble(textField_2.getText());
 				totalCom = (total * porCentCom)/100 ;
 				JOptionPane.showMessageDialog(null,"Comisso calculada!"+ "\nR$ " + totalCom);	
 				textField_2.setText(String.valueOf(totalCom));
 			}
 		} catch (NumberFormatException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (DaoException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean validarFormulrio(){
 		boolean result = true;
 
 		if(!ValidacaoUtil.textFieldVazio(textField_9)){
 			JOptionPane.showMessageDialog(null, "Campo Nome Vazio!");
 			result = false;
 		}
 		if(!ValidacaoUtil.textFieldVazio(textField_1)){
 			JOptionPane.showMessageDialog(null, "Campo Profisso Vazio!");
 			result = false;
 		}
 		if(!ValidacaoUtil.textFieldVazio(textField_2)){
 			JOptionPane.showMessageDialog(null, "Campo Comisso Vazio!");
 			result = false;
 		}
 		if(!ValidacaoUtil.textFieldVazio(textField_3)){
 			JOptionPane.showMessageDialog(null, "Campo Salrio Vazio!");
 			result = false;
 		}
 		if(!ValidacaoUtil.textFieldVazio(textField_4)){
 //			JOptionPane.showMessageDialog(null, "Campo Bnus Vazio!");
 			textField_4.setText("0");
 			result = false;
 		}
 		if(!ValidacaoUtil.textFieldVazio(textField_6)){
 			JOptionPane.showMessageDialog(null, "Campo Total Vazio!");
 			result = false;
 		}
 		if(!ValidacaoUtil.textFieldVazio(textField)){
 			JOptionPane.showMessageDialog(null, "Campo N Vazio!");
 			result = false;
 		}
 		if(!ValidacaoUtil.textFieldVazio(dateInicio)){
 			JOptionPane.showMessageDialog(null, "Campo Data Inicial Vazio!");
 			result = false;
 		}
 		if(!ValidacaoUtil.textFieldVazio(dateFim)){
 			JOptionPane.showMessageDialog(null, "Campo Data Final Vazio!");
 			result = false;
 		}
 		
 		return result;
 	}	
 	
 	public boolean validarFormulrioCalculo(){
 		boolean result = true;
 
 		if(!ValidacaoUtil.textFieldVazio(textField_2)){
 			JOptionPane.showMessageDialog(null, "Campo Comisso Vazio!");
 			result = false;
 		}
 		if(!ValidacaoUtil.textFieldVazio(textField_3)){
 			JOptionPane.showMessageDialog(null, "Campo Salrio Vazio!");
 			result = false;
 		}
 		if(!ValidacaoUtil.textFieldVazio(textField_4)){
 			JOptionPane.showMessageDialog(null, "Campo Bnus Vazio!");
 //			textField_4.setText("0");
 			result = false;
 		}				
 				
 		return result;
 	}
 	
 	public boolean validarFormulrioGerar(){
 		boolean result = true;
 
 		if(!ValidacaoUtil.textFieldVazio(textField_2)){
 			JOptionPane.showMessageDialog(null, "Digite a Porcentagem!");
 			result = false;
 		}	
 				
 		return result;
 	}
 	
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void keyPressed(KeyEvent event) {
 		if(event.getSource() == txtPesq){
 			try {
 				atualizaLista(table, txtPesq.getText().toString());
 			} catch (DaoException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 		
 	
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 	private static void addPopup(Component component, final JPopupMenu popup) {
 		component.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				if (e.isPopupTrigger()) {
 					showMenu(e);
 				}
 			}
 			public void mouseReleased(MouseEvent e) {
 				if (e.isPopupTrigger()) {
 					showMenu(e);
 				}
 			}
 			private void showMenu(MouseEvent e) {
 				popup.show(e.getComponent(), e.getX(), e.getY());
 			}
 		});
 	}
 }
 
