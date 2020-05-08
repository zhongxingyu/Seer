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
        System.out.println(from + ", " + hoursDeparture + ", " + minutesDeparture + ", " + to + ", " + hoursArrival + ", " + minutesArrival + ", " + train);
         //graph.insertNode(WDGimpl.new Node());
     }
 
     public void loadFile(String path)
     {
         try
         {
             FileReader fileReader = new FileReader(path);
             BufferedReader bufferedReader = new BufferedReader(fileReader);
 
            // the formatting in the time table file is shady, it's not uniformed..
             // assuming format:
             // <train>
             // <departureCity>:<departureTime>-<arrivalCity>:<arrivalTime>
             //
             // example:
             // high-speed commuter train 0501
             // Steinerstadt: 05.40 - El Seco: 06.03
             // El Seco: 06.05 - Neubergstadt: 06.39
             // Neubergstadt: 06.41 - Steinerstadt: 06:58
            // REGEXP to match a line: [^:]*: *[^ ]* *- *[^:]*: *[^$]*
             String line = bufferedReader.readLine();
            String train = "Unknown train";
             while (line != null) 
             {
                System.out.println("line=" + line);
                // Route
                if (line.matches("[^:]*: *[^ ]* *- *[^:]*: *[^$]*"))
                {
                    String[] parts = line.split("[:.-]");
                    //System.out.println("From" + parts[0].trim() + ",h" + parts[1].trim() + ",min" + parts[2].trim() + ",To" + parts[3].trim() + ",h" + parts[4].trim() + ",min" + parts[5].trim() + ",train" + train);
                     addRoute(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim(), parts[5].trim(), train);
                }
                else // Train name (or blank line, but who-the-heck cares)
                    train = line.trim();

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
 
        steinerland.loadFile("data/timetable-fixed.tbl");
         new Gui(steinerland);
     }
 }
