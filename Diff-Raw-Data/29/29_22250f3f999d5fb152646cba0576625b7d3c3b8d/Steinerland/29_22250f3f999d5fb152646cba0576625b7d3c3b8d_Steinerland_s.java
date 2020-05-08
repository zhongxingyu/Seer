 package steinerland;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 
 import graf.WDGimpl;
 import graf.WeightedDirectedGraph;
 
 public class Steinerland {
     protected WeightedDirectedGraph<SteinerlandNode,String> graph;
 
     public Steinerland() {
         //graph = new WDGDijkstra();
         graph = new WDGimpl();
     }
 
     public String search(String from, String hours, String minutes, String to)
     {
         return "Just go to the station and wait for the next train. It'll be there \"soon\".";
     }
 
     public void addRoute(String from, String hoursDeparture, String minutesDeparture, String to, String hoursArrival, String minutesArrival, String train)
     {
         //graph.insertNode(WDGimpl.new Node());
     }
 
     public void loadFile(String path)
     {
         try
         {
             FileReader fileReader = new FileReader(path);
             BufferedReader bufferedReader = new BufferedReader(fileReader);
 
             // assuming format:
             // <train>
             // <departureCity>:<departureTime>-<arrivalCity>:<arrivalTime>
             //
             // example:
             // high-speed commuter train 0501
             // Steinerstadt: 05.40 - El Seco: 06.03
             // El Seco: 06.05 - Neubergstadt: 06.39
             // Neubergstadt: 06.41 - Steinerstadt: 06:58
             String line = bufferedReader.readLine();
             while (line != null) 
             {
                String train = "Unknown train";
                // the formatting in the time table file is shady, it's not uniformed..
                String[] parts = line.split("[.-:]");
                System.out.println(parts);
                if (parts.length == 6)
                     addRoute(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim(), parts[5].trim(), train);
                else
                    train = line;
                 line = bufferedReader.readLine();
             }
 
             bufferedReader.close();
         }
         catch (IOException e)
         {
             System.out.println("[ERROR] Exception: " + e);
         }
     }
 
     public static void main( String[] args ) {
         Steinerland steinerland = new Steinerland();
 
        steinerland.loadFile("data/timetable.tbl");
         new Gui(steinerland);
     }
 }
