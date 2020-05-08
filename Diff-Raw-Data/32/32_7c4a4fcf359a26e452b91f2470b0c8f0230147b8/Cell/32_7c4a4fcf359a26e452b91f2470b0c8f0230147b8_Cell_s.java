 package org.kroqgar78.jsweeper;
 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 
 public class Cell extends JButton implements MouseListener
 {
 	public static ImageIcon mineImage = new ImageIcon("res/mine.png");
 	public static ImageIcon flagImage = new ImageIcon("res/flag.png");
 	
 	public static final int EMPTY = 0;
 	public static final int MINE = -1;
 	
 	public Cell(int x, int y, int val, Jsweeper instance)
 	{
 		super();
 		this.x = x;
 		this.y = y;
 		this.val = val;
 		this.inst = instance;
 		
 		super.addMouseListener(this);
 	}
 	public Cell(int x, int y, Jsweeper instance)
 	{
 		this(x, y, EMPTY, instance);
 	}
 	
 	//public int getX() { return this.x; }
 	//public void setX(int x) { this.x = x; }
 	
 	//public int getY() { return this.y; }
 	//public void setY(int y) { this.y = y; }
 	
 	public int[] getPosition() { return new int[] {x, y}; }
 	/*public void setPosition(int x, int y)
 	{
 		this.x = x;
 		this.y = y;
 	}
 	public void setPosition(int[] pos) { setPosition(pos[0], pos[1]); }*/
 	
 	public int getValue() { return this.val; }
 	public void setValue(int val)
 	{
 		this.val = val;
 		super.setText("");
 		super.setIcon(null);
 	}
 	public void incrementValue()
 	{
 		if(isMine()) return; // don't do anything if it's a mine
 		this.val++;
 		//super.setText(Integer.toString(this.val));
 	}
 	
 	public boolean isMine() { return val == MINE; }
 	public void setMine(boolean mine) { setValue(mine?MINE:EMPTY); }
 	
 	public void clickCell()
 	{
 		setEnabled(false);
 		super.setIcon(null);
 		flagged = false;
 		
 		if(!clicked)
 		{
 			if(val == MINE)
 			{
 				// if no cells have been clicked yet, then regenerate the field
 				if(!inst.isAnyCellClicked())
 				{
 					inst.restartGame();
 					return;
 				}
 				super.setText("");
 				super.setIcon(mineImage);
 				inst.gameOver();
 			}
 			else if(val == EMPTY)
 			{
 				super.setText("");
 				Cell[] adjCells = inst.getAdjacentCells(x, y);
 				for( int i = 0; i < adjCells.length; i++ )
 				{
 					if( adjCells[i].getValue() >= 0 && !adjCells[i].clicked ) adjCells[i].clickCell();
 				}
 			}
 			else super.setText(Integer.toString(this.val));
			clicked = true;
 			inst.update();
 		}
 	}
 	
 	public void flagCell()
 	{
 		clicked = false;
 		flagged = true;
 		super.setText("");
 		super.setIcon(flagImage);
 	}
 	
 	public void unflagCell()
 	{
 		clicked = false;
 		flagged = false;
 		super.setText("");
 		super.setIcon(null);
 	}
 	
 	public void toggleFlag()
 	{
 		if(!flagged) flagCell();
 		else unflagCell();
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {}
 	
 	@Override
 	public void mouseEntered(MouseEvent arg0)
 	{
 		containsMouse = true;
 		//getModel().setArmed(pressed);
 		//getModel().setPressed(pressed);
 	}
 	@Override
 	public void mouseExited(MouseEvent arg0)
 	{
 		containsMouse = false;
 		//getModel().setArmed(true);
 		//getModel().setPressed(false);
 	}
 	@Override
 	public void mousePressed(MouseEvent arg0)
 	{
 		pressed = true;
 		if(clicked) // highlight other adjacent cells if already clicked
 		{
 			Cell[] adjCells = inst.getAdjacentCells(x, y);
 			for( int i = 0; i < adjCells.length; i++ )
 			{
 				if(adjCells[i].flagged) continue;
 				adjCells[i].getModel().setArmed(true);
 				adjCells[i].getModel().setPressed(true);
 			}
 		}
 		//getModel().setArmed(pressed);
 		//getModel().setPressed(pressed);
 	}
 	@Override
 	public void mouseReleased(MouseEvent arg0)
 	{
 		pressed = false;
 		//getModel().setArmed(pressed);
 		//getModel().setPressed(pressed);
 		if(clicked) // highlight other adjacent cells if already clicked
 		{
 			Cell[] adjCells = inst.getAdjacentCells(x, y);
 			for( int i = 0; i < adjCells.length; i++ )
 			{
 				adjCells[i].getModel().setArmed(false);
 				adjCells[i].getModel().setPressed(false);
 			}
 			if(containsMouse)
 			{
 				int adjFlagCount = 0;
 				for( int i = 0; i < adjCells.length; i++ )
 				{
 					if( adjCells[i].flagged ) adjFlagCount++;
 				}
 				if(adjFlagCount == this.val)
 				{
 					for( int i = 0; i < adjCells.length; i++ )
 					{
 						if(adjCells[i].flagged) continue;
 						adjCells[i].clickCell();
 					}
 				}
 			}
 		}
 		else
 		{
 			if(!containsMouse) return;
 			System.out.println(clicked);
 			System.out.println("Button pressed at (" + x + "," + y + ")" );
 			if(arg0.getButton() == MouseEvent.BUTTON1 && !flagged) clickCell();
 			if(arg0.getButton() == MouseEvent.BUTTON3 && !clicked) toggleFlag();
 			System.out.println(clicked);
 		}
 	}
 	
 	boolean containsMouse, clicked, flagged, pressed = false;
 	private int x, y;
 	private int val;
 	private Jsweeper inst;
 }
