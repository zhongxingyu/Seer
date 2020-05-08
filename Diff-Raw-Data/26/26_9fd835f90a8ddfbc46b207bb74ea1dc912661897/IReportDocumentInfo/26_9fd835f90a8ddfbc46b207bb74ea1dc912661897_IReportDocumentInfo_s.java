 /*******************************************************************************
  * Copyright (c) 2004 Actuate Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *  Actuate Corporation  - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.birt.report.engine.api;
 
 import org.eclipse.birt.core.exception.BirtException;
 
 /**
  * the interface used to access the traisent informations of a report document.
  * 
 * @version $Revision: 1.1 $ $Date: 2006/04/05 13:22:49 $
  */
 public interface IReportDocumentInfo
 {
 	IReportDocument openReportDocument() throws BirtException;
 }
