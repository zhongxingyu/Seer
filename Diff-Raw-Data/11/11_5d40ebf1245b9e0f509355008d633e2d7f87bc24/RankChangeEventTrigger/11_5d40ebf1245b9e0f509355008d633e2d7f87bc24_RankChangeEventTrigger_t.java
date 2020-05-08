 /**
  * Tysan Clan Website
  * Copyright (C) 2008-2013 Jeroen Steenbeeke and Ties van de Ven
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.tysanclan.site.projectewok.event.handlers;
 
 import java.util.ArrayList;
 
 import com.google.common.collect.Lists;
 import com.jeroensteenbeeke.hyperion.events.Event;
 import com.jeroensteenbeeke.hyperion.events.EventHandler;
 import com.jeroensteenbeeke.hyperion.events.EventResult;
 import com.tysanclan.site.projectewok.entities.Rank;
 import com.tysanclan.site.projectewok.entities.User;
 import com.tysanclan.site.projectewok.event.ForumUserBannedEvent;
 import com.tysanclan.site.projectewok.event.MembershipTerminatedEvent;
 import com.tysanclan.site.projectewok.event.RankChangeEvent;
 import com.tysanclan.site.projectewok.event.UserAppointedTruthsayerEvent;
 import com.tysanclan.site.projectewok.event.UserElectedToChancellorEvent;
 import com.tysanclan.site.projectewok.event.UserElectedToSenateEvent;
 import com.tysanclan.site.projectewok.event.UserPromotedEvent;
 import com.tysanclan.site.projectewok.event.UserUnbannedEvent;
 
 public class RankChangeEventTrigger implements EventHandler<RankChangeEvent> {
 	@Override
 	public EventResult onEvent(RankChangeEvent event) {
 		ArrayList<Event<?>> triggered = Lists.newArrayList();
 
 		User user = event.getSubject();
 
 		Rank oldRank = user.getOldRank();
 		Rank newRank = user.getRank();
 
 		if (newRank == Rank.FORUM || newRank == Rank.BANNED) {
 			if (oldRank == Rank.FORUM && newRank == Rank.BANNED) {
 				// Forum user was banned from forum
 				triggered.add(new ForumUserBannedEvent(user));
 			} else if (oldRank == Rank.BANNED && newRank == Rank.FORUM) {
 				// User was unbanned
 				triggered.add(new UserUnbannedEvent(user));
 			} else {
 				triggered.add(new MembershipTerminatedEvent(user));
 			}
 		} else {
 			// Otherwise, we don't give a crap about the old rank
 			switch (newRank) {
 				case CHANCELLOR:
 					// Elected
 					triggered.add(new UserElectedToChancellorEvent(user));
 					break;
 				case SENATOR:
 					// Elected
 					triggered.add(new UserElectedToSenateEvent(user));
 					break;
 				case TRUTHSAYER:
 					// Appointed and approved
 					triggered.add(new UserAppointedTruthsayerEvent(user));
 					break;
 				default:
 					// Promoted, since demotion is impossible
 					triggered.add(new UserPromotedEvent(user));
 			}
 		}
 
		return EventResult.ok(triggered);
 	}
 }
