 /*   _______ __ __                    _______                    __   
  *  |     __|__|  |.--.--.-----.----.|_     _|.----.-----.--.--.|  |_ 
  *  |__     |  |  ||  |  |  -__|   _|  |   |  |   _|  _  |  |  ||   _|
  *  |_______|__|__| \___/|_____|__|    |___|  |__| |_____|_____||____|
  * 
  *  Copyright 2008 - Gustav Tiger, Henrik Steen and Gustav Sothell
  * 
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  * 
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  * 
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package silvertrout;
 
 import java.io.File;
 import java.io.FileInputStream;
 
 class PluginClassLoader extends ClassLoader {
 
     @Override
     protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (name.startsWith("jbt.plugins")) {
             name = name.replace('.', '/');
             try {
                 File f = new File(name + ".class");
                 if (f.exists()) {
                     byte[] bytes = new byte[(int) f.length()]; //Fix possible error when long > Integer.MAX
                     FileInputStream s = new FileInputStream(f);
                     for (int i = 0; i < f.length(); i++) {
                         bytes[i] = (byte) s.read();
                     }
                     return defineClass(null, bytes, 0, (int) f.length());
                 } else {
                     System.out.println("File don't exists: " + name + ".class");
                 }
             } catch (Exception e) {
             }
         } else {
             System.out.println("Wrong prefix");
         }
         throw new ClassNotFoundException(name + " is not a plugin");
     }
 
     @Override
     protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.startsWith("jbt.plugins")) {
             return findClass(name);
         } else {
             return super.loadClass(name, resolve);
         }
     }
 }
 
