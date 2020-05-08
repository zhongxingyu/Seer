 package com.twistlet.falcon.model.repository;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
 import org.springframework.stereotype.Repository;
 
 import com.mysema.query.jpa.JPQLQuery;
 import com.mysema.query.jpa.impl.JPAQuery;
 import com.mysema.query.types.expr.BooleanExpression;
 import com.twistlet.falcon.model.entity.FalconAppointment;
 import com.twistlet.falcon.model.entity.FalconStaff;
 import com.twistlet.falcon.model.entity.FalconUser;
 import com.twistlet.falcon.model.entity.QFalconAppointment;
 import com.twistlet.falcon.model.entity.QFalconStaff;
 import com.twistlet.falcon.model.entity.QFalconUser;
 
 @Repository
 public class FalconStaffRepositoryImpl implements FalconStaffRepositoryCustom {
 
 
 	@PersistenceContext
 	EntityManager entityManager;
 
 	@Override
 	public List<FalconStaff> findByFalconUserNameLike(FalconUser admin, String name) {
 		final JPQLQuery query = new JPAQuery(entityManager);
 		final QFalconStaff falconStaff = QFalconStaff.falconStaff;
 		final QFalconUser falconUser = QFalconUser.falconUser;
 		query.from(falconStaff);
 		query.join(falconStaff.falconUser, falconUser).fetch();
 		final BooleanExpression conditionNameLike = falconStaff.name.containsIgnoreCase(name);
 		final BooleanExpression conditionFalconUser = falconStaff.falconUser.username.eq(admin.getUsername());
 		query.where(conditionFalconUser.and(conditionNameLike).and(falconStaff.valid.eq(true)));
 		query.orderBy(falconStaff.name.asc());
 		return query.list(falconStaff);
 	}
 
 	@Override
 	public List<FalconStaff> findByFalconUserNricLike(FalconUser admin, String nric) {
 		final JPQLQuery query = new JPAQuery(entityManager);
 		final QFalconStaff falconStaff = QFalconStaff.falconStaff;
 		final QFalconUser falconUser = QFalconUser.falconUser;
 		query.from(falconStaff);
 		query.join(falconStaff.falconUser, falconUser).fetch();
 		final BooleanExpression conditionNricLike = falconStaff.nric.containsIgnoreCase(nric);
 		final BooleanExpression conditionFalconUser = falconStaff.falconUser.username.eq(admin.getUsername());
 		query.where(conditionFalconUser.and(conditionNricLike).and(falconStaff.valid.eq(true)));
 		query.orderBy(falconStaff.name.asc());
 		List<FalconStaff> staffs = query.list(falconStaff);
 		return staffs;
 	}
 
 	@Override
 	public List<FalconStaff> findByFalconUserEmailLike(FalconUser admin, String email) {
 		final JPQLQuery query = new JPAQuery(entityManager);
 		final QFalconStaff falconStaff = QFalconStaff.falconStaff;
 		final QFalconUser falconUser = QFalconUser.falconUser;
 		query.from(falconStaff);
 		query.join(falconStaff.falconUser, falconUser).fetch();
 		final BooleanExpression conditionNricLike = falconStaff.email.containsIgnoreCase(email);
 		final BooleanExpression conditionFalconUser = falconStaff.falconUser.username.eq(admin.getUsername());
 		query.where(conditionFalconUser.and(conditionNricLike).and(falconStaff.valid.eq(true)));
 		query.orderBy(falconStaff.name.asc());
 		return query.list(falconStaff);
 	}
 	
 	@Override
 	public List<FalconStaff> findByFalconUserHpTelLike(FalconUser admin, String hpTel) {
 		final JPQLQuery query = new JPAQuery(entityManager);
 		final QFalconStaff falconStaff = QFalconStaff.falconStaff;
 		final QFalconUser falconUser = QFalconUser.falconUser;
 		query.from(falconStaff);
 		query.join(falconStaff.falconUser, falconUser).fetch();
 		final BooleanExpression conditionMobileLike = falconStaff.hpTel.containsIgnoreCase(hpTel);
 		final BooleanExpression conditionFalconUser = falconStaff.falconUser.username.eq(admin.getUsername());
 		query.where(conditionFalconUser.and(conditionMobileLike).and(falconStaff.valid.eq(true)));
 		query.orderBy(falconStaff.name.asc());
 		return query.list(falconStaff);
 	}
 
 	@Override
 	public List<FalconStaff> findByFalconUserStaffLike(FalconUser admin,
 			FalconStaff staff) {
 		final JPQLQuery query = new JPAQuery(entityManager);
 		final QFalconStaff falconStaff = QFalconStaff.falconStaff;
 		final QFalconUser falconUser = QFalconUser.falconUser;
 		query.from(falconStaff);
 		query.join(falconStaff.falconUser, falconUser).fetch();
 		BooleanExpression conditionStaff = null;
 		if(StringUtils.isNotBlank(staff.getEmail())){
 			conditionStaff = falconStaff.email.eq(staff.getEmail());
 		}
 		if(StringUtils.isNotBlank(staff.getHpTel())){
 			if(conditionStaff == null){
 				conditionStaff = falconStaff.hpTel.eq(staff.getHpTel());
 			}else{
 				conditionStaff.and(falconStaff.hpTel.eq(staff.getHpTel()));
 			}
 		}
 		if(StringUtils.isNotBlank(staff.getName())){
 			if(conditionStaff == null){
 				conditionStaff = falconStaff.name.eq(staff.getName());
 			}else{
 				conditionStaff.and(falconStaff.name.eq(staff.getName()));
 			}
 		}
 		if(StringUtils.isNotBlank(staff.getNric())){
 			if(conditionStaff == null){
 				conditionStaff = falconStaff.nric.eq(staff.getNric());
 			}else{
 				conditionStaff.and(falconStaff.nric.eq(staff.getNric()));
 			}
 		}
 		if(admin.getUsername() == null){
 			query.where(conditionStaff.and(falconStaff.valid.eq(true)));
 		}else{
 			final BooleanExpression conditionFalconUser = falconStaff.falconUser.username.eq(admin.getUsername());
 			query.where(conditionFalconUser.and(conditionStaff).and(falconStaff.valid.eq(true)));
 		}
 		query.orderBy(falconStaff.name.asc());
 		return query.list(falconStaff);
 	}
 
 	@Override
 	public Set<FalconStaff> findStaffDateRange(FalconUser admin, Date start, Date end) {
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
 		Set<FalconStaff> staffs = new HashSet<>();
 		for(FalconAppointment appointment: falconAppointments){
 			staffs.add(appointment.getFalconStaff());
 		}
 		return staffs;
 	}
 
 }
