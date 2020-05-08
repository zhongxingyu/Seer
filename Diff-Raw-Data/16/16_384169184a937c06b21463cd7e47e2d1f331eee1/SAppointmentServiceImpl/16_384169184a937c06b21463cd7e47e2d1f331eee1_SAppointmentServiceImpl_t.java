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
 
 package org.agatom.springatom.mvc.model.service.impl;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Lists;
 import com.google.common.primitives.Longs;
 import com.mysema.query.types.expr.BooleanExpression;
 import com.mysema.query.types.path.DateTimePath;
 import org.agatom.springatom.jpa.repositories.*;
 import org.agatom.springatom.model.beans.appointment.QSAppointment;
 import org.agatom.springatom.model.beans.appointment.QSAppointmentTask;
 import org.agatom.springatom.model.beans.appointment.SAppointment;
 import org.agatom.springatom.model.beans.appointment.SAppointmentTask;
 import org.agatom.springatom.model.beans.car.QSCar;
 import org.agatom.springatom.model.beans.car.SCar;
 import org.agatom.springatom.model.beans.links.SAppointmentWorkerLink;
 import org.agatom.springatom.model.beans.meta.QSMetaData;
 import org.agatom.springatom.model.beans.meta.SAppointmentTaskType;
 import org.agatom.springatom.model.beans.person.mechanic.SMechanic;
 import org.agatom.springatom.model.beans.util.SIssueReporter;
 import org.agatom.springatom.model.dto.SAppointmentTaskDTO;
 import org.agatom.springatom.mvc.model.exceptions.SEntityDoesNotExists;
 import org.agatom.springatom.mvc.model.exceptions.SException;
 import org.agatom.springatom.mvc.model.service.SAppointmentService;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.joda.time.ReadableDuration;
 import org.joda.time.ReadableInterval;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Isolation;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.annotation.Nullable;
 import java.util.Collection;
 import java.util.List;
 
 import static com.google.common.base.Preconditions.checkArgument;
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 @Service
 @Transactional(readOnly = true, isolation = Isolation.SERIALIZABLE)
 @SuppressWarnings("SpringJavaAutowiringInspection")
 public class SAppointmentServiceImpl
         extends SBasicServiceImpl<SAppointment, Long, SAppointmentRepository>
         implements SAppointmentService {
     private static final String BAD_ARG_MSG                = "No %s provided, could not execute call";
     private static final String ERROR_MESSAGE_SD_GT_ED_MSG = "startDate must be greater than endDate";
     private static final Logger LOGGER                     = Logger.getLogger(SAppointmentServiceImpl.class);
     protected SAppointmentRepository repository;
     @Autowired
     @Qualifier("SAppointmentTaskRepository1")
     SAppointmentTaskRepository       taskRepository;
     @Autowired
     SCarRepository                   carRepository;
     @Autowired
     SMechanicRepository              mechanicRepository;
     @Autowired
     SAppointmentWorkerLinkRepository workerLinkRepository;
     @Autowired
     SMetaDataRepository              metaDataRepository;
 
     @Override
     @Autowired
     public void autoWireRepository(final SAppointmentRepository repo) {
         super.autoWireRepository(repo);
         this.repository = repo;
     }
 
     @Override
     public SAppointment withFullLoad(final SAppointment obj) {
         final long idAppointment = obj.getId();
 
         final QSAppointment qsAppointment = QSAppointment.sAppointment;
         final QSCar qsCar = QSCar.sCar;
 
         final SCar car = this.carRepository
                 .createCustomQuery()
                .from(qsCar, qsAppointment)
                .where(qsCar.id.eq(qsAppointment.car.id).and(qsAppointment.id.eq(idAppointment)))
                 .fetchAll()
                 .singleResult(qsCar);
         final Collection<SAppointmentTask> tasks = (Collection<SAppointmentTask>) this.taskRepository
                 .findAll(QSAppointmentTask.sAppointmentTask.appointment.id.eq(idAppointment));
 
         obj.setTasks(tasks);
         obj.setCar(car);
 
         return obj;
     }
 
     @Override
     public SAppointment newAppointment(final ReadableInterval interval,
                                        final long carId,
                                        final long assigneeId,
                                        final long reportedId,
                                        final SAppointmentTaskDTO... tasks) throws SException {
         checkArgument(carId > 0, String.format(BAD_ARG_MSG, "SCar#getId()"));
         checkArgument(assigneeId > 0, String.format(BAD_ARG_MSG, "SMechanic#getId()"));
 
         final SCar car = this.getCar(carId);
         final SMechanic assignee = this.getMechanic(assigneeId);
         final SMechanic reporter = this.getMechanic(reportedId);
 
         final SAppointment appointment_pre = new SAppointment().setCar(car).setInterval(interval);
         final SAppointment appointment = this.repository
                 .save(appointment_pre.addTask(this.convertTasks(tasks, appointment_pre)));
         final SAppointmentWorkerLink workerLink = this.workerLinkRepository.saveAndFlush(
                 new SAppointmentWorkerLink()
                         .setAppointment(appointment)
                         .setAssignee(assignee)
                         .setReporter(
                                 new SIssueReporter()
                                         .setAssigned(DateTime.now())
                                         .setMechanic(reporter)
                         )
         );
 
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug(String
                     .format("%s saved with %d task and assigned to %s", SAppointment.class
                             .getSimpleName(), tasks.length, workerLink));
         }
 
         return workerLink.getAppointment();
     }
 
     private SMechanic getMechanic(final long mechanicId) throws SEntityDoesNotExists {
         final SMechanic mechanic = this.mechanicRepository.findOne(mechanicId);
         if (mechanic == null) {
             throw new SEntityDoesNotExists(SMechanic.class, mechanicId);
         }
         return mechanic;
     }
 
     private SCar getCar(final long carId) throws SEntityDoesNotExists {
         final SCar car = this.carRepository.findOne(carId);
         if (car == null) {
             throw new SEntityDoesNotExists(SCar.class, carId);
         }
         return car;
     }
 
     private Collection<SAppointmentTask> convertTasks(final SAppointmentTaskDTO[] tasks, final SAppointment appointment) {
         final SMetaDataRepository metaDataRepository = this.metaDataRepository;
         return Lists
                 .transform(Lists.newArrayList(tasks), new Function<SAppointmentTaskDTO, SAppointmentTask>() {
                     @Nullable
                     @Override
                     public SAppointmentTask apply(
                             @Nullable
                             final SAppointmentTaskDTO input) {
                         assert input != null;
                         return (SAppointmentTask) new SAppointmentTask()
                                 .setTask(input.getTask())
                                 .setAppointment(appointment)
                                 .setMetaInformation((SAppointmentTaskType) metaDataRepository
                                         .findOne(QSMetaData.sMetaData.type.eq(input.getType())));
                     }
                 });
     }
 
     @Override
     @Transactional(readOnly = false, rollbackFor = {SEntityDoesNotExists.class, IllegalArgumentException.class}, isolation = Isolation.READ_COMMITTED)
     public SAppointment addTask(final long idAppointment, final SAppointmentTaskDTO... tasks) throws
             SEntityDoesNotExists {
         checkArgument(idAppointment > 0, String.format(BAD_ARG_MSG, "SAppointment#getId()"));
         checkArgument(tasks.length != 0, String.format(BAD_ARG_MSG, "SAppointmentTask#getId()"));
 
         this.taskRepository.save(this.convertTasks(tasks, this.findAppointment(idAppointment)));
         final SAppointment appointment = this.repository.findOne(idAppointment);
         appointment.setTasks((Collection<SAppointmentTask>) this.taskRepository
                 .findAll(QSAppointmentTask.sAppointmentTask.appointment.id.eq(idAppointment)));
         return appointment;
     }
 
     @Override
     public List<SAppointmentTask> findTasks(final long idAppointment) {
         final QSAppointmentTask task = QSAppointmentTask.sAppointmentTask;
         final BooleanExpression eq = task.appointment.id.eq(idAppointment);
         return (List<SAppointmentTask>) this.taskRepository.findAll(eq);
     }
 
     @Override
     @Transactional(readOnly = false, rollbackFor = {SEntityDoesNotExists.class, IllegalArgumentException.class}, isolation = Isolation.READ_COMMITTED)
     public SAppointment removeTask(final long idAppointment, final long... tasksId) throws SEntityDoesNotExists {
         checkArgument(idAppointment > 0, String.format(BAD_ARG_MSG, "SAppointment#getId()"));
         this.taskRepository.deleteInBatch(this.taskRepository
                 .findAll(
                         QSAppointmentTask
                                 .sAppointmentTask
                                 .id.
                                 in(Longs.asList(tasksId))
                 )
         );
         return this.repository.findOne(idAppointment);
     }
 
     @Override
     @Transactional(rollbackFor = IllegalArgumentException.class)
     public SAppointment findByTask(final long... tasks) {
         checkArgument(tasks.length != 0, String.format(BAD_ARG_MSG, "SAppointmentTask#getId()"));
         return this.repository.findOne(QSAppointment.sAppointment.tasks.any().id.in(Longs.asList(tasks)));
     }
 
     @Override
     public List<SAppointment> findByCar(final long carId) {
         final QSAppointment sAppointment = QSAppointment.sAppointment;
         return (List<SAppointment>) this.repository.findAll(sAppointment.car.id.eq(carId));
     }
 
     @Override
     @Transactional(rollbackFor = IllegalArgumentException.class)
     public List<SAppointment> findBetween(final DateTime startDate, final DateTime endDate) {
         checkArgument(startDate.isBefore(endDate), ERROR_MESSAGE_SD_GT_ED_MSG);
         final DateTimePath<DateTime> begin = QSAppointment.sAppointment.begin;
         final DateTimePath<DateTime> end = QSAppointment.sAppointment.end;
         return (List<SAppointment>) this.repository.findAll(begin.goe(startDate).and(end.loe(endDate)));
     }
 
     @Override
     public List<SAppointment> findLater(final DateTime dateTime) {
         final DateTimePath<DateTime> begin = QSAppointment.sAppointment.begin;
         final DateTimePath<DateTime> end = QSAppointment.sAppointment.end;
         return (List<SAppointment>) this.repository.findAll(end.goe(dateTime).and(end.gt(begin)));
     }
 
     @Override
     public List<SAppointment> findEarlier(final DateTime dateTime) {
         final DateTimePath<DateTime> begin = QSAppointment.sAppointment.begin;
         final DateTimePath<DateTime> end = QSAppointment.sAppointment.end;
         return (List<SAppointment>) this.repository.findAll(end.loe(dateTime).and(end.gt(begin)));
     }
 
     @Override
     @Transactional(readOnly = false, rollbackFor = SException.class)
     public SAppointment postponeToFuture(final long idAppointment, final ReadableDuration duration)
             throws SException {
         try {
             return this.postpone(idAppointment, duration, true);
         } catch (SAppointmentNotPostponedException e) {
             LOGGER.error("Appointment not postponed to future", e);
             throw new SException(SAppointment.class, e);
         } catch (SEntityDoesNotExists e2) {
             LOGGER.error("Appointment set pk=%d does not exists", e2);
             throw new SException(SAppointment.class, e2);
         }
     }
 
     private SAppointment postpone(final long idAppointment, final ReadableDuration duration, final boolean toFuture) throws
             SEntityDoesNotExists,
             SAppointmentNotPostponedException {
         final SAppointment appointment = this.findAppointment(idAppointment);
         if (appointment.postpone(duration, toFuture)) {
             return this.repository.saveAndFlush(appointment);
         } else {
             throw new SAppointmentNotPostponedException(idAppointment, duration, toFuture);
         }
     }
 
     @Override
     @Transactional(readOnly = false, rollbackFor = SException.class)
     public SAppointment postponeToPast(final long idAppointment, final ReadableDuration duration) throws
             SException {
         try {
             return this.postpone(idAppointment, duration, false);
         } catch (SAppointmentNotPostponedException e) {
             LOGGER.error("Appointment not postponed to past", e);
             throw new SException(SAppointment.class, e);
         } catch (SEntityDoesNotExists e2) {
             LOGGER.error("Appointment set pk=%d does not exists", e2);
             throw new SException(SAppointment.class, e2);
         }
     }
 
     private SAppointment findAppointment(final long idAppointment) throws SEntityDoesNotExists {
         final SAppointment appointment = this.repository.findOne(idAppointment);
         if (appointment == null) {
             try {
                 throw new SEntityDoesNotExists(SAppointment.class, idAppointment);
             } catch (SEntityDoesNotExists e) {
                 LOGGER.error("Appointment set pk=%d does not exists", e);
                 throw e;
             }
         }
         return appointment;
     }
 
     public class SAppointmentNotPostponedException
             extends SException {
         public SAppointmentNotPostponedException(final long idAppointment, final ReadableDuration duration, final boolean toFuture) {
             super(SAppointment.class, String
                     .format("%s=[id==%d] was not postponed[duration=%s, toFuture=%s]", SAppointment.class
                             .getSimpleName(), idAppointment, duration, toFuture));
         }
     }
 }
