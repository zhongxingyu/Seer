 /*
  * Copyright 2014, MyCellar
  *
  * This file is part of MyCellar.
  *
  * MyCellar is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * MyCellar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with MyCellar. If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.mycellar.interfaces.web.services.admin;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.inject.Named;
 import javax.inject.Singleton;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 
 import org.slf4j.LoggerFactory;
 
 import ch.qos.logback.classic.Level;
 import ch.qos.logback.classic.Logger;
 import ch.qos.logback.classic.LoggerContext;
 import fr.mycellar.domain.shared.exception.BusinessException;
 
 /**
  * @author speralta
  */
 @Named
 @Singleton
 @Path("/admin/loggers")
 public class LoggerWebService {
 
     @POST
     @Consumes(MediaType.APPLICATION_JSON)
     @Path("changeLevel")
     public void changeLevel(LoggerDto loggerDto) throws BusinessException {
         LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
         loggerContext.getLogger(loggerDto.getName()).setLevel(Level.toLevel(loggerDto.getLevel(), Level.INFO));
     }
 
     @GET
     @Produces(MediaType.APPLICATION_JSON)
     @Path("loggers")
     public List<LoggerDto> getLoggers() {
         LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
 
         List<LoggerDto> loggers = new ArrayList<LoggerDto>();
         for (Logger logger : context.getLoggerList()) {
             loggers.add(new LoggerDto(logger));
         }
         return loggers;
     }
 }
