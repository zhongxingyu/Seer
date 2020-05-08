 /* Jlint Ant Task
     Copyright (C) 2012 Keita Kita
 
     This library is free software; you can redistribute it and/or
     modify it under the terms of the GNU Lesser General Public
     License as published by the Free Software Foundation; either
     version 2.1 of the License, or (at your option) any later version.
 
     This library is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
     Lesser General Public License for more details.
 
     You should have received a copy of the GNU Lesser General Public
     License along with this library; if not, write to the Free Software
     Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package com.github.mikanbako.ant.jlinttask;
 
 import java.io.IOException;
 import java.util.List;
 
 /**
  * This class executes command.
  */
 /* package */ interface CommandExecutor {
     /**
      * Execute a command.
      *
      * @param command Program and its arguments
      * @param output {@link StringBuilder} that is appended for the standard
      *  output from command
      * @param error {@link StringBuilder} that is appended for the standard
      *  error from command
      * @return Exit code of program.
      * @throws IOException If I/O error occurs
      * @throws InterruptedException If other thread is interrupted.
      */
    int execute(List<String> command, StringBuilder output, StringBuilder error) throws IOException, InterruptedException;
 }
