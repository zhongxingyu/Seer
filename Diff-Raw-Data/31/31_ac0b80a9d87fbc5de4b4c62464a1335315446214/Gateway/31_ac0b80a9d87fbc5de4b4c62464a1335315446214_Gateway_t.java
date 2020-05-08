 /*
  * $file.name
  *     Copyright (C) 2012  Philippe VIENNE
  *
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.javascool.polyfilewriter;
 
 import org.javascool.tools.FileManager;
 
import javax.swing.*;
 import java.applet.Applet;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 
 /**
  * Created with IntelliJ IDEA.
  * User: pvienne
  * Date: 7/30/12
  * Time: 9:20 AM
  * To change this template use File | Settings | File Templates.
  */
 public class Gateway extends Applet {
 
    private boolean appletLocked=false;

    @Override
    public void init(){
        if(!getCodeBase().getProtocol().equals("file")){
            appletLocked=true;
        }
        super.init();
    }

     public String load(final String location) {
        assertSafeUsage();
         return (String) AccessController.doPrivileged(
                 new PrivilegedAction() {
                     public Object run() {
                         return FileManager.load(location);
                     }
                 }
         );
     }
 
     public boolean save(final String location, final String what) {
        assertSafeUsage();
         return (Boolean) AccessController.doPrivileged(
                 new PrivilegedAction() {
                     public Object run() {
                         try {
                             FileManager.save(location, what);
                             return true;
                         } catch (Exception e) {
                             return false;
                         }
                     }
                 }
         );
     }
 
     public String askFile() {
         return askFile(false);
     }
 
     public String askFile(boolean forSave) {
        assertSafeUsage();
         return "";
     }
 
    private boolean showMessage=true;

    private void assertSafeUsage(){
        if(appletLocked){
            if(showMessage){
                JOptionPane.showMessageDialog(this, "This website ("+getCodeBase().getHost()+") tried to hack your computer by accessing to the local file system (Attack stopped)", "Error", JOptionPane.ERROR_MESSAGE);
                showMessage=false;
            }
            throw new SecurityException("This website is not authorized to use this applet");
        }else{}
    }

 }
