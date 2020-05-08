 package com.boxysystems.libraryfinder.model;
 
import java.io.File;
import java.io.IOException;
 import java.util.*;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Siddique Hameed
  * Date: Sep 28, 2007
  * Time: 10:44:24 PM
  */
 public class LibraryVisitorWithObserverTest extends AbstractLibraryFinderTest {
 
     public void testFindLibraryForDtdFile_WithObserver() throws LibraryFinderException {
         MockObserver observer = new MockObserver();
         LibraryFinderQuery query = new LibraryFinderQuery(testLog4jJar.getParentFile().getPath(), "org/apache/log4j/xml/log4j.dtd");
         LibraryVisitor visitor = new LibraryVisitor(query, observer);
         visitor.visitLibraries();
         Set<LibraryFinderResult> results = visitor.getResults();
         assertEquals(1, results.size());
         LibraryFinderResult actualResult = results.iterator().next();
         assertEquals(log4jLibraryFinderResult, actualResult);
         assertTrue(observer.getObservedArgs().size() > 0);
         Object observedArg = observer.getObservedArgs().get(0);
         assertTrue(((String) observedArg).contains(Constants.SEARCH_MSG_PREFIX));
     }
 
     public void testFindLibraryForClassFiles_Regex_WithObserver() throws LibraryFinderException {
         MockObserver observer = new MockObserver();
         LibraryFinderQuery query = new LibraryFinderQuery(testLog4jJar.getParentFile().getPath(), "org/apache/log4j/Logger.class|org/apache/log4j/lf5/lf5/properties");
         query.setRegex(true);
         LibraryVisitor visitor = new LibraryVisitor(query, observer);
         visitor.visitLibraries();
         Set<LibraryFinderResult> results = visitor.getResults();
         assertEquals(1, results.size());
         LibraryFinderResult actualResult = results.iterator().next();
         assertEquals(log4jLibraryFinderResult, actualResult);
         assertTrue(observer.getObservedArgs().size() > 0);
         Object observedArg = observer.getObservedArgs().get(0);
         assertTrue(((String) observedArg).contains(Constants.SEARCH_MSG_PREFIX));
     }
 
    public void testFindLibraryWithoutExcludedFolders() throws LibraryFinderException, IOException {
         MockObserver observer = new MockObserver();
        LibraryFinderQuery query = new LibraryFinderQuery(new File("./").getCanonicalPath(), "org/apache/log4j/Logger.class");
         LibraryVisitor visitor = new LibraryVisitor(query, observer);
         visitor.visitLibraries();
         Set<LibraryFinderResult> results = visitor.getResults();
        assertTrue(results.size() > 0);
         assertTrue(observer.getObservedArgs().size() > 0);
        boolean excludedFolderFound = false;
        for (int i = 0; i < observer.getObservedArgs().size(); i++) {
            Object observedArg = observer.getObservedArgs().get(i);
            if (((String) observedArg).contains(".git")) {
                excludedFolderFound = true;
                break;
            }
        }
        assertTrue(excludedFolderFound);
     }
 
     public void testFindLibraryWithExcludedFolders() throws LibraryFinderException {
         MockObserver observer = new MockObserver();
         LibraryFinderQuery query = new LibraryFinderQuery(testLog4jJar.getParentFile().getPath(), "org/apache/log4j/Logger.class");
         query.setExcludedFolders(Constants.DEFAULT_EXCLUDED_FOLDERS);
         LibraryVisitor visitor = new LibraryVisitor(query, observer);
         visitor.visitLibraries();
         Set<LibraryFinderResult> results = visitor.getResults();
         assertEquals(1, results.size());
         LibraryFinderResult actualResult = results.iterator().next();
         assertEquals(log4jLibraryFinderResult, actualResult);
         assertTrue(observer.getObservedArgs().size() > 0);
         Object observedArg = observer.getObservedArgs().get(0);
         assertFalse(((String) observedArg).contains(".svn"));
     }
 
     private class MockObserver implements Observer {
 
         private List<Object> observedArgs = new ArrayList<Object>();
 
         public void update(Observable o, Object arg) {
             observedArgs.add(arg);
             System.out.println(arg);
         }
 
         public List<Object> getObservedArgs() {
             return observedArgs;
         }
     }
 }
