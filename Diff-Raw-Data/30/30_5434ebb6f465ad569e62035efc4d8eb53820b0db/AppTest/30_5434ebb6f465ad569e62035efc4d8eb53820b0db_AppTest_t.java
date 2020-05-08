 package aj.apps.microsync.tests;
 
 import java.io.InputStream;
 
 import one.utils.jre.OneUtilsJre;

import org.junit.Test;

 import textsync.internal.engine.ParseSectionsProcess;
 
 /**
  * Unit test for simple App.
  */
 public class AppTest
         {
 
 
     public void testNothing() {
         
     }
     
   @Test
     public void testExampleInput2() throws Exception {
     	 InputStream is = this.getClass().getResourceAsStream("example_input2.txt");
     	 
     	 if (is == null) {
     		 System.err.println("Test resource not available.");
     		 return;
     	 }
     	 
     	 byte[] testData = OneUtilsJre.toByteArray(is);
       
     	 String testString = new String(testData);
     	 
     	 System.out.println(testString);
     	 
     	 ParseSectionsProcess process = new ParseSectionsProcess(testString);
     	 
     	System.out.println( process.parse());
     	 
     	
     	
     	
     }
     
 //    public void testExample1() throws Exception {
 //        OneJre.init();
 //        
 //        InputStream is = this.getClass().getResourceAsStream("example_input1.txt");
 //        
 //        byte[] testData = OneUtilsJre.toByteArray(is);
 //        
 //        String testString = new String(testData);
 //        
 //        ParseTextProcess.processText(testString, ".html", new DummyDataService(), new DummyLogService(), false, new ParseTextProcess.WhenSyncComplete() {
 //
 //            public void onSuccess(String text) {
 //                System.out.println("Successfully processed");
 //            }
 //
 //            public void onFailure(Throwable t) {
 //                throw new RuntimeException(t);
 //            }
 //        });
 //    }
 //    
 //    public void testUploadOperation() {
 //        OneJre.init();
 //        ParseTextProcess.processText("ignore <!-- one.upload https://u1.linnk.it/qc8sbw/usr/apps/textsync/docs/mytest --> content <!-- one.end --> ignore too", "txt", new DummyDataService(), new DummyLogService(), false,new ParseTextProcess.WhenSyncComplete() {
 //
 //            public void onSuccess(String text) {
 //                //System.out.println(text);
 //            }
 //
 //            public void onFailure(Throwable t) {
 //                throw new RuntimeException(t);
 //            }
 //        });
 //        
 //    }
 //    
 //    public void testSyncOperation() {
 //        OneJre.init();
 //        ParseTextProcess.processText("ignore <!-- one.upload http://test.com/mynode --> some rather lengthy\n text. <!-- -->ignore too", "txt", new DummyDataService(), new DummyLogService(), false,new ParseTextProcess.WhenSyncComplete() {
 //
 //            public void onSuccess(String text) {
 //               
 //            }
 //
 //            public void onFailure(Throwable t) {
 //               throw new RuntimeException(t);
 //            }
 //        });
 //        
 //    }
 //    
 //    public void testSyncUploadAndSyncOperation() {
 //        OneJre.init();
 //        ParseTextProcess.processText("ignore <!-- one.upload http://test.com/mynode --> some rather lengthy\n text. <!-- -->ignore<!-- one.uploadNew newNode --> to create <!-- --> too", "txt", new DummyDataService(), new DummyLogService(),false, new ParseTextProcess.WhenSyncComplete() {
 //
 //            public void onSuccess(String text) {
 //                //System.out.println(text);
 //            }
 //
 //            public void onFailure(Throwable t) {
 //                throw new RuntimeException(t);
 //            }
 //        });
 //        
 //    }
 //    
 //    public void testDownloadOperation() {
 //        OneJre.init();
 //        
 //        String baesText = "ignore<!-- one.download http://test.com/mynode -->download<!-- one.end -->ignore";
 //        ParseTextProcess.processText(baesText, "txt", new DummyDataService(), new DummyLogService(), false, new ParseTextProcess.WhenSyncComplete() {
 //
 //            public void onSuccess(String text) {
 //                Assert.assertEquals("ignore<!-- one.download http://test.com/mynode -->download+<!-- one.end -->ignore", text);
 //            }
 //
 //            public void onFailure(Throwable t) {
 //                throw new RuntimeException(t);
 //            }
 //        });
 //        
 //    }
 //    
 //    public void testIgnoreOperation() {
 //        OneJre.init();
 //        
 //        String baesText = "ignore<!-- one.download http://test.com/mynode -->start<!-- one.ignoreNext --><!-- one.end -->end // <!-- one.end -->ignore";
 //        ParseTextProcess.processText(baesText, "txt", new DummyDataService(), new DummyLogService(), false, new ParseTextProcess.WhenSyncComplete() {
 //
 //            public void onSuccess(String text) {
 //               // System.out.println(text);
 //                Assert.assertEquals("ignore<!-- one.download http://test.com/mynode -->start<!-- one.ignoreNext --><!-- one.end -->end +// <!-- one.end -->ignore", text);
 //            }
 //
 //            public void onFailure(Throwable t) {
 //                throw new RuntimeException(t);
 //            }
 //        });
 //        
 //    }
 //
 //    private static class DummyLogService implements LogService {
 //
 //        public void note(String text) {
 //            // do nothing
 //        }
 //        
 //    }
 //    
 //    private static class DummyDataService implements DataService {
 //
 //        public DummyDataService() {
 //        }
 //
 //        public void createNewNode(String value, String title, String extension, boolean isPublic, WhenNewNodeCreated callback) {
 //            //System.out.println("Create node: "+title+" with "+value);
 //            callback.thenDo(One.reference("http://test.com"));
 //        }
 //
 //        public void downloadChanges(String value, String nodeUri, WhenChangesDownloaded callback) {
 //          // System.out.println("download: "+nodeUri+" with "+value);
 //            callback.onChanged(value+"+");
 //        }
 //
 //        
 //        
 //        public void uploadChanges(String enclosedWithinComments, String parameter, WhenChangesUploaded callback) {
 //            
 //            callback.thenDo(true);
 //        }
 //
 //        public void shutdown(WhenShutdown callback) {
 //           callback.thenDo();
 //        }
 //    }
 }
