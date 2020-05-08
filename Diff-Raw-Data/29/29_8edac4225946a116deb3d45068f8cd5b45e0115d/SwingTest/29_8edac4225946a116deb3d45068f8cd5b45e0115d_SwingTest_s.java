 package suschord.newcoldhorizon.tests;
 
 import net.slashie.libjcsi.*;
 import net.slashie.util.*;
 import net.slashie.libjcsi.wswing.*;
 import suschord.libjcsi.textcomponents.*;
 import java.util.*;
 
 public class SwingTest {
   static Position pos = new Position(2,2);
   
   public static void main(String[] args) {
     //ConsoleSystemInterface csi = null;
     WSwingConsoleInterface csi = null;
     try {
       csi = new WSwingConsoleInterface("SwingTest.java", false);
     } catch (Exception ex) {ex.printStackTrace();}
        
     
     DialogBox db = new DialogBox(csi, 1, "What is your name?");
     db.setPosition(pos.x, pos.y);
     csi.cls();
     db.draw();
     csi.refresh();
     System.out.println("recieved: " + db.getInput(15));
     csi.waitKey(CharKey.ENTER);
     csi.cls();
     
     ListBox lb = new ListBox(csi);
     lb.setPosition(pos.x, pos.y);
     lb.setBorder(true);
     lb.addElement(new BasicListItem('a', ConsoleSystemInterface.RED, "An example item."));
     lb.addElement(new BasicListItem('b', ConsoleSystemInterface.GREEN, "Another example item."));
     lb.setWidth(40);
     lb.setHeight(4);
     csi.cls();
     lb.draw();
     csi.refresh();
     csi.waitKey(CharKey.ENTER);
     csi.cls();
     
     TextBox tb = new TextBox(csi);
     tb.setPosition(pos.x, pos.y);
     tb.setHeight(4);
     tb.setWidth(22);
     tb.setBorder(true);
     tb.setTitle("Tutorial");
     tb.setText("Welcome.");
     
     tb.draw();
     csi.refresh();
     csi.waitKey(CharKey.ENTER);
     csi.cls();
     
     TextInformBox tib = new TextInformBox(csi);
     tib.setPosition(pos.x, pos.y);
     tib.setHeight(4);
     tib.setWidth(22);
     tib.setBorder(true);
    tib.setText("Welcome to the game.");
     
     tib.draw();
     csi.refresh();
     tib.addText("AAAA BBBBB CCCCC DDDDD EEEEE FFFFF GGGGG HHHHH IIIII JJJJJ KKKKK LLLLL MMMMM NNNNN OOOOO PPPPP QQQQQ RRRRR SSSSS TTTTT WWWWW XXXXX YYYYY ZZZZZ AAAA BBBBB CCCCC DDDDD EEEEE FFFFF GGGGG HHHHH IIIII JJJJJ KKKKK LLLLL MMMMM NNNNN OOOOO PPPPP QQQQQ RRRRR SSSSS TTTTT WWWWW XXXXX YYYYY ZZZZZ AAAA BBBBB CCCCC DDDDD EEEEE FFFFF GGGGG HHHHH IIIII JJJJJ KKKKK LLLLL MMMMM NNNNN OOO PPPPP QQQQQ RRRRR SSSSS TTTTT WWWWW XXXXX YYYYY ZZZZZ");
     csi.refresh();
     csi.waitKey(CharKey.ENTER);
     tib.clear();
     csi.refresh();
     csi.waitKey(CharKey.ENTER);
     csi.cls();
     
     // menubox is pretty broken...
     MenuBox mb = new MenuBox(csi);
     Vector<BasicMenuItem> m_items = new Vector<BasicMenuItem>();
     m_items.add(new BasicMenuItem('a', ConsoleSystemInterface.RED, "An item."));
     m_items.add(new BasicMenuItem('b', ConsoleSystemInterface.GREEN, "An item."));
     m_items.add(new BasicMenuItem('c', ConsoleSystemInterface.YELLOW, "An item."));
     m_items.add(new BasicMenuItem('d', ConsoleSystemInterface.WHITE, "An item."));
     m_items.add(new BasicMenuItem('e', ConsoleSystemInterface.BLUE, "An item."));
     m_items.add(new BasicMenuItem('f', ConsoleSystemInterface.BROWN, "An item."));
     m_items.add(new BasicMenuItem('g', ConsoleSystemInterface.CYAN, "An item."));
     m_items.add(new BasicMenuItem('h', ConsoleSystemInterface.LEMON, "An item."));
     m_items.add(new BasicMenuItem('i', ConsoleSystemInterface.MAGENTA, "An item."));
     m_items.add(new BasicMenuItem('j', ConsoleSystemInterface.TEAL, "An item."));
     mb.setMenuItems(m_items);
     mb.setPosition(pos.x, pos.y);
    mb.setPrompt("Select something.");
    mb.setPromptSize(9);
     mb.setHeight(10);
     mb.setWidth(22);
    //mb.setBorder(true);
    //mb.setTitle("A menu.");
     
     csi.cls();
     mb.draw();
     csi.refresh();
     
     while(true) {
       System.out.println(((BasicMenuItem)mb.getSelection()).getMenuDescription());
     }
     
     
     
     /*
     do {
       csi.cls();
       csi.print(pos.x, pos.y, "@", ConsoleSystemInterface.RED);
       csi.refresh();
       CharKey key = csi.inkey();
       //System.out.println(key);
       if(key.isUpArrow()) pos.y--;
       if(key.isDownArrow()) pos.y++;
       if(key.isLeftArrow()) pos.x--;
       if(key.isRightArrow()) pos.x++;
       if(key.code == CharKey.ESC) System.exit(0);
       //System.out.println(x);
       //System.out.println(y);
     } while(true);
     */
   }
 }
