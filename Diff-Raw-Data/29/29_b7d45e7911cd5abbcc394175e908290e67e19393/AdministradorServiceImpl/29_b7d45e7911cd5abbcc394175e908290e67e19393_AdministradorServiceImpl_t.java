 /*
  * Copyright 2011 Google Inc. All Rights Reserved.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package v.server;
 
 import java.util.HashMap;
 import java.util.List;
 
 import javax.ejb.EJB;
 
 import v.client.AppConstants;
 import v.client.rpc.AdministradorService;
 import v.facade.AdministradorFacadeLocal;
 
 import com.extjs.gxt.ui.client.data.BasePagingLoadResult;
 import com.extjs.gxt.ui.client.data.FilterConfig;
 import com.extjs.gxt.ui.client.data.FilterPagingLoadConfig;
 import com.extjs.gxt.ui.client.data.PagingLoadResult;
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
import v.modelo.Caja;
 import v.modelo.Usuario;
 
 @SuppressWarnings("serial")
 public class AdministradorServiceImpl extends RemoteServiceServlet implements AdministradorService {
 
 	@EJB
 	AdministradorFacadeLocal administradorFacade;
 	
 	@Override
 	public PagingLoadResult<Usuario> listarUsuarios(FilterPagingLoadConfig config) {
 		int count = administradorFacade.getTotalUsuarios();
 		List<FilterConfig> filters = config.getFilterConfigs();
 		int start = config.getOffset();
 		int limit = AppConstants.PAGE_SIZE;
 		HashMap<String, Object> plainFilters = Filter.processFilters(filters);
 		List<Usuario> users = administradorFacade.listarUsuarios(plainFilters, start, limit);
		Converter<Usuario> cu = new Converter<Usuario>();
		Converter<Caja> cc = new Converter<Caja>();
		users = cu.convertObjects(users);
		for(Usuario u: users){
			if(u.getRol().equals(AppConstants.CAJERO_ROL)){
				u.setCaja(cc.convertObject(u.getCaja()));
			}
		}
 		return new BasePagingLoadResult<Usuario>(users, config.getOffset(), count);
 	}
 	
 }
