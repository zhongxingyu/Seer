 /*
  * PatientView
  *
  * Copyright (c) Worth Solutions Limited 2004-2013
  *
  * This file is part of PatientView.
  *
  * PatientView is free software: you can redistribute it and/or modify it under the terms of the
  * GNU General Public License as published by the Free Software Foundation, either version 3 of the License,
  * or (at your option) any later version.
  * PatientView is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
  * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  * You should have received a copy of the GNU General Public License along with PatientView in a file
  * titled COPYING. If not, see <http://www.gnu.org/licenses/>.
  *
  * @package PatientView
  * @link http://www.patientview.org
  * @author PatientView <info@patientview.org>
  * @copyright Copyright (c) 2004-2013, Worth Solutions Limited
  * @license http://www.gnu.org/licenses/gpl-3.0.html The GNU General Public License V3.0
  */
 
 package org.patientview.repository.impl;
 
 import org.patientview.patientview.logon.UnitAdmin;
 import org.patientview.patientview.model.Specialty;
 import org.patientview.patientview.model.Unit;
 import org.patientview.patientview.model.Unit_;
 import org.patientview.patientview.model.User;
 import org.patientview.repository.AbstractHibernateDAO;
 import org.patientview.repository.UnitDao;
 import org.patientview.utils.LegacySpringUtils;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.core.RowMapper;
 import org.springframework.stereotype.Repository;
 
 import javax.annotation.PostConstruct;
 import javax.inject.Inject;
 import javax.persistence.NoResultException;
 import javax.persistence.Query;
 import javax.persistence.criteria.CriteriaBuilder;
 import javax.persistence.criteria.CriteriaQuery;
 import javax.persistence.criteria.Predicate;
 import javax.persistence.criteria.Root;
 import javax.sql.DataSource;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  *  Note: I have changed the implementation to allow units to be returned when the specialty is null
  *  i.e. we are not a logged in user.  (PC 01/03/2013)
  *  The unitcode is unique so we should not get NonUniqueResultException
  */
 @Repository(value = "unitDao")
 public class UnitDaoImpl extends AbstractHibernateDAO<Unit> implements UnitDao {
 
     private JdbcTemplate jdbcTemplate;
 
     @Inject
     private DataSource dataSource;
 
     @PostConstruct
     public void init() {
         jdbcTemplate = new JdbcTemplate(dataSource);
     }
 
     @Override
     public Unit get(String unitCode, Specialty specialty) {
 
         CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
         CriteriaQuery<Unit> criteria = builder.createQuery(Unit.class);
         Root<Unit> from = criteria.from(Unit.class);
         List<Predicate> wherePredicates = new ArrayList<Predicate>();
 
         wherePredicates.add(builder.equal(from.get(Unit_.unitcode), unitCode));
 
         if (specialty != null) {
             wherePredicates.add(builder.equal(from.get(Unit_.specialty), specialty));
         }
 
         buildWhereClause(criteria, wherePredicates);
         try {
             return getEntityManager().createQuery(criteria).getSingleResult();
         } catch (NoResultException e) {
             return null;
         }
     }
 
     @Override
     public List<Unit> getAll(boolean sortByName) {
         if (sortByName) {
             CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
             CriteriaQuery<Unit> criteria = builder.createQuery(Unit.class);
             Root<Unit> from = criteria.from(Unit.class);
 
             criteria.orderBy(builder.asc(from.get(Unit_.name)));
 
             return getEntityManager().createQuery(criteria).getResultList();
         } else {
             return getAll();
         }
     }
 
     @Override
     public List<Unit> getAll(boolean sortByName, Specialty specialty) {
 
         if (sortByName) {
 
             CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
             CriteriaQuery<Unit> criteria = builder.createQuery(Unit.class);
             Root<Unit> from = criteria.from(Unit.class);
             List<Predicate> wherePredicates = new ArrayList<Predicate>();
 
             if (specialty != null) {
                 wherePredicates.add(builder.equal(from.get(Unit_.specialty), specialty));
             }
 
             buildWhereClause(criteria, wherePredicates);
 
             criteria.orderBy(builder.asc(from.get(Unit_.name)));
 
             return getEntityManager().createQuery(criteria).getResultList();
         } else {
             return getAll();
         }
     }
 
     @Override
     public List<Unit> getAll(String[] sourceTypesToExclude, String[] sourceTypesToInclude) {
         CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
         CriteriaQuery<Unit> criteria = builder.createQuery(Unit.class);
         Root<Unit> from = criteria.from(Unit.class);
         List<Predicate> wherePredicates = new ArrayList<Predicate>();
 
         if (sourceTypesToInclude != null && sourceTypesToInclude.length > 0) {
             wherePredicates.add(from.get(Unit_.sourceType).in(sourceTypesToInclude));
         }
 
         if (sourceTypesToExclude != null) {
             for (String notSourceType : sourceTypesToExclude) {
                 wherePredicates.add(builder.notEqual(from.get(Unit_.sourceType), notSourceType));
             }
         }
 
         buildWhereClause(criteria, wherePredicates);
         criteria.orderBy(builder.asc(from.get(Unit_.name)));
 
         return getEntityManager().createQuery(criteria).getResultList();
     }
 
     @Override
     public List<Unit> getUnitsWithUser(Specialty specialty) {
 
         CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
         CriteriaQuery<Unit> criteria = builder.createQuery(Unit.class);
         Root<Unit> from = criteria.from(Unit.class);
         List<Predicate> wherePredicates = new ArrayList<Predicate>();
 
         wherePredicates.add(builder.isNotNull(from.get(Unit_.unituser)));
         wherePredicates.add(builder.notEqual(from.get(Unit_.unituser), ""));
 
         if (specialty != null) {
             wherePredicates.add(builder.equal(from.get(Unit_.specialty), specialty));
         }
 
         buildWhereClause(criteria, wherePredicates);
         return getEntityManager().createQuery(criteria).getResultList();
     }
 
     @Override
     public List<Unit> getAdminsUnits(Specialty specialty) {
 
         CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
         CriteriaQuery<Unit> criteria = builder.createQuery(Unit.class);
         Root<Unit> from = criteria.from(Unit.class);
         List<Predicate> wherePredicates = new ArrayList<Predicate>();
 
         wherePredicates.add(builder.equal(from.get(Unit_.specialty), specialty));
 //        wherePredicates.add(builder.notEqual(from.get(Unit_.sourceType), "radargroup"));
 
         criteria.orderBy(builder.asc(from.get(Unit_.name)));
 
         buildWhereClause(criteria, wherePredicates);
         return getEntityManager().createQuery(criteria).getResultList();
     }
 
     @Override
     public List<Unit> get(List<String> usersUnitCodes, Specialty specialty) {
 
         CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
         CriteriaQuery<Unit> criteria = builder.createQuery(Unit.class);
         Root<Unit> from = criteria.from(Unit.class);
         List<Predicate> wherePredicates = new ArrayList<Predicate>();
 
         if (usersUnitCodes != null && usersUnitCodes.size() > 0) {
             wherePredicates.add(from.get(Unit_.unitcode).in(usersUnitCodes.toArray(new String[usersUnitCodes.size()])));
         }
 
         if (specialty != null) {
             wherePredicates.add(builder.equal(from.get(Unit_.specialty), specialty));
         }
 
         buildWhereClause(criteria, wherePredicates);
         return getEntityManager().createQuery(criteria).getResultList();
     }
 
     @Override
     public List<Unit> get(List<String> usersUnitCodes, String[] notTheseUnitCodes, String[] plusTheseUnitCodes,
                           Specialty specialty) {
 
         CriteriaBuilder builder = getEntityManager().getCriteriaBuilder();
         CriteriaQuery<Unit> criteria = builder.createQuery(Unit.class);
         Root<Unit> from = criteria.from(Unit.class);
         List<Predicate> wherePredicates = new ArrayList<Predicate>();
 
         // add in the additional ones, only add in if we have usersUnitCodes, otherwise we need all of them
         if (plusTheseUnitCodes != null && plusTheseUnitCodes.length > 0 && usersUnitCodes != null
                 && usersUnitCodes.size() > 0) {
             Collections.addAll(usersUnitCodes, plusTheseUnitCodes);
         }
 
         if (usersUnitCodes != null && usersUnitCodes.size() > 0) {
             wherePredicates.add(from.get(Unit_.unitcode).in(usersUnitCodes.toArray(new String[usersUnitCodes.size()])));
         }
 
         if (notTheseUnitCodes != null) {
             for (String notUnitCode : notTheseUnitCodes) {
                 wherePredicates.add(builder.notEqual(from.get(Unit_.unitcode), notUnitCode));
             }
         }
 
         if (specialty != null) {
             wherePredicates.add(builder.equal(from.get(Unit_.specialty), specialty));
         }
 
         buildWhereClause(criteria, wherePredicates);
         criteria.orderBy(builder.asc(from.get(Unit_.name)));
 
         return getEntityManager().createQuery(criteria).getResultList();
     }
 
     @Override
     public List<UnitAdmin> getUnitUsers(String unitcode, Specialty specialty) {
         String sql = "SELECT "
                + "  u.*, um.unitcode  "
                 + "FROM "
                 + "   User u, "
                 + "   UserMapping um, "
                 + "   SpecialtyUserRole sur "
                 + "WHERE "
                 + "   u.username = um.username "
                 + "AND "
                 + "   u.id = sur.user_id "
                 + "AND "
                 + "   sur.specialty_id = ? "
                 + "AND "
                 + "   um.unitcode = ? ";
 
         String userRole = LegacySpringUtils.getUserManager().getLoggedInUserRole();
         if ("radaradmin".equals(userRole) || "superadmin".equals(userRole)) {
             sql += "AND "
                     + "   (sur.role = 'radaradmin')";
         } else {
             sql += "AND "
                     + "   (sur.role = 'unitadmin' OR sur.role = 'unitstaff')";
         }
 
         List<Object> params = new ArrayList<Object>();
         params.add(specialty == null ? "" : specialty.getId());
         params.add(unitcode);
 
         return jdbcTemplate.query(sql, params.toArray(), new UnitAdminMapper());
     }
 
     private class UnitAdminMapper implements RowMapper<UnitAdmin> {
         @Override
         public UnitAdmin mapRow(ResultSet resultSet, int i) throws SQLException {
             UnitAdmin unitAdmin = new UnitAdmin();
             unitAdmin.setUsername(resultSet.getString("username"));
             unitAdmin.setName(resultSet.getString("name"));
             unitAdmin.setEmail(resultSet.getString("email"));
             unitAdmin.setEmailverfied(resultSet.getBoolean("emailverified"));
            unitAdmin.setRole(resultSet.getString("role"));
             unitAdmin.setFirstlogon(resultSet.getBoolean("firstlogon"));
             unitAdmin.setIsrecipient(resultSet.getBoolean("isrecipient"));
             unitAdmin.setIsclinician(resultSet.getBoolean("isclinician"));
             unitAdmin.setLastlogon(resultSet.getDate("lastlogon"));
             unitAdmin.setAccountlocked(resultSet.getBoolean("accountlocked"));
             unitAdmin.setUnitcode(resultSet.getString("unitcode"));
             return unitAdmin;
         }
     }
 
     @Override
     public List<UnitAdmin> getAllUnitUsers(Specialty specialty) {
         String sql = "SELECT "
                + "  u.*, um.unitcode  "
                 + "FROM "
                 + "   User u, "
                 + "   UserMapping um, "
                 + "   SpecialtyUserRole sur "
                 + "WHERE "
                 + "   u.username = um.username "
                 + "AND "
                 + "   u.id = sur.user_id "
                 + "AND "
                 + "   sur.specialty_id = ? "
                 + "AND "
                 + "   (sur.role = 'unitadmin' OR sur.role = 'unitstaff' OR sur.role = 'radaradmin')";
 
 
         List<Object> params = new ArrayList<Object>();
         params.add(specialty == null ? "" : specialty.getId());
 
         return jdbcTemplate.query(sql, params.toArray(), new UnitAdminMapper());
 
     }
 
     @Override
     public List<User> getUnitPatientUsers(String unitcode, Specialty specialty) {
         String sql = "SELECT "
                 + "   u.* "
                 + "FROM "
                 + "   usermapping um, "
                 + "   USER u, "
                 + "   specialtyuserrole sur "
                 + "WHERE"
                 + "   um.username = u.username "
                 + "AND"
                 + "   u.id = sur.user_id "
                 + "AND"
                 + "   sur.specialty_id = :specialtyId "
                 + "AND"
                 + "   um.unitcode = :unitcode "
                 + "AND"
                 + "   sur.role = 'patient' ";
 
         Query query = getEntityManager().createNativeQuery(sql, User.class);
 
         query.setParameter("specialtyId", specialty.getId());
         query.setParameter("unitcode", unitcode);
 
         return query.getResultList();
     }
 }
