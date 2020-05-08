 package org.cagrid.transfer.extension.upgrade;
 
 import gov.nih.nci.cagrid.common.Utils;
 import gov.nih.nci.cagrid.introduce.beans.extension.ExtensionType;
 import gov.nih.nci.cagrid.introduce.beans.namespace.NamespaceType;
 import gov.nih.nci.cagrid.introduce.common.CommonTools;
 import gov.nih.nci.cagrid.introduce.common.ServiceInformation;
 import gov.nih.nci.cagrid.introduce.extension.ExtensionsLoader;
 import gov.nih.nci.cagrid.introduce.extension.utils.ExtensionUtilities;
 import gov.nih.nci.cagrid.introduce.upgrade.common.StatusBase;
 import gov.nih.nci.cagrid.introduce.upgrade.one.x.ExtensionUpgraderBase;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 
 /**
  * TransferUpgrade1pt3to1pt4
  * Updates transfer from 1.3 to 1.4
  * 
  * @author oster
  * @author dervin
  * @created Apr 9, 2007 11:21:24 AM
  * @version $Id: multiscaleEclipseCodeTemplates.xml,v 1.1 2007/03/02 14:35:01
  *          dervin Exp $
  */
 public class TransferUpgrade1pt3to1pt4 extends ExtensionUpgraderBase {
 
     private static final String CAGRID_1_3_TRANSFER_JAR_PREFIX = "caGrid-Transfer";
     private static final String CAGRID_1_3_TRANSFER_JAR_SUFFIX = "-1.3.jar";
 
     private File extensionDir = null;
     private File extensionSchemaDir = null;
     private File extensionLibDir = null;
 
     public TransferUpgrade1pt3to1pt4(ExtensionType extensionType, ServiceInformation serviceInfo, String servicePath,
         String fromVersion, String toVersion) {
         super("TransferUpgrade1pt3to1pt4", extensionType, serviceInfo, servicePath, fromVersion, toVersion);
         extensionDir = new File(ExtensionsLoader.getInstance().getExtensionsDir(), "caGrid_Transfer");
         extensionSchemaDir = new File(extensionDir, "schema");
         extensionLibDir = new File(extensionDir, "lib");
     }
 
 
     @Override
     protected void upgrade() throws Exception {
         upgradeJars();
         upgradeSchemas();
         getStatus().setStatus(StatusBase.UPGRADE_OK);
     }
     
     
     private void upgradeSchemas() throws Exception {
         final File[] newTransferSchemas = extensionSchemaDir.listFiles();
         FileFilter transferSchemaFilter = new FileFilter() {
             public boolean accept(File pathname) {
                 String name = pathname.getName();
                 for (File newSchema : newTransferSchemas) {
                     if (newSchema.getName().equals(name)) {
                         return true;
                     }
                 }
                 return false;
             }
         };
         String serviceName = getServiceInformation().getServices().getService(0).getName();
         File serviceSchemaDir = new File(getServicePath(), "schema" + File.separator + serviceName);
         File[] oldTransferSchemas = serviceSchemaDir.listFiles(transferSchemaFilter);
         for (File oldSchema : oldTransferSchemas) {
             oldSchema.delete();
             getStatus().addDescriptionLine("Removed 1.2 schema file: " + oldSchema.getName());
         }
         
         // copy in the caGrid transfer schema and add the namespace types for it
         File transferSchema = new File(extensionSchemaDir, "TransferServiceContextTypes.xsd");
         try {
             Utils.copyFile(
                 transferSchema, new File(serviceSchemaDir, "TransferServiceContextTypes.xsd"));
             getStatus().addDescriptionLine("Copied caGrid 1.4 transfer schema " + transferSchema.getName());
         } catch (IOException e) {
             throw new RuntimeException(e.getMessage(), e);
         }
 
         NamespaceType transferServiceNamespace = null;
         try {
             transferServiceNamespace = CommonTools.createNamespaceType(
                 serviceSchemaDir + File.separator + "TransferServiceContextTypes.xsd", 
                 serviceSchemaDir);
         } catch (Exception e) {
             throw new RuntimeException(e.getMessage(), e);
         }
         transferServiceNamespace.setGenerateStubs(new Boolean(false));
         transferServiceNamespace.setPackageName("org.cagrid.transfer.context.stubs.types");
         
         File transferDescSchema = new File(extensionSchemaDir, "caGrid_Transfer.xsd");
         try {
             Utils.copyFile(transferDescSchema, 
                 new File(serviceSchemaDir, "caGrid_Transfer.xsd"));
             getStatus().addDescriptionLine("Copied caGrid 1.4 schema " + transferDescSchema.getName());
         } catch (IOException e) {
             throw new RuntimeException(e.getMessage(), e);
         }
 
         NamespaceType transferDescNamespace = null;
         try {
             transferDescNamespace = CommonTools.createNamespaceType(
                 serviceSchemaDir + File.separator + "caGrid_Transfer.xsd", 
                 serviceSchemaDir);
         } catch (Exception e) {
             throw new RuntimeException(e.getMessage(), e);
         }
         transferDescNamespace.setGenerateStubs(new Boolean(false));
         transferDescNamespace.setPackageName("org.cagrid.transfer.descriptor");
         
         NamespaceType[] namespaces = getServiceInformation().getNamespaces().getNamespace();
         for (int i = 0; i < namespaces.length; i++) {
             if (namespaces[i].getNamespace().equals(transferDescNamespace.getNamespace())) {
                 namespaces[i] = transferDescNamespace;
                 getStatus().addDescriptionLine(
                     "Replaced transfer namespace type in service model: " + namespaces[i].getNamespace());
             }
             if (namespaces[i].getNamespace().equals(transferServiceNamespace.getNamespace())) {
                 namespaces[i] = transferServiceNamespace;
                 getStatus().addDescriptionLine(
                     "Replaced transfer namespace type in service model " + namespaces[i].getNamespace());
             }
         }
     }
 
 
     /**
      * Upgrade the jars which are required for Transfer
      */
     private void upgradeJars() {
         FileFilter transferLibFiler = new FileFilter() {
             public boolean accept(File pathname) {
                 String name = pathname.getName();
                 return name.endsWith(CAGRID_1_3_TRANSFER_JAR_SUFFIX) && name.startsWith(CAGRID_1_3_TRANSFER_JAR_PREFIX);
             }
         };
 
         // locate the old data service libs in the service
         File serviceLibDir = new File(getServicePath(), "lib");
         File[] serviceTransferLibs = serviceLibDir.listFiles(transferLibFiler);
         // delete the old libraries
         for (File oldLib : serviceTransferLibs) {
             oldLib.delete();
             getStatus().addDescriptionLine("caGrid 1.3 library " + oldLib.getName() + " removed");
         }
         // copy new libraries in
         File[] transferLibs = extensionLibDir.listFiles();
         List<File> outLibs = new ArrayList<File>(transferLibs.length);
         for (File newLib : transferLibs) {
             File out = new File(serviceLibDir, newLib.getName());
             try {
                 Utils.copyFile(newLib, out);
                 getStatus().addDescriptionLine("caGrid 1.4 library " + newLib.getName() + " added");
             } catch (IOException ex) {
                 // TODO: change this to use a better exception
                 throw new RuntimeException("Error copying new transfer library: " + ex.getMessage(), ex);
             }
             outLibs.add(out);
         }
 
         // update the Eclipse .classpath file
         File classpathFile = new File(getServicePath(), ".classpath");
         File[] outLibArray = new File[transferLibs.length];
         outLibs.toArray(outLibArray);
         try {
             ExtensionUtilities.syncEclipseClasspath(classpathFile, outLibArray);
             getStatus().addDescriptionLine("Eclipse .classpath file updated");
         } catch (Exception ex) {
             // TODO: change this to use a better exception
             throw new RuntimeException("Error updating Eclipse .classpath file: " + ex.getMessage(), ex);
         }
     }
 }
