 package com.example.kindle.winningfour.boardgame;
 
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.kwt.ui.KWTProgressBar;
 
 import com.amazon.kindle.kindlet.ui.KLabel;
 import com.example.kindle.boardgame.GameEvent;
 import com.example.kindle.utils.GameClock;
 import com.example.kindle.winningfour.App;
 import com.example.kindle.winningfour.AppResources;
 import com.example.kindle.winningfour.boardgame.BoardItem;
 import com.example.kindle.winningfour.gui.GameImage;
 import com.example.kindle.winningfour.options.OptionsFactory;
 import com.example.kindle.winningfour.skins.BoardLayout;
 
 public class GameView extends Container
 {
 	public GameView()
 	{
 		App.log("GameView::GameView");
 		this.setLayout(null);
 
 		this.gameSelector = new GameImage(null);
 		this.statusLabel = new KLabel("", KLabel.CENTER);
 		this.statusLabel.setFont(AppResources.getFont(AppResources.ID_FONT_GAME_STATUS));
         this.progressBar = new KWTProgressBar(GameClock.RESOLUTION);
         this.progressBar.setLabelStyle(KWTProgressBar.STYLE_NONE);
 
 		// Add items in z-order: top items first, to bottom items last
 		this.add(this.gameSelector);
 		this.add(this.statusLabel);
 		this.add(this.progressBar);
 
 		this.gameSelector.setFocusable(false);
 
 		this.setFocusable(true);
 	    this.setFocusTraversalKeysEnabled(false);
 		
 		App.log("GameView::GameView done");
 	}
 
 	public void doLayout()
 	{
 		App.log("GameView::doLayout for " + this.getSize());
 		final Dimension size = this.getSize();
 
		// TODO: layout reset
		this.layout = AppResources.getSkin().getLayout(size);

 		if (this.layoutSize != null && this.layoutSize.equals(size))
 		{
 			App.log("GameView::doLayout early exit (same size)");
 			return;
 		}
 
 		this.layoutSize = size;
 
 		this.gameSelector.setBounds(size.width/2 - this.layout.pieceSizeX/2, this.layout.selectorY,
 				this.layout.pieceSizeX, this.layout.pieceSizeX);
 		this.gameSelector.setImage(AppResources.getImage("selector.png", this,
 				this.layout.pieceSizeX, this.layout.pieceSizeX));
 		
 		this.gameSelector.setLocation(this.layout.boardRect.x +
 				this.selectedRow*(this.layout.pieceSizeX + this.layout.pieceGapX),
 				this.layout.selectorY);
 
 		Dimension sz = this.statusLabel.getPreferredSize();
 		this.statusLabel.setBounds(0, this.layout.labelY, size.width, sz.height);
 
         this.progressBar.setWidth(size.width);
         Dimension pz = this.progressBar.getPreferredSize();
 		this.progressBar.setBounds(10, size.height - pz.height, size.width - 20, pz.height);
 
 		App.log("GameView::doLayout done");
 	}
 	
 	public void reset()
 	{
 		this.layoutSize = null;
 	}
 
 	public void paint(Graphics g)
 	{
 		App.log("GameView::paint in clipBounds " + g.getClipBounds());
 
 		this.gameSelector.setLocation(this.layout.boardRect.x +
 				this.selectedRow*(this.layout.pieceSizeX + this.layout.pieceGapX),
 				this.layout.selectorY);
 
 		this.statusLabel.setText(this.getStatusString());
 
 		super.paint(g);
 		AppResources.getSkin().paintBoard(g, this);
 		this.paintItems(this.items, g);
 	}
 	
 	public void paintItems(final ArrayList items, Graphics g)
 	{
 		Iterator iter = items.iterator();
 		while(iter.hasNext())
 		{
 			AppResources.getSkin().paintBoardItem(g, this, (BoardItem)iter.next());
 		}
 	}
 
 	public void setItems(final ArrayList items)
 	{
 		this.items = items;
 		this.repaint();
 	}
 
 	public void setSelectedRow(int row)
 	{
 		if (row == -1)
 		{
 			this.gameSelector.setVisible(false);
 		}
 		else
 		{
 			this.selectedRow = row;
 			this.gameSelector.setVisible(true);
 		}
 
 		this.gameSelector.repaint();
 	}
 
 	public void setStatus(final int currentPlayerId, int status)
 	{
 		this.status = status;
 		this.currentPlayerId = currentPlayerId;
 		this.statusLabel.repaint();
 	}
 	
 	private String getStatusString()
 	{
 		String result = "";
 		String actor = (new OptionsFactory()).createSkin().getPlayerNames()[this.currentPlayerId];
 		if (this.status == GameEvent.WIN)
 		{
 			result = actor + App.bundle.getString(AppResources.KEY_GAME_WIN);			
 		}
 		else if (this.status == GameEvent.DRAW)
 		{
 			result = App.bundle.getString(AppResources.KEY_GAME_DRAW);
 		}
 		else if (this.status == GameEvent.CONTINUE)
 		{
 			result = actor + App.bundle.getString(AppResources.KEY_GAME_TURN);
 		}
 		return result;
 	}
 	
 	public void setProgressTicks(int percents)
 	{
 		this.progressBar.setCurrentTick(percents);
 		this.progressBar.repaint();
 	}
 
 	public int getProgressPercents()
 	{
 		return this.progressBar.getCurrentTick();
 	}
 
 	public void destroy()
 	{
 		App.log("GameView::destroy");
 
 		this.removeAll();
 
 		this.gameSelector.destroy();
 		this.gameSelector = null;
 
 		this.statusLabel = null;
 
 		this.items.clear();
 		this.items = null;
 		
 		this.layoutSize = null;
 
 		App.log("GameView::destroy done");
 	}
 
 	private static final long serialVersionUID = 1;
 	private GameImage gameSelector;
 	private KLabel statusLabel;
 	private Dimension layoutSize;
 	private ArrayList items;
 	private int selectedRow;
 	private KWTProgressBar progressBar;
 	
 	private int status;
 	private int currentPlayerId;
 
 	private BoardLayout layout;
 }
