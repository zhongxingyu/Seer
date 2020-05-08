 /**************************************************************************************************
  * This file is part of [SpringAtom] Copyright [kornicameister@gmail.com][2013]                   *
  *                                                                                                *
  * [SpringAtom] is free software: you can redistribute it and/or modify                           *
  * it under the terms of the GNU General Public License as published by                           *
  * the Free Software Foundation, either version 3 of the License, or                              *
  * (at your option) any later version.                                                            *
  *                                                                                                *
  * [SpringAtom] is distributed in the hope that it will be useful,                                *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of                                 *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the                                  *
  * GNU General Public License for more details.                                                   *
  *                                                                                                *
  * You should have received a copy of the GNU General Public License                              *
  * along with [SpringAtom].  If not, see <http://www.gnu.org/licenses/gpl.html>.                  *
  **************************************************************************************************/
 
 package org.agatom.springatom.server.service.form.impl;
 
 import com.google.common.collect.Lists;
 import com.mysema.query.types.expr.BooleanExpression;
 import com.mysema.query.types.path.EnumPath;
 import org.agatom.springatom.server.model.beans.appointment.SAppointment;
 import org.agatom.springatom.server.model.beans.appointment.SAppointmentTask;
 import org.agatom.springatom.server.model.beans.car.SCar;
 import org.agatom.springatom.server.model.beans.user.QSUser;
 import org.agatom.springatom.server.model.beans.user.SUser;
 import org.agatom.springatom.server.model.beans.user.authority.SAuthority;
 import org.agatom.springatom.server.model.types.user.SRole;
 import org.agatom.springatom.server.repository.repositories.user.SUserRepository;
 import org.agatom.springatom.server.service.domain.SCarService;
 import org.agatom.springatom.server.service.form.SAppointmentWizardService;
 import org.agatom.springatom.server.service.support.exceptions.ServiceException;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.data.domain.Persistable;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Isolation;
 import org.springframework.transaction.annotation.Propagation;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.Assert;
 import org.springframework.webflow.action.EventFactorySupport;
 import org.springframework.webflow.execution.Event;
 
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 @Service(value = "SAppointmentFormService")
 @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE, propagation = Propagation.REQUIRES_NEW)
 public class SAppointmentWizardServiceImpl
         implements SAppointmentWizardService {
 
     @Qualifier("UserRepo") @Autowired
     private SUserRepository userRepository;
     @Autowired
     private SCarService     carService;
 
     @Override
     public List<SUser> getReporters() throws ServiceException {
         final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         final Object principal = authentication.getPrincipal();
         if (principal instanceof SUser) {
             final SUser sUser = (SUser) principal;
             if (!sUser.hasAuthority(SAuthority.fromRole(SRole.ROLE_BOSS))) {
 
                 final EnumPath<SRole> role = QSUser.sUser.roles.any().pk.authority.role;
                 final BooleanExpression userId = QSUser.sUser.id.ne(sUser.getId());
                 final BooleanExpression predicate = role.eq(SRole.ROLE_MECHANIC).and(userId);
 
                 return Collections.unmodifiableList(Lists.newArrayList(this.userRepository.findAll(predicate)));
             } else {
                 return Collections.unmodifiableList(Collections.singletonList(sUser));
             }
         }
         throw new SAppointmentFormServiceException(SUser.class, String.format("Principal\n\t[%s]\nis not authenticated", principal));
     }
 
     @Override
     public List<SUser> getAssignees() throws ServiceException {
         return this.getReporters();
     }
 
     @Override
     public List<SCar> getCars() {
         return Collections.unmodifiableList(this.carService.findAll());
     }
 
     @Override
     public SAppointment getNewAppointment(final boolean withTasks) {
         return this.getNewAppointment(withTasks ? 1 : 0);
     }
 
     @Override
     public SAppointment getNewAppointment(@Min(value = 0) final int taskCounts) {
         return new SAppointment().addTask(Lists.<SAppointmentTask>newArrayListWithExpectedSize(taskCounts));
     }
 
     @Override
     public Event addNewTask(@NotNull final SAppointment appointment, final Event event) {
         Assert.notNull(appointment);
         Assert.notNull(event);
         final String type = event.getAttributes().getRequiredString("type");
         final String task = event.getAttributes().getRequiredString("task");
         if (type == null || task == null) {
             return new EventFactorySupport().success(appointment);
         }
         if (type.isEmpty() || task.isEmpty()) {
             return new EventFactorySupport().success(appointment);
         }
         return this.addNewTask(appointment, new SAppointmentTask().setType(type).setTask(task));
     }
 
     @Override
     public Event addNewTask(@NotNull final SAppointment appointment, @NotNull final SAppointmentTask task) {
         appointment.addTask(Collections.singletonList(task));
         return new EventFactorySupport().success(task);
     }
 
     @Override
     public SAppointment removeTask(@NotNull final SAppointment appointment, final String pos) {
         final Integer position = Integer.valueOf(pos);
         final List<SAppointmentTask> tasks = appointment.getTasks();
         if (position < 0 || position == tasks.size()) {
             return appointment;
         }
         return appointment.removeTask(tasks.get(position));
     }
 
     protected class SAppointmentFormServiceException
             extends ServiceException {
         private static final long serialVersionUID = -2721658358817267557L;
 
         public SAppointmentFormServiceException(final Class<? extends Persistable> target, final String message) {
             super(target, message);
         }
     }
 }
