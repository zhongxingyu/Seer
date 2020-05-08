/**
 * Copyright (C) 2011  JTalks.org Team
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
 package org.jtalks.poulpe.model.dao.hibernate;
 
 import java.util.List;
 import org.jtalks.common.model.dao.hibernate.AbstractHibernateParentRepository;
 import org.jtalks.poulpe.model.dao.RankDao;
 import org.jtalks.poulpe.model.entity.Rank;
 
 /**
  * Implementation of dao for {@link Rank}. The most of methods inherited from
  * superclass.
  * @author Pavel Vervenko
  */
 public class RankHibernateDao extends AbstractHibernateParentRepository<Rank> implements RankDao {
 
     /**
      * {@inheritDoc}
      */
     @Override
     public final List<Rank> getAll() {
         return getSession().createQuery("from Rank").list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isRankNameExists(String name) {
         return ((Number) getSession().createQuery(
                 "select count(*) from Rank r where r.rankName = ?")
                 .setString(0, name)
                 .uniqueResult()).intValue() != 0;
     }
 }
