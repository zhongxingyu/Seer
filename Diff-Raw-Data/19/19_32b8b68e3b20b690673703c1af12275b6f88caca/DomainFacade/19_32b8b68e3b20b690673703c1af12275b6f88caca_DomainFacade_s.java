 /* - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
  Copyright (C) 2008 CEJUG - Ceará Java Users Group
  
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
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
  
  This file is part of the CEJUG-CLASSIFIEDS Project - an  open source classifieds system
  originally used by CEJUG - Ceará Java Users Group.
  The project is hosted https://cejug-classifieds.dev.java.net/
  
  You can contact us through the mail dev@cejug-classifieds.dev.java.net
  - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - */
 package net.java.dev.cejug.classifieds.server.ejb3.entity.facade;
 
import java.util.List;

 import javax.ejb.Stateless;
 
 import net.java.dev.cejug.classifieds.server.ejb3.entity.DomainEntity;
 
 /**
  * @author $Author$
  * @version $Rev$ ($Date$)
  */
 @Stateless
 public class DomainFacade extends EntityFacade<DomainEntity> implements
 		DomainFacadeLocal {
 
 	@Override
 	public DomainEntity get(int id) throws Exception {
 		return manager.find(DomainEntity.class, id);
 	}
 
 	public DomainEntity updateDomain(DomainEntity entity) throws Exception {
 
 		return update(entity);
 	}
 }
