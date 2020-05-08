 package org.kroqgar78.jsweeper;
 
 import java.util.Random;
 
 public class JsweeperAI
 {
 	public JsweeperAI(Jsweeper inst)
 	{
 		this.inst = inst;
 		
 		rand = new Random(System.currentTimeMillis());
 	}
 	
 	public boolean doStep()
 	{
 		if(inst.isAnyCellClicked())
 		{
 			Cell[] clickedCells = inst.getClickedCells();
 			// do some checks on clicked cells
 			for( int i = 0; i < clickedCells.length; i++ )
 			{
 				if(clickedCells[i].getValue() == Cell.EMPTY) continue;
 				int x = clickedCells[i].getPosition()[0];
 				int y = clickedCells[i].getPosition()[1];
 				System.out.println(i+" at ("+x+","+y+")");
				Cell[] adjCells = inst.getAdjacentCells(x, y);
				Cell[] unclickedCells = Jsweeper.getUnclickedCells(adjCells);
 				// click if enough flags are present
 				if(inst.getAdjacentFlagCount(x, y) == clickedCells[i].getValue())
 				{
					for( int j = 0; j < unclickedCells.length; j++ )
					{
						if(unclickedCells[j].flagged) continue;
						unclickedCells[j].clickCell();
					}
 					return true;
 				}
 				// flag if there are enough adjacent cells
 				if(unclickedCells.length == clickedCells[i].getValue())
 				{
 					for( int j = 0; j < unclickedCells.length; j++ )
 					{
 						unclickedCells[j].flagCell();
 					}
 					return true;
 				}
 			}
 			
 		}
 		else
 		{
 			Cell randCell = inst.getRandomCell();
 			int x = randCell.getPosition()[0];
 			int y = randCell.getPosition()[1];
 			System.out.println("Random cell is mine");
 			inst.getRandomCell().clickCell();
 			System.out.println("Click random cell at ("+x+","+y+")");
 		}
 		return false;
 	}
 	
 	private Jsweeper inst;
 	private Random rand;
 	
 	public static void main(String[] args)
 	{
 		Jsweeper inst = new Jsweeper(16, 16, 40);
 		JsweeperAI ai = new JsweeperAI(inst);
 		ai.doStep();
 		while(ai.doStep());
 	}
 }
