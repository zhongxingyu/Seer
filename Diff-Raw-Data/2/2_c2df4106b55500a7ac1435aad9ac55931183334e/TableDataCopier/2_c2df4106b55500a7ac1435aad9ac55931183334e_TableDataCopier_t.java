 package com.googlecode.usc;
 
 import java.awt.Desktop;
 import java.awt.EventQueue;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Timer;
 
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.SwingConstants;
 import javax.swing.border.EmptyBorder;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.jdbc.core.simple.SimpleJdbcTemplate;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 import org.springframework.util.StopWatch;
 
 class TableDataCopier extends JFrame {
     private static final long serialVersionUID = 5840057159492381296L;
 
     private Logger logger = LoggerFactory.getLogger(getClass());
     private final String CONF_FILE_NAME = "config.properties";
     private final long PER_SECOND = 1000;
     private int BATCH_SIZE = 100;
     private boolean IS_OPEN_LOG_FILE = false;
     private boolean IS_DELETE_ORIGINAL_DATA = true;
     private boolean ABORT_WHEN_ABNORMAL_INSERT = false;
 
     private long DELAY_TIME = 1000;
     private boolean STOP = false;
     private static final String SNAPSHOT_FLAG = "-SNAPSHOT";
     private static final String LINE_FORMAT = "%04d";
 
     private JButton btnCopy;
     private JTextField textField_0_0;
     private JTextField textField_0_1;
     private JTextField textField_1_0;
     private JTextField textField_1_1;
     private JTextField textField_2_0;
     private JTextField textField_2_1;
     private JTextField textField_3_0;
     private JTextField textField_3_1;
     private JTextField textField_4_0;
     private JTextArea results;
 
     /**
      * Launch the application.
      */
     public static void main(String[] args) {
         EventQueue.invokeLater(new Runnable() {
             public void run() {
                 try {
                     TableDataCopier frame = new TableDataCopier();
                     frame.setVisible(true);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         });
     }
 
     /**
      * Create the frame.
      */
     public TableDataCopier() {
         setResizable(false);
         setTitle("TableDataCopier-ShunLi(QQ:506817493)");
         setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getClassLoader().getResource("dataCopier.png")));
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         double width = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
         double height = Toolkit.getDefaultToolkit().getScreenSize().getHeight();
         setBounds((int) ((width - 1024) / 2), (int) ((height - 648) / 4), 999, 609);// 128
                                                                                     // =
                                                                                     // (1280-1024)/2
         JPanel contentPane = new JPanel();
         contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
         setContentPane(contentPane);
 
         JLabel label = new JLabel("TableDataCopier");
         label.setFont(new Font("Microsoft YaHei", Font.PLAIN, 24));
 
         Properties prop = Utils.loadPropertiesFile(this.getClass().getClassLoader().getResourceAsStream("build-info.properties"));
         String version = prop.getProperty("version", "1.0");
 
         int snapshotIndex = version.lastIndexOf(SNAPSHOT_FLAG);
         if (snapshotIndex > 0) {
             version = version.substring(0, snapshotIndex);
         }
 
         JLabel lblv = new JLabel("ShunLi©V" + version);
         lblv.setEnabled(false);
         lblv.setFont(new Font("Microsoft YaHei", Font.PLAIN, 20));
         lblv.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 Desktop desktop = Desktop.getDesktop();
                 try {
                     desktop.browse(new URI("http://blogjava.net/lishunli"));
                 } catch (IOException e1) {
                     e1.printStackTrace();
                 } catch (URISyntaxException e1) {
                     e1.printStackTrace();
                 }
             }
         });
 
         JPanel panel = new JPanel();
         FlowLayout flowLayout_1 = (FlowLayout) panel.getLayout();
         flowLayout_1.setAlignment(FlowLayout.LEFT);
         panel.setFont(new Font("SimSun", Font.PLAIN, 12));
 
         JPanel panel_1 = new JPanel();
         FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
         flowLayout.setAlignment(FlowLayout.LEFT);
 
         JLabel lblResults = new JLabel("Results");
         lblResults.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
 
         JPanel panel_2 = new JPanel();
         FlowLayout flowLayout_5 = (FlowLayout) panel_2.getLayout();
         flowLayout_5.setAlignment(FlowLayout.LEFT);
 
         JPanel panel_3 = new JPanel();
         FlowLayout flowLayout_2 = (FlowLayout) panel_3.getLayout();
         flowLayout_2.setAlignment(FlowLayout.LEFT);
 
         JPanel panel_4 = new JPanel();
         FlowLayout flowLayout_3 = (FlowLayout) panel_4.getLayout();
         flowLayout_3.setAlignment(FlowLayout.LEFT);
 
         JPanel panel_5 = new JPanel();
         FlowLayout flowLayout_4 = (FlowLayout) panel_5.getLayout();
         flowLayout_4.setAlignment(FlowLayout.LEFT);
 
         JLabel lblUsername = new JLabel("Username");
         lblUsername.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
 
         JLabel lblPassword = new JLabel("Password");
         lblPassword.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
 
         JLabel lblTable = new JLabel("Criteria");
         lblTable.setHorizontalAlignment(SwingConstants.CENTER);
         lblTable.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
 
         JLabel lblUrl = new JLabel("Url");
         lblUrl.setHorizontalAlignment(SwingConstants.CENTER);
         lblUrl.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
 
         JScrollPane scrollPane = new JScrollPane();
 
         JLabel lblDriver = new JLabel("Driver   ");
         lblDriver.setHorizontalAlignment(SwingConstants.CENTER);
         lblDriver.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
         GroupLayout gl_contentPane = new GroupLayout(contentPane);
         gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING).addGroup(
                 gl_contentPane.createSequentialGroup().addContainerGap().addGroup(
                         gl_contentPane.createParallelGroup(Alignment.TRAILING).addGroup(gl_contentPane.createSequentialGroup().addComponent(label).addGap(372)).addGroup(gl_contentPane.createSequentialGroup().addComponent(lblv).addGap(43)).addGroup(
                                 gl_contentPane.createSequentialGroup().addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addComponent(lblDriver).addComponent(lblUrl).addComponent(lblUsername).addComponent(lblPassword).addComponent(lblTable).addComponent(lblResults)).addGap(10).addGroup(
                                         gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(
                                                 gl_contentPane.createSequentialGroup().addGroup(
                                                         gl_contentPane.createParallelGroup(Alignment.LEADING).addComponent(panel, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 851, Short.MAX_VALUE).addComponent(panel_1, GroupLayout.DEFAULT_SIZE, 851, Short.MAX_VALUE).addComponent(panel_2, GroupLayout.DEFAULT_SIZE,
                                                                 851, Short.MAX_VALUE).addComponent(panel_3, GroupLayout.DEFAULT_SIZE, 851, Short.MAX_VALUE).addComponent(panel_4, GroupLayout.DEFAULT_SIZE, 851, Short.MAX_VALUE).addComponent(panel_5, GroupLayout.DEFAULT_SIZE, 851, Short.MAX_VALUE)).addGap(26)).addGroup(
                                                 gl_contentPane.createSequentialGroup().addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 842, GroupLayout.PREFERRED_SIZE).addContainerGap()))))));
         gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(
                 gl_contentPane.createSequentialGroup().addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPane.createSequentialGroup().addContainerGap().addComponent(label)).addGroup(gl_contentPane.createSequentialGroup().addGap(44).addComponent(lblv))).addGap(18).addComponent(panel,
                         GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.UNRELATED).addGroup(
                         gl_contentPane.createParallelGroup(Alignment.TRAILING, false).addGroup(
                                 gl_contentPane.createSequentialGroup().addComponent(lblDriver, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE).addGap(1).addComponent(lblUrl, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE).addPreferredGap(ComponentPlacement.RELATED, 10, Short.MAX_VALUE)
                                         .addComponent(lblUsername, GroupLayout.PREFERRED_SIZE, 33, GroupLayout.PREFERRED_SIZE)).addGroup(
                                 gl_contentPane.createSequentialGroup().addComponent(panel_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE).addGap(1).addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE).addGap(1).addComponent(panel_3,
                                         GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE))).addGap(1).addGroup(
                         gl_contentPane.createParallelGroup(Alignment.LEADING).addComponent(lblPassword, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE).addComponent(panel_4, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE)).addGap(10).addGroup(
                         gl_contentPane.createParallelGroup(Alignment.TRAILING).addComponent(panel_5, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE).addComponent(lblTable, GroupLayout.PREFERRED_SIZE, 36, GroupLayout.PREFERRED_SIZE)).addPreferredGap(ComponentPlacement.UNRELATED).addGroup(
                         gl_contentPane.createParallelGroup(Alignment.BASELINE).addComponent(lblResults).addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)).addContainerGap(103, Short.MAX_VALUE)));
 
         results = new JTextArea();
         results.setLineWrap(true);
         results.setEditable(false);
         results.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
         results.setRows(9);
         scrollPane.setViewportView(results);
 
         textField_4_0 = new JTextField();
         textField_4_0.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
         textField_4_0.setColumns(49);
         panel_5.add(textField_4_0);
 
         textField_3_0 = new JTextField();
         textField_3_0.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
         textField_3_0.setColumns(25);
         panel_4.add(textField_3_0);
 
         JLabel label_4 = new JLabel("              ");
         label_4.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
         panel_4.add(label_4);
 
         textField_3_1 = new JTextField();
         textField_3_1.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
         textField_3_1.setColumns(25);
         panel_4.add(textField_3_1);
 
         textField_2_0 = new JTextField();
         textField_2_0.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
         textField_2_0.setColumns(25);
         panel_3.add(textField_2_0);
 
         JLabel label_2 = new JLabel("              ");
         label_2.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
         panel_3.add(label_2);
 
         textField_2_1 = new JTextField();
         textField_2_1.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
         textField_2_1.setColumns(25);
         panel_3.add(textField_2_1);
 
         textField_1_0 = new JTextField();
         textField_1_0.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
         textField_1_0.setColumns(25);
         panel_2.add(textField_1_0);
 
         JLabel label_1 = new JLabel("              ");
         label_1.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
         panel_2.add(label_1);
 
         textField_1_1 = new JTextField();
         textField_1_1.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
         textField_1_1.setColumns(25);
         panel_2.add(textField_1_1);
 
         textField_0_0 = new JTextField();
 
         textField_0_0.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
         panel_1.add(textField_0_0);
         textField_0_0.setColumns(25);
 
         JLabel label_3 = new JLabel("              ");
         label_3.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
         panel_1.add(label_3);
 
         textField_0_1 = new JTextField();
         textField_0_1.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
         textField_0_1.setColumns(25);
         panel_1.add(textField_0_1);
 
         List<JTextField> fields = Arrays.asList(textField_0_0, textField_0_1, textField_1_0, textField_1_1, textField_2_0, textField_2_1, textField_3_0, textField_3_1, textField_4_0);
 
         initData(fields);
         addCanCopyDataListener(fields);
 
         JLabel lblFrom = new JLabel("                                                From=======");
         lblFrom.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
         panel.add(lblFrom);
 
         btnCopy = new JButton("Copy");
         panel.add(btnCopy);
         btnCopy.addActionListener(new ActionListener() {
 
             public void actionPerformed(ActionEvent e) {
                 STOP = false;
                 new Thread() {
                     public void run() {
                         while (!STOP) {
                             buttonActionPerformed();
                         }
                     }
                 }.start();
 
             }
         });
         btnCopy.setToolTipText("copy table's data from one database to another database(same table schema)");
         btnCopy.setFont(new Font("Microsoft YaHei", Font.PLAIN, 16));
         canCopyData(fields);
 
         JLabel lblTo = new JLabel("=======>To");
         lblTo.setFont(new Font("Microsoft YaHei", Font.PLAIN, 18));
         panel.add(lblTo);
         contentPane.setLayout(gl_contentPane);
 
     }
 
     // Copy
     private void buttonActionPerformed() {
         copy();
     }
 
     @SuppressWarnings("unchecked")
     private void copy() {
         // clear
         results.setText("");
 
         String criteria = textField_4_0.getText();
         String tableName = null, deleteSql = null, countSql = null, selectSql = null;
 
        int indexOf = criteria.toUpperCase().lastIndexOf("FROM");
         if (indexOf < 0) { // not find "FROM" word. case insensitive
             tableName = criteria.trim().split("\\s", 2)[0];
             deleteSql = "Delete FROM " + tableName;
             countSql = "SELECT COUNT(1) FROM " + tableName;
             selectSql = "SELECT * FROM " + criteria;
         } else {// find "FROM" word
             tableName = criteria.substring(indexOf + 4).trim().split("\\s", 2)[0];
             deleteSql = "Delete FROM " + tableName;
             countSql = "SELECT COUNT(1) FROM " + tableName;
             selectSql = criteria;
         }
 
         int updateNums = 0;
         boolean hasException = false;
         Timer timer = new Timer();
         StopWatch stopWatch = new StopWatch();
 
         try {
             // From
             DriverManagerDataSource dataSourceFrom = new DriverManagerDataSource();
             dataSourceFrom.setDriverClassName(textField_0_0.getText());
             dataSourceFrom.setUrl(textField_1_0.getText());
             dataSourceFrom.setUsername(textField_2_0.getText());
             dataSourceFrom.setPassword(textField_3_0.getText());
 
             // To
             DriverManagerDataSource dataSourceTo = new DriverManagerDataSource();
             dataSourceTo.setDriverClassName(textField_0_1.getText());
             dataSourceTo.setUrl(textField_1_1.getText());
             dataSourceTo.setUsername(textField_2_1.getText());
             dataSourceTo.setPassword(textField_3_1.getText());
 
             SimpleJdbcTemplate jdbcTemplateFrom = new SimpleJdbcTemplate(dataSourceFrom);
             SimpleJdbcTemplate jdbcTemplateTo = new SimpleJdbcTemplate(dataSourceTo);
             HashMap<String, Object> paramMap = new HashMap<String, Object>();
 
             if (IS_DELETE_ORIGINAL_DATA) {
                 // clear temp table first
                 showResults("Delete Table ");
                 timer.schedule(new PrintTimerTask(), 0, PER_SECOND);
 
                 logger.info("Successfully delete sql is {}", deleteSql);
 
                 stopWatch.start("batch delete");
                 logger.info("Delete {} records", jdbcTemplateTo.update(deleteSql, paramMap));
                 stopWatch.stop();
 
                 showResults("\nSelect Table ");
             }
 
             // start count
             updateNums = jdbcTemplateTo.queryForInt(countSql);
 
             // select all
             if (!IS_DELETE_ORIGINAL_DATA) {
                 showResults("Select Table ");
                 timer.schedule(new PrintTimerTask(), 0, PER_SECOND);
             }
             logger.info("Select sql is {}", selectSql);
             stopWatch.start("batch select");
 
             List<Map<String, Object>> selectList = jdbcTemplateFrom.queryForList(selectSql, paramMap);
             int size = selectList.size();
             logger.info("Successfully select {} records", size);
             for (int i = 0; i < size; i++) {
                 logger.info("{} : {}", String.format(LINE_FORMAT, i + 1), selectList.get(i).toString());
             }
 
             stopWatch.stop();
 
             String egiOgInsertSql = null;
 
             showResults("\nInsert Table ");
             stopWatch.start("batch insert");
             for (int i = 0; i * BATCH_SIZE < size; i++) {
                 if (i == 0) {
                     egiOgInsertSql = Utils.buildInsertSql(selectList.get(0).keySet(), tableName);
                     logger.info("Insert sql is {}", egiOgInsertSql);
                 }
 
                 int fromIndex = i * BATCH_SIZE;
                 int toIndex = size - fromIndex > BATCH_SIZE ? fromIndex + BATCH_SIZE : size;
                 // String msg = "batch insert from " + String.format("%04d",
                 // fromIndex) + " to " + String.format("%04d", toIndex);
                 // showResultsln(msg);
 
                 List<Map<String, Object>> subEgiList = selectList.subList(fromIndex, toIndex);
 
                 try {
                     // batch insert
                     jdbcTemplateTo.batchUpdate(egiOgInsertSql, subEgiList.toArray(new Map[0]));
                 } catch (Exception e) {
                     hasException = true;
                     exceptionHandler(e);
 
                     if (ABORT_WHEN_ABNORMAL_INSERT) {
                         break;
                     }
                 }
             }
             stopWatch.stop();
 
             // end count
             updateNums = jdbcTemplateTo.queryForInt(countSql) - updateNums;
 
             showResults("\n----------------------Result----------------------\n");
             for (StopWatch.TaskInfo taskInfo : stopWatch.getTaskInfo()) {
                 showResults(taskInfo.getTaskName() + ",escaped time " + taskInfo.getTimeMillis() + " ms\n");
             }
             showResults("batch all escaped time " + stopWatch.getTotalTimeSeconds() + " s");
 
         } catch (Exception e) {
             hasException = true;
             exceptionHandler(e);
         } finally {
             timer.cancel();
             if (stopWatch.isRunning()) {
                 stopWatch.stop();
             }
             STOP = true;
             logger.info("Successfully insert {} records", updateNums);
 
             if (IS_OPEN_LOG_FILE || hasException) {
                 Timer timer2 = new Timer();
                 timer2.schedule(new OpenLogTask(), DELAY_TIME);
             }
         }
 
     }
 
     private void initData(List<JTextField> fields) {
         // first get outside, second get inside
         Properties config = Utils.loadPropertiesFile(System.getProperty("user.dir") + "\\" + CONF_FILE_NAME, this.getClass().getClassLoader().getResourceAsStream(CONF_FILE_NAME));
 
         List<String> props = Arrays.asList("fromDbDriver", "toDbDriver", "fromDbUrl", "toDbUrl", "fromDbUsername", "toDbUsername", "fromDbPassword", "toDbPassword", "criteria");
 
         logger.info("Init data from configruation");
         for (int i = 0; i < fields.size(); i++) {
             String key = props.get(i);
             String value = config.getProperty(key);
             logger.info("{} is {} ", key, value);
             fields.get(i).setText(value);
         }
 
         IS_OPEN_LOG_FILE = Utils.parseString2Boolean(config.getProperty("openLogFile"), false);
         DELAY_TIME = Utils.parseString2Long(config.getProperty("delayTime"), PER_SECOND);
         IS_DELETE_ORIGINAL_DATA = Utils.parseString2Boolean(config.getProperty("deleteOriginalData"), true);
         ABORT_WHEN_ABNORMAL_INSERT = Utils.parseString2Boolean(config.getProperty("abort"), false);
     }
 
     private void addCanCopyDataListener(final List<JTextField> fields) {
         for (JTextField jtextField : fields) {
             jtextField.addFocusListener(new FocusAdapter() {
                 @Override
                 public void focusLost(FocusEvent e) {
                     canCopyData(fields);
                 }
             });
         }
     }
 
     /**
      * can copy data?
      */
     private void canCopyData(List<JTextField> fields) {
         btnCopy.setEnabled(validateAllFiledToBeFilled(fields));
     }
 
     private boolean validateAllFiledToBeFilled(List<JTextField> fields) {
         for (JTextField obj : fields) {
             if (Utils.isBlank(obj.getText())) {
                 return false;
             }
         }
         return true;
     }
 
     private void showResults(String info) {
         results.setText(results.getText() + info);
         // logger.info(info);
     }
 
     private void exceptionHandler(Exception e) {
         String message = e.getMessage();
 
         logger.error(message);
         logger.info("Please see log file or contact me(QQ.506817493).Thanks.");
         if (Utils.isBlank(results.getText())) {
             results.setText("[ERROR] " + message);
         } else {
             results.setText(results.getText() + "\n[ERROR] " + message + "\nplease see log file or contact me(QQ.506817493).Thanks. ");
         }
     }
 
     private class PrintTimerTask extends java.util.TimerTask {
         @Override
         public void run() {
             showResults("· ");
         }
     }
 
 }
