 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * ItoPosFrame.java
  *
  * Created on 2009/04/10, 7:34:18
  */
 package itopos;
 
 import db.DaoFactry;
 import db.HistoryDao;
 import db.ItemDao;
 import db.UserDao;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.concurrent.FutureTask;
 import javax.swing.DefaultListModel;
 import javax.swing.JLabel;
 import javax.swing.Timer;
 import obj.Customer;
 import obj.History;
 import obj.Item;
 import sound.Sound;
 import twitter.TwitterAccount;
 import twitter4j.TwitterException;
 
 /**
  *
  * @author yasuhiro-i,shinya-m
  */
 @SuppressWarnings("serial")
 public class ItoPosFrame extends javax.swing.JFrame implements Mediator, ActionListener {
 
     ItemDao idao;
     UserDao udao;
     HistoryDao hdao;
     UdpFelica udpFelica;
     protected DefaultListModel model;
     constant.Status.ItoPosStatus itoposStatus;
     ArrayList<Item> bucket;
     Customer user;
     Timer timer;
     sound.Sound sound;
     boolean updateItem;
     TwitterAccount tw;
 
     void requestRePaint() {
         this.configPanel.validate();
     }
 
     /** Creates new form ItoPosFrame */
     public ItoPosFrame() {
         initComponents();
 
         this.idao = DaoFactry.createItemDao();
         this.udao = DaoFactry.createUserDao();
         this.hdao = DaoFactry.createHistoryDao();
 
         model = new DefaultListModel();
         bucket = new ArrayList<Item>();
         udpFelica = new UdpFelica(50005);
         Thread th = new Thread(udpFelica);
         th.start();
         createCollegues();
         jListBacket.setModel(model);
         itoposStatus = constant.Status.ItoPosStatus.INIT;
         centerImagePanel.setFolder(constant.Graphics.CENTER_LOGO_FOLDER);
 
         sound = new Sound("hogehoge");
         
         try {
 			tw=new TwitterAccount(this);
 		} catch (TwitterException e1) {
 			e1.printStackTrace();
 		} catch (URISyntaxException e1) {
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 
 
         barcodeField.addKeyListener(new KeyAdapter() {
 
             @Override
             public void keyPressed(KeyEvent e) {
                 //System.out.println(e.getKeyCode());
 
                 if (e.getKeyCode() == 10) {//バーコードを読み込んだら
                     if (!barcodeField.isEditable()) {
                         return;
                     }
 
                     if (barcodeField.getText().equals(constant.Barcode.HISTORY)) {//読み取ったバーコードがヒストリーならば
                         ArrayList<String> histories = hdao.getHistory();
                         barcodeField.setText("");
                         configPanel.removeAll();
                         configPanel.setLayout(new GridLayout(0, 1, 5, 5));
 
                         int index = 0;
                         for (String history : histories) {
                             index++;
                             if (index > 10) {
                                 break;
                             }
                             JLabel obj = new JLabel(history);
                             obj.setFont(new java.awt.Font("HGS創英角ﾎﾟｯﾌﾟ体", 0, 12));
                             configPanel.add(obj);
                         }
                         configPanel.repaint();
                         configPanel.validate();
                         return;
                     }
 
                     if (barcodeField.getText().equals(constant.Barcode.LANKING)) {//読み取ったバーコードがランキングならば
                         ArrayList<Customer> users = udao.selectUserLanking();
                         barcodeField.setText("");
                         configPanel.removeAll();
                         configPanel.setLayout(new GridLayout(0, 2, 10, 10));
                         //configPanel.removeAll();
                         //configPanel.setLayout(new GridLayout(10, 0));
 
                         int index = 0;
                         for (Customer user : users) {
                             index++;
                             if (index > 10) {
                                 break;
                             }
                             JLabel name = new JLabel(user.getNickName());
                             JLabel money = new JLabel(Integer.toString((int) user.getAllConsumedPoint()) + "円");
                             name.setFont(new java.awt.Font("HGS創英角ﾎﾟｯﾌﾟ体", 0, 18));
                             money.setFont(new java.awt.Font("HGS創英角ﾎﾟｯﾌﾟ体", 0, 18));
                             configPanel.add(name);
                             configPanel.add(money);
                         }
                         configPanel.repaint();
                         configPanel.validate();
                         return;
 
                     }
 
                     
                     if (barcodeField.getText().equals(constant.Barcode.CANCEL)) {//読み取ったバーコードがキャンセルならば
                         messageLabel.setText("キャンセルされました");
                         requestResetSystem(3);
                         return;
                     }
 
                     if (barcodeField.getText().equals(constant.Barcode.BUY)) {//読み取ったバーコードが購入ならば
 
                         if (bucket.size() == 0) {//買い物かごに何もなければ
                             barcodeField.setText("");
                             messageLabel.setText("恐れ入ります。カゴに何も入っていません。もう一度ゆっくりとお願いします。");
                             FutureTask task = new FutureTask(new Sound(constant.SoundFile.DENY));
                             new Thread(task).start();
                             itoposStatus = constant.Status.ItoPosStatus.REBOOT;
                             requestResetSystem(3);
                             return;
                         }
 
                         if (itoposStatus.equals(constant.Status.ItoPosStatus.AUTHORIZED)) {//学生証が読み込まれていれば
                             int cost = 0;
                             int count=0;
                             StringBuilder sb=new StringBuilder();
 
                             for (Item item : bucket) {
                                 cost += item.getSold_cost();
                                 //item.setPro_num(item.getPro_num()-1);
                                 Item tmp = idao.selectByBarCode(item.getBarcode());
                                 sb.append(tmp.getName());
                                 if(count<bucket.size()-1)sb.append(",");
                                 count++;
                                 int stock = tmp.getPro_num() - 1;
                                 item.setPro_num(stock);//在庫を減らす
                                 idao.updateBuppin(item);
                                 if(stock==0){
                                 	try {
 										tw.tweet(item.getName()+"の在庫が無くなりました。完売御礼！");
 									} catch (TwitterException e1) {
 										e1.printStackTrace();
 									}
                                 }
                             }
                             int aftercost = user.getCost() - cost;//残金
                             double allcost = user.getAllConsumedPoint();
                             udao.updateUser(user.getFelicaId(), aftercost, allcost + cost);
                             messageLabel.setText("お買い上げ金額は　" + cost + "円です。　ありがとうございました。" +
                                     "残金は" + aftercost + "円です(買う前" + user.getCost() + ")");
                             sound.PlayWave(constant.SoundFile.END);
 
                             barcodeField.setText("");
                             for (Item item : bucket) {
                                 History h = new History();
                                 h.setBid(item.getBarcode());
                                 h.setUid(user.getFelicaId());
                                 hdao.insertHistory(h);
 
                             }
 
                             itoposStatus = constant.Status.ItoPosStatus.REBOOT;
                             requestResetSystem(3);
                             
                             try {
                             	String s=user.getNickName()+"さんが";
                             	String ss="を購入しました！";
                            	String sss=user.getNickName()+"さんはこれまでに"+((int)user.getAllConsumedPoint()+cost)+"円使ってますよ。";
                             	String tweet=s+tw.shrinkString(sb.toString(),140-s.length()-ss.length()-sss.length())+ss+sss;
         						tw.tweet(tweet);
         					} catch (TwitterException e1) {
         						e1.printStackTrace();
         					}
                             return;
 
                         }
                     }
                     
                     String bcode = barcodeField.getText();
                     if(!idao.isBuppinExist(bcode)){//読み取ったバーコードの商品がない場合は登録フォームを出す
                     	new ItemRegistrationForm(idao,ItoPosFrame.this,bcode,tw);
                     	barcodeField.setText("");
                     	return;
 //                      barcodeField.setText("");
 //                      messageLabel.setText("存在しないバーコードです");
 //                      FutureTask task = new FutureTask(new Sound(constant.SoundFile.DENY));
 //                      new Thread(task).start();
                     }
                     
                     if (idao.isBuppinExist(bcode)) {//読み取ったバーコードの商品がある場合
                     	if(itoposStatus.equals(constant.Status.ItoPosStatus.AUTHORIZED)){//学生証が読み込まれていれば
                     		Item item = idao.selectByBarCode(bcode);
                             model.addElement(item.getName() + ", " + item.getSold_cost() + "円");
                             bucket.add(item);
                             barcodeField.setText("");
                             barcodeField.requestFocus();
                             messageLabel.setText("かごに入れました");
                     	}else if(updateItem){//在庫追加モードならば
                     		new ItemRegistrationForm(idao, ItoPosFrame.this,tw).set(idao.selectByBarCode(bcode));
                     		barcodeField.setText("");
                     	}else{
                     		messageLabel.setText("認証してください");
                             requestResetSystem(3);
                             return;
                     	}
                     }
                 }
                 else if(e.getKeyCode()==27){//ESCで終了
                 	System.exit(0);
                 }else if(e.getKeyCode()==KeyEvent.VK_F5){//F5キー
                 	if (itoposStatus.equals(constant.Status.ItoPosStatus.AUTHORIZED)){
                 		new CostForm(udao, user,ItoPosFrame.this);
                 	}
                 }else if(e.getKeyCode()==KeyEvent.VK_F1){//F1キー、在庫追加モード
                 	updateItem=true;
                 	return;
                 }
                 super.keyPressed(e);
             }
         });
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jDialog1 = new javax.swing.JDialog();
         jPanel1 = new javax.swing.JPanel();
         jPanel4 = new javax.swing.JPanel();
         messageLabel = new javax.swing.JLabel();
         statusLabel = new javax.swing.JLabel();
         countDown1 = new count.countDown();
         jPanel2 = new javax.swing.JPanel();
         barcodeField = new javax.swing.JTextField();
         jLabel1 = new javax.swing.JLabel();
         jPanel3 = new javax.swing.JPanel();
         jScrollPane1 = new javax.swing.JScrollPane();
         jListBacket = new javax.swing.JList();
         jLabel2 = new javax.swing.JLabel();
         configPanel = new javax.swing.JPanel();
         centerImagePanel = new util.ImagePanel();
 
         javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
         jDialog1.getContentPane().setLayout(jDialog1Layout);
         jDialog1Layout.setHorizontalGroup(
             jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 400, Short.MAX_VALUE)
         );
         jDialog1Layout.setVerticalGroup(
             jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 300, Short.MAX_VALUE)
         );
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
         setUndecorated(true);
 
         jPanel1.setBackground(new java.awt.Color(255, 255, 255));
         jPanel1.setLayout(new java.awt.BorderLayout(10, 10));
 
         jPanel4.setBackground(new java.awt.Color(255, 204, 204));
         jPanel4.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
 
         messageLabel.setFont(new java.awt.Font("HGS創英角ﾎﾟｯﾌﾟ体", 0, 18));
         messageLabel.setText("メッセージ");
 
         statusLabel.setFont(new java.awt.Font("HGP創英角ﾎﾟｯﾌﾟ体", 0, 18));
         statusLabel.setText("ステータス");
 
         countDown1.setFont(new java.awt.Font("MS UI Gothic", 1, 14));
 
         javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(messageLabel)
                     .addComponent(statusLabel)
                     .addComponent(countDown1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(916, Short.MAX_VALUE))
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel4Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(messageLabel)
                 .addGap(29, 29, 29)
                 .addComponent(statusLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(countDown1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(62, Short.MAX_VALUE))
         );
 
         jPanel1.add(jPanel4, java.awt.BorderLayout.SOUTH);
 
         jPanel2.setBackground(new java.awt.Color(255, 153, 153));
         jPanel2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
 
         barcodeField.setBackground(new java.awt.Color(255, 255, 204));
         barcodeField.setFont(new java.awt.Font("HG創英角ﾎﾟｯﾌﾟ体", 0, 18));
         barcodeField.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
 
         jLabel1.setFont(new java.awt.Font("HGP創英角ﾎﾟｯﾌﾟ体", 3, 18));
         jLabel1.setText("バーコード");
 
         javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jLabel1)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                         .addContainerGap()
                         .addComponent(barcodeField, javax.swing.GroupLayout.DEFAULT_SIZE, 994, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel1)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(barcodeField, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(95, Short.MAX_VALUE))
         );
 
         jPanel1.add(jPanel2, java.awt.BorderLayout.PAGE_START);
 
         jPanel3.setBackground(new java.awt.Color(255, 204, 204));
         jPanel3.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
 
         jListBacket.setBackground(new java.awt.Color(204, 255, 204));
         jListBacket.setFont(new java.awt.Font("HGP創英角ﾎﾟｯﾌﾟ体", 0, 18));
         jScrollPane1.setViewportView(jListBacket);
 
         jLabel2.setFont(new java.awt.Font("HGS創英角ﾎﾟｯﾌﾟ体", 0, 18));
         jLabel2.setText("買い物かご");
 
         javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE)
                     .addComponent(jLabel2))
                 .addContainerGap())
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .addComponent(jLabel2)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 231, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jPanel1.add(jPanel3, java.awt.BorderLayout.WEST);
 
         configPanel.setBackground(new java.awt.Color(255, 204, 204));
         configPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         configPanel.setMinimumSize(new java.awt.Dimension(300, 300));
 
         javax.swing.GroupLayout configPanelLayout = new javax.swing.GroupLayout(configPanel);
         configPanel.setLayout(configPanelLayout);
         configPanelLayout.setHorizontalGroup(
             configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 300, Short.MAX_VALUE)
         );
         configPanelLayout.setVerticalGroup(
             configPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 276, Short.MAX_VALUE)
         );
 
         jPanel1.add(configPanel, java.awt.BorderLayout.EAST);
 
         centerImagePanel.setBackground(new java.awt.Color(204, 204, 204));
         centerImagePanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         centerImagePanel.setMaximumSize(new java.awt.Dimension(300, 300));
 
         javax.swing.GroupLayout centerImagePanelLayout = new javax.swing.GroupLayout(centerImagePanel);
         centerImagePanel.setLayout(centerImagePanelLayout);
         centerImagePanelLayout.setHorizontalGroup(
             centerImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 421, Short.MAX_VALUE)
         );
         centerImagePanelLayout.setVerticalGroup(
             centerImagePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 276, Short.MAX_VALUE)
         );
 
         jPanel1.add(centerImagePanel, java.awt.BorderLayout.CENTER);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 1020, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 682, Short.MAX_VALUE)
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JTextField barcodeField;
     private util.ImagePanel centerImagePanel;
     private javax.swing.JPanel configPanel;
     private count.countDown countDown1;
     private javax.swing.JDialog jDialog1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JList jListBacket;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JLabel messageLabel;
     private javax.swing.JLabel statusLabel;
     // End of variables declaration//GEN-END:variables
 
     public void resetSystem() {
         barcodeField.setText("");
         messageLabel.setText("ようこそ");
         barcodeField.requestFocus();
         model.clear();
         bucket.clear();
         barcodeField.requestFocus();
         itoposStatus = constant.Status.ItoPosStatus.INIT;
         statusLabel.setText("初期状態");
         barcodeField.setEnabled(true);
     }
 
     private void requestResetSystem(int sec) {
         //timer = new Timer(sec * 1000, this);
         //timer.start();
         barcodeField.setEnabled(false);
         countDown1.setCount(sec);
     }
 
     public void requestResetSystem(int sec, boolean editable) {
         barcodeField.setEnabled(editable);
         countDown1.setCount(sec);
     }
 
     public void createCollegues() {
         udpFelica.setMediator(this);
         countDown1.setMediator(this);
     }
 
     @Override
     public void collegueChanged(String message) {
         if (message.equals("GETPACKET")) {
             user = udao.selectUser(udpFelica.getMID());
             if (!this.itoposStatus.equals(constant.Status.ItoPosStatus.INIT)) {
                 return;
             }
             if (user.getName() != null) {
                 if (user.getCost() >= 100) {
                     messageLabel.setText("毎度ありがとうございます" + user.getName() + "さん。 " + "残金は" + user.getCost() + "円です");
                     itoposStatus = constant.Status.ItoPosStatus.AUTHORIZED;
                     statusLabel.setText("認証済み");
                     barcodeField.setEditable(false);
                     FutureTask task = new FutureTask(new Sound(constant.SoundFile.AUTH_SOUND));
                     new Thread(task).start();
                     barcodeField.setEditable(true);
                     requestResetSystem(360, true);
                 } else {
                     messageLabel.setText("毎度ありがとうございます" + user.getName() + "さん。 " + " 残金は" + user.getCost() + "円ですが、少なすぎないですか？");
                     itoposStatus = constant.Status.ItoPosStatus.AUTHORIZED;
                     statusLabel.setText("認証済み");
                     barcodeField.setEditable(false);
                     FutureTask task = new FutureTask(new Sound(constant.SoundFile.SAISOKU));
                     new Thread(task).start();
                     barcodeField.setEditable(true);
                     requestResetSystem(360, true);
                 }
 
             } else {//学生証が登録されていなかったら
             	//TODO
 //                messageLabel.setText("認証エラーです。やりなおしてください");
 //                FutureTask task = new FutureTask(new Sound(constant.SoundFile.DENY));
 //                new Thread(task).start();
 //                this.itoposStatus = constant.Status.ItoPosStatus.REBOOT;
 //                requestResetSystem(3);
             	new UserRegistrationForm(udpFelica.getMID(),udao);
             }
         }
         if (message.equals("TIME_OUT")) {
             resetSystem();
         }
 
     }
     
     public void cancelUpdateItem(){
     	updateItem=false;
     }
 
     public void actionPerformed(ActionEvent e) {
     }
 }
