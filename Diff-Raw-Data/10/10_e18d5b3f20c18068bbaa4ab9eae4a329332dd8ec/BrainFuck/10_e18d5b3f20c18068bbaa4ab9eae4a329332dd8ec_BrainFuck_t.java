 package megascus.brainfuck;
 
 import java.util.Stack;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * BrainFuckの実装です。
  * {@link Commands} クラスを元に実行します。
  * <p/>
  * http://ja.wikipedia.org/wiki/Brainfuck
  * <p/>
  * 本家との違いは以下のとおりです。
  * <ul>
  * <li>本家ではstreamを扱うが固定文字列を扱う。</li>
  * <li>本家ではbyteで扱うが、charで扱う。</li>
  * </ul>
  * <p/>
  *
  * @author megascus
  */
 public class BrainFuck {
 
     private Commands cmd;
     private Buffer buff = new Buffer();
     private Stack<Integer> openPoint = new Stack<>();
 
     BrainFuck(Commands cmd) {
         this.cmd = cmd;
     }
 
     public String fuck(String in) {
         StringBuilder out = new StringBuilder();
         Pattern ptn = PatternBuilder.build(cmd);
         Matcher match = ptn.matcher(in);
         while (match.find()) {
             executeCommand(in, out, match);
         }
         return out.toString();
     }
 
     private void executeCommand(String in, StringBuilder out, Matcher match) {
         String g = match.group();
         if (cmd.getGetChar().contains(g)) {
             out.append(buff.getChar());
             return;
         }
         if (cmd.getPutChar().contains(g)) {
             buff.putChar(in.charAt(match.end()));
             return;
         }
         if (cmd.getIncrement().contains(g)) {
             buff.increment();
             return;
         }
         if (cmd.getDecrement().contains(g)) {
             buff.decrement();
             return;
         }
         if (cmd.getPrevious().contains(g)) {
             buff.previous();
             return;
         }
         if (cmd.getNext().contains(g)) {
             buff.next();
             return;
         }
         if (cmd.getOpen().contains(g)) {
             if (buff.getChar() != 0) {
                 openPoint.push(match.start());
                 return;
             }
            int openTagCount = 1;
             do {
                 match.find();
                String group = match.group();
                if(cmd.getClose().contains(group)) {
                    openTagCount -=1;
                } else if(cmd.getOpen().contains(group)) {
                    openTagCount +=1;
                }
            } while (openTagCount != 0);
             return;
         }
         if (cmd.getClose().contains(g)) {
             int start = openPoint.pop();
             match.find(start);
             executeCommand(in, out, match);
             return;
         }
     }
 }
