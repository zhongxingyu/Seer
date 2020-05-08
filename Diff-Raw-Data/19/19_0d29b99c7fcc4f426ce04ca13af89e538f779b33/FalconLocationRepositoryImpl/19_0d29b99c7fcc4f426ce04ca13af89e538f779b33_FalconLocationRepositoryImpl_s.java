 package com.twistlet.falcon.model.repository;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.apache.commons.lang3.time.DateUtils;
 import org.springframework.stereotype.Repository;
 
 import com.mysema.query.jpa.JPQLQuery;
 import com.mysema.query.jpa.impl.JPAQuery;
 import com.mysema.query.types.expr.BooleanExpression;
 import com.twistlet.falcon.model.entity.FalconAppointment;
 import com.twistlet.falcon.model.entity.FalconLocation;
 import com.twistlet.falcon.model.entity.FalconUser;
 import com.twistlet.falcon.model.entity.QFalconAppointment;
 import com.twistlet.falcon.model.entity.QFalconLocation;
 import com.twistlet.falcon.model.entity.QFalconUser;
 
 @Repository
 public class FalconLocationRepositoryImpl implements FalconLocationRepositoryCustom {
 	
 	@PersistenceContext
 	EntityManager entityManager;
 
 	@Override
 	public Set<FalconLocation> findLocationDateRange(FalconUser admin, Date start, Date end) {
 		final JPQLQuery query = new JPAQuery(entityManager);
 		final QFalconAppointment falconAppointment = QFalconAppointment.falconAppointment;
 		query.from(falconAppointment);
 		start = DateUtils.addSeconds(start, 1);
 		end = DateUtils.addSeconds(end, -1);
 		final BooleanExpression conditionStartDate = falconAppointment.appointmentDate.between(start, end);
 		final BooleanExpression conditionEndDate = falconAppointment.appointmentDateEnd.between(start, end);
 		final BooleanExpression conditionStartEndDate = falconAppointment.appointmentDate.before(start).and(falconAppointment.appointmentDateEnd.after(end));
 		BooleanExpression conditionTimeRange = conditionStartDate.or(conditionEndDate);
 		conditionTimeRange = conditionTimeRange.or(conditionStartEndDate);
 		query.where(conditionTimeRange);
 		List<FalconAppointment> falconAppointments = query.list(falconAppointment);
 		Set<FalconLocation> locations = new HashSet<>();
 		for(FalconAppointment appointment: falconAppointments){
 			locations.add(appointment.getFalconLocation());
 		}
 		return locations;
 	}
 
 	@Override
 	public List<FalconLocation> findByFalconUserLike(FalconLocation location) {
 		final JPQLQuery query = new JPAQuery(entityManager);
 		final QFalconLocation falconLocation = QFalconLocation.falconLocation;
 		final QFalconUser falconUser = QFalconUser.falconUser;
 		query.from(falconLocation);
 		query.join(falconLocation.falconUser, falconUser).fetch();
 		BooleanExpression conditionValid = falconLocation.valid.isTrue();
 		BooleanExpression conditionAdmin = falconUser.username.eq(location.getFalconUser().getUsername());
 		BooleanExpression conditionNameLike = falconLocation.name.containsIgnoreCase(location.getName());
 		query.where(conditionValid.and(conditionAdmin.and(conditionNameLike)));
 		return query.list(falconLocation);
 	}
 	
 	public Set<FalconLocation> findLocationDateRange(FalconUser admin, Date start, Date end, Integer appointmentId) {
 		final JPQLQuery query = new JPAQuery(entityManager);
 		final QFalconAppointment falconAppointment = QFalconAppointment.falconAppointment;
 		query.from(falconAppointment);
 		start = DateUtils.addSeconds(start, 1);
 		end = DateUtils.addSeconds(end, -1);
 		final BooleanExpression conditionDontSelectCurrent = falconAppointment.id.ne(appointmentId);
 		final BooleanExpression conditionStartDate = falconAppointment.appointmentDate.between(start, end);
 		final BooleanExpression conditionEndDate = falconAppointment.appointmentDateEnd.between(start, end);
 		final BooleanExpression conditionStartEndDate = falconAppointment.appointmentDate.before(start).and(falconAppointment.appointmentDateEnd.after(end));
 		BooleanExpression conditionTimeRange = conditionStartDate.or(conditionEndDate);
 		conditionTimeRange = conditionTimeRange.or(conditionStartEndDate);
 		query.where(conditionTimeRange.and(conditionDontSelectCurrent));
 		List<FalconAppointment> falconAppointments = query.list(falconAppointment);
 		Set<FalconLocation> locations = new HashSet<>();
 		for(FalconAppointment appointment: falconAppointments){
 			locations.add(appointment.getFalconLocation());
 		}
 		return locations;
 	}
 
 }
