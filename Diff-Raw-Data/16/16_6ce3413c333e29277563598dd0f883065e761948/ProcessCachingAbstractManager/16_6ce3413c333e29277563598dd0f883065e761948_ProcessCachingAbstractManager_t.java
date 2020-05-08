 /**
  * (C) Copyright 2010-2013. Nigel Cook. All rights reserved.
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
  * 
  * Licensed under the terms described in LICENSE file that accompanied this code, (the "License"); you may not use this file
  * except in compliance with the License. 
  * 
  *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on 
  *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the 
  *  specific language governing permissions and limitations under the License.
  *  
  *  @author Lucio P. Cossio
  */
 package n3phele.service.model;
 import n3phele.service.core.NotFoundException;
 import n3phele.service.model.core.AbstractManager;
 
 public abstract class ProcessCachingAbstractManager extends AbstractManager<CloudProcess> {
 
 	@Override
 	protected void add(CloudProcess item) throws IllegalArgumentException {
 			super.add(item);
 			
 			if(item != null) 
 			if(item.isTopLevel())
 			{
 				ChangeManager.factory().addChange(super.path);
 			}
 			else
 			{
 				ChangeManager.factory().addChange(item.getParent());				
 			}
 	}
 
 	@Override
 	protected void update(CloudProcess item)throws NotFoundException {
 		try {
 			super.update(item);
 		} catch (NotFoundException e) {
 			ChangeManager.factory().addChange(super.path);
 			throw e;
 		}
 		
 		if(item != null) 
 		if(item.isTopLevel())
 		{
 			ChangeManager.factory().addChange(item);
 		}
 		else
 		{
 			ChangeManager.factory().addChange(item.getParent());
 		}
 	}
 
 	@Override
 	protected void delete(CloudProcess item) {
 		if(item != null) {
 			super.delete(item);
 			if(item.isTopLevel())
 			{
 				ChangeManager.factory().addChange(super.path);				
 			}
 			else
 			{
 				ChangeManager.factory().addChange(item.getParent());				
 			}
 			ChangeManager.factory().addDelete(item);			
 		}
 	}
 }
