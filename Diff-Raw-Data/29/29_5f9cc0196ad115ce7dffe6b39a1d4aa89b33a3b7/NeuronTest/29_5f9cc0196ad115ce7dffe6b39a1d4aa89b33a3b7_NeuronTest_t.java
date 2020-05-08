 package com.youkaicountry.youml.test;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import com.youkaicountry.youml.module.BiasUnit;
 import com.youkaicountry.youml.module.Module;
 import com.youkaicountry.youml.module.PassThroughLayer;
 import com.youkaicountry.youml.module.SigmoidLayer;
 import com.youkaicountry.youml.module.connection.Connection;
 import com.youkaicountry.youml.module.connection.FullConnection;
 import com.youkaicountry.youml.netgraph.NetGraph;
 
 public class NeuronTest
 {
     
     private double sigmoid(double z)
     {
         return 1.0/(1.0+Math.exp(-z));
     }
     
     @Test
     public void test_sigmoid_layer()
     {
         /*
          * Test the sigmoid layer object
          */
         double[] cases_in = {1.0, 2.2, 3.4, 6.0};
         double[] cases_out = new double[cases_in.length];
         for (int i = 0; i < cases_out.length; i++)
         {
             cases_out[i] = sigmoid(cases_in[i]);
         }
         
         SigmoidLayer s = new SigmoidLayer("sigmoid", cases_in.length);
         s.activate(cases_in);
         
         for (int i = 0; i < s.output_buffer.length; i++)
         {
             assertEquals(s.output_buffer[i], cases_out[i], .000001);
         }
         //fail("Not yet implemented");
         return;
     }
     
     @Test
     public void test_graph()
     {
         // o\ /o\
         //   o   o
         // o/ \o/
         SigmoidLayer in0 = new SigmoidLayer("in0", 2);
         SigmoidLayer in1 = new SigmoidLayer("in1", 2);
         SigmoidLayer hid0 = new SigmoidLayer("hid0", 2);
         SigmoidLayer hid1 = new SigmoidLayer("hid1", 2);
         SigmoidLayer hid2 = new SigmoidLayer("hid2", 2);
         SigmoidLayer out0 = new SigmoidLayer("out0", 2);
         Connection c0 = new FullConnection("c0", in0, hid0);
         Connection c1 = new FullConnection("c1", in1, hid0);
         Connection c2 = new FullConnection("c2", hid0, hid1);
         Connection c3 = new FullConnection("c3", hid0, hid2);
         Connection c4 = new FullConnection("c4", hid1, out0);
         Connection c5 = new FullConnection("c5", hid2, out0);
         Module[] inputs = new Module[] {in0, in1};
         Module[] hidden = new Module[] {hid0, c1, c5, hid2, c0, c2, hid1, c4, c3};
         Module[] outputs = new Module[] {out0};
         
         NetGraph ng = new NetGraph(inputs, hidden, outputs);
         String a = ng.sorted_modules[0].name;
         String b = ng.sorted_modules[1].name;
         if (a == "c0") 
         {
             assertTrue(b == "c1");
         }
         else
         {
             assertTrue(b == "c0");
         }
         assertTrue(ng.sorted_modules[2].name == "hid0");
         a = ng.sorted_modules[3].name;
         b = ng.sorted_modules[4].name;
         if (a == "c2") 
         {
             assertTrue(b == "c3");
         }
         else
         {
             assertTrue(b == "c2");
         }
         a = ng.sorted_modules[5].name;
         b = ng.sorted_modules[6].name;
         if (a == "hid1") 
         {
             assertTrue(b == "hid2");
         }
         else
         {
             assertTrue(b == "hid1");
         }
         a = ng.sorted_modules[7].name;
         b = ng.sorted_modules[8].name;
         if (a == "c4") 
         {
             assertTrue(b == "c5");
         }
         else
         {
             assertTrue(b == "c4");
         }
         return;
     }
     
     @Test
     public void test_feed_forward()
     {
         //construct a simple 2 hidden layer feed-forward network
         PassThroughLayer inp0 = new PassThroughLayer("inp0", 2);
         BiasUnit bhid0 = new BiasUnit("bhid0");
         SigmoidLayer hid0 = new SigmoidLayer("hid0", 8);
         Connection c0 = new FullConnection("c0", inp0, hid0);
         Connection c1 = new FullConnection("c1", bhid0, hid0);
         BiasUnit bhid1 = new BiasUnit("bhid1");
         SigmoidLayer hid1 = new SigmoidLayer("hid1", 8);
         Connection c2 = new FullConnection("c2", hid0, hid1);
         Connection c3 = new FullConnection("c3", bhid1, hid1);
         BiasUnit bout0 = new BiasUnit("bout0");
         SigmoidLayer out0 = new SigmoidLayer("out0", 3);
         Connection c4 = new FullConnection("c4", bout0, out0);
         Connection c5 = new FullConnection("c5", hid1, out0);
         Module[] inputs = new Module[] {inp0};
         Module[] hidden = new Module[] {hid0, c1, bhid1, bout0, c0, c5, bhid0, c2, hid1, c4, c3};
         Module[] outputs = new Module[] {out0};
         NetGraph ng = new NetGraph(inputs, hidden, outputs);
         //for (int i = 0; i < ng.sorted_modules.length; i++)
         //    System.out.println(ng.sorted_modules[i].name);
         return;
     }
    
    @Test
    public void test_single_parameter()
    {
        return;
    }
    
 
 }
