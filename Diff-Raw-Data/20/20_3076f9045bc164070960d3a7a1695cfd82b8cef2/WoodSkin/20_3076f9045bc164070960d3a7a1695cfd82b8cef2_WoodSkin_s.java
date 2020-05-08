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
 		super("wood", boardSize);
 	}
 
 	public BoardLayout getLayout(final Dimension size)
 	{
		return this.createLayout(size);
 	}
 
 	public void paintBoard(final Graphics g, final Component parent)
 	{
 		Dimension size = parent.getSize();
 		BoardLayout l = this.getLayout(size);
 		
 		double hfactor = size.height / BaseSkin.DESIGN_HEIGHT;
 
 		for (int i = 0; i < this.boardSize.width + 1; i++)
 		{
 			int recty = this.asInt(this.boardLayout, "y");
 			int recth = this.asInt(this.boardLayout, "height");
 
 			int sx = l.pieceSizeX;
 			int gap = l.pieceGapX;
 			int x = (int) (l.boardRect.x + i*(sx+gap));
 			int ry = (int) (recth * hfactor);
 			int y = (int) (recty * hfactor);
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
 		int recty = this.asInt(this.boardLayout, "y");
 		int recth = this.asInt(this.boardLayout, "height");
 
 		int col = (int) (this.boardSize.getHeight() - item.getPosition().col() - 1);
 		int y = (int) ((recty + recth) * hfactor) - l.pieceSizeY/4;
 		y -= col * (l.pieceSizeY + l.pieceGapY - overlap) + oy;
 
 		int x = l.boardRect.x + item.getPosition().row()*(l.pieceSizeX + l.pieceGapX);
 
 		this.paintPiece(g, parent, item.getPiece(), new Rectangle(x, y, l.pieceSizeX, l.pieceSizeY), item.getType());
 	}
 }
