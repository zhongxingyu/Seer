 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cn.fjy.hostmanager.views;
 
 import cn.fjy.hostmanager.host.HostFileSupport;
 import cn.fjy.hostmanager.plan.PlanService;
 import cn.fjy.hostmanager.pojo.Domain;
 import cn.fjy.hostmanager.pojo.Plan;
 import com.google.common.base.Strings;
 
 import javax.swing.*;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.List;
 import java.util.Vector;
 
 /**
  * @author GrayF
  */
 public class Lanucher extends JFrame {
 
     private static final long serialVersionUID = -6128217878264960565L;
 
     private JButton btnAdd;
     private JButton btnDel;
     private JButton btnSave;
     private JButton btnDelPlan;
     private JButton btnSwitchDNS;
     private JButton btnClearMapping;
     private JComboBox cboxPlanList;
     private JLabel labPlanName;
     private JLabel labNewPlanName;
     private JLabel labIP;
     private JLabel labDomain;
     private JScrollPane jScrollPane1;
     private JScrollPane jScrollPane4TextArea;
     private JTable jTable1;
     private JTextField planNameText;
     private JTextField ipText;
     private JTextField domainText;
 
     private TableModel tableModel;
 
     private JTabbedPane jTabbedPane1;
 
     private CBoxModel cBoxModel;
 
     private JTextArea hostFileContent;
 
     private PlanService service;
 
     /**
      * Creates new form Main
      */
     public Lanucher() {
         initComponents();
     }
 
     private void initComponents() {
 
         service = new PlanService();
 
         labPlanName = new JLabel();
         cboxPlanList = new JComboBox();
         labNewPlanName = new JLabel();
         planNameText = new JTextField();
         labIP = new JLabel();
         ipText = new JTextField();
         labDomain = new JLabel();
         domainText = new JTextField();
         btnAdd = new JButton();
         btnDel = new JButton();
         btnSave = new JButton();
         btnDelPlan = new JButton();
         btnSwitchDNS = new JButton();
         btnClearMapping = new JButton();
         jTabbedPane1 = new javax.swing.JTabbedPane();
         jScrollPane1 = new JScrollPane();
         jTable1 = new JTable();
         jScrollPane4TextArea = new JScrollPane();
         hostFileContent = new JTextArea();
 
         tableModel = new TableModel();
 
         cBoxModel = new CBoxModel();
 
         setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
 
         initComponentsTitle();
 
         initCombox();
 
         initButtonLinstener();
 
         jTable1.setModel(tableModel);
         jScrollPane1.setViewportView(jTable1);
         jScrollPane4TextArea.setViewportView(hostFileContent);
         jTabbedPane1.addTab("地址列表", jScrollPane1);
         jTabbedPane1.addTab("本地配置", jScrollPane4TextArea);
 
         service.initDatabase();
 
         new HostFileSupport().backup();
 
         initData();
 
         GroupLayout layout = new GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
                 layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                         .addGroup(layout.createSequentialGroup()
                                 .addContainerGap()
                                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                         .addComponent(jTabbedPane1, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                                         .addGroup(layout.createSequentialGroup()
                                                 .addComponent(labDomain)
                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                 .addComponent(domainText))
                                         .addGroup(layout.createSequentialGroup()
                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 50, 50)
                                                 .addComponent(btnAdd)
                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                 .addComponent(btnDel)
                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                 .addComponent(btnSave)
                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                 .addComponent(btnDelPlan))
                                         .addGroup(layout.createSequentialGroup()
                                                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                         .addComponent(labPlanName)
                                                         .addComponent(labIP, GroupLayout.Alignment.TRAILING))
                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                         .addGroup(layout.createSequentialGroup()
                                                                 .addComponent(cboxPlanList, GroupLayout.PREFERRED_SIZE, 113, GroupLayout.PREFERRED_SIZE)
                                                                 .addGap(24, 24, 24)
                                                                 .addComponent(labNewPlanName)
                                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                 .addComponent(planNameText, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE))
                                                         .addComponent(ipText))
                                                 .addGap(0, 0, Short.MAX_VALUE))
                                         .addGroup(layout.createSequentialGroup()
                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED, 120, 120)
                                                 .addComponent(btnSwitchDNS)
                                                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                 .addComponent(btnClearMapping)))
                                 .addContainerGap())
         );
         layout.setVerticalGroup(
                 layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                         .addGroup(layout.createSequentialGroup()
                                 .addContainerGap()
                                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                         .addComponent(labPlanName)
                                         .addComponent(cboxPlanList, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                         .addComponent(labNewPlanName)
                                         .addComponent(planNameText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                         .addComponent(labIP)
                                         .addComponent(ipText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                         .addComponent(labDomain)
                                         .addComponent(domainText, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                 .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                         .addComponent(btnDel)
                                         .addComponent(btnSave)
                                         .addComponent(btnAdd)
                                         .addComponent(btnDelPlan))
                                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jTabbedPane1, GroupLayout.PREFERRED_SIZE, 266, GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                 .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                         .addComponent(btnSwitchDNS)
                                         .addComponent(btnClearMapping))
                                 .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pack();
     }
 
     private void initComponentsTitle() {
         this.setTitle("Hosts Manager");
 
         labPlanName.setText("方案：");
 
         labNewPlanName.setText("新方案名：");
 
         labIP.setText("IP：");
 
         labDomain.setText("域名：");
 
         btnAdd.setText("添加");
 
         btnDel.setText("删除");
 
         btnSave.setText("保存方案");
 
         btnDelPlan.setText("删除方案");
 
         btnSwitchDNS.setText("切换DNS");
 
         btnClearMapping.setText("还原Hosts");
 
     }
 
     private void initCombox() {
         cboxPlanList.setModel(cBoxModel);
         cboxPlanList.addItemListener(new ItemListener() {
             public void itemStateChanged(ItemEvent evt) {
                 initTableData(cBoxModel.getSelectIndex());
             }
         });
     }
 
     private void initButtonLinstener() {
         btnAdd.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent evt) {
                 btnAddMouseClicked(evt);
             }
         });
 
         btnDel.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent evt) {
                 btnDelMouseClicked(evt);
             }
         });
 
         btnSave.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent evt) {
                 btnSaveMouseClicked(evt);
             }
         });
 
         btnDelPlan.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent evt) {
                 btnPlanDelMouseClicked(evt);
             }
         });
 
         btnSwitchDNS.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 btnSwitchDnsClicked();
             }
 
         });
 
         btnClearMapping.addMouseListener(new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 clearMapping();
             }
         });
     }
 
     private void clearMapping() {
         boolean result = new HostFileSupport().clearMapping();
         if (result) {
             JOptionPane.showMessageDialog(this, "Hosts文件已还原");
         }
     }
 
     private void btnSwitchDnsClicked() {
         Vector<Vector<Object>> rows = tableModel.getRows();
         if (rows == null || rows.size() < 1) {
             JOptionPane.showMessageDialog(this, "没有需要切换的DNS信息");
             return;
         }
         if (service.switchDNS(rows)) {
             JOptionPane.showMessageDialog(this, "DNS映射更换成功！");
             //btnSaveMouseClicked(null);
         } else {
             JOptionPane.showMessageDialog(this, "DNS映射更换失败！");
         }
     }
 
     // save plan
     private void btnSaveMouseClicked(MouseEvent evt) {
         String planName = planNameText.getText();
         Integer planId = null;
         if (Strings.isNullOrEmpty(planName)) {
             planName = cboxPlanList.getSelectedItem().toString();
             planId = cBoxModel.getSelectIndex();
         }
         if (tableModel.getRows() == null || tableModel.getRows().size() < 1) {
             JOptionPane.showMessageDialog(this, "列表为空，无法保存方案");
             return;
         }
         service.save(planId, planName, tableModel.getRows());
         planNameText.setText("");
         initTableData(planId);
     }
 
     // add
     private void btnAddMouseClicked(MouseEvent evt) {
         Vector<Object> row = new Vector<Object>();
         String ip = ipText.getText();
         String domain = domainText.getText();
         if (!Strings.isNullOrEmpty(ip) && !Strings.isNullOrEmpty(domain)) {
             row.add(0, ip);
             row.add(1, domain);
             tableModel.add(null, row);
             ipText.setText("");
             domainText.setText("");
             jTable1.updateUI();
         } else {
             JOptionPane.showMessageDialog(this, "IP、域名为空，无法保存！");
             return;
         }
     }
 
     // del row
     private void btnDelMouseClicked(MouseEvent evt) {
         int index = jTable1.getSelectedRow();
         if (index < 0) {
             JOptionPane.showMessageDialog(this, "请选择一条记录！");
             return;
         }
         tableModel.del(index);
         jTable1.updateUI();
     }
 
     // del plan
     private void btnPlanDelMouseClicked(MouseEvent evt) {
         Integer state = JOptionPane.showConfirmDialog(this, "确认删除？");
         if (state != 0) {
             return;
         }
         Integer planId = cBoxModel.getSelectIndex();
         if (planId != null) {
             service.del(planId);
             initData();
         }
     }
 
     private void initData() {
         Integer pId = null;
         List<Plan> planList = service.fingPlanList();
         if (planList != null && planList.size() > 0) {
             pId = planList.get(0).getId();
         }
         initTableData(pId);
         cBoxModel.initItems(planList);
         cboxPlanList.updateUI();
        HostFileSupport hostFileSupport = new HostFileSupport();
        hostFileContent.setText(hostFileSupport.getHostFileContent());
     }
 
     private void initTableData(Integer planId) {
         tableModel.initRows(null);
         if (planId != null) {
             List<Domain> domainList = service.findDomainList(planId);
             tableModel.initRows(domainList);
         }
         jTable1.updateUI();
     }
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[]) {
         /* Create and display the form */
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 new Lanucher().setVisible(true);
             }
         });
     }
 }
