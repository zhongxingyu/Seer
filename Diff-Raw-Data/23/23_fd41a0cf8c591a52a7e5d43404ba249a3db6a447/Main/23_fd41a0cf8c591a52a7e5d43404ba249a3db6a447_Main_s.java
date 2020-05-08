 
 
 package dsmw;
 
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.TimeZone;
 
 /**
  *
  * @author klm
  */
 public class Main {
 
         public static void main( String[] args ) throws IOException, Exception
     {
         if (args.length<2)
         {
                 System.err.println("Usage: java -jar dsmw.jar <TDB folder> <ontology file> ");
                 System.exit(0);
         }
 
         String DBdirectory = args[0] ;
         String ontoFile = "file:"+args[1];
         Git G = new Git();
         Jena J= new Jena(DBdirectory,ontoFile);
 
         System.out.print("Loading ChangeSets ... ");
         G.gitLog(J);
         System.out.println("DONE");
 
         System.out.print("Adding PushFeeds ... ");
         J.addPushFeeds();
         System.out.println("DONE");
 
         System.out.print("Adding Sites ... ");
         J.addsites();
         System.out.println("DONE");
 

         J.listSites();
         System.out.println("===========");
 //        J.listStatements();
 //        System.out.println("===========");
 
         ChangeSet FCS=J.getFirstCS();
         System.out.print("First CS: ");
         FCS.print();
 
         ArrayList <ChangeSet> AL1=new ArrayList <ChangeSet>();
         AL1=J.getNextCS(FCS.getChgSetID());
         System.out.println("Second CSs?: ");
         for (ChangeSet o:AL1)
         {
             o.print();
         }
 
        String date=FCS.getDate();
         Date D;
         SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
         sdf1.setTimeZone(TimeZone.getTimeZone("GMT"));
         D = sdf1.parse(date);
         Calendar cal = Calendar.getInstance();
         cal.setTimeZone(TimeZone.getTimeZone("GMT"));
         cal.setTime(D);
         int step=86400; //in seconds
 
         ArrayList <ChangeSet> AL2=new ArrayList <ChangeSet>();
         boolean more=true;
         FileOutputStream fos = new FileOutputStream("out.txt");
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
         
         J.close();
         out.close();
     }
 
 }
