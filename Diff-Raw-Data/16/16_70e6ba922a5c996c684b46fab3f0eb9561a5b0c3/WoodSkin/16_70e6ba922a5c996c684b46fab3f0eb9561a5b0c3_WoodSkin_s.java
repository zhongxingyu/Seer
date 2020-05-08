 package com.example.kindle.winningfour.skins.wood;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 
 import com.example.kindle.winningfour.boardgame.BoardItem;
 import com.example.kindle.winningfour.skins.BaseSkin;
 import com.example.kindle.winningfour.skins.BoardLayout;
 
 public class WoodSkin extends BaseSkin
 {
 	public WoodSkin(final Dimension boardSize)
 	{
 		super(boardSize);
 		this.boardRect = new Rectangle(100, 210, 400, 300);
 	}
 
 	public BoardLayout getLayout(final Dimension size)
 	{
 		return this.createLayout(size, this.boardRect, this.selectorY, this.pieceGapX);
 	}
 
 	public void paintBoard(final Graphics g, final Component parent)
 	{
 		Dimension size = parent.getSize();
 		BoardLayout l = this.getLayout(size);
 		
 		double hfactor = size.height / BaseSkin.DESIGN_HEIGHT;
 
 		for (int i = 0; i < this.boardSize.width + 1; i++)
 		{
 			int sx = l.pieceSizeX;
 			int gap = l.pieceGapX;
 			int x = (int) (l.boardRect.x + i*(sx+gap));
 			int ry = (int) (this.boardRect.height * hfactor);
 			int y = (int) (this.boardRect.y * hfactor);
 			int rx = 2*sx/3;
 
 			this.paintImage(g, parent, "stick.png", new Rectangle(x-gap-rx+rx/2, y, rx, ry));
 		}
 	}
 
 	public void paintBoardItem(final Graphics g, final Component parent, final BoardItem item)
 	{
 		Dimension size = parent.getSize();
 		BoardLayout l = this.getLayout(size);
 
 		int overlap = l.pieceSizeY/8;
 		int oy = (overlap - 1) * this.boardSize.height;
 
 		double hfactor = size.height / BaseSkin.DESIGN_HEIGHT;
 
		int y = (int) (this.boardRect.height * hfactor - this.boardSize.height * l.pieceSizeY);
		y += item.getPosition().col()*(l.pieceSizeY + l.pieceGapY - overlap) + oy;
 		int x = l.boardRect.x + item.getPosition().row()*(l.pieceSizeX + l.pieceGapX);
 
 		this.paintPiece(g, parent, item.getPiece(), new Rectangle(x, y, l.pieceSizeX, l.pieceSizeY));
 	}
 
 	public String getName()
 	{
 		return "wood";
 	}
 
 	private int selectorY = 600;
 	private Rectangle boardRect;
 
 	private int pieceGapX = 4;
 }
