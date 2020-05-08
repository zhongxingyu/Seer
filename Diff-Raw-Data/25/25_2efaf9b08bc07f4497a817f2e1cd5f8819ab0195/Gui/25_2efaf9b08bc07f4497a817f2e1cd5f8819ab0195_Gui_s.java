 package io.github.qingtian.freecall.gui;
 
 import io.github.qingtian.freecall.ConfigTool;
 import io.github.qingtian.freecall.Contacter;
 import io.github.qingtian.freecall.MethodInvokeTool;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 
 @SuppressWarnings("serial")
 public class Gui extends JFrame implements ActionListener,
 		ListSelectionListener {
 
 	private static final Log log = LogFactory.getLog(Gui.class);
 
 	private static final int WIDTH = 600;
 	private static final int HEIGHT = 400;
 	private static final int OFFSET = 100;
 
 	private Container container;
 	private JPanel p_main, p_top, p_top_item, p_call_btns;
 	private JScrollPane numbers_sp, log_sp;
 	private JList numbers_list;
 	private JLabel label_ecpaccount, label_caller, label_called;
 	private JTextField tf_ecpaccount, tf_caller, tf_called;
 	private JPanel p_bottom;
 	private JTextArea ta_log;
 	private JButton clear_bt;
 	private JButton call_bt, cancle_bt;
 	private JButton save_ecpaccount_bt, save_caller_bt;
 
 	private JButton add_bt, delete_bt;
 	private JPanel p_number, p_n_1;
 
 	private Contacter[] contacters;
 
 	public Gui() {
 		super("免费呼叫通 - 晴天");
 		setSize(WIDTH, HEIGHT);
 		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 		setLocation((int) (d.getWidth() - WIDTH - OFFSET),
 				(int) (d.getHeight() - HEIGHT) / 2);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		initComponent();
 		loadData();
 		registerListener();
 
 		setVisible(true);
 	}
 
 	private void loadData() {
 		// 加载数据
 		// 联系人
 		contacters = ConfigTool.readContactersFromFile();
 		numbers_list.setListData(contacters);
 		if (contacters.length == 0) {
 			delete_bt.setEnabled(false);
 		}
 		// ECP账号
 		String ecpaccount = ConfigTool.readEcpaccountFromFile();
 		if (StringUtils.isNotBlank(ecpaccount)) {
 			tf_ecpaccount.setText(ecpaccount);
 		}
 		// 主叫
 		String caller = ConfigTool.readCallerFromFile();
 		if (StringUtils.isNotBlank(caller)) {
 			tf_caller.setText(caller);
 		}
 	}
 
 	private void initComponent() {
 		container = getContentPane();
 		container.setLayout(new BorderLayout());
 
 		// 左侧
 		p_number = new JPanel();
 		p_number.setLayout(new BorderLayout());
 
 		p_n_1 = new JPanel();
 		add_bt = new JButton("添加联系人");
 		delete_bt = new JButton("删除联系人");
 		p_n_1.add(add_bt);
 		p_n_1.add(delete_bt);
 
 		numbers_list = new JList();
 		numbers_sp = new JScrollPane(numbers_list);
 
 		p_number.add(p_n_1, BorderLayout.NORTH);
 		p_number.add(numbers_sp, BorderLayout.CENTER);
 
 		// 右侧
 		p_main = new JPanel();
 		p_top = new JPanel();
 		p_top_item = new JPanel();
 		p_call_btns = new JPanel();
 
 		label_ecpaccount = new JLabel("ECP账号");
 		tf_ecpaccount = new JTextField(20);
 		save_ecpaccount_bt = new JButton("保存ECP账号");
 		label_caller = new JLabel("主叫");
 		tf_caller = new JTextField(20);
 		save_caller_bt = new JButton("保存主叫者");
 		label_called = new JLabel("被叫");
 		tf_called = new JTextField(20);
 
 		p_top.setBorder(BorderFactory.createTitledBorder("发起呼叫"));
 		p_top.setLayout(new BorderLayout());
 
 		p_top_item.setLayout(new GridLayout(3, 3));
 		p_top_item.add(label_ecpaccount);
 		p_top_item.add(tf_ecpaccount);
 		p_top_item.add(save_ecpaccount_bt);
 
 		p_top_item.add(label_caller);
 		p_top_item.add(tf_caller);
 		p_top_item.add(save_caller_bt);
 
 		p_top_item.add(label_called);
 		p_top_item.add(tf_called);
 		p_top_item.add(new JLabel(" "));
 
 		call_bt = new JButton("现在呼叫");
 		cancle_bt = new JButton("取消被叫者设置");
 		p_call_btns.setAlignmentX(LEFT_ALIGNMENT);
 		p_call_btns.add(call_bt);
 		p_call_btns.add(cancle_bt);
 
 		p_top.add(p_top_item, BorderLayout.CENTER);
 		p_top.add(p_call_btns, BorderLayout.SOUTH);
 
 		p_bottom = new JPanel();
 		p_bottom.setLayout(new BorderLayout());
 		ta_log = new JTextArea();
 		log_sp = new JScrollPane(ta_log);
 		clear_bt = new JButton("清空日志");
 		p_bottom.add(log_sp, BorderLayout.CENTER);
 		p_bottom.add(clear_bt, BorderLayout.SOUTH);
 
 		p_main.setLayout(new BorderLayout());
 		p_main.add(p_top, BorderLayout.NORTH);
 		p_main.add(p_bottom, BorderLayout.CENTER);
 
 		container.add(p_number, BorderLayout.WEST);
 		container.add(p_main, BorderLayout.CENTER);
 
 	}
 
 	private void registerListener() {
 		save_ecpaccount_bt.addActionListener(this);
 		save_caller_bt.addActionListener(this);
 		add_bt.addActionListener(this);
 		delete_bt.addActionListener(this);
 		call_bt.addActionListener(this);
 		cancle_bt.addActionListener(this);
 		clear_bt.addActionListener(this);
 		numbers_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		numbers_list.addListSelectionListener(this);
 	}
 
 	public void log(String content) {
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		String now = sdf.format(new Date());
 		ta_log.append(now + ": ");
 		ta_log.append(content);
 		ta_log.append(System.getProperty("line.separator"));
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		Object source = e.getSource();
 		if (source == call_bt) {
 
 			final String ecpaccount = tf_ecpaccount.getText().trim();
 			final String caller = tf_caller.getText().trim();
 			final String called = tf_called.getText().trim();
 			if (StringUtils.isBlank(ecpaccount)) {
 				log("ECP账号不能为空");
 				tf_ecpaccount.requestFocus();
 				return;
 			}
 			if (StringUtils.isBlank(caller)) {
 				log("主叫者不能为空");
 				tf_caller.requestFocus();
 				return;
 			}
 			if (StringUtils.isBlank(called)) {
 				log("被叫者不能为空");
 				tf_called.requestFocus();
 				return;
 			}
 			log("正在呼叫[" + called + "] ... ");
 
 			new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					try {
 						String[] rtn = MethodInvokeTool.getInstance().call(
 								ecpaccount, caller, called);
 						if (StringUtils.equals(rtn[0], "200")) {
 							String response = rtn[1];
 							Document doc = DocumentHelper.parseText(response);
 							Element root = doc.getRootElement();
 
 							// 响应节点名称
 							Element result = root.element("Body")
 									.element("extendServiceResponse")
 									.element("return");
 
							if (StringUtils.equals(result.getText(), "1100")) {
 								log("呼叫成功，请注意手机振铃");
 							} else {
								log("呼叫失败[" + result.getText() + "]");
 							}
 						} else {
 							log("请求失败[" + rtn[0] + "]");
 						}
 					} catch (Exception e1) {
 						log.error(e1);
 					}
 				}
 
 			}).start();
 
 		} else if (source == cancle_bt) {
 			log("取消呼叫被叫者设置");
 			tf_called.setText("");
 			tf_called.requestFocus();
 		} else if (source == add_bt) {
 			AddContacterDialog addContacterDialog = new AddContacterDialog(this);
 			addContacterDialog.setVisible(true);
 
 			if (contacters.length > 0) {
 				delete_bt.setEnabled(true);
 			}
 		} else if (source == delete_bt) {
 			int selected_index = numbers_list.getSelectedIndex();
			Contacter c = (Contacter)numbers_list.getSelectedValue();
 			if (selected_index != -1) {
 
				int rtn = JOptionPane.showConfirmDialog(Gui.this, "确认要删除联系人["+c.getName()+"]嘛？",
						"删除确认",JOptionPane.YES_NO_OPTION);
 				if (rtn == JOptionPane.YES_OPTION) {
 					contacters = (Contacter[]) ArrayUtils.remove(contacters,
 							selected_index);
 					ConfigTool.writeContacterIntoFile(contacters);
 					numbers_list.setListData(contacters);
 					log("删除联系人成功");
 					if (contacters.length == 0) {
 						delete_bt.setEnabled(false);
 					}
 				}
 			} else {
 				log("删除前请先选中联系人");
 			}
 
 		} else if (source == clear_bt) {
 			ta_log.setText("");
 		} else if (source == save_ecpaccount_bt) {
 			String ecpaccount = tf_ecpaccount.getText().trim();
 			if (StringUtils.isNotBlank(ecpaccount)) {
 				ConfigTool.writeEcpaccountIntoFile(ecpaccount);
 				log("保存ECP账号[" + ecpaccount + "]成功 ");
 			} else {
 				String tip = "保存失败，原因：ECP账号不能为空";
 				log(tip);
 				JOptionPane.showMessageDialog(Gui.this, tip);
 				tf_ecpaccount.requestFocus();
 			}
 		} else if (source == save_caller_bt) {
 			String caller = tf_caller.getText().trim();
 			if (StringUtils.isNotBlank(caller)) {
 				ConfigTool.writeCallerIntoFile(caller);
 				log("保存主叫者[" + caller + "]成功 ");
 			} else {
 				String tip = "保存失败，原因：主叫者不能为空";
 				log(tip);
 				JOptionPane.showMessageDialog(Gui.this, tip);
 				tf_caller.requestFocus();
 			}
 		}
 	}
 
 	@Override
 	public void valueChanged(ListSelectionEvent e) {
 		Object source = e.getSource();
 		if (source == numbers_list) {
 			Contacter contacter = (Contacter) numbers_list.getSelectedValue();
 			if (contacter != null
 					&& StringUtils.isNotBlank(contacter.getNumber()))
 				tf_called.setText(contacter.getNumber());
 		}
 	}
 
 	public JList getNumbers_list() {
 		return numbers_list;
 	}
 
 	public void setNumbers_list(JList numbers_list) {
 		this.numbers_list = numbers_list;
 	}
 
 	public Contacter[] getContacters() {
 		return contacters;
 	}
 
 	public void setContacters(Contacter[] contacters) {
 		this.contacters = contacters;
 	}
 
 }
