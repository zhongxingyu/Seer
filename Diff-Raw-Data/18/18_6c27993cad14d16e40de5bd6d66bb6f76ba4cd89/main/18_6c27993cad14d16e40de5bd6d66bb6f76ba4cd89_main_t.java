 import com.mysql.jdbc.jdbc2.optional.MysqlConnectionPoolDataSource;
 import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.*;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.*;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Vector;
 
 
 public class main {
     public DefaultTableModel tableModel1;
     public JTable table;
     JPanel panelT;
 
     private void createUIComponents() {
 
     }
 
 
     private JTabbedPane tabbedPane1;
     private JPanel panel1;
     private JButton button1;
     private JTextField textField1;
     private JTextField textField2;
     private JTextField textField3;
     protected JList<Object> list1;
     private JButton openTableButton;
     private JTable table1;
     private JButton createTableButton;
     private JComboBox comboBox1;
     private JButton refreshListButton;
     private JScrollPane scr;
     static Settings settings;
     private static DefaultTableModel tableModel;
     JFrame createTableFrame;
     JScrollPane createTableScrollPane;
     JComboBox<String> ctCombo;
     JTextField newTableName;
 
 
 
 
     public main() {
 
         settings = new Settings("ka","ka","kax");
 
         try{
 
             FileInputStream fileInputStream=new FileInputStream("settings.st");//ვქმნით ფაილის წასაკითხ კანალს
             ObjectInputStream objectInputStream=new ObjectInputStream(fileInputStream);//ვქმნით ობიექტის შემომტან კანალს
             Object one=objectInputStream.readObject();//ვკითხულობთ ობიექტს
             settings=(Settings)one;//settings ცვლადს ვანიჭებთ წაკითხულ ობიექტს ისე, თან ვუთითებთ რომ Settings ტიპად მიენიჭოს
             textField1.setText(settings.getHost());
             textField2.setText(settings.getUsername());
             textField3.setText(settings.getPassword());
 
         }catch (Exception s){
 
         }
         try{
 
             Class.forName("com.mysql.jdbc.Driver");
 
 
             Connection connection=DriverManager.getConnection("jdbc:mysql://"+settings.getHost(),settings.getUsername(),settings.getPassword());
             DatabaseMetaData metaData=connection.getMetaData();
             ResultSet resultSet=metaData.getTables(null,null,"%",null);
             ArrayList<String> tables=new ArrayList<String>();
             while (resultSet.next()){
                 tables.add(resultSet.getString(3));
             }
             list1.setListData(tables.toArray());
 
         }catch (Exception ss){
 
         }
 
 
 
 
         button1.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if(!textField1.getText().isEmpty()&&!textField2.getText().isEmpty()){
 
                     setS(textField1.getText(),textField2.getText(),textField3.getText());
                     try{
                         FileOutputStream fileOutputStream=new FileOutputStream("settings.st");
                         ObjectOutputStream objectOutputStream=new ObjectOutputStream(fileOutputStream);
                         objectOutputStream.writeObject(settings);
                         objectOutputStream.close();
 
                     }catch (IOException l){
 
                     }
                 }else{
                     if(textField1.getText().isEmpty()){
                         textField1.setText("empty");
                     }
                     if(textField2.getText().isEmpty()){
                         textField2.setText("empty");
                     }
                 }
 
             }
         });
         openTableButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 try{
                     final String tableName=list1.getSelectedValue().toString();
                     Class.forName("com.mysql.jdbc.Driver");
                     Connection connection=DriverManager.getConnection("jdbc:mysql://"+settings.getHost(),settings.getUsername(),settings.getPassword());
                     PreparedStatement preparedStatement=connection.prepareStatement(String.format("select * from %s", tableName));
                     final ResultSet resultSet=preparedStatement.executeQuery();
                     final ResultSetMetaData metadata = resultSet.getMetaData();
                     final int columnCount;
                     columnCount = metadata.getColumnCount();
                    final JCheckBox checkBoxQuery;
                     checkBoxQuery=new JCheckBox("serch the whole word");
 
                     tableModel = new DefaultTableModel(new Object[]{},0);
                     final JFrame frameTable=new JFrame("table "+ tableName);
                     Vector<String> v=new Vector<String>();
 
                     for(int i=1;i<=columnCount;i++){
                         String name=metadata.getColumnName(i);
 
                         tableModel.addColumn(name);
                         v.add(name);
 
                     }
                     while (resultSet.next()){
                         String[] ss=new String[columnCount];
                         for (int i=1;i<=columnCount;i++){
                             ss[i-1]=resultSet.getString(i);
                         }
                         tableModel.addRow(ss);
                     }
 
                     table = new JTable(tableModel);
 
                     frameTable.setSize(800,400);
                    final JTextField searchField;
                     searchField = new JTextField();
                     final JComboBox comboBox=new JComboBox(v);
 
 
 
                     JButton btn1=new JButton("Search");
 
                     panelT = new JPanel();
                     panelT.add(new JScrollPane(table));
                     JPanel panelT2 =new JPanel();
 
                     JButton btn2=new JButton("click");
                     searchField.setColumns(15);
                     panelT2.add(comboBox);
                     panelT2.add(checkBoxQuery);
                     panelT2.add(searchField);
                     panelT2.add(btn1);
                     panelT.add(panelT2);
 
 
                     panelT.setLayout(new BoxLayout(panelT, BoxLayout.Y_AXIS));
                     frameTable.getContentPane().add(panelT);
 
 
                     frameTable.setVisible(true);
                     btn1.addActionListener(new ActionListener() {
                         @Override
                         public void actionPerformed(ActionEvent e) {
                             String searchString=searchField.getText().toString();
                             try {
                                 Class.forName("com.mysql.jdbc.Driver");
                                 String col=comboBox.getSelectedItem().toString();
                                 Connection connection=DriverManager.getConnection("jdbc:mysql://"+settings.getHost(),settings.getUsername(),settings.getPassword());
                                 StringBuilder sql=new StringBuilder();
                                final String query;
                                 if(!checkBoxQuery.isSelected()){
 
                                    query = String.format("select * from %s where %s like '%s'", tableName,col,searchString);
                                 } else{
                                    query= String.format("select * from %s where %s='%s'", tableName, col, searchString);
                                 }
                                 PreparedStatement statement2=connection.prepareStatement(query);
                                 final ResultSet resultSet2=statement2.executeQuery();
                                 final ResultSetMetaData metadata2 = resultSet2.getMetaData();
                                 tableModel1 = new DefaultTableModel(new Object[]{},0);
                                 for(int i=1;i<=columnCount;i++){
                                     String name=metadata2.getColumnName(i);
 
                                     tableModel1.addColumn(name);
                                 }
                                 while (resultSet2.next()){
                                     String[] ss=new String[columnCount];
                                     for (int i=1;i<=columnCount;i++){
                                         ss[i-1]=resultSet2.getString(i);
                                     }
                                     tableModel1.addRow(ss);
                                 }
                                 JFrame searchFrame=new JFrame("search frame");
                                 JPanel searchPanel=new JPanel();
                                 JTable searchTable=new JTable(tableModel1);
                                 searchPanel.add(new JScrollPane(searchTable));
                                 searchFrame.add(searchPanel);
                                 searchFrame.setSize(600,200);
                                 searchFrame.setVisible(true);
 
                             } catch (ClassNotFoundException e1) {
                                 e1.printStackTrace();
                             } catch (SQLException e1) {
                                 e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                             }
                         }
                     });
                 }catch (Exception ee){}
             }
         });
         createTableButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
 
                 if(comboBox1.getSelectedItem().toString()!="Number Of Fields"){
                     Vector<String> dataTypes=new Vector<String>();
                     dataTypes.add("INT ( 11 )");
                     dataTypes.add("VARCHAR( 50 )");
                     dataTypes.add("DATE");
                     dataTypes.add("TEXT");
                     dataTypes.add("TIME");
                     dataTypes.add("BLOB");
                     dataTypes.add("LONGBLOB");
                     dataTypes.add("ENUM");
                     dataTypes.add("BOOL");
                     dataTypes.add("SET");
                     dataTypes.add("DECIMAL");
                     dataTypes.add("CHAR");
                     final JCheckBox dropTableCheck=new JCheckBox("Drop table if existes");
 
 
                     createTableFrame = new JFrame("Create Table");
                     final int numberOfFields;
                     numberOfFields = Integer.valueOf(comboBox1.getSelectedItem().toString());
                     final ArrayList<JComboBox> comboBoxes=new ArrayList<JComboBox>();
                     final ArrayList<JTextField> textFields=new ArrayList<JTextField>();
                 for(int i=0;i<numberOfFields;i++){
                     textFields.add(new JTextField());
                     comboBoxes.add(new JComboBox(dataTypes));
                     }
                     JPanel panelc=new JPanel();
                     panelc.setLayout(new BoxLayout(panelc, BoxLayout.Y_AXIS));
 
                 for (int i=0;i<numberOfFields;i++){
                     JPanel panelInner=new JPanel();
                     panelInner.add(textFields.get(i));
                     textFields.get(i).setColumns(15);
                     panelInner.add(comboBoxes.get(i));
                     panelc.add(panelInner);
 
                 }
                     panelc.add(dropTableCheck);
 
                     JPanel pp=new JPanel();
                     JButton btnCreate=new JButton("Create Table");
 
                     newTableName = new JTextField();
                     newTableName.setColumns(15);
                     pp.add(new JScrollPane(panelc));
 
                     pp.add(newTableName);
                     pp.add(btnCreate);
 
                     createTableFrame.add(pp);
 
 
                     createTableFrame.setSize(300,420);
                     createTableFrame.setResizable(false);
                     createTableFrame.setVisible(true);
                     btnCreate.addActionListener(new ActionListener() {
                         @Override
                         public void actionPerformed(ActionEvent e) {
                             String query="";
                             query+="CREATE TABLE ";
                             query+=newTableName.getText().toString();
                             query+=" (";
                             for(int i=0;i<numberOfFields;i++){
                                      query+=String.format("%s %s",textFields.get(i).getText().toString(),comboBoxes.get(i).getSelectedItem().toString());
                                 if(i<numberOfFields-1){
                                     query+=",";
                                 }
                             }
                             query+=")";
 
 
 
 
                             MysqlDataSource ds = new MysqlConnectionPoolDataSource();
                             String[] all = settings.getHost().split("/");
                             ds.setServerName(all[0]);
                             ds.setPort(3306);
                             ds.setUser(settings.getUsername());
                             ds.setPassword(settings.getPassword());
                             Connection connection = null ;
                             try {
                                 connection =  ds.getConnection();
                                 Statement statement = connection.createStatement();
                                 statement.executeUpdate("USE "+all[1]) ;
                                 if(dropTableCheck.isSelected()){
                                     statement.execute(String.format("DROP TABLE IF EXISTS %s", newTableName.getText().toString()));
                                 }
                                 statement.executeUpdate(query);
                             } catch (SQLException e1) {
                                 e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                             }
                         }
                     });
 
             } }
         });
 
         refreshListButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 try{
 
                     Class.forName("com.mysql.jdbc.Driver");
 
 
                     Connection connection=DriverManager.getConnection("jdbc:mysql://"+settings.getHost(),settings.getUsername(),settings.getPassword());
                     DatabaseMetaData metaData=connection.getMetaData();
                     ResultSet resultSet=metaData.getTables(null,null,"%",null);
                     ArrayList<String> tables=new ArrayList<String>();
                     while (resultSet.next()){
                         tables.add(resultSet.getString(3));
                     }
                     list1.setListData(tables.toArray());
 
                 }catch (Exception ss){
 
                 }
             }
         });
     }
     public void setS(String s1,String s2,String s3){
         settings.setHost(s1);
         settings.setUsername(s2);
         settings.setPassword(s3);
     }
     public static void main(String[] args) throws InterruptedException, InvocationTargetException {
 
         JFrame frame = new JFrame("main");
         frame.setContentPane(new main().panel1);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.pack();
         frame.setResizable(false);
         frame.setVisible(true);
     }
 }
