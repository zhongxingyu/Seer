 /*******************************************************************************
  * Nof1 Trials helper, making life easier for clinicians and patients in N of 1 trials.
  * Copyright (C) 2012  John Lawson
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You may obtain a copy of the GNU General Public License at 
  * <http://www.gnu.org/licenses/>.
  * 
  * Contributors:
  *     John Lawson - initial API and implementation
  ******************************************************************************/
 package org.nof1trial.nof1.shared;
 
 import java.util.List;
 
import com.google.web.bindery.requestfactory.shared.Request;
 import com.google.web.bindery.requestfactory.shared.RequestContext;
 import com.google.web.bindery.requestfactory.shared.ServiceName;
 
 /**
  * @author John Lawson
  * 
  */
@ServiceName("org.nof1trial.nof1.server.ConfigProxy")
 public interface ConfigRequest extends RequestContext {
 
	public Request<List<ConfigProxy>> findAllConfigs();

	public Request<ConfigProxy> findConfig(Long id);
 
	public Request<List<ConfigProxy>> findConfigByDoctor(String doctorEmail, int maxResults);
 
	public Request<List<ConfigProxy>> findConfigByPatient(String patientEmail, int maxResults);
 
	public Request<ConfigProxy> update(ConfigProxy config);
 
 }
