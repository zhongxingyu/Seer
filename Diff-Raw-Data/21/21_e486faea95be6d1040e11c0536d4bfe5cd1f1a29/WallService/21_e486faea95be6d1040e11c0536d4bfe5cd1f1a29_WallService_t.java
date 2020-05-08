 package com.jsmadja.wall.projectwall.service;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 
 import com.google.common.base.Preconditions;
 import com.jsmadja.wall.projectwall.domain.Wall;
 
 @Service
public final class WallService {

    private final static int EVERY_FIVE_MINUTES = 5*60*1000;
 
     private Set<Wall> walls = new HashSet<Wall>();
 
     private static final Logger LOG = LoggerFactory.getLogger(WallService.class);
 
     public WallService() {
         if (LOG.isInfoEnabled()) {
             LOG.info("WallService is starting ...");
         }
     }
 
    @Scheduled(fixedDelay=EVERY_FIVE_MINUTES)
     public synchronized void refreshWalls() {
         if (LOG.isInfoEnabled()) {
             LOG.info("It's time to refresh all walls");
         }
         for(Wall wall:walls) {
             if (LOG.isInfoEnabled()) {
                 LOG.info("Refreshing wall : "+wall+" and its "+wall.getProjects().size()+" projects");
             }
             wall.refreshProjects();
         }
     }
 
     public synchronized void addWall(Wall wall) {
         Preconditions.checkNotNull(wall);
         walls.add(wall);
     }
 }
