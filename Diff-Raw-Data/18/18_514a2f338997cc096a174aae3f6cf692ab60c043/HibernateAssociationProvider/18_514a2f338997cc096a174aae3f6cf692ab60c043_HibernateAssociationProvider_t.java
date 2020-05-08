 /*
  * Copyright 2007-2011 the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.shept.persistence.provider.hibernate;
 
 import java.io.Serializable;
import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.shept.beans.support.ReloadableAssociation;
 import org.shept.persistence.provider.DaoUtils;
 import org.shept.persistence.provider.DataLoadException;
 import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
 import org.springframework.util.ReflectionUtils;
 
 /**
  * @version $$Id: HibernateAssociationProvider.java 34 2010-08-20 16:46:49Z aha $$
  * 
  * @author Andi
  * 
  */
 public class HibernateAssociationProvider extends AbstractHibernateListProvider {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.shept.persistence.provider.AbstractScrollingListProvider#loadListFirst
 	 * ()
 	 */
	@SuppressWarnings({ "rawtypes" })
 	@Override
 	public List<?> loadListFirst() {
 		ReloadableAssociation ass = (ReloadableAssociation) getFilterDefinition();
 		Object model = ass.getSourceModel();
 
 		// reload the object again ...
 		// hibernateDao.getHibernateTemplate().refresh(model) won't work, so here's a workaround ...
 		// ... to get rid of this dreaded and random LazyLoadingExceptions when opening the link ...
 		
 		Object idxObj = DaoUtils.getIdValue((HibernateDaoSupport) this.dao, model);
		if (idxObj == null) {
			// can't load from database so we have nothing here ...
			// we should not use existing transient objects from the cache !
			return new ArrayList();
 		}
		
		model = getHibernateTemplate().load(model.getClass(), (Serializable) idxObj);
 
		Object res = ReflectionUtils.invokeMethod(ass.getAssociationMethod(),model);
 		if (!(res instanceof List)) {
 			throw new DataLoadException(
 					"Could not provide the requested data '"
 							+ ass.getAssociationMethod().getName()
 							+ "' from DataSource '"
 							+ ass.getSourceModel().getClass() + ". Result is '"
 							+ res + "' but a List is required");
 		}
 		eol = true;
 		return (List<?>) res;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.shept.persistence.provider.AbstractScrollingListProvider#loadListNext
 	 * ()
 	 */
 	@Override
 	public List<?> loadListNext() {
 		return Collections.emptyList();
 	}
 
 }
