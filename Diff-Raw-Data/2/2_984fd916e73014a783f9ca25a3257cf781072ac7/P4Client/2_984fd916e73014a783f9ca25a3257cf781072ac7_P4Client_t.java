 package com.github.bchang.p4.base;
 
 import gw.util.Predicate;
 import gw.util.ProcessStarter;
 
 import java.util.List;
 import java.util.Map;
 
 /**
  */
 public interface P4Client {
   String getHost();
   int getPort();
   void setHost(String host);
   void setPort(int port);
   String getClient();
   void setClient(String client);
   String getUser();
   void setUser(String u);
 
   void clearStats();
   void printStats();
 
   void add(Path... paths);
   void edit(Path... paths);
   void delete(Path... paths);
   void revert(Path... paths);
 
   List<Diff2.Entry> diff2(Path left, Path right);
   List<FileLog.Entry> filelog(Path path);
   List<FileLog.Entry> filelog(Path path, int maxrevs);
   Map<String, String> fstat(Path path);
   List<String> print(Path path);
 
   P4Blame blame();
 
   List<P4UnmarshalledObject> runForObjects(List<String> op);
   List<String> runForRawOutput(List<String> op);
  String run(List<String> op);
 
   void exec(String op, ProcessStarter.OutputHandler handler);
   void run(String op, ProcessStarter.ProcessHandler handler);
 }
