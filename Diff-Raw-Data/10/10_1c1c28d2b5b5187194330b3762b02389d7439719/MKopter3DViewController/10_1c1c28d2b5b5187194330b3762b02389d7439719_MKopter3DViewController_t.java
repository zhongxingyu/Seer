 package mkpc.model3D;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 
 import mkpc.comm.MKData3D;
 import mkpc.comm.MKParameter;
 import mkpc.log.LogSystem;
 
 
 /**
 * This code was edited or generated using CloudGarden's Jigloo
 * SWT/Swing GUI Builder, which is free for non-commercial
 * use. If Jigloo is being used commercially (ie, by a corporation,
 * company or business for any purpose whatever) then you
 * should purchase a license for each developer using Jigloo.
 * Please visit www.cloudgarden.com for details.
 * Use of Jigloo implies acceptance of these licensing terms.
 * A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
 * THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
 * LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
 */
 public class MKopter3DViewController extends JPanel
 {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	
 	private JFrame externWindow;		// need to show the 3DView in an extra window
 	private MKopter3DView view3D;
 	private JPanel menuPanel;			// holds the menu buttons
 	
 	private JToggleButton btn_coordinateLines;
 	private JButton btn_externWindow;
 	private JToggleButton btn_playPause;
 	
 	private Rectangle lastBounds = null;
 	
 	public MKopter3DViewController()
 	{
 		this.initGUI();
 	}
 	
 	private void initGUI()
 	{
 		try
 		{
 			this.setLayout(new BorderLayout());
 			this.setPreferredSize(new java.awt.Dimension(285, 231));
 			
 			{
 				view3D = new MKopter3DView();
 				this.add(view3D, BorderLayout.CENTER);
 			}
 			{
 				GridLayout menuLayout = new GridLayout(4,1);
 				menuLayout.setHgap(0);
 				menuLayout.setVgap(0);
 				menuLayout.setColumns(4);
 				menuLayout.setRows(1);
 				menuPanel = new JPanel(menuLayout);
 				this.add(menuPanel, BorderLayout.SOUTH);
 				menuPanel.setSize(this.getSize().width, 30);
 				menuPanel.setBackground(new Color(255,255,255));
 				{
 					btn_playPause = new JToggleButton();
 					menuPanel.add(btn_playPause);
 					btn_playPause.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent evt) {
 							btn_playPauseActionPerformed(evt);
 						}
 					});
 					btn_playPause.setIcon(new ImageIcon(getClass().getResource("resources/button.play.20x20.png")));
 					btn_playPause.setSelectedIcon(new ImageIcon(getClass().getResource("resources/button.pause.20x20.png")));
 					btn_playPause.setSelected(true);
 				}
 				{
 					JButton btn = new JButton();
 					menuPanel.add(btn);
 					btn.setText("stop");
 					btn.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent evt) {
 							btn_stopActionPerformed(evt);
 						}
 					});
 				}
 				{
 					JButton btn = new JButton();
 					menuPanel.add(btn);
 					btn.setText("grid");
 				}
 				{
 					btn_coordinateLines = new JToggleButton();
 					menuPanel.add(btn_coordinateLines);
 					btn_coordinateLines.setIcon(new ImageIcon(getClass().getResource("resources/button.coordinateLines.20x20.png")));
 					btn_coordinateLines.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent evt) {
 							btn_coordinateLinesActionPerformed(evt);
 						}
 					});
 				}
 				{
 					btn_externWindow = new JButton();
 					menuPanel.add(btn_externWindow);
 					btn_externWindow.setIcon(new ImageIcon(getClass().getResource("resources/button.fullscreen.20x20.png")));
 					btn_externWindow.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent evt) {
 							btn_externWindowActionPerformed(evt);
 						}
 					});
 				}
 			}
 		}
 		catch(Exception e)
 		{
 			LogSystem.addLog("Error on creating 3DViewController!");
 		}
 	}
 	
 	public void stop()
 	{
 		view3D.stopAnimation();
 	}
 	
 	public void start()
 	{
 		view3D.startAnimation();
 	}
 	
 	public void btn_playPauseActionPerformed(ActionEvent evt)
 	{
 		if(btn_playPause.isSelected())
 		{
 			view3D.startAnimation();
 		}
 		else
 		{
 			view3D.stopAnimation();
 		}
 	}
 	
 	public void btn_stopActionPerformed(ActionEvent evt)
 	{
 		view3D.stopAnimation();
 	}
 	
 	public void btn_coordinateLinesActionPerformed(ActionEvent evt)
 	{
 		if(btn_coordinateLines.isSelected())
 		{
 			view3D.setCoordianteLineHidden(false);
 		}
 		else
 		{
 			//btn_coordinateLines.setSelected(true);
 			view3D.setCoordianteLineHidden(true);
 		}
 	}
 	
 	public void btn_externWindowActionPerformed(ActionEvent evt)
 	{
 		LogSystem.CLog("Window is " + externWindow);
 		if(externWindow == null)
 		{
 			lastBounds = this.getBounds();
 			LogSystem.CLog(lastBounds.toString());
 			mkpc.app.Application.sharedApplication().getTopPanel().remove(this);
 			mkpc.app.Application.sharedApplication().getTopPanel().revalidate();
 			mkpc.app.Application.sharedApplication().getTopPanel().repaint();
 			
 			externWindow = new JFrame();
 			externWindow.setSize(400, 300);
 			externWindow.setBounds(0, 0, 400, 300);
 			externWindow.getContentPane().add(this);
 			externWindow.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 			externWindow.setVisible(true);
 			externWindow.pack();
 		
 			btn_externWindow.setIcon(new ImageIcon(getClass().getResource("resources/button.leavefullscreen.20x20.png")));
 		}
 		else
 		{		
 			externWindow.setVisible(false);
 			externWindow.remove(this);
 			externWindow.repaint();
 			externWindow = null;
 
 			mkpc.app.Application.sharedApplication().getTopPanel().add(this);
 			this.setBounds(lastBounds);
 			this.setSize(lastBounds.width, lastBounds.height);
 			mkpc.app.Application.sharedApplication().getTopPanel().revalidate();
 			mkpc.app.Application.sharedApplication().getTopPanel().repaint();
 			this.repaint();
 
 			btn_externWindow.setIcon(new ImageIcon(getClass().getResource("resources/button.fullscreen.20x20.png")));
 		}
 	}
 	
 	public void dispatchIncomingData(char[] data)
 	{
 		// save data to parameters
 		MKData3D data3D = MKParameter.shardParameter().getData3D();
 		
 		LogSystem.CLog("Data0: "+Integer.toString(data[0])+" Data0: "+Integer.toString(data[1]));
 		
		data3D.setNick((short) (((data[1] << 8) & 0xFF00 ) | data[0]));
		data3D.setRoll((short) (((data[3] << 8) & 0xFF00 ) | data[2]));
		data3D.setCompass((short) (((data[5] << 8) & 0xFF00 ) | data[4]));
		
		LogSystem.CLog("Nick: "+data3D.nick());
 		LogSystem.CLog("Roll: "+data3D.roll());
 		LogSystem.CLog("Compass"+data3D.compass());
 	}
 }
 
 
 
 
 
 
 
 
 
 
 
 
