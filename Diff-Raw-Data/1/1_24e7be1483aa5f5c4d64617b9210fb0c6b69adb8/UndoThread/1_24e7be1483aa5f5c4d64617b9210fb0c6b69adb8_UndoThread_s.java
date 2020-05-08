 package com.xegaming.worldthreadit;
 
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.NoSuchElementException;
 import java.util.UUID;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 
 public class UndoThread extends Thread {
     final LinkedList<QueuedBlock> undoList = new LinkedList<QueuedBlock>();
     private final WorldThreadit threadit;
     boolean canrun = true;
     final public static LinkedHashMap<UUID, LinkedList<QueuedBlock>> edited = new LinkedHashMap<UUID, LinkedList<QueuedBlock>>();
     final public static LinkedList<UUID> undo = new LinkedList<UUID>();
     final public static LinkedHashMap<String, LinkedList<UUID>> edits = new LinkedHashMap<String, LinkedList<UUID>>();
     public UndoThread(WorldThreadit threadit) {
         this.threadit = threadit;
         this.setName("threadit-UndoThread");
     }
 
     @Override
     public void run() {
     	while(canrun){
     		
     		try {
     			sleep(100);
     		} catch (InterruptedException e) {
     			e.printStackTrace();
     		}
             synchronized (edited) {
             	
     		for(UUID id :undo){
         			LinkedList<QueuedBlock> undolist = edited.get(id);
                     int sleeptime = 0;
                     while (!undolist.isEmpty()) {
                        WorldThreadit.log.info("Undo Queue loaded : " + undolist.size() + " remaining.");
                     	try {
                         	final QueuedBlock queuedBlock = undolist.pop();
                         	threadit.getServer().getScheduler().scheduleSyncDelayedTask(threadit, new Runnable() {
                                 public void run() {
                                     World w = threadit.getServer().getWorld(queuedBlock.worldName);
                                     if (w == null) {
                                         return;
                                     }
                                     final Block b = w.getBlockAt(queuedBlock.X, queuedBlock.Y, queuedBlock.Z);
                                     if (b == null) {
                                         return;
                                     }
 
                                     if (!b.getChunk().isLoaded()) {
                                         b.getChunk().load();
                                     }
                                     b.setTypeId(queuedBlock.oldID);
                                     b.setData(queuedBlock.oldData);
                                 }
                             });
                             sleeptime++;
                             if(sleeptime>=500){
                             	try {
     								sleep(5);
     							} catch (InterruptedException e) {
     								e.printStackTrace();
     							}
                             	sleeptime = 0;
                             }
                         	
                         	
                         } catch (NoSuchElementException e) {
                             return;
                         }
                     }
         		}
         	}
     	}
     }
     
     
     
     public static void addBlock(QueuedBlock b){
     	UUID id = b.uuid;
     	
     	synchronized (edited){
     		if(!edited.containsKey(id)){
     			edited.put(id, new LinkedList<QueuedBlock>());
     		}
     		edited.get(id).add(b);
     	}
     }
     public static void undo(Player p){
     	String name = p.getName();
     	synchronized(edits){
     		synchronized(edited){
         		synchronized(undo){
     		if(edits.containsKey(name)){
     			try {
         			UUID id = edits.get(name).getLast();
         			edits.get(name).removeLast();
         			if(edited.containsKey(id)){
                		 	undo.add(id);
             		}else{
             			Util.sendMessage(p, ChatColor.RED+"You Have No Previous Edits.");
             		}
                 } catch (NoSuchElementException e) {
         			Util.sendMessage(p, ChatColor.RED+"You Have No Previous Edits.");
                     return;
                 }
     		}else{
     			Util.sendMessage(p, ChatColor.RED+"You Have No Previous Edits.");
     		}
     		}
     		}
     	}
 
     }
     public static void addEdit(Player p, UUID id){
     	String name = p.getName();
     	System.out.println(id.toString().length());
     	synchronized(edits){
     		if(!edits.containsKey(name)){
     			edits.put(name, new LinkedList<UUID>());
     		}
     		edits.get(name).add(id);
     	}
     }
     
     
     
     
     
     
     
     
 }
