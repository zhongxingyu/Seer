 package com.github.kpacha.mafia.service.impl;
 
 import java.util.Random;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.github.kpacha.mafia.model.Gangster;
 import com.github.kpacha.mafia.model.Place;
 import com.github.kpacha.mafia.service.GangsterService;
 import com.github.kpacha.mafia.service.PlaceService;
 import com.github.kpacha.mafia.service.PopulatorService;
 
 @Service
 @Transactional
 public class PopulatorServiceImpl implements PopulatorService {
 
     private static final Logger log = LoggerFactory
 	    .getLogger(PopulatorServiceImpl.class);
 
     @Autowired
     private GangsterService gangsterService;
     @Autowired
     private PlaceService placeService;
 
     private Random random = new Random();
 
     private int totalCreatedGangsters = 0;
 
     public int populate(final int deep) {
 	log.debug("Populating with deep: " + deep);
 	gangsterService.deleteAll();
 	totalCreatedGangsters = 0;
 	Gangster superboss = buildGangster(deep, 0, deep);
 
 	// superboss.setName("Tony Soprano");
 	// superboss.setComment("Bada Bing! " + superboss.getComment());
 	// repo.save(superboss);
 	log.debug("Populated [" + totalCreatedGangsters + "] instances");
 	return totalCreatedGangsters;
     }
 
     public Gangster buildGangster(int maxSubordinates, int level, int deep) {
 	Gangster gangster = new Gangster();
 	gangster.setName("Sample " + random.nextInt(1000));
 	gangster.setComment("Has " + ((deep > 0) ? maxSubordinates : 0)
 		+ " subordinates and his level is " + level);
 	gangster.setOnDuty(true);
 	totalCreatedGangsters++;
 	gangster = gangsterService.save(gangster);
 	log.debug("Building a gangster with maxSubordinates: "
 		+ maxSubordinates + ", level: " + level + ", deep: " + deep
 		+ ", nodeId: " + gangster.getNodeId());
 
 	if (deep > 0) {
 	    for (int i = 0; i < maxSubordinates; i++) {
 		Gangster subordinate = buildGangster(maxSubordinates + level,
 			level + 1, deep - 1);
 		gangster = gangsterService.enroleSubordinate(gangster,
 			subordinate);
 	    }
 	    if (maxSubordinates > 0) {
		gangsterService.save(gangster);
 	    }
 	}
 
 	log.debug("Builded [" + gangster + "]");
 	return gangster;
     }
 
     public Place buildPlace(float lon, float lat) {
 	Place place = new Place();
 	place.setLocation(lon, lat);
 	return placeService.save(place);
     }
 }
