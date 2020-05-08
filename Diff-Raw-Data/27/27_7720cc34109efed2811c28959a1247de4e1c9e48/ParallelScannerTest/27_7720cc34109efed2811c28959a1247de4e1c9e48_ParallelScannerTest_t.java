 package org.sonatype.sisu.scanner.scanners;
 
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;
 import org.sonatype.sisu.resource.scanner.Scanner;
 import org.sonatype.sisu.resource.scanner.scanners.ParallelScanner;
 
 public class ParallelScannerTest
     extends AbstractScannerTest
 {
 
     protected Scanner createScanner()
     {
         return new ParallelScanner( 2 );
     }

    @Test
    public void parallelisationStrategyNever()
    {
        assertEquals( false, ParallelScanner.NEVER.shouldScanInParallel( new File( "" ) ) );
    }

    @Test
    public void parallelisationStrategyEveryDirectory()
    {
        assertEquals( true, ParallelScanner.EVERY_DIRECTORY.shouldScanInParallel( new File( "" ) ) );
    }

 }
