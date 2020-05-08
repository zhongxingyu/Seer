 package com.tomsovi;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * State machine (very very basic parser)
  *
  */
 public class App 
 {
     List<State> states;
     
     public App() {
         State stateOutside = new State("Outside");
         State stateInParens1 = new State("In parentheses 1");
         State stateInParens2 = new State("In parentheses 2");
         State stateError = new State("Error - should not come here");
         states = new ArrayList<State>(Arrays.asList(stateOutside, stateInParens1, stateInParens2, stateError));
        stateOutside.addRoute("(", stateInParens1);
        stateOutside.addRoute(")", stateError);
        stateInParens1.addRoute("(", stateInParens2);
        stateInParens1.addRoute(")", stateOutside);
        stateInParens2.addRoute("(", stateError); // too much parantheses
        stateInParens2.addRoute(")", stateInParens1);
     }
     
     public void execute(List<String> signals) {
         State current = states.get(0);
         for(String signal : signals) {
             System.out.println("----------------");
             System.out.println("Current: " + current.getMessage());
             System.out.println("Received: '" + signal + "'");
             current = current.route(signal);
             System.out.println("New: " + current.getMessage());
         }
         System.out.println("----------------");
     }
     
     public static void main( String[] args )
     {
        List<String> signals = new ArrayList<String>( Arrays.asList("(","x",")") );
         App app = new App();
         app.execute( signals );
     }
 }
