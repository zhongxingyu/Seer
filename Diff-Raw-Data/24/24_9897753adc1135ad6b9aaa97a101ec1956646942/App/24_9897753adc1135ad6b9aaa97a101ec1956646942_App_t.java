 package de.weltraumschaf.neuron;
 
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

 /**
  * Hello world!
  *
  */
public class App {
    public static void main( String[] args ) throws IOException {
        final BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            final String input = inputReader.readLine();

            if ("exit".equals(input)) {
                System.out.println("bye bye!");
                break;
            }

            System.out.println(input);
        }
     }
 }
