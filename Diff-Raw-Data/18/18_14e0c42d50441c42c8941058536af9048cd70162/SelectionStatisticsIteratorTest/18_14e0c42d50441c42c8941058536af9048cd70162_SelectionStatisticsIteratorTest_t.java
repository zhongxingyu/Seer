 package edu.rivfader.test.profiling;
 
 import edu.rivfader.profiling.ICountingIterator;
 import edu.rivfader.profiling.ICostAccumulator;
 import edu.rivfader.profiling.SelectionStatisticsIterator;
 import edu.rivfader.relalg.Selection;
 import edu.rivfader.relalg.IQualifiedNameRow;
 import edu.rivfader.relalg.RowFactory;
 
 import java.util.List;
 import java.util.LinkedList;
 import java.util.Iterator;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.Matchers.hasItem;
 
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import static org.easymock.EasyMock.expect;
 import static org.powermock.api.easymock.PowerMock.createMock;
 import static org.powermock.api.easymock.PowerMock.replayAll;
 import static org.powermock.api.easymock.PowerMock.verifyAll;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(Selection.class)
 public class SelectionStatisticsIteratorTest {
    private interface RowCounter extends ICountingIterator<IQualifiedNameRow> {
    }

     @Test public void evaluationWorks() {
         ICostAccumulator statisticsDestination;
         ICountingIterator<IQualifiedNameRow> inputCounter;
         Iterator<IQualifiedNameRow> output;
         SelectionStatisticsIterator subject;
         Selection activeNode;
 
         RowFactory selectedRows = new RowFactory(new String[] {"t", "c"});
         selectedRows.addRow("a");
         selectedRows.addRow("b");
 
        inputCounter = createMock(RowCounter.class);
         activeNode = createMock(Selection.class);
         statisticsDestination = createMock(ICostAccumulator.class);
         statisticsDestination.handleSelectionStatistics(activeNode,
                                                         42,
                                                         selectedRows.getRows().size(),
                                                         1);
         expect(inputCounter.getNumberOfElements()).andReturn(42);
         output = selectedRows.getRows().iterator();
         replayAll();
         subject = new SelectionStatisticsIterator(activeNode, output,
                 statisticsDestination, inputCounter);
         List<IQualifiedNameRow> result = new LinkedList<IQualifiedNameRow>();
         while(subject.hasNext()) {
             result.add(subject.next());
         }
         for(IQualifiedNameRow outputRow : selectedRows.getRows()) {
             assertThat(result, hasItem(outputRow));
         }
         verifyAll();
     }
 }
