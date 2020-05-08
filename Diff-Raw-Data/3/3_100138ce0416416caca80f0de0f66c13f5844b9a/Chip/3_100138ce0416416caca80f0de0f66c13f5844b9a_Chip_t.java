 /* Chip.java */
 
 package player;
 
 import list.*;
 
 class Chip {
     private int color;
     private int x;
     private int y;
     public Chip[] inSight;
    
     /**
      * contructs and empty, invalid chip
      * there are only 8 possible chip within ones sight but the 9th slot is to
      * allow for a possible temporary slot for min/max
      */
     public Chip() {
         x = -1;
         y = -1;
         color = -1;
         inSight = new Chip[9];
     }
     
     /**
      * contructs a new chip with x, y coordinates and a color
      * there are only 8 possible chip within ones sight but the 9th slot is to
      * allow for a possible temporary slot for min/max
      */
     public Chip(int X, int Y, int Color) {
         x = X;
         y = Y;
         color = Color;
         inSight = new Chip[9];
     }
     
     /**
      * takes in no arguments and returns this chips x value
      */
     public int getX() {
         return x;
     }
 
     /**
      * takes in no arguments and returns this chips y value
      */
     public int getY() {
         return y;
     }
 
     /**
      * takes in no arguments and returns this chips color
      */
     public int color() {
         return color;
     }
 
     /**
      * clears the chip's inSight to be repopulated by Board.addChip
      */
     public void clear() {
         inSight = new Chip[9];
     }
 
     /**
      * checks if each chip is already in each others list
      * adds c to the chip's inSight if not already in
      * also adds self to c's inSight if not already in
      */
     public void addC(Chip c) {
         int add1 = -1;
         int add2 = -1;
         boolean added1 = false;
         boolean added2 = false;
         for (int i = 0; i < inSight.length; i++) {
             if (inSight[i] == c) {
                 added1 = true;
             }
             if (c.inSight[i] == this) {
                 added2 = true;
             }
             if (inSight[i] == null) {
                 add1 = i;
             }
             if (c.inSight[i] == null) {
                 add2 = i;
             }
         }
         if (!added1) {
             inSight[add1] = c;
         }
         if (!added2) {
             c.inSight[add2] = this;
         }
     }
 
     /**
      * removes all instances of c from chip's inSight 
      * also removes all instances of self from c's inSight
      */
     public void noC(Chip c) {
         for (int i = 0; i < inSight.length; i++) {
             if (inSight[i] == c) {
                 inSight[i] = null;
             }
             if (c.inSight[i] == this) {
                 c.inSight[i] = null;
             }
         }
     }
 
     /**
      * return a DList of all possible networks(legal or illegal)
      * that this chip is connected to(also represented as a Dlist)
      */
     public DList network(int color) {
         DList network = new DList();
         DList list = new DList();
         list.insertFront(this);
         DList blacklist = new DList();
         findTails(color, network, list, blacklist);
         build(network);
         return network;
     }
 
     /**
      * This is called findTails because this network is implemented as a ripple, it starts
      * from the center (could be considered the head) and branches outwards
      * searches and builds DLists using inSight
      *
      * color - gives the color of the chips in these networks
      * network - is a DList that holds all of the networks found which are also represented as DLists
      * list - is the current network that is being used in the current recursive iteration
      * it is copied and branched off into further networks
      * blacklist - holds the previous chips, so search doesn't go looking back
      * each iteration of findTails holds its own blacklist, because if the blacklist is
      * simply mutated then you will end up choking off possible networks
      */
     private void findTails(int color, DList network, DList list, DList blacklist) {
         //for every non-null value in inSight except for what in the blacklist
         for (int i = 0; i < inSight.length; i++) {
             //resetting blacklist for next iteration
             DList blist = blacklist.copy();
             DList tmplist = list.copy();
             if (inSight[i] != null && inSight[i].color() == color && !blist.has(inSight[i])) {
                 //add self to blacklist
                 blist.insertBack(this);
                 //add that to the back of list
                 tmplist.insertBack(inSight[i]);
                 //make a copy of this and add it to the network
                 network.insertBack(tmplist.copy());
                 //call findTails on chip from inSight
                 inSight[i].findTails(color, network, tmplist, blist);
             }
         }
     }
 
     /**
      * takes in the load of all possible tails from findTails as network
      * knowing that all tails start with "this" chip,
      * piece them all together in every combination with tails pointing "outwards"
      */
     private void build(DList network) {
         int length = network.length();
         DListNode n1 = network.front();
         DListNode n2;
         for (int i = 0; i < length; i++) {
             //resetting n2
             n2 = network.front();
             //moving n2 to the correct node
             for (int k = 0; k <= i; k++) {
                 n2 = network.next(n2);
             }
             //link-mutate copies and add to the end
             for (int j = i + 1; j < length; j++) {
                 DList copy = ((DList) n1.item).copy();
                 link(copy, (DList) n2.item);
                 if (!copy.hasRepeats()) {
                     network.insertBack(copy);
                 }
                 n2 = network.next(n2);
             }
             n1 = network.next(n1);
         }
     }
 
     /**
      * takes 2 lists and mutates list1, adding list2 onto it
      */
     private void link(DList list1, DList list2) {
         DListNode curr = list2.front();
         list1.remove(list1.front());
         while (curr != null) {
             list1.insertFront(curr.item);
             curr = list2.next(curr);
         }
     }
 
     /**
      * returns true if "this" and chip have equal x, y, and color fields
      * returns false otherwise
      */
     public boolean equals(Chip chip) {
         return (x == chip.x && y == chip.y && color == chip.color);
     } 
     
     public String toString() {
     	String s;
     	if (color==Board.BLACK)
     	{
     		s="-BLACK";
     	}
     	else
     	{
     		s="-WHITE";
     	}
     	
     	s=s+" at ["+getX()+"]["+getY()+"]-";
     	
     	return s;
 	}
     
 /* ##########ANYTHING BELOW THIS POINT IS FOR TESTING PURPOSES ONLY########## */
 
     public String inSightString(){
     	String s=new String("[");
     	for (int i=0; i<inSight.length;i++)
     	{
     		s=s+" "+inSight[i];
     	}
     	return s+" ]";
     }
 
     public void tester() {
         Board board = new Board();
         Move m1 = new Move(1, 1);
         Move m2 = new Move(3, 1);
         Move m3 = new Move(3, 3);
         Move m4 = new Move(1, 3);
         Move m5 = new Move(3, 5);
         Chip chip = new Chip();
         Chip c1 = new Chip();
         Chip c2 = new Chip();
 /*        System.out.println("adding c1");
         chip.addC(c1);
         //printinSight(chip);
         visualChip(chip);
         System.out.println("adding c1 again");
         chip.addC(c1);
         //printinSight(chip);
         visualChip(chip);
         System.out.println("adding c2");
         chip.addC(c2);
         //printinSight(chip);
         visualChip(chip);
         System.out.println("removing c1");
         chip.noC(c1);
         //printinSight(chip);
         visualChip(chip);
         System.out.println("removing c1 again");
         chip.noC(c1);
         //printinSight(chip);
         visualChip(chip);
         System.out.println("removing c2");
         chip.noC(c2);
         //printinSight(chip);
         visualChip(chip);
 */
         board.makeMove(Board.WHITE, m1);
         board.makeMove(Board.WHITE, m2);
         //board.makeMove(Board.WHITE, m3);
         board.makeMove(Board.WHITE, m4);
         //board.makeMove(Board.WHITE, m5);
         board.printboard(board);
 
        c1 = new Chip();
        //c1 = board.testChip(m1.x1, m1.y1);
         //ripped out of network()
         DList network = new DList();
         DList list = new DList();
         list.insertFront(c1);
         DList blacklist = new DList();
         c1.findTails(c1.color(), network, list, blacklist);
         System.out.println(network);
         System.out.println();
         c1.build(network);
         System.out.println(network);
     }
 
     public void printinSight(Chip chip) {
         for (int i = 0; i < chip.inSight.length; i++) {
             System.out.println(chip.inSight[i]);
         }
     }
 
     public void visualChip(Chip chip) {
         System.out.print("x: " + x + " y: " + y + " color: " + color + " [");
         for (int i = 0; i < chip.inSight.length; i++) {
             if (chip.inSight[i] == null) {
                 System.out.print("X");
             } else {
                 System.out.print("C");
             }
         }
         System.out.println("]");
     }
 }
