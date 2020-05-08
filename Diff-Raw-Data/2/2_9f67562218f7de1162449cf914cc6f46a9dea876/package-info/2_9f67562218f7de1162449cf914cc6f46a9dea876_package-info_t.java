 /*-
  * Copyright © 2010 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 /**
  * <code>gda.data.fileregistrar</code> contains code used for notifying (possibly external) entities
  * when files are created. Main purpose is archiving and indexing, but data processing could also be 
  * triggered. 
  * 
  * 
  *      <p> There are two main routes. One is directly by a detector, a processing task or anything else
  *      that creates a file that needs archiving to find classes implementing IFileRegistrar from the Finder
  *      and call their registerFile method(s) or use the FileRegistrarHelper class to ease that task.
  *      <p> The other route is implemented in the concrete FileRegistrar class which also implements 
  *      DataWriterExtender. If that extender is added to a DataWriter (via a DataWriterFactory for example)
 *      it receives ScanDataPoints through it. The SDP are inspected and Filenames in it that originate from 
  *      file-writing detectors are registered automatically. 
  *      
  */
 package gda.data.fileregistrar;
