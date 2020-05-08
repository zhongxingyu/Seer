 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 import org.junit.Test;
 
 public class ANNTest {
 
     @Test
     public void test_setWeights(){
        ANN net = new ANN(2,2,1,100,.6);
        for(int x=0; x<net.numHidden;x++){
            for(int y=0; y<net.inputDimension;y++){
                 assertFalse(net.hiddenWeights[x][y] ==0);
            }
            for(int z=0;z<net.numOutputClasses; z++){
                assertFalse(net.outputWeights[z][x]  == 0);
            }
        }
        net.setWeightsToOne();
         for(int x=0; x<net.numHidden;x++){
            for(int y=0; y<net.inputDimension;y++){
                 assertEquals(1,net.hiddenWeights[x][y],.00001);
            }
            for(int z=0;z<net.numOutputClasses; z++){
                assertEquals(1, net.outputWeights[z][x],.00001);
            }
        }
     }
 
     @Test
     public void test_inputToHidden(){
         double  input [] = {2.0,2.0,2.0};
         ANN net = new ANN(3,2,3,10,.5);
         net.setWeightsToOne();
         net.calcHiddenFromInput(input);
         assertEquals(-1, net.hiddenOut[0],0);
         for(int x=1;x<net.hiddenOut.length; x++){
             assertEquals(net.sigmoid(7),net.hiddenOut[x],0.01);
         }
     }
   
     //@Test
     public void test_hiddentToOutput(){
         double sum =0;
         double  input [] = {2.0,2.0,2.0};
         ANN net = new ANN(3,3,3,10,.5);
         net.setWeightsToOne();
         net.calcHiddenFromInput(input);
         net.calcOutputFromHidden();
         for(int x=0;x<net.numOutputClasses; x++){
             sum+= net.output[x];
         }
             assertEquals(1 ,sum, .0001);
 
     }
 
     @Test
     public void test_feedForward(){
         double In[][] ={ {3.4,7.0},{1.0,0.3},{0.0,1.4},{1.0,1.0}};
         ANN net = new ANN(2,3,2,10,.5);
         double [] e = net.feedForward(In[0]);
         assertArrayEquals(e, net.output, .001);
         e = net.feedForward(In[3]);
         assertArrayEquals(e, net.output, .001);
         for(int i=0; i<e.length;i++){
             System.out.println("output at class "+i+" is  "+e[i]);
         }
         for(int x=0; x<net.numHidden;x++){
            for(int y=0; y<net.inputDimension;y++){
                 //System.out.println(
                 //"Weight for ("+ x +","+ y +"): " + net.hiddenWeights[x][y]);
            }
            for(int z=0;z<net.numOutputClasses; z++){
                 //System.out.println(
                 //"Weight for ("+x+","+z+"): " + net.outputWeights[z][x]);
            }
        }
     }
 
     @Test
     public void test_calcDeltas(){
         double In[][] ={ 
            {3.4,7.0},
            {1.0,0.3},
            {0.0,1.4},
            {1.0,1.0}};
 
         double [][] expect = { {3.0,1.0},{1.3,9.2},{3.0,1.0},{2.0,7.0} };
 
         ANN net = new ANN(2,3,2,10,.2);
         double [][] old_weight_hidden = 
            new double[net.numHidden][net.inputDimension];
         double [][] old_weight_output =
            new double[net.numOutputClasses][net.numHidden];
 
         // copy the weights to old weights
         for(int x=0; x<net.numHidden;x++){
            for(int y=0; y<net.inputDimension;y++){
                 old_weight_hidden[x][y] =  net.hiddenWeights[x][y];
            }
            for(int z=0;z<net.numOutputClasses; z++){
                 old_weight_output[z][x] =  net.outputWeights[z][x];
            }
        }
        // make sure deltas are set to zero
         for(int x=0; x<net.numHidden;x++){
            for(int y=0; y<net.inputDimension;y++){
                 assertEquals(0, net.deltaHidden[x][y],0.00001);
            }
            for(int z=0;z<net.numOutputClasses; z++){
                 assertEquals(0, net.deltaOut[z][x],0.00001);
            }
        }
         double [] e = net.feedForward(In[0]);
         net.compute(In[0],expect[0]);
 
         
         net.updateOutputWeight();
 
         assertThat(old_weight_hidden, equalTo(net.hiddenWeights));
         assertThat(old_weight_output, not(equalTo(net.outputWeights)));
 
         net.updateHiddenWeight();
 
         assertThat(old_weight_hidden, not(equalTo(net.hiddenWeights)));
         assertThat(old_weight_output, not(equalTo(net.outputWeights)));
 
 
     }
 
     @Test
     public void test_offline_learning(){
         double In[][] ={ 
            {3.4,7.0},
            {1.0,0.3},
            {3.0,1.4},
            {5.0,1.0}};
 
         double [][] expect = { {3.0,1.0},{1.3,9.2},{3.0,1.0},{2.0,7.0} };
 
        ANN net = new ANN(2,2,2,300,.1);
         double [] error = net.offlineLearning(In, expect);
  
     }
     
 
 }
