 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jgpfun;
 
 import jgpfun.world2d.FoodFinder;
 import jgpfun.jgp.OpCode;
 import jgpfun.jgp.EvoVM2;
 import java.security.SecureRandom;
 import java.util.Random;
 import jgpfun.world2d.Body2d;
import jgpfun.world2d.PrecisionBody2d;
 
 /**
  *
  * @author hansinator
  */
 public class Organism implements Comparable<Organism> {
 
     public static final double maxForce = 10.0;
 
     public static final double maxSpeed = 4.0;
 
     protected static final Random rnd = new SecureRandom();
 
     protected final EvoVM2 vm;
 
     protected final OpCode[] program;
 
     public final Body2d[] bodies;
 
     public int food;
 
 
     public Organism(OpCode[] program, int worldWidth, int worldHeight, FoodFinder foodFinder) {
         this.program = program;
         this.vm = new EvoVM2(24, program);
         this.food = 0;
 
         bodies = new Body2d[1];
         for (int i = 0; i < bodies.length; i++) {
            bodies[i] = new PrecisionBody2d(rnd.nextInt(worldWidth), rnd.nextInt(worldHeight), rnd.nextDouble(), foodFinder);
         }
     }
 
 
     public void live() throws Exception {
         double left, right, foodDist;
         double scale = 8192.0;
 
         //write input registers
         int inreg = 0;
         for (Body2d b : bodies) {
             b.food = b.foodFinder.findNearestFood(b.x, b.y);
             foodDist = b.foodFinder.foodDist(b.food, b.x, b.y);
 
            //cached cosdir and scale as int are meant to speed this up
            vm.regs[inreg++] = (int) (((PrecisionBody2d) b).cosdir * scale);
            //vm.regs[inreg++] = (int) (Math.cos(b.dir) * scale);
            
             vm.regs[inreg++] = (int) (((b.food.x - b.x) / foodDist) * scale);
             vm.regs[inreg++] = (int) (((b.food.y - b.y) / foodDist) * scale);
         }
 
         vm.run();
 
         //use output values
         for (Body2d b : bodies) {
             //fetch, limit and scale outputs
             left = Math.max(0, Math.min(vm.regs[inreg++], 65535)) / scale;
             right = Math.max(0, Math.min(vm.regs[inreg++], 65535)) / scale;
 
             //move
             b.motor.move(left, right);
         }
     }
 
 
     static Organism randomOrganism(int xmax, int ymax, int progsize, FoodFinder foodFinder) {
         OpCode[] program = new OpCode[rnd.nextInt(progsize)];
 
         for (int i = 0; i < program.length; i++) {
             program[i] = OpCode.randomOpCode(rnd);
         }
 
         return new Organism(program, xmax, ymax, foodFinder);
     }
 
 
     @Override
     public OpCode[] clone() {
         OpCode[] p = new OpCode[program.length];
 
         for (int i = 0; i < program.length; i++) {
             p[i] = program[i].clone();
         }
 
         return p;
     }
 
 
     @Override
     public int compareTo(Organism o) {
         return new Integer(food).compareTo(o.food);
     }
 
 }
