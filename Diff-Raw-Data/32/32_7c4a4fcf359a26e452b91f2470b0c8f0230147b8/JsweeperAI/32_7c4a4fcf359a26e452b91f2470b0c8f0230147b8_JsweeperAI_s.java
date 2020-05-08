 package org.kroqgar78.jsweeper;
 
 import java.util.Random;
 
 public class JsweeperAI
 {
 	public JsweeperAI(Jsweeper inst)
 	{
 		this.inst = inst;
 		
 		rand = new Random(System.currentTimeMillis());
 	}
 	
 	public void doStep()
 	{
 		// check if any cells are clicked at all; else, click a random one
 		if(!inst.isAnyCellClicked())
 		{
			int x = rand.nextInt(inst.getSize()[0]);
			int y = rand.nextInt(inst.getSize()[1]);
			/*do
			{
				x = rand.nextInt(inst.getSize()[0]);
				y = rand.nextInt(inst.getSize()[1]);
			}
			while(!inst.getCells()[x][y].isMine());*/
			inst.getCells()[x][y].clickCell();
 		}
 	}
 	
 	private Jsweeper inst;
 	private Random rand;
 	
 	public static void main(String[] args)
 	{
 		Jsweeper inst = new Jsweeper(16, 16, 40);
 		JsweeperAI ai = new JsweeperAI(inst);
 		ai.doStep();
 	}
 }
