 package info.gehrels.diplomarbeit;
 
 import info.gehrels.diplomarbeit.dex.DexBenchmarkStep;
 import info.gehrels.diplomarbeit.flockdb.FlockDBBenchmarkStep;
 import info.gehrels.diplomarbeit.hypergraphdb.HyperGraphDBBenchmarkStep;
 import info.gehrels.diplomarbeit.neo4j.Neo4jBenchmarkStep;
 
 
 public class RunBenchmarkStep {
   public static void main(String[] args) throws Exception {
     String inputPath = args[0];
     String dbName = args[1];
     String algorithm = args[2];
 
     switch (dbName) {
       case "flockdb": {
         new FlockDBBenchmarkStep(algorithm, inputPath).execute();
         break;
       }
 
       case "neo4j": {
         new Neo4jBenchmarkStep(algorithm, inputPath).execute();
         break;
       }
 
       case "hypergraphdb": {
         new HyperGraphDBBenchmarkStep(algorithm, inputPath).execute();
         break;
       }
 
       case "dex": {
         new DexBenchmarkStep(algorithm, inputPath).execute();
       }
 
       default: {
         throw new IllegalArgumentException(dbName + " is not a known db engine");
       }
     }
   }
 }
