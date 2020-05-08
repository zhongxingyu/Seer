 package com.youkaicountry.youml.test;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.Random;
 
 import org.junit.Test;
 
 import com.youkaicountry.youml.data.TrainingData;
 import com.youkaicountry.youml.module.BiasUnit;
 import com.youkaicountry.youml.module.Module;
 import com.youkaicountry.youml.module.connection.Connection;
 import com.youkaicountry.youml.module.connection.FullConnection;
 import com.youkaicountry.youml.module.layer.LinearLayer;
 import com.youkaicountry.youml.module.layer.SigmoidLayer;
 import com.youkaicountry.youml.module.network.FeedForwardNetwork;
 import com.youkaicountry.youml.trainers.BackPropTrainer;
 
 public class TrainerTest 
 {
 	@Test
     public void test_back_prop()
     {
         LinearLayer inp0 = new LinearLayer("inp0", 2);
         SigmoidLayer out0 = new SigmoidLayer("out0", 2);
         Connection c0 = new FullConnection("c0", inp0, out0);
         Module[] inputs = new Module[] {inp0};
         Module[] hidden = new Module[] {c0};
         Module[] outputs = new Module[] {out0};
         FeedForwardNetwork n = new FeedForwardNetwork("ffn", inputs, hidden, outputs);
         n.setParam(0, .2);
         n.setParam(1, .1);
         n.setParam(2, .02);
         n.setParam(3, .3);
         TrainingData td = new TrainingData();
         td.addCase(new double[] {1.0, 1.0}, new double[] {sigmoid(2.0), sigmoid(2.0)});
         BackPropTrainer bpt = new BackPropTrainer(n, td, 1.0);
        double ierror = Math.abs(bpt.train()); //get the error on the training set
        for (int i = 0; i < 100; i++)
         {
         	bpt.train();
         }
        assertTrue(Math.abs(bpt.train()) < ierror);
         return;
     }
     
     @Test
     public void test_backprop_xor()
     {
         //construct a simple 2 hidden layer feed-forward network
         LinearLayer inp0 = new LinearLayer("inp0", 2);
         BiasUnit bhid0 = new BiasUnit("bhid0", 1.0);
         SigmoidLayer hid0 = new SigmoidLayer("hid0", 3);
         Connection c0 = new FullConnection("c0", inp0, hid0);
         Connection c1 = new FullConnection("c1", bhid0, hid0);
         BiasUnit bhid1 = new BiasUnit("bhid1", 1.0);
         SigmoidLayer hid1 = new SigmoidLayer("hid1", 3);
         Connection c2 = new FullConnection("c2", hid0, hid1);
         Connection c3 = new FullConnection("c3", bhid1, hid1);
         BiasUnit bout0 = new BiasUnit("bout0", 1.0);
         SigmoidLayer out0 = new SigmoidLayer("out0", 1);
         Connection c4 = new FullConnection("c4", bout0, out0);
         Connection c5 = new FullConnection("c5", hid1, out0);
         Module[] inputs = new Module[] {inp0};
         Module[] hidden = new Module[] {hid0, c1, bhid1, bout0, c0, c5, bhid0, c2, hid1, c4, c3};
         Module[] outputs = new Module[] {out0};
         FeedForwardNetwork n = new FeedForwardNetwork("ffn", inputs, hidden, outputs);
         Random r = new Random(1212);
         for (int i = 0; i < n.size(); i++)
         {
             n.setParam(i, r.nextDouble()*.1);
         }
         
         TrainingData td = new TrainingData();
         td.addCase(new double[] {1.0, 1.0}, new double[] {0.0});
         td.addCase(new double[] {1.0, 0.0}, new double[] {1.0});
         td.addCase(new double[] {0.0, 1.0}, new double[] {1.0});
         td.addCase(new double[] {0.0, 0.0}, new double[] {0.0});
         BackPropTrainer bpt = new BackPropTrainer(n, td, .01);
        double ierror = Math.abs(bpt.train()); //get the error on the training set
         for (int i = 0; i < 1000; i++)
         {
         	bpt.train();
         }
        assertTrue(Math.abs(bpt.train()) < ierror);
         
         return;
     }
     
     private double sigmoid(double z)
     {
         return 1.0/(1.0+Math.exp(-z));
     }
 }
