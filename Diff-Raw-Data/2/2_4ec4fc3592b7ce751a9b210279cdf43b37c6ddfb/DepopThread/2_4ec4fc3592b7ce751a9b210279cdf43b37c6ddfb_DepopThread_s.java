 package com.nullblock.vemacs.perplayer.threads;
 
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Monster;
 
 public class DepopThread implements Runnable {
 
 	private int safe;
 	private List<Entity> entities;
 	private int pass = 10;
 	private int delay = 1;
 	private String name;
 
 	public DepopThread(int safe, List<Entity> entities, String name) {
 		this.safe = safe;
 		this.entities = entities;
 		this.name = name;
 	}
 
 	public void run() {
 		while (entities.size() > safe) {
 			List<Entity> toremove = pickRandom(entities, pass);
 			depop(toremove);
 			try {
 				Thread.sleep(delay * 1000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 		MonitorThread.LOGGER.info("Depopulation completed for " + name);
 		Thread.currentThread().interrupt();
 	}
 
 	public void depop(List<Entity> toremove) {
		Iterator remover = entities.iterator();
 		while (remover.hasNext()) {
 			Entity nextentity = (Entity) remover.next();
 			if (nextentity != null && nextentity instanceof Monster) {
 				nextentity.remove();
 			}
 			remover.remove();
 		}
 	}
 
 	public List<Entity> pickRandom(List<Entity> entities, int number) {
 		List<Entity> copy = new LinkedList<Entity>(entities);
 		Collections.shuffle(copy);
 		return copy.subList(0, number);
 	}
 }
