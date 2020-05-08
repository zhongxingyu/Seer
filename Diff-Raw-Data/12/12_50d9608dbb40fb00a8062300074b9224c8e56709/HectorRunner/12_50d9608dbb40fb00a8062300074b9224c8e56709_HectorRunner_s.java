 import me.prettyprint.hector.api.Cluster;
 import me.prettyprint.hector.api.factory.HFactory;
 
 
 public class HectorRunner {
 
 	public static void main(String[] args) {
 		try{
 		final String SERVER = args[0];
 		final String CLUSTER = args[1];
 		final String COMMAND = args[2];
 
 		Cluster myCluster = HFactory.getOrCreateCluster(CLUSTER, SERVER);
 		Keyspace keyspace = HFactory.createKeyspace("Tutorial", myCluster);
                 ColumnQuery<String, String, String> columnQuery = HFactory.createStringColumnQuery(keyspace);
 		StringSerializer stringSerializer = StringSerializer.get();
 	        BytesArraySerializer byteSerializer = BytesArraySerializer.get();
         	LongSerializer longSerializer = LongSerializer.get();
 		Mutator<String> mutator = HFactory.createMutator(keyspace, stringSerializer);
         
 		if(COMMAND.equals("status"))
 		{			
 			System.out.println(myCluster.describeRing(CLUSTER));
 		} else if (COMMAND.equals("read_col")) {
 		columnQuery.setColumnFamily("Npanxx");
 		columnQuery.setKey("512204");
 	        columnQuery.setName("city");
         	QueryResult<HColumn<String, String>> result = columnQuery.execute();
 	        HColumn<String, String> row =result.get();
 	        System.out.print(row.getName()+" "+row.getValue());
 		} else if (COMMAND.equals("write_col")) {
 		 mutator.addInsertion("CA Burlingame", "StateCity", HFactory.createColumn(650L, "37.57x122.34",longSerializer,stringSerializer));
 	        mutator.addInsertion("650", "AreaCode", HFactory.createStringColumn("Burlingame__650", "37.57x122.34"));
 	        mutator.addInsertion("650222", "Npanxx", HFactory.createStringColumn("lat", "37.57"));
 	        mutator.addInsertion("650222", "Npanxx", HFactory.createStringColumn("lng", "122.34"));
 	        mutator.addInsertion("650222", "Npanxx", HFactory.createStringColumn("city", "Burlingame"));
 	        mutator.addInsertion("650222", "Npanxx", HFactory.createStringColumn("state", "CA")); 
 	        mutator.addInsertion("650222", "Npanxx", HFactory.createStringColumn("state2", "")); 			       
         	MutationResult mr = mutator.execute();
 	        System.out.print(mr);	
 		}
 
 		}catch(Exception e){e.printStackTrace();}
 
 	}
 
 }
