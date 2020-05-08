 package org.kevoree.monitoring;
 
import org.kevoree.ContainerNode;
import org.kevoree.ContainerRoot;
import org.kevoree.DeployUnit;
 import org.kevoree.annotation.ComponentType;
 import org.kevoree.annotation.Start;
 import org.kevoree.annotation.Stop;
 import org.kevoree.annotation.Update;
import org.kevoree.api.service.core.handler.ModelListener;
 import org.kevoree.framework.AbstractComponentType;
 import org.kevoree.microsandbox.core.CoverageRuntime;
 
 //import org.kevoree.microsandbox.core.KevoreeJarClassLoaderCoverageInjection;
 
 /**
  * Created with IntelliJ IDEA.
  * User: inti
  * Date: 6/24/13
  * Time: 3:55 PM
  * To change this template use File | Settings | File Templates.
  */
 @ComponentType
 public class CoverageChecker extends AbstractComponentType {
     private class Th extends Thread {
         private synchronized boolean isB() {
             return b;
         }
 
         private synchronized void setB(boolean b) {
             this.b = b;
         }
 
         boolean b;
         @Override
         public void run() {
             while (!isB()) {
                 try {
                     Thread.sleep(3000);
                     ClassLoader l = this.getClass().getClassLoader();
                     if (l == null)
                         System.out.println("Impossible to calculate coverage");
                     else
                        System.out.println("Coverage is around : " + CoverageRuntime.instance$.calculateCoverage(l));
                 } catch (InterruptedException e) {
 
                 }
             }
         }
     }
 
     Th th;
 
     @Start
     public void startComponent() {
         th = new Th();
         th.start();
        ContainerRoot root = getModelService().getLastModel();
        System.out.println("Deploy Units");
        for (DeployUnit unit : root.getDeployUnits()) {
            System.out.println(unit.getUnitName() + " " + unit.getName());
        }

        System.out.println("Deploy Units");
        for (ContainerNode node : root.getNodes()) {
            node.
            System.out.println(node.getHost());

        }
     }
 
     @Stop
     public void stopComponent() {
         th.setB(true);
     }
 
     @Update
     public void updateComponent() {
         stopComponent();
         startComponent();
     }
 }
