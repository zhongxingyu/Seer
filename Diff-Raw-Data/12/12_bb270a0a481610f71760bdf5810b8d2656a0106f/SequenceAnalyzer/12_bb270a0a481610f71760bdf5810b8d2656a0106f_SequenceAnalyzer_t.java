 package shchitov.ivan.sequencerealnumbers;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.PrintWriter;
 import java.util.Scanner;
 import java.util.Locale;
 import java.util.InputMismatchException;
 
 /**
  * The sequence analyzer class.
  *
  * @author Ivan Shchitov
  */
class SequenceAnalyzer {
     /**
      * Constructor.
      *
      * @param args command line arguments: names files.
     * @throws FileNotFoundException if not correct file name
      */
    SequenceAnalyzer(String[] args) throws FileNotFoundException {
         if (args.length == 0) {
             throw new ArrayIndexOutOfBoundsException("No input file.");
         }
         Scanner scanner = new Scanner(new File(args[0]));
         scanner.useLocale(Locale.US);
         SequenceRealNumbers sequenceRealNumbers = new SequenceRealNumbers();
         int sizeSequence = scanner.nextInt();
         inputSequence(scanner, sequenceRealNumbers, sizeSequence);
         scanner.close();
         if (args.length == 1) {
             outputSequenceData(sequenceRealNumbers, new PrintWriter(System.out));
         } else {
             outputSequenceData(sequenceRealNumbers, new PrintWriter(new File(args[1])));
         }
     }
 
     /**
      * Inputs a sequence of numbers from the scanner.
      *
      * @param scanner             file scanner
      * @param sequenceRealNumbers sequence real numbers
      * @param sizeSequence        size of sequence
      */
     private void inputSequence(Scanner scanner, SequenceRealNumbers sequenceRealNumbers,
                                int sizeSequence) {
         int index = 0;
         while (scanner.hasNext()) {
             try {
                 sequenceRealNumbers.add(scanner.nextDouble());
                 index++;
                 if (index == sizeSequence) {
                     break;
                 }
             } catch (InputMismatchException e) {
                 System.out.println("Error. Correct the file and try again.");
                 System.exit(1);
             }
         }
         if (scanner.hasNext() || index < sizeSequence) {
             System.out.println("Error. You have entered too many numbers.\n"
                     + "Enter as many elements as the size and sequence.");
         }
     }
 
     /**
      * Outputs data about sequence in output file.
      *
      * @param sequenceRealNumbers sequence real number class
      * @param printWriter         text-output stream
      * @throws FileNotFoundException if not correct file name
      */
    private void outputSequenceData(SequenceRealNumbers sequenceRealNumbers,
                                    PrintWriter printWriter) throws FileNotFoundException {
         sequenceRealNumbers.printSequence(printWriter);
         printWriter.println("Maximum number: " + sequenceRealNumbers.getMaximum());
         printWriter.println("Minimum number: " + sequenceRealNumbers.getMinimum());
         printWriter.println("Average number: " + sequenceRealNumbers.getAverage());
         printWriter.println("Median of sequence: " + sequenceRealNumbers.getMedian());
         printWriter.println(sequenceRealNumbers.getMods());
         System.out.println("Complete.");
         printWriter.close();
     }
 }
