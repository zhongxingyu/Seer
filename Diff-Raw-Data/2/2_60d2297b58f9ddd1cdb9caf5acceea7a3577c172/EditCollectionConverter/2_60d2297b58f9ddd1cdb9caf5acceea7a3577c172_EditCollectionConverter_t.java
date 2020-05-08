 /*
  * jPOS Presentation Manager [http://jpospm.blogspot.com]
  * Copyright (C) 2010 Jeronimo Paoletti [jeronimo.paoletti@gmail.com]
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.jpos.ee.pm.struts.converter;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.jpos.ee.pm.converter.ConverterException;
 import org.jpos.ee.pm.core.Entity;
 import org.jpos.ee.pm.core.EntityInstanceWrapper;
 import org.jpos.ee.pm.core.Field;
 import org.jpos.ee.pm.core.Operation;
 import org.jpos.ee.pm.core.PMLogger;
 import org.jpos.ee.pm.struts.PMEntitySupport;
 
 public class EditCollectionConverter extends StrutsEditConverter {
 
 	public Object build(Entity entity, Field field, Operation operation,
 			EntityInstanceWrapper einstance, Object value) throws ConverterException {
 		try{
 			String collection_class = getConfig("collection-class");
 			if(collection_class == null) throw new ConverterException("collection-class must be defined");
 			
 			Collection<Object> result = (Collection<Object>) PMEntitySupport.getInstance().getPmservice().getFactory().newInstance (collection_class);
 			String s = (String)value;
 			if(s.trim().compareTo("")==0) return result;
 			String[] ss = s.split(";");
 			if(ss.length > 0 ){
 				String eid = ss[0].split("@")[0];
 				PMEntitySupport es = PMEntitySupport.getInstance();
 				Entity e = es.getPmservice().getEntity(eid);
 				if(e==null) throw new ConverterException("Cannot find entity "+eid);
				List<?> list = e.getList();
 				for (int i = 0; i < ss.length; i++) {
 					Integer x = Integer.parseInt(ss[i].split("@")[1]);
 					result.add(list.get(x));
 				}
 			}
 			return result;
 		} catch (ConverterException e2) {
 			throw e2;
 		} catch (Exception e1) {
 			PMLogger.error(e1);
 			throw new ConverterException("Cannot convert collection");
 		}
 	}
 
 	public String visualize(Entity entity, Field field, Operation operation,
 			EntityInstanceWrapper einstance, String extra) throws ConverterException {
 		return super.visualize("collection_converter.jsp?filter="+getConfig("filter")+"&entity="+getConfig("entity"));
 	}
 
 }
