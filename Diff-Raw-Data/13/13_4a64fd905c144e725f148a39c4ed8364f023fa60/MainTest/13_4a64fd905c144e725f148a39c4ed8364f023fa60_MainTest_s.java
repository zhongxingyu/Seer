 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package hashJoin;
 
 import gammaContainers.MapReduceBFilter;
 import gammaContainers.MapReduceBloom;
 import gammaContainers.MapReduceGamma;
 import hashJoin.basicConnector.Connector;
 import hashJoin.basicConnector.ReadEnd;
 import java.io.BufferedReader;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Map;
 import java.util.TreeMap;
 
 /**
  *
  * @author bansal
  */
 public class MainTest {
     public static void main(String[] args) throws Exception {
         readRelation_PrintTupleTest("client.txt");
         readRelation_PrintTupleTest("viewing.txt");
         readRelation_Hsplit_PrintTuple();
         readRelation_Hjoin_PrintTupleTest("client.txt","viewing.txt");
         gammaTest("client.txt","viewing.txt");
         readRelation_Hsplit_Merge_PrintTuple();
         bloomTest();
         mapReduceBloomTest();
         testBloomSimulator();
         testBFilterAndPrint();
         testBloomSimulatorFilter();
     }
 
     public static void readRelation_PrintTupleTest(String fileName) {
         // read --> sort --> print
         System.out.println("Starting ReadRelation _ PrintTuple");
         Connector read_A = new Connector("input1");
         ReadRelation r = new ReadRelation(fileName,"clientDB", read_A.getWriteEnd());
         PrintTuple p = new PrintTuple(read_A.getReadEnd());
         r.start();
         p.start();
         System.out.println("-----------------");
         try {
             Thread.sleep(10);
         } catch (InterruptedException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
     }
     
     public static void bloomTest() throws Exception {
         System.out.println("Starting Bloom Test");
         Connector print_bloom = new Connector("print_bloom");
         ReadRelation r = new ReadRelation("client.txt","clientDB", print_bloom.getWriteEnd());
         Connector bloom_join = new Connector("bloom_join");
         PrintTuple p1 = new PrintTuple(bloom_join.getReadEnd());
         Connector bloom_bmap = new Connector("bloom_bmap");
         ReadEnd readEnd = bloom_bmap.getReadEnd();
         Bloom bloom = new Bloom(print_bloom.getReadEnd(), bloom_join.getWriteEnd(), bloom_bmap.getWriteEnd(), 0);
         r.start();
         bloom.start();
         p1.start();       
         System.out.println(readEnd.getNextString());
     }
 
     public static void mapReduceBloomTest() throws Exception {
         System.out.println("Starting Map Reduce Bloom Test");
         Connector print_bloom = new Connector("print_bloom");
         ReadRelation r = new ReadRelation("client.txt","clientDB", print_bloom.getWriteEnd());
         Connector bloom_join = new Connector("bloom_join");
         PrintTuple p1 = new PrintTuple(bloom_join.getReadEnd());
         Connector bloom_bmap = new Connector("bloom_bmap");
         ReadEnd readEnd = bloom_bmap.getReadEnd();
         MapReduceBloom mapReduceBloom = new MapReduceBloom(print_bloom.getReadEnd(), bloom_join.getWriteEnd(), bloom_bmap.getWriteEnd(), 0);
         r.start();
         mapReduceBloom.start();
         p1.start();       
         System.out.println(readEnd.getNextString());
     }
 
     public static void readRelation_Hsplit_PrintTuple() {
         // read --> sort --> print
         //System.out.println("Starting ReadRelation _ Hsplit _  PrintTuple");
         Connector read_A = new Connector("input1");
         ReadRelation r = new ReadRelation("client.txt","clientDB", read_A.getWriteEnd());
         Connector hash_1 = new Connector("pipe1");
         Connector hash_2 = new Connector("pipe2");
         Connector hash_3 = new Connector("pipe3");
         Connector hash_4 = new Connector("pipe4");
         HSplit h = new HSplit(0,read_A.getReadEnd(),hash_1.getWriteEnd(),hash_2.getWriteEnd(),hash_3.getWriteEnd(),hash_4.getWriteEnd());
         PrintTuple p1 = new PrintTuple(hash_1.getReadEnd());
         PrintTuple p2 = new PrintTuple(hash_2.getReadEnd());
         PrintTuple p3 = new PrintTuple(hash_3.getReadEnd());
         PrintTuple p4 = new PrintTuple(hash_4.getReadEnd());
         r.start();
         h.start();
         p1.start();
         p2.start();
         p3.start();
         p4.start();
         //System.out.println("-----------------");
         try {
             Thread.sleep(10);
         } catch (InterruptedException e) {
             e.printStackTrace();
     }
     }
 
     public static void gammaTest(String file1, String file2) {
         System.out.println("Starting Gamma Test...");
         Connector out = new Connector("output");
         MapReduceGamma h = new MapReduceGamma(0, file1, 0, file2,out.getWriteEnd());
         PrintTuple p = new PrintTuple(out.getReadEnd());
         h.start();
         p.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }        
     }
     
     public static void readRelation_Hjoin_PrintTupleTest(String file1, String file2) {
        System.out.println("Starting ReadRelation _ HJoin _ PrintTuple");
         Connector read_A = new Connector("input1");
         ReadRelation r1 = new ReadRelation(file1,file1, read_A.getWriteEnd());
         Connector read_B = new Connector("input2");
         ReadRelation r2 = new ReadRelation(file2,file2, read_B.getWriteEnd());
         Connector out = new Connector("output");
         HJoin h = new HJoin(0, read_A.getReadEnd(),0,read_B.getReadEnd(),out.getWriteEnd());
         PrintTuple p = new PrintTuple(out.getReadEnd());
         r1.start();
         r2.start();
         h.start();
         p.start();
         try {
             Thread.sleep(100);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
 
 //        Connector out2 = new Connector("output2");
 //        MapReduceHJoin mapJ = new MapReduceHJoin(0, file1, 0 , file2,out2.getWriteEnd() );
 //        mapJ.start();
 //        PrintTuple p2 = new PrintTuple(out2.getReadEnd());
 //        p2.start();
 
     }
     public static void readRelation_Hsplit_Merge_PrintTuple() {
         System.out.println("Starting ReadRelation _ Hsplit _ Merge _ PrintTuple");
         Connector read_A = new Connector("input1");
         ReadRelation r = new ReadRelation("client.txt","clientDB", read_A.getWriteEnd());
         Connector hash_1 = new Connector("pipe1");
         Connector hash_2 = new Connector("pipe2");
         Connector hash_3 = new Connector("pipe3");
         Connector hash_4 = new Connector("pipe4");
         Connector out = new Connector("output");
         HSplit h = new HSplit(0,read_A.getReadEnd(),hash_1.getWriteEnd(),hash_2.getWriteEnd(),hash_3.getWriteEnd(),hash_4.getWriteEnd());
         Merge m = new Merge(out.getWriteEnd(),hash_1.getReadEnd(),hash_2.getReadEnd(),hash_3.getReadEnd(),hash_4.getReadEnd() );
         PrintTuple p = new PrintTuple(out.getReadEnd());
         r.start();
         h.start();
         m.start();
         p.start();
         System.out.println("-----------------");
         try {
             Thread.sleep(10);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
     
     public static void testBFilterAndPrint() {
         System.out.println("Starting Test BFilter and Print");
         Connector readBloomRelationConnector = new Connector("print_bloom");
         Connector readBloomFilterRelationConnector = new Connector("print_bloomFilter");
         Connector bloomConnector = new Connector("bloomConnector");
         //Connector hsplitConnector = new Connector("hsplitConnector");
         Connector hBloomJoinConnector = new Connector("hBloomJoinConnector");
         Connector hBloomFilterJoinConnector = new Connector("hBloomFilterJoinConnector");
         
         int fieldNumber = 0;
         
         ReadRelation r_bloom = new ReadRelation("client.txt","clientDB", readBloomRelationConnector.getWriteEnd());
         ReadRelation r_bloomFilter = new ReadRelation("client.txt","clientDB", readBloomFilterRelationConnector.getWriteEnd());
         
         Bloom bloom = new Bloom(readBloomRelationConnector.getReadEnd(),
                 hBloomJoinConnector.getWriteEnd(), bloomConnector.getWriteEnd(), fieldNumber);   
         //BFilter filter = new BFilter(bloomConnector.getReadEnd(), 
         //        readBloomFilterRelationConnector.getReadEnd(), hBloomFilterJoinConnector.getWriteEnd(), fieldNumber);
         
         r_bloom.start();
         r_bloomFilter.start();
         bloom.start();
       
         MapReduceBFilter filter = 
                 new MapReduceBFilter(bloomConnector.getReadEnd(), 
                     readBloomFilterRelationConnector.getReadEnd(), 
                     hBloomFilterJoinConnector.getWriteEnd(), fieldNumber);
         filter.start();
         
         PrintTuple readEnd = new PrintTuple(hBloomFilterJoinConnector.getReadEnd());
         readEnd.start();
  
         try {
             Thread.sleep(5);
 //            while(true) {
 //                String xx = readEnd.getNextString();
 //                if (xx == null) {
 //                    break;
 //                }
 //                System.out.println(xx);
 //            }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     public static void testBloomSimulator() throws Exception {
         Connector bloom_print = new Connector("bloom_print");
         BloomSimulator bloomSimulator = new BloomSimulator(bloom_print.getWriteEnd(), "client.txt");
         ReadEnd readEnd = bloom_print.getReadEnd();
         bloomSimulator.start();
         System.out.println(readEnd.getNextString());        
     }
 
     public static void testBloomSimulatorFilter() throws Exception {
         Connector bloom_print = new Connector("bloom_print");
         BloomSimulator bloomSimulator = new BloomSimulator(bloom_print.getWriteEnd(), "client.txt");
         Connector readBloomFilterRelationConnector = new Connector("print_bloomFilter");
         ReadRelation r_bloom = new ReadRelation("client.txt","clientDB", readBloomFilterRelationConnector.getWriteEnd());
         Connector filter_print = new Connector("filter_print");
         PrintTuple p = new PrintTuple(filter_print.getReadEnd());
         BFilter filter = new BFilter(bloom_print.getReadEnd(), 
                 readBloomFilterRelationConnector.getReadEnd(), filter_print.getWriteEnd(), 0);
         r_bloom.start();
         p.start();
         filter.start();
         bloomSimulator.start();
     }
     
     public static void FileSort(String fileName) {
         try {
             BufferedReader reader = new BufferedReader(new FileReader(fileName));
             ArrayList<String> list = new ArrayList<String>();
             String line = "";
             while((line = reader.readLine()) != null) {
                 list.add(line);
             }
             reader.close();
             FileWriter writer = new FileWriter(fileName);
             Collections.sort(list);
             for(String val : list){
                 writer.write(val);	
                 writer.write('\n');
             }
             writer.close();
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 
 }
