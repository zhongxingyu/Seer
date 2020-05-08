 package FRAME;
 
 import RESTAURANTE.DAO.FornecedorDAO;
 import RESTAURANTE.DAO.GrupoItemDAO;
 import RESTAURANTE.DAO.IMPL.FornecedorDAOIMPL;
 import RESTAURANTE.DAO.IMPL.GrupoItemDAOIMPL;
 import RESTAURANTE.DAO.IMPL.ProdutoDAOIMPL;
 import RESTAURANTE.DAO.IMPL.SubGrupoItensDAOIMPL;
 import RESTAURANTE.DAO.IMPL.UnidadeMedidaDAOIMPL;
 import RESTAURANTE.DAO.ProdutoDAO;
 import RESTAURANTE.DAO.SubGrupoItemDAO;
 import RESTAURANTE.DAO.UTIL.ComboBoxGrupoItem;
 import RESTAURANTE.DAO.UTIL.ComboBoxUnidadeMedida;
 import RESTAURANTE.DAO.UTIL.ComboBoxSubGrupoItem;
 import RESTAURANTE.DAO.UnidadeMedidaDAO;
 import RESTAURANTE.MODEL.*;
 import java.util.List;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JOptionPane;
 import org.jdesktop.observablecollections.ObservableCollections;
 
 public class FrameCadastroProdutos extends javax.swing.JFrame {
 
     public FrameCadastroProdutos() {
         initComponents();
         setLocation(200, 100);
         novoProduto();
         produtoDao = new ProdutoDAOIMPL();
         fornecedorDao = new FornecedorDAOIMPL();
         grupoItemDao = new GrupoItemDAOIMPL();
         subGrupoItemDao = new SubGrupoItensDAOIMPL();
         unidadeMedidaDao = new UnidadeMedidaDAOIMPL();
         atualizaTabela();
         atualizaComponentes();
         setaCodigos();
         jtbProdutos.setAutoResizeMode(jtbProdutos.AUTO_RESIZE_OFF);
         jtbProdutos.getColumnModel().getColumn(0).setPreferredWidth(75);
         jtbProdutos.getColumnModel().getColumn(1).setPreferredWidth(690);
     }
 
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         bindingGroup = new org.jdesktop.beansbinding.BindingGroup();
 
         jTabbedPane1 = new javax.swing.JTabbedPane();
         jbtSalvar = new javax.swing.JButton();
         jbtSair = new javax.swing.JButton();
         jbtEditar = new javax.swing.JButton();
         jbtExcluir = new javax.swing.JButton();
         jbtNovo = new javax.swing.JButton();
         jLabel1 = new javax.swing.JLabel();
         jtbpProdutos = new javax.swing.JTabbedPane();
         jPanel4 = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         jtbProdutos = new javax.swing.JTable();
         jtfPesquisar = new javax.swing.JTextField();
         jbtDetalhar = new javax.swing.JButton();
         jbtPesquisar = new javax.swing.JButton();
         jPanel3 = new javax.swing.JPanel();
         jLabel2 = new javax.swing.JLabel();
         jtfCodigo = new javax.swing.JTextField();
         jLabel4 = new javax.swing.JLabel();
         jtfDescricaoProduto = new javax.swing.JTextField();
         jtfDescricaoProduto1 = new javax.swing.JTextField();
         jLabel5 = new javax.swing.JLabel();
         jcbGrupoItem = new javax.swing.JComboBox();
         jLabel6 = new javax.swing.JLabel();
         jLabel7 = new javax.swing.JLabel();
         jcbSubGrupo = new javax.swing.JComboBox();
         jLabel8 = new javax.swing.JLabel();
         jcbUnMedida = new javax.swing.JComboBox();
         jLabel3 = new javax.swing.JLabel();
         jtfPrecoCompra = new javax.swing.JTextField();
         jLabel9 = new javax.swing.JLabel();
         jtfPrecoVenda = new javax.swing.JTextField();
         jLabel10 = new javax.swing.JLabel();
         jtfFornecedor = new javax.swing.JTextField();
         jbtPesquisaFornecedor = new javax.swing.JButton();
         jLabel11 = new javax.swing.JLabel();
         jtfSaldoEstoque = new javax.swing.JTextField();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
         setTitle("Produtos");
         setMaximizedBounds(new java.awt.Rectangle(0, 0, 0, 0));
         setResizable(false);
 
         jbtSalvar.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jbtSalvar.setText("Salvar");
         jbtSalvar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jbtSalvarActionPerformed(evt);
             }
         });
 
         jbtSair.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jbtSair.setText("Sair");
         jbtSair.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jbtSairActionPerformed(evt);
             }
         });
 
         jbtEditar.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jbtEditar.setText("Alterar");
         jbtEditar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jbtEditarActionPerformed(evt);
             }
         });
 
         jbtExcluir.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jbtExcluir.setText("Excluir");
         jbtExcluir.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jbtExcluirActionPerformed(evt);
             }
         });
 
         jbtNovo.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jbtNovo.setText("Novo");
         jbtNovo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jbtNovoActionPerformed(evt);
             }
         });
 
         jLabel1.setFont(new java.awt.Font("Hiragino Sans GB", 0, 24)); // NOI18N
         jLabel1.setText("Produtos");
 
         jtbpProdutos.setBackground(new java.awt.Color(153, 153, 153));
         jtbpProdutos.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
         jtbpProdutos.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
         jtbpProdutos.addChangeListener(new javax.swing.event.ChangeListener() {
             public void stateChanged(javax.swing.event.ChangeEvent evt) {
                 jtbpProdutosStateChanged(evt);
             }
         });
 
         jPanel4.setBackground(new java.awt.Color(102, 102, 102));
 
         org.jdesktop.beansbinding.ELProperty eLProperty = org.jdesktop.beansbinding.ELProperty.create("${produtos}");
         org.jdesktop.swingbinding.JTableBinding jTableBinding = org.jdesktop.swingbinding.SwingBindings.createJTableBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, eLProperty, jtbProdutos);
         org.jdesktop.swingbinding.JTableBinding.ColumnBinding columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${codigo}"));
         columnBinding.setColumnName("Código");
         columnBinding.setColumnClass(Integer.class);
         columnBinding.setEditable(false);
         columnBinding = jTableBinding.addColumnBinding(org.jdesktop.beansbinding.ELProperty.create("${descricao}"));
         columnBinding.setColumnName("Descrição");
         columnBinding.setColumnClass(String.class);
         columnBinding.setEditable(false);
         bindingGroup.addBinding(jTableBinding);
         jTableBinding.bind();
         jtbProdutos.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jtbProdutosMouseClicked(evt);
             }
         });
         jScrollPane1.setViewportView(jtbProdutos);
 
         jtfPesquisar.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jtfPesquisar.setToolTipText("Campo de Pesquisa");
 
         jbtDetalhar.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jbtDetalhar.setText("Detalhar");
         jbtDetalhar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jbtDetalharActionPerformed(evt);
             }
         });
 
         jbtPesquisar.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jbtPesquisar.setText("Pesquisar");
         jbtPesquisar.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jbtPesquisarActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel4Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jPanel4Layout.createSequentialGroup()
                        .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 755, Short.MAX_VALUE)
                         .addContainerGap())
                     .add(jPanel4Layout.createSequentialGroup()
                         .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jPanel4Layout.createSequentialGroup()
                                 .add(jtfPesquisar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 148, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                 .add(jbtPesquisar))
                             .add(jbtDetalhar))
                         .add(0, 0, Short.MAX_VALUE))))
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jtfPesquisar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jbtPesquisar))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 284, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jbtDetalhar)
                 .addContainerGap())
         );
 
         jtbpProdutos.addTab("Produtos", jPanel4);
 
         jPanel3.setBackground(new java.awt.Color(153, 153, 153));
         jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "", javax.swing.border.TitledBorder.TRAILING, javax.swing.border.TitledBorder.ABOVE_TOP, new java.awt.Font("Lucida Grande", 0, 12))); // NOI18N
 
         jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jLabel2.setText("*Código");
 
         jtfCodigo.setEditable(false);
         jtfCodigo.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
 
         jLabel4.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jLabel4.setText("*Descrição");
 
         jtfDescricaoProduto.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
 
         org.jdesktop.beansbinding.Binding binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${produto.descricao}"), jtfDescricaoProduto, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         jtfDescricaoProduto1.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${produto.codigoFabrica}"), jtfDescricaoProduto1, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         jLabel5.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jLabel5.setText("*Código de fábrica");
 
         jcbGrupoItem.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         jcbGrupoItem.setRenderer(new ComboBoxGrupoItem());
         jcbGrupoItem.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jcbGrupoItemActionPerformed(evt);
             }
         });
 
         jLabel6.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jLabel6.setText("*Grupo");
 
         jLabel7.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jLabel7.setText("*Sub Grupo");
 
         jcbSubGrupo.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         jcbSubGrupo.setRenderer(new ComboBoxSubGrupoItem());
         jcbSubGrupo.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jcbSubGrupoActionPerformed(evt);
             }
         });
 
         jLabel8.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jLabel8.setText("*Unidade Medida");
 
         jcbUnMedida.setFont(new java.awt.Font("Tahoma", 0, 12)); // NOI18N
         jcbUnMedida.setRenderer(new ComboBoxUnidadeMedida());
         jcbUnMedida.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jcbUnMedidaActionPerformed(evt);
             }
         });
 
         jLabel3.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jLabel3.setText("Preço Compra");
 
         jtfPrecoCompra.setEditable(false);
         jtfPrecoCompra.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${produto.precoCompra}"), jtfPrecoCompra, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         jLabel9.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jLabel9.setText("*Preço Venda");
 
         jtfPrecoVenda.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${produto.precoVenda}"), jtfPrecoVenda, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         jLabel10.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jLabel10.setText("*Fornecedor");
 
         jtfFornecedor.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${produto.fornecedor.razaoSocial}"), jtfFornecedor, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         jbtPesquisaFornecedor.setText("Buscar");
         jbtPesquisaFornecedor.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 jbtPesquisaFornecedorActionPerformed(evt);
             }
         });
 
         jLabel11.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
         jLabel11.setText("Saldo em estoque");
 
         jtfSaldoEstoque.setEditable(false);
         jtfSaldoEstoque.setFont(new java.awt.Font("Lucida Grande", 0, 12)); // NOI18N
 
         binding = org.jdesktop.beansbinding.Bindings.createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, this, org.jdesktop.beansbinding.ELProperty.create("${produto.saldoEstoque}"), jtfSaldoEstoque, org.jdesktop.beansbinding.BeanProperty.create("text"));
         bindingGroup.addBinding(binding);
 
         org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                         .add(jLabel4)
                         .add(jPanel3Layout.createSequentialGroup()
                             .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                 .add(jtfCodigo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 109, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                 .add(jLabel2))
                             .add(18, 18, 18)
                             .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                 .add(jLabel5)
                                 .add(jtfDescricaoProduto1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 123, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                         .add(jtfDescricaoProduto)
                         .add(jLabel10)
                         .add(jPanel3Layout.createSequentialGroup()
                             .add(jbtPesquisaFornecedor)
                             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                             .add(jtfFornecedor))
                         .add(jPanel3Layout.createSequentialGroup()
                             .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                 .add(jtfPrecoCompra, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 109, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                 .add(jLabel3))
                             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                             .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                 .add(jtfPrecoVenda, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 109, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                 .add(jLabel9))
                             .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                             .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                                 .add(jLabel11)
                                 .add(jtfSaldoEstoque, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 103, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                     .add(jPanel3Layout.createSequentialGroup()
                         .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jPanel3Layout.createSequentialGroup()
                                 .add(jLabel6)
                                .add(0, 177, Short.MAX_VALUE))
                             .add(jcbGrupoItem, 0, 212, Short.MAX_VALUE))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jLabel7)
                             .add(jcbSubGrupo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 217, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jLabel8)
                             .add(jcbUnMedida, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 103, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                         .add(213, 213, 213))))
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel2)
                     .add(jLabel5))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jtfCodigo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jtfDescricaoProduto1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jLabel4)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jtfDescricaoProduto, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(jPanel3Layout.createSequentialGroup()
                         .add(jLabel6)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jcbGrupoItem, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(jPanel3Layout.createSequentialGroup()
                         .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(jLabel8)
                             .add(jLabel7))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(jcbUnMedida, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(jcbSubGrupo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jLabel10)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jbtPesquisaFornecedor)
                     .add(jtfFornecedor, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel11)
                     .add(jLabel3)
                     .add(jLabel9))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jtfSaldoEstoque, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jtfPrecoCompra, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jtfPrecoVenda, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(102, Short.MAX_VALUE))
         );
 
         jtbpProdutos.addTab("Cadastro", jPanel3);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jtbpProdutos)
                     .add(layout.createSequentialGroup()
                         .add(jbtNovo, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 103, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jbtSalvar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 103, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jbtEditar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 103, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jbtExcluir, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 103, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jbtSair, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 103, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(0, 0, Short.MAX_VALUE))
                     .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                         .add(0, 0, Short.MAX_VALUE)
                         .add(jLabel1)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jLabel1)
                 .add(18, 18, 18)
                 .add(jtbpProdutos, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 401, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(18, 18, 18)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jbtSalvar)
                     .add(jbtExcluir)
                     .add(jbtNovo)
                     .add(jbtEditar)
                     .add(jbtSair))
                 .addContainerGap())
         );
 
         jtbpProdutos.getAccessibleContext().setAccessibleName("Fornecedores");
         jtbpProdutos.getAccessibleContext().setAccessibleDescription("Fornecedores");
 
         bindingGroup.bind();
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void jbtNovoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtNovoActionPerformed
         jtbpProdutos.setSelectedIndex(1);
         novoProduto();
         atualizaComponentes();
         setaCodigos();
     }//GEN-LAST:event_jbtNovoActionPerformed
 
     private void jtbpProdutosStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jtbpProdutosStateChanged
         if (jtbpProdutos.getSelectedIndex() == 0) {
             jbtEditar.setVisible(false);
             jbtExcluir.setVisible(false);
             jbtSalvar.setVisible(false);
         } else {
             jbtSalvar.setVisible(true);
             jbtEditar.setVisible(true);
             jbtExcluir.setVisible(true);
             if (!(produto.getCodigoFabrica() == null)) {
                 jbtSalvar.setEnabled(false);
             }
         }
     }//GEN-LAST:event_jtbpProdutosStateChanged
 
     private void jbtSalvarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtSalvarActionPerformed
         try {
             if (produto.getDescricao() == null
                     || produto.getCodigoFabrica() == null
                     || produto.getUnidadeMedida() == null
                     || produto.getSubGrupoItens() == null
                     || produto.getFornecedor() == null
                     || produto.getPrecoVenda() == null
                     || produto.getUnidadeMedida() == null) {
                 JOptionPane.showMessageDialog(null, "Preencha todos os campos Necessários.");
             } else {
                 produtoDao.inserir(produto);
                 novoProduto();
                 atualizaTabela();
                 atualizaComponentes();
                 setaCodigos();
             }
         } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Preencha todos os campos Necessários.");
         }
     }//GEN-LAST:event_jbtSalvarActionPerformed
 
     private void jbtEditarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtEditarActionPerformed
         try {
             if (JOptionPane.showConfirmDialog(null,
                     "Deseja realmente fazer esta auteração ?",
                     "Atenção!", JOptionPane.YES_NO_OPTION) == 0) {
 
                 produto.setSubGrupoItens((SubGrupoItem) jcbSubGrupo.getSelectedItem());
                 produtoDao.alterar(produto);
                 atualizaTabela();
                  JOptionPane.showMessageDialog(rootPane, "Alterado com sucesso!");
             }
         } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Erro !\nMotivo: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
         }
     }//GEN-LAST:event_jbtEditarActionPerformed
 
     private void jbtExcluirActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtExcluirActionPerformed
         try {
             if (JOptionPane.showConfirmDialog(null,
                     "Deseja realmente excluir este Produto ?",
                     "Atenção!", JOptionPane.YES_NO_OPTION) == 0) {
                 produtoDao.remover(produto);
                 novoProduto();
                 atualizaTabela();
                 atualizaComponentes();
             }
         } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Erro !\nMotivo: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
         }
     }//GEN-LAST:event_jbtExcluirActionPerformed
 
     private void jbtSairActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtSairActionPerformed
         dispose();
     }//GEN-LAST:event_jbtSairActionPerformed
 
     private void jbtPesquisarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtPesquisarActionPerformed
         setProdutos(produtoDao.buscarPorDescricao(jtfPesquisar.getText()));
         if (produtoDao.buscarTodos().isEmpty()) {
         } else {
             jtbProdutos.addRowSelectionInterval(0, 0);
         }
     }//GEN-LAST:event_jbtPesquisarActionPerformed
 
     private void jbtDetalharActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtDetalharActionPerformed
         jcbSubGrupo.removeAllItems();
         subGrupoItens = null;
         setProduto(produtos.get(jtbProdutos.getSelectedRow()));
         jtbpProdutos.setSelectedIndex(1);
         setaGrupoJCBGrupoItem();
         setaSubGrupoJCBSubGrupoItem();
         setaUNJcbUnidadeMedida();
         jbtSalvar.setEnabled(false);
         jtfCodigo.setText(produto.getCodigo().toString());
         
     }//GEN-LAST:event_jbtDetalharActionPerformed
 
     private void jtbProdutosMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jtbProdutosMouseClicked
         if (evt.getClickCount() == 2) {
             jcbSubGrupo.removeAllItems();
             subGrupoItens = null;
             setProduto(produtos.get(jtbProdutos.getSelectedRow()));
             jtbpProdutos.setSelectedIndex(1);
             setaGrupoJCBGrupoItem();
             setaSubGrupoJCBSubGrupoItem();
             setaUNJcbUnidadeMedida();
             jbtSalvar.setEnabled(false);
             jtfCodigo.setText(produto.getCodigo().toString());
         }
     }//GEN-LAST:event_jtbProdutosMouseClicked
 
     private void jcbGrupoItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbGrupoItemActionPerformed
         atualizaComboBoxSubGrupoItem();
     }//GEN-LAST:event_jcbGrupoItemActionPerformed
 
     private void jbtPesquisaFornecedorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jbtPesquisaFornecedorActionPerformed
         buscaFornecedor();
     }//GEN-LAST:event_jbtPesquisaFornecedorActionPerformed
 
     private void jcbSubGrupoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbSubGrupoActionPerformed
         produto.setSubGrupoItens((SubGrupoItem) jcbSubGrupo.getSelectedItem());
     }//GEN-LAST:event_jcbSubGrupoActionPerformed
 
     private void jcbUnMedidaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jcbUnMedidaActionPerformed
         produto.setUnidadeMedida((UnidadeMedida) jcbUnMedida.getSelectedItem());
     }//GEN-LAST:event_jcbUnMedidaActionPerformed
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /*
          * Set the Nimbus look and feel
          */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /*
          * If Nimbus (introduced in Java SE 6) is not available, stay with the
          * default look and feel. For details see
          * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
          */
         try {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                 if ("Nimbus".equals(info.getName())) {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
                     break;
                 }
             }
         } catch (ClassNotFoundException ex) {
             java.util.logging.Logger.getLogger(FrameCadastroProdutos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex) {
             java.util.logging.Logger.getLogger(FrameCadastroProdutos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex) {
             java.util.logging.Logger.getLogger(FrameCadastroProdutos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex) {
             java.util.logging.Logger.getLogger(FrameCadastroProdutos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         /*
          * Create and display the form
          */
         java.awt.EventQueue.invokeLater(new Runnable() {
             @Override
             public void run() {
                 new FrameCadastroProdutos().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel10;
     private javax.swing.JLabel jLabel11;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JLabel jLabel6;
     private javax.swing.JLabel jLabel7;
     private javax.swing.JLabel jLabel8;
     private javax.swing.JLabel jLabel9;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JTabbedPane jTabbedPane1;
     private javax.swing.JButton jbtDetalhar;
     private javax.swing.JButton jbtEditar;
     private javax.swing.JButton jbtExcluir;
     private javax.swing.JButton jbtNovo;
     private javax.swing.JButton jbtPesquisaFornecedor;
     private javax.swing.JButton jbtPesquisar;
     private javax.swing.JButton jbtSair;
     private javax.swing.JButton jbtSalvar;
     private javax.swing.JComboBox jcbGrupoItem;
     private javax.swing.JComboBox jcbSubGrupo;
     private javax.swing.JComboBox jcbUnMedida;
     private javax.swing.JTable jtbProdutos;
     private javax.swing.JTabbedPane jtbpProdutos;
     private javax.swing.JTextField jtfCodigo;
     private javax.swing.JTextField jtfDescricaoProduto;
     private javax.swing.JTextField jtfDescricaoProduto1;
     private javax.swing.JTextField jtfFornecedor;
     private javax.swing.JTextField jtfPesquisar;
     private javax.swing.JTextField jtfPrecoCompra;
     private javax.swing.JTextField jtfPrecoVenda;
     private javax.swing.JTextField jtfSaldoEstoque;
     private org.jdesktop.beansbinding.BindingGroup bindingGroup;
     // End of variables declaration//GEN-END:variables
     //produto
     private Produto produto;
     private List<Produto> produtos;
     private ProdutoDAO produtoDao;
     //Fornecedor
     private Fornecedor fornecedor;
     private FornecedorDAO fornecedorDao;
     //Grupo
     private GrupoItem grupoItem;
     private GrupoItemDAO grupoItemDao;
     List<GrupoItem> grupoItens;
     //SubGrupo
     private SubGrupoItem subGrupoItem;
     private SubGrupoItemDAO subGrupoItemDao;
     List<SubGrupoItem> subGrupoItens;
     //UnidadeMedida
     private UnidadeMedidaDAO unidadeMedidaDao;
     List<UnidadeMedida> unidadeMedidas;
     //FramePesqusia
     FramePesquisaFornecedor frPesqFornecedor;
 
     public Produto getProduto() {
         return produto;
     }
 
     public void setProduto(Produto produto) {
         Produto produtoVelho = this.produto;
         this.produto = produto;
         firePropertyChange("produto", produtoVelho, this.produto);
     }
 
     public List<Produto> getProdutos() {
         return produtos;
     }
 
     public void setProdutos(List<Produto> produtos) {
         List<Produto> produtosVelhos = this.produtos;
         this.produtos = ObservableCollections.observableList(produtos);
         firePropertyChange("produtos", produtosVelhos, this.produtos);
     }
 
     public Fornecedor getFornecedor() {
         return fornecedor;
     }
 
     public void setFornecedor(Fornecedor fornecedor) {
         Fornecedor fornecedorVelho = this.fornecedor;
         this.fornecedor = fornecedor;
         firePropertyChange("fornecedor", fornecedorVelho, this.fornecedor);
     }
 
     private void atualizaTabela() {
         setProdutos(produtoDao.buscarTodos());
         if (produtoDao.buscarTodos().isEmpty()) {
         } else {
             jtbProdutos.addRowSelectionInterval(0, 0);
         }
     }
 
     private void atualizaComponentes() {
         atualizaComboBoxGrupoItem();
         atualizaComboBoxUnidadeMedida();
         atualizaComboBoxSubGrupoItem();
     }
 
     private void atualizaComboBoxUnidadeMedida() {
         jcbUnMedida.removeAllItems();
         unidadeMedidas = null;
         //if (unidadeMedidas != null) {
         //    unidadeMedidas.clear();
         //} else {
         unidadeMedidas = unidadeMedidaDao.buscarTodos();
         for (UnidadeMedida un : unidadeMedidas) {
             jcbUnMedida.addItem(un);
         }
         //jcbUnMedida.setSelectedIndex(0);
         // }
     }
 
     private void setaUNJcbUnidadeMedida() {
         jcbUnMedida.getModel().setSelectedItem(produto.getUnidadeMedida());
     }
 
     private void atualizaComboBoxGrupoItem() {
         jcbGrupoItem.removeAllItems();
         grupoItens = null;
         // if (grupoItens != null) {
         //     grupoItens.clear();
         // } else {
         grupoItens = grupoItemDao.buscarTodos();
         for (GrupoItem gpItem : grupoItens) {
             jcbGrupoItem.addItem(gpItem);
         }
         //      jcbGrupoItem.setSelectedIndex(0);
         // }
     }
 
     private void setaGrupoJCBGrupoItem() {
         jcbGrupoItem.getModel().setSelectedItem(produto.getSubGrupoItens().getGrupoItem());
     }
 
     private void atualizaComboBoxSubGrupoItem() {
         jcbSubGrupo.removeAllItems();
         subGrupoItens = null;
         // if (subGrupoItens != null) {
         //     subGrupoItens.clear();
         // } else {
         subGrupoItens = subGrupoItemDao.buscarPorGrupoItem(jcbGrupoItem.getSelectedIndex() + 1);
         for (SubGrupoItem subGrupo : subGrupoItens) {
             jcbSubGrupo.addItem(subGrupo);
         }
         //     jcbSubGrupo.setSelectedIndex(0);
         // }
     }
 
     private void setaSubGrupoJCBSubGrupoItem() {
         jcbSubGrupo.getModel().setSelectedItem(produto.getSubGrupoItens());
     }
 
     public void setCampoFornecedor() {
         jtfFornecedor.setText(produto.getFornecedor().getRazaoSocial());
     }
 
     private void novoProduto() {
         setProduto(new Produto(new UnidadeMedida(), new SubGrupoItem(new GrupoItem()), new Fornecedor()));
         jbtSalvar.setEnabled(true);
         
     }
 
     private void setaCodigos() {
         if (produtoDao.buscaIdMaio() == null) {
             produto.setCodigo(1);
         } else {
             produto.setCodigo(produtoDao.buscaIdMaio() + 1);
         }
 
         jtfCodigo.setText(produto.getCodigo().toString());
     }
 
     public void buscaFornecedor() {
         //cria a tela de busca como modal
         FramePesquisaFornecedor tela_busca = new FramePesquisaFornecedor();
         tela_busca.setModal(true);
         //exibe
         tela_busca.setVisible(true);
         //recupera os dados
         Fornecedor f = new Fornecedor();
         f = tela_busca.retornaFornecedor();
         this.produto.setFornecedor(f);
 
         if (produto.getFornecedor() != null) {
             jtfFornecedor.setText(produto.getFornecedor().getRazaoSocial());
         }
     }
      public void setacodigos (){
         jtfCodigo.setText(produto.getCodigo().toString());
             
      }
 }
