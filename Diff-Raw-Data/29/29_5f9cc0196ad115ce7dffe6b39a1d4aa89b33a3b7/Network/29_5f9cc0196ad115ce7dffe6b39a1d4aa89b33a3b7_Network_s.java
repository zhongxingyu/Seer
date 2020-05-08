 package com.youkaicountry.youml.module.network;
 
 import com.youkaicountry.youml.module.Module;
 import com.youkaicountry.youml.netgraph.NetGraph;
 
 public abstract class Network extends Module
 {
     protected NetGraph graph;
     
     public Network(String name, Module[] input_modules, Module[] hidden_modules, Module[] output_modules)
     {
         super(name);
         this.graph = new NetGraph(input_modules, hidden_modules, output_modules);
         //TODO: track offsets for each input
         int input_size = 0;
         for (int i = 0; i < input_modules.length; i++)
         {
             input_size += input_modules[i].input_dim;
         }
         int output_size = 0;
         for (int i = 0; i < output_modules.length; i++)
         {
             output_size += output_modules[i].output_dim;
         }
         this.moduleInit(0, 0, 0);
     }
 
 }
