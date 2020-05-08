 package no.magott.spring.batch.tasklet;
 
 import java.util.List;
 
 import javax.sql.DataSource;
 
 import org.springframework.batch.core.StepContribution;
 import org.springframework.batch.core.scope.context.ChunkContext;
 import org.springframework.batch.core.step.tasklet.Tasklet;
 import org.springframework.batch.item.ExecutionContext;
 import org.springframework.batch.item.ItemStream;
 import org.springframework.batch.item.ItemStreamException;
 import org.springframework.batch.item.util.ExecutionContextUserSupport;
 import org.springframework.batch.repeat.RepeatStatus;
 import org.springframework.jdbc.core.JdbcTemplate;
 
 public class SqlExecutingTasklet implements Tasklet, ItemStream {
 
     private final static String EXECUTION_COUNT = "sql.execution.count";
 	
 	private JdbcTemplate jdbcTemplate;
     private ExecutionContextUserSupport ecSupport;
     private List<String> sqls;
     private int count = 0;
     
     public SqlExecutingTasklet() {
 		ecSupport = new ExecutionContextUserSupport(SqlExecutingTasklet.class.getSimpleName());
 	}
     
     public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
         String sql = sqls.get(count);
         int updateCount = jdbcTemplate.update(sql);
         contribution.incrementWriteCount(updateCount);
        return count++ > sqls.size() ? RepeatStatus.FINISHED : RepeatStatus.CONTINUABLE;
     }
 
     public void setDataSource(DataSource dataSource) {
         jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     public void close() throws ItemStreamException {
     	count = 0;
     }
 
     public void open(ExecutionContext executionContext) throws ItemStreamException {
     	if(executionContext.containsKey(ecSupport.getKey(EXECUTION_COUNT))){
     		count = executionContext.getInt(ecSupport.getKey(EXECUTION_COUNT));
     	}
     }
 
     public void update(ExecutionContext executionContext) throws ItemStreamException {
         executionContext.putInt(ecSupport.getKey(EXECUTION_COUNT), count);        
     }
 
 	public void setSqls(List<String> sqls) {
 		this.sqls = sqls;
 	}
 
 }
