 import java.io.FileWriter;
 
 import java.io.*;
 import java.util.*;
 
 public class EricTest {
 
 	/**
 	 * @param args
 	 * @throws Exception 
 	 */
 	 
 	public static XAALScripter scripter;
 	public static void main(String[] args) throws Exception 
 	{
 		scripter = new XAALScripter();
 		
 		Scope global = new Scope("Global", "blue", true);
 		Variable var1 = new Variable("x", 3, false);
 		Variable var2 = new Variable("y", 12, false);
 		var2.addCopy();
 		global.addVariable(var1);
 		global.addVariable(var2);
 		Scope main = new Scope("main", "red", false);
 		main.setHidden(true);
 		Variable var3 = new Variable("a", var1, true);
 
 		main.addVariable(var3);
 		global.addScope(main);
 		
 		Scope foo = new Scope("foo", "green", false);
 		Variable var4 = new Variable("q", 0, true);
 		Variable var5 = new Array("p", new int[]{1,3,4}, false);
 		foo.setHidden(true);
 		foo.addVariable(var4);
 		foo.addVariable(var5);
 		
 		global.addScope(foo);
 		global.draw(scripter);
 		
 		
 		//Start writing slides
 		
 		scripter.startSlide();
 		scripter.startPar();
 			showScope(main);
 			showVar(var3);
 		scripter.endPar();	
 		scripter.endSlide();
 		
 		scripter.startSlide();
 		scripter.startPar();
 			showScope(foo);
 			showVar(var4);
 			showVar(var5);
 		scripter.endPar();
 		scripter.endSlide();
 		
 		scripter.startSlide();
 		scripter.startPar();
 			//Move a copy down
 
 
 			moveCopy(var2, var3);
 
 
 
 
 		scripter.endPar();
 		
 		scripter.endSlide();
 		
 		
 		FileWriter writer = new FileWriter("C:\\Users\\Eric\\Desktop\\tomxaal.xaal");
 		
 		writer.write(scripter.toString());
 		
 		writer.close();
 	}
 	
 	public static void moveCopy(Variable var1, Variable var2)
 	{
 		ArrayList<String> ids = var1.getIds();
 		String lastCopy = ids.get(ids.size() -1 );
 		System.out.println("Moving " + lastCopy);
 		int startX = var1.getXPos();
 		int startY = var1.getYPos();
 		
 		int endX = var2.getXPos();
		int endY = var2.getYPos();
 		
 		int moveX = startX - endX;
 		int moveY = startY - endY;
 		System.out.println("Startx: " + startX + " endx: " + endX);
 		System.out.println("Starty: " + startY + " endy: " + endY);
 		System.out.println("Moving x: " + moveX + " and Y: " + moveY);
 		try
 		{
 			scripter.addTranslate(-moveX, -moveY, lastCopy);
 		}
 		catch (Exception e)
 		{
 			System.out.println(e);
 		}
 		
 		
 	}
 	public static void showScope(Scope s)
 	{
 			ArrayList<String> ids = s.getIds();
 			for (String id : ids)
 			{
 				try
 				{
 					scripter.addShow(id);
 				}
 				catch (Exception e)
 				{
 					System.out.println(e);
 				}
 			}
 
 	}
 	
 	public static void showVar(Variable v)
 	{
 		ArrayList<String> ids = v.getIds();
 		for (String id : ids)
 		{
 			try
 			{
 				scripter.addShow(id);
 			}
 			catch (Exception e)
 			{
 				System.out.println(e);
 			}
 		}
 	}
 
 }
 
