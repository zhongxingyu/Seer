 package com.carbonfive.db.jdbc;
 
 import com.mockrunner.mock.jdbc.*;
 import static org.hamcrest.MatcherAssert.*;
 import static org.hamcrest.core.Is.*;
 import static org.hamcrest.text.IsEqualIgnoringWhiteSpace.*;
 import org.junit.*;
 
 import java.io.*;
 
 public class ScriptRunnerImplTest
 {
     @Test
    public void scriptRunnerShouldBatchSimpleCommands() throws Exception
     {
         ScriptRunnerImpl runner = new ScriptRunnerImpl();
         Reader reader = new InputStreamReader(getClass().getResourceAsStream("simple.sql"));
         MockConnection connection = new MockConnection();
 
         runner.execute(connection, reader);
 
         assertThat(connection.getStatementResultSetHandler().getExecutedStatements().size(), is(2));
         assertThat(connection.getStatementResultSetHandler().getExecutedStatements().get(0).toString(),
                    is(equalToIgnoringWhiteSpace("create table users ( username varchar not null, password varchar not null )")));
         assertThat(connection.getStatementResultSetHandler().getExecutedStatements().get(1).toString(),
                    is(equalToIgnoringWhiteSpace("alter table users add index (username), add unique (username)")));
     }
 
     @Test
    public void scriptRunnerShouldBatchMySQLFunctionsAndProcedures() throws Exception
     {
         ScriptRunnerImpl runner = new ScriptRunnerImpl();
         Reader reader = new InputStreamReader(getClass().getResourceAsStream("stored-procedure-mysql.sql"));
         MockConnection connection = new MockConnection();
 
         runner.execute(connection, reader);
 
         assertThat(connection.getStatementResultSetHandler().getExecutedStatements().size(), is(3));
         assertThat(connection.getStatementResultSetHandler().getExecutedStatements().get(0).toString(),
                    is(equalToIgnoringWhiteSpace("CREATE FUNCTION hello (s CHAR(20)) RETURNS CHAR(50) DETERMINISTIC RETURN CONCAT('Hello, ',s,'!')")));
         assertThat(connection.getStatementResultSetHandler().getExecutedStatements().get(1).toString(),
                    is(equalToIgnoringWhiteSpace("CREATE FUNCTION weighted_average (n1 INT, n2 INT, n3 INT, n4 INT) RETURNS INT DETERMINISTIC BEGIN DECLARE avg INT; SET avg = (n1+n2+n3*2+n4*4)/8; RETURN avg; END")));
         assertThat(connection.getStatementResultSetHandler().getExecutedStatements().get(2).toString(),
                    is(equalToIgnoringWhiteSpace("CREATE PROCEDURE payment(payment_amount DECIMAL(6,2), payment_seller_id INT) BEGIN DECLARE n DECIMAL(6,2); SET n = payment_amount - 1.00; INSERT INTO Moneys VALUES (n, CURRENT_DATE); IF payment_amount > 1.00 THEN UPDATE Sellers SET commission = commission + 1.00 WHERE seller_id = payment_seller_id; END IF; END")));
     }

    @Test
    public void scriptRunnerShouldUseTheSameDelimiterUntilExplicitlyChanged() throws Exception
    {
        ScriptRunnerImpl runner = new ScriptRunnerImpl();
        Reader reader = new InputStreamReader(getClass().getResourceAsStream("function-mysql.sql"));
        MockConnection connection = new MockConnection();

        runner.execute(connection, reader);

        assertThat(connection.getStatementResultSetHandler().getExecutedStatements().size(), is(3));
        assertThat(connection.getStatementResultSetHandler().getExecutedStatements().get(0).toString(),
                   is(equalToIgnoringWhiteSpace("DROP FUNCTION IF EXISTS simpleFunction")));
        assertThat(connection.getStatementResultSetHandler().getExecutedStatements().get(1).toString(),
                   is(equalToIgnoringWhiteSpace("CREATE FUNCTION simpleFunction() RETURNS varchar(100) READS SQL DATA begin declare message varchar(100) default 'Hello Word'; return message; end")));
        assertThat(connection.getStatementResultSetHandler().getExecutedStatements().get(2).toString(),
                   is(equalToIgnoringWhiteSpace("select simpleFunction()")));
    }
 }
