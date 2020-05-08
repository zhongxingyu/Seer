 /*
  * Copyright (C) 2011 Secretariat of the Pacific Community
  *
  * This file is part of TUBS.
  *
  * TUBS is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * TUBS is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with TUBS.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.spc.ofp.tubs.domain.purseseine;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.persistence.PersistenceContext;
 import javax.persistence.PersistenceUnit;
 
 import org.springframework.stereotype.Repository;
 
 /**
  * @author Corey Cole <coreyc@spc.int>
  *
  */
 @Repository("purseseine.TripRepository")
 public class TripRepository {
 
 	@PersistenceContext
 	protected EntityManager em; // Used for read-only stuff
 	
 	@PersistenceUnit
 	protected EntityManagerFactory emf;
 	
 	public void save(final PurseSeineTrip trip) {
 		final EntityManager mgr = emf.createEntityManager();
		try {
			final EntityTransaction xa = mgr.getTransaction();
 			xa.begin();
 			mgr.persist(trip);
 			mgr.flush();
 			xa.commit();
 			mgr.refresh(trip);
 		} finally {
 			mgr.close();
 		}
 	}
 	
 	public void update(final PurseSeineTrip trip) {
 		final EntityManager mgr = emf.createEntityManager();
		try {
			final EntityTransaction xa = mgr.getTransaction();
 			xa.begin();
 			mgr.merge(trip);
 			xa.commit();
 		} finally {
 			mgr.close();
 		}
 	}
 	
 	public PurseSeineTrip findById(final long tripId) {
 		return em.find(PurseSeineTrip.class, tripId);
 	}
 }
