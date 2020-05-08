 package interpret.views;
 
 import java.awt.*;
 import javax.swing.*;
 import javax.swing.border.*;
 
 public class MainFrame extends JFrame {
   private static final long serialVersionUID = -4570424264389438080L;
 
   private JLabel title; //タイトル
   private JLabel searchLabel;   // クラス検索用
   private JTextField searchBox; // クラス検索用
   private JButton searchButton; // クラス検索用
   private JLabel constLabel;    // コンストラクタ用
  //private JTextArea constArea;  // コンストラクタ用
   private JList constList;      // コンストラクタ用
 
   private JLabel objectLabel;   // オブジェクト一覧用
   private JList  objectList;   // オブジェクト一覧用
 
   private GridBagLayout layout;
   private GridBagConstraints constraints;
 
   private static final Font commonFont = new Font("Arial", Font.BOLD, 20);
 
   public MainFrame(){
     super("MainFrame");
     setSize(new Dimension(1000, 700));
     setLocationRelativeTo(null);
     setResizable(false);
     setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
     /* レイアウトの設定 */
     layout = new GridBagLayout();
     setLayout(layout);
     constraints = new GridBagConstraints();
 
     /* 各コンポーネントの生成 */
     title = new JLabel("Interpret");
     title.setFont(new Font("Arial", Font.BOLD, 30));
 
     searchLabel = new JLabel("クラス");
     searchLabel.setFont(commonFont);
     searchBox = new JTextField();
     searchBox.setPreferredSize(new Dimension(200, 40));
     searchButton = new JButton("検索");
 
     constLabel = new JLabel("コンストラクタ一覧");
     constLabel.setFont(commonFont);
     constList = new JList();
     constList.setPreferredSize(new Dimension(300, 400));
     constList.setBorder(new EtchedBorder(EtchedBorder.RAISED));
 
     objectLabel = new JLabel("オブジェクト一覧");
     objectLabel.setFont(commonFont);
     objectList = new JList();
     objectList.setPreferredSize(new Dimension(300, 300));
     objectList.setBorder(new EtchedBorder(EtchedBorder.RAISED));
 
    //constArea = new JTextArea();
    //constArea.setBorder(new EtchedBorder(EtchedBorder.RAISED));
    //constArea.setPreferredSize(new Dimension(800, 500));

     /* 共通設定 */
     constraints.insets = new Insets(5, 5, 5, 5);
 
     /* タイトルの表示 */
     constraints.gridwidth = GridBagConstraints.REMAINDER;
     constraints.anchor = GridBagConstraints.NORTHWEST;
     addComp(title, 0, 0, 3, 1);
     constraints.anchor = GridBagConstraints.SOUTHWEST;
     addComp(searchLabel, 0, 1, 3, 1);
     addComp(searchBox, 0, 2, 4, 1);
     addComp(searchButton, 4, 2, 1,1);
 
     addComp(constLabel, 0, 3, 3, 1);
     addComp(constList, 0, 4, 5, 6);
 
     addComp(objectLabel, 5, 1, 3, 1);
     addComp(objectList, 5, 2, 3, 3);
 
     new MessageFrame();
 
     setVisible(true);
   }
 
   private void addComp(Component com, int x, int y, int width, int height) {
     constraints.gridx = x;
     constraints.gridy = y;
     constraints.gridheight = height;
     constraints.gridwidth = width;
     layout.setConstraints(com, constraints);
     add(com);
   }
 }
