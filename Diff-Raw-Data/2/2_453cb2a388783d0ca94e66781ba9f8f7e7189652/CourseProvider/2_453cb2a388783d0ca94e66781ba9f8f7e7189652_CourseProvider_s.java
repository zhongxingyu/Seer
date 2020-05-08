 /*
  * Copyright (c) 2013, Blackboard, Inc. All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  * 
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  * disclaimer. 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided with the distribution. 3. Neither the
  * name of the Blackboard Inc. nor the names of its contributors may be used to endorse or promote products derived from
  * this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  * 
  * BLACKBOARD MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE SOFTWARE, EITHER EXPRESS OR IMPLIED,
  * INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR NON-
  * INFRINGEMENT. BLACKBOARD SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
  * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
  */
 package blackboard.plugin.hayabusa.provider;
 
 import java.util.Set;
 
 import blackboard.base.BbList;
 import blackboard.data.course.Course;
 import blackboard.data.course.Course.ServiceLevel;
 import blackboard.data.user.User;
 import blackboard.persist.PersistenceException;
 import blackboard.persist.PersistenceRuntimeException;
 import blackboard.persist.course.CourseDbLoader;
 import blackboard.platform.context.ContextManagerFactory;
 import blackboard.platform.security.SystemRole;
 import blackboard.plugin.hayabusa.command.*;
 
 import com.google.common.collect.Sets;
 
 /**
  * A {@link Provider} for courses
  * 
  * @author Li Guoyu
  * @since 1.0
  */
 @SuppressWarnings( "deprecation" )
 public class CourseProvider implements Provider
 {
   private static final String ADMIN_COURSE_URL_TEMPLATE = "/webapps/blackboard/execute/courseMain?course_id=%s";
   private static final String My_COURSE_URL_TEMPLATE = "/webapps/blackboard/execute/launcher?type=Course?id=%s";
 
   @Override
   public Iterable<Command> getCommands()
   {
     User currentUser = ContextManagerFactory.getInstance().getContext().getUser();
    if ( currentUser.getSystemRole().equals( SystemRole.Ident.AccountAdmin ) )
     {
       return getAdminCourses();
     }
     else
     {
       return getMyCourses( currentUser );
     }
   }
 
   private Iterable<Command> getAdminCourses()
   {
     try
     {
       BbList<Course> courses = CourseDbLoader.Default.getInstance().loadAllByServiceLevel( ServiceLevel.FULL );
       Set<Command> commands = Sets.newTreeSet();
       for ( Course course : courses )
       {
         String url = String.format( ADMIN_COURSE_URL_TEMPLATE, course.getId().toExternalString() );
         SimpleCommand command = new SimpleCommand( course.getTitle(), url, Category.COURSE );
         commands.add( command );
       }
       return commands;
 
     }
     catch ( PersistenceException e )
     {
       throw new PersistenceRuntimeException( e );
     }
   }
 
   private Iterable<Command> getMyCourses( User user )
   {
     try
     {
       BbList<Course> courses = CourseDbLoader.Default.getInstance().loadByUserId( user.getId() );
       Set<Command> commands = Sets.newTreeSet();
       for ( Course course : courses )
       {
         String url = String.format( My_COURSE_URL_TEMPLATE, course.getId().toExternalString() );
         SimpleCommand command = new SimpleCommand( course.getTitle(), url, Category.COURSE );
         commands.add( command );
       }
       return commands;
     }
     catch ( PersistenceException e )
     {
       throw new PersistenceRuntimeException( e );
     }
   }
 
 }
