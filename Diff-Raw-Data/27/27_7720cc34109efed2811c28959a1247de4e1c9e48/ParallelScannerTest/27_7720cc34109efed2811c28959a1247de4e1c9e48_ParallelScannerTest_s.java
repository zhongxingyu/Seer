 package org.sonatype.sisu.scanner.scanners;
 
 import org.sonatype.sisu.resource.scanner.Scanner;
 import org.sonatype.sisu.resource.scanner.scanners.ParallelScanner;
 
 public class ParallelScannerTest
     extends AbstractScannerTest
 {
 
     protected Scanner createScanner()
     {
         return new ParallelScanner( 2 );
     }
    
 }
