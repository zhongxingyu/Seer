 package com.rockwellautomation.verification.execution;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Formatter;
 import java.util.List;
 
 import com.google.protobuf.CodedInputStream;
 import com.google.protobuf.ExtensionRegistry;
 import com.rockwellautomation.verification.Performance.Element;
 import com.rockwellautomation.verification.Performance.Operations;
 import com.rockwellautomation.verification.converter.ElementConverter;
 import com.rockwellautomation.verification.converter.OperationConverter;
 import com.rockwellautomation.verification.element.VisitableElement;
 import com.rockwellautomation.verification.operation.OperationVisitor;
 import com.rockwellautomation.verification.operation.SizeOperation;
 
 /**
  * Abstract base class for TestRunners. This base class performs common operations, such as reading in the protocol
  * buffer files and hydrating the data.
  * 
  * @author JCase
  */
 public abstract class BaseTestRunner implements Runnable {
   // input data
   protected String dataFilePath;
   protected String operationFilePath;
 
   // output data
   protected long dataHydrationTime;
   protected long operationHydrationTime;
   protected long dataConversionTime;
   protected long operationConversionTime;
   protected long testExecutionTime;
   protected long numberOfOperations;
   protected long numberOfElements;
 
   // running state
   protected Element testData;
   protected Operations operationsData;
   protected List<VisitableElement> convertedTestData;
   protected List<OperationVisitor<?>> convertedOperations;
 
   public BaseTestRunner(String dataFilePath, String operationFilePath) {
     this.dataFilePath = dataFilePath;
     this.operationFilePath = operationFilePath;
   }
 
   public long getDataHydrationTime() {
     return dataHydrationTime;
   }
 
   public long getOperationHydrationTime() {
     return operationHydrationTime;
   }
 
   public long getDataConversionTime() {
     return dataConversionTime;
   }
 
   public long getOperationConversionTime() {
     return operationConversionTime;
   }
 
   public long getTestExecutionTime() {
     return testExecutionTime;
   }
 
   public long getNumberOfOperations() {
     return numberOfOperations;
   }
 
   public long getNumberOfElements() {
     return numberOfElements;
   }
 
   /**
    * Main entry point for all TestRunner implementations. This method handles reading in the test data, taking counts
    * and timing measurements, and calling the {@link #runTheTest()} template method on the implementation.
    */
   @Override
   public void run() {
     try {
       this.testData = this.hydrateTestData();
       this.operationsData = this.hydrateOperationsData();
     }
     catch (IOException e) {
       throw new RuntimeException("Unable to hydrate test or operations file", e);
     }
 
     // Convert the protocol buffer test data to VisitableElements and Operations
     convertData();
     convertOperations();
 
     // Capture some output data in terms of data set sizes
     this.numberOfOperations = this.operationsData.getOperationsCount();
     this.numberOfElements = this.countElements();
 
     // Execute the test
     long testExecutionStartTime = System.currentTimeMillis();
     runTheTest();
     this.testExecutionTime = System.currentTimeMillis() - testExecutionStartTime;
   }
 
   /**
    * Execute the test using the converted test data and operations
    */
   protected abstract void runTheTest();
 
   /**
    * Convert the Element protocol buffer data to a List of VisitableElements. Also sets timing data
    */
   private void convertData() {
     long startTime = System.currentTimeMillis();
     this.convertedTestData = ElementConverter.convertMessageToElement(this.testData);
     this.dataConversionTime = System.currentTimeMillis() - startTime;
   }
 
   /**
    * Convert the Operations protocol buffer data to a List of OperationVisitors. Also sets timing data
    */
   private void convertOperations() {
     long startTime = System.currentTimeMillis();
     this.convertedOperations = OperationConverter.convertOperationMessageToList(this.operationsData);
     this.operationConversionTime = System.currentTimeMillis() - startTime;
   }
 
   /**
    * Determine the size of the data set.
    * <p/>
    * Uses the SizeOperation for convenience. This is still considered setup code and is executed outside of the actual
    * test execution timing
    * 
    * @return
    */
   private long countElements() {
     SizeOperation size = new SizeOperation();
     for (VisitableElement e : this.convertedTestData) {
       e.accept(size);
     }
     return size.getResult();
   }
 
   /**
    * Read in and hydrate the test data file
    * 
    * @return
    * @throws IOException
    */
   private Element hydrateTestData() throws IOException {
     long startTime = System.currentTimeMillis();
     File dataFile = new File(this.dataFilePath);
     if (!dataFile.exists() || !dataFile.canRead()) {
       throw new RuntimeException("Unable to read data file: " + this.dataFilePath);
     }
 
     ExtensionRegistry extensionRegistry = ExtensionRegistry.newInstance();
     extensionRegistry.add(Element.booleanValue);
     extensionRegistry.add(Element.stringValue);
     extensionRegistry.add(Element.numericValue);
     CodedInputStream cis = CodedInputStream.newInstance(new FileInputStream(dataFile));
     cis.setRecursionLimit(Integer.MAX_VALUE);
     Element ret = Element.parseFrom(cis, extensionRegistry);
 
     this.dataHydrationTime = System.currentTimeMillis() - startTime;
 
     return ret;
   }
 
   /**
    * Read in and hydrate the operations data file
    * 
    * @return
    * @throws IOException
    */
   private Operations hydrateOperationsData() throws IOException {
     long startTime = System.currentTimeMillis();
     File operationsFile = new File(this.operationFilePath);
     if (!operationsFile.exists() || !operationsFile.canRead()) {
      throw new RuntimeException("Unable to read operations file: " + this.operationFilePath);
     }
 
     Operations ret = Operations.parseFrom(new FileInputStream(operationsFile));
 
     this.operationHydrationTime = System.currentTimeMillis() - startTime;
 
     return ret;
   }
 
   @Override
   public String toString() {
     StringBuilder sb = new StringBuilder();
     Formatter formatter = new Formatter(sb);
     String newLine = System.getProperty("line.separator");
     sb.append("# elements: " + this.getNumberOfElements());
     sb.append(newLine);
     sb.append("# operations: " + this.getNumberOfOperations());
     sb.append(newLine);
     sb.append("data hydration time: " + this.getDataHydrationTime() + "ms");
     sb.append(newLine);
     sb.append("operation hydration time: " + this.getOperationHydrationTime() + "ms");
     sb.append(newLine);
     sb.append("data conversion time: " + this.getDataConversionTime() + "ms");
     sb.append(newLine);
     sb.append("operation conversion time: " + this.getOperationConversionTime() + "ms");
     sb.append(newLine);
     sb.append("test execution time: " + this.getTestExecutionTime() + "ms");
     sb.append(newLine);
     formatter.format("Operations per second: %,.3f", computeOpsPerSecond());
     formatter.flush();
     return sb.toString();
   }
 
   /**
    * Compute the total operations per second during the TestRunner's execution
    * 
    * @param runner
    * @return (numOperations / milliseconds) * 1,000
    */
   private double computeOpsPerSecond() {
     return ((double) this.getNumberOfOperations() / (double) this.getTestExecutionTime()) * 1000;
   }
 }
