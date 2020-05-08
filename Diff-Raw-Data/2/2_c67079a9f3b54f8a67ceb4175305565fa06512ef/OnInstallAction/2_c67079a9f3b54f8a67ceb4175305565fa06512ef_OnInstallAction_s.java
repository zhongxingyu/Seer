 package org.zeroturnaround.example.p2.spells;
 
 import java.io.IOException;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.equinox.internal.p2.engine.InstallableUnitOperand;
 import org.eclipse.equinox.p2.engine.spi.ProvisioningAction;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 
 @SuppressWarnings("restriction")
 public class OnInstallAction extends ProvisioningAction {
 
   @Override
   public IStatus execute(Map<String, Object> parameters) {
     IInstallableUnit iu = (IInstallableUnit) parameters.get("iu");
     IInstallableUnit upgradeFrom = null;
     Object operand = parameters.get("operand");
     try {
       if (operand instanceof InstallableUnitOperand)
         upgradeFrom = ((InstallableUnitOperand) operand).first();
     }
     catch (Throwable e) {
       // Ignore class not found in case InstallableUnitOperand is missing
     }
     if (upgradeFrom != null)
       performUpgrade(iu, upgradeFrom);
     else
       performNewInstall(iu);
     return Status.OK_STATUS;
   }
 
   private void performUpgrade(IInstallableUnit iu, IInstallableUnit oldIu) {
    String msg = "Upgrade: " + iu.getId() + " " + oldIu + " -> " + iu.getVersion();
     System.out.println(msg);
     try {
       TxtFile txtFile = TxtFile.get();
       System.out.println("Writing to " + txtFile.path());
       txtFile.appendLine(msg);
     }
     catch (IOException e) {
       e.printStackTrace();
     }
   }
 
   private void performNewInstall(IInstallableUnit iu) {
     String msg = "New Install: " + iu.getId() + " " + iu.getVersion();
     System.out.println(msg);
     try {
       TxtFile txtFile = TxtFile.get();
       System.out.println("Writing to " + txtFile.path());
       txtFile.appendLine(msg);
     }
     catch (IOException e) {
       e.printStackTrace();
     }
   }
 
   @Override
   public IStatus undo(Map<String, Object> parameters) {
     return null;
   }
 
 }
