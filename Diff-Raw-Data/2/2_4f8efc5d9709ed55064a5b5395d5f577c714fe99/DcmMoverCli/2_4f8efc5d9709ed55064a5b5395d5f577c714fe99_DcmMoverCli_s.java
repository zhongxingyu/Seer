 package org.dcm4che2.tool.dcmmover;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.List;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.GnuParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.dcm4che2.data.Tag;
 import org.dcm4che2.net.CommandUtils;
 
 /**
  * Command line interface for the DICOM study mover.
  * 
  * @author gpotter (gcac96@gmail.com)
  * @version $Revision$
  */
 public class DcmMoverCli {
 
     private static int LOCAL_AE_ARG_INDEX = 0;
     private static int QR_SCP_ARG_INDEX = 1;
     private static int REMOTE_AE_ARG_INDEX = 2;
     private static int STUDY_UID_ARG_INDEX = 3;
 
     private static final String USAGE =
         "org.dcm4che2.tool.dcmmover.DcmMoverCli <ae> <query/retreive scp ae[@host[:port]]>\n"
             + "<store scp ae[@host[:port]]> <study uid of study to move> [Options]";
 
     private static final String DESCRIPTION = "Move a study from the specified Query/Retreive SCP to the specified\n"
         + "Storage SCP. For both the Q/R SCP and Store SCP, if <host> is not\n"
         + "specified localhost is assumed, and if <port> is not specified 104 is\n" + "is assumed.\nOptions:";
 
     private static final String EXAMPLE = "\nExample:\n"
         + "org.dcm4che2.tool.dcmmover.DcmMoverCli IMPX_QR_SCP@localhost:104\n"
         + "DCM4CHEE_STORE_SCP@localhost:306 100.118.116.2005.2.1.1132055943.796.3\n"
         + "-stgcmt -x PatientName=JONES^JOHN PatientId=9001\n"
         + "=> Move the study with uid '100.118.116.2005.2.1.1132055943.796.3'\n"
         + "from application entity IMPX_QR_SCP listening on local port 104 to the\n"
         + "application entity listening on local port 306. During the move, the\n"
         + "study PatientName and PatientId attribute values are changed to JONES^JOHN\n"
         + "and 9001, respectively, and new Study, Series, and Object uid's are\n" + "generated.\n";
 
     private static int toPort(String port) {
         return port != null ? parseInt(port, "illegal port number", 1, 0xffff) : 104;
     }
 
     private static int parseInt(String s, String errPrompt, int min, int max) {
         try {
             int i = Integer.parseInt(s);
             if (i >= min && i <= max) {
                 return i;
             }
         } catch (NumberFormatException e) {
             // ignore
         }
         exit(errPrompt);
         throw new RuntimeException();
     }
 
     private static String[] split(String s, char delim) {
         String[] s2 = { s, null };
         int pos = s.indexOf(delim);
         if (pos != -1) {
             s2[0] = s.substring(0, pos);
             s2[1] = s.substring(pos + 1);
         }
         return s2;
     }
 
     private static void exit(String msg) {
         System.err.println(msg);
         System.err.println("Try 'dcmmovercli -h' for more information.");
         System.exit(1);
     }
 
     //
     // NOTE: The arguments must be passed in the exact same order that they are defined in this method!
     //
     private static CommandLine parse(String[] args) {
         Options opts = new Options();
 
         // Options for the database interface only
 
         // Control the move process asyncronously with the DcmMoverDbi (database interface) class
         opts.addOption("dbi", false, "Control the move process asyncronously with the database interface.");
 
         OptionBuilder.withArgName("dbadapter");
         OptionBuilder.hasArg();
         OptionBuilder.withDescription("(dbi) Database adapter name.");
         opts.addOption(OptionBuilder.create("dbAdapter"));
 
         OptionBuilder.withArgName("dbhost");
         OptionBuilder.hasArg();
         OptionBuilder.withDescription("(dbi) Database host name.");
         opts.addOption(OptionBuilder.create("dbHost"));
 
         OptionBuilder.withArgName("dbport");
         OptionBuilder.hasArg();
         OptionBuilder.withDescription("(dbi) Database port number.");
         opts.addOption(OptionBuilder.create("dbPort"));
 
         OptionBuilder.withArgName("db");
         OptionBuilder.hasArg();
         OptionBuilder.withDescription("(dbi) Database name.");
         opts.addOption(OptionBuilder.create("db"));
 
         OptionBuilder.withArgName("dbuser");
         OptionBuilder.hasArg();
         OptionBuilder.withDescription("(dbi) Database user name.");
         opts.addOption(OptionBuilder.create("dbUser"));
 
         OptionBuilder.withArgName("dbpwd");
         OptionBuilder.hasArg();
         OptionBuilder.withDescription("(dbi) Database password.");
         opts.addOption(OptionBuilder.create("dbPwd"));
 
         // Dicom Move specific options
 
         OptionBuilder.withArgName("[ip][:port]");
         OptionBuilder.hasOptionalArg();
         OptionBuilder.withDescription("Request storage commitment of (successfully) moved objects and optionally "
             + "specify the address and port of a separate association to receive commitment on.");
         opts.addOption(OptionBuilder.create("stgcmt"));
 
         OptionBuilder.withArgName("ms");
         OptionBuilder.hasArg();
         OptionBuilder.withDescription("(QR SCU, STORE SCU) Timeout in ms for TCP connect, no timeout by default.");
         opts.addOption(OptionBuilder.create("connectTO"));
 
         OptionBuilder.withArgName("ms");
         OptionBuilder.hasArg();
         OptionBuilder.withDescription("(All) Timeout in ms for receiving DIMSE-RSP, 10s by default.");
         opts.addOption(OptionBuilder.create("rspTO"));
 
         opts.addOption("tcpdelay", false, "(All) Set TCP_NODELAY socket option to false, true by default.");
 
         OptionBuilder.withArgName("ms");
         OptionBuilder.hasArg();
         OptionBuilder.withDescription("(QR SCU, STORE SCU) Timeout in ms for receiving A-ASSOCIATE-AC, 5s by default.");
         opts.addOption(OptionBuilder.create("acceptTO"));
 
         OptionBuilder.withArgName("ms");
         OptionBuilder.hasArg();
         OptionBuilder.withDescription("(QR SCU, STORE SCU) Timeout in ms for receiving A-RELEASE-RP, 5s by default.");
         opts.addOption(OptionBuilder.create("releaseTO"));
 
         OptionBuilder.withArgName("[seq/]attr=value");
         OptionBuilder.hasArgs(); // Take any number of attr/value pairs
         OptionBuilder.withValueSeparator('=');
         OptionBuilder.withDescription("Specify patient and study attributes whose values should be added or"
             + "replaced during the study move. Can be specified by name or tag value "
             + "(in hex), e.g. PatientName or 00100010.");
         opts.addOption(OptionBuilder.create("x"));
 
         OptionBuilder.withArgName("[seq/]attr");
         OptionBuilder.hasArgs();
         OptionBuilder.withDescription("Specify DICOM object attributes that should be removed during the"
             + "study move. Can be specified by name or tag value (in hex).");
         opts.addOption(OptionBuilder.create("y"));
 
         opts.addOption("lowprior", false, "LOW priority of the C-FIND/C-MOVE operation, MEDIUM by default");
         opts.addOption("highprior", false, "HIGH priority of the C-FIND/C-MOVE operation, MEDIUM by default");
 
         opts.addOption("h", "help", false, "print this message");
 
         opts.addOption("V", "version", false, "print the version information and exit");
 
         CommandLine cl = null;
         try {
             cl = new GnuParser().parse(opts, args);
         } catch (ParseException e) {
             exit("dcmmover: " + e.getMessage());
             throw new RuntimeException("unreachable");
         }
         if (cl.hasOption('V')) {
             Package p = DcmMover.class.getPackage();
             System.out.println("dcmmover v" + p.getImplementationVersion());
             System.exit(0);
         }
         if (cl.hasOption('h') || cl.getArgList().size() != 4) {
             HelpFormatter formatter = new HelpFormatter();
             formatter.printHelp(USAGE, DESCRIPTION, opts, EXAMPLE);
             System.exit(0);
         }
 
         return cl;
     }
 
     public static void main(String[] args) {
 
         CommandLine cl = parse(args);
 
         DcmMover dcmMover = createDcmMover(cl);
 
         final List argList = cl.getArgList();
         String[] aetHostPort;
 
         // Set the local ae and the optional bind address and listen port to receive the C-MOVE's
         String AE = "";
         String localAE = (String) argList.get(LOCAL_AE_ARG_INDEX);
         aetHostPort = split(localAE, '@');
         if (aetHostPort[1] == null) {
             String[] aetPort = split(aetHostPort[0], ':');
             AE = aetPort[0];
             dcmMover.setAET(aetPort[0]);
             if (aetPort[1] != null) {
                 dcmMover.setReceiveSCPListenPort(toPort(aetPort[1]));
             }
         } else {
             dcmMover.setAET(aetHostPort[0]);
             String[] hostPort = split(aetHostPort[1], ':');
             dcmMover.setLocalHost(hostPort[0]);
             dcmMover.setReceiveSCPListenPort(toPort(hostPort[1]));
         }
 
         // Get the query/retrieve scp ae (called) and host and port
         String sourceAE = "";
         String remoteQRAE = (String) argList.get(QR_SCP_ARG_INDEX);
         aetHostPort = split(remoteQRAE, '@');
         if (aetHostPort[1] == null) {
             String[] aetPort = split(aetHostPort[0], ':');
             sourceAE = aetPort[0];
             dcmMover.setQRSCUCalledAET(aetPort[0]);
             if (aetPort[1] != null) {
                 dcmMover.setQRSCURemotePort(toPort(aetPort[1]));
             }
         } else {
             sourceAE = aetHostPort[0];
             dcmMover.setQRSCUCalledAET(aetHostPort[0]);
             String[] hostPort = split(aetHostPort[1], ':');
             if (hostPort[1] == null) {
                 dcmMover.setQRSCURemotePort(toPort(hostPort[0]));
             } else {
                 dcmMover.setQRSCURemoteHost(hostPort[0]);
                 dcmMover.setQRSCURemotePort(toPort(hostPort[1]));
             }
         }
 
         // Get the remote (called) ae and optional bind address and optional port
         String destAE = "";
         String remoteAE = (String) argList.get(REMOTE_AE_ARG_INDEX);
         aetHostPort = split(remoteAE, '@');
         if (aetHostPort[1] == null) {
             String[] aetPort = split(aetHostPort[0], ':');
             destAE = aetPort[0];
             dcmMover.setSendSCUCalledAET(aetPort[0]);
             if (aetPort[1] != null) {
                 dcmMover.setSendSCURemotePort(toPort(aetPort[1]));
             }
         } else {
             destAE = aetHostPort[0];
             dcmMover.setSendSCUCalledAET(aetHostPort[0]);
             String[] hostPort = split(aetHostPort[1], ':');
             if (hostPort[1] == null) {
                 dcmMover.setSendSCURemotePort(toPort(hostPort[0]));
             } else {
                 dcmMover.setSendSCURemoteHost(hostPort[0]);
                 dcmMover.setSendSCURemotePort(toPort(hostPort[1]));
             }
         }
 
         // Get the study instance uid of the study to move and do the move
         String studyUid = (String) argList.get(STUDY_UID_ARG_INDEX);
 
         // Set other options supplied on the command line
 
         if (cl.hasOption("stgcmt")) {
             dcmMover.setStorageCommitment(true);
             String stgcmtArg = cl.getOptionValue("stgcmt");
             if (null != stgcmtArg) {
                 String[] hostPort = split(stgcmtArg, ':');
                 if ((hostPort[0] != null) && (hostPort[0].length() != 0)) {
                     dcmMover.setStorageCommitmentHost(hostPort[0]);
                 }
                 if ((hostPort[1] != null) && (hostPort[1].length() != 0)) {
                     dcmMover.setStorageCommitmentPort(toPort(hostPort[1]));
                 }
             }
         }
 
         if (cl.hasOption("connectTO")) {
             dcmMover.setConnectTimeout(parseInt(cl.getOptionValue("connectTO"),
                 "illegal argument of option -connectTO", 1, Integer.MAX_VALUE));
         }
 
         if (cl.hasOption("rspTO")) {
             dcmMover.setDimseRspTimeout(parseInt(cl.getOptionValue("rspTO"), "illegal argument of option -rspTO", 1,
                 Integer.MAX_VALUE));
         }
 
         dcmMover.setTcpNoDelay(!cl.hasOption("tcpdelay"));
 
         if (cl.hasOption("acceptTO")) {
             dcmMover.setAcceptTimeout(parseInt(cl.getOptionValue("acceptTO"), "illegal argument of option -acceptTO",
                 1, Integer.MAX_VALUE));
         }
 
         if (cl.hasOption("releaseTO")) {
             dcmMover.setReleaseTimeout(parseInt(cl.getOptionValue("releaseTO"),
                 "illegal argument of option -releaseTO", 1, Integer.MAX_VALUE));
         }
 
         if (cl.hasOption("lowprior")) {
             dcmMover.setMovePriority(CommandUtils.LOW);
         }
         if (cl.hasOption("highprior")) {
             dcmMover.setMovePriority(CommandUtils.HIGH);
         }
 
         // Get patient and study data to use for anonymizing the moved study objects
         ObjectTransformData xformObjData = null;
         if (cl.hasOption("x")) {
             xformObjData = new ObjectTransformData();
             String[] xformKeys = cl.getOptionValues("x");
             for (int i = 1; i < xformKeys.length; i++, i++) {
                 if (!addPatientStudyDataToXform(xformObjData, Tag.toTag(xformKeys[i - 1]), xformKeys[i])) {
                     return;
                 }
             }
         }
 
         // Get object data to remove from the moved study objects
         if (cl.hasOption("y")) {
             if (null == xformObjData) {
                 xformObjData = new ObjectTransformData();
             }
             String[] xformKeys = cl.getOptionValues("y");
             for (int i = 0; i < xformKeys.length; i++) {
                 xformObjData.addAttrToRemove(Tag.toTag(xformKeys[i]));
             }
         }
 
         //
         // End of command line processing
 
         // Do the move - syncronously or async
         System.out.print("\n\nAE " + AE + " starting move " + (xformObjData == null ? "" : "with transformation")
             + " of study [" + studyUid + "] from AE " + sourceAE + " to AE " + destAE);
         doMove(dcmMover, studyUid, xformObjData);
     }
 
     private static DcmMover createDcmMover(CommandLine cl) {
         return new DcmMover(true);
     }
 
     private static void doMove(DcmMover mover, String studyUid, ObjectTransformData psd) {
 
         // Do the move and get the move response - this method blocks until process is complete
         MoveResponse response = mover.moveStudy(studyUid, psd);
 
         if (response.moveSuccessful() == true) {
             System.err.print("\nMove of study [" + studyUid + "] succeeded.");
         } else {
             System.err.print("\nMove of study [" + studyUid + "] FAILED.");
         }
         System.out.print(response.toString());
 
     }
 
     public static boolean addPatientStudyDataToXform(ObjectTransformData psd, int tag, String value) {
         switch (tag) {
         //
         // Patient attributes
         //
             case Tag.PatientName:
                 psd.setPatientName(value);
                 break;
             case Tag.PatientID:
                 psd.setPatientId(value);
                 break;
             case Tag.PatientBirthDate: {
                DateFormat fmt = new SimpleDateFormat("yyyyMMDD");
                 try {
                     psd.setPatientBirthDate(fmt.parse(value));
                 } catch (java.text.ParseException e) {
                     System.err.println("Bad patient birth date format");
                     e.printStackTrace();
                     return false;
                 }
                 break;
             }
             case Tag.PatientBirthTime: {
                 DateFormat fmt = new SimpleDateFormat("HHmmss");
                 try {
                     psd.setPatientBirthTime(fmt.parse(value));
                 } catch (java.text.ParseException e) {
                     System.err.println("Bad patient birth time format");
                     e.printStackTrace();
                     return false;
                 }
                 break;
             }
             case Tag.PatientSex:
                 psd.setPatientSex(value);
                 break;
             case Tag.EthnicGroup:
                 psd.setEthnicGroup(value);
                 break;
             //
             // Study attributes
             //
             case Tag.AccessionNumber:
                 psd.setAccessionNumber(value);
                 break;
             case Tag.StudyID:
                 psd.setStudyId(value);
                 break;
             case Tag.PerformingPhysicianName:
                 psd.setPerformingPhysicianName(value);
                 break;
             case Tag.ReferringPhysicianName:
                 psd.setReferringPhysicianName(value);
                 break;
             case Tag.StudyDate:
                 psd.setStudyDate(value);
                 break;
             case Tag.StudyTime:
                 psd.setStudyTime(value);
                 break;
         }
         return true;
     }
 }
