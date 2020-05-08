 package sim.demos;
 
 import java.awt.Rectangle;
 
 import sim.functions.Get;
 import sim.functions.Pop;
 import sim.functions.Push;
 import sim.structures.Stack;
 import sim.structures.Variable;
 
 public class RailwayPostfix {
 	public static void main(String[] args) {
 
 		new RailwayPostfix();
 
 	}
 	public RailwayPostfix(){
 		DemoFrame frame = new DemoFrame("infix to postfix using railway algorith");		
 		
 		Stack s = new Stack(new Rectangle(325,300, 70, 200));
 		Variable si = new Variable(new Rectangle(390,170,60,50),"#",false);
 		Variable so = new Variable(new Rectangle(275,170,60,50),"",false);
 
 		Variable infix = new Variable(new Rectangle(500,170,200,50),"a+b*c-e",true);
 		
		Get getInfix = new Get(new Rectangle(450,170,50,50),infix,si,true,true);
 		
 		Get getSi = new Get(new Rectangle(335,170,55,50),si,so,true,true);
 		
 		Variable postfix = new Variable(new Rectangle(25,170,200,50),"                               ",true);
 		
 		Get movePostfix = new Get(new Rectangle(225,170,50,50),so,postfix,true,false);
 		
 		Pop popSi = new Pop(new Rectangle(405,300,70,25),s, si);
 		Push pushSi = new Push(new Rectangle(405,350,70,25),si, s);
 		Pop popSo = new Pop(new Rectangle(225,300,70,25),s, so);
 		Push pushSo = new Push(new Rectangle(225,350,70,25),so, s);
 		
 		frame.add(popSi);
 		frame.add(pushSi);
 		frame.add(popSo);
 		frame.add(pushSo);
 		frame.add(si);
 		frame.add(so);
 		frame.add(s);
 		frame.add(infix);
 		frame.add(postfix);
 		frame.add(getInfix);
 		frame.add(getSi);
 		frame.add(movePostfix);
 		
 		frame.validate();
 //		frame.repaint();
 	}
 	
 }
