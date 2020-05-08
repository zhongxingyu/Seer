 package com.youkaicountry.youml.module;
 
 import java.util.Arrays;
 
 import com.youkaicountry.youml.parameter.ParameterVector;
 
 //TODO: Add offsets for input/output
 
 //TODO: Will implement evolvable, and point to the pv evolvable components
 public abstract class Module
 {
     public double[] input_buffer;
     public double[] output_buffer;
     public double[] input_error_buffer;
     public double[] output_error_buffer;
     
     public int input_dim;
     public int output_dim;
     
     public String name;
     
     public ParameterVector pv;
     
    public Module(String name, int input_dim, int output_dim, int parameters)
     {
         this.name = name;
        this.moduleInit(input_dim, output_dim, parameters);
         return;
     }
     
     public Module(String name)
     {
         this.name = name;
     }
     
    public void moduleInit(int input_dim, int output_dim, int parameters)
     {
        //this.pv = new ParameterVector(parameters);
         this.input_dim = input_dim;
         this.output_dim = output_dim;
         this.input_buffer = null;
         this.output_buffer = new double[output_dim];
         this.input_error_buffer = null;
         this.output_error_buffer = new double[output_dim];
         return;
     }
     
     public void step()
     {
         this.forwardProp(this.input_buffer, this.output_buffer);
         return;
     }
     
     public void activate(double[] input)
     {
         //TODO: ensure input is the length of input_dim
         this.input_buffer = input;
         this.step();
         return;
     }
     
     public abstract void forwardProp(double[] input, double[] output);
     public abstract void backProp();
 }
