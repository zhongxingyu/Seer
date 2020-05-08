 package com.someluigi.slperiph.tileentity;
 
 import java.io.PrintStream;
 import java.util.LinkedList;
 
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.tileentity.TileEntity;
 
 import com.someluigi.slperiph.SLPMod;
 import com.someluigi.slperiph.server.SLPHTTPServer;
 
 import dan200.computer.api.IComputerAccess;
 import dan200.computer.api.IPeripheral;
 
 public class TileEntityHTTPD extends TileEntity implements IPeripheral {
 
     public static String[] methods = new String[] { "isActive", "respond",
             "start", "stop" };
 
     public LinkedList<Object> reqsw = new LinkedList<Object>();
 
     @Override
     public String getType() {
         return "http-server";
     }
 
     @Override
     public String[] getMethodNames() {
         return TileEntityHTTPD.methods;
     }
 
     @Override
     public Object[] callMethod(IComputerAccess computer, int method,
             Object[] args) throws Exception {
 
         String mn = methods[method];
 
         if (mn.equals("isActive")) return new Object[] { SLPMod.httpdEnabled };
         if (mn.equals("respond")) {
            
            Object pso = this.reqsw.get((int) ((Double) args[0]).doubleValue());
            
            if (pso instanceof PrintStream) {
                PrintStream ps = (PrintStream) pso;
                
                ps.print(args[1]);
                ps.close();
             }
         }
         if (mn.equals("start")) {
             SLPHTTPServer.services.put(computer.getID(), new Object[] {
                     computer, this });
         }
         if (mn.equals("stop")) {
             SLPHTTPServer.services.remove(computer.getID());
         }
 
         return null;
     }
 
     @Override
     public boolean canAttachToSide(int side) {
         if (SLPMod.debugM) {
             System.out.print("can attach?");
         }
         return true;
     }
 
     @Override
     public void attach(IComputerAccess computer) {
         if (SLPMod.debugM) {
             System.out.print("attach");
         }
     }
 
     @Override
     public void detach(IComputerAccess computer) {
         if (SLPMod.debugM) {
             System.out.print("detach");
         }
     }
 
     @Override
     public void writeToNBT(NBTTagCompound nbttagcompound) {
         super.writeToNBT(nbttagcompound);
     }
 
     @Override
     public void readFromNBT(NBTTagCompound nbttagcompound) {
         super.readFromNBT(nbttagcompound);
     }
 
 }
