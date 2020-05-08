 package ui;
 
 import javax.swing.*;
 import javax.swing.filechooser.FileFilter;
 import java.awt.event.*;
 import java.io.*;
 import java.net.URL;
 import java.nio.ByteBuffer;
 import java.nio.channels.*;
 import java.util.Scanner;
 
 import database.Configure;
 import database.Detail;
 import database.List;
 
 @SuppressWarnings("serial")
 class Port extends JFrame {
 	private static final String[] NAME = { "导入", "导出" };
 	private Main frame;
 	private String[] id;
 	private int mode;
 	private JProgressBar progressBar;
 	private JTextField csvField, picField;
 	private File csvFile, picDirectory;
 
 	private class Import extends Thread {
 		// mode 1 = override, mode 2 = merge, mode 3 = ignore
 		private int mode = 0;
 
 		@Override
 		public void run() {
 			try {
 				Scanner csvScan = new Scanner(csvFile);
 				int count = 0;
 				while (csvScan.hasNext()) {
 					csvScan.nextLine();
 					count++;
 				}
 				csvScan.close();
 				csvScan = new Scanner(csvFile);
 				for (int i = 0; i < count; i++) {
 					progressBar.setValue(i * 100 / count);
 					String data = csvScan.nextLine(), id = data.substring(1,
 							data.indexOf('\'', 1));
 					if (frame.database.exist(id)) {
 						if (mode == 0) {
 							String[] option = { "覆盖", "合并", "忽略" };
 							mode = JOptionPane.showOptionDialog(Port.this,
 									"请选择重复项处理方式", "遇到重复项",
 									JOptionPane.YES_NO_CANCEL_OPTION,
 									JOptionPane.QUESTION_MESSAGE, null, option,
 									option[0]) + 1;
 						}
 						switch (mode) {
 						case 1:
 							frame.database.deletePic(frame.webServer, id);
 							frame.database.delete(id);
 							break;
 						case 2:
 							frame.database.merge(id, data);
 							File picFile = new File(picDirectory.getPath()
 									+ "/" + id + ".jpg");
 							if (picFile.exists()) {
 								frame.database.deletePic(frame.webServer, id);
 								frame.database
 										.update(id,
 												"pic",
 												"'"
 														+ frame.webServer
 																.setPic(picFile)
 														+ "'");
 							}
 						case 3:
 							continue;
 						}
 					}
 					frame.database.insert(data);
 					File picFile = new File(picDirectory.getPath() + "/" + id
 							+ ".jpg");
 					if (picFile.exists())
 						frame.database.update(id, "pic",
 								"'" + frame.webServer.setPic(picFile) + "'");
 				}
 				csvScan.close();
 				frame.refresh();
 				progressBar.setValue(100);
 				finish();
 			} catch (Exception e) {
 				e.printStackTrace();
 				JOptionPane.showMessageDialog(Port.this, "导入失败！", "错误",
 						JOptionPane.ERROR_MESSAGE);
 				try {
 					if (frame.database.exist("temp"))
 						frame.database.delete("temp");
 				} catch (Exception e1) {
 				}
 				frame.refresh();
 				dispose();
 			}
 		}
 	}
 
 	private class Export extends Thread {
 		@Override
 		public void run() {
 			try {
 				if (!picDirectory.exists())
 					picDirectory.mkdirs();
 				File csvPath = csvFile.getParentFile();
 				if ((csvPath != null) && !csvPath.exists())
 					csvPath.mkdirs();
 				PrintWriter csvWrite = new PrintWriter(csvFile);
 				for (int i = 0; i < id.length; i++) {
 					progressBar.setValue(i * 100 / id.length);
 					csvWrite.print("'" + id[i] + "'");
 					Detail info = new Detail(frame.database, id[i]);
 					for (int x = 0; x < 7; x++)
 						for (int y = 0; y < 7; y++) {
 							String data = info.get(List.COLUMN_NAME[x][y]);
 							switch (List.COLUMN_TYPE[x][y]) {
 							case 1:
 								data = "'" + data + "'";
 								break;
 							case 2:
 								if (data == null)
 									data = "null";
 								break;
 							case 3:
 								if (data == null)
 									data = "0000-00-00";
 								data = "'" + data + "'";
 								break;
 							case 4:
 								data = "'" + data + "'";
 							}
 							csvWrite.print("," + data);
 						}
 					csvWrite.println();
 					String picAddress = info.get("pic");
 					info.close();
 					if (picAddress.length() == 32) {
 						ReadableByteChannel url = Channels.newChannel(new URL(
 								"http://"
 										+ Configure.webserverAddress
										+ "/pic/"
 										+ picAddress.substring(0,
 												picAddress.length() - 5)
 										+ "/"
 										+ picAddress.substring(picAddress
 												.length() - 5) + ".jpg")
 								.openStream());
 						FileOutputStream outStream = new FileOutputStream(
 								picDirectory.getPath() + "/" + id[i] + ".jpg");
 						FileChannel out = outStream.getChannel();
 						ByteBuffer buffer = ByteBuffer.allocate(10000);
 						while (url.read(buffer) != -1) {
 							buffer.flip();
 							out.write(buffer);
 							buffer.clear();
 						}
 						out.close();
 						outStream.close();
 						url.close();
 					}
 				}
 				csvWrite.close();
 				progressBar.setValue(100);
 				finish();
 			} catch (Exception e) {
 				JOptionPane.showMessageDialog(Port.this, "导出失败！", "错误",
 						JOptionPane.ERROR_MESSAGE);
 				dispose();
 			}
 		}
 	}
 
 	Port(Main frame, final int mode, String[] id) {
 		super(NAME[mode] + "数据");
 		setSize(370, 200);
 		setLocationRelativeTo(frame);
 		setResizable(false);
 		this.frame = frame;
 		this.id = id;
 		this.mode = mode;
 		getContentPane().setLayout(null);
 
 		JLabel csvLabel = new JLabel("CSV文件：");
 		csvLabel.setBounds(30, 35, 63, 16);
 		getContentPane().add(csvLabel);
 
 		JLabel picLabel = new JLabel("照片文件夹：");
 		picLabel.setBounds(30, 75, 78, 16);
 		getContentPane().add(picLabel);
 
 		csvField = new JTextField();
 		csvField.setBounds(120, 30, 134, 28);
 		getContentPane().add(csvField);
 		csvField.setColumns(10);
 		csvLabel.setLabelFor(csvField);
 
 		picField = new JTextField();
 		picField.setBounds(120, 70, 134, 28);
 		getContentPane().add(picField);
 		picField.setColumns(10);
 		picLabel.setLabelFor(picField);
 
 		final JButton csvButton = new JButton("选取...");
 		csvButton.setBounds(266, 30, 80, 29);
 		getContentPane().add(csvButton);
 		final JFileChooser csvChooser = new JFileChooser();
 		csvChooser.setFileFilter(new FileFilter() {
 			@Override
 			public boolean accept(File file) {
 				boolean flag = false;
 				if (file.isDirectory() || file.toString().endsWith(".csv"))
 					flag = true;
 				return flag;
 			}
 
 			@Override
 			public String getDescription() {
 				return "CSV 逗号分割的文件";
 			}
 		});
 		csvButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int result;
 				if (mode == 0)
 					result = csvChooser.showOpenDialog(Port.this);
 				else
 					result = csvChooser.showSaveDialog(Port.this);
 				if (result == JFileChooser.APPROVE_OPTION)
 					csvField.setText(csvChooser.getSelectedFile().toString());
 			}
 		});
 
 		final JButton picButton = new JButton("选取...");
 		picButton.setBounds(266, 70, 80, 29);
 		getContentPane().add(picButton);
 		final JFileChooser picChooser = new JFileChooser();
 		picChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 		picButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int result;
 				if (mode == 0)
 					result = picChooser.showOpenDialog(Port.this);
 				else
 					result = picChooser.showSaveDialog(Port.this);
 				if (result == JFileChooser.APPROVE_OPTION)
 					picField.setText(picChooser.getSelectedFile().toString());
 			}
 		});
 
 		progressBar = new JProgressBar();
 		progressBar.setBounds(30, 130, 160, 20);
 		getContentPane().add(progressBar);
 
 		final JButton button = new JButton("开始");
 		button.setBounds(230, 126, 117, 29);
 		getContentPane().add(button);
 		getRootPane().setDefaultButton(button);
 		button.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				csvFile = new File(csvField.getText());
 				picDirectory = new File(picField.getText());
 				if (mode == 0) {
 					if (!csvFile.exists() || csvFile.isDirectory()
 							|| !picDirectory.isDirectory()) {
 						JOptionPane.showMessageDialog(Port.this, "请选择正确的文件",
 								"注意", JOptionPane.INFORMATION_MESSAGE);
 						return;
 					}
 				} else if (csvFile.exists()
 						|| (picDirectory.exists() && !picDirectory
 								.isDirectory())
 						|| csvFile.getPath().equals(picDirectory.getPath())) {
 					JOptionPane.showMessageDialog(Port.this, "文件已存在", "注意",
 							JOptionPane.INFORMATION_MESSAGE);
 					return;
 				}
 				button.setEnabled(false);
 				csvButton.setEnabled(false);
 				picButton.setEnabled(false);
 				csvField.setEnabled(false);
 				picField.setEnabled(false);
 				if (mode == 0)
 					new Import().start();
 				else
 					new Export().start();
 			}
 		});
 
 		setVisible(true);
 	}
 
 	void finish() {
 		JOptionPane.showMessageDialog(this, NAME[mode] + "完成！", "成功",
 				JOptionPane.INFORMATION_MESSAGE);
 		dispose();
 	}
 }
