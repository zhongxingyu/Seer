 package com.bigvisible.kanbansimulatortester.gui;
 
 import static org.fest.swing.data.TableCell.row;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.table.TableColumn;
 
 import org.fest.swing.edt.GuiActionRunner;
 import org.fest.swing.edt.GuiQuery;
 import org.fest.swing.fixture.FrameFixture;
 import org.fest.swing.fixture.JButtonFixture;
 import org.fest.swing.fixture.JLabelFixture;
 import org.fest.swing.fixture.JTableFixture;
 import org.fest.swing.fixture.JTableHeaderFixture;
 import org.fest.swing.fixture.JTextComponentFixture;
 
 import com.bigvisible.kanbansimulator.GUI;
 import com.bigvisible.kanbansimulator.IterationParameter;
 import com.bigvisible.kanbansimulator.IterationResult;
 import com.bigvisible.kanbansimulator.Simulator;
 
 public class GUIDriver implements Simulator {
     private FrameFixture mainWindowFixture;
     private FrameFixture outputWindowFixture;
     private List<IterationResult> iterationResults = new LinkedList<IterationResult>();
 
     public void start() {
        GUI mainWindow = GuiActionRunner.execute(new GuiQuery<GUI>() {
             protected GUI executeInEDT() {
                 return new GUI();
             }
         });
         mainWindowFixture = new FrameFixture(mainWindow);
         mainWindowFixture.show();
     }
 
     public void finish() {
         mainWindowFixture.cleanUp();
         mainWindowFixture = null;
     }
 
     public void run(OutputStream rawOutputStream) {
         JButtonFixture runButtonFixture = mainWindowFixture.button("runButton");
 
         runButtonFixture.click();
         waitForSimulatorToFinish();
 
         JTextComponentFixture outputTextBoxFixture = outputWindowFixture.textBox("outputTextArea");
 
         String[] lines = outputTextBoxFixture.text().split("\n");
 
         iterationResults = new LinkedList<IterationResult>();
         for (int idx = 0; idx < lines.length; idx++) {
             String line = lines[idx];
             iterationResults.add(IterationResult.parseCSV(line));
         }
 
         if (rawOutputStream != null) {
             try {
                 rawOutputStream.write(outputTextBoxFixture.text().getBytes());
                 rawOutputStream.flush();
             } catch (IOException e) {
                 throw new RuntimeException(String.format(
                         "Failed to write simulator output to the supplied stream (which is of type %s).",
                         rawOutputStream.getClass()), e);
             }
         }
     }
 
     private void waitForSimulatorToFinish() {
         long startTime = System.currentTimeMillis();
         String simulatorStatus = "";
         while (simulatorStatus != "Done") {
             try {
                 Thread.sleep(1);
             } catch (InterruptedException e) {
                 // Don't stop/interrupt the test just because we got an interrupt signal.
                 e.printStackTrace();
             }
             long currentTime = System.currentTimeMillis();
             long beenWaiting = currentTime - startTime;
             if (beenWaiting > 5000) {
                 throw new RuntimeException(String.format("Timed-out while waiting for simulator to finish.  Waiting for status = \"%s\".  Current status is \"%s\".",
                         "Done", simulatorStatus));
             }
             simulatorStatus = simulatorStatus();
         }
     }
 
     private String simulatorStatus() {
         JLabelFixture statusLabel = mainWindowFixture.label("statusLabel");
 
         return statusLabel.text();
     }
 
     public void setBatchSize(int batchSize) {
         JTextComponentFixture batchSizeFixture = mainWindowFixture.textBox("batchSize");
 
         batchSizeFixture.setText("11");
     }
 
     public void setBusinessAnalystCapacity(int businessAnalystCapacity) {
         // TODO Auto-generated method stub
 
     }
 
     public void setDevelopmentCapacity(int developmentCapacity) {
         // TODO Auto-generated method stub
 
     }
 
     public void setWebDevelopmentCapacity(int webDevelopmentCapacity) {
         // TODO Auto-generated method stub
 
     }
 
     public void setQualityAssuranceCapacity(int qualityAssuranceCapacity) {
         // TODO Auto-generated method stub
 
     }
 
     public void setNumberOfIterationsToRun(int numberOfIterationsToRun) {
         JTextComponentFixture iterationsToRunFixture = mainWindowFixture.textBox("iterationsToRun");
         
         iterationsToRunFixture.setText(""+numberOfIterationsToRun);
     }
 
     private Integer currentIteration;
     private int rowIndex;
     private Map<String, Integer> workflowStepNameToColumnIndexCache;
     private List<String> tableColumnNames = new ArrayList<String>();
 
     public void addParameter(IterationParameter iterationParameter) {
         JTableFixture tableFixture = mainWindowFixture.table();
         Map<String, Integer> workflowStepNameToColumnIndex = getWorkflowStepNameToColumnIndexMappings(tableFixture);
 
         if (currentIteration == null) {
             rowIndex = 0;
             currentIteration = iterationParameter.getIteration();
         } else {
             if (!currentIteration.equals(iterationParameter.getIteration())) {
                 rowIndex++;
                 currentIteration = iterationParameter.getIteration();
             }
         }
         Integer columnIndex = null;
         Integer cellValue = null;
         if(iterationParameter.hasWorkflowConfiguration()) {
             columnIndex = workflowStepNameToColumnIndex.get(iterationParameter.getWorkflowStepName());
             if (columnIndex == null) {
                 throw new RuntimeException(
                         "Could not find column in JTable that matches workflow step name of iteration parameter."
                                 + String.format("where workflow step name = \"%s\" and the JTable column names are: %s",
                                         iterationParameter.getWorkflowStepName(), tableColumnNames));
             }
             cellValue = iterationParameter.getCapacity();
         } else {
             columnIndex = workflowStepNameToColumnIndex.get("Batch Size");
             cellValue = iterationParameter.getBatchSize();
         }
 
         tableFixture.enterValue(row(rowIndex).column(columnIndex), "" + cellValue);
     }
 
     private Map<String, Integer> getWorkflowStepNameToColumnIndexMappings(JTableFixture tableFixture) {
         if (workflowStepNameToColumnIndexCache == null) {
             workflowStepNameToColumnIndexCache = new HashMap<String, Integer>();
             JTableHeaderFixture tableHeadFixture = tableFixture.tableHeader();
 
             Enumeration<TableColumn> tableColumns = tableHeadFixture.component().getColumnModel().getColumns();
             while (tableColumns.hasMoreElements()) {
                 TableColumn tableColumn = tableColumns.nextElement();
                 String workflowStepName = (String) tableColumn.getHeaderValue();
                 int columnIndex = tableColumn.getModelIndex();
 
                 workflowStepNameToColumnIndexCache.put(workflowStepName, columnIndex);
                 tableColumnNames.add(workflowStepName);
             }
         }
         return workflowStepNameToColumnIndexCache;
     }
 
     public List<IterationResult> results() {
         return iterationResults;
     }
 
     public void addStories(int storiesToAdd) {
         JTextComponentFixture storiesInBacklogTextBox = mainWindowFixture.textBox("storiesInBacklog");
 
         storiesInBacklogTextBox.setText("" + storiesToAdd);
     }
 
     public int getStoriesCompleted() {
         // TODO Auto-generated method stub
         return 0;
     }
 
     public int getIterationsRun() {
         // TODO Auto-generated method stub
         return 0;
     }
 
 }
