 package com.twistlet.falcon.model.repository;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.springframework.stereotype.Repository;
 
 import com.mysema.query.jpa.JPQLQuery;
 import com.mysema.query.jpa.impl.JPAQuery;
 import com.mysema.query.types.expr.BooleanExpression;
 import com.twistlet.falcon.model.entity.FalconAppointment;
 import com.twistlet.falcon.model.entity.FalconAppointmentPatron;
 import com.twistlet.falcon.model.entity.FalconLocation;
 import com.twistlet.falcon.model.entity.FalconService;
 import com.twistlet.falcon.model.entity.FalconUser;
 import com.twistlet.falcon.model.entity.QFalconAppointment;
 import com.twistlet.falcon.model.entity.QFalconAppointmentPatron;
 import com.twistlet.falcon.model.entity.QFalconLocation;
 import com.twistlet.falcon.model.entity.QFalconService;
 import com.twistlet.falcon.model.entity.QFalconStaff;
 
 @Repository
 public class FalconAppointmentRepositoryImpl implements FalconAppointmentRepositoryCustom {
 	
 	@PersistenceContext
 	EntityManager entityManager;
 
 	@Override
 	public List<FalconAppointment> listFullByAppointmentDateBetween(Date start,
 			Date end) {
 		final JPQLQuery query = new JPAQuery(entityManager);
 		final QFalconAppointment falconAppointment = QFalconAppointment.falconAppointment;
 		final QFalconLocation falconLocation = QFalconLocation.falconLocation;
 		final QFalconService falconService = QFalconService.falconService;
 		final QFalconStaff falconStaff = QFalconStaff.falconStaff;
 		query.from(falconAppointment);
 		query.join(falconAppointment.falconStaff, falconStaff);
 		query.join(falconAppointment.falconLocation, falconLocation);
 		query.join(falconAppointment.falconService, falconService);
 		query.orderBy(falconAppointment.appointmentDate.desc());
 		query.where(falconAppointment.appointmentDate.between(start, end));
 		return query.list(falconAppointment);
 	}
 
 	@Override
 	public List<FalconAppointment> listAppointmentsByParam(FalconUser staff,
			FalconAppointmentPatron patron, Date start, Date end, FalconLocation location,
 			FalconService service) {
 		final JPQLQuery query = new JPAQuery(entityManager);
 		final QFalconAppointment falconAppointment = QFalconAppointment.falconAppointment;
 		final QFalconLocation falconLocation = QFalconLocation.falconLocation;
 		final QFalconService falconService = falconAppointment.falconService;
 		final QFalconStaff falconStaff = QFalconStaff.falconStaff;
 		final QFalconAppointmentPatron falconAppointmentPatron = QFalconAppointmentPatron.falconAppointmentPatron;
 		query.from(falconAppointment);
 		query.join(falconAppointment.falconStaff, falconStaff).fetch();
 		query.join(falconAppointment.falconAppointmentPatrons, falconAppointmentPatron);
 		query.join(falconAppointment.falconLocation, falconLocation).fetch();
 		List<BooleanExpression> expressions = new ArrayList<>();
 		if(start != null){
 			BooleanExpression x = falconAppointment.appointmentDate.between(start, end);
 			expressions.add(x);
 		}
 		if(staff != null){
			BooleanExpression x = falconStaff.falconUser.eq(staff);
			expressions.add(x);
		}
		if(patron != null){
			BooleanExpression x = falconAppointmentPatron.eq(patron);
 			expressions.add(x);
 		}
 		if(location != null){
			BooleanExpression x = falconAppointment.falconLocation.eq(location);
 			expressions.add(x);
 		}
 		if(service != null){
			BooleanExpression x = falconService.eq(service);
 			expressions.add(x);
 		}
 		query.where(expressions.toArray(new BooleanExpression[]{}));
 		return query.list(falconAppointment);
 	}
 
 }
