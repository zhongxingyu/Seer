 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 
 /*
  * Trieda starajuca sa o komunikaciu medzi nasim programom a spustenymi procesmi
  * 
  * 
  */
 public class Program extends Thread {
     Vertex vertex;
     int id;
     ArrayList<Integer> ports;
     boolean running;
 
     Process process;
     InputStream output;
     OutputStream input;
     PrintWriter in;
 
     public Program(Vertex v, Model m) {
         super();
         vertex = v;
         vertex.program = this;
         running = false;
         this.id = vertex.getID();
         ports = new ArrayList<Integer>();
         for (int i = 0; i < v.edges.size(); ++i) {
             ports.add(i);
         }
     }
 
     @Override
     public void run() {
         super.run();
         System.out.println("bezim " + id);
         try {
             BufferedReader out = new BufferedReader(new InputStreamReader(
                     output));
             String line;
             running = true;
             while ((line = out.readLine()) != null) {
                 if (line.charAt(0) == '@') {
                     String[] parts = line.substring(1).split(":", 2);
                     send(Integer.parseInt(parts[0].trim()), parts[1].trim());
                 }
             }
             out.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public void load(String path) {
         try {
             System.err.println("Loading... " + path);
             process = Runtime.getRuntime().exec(path);
             output = process.getInputStream();
             input = process.getOutputStream();
             in = new PrintWriter(input);
         } catch (Exception e) {
             e.printStackTrace();
         }
         this.init();
         this.start();
     }
 
     public void kill() {
         running = false;
         vertex.program = null;
         this.interrupt();
         process.destroy();
     }
 
     // program sa dozvie pociatocne hodnoty, ako napriklad pocet portov
     public void init() {
         // pocet portov a ich hodnoty
         in.print("* ports : " + ports.size());
         for (int p : ports) {
             in.print(" " + p);
         }
         in.println();
         
         // id
         //TODO skontrolovat anonymitu
         in.println("* id : " + id);
 
         in.flush();
     }
 
     public void send(int port, String content) {
         System.err.println("send " + id + " " + port + " " + content);
 
         // svoj port zmenim na port vrchola
         // TODO spravit efektivnejsie nez cez indexOf
         port = ports.indexOf(port);
         vertex.send(new Message(port, content));
     }
 
    public void receive(Message message) {
        System.err.println("receive " + id + " " + message.toPort + " "
                 + message.content);
 
         in.println("@ " + ports.get(message.toPort) + " : " + message.content);
         in.flush();
     }
 }
