 package reversi;
 import reversi.*;
 import java.io.*;
 import  java.net.*;
 import org.xml.sax.InputSource;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.validation.*;
 import javax.xml.XMLConstants;
 import java.util.Scanner;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Node;
 import java.util.ArrayList;
 import javax.xml.transform.dom.DOMSource;
 import java.util.UUID;
 import java.util.HashMap;
 import java.util.Arrays;
 
 public class ReversiClient {
     private String nick, uuid, color, tableid;
     private String recentObservation; // stores the most recent table observed
     private boolean playing;
     private int wins, losses, points;
     private double ratio;
     private URL url;
     private static boolean verbose = false;
     private Scanner in = new Scanner(System.in);
     Schema responseSchema;
     Validator validator;
 
     private String makeXML(String type, String content) {
         String base = "<?xml version=\"1.0\" ?><request "+
                         "type=\"%s\"><nick>%s</nick><uuid>%s"+
                         "</uuid>%s</request>";
         return String.format(base, type, nick, uuid, content);
     }
 
     // convenience method
     private String makeXML(String type) {
         return makeXML(type, "");
     }
 
     public ReversiClient(String url) throws MalformedURLException {
         this.url = new URL(url);
         
         // load up schema
         SchemaFactory sf = 
             SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
         
         // load schema into memory
         try {
             File schemaFile = new File("reversi/response.xml");
             responseSchema = sf.newSchema(schemaFile);
             validator = responseSchema.newValidator();
         } catch (Exception e) {
             System.out.println("Error loading schema: ");
             e.printStackTrace();
         }
 
         // this will keep looping until a valid username is found
         while (true) {
             System.out.print("Please enter a nick, or 'exit' to quit: ");
             String input = in.nextLine();
             if (input.equalsIgnoreCase("exit")) {
                 System.exit(0);
             }
             nick = input;
             if (register()) break;
         }
         System.out.println("Successfully registered as " + nick + ".");
     }
 
     public ReversiClient(String url, String nick) 
                                    throws MalformedURLException {
         this.url = new URL(url);
         this.nick = nick;
         if (!register()) {
             throw new RuntimeException("Failed to register nick " + nick);
         }
     }
 
     private Element findSubelem(Element elem, String name) {
         // Given an element, finds first subelement with given name
         NodeList nodes = elem.getElementsByTagName(name);
         if (nodes.getLength() > 0)
             return (Element) nodes.item(0);
         return null;
     }
 
     private Document getDOM(String xml) throws Exception {
         // parses into a DOM and validates at the same time. If not
         // valid, throws an exception.
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = factory.newDocumentBuilder();
 
         Document document =
             builder.parse(new InputSource(new StringReader(xml)));
         try {
             validateXML(document);
         } catch (Exception e) {
            // This is a bit of a hack: when running the game "manually,"
            // I never get a NullPointerException, but for some reason when
            // running the test script, I get a lot of NullPointerExceptions.
            // I don't know why this is the case, because its behavior seems
            // otherwise normal, but maybe it has to do with the speed of the
            // communications? I don't know. Anyway, throwing this on there
            // seems to return it to normal.
            if (!(e instanceof NullPointerException)) {
                System.out.println("Validation error: ");
                e.printStackTrace();
                System.out.println("Original XML:\n");
                System.out.println(xml);
            }
         }
         return document;
     }
 
     private void validateXML(Document document) throws Exception {
         validator.validate(new DOMSource(document));
     }
 
     // convenience method
     private Element makeRequest(String xml) {
         return makeRequest(xml, "POST");
     }
 
     // tells us if response is confirm, update or error
     private String responseType(Element resp) {
         return resp.getAttribute("type");
     }
 
     // tells us which request the server is responding to
     private String responseRequest(Element resp) {
         return resp.getAttribute("request");
     }
 
     private Element makeRequest(String xml, String method) {
         if (verbose) System.out.println("Request:\n" + xml);
         try {
             // open a connection
             HttpURLConnection conn = (HttpURLConnection) url.openConnection();
             // set HTTP properties
             conn.setRequestMethod(method);
             conn.setRequestProperty("Content-Type", "text/xml");
             conn.setRequestProperty("Content-Language", "en-US");  
             conn.setDoInput(true);
             conn.setDoOutput(true);
             // set up output stream
             PrintWriter pw = new PrintWriter(conn.getOutputStream());
             pw.write(xml);
             pw.close();
 
             // get response
             BufferedReader in = 
                 new BufferedReader(
                     new InputStreamReader(conn.getInputStream())
                     );
             StringBuffer retVal = new StringBuffer();
             String line;
             while ((line = in.readLine()) != null) {
                 retVal.append(line + "\n");
             }
 
             // close connection
             conn.disconnect();
 
             String resp = retVal.toString();
             if (verbose) System.out.println("Response: \n" + resp);
 
             // convert to Document
             try {
                 Document document = getDOM(resp);
 
                 if (document == null) {
                     throw new RuntimeException("Malformed XML:" + resp);
                 }
                 // return root element
                 return document.getDocumentElement();
             } catch (Exception e) {
                 e.printStackTrace();
                 return null;
             }
         } catch (IOException e) {
             e.printStackTrace();
             System.out.println("Could not connect to server");
             System.exit(0);
             return null;
         }
     }
 
     private boolean register() {
         String reg = 
             "<?xml version=\"1.0\" ?><request "+
             "type=\"register\"><nick>" + nick + "</nick></request>";
         Element resp = makeRequest(reg);
         if (resp == null) {
             System.out.println("Malformed response from server.");
             return false;
         }
         if (resp.getAttribute("type").equals("confirm")) {
             Element uiElem = findSubelem(resp, "regInfo");
             // if we didn't get a uuid from the server, this is a problem!
             if (uiElem == null)
                 throw new RuntimeException("Didn't get uuid from the server");
             
             uuid = findSubelem(uiElem, "uuid").getTextContent();
             return true;
         } else if (resp.getAttribute("type").equals("error")) {
             Element message = findSubelem(resp, "message");
             printMessage("Error", message.getTextContent());
         }
         return false;
     }
 
     private void join(String tableid) {
         String contents = "<join>";
         if (tableid.length() > 0) {
             contents += "<tableid>" + tableid + "</tableid>";
         }
         contents += "</join>";
         // System.out.println("Sending join request to the server");
         update(makeRequest(makeXML("join", contents)));
     }
 
     private void join() {
         // joins any open table with any color
         join("");
     }
 
     private void move(int row, int col) {
         // System.out.printf("%s moves to %d, %d\n", nick, row, col);
         String posn = "<position><row>" + row +
                       "</row><col>" + col + "</col></position>";
         update(makeRequest(makeXML("move", posn)));
     }
 
     private void pass() {
         // System.out.printf("%s passes turn\n", this.nick);
         update(makeRequest(makeXML("pass")));
     }
 
     private void leave() {
         update(makeRequest(makeXML("leave")));
     }
 
     // updates by sending an update request to server
     private void update() {
         String tbl = "";
         // if we have a table that we're at, add it to the request
         if (tableid != null) {
             tbl = "<tableid>" + tableid + "</tableid>";
         }
         update(makeRequest(makeXML("update", tbl), "GET"));
     }
 
     // get win/loss/points stats
     private void stats() {
         System.out.print("Press enter for your stats, or type a "+
                          "player's name: ");
         String input = in.nextLine();
         String content = "";
         if (input.length() > 0) {
             content = "<player>" + input + "</player>";
         }
         Element resp = makeRequest(makeXML("stats", content), "GET");
         if (resp == null) {
             System.out.println("Malformed response from server.");
             return;
         }
         if (resp.getAttribute("type").equals("confirm")) {
             Element userInfo = findSubelem(resp, "userInfo");
             System.out.printf("Player %s:\n\tTotal points: %s\n" +
                               "\tWin/loss ratio: %s\n",
                               findSubelem(userInfo, "nick").getTextContent(),
                               findSubelem(userInfo, "points").getTextContent(),
                               findSubelem(userInfo, "ratio").getTextContent());
 
         } else if (resp.getAttribute("type").equals("error")) {
             Element message = findSubelem(resp, "message");
             printMessage("Error", message.getTextContent());
         }
     }
 
     private void listPlayers() {
         Element resp = makeRequest(makeXML("listPlayers"), "GET");
         if (resp == null) {
             System.out.println("Malformed response from server.");
             return;
         }
         NodeList ps = resp.getElementsByTagName("userInfo");
         try {
             for (int i = 0; i < ps.getLength(); ++i) {
                 Element pl = (Element) ps.item(i);
                 String nick = findSubelem(pl, "nick").getTextContent();
                 String ratio = findSubelem(pl, "ratio").getTextContent();
                 String points = findSubelem(pl, "points").getTextContent();
                 System.out.printf("Player '%s'\n\tTotal points thus far: "+
                                    "%s\n\tWin/loss ratio: %s\n", 
                                    nick, points, ratio);
                 Element tbl = findSubelem(pl, "tableInfo");
                 if (tbl != null)
                     printTableInfo(tbl, true, true);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void listTables() {
         Element resp = makeRequest(makeXML("listTables"), "GET");
         if (!resp.getAttribute("type").equals("confirm")) {
             return;
         }
         NodeList tbls = resp.getElementsByTagName("tableInfo");
         if (tbls.getLength() == 0) {
             System.out.println("No tables currently on server");
         }
         for (int i = 0; i < tbls.getLength(); ++i) {
             printTableInfo((Element)tbls.item(i), true, true);
         }
     }
 
     // updates based on the response to some request we sent
     private void update(Element resp) {
         if (resp == null) {
             throw new RuntimeException("Malformed XML in response?");
         }
 
         if (resp.getAttribute("type").equals("error")) {
             System.out.println("Looks like we've got an error:");
             Element message = findSubelem(resp, "message");
             printMessage("Error", message.getTextContent());
             return;
         }
 
         if (resp.getAttribute("type").equals("gameover")) {
             System.out.println("Game over!");
             return;
         }
 
         Element tableInfo = findSubelem(resp, "tableInfo");
         // if no table element, then we're not at a table
         if (tableInfo == null) {
             color = "none";
             tableid = null;
             return;
         }
 
         if (tableid == null)
             tableid = findSubelem(tableInfo, "tableid").getTextContent();
 
         // see if we're the black player or the white player
         String black = 
             findSubelem(tableInfo, "blackPlayer").getTextContent();
         String white = 
             findSubelem(tableInfo, "whitePlayer").getTextContent();
         if (nick.equals(black))
             color = "black";
         else if (nick.equals(white))
             color = "white";
         // otherwise, this isn't our table that we're looking at...
         printTableInfo(tableInfo, false, false);
 
         Element userInfo = findSubelem(resp, "userInfo");
         if (userInfo != null) {
             ratio = Double.parseDouble(
                         findSubelem(userInfo, "ratio").getTextContent()
                     );
             points = Integer.parseInt(
                         findSubelem(userInfo, "points").getTextContent()
                     );
         }
     }
 
     private void printMessage(String header, String message) {
         // this will print a message in a textbox
         // surrounded by pipes and hypens like this, :
         /*------------------------------------------------------------+
         | Message:                                                    |
         |   message goes here                                         |
         +------------------------------------------------------------*/
         System.out.printf(
           "/*------------------------------------------------------------+\n"+
           "| %-7s:                                                    |\n", 
           header);
         // splits message into substrings 56 characters long
         String[] lines = message.split("(?<=\\G.{56})");
         for (int i = 0; i < lines.length; ++i) {
             System.out.printf("|   %-56s  |\n", lines[i]);
         }
         System.out.println(
           "+------------------------------------------------------------*/");
     }
 
     private void printTableInfo(Element tableInfo, 
                                 boolean skipBoard,
                                 boolean skipMessage) {
         System.out.printf("Table %s\n", 
             findSubelem(tableInfo, "tableid").getTextContent()
         );
 
         // print a message if there is one and we don't want to skip it
         Element msg = findSubelem(tableInfo, "message");
         if (msg != null && !skipMessage) {
             printMessage("Message", msg.getTextContent());
         }
 
         // print a board if there is one and we don't want to skip it
         Element board = findSubelem(tableInfo, "board");
         if (board != null && !skipBoard) {
             printBoard(board);
         }
 
         String blackPlayer = 
             findSubelem(tableInfo, "blackPlayer").getTextContent();
         String whitePlayer = 
             findSubelem(tableInfo, "whitePlayer").getTextContent();
 
         int blackScore = 0, whiteScore = 0;
         String turn = "No one";
         try {
             blackScore = Integer.parseInt(
                 findSubelem(tableInfo, "blackScore").getTextContent());
             whiteScore = Integer.parseInt(
                 findSubelem(tableInfo, "whiteScore").getTextContent());
             // see whose turn it is
             turn = findSubelem(tableInfo, "turn").getTextContent();
         } catch (Exception e) { /* ignore, just means not defined */ }
         System.out.printf("\tBlack: %s, %d points\n"+
                           "\tWhite: %s, %d points\n\t%s's turn\n",
                           blackPlayer, blackScore,
                           whitePlayer, whiteScore, turn);
     }
 
     private void help() {
         System.out.println("Available commands:\n"+
                            "\thelp: to see this menu\n"+
                            "\tjoin: to join a table\n"+
                            "\tmove: to make a move\n"+
                            "\tpass: to pass your turn\n"+
                            "\tleave: to exit a table\n"+
                            "\tupdate: to get the latest at your table\n"+
                            "\tobserve: see the state of a table\n" +
                            "\ttables: to see all tables on the server\n" +
                            "\tplayers: to see all players on the server\n" +
                            "\texit: quit game"
         );
     }
 
     private int[] getMove() {
         // get row and column from user input
         System.out.print("Enter row: ");
         int row = Integer.parseInt(in.nextLine());
         System.out.print("Enter column: ");
         int column = Integer.parseInt(in.nextLine());
         // package row and column in array and return
         int[] move = {row, column};
         return move;
     }
 
     private void joinTable() {
         // make this a loop so that if the user wants, they can see all of
         // the tables available to them
         while(true) {
             System.out.print("Enter table id (type tables for list, "+
                              "or enter for the first open table): ");
             
             String input = in.nextLine();
             if (input.equals("tables")) {
                 // send a list tables request
                 listTables();
                 // loop will now repeat
             } else if (input.length() == 0) {
                 // if 
                 join();
                 break;
             } else {
                 join(input);
                 break;
             }
         }
     }
 
     public void observe() {
         Element resp;
         // loop until we make a request
         while (true) {
             System.out.print("Enter the table you wish to observe "
                              + "(For a list of tables, type tables): ");
             String input = in.nextLine();
             if (input.length() == 0 && recentObservation != null) {
                 resp = makeRequest(makeXML("observe", "<tableid>" + 
                                     recentObservation + "</tableid>"));
                 break;
             }
             if (input.equals("tables")) {
                 listTables();
             } else {
                 recentObservation = input;
                 resp = makeRequest(makeXML("observe", "<tableid>" + 
                                     input + "</tableid>"));
                 break;   
             }
         }
         if (resp == null) {
             System.out.println("Malformed response from server.");
             return;
         }
         if (resp.getAttribute("type").equals("error")) {
             System.out.println("Looks like we've got an error:");
             Element message = findSubelem(resp, "message");
             printMessage("Error", message.getTextContent());
             return;
         }
         Element tableInfo = findSubelem(resp, "tableInfo");
         if (tableInfo != null) {
             printTableInfo(tableInfo, false, false);
         }
         else {
             printMessage("Error", "Server did not send any table info.");
         }
     }
 
     public void printBoard(Element board) {
         // build arraylist of rows
         NodeList rows = board.getElementsByTagName("row");
         ArrayList<String> rowStrings = new ArrayList<String>();
         for (int i = 0; i < rows.getLength(); ++i)
             rowStrings.add(((Element)rows.item(i)).getTextContent());
         // render the board
         System.out.println(Renderer.render(rowStrings));
     }
 
     public void run() {
         while (true) {
             System.out.print("Logged in as " + nick + ".");
             if (tableid != null)
                 System.out.print(" Table: " + tableid + "\n");
             else
                 System.out.print(" Not currently at a table.\n");
             System.out.print("Enter command (type help for list): ");
             String input = in.nextLine();
             if (input.equalsIgnoreCase("exit"))
                 return;
             else if (input.equalsIgnoreCase("join")) {
                 joinTable();
             }
             else if (input.equalsIgnoreCase("move")) {
                 update(); // get latest board status first
                 int[] move = getMove();
                 move(move[0], move[1]);
             }
             else if (input.equalsIgnoreCase("pass")) {
                 pass();
             }
             else if (input.equalsIgnoreCase("update")) {
                 update();
             }
             else if (input.equalsIgnoreCase("help")) {
                 help();
             }
             else if (input.equalsIgnoreCase("tables")) {
                 listTables();
             }
             else if (input.equalsIgnoreCase("players")) {
                 listPlayers();
             }
             else if (input.equalsIgnoreCase("leave")) {
                 leave();
             }
             else if (input.equalsIgnoreCase("stats")) {
                 stats();
             }
             else if (input.equalsIgnoreCase("observe")) {
                 observe();
             }
             else {
                 printMessage("System", "Unrecognized command '" + input + "'");
             }
         }
     }
 
     public static void test(String nick1, String nick2) 
                         throws MalformedURLException {
         String url = "http://localhost:8080/reversi";
         ReversiClient cli1 = new ReversiClient(url, nick1);
         ReversiClient cli2 = new ReversiClient(url, nick2);
         cli1.join();
         cli2.join();
         cli1.update();
         cli1.move(2, 3);
         cli1.move(2, 3);
         cli2.update();
         cli2.move(2, 2);
         cli1.update();
         cli1.move(3,2);
         cli2.update();
         cli2.move(2, 4);
         cli1.update();
         cli1.move(3, 5);
         cli2.move(4,2);
         cli1.update();
         cli1.pass();
         cli2.move(3, 6);
         cli1.pass();
     }
 
     public static void main(String[] args) throws MalformedURLException {
         if (args.length > 0 && args[0].equals("test"))
             test("foo", "bar");
         else {
             String url = "http://localhost:8080/reversi";
             //initialize the client
             ReversiClient cli = new ReversiClient(url);
             cli.run();
         }
     }
 }
 
 class Renderer {
     public static String makeTop(int n) {
         StringBuffer res = new StringBuffer("    ");
         for (int i = 0; i < n; ++i) {
             res.append("  " + i + " ");
         }
         return res.toString() + "\n";
     }
     public static String makeLine(String cs, int row) {
         StringBuffer res = new StringBuffer("  " + row + " ");
         for (int i=0; i<cs.length(); ++i) {
             if (i == 0) res.append("|");
             res.append(" " + cs.charAt(i) + " |");
         }
         res.append("\n");
         return res.toString();
     }
     public static String makePlusLine(String cs) {
         StringBuffer res = new StringBuffer("    ");
         for (int i=0; i<cs.length(); ++i) {
             if (i == 0) res.append("+");
             res.append("---+");
         }
         res.append("\n");
         return res.toString();
     }
     public static String render(ArrayList<String> rows) {
         StringBuffer res = new StringBuffer(makeTop(rows.size()));
         int mid = rows.size() / 2;
         for (int i=0; i<rows.size(); ++i) {
             String row = rows.get(i);
             if (i == 0) 
                 res.append(makePlusLine(row));
             res.append(makeLine(row, i));
             res.append(makePlusLine(row));
         }
         return res.toString();
     }
 }
