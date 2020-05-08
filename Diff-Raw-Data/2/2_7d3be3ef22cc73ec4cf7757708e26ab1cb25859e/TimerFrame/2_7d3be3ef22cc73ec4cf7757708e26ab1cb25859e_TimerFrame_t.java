 package ru.ifmo.neerc.timer;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.util.Calendar;
 import java.util.Date;
 
 import javax.sql.rowset.spi.SyncResolver;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 
 
 import pcms2.services.site.Clock;
 import ru.ifmo.neerc.gui.ImagePanel;
 
 
 public class TimerFrame extends JFrame {
 	public static final int BEFORE = 0;
 	public static final int RUNNING = 1;
 	public static final int FROZEN = 3;
 	public static final int PAUSED = 4;
 	public static final int LEFT5MIN = 5;
 	public static final int LEFT1MIN = 6;
 	public static final int OVER = 7;
 	
 	private JLabel timeLabel = new JLabel();
 	private ImagePanel panelBgImg;
 	private Integer status;
 	private Long cTime, cDelta;
 	private Color[] palette;
 
 	TimerFrame() {
 		super("PCMS2 Timer");
 		palette = new Color[8];
 		for (int i = 0; i < 8; ++i)	{
 			palette[i] = Color.BLACK;
 		}
 		
 		this.setUndecorated(true);
 		setBounds(0, 0, 1024, 768);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Container con = this.getContentPane();
 		timeLabel.setFont(new Font("Calibri", Font.BOLD, 350));
 		timeLabel.setHorizontalAlignment(JLabel.CENTER);
 		timeLabel.setVerticalAlignment(JLabel.CENTER);
 		timeLabel.setForeground(Color.green);
 		con.setBackground(Color.BLACK);
 
 		timeLabel.setText("0:00:00");
 		
 		Toolkit tk = Toolkit.getDefaultToolkit();
 		int xSize = ((int) tk.getScreenSize().getWidth());
 		int ySize = ((int) tk.getScreenSize().getHeight());
 		setSize(xSize, ySize);
 
 		con.setLayout(null);
 		panelBgImg = new ImagePanel();
 		con.add(panelBgImg);
 		
 		panelBgImg.setLayout(new BorderLayout());
 		panelBgImg.add(timeLabel, BorderLayout.CENTER);
 		panelBgImg.setBounds(0, 0, xSize, ySize);
 		cTime = Long.valueOf(0);
 		cDelta = Long.valueOf(0);
 		status = Integer.valueOf(0);
 		
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				TimerFrame.this.setVisible(true);
 				
 			}
 			
 		}).start();
 		
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				long timestamp = System.currentTimeMillis();
 				while (true) {
 					long nts = System.currentTimeMillis();
 					long diff = nts - timestamp;
 					timestamp = nts;
 					long dtime = 0;
 					synchronized (cDelta) {
 						synchronized (cTime) {
							long correction = Math.min(cDelta, diff / 2);
 							synchronized (status) {
 								if (status == Clock.RUNNING) {
 									cTime = cTime - diff + correction;
 								}
 							}
 							
 							cDelta = cDelta - correction;
 							dtime = cTime;
 						}
 					}
 					
 					dtime /= 1000;
 					int seconds = (int) (dtime % 60);
 					dtime /= 60;
 					int minutes = (int) (dtime % 60);
 					dtime /= 60;
 					int hours = (int) dtime;
 					
 					
 					
 					String text = null;
 					Color c = null;
 					synchronized (status) {
 						switch (status) {
 						case Clock.BEFORE:
 							c = palette[BEFORE];
 							break;
 						case Clock.RUNNING:
 							c = palette[RUNNING];
 							break;
 						case Clock.OVER:
 							c = palette[OVER];
 							break;
 						case Clock.PAUSED:
 							c = palette[PAUSED];
 							break;
 						}
 					}
 					
 					if (minutes <= 1) {
 						c = palette[LEFT1MIN];
 					} else if (minutes <= 5) {
 						c = palette[LEFT5MIN];
 					}
 					
 					if (hours > 0) {
 						text = String.format("%d:%02d:%02d", hours, minutes, seconds);
 					} else if (minutes > 0) {						 
 						text = String.format("%02d:%02d", minutes, seconds);
 					} else {
 						text = String.format("%d", seconds);
 					}
 					
 					timeLabel.setText(text);
 					timeLabel.setForeground(c);
 					
 					try {
 						Thread.sleep(100);
 					} catch (InterruptedException e) {
 							return;
 					}
 				}
 
 			}
 			
 		}).start();
 		
 	}
 	
 	public void setStatus(int status) {
 		synchronized (this.status) {
 			this.status = Integer.valueOf(status);
 			this.repaint();
 		}
 	}
 		
 	public void sync(long time) {
 		synchronized (this.cDelta) {
 			synchronized (this.cTime) {
 				cDelta = time - this.cTime;
 				if (cDelta >= 100000) {
 					cDelta = Long.valueOf(0);
 					this.cTime = time;
 				}
 				this.repaint();
 			}
 		}
 	}
 }
