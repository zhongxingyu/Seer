 /**
  * LEDNumPanel_Demo
  * @version 1.0
  * @created 7-6-2011
  * @updated 7-19-2011
  * @author Corbin Tarrant
  * @site iamcorbin.net
  * @email Corbin@IAmCorbin.net
  *
  * This is a simple component that will simulate an 8-segment LED number panel
  * 
  */
 
 package net.iamcorbin.myComponents;
 
 import javax.swing.JButton;
 import javax.swing.SwingUtilities;
 import javax.swing.JPanel;
 import javax.swing.BorderFactory;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics; 
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 /**
  * The main class which handles the creation of the panel along with
  * optional buttons for value manipulation
  * @author corbin
  *
  */
 class LEDNumPanel extends JPanel implements MouseListener {
 	
 	private static final long serialVersionUID = 1L;
 	//Panels to hold three rows
 	JPanel top,middle,bottom;
 	//top and bottom button rows
 	JButton[] b_inc, b_dec;
 	//The Number Panel
 	NumPanel LED;
 	/**
      * Constructor
      * @param d set number of digits in the panel
      * @param s size of panel
      * @param buttons optional value manipulation buttons
      */
 	public LEDNumPanel(int d, int s, boolean buttons) {
 		//setup main panels
 		middle = new JPanel(); 
 		 //Optionally Create Buttons
 		 if(buttons) {
 			 top = new JPanel();
 	         top.setLayout(new FlowLayout(FlowLayout.CENTER, 5*s, s));
 	         bottom = new JPanel();
 	         bottom.setLayout(new FlowLayout(FlowLayout.CENTER, 5*s, s));
 	         //Increase Value Buttons
 	         b_inc = new JButton[d];
 	         b_dec = new JButton[d];
 	         for(int n=d-1; n>=0; n--) {
 	                //create buttons
 	                b_inc[n] = new JButton();
 	                b_dec[n] = new JButton();
 	                //set button sizes
 	                b_inc[n].setPreferredSize(new Dimension(4*s,s*4));
 	                b_dec[n].setPreferredSize(new Dimension(4*s,s*4));
 	                //name buttons
 	                b_inc[n].setName("+"+(long)Math.pow(10, n));
 	                b_dec[n].setName("-"+(long)Math.pow(10, n));
 	                //add event handling
 	                b_inc[n].addMouseListener(this);
 	                b_dec[n].addMouseListener(this);
 	                //add buttons to panels
 	                top.add(b_inc[n]);
 	                bottom.add(b_dec[n]);
 	         }
 		 }
          //LED Panel
          LED = new NumPanel(d,s);
          middle.add(LED);
          
          //add panels
          if(buttons)
         	 add(top);
          add(middle);
          if(buttons)
         	 add(bottom);
 
         
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		String action = arg0.getComponent().getName();
     	if(action.startsWith("+"))
 			LED.add(Long.parseLong(action.substring(1)));
 		if(action.startsWith("-"))
 			LED.add(-Long.parseLong((action.substring(1))));
 		LED.repaint();
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 }
 
 /**
  * This is the class the coordinates the creation of the LED numbers 
  * and allows them to work together as a panel
  * @author corbin
  *
  */
 class NumPanel extends JPanel {
 	private static final long serialVersionUID = 1L;
 	//debug, counting number of times paintComponent is called
	private long count = 00000;
 	//the number of digits in the panel
 	private final int digits;
 	//size reference
 	private final int size;
 	//x reference
 	private final int xPos;
 	//y reference
 	private final int yPos;
 	//The LED Digits
     private LEDNum[] led_nums;
    
     /**
      * Constructor
      * @param d set number of digits in the panel
      * @param s size of panel
      */
     public NumPanel(int d, int s) {
         setBorder(BorderFactory.createLineBorder(Color.BLACK));
         this.digits = d;
         this.xPos = 0;
         this.yPos = 0;
        this.size = s;
        //repaint the digits (saved here for reference)
        //repaint(xPos,yPos,(size*9)*digits,size*16);
        
         
         //create digits
         led_nums = new LEDNum[this.digits];
         LEDNum temp = null;
         for(int n=this.digits-1; n>=0; n--) {
             //create digit
         	led_nums[n] = new LEDNum(this.xPos+((9*s)*(this.digits-1-n)),this.yPos,s, temp);
         	//save reference to store this position in the next digit
         	temp = led_nums[n];
         }
         //setup initial panel number
        //addToPanel(count);
        this.led_nums[0].add(count);
     }
     //access the LEDNum internal add function
     public void add(long n) {
     	this.led_nums[0].add(n);
     }
     
     //get the current value
     public long getCount() {
     	return this.count;
     }
     
     public Dimension getPreferredSize() {
         return new Dimension(this.xPos+((9*this.size)*(this.digits)),this.size*17);
     }
     
     public void paintComponent(Graphics g) {
         super.paintComponent(g);       
         for(int n=this.digits-1; n>=0; n--) {
         	//paint digit
         	led_nums[n].paintNum(g);
         }
     }
     
     public void repaint() {
     	SwingUtilities.invokeLater(new Runnable() {
             public void run() {
               //repaint the digits
           	  repaint(xPos,yPos,(size*9)*digits,size*16);
             }
         });
     }
 }
 
 /**
  * The individual LED number class
  * Each number knows about it's next highest neighbor and 
  * contains recursive borrow and carry operations inside the add() function
  * @author corbin
  *
  */
 class LEDNum {
 	//upper left coordinate of number panel
     private int xPos;
     //upper right coordinate of number panel
     private int yPos;
     //width of bars
     private int width;
     //height of bars
     private int height;
     //the number currently displayed
 	private long num = 0;
 	//padding around the numbers
 	private int padding;
 	//reference to the next highest digit
 	private LEDNum next = null;
 	//off color for a segment
 	private Color c_off = new Color(0.2f,0.0f,0.0f);
 	//on color for a segment
 	private Color c_on = new Color(1.0f,0.0f,0.0f);
 	//number border color
 	private Color c_border = new Color(0.1f, 0.1f, 0.1f);
 	
     public LEDNum(int x, int y, int size, LEDNum n) {
     	width = size;
     	height = size*6;
     	padding = width;
     	xPos = x+padding;
     	yPos = y+padding;
     	next = n;
     }
     
     public void setX(int xPos){ 
         this.xPos = xPos;
     }
 
     public int getX(){
         return xPos;
     }
 
     public void setY(int yPos){
         this.yPos = yPos;
     }
 
     public int getY(){
         return yPos;
     }
 
     public int getWidth(){
         return width;
     } 
 
     public int getHeight(){
         return height;
     }
     
     public void setNum(long n) {
     	num = n;
     	if(n > 9 || n < 0)
     		System.out.println("WARNING - Number of a single digit was set above 9 or below 0");
     }
     /**
      * Add to a digit
      * @param n the number to add
      * return false on error
      */
     public void add(long n) {
     	num += n;
     	if(num>9) {
     		//need to carry
 	    	while(num>9) {
 	    		if(next != null) {
     				next.add(1);
     				num -= 10;
 	    		} else {
 	    			System.out.println("MAX HIT");
 	    			this.add(-n);
 	    			return;
 	    		}
 	    	}
     	} else if(num<0) {
     		//need to borrow
     		while(num<0) {
     			if(!borrow()) {
     				System.out.println("MIN HIT");
     				this.add(-n);
     			}
     		}
     	}
     }
     
     /**recursive borrow function for subtraction
      * return true or false on error
      */
 	private boolean borrow()
 	{
 		//use head recursion, rather than tail recursion
 		//- want to validate borrow() to the deepest point before changing values
 		//- want to borrow from left to right, not right to left
 		if( next == null || next.getNum() == 0 && next.borrow() == false )
     		return false;
 
 		//decrement next number through api
     		next.setNum( next.getNum() - 1 );
 		//manually increase this number by 10 through private data member access
 		//- avoid recursion between add() & borrow()
 		//- to bypass warning in setNum()
 	    	this.num += 10;
     		return true;
     }
     
     public long getNum() {
     	return num;
     }
     
     public int[] getLoc() {
     	int[] a = {xPos-padding, yPos-padding, (2*padding)+width*7, (2*padding)+width*15};
     	return a;
     }
     
     public void paintNum(Graphics g){
         //background
     	g.setColor(Color.BLACK);
     	int[] loc = getLoc();
     	g.fillRect(loc[0],loc[1],loc[2],loc[3]);
     	//left-top
     	if(num == 0||num == 4||num == 5||num == 6||num == 8||num == 9)
     		g.setColor(c_on);
     	else
     		g.setColor(c_off);
         g.fill3DRect(xPos,yPos+(1*width),width,height, true);
         //left-top-border
         g.setColor(c_border);
         g.draw3DRect(xPos,yPos+(1*width),width,height,true);
         //top
         if(num == 0||num == 2||num == 3||num == 5||num == 6||num == 7||num == 8||num == 9)
     		g.setColor(c_on);
     	else
     		g.setColor(c_off);
         g.fill3DRect(xPos+(1*width),yPos,width*5,width, true);
         //top-border
         g.setColor(c_border);
         g.draw3DRect(xPos+(1*width),yPos,width*5,width,true);
         //right-top
         if(num == 0||num == 1||num == 2||num == 3||num == 4||num == 7||num == 8||num == 9)
     		g.setColor(c_on);
     	else
     		g.setColor(c_off);
         g.fill3DRect(xPos+(6*width),yPos+(1*width),width,height, true);
         //right-top-border
         g.setColor(c_border);
         g.draw3DRect(xPos+(6*width),yPos+(1*width),width,height,true);
         //middle
         if(num == 2||num == 3||num == 4||num == 5||num == 6||num == 8||num == 9)
     		g.setColor(c_on);
     	else
     		g.setColor(c_off);
         g.fill3DRect(xPos+(1*width),yPos+(7*width),width*5,width, true);
         //middle-border
         g.setColor(c_border);
         g.draw3DRect(xPos+(1*width),yPos+(7*width),width*5,width,true);
         //left-bottom
         if(num == 0||num == 2||num == 6||num == 8)
     		g.setColor(c_on);
     	else
     		g.setColor(c_off);
         g.fill3DRect(xPos,yPos+(8*width),width,height, true);
         //left-bottom-border
         g.setColor(c_border);
         g.draw3DRect(xPos,yPos+(8*width),width,height,true);
         //bottom
         if(num == 0||num == 2||num == 3||num == 5||num == 6||num == 8||num == 9)
     		g.setColor(c_on);
     	else
     		g.setColor(c_off);
         g.fill3DRect(xPos+(1*width),yPos+(14*width),width*5,width,true);
         //bottom-border
         g.setColor(c_border);
         g.draw3DRect(xPos+(1*width),yPos+(14*width),width*5,width,true);    
         //right-bottom
         if(num == 0||num == 1||num == 3||num == 4||num == 5||num == 6||num == 7||num == 8||num == 9)
     		g.setColor(c_on);
     	else
     		g.setColor(c_off);
         g.fill3DRect(xPos+(6*width),yPos+(8*width),width,height, true);
         //right-bottom-border
         g.setColor(c_border);
         g.draw3DRect(xPos+(6*width),yPos+(8*width),width,height,true);
     }
 }
