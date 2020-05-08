 package sim.demos;
 
 import java.awt.Rectangle;
 
 import sim.functions.Get;
 import sim.functions.Pop;
 import sim.functions.Push;
 import sim.structures.Info;
 import sim.structures.Stack;
 import sim.structures.Variable;
 
 public class RailwayPostfix {
 	public static void main(String[] args) {
 
 		new RailwayPostfix();
 
 	}
 	public RailwayPostfix(){
 		DemoFrame frame = new DemoFrame("Infix to postfix using railway algorithm", 800, 550);	
 		
 		Info title = new Info(new Rectangle(10,10,110,20),"Railway Algorithm",false);
 		
 		String text = "The Railway algorithm is useful for generating pre- and postfix expressions for easier computing. \n" +
 				"To use this demo with the infix expression given, do the following: \n\n" +
 				"" +
 				"Press the right 'Push' to Push # on to the stack. This is the 'Stack empty' testing char.\n" +
 				"Press the right 'Get' to get the first character from infix. Since this is an operand we can go ahead and press " +
 				"the middle and then the left Get to move it to the postfix textfield.\n" +
 				"Now press the right get. Since this is an operator and there are no operators on the stack, Push it to the stack.\n" +
 				"Get the next char and move the operand to the postfix area.\n" +
 				"Get the next char. Since * has a higher mathematical priority than + we push it on to the stack and get the next character.\n" +
 				"A new operand. Now get the next character\n" +
 				"Since - has a lower priority than * we pop * from the stack using the LEFT Pop button. Now push * to the postfix area\n" +
 				"+ has the same priority as - but came first. If the priority is equal, prefer popping from stack.\n" +
 				"Now push - onto the stack and see if you can figure out the rest for yourself.";
 		
		Info info = new Info(new Rectangle(10,320,700,120),text,false);
 		
		Stack s = new Stack(new Rectangle(325,150, 80, 150));
		Variable si = new Variable(new Rectangle(405,70,60,50),"#",false);
		Variable so = new Variable(new Rectangle(260,70,60,50),"",false);
 
		Variable infix = new Variable(new Rectangle(550,70,150,50),"a+b*c-e",true);
		Get getInfix = new Get(new Rectangle(480,70,55,50),infix,si,true,true);
 		
		Get getSi = new Get(new Rectangle(335,70,55,50),si,so,true,true);
 		
		Variable postfix = new Variable(new Rectangle(25,70,150,50),"                               ",true);
 		
		Get movePostfix = new Get(new Rectangle(190,70,55,50),so,postfix,true,false);
 		
		Pop popSi = new Pop(new Rectangle(435,200,70,25),s, si);
		Push pushSi = new Push(new Rectangle(435,250,70,25),si, s);
		Pop popSo = new Pop(new Rectangle(225,200,70,25),s, so);
		Push pushSo = new Push(new Rectangle(225,250,70,25),so, s);
 		
 		frame.add(popSi);
 		frame.add(info);
 		frame.add(title);
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
