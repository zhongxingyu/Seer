 package org.cagrid.transfer.test.system.steps;
 
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.cagrid.introduce.test.TestCaseInfo;
 import gov.nih.nci.cagrid.introduce.test.steps.BaseStep;
 import gov.nih.nci.cagrid.introduce.test.util.SourceUtils;
 
 import java.io.File;
 
 
 public class AddCreateStreamingTransferMethodImplStep extends BaseStep {
     private TestCaseInfo tci;
 
 
     public AddCreateStreamingTransferMethodImplStep(TestCaseInfo tci, boolean build) throws Exception {
         super(tci.getDir(), build);
         this.tci = tci;
 
     }
 
 
     public void runStep() throws Throwable {
         System.out.println("Adding a simple transfer method implementation.");
 
         File inFileClient = new File(".." + File.separator + ".." + File.separator + ".."
            + File.separator + "tests" + File.separator + "projects" + File.separator + "transferTests"
             + File.separator + "resources" + File.separator + "Streaming"+ tci.getName() + "Client.java");
         File outFileClient = new File(tci.getDir() + File.separator + "src" + File.separator + tci.getPackageDir()
             + File.separator + "client" + File.separator + tci.getName() + "Client.java");
 
         Utils.copyFile(inFileClient, outFileClient);
 
         File inFileImpl = new File(".." + File.separator + ".." + File.separator + ".."
            + File.separator + "tests" + File.separator + "projects" + File.separator + "transferTests"
             + File.separator + "resources" + File.separator + "Streaming"+ tci.getName() + "Impl.java");
         File outFileImpl = new File(tci.getDir() + File.separator + "src" + File.separator + tci.getPackageDir()
             + File.separator + "service" + File.separator + tci.getName() + "Impl.java");
 
         SourceUtils.modifyImpl(inFileImpl, outFileImpl, "reateStreamingTransferMethodStep");
 
         buildStep();
     }
 
 }
