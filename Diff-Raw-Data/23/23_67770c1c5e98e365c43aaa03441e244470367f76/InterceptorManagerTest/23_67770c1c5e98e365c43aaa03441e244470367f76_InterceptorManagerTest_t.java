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
 package br.octahedron.cotopaxi.interceptor;
 
 import static junit.framework.Assert.assertEquals;
 
 import java.lang.annotation.Annotation;
 
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author Danilo Queiroz - DaniloQueiroz@octahedron.com.br
  * 
  */
 public class InterceptorManagerTest {
 
 	private int order;
 	private TestInterceptor interceptor1;
 	private TestInterceptor interceptor2;
 	private InterceptorManager manager;
 	private TestInterceptor interceptor3;
 
 	@Before
 	public void setUp() {
 		order = 1;
 		manager = new InterceptorManager();
 		interceptor1 = new TestInterceptor(TestingOne.class);
 		interceptor2 = new TestInterceptor(TestingTwo.class);
 		interceptor3 = new TestInterceptor(TestingThree.class);
 
 	}
 
 	@Test
 	public void testLoadInterceptors() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
 		assertEquals(0, this.manager.templateInterceptors.size());
 		assertEquals(0, this.manager.finalizerInterceptors.size());
 		this.manager.addInterceptor("br.octahedron.cotopaxi.interceptor.FakeTemplateInterceptor");
 		this.manager.addInterceptor("br.octahedron.cotopaxi.interceptor.FakeFinalizerInterceptor");
 		assertEquals(1, this.manager.templateInterceptors.size());
 		assertEquals(1, this.manager.finalizerInterceptors.size());
 	}
 
 	@Test
	public void testInterceptorManager1() throws SecurityException, NoSuchMethodException {
 		this.manager.addControllerInterceptor(interceptor1);
 		this.manager.addControllerInterceptor(interceptor2);
 		this.manager.addControllerInterceptor(interceptor3);
		this.manager.execute(AnnotatedClass.class.getMethod("test", null));
 
 		assertEquals(1, interceptor1.myOrder);
 		assertEquals(TestingOne.class, interceptor1.receivedAnn.annotationType());
 		assertEquals(2, interceptor2.myOrder);
 		assertEquals(TestingTwo.class, interceptor2.receivedAnn.annotationType());
 		assertEquals(3, interceptor3.myOrder);
 		assertEquals(TestingThree.class, interceptor3.receivedAnn.annotationType());
 	}
 
 	@Test
	public void testInterceptorManager2() throws SecurityException, NoSuchMethodException {
 		this.manager.addControllerInterceptor(interceptor3);
 		this.manager.addControllerInterceptor(interceptor2);
 		this.manager.addControllerInterceptor(interceptor1);
		this.manager.execute(AnnotatedClass.class.getMethod("test", null));
 
 		assertEquals(1, interceptor3.myOrder);
 		assertEquals(TestingThree.class, interceptor3.receivedAnn.annotationType());
 		assertEquals(2, interceptor2.myOrder);
 		assertEquals(TestingTwo.class, interceptor2.receivedAnn.annotationType());
 		assertEquals(3, interceptor1.myOrder);
 		assertEquals(TestingOne.class, interceptor1.receivedAnn.annotationType());
 	}
 
 	class TestInterceptor extends ControllerInterceptor {
 
 		private Class<? extends Annotation> ann;
 		private Annotation receivedAnn;
 		private int myOrder;
 
 		public TestInterceptor(Class<? extends Annotation> ann) {
 			this.ann = ann;
 		}
 
 		@Override
 		public void execute(Annotation ann) {
 			this.receivedAnn = ann;
 			this.myOrder = order++;
 		}
 
 		@Override
 		public Class<? extends Annotation> getInterceptorAnnotation() {
 			return ann;
 		}
 
 	}
 
 }
