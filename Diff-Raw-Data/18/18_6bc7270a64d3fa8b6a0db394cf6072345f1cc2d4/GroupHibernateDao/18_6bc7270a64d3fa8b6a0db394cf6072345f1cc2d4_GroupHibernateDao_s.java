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
 
 import org.apache.commons.lang.Validate;
 import org.apache.commons.lang3.StringUtils;
 import org.hibernate.Query;
 import org.jtalks.common.model.dao.hibernate.AbstractHibernateParentRepository;
 import org.jtalks.common.model.entity.Group;
 import org.jtalks.poulpe.model.dao.GroupDao;
 import org.jtalks.poulpe.model.dao.utils.SqlLikeEscaper;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 import ru.javatalks.utils.general.Assert;
 
 import java.util.List;
 
 /**
  * Hibernate implementation of {@link GroupDao}
  *
  * @author Vitaliy Kravchenko
  * @author Pavel Vervenko
  * @author Leonid Kazancev
  */
 public class GroupHibernateDao extends AbstractHibernateParentRepository<Group> implements GroupDao {
     private static final String FIND_BRANCHES_MODERATED_BY_GROUP = "findBranchesModeratedByGroup";
     private static final String FIND_GROUP_BY_NAME = "findGroupByName", FIND_ALL_GROUPS = "findAllGroups";
     private static final String FIND_EXACTLY_BY_NAME = "findGroupExactlyByName";
 
     /**
      * {@inheritDoc}
      */
     @Override
     @SuppressWarnings("unchecked")
     public List<Group> getAll() {
         return getSession().getNamedQuery(FIND_ALL_GROUPS).list();
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     @Override
     public List<Group> getByNameContains(String name) {
         Validate.notNull(name, "User Group name can't be null");
         if (StringUtils.isBlank(name)) {
             return this.getAll();
         }
         Query query = getSession().getNamedQuery(FIND_GROUP_BY_NAME);
         query.setString("name", SqlLikeEscaper.escapeControlCharacters(name));
         return query.list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public List<Group> getByName(String name) {
         Validate.notNull(name, "User Group name can't be null");
         Query query = getSession().getNamedQuery(FIND_EXACTLY_BY_NAME);
         // we should use lower case to search ignoring case
        query.setString("name", SqlLikeEscaper.escapeControlCharacters(name));
         return query.list();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void delete(Group group) {
         getSession().update(group);
 
         group.getUsers().clear();
         saveOrUpdate(group);
         super.delete(group);
     }
 
     /**
      * {@inheritDoc}
      */
     @SuppressWarnings("unchecked")
     @Override
     public List<PoulpeBranch> getModeratingBranches(Group group) {
         Query query = getSession().getNamedQuery(FIND_BRANCHES_MODERATED_BY_GROUP);
         query.setLong(0, group.getId());
         return query.list();
     }
 
 }
