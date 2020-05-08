 package scho;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 public class Main {
 
     public static final boolean GIT_LOG=true;
     public static final boolean Mercurial_LOG=false;
     public static String cmd,dateFormatLog,dateFormatJena;
 
         public static void main( String[] args ) throws IOException, Exception
     {
         if (args.length<4)
         {
                 System.err.println("Usage: java -jar scho.jar <TDB folder> <step in seconds> <log_type> <output_file>");
                 System.exit(0);
         }
 
 
         long startTime = System.currentTimeMillis();
         String DBdirectory = args[0] ;
         Git G = new Git();
         Jena J= new Jena(DBdirectory);
 
         System.out.print("Loading ChangeSets and adding PullFeeds ... ");
         String logtype= args[2];
 
         dateFormatLog="yyyy-MM-dd HH:mm:ss Z";
         dateFormatJena="yyyy-MM-dd'T'HH:mm:ss'Z'";
         if (logtype.equalsIgnoreCase("git"))
         {
             G.gitLog(J,GIT_LOG);
             
         }
         else
         {
             G.gitLog(J,Mercurial_LOG);
         }
         
         System.out.println("DONE");
 
         System.out.print("Adding PushFeeds ... ");
         J.addPushFeeds();
         System.out.println("DONE");
 
         System.out.print("Adding Sites ... ");
         J.addsites();
         System.out.println("DONE");
         long endTime = System.currentTimeMillis();
         J.listSites();
         System.out.println("===========");
 //        J.listStatements();
 //        System.out.println("===========");
 
         ChangeSet FCS=J.getFirstCS();
 //        System.out.print("First CS: ");
 //        FCS.print();
 //
 //        ArrayList <ChangeSet> AL1=new ArrayList <ChangeSet>();
 //        AL1=J.getNextCS(FCS.getChgSetID());
 //        System.out.println("Second CSs?: ");
 //        for (ChangeSet o:AL1)
 //        {
 //            o.print();
 //        }
 
         String date= FCS.getDate(); ////for cakePHP "2009-01-01T00:00:00Z";//
         Date D;
         SimpleDateFormat sdf1 = new SimpleDateFormat(dateFormatJena);
         sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
         D = sdf1.parse(date);
         Calendar cal = Calendar.getInstance();
         cal.setTimeZone(TimeZone.getTimeZone("GMT"));
         cal.setTime(D);
         int step=Integer.valueOf(args[1]);//86400; //in seconds
 
         ArrayList <ChangeSet> AL2=new ArrayList <ChangeSet>();
         boolean more=true;
         FileOutputStream fos = new FileOutputStream(args[3]);
         PrintWriter out = new PrintWriter(fos);
         
         while (more)
         {
             AL2=J.getCStillDate(cal.getTime());
             System.out.println("Divergence awareness at " + cal.getTime().toString());
             int RM=0;
             int LM=0;
 
 
             // calculate divergence in time t
             for (ChangeSet o : AL2)
             {
                 //o.print();
                 if (!o.isPublished())
                 {
                     if (J.inPushFeed(o,cal.getTime()))
                     {
                         o.publish();
                         J.publishChangeSet(o);
                         System.out.println("published : "+o.getChgSetID());
                     }
                     else
                     {
                         if (J.inPullFeed(o,cal.getTime()))
                         {
                             if (J.isPullHead(o,cal.getTime())) //pull head
                             {
                                 //publish parents
                                 System.out.println("remotely modified: "+o.getChgSetID());
                                 RM++;
                                 Date D2=sdf1.parse(J.getNextCS(o.getChgSetID()).get(0).getDate());
                                 if (D2.before(cal.getTime()))
                                 {
                                     J.publishParents(o,cal.getTime());
                                     J.publishChangeSet(o);
                                 }
                             }
                             else
                             {
                                 System.out.println("remotely modified: "+o.getChgSetID());
                                 RM++;
                             }
                         }
                         else
                         {
                             System.out.println("locally modified: "+ o.getChgSetID());
                             LM++;
                         }
                     }
 
                     if (J.getNextCS(o.getChgSetID()).isEmpty())
                     {
                         more = false;
                     }
                 }
                 else System.out.println("published : "+o.getChgSetID());
             }
 
 // TO jump to next CS date
 //            if (!J.getNextCS(AL2.get(AL2.size()-1).getChgSetID()).isEmpty())
 //            {
 //                date=J.getNextCS(AL2.get(AL2.size()-1).getChgSetID()).get(0).getDate();
 //                sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
 //                sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
 //                D = sdf1.parse(date);
 //                cal.setTime(D);
 //            }
 //            else
                 
                 if (RM>0) System.out.println("Remotely Modified = "+RM);
                 if (LM>0) System.out.println("Locally Modified = "+LM);
                 if (LM==0 && RM==0) System.out.println("Up-to-date");
 
                 out.print(cal.getTime().getTime()+"\t"+LM+"\t"+RM+"\n");
 
                 cal.add(Calendar.SECOND, step);
 
         }
         J.dump();
         J.close();
         out.close();
        
         System.out.println("Total ontology population time :"+ (endTime-startTime));
     }
 
 }
