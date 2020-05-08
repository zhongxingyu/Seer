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
 
 package org.agatom.springatom.server.service.form;
 
 import org.agatom.springatom.server.model.beans.appointment.SAppointment;
 import org.agatom.springatom.server.model.beans.appointment.SAppointmentTask;
 import org.agatom.springatom.server.model.beans.car.SCar;
 import org.agatom.springatom.server.model.beans.user.SUser;
 import org.agatom.springatom.server.service.support.exceptions.ServiceException;
 import org.hibernate.validator.constraints.NotEmpty;
 import org.springframework.webflow.execution.Event;
 
 import javax.validation.constraints.Min;
 import javax.validation.constraints.NotNull;
 import java.util.List;
 
 /**
  * @author kornicameister
  * @version 0.0.1
  * @since 0.0.1
  */
 public interface SAppointmentWizardService {
     @NotEmpty List<SUser> getReporters() throws ServiceException;
 
     @NotEmpty List<SUser> getAssignees() throws ServiceException;
 
     @NotEmpty List<SCar> getCars();
 
     @NotNull SAppointment getNewAppointment(final boolean withTasks);
 
     @NotNull SAppointment getNewAppointment(@Min(value = 0) final int taskCounts);
 
     @NotNull Event addNewTask(@NotNull final SAppointment appointment, final Event event);
 
     @NotNull Event addNewTask(@NotNull final SAppointment appointment, @NotNull SAppointmentTask task);
 
    @NotNull SAppointmentTask getNewTask();

     @NotNull SAppointment removeTask(@NotNull final SAppointment appointment, final String position);
 }
