 package fedora.client.export;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.*;
 import javax.swing.*;
 
 import fedora.client.Administrator;
 import fedora.client.APIAStubFactory;
 import fedora.client.APIMStubFactory;
 import fedora.client.FTypeDialog;
 import fedora.client.export.AutoExporter;
 import fedora.client.search.AutoFinder;
 
 import fedora.server.access.FedoraAPIA;
 import fedora.server.management.FedoraAPIM;
 
 import fedora.server.types.gen.Condition;
 import fedora.server.types.gen.ComparisonOperator;
 import fedora.server.types.gen.FieldSearchQuery;
 import fedora.server.types.gen.FieldSearchResult;
 import fedora.server.types.gen.ListSession;
 import fedora.server.types.gen.ObjectFields;
 
 public class Export {
 
     public static int ONE=0;
     public static int MULTI=1;
 
     // launch interactively
     public Export(int kind) {
         try {
             JFileChooser browse=new JFileChooser(Administrator.getLastDir());
             browse.setDialogTitle("Export to which directory?");
             browse.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
             int returnVal = browse.showOpenDialog(Administrator.getDesktop());
             if (returnVal == JFileChooser.APPROVE_OPTION) {
                 File file = browse.getSelectedFile();
                 Administrator.setLastDir(file.getParentFile());
                 if (kind==ONE) {
                     String pid=JOptionPane.showInputDialog("Enter the PID of the object to export.");
                     if (pid!=null && !pid.equals("")) {
                         one(Administrator.APIM, pid, file);
                         JOptionPane.showMessageDialog(Administrator.getDesktop(),
                            "Export succeeded.  PID='" + pid + "'.");
                     }
                 } else {
                     FTypeDialog dlg=new FTypeDialog();
                     if (dlg.getResult()!=null) {
                         String fTypes=dlg.getResult();
                         long st=System.currentTimeMillis();
                         String[] pids=multi(Administrator.APIA, 
                                             Administrator.APIM, 
                                             fTypes, 
                                             file);
                         long et=System.currentTimeMillis();
                         JOptionPane.showMessageDialog(Administrator.getDesktop(),
                             "Export of " + pids.length + " objects finished.\n"
                             + "Time elapsed: " + getDuration(et-st));  
                     }
                 }
             }
         } catch (Exception e) {
             String msg=e.getMessage();
             if (msg==null) {
                 msg=e.getClass().getName();
             }
             JOptionPane.showMessageDialog(Administrator.getDesktop(),
                     msg,
                     "Export Failure",
                     JOptionPane.ERROR_MESSAGE);
         }
     }
     
     private static String getDuration(long millis) {
         long tsec=millis/1000;
         long h=tsec/60/60;
         long m=(tsec - (h*60*60))/60;
         long s=(tsec - (h*60*60) - (m*60));
         StringBuffer out=new StringBuffer();
         if (h>0) {
             out.append(h + " hour");
             if (h>1) out.append('s');
         }
         if (m>0) {
             if (h>0) out.append(", ");
             out.append(m + " minute");
             if (m>1) out.append('s');
         }
         if (s>0 || (h==0 && m==0)) {
             if (h>0 || m>0) out.append(", ");
             out.append(s + " second");
             if (s!=1) out.append('s');
         }
         return out.toString();
     }
 
     public static void one(FedoraAPIM apim, String pid, File dir)
             throws Exception {
         String fName=pid.replaceAll(":", "_") + ".xml";
         File file=new File(dir, fName);
         System.out.println("Exporting " + pid + " to " + file.getPath());
         AutoExporter.export(apim, pid, new FileOutputStream(file), false);
     }
     
     public static String[] multi(FedoraAPIA apia, 
                                  FedoraAPIM apim,
                                  String fTypes,
                                  File dir)
             throws Exception {
         String tps=fTypes.toUpperCase();
         Set toExport=new HashSet();
         Set pidSet=new HashSet();
         if (tps.indexOf("D")!=-1) {
             toExport=getPIDs(apia, "D");
             System.out.println("Found " + toExport.size() + " behavior definitions.");
             pidSet.addAll(toExport);
         }
         if (tps.indexOf("M")!=-1) {
             toExport=getPIDs(apia, "M");
             System.out.println("Found " + toExport.size() + " behavior mechanisms.");
             pidSet.addAll(toExport);
         }
         if (tps.indexOf("O")!=-1) {
             toExport=getPIDs(apia, "O");
             System.out.println("Found " + toExport.size() + " regular objects.");
             pidSet.addAll(toExport);
         }
         Iterator iter=pidSet.iterator();
         String[] pids=new String[pidSet.size()];
         int i=0;
         while (iter.hasNext()) {
             String pid=(String) iter.next();
             one(apim, pid, dir);
             pids[i++]=pid;
         }
         return pids;
     }
 
     public static Set getPIDs(FedoraAPIA apia,
                               String fType)
             throws Exception {
         // get pids with fType='$fType', adding all to set at once,
         // then returning the entire set.
         HashSet set=new HashSet();
         Condition cond=new Condition();
         cond.setProperty("fType");
         cond.setOperator(ComparisonOperator.fromValue("eq"));
         cond.setValue(fType);
         Condition[] conds=new Condition[1];
         conds[0]=cond;
         FieldSearchQuery query=new FieldSearchQuery();
         query.setConditions(conds);
         query.setTerms(null);
         String[] fields=new String[1];
         fields[0]="pid";
         FieldSearchResult res=AutoFinder.findObjects(apia,
                                                      fields,
                                                      1000,
                                                      query);
         boolean exhausted=false;
         while (res!=null && !exhausted) {
             ObjectFields[] ofs=res.getResultList();
             for (int i=0; i<ofs.length; i++) {
                 set.add(ofs[i].getPid());
             }
             if (res.getListSession()!=null && res.getListSession().getToken()!=null) {
                 res=AutoFinder.resumeFindObjects(apia,
                                                  res.getListSession().getToken());
             } else {
                 exhausted=true;
             }
         }
         return set;
     }
 
     /**
      * Print error message and show usage for command-line interface.
      */
     public static void badArgs(String msg) {
         System.err.println("Error  : " + msg);
         System.err.println();
         System.err.println("Command: fedora-export");
         System.err.println();
         System.err.println("Summary: Exports one or more objects from a Fedora repository.");
         System.err.println();
         System.err.println("Syntax:");
         System.err.println("  fedora-export HST:PRT USR PSS PID|FTYPS PATH");
         System.err.println();
         System.err.println("Where:");
         System.err.println("  HST    is the repository's hostname.");
         System.err.println("  PRT    is the repository's port number.");
         System.err.println("  USR    is the id of the repository user.");
         System.err.println("  PSS    is the password of repository user.");
         System.err.println("  PID    is the id of the object to export from the source repository.");
         System.err.println("  FTYPS  is any combination of the characters O, D, and M, specifying");
         System.err.println("         which Fedora object type(s) should be exported. O=regular objects,");
         System.err.println("         D=behavior definitions, and M=behavior mechanisms.");
         System.err.println("  PATH   is the directory to export to.");
         System.err.println();
         System.err.println("Examples:");
         System.err.println("fedora-export example.com:80 fedoraAdmin fedoraAdmin changeme:1 .");
         System.err.println();
         System.err.println("  Exports changeme:1 from example.com:80 to the current directory.");
         System.err.println();
         System.err.println("fedora-export example.com:80 fedoraAdmin fedoraAdmin DMO /tmp/fedoradump");
         System.err.println();
         System.err.println("  Exports all objects from example.cocm:80 to /tmp/fedoradump");
         System.err.println();
         System.exit(1);
     }
 
     /**
      * Command-line interface for doing exports.
      */
     public static void main(String[] args) {
         try {
             if (args.length!=5) {
                 Export.badArgs("Wrong number of arguments.");
             }
             String[] hp=args[0].split(":");
             if (hp.length!=2) {
                 Export.badArgs("First arg must be of the form 'host:portnum'");
             }
             if (args[3].indexOf(":")==-1) {
                 // assume args[3] is FTYPS... so multi-export
                 String[] pids=Export.multi(
                         APIAStubFactory.getStub(hp[0],
                                                 Integer.parseInt(hp[1]),
                                                 args[1],
                                                 args[2]),
                         APIMStubFactory.getStub(hp[0],
                                                 Integer.parseInt(hp[1]),
                                                 args[1],
                                                 args[2]),
                         args[3],
                         new File(args[4]));
                 System.out.print("Exported ");
                 for (int i=0; i<pids.length; i++) {
                     if (i>0) System.out.print(", ");
                     System.out.print(pids[i]);
                 }
                 System.out.println();
             } else {
                 // assume args[3] is a PID...they only want to export one object
                 Export.one(APIMStubFactory.getStub(hp[0],
                                                    Integer.parseInt(hp[1]),
                                                    args[1],
                                                    args[2]),
                            args[3],
                            new File(args[4]));
                 System.out.println("Exported " + args[3]);
             }
         } catch (Exception e) {
             System.err.print("Error  : ");
             if (e.getMessage()==null) {
                 e.printStackTrace();
             } else {
                 System.err.print(e.getMessage());
             }
         }
     }
 
 }
