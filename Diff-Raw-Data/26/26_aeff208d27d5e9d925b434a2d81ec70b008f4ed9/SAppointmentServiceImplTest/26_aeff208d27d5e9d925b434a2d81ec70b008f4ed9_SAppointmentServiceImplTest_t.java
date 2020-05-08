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
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import org.agatom.springatom.AbstractCarTestCase;
 import org.agatom.springatom.model.beans.appointment.SAppointment;
 import org.agatom.springatom.model.beans.appointment.SAppointmentTask;
 import org.agatom.springatom.model.beans.car.SCar;
 import org.agatom.springatom.model.beans.person.embeddable.SPersonalInformation;
 import org.agatom.springatom.model.beans.person.mechanic.SMechanic;
 import org.agatom.springatom.model.dto.SAppointmentTaskDTO;
 import org.agatom.springatom.model.types.meta.SMetaDataEnum;
 import org.agatom.springatom.mvc.model.service.SAppointmentService;
 import org.agatom.springatom.mvc.model.service.SMechanicService;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 import org.joda.time.Interval;
 import org.joda.time.ReadableInterval;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.FixMethodOrder;
 import org.junit.Test;
 import org.junit.runners.MethodSorters;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 
 import javax.annotation.Nullable;
 import javax.validation.ConstraintViolationException;
 import java.util.*;
 
 import static org.hamcrest.core.Is.is;
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 
 @FixMethodOrder(value = MethodSorters.NAME_ASCENDING)
 public class SAppointmentServiceImplTest
         extends AbstractCarTestCase {
     public static final  int                                     INT         = 20;
     private static final ArrayList<MockAppointment>              MOCKED_APP  = new ArrayList<>();
     private static       Map<Long, Collection<SAppointmentTask>> APP_TO_TASK = new HashMap<>();
     private static       List<SAppointment>                      APPS        = new ArrayList<>();
     private static SMechanic MECHANIC;
     @Autowired
     SAppointmentService appointmentService;
     @Autowired
     SMechanicService    mechanicService;
 
     @Before
     @Override
     public void setUp() throws Exception {
         super.setUp();
         if (SAppointmentServiceImplTest.MECHANIC == null) {
             SPersonalInformation personalInformation = new SPersonalInformation();
             personalInformation.setFirstName("John");
             personalInformation.setLastName("Doe");
 
             SMechanic sMechanic = new SMechanic();
             sMechanic.setInformation(personalInformation);
             sMechanic.setPrimaryMail("john.doe@gmail.com");
 
             SMechanic newMechanic = this.mechanicService.save(sMechanic);
 
 
             Assert.assertNotNull("newMechanic is null", newMechanic);
             Assert.assertNotNull("newMechanic#id is null", newMechanic.getId());
             SAppointmentServiceImplTest.MECHANIC = newMechanic;
         }
         if (SAppointmentServiceImplTest.MOCKED_APP.isEmpty()) {
             final Map<Long, SCar> map = new HashMap<>();
             final List<SCar> list = new ArrayList<>();
 
             int it = 0;
             for (final MockCar mockedCar : MOCK_CARS) {
                final String licencePlate = mockedCar.licencePlate;
                final String vinNumber = mockedCar.vinNumber;
                 final Long ownerId = C_1.getId();
                 list.add(this.carService
                         .newCar(mockedCar.mockMaster.brand, mockedCar.mockMaster.model, licencePlate, vinNumber, ownerId));
             }
 
             Assert.assertTrue(list.size() == MOCKED_MASTERS.size());
             for (final SCar sCar : list) {
                 Assert.assertNotNull(sCar);
                 Assert.assertFalse(sCar.isNew());
                 Assert.assertNotNull(sCar.getId());
                 map.put(sCar.getId(), sCar);
             }
 
             SCarServiceImplTest.MOCKED_CARS = map;
 
             for (final Long mockCarId : MOCKED_CARS.keySet()) {
                 it = 1;
                 final List<SAppointmentTaskDTO> tasks = new ArrayList<>(INT);
 
                 for (int i = 0 ; i < INT ; i++) {
                     tasks.add(new SAppointmentTaskDTO(
                             SMetaDataEnum.SAT_NORMAL,
                             String.format("66666666666666_Normal_%d", new Random().nextInt(it++))));
                     tasks.add(new SAppointmentTaskDTO(
                             SMetaDataEnum.SAT_OIL_CHANGE,
                             String.format("66666666666666_OilChange_%d", new Random().nextInt(it++))));
                     tasks.add(new SAppointmentTaskDTO(
                             SMetaDataEnum.SAT_REPAIR,
                             String.format("6666666666666666_Repair_%d", new Random().nextInt(it++))));
                 }
 
                 MOCKED_APP.add(new MockAppointment(
                         new Interval(DateTime.now(), DateTime.now().plusDays(1)),
                         mockCarId,
                         MECHANIC.getId(),
                         tasks.toArray(new SAppointmentTaskDTO[INT])
                 ));
             }
         }
     }
 
     @Test(expected = ConstraintViolationException.class)
     public void test_A_NewAppointmentWithoutTaskExpectingException() throws Exception {
         for (final MockAppointment mockAppointment : MOCKED_APP) {
             this.appointmentService.newAppointment(
                     mockAppointment.interval,
                     mockAppointment.carId,
                     mockAppointment.assigneeId,
                     mockAppointment.assigneeId
             );
         }
     }
 
     @Test
     public void test_B_NewAppointmentWithTasks() throws Exception {
         final List<SAppointment> tmpApps = new ArrayList<>();
         for (final MockAppointment mockAppointment : MOCKED_APP) {
             final SAppointment appointment = this.appointmentService
                     .withFullLoad(this.appointmentService.newAppointment(
                             mockAppointment.interval,
                             mockAppointment.carId,
                             mockAppointment.assigneeId,
                             mockAppointment.assigneeId,
                             mockAppointment.tasks
                     ));
             Assert.assertNotNull(appointment);
             Assert.assertFalse(appointment.isNew());
             Assert.assertNotNull(appointment.getId());
             Assert.assertNotNull(appointment.getTasks());
             for (final SAppointmentTask task : appointment.getTasks()) {
                 Assert.assertNotNull(task);
                 Assert.assertFalse(task.isNew());
                 Assert.assertNotNull(task.getId());
                 Assert.assertNotNull(task.getAppointment());
             }
             tmpApps.add(appointment);
         }
         Assert.assertThat(tmpApps.size(), is(MOCKED_APP.size()));
         SAppointmentServiceImplTest.APPS = tmpApps;
     }
 
     @Test
     public void test_C_addTask() throws Exception {
         for (final SAppointment mockAppointment : APPS) {
             final int before = mockAppointment.getTasks().size();
             final SAppointment appointment = this.appointmentService
                     .withFullLoad(this.appointmentService
                             .addTask(mockAppointment.getId(), new SAppointmentTaskDTO(
                                     SMetaDataEnum.SAT_NORMAL,
                                     String.format("Adding task for app=%d", mockAppointment.getId()))));
             final int after = appointment.getTasks().size();
             Assert.assertTrue(before < after);
         }
     }
 
     @Test
     public void test_D_findTasks() throws Exception {
         final Map<Long, Collection<SAppointmentTask>> map = Maps.newHashMap();
         for (final SAppointment mockAppointment : APPS) {
             final List<SAppointmentTask> tasks = this.appointmentService
                     .findTasks(mockAppointment.getId());
             Assert.assertNotNull(tasks);
             Assert.assertFalse(tasks.isEmpty());
             for (final SAppointmentTask task : tasks) {
                 Assert.assertNotNull(task);
                 Assert.assertFalse(task.isNew());
                 Assert.assertNotNull(task.getId());
             }
             map.put(mockAppointment.getId(), tasks);
         }
         SAppointmentServiceImplTest.APP_TO_TASK = map;
     }
 
     @Test
     public void test_F_removeTask() throws Exception {
         final List<Long> toRemove = Lists.newArrayList();
         for (Map.Entry<Long, Collection<SAppointmentTask>> entry : APP_TO_TASK.entrySet()) {
             final Long appointmentId = entry.getKey();
             if (appointmentId % 2 == 0) {
                 final List<SAppointmentTask> beforeTask = this.appointmentService.findTasks(appointmentId);
                 final int beforeTaskSize = beforeTask.size();
 
                 final SAppointment appointment = this.appointmentService
                         .withFullLoad(this.appointmentService
                                 .removeTask(appointmentId, this.extractIDS(entry.getValue()))
                         );
 
                 final int afterTaskSize = appointment.getTasks().size();
 
                 Assert.assertTrue(beforeTaskSize > afterTaskSize);
                 Assert.assertTrue(appointment.getTasks().isEmpty());
                 toRemove.add(appointmentId);
             }
         }
         for (final Long taskId : toRemove) {
             APP_TO_TASK.remove(taskId);
         }
     }
 
     @Test
     public void test_G_1_findAll() throws Exception {
         Assert.assertTrue(this.appointmentService.findAll().size() == APPS.size());
     }
 
     @Test
     public void test_G_2_findAllPageable() throws Exception {
         final int appsCount = APPS.size();
         final int pages = 2;
         final int pageSize = Math.round(appsCount / pages);
         for (int page = 0 ; page < pages ; page++) {
             final Page<SAppointment> appointmentPage = this.appointmentService.findAll(new PageRequest(page, pageSize));
             Assert.assertNotNull(appointmentPage);
             Assert.assertTrue(appointmentPage.getNumber() == page);
             Assert.assertTrue(appointmentPage.getSize() <= pageSize);
         }
     }
 
     @Test
     public void test_G_3_findByTask() throws Exception {
         for (final Map.Entry<Long, Collection<SAppointmentTask>> entry : APP_TO_TASK.entrySet()) {
             final Collection<Long> ids = Collections2
                     .transform(entry.getValue(), new Function<SAppointmentTask, Long>() {
                         @Nullable
                         @Override
                         public Long apply(
                                 @Nullable
                                 final
                                 SAppointmentTask input) {
                             assert input != null;
                             return input.getId();
                         }
                     });
 
             final long[] arrayOfIds = new long[ids.size()];
             int it = 0;
             for (final Long id : ids) {
                 arrayOfIds[it++] = id;
             }
 
             final SAppointment appointment = this.appointmentService.findByTask(arrayOfIds);
             Assert.assertNotNull(appointment);
             Assert.assertNotNull(appointment.getId());
             Assert.assertFalse(appointment.isNew());
             Assert.assertTrue(appointment.getId().equals(entry.getKey()));
         }
     }
 
     @Test
     public void test_H_1_1_findEarlier_NoResults() throws Exception {
         for (final Long ids : APP_TO_TASK.keySet()) {
             final SAppointment mockAppointment = this.appointmentService.findOne(ids);
             final List<SAppointment> list = this.appointmentService
                     .findEarlier(mockAppointment.getBegin().minusDays(1));
             Assert.assertTrue(list.isEmpty());
         }
     }
 
     @Test
     public void test_H_1_2_findEarlier_Results() throws Exception {
         for (final Long ids : APP_TO_TASK.keySet()) {
             final SAppointment mockAppointment = this.appointmentService.findOne(ids);
             final List<SAppointment> list = this.appointmentService
                     .findEarlier(mockAppointment.getBegin().plusDays(1));
             Assert.assertFalse(list.isEmpty());
         }
     }
 
     @Test
     public void test_H_2_1_findLater_NoResults() throws Exception {
         for (final Long ids : APP_TO_TASK.keySet()) {
             final SAppointment mockAppointment = this.appointmentService.findOne(ids);
             final List<SAppointment> list = this.appointmentService
                     .findLater(mockAppointment.getEnd().plusDays(1));
             Assert.assertTrue(list.isEmpty());
         }
     }
 
     @Test
     public void test_H_2_2_findLater_Results() throws Exception {
         for (final Long ids : APP_TO_TASK.keySet()) {
             final SAppointment mockAppointment = this.appointmentService.findOne(ids);
             final List<SAppointment> list = this.appointmentService
                     .findLater(mockAppointment.getEnd().minusDays(1));
             Assert.assertFalse(list.isEmpty());
         }
     }
 
     @Test
     public void test_H_3_findBetween() throws Exception {
         for (final Long ids : APP_TO_TASK.keySet()) {
             final SAppointment mockAppointment = this.appointmentService.findOne(ids);
             final List<SAppointment> list = this.appointmentService
                     .findBetween(mockAppointment.getBegin().minusHours(1), mockAppointment.getEnd().plusHours(1));
             Assert.assertFalse(list.isEmpty());
         }
     }
 
     @Test
     public void test_I_1_postponeToFuture() throws Exception {
         for (final Long ids : APP_TO_TASK.keySet()) {
             final SAppointment mockAppointment = this.appointmentService.findOne(ids);
             final Duration durationBefore = mockAppointment.getInterval().toDuration();
 
             final SAppointment appointment = this.appointmentService.postponeToFuture(
                     mockAppointment.getId(),
                     Duration.standardHours(1));
             Assert.assertNotNull(appointment);
             Assert.assertTrue(appointment.getBegin().isAfter(mockAppointment.getBegin()));
             Assert.assertTrue(appointment.getEnd().isAfter(mockAppointment.getEnd()));
 
             final Duration durationAfter = appointment.getInterval().toDuration();
             Assert.assertTrue(durationBefore.equals(durationAfter));
         }
     }
 
     @Test
     public void test_I_2_postponeToPast() throws Exception {
         for (final Long ids : APP_TO_TASK.keySet()) {
             final SAppointment mockAppointment = this.appointmentService.findOne(ids);
             final Duration durationBefore = mockAppointment.getInterval().toDuration();
 
             final SAppointment appointment = this.appointmentService.postponeToPast(
                     mockAppointment.getId(),
                     Duration.standardHours(1));
             Assert.assertNotNull(appointment);
             Assert.assertTrue(appointment.getBegin().isBefore(mockAppointment.getBegin()));
             Assert.assertTrue(appointment.getEnd().isBefore(mockAppointment.getEnd()));
 
             final Duration durationAfter = appointment.getInterval().toDuration();
             Assert.assertTrue(durationBefore.equals(durationAfter));
         }
     }
 
     @Test
     public void test_J_findByCar() throws Exception {
         for (final Long carId : MOCKED_CARS.keySet()) {
             final List<SAppointment> appointments = this.appointmentService
                     .withFullLoad(
                             this.appointmentService
                                     .findByCar(carId)
                     );
             Assert.assertNotNull(appointments);
             Assert.assertTrue(!appointments.isEmpty());
             for (final SAppointment appointment : appointments) {
                 Assert.assertNotNull(appointment);
                 Assert.assertNotNull(appointment.getId());
                 Assert.assertFalse(appointment.isNew());
                 Assert.assertNotNull(appointment.getCar());
                 Assert.assertTrue(appointment.getCar().getId().equals(carId));
             }
         }
     }
 
     @Test
     public void test_K_DeleteAll() throws Exception {
         this.appointmentService.deleteAll();
         final List<SAppointment> sAppointments = this.appointmentService.findAll();
         Assert.assertTrue(sAppointments.isEmpty());
         for (final Long entry : APP_TO_TASK.keySet()) {
             final List<SAppointmentTask> tasks = this.appointmentService.findTasks(entry);
             Assert.assertTrue(tasks.isEmpty());
         }
     }
 
     protected class MockAppointment {
         final ReadableInterval      interval;
         final Long                  carId;
         final Long                  assigneeId;
         final SAppointmentTaskDTO[] tasks;
 
         public MockAppointment(final ReadableInterval interval,
                                final Long carId,
                                final Long assigneeId,
                                final SAppointmentTaskDTO... tasks) {
             this.interval = interval;
             this.carId = carId;
             this.assigneeId = assigneeId;
             this.tasks = tasks;
         }
     }
 }
