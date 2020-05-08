 package google;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 
 public class Crossthemaze {
   
   private static char nextDir(char in) {
     switch (in) {
     case 'E': return 'S';
     case 'S': return 'W';
     case 'W': return 'N';
     default : return 'E';
     }
   }
   
   private static char leftDir(char in) {
     switch (in) {
     case 'E': return 'N';
     case 'S': return 'E';
     case 'W': return 'S';
     default : return 'W';
     }
   }
   
   private static class Point {
     int x;
     int y;
     
     public Point() {
       x = 0;
       y = 0;
     }
     
     public Point(int x, int y) {
       this.x = x;
       this.y = y;
     }
     
     public void move(Point dir) {
       x += dir.x;
       y += dir.y;
     }
   }
   
   private static Map<Character, Point> map = new HashMap<Character, Point>();
   
   private static String proceed(Scanner scan) {
     int N = scan.nextInt();
     char[][] maze = new char[N][];
     for (int i = 0; i < N; ++i) {
       maze[i] = scan.next().toCharArray();
     }
     
     Point s = new Point();
     Point e = new Point();
     s.y = scan.nextInt() - 1;
     s.x = scan.nextInt() - 1;
     e.y = scan.nextInt() - 1;
     e.x = scan.nextInt() - 1;
     
    char f = 0;
     if (s.x == 0) {
       if (s.y == 0) {
         f = 'E';
       } else {
         f = 'N';
       }
     } else {
       if (s.y == 0) {
         f = 'S';
       } else {
         f = 'W';
       }
     }
 
     int step = 0;
     int turn = 0;
     boolean finished = false;
     StringBuilder builder = new StringBuilder();
    while (step < 10000) {
      if (s.x == e.x && s.y == e.y) {
         finished = true;
         break;
       }
       
       Point dp = map.get(f);
      if (s.x + dp.x < 0 || s.x + dp.x >= N || s.y + dp.y < 0 || s.y + dp.y >= N || maze[s.y + dp.y][s.x + dp.x] == '#') {
         if (turn == 3) break;
           
         f = nextDir(f);
         ++turn;
       } else {
        s.move(dp);
        ++step;
         turn = 0;
         builder.append(f);
         
         dp = map.get(leftDir(f));
        if (s.x + dp.x >= 0 && s.x + dp.x < N && s.y + dp.y >= 0 && s.y + dp.y < N && maze[s.y + dp.y][s.x + dp.x] == '.') {
           f = leftDir(f);
         }
       }
     }
     
     if (finished) {
       return step + "\n" + builder.toString();
     } else {
       return "Edison ran out of energy.";
     }
   }
   
   public static void init() {
     map.put('E', new Point(1, 0));
     map.put('S', new Point(0, 1));
     map.put('W', new Point(-1, 0));
     map.put('N', new Point(0, -1));
   }
   
   public static void main(String[] args) throws IOException {
    final String filePrefix = "D-large-practice";
     Scanner scan = new Scanner(new File(filePrefix + ".in"));
     PrintWriter out = new PrintWriter(new FileWriter(filePrefix + ".out"));
     init();    
     int caseNum = scan.nextInt();
     for (int i = 0; i < caseNum; ++i) {
       String strResult = proceed(scan);
       out.println("Case #" + (i + 1) + ": " + strResult);
     }
     scan.close();
     out.close();
   }
 
 }
