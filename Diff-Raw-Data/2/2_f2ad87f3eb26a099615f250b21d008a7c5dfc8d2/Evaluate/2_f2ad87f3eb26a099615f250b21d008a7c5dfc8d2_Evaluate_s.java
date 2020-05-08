 package ui;
 
 import javax.swing.*;
 import java.awt.BorderLayout;
 import java.awt.event.*;
 
 @SuppressWarnings("serial")
 public class Evaluate extends JFrame {
 	private JLabel label = new JLabel();
 	private int blackWin = 0, whiteWin = 0, draw = 0;
 
 	public Evaluate(final Main frame) {
 		super("统计");
 		setBounds(600, 300, 200, 150);
 		setResizable(false);
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				frame.evaluateWindow.dispose();
 				frame.infoWindow.dispose();
 				frame.dispose();
 				new Main();
 			}
 		});
 
 		label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		getContentPane().add(label, BorderLayout.CENTER);
 
 		final JCheckBox checkBox = new JCheckBox("显示棋盘");
 		checkBox.setSelected(true);
 		getContentPane().add(checkBox, BorderLayout.SOUTH);
 		checkBox.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.showBoard = checkBox.isSelected();
 				if (frame.showBoard) {
 					frame.setVisible(true);
 					frame.infoWindow.setVisible(true);
 				} else {
 					frame.setVisible(false);
 					frame.infoWindow.setVisible(false);
 				}
 			}
 		});
 
 		setVisible(true);
 	}
 
 	public void updateLabel(Main frame) {
 		if (frame.finished)
 			switch (frame.winner) {
 			case -1:
 				whiteWin++;
 				break;
 			case 0:
 				draw++;
 				break;
 			case 1:
 				blackWin++;
 			}
 		int num = blackWin + whiteWin + draw;
 		if (num == 0)
 			num = 1;
 		label.setText("<html><b>黑方赢：</b>" + blackWin + "局<br><b>白方赢：</b>"
				+ whiteWin + "局<br><b>平：</b>" + draw + "局<br><br><b>黑方：</b>"
 				+ blackWin * 100 / num + "%<br><b>白方胜率：</b>" + whiteWin * 100
 				/ num + "%</html>");
 	}
 }
