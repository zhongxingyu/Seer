 /**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 */
 
 package truelauncher.launcher;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 
 import truelauncher.config.AllSettings;
 import truelauncher.gcomponents.TButton;
 import truelauncher.gcomponents.TLabel;
 import truelauncher.main.GUI;
 
 @SuppressWarnings("serial")
 public class LauncherUpdateDialog  extends JDialog {
 	
	private String labelimage = "images/labelbar.png";
 
 	Object result;
 	int w = 250; int h = 90;
 	public TButton ldownload;
 	public JProgressBar lpbar;
 	public TLabel lstatus;
 	TButton later;
 	JPanel panel;
 	GUI parent;
 	
 	private LauncherUpdateDialog thisclass = this;
 	
 	public LauncherUpdateDialog(GUI parent) {
         super(parent.f, true);
         this.parent = parent;
         this.setUndecorated(true);
 		this.setLayout(null);
     }
 	
 	public void open()
 	{
 		setResizable(false);
 		setSize(w,h);
 		setLocationRelativeTo(parent);
 		initUI();
 		setVisible(true);
 	}
 	
 	
 	private void initUI()
 	{
 		panel = new JPanel();
 		panel.setBounds(0, 0, this.w, this.h);
 		panel.setLayout(null);
 		panel.setBorder(BorderFactory.createEtchedBorder(Color.GRAY, Color.GRAY));
 		this.add(panel);
 		
      	lstatus = new TLabel();
      	lstatus.setBounds(2,2,w-4,24);
      	lstatus.setBackgroundImage(GUI.class.getResourceAsStream(labelimage));
      	lstatus.setText("Доступно обновление лаунчера");
      	lstatus.setHorizontalAlignment(TLabel.CENTER);
      	panel.add(lstatus);
      	
   	  	lpbar = new JProgressBar();
   	  	lpbar.setBounds(2,26,w-4, 14);
      	panel.add(lpbar);
   	  	
   	  	
     	ldownload = new TButton();
     	ldownload.setText("Обновить");
     	ldownload.setBounds(2, 40, w-4, 25);
     	ldownload.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
             	lstatus.setText("Скачиваем обновление");
             	ldownload.setEnabled(false);
             	later.setEnabled(false);
            		new LauncherUpdateThread(thisclass, AllSettings.getLauncherWebUpdateURLFolder()+"Launcher.jar").start();
             }
         });
     	panel.add(ldownload);
 
     	
     	
     	later = new TButton();
     	later.setText("Позже");
     	later.setBounds(2,65,w-4,23);
     	later.addActionListener(new ActionListener() {
  	               @Override
  	               public void actionPerformed(ActionEvent e) {
  	            	   parent.f.getGlassPane().setVisible(false);
  	            	   thisclass.dispose();
  	               }
     	});
     	panel.add(later);
 
 	}
 
 }
