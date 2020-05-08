 package TopView;
 
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.BevelBorder;
 
 import net.miginfocom.swing.MigLayout;
import AddressBook.PaneAddress;
 
 /** 画面上部のメニュー */
 public class MenuPanel extends JPanel {
 
 	// アイコンたち
 	JLabel newMail = new JLabel("新規作成", new ImageIcon("data/menuIcon/newMail.png"), JLabel.CENTER);
 	JLabel receiveBox = new JLabel("受信BOX", new ImageIcon("data/menuIcon/receive.png"), JLabel.CENTER);
 	JLabel sentBox = new JLabel("送信BOX", new ImageIcon("data/menuIcon/sent.png"), JLabel.CENTER);
 	JLabel notSendBox = new JLabel("未送信BOX", new ImageIcon("data/menuicon.png"), JLabel.CENTER);
 	JLabel trush = new JLabel("ゴミ箱", new ImageIcon("data/menuIcon/trush.png"), JLabel.CENTER);
 	JLabel addressBook = new JLabel("アドレス帳", new ImageIcon("data/menuicon.png"), JLabel.CENTER);
 	JLabel option = new JLabel("設定", new ImageIcon("data/menuIcon/option.png"), JLabel.CENTER);
 	
 	// アイコンクリックで表示する各種JPanel
	PaneAddress paneAddress;
 	/** デバッグ用 */
 	JPanel dummyFrame;
 
 	public MenuPanel() {
 
 		// レイアウト設定
 		this.setLayout(new MigLayout("", "[]40[]40[]40[]40[]40[]", "[grow]"));
 		this.setBorder(new BevelBorder(BevelBorder.RAISED));
 
 		// 各種アイコンの設定と追加
 		List<JLabel> menuIcons = Arrays.asList(newMail, receiveBox, sentBox, notSendBox, trush, addressBook, option);
 		for (JLabel menuIcon : menuIcons) {
 			menuIcon.setHorizontalTextPosition(JLabel.CENTER);
 			menuIcon.setVerticalTextPosition(JLabel.BOTTOM);
 			menuIcon.addMouseListener(new MenuIconsAction());
 			add(menuIcon, "");
 		}
 
 	}
 	
 	class MenuIconsAction extends MouseAdapter {
 		
 		@Override
 		public void mouseEntered(MouseEvent e) {
 			
 			// マウスカーソルを手の形に変える
 			Cursor cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
 			Component comp = (Component)e.getSource();
 			comp.setCursor(cursor);
 			
 			// マウスカーソルの変化だけでなく、アイコンに変化があれば、
 			// マウスが乗っかっているのがわかりやすくなりそうです。
 			
 			// アイコンに変化をつける（余裕あれば）
 		}
 		
 		public void mouseExited(MouseEvent e) {
 			// アイコンをもとに戻す（余裕あれば）
 		}
 		
 		@Override
 		public void mouseClicked(MouseEvent e) {
 			
 			Component comp = e.getComponent();
 			
 			// いまいちスマートじゃない…
 			if(comp == newMail) {
 				// 新規作成
 				System.out.println("newMail!");
 			}
 			else if (comp == receiveBox) {
 				// 受信BOX
 				System.out.println("receive!");
 			}
 			else if(comp == sentBox) {
 				// 送信BOX
 				System.out.println("sent!");
 			}
 			else if (comp == notSendBox) {
 				// 未送信BOX
 				System.out.println("notSend!");
 			}
 			else if (comp == trush) {
 				// ゴミ箱
 				System.out.println("trush!");
 			}
 			else if(comp == addressBook) {
 				// アドレス帳
 				System.out.println("addressBook!");
 				
				if(!TopView.showTab(paneAddress)) {
					paneAddress = new PaneAddress();
					TopView.addTab("アドレス帳", paneAddress);
 				}
 			}
 			else if (comp == option) {
 				// 設定
 				System.out.println("option!");
 				
 				if(!TopView.showTab(dummyFrame)) {
 					dummyFrame = new DummyPanel();
 					TopView.addTab("ダミー", dummyFrame);
 				}
 			}
 				
 		}
 	}
 
 // ↓ 一応残しておきます。そのうち消します。
 	
 //	@Override
 //	public void actionPerformed(ActionEvent e) {
 //		String buttonEvent = e.getActionCommand();
 //		switch(buttonEvent){
 //		case "新規作成":
 //			System.out.println("newMail!!");
 //			break;
 //		case "ゴミ箱":
 //			System.out.println("trush!");
 //			break;
 //		case "未送信":
 //			System.out.println("notSend");
 //			break;
 //		case "アドレス帳":
 //			/*
 //			 * 複数タブが生成されるのを防止する
 //			 */
 //			if(frmAddress == null){
 //				frmAddress = new FrmAddress();
 //				TopView.topViewAddTab("アドレス帳", frmAddress);
 //			}
 //			break;
 //		case "設定":
 //			System.out.println("option!");
 //			break;
 //		case "フォルダ作成":
 //			System.out.println("newFolder!");
 //			break;
 //		}
 //
 //	}
 
 }
