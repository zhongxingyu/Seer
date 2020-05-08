 package com.twistlet.falcon.model.repository;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.stereotype.Repository;
 
 import com.mysema.query.jpa.JPQLQuery;
 import com.mysema.query.jpa.impl.JPAQuery;
 import com.mysema.query.types.expr.BooleanExpression;
 import com.twistlet.falcon.model.entity.FalconAppointment;
 import com.twistlet.falcon.model.entity.FalconAppointmentPatron;
 import com.twistlet.falcon.model.entity.FalconPatron;
 import com.twistlet.falcon.model.entity.FalconUser;
 import com.twistlet.falcon.model.entity.QFalconAppointment;
 import com.twistlet.falcon.model.entity.QFalconPatron;
 import com.twistlet.falcon.model.entity.QFalconUser;
 
 @Repository
 public class FalconPatronRepositoryImpl implements FalconPatronRepositoryCustom {
 
 	@PersistenceContext
 	EntityManager entityManager;
 	
 	@Override
 	public Set<FalconPatron> findPatronsDateRange(FalconUser admin, Date start, Date end) {
 		final JPQLQuery query = new JPAQuery(entityManager);
 		final QFalconAppointment falconAppointment = QFalconAppointment.falconAppointment;
 		query.from(falconAppointment);
 		final BooleanExpression conditionStartDate = falconAppointment.appointmentDate.between(start, end);
 		final BooleanExpression conditionEndDate = falconAppointment.appointmentDateEnd.between(start, end);
 		final BooleanExpression conditionStartEndDate = falconAppointment.appointmentDate.before(start).and(falconAppointment.appointmentDateEnd.after(end));
 		BooleanExpression conditionTimeRange = conditionStartDate.or(conditionEndDate);
 		conditionTimeRange = conditionTimeRange.or(conditionStartEndDate);
 		query.where(conditionTimeRange);
 		List<FalconAppointment> falconAppointments = query.list(falconAppointment);
 		Set<FalconPatron> patrons = new HashSet<>();
 		for(FalconAppointment appointment: falconAppointments){
 			for(FalconAppointmentPatron falconAppointmentPatron : appointment.getFalconAppointmentPatrons()){
 				patrons.add(falconAppointmentPatron.getFalconPatron());
 			}
 		}
 		return patrons;
 	}
 
 	@Override
 	public List<FalconPatron> findByFalconUserNameLike(FalconUser admin,
 			String name) {
 		final JPQLQuery query = new JPAQuery(entityManager);
 		final QFalconPatron falconPatron = QFalconPatron.falconPatron;
 		final QFalconUser falconUserByPatron = QFalconUser.falconUser;
 		//final QFalconUser falconUserByAdmin = QFalconUser.falconUser;
 		query.from(falconPatron);
 		query.join(falconPatron.falconUserByPatron, falconUserByPatron).fetch();
 		//query.join(falconPatron.falconUserByAdmin, falconUserByAdmin).fetch();
 		final BooleanExpression conditionNameLike = falconUserByPatron.name.containsIgnoreCase(name);
 		final BooleanExpression conditionValidPatron = falconUserByPatron.valid.eq(true);
 		//final BooleanExpression conditionFalconUser = falconUserByAdmin.username.eq(admin.getUsername());
 		//query.where(conditionNameLike.and(conditionFalconUser));
 		query.where(conditionNameLike.and(conditionValidPatron));
 		query.orderBy(falconUserByPatron.name.asc());
 		return query.list(falconPatron);
 	}
 
 	@Override
 	public List<FalconPatron> findByFalconUserNricLike(FalconUser admin,
 			String nric) {
 		final JPQLQuery query = new JPAQuery(entityManager);
 		final QFalconPatron falconPatron = QFalconPatron.falconPatron;
 		final QFalconUser falconUserByPatron = QFalconUser.falconUser;
 		//final QFalconUser falconUserByAdmin = QFalconUser.falconUser;
 		query.from(falconPatron);
 		query.join(falconPatron.falconUserByPatron, falconUserByPatron).fetch();
 		//query.join(falconPatron.falconUserByAdmin, falconUserByAdmin).fetch();
 		final BooleanExpression conditionNameLike = falconUserByPatron.nric.containsIgnoreCase(nric);
 		final BooleanExpression conditionValidPatron = falconUserByPatron.valid.eq(true);
 		//final BooleanExpression conditionFalconUser = falconUserByAdmin.username.eq(admin.getUsername());
 		//query.where(conditionNameLike.and(conditionFalconUser));
 		query.where(conditionNameLike.and(conditionValidPatron));
 		query.orderBy(falconUserByPatron.name.asc());
 		return query.list(falconPatron);
 	}
 
 	@Override
 	public List<FalconPatron> findByFalconUserEmailLike(FalconUser admin,
 			String email) {
 		final JPQLQuery query = new JPAQuery(entityManager);
 		final QFalconPatron falconPatron = QFalconPatron.falconPatron;
 		final QFalconUser falconUserByPatron = QFalconUser.falconUser;
 		//final QFalconUser falconUserByAdmin = QFalconUser.falconUser;
 		query.from(falconPatron);
 		query.join(falconPatron.falconUserByPatron, falconUserByPatron).fetch();
 		//query.join(falconPatron.falconUserByAdmin, falconUserByAdmin).fetch();
 		final BooleanExpression conditionNameLike = falconUserByPatron.email.containsIgnoreCase(email);
 		final BooleanExpression conditionValidPatron = falconUserByPatron.valid.eq(true);
 		//final BooleanExpression conditionFalconUser = falconUserByAdmin.username.eq(admin.getUsername());
 		//query.where(conditionNameLike.and(conditionFalconUser));
 		query.where(conditionNameLike.and(conditionValidPatron));
 		query.orderBy(falconUserByPatron.name.asc());
 		return query.list(falconPatron);
 	}
 
 	@Override
 	public List<FalconPatron> findByFalconUserHpTelLike(FalconUser admin,
 			String hpTel) {
 		final JPQLQuery query = new JPAQuery(entityManager);
 		final QFalconPatron falconPatron = QFalconPatron.falconPatron;
 		final QFalconUser falconUserByPatron = QFalconUser.falconUser;
 		//final QFalconUser falconUserByAdmin = QFalconUser.falconUser;
 		query.from(falconPatron);
 		query.join(falconPatron.falconUserByPatron, falconUserByPatron).fetch();
 		//query.join(falconPatron.falconUserByAdmin, falconUserByAdmin).fetch();
 		final BooleanExpression conditionNameLike = falconUserByPatron.phone.containsIgnoreCase(hpTel);
 		final BooleanExpression conditionValidPatron = falconUserByPatron.valid.eq(true);
 		//final BooleanExpression conditionFalconUser = falconUserByAdmin.username.eq(admin.getUsername());
 		//query.where(conditionNameLike.and(conditionFalconUser));
 		query.where(conditionNameLike.and(conditionValidPatron));
 		query.orderBy(falconUserByPatron.name.asc());
 		return query.list(falconPatron);
 	}
 
 	@Override
 	public List<FalconPatron> findByFalconUserPatronLike(FalconUser admin,
 			FalconUser patron) {
 		final JPQLQuery query = new JPAQuery(entityManager);
 		final QFalconPatron falconPatron = QFalconPatron.falconPatron;
 		final QFalconUser falconUserByPatron = QFalconUser.falconUser;
 		//final QFalconUser falconUserByAdmin = QFalconUser.falconUser;
 		query.from(falconPatron);
 		query.join(falconPatron.falconUserByPatron, falconUserByPatron).fetch();
 		//query.join(falconPatron.falconUserByAdmin, falconUserByAdmin).fetch();
 		BooleanExpression conditionPatronLike = null;
 		if(StringUtils.isNotBlank(patron.getPhone())){
 			conditionPatronLike = falconUserByPatron.phone.eq(patron.getPhone());
 		} else if(StringUtils.isNotBlank(patron.getEmail())){
 			conditionPatronLike = falconUserByPatron.email.eq(patron.getEmail());
 		} else if(StringUtils.isNotBlank(patron.getNric())){
 			conditionPatronLike = falconUserByPatron.nric.eq(patron.getNric());
 		} else if(StringUtils.isNotBlank(patron.getUsername())){
 			conditionPatronLike = falconUserByPatron.username.eq(patron.getUsername());
 		}
 		final BooleanExpression conditionValidPatron = falconUserByPatron.valid.eq(true);
 		query.where(conditionPatronLike.and(conditionValidPatron));
 		query.orderBy(falconUserByPatron.name.asc());
 		return query.list(falconPatron);
 	}
 
 	
 }
