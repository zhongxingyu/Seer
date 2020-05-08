 package ui;
 
 import javax.swing.*;
 import java.awt.BorderLayout;
 import java.awt.event.*;
 
 @SuppressWarnings("serial")
 public class EvaluateWindow extends JFrame {
 	private JLabel label = new JLabel();
 	private int blackWin = 0, whiteWin = 0, draw = 0;
 
 	public EvaluateWindow(final Main frame) {
 		super("统计");
 		setBounds(600, 280, 200, 180);
 		setResizable(false);
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				frame.evaluateWindow.dispose();
				if (frame.infoWindow != null)
					frame.infoWindow.dispose();
 				frame.dispose();
 				frame.evaluating = false;
 				new ResetThread(frame).start();
 			}
 		});
 
 		label.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		getContentPane().add(label, BorderLayout.CENTER);
 
 		JPanel panel = new JPanel();
 		getContentPane().add(panel, BorderLayout.SOUTH);
 
 		final JCheckBox checkBox = new JCheckBox("显示棋盘");
 		checkBox.setSelected(true);
 		panel.add(checkBox);
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
 
 		final JToggleButton pauseButton = new JToggleButton("暂停");
 		panel.add(pauseButton);
 		pauseButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.evaluating = !pauseButton.isSelected();
 				if (frame.evaluating)
 					new RestartThread(frame).start();
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
 		label.setText("<html><b>黑方赢：　</b>" + blackWin + "局<br><b>白方赢：　</b>"
 				+ whiteWin + "局<br><b>平：　　　</b>" + draw
 				+ "局<br><br><b>黑方胜率：</b>" + blackWin * 100 / num
 				+ "%<br><b>白方胜率：</b>" + whiteWin * 100 / num + "%</html>");
 	}
 }
