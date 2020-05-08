 package example;
 
 import kom.handlersocket.*;
 import kom.handlersocket.query.*;
 import kom.handlersocket.result.HSResult;
 import kom.handlersocket.result.HSResultFuture;
 
 import java.util.Arrays;
 import java.util.List;
 
 public class HSExample2 {
 	public static void main(String[] args) throws Exception {
 		HSConnectionFactory connectionFactory = new HSConnectionFactory("192.168.56.101");
 
 		HSConnection connection = connectionFactory.connect(HSConnectionMode.READ_WRITE);
 		HSIndexDescriptor indexDescriptor = new HSIndexDescriptor("test", "test", "PRIMARY", Arrays.asList("v1", "v2"), Arrays.asList("v1", "v2"));
 		HSResultFuture resultFuture;
 		HSResult result;
 
 		resultFuture = connection.execute(
 				indexDescriptor,
 				Arrays.asList(
 						(HSQuery) new HSOpenIndexQuery(),
 						(HSQuery) new HSFindQuery(CompareOperator.GE, Arrays.asList(""))
 								.in(0, Arrays.asList("1", "2")).limit(10),
 						(HSQuery) new HSFindQuery(CompareOperator.GE, Arrays.asList(""))
								.filter(HS.FilterType.FILTER, CompareOperator.EQ, "v1", "1 v1 value").limit(10),
 //						(HSQuery) new HSInsertQuery(Arrays.asList("3 v1 value", "3 v2 text")),
 //						(HSQuery) new HSInsertQuery().values(Arrays.asList("4 v1 value", "4 v2 text"))
 						(HSQuery) new HSDeleteQuery(CompareOperator.EQ, Arrays.asList("3"), true)
 				));
 
 		result = resultFuture.get();
 		result.debug();
 
 		connectionFactory.release();
 	}
 }
