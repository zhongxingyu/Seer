 /*
  *  This file is part of Cotopaxi.
  *
  *  Cotopaxi is free software: you can redistribute it and/or modify
  *  it under the terms of the Lesser GNU General Public License as published
  *  by the Free Software Foundation, either version 3 of the License, or
  *  any later version.
  *
  *  Cotopaxi is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  Lesser GNU General Public License for more details.
  *
  *  You should have received a copy of the Lesser GNU General Public License
  *  along with Cotopaxi. If not, see <http://www.gnu.org/licenses/>.
  */
 package br.octahedron.cotopaxi;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.verify;
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import br.octahedron.cotopaxi.controller.FacadeTwo;
 import br.octahedron.cotopaxi.controller.FiltersHelper;
 import br.octahedron.cotopaxi.controller.MyExceptionFilterAfter;
 import br.octahedron.cotopaxi.controller.MyExceptionFilterBefore;
 import br.octahedron.cotopaxi.controller.MyFilter;
 import br.octahedron.cotopaxi.controller.filter.FilterException;
 import br.octahedron.cotopaxi.controller.filter.FilterExecutor;
 import br.octahedron.cotopaxi.metadata.MetadataMapper;
 import br.octahedron.cotopaxi.metadata.PageNotFoundExeption;
 import br.octahedron.cotopaxi.metadata.annotation.Action.ActionMetadata;
 import br.octahedron.cotopaxi.metadata.annotation.Action.HTTPMethod;
 
 /**
  * @author Danilo Penna Queiroz - daniloqueiroz@octahedron.com.br
  */
 public class FilterExecutorTest {
 
 	private FilterExecutor filterExec;
 	private MetadataMapper mapper;
 
 	@Before
 	public void setUp() throws SecurityException, NoSuchMethodException {
 		FiltersHelper.reset();
 		CotopaxiConfigView.reset();
 		CotopaxiConfig config = CotopaxiConfigView.getInstance().getCotopaxiConfig();
 		config.addModelFacade(FacadeTwo.class);
 		this.mapper = new MetadataMapper(CotopaxiConfigView.getInstance());
 		this.filterExec = new FilterExecutor(CotopaxiConfigView.getInstance());
 		
 	}
 	
 	@Test
 	public void controllerTest8() throws IllegalArgumentException, FilterException, IllegalAccessException, PageNotFoundExeption {
 		/*
 		 * This test checks many atts and local Filter
 		 */
 		// Prepare test
 		RequestWrapper request = createMock(RequestWrapper.class);
 		expect(request.getURL()).andReturn("/two/many").atLeastOnce();
 		expect(request.getHTTPMethod()).andReturn(HTTPMethod.POST).atLeastOnce();
 		expect(request.getFormat()).andReturn(null);
 		replay(request);
 
 		// invoking the filterexecutor
 		ActionMetadata actionMetadata = this.mapper.getMapping(request).getActionMetadata();
 		filterExec.executeFiltersBefore(actionMetadata, request);
 		filterExec.executeFiltersAfter(actionMetadata, request, null);
 
 		// check test results
 		verify(request);
 		assertEquals(1, FiltersHelper.getFilterBefore());
 		assertEquals(1, FiltersHelper.getFilterAfter());
 	}
 
 	@Test
 	public void controllerTest9() throws IllegalArgumentException, FilterException, IllegalAccessException, PageNotFoundExeption {
 		/*
 		 * This test using Global Filter
 		 */
 		// Prepare test
 		RequestWrapper request = createMock(RequestWrapper.class);
 		expect(request.getURL()).andReturn("/two/varargs").atLeastOnce();
 		expect(request.getHTTPMethod()).andReturn(HTTPMethod.POST).atLeastOnce();
 		expect(request.getFormat()).andReturn(null);
 		replay(request);
 		CotopaxiConfig config = CotopaxiConfigView.getInstance().getCotopaxiConfig();
 		config.addGlobalFilter(MyFilter.class);
 
 		// invoking the filterexecutor
 		ActionMetadata actionMetadata = this.mapper.getMapping(request).getActionMetadata();
 		filterExec.executeFiltersBefore(actionMetadata, request);
 		filterExec.executeFiltersAfter(actionMetadata, request, null);
 
 		// check test results
 		verify(request);
 		assertEquals(1, FiltersHelper.getFilterBefore());
 		assertEquals(1, FiltersHelper.getFilterAfter());
 	}
 
 	@Test
 	public void controllerTest10() throws IllegalArgumentException, FilterException, IllegalAccessException, PageNotFoundExeption {
 		/*
 		 * This test checks many atts with Filters (Global and Local)
 		 */
 		// Prepare test
 		RequestWrapper request = createMock(RequestWrapper.class);
 		expect(request.getURL()).andReturn("/two/many").atLeastOnce();
 		expect(request.getHTTPMethod()).andReturn(HTTPMethod.POST).atLeastOnce();
 		expect(request.getFormat()).andReturn(null);
 		replay(request);
 		CotopaxiConfig config = CotopaxiConfigView.getInstance().getCotopaxiConfig();
 		config.addGlobalFilter(MyFilter.class);
 
 		// invoking the filterexecutor
 		ActionMetadata actionMetadata = this.mapper.getMapping(request).getActionMetadata();
 		filterExec.executeFiltersBefore(actionMetadata, request);
 		filterExec.executeFiltersAfter(actionMetadata, request, null);
 
 		// check test results
 		verify(request);
 		assertEquals(2, FiltersHelper.getFilterBefore());
 		assertEquals(2, FiltersHelper.getFilterAfter());
 	}
 
 	@Test(expected = FilterException.class)
 	public void controllerTest11() throws IllegalArgumentException, FilterException, IllegalAccessException, PageNotFoundExeption {
 		/*
 		 * This test checks many atts with Filters (Global and Local)
 		 */
 		// Prepare test
 		RequestWrapper request = createMock(RequestWrapper.class);
 		expect(request.getURL()).andReturn("/two/many").atLeastOnce();
 		expect(request.getHTTPMethod()).andReturn(HTTPMethod.POST).atLeastOnce();
 		expect(request.getFormat()).andReturn(null);
 		replay(request);
 		CotopaxiConfig config = CotopaxiConfigView.getInstance().getCotopaxiConfig();
 		config.addGlobalFilter(MyExceptionFilterBefore.class);
 		try {
 			// invoking the filterexecutor
 			ActionMetadata actionMetadata = this.mapper.getMapping(request).getActionMetadata();
 			filterExec.executeFiltersBefore(actionMetadata, request);
 			filterExec.executeFiltersAfter(actionMetadata, request, null);
 		} finally {
 			// check test results
 			verify(request);
 			assertEquals(1, FiltersHelper.getFilterBefore());
 			assertEquals(0, FiltersHelper.getFilterAfter());
 		}
 	}
 
 	@Test(expected = FilterException.class)
 	public void controllerTest12() throws IllegalArgumentException, FilterException, IllegalAccessException, PageNotFoundExeption {
 		/*
 		 * This test checks many atts with Filters (Global and Local)
 		 */
 		// Prepare test
 		RequestWrapper request = createMock(RequestWrapper.class);
 		expect(request.getURL()).andReturn("/two/many").atLeastOnce();
 		expect(request.getHTTPMethod()).andReturn(HTTPMethod.POST).atLeastOnce();
 		expect(request.getFormat()).andReturn(null);
 		replay(request);
 		CotopaxiConfig config = CotopaxiConfigView.getInstance().getCotopaxiConfig();
 		config.addGlobalFilter(MyExceptionFilterAfter.class);
 		try {
 			// invoking the filterexecutor
 			ActionMetadata actionMetadata = this.mapper.getMapping(request).getActionMetadata();
 			filterExec.executeFiltersBefore(actionMetadata, request);
 			filterExec.executeFiltersAfter(actionMetadata, request, null);
 		} finally {
 			// check test results
 			verify(request);
 			assertEquals(2, FiltersHelper.getFilterBefore());
			assertEquals(1, FiltersHelper.getFilterAfter());
 		}
 	}
 	
 }
