 package edu.stuy.goldfish;
 
 import edu.stuy.goldfish.rules.*;
 
 public class Goldfish {
     private static final String[] RULES = {"Conway", "Conway4", "Life Without Death", "Brian's Brain"};
 
     private Grid _grid;
     private Render _render;
 
     public Goldfish() {
         int width = 128;
         int height = 128;
         _grid = new Grid(width, height);
         _render = new Render(width, height, _grid, RULES);
     }
 
     /* Main method for running the simulator. */
     public void run() {
         setup(_render.rule);
         while (true) {
             if (_render.reset) {
                _render.acquireLock(0);
                 setup(_render.rule);
                _render.releaseLock(0);
                 _render.reset = false;
             }
             if (!_render.paused) {
                 _render.acquireLock(0);
                 doLogic(_render.rule);
                 _render.releaseLock(0);
             }
             _render.setGrid(_grid);
             _render.run();
             _render.sleep();
         }
     }
 
     /* Advance the grid one step using the given algorithm. */
     private void doLogic(String rule) {
         if (rule.equals("Conway"))
             _grid = Conway.run(_grid);
         else if (rule.equals("Conway4"))
             _grid = Conway4.run(_grid);
         else if (rule.equals("Life Without Death"))
             _grid = LifeWithoutDeath.run(_grid);
         else if (rule.equals("Brian's Brain"))
             _grid = BriansBrain.run(_grid);
     }
 
     /* Return the max number of states a patch can be in for the given rule. */
     public static int getMaxStates(String rule) {
         if (rule.equals("Conway"))
             return Conway.states;
         else if (rule.equals("Conway4"))
             return Conway4.states;
         else if (rule.equals("Life Without Death"))
             return LifeWithoutDeath.states;
         else if (rule.equals("Brian's Brain"))
             return BriansBrain.states;
         return 2;
     }
 
     /* Generate a default shape for the grid based on the given rule. */
     private void setup(String rule) {
         if (rule.equals("Conway"))
             Conway.setup(_grid);
         else if (rule.equals("Conway4"))
             Conway4.setup(_grid);
         else if (rule.equals("Life Without Death"))
             LifeWithoutDeath.setup(_grid);
         else if (rule.equals("Brian's Brain"))
             BriansBrain.setup(_grid);
     }
 
     public static void main(String[] args) {
         Goldfish g = new Goldfish();
         g.run();
     }
 }
