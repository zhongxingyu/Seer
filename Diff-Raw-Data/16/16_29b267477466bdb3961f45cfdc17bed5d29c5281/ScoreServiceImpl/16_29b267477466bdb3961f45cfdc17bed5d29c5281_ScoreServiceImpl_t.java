 /*
  * Copyright (c) 2013 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.openinfinity.tagcloud.domain.service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.openinfinity.core.annotation.AuditTrail;
 import org.openinfinity.core.annotation.Log;
 import org.openinfinity.tagcloud.domain.entity.Profile;
 import org.openinfinity.tagcloud.domain.entity.Score;
 import org.openinfinity.tagcloud.domain.entity.Target;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * Product service implementation with specification.
  * 
  * @author Ilkka Leinonen
  */
 @Service
 public class ScoreServiceImpl extends AbstractCrudServiceImpl<Score> implements ScoreService {
   
 	@Autowired
 	private ProfileService profileService;
 
 	@Autowired
 	private TargetService targetService;
 
 	@Log
     @AuditTrail
     @Override
     @Transactional
     public void scoreTarget(int scoreStars, Target target, String facebookId) {
		List<Score> scores = target.getScores();
		Profile profile = profileService.loadByFacebookId(facebookId);
		Score score = create(new Score(scoreStars, profile));
		boolean exists = false;
        for(Score s : scores){
        	if(s.getProfile().getFacebookId() == facebookId){
        		s = score;
        		exists = true;
        	}
        }
        if(!exists){
        	target.addScore(score);
        }              
         targetService.update(target);
         profile.addScoredTarget(target);
         profileService.update(profile);
     }
 
 	@Override
 	public List<Score> getFriendsScores(List<String> friendFacebookIds, Target target) {
 		List<Score> friendScores = new ArrayList<Score>();
 		for(Score score : target.getScores()) {
 			if(friendFacebookIds.contains(score.getProfile().getFacebookId())) {
 				friendScores.add(score);
 			}
 		}
 		return friendScores;
 	}
 
 	@Override
 	public double getAverageFriendScore(List<String> friendFacebookIds, Target target) {
 		List<Score> friendScores = getFriendsScores(friendFacebookIds, target);
 		if(friendScores.size()==0) return -1;
 		int sum = 0;
 		for(Score score : friendScores) {
 			sum += score.getStars();
 		}
 		return 1.0*sum/friendScores.size();
 	}
 
 	@Override
 	public Score getOwnScore(Profile profile, Target target) {
 		if(!profile.getMyScoredTargets().contains(target.getId()))
 			return null;
 		
 		for(Score score : target.getScores()) {
 			if (score.getProfile().equals(profile)) {
 				return score;
 			}
 		}
 		
 		return null;
 	}
 
 }
