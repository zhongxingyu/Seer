 package net.micwin.elysium.jobs;
 
 /*
  (c) 2012 micwin.net
 
  This file is part of open-space.
 
  open-space is free software: you can redistribute it and/or modify
  it under the terms of the GNU Affero Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.
 
  open-space is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Affero Public License for more details.
 
  You should have received a copy of the GNU Affero Public License
  along with open-space.  If not, see http://www.gnu.org/licenses.
 
  Diese Datei ist Teil von open-space.
 
  open-space ist Freie Software: Sie können es unter den Bedingungen
  der GNU Affero Public License, wie von der Free Software Foundation,
  Version 3 der Lizenz oder (nach Ihrer Option) jeder späteren
  veröffentlichten Version, weiterverbreiten und/oder modifizieren.
 
  open-space wird in der Hoffnung, dass es nützlich sein wird, aber
  OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
  Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
  Siehe die GNU Affero Public License für weitere Details.
 
  Sie sollten eine Kopie der GNU Affero Public License zusammen mit diesem
  Programm erhalten haben. Wenn nicht, siehe http://www.gnu.org/licenses. 
 
  */
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.TimerTask;
 
 import net.micwin.elysium.bpo.MessageBPO;
 import net.micwin.elysium.dao.DaoManager;
 import net.micwin.elysium.entities.GalaxyTimer;
 import net.micwin.elysium.entities.NaniteGroup;
 import net.micwin.elysium.entities.NaniteGroup.State;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
 
 public class AdvancerTask extends TimerTask {
 
 	private static final Logger L = LoggerFactory.getLogger(AdvancerTask.class);
 
 	@Autowired
 	private SessionFactory sessionFactory;
 
 	@Override
 	public void run() {
 		L.info("running @ galaxy time " + GalaxyTimer.get().getGalaxyDate() + "...");
 
		Session session = sessionFactory.openSession();

 		try {
 
 			runNanitesAdvancer();
 			session.flush();
 			advanceArena();
 			session.flush();
 			reanalyzePublicGates();
 			session.flush();
 			advanceNpc();
 			session.flush();
			
 
 		} catch (Exception e) {
 
 			L.error("exception while advancer loop", e);
 		}
 
		session.close();

 		L.info("done");
 	}
 
 	private void advanceNpc() {
 		try {
 			new NPCAdvancer().advance();
 		} catch (Exception e) {
 			L.error("could not advance npcs", e);
 		}
 	}
 
 	/**
 	 * Retrieves all gates that are not home gates (any more) and publish them
 	 * into a sys property.
 	 */
 	private void reanalyzePublicGates() {
 		try {
 			new PublicGatesAdvancer().advance();
 		} catch (Exception e) {
 			L.error("could not advance public gates", e);
 		}
 	}
 
 	private void advanceArena() {
 		try {
 			new ArenaAdvancer().advance();
 		} catch (Exception e) {
 			L.error("could not advance arena", e);
 		}
 
 	}
 
 	private void runNanitesAdvancer() {
 		L.info("running nanites advancer ...");
 
 		org.hibernate.classic.Session currentSession = sessionFactory.getCurrentSession();
 
 		currentSession.getTransaction().begin();
 
 		Collection<NaniteGroup> result = DaoManager.I.getNanitesDao().findByState(State.ENTRENCHING);
 		int changeCount = 0;
 
 		for (Iterator<NaniteGroup> iterator = result.iterator(); iterator.hasNext();) {
 			NaniteGroup naniteGroup = iterator.next();
 
 			boolean changedSomething = advanceEntrenching(naniteGroup);
 
 			if (changedSomething) {
 				changeCount++;
 				currentSession.update(naniteGroup);
 			}
 
 		}
 		if (changeCount > 0) {
 			currentSession.flush();
 			currentSession.getTransaction().commit();
 
 		}
 
 		L.info(changeCount + " groups advanced");
 	}
 
 	/**
 	 * Checks wether this group is entrenching and if, check dates.
 	 * 
 	 * @param naniteGroup
 	 * @return
 	 */
 	public boolean advanceEntrenching(NaniteGroup naniteGroup) {
 
 		if (naniteGroup.getState() != State.ENTRENCHING) {
 			return false;
 		}
 
 		if (naniteGroup.getStateEndGT().before(GalaxyTimer.get().getGalaxyDate())) {
 			naniteGroup.setStateEndGT(null);
 			naniteGroup.setState(State.ENTRENCHED);
 			new MessageBPO().send(naniteGroup, naniteGroup.getController(), "Eingraben beendet.");
 			return true;
 		}
 		L.debug("nanite group still has to wait until " + naniteGroup.getStateEndGT() + " ");
 		return false;
 	}
 }
