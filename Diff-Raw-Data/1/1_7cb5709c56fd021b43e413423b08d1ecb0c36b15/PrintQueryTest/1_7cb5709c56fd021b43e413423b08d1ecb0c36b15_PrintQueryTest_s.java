 package edu.rivfader.test.commands;
 
 import edu.rivfader.commands.PrintQuery;
 import edu.rivfader.data.Database;
 import edu.rivfader.data.Row;
 import edu.rivfader.relalg.IRelAlgExpr;
 import edu.rivfader.relalg.IQualifiedNameRow;
 import edu.rivfader.relalg.QualifiedNameRow;
 import edu.rivfader.relalg.IQualifiedColumnName;
 import edu.rivfader.relalg.QualifiedColumnName;
 import edu.rivfader.relalg.StubResult;
 
 import java.io.Writer;
 import java.io.IOException;
 
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Iterator;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 import static org.easymock.EasyMock.expect;
 import org.powermock.modules.junit4.PowerMockRunner;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import static org.powermock.api.easymock.PowerMock.createMock;
 import static org.powermock.api.easymock.PowerMock.replayAll;
 import static org.powermock.api.easymock.PowerMock.verifyAll;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest({Row.class, Database.class})
 public class PrintQueryTest {
     @Test
     public void checkPrintQueryExecution() throws IOException {
         Database database = createMock(Database.class);
         IRelAlgExpr query;
         Writer writer = createMock(Writer.class);
         List<IQualifiedColumnName> columns =
             new LinkedList<IQualifiedColumnName>();
         columns.add(new QualifiedColumnName("t", "cow"));
         columns.add(new QualifiedColumnName("t", "chicken"));
         IQualifiedNameRow row1 = new QualifiedNameRow(columns);
         row1.setData(new QualifiedColumnName("t", "cow"), "milk");
         row1.setData(new QualifiedColumnName("t", "chicken"), "eggs");
         IQualifiedNameRow row2 = new QualifiedNameRow(columns);
         row2.setData(new QualifiedColumnName("t", "cow"), "cows");
         row2.setData(new QualifiedColumnName("t", "chicken"), "chickens");
         List<IQualifiedNameRow> resultRows =
             new LinkedList<IQualifiedNameRow>();
         resultRows.add(row1);
         resultRows.add(row2);
         query = new StubResult<Iterator<IQualifiedNameRow>>(
                     resultRows.iterator());
         writer.write("t.chicken t.cow\n");
         writer.write("eggs milk\n");
         writer.write("chickens cows\n");
         replayAll();
         PrintQuery subject = new PrintQuery(query);
         subject.execute(database, writer);
         verifyAll();
     }
 
     @Test
     public void checkEmptySetPrinted() throws IOException {
         Database database = createMock(Database.class);
         IRelAlgExpr query;
         List<IQualifiedColumnName> columns =
             new LinkedList<IQualifiedColumnName>();
         columns.add(new QualifiedColumnName("t", "cow"));
         columns.add(new QualifiedColumnName("t", "chicken"));
 
         Writer writer  = createMock(Writer.class);
 
         List<IQualifiedNameRow> resultRows =
             new LinkedList<IQualifiedNameRow>();
         query = new StubResult<Iterator<IQualifiedNameRow>>(resultRows.iterator());
         writer.write("Empty result set.\n");
         replayAll();
         PrintQuery subject = new PrintQuery(query);
         subject.execute(database, writer);
         verifyAll();
     }
 }
