 package com.denisneuling.connectfour.gui.components;
 
 import javax.swing.JPanel;
 
 import net.miginfocom.swing.MigLayout;
 
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 import com.denisneuling.connectfour.gui.MainFrame;
 import com.denisneuling.connectfour.gui.components.listener.TileActionListener;
 import com.denisneuling.connectfour.service.GridService;
 
 @Component
 /**
  * <p>ConnectFourGrid class.</p>
  *
  * @author ska
  * @version $Id: $Id
  */
 public class ConnectFourGrid extends JPanel implements InitializingBean {
 	private static final long serialVersionUID = 4883973788405021695L;
 
 	@Value("${defaults.grid.x}")
 	private String defaultXString;
 
 	@Value("${defaults.grid.y}")
 	private String defaultYString;
 	
 	@Value("${defaults.tile.x}")
 	private String defaultTileXString;
 
 	@Value("${defaults.tile.y}")
 	private String defaultTileYString;
 
 	@Autowired
 	private GridService gridService;
 
 	@Autowired
 	private TileActionListener tileActionListener;
 	
 	@Autowired
 	private MainFrame mainFrame;
 
 	private volatile MigLayout layout;
 
 	private volatile int horizontal;
 	private volatile int vertical;
 	
 	private volatile int tileHorizontal;
 	private volatile int tileVertical;
 
 	/**
 	 * <p>Getter for the field <code>horizontal</code>.</p>
 	 *
 	 * @return a int.
 	 */
 	public int getHorizontal() {
 		return horizontal;
 	}
 
 	/**
 	 * <p>Setter for the field <code>horizontal</code>.</p>
 	 *
 	 * @param horizontal a int.
 	 */
 	public void setHorizontal(int horizontal) {
 		this.horizontal = horizontal;
 	}
 
 	/**
 	 * <p>Getter for the field <code>vertical</code>.</p>
 	 *
 	 * @return a int.
 	 */
 	public int getVertical() {
 		return vertical;
 	}
 
 	/**
 	 * <p>Setter for the field <code>vertical</code>.</p>
 	 *
 	 * @param vertical a int.
 	 */
 	public void setVertical(int vertical) {
 		this.vertical = vertical;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		horizontal = Integer.parseInt(defaultXString);
 		vertical = Integer.parseInt(defaultYString);
 		
 		tileHorizontal = Integer.parseInt(defaultTileXString);
 		tileVertical = Integer.parseInt(defaultTileYString);
 
 		this.renew(this.horizontal, this.vertical);
 		
 		this.setVisible(true);
 	}
 
 	public void renew(int horizontal, int vertical) {
 		this.horizontal = horizontal;
 		this.vertical = vertical;
 		
 		this.removeAll();
 		
 		gridService.build(horizontal, vertical);
 
 		layout = new MigLayout(String.format("wrap %d", horizontal));
 		this.setLayout(layout);
 
 		for (int y = vertical-1; y >= 0; y--) {
 			for (int x = 0; x < horizontal; x++) {
 				Tile tile = new Tile(x,y,tileHorizontal,tileVertical);
 				gridService.register(tile);
 				tile.addMouseListener(tileActionListener);
 				this.add(tile);
 			}
 		}
 		this.updateUI();
 		mainFrame.pack();
 	}
 }
