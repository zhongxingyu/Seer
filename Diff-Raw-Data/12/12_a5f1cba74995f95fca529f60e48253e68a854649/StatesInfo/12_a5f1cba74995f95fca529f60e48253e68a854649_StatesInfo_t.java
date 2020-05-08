 
 
 import agentsproject.Packet;
 import java.util.*;
 import java.awt.*;
 import java.io.*;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 public class StatesInfo
 {
   public boolean WritedFile;
   private Map H;
   public Map Obstacles;
   public Map MyStates;
   public java.util.List AgentStartPos;
   public java.util.List PacketPos;
   public java.util.List MultipleTargets;
   private Map Agents;
   //private ArrayList mytemp = new ArrayList();
   private Map<Point, Packet> Packets;
   public int Count, AgentsDone;
   public ConcurrentLinkedQueue<Point> helpCalls;
 
   public StatesInfo()
   {
     Count=0;
     H = new HashMap();
     MyStates = new HashMap();
     Obstacles = new HashMap();
     Agents = new HashMap();
     AgentsDone = 0;
     Packets = new HashMap<>();
     MultipleTargets = new ArrayList();
     AgentStartPos = new ArrayList();
     PacketPos = new ArrayList();
     WritedFile = false;
     helpCalls = new ConcurrentLinkedQueue<>();
   }
 
   public void Initialize()
   {
     Count=0;
     H = new HashMap();
     MyStates = new HashMap();
     Obstacles = new HashMap();
     Agents = new HashMap();
     AgentsDone = 0;
     Packets = new HashMap<>();
     MultipleTargets = new ArrayList();
     AgentStartPos = new ArrayList();
     PacketPos = new ArrayList();
     WritedFile = false;
     helpCalls = new ConcurrentLinkedQueue<>();
   }
 
   public void GenerateMaze(int Dimx, int Dimy, double ObstacleRatio,
                            int NoofAgents, int NoofPackets, int NoofDestinations)
   {
     int Randx, Randy;
     /************************************************
      ** Generate Agents Starting Position
      ***********************************************/
     for (int i = 0; i < NoofAgents; i++)
     {
       Randx = (int) (Math.random() * Dimx);
       Randy = (int) (Math.random() * Dimy);
       AddAgent(Randx, Randy);
       System.out.println("Agent->" + Randx + "," + Randy);
     }
     
     /************************************************
      ** Generate Obstacles In Search Space
      ***********************************************/
     int NoofObstacle = 0;
     int Obstaclesput = 0;
 
     NoofObstacle = (int) (Dimx * Dimy * ObstacleRatio);
 
     while (Obstaclesput <= NoofObstacle)
     {
       Randx = (int) (Math.random() * Dimx);
       Randy = (int) (Math.random() * Dimy);
       boolean b;
       b = AddObstacle(Randx, Randy);
       if ( (b == true) && (GetAgent(Randx, Randy) == -1))
       {
         Obstaclesput++;
       }
     }
     /************************************************
      ** Generate Target Position
      ***********************************************/
     for(int i=0;i<NoofDestinations;i++)
     {
       while (true)
       {
         Randx = (int) (Math.random() * Dimx);
         Randy = (int) (Math.random() * Dimy);
 
         if (GetAgent(Randx, Randy) == -1 && GetObstacle(Randx, Randy) == -1)
         {
           AddTarget(Randx, Randy);
           break;
         }
       }
     }
     
     /************************************************
      ** Generate Packet Starting Position
      ***********************************************/
 /* ==================== QUAIN Code starts here ============================== */ 
     for (int i = 0; i < NoofPackets; i++)
     {
         while (true)
         {
           Randx = (int) (Math.random() * Dimx);
           Randy = (int) (Math.random() * Dimy);
           int weight = ((int)(Math.random()*100))%NoofAgents+1;
 
           if (GetAgent(Randx, Randy) == -1 && GetObstacle(Randx, Randy) == -1)
           {
             AddPacket(Randx, Randy, weight);
             System.out.println("Packet->" + Randx + "," + Randy);
             break;
           }
         }
       
     }
 /* ==================== QUAIN Code ends here ============================== */ 
   }
 
 
   public synchronized void AddH(int Dimx, int Dimy, int HValue)
   {
     String RIndex = Dimx + "," + Dimy + "";
     String s = HValue + "";
     H.put(RIndex, s);
   }
 
   public int GetH(int Dimx, int Dimy)
   {
     String RIndex = Dimx + "," + Dimy + "";
     String s;
     s = (String) H.get(RIndex);
     if (s != null)
     {
       return Integer.parseInt(s);
     }
     else
     {
       return 0;
     }
   }
 
   public synchronized void AddStateColor(int Dimx, int Dimy, Color Col)
   {
     String RIndex = Dimx + "," + Dimy;
     MyStates.put(RIndex, Col);
   }
 
   public Color GetStateColor(int Dimx, int Dimy)
   {
     String RIndex = Dimx + "," + Dimy;
     Color c;
     try
     {
       c = (Color) MyStates.get(RIndex);
     }
     catch (Exception ex)
     {
       c = Color.WHITE;
     }
     return c;
   }
 
   public boolean AddObstacle(int Randx, int Randy)
   {
     long t = GetObstacle(Randx, Randy);
 /*
  * Change the logic of creation of obstacles on GUI because it overlaps with Agents and packets
  * 
  * */
     if ((t == -1)&& (GetAgent(Randx, Randy) == -1)&& (getPacket(Randx, Randy) == null))
     {
       String s = "1";
       String RIndex = Randx + "," + Randy + "";
       Obstacles.put(RIndex, s);
       return true;
     }
     else
     {
       return false;
     }
   }
 
   public long GetObstacle(int Randx, int Randy)
   {
     String RIndex = Randx + "," + Randy + "";
     String s;
     s = (String) Obstacles.get(RIndex);
     if (s != null)
     {
       return Integer.parseInt(s);
     }
     else
     {
       return -1;
     }
   }
 
   private void AddAgent(int Randx, int Randy)
   {
     String s = "1";
     String RIndex = Randx + "," + Randy + "";
     Agents.put(RIndex, s);
     Point P = new Point();
     P.x = Randx;
     P.y = Randy;
     AgentStartPos.add(P);
   }
  
   private long GetAgent(int Randx, int Randy)
   {
     String RIndex = Randx + "," + Randy + "";
     String s;
     s = (String) Agents.get(RIndex);
     if (s != null)
     {
       return Integer.parseInt(s);
     }
     else
     {
       return -1;
     }
   }
 
   private void AddTarget(int Randx, int Randy)
   {
    //String s = "1";
    //String RIndex = Randx + "," + Randy + "";
    //Targets.put(RIndex, s);
     Point P = new Point();
     P.x = Randx;
     P.y = Randy;
     MultipleTargets.add(P);
   }
 
   public void InitStates()
   {
     String FileName = "StatesInfo.txt";
     String str;
     try
     {
       BufferedReader in = new BufferedReader(new FileReader(FileName));
       while ( (str = in.readLine()) != null)
       {
         //Split str and pick the position
         String[] sArr = str.split(",");
         Color c = Color.WHITE;
         Point PTmp = new Point();
         PTmp.x = Integer.parseInt(sArr[0]);
         PTmp.y = Integer.parseInt(sArr[1]);
         AddStateColor(PTmp.x, PTmp.y, c);
         AddH(PTmp.x, PTmp.y, Integer.parseInt(sArr[2]));
       }
       in.close();
     }
     catch (IOException e)
     {
       System.out.println("Error Occured While Reading From File...!");
     }
   }
 
   public void WriteStates()
   {
     if (WritedFile == false)
     {
       WritedFile = true;
       String FileName = "StatesInfo.txt";
       try
       {
         BufferedWriter out = new BufferedWriter(new FileWriter(FileName));
         Iterator KeyIterator = MyStates.keySet().iterator();
         String PTmp;
         Point P = new Point();
         while (KeyIterator.hasNext())
         {
           PTmp = (String) KeyIterator.next();
           String[] s = PTmp.split(",");
           P.x = Integer.parseInt(s[0]);
           P.y = Integer.parseInt(s[1]);
           out.write(PTmp + "," + GetH(P.x, P.y));
           out.newLine();
         }
         out.close();
       }
       catch (IOException e)
       {
         System.out.println("Error Occured While Writing To File...!");
       }
     }
   }
 
   public void MakeFileEmpty()
   {
     String FileName = "StatesInfo.txt";
     try
     {
       BufferedWriter out = new BufferedWriter(new FileWriter(FileName));
     }
     catch (IOException e)
     {
       System.out.println("Error Occured While Writing To File...!");
     }
   }
 /* ==================== QUAIN Code starts here ============================== */   
     private void AddPacket(int Randx, int Randy, int weight)
     {
       Point init = new Point(Randx, Randy);
 
       //TODO: Randomize these parameters...
       int targetIndex = (int) (Math.random()*MultipleTargets.size());
       Point end =(Point) MultipleTargets.get(targetIndex);
       Packet p = new Packet(init, end, weight);
 
       Packets.put(p.getPos(), p);
 
       PacketPos.add(p.getPos());
     }
 
     public Packet getPacket(int Randx, int Randy)
     {
       Point RIndex = new Point(Randx , Randy);
       return Packets.get(RIndex);
     }
     
     public synchronized Packet getPacket(Point p)
     {
       return Packets.get(p);
     }
   
     public synchronized Point ChoosePacket(Point MyPos)
     {
       int ax=MyPos.x;
       int ay=MyPos.y;
       int j=0;
       //Closest Packet 
       Point M_P =new Point();
       M_P.x=-1;
       M_P.y=-1;
       double M_D=10000;
       for (int i = 0; i < PacketPos.size(); i++)
       {
 	       double distance;
 	       Point PP = (Point) PacketPos.get(i);
               if(getPacket(PP.x, PP.y) != null)
                   if(getPacket(PP.x, PP.y).isTaken())
                    continue;
 	       int px=PP.x;
 	       int py=PP.y; 
 	       distance = Math.sqrt(((ax-px)*(ax-px))+((ay-py)*(ay-py)));
 	       if ( M_D > distance)
 	       {
 	            M_D = distance;       
 	            M_P = PP;   
 	            j=PacketPos.indexOf(i);
 	       }
         }
        return M_P;
     }
 
     public synchronized void  removePacket(Point MyPos)
     {
             String RIndex = MyPos.x + "," + MyPos.y + "";
             Packets.remove(MyPos);
             PacketPos.remove(MyPos);
     }
     
     public void ClearH()
     {
         H.clear();
     }
     
     public void callForHelp(Point p, int weight)
     {
         for(int i = 0; i < weight; i++)
             helpCalls.add(p);
     }
     
     public synchronized Point getHelpCall()
     {
         return helpCalls.poll();
     }
     
     public synchronized Point peekHelpCall()
     {
         return helpCalls.peek();
     }
     
     public synchronized boolean checkHelpCall()
     {
         return !helpCalls.isEmpty();
     }
     
     public void cancelHelpCall(Point p)
     {
         while(helpCalls.remove(p));
     }
     
     
   public int getTotalWeight()
   {
       int sum =0;
       for(Packet p: Packets.values())
       {
           sum += p.getWeight();
       }
       return sum;
   }
     
 /* ==================== QUAIN Code ends here ============================== */ 
 }
