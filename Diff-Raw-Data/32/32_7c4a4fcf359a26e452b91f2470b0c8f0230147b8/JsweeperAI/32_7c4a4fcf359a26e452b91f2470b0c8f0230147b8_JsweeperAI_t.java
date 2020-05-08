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
			inst.getRandomCell().clickCell();
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
