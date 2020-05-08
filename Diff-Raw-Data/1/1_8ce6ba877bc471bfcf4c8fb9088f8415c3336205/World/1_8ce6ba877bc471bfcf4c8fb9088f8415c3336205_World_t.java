 package jlarv;
 
 import java.util.*;
 import jlarv.util.*;
 import jlarv.util.tuple.*;
 
 //TODO: add a way to recognize when there aren't anymore engines left, to end the program
 
 /*
     The world defines a way to use multiple engines in the same program.
     That might be useful when you want to have, for example, a title screen
     which is an introduction to the game and then an engine for the game itself.
     On every update call, updates the last pushed mode (the one on top of the
     stack).
 
     It's not a bad idea to define a global World instance on your games, as
     using many different worlds will rarely be needed.
  */
 public class World {
 	private ArrayDeque<Engine> engine_stack; // holds all the engines
 	
 	// Variables to manage post update functions.
 	private ArrayList<Tuple> postupdate_functions;
 	private final TupleType functionArgsTupleType = 
 			TupleType.DefaultFactory.create(
 					Callable.class, //the function, will need to implement a call method
 					ArrayList.class //arguments of the function
 					);
 	
 	
 	public World() {
 		engine_stack = new ArrayDeque<Engine>();
 		postupdate_functions = new ArrayList<Tuple>();
 	}
 	
 	/**
 	 * Adds the given Engine to the engine stack, giving it instant priority
 	 * when updating.
 	 */
 	public void push(Engine engine) {
 		engine_stack.push(engine);
 	}
 	
 	/**
 	 * Removes and returns the Engine on top of the stack.
 	 */
 	public Engine pop() {
 		return engine_stack.pop();
 	}
 	
 	/**
 	 * Exchanges the actual engine for the given engine.
 	 * @return the last active engine.
 	 */
 	public Engine exchange(Engine engine) {
 		Engine popped = this.pop();
 		push(engine);
 		return popped;		
 	}
 	
 	/**
 	 * Updates only the system on top of the stack.
 	 * Also calls any post update function and resets the post update functions list.
 	 */
 	public void update() {
 		engine_stack.peek().update();
 		
 		for (Tuple tuple : postupdate_functions) {
 			Callable function = tuple.getNthValue(0);
 			ArrayList<Object> args = tuple.getNthValue(1);
 			
 			function.call(args);
 		}
		postupdate_functions.clear();
 	}
 	
 	/**
 	 * Adds a new function to be called at the end of the next update loop.
 	 * @param func - the function we want to call
 	 * @param args - array list with the arguments we want to use in the function
 	 */
 	public void addPostUpdateFunction(Callable func, ArrayList<Object> args) {
 		final Tuple tuple = functionArgsTupleType.createTuple(func, args);
 		postupdate_functions.add(tuple);
 	}
 }
