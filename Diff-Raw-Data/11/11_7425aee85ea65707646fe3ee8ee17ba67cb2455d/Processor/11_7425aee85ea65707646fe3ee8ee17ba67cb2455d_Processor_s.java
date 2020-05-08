 package mrz.lab1.processor;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.IOUtils;
 
 import mrz.lab1.binary.BinaryNumber;
 
 /**
  * @author Q-YAA
  */
 public class Processor {
 
     private static final String DIGIT_CAPACITY_PATTERN = "digitCapacity\\s*=\\s*(\\d+)";
 
     private static final String PROCESSING_ITEM_PATTERN = "\\[(\\d+)\\s*\\*\\s*(\\d+)\\]";
 
     private List<ProcessorElement> processorElements;
 
     private List<ProcessingItem> processingItems;
 
     private int digitCapacity;
 
     public Processor(String dataFile) {
         loadData(dataFile);
         initProcessorElements(digitCapacity);
     }
 
     public List<ProcessingItem> processData() {
         int tactNumber = 0;
 
         int processedElementNumber = 0;
 
         while (processedElementNumber < processingItems.size()) {
             tactNumber++;
             log("\n------------------------- (%s) ---------------------------\n", tactNumber);
 
             for (int i = processedElementNumber; i < processingItems.size(); i++) {
                 if (i >= tactNumber) {
                     break;
                 }
 
                 ProcessingItem processingItem = processingItems.get(i);
                 ProcessorElement processorElement = processorElements.get(tactNumber - i - 1);
 
                 processorElement.process(processingItem);
 
                 log(processingItem, processorElement, tactNumber);
             }
 
             if (tactNumber % processorElements.size() - processedElementNumber == 0) {
                 processedElementNumber++;
             }
         }
 
         return processingItems;
     }
 
     private void initProcessorElements(int digitCapacity) {
         processorElements = new ArrayList<ProcessorElement>();
 
         for (int i = 0; i < digitCapacity; i++) {
            processorElements.add(new Summator(i + 1));
            processorElements.add(new ShiftElement(i + 1));
         }
     }
 
     private void loadData(String dataFile) {
         InputStream inputStream = null;
         try {
             inputStream = new FileInputStream(dataFile);
             String text = new String(IOUtils.toByteArray(inputStream));
 
             digitCapacity = loadDigitCapacity(text);
             processingItems = loadProcessingItems(text);
 
             int i = 0;
         }
         catch (FileNotFoundException e) {
             throw new IllegalStateException("Error when load data file.", e);
         }
         catch (IOException e) {
             throw new IllegalStateException("Error when load data file.", e);
         }
         finally {
             IOUtils.closeQuietly(inputStream);
         }
     }
 
     private int loadDigitCapacity(String text) {
         Pattern pattern = Pattern.compile(DIGIT_CAPACITY_PATTERN);
         Matcher matcher = pattern.matcher(text);
         matcher.find();
 
         return Integer.parseInt(matcher.group(1));
     }
 
     private List<ProcessingItem> loadProcessingItems(String text) {
         Pattern pattern = Pattern.compile(PROCESSING_ITEM_PATTERN);
         Matcher matcher = pattern.matcher(text);
 
         List<ProcessingItem> processingItems = new ArrayList<ProcessingItem>();
         while (matcher.find()) {
             BinaryNumber multiplicand = BinaryNumber.fromDecimalString(matcher.group(1), digitCapacity);
             BinaryNumber factor = BinaryNumber.fromDecimalString(matcher.group(2), digitCapacity);
 
             processingItems.add(new ProcessingItem(multiplicand, factor));
         }
 
         return processingItems;
     }
 
     private void log(ProcessingItem processingItem, ProcessorElement processorElement, int tactNumber) {
         StringBuilder stringBuilder = new StringBuilder();
         stringBuilder.append("tact number '").append(tactNumber).append("'").append("\n");
         stringBuilder.append(processorElement.getName()).append(" (").append(processorElement.getNumber()).append(")\n");
 
         stringBuilder.append(processingItem.getFactor()).append("\n");
         stringBuilder.append(processingItem.getMultiplicand()).append("\n");
         stringBuilder.append(processingItem.getPartialSum()).append("\n");
 
         System.out.println(stringBuilder.toString());
     }
 
     private void log(String message, Object... objects) {
         System.out.println(String.format(message, objects));
     }
 
 }
