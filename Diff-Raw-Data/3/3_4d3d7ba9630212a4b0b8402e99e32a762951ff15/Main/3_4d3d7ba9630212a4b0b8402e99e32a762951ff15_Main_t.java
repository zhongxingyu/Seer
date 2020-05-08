 import input.Event;
 import input.InputParser;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.BufferedWriter;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 
 import output.Output;
 import output.Tag;
 import processor.Processor;
 
 public class Main
 {

 	
 		public static void main(String[] args){
 			//get list of events
 			InputParser input = InputParser.ParserFactory.generate("DukeBasketBall.xml");
 			List<Event> eventList = input.getListOfEvents();
             Collections.sort(eventList);
 			//processor
 			Processor process = new Processor();
 			//output
 			Output o = new Output(eventList);
 			o.generateCalendar();
 
 
 
 		}
 }

