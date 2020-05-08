 /**
  * Copyright (c) 2009 The RCER Development Team.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
  * this entire header must remain intact.
  *
  * $Id$
  */
 package net.sf.rcer.cts;
 
 import java.util.Date;
 
 import net.sf.rcer.cts.rfc.ReadTransportResponse;
 
 /**
  * Abstract superclass of {@link TransportOrder} and {@link TransportTask}.
  * @author vwegert
  *
  */
 public abstract class AbstractTransport {
 
 	/**
 	 * The {@link TransportSystem} instance used to read the transport object. 
 	 */
 	protected TransportSystem transportSystem;
 	
 	private String id;
 	private TransportStatus status;
 	private TransportCategory category;
 	private String owner;
 	private Date lastChangeDate;
 	private String description;
 
 	/**
 	 * Constructor that extracts the header data.
 	 * @param transportSystem the {@link TransportSystem} instance used to read the object
 	 * @param id the transport object ID
 	 * @param result the RFC call result to read the header data from
 	 * @throws TransportException 
 	 */
	AbstractTransport(TransportSystem transportSystem, String id, ReadTransportResponse result) throws TransportException {
 		this.transportSystem = transportSystem;
 		this.id = id;
 		this.status = TransportStatus.fromInternalString(result.getHeader().getStatus());
 		this.category = TransportCategory.fromInternalString(result.getHeader().getCategory());
 		this.owner = result.getHeader().getUser();
 		this.lastChangeDate = new Date(result.getHeader().getLastChangeDate().getTime() + result.getHeader().getLastChangeTime().getTime());
 		this.description = result.isTextMissing() ? "" : result.getText().getText(); //$NON-NLS-1$
 
 		// TODO parse the object list and object list keys
 //		table            "TT_E071"              = ObjectListEntry     objectList     comment "object list of the transport";
 //		field "TRKORR"   = String  transportID    comment "Request or Task";
 //		field "AS4POS"   = int     row            comment "Row number";
 //		field "PGMID"    = String  programID      comment "Program ID";
 //		field "OBJECT"   = String  objectType     comment "Object Type";
 //		field "OBJ_NAME" = String  objectName     comment "Object Name";
 		
 //		field "OBJFUNC"  = String  objectFunction comment "Object Function";
 //		field "LOCKFLAG" = boolean locked         comment "Status";
 //
 //		table            "TT_E071K"             = ObjectListKeyEntry  objectListKeys comment "key list of the transport";
 //		field "TRKORR"     = String transportID      comment "Request or Task";
 //		field "AS4POS"     = int    row              comment "Row number";
 //		field "PGMID"      = String programID        comment "Program ID";
 //		field "OBJECT"     = String objectType       comment "Object Type";
 //		field "OBJNAME"    = String objectName       comment "Object Name";
 		
 //		field "MASTERTYPE" = String masterObjectType comment "Master Object Type";
 //		field "MASTERNAME" = String masterObjectName comment "Master Object Name";
 //		field "VIEWNAME"   = String viewName         comment "View Name";
 //		field "TABKEY"     = String tableKey         comment "Table Key";
 
 	}
 
 	/**
 	 * @return the {@link TransportSystem} instance
 	 */
 	TransportSystem getTransportSystem() {
 		return transportSystem;
 	}
 	
 	/**
 	 * @return the transport ID
 	 */
 	public String getID() {
 		return id;
 	}
 
 	/**
 	 * @return the status
 	 */
 	public TransportStatus getStatus() {
 		return status;
 	}
 
 	/**
 	 * @return the category
 	 */
 	public TransportCategory getCategory() {
 		return category;
 	}
 
 	/**
 	 * @return the owner
 	 */
 	public String getOwner() {
 		return owner;
 	}
 
 	/**
 	 * @return the lastChangeDate
 	 */
 	public Date getLastChangeDate() {
 		return lastChangeDate;
 	}
 
 	/**
 	 * @return the description
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 }
