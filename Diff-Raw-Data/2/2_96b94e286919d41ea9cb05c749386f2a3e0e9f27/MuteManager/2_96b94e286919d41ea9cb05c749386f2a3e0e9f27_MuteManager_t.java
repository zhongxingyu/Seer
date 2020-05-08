 /*
  * Copyright (c) 2013 Chris Darnell (cedeel).
  * All rights reserved.
  *
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *     * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright
  *       notice, this list of conditions and the following disclaimer in the
  *       documentation and/or other materials provided with the distribution.
  *     * The name of the author may not be used to endorse or promote products
  *       derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS''
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package be.darnell.mc.FuzzyMessenger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.Map;
 
 public class MuteManager {
 
     private File location;
 
     public MuteManager(File storageFolder) {
         location = storageFolder;
         load();
     }
 
     private Map<String, Mutee> mutees = new HashMap<String, Mutee>();
 
     public boolean isMuted(Player player) {
         return mutees.containsKey(player.getName().toLowerCase());
     }
 
     public Mutee get(String name) {
         return mutees.get(name.toLowerCase());
     }
 
     public Map<String, Mutee> getAll() {
         return mutees;
     }
 
     public boolean add(String mutee) {
        return mutees.put(mutee.toLowerCase(), new Mutee(Bukkit.getPlayer(mutee))) == null;
     }
 
     public boolean remove(String mutee) {
         return mutees.remove(mutee.toLowerCase()) != null;
     }
 
     public void save() {
         File muteeFile = new File(location, "mutees.txt");
         try {
             ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(muteeFile));
             out.writeObject(mutees);
         } catch (Exception e) {
             Bukkit.getServer().getLogger().severe("Could not write mutees to file");
         }
     }
 
     @SuppressWarnings("unchecked")
     private HashMap<String, Mutee> load() {
         File muteeFile = new File(location, "mutees.txt");
         try {
             ObjectInputStream in = new ObjectInputStream(new FileInputStream(muteeFile));
             return (HashMap<String, Mutee>) in.readObject();
         } catch (Exception e) {
             Bukkit.getServer().getLogger().severe("Could not read mutees from file");
             return new HashMap<String, Mutee>();
         }
     }
 }
