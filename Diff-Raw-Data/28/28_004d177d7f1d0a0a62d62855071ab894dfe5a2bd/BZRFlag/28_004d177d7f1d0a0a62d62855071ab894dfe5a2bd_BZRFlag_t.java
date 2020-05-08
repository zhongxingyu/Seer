 package main;
 
 import ServerResponse.*;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static java.lang.Double.*;
 
 
 /**
  * Created with IntelliJ IDEA.
  * User: ty
  * Date: 9/21/13
  * Time: 7:15 PM
  * This is basically the interface a bot has with the game
  */
 public class BZRFlag {
     /**Output for messages to BZRflag game*/
     private final PrintWriter mOut;
     /**Input for messages from BZRflag game*/
     private final BufferedReader mIn;
     /**Debug flag*/
     private boolean mDebug = false;
 
     /**
      * Constructor
      */
     public BZRFlag(String host, int port) throws IOException {
         Socket socket = new Socket(host, port);
         mIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
         mOut = new PrintWriter(socket.getOutputStream(), true);
     }
 
     /**
      * Sends a message to the BZRflag game server
      */
     private void sendLine(String msgToServer)
     {
         mOut.println(msgToServer);
 
         if(mDebug)
             System.out.println("Sent command:" + msgToServer);
     }
 
     /**
      * Reads reply from the BZRflag game
      */
     private String readOneReplyLine() throws IOException {
         String serverResponse = mIn.readLine();
 
         if(mDebug)
             System.out.println(serverResponse);
 
         return serverResponse;
     }
 
     /**
      * Performs handshake with the server
      */
     public void handshake() throws IOException {
         String reply = readOneReplyLine();
         assert reply != null;
         assert(reply.equals("bzrobots 1"));
         sendLine("agent 1");
     }
 
     /**
      * Reads an acknowledgment message from the server
      * @return If the
      */
     private boolean readAck(String cmdSent) throws IOException
     {
         String ackLine = readOneReplyLine();
 
         //Pattern ackRegex = Pattern.compile("ack (-?[0-9]+\\.[0-9]+) (" + cmdSent + ")");
         Pattern ackRegex = Pattern.compile("ack (-?[0-9]+\\.[0-9]+) (.*?)");
         Matcher matcher = ackRegex.matcher(ackLine);
 
         assert(matcher.matches());
         parseDouble(matcher.group(1));
 
         return true;
     }
 
     /**
      * Reads the boolean line
      */
     private BoolResponse readBool() throws IOException {
         String boolLine = readOneReplyLine();
         Pattern boolRegex = Pattern.compile("(ok|fail)( .*)?");
         Matcher matcher = boolRegex.matcher(boolLine);
 
         assert(matcher.matches());
         boolean success = matcher.group(1).equals("ok");
         String descrip = matcher.group(2) != null ? matcher.group(2) : "";
 
         return new BoolResponse(success, descrip);
     }
 
     /**
      * Sends the shoot command to the indexed bot
      * @param botId Index of the bot we are telling to shoot
      * @throws IOException
      * @return Returns false if the command was successful and false otherwise
      */
     public BoolResponse shoot(int botId) throws IOException {
         StringBuilder cmdBuilder = new StringBuilder("shoot ");
         cmdBuilder.append(Integer.toString(botId));
         String shootCmd = cmdBuilder.toString();
         sendLine(shootCmd);
         readAck(shootCmd);
         return readBool();
     }
 
     /**
      * Sends the the speed command
      * @throws IOException
      */
     public BoolResponse speed(int botId, double speed) throws IOException {
         StringBuilder cmdBuilder = new StringBuilder("speed ");
         cmdBuilder.append(Integer.toString(botId));
         cmdBuilder.append(" ").append(Double.toString(speed));
         String speedCmd = cmdBuilder.toString();
         sendLine(speedCmd);
         readAck(speedCmd);
         return readBool();
     }
 
     /**
      * Sends the angular velocity command
      * @param botId
      * @param angVel
      * @return
      * @throws IOException
      */
     public BoolResponse angVel(int botId, double angVel) throws IOException {
         String command = String.format("angvel %d %s", botId, angVel);
         sendLine(command);
         readAck(command);
         return readBool();
     }
 
     /**
      * Queries the obstacles within the world
      * @return
      * @throws IOException
      */
     public ArrayList<Obstacle> getObstacles() throws IOException {
         String queryCmd = "obstacles";
         sendLine(queryCmd);
         readAck(queryCmd);
 
         ArrayList<Obstacle> obstacles = new ArrayList<Obstacle>();
 
         Pattern obstacleLine = Pattern.compile(
                 "obstacle (-?[0-9]+\\.[0-9]?) (-?[0-9]+\\.[0-9]?) (-?[0-9]+\\.[0-9]?) (-?[0-9]+\\.[0-9]?) (-?[0-9]+\\.[0-9]?) (-?[0-9]+\\.[0-9]?) (-?[0-9]+\\.[0-9]?) (-?[0-9]+\\.[0-9]?)"
         );
         Matcher matcher = null;
         String arrayLine = readOneReplyLine();
         assert(arrayLine.equals("begin"));
         arrayLine = readOneReplyLine();
         while(!arrayLine.equals("end")) {
             matcher = obstacleLine.matcher(arrayLine);
             assert(matcher.matches());
 
             Vector p0 = new Vector(parseDouble(matcher.group(1)), parseDouble(matcher.group(2)));
             Vector p1 = new Vector(parseDouble(matcher.group(3)), parseDouble(matcher.group(4)));
             Vector p2 = new Vector(parseDouble(matcher.group(5)), parseDouble(matcher.group(6)));
             Vector p3 = new Vector(parseDouble(matcher.group(7)), parseDouble(matcher.group(8)));
 
             obstacles.add(new Obstacle(p0, p1, p2, p3));
 
             arrayLine = readOneReplyLine();
         }
 
         return obstacles;
     }
 
     /**
      * Queries the other tanks within the world
      * @throws IOException
      */
     public ArrayList<Tank> getOtherTanks() throws IOException
     {
         String queryCmd = "othertanks";
         sendLine(queryCmd);
         readAck(queryCmd);
 
         ArrayList<Tank> otherTanks = new ArrayList<Tank>();
 
         Pattern obstacleLine = Pattern.compile(
                 "othertank (.*?[0-9]) (.*?) (.*?) (.*?) (.*?) (.*?) (.*?)"
         );
 
         Matcher matcher = null;
         String arrayLine = readOneReplyLine();
         assert(arrayLine.equals("begin"));
         arrayLine = readOneReplyLine();
         while(!arrayLine.equals("end")) {
             matcher = obstacleLine.matcher(arrayLine);
             assert(matcher.matches());
 
             String callSign = matcher.group(1);
             Tank.TeamColor teamColor = Tank.TeamColor.valueOf(matcher.group(2).toUpperCase());
             Tank.TankStatus status = Tank.TankStatus.valueOf(matcher.group(3).toUpperCase());
             Tank.TeamColor flagColor = matcher.group(4).equals("-") ? Tank.TeamColor.NONE : Tank.TeamColor.valueOf(matcher.group(4).toUpperCase());
             double xPos = parseDouble(matcher.group(5));
             double yPos = parseDouble(matcher.group(6));
             Vector tankPos = new Vector(xPos, yPos);
             double angle = parseDouble(matcher.group(7));
 
             Tank tank = new Tank(callSign, teamColor, status, flagColor, tankPos, angle);
             otherTanks.add(tank);
 
             arrayLine = readOneReplyLine();
         }
 
         return otherTanks;
     }
 
     public ArrayList<MyTank> getMyTanks(Tank.TeamColor myColor) throws IOException {
         String queryCmd = "mytanks";
         sendLine(queryCmd);
         readAck(queryCmd);
 
         ArrayList<MyTank> myTanks = new ArrayList<MyTank>();
 
         Pattern obstacleLine = Pattern.compile(
                 "mytank ([0-9])\\s+(.*?[0-9])\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+ (.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)\\s+(.*?)"
         );
 
         Matcher matcher = null;
         String arrayLine = readOneReplyLine();
         assert(arrayLine.equals("begin"));
         arrayLine = readOneReplyLine();
         while(!arrayLine.equals("end")) {
             matcher = obstacleLine.matcher(arrayLine);
             assert(matcher.matches());
 
             int index = Integer.parseInt(matcher.group(1));
             String callSign = matcher.group(2);
             Tank.TankStatus status = Tank.TankStatus.valueOf(matcher.group(3).toUpperCase());
             int shotsAvail = Integer.parseInt(matcher.group(4));
             double timeToReload = parseDouble(matcher.group(5));
             Tank.TeamColor flagColor = matcher.group(6).equals("-") ? Tank.TeamColor.NONE : Tank.TeamColor.valueOf(matcher.group(4).toUpperCase());
             double xPos = parseDouble(matcher.group(7));
             double yPos = parseDouble(matcher.group(8));
             Vector tankPos = new Vector(xPos, yPos);
             double angle = parseDouble(matcher.group(9));
             double xVel = parseDouble(matcher.group(10));
             double yVel = parseDouble(matcher.group(11));
             Vector vel = new Vector(xVel, yVel);
             double angVel = parseDouble(matcher.group(12));
 
             MyTank tank = new MyTank(index,
                     callSign,
                     myColor,
                     status, shotsAvail,
                     timeToReload,
                     flagColor,
                     tankPos,
                     angle,
                     vel,
                     angVel);
             myTanks.add(tank);
 
             arrayLine = readOneReplyLine();
         }
 
         return myTanks;
     }
 
     public ArrayList<Flag> getFlags() throws IOException {
         String queryCmd = "flags";
         sendLine(queryCmd);
         readAck(queryCmd);
 
         Pattern flagLine = Pattern.compile(
                 "flag (.*?) (.*?) (.*?) (.*?)"
         );
 
         ArrayList<Flag> flags = new ArrayList<Flag>();
 
         Matcher matcher = null;
         String arrayLine = readOneReplyLine();
         assert(arrayLine.equals("begin"));
         arrayLine = readOneReplyLine();
         while(!arrayLine.equals("end")) {
             matcher = flagLine.matcher(arrayLine);
             assert(matcher.matches());
 
             Tank.TeamColor flagColor = Tank.TeamColor.valueOf(matcher.group(1).toUpperCase());
             Tank.TeamColor possessingTeamColor = Tank.TeamColor.valueOf(matcher.group(2).toUpperCase());
             double xPos = parseDouble(matcher.group(3));
             double yPos = parseDouble(matcher.group(4));
             Flag flag = new Flag(flagColor, possessingTeamColor, new Vector(xPos, yPos));
             flags.add(flag);
             arrayLine = readOneReplyLine();
         }
 
         return flags;
     }
 
     public static void plotWorld() throws IOException {
         BZRFlag agent = new BZRFlag("localhost", 33925);
         agent.handshake();
         ArrayList<Obstacle> obstacles = agent.getObstacles();
 
         PrintWriter gpiFile = new PrintWriter("world.gpi", "UTF-8");
         gpiFile.println("set xrange [-400.0: 400.0]");
         gpiFile.println("set yrange [-400.0: 400.0]");
         gpiFile.println("unset arrow");
 
         for(Obstacle obstacle : obstacles) {
             gpiFile.println(GnuplotPrinter.getObstaclePlotCmds(obstacle));
         }
 
         gpiFile.println("e");
 
         gpiFile.close();
     }
 
     public static void main(String args[]) throws IOException {
         //plotWorld();
 
 
         BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
         /*System.out.print("Input host:");
         String host = br.readLine();*/
 
         /*System.out.print("Input port num:");
         String portNumStr = br.readLine();
         int portNum = Integer.parseInt(portNumStr);*/
 
         //BZRFlag agent = new BZRFlag("localhost", portNum);
         /*BZRFlag agent = new BZRFlag("localhost", 50271);
         agent.sendLine("agent 1");
         agent.readOneReplyLine();
 
         agent.shoot(1);
         agent.speed(1, -1.0);
         agent.angVel(1, 1.0);
         agent.getObstacles();
         agent.getOtherTanks();
         agent.getMyTanks(Tank.TeamColor.BLUE);*/
 
        BZRFlag blueServer = new BZRFlag("localhost", 58772);
         PFAgent pfAgent = new PFAgent(blueServer, Tank.TeamColor.BLUE);
        while(true) {
           pfAgent.tick();
        }
        //pfAgent.plotPfs();
 
         /*DumbAgent dumbAgent = new DumbAgent(blueServer, Tank.TeamColor.BLUE);
         while(true) {
             dumbAgent.tick();
         }*/
 
     }
 }
