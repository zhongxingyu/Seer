 package edu.ch.unifr.diuf.testing_tool;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 
 public class TestParams 
 {
     public int numClients;
     public String testId;
     
     private String testServerSourceGraphName;
     private String testServerDestGraphName;
     private int testServerGraphReset;
     private int testServerGraphSnaphshot;
     private String testServerReadCons; 
     private String testServerWriteCons; 
     private String testServerTransLockingGran; 
     private int testServerReplicationFactor;
     
     private int testNum;
     private List testThreadNumber;
     private int testWarmupPer;
     private int testRunningPer;
     private int testOperationType;
     private int testOperationNum;
     private int testTransRetrials;
 
     private String testConflictsFlag;
     // number of different entities, properties per entity and values per property
     private int testDiffEnt;
     private int testDiffPropPerEnt; 
     private int testDiffValuesPerProf;
     
     public TestParams() { 
         this.testThreadNumber = new ArrayList<>();
     }
     
 
     public void setNumClients(int numClients) { 
         this.numClients = numClients;
     }
     
     public int getNumClients() { 
         return this.numClients;
     }
     
     public String getTestServerSourceGraphName() { 
         return this.testServerSourceGraphName;
     }
     
     public void setTestServerSourceGraphName(String graphName) { 
         this.testServerSourceGraphName = graphName;
     }
     
     public String getTestServerDestGraphName() { 
         return this.testServerDestGraphName;
     }
     
     public void setTestServerDestGraphName(String graphName) { 
         this.testServerDestGraphName = graphName;
     }
     
     public int getTestServerGraphReset() { 
         return this.testServerGraphReset;
     }
     
     public void setTestServerGraphReset(int reset) { 
         this.testServerGraphReset = reset;
     }
     
     public int getGraphSnapshot() { 
         return this.testServerGraphSnaphshot;
     }
     
     public void setGraphSnapshot(int snapshot) { 
         this.testServerGraphSnaphshot = snapshot;
     } 
     
     public String getTestReadCons() { 
         return this.testServerReadCons;
     }
     
     public void setTestReadCons(String read_cons) { 
         this.testServerReadCons = read_cons;
     }
     
     public String getTestWriteCons() { 
         return this.testServerWriteCons;
     }
     
     public void setTestWriteCons(String write_cons) { 
         this.testServerWriteCons = write_cons;
     }
     
     public String getTransLockGran() { 
         return this.testServerTransLockingGran;
     }
     
     public void setTransLockGran(String lock_gran) { 
         this.testServerTransLockingGran = lock_gran;
     }
     
     public int getReplicationFactor() {
         return this.testServerReplicationFactor;
     }
     
     public void setReplicationFactor(int factor) { 
         this.testServerReplicationFactor = factor;
     }
     
     public int getTransRetrials() { 
         return this.testTransRetrials;
     }
     
     public void setTransRetrials(int retrials) { 
         this.testTransRetrials = retrials;
     }
     
     public int getTestNum() { 
         return this.testNum;
     }
     
     public void setTestNum(int num) {
         this.testNum = num;
     }
     
     public List getTestThreadNum() {
         return this.testThreadNumber;
     }
     
     public void addTestThreadNum(int thread_num) { 
         this.testThreadNumber.add(thread_num);
     }
     
     public int getTestWarmupPer() { 
         return this.testWarmupPer;
     }
     
     public void setTestWarmupPer(int warmup_per) { 
         this.testWarmupPer = warmup_per;
     }
     
     public int getTestRunningPer() { 
         return this.testRunningPer;
     }
     
     public void setTestRunningPer(int running_per) { 
         this.testRunningPer = running_per;
     }
     
     public int getTestOperationType() { 
         return this.testOperationType;
     }
     
     public void setTestOperationType(int oper_type) { 
         this.testOperationType = oper_type;
     }
     
     public int getTestOperationNum() { 
         return this.testOperationNum;
     }
     
     public void setTestOperationNum(int oper_num) { 
         this.testOperationNum = oper_num;
     }
     
     public int getTestTransRetrials() { 
         return this.testTransRetrials;
     }
     
     public void setTestTransRetrials(int retrials) { 
         this.testTransRetrials = retrials;
     }
 
     public int getDiffEnt() { 
         return this.testDiffEnt;
     }
     
     public void setDiffEnt(int diffEnt) { 
         this.testDiffEnt = diffEnt;
     }
     
     public int getDiffPropPerEnt() { 
         return this.testDiffPropPerEnt;
     }
    
     public void setDiffPropPerEnt(int propPerEnt) { 
         this.testDiffPropPerEnt = propPerEnt;
     }
     
     public int getDiffValuesPerProp() { 
         return this.testDiffValuesPerProf;
     }
     
     public void setDiffValuesPerProp(int valuesPerProp) { 
         this.testDiffValuesPerProf = valuesPerProp;
     }
     
     private double getProbabilityOfConflicts(int no_threads) { 
         return ((no_threads+0.0)*numClients)/
                 (testDiffEnt*testDiffPropPerEnt*testDiffValuesPerProf)*100;
     }
     
     public String getConflictsParameter() { 
         return this.testConflictsFlag;
     }
     
     public void setConflictsParameter(String param) { 
         this.testConflictsFlag = param;
     }
     
     // meaning with directory if any 
     public String getFullTestName() { 
         return this.testId;
     }
     
     public String getTestName() { 
         if( this.testId.contains("/") ) 
             return testId.substring(testId.indexOf("/")+1);
         return testId;
     }
     
     public void setTestName(String name) { 
         this.testId = name;
     }
     
     public String getFinalRestultFilename() { 
        return "final_result-"+this.getTestName()+".data";
     }
     
     public String toString() { 
         StringBuilder sb = new StringBuilder();
         sb.append("\tSERVER PARAMS: ").append(testServerSourceGraphName)
                 .append("/").append(testServerDestGraphName).append(" ");
         sb.append(testServerGraphReset).append(" ");
         sb.append(testServerGraphSnaphshot).append(" ").append(testServerReadCons);
         sb.append(" ").append(testServerWriteCons).append(" ").append(testServerTransLockingGran);
         sb.append(" ").append(testServerReplicationFactor).append("\n");
         
         sb.append("\tTEST PARAMS: \n");
         //sb.append("\t\tinput filename: ").append(testInputFilename).append("\n");
         sb.append("\t\trun steps: ").append(testNum).append("\n");
         sb.append("\t\tthread num per client: ");
         for(Iterator it=testThreadNumber.iterator(); it.hasNext(); ) {
             sb.append((it.next())).append(" ");
         }
         sb.append("\n");
         sb.append("\t\twarmup period sec: ").append(testWarmupPer).append("\n");
         sb.append("\t\trunning period sec: ").append(testRunningPer).append("\n");
         sb.append("\t\toperation type: ").append(testOperationType).append("\n");
         sb.append("\t\tnum oper per trans: ").append(testOperationNum).append("\n");
         sb.append("\t\ttrans num of retrials: ").append(testRunningPer).append("\n");
         sb.append("\t\tconflicts flag: ").append(testConflictsFlag).append("\n");
         if( testConflictsFlag.equals("yes") ) {
             sb.append("\t\tnum of different entities: ").append(testDiffEnt).append("\n");
             sb.append("\t\tnum of different prop per ent: ").append(testDiffPropPerEnt).append("\n");
             sb.append("\t\tnum of different values per prop: ").append(testDiffValuesPerProf).append("\n");
             sb.append("\t\tPROBABILITY of conflicts (%): ");
             for(Iterator it=testThreadNumber.iterator(); it.hasNext(); ) {
                 int no_threads = (int)(it.next());
                 sb.append(no_threads).append("th->").append(getProbabilityOfConflicts(no_threads))
                         .append("  ");   
             }
             sb.append("\n");
         }
         return sb.toString();
     }
     
 }
